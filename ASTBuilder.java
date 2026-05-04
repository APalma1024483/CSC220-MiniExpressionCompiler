import java.util.List;

/**
 * PART 3 - Syntax Tree Builder (AST Generator)
 *
 * Takes the validated token list from the Parser and builds an Abstract
 * Syntax Tree using the same recursive-descent grammar:
 *
 *   E  -> T E'
 *   E' -> + T E' | - T E' | epsilon
 *   T  -> F T'
 *   T' -> * F T' | / F T' | epsilon
 *   F  -> ( E ) | number | - F     // unary minus
 *
 * The AST node classes (ASTNode + NumberNode, BinaryOpNode, UnaryOpNode)
 * are defined as nested classes here so Part 3 is a single .java file.
 *
 * Throws RuntimeException with a descriptive message on invalid input.
 */
public class ASTBuilder {

    // -------------------------------------------------------------------------
    // Nested AST node classes
    // -------------------------------------------------------------------------

    /**
     * Base class for all AST nodes. Provides the indented-tree printer
     * used in the trace output (Part 5).
     */
    public static abstract class ASTNode {
        /** Returns a multi-line indented-tree string starting at this node. */
        public String toTreeString() {
            StringBuilder sb = new StringBuilder();
            buildTree(sb, "", true);
            return sb.toString();
        }

        /**
         * Public entry point for callers (like Main) that want to print a
         * subtree with a custom prefix and last-child flag.
         */
        public void appendTree(StringBuilder sb, String prefix, boolean isLast) {
            buildTree(sb, prefix, isLast);
        }

        /** Internal helper: prefix is the indentation, isLast picks the connector. */
        protected abstract void buildTree(StringBuilder sb, String prefix, boolean isLast);
    }

    /** AST leaf node for a numeric literal. */
    public static class NumberNode extends ASTNode {
        public final double value;

        public NumberNode(double value) {
            this.value = value;
        }

        // Show whole-number doubles without the ".0" (e.g. 3.0 -> "3")
        private String displayValue() {
            if (value == Math.floor(value) && !Double.isInfinite(value)) {
                return String.valueOf((long) value);
            }
            return String.valueOf(value);
        }

        @Override
        protected void buildTree(StringBuilder sb, String prefix, boolean isLast) {
            sb.append(prefix);
            sb.append(isLast ? "\\-- " : "|-- ");
            sb.append(displayValue()).append("\n");
        }

        @Override
        public String toString() {
            return displayValue();
        }
    }

    /** AST internal node for a binary operator (+, -, *, /). */
    public static class BinaryOpNode extends ASTNode {
        public final String operator;
        public final ASTNode left;
        public final ASTNode right;

        public BinaryOpNode(String operator, ASTNode left, ASTNode right) {
            this.operator = operator;
            this.left = left;
            this.right = right;
        }

        @Override
        protected void buildTree(StringBuilder sb, String prefix, boolean isLast) {
            sb.append(prefix);
            sb.append(isLast ? "\\-- " : "|-- ");
            sb.append(operator).append("\n");

            String childPrefix = prefix + (isLast ? "    " : "|   ");
            left.buildTree(sb, childPrefix, false);   // left  child: not last
            right.buildTree(sb, childPrefix, true);   // right child: last
        }

        @Override
        public String toString() {
            return operator;
        }
    }

    /** AST node for a unary operator (currently just unary minus). */
    public static class UnaryOpNode extends ASTNode {
        public final String operator;
        public final ASTNode operand;

        public UnaryOpNode(String operator, ASTNode operand) {
            this.operator = operator;
            this.operand = operand;
        }

        @Override
        protected void buildTree(StringBuilder sb, String prefix, boolean isLast) {
            sb.append(prefix);
            sb.append(isLast ? "\\-- " : "|-- ");
            sb.append("unary").append(operator).append("\n");

            String childPrefix = prefix + (isLast ? "    " : "|   ");
            operand.buildTree(sb, childPrefix, true);
        }

