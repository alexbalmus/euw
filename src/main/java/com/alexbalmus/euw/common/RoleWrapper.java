package com.alexbalmus.euw.common;

/**
 * Basic entity role wrapper
 * @param <E> the type of the entity
 */
public interface RoleWrapper<E>
{
    E unwrap();
}
