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
    private String name;
    private String password;
    private String email;
    private String organizationId;
    private String unitCode;
    private String unitId;
    private String merchantCode;
    private String merchantId;
    private String roleId;
    private List<String> permissions;
    private String token;
    private String refreshToken;
}
