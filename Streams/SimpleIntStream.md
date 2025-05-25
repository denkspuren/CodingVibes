# `SimpleIntStream`: Selbstbau von `IntStream`

Wie funktionieren Streams konzeptionell? Die beispielhafte Umsetzung [SimpleIntStream.java](SimpleIntStream.java) zeigt, wie man die Klasse `IntStream` nachbauen kann. Dazu nehmen wir eine Vereinfachung vor: Die regulären Streams in Java arbeiten mit dem Spliterator-Konzept, um u.a. die Parallelverarbeitung in Strömen zu realisieren. Wenn man darauf verzichtet, kommt man mit dem Iterator-Konzept aus.

## Was ist ein Iterator?

Das Konzept des **Iterators** bildet das Interface [`java.util.Iterator<T>`](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/Iterator.html) ab. Mit einem Iterator kann man auf die von einem Objekt verwalteten Datenelemente nacheinander kontrolliert zugreifen -- vorausgesetzt natürlich, die Klasse des Objekts implementiert das Interface.

### Zwei wichtige Methoden: `hasNext()` und `next()`

Die zwei zentralen Methoden sind:

* `boolean hasNext()` -- Prüft, ob noch ein weiteres Element vorhanden ist. Gibt `true` zurück, solange noch Elemente zum Verarbeiten da sind.

* `T next()` -- Liefert das nächste Element und rückt den internen Zeiger weiter. Ruft man `next()` ohne ein vorheriges `hasNext()` auf, kann eine `NoSuchElementException` geworfen werden, wenn kein Element mehr verfügbar ist.

Daneben gibt es noch die Methode `remove`, die bei Bedarf implementiert werden kann, und die default-Methode `forEachRemaining`. Beide sind hier nicht relevant.

### Collections liefern Iterator mit `iterator()`

Jede Collection (wie z.B. `List`, `Set`, `Queue`) stellt über `.iterator()` einen `Iterator` bereit, sodass jede Collection auf dieselbe Art durchlaufen werden kann.

```java
Iterator<String> it = List.of("Alice", "Bob", "Charlie").iterator();
while (it.hasNext()) IO.println(it.next());
// Alice
// Bob
// Charlie
```

### `for`-each erwartet `iterator()`-Methode

Wussten Sie es schon? Die _for-each_-Variante der `for`-Schleife erwartet intern einen Iterator!

```java
for (String name : List.of("Alice", "Bob", "Charlie")) IO.println(name);
> Alice
> Bob
> Charlie
```

### Spezielle Iteratoren für primitive Typen

Um **Boxing/Unboxing** zu vermeiden, existieren in Java für primitive Typen spezielle Iteratoren, z.B. [`PrimitiveIterator.OfInt`](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/PrimitiveIterator.OfInt.html), der statt `next()` eine Methode `nextInt()` bietet. Für den nachgebauten `IntStream` verwenden wir diesen speziellen Iterator.

### Spliteratoren ermöglichen parallele Traversierung

Die "echten" Streams verwenden `java.util.Spliterator<T>` statt `Iterator<T>`, die unser Nachbau verwendet. Aus zwei Gründen:

* Die Spliterator-Methoden verarbeiten die Elemente einer Datenquelle etwas effizienter.
* Neben dem sequentiellen Durchlauf (Traversieren) der Datenelemente ermöglichen Spliteratoren auch eine effiziente parallele Traversierung

## Der Aufbau von `SimpleIntStream`

Die Klasse `SimpleIntStream` demonstriert die Konzeption eines Streams mithilfe von Iteratoren: Eine Datenverarbeitungskette (Pipeline) aus aufeinanderfolgenden Iteratoren wird aufgesetzt, die erst bei einer Terminal-Operation ausgeführt wird.

Das aufwändigere Spliterator-Konzept sparen wir bewusst aus, um die Grundmechanismen von Streams anschaulich herauszuarbeiten.

### Grundlegende Stream-Operationen

Bei den Stream-Operationen unterscheidet man

* die Konstruktion bzw. **Erstellung einer Stream-Pipeline** (Datenverarbeitungskette) anhand einer **Datenquelle**; dafür stehen verschiedene statische Methoden in `SimpleIntStream` zur Verfügung wie z.B. `of` und `range`.
* Intermediate-Operationen, also Methoden, die zum Aufbau der Pipeline dienen wie z.B. `filter` oder `map`.
* Terminal-Operationen, also Methoden, die die Verarbeitungskette beenden, wie z.B. `forEach` oder `reduce`.

