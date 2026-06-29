package com.hanzii.specification;

import com.hanzii.entity.Sentence;
import org.springframework.data.jpa.domain.Specification;

public final class SentenceSpecification {

    private SentenceSpecification() {}

    public static Specification<Sentence> withFilters(Integer hskLevel, Long topicId) {
        return (root, query, cb) -> {
            var predicate = cb.or(cb.isFalse(root.get("deleted")), cb.isNull(root.get("deleted")));
            if (hskLevel != null) {
                predicate = cb.and(predicate, cb.equal(root.get("hskLevel"), hskLevel));
            }
            if (topicId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("topic").get("id"), topicId));
            }
            return predicate;
        };
    }

    public static Specification<Sentence> filter(Integer hskLevel, Long topicId) {
        return (root, query, cb) -> {
            var predicate = cb.or(cb.isFalse(root.get("deleted")), cb.isNull(root.get("deleted")));
            if (hskLevel != null && hskLevel > 0) {
                predicate = cb.and(predicate, cb.equal(root.get("hskLevel"), hskLevel));
            }
            if (topicId != null && topicId > 0) {
                predicate = cb.and(predicate, cb.equal(root.get("topic").get("id"), topicId));
            }
            return predicate;
        };
    }

    public static Specification<Sentence> searchByChinese(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("chineseSentence")), "%" + keyword.toLowerCase() + "%");
        };
    }

    public static Specification<Sentence> searchByVietnamese(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("vietnameseSentence")), "%" + keyword.toLowerCase() + "%");
        };
    }
}
