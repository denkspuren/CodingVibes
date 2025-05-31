# `SimpleStream`: Die Typisierung bei genererische Programmierung

Nach der Umsetzung der Klasse `SimpleIntStream` war es ein Leichtes, `SimpleStream` zu implementieren: Der `int` Typ, der typgerechte Iterator `PrimitiveIterator.OfInt` und die `nextInt`-Methode mussten ersetzt werden durch generische Typvariablen und den generischen `Iterator<T>`.

## 🧠 Was bedeutet das `T` in `class SimpleStream<T>`?

Was bedeutet es, wenn man den Kopf der Klasse generisch deklariert?

```java
public class SimpleStream<T> { /* ... */ }
```

Die Deklaration `SimpleStream<T>` macht die Klasse **generisch**, d.h. sie ist nicht auf einen konkreten Datentyp festgelegt, sondern arbeitet mit einem **Typparameter `T`**, der **zur Verwendungszeit** (also beim Instanziieren) bestimmt wird.

* `T` steht für einen beliebigen Objekttyp
* Die Klasse kann **mit jedem Referenztyp** verwendet werden: `SimpleStream<String>`, `SimpleStream<Integer>`, `SimpleStream<MyClass>`, ...
* Innerhalb der Klasse kann dann `T` überall wie ein "echter" Typ verwendet werden
  * als Typargument 
  * als Rückgabetyp
  * als Parametertyp

**Beispiel:**

```java
SimpleStream<String> names = new SimpleStream<>();
SimpleStream<Integer> numbers = new SimpleStream<>();
```

* `names` ist ein Stream von `String`-Werten
* `numbers` ist ein Stream von `Integer`-Werten
* Beide benutzen denselben Klassencode, aber mit unterschiedlichem **Typargument**

Das macht den Code:

* **wiederverwendbar** (nur einmal geschrieben)
* **typsicher** (kein Cast nötig, der zur Laufzeit schiefgehen kann)
* **lesbar und dokumentierend** (die API sagt direkt, mit welchem Typ sie arbeitet)

---

* Typparameter werden in Deklarationsköpfen von Klassen, Interfaces und Methoden verwendet.
* Typvariablen sind Platzhalter für einen konkreten Typen, sie beziehen sich namentlich auf einen Typparameter im Kopf des Deklarationskonstrukts. Typvariablen können überall dort im Rumpf einer Deklaration verwendet werden, wo ein konkreter Referenztyp stehen könnte. 
* Typargumente belegen einen Typparameter mit einem Typ, wenn die typparametrisierte Klasse bzw. das typparametrisierte Interface instanziiert oder eine typparametrisierte Methode aufgerufen wird.

---

### Typparameter, Typvariablen und Typargumente

Man unterscheidet Typparameter, Typvariablen und Typargumente. Um sich den Unterschied zwischen Parameter, Variable und Argument zu verdeutlichen, sei an die Begrifflichkeiten bei Methoden und Konstruktoren erinnert.

#### Methoden und Konstruktoren sind parametrisierbare Codeblöcke 

Im Kopf einer Methode oder eines Konstruktors werden Parameter deklariert. Im Rumpf stehen sie als Variablen zu Verfügung. Und ein Wert wird beim Aufruf, also der Verwendung der Methode bzw. Konstruktor, als Argument übergeben.

Ein Beispiel:

```
int add(int x, int y) { return x + y;)
```

* Eine Methode bzw. ein Konstruktor deklariert einen benamten Codeblock, der über die Angabe `int x` und `int y` parametrisiert wird; `int x` und `int y` sind die **Parameter** der Methode. Der Codeblock (der Deklarationsrumpf) ist der Anteil, der in geschweiften Klammern direkt nach Rückgabetyp und Signatur folgt.
* Damit können im Rumpf der Methode bzw. des Konstruktor die **Variablen** `x` und `y` verwendet werden.
* Beim Aufruf der Methode mit Werten für die **Argumente** (z.B. `add(2, 3)`), wird der Variablen `x` der Wert `2` und der Variablen `y` der Wert `3` zugewiesen, bevor mit der Ausführung des Codes begonnen wird.

Während eine Methode parametrisiert sein kann, aber nicht sein muss, ist die Angabe des Rückgabetyps Pflicht.

* Im Beispiel ist `int` der Rückgabetyp der Methode `add`. Die Methode liefert am Ende ihrer Ausführung einen Wert vom Typ `int` zurück.

#### Konstruktoren sind typparametrisierbar

Konstruktoren lassen sich implizit bei der Deklaration des Referenztypen, also der Klasse bzw. dem Interface, mit Typen parametrisieren. Ein oder mehr **Typparameter** folgen dem Klassennamen in spitzen Klammern.

