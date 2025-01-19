### Hoot Smalltalk Type Library

Contains Hoot Smalltalk code for the standard Smalltalk protocols located in [src/main/hoot](src/main/hoot).
This project invokes the Hoot Smalltalk [compiler plugin][hoot-maven-plugin] to generate its equivalent Java code during
the build process.

| **Package** | **Contents** |
| ----------- | ------------ |
| Smalltalk Blocks      | standard closures and predicates |
| Smalltalk Collections | standard collection protocols |
| Smalltalk Core        | standard behavior protocols |
| Smalltalk Exceptions  | standard exception protocols |
| Smalltalk Magnitudes  | standard value protocols |
| Smalltalk Streams     | standard stream protocols |

#### Notes

No surprise: some names are quite popular and descriptive for commonly found types.
To reduce naming conflicts and keep some of its class references simple,
Hoot Smalltalk maps some of the standard Smalltalk protocols to type names _different_ from those
found in the [ANSI standard][st-ansi].

This applies especially to those types whose names also have potential conflicts
with some found in the underlying Java type library.
These renamed types are emphasized in the table below with **bold**.

To simplify its implementation of the valuable protocols, **Hoot Runtime Blocks Enclosure** implements **all** the
valuable protocols defined in section 5.4 of the standard.
Also notice that **Transcript** becomes a class with **log** as its sole instance, so that the associated protocol
can be instantiated.

Many of the exception types and collection types defined in the standard are mapped directly to classes in Hoot Smalltalk,
especially those whose names are called out in the **Standard Globals** (5.2).

The method **Collected.isEmpty** in the collection protocols 5.7.1 required special treatment because of the 
native Java **String.isEmpty** method.
The native Java method has a primitive **boolean** result that cannot be replaced by **Smalltalk Core Posit**.

This imposed pervasive changes in the Smalltalk code to use **_isEmpty** instead. 
So, the compiler recognizes when the special selector **isEmpty** appears in Smalltalk code, 
translates that with a leading underscore, and replaces its usage as appropriate.
The Hoot library code followed suit where appropriate.

