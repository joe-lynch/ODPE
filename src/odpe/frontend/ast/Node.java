/*  
 * 
 * This file is part of the Open Deduction Proof Editor.
 * 
 * @author Joe Lynch
 *
 */
package odpe.frontend.ast;

import java.io.IOException;
import java.util.Collections;
import java.util.Objects;
import java.util.Vector;

import odpe.backend.ProverException;
import odpe.backend.odpe2maude.MaudeException;
import odpe.backend.odpe2maude.ODPE2Maude;
import odpe.frontend.model.InferenceSystem;
import odpe.frontend.syntax.BinaryOperator;
import odpe.frontend.syntax.InferenceRule;
import odpe.frontend.syntax.Operator;
import odpe.frontend.syntax.SyntaxElement;
import odpe.frontend.syntax.UnaryOperator;
import odpe.frontend.view.CompositionByConnectives;
import odpe.frontend.view.InferenceFork;
import odpe.frontend.view.WidgetView;
import odpe.util.Pair;


public class Node{
	
	private InferenceSystem system;
	private Node parent;
	private SyntaxElement syntax;
	private Vector<Node> children;
	private Selection selection;
	private boolean special;
	
	public Node(InferenceSystem system, SyntaxElement syn, Vector<Node> chld) {
		this.system = system;
		this.children = chld;
		this.syntax = syn;
		this.special = false;
		selection = new Selection(this, 0, chld.size()-1);
		for(Node c : this.children)
			c.parent = this;
	}

	public Node(InferenceSystem system, Operator syn, Node... operatorNodes) {
		this.system = system;
		this.syntax = syn;
		this.children = new Vector<Node>(operatorNodes.length);
		
		for(int i = 0; i < operatorNodes.length; ++i) {
			operatorNodes[i].setParent(this);
			this.children.add(operatorNodes[i]);
		}
	}
	
	public void replaceChild(Node before, Node after) {
		int i = children.indexOf(before);
		children.set(i, after);
		after.setParent(this);
	}
	
	public void replaceChildren(int start, int end, Node newNode) {
		int d = end - start;
		int i;
		setChild(start, newNode);
		for(i=start+1;i+d<children.size();++i)
			children.set(i,children.get(i+d));
		for(;i<children.size();)
			children.remove(i);
	}
	
	public Node clone() {
		int n = children.size();
		Vector<Node> newchildren = new Vector<Node>(n);
		for(int i = 0; i < n; ++i) {
			Node cln = children.elementAt(i).clone();
			newchildren.add(cln);
			cln.setSelection(children.elementAt(i).selection.getStart(), children.elementAt(i).selection.getEnd());
		}
		return new Node(system, syntax, newchildren);
	}

	public void swap(int oppos) {
		Node tmp = children.get(oppos-1);
		children.set(oppos-1, children.get(oppos));
		children.set(oppos, tmp);
	}
	
	public void refresh() {
		if(selection.getEnd() >= children.size())
			setSelection(0, children.size()-1);
		for(Node c : children) {
			c.parent = this;
			c.refresh();
		}
	}
	
	//iterates through derivation drawing
	public WidgetView draw() {
		WidgetView[] rands = new WidgetView[children.size()];
		for(int i = 0; i < rands.length; ++i)
			rands[i] = children.get(i).draw();

		return syntax.draw(this, rands);
	}
	
	public Vector<Node> getUnselected(){
		Vector<Node> list = new Vector<Node>();
		for(Node c : children) {
			if(syntax instanceof InferenceRule) {
				list.addAll(children.get(0).getUnselected());
				list.addAll(children.get(1).getUnselected());
				break;
			}
			else
				list.addAll(c.getUnselected());
		}
		
		
		if(syntax instanceof InferenceRule) {
			parent.children.set(parent.children.indexOf(this), children.lastElement());
			return list;
		}
		else if(syntax instanceof BinaryOperator && children.size() == 1) {
			try {
				parent.replaceChild(this, children.firstElement());
			} catch(NullPointerException ne) {
				return list;
			}
		}
			
		
		if (this.selection.notSelected() == null)
			return list;
		
		Vector<Node> chld = new Vector<Node>();
		for(Node k : this.selection.notSelected().fst)
			chld.add(k);
		chld.addAll(list);
		for(Node n : chld)
			if (n.syntax instanceof BinaryOperator && n.children.size() == 1)
				chld.set(chld.indexOf(n), n.children.firstElement());
			else if (n.syntax instanceof InferenceRule)
				chld.set(chld.indexOf(n), n.children.lastElement());	
				
		Node t = new Node(system, this.syntax, chld);
		list.clear();
		list.add(t);
		list.removeIf(Objects::isNull);
		return list;
	}
	
