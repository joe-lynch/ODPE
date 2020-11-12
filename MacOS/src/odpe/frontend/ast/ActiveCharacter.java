/*  
 * 
 * This file is part of the Open Deduction Proof Editor.
 * 
 * @author Originally created for GraPE by Max Schaefer
 *
 */
package odpe.frontend.ast;


public class ActiveCharacter {
    
    public Node owner;
    public int oppos;
    
    public ActiveCharacter(Node owner, int oppos) {
        this.owner = owner;
        this.oppos = oppos;
    }
    
    public void trigger() {
        this.owner.swap(oppos);
    }
    
}
