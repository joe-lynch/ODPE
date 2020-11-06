/*  
 * 
 * This file is part of the Open Deduction Proof Editor.
 * 
 * @author Adapted by Joe Lynch from the Originally created for GraPE by Max Schaefer
 *
 */

package odpe.frontend.view.dialogs;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class ExceptionDialog {
	
	public static void showExceptionDialog(JFrame parent, Exception e) {
		showWarningDialog(parent, e);
		e.printStackTrace();
		System.exit(-1);
	}

	public static void showWarningDialog(JFrame parent, Exception e) {
		JOptionPane.showMessageDialog(parent,
				"Exception "+e.getClass().getName()+":\n\n"+e.getMessage(), 
				e.getClass().getName(),
				JOptionPane.ERROR_MESSAGE);
	}
	
	public static void showWarningDialog(JFrame parent, Exception e, String title) {
		JOptionPane.showMessageDialog(parent,
				"Exception "+e.getClass().getName()+":\n\n"+e.getMessage(), 
				title,
				JOptionPane.ERROR_MESSAGE);
	}

}
