# Generische Programmierung

> Unfertige Entwurfsfassung!

## 1. Typparameter, Typvariablen und Typargumente

Man unterscheidet Typparameter, Typvariablen und Typargumente. Um sich den Unterschied zwischen Parameter, Variable und Argument zu verdeutlichen, sei an die Begrifflichkeiten bei Methoden und Konstruktoren erinnert.

## 2. Methoden und Konstruktoren sind parametrisierbare Codeblöcke 

Im Kopf einer Methode oder eines Konstruktors werden Parameter deklariert. Im Rumpf stehen sie als Variablen zur Verfügung. Und ein Wert wird beim Aufruf, also der Verwendung der Methode bzw. Konstruktor, als Argument übergeben.

Ein Beispiel:

```java
int add(int x, int y) { return x + y; }
```

* Eine Methode bzw. ein Konstruktor deklariert einen benamten Codeblock, der über die Angabe `int x` und `int y` parametrisiert wird; `int x` und `int y` sind die **Parameter** der Methode. Der Codeblock (der Deklarationsrumpf) ist der Anteil, der in geschweiften Klammern direkt nach Rückgabetyp und Signatur folgt.
* Damit können im Rumpf der Methode bzw. des Konstruktor die **Variablen** `x` und `y` verwendet werden.
* Beim Aufruf der Methode mit Werten für die **Argumente** (z.B. `add(2, 3)`), wird der Variablen `x` der Wert `2` und der Variablen `y` der Wert `3` zugewiesen, bevor mit der Ausführung des Codes begonnen wird.

Während eine Methode parametrisiert sein kann, aber nicht sein muss, ist die Angabe des Rückgabetyps Pflicht.

* Im Beispiel ist `int` der Rückgabetyp der Methode `add`. Die Methode liefert am Ende ihrer Ausführung einen Wert vom Typ `int` zurück.

## 3. Referenztypen sind typparametrisierbar

Referenztypen, also Klassen (inkl. Records und Enums) wie auch Interfaces, lassen sich mit Typen parametrisieren. Ein oder mehr **Typparameter** folgen dem Klassennamen in spitzen Klammern und wirken sich auf den Rest der Deklaration der Klasse bzw. des Interfaces aus.

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

## 4. Methoden sind typparametrisierbar

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

Da Java den Typen auch inferieren, d.h. anhand der Kontexte (hier der Datenwerte) automatisch ableiten kann, kann man auch schreiben:

```java
 Pair.of(1, 2) // ==> Pair@7fe04ac
```

## 5. Gleichnamige Typparameter in Methoden haben Vorrang

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

Das Beispiel zeigt, dass, wenn eine Methode einen Typparameter mit demselben Namen wie der Typparameter der umgebenden Klasse deklariert, der Methoden-Typparameter für den Geltungsbereich dieser Methode Vorrang hat und den Klassen-Typparameter mit demselben Namen "verdeckt". Und das Beispiel zeigt darüber hinaus: Wenn der Typ nicht parametrisiert wird, greift wieder die Typ-Inferenz: der Typ wird automatisch aus dem Argument des Wertes `3` abgeleitet.

Ohne den Typparameter `<T>` in der Methode ist das Ergebnis ein anderes:

```java
class Container<T> {
    T returnValue(T value) { return value; } 
}
```

```java
new Container().returnValue(3) // ==> Object: 3 -- mit Warnung, da Typ <T> nicht eingegrenzt wird
new Container<Integer>().returnValue(3) // ==> Integer: 3
new Container<String>().returnValue(3) // FEHLER, da 3 nicht zu String passt
```

## 6. Auch Konstruktoren sind (wie Methoden) typparametrisierbar

Auch die Deklaration eines Konstruktors mit einem Typparameter ist möglich, auch wenn man das selten in Code sieht!

Das Beispiel dient nur zur Demonstration und ist ansonsten nicht wirklich sinnvoll.

```java
class Box<T> {
    <U> Box(U u, T t) {
        System.out.println(u + " and " + t);
    }
}
```

```java
new <Double> Box<Integer>(2.4, 5) // ==> 2.4 and 5
```

<!---- ENDE SCHREIBTEXT ---->

# Wildcards

## 1. Warum Wildcards gebraucht werden

In Java können Klassen (oder Interfaces) generisch deklariert werden. Zum Beispiel:

