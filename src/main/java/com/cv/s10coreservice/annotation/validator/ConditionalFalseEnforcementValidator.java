package com.cv.s10coreservice.annotation.validator;

import com.cv.s10coreservice.annotation.ConditionalFalseEnforcement;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Arrays;

public class ConditionalFalseEnforcementValidator implements ConstraintValidator<ConditionalFalseEnforcement, Object> {

    private String triggerField;
    private String[] dependentFields;

    @Override
    public void initialize(ConditionalFalseEnforcement constraintAnnotation) {
        this.triggerField = constraintAnnotation.trigger();
        this.dependentFields = constraintAnnotation.dependentFields();
    }

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context) {
        try {
            PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(obj.getClass()).getPropertyDescriptors();

            Method triggerGetter = Arrays.stream(propertyDescriptors)
                    .filter(pd -> pd.getName().equals(triggerField))
                    .findFirst()
                    .map(PropertyDescriptor::getReadMethod)
                    .orElseThrow(() -> new RuntimeException("Invalid trigger field: " + triggerField));

            Boolean triggerValue = (Boolean) triggerGetter.invoke(obj);

            if (triggerValue == null || triggerValue) {
                return true; // No constraint when true or null
            }

            for (String field : dependentFields) {
                Method fieldGetter = Arrays.stream(propertyDescriptors)
                        .filter(pd -> pd.getName().equals(field))
                        .findFirst()
                        .map(PropertyDescriptor::getReadMethod)
                        .orElseThrow(() -> new RuntimeException("Invalid dependent field: " + field));

                Boolean fieldValue = (Boolean) fieldGetter.invoke(obj);

                if (Boolean.TRUE.equals(fieldValue)) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate(
                            String.format("Field '%s' must be false when '%s' is false", field, triggerField)
                    ).addPropertyNode(field).addConstraintViolation();
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            throw new RuntimeException("Validation error: " + e.getMessage(), e);
        }
    }
}
