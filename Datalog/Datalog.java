import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A compact Datalog engine with:
 *   - stratified negation (naive bottom-up fixpoint per stratum),
 *   - built-in comparisons  =  !=  <  <=  >  >=   over numbers and strings,
 *   - arithmetic expressions  + - * / %  inside comparisons,
 *     where "=" doubles as a binder, e.g.  M = N + 1  binds M,
 *   - a recursive-descent parser for program files, and
 *   - a REPL for queries.
 *
 * Surface syntax (Prolog-flavoured):
 *   Uppercase / leading _  ->  variable          (X, _Y)
 *   lowercase ident        ->  symbolic constant / predicate name   (a, edge)
 *   "..."                  ->  string constant
 *   123 / 1.5              ->  numeric constant   (long / double)
 *   //                     ->  line comment
 *   clause                 ->  head.   or   head :- body.
 *   query (REPL)           ->  goal1, goal2, ... .        optional leading ?-
 *
 * Run:  java Datalog.java program.dl   (loads, evaluates, then REPL on stdin)
 */
public final class Datalog {

    // ======================================================================
    //  Program model
    // ======================================================================
    sealed interface Term permits Var, Const, BinExpr {}
    record Var(String name)            implements Term { @Override public String toString() { return name; } }
    record Const(Object value)         implements Term { @Override public String toString() { return String.valueOf(value); } }
    record BinExpr(char op, Term left, Term right) implements Term {
        @Override public String toString() { return "(" + left + " " + op + " " + right + ")"; }
    }

    record Atom(String predicate, List<Term> args) {   // relational atom: args are Var | Const only
        Atom { args = List.copyOf(args); }
        int arity() { return args.size(); }
        boolean isGround() { return args.stream().allMatch(t -> t instanceof Const); }
        @Override public String toString() {
            return args.isEmpty() ? predicate
                : predicate + args.stream().map(Object::toString).collect(Collectors.joining(", ", "(", ")"));
        }
    }

    sealed interface Literal permits Rel, Cmp {}
    record Rel(Atom atom, boolean negated) implements Literal {
        @Override public String toString() { return (negated ? "not " : "") + atom; }
    }
    record Cmp(String op, Term left, Term right) implements Literal {
        @Override public String toString() { return left + " " + op + " " + right; }
    }

    record Rule(Atom head, List<Literal> body) {
        Rule { body = List.copyOf(body); }
        boolean isFact() { return body.isEmpty(); }
        @Override public String toString() {
            return isFact() ? head + "."
                : head + " :- " + body.stream().map(Object::toString).collect(Collectors.joining(", ")) + ".";
        }
    }

    static List<Var> varsOf(Term t) {
        return switch (t) {
            case Var v   -> List.of(v);
            case Const c -> List.of();
            case BinExpr(char op, Term l, Term r) -> {
                var out = new ArrayList<Var>(); out.addAll(varsOf(l)); out.addAll(varsOf(r)); yield out;
            }
        };
    }

    // ======================================================================
    //  Database (ground facts indexed by predicate)
    // ======================================================================
    static final class Database {
        private final Map<String, Set<Atom>> byPred = new HashMap<>();
        boolean add(Atom a)      { return byPred.computeIfAbsent(a.predicate(), p -> new LinkedHashSet<>()).add(a); }
        boolean contains(Atom a) { return byPred.getOrDefault(a.predicate(), Set.of()).contains(a); }
        Set<Atom> facts(String p){ return byPred.getOrDefault(p, Set.of()); }
        Map<String, Set<Atom>> byPredicate() { return byPred; }
    }

    // ======================================================================
    //  Evaluation of terms / comparisons
    // ======================================================================
    /** Evaluate an expression under a substitution; returns null if any variable is unbound. */
    static Const evalExpr(Term t, Map<Var, Const> s) {
        return switch (t) {
            case Const c -> c;
            case Var v   -> s.get(v);
            case BinExpr(char op, Term l, Term r) -> {
                Const lv = evalExpr(l, s), rv = evalExpr(r, s);
                yield (lv == null || rv == null) ? null : new Const(arith(op, lv.value(), rv.value()));
            }
        };
    }

