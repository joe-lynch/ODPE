/*  
 * 
 * This file is part of the Open Deduction Proof Editor.
 * 
 * @author Originally created for GraPE by Max Schaefer
 *
 */
package odpe.frontend.model;

public class DescriptionFileException extends Exception {

    private static final long serialVersionUID = 1L;
    
    public DescriptionFileException(String msg) {
        super(msg);
    }
    
    public DescriptionFileException(String msg, Exception cause) {
        super(msg, cause);
        
    }
    
}
