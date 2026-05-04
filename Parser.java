import java.util.List;

/**
 * PART 2 - Parser (Grammar-Based Validator)
 *
 * Validates that the token stream produced by the Lexer follows the grammar
 * (rewritten without left recursion so it works with recursive descent):
 *
 *   E  -> T E'
 *   E' -> + T E' | - T E' | epsilon
 *   T  -> F T'
 *   T' -> * F T' | / F T' | epsilon
 *   F  -> ( E ) | number | - F     // unary minus supported
 *
 * Returns a ParseResult: success, or failure with a clear error message.
 * ParseResult is defined as a nested class so Part 2 is a single .java file.
 */
public class Parser {

    // -------------------------------------------------------------------------
    // Nested type: ParseResult
    // -------------------------------------------------------------------------

    /** Outcome of a parse: either success, or failure with a message. */
    public static class ParseResult {
        public final boolean success;
        public final String errorMessage; // null on success

        public ParseResult(boolean success, String errorMessage) {
            this.success = success;
            this.errorMessage = errorMessage;
        }
    }

    // -------------------------------------------------------------------------
    // Parser state
    // -------------------------------------------------------------------------

    private final List<Lexer.Token> tokens;
    private int pos;

    public Parser(List<Lexer.Token> tokens) {
        this.tokens = tokens;
        this.pos = 0;
    }

    /**
     * Validates the token stream against the grammar.
     * @return ParseResult.success == true if input is well-formed.
     */
    public ParseResult parse() {
        if (tokens.isEmpty()) {
            return new ParseResult(false, "Empty input");
        }
        try {
            parseE();
            // After parsing the top-level expression there should be nothing left.
            if (pos < tokens.size()) {
                Lexer.Token unexpected = tokens.get(pos);
                return new ParseResult(false,
                    "Unexpected token '" + unexpected.value
                  + "' at position " + unexpected.position);
            }
            return new ParseResult(true, null);
        } catch (RuntimeException e) {
            return new ParseResult(false, e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /** Returns current token, or a synthetic EOF token if past end. */
    private Lexer.Token peek() {
        if (pos < tokens.size()) return tokens.get(pos);
        Lexer.Token last = tokens.isEmpty() ? null : tokens.get(tokens.size() - 1);
        int eofPos = (last != null) ? last.position + 1 : 0;
        return new Lexer.Token(Lexer.TokenType.EOF, "EOF", eofPos);
    }

    private Lexer.Token consume() {
        return tokens.get(pos++);
    }

    private void expect(Lexer.TokenType type) {
        Lexer.Token t = peek();
        if (t.type != type) {
            throw new RuntimeException(
                "Expected '" + tokenTypeName(type)
              + "' but got '" + t.value + "' at position " + t.position);
        }
        consume();
    }

    private String tokenTypeName(Lexer.TokenType type) {
        switch (type) {
            case LPAREN: return "(";
            case RPAREN: return ")";
            case NUMBER: return "number";
            default:     return type.toString();
        }
    }

    // -------------------------------------------------------------------------
    // Grammar rules
    // -------------------------------------------------------------------------

    // E -> T E'
    private void parseE() {
        parseT();
        parseEPrime();
    }

    // E' -> + T E' | - T E' | epsilon
    private void parseEPrime() {
        Lexer.Token t = peek();
        if (t.type == Lexer.TokenType.PLUS || t.type == Lexer.TokenType.MINUS) {
            consume();
            parseT();
            parseEPrime();
        }
        // else epsilon
    }

    // T -> F T'
    private void parseT() {
        parseF();
        parseTPrime();
    }

    // T' -> * F T' | / F T' | epsilon
    private void parseTPrime() {
        Lexer.Token t = peek();
        if (t.type == Lexer.TokenType.STAR || t.type == Lexer.TokenType.SLASH) {
            consume();
            parseF();
            parseTPrime();
        }
        // else epsilon
    }

    // F -> ( E ) | number | - F
    private void parseF() {
        Lexer.Token t = peek();
        if (t.type == Lexer.TokenType.LPAREN) {
            consume();
            parseE();
            expect(Lexer.TokenType.RPAREN);
        } else if (t.type == Lexer.TokenType.NUMBER) {
            consume();
        } else if (t.type == Lexer.TokenType.MINUS) {
            // unary minus, e.g. -3 or -(2+1)
            consume();
            parseF();
        } else {
            throw new RuntimeException(
                "Unexpected token '" + t.value + "' at position " + t.position);
        }
    }
}
