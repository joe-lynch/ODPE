/*  
 * 
 * This file is part of the Open Deduction Proof Editor.
 * 
 * @author Originally created for GraPE by Max Schaefer
 *
 */
package odpe.frontend.model;

import odpe.backend.Prover;
import odpe.backend.ProverException;
import odpe.frontend.syntax.*;
import odpe.frontend.view.InferenceFork;
import odpe.frontend.view.SimpleViewableObject;
import odpe.frontend.view.ViewableObject;
import odpe.util.Triple;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class InferenceSystem {
    
    private String name;
    private String subatomic_name;
    private Lexer lexer;
    private Parser parser;
    private Map<String, SyntaxElement> syntax_elements;
    private Prover backend;
    private String embedding;
    
    public InferenceSystem(String name, String subatomic_name, Lexer l, Parser p) {
        this.name = name;
        this.subatomic_name = subatomic_name;
        this.lexer = l;
        this.parser = p;
        this.syntax_elements = new HashMap<String, SyntaxElement>();
    }
    
    public String getName() {
        return name;
    }

    private void addSyntaxElement(SyntaxElement elt) {
        syntax_elements.put(elt.getID(), elt);
    }
    
    public SyntaxElement getSyntaxElement(String id) {
        return syntax_elements.get(id);
    }
    
    public Collection<SyntaxElement> getSyntaxElements() {
        return syntax_elements.values();
    }
    
    public Collection<Rule> getRules() {
        Collection<Rule> res = new Vector<Rule>();
        Collection<SyntaxElement> tmp = getSyntaxElements();
        for(SyntaxElement syn : tmp)
            if(syn instanceof Rule)
                res.add((Rule)syn);
        return res;
    }
    /*
    public Collection<Rule> getSubatomicRules() {
        Collection<Rule> res = new Vector<Rule>();
        Collection<SyntaxElement> tmp = getSyntaxElements();
        for(SyntaxElement syn : tmp)
            if(syn instanceof SubatomicRule)
                res.add((SubatomicRule)syn);
        return res;
    }
    */
    public Lexer getLexer() {
        return lexer;
    }
    
    public Parser getParser() {
        return parser;
    }
    
    public void setBackend(Prover p) {
        this.backend = p;
    }
    
    public Prover getBackend() {
        return backend;
    }
    
    private void setEmbedding(String ctxt) {
        this.embedding = ctxt;
    }
    
    public String getEmbedding() {
        return embedding;
    }

    public static InferenceSystem fromFile(File f) throws DescriptionFileException {
        Document d = parse_description_file(f);
        Element root = d.getDocumentElement();
        String normal_name = root.getAttribute("name");
        String subatomic_name = root.getAttribute("subatomic-name");
        Node normal = root.getElementsByTagName("normal").item(0);
        Node subatomic = root.getElementsByTagName("subatomic").item(0);

        Lexer l = new Lexer();
        Parser p = new Parser();
        InferenceSystem inf = new InferenceSystem(normal_name, subatomic_name, l, p);

        int prec = syntax((Element) normal, l, p, inf, 0);
        syntax((Element) subatomic, l, p, inf, prec);
        rules((Element) normal, inf);
        rules((Element) subatomic, inf);
        backend(root, inf);
        return inf;
    }

    private static Document parse_description_file(File f) throws DescriptionFileException {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setValidating(true);
            dbf.setIgnoringComments(true);
            dbf.setIgnoringElementContentWhitespace(true);
            DocumentBuilder db;
            db = dbf.newDocumentBuilder();
            db.setErrorHandler(new ErrorHandler() {
                public void warning(SAXParseException e) throws SAXException {
                    // warnings are ignored
                }
                public void error(SAXParseException e) throws SAXException {
                    throw new SAXException(e);
                }
                public void fatalError(SAXParseException e) throws SAXException {
                    throw new SAXException(e);
                }
            });
            Document d = db.parse(f);
            return d;
        } catch(ParserConfigurationException e) {
            throw new DescriptionFileException("parser configuration exception", e);
        } catch(IOException e) {
            throw new DescriptionFileException("I/O exception", e);
        } catch(SAXException e) {
            throw new DescriptionFileException("SAX exception", e);
        }
    }
    
    private static Boolean attrDefined(NamedNodeMap m, String name) {
        return m.getNamedItem(name) != null;
    }
    
    private static String getAttr(NamedNodeMap m, String name) throws DescriptionFileException {
        Node tmp = m.getNamedItem(name);
        if(tmp == null)
            throw new DescriptionFileException("attribute `"+name+"' not defined");
        return tmp.getNodeValue();
    }
    
    private static String getAttr(NamedNodeMap m, String name, String deflt) {
        Node tmp = m.getNamedItem(name);
        if(tmp == null)
            return deflt;
        return tmp.getNodeValue();
    }
    
    private static boolean boolFromFlag(String flag) {
        return !flag.equalsIgnoreCase("no");
    }
    
    private static OperandPosition buildOperandPosition(InferenceSystem inf, int prec, String spec) 
            throws DescriptionFileException {
        if(spec.equals("singleton"))
            return new SingletonOperandPosition(inf, prec);
        else if(spec.startsWith("list")) {
            String sep = spec.substring("list".length());
            return new ListOperandPosition(inf, prec, false, new SimpleViewableObject(sep));
        } else if(spec.startsWith("multiset")) {
            String sep = spec.substring("multiset".length());
            return new ListOperandPosition(inf, prec, true, new SimpleViewableObject(sep));
        } else
            throw new DescriptionFileException("invalid operand position specification: "+spec);
    }
    
    private static Triple<String, String, String> threeOrOne(NamedNodeMap m, String one, String fst, String snd, String thrd) throws DescriptionFileException {
        String startsym, sepsym, endsym;
        if(attrDefined(m, one)) {
            String sym = getAttr(m, one);
            if(attrDefined(m, fst) || attrDefined(m, snd) || attrDefined(m, thrd))
                throw new DescriptionFileException("cannot "+"give both `"+one+"' and `"+fst+"'/"+
                                                    "`"+snd+"'/`"+thrd+"' attributes");
            if(sym.length() != 3)
                throw new DescriptionFileException("`"+fst+"'"+ " attribute must have length 3");
            startsym = sym.substring(0, 1);
            sepsym = sym.substring(1, 2);
            endsym = sym.substring(2, 3);
        } else {
            startsym = getAttr(m, fst);
            sepsym = getAttr(m, snd);
            endsym = getAttr(m, thrd);
        }
        return new Triple<String, String, String>(startsym,
                sepsym, endsym);
    }
    
    private static Triple<String, String, String> threeOrOne(NamedNodeMap m, String one,String fst, String snd, String thrd,
                       String startdeflt, String sepdeflt, String enddeflt) 
            throws DescriptionFileException {
        String startsym, sepsym, endsym;
        if(attrDefined(m, one)) {
            String sym = getAttr(m, one);
            if(attrDefined(m, fst) || attrDefined(m, snd) || attrDefined(m, thrd))
                throw new DescriptionFileException("cannot "+ "give both `"+one+"' and `"+fst+"'/"+
                                                    "`"+snd+"'/`"+thrd+"' attributes");
            if(sym.length() != 3)
                throw new DescriptionFileException("`"+fst+"'"+" attribute must have length 3");
            startsym = sym.substring(0, 1);
            sepsym = sym.substring(1, 2);
            endsym = sym.substring(2, 3);
        } else {
            startsym = getAttr(m, fst, startdeflt);
            sepsym = getAttr(m, snd, sepdeflt);
            endsym = getAttr(m, thrd, enddeflt);
        }
        return new Triple<String, String, String>(startsym, sepsym, endsym);
    }
    
    private static int syntax(Element root, Lexer l, Parser p, InferenceSystem inf, int prec) throws DescriptionFileException {
        Node s = root.getElementsByTagName("syntax").item(0);
        NodeList rators = s.getChildNodes();
        try {
            // now we traverse the list of operators backwards, starting
            // from lowest priority
            for(int i = rators.getLength()-1; i>=0; --i, ++prec) {
                Node n = rators.item(i);
                NamedNodeMap attr = n.getAttributes();
                String tag = n.getNodeName();

                switch (tag) {
                    case "binary-outfix": {
                        String symstart, symsep, symend;
                        String instart, insep, inend;
                        String texstart, texsep, texend;
                        Triple<String, String, String> t;
                        t = threeOrOne(attr, "symbol", "startsym", "sepsym", "endsym");
                        symstart = t.fst;
                        symsep = t.snd;
                        symend = t.thrd;
                        String id = getAttr(attr, "id", symstart + "_" + symsep + "_" + symend);
                        t = threeOrOne(attr, "input", "instart", "insep", "inend", symstart, symsep, symend);
                        instart = t.fst;
                        insep = t.snd;
                        inend = t.thrd;
                        t = threeOrOne(attr, "tex", "texstart", "texsep", "texend", symstart, symsep, symend);
                        texstart = t.fst;
                        texsep = t.snd;
                        texend = t.thrd;
                        int myprec = Integer.parseInt(getAttr(attr, "prec", prec + ""));

                        boolean assoc = boolFromFlag(getAttr(attr, "assoc"));
                        boolean comm = boolFromFlag(getAttr(attr, "comm"));

                        int leftprec = Integer.parseInt(getAttr(attr, "leftprec", (-1) + ""));
                        String leftop = getAttr(attr, "leftop");
                        int rightprec = Integer.parseInt(getAttr(attr, "rightprec", (-1) + ""));
                        String rightop = getAttr(attr, "rightop");

                        OperandPosition left = buildOperandPosition(inf, leftprec, leftop);
                        OperandPosition right = buildOperandPosition(inf, rightprec, rightop);

                        ViewableObject start = new SimpleViewableObject(instart, symstart, texstart);
                        ViewableObject sep = new SimpleViewableObject(insep, symsep, texsep);
                        ViewableObject end = new SimpleViewableObject(inend, symend, texend);

                        Operator op = new BinaryOutfixOperator(id, inf, myprec, left, right, assoc, comm, start, sep, end);

                        l.addToken(instart);
                        l.addToken(insep);
                        l.addToken(inend);
                        p.addOperator(op);
                        inf.addSyntaxElement(op);

                        if (left instanceof ListOperandPosition) {
                            ListOperandPosition lop = (ListOperandPosition) left;
                            l.addToken(lop.getSeparator().getTextInputView());
                            inf.addSyntaxElement(lop);
                        }
                        if (right instanceof ListOperandPosition) {
                            ListOperandPosition lop = (ListOperandPosition) right;
                            l.addToken(lop.getSeparator().getTextInputView());
                            inf.addSyntaxElement(lop);
                        }
                        prec = myprec;

                        break;
                    }
                    case "unary-prefix": {
                        String sym = getAttr(attr, "symbol");
                        String id = getAttr(attr, "id", sym + "_");
                        String in = getAttr(attr, "input", sym);
                        String tex = getAttr(attr, "tex", sym);

                        int myprec = Integer.parseInt(getAttr(attr, "prec", prec + ""));
                        int opprec = Integer.parseInt(getAttr(attr, "opprec", myprec + ""));

                        String opspec = getAttr(attr, "op");
                        OperandPosition pos = buildOperandPosition(inf, opprec, opspec);
                        ViewableObject opsym = new SimpleViewableObject(in, sym, tex);
                        Operator op = new UnaryPrefixOperator(id, inf, myprec, pos, opsym);

                        l.addToken(in);
                        p.addOperator(op);
                        inf.addSyntaxElement(op);

                        if (pos instanceof ListOperandPosition) {
                            ListOperandPosition lop = (ListOperandPosition) pos;
                            l.addToken(lop.getSeparator().getTextInputView());
                            inf.addSyntaxElement(lop);
                        }
                        prec = myprec;

                        break;
                    }
                    case "constant": {
                        String sym = getAttr(attr, "symbol");
                        String id = getAttr(attr, "id", sym);
                        String in = getAttr(attr, "input", sym);
                        String tex = getAttr(attr, "tex", sym);
                        int myprec = Integer.parseInt(getAttr(attr, "prec", prec + ""));
                        ViewableObject namesym = new SimpleViewableObject(in, sym, tex);
                        Constant op = new Constant(id, inf, myprec, namesym);
                        l.addToken(in);
                        p.addOperator(op);
                        inf.addSyntaxElement(op);
                        prec = myprec;
                        break;
                    }
                    case "constants": {
                        String tmp = getAttr(attr, "symbols");
                        String[] syms = tmp.split("\\s+");
                        String[] ids = getAttr(attr, "ids", tmp).split("\\s+");
                        String[] ins = getAttr(attr, "inputs", tmp).split("\\s+");
                        String[] texs = getAttr(attr, "texs", tmp).split("\\s+");
                        int myprec = Integer.parseInt(getAttr(attr, "prec", prec + ""));
                        int k = syms.length;
                        if (ids.length == k && ins.length == k && texs.length == k) {
                            for (int j = 0; j < k; ++j, ++myprec) {
                                ViewableObject namesym = new SimpleViewableObject(ins[j], syms[j], texs[j]);
                                Constant op = new Constant(ids[j], inf, myprec, namesym);
                                l.addToken(ins[j]);
                                p.addOperator(op);
                                inf.addSyntaxElement(op);
                            }
                            prec = myprec - 1;
                        }
                        break;
                    }
                    case "name": {
                        int myprec = Integer.parseInt(getAttr(attr, "prec", prec + ""));
                        Operator op = new Name("", inf, myprec);
                        p.addOperator(op);
                        inf.addSyntaxElement(op);
                        prec = myprec;
                        break;
                    }
                    case "embed":
                        String ctxt = getAttr(attr, "context");
                        inf.setEmbedding(ctxt);
                        break;
                    default:
                        throw new DescriptionFileException("unknown operator type: " + tag + "(value: " + n.getNodeValue() + ")");
                }
            }
        } catch(InvalidPrecedenceException ipe) {
            throw new DescriptionFileException("invalid precedence given/inferred", ipe);
        }
        return prec;
    }
    
    private static void rules(Element root, InferenceSystem inf) throws DescriptionFileException {
        Node s = root.getElementsByTagName("rules").item(0);
        NodeList rules = s.getChildNodes();
        for(int i = 0; i < rules.getLength(); ++i) {
            Node n = rules.item(i);
            NamedNodeMap attr = n.getAttributes();
            String tag = n.getNodeName();
            String name = getAttr(attr, "name");
            String id = getAttr(attr, "id", name);
            String tex = getAttr(attr, "tex", name);
            boolean enabled = boolFromFlag(getAttr(attr, "enabled"));
            switch (tag) {
                case "inference-rule":
                    if(root.getTagName().equals("subatomic"))
                        inf.addSyntaxElement(new SubatomicRule(id, name, inf, tex, enabled, true));
                    else
                        inf.addSyntaxElement(new InferenceRule(id, name, inf, tex, enabled, true));
                    break;
                case "reverse-inference-rule":
                    if(root.getTagName().equals("subatomic"))
                        inf.addSyntaxElement(new SubatomicRule(id, name, inf, tex, enabled, false));
                    else
                        inf.addSyntaxElement(new InferenceRule(id, name, inf, tex, enabled, false));
                    break;
                default:
                    throw new DescriptionFileException("unknown rule type " + tag);
            }
        }
    }
    
    private static void backend(Element root, InferenceSystem inf) 
            throws DescriptionFileException {
        try {
            Node s = root.getElementsByTagName("backend").item(0);
            NamedNodeMap m = s.getAttributes();
            String classname = getAttr(m, "class");
            ClassLoader cl = ClassLoader.getSystemClassLoader();
            Class backendClass = cl.loadClass(classname);
            Class[] parmtypes = { InferenceSystem.class };
            Constructor ctor = backendClass.getConstructor(parmtypes);
            Object[] args = { inf };
            Prover backend = (Prover)ctor.newInstance(args);
            NodeList elements = s.getChildNodes();
            for(int i = 0; i < elements.getLength(); ++i) {
                Node n = elements.item(i);
                if(n.getNodeType() != Node.ELEMENT_NODE)
                    continue;
                NamedNodeMap attr = n.getAttributes();
                String tag = n.getNodeName();
                int l = attr.getLength();
                Map<String, String> attrs = new HashMap<String, String>();
                for(int j = 0; j < l; ++j)
                    attrs.put(attr.item(j).getNodeName(), attr.item(j).getNodeValue());
                backend.setParam(tag, attrs);
            }
            inf.setBackend(backend);
        } catch(ClassNotFoundException cnfe) {
            throw new DescriptionFileException("class not found", cnfe);
        } catch(NoSuchMethodException nsme) {
            throw new DescriptionFileException("prover does not have appropriate constructor", nsme);
        } catch(IllegalAccessException iae) {
            throw new DescriptionFileException("constructor not accessible", iae);
        } catch(InvocationTargetException ite) {
            throw new DescriptionFileException("an invocation target exception occurred", ite);
        } catch(InstantiationException ie) {
            throw new DescriptionFileException("an instantiation exception occured", ie);
        } catch(ProverException pe) {
            throw new DescriptionFileException("a prover exception occured", pe);
        }
    }
    
    public void start() throws ProverException {
        backend.start();
    }


}
