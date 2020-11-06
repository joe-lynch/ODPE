/*  
 * 
 * This file is part of the Open Deduction Proof Editor.
 * 
 * @author It has been adapted by Joe Lynch from the original file for GraPE by Max Schaefer
 *
 */

package odpe.frontend.view;

import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JTextPane;
import javax.swing.plaf.basic.BasicTextUI.BasicCaret;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

import odpe.util.Pair;

public class StructuredLabel extends JTextPane implements MouseListener, MouseMotionListener {
	
	private static final long serialVersionUID = 1L;

	/** the text displayed in this structured label */
	private String txt;
	
	/** the markable character ranges */
	private Vector<Pair<Integer,Integer>> markings;
	
	/** the active character positions */
	private boolean[] active;

	/** this field stores the position the user starts dragging the caret from;
	 * since we allow only the given character ranges to be marked, this might not
	 * be the same as the current caret mark, but we have to store it to correctly compute
	 * the best fitting character range */
	private int realmark = -1;
	
	/** the active character the mouse is currently over -- if any */
	private int activechar = -1;
	
	/** indicates whether anything can be selected in this label */
	boolean selectable;

	/** Constructs a structured label from a given text, an array of
	 * marking positions indicating the selectable character ranges,
	 * and an array of active character positions.
	 * 
	 * @author Original by Max Schaefer, adapted by Joe Lynch for multiple selections
	 * 
	 * @param txt the text the label should display
	 * @param markingposs marking positions; two consecutive marking positions delimit one selectable range
	 * @param active_chars the positions of the active characters
	 */
	public StructuredLabel(String txt, int[] markingposs, int[] active_chars) {
		super();
		super.setDisabledTextColor(Color.black);
		super.setBorder(BorderFactory.createLineBorder(Color.white));
		Font f = new Font(Font.SANS_SERIF, Font.PLAIN, 15);
		this.txt = txt;
		if(markingposs.length % 2 != 0)
			throw new IndexOutOfBoundsException();
		this.markings = new Vector<Pair<Integer,Integer>>(markingposs.length/2);
		for(int i = 0; i < markingposs.length; i += 2) {
			markings.add(new Pair<Integer, Integer>(markingposs[i], markingposs[i+1]));
		}
		this.active = new boolean[this.txt.length()+1];
		for(int i = 0; i < active_chars.length; ++i)
			this.active[active_chars[i]] = true;
		super.setEditable(false);
		super.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		String curr=this.txt;
		char[] alpha = "abcdefghijklmnopqrstuvwxyz".toCharArray();
		Vector<String> str = new Vector<String>();
		for(char c : alpha) {
			for(int i=0; i<10; i++) {
				str.add(Character.toString(c)+Integer.toString(i));
			}
		}
		for(char c1 : alpha) {
			str.add(Character.toString(c1));
		}
		for(String s : str) {
			if(curr.contains("-"+s)) {
				curr = curr.replace("-"+s, s+"\u0304");
			}
		}
		super.setFont(f);
		super.setText(curr);
		super.addMouseListener(this);
		super.addMouseMotionListener(this);
		super.setCaret(new HighlightCaret());
		
        selectable = true;
	}
	
	public String getText() {
		return txt;
	}
	
	/**
	 * Handles dragging events; makes sure that the selected
	 * range of characters always is fitted to an allowed range.
	 * @author Original by Max Schaefer, adapted by Joe Lynch for multiple selections
	 * 
	 * @param e the mouse event to handle
	 */
	public void mouseDragged(MouseEvent e) {
		if(!selectable)
			return;
		Caret c = super.getCaret();
		int mark = c.getMark();
		int dot = c.getDot();
		if(dot == mark) {
			realmark = mark;
			super.getHighlighter().removeAllHighlights();
			return;
		} else {
			mark = realmark;
		}
		int left, right;
		//left and right of the selection
		left = Math.min(dot, mark);
		right = Math.max(dot, mark);
		Pair<Integer, Integer> best = new Pair<Integer, Integer>(-1, -1);
		int dist = -1;
		
		for(Pair<Integer, Integer> p : markings) {
			//if marking is smaller than the selection, continue
			if(p.fst > left || p.snd < right)
				continue;
			//work out the distance, finds the smallest distance, which will be nearest marking to the selection
			int d = Math.abs(left - p.fst) + Math.abs(p.snd - right);
			if((dist == -1) || (d < dist)) {
				dist = d;
				best.copy(p);
			}
		}
		
		if(dist == -1) {
			super.setCaretPosition(0);
			super.moveCaretPosition(0);
			super.getHighlighter().removeAllHighlights();
			super.firePropertyChange("SelectionErased", null, null);
		} else {
			
			super.setCaretPosition(best.fst);
			super.moveCaretPosition(best.snd);
			try {
				super.getHighlighter().removeAllHighlights();
				super.getHighlighter().addHighlight(best.fst, best.snd, HighlightCaret.getFocusedpainter());
				
			} catch(BadLocationException err) {
				err.printStackTrace();
			}
		
			Container rootComp = this;
			while(!(rootComp.getParent().getLayout() instanceof java.awt.BorderLayout))
				rootComp = rootComp.getParent();	
			super.firePropertyChange("NewSelection", rootComp, best);
		}
	}

