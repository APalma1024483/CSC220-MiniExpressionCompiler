import java.util.List;

// PART 2 - Parser (Grammar-Based Validator)
// Checks that the tokens follow the grammar:
//   E -> E + T | E - T | T
//   T -> T * F | T / F | F
//   F -> ( E ) | number | -F     (unary minus)
//
// We use recursive descent. Returns true if valid, false otherwise.
public class Parser {

    private List<Lexer.Token> tokens;
    private int pos;
    public String error;   // error message if parse failed

    public Parser(List<Lexer.Token> tokens) {
        this.tokens = tokens;
        this.pos = 0;
        this.error = null;
    }

    public boolean parse() {
        if (tokens.size() == 0) {
            error = "Empty input";
            return false;
        }
        try {
            parseE();
            if (pos < tokens.size()) {
                error = "Unexpected token: " + tokens.get(pos).value;
                return false;
            }
            return true;
        } catch (RuntimeException e) {
            error = e.getMessage();
            return false;
        }
    }

    // E -> T (+|-) T (+|-) T ...
    private void parseE() {
        parseT();
        while (pos < tokens.size()
            && (tokens.get(pos).value.equals("+") || tokens.get(pos).value.equals("-"))) {
            pos++;        // eat the + or -
            parseT();
        }
    }

    // T -> F (*|/) F (*|/) F ...
    private void parseT() {
        parseF();
        while (pos < tokens.size()
            && (tokens.get(pos).value.equals("*") || tokens.get(pos).value.equals("/"))) {
            pos++;        // eat the * or /
            parseF();
        }
    }

    // F -> ( E ) | number | -F
    private void parseF() {
        if (pos >= tokens.size()) {
            throw new RuntimeException("Unexpected end of input");
        }
        String t = tokens.get(pos).value;

        if (t.equals("(")) {
            pos++;            // eat (
            parseE();
            if (pos >= tokens.size() || !tokens.get(pos).value.equals(")")) {
                throw new RuntimeException("Expected ')'");
            }
            pos++;            // eat )
        }
        else if (t.equals("-")) {     // unary minus
            pos++;
            parseF();
        }
        else if (isNumber(t)) {
            pos++;
        }
        else {
            throw new RuntimeException("Unexpected token: " + t);
        }
    }

    // helper: is this token text a number?
    private boolean isNumber(String s) {
        if (s.length() == 0) return false;
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i))) return false;
        }
        return true;
    }
}
