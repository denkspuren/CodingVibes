import java.util.*;
import java.util.function.*;

public class SimpleIntStream {
    // Ein Supplier liefert bei Bedarf einen frischen Iterator über die Basis-Daten + alle verketteten Stages
    private final Supplier<PrimitiveIterator.OfInt> iteratorSupplier;

    private SimpleIntStream(Supplier<PrimitiveIterator.OfInt> supplier) {
        this.iteratorSupplier = supplier;
    }

    // ─── Quell-Stage ("Head") ─────────────────────────────────────────────────

    /** Erzeugt einen Stream mit den Werten [start, end) */
    public static SimpleIntStream range(int start, int end) {
        return new SimpleIntStream(() -> new PrimitiveIterator.OfInt() {
            private int curr = start;
            @Override public boolean hasNext() { return curr < end; }
            @Override public int nextInt() { return curr++; }
        });
    }

    /** Erzeugt einen Stream mit den Werten [start, end] */
    public static SimpleIntStream rangeClosed(int start, int end) {
        return range(start, end + 1);
    }

    /** Erzeugt einen Stream aus den angegebenen Werten */
    public static SimpleIntStream of(int... values) {
        return new SimpleIntStream(() -> new PrimitiveIterator.OfInt() {
            private int index = 0;
            @Override public boolean hasNext() { return index < values.length; }
            @Override public int nextInt() { return values[index++]; }
        });
    }

    /** Wandelt einen Iterator in einen Stream */
    public static SimpleIntStream of(PrimitiveIterator.OfInt it) {
        return new SimpleIntStream(() -> it);
    }

    /** Leerer Stream */
    public static SimpleIntStream empty() {
        return of();
    }

    /** Unendlicher Stream: f(seed), f(f(seed)), … */
    public static SimpleIntStream iterate(int seed, IntUnaryOperator f) {
        return new SimpleIntStream(() -> new PrimitiveIterator.OfInt() {
            private int curr = seed;
            private boolean first = true;
            @Override public boolean hasNext() { return true; }
            @Override public int nextInt() {
                if (first) { first = false; return curr; }
                return curr = f.applyAsInt(curr);
            }
        });
    }

    /** Unendlicher Stream: wiederholt Werte aus dem Supplier */
    public static SimpleIntStream generate(IntSupplier supplier) {
        return new SimpleIntStream(() -> new PrimitiveIterator.OfInt() {
            @Override public boolean hasNext() { return true; }
            @Override public int nextInt() { return supplier.getAsInt(); }
        });
    }

    // ─── Intermediate-Stages ────────────────────────────────────────────────

    /** Filter: behält nur diejenigen ints, für die predicate true ist */
    public SimpleIntStream filter(IntPredicate predicate) {
        return new SimpleIntStream(() -> new PrimitiveIterator.OfInt() {
            final PrimitiveIterator.OfInt it = iteratorSupplier.get();
            private int next;
            private boolean hasNextComputed = false;

            private void computeNext() {
                while (it.hasNext()) {
                    int v = it.nextInt();
                    if (predicate.test(v)) {
                        next = v;
                        hasNextComputed = true;
                        return;
                    }
                }
                hasNextComputed = false;
            }

            @Override
            public boolean hasNext() {
                if (!hasNextComputed) computeNext();
                return hasNextComputed;
            }

            @Override
            public int nextInt() {
                if (!hasNext()) throw new NoSuchElementException();
                hasNextComputed = false;
                return next;
            }
        });
    }

    /** Skip: überspringt n Elemente */
    public SimpleIntStream skip(long n) {
        return new SimpleIntStream(() -> new PrimitiveIterator.OfInt() {
            final PrimitiveIterator.OfInt it = iteratorSupplier.get();
            private long counter = n;
            private boolean hasSkipped = false;

            private void goSkipping() {            
                while (counter-- > 0 && it.hasNext()) it.nextInt();
                hasSkipped = true;
            }

            @Override public boolean hasNext() {
                if (!hasSkipped) goSkipping();
                return it.hasNext();
            }

            @Override public int nextInt() {
                return it.next();
            }
        });
    }

    /** Limit: behält nur die ersten n Elemente */
    public SimpleIntStream limit(long n) {
        return new SimpleIntStream(() -> new PrimitiveIterator.OfInt() {
            final PrimitiveIterator.OfInt it = iteratorSupplier.get();
            private long counter = 0;

            @Override public boolean hasNext() {
                return counter < n && it.hasNext();
            }

            @Override public int nextInt() {
                counter++;
                return it.next();
            }
        });
    }

