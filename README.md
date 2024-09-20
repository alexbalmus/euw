# Entity - UseCase - Wrapper
A DCI-inspired approach for Java

If you are new to Data-Context-Interaction, then it's recommended you read the following article first:
https://fulloo.info/Documents/ArtimaDCI.html

Please note that given Java's dynamic limitations and the considerations mentioned below, this implementation is a "wrapper approach" and therefore is not true DCI: https://fulloo.info/doku.php?id=why_isn_t_it_dci_if_you_use_a_wrapper_object_to_represent_the_role

For a more DCI-savvy approach, check out: 

https://github.com/alexbalmus/dci_java_playground/tree/context_with_role_methods

DCI is a valuable (but not very well known) use case oriented design & architecture approach 
and OOP paradigm shift. Due to its particular characteristics, it's rather difficult to implement in a strongly typed 
programming language like Java. Two reference examples have been provided by DCI's authors, one using a library called 
Qi4J and the other using Java's reflection API: https://fulloo.info/Examples/JavaExamples/ 

In my case, I'm going for some tradeoffs: this is not "pure" DCI, but still aiming to be as close as possible to 
the valuable features DCI brings.

Prior considerations:
- Pure Java for roles/role-injection - no third party libraries / frameworks, as they might not be accepted in certain projects
- No reflection - this also might not be accepted in some projects, and Java's reflection API is a pain to work with
- Able to integrate in a mature/legacy code base, i.e., not requiring any changes to existing entities.

Approach taken for roles in Java: interfaces with default methods 
(one of the suggestions from the Wikipedia article listed in the "More info" section below). 

This approach is a variation of https://github.com/alexbalmus/dci_java_playground/tree/wrapper_approach adapted for the situation when we might want to check if the same object (wrapper) has played different roles in different runs (for example in a recursive call).

We start out with a generic functional/SAM (Single Abstract Method) interface called RoleWrapper having a method rolePlayer() 
which would return a reference to the target (role playing) object:

com.alexbalmus.euw.common.RoleWrapper:

    public interface RoleWrapper<E>
    {
        E rolePlayer();
    }


Actual roles might look something like this:

com.alexbalmus.euw.examples.bankaccounts.usecases.moneytransfer.Account_SourceRoleWrapper:

    interface Account_SourceRoleWrapper<A extends Account> extends RoleWrapper<A>
    {
        String INSUFFICIENT_FUNDS = "Insufficient funds.";

        default void transfer(final Double amount, final Account_DestinationRoleWrapper<? super A> destination)
        {
            if (rolePlayer().getBalance() < amount)
            {
                throw new BalanceException(INSUFFICIENT_FUNDS); // Rollback.
            }
            rolePlayer().decreaseBalanceBy(amount);
            destination.receive(amount);
        }
    }

com.alexbalmus.euw.examples.bankaccounts.usecases.moneytransfer.Account_DestinationRoleWrapper:

    interface Account_DestinationRoleWrapper<A extends Account> extends RoleWrapper<A>
    {
        default void receive(final Double amount)
        {
            rolePlayer().increaseBalanceBy(amount);
        }
    }

com.alexbalmus.euw.examples.bankaccounts.usecases.moneytransfer.Account_PotentialRolesWrapper:

    // An all-potential-roles wrapper:
    public interface Account_PotentialRolesWrapper<A extends Account>
        extends Account_SourceRoleWrapper<A>, Account_DestinationRoleWrapper<A>
    {
    }

Now, for the "role injection" (actually the wrapping) performed inside a context, this will be done by means of an 
anonymous inner class that implements a role interface and who's instance will wrap the target object; 
the implementation of the rolePlayer() method will return the wrapped target object. Since Java 8 we can use a lambda expression:

com.alexbalmus.euw.examples.bankaccounts.usecases.moneytransfer.MoneyTransferContext.wrapWithPotentialRoles:

    // Potential roles wrapping:
    Account_PotentialRolesWrapper<A> wrapWithPotentialRoles(final A account)
    {
        return () -> account;
    }


The context object would select the objects participating in the use case, assign the necessary roles to them 
and then kick-off the use case:

com.alexbalmus.euw.examples.bankaccounts.usecases.moneytransfer.MoneyTransferContext:

    private final RoleWrapper<A> sourceWrapper;
    private final RoleWrapper<A> destinationWrapper;
    ...

com.alexbalmus.euw.examples.bankaccounts.usecases.moneytransfer.MoneyTransferContext.MoneyTransferContext(java.lang.Double, A, A, A):

    // Potential roles wrapping:
    this.sourceWrapper = wrapWithPotentialRoles(sourceAccount);
    Validate.isTrue(sourceAccount == sourceWrapper.rolePlayer());

    this.destinationWrapper = wrapWithPotentialRoles(destinationAccount);
    Validate.isTrue(destinationAccount == destinationWrapper.rolePlayer());
    
    ...

com.alexbalmus.euw.examples.bankaccounts.usecases.moneytransfer.MoneyTransferContext.transferMoney:

    private void transferMoney(
        final RoleWrapper<A> sourceWrapper,
        final RoleWrapper<A> destinationWrapper,
        // The purpose of the following parameter is just to prove that
        // we can check that the same object wrapper has played different roles in different installments:
        final RoleWrapper<A> previousDestinationWrapper,
        final Double amount)
    {
        Validate.isTrue(sourceWrapper != destinationWrapper,
            "Source and destination can't be the same.");

        if (previousDestinationWrapper != null)
        {
            Validate.isTrue(sourceWrapper == previousDestinationWrapper,
                "Source must match previous destination in the second step of A-B-C transfer scenario.");
        }

        // Use case roles setup:
        var SOURCE = (Account_SourceRoleWrapper<A>) sourceWrapper;
        var DESTINATION = (Account_DestinationRoleWrapper<A>) destinationWrapper;

        // Interaction:
        SOURCE.transfer(amount, DESTINATION);
    }

The important difference compared to https://github.com/alexbalmus/dci_java_playground/tree/wrapper_approach is the fact that 
we don't wrap with an individual role, but instead we use the Account_PotentialRolesWrapper which combines all possible roles:

    private final RoleWrapper<A> sourceWrapper;
    ...
    this.sourceWrapper = wrapWithPotentialRoles(sourceAccount);

Notice that the type of sourceWrapper is RoleWrapper - the base interface, not Account_PotentialRolesWrapper
, which is actually used by wrapWithPotentialRoles. That means although the object is wrapped with all possible capabilities,
initially no access to any capability is available.
Instead, before the use case interaction takes place, we will cast to the desired role:

    // Use case roles setup:
    var SOURCE = (Account_SourceRoleWrapper<A>) sourceWrapper;
    var DESTINATION = (Account_DestinationRoleWrapper<A>) destinationWrapper;

So we now have access to the role method behavior:

    // Interaction:
    SOURCE.transfer(amount, DESTINATION);

More info:

https://fulloo.info/ 

https://fulloo.info/Documents/ArtimaDCI.html

https://en.wikipedia.org/wiki/Data,_context_and_interaction

https://gist.github.com/kt3k/8312661
