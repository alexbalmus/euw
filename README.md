# Entity - UseCase - Wrapper
A DCI-inspired approach for Java

If you are new to Data-Context-Interaction, then it's recommended you read the following article first:
https://fulloo.info/Documents/ArtimaDCI.html

Please note that given Java's dynamic limitations and the considerations mentioned below, 
this implementation is a "wrapper approach" and therefore is not true DCI:
https://fulloo.info/doku.php?id=why_isn_t_it_dci_if_you_use_a_wrapper_object_to_represent_the_role

For a more DCI-savvy approach, check out: 

https://github.com/alexbalmus/dci_java_playground

DCI is a valuable (but not very well known) use case oriented design & architecture approach 
and OOP paradigm shift. Due to its particular characteristics, it's rather difficult to implement in a strongly typed 
programming language like Java. Two reference examples have been provided by DCI's authors, one using a library called 
Qi4J and the other using Java's reflection API: https://fulloo.info/Examples/JavaExamples/ 

In my case, I'm going for some tradeoffs: this is not true DCI, but still aiming to take as much as possible from 
the valuable features DCI brings.

Prior considerations:
- Pure Java for roles/role-injection - no third party libraries / frameworks, as they might not be accepted in certain projects
- No reflection - this also might not be accepted in some projects, and Java's reflection API is a pain to work with
- Able to integrate in a mature/legacy code base, i.e., not requiring any changes to existing entities.

Approach taken for roles in Java: interfaces with default methods 
(one of the suggestions from the Wikipedia article listed in the "More info" section below).

Measures taken to alleviate the issues related to using a wrapper (which are usually the source of criticism):
- it's just a wrapper, not a Decorator, i.e. there's no common interface for the entity and the wrapper to implement
in order to avoid accidental (unwanted) use of Polymorphism
- the intent is explicit: there's an ".unwrap()" method that returns a reference to the wrapped object when needed
- each entity will be wrapped only once by a single "multirole wrapper" that will expose one of its capabilities (roles) 
when required; this way the wrapper will also act as on object representative 
(it would be stretching it too far to say it's a "surrogate" identity but there will always be this 
entity-wrapper correspondence for the lifetime of the use case execution, regardless of the different roles that 
might be enabled for that wrapper in different installments of the same use case execution)

Mental model: I like to think of the wrapper as some sort of vehicle, or another type of machine that encloses its subject; 
for example, on a construction site the workers will perform their roles with the help of specialized vehicles that *wrap* them;
So, while true DCI allows to actually become a cyborg, this approach is more modest in its goals, but could still be useful enough.

Implementation:

We start out with a generic functional/SAM (Single Abstract Method) interface called RoleWrapper having a method unwrap() 
which would return a reference to the target (role playing) object (entity):

com.alexbalmus.euw.common.RoleWrapper:

    public interface RoleWrapper<E>
    {
        E unwrap();
    }

This interface will be extended by various "role" interfaces with default methods that contribute behavior.

Then we have another interface that extends the first; this will be implemented by the multirole wrapper objects and
has a convenient assignRole() method that performs the type casting to the specific role (a RoleWrapper subtype):

com.alexbalmus.euw.common.MultiroleWrapper:

    public interface MultiroleWrapper<E> extends RoleWrapper<E>
    {
        @SuppressWarnings("unchecked")
        default <R extends RoleWrapper<E>> R assignRole()
        {
            return (R) this;
        }
    }

Actual roles might look something like this (notice how the .unwrap() method is used to get access to the underlying object):

com.alexbalmus.euw.examples.bankaccounts.usecases.moneytransfer.Account_Source:

    interface Account_Source<A extends Account> extends RoleWrapper<A>
    {
        String INSUFFICIENT_FUNDS = "Insufficient funds.";
    
        default void transfer(final Double amount, final Account_Destination<? super A> destination)
        {
            if (unwrap().getBalance() < amount)
            {
                throw new BalanceException(INSUFFICIENT_FUNDS); // Rollback.
            }
            unwrap().decreaseBalanceBy(amount);
            destination.receive(amount);
        }
    }

com.alexbalmus.euw.examples.bankaccounts.usecases.moneytransfer.Account_Destination:

    interface Account_Destination<A extends Account> extends RoleWrapper<A>
    {
        default void receive(final Double amount)
        {
            unwrap().increaseBalanceBy(amount);
        }
    }

The following is a particular multirole wrapper interface for objects that will wrap an entity of type Account (or subtype); 
as can be seen, it extends MultiroleWrapper (to access the assignRole() method) and also all the role interfaces that
correspond to all the possible roles that an Account object might play:

com.alexbalmus.euw.examples.bankaccounts.usecases.moneytransfer.Account_Multirole:

    public interface Account_Multirole<A extends Account>
        extends MultiroleWrapper<A>, Account_Source<A>, Account_Destination<A>
    {
    }

Now, for the actual wrapping performed inside a context, this will be done by means of an object whose type is an 
anonymous inner class that implements a particular multirole interface; 
the implementation of the unwrap() method will return the wrapped target object. Since Java 8 we can use a lambda expression:

com.alexbalmus.euw.examples.bankaccounts.usecases.moneytransfer.MoneyTransferContext.wrapWithPotentialRoles:

    // Potential roles wrapping:
    Account_Multirole<A> wrapWithPotentialRoles(final A account)
    {
        return () -> account;
    }


The context object gathers the objects participating in the use case, assigns the necessary roles to them 
and then kicks-off the execution:

com.alexbalmus.euw.examples.bankaccounts.usecases.moneytransfer.MoneyTransferContext:

    private final MultiroleWrapper<A> sourceWrapper;
    private final MultiroleWrapper<A> destinationWrapper;
    ...

com.alexbalmus.euw.examples.bankaccounts.usecases.moneytransfer.MoneyTransferContext.MoneyTransferContext(java.lang.Double, A, A, A):

    // Potential roles wrapping:
    this.sourceWrapper = wrapWithPotentialRoles(sourceAccount);
    Validate.isTrue(sourceAccount == sourceWrapper.unwrap());

    this.destinationWrapper = wrapWithPotentialRoles(destinationAccount);
    Validate.isTrue(destinationAccount == destinationWrapper.unwrap());
    
    ...

com.alexbalmus.euw.examples.bankaccounts.usecases.moneytransfer.MoneyTransferContext.transferMoney:

    private void transferMoney(
        final MultiroleWrapper<A> sourceWrapper,
        final MultiroleWrapper<A> destinationWrapper,
        // The purpose of the following parameter is just to prove that
        // we can check that the same object wrapper has played different roles in different installments:
        final MultiroleWrapper<A> previousDestinationWrapper,
        final Double amount)
    {
        Validate.isTrue(sourceWrapper != destinationWrapper,
            "Source and destination can't be the same.");

        if (previousDestinationWrapper != null)
        {
            Validate.isTrue(sourceWrapper == previousDestinationWrapper,
                "Source must match previous destination in this step of A-B-C transfer scenario.");
        }

        // Use case roles setup:
        final Account_Source<A> SOURCE = sourceWrapper.assignRole();
        final Account_Destination<A> DESTINATION = destinationWrapper.assignRole();

        // Interaction:
        SOURCE.transfer(amount, DESTINATION);
    }

Notice how a particular role is selected using the ".assignRole()" method. Please note that we can choose either styles:

    Account_Source<A> SOURCE = sourceWrapper.assignRole();

    // or:

    var SOURCE = sourceWrapper.<Account_Source<A>>assignRole();

The important aspect is that after the role assignment, SOURCE == sourceWrapper will hold true. 
Furthermore, if we were to then select a different role for the same wrapper, the reference equality would still hold:

    SOURCE == sourceWrapper.<Account_Destination<A>>assignRole()

Also see com.alexbalmus.euw.examples.bankaccounts.usecases.moneytransfer.MoneyTransferContextTest.testIdentity

Finally, the interaction takes place: while a basic Account object only has methods related to its own properties, 
the wrapper brings interaction to the table (in this case transferring an amount to another account) and works together
with the underlying entity to create the synergy that mimics the idea of an object gaining additional capabilities:

    // Interaction:
    SOURCE.transfer(amount, DESTINATION);

More info:

https://fulloo.info/ 

https://fulloo.info/Documents/ArtimaDCI.html

https://en.wikipedia.org/wiki/Data,_context_and_interaction

https://gist.github.com/kt3k/8312661
