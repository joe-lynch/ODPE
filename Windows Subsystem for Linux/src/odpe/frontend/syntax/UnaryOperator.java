/*  
 * 
 * This file is part of the Open Deduction Proof Editor.
 * 
 * @author Originally created for GraPE by Max Schaefer
 *
 */
package odpe.frontend.syntax;

import odpe.frontend.model.InferenceSystem;

public abstract class UnaryOperator extends Operator {
    
    protected OperandPosition operandType;
    
    protected UnaryOperator(String id, InferenceSystem sys, 
            int prec, OperandPosition type) {
        super(id, sys, prec);
        this.operandType = type;
    }
    
    public OperandPosition getOperandType() {
        return operandType;
    }
    
    public int getOperandPrecedence(int i) {
        return operandType.getPrecedence();
    }

}
