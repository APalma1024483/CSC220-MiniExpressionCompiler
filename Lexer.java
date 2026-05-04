import java.util.ArrayList;
import java.util.List;

/**
 * PART 1 - Lexical Analyzer (Tokenizer)
 *
 * Converts an input string into a list of valid tokens.
 * Recognized tokens:
 *   - Numbers (integers and decimals, e.g. 3, 42, 3.14)
 *   - Operators: +, -, *, /
 *   - Parentheses: ( )
 *
 * Whitespace is skipped. Illegal characters cause a RuntimeException
 * with the offending position so the user knows where the error is.
 *
 * The Token and TokenType types are defined as nested classes inside
 * this file so Part 1 lives in a single .java file.
 */
public class Lexer {

    // -------------------------------------------------------------------------
    // Nested types: TokenType + Token
    // -------------------------------------------------------------------------

    /** All kinds of tokens the lexer can produce. */
    public enum TokenType {
        NUMBER,   // e.g. 3, 42, 3.14
        PLUS,     // +
        MINUS,    // -
        STAR,     // *
        SLASH,    // /
        LPAREN,   // (
        RPAREN,   // )
        EOF       // end of input (synthetic, used by parser)
    }

    /** Single lexical token: kind, original text, and source position. */
    public static class Token {
        public final TokenType type;
        public final String value;
        public final int position; // index in the input string, for error messages

        public Token(TokenType type, String value, int position) {
            this.type = type;
            this.value = value;
            this.position = position;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    // -------------------------------------------------------------------------
    // Lexer state
    // -------------------------------------------------------------------------

    private final String input;
    private int pos;

    public Lexer(String input) {
        this.input = input;
        this.pos = 0;
    }

    /**
     * Scans the entire input and returns the list of tokens.
     * @throws RuntimeException on illegal character
     */
    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        while (pos < input.length()) {
            char c = input.charAt(pos);

            // Skip whitespace
            if (Character.isWhitespace(c)) {
                pos++;
                continue;
            }

            // Number (integer or decimal, including leading-dot like .5)
            if (Character.isDigit(c)
                || (c == '.' && pos + 1 < input.length()
                             && Character.isDigit(input.charAt(pos + 1)))) {
                int start = pos;
                while (pos < input.length()
                        && (Character.isDigit(input.charAt(pos)) || input.charAt(pos) == '.')) {
                    pos++;
                }
                tokens.add(new Token(TokenType.NUMBER, input.substring(start, pos), start));
                continue;
            }

            // Operators and parentheses
            int tokenPos = pos;
            switch (c) {
                case '+': tokens.add(new Token(TokenType.PLUS,   "+", tokenPos)); pos++; break;
                case '-': tokens.add(new Token(TokenType.MINUS,  "-", tokenPos)); pos++; break;
                case '*': tokens.add(new Token(TokenType.STAR,   "*", tokenPos)); pos++; break;
                case '/': tokens.add(new Token(TokenType.SLASH,  "/", tokenPos)); pos++; break;
                case '(': tokens.add(new Token(TokenType.LPAREN, "(", tokenPos)); pos++; break;
                case ')': tokens.add(new Token(TokenType.RPAREN, ")", tokenPos)); pos++; break;
                default:
                    throw new RuntimeException(
                        "Illegal character '" + c + "' at position " + pos);
            }
        }

        return tokens;
    }
}
