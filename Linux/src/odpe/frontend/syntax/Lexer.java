/*  
 * 
 * This file is part of the Open Deduction Proof Editor.
 * 
 * @author Originally created for GraPE by Max Schaefer
 *
 */
package odpe.frontend.syntax;

import java.util.Vector;

public class Lexer {

    private String input;
    private int pos;
    private String current;
    private Vector<String> tokens;
    
    public Lexer() {
        this.input = "";
        this.pos = 0;
        this.tokens = new Vector<String>();
    }
    
    public void start(String input) {
        this.input = input;
        this.pos = 0;
        update_current();
    }
    
    public void addToken(String tk) {
        int i;
//      System.out.println("adding token "+tk);
        for(i = 0; i < tokens.size(); ++i)
            if(tokens.get(i).length() < tk.length())
                break;
        tokens.add(i, tk);
    }
    
    public void update_current() {
        if(pos >= input.length()) {
            current = "";
        } else {
            while(Character.isWhitespace(input.charAt(pos)))
                ++pos;
            if(pos >= input.length())
                current = "";
            else {
                String tk = token_at_pos(pos);
                if(tk == null) {
                    int j;
                    for(j=pos+1; j < input.length() && token_at_pos(j)==null; ++j)
                        ;
                    current = input.substring(pos, j);
                    pos = j;
                } else {
                    current = tk;
                    pos += tk.length();
                }
            }
        }
    }
    
    private String token_at_pos(int p) {
        String tmp = input.substring(p);
        for(String tk : tokens)
            if(tmp.startsWith(tk))
                return tk;
        return null;
    }
    
    public String current() {
        return current;
    }
    
    public String next() {
        update_current();
        return current();
    }
    
}
