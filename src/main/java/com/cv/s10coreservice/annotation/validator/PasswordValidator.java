package com.cv.s10coreservice.annotation.validator;

import com.cv.s10coreservice.annotation.ValidPassword;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

public class PasswordValidator implements ConstraintValidator<ValidPassword, Object> {

    private String passwordField;
    private String confirmPasswordField;

    private static final Pattern UPPER = Pattern.compile(".*[A-Z].*");
    private static final Pattern LOWER = Pattern.compile(".*[a-z].*");
    private static final Pattern DIGIT = Pattern.compile(".*\\d.*");
    private static final Pattern SPECIAL = Pattern.compile(".*[!@#$%^&*()_+\\-\\=\\[\\]{};':\"\\\\|,.<>/?].*");
    private static final Pattern WHITESPACE = Pattern.compile(".*\\s.*");

    @Override
    public void initialize(ValidPassword constraintAnnotation) {
        this.passwordField = constraintAnnotation.passwordField();
        this.confirmPasswordField = constraintAnnotation.confirmPasswordField();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        try {
            String password = getFieldValue(value, passwordField);
            String confirmPassword = getFieldValue(value, confirmPasswordField);

            if (password == null || confirmPassword == null) {
                return false;
            }

            boolean isValid = true;
            context.disableDefaultConstraintViolation();

            if (!password.equals(confirmPassword)) {
                context.buildConstraintViolationWithTemplate("{password.mismatch}")
                        .addPropertyNode(confirmPasswordField)
                        .addConstraintViolation();
                isValid = false;
            }

            if (password.length() < 8 || password.length() > 16) {
                context.buildConstraintViolationWithTemplate("{password.length}")
                        .addPropertyNode(passwordField)
                        .addConstraintViolation();
                isValid = false;
            }

            if (!UPPER.matcher(password).matches()) {
                context.buildConstraintViolationWithTemplate("{password.uppercase}")
                        .addPropertyNode(passwordField)
                        .addConstraintViolation();
                isValid = false;
            }

            if (!LOWER.matcher(password).matches()) {
                context.buildConstraintViolationWithTemplate("{password.lowercase}")
                        .addPropertyNode(passwordField)
                        .addConstraintViolation();
                isValid = false;
            }

            if (!DIGIT.matcher(password).matches()) {
                context.buildConstraintViolationWithTemplate("{password.digit}")
                        .addPropertyNode(passwordField)
                        .addConstraintViolation();
                isValid = false;
            }

            if (!SPECIAL.matcher(password).matches()) {
                context.buildConstraintViolationWithTemplate("{password.special}")
                        .addPropertyNode(passwordField)
                        .addConstraintViolation();
                isValid = false;
            }

            if (WHITESPACE.matcher(password).matches()) {
                context.buildConstraintViolationWithTemplate("{password.whitespace}")
                        .addPropertyNode(passwordField)
                        .addConstraintViolation();
                isValid = false;
            }

            return isValid;

        } catch (Exception e) {
            return false;
        }
    }

    private String getFieldValue(Object object, String fieldName) throws Exception {
        Method method = object.getClass().getDeclaredMethod("get" + capitalize(fieldName));
        return (String) method.invoke(object);
    }

    private String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
