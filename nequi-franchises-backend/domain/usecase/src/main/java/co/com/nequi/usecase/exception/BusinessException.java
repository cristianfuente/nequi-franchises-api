package co.com.nequi.usecase.exception;

import co.com.nequi.usecase.constant.ExceptionMessage;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final String code;

    public BusinessException(ExceptionMessage exceptionMessage) {
        super(exceptionMessage.getMessage());
        this.code = exceptionMessage.name();
    }

}
