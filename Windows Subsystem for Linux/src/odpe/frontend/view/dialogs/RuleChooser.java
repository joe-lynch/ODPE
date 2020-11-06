/*  
 * 
 * This file is part of the Open Deduction Proof Editor.
 * 
 * @author Originally created for GraPE by Max Schaefer
 *
 */


package odpe.frontend.view.dialogs;

import java.awt.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;

import odpe.frontend.syntax.InferenceRule;
import odpe.frontend.syntax.Rule;
import odpe.frontend.syntax.SubatomicRule;
import odpe.frontend.view.InferenceFork;
import odpe.frontend.view.SubAtom;

public class RuleChooser extends JDialog {
	
	private static final long serialVersionUID = 1L;
	private Box inf_rules;
	private Box rinf_rules;
	private boolean subatomised;

	private Map<Rule, JCheckBox> checkboxes;

	/*
	public RuleChooser(JFrame parent, String sysname, Collection<Rule> rules, Collection<Rule> subatomic_rules){
		this(parent, sysname, Stream.concat(rules.stream(), subatomic_rules.stream()).collect(Collectors.toList()));
	}
	*/
	
	public RuleChooser(JFrame parent, String sysname, Collection<Rule> rules) {
		super(parent, "System "+sysname, false);
		inf_rules = new Box(BoxLayout.Y_AXIS);
		rinf_rules = new Box(BoxLayout.Y_AXIS);
		checkboxes = new HashMap<Rule, JCheckBox>(rules.size());
		for(Rule r : rules) {
			JCheckBox c = new JCheckBox(pad(r.getName(), 15));
			c.setSelected(r.isDefaultEnabled());
			checkboxes.put(r, c);
			if(r instanceof SubatomicRule && r.isUpDirection()) {
				c.setVisible(false);
				inf_rules.add(c);
			}
			else if (r instanceof SubatomicRule && !r.isUpDirection()) {
				c.setVisible(false);
				rinf_rules.add(c);
			}
			else if(r instanceof InferenceRule && r.isUpDirection())
				inf_rules.add(c);
			else if (r instanceof InferenceRule && !r.isUpDirection())
				rinf_rules.add(c);
		}
		inf_rules.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Inference Rules"),
				BorderFactory.createEmptyBorder(5,5,5,5)));
		/**rinf_rules.setBorder(BorderFactory.createCompoundBorder(
        		BorderFactory.createTitledBorder("Down Inference Rules"),
        		BorderFactory.createEmptyBorder(5,5,5,5))); **/
		super.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		super.add(inf_rules, c);
		c.gridy = 1;
		//super.add(rinf_rules, c);
		super.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		super.pack();
	}
	
	private String pad(String s, int w) {
		if(s.length() < w) {
			int d = w - s.length();
			StringBuilder b = new StringBuilder();
			b.append(" ".repeat(d));
			return s + b.toString();
		} else {
			return s;
		}
	}

	public void showSubatomicRules (boolean flag){
		subatomised = flag;
		for( Map.Entry<Rule, JCheckBox> entry : checkboxes.entrySet())
			if(entry.getKey() instanceof SubatomicRule)
				entry.getValue().setVisible(flag);
			else if(entry.getKey() instanceof InferenceRule)
				entry.getValue().setVisible(!flag);
	}
	
	public boolean ruleActive(Rule r) {
		if (checkboxes.get(r).isSelected()) {
			if (r instanceof SubatomicRule && subatomised)
				return true;
			else return r instanceof InferenceRule && !subatomised;
		}
		return false;
	}
}
