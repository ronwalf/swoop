package org.mindswap.swoop.refactoring;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


import org.mindswap.pellet.output.OutputFormatter;
import org.mindswap.pellet.taxonomy.Taxonomy;
import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.reasoner.PelletReasoner;
import org.mindswap.swoop.utils.owlapi.CorrectedRDFRenderer;
import org.mindswap.swoop.utils.owlapi.DefaultShortFormProvider;
import org.mindswap.swoop.utils.owlapi.ImportChange;
import org.mindswap.swoop.utils.owlapi.KRSSConverter;
import org.semanticweb.owl.io.ShortFormProvider;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLClassAxiom;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLPropertyAxiom;
import org.semanticweb.owl.model.change.AddEntity;
import org.semanticweb.owl.model.change.AddPropertyAxiom;
import org.semanticweb.owl.model.change.ChangeVisitor;
import org.semanticweb.owl.model.change.SetLogicalURI;

/*
 * Created on May 10, 2004
 */

/**
 * @author Bernardo Cuenca Grau
 */
public class TestSaveModules {
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
		
		 new URI("http://www.mpi-sb.mpg.de/~ykazakov/default-GALEN.owl"); 
	    
		SwoopModel swoopModel = new SwoopModel();
		
	    
	    //Load Input
		File ontFile1 = null; 
		
		//Dual Concepts and Dual Roles
		boolean dualConcepts = false;
		boolean dualRoles = false;
		//
		
		//String fileName1 = "C:/ontologies/galen.lisp";
		String fileName1 = "C:/ontologies/Snomed/SnoMed-krss/snomedct-20050209.krss.txt";
	    ontFile1 = new File(fileName1);
	    String filePath = ontFile1.toURI().toString();
        URI uri1 = new URI(filePath);
        System.out.println("Parsing");
        //Reading an ontology in KRSS
        KRSSConverter converter = new KRSSConverter();
        OWLOntology ontology = converter.readTBox( fileName1);
        //OWLOntology ontology = swoopModel.addOntology(uri1);
        System.out.println("Done Parsing");
        
        URI uriOriginal = ontology.getLogicalURI();
		//
	    
        //Create Segmentation Object
	
        Segmentation seg = new Segmentation(ontology,dualConcepts,dualRoles);
	    //
	    Set allClasses = ontology.getClasses();  
		Set allProperties = ontology.getObjectProperties();
		allProperties.addAll(ontology.getDataProperties());
		Set allEntities = new HashSet();
		allEntities.addAll(allClasses);
		allEntities.addAll(allProperties);
	    
		System.out.println("Getting the axioms in the ontology");
		Set allAxioms = seg.getAllAxioms();
		System.out.println("Total number of axioms in the Ontology: " + allAxioms.size());
		Map axSignature = seg.getAxiomsToSignature();
		Map sigToAxioms = seg.getSignatureToAxioms();
		System.out.println("DONE");
		
		
		System.out.println("Creating Signature Dependency Map");
		Map signatureTable = new HashMap();
		boolean save = true;
		signatureTable = seg.computeSignatureDependenciesOptimized(allAxioms, sigToAxioms, axSignature, allClasses, save);
		System.out.println("DONE Creating Signature Dependency Map");
		System.out.println("********");
		
		/*		
		System.out.println("Pruning modules");
		signatureTable =seg.pruneModules(signatureTable);
		System.out.println("Done pruning");
		System.out.println("Number of modules after pruning: " + signatureTable.keySet().size() );
		System.out.println("********");
		System.out.println("Classifying Modules " );
		*/
		
		/*
		Iterator iter = signatureTable.keySet().iterator();
		int j = 0;
		while (iter.hasNext()){
			j++;
			OWLEntity ent = (OWLEntity)iter.next();
			Set sigModule = new HashSet();
			sigModule = (Set)signatureTable.get(ent);
			Set axiomsInModule = new HashSet();
			axiomsInModule =  seg.getModuleFromSignature(allAxioms,sigModule,axSignature);
			//URI uriModule= new URI("http://www.mindswap.org/testModule" + j +".owl");
			URI uriModule= new URI("http://" + shortFormProvider.shortForm(ent.getURI()) +".owl" );
			System.out.println("Getting module");
			OWLOntology ont = seg.getOntologyFromAxioms(axiomsInModule, uriModule);
			System.out.println("Module size (number of classes):" + ont.getClasses().size());
			System.out.println("Saving module");
			ShortFormProvider shortFormProvider = new DefaultShortFormProvider();
			String path = "C:/ontologies/galenFull/" + shortFormProvider.shortForm(ontology.getURI()) + "-" + shortFormProvider.shortForm(ent.getURI()) +".owl";
			seg.saveOntologyToDisk(ont,path);
			System.out.println("Saved Module: " + j);
		}
		*/
	
	}
	
	
}
