/* Bubblesort an IntStream (>= JDK24)

Enable preview features when using JDK24

JShell: jshell --enable-preview -R-ea
Java  : java --enable-preview -ea bubblesortIntStream.java
*/
import java.util.concurrent.atomic.AtomicBoolean;

record Result(boolean changed, IntStream ints) {}

Result bubble(IntStream numbers) {
    final AtomicBoolean hasChanged = new AtomicBoolean(false);
    final IntStream.Builder result = IntStream.builder();
    OptionalInt lastInt = numbers.reduce((x, y) -> {
        if (x > y) {
            hasChanged.set(true);
            result.accept(y);
            return x;
        } else {
            result.accept(x);
            return y;
        }
    });
    lastInt.ifPresent(i -> result.accept(i));
    return new Result(hasChanged.get(), result.build());
}

IntStream bubblesort(IntStream numbers) {
    Result r = new Result(true, numbers);
    while (r.changed())
        r = bubble(r.ints());
    return r.ints();
}

void main() {
    try {
        assert false;
        IO.println("WARNING: To run tests, enable assertions with -ea");
    } catch (AssertionError e) {
        IO.println("Running tests ...");
    }
    assert Arrays.equals(bubblesort(IntStream.of()).toArray(), new int[]{});
    assert Arrays.equals(bubblesort(IntStream.of(1)).toArray(), new int[]{1});
    assert Arrays.equals(bubblesort(IntStream.of(1, 2)).toArray(), new int[]{1, 2});
    assert Arrays.equals(bubblesort(IntStream.of(2, 1)).toArray(), new int[]{1, 2});
    assert Arrays.equals(bubblesort(IntStream.of(1, 2, 3)).toArray(), new int[]{1, 2, 3});
    assert Arrays.equals(bubblesort(IntStream.of(1, 3, 2)).toArray(), new int[]{1, 2, 3});
    assert Arrays.equals(bubblesort(IntStream.of(2, 1, 3)).toArray(), new int[]{1, 2, 3});
    assert Arrays.equals(bubblesort(IntStream.of(2, 3, 1)).toArray(), new int[]{1, 2, 3});
    assert Arrays.equals(bubblesort(IntStream.of(3, 1, 2)).toArray(), new int[]{1, 2, 3});
    assert Arrays.equals(bubblesort(IntStream.of(3, 2, 1)).toArray(), new int[]{1, 2, 3});
    assert ((BooleanSupplier)() -> { IO.println("✅ All tests passed!"); return true; }).getAsBoolean();
}