    static Object arith(char op, Object a, Object b) {
        if (!(a instanceof Number na) || !(b instanceof Number nb))
            throw new RuntimeException("Arithmetic requires numbers: " + a + " " + op + " " + b);
        boolean bothLong = na instanceof Long && nb instanceof Long;
        if ((op == '/' || op == '%') && (bothLong ? na.longValue() == 0L : nb.doubleValue() == 0.0))
            throw new RuntimeException((op == '/' ? "division" : "modulo") + " by zero");
        if (bothLong) {
            long x = na.longValue(), y = nb.longValue();
            return switch (op) {
                case '+' -> x + y;  case '-' -> x - y;  case '*' -> x * y;  case '%' -> x % y;
                case '/' -> (x % y == 0) ? (Object) (x / y) : (double) x / y;
                default  -> throw new RuntimeException("bad operator " + op);
            };
        }
        double x = na.doubleValue(), y = nb.doubleValue();
        return switch (op) {
            case '+' -> x + y;  case '-' -> x - y;  case '*' -> x * y;  case '/' -> x / y;  case '%' -> x % y;
            default  -> throw new RuntimeException("bad operator " + op);
        };
    }

    static boolean numericEquals(Object a, Object b) {
        if (a instanceof Number na && b instanceof Number nb) return na.doubleValue() == nb.doubleValue();
        return Objects.equals(a, b);
    }
    static int order(Object a, Object b) {
        if (a instanceof Number na && b instanceof Number nb) return Double.compare(na.doubleValue(), nb.doubleValue());
        if (a instanceof String sa && b instanceof String sb) return sa.compareTo(sb);
        throw new RuntimeException("Cannot order values: " + a + " vs " + b);
    }
    static boolean compare(String op, Const l, Const r) {
        Object a = l.value(), b = r.value();
        return switch (op) {
            case "="  -> numericEquals(a, b);
            case "!=" -> !numericEquals(a, b);
            case "<"  -> order(a, b) < 0;
            case "<=" -> order(a, b) <= 0;
            case ">"  -> order(a, b) > 0;
            case ">=" -> order(a, b) >= 0;
            default   -> throw new RuntimeException("bad operator " + op);
        };
    }

    // ======================================================================
    //  Matching / substitution for relational atoms
    // ======================================================================
    static Atom apply(Atom a, Map<Var, Const> s) {
        List<Term> out = a.args().stream().map(t -> (Term) switch (t) {
            case Var vv  -> { Const c = s.get(vv); yield c != null ? c : vv; }
            case Const c -> c;
            case BinExpr be -> { Const c = evalExpr(be, s); yield c != null ? c : be; }
        }).toList();
        return new Atom(a.predicate(), out);
    }

    static Optional<Map<Var, Const>> match(Atom pattern, Atom fact, Map<Var, Const> subst) {
        if (!pattern.predicate().equals(fact.predicate()) || pattern.arity() != fact.arity())
            return Optional.empty();
        Map<Var, Const> r = new HashMap<>(subst);
        for (int i = 0; i < pattern.arity(); i++) {
            Const fc = (Const) fact.args().get(i); // facts are ground
            switch (pattern.args().get(i)) {
                case Const c -> { if (!c.equals(fc)) return Optional.empty(); }
                case Var vv  -> {
                    Const bound = r.get(vv);
                    if (bound == null) r.put(vv, fc);
                    else if (!bound.equals(fc)) return Optional.empty();
                }
                case BinExpr be -> throw new IllegalStateException("expression in relational position: " + be);
            }
        }
        return Optional.of(r);
    }

    // ======================================================================
    //  Body evaluation:  positive join  ->  = binding + comparisons  ->  NAF
    // ======================================================================
    static List<Map<Var, Const>> evalBody(List<Literal> body, Database db) {
        List<Map<Var, Const>> subs = new ArrayList<>();
        subs.add(new HashMap<>());

        for (Literal lit : body) {                              // 1) positive relational join
            if (lit instanceof Rel r && !r.negated()) {
                List<Map<Var, Const>> next = new ArrayList<>();
                for (Map<Var, Const> s : subs)
                    for (Atom f : db.facts(r.atom().predicate()))
                        match(r.atom(), f, s).ifPresent(next::add);
                subs = next;
            }
        }
        List<Cmp> cmps = new ArrayList<>();
        List<Rel> negs = new ArrayList<>();
        for (Literal lit : body) {
            if (lit instanceof Cmp c) cmps.add(c);
            else if (lit instanceof Rel r && r.negated()) negs.add(r);
        }
        List<Map<Var, Const>> out = new ArrayList<>();
        for (Map<Var, Const> s : subs) resolve(s, cmps, negs, db).ifPresent(out::add);
        return out;
    }

