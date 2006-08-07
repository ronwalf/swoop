//The MIT License
//
// Copyright (c) 2004 Mindswap Research Group, University of Maryland, College Park
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to
// deal in the Software without restriction, including without limitation the
// rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
// sell copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
// IN THE SOFTWARE.

package org.mindswap.swoop.debugging;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.mindswap.pellet.debug.owlapi.Reasoner;
import org.mindswap.swoop.SwoopFrame;
import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.reasoner.PelletReasoner;
import org.mindswap.swoop.renderer.SwoopCellRenderer;
import org.mindswap.swoop.renderer.entity.ConciseFormatEntityRenderer;
import org.mindswap.swoop.renderer.entity.OWLObjectContainer;
import org.mindswap.swoop.utils.SetUtils;
import org.mindswap.swoop.utils.owlapi.EntailmentChecker;
import org.mindswap.swoop.utils.owlapi.OWLDescriptionFinder;
import org.mindswap.swoop.utils.owlapi.OWLOntBuilder;
import org.mindswap.swoop.utils.ui.EntityComparator;
import org.mindswap.swoop.utils.ui.JTabbedPaneWithCloseIcons;
import org.semanticweb.owl.impl.model.OWLDataFactoryImpl;
import org.semanticweb.owl.impl.model.OWLObjectPropertyRangeAxiomImpl;
import org.semanticweb.owl.impl.model.OWLPropertyDomainAxiomImpl;
import org.semanticweb.owl.model.OWLAnd;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLClassAxiom;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataType;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDisjointClassesAxiom;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLNot;
import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOr;
import org.semanticweb.owl.model.OWLProperty;
import org.semanticweb.owl.model.OWLPropertyDomainAxiom;
import org.semanticweb.owl.model.OWLSubClassAxiom;
import org.semanticweb.owl.model.change.ChangeVisitor;
import org.semanticweb.owl.model.change.RemoveClassAxiom;
import org.semanticweb.owl.model.helper.OntologyHelper;

/**
 * @author Aditya
 *
 */
public class RepairFrame extends JFrame implements HyperlinkListener, ListSelectionListener, ActionListener, ItemListener {

	// Main Swoop stuff:
	SwoopFrame swoopHandler;
	SwoopModel swoopModel;
	PelletReasoner pellet;
	OWLOntology ontology;
	Reasoner pelletDebug;
	
	// UI stuff:
	JSplitPane axiomTablePane;
	JEditorPane repTable, planPane, indentPane, keptPane, removedPane;
	JFrame keptFrame, removedFrame;
	Font tahoma = new Font("Tahoma", Font.PLAIN, 11);
	JList classList;
	JButton rankBtn, generateBtn, settingsBtn, clearBtn, execBtn, saveBtn, previewBtn;
	JButton keptBtn, removedBtn;
	JComboBox granularityCombo;
	JCheckBox rewriteChk, viewGlobalChk, updatePlanChk, impactUnsatChk;
	JTextField w_arityFld, w_impactFld, w_usageFld;
	JTabbedPaneWithCloseIcons planTabs;
	boolean displayMUPSUnion = true, rewriteEnabled = false, viewGlobal = false;
	int planCtr = 0;
	public Set[] planSolnAxioms = new HashSet[100];
	final boolean DEBUG = true;
	String logFile = "";
	String NEWLINE = System.getProperty("line.separator");
	int logCtr = 0;
	
	// Core Repair Stuff:
	Set currUnsat, currRoots, currDerived;
	List currAxioms;
	HashMap claMUPSMap; // class->MUPS
	public HashMap axiomUnsatClaMap, axiomUsageMap, axiomSOSMap, axiomRanksMap; // ranking params
	public HashMap objectMap; // generically used to store contents of hyperlinked-popups
	public HashMap hcodeAxiomMap; 
	public HashMap whyMap;
	public Set keptAxiomSet, removedAxiomSet, rewriteAxiomSet; 
	private boolean enableImpactUnsat = true;
	private boolean turnOffUsage = false;
	private int baseRank = 10;
	
	// Repair Parameters:
	final int ARITY = 0;
	final int IMPACT = 1;
	final int USAGE = 2;
	final int RANK = 3;
	int CURR_METRIC = 3;
	double[] weights = new double[3];
	
	/*
	 * constructor: called from SwoopFrame
	 */
	public RepairFrame(SwoopFrame handler, SwoopModel model, PelletReasoner pelletReasoner) {
		
		// pass all required parameters
		this.swoopHandler = handler;
		this.swoopModel = model;
		this.pellet = pelletReasoner;
		
		// check if pellet has processed currently selected ontology
		if (pelletReasoner.getOntology()==null || !pellet.getOntology().equals(swoopModel.getSelectedOntology())) {
			try {
				pelletReasoner.setOntology(swoopModel.getSelectedOntology());
			} catch (OWLException e) {
				e.printStackTrace();
			}
		}
		this.ontology = swoopModel.getSelectedOntology(); // should be same as pellet.getOntology()!
		this.setWeights(0.9, 0.7, 0.1);
		this.resetSets();
		setupUI();
		if (DEBUG)
			try {
				logFile = "Launching Repair UI for Ontology "+swoopModel.shortForm(ontology.getURI())+" "+swoopModel.getTimeStamp();
			} catch (OWLException e) {
				e.printStackTrace();
			}
		// set log counter
		File file = null;
		do {
			logCtr++;
			file = new File("REPAIR-LOG_"+String.valueOf(logCtr)+".txt");			
		}
		while (file.exists());		
	}
	
	/* 
	 * set weights of parameters for computing axiom ranks
	 */
	private void setWeights(double arity, double impact, double usage) {
		weights[ARITY] = arity;
		weights[IMPACT] = impact;
		weights[USAGE] = usage;
	}
	
	private void resetSets() {
		this.keptAxiomSet = new HashSet();
		this.removedAxiomSet = new HashSet();
		this.rewriteAxiomSet = new HashSet();
	}
	
	/*
	 * reset (clear) all HashMaps
	 */
	private void resetMaps() {
		
		this.axiomUnsatClaMap = new HashMap();
		this.axiomSOSMap = new HashMap();
		this.axiomUsageMap = new HashMap();
		this.axiomRanksMap = new HashMap(); 
		
		this.claMUPSMap = new HashMap();
		
		this.objectMap = new HashMap();
		this.hcodeAxiomMap = new HashMap();
			
		this.whyMap = new HashMap();
	}
	
