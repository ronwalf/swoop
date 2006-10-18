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

package org.mindswap.swoop.refactoring;

import java.io.File;
import java.io.FileWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.mindswap.pellet.utils.Timer;
import org.mindswap.swoop.SwoopFrame;
import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.debugging.MUPSFinder;
import org.mindswap.swoop.reasoner.PelletReasoner;
import org.mindswap.swoop.utils.owlapi.AxiomCollector;
import org.mindswap.swoop.utils.owlapi.CorrectedRDFRenderer;
import org.mindswap.swoop.utils.owlapi.OWLOntBuilder;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.change.AddImport;
import org.semanticweb.owl.model.change.ChangeVisitor;
import org.semanticweb.owl.model.change.OntologyChange;
import org.semanticweb.owl.model.change.RemoveImport;
import org.semanticweb.owl.model.change.SetLinkTarget;
import org.semanticweb.owl.model.helper.OWLBuilder;

/**
 * @author bernardo
 *
 */
public class AutoEconnPartitioning {

	private OWLOntology ontology;
	//private SwoopReasoner reasoner;
	private SwoopModel swoopModel;
	private SwoopFrame swoopHandler;
	ArrayList changes;
	Set movedClasses;
	Map targetExpressivity;
	String sourceExpressivity;
	Map linkExpressivity;
	Map linksToSource; // Key: Link properties generated in partitioning steps
	                   //      from target to source
	                   // Object: The set of class descriptions that are ``bound'' to that link prop
	Map boundedInverses;
	boolean test;
	
	public List partitions;
	Timer timers = new Timer("Total");
	Timer timers2 = new Timer("Build Indices");
	Timer timers3 = new Timer("VerifyLinks");
	Timer timers4 = new Timer("RunStateMachine");
	Timer timers5 = new Timer("MakeChanges");
	Timer timers6 = new Timer("MakeChanges if not apply");
	
	
	public AutoEconnPartitioning(SwoopFrame swoopHandler, SwoopModel model, OWLOntology onto, boolean test) {
		this.swoopHandler = swoopHandler;
		this.swoopModel = model;
		this.ontology = onto;
		this.changes = new ArrayList();
		this.movedClasses = new HashSet();
		this.targetExpressivity = new HashMap();
		this.sourceExpressivity = null;
	 	this.linkExpressivity = new HashMap();
	 	this.linksToSource = new HashMap();
	 	this.boundedInverses = new HashMap();
	 	this.test = test;
	}
	
