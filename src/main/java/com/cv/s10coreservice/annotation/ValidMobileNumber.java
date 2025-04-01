package com.cv.s10coreservice.annotation;

import com.cv.s10coreservice.annotation.validator.MobileNumberValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MobileNumberValidator.class)
@Documented
public @interface ValidMobileNumber {
    String message() default "Invalid mobile number";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String countryCodeField();

    String mobileNumberField();
}