	/**
	 * Handles mouse move events; changes the cursor to a hand
	 * shape when it hovers over an active character.
	 * 
	 * @param e the mouse event to handle
	 */
	public void mouseMoved(MouseEvent e) {
		if(!selectable)
			return;
		int i = super.viewToModel(e.getPoint());
		if(active[i]) {
			super.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			activechar = i;
		} else {
			super.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
			activechar = -1;
		}
	}

	/**
	 * Handles mouse click events; if the click is a left-click
	 * on an active character, the corresponding event is fired.
	 * 
	 * @param e the mouse event to handle
	 */
	public void mouseClicked(MouseEvent e) {
		if(!selectable)
			return;
		if(e.getButton() == MouseEvent.BUTTON1) {
			if(activechar != -1) {
				super.firePropertyChange("ActiveCharacterClicked", 0, activechar);
			}
			unselect();
			realmark = 0;
		}
	}

	public void mousePressed(MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON1) {
			super.getHighlighter().getHighlights();
			super.getHighlighter().removeAllHighlights();
		}
	}

	/**
	 * Deals with when the mouse is released, it checks if there is a current
	 * selection and fires property changes for the correct marking information
	 * 
	 * @author Joe Lynch
	 */
	public void mouseReleased(MouseEvent e) {
		Highlighter.Highlight[] highlights = this.getHighlighter().getHighlights();
		
		try{
			if (highlights[0] == null)
				System.out.println("k");

			Pair<Integer, Integer> best = new Pair<Integer, Integer>();
			best.fst = highlights[0].getStartOffset();
			best.snd = highlights[0].getEndOffset();
			//
			
			int dist = -1;
			
			for(Pair<Integer, Integer> p : markings) {
				//if marking is smaller than the selection, continue
				if(p.fst > best.fst || p.snd < best.snd)
					continue;
				//work out the distance, finds the smallest distance, which will be nearest marking to the selection
				int d = Math.abs(best.fst - p.fst) + Math.abs(p.snd - best.snd);
				if((dist == -1) || (d < dist)) {
					dist = d;
					best.copy(p);
				}
			}
			
			if(dist == -1) {
				super.setCaretPosition(0);
				super.moveCaretPosition(0);
				super.getHighlighter().removeAllHighlights();
				super.firePropertyChange("SelectionErased", null, null);
			} else {
				
				super.setCaretPosition(best.fst);
				super.moveCaretPosition(best.snd);
				try {
					super.getHighlighter().removeAllHighlights();
					super.getHighlighter().addHighlight(best.fst, best.snd, HighlightCaret.getFocusedpainter());
					
				} catch(BadLocationException err) {
					err.printStackTrace();
				}
			
				Container rootComp = this;
				while(!(rootComp.getParent().getLayout() instanceof java.awt.BorderLayout))
					rootComp = rootComp.getParent();	
				super.firePropertyChange("NewSelection", rootComp, best);
			}
		
			
		} catch(ArrayIndexOutOfBoundsException ae) {
			super.firePropertyChange("SelectionErased", null, this);
		}

	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	/**
	 * Sets the selectability status of this label.
	 * 
	 * @param b <code>true</code> to enable selections and active character handling, <code>false</code> otherwise
	 */
	public void setSelectable(boolean b) {
		//selectable = b;
		//super.setEnabled(b);
	}
	

	/**
	 * Erases the current selection in this label.
	 *
	 */
	public void unselect() {
		realmark = activechar = -1;
		super.setCaretPosition(0);
		super.moveCaretPosition(0);
		super.getHighlighter().removeAllHighlights();
		super.firePropertyChange("SelectionErased", null, null);
	}
}


/**
 * Handles the highlighting of labels - it allows for the selection
 * of multiple labels 
 * 
 * @author Joe Lynch
 */
class HighlightCaret extends BasicCaret {

    private static final Highlighter.HighlightPainter focusedPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.CYAN);
    private static final long serialVersionUID = 1L;
    private boolean isFocused;

    @Override
    protected Highlighter.HighlightPainter getSelectionPainter() {
        return getFocusedpainter();
    }

    @Override
    public void setSelectionVisible(boolean hasFocus) {
        if (hasFocus != isFocused) {
            isFocused = hasFocus;
            super.setSelectionVisible(false);
            super.setSelectionVisible(true);
        }
    }

	public static Highlighter.HighlightPainter getFocusedpainter() {
		return focusedPainter;
	}
	
}
