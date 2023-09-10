#### Exceptions ####

While some superficial similarities exist between [Smalltalk][smalltalk] and [Java][java] exception
handling mechanisms, there are also some fundamental differences.
So, deciding whether and how to integrate these mechanisms presented one of the more challenging
problems for the design of [Bistro][bistro].
Hoot Smalltalk benefits from the understanding of those experiments.
The following table compares the salient aspects of the Smalltalk and Java exception handling mechanisms.

| **Language** | **Feature** |
| ------------ | ----------- |
| Java      | models exceptions as instances of exception classes (**Throwable** and its subclasses) |
| Smalltalk | models exceptions as instances of exception classes (**Exception** and its subclasses) |
| Java      | special syntax for dealing with exceptions: `try { } catch { } finally { }`, **throw**, and **throws** |
| Smalltalk | no special syntax, only standard message idioms `[ ] catch: [ ] ensure: [ ]`, `[ ] ifCurtailed: [ ]` |
| Java      | exception handling is strictly stack oriented - once unwound, old stack frames are unavailable |
| Smalltalk | fine grained control: whether + when stack frames are unwound, whether execution resumes at the point of origin |
| Java      | method signatures must declare checked exceptions with a **throws** clause |
| Smalltalk | exceptions never impact method signatures |

#### Contexts and Closures

In Hoot Smalltalk, **Closures** provide the basis for block evaluation, methods calls, handling exceptions (as exception handlers), and
final blocks from **ensure:** and **ifCurtailed:** clauses mentioned above in these standard Smalltalk message idioms.

**ExceptionContexts** register exception handlers, and then each context manages evaluations of a block with some
registered exception handler(s) and/or a final block that always gets evaluated after completion of the others.
When an exception gets raised within an exception context, a matching handler **Enclosure** is found and evaluated.
Here are descriptions of some primary elements that participate in this mechanism.

| **Element** | **Description** |
| ----------- | --------------- |
| HandledException | an exception raised within a block or method |
| ExceptionContext | registers exception handlers and manages evaluations within that context |
| Enclosure   | a base class for both block Closures and exception handlers |
| Closure     | a kind of Enclosure that implements a Block |
| Block       | emits Statements within a Closure (or Predicate) |
| Method      | emits Statements within a Method or a method Closure |

The following diagram shows the relationships between some of these primary elements.

![Exception Model][exception-model]

As can be seen from this diagram, both **Enclosures** and **ExceptionContexts** are managed with a **CachedStack**.
Each such stack gets allocated and managed per thread.
The stacked **Enclosures** allow method and block closures with final blocks to be unwound properly.
The stacked **ExceptionContexts** allow exception handlers to be managed properly within nested block scopes.
Additionally, these mechanisms support rapid [method exits](blocks.md#method-returns-from-blocks) from within nested block scopes.

| **Back** | **Up** | **Next** |
| -------- | ------ | -------- |
| [Blocks](blocks.md#blocks) | [Features](../#features) | [Questions](faq.md#frequently-asked-questions) |

```
Copyright 2010,2023 Nikolas S Boyd.
Permission is granted to copy this work provided this copyright statement is retained in all copies.
```


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
[exception-model]: closures.png
