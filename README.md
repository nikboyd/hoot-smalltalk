### Hoot Smalltalk

![Hoot Owl][logo]

Hoot Smalltalk is a variation of [Smalltalk][smalltalk] that integrates the best features of Smalltalk
with those of Java and its virtual machine VM.

[Coverage Test Results][hub-coverage] 

![Runtime Coverage][runtime-coverage] ![Compiler Coverage][compiler-coverage] ![Plugin Coverage][plugin-coverage] ![Library Coverage][libs-coverage] ![Maven Result][maven-badge] 


| **Section** | **Discussions** |
| ----------- | --------------- |
| [Introduction](#introduction) | some background for Hoot Smalltalk |
| [Platform Requirements](#platform-requirements) | tools needed to build Hoot Smalltalk and work with it |
| [Project Structure](#project-structure) | structure of this project repository |
| [Bundled Libraries](#bundled-libraries) | location and configuration of the project bundles |
| [Hoot Smalltalk Compiler](#hoot-smalltalk-compiler) | tools used to construct the compiler |
| [Source Code Inclusion](#source-code-inclusion) | following in the footsteps of long-standing Smalltalk tradition |
| [Project Planning](#project-planning) | recommendations for how to structure _your_ Hoot Smalltalk projects |
| [Hoot Smalltalk Features](#features) | lists the features of Hoot Smalltalk |
| [Frequently Asked Questions][faq] | frequently asked questions |

#### Introduction

Hoot Smalltalk is an experimental new programming language which builds on experience and understanding gained during
the development of [Bistro Smalltalk][bistro].
Hoot Smalltalk is the natural and conceptual successor to [Bistro Smalltalk][bistro].

Whereas, the Bistro grammar included and supported some Java keywords like `static`, `public`, `private`,
Hoot Smalltalk makes all such code decorations optional [annotations][notes], and it uses naming conventions,
type inference, and [Lambda functions][lambdas] to support _strong_ dynamic types at runtime.

Hoot Smalltalk extends some of the ideas pioneered in Bistro:

* the [simplicity][st-syntax] and expressiveness of Smalltalk  message syntax,
* support for _most_ [ANSI standard Smalltalk][lib] type protocols,
* support for [_optional_ type annotations][optional],
* support for [_generic_ types annotations][generics].

Hoot Smalltalk also takes advantage of many existing tools and libraries.

* no image-based persistence,
* no proprietary virtual machine,
* integration with existing tools,
* easy integration with other class libraries.

Example: You can write tests in Hoot Smalltalk that translate into Java tests run by JUnit.
Hoot Smalltalk provides its own [testing framework][tests] that ultimately integrates with [JUnit 4.11][junit].

Hoot Smalltalk does not use [image-based persistence][st-image] for storing its code.
Rather, like many programming languages, it uses simple text files with `.hoot` file type suffix.
This allows developers to use popular tools for text editing and source code [version control][version-control],
just as this [Git][git-doc] repository contains the code for Hoot Smalltalk itself.
Simple text files also allow you to use any of the existing integrated development tools that support Java.
Hoot Smalltalk was developed with [NetBeans][net-beans], largely because of its mature support for
and integration with [Maven][maven].

Hoot Smalltalk does not provide its own [virtual machine][st-image], like some [Smalltalks][st-imps].
Rather, Hoot Smalltalk takes advantage of already existing VMs that have matured over the last few decades.
The maturity of tool chains and the various options for the Java Virtual Machine JVM largely drove the choice of Java
as the primary foundation for Hoot Smalltalk.

#### Platform Requirements ####

Hoot Smalltalk was originally developed with Java SE [JDK 8][jdk8], partly due to its
[Long Term Support][java-lts] LTS and its support for [Lambdas][lambdas].
However, Java SE 11 also has LTS and a few nice language enhancements, including
better type inference and support for local [var][inference] declarations.

The initial target platforms for Hoot Smalltalk include [Java][java] (and its [JVM][jvm]),
plus [.Net][dot-net] (and its [CLR][clr]).
With selective Maven [configuration profiles][java-profiles], the Hoot project supports both JDK 8 (up through 10)
and JDK 11 (and above).
The Hoot Smalltalk compiler will tune how it generates Java code depending on which platform is available.
Advances in the Java platform allow the Hoot Smalltalk compiler to generate simpler Java code.

So, ...

Hoot Smalltalk _requires_ at least Java SE [JDK 8][jdk8].
You'll also need [Maven][maven], at least [version 3.5.0][maven-350] is recommended.
If you intend to run Hoot Smalltalk on the [.Net][dot-net] [CLR][clr], you'll want JDK 8 (not any later version)
and some [additional tools][hoot-dotnet], which depend on JDK 8.

This repo provides a [shell script][install-tools] for installing the required Java and Maven
versions on Ubuntu using **apt-get**.

#### GraalVM Adopted as Recommended Platform

This note was added early in March 2021.

As of now, Hoot Smalltalk still _works fine_ with JDK 8 (through 10) and 11+.
However, with the advent of [GraalVM][graal-vm] and discovery of its support for polyglot programming and
language development using [Truffle][truffle], all future development of Hoot Smalltalk will be shifting focus
to integrate with those tools.
Even now, Hoot Smalltalk _as is_ runs fine with GraalVM CE [21.0.0.2][graal-install].
And so, GraalVM is now recommended as the preferred platform on which to run the associated Java code,
esp. if you want to track the further development of Hoot Smalltalk.

More discussions about this will emerge as they gain focus. For now, the opportunities seem quite substantial.

#### Building from Sources ####

Clone this repository, and run the following shell command in the base project folder:

```
mvn -U -B clean install
```

This also resembles how the associated [build pipeline](#build-and-coverage-pipelines)
runs in GitHub.
This command will build all the Hoot Smalltalk runtime components and compiler, generate Java sources from
the Hoot Smalltalk sources for the various Hoot Smalltalk library types and classes, and run the included library tests.

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
You can also review the uploaded [test results](https://nikboyd.github.io/hoot-smalltalk/).

#### Project Structure

This repository contains the Hoot Smalltalk runtime, compiler, type and class libraries, and [design docs][design].
As indicated above, the library projects in this repo have a dependency structure, layer on layer.
Their transitive dependencies are structured (using Maven) such that upstream libraries get built before
downstream libraries.

This repository is organized into the following Maven projects, each of which builds a portion of Hoot overall.
Some of these use a mix of languages, but there's always a _primary_ language, as indicated.

| **Project** | **Summary** | **Language** | **Contents** |
| ----------- | ----------- | ------------ | ------------ |
| [hoot-abstracts][hoot-abstracts] | abstractions | Java | mostly interfaces for the runtime, with a few basic classes |
| [hoot-runtime][hoot-runtime]     | runtime | Java | essential Hoot runtime foundation classes |
| [hoot-compiler][hoot-compiler]   | compiler | Java | Hoot [grammar][grammar], code [templates][code-lib], AST elements, compiler |
| [hoot-compiler-boot][hoot-compiler-boot] | launch | Java | Spring Boot app to launch the compiler from a command |
| [hoot-maven-plugin][hoot-maven-plugin] | compiler plugin | Java | runs the compiler to generate Java code from a library project |
| [libs-smalltalk][libs-smalltalk] | Smalltalk types | Hoot | Hoot sources for Smalltalk library types + compile via the plugin |
| [libs-hoot][libs-hoot]           | Hoot classes | Hoot | Hoot sources + tests for library classes + compile via the plugin |
| [hoot-compiler-bundle][hoot-bundle] | compiler bundle | both | plugin + compiler + runtime libs |
| [hoot-libs-bundle][libs-bundle]  | library bundle | both | compiled hoot classes + runtime libs |

#### Bundled Libraries

Notice the last two projects listed above.
These are the bundled libraries that are hosted in [GitHub][hub-bundles].
Also, notice the Spring Boot application project listed above.

To simplify library dependencies in [other projects][eco-depot], it was decided to bundle the Hoot
libraries resulting from the Maven build process.
Two scenarios are most often used:
1. compiling Hoot Smalltalk source code + associated test code (if present), and
2. running the resultant applications.

Compiling Hoot Smalltalk code needs the Hoot Smalltalk compiler with its associated Maven plugin
and support libraries. This scenario needs the [hoot-maven-plugin](#hoot-compiler-plugin)
and [hoot-compiler-boot](#hoot-smalltalk-compiler).
Running a resulting [application][console-apps] needs the Hoot Smalltalk libraries and supporting runtime libraries.
This scenario uses the [hoot-libs-bundle][libs-bundle].

The [hoot-maven-plugin](#hoot-compiler-plugin) runs the Hoot compiler from the command line as a sub-task using
the [hoot-compiler-boot][hoot-compiler-boot] Spring Boot application to launch the compiler.
This mimics what you would do to run the compiler from the command line.

GitHub provides a package registry for hosting Maven artifacts.
The bundles and compiler plugin are hosted in the [package registry][hub-bundles] for this repository.
However, the GitHub package registry does not yet support anonymous access from an external Maven build.
So, the project artifacts are also hosted in a [Cloudsmith repository][cloud-repo].

#### Build and Coverage Pipelines

The project build process is driven by a set of [shell scripts](shell/README.md#shell-scripts).
During development, it was discovered the Maven builds were a bit compute hungry
and really run _much_ faster with more than one core.
Fortunately, GitHub supports builds using [**macOS** with 3 cores][hub-runners] in its workflows.

```
runs-on: macos-latest
```

After reviewing and using some alternatives for hosting the build, [GitHub Actions][hub-build]
was chosen to host the Hoot Smalltalk [build pipeline][hub-pipe].
Thereafter, the [test coverage reports][hub-coverage] were hosted in GitHub with Pages.

#### Hoot Smalltalk Compiler

The Hoot Smalltalk compiler was built with [ANTLR 4][antlr] and [StringTemplate][st].
This project uses [ANTLR][antlr] to generate the Hoot Smalltalk parser from [its grammar][grammar].
Many thanks to [Terrence Parr][antlr-parr] and his team for their passion about language translation!

The Hoot Smalltalk compiler accepts directions with a command line interface [CLI][usage], that it uses to invoke the parser.
The parser recognizes the code in Hoot Smalltalk `.hoot` source files, and builds trees of [StringTemplate][st] ST instances.
The ST instances then use [code generation templates][code-lib] to output the corresponding Java source code.

Overall, the Hoot Smalltalk compiler performs source-to-source translation (trans-coding) from Hoot Smalltalk to Java.
Then, the Maven tooling uses a standard Java compiler **javac** to translate the intermediate Java sources
into class files for the Java Virtual Machine **JVM** runtime.

Maven helps with all this by packaging the generated class files into Java archive JAR files and providing a
[build life-cycle][life-cycle] with supportive tools.
For ease of use and reference, the Hoot Smalltalk compiler and its associated support runtime classes get
bundled into a single JAR, [hoot-compiler-bundle][hoot-bundle].
The Hoot project also provides a [Maven plugin](#hoot-compiler-plugin) that takes advantage of the Maven build life-cycle.

#### Hoot Compiler Plugin

Some of the included library projects have no Java source under **src/main/java**, only Hoot Smalltalk sources
under **src/main/hoot**.
In cases where a project has Hoot Smalltalk sources, the supplied Hoot [compiler plugin][hoot-maven-plugin]
runs the [Hoot Smalltalk compiler][usage] to generate Java code from the Hoot Smalltalk `.hoot` sources (trans-coding).

While the Hoot Smalltalk [compiler commands][usage] offer several options, it provides some defaults to simplify its use.
This allows the [compiler plugin][hoot-maven-plugin] to mimic what might otherwise be done with a compiler command.

The [compiler plugin][hoot-maven-plugin] instructs the Hoot Smalltalk compiler to output the generated the Java code in
an appropriate folder within the surrounding Maven project, so that it gets compiled by the Java compiler
during the normal Maven [build life-cycle][life-cycle].

Here's an example invocation of the plugin from the **libs-hoot** [configuration][plugin-example].

```xml
<plugin>
    <groupId>hoot-smalltalk</groupId>
    <artifactId>hoot-maven-plugin</artifactId>
    <executions>
        <execution>
            <goals><goal>generate</goal></goals>
        </execution>
    </executions>
</plugin>

```

In this example, the Hoot Smalltalk source code from the [libs-hoot][libs-hoot] project is being translated into Java code
and placed into the proper place in the folder structure in [libs-hoot][libs-hoot].
The plugin also detects whether tests written in Hoot Smalltalk are present in the project under **src/test/hoot**,
and translates those also.

The plugin helps simplify Hoot projects, keeping Hoot Smalltalk source code together with generated project library Java code.
While the plugin provides support for the various compiler [command arguments][usage], it uses some conventions and
knowledge of the surrounding context to simplify its configuration.

#### Source Code Inclusion

In past years, Smalltalk systems most often provided the source code for the included library classes.
This practice allowed for the inspection of the library classes used by downstream software developers.
This kind of transparency has major advantages, as it lets downstream developers look at how the library
classes work, and also to see and learn what cohesive design patterns, code patterns, and naming patterns
all look like.

So, the inclusion of the original library source code has been a long standing tradition in the Smalltalk world,
going all the way back to [Smalltalk-80][smalltalk].
On deeper review, you'll see the Hoot Smalltalk project also provides the source code for all its major parts.
To support this for the library code included in the released JARs, you'll find resource inclusion patterns
like the following in selected **pom.xml** files.

```xml
<build>
    <resources>
        <resource>
            <directory>${project.basedir}/..</directory>
            <targetPath>META-INF</targetPath>
            <includes>
                <include>LICENSE.txt</include>
            </includes>
        </resource>
        <resource>
            <directory>${project.basedir}/src/main/hoot</directory>
            <targetPath>META-INF/hoot-sources</targetPath>
            <includes>
                <include>**/*.hoot</include>
            </includes>
        </resource>
        <resource>
            <directory>${project.basedir}/src/test/hoot</directory>
            <targetPath>META-INF/test-sources</targetPath>
            <includes>
                <include>**/*.hoot</include>
            </includes>
        </resource>
    </resources>

    ...
</build>
```

While such library source inclusion practice is certainly **not required** to use Hoot Smalltalk for _your_
development projects, it remains a possibility. It's _your_ choice.

#### Project Planning

The foregoing discussions are intended to help you think about how to structure _your_ software projects.
It's recommended to use a similar structure: have a separate Maven project for each library you write in
Hoot Smalltalk that folds together with the corresponding Java code and classes that get generated by the compilers.
This way you can take advantage of the natural capabilities of Maven for building and testing your code.

In your project base **pom.xml**, you'll want to include references to the Hoot project bundle repository.

```xml
<repositories>
    <repository>
        <id>hoot-libs</id>
        <url>https://dl.cloudsmith.io/public/educery/hoot-libs/maven/</url>
    </repository>
</repositories>

<pluginRepositories>
    <pluginRepository>
        <id>hoot-libs</id>
        <url>https://dl.cloudsmith.io/public/educery/hoot-libs/maven/</url>
    </pluginRepository>
</pluginRepositories>
```

In your project base **pom.xml**, you'll also want to include the latest version of the bundles.
Mind you, the latest Hoot Smalltalk version will likely change over time.
So, you may need to adjust that, and replace **2021.1206.1258** below with the latest released version of
[Hoot Smalltalk][cloud-repo].

```xml
<properties>
  ...
  <hoot-bundles-version>2021.1206.1258</hoot-bundles-version>
  ...
</properties>
```

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>hoot-smalltalk</groupId>
            <artifactId>hoot-libs-bundle</artifactId>
            <version>${hoot-bundles-version}</version>
        </dependency>
        <dependency>
            <groupId>hoot-smalltalk</groupId>
            <artifactId>hoot-compiler-bundle</artifactId>
            <version>${hoot-bundles-version}</version>
        </dependency>
    </dependencies>
</dependencyManagement>

```

And, you'll want to mention the latest version of the compiler plugin.

```xml
<build>
    <pluginManagement>
        <plugins>
            <plugin>
                <groupId>hoot-smalltalk</groupId>
                <artifactId>hoot-maven-plugin</artifactId>
                <version>${hoot-bundles-version}</version>
                <dependencies>
                    <dependency>
                        <groupId>org.eclipse.aether</groupId>
                        <artifactId>aether-transport-file</artifactId>
                        <version>1.1.0</version>
                    </dependency>
                    <dependency>
                        <groupId>org.eclipse.aether</groupId>
                        <artifactId>aether-transport-http</artifactId>
                        <version>1.1.0</version>
                    </dependency>
                    <dependency>
                        <groupId>hoot-smalltalk</groupId>
                        <artifactId>hoot-compiler-boot</artifactId>
                        <version>${hoot-bundles-version}</version>
                    </dependency>
                </dependencies>
            </plugin>
        </plugins>
    </pluginManagement>
</build>

```

With these tooling dependencies, you'll be able to make use of the compiler plugin in each module project.

```xml
<plugin>
    <groupId>hoot-smalltalk</groupId>
    <artifactId>hoot-maven-plugin</artifactId>
    <executions>
        <execution>
            <goals><goal>generate</goal></goals>
        </execution>
    </executions>
</plugin>

```

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

#### Features ####

Many of the following features were originally developed in the context of [Bistro Smalltalk][bistro].
However, Hoot Smalltalk provides several improvements over Bistro.
For example, Hoot Smalltalk has a uniform model for annotations and for the various language-specific
code decorations, e.g., `static`, `public`, `private`, etc.
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

| **Back** | **Up** | **Next** |
| -------- | ------ | -------- |
| ... | ... | [Language Model](hoot-design/model.md#language-model) |

```
Copyright 2010,2021 Nikolas S Boyd.
Permission is granted to copy this work provided this copyright statement is retained in all copies.
```
See https://github.com/nikboyd/hoot-smalltalk/blob/main/LICENSE.txt for LICENSE details.


[logo]: hoot-design/hoot-owl.svg "Hoot Owl"

[bistro]: https://bitbucket.org/nik_boyd/bistro-smalltalk/ "Bistro"
[smalltalk]: https://en.wikipedia.org/wiki/Smalltalk "Smalltalk"
[st-syntax]: https://en.wikipedia.org/wiki/Smalltalk#Syntax "Smalltalk Syntax"
[st-imps]: https://en.wikipedia.org/wiki/Smalltalk#List_of_implementations "Smalltalk Implementations"
[eco-depot]: https://github.com/nikboyd/eco-depot#eco-depot-hazmat-facility-conceptual-model

[jdk8]: https://openjdk.java.net/projects/jdk8/
[jdk11]: https://openjdk.java.net/projects/jdk/11/
[java-lts]: https://www.oracle.com/technetwork/java/java-se-support-roadmap.html
[java]: https://en.wikipedia.org/wiki/Java_%28programming_language%29 "Java"
[jvm]: https://en.wikipedia.org/wiki/Java_virtual_machine "Java Virtual Machine"
[lambdas]: https://www.oracle.com/webfolder/technetwork/tutorials/obe/java/Lambda-QuickStart/index.html
[inference]: https://developer.oracle.com/java/jdk-10-local-variable-type-inference
[graal-vm]: https://www.graalvm.org/docs/introduction/
[graal-install]: https://github.com/graalvm/graalvm-ce-builds/releases/tag/vm-21.0.0.2
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

[grammar]: hoot-compiler/src/main/antlr4/Hoot/Compiler/Hoot.g4
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
[console-apps]: hoot-design/tests.md#running-applications
[hoot-dotnet]: hoot-design/dotnet.md#running-hoot-smalltalk-on-net "Dot Net"

[hoot-abstracts]: hoot-abstracts/README.md#hoot-abstractions
[hoot-runtime]: hoot-runtime/README.md#hoot-runtime-library
[hoot-compiler]: hoot-compiler/README.md#hoot-compiler-library
[hoot-compiler-boot]: hoot-compiler-boot/README.md#hoot-compiler-boot
[hoot-maven-plugin]: hoot-maven-plugin/README.md#hoot-maven-plugin
[libs-hoot]: libs-hoot/README.md#hoot-class-library
[hoot-tests]: libs-hoot/src/test/hoot/Hoot/Tests
[lib]: libs-smalltalk/README.md#hoot-smalltalk-type-library
[libs-smalltalk]: libs-smalltalk/README.md#hoot-smalltalk-type-library
[hoot-bundle]: hoot-compiler-bundle/README.md
[libs-bundle]: hoot-libs-bundle/README.md
[plugin-example]: libs-hoot/pom.xml#L44
[java-profiles]: pom.xml#L248

[cloud-repo]: https://cloudsmith.io/~educery/repos/hoot-libs/packages/
[cloud-build]: https://cloud.google.com/cloud-build
[cloud-smith]: https://cloudsmith.com/

[hub-package]: https://github.com/nikboyd/hoot-smalltalk/packages/1130290
[hub-coverage]: https://nikboyd.github.io/hoot-smalltalk/
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

[maven-badge]: https://nikboyd.github.io/hoot-smalltalk/hoot-runtime/maven_badge.png
[runtime-coverage]: https://nikboyd.github.io/hoot-smalltalk/hoot-runtime/coverage_badge.png
[compiler-coverage]: https://nikboyd.github.io/hoot-smalltalk/hoot-compiler/coverage_badge.png
[plugin-coverage]: https://nikboyd.github.io/hoot-smalltalk/hoot-maven-plugin/coverage_badge.png
[libs-coverage]: https://nikboyd.github.io/hoot-smalltalk/libs-hoot/coverage_badge.png
