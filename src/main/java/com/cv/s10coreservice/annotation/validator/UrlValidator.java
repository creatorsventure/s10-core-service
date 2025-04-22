package com.cv.s10coreservice.annotation.validator;

import com.cv.s10coreservice.annotation.ValidUrl;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class UrlValidator implements ConstraintValidator<ValidUrl, String> {

    private static final Pattern URL_PATTERN = Pattern.compile(
            "^(https?:\\/\\/)(localhost|\\d{1,3}(\\.\\d{1,3}){3}|[\\w.-]+\\.[a-z]{2,})(:\\d{1,5})?(\\/[^\\s]*)?$",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    public void initialize(ValidUrl constraintAnnotation) {
        // no initialization required
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) return true; // use @NotBlank if required
        return URL_PATTERN.matcher(value).matches();
    }
}
