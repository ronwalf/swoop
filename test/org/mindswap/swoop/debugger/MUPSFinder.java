package org.mindswap.swoop.debugger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mindswap.pellet.debug.owlapi.Reasoner;
import org.mindswap.pellet.debug.utils.Timer;
import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.reasoner.PelletReasoner;
import org.mindswap.swoop.utils.SetUtils;
import org.mindswap.swoop.utils.owlapi.CorrectedRDFRenderer;
import org.mindswap.swoop.utils.owlapi.OWLOntBuilder;
import org.semanticweb.kaon2.api.DefaultOntologyResolver;
import org.semanticweb.kaon2.api.KAON2Connection;
import org.semanticweb.kaon2.api.KAON2Manager;
import org.semanticweb.kaon2.api.Ontology;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLClassAxiom;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owl.model.OWLDisjointClassesAxiom;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectPropertyInstance;
import org.semanticweb.owl.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLProperty;
import org.semanticweb.owl.model.OWLPropertyDomainAxiom;
import org.semanticweb.owl.model.OWLSameIndividualsAxiom;
import org.semanticweb.owl.model.OWLSubClassAxiom;
import org.semanticweb.owl.model.change.AddEntity;
import org.semanticweb.owl.model.change.AddEquivalentClass;
import org.semanticweb.owl.model.change.AddIndividualClass;
import org.semanticweb.owl.model.change.ChangeVisitor;
import org.semanticweb.owl.model.change.OntologyChange;
import org.semanticweb.owl.model.change.RemoveClassAxiom;
import org.semanticweb.owl.model.change.RemoveDomain;
import org.semanticweb.owl.model.change.RemoveEntity;
import org.semanticweb.owl.model.change.RemoveEquivalentClass;
import org.semanticweb.owl.model.change.RemoveIndividualAxiom;
import org.semanticweb.owl.model.change.RemoveIndividualClass;
import org.semanticweb.owl.model.change.RemoveObjectPropertyInstance;
import org.semanticweb.owl.model.change.RemoveObjectPropertyRange;
import org.semanticweb.owl.model.change.RemoveSuperClass;
import org.semanticweb.owl.model.helper.OntologyHelper;

public class MUPSFinder {

	SwoopModel swoopModel = new SwoopModel();
	boolean DEBUG = true;
	Map entTest = new HashMap();
	String NEWLINE = System.getProperty("line.separator");
	public boolean useRACER, useKAON;
	Timer testTimer;
	Map axiomMap = new HashMap();
	Map signatureMap = new HashMap();
	Map usageMap = new HashMap();
	int axiomLimit = 40;
	String logFile = "";
	public boolean allMUPS = false;
	public boolean useTableau = true;
	long racerTime = 0;
	
	public void init() throws Exception {
		
		// check for existing entMap
		loadMap();
		
		// load ontologies
		List testOnt = new ArrayList();		
		
		// read local file 
//		String fname = "testOntologies";
//		BufferedReader in = new BufferedReader(new FileReader(new File(fname+".txt")));
//		String line = null; 
//	    while (( line = in.readLine()) != null) {
//	     	 URI ontURI = new URI(line);
//	     	 System.out.println("Loading ontology: "+ontURI);
//	     	 OWLOntology ont = swoopModel.loadOntology(ontURI);
//	     	 testOnt.add(ont);
//	    }
		
		// read entire directory Swoop/test/ontologies
	 	String loc = "C:/Documents and Settings/UMD/My Documents/Semantic Web/SWOOP/test/ontologies/onts";
        File dir = new File( loc );
        File[] files = dir.listFiles(); 
        for( int i = 0; i < files.length; i++ ) {
        	File file = files[i];
        	String fname = file.getAbsolutePath().replaceAll(" ", "%20");
        	while (fname.indexOf("\\")>=0) {
        		fname = fname.substring(0, fname.indexOf("\\")) + "/" + fname.substring(fname.indexOf("\\")+1, fname.length());
        	}
//        	fname = fname.replaceAll("\\", "/");
        	System.out.println(fname);
        	OWLOntology ont = swoopModel.loadOntology(new URI("file:///"+fname));
        	testOnt.add(ont);        	
        }
	           
	    System.out.println("DONE: Ontologies Loaded");
	    
	    // select entailments in ontology
	    for (Iterator iter = testOnt.iterator(); iter.hasNext();) {
	    	OWLOntology ont = (OWLOntology) iter.next();
	    	this.selectEntailments(ont);
	    }
	    System.out.println("DONE: Entailments Selected");
	}
	
	public void viewEntailmentMap() throws Exception {
		if (entTest.isEmpty()) this.loadMap();
		System.out.println("Viewing entailment-test map");
		for (Iterator iter = entTest.keySet().iterator(); iter.hasNext();) {
			URI ontURI = (URI) iter.next();
			Set ents = (HashSet) entTest.get(ontURI);
			System.out.println(ontURI+" : "+ents.size());
		}
	}