    /** Apply comparisons (with '=' as binder) and negation-as-failure to one substitution. */
    static Optional<Map<Var, Const>> resolve(Map<Var, Const> s0, List<Cmp> cmps, List<Rel> negs, Database db) {
        Map<Var, Const> s = new HashMap<>(s0);
        Set<Cmp> pending = new LinkedHashSet<>(cmps);
        boolean progress = true;
        while (progress) {
            progress = false;
            for (Iterator<Cmp> it = pending.iterator(); it.hasNext(); ) {
                Cmp c = it.next();
                Const lv = evalExpr(c.left(), s), rv = evalExpr(c.right(), s);
                if (lv != null && rv != null) {                 // fully ground -> filter
                    if (!compare(c.op(), lv, rv)) return Optional.empty();
                    it.remove(); progress = true;
                } else if (c.op().equals("=")) {                // try to bind a lone variable
                    if (c.left() instanceof Var v && lv == null && rv != null)      { s.put(v, rv); it.remove(); progress = true; }
                    else if (c.right() instanceof Var v && rv == null && lv != null) { s.put(v, lv); it.remove(); progress = true; }
                }
            }
        }
        if (!pending.isEmpty()) throw new IllegalStateException("Unresolved comparison(s): " + pending);
        for (Rel r : negs) {
            Atom g = apply(r.atom(), s);
            if (!g.isGround()) throw new IllegalStateException("Unsafe negation: " + r);
            if (db.contains(g)) return Optional.empty();
        }
        return Optional.of(s);
    }

    // ======================================================================
    //  Safety analysis
    // ======================================================================
    static void checkSafety(Rule r) {
        Set<Var> safe = r.body().stream()
            .filter(l -> l instanceof Rel rr && !rr.negated())
            .flatMap(l -> ((Rel) l).atom().args().stream())
            .filter(t -> t instanceof Var).map(t -> (Var) t)
            .collect(Collectors.toCollection(HashSet::new));

        List<Cmp> eqs = r.body().stream()
            .filter(l -> l instanceof Cmp c && c.op().equals("=")).map(l -> (Cmp) l).toList();
        boolean progress = true;
        while (progress) {
            progress = false;
            for (Cmp c : eqs) {
                if (c.left()  instanceof Var v && !safe.contains(v) && varsOf(c.right()).stream().allMatch(safe::contains)) { safe.add(v); progress = true; }
                if (c.right() instanceof Var v && !safe.contains(v) && varsOf(c.left()).stream().allMatch(safe::contains))  { safe.add(v); progress = true; }
            }
        }
        for (Term t : r.head().args())
            if (t instanceof Var v && !safe.contains(v))
                throw new IllegalArgumentException("Unsafe rule (head variable " + v + " unbound): " + r);
        for (Literal l : r.body())
            switch (l) {
                case Rel rr -> {
                    if (rr.negated())
                        for (Term t : rr.atom().args())
                            if (t instanceof Var v && !safe.contains(v))
                                throw new IllegalArgumentException("Unsafe rule (negated variable " + v + " unbound): " + r);
                }
                case Cmp c -> {
                    for (Var v : varsOf(c.left()))  if (!safe.contains(v)) throw new IllegalArgumentException("Unsafe rule (variable " + v + " in comparison unbound): " + r);
                    for (Var v : varsOf(c.right())) if (!safe.contains(v)) throw new IllegalArgumentException("Unsafe rule (variable " + v + " in comparison unbound): " + r);
                }
            }
    }

