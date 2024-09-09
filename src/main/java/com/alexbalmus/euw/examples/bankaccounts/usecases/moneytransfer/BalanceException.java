package com.alexbalmus.euw.examples.bankaccounts.usecases.moneytransfer;

class BalanceException extends RuntimeException
{
    public BalanceException(final String message)
    {
        super(message);
    }
}
