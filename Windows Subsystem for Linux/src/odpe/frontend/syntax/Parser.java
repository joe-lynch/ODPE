/*  
 * 
 * This file is part of the Open Deduction Proof Editor.
 * 
 * @author Originally created for GraPE by Max Schaefer
 *
 */
package odpe.frontend.syntax;

import odpe.backend.odpe2maude.ODPE2Maude;
import odpe.frontend.ast.Node;

/**
 * The generic parser used to parse formulae input 
 * by the user.
 * <p>It is maintains a table of operators (see 
 * {@link Operator}) sorted by precedence,
 * which is an integer value between <code>0</code> and 
 * <code>MAX_PRECEDENCE</code> (numerically greater precedence
 * means looser binding). Note that there can be at most
 * one operator with a given precedence level.
 * <p>To parse an input, it simply invokes the operators'
 * <code>parse</code> method in the inverse order of their 
 * precedence, starting from the loosest-binding one.
 * <p>An operator's <code>parse</code> method may, in turn,
 * call back into the parser to parse operands (usually
 * giving an initial precedence).
 * 
 * @author Max Schaefer
 *
 */

public class Parser {
	
	/** The table of operators, indexed by precedence. */
	private Operator[] operators;
	
	/** Maximum precedence allowed. */
	private final static int MAX_PRECEDENCE = 255;
	
	/** Constructs a Parser and allocates its operator table. */
	public Parser() {
		operators = new Operator[MAX_PRECEDENCE+1];
	}
	
	/**
	 * Adds an operator. Operators know about their own
	 * precedence, hence it can be extracted from the object.
	 * The method checks whether the precedence is sensible
	 * (i.e., <code>&gt;=0</code> and <code>&lt;=MAX_PRECEDENCE</code>,
	 * and that there is not already an operator with that
	 * precedence; otherwise, an exception is thrown.
	 * 
	 * @param op the operator to be added
	 * @throws InvalidPrecedenceException
	 */

	public void addOperator(Operator op) throws InvalidPrecedenceException {
		int prec = op.getPrecedence();
		if(0 <= prec && prec <= MAX_PRECEDENCE)
			if(operators[prec] == null)
				operators[prec] = op;
			else
				throw new InvalidPrecedenceException("multiple operators with same precedence ("+prec+"): "+
						operators[prec]+" and "+op);
		else
			throw new InvalidPrecedenceException("precedence out of range");
	}

	/**
	 * Parses input provided by a {@link Lexer}, starting at
	 * a given precedence.
	 * @param l the lexer to use
	 * @param prec the precedence to start from; if -1, then parsing starts from <code>MAX_PRECEDENCE</code>
	 * @return the parsed node
	 * @throws ParseError
	 */
	public Node parse(Lexer l, int prec) throws ParseError {
		int i;
		if(prec == -1)
			prec = MAX_PRECEDENCE;
		for(i=prec; i>=0 && operators[i]==null; --i) ;
		if(i < 0)
			throw new ParseError("no matching operator definition found");
		return operators[i].parse(l, this);
	}
	
	/**
	 * Parses input provided by a {@link Lexer}, starting at the
	 * highest priority.
	 * @param l the lexer to use
	 * @return the parsed node
	 * @throws ParseError
	 */
	public Node parse(Lexer l) throws ParseError {
		return parse(l, MAX_PRECEDENCE);
	}

	/**
	 * Parses a given input string using a {@link Lexer}, starting
	 * at the highest priority.
	 * @param str the string to parse
	 * @param l the lexer to use
	 * @return the parsed node
	 * @throws ParseError
	 */
	public Node parse(String str, Lexer l) throws ParseError {
		l.start(str);
		Node res = parse(l, MAX_PRECEDENCE);
		if(!l.current().equals("") && !l.current().equals(">"))
			throw new ParseError("unexpected token: `"+l.current()+"'");
		return res;
	}

}
