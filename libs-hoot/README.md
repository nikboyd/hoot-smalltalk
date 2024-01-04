### Hoot Class Library

Contains code for the Hoot Smalltalk library classes located in [src/main/hoot](src/main/hoot).
This project invokes the Hoot Smalltalk [compiler plugin][hoot-maven-plugin] to generate its equivalent Java code during
the build process.

| **Package** | **Contents** |
| ----------- | ------------ |
| Hoot Behaviors   | essential behaviors: `Object`, `Behavior`, `Class`, `Boolean`, etc. |
| Hoot Collections | various standard collections: `Array`, `Set`, `Dictionary`, `OrderedCollection`, etc. |
| Hoot Exceptions  | standard and extended exceptions: `Error`, `ZeroDivide`, etc. |
| Hoot Geometry    | simple geometry: `Point`, `Rectangle` |
| Hoot Magnitudes  | standard and literal values: `SmallInteger`, `Fraction`, `Fixed`, `Float`, etc. |
| Hoot Streams     | standard streams: `Transcript`, `FileStream`, `CollectionStream`, etc. |
| Hoot Tests       | code samples and [tests](../hoot-design/tests.md#test-framework) |
| Hoot Tools       | tool for running Hoot Smalltalk console [applications][console-apps] |

#### Notes

To ease integration with the underlying platform library, some of the standard Hoot Smalltalk classes wrap an underlying
platform class. Here are a few representative examples:

| **Hoot Smalltalk** | **Java** |
| -------- | -------- |
| MetaclassBase      | java.lang.Class |
| OrderedCollection  | java.util.ArrayList |
| IdentityDictionary | java.util.IdentityHashMap |
| IdentitySet        | java.util.IdentityHashMap |
| Dictionary         | java.util.HashMap |
| Set    | java.util.HashSet |
| String | java.lang.StringBuffer |
| Symbol | java.lang.String (**intern**-ed) |


```
Copyright 2010,2024 Nikolas S Boyd. Permission is granted to copy this work 
provided this copyright statement is retained in all copies.
```

[hoot-maven-plugin]: ../hoot-maven-plugin/README.md#hoot-maven-plugin
[tests]: ../hoot-design/tests.md#test-framework
[console-apps]: ../hoot-design/tests.md#running-applications