| **Section** | **ANSI Smalltalk** | **Hoot Smalltalk** |
| ----------- | ------------------ | ------------------ |
| _**5.3.1**_ | Object | Smalltalk Core [Subject][subject] |
| 5.3.2 | nil | Smalltalk Core [Undefined][undefined] (Nil) |
| 5.3.3 | boolean | Smalltalk Core [Posit][posit] (Boolean) |
| 5.3.4 | Character | Smalltalk Magnitudes [Code][code] (Character) |
| 5.3.5 | Character factory | Smalltalk Magnitudes [Code][code] type (Character type) |
| 5.3.6 | failedMessage | Smalltalk Core [SentMessage][sent-message] (Message) |
| 5.3.7 | selector | Hoot Runtime Faces [Selector][selector] (Symbol) |
| 5.3.8 | classDescription | Smalltalk Core [Classified][classified] (Behavior) |
| 5.3.9 | instantiator | Smalltalk Core [Subject][subject] type |
| 5.3.10 | Object class | Hoot Behaviors [Object][object] class |
| _**5.4.1**_ | valuable | Hoot Runtime Blocks [Valuable][valuable] + [Arguable][arguable] |
| 5.4.2 | niladicValuable | Hoot Runtime Blocks [NiladicValuable][niladic-valuable] <- Valuable |
| 5.4.3 | niladicBlock | Hoot Runtime Blocks [Enclosure][enclosure] <- NiladicValuable |
| 5.4.4 | monadicValuable | Hoot Runtime Blocks [MonadicValuable][monadic-valuable] + Arguable |
| 5.4.5 | monadicBlock | Hoot Runtime Blocks [Enclosure][enclosure] <- MonadicValuable |
| 5.4.6 | dyadicValuable | Hoot Runtime Blocks [DyadicValuable][dyadic-valuable] + Arguable |
| _**5.5.1**_ | exceptionDescription | Smalltalk Exceptions [ExceptionDescription][ex-description] |
| 5.5.2 | exceptionSignaler | Smalltalk Exceptions [ExceptionSignaler][ex-signaler] |
| 5.5.3 | exceptionBuilder | Smalltalk Exceptions [ExceptionBuilder][ex-builder] |
| 5.5.4 | signaledException | Smalltalk Exceptions [SignaledException][signaled-ex] |
| 5.5.5 | exceptionSelector | Smalltalk Exceptions [ExceptionSelector][ex-selector] |
| 5.5.6 | exceptionInstantiator | Hoot Exceptions [Exception][exception] class |
| 5.5.7 | Exception class | Hoot Exceptions [Exception][exception] class |
| 5.5.8 | Exception | Hoot Exceptions [Exception][exception] |
| 5.5.9 | Notification class | Hoot Exceptions [Notification][notification] class |
| 5.5.10 | Notification | Hoot Exceptions [Notification][notification] |
| 5.5.11 | Warning class | Hoot Exceptions [Warning][warning] class |
| 5.5.12 | Warning | Hoot Exceptions [Warning][warning] |
| 5.5.13 | Error class | Hoot Exceptions [Error][error] class |
| 5.5.14 | Error | Hoot Exceptions [Error][error] |
| 5.5.15 | ZeroDivide factory | Hoot Exceptions [ZeroDivide][zero-divide] class |
| 5.5.16 | ZeroDivide | Hoot Exceptions [ZeroDivide][zero-divide] |
| 5.5.17 | MessageNotUnderstoodSelector | _not used_ |
| 5.5.18 | MessageNotUnderstood | Hoot Exceptions [MessageNotUnderstood][not-understood] |
| 5.5.19 | exceptionSet | Hoot Exceptions [ExceptionSet][exception-set] |
| _**5.6.1**_ | magnitude | Smalltalk Magnitudes [Scalar][scalar] |
| 5.6.2 | number | Smalltalk Magnitudes [Numeric][numeric] |
| 5.6.3 | rational | Smalltalk Magnitudes [Ratio][ratio] |
| 5.6.4 | Fraction | Smalltalk Magnitudes [Fractional][fractional] |
| 5.6.5 | integer | Smalltalk Magnitudes [Ordinal][ordinal] |
| 5.6.6 | scaledDecimal | Smalltalk Magnitudes [ScaledDecimal][scaled] |
| 5.6.7 | Float | Smalltalk Magnitudes [Floater][floater] |
| 5.6.8 | floatCharacterization | Smalltalk Magnitudes [Floater][floater] type |
| 5.6.9 | Fraction factory | Smalltalk Magnitudes [Fractional][fractional] type |
| _**5.7.1**_ | collection | Smalltalk Collections [Collected][collected] |
| 5.7.2 | abstractDictionary | Smalltalk Collections [CollectedDictionary][collected-dic] |
| 5.7.3 | Dictionary | Hoot Collections [Dictionary][dictionary] |
| 5.7.4 | IdentityDictionary | Hoot Collections [IdentityDictionary][id-dictionary] |
| 5.7.5 | extensibleCollection | Smalltalk Collections [CollectedVariably][variable] |
| 5.7.6 | Bag | Smalltalk Collections [CollectedBaggage][baggage] |
| 5.7.7 | Set | Smalltalk Collections [CollectedDistinctly][distinct] |
| 5.7.8 | sequencedReadableCollection | Smalltalk Collections [CollectedSequentially][sequential] |
| 5.7.9 | Interval | Smalltalk Collections [OrdinalRange][ordinal-range] |
| 5.7.10 | readableString | Smalltalk Collections [ReadableString][readable-string] |
| 5.7.11 | symbol | Hoot Collections [Symbol][symbol] |
| 5.7.12 | sequencedCollection | Smalltalk Collections [CollectedSequentially][sequential] |
| 5.7.13 | String | Smalltalk Collections [MutableString][mutable-string] |
| 5.7.14 | Array | Smalltalk Collections [CollectedArray][collected-array] |
| 5.7.15 | ByteArray | Hoot Collections [ByteArray][byte-array] |
| 5.7.16 | sequencedContractibleCollection | Smalltalk Collections [CollectedCollapsibly][collapsible] |
| 5.7.17 | SortedCollection | Smalltalk Collections [CollectedSortably][sortable] |
| 5.7.18 | OrderedCollection | Smalltalk Collections [CollectedOrdinally][collected-ord] |
| 5.7.19 | Interval factory | Smalltalk Collections [OrdinalRange][ordinal-range] type |
| 5.7.20 | collection factory | Smalltalk Collections [Collected][collected] type |
| 5.7.21 | Dictionary factory | Smalltalk Collections [CollectedDictionary][collected-dic] type |
| 5.7.22 | IdentityDictionary factory | Hoot Collections [IdentityDictionary][id-dictionary] class |
| 5.7.23 | initializableCollection factory | Smalltalk Collections [Collected][collected] type |
| 5.7.24 | Array factory | Smalltalk Collections [CollectedArray][collected-array] type |
| 5.7.25 | Bag factory | Smalltalk Collections [CollectedBaggage][baggage] type |
| 5.7.26 | ByteArray factory | Hoot Collections [ByteArray][byte-array] class |
| 5.7.27 | OrderedCollection factory | Smalltalk Collections [CollectedOrdinally][collected-ord] type |
| 5.7.28 | Set factory | Smalltalk Collections [CollectedDistinctly][distinct] type |
| 5.7.29 | SortedCollection factory | Smalltalk Collections [CollectedSortably][sortable] type |
| 5.7.30 | String factory | Hoot Collections [String][string] class |
| _**5.9.1**_ | sequencedStream | Smalltalk Streams [StreamedSequence][stream-sequence] |
| 5.9.2 | gettableStream | Smalltalk Streams [StreamedSource][stream-source] |
| 5.9.3 | collectionStream | Hoot Streams [CollectionStream][collect-stream] |
| 5.9.4 | puttableStream | Smalltalk Streams [StreamedSink][stream-sink] |
| 5.9.5 | ReadStream | Hoot Streams [ReadStream][read-stream] |
| 5.9.6 | WriteStream | Hoot Streams [WriteStream][write-stream] |
| 5.9.7 | ReadWriteStream | Smalltalk Streams [StreamStore][stream-store] |
| 5.9.8 | Transcript | Hoot Streams [Transcript][transcript] log |
| 5.9.9 | ReadStream factory | Hoot Streams [ReadStream][read-stream] class |
| 5.9.10 | ReadWriteStream factory | Smalltalk Streams [StreamStore][stream-store] type |
| 5.9.11 | WriteStream factory | Hoot Streams [WriteStream][write-stream] class |
| 5.10.1 | FileStream | Hoot Streams [FileStream][file-stream] |
| 5.10.2 | readFileStream | Smalltalk Streams [FileStreamReader][stream-reader] |
| 5.10.3 | writeFileStream | Smalltalk Streams [FileStreamWriter][stream-writer] |
| 5.10.4 | FileStream factory | Hoot Streams [FileStream][file-stream] class |

