#### Hoot Compiler Usage ####

The [Hoot Smalltalk compiler plugin](../hoot-maven-plugin#hoot-maven-plugin) is now the preferred way to use
the Hoot Smalltalk compiler.
Please refer to those notes. The remainder of this page provides a context for those notes.

The Hoot Smalltalk compiler has a command line interface CLI.

```shell
java -jar hoot-compiler-boot/target/hoot-compiler-boot*.jar --help
```

... from the project base folder after a build produces the following:

```
usage: hoot-compiler
 -b,--base <basePath>           optional: base folder path, assumes 'user.dir' value
 -f,--folder <targetPath>       required: target Java folder path
 -h,--help                      optional: displays this help
 -o,--only-test                 optional: only tests compile arguments
 -p,--packages <packageNames>   optional: packages to compile, none = all
 -s,--source <sourcePath>       optional: Hoot Smalltalk sources folder path
 -t,--test-source <sourcePath>  optional: Hoot Smalltalk test sources folder path
```

| **Argument** | **Description** | **Default** |
| ------------ | --------------- | ----------- |
| basePath     | overall base folder | the current working directory (from which command is run) |
| sourcePath   | the folder relative to which Hoot Smalltalk packages will be read   | **basePath**/src/main/hoot |
| targetPath   | the folder relative to which Java code will be written    | **basePath**/target/generated-sources |
| packageNames | names of the packages under **sourcePath** to be compiled | all packages under **sourcePath** |

#### Notes

The Hoot Smalltalk compiler is a trans-coding compiler.
It converts Hoot Smalltalk source code into Java source code, which then gets compiled by the Java compiler.

However, it presumes a tooling context: [Maven][maven], and folders structured according to Maven's organization conventions.
Conforming to these conventions, the Hoot Smalltalk compiler locates its sources and Java target folders conveniently for Maven.
However, the defaults can be overridden.

Note the generated Java code prefers being written under a project **/target** folder.
This provides better integration with the Maven build life-cycle, esp. the ability to **clean** before building.
The ability to **clean** before a build provides a convenient way to regenerate a library from its Hoot Smalltalk sources.
Thereafter, Maven compiles the generated Java code, and packages the resulting class files into a JAR.


```
Copyright 2010,2023 Nikolas S Boyd.
Permission is granted to copy this work provided this copyright statement is retained in all copies.
```


[maven]: https://maven.apache.org/
