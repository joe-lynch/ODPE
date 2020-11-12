/*  
 * 
 * This file is part of the Open Deduction Proof Editor.
 * 
 * @author It was originally created for GraPE by Max Schaefer
 *
 */

package odpe.frontend.view;

import odpe.frontend.ast.Selection;

public class SelectionInfo {
    
    public Selection selection;
    public int startchar;
    public int endchar;
    
    public SelectionInfo(Selection sel, int s, int e) {
        this.selection = sel;
        this.startchar = s;
        this.endchar = e;
    }
    
    public void shift(int offset) {
        this.startchar += offset;
        this.endchar += offset;
    }

}
