package com.cv.s10coreservice.exception;

import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ExceptionComponent {

    private MessageSource messageSource;

    public ApplicationException expose(String msgOrKey, Boolean isProperty, Object... args) {
        if (isProperty) {
            msgOrKey = messageSource.getMessage(msgOrKey, args, LocaleContextHolder.getLocale());
        }
        return new ApplicationException(msgOrKey);
    }
}