    // ======================================================================
    //  Stratification (only relational literals create dependencies)
    // ======================================================================
    static Map<String, Integer> stratify(List<Rule> rules) {
        Set<String> preds = new HashSet<>();
        for (Rule r : rules) {
            preds.add(r.head().predicate());
            for (Literal l : r.body()) if (l instanceof Rel rr) preds.add(rr.atom().predicate());
        }
        Map<String, Integer> stratum = new HashMap<>();
        preds.forEach(p -> stratum.put(p, 0));
        int n = preds.size();
        for (int pass = 0; pass <= n; pass++) {
            boolean changed = false;
            for (Rule r : rules) {
                String h = r.head().predicate();
                for (Literal l : r.body()) {
                    if (!(l instanceof Rel rr)) continue;
                    String b = rr.atom().predicate();
                    int need = rr.negated() ? stratum.get(b) + 1 : stratum.get(b);
                    if (stratum.get(h) < need) { stratum.put(h, need); changed = true; }
                }
            }
            if (!changed) return stratum;
        }
        throw new IllegalArgumentException("Program is not stratifiable: negation occurs inside a recursive cycle.");
    }

    // ======================================================================
    //  Bottom-up evaluation, stratum by stratum
    // ======================================================================
    static Database evaluate(List<Rule> rules) {
        rules.forEach(Datalog::checkSafety);
        Map<String, Integer> stratum = stratify(rules);
        int maxStratum = stratum.values().stream().max(Integer::compareTo).orElse(0);
        Map<Integer, List<Rule>> byStratum = rules.stream()
            .collect(Collectors.groupingBy(r -> stratum.get(r.head().predicate())));

        Database db = new Database();
        for (int s = 0; s <= maxStratum; s++) {
            List<Rule> layer = byStratum.getOrDefault(s, List.of());
            boolean changed = true;
            while (changed) {                                   // naive fixpoint
                changed = false;
                for (Rule r : layer)
                    for (Map<Var, Const> subst : evalBody(r.body(), db))
                        if (db.add(apply(r.head(), subst))) changed = true;
            }
        }
        return db;
    }

    // ======================================================================
    //  Lexer
    // ======================================================================
    enum T { IDENT, VAR, NUM, STR, LPAREN, RPAREN, COMMA, DOT, IMPLIES, QUERY, NOT, OP, PLUS, MINUS, STAR, SLASH, PERCENT, EOF }
    record Tok(T type, String text) {}

    static List<Tok> tokenize(String src) {
        List<Tok> out = new ArrayList<>();
        int i = 0, n = src.length();
        while (i < n) {
            char ch = src.charAt(i);
            if (Character.isWhitespace(ch)) { i++; continue; }
            if (ch == '/' && i + 1 < n && src.charAt(i + 1) == '/') { while (i < n && src.charAt(i) != '\n') i++; continue; }
            switch (ch) {
                case '(' -> { out.add(new Tok(T.LPAREN, "(")); i++; continue; }
                case ')' -> { out.add(new Tok(T.RPAREN, ")")); i++; continue; }
                case ',' -> { out.add(new Tok(T.COMMA, ",")); i++; continue; }
                case '.' -> { out.add(new Tok(T.DOT, ".")); i++; continue; }
                case '+' -> { out.add(new Tok(T.PLUS, "+")); i++; continue; }
                case '*' -> { out.add(new Tok(T.STAR, "*")); i++; continue; }
                case '/' -> { out.add(new Tok(T.SLASH, "/")); i++; continue; }
                case '%' -> { out.add(new Tok(T.PERCENT, "%")); i++; continue; }
                case '-' -> { out.add(new Tok(T.MINUS, "-")); i++; continue; }
                default -> { /* fall through to multi-char / literals below */ }
            }
            if (ch == ':' && i + 1 < n && src.charAt(i + 1) == '-') { out.add(new Tok(T.IMPLIES, ":-")); i += 2; continue; }
            if (ch == '?' && i + 1 < n && src.charAt(i + 1) == '-') { out.add(new Tok(T.QUERY, "?-")); i += 2; continue; }
            if (ch == '=') { out.add(new Tok(T.OP, "=")); i++; continue; }
            if (ch == '!' && i + 1 < n && src.charAt(i + 1) == '=') { out.add(new Tok(T.OP, "!=")); i += 2; continue; }
            if (ch == '<') {
                if (i + 1 < n && src.charAt(i + 1) == '=')      { out.add(new Tok(T.OP, "<="));  i += 2; }
                else if (i + 1 < n && src.charAt(i + 1) == '>') { out.add(new Tok(T.OP, "!="));  i += 2; }
                else                                            { out.add(new Tok(T.OP, "<"));   i++; }
                continue;
            }
            if (ch == '>') {
                if (i + 1 < n && src.charAt(i + 1) == '=') { out.add(new Tok(T.OP, ">=")); i += 2; }
                else                                       { out.add(new Tok(T.OP, ">"));  i++; }
                continue;
            }
            if (ch == '"') {
                StringBuilder sb = new StringBuilder(); i++;
                while (i < n && src.charAt(i) != '"') {
                    char c = src.charAt(i++);
                    if (c == '\\' && i < n) { char e = src.charAt(i++); sb.append(switch (e) { case 'n' -> '\n'; case 't' -> '\t'; default -> e; }); }
                    else sb.append(c);
                }
                if (i >= n) throw new RuntimeException("Unterminated string literal");
                i++; out.add(new Tok(T.STR, sb.toString())); continue;
            }
            if (Character.isDigit(ch)) {
                int s = i; while (i < n && Character.isDigit(src.charAt(i))) i++;
                if (i < n && src.charAt(i) == '.' && i + 1 < n && Character.isDigit(src.charAt(i + 1))) {
                    i++; while (i < n && Character.isDigit(src.charAt(i))) i++;
                }
                out.add(new Tok(T.NUM, src.substring(s, i))); continue;
            }
            if (Character.isLetter(ch) || ch == '_') {
                int s = i; while (i < n && (Character.isLetterOrDigit(src.charAt(i)) || src.charAt(i) == '_')) i++;
                String w = src.substring(s, i);
                if (w.equals("not")) out.add(new Tok(T.NOT, w));
                else if (Character.isUpperCase(w.charAt(0)) || w.charAt(0) == '_') out.add(new Tok(T.VAR, w));
                else out.add(new Tok(T.IDENT, w));
                continue;
            }
            throw new RuntimeException("Unexpected character '" + ch + "' at position " + i);
        }
        out.add(new Tok(T.EOF, ""));
        return out;
    }

