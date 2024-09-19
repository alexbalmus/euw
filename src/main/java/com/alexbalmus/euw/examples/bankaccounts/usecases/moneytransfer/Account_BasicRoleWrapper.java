package com.alexbalmus.euw.examples.bankaccounts.usecases.moneytransfer;

import com.alexbalmus.euw.common.RoleWrapper;
import com.alexbalmus.euw.examples.bankaccounts.entities.Account;

/**
 * Basic marker interface for Account (or subclass) object wrapper
 */
interface Account_BasicRoleWrapper<A extends Account> extends RoleWrapper<A>
{
}
