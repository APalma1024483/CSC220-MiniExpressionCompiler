/**
 * PART 4 - Evaluator
 *
 * Walks the AST produced by ASTBuilder in post-order
 * (left -> right -> root) and computes the numeric result.
 *
 * Uses double arithmetic throughout so decimals work; the printed
 * result is shown as an integer when there is no fractional part.
 *
 * Throws RuntimeException for division by zero or unknown node types.
 */
public class Evaluator {

    /**
     * Recursively evaluates the subtree rooted at {@code node}.
     */
    public double evaluate(ASTBuilder.ASTNode node) {
        if (node instanceof ASTBuilder.NumberNode) {
            // Leaf: return the literal value
            return ((ASTBuilder.NumberNode) node).value;

        } else if (node instanceof ASTBuilder.BinaryOpNode) {
            ASTBuilder.BinaryOpNode binOp = (ASTBuilder.BinaryOpNode) node;
            double left  = evaluate(binOp.left);
            double right = evaluate(binOp.right);

            switch (binOp.operator) {
                case "+": return left + right;
                case "-": return left - right;
                case "*": return left * right;
                case "/":
                    if (right == 0) {
                        throw new RuntimeException("Division by zero");
                    }
                    return left / right;
                default:
                    throw new RuntimeException(
                        "Unknown binary operator: " + binOp.operator);
            }

        } else if (node instanceof ASTBuilder.UnaryOpNode) {
            ASTBuilder.UnaryOpNode unary = (ASTBuilder.UnaryOpNode) node;
            double val = evaluate(unary.operand);
            if (unary.operator.equals("-")) {
                return -val;
            }
            throw new RuntimeException("Unknown unary operator: " + unary.operator);
        }

        throw new RuntimeException(
            "Unknown AST node type: " + node.getClass().getSimpleName());
    }

    /**
     * Formats the result for display: drops the ".0" on whole numbers
     * (e.g. 24.0 -> "24"), full precision otherwise.
     */
    public String formatResult(double result) {
        if (result == Math.floor(result) && !Double.isInfinite(result)) {
            return String.valueOf((long) result);
        }
        return String.valueOf(result);
    }
}
