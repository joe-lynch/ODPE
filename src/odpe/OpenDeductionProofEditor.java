/*  
 * 
 * This file is part of the Open Deduction Proof Editor.
 * 
 * @author Adaption by Joe Lynch of the original file create for GraPE by Max Schaefer
 *
 */
package odpe;

import odpe.backend.ProverException;
import odpe.backend.Rewrite;
import odpe.backend.odpe2maude.MaudeException;
import odpe.frontend.ast.ActiveCharacter;
import odpe.frontend.ast.Node;
import odpe.frontend.ast.Selection;
import odpe.frontend.model.InferenceSystem;
import odpe.frontend.syntax.*;
import odpe.frontend.view.WidgetView;
import odpe.frontend.view.dialogs.ExceptionDialog;
import odpe.frontend.view.dialogs.FormulaEditor;
import odpe.frontend.view.dialogs.RuleChooser;
import odpe.frontend.view.dialogs.SystemChooser;
import odpe.util.Pair;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


public class OpenDeductionProofEditor implements ActionListener, PropertyChangeListener, MouseListener, KeyListener{

	private Map <Object, Selection> curr_pair;
	private Node curr_connective;
	private boolean multiple_derivations;
	private boolean valid_selection;
	private boolean subatomised;
	private Boolean reverse;
	private InferenceSystem system;
	private InferenceSystem sa_system;
	private Node derivation;
	private Selection current_selection;
	private Stack<Node> undo_stack;
	private Stack<Node> redo_stack;
	private Node redexNode;
	private File description_file;

	private void init_model() {
		system = null;
		sa_system = null;
		derivation = null;
		current_selection = null;
		multiple_derivations = false;
		valid_selection = true;
		subatomised = false;
		reverse = false;
		curr_connective=null;
		description_file = null;
		redexNode = null;
		curr_pair = new HashMap<Object, Selection>();
		undo_stack = new Stack<Node>();
		redo_stack = new Stack<Node>();
	}

	private void initialize_system(String sysfn) {
		File f = new File(sysfn);
		try {
			system = InferenceSystem.fromFile(f);

			/* the description file's parent directory will
			 * be the default directory for the backend */
			Map<String, String> tmp = new HashMap<String, String>();
			tmp.put("name", f.getParent());
			system.getBackend().setParam("directory", tmp);
			/* initialize the rule chooser */
			description_file = f;
			ruleChooser = new RuleChooser(frame, system.getName(), system.getRules());
		} catch(Exception e) {
			ExceptionDialog.showExceptionDialog(frame, e);
		}
	}

	
	/**
	 * This method finds the possible rewrites of a formula
	 * It converts a single formula to multiple formula
	 * by using the redexNode that is calculated in the 
	 * lowest common ancestor method
	 * @author Joe Lynch
	 *  
	 */
	private Pair<Vector<Rewrite>, Vector<Rewrite>> read_choices(Collection<Rule> rules) throws ProverException  {
		Pair<Vector<Rewrite>, Vector<Rewrite>> choicePair = new Pair<>();
		Vector<Node> curr_sel_nodes = new Vector<>();
		Node n;

		if (multiple_derivations) {
			if(curr_connective.getSyntax() instanceof UnaryOperator) {
				Node tmp = current_selection.asNode().clone();
				tmp.getUnary();
				curr_sel_nodes.add(tmp.getChild(0));
				n = new Node(system, curr_connective.getSyntax(), curr_sel_nodes);
			}
			else
				n = redexNode;
		}
		else {
			n = current_selection.asNode();
		}

		if(curr_pair.isEmpty()) {
			valid_selection = false;
			choicePair=null;
		}
		else {
				boolean isSingle = true;
				boolean allRight = true;
				boolean allLeft = true;

				if(curr_pair.size() > 1) {
					isSingle = false;
					for (Selection s : curr_pair.values()) {
						if (!s.getSurroundingNode().isRight()) {
							allRight = false;
							break;
						}
					}
					for (Selection s : curr_pair.values()) {
						if (!s.getSurroundingNode().isLeft()) {
							allLeft= false;
							break;
						}
					}
				}

				//System.out.println("allRight: "+allRight);
				//System.out.println("allLeft: "+allLeft);

				if(subatomised)
					choicePair = system.getBackend().findSubatomicRewrites(n, rules, allLeft, allRight, isSingle);
				else
					choicePair = system.getBackend().findNormalRewrites(n, rules, allLeft, allRight, isSingle);

		}
		status(PROVING);

        if (!valid_selection || ( choicePair.fst == null || choicePair.fst.isEmpty()) && (choicePair.snd == null || choicePair.snd.isEmpty())) {
			JOptionPane.showMessageDialog(frame, "No proof step possible.", "Oops...", JOptionPane.ERROR_MESSAGE);
			return null;
		}
		else
			return choicePair;
	}

