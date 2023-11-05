#### Methods ####

Hoot Smalltalk methods resemble standard [Smalltalk][smalltalk] methods.
The syntax for Smalltalk methods and blocks is very similar. Both contain a series of statements.
The primary difference is that blocks are delimited with square brackets `[ ]`, while methods are not.
In order to support a _declarative_ language model and normalize its syntax,
Hoot Smalltalk uses square brackets as scope delimiters for all scopes: protocols (member groups), methods, and
blocks are all delimited with `[ ]`.

```smalltalk
Point members: "functional arithmetic"
[
    negated [ ^x negated @ y negated ]
    "..."
]
```

#### Interoperability ####

To achieve (relatively) seamless integration between Hoot Smalltalk and Java, their methods must be interoperable.
Hoot Smalltalk code must be able to invoke methods in existing Java classes, and Java code must be able
to invoke methods in Hoot Smalltalk classes.
To achieve such interoperability, the Hoot Smalltalk compiler renames some methods using a set of naming conventions.

Hoot Smalltalk supports the standard binary operators provided by [Smalltalk][smalltalk].
When translating a binary method name, the Hoot Smalltalk compiler includes a `$` dollar prefix in the translated
method name to prevent confusion with existing Java method names (see examples below).

Hoot Smalltalk also supports standard Smalltalk keyword method signatures.
Each keyword is an ordinary word that ends with a **:** colon.

```smalltalk
dictionary at: key put: value.
```

When translating a keyword method name, the Hoot Smalltalk compiler replaces the colons with an underscore,
and drops the trailing colon(s).

Hoot Smalltalk also supports the use of colons as argument separators,
serving as anonymous keywords like those found in block signatures.
These are also dropped during translation, which allows Hoot Smalltalk to support the definition and
invocation of Java methods that take more than one argument (see the example below: **put:** key **:** value).

As Java and C# both support trailing variable arguments lists, Hoot Smalltalk also supports them.
These are method signatures which can take multiple arguments of the same type as the trailing method argument(s).
The following table shows a representative sample of the kinds of method naming changes
made by the Hoot Smalltalk compiler during translation:

| **Hoot Smalltalk** | **Java** |
| -------- | -------- |
| bool `&` aBoolean          | aBoolean.$and(anotherBoolean)  |
| bool `\|` aBoolean         | aBoolean.$or(anotherBoolean)  |
| x `@` y                    | x.$at(y)  |
| list `,` element           | list.$append(element)  |
| number `+` aNumber         | number.$plus(aNumber)  |
| number `-` aNumber         | number.$minus(aNumber)  |
| number `*` aNumber         | number.$times(aNumber)  |
| number `%` aNumber         | number.$modulus(aNumber)  |
| number `/` aNumber         | number.$dividedBy(aNumber)  |
| number `//` aNumber        | number.$idiv(aNumber)  |
| number `\\` aNumber        | number.$into(aNumber)  |
| number `\\\\` aNumber      | number.$imod(aNumber)  |
| value `=` aValue           | value.$equal(aValue)  |
| value `~=` aValue          | value.$notEqual(aValue)  |
| value `==` aValue          | value.$is(aValue)  |
| value `~~` aValue          | value.$isnt(aValue)  |
| value `<` aValue           | value.$lessThan(aValue)  |
| value `<=` aValue          | value.$lessEqual(aValue)  |
| value `>` aValue           | value.$moreThan(aValue)  |
| value `>=` aValue          | value.$moreEqual(aValue)  |
| jMap `get:` key            | jMap.get(key)  |
| jMap `put:` key `:` value  | jMap.put(key, value)  |
| dictionary `at:` key `put:` value | dictionary.at_put(key, value)  |

#### Dynamic Method Resolution ####

The lack of pervasive type declarations in [Smalltalk][smalltalk] contributes to its simplicity and agility.
Software prototyping is much easier without type specifications littered throughout the code.
Specifying types pervasively in code during development requires much more time.
Also, static type information simply makes the resolution of method implementations safer and more efficient.

Experiments with [Bistro][bistro] used [reflection][reflect] to resolve unknown method implementations at runtime.
The Bistro runtime then cached the resolved method references to speed up dynamic method invocations.
While this allowed Bistro to mimic Smalltalk, and made its dynamic method invocations work,
it also obviously had both space (memory) and time (performance) costs.

