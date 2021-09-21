package dev.coding.common.exception;

public class SystemException extends BaseException {
    public SystemException (final String message) { super(message); }
    public SystemException (final Throwable cause) { super(cause); }
    public SystemException (final String message, final Throwable ex) { super(message, ex); }
}
