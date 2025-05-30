

// assertTrue and assertFalse make your tests somewhat more convenient
boolean assertBool(Boolean criteria, String intent, Object... values) {
    if (values.length == 0) throw new IllegalArgumentException("assertBool requires at least three args");
    int count = 0;
    print("- " + intent + ":");
    for (Object value : values) {
        if (value instanceof Boolean && !value.equals(criteria))
            throw new AssertionError(intent);
        print(" " + ++count);
    }
    println(" 🆗");
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
        SimpleIntStream.of(2,4,3).average().equals(OptionalDouble.of(3.0)),
        IntStream.of(2,4,3).average().equals(OptionalDouble.of(3.0)),
        SimpleIntStream.empty().average().equals(OptionalDouble.empty()),
        IntStream.empty().average().equals(OptionalDouble.empty()));

    println("✅ All tests passed!");
}

