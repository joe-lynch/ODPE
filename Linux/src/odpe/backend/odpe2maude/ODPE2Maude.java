/*  
 * 
 * This file is part of the Open Deduction Proof Editor.
 * 
 * @author Adaption by Joe Lynch from the original file created for GraPE by Max Schaefer
 *
 */
package odpe.backend.odpe2maude;

import odpe.backend.Prover;
import odpe.backend.ProverException;
import odpe.backend.Rewrite;
import odpe.frontend.ast.Node;
import odpe.frontend.model.InferenceSystem;
import odpe.frontend.syntax.BinaryInfixOperator;
import odpe.frontend.syntax.BinaryOperator;
import odpe.frontend.syntax.BinaryOutfixOperator;
import odpe.frontend.syntax.Constant;
import odpe.frontend.syntax.InferenceRule;
import odpe.frontend.syntax.ListOperandPosition;
import odpe.frontend.syntax.Name;
import odpe.frontend.syntax.Operator;
import odpe.frontend.syntax.Rule;
import odpe.frontend.syntax.SingletonOperandPosition;
import odpe.frontend.syntax.SyntaxElement;
import odpe.frontend.syntax.UnaryPrefixOperator;
import odpe.util.Pair;

import java.io.File;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import java.util.Collection;
import java.util.Map;
import java.util.Vector;

public class ODPE2Maude implements Prover {
    
    private static final int PUSHBACK_CAPACITY = 256;
    
    private InferenceSystem system;
    private Vector<Pair<SyntaxElement, String>> maude_names;
    private Vector<Pair<SyntaxElement, String>> other_maude_names;
    private Vector<String> files;
    private Vector<String> ignore_patterns;
    private String inferencer;
    private String reverseinferencer;
    private String sa_inferencer;
    private String sa_reverseinferencer;
    private String normalizer;
    private String subatomiser;
    private String interpreter;
    private String result_sort;
    private File dir;
    private Maude maude;
    private Boolean interpret = false;
    private Boolean proofSearch = false;

    
    public ODPE2Maude(InferenceSystem sys) throws ProverException {
        this.system = sys;
        this.ignore_patterns = new Vector<String>();
        this.maude_names = new Vector<Pair<SyntaxElement, String>>();
        for (SyntaxElement elt : this.system.getSyntaxElements()) {
            //System.out.println("setting Maude name for "+elt);
            maude_names.add(new Pair<SyntaxElement, String>(elt, defaultMaudeName(elt)));
        }
        this.files = new Vector<String>();
        File util = new File("util.maude");
        if (!util.exists())
            throw new ProverException("unable to find file util.maude");
        //files.add(util.getAbsolutePath());
        files.add(util.getName());
        this.dir = null;
    }

    public Vector<Pair<SyntaxElement, String>> getOtherMaudeNames(){
        return maude_names;
    }

    private String getMaudeName(SyntaxElement op) {
        for(Pair<SyntaxElement, String> p : maude_names) {
            if (p.fst.equals(op)
                    || (p.fst.getID().equals(op.getID())))
                return p.snd;
        }
        return null;
    }
    
    private void setMaudeName(SyntaxElement op, String name) {
        for(Pair<SyntaxElement, String> p : maude_names)
            if(p.fst.equals(op)) {
                p.snd = name;
                return;
            }
        maude_names.add(new Pair<SyntaxElement, String>(op, name));
    }
    
    private SyntaxElement getSyntaxElement(String name) {
        for(Pair<SyntaxElement, String> p : maude_names)
            if(p.snd.equals(name))
                return p.fst;
        return null;
    }
    
    private static String quotemeta(String str) {
        StringBuffer buf = new StringBuffer();
        for(int i = 0; i < str.length(); ++i) {
            char c = str.charAt(i);
            if("{[(,)]}".indexOf(c) != -1)
                buf.append("`").append(c);
            else
                buf.append(c);
        }
        return buf.toString();
    }
    
    private static String unquotemeta(String str) {
        StringBuffer buf = new StringBuffer();
        for(int i = 0; i < str.length(); ++i) {
            char c = str.charAt(i);
            if(c == '`')
                buf.append(str.charAt(++i));
            else
                buf.append(c);
        }
        return buf.toString();
    }
    
