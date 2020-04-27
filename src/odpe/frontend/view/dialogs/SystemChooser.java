/*  
 * 
 * This file is part of the Open Deduction Proof Editor.
 * 
 * @author Joe Lynch, heavy adaption from the original system chooser in GraPE by Max Schaefer
 *
 */
package odpe.frontend.view.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class SystemChooser extends JDialog implements ActionListener {

	private static final long serialVersionUID = 1L;

	JLabel syslbl;
	JTextField sysfn;
	JButton sysbrowse;
	JLabel sysdefaultlbl;
	JButton sysdefault;
	GridBagConstraints c;

	static class MaudeFileFilter extends javax.swing.filechooser.FileFilter {

		public boolean accept(File f) {
			return f.isDirectory() || f.getName().endsWith(".xml");
		}

		public String getDescription() {
			return "XML files (*.xml)";
		}
	}

	JFileChooser fc;
	JButton okButton;
	JButton cancelButton;

	private JButton sysKSg;

	private AbstractButton sysSKV;

	public SystemChooser(JFrame parent, String sys) {
		super(parent, "Select a proof system", true);
		super.setLayout(new BorderLayout());
		
		JPanel defaultPanel = new JPanel(new GridBagLayout());
		defaultPanel.setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(10, 10, 10, 10), new LineBorder(Color.black)));
		GridBagConstraints c = new GridBagConstraints();
		
		JPanel test = new JPanel(new FlowLayout());
		sysdefaultlbl = new JLabel("Default proof systems");
		test.add(sysdefaultlbl);
		super.add(test);
		c.gridx = 0;
		c.gridy = 0;
		
		defaultPanel.add(test, c);
		
		JPanel ksgPanel = new JPanel(new FlowLayout());
		sysKSg = new JButton("KSg");
		sysKSg.addActionListener(this);
		ksgPanel.add(sysKSg);
		super.add(ksgPanel);
		c.gridx = 0;
		c.gridy = 1;
		
		defaultPanel.add(ksgPanel, c);
		
		JPanel skvPanel = new JPanel(new FlowLayout());
		sysSKV = new JButton("SKV");
		sysSKV.addActionListener(this);
		skvPanel.add(sysSKV);
		super.add(skvPanel);
		c.gridx = 0;
		c.gridy = 2;
		
		defaultPanel.add(skvPanel, c);
		super.add(defaultPanel, BorderLayout.NORTH);
		
		JPanel browsePanel = new JPanel(new GridBagLayout());
		browsePanel.setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(10, 10, 10, 10), new LineBorder(Color.black)));
		c = new GridBagConstraints();
		
		JPanel p = new JPanel(new FlowLayout());
		syslbl = new JLabel("System Description:", JLabel.RIGHT);
		sysfn = new JTextField(sys, 10);
		sysbrowse = new JButton("Browse...");
		sysbrowse.addActionListener(this);
		p.add(syslbl);
		p.add(sysfn);
		p.add(sysbrowse);
		
		c.gridx=0;
		c.gridy=0;
		
		browsePanel.add(p, c);
		
		JPanel buttonPanel = new JPanel(new FlowLayout());
		okButton = new JButton("OK");
		okButton.addActionListener(this);
		buttonPanel.add(okButton);
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);
		buttonPanel.add(cancelButton);
		
		c.gridx=0;
		c.gridy=1;
		
		browsePanel.add(buttonPanel, c);
		
		super.add(browsePanel, BorderLayout.SOUTH);
		
		String cwd = System.getProperty("user.dir");
		fc = new JFileChooser(new File(cwd));
		fc.addChoosableFileFilter(new MaudeFileFilter());
	}

	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if (src == sysbrowse) {
			int retval = fc.showOpenDialog(this);
			if (retval == JFileChooser.APPROVE_OPTION)
				sysfn.setText(fc.getSelectedFile().getPath());
		} else if(src == okButton && !sysfn.getText().equals("")) {
			File tmp = new File(sysfn.getText());
			if(!getFileExtension(tmp).equals(".xml")) {
				FileNotFoundException fileNotFound = 
						new FileNotFoundException("You must select the xml file for the Maude proof system. e.g. 'ksg-maude.xml'");
				JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
				ExceptionDialog.showWarningDialog(topFrame, fileNotFound, "xml file not found");
				sysfn.setText("");
			} else {
				super.setVisible(false);
			}
		} else if(src == cancelButton) {
			sysfn.setText("");
			super.setVisible(false);
		} else if(src == sysKSg) {
			File ksg = new File(System.getProperty("user.dir") + "/ksg-maude.xml");
			if(ksg.exists()) {
				sysfn.setText(ksg.getAbsolutePath());
				super.setVisible(false);
			}
			else {
				FileNotFoundException fileNotFound = 
						new FileNotFoundException("ksg-maude.xml not found in directory. Try browsing for the xml file.");
				JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
				ExceptionDialog.showWarningDialog(topFrame, fileNotFound, "ksg-maude.xml not found");
				sysfn.setText("");
			}
		} else if(src == sysSKV) {
			File skv = new File(System.getProperty("user.dir") + "/skv-maude.xml");
			if(skv.exists()) {
				sysfn.setText(skv.getAbsolutePath());
				super.setVisible(false);
			}
			else {
				FileNotFoundException fileNotFound = 
						new FileNotFoundException("skv-maude.xml not found in directory. Try browsing for the xml file.");
				JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
				ExceptionDialog.showWarningDialog(topFrame, fileNotFound, "skv-maude.xml not found");
				sysfn.setText("");
			}
		}
		
	}
	


	private String getFileExtension(File file) {
	    String name = file.getName();
	    int lastIndexOf = name.lastIndexOf(".");
	    if (lastIndexOf == -1) {
	        return ""; // empty extension
	    }
	    return name.substring(lastIndexOf);
	}



	public String getSystemFilename() {
		return sysfn.getText();
	}

}
