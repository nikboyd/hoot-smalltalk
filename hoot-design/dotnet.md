#### Running Hoot Smalltalk on .Net ####

Provisional support for running Hoot Smalltalk on the [.Net][dot-net] [CLR][clr] platform requires
[Mono][mono-home] and [IKVM][ikvm-home].

#### Tool Installation

[IKVM][ikvm-home] v8 requires Java 1.8. So, be sure you're working with the latest release of that version of Java
(which Hoot Smalltalk still supports).
In additional to the required [Java 1.8 tools](../#platform-requirements), you'll also need to install
[Mono][mono-install] (latest) and [IKVM 8.5.0.2][ikvm-works].
When installing Mono, make sure you have whatever prerequisites it needs (if any) for your selected OS.

While there have been multiple releases of [IKVM][ikvm-releases], it's recommended to install [v8.5.0.2][ikvm-works].
Download the released ZIP file, extract its contents in your preferred location.
I use **ikvm-home** (as shown below).
The process of running the Hoot Smalltalk examples has only been verified to work with [IKVM 8.5.0.2][ikvm-works].

#### Converting and Running Hoot Smalltalk Examples

Once you've installed the foregoing tools (and tested them), you'll want to use the **IKVMC** compiler to convert
the Hoot Smalltalk examples JAR to a .Net executable file, and copy the DLLs from IKVM into the same folder containing
the resulting executable.

To run the IKVMC compiler, it's recommended you map it to resemble a command, perhaps with an alias (like the following).

```
alias ikvmc="mono ~/ikvm-home/ikvmc.exe"
```

Notice in the above alias that IKVMC needs and gets executed with MONO.
Navigate to the **libs-hoot/target** folder, which contains the **hoot-exec.jar** file that resulted from
[building Hoot Smalltalk](../#building-from-sources). Compile it to an EXE with IKVMC.

```
cd hoot-smalltalk/hoot-libs-bundle/target
ikvmc -nowarn:0100 -out:hoot-exec.exe hoot-libs-bundle-2020.0101.0101.jar
```

Please note that this would otherwise report several _warnings_ for those classes that IKVMC can't locate.
However, those don't pose any problems for running the examples, so we ignore them with **nowarn**.

Once you've compiled it, copy the IKVM runtime DLLs into the **/target** folder along with the generated EXE file.
On an Ubuntu host, this looks like:

```
cp ~/ikvm-home/*.dll .
```

Once you have these DLL files in the same folder as the EXE, you should be able to run an example with Mono.

```
mono hoot-exec.exe Hoot.Tests.HelloWorld
```

The former command will run the HelloWorld test.
You can also run other individual tests using a fully qualified class name.

```
mono hoot-exec.exe Hoot.Tests.HanoiTower
mono hoot-exec.exe Hoot.Tests.SticBenchmark
mono hoot-exec.exe Hoot.Tests.FileStream
mono hoot-exec.exe Hoot.Tests.TextStream
... etc ...
```

| **NEXT** | **BACK** | **UP** |
| -------- | -------- | ------ |
| <p align="center">[Tools Used][tools]</p><img width="250" height="1" />  | <p align="center">[Build][build]</p><img width="250" height="1" /> | <p align="center">[Features][features]</p><img width="250" height="1" />  |

```
Copyright 2010,2025 Nikolas S Boyd. Permission is granted to copy this work 
provided this copyright statement is retained in all copies.
```

[features]: README.md#features
[build]: build.md#building-from-sources "Build"
[tools]: tools.md#tool-integration "Tools"

[dot-net-install]: https://docs.microsoft.com/en-us/dotnet/core/tools/dotnet-install-script
[dot-net]: https://en.wikipedia.org/wiki/.NET_Framework
[clr]: https://en.wikipedia.org/wiki/Common_Language_Runtime "Common Language Runtime"
[mono-home]: https://www.mono-project.com/
[mono-install]: https://www.mono-project.com/docs/getting-started/install/
[ikvm-home]: http://www.ikvm.net/
[ikvm-releases]: https://github.com/wwrd/ikvm8/releases
[ikvm-works]: https://github.com/wwrd/ikvm8/releases/tag/8.5.0.2
