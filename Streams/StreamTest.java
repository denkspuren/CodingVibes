void main() {
    assert SimpleStream.of("A", "B", "C").count() == 3;
    assert Stream.of("A", "B", "C").count() == 3;

    assert SimpleIntStream.of(2,4,3).average() == OptionalDouble.of(3.0);
    assert IntStream.of(2,4,3).average() == OptionalDouble.of(3.0);


}

