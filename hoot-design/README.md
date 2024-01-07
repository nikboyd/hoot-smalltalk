### Hoot Smalltalk Design Notes

* Hoot Smalltalk is a variation of the original [Smalltalk][smalltalk].
* Hoot integrates better Smalltalk [features](#features) with those of a [JVM][jvm].

#### Features

Each section link below leads to a discussion about specific language features and design details.

| **Section** | **Summary** |
|:----------- |:----------- |
| [Introduction][intro]      | some background notes |
| [Building Hoot][build]     | building the runtime, libraries, and tests |
| [Tools Needed][tool-needs] | tools you'll need to work with Hoot |
| [Tools Used][tools]        | Hoot integrates with some tools, including Maven. |
| [Structure][structure]     | the structure of this repository |
| [Planning][planning]       | how to structure _your_ Hoot projects |
| [Commands][usage]          | how to compile Hoot code from the command line |
| **Design** | **Summary** |
| [Language Model][model]    | Hoot Smalltalk has a _declarative_ language model. |
| [Name Spaces][spaces]      | Hoot class packages and name spaces are based on folders. |
| [Classes][classes]         | Hoot classes are hybrids like standard Smalltalk. |
| [Meta-classes][classes]    | Hoot supports meta-classes like those in standard Smalltalk. |
| [Types][types]             | Hoot supports first-class interfaces (as types) like Java. |
| [Meta-types][types]        | Hoot types can have associated meta-types. |
| [Access Control][access]   | Hoot can use access controls: @**Public**, @**Protected**, @**Private**. |
| [Decorations][decor]       | Hoot supports: @**Abstract**, @**Final**,   @**Static**, @**Default**. |
| [Annotations][notes]       | Hoot translates other annotations to a host language. |
| [Optional Types][optional] | Hoot variable and argument type specifications are _**optional**_. |
| [Generic Types][generics]  | Hoot supports definition and use of **generic types**. |
| [Methods][methods]         | Hoot methods resemble those of standard Smalltalk. |
| [Interoperability][xop]    | Hoot method names become compatible host method names. |
| [Primitives][prims]        | Hoot supports @**Primitive** methods. |
| [Comments][comments]       | Hoot comments are copied into the host language. |
| [Library][libs-st]         | Hoot includes types that define [ANSI Smalltalk][st-ansi] protocols. |
| [Blocks][blocks]           | Hoot blocks are implemented with Java Lambdas. |
| [Threads][threads]         | Hoot blocks support the **fork** protocol for spawning threads. |
| [Exceptions][except]       | Hoot supports both Smalltalk and Java exception handling. |
| [Tests][tests]             | Hoot also includes a [test framework][tests]. |
| [FAQ][faq]                 | Frequently asked questions about Hoot. |

```
Copyright 2010,2024 Nikolas S Boyd. Permission is granted to copy this work 
provided this copyright statement is retained in all copies.
```

[logo]: ../logo.png "Hoot Owl"

[java]: https://en.wikipedia.org/wiki/Java_%28programming_language%29 "Java"
[jvm]: https://en.wikipedia.org/wiki/Java_virtual_machine "Java Virtual Machine"

[bistro]: https://bitbucket.org/nik_boyd/bistro-smalltalk/ "Bistro"
[smalltalk]: https://en.wikipedia.org/wiki/Smalltalk "Smalltalk"
[st-ansi]: https://web.archive.org/web/20060216073334/http://www.smalltalk.org/versions/ANSIStandardSmalltalk.html

[design]: README.md#hoot-smalltalk-design-notes
[intro]: intro.md#introduction "Intro"
[build]: build.md#building-from-sources "Build"
[tool-needs]: build.md#tools-needed "Tools Needed"
[tools]: tools.md#tool-integration "Tools"
[planning]: planning.md#project-planning "Planning"
[structure]: structure.md#project-structure "Structure"
[model]: model.md#language-model "Language Model"
[spaces]: libs.md#name-spaces "Name Spaces"
[classes]: libs.md#classes-and-metaclasses "Classes"
[types]: libs.md#types-and-metatypes "Types"
[access]: notes.md#access-controls "Access Controls"
[notes]: notes.md#annotations "Annotations"
[decor]: notes.md#decorations "Decorations"
[optional]: notes.md#optional-types "Optional Types"
[generics]: notes.md#generic-types "Generics"
[methods]: methods.md#methods "Methods"
[comments]: methods.md#comments "Comments"
[xop]: methods.md#interoperability "Interoperability"
[prims]: methods.md#primitive-methods "Primitives"
[blocks]: blocks.md#blocks "Blocks"
[except]: exceptions.md#exceptions "Exceptions"
[faq]: faq.md#frequently-asked-questions "Questions"
[usage]: usage.md#hoot-compiler-usage "Usage"
[threads]: blocks.md#threads "Threads"
[tests]: tests.md#test-framework "Tests"
[console-apps]: tests.md#running-applications
[hoot-dotnet]: dotnet.md#running-hoot-smalltalk-on-net "Dot Net"

[java-extend]: ../java-extend/README.md#java-extensions
[hoot-abstracts]: ../hoot-abstracts/README.md#hoot-abstractions
[hoot-runtime]: ../hoot-runtime/README.md#hoot-runtime-library
[hoot-compiler-ast]: ../hoot-compiler-ast/README.md#hoot-compiler-library
[hoot-compiler]: ../hoot-compiler/README.md#hoot-compiler
[hoot-compiler-boot]: ../hoot-compiler-boot/README.md#hoot-compiler-boot
[hoot-maven-plugin]: ../hoot-maven-plugin/README.md#hoot-maven-plugin
[libs-hoot]: ../libs-hoot/README.md#hoot-class-library
[hoot-tests]: ../libs-hoot/src/test/hoot/Hoot/Tests
[libs-st]: ../libs-smalltalk/README.md#hoot-smalltalk-type-library
[hoot-bundle]: ../hoot-compiler-bundle/README.md
[libs-bundle]: ../hoot-libs-bundle/README.md
[docs-bundle]: ../hoot-docs-bundle/README.md
[plugin-example]: ../libs-hoot/pom.xml#L44
[java-profiles]: ../pom.xml#L316