    private static String defaultMaudeName(SyntaxElement elt) throws ProverException {
        if(elt instanceof Rule) {
            return quotemeta(elt.getID());
        } else if(elt instanceof BinaryInfixOperator) {
            String opsym = ((BinaryInfixOperator)elt).getOperatorSymbol().getTextInputView();
            return quotemeta("_"+opsym+"_"); 
        } else if(elt instanceof BinaryOutfixOperator) {
            BinaryOutfixOperator bop = (BinaryOutfixOperator)elt;
            String startsym = bop.getStartSymbol().getTextInputView(),
                   sepsym = bop.getSeparatorSymbol().getTextInputView(),
                   endsym = bop.getEndSymbol().getTextInputView();
            return quotemeta(startsym+"_"+sepsym+"_"+endsym);
        } else if(elt instanceof Constant) {
            return quotemeta(((Constant)elt).getName().getTextInputView());
        } else if(elt instanceof ListOperandPosition) {
            String sep = 
                ((ListOperandPosition)elt).getSeparator().getTextInputView();
            return quotemeta("_"+sep+"_");
        } else if(elt instanceof Name) {
            return "name";
        } else if(elt instanceof SingletonOperandPosition) {
            return "";
        } else if(elt instanceof UnaryPrefixOperator) {
            String opsym = 
                ((UnaryPrefixOperator)elt).getOperatorSymbol().getTextInputView();
            return quotemeta(opsym+"_");
        } else {
            throw new ProverException("unknown syntax element type");
        }
    }
    
    private String maudify(Node tree) {
        SyntaxElement elt = tree.getSyntax();
        String tmp;
        if(elt instanceof InferenceRule) {
            StringBuffer res = new StringBuffer();
            res.append(quotemeta("_>[_]>_"));
            res.append("((");
            res.append(maudify(tree.getChild(0)));
            res.append("),'");
            res.append(quotemeta(getMaudeName(elt)));
            res.append(",(");
            int n = tree.getNumChildren();
            for(int i = 1; i < n; ++i) {
                if(i != 1)
                    res.append("|");
                tmp = maudify(tree.getChild(i));
                if(tmp.startsWith(quotemeta("_>[_]>_")))
                    res.append(tmp);
                else
                    res.append("(").append(tmp).append(")");
                    //res.append("premise(upTerm(").append(tmp).append("))");
            }
            res.append("))");
            return res.toString();
        } else if(elt instanceof Name) {
            Name n = (Name)elt;
            return "'"+quotemeta(n.getName());
        } else if(elt instanceof Constant && tree.getChildren().isEmpty()) {
            return getMaudeName((Constant)elt);
        } else if(elt instanceof ListOperandPosition && tree.getNumChildren() == 1) {
            return maudify(tree.getChild(0));
        } else {
            String maudename = getMaudeName((Operator)elt);
            if(maudename.equals(""))
                return maudify(tree.getChild(0));
            int n = tree.getNumChildren();
            StringBuffer res;
            if(elt instanceof BinaryOperator || elt instanceof ListOperandPosition) {
                res = new StringBuffer();
                for(int i = 0; i < n - 1; ++i) {
                    res.append(maudename).append("(");
                    res.append(maudify(tree.getChild(i)));
                    res.append(",");
                }
                res.append(maudify(tree.getChild(n-1)));
                res.append(fill(n-1, ')'));
            } else if (elt instanceof  Constant && !tree.getChildren().isEmpty()) {
                if (interpret)
                    return maude_interpretise(tree);

                res = new StringBuffer();
                res.append(quotemeta("___"));
                res.append("(");
                res.append(maudify(tree.getChild(0)));
                res.append(",");
                res.append(maudename);
                res.append(",");
                res.append(maudify(tree.getChild(1)));
                res.append(")");
            }
            else {
                res = new StringBuffer(maudename);
                res.append("(");
                for(int i = 0; i < n; ++i) {
                    if(i != 0)
                        res.append(",");
                    res.append(maudify(tree.getChild(i)));
                }
                res.append(")");
            }
            return res.toString();
        }
    }
    
    private String fill(int n, char c) {
        StringBuffer res = new StringBuffer();
        for(;n>0;--n)
            res.append(c);
        return res.toString();
    }
    
    private final String magic = "{[(,)]}";

