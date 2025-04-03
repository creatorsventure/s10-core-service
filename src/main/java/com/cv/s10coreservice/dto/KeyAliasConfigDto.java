package com.cv.s10coreservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KeyAliasConfigDto {
    private String name;
    private String keyPassword;
}
