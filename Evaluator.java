// PART 4 - Evaluator
// Walks the tree and computes the answer.
public class Evaluator {

    public static int evaluate(ASTBuilder.Node node) {
        // a number: just return its value
        if (node.left == null && node.right == null) {
            return Integer.parseInt(node.value);
        }

        // unary minus: left is null, right has the operand
        if (node.left == null) {
            return -evaluate(node.right);
        }

        // binary operator: compute both sides, then combine
        int leftVal  = evaluate(node.left);
        int rightVal = evaluate(node.right);

        if (node.value.equals("+")) return leftVal + rightVal;
        if (node.value.equals("-")) return leftVal - rightVal;
        if (node.value.equals("*")) return leftVal * rightVal;
        if (node.value.equals("/")) {
            if (rightVal == 0) throw new RuntimeException("Division by zero");
            return leftVal / rightVal;
        }

        throw new RuntimeException("Unknown operator: " + node.value);
    }
}
