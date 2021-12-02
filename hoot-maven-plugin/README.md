### Hoot Maven Plugin

Contains the Hoot Smalltalk compiler plugin for Maven.

| **Package** | **Contents** |
| ----------- | ------------ |
| Hoot.Compiler.Mojo | **HootMojo** runs the Hoot Smalltalk compiler |
| Test.Compiler.Mojo | **MojoTest** tests the Hoot Smalltalk compiler plugin |

#### Preparations for Use

Please include the following sections in your base project **pom.xml**.

```xml
<repositories>
    <repository>
        <id>hoot-libs</id>
        <url>https://maven.cloudsmith.io/educery/hoot-libs/</url>
    </repository>
</repositories>

<pluginRepositories>
    <pluginRepository>
        <id>hoot-libs</id>
        <url>https://maven.cloudsmith.io/educery/hoot-libs/</url>
    </pluginRepository>
</pluginRepositories>
```

In your project base **pom.xml**, you'll also want to include the latest version of the bundles.
Mind you, the latest Hoot Smalltalk version will likely change over time. So, you may want to update that.

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>hoot-smalltalk</groupId>
            <artifactId>hoot-libs-bundle</artifactId>
            <version>2021.0203.1240</version>
        </dependency>
        <dependency>
            <groupId>hoot-smalltalk</groupId>
            <artifactId>hoot-compiler-bundle</artifactId>
            <version>2021.0203.1240</version>
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
                <version>2021.0203.1240</version>
                <dependencies>
                    <dependency>
                        <groupId>hoot-smalltalk</groupId>
                        <artifactId>hoot-compiler-bundle</artifactId>
                        <version>2021.0203.1240</version>
                    </dependency>
                </dependencies>
            </plugin>
        </plugins>
    </pluginManagement>
</build>
```

#### Plugin Configuration

The Maven [build life-cycle][life-cycle] supports code generation as part of its standard process
with the **generate-sources** phase.
The Hoot Smalltalk project includes a plugin that takes advantage of the **generate-sources** phase.

When used in a Maven project, this plugin invokes the Hoot Smalltalk compiler passing it appropriate arguments.
Here's an example:

```xml
<plugin>
    <groupId>hoot-smalltalk</groupId>
    <artifactId>hoot-maven-plugin</artifactId>
    <configuration>
        <main-args>
            <source>../hoot-compiler/src/test/hoot</source>
            <folder>../sample-code</folder>
            <test>true</test>
        </main-args>
        <packages>
            <package>Samples.Behaviors</package>
            <package>Samples.Core</package>
            <package>Samples.Magnitudes</package>
            <package>Samples.Geometry</package>
        </packages>
    </configuration>
    <executions>
        <execution>
            <goals><goal>generate</goal></goals>
        </execution>
    </executions>
</plugin>
```

#### Simplified Configuration

While the plugin can be configured as shown above, it also supports some [conventions](#hoot-conventions)
that simplify its configuration.
Here's a more representative example from the **libs-hoot** configuration.

```xml
<groupId>hoot-smalltalk</groupId>
<artifactId>hoot-maven-plugin</artifactId>
<executions>
    <execution>
        <goals><goal>generate</goal></goals>
    </execution>
</executions>
```

Given it's configured within a project, the plugin knows where the project is located in the file system.
This allows it to locate the folders the compiler needs.

#### Hoot Conventions

The Hoot Smalltalk compiler uses some conventions for the folders it needs to locate Hoot Smalltalk sources and generate Java code.
Given it knows a library project folder and the project has its Hoot Smalltalk code located in the expected places,
the compiler will figure out the rest.

| **path** | **default** | **description** |
| -------- | ----------- | --------------- |
| base | **{libBase}** | a given library folder, under a Maven project base folder {user.dir} |
| source | **{libBase}/src/main/hoot** | under which are located Hoot Smalltalk packages with Hoot Smalltalk code |
| target | **{libBase}/target/generated-sources** | under which the compiler generates Java code |
| test-source | **{libBase}/src/test/hoot** | under which Hoot Smalltalk test code may be located |
| test-target | **{libBase}/target/generated-test-sources** | under which the compiler generates Java test code |

These various locations can be overridden, but sticking to the conventions helps simplify project structure.
The compiler can use relative paths to locate the **source** and target **folder** for each library project.
The compiler will look for Hoot Smalltalk sources under **src/main/hoot** by default.
The plugin will look under **src/test/hoot** for Hoot Smalltalk test sources by default.

#### Hoot Compiler Support

In the first of the foregoing plugin [configurations](#plugin-configuration), there are two primary
sections: **main-args** and **packages**.
You can use the **packages** section to list those packages under the **source** folder you want compiled.
Alternatively, you can simply specify:

```xml
<package>*</package>
```

... which will instruct the plugin to compile **all** the packages that appear under the **source** folder.
Or, you can simply remove the **packages** section to accomplish the same thing.

The plugin passes the values from **main-args** to the Hoot Smalltalk compiler.

| **main-args** | **type** | **compiler argument** | **default** |
| ------------- | -------- | --------------------- | ----------- |
| folder | path | path to the Java folder under which code will be generated | {libBase}/target/generated-sources |
| source | path | path to the library Hoot sources | {libBase}/src/main/hoot |
| test   | bool | `true` indicates you only want to test the compiler | `false` |
| help   | bool | `true` indicates you want to show the compiler command help | `false` |


```
Copyright 2010,2021 Nikolas S Boyd.
Permission is granted to copy this work provided this copyright statement is retained in all copies.
```


[life-cycle]: https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html

