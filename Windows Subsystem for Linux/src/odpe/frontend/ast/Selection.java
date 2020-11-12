/*  
 * 
 * This file is part of the Open Deduction Proof Editor.
 * 
 * @author Joe Lynch
 *
 */
package odpe.frontend.ast;

import java.util.Vector;

import odpe.frontend.syntax.Constant;
import odpe.frontend.syntax.SyntaxElement;
import odpe.util.Triple;

public class Selection {
    
    private Node node;
    private int start;
    private int end;
    
    public Selection(Node node, int start, int end) {
        if(start < 0 || end < -1)
            throw new IndexOutOfBoundsException("invalid start or end");
        this.node = node;
        this.start = start;
        this.end = end;
    }
    
    public Selection(Node node) {
        this.node = node;
        this.start = 0;
        this.end = node.getNumChildren() - 1;
    }
    
    public Integer getStart() {
        return start;
    }
    
    public int getEnd() {
        return end;
    }
    
    public Node getSurroundingNode() {
        return node;
    }
    
    public boolean isCompleteNode() {
        if (node.getSyntax() instanceof Constant && node.getNumChildren() == 2) return true;
        return start == 0 && end == node.getNumChildren() - 1;
    }
    
    public Node asNode() {
        if(isCompleteNode())
            return node;
        Vector<Node> children = new Vector<Node>();
        for(int i = start; i <= end; ++i)
            children.add(node.getChild(i));
        Node n = new Node(node.getSystem(), node.getSyntax(), children);
        return n;
    }
    
    public Triple<Vector<Node>, Vector<Integer>, SyntaxElement> notSelected() {
        Vector<Node> children = new Vector<Node>();
        Vector<Integer> index = new Vector<Integer>();
        for(int i = 0; i < start; i++) {
            children.add(node.getChild(i));
            index.add(i);
        }
        for(int i = end+1; i<node.getNumChildren(); i++) {
            children.add(node.getChild(i)); //i-1????
            index.add(i);
        }
        if (children.size() == 0)
            return null;

        return new Triple<Vector<Node>, Vector<Integer>, SyntaxElement>(children, index, node.getSyntax());
    }
    
    public Node replace(Node root, Node newNode) {
        if(isCompleteNode()) {
            Node p = node.getParent();
            if(p == null) {
                return newNode;
            } else
                p.replaceChild(node, newNode);
        } else {
            node.replaceChildren(start, end, newNode);
            return node;
        }
        return root;
    }
    
    public String toString() {
        return "(selection in "+node.prettyprint()+", start="+start+", end="+end+")";
    }
    
}
