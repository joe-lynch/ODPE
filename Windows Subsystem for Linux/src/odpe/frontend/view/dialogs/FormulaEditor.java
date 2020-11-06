/*  
 * 
 * This file is part of the Open Deduction Proof Editor.
 * 
 * @author Originally created for GraPE by Max Schaefer
 *
 */
package odpe.frontend.view.dialogs;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class FormulaEditor {
	
	private String formula;
	private JFrame parent;
	private JTextArea ta;
	
	public FormulaEditor(JFrame parent) {
		this.formula = null;
		this.parent = parent;
	}

	public void show() {
		parent.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		parent.setResizable(true);
		formula = "";
		JTextArea ta = new JTextArea(3,5);
		ta.setLineWrap(true);
		JScrollPane sp = new JScrollPane(ta);
		JLabel la = new JLabel("Enter a formula (or derivation): ");

		JPanel pa = new JPanel() {
			@Override
			public Dimension getPreferredSize() {
				return new Dimension(300, 120);
			}
		};
		pa.setLayout(new BoxLayout(pa, BoxLayout.Y_AXIS));
		pa.add(la);
		pa.add(Box.createRigidArea(new Dimension(5, 5)));
		pa.add(sp);


		//sets focus to text area
		ta.addHierarchyListener(e -> {
			if(e.getComponent().isShowing() && (e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0)
				SwingUtilities.invokeLater(e.getComponent()::requestFocusInWindow);
		});


		while ((formula != null) && (formula.equals(""))) {
			//formula
			int option = JOptionPane.showConfirmDialog(parent,
					pa, "New Proof",
					JOptionPane.OK_CANCEL_OPTION);
			if (option == JOptionPane.OK_OPTION){
				formula = ta.getText();
			}

			else if (option == JOptionPane.CANCEL_OPTION){
				formula = null;
			}
		}
	}

	public String getFormula() {
		return formula;
	}

}
