/*  
 * 
 * This file is part of the Open Deduction Proof Editor.
 * 
 * @author It was originally created for GraPE by Max Schaefer
 *
 */
package odpe.frontend.view;

public class SimpleViewableObject extends ViewableObject {

    private WidgetView graphical;
    private String textIn;
    private String textOut;
    private String tex;
    
    public SimpleViewableObject(WidgetView gv, String ti,
            String to, String tx) {
        this.graphical = gv;
        this.textIn = ti;
        this.textOut = to;
        this.tex = tx;
    }
    
    public SimpleViewableObject(String ti, String to, String tx) {
        this(new MarkableString(to), ti, to, tx);
    }
    
    public SimpleViewableObject(String str) {
        this(new MarkableString(str), str, str, str);
    }
    
    public WidgetView getGraphicalView() {
        return graphical;
    }
    
    public String getTextInputView() {
        return textIn;
    }
    
    public String getTextOutputView() {
        return textOut;
    }
    
    public String getTeXView() {
        return tex;
    }
    
}
