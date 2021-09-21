package dev.coding.common.exception;

public abstract class BaseException extends RuntimeException {
    public BaseException(final String message) { super(message); }
    public BaseException(final Throwable cause) { super(cause); }
    public BaseException(final String message, final Throwable ex) { super(message, ex); }
}
