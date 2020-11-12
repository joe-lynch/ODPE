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
import odpe.frontend.view.CompositionByConnectives;
import odpe.frontend.view.MarkableString;
import odpe.frontend.view.SelectionInfo;
import odpe.frontend.view.ViewableObject;
import odpe.frontend.view.WidgetView;

public class UnaryPrefixOperator extends UnaryOperator {
    
    private ViewableObject operatorSymbol;
    
    public UnaryPrefixOperator(String id, InferenceSystem sys, 
            int prec, 
            OperandPosition pos,
            ViewableObject opsym) {
        super(id, sys, prec, pos);
        this.operatorSymbol = opsym;
    }
    
    public ViewableObject getOperatorSymbol() {
        return operatorSymbol;
    }

    public Node parse(Lexer l, Parser p) 
            throws ParseError {
        String opsym = operatorSymbol.getTextInputView();
        if(l.current().equals(opsym)) {
            l.next();
            Node rand;
            rand = operandType.parse(l, p);
            return new Node(system, this, rand);
        } else {
            return p.parse(l, precedence-1);
        }
    }

    public WidgetView draw(Node n, WidgetView[] rands) {
        //if necessary perform build between derivations with Join
        for(Node c : n.getChildren())
            if (c.getSyntax() instanceof InferenceRule || rands[n.getChildren().indexOf(c)] instanceof CompositionByConnectives)
                return new CompositionByConnectives(n, rands, operatorSymbol,true);
        MarkableString opsym = (MarkableString)operatorSymbol.getGraphicalView();
        MarkableString res = new MarkableString();
        res.append(opsym);
        res.append((MarkableString)rands[0]);
        res.add(new SelectionInfo(new Selection(n,0,0),0,res.length()));
        return res;
    }

    public String prettyprint(Node n, String[] rands) {
        return operatorSymbol.getTextOutputView() + rands[0];
    }



}
