package com.alexbalmus.euw.examples.bankaccounts.repositories;

import com.alexbalmus.euw.examples.bankaccounts.entities.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountsRepository extends JpaRepository<Account, Long>
{
}
