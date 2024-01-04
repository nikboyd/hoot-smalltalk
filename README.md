### Hoot Smalltalk

<div align="left" >
<img align="left" src="logo.png" width="150" />
</div>

[Hoot Smalltalk](#hoot-smalltalk) is a variation of the original [Smalltalk][smalltalk].

* Hoot is [_experimental_](#introduction).
* Hoot runs Smalltalk on top of the Java Virtual Machine [JVM][jvm].
* Hoot integrates better Smalltalk [features](#features) with those of a JVM.

<br/>
each following badge links to some test reports:
<br/>
<br/>

<div align="left" >
<a href="https://hoot-docs-host-drm7kw4jza-uw.a.run.app/">
<img src="https://hoot-docs-host-drm7kw4jza-uw.a.run.app/hoot-runtime/maven_badge.png" />
</a>
<a href="https://hoot-docs-host-drm7kw4jza-uw.a.run.app/hoot-compiler/index.html">
<img src="https://hoot-docs-host-drm7kw4jza-uw.a.run.app/hoot-compiler/coverage_badge.png" />
</a>
</div>

<div align="left" >
<a href="https://hoot-docs-host-drm7kw4jza-uw.a.run.app/hoot-plugin/index.html">
<img src="https://hoot-docs-host-drm7kw4jza-uw.a.run.app/hoot-plugin/coverage_badge.png" />
</a>
<a href="https://hoot-docs-host-drm7kw4jza-uw.a.run.app/hoot-runtime/index.html">
<img src="https://hoot-docs-host-drm7kw4jza-uw.a.run.app/hoot-runtime/coverage_badge.png" />
</a>
<a href="https://hoot-docs-host-drm7kw4jza-uw.a.run.app/libs-hoot/index.html">
<img src="https://hoot-docs-host-drm7kw4jza-uw.a.run.app/libs-hoot/coverage_badge.png" />
</a>
</div>
<br/>

| **Section** | **Discussions** |
| ----------- | --------------- |
| [Introduction](#introduction) | some background for Hoot Smalltalk |
| [Platform Requirements](hoot-design/platform.md#platform-requirements) | tools you'll need for Hoot work |
| [Project Structure](#project-structure) | structure of this project repository |
| [Bundled Libraries](#bundled-libraries) | location and configuration of bundles |
| [Hoot Smalltalk Compiler](hoot-design/tools.md#hoot-smalltalk-compiler) | tools used to build the compiler |
| [Source Code Inclusion](hoot-design/inclusion.md#source-code-inclusion) | follows the footsteps of ST tradition |
| [Project Planning](hoot-design/planning.md#project-planning) | how to structure _your_ Hoot projects |
| [Hoot Smalltalk Features](#features) | lists the features of Hoot Smalltalk |
| [FAQs][faq] | frequently asked questions |

#### Introduction

Hoot Smalltalk (aka Hoot) is `experimental`. 
Hoot Smalltalk is the natural and conceptual successor to [Bistro Smalltalk][bistro].
Hoot builds on and extends the experience and understanding gained during
the development of [Bistro Smalltalk][bistro].

Whereas, the Bistro grammar included and supported some Java keywords like `static`, `public`, `private`,
Hoot Smalltalk makes all such code decorations optional [annotations][notes], and it uses naming conventions,
type inference, and [Lambda functions][lambdas] to support _**strong**_ dynamic types at runtime.

Hoot Smalltalk extends some of the ideas pioneered in Bistro:

* the [simplicity][st-syntax] and expressiveness of Smalltalk  message syntax,
* support for _most_ [ANSI standard Smalltalk][lib] type protocols,
* support for [_optional_ types][optional] via annotations,
* support for [_generic_ types][generics] via annotations.

Hoot Smalltalk also takes advantage of many existing tools and libraries.

* no proprietary virtual machine,
* no image-based persistence,
* integration with existing [tools][tools] (like [Git][git-doc] and GitHub),
* easy integration with other class libraries,
* including integration with [JUnit][junit] for tests.

You can write tests in Hoot Smalltalk that translate into Java tests run by JUnit.
Hoot Smalltalk provides its own [test framework][tests] that ultimately integrates with [JUnit][junit].

#### BYOVM

Hoot Smalltalk does not provide its own [virtual machine][st-image], like some [Smalltalks][st-imps].
Rather, Hoot Smalltalk takes advantage of already existing VMs that have matured over the last few decades.
The maturity of tool chains and the various options for the Java Virtual Machine JVM largely drove the choice of Java
as the primary foundation for Hoot Smalltalk.
That Java derived many of its early technical foundations from Smalltalk also helps.

#### No Image

Hoot Smalltalk does not use [image-based persistence][st-image] for storing its code.
Rather, like many programming languages, it uses simple text files with `.hoot` file type suffix.
This allows developers to use popular tools for text editing and source code [version control][version-control],
just as this [Git][git-doc] repository contains the code for Hoot Smalltalk itself.
Simple text files also allow you to use any of the existing integrated development tools that support Java.
Hoot Smalltalk was developed with [NetBeans][net-beans], largely because of its mature support for
and integration with [Maven][maven].

#### Project Structure

This repository contains the Hoot Smalltalk runtime, compiler, type and class libraries, and [design docs][design].
As indicated above, the library projects in this repo have a dependency structure, layer on layer.
Their transitive dependencies are structured (using Maven) such that upstream libraries get built before
downstream libraries.

This repository is organized into the following Maven projects, each of which builds a portion of Hoot overall.
Some of these use a mix of languages, but there's always a _primary_ language, as indicated.

| **Library** | **Code** | **Contents** |
| ----------- | ------------ | ------------ |
| [java-extend][java-extend]       | Java | Java library extensions |
| [hoot-abstracts][hoot-abstracts] | Java | runtime interfaces and classes |
| [hoot-runtime][hoot-runtime]     | Java | runtime foundation classes |
| [hoot-compiler-ast][hoot-compiler-ast] | Java | AST nodes and [grammar][grammar] |
| [hoot-compiler][hoot-compiler]   | Java | compiler library, [templates][code-lib] |
| [hoot-compiler-boot][hoot-compiler-boot] | Java | command to run the compiler |
| [hoot-maven-plugin][hoot-maven-plugin] | Java | run the compiler for a Maven project |
| [libs-smalltalk][libs-st]    | Hoot | Smalltalk protocols compiled by plugin |
| [libs-hoot][libs-hoot]              | Hoot | Hoot library classes compiled by plugin |
| [hoot-docs-bundle][docs-bundle]     | Java | bundled test coverage reports |
| [hoot-compiler-bundle][hoot-bundle] | both | plugin + compiler + runtime libs |
| [hoot-libs-bundle][libs-bundle]     | both | compiled hoot classes + runtime libs |

#### Bundled Libraries

Note the last two projects listed above.
These are bundled libraries that are hosted in [GitHub][hub-bundles].
Also, notice the Spring Boot application project listed above.

To simplify library dependencies in [other projects][eco-depot], it was decided to bundle the Hoot
libraries resulting from the Maven build process.
Two scenarios are most often used:
1. compiling Hoot Smalltalk source code + associated test code (if present), and
2. running the resultant applications.

Compiling Hoot code needs the Hoot Smalltalk compiler, its associated Maven plugin,
and the runtime support libraries. 
This scenario uses the [hoot-compiler-bundle][hoot-bundle].

Running a resulting [application][console-apps] needs the Hoot Smalltalk libraries and supporting runtime libraries.
This scenario uses the [hoot-libs-bundle][libs-bundle].

The [hoot-maven-plugin](#hoot-compiler-plugin) runs the Hoot compiler from the command line as a sub-task using
the [hoot-compiler-boot][hoot-compiler-boot] Spring Boot application to launch the compiler.
This mimics what you would do to run the compiler from the command line.

GitHub provides a package registry for hosting Maven artifacts.
The bundles and compiler plugin are hosted in the [package registry][hub-bundles] for this repository.
However, the GitHub package registry does not yet support anonymous access from an external Maven build.
So, the project artifacts are also hosted in a [Cloudsmith repository][cloud-repo].

#### Features ####

Many of the following features were first developed in the context of [Bistro Smalltalk][bistro].
However, Hoot Smalltalk provides several improvements over Bistro.
For example, Hoot Smalltalk has a uniform model for annotations and for the various language-specific
code decorations, e.g., `static`, `public`, `private`, etc.
Each link below leads to discussions of the specific language feature design details.

| **Feature** | **Summary** |
| ----------- | ----------- |
| [Language Model][model]    | Hoot Smalltalk has a _declarative_ language model. |
| [Name Spaces][spaces]      | Hoot class packages and name spaces are folder-based. |
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
| [Library][libs-st]         | Hoot includes types that define [ANSI Smalltalk][st-ansi] protocols. |
| [Blocks][blocks]           | Hoot blocks are implemented with Java Lambdas. |
| [Threads][threads]         | Hoot blocks support the **fork** protocol for spawning threads. |
| [Exceptions][except]       | Hoot supports both Smalltalk and Java exception handling. |
| [Tests][tests]             | Hoot also includes a [test framework][tests]. |
| [Tools][tools]             | Hoot needs some tools, including Maven. |
| [FAQ][faq]                 | Frequently asked questions about Hoot. |

| **Back** | **Up** | **Next** |
| -------- | ------ | -------- |
| ... | ... | [Language Model](hoot-design/model.md#language-model) |

```
Copyright 2010,2024 Nikolas S Boyd. Permission is granted to copy this work 
provided this copyright statement is retained in all copies.
```
See https://github.com/nikboyd/hoot-smalltalk/blob/main/LICENSE.txt for LICENSE details.


[logo]: logo.png "Hoot Owl"

[bistro]: https://bitbucket.org/nik_boyd/bistro-smalltalk/ "Bistro"
[smalltalk]: https://en.wikipedia.org/wiki/Smalltalk "Smalltalk"
[st-syntax]: https://en.wikipedia.org/wiki/Smalltalk#Syntax "Smalltalk Syntax"
[st-imps]: https://en.wikipedia.org/wiki/Smalltalk#List_of_implementations "Smalltalk Implementations"
[eco-depot]: https://github.com/nikboyd/eco-depot#eco-depot-hazmat-facility-conceptual-model

[jdk8]: https://openjdk.java.net/projects/jdk8/
[jdk11]: https://openjdk.java.net/projects/jdk/11/
[jdk17]: https://openjdk.org/projects/jdk/17/
[jdk21]: https://openjdk.org/projects/jdk/21/
[java-lts]: https://www.oracle.com/technetwork/java/java-se-support-roadmap.html
[java]: https://en.wikipedia.org/wiki/Java_%28programming_language%29 "Java"
[jvm]: https://en.wikipedia.org/wiki/Java_virtual_machine "Java Virtual Machine"
[lambdas]: https://www.oracle.com/webfolder/technetwork/tutorials/obe/java/Lambda-QuickStart/index.html
[inference]: https://developer.oracle.com/java/jdk-10-local-variable-type-inference
[graal-vm]: https://www.graalvm.org/docs/introduction/
[graal-install]: https://github.com/graalvm/graalvm-ce-builds/releases/tag/jdk-21.0.0
[truffle]: https://www.graalvm.org/graalvm-as-a-platform/language-implementation-framework/

[ikvm-home]: http://www.ikvm.net/
[mono-home]: https://www.mono-project.com/
[dot-net]: https://en.wikipedia.org/wiki/.NET_Framework
[csharp]: https://en.wikipedia.org/wiki/C_Sharp_%28programming_language%29 "C#"
[clr]: https://en.wikipedia.org/wiki/Common_Language_Runtime "Common Language Runtime"
[st]: https://www.stringtemplate.org/ "StringTemplate"
[antlr]: https://www.antlr.org/ "ANTLR"
[antlr-grammar]: https://github.com/antlr/antlr4/blob/master/doc/grammars.md
[antlr-parr]: https://parrt.cs.usfca.edu/
[maven]: https://maven.apache.org/
[maven-350]: https://maven.apache.org/docs/3.5.0/release-notes.html
[maven-395]: https://maven.apache.org/docs/3.9.5/release-notes.html
[maven-docker]: https://hub.docker.com/_/maven/
[life-cycle]: https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html
[net-beans]: https://netbeans.apache.org/
[junit]: https://junit.org/junit4/

[git-doc]: https://git-scm.com/
[hoot-ansi]: hoot-design/ANSI-X3J20-1.9.pdf
[squeak-ansi]: http://wiki.squeak.org/squeak/172
[st-ansi]: https://web.archive.org/web/20060216073334/http://www.smalltalk.org/versions/ANSIStandardSmalltalk.html
[st-image]: https://en.wikipedia.org/wiki/Smalltalk#Image-based_persistence
[version-control]: https://en.wikipedia.org/wiki/Version_control#Overview

[grammar]: hoot-compiler-ast/src/main/antlr4/Hoot/Compiler/Parser/Hoot.g4
[code-lib]: hoot-compiler/src/main/resources/CodeTemplates.stg

[design]: hoot-design/README.md#hoot-smalltalk-design-notes
[model]: hoot-design/model.md#language-model "Language Model"
[spaces]: hoot-design/libs.md#name-spaces "Name Spaces"
[classes]: hoot-design/libs.md#classes-and-metaclasses "Classes"
[types]: hoot-design/libs.md#types-and-metatypes "Types"
[access]: hoot-design/notes.md#access-controls "Access Controls"
[notes]: hoot-design/notes.md#annotations "Annotations"
[decor]: hoot-design/notes.md#decorations "Decorations"
[optional]: hoot-design/notes.md#optional-types "Optional Types"
[generics]: hoot-design/notes.md#generic-types "Generics"
[methods]: hoot-design/methods.md#methods "Methods"
[comments]: hoot-design/methods.md#comments "Comments"
[xop]: hoot-design/methods.md#interoperability "Interoperability"
[prims]: hoot-design/methods.md#primitive-methods "Primitives"
[blocks]: hoot-design/blocks.md#blocks "Blocks"
[except]: hoot-design/exceptions.md#exceptions "Exceptions"
[faq]: hoot-design/faq.md#frequently-asked-questions "Questions"
[usage]: hoot-design/usage.md#hoot-compiler-usage "Usage"
[threads]: hoot-design/blocks.md#threads "Threads"
[tests]: hoot-design/tests.md#test-framework "Tests"
[tools]: hoot-design/tools.md#tool-integration "Tools"
[console-apps]: hoot-design/tests.md#running-applications
[hoot-dotnet]: hoot-design/dotnet.md#running-hoot-smalltalk-on-net "Dot Net"

[java-extend]: java-extend/README.md#java-extensions
[hoot-abstracts]: hoot-abstracts/README.md#hoot-abstractions
[hoot-runtime]: hoot-runtime/README.md#hoot-runtime-library
[hoot-compiler-ast]: hoot-compiler-ast/README.md#hoot-compiler-library
[hoot-compiler]: hoot-compiler/README.md#hoot-compiler
[hoot-compiler-boot]: hoot-compiler-boot/README.md#hoot-compiler-boot
[hoot-maven-plugin]: hoot-maven-plugin/README.md#hoot-maven-plugin
[libs-hoot]: libs-hoot/README.md#hoot-class-library
[hoot-tests]: libs-hoot/src/test/hoot/Hoot/Tests
[libs-st]: libs-smalltalk/README.md#hoot-smalltalk-type-library
[hoot-bundle]: hoot-compiler-bundle/README.md
[libs-bundle]: hoot-libs-bundle/README.md
[docs-bundle]: hoot-docs-bundle/README.md
[plugin-example]: libs-hoot/pom.xml#L44
[java-profiles]: pom.xml#L316

[cloud-repo]: https://cloudsmith.io/~educery/repos/hoot-libs/packages/
[cloud-build]: https://cloud.google.com/cloud-build
[cloud-smith]: https://cloudsmith.com/

[hub-package]: https://github.com/nikboyd/hoot-smalltalk/packages/1130290
[hub-bundles]: https://github.com/nikboyd?tab=packages&repo_name=hoot-smalltalk
[hub-build]: https://docs.github.com/en/actions/learn-github-actions/understanding-github-actions#create-an-example-workflow
[hub-runners]: https://docs.github.com/en/actions/using-github-hosted-runners/about-github-hosted-runners#supported-runners-and-hardware-resources
[hub-pipe]: .github/workflows/main.yml#L11

[build]: shell/build-all-mods.sh
[build-pipe]: cloudbuild.yaml#L4
[build-cache]: cloudbuild.yaml#L36
[multi-core]: cloudbuild.yaml#L47
[lab-pipe]: .gitlab-ci.yml#L11
[lab-trigger]: shell/build-all-mods.sh#L42
[install-tools]: shell/install-tools.sh#L4

[hub-coverage]: https://hoot-docs-host-drm7kw4jza-uw.a.run.app/
[maven-badge]: https://hoot-docs-host-drm7kw4jza-uw.a.run.app/hoot-runtime/maven_badge.png
[runtime-coverage]: https://hoot-docs-host-drm7kw4jza-uw.a.run.app/hoot-runtime/coverage_badge.png
[compiler-coverage]: https://hoot-docs-host-drm7kw4jza-uw.a.run.app/hoot-compiler/coverage_badge.png
[plugin-coverage]: https://hoot-docs-host-drm7kw4jza-uw.a.run.app/hoot-plugin/coverage_badge.png
[libs-coverage]: https://hoot-docs-host-drm7kw4jza-uw.a.run.app/libs-hoot/coverage_badge.png