     public void findModule(OWLEntity entity) {
     	
     	try {
     		List partitions = this.findPartitions(true, false, false, true);
     		OWLOntology baseOnt = null;
     		for (Iterator iter = partitions.iterator(); iter.hasNext();) {
     			OWLOntology ont = (OWLOntology) iter.next();
     			if (swoopModel.getEntity(ont, entity.getURI(), false)!=null) {
     				baseOnt = ont;
     				break;
     			}
     		}
     		
     		if (baseOnt!=null) {
     			
     			boolean isModule = false;
     			
     			System.out.println("baseont:"+baseOnt);
     			// expand finding all nodes connected by incoming
     			// or outgoing link dependencies
     			Set totalOnts = new HashSet();
     			totalOnts.add(baseOnt);
     			Set temp = new HashSet();
     			while (!temp.equals(totalOnts)) {
     				temp = new HashSet(totalOnts);
     				for (Iterator iter = new HashSet(totalOnts).iterator(); iter.hasNext(); ) {
     					OWLOntology ont = (OWLOntology) iter.next();
     					Set foreign = ont.getForeignOntologies();
     					for (Iterator iter2 = foreign.iterator(); iter2.hasNext();) {
     						URI uri = (URI) iter2.next();
     						for (Iterator iter3 = partitions.iterator(); iter3.hasNext();) {
     							OWLOntology par = (OWLOntology) iter3.next();
     							if (par.getURI().equals(uri)) {
     								totalOnts.add(par);
     							}
     						}
     					}
     				}
     				for (Iterator iter = new HashSet(totalOnts).iterator(); iter.hasNext();) {
     					OWLOntology ont = (OWLOntology) iter.next();
     					for (Iterator iter2 = partitions.iterator(); iter2.hasNext();) {
     	     				OWLOntology par = (OWLOntology) iter2.next();
     	     				Set foreign = par.getForeignOntologies();
     	     				for (Iterator iter3 = foreign.iterator(); iter3.hasNext();) {
     	     					URI uri = (URI) iter3.next();								
     	     					if (uri.equals(ont.getURI())) totalOnts.add(par);
     	     				}
     					}
     				}
     			}
     			
     			// foreign onts are dependencies (outgoing links)
//     			Set outOnts = new HashSet();
//     			outOnts.add(baseOnt);
//     			Set temp = new HashSet();
//     			while (!outOnts.equals(temp)) {
//     				temp = new HashSet(outOnts);
//     				for (Iterator iter = new HashSet(outOnts).iterator(); iter.hasNext();) {
//     					OWLOntology ont = (OWLOntology) iter.next();
//     					Set foreign = ont.getForeignOntologies();
//     					for (Iterator iter2 = foreign.iterator(); iter2.hasNext();) {
//     						URI uri = (URI) iter2.next();
//     						for (Iterator iter3 = partitions.iterator(); iter3.hasNext();) {
//     							OWLOntology par = (OWLOntology) iter3.next();
//     							if (par.getURI().equals(uri)) {
//     								outOnts.add(par);
//     							}
//     						}
//     					}
//     				}
//     			}
//     			System.out.println("outgoing:"+outOnts);
//     			
//     			// get all onts which point to baseOnt (incoming links)
//     			Set incOnts = new HashSet();
//     			incOnts.addAll(outOnts); //*** 
//     			temp = new HashSet();
//     			while (!incOnts.equals(temp)) {
//     				temp = new HashSet(incOnts);
//     				for (Iterator iter = new HashSet(incOnts).iterator(); iter.hasNext();) {
//     					OWLOntology ont = (OWLOntology) iter.next();
//     					for (Iterator iter2 = partitions.iterator(); iter2.hasNext();) {
//     	     				OWLOntology par = (OWLOntology) iter2.next();
//     	     				Set foreign = par.getForeignOntologies();
//     	     				for (Iterator iter3 = foreign.iterator(); iter3.hasNext();) {
//     	     					URI uri = (URI) iter3.next();								
//     	     					if (uri.equals(ont.getURI())) incOnts.add(par);
//     	     				}
//     					}
//     				}
//     			}
////     			incOnts.remove(baseOnt);
     			System.out.println("total:"+totalOnts);
//     			// check consistency of entire incOnts
     			for (Iterator iter = new HashSet(totalOnts).iterator(); iter.hasNext();) {
     				OWLOntology ont = (OWLOntology) iter.next();
     				// hack! render in abstract syntax since no "foreign" stuff gets inserted
     				org.semanticweb.owl.io.abstract_syntax.Renderer ASRenderer = 
     					new org.semanticweb.owl.io.abstract_syntax.Renderer();
     				StringWriter st = new StringWriter();
     				ASRenderer.renderOntology(ont, st);
     				StringReader read = new StringReader(st.toString());
     				OWLOntology newOnt = swoopModel.loadOntologyInAbstractSyntax(read, ont.getURI());
     				totalOnts.remove(ont);
     				totalOnts.add(newOnt);
     			}
     			// make any one ontology import all the other
     			OWLOntology anyOnt = (OWLOntology) totalOnts.iterator().next();
     			totalOnts.remove(anyOnt);
     			Set changes = new HashSet();
     			for (Iterator iter = totalOnts.iterator(); iter.hasNext();) {
     				OWLOntology ont = (OWLOntology) iter.next();
     				AddImport ai = new AddImport(anyOnt, ont, null);
     				ai.accept((ChangeVisitor) anyOnt);
     				changes.add(new RemoveImport(anyOnt, ont, null));
     			}
     			
     			// check consistency of anyOnt
     			PelletReasoner pellet = new PelletReasoner();
     			pellet.setOntology(anyOnt);
     			if (false) {
     				// return baseOnt + all foreign dependencies
//     				swoopModel.removeOntology(swoopModel.getSelectedOntology().getURI());
     				// undo all import changes just to be safe
     				for (Iterator iter = changes.iterator(); iter.hasNext();) {
     					OntologyChange ch = (OntologyChange) iter.next();
     					ch.accept((ChangeVisitor) ch.getOntology());
     				}
     				isModule = true;     				
     			}
     			else {
     				// get MUPS
     				MUPSFinder mf = new MUPSFinder();
     				mf.useRACER = false;
     				mf.useKAON = false;
     				mf.useTableau = true;
     				System.out.println("calling Pellet");
     				Set sos = mf.getTableauSOS(anyOnt, null);
     				System.out.println("sos size: "+sos.size());
     				List MUPS = new ArrayList();
     				MUPS.add(sos);
     				mf.HSTMUPS(sos, anyOnt, null, MUPS, new ArrayList(), new HashSet(), new HashSet());
     				// check if MUPS intersects baseOnt
     				Set module = this.getModule(baseOnt);
     				for (Iterator iter = MUPS.iterator(); iter.hasNext();) {
     					Set mups = (HashSet) iter.next();
     					Set temp2 = new HashSet(module);
     					temp2.removeAll(mups);
     					if (!(temp2.equals(module))) {
     						isModule = false;
     						break;
     					}
     				}     				
     			}
     			if (isModule) {
     				Set module = this.getModule(baseOnt);
     				// build ontology
     				OWLBuilder b = new OWLBuilder();
     				b.createOntology(new URI("test://module"), baseOnt.getPhysicalURI());
     				OWLOntology ont = b.getOntology();
     				OWLOntBuilder ob = new OWLOntBuilder(ont);
     				ob.addAxiom = true;
     				for (Iterator iter = module.iterator(); iter.hasNext();) {
     					OWLObject axiom = (OWLObject) iter.next();
     					axiom.accept(ob);
     				}
     				swoopModel.addOntology(ob.currentOnt);
     			}
     		}
     	}
     	catch (Exception ex) {
     		ex.printStackTrace();
     	}
     }
 	
