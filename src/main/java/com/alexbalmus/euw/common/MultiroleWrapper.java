package com.alexbalmus.euw.common;


public interface MultiroleWrapper<E> extends RoleWrapper<E>
{
    @SuppressWarnings("unchecked")
    default <R extends RoleWrapper<E>> R assignRole()
    {
        return (R) this;
    }
}
