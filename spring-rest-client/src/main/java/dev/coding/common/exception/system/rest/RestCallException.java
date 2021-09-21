package dev.coding.common.exception.system.rest;

import dev.coding.common.exception.SystemException;

public class RestCallException extends SystemException {
    public RestCallException (final String message) { super(message); }
    public RestCallException (final Throwable cause) { super(cause); }
    public RestCallException (final String message, final Throwable ex) { super(message, ex); }
}
