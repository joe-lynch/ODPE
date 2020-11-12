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
import odpe.frontend.view.MarkableString;
import odpe.frontend.view.SelectionInfo;
import odpe.frontend.view.ViewableObject;
import odpe.frontend.view.WidgetView;


public class BinaryInfixOperator extends BinaryOperator {
    
    private ViewableObject operatorSymbol;
    
    // note: for associative operators, left and right operand
    //       position have to be the same (insert a check?)
    public BinaryInfixOperator(String id, InferenceSystem sys,
            int prec, 
            OperandPosition left, OperandPosition right,
            boolean assoc, boolean comm,
            ViewableObject opsym) {
        super(id, sys, prec, left, right, assoc, comm);
        this.operatorSymbol = opsym;
    }
    
    public ViewableObject getOperatorSymbol() {
        return operatorSymbol;
    }
    
    public Node parse(Lexer l, Parser p) 
            throws ParseError {
        Vector<Node> rands = new Vector<Node>();
        String opsym = operatorSymbol.getTextInputView();
        rands.add(leftOperand.parse(l, p));
        do {
            if(l.current().equals(opsym)) {
                l.next();
                rands.add(rightOperand.parse(l, p));
            } else {
                break;
            }
        } while(associative);
        if(rands.size() == 1)
            return rands.elementAt(0);
        return new Node(system, this, rands);
    }

    public WidgetView draw(Node n, WidgetView[] rands) {
        MarkableString res = new MarkableString();
        MarkableString opsym = 
            (MarkableString)operatorSymbol.getGraphicalView();
        Vector<Integer> gaps = new Vector<Integer>();
        int gapwidth = 1+opsym.length()+1;
        int l = rands.length;
        for(int i = 0; i < l; ++i) {
            if(i != 0) {
                res.append(" ");
                res.append(opsym);
                res.append(" ");
                if(commutative)
                    res.add(
                        new ActiveCharacterInfo(
                            new ActiveCharacter(n, i), 
                            res.length()-2));
            }
            gaps.add(res.length());
            res.append((MarkableString)rands[i]);
        }
        for(int j = 0; j < gaps.size() - 1; ++j) {
            for(int k = j+1; k < gaps.size() - 1; ++k) {
                res.add(
                    new SelectionInfo(
                        new Selection(n, j, k), 
                        gaps.get(j), gaps.get(k+1)-gapwidth));
            }
            if(j != 0)
                res.add(
                    new SelectionInfo(
                        new Selection(n, j, gaps.size()-1),
                        gaps.get(j), res.length()));
        }
        res.add(new SelectionInfo(
                new Selection(n, 0, rands.length-1),
                0, res.length()));
        return res;
    }

    public String prettyprint(Node n, String[] rands) {
        StringBuffer res = new StringBuffer();
        String opsym = operatorSymbol.getTextOutputView();
        for(int i = 0; i < rands.length; ++i) {
            if(i != 0) {
                res.append(" ");
                res.append(opsym);
                res.append(" ");
            }
            res.append(rands[i]);
        }
        return res.toString();
    }

}