    private String lex(PushbackReader r) {
        try {
            int c = r.read();
            if(c == -1)
                return "";
            if (Character.isWhitespace(c))
                return lex(r);
            if (magic.indexOf(c) != -1)
                return "" + (char)c;
            StringBuffer buf = new StringBuffer();
            do {
                buf.append((char)c);
                if((char)c == '`') {
                    c = r.read();
                    buf.append((char)c);
                }
            } while (((c = r.read()) != -1)
                    && (!Character.isWhitespace((char)c))
                    && (magic.indexOf((char)c) == -1));
            if(c != -1)
                r.unread(c);
            return buf.toString();
        } catch(IOException ioe) {
            return "";
        }
    }
    
    private boolean is_name(String str) {
        return !(str.equals("") || magic.contains(str));
    }

    private String normalizeString(String str) throws ProverException, IOException, MaudeException {
        return str.replaceAll("-(.*?)", "- $1")
                   .replaceAll(">", " > ")
                   .replaceAll("Q\\d+", "'nowt").replaceAll("Q","'nowt")
                   .replaceAll("phi(\\d+)","phi$1:Structure");
    }

    public Node test(String str) throws ProverException, IOException, MaudeException {
        String reducedString = maude.reduce(normalizer, maude.reduce(subatomiser, normalizeString(str)));
        String odpeReadable = reducedString.replaceAll("phi(\\d+):Structure","phi$1");
        return parse(odpeReadable, inferencer);
    }

    private String proofSearchableString(String str) throws ProverException, IOException, MaudeException {
        String proofSearchable = str.replaceAll("phi(\\d+)", "phi$1:Structure");
        int i = 0;
        while (proofSearchable.contains("'nowt")) {
            proofSearchable = proofSearchable.replaceFirst("'nowt", "Q" + i + ":Qid");
            i++;
        }
        return proofSearchable;
    }

    public Node proofSearch(Node proof) throws IOException, MaudeException, ProverException {
        //check if subatomised or not, and change the parse and module (future work)
        proofSearch = true;
        String trm = proofSearchableString(maudify(proof));
        String reducedString = maude.reduce("SamStr", "unwrite(downTerm(start("+trm+"),E:Structure))");
        return parse(reducedString, subatomiser);
    }

    public Node parse(String str, String inferencer) throws ProverException {
        PushbackReader r = new PushbackReader(new StringReader(str), PUSHBACK_CAPACITY);
        return parse(r, inferencer);
    }

