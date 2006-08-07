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
import org.mindswap.swoop.utils.owlapi.ImportChange;
import org.mindswap.swoop.utils.owlapi.KRSSConverter;
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
public class TestSegmentedClassification {
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
		//String fileName1 = "C:/ontologies/galen.lisp";
		String fileName1 = "C:/ontologies/default-GALEN1.owl";
	    ontFile1 = new File(fileName1);
	    String filePath = ontFile1.toURI().toString();
        URI uri1 = new URI(filePath);
        System.out.println("Parsing");
        //Reading an ontology in KRSS
        //KRSSConverter converter = new KRSSConverter();
        //OWLOntology ontology = converter.readTBox( fileName1);
        OWLOntology ontology = swoopModel.addOntology(uri1);
        System.out.println("Done Parsing");
        
        URI uriOriginal = ontology.getLogicalURI();
		//
	    
        //Create Segmentation Object
	    Segmentation seg = new Segmentation(ontology);
	    Set allClasses = ontology.getClasses();  
		Set allProperties = ontology.getObjectProperties();
		allProperties.addAll(ontology.getDataProperties());
		Set allEntities = new HashSet();
		allEntities.addAll(allClasses);
		allEntities.addAll(allProperties);
	    
		System.out.println("Getting the axioms in the ontology");
		Set allAxioms = seg.getAxiomsInOntology(ontology);
		System.out.println("Total number of axioms in the Ontology: " + allAxioms.size());
		System.out.println("Getting signature of axioms");
		Map axSignature = seg.axiomsToSignature(allAxioms);
		System.out.println("Got signature of the axioms");
		System.out.println("Creating Map from concept names to axioms");
		Map sigToAxioms = seg.signatureToAxioms(allAxioms, allEntities);
		System.out.println("DONE");
		
		
		System.out.println("Creating Signature Dependency Map");
		Map signatureTable = new HashMap();
		signatureTable = seg.computeSignatureDependenciesOptimized(allAxioms, sigToAxioms, axSignature, allClasses);
		System.out.println("DONE Creating Signature Dependency Map");
		System.out.println("********");
		
		
		System.out.println("Getting Large Modules");
		double threshold = (allClasses.size())/20; 
		Map largeModules = new HashMap();
		largeModules = seg.getLargeModules(signatureTable, threshold);
		System.out.println("DONE Getting Large Modules");
		System.out.println("Original number of large modules: " + largeModules.keySet().size());
		System.out.println("********");
		System.out.println("Merging Large Modules");
		//Maximum size of the merged modules
		double maxSize = 5*threshold;
		signatureTable = seg.mergeLargeModules(largeModules,signatureTable, maxSize);
		System.out.println("DONE Merging Large Modules");
		System.out.println("Modules remaining: " + signatureTable.keySet().size());
		System.out.println("********");
		
		
		
		System.out.println("Pruning modules");
		signatureTable =seg.pruneModules(signatureTable);
		System.out.println("Done pruning");
		System.out.println("Number of modules after pruning: " + signatureTable.keySet().size() );
		System.out.println("********");
		System.out.println("Classifying Modules " );
		//Set allModules = new HashSet();
		Taxonomy fullTaxonomy = new Taxonomy();
		Taxonomy fullRoleTaxonomy = new Taxonomy();
		Iterator iter = signatureTable.keySet().iterator();
		int j = 0;
		int nlargeModules = 0;
		Set TaxonomySet = new HashSet();
		Set RoleTaxonomySet = new HashSet();
		//boolean b = false;
		while (iter.hasNext()){
			j++;
			OWLEntity ent = (OWLEntity)iter.next();
			Set sigModule = new HashSet();
			sigModule = (Set)signatureTable.get(ent);
			Set axiomsInModule = new HashSet();
			axiomsInModule =  seg.getModuleFromSignature(allAxioms,sigModule,axSignature);
			URI uriModule= new URI("http://www.mindswap.org/testModule" + j +".owl");
			System.out.println("Getting module");
			OWLOntology ont = seg.getOntologyFromAxioms(axiomsInModule, uriModule);
			
			System.out.println("Classifying module");
			PelletReasoner reasoner = new PelletReasoner();
			System.out.println("Modules classified: " + j );
			reasoner.setOntology(ont);
			Taxonomy current = new Taxonomy(); 
			current = reasoner.getTaxonomy();
			/*
			if(axiomsInModule.size()>500){
				nlargeModules++;
				String path1 = "C:/ontologies/classTree.txt";
				File wkspcFile1 = new File(path1);
				ObjectOutputStream outs = new ObjectOutputStream(
						new FileOutputStream(wkspcFile1));
				OutputFormatter output = new OutputFormatter(outs, false);
			    current.print(output);
			   	b= true;
			}
			*/
			TaxonomySet.add(current);
			Taxonomy currentRoleTax = new Taxonomy();
			currentRoleTax = reasoner.getRoleTaxonomy();
			RoleTaxonomySet.add(currentRoleTax);
		}
		
