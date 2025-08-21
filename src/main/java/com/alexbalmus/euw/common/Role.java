package com.alexbalmus.euw.common;

/**
 * Basic role wrapper interface
 * @param <E> the generic type of the entity to be wrapped
 */
public interface Role<E>
{
    /**
     * @return a reference to the wrapped entity
     */
    E unwrap();
}
