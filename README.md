# Entity - UseCase - Wrapper
A role-based, "contextual" OOP approach for Java inspired by ideas from DCI (Data-Context-Interaction) and Eclipse Object Teams.

If you are new to DCI, then it's recommended you read the following article first:
https://fulloo.info/Documents/ArtimaDCI.html

Running the example:
- tested with JDK 25 & Maven 3.9.15
- building: mvn package
- running: mvn spring-boot:run

EUW (Entity - UseCase - Wrapper) was born out of frustration with the fact that true DCI can't be implemented in plain Java.

Some key ideas behind DCI:
- two orthogonal perspectives: "what the system is" (Data) and "what the system does" (Interaction in a Context)
- explicitly capture object interaction as a network of roles played by objects in a context
- objects should initially be simple / "dumb" / data-objects: they may contain methods related to themselves (for
  validations, invariants protection etc.) but not for interaction with other objects
- objects will receive additional behavior from the roles they play in a certain context
- identity of the role-playing objects must be retained (i.e. a role *may not* be implemented as a wrapper)
  https://fulloo.info/doku.php?id=why_isn_t_it_dci_if_you_use_a_wrapper_object_to_represent_the_role

The last point is especially problematic for Java because the language does not have any feature that would 
allow to add additional behavior to existing objects, or to mimic this while retaining object identity:
https://fulloo.info/doku.php?id=can_i_use_dci_in_java

One possible solution is the use of extension methods using a third party library like Lombok, which was explored here:
https://github.com/alexbalmus/dci_java_playground

In this project though, we take a "rebellious" approach: instead of avoiding the wrapper, we aim to embrace it in an open, explicit and safe way.

Measures taken to alleviate the issues related to using a wrapper (which are usually the source of criticism):
- it's just a wrapper, not a Decorator, i.e. there's no common interface for the entity and the wrapper to implement
in order to avoid accidental (unwanted) use of Polymorphism
- the intent is explicit: there's an ".unwrap()" method that returns a reference to the wrapped object when needed
- each entity will be wrapped only once by a single "multirole wrapper" that will expose one of its capabilities (roles) 
when required; this way the wrapper will also act as an object representative, i.e. there will always be this
entity-wrapper correspondence for the lifetime of the use case execution, regardless of the different roles that 
might be enabled for that wrapper in different installments of the same use case execution

Mental model: I like to think of the wrapper as some sort of vehicle, or another type of machine that encloses its subject; 
for example, on a construction site the workers will perform their roles with the help of specialized vehicles that *wrap* them;
So, while true DCI allows to actually become a cyborg, this approach is more modest in its goals, but could still be useful enough.

Implementation:

We start out with a generic functional/SAM (Single Abstract Method) interface called Role having a method unwrap() 
which would return a reference to the target (role playing) object (entity):