With the advent of support in Java for dynamic method invocation in both the VM and the runtime library,
it's now feasible to use those capabilities in Hoot Smalltalk.
The Hoot Smalltalk compiler and runtime cooperate and use dynamic method invocation of the underlying host platform,
be it Java or C#.
Hoot Smalltalk also uses this mechanism to implement the standard Smalltalk **perform:** operations.
Unimplemented methods discovered at runtime produce **MessageNotUnderstood** exceptions, just like Smalltalk.

As mentioned [elsewhere][optional], Hoot Smalltalk supports optional typing.
The Hoot Smalltalk compiler attempts to resolve the type of each message receiver.
For message primaries such as constants, variables and arguments, the compiler resolves
an inferred type (for constants) or a declared type (for variables and arguments).
For nested message expressions, the compiler attempts to resolve the type of each nested message result.
Where such type resolution is possible, the compiler determines whether the receiver implements
the message selector using the Java reflection facility.

If the compiler can locate an appropriate method during translation, the compiler generates a
direct method invocation against the receiver rather than a dynamic method resolution using
one of the **perform:** selectors.
Whenever the compiler can generate a direct method invocation, it also remembers the result
type of the invoked method.
This gives it the opportunity to resolve a further method invoked on the message result in the
case of nested message expressions.


#### Primitive Methods ####

[Smalltalk][smalltalk] is a strongly, but _dynamically_ typed language.
Smalltalk has no static type declarations on either variables, nor method arguments.
As experiments with [Bistro][bistro] confirmed, static type declarations can be used to improve
code safety and performance.
During translations, the Hoot Smalltalk compiler looks for opportunities to optimize method calls, but limits
itself to simple naming conventions, type inference, and discovery.

While [Bistro][bistro] provided the ability to include Java code for primitive methods,
Hoot Smalltalk provides the ability to indicate that a given method is primitive using @**Primitive**.
The Hoot Smalltalk compiler treats @**Primitive** methods differently, calling additional optimizations into play.
Such primitive methods get translated directly into elementary Java statements, and
require full type specifications for all arguments and local variables.
In other words, the compiler won't use **perform:** to resolve method calls within a @**Primitive** method.
If the compiler cannot resolve all the method calls inside a method marked @**Primitive**, it will output a warning.

#### Native and Abstract Methods ####

Hoot Smalltalk also provides the ability to indicate that a method is @**Native** or @**Abstract**.
Both of these kinds of methods must have an empty method body **[]**, as their implementations are provided elsewhere.
@**Abstract** methods must be implemented in a derived class.
@**Native** methods must be implemented as external functions, and made available through
[Java Native Interface (JNI)][jni] bindings.
The workings of JNI and how that can made to work with Hoot Smalltalk are (way) beyond the scope of this discussion.

| **Back** | **Up** | **Next** |
| -------- | ------ | -------- |
| [Annotations](notes.md#annotations) | [Features](../#features) | [Blocks](blocks.md#blocks) |

```
Copyright 2010,2023 Nikolas S Boyd. Permission is granted to copy this work 
provided this copyright statement is retained in all copies.
```

[bistro]: https://bitbucket.org/nik_boyd/bistro-smalltalk/ "Bistro"
[smalltalk]: https://en.wikipedia.org/wiki/Smalltalk "Smalltalk"
[images]: https://en.wikipedia.org/wiki/Smalltalk#Image-based_persistence "Image Persistence"
[java]: https://en.wikipedia.org/wiki/Java_%28programming_language%29 "Java"
[jni]: https://en.wikipedia.org/wiki/Java_Native_Interface
[csharp]: https://en.wikipedia.org/wiki/C_Sharp_%28programming_language%29 "C#"
[antlr]: https://www.antlr.org/ "ANTLR"
[st]: https://www.stringtemplate.org/ "StringTemplate"
[git]: https://git-scm.com/ "Git"
[github]: https://github.com/ "GitHub"
[nexus]: https://www.sonatype.com/nexus "Sonatype Nexus"
[generics]: https://en.wikipedia.org/wiki/Parametric_polymorphism "Generic Types"
[reflect]: https://docs.oracle.com/javase/7/docs/api/java/lang/reflect/package-summary.html "Java Reflection"

[optional]: notes.md#optional-types
