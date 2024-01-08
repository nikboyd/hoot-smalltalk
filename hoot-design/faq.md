### Frequently Asked Questions ###

* [Why run Smalltalk on GraalVM?](#why-run-smalltalk-on-graalvm)
* [Why is Hoot Smalltalk different from standard Smalltalk?](#why-is-hoot-smalltalk-different-from-standard-smalltalk)
* [What differences are there between Hoot Smalltalk and standard Smalltalk?](#what-differences-are-there-between-hoot-smalltalk-and-standard-smalltalk)
* [What limitations are there in Hoot Smalltalk?](#what-limitations-are-there-in-hoot-smalltalk)

#### Why run Smalltalk on GraalVM?

[Smalltalk][smalltalk], [Java][java], and [C#][csharp] all have their technical merits.
If you look at the history of advances in software technology, many of the innovations found in Java trace their
conceptual origins back to Smalltalk.
Smalltalk was the first language and tool set to bring fully (pure) object-oriented programming into the mainstream.
[Smalltalk-80][smalltalk] was the first commercially available tool set that included: a virtual machine VM,
an integrated suite of development tools IDE, code for most of its library classes as Open Sources, the ability
to develop code on one platform and easily port to another: a limited form of Write Once, Run Anywhere.

Together, Smalltalk and the Java ecosystem provide a powerful combination.
Likewise for C# plus .Net, which also derive much of their technical merits from both Java and
Smalltalk as conceptual predecessors.
From a certain point of view, the integration of Smalltalk with these platforms seems inevitable.
The advent and maturing of [GraalVM][graal-vm] furthers this ambition, as can seen embodied in
[TruffleSqueak][truffle-squeak].

**1. Smalltalk is a better modeling language than Java and C#.**

I've been a Java developer for a good portion of my [career][nik-career], many many years.
Still, I've been a fan of Smalltalk and got to [work with it][nik-st] extensively in the early 90s.

Smalltalk is far more expressive and readable than Java and C#, partly due to Smalltalk's simplicity.
But also, as I indicated in [Using Natural Language in Software Development][using-natural-language],
Smalltalk provides a natural way to model the elements of problem descriptions expressed in English
(and at least potentially, any natural language) by directly supporting the
[linguistic metaphors][language-metaphor] we use in [object-oriented][objects] software designs.
Thus, Smalltalk can provide superior traceability from code back to the original solution requirements.

With its blend of elements from both Smalltalk and Java, Hoot Smalltalk may better express notions
of [Enduring Business Themes and Business Objects][stable-concept],
while Java and C# may better encode [Industrial Objects][stable-concept].
Although, Hoot Smalltalk supports all three kinds of object designs: Policy (Themes), Business, and Industrial Objects.
A somewhat representative example applying these ideas can be found in the [ECO depot conceptual model][eco-depot].

**2. Smalltalk has first-class meta-classes, while Java does not (nor does C#).**

Object-oriented designs often need meta-protocols for factory and registry methods.
Object-oriented frameworks with meta-protocols often make polymorphic meta-protocols desirable.
Java `static` methods support a kind of inheritance, but without full polymorphism.
So, Java syntax does not directly support full meta-design and meta-programming.
First Smalltalk provided first-class [meta-classes][meta-classes], and now Hoot Smalltalk provides that feature
along with first-class [meta-types][meta-types], a feature which supports both type inheritance and meta-type polymorphism
for the design of meta-protocols.

**3. Java and C# provide name spaces and standardized binary distribution mechanisms.**

Smalltalk had no [name space](libs.md#name-spaces) mechanism.
Java and C# both have name space mechanisms built into their languages and VMs.
The lack of name spaces:
* often creates type naming conflicts, especially for commonly used names,
* thereby contaminates names to differentiate them between libraries,
* complicates the integration of libraries from disparate sources.

Smalltalk had no standardized binary distribution mechanism for its class libraries.
While a typical Smalltalk image-based memory model contributes to its agility and productivity,
it complicates final product packaging and deployment.

Java provides several options for packaging and deploying its binary artifacts, 
including JARs=Java archives, WARs=web archives, EARs=enterprise archives, etc.
C# has its class binaries combined into assemblies as DLLs=dynamic link libraries.

By supporting various byte-coded and bit-coded binary distribution formats, GraalVM provides the mechanisms
needed to integrate libraries from various independent origins.

**4. The JVM and CLR offer relative ubiquity for deployment. LLVM too? WASM??**

Given the relative ubiquity of the JVM (esp. with [OpenJDK][open-jdk]) and
CLR (esp. with [.NET Core][net-core]), and their availability across many platforms,
it made most sense to target these foundations first.
At some time in the future, it may also make sense to target [Low-Level VM LLVM][low-level-vm],
and perhaps [Web Assembly WASM][web-assembly].

#### 2021-03 Update

As noted in the [build notes][graal-adopt], [GraalVM][graal-vm] was [discovered][vm-news] and appears to offer a well
designed and mature set of tools for further development of Hoot Smalltalk in the context of [Truffle][truffle].
GraalVM appears to have both [LLVM][graal-llvm] and [WASM][graal-wasm] in its sights as targets.
So, prior aspirations toward those appear to be covered by the adoption of GraalVM as base platform for Hoot Smalltalk.

Whether GraalVM will ever fully support [C#][graal-langs], its libraries, and integration with .Net Core remains to be seen.
In principle, this appears completely do-able. So, it's likely just a matter of time.
Whether such coverage of .Net and its languages will address developer needs and have any kind of adoption
raises additional questions well beyond the scope of current consideration.

Meanwhile, project focus for Hoot Smalltalk will be shifting towards better integration with GraalVM using Truffle.
Within this main focus will also be possible integrations with other Smalltalk implementations, including
both [TruffleSqueak][truffle-squeak] and [Pharo Smalltalk][pharo-st].
If you have a specific interest in helping port Hoot Smalltalk to GraalVM, or further explorations of it within that
frame, please contact the project lead, nikboyd AT sonic.net.

#### Why is Hoot Smalltalk different from standard Smalltalk?

Many ideas found in Hoot Smalltalk were pioneered in [Bistro][bistro].
Much like Bistro, the design of Hoot Smalltalk was driven by the following goals:

* Retain much of the simplicity, expressiveness, and readability of Smalltalk
* Compile Hoot Smalltalk code to its supported host platform binaries
* Leverage the VM and language advances made in the supported host platforms
* Provide seamless integration with existing class libraries
* Support the standard [ANSI Smalltalk][st-ansi] protocols
* Optimize with direct method calls as much as possible
* Support @**Primitive** methods written in Hoot Smalltalk

Using annotations uniformly for both code decorations and host language annotations provided a major
simplification of the Hoot Smalltalk syntax over that offered by [Bistro][bistro].
Using more of the available punctuation for type declarations permitted better support for
[optional typing][optional], better support for casting, and better definition and usage of
[generic types][generics].

Simple punctuation **( @ ? ! ^ <- -> ... [ ] )** makes these decorations less obtrusive in the code,
retaining readability, while gaining the power of these additions to the language model:

* Optional Types
* Generic Types
* Type Casting
* Uniform Annotations

This also includes the ability for method signatures to have trailing variable argument lists (with **...**),
providing a way to support this common idiom found in both Java and C#.

#### What differences are there between Hoot Smalltalk and standard Smalltalk?

There are many similarities between Hoot Smalltalk and standard Smalltalk, but also some differences.

**1. Hoot Smalltalk supports standard Smalltalk message syntax, with some extensions.**

To retain the expressiveness and readability of standard Smalltalk, Hoot Smalltalk uses the same syntax for
unary, binary, and keyword messages.
However, to support seamless integration with Java, Hoot Smalltalk extends the keyword message syntax with
trailing argument lists.
Thus, keyword messages can include trailing arguments separated by colons, as in the following example.

```smalltalk
Point basicNew: 0 : 0
```

The Hoot Smalltalk compiler translates the expression above into the following Java code.

```java
new Point(0, 0)
```

**2. Hoot Smalltalk blocks and methods resemble those of standard Smalltalk.**

Smalltalk blocks and methods have many similarities.
However, Smalltalk blocks contain their signatures,
while method signatures precede their bodies.
So, while blocks have delimiters **[ ]**, standard Smalltalk method bodies don't.
This was one of the factors that originally drove the design of the standard Smalltalk [chunk file format][chunk-files].
When represented in a flat file, Smalltalk method definitions need some kind of delimiter - hence the use of bang **!**
to mark the end of each Smalltalk method.

```smalltalk
"sample Smalltalk method (chunk)"
yourself
    ^self !
```

Hoot Smalltalk resolves these differences with a uniform syntax:
all scopes are delimited with square brackets **[ ]**.

```smalltalk
"sample Hoot Smalltalk method"
yourself [ ^self ]
```

This simple change in syntax eliminates the need for interpreted code chunks and allows for a declarative class
definition format.

**3. Hoot Smalltalk variables can be defined and initialized together.**

Hoot Smalltalk variables can be initialized when they first appear within a scope.
This includes instance and `static` variables associated with a class (or type), and local variables
within a method scope or a block scope.

```smalltalk
sampleVariable := 5.
```

This resembles the syntax of Java, and it's somewhat simpler than the corresponding idioms for standard Smalltalk.
It introduces a small limitation though: each variable must be declared before use
(see the [note](#what-limitations-are-there-in-hoot-smalltalk) below for more details).

**4. Hoot Smalltalk supports optional type annotations.**

While not required, variables and arguments may also have associated type annotations.

```smalltalk
SmallInteger! sampleVariable := 5.
```

**5. Hoot Smalltalk type declarations are messages, with associated type members.**

Unlike standard Smalltalk, Hoot Smalltalk supports the definition of types separate from classes.
Hoot Smalltalk types (and meta-types) get translated into Java interfaces.
Here's a sample from the **Smalltalk Magnitudes** package.

```smalltalk
Numeric subtype: Ratio. "Smalltalk Rational type (ANSI X3J20 section 5.6.3)."

Ratio type members: [ ]
Ratio "accessing" members:
[
    Ordinal! numerator "the numerator of this ratio" []
    Ordinal! denominator "the denominator of this ratio" []
]
```

This sample shows how **Ratio** is a simple extension subtype of **Numeric**.
Note that all method signatures in Hoot Smalltalk type definitions have empty method scopes **[]**.
Both @**Abstract** and @**Native** methods also have [empty method scopes](methods.md#native-and-abstract-methods).

Note also that **Ratio** has (an empty) meta-type **Ratio type**.
The Hoot Smalltalk compiler then also generates a nested meta-type in Java named **Ratio Metatype**
which extends **Numeric Metatype**.

The [ANSI Smalltalk][st-ansi] protocols have all been converted into Hoot Smalltalk type definitions.
The appropriate Hoot Smalltalk library classes implement the
Smalltalk [standard types](../libs-smalltalk#hoot-smalltalk-type-library).

**6. Hoot Smalltalk class declarations are messages, with associated class members.**

Here's a (partial) sample from the **Hoot Geometry** package.

```smalltalk
Hoot Magnitudes Magnitude import.
"... more imports ..."

Magnitude subclass: Point. "A point on a 2-dimensional plane."
Point class members: "creating instances" [ "..." ]
Point "arithmetic" members: [ "..." ]
```

Note that a class definition can **import** types and classes from other packages.
There are several variations of [class definition](model.md#hoot-class-structure) patterns supported by Hoot Smalltalk.
See the Hoot Smalltalk library classes for examples.

**7. Hoot Smalltalk uses Java-style packages for its name spaces.**

The Hoot Smalltalk compiler assigns each class to a Java package based on its location in the file system (relative to a base folder).
While the C# language does not require files to be located in package folders to indicate their name spaces,
the Hoot Smalltalk compiler organizes the C# code it generates in that way also.

#### What limitations are there in Hoot Smalltalk?

**1. Variable declarations must precede their use.**

The Hoot Smalltalk compiler scans the source code in a single pass.
Also, it automatically generates locally scoped variables for undeclared references it detects in method and block scopes.

So, the compiler needs to detect and add variables to its symbol table before they are referenced elsewhere in the code.
Otherwise, the compiler won't properly detect that a declared variable is a class member (for example), and it will
automatically generate a local variable for the method (or block) scope in which it appears.

During the development of the Hoot Smalltalk class library, this was found to be a frequent problem.
However, the solution is rather simple.
Re-order the bits of code so that variable declarations **always precede their usage**.

| **NEXT** | **BACK** | **UP** |
| -------- | -------- | ------ |
| <p align="center">[Compiler Usage][usage]</p><img width="250" height="1" /> | <p align="center">[Tests][tests]</p><img width="250" height="1" />  | <p align="center">[Features][features]</p><img width="250" height="1" />  |

```
Copyright 2010,2024 Nikolas S Boyd. Permission is granted to copy this work 
provided this copyright statement is retained in all copies.
```

[nik-career]: https://educery.dev/about/cvs/history/#work-history
[nik-st]: https://educery.dev/about/cvs/citigroup/#summary

[features]: README.md#features
[except]: exceptions.md#exceptions "Exceptions"
[usage]: usage.md#hoot-compiler-usage "Usage"
[tests]: tests.md "Tests"

[bistro]: https://bitbucket.org/nik_boyd/bistro-smalltalk/ "Bistro"
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
[chunk-files]: https://wiki.squeak.org/squeak/1105 "Chunk File Format"

[open-jdk]: https://en.wikipedia.org/wiki/OpenJDK "OpenJDK"
[net-core]: https://en.wikipedia.org/wiki/.NET_Core ".NET Core"
[low-level-vm]: https://llvm.org/
[web-assembly]: https://webassembly.org/
[vm-news]: https://www.infoq.com/news/2021/01/graalvm-21-jvm-java/

[graal-adopt]: build.md#graalvm-adopted-as-recommended-platform
[graal-vm]: https://www.graalvm.org/docs/introduction/
[graal-install]: https://github.com/graalvm/graalvm-ce-builds/releases/tag/vm-21.0.0.2
[truffle]: https://www.graalvm.org/graalvm-as-a-platform/language-implementation-framework/
[graal-llvm]: https://www.graalvm.org/reference-manual/llvm/
[graal-wasm]: https://www.graalvm.org/reference-manual/wasm/
[graal-langs]: https://www.graalvm.org/uploads/graalvm-language-level-virtualization-oracle-tech-papers.pdf

[truffle-squeak]: https://github.com/hpi-swa/trufflesqueak#getting-started
[pharo-st]: https://pharo.org/

[hoot-ansi]: ANSI-X3J20-1.9.pdf
[squeak-ansi]: https://wiki.squeak.org/squeak/172
[st-ansi]: https://web.archive.org/web/20060216073334/http://www.smalltalk.org/versions/ANSIStandardSmalltalk.html
[eco-depot]: https://github.com/nikboyd/eco-depot#eco-depot-hazmat-facility-conceptual-model

[objects]: https://educery.dev/papers/software-metaphors/#objects
[using-natural-language]: https://educery.dev/papers/modeling/natural-language-software-development/#head
[stable-concept]: https://educery.dev/educe/patterns/nuclear-sentence/stable-concept/#consequences
[language-metaphor]: https://educery.dev/papers/software-metaphors/#natural-language
[optional]: notes.md#optional-types "Optional Types"
[generics]: notes.md#generic-types "Generics"
[meta-classes]: libs.md#classes-and-metaclasses
[meta-types]: libs.md#types-and-metatypes