[Role](https://github.com/alexbalmus/euw/blob/main/src/main/java/com/alexbalmus/euw/common/Role.java):

```java
public interface Role<E>
{
    E unwrap();
}
```

This interface will be extended by various "role" interfaces with default methods that contribute behavior.

Then we have another interface that extends the first; this will be implemented by the multirole wrapper objects and
has a convenient assignRole() method that performs the type casting to the specific role (a Role subtype):

[Multirole](https://github.com/alexbalmus/euw/blob/main/src/main/java/com/alexbalmus/euw/common/Multirole.java):

```java
public interface Multirole<E> extends Role<E>
{
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
```

In the provided example, [Account](https://github.com/alexbalmus/euw/blob/main/src/main/java/com/alexbalmus/euw/examples/bankaccounts/entities/Account.java)
is a simple JPA entity which will play different roles (with the help of a "multi-role" wrapper).

Disclaimer: this example is solely for the purpose of showcasing EUW in a simple way and therefore it should not be viewed as a reference implementation for an actual banking system.

Actual roles might look something like this (notice how the .unwrap() method is used to get access to the underlying object):

[Account_Source](https://github.com/alexbalmus/euw/blob/main/src/main/java/com/alexbalmus/euw/examples/bankaccounts/usecases/moneytransfer/Account_Source.java):

```java
interface Account_Source extends Role<Account>
{
    default void transfer(final Double amount, final Account_Destination destination)
    {
        if (unwrap() == destination.unwrap())
        {
            throw new IllegalArgumentException("Source and destination can't be the same.");
        }

        unwrap().decreaseBalanceBy(amount);
        destination.receive(amount);
    }
}
```

[Account_Destination](https://github.com/alexbalmus/euw/blob/main/src/main/java/com/alexbalmus/euw/examples/bankaccounts/usecases/moneytransfer/Account_Destination.java):

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
correspond to all the possible roles that might be played.
It also features a static method called wrap(...) for the actual wrapping performed inside a context. The wrapping is done by means of an object whose type is an anonymous inner class that implements the multirole interface;
the implementation of the unwrap() method will return the wrapped target object (since Java 8, we can use a lambda expression to achieve this):

[Account_Multirole](https://github.com/alexbalmus/euw/blob/main/src/main/java/com/alexbalmus/euw/examples/bankaccounts/usecases/moneytransfer/Account_Multirole.java):

```java
interface Account_Multirole
    extends Multirole<Account>, Account_Source, Account_Destination
{
    static Multirole<Account> wrap(final Account account)
    {
        return (Account_Multirole) () -> account;
    }
}
```

Notice the difference between the actual object type (Account_Multirole) and the returned type (Multirole<Account>):
 the idea is to not expose initially all possible behavior, but upon role assignment (done in a certain scenario) just
expose the behavior that corresponds to the chosen role.

The use case object gathers the participating objects, assigns the necessary roles to them 
and then kicks off the execution:

[MoneyTransferUseCase](https://github.com/alexbalmus/euw/blob/main/src/main/java/com/alexbalmus/euw/examples/bankaccounts/usecases/moneytransfer/MoneyTransferUseCase.java):

```java
public class MoneyTransferUseCase
{
    private final Multirole<Account> wSource;
    private final Multirole<Account> wDestination;

    public MoneyTransferUseCase(Account source, Account destination)
    {
        wSource      = wrap(source);
        wDestination = wrap(destination);
    }

    public void transferFromSourceToDestination(final Double amount)
    {
        //--- Use case roles setup:
        var rSource      = wSource.assignRole(Account_Source.class);
        var rDestination = wDestination.assignRole(Account_Destination.class);

        //--- Interaction:
        rSource.transfer(amount, rDestination);
    }
}
```

Notice how a particular role is selected using the ".assignRole()" method:

```java
    var rSource = wSource.assignRole(Account_Source.class);
```

Also see [MoneyTransferUseCaseTest#testIdentity](https://github.com/alexbalmus/euw/blob/main/src/test/java/com/alexbalmus/euw/examples/bankaccounts/usecases/moneytransfer/MoneyTransferUseCaseTest.java#L51)

Finally, the interaction takes place: while a basic Account object only has methods related to its own properties, 
the wrapper brings interaction to the table (in this case transferring an amount to another account) and works together
with the underlying entity to create the synergy that mimics the idea of an object gaining additional capabilities:

```java
    //--- Interaction:
    rSource.transfer(amount, rDestination);
```

Also see [MultiroleMoneyTransferUseCase](https://github.com/alexbalmus/euw/blob/main/src/main/java/com/alexbalmus/euw/examples/bankaccounts/usecases/moneytransfer/MultiroleMoneyTransferUseCase.java) for an example of how the same wrapper can play different roles.

References:

https://fulloo.info/ 

https://fulloo.info/Documents/ArtimaDCI.html

https://en.wikipedia.org/wiki/Data,_context_and_interaction

https://www.eclipse.org/objectteams/documentation.php

https://gist.github.com/kt3k/8312661