	/*
	 * compute new root, derived unsatisfiable classes
	 * also insert mups for roots into axiomClaMUPS map
	 */
	private void initRoots() {
		// initialization
		this.currUnsat = new HashSet();
		this.currAxioms = new ArrayList();
		this.currRoots = new HashSet();
		this.currDerived = new HashSet();
		
		// obtain root, derived and total unsatisfiable classes in ontology
		if (pellet.depFinder == null) pellet.autoRootDiscovery();
		if (pellet.depFinder == null) {
			// no root classes!
			JOptionPane.showMessageDialog(this, "No More Unsatisfiable Concepts in Ontology", "Ontology Repair", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		
		this.currRoots = new HashSet(pellet.depFinder.rootClasses);
		this.currDerived = new HashSet(pellet.depFinder.derivedClasses);
		this.currUnsat.addAll(currRoots);
		this.currUnsat.addAll(currDerived);
		
		// get all mups for root classes and insert them in axiomClaMUPS
		for (Iterator iter = currRoots.iterator(); iter.hasNext();) {
			OWLClass root = (OWLClass) iter.next();
			this.findMUPS(root);
		}
		// also dynamically set cell renderer based on currRoots
		classList.setCellRenderer(new SwoopCellRenderer(swoopModel, currRoots));
	}
	
	public void launch() {
//		this.modifyEquivalence();
		
		resetMaps();
		this.refreshPelletDebuggerOntology(ontology);
		
		initRoots();
		
		//*** Special Case: Compute impact and ranks for all root mups axioms ***
		if (!currRoots.isEmpty()) {
//			this.computeImpactSOS(); //EITHER USE THIS OR LINE BELOW
	//		this.loadImpactSOSFile();
			this.computeRanks(axiomUnsatClaMap.keySet());		
		}
		
		// UI changes:
		this.populateClassList();
		planTabs.removeAll();
		planSolnAxioms[0] = new HashSet();
		planCtr = 0;
		planTabs.addTab("Main Plan", new JScrollPane(planPane));
		this.refreshPlan();
	}
	
	private void computeRanks(Set axioms) {
		for (Iterator iter = axioms.iterator(); iter.hasNext();) {
			OWLObject axiom = (OWLObject) iter.next();
			// get arity
			int arity = ((HashSet) this.axiomUnsatClaMap.get(axiom)).size();
			// get impact
			int impact = 0;
			// *** special cases -- 2 types ***
			// Type 1: compute impact on entailments between unsatisfiable concepts
			if (this.enableImpactUnsat) this.computeImpactUnsat(); //***************
			// Type 2: normal impact between satisfiable concepts
			try {
				if (axiom instanceof OWLSubClassAxiom) {
					OWLSubClassAxiom subAx = (OWLSubClassAxiom) axiom;
					if (subAx.getSubClass() instanceof OWLClass && subAx.getSuperClass() instanceof OWLClass) {
						OWLClass sub = (OWLClass) subAx.getSubClass();
						OWLClass sup = (OWLClass) subAx.getSuperClass();
						for (Iterator it = SetUtils.union(pellet.descendantClassesOf(sub)).iterator(); it.hasNext();) {
							OWLDescription s1 = (OWLDescription) it.next();
							for (Iterator it2 = SetUtils.union(pellet.ancestorClassesOf(sup)).iterator(); it2.hasNext();) {	
								OWLDescription s2 = (OWLDescription) it2.next();
								if (!s2.equals(ontology.getOWLDataFactory().getOWLThing()) && !s1.equals(ontology.getOWLDataFactory().getOWLNothing())) {
									OWLSubClassAxiom ax = pellet.getOntology().getOWLDataFactory().getOWLSubClassAxiom(s1, s2);
									this.addAxiom(axiomSOSMap, axiom, ax);
								}
							}
						}
					}						
				}
				else if (axiom instanceof OWLDisjointClassesAxiom) {
					Set dis = ((OWLDisjointClassesAxiom) axiom).getDisjointClasses();
					boolean complex = false;
					for (Iterator d = dis.iterator(); d.hasNext();) {
						OWLDescription desc = (OWLDescription) d.next();
						if (!(desc instanceof OWLClass)) {
							complex = true;
							break;
						}
					}
					if (!complex) {
						List disjoints= new ArrayList(dis);
						for (Iterator d = new ArrayList(disjoints).iterator(); d.hasNext();) {
							Object obj = d.next();
							if (!obj.equals(ontology.getOWLDataFactory().getOWLNothing())) {
								disjoints.remove(obj);
								Set descendants = SetUtils.union(pellet.descendantClassesOf((OWLClass) obj));
								descendants.removeAll(pellet.equivalentClassesOf(ontology.getOWLDataFactory().getOWLNothing()));
								if (enableImpactUnsat) {
									descendants.addAll(((OWLClass) obj).getSubClasses(ontology));
								}
								for (Iterator it = descendants.iterator(); it.hasNext();) {
									OWLDescription desc1 = (OWLDescription) it.next();
									if (!desc1.equals(ontology.getOWLDataFactory().getOWLNothing())) {
										if (enableImpactUnsat || !pellet.equivalentClassesOf(desc1).contains(ontology.getOWLDataFactory().getOWLNothing())) {
											for (Iterator it2 = disjoints.iterator(); it2.hasNext();) {								
												OWLDescription desc2 = (OWLDescription) it2.next();
												if (!desc2.equals(desc1)) {
													Set newDis = new HashSet();
													newDis.add(desc1);
													newDis.add(desc2);
													OWLDisjointClassesAxiom ax = pellet.getOntology().getOWLDataFactory().getOWLDisjointClassesAxiom(newDis);
													this.addAxiom(axiomSOSMap, axiom, ax);
												}
											}
										}
									}
								}
								disjoints.add(obj);
							}							
						}
					}
				}
				else if (axiom instanceof OWLPropertyDomainAxiom) {
					OWLPropertyDomainAxiom pd = (OWLPropertyDomainAxiom) axiom;
					Set onts = new HashSet();
					onts.add(ontology);
					if (pd.getDomain() instanceof OWLClass) {
						OWLClass dom = (OWLClass) pd.getDomain();
						Set sup = OWLDescriptionFinder.getSuperClasses(dom, onts);
						for (Iterator iter2 = sup.iterator(); iter2.hasNext();) {
							OWLDescription supCla = (OWLDescription) iter2.next();
							OWLPropertyDomainAxiomImpl ax = new OWLPropertyDomainAxiomImpl((OWLDataFactoryImpl) ontology.getOWLDataFactory(), pd.getProperty(), supCla);
							this.addAxiom(axiomSOSMap, axiom, ax);
						}
					}
				}
				else if (axiom instanceof OWLObjectPropertyRangeAxiom) {
					OWLObjectPropertyRangeAxiom pd = (OWLObjectPropertyRangeAxiom) axiom;
					Set onts = new HashSet();
					onts.add(ontology);
					if (pd.getRange() instanceof OWLClass) {
						OWLClass ran = (OWLClass) pd.getRange();
						Set sup = OWLDescriptionFinder.getSuperClasses(ran, onts);
						for (Iterator iter2 = sup.iterator(); iter2.hasNext();) {
							OWLClass supCla = (OWLClass) iter2.next();
							OWLObjectPropertyRangeAxiomImpl ax = new OWLObjectPropertyRangeAxiomImpl((OWLDataFactoryImpl) ontology.getOWLDataFactory(), pd.getProperty(), supCla);
							this.addAxiom(axiomSOSMap, axiom, ax);
						}
					}
				}
			}
			catch (OWLException ex) {
				ex.printStackTrace();
			}
			if (axiomSOSMap.containsKey(axiom)) impact = ((HashSet) this.axiomSOSMap.get(axiom)).size();
			// get usage
			int usage = ((HashSet) this.axiomUsageMap.get(axiom)).size();
			double rank = this.calculateRank(arity, impact, usage);
			String rankStr = String.valueOf(rank);
			if (rankStr.length()>4) rankStr = rankStr.substring(0, 4);
			axiomRanksMap.put(axiom, rankStr);
		}
	}
	
	/*
	 * Populate class list with unsat classes
	 */
	private void populateClassList() {
		Set claSet = new TreeSet(EntityComparator.INSTANCE);
		claSet.addAll(currUnsat);
//		claSet.add("<All Unsatisfiable>");
//		claSet.add("<All Roots>");
		classList.setListData(claSet.toArray());
		// select root classes
		int[] indices = new int[currRoots.size()];
		int i = 0;
		for (int ctr=0; ctr<classList.getModel().getSize(); ctr++) {
			if (currRoots.contains(classList.getModel().getElementAt(ctr)))
				indices[i++] = ctr;
		}
		classList.setSelectedIndices(indices);
		this.refreshClaListSelection();
	}
	
	/*
	 * refresh axiom results
	 */
	private void refreshTable(List order) {
		
		// set current Axioms displayed
		this.currAxioms = order;
		
		String html = "<html><body>";
		String dispUnion = "Union (<a href=\":DISP_UNION\">Toggle</a>)";
		if (!this.displayMUPSUnion) dispUnion = "Intersection (<a href=\":DISP_UNION\">Toggle</a>)";
		html += insFont()+"Displaying Axioms in " + dispUnion + " of MUPS of Selected Classes <br>";
//		html += insFont() + "<b>Root Classes: ";
//		for (Iterator iter = currRoots.iterator(); iter.hasNext();) {
//			OWLClass root = (OWLClass) iter.next();
//			html += this.getOWLObjectHTML(root) + " ";
//		}
		html += "</b><hr><table border=\"1\">";
		html += "<tr><td>"+insFont()+"<b>Erroneous Axioms</b></td><td>"+insFont()+"<b><a href=\":ARITY\">Arity</a></b></td><td>"+insFont()+"<b><a href=\":IMPACT\">Impact</a></b></td><td>"+insFont()+"<b><a href=\":USAGE\">Usage</a></b></td><td>"+insFont()+"<b><a href=\":RANK\">Rank</a></b></td><td>"+insFont()+"<b>Status</b></td></tr>";
		
		for (Iterator iter = order.iterator(); iter.hasNext();) {
			
			OWLObject axiom = (OWLObject) iter.next();
			
			if (keptAxiomSet.contains(axiom)) html += "<tr bgcolor=\"C3FDB8\"><td>";
    		else if (removedAxiomSet.contains(axiom)) html += "<tr bgcolor=\"FFDDDD\"><td>";
    		else html += "<tr><td>";
			
			html += insFont();
			hcodeAxiomMap.put(String.valueOf(axiom.hashCode()), axiom);
			html += this.getOWLObjectHTML(axiom); 
			html += "</td>";
			
			int arity = ((HashSet) this.axiomUnsatClaMap.get(axiom)).size();
			String hash = String.valueOf(this.axiomUnsatClaMap.get(axiom).hashCode());
			objectMap.put(hash, this.axiomUnsatClaMap.get(axiom));
			html += "<td>" + insFont()+ "<a href=\":HASH:Arity:"+hash+"\">"+String.valueOf(arity) + "</a></td>";
			int impact = 0;
			Set impactSet = new HashSet();
			if (axiomSOSMap.containsKey(axiom)) impactSet = (HashSet) this.axiomSOSMap.get(axiom);
			impact = impactSet.size();
			hash = String.valueOf(impactSet.hashCode());
			objectMap.put(hash, impactSet);
			html += "<td>" + insFont()+ "<a href=\":HASH:Impact:"+hash+"\">" + String.valueOf(impact) + "</td>";
			int usage = ((HashSet) this.axiomUsageMap.get(axiom)).size();
			hash = String.valueOf(this.axiomUsageMap.get(axiom).hashCode());
			objectMap.put(hash, this.axiomUsageMap.get(axiom));
			html += "<td>" + insFont()+ "<a href=\":HASH:Usage:"+hash+"\">" + String.valueOf(usage) + "</td>";
			String rank = "-";
			if (axiomRanksMap.containsKey(axiom)) rank = this.axiomRanksMap.get(axiom).toString();
			html += "<td>" + insFont()+  rank + "</td>";
			
			String rem = "Remove";
			if (removedAxiomSet.contains(axiom)) rem="Undo";
			html += "<td>"+insFont()+"<font color = \"red\">[<a href=\":FORCE:"+axiom.hashCode()+"\">"+rem+"</a>]&nbsp;</font>";
			String keep = "Keep";
			if (keptAxiomSet.contains(axiom)) keep = "Undo";
			html += insFont()+"<font color = \"green\">[<a href =\":BLOCK:"+axiom.hashCode()+"\">"+keep+"</a>]&nbsp;</font>";
			html += "</td>";
			
			html += "</tr>";
		}
		
		html += "</table>";
		html += "</body></html>";
		repTable.setText(html);
		repTable.setCaretPosition(0);
		
	}
	
	/*
	 * Use ConciseFormatEntityRender to get HTML for a particular OWLObject
	 */
	private String getOWLObjectHTML(OWLObject object) {
		ConciseFormatEntityRenderer cfRend = new ConciseFormatEntityRenderer();
    	cfRend.setSwoopModel(swoopModel);
    	cfRend.visitor = cfRend.createVisitor();
    	StringWriter st = new StringWriter();
		PrintWriter buffer = new PrintWriter(st);
    	cfRend.setWriter(buffer);
    	try {
			cfRend.printObject(object);
		} catch (OWLException e) {
			e.printStackTrace();
		}
		return st.toString();
	}
	
	/*
	 * Refresh the reasoner (debugger) over the ontology 
	 */
	private void refreshPelletDebuggerOntology(OWLOntology ont) {
		pelletDebug = new Reasoner();
		try {
			pelletDebug.setOntology(ont);
		} catch (OWLException e) {
			e.printStackTrace();
		}
		pelletDebug.getKB().setDoExplanation(true);
		pelletDebug.getKB().doDependencyTracking = true;
	}
	
	private void findMUPS(OWLDescription desc) {
		this.findMUPS(desc, null);
	}
	
	/*
	 * Find MUPS of a single unsat. concept
	 * *** Store info in appropriate HashMaps: arity and usage ***
	 */
	private void findMUPS(OWLDescription desc, OWLObject entailment) {
		
		try {
			pelletDebug.isConsistent(desc);
			Set explSet = pelletDebug.getKB().getExplanationSet();
			// prune the axioms in case there are additional axioms
			Set mups = new HashSet(explSet);
			for (Iterator iter2 = explSet.iterator(); iter2.hasNext();) {
				OWLObject axiom = (OWLObject) iter2.next();
				mups.remove(axiom);
				boolean sat = false;
				sat = this.checkSatisfiability(mups, desc);
				if (sat) {
					mups.add(axiom);
					// add axiom-cla relationship to hashmap
					if (desc instanceof OWLClass) this.addAxiom(axiomUnsatClaMap, axiom, (OWLClass) desc);
					if (entailment!=null) this.addAxiom(axiomSOSMap, axiom, entailment);
						
					// compute usage of axiom when MUPS is found
					this.computeUsage(axiom);
				}
			}
			// also cross-references class with MUPS
			if (desc instanceof OWLClass) this.claMUPSMap.put((OWLClass) desc, mups);
			if (entailment!=null) this.whyMap.put(entailment, new HashSet(mups));
		}
		catch (OWLException ex) {
			ex.printStackTrace();
		}
	}
	
	/*
	 * return set of all MUPS of all root classes
	 */
	private Set findRootsMUPS() {
		
		try {
			Set rootMUPS = new HashSet();
			for (Iterator iter = currRoots.iterator(); iter.hasNext();) {
				OWLClass root = (OWLClass) iter.next();
				if (!claMUPSMap.containsKey(root)) this.findMUPS(root);
				rootMUPS.add(claMUPSMap.get(root));
			}
			return rootMUPS;
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		return new HashSet();
	}
	
	/* 
	 * Add a pair of (axiom, ..) to the corresponding hashmap
	 */
	private void addAxiom(HashMap map, OWLObject axiom, Object desc) {
		Set elems = new HashSet();
		elems.add(desc);
		if (map.containsKey(axiom)) elems.addAll((HashSet) map.get(axiom));
		map.put(axiom, elems);
	}
	
	private void setupUI() {
		try {
			Container content = this.getContentPane();
			JSplitPane mainPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
			
			classList = new JList();
			classList.setFont(tahoma);
			classList.addListSelectionListener(this);
			
			repTable = new JEditorPane();
			repTable.setContentType("text/html");
			repTable.setEditable(false);
			repTable.addHyperlinkListener(this);
			JPanel toprightPane = new JPanel();
			toprightPane.setLayout(new BorderLayout());
			JLabel wlbl = new JLabel("Weights: (Note: Rank = -W1 * arity + W2 * impact + W3 * usage)");
			wlbl.setFont(tahoma);
			w_arityFld = new JTextField("0.9");
			w_impactFld = new JTextField("0.7");
			w_usageFld = new JTextField("0.1");
			JLabel ariLbl = new JLabel(" W1: ");
			ariLbl.setFont(tahoma);
			JLabel impLbl = new JLabel(" W2: ");
			impLbl.setFont(tahoma);
			JLabel usaLbl = new JLabel(" W3: ");
			usaLbl.setFont(tahoma);
			Box box1 = Box.createHorizontalBox();
			box1.add(ariLbl);
			box1.add(w_arityFld);
			Box box2 = Box.createHorizontalBox();
			box2.add(impLbl);
			box2.add(w_impactFld);
			Box box3 = Box.createHorizontalBox();
			box3.add(usaLbl);
			box3.add(w_usageFld);
			rankBtn = new JButton("Recompute Ranks");
			rankBtn.setFont(tahoma);
			rankBtn.addActionListener(this);
			JToolBar bar = new JToolBar();
			bar.add(wlbl);
			bar.add(box1);
			bar.add(box2);
			bar.add(box3);
			bar.add(rankBtn);
			viewGlobalChk = new JCheckBox("View Axioms Globally");
			viewGlobalChk.setSelected(false);
			viewGlobalChk.addActionListener(this);
			bar.add(viewGlobalChk);
			toprightPane.add(bar, "North");
			indentPane = new JEditorPane();
			indentPane.setContentType("text/html");
			indentPane.setEditable(false);
			indentPane.addHyperlinkListener(this);
			axiomTablePane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
			toprightPane.add(axiomTablePane, "Center");
			axiomTablePane.setLeftComponent(new JScrollPane(repTable));
			axiomTablePane.setRightComponent(new JScrollPane(indentPane));
			
			JSplitPane axiomAnalysisPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
			axiomAnalysisPane.setRightComponent(toprightPane);
			axiomAnalysisPane.setLeftComponent(new JScrollPane(classList));
			
			keptBtn = this.createButton("Kept Axioms (0)");
			keptPane = new JEditorPane();
			keptPane.setContentType("text/html");
			keptPane.addHyperlinkListener(this);
			keptPane.setEditable(false);
			keptFrame = new JFrame("Axioms to be KEPT in Ontology");
			keptFrame.getContentPane().add(new JScrollPane(keptPane));
			keptFrame.setLocation(100, 100);
			keptFrame.setSize(500, 300);
			removedBtn = this.createButton("Removed Axioms (0)");
			removedPane = new JEditorPane();
			removedPane.setContentType("text/html");
			removedPane.addHyperlinkListener(this);
			removedPane.setEditable(false);
			removedFrame = new JFrame("Axioms to be REMOVED from Ontology");
			removedFrame.getContentPane().add(new JScrollPane(removedPane));
			removedFrame.setLocation(100, 200);
			removedFrame.setSize(500, 300);
			impactUnsatChk = new JCheckBox("Extended Impact");
			impactUnsatChk.setSelected(true);
			impactUnsatChk.setFont(tahoma);
			impactUnsatChk.addActionListener(this);
			generateBtn = this.createButton("Generate Plan");
			granularityCombo = new JComboBox();
			granularityCombo.setFont(tahoma);
			granularityCombo.addActionListener(this);
			granularityCombo.addItem("Repair All Unsatisfiable");
			granularityCombo.addItem("Repair All Roots");
			granularityCombo.addItem("Repair Selected (above)");
			granularityCombo.setSelectedIndex(1); // DEFAULT
			granularityCombo.addItemListener(this);
			rewriteChk = new JCheckBox("Include Rewrites");
			rewriteChk.setFont(tahoma);
			rewriteChk.addActionListener(this);
			updatePlanChk = new JCheckBox("Auto Recompute Plan"); // (when changes are made)");
			updatePlanChk.setFont(tahoma);
			updatePlanChk.addActionListener(this);
			updatePlanChk.setSelected(true);
			settingsBtn = this.createButton("Settings");
			JPanel toolBar = new JPanel();
			toolBar.setLayout(new GridLayout(2,7));
			toolBar.add(keptBtn);
			toolBar.add(removedBtn);
			toolBar.add(impactUnsatChk);
			toolBar.add(new JLabel(""));
			toolBar.add(new JLabel(""));
			toolBar.add(new JLabel(""));
			toolBar.add(new JLabel(""));
			toolBar.add(generateBtn);
			toolBar.add(granularityCombo);
			toolBar.add(rewriteChk);
//			toolBar.add(settingsBtn);
			toolBar.add(updatePlanChk);
			toolBar.add(new JLabel(""));
			toolBar.add(new JLabel(""));
			toolBar.add(new JLabel(""));
			JPanel topPanel = new JPanel();
			topPanel.setLayout(new BorderLayout());
			topPanel.add(axiomAnalysisPane, "Center");
			topPanel.add(toolBar, "South");
			
			mainPane.setTopComponent(topPanel);
			
			planTabs = new JTabbedPaneWithCloseIcons();
			planPane = new JEditorPane();
			planPane.setContentType("text/html");
			planPane.setEditable(false);
			planPane.addHyperlinkListener(this);
			planTabs.addTab("Main Plan", new JScrollPane(planPane));
			JPanel planPanel = new JPanel();
			planPanel.setLayout(new BorderLayout());
			planPanel.add(planTabs, "Center");
			clearBtn = createButton("Clear");
			saveBtn = createButton("Save");
			previewBtn = createButton("Preview");
			execBtn = createButton("Execute");
			JToolBar btnPanel = new JToolBar();
			btnPanel.add(clearBtn);
			btnPanel.add(saveBtn);
			btnPanel.add(previewBtn);
			btnPanel.add(execBtn);
			planPanel.add(btnPanel, "South");
			mainPane.setBottomComponent(planPanel);
			content.add(mainPane);
			
			this.setSize(1024, 740);
			this.setLocation(1, 1);
			this.setTitle("Repairing Ontology "+swoopModel.shortForm(ontology.getURI()));
			this.show();
			
			// set divider positions after displaying UI
			axiomAnalysisPane.setDividerLocation(150);
			mainPane.setDividerLocation(460);
			axiomTablePane.setDividerLocation(0);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private JButton createButton(String lbl) {
		JButton btn = new JButton(lbl);
		btn.setFont(tahoma);
		btn.addActionListener(this);
		return btn;
	}
	
	/*
	 * Used to prune explanation set obtained from Pellet
	 */
	public boolean checkSatisfiability(Set axioms, OWLDescription clazz) {
		// create a new ontology with axioms
		// and check satisfiability of clazz
		boolean sat = false;
		try {
			OWLOntBuilder ontBuilder = new OWLOntBuilder();
			
			// create new ontology using axioms
			// use OWLOntBuilder to build a new ontology given axioms
			for (Iterator iter = axioms.iterator(); iter.hasNext();) {
				OWLObject obj = (OWLObject) iter.next();
				obj.accept(ontBuilder);
			}
			
			// if clazz is not in ontology, return true
			OWLOntology newOnt = ontBuilder.currentOnt;
			if (clazz!=null && clazz instanceof OWLClass && newOnt.getClass(((OWLClass) clazz).getURI())==null) return true;
			else if (clazz!=null) {
				// get clazz in newOnt
				clazz = ontBuilder.visitDescription(clazz);				
			}
			
			// create new instance of pellet and check sat. of clazz
			PelletReasoner newPellet = new PelletReasoner();
			newPellet.setOntology(newOnt, false);
			if (clazz!=null) sat = newPellet.isConsistent(clazz);
			else sat = newPellet.isConsistent();
		}
		catch (Exception ex) {	
			System.out.println(ex.getMessage()); // clazz (description) may not be in ontology!
//			ex.printStackTrace();
			return true;
		}
		
		return sat;
	}
	
	private String insFont() {
		return("<FONT FACE=\""+swoopModel.getFontFace()+"\" SIZE="+swoopModel.getFontSize()+">");
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.HyperlinkListener#hyperlinkUpdate(javax.swing.event.HyperlinkEvent)
	 */
	public void hyperlinkUpdate(HyperlinkEvent e) {
		
		if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
//			if (e.getSource() == repTable || e.getSource() == planPane) {
				String hLink = e.getDescription();
				
				if (isURI(hLink)) {
					try {
						if (DEBUG) logFile += NEWLINE + "Clicking URI: "+hLink+" "+swoopModel.getTimeStamp();
						URI uri = new URI(hLink);
						// popup entity
						// if entity already displayed, return
						if (uri.equals(swoopModel.selectedOWLObject.getURI())) return;
						if (swoopModel.getOntologyURIs().contains(uri)) {
							swoopHandler.ontDisplay.selectOntology(swoopModel.getOntology(uri));
						}
						else {
							swoopHandler.termDisplay.selectEntity(hLink);
							// popup entity
							ConciseFormatEntityRenderer cfRend = new ConciseFormatEntityRenderer();
							boolean temp = swoopModel.getEditorEnabled();
							swoopModel.setEditorEnabled(false);
							cfRend.setSwoopModel(swoopModel);
							cfRend.createVisitor();
							StringWriter st = new StringWriter();
							cfRend.render(swoopModel.getSelectedEntity(), swoopModel, st);
							displayPopup(st.toString(), swoopModel.shortForm(swoopModel.getSelectedEntity().getURI()));
							swoopModel.setEditorEnabled(temp);
						}						
					} 
					catch (Exception e1) {						
						e1.printStackTrace();
					}					
				}
				else if (hLink.startsWith(":DISP_UNION")) {
					if (DEBUG) logFile += NEWLINE + "Switched MUPS Union: "+String.valueOf(!this.displayMUPSUnion)+ " "+swoopModel.getTimeStamp();
					this.displayMUPSUnion = !this.displayMUPSUnion;
					this.refreshClaListSelection();
				}
				else
				if (hLink.startsWith(":FORCE")) {
					// (un)force axiom into solution set
					if (DEBUG) logFile += NEWLINE + "REMOVED Axiom "+swoopModel.getTimeStamp();
					String hcode = hLink.substring(hLink.lastIndexOf(":")+1, hLink.length());
					OWLObject axiom = (OWLObject) hcodeAxiomMap.get(hcode);
					if (removedAxiomSet.contains(axiom)) {
						removedAxiomSet.remove(axiom);
//						solnAxiomSet.remove(axiom); ??
					}
					else
					if (!keptAxiomSet.contains(axiom)) {
						removedAxiomSet.add(axiom);						
					}
					
					removedBtn.setText("Removed Axioms ("+removedAxiomSet.size()+")");
					this.refreshRemovedFrame();
					if (this.updatePlanChk.isSelected()) {
						this.genPlan();
					}
					this.sortAndDisplayAxioms(CURR_METRIC);
					this.refreshPlan();
					planTabs.setSelectedIndex(0);					
				}
				else				
				if (hLink.startsWith(":BLOCK")) {
					if (DEBUG) logFile += NEWLINE + "KEPT Axiom "+swoopModel.getTimeStamp();
					// toggle axiom blocked status (i.e. if blocked, it cannot be part of the solution)
					String hcode = hLink.substring(hLink.lastIndexOf(":")+1, hLink.length());
					OWLObject axiom = (OWLObject) hcodeAxiomMap.get(hcode);
					
					if (!keptAxiomSet.contains(axiom)) {
						keptAxiomSet.add(axiom);
						// also remove axiom from forced set and soln set
						Set mainSolnSet = planSolnAxioms[0];
						if (removedAxiomSet.contains(axiom) || mainSolnSet.contains(axiom)) {
							removedAxiomSet.remove(axiom);
							mainSolnSet.remove(axiom);							
						}
					}
					else keptAxiomSet.remove(axiom);
					
					keptBtn.setText("Kept Axioms ("+keptAxiomSet.size()+")");
					this.refreshKeptFrame();
					if (this.updatePlanChk.isSelected()) {
						this.genPlan();
					}
					this.sortAndDisplayAxioms(CURR_METRIC);
					this.refreshPlan();
					planTabs.setSelectedIndex(0);					
				}
				else				
				if (hLink.startsWith(":REMOVE")) {
					if (DEBUG) logFile += NEWLINE + "DISCARDED Axiom "+swoopModel.getTimeStamp();
					// remove axiom from soln set
					String hcode = hLink.substring(hLink.lastIndexOf(":")+1, hLink.length());
					OWLObject remAxiom = (OWLObject) hcodeAxiomMap.get(hcode);
					int num = 0;
					String title = planTabs.getTitleAt(planTabs.getSelectedIndex());
					if (title.indexOf("Main")==-1) num = Integer.parseInt(title.substring(title.lastIndexOf(" ")+1), title.length())-1;
					Set soln = planSolnAxioms[num];
					soln.remove(remAxiom);
//					manualAxiomSet.remove(axiom); ??
					if (planTabs.getSelectedIndex()!=0) {
						JEditorPane copyPane = (JEditorPane) planTabs.getSelectedComponent();
						this.setCopyPaneText(copyPane, soln);
					}
					else {
						this.refreshPlan();
					}
				}			
				else				
				if (hLink.equals(":ARITY")) {
					if (DEBUG) logFile += NEWLINE + "Sorting by ARITY "+swoopModel.getTimeStamp();
					// sort axioms by arity
					CURR_METRIC = ARITY;
					this.sortAndDisplayAxioms(ARITY);
				}
				else if (hLink.equals(":IMPACT")) {
					if (DEBUG) logFile += NEWLINE + "Sorting by IMPACT "+swoopModel.getTimeStamp();
					// sort axioms by impact
					CURR_METRIC = IMPACT;
					this.sortAndDisplayAxioms(IMPACT);
				}
				else if (hLink.equals(":USAGE")) {
					if (DEBUG) logFile += NEWLINE + "Sorting by USAGE "+swoopModel.getTimeStamp();
					// sort axioms by usage
					CURR_METRIC = USAGE;
					this.sortAndDisplayAxioms(USAGE);
				}
				else if (hLink.equals(":RANK")) {
					if (DEBUG) logFile += NEWLINE + "Sorting by RANK "+swoopModel.getTimeStamp();
					// sort axioms by rank
					CURR_METRIC = RANK;
					this.sortAndDisplayAxioms(RANK);
				}
				else if (hLink.startsWith(":REWRITE")) {
					String hashCode = hLink.substring(hLink.lastIndexOf(":")+1, hLink.length());
					OWLObject axiom = (OWLObject) hcodeAxiomMap.get(hashCode);
					if (DEBUG) {
						if (rewriteAxiomSet.contains(axiom)) logFile += NEWLINE + "Chose to UNDO REWRITE "+swoopModel.getTimeStamp();
						else logFile += NEWLINE + "Chose to REWRITE "+swoopModel.getTimeStamp();
					}
					if (rewriteAxiomSet.contains(axiom)) rewriteAxiomSet.remove(axiom);
					else rewriteAxiomSet.add(axiom);
					
					// TODO
//					if (this.updatePlanChk.isSelected()) {
//						this.genPlan();						
//					}
					
					this.refreshPlan();
				}
				else if (hLink.startsWith(":HASH")) {
					// display arity/impact in popup
					String param = hLink.substring(6, hLink.lastIndexOf(":"));
					if (DEBUG) logFile += NEWLINE + "Viewing Parameter: "+param+" "+swoopModel.getTimeStamp();
					
					String hashCode = hLink.substring(hLink.lastIndexOf(":")+1, hLink.length());
					Set disp = (HashSet) objectMap.get(hashCode);
					String html = "<html><body>";
					int ctr = 1;
					for (Iterator iter = disp.iterator(); iter.hasNext();) {
						OWLObject obj = (OWLObject) iter.next();
						swoopModel.repairColor = true;
				    	swoopModel.repairSet = new HashSet(currUnsat);
						html += insFont() + String.valueOf(ctr++) + ".&nbsp;"+this.getOWLObjectHTML(obj);
						swoopModel.repairColor = false;
						swoopModel.repairSet = new HashSet();
						// also add why? link if present
						if (whyMap.containsKey(obj)) {
							this.hcodeAxiomMap.put(String.valueOf(obj.hashCode()), obj);
							html += "&nbsp;&nbsp;(<a href=\":WHY:"+obj.hashCode()+"\">Why?</a>)";
						}
						html += "<br>";
					}
					displayPopup(html, param+" Details");
				}
				else if (hLink.startsWith(":WHY")) {
					// display explanation for impact related to unsat. classes
					try {
						String param = hLink.substring(hLink.lastIndexOf(":")+1, hLink.length());
						if (DEBUG) logFile += NEWLINE + "Viewing Entailment Explanation: "+param+" "+swoopModel.getTimeStamp();
						OWLObject entailment = (OWLObject) this.hcodeAxiomMap.get(param);
						Set expl = (HashSet) whyMap.get(entailment);
						String html = "<html><body>";
						html += "<font face=\"Verdana\" size=3><b>Explanation for "+ this.getOWLObjectHTML(entailment) +"</b><hr>";
						// get indented explanation
						ConciseFormatEntityRenderer cfRend = new ConciseFormatEntityRenderer();
				    	cfRend.setSwoopModel(swoopModel);
				    	cfRend.visitor = cfRend.createVisitor();
						StringWriter st = new StringWriter();
						PrintWriter buffer = new PrintWriter(st);
				    	cfRend.setWriter(buffer);
				    	OWLClass thing = ontology.getOWLDataFactory().getOWLThing();
				    	OWLObjectContainer cont = new OWLObjectContainer(thing);
				    	cont.visit(entailment);
				    	OWLClass any = null;
				    	if (cont.getLHS().size()>0) any = (OWLClass) cont.getLHS().iterator().next();
				    	else any = (OWLClass) cont.getRHS().iterator().next();
				    	if (any==null) any = thing;
				    	cfRend.printRepairSOS(expl, any, this);
						String sosStr = st.toString().replaceAll("problem", "entailment");
						html += "<font face=\"Verdana\" size=3>" + sosStr;
						displayPopup(html, "Why?", 600, 200);
					}
					catch (OWLException ex) {
						ex.printStackTrace();
					}
				}
//			}
		}
	}
	
	private void refreshKeptFrame() {
		// print forced axioms first
		String planStr = "<html><head></head><body>";
		if (!keptAxiomSet.isEmpty()) {
			planStr += insFont(); //+"<b>Axioms to be KEPT in Ontology</b><br>";
			for (Iterator iter = keptAxiomSet.iterator(); iter.hasNext();) {
				OWLObject axiom = (OWLObject) iter.next();
				hcodeAxiomMap.put(String.valueOf(axiom.hashCode()), axiom);
				planStr += "<font color=\"green\">[<a href=\":BLOCK:"+axiom.hashCode()+"\">Undo Keep</a>]</font>&nbsp;";
				planStr += this.getOWLObjectHTMLWithAttribs(axiom) + "<br>";
			}
			planStr += "<br>";
		}
		planStr += "</body></html>";
		keptPane.setText(planStr);
	}
	
	private void refreshRemovedFrame() {
		// print blocked axioms
		String planStr = "<html><head></head><body>";
		if (!removedAxiomSet.isEmpty()) {
			planStr += insFont(); //+"<b>Axioms to be REMOVED from Ontology</b><br>";
			for (Iterator iter = removedAxiomSet.iterator(); iter.hasNext();) {
				OWLObject axiom = (OWLObject) iter.next();
				hcodeAxiomMap.put(String.valueOf(axiom.hashCode()), axiom);
				planStr += "<font color=\"red\">[<a href=\":FORCE:"+axiom.hashCode()+"\">Undo Remove</a>]</font>&nbsp;";
				planStr += this.getOWLObjectHTMLWithAttribs(axiom) + "<br>";
			}
			planStr += "<br>";
		}
		planStr += "</body></html>";
		removedPane.setText(planStr);
	}
	
	private void displayPopup(String html, String title) {
		this.displayPopup(html, title, 400, 300);
	}
	
	private void displayPopup(String html, String title, int width, int height) {
		JFrame popup = new JFrame();
		JEditorPane pane = new JEditorPane();
		pane.setContentType("text/html");
		pane.setEditable(false);
		pane.addHyperlinkListener(this);
		pane.setText(html);
		pane.setCaretPosition(0);
		popup.getContentPane().add(new JScrollPane(pane));
		popup.setSize(width, height);
		popup.setLocation(100, 100);
		popup.setTitle(title);
		popup.show();
	}
	
	private void setCopyPaneText(JEditorPane copyPane, Set soln) {
		String html = "<html><body>";
		for (Iterator iter = soln.iterator(); iter.hasNext();) {
			OWLObject axiom = (OWLObject) iter.next();
			hcodeAxiomMap.put(String.valueOf(axiom.hashCode()), axiom);
			html += insFont()+"<font color=\"blue\">[<a href=\":REMOVE:"+axiom.hashCode()+"\">X</a>]</font>&nbsp;";
			String rem = "Remove";
			if (removedAxiomSet.contains(axiom)) rem = "Undo-Remove";
			html += "<font color=\"red\">[<a href=\":FORCE:"+axiom.hashCode()+"\">"+rem+"</a>]</font>&nbsp;";
			html += "<font color=\"green\">[<a href=\":BLOCK:"+axiom.hashCode()+"\">Keep</a>]</font>&nbsp;";
			if (removedAxiomSet.contains(axiom)) html += "<font color = \"red\">";
			html += this.getOWLObjectHTMLWithAttribs(axiom) + "<br>";
			if (removedAxiomSet.contains(axiom)) html += "</font>"; 
		}
		html += "</body></html>";
		copyPane.setText(html);
	}
	
	/*
	 * sort axioms in table based on specified parameter
	 */
	private void sortAndDisplayAxioms(int param) {
		
		int numAxioms = currAxioms.size();
		OWLObject[] sorted = new OWLObject[numAxioms];
		HashMap sourceMap = null;
		boolean increase = false;
		switch (param) {
			case ARITY:
				sourceMap = axiomUnsatClaMap;
				increase = false;
				break;
			case IMPACT:
				increase = true;
				sourceMap = axiomSOSMap;
				break;
			case USAGE:
				increase = true;
				sourceMap = axiomUsageMap;
				break;
			case RANK:
				increase = true;
				sourceMap = axiomRanksMap;
		}
		
		// sort based on sourceMap selected
		for (Iterator iter = currAxioms.iterator(); iter.hasNext();) {
			OWLObject axiom = (OWLObject) iter.next();
			double metric = 0;
			if (sourceMap.containsKey(axiom)) {
				if (!sourceMap.equals(axiomRanksMap)) metric = ((HashSet) sourceMap.get(axiom)).size();
				else {
					String m = sourceMap.get(axiom).toString();
					metric = Double.parseDouble(m);
				}
			}
			for (int ctr=0; ctr<numAxioms; ctr++) {
				OWLObject ax = sorted[ctr];
				if (ax!=null) {
					double metric2 = 0;
					if (sourceMap.containsKey(ax)) {
						if (!sourceMap.equals(axiomRanksMap)) metric2 = ((HashSet) sourceMap.get(ax)).size();
						else {
							metric2 = Double.parseDouble(sourceMap.get(ax).toString());
						}
					}
					if ((increase && metric<metric2) || (!increase && metric>metric2))  {
						insert(sorted, axiom, ctr);
						break;
					}
				}
				else {
					insert(sorted, axiom, ctr);
					break;
				}
			}
		}
		
		// refresh repair table after sorting
		List order = new ArrayList();
		for (int ctr=0; ctr<numAxioms; ctr++) order.add(sorted[ctr]);
		this.refreshTable(order);
	}
	
	private void insert(Object[] array, Object obj, int pos) {
		int len = array.length;
//		System.out.println(array+" "+obj+" "+pos);
		for (int i=len-2; i>=pos; i--) {
			array[i+1] = array[i];
		}
		array[pos] = obj;
	}
	
	/*
	 * for each axiom, compute its relevance to the ontology based on usage
	 */
	private void computeUsage(OWLObject axiom) {
		try {
			OWLClass thing = ontology.getOWLDataFactory().getOWLThing();
			OWLObjectContainer owlCont = new OWLObjectContainer(thing);
			owlCont.visit(axiom);
			List axiomElements = new ArrayList();
			axiomElements.addAll(owlCont.getLHS());
			axiomElements.addAll(owlCont.getRHS());
			// get usage for each entity in axiom-elements
			Set usage = new HashSet();
			for (Iterator iter2=axiomElements.iterator(); iter2.hasNext();) {
				Object obj = iter2.next();
				if (obj instanceof OWLEntity) {
					usage.addAll(OntologyHelper.entityUsage(ontology, (OWLEntity) obj));
				}
			}
			for (Iterator iter2 = new HashSet(usage).iterator(); iter2.hasNext();) {
				OWLObject obj = (OWLObject) iter2.next();
				if (!(obj instanceof OWLEntity)) usage.remove(obj);
			}
			if (!turnOffUsage) axiomUsageMap.put(axiom, usage);		
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private String getOWLObjectHTMLWithAttribs(OWLObject axiom) {
		String html = "";
		html += this.getOWLObjectHTML(axiom) + "&nbsp; (";
		int arity = -1;
		if (axiomUnsatClaMap.containsKey(axiom)) arity = ((HashSet) this.axiomUnsatClaMap.get(axiom)).size();
		if (arity!=-1) {
			String hash = String.valueOf(this.axiomUnsatClaMap.get(axiom).hashCode());
			html += "Arity:<a href=\":HASH:Arity:"+hash+"\">"+String.valueOf(arity) + "</a>";
			objectMap.put(hash, (HashSet) this.axiomUnsatClaMap.get(axiom));
		}
		int impact = -1;
		Set impactSet = new HashSet();
		if (axiomSOSMap.containsKey(axiom)) impactSet = (HashSet) this.axiomSOSMap.get(axiom);
		impact = impactSet.size();
		if (impact!=-1) {
			String hash = String.valueOf(impactSet.hashCode());
			objectMap.put(hash, impactSet);
			html += "&nbsp; Impact: <a href=\":HASH:Impact:"+hash+"\">" + String.valueOf(impact)+"</a>";
		}
		int usage = -1;
		if (axiomUsageMap.containsKey(axiom)) usage = ((HashSet) this.axiomUsageMap.get(axiom)).size();
		if (usage!=-1) {
			String hash = String.valueOf(this.axiomUsageMap.get(axiom).hashCode());
			objectMap.put(hash, this.axiomUsageMap.get(axiom));
			html += "&nbsp; Usage: <a href=\":HASH:Usage:"+hash+"\">" + String.valueOf(usage)+"</a>";
		}
		html += ")";
		return html;
	}
	
	public void refreshPlan() {
		refreshPlan(false);
		int pos = indentPane.getCaretPosition();
		this.refreshIndentPane();
		try {
			indentPane.setCaretPosition(pos);
		}
		catch (Exception ex) {
			indentPane.setCaretPosition(0);
		}		
	}
	
	// refresh plan
	// plan axioms are present in planSolnAxioms[0]
	private void refreshPlan(boolean genPlans) {
		
		String planStr = "<html><body>"+insFont();
		
		// print solution axioms
		Set mainSolnSet = planSolnAxioms[0];
		if (!mainSolnSet.isEmpty()) {
//			planStr += "<b><u>GENERATED REPAIR PLAN:" + " to "+ granularityCombo.getSelectedItem().toString()+"</u></b><br>";
			for (Iterator iter = mainSolnSet.iterator(); iter.hasNext();) {
				OWLObject axiom = (OWLObject) iter.next();
				hcodeAxiomMap.put(String.valueOf(axiom.hashCode()), axiom);
				planStr += insFont()+"<font color=\"blue\">[<a href=\":REMOVE:"+axiom.hashCode()+"\">X</a>]</font>&nbsp;";
				String rem = "Remove";
				if (removedAxiomSet.contains(axiom)) rem = "Undo-Remove";
				planStr += "<font color=\"red\">[<a href=\":FORCE:"+axiom.hashCode()+"\">"+rem+"</a>]</font>&nbsp;";
				planStr += "<font color=\"green\">[<a href=\":BLOCK:"+axiom.hashCode()+"\">Keep</a>]</font>&nbsp;";
				if (removedAxiomSet.contains(axiom)) planStr += "<font color = \"red\">";
				planStr += this.getOWLObjectHTMLWithAttribs(axiom);
				if (removedAxiomSet.contains(axiom)) planStr += "</font>";
				
				// suggestions for rewrites?
				if (rewriteEnabled) planStr += suggestRewrites(axiom);
				planStr += "<br>";				
			}
			// also add preview information directly
			planStr += "<br>"+previewEffect(true);			
		}
		else if (genPlans) {
			planStr += "<br><b>No Plan Found.</b> Try changing one or more of the KEPT or REMOVED axioms.<br>";
		}
		
		planStr += "</body></html>";
		planPane.setText(planStr);
		planPane.setCaretPosition(0);
	}

	private String suggestRewrites(OWLObject axiom) {
		String html = "";
		try {
			if (axiom instanceof OWLEquivalentClassesAxiom) {
				// weaken equivalent to subclass
				OWLEquivalentClassesAxiom equ = (OWLEquivalentClassesAxiom) axiom;
				OWLClass cla = null;
				for (Iterator iter = equ.getEquivalentClasses().iterator(); iter.hasNext();) {
					OWLDescription desc = (OWLDescription) iter.next();
					if (desc instanceof OWLClass) {
						cla = (OWLClass) desc;
						break;
					}
				}
				Set copy = new HashSet(equ.getEquivalentClasses());
				copy.remove(cla);
				for (Iterator iter = copy.iterator(); iter.hasNext();) {
					OWLDescription desc = (OWLDescription) iter.next();
					OWLSubClassAxiom sub = ontology.getOWLDataFactory().getOWLSubClassAxiom(cla, desc);
					hcodeAxiomMap.put(String.valueOf(sub.hashCode()), sub);
					String rewrite = "Undo Rewrite";
//					rewriteAxiomSet.add(sub);
					if (!rewriteAxiomSet.contains(sub)) rewrite = "Rewrite?";
					html += "<br>"+"<font face=\"Verdana\" size=2>&nbsp;|_[<a href=\":REWRITE:"+sub.hashCode()+"\">" + rewrite + "</a>]&nbsp;"+this.getOWLObjectHTML(sub)+"</font>";
				}
			}
			else if (axiom instanceof OWLPropertyDomainAxiom) {
				// rewrite domain into local property restriction
				OWLPropertyDomainAxiom opd = (OWLPropertyDomainAxiom) axiom;
				OWLProperty prop = opd.getProperty();
				OWLDescription dom = opd.getDomain();
				OWLDescription all = null;
				if (prop instanceof OWLObjectProperty) {
					OWLObject ran = ontology.getOWLDataFactory().getOWLThing();
					if (prop.getRanges(ontology).size()>0) ran = (OWLObject) prop.getRanges(ontology).iterator().next();
					all = ontology.getOWLDataFactory().getOWLObjectAllRestriction((OWLObjectProperty) prop, (OWLDescription) ran);
				}
				else {
					OWLObject ran = ontology.getOWLDataFactory().getOWLConcreteDataType(new URI("http://www.w3.org/2001/XMLSchema#string"));
					if (prop.getRanges(ontology).size()>0) ran = (OWLObject) prop.getRanges(ontology).iterator().next();
					all = ontology.getOWLDataFactory().getOWLDataAllRestriction((OWLDataProperty) prop, (OWLDataType) ran);
				}
				OWLSubClassAxiom sub = ontology.getOWLDataFactory().getOWLSubClassAxiom(dom, all);
				hcodeAxiomMap.put(String.valueOf(sub.hashCode()), sub);
				String rewrite = "Undo Rewrite";
//				rewriteAxiomSet.add(sub);
				if (!rewriteAxiomSet.contains(sub)) rewrite = "Rewrite?";
				html += "<br>"+"<font face=\"Verdana\" size=2>&nbsp;|_[<a href=\":REWRITE:"+sub.hashCode()+"\">" + rewrite + "</a>]&nbsp;"+this.getOWLObjectHTML(sub)+"</font>";
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		return html;
	}
	
	public void valueChanged(ListSelectionEvent e) {
		
		if (e.getSource() == classList) {
			refreshClaListSelection();
		}
	}

	/*
	 * refresh table based on class list selection
	 * either display union or intersection of MUPS
	 */
	private void refreshClaListSelection() {
		if (classList.getSelectedValue()!=null) {
			// init the axiom set that is to be displayed
			// based on selected classes and union/intersection 
			Set displayAxioms = new HashSet();
			for (int i = 0; i < classList.getSelectedValues().length; i++) {
				OWLClass cla = (OWLClass) classList.getSelectedValues()[i];
				if (!claMUPSMap.containsKey(cla)) this.findMUPS(cla);
				Set mups = new HashSet((Set) claMUPSMap.get(cla));
				if (this.displayMUPSUnion) displayAxioms.addAll(mups);
				else {
					// display intersection
					if (displayAxioms.isEmpty()) displayAxioms = mups;
					else displayAxioms.retainAll(mups);
				}
			}
			this.refreshTable(new ArrayList(displayAxioms));
			this.sortAndDisplayAxioms(CURR_METRIC);
			int pos = indentPane.getCaretPosition();
			this.refreshIndentPane();
			try {
				indentPane.setCaretPosition(pos);
			}
			catch (Exception ex) {
				indentPane.setCaretPosition(0);
			}									
		}
		else this.refreshTable(new ArrayList());
	}
	
	/*
	 * display indented sos for selected classes
	 */
	private void refreshIndentPane() {
		
		try {
			ConciseFormatEntityRenderer cfRend = new ConciseFormatEntityRenderer();
	    	cfRend.setSwoopModel(swoopModel);
	    	cfRend.visitor = cfRend.createVisitor();
	    	
	    	Set claSet = new HashSet();
	    	for (int i = 0; i < classList.getSelectedValues().length; i++) {
	    		claSet.add(classList.getSelectedValues()[i]);
	    	}
	    	
	    	List claList = new ArrayList();
	    	while (claSet.size()>0) {
	    		// take any one class out of set
	    		OWLClass cla = (OWLClass) claSet.iterator().next();
	    		claList.add(cla);
	    		claSet.remove(cla);
	    		
	    		// find all other classes that share atleast one axiom
	    		if (!claMUPSMap.containsKey(cla)) this.findMUPS(cla);
				Set mups = new HashSet((Set) claMUPSMap.get(cla));
				for (Iterator iter = mups.iterator(); iter.hasNext();) {
					OWLObject axiom = (OWLObject) iter.next();
					for (Iterator iter2 = ((Set) axiomUnsatClaMap.get(axiom)).iterator(); iter2.hasNext(); ) {
						OWLClass sharedCla = (OWLClass) iter2.next();
						if (claSet.contains(sharedCla)) {
							claList.add(sharedCla);
							claSet.remove(sharedCla);
						}
					}
				}
	    	}
	    	
	    	String html = "<html><body>";
	    	for (int i = 0; i < claList.size(); i++) {
				OWLClass cla = (OWLClass) claList.get(i);
				Set mups = new HashSet((Set) claMUPSMap.get(cla));
				StringWriter st = new StringWriter();
				PrintWriter buffer = new PrintWriter(st);
		    	cfRend.setWriter(buffer);
		    	cfRend.printRepairSOS(mups, cla, false, this);
				String sosStr = st.toString();
				sosStr = sosStr.replaceAll("problem:", "problem:&nbsp;"+swoopModel.shortForm(cla.getURI()));
				html += "<font face=\"Verdana\" size=2>"+ sosStr + "<br>";
			}
			html += "</body></html>";
	//		System.out.println(html);
			indentPane.setText(html);			
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/*
	 * write logFile for user study
	 */
	private void writeLogFile() {
		try {
			String fname = "REPAIR-LOG_"+String.valueOf(logCtr)+".txt";
			FileWriter fw = new FileWriter(new File(fname));
			fw.write(logFile);
			fw.close();
			System.out.println("Wrote Repair Log File: "+fname);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	public void genPlan() {
		List plans = this.generatePlans();
		if (plans.size()>0) {
			Set mainSolnSet = new HashSet();
			mainSolnSet.addAll((List) plans.get(plans.size()-1)); // *** last plan is most optimum ***
			planSolnAxioms[0] = mainSolnSet;				
		}
		this.refreshPlan(true);
	}
	
	public void actionPerformed(ActionEvent e) {
		
		if (e.getSource() == generateBtn) {
			genPlan();			
		}
		else if (e.getSource() == rankBtn) {
			if (DEBUG) logFile += NEWLINE + "Recompute RANKS (WA: "+this.w_arityFld.getText()+" WI: "+this.w_impactFld.getText()+" WU: "+this.w_usageFld.getText()+" "+swoopModel.getTimeStamp();
			this.refreshRanks();
		}
		else if (e.getSource() == execBtn) {
			if (DEBUG) logFile += NEWLINE + "EXECUTE Plan "+swoopModel.getTimeStamp();
			executePlan();
			// ****** write log file here *******
			if (DEBUG) this.writeLogFile();
		}
		else if (e.getSource() == clearBtn) {
			if (DEBUG) logFile += NEWLINE + "CLEAR Plan "+swoopModel.getTimeStamp();
			planSolnAxioms[0] = new HashSet();
			this.refreshPlan();
		}
		else if (e.getSource() == saveBtn) {
			// save plan in a new tab
			if (DEBUG) logFile += NEWLINE + "SAVE Plan "+swoopModel.getTimeStamp();
			JEditorPane copyPane = new JEditorPane();
			copyPane.setEditable(false);
			copyPane.setContentType("text/html");
			copyPane.addHyperlinkListener(this);
			planCtr = planTabs.getTabCount()+1;
			Set copySoln = new HashSet(planSolnAxioms[0]);
			planSolnAxioms[planCtr-1] = copySoln;
			this.setCopyPaneText(copyPane, copySoln);
			planTabs.add("Saved Plan "+String.valueOf(planCtr), copyPane);			
		}
		else if (e.getSource() == previewBtn) {
			if (DEBUG) logFile += NEWLINE + "PREVIEW Plan "+swoopModel.getTimeStamp();
			this.previewEffect(false); 
		}
		else if (e.getSource() == rewriteChk) {
			this.rewriteEnabled = rewriteChk.isSelected();
			if (planSolnAxioms[0].size()>0 && this.updatePlanChk.isSelected()) {
				this.genPlan();
			}
		}
		else if (e.getSource() == viewGlobalChk) {
			this.viewGlobal = viewGlobalChk.isSelected();
//			indentPane.setVisible(!viewGlobal);
			if (viewGlobal) axiomTablePane.setDividerLocation(1000);
			else {
				axiomTablePane.setDividerLocation(0);
				this.refreshIndentPane();
				indentPane.setCaretPosition(0);
			}
		}
		else if (e.getSource() == keptBtn) {
			keptFrame.setVisible(true);
		}
		else if (e.getSource() == removedBtn) {
			removedFrame.setVisible(true);
		}
		else if (e.getSource() == impactUnsatChk) {
			this.enableImpactUnsat = impactUnsatChk.isSelected();
			this.axiomSOSMap.clear();
			this.rankBtn.doClick();
		}
	}
	
	/*
	 * preview effect of executing current repair plan
	 */
	private String previewEffect(boolean concise) {
		
		String html = "";
		int num = 0;
		String title = planTabs.getTitleAt(planTabs.getSelectedIndex());
		if (title.indexOf("Main")==-1) num = Integer.parseInt(title.substring(title.lastIndexOf(" ")+1), title.length())-1;
		Set soln = planSolnAxioms[num];
		OWLOntology copyOnt = swoopModel.cloneOntology(ontology);
		try {
			OWLOntBuilder ob = new OWLOntBuilder(copyOnt);
			ob.addAxiom = false;
			for (Iterator iter = soln.iterator(); iter.hasNext();) {
				OWLObject axiom = (OWLObject) iter.next();
				axiom.accept(ob);
			}
			// also consider rewrites!
			ob.addAxiom = true;
			for (Iterator iter = rewriteAxiomSet.iterator(); iter.hasNext();) {
				OWLObject axiom = (OWLObject) iter.next();
				axiom.accept(ob);
			}
			copyOnt = ob.currentOnt;
			PelletReasoner newPellet = new PelletReasoner();
			newPellet.setOntology(copyOnt);
			Set copyUnsat = new HashSet(currUnsat);
			Set newUnsat = newPellet.equivalentClassesOf(copyOnt.getOWLDataFactory().getOWLNothing());
			copyUnsat.removeAll(newUnsat);
			
			if (!concise) 
				html = "<head><body>"+insFont();
			else 
				html += "PREVIEW:<br>";
			html += "<b>Unsatisfiable</b> ";
			if (!concise) html += "<br>";
			html += "Fixed:"+String.valueOf(copyUnsat.size())+" ";
			if (!concise) {
				for (Iterator iter = copyUnsat.iterator(); iter.hasNext();) {
					OWLClass cla = (OWLClass) iter.next();
					html += "&nbsp;<a href=\""+cla.getURI()+"\">"+swoopModel.shortForm(cla.getURI())+"</a>&nbsp;";
				}
				html += "<br><br>";
			}
			html += "Remaining:" + String.valueOf(newUnsat.size())+" ";
			if (!concise) {
				for (Iterator iter = newUnsat.iterator(); iter.hasNext();) {
					OWLClass cla = (OWLClass) iter.next();
					html += "&nbsp;<a href=\""+cla.getURI()+"\">"+swoopModel.shortForm(cla.getURI())+"</a>";
				}
			}			
//			JOptionPane.showMessageDialog(this, "Unsatisfiable Concepts Fixed: "+new JScrollPane(list1)+" Remaining: "+newUnsat.size(), "Preview Solution", JOptionPane.INFORMATION_MESSAGE);
			
			//***************************************************
			// also preview cumulative impact
			Set entail = new HashSet();
			for (Iterator iter = planSolnAxioms[0].iterator(); iter.hasNext();) {
				OWLObject ax = (OWLObject) iter.next();
				if (axiomSOSMap.containsKey(ax)) {
					Set sos = (HashSet) axiomSOSMap.get(ax);
					entail.addAll(sos);
				}
			}
			
			// now compute lost entailments
			Set lost = new HashSet();
			EntailmentChecker chk = new EntailmentChecker(newPellet, copyOnt);
			for (Iterator iter = entail.iterator(); iter.hasNext();) {
				OWLObject ent = (OWLObject) iter.next();
				if (!chk.isEntailed(ent)) {
					lost.add(ent);
				}
			}
			entail.removeAll(lost);
			
			// also compute special case entailments retained
			// check equivalence between unsatisfiable concepts just turned satisfiable
			OWLClass[] list  = new OWLClass[copyUnsat.size()];
			int ctr = -1;
			for (Iterator iter = copyUnsat.iterator(); iter.hasNext();) {
				list[++ctr] = (OWLClass) iter.next();
			}
			for (int i=0; i<ctr-1; i++) {
				for (int j = i+1; j<ctr; j++) {
					OWLClass cla1 = list[i];
					OWLClass cla2 = list[j];
					Set s = new HashSet();
					s.add(cla1);
					s.add(cla2);
					OWLClassAxiom ax = ontology.getOWLDataFactory().getOWLEquivalentClassesAxiom(s);
					if (chk.isEntailed(ax)) {
						entail.add(ax);
					}
//					ax = ontology.getOWLDataFactory().getOWLSubClassAxiom(cla1, cla2);
//					if (chk.isEntailed(ax)) {
//						entail.add(ax);
//					}
//					ax = ontology.getOWLDataFactory().getOWLSubClassAxiom(cla2, cla1);
//					if (chk.isEntailed(ax)) {
//						entail.add(ax);
//					}
				}
			}
			// also check retaining of extended impact
			for (Iterator iter = axiomSOSMap.keySet().iterator(); iter.hasNext();) {
				OWLObject ax = (OWLObject) iter.next();
				for (Iterator iter2= ((HashSet) axiomSOSMap.get(ax)).iterator(); iter2.hasNext();) {
					OWLObject ent = (OWLObject) iter2.next();
					if (chk.isEntailed(ent)) {
						entail.add(ent);
					}
				}
			}
			
			// finally print
			if (!concise) html += "<hr>"+insFont();
			else html += "<br>";
			html += "<b>Entailments</b>";
			if (concise) {
				html += " Lost: "+String.valueOf(lost.size())+" Retained: "+String.valueOf(entail.size());
			}
			else {
				swoopModel.repairColor = true;
		    	swoopModel.repairSet = new HashSet(currUnsat);
		    	html += "<br>Lost:";
		    	for (Iterator iter = lost.iterator(); iter.hasNext();) {
					OWLObject ax = (OWLObject) iter.next();
					html += "<br>"+getOWLObjectHTML(ax);
					if (whyMap.containsKey(ax)) {
						this.hcodeAxiomMap.put(String.valueOf(ax.hashCode()), ax);
						html += "&nbsp;&nbsp;(<a href=\":WHY:"+ax.hashCode()+"\">Why?</a>)";
					}
				}
				html += "<br><br>Retained:";
				for (Iterator iter = entail.iterator(); iter.hasNext();) {
					OWLObject ax = (OWLObject) iter.next();
					html += "<br>"+getOWLObjectHTML(ax);
					if (whyMap.containsKey(ax)) {
						this.hcodeAxiomMap.put(String.valueOf(ax.hashCode()), ax);
						html += "&nbsp;&nbsp;(<a href=\":WHY:"+ax.hashCode()+"\">Why?</a>)";
					}
				}
				swoopModel.repairColor = false;
		    	swoopModel.repairSet = new HashSet();
			}
			
			if (!concise) this.displayPopup(html, "Preview Effect of Repair Solution", 600, 400);
		}
		catch (OWLException ex) {
			ex.printStackTrace();
		}
		return html;
	}
	
	/*
	 * execute current repair plan
	 */
	private void executePlan() {
		try {
			// execute / apply current solution
			int num = 0;
			String title = planTabs.getTitleAt(planTabs.getSelectedIndex());
			if (title.indexOf("Main")==-1) num = Integer.parseInt(title.substring(title.lastIndexOf(" ")+1), title.length())-1;
			Set soln = new HashSet(planSolnAxioms[num]);
			OWLOntBuilder ob = new OWLOntBuilder(ontology);
			ob.addAxiom = false;
			for (Iterator iter = soln.iterator(); iter.hasNext();) {
				OWLObject axiom = (OWLObject) iter.next();
				axiom.accept(ob);
				// also remove axiom from forced axiom set or blocked axiom set
				this.removedAxiomSet.remove(axiom);
				this.keptAxiomSet.remove(axiom);
			}
			// add rewritten axioms
			ob.addAxiom = true;
			for (Iterator iter = rewriteAxiomSet.iterator(); iter.hasNext();) {
				OWLObject axiom = (OWLObject) iter.next();
				axiom.accept(ob);
			}
			List changes = new ArrayList(ob.changes);
			swoopModel.addCommittedChanges(changes);
			ontology = ob.currentOnt;
			pellet.setOntology(ontology);
			swoopModel.clearCaches(ontology); // *** KEY CHANGE ***
			this.launch();
			this.repaint();
			if (this.updatePlanChk.isSelected()) {
				this.genPlan();
				this.refreshPlan();
			}
		}
		catch (OWLException ex) {
			ex.printStackTrace();
		}
	}
	
	/*
	 * recompute ranks when user changes weights of parameters
	 */
	private void refreshRanks() {
		double ari = Double.parseDouble(w_arityFld.getText());
		double imp = Double.parseDouble(w_impactFld.getText());
		double usa = Double.parseDouble(w_usageFld.getText());
		this.setWeights(ari, imp, usa);
		this.computeRanks(axiomUnsatClaMap.keySet());
		this.sortAndDisplayAxioms(CURR_METRIC);
		this.refreshIndentPane();
	}
	
	/*
	 * Load locally saved hashmap for SOS 
	 */
	private void loadImpactSOSFile() {
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream("axiomSOSMap"));
			axiomSOSMap = (HashMap) in.readObject();
			in.close();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/*
	 * Save axiomSOSMap to disk 
	 */
	private void saveImpactSOSFile(HashMap axiomSOSMap) {
		try {
			System.out.println("Saving file axiomSOSMap");
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("axiomSOSMap"));
			out.writeObject(axiomSOSMap);
			out.close();
			System.out.println("SOS computed and stored");
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/*
	 * Compute SOS for all entailments..store in hashMap (and save locally?)
	 */
	private void computeImpactSOS() {
		try {
			System.out.println("Computing SOS for each relation..");
			axiomSOSMap = new HashMap();
			for (Iterator iter = ontology.getClasses().iterator(); iter.hasNext();) {
				OWLClass cla = (OWLClass) iter.next();
				if (!pellet.isConsistent(cla)) continue; 
				
				// get SOS for each superclass
				Set sup = pellet.superClassesOf(cla);
				System.out.println("For class: "+cla);
				for (Iterator iter2 = SetUtils.union(sup).iterator(); iter2.hasNext();) {
					OWLClass supCla = (OWLClass) iter2.next();
					System.out.println("For superclass: "+supCla);
					OWLDescription notSupCla = ontology.getOWLDataFactory().getOWLNot(supCla);
					Set andSet = new HashSet();
					andSet.add(cla);
					andSet.add(notSupCla);
					OWLDescription desc = ontology.getOWLDataFactory().getOWLAnd(andSet);
					OWLSubClassAxiom ax = ontology.getOWLDataFactory().getOWLSubClassAxiom(cla, supCla);
					this.findMUPS(desc, ax);
				}
				// get SOS for each disjoint
				Set disj = pellet.disjointClassesOf(cla);
				for (Iterator iter2 = SetUtils.union(disj).iterator(); iter2.hasNext();) {
					OWLClass disjCla = (OWLClass) iter2.next();
					System.out.println("For disjoint: "+disjCla);
					// get SOS for relationship
					Set andSet = new HashSet();
					andSet.add(cla);
					andSet.add(disjCla);
					OWLDescription desc = ontology.getOWLDataFactory().getOWLAnd(andSet);
					OWLDisjointClassesAxiom ax = ontology.getOWLDataFactory().getOWLDisjointClassesAxiom(andSet);
					this.findMUPS(desc, ax);
				}
			}
			// save axiomSOSMap to file
//			this.saveImpactSOSFile(axiomSOSMap);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public boolean isURI(String str) {
		try {
			new URI(str);			
		}
		catch (Exception ex) {
			return false;
		}
		return true;
	}
	
	/*
	 * TESTING PURPOSES ONLY
	 * Modify all equivalences in the ontology to subclass
	 */
//	private void modifyEquivalence() {
//		// rewrite all equivalences into one direction subclasses
//		try {
//			List changes = new ArrayList();
//			for (Iterator iter = ontology.getClasses().iterator(); iter.hasNext();) {
//				OWLClass cla = (OWLClass) iter.next();
//				for (Iterator iter2 = cla.getEquivalentClasses(ontology).iterator(); iter2.hasNext();) {
//					OWLDescription equ = (OWLDescription) iter2.next();
//					if (!(equ instanceof OWLClass)) {
//						RemoveEquivalentClass rem = new RemoveEquivalentClass(ontology, cla, equ, null);
//						changes.add(rem);
//						AddSuperClass add = new AddSuperClass(ontology, cla, equ, null);
//						changes.add(add);
//					}
//				}			
//			}
//			System.out.println("***** changes to equivalence: "+changes.size());
//			for (Iterator iter = changes.iterator(); iter.hasNext();) {
//				OntologyChange oc = (OntologyChange) iter.next();
//				oc.accept((ChangeVisitor) ontology);
//			}
//			// save locally
//			CorrectedRDFRenderer rdfRend = new CorrectedRDFRenderer();
//			StringWriter st = new StringWriter();
//			rdfRend.renderOntology(ontology, null, st);
//			FileWriter fw = new FileWriter(new File("tambis-MOD.owl"));
//			fw.write(st.toString());
//			fw.close();
//		}
//		catch (Exception ex) {
//			ex.printStackTrace();
//		}
//	}
	
	private double calculateRank(double arity, double impact, double usage) {
		double rank = 0 ;
		if (weights[ARITY]!=0) rank = - (weights[ARITY]) * arity;
		rank += weights[IMPACT] * impact;
		rank += weights[USAGE] * usage;
		return rank;
	}
	
	/*
	 * Use Modified Reiter's Hitting Set to generate repair plans
	 * based on axiom ranks
	 */
	private List generatePlans() {
		
		List plans = new ArrayList();
		// also rewrite axioms set
		rewriteAxiomSet = new HashSet();
		
		try {
			if (this.granularityCombo.getSelectedIndex() == 0) {
				
				if (DEBUG) logFile += NEWLINE + "Generate Plans: ALL UNSAT. "+swoopModel.getTimeStamp();
				
				// plan to fix all unsat. classes
				List soln = new ArrayList();
				
				// backup everything
				OWLOntology copy = swoopModel.cloneOntology(ontology);
				HashMap temp1 = new HashMap(this.axiomUnsatClaMap);
				HashMap temp2 = new HashMap(this.claMUPSMap);
				Set temp3 = new HashSet(currRoots);
				Set temp4 = new HashSet(this.removedAxiomSet);
				Set temp5 = new HashSet(this.keptAxiomSet);
				
				Set rootMUPS = this.findRootsMUPS();
				
				// iteratively fix all roots
				while (!rootMUPS.isEmpty()) {
					
					// get intermediate soln for all roots
					List pl = this.solveUsingHST(rootMUPS);
					
					if (pl.size()>0) {
						// remove all axioms in intermediate soln from ontology
						OWLOntBuilder ob = new OWLOntBuilder(copy);
						ob.addAxiom = false;
						for (Iterator iter = ((List) pl.get(pl.size()-1)).iterator(); iter.hasNext();) { // *** last plan is most optimum ***
							OWLObject solAxiom = (OWLObject) iter.next();
							this.removedAxiomSet.remove(solAxiom);
							this.keptAxiomSet.remove(solAxiom);
							soln.add(solAxiom);
							solAxiom.accept(ob);							
						}
						// pass new ontology into pellet
						copy = ob.currentOnt;
						pellet.setOntology(copy);
						rootMUPS.clear();
						// temp store maps and then clear them
						this.axiomUnsatClaMap.clear();
						this.claMUPSMap.clear();
						// get new roots
						pellet.autoRootDiscovery();
						if (pellet.depFinder == null) {
							break;
						}
						
						this.currRoots = new HashSet(pellet.depFinder.rootClasses);
						this.refreshPelletDebuggerOntology(copy); // needed for MUPS
						rootMUPS = this.findRootsMUPS();
//						this.computeImpactSOS(); //EITHER USE THIS OR LINE BELOW
//						this.loadImpactSOSFile();
						this.computeRanks(this.axiomUnsatClaMap.keySet());						
					}
					else {
						// something funky
						JOptionPane.showMessageDialog(this, "ERROR: Cannot find a complete plan", "Repair Plan", JOptionPane.ERROR_MESSAGE);
						System.out.println("ERROR: Cannot find a complete plan");
						plans.add(soln);
						return plans;
					}					
				} // end while loop
				
				// restore all maps (from temp)
//				this.ontology = swoopModel.cloneOntology(copy);
				this.axiomUnsatClaMap = new HashMap(temp1);
				this.claMUPSMap = new HashMap(temp2);
				this.currRoots = new HashSet(temp3);
				this.removedAxiomSet = new HashSet(temp4);
				this.keptAxiomSet = new HashSet(temp5);
				this.refreshPelletDebuggerOntology(ontology);
				pellet.setOntology(ontology);
				plans.add(soln);				
			}
			else
			if (this.granularityCombo.getSelectedIndex() == 1) {
				// plan to fix all roots
				if (DEBUG) logFile += NEWLINE + "Generate Plans: ALL ROOTS "+swoopModel.getTimeStamp();
				Set rootMUPS = this.findRootsMUPS();
				plans = this.solveUsingHST(rootMUPS);
			}
			else 
			if (this.granularityCombo.getSelectedIndex() == 2) {
				if (DEBUG) logFile += NEWLINE + "Generate Plans: SELECTED "+swoopModel.getTimeStamp();
				// plan to fix selected classes alone
				Set mups = new HashSet();
				for (int i = 0; i<classList.getSelectedValues().length; i++) {
					mups.add(this.claMUPSMap.get(classList.getSelectedValues()[i]));
				}
				plans = this.solveUsingHST(mups);
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		return plans;
	}
	
	/*
	 * Given a set of MUPS, return all minimal hitting sets
	 * i.e. each HS contains a set of axioms that touch all mups
	 * (optimization criteria: ranks as specified in axiomRanksMap)
	 */
	private List solveUsingHST(Set mups) {
		
		List plans = new ArrayList();
		
		// remove all sets from mups that contain any of the forced axioms
		Set copyMups = new HashSet(mups);
		for (Iterator iter = removedAxiomSet.iterator(); iter.hasNext();) {
			OWLObject axiom = (OWLObject) iter.next();
			for (Iterator iter2=mups.iterator(); iter2.hasNext();) {
				Set m = (HashSet) iter2.next();
				if (m.contains(axiom)) copyMups.remove(m);
			}
		}
		mups = copyMups;
		if (mups.isEmpty()) {
			List path = new ArrayList(this.removedAxiomSet);
			plans.add(path);
			return plans;
		}
		
		// select any random mups as root of HST
		Set root = (HashSet) mups.iterator().next(); // any random MUPS
		
		// order axioms and add to stack
		List stack = new ArrayList();
		stack.addAll(orderAxioms(root));
		
		// initialize all other variables before proceeding to main loop
		List path = new ArrayList();
		double optimum = 1000; // some high value (upper bound)
		
		while (stack.size()!=0) {
			
			// always: last item on stack (i.e. tail of list) needs to be popped next
			OWLObject axiom = (OWLObject) stack.get(stack.size()-1);
			stack.remove(axiom);
			
			// when explored all options with this immediate edge from root
			// remove edge and proceed to next edge
			if (path.contains(axiom)) {
				path.remove(axiom);
				continue;
			}
			
			path.add(axiom);
			double pr = pathRank(path);
			
			// early path termination (check if it already exceeds optimum)
			if (pr >= optimum) {
				// remove from stack and backtrack
				path.remove(axiom);
				continue;
			}
			
			// special case: check for blocked axioms
			if (keptAxiomSet.contains(axiom)) {
				path.remove(axiom);
				continue;
			}
			
			// check if path is a Hitting Set (HS)
			Set left = checkPathHS(path, mups);
			if (left.isEmpty()) {
				// found new optimum path (plan)
				//TODO actually compute cumulative impact..hence new path rank
				// check if path contains manualAxioms
//				if (forceAxiomSet.isEmpty() || path.containsAll(forceAxiomSet)) {
					optimum = pr;
					path.addAll(new HashSet(this.removedAxiomSet));
					plans.add(new ArrayList(path));					
//				}
				path.remove(axiom);
			}
			else {
				// found new node to add to HST
				stack.add(axiom);
				stack.addAll(orderAxioms(left));
			}
		}
		
		return plans;
	}
	
	/*
	 * Order axioms in a MUPS such that lowest ranked axioms are at the tail of the list
	 */
	private List orderAxioms(Set mups) {
		List m = new ArrayList(mups);
		OWLObject ordered[] = new OWLObject[m.size()];
		int ord = 0;
		for (Iterator iter = m.iterator(); iter.hasNext();) ordered[ord++] = (OWLObject) iter.next();
		
		for (int i=0; i < m.size()-1; i++) {
			OWLObject a = ordered[i]; //(OWLObject) m.get(i);
			for (int j=i+1; j < m.size(); j++) {
				OWLObject b = ordered[j]; //(OWLObject) m.get(j);
				double rankA = -1;
				double rankB = -1;
				if (axiomRanksMap.containsKey(a)) rankA = Double.parseDouble(axiomRanksMap.get(a).toString());
				if (axiomRanksMap.containsKey(b)) rankB = Double.parseDouble(axiomRanksMap.get(b).toString());
				if (rankA < rankB) {
					// swap a, b in ordered
					OWLObject temp = ordered[j];
					ordered[j] = ordered[i];
					ordered[i] = temp;
				}
			}
		}
		List result = new ArrayList();
		for (int ctr = 0; ctr < ordered.length; ctr++) result.add(ordered[ctr]);
		return result;
	}
	
	/*
	 * Check if a path of axioms is a Hitting Set (HS) for a collection of MUPS
	 */
	private Set checkPathHS(List path, Set allMUPS) {
		for (Iterator iter = allMUPS.iterator(); iter.hasNext();) {
			Set mups = (HashSet) iter.next();
			boolean hit = false;
			for (Iterator iter2 = path.iterator(); iter2.hasNext();) {
				OWLObject axiom = (OWLObject) iter2.next();
				if (mups.contains(axiom)) {
					hit = true;
					break;
				}
			}
			if (!hit) return mups;
		}
		return new HashSet(); // path is a HS
	}
	
//	/*
//	 * Return the minRanked axiom from a given axiom set
//	 */
//	private OWLObject minRankedAxiom(Set axioms) {
//		double min = 100;
//		OWLObject minAxiom = null;
//		for (Iterator iter = axioms.iterator(); iter.hasNext();) {
//			OWLObject axiom = (OWLObject) iter.next();
//			if (axiomRanksMap.containsKey(axiom)) {
//				double rank = Double.parseDouble(axiomRanksMap.get(axiom).toString());
//				if (rank < min) {
//					min = rank;
//					minAxiom = axiom;
//				}
//			}
//		}
//		return minAxiom;
//	}
	
	public double pathRank(List path) {
		double pr = 0;
		for (Iterator iter = path.iterator(); iter.hasNext();) {
			OWLObject axiom = (OWLObject) iter.next();
			if (axiomRanksMap.containsKey(axiom)) pr += Double.parseDouble(axiomRanksMap.get(axiom).toString());
		}
		return pr;
	}
	
	/*
	 * Compute impact (of removing axioms) on entailments related to unsatisfiable classes:
	 * Brute Force: Given list of unsat classes and axioms in sos of each class,
	 * generate a repair plan, compute entailments between previously
	 * unsatisfiable classes, for each entailment, compute sos
	 * and see if earlier axiom falls in this sos
	 * Hueristic: inferredDependencyDetection
	 */
	private void computeImpactUnsat() {
		
		turnOffUsage = true;
		try {
			// using alternate method infDepDetection: remove disjoint axioms
			OWLOntology copy = swoopModel.cloneOntology(ontology);
			for (Iterator iter = new HashSet(copy.getClassAxioms()).iterator(); iter.hasNext();) {
				OWLClassAxiom axiom = (OWLClassAxiom) iter.next();
				if (axiom instanceof OWLDisjointClassesAxiom) {
					OWLDisjointClassesAxiom dis = (OWLDisjointClassesAxiom) axiom;
					// check that dis contains atleast one root, otherwise dont remove it
					boolean check = true; //false;
//					for (Iterator iter2 = currRoots.iterator(); iter2.hasNext();) {
//						OWLClass root = (OWLClass) iter2.next();
//						if (dis.getDisjointClasses().contains(root)) {
//							check = true;
//							break;
//						}
//					}
					if (check) {
						// remove disjoint axiom
						RemoveClassAxiom rem = new RemoveClassAxiom(copy, dis, null);
						rem.accept((ChangeVisitor) copy);
					}
				}
			}
			// now check for subsumption between unsatisfiable classes
			OWLDataFactory copyDF = copy.getOWLDataFactory();
			OWLClass thing = copyDF.getOWLThing();
			PelletReasoner pellet = new PelletReasoner();
			pellet.setOntology(copy);
			// *** key step next to get correct MUPS (becos of call to findMUPS below)
			pelletDebug.setOntology(copy);
			// *** end of key step
			for (Iterator iter = currRoots.iterator(); iter.hasNext();) {
				OWLClass root = (OWLClass) iter.next();
				if (pellet.isConsistent(root)) {
					// check equivalence to thing?
					if (pellet.equivalentClassesOf(thing).contains(root)) {
						OWLNot not = copyDF.getOWLNot(root);
						Set and = new HashSet();
						and.add(not);
						and.add(thing);
						OWLAnd test = copyDF.getOWLAnd(and);
						Set equ = new HashSet();
						equ.add(root);
						equ.add(thing);
						OWLEquivalentClassesAxiom ax = copyDF.getOWLEquivalentClassesAxiom(equ);
						System.out.println("checking equivalence between owl:Thing and " + swoopModel.shortForm(root.getURI()));
						this.findMUPS(test, ax);						
					}
					else {
						for (Iterator iter2 = pellet.equivalentClassesOf(root).iterator(); iter2.hasNext();) {
							OWLClass equCla = (OWLClass) iter2.next();
//							if (currRoots.contains(equCla)) {
								Set op1 = new HashSet();
								OWLNot not1 = copyDF.getOWLNot(root);
								op1.add(equCla);
								op1.add(not1);
								OWLAnd t1 = copyDF.getOWLAnd(op1);
								Set op2 = new HashSet();
								OWLNot not2 = copyDF.getOWLNot(equCla);
								op2.add(not2);
								op2.add(root);
								OWLAnd t2 = copyDF.getOWLAnd(op2);
								Set or = new HashSet();
								or.add(t1);
								or.add(t2);
								OWLOr test = copyDF.getOWLOr(or);
								Set equ = new HashSet();
								equ.add(root);
								equ.add(equCla);
								OWLEquivalentClassesAxiom ax = copyDF.getOWLEquivalentClassesAxiom(equ);
								System.out.println("check equivalence between " + swoopModel.shortForm(root.getURI()) + " and "+swoopModel.shortForm(equCla.getURI()));
								this.findMUPS(test, ax);								
//							}
						}
						for (Iterator iter2 = SetUtils.union(pellet.ancestorClassesOf(root)).iterator(); iter2.hasNext();) {
							OWLClass supCla = (OWLClass) iter2.next();
//							if (currRoots.contains(supCla)) {
							if (!pellet.equivalentClassesOf(thing).contains(supCla) && !supCla.equals(thing)) {
								Set op2 = new HashSet();
								OWLNot not2 = copyDF.getOWLNot(supCla);
								op2.add(not2);
								op2.add(root);
								OWLAnd test = copyDF.getOWLAnd(op2);
								OWLSubClassAxiom ax = copyDF.getOWLSubClassAxiom(root, supCla);
								System.out.println("check subclass between " + swoopModel.shortForm(root.getURI()) + " and "+swoopModel.shortForm(supCla.getURI()));
								this.findMUPS(test, ax);
							}
//							}
						}
					}
				}
			}
		}
		catch (OWLException ex) {
			ex.printStackTrace();
		}
		finally {
			turnOffUsage = false;
			try {
				// reset this after findMUPS has been called above
				pelletDebug.setOntology(ontology);
			} catch (OWLException e) {
				e.printStackTrace();
			}			
		}
	}

	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() == granularityCombo && updatePlanChk.isSelected()) {
			this.genPlan();
			this.refreshPlan();
		}
	}
}
