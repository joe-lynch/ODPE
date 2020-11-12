/*  
 * 
 * This file is part of the Open Deduction Proof Editor.
 * 
 * @author Originally created for GraPE by Max Schaefer
 *
 */
package odpe.frontend.syntax;

import odpe.frontend.ast.ActiveCharacter;
import odpe.frontend.ast.Node;
import odpe.frontend.model.InferenceSystem;
import odpe.frontend.view.ActiveCharacterInfo;
import odpe.frontend.view.MarkableString;
import odpe.frontend.view.ViewableObject;
import odpe.frontend.view.WidgetView;

import java.util.Vector;



public class ListOperandPosition extends OperandPosition {
    
    private ViewableObject separator;
    private boolean commutative;
    
    private static int counter = 0;
    
    public ListOperandPosition(InferenceSystem sys, 
            int prec, boolean comm, ViewableObject sep) {
        super("list-operand"+counter, sys, prec);
        ++counter;
        this.separator = sep;
        this.commutative = comm;
    }
    
    public ViewableObject getSeparator() {
        return separator;
    }
    
    public boolean isCommutative() {
        return commutative;
    }

    public Node parse(Lexer l, Parser p) 
            throws ParseError {
        String sep = separator.getTextInputView();
        Vector<Node> rands = new Vector<Node>();
        rands.add(p.parse(l, precedence));
        while(l.current().equals(sep)) {
            l.next();
            rands.add(p.parse(l, precedence));
        }
        return new Node(system, this, rands);
    }

    public WidgetView draw(Node n, WidgetView[] rands) {
        MarkableString sepsym = 
            (MarkableString)separator.getGraphicalView();
        MarkableString res = new MarkableString();
        for(int i = 0; i < rands.length; ++i) {
            if(i != 0) {
                if(commutative)
                    res.add(
                        new ActiveCharacterInfo(
                            new ActiveCharacter(n, i),
                            res.length()));
                res.append(sepsym);
                res.append(" ");
            }
            res.append((MarkableString)rands[i]);
        }
        return res;
    }

    public String prettyprint(Node n, String[] rands) {
        //String sepsym = separator.getTeXView();
        StringBuffer res = new StringBuffer();
        for(int i = 0; i < rands.length; ++i) {
            if(i != 0) {
                //res.append(sepsym);
                res.append(" ");
            }
            res.append(rands[i]);
        }
        return res.toString();
    }

    public int getOperandPrecedence(int i) {
        return precedence;
    }

}
