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
    private String userId;
    private String password;
    private String email;
    private List<String> organizationIds;
    private String token;
    private String refreshToken;
}
