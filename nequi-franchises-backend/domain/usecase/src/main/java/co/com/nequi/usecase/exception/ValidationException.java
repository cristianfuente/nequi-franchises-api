package co.com.nequi.usecase.exception;

import co.com.nequi.usecase.constant.ExceptionMessage;

public class ValidationException extends BusinessException {

    public ValidationException(ExceptionMessage e) {
        super(e);
    }

}
