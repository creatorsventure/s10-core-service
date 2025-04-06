package com.cv.s10coreservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class APIResponseDto implements Serializable {

    @Serial
    private static final long serialVersionUID = -7359921237549127316L;

    private boolean status;
    private String message;
    private Integer type;
    private Object object;

}
