package com.cv.s10coreservice.controller.generic;

import com.cv.s10coreservice.dto.PaginationDto;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

public interface GenericController<T> {

    ResponseEntity<Object> create(T dto, BindingResult result);

    ResponseEntity<Object> update(T dto, BindingResult result);

    ResponseEntity<Object> updateStatus(String id, boolean status);

    ResponseEntity<Object> readOne(String id);

    ResponseEntity<Object> readPage(PaginationDto dto);

    ResponseEntity<Object> readIdNameMapping();

    ResponseEntity<Object> delete(String id);

}
