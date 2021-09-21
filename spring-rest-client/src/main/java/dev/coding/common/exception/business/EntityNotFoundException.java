package dev.coding.common.exception.business;

import dev.coding.common.exception.BusinessException;

public class EntityNotFoundException extends BusinessException {
    public EntityNotFoundException (final String message) { super(message); }
    public EntityNotFoundException (final Throwable cause) { super(cause); }
    public EntityNotFoundException (final String message, final Throwable ex) { super(message, ex); }
}
