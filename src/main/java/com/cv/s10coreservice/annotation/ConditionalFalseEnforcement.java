package com.cv.s10coreservice.annotation;

import com.cv.s10coreservice.annotation.validator.ConditionalFalseEnforcementValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ConditionalFalseEnforcementValidator.class)
public @interface ConditionalFalseEnforcement {

    String message() default "app.message.conditional.field.not.false";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String trigger();  // The field to check (e.g., "enforce")

    String[] dependentFields(); // Fields that must be false if trigger is false
}
