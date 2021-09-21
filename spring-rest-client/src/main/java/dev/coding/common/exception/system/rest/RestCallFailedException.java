package dev.coding.common.exception.system.rest;

public class RestCallFailedException extends RestCallException {
    public RestCallFailedException (final String message) { super(message); }
    public RestCallFailedException (final Throwable cause) { super(cause); }
    public RestCallFailedException (final String message, final Throwable ex) { super(message, ex); }
}