    // ======================================================================
    //  Parser (recursive descent)
    // ======================================================================
    static final class Parser {
        private final List<Tok> toks;
        private int p = 0;
        Parser(String src) { this.toks = tokenize(src); }

        private Tok peek()          { return toks.get(p); }
        private Tok peek(int k)     { return toks.get(Math.min(p + k, toks.size() - 1)); }
        private Tok next()          { return toks.get(p++); }
        private boolean at(T t)     { return peek().type() == t; }
        private Tok expect(T t) {
            if (!at(t)) throw new RuntimeException("Expected " + t + " but found '" + peek().text() + "'");
            return next();
        }

        List<Rule> parseProgram() {
            List<Rule> rules = new ArrayList<>();
            while (!at(T.EOF)) {
                if (at(T.QUERY)) throw new RuntimeException("Queries (?-) belong in the REPL, not in a program file.");
                rules.add(parseClause());
            }
            return rules;
        }

        List<Literal> parseQuery() {
            if (at(T.QUERY)) next();
            List<Literal> body = parseBody();
            if (at(T.DOT)) next();
            expect(T.EOF);
            return body;
        }

        private Rule parseClause() {
            Atom head = parseAtom();
            List<Literal> body = List.of();
            if (at(T.IMPLIES)) { next(); body = parseBody(); }
            expect(T.DOT);
            return new Rule(head, body);
        }

        private List<Literal> parseBody() {
            List<Literal> lits = new ArrayList<>();
            lits.add(parseLiteral());
            while (at(T.COMMA)) { next(); lits.add(parseLiteral()); }
            return lits;
        }

        private Literal parseLiteral() {
            if (at(T.NOT)) { next(); return new Rel(parseAtom(), true); }
            if (at(T.IDENT) && peek(1).type() == T.LPAREN) return new Rel(parseAtom(), false);
            if (at(T.IDENT) && (peek(1).type() == T.COMMA || peek(1).type() == T.DOT || peek(1).type() == T.EOF))
                return new Rel(parseAtom(), false);                  // 0-ary predicate
            Term left = parseExpr();
            if (!at(T.OP)) throw new RuntimeException("Expected comparison operator but found '" + peek().text() + "'");
            String op = next().text();
            return new Cmp(op, left, parseExpr());
        }

