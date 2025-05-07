package com.cv.s10coreservice.dto;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@ToString
@Builder
public class IdNameMapDto implements Serializable {


    @Serial
    private static final long serialVersionUID = -1132307551450319398L;

    private Map<String, Map<String, String>> idNameMaps;

}
