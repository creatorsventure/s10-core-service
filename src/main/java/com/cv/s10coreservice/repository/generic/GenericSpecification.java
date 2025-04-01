package com.cv.s10coreservice.repository.generic;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

public interface GenericSpecification<T> {

    default Specification<T> statusSpec1() {
        return (root, query, builder) -> builder.equal(root.get("status"), true);
    }

    default Specification<T> searchSpec(String searchField, String searchValue) {
        return (root, query, builder) -> {
            if (StringUtils.isNumeric(searchValue)) {
                return builder.equal(root.get(searchField), searchValue);

            } else {
                return builder.like(builder.upper(root.get(searchField)), "%" + searchValue.toUpperCase() + "%");
            }
        };
    }

    default Specification<T> searchWithStatusSpec(String searchField, String searchValue) {
        return (root, query, builder) -> {
            if (StringUtils.isNumeric(searchValue)) {
                return builder.and(builder.equal(root.get(searchField), searchValue),
                        builder.equal(root.get("status"), true));
            } else {
                return builder.and(
                        builder.like(builder.upper(root.get(searchField)), "%" + searchValue.toUpperCase() + "%"),
                        builder.equal(root.get("status"), true));
            }
        };
    }
}
