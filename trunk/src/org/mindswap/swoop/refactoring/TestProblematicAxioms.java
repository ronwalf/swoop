package org.mindswap.swoop.refactoring;

import java.io.File;
import java.net.URI;
import java.util.Iterator;
import java.util.Set;

import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.reasoner.PelletReasoner;
import org.semanticweb.owl.model.OWLClassAxiom;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLSubClassAxiom;

public class TestProblematicAxioms {

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
			 new URI("http://www.mindswap.org/ontologies/tambis-full.owl"); 
		   	   	
	     
		    SwoopModel swoopModel = new SwoopModel();
				    
			File ontFile1 = null; 
			String fileName1 = "C:/ontologies/ProblematicAxioms/ProblematicBottom1.owl";
		    ontFile1 = new File(fileName1);
		    String filePath = ontFile1.toURI().toString();
	        URI uri1 = new URI(filePath);
	        System.out.println("Parsing");
			OWLOntology ontology = swoopModel.addOntology(uri1);
			System.out.println("Done Parsing");
			
			//Set allAxioms = ontology.getClassAxioms();
			
			Segmentation seg = new Segmentation(ontology);
			//System.out.println("Getting the axioms in the ontology");
			Set allAxioms = seg.getAxiomsInOntology(ontology);
			System.out.println("Total number of axioms in the Ontology: " + allAxioms.size());
			System.out.println("done");
			
			PelletReasoner reasoner = new PelletReasoner();
			Iterator i = allAxioms.iterator();
			
			while(i.hasNext()){
				OWLClassAxiom axiom = (OWLClassAxiom)i.next();			
				if (axiom instanceof OWLSubClassAxiom){
					OWLDescription sup = ((OWLSubClassAxiom)axiom).getSuperClass();
					OWLDescription sub = ((OWLSubClassAxiom)axiom).getSubClass();
					if(reasoner.isSubClassOf(sub,sup)){
							System.out.println("The subclass axiom is a tautology");
					}
					
				}
				
				if (axiom instanceof OWLEquivalentClassesAxiom){
					Set eqclasses = ((OWLEquivalentClassesAxiom)axiom).getEquivalentClasses();
					Iterator iter = eqclasses.iterator();
					if(eqclasses.size() == 2){
						OWLDescription first = (OWLDescription)iter.next();
						OWLDescription second = (OWLDescription)iter.next();
						if(seg.isObviousEquivalence((OWLEquivalentClassesAxiom)axiom)){
							System.out.println("The subclass axiom is an OBVIOUS tautology");
						}
						else{
							if(reasoner.isEquivalentClass(first, second))
								System.out.println("The Equivalence Axiom is a Tautology");
							else
								System.out.println("The Equivalence Axiom is not a Tautology");
						}
					}
				}
		
			}
		}

}