     public Set getModule(OWLOntology baseOnt) {
     	Set module = new HashSet();
     	try {
	     	Set totalOnts = new HashSet();
	     	totalOnts.add(baseOnt); 
	     	Set temp = new HashSet();
	     	while (!temp.equals(totalOnts)) {
	     		temp = new HashSet(totalOnts);
	     		for (Iterator iter = new HashSet(totalOnts).iterator(); iter.hasNext(); ) {
 					OWLOntology ont = (OWLOntology) iter.next();
 					Set foreign = ont.getForeignOntologies();
 					for (Iterator iter2 = foreign.iterator(); iter2.hasNext();) {
 						URI uri = (URI) iter2.next();
 						for (Iterator iter3 = partitions.iterator(); iter3.hasNext();) {
 							OWLOntology par = (OWLOntology) iter3.next();
 							if (par.getURI().equals(uri)) {
 								totalOnts.add(par);
 							}
 						}
 					}
 				}
	     	}
	     	for (Iterator iter = totalOnts.iterator(); iter.hasNext();) {
	     		OWLOntology ont = (OWLOntology) iter.next();
	     		AxiomCollector ac = new AxiomCollector(ont);
	     		module.addAll(ac.axiomize(ont));	     		
	     	}
     	}
     	catch (OWLException ex) {
     		ex.printStackTrace();
     	}
        return module;
     }
      	
