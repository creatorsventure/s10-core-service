package com.cv.s10coreservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaginationDto implements Serializable {

    @Serial
    private static final long serialVersionUID = -6014145155933828355L;

    private int pageIndex;
    private int pageSize;
    private String sortField;
    private String sortOrder;
    private Long total;
    private String searchField;
    private String searchValue;
    private List<Object> result;

}
