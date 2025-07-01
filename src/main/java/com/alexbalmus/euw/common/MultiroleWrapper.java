package com.alexbalmus.euw.common;

/**
 * Interface for "multirole" wrappers
 * @param <E>
 */
public interface MultiroleWrapper<E> extends RoleWrapper<E>
{
    /**
     * Generic method that allows choosing a specific role to play
     * @return a reference to the wrapper with the selected role
     * @param <R> the type of the role to play
     */
    @SuppressWarnings("unchecked")
    default <R extends RoleWrapper<E>> R assignRole()
    {
        return (R) this;
    }
}