```java
class Box<T> { 
    private T value;
    public Box(T v) { value = v; }
    public T get() { return value; }
    public void set(T v) { value = v; }
}
```

Möchte man nun mit mehreren, aber nicht genau bekannten Typen arbeiten, ist eine Möglichkeit gefragt, „einen beliebigen“ generischen Typ anzusprechen, ohne ihn exakt festlegen zu müssen. Beispiele:

* **Lesen aus einer Liste**: Man hat eine `List<Integer>`, aber vielleicht soll eine Methode auch `List<Double>` oder `List<String>` zulassen.
* **Schreiben in eine Liste**: Man möchte z. B. einer Methode erlauben, in **jede** `List<Number>` oder `List<Object>` ein `Integer` zu schreiben.

Java-Generics sind invariant („keine automatische Unter-/Obertypbeziehung“), weshalb `List<Number>` **nicht** direkt ein Obertyp von `List<Integer>` ist. Wildcards bieten hier eine gewisse Flexibilität, indem sie Platzhalter für einen unbekannten, aber eingeschränkten Typ verwenden.

---

## 2. Die drei Wildcard-Formen

1. **Unbounded Wildcard – `<?>`**

   * **Bedeutung:** „Irgendein Typ.“
   * **Schreibweise:**

     ```java
     List<?>           // Liste von irgendetwas
     Map<?, ?>         // Map von irgendeinem Typ auf irgendeinen Typ
     Box<?>            // Box von irgendeinem Typ
     ```
   * **Verwendung:**

     * Wenn nur **gelesen** wird (Lesefall) und keine Elemente eingefügt werden (außer `null`).
     * Beispiel:

       ```java
       void printAll(List<?> list) {
           for (Object o : list) {
               System.out.println(o);
           }
       }
       ```

       Hier kann man `printAll` mit `List<String>`, `List<Integer>` usw. aufrufen.
   * **Einschränkung:** In `List<?>` kann kein Element (außer `null`) hinzugefügt werden, weil unbekannt ist, welche Typen konkret erlaubt sind.

2. **Upper-Bounded Wildcard – `<? extends T>`**

   * **Bedeutung:** „Ein Typ, der entweder `T` selbst ist oder ein Untertyp von `T`.“
   * **Schreibweise:**

     ```java
     List<? extends Number>   // Liste von Number oder einer beliebigen Unterklasse (Integer, Double, …)
     Comparable<? extends T>  // vergleichbar mit T oder einem Untertyp
     ```
   * **Verwendung:**

     * Wenn aus der Struktur Werte **gelesen** werden sollen, die garantiert vom Typ `T` oder einer Unterklasse sind.
     * Beispiel:

       ```java
       double sum(List<? extends Number> nums) {
           double s = 0;
           for (Number n : nums) {
               s += n.doubleValue();
           }
           return s;
       }
       ```

       Hier kann man `sum(new ArrayList<Integer>(…))` oder `sum(new ArrayList<Double>(…))` aufrufen.
   * **Einschränkung:** In `List<? extends Number>` kann kein Element hinzugefügt werden (außer `null`), weil unklar ist, ob es sich um eine `List<Integer>`, `List<Double>` oder eine andere Unterklasse handelt.

3. **Lower-Bounded Wildcard – `<? super T>`**

   * **Bedeutung:** „Ein Typ, der entweder `T` selbst ist oder eine Oberklasse von `T`.“
   * **Schreibweise:**

     ```java
     List<? super Integer>  // Liste von Integer oder irgendeinem Supertyp, z. B. Number, Object
     Comparator<? super T>  // Comparator, der mindestens Objekte vom Typ T (oder Supertypen) vergleichen kann
     ```
   * **Verwendung:**

     * Wenn **geschrieben** werden soll: Man möchte Objekte vom Typ `T` (oder Untertypen) in die Struktur einfügen, aber gleichzeitig zulassen, dass die tatsächliche Liste auch ein Supertyp von `T` ist.
     * Beispiel:

       ```java
       void addIntegers(List<? super Integer> list) {
           list.add(42);
           list.add(7);
           // list.add(new Object()); // ❌ nicht erlaubt, weil Object möglicherweise kein Integer ist
       }
       ```

       Diese Methode lässt sich auf `List<Integer>`, `List<Number>` oder `List<Object>` aufrufen.
   * **Einschränkung:** Beim Lesen aus `List<? super Integer>` erhält man nur `Object` (kleinster gemeinsamer Supertyp), denn die Liste könnte ja z. B. eine `List<Object>` sein. Ein Lesezugriff liefert deshalb nur `Object`, nicht `Integer` direkt.

