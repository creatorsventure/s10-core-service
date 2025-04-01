package com.cv.s10coreservice.annotation.validator;

import com.cv.s10coreservice.annotation.ValidMobileNumber;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class MobileNumberValidator implements ConstraintValidator<ValidMobileNumber, Object> {

    private String countryCodeField;
    private String mobileNumberField;

    @Override
    public void initialize(ValidMobileNumber constraintAnnotation) {
        this.countryCodeField = constraintAnnotation.countryCodeField();
        this.mobileNumberField = constraintAnnotation.mobileNumberField();
    }

    @Override
    public boolean isValid(Object object, ConstraintValidatorContext context) {
        try {
            if (object == null) return false;

            String countryCode = (String) object.getClass()
                    .getDeclaredMethod("get" + capitalize(countryCodeField))
                    .invoke(object);

            String mobileNumber = (String) object.getClass()
                    .getDeclaredMethod("get" + capitalize(mobileNumberField))
                    .invoke(object);

            if (mobileNumber == null || countryCode == null) return false;

            int dialCode = Integer.parseInt(countryCode.replace("+", ""));
            String regionCode = PhoneNumberUtil.getInstance().getRegionCodeForCountryCode(dialCode);

            PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
            Phonenumber.PhoneNumber number = phoneUtil.parse(mobileNumber, regionCode);
            return phoneUtil.isValidNumber(number);

        } catch (Exception e) {
            return false;
        }
    }

    private String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
