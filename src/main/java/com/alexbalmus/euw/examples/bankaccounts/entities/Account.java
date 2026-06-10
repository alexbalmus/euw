package com.alexbalmus.euw.examples.bankaccounts.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.alexbalmus.euw.examples.bankaccounts.exceptions.BalanceException;


@Entity
@Table(name="account")
public class Account
{
    public static final String INSUFFICIENT_FUNDS = "Insufficient funds.";
    public static final String INVALID_AMOUNT = "Amount must be a positive finite value.";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "balance")
    private Double balance;

    public Account(final Double balance)
    {
        this.balance = balance;
    }

    // Required by tests
    public Account(final Long id, final Double balance)
    {
        this.id = id;
        this.balance = balance;
    }

    // Required by ORM
    protected Account()
    {
    }

    public void increaseBalanceBy(final Double amount)
    {
        validateAmount(amount);
        balance += amount;
    }

    public void decreaseBalanceBy(final Double amount)
    {
        validateAmount(amount);
        if (balance < amount)
        {
            throw new BalanceException(INSUFFICIENT_FUNDS);
        }
        balance -= amount;
    }

    public Long getId()
    {
        return id;
    }

    public Double getBalance()
    {
        return balance;
    }

    private void validateAmount(final Double amount)
    {
        if (amount == null || amount <= 0 || amount.isNaN() || amount.isInfinite())
        {
            throw new IllegalArgumentException(INVALID_AMOUNT);
        }
    }


    // equals and hashCode for JPA based on tutorials from Vlad Mihalcea and Thorben Janssen:

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        Account other = (Account) o;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode()
    {
        return getClass().hashCode();
    }
}

