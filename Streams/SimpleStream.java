import java.util.*;
import java.util.function.*;

public class SimpleStream<T> {
    // Ein Supplier liefert bei Bedarf einen frischen Iterator über die Basis-Daten + alle verketteten Stages
    private final Supplier<Iterator<T>> iteratorSupplier;

    private SimpleStream(Supplier<Iterator<T>> supplier) {
        this.iteratorSupplier = supplier;
    }

    // ─── Quell-Stage ("Head") ─────────────────────────────────────────────────

    /** Erzeugt einen Stream aus den angegebenen Werten */
    @SafeVarargs public static <T> SimpleStream<T> of(T... values) {
        return new SimpleStream<>(() -> new Iterator<T>() {
            private int index = 0;
            @Override public boolean hasNext() { return index < values.length; }
            @Override public T next() { return values[index++]; }
        });
    }

    public static <T> SimpleStream<T> empty() {
        return SimpleStream.<T>of();
    }

    /** Unendlicher Stream: f(seed), f(f(seed)), … */
    public static <T> SimpleStream<T> iterate(T seed, UnaryOperator<T> f) {
        return new SimpleStream<>(() -> new Iterator<T>() {
            private T curr = seed;
            private boolean first = true;
            @Override public boolean hasNext() { return true; }
            @Override public T next() {
                if (first) { first = false; return curr; }
                return curr = f.apply(curr);
            }
        });
    }

    /** Unendlicher Stream: wiederholt Werte aus dem Supplier */
    public static <T> SimpleStream<T> generate(Supplier<T> supplier) {
        return new SimpleStream<>(() -> new Iterator<T>() {
            @Override public boolean hasNext() { return true; }
            @Override public T next() { return supplier.get(); }
        });
    }



    /** Filter: behält nur diejenigen Elemente, für die predicate true ist */
    public SimpleStream<T> filter(Predicate<T> predicate) { // public <T> SimpleStream<T> filter(...) FAILED!
        return new SimpleStream<>(() -> new Iterator<T>() {
            final Iterator<T> it = iteratorSupplier.get();
            private T next;
            private boolean hasNextComputed = false;

            private void computeNext() {
                while (it.hasNext()) {
                    T v = it.next();
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
            public T next() {
                if (!hasNext()) throw new NoSuchElementException();
                hasNextComputed = false;
                return next;
            }
        });
    }

    /** Skip: überspringt n Elemente */
    public SimpleStream<T> skip(long n) {
        return new SimpleStream<>(() -> new Iterator<T>() {
            final Iterator<T> it = iteratorSupplier.get();
            private long counter = n;
            private boolean hasSkipped = false;

            private void goSkipping() {            
                while (counter-- > 0 && it.hasNext()) it.next();
                hasSkipped = true;
            }

            @Override public boolean hasNext() {
                if (!hasSkipped) goSkipping();
                return it.hasNext();
            }

            @Override public T next() {
                return it.next();
            }
        });
    }

    /** Limit: behält nur die ersten n Elemente */
    public SimpleStream<T> limit(long n) {
        return new SimpleStream<>(() -> new Iterator<T>() {
            final Iterator<T> it = iteratorSupplier.get();
            private long counter = 0;

            @Override public boolean hasNext() {
                return counter < n && it.hasNext();
            }

            @Override public T next() {
                counter++;
                return it.next();
            }
        });
    }

    /** Map: wendet mapper auf jedes Element an */
    public <R> SimpleStream<R> map(Function<? super T, ? extends R> mapper) {
        return new SimpleStream<>(() -> new Iterator<R>() {
            final Iterator<T> it = iteratorSupplier.get();
            @Override public boolean hasNext() { return it.hasNext(); }
            @Override public R next()  { return mapper.apply(it.next()); }
        });
    }


}
