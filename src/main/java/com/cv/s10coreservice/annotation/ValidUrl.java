package com.cv.s10coreservice.annotation;

import com.cv.s10coreservice.annotation.validator.UrlValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UrlValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidUrl {
    String message() default "app.message.failure.invalid.url";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
