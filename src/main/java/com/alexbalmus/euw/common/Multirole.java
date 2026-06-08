package com.alexbalmus.euw.common;

/**
 * Interface for "multirole" wrappers
 * @param <E>
 */
public interface Multirole<E> extends Role<E>
{
    /**
     * Generic method that allows choosing a specific role to play
     * @param roleType the type of the role to play
     * @return a reference to the wrapper with the selected role
     * @param <R> the type of the role to play
     */
    default <R extends Role<E>> R assignRole(final Class<R> roleType)
    {
        try
        {
            return roleType.cast(this);
        }
        catch (ClassCastException e)
        {
            throw new IllegalStateException("Attempting to play an invalid role: " + roleType.getName(), e);
        }
    }
}
