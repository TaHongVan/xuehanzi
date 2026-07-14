import { useEffect, useMemo, useRef, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { Button, Card, message, Select, Space, Tag, Tooltip, Typography } from 'antd'
import {
  ClearOutlined,
  EyeInvisibleOutlined,
  EyeOutlined,
  HeartOutlined,
  HighlightOutlined,
  InfoCircleOutlined,
  PlayCircleOutlined,
  ReloadOutlined,
  SoundOutlined,
} from '@ant-design/icons'
import HanziWriter from 'hanzi-writer'
import { topicApi, vocabularyApi } from '../api/services'

const { Title, Text } = Typography

const WRITER_SIZE = 260
const WRITING_STROKE_COLOR = '#0077c8'

function HanziPracticeTile({ character, index, showTemplate, wordInfo, onHideTemplate }) {
  const writerHostRef = useRef(null)
  const writerRef = useRef(null)
  const quizRef = useRef(false)
  const [isQuizMode, setIsQuizMode] = useState(false)
  const [result, setResult] = useState(null)

  useEffect(() => {
    if (!writerHostRef.current) return

    writerHostRef.current.innerHTML = ''
    quizRef.current = false
    setIsQuizMode(false)
    setResult(null)

    writerRef.current = HanziWriter.create(writerHostRef.current, character, {
      width: WRITER_SIZE,
      height: WRITER_SIZE,
      padding: 18,
      showOutline: showTemplate,
      showCharacter: showTemplate,
      strokeColor: WRITING_STROKE_COLOR,
      radicalColor: WRITING_STROKE_COLOR,
      outlineColor: '#e8dfd6',
      drawingColor: WRITING_STROKE_COLOR,
      highlightColor: WRITING_STROKE_COLOR,
      drawingWidth: 30,
      strokeAnimationSpeed: 1,
      delayBetweenStrokes: 120,
    })
  }, [character])

  useEffect(() => {
    const writer = writerRef.current
    if (!writer) return

    if (showTemplate) {
      writer.showOutline({ duration: 150 })
      writer.showCharacter({ duration: 150 })
    } else {
      writer.hideOutline({ duration: 150 })
      writer.hideCharacter({ duration: 150 })
    }
  }, [showTemplate])

  const speakCharacter = () => {
    if (!('speechSynthesis' in window)) {
      message.warning('Trình duyệt không hỗ trợ phát âm')
      return
    }
    window.speechSynthesis.cancel()
    const utterance = new SpeechSynthesisUtterance(character)
    utterance.lang = 'zh-CN'
    utterance.rate = 0.75
    window.speechSynthesis.speak(utterance)
  }

  const animateCharacter = () => {
    const writer = writerRef.current
    if (!writer) return
    quizRef.current = false
    setIsQuizMode(false)
    setResult(null)
    writer.cancelQuiz()
    writer.animateCharacter()
  }

  const startQuiz = () => {
    const writer = writerRef.current
    if (!writer) return

    quizRef.current = true
    setIsQuizMode(true)
    setResult({ mistakes: 0, strokes: 0, complete: false })
    writer.quiz({
      showHintAfterMisses: 2,
      highlightOnComplete: true,
      leniency: 1.15,
      onCorrectStroke: (summaryData) => {
        setResult(current => ({
          mistakes: current?.mistakes || 0,
          strokes: summaryData?.totalStrokes || current?.strokes || 0,
          complete: false,
        }))
      },
      onMistake: (strokeData) => {
        setResult(current => ({
          mistakes: strokeData?.totalMistakes || (current?.mistakes || 0) + 1,
          strokes: current?.strokes || 0,
          complete: false,
        }))
      },
      onComplete: (summaryData) => {
        quizRef.current = false
        setIsQuizMode(false)
        setResult({
          mistakes: summaryData?.totalMistakes || 0,
          strokes: summaryData?.totalStrokes || 0,
          complete: true,
        })
        message.success(`Đã viết đúng chữ ${character}`)
      },
    })
  }

  const resetPractice = () => {
    writerRef.current?.cancelQuiz()
    writerRef.current?.setCharacter(character)
    onHideTemplate()
    setTimeout(() => startQuiz(), 0)
  }

  return (
    <div className="writer-tile">
      <div className="writer-tile-heading">
        <span className="writer-tile-character">{character}</span>
        <Text type="secondary">Chữ {index + 1}</Text>
      </div>
      <div className="writer-board">
        <div className="writer-grid" />
        <div ref={writerHostRef} className="hanzi-writer-host" />
      </div>
      <div className="writer-icon-row">
        <Tooltip title="Hiện cách viết từng nét">
          <Button icon={<PlayCircleOutlined />} onClick={animateCharacter} />
        </Tooltip>
        <Tooltip title="Luyện viết theo nét chữ mẫu">
          <Button className={isQuizMode ? 'writer-icon-active' : ''} icon={<HighlightOutlined />} onClick={startQuiz} />
        </Tooltip>
        <Tooltip title="Nghe phát âm chữ đang viết">
          <Button icon={<SoundOutlined />} onClick={speakCharacter} />
        </Tooltip>
        <Tooltip title="Xóa và viết lại">
          <Button icon={<ClearOutlined />} onClick={resetPractice} />
        </Tooltip>
{/*         <Tooltip title={wordInfo ? `${wordInfo.pinyin} - ${wordInfo.meaning}` : 'Thong tin chu'}> */}
{/*           <Button icon={<InfoCircleOutlined />} /> */}
{/*         </Tooltip> */}
 {/*         <Tooltip title="Danh dau yeu thich"> */}
 {/*           <Button icon={<HeartOutlined />} /> */}
 {/*         </Tooltip> */}
      </div>
      {result && (
        <div className="writer-tile-result">
          <Tag color={result.complete ? 'success' : 'processing'}>
            {result.complete ? 'Hoàn thành' : 'Đang luyện'}
          </Tag>
          <Text type="secondary">Đúng: {result.strokes || 0} nét</Text>
          <Text type="secondary">Sai: {result.mistakes || 0}</Text>
        </div>
      )}
    </div>
  )
}

export default function HandwritingPage() {
  const [searchParams] = useSearchParams()
  const queryText = searchParams.get('word') || searchParams.get('char') || '你'
  const [practiceText, setPracticeText] = useState(queryText)
  const [vocabList, setVocabList] = useState([])
  const [topics, setTopics] = useState([])
  const [filters, setFilters] = useState({ hsk: null, topic: null })
  const [showTemplate, setShowTemplate] = useState(true)

  useEffect(() => {
    topicApi.list()
      .then(r => setTopics(r.data.data || []))
      .catch(() => {})
  }, [])

  useEffect(() => {
    const params = { page: 0, size: 100 }
    if (filters.hsk) params.hsk = filters.hsk
    if (filters.topic) params.topic = filters.topic

    vocabularyApi.list(params)
      .then(r => {
        const words = r.data.data.content || []
        setVocabList(words)
        if (words.length && !words.some(v => v.chineseWord === practiceText)) {
          setPracticeText(words[0].chineseWord)
        }
      })
      .catch(() => message.error('Không thể tải danh sách chữ luyện viết'))
  }, [filters])

  useEffect(() => {
    const nextText = searchParams.get('word') || searchParams.get('char')
    if (nextText) setPracticeText(nextText)
  }, [searchParams])

  const selectedWord = useMemo(
    () => vocabList.find(v => v.chineseWord === practiceText) || vocabList.find(v => v.chineseWord?.startsWith(practiceText)),
    [practiceText, vocabList]
  )

  const practiceChars = useMemo(
    () => [...(practiceText || '')].filter(char => /\p{Script=Han}/u.test(char)),
    [practiceText]
  )

  const randomWord = () => {
    if (vocabList.length === 0) return
    const random = vocabList[Math.floor(Math.random() * vocabList.length)]
    setPracticeText(random.chineseWord)
  }

  const speakWord = () => {
    if (!('speechSynthesis' in window)) {
      message.warning('Trình duyệt không hỗ trợ phát âm')
      return
    }
    window.speechSynthesis.cancel()
    const utterance = new SpeechSynthesisUtterance(practiceText)
    utterance.lang = 'zh-CN'
    utterance.rate = 0.75
    window.speechSynthesis.speak(utterance)
  }

  const toggleTemplate = () => {
    setShowTemplate(current => !current)
    message.info('Vẫn nhận diện theo dữ liệu chữ mẫu dù mẫu đang ẩn')
  }

  return (
    <div className="page-container handwriting-page">
      <div className="page-title-row">
        <div>
          <Title level={3}>Luyện viết chữ Hán</Title>
          <Text type="secondary">
            Từ ghép sẽ hiển thị đủ từng chữ Hán. Mỗi chữ có khung riêng và nét viết có màu riêng để dễ nhìn.
          </Text>
        </div>
        <Space wrap>
          <Button icon={<SoundOutlined />} onClick={speakWord}>Đọc từ</Button>
          <Button icon={showTemplate ? <EyeOutlined /> : <EyeInvisibleOutlined />} onClick={toggleTemplate}>
            {showTemplate ? 'Tắt mẫu' : 'Hiện mẫu'}
          </Button>
          <Button icon={<ReloadOutlined />} onClick={randomWord}>Ngẫu nhiên</Button>
        </Space>
      </div>

      <div className="handwriting-layout">
        <Card className="practice-card">
          <div className="practice-header">
            <div>
              <Text type="secondary">Từ đang luyện</Text>
              <div className="practice-character practice-word">{practiceText}</div>
              {selectedWord && (
                <Text className="practice-meaning">
                  {selectedWord.pinyin} - {selectedWord.meaning}
                </Text>
              )}
            </div>
            <Space wrap className="practice-selectors">
              <Select
                placeholder="Tất cả HSK"
                allowClear
                value={filters.hsk}
                onChange={(hsk) => setFilters(current => ({ ...current, hsk }))}
                style={{ width: 130 }}
                options={[1, 2, 3, 4, 5, 6].map(n => ({ value: n, label: `HSK ${n}` }))}
              />
              <Select
                placeholder="Tất cả chủ đề"
                allowClear
                value={filters.topic}
                onChange={(topic) => setFilters(current => ({ ...current, topic }))}
                style={{ width: 170 }}
                options={topics.map(topic => ({ value: topic.id, label: topic.name }))}
              />
              <Select
                value={practiceText}
                onChange={(value) => setPracticeText(value)}
                style={{ width: 260 }}
                showSearch
                optionFilterProp="label"
                options={vocabList.map(v => ({
                  value: v.chineseWord,
                  label: `${v.chineseWord} - ${v.meaning}`,
                }))}
              />
            </Space>
          </div>

          <div className="writer-tile-grid">
            {practiceChars.map((char, index) => (
              <HanziPracticeTile
                key={`${practiceText}-${index}-${char}`}
                character={char}
                index={index}
                showTemplate={showTemplate}
                wordInfo={selectedWord}
                onHideTemplate={() => setShowTemplate(false)}
              />
            ))}
          </div>
        </Card>

        <Card className="practice-result" title="Hướng dẫn">
          <Text type="secondary">
            Mỗi ô đại diện cho một chữ Hán trong từ. Bấm play để xem thứ tự nét, bấm biểu tượng bút để viết theo nét mẫu.
            Khi tắt mẫu, hệ thống vẫn chấm đúng theo hình dạng chữ mẫu của từng chữ.
          </Text>
        </Card>
      </div>
    </div>
  )
}