        private Atom parseAtom() {
            String name = expect(T.IDENT).text();
            List<Term> args = new ArrayList<>();
            if (at(T.LPAREN)) {
                next();
                if (!at(T.RPAREN)) {
                    args.add(parseAtomTerm());
                    while (at(T.COMMA)) { next(); args.add(parseAtomTerm()); }
                }
                expect(T.RPAREN);
            }
            return new Atom(name, args);
        }

        private Term parseAtomTerm() {           // only Var | Const allowed in atom positions
            Tok t = peek();
            return switch (t.type()) {
                case VAR   -> { next(); yield new Var(t.text()); }
                case NUM   -> { next(); yield new Const(num(t.text())); }
                case STR   -> { next(); yield new Const(t.text()); }
                case IDENT -> { next(); yield new Const(t.text()); }
                default    -> throw new RuntimeException("Expected a term but found '" + t.text() + "'");
            };
        }

        private Term parseExpr() {
            Term left = parseTerm2();
            while (at(T.PLUS) || at(T.MINUS)) { char op = next().text().charAt(0); left = new BinExpr(op, left, parseTerm2()); }
            return left;
        }
        private Term parseTerm2() {
            Term left = parseFactor();
            while (at(T.STAR) || at(T.SLASH) || at(T.PERCENT)) { char op = next().text().charAt(0); left = new BinExpr(op, left, parseFactor()); }
            return left;
        }
        private Term parseFactor() {
            if (at(T.MINUS))  { next(); return new BinExpr('-', new Const(0L), parseFactor()); }
            if (at(T.LPAREN)) { next(); Term e = parseExpr(); expect(T.RPAREN); return e; }
            Tok t = peek();
            return switch (t.type()) {
                case NUM   -> { next(); yield new Const(num(t.text())); }
                case VAR   -> { next(); yield new Var(t.text()); }
                case STR   -> { next(); yield new Const(t.text()); }
                case IDENT -> { next(); yield new Const(t.text()); }
                default    -> throw new RuntimeException("Expected an expression but found '" + t.text() + "'");
            };
        }

        static Object num(String s) {
            return s.indexOf('.') >= 0 ? (Object) Double.parseDouble(s) : (Object) Long.parseLong(s);
        }
    }

    // ======================================================================
    //  REPL
    // ======================================================================
    static final class Session {
        List<Rule> program = new ArrayList<>();
        Database db = new Database();
        void recompute() {
            try { db = evaluate(program); }
            catch (Exception e) { System.out.println("Program error: " + e.getMessage()); }
        }
    }

    public static void main(String[] args) throws IOException {
        List<String> files = new ArrayList<>();
        boolean testMode = false;
        for (String a : args) {
            if (a.equals("--test")) testMode = true;
            else if (!a.startsWith("-")) files.add(a);
        }
        if (testMode) { runTests(files); return; }

        Session ses = new Session();
        for (String a : files) {
            String src = Files.readString(Path.of(a));
            ses.program.addAll(new Parser(src).parseProgram());
            System.out.println("Loaded " + a + " (" + ses.program.size() + " clauses total).");
        }
        ses.recompute();
        printStrata(ses);
        System.out.println("Datalog REPL - :help for commands, or type a query like  path(a, X).");
        repl(ses);
    }

