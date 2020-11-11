/*  
 * 
 * This file is part of the Open Deduction Proof Editor.
 * 
 * @author Originally created for GraPE by Max Schaefer
 *
 */
package odpe.frontend.syntax;

import odpe.frontend.model.InferenceSystem;

public abstract class BinaryOperator extends Operator {
    
    protected OperandPosition leftOperand, rightOperand;
    protected boolean associative, commutative;
    
    protected BinaryOperator(String id, InferenceSystem sys,
            int precedence, 
            OperandPosition left, OperandPosition right, 
            boolean assoc, boolean comm) {
        super(id, sys, precedence);
        this.leftOperand = left;
        this.rightOperand = right;
        this.associative = assoc;
        this.commutative = comm;
    }
    
    public OperandPosition getLeftOperandType() {
        return leftOperand;
    }
    
    public OperandPosition getRightOperandType() {
        return rightOperand;
    }
    
    public boolean isAssociative() {
        return associative;
    }
    
    public boolean isCommutative() {
        return commutative;
    }

    public int getOperandPrecedence(int i) {
        if(i == 0)
            return leftOperand.getPrecedence();
        return rightOperand.getPrecedence();
    }
    
}
