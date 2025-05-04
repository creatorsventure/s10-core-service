package com.cv.s10coreservice.annotation;

import com.cv.s10coreservice.annotation.validator.EmailDomainValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = EmailDomainValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEmailDomain {
    String message() default "Email domain is not valid.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
