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
import odpe.frontend.view.InferenceFork;
import odpe.frontend.view.WidgetView;

public class InferenceRule extends Rule {
    
    private String tex;
    
    public InferenceRule(String id, String name, InferenceSystem sys, String tex, boolean defaultEnabled, boolean upDirection) {
        super(id, name, sys, defaultEnabled, upDirection);
    }
    
    public WidgetView draw(Node n, WidgetView[] rands) {
        return new InferenceFork(getName(), rands);
    }

    public String prettyprint(Node n, String[] rands) {
        return "#<derivation>";
    }

    public int getPrecedence() {
        return 0;
    }

    public int getOperandPrecedence(int i) {
        return -1;
    }

}
