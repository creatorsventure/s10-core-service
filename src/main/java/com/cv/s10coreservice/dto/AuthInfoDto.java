package com.cv.s10coreservice.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
public class AuthInfoDto {
    private String username;
    private String password;
    private String email;
    private List<String> organizationIds;
}
