package com.alexbalmus.euw.common;

/**
 * Interface for "multirole" wrappers
 * @param <E>
 */
public interface Multirole<E> extends Role<E>
{
    /**
     * Generic method that allows choosing a specific role to play
     * @return a reference to the wrapper with the selected role
     * @param <R> the type of the role to play
     */
    @SuppressWarnings("unchecked")
    default <R extends Role<E>> R assignRole()
    {
        try
        {
            return (R) this;
        }
        catch (ClassCastException e)
        {
            throw new IllegalStateException("Attempting to play an invalid role.");
        }
    }
}
