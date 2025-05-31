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

#### Methoden und Konstruktoren können zudem typparametrisiert werden

Die Deklaration einer Variablen (im Rumpf einer Methode bzw. eines Konstruktors oder im Kopf als Parameter) folgt der Konvention:

```
<Typname> <Variablenname> // mit bzw. ohne Initialisierungsausdruck
```

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