---

## 3. Anwendungsstellen in Signaturen

### 3.1. In Collections-Methoden der Standardbibliothek

1. **`Collection<? extends E> c` in `addAll`**

   ```java
   interface Collection<E> {
       boolean addAll(Collection<? extends E> c);
   }
   ```

   * **Erklärung:**

     * `E` ist der Elementtyp der Ziel-Collection (`this`).
     * `c` darf eine beliebige `Collection<? extends E>` sein (z. B. `Collection<Integer>` oder `Collection<Double>`), weil jedes Element von `c` garantiert mindestens vom Typ `E` ist (oder ein Untertyp).
     * Aufruf-Beispiel:

       ```java
       List<Number> nums = new ArrayList<>();
       List<Integer> ints = List.of(1, 2, 3);
       nums.addAll(ints); // erlaubt, weil Integer extends Number
       ```
     * **Lesefall → Producer → `extends`.**

2. **`Collections.copy(List<? super T> dest, List<? extends T> src)`**

   ```java
   public static <T> void copy(
       List<? super T> dest, // Ziel: nimmt T oder Super-T auf
       List<? extends T> src // Quelle: liefert T oder Sub-T
   )
   ```

   * **Aufruf-Beispiel:**

     ```java
     List<Number> numbers = new ArrayList<>();
     List<Integer> ints = List.of(1, 2, 3);
     Collections.copy(numbers, ints);
     // dest: List<Number> ist ? super Integer (Number ist Superklasse von Integer)
     // src:  List<Integer>  ist ? extends Integer (Integer ist genau Integer)
     ```
   * **Begründung (PECS):**

     * `src` ist Producer (liefert Elemente vom Typ `T` oder Untertyp), daher `? extends T`.
     * `dest` ist Consumer (nimmt Elemente vom Typ `T` oder Obertypen auf), daher `? super T`.

3. **`Comparator<? super T>` in Sortiermethoden**

   ```java
   public static <T> void sort(List<T> list, Comparator<? super T> c);
   ```

   * Ein `Comparator<? super T>` kann zwei Objekte vom Typ `T` (oder Untertyp) vergleichen, wenn z. B. ein `Comparator<Number>` verwendet wird.
   * Beispiel:

     ```java
     List<Integer> ints = List.of(3, 1, 4);
     Comparator<Number> cmpNum = (a, b) ->
         Double.compare(a.doubleValue(), b.doubleValue());
     Collections.sort(ints, cmpNum);
     // Erlaubt, weil Number ist Superklasse von Integer, also cmpNum: Comparator<? super Integer>
     ```

### 3.2. In eigenen Methoden-Signaturen

* **Nur Lesen (Producer)**

  ```java
  public static double sumOfNumbers(List<? extends Number> list) {
      double sum = 0;
      for (Number n : list) {
          sum += n.doubleValue();
      }
      return sum;
  }
  ```

  * Aufruf-Beispiele:

    ```java
    List<Integer> ints = List.of(1, 2, 3);
    List<Double> dbls = List.of(1.5, 2.5);
    sumOfNumbers(ints); // zulässig, weil Integer extends Number
    sumOfNumbers(dbls); // zulässig, weil Double extends Number
    ```

* **Nur Schreiben (Consumer)**

  ```java
  public static void fillWithZeros(List<? super Integer> list) {
      for (int i = 0; i < 5; i++) {
          list.add(0);
      }
  }
  ```

  * Aufruf-Beispiele:

    ```java
    List<Integer> intList = new ArrayList<>();
    List<Number> numList = new ArrayList<>();
    List<Object> objList = new ArrayList<>();

    fillWithZeros(intList);  // List<Integer> ist ? super Integer
    fillWithZeros(numList);  // List<Number> ist ? super Integer
    fillWithZeros(objList);  // List<Object> ist ebenfalls ? super Integer
    ```

