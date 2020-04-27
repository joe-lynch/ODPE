/*  
 * 
 * This file is part of the Open Deduction Proof Editor.
 * 
 * @author Originally created for GraPE by Max Schaefer
 *
 */
package odpe.frontend.syntax;

import odpe.frontend.ast.Node;
import odpe.frontend.ast.Selection;
import odpe.frontend.model.InferenceSystem;
import odpe.frontend.view.*;

public class Constant extends Operator {
	
	private ViewableObject name;
	
	public Constant(String id, InferenceSystem sys, 
			int prec, ViewableObject name) {
		super(id, sys, prec);
		this.name = name;
	}
	
	public ViewableObject getName() {
		return name;
	}

	public Node parse(Lexer l, Parser p) 
			throws ParseError {
		if(l.current().equals(name.getTextInputView())) {
			l.next();
			return new Node(system, this);
		} else if(precedence == 0) {
			throw new ParseError("unexpected token: "+
					(l.current().equals("") ? "EOF" :
						l.current()));
		} else {
			return p.parse(l, precedence-1);
		}
	}

	public WidgetView draw(Node n, WidgetView[] rands) {
		String str = name.getGraphicalView().toString();
		if (rands.length == 2) {

			for(Node c : n.getChildren())
				if (c.getSyntax() instanceof InferenceRule || rands[n.getChildren().indexOf(c)] instanceof CompositionByConnectives)
					return new CompositionByConnectives(n, rands, new SimpleViewableObject(n.getSyntax().getID()), true);

			MarkableString ms0 = ((MarkableString)rands[0]);
			MarkableString ms2 = ((MarkableString)rands[1]);	
			MarkableString lbl = new MarkableString(str);
			
			MarkableString res = new MarkableString();
			
			res.append(ms0);
			res.append(" ");
			res.append(lbl);
			res.append(" ");
			res.append(ms2);
			
			res.add(new SelectionInfo(new Selection(n, 0, rands.length), 0, res.length()));

			
			return res;
		}
		else if ((n.getParent()!=null) && n.getParent().getSyntax() instanceof Constant){
			MarkableString lbl = new MarkableString(str);
			lbl.add( new SelectionInfo( new Selection(n,0,0),0,0));
			return lbl;
		}
		else {
			MarkableString lbl = new MarkableString(str);
			lbl.add( new SelectionInfo( new Selection(n,0,-1),0,lbl.length()));
			return lbl;
		}
	}

	public String prettyprint(Node n, String[] rands) {
		return name.getTextOutputView();
	}

	public int getOperandPrecedence(int i) {
		throw new IndexOutOfBoundsException("constant shouldn't be asked for operand precedence");
	}

}
