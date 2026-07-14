# Thesis examples, adapted to our Datalog engine

These are the example programs from *"Adding Stratified Negation to a Rust-based
Datalog Engine utilizing Magic Sets"* (Jäger, THM 2026), rewritten for our
Java engine (`Datalog.java`).

## What was changed during adaptation

- Negation `!p(...)` → `not p(...)` (our surface syntax).
- Inline `?- query.` goals were removed from the program bodies (our parser
  keeps queries in the REPL, not in files) and moved into a `// REPL query:`
  header comment together with the expected answer.
- Comments `%`/`//` → `//`; string constants stay quoted; symbolic constants
  stay lowercase.

The **Magic-Sets–transformed** listings from the thesis are deliberately *not*
reproduced as runnable programs, with one instructive exception (`11b`): those
transformed programs encode a specific query for demand-driven evaluation, and
the negation variants are intentionally unstratifiable — which is precisely the
problem the thesis solves with Supplementary Soft Stratification. Our engine has
no magic-sets machinery, so it evaluates the **original** (untransformed)
programs, which are ordinary stratified Datalog and produce the correct answers.

## How to run

    java Datalog.java thesis_examples/07_derived_negation_filter.dl
    ?- safe(1, Y).

(In a JRE-only environment this runs via single-file source launch; with a JDK,
`javac Datalog.java` then `java Datalog thesis_examples/<file>.dl`.)

## Index (all answers verified against the engine)

| File | Thesis source | REPL query | Expected answer |
|------|---------------|------------|-----------------|
| `01_transitive_closure.dl` | Listing 2 | `reach(a, X).` | b, c, d |
| `02_grandparent.dl` | Sec. 2.3 | `grandparent(X, Y).` | (alice,charlie), (alice,david) |
| `03_scc_pathfinding.dl` | Listing 3 | `long_path(X, Y).` | (1,3), (2,4), (1,4) |
| `04_unstratifiable.dl` | Listing 5 | — | **rejected** (negative cycle) |
| `05_safe_antijoin.dl` | Sec. 3.3 / Table 6 | `unpaid(X).` | bob |
| `05b_unsafe_rejected.dl` | Listing 6 | — | **rejected** (unsafe rule) |
| `06_reachability_negation.dl` | Listing 7 | `unreachable(X, Y).` | (a,a),(b,a),(b,b),(c,a),(c,b),(c,c) |
| `07_derived_negation_filter.dl` | Listing 13 | `safe(1, Y).` | 2, 3  (correctly **not** 4) |
| `08_asymmetric_edb_negation.dl` | Example 1 / Listing 17 | `asym(X, Y).` | (1,3), (3,4) |
| `09_known_unknown.dl` | Example 2 / Listing 19 | `unknown(X).` | 2, 4 |
| `10_safe_reachability.dl` | Example 3 / Listing 21 | `reach(1, Y).` | 2, 3 |
| `11_three_antijoin_guards.dl` | Example 4 / Listing 23 | `p(A, B, C).` | (no solutions) |
| `11b_smst_transformed_rejected.dl` | Listing 24 | — | **rejected** (the ordering problem) |
| `12_benchmark_chain.dl` | Listings 29/30 | `path(0, Y).` / `safe_path(3, Y).` | 1..10 / (none) |

## Running the regression tests

Each `.dl` file carries machine-readable directives as `//`-comments (invisible
to the program parser):

    //test <query> => <expected>     run the query, compare the solution SET
    //reject                         the program must be rejected (unsafe / unstratifiable)

`<expected>` is `empty`, `true`, `false`, or a set of solutions separated by `;`,
each solution a comma list of `Var=Value` (e.g. `X=1,Y=3 ; X=2,Y=4`). The
comparison is order-independent, so answer ordering never causes a spurious fail.

On macOS, double-click **`run_tests.command`** in Finder (or run it from a
terminal). If double-click is blocked by Gatekeeper, run:

    bash run_tests.command

Equivalently, invoke the engine's test mode directly:

    java Datalog.java --test *.dl          # single-file source launch (JRE is enough)
    # or, with a JDK:
    javac Datalog.java && java Datalog --test *.dl

The runner prints one PASS/FAIL line per query, a summary, and exits non-zero if
anything fails (suitable for CI). If the exec bit was lost on download, restore
it with `chmod +x run_tests.command`.

## Notes on two examples worth a closer look

- **`07`** is the thesis's central motivating case (a derived negation filter
  over a recursive predicate). The thesis shows that SMST fires the anti-join
  too early and wrongly derives `safe(1,4)`. Plain stratified evaluation — what
  our engine does — computes `reach` and `blocked` to their fixpoints first, so
  the anti-join sees a complete `blocked` and the answer is exactly `{2, 3}`.
- **`11b`** is the SMST-transformed Kerisit program. After the transformation a
  magic-seed edge closes a cycle through a negation edge, so our engine rejects
  it as unstratifiable. That rejection is a faithful illustration of *why*
  combining SMST with negation is hard — and what Supplementary Soft
  Stratification is designed to make correct.
