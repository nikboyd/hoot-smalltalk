#### Blocks ####

Blocks are a very powerful part of the [Smalltalk][smalltalk] language.
They are so important, Hoot Smalltalk not only retains the block concepts of standard Smalltalk,
but also extends them in thoughtful ways to improve integration with its host platforms.
Blocks are so flexible, Hoot Smalltalk uses them for a wide variety of language features,
including decision structures, collection iteration, exception handling, multi-threading, and event handling,
in ways that Smalltalk developers will find most familiar.

#### Decision Structures ####

Like standard Smalltalk, there are no reserved words for decision structures in Hoot Smalltalk like there are in
languages such as [Java][java] and [C#][csharp].
Instead, decision structures use message idioms that combine Boolean expressions with Blocks.
The table below lists some of the most commonly used decision idioms as they are expressed in Hoot Smalltalk.

Notice that the decision idioms do not include a **switch** or a **case** statement.
There are ways to mimic such a control structure in Hoot Smalltalk.
However, that approach is generally discouraged in favor of object-oriented designs that make use of
classes and polymorphism to distinguish and handle the separation of cases.

Many of the decision structures identified below can be optimized during translation to a host language (Java or C#).
Often, they can be translated directly into equivalent decision structures.
Similar optimizations are often performed by commercial Smalltalk compilers.
However, under certain circumstances, these control structures and other custom blocks are best
implemented using closures (lambdas).

| **Idiom** | **Examples** |
| --------- | ------------ |
| Decisions     | result := boolValue `or:` [ aBoolean ].   |
|               | result := boolValue `and:` [ aBoolean ].  |
| Alternatives  | result := boolValue `ifTrue:` [ "..." ].  |
|               | result := boolValue `ifTrue:` [ "..." ] `ifFalse:` [ "..." ].  |
|               | result := boolValue `ifFalse:` [ "..." ].  |
|               | result := boolValue `ifFalse:` [ "..." ] `ifTrue:` [ "..." ].  |
| Loops         | [ boolValue ] `whileTrue:` [ "..." ]. |
|               | [ boolValue ] `whileFalse:` [ "..." ]. |
|               | [ "..." boolValue ] `whileTrue`. |
|               | [ "..." boolValue ] `whileFalse`. |
| Intervals     | start `to:` end `do:` [ :index \| "..." ]. |
|               | start `to:` end `by:` delta `do:` [ :index \| "..." ]. |
| Collections   | elements `do:` [ :element \| "..." ]. |
|               | results := elements `collect:` [ :element \| "..." ]. |
|               | results := elements `select:` [ :element \| "..." ]. |
|               | results := elements `reject:` [ :element \| "..." ]. |
|               | results := elements `collect:` [ :element \| "..." ]. |
|               | result := elements `detect:` [ :element \| "..." ]. |
|               | result := elements `detect:` [ :element \| "..." ] `ifNone:` [ "..." ]. |
|               | result := elements `inject:` initialValue `into:` [ :aValue :element \| "..." ]. |
| Evaluations   | result := [ :a :b \| "..." ] `value:` x value: y. |
|               | result := [ :a \| "..." ] `value:` x. |
|               | result := [ "..." ] `value`. |
| Exceptions    | [ "..." ] `ifCurtailed:` [ "... catch any exception ..." ]. |
|               | [ "..." ] `ensure:` [ "... evaluated finally ..." ]. |
|               | [ "..." ] `catch:` [ : ExceptionType! ex \| "..." ] `ensure:` [ "... evaluated finally ..." ]. |
| Threads       | [ "..." ] `fork`. |
|               | [ "..." ] `forkAt:` aPriority. |
| Synchronizing | subject `lockDuring:` [ "..." ]. |
|               | subject `notifyOneWaitingThread`. |
|               | subject `notifyAllWaitingThreads`. |
|               | subject `wait:` msecsDuration `ifInterrupted:` [ "..." ]. |
| Adapters      | InterfaceType `asNew:` [ "..." ] |

#### Implementing Blocks ####

Early experiments with Bistro mapped Smalltalk blocks to anonymous inner classes derived from Block base classes.
A similar approach is taken with Hoot Smalltalk.
However, with the advent of Lambdas in Java SE 8, Hoot Smalltalk has a simpler mapping in how blocks are translated and used.
Largely, this results from the elimination of some boiler plate elements needed for inner classes.
Java lambdas have their analog in certain kinds of Smalltalk block closures.
So, translating those becomes syntactically much simpler.

```smalltalk
DyadicPredicate! sortBlock [ "orders a comparable pair of elements"
    ^[ : Ordered! a : Ordered! b | ^a < b ] toPredicate
]
```

... becomes ...

```java
public DyadicPredicate sortBlock()
{
  java.lang.String exitID = "SortedCollectionMetatype>>sortBlock";
  Frame f0 = new Frame(exitID);
  return (DyadicPredicate)Closure.with(f2 -> {
    Ordered a = f2.getValue(0).value();
    Ordered b = f2.getValue(1).value();
    return (a.lessThan(b));
  }, "a", "b").toPredicate();
}
```

In the translated code above, note usage of the **Closure** class, which wraps the Java closure:

```java
Closure.with(f2 -> { ... }, "a", "b").toPredicate();
```

There's also a conversion of the **Closure** to **DyadicPredicate**, which is the required type
returned by the **sortBlock** method.
The **Closure** class provides such convenience conversion methods, and the **Predicate** type defines the protocols
for producing **Boolean** results from functions, including wrapped lambdas.

It's also worth noting that the translation exposes the block arguments (a, b) as typed variables passed into the
closure from another **Frame** (f2).
So, there's some cooperation between the block implementation classes and the code generator.

#### Method Returns from Blocks ####

Like standard Smalltalk, Hoot Smalltalk supports the ability to return method results directly from inside
nested blocks using a message expression that begins with a `^` caret.
Method returns exit all enclosing block scopes, including the enclosing method scope.

The following **search:** method provides an example of this feature.
If the method finds any of the **searchTargets** in **aCollection**, it returns the element as the result of the method.
Note that in this case, the method result is returned by an exit from a nested block scope.

```smalltalk
search: aCollection for: searchTargets [
    aCollection do: [ :element |
        (searchTargets includes: element)
            ifTrue: [ ^element ] "<-- note method exit"
    ].
    ^nil
]
```

Early experiments with [Bistro][bistro] mapped method exits from nested scopes to Java using an exception based mechanism.
While convenient, this approach impacted performance, as raising and handling an exception tends to be slower than
simple method invocation and return.
A better mechanism for implementing method exits from nested blocks was informed and inspired in part by an article
written some years back by Allen Wirfs-Block: [Efficient Implementations of Smalltalk Block Returns][block-returns].

In Hoot Smalltalk, **Closures** provide the basis for block evaluation, methods calls, handling exceptions
(as exception handlers), and final blocks from **ensure:** and **ifCurtailed:** clauses mentioned above in
these standard Smalltalk message idioms.
When translating methods, the Hoot Smalltalk compiler code generator automatically inserts a stack **Frame** in every
generated method scope.
The Hoot Smalltalk runtime takes advantage of this to provide a better method exit mechanism than that
used in Bistro.

Instead of raising and catching an exception in its runtime, Hoot Smalltalk uses the stacked **Enclosures** to
find the most recently invoked method scope, and then force it to exit with a value.
The code generated by the Hoot Smalltalk compiler delegates this to the standard **Frame** included in every method.
Each standard method **Frame** knows the name of its enclosing method, which distinguishes it from any nested block
**Frames**.
Whether a method generates its statements within a surrounding Closure depends on whether or not there are any exit
statements within nested blocks inside the method.

| **Element** | **Description** |
| ----------- | --------------- |
| Enclosure   | a base class for both block Closures and exception handlers |
| Closure     | a kind of Enclosure that implements a Block |
| Predicate   | a kind of Closure that produces a Boolean result |
| Block       | emits Statements within a Closure (or Predicate) |
| Method      | emits Statements within a Method or a method Closure |

The following diagram shows the relationships between the elements that implement the Hoot Smalltalk method exit mechanism.

![Block Model][block-model]

As can be seen from this diagram, both **Enclosures** and **ExceptionContexts** are managed with a **CachedStack**.
Each such stack gets allocated and managed per thread.
The stacked **Enclosures** allow method and block closures with final blocks to be unwound and exited properly.
The stacked **ExceptionContexts** allow [exception handlers][except] to be managed properly within nested block scopes.

#### Threads ####

Hoot Smalltalk supports the common Smalltalk block **fork** idiom for spawning threads, but implements these threads
using primitive Java threads.
In Hoot Smalltalk, a block **fork** message returns a running instance of **java.lang.Thread**.

```smalltalk
aThread := [ "... block expressions ..." ] fork.
```

#### Thread Synchronization ####

Java supports thread synchronization on methods and within methods.
Hoot Smalltalk supports the declaration of @**Synchronized** methods and also supports object synchronization within methods.
The base class, **Hoot Behaviors Object**, provides a method that acquires a Java monitor.
Thus, any Hoot Smalltalk method may synchronize threads on an object by using a statement similar to the following one.

```smalltalk
"aquires a monitor on sample Object"
sample acquireMonitorDuring: [ "... critical section ..." ].
```

Hoot Smalltalk methods can also wait on an object monitor using the following idioms.

```smalltalk
"current thread waits on sample until notified or interrupted."
sample waitForChangeIfInterrupted: [ "..." ].

"current thread waits on sample until notified, interrupted, or a millisecondDuration expires."
sample waitForChange: millisecondDuration ifInterrupted: [ "..." ].
```

After a thread has been suspended using one these wait idioms, another thread can awaken the sleeping
thread using one of the following methods.

```smalltalk
"current thread awakens one thread waiting on sample."
sample awakenWaitingThread.

"current thread awakens all threads waiting on sample."
sample awakenAllWaitingThreads.
```

| **Back** | **Up** | **Next** |
| -------- | ------ | -------- |
| [Methods](methods.md#methods) | [Features](../#features) | [Exceptions](exceptions.md#exceptions) |

```
Copyright 2010,2023 Nikolas S Boyd. Permission is granted to copy this work 
provided this copyright statement is retained in all copies.
```


[smalltalk]: https://en.wikipedia.org/wiki/Smalltalk "Smalltalk"
[images]: https://en.wikipedia.org/wiki/Smalltalk#Image-based_persistence "Image Persistence"
[java]: https://en.wikipedia.org/wiki/Java_%28programming_language%29 "Java"
[csharp]: https://en.wikipedia.org/wiki/C_Sharp_%28programming_language%29 "C#"
[antlr]: https://www.antlr.org/ "ANTLR"
[st]: https://www.stringtemplate.org/ "StringTemplate"
[git]: https://git-scm.com/ "Git"
[github]: https://github.com/ "GitHub"
[nexus]: https://www.sonatype.com/nexus "Sonatype Nexus"
[generics]: https://en.wikipedia.org/wiki/Parametric_polymorphism "Generic Types"
[block-returns]: http://www.wirfs-brock.com/allen/things/smalltalk-things/efficient-implementation-smalltalk-block-returns

[bistro]: https://bitbucket.org/nik_boyd/bistro-smalltalk/ "Bistro"
[except]: exceptions.md#exceptions "Exceptions"
[block-model]: closures.png
