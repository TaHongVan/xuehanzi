package com.hanzii.service;

import com.hanzii.dto.response.ExcelImportResponse;
import com.hanzii.entity.Topic;
import com.hanzii.exception.BadRequestException;
import com.hanzii.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExcelImportService {

    private static final String[] VOCAB_HEADERS = {"Chinese", "Pinyin", "Meaning", "Example", "HSK Level", "Topic"};
    private static final String[] SENTENCE_HEADERS = {"Chinese Sentence", "Vietnamese Sentence", "HSK Level", "Topic"};

    private final AdminVocabularyService adminVocabularyService;
    private final AdminSentenceService adminSentenceService;
    private final TopicRepository topicRepository;
    private final DataFormatter dataFormatter = new DataFormatter();

    public Resource generateVocabularyTemplate() throws IOException {
        return generateTemplate("Vocabulary", VOCAB_HEADERS, new String[][]{
                {"你好", "nǐ hǎo", "Xin chào", "你好，我是小明。", "1", "Giao tiếp hàng ngày"}
        });
    }

    public Resource generateSentenceTemplate() throws IOException {
        return generateTemplate("Sentences", SENTENCE_HEADERS, new String[][]{
                {"我明天去北京。", "Ngày mai tôi đi Bắc Kinh.", "2", "Du lịch"}
        });
    }

    public ExcelImportResponse importVocabularyExcel(MultipartFile file) throws IOException {
        validateFile(file);

        List<String> errors = new ArrayList<>();
        Map<String, Topic> topicCache = new HashMap<>();
        int imported = 0;
        int updated = 0;
        int skipped = 0;
        int totalRows = 0;

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isEmptyRow(row, 3)) {
                    continue;
                }
                totalRows++;

                try {
                    String chinese = getCellString(row, 0);
                    String pinyin = getCellString(row, 1);
                    String meaning = getCellString(row, 2);
                    String example = getCellString(row, 3);
                    int hskLevel = (int) getCellNumeric(row, 4);
                    String topicName = getCellString(row, 5);

                    String validationError = validateVocabularyRow(chinese, pinyin, meaning, example, hskLevel, topicName);
                    if (validationError != null) {
                        errors.add("Dòng " + (i + 1) + ": " + validationError);
                        skipped++;
                        continue;
                    }

                    Topic topic = resolveTopic(topicName, topicCache);
                    boolean isUpdate = adminVocabularyService.existsByChineseAndTopic(chinese, topic.getId());
                    adminVocabularyService.upsertFromExcel(chinese, pinyin, meaning, example, hskLevel, topic);
                    if (isUpdate) {
                        updated++;
                    } else {
                        imported++;
                    }
                } catch (Exception e) {
                    errors.add("Dòng " + (i + 1) + ": " + e.getMessage());
                    skipped++;
                }
            }
        }

        return ExcelImportResponse.builder()
                .totalRows(totalRows)
                .imported(imported)
                .updated(updated)
                .skipped(skipped)
                .errors(errors)
                .build();
    }

    public ExcelImportResponse importSentenceExcel(MultipartFile file) throws IOException {
        validateFile(file);

        List<String> errors = new ArrayList<>();
        Map<String, Topic> topicCache = new HashMap<>();
        int imported = 0;
        int updated = 0;
        int skipped = 0;
        int totalRows = 0;

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isEmptyRow(row, 2)) {
                    continue;
                }
                totalRows++;

                try {
                    String chinese = getCellString(row, 0);
                    String vietnamese = getCellString(row, 1);
                    int hskLevel = (int) getCellNumeric(row, 2);
                    String topicName = getCellString(row, 3);

                    String validationError = validateSentenceRow(chinese, vietnamese, hskLevel, topicName);
                    if (validationError != null) {
                        errors.add("Dòng " + (i + 1) + ": " + validationError);
                        skipped++;
                        continue;
                    }

                    Topic topic = resolveTopic(topicName, topicCache);
                    boolean isUpdate = adminSentenceService.existsByChineseSentence(chinese);
                    adminSentenceService.upsertFromExcel(chinese, vietnamese, hskLevel, topic);
                    if (isUpdate) {
                        updated++;
                    } else {
                        imported++;
                    }
                } catch (Exception e) {
                    errors.add("Dòng " + (i + 1) + ": " + e.getMessage());
                    skipped++;
                }
            }
        }

        return ExcelImportResponse.builder()
                .totalRows(totalRows)
                .imported(imported)
                .updated(updated)
                .skipped(skipped)
                .errors(errors)
                .build();
    }

    private Topic resolveTopic(String topicName, Map<String, Topic> topicCache) {
        String normalizedName = topicName == null || topicName.isBlank() ? "Khác" : topicName.trim();
        return topicCache.computeIfAbsent(normalizedName, name -> topicRepository.findByName(name)
                .orElseGet(() -> topicRepository.save(Topic.builder()
                        .name(name)
                        .description("Imported topic")
                        .build())));
    }

    private Resource generateTemplate(String sheetName, String[] headers, String[][] samples) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(sheetName);
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
                sheet.setColumnWidth(i, 6000);
            }
            for (int r = 0; r < samples.length; r++) {
                Row row = sheet.createRow(r + 1);
                for (int c = 0; c < samples[r].length; c++) {
                    row.createCell(c).setCellValue(samples[r][c]);
                }
            }
            workbook.write(out);
            return new ByteArrayResource(out.toByteArray());
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.toLowerCase().endsWith(".xlsx") && !filename.toLowerCase().endsWith(".xls"))) {
            throw new BadRequestException("Only .xlsx and .xls files are supported");
        }
    }

    private boolean isEmptyRow(Row row, int requiredCols) {
        for (int i = 0; i < requiredCols; i++) {
            if (!getCellString(row, i).isBlank()) {
                return false;
            }
        }
        return true;
    }

    private String getCellString(Row row, int index) {
        Cell cell = row.getCell(index);
        if (cell == null) {
            return "";
        }
        return dataFormatter.formatCellValue(cell).trim();
    }

    private double getCellNumeric(Row row, int index) {
        Cell cell = row.getCell(index);
        if (cell == null) {
            return 1;
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getNumericCellValue();
        }
        try {
            return Double.parseDouble(dataFormatter.formatCellValue(cell).trim());
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private String validateVocabularyRow(String chinese, String pinyin, String meaning, String example,
                                         int hskLevel, String topicName) {
        if (chinese.isBlank() || pinyin.isBlank() || meaning.isBlank()) {
            return "Thiếu Chinese/Pinyin/Meaning";
        }
        if (hskLevel < 1 || hskLevel > 6) {
            return "HSK level phải từ 1-6";
        }
        if (chinese.length() > 50) return "Chinese vượt quá 50 ký tự";
        if (pinyin.length() > 100) return "Pinyin vượt quá 100 ký tự";
        if (meaning.length() > 500) return "Meaning vượt quá 500 ký tự";
        if (example != null && example.length() > 1000) return "Example vượt quá 1000 ký tự";
        if (topicName != null && topicName.trim().length() > 100) return "Topic vượt quá 100 ký tự";
        return null;
    }

    private String validateSentenceRow(String chinese, String vietnamese, int hskLevel, String topicName) {
        if (chinese.isBlank() || vietnamese.isBlank()) {
            return "Thiếu câu tiếng Trung hoặc tiếng Việt";
        }
        if (hskLevel < 1 || hskLevel > 6) {
            return "HSK level phải từ 1-6";
        }
        if (chinese.length() > 500) return "Chinese Sentence vượt quá 500 ký tự";
        if (vietnamese.length() > 500) return "Vietnamese Sentence vượt quá 500 ký tự";
        if (topicName != null && topicName.trim().length() > 100) return "Topic vượt quá 100 ký tự";
        return null;
    }
}