```
Copyright 2010,2025 Nikolas S Boyd. Permission is granted to copy this work 
provided this copyright statement is retained in all copies.
```

[st-ansi]: https://web.archive.org/web/20060216073334/http://www.smalltalk.org/versions/ANSIStandardSmalltalk.html
[hoot-maven-plugin]: ../hoot-maven-plugin/README.md#hoot-maven-plugin

[object]: ../libs-hoot/src/main/hoot/Hoot/Behaviors/Object.hoot#L19
[subject]: src/main/hoot/Smalltalk/Core/Subject.hoot#L10
[undefined]: src/main/hoot/Smalltalk/Core/Undefined.hoot#L5
[classified]: src/main/hoot/Smalltalk/Core/Classified.hoot#L11
[posit]: src/main/hoot/Smalltalk/Core/Posit.hoot#L5
[code]: src/main/hoot/Smalltalk/Magnitudes/Code.hoot#L7
[selector]: ../hoot-abstracts/src/main/java/Hoot/Runtime/Faces/Selector.java#L14
[sent-message]: src/main/hoot/Smalltalk/Core/SentMessage.hoot#L7

[arguable]: ../hoot-abstracts/src/main/java/Hoot/Runtime/Blocks/Arguable.java#L13
[valuable]: ../hoot-abstracts/src/main/java/Hoot/Runtime/Blocks/Valuable.java#L14
[niladic-valuable]: ../hoot-abstracts/src/main/java/Hoot/Runtime/Blocks/NiladicValuable.java#L13
[monadic-valuable]: ../hoot-abstracts/src/main/java/Hoot/Runtime/Blocks/MonadicValuable.java#L12
[dyadic-valuable]: ../hoot-abstracts/src/main/java/Hoot/Runtime/Blocks/DyadicValuable.java#L12

[enclosure]: ../hoot-runtime/src/main/java/Hoot/Runtime/Blocks/Enclosure.java#L38
[closure]: src/main/hoot/Smalltalk/Blocks/Closure.hoot#L17

[ex-builder]: src/main/hoot/Smalltalk/Exceptions/ExceptionBuilder.hoot#L8
[ex-description]: src/main/hoot/Smalltalk/Exceptions/ExceptionDescription.hoot#L9
[ex-signaler]: src/main/hoot/Smalltalk/Exceptions/ExceptionSignaler.hoot#L8
[ex-selector]: src/main/hoot/Smalltalk/Exceptions/ExceptionSelector.hoot#L5
[signaled-ex]: src/main/hoot/Smalltalk/Exceptions/SignaledException.hoot#L7

