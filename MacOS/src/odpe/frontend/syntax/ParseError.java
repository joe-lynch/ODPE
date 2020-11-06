/*  
 * 
 * This file is part of the Open Deduction Proof Editor.
 * 
 * @author Originally created for GraPE by Max Schaefer
 *
 */
package odpe.frontend.syntax;

public class ParseError extends Exception {

	private static final long serialVersionUID = 1L;

	public ParseError(String msg) {
		super(msg);
	}

}
