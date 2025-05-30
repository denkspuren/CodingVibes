
/**
 * Die Infrastruktur zum Testen aus dem `CustomGatherer` und `assertBool` ist
 * etwas Overkill, aber ein schöner Anwendungsfall, um sich mit den neuen
 * Gatherern für Streams zu befassen.
 * 
 * Tests laufen mit Java 24 or höher; in Java 24 preview features aktivieren.
 * 
 * java --enable-preview StreamTest.java
 *  
 */
public class CustomGatherer {
    private static class Store {
        private Object value, tmp = null;
        boolean isEmpty() { return value == null; }
        void set(Object value) { this.value = value; }
        Object getAndClear() { tmp = value; value = null; return tmp; }
        // Ein netter, aber unleserlicher "Hack"
        // Object getAndClear() { return (tmp = value) == null ? null : ((value = null) == null ? tmp : tmp); }       

    }

    /**
     * Gatherer, der:
     * - Boolean-Objekte durchleitet
     * - andere Objekte paarweise zu new Object[]{first, second} zusammenfasst
     * - bei Restobjekten einen Fehler wirft
     */
    public static Gatherer<Object, ?, Object> pairingNonBoolean() {
        return Gatherer.ofSequential(
            Store::new,

            (Store memory, Object element, Gatherer.Downstream<? super Object> downstream) -> {
                if (element instanceof Boolean) {
                    if (memory.isEmpty()) downstream.push(element);
                    else throw new IllegalStateException("Ungepaartes Element übrig: " + memory.getAndClear()); 
                    return true;
                }
                if (memory.isEmpty()) memory.set(element);
                else downstream.push(new Object[]{memory.getAndClear(), element});
                return true;
            },

            (Store memory, Gatherer.Downstream<? super Object> downstream) -> {
                if (!memory.isEmpty())
                    throw new IllegalStateException("Ungepaartes Element übrig: " + memory.getAndClear());
            }
        );
    }
}

// assertTrue and assertFalse make your tests somewhat more convenient
boolean assertBool(Boolean criteria, String intent, Object... values) {
    if (values.length == 0) throw new IllegalArgumentException("assertBool requires at least three args");
    System.out.println("- " + intent + ":");
    AtomicInteger counter = new AtomicInteger(1);
    boolean result =
        Arrays.stream(values).
            gather(CustomGatherer.pairingNonBoolean()).
            peek(val -> IO.println("" + counter.getAndIncrement() + ". " +
                (val instanceof Object[] pair
                    ? pair[0] + " equals " + pair[1]
                    : val))).
            map(val -> (val instanceof Object[] pair)
                ? pair[0].equals(pair[1])
                : val).
            allMatch(res -> criteria == (boolean)res);
    if (!result) throw new AssertionError("❌ TEST " + (counter.get() - 1) + " FAILED");
    return result;
}

boolean assertTrue(String intent, Object... values) { return assertBool(Boolean.TRUE, intent, values); }
boolean assertTrue(Object... values) { return assertTrue("assertTrue", values); }
boolean assertFalse(String intent, Object... values) { return assertBool(Boolean.FALSE, intent, values); }
boolean assertFalse(Object... values) { return assertFalse("assertFalse", values); }

void main() {
    println("🔎 TESTING: -- Use Java 24 or higher. For Java 24 '--enable-preview' is required.");

    assertTrue("Interplay of SimpleStream and SimpleIntStream via count",
        SimpleStream.of("A", "B", "C").count() == 3,
        Stream.of("A", "B", "C").count() == 3,
        SimpleStream.<String>of().count() == 0,
        Stream.<String>of().count() == 0);

    assertTrue("Interplay of SimpleIntStream and SimpleStream via average",
        SimpleIntStream.of(2,4,3).average(), OptionalDouble.of(3.0),
        IntStream.of(2,4,3).average(),       OptionalDouble.of(3.0),
        SimpleStream.of("A", "B", "C").count() == 3,
        SimpleStream.of("A", "C").count() == 2,
        SimpleIntStream.of().average(),   OptionalDouble.empty(),
        IntStream.empty().average(),         OptionalDouble.empty());

    println("✅ All tests passed!");
}

