package com.cv.s10coreservice.repository.generic;

import java.util.List;
import java.util.Optional;

public interface GenericRepository {

    <T> Optional<T> findByIdAndStatus(String id, boolean status, Class<T> modelType);

    <T> Optional<List<T>> findAllByStatusAndIdIn(boolean status, List<String> ids, Class<T> modelType);

    <T> Optional<List<T>> findAllByStatus(boolean status, Class<T> modelType);

}