```java
class Pair<T1, T2> {
    private T1 left;
    private T2 right;
    public Pair(T1 left, T2 right) {
        this.left = left;
        this.right = right;
    }
}
```

Damit kann im Rumpf der Deklaration an all den Stellen eine namensgleiche **Typvariable** verwendet werden, wo syntaktisch eine Typangabe hingehört: bei der Deklaration von Klassen- und Instanzvariablen, in den Köpfen und Rümpfen von Klassen- und Instanzmethoden und denen von Konstruktoren, den Init-Blocken für die Klasse bzw. die Instanz.

Damit wird der Referenztyp generisch, d.h. anpassbar durch ein **Typargument**, das beim Konstruktoraufruf übergeben wird.

```java
new Pair<Integer, String>(1, "one")
```

Die Typvariable `T1` bekommt den Typwert `Integer`, die Typvariable `T2` den Typwert `String`. Folglich wird eine Instanz von einer Klasse `Pair` erstellt, deren Code sich gedanklich wie folgt darstellt:


```java
class Pair {
    private Integer left;
    private String right;
    public Pair(Integer left, String right) {
        this.left = left;
        this.right = right;
    }   
}
```

#### Methoden sind typparametrisierbar

Eine Methode, gleich ob Klassen- oder Objektmethode, kann ebenfalls mit einem **Typparameter** oder mehreren versehen werden. Die Typparameter werden vor dem Rückgabetyp in spitzen Klammern deklariert.

In dem Beispiel wird die statische `of`-Methode (also eine Klassenmethode) mit dem Typparameter `T` parametrisiert. Der Rückgabetyp und die Methodenparameter verwenden die **Typvariable** `T`.

```java
class Pair<T1, T2> {
    static <T> Pair<T, T> of(T left, T right) {
        return new Pair<>(left, right);
    }
    private T1 left;
    private T2 right;
    public Pair(T1 left, T2 right) {
        this.left = left;
        this.right = right;
    }
}
```

Beim Aufruf der Methode wird der Typ, in dem Fall `Integer`, als **Typargument** übergeben.

```java
 Pair.<Integer>of(1, 2) // ==> Pair@5ef04b5
```

Die `of`-Methode stellt sich mit dem Typargument gedanklich wie folgt dar:

```java
    static Pair<Integer, Integer> of(Integer left, Integer right) {
        return new Pair<>(left, right);
    }
```

#### Gleichnamige Typparameter in Methoden haben Vorrang

Was, wenn ein Typparameter in der Klasse oder im Interface den gleichen Namen hat, wie der Typparameter in der Methode?

> Typparameter in Methoden "überschreiben" gleichnamige Typparameter der umgebenden Klasse bzw. des Interface.

```java
class Container<T> {
    <T> T returnValue(T value) { return value; } 
}
```

```java
new Container<String>().<Integer>returnValue(3) // ==> Integer: 3
new Container<String>().returnValue(3) // ==> Integer: 3
```

Das Beispiel zeigt, dass, wenn eine Methode einen Typparameter mit demselben Namen wie der Typparameter der umgebenden Klasse deklariert, der Methoden-Typparameter für den Geltungsbereich dieser Methode Vorrang hat und den Klassen-Typparameter mit demselben Namen "verdeckt".


<!---- ENDE SCHREIBTEXT ---->


---

Die Deklaration einer Variablen (im Rumpf einer Methode bzw. eines Konstruktors oder im Kopf als Parameter) folgt der Konvention:

```java 
// <Typname> <Variablenname> mit bzw. ohne Initialisierungsausdruck
int x;
Book book;
String repr(Item o) { /* ... */ }
```

Dazu kommt, das Methoden Rückgabetypen haben:

```java
int add(int x, int y) { return x + y; }

```
<Typvariablenname> <Variablenname> // mit bzw. ohne Initialisierungsausdruck
```





* Typparameter werden im Deklarationskopf von Klassen, Interfaces oder Methoden definiert.

* Damit können im Rumpf der Deklaration Typvariablen verwendet werden, die gleichermaßen als Platzhalter fungieren, an denen ein konkreter Referenztyp stehen könnte.

* Ein Typargument liefert einen konkreten Referenztyp oder eine Typvariable als Typ, der bei der Verwendung des Deklarationskopfes (Instanziierung bzw. Methodenaufruf) bestimmt wird und somit den Typwert den Typvariablen im Rumpf der Deklaration zuweist.




Sie legen formale Platzhalter für Typen fest.

* Typvariablen sind Verweise auf diese Typparameter im Rumpf einer Deklaration. Sie können überall dort eingesetzt werden, wo ein konkreter Referenztyp erlaubt ist – z. B. als Rückgabe-, Parameter- oder Feldtyp.

* Typargumente sind konkrete Typen, mit denen Typparameter bei der Verwendung einer generischen Klasse, eines Interfaces oder einer Methode belegt werden.

* Eine Klasse, ein Interface oder eine Methode heißt generisch, wenn sie im Deklarationskopf typparametrisiert ist – also mindestens einen Typparameter deklariert.

Zur Notation: 

* Typparameter in Deklarationsköpfen, z.B.

```
class SimpleStream<T> { /* ... */ }
public static <T> SimpleStream<T> iterate(T seed, UnaryOperator<T> f) { /* ... */ }
```

* Bei Klassen und Interfaces direkt nach dem Klassen- bzw. Interfacenamen in spitzen Klammern
* Bei Methoden *vor* dem Rückgabetyp in spitzen Klammern


## Fallbeispiele

### `filter`-Methode

```java
public <T> SimpleStream<T> filter(Predicate<T> predicate)
```

* Die Klasse `SimpleStream<T>` hat den Typparameter `T`. Zur Verdeutlichung nennen wir diesen Typparameter `T_class`.
* Die `filter`-Methode führt einen eigenen Typparameter `T` ein (`public` **`<T>`** ...), den wir mit `T_method` bezeichnen
* Aufgrund der Namensgleichheit

---
The issue with the selected line, final Iterator<T> it = iteratorSupplier.get();, lies in how the generic type T is being used within the filter method.

Let's break it down:

Your class SimpleStream<T> has a type parameter T (let's call this T_class for clarity).
The field iteratorSupplier is of type Supplier<Iterator<T_class>>. So, iteratorSupplier.get() returns an Iterator<T_class>.
The filter method is declared as public <T> SimpleStream<T> filter(Predicate<T> predicate). The <T> here introduces a new type parameter, local to the filter method (let's call this T_method). This T_method shadows T_class.
Inside the anonymous Iterator within filter, the line final Iterator<T> it = iteratorSupplier.get(); attempts to assign the result of iteratorSupplier.get() (which is Iterator<T_class>) to the variable it, which is declared as Iterator<T_method>.
This creates a type mismatch: you're trying to assign an Iterator<T_class> to an Iterator<T_method>. These types are not necessarily the same, leading to a compile-time error or unsafe type operations.

How to fix it:

The filter operation should process elements of the type contained in the current stream (T_class). Therefore, the filter method should not declare its own, new type parameter <T>. It should use the existing T from the class SimpleStream<T>.

Additionally, for the predicate, it's good practice to use Predicate<? super T>. This makes the filter method more flexible, allowing it to accept predicates that can handle T or any supertype of T (following the PECS principle - Producer Extends, Consumer Super).

Here's the suggested change:

SimpleStream.java
-2
+2


    /** Filter: behält nur diejenigen Elemente, für die predicate true ist */
    public <T> SimpleStream<T> filter(Predicate<T> predicate) {
        return new SimpleStream<>(() -> new Iterator<T>() {
    public SimpleStream<T> filter(Predicate<? super T> predicate) {
        return new SimpleStream<>(() -> new Iterator<T>() { // Now T refers to the class-level T
            final Iterator<T> it = iteratorSupplier.get();
            private T next;
            private boolean hasNextComputed = false;

Explanation of the fix:

public SimpleStream<T> filter(Predicate<? super T> predicate):

We remove the <T> from after public. Now, T throughout the filter method (including in the anonymous Iterator and the Predicate) refers to the type parameter of the SimpleStream class (T_class).
Predicate<? super T>: The predicate can now be any predicate that consumes type T or any of its supertypes.
final Iterator<T> it = iteratorSupplier.get();:

With the change above, T in Iterator<T> it is now T_class.
iteratorSupplier.get() returns Iterator<T_class>.
So, this line becomes final Iterator<T_class> it = iteratorSupplier.get();, which is type-correct.
All other uses of T within the filter method's implementation (like private T next;, T v = it.next();) will now correctly refer to the stream's element type, resolving the type conflict and making your filter method operate correctly and safely.

----

### `map`-Methode

```java
    public <R> SimpleStream<T> map(Function<? super T, ? extends R> mapper) {
        return new SimpleStream<>(() -> new Iterator<R>() {
            final Iterator<T> it = iteratorSupplier.get();
            @Override public boolean hasNext() { return it.hasNext(); }
            @Override public R next()  { return mapper.apply(it.next()); }
        });
    }
