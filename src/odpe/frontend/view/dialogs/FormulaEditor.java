/*  
 * 
 * This file is part of the Open Deduction Proof Editor.
 * 
 * @author Originally created for GraPE by Max Schaefer
 *
 */
package odpe.frontend.view.dialogs;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class FormulaEditor {
	
	private String formula;
	private JFrame parent;
	
	public FormulaEditor(JFrame parent) {
		this.formula = null;
		this.parent = parent;
	}

	public void show() {
		formula = "";
		while ((formula != null) && (formula.equals(""))) {
			formula = JOptionPane.showInputDialog(parent,
					"Enter a formula to prove: ", "New Proof",
					JOptionPane.PLAIN_MESSAGE);
		}
	}

	public String getFormula() {
		return formula;
	}

}