* **Gemischt (Lesen und Schreiben) ohne Wildcard**
  Wenn in derselben Methode **Lesen** und **Schreiben** auf den gleichen konkreten Typ erforderlich ist, verwendet man meist den exakten Typ:

  ```java
  public static void processStrings(List<String> list) {
      list.add("foo");         // schreiben
      String s = list.get(0);  // lesen
      // ...
  }
  ```

  Dabei ist kein Wildcard-Typ möglich, da sowohl Einfügen als auch Zugriff in identischem Bezug auf denselben konkreten Typ erfolgen sollen.

### 3.3. In generischen Klassen- oder Methodendeklarationen

Wildcards können auch als Feldtyp, Rückgabetyp oder Parameter in einer generischen Klasse auftreten:

```java
class Container<T> {
    private List<? super T> sink;       // erlaubt Einfügen von T in sink
    private List<? extends T> source;   // erlaubt Lesen von T aus source

    public Container(List<? super T> sink, List<? extends T> source) {
        this.sink = sink;
        this.source = source;
    }

    public void copyAll() {
        for (T element : source) {
            sink.add(element);
        }
    }
}
```

* `source` ist eine Liste von Typen, die mindestens `T` sind (Covarianz → `extends` → Producer).
* `sink` ist eine Liste, die mindestens Platz für `T` bietet (Contravarianz → `super` → Consumer).

---

## 4. Die PECS-Merkregel

**PECS** steht für **Producer Extends, Consumer Super**:

* **Producer Extends:**
  Verwenden Sie `? extends T`, wenn die Datenstruktur **nur liefert** (nur gelesen wird).
  Beispiel: Methode, die aus einer Liste Werte entnimmt und verarbeitet.

* **Consumer Super:**
  Verwenden Sie `? super T`, wenn die Datenstruktur **nur Werte aufnimmt** (nur geschrieben wird).
  Beispiel: Methode, die `Integer`-Werte in eine Liste einfügt, die Integer oder einen Supertyp akzeptiert.

* **Beispiel für das Zusammenspiel:**

  ```java
  public static <T> void copy(List<? super T> dest, List<? extends T> src) {
      for (T item : src) {
          dest.add(item);
      }
  }
  ```

  * `src` ist Producer → `? extends T`.
  * `dest` ist Consumer → `? super T`.

---

## 5. Weitere typische Anwendungsfälle von `<? super T>`

1. **`Comparator<? super T>`**
   Viele Sortier- oder Vergleichs-APIs akzeptieren `Comparator<? super T>`, damit nicht zwingend ein `Comparator<T>` geschrieben werden muss, sondern z. B. auch ein `Comparator<Object>` oder `Comparator<Number>`.
   Beispiel:

   ```java
   List<Integer> ints = List.of(1, 2, 3);
   Comparator<Number> cmpNum = (a, b) -> 
       Double.compare(a.doubleValue(), b.doubleValue());
   Collections.sort(ints, cmpNum);
   // Erlaubt, weil Number ist Superklasse von Integer → Comparator<? super Integer>
   ```

2. **`Map<? super K, ? extends V>`**
   In einigen Methoden-Signaturen sehen Sie zum Beispiel:

   ```java
   void doSomething(Map<? super K, ? extends V> map) { … }
   ```

   Das bedeutet:

   * Schlüssel `K` oder ein Supertyp von `K` (Consumer)
   * Werte mindestens vom Typ `V` oder Untertyp (Producer).
     So kann man z. B. in die Map `put(K key, V value)` und aus ihr als `V` (oder Untertyp) lesen.

3. **Eigene generische Klassen mit Feldern vom Typ `? super T`**

   ```java
   class Handler<T> {
       private List<? super T> targets;
       public Handler(List<? super T> targets) {
           this.targets = targets;
       }
       public void handle(T item) {
           targets.add(item); // zulässig
       }
   }
   ```

   Hier kann `Handler<String>` auf `List<Object>`, `List<CharSequence>` oder `List<String>` verweisen und `String`-Objekte hineinschreiben.

---

## 6. Zusammenfassung: Wo kommen `<? super T>` (und verwandte Formen) vor?

* **Methodenparameter in JDK-APIs**

  * `Collection<? extends E> c` in `addAll` (Producer)
  * `List<? super T>` und `List<? extends T>` in `Collections.copy` (Consumer / Producer)
  * `Comparator<? super T>` in `Collections.sort` (Consumer)

* **Eigene Methoden-Signaturen**

  * `void copy(List<? super T> dest, List<? extends T> src)`
  * `void fillWithZeros(List<? super Integer> list)`
  * `double sum(List<? extends Number> nums)`

