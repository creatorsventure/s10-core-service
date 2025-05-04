package com.cv.s10coreservice.dto;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
public class ContextParamDto implements Serializable {


    @Serial
    private static final long serialVersionUID = 8595520619900063198L;

    private String organizationId;
    private String unitId;
    private String merchantId;
}
