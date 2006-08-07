package org.mindswap.swoop.refactoring;
import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.utils.owlapi.CorrectedRDFRenderer;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDisjointClassesAxiom;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.change.AddClassAxiom;
import org.semanticweb.owl.model.change.AddDomain;
import org.semanticweb.owl.model.change.AddEntity;
import org.semanticweb.owl.model.change.AddIndividualClass;
import org.semanticweb.owl.model.change.AddObjectPropertyRange;
import org.semanticweb.owl.model.change.AddSuperClass;
import org.semanticweb.owl.model.change.ChangeVisitor;
import org.semanticweb.owl.model.change.OntologyChange;

/*
 * Created on May 10, 2004
 */

/**
 * @author Evren Sirin
 */
public class TestRefactoring {
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
		     new URI("http://www.mindswap.org/dav/ontologies/sweet.owl");
		    
		Map linkExpressivity = new HashMap();    
		System.out.println("Refactoring " + uri);	
		String EconnExpressivity = "C(";
		int linkProperties = 0;
		   	
		SwoopModel swoopModel = new SwoopModel();
//	    swoopModel.setDefaultReasoner(new SwoopToldReasoner());
	    
		/*
		File ontFile1 = null; 
		String fileName1 = "C:/nciOncology.owl";
	    
		ontFile1 = new File(fileName1);
	    String filePath = ontFile1.toURI().toString();
        URI uri1 = new URI(filePath);
		//This ontology is the source ontology
        OWLOntology ontology = swoopModel.addOntology(uri1);
     */
		OWLOntology ontology = swoopModel.addOntology(uri);
		swoopModel.getReasoner().setOntology(ontology);

		System.out.println("Number of classes " + ontology.getClasses().size());
		System.out.println("Number of Object Properties " + ontology.getObjectProperties().size());
		System.out.println("Number of Datatype Properties " + ontology.getDataProperties().size());
		System.out.println("Number of Individuals " + ontology.getIndividuals().size());
		
		
		AutoEconnPartitioning suggestions = new AutoEconnPartitioning(null, swoopModel, ontology, true);
		
		linkExpressivity = suggestions.getLinkExpressivity();
		
		// pass applychanges?, debug?, saveToDisk?(all boolean)
		suggestions.findPartitions( true, false, false, false);
		
		System.out.println("Expressivity of the original Ontology " + suggestions.getSourceExpressivity());
		
		String links = "";
		if(linkExpressivity.containsKey("Functionality")){
			links+="F";
		}
		else{
		  if(linkExpressivity.containsKey("Cardinality"))
			links+="N";
		}
		if(linkExpressivity.containsKey("Inverse"))
		   links+="I";
		