        @Override
        public String toString() {
            return "unary" + operator;
        }
    }

    // -------------------------------------------------------------------------
    // Builder state
    // -------------------------------------------------------------------------

    private final List<Lexer.Token> tokens;
    private int pos;

    public ASTBuilder(List<Lexer.Token> tokens) {
        this.tokens = tokens;
        this.pos = 0;
    }

    /**
     * Builds the AST from the token list.
     * @return root ASTNode
     * @throws RuntimeException if the token stream is syntactically invalid
     */
    public ASTNode build() {
        if (tokens.isEmpty()) {
            throw new RuntimeException("Empty input: nothing to parse");
        }
        ASTNode root = parseE();
        if (pos < tokens.size()) {
            Lexer.Token leftover = tokens.get(pos);
            throw new RuntimeException(
                "Unexpected token '" + leftover.value
              + "' at position " + leftover.position);
        }
        return root;
    }

    // -------------------------------------------------------------------------
    // Grammar rules (each returns the subtree it built)
    // -------------------------------------------------------------------------

    // E -> T E'
    private ASTNode parseE() {
        ASTNode left = parseT();
        return parseEPrime(left);
    }

    // E' -> + T E' | - T E' | epsilon
    private ASTNode parseEPrime(ASTNode left) {
        if (pos >= tokens.size()) return left;
        Lexer.Token t = tokens.get(pos);
        if (t.type == Lexer.TokenType.PLUS || t.type == Lexer.TokenType.MINUS) {
            pos++;
            ASTNode right = parseT();
            ASTNode node = new BinaryOpNode(t.value, left, right);
            return parseEPrime(node); // left-associative: fold and recurse
        }
        return left; // epsilon
    }

    // T -> F T'
    private ASTNode parseT() {
        ASTNode left = parseF();
        return parseTPrime(left);
    }

    // T' -> * F T' | / F T' | epsilon
    private ASTNode parseTPrime(ASTNode left) {
        if (pos >= tokens.size()) return left;
        Lexer.Token t = tokens.get(pos);
        if (t.type == Lexer.TokenType.STAR || t.type == Lexer.TokenType.SLASH) {
            pos++;
            ASTNode right = parseF();
            ASTNode node = new BinaryOpNode(t.value, left, right);
            return parseTPrime(node); // left-associative
        }
        return left; // epsilon
    }

    // F -> ( E ) | number | - F
    private ASTNode parseF() {
        if (pos >= tokens.size()) {
            int errPos = tokens.isEmpty() ? 0 : tokens.get(tokens.size() - 1).position + 1;
            throw new RuntimeException("Unexpected end of input at position " + errPos);
        }
        Lexer.Token t = tokens.get(pos);

        if (t.type == Lexer.TokenType.LPAREN) {
            pos++; // consume '('
            ASTNode inner = parseE();
            expectRParen();
            return inner;

        } else if (t.type == Lexer.TokenType.NUMBER) {
            pos++;
            return new NumberNode(Double.parseDouble(t.value));

        } else if (t.type == Lexer.TokenType.MINUS) {
            // unary minus: e.g. -3, -(2+1)
            pos++;
            ASTNode operand = parseF();
            return new UnaryOpNode("-", operand);

        } else {
            throw new RuntimeException(
                "Unexpected token '" + t.value + "' at position " + t.position);
        }
    }

    /** Consume ')' or throw a clear error message. */
    private void expectRParen() {
        if (pos >= tokens.size()) {
            int errPos = tokens.isEmpty() ? 0 : tokens.get(tokens.size() - 1).position + 1;
            throw new RuntimeException(
                "Expected ')' but reached end of input at position " + errPos);
        }
        Lexer.Token t = tokens.get(pos);
        if (t.type != Lexer.TokenType.RPAREN) {
            throw new RuntimeException(
                "Expected ')' but got '" + t.value + "' at position " + t.position);
        }
        pos++;
    }
}