* **Felder in eigenen generischen Klassen**

  ```java
  class DataHolder<T> {
      private List<? super T> consumers;   // nimmt T auf
      private List<? extends T> producers; // liefert T
      …
  }
  ```

* **Rückgabetyp von Methoden**

  ```java
  public List<? extends Animal> getAnimals() { … }
  public Map<String, ? super Number> getStatsMap() { … }
  ```

* **Streams und andere JDK-Komponenten**
  Auch in manchen `Stream<? super T>`-Operatoren oder `ComparatorChain<? super T>` tauchen Wildcards auf, um Varianz abzubilden.

---

## 7. Fazit

* **`<?>`**: Verwenden Sie diese Wildcard, wenn ein völlig beliebiger generischer Typ akzeptiert werden soll und nur **gelesen** wird (Lesefall).
* **`<? extends T>`**: Verwenden Sie diese Wildcard, wenn nur **gelesen** wird (Producer). Typ wird auf eine Obergrenze (Upper Bound) eingeschränkt.
* **`<? super T>`**: Verwenden Sie diese Wildcard, wenn nur **geschrieben** wird (Consumer). Typ wird auf eine Untergrenze (Lower Bound) eingeschränkt.

Die Faustregel **PECS** („Producer Extends, Consumer Super“) hilft, im Zweifel die richtige Wildcard zu wählen. So taucht `<? super T>` immer dann auf, wenn eine Datenstruktur oder Methode Elemente vom Typ `T` (oder Untertypen) **aufnehmen** soll, gleichzeitig aber flexibel bleiben möchte, welche Supertypen von `T` intern verwendet werden.


# Covarianz, Contravarianz und Invarianz

**Covarianz, Contravarianz und Invarianz** beschreiben in der Typentheorie, wie sich Untertypenbeziehungen auf „abgeleitete“ Typkonstrukte (etwa Container, Listen, Funktionen) übertragen. Kurz gesagt:

1. **Invarianz**
2. **Covarianz**
3. **Contravarianz**

Im Folgenden jeweils Definition, Bedeutung und Beispiele (insbesondere in Java).

---

## 1. Invarianz

* **Definition:**
  Ein parametrischer Typ (z. B. `Container<T>`) ist invarianzbezogen, wenn es **keine** Untertyp-Beziehung zwischen zwei Instanzen `Container<S>` und `Container<T>` gibt, selbst wenn `S` Untertyp von `T` ist.
  Anders formuliert:

  > `Container<S>` ist **nicht** Subtyp von `Container<T>`, wenn `S` Subtyp von `T` ist.

* **Bedeutung:**
  Wenn ein generischer Typ invarianz ist, kann man nicht einfach eine z. B. `List<Integer>` in einen „Platzhalter“ vom Typ `List<Number>` stecken, obwohl `Integer` ein Untertyp von `Number` ist. Java-Generics sind standardmäßig **invariant**.

* **Beispiel in Java:**

  ```java
  List<Number> zahlenListe = new ArrayList<Number>();
  List<Integer> intListe  = new ArrayList<Integer>();

  // ❌ Invarianz: list< Integer > ist kein Subtyp von list< Number >:
  zahlenListe = intListe; // <-- verursacht Compilerfehler
  ```

  Obwohl `Integer` zu `Number` gehört, ist `List<Integer>` **nicht** zuweisbar an `List<Number>`.

* **Konsequenz:**
  Bei invariant deklarierten generischen Klassen und Methoden müssen Typen genau übereinstimmen.
  → Wenn man Flexibilität möchte, wird man Wildcards einsetzen, um Covarianz (`<? extends T>`) oder Contravarianz (`<? super T>`) zu erzwingen.

---

## 2. Covarianz

* **Definition:**
  Ein Typsystem ist dort **kovariant**, wo eine Untertyp-Beziehung erhalten bleibt, wenn man den Parametertyp „nach oben“ kompositioniert.
  Formal:

  > Wenn `S` ein Untertyp von `T` ist (`S <: T`), dann ist auch `Container<S>` ein Untertyp von `Container<T>`.

* **Bedeutung:**
  Mit Covarianz kann man Objekte einer Liste von Untertypen dort verwenden, wo eine Liste des Obertyps verlangt wird – allerdings nur in einer “lesenden” Position (Producer). Man riskiert dabei, Elemente in der Liste nicht „sicher“ hinzufügen zu können.

