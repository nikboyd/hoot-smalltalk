#### Test Framework

Hoot Smalltalk provides a testing framework which leverages [JUnit][junit] under the covers.
The test framework was designed to support defining (mostly) simple tests conveniently.
The following foundation library types can be found in the **Hoot Tests** package.

| **Type** | **Description** |
| -------- | --------------- |
| Testable | defines the protocol for running a test |
| TestableClosure | runs a compiled closure as a test |
| TestableCode | makes some Hoot Smalltalk code (text) testable (as closures) |
| TestBase | a basic abstract test adapter for running a test with [JUnit][junit] |

There are two kinds of tests that are included in this repository:
1. Hoot Smalltalk samples that demonstrate usage of the libraries, and
2. Hoot platform tests that exercise the libraries and underlying runtime.

The first kind are all available for use as samples in the bundled library and can be exercised using **HootRunner**.

The later all use **TestBase** as their base class, and show a way to create tests for your Hoot Smalltalk projects.
Some of the provided platform tests also use **TestableCode** to turn a collection of simple Hoot Smalltalk expressions
into tests that just show the resulting values in the console.

Of course, it's also possible to use [JUnit][junit] directly.

#### Running Applications

After you've built a class library using Hoot Smalltalk, the Java platform provides several mechanisms for
running the generated Java code.
How you run your resulting code will depend a lot on which Java mechanism you're targeting:
console app, servlet, etc.
One of the simplest ways of running a library JAR will be as a console application that uses a command
like the following:

```shell
java -jar some.jar ...
```

But, this presumes you've built a console application with a **main** entry point.
The Hoot Smalltalk library provides just such a **main** entry point in **Hoot Tools HootRunner**.
You'll also find this library class included in the [hoot-libs-bundle][libs-bundle].

**HootRunner** can run any closure that implements the **MultiValuable** type.
**HootRunner** passes any arguments supplied to it on the command line into a **MultiValuable** type implementer,
whose fully qualified class name appears first on the command line.
For example after building Hoot, try the following commands in the base project folder:

```shell
java -jar hoot-libs-bundle/target/hoot-libs-bundle-2020.0101.0101.jar Hoot.Tests.HelloWorld
```
... and then ...
```shell
java -jar hoot-libs-bundle/target/hoot-libs-bundle-2020.0101.0101.jar Hoot.Tests.HelloWorld aaa bbb ccc
```

In the 2nd case, **HelloWorld** lists the arguments provide to it on the command line.
**HelloWorld** is a subclass of **TestableClosure**, and so it's also an implementer of **MultiValuable**.
Given your library depends on the [hoot-libs-bundle][libs-bundle], you can include a class of your own that
`extends` **TestableClosure** or that directly `implements` **MultiValuable**.
Then, you'll have a way to start your application from the command line similarly.

```
Copyright 2010,2023 Nikolas S Boyd. Permission is granted to copy this work 
provided this copyright statement is retained in all copies.
```

[junit]: https://junit.org/junit4/
[libs-bundle]: ../README.md#bundled-libraries
