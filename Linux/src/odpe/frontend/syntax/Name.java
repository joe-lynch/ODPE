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
import odpe.frontend.view.MarkableString;
import odpe.frontend.view.SelectionInfo;
import odpe.frontend.view.WidgetView;

public class Name extends Operator {
    
    private String name;
    
    public Name(String name, InferenceSystem sys, int prec) {
        super("name", sys, prec);
        this.name = name;
    }
    
    public Name(InferenceSystem sys) {
        super("name", sys, 0);
        this.name = null;
    }
    
    public String getName() {
        return name;
    }
    
    public Node parse(Lexer l, Parser p) 
            throws ParseError {
        if(l.current().equals(""))
            throw new ParseError("expected name, but found end of input");
        String name = l.current();
        l.next();
        return new Node(system, new Name(name, system, getPrecedence()));
    }

    public WidgetView draw(Node n, WidgetView[] rands) {
        MarkableString res = new MarkableString(name);
        res.add(
            new SelectionInfo(
                new Selection(n,0,-1),0,name.length()));
        return res;
    }

    public String prettyprint(Node n, String[] rands) {
        return name;
    }

    public String texify(Node n, String[] rands) {
        return "\\mathit{"+name+"}";
    }

    public int getOperandPrecedence(int i) {
        throw new IndexOutOfBoundsException("name shouldn't be asked for operand precedence");
    }

}
