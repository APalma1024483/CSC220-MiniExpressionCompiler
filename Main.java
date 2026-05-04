import java.util.List;
import java.util.Scanner;

// PART 5 - Trace Output and Entry Point
// Reads an expression and prints the tokens, parse result, tree, and answer.
public class Main {

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        System.out.println("Mini Expression Compiler (type 'quit' to exit)");

        while (true) {
            System.out.print("Expression: ");
            String line = input.nextLine().trim();
            if (line.equals("quit")) break;
            if (line.equals("")) continue;

            run(line);
            System.out.println();
        }
        input.close();
    }

    public static void run(String expr) {
        // 1) Tokenize
        List<Lexer.Token> tokens;
        try {
            tokens = Lexer.tokenize(expr);
        } catch (RuntimeException e) {
            System.out.println("Lexer error: " + e.getMessage());
            return;
        }
        System.out.println("Tokens: " + tokens);

        // 2) Parse (validate grammar)
        Parser parser = new Parser(tokens);
        if (!parser.parse()) {
            System.out.println("Parse Result: FAILURE");
            System.out.println("Error: " + parser.error);
            return;
        }
        System.out.println("Parse Result: SUCCESS");

        // 3) Build the tree
        ASTBuilder builder = new ASTBuilder(tokens);
        ASTBuilder.Node tree = builder.build();

        // 4) Print the tree
        System.out.println("Parse Tree:");
        printTree(tree, 0);

        // 5) Evaluate
        try {
            int result = Evaluator.evaluate(tree);
            System.out.println("Evaluation Result: " + result);
        } catch (RuntimeException e) {
            System.out.println("Evaluation error: " + e.getMessage());
        }
    }

    // Prints the tree indented, deeper nodes get more spaces.
    // Right child first, then root, then left child gives a sideways tree.
    private static void printTree(ASTBuilder.Node node, int depth) {
        if (node == null) return;
        printTree(node.right, depth + 1);
        for (int i = 0; i < depth; i++) System.out.print("    ");
        System.out.println(node.value);
        printTree(node.left, depth + 1);
    }
}