	public void loadMap() throws Exception {
		File file = new File("debugTestEnts.map");
		if (file.exists()) {
			ObjectInputStream iis = new ObjectInputStream(new FileInputStream(file));
			entTest = (HashMap) iis.readObject();
			iis.close();
			System.out.println("Loaded entailment test map");
		}
	}
	
	public void writeTestEntsMap() throws Exception {
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File("debugTestEnts.map")));
		oos.writeObject(entTest);
		oos.close();
		System.out.println("Writing File: debugTestEnts.map");
	}
	
	public void writeOntology(OWLOntology ont) throws Exception {
		CorrectedRDFRenderer rdfRend = new CorrectedRDFRenderer();
		StringWriter st = new StringWriter();
		rdfRend.renderOntology(ont, st);
		FileWriter fw = new FileWriter(new File("test.owl"));
		fw.write(st.toString());
		fw.close();
	}
	
	public void writeLogFile() throws Exception {
		FileWriter fw = new FileWriter(new File("debugEvalLog.txt"));
		fw.write(logFile);
		fw.close();
		System.out.println("Written log: debugEvalLog.txt");
	}
	
	public void selectEntailments(OWLOntology ont) throws Exception {
		// pass ont through pellet
		PelletReasoner pellet = new PelletReasoner();
		System.out.println("Processing ontology "+ont.getURI());
		pellet.setOntology(ont);
		Set entailments = new HashSet();
		OWLClass thing = ont.getOWLDataFactory().getOWLThing();
		
		System.out.println("Checking for unsatisfiable classes"+ont.getURI());
		Set unsat = pellet.equivalentClassesOf(ont.getOWLDataFactory().getOWLNothing());
		if (unsat.isEmpty()) {
			// obtain inferred subsumption and instantiation entailments
			System.out.println("No unsatisfiable classes found");
			for (Iterator iter = ont.getClasses().iterator(); iter.hasNext();) {
				OWLClass cla = (OWLClass) iter.next();
				Set infSup = SetUtils.union(pellet.superClassesOf(cla));
				Set assSup = cla.getSuperClasses(ont);
				infSup.removeAll(assSup);
				for (Iterator iter2 = infSup.iterator(); iter2.hasNext();) {
					OWLClass sup = (OWLClass) iter2.next();
					OWLSubClassAxiom axiom = ont.getOWLDataFactory().getOWLSubClassAxiom(cla, sup);
					String sub = "sub("+cla.getURI()+","+((OWLClass) sup).getURI()+")";
					if (!sup.equals(thing)) {
						entailments.add(sub);
						if (DEBUG) System.out.println("Inferred Subclass "+cla+" "+sup);
					}
				}
			}
			for (Iterator iter = ont.getIndividuals().iterator(); iter.hasNext();) {
				OWLIndividual ind = (OWLIndividual) iter.next();
				Set infInst = SetUtils.union(pellet.typesOf(ind));
				Set assInst = ind.getTypes(ont);
				infInst.removeAll(assInst);
				for (Iterator iter2 = infInst.iterator(); iter2.hasNext();) {
					OWLDescription desc = (OWLDescription) iter2.next();					
//					OWLIndividualTypeAssertion axiom = new OWLIndividualTypeAssertionImpl((OWLDataFactoryImpl) ont.getOWLDataFactory(), ind, desc);
					String type = "type("+ind.getURI()+","+((OWLClass) desc).getURI()+")";
					if (!desc.equals(thing) && !ind.isAnonymous()) {
						entailments.add(type);
						if (DEBUG) System.out.println("Inferred Instance "+ind+" "+desc);
					}
				}
			}
		}
		else {
			if (DEBUG) System.out.println("Found Unsatisfiable Classes: "+unsat.size());
			OWLClass nothing = ont.getOWLDataFactory().getOWLNothing();
			for (Iterator iter = unsat.iterator(); iter.hasNext();) {
				OWLClass cla = (OWLClass) iter.next();
//				OWLSubClassAxiom axiom = ont.getOWLDataFactory().getOWLSubClassAxiom(cla, nothing);
				String sub = "sub("+cla.getURI()+","+nothing.getURI()+")";
				entailments.add(sub);
			}
		}
		entTest.put(ont.getPhysicalURI(), entailments);
		this.writeTestEntsMap();
	}
	
	public void runTests() throws Exception {
		this.loadMap();
		for (Iterator iter = entTest.keySet().iterator(); iter.hasNext();) {
			URI ontURI = (URI) iter.next();
			Set ents = (HashSet) entTest.get(ontURI);
			this.processOntEnts(ontURI, ents);			
		}
	}
	
	public void processOntEnts(URI ontURI, Set ents) throws Exception {
		
		System.out.println("Processing Ontology: "+ontURI);
		OWLOntology ont = swoopModel.loadOntology(ontURI);
		// select atmost n entailments randomly from ents
		Set selEnts = new HashSet(ents);
		while (selEnts.size()>10) {
			selEnts.remove(selEnts.iterator().next());
		}
		System.out.println("No. of entailments being tested: "+selEnts.size());
		
		logFile += "Ontology: "+ontURI + NEWLINE;
		
		for (Iterator iter = selEnts.iterator(); iter.hasNext();) {
			
			String ent = (String) iter.next();
			System.out.println("Testing entailment: "+ent);
			
			logFile += ent;
			
			OWLClass cla = null;
			List remove = new ArrayList();
			
			if (ent.startsWith("sub")) {
				// subclass - sub(C1, C2)
				String claURI1 = ent.substring(ent.indexOf("(")+1, ent.indexOf(","));
				String claURI2 = ent.substring(ent.indexOf(",")+1, ent.indexOf(")"));
				OWLClass cla1 = (OWLClass) swoopModel.getEntity(ont, new URI(claURI1), true); //ont.getClass(new URI(claURI1));
				cla = cla1;
				
				if (claURI2.indexOf("Nothing")==-1) {
					OWLClass cla2 = (OWLClass) swoopModel.getEntity(ont, new URI(claURI2), true); // ont.getClass(new URI(claURI2));
					OWLDescription notC2 = ont.getOWLDataFactory().getOWLNot(cla2);
					Set and = new HashSet();
					and.add(cla1);
					and.add(notC2);
					OWLDescription andDesc = ont.getOWLDataFactory().getOWLAnd(and);
					// add new temp class to ontology equivalent to complex and description
					cla = ont.getOWLDataFactory().getOWLClass(new URI(ont.getLogicalURI()+"#tempClass"));
					AddEntity ae = new AddEntity(ont, cla, null);
					ae.accept((ChangeVisitor) ont);
					Set equs = new HashSet();
					equs.add(cla);
					equs.add(andDesc);
					AddEquivalentClass aec = new AddEquivalentClass(ont, cla, andDesc, null);
					aec.accept((ChangeVisitor) ont);
//					remove.add(new RemoveEquivalentClass(ont, cla, andDesc, null));
					remove.add(new RemoveEntity(ont, cla, null));
				}							
			}
			else {
				// type assertion - type(I1, C1)
				String indURI = ent.substring(ent.indexOf("(")+1, ent.indexOf(","));
				String claURI = ent.substring(ent.indexOf(",")+1, ent.indexOf(")"));
				OWLIndividual ind = ont.getIndividual(new URI(indURI));
				OWLClass type = ont.getClass(new URI(claURI));
				OWLDescription notT = ont.getOWLDataFactory().getOWLNot(type);
				AddIndividualClass aic = new AddIndividualClass(ont, ind, notT, null);
				aic.accept((ChangeVisitor) ont);
				remove.add(new RemoveIndividualClass(ont, ind, notT, null));
				// add temp class
				cla = ont.getOWLDataFactory().getOWLClass(new URI(ont.getLogicalURI()+"#tempClass"));
				AddEntity ae = new AddEntity(ont, cla, null);
				ae.accept((ChangeVisitor) ont);
				AddEquivalentClass aec = new AddEquivalentClass(ont, cla, notT, null);
				aec.accept((ChangeVisitor) ont);
				remove.add(new RemoveEntity(ont, cla, null));			
			}
			
			// call key method now
			this.callReasoners(ont, cla);
			
			// remove temp changes
			for (Iterator iter2 = remove.iterator(); iter2.hasNext();) {
				OntologyChange oc = (OntologyChange) iter2.next();
				oc.accept((ChangeVisitor) ont);
			}
			
			logFile += NEWLINE;
			
			this.writeLogFile();
		}		
	}
	
	public void callReasoners(OWLOntology ont, OWLClass cla) throws Exception {

		Set sos = new HashSet(); //TODO check size / return
		// run all tests by calling reasoners
		// write ontology locally :(
		this.writeOntology(ont);
		
		List MUPS = new ArrayList();		
		
		// ** RACER
//		useRACER = true;
//		useKAON = false;
//		System.out.println("calling RACER");
//		sos = this.getBlackBoxSOS(ont, cla);
//		System.out.println("sos size: "+sos.size());
//		MUPS.add(sos);
//		if (allMUPS) HSTMUPS(sos, ont, cla, MUPS, new ArrayList(), new HashSet(), new HashSet());
//		System.out.println("time: "+(testTimer.getTotal()+racerTime)+" mups size: "+MUPS);
//		logFile += " "+String.valueOf(testTimer.getTotal()+racerTime) + " ("+MUPS.size()+")";
//		racerTime = 0; //***** key
		
		// ** KAON2
//		useKAON = true;
//		useRACER = false;
//		System.out.println("calling KAON2");
//		sos = this.getBlackBoxSOS(ont, cla);
//		System.out.println("sos size: "+sos.size());
//		MUPS = new ArrayList();
//		MUPS.add(sos);
//		if (allMUPS) HSTMUPS(sos, ont, cla, MUPS, new ArrayList(), new HashSet(), new HashSet());
//		System.out.println("time: "+testTimer.getTotal()+" mups size: "+MUPS);
//		logFile += " "+testTimer.getTotal() + " ("+MUPS.size()+")";
		
		// ** Pellet 
		useRACER = false;
		useKAON = false;
		System.out.println("calling Pellet");
		sos = this.getBlackBoxSOS(ont, cla);
		System.out.println("sos size: "+sos.size());
		MUPS = new ArrayList();
		MUPS.add(sos);
		if (allMUPS) HSTMUPS(sos, ont, cla, MUPS, new ArrayList(), new HashSet(), new HashSet());
		System.out.println("time: "+testTimer.getTotal()+" mups size: "+MUPS);
		logFile += " "+testTimer.getTotal() + " ("+MUPS.size()+")";
		
	}
	
	public Set getBlackBoxSOS(OWLOntology ont, OWLClass cla) throws Exception {
		
		Set sos = new HashSet();
		
		// reset all maps and set limit
		axiomLimit = 40;
		usageMap.clear();
		axiomMap.clear();
		signatureMap.clear();
		
		testTimer = new Timer("total");
		testTimer.start();
		
		// add axioms related to class 
		Set axioms = swoopModel.getAxioms(cla, ont);
		axiomMap.put(cla.getURI(), axioms);
		OWLOntBuilder ob = new OWLOntBuilder();
		ob.addAxiom = true; // add axiom mode set to true
		
		// add all base axioms to testOnt via ob
		for (Iterator iter = axioms.iterator(); iter.hasNext();) {
			OWLObject axiom = (OWLObject) iter.next();
			axiom.accept(ob);
		}
		
		// toggle a variable that considers entity usage while expanding axiom set
		boolean expandMore = false;
		
		testTimer.stop();
		
		// add linked references iteratively
		while (checkSatisfiability(ob, cla)) {
			
			testTimer.start();
			
			Set newAxioms = this.expandAxiomSet(axioms, ont, expandMore);
//			System.out.println("Size of axioms: "+axioms.size());
			
			// add axioms from latest to testOnt
			for (Iterator it = newAxioms.iterator(); it.hasNext();) {
				OWLObject axiom = (OWLObject) it.next(); 
				axiom.accept(ob);					
			}
			
			if (newAxioms.isEmpty() && expandMore) {
				System.out.println("ERROR: Could not find axioms responsible for error!");
				testTimer.stop();
				return sos; 
			}
			else if (newAxioms.isEmpty()) {
				expandMore = true;
			}
//			System.out.println(axioms);
			testTimer.stop();
		}
		
		// now axioms contains cla unsatisfiable
		// remove one axiom at a time and if it turns satisfiable, add it to sos
//		System.out.println("Found concept unsatisfiable: #axioms = "+axioms.size());
		
		testTimer.start();
		
		// fast pruning 
		List axiomList = new ArrayList(axioms);
		int pruneWindow = 10;
		if (axiomList.size()>pruneWindow) {
			axioms.clear();
			int parts = axiomList.size() / pruneWindow;
			for (int part=0; part<parts; part++) {
				for (int i=part*pruneWindow; i<part*pruneWindow+pruneWindow; i++) {
					ob.addAxiom = false;
					((OWLObject)axiomList.get(i)).accept(ob);
				}
				testTimer.stop();
				if (checkSatisfiability(ob, cla)) {
					testTimer.start();
					for (int i=part*pruneWindow; i<part*pruneWindow+pruneWindow; i++) {
						axioms.add(axiomList.get(i));
						ob.addAxiom = true;
						((OWLObject)axiomList.get(i)).accept(ob);
					}
					testTimer.stop();
				}
				testTimer.start();
			}
			if (axiomList.size()>parts*pruneWindow) {
				// add remaining from list to axioms
				for (int i=parts*pruneWindow; i<axiomList.size(); i++) {
					axioms.add(axiomList.get(i));
				}
			}
		}
		
		// slow pruning
		for (Iterator iter = new HashSet(axioms).iterator(); iter.hasNext();) {
			OWLObject axiom = (OWLObject) iter.next();
			axioms.remove(axiom);
			ob.addAxiom = false;
			axiom.accept(ob);
			testTimer.stop();
			if (checkSatisfiability(ob, cla)) {
				testTimer.start();
				sos.add(axiom);
				axioms.add(axiom);
				ob.addAxiom = true;
				axiom.accept(ob);
				testTimer.stop();
			}
			testTimer.start();
		}
		
		testTimer.stop();
		return sos;
	}
	
	private boolean checkSatisfiability(OWLOntBuilder ob, OWLDescription clazz) throws Exception {
		
		// check satisfiability of clazz in ont in ob
		OWLOntology newOnt = ob.currentOnt;
		boolean sat = false;
		boolean checkConsistency = false;
		
		if (clazz == null) {
			checkConsistency = true;			
		}
		else {
			// check if clazz is not present at all
			if (clazz instanceof OWLClass && newOnt.getClass(((OWLClass) clazz).getURI())==null) return true;
			clazz = ob.visitDescription(clazz);
		}
		
		// use reasoner to check class consistency
		String fname = "file:///C:/Docume~1/UMD/MyDocu~1/Semant~1/SWOOP/test.owl";
//		if (useRACER) {
//			// communicate with Racer using JRacer API
//			try {
//				// write ontology locally!
//				this.writeOntology(newOnt);
//				
//				// communicate with RACER
//				RacerServer racer = new RacerServer("localhost",8088);
//				racer.openConnection();
////				racer.send("(logging-on)");
//				racer.send("(owl-read-document \""+fname+"\")");
//				String response = "";
//				PrintStream temp = System.out;
//				System.setOut(new PrintStream(new FileOutputStream(new File("time.txt"))));
//			    if (checkConsistency) {
//					response = racer.send("(time (abox-consistent-p))");
//				}
//				else {
//					response = racer.send("(time (concept-satisfiable? "+"|"+((OWLClass) clazz).getURI().toString()+"|))");					
//				}
//			    BufferedReader in = new BufferedReader(new FileReader(new File("time.txt")));
//		        String line = null, hack = "";
//		        while (( line = in.readLine()) != null) {
//			       hack += line;
//			    }
//			    System.setOut(temp);
//			    int spos = hack.lastIndexOf("took");
//			    int epos = hack.lastIndexOf("seconds");
//			    if (spos!=-1 && epos!=-1) {
//			   	   String time = hack.substring(spos+4, epos);
//				   racerTime += (long) Float.parseFloat(time)*1000;				   
//			    }
//				sat = (response.equalsIgnoreCase("nil")) ? false : true;
//			}
//			catch (Exception ex) {System.out.println("No RACER: "+ex.getMessage());}
//		}
		if (useKAON) {
			// write ontology!
			this.writeOntology(newOnt);
			
			// communicate with KAON2 using API
			KAON2Connection connection = KAON2Manager.newConnection();
			DefaultOntologyResolver resolver = new DefaultOntologyResolver();
			resolver.registerReplacement(newOnt.getLogicalURI().toString(), fname); //"http://kaon2.semanticweb.org/example1","file:src/ex1/example1.xml");
	        connection.setOntologyResolver(resolver);
	        // read in ontology
	        try {
	        	Ontology ontology = connection.openOntology(newOnt.getLogicalURI().toString(), new HashMap());
	        	org.semanticweb.kaon2.api.owl.elements.OWLClass cla = KAON2Manager.factory().owlClass(((OWLClass) clazz).getURI().toString());
		        org.semanticweb.kaon2.api.reasoner.Reasoner reasoner = ontology.createReasoner();
		        testTimer.start();
				if (checkConsistency) sat = reasoner.isSatisfiable();
				else sat = reasoner.isSatisfiable(cla);
				testTimer.stop();
	        }
	        catch (Exception ex) {System.out.println("No KAON: "+ex.getMessage());}	        		    	        
		}
		else {
			// create new instance of pellet and check sat. of clazz
			PelletReasoner newPellet = new PelletReasoner();
			newPellet.setOntology(newOnt, false);
			testTimer.start();
			if (checkConsistency) sat = newPellet.isConsistent();
			else sat = newPellet.isConsistent(clazz);
			testTimer.stop();		        
		}		
		return sat;
	}
	
	public Set expandAxiomSet(Set axioms, OWLOntology ont, boolean expandMore) {
		
		try {
			Set newEntities = new HashSet();
			
			for (Iterator iter = new HashSet(axioms).iterator(); iter.hasNext();) {
				OWLObject axiom = (OWLObject) iter.next();
				if (signatureMap.containsKey(axiom)) {
					newEntities.addAll((HashSet) signatureMap.get(axiom));
				}
				else {
					Set ents = swoopModel.getAxiomSignature(axiom, ont);
					signatureMap.put(axiom, ents);
					newEntities.addAll(ents);					
				}
				
				if (expandMore) {
					// expand entity set to include usage entities
					for (Iterator iter2 = new HashSet(newEntities).iterator(); iter2.hasNext();) {
						Set usage = new HashSet();
						OWLEntity ent = (OWLEntity) iter2.next();
						if (ent==null || ent.getURI()==null) {
							continue;
						}
					
						// check in local cache first before using OntologyHelper
						if (usageMap.containsKey(ent.getURI())) {
							usage = (HashSet) usageMap.get(ent.getURI());
						}
						else {	
							usage = OntologyHelper.entityUsage(ont, ent);
							usageMap.put(ent.getURI(), usage);
						}
						// only add entities because axioms are returned
						for (Iterator it = usage.iterator(); it.hasNext();) {
							Object e = it.next();
							if (e instanceof OWLEntity) newEntities.add(e);
							else if (e instanceof OWLObject) {
								if (signatureMap.containsKey(e)) {
									newEntities.addAll((HashSet) signatureMap.get(e));
								}
								else {
									Set ents = swoopModel.getAxiomSignature((OWLObject) e, ont);
									signatureMap.put(e, ents);
									newEntities.addAll(ents);					
								}
							}
						}
					}
				}
			}
			
			// get axioms for all newEntities either from local cache or from swoopModel
			Set newAxioms = new HashSet();
			for (Iterator iter2 = newEntities.iterator(); iter2.hasNext();) {
				OWLEntity ent = (OWLEntity) iter2.next();
				if (ent==null || ent.getURI()==null) {
					continue;
				}
				if (axiomMap.containsKey(ent.getURI())) {
					newAxioms.addAll((HashSet) axiomMap.get(ent.getURI()));
				}
				else {
					Set ax = swoopModel.getAxioms(ent, ont);
					axiomMap.put(ent.getURI(), ax);
					newAxioms.addAll(ax);
				}								
			}
			
			if (axioms.containsAll(newAxioms)) {
				return new HashSet();
			}
			else {
				// determine latest axioms
				Set before = new HashSet(axioms);
				Set latest = new HashSet(axioms);
				latest.addAll(newAxioms);
				latest.removeAll(before);
//				// set a limit on axioms to be added
				if (latest.size()>axiomLimit) {
					// only let limited entities remain in latest
					Set copyLatest = new HashSet(latest);
					latest.clear();
					for (int ctr = 0; ctr < axiomLimit; ctr++) {
						Object ax = copyLatest.iterator().next();
						latest.add(ax);
						copyLatest.remove(ax);
					}
					axiomLimit *= 1.25; // slowly increase axiom limit
				}
				newAxioms = latest;
				axioms.addAll(newAxioms);
				return newAxioms;
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		return new HashSet();
	}
	
	public void HSTMUPS(Set mups, OWLOntology onto, OWLClass cla, List MUPS, List explStr, Set satPaths, Set currPath) {
		
		// key step - make a backup of onto
		OWLOntology backup = swoopModel.cloneOntology(onto);
		
		try {
			for (Iterator iter = mups.iterator(); iter.hasNext();) {
				
				// reset ontology 
				OWLOntology copyOnt = swoopModel.cloneOntology(backup);
				
				testTimer.start();
				
				OWLObject axiom = (OWLObject) iter.next();
				currPath.add(axiom);
//				System.out.println(axiom);
				
				// **** remove axiom from copyOnt *****
				if (axiom instanceof OWLDisjointClassesAxiom) {
					OWLDisjointClassesAxiom dis = (OWLDisjointClassesAxiom) axiom;
					Set disSet = dis.getDisjointClasses();
					Set newDisSet = new HashSet();
					for (Iterator iter2 = disSet.iterator(); iter2.hasNext();) {
						OWLDescription desc = (OWLDescription) iter2.next();
						if (desc instanceof OWLClass) 
							newDisSet.add(copyOnt.getClass(((OWLClass) desc).getURI()));						
						else 
							newDisSet.add(desc);
					}
					OWLDisjointClassesAxiom newDis = copyOnt.getOWLDataFactory().getOWLDisjointClassesAxiom(newDisSet);
					RemoveClassAxiom r = new RemoveClassAxiom(copyOnt, (OWLClassAxiom) newDis, null);
					r.accept((ChangeVisitor) copyOnt);
				}
				else if (axiom instanceof OWLEquivalentClassesAxiom) {
					OWLEquivalentClassesAxiom equ = (OWLEquivalentClassesAxiom) axiom;
					Set equSet = equ.getEquivalentClasses();
					Set newEquSet = new HashSet();
					List equList = new ArrayList();
					for (Iterator iter2 = equSet.iterator(); iter2.hasNext();) {
						OWLDescription desc = (OWLDescription) iter2.next();
						if (desc instanceof OWLClass) { 
							newEquSet.add(copyOnt.getClass(((OWLClass) desc).getURI()));
							equList.add(copyOnt.getClass(((OWLClass) desc).getURI()));
						}
						else {
							newEquSet.add(desc);
							equList.add(desc);
						}
					}
					OWLEquivalentClassesAxiom newEqu = copyOnt.getOWLDataFactory().getOWLEquivalentClassesAxiom(newEquSet);
					RemoveClassAxiom r = new RemoveClassAxiom(copyOnt, (OWLClassAxiom) newEqu, null);
					r.accept((ChangeVisitor) copyOnt);
					if (equList.size()==2) {
						OWLDescription desc1 = (OWLDescription) equList.get(0);
						OWLDescription desc2 = (OWLDescription) equList.get(0);
						if (desc1 instanceof OWLClass) {
							RemoveEquivalentClass re = new RemoveEquivalentClass(copyOnt, (OWLClass) desc1, desc2, null);
							re.accept((ChangeVisitor) copyOnt);
						}
						if (desc2 instanceof OWLClass) {
							RemoveEquivalentClass re = new RemoveEquivalentClass(copyOnt, (OWLClass) desc2, desc1, null);
							re.accept((ChangeVisitor) copyOnt);
						}
					}
				}
				else if (axiom instanceof OWLSubClassAxiom) {
					OWLSubClassAxiom subA = (OWLSubClassAxiom) axiom;
					OWLDescription sub = subA.getSubClass();
					OWLDescription sup = subA.getSuperClass();
					OWLDescription newSub = sub;
					if (sub instanceof OWLClass) newSub = copyOnt.getClass(((OWLClass) sub).getURI());
					OWLDescription newSup = sup;
					if (sup instanceof OWLClass) newSup = copyOnt.getClass(((OWLClass) sup).getURI());
					OWLSubClassAxiom newSubA = copyOnt.getOWLDataFactory().getOWLSubClassAxiom(newSub, newSup);
					OntologyChange r = new RemoveClassAxiom(copyOnt, (OWLClassAxiom) newSubA, null);
					r.accept((ChangeVisitor) copyOnt);
					if (newSub instanceof OWLClass) {
						r = new RemoveSuperClass(copyOnt, (OWLClass) newSub, newSup, null);
						r.accept((ChangeVisitor) copyOnt);
					}					
				}
				else if (axiom instanceof OWLPropertyDomainAxiom) {
					OWLPropertyDomainAxiom opd = (OWLPropertyDomainAxiom) axiom;
					OWLProperty prop = opd.getProperty();
					OWLProperty newProp = null;
					if (prop instanceof OWLDataProperty) newProp = copyOnt.getDataProperty(prop.getURI());
					else newProp = copyOnt.getObjectProperty(prop.getURI());
					OWLDescription desc = opd.getDomain();
					OWLDescription newDesc = desc;
					if (desc instanceof OWLClass) newDesc = copyOnt.getClass(((OWLClass) desc).getURI());
					RemoveDomain rd = new RemoveDomain(copyOnt, newProp, newDesc, null);
					rd.accept((ChangeVisitor) copyOnt);
				}
				else if (axiom instanceof OWLObjectPropertyRangeAxiom) {
					OWLObjectPropertyRangeAxiom opd = (OWLObjectPropertyRangeAxiom) axiom;
					OWLObjectProperty prop = opd.getProperty();
					OWLObjectProperty newProp = copyOnt.getObjectProperty(prop.getURI());
					OWLDescription desc = opd.getRange();
					OWLDescription newDesc = desc;
					if (desc instanceof OWLClass) newDesc = copyOnt.getClass(((OWLClass) desc).getURI());
					RemoveObjectPropertyRange ropr = new RemoveObjectPropertyRange(copyOnt, newProp, newDesc, null);
					ropr.accept((ChangeVisitor) copyOnt);
				}
				else if (axiom instanceof OWLObjectPropertyInstance) {
					OWLObjectPropertyInstance oop = (OWLObjectPropertyInstance) axiom;
					OWLIndividual sub = oop.getSubject();
					if (sub.isAnonymous()) {
						sub = copyOnt.getOWLDataFactory().getAnonOWLIndividual(sub.getAnonId());
					}
					else {
						sub = copyOnt.getIndividual(sub.getURI());
					}
					OWLObjectProperty prop = copyOnt.getObjectProperty(oop.getProperty().getURI());
					OWLIndividual obj = copyOnt.getIndividual(oop.getObject().getURI());
					RemoveObjectPropertyInstance ropi = new RemoveObjectPropertyInstance(copyOnt, sub, prop, obj, null);
					ropi.accept((ChangeVisitor) copyOnt);
				}
				else if (axiom instanceof OWLSameIndividualsAxiom) {
					OWLSameIndividualsAxiom osi = (OWLSameIndividualsAxiom) axiom;
					Set newInd = new HashSet();
					for (Iterator it = osi.getIndividuals().iterator(); it.hasNext();) {
						newInd.add(copyOnt.getIndividual(((OWLIndividual) it.next()).getURI()));						
					}
					OWLSameIndividualsAxiom copyInd = copyOnt.getOWLDataFactory().getOWLSameIndividualsAxiom(newInd);
					RemoveIndividualAxiom ria = new RemoveIndividualAxiom(copyOnt, copyInd, null);
					ria.accept((ChangeVisitor) copyOnt);
				}
				else if (axiom instanceof OWLDifferentIndividualsAxiom) {
					OWLDifferentIndividualsAxiom osi = (OWLDifferentIndividualsAxiom) axiom;
					Set newInd = new HashSet();
					for (Iterator it = osi.getIndividuals().iterator(); it.hasNext();) {
						newInd.add(copyOnt.getIndividual(((OWLIndividual) it.next()).getURI()));						
					}
					OWLDifferentIndividualsAxiom copyInd = copyOnt.getOWLDataFactory().getOWLDifferentIndividualsAxiom(newInd);
					RemoveIndividualAxiom ria = new RemoveIndividualAxiom(copyOnt, copyInd, null);
					ria.accept((ChangeVisitor) copyOnt);
				}
				//TODO: more removal!
				
				// test if copyOnt has changed
				//FIXME: not working when individual obj prop assertions are actually removed 
//				if (copyOnt.equals(onto)) {
//					System.out.println("Ontology hasn't changed after removing axiom "+axiom);
//					continue;
//				}
				
				// get class in copyOnt
				cla = copyOnt.getClass(cla.getURI());
				
				// early path termination
				boolean earlyTermination = false;
				for (Iterator i=satPaths.iterator(); i.hasNext();) {
					Set satPath = (HashSet) i.next();
					if (satPath.containsAll(currPath)) {
						System.out.println("EARLY PATH TERMINATION!");
						earlyTermination = true;
						break;
					}
				}
				
				if (!earlyTermination) { 
					// check if there is a new mups of class
					Set newMUPS = new HashSet();
					String expl = "";
					if (testTimer.isStarted()) testTimer.stop();
					if (useTableau) {
						// use tableau tracing
						newMUPS = this.getTableauSOS(copyOnt, cla);						
					}
					else {
						// use black box
						newMUPS = this.getBlackBoxSOS(copyOnt, cla);
					}
					testTimer.start();
					if (!newMUPS.isEmpty()) {
						if (!MUPS.contains(newMUPS)) { 
							// print explanation for new MUPS
							MUPS.add(newMUPS);
							explStr.add(expl);
							System.out.println("FOUND NEW MUPS - MUPS COUNT: "+MUPS.size());
							// recurse!						
							HSTMUPS(newMUPS, copyOnt, cla, MUPS, explStr, satPaths, currPath);
						}
					}
					else {
						satPaths.add(new HashSet(currPath));
					}
				}
				
				currPath.remove(axiom);
				
				if (testTimer.isStarted()) testTimer.stop();
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		finally {
			if (testTimer.isStarted()) testTimer.stop();
		}
	}
	
	public void cleanLog() throws Exception {
		BufferedReader in = new BufferedReader(new FileReader(new File("debugEvalLog.txt")));
		String line = null; 
		String newLog = "";
		swoopModel.setShowQNames(false);
	    while (( line = in.readLine()) != null) {
	     	 if (line.indexOf("(")!=-1) {  
			 	String token1 = line.substring(line.indexOf("(")+1, line.indexOf(","));
			 	String token2 = line.substring(line.indexOf(",")+1, line.indexOf(")"));
			 	line = line.replace(token1, swoopModel.shortForm(new URI(token1)));
			 	line = line.replace(token2, swoopModel.shortForm(new URI(token2)));
	     	 }
	     	 newLog += line + NEWLINE;
	    }
	    logFile = newLog;
	    System.out.print("Cleant file..");
	    this.writeLogFile();
	}
	
	public Set getTableauSOS(OWLOntology ont, OWLDescription clazz) {
		try {
			Reasoner pelletDebug = new Reasoner();
			pelletDebug.setOntology(ont);
			pelletDebug.getKB().setDoExplanation(true);
			pelletDebug.getKB().doDependencyTracking = true;
			boolean consistent = true;
			Timer timers = new Timer("Pellet Debugging Check");
		    timers.start();
			if (clazz!=null) consistent = pelletDebug.isConsistent(clazz);
			else consistent = pelletDebug.isConsistent();
			timers.stop();
			System.out.println(timers);
			if (consistent) {
				// no SOS cos ABox is consistent!
				System.out.println("No SOS since ABox is consistent");
				return new HashSet();
			}
			
			Set explanationSet = pelletDebug.getKB().getExplanationSet();
			
			// prune the axioms in case there are additional axioms
			Set prunedSet = new HashSet(explanationSet);
			for (Iterator iter = explanationSet.iterator(); iter.hasNext();) {
				OWLObject axiom = (OWLObject) iter.next();
				prunedSet.remove(axiom);
				boolean sat = false;
				sat = this.checkSatisfiability(prunedSet, clazz);
				if (sat) prunedSet.add(axiom);
			}
			explanationSet = prunedSet;
			// end of pruning
			
			return explanationSet;			
		}
		catch (OWLException ex) {
			ex.printStackTrace();
		}
		return new HashSet();
	}
	
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
	
	public void removeOnt() throws Exception {
		if (entTest.isEmpty()) this.loadMap();
		entTest.remove(new URI("http://sweet.jpl.nasa.gov/ontology/earthrealm.owl"));
		this.writeTestEntsMap();
	}
	
	public static void main(String[] args) {
		try {
			MUPSFinder t = new MUPSFinder();
			
			//*** part 1: run through test ontologies, collect interesting entailments, 
			// i.e., unsat classes and inferred subsumption/instantiation
			// and store them in a map - entTest..write to file "debugTestEnts.map"			
//			t.init();
//			 t.removeOnt();
//			t.viewEntailmentMap();
			
			//*** part 2: read debugTestEnts map, for each ontology, select 20 entailments randomly
			// and run through various reasoners, collect timing information and store in logFile
			// write to "debugEvalLog.txt"
			t.runTests();			
			t.cleanLog();
			
//			System.out.println(t.logFile);
		} 
		catch (Exception e) {
			e.printStackTrace();
		}		
	}
}
