/*  
 * 
 * This file is part of the Open Deduction Proof Editor.
 * 
 * @author Originally created for GraPE by Max Schaefer
 *
 */
package odpe.frontend.syntax;

import java.util.Vector;

import odpe.frontend.ast.ActiveCharacter;
import odpe.frontend.ast.Node;
import odpe.frontend.ast.Selection;
import odpe.frontend.model.InferenceSystem;
import odpe.frontend.view.ActiveCharacterInfo;
import odpe.frontend.view.CompositionByConnectives;
import odpe.frontend.view.MarkableString;
import odpe.frontend.view.SelectionInfo;
import odpe.frontend.view.ViewableObject;
import odpe.frontend.view.WidgetView;


public class BinaryOutfixOperator extends BinaryOperator {

	private ViewableObject startSymbol, separatorSymbol, endSymbol;
	
	// note: for associative operators, left and right operand
	//       position have to be the same (insert a check?)
	public BinaryOutfixOperator(String id, InferenceSystem sys,
			int prec, 
			OperandPosition left, OperandPosition right,
			boolean assoc, boolean comm,
			ViewableObject startsym, 
			ViewableObject sepsym, ViewableObject endsym) {
		super(id, sys, prec, left, right, assoc, comm);
		this.startSymbol = startsym;
		this.separatorSymbol = sepsym;
		this.endSymbol = endsym;
	}
	
	public ViewableObject getStartSymbol() {
		return startSymbol;
	}
	
	public ViewableObject getSeparatorSymbol() {
		return separatorSymbol;
	}
	
	public ViewableObject getEndSymbol() {
		return endSymbol;
	}

	public Node parse(Lexer l, Parser p) throws ParseError {
		String startsym = startSymbol.getTextInputView();
		String sepsym = separatorSymbol.getTextInputView();
		String endsym = endSymbol.getTextInputView();
		if(l.current().equals(startsym)) {
			l.next();
			Vector<Node> rands = new Vector<Node>();
			rands.add(leftOperand.parse(l, p));
			if(associative) {
				while(l.current().equals(sepsym)) {
					l.next();
					rands.add(rightOperand.parse(l, p));
				}
			} else if(l.current().equals(sepsym)) {
					l.next();
					rands.add(rightOperand.parse(l, p));
			}
			if(!l.current().equals(endsym))
				throw new ParseError("end symbol `"+endsym+
						"' expected");
			l.next();
			if(rands.size() == 1)
				return rands.elementAt(0);
			return new Node(system, this, rands);
		} else {
			return p.parse(l, precedence-1);
		}
	}

	public WidgetView draw(Node n, WidgetView[] rands) {
		
		//if necessary perform build between derivations with Join
		for(Node c : n.getChildren())
			if (c.getSyntax() instanceof Rule || rands[n.getChildren().indexOf(c)] instanceof CompositionByConnectives)
				return new CompositionByConnectives(n, rands, separatorSymbol);
		
		//initiate variables
		MarkableString startsym = (MarkableString)startSymbol.getGraphicalView();
		MarkableString sepsym = (MarkableString)separatorSymbol.getGraphicalView();
		MarkableString endsym = (MarkableString)endSymbol.getGraphicalView();
		MarkableString res = new MarkableString();
		Vector<Integer> gaps = new Vector<Integer>();
		
		//build the formula
		res.append(startsym);
		int gapwidth = 1+sepsym.length()+1;
		int l = rands.length;
		for(int i = 0; i < l; ++i) {
			if(i != 0) {
				res.append(" ");
				res.append(sepsym);
				res.append(" ");
				if(commutative)
					res.add(new ActiveCharacterInfo(new ActiveCharacter(n, i),res.length()-2));
			}
			gaps.add(res.length());
			res.append((MarkableString)rands[i]);
		}
		res.append(endsym);
		
		//add selection info to the formula
		for(int j = 0; j < gaps.size() - 1; ++j) {
			for(int k = j+1; k < gaps.size() - 1; ++k)
				res.add(new SelectionInfo(new Selection(n, j, k), gaps.get(j), gaps.get(k+1)-gapwidth));
			if(j != 0)
				res.add(new SelectionInfo(new Selection(n, j, gaps.size()-1), gaps.get(j), res.length()-endsym.length()));
		}
		res.add(new SelectionInfo(new Selection(n, 0, rands.length-1), 0, res.length()));

		return res;
	}

	public String prettyprint(Node n, String[] rands) {
		String startsym = startSymbol.getTextOutputView(),
			   sepsym = separatorSymbol.getTextOutputView(),
			   endsym = endSymbol.getTextOutputView();
		StringBuffer res = new StringBuffer();
		res.append(startsym);
		for(int i = 0; i < rands.length; ++i) {
			if(i != 0) {
				res.append(sepsym);
				res.append(" ");
			}
			res.append(rands[i]);
		}
		res.append(endsym);
		return res.toString();
	}

	
}