	/**
	 * This method modifies the derivation tree, in order to represent the application
	 * of inference rules, and implement the rewrites into the tree. The tree is then
	 * graphically displayed
	 * @author Joe Lynch
	 *  
	 */
	private synchronized void modifyTree(Rewrite r, Selection sel) throws ImplementationException, ProverException  {
		Node n, p = null;
		if(multiple_derivations)
			n = sel.asNode();
		else {
			n = r.getRedex();
			if(!(sel.getSurroundingNode().getSyntax() instanceof Operator))
				sel = new Selection(n);
			while((p = n.getParent()) != null && p.getSyntax() instanceof Operator)
				n = p;
		}
		Vector<Node> redexRuleResult = new Vector<Node>();

        /* shallow modification / single forumla */
		//check if it's on left or right side of parent...
		if(sel.isCompleteNode() && n == sel.getSurroundingNode() && !multiple_derivations) {
			
			Node parent = n.getParent();
			//create redex-rule-result tree
			redexRuleResult.add(n);
			redexRuleResult.add(r.getResults().firstElement());

			if(!r.getRule().isUpDirection())
				Collections.reverse(redexRuleResult);
			Node replacement = new Node(system, r.getRule(), redexRuleResult); 
		
			if(p == null)
				derivation = replacement;
			else
				parent.replaceChild(n, replacement);
			//undo_stack.push(replacement);
		}
		else if (multiple_derivations){
			
			//if multiple derivation selections
			Node conclusion_old_derivation = current_selection.asNode().clone();
			
			//get unselected branches
			Vector<Node> unselectedVector = current_selection.asNode().getUnselected();
			unselectedVector.forEach(item -> {
				if (item.getSyntax() instanceof InferenceRule || item.getSyntax() instanceof SubatomicRule)
					unselectedVector.set(unselectedVector.indexOf(item), item.getChildren().lastElement());
				else if (item.getSyntax() instanceof BinaryOperator && item.getChildren().size()==1)
					unselectedVector.set(unselectedVector.indexOf(item), item.getChildren().firstElement());
			});
		
			//add conclusion to derivation
			Vector<Node> derivation_children = new Vector<Node>();
			
			//if no unselected derivations
			Node replacement;
            /* shallow modification / multiple forumlae **/
			if(unselectedVector.isEmpty()) {
				
		
				/* create new derivation with old derivation as conclusion and result as premise */
				derivation_children.add(conclusion_old_derivation);
				derivation_children.add(r.getResults().firstElement());

				if(!r.getRule().isUpDirection())
					Collections.reverse(derivation_children);
				replacement = new Node(system, r.getRule(), derivation_children);
			}
            /* deep modification / multiple forumlae **/
			else {
				/* create new derivation with equality rule, with old derivation as conclusion and
				 * redex-rule-result derivation and unselected parts as premise */
				
				//create redex-rule-result tree
		
				redexRuleResult.add(r.getRedex().clone().removeRedundantOperators());
				redexRuleResult.add(r.getResults().firstElement());
				
				if(!r.getRule().isUpDirection())
					Collections.reverse(redexRuleResult);
	
				//add redex-rule-result & unselected parts to premise
				Vector<Node> premise = new Vector<Node>();
				premise.add(new Node(system, r.getRule(), redexRuleResult));
				premise.addAll(unselectedVector);
				
				//add premise to derivation
				derivation_children.add(conclusion_old_derivation);	
				derivation_children.add(new Node(system, curr_connective.getSyntax(), premise));

				if(!r.getRule().isUpDirection())
					Collections.reverse(derivation_children);
				
				//construct equality derivation
				replacement = new Node(system, new InferenceRule ("equality", "=", system, "=", true, true), derivation_children);
			}
			//replace relevant part of tree with replacement
			n = sel.replace(n, replacement);
			while((p = n.getParent()) != null)
				n = p;
			derivation = n;
			//undo_stack.push(replacement);
		}
        /* deep modification / single forumla **/
		else {
			//create redex-rule-result tree
			redexRuleResult.add(r.getRedex().clone());
			redexRuleResult.add(r.getResults().firstElement());

			if(!r.getRule().isUpDirection())
				Collections.reverse(redexRuleResult);
			Node replacement = new Node(system, r.getRule(), redexRuleResult);
			
			//replace relevant part of tree with replacement
			n = sel.replace(n, replacement);
			while((p = n.getParent()) != null)
				n = p;
			derivation = n;
			//undo_stack.push(replacement);
		}
		curr_pair.clear();
		multiple_derivations = false;
		//derivation = system.getBackend().normalize(derivation);
		current_selection = new Selection(derivation);
		undo_stack.push(derivation.clone());
		if (redo_stack != null && !redo_stack.isEmpty())
			redo_stack.clear();
		canUndo(true);
		canRedo(false);
	}

	private void do_proofstep(Collection<Rule> rules) {
		try {
			status(READING_CHOICES);

			//rules are the current enabled rules, read_choices returns all possible choices
			Pair<Vector<Rewrite>, Vector<Rewrite>> choices = read_choices(rules);

			if(choices != null && (choices.fst != null || choices.snd !=null)) {
				//gets the choice that the user selects
				Rewrite r = get_choice(choices);
				if (r != null) {
					//update selection of tree with selected nodes if multiple formulae
					if(multiple_derivations) {
						curr_connective.updateSelection(curr_pair.values().stream().map(Selection::getSurroundingNode).collect(Collectors.toCollection(Vector<Node>::new)));
						current_selection = curr_connective.getSelection();
					}
					modifyTree(r, current_selection);
					display_proof();
				}
			}

			curr_pair.clear();
			current_selection = new Selection(derivation);
		} catch(Exception e) {
			ExceptionDialog.showExceptionDialog(frame, e);
		}
	}
	
	private void subatomise() throws ProverException, IOException, MaudeException {
		subatomised = true;
		ruleChooser.showSubatomicRules(true);
		Node d = system.getBackend().subatomise_convert(derivation);
		undo_stack.clear();
		redo_stack.clear();
		canUndo(false);
		canRedo(false);
		d.print();
		derivation = d;
		undo_stack.push(derivation.clone());
		display_proof();	
	}
	
	private void interpret() throws ProverException, IOException, MaudeException {
		subatomised = false;
		ruleChooser.showSubatomicRules(false);
		undo_stack.clear();
		redo_stack.clear();
		canUndo(false);
		canRedo(false);
		Node d = system.getBackend().interpret_convert(derivation);
		derivation = d;
		undo_stack.push(derivation.clone());
		display_proof();
	}

