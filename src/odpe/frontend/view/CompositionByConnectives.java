/*  
 * This file is part of the Open Deduction Proof Editor
 * 
 * @author Joe Lynch
 */

package odpe.frontend.view;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import odpe.frontend.ast.ActiveCharacter;
import odpe.frontend.ast.Node;
import odpe.frontend.ast.Selection;
import odpe.frontend.syntax.SyntaxElement;
import odpe.frontend.view.InferenceFork.InferenceForkWidget;

public class CompositionByConnectives extends WidgetView {
	
	SyntaxElement syn;
	WidgetView[] rands;
	Node n;
	boolean available;
	ViewableObject operatorSymbol=null;
	boolean sa=false;
	
	public CompositionByConnectives(Node n, WidgetView[] rands) {
		this.syn = n.getSyntax();
		this.rands = rands;
		this.n = n;
	}

	public CompositionByConnectives(Node n, WidgetView[] rands, ViewableObject operatorSymbol) {
		this.syn = n.getSyntax();
		this.rands = rands;
		this.n = n;
		this.operatorSymbol = operatorSymbol;
	}

	public CompositionByConnectives(Node n, WidgetView[] rands, ViewableObject operatorSymbol, boolean sa) {
		this.syn = n.getSyntax();
		this.rands = rands;
		this.n = n;
		this.operatorSymbol = operatorSymbol;
		this.sa = sa;
	}

	@Override
	public JComponent getWidget() {
		return new SubJoin(rands, syn, n, operatorSymbol, sa);
	}
	
	public static class SubJoin extends JPanel implements MouseListener {
		
		private static final long serialVersionUID = 1L;
		String label;
		Vector<JComponent> widgets;
		Node n;
		ViewableObject operatorSymbol;
		WidgetView[] rands;
		boolean sa;
		
		SubJoin(WidgetView[] rands, SyntaxElement syn, Node n, ViewableObject operatorSymbol, boolean sa){
			super(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.insets = new Insets(4, 4, 4, 4);
			this.n = n;
			this.sa = sa;
			this.rands = rands;
			widgets = new Vector<JComponent>();
			this.operatorSymbol = operatorSymbol;
		
			for(WidgetView r : rands) {
				widgets.add(r.getWidget());
			}
				
			super.addMouseListener(this);
			super.setBorder(BorderFactory.createCompoundBorder(new LineBorder(Color.black),new EmptyBorder(10, 10, 10, 10)));
			structure();
		}
		
		//structure inference steps and compose them by connectives
		private void structure() {
			//Setting up background
			super.setBackground(Color.white);
			Vector<JPanel> panels = new Vector<JPanel>();
			GridBagConstraints c = new GridBagConstraints();
			for(JComponent w : widgets) {
				JPanel panel = new JPanel(new GridBagLayout());
				panel.setBackground(Color.white);
				c.gridx = 0;
				c.gridy = 0;
				c.ipadx = 5;
				c.anchor = GridBagConstraints.SOUTH;
				panel.add(w, c);
				panels.add(panel);
			}
			if(widgets.size() == 1) {
				if(widgets.firstElement() instanceof InferenceForkWidget || widgets.firstElement() instanceof SubJoin) {
					c.anchor = GridBagConstraints.CENTER;
					c.ipadx = 0;
					c.ipady = 0;
					c.gridx = 0;
					c.gridy = 0;
					
					MarkableString opsym = (MarkableString)operatorSymbol.getGraphicalView();
					MarkableString res = new MarkableString();
					res.append(opsym);
					res.add(new SelectionInfo(new Selection(n,0,0),0,res.length()));
					
					MarkableLabel lbl = (MarkableLabel) res.getWidget();
					Font fnt = lbl.getFont();
					lbl.setFont(fnt.deriveFont((float)10));
					lbl.setForeground(Color.black);
					widgets.add(lbl);
					super.add(lbl,c);
					c.gridx = 1;
					super.add(panels.firstElement(), c);
				}
			}
			else {
				int x = 0, y = 0;
				for(JPanel p : panels) {
					c.anchor = GridBagConstraints.CENTER;
					c.ipadx = 0;
					c.ipady = 0;
					c.gridx = x++;
					c.gridy = 0;
					
					super.add(p, c);
					if(panels.indexOf(p) != panels.size()-1) {
						c.ipadx = 20;
						c.ipady = 20;
						//System.out.println(operatorSymbol.getTextOutputView());
						JLabel lbl;

						if(!sa)
							lbl = new JLabel("<html><span style='font-size:14px; font-weight:50;'>" + operatorSymbol.getTextOutputView() + "</span></html>");
						else
							lbl = new JLabel("<html><span style='font-size:12px; font-weight:20;'>" + operatorSymbol.getTextOutputView() + "</span></html>");

					
						lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

						y++;
						int oppos = y;
						lbl.addMouseListener(new MouseAdapter() {
					        public void mouseClicked(MouseEvent e) {
					        	firePropertyChange("ActiveCharacterEventJoin", 0, new ActiveCharacter(n, oppos));
					        }
					    });
						
						Font fnt = lbl.getFont();
						lbl.setFont(fnt.deriveFont((float)10));
						lbl.setForeground(Color.black);
						JPanel lblPanel = new JPanel(new GridBagLayout());
						lblPanel.setBackground(Color.white);
						lblPanel.add(lbl);
						c.gridx = x++;
						super.add(lblPanel,c);
					}
				}
			}
		}
		
		public void setSelectable(boolean b) {
			for(JComponent w : widgets)
				if (w instanceof MarkableLabel)
					((MarkableLabel) w).setSelectable(b);
				else if (w instanceof SubJoin)
					((SubJoin) w).setSelectable(b);
				else if (w instanceof InferenceForkWidget)
					((InferenceForkWidget) w).setSelectable(b);
		}

		
		public void addPropertyChangeListener(PropertyChangeListener l) {
			super.addPropertyChangeListener(l);
			for(JComponent w : widgets) {
				w.addPropertyChangeListener(l);
			}
		}
		
		public void addMouseListener(MouseListener l) {
			super.addMouseListener(l);
			for(JComponent w : widgets)
				w.addMouseListener(l);
		}

		@Override
		public void mouseClicked(MouseEvent e) {
		}

		@Override
		public void mousePressed(MouseEvent e) {
		}

		@Override
		public void mouseReleased(MouseEvent e) {
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}
		
	}

	
}