```

You've correctly identified a typing issue in your map method!

The map operation is designed to transform elements of type T from the input stream into elements of a potentially different type R in the output stream. Therefore, the map method should return a SimpleStream<R>, not SimpleStream<T>.

Here's the breakdown:

public <R> SimpleStream<T> map(...):

You declare a new type parameter <R> for the method, which is correct. This R represents the type of the elements after the mapping function is applied.
However, the return type is SimpleStream<T>. This means you're declaring that map will return a stream of the original type T, which contradicts the purpose of mapping to a new type R.
return new SimpleStream<>(() -> new Iterator<R>() { ... });:

Inside the method, you are correctly creating a Supplier that provides an Iterator<R>. This iterator will yield elements of the new type R.
When you call new SimpleStream<>(...), the Java compiler tries to infer the type parameter for the SimpleStream being constructed.
If the map method's return type is SimpleStream<T>, the compiler expects the constructor argument to be a Supplier<Iterator<T>>.
But you are providing a Supplier<Iterator<R>>. This creates a mismatch if R is different from T.
The Fix:

You need to change the return type of the map method to SimpleStream<R>. This aligns the declared return type with the actual type of the stream being produced.

SimpleStream.java
-1
+1
    //     });
    // }

    public <R> SimpleStream<T> map(Function<? super T, ? extends R> mapper) {
    public <R> SimpleStream<R> map(Function<? super T, ? extends R> mapper) {
        return new SimpleStream<>(() -> new Iterator<R>() {
            final Iterator<T> it = iteratorSupplier.get();
            @Override public boolean hasNext() { return it.hasNext(); }

With this change:

public <R> SimpleStream<R> map(Function<? super T, ? extends R> mapper): Now correctly states that map returns a stream of elements of type R.
return new SimpleStream<R>(() -> new Iterator<R>() { ... });: The compiler will correctly infer that you are constructing a SimpleStream<R> because the Supplier provides an Iterator<R>, and this matches the method's return type.
This makes your map operation type-safe and behave as expected, transforming a stream of Ts into a stream of Rs.


### `mapToInt`-Methode

```java
    public <T> IntStream mapToInt(ToIntFunction<? super T> mapper) {
        return new SimpleIntStream(() -> new PrimitiveIterator.ofInt() {
            final Iterator<T> it = iteratorSupplier.get();
            @Override public boolean hasNext() { return it.hasNext(); }
            @Override public int nextInt()  { return mapper.applyAsInt(it.next()); }
        });
    }
```

You've got a couple of type-related issues in your mapToInt method. Let's sort them out!

Shadowing Type Parameter <T>: Your method public <T> IntStream mapToInt(...) declares its own type parameter <T>. This new T (local to the method) shadows the T from your class SimpleStream<T>. Inside the method, iteratorSupplier.get() returns an Iterator of the class's T, but when you declare final Iterator<T> it, this T refers to the method's T. This creates a mismatch. The mapToInt method should operate on the T of the stream it belongs to.

Return Type Mismatch: You're returning new SimpleIntStream(...), but the method signature declares a return type of java.util.stream.IntStream. Your SimpleIntStream is a custom class and not a subtype of the standard java.util.stream.IntStream. You should return your own SimpleIntStream.

Minor Typo in Iterator Creation: It should be new PrimitiveIterator.OfInt() (capital O) instead of new PrimitiveIterator.ofInt() (lowercase o) to match the java.util.PrimitiveIterator.OfInt interface.

Here's how to fix it:

SimpleStream.java
-2
+2
        });
    }

    public <T> IntStream mapToInt(ToIntFunction<? super T> mapper) {
        return new SimpleIntStream(() -> new PrimitiveIterator.ofInt() {
    public SimpleIntStream mapToInt(ToIntFunction<? super T> mapper) { // Removed <T>, changed return type
        return new SimpleIntStream(() -> new PrimitiveIterator.OfInt() { // Corrected OfInt
            final Iterator<T> it = iteratorSupplier.get();
            @Override public boolean hasNext() { return it.hasNext(); }
            @Override public int nextInt()  { return mapper.applyAsInt(it.next()); }

Explanation of Changes:

public SimpleIntStream mapToInt(ToIntFunction<? super T> mapper):

We removed the <T> from after public. Now, the T in ToIntFunction<? super T> and within the method body correctly refers to the type parameter of the SimpleStream<T> class.
The return type is changed from IntStream to SimpleIntStream to match the type of object you are actually constructing and returning.
new PrimitiveIterator.OfInt():

Changed ofInt to OfInt to correctly instantiate an anonymous class implementing java.util.PrimitiveIterator.OfInt.
With these adjustments, your mapToInt method will be type-safe and correctly transform your generic SimpleStream<T> into your specialized SimpleIntStream.