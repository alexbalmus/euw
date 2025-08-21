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
- Pure Java for roles and role-assignment - no third party libraries / frameworks, as they might not be accepted in certain projects
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

We start out with a generic functional/SAM (Single Abstract Method) interface called Role having a method unwrap() 
which would return a reference to the target (role playing) object (entity):

com.alexbalmus.euw.common.Role:

```java
    public interface Role<E>
    {
        E unwrap();
    }
```

This interface will be extended by various "role" interfaces with default methods that contribute behavior.

Then we have another interface that extends the first; this will be implemented by the multirole wrapper objects and
has a convenient assignRole() method that performs the type casting to the specific role (a Role subtype):

com.alexbalmus.euw.common.Multirole:

```java
    public interface Multirole<E> extends Role<E>
    {
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
```

Actual roles might look something like this (notice how the .unwrap() method is used to get access to the underlying object):

com.alexbalmus.euw.examples.bankaccounts.usecases.moneytransfer.Account_Source:

```java
    interface Account_Source extends Role<Account>
    {
        String INSUFFICIENT_FUNDS = "Insufficient funds.";
    
        default void transfer(final Double amount, final Account_Destination destination)
        {
            if (unwrap().getBalance() < amount)
            {
                throw new BalanceException(INSUFFICIENT_FUNDS); // Rollback.
            }
            unwrap().decreaseBalanceBy(amount);
            destination.receive(amount);
        }
    }
```

com.alexbalmus.euw.examples.bankaccounts.usecases.moneytransfer.Account_Destination:

```java
    interface Account_Destination extends Role<Account>
    {
        default void receive(final Double amount)
        {
            unwrap().increaseBalanceBy(amount);
        }
    }
```

The following is a particular multirole wrapper interface for objects that will wrap an entity of type Account; 
as can be seen, it extends Multirole (to access the assignRole() method) and also all the role interfaces that
correspond to all the possible roles that might be played:

com.alexbalmus.euw.examples.bankaccounts.usecases.moneytransfer.Account_Multirole:

```java
    interface Account_Multirole
        extends Multirole<Account>, Account_Source, Account_Destination
    {
    }
```

Now, for the actual wrapping performed inside a context, this will be done by means of an object whose type is an 
anonymous inner class that implements a particular multirole interface; 
the implementation of the unwrap() method will return the wrapped target object. Since Java 8 we can use a lambda expression:

com.alexbalmus.euw.examples.bankaccounts.usecases.moneytransfer.MoneyTransferUseCase.wrapWithPotentialRoles:

```java
    static Multirole<Account> wrapWithPotentialRoles(final Account account)
    {
        return (Account_Multirole) () -> account;
    }
```

Notice the difference between the actual object type (Account_Multirole) and the returned type (Multirole<Account>):
 the idea is to not expose initially all possible behavior, but upon role assignment (done in a certain scenario) just
expose the behavior that corresponds to the chosen role.

The use case object gathers the participating objects, assigns the necessary roles to them 
and then kicks off the execution:

com.alexbalmus.euw.examples.bankaccounts.usecases.moneytransfer.MoneyTransferUseCase.createWrappersMap:

```java
    /**
     * Convenience method for creating a map of wrappers instead of calling wrapWithPotentialRoles(...) multiple times
     *
     * @param accountIds the entities to be wrapped
     *
     * @return the map of wrappers
     */
    final Map<Account, Multirole<Account>> createWrappersMap(final Account... accountIds)
    {
        var wrappersMap = new HashMap<Account, Multirole<Account>>();
    
        for (var account : accountIds)
        {
            wrappersMap.put(account, wrapWithPotentialRoles(account));
        }
    
        return wrappersMap;
    }
```

com.alexbalmus.euw.examples.bankaccounts.usecases.moneytransfer.MoneyTransferUseCase.transferFromSourceToDestinationViaTemporary:

```java
    /**
     * Transfer amount from source to destination while traversing a temporary account
     * @param source the source account
     * @param destination the destination account
     * @param temp the temporary account
     * @param amount the amount to transfer
     */
    public void transferFromSourceToDestinationViaTemporary(
        final Account source, final Account destination, final Account temp, final Double amount)
    {
        var wrappersMap = createWrappersMap(source, destination, temp);
    
        transferMoney(
            wrappersMap.get(source),
            wrappersMap.get(temp),
            null, // previous destination
            amount);
    
        transferMoney(
            wrappersMap.get(temp),
            wrappersMap.get(destination),
            wrappersMap.get(temp), // previous destination
            amount);
    }
```

com.alexbalmus.euw.examples.bankaccounts.usecases.moneytransfer.MoneyTransferUseCase.transferMoney:

```java
    /**
     * The parametrized use case method that performs the setup of necessary roles and kicks off the interaction
     *
     * @param wSource the source wrapper
     * @param wDestination the destination wrapper
     * @param wPreviousDestination the previous destination wrapper
     * @param amount the amount to transfer
     */
    private void transferMoney(
        final Multirole<Account> wSource,
        final Multirole<Account> wDestination,
        final Multirole<Account> wPreviousDestination,
        final Double amount)
    {
        Validate.isTrue(wSource != wDestination,
            "Source and destination can't be the same.");
    
        //--- Use case roles setup:
        Account_Source source = wSource.assignRole();
        Account_Destination destination = wDestination.assignRole();
    
        if (wPreviousDestination != null)
        {
            Account_Destination previousDestination = wPreviousDestination.assignRole();
    
            // Identity check: it's the same wrapper even though different roles were played in different installments:
            Validate.isTrue(source == previousDestination,
                "Source must match previous destination in this step of A-B-C transfer scenario.");
            // Likewise, it's the same underlying (wrapped) object:
            Validate.isTrue(source.unwrap() == previousDestination.unwrap());
        }
    
        //--- Interaction:
        source.transfer(amount, destination);
    }
```

Notice how a particular role is selected using the ".assignRole()" method. Please note that we can choose either style:

```java
    Account_Source source = wSource.assignRole();

    // or:

    var source = wSource.<Account_Source>assignRole();
```

The important aspect is that after the role assignment, source == wSource will hold true. 
Furthermore, if we were to then select a different role for the same wrapper, the reference equality would still hold:

```java
    source == wSource.<Account_Destination>assignRole()
```

Also see com.alexbalmus.euw.examples.bankaccounts.usecases.moneytransfer.MoneyTransferUseCaseTest.testIdentity

Finally, the interaction takes place: while a basic Account object only has methods related to its own properties, 
the wrapper brings interaction to the table (in this case transferring an amount to another account) and works together
with the underlying entity to create the synergy that mimics the idea of an object gaining additional capabilities:

```java
    //--- Interaction:
    source.transfer(amount, destination);
```


More info:

https://fulloo.info/ 

https://fulloo.info/Documents/ArtimaDCI.html

https://en.wikipedia.org/wiki/Data,_context_and_interaction

https://gist.github.com/kt3k/8312661
