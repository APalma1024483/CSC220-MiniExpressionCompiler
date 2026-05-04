import java.util.ArrayList;
import java.util.List;

// PART 1 - Lexical Analyzer (Tokenizer)
// Turns the input string into a list of tokens (numbers, + - * /, parens).
public class Lexer {

    // A token is just a small piece of the input, like "3" or "+".
    public static class Token {
        public String value;        // the actual text, e.g. "3" or "+"
        public Token(String value) { this.value = value; }
        public String toString() { return value; }
    }

    public static List<Token> tokenize(String input) {
        List<Token> tokens = new ArrayList<>();
        int i = 0;

        while (i < input.length()) {
            char c = input.charAt(i);

            // skip spaces
            if (c == ' ' || c == '\t') {
                i++;
            }
            // a number: read all the digits in a row
            else if (Character.isDigit(c)) {
                String number = "";
                while (i < input.length() && Character.isDigit(input.charAt(i))) {
                    number += input.charAt(i);
                    i++;
                }
                tokens.add(new Token(number));
            }
            // an operator or paren
            else if (c == '+' || c == '-' || c == '*' || c == '/'
                  || c == '(' || c == ')') {
                tokens.add(new Token("" + c));
                i++;
            }
            // anything else is an error
            else {
                throw new RuntimeException("Illegal character: " + c);
            }
        }

        return tokens;
    }
}
