# Nachbau von `IntStream` und `Stream``

Das Nachbauen von Programmierkonzepten ist ein Zugang, um sich zu verdeutlichen, wie etwas funktioniert.

* [`SimpleIntStream.java`](SimpleIntStream.java) ist ein Nachbau von `java.util.stream.IntStream`
* [`SimpleStream.java`](SimpleStream.java) ist ein Nachbau von `java.util.stream.Stream`

Beim Nachbau habe ich vereinfacht. Die "echten" Streams nutzen das Konzept des Spliterators -- eine besondere Iterator-Variante, die die parallele Verarbeitung in Streams ermöglicht. Wenn man darauf verzichtet und stattdessen einen "gewöhnlichen" Iterator verwendet, vereinfacht sich die Implementierung und das Konzept der Machart und Bauweise von Streams tritt deutlich hervor.

Sie werden feststellen, dass der Anfang eines Streams einen Iterator aufsetzt und alle nachfolgenden, intermediären Stream-Operationen ebenfalls Iteratoren sind, die auf den Iterator der vorigen Stufe zugreifen. So entsteht eine Kette einer _pull_-basierten Verarbeitungsfolge von Iteratoren, die durch eine terminale Operation erst in Gang gesetzt wird und ein Ergebnis produziert.
 
Wenn Sie sich das nachvollziehen möchten:

* Starten Sie mit dem Studium von [`SimpleIntStream.java`](SimpleIntStream.java)
* Lesen Sie dazu [`SimpleIntStream.md`](SimpleIntStream.md)
* Sie werden feststellen, dass danach [`SimpleStream.java`](SimpleStream.java) nicht mehr so schwer zu verstehen ist. Alles, was spezifisch auf Integer ausgelegt war, wird hier generisch ausgelegt.

Wenn Sie mit den Strom-Nachbauten experimentieren wollen, so ist das in der JShell leicht möglich. Sie müssen beide Stromvarianten laden, da sie aufeinander Bezug nehmen.

```
jshell --enable-preview                     
|  Willkommen bei JShell - Version 24
|  Geben Sie für eine Einführung Folgendes ein: /help intro
jshell> /o SimpleIntStream.java

jshell> /o SimpleStream.java

jshell> SimpleIntStream.of(1,2,3).average()
$8 ==> OptionalDouble[2.0]
```

Sie können bei der Gelegenheit auch stets die "echten" Ströme ausprobieren.

```
jshell> IntStream.of(1,2,3).average()
$9 ==> OptionalDouble[2.0]
```

## Tests

Ein einfaches Testframework samt bislang weniger Tests finden Sie in [`StreamTest.java`](StreamTest.java). Die Tests lassen Sie wie folgt laufen:

```
java --enable-preview StreamTest.java 
```

Das Testframework zeigt den Einsatz eines Gatherers -- das ist ein sehr junges Konstrukt, das sich mittlerweile in die Streamverarbeitung einklinken lässt. So ganz intuitiv empfinde ich den Gebrauch bislang nicht; das liegt aber auch daran, dass ich mit das Konstrukt noch nicht systematisch erarbeitet habe.

## TODOs: Nachbauten von Gatherern und Collectoren

Gatherer erlauben sehr flexible Zwischenoperationen in Streams, und Collectoren bieten ebenfalls flexible Abschlussmöglichkeiten, die Elemente am Ende eines Streams zu verarbeiten. Für beides könnte man ebenfalls Nachbauten erstellen, um sich deren Arbeitsweise zu erschließen.

## Links

* [Core Java: Die Stream-API im Wandel – funktionale Datenflüsse in Java](https://www.heise.de/hintergrund/Core-Java-Die-Stream-API-im-Wandel-funktionale-Datenfluesse-in-Java-10353156.html), heise-online, 2025-04-25

