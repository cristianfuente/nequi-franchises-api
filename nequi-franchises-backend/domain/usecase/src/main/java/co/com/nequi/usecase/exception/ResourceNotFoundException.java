package co.com.nequi.usecase.exception;

import co.com.nequi.usecase.constant.ExceptionMessage;

public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(ExceptionMessage exceptionMessage) {
        super(exceptionMessage);
    }
}
