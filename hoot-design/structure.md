#### Project Structure

This repository contains the Hoot Smalltalk runtime, compiler, type and class libraries, and [design docs][design].
The library projects in this repo have a dependency structure, layer on layer.
Their transitive dependencies are structured with Maven such that libraries get built in their proper order.

This repository is organized into the following Maven projects, each of which builds a portion of Hoot overall.
Some of these use a mix of languages, but there's always a _primary_ language, as indicated.

| **Library** | **Code** | **Contents** |
| ----------- | ------------ | ------------ |
| [java-extend][java-extend]       | Java | Java library extensions |
| [hoot-abstracts][hoot-abstracts] | Java | runtime interfaces and classes |
| [hoot-runtime][hoot-runtime]     | Java | runtime foundation classes |
| [hoot-compiler-ast][hoot-compiler-ast] | Java | [grammar][grammar], [parser][compiler-tool], AST nodes |
| [hoot-compiler][hoot-compiler]   | Java | [command line interface][usage], [templates][code-lib] |
| [hoot-compiler-boot][hoot-compiler-boot] | Java | process to run a compiler command |
| [hoot-maven-plugin][hoot-maven-plugin] | Java | runs the [compiler][compiler-tool] for a Maven project |
| [libs-smalltalk][libs-st]    | Hoot | [Smalltalk protocols][libs-st], compiled by [plugin][plugin-tool] |
| [libs-hoot][libs-hoot]              | Hoot | [Hoot library][libs-hoot] classes, compiled by [plugin][plugin-tool] |
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
This mimics what you would do to run the compiler from the command line _by hand_.

GitHub provides a package registry for hosting Maven artifacts.
The bundles and compiler plugin are hosted in the [package registry][hub-bundles] for this repository.
However, the GitHub package registry does not yet support anonymous access from an external Maven build.
So, the project artifacts are also hosted in a [Cloudsmith repository][cloud-repo].

| **NEXT** | **BACK** | **UP** |
| -------- | ------ | -------- |
| <p align="center">[Project Planning][planning]</p><img width="250" height="1" />  | <p align="center">[Tools Used][tools]</p><img width="250" height="1" /> | <p align="center">[Features][features]</p><img width="250" height="1" />  |

```
Copyright 2010,2025 Nikolas S Boyd. Permission is granted to copy this work 
provided this copyright statement is retained in all copies.
```


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
[combo-type]: https://en.wikipedia.org/wiki/Type_system#Combining_static_and_dynamic_type_checking

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
[hoot-ansi]: ANSI-X3J20-1.9.pdf
[squeak-ansi]: http://wiki.squeak.org/squeak/172
[st-ansi]: https://web.archive.org/web/20060216073334/http://www.smalltalk.org/versions/ANSIStandardSmalltalk.html
[st-image]: https://en.wikipedia.org/wiki/Smalltalk#Image-based_persistence
[version-control]: https://en.wikipedia.org/wiki/Version_control#Overview

[grammar]: ../hoot-compiler-ast/src/main/antlr4/Hoot/Compiler/Parser/Hoot.g4
[code-lib]: ../hoot-compiler/src/main/resources/CodeTemplates.stg

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

[cloud-repo]: https://cloudsmith.io/~educery/repos/hoot-libs/packages/
[cloud-build]: https://cloud.google.com/cloud-build
[cloud-smith]: https://cloudsmith.com/

[hub-package]: https://github.com/nikboyd/hoot-smalltalk/packages/1130290
[hub-bundles]: https://github.com/nikboyd?tab=packages&repo_name=hoot-smalltalk
[hub-build]: https://docs.github.com/en/actions/learn-github-actions/understanding-github-actions#create-an-example-workflow
[hub-runners]: https://docs.github.com/en/actions/using-github-hosted-runners/about-github-hosted-runners#supported-runners-and-hardware-resources
[hub-pipe]: ../.github/workflows/main.yml#L11

[build]: ../shell/build-all-mods.sh
[build-pipe]: ../cloudbuild.yaml#L4
[build-cache]: ../cloudbuild.yaml#L36
[multi-core]: ../cloudbuild.yaml#L47
[lab-pipe]: ../.gitlab-ci.yml#L11
[lab-trigger]: ../shell/build-all-mods.sh#L42
[install-tools]: ../shell/install-tools.sh#L4
