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
development projects, it remains a possibility. 
It's _your_ choice whether or not to make your code _visible_ and work _**in the open**_.

| **NEXT** | **BACK** | **UP** |
| -------- | -------- | ------ |
| <p align="center">[Compiler][compiler]</p><img width="250" height="1" /> | <p align="center">[Tools][tools]</p><img width="250" height="1" />  | <p align="center">[Features][features]</p><img width="250" height="1" />  |

```
Copyright 2010,2025 Nikolas S Boyd. Permission is granted to copy this work 
provided this copyright statement is retained in all copies.
```


[smalltalk]: https://en.wikipedia.org/wiki/Smalltalk "Smalltalk"

[features]: README.md#features
[build]: build.md#building-from-sources "Build"
[compiler]: tools.md#hoot-smalltalk-compiler "Compiler"
[tools]: tools.md#tool-integration "Tools"

