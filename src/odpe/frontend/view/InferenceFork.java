/*  
 * This file is part of the Open Deduction Proof Editor
 * 
 * @author It was originally created for GraPE by Max Schaefer
 * and has been adapted for Open Deduction by Joe Lynch
 */
package odpe.frontend.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import odpe.frontend.view.CompositionByConnectives.SubJoin;

public class InferenceFork extends WidgetView {
	
	public WidgetView conclusion;
	private String label;
	private WidgetView[] premises;
	
	public InferenceFork(String label, WidgetView[] args) {
		this.conclusion = args[0];
		this.label = label;
		this.premises = new WidgetView[args.length-1];
		for(int i=1;i<args.length;++i)
			this.premises[i-1] = args[i];
	}

	public JComponent getWidget() {
		JComponent[] tmp = new JComponent[premises.length];
		for(int i = 0; i < premises.length; ++i)
			tmp[i] = premises[i].getWidget();
		return new InferenceForkWidget(conclusion.getWidget(), label, tmp);
	}

	public static class InferenceForkWidget extends JPanel{
		private static final long serialVersionUID = 1L;
		private JComponent conclusion;
		private JComponent[] premises;
		
		public InferenceForkWidget(JComponent conclusion, String label, JComponent[] premises) {
			this(conclusion, label, premises, true);
		}
		
		public InferenceForkWidget(JComponent conclusion, String label, JComponent[] premises, boolean showPremises) {
			super(new GridBagLayout());
			super.setBackground(Color.white);

			GridBagConstraints c = new GridBagConstraints();
			JLabel b = new JLabel();
			b.setOpaque(true);
			b.setBackground(showPremises ? Color.black : Color.white);
			b.setPreferredSize(new Dimension(10, 1));
			JLabel lbl = new JLabel(label);
			Font fnt = lbl.getFont();
			lbl.setFont(fnt.deriveFont((float)10));
			lbl.setForeground(showPremises ? Color.black : Color.white);
			this.conclusion = conclusion;
			
			//if showPremises is true, and conclusion is a markable label then make it not selectable
			//makes 'a' not selectable
			if(showPremises && this.conclusion instanceof MarkableLabel)
				((MarkableLabel) this.conclusion).setSelectable(false);
			if(showPremises && this.conclusion instanceof SubJoin)
				((SubJoin) this.conclusion).setSelectable(false);
			if(showPremises && this.conclusion instanceof InferenceForkWidget)
				((InferenceForkWidget) this.conclusion).setSelectable(false);
			
			JPanel premisePanel = new JPanel(new GridBagLayout());
			premisePanel.setBackground(Color.white);
			c.gridx = 0;
			c.gridy = 0;
			c.ipadx = 5;
			c.anchor = GridBagConstraints.SOUTH;
			
			if(premises != null) {
				for(JComponent w : premises) {
					if(w instanceof JComponent)
						premisePanel.add(new InferenceForkWidget(w, label, null, false), c);
					else if (w instanceof SubJoin)
						premisePanel.add(new InferenceForkWidget(w, label, null, true), c);
					else
						premisePanel.add(w, c);
					c.gridx++;
				}
			}
			
			this.premises = premises;
			c.gridx = 0;
			c.gridy = 0;
			c.ipadx = 0;
			c.anchor = GridBagConstraints.CENTER;
			c.gridwidth = 2;
			super.add(premisePanel, c);
			c.gridx = 0;
			c.gridy = 1;
			c.gridwidth = 1;
			super.add(lbl, c);
			c.gridx = 1;
			c.gridy = 1;
			c.fill = GridBagConstraints.HORIZONTAL;
			super.add(b, c);
			c.gridx = 1;
			c.gridy = 2;
			c.fill = GridBagConstraints.NONE;
			super.add(this.conclusion, c);
		}
	
		public void setSelectable(boolean b) {
			
			//sets all conclusion to false
			if (this.conclusion instanceof MarkableLabel)
				((MarkableLabel) this.conclusion).setSelectable(b);
			else if (this.conclusion instanceof SubJoin)
				((SubJoin) this.conclusion).setSelectable(b);
			else if (this.conclusion instanceof InferenceForkWidget)
				((InferenceForkWidget) this.conclusion).setSelectable(b);
			
			//sets all premises to false
			for(JComponent p : this.premises)
				if(p instanceof MarkableLabel)
					((MarkableLabel) p).setSelectable(b);
				else if (p instanceof SubJoin)
					((SubJoin) p).setSelectable(b);
				else if (p instanceof InferenceForkWidget)
					((InferenceForkWidget) p).setSelectable(b);
					
		}
		
		public void addPropertyChangeListener(PropertyChangeListener l) {
			super.addPropertyChangeListener(l);
			conclusion.addPropertyChangeListener(l);
			for(int i = 0; i < premises.length; ++i)
				premises[i].addPropertyChangeListener(l);
		}
		
		public void addMouseListener(MouseListener l) {
			super.addMouseListener(l);
			conclusion.addMouseListener(l);
			for(int i = 0; i < premises.length; ++i)
				premises[i].addMouseListener(l);
		}
	}
}