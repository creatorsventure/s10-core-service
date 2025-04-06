package com.cv.s10coreservice.dto;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
public class AuthInfoDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 8553698601517438499L;

    private String userId;
    private String password;
    private String email;
    private List<String> organizationIds;
    private List<String> roleIds;
    private String token;
    private String refreshToken;
}
