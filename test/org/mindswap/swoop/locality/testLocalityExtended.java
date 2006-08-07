package org.mindswap.swoop.locality;

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
import org.mindswap.swoop.racer.JRacer;
import org.mindswap.swoop.reasoner.PelletReasoner;
import org.mindswap.swoop.refactoring.LocalityChecker;
import org.mindswap.swoop.refactoring.LocalityCheckerExtended;
import org.mindswap.swoop.utils.SetUtils;
import org.mindswap.swoop.utils.owlapi.CorrectedRDFRenderer;
import org.mindswap.swoop.utils.owlapi.OWLOntBuilder;
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

public class testLocalityExtended {

	SwoopModel swoopModel = new SwoopModel();
	boolean DEBUG = true;
	Map entTest = new HashMap();
	String NEWLINE = System.getProperty("line.separator");
	Timer testTimer;
	String logFile = "";
	List testOnt;
	final public static int SHOW_INDIVIDUALS    = 1;
	final public static int SHOW_CLASSES        = 2;
	final public static int SHOW_PROPERTIES     = 3;
	final public static int SHOW_DATAPROPERTIES = 4;
	final public static int SHOW_OBJPROPERTIES  = 5;
	
	public testLocalityExtended() throws Exception {
		
		// load ontologies
		this.testOnt = new ArrayList();		
	
		// read entire directory Swoop/test/ontologies
	 	String loc = "C:/ontologies/imports/ontologies27";
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
	    
	 }
	
	public void runTest() throws Exception{
		Iterator iter = testOnt.iterator();
		int loc = 0;
		int nloc = 0;
		int total = 0;
		int cycle = 0;
		while(iter.hasNext()){
			OWLOntology ontology = (OWLOntology)iter.next();
			System.out.println("-------------------------------------------");
			System.out.println("Testing Ontology " + ontology.getURI().toString());
			//Create Locality Checker Object
			LocalityCheckerExtended checker = new LocalityCheckerExtended(ontology);
			//retrieve the foreign entities in the ontology
			Set foreign = new HashSet();
			foreign = swoopModel.getEntitySet(ontology, swoopModel.IMPORTED_ONT, SHOW_CLASSES);
			int totalEntities = swoopModel.getEntitySet(ontology, swoopModel.TRANSCLOSE_ONT, SHOW_CLASSES).size();
			//foreign.removeAll(swoopModel.getEntitySet(ontology, swoopModel.BASE_ONT, SHOW_CLASSES));
			//foreign.removeAll(ontology.getObjectProperties());
			//foreign.removeAll(ontology.getDataProperties());
			//foreign.removeAll(ontology.getIndividuals());
			foreign.removeAll(ontology.getAnnotationProperties());
			
			
			if(DEBUG){
				System.out.println("Total number of entities in imported ontologies: " + totalEntities);
				System.out.println("number of foreign entities: " + foreign.size());
			    System.out.println("The Foreign entities are the following:");
			    Iterator it = foreign.iterator();
			    while (it.hasNext()){
			    	OWLEntity ent = (OWLEntity)it.next();
			  //  	System.out.println(ent.getURI().toString());
			    }
			}
			if(!checker.isHierarchical(ontology)){
				System.out.println("The importing structure contains a CYCLE");
			}
			else{
				if (checker.isLocal(ontology,foreign)){
				System.out.println("The ontology is Local with respect to the foreign signature");
				loc = loc + 1;
				}
				else{
					System.out.println("The ontology is non-local");
					Set nlocal = checker.getNonLocalAxioms(foreign);
					nloc = nloc + 1;
					OWLOntology ont = checker.createNonLocalPart(nlocal);
					String result = checker.renderNonLocal(ont);
					String usedForeign = checker.renderForeignInNonLocal(ont,foreign);
					
					System.out.println(result);
					System.out.println("The ontology has " + nlocal.size() + " non-local axioms") ;
					System.out.println("The foreign entities in the non-local part are " + usedForeign);
				}
			}
			System.out.println("-------------------------------------------");
			
		}	
		total = loc + nloc;
		System.out.println("Total number of ontologies: " + total);
		System.out.println("Total number of LOCAL ontologies: " + loc);
		System.out.println("Total number of NON-LOCAL ontologies: " + nloc);
		System.out.println("Total number of NON-HIERARCHICAL integrations: " + cycle);
		
	}
	
	
		
	public void writeLogFile() throws Exception {
		FileWriter fw = new FileWriter(new File("debugEvalLog.txt"));
		fw.write(logFile);
		fw.close();
		System.out.println("Written log: debugEvalLog.txt");
	}
	
	
	
	
	
	
	public void cleanLog() throws Exception {
		BufferedReader in = new BufferedReader(new FileReader(new File("LocalityTest.txt")));
		String line = null; 
		String newLog = "";
		swoopModel.setShowQNames(false);
	    while (( line = in.readLine()) != null) {
	     	 if (line.indexOf("(")!=-1) {  
			 	String token1 = line.substring(line.indexOf("(")+1, line.indexOf(","));
			 	String token2 = line.substring(line.indexOf(",")+1, line.indexOf(")"));
			 	line = line.replaceAll(token1, swoopModel.shortForm(new URI(token1)));
			 	line = line.replaceAll(token2, swoopModel.shortForm(new URI(token2)));
	     	 }
	     	 newLog += line + NEWLINE;
	    }
	    logFile = newLog;
	    System.out.print("Cleant file..");
	    this.writeLogFile();
	}
	
	
	
	
	
	public static void main(String[] args) {
		try {
			testLocalityExtended t = new testLocalityExtended();
			t.runTest();			
						
//			System.out.println(t.logFile);
		} 
		catch (Exception e) {
			e.printStackTrace();
		}		
	}
}