	private void proofSearch() throws ProverException, IOException, MaudeException {
		status(SEARCHING);
		Node d = system.getBackend().proofSearch(derivation);
		status(PROVING);
		d.print();
		derivation = d;
		display_proof();
	}

private synchronized void redo(){
		if (redo_stack.isEmpty()){
			return;
		}
		derivation = redo_stack.peek().clone();
		Node last_deriv = redo_stack.pop();
		undo_stack.push(last_deriv.clone());
		curr_pair.clear();
		current_selection = new Selection(derivation);
		status(PROVING);
}

private synchronized void undo(){
	if (undo_stack.size() <= 1){
		return;
	}

	Node last_deriv = undo_stack.pop();
	redo_stack.push(last_deriv.clone());
	derivation = undo_stack.peek().clone();
	curr_pair.clear();
	current_selection = new Selection(derivation);
	status(PROVING);
}
/*
private synchronized void undo() {
	if (undo_stack.empty())
			return;
	Node last_change = undo_stack.pop();
	SyntaxElement elt = last_change.getSyntax();
	if(elt instanceof Operator) {
		// this happens if no rule has been applied yet
		// in that case, we simply delete the current derivation
		// and wait for the user to start a new one
		status(NO_ACTIVE_PROOF);
	} else if(elt instanceof InferenceRule) {
		Node parent = last_change.getParent();
		if(parent == null)
			derivation = last_change.getChild(0);
		else {
			try {
				parent.replaceChild(last_change, last_change.getChild(0));
			}catch(ArrayIndexOutOfBoundsException ignored) {}
			Node p;
			Node n = parent;
			while((p = n.getParent()) != null)
				n = p;
			derivation = n;	
		}
		current_selection = new Selection(derivation);
		status(PROVING);
	} else {
		derivation = last_change.getChild(0);
		current_selection = new Selection(derivation);
		status(PROVING);
	}
	// make sure to properly orphan the derivation rule
	if(derivation != null)
		derivation.setParent(null);
	curr_pair.clear();
	}
*/
	/**
	 * Returns the currently enabled rules as a vector.
	 */
	private Vector<Rule> getEnabledRules() {
		Vector<Rule> res = new Vector<Rule>();
		if(ruleChooser == null)
			return res;
		for(Rule r : system.getRules())
			if(ruleChooser.ruleActive(r))
				res.add(r);
		return res;
	}

	private JFrame frame;
	private JMenuItem newItem;
	private JMenuItem stepItem1;
	private JMenuItem stepItem2;
	private JMenuItem undoItem1;
	private JMenuItem undoItem2;
	private JMenuItem redoItem1;
	private JMenuItem redoItem2;
	private JMenuItem exitItem;
	private JButton newButton;
	private JButton subatomiseButton;
	private JButton interpretButton;
	private JButton proofSearchButton;
	private JButton undoButton;
	private JButton redoButton;
	private JButton stopButton;
	private JPanel contentPanel;
	private JPanel proofPanel;
	private JLabel statusBar;
	private JPopupMenu popup;
	private RuleChooser ruleChooser;
	private RuleChooser sa_ruleChooser;
	private JScrollPane scrollPane;

