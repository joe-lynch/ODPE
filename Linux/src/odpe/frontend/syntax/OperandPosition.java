/*  
 * 
 * This file is part of the Open Deduction Proof Editor.
 * 
 * @author Originally created for GraPE by Max Schaefer
 *
 */
package odpe.frontend.syntax;

import odpe.frontend.model.InferenceSystem;


public abstract class OperandPosition extends Operator {
	
	protected OperandPosition(String id, InferenceSystem sys, 
			int prec) {
		super(id, sys, prec);
	}
	
}