    /** Map: wendet mapper auf jedes Element an */
    public SimpleIntStream map(IntUnaryOperator mapper) {
        return new SimpleIntStream(() -> new PrimitiveIterator.OfInt() {
            final PrimitiveIterator.OfInt it = iteratorSupplier.get();
            @Override public boolean hasNext() { return it.hasNext(); }
            @Override public int nextInt()  { return mapper.applyAsInt(it.nextInt()); }
        });
    }

    public <T> SimpleStream<T> mapToObj(IntFunction<T> mapper) {
        return SimpleStream.of(new Iterator<T>() {
            final PrimitiveIterator.OfInt it = iteratorSupplier.get();
            @Override public boolean hasNext() { return it.hasNext(); }
            @Override public T next() { return mapper.apply(it.nextInt()); }
        });
    }

    /** Peek: verarbeite jedes Element mit action */
    public SimpleIntStream peek(IntConsumer action) {
        return map(n -> { action.accept(n); return n; });
    }

    // ─── Terminal-Stages ────────────────────────────────────────────────────

    /** Reduce: kombiniert mit identity beginnend alle Element mit op zu einem einzigen Ergebnis */
    public int reduce(int identity, IntBinaryOperator op) {
        PrimitiveIterator.OfInt it = iteratorSupplier.get();
        if (!it.hasNext()) return identity;
        int result = identity;
        while (it.hasNext()) {
            result = op.applyAsInt(result, it.nextInt());
        }
        return result;
    }

    /** Reduce: kombiniert alle Elemente mit op zu einem einzigen Ergebnis */
    public OptionalInt reduce(IntBinaryOperator op) {
        PrimitiveIterator.OfInt it = iteratorSupplier.get();
        if (!it.hasNext()) return OptionalInt.empty();
        int result = it.nextInt();
        while (it.hasNext()) {
            result = op.applyAsInt(result, it.nextInt());
        }
        return OptionalInt.of(result);
    }

    /** ForEach: verarbeitet jedes Element mit action */
    public void forEach(IntConsumer action) { peek(action).reduce((a, b) -> a); }

    /** Sum: Addiert alle Elemente */
    public int sum() { return reduce(0, (a, b) -> a + b); }

    /** Prod: Multipliziert alle Elemente */
    public int prod() { return reduce(1, (a, b) -> a * b); }

    /** Count: Zählt die Anzahl der Elemente */
    public int count() { return map(n -> 1).sum(); }

    /** Average: Mittelwert aller Elemente (ineffiziente Umsetzung) */
    public OptionalDouble average() {
        int count = count();
        if (count == 0) { return OptionalDouble.empty(); }
        return OptionalDouble.of((double) sum() / count);
    }
    public OptionalDouble average2() {
        record Pair(int value, int count) {}
        Pair result = mapToObj(i -> new Pair(i, 1)).
                      reduce(new Pair(0, 0), (a, b) -> new Pair(a.value + b.value, a.count + 1));
        if (result.count == 0) { return OptionalDouble.empty(); }
        return OptionalDouble.of((double) result.value / result.count);
    }

    // FindFirst: Liefere erstes Element im Stream aus */
    public OptionalInt findFirst() { return limit(1).reduce((a, b) -> a); }

    // ─── Test / Beispiel ────────────────────────────────────────────────────

    public static void main(String[] args) {
        // Beispiel: Quadrate gerader Zahlen von 0 bis 9 aufsummieren
        OptionalInt sumOfSquares = SimpleIntStream
            .range(0, 10)                     // 0,1,2,…,9
            .filter(n -> n % 2 == 0)          // 0,2,4,6,8
            .map(n -> n * n)                  // 0,4,16,36,64
            .reduce((a, b) -> a + b);         // 0+4+16+36+64 = 120

        sumOfSquares.ifPresentOrElse(
            v -> System.out.println("Summe der Quadrate: " + v),
            () -> System.out.println("Kein Wert!")
        );

        int sumOfSquares2 = SimpleIntStream
            .range(0, 10)                     // 0,1,2,…,9
            .filter(n -> n % 2 == 0)          // 0,2,4,6,8
            .map(n -> n * n)                  // 0,4,16,36,64
            .sum();                           // 0+4+16+36+64 = 120
        System.out.println("Summe der Quadrate: " + sumOfSquares2);
    }
}
