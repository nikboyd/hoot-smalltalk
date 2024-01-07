### Hoot Smalltalk

<table border="0" width="100%">
<tr>
<td>
<div align="left" >
<img align="left" src="logo.png" width="150" />
</div>
</td>
<td>

[Hoot Smalltalk](#hoot-smalltalk) is a variation of the original [Smalltalk][smalltalk].

* Hoot is [_experimental_][intro]. 
* Hoot runs Smalltalk on a Java [virtual machine][jvm].
* Hoot integrates better Smalltalk and Java [features][features].

each badge below links to some test reports:
<br/>

<div align="left" >
<a href="https://hoot-docs-host-drm7kw4jza-uw.a.run.app/">
<img src="https://hoot-docs-host-drm7kw4jza-uw.a.run.app/hoot-runtime/maven_badge.png" />
</a>
</div>

<div align="left" >
<a href="https://hoot-docs-host-drm7kw4jza-uw.a.run.app/libs-hoot/index.html">
<img src="https://hoot-docs-host-drm7kw4jza-uw.a.run.app/libs-hoot/coverage_badge.png" />
</a>
<a href="https://hoot-docs-host-drm7kw4jza-uw.a.run.app/hoot-runtime/index.html">
<img src="https://hoot-docs-host-drm7kw4jza-uw.a.run.app/hoot-runtime/coverage_badge.png" />
</a>
<br/>
<a href="https://hoot-docs-host-drm7kw4jza-uw.a.run.app/hoot-plugin/index.html">
<img src="https://hoot-docs-host-drm7kw4jza-uw.a.run.app/hoot-plugin/coverage_badge.png" />
</a>
<a href="https://hoot-docs-host-drm7kw4jza-uw.a.run.app/hoot-compiler/index.html">
<img src="https://hoot-docs-host-drm7kw4jza-uw.a.run.app/hoot-compiler/coverage_badge.png" />
</a>
</div>
</td>
</tr>
</table>
<br/>

| **NEXT** | **BACK** | **DOWN** |
| -------- | -------- | ------ |
|  <p align="center">[Introduction][intro]</p><img width="250" height="1" /> | <p align="center">...</p><img width="250" height="1" />  | <p align="center">[Overview](hoot-design/README.md)</p><img width="250" height="1" />  |


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
[hoot-ansi]: hoot-design/ANSI-X3J20-1.9.pdf
[squeak-ansi]: http://wiki.squeak.org/squeak/172
[st-ansi]: https://web.archive.org/web/20060216073334/http://www.smalltalk.org/versions/ANSIStandardSmalltalk.html
[st-image]: https://en.wikipedia.org/wiki/Smalltalk#Image-based_persistence
[version-control]: https://en.wikipedia.org/wiki/Version_control#Overview

[grammar]: hoot-compiler-ast/src/main/antlr4/Hoot/Compiler/Parser/Hoot.g4
[code-lib]: hoot-compiler/src/main/resources/CodeTemplates.stg

[design]: hoot-design/README.md#hoot-smalltalk-design-notes
[features]: hoot-design/README.md#features
[intro]: hoot-design/intro.md#introduction "Intro"
[build]: hoot-design/build.md#building-from-sources "Build"
[tool-needs]: hoot-design/build.md#tools-needed "Tools Needed"
[structure]: hoot-design/structure.md#project-structure "Structure"
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
[compiler-tool]: hoot-design/tools.md#hoot-smalltalk-compiler "Compiler"
[plugin-tool]: hoot-design/tools.md#hoot-compiler-plugin "Plugin"
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
