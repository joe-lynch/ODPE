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
import odpe.frontend.view.WidgetView;

public abstract class SyntaxElement {
	
	private String id;
	protected InferenceSystem system;
	
	protected SyntaxElement(String id, InferenceSystem sys) {
		this.setId(id);
		this.system = sys;
	}
	
	public String getID() {
		return id;
	}
	
	public InferenceSystem getSystem() {
		return system;
	}

	public abstract int getPrecedence();
	public abstract int getOperandPrecedence(int i);
	
	public abstract WidgetView draw(Node n, WidgetView[] rands);
	public abstract String prettyprint(Node n, String[] rands);

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
