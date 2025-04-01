package com.cv.s10coreservice.dto;

import java.io.Serial;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class APIResponseDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 695962514098607683L;
    private boolean status;
    private String message;
    private Integer type;
    private Object object;

}
