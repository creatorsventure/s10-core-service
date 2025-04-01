package com.cv.s10coreservice.service.function;

import com.cv.s10coreservice.constant.ApplicationConstant;
import com.cv.s10coreservice.dto.PaginationDto;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import java.util.function.Function;

public class StaticFunction {

    public static Function<PaginationDto, PageRequest> generatePageRequest = (dto -> {
        if (StringUtils.hasText(dto.getSortField())) {
            return PageRequest.of((dto.getPageIndex() - 1), dto.getPageSize(),
                    Sort.by(StringUtils.hasText(dto.getSortOrder())
                                    && ApplicationConstant.APPLICATION_SORT_ORDER_DESC.equals(dto.getSortOrder())
                                    ? Sort.Direction.DESC
                                    : Sort.Direction.ASC,
                            dto.getSortField()));
        } else {
            return PageRequest.of((dto.getPageIndex() - 1), dto.getPageSize());
        }
    });


}
