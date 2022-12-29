### Hoot Smalltalk Design Notes

![Hoot Owl][logo]

Hoot Smalltalk is a variation of [Smalltalk][smalltalk] that integrates the best features of Smalltalk
with those of Java and its virtual machine VM.
Many of the following features were originally developed in the context of [Bistro Smalltalk][bistro].
However, Hoot Smalltalk provides several improvements over Bistro.

Each link below leads to discussions of the specific language feature design details.

| **Feature** | **Summary** |
| ----------- | ----------- |
| [Language Model][model]    | Hoot Smalltalk has a _declarative_ language model. |
| [Name Spaces][spaces]      | Hoot Smalltalk class packages and name spaces are based on folders. |
| [Classes][classes]         | Hoot Smalltalk classes are hybrids based primarily on standard Smalltalk. |
| [Meta-classes][classes]    | Hoot Smalltalk supports meta-classes like those found in standard Smalltalk. |
| [Types][types]             | Hoot Smalltalk supports first-class interfaces (as types) like Java. |
| [Meta-types][types]        | Hoot Smalltalk types can have associated meta-types. |
| [Access Control][access]   | Hoot Smalltalk can use access controls: @**Public**, @**Protected**, @**Private**. |
| [Decorations][decor]       | Hoot Smalltalk supports: @**Abstract**, @**Final**,   @**Static**. |
| [Annotations][notes]       | Hoot Smalltalk translates other annotations into those of its host language. |
| [Optional Types][optional] | Hoot Smalltalk variable and argument type specifications are _optional_. |
| [Generic Types][generics]  | Hoot Smalltalk supports definition and use of **generic types**. |
| [Methods][methods]         | Hoot Smalltalk methods resemble those of standard Smalltalk. |
| [Interoperability][xop]    | Hoot Smalltalk method names become compatible host method names. |
| [Primitives][prims]        | Hoot Smalltalk supports @**Primitive** methods. |
| [Comments][comments]       | Hoot Smalltalk comments are copied into the host language. |
| [Standard Library][lib]    | Hoot Smalltalk includes types that define [ANSI Smalltalk][st-ansi] protocols. |
| [Blocks][blocks]           | Hoot Smalltalk blocks are implemented with Java Lambdas. |
| [Threads][threads]         | Hoot Smalltalk blocks support the **fork** protocol for spawning threads. |
| [Exceptions][except]       | Hoot Smalltalk supports both Smalltalk and Java exception handling. |
| [Tests][tests]             | Hoot Smalltalk also includes a [test framework][tests]. |
| [Questions][faq]           | Frequently Asked Questions FAQ about Hoot Smalltalk. |

[logo]: hoot-owl.svg "Hoot Owl"

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
[lib]: libs-smalltalk/README.md#hoot-smalltalk-type-library
