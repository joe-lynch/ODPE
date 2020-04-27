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

public abstract class Operator extends SyntaxElement {
	
	protected int precedence;
	
	protected Operator(String id, InferenceSystem sys, 
			int precedence) {
		super(id, sys);
		this.precedence = precedence;
	}
	
	public int getPrecedence() {
		return precedence;
	}
	
	public abstract Node parse(Lexer l, Parser p) 
		throws ParseError;

}