    // ======================================================================
    //  Test runner:  java Datalog --test <files...>
    //  Directives embedded as //-comments (invisible to the program parser):
    //    //reject                       program must be rejected (unsafe / unstratifiable)
    //    //test <query> => <expected>   run query, compare solution SET (order-independent)
    //  <expected> is  empty | true | false  or  sol ; sol ; ...  with sol = Var=Val, Var=Val
    // ======================================================================
    static void runTests(List<String> files) throws IOException {
        int passed = 0, failed = 0;
        for (String file : files) {
            String src = Files.readString(Path.of(file));
            boolean expectReject = false;
            List<String> tests = new ArrayList<>();
            for (String raw : src.split("\n")) {
                String line = raw.strip();
                if (line.startsWith("//reject")) expectReject = true;
                else if (line.startsWith("//test")) tests.add(line.substring(6).strip());
            }
            System.out.println(file);

            List<Rule> program;
            try {
                program = new Parser(src).parseProgram();
            } catch (Exception e) {
                if (expectReject) { System.out.println("  PASS  rejected at parse: " + e.getMessage()); passed++; }
                else { System.out.println("  FAIL  parse error: " + e.getMessage()); failed++; }
                continue;
            }
            if (expectReject) {
                try { evaluate(program); System.out.println("  FAIL  expected rejection, but program was accepted"); failed++; }
                catch (Exception e) { System.out.println("  PASS  rejected: " + e.getMessage()); passed++; }
                continue;
            }
            Database db;
            try { db = evaluate(program); }
            catch (Exception e) { System.out.println("  FAIL  evaluation error: " + e.getMessage()); failed += Math.max(1, tests.size()); continue; }

            for (String d : tests) {
                int arrow = d.indexOf("=>");
                if (arrow < 0) { System.out.println("  FAIL  malformed //test (no =>): " + d); failed++; continue; }
                String querySrc = d.substring(0, arrow).strip();
                String expectedSrc = d.substring(arrow + 2).strip();
                try {
                    StringBuilder actual = new StringBuilder();
                    boolean ok = checkQuery(querySrc, expectedSrc, db, actual);
                    if (ok) { System.out.println("  PASS  " + querySrc + "  =>  " + expectedSrc); passed++; }
                    else { System.out.println("  FAIL  " + querySrc + "  expected [" + expectedSrc + "]  got [" + actual + "]"); failed++; }
                } catch (Exception e) { System.out.println("  FAIL  " + querySrc + "  error: " + e.getMessage()); failed++; }
            }
        }
        System.out.println();
        System.out.println("Summary: " + passed + " passed, " + failed + " failed");
        if (failed > 0) System.exit(1);
    }

    static boolean checkQuery(String querySrc, String expectedSrc, Database db, StringBuilder actualOut) {
        List<Literal> goal = new Parser(querySrc).parseQuery();
        LinkedHashSet<Var> vars = new LinkedHashSet<>();
        for (Literal l : goal) switch (l) {
            case Rel r -> { for (Term t : r.atom().args()) if (t instanceof Var v) vars.add(v); }
            case Cmp c -> { vars.addAll(varsOf(c.left())); vars.addAll(varsOf(c.right())); }
        }
        List<Map<Var, Const>> results = evalBody(goal, db);

        if (vars.isEmpty()) {                                   // boolean query
            boolean actualTrue = !results.isEmpty();
            actualOut.append(actualTrue ? "true" : "false");
            String e = expectedSrc.strip();
            if (e.equalsIgnoreCase("true")) return actualTrue;
            return !actualTrue;                                 // empty/false/none
        }
        Set<Map<String, Const>> actual = new HashSet<>();
        for (Map<Var, Const> s : results) {
            Map<String, Const> row = new HashMap<>();
            for (Var v : vars) row.put(v.name(), s.get(v));
            actual.add(row);
        }
        actualOut.append(renderRows(actual, vars));
        return actual.equals(parseExpected(expectedSrc));
    }

    static Set<Map<String, Const>> parseExpected(String e) {
        Set<Map<String, Const>> set = new HashSet<>();
        e = e.strip();
        if (e.isBlank() || e.equalsIgnoreCase("empty") || e.equalsIgnoreCase("none") || e.equalsIgnoreCase("false"))
            return set;
        for (String sol : e.split(";")) {
            if (sol.isBlank()) continue;
            Map<String, Const> row = new HashMap<>();
            for (String bind : sol.split(",")) {
                int eq = bind.indexOf('=');
                row.put(bind.substring(0, eq).strip(), constOf(bind.substring(eq + 1).strip()));
            }
            set.add(row);
        }
        return set;
    }

    static Const constOf(String tok) {
        Tok t = tokenize(tok).get(0);
        return switch (t.type()) {
            case NUM        -> new Const(Parser.num(t.text()));
            case STR, IDENT -> new Const(t.text());
            default         -> throw new RuntimeException("cannot parse expected value: " + tok);
        };
    }

    static String renderRows(Set<Map<String, Const>> rows, LinkedHashSet<Var> vars) {
        if (rows.isEmpty()) return "empty";
        List<String> order = vars.stream().map(Var::name).sorted().toList();
        return rows.stream()
            .map(r -> order.stream().map(n -> n + "=" + r.get(n)).collect(Collectors.joining(",")))
            .sorted().collect(Collectors.joining(" ; "));
    }

