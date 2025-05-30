// Runs with Java 24 or higher; in Java 24 preview features must be enabled
//
// java --enable-preview StreamTest.java

// assertTrue and assertFalse make your tests somewhat more convenient
boolean assertBool(Boolean criteria, String intent, Object... values) {
    if (values.length == 0) throw new IllegalArgumentException("assertBool requires at least three args");
    int count = 0;
    boolean compareWithNextValue = false;
    System.out.print("- " + intent + ":");
    for (int i = 0; i < values.length; i++) {
        if (values[i] instanceof Boolean) {
            System.out.print(" " + ++count);
            compareWithNextValue = false;
            if (!criteria.equals(values[i])) {
                System.out.println(" ❌");
                throw new AssertionError(intent);
            }
            continue;
        }
        if (!compareWithNextValue) {
            compareWithNextValue = true;
            continue;
        }
        // Now, we must compare storedFirstValue with actual value
        System.out.print(" " + ++count);
        if (values[i - 1] == null) {
            System.out.println(" ❌");
            throw new AssertionError("In pairwise comparisons first value must not be null!");
        }
        boolean comparison = values[i - 1].equals(values[i]);
        compareWithNextValue = false;
        if (!criteria.equals(comparison)) {
            System.out.println(" ❌");
            throw new AssertionError(intent);
        }
    }
    System.out.println(" 🆗");
    return true; 
}

boolean assertTrue(String intent, Object... values) { return assertBool(Boolean.TRUE, intent, values); }
boolean assertTrue(Object... values) { return assertTrue("assertTrue", values); }
boolean assertFalse(String intent, Object... values) { return assertBool(Boolean.FALSE, intent, values); }
boolean assertFalse(Object... values) { return assertFalse("assertFalse", values); }

void main() {
    println("🔎 TESTING: -- Use Java 24 or higher");

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