	private void init_view() {
		frame = new JFrame("Open Deduction Proof Editor");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				if (JOptionPane.showConfirmDialog(frame,
						"Are you sure you want to close this window?", "Close Window?",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
					System.exit(0);
				}
			}
		});

		JMenuBar menuBar = new JMenuBar();
		JMenu proofMenu = new JMenu("Proof");
		proofMenu.setMnemonic(KeyEvent.VK_P);
		newItem = new JMenuItem("New Proof", KeyEvent.VK_N);
		newItem.addActionListener(this);
		proofMenu.add(newItem);
		proofMenu.addSeparator();
		stepItem1 = new JMenuItem("Do One Proof Step...", KeyEvent.VK_S);
		stepItem1.addActionListener(this);
		proofMenu.add(stepItem1);
		undoItem1 = new JMenuItem("Undo Last Step", KeyEvent.VK_U);
		undoItem1.setEnabled(false);
		undoItem1.addActionListener(this);
		proofMenu.add(undoItem1);
		redoItem1 = new JMenuItem("Redo Last Step", KeyEvent.VK_U);
		redoItem1.setEnabled(false);
		redoItem1.addActionListener(this);
		proofMenu.add(redoItem1);
		proofMenu.addSeparator();
		proofMenu.addSeparator();
		exitItem = new JMenuItem("Exit", KeyEvent.VK_X);
		exitItem.addActionListener(this);
		proofMenu.add(exitItem);
		menuBar.add(proofMenu);
		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic(KeyEvent.VK_H);
		helpMenu.addSeparator();
		frame.setJMenuBar(menuBar);

		contentPanel = new JPanel(new BorderLayout());
		contentPanel.setBackground(Color.white);
		contentPanel.setBorder(BorderFactory.createMatteBorder(30, 30, 50, 30, Color.white));
		contentPanel.addMouseListener(this);
		contentPanel.addKeyListener(this);
		proofPanel = new JPanel(new GridBagLayout());
		proofPanel.setBackground(Color.white);
		proofPanel.addMouseListener(this);
		proofPanel.addKeyListener(this);
		contentPanel.add(proofPanel, BorderLayout.CENTER);
		scrollPane = new JScrollPane(contentPanel);
		frame.getContentPane().setPreferredSize(new Dimension(600, 600));
		frame.pack();
		display_proof();

		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(scrollPane, BorderLayout.CENTER);

		statusBar = new JLabel("No current proof");
		statusBar.setBorder(BorderFactory.createLoweredBevelBorder());
		frame.getContentPane().add(statusBar, BorderLayout.SOUTH);

		JToolBar toolBar = new JToolBar("ODPE Toolbar");
		toolBar.setFloatable(false);
		toolBar.setRollover(true);
		newButton = new JButton(new ImageIcon(OpenDeductionProofEditor.class.getResource("icons/New16.gif")));
		newButton.addActionListener(this);
		toolBar.add(newButton);
		
		subatomiseButton = new JButton("Subatomise "); //new JButton(new ImageIcon(OpenDeductionProofEditor.class.getResource("icons/New16.gif")));
		subatomiseButton.addActionListener(this);
		toolBar.add(subatomiseButton);
		
		interpretButton = new JButton("Interpret "); //new JButton(new ImageIcon(OpenDeductionProofEditor.class.getResource("icons/New16.gif")));
		interpretButton.addActionListener(this);
		toolBar.add(interpretButton);
		//interpretButton.setVisible(false);

		proofSearchButton = new JButton(" Proof Search "); //new JButton(new ImageIcon(OpenDeductionProofEditor.class.getResource("icons/New16.gif")));
		proofSearchButton.addActionListener(this);
		toolBar.add(proofSearchButton);
		
		undoButton = new JButton(new ImageIcon(OpenDeductionProofEditor.class.getResource("icons/Undo16.gif")));
		undoButton.addActionListener(this);
		undoButton.setEnabled(false);
		toolBar.add(undoButton);

		redoButton = new JButton(new ImageIcon(OpenDeductionProofEditor.class.getResource("icons/Redo16.gif")));
		redoButton.addActionListener(this);
		redoButton.setEnabled(false);
		toolBar.add(redoButton);

		stopButton = new JButton(new ImageIcon(OpenDeductionProofEditor.class.getResource("icons/Stop16.gif")));
		stopButton.addActionListener(this);
		stopButton.setEnabled(false);
		toolBar.add(stopButton);
		toolBar.addSeparator();
		toolBar.addSeparator();
		frame.getContentPane().add(toolBar, BorderLayout.NORTH);
		
		popup = new JPopupMenu("Proof Actions");
		stepItem2 = new JMenuItem("Do One Proof Step...", KeyEvent.VK_S);
		stepItem2.addActionListener(this);
		undoItem2 = new JMenuItem("Undo", KeyEvent.VK_U);
		undoItem2.addActionListener(this);
		redoItem2 = new JMenuItem("Redo", KeyEvent.VK_U);
		redoItem2.addActionListener(this);
		
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	public synchronized void display_proof() {
		proofPanel.removeAll();
		proofPanel.repaint();
		
		if(derivation != null) {
			// get the derivation's view
			WidgetView n = derivation.draw();
			JComponent widget = n.getWidget();
			widget.addPropertyChangeListener(this);
			widget.addMouseListener(this);
			widget.addKeyListener(this);
		
			GridBagConstraints c = new GridBagConstraints();
			c.gridx = 0;
			c.gridy = 0;
			c.weighty = 1;
			proofPanel.add(new JLabel(""), c);
			c.gridy = 1;
			c.weighty = 0;		
			proofPanel.add(widget, c);

			int frame_w = 600;
			int frame_h = 600;

            int widget_w = (int)widget.getPreferredSize().getWidth() + 200;
            int widget_h = (int)widget.getPreferredSize().getHeight() + 200;

            if (widget_w > frame_w || widget_h > frame_h) {
				if (widget_w > frame_w)
					frame_w = widget_w;
				if (widget_h > frame_h)
					frame_h = widget_h;
				frame.setSize(new Dimension(frame_w, frame_h));
			}
		}
		frame.validate();
	}
	
	private Rewrite get_choice(Pair<Vector<Rewrite>, Vector<Rewrite>> choices) {
		Rewrite[] a = new Rewrite[20];
		Rewrite deflt = null;
		Collection<Rewrite> choicesCollection = new Vector<Rewrite>();
		if(choices.fst != null)
			choicesCollection.addAll(choices.fst);
		if(choices.snd != null)
			choicesCollection.addAll(choices.snd);

		for(Rewrite r : choicesCollection) {
			deflt = r;
			break;
		}

		if(choicesCollection.size() == 1)
			return deflt;
		return (Rewrite) 
			JOptionPane.showInputDialog(frame, "Select a Step", "Select step", JOptionPane.QUESTION_MESSAGE, null, choicesCollection.toArray(a), deflt);
	}
	
	
	
	/**
	 * This method is called when a new proof must be created
	 * @author adapted by Joe Lynch, from original method in GraPE by Max Schaefer
	 *  
	 */
	private void new_proof() {
		derivation = null;
		current_selection = null;
		curr_connective=null;
		multiple_derivations=false;
		valid_selection=false;
		curr_pair.clear();

		subatomised = false;
		display_proof();
		if(ruleChooser != null) {
			ruleChooser.setVisible(false);
			ruleChooser.showSubatomicRules(false);
			ruleChooser = null;
		}
		
		String fn = description_file == null ? "" :
		description_file.getAbsolutePath();
		SystemChooser syschoose = new SystemChooser(frame, fn);
		syschoose.pack();
		syschoose.setLocationRelativeTo(frame);
		syschoose.setVisible(true);
		String sysfn = syschoose.getSystemFilename();
		
		// check whether the user hit the "Cancel" button
		if(sysfn.equals("")) {
			status(NO_ACTIVE_PROOF);
			return;
		}
		// initialize the system
		initialize_system(sysfn);
		
		FormulaEditor sc = new FormulaEditor(frame);
		
		boolean done = false;

		while(!done) {
			sc.show();
			//gets the user input e.g. the formula
			String f = sc.getFormula();
			
			if (f == null) {
				status(NO_ACTIVE_PROOF);
				new_proof();
				//status(NO_ACTIVE_PROOF);
				//break;
			}
			// initialize the backend and set everything up
			try {
				status(INITIALIZING_BACKEND);
				system.start();
				
				String ctxt = system.getEmbedding();
				if(ctxt != null)
					f = ctxt.replace("#1", f);
				//gets the derivation
				//derivation = system.getBackend().normalize( system.getParser().parse(f.trim(),system.getLexer()));

				derivation = system.getBackend().test(f.trim());

				derivation.prettyprint();
				current_selection = new Selection(derivation);
				undo_stack.push(derivation.clone());
				status(PROVING);
				done = true;
			} catch(ProverException e) {
				ExceptionDialog.showExceptionDialog(frame, e);
				status(NO_ACTIVE_PROOF);
			} catch (IOException | MaudeException parseError) {
				parseError.printStackTrace();
			}
		}
	}
	
	
	private static final int NO_ACTIVE_PROOF = 0;
	private static final int INITIALIZING_BACKEND = 1;
	private static final int PROVING = 2;
	private static final int READING_CHOICES = 3;
	private static final int STOPPING_BACKEND = 4;
	private static final int SEARCHING = 5;
	private static final int maxStatus = 5;

	
	/**
	 * This method provides the status of the open deduction proof editor
	 * @author Joe Lynch, adapted from Max Schaefer's version
	 * 
	 * @param newStatus     the current status of the system
	 */
	private void status(int newStatus) {
		if(newStatus < 0 || newStatus > maxStatus)
			throw new IndexOutOfBoundsException("not a valid status: "+newStatus);
		switch(newStatus) {
		case NO_ACTIVE_PROOF:
			derivation = null;
			current_selection = null;
			system = null;

			undo_stack.clear();
			canNew(true);
			canStep(false);
			canUndo(false);
			canRedo(false);
			canStop(false);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					statusBar.setText("No active proof");
					setCursor(NORMAL);
					if(ruleChooser != null) {
						ruleChooser.setVisible(false);
						ruleChooser = null;
					}
					display_proof();
				}
			});
			break;
		case PROVING:
			canNew(true);
			canStep(true);
			canUndo(true);
			canRedo(true);
			canStop(false);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					statusBar.setText("Proving");
					setCursor(NORMAL);
					ruleChooser.setVisible(true);
					display_proof();
				}
			});
			break;
		case INITIALIZING_BACKEND:
			canNew(false);
			canStep(false);
			canUndo(false);
			canRedo(false);
			canStop(false);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					statusBar.setText("Initializing Backend");
					setCursor(BUSY);
				}
			});
			break;
		case READING_CHOICES:
			canNew(false);
			canStep(false);
			canUndo(false);
			canRedo(false);
			canStop(true);
			SwingUtilities.invokeLater(() -> {
				statusBar.setText("Reading Choices");
				setCursor(BUSY);
			});
			break;
		case STOPPING_BACKEND:
			canNew(false);
			canStep(false);
			canUndo(false);
			canRedo(false);
			canStop(false);
			SwingUtilities.invokeLater(() -> {
				statusBar.setText("Stopping Backend");
				setCursor(BUSY);
			});
			break;
		case SEARCHING:
			canNew(false);
			canStep(false);
			canUndo(false);
			canRedo(false);
			canStop(true);
			SwingUtilities.invokeLater(() -> {
				statusBar.setText("Searching");
				setCursor(BUSY);
			});
			break;
		}
	}
	
	/** Called to indicate whether a new derivation can be started. */
	private void canNew(boolean b) {
		newItem.setEnabled(b);
		newButton.setEnabled(b);
	}

	/** Called to indicate whether a proofstep can be taken. */
	private void canStep(boolean b) {
		stepItem1.setEnabled(b);
		stepItem2.setEnabled(b);
	}
	
	/** Called to indicate whether undo is possible. */
	private void canUndo(boolean b) {
		if(undo_stack.size() <= 1)
			b = false;
		undoItem1.setEnabled(b);
		undoItem2.setEnabled(b);
		undoButton.setEnabled(b);
	}

	/** Called to indicate whether undo is possible. */
	private void canRedo(boolean b) {
		if(redo_stack.empty())
			b = false;
		redoItem1.setEnabled(b);
		redoItem2.setEnabled(b);
		redoButton.setEnabled(b);
	}
	
	/** Called to indicate whether the backend can be interrupted. */
	private void canStop(boolean b) {
		stopButton.setEnabled(b);
	}
	
	/** Populates the popup menu with menu entries for every
	 * currently activated rule
	 */


	private void populate_popup() {

		RuleItem ri;
		popup.removeAll();
		popup.add(stepItem2);
		popup.add(undoItem2);
		popup.add(redoItem2);
		//popup.addSeparator();
		//for(Rule r : system.getRules())
		//	if(ruleChooser == null || ruleChooser.ruleActive(r)) {
		//		ri = new RuleItem(r);
		//		ri.addActionListener(this);
		//		popup.add(ri);
		//	}

	}

	private static class RuleItem extends JMenuItem {
		
		private Rule rule;
		private static final long serialVersionUID = 1L;
		public RuleItem(Rule r) {
			super(r.getName());
			this.rule = r;
		}
		public Rule getRule() {
			return rule;
		}
	}
	
	private static final int NORMAL = 0;
	private static final int BUSY = 1;
	private static final int maxCursor = 1;

	private void setCursor(int cursor) {
		if(cursor < 0 || cursor > maxCursor)
			throw new IndexOutOfBoundsException("not a valid cursor: "+cursor);
		switch(cursor) {
		case NORMAL:
			contentPanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			break;
		case BUSY:
			contentPanel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			break;
		}
	}

	private class Invoker implements Runnable { 
		private Object src;
		
		public Invoker(Object src) {
			this.src = src;
		}
		
		public void run() {	
			if ((src == newItem) || (src == newButton)) {
				new_proof();
			} else if(((src == stepItem1) || (src == stepItem2) || (src.equals('\n')))) {
				do_proofstep(getEnabledRules());
				curr_pair.clear();
			} else if((src == subatomiseButton)) {
				try {
					if(!subatomised)
						subatomise();
				} catch (ProverException | MaudeException | IOException e) {
					e.printStackTrace();
				}
            } else if((src == interpretButton)) {
				try {
					if(subatomised)
						interpret();
				} catch (ProverException | MaudeException | IOException e) {
					e.printStackTrace();
				}
            } else if((src == proofSearchButton)) {
				try {
					proofSearch();
				} catch (ProverException | MaudeException | IOException e) {
					e.printStackTrace();
				}
			} else if (src == exitItem) {
				System.exit(0);
			} else if ((src == undoItem1) || (src == undoItem2) || (src == undoButton)) {
				undo();
			} else if ((src == redoItem1) || (src == redoItem2) || (src == redoButton)) {
				redo();
			}
			else if (src == stopButton) {
				try {
					status(STOPPING_BACKEND);
					system.getBackend().abort();
					status(PROVING);
				} catch(Exception e) {
					ExceptionDialog.showExceptionDialog(frame, e);
				}
			} else if(src instanceof ActiveCharacter) {
				ActiveCharacter ac = (ActiveCharacter)src;
				ac.trigger();
				display_proof();
				curr_pair.clear();
			} else if(src instanceof RuleItem) {
				Vector<Rule> tmp = new Vector<Rule>(1);
				tmp.add(((RuleItem)src).getRule());
				do_proofstep(tmp);
			}
		}
	}

	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		Thread t = new Thread(new Invoker(src), "Invoker");
		try {
			Thread.sleep(10);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		t.start();
	}

	public void mousePressed(MouseEvent e) {
		if(e.isPopupTrigger()) {
			populate_popup();
			popup.show(e.getComponent(), e.getX(), e.getY());
		}
	}

	public void mouseReleased(MouseEvent e) {
		if(e.isPopupTrigger()) {
			populate_popup();
			popup.show(e.getComponent(), e.getX(), e.getY());
		}
	}

	/**
	 * This method finds is called when any property event changes are fired
	 * it deals with new selections and removing selections - as well as active characters
	 * @author Joe Lynch
	 * 
	 */
	public void propertyChange(PropertyChangeEvent evt) {
        switch (evt.getPropertyName()) {
            case "ActiveCharacterEvent":
            case "ActiveCharacterEventJoin":
                ActiveCharacter ac = (ActiveCharacter) evt.getNewValue();
                SwingUtilities.invokeLater(new Invoker(ac));
                break;
            case "SelectionErased":
                curr_pair.remove(evt.getSource());

                //reset the selections of nodes
                for (Selection k : curr_pair.values())
                    k.getSurroundingNode().setSelection(k.getStart(), k.getEnd());

                multiple_derivations = false;

                //if no formula, single formula, or multiple formulae selected
                if (curr_pair.size() == 1) {
                    for (Object o : curr_pair.keySet())
                        current_selection = (Selection) curr_pair.get(o);
                    valid_selection = true;
                } else if (curr_pair.size() == 0) {
                    current_selection = new Selection(derivation);
                    valid_selection = true;
                } else
                    multipleDerivationProcess();
                break;
            case "NewSelectionEvent":
                curr_pair.put(evt.getSource(), (Selection) evt.getNewValue());
                //reset the selections of nodes
                for (Selection k : curr_pair.values())
                    k.getSurroundingNode().setSelection(k.getStart(), k.getEnd());

                multiple_derivations = false;

                //if single formula selected, else if multiple formulae selected
                if (curr_pair.size() <= 1) {
                    valid_selection = true;
                    current_selection = (Selection) evt.getNewValue();
                } else
                    multipleDerivationProcess();
                break;
        }
	}
	
	/**
	 * This method finds the lca and creates the redex tree for multiple selections
	 * It also checks that a selection was valid
	 * @author Joe Lynch
	 * 
	 */
	private synchronized void multipleDerivationProcess() {
		redexNode = null;
		derivation.refresh();

		Vector<Node> selectedNodes = curr_pair.values().stream().map(Selection::getSurroundingNode).collect(Collectors.toCollection(Vector<Node>::new));
		selectedNodes.forEach(item -> item.setSpecial(false));
		
		orderByDepth(selectedNodes, false);
		Node initialNode = selectedNodes.lastElement();
		selectedNodes.remove(initialNode);
		
		//finds the lowest common ancestor connective
		Node connective = lowestCommonAncestor(initialNode, selectedNodes);
		if(connective == null) {
			multiple_derivations = false;
			current_selection = new Selection(derivation);
			return;
		}
		
		selectedNodes = curr_pair.values().stream().map(Selection::getSurroundingNode).collect(Collectors.toCollection(Vector<Node>::new));
		connective.refresh();
		
		//checks if the selection is valid
		valid_selection = validate(selectedNodes, connective);
		
		current_selection = connective.getSelection();
		curr_connective = connective;
		multiple_derivations = true;
	}

	/**
	 * This method finds the lowest common ancestor node
	 * it recursively goes through the list to find the lca node
	 * @author Joe Lynch
	 * 
	 * @param nodeA			the node to test against
	 * @param nodes 		list of nodes 
	 */
	private Node lowestCommonAncestor(Node nodeA, Vector<Node> nodes) {
			Node nodeB = nodes.lastElement();
			Vector<Node> currChildren = new Vector<Node>();
			currChildren.add(nodeA);
			currChildren.add(nodeB);
		    // Find all of nodeA's ancestors.
			Vector<Node> ancestorsA = new Vector<Node>();
			Node lca = null;
			Node pA;
		    while ((pA=nodeA.getParent()) != null) {
		    	ancestorsA.add(nodeA);
		        nodeA = pA;
		    }
		    ancestorsA.add(nodeA);
		    
		    // Compare nodeB's ancestors to the list of NodeA's ancestors
		    Node pB;
		    while ((pB=nodeB.getParent()) != null) {
		        if (ancestorsA.contains(nodeB)) {
		            lca = nodeB;
		            break;
		        }
		        nodeB = pB;
		    }
		    if(ancestorsA.contains(nodeB))
		    	lca = nodeB;
		    else
		    	lca = null;
		    
		    nodes.remove(nodes.lastElement());
		    
		    if(lca == null) {
		    	return null;
		    }
		    // Create unary redex for rewriting
		    if(lca.getSyntax() instanceof UnaryOperator) {
		    	Node tmp = lca.clone();
		    	tmp.getUnary();
		    	redexNode = tmp;
		    }
		    else {
			    // Create redexNode for rewriting
			    if(currChildren.contains(lca) && redexNode != null) {
			    	redexNode.addChild(currChildren.lastElement());
			    }
			    else {
				    Vector<Node> tmpChildren = new Vector<Node>();
				    if(redexNode != null) {
				    	tmpChildren.add(redexNode);
				    	tmpChildren.add(currChildren.lastElement());
				    }
				    else
				    	tmpChildren = currChildren;
				    redexNode = new Node(system, lca.getSyntax(), tmpChildren);
				    redexNode.removeRedundantOperators();
			    }
		    }
		    
		    if(nodes.isEmpty())
		    	return lca;
		    else
		    	return lowestCommonAncestor(lca, nodes);
	}
	
	/**
	 * This method validates whether the selection was correct
	 * @author Joe Lynch
	 * 
	 * @param nodes 		list of selected nodes
	 * @parem connective	the lca node
	 */
	private boolean validate(Vector<Node> nodes, Node connective) {
		Vector<Node> ign = validateUnary(nodes);
		Vector<Node> parents = validateParents(nodes, connective, ign);
		if(parents == null)
			return false;
		if(allEqual(parents))
			return true;
		else return validateSpecialCase(parents, connective);
    }
	
	/**
	 * This method validates whether the a selection was made with all
	 * the same syntax of nodes in the path
	 * @author Joe Lynch
	 * 
	 * @param nodes 		list of nodes 
	 * @parm connective		lca node
	 */
	private boolean validateSpecialCase(Vector<Node> nodes, Node connective){
		//finds all syntax elements (only binary operators in this case) in path to connective, for each node
		
		Vector<SyntaxElement> syns = new Vector<SyntaxElement>();
		for(Node n : nodes) {
			Vector<Node> selectedNodes1 = curr_pair.values().stream().map(Selection::getSurroundingNode).collect(Collectors.toCollection(Vector<Node>::new));
			Pair<Node, Vector<Node>> pClone = n.specialClone(selectedNodes1);
			pClone.fst.updateSelection(pClone.snd);
			if(!pClone.fst.getSelection().isCompleteNode())
				syns.add(n.getSyntax());
			Node p;
			while((p = n.getParent())!=connective && (p = n.getParent())!=null) {
				if(p.getSyntax() instanceof BinaryOperator || p.getSyntax() instanceof UnaryOperator)
					syns.add(p.getSyntax());
				n = p;
			}
		}
		syns.add(connective.getSyntax());
        return syns.size() > 1 && allEqualSyn(syns);
	}
	
	/**
	 * This method validates whether the parents are the same for
	 * a list of nodes - it checks the selections are siblings
	 * @author Joe Lynch
	 * 
	 * @param nodes 		list of nodes
	 * @parm connective		lca node
	 * @parem ign			nodes to ignore
	 */
	private Vector<Node> validateParents(Vector<Node> nodes, Node connective, Vector<Node> ign) {
		if(ign == null)
			return null;
		Vector<Node> parents = pars(nodes, connective, ign);

		orderByDepth(parents, true);
		Set<Node> parentsSet = new LinkedHashSet<Node>(parents);
		for(Node p : parentsSet) {
			if ( (Collections.frequency(parents, p) == p.getNumChildren() &&
					Collections.frequency(parents, p) != 1)
					|| (p.getSyntax() instanceof UnaryOperator
							&& Collections.frequency(parents, p) > 1
							&& p.getSpecial())) {
				
					Vector<Node> selectedNodes1 = curr_pair.values().stream().map(Selection::getSurroundingNode).collect(Collectors.toCollection(Vector<Node>::new));
					Pair<Node, Vector<Node>> pClone = p.specialClone(selectedNodes1);
					pClone.fst.updateSelection(pClone.snd);
					if(!pClone.fst.getSelection().isCompleteNode()) {
						parents.removeIf(p::equals);
					}
					else {
						parents.removeIf(p::equals);
						parents.add(pars(p, connective));
					}
				}
				else
					continue;
			}
		return parents;
	}
	
	
	/**
	 * This method validates whether the selection of a unary operator
	 * is correct
	 * @author Joe Lynch
	 * 
	 * @param nodes 		list of nodes 
	 */
	private Vector<Node> validateUnary(Vector<Node> nodes) {
		Vector<Node> ign = new Vector<Node>();
		Vector<Node> nds = new Vector<Node>(nodes);
		
		for(Node u : nds) {
			if (u.getSyntax() instanceof UnaryOperator
					&& !(u.getChildren().size() == 1 && u.getChild(0).getChildren().size() == 0))
				u.setSpecial(true);
		}
		
		for(Node n : nds) {
			Vector<Node> tmp = new Vector<Node>();
			if (n.getSyntax() instanceof UnaryOperator
				&& !(n.getChildren().size() == 1 && n.getChild(0).getChildren().size() == 0)){
				
				Node suitableChild = n.getChild(0).getUnary1();
				for(Node n1 : nds) {
					if (n1.getParent() != null && (n1.getParent().unaryAncestor(n))) {
						tmp.add(n1);	
					}
				}
				
				if(tmp.isEmpty())
					return null;
				else if(tmp.size()==1 && tmp.firstElement().getParent() == suitableChild)
					return null;
				
				Vector<Node> tempNode;
				if (!(tempNode = validateParents(tmp, suitableChild, new Vector<Node>())).isEmpty()
						&& tempNode.firstElement() == suitableChild
						&& allEqual(tempNode)) {			
					ign.addAll(tmp);
				}
				else {
					return null;
				}
			}
		}
		return ign;
		
	}
	
	
	/**
	 * This method finds the parents for a single node
	 * and doesn't ignore any nodes
	 * @author Joe Lynch
	 * 
	 * @param n 			the node to find the parents of
	 * @param connective    the lca node
	 */
	private Node pars(Node n, Node connective) {
		
		Vector<Node> parents = new Vector<Node>();

		Node p;
		while((p = n.getParent())!=null && p.getSyntax() instanceof InferenceRule && n != connective) {
				if(!n.getSelection().isCompleteNode() && !(n.getSyntax() instanceof InferenceRule))
					break;
			n = p;
		}
		if((!n.getSelection().isCompleteNode() && !(n.getSyntax() instanceof InferenceRule))
				|| p == null
				|| n == connective)
			parents.add(n);
		else
			parents.add(p);
	
		return parents.firstElement();
	}
	
	/**
	 * This method finds the parents of a list of connectives
	 * @author Joe Lynch
	 * 
	 * @param nodes         the nodes to find the parents of
	 * @param connective   	the lca node
	 * @param ign           the nodes to ignore
	 */
	private Vector<Node> pars(Vector<Node> nodes, Node connective, Vector<Node> ign){
		Vector<Node> parents = new Vector<Node>();
		for(Node n : nodes) {
			Node p;
			while((p = n.getParent())!=null && p.getSyntax() instanceof InferenceRule && n != connective && !n.getSpecial()) {
					if(!n.getSelection().isCompleteNode() && !(n.getSyntax() instanceof InferenceRule))
						break;
				n = p;
			}
			if((!n.getSelection().isCompleteNode() && !(n.getSyntax() instanceof InferenceRule))
					|| p == null
					|| n == connective
					|| n.getSpecial())
				parents.add(n);
			else
				parents.add(p);
		}
		return parents;
	}
	
	
	/**
	 * This method checks if all nodes in a list are the same
	 * @author Joe Lynch
	 * 
	 * @param connectives    the nodes to check
	 */
	private boolean allEqual(Vector<Node> connectives) {
		if(connectives == null)
			return false;
		for(Node c : connectives)
			if (c == null)
				return false;
			else if (c.equals(connectives.firstElement()))
				continue;
			else
				return false;
		return true;
	}
	
	/**
	 * This method checks if the syntax of all operators in a list is the same
	 * @author Joe Lynch
	 * 
	 * @param  syns    list of nodes and their syntax
	 */
	private boolean allEqualSyn(Vector<SyntaxElement> syns) {
		for(SyntaxElement s : syns)
			if (s.equals(syns.firstElement()))
				continue;
			else
				return false;
		return true;
	}
	
	/**
	 * This method orders nodes in a list by their depth in the tree
	 * @author Joe Lynch
	 * 
	 * @param nodes         the nodes to reorder
	 * @param reverse       whether it should order in reverse
	 */
	private void orderByDepth(Vector<Node> nodes, Boolean reverse) {
		Vector<Integer> orderedByDepth = new Vector<>();
		nodes.forEach(item -> orderedByDepth.add(item.getDepth(0)));
		if(reverse)
			orderedByDepth.sort(Collections.reverseOrder());
		else
			Collections.sort(orderedByDepth);
		nodes.sort(Comparator.comparing(item -> orderedByDepth.indexOf(item.getDepth(0))));
	}

	public void mouseClicked(MouseEvent arg0) {
	}

	public void mouseEntered(MouseEvent arg0) {
	}

	public void mouseExited(MouseEvent arg0) {
	}

	public void keyPressed(KeyEvent e) {
	}

	public void keyReleased(KeyEvent e) {
	}

	public void keyTyped(KeyEvent e) {
		char c = e.getKeyChar();
		if(c == '\r' || c == '\n') {
			SwingUtilities.invokeLater(new Invoker('\n'));
		}
	}

	/**
	 * The main method initializes the model, displays
	 * the GUI, and prompts the user to start a new proof.
	 * 
	 * @param args command line parameters; not currently used
	 */
	public static void main(String[] args) {
		final OpenDeductionProofEditor app = new OpenDeductionProofEditor();
		app.init_model();
		app.init_view();
		app.new_proof();
	}
}