* **Java-Beispiel über Wildcards:**
  Da Java-Generics standardmäßig invariant sind, gibt es keine direkte Deklaration wie `class List<+T>`, um Covarianz einzuführen. Stattdessen verwendet man **upper-bounded wildcards**:

  ```java
  // Methode akzeptiert List<E> oder jede List< Untertyp von E >
  void leseAlle(List<? extends Number> liste) {
      for (Number n : liste) {
          System.out.println(n);
      }
      // liste.add(…) ist hier nicht erlaubt (außer null)
  }

  List<Integer> intListe = List.of(1,2,3);
  List<Double> dblListe  = List.of(1.5, 2.5);

  // Covarianz durch <? extends Number> → Integer und Double sind Untertypen
  leseAlle(intListe); // zulässig
  leseAlle(dblListe); // zulässig
  ```

  Hier ist `List<Integer>` und `List<Double>` jeweils ein Subtyp von `List<? extends Number>`.
  Weil man nur **liest**, bleibt Typ-Sicherheit gewährleistet und man kann beliebige Untertypen verarbeiten.

* **Weitere Beispiele (Arrays als Ausnahme in Java):**
  In Java sind Arrays (historisch) **kovariant**, aber das kann zu Laufzeitfehlern führen:

  ```java
  Number[] zahlen = new Integer[5]; // erlaubt, da Arrays kovariant sind
  zahlen[0] = 3.14; // ArrayStoreException zur Laufzeit, weil Float nicht in ein Integer-Array passt
  ```

  → Bei Arrays heißt Covarianz, dass `Integer[] <: Number[]` gilt, obwohl Einfügen von `Double` in ein `Integer[]` zu einem Laufzeitfehler führt. Deshalb sind Arrays im Java-Generics-Kontext als unsauber covariant einzustufen.

---

## 3. Contravarianz

* **Definition:**
  Ein Typsystem ist **kontravariant**, wenn eine Untertyp-Beziehung umkehrt:

  > Wenn `S` ein Untertyp von `T` ist (`S <: T`), dann ist `Container<T>` ein Untertyp von `Container<S>`.

  Man dreht also die Richtung der Untertyp-Beziehung um.

* **Bedeutung:**
  Contravarianz eignet sich, wenn ich Methoden oder Container typ-sicher **mit Werten des Untertyps füllen** will, und gleichzeitig möchte, dass Dieselbe Methode auch mit Obertypen funktioniert.
  In Java erreicht man Contravarianz mit **lower-bounded wildcards**:

  ```java
  void fügeIntsHinzu(List<? super Integer> liste) {
      liste.add(1);
      liste.add(2);
      // // liste.add(new Number(…)) ❌ nicht erlaubt, weil nur Integer oder Untertypen
  }

  List<Number> numListe = new ArrayList<>();
  List<Object> objListe = new ArrayList<>();

  // Contravarianz: ? super Integer erlaubt Number und Object
  fügeIntsHinzu(numListe); // erlaubt, Number ist Super von Integer
  fügeIntsHinzu(objListe);// erlaubt, Object ist Super von Integer

  // fügeIntsHinzu(new ArrayList<String>()); // ❌ String ist nicht Super von Integer
  ```

  Hier ist `List<Number>` und `List<Object>` jeweils Subtyp von `List<? super Integer>`.
  Man kann also jede Liste, deren Elementtyp „mindestens Integer als Untertyp besitzt“, mit `Integer` befüllen.

* **Beispiel aus JDK: `Comparator<? super T>`**

  ```java
  List<Integer> liste = List.of(3,1,2);
  Comparator<Number> cmpNum = (a,b) → Double.compare(a.doubleValue(), b.doubleValue());
  Collections.sort(liste, cmpNum);
  // Comparator<Number> ist Subtyp von Comparator<? super Integer>, weil Number Super von Integer ist
  ```

  Der `Comparator<Number>` kann problemlos `Integer`-Objekte vergleichen, weil `Integer <: Number`. Daher ist `Comparator<Number>` kontravariant in Bezug auf `Comparator<Integer>`: ein „größerer“ (weiter oberer) Typ `Number` kann angewendet werden, wo `Comparator<Integer>` erwartet wird.

---

## 4. Invarianz, Covarianz & Contravarianz im Vergleich

