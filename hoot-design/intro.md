#### Introduction

Hoot Smalltalk (aka Hoot) is `experimental`.

* Hoot Smalltalk is the natural and conceptual successor to [Bistro Smalltalk][bistro].
* Hoot builds on and extends the experience gained with [Bistro][bistro].
* Bistro included and supported some Java keywords like `static`, `public`, `private`.
* Hoot makes such code decorations optional [annotations][notes].
* Hoot uses naming conventions, type inference, and Java [lambda functions][lambdas].
* Hoot provides _**strong**_ [dynamic types][combo-type] at runtime.

Hoot Smalltalk extends some of the ideas pioneered in Bistro:

* the [simplicity][st-syntax] and expressiveness of Smalltalk  message syntax,
* support for _most_ [ANSI standard Smalltalk][libs-st] type protocols,
* support for [_optional_ types][optional],
* support for [_generic_ types][generics].

Hoot Smalltalk also takes advantage of many existing tools and libraries.

* no proprietary [virtual machine](#byovm),
* no [image-based](#no-image) persistence,
* integration with existing [tools][tools] (like [Git][git-doc] and GitHub),
* easy integration with other Java class libraries,
* including [integration](#test-framework) with [JUnit][junit] for tests.

#### BYOVM

Hoot Smalltalk does not provide its own [virtual machine][st-vm], like some [Smalltalks][st-imps].
Rather, Hoot Smalltalk takes advantage of already existing VMs that have matured over some few decades.
The maturity of its tool chains and the various options for the [Java virtual machine][jvm] largely drove 
the choice of [Java][java] as the primary foundation for Hoot Smalltalk.
That Java derived many of its [early technical foundations][hot-spot] from Smalltalk also helped.

#### No Image

Hoot Smalltalk does not use [image-based persistence][st-image] for storing its code.
Rather, like many programming languages, it uses simple text files with `.hoot` file type suffix.
This allows developers to use popular tools for text editing and source code [version control][version-control],
just as this [Git][git-doc] repository contains the code for Hoot Smalltalk.
Simple text files also allow you to use any of the existing integrated development tools that support Java.
Hoot Smalltalk was developed with [NetBeans][net-beans], largely because of its mature support for
and integration with [Maven][maven].

#### Test Framework

Hoot Smalltalk provides its own [test framework][tests] that ultimately integrates with [JUnit][junit].
So, you can write and run your own tests in Hoot Smalltalk.
After you've [built Hoot][build] for the first time, you can build and run just the library tests as follows:

```
mvn -U -B -pl libs-hoot test
```

You can also be more selective by running the tests in a single test class with Maven.

```
mvn -U -B -pl libs-hoot test -Dtest=BenchmarkTest
```

See the included [tests folder][hoot-tests] for a list of the available tests you can run.
There's also another way to run these tests using the [hoot-libs-bundle][libs-bundle].
See the additional [notes][tests] about using this way of running tests with the bundle.
You can also review the uploaded [test results][hub-coverage] that are hosted using the [hoot-docs-bundle][docs-bundle].


| **NEXT** | **BACK** | **UP** |
| -------- | -------- | ------ |
| <p align="center">[Build][build]</p><img width="250" height="1" /> | <p align="center">...</p><img width="250" height="1" />  | <p align="center">[Features][features]</p><img width="250" height="1" />  |

```
Copyright 2010,2025 Nikolas S Boyd. Permission is granted to copy this work 
provided this copyright statement is retained in all copies.
```


[bistro]: https://bitbucket.org/nik_boyd/bistro-smalltalk/src/master/README.md "Bistro"
[smalltalk]: https://en.wikipedia.org/wiki/Smalltalk "Smalltalk"
[st-vm]: https://github.com/OpenSmalltalk/opensmalltalk-vm/?tab=readme-ov-file#overview
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
[hot-spot]: https://en.wikipedia.org/wiki/HotSpot_(virtual_machine)#History
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
[build]: build.md#building-from-sources "Build"
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
[tools]: tools.md#tool-integration "Tools"
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

[hub-coverage]: https://hoot-docs-host-drm7kw4jza-uw.a.run.app/
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

