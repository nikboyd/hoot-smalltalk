#### Name Spaces ####

The absence of a name space mechanism is a major deficiency of most traditional [Smalltalks][smalltalk].
The absence of name spaces permits the occurrence of class naming conflicts, especially when integrating large
class libraries from disparate software development organizations, esp. third party libraries.
While some name space models have been proposed in the past for Smalltalk, one has not yet been widely adopted,
and the ANSI X3J20 committee did not include any name space model in the [Smalltalk standard][st-ansi].

Name spaces help designers organize their classes and help prevent potential class naming conflicts.
Several modern programming languages support the organization of classes using name spaces.
Both of the target languages, [Java][java] and [C#][csharp], support the name space concept.
C# supports name spaces directly in its language syntax.

Both extant Smalltalk implementations and Java have a notion of **packages**.
However, those found in Smalltalk are completely different from those found in Java.
Java supports the name space concept with its **packages**, whose names correspond with the folders in which
class files are located.
Java class files are also physically organized into folders in a file system.
There is a direct correspondence between the logical and physical organizations.
The following diagram depicts these relationships.

![Packaging][packages]

The overall organization of Hoot Smalltalk classes resembles that found in [Java][java]: classes (and types)
are located in folders whose names correspond to their name spaces.
Name spaces can be nested, and their path names correspond to their folder path names.
Hoot Smalltalk name spaces are assigned automatically by the compiler, based on the folder
location of each Hoot Smalltalk source file.
As it translates Hoot Smalltalk code into Java, the Hoot Smalltalk compiler generates a corresponding Java
class file along with its package name and folder location.

All classes defined in a name space (Java package) are immediately visible to each other.
**@Public** classes from other name spaces can be made visible by importing them.
Each **import** establishes visibility to an individual class, or all the **@Public** classes in a package.

```smalltalk
Hoot Behaviors importAll. "all behavior classes"
Hoot Collections OrderedCollection import.
```

As with Java, any class outside the scope of the current package may be imported,
or a class may be qualified by the name of the package in which it was defined.
So, while the import in the above code fragment makes **OrderedCollection** visible,
it may also be fully qualified as **Hoot Collections OrderedCollection** in the code of a method.

#### Classes and Metaclasses ####

Hoot Smalltalk classes are translated into [Java][java] classes.
Hoot Smalltalk class member variables and methods become Java class variables and methods.
However, Smalltalk has first-class meta-classes, while Java classes don't (neither do C# classes).
Hoot Smalltalk implements its meta-classes by splitting each class definition in two parts -
one Java class for the Hoot Smalltalk class methods and variables, and another Java class for the
Hoot Smalltalk meta-class methods and variables.
Thus, the Hoot Smalltalk meta-class hierarchy generally parallels the Hoot Smalltalk class hierarchy.
This approach provides inheritance and polymorphism for meta-classes like that found in Smalltalk.

An included [diagram](hierarchy.md#type-hierarchy-diagram) shows the full inheritance hierarchy for some of the
essential core Hoot Smalltalk classes.
Each Hoot Smalltalk meta-class is implemented as a **public static** nested class of the corresponding Java class.
The corresponding nested class name is always **Metaclass**.
Then, each class has a **public static** member **$class** that refers to the sole instance of its singleton meta-class.
However, because meta-classes are singletons, they do not support the definition of **abstract** methods.
The following list shows the parallels between a few selected classes and their meta-classes.

```
Hoot Behaviors Object
Hoot Behaviors Object Metaclass

Hoot Collections OrderedCollection
Hoot Collections OrderedCollection Metaclass

Hoot Geometry Point
Hoot Geometry Point Metaclass
```

So, each class definition includes a **static** link to its meta-class via **$class**.
However, each **$class** link must be resolved (by instantiation) after the inheritance links
have been established (during compilation).
Making each meta-class a **public static** nested class enables this.
Then, each meta-class also has a reference to its corresponding outer class **instanceClass**.
Here's a representative example from the code generated for the Hoot **Behavior** class.

```java
public abstract class Behavior extends Object implements Classified
{
  public static Metaclass type() { return (Metaclass)Metaclass.$class; }
  @Override public Metaclass $class() { return (Metaclass)Metaclass.$class; }
  public static class Metaclass extends Object.Metaclass implements Classified.Metatype
  {
    static final Behavior.Metaclass $class = new Behavior.Metaclass();
    public Metaclass() {
      this(Behavior.Metaclass.class);
      initialize();
    }

    public Metaclass(java.lang.Class aClass) {
      super(aClass);
      instanceClass = Behavior.class;
    }

  }

  // ...
}

```


The included [type hierarchy diagram](hierarchy.md#type-hierarchy-diagram) depicts the
relationships of the core behavior classes.
All root classes, those derived from **nil**, have inheritance and meta-class structures
like that of **Hoot Behaviors Object**.
A Hoot Smalltalk class can also be derived from a Java class.
In this case, the inheritance and meta-class structures of the generated classes also looks
like that of **Hoot Behaviors Object**.

#### Types and Metatypes ####

Support for defining interfaces is one of the more powerful and innovative features of Java (and C#).
Type interfaces provide a language mechanism for defining polymorphic behavior independent of inheritance.
Hoot Smalltalk supports the definition and use of interfaces as **types**.
However, because Smalltalk supports first-class meta-classes and full meta-programming,
Hoot Smalltalk takes a further step by coupling each type with a **meta-type**, similar to how classes and
their meta-classes are implemented.
This gives software designers the ability to coordinate programming and meta-programming.
This can be especially useful for framework designs.

Each Hoot Smalltalk type and meta-type is translated into a Java interface definition.
Each meta-type interface is defined as a nested interface of its corresponding type interface.
As with Java interfaces, Hoot Smalltalk supports type inheritance.
As with Hoot Smalltalk classes and meta-classes, the meta-type inheritance structures parallel the type inheritance structures.

When a Hoot Smalltalk class implements a type, its corresponding meta-class also implements the corresponding meta-type(s).
When a Hoot Smalltalk class implements a pre-existing Java interface, a corresponding meta-type does not exist.
And so consequently, the Hoot Smalltalk meta-class does not (and can't) implement those meta-types (which don't exist).

Here are a couple of representative examples from the Hoot Smalltalk streams library.
The following types take their shapes from the indicated sections of the ANSI Smalltalk standard.

`Smalltalk Streams StreamReader` (5.9.9)
```java
public interface StreamReader<ElementType extends Subject>
    extends StreamedSource<ElementType>, StreamedSequence<ElementType>
{
  public static interface Metatype
        extends StreamedSource.Metatype, StreamedSequence.Metatype
  {
    public  <ElementType extends Subject> StreamReader<ElementType>
        on(final CollectedReadably<ElementType> aCollection);
  }
}
```

`Smalltalk Streams StreamWriter` (5.9.11)
```java
public interface StreamWriter<ElementType extends Subject>
    extends StreamedSink<ElementType>, StreamedSequence<ElementType>
{
  public static interface Metatype
        extends StreamedSink.Metatype, StreamedSequence.Metatype
  {
    public  <ElementType extends Subject> StreamWriter<ElementType>
        with(final CollectedVariably<ElementType> aCollection);
  }
}
```

Notice that these types are fairly complex:

* each extends other types whose shapes are also defined by the ANSI standard,
* each type is _generic_: they are parameterized with **ElementType**, and
* each has a nested _**Metatype**_ that defines how their respective instances get created.

So, they pull together several of the type definition options supported by Hoot Smalltalk, and show how these translate
into the underlying type mechanisms supported by Java.
You can find more details about the mapping of the ANSI standard in the
[Smalltalk Type Library](../code-smalltalk#smalltalk-type-library).

| **NEXT** | **BACK** | **UP** |
| -------- | -------- | ------ |
| <p align="center">[Annotations][notes]</p><img width="250" height="1" /> | <p align="center">[Language Model][model]</p><img width="250" height="1" />  | <p align="center">[Features][features]</p><img width="250" height="1" />  |


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
[nexus]: https://www.sonatype.com/nexus "Sonatype Nexus"
[generics]: https://en.wikipedia.org/wiki/Parametric_polymorphism "Generic Types"

[hoot-ansi]: ANSI-X3J20-1.9.pdf
[squeak-ansi]: https://wiki.squeak.org/squeak/172
[st-ansi]: https://web.archive.org/web/20060216073334/http://www.smalltalk.org/versions/ANSIStandardSmalltalk.html
[type-diagram]: https://github.com/nikboyd/hoot-smalltalk/blob/main/hoot-design/pics/behaviors.svg "Metaclasses"
[packages]: https://github.com/nikboyd/hoot-smalltalk/blob/main/hoot-design/pics/packages.svg "Packaging"