| Begriff           | Kurzdefinition                                                                                                 | Java-Notation       | Lese-/Schreiboperationen erlaubt?                                                                                                                 |
| ----------------- | -------------------------------------------------------------------------------------------------------------- | ------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Invarianz**     | Keine automatische Untertyp-Beziehung zwischen `Container<S>` und `Container<T>`, selbst wenn `S <: T`         | `List<T>`           | - Lesen von `T` erlaubt<br>- Schreiben von `T` erlaubt                                                                                            |
| **Covarianz**     | `S <: T` ⇒ `Container<S> <: Container<T>` (Untertyp-Beziehung bleibt erhalten, wenn `S` Untertyp von `T` ist). | `List<? extends T>` | - Lesen von `T` (bzw. Untertyp) erlaubt<br>- Schreiben **nicht** erlaubt (außer `null`), da Typ nicht genau feststeht                             |
| **Contravarianz** | `S <: T` ⇒ `Container<T> <: Container<S>` (Untertyp-Beziehung kehrt sich um).                                  | `List<? super T>`   | - Lesen liefert nur `Object` (oder das nächstobere Typergebnis), da exakter Typ unspezifisch.<br>- Schreiben von `T` (oder Untertyp) ist erlaubt. |

* **Wann in Java üblich?**

  * **Invarianz**: Standard bei sämtlichen generischen Klassen/Interfaces (z. B. `List<String>` ≠ `List<Object>`).
  * **Covarianz**: Realisiert über `<? extends T>`, wenn man Objekte aus einem generischen Container lesen will.
  * **Contravarianz**: Realisiert über `<? super T>`, wenn man Objekte in einen generischen Container einfügen will.

---

## 5. Warum das wichtig ist

1. **Typ-Sicherheit**

   * Covariante Container erlauben das Lesen (Producer), aber verhindern, dass man falsche Objekte hineinschreibt.
   * Kontravariante Container erlauben das Schreiben (Consumer) eines bestimmten Typs, verhindern aber unsichere Leseoperationen (man erhält nur `Object` oder einen sehr allgemeinen Typ).

2. **Flexibilität**

   * Entwickler können APIs entwerfen, die sowohl Untertypen als auch Obertypen akzeptieren, ohne den strengen Generics-Typen zu verletzen.
   * Beispiel: Eine Methode, die alle Elemente einer Liste von Unters `T` summiert, kann als `List<? extends Number>` definiert werden, um `List<Integer>`, `List<Double>` usw. zu unterstützen.

3. **Typinferenz und Lesbarkeit**

   * Durch gezielten Einsatz von `extends` und `super` bleiben dieselben Methoden für verschiedene konkrete Typvarianten nutzbar, statt mehrere Überladungen zu schreiben.
   * Invarianten Typen sind streng, oft zu restriktiv; Wildcards lösen typische Probleme bei API-Designs.

---

## 6. Zusammenfassung

* **Invarianz:**

  > **`Container<T>`** ist nur kompatibel mit **`Container<T>`**.
  > Es existiert keine automatische Subtyping-Beziehung zwischen `Container<UnterTyp>` und `Container<OberTyp>`.

* **Covarianz:**

  > **`Container<? extends T>`** akzeptiert alle `Container<S>`, bei denen `S` Untertyp von `T` ist.
  > → Man kann aus dem Container Objekte entnehmen, darf aber nichts hineinschreiben.

* **Contravarianz:**

  > **`Container<? super T>`** akzeptiert alle `Container<S>`, bei denen `T` Untertyp von `S` ist (also `S` Obertyp von `T`).
  > → Man kann Objekte vom Typ `T` (oder Untertypen) hineinlegen, darf jedoch keine spezifischeren Typinformationen lesen (nur `Object` o. Ä.).

Die **PECS-Regel** („Producer Extends, Consumer Super“) fasst das Entscheidende zusammen:

1. **Producer Extends:** Wenn ein Container ausschließlich „produziert“ (lädt Objekte, die man lesen will), dann `<? extends T>`.
2. **Consumer Super:** Wenn ein Container ausschließlich „konsumiert“ (man legt Objekte vom Typ T hinein), dann `<? super T>`.

Damit lässt sich in Java präzise steuern, wo Typsicherheit gewährleistet bleibt, und wo man flexibel mit Unter- bzw. Obertypen arbeiten kann.