[exception]: ../libs-hoot/src/main/hoot/Hoot/Exceptions/Exception.hoot#L15
[notification]: ../libs-hoot/src/main/hoot/Hoot/Exceptions/Notification.hoot#L6
[zero-divide]: ../libs-hoot/src/main/hoot/Hoot/Exceptions/ZeroDivide.hoot#L7
[warning]: ../libs-hoot/src/main/hoot/Hoot/Exceptions/Warning.hoot#L6
[error]: ../libs-hoot/src/main/hoot/Hoot/Exceptions/Error.hoot#L5
[not-understood]: ../libs-hoot/src/main/hoot/Hoot/Exceptions/MessageNotUnderstood.hoot#L6
[exception-set]: ../libs-hoot/src/main/hoot/Hoot/Exceptions/ExceptionSet.hoot#L14

[scalar]: src/main/hoot/Smalltalk/Magnitudes/Scalar.hoot#L5
[numeric]: src/main/hoot/Smalltalk/Magnitudes/Numeric.hoot#L7
[ratio]: src/main/hoot/Smalltalk/Magnitudes/Ratio.hoot#L5
[scaled]: src/main/hoot/Smalltalk/Magnitudes/ScaledDecimal.hoot#L5
[ordinal]: src/main/hoot/Smalltalk/Magnitudes/Ordinal.hoot#L10
[floater]: src/main/hoot/Smalltalk/Magnitudes/Floater.hoot#L5
[fractional]: src/main/hoot/Smalltalk/Magnitudes/Fractional.hoot#L5

[collected]: src/main/hoot/Smalltalk/Collections/Collected.hoot#L9
[collected-dic]: src/main/hoot/Smalltalk/Collections/CollectedDictionary.hoot#L9
[collected-ord]: src/main/hoot/Smalltalk/Collections/CollectedOrdinally.hoot#L10
[collected-array]: src/main/hoot/Smalltalk/Collections/CollectedArray.hoot#L6
[baggage]: src/main/hoot/Smalltalk/Collections/CollectedBaggage.hoot#L8
[distinct]: src/main/hoot/Smalltalk/Collections/CollectedDistinctly.hoot#L6
[sequential]: src/main/hoot/Smalltalk/Collections/CollectedSequentially.hoot#L8
[variable]: src/main/hoot/Smalltalk/Collections/CollectedVariably.hoot#L6
[collapsible]: src/main/hoot/Smalltalk/Collections/CollectedCollapsibly.hoot#L8
[sortable]: src/main/hoot/Smalltalk/Collections/CollectedSortably.hoot#L6
[readable-string]: src/main/hoot/Smalltalk/Collections/ReadableString.hoot#L8
[mutable-string]: src/main/hoot/Smalltalk/Collections/MutableString.hoot#L7
[ordinal-range]: src/main/hoot/Smalltalk/Collections/OrdinalRange.hoot#L8

[array]: ../libs-hoot/src/main/hoot/Hoot/Collections/Array.hoot#L15
[id-set]: ../libs-hoot/src/main/hoot/Hoot/Collections/IdentitySet.hoot#L15
[string]: ../libs-hoot/src/main/hoot/Hoot/Collections/String.hoot#L19
[symbol]: ../libs-hoot/src/main/hoot/Hoot/Collections/Symbol.hoot#L11
[byte-array]: ../libs-hoot/src/main/hoot/Hoot/Collections/ByteArray.hoot#L12
[dictionary]: ../libs-hoot/src/main/hoot/Hoot/Collections/Dictionary.hoot#L17
[id-dictionary]: ../libs-hoot/src/main/hoot/Hoot/Collections/IdentityDictionary.hoot#L14

[stream-sequence]: src/main/hoot/Smalltalk/Streams/StreamedSequence.hoot#L8
[stream-source]: src/main/hoot/Smalltalk/Streams/StreamedSource.hoot#L9
[stream-store]: src/main/hoot/Smalltalk/Streams/StreamStore.hoot#L7
[stream-sink]: src/main/hoot/Smalltalk/Streams/StreamedSink.hoot#L7
[stream-reader]: src/main/hoot/Smalltalk/Streams/FileStreamReader.hoot#L9
[stream-writer]: src/main/hoot/Smalltalk/Streams/FileStreamWriter.hoot#L9

[file-stream]: ../libs-hoot/src/main/hoot/Hoot/Streams/FileStream.hoot#L13
[read-stream]: ../libs-hoot/src/main/hoot/Hoot/Streams/ReadStream.hoot#L15
[write-stream]: ../libs-hoot/src/main/hoot/Hoot/Streams/WriteStream.hoot#L13
[collect-stream]: ../libs-hoot/src/main/hoot/Hoot/Streams/CollectionStream.hoot#L14
[transcript]: ../libs-hoot/src/main/hoot/Hoot/Streams/Transcript.hoot#L10