	 public List findPartitions(boolean applyWithoutConfirm, boolean debugMessages, boolean saveToDisk, boolean doNotAddSwoop) throws OWLException, Exception{

	 	List newPartition = new ArrayList();
	 	OWLOntology source = swoopModel.getSelectedOntology();
	 	OWLClass owlThing = source.getOWLDataFactory().getOWLThing();
	 	Map updatedInverses = new HashMap();

		OWLOntology target = null;
		String trace = " ";
		partitions = new ArrayList();
		movedClasses.add(owlThing);
	 	int count =1;
	 	
	 	//*** start partitioning
	 	System.out.println("start partitioning:"+swoopModel.getTimeStamp());
	 	
	 	// turn OFF checkpointing!!
	 	boolean saveCheckPointSetting = swoopModel.getEnableAutoSaveChkPts();
	 	swoopModel.setEnableAutoSaveChkPts(false, false);
	 	
	 	// initialize EconnIterativePartitioning
	 	//Start timers
	 	timers.start();
	 	timers2.start();
	 	
	 	StringWriter sw =  new StringWriter();
	 	EconnIterativePartitioning iterativeRefactor = new EconnIterativePartitioning(swoopModel);
	 	iterativeRefactor.setOutput(sw);
		iterativeRefactor.setDebug(debugMessages);
		iterativeRefactor.setApply(applyWithoutConfirm);
		iterativeRefactor.init(swoopModel.getSelectedOntology(),target,linksToSource,boundedInverses);
		//Stop timer for computing indices
		timers2.stop();
		// after computing indices (only once), we get expressivity
		sourceExpressivity=iterativeRefactor.getExpressivity();
		
		// iteratively partition!!
		Set auxSet = new HashSet();
		//Set auxSet = SetUtils.union(swoopModel.getReasoner().subClassesOf(owlThing));
	 	auxSet.addAll(source.getClasses());
	 	Iterator k = auxSet.iterator();
	 	while(k.hasNext()){
			OWLClass cla = (OWLClass)k.next();
			
			if(!(movedClasses.contains(cla))){
//				OWLBuilder builder = new OWLBuilder();
				  URI uri;
				  try {
				  	// create uri from current ontology being partitioned
				  	String uriStr = swoopModel.shortForm(source.getURI());
				  	if (uriStr.indexOf(":")>=0) uriStr = uriStr.substring(uriStr.indexOf(":")+1, uriStr.length());
				  	if (uriStr.indexOf(".")>=0) uriStr = uriStr.substring(0, uriStr.lastIndexOf("."));
					uri = new URI("http://www.mindswap.org/"+uriStr+"_partition" +count+".owl");
//					
					target = source.getOWLDataFactory().getOWLOntology(uri, uri);
					OWLDataFactory df = ontology.getOWLDataFactory();
					
					partitions.add(target);
					System.out.println("Using the iterative partitioning algorithm..");
					
					iterativeRefactor.reset(swoopModel.getSelectedOntology(),target,linksToSource,boundedInverses);
					timers4.start();
					iterativeRefactor.RunStateMachine(cla);
					timers4.stop();
					Iterator h = iterativeRefactor.getMovedClasses().iterator();
				    while(h.hasNext()){
				    	OWLDescription c = (OWLDescription)h.next();
				    	if(c instanceof OWLClass){
				    		movedClasses.add(c);
				    	}
				    }
				    iterativeRefactor.computeForeignEntities();
					timers5.start();
					iterativeRefactor.makeChanges();
					timers5.stop();
					timers3.start();
					iterativeRefactor.verifyLinks(partitions);
					timers3.stop();
					linksToSource = iterativeRefactor.getLinksToSource();
					boundedInverses = iterativeRefactor.getBoundedInverses();
		            updatedInverses.putAll(iterativeRefactor.getUpdatedInverses());
					if(iterativeRefactor.getApply()==false){
				    	timers6.start();
					    changes = iterativeRefactor.getChanges();
						swoopModel.addUncommittedChanges(changes, false);						 
						System.out.println("Applying the changes");
						swoopModel.applyOntologyChanges(false, false);
						System.out.println("Applied all the changes");
						timers6.stop();
					}
				    
				    System.out.println("finished partitioning iteration "+count);
					count++;
				  } 
				  catch (Exception e) {
			   		e.printStackTrace();
				  }
				}
			}
	 	timers.stop();
	 	System.out.println("end partitioning:"+swoopModel.getTimeStamp());
	 	
	 	//Final check on correctness of link references
	 	//The idea is to turn into object properties the link properties
	 	//linking to source that haven't been used at all
	 	
	 	for(Iterator iter = partitions.iterator(); iter.hasNext(); ){
	 		OWLOntology partition = (OWLOntology)iter.next();
	 		for(Iterator it = partition.getLinkProperties().iterator(); it.hasNext();){
	 			OWLObjectProperty prop = (OWLObjectProperty)it.next();
	 	  		
	 			if(updatedInverses.containsKey(prop)){
  			  			OntologyChange oc = new SetLinkTarget(partition,prop,(URI)updatedInverses.get(prop),null);
  			  			//changes.add(oc);
  				   	    oc.accept((ChangeVisitor)partition);
  			    }
		        else{
		 			if(prop.getLinkTarget().equals(source.getURI())){
		 				
		 				OntologyChange oc = new SetLinkTarget(partition,prop,null,null);
				  	   	oc.accept((ChangeVisitor)partition);
				 	}
			   }
	 		}
	 	}
	 	
	 	System.out.println("-------------------------");
	 	System.out.println("Timers in Miliseconds");
        System.out.println("-------------------------");
	 	System.out.println(timers.toString());
	 	System.out.println(timers3.toString());
	 	System.out.println(timers2.toString());
	 	System.out.println(timers4.toString());
	 	System.out.println(timers5.toString());
	 	System.out.println(timers6.toString());
	 	
	 	System.out.println("-------------------------");
	 	
	 	
	 	if(!test) {
	 		
	 		// if not test, then its called from Swoop
			trace = trace.concat("<HR>");
//			trace = trace.concat("<b> STEP: </b> " + count + "<br><hr>");
			String traceStep = sw.getBuffer().toString();
			trace = trace.concat(traceStep);
		
			// post proc to insert partitions into swoopmodel
		 	// serialize and reparse each partition! still works fast
		 	int index = 1;
		 	for (Iterator iter = partitions.iterator(); iter.hasNext();) {
		 		OWLOntology partition = (OWLOntology) iter.next();
		 		StringWriter st = new StringWriter();
		 		CorrectedRDFRenderer rdfRenderer = new CorrectedRDFRenderer();
		 		rdfRenderer.renderOntology(partition, st);
		 		String partitionSource = st.toString();
		 		if (saveToDisk) {
		 			// save partition to disk if partition size is large
		 			String fName = "partitions/"+this.getPartitionQName(partition.getURI());
		 			FileWriter writer = new FileWriter(new File(fName));
		            writer.write(partitionSource);
		            writer.close();
		 		}
		 		else {
		 			// reparse partition back into swoop
			 		StringReader reader = new StringReader(partitionSource);
					newPartition.add(swoopModel.loadOntologyInRDF(reader, partition.getURI()));
		 		}
		 	}
		 	// add reparsed partitions to swoopModel
			// replace current ontology with copy
		 	if (!saveToDisk) {
		 		if (!doNotAddSwoop) {
		 			swoopModel.removeOntology(source.getURI());
		 			swoopModel.addOntologies(newPartition);
		 		}
		 		else return newPartition;		 		
		 	}
			else {
				// display message pointing to saved partitions
				JOptionPane.showMessageDialog(null, "Ontology Partitions saved in the SWOOP/Partitions/ directory.", "Partitions Generated", JOptionPane.INFORMATION_MESSAGE);
			}
			
		 	// restore setting on enableCheckPoints
			swoopModel.setEnableAutoSaveChkPts(saveCheckPointSetting, false);			
	 	}
	 	
		//Display statistics
		int counting = 0;
		int nLinkProps = 0;
		String statistics = " ";
		for(Iterator i = partitions.iterator(); i.hasNext(); ) 
		{
		    OWLOntology ont = (OWLOntology) i.next();
		    statistics = statistics.concat("<b> Ontology: </b> " + ont.getURI()+ "<br>\n");
		    counting++;
		    Map foreignEntities = ont.getForeignEntities();
		    Set foreignEntitySet = foreignEntities.keySet();
		    int numForeignClasses = 0;
		    
		    for (Iterator it = foreignEntitySet.iterator(); it.hasNext(); )
		    {
		    	OWLEntity ent = (OWLEntity)it.next();
		    	if ( ent instanceof OWLClass)
		    		numForeignClasses++;
		    }
		    
		    
		    statistics = statistics.concat("Number of classes " + (ont.getClasses().size() - numForeignClasses) + "<br>\n");
		    statistics = statistics.concat("Number of Link Properties " + ont.getLinkProperties().size()+ "<br>\n");
		    int nobjectprops = ont.getObjectProperties().size() -  ont.getLinkProperties().size();
		    nLinkProps =+ nLinkProps + ont.getLinkProperties().size();
		    statistics = statistics.concat("Number of Object Properties " + nobjectprops+ "<br> \n");
			statistics = statistics.concat("Number of Datatype Properties " + ont.getDataProperties().size()+ " <br>\n");
			statistics = statistics.concat("Number of Individuals " + ont.getIndividuals().size()+ " <br> \n");
			statistics = statistics.concat(" <hr> <br>");
		}
		statistics = statistics.concat("<b> General Statistics </b> <br> ");
		statistics = statistics.concat("Total Number of Components:  " + counting + "<br>"  );
		statistics = statistics.concat("Total Number of LinkProperties: " + nLinkProps + "<br>");
		statistics = statistics.concat("Expressivity of the Original Ontology: " + sourceExpressivity+ "<br>");
		
	
		/* This snippet of code here makes sure that every OWLOntology in partitions is present
		 *   in the SwoopModel.  This is necessary because cloning of OWLOntologies during
		 *   partitioning causes the pointers to get mangled up.
		 */
		Collection ontologySet = swoopModel.getOntologies();
		for (int counter = 0 ; counter < partitions.size(); counter++ )
		{
			OWLOntology partition = (OWLOntology)partitions.get( counter );
			if (!ontologySet.contains( partition ))
				partitions.set( counter, swoopModel.getOntology( partition.getURI() ));
		}
		swoopModel.setSelectedOntology( (OWLOntology)partitions.get(0) );
		
		if (!test) {
			RefactoringSummary summary = new RefactoringSummary(new JFrame(), swoopModel, partitions, statistics, trace);
		}
		else {
			System.out.println(statistics);
		}
		
		//System.out.println("Started with class: " + initial.getURI());
		return newPartition;
	} 
	
	  public Map getTargetExpressivities(){
	  	return targetExpressivity;
	  }
	  public String getSourceExpressivity(){
	  	return sourceExpressivity;
	  }
	  
	  public Map getLinkExpressivity(){
	  	return linkExpressivity;
	  }
	  
	  private String getPartitionQName(URI uri) {
	  	String uriStr = uri.toString();
	  	return uriStr.substring(uriStr.lastIndexOf("/")+1, uriStr.length());
	  }
	  
}
