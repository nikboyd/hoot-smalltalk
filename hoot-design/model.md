#### Language Model ####

Traditionally, [Smalltalk][smalltalk] systems were built in the context of an object memory [image][images].
Smalltalk class definitions loaded from a source file were _interpreted_ in the context of
the memory [image][images].
While an [image][images]-based development environment contributes significantly to the agility of Smalltalk's
integrated suite of tools, it introduces some difficulties for source code management, component management,
system configuration, and release management in large development projects.

In contrast, many other programming languages are text file based for their source code,
including both [Java][java] and [C#][csharp].
Such declarative, file based programming languages make it much easier to use popular tools for version
control (like [Git][git] and [GitHub][github]) and component management (like [Nexus][nexus]).
For example, the [Java][java] language is file based, both for its source code and its executable binaries.
Also, the [Java][java] platform uses archive files (JARs, WARs, ...) to deploy its compiled class libraries.
The [C#][csharp] language is also file based for its source code, and its compiled binaries are packaged
as dynamic link library (DLL) files.

Thus, in order to better support source code management, better correspond with its target languages, and
thereby ease the utilization of those platforms, Hoot Smalltalk uses a text based, _declarative language model_
for its source code files (classes and types).
Here's an abbreviated example of a Hoot Smalltalk source code file (for a Point class):

```smalltalk
Hoot Magnitudes Number import.
Hoot Magnitudes Magnitude import.
Smalltalk Magnitudes Scalar import.

Magnitude subclass: Point. "A point on a 2-dimensional plane."
Point class "creating instances" members: [ "..." ].
Point "point arithmetic" members: [ "..." ].
```

#### Hoot Smalltalk Class Structure ####

The overall source code structure of each Hoot Smalltalk class resembles a series of Smalltalk messages.
Note that the Point class is located in the **Hoot Geometry** package, which the Hoot Smalltalk compiler assigns,
as it translates the Point class into Java.
Notice the example above shows an **import** for the Magnitude class.
The Hoot Smalltalk compiler generates some of the more common imports of runtime classes as it translates Hoot Smalltalk
code into its target language. See the discussion [Name Spaces](libs.md#name-spaces) for more about packages and imports.

The following table provides a more complete set of examples of possible Hoot Smalltalk class and type definition
templates.


| **Class Definitions** | **Type Definitions** | **Type Implementation** |
| --------------------- | -------------------- | ----------------------- |
| nil **subclass:** RootClass.        | nil **subclass:** RootType.       | nil **subclass:** TypeName! RootClass.        |
| Superclass **subclass:** Subclass.  | Supertype **subclass:** Subtype.  | Superclass **subclass:** TypeName! Subclass.  |


Notice that root classes and types are derived from **nil**.
Root types have no equivalent Java supertype, but root classes are derived from **java lang Object**
and root meta-classes are derived from **Hoot Behaviors Class**.
See the discussion about [Metaclasses](#classes-and-metaclasses) for more details.

Hoot Smalltalk classes implement types with the same syntax used to specify the type of a variable:
the type name followed by an exclamation point **!**.
So, a Hoot Smalltalk class implements multiple types simply by mentioning them in a sequence before the subclass name.

```smalltalk
Hoot Exceptions ExceptionBuilder import.
Hoot Exceptions SignaledException import.
Object subclass: ExceptionBuilder! SignaledException! Exception.
```

A Hoot Smalltalk type definition works a little differently. Each type can extend multiple super-types, but using
a comma-separated list.

```smalltalk
ExceptionDescription, ExceptionSignaler subtype: ExceptionBuilder.
```


| **NEXT** | **BACK** | **UP** |
| -------- | -------- | ------ |
| <p align="center">[Name Spaces][spaces]</p><img width="250" height="1" /> | <p align="center">[Project Planning][planning]</p><img width="250" height="1" />  | <p align="center">[Features][features]</p><img width="250" height="1" />  |

```
Copyright 2010,2024 Nikolas S Boyd. Permission is granted to copy this work 
provided this copyright statement is retained in all copies.
```


[design]: README.md#hoot-smalltalk-design-notes
[features]: README.md#features
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

[smalltalk]: https://en.wikipedia.org/wiki/Smalltalk "Smalltalk"
[images]: https://en.wikipedia.org/wiki/Smalltalk#Image-based_persistence "Image Persistence"
[java]: https://en.wikipedia.org/wiki/Java_%28programming_language%29 "Java"
[csharp]: https://en.wikipedia.org/wiki/C_Sharp_%28programming_language%29 "C#"
[antlr]: https://www.antlr.org/ "ANTLR"
[st]: https://www.stringtemplate.org/ "StringTemplate"
[git]: https://git-scm.com/ "Git"
[github]: https://github.com/ "GitHub"
[gitlab]: https://gitlab.com/ "GitLab"
[nexus]: https://www.sonatype.com/nexus "Sonatype Nexus"
[generics]: https://en.wikipedia.org/wiki/Parametric_polymorphism "Generic Types"
