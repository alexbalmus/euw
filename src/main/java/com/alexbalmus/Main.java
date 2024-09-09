package com.alexbalmus;

import com.alexbalmus.euw.examples.bankaccounts.BankAccountsExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class Main implements CommandLineRunner
{
    @Autowired
    BankAccountsExample bankAccountsExample;

    public static void main(String[] args)
    {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args)
    {
        runBankAccountsExample();
    }

    private void runBankAccountsExample() {
        System.out.println("\n\n----- Bank Accounts Example -----");

        System.out.println("\nExecuting A to B money transfer scenario: \n");
        bankAccountsExample.executeAToBMoneyTransferScenario();

        System.out.println("\nExecuting A to B to C money transfer scenario: \n");
        bankAccountsExample.executeAToBToCMoneyTransferScenario();
    }

}
