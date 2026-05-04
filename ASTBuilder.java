import java.util.List;

// PART 3 - Syntax Tree Builder (AST Generator)
// Builds an expression tree from the tokens.
// Each node is either:
//   - a number (left and right are null), or
//   - an operator with left/right children
public class ASTBuilder {

    // One simple Node class for everything in the tree.
    public static class Node {
        public String value;     // the number text or the operator ("+", "-", etc.)
        public Node left;        // left child  (null for numbers)
        public Node right;       // right child (null for numbers and unary minus)

        public Node(String value, Node left, Node right) {
            this.value = value;
            this.left = left;
            this.right = right;
        }
    }

    private List<Lexer.Token> tokens;
    private int pos;

    public ASTBuilder(List<Lexer.Token> tokens) {
        this.tokens = tokens;
        this.pos = 0;
    }

    public Node build() {
        return parseE();
    }

    // E -> T (+|-) T ...
    private Node parseE() {
        Node left = parseT();
        while (pos < tokens.size()
            && (tokens.get(pos).value.equals("+") || tokens.get(pos).value.equals("-"))) {
            String op = tokens.get(pos).value;
            pos++;
            Node right = parseT();
            left = new Node(op, left, right);
        }
        return left;
    }

    // T -> F (*|/) F ...
    private Node parseT() {
        Node left = parseF();
        while (pos < tokens.size()
            && (tokens.get(pos).value.equals("*") || tokens.get(pos).value.equals("/"))) {
            String op = tokens.get(pos).value;
            pos++;
            Node right = parseF();
            left = new Node(op, left, right);
        }
        return left;
    }

    // F -> ( E ) | number | -F
    private Node parseF() {
        String t = tokens.get(pos).value;

        if (t.equals("(")) {
            pos++;                  // eat (
            Node inside = parseE();
            pos++;                  // eat )
            return inside;
        }
        else if (t.equals("-")) {   // unary minus, e.g. -3
            pos++;
            Node operand = parseF();
            // store as a node with operator "-" and only a right child
            return new Node("-", null, operand);
        }
        else {                      // a number
            pos++;
            return new Node(t, null, null);
        }
    }
}
