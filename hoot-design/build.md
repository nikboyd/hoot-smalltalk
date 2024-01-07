#### Words of Advice!

* Ensure you have set **JAVA_HOME** for the JDK: the Hoot compiler _needs_ this.
* Don't muddy the waters! Always start _fresh_,  ...
* Empty your local **.m2/repository** when switching JDK versions. 
* Use small cycles when writing new code. Start with working code, then: ...
* [SOP][sop]: write a test, write new code, test the code, ...
* Repeat until the test passes and you have working code again.

#### Building from Sources

First, prepare your system with the [appropriate tools](#platform-requirements).
Then, clone this repository, and run the following shell command in the base project folder:

```
mvn -U -B clean install
```

This command will build all the Hoot Smalltalk runtime components and compiler, generate Java sources from
the Hoot Smalltalk sources for the various Hoot Smalltalk library types and classes, and run the included library tests.
This also resembles how the associated [build pipeline](tools.md#build-and-coverage-pipelines) runs in GitHub.

After you've built Hoot for the first time, you can build and run just the library tests as follows:

```
mvn -U -B -pl libs-hoot test
```

You can also be more selective by running a single test with Maven.

```
mvn -U -B -pl libs-hoot test -Dtest=BenchmarkTest
```

See the included [tests folder][hoot-tests] for a list of the available tests you can run.
There's also another way to run these tests using the [hoot-libs-bundle][libs-bundle].
See the additional [notes][tests] about using this way of running tests with the bundle.
You can also review the uploaded [test results][hub-coverage].

#### Tools Needed

* Hoot Smalltalk _requires_ at least Java SE [JDK 8][jdk8], but Java SE [JDK 21][jdk21] is now recommended.
* You'll also need [Maven][maven], currently [version 3.9.5][maven-395] is recommended.

This repo provides a [shell script][install-tools] for installing the required Java and Maven
versions on Ubuntu using **apt-get**.

If you intend to run Hoot Smalltalk on the [.Net][dot-net] [CLR][clr], you'll want **JDK 8** (not any later version)
and some [additional tools][hoot-dotnet], which depend on JDK 8.

#### Platform Requirements

The initial target platforms for Hoot Smalltalk include [Java][java] (and its [JVM][jvm]),
plus [.Net][dot-net] (and its [CLR][clr]).

Hoot Smalltalk was originally developed with Java SE [JDK 8][jdk8], partly due to its
[Long Term Support][java-lts] LTS and its support for [Lambdas][lambdas].
However, Java SE [JDK 11][jdk11] also has LTS and a few nice language enhancements, including
better type inference and support for local [var][inference] declarations.

* Hoot Smalltalk has now been improved and tested with OpenJDK 8, 11, 17, and 21.
* With selective Maven [configuration profiles][java-profiles], this project supports JDK 8-10 and JDK 11+.

The Hoot Smalltalk compiler tunes how it generates Java code depending on which Java version is available.
Advances in the Java platform allow the Hoot Smalltalk compiler to generate simpler Java code.
While it still supports JDK 8, [JDK 21][jdk21] is now recommended,
in order to better track the more recent platform upgrades.

That said,
* Hoot doesn't yet take advantage of any Java features that require a version later than JDK 11.
* We are tracking some of those being offered by [JDK 21][jdk21].

#### GraalVM Adopted as Recommended Platform

This note was updated in mid Oct 2023.

As of now, Hoot Smalltalk still _works fine_ with JDK 8-17.
However, with the advent of [GraalVM][graal-vm] and discovery of its support for polyglot programming and
language development using [Truffle][truffle], all future development of Hoot Smalltalk will be shifting focus
to integrate with those tools.

Even now, Hoot Smalltalk _as is_ runs fine with GraalVM for JDK 21 [21.0.0][graal-install].
And so, GraalVM is now recommended as the preferred platform on which to run the associated Java code,
esp. if you want to track the further development of Hoot Smalltalk.

More discussions about this will emerge as they gain focus. For now, the opportunities seem quite substantial.

#### Dot Net Support

Most of these discussions reference [Java][java] and how the Hoot Smalltalk compiler
translates Hoot Smalltalk code into Java code.
However, the design discussions also generally apply to running Hoot Smalltalk on [.Net][hoot-dotnet],
especially as regards how Hoot Smalltalk maps the Smalltalk [object model](hierarchy.md#type-hierarchy-diagram)
into a host language and its platform.

Provisional support for running Hoot Smalltalk on the [.Net][dot-net] [CLR][clr] platform requires
[Mono][mono-home] and [IKVM][ikvm-home].
If you want to run your Hoot Smalltalk code on the .Net CLR, be sure to read the [additional notes][hoot-dotnet]
about how this is currently supported.

| **NEXT** | **BACK** | **UP** |
| -------- | -------- | ------ |
| <p align="center">[Tools Used][tools]</p><img width="250" height="1" />  | <p align="center">[Introduction][intro]</p><img width="250" height="1" /> | <p align="center">[Features][features]</p><img width="250" height="1" />  |

```
Copyright 2010,2024 Nikolas S Boyd. Permission is granted to copy this work 
provided this copyright statement is retained in all copies.
```


[bistro]: https://bitbucket.org/nik_boyd/bistro-smalltalk/ "Bistro"
[smalltalk]: https://en.wikipedia.org/wiki/Smalltalk "Smalltalk"
[st-syntax]: https://en.wikipedia.org/wiki/Smalltalk#Syntax "Smalltalk Syntax"
[st-imps]: https://en.wikipedia.org/wiki/Smalltalk#List_of_implementations "Smalltalk Implementations"
[eco-depot]: https://github.com/nikboyd/eco-depot#eco-depot-hazmat-facility-conceptual-model
[sop]: https://en.wikipedia.org/wiki/Standard_operating_procedure

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
[antlr-parr]: https://explained.ai/
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

[hub-coverage]: https://hoot-docs-host-drm7kw4jza-uw.a.run.app/
[hub-package]: https://github.com/nikboyd/hoot-smalltalk/packages/1130290
[hub-bundles]: https://github.com/nikboyd?tab=packages&repo_name=hoot-smalltalk
[hub-build]: https://docs.github.com/en/actions/learn-github-actions/understanding-github-actions#create-an-example-workflow
[hub-runners]: https://docs.github.com/en/actions/using-github-hosted-runners/about-github-hosted-runners/about-github-hosted-runners#supported-runners-and-hardware-resources
[hub-pipe]: ../.github/workflows/main.yml#L11

[shells]: ../shell/README.md#shell-scripts
[build]: ../shell/build-all-mods.sh
[build-pipe]: ../cloudbuild.yaml#L4
[build-cache]: ../cloudbuild.yaml#L36
[multi-core]: ../cloudbuild.yaml#L47
[lab-pipe]: ../.gitlab-ci.yml#L11
[lab-trigger]: ../shell/build-all-mods.sh#L42
[install-tools]: ../shell/install-tools.sh#L4
