package dev.coding.common.exception.system.rest;

public class RestCallShouldRetryException extends RestCallException {
    public RestCallShouldRetryException (final String message) { super(message); }
    public RestCallShouldRetryException (final Throwable cause) { super(cause); }
    public RestCallShouldRetryException (final String message, final Throwable ex) { super(message, ex); }
}
