/*  
 * 
 * This file is part of the Open Deduction Proof Editor.
 * 
 * @author It was originally created for GraPE by Max Schaefer
 *
 */
package odpe.frontend.view;

public abstract class ViewableObject {
    
    public abstract WidgetView getGraphicalView();
    
    public abstract String getTextInputView();
    
    public abstract String getTextOutputView();
    
}
