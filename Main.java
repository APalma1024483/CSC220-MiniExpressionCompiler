import java.util.List;
import java.util.Scanner;

/**
 * PART 5 - Trace Output & Entry Point
 *
 * Drives the full compilation pipeline and prints a trace of each phase:
 *   1. Token stream
 *   2. Parse result (success / failure with error message)
 *   3. Expression tree (indented text format)
 *   4. Evaluated result
 *
 * Run from the command line:
 *   javac *.java
 *   java Main
 *
 * then type an expression when prompted, e.g.:  (3 + 2) * 5 - 1
 */
public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Mini Expression Compiler ===");
        System.out.println("Type 'quit' to exit.\n");

        while (true) {
            System.out.print("Expression: ");
            if (!scanner.hasNextLine()) break;
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("quit")) break;
            if (input.isEmpty()) continue;

            runCompiler(input);
            System.out.println();
        }

        scanner.close();
    }

    /**
     * Runs the full compiler pipeline on {@code input} and prints the trace.
     * Public so tests / other code can call it directly.
     */
    public static void runCompiler(String input) {
        System.out.println("----------------------------------------");

        // ------------------------------------------------------------------
        // PHASE 1: Lexical Analysis
        // ------------------------------------------------------------------
        List<Lexer.Token> tokens;
        try {
            Lexer lexer = new Lexer(input);
            tokens = lexer.tokenize();
        } catch (RuntimeException e) {
            System.out.println("Lexer Error: " + e.getMessage());
            return;
        }

        // Trace: token stream
        System.out.print("Tokens: [");
        for (int i = 0; i < tokens.size(); i++) {
            System.out.print(tokens.get(i));
            if (i < tokens.size() - 1) System.out.print(", ");
        }
        System.out.println("]");

        // ------------------------------------------------------------------
        // PHASE 2: Parsing (grammar validation)
        // ------------------------------------------------------------------
        Parser parser = new Parser(tokens);
        Parser.ParseResult parseResult = parser.parse();

        if (parseResult.success) {
            System.out.println("Parse Result: SUCCESS");
        } else {
            System.out.println("Parse Result: FAILURE");
            System.out.println("  Error: " + parseResult.errorMessage);
            return; // no tree or result to show for invalid input
        }

        // ------------------------------------------------------------------
        // PHASE 3: AST Construction
        // ------------------------------------------------------------------
        ASTBuilder.ASTNode ast;
        try {
            ASTBuilder builder = new ASTBuilder(tokens);
            ast = builder.build();
        } catch (RuntimeException e) {
            // Should not normally reach here if the parser already succeeded.
            System.out.println("AST Error: " + e.getMessage());
            return;
        }

        // Trace: expression tree
        System.out.println("Parse Tree:");
        System.out.println(treeRootString(ast));

        // ------------------------------------------------------------------
        // PHASE 4: Evaluation
        // ------------------------------------------------------------------
        try {
            Evaluator evaluator = new Evaluator();
            double result = evaluator.evaluate(ast);
            System.out.println("Evaluation Result: " + evaluator.formatResult(result));
        } catch (RuntimeException e) {
            System.out.println("Evaluation Error: " + e.getMessage());
        }
    }

    /**
     * Prints the root node without a leading connector, then delegates to
     * the normal tree-building logic for children. Output for (3 + 2) * 5 - 1:
     *
     *   -
     *   |-- *
     *   |   |-- +
     *   |   |   |-- 3
     *   |   |   \-- 2
     *   |   \-- 5
     *   \-- 1
     */
    private static String treeRootString(ASTBuilder.ASTNode root) {
        StringBuilder sb = new StringBuilder();

        if (root instanceof ASTBuilder.BinaryOpNode) {
            ASTBuilder.BinaryOpNode bin = (ASTBuilder.BinaryOpNode) root;
            sb.append(bin.operator).append("\n");
            bin.left.appendTree(sb, "", false);
            bin.right.appendTree(sb, "", true);

        } else if (root instanceof ASTBuilder.UnaryOpNode) {
            ASTBuilder.UnaryOpNode un = (ASTBuilder.UnaryOpNode) root;
            sb.append("unary").append(un.operator).append("\n");
            un.operand.appendTree(sb, "", true);

        } else {
            // NumberNode or anything else: just print it.
            sb.append(root.toString()).append("\n");
        }

        return sb.toString();
    }
}
