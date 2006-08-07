package org.mindswap.swoop.refactoring;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.reasoner.PelletReasoner;
import org.mindswap.swoop.utils.owlapi.CorrectedRDFRenderer;
import org.mindswap.swoop.utils.owlapi.ImportChange;
import org.mindswap.swoop.utils.owlapi.KRSSConverter;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLClassAxiom;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.change.AddEntity;
import org.semanticweb.owl.model.change.ChangeVisitor;

/*
 * Created on May 10, 2004
 */

/**
 * @author Bernardo Cuenca Grau
 */
public class TestSegmentation {
	public static void main(String[] args)  {
		try {
			run(args);
		} catch (Throwable e) {
			System.out.println("********* error **********");
			printStackTrace(e);
		}
		finally {
			System.out.println("finished");
		}
	}
	
	public static void printStackTrace(Throwable e) {
		StackTraceElement[] ste = e.getStackTrace();
		
		System.out.println(e);
		System.out.println("Stack length: " + ste.length);
		if(ste.length > 100) {
			for(int i = 0; i < 10; i++)
				System.out.println("   " + ste[i]);
			System.out.println("   ...");
			for(int i = ste.length - 60; i < ste.length; i++)
				System.out.println("   " + ste[i]);
		}
		else {
			for(int i = 0; i < ste.length; i++)
				System.out.println("   " + ste[i]);			
		}
	}
	
	public static void run(String[] args) throws Exception {
		URI uri = (args.length > 0) 
	    ? new URI(args[0])
	    : //new URI("http://www.cs.man.ac.uk/~horrocks/OWL/Ontologies/tambis-full.owl");
		 //new URI("http://protege.stanford.edu/plugins/owl/owl-library/koala.owl");
	     //new URI("http://www.purl.org/net/ontology/beer");
	     //new URI("http://www.mpi-sb.mpg.de/~ykazakov/default-GALEN.owl");   
		 new URI("http://www.mpi-sb.mpg.de/~ykazakov/default-GALEN.owl"); 
	    
		   	
     
	    SwoopModel swoopModel = new SwoopModel();
		
	    
	    
		File ontFile1 = null; 
		String fileName1 = "C:/ontologies/galen.lisp";
	    ontFile1 = new File(fileName1);
	    String filePath = ontFile1.toURI().toString();
        URI uri1 = new URI(filePath);
        System.out.println("Parsing");
        //Reading an ontology in KRSS
        KRSSConverter converter = new KRSSConverter();
        OWLOntology ontology = converter.readTBox( fileName1);
        
        //OWLOntology ontology = swoopModel.addOntology(uri1);
		//
        System.out.println("Done Parsing");
		
	    
	    
		
	    //OWLOntology ontology = swoopModel.addOntology(uri);
		
	
		//Set ontSig = ontology.getClasses();
		
		Segmentation seg = new Segmentation(ontology);
		//String pathAux =  "C:/ontologies/galen.owl";
		//System.out.println("Saving Ontology");
		//seg.saveOntologyToDisk(ontology, pathAux);
		
		System.out.println("Getting the axioms in the ontology");
		Set allAxioms = seg.getAxiomsInOntology(ontology);
		System.out.println("Total number of axioms in the Ontology: " + allAxioms.size());
		System.out.println("Getting signature of axioms");
		Map axSignature = seg.axiomsToSignature(allAxioms);
		System.out.println("Got signature of the axioms");
		
		//URI uriAux = new URI("http://www.co-ode.org/ontologies/galen#GrandMultiparas");
		//Set sig = new HashSet();
		//OWLClass cl = ontology.getClass(uriAux);		
		//sig.add(cl);
		//Set result = seg.getModule(allAxioms,sig,axSignature);
		//OWLOntology module = seg.getOntologyFromAxioms(result);
		//System.out.println("done");
		///String path = "C:/ontologies/testSegmentation/testGalenFull"+".owl";
		//seg.saveOntologyToDisk(module, path);
		
		Set processed = new HashSet();
		
		int j = 0;
		int skipped = 0;
		Iterator i = ontology.getClasses().iterator();
		
		while(i.hasNext()){
			System.out.println("Modules extracted: " + j);
			j++;
			OWLClass cl = (OWLClass)i.next();
			if(!processed.contains(cl) && j > 902){
					
				Set testSet = new HashSet();
				testSet.add(cl);
				processed.add(cl);
				System.out.println("Starting with class: " + cl.getURI().toString());
				//get the set of axioms in the module
				Set result = seg.getModule(allAxioms,testSet,axSignature);
				System.out.println("NUMBER of axioms in module for " + swoopModel.shortForm(cl.getURI()) + ":  " + result.size());
				
				
				OWLOntology module = seg.getOntologyFromAxioms(result);
				//if the module is empty, then at least add the class
				if(result.isEmpty()){
					AddEntity add = new AddEntity(module, cl, null);
					add.accept((ChangeVisitor) module);
				}
				Set moduleSignature = new HashSet();
				moduleSignature = seg.getOntologySignature(result,module);
				processed.addAll(moduleSignature);
				
				
				PelletReasoner reasoner = new PelletReasoner();
				reasoner.setOntology(module);
				OWLDataFactory df = ontology.getOWLDataFactory();
				Set unsat = new HashSet();
				unsat = reasoner.subClassesOf(df.getOWLNothing());
				if(unsat.isEmpty()){
					System.out.println("The module for the class  " + cl.getURI() + "is satisfiable");
					String path = "C:/ontologies/testSegmentation/testGalen"+j+".owl";
					seg.saveOntologyToDisk(module, path);
				}
				else{
					System.out.println("The module contains unsatisfiable classes");
					String path = "C:/ontologies/testSegmentation/testGalenUnsat"+j+".owl";
					seg.saveOntologyToDisk(module, path);
				}
				
				
				/*
				if(reasoner.isConsistent(cl)){
					System.out.println("The Class " + cl.getURI() + "is satisfiable");
					String path = "C:/ontologies/testSegmentation/testGalen"+j+".owl";
					seg.saveOntologyToDisk(module, path);
				}
				else{
					System.out.println("The Class " + cl.getURI() + "is UNSATISFIABLe");
					String path = "C:/ontologies/testSegmentation/testGalenUnsat"+j+".owl";
					seg.saveOntologyToDisk(module, path);
				}
			*/
			}
		
			else{
				System.out.println("I am skipping this class");
				skipped++;
			}
			
			System.out.println("CONCEPTS PROCESSED SO FAR: " + processed.size() );
		    		
		}
		System.out.println("I have skipped " + skipped + " classes");
	
	}
}
