/*  
 * 
 * This file is part of the Open Deduction Proof Editor.
 * 
 * @author Originally created for GraPE by Max Schaefer
 *
 */
package odpe.frontend.syntax;

import odpe.frontend.model.InferenceSystem;

public abstract class Rule extends SyntaxElement {

    private String name;
    private boolean defaultEnabled;
    private boolean upDirection;

    protected Rule(String id, String name, InferenceSystem sys, boolean defaultEnabled, boolean upDirection) {
        super(id, sys);
        this.name = name;
        this.defaultEnabled = defaultEnabled;
        this.upDirection = upDirection;
    }
    
    public boolean isDefaultEnabled() {
        return defaultEnabled;
    }
    
    public String getName() {
        return name;
    }

    public boolean isUpDirection(){
        return upDirection;
    }

}