	public Node removeRedundantOperators(){
		Vector<Node> toRemove = new Vector<Node>();
		Vector<Node> toAdd = new Vector<>();
		for(Node c : this.children)
			if(this.syntax == c.removeRedundantOperators().syntax && this.syntax instanceof BinaryOperator) {
				toAdd.addAll(c.children);
				for(Node cc : c.children)
					cc.parent = this;
				toRemove.add(c);
				c.parent = null;
			}
		this.children.removeAll(toRemove);
		this.children.addAll(toAdd);
		return this;
	}
	
	public void getUnary() {
		for(Node c : children) {
			if(syntax instanceof InferenceRule) {
				parent.setChild(parent.children.indexOf(this), children.lastElement());
				children.lastElement().setParent(parent);
				parent = null;
				children.lastElement().getUnary();
				break;
			}
			else
				c.getUnary();
		}
	}
	
	public Node getUnary1() {
		if (syntax instanceof InferenceRule) {
			return children.lastElement().getUnary1();
		}
		return this;
	}
	
	public boolean unaryAncestor(Node q){
		if(syntax instanceof UnaryOperator) {
			if(this == q)
				return true;
			//else
				//return false;
		}
		if(parent == null)
			return false;
		
		return parent.unaryAncestor(q);
	}
	
	public boolean lcaAncestor(Node q, Vector<Node> lca_list){
		if(lca_list.contains(this)) {
			if(this == q)
				return true;
			else
				return false;
		}
		if(parent == null)
			return false;
		
		return parent.lcaAncestor(q, lca_list);
	}
	
	public boolean updateSelection(Vector<Node> sel) {
		Vector<Integer> list = new Vector<Integer>();
		Vector<Integer> antilist = new Vector<Integer>();
		Vector<Node> reorder = new Vector<Node>();
		for(Node c : children) {
			if(syntax instanceof InferenceRule) {
				if(children.lastElement().updateSelection(sel)) {
					list.add(1);
					this.setSelection(Collections.min(list), Collections.max(list));
				}
				else if(children.firstElement().updateSelection(sel)) {
					list.add(0);
					this.setSelection(Collections.min(list), Collections.max(list));
				}
				break;
			}
			if(c.updateSelection(sel)) {
				list.add(children.indexOf(c));
				this.setSelection(Collections.min(list), Collections.max(list));
			}
		}
		
		try {
			if((parent==null || parent.getSyntax() instanceof InferenceRule) && !(syntax instanceof InferenceRule)) {
				reorder.addAll(children);
				for(int k = 0; k<children.size(); k++)
					if(!list.contains(k))
						antilist.add(k);
				int ind = 0;
				for(int i : list) {
					reorder.set(ind, children.get(i));
					ind++;
				}
				for(int i : antilist) {
					reorder.set(ind, children.get(i));
					ind++;
				}
				children = reorder;
				for(int i =0; i<list.size();i++)
					list.set(i, i);
				if(!list.isEmpty())
					this.setSelection(Collections.min(list), Collections.max(list));
			}
		} catch(NullPointerException np) {
		}
		
		if(!list.isEmpty())
			sel.add(this);
		if(sel.contains(this))
			return true;
		else{
			this.setSelection(0, this.children.size()-1);
			return false;
		}
	}


	public Boolean isRight() {
		if(parent==null) {
			return true;
		}
		while(!(parent.getSyntax() instanceof InferenceRule)) {
			return parent.isRight();
		}
		if(parent.children.get(1) == this)
			return true;
		else
			return false;
	}

	public Boolean isLeft() {
		if(parent==null)
			return true;
		while(!(parent.getSyntax() instanceof InferenceRule)) {
			return parent.isLeft();
		}
		if(parent.children.get(0) == this)
			return true;
		else
			return false;
	}
	
	
	public Node subatomise(ODPE2Maude odpe2Maude) throws ProverException, IOException, MaudeException {
		boolean flag = false;
		if (syntax instanceof BinaryOperator)
			for(Node c : children)
				if (c.syntax instanceof InferenceRule) {
					flag = true;
					break;
				}
		/*
		if (syntax instanceof InferenceRule || flag) {
			Vector<Node> v = new Vector<Node>();
			for(Node c : children){
				v.add(c.subatomise(odpe2Maude));
			}
			return new Node(system, this.getSyntax(),v);
		}
		*/
		return odpe2Maude.subatomise_reduce(this);
	}
/*
	public Node subatomise1(ODPE2Maude odpe2Maude) throws ProverException, IOException, MaudeException {
		boolean flag = false;
		for(Node c : children){
			Vector<Node> v = new Vector<Node>();
			v.add(c.subatomise1(odpe2Maude));
		}

		if(syntax instanceof InferenceRule){
			return odpe2Maude.subatomise_reduce1(this);
		}

		// search up and if it hits null before inference rule, then convert
		Node p;
		Node tmp = this;
		while(((p = tmp.parent) != null) && !(p.syntax instanceof InferenceRule)){
			tmp = p;
		}
		if(p == null){
			return odpe2Maude.subatomise_reduce1(this);
		}


		return this;
	}

*/
	
