package com.cv.s10coreservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@ToString
public class VerifyOTPDto implements Serializable {

    @Serial
    private static final long serialVersionUID = -2214339994540765680L;

    private String userId;
    private String unitId;
    private String merchantId;
    private String otp;
}