    private Node parse(PushbackReader r, String inferencer) throws ProverException {
        String curtk = lex(r);
        if (unquotemeta(curtk).equals("_>[_]>_")) {
            if (!lex(r).equals("("))
                throw new ProverException("`(' expected");
            Node conc = parse(parseTerm(r), inferencer); //downTerm(parseTerm(r), inferencer);
            if (!lex(r).equals(","))
                throw new ProverException("`,' expected");
            String k = lex(r);
            InferenceRule i;
            String n = null;
            if ( !k.startsWith("'") ){
                i = new InferenceRule("nowt", " ", system, "nowt", true, false);
            }
            else {
                n = unquotemeta(k.substring(1));
                i = (InferenceRule) getSyntaxElement(n);
            }

            if (r == null)
                throw new ProverException("no such inference rule: " + n);
            if (!lex(r).equals(","))
                throw new ProverException("`,' expected");
            curtk = lex(r);
            Vector<Node> args = new Vector<Node>();
            args.add(conc);
            if (unquotemeta(curtk).equals("_|_")) {
                if (!lex(r).equals("("))
                    throw new ProverException("`(' expected");
                do {
                    args.add(parse(r, inferencer));
                    curtk = lex(r);
                    if (curtk.equals(")"))
                        break;
                    else if (curtk.equals(","))
                        continue;
                    else
                        throw new ProverException("`,' or `)' expected");
                } while (true);
            } else {
                try {
                    r.unread(curtk.toCharArray());
                } catch (IOException ioe) {
                    throw new ProverException("couldn't unread");
                }
                args.add(parse(r, inferencer));
                if (!lex(r).equals(")"))
                    throw new ProverException("`)' expected");
            }
            return new Node(system, i, args);
        } else if (unquotemeta(curtk).equals("___")) {
            if (!lex(r).equals("("))
                throw new ProverException("`(' expected");
            Node conc = parse(parseTerm(r),inferencer); //ff
            if (!lex(r).equals(","))
                throw new ProverException("`,' expected");
            //PushbackReader re =
            //  new PushbackReader(new StringReader(lex(r)),
            //          PUSHBACK_CAPACITY);  //a
            Operator rator = (Operator) getSyntaxElement(lex(r));
            if (!lex(r).equals(","))
                throw new ProverException("`,' expected");
            Node prem = parse(parseTerm(r), inferencer);  //tt
            if (!lex(r).equals(")"))
                throw new ProverException("`)' expected");
            Vector<Node> v = new Vector<Node>();
            v.add(conc);
            v.add(prem);
            Node n = new Node(system, rator, v);
            return n;
        } else if (unquotemeta(curtk).equals("premise")) {
            if (!lex(r).equals("("))
                throw new ProverException("`(' expected");
            Node n = downTerm(parseTerm(r), inferencer);
            if (!lex(r).equals(")"))
                throw new ProverException("`)' expected");
            return n;
        } else if (is_name(curtk) || curtk.equals("(")) {
            Operator rator = (Operator) getSyntaxElement(curtk);
            if (rator == null) {
                if (curtk.codePointAt(0) == '\'')
                    curtk = curtk.substring(1);
                return new Node(system, new Name(curtk, system, 0),
                        new Vector<Node>());
            } else if (rator instanceof Constant) {
                return new Node(system, rator, new Vector<Node>());
            } else {
                curtk = lex(r);
                if (!curtk.equals("("))
                    throw new ProverException("expected `('");
                Vector<Node> rands = new Vector<Node>();
                boolean flag = true;
                do {
                    if (curtk.equals("(")) {
                        try {
                            int c = r.read();
                            if (c != 40)
                                r.unread(c);
                            else
                                flag = false;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                    rands.add(parse(r, inferencer));
                    curtk = lex(r);
                    if (curtk.equals(")") && flag)
                        break;
                    if (curtk.equals(")") && !flag) {
                        flag = true;
                        lex(r);
                        lex(r);
                        curtk = lex(r);
                        continue;

                    }
                    if (curtk.equals(","))
                        continue;
                    else
                        throw new ProverException("expected `)' or `,', found " + curtk);
                } while (true);
                return new Node(system, rator, rands);
            }
        }
        else{
                throw new ProverException("operator expected -- found " + curtk + " instead");
            }
    }
    
    private String parseTerm(PushbackReader r)
            throws ProverException {
        try {
            String functor = lex(r);
            int c = r.read();
            if((char)c == '(') {
                Vector<String> arglist = parseTermList(r);
                StringBuffer res = new StringBuffer(functor);
                res.append("(");
                for(int i = 0; i < arglist.size(); ++i) {
                    if(i != 0)
                        res.append(", ");
                    res.append(arglist.get(i));
                }
                res.append(")");
                return res.toString();
            } else {
                if(c != -1)
                    r.unread(c);
                return functor;
            }
        } catch(IOException ioe) {
            throw new ProverException("an I/O exception occured: "+ioe);
        }
    }
    
    private Vector<String> parseTermList(PushbackReader r) 
            throws ProverException {
        try {
            Vector<String> res = new Vector<String>();
            int c;
            while(true) {
                res.add(parseTerm(r));
                c = r.read();
                if((char)c == ')')
                    break;
                if((char)c != ',')
                    throw new ProverException("`,' expected");
            }
            return res;
        } catch(IOException ioe) {
            throw new ProverException("an I/O exception occured: "+ioe);
        }
    }
    
    private Vector<String> parseTermOrTermList(String input, String inferencer)
        throws ProverException {
        try {
            PushbackReader r = new PushbackReader(new StringReader(input), PUSHBACK_CAPACITY);


            String curtk = lex(r);
            if(unquotemeta(curtk).equals("_,_")) {
                if(!lex(r).equals("("))
                    throw new ProverException("`,' expected");
                return parseTermList(r);
            } else {
                r.unread(curtk.toCharArray());
                Vector<String> tmp = new Vector<String>(1);
                tmp.add(parseTerm(r));
                return tmp;
            }
        } catch(IOException ioe) {
            throw new ProverException("an I/O exception occured: "+ioe);
        }
    }

    public void setParam(String name, Map<String, String> args) throws ProverException {
        if(name.equals("load")) {
            File f = new File(args.get("name"));
            if(!f.exists())
                throw new ProverException("file does not exist: "+dir);

            files.add(f.getName());
        } else if(name.equals("inferencer"))
            inferencer = args.get("name");
        else if(name.equals("reverseinferencer"))
            reverseinferencer = args.get("name");
        else if(name.equals("sa_inferencer"))
            sa_inferencer = args.get("name");
        else if(name.equals("sa_reverseinferencer"))
            sa_reverseinferencer = args.get("name");
        else if(name.equals("normalizer"))
            normalizer = args.get("name");
        else if(name.equals("subatomiser"))
            subatomiser = args.get("name");
        else if(name.equals("interpreter"))
            interpreter = args.get("name");
        else if(name.equals("directory")) {
            dir = new File(args.get("name"));
            if(!dir.exists()) {
                dir = null;
                throw new ProverException("directory does not exist: "+dir);
            }
        } else if(name.equals("result-sort"))
            result_sort = quotemeta(args.get("name"));
        else if(name.equals("ignore"))
            ignore_patterns.add(args.get("pattern"));
        else if(name.equals("maudename")) {
            String id = args.get("id");
            String n = args.get("name");
            SyntaxElement syn = system.getSyntaxElement(id);
            setMaudeName(syn, quotemeta(n));
        } else
            throw new ProverException("unknown parameter "+name);
    }

    public void start() throws ProverException {
        if(inferencer == null || sa_inferencer == null | sa_reverseinferencer == null || reverseinferencer == null || normalizer == null || result_sort == null || subatomiser == null || interpreter == null)
            throw new ProverException("inferencer, sa_inference, sa_reverseinferencer, reverseinferencer, normalizer, subatomiser, interpreter or result sort not specified");
        try {
            String[] tmp = new String[files.size()];
            for(int i = 0; i < tmp.length; ++i)
                tmp[i] = files.elementAt(i);
            maude = new Maude(dir, ignore_patterns, tmp);
        } catch(IOException | MaudeException ioe) {
            throw new ProverException("couldn't start Maude", ioe);
        }
    }

    public Node normalize(Node proof) throws ProverException {
        if(proof.getSyntax() instanceof Operator) {
            Node tmp = reduce(normalizer, proof);
            return reduce(inferencer, tmp);
        } else {
            for(int i = 0; i < proof.getNumChildren(); ++i)
                proof.setChild(i, normalize(proof.getChild(i)));
            return proof;
        }
    }

    public Node subatomise_reduce(Node proof) throws ProverException, IOException, MaudeException {
        interpret = false;
        String term = "gnf(" + maudify(proof) + ")";
        String subatomised_deriv = maude.reduce(subatomiser, term);
        if (subatomised_deriv.equals("error"))
            return null;
        return parse(subatomised_deriv, subatomiser);
    }
    
    public Node subatomise_convert(Node proof) throws ProverException, IOException, MaudeException {
        return proof.subatomise(this);
    }
    
    public Node interpret_reduce(Node proof) throws ProverException, IOException, MaudeException {
        String term = "interpret-reduce( "+maudify(proof)+")";
        interpret = true;
        String interpretised_deriv = maude.reduce(interpreter, term);
        if (interpretised_deriv.equals("error"))
            return null;
        return parse(interpretised_deriv, interpreter);
    }
    
    public Node interpret_convert(Node proof) throws ProverException, IOException, MaudeException {
        return proof.interpret(this);
    }

    private String maude_subatomise(Node tree) {
        SyntaxElement elt = tree.getSyntax();
        String n = null;
        StringBuffer res;
    
        if (elt instanceof Constant || elt instanceof UnaryPrefixOperator) {
            res = new StringBuffer();
            
            if(tree.getParent() == null || tree.getParent().getSyntax() instanceof InferenceRule) {
                if (tree.getChildren().isEmpty()) {
                    res.append("E(");
                    res.append(getMaudeName((Constant)elt));
                    res.append(")");
                }
                else if (tree.getChildren().size() == 1) {
                    res.append("E(");
                    res.append(getMaudeName((UnaryPrefixOperator)elt));
                    res.append("(");
                    res.append(maude_subatomise(tree.getChild(0)));
                    res.append(")");
                    res.append(")");
                }
                else {
                    System.out.print("should never happen - unless unary operator isn't only negation - redo this for that case");
                }
            } else if (elt instanceof UnaryPrefixOperator) {
                res.append(getMaudeName((UnaryPrefixOperator)elt));
                res.append("(");
                res.append(maude_subatomise(tree.getChild(0)));
                res.append(")");
            }
            else{
                res.append(getMaudeName((Constant) elt));
            }

            return res.toString();
        }
        else if (elt instanceof BinaryOperator) {
            res = new StringBuffer();
            res.append(getMaudeName((Operator)elt));
            res.append("(");
            int l = tree.getChildren().size();
            for(int i=0; i<l-1; i++) {
                res.append(maude_subatomise(tree.getChild(i)));
                res.append(",");
            }
            res.append(maude_subatomise(tree.getChildren().lastElement()));
            res.append(")");
            return res.toString();
        }
        return n;
    }
    
    private String maude_interpretise(Node tree) {
        SyntaxElement elt = tree.getSyntax();
        String n = null;
        StringBuffer res;
        if (elt instanceof Constant) {
            res = new StringBuffer();
            if(tree.getChildren().size() == 2) {
                res.append("(");
                res.append(maude_interpretise(tree.getChild(0)));
                res.append(" ");
                res.append(getMaudeName((Constant)elt));
                res.append(" ");
                res.append(maude_interpretise(tree.getChild(1)));
                res.append(")");
            }
            else {
                res.append(getMaudeName((Constant)elt));
            }
            return res.toString();
        }
        else if (elt instanceof BinaryOperator) {
            res = new StringBuffer();
            res.append(getMaudeName((Operator)elt));
            res.append("(");
            int l = tree.getChildren().size();
            for(int i=0; i<l-1; i++) {
                res.append(maude_interpretise(tree.getChild(i)));
                res.append(",");
            }
            res.append(maude_interpretise(tree.getChildren().lastElement()));
            res.append(")");
            return res.toString();
        }
        return n;
    }
    
    private Node reduce(String modname, Node tree) 
            throws ProverException {
        try {
            String term = maudify(tree);
            
            //String term = maude_subatomise(proof);
            return parse(maude.reduce(modname, term), modname);
        } catch(IOException ioe) {
            throw new ProverException("an I/O exception occured", ioe);
        } catch(MaudeException me) {
            throw new ProverException("a Maude exception occured", me);
        }
    }

    public Pair<Vector<Rewrite>, Vector<Rewrite>> findSubatomicRewrites(Node n, Collection<Rule> rules, boolean isLeft, boolean isRight, boolean isSingle) throws ProverException {
        return findRewrites(n, rules, isLeft, isRight, isSingle, sa_inferencer, sa_reverseinferencer);
    }

    public Pair<Vector<Rewrite>, Vector<Rewrite>> findNormalRewrites(Node n, Collection<Rule> rules, boolean isLeft, boolean isRight, boolean isSingle) throws ProverException {
        return findRewrites(n, rules, isLeft, isRight, isSingle, inferencer, reverseinferencer);
    }

    private Pair<Vector<Rewrite>, Vector<Rewrite>> findRewrites(Node n, Collection<Rule> rules, boolean isLeft, boolean isRight, boolean isSingle, String inf, String rinf) throws ProverException {
        Pair<Vector<Rewrite>, Vector<Rewrite>> ret = new Pair<>();

        if(isSingle){
            if (n.isRight())
                ret.fst = findRewritesOfInf(n, rules, inf, false);
            if (false) //(n.isLeft())
                ret.snd = findRewritesOfInf(n, rules, rinf, true);
        }
        else {
            if (isRight)
                ret.fst = findRewritesOfInf(n, rules, inf, false);
            if (false) //(isLeft)
                ret.snd = findRewritesOfInf(n, rules, rinf, true);
        }
        return ret;
    }

    private Vector<Rewrite> findRewritesOfInf(Node n, Collection<Rule> rules, String infMod, boolean reverse) throws ProverException {
        StringBuffer infs = new StringBuffer();
        Vector<Rewrite> res = new Vector<Rewrite>();
            for (Rule r : rules)
                if (r instanceof Rule && (reverse || r.isUpDirection()))
                    infs.append("'").append(getMaudeName(r)).append(" ");
        if(infs.length() != 0){
            Vector<Node> formulae = new Vector<Node>();
            getFormulae(n, formulae);
            for(Node f : formulae)
                findRewritesForInferenceRules(f, infs, res, infMod);
        }
        return new Vector<Rewrite>(res);
    }

    private void getFormulae(Node n, Vector<Node> formulae) {
        if(n.getSyntax() instanceof Operator) {
            formulae.add(n);
        } else {
            for(int i = 1; i < n.getNumChildren(); ++i)
                getFormulae(n.getChild(i), formulae);
        }
    }

    private void findRewritesForInferenceRules(Node f, StringBuffer infs, Vector<Rewrite> res, String inferencer) throws ProverException {
        try {
            String trm = upTerm(f, inferencer);
            String rewlist = maude.reduce("Util", "findRewrites(" + trm + ", '" + inferencer + ", " + infs + ")");
            parseRewriteList(f, rewlist, res, inferencer);
        }
        catch(IOException ioe) {
            throw new ProverException("an I/O exception occurred", ioe);
        } catch(MaudeException me) {
            throw new ProverException("a Maude exception occurred", me);
        }
    }
    
    private void parseRewriteList(Node input, String str, Vector<Rewrite> res, String inferencer) throws ProverException {
        parseRewriteList(input, new PushbackReader(new StringReader(str), PUSHBACK_CAPACITY), res, inferencer);
    }

    private void parseRewriteList(Node input, PushbackReader r, Vector<Rewrite> res, String inferencer) throws ProverException {
        try {
            String curtk = lex(r);
            int c;
            if(curtk.equals("(")) {
                if(!lex(r).equals("empty"))
                    throw new ProverException("`empty' expected");
                return;
            } if(unquotemeta(curtk).equals("_,_")) {
                if(!lex(r).equals("("))
                    throw new ProverException("`(' expected");
                while(true) {
                    parseRewrite(input, r, res, inferencer);
                    c = r.read();
                    if(c == ')')
                        break;
                    else if(c != ',')
                        throw new ProverException("`,' or `)' expected");
                }
            } else {
                r.unread(curtk.toCharArray());
                parseRewrite(input, r, res, inferencer);
            }
        } catch(IOException ioe) {
            throw new ProverException("an I/O exception occurred", ioe);
        }
    }

    private void parseRewrite(Node input, PushbackReader r, Vector<Rewrite> res, String inferencer) throws ProverException {
        String curtk, rlname;
        Vector<Node> result = new Vector<Node>();
        if(!lex(r).equals("rewriting"))
            throw new ProverException("`rewriting' expected");
        if(!lex(r).equals("("))
            throw new ProverException("`(' expected");
        if(!(curtk = lex(r)).startsWith("'"))
            throw new ProverException("Qid expected");
        rlname = curtk.substring(1);
        if(!lex(r).equals(","))
            throw new ProverException("`,' expected");
        // skip the context
        parseTerm(r);
        if(!lex(r).equals(","))
            throw new ProverException("`,' expected");
        String term = parseTerm(r);
        if(!lex(r).equals(")"))
            throw new ProverException("`)' expected");
        Vector<String> tmp = parseTermOrTermList(term, inferencer);
        for(String s : tmp)
            result.add(downTerm(s, inferencer));
        Rule rl = (Rule)this.getSyntaxElement(rlname);
        /*for(int i = 0; i < result.size(); ++i) {
            System.out.println("normalizing "+result.get(i).prettyprint()+
                    ", result is "+normalize(result.get(i)).prettyprint());
            result.set(i, normalize(result.get(i)));
        }*/
        Rewrite rew = new Rewrite(input, rl, result);
        //System.out.println("parsed rewrite `"+rew+"'");
        res.add(rew);
    }

    private String upTerm(Node n, String inferencer) throws ProverException {
        return upTerm(maudify(n), inferencer);
    }
    
    private String upTerm(String t, String inferencer) throws ProverException {
        try {
            return maude.reduce(inferencer, "upTerm("+t+")");
        } catch(IOException ioe) {
            throw new ProverException("an I/O exception occured", ioe);
        } catch(MaudeException me) {
            throw new ProverException("a Maude exception occured", me);
        }
    }
    
    private Node downTerm(String term, String inferencer) throws ProverException {
        try {
            String res;
            res = maude.reduce(inferencer,"downTerm("+term+", Err:"+result_sort+")");
            if(res.equals("Err:"+result_sort))
                throw new ProverException("unable to `down' term "+term);
            return parse(res, inferencer);
        } catch(IOException ioe) {
            throw new ProverException("an I/O exception occured", ioe);
        } catch(MaudeException me) {
            throw new ProverException("a Maude exception occured", me);
        }
    }
    
    public void abort() throws ProverException {
        try {
            maude.stop();
        } catch(IOException | MaudeException ioe) {
            throw new ProverException("unable to abort", ioe);
        }
    }

}
