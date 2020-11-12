/*  
 * 
 * This file is part of the Open Deduction Proof Editor.
 * 
 * @author Originally created for GraPE by Max Schaefer
 *
 */
package odpe.backend;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Vector;

import odpe.backend.odpe2maude.MaudeException;
import odpe.frontend.ast.Node;
import odpe.frontend.syntax.Rule;
import odpe.frontend.syntax.SyntaxElement;
import odpe.util.Pair;

/**
 * Interface that all theorem proving backends must implement.
 * It makes sure that the backend implements methods to normalize
 * a derivation and to find rewrite steps; another method is
 * provided to set prover-specific configuration parameters.
 * <p>Additionally, each backend must have a constructor with the
 * signature <code>Prover(InferenceSystem)</code>. Note that this
 * constraint is not enforceable via Java's interface mechanism.
 * 
 * @author Max Schaefer
 *
 */

public interface Prover {

    /**
     * Sets a prover-specific parameter. There is one standard
     * parameter that every prover must support (although it can,
     * of course, ignore it), namely "directory", which sets the
     * prover's current working directory.
     * 
     * @param name name of the parameter to set
     * @param args map of key-value pairs describing the parameter value
     * @throws ProverException
     */
    public void setParam(String name,
                         Map<String, String> args) throws ProverException;
    
    /**
     * Starts the prover. May throw a <code>ProverException</code>
     * if the prover is not completely initialized (for example, if
     * a needed parameter has not been set yet).
     * 
     * @throws ProverException
     */
    public void start() throws ProverException;
    
    /**
     * Normalizes a given derivation. This might entail, for example,
     * converting every formula to negation normal form.
     * 
     * @param derivation the derivation to normalize
     * @return the normalized derivation
     * @throws ProverException
     */
    public Node normalize(Node derivation) throws ProverException;
    
    /**
     * Finds the possible rewrites for a given node using some rule
     * from a given collection of rules.
     * 
     * @param n the node to rewrite
     * @param rules rules that can be used for rewriting
     * @return a collection of <code>Rewrite</code>s detailing every single rewrite
     * @throws ProverException
     */
    //private Pair<Vector<Rewrite>, Vector<Rewrite>> findRewrites(Node n, Collection<Rule> rules, boolean isLeft, String inf, String rinf)
    //  throws ProverException;
    
    /**
     * Aborts the provers current activity. This will only be called
     * if the user presses the abort button in the toolbar.
     *  
     * @throws ProverException
     */
    public void abort() throws ProverException;

    public Node subatomise_convert(Node derivation) throws ProverException, IOException, MaudeException;

    public Node interpret_convert(Node derivation) throws ProverException, IOException, MaudeException;;

    public Node test(String str) throws ProverException, IOException, MaudeException;

    public Node proofSearch(Node proof) throws IOException, MaudeException, ProverException;

    Pair<Vector<Rewrite>, Vector<Rewrite>> findSubatomicRewrites(Node n, Collection<Rule> rules, boolean allLeft, boolean allRight, boolean isSingle)
            throws ProverException;

    Pair<Vector<Rewrite>, Vector<Rewrite>> findNormalRewrites(Node n, Collection<Rule> rules, boolean allLeft, boolean allRight, boolean isSingle)
            throws ProverException;

}
