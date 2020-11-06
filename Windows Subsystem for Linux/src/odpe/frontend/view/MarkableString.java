/*  
 * 
 * This file is part of the Open Deduction Proof Editor.
 * 
 * @author It was originally created for GraPE by Max Schaefer
 *
 */


package odpe.frontend.view;

import java.util.Vector;
import javax.swing.JComponent;

public class MarkableString extends WidgetView {

	private StringBuffer text;
	private Vector<SelectionInfo> markable;
	private Vector<ActiveCharacterInfo> actchars;
	
	public MarkableString() {
		this.setText(new StringBuffer());
		this.setMarkable(new Vector<SelectionInfo>());
		this.setActchars(new Vector<ActiveCharacterInfo>());
	}
	
	public MarkableString(String text) {
		this.setText(new StringBuffer(text));
		this.setMarkable(new Vector<SelectionInfo>());
		this.setActchars(new Vector<ActiveCharacterInfo>());
	}

	public JComponent getWidget() {
		MarkableLabel lbl = new MarkableLabel(getTextString(), getMarkable(), getActchars());
		return lbl;
	}

	public void shift(int offset) {
		for(SelectionInfo rng : getMarkable())
			rng.shift(offset);
		for(ActiveCharacterInfo ac : getActchars())
			ac.shift(offset);
	}
	
	public void append(MarkableString str) {
		int offset = getText().length();
		getText().append(str.getText());
		Vector<SelectionInfo> tmp = 
			new Vector<SelectionInfo>(str.getMarkable().size());
		for(SelectionInfo inf : str.getMarkable()) {
			inf.shift(offset);
			tmp.add(inf);
		}
		Vector<ActiveCharacterInfo> tmp2 = 
			new Vector<ActiveCharacterInfo>(str.getActchars().size());
		for(ActiveCharacterInfo inf : str.getActchars()) {
			inf.shift(offset);
			tmp2.add(inf);
		}
		getMarkable().addAll(tmp);
		getActchars().addAll(tmp2);
	}
	
	public void append(String str) {
		getText().append(str);
	}

	public int length() {
		return getText().length();
	}

	public void add(ActiveCharacterInfo info) {
		getActchars().add(info);
	}

	public void add(SelectionInfo info) {
		getMarkable().add(info);
	}
	
	public String toString() {
		return getText().toString();
	}

	public Vector<SelectionInfo> getMarkable() {
		return markable;
	}

	public void setMarkable(Vector<SelectionInfo> markable) {
		this.markable = markable;
	}

	public Vector<ActiveCharacterInfo> getActchars() {
		return actchars;
	}

	public void setActchars(Vector<ActiveCharacterInfo> actchars) {
		this.actchars = actchars;
	}

	public String getTextString() {
		return text.toString();
	}
	
	public StringBuffer getText() {
		return text;
	}

	public void setText(StringBuffer text) {
		this.text = text;
	}

}