		System.out.println("END classification");
		System.out.println("Number of large modules :" + nlargeModules);
		
		System.out.println("Recomposing class tree");
		int progress = 0;
		while(TaxonomySet.size()>1){
			Set TaxSetAux = new HashSet();
			Iterator it = TaxonomySet.iterator();
			while(it.hasNext()){
				Taxonomy tax1 = (Taxonomy)it.next();
				Taxonomy tax2 = new Taxonomy();
				if(it.hasNext())
					tax2 = (Taxonomy)it.next();
				Taxonomy auxTax = tax1.merge(tax2);
				TaxSetAux.add(auxTax);
			}
			TaxonomySet = new HashSet();
			TaxonomySet.addAll(TaxSetAux);
			System.out.println(TaxonomySet.size());
		}
		Iterator i = TaxonomySet.iterator();
		while(i.hasNext()){
			Taxonomy tax = (Taxonomy)i.next();
			Taxonomy auxTax = new Taxonomy();
			auxTax = fullTaxonomy.merge(tax);
			fullTaxonomy = (Taxonomy)auxTax.clone();
		}
		
		System.out.println("Recomposing property tree");
		progress = 0;
		while(RoleTaxonomySet.size()>1){
			Set TaxSetAux = new HashSet();
			Iterator it = RoleTaxonomySet.iterator();
			while(it.hasNext()){
				Taxonomy tax1 = (Taxonomy)it.next();
				Taxonomy tax2 = new Taxonomy();
				if(it.hasNext())
					tax2 = (Taxonomy)it.next();
				Taxonomy auxTax = tax1.merge(tax2);
				TaxSetAux.add(auxTax);
			}
			RoleTaxonomySet = new HashSet();
			RoleTaxonomySet.addAll(TaxSetAux);
			System.out.println(RoleTaxonomySet.size());
		}
		Iterator k = RoleTaxonomySet.iterator();
		while(k.hasNext()){
			Taxonomy tax = (Taxonomy)k.next();
			Taxonomy auxTax = new Taxonomy();
			auxTax = fullRoleTaxonomy.merge(tax);
			fullRoleTaxonomy = (Taxonomy)auxTax.clone();
		}
		

		
		
		System.out.println("********");
		System.out.println("COMPLETE CLASSIFICATION TREE");
		System.out.println("********");
		fullTaxonomy.print();
		System.out.println("********");
		System.out.println("ROLE HIERARCHY:");
		System.out.println("********");
		fullRoleTaxonomy.print();
		System.out.println("CHECK CORRECTNESS");
		System.out.println("Classifying original ontology");
		PelletReasoner reasoner = new PelletReasoner();
		reasoner.setOntology(ontology);
		Taxonomy original = reasoner.getTaxonomy();
		System.out.println("Number of discrepancies: " + original.compareTaxonomy(fullTaxonomy));
	
		System.out.println("Writting classification to file");
		String path1 = "C:/ontologies/classTree.txt";
		String path2 = "C:/ontologies/roleTree.txt";
		File wkspcFile1 = new File(path1);
		File wkspcFile2 = new File(path2);
		ObjectOutputStream outs = new ObjectOutputStream(
				new FileOutputStream(wkspcFile1));
		OutputFormatter output = new OutputFormatter(outs, false);
	    fullTaxonomy.print(output);
	    ObjectOutputStream outs2 = new ObjectOutputStream(
				new FileOutputStream(wkspcFile2));
		OutputFormatter output2 = new OutputFormatter(outs2, false);
	    fullTaxonomy.print(output2);
		
	
	}
	
	
}