	public Node interpret(ODPE2Maude odpe2Maude) throws ProverException, IOException, MaudeException {
		boolean flag = false;

		//iterate down the tree, interpretting at every inference rule
		/*
		if (syntax instanceof BinaryOperator)
			for(Node c : children)
				if (c.syntax instanceof InferenceRule)
					flag = true;
		
		if (syntax instanceof InferenceRule || flag) {
			Vector<Node> v = new Vector<Node>();
			for(Node c : children){
				v.add(c.interpret(odpe2Maude));
			}
			return new Node(system, this.getSyntax(),v);
		}
		*/
		return odpe2Maude.interpret_reduce(this);
	}
	
	
	
	/** 		Getters and Setters 
	 *  
	 *  These methods get and set values and nodes
	 *  
	 *  **/
	
	public int getPrecedence() {
		return syntax.getPrecedence();
	}
	
	public SyntaxElement getSyntax() {
		return syntax;
	}
	
	public InferenceSystem getSystem() {
		return system;
	}
	
	public int getDepth(int n) {
		if(parent != null)
			return this.parent.getDepth(n+1);
		return n; 
	}

	public Node getParent() {
		return parent;
	}
	
	public void setParent(Node parent) {
		this.parent = parent;
	}
	
	public void setSelection(int start, int end) {
		selection = new Selection(this, start, end);
	}
	
	public Selection getSelection() {
		return selection;
	}
	
	public int getNumChildren() {
		return children.size();
	}
	
	public Vector<Node> getChildren(){
		return this.children;
	}

	public Node getChild(int index) {
		return children.elementAt(index);
	}

	public void setChild(int index, Node child) {
		children.set(index, child);
		child.parent = this;
	}
	
	public void addChild(Node child) {
		children.add(child);
		setSelection(0, children.size()-1);
		child.parent = this;
	}
	
	/** 		Debugging 
	 *  
	 *  These methods debug the derivation tree
	 *  
	 *  **/
	

	public String prettyprint() {
		String[] rands = new String[children.size()];
		for(int i = 0; i < rands.length; ++i)
			rands[i] = children.get(i).prettyprint();
		return syntax.prettyprint(this, rands);
	}

	private void print(String prefix, boolean isTail) {
		String[] rands = new String[children.size()];
		for(int i = 0; i < rands.length; ++i)
			rands[i] = children.get(i).prettyprint();
        System.out.println(prefix + (isTail ? "└── " : "├── ") + syntax.prettyprint(this,rands));
        for (int i = 0; i < children.size() - 1; i++) {
            children.get(i).print(prefix + (isTail ? "    " : "│   "), false);
        }
        if (children.size() > 0) {
            children.get(children.size() - 1)
                    .print(prefix + (isTail ?"    " : "│   "), true);
        }
    }

	public void print() {
		print("", true);
	}

	public void setSpecial(boolean b) {
		this.special = b;
	}
	
	public boolean getSpecial() {
		return this.special;
	}

	public Pair<Node, Vector<Node>> specialClone(Vector<Node> selectedNodes) {
		Vector<Node> list = new Vector<Node>();
		int n = children.size();
		Vector<Node> newchildren = new Vector<Node>(n);
		for(Node c : children) {
			Pair<Node, Vector<Node>> cln = c.specialClone(selectedNodes);
			list.addAll(cln.snd);
			newchildren.add(cln.fst);
			cln.fst.setSelection(c.selection.getStart(), c.selection.getEnd());
		}
		
		
		
		Node rtn = new Node(system, syntax, newchildren);
		rtn.setSelection(this.selection.getStart(), this.selection.getEnd());
		if(selectedNodes.contains(this))
			list.add(rtn);
		
		return new Pair<Node, Vector<Node>>(rtn, list);
	}
	

}
