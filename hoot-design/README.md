### Hoot Smalltalk Design Notes

<div align="left" >
<img align="left" src="../logo.png" width="150" />
</div>

Hoot Smalltalk is a variation of [Smalltalk][smalltalk] that integrates the best features of Smalltalk
with those of Java and its virtual machine VM.

Many of the following features were originally developed in the context of [Bistro Smalltalk][bistro].
However, Hoot Smalltalk provides several improvements over Bistro.

Each link below leads to discussions of the specific language feature design details.

| **Feature** | **Summary** |
| ----------- | ----------- |
| [Language Model][model]    | Hoot Smalltalk has a _declarative_ language model. |
| [Name Spaces][spaces]      | Hoot class packages and name spaces are based on folders. |
| [Classes][classes]         | Hoot classes are hybrids like standard Smalltalk. |
| [Meta-classes][classes]    | Hoot supports meta-classes like those in standard Smalltalk. |
| [Types][types]             | Hoot supports first-class interfaces (as types) like Java. |
| [Meta-types][types]        | Hoot types can have associated meta-types. |
| [Access Control][access]   | Hoot can use access controls: @**Public**, @**Protected**, @**Private**. |
| [Decorations][decor]       | Hoot supports: @**Abstract**, @**Final**,   @**Static**, @**Default**. |
| [Annotations][notes]       | Hoot translates other annotations to a host language. |
| [Optional Types][optional] | Hoot variable and argument type specifications are _optional_. |
| [Generic Types][generics]  | Hoot supports definition and use of **generic types**. |
| [Methods][methods]         | Hoot methods resemble those of standard Smalltalk. |
| [Interoperability][xop]    | Hoot method names become compatible host method names. |
| [Primitives][prims]        | Hoot supports @**Primitive** methods. |
| [Comments][comments]       | Hoot comments are copied into the host language. |
| [Standard Library][lib]    | Hoot includes types that define [ANSI Smalltalk][st-ansi] protocols. |
| [Blocks][blocks]           | Hoot blocks are implemented with Java Lambdas. |
| [Threads][threads]         | Hoot blocks support the **fork** protocol for spawning threads. |
| [Exceptions][except]       | Hoot supports both Smalltalk and Java exception handling. |
| [Tests][tests]             | Hoot also includes a [test framework][tests]. |
| [FAQ][faq]                 | Frequently asked questions about Hoot. |

```
Copyright 2010,2023 Nikolas S Boyd. Permission is granted to copy this work 
provided this copyright statement is retained in all copies.
```

[logo]: ../logo.png "Hoot Owl"

[bistro]: https://bitbucket.org/nik_boyd/bistro-smalltalk/ "Bistro"
[smalltalk]: https://en.wikipedia.org/wiki/Smalltalk "Smalltalk"
[st-ansi]: https://web.archive.org/web/20060216073334/http://www.smalltalk.org/versions/ANSIStandardSmalltalk.html

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
[hoot-dotnet]: dotnet.md#running-hoot-smalltalk-on-net "Dot Net"
[lib]: ../libs-smalltalk/README.md#hoot-smalltalk-type-library