    static void repl(Session ses) {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        try {
            while (true) {
                System.out.print("?- "); System.out.flush();
                String line = in.readLine();
                if (line == null) break;
                line = line.trim();
                if (line.isEmpty()) continue;
                try {
                    if (line.startsWith(":")) { if (!command(line, ses)) break; }
                    else runQuery(line, ses);
                } catch (Exception e) { System.out.println("Error: " + e.getMessage()); }
            }
        } catch (IOException ignored) { }
        System.out.println("bye.");
    }

    static boolean command(String line, Session ses) throws IOException {
        String[] parts = line.split("\\s+", 2);
        String cmd = parts[0];
        String arg = parts.length > 1 ? parts[1].trim() : "";
        switch (cmd) {
            case ":help" -> printHelp();
            case ":quit", ":q", ":exit" -> { return false; }
            case ":load", ":consult" -> {
                ses.program = new ArrayList<>(new Parser(Files.readString(Path.of(arg))).parseProgram());
                ses.recompute();
                System.out.println("Loaded " + arg + " (" + ses.program.size() + " clauses).");
                printStrata(ses);
            }
            case ":add", ":assert" -> {
                List<Rule> rs = new Parser(arg.endsWith(".") ? arg : arg + ".").parseProgram();
                ses.program.addAll(rs); ses.recompute();
                System.out.println("Added " + rs.size() + " clause(s).");
            }
            case ":facts" -> {
                if (arg.isEmpty())
                    ses.db.byPredicate().keySet().stream().sorted()
                        .forEach(p -> ses.db.facts(p).stream().map(Atom::toString).sorted().forEach(a -> System.out.println("  " + a)));
                else
                    ses.db.facts(arg).stream().map(Atom::toString).sorted().forEach(a -> System.out.println("  " + a));
            }
            case ":rules"  -> ses.program.forEach(r -> System.out.println("  " + r));
            case ":strata" -> printStrata(ses);
            default -> System.out.println("Unknown command " + cmd + " (try :help)");
        }
        return true;
    }

    static void runQuery(String line, Session ses) {
        List<Literal> goal = new Parser(line).parseQuery();
        LinkedHashSet<Var> vars = new LinkedHashSet<>();
        for (Literal l : goal) switch (l) {
            case Rel r -> { for (Term t : r.atom().args()) if (t instanceof Var v) vars.add(v); }
            case Cmp c -> { vars.addAll(varsOf(c.left())); vars.addAll(varsOf(c.right())); }
        }
        List<Map<Var, Const>> results = evalBody(goal, ses.db);
        if (vars.isEmpty()) { System.out.println(results.isEmpty() ? "false." : "true."); return; }

        List<Var> order = new ArrayList<>(vars);
        LinkedHashSet<String> rows = new LinkedHashSet<>();
        for (Map<Var, Const> s : results)
            rows.add(order.stream().map(v -> v + " = " + (s.get(v) == null ? "_" : s.get(v))).collect(Collectors.joining(", ")));
        if (rows.isEmpty()) System.out.println("false.");
        else { rows.forEach(r -> System.out.println("  " + r)); System.out.println(rows.size() + " solution(s)."); }
    }

    static void printStrata(Session ses) {
        if (ses.program.isEmpty()) { System.out.println("(empty program)"); return; }
        try { System.out.println("Strata: " + new TreeMap<>(stratify(ses.program))); }
        catch (Exception e) { System.out.println("Strata: " + e.getMessage()); }
    }

    static void printHelp() {
        System.out.println("""
            Commands:
              :load <file>     load a program file (replaces current program)
              :add <clause>    assert a rule/fact, e.g.  :add ancestor(X,Y) :- parent(X,Y).
              :facts [pred]    list derived facts (optionally for one predicate)
              :rules           list the current program
              :strata          show the computed stratification
              :help            this text
              :quit            leave the REPL
            Query examples:
              path(a, X).            // find all X reachable from a
              even(N).               // all N with a fact even(N)
              X = 2 * 3 + 1.         // evaluate an expression
              path(a, c).            // yes/no query -> true. / false.
            Syntax: Uppercase = variable, lowercase = constant/predicate, // = comment.
            Built-ins: = != < <= > >= with arithmetic + - * / % (= also binds, e.g. M = N + 1).""");
    }
}