Dabei kommt eine Besonderheit zum Tragen:

* Streams werden _lazy_ bearbeitet. Die Terminal-Operation "zieht" (_pull_) die Daten aus der Pipeline, und zwar nur dann, wenn sie benötigt werden und auch nur so viele, wie benötigt werden.

### Lazy-Evaluation durch `Supplier`

Die **Kernidee**: Jeder Aufruf einer Stream-Operation erzeugt keine direkte Datenverarbeitung, sondern **verkettet** nur die nächsten Operationen. Die zurückgestellte Datenverarbeitung kann mit einem Supplier realisiert werden.

```java
private final Supplier<PrimitiveIterator.OfInt> iteratorSupplier;
```

### Pipeline-Erstellung mit einem Head-Iterator

Statische Methoden wie `range`, `rangeClosed`, `of`, `empty`, `iterate`, `generate` erzeugen den Anfang einer Datenverarbeitungskette (Stream-Pipeline) mit einem entsprechenden Iterator. Dieser Iterator bildet den Kopf (_head iterator_) der Pipeline.

```java
public static SimpleIntStream range(int start, int end) {
    return new SimpleIntStream(() -> new PrimitiveIterator.OfInt() {
        private int curr = start;
        @Override public boolean hasNext() { return curr < end; }
        @Override public int nextInt() { return curr++; }
    });
}
```

> Das Interface `PrimitiveIterator.OfInt` wird über eine abstrakte Klasse implementiert und instanziiert! Die Implementierung richtet sich nach der entsprechenden Factory-Methode, also einer der statischen Methoden.

### Intermediate-Operations (`filter`, `map`, `peek`)

Jede Intermediate-Operation erzeugt einen neuen `SimpleIntStream`, dessen Iterator sich über den Supplier des Vorgängers auf den Vorgänger-Iterator in der Pipeline bezieht (`iteratorSupplier.get()`). Dadurch entsteht eine Kette von Iteratoren, die sich zur Verarbeitung jeweils beim Vorgänger-Iterator die Elemente mit `nextInt` "ziehen".

```java
public SimpleIntStream filter(IntPredicate predicate) {
    return new SimpleIntStream(() -> new PrimitiveIterator.OfInt() {
        final PrimitiveIterator.OfInt it = iteratorSupplier.get();
        // ... Filter-Logik ...
    });
}
```

* `filter`: Überspringt Elemente, bis das Prädikat `true` ergibt.
* `map`: Wendet eine Funktion auf jedes Element an.
* `peek`: Führt einen Seiteneffekt aus und gibt das unveränderte Element weiter.

### Terminal-Operations (`reduce`, `forEach`, `sum`, …)

Terminal-Methoden lösen die Verarbeitung der Daten über die Pipeline aus und führen zu einem Ergebniswert oder einem Seiteneffekt.

* `reduce`: Traversiert die Datenelemente und akkumuliert das Ergebnis.
* `forEach`: Führt eine Aktion für jedes Element aus.
* Aggregationen (`sum`, `prod`, `count`, `average`) bauen auf `reduce` oder `map` + `sum` auf.

Beispiel `reduce`:

```java
public OptionalInt reduce(IntBinaryOperator op) {
    PrimitiveIterator.OfInt it = iteratorSupplier.get();
    if (!it.hasNext()) return OptionalInt.empty();
    int result = it.nextInt();
    while (it.hasNext()) {
        result = op.applyAsInt(result, it.nextInt());
    }
    return OptionalInt.of(result);
}
```

Die `reduce`-Methode hat noch eine zweite Variante, die zu der Lambda-Operation einen Wert namens `identity` erwartet. In dem Fall wird kein mit `OptionalInt` "eingewickelter" (gewrappter) Wert zurückgeliefert. Denn selbst bei einem leeren Strom gibt es einen Ergebniswert. Die obige `reduce`-Methode hingegen kennt keinen Wert, der bei einem leeren Strom zurückgegeben werden kann und arbeitet deshalb mit `OptionalInt`.

# Links

* `java.util.stream`: https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/package-summary.html
* `java.util.stream.IntStream`: https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/IntStream.html
* `ja.util.Iterator<T>`: https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/Iterator.html
* `java.util.PrimitiveIterator.OfInt`: https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/PrimitiveIterator.OfInt.html
* `java.util.OptionalInt`: https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/OptionalInt.html