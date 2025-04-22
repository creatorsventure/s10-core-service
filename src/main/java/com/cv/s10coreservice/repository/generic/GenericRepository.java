package com.cv.s10coreservice.repository.generic;

import java.util.List;
import java.util.Optional;

public interface GenericRepository {

    <T> Optional<T> findByIdAndStatusTrue(String id, Class<T> modelType);

    <T> Optional<T> findByIdAndStatusFalse(String id, Class<T> modelType);

    <T> Optional<List<T>> findAllByStatusTrueAndIdIn(List<String> ids, Class<T> modelType);

    <T> Optional<List<T>> findAllByStatusTrue(Class<T> modelType);
}
