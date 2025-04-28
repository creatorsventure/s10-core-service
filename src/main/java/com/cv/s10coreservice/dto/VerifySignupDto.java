package com.cv.s10coreservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@ToString
public class VerifySignupDto implements Serializable {

    @Serial
    private static final long serialVersionUID = -2214339994540765680L;

    private String adminUserId;
    private String adminMobileNumber;
    private String adminCountryCode;
    private String adminEmail;
    private String adminName;
    private String entityName;
    private String entityCode;
    private String entityId;
    private boolean otpRequired;
    private String otp;
    private LocalDateTime createdAt;
}