		// display class trees for debugging
		int counting = 1;
		HashMap modules = new HashMap(); // key - partition hashcode, value - no. of modules
		for(Iterator i = suggestions.partitions.iterator(); i.hasNext(); ) {
		    OWLOntology partition = (OWLOntology) i.next();
		    StringWriter st = new StringWriter();
	 		CorrectedRDFRenderer rdfRenderer = new CorrectedRDFRenderer();
	 		rdfRenderer.renderOntology(partition, st);
	 		String partitionSource = st.toString();
	 		// save partition to disk if partition size is large
	 		String fName = "partitions/"+getPartitionQName(partition.getURI());
	 		FileWriter writer = new FileWriter(new File(fName));
	        writer.write(partitionSource);
	        writer.close();
	 	}
	 	//Recover source ontology with disjoint Statements
		System.out.println(" Adding disjoint and different-from statements");
		OWLOntology sourceModified = addDisjoints(suggestions.partitions,swoopModel.addOntology(uri));
		//Save it to disk
		StringWriter st = new StringWriter();
 		CorrectedRDFRenderer rdfRenderer = new CorrectedRDFRenderer();
 		rdfRenderer.renderOntology(sourceModified, st);
 		String partitionSource = st.toString();
 		// save partition to disk if partition size is large
 		String fName = "partitions/" + "Modified" +getPartitionQName(sourceModified.getURI());
 		FileWriter writer = new FileWriter(new File(fName));
 		System.out.println("File " + fName + " saved successfully");
        writer.write(partitionSource);
        writer.close();
 	
	}
	
	/** This method is for evaluating Pellet wrt Econns
	 * We generate all the explicit disjoint statements
	 * We pass the result of a partitioning as a List of ontologies and the source ontology
	 */
	
	private static OWLOntology addDisjoints(List partitions, OWLOntology source) throws OWLException, URISyntaxException{
		List changeList = new ArrayList();
		Map tops = new HashMap();
		Set topClasses = new HashSet();
		int count = 0;
		OWLClass top;
		for(Iterator j = partitions.iterator(); j.hasNext();){
			count++;
			OWLOntology partition = (OWLOntology)j.next();
			String claStr = "Top" + count;
			claStr =  partition.getLogicalURI()+ "#" + claStr;
            URI claURI = new URI(claStr);
            top = partition.getOWLDataFactory().getOWLClass(claURI);
            tops.put(partition,top);
            topClasses.add(top);
		 	OntologyChange oc = new AddEntity(source,top,null);
		 	System.out.println("Adding  top class " + top.getURI().toString());
		 	changeList.add(oc);
		}
		
		for(Iterator i = partitions.iterator(); i.hasNext();){
			OWLOntology partition = (OWLOntology)i.next();
			top = (OWLClass)tops.get(partition);
			Set classSet = partition.getClasses();
			Set indSet = partition.getIndividuals();
			//Remove extra classes 
			Set foreign = partition.getForeignEntities().keySet();
			classSet.removeAll(foreign);
			indSet.removeAll(foreign);
		 	for(Iterator k = classSet.iterator(); k.hasNext(); ){
		 		OWLClass clazz = (OWLClass)k.next();
		 		OWLClass cl = source.getClass(clazz.getURI());
		 		boolean b = false;
		 		if(clazz.getSuperClasses(partition).isEmpty())
		 			b=true;
		 		if(!b){
		 			b= true;
		 		    for(Iterator t= clazz.getSuperClasses(partition).iterator(); t.hasNext(); ){
		 			   OWLDescription desc = (OWLDescription)t.next();
		 			   if(desc instanceof OWLClass)
		 			   	b=false;
		 		 }
		 		}
		 		if(b){
		 			OntologyChange oc2 = new AddSuperClass(source,cl,top,null);
		 			changeList.add(oc2);
		 		}
		 	}
		 	for(Iterator t = partition.getObjectProperties().iterator(); t.hasNext(); ){
		 		OWLObjectProperty prop = (OWLObjectProperty)t.next();
		 		OWLObjectProperty p = source.getObjectProperty(prop.getURI());
		 		OntologyChange oc3 = new AddDomain(source,p,top,null);
		 		changeList.add(oc3);
		 		if(!prop.isLink()){
		 			OntologyChange oc0 = new AddObjectPropertyRange(source,p,top,null);
			 		changeList.add(oc0);
		 		}
		 		else{
		 			URI u = prop.getLinkTarget();
		 			for(Iterator r = tops.keySet().iterator(); r.hasNext();){
		 			    OWLOntology o = (OWLOntology)r.next();
		 			    if(o.getURI().equals(u)){
		 			    	OWLClass foreignTop = (OWLClass)tops.get(o);
		 					OntologyChange oc0 = new AddObjectPropertyRange(source,p,foreignTop,null);
					 		changeList.add(oc0);
						}
		 			}
		 		}
		 	}
			for(Iterator y = partition.getDataProperties().iterator(); y.hasNext(); ){
				OWLDataProperty prop = (OWLDataProperty)y.next();
				OWLDataProperty p = source.getDataProperty(prop.getURI());
				OntologyChange oc9 = new AddDomain(source,p,top,null);
				changeList.add(oc9);
			}	
			for(Iterator m = indSet.iterator(); m.hasNext(); ){
				OWLIndividual ind = (OWLIndividual)m.next();
				OWLIndividual indiv = source.getIndividual(ind.getURI());
				OntologyChange oc8 = new AddIndividualClass(source,indiv,top,null);
				changeList.add(oc8);
			}
			//Add disjoint axioms between tops
		
			OWLDisjointClassesAxiom disAxiom = source.getOWLDataFactory().getOWLDisjointClassesAxiom(topClasses);
			OntologyChange o = new AddClassAxiom(source,disAxiom,null);
			changeList.add(o);

		}
		for(Iterator h = changeList.iterator(); h.hasNext(); ){
			OntologyChange oc = (OntologyChange)h.next();
			oc.accept((ChangeVisitor)source);
			
		}
		return source;
	}
	
		
	 private static String getPartitionQName(URI uri) {
	  	String uriStr = uri.toString();
	  	return uriStr.substring(uriStr.lastIndexOf("/")+1, uriStr.length());
	  }
	 

}
