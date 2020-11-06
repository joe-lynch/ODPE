/*  
 * 
 * This file is part of the Open Deduction Proof Editor.
 * 
 * @author Originally created for GraPE by Max Schaefer
 *
 */
package odpe.backend;

import java.util.Vector;

import odpe.frontend.ast.Node;
import odpe.frontend.syntax.Rule;

/**
 * Class encapsulating all information about a possible rewrite.
 * It contains information about the redex, the used rule, and
 * the results. Note the plural here: rewrites can have more
 * than one result, in which case they correspond to the application
 * of a branching inference rule.
 * 
 * @author Max Schaefer
 *
 */

public class Rewrite {

	/** The redex rewritten by this rewrite. */
	private Node redex;
	
	/** The rule used in this rewrite. */
	private Rule rule;
	
	/** The results of this rewrite. If there are more than
	 * one, this rewrite corresponds to the application of a branching
	 * inference rule. */
	private Vector<Node> results;
	
	private boolean reverse;
	
	/**
	 * Constructs a rewrite from a redex, a rule, and a list of
	 * results.
	 * 
	 * @param redex the redex
	 * @param rule the rule
	 * @param results the results
	 */
	public Rewrite(Node redex, Rule rule, Vector<Node> results) {
		this.redex = redex;
		this.rule = rule;
		this.results = results;
	}
	
	/** 
	 * Provides access to the redex of a rewrite. 
	 * 
	 * @return the redex */
	public Node getRedex() {
		return redex;
	}
	
	/** 
	 * Provides access to the rule used in a rewrite. 
	 * 
	 * @return the rule */
	public Rule getRule() {
		return rule;
	}
	
	/** 
	 * Provides access to the results of a rewrite. 
	 * 
	 * @return the results */
	public Vector<Node> getResults() {
		return results;
	}
	
	public boolean getReverse() {
		return reverse;
	}
	
	/**
	 * Builds a textual representation of this rewrite
	 * of the form <code>by &lt;rulename&gt;: &lt;results&gt;</code>.
	 * This is the representation displayed in the dialog
	 * for choosing rewrites.
	 * 
	 * @return the textual representation
	 */
	public String toString() {
		StringBuffer res = new StringBuffer("by "+rule.getName()+": ");
		for(int i = 0; i < results.size(); ++i) {
			if(i != 0)
				res.append("    ");
			res.append(results.elementAt(i).prettyprint());
		}
		res.append("  ");
		return res.toString();
	}

}
