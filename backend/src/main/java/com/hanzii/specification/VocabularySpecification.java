package com.hanzii.specification;

import com.hanzii.entity.Topic;
import com.hanzii.entity.UserVocabulary;
import com.hanzii.entity.Vocabulary;
import com.hanzii.entity.enums.LearningStatus;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

public final class VocabularySpecification {

    private VocabularySpecification() {}

    public static Specification<Vocabulary> withFilters(Integer hskLevel, Long topicId, String keyword) {
        return (root, query, cb) -> {
            Predicate predicate = cb.or(cb.isFalse(root.get("deleted")), cb.isNull(root.get("deleted")));
            if (hskLevel != null) {
                predicate = cb.and(predicate, cb.equal(root.get("hskLevel"), hskLevel));
            }
            if (topicId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("topic").get("id"), topicId));
            }
            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.toLowerCase() + "%";
                predicate = cb.and(predicate, cb.or(
                        cb.like(cb.lower(root.get("chineseWord")), pattern),
                        cb.like(cb.lower(root.get("pinyin")), pattern),
                        cb.like(cb.lower(root.get("meaning")), pattern)
                ));
            }
            return predicate;
        };
    }

    public static Specification<Vocabulary> withStatusFilter(Long userId, LearningStatus status) {
        return (root, query, cb) -> {
            if (status == null) return cb.conjunction();

            Subquery<Long> subquery = query.subquery(Long.class);
            Root<UserVocabulary> uvRoot = subquery.from(UserVocabulary.class);
            subquery.select(uvRoot.get("vocabulary").get("id"))
                    .where(cb.and(
                            cb.equal(uvRoot.get("user").get("id"), userId),
                            cb.equal(uvRoot.get("status"), status)
                    ));

            if (status == LearningStatus.NEW) {
                return cb.or(
                        cb.in(root.get("id")).value(subquery),
                        cb.not(cb.exists(buildExistsSubquery(root, query, cb, userId)))
                );
            }
            return cb.in(root.get("id")).value(subquery);
        };
    }

    private static Subquery<Long> buildExistsSubquery(Root<Vocabulary> root, CriteriaQuery<?> query,
                                                       CriteriaBuilder cb, Long userId) {
        Subquery<Long> exists = query.subquery(Long.class);
        Root<UserVocabulary> uv = exists.from(UserVocabulary.class);
        exists.select(uv.get("id"))
                .where(cb.and(
                        cb.equal(uv.get("vocabulary").get("id"), root.get("id")),
                        cb.equal(uv.get("user").get("id"), userId)
                ));
        return exists;
    }
}
