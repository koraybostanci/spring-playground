package dev.coding.common.exception;

public class BusinessException extends BaseException {
    public BusinessException (final String message) { super(message);}
    public BusinessException (final Throwable cause) { super(cause); }
    public BusinessException (final String message, final Throwable ex) { super(message, ex); }
}
