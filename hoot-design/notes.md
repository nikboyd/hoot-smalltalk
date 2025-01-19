#### Annotations ####

Annotations have emerged in both [Java][java] and [C#][csharp] as a mechanism for programmers
to declaratively provide additional information about their intentions beyond the mere structure
and function of classes.
Annotations can be very powerful means for conveying information about:

* Object-Relational Mappings (ORM)
* Object Serialization and De-serialization (XML and JSON)
* Mapping RESTful APIs to Service Methods

And these are just a few of the more common, representative examples.
Because of their power and the way they complement types, Hoot Smalltalk supports the use of annotations,
and translates these into the corresponding annotations supported by its host languages.

Hoot Smalltalk annotations start with **@** and a class name, followed (optionally) by a message with
literals for arguments. For example:

```smalltalk
"a foreign key mapping"
@ManyToMany fetch: FetchType EAGER cascade: CascadeType ALL!
```

Because of their generality, Hoot Smalltalk also uses annotations uniformly to support host language keywords
that provide access controls and other code decorations.

#### Access Controls ####

In Hoot Smalltalk, all access controls are just another kind of [annotation](#annotations).
Access controls play an important role in defining contracts in object-oriented designs.
Like Java, Hoot Smalltalk provides access controls on classes, types, methods, and variables.
Hoot Smalltalk supports specification of access control, including @**Public**, @**Protected**, @**Private**,
which map to their corresponding keywords in the supported target languages.

While each class, method, and variable may be declared with any one of these three access controls,
Hoot Smalltalk uses the common Smalltalk conventions for default access when no explicit control has been supplied.
By default, Hoot Smalltalk classes and types are @**Public**, methods are @**Public**, and variables are @**Protected**.
Also, Hoot Smalltalk meta-classes and meta-types are always @**Public**.
All access controls are enforced at runtime by the Java VM.

#### Decorations ####

Hoot Smalltalk supports the common decorators, @**Abstract**, @**Final**, @**Static**, @**Default**.
A @**Default** method in a type definition is especially useful for _grafting_ shared
common behaviors onto class hierarchies that are otherwise unrelated.

#### Optional Types ####

Hoot Smalltalk allows variables and method arguments to have associated types, but does not require them.
Variables can also be assigned initial values. Here's an example of such an assignment.

```smalltalk
"define a static constant"
@Private @Static Integer! Zero := 0.
```

Note this example integrates the use of annotations, type specification, variable declaration, and
variable initialization. Here's an example method signature:

```smalltalk
"define a sort method"
@Public Boolean! sort: Integer! a : Integer! b [ "..." ]
```

Again, note the use of annotations and type specifications. This could also have been defined as follows:

```smalltalk
"define a sort method"
Boolean! sort: lowerInteger : upperInteger [ "..." ]
```

Without annotations that indicate otherwise, the Hoot Smalltalk compiler makes methods @Public.
Also, the Hoot Smalltalk compiler will infer that the arguments are both Integers from their names.

#### Generic Types ####

Another useful advance in both [Java][java] and [C#][csharp] was support for [generic types][generics].
Hoot Smalltalk also supports the definition and use of generic types, with appropriate mappings to Java.
For example, consider the following definition of the **Hoot Collections Dictionary** class:

```smalltalk
Collection? ElementType subclass:
CollectedDictionary? KeyType : ElementType!
Dictionary? KeyType -> Object : ElementType -> Object.
```

This type extends the generic **Collection** class, and uses both a **KeyType** and **ElementType** for its generic parameters.
Note that **KeyType** and **ElementType** both extend **Object**.
This produces a Java class with the following signature:

```java
public class Dictionary<KeyType extends Object, ElementType extends Object>
    extends Collection<ElementType> implements CollectedDictionary<KeyType, ElementType>
```

Thus, the **Dictionary** keys are comparable (via **Ordered**) and the values are compatible with the **ElementType**
constraint needed by the base **Collection** class.
Also, the **Dictionary** class implements the Smalltalk type defined by **CollectedDictionary**.
So, it supports the same protocols defined by that standard library type.

| **NEXT** | **BACK** | **UP** |
| -------- | -------- | ------ |
| <p align="center">[Methods][methods]</p><img width="250" height="1" /> | <p align="center">[Classes][classes]</p><img width="250" height="1" />  | <p align="center">[Features][features]</p><img width="250" height="1" />  |

```
Copyright 2010,2025 Nikolas S Boyd. Permission is granted to copy this work 
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
[nexus]: https://www.sonatype.com/nexus "Sonatype Nexus"
[generics]: https://en.wikipedia.org/wiki/Parametric_polymorphism "Generic Types"
