/*  
 * 
 * This file is part of the Open Deduction Proof Editor.
 * 
 * @author Originally created for GraPE by Max Schaefer
 *
 */
package odpe.frontend.syntax;

import odpe.frontend.ast.Node;
import odpe.frontend.model.InferenceSystem;
import odpe.frontend.view.WidgetView;

public class SingletonOperandPosition extends OperandPosition {
	
	public SingletonOperandPosition(InferenceSystem sys, 
			int prec) {
		super("singleton-operand", sys, prec);
	}

	public Node parse(Lexer l, Parser p) 
			throws ParseError {
		return p.parse(l, precedence);
	}

	public WidgetView draw(Node n, WidgetView[] rands) {
		return rands[0];
	}

	public String prettyprint(Node n, String[] rands) {
		return rands[0];
	}

	public String texify(Node n, String[] rands) {
		return rands[0];
	}

	public int getOperandPrecedence(int i) {
		return precedence;
	}

}
