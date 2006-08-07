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

package org.mindswap.swoop.reasoner;

import java.net.URI;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.mindswap.pellet.EconnectedKB;
import org.mindswap.pellet.owlapi.Reasoner;
import org.mindswap.pellet.taxonomy.ClassifyProgress;
import org.mindswap.pellet.taxonomy.DefaultClassifyProgress;
import org.mindswap.pellet.taxonomy.Taxonomy;
import org.mindswap.pellet.taxonomy.TaxonomyNode;
import org.mindswap.pellet.utils.Timer;
import org.mindswap.swoop.utils.ExpressivityChecker;
import org.mindswap.swoop.utils.exceptions.UserCanceledException;
import org.semanticweb.owl.io.ShortFormProvider;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLIndividualAxiom;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLSameIndividualsAxiom;
import org.semanticweb.owl.model.helper.OntologyHelper;

import aterm.ATermAppl;

//import com.vladium.utils.IObjectProfileNode;
//import com.vladium.utils.ObjectProfiler;

/**
 * @author Evren Sirin
 */
public class PelletReasoner extends Reasoner implements SwoopReasoner {
    
	public static boolean AUTO_CLASSIFY = true;
    public boolean doExplanation = false; // glass box - clash detection
    									  // and for dependency axioms
    public DependencyReasoner depFinder;
    
    private HashMap sameAs, differentFrom;

   
    public synchronized void setOntologyWithoutClassif(OWLOntology ontology) throws OWLException 
	{
		// default call is to show progress bar (3rd argument)
		setOntology( ontology, false, false);
	}
	
    
	public synchronized void setOntology(OWLOntology ontology) throws OWLException {
	    Timer timers = new Timer("Pellet Processing");
	    timers.start();
		depFinder = null;
		setOntology(ontology, true);
		timers.stop();
		System.out.println(timers);
	}
	
	public synchronized void setOntology(OWLOntology ontology, boolean realize) throws OWLException 
	{
		// default call is to show progress bar (3rd argument)
		setOntology( ontology, realize, true);
	}
	
	public synchronized void setOntologyWithoutClassif(OWLOntology ontology, boolean realize) throws OWLException 
	{
		// default call is to show progress bar (3rd argument)
		setOntology( ontology, false, false);
	}
	
	public synchronized void setOntology(OWLOntology ontology, boolean realize, boolean showProgress) throws OWLException {
//	    System.out.println("Explanation: " + getDoExplanation());
	    
		super.setOntology(ontology);
		
//		PelletOptions.USE_PSEUDO_NOMINALS = true;
		Taxonomy.DEBUG = false;
//		PelletOptions.SHOW_CLASSIFICATION_PROGRESS = true;
		if(isConsistent() && AUTO_CLASSIFY) {
//		    setDoExplanation(true);
		    
		    if(realize) {
		        System.out.print("Pellet classifying...");
		        
		        // if showing progress, use DefaultClassifyProgress
		        if ( showProgress )
		        {
		        	ClassifyProgress progress = new DefaultClassifyProgress();
		        	kb.getTaxonomyBuilder().setListener( progress ); 
		        }
		        else // if not showing progress, use SilentClassifyProgress (by passing in null)
		        	kb.getTaxonomyBuilder().setListener( null );
		        
		    	kb.realize();
		    	
		    	System.out.println( "done " );
		    	
		    	if( !kb.isRealized() ) {
		    	    kb = null;
		    	    System.out.println( "User canceled" );
		    	    throw new UserCanceledException( "User canceled classification " );
		    	}
		    	
		    }
		}
		
		kb.setOntology(ontology.getURI().toString());
		
		if(realize) {
			//Aditya: This stuff for obtaining relations between individuals
			// sameAs and differentFrom has been taken from the RDFS reasoner
			// We need to use Pellet to infer same/different b/w individuals
			sameAs = new HashMap();
			differentFrom = new HashMap();
			this.computeIndividualRelations(ontology);
		}
		
		// size of expTable
//		ExplanationTable expTable = kb.getExplanationTable();
////		expTable = new ExplanationTable();
//		IObjectProfileNode profile = ObjectProfiler.profile (expTable);
//        System.out.println ("ExplanationTable size = " + profile.size () + " bytes");
	}
	
	public String getName() {
		return "Pellet";
	}

	public synchronized boolean isConsistent() {
		return kb.isConsistent();
	}
	
	public void printClassTree(){
		kb.getTaxonomy().print();
	}
	
	public void printPropertyTree(){
		kb.getRoleTaxonomy().print();
	}
	public Taxonomy getRoleTaxonomy(){
		return kb.getRoleTaxonomy();		
	}
	
	public Taxonomy getTaxonomy(){
		return kb.getTaxonomy();		
	}
	
	public Set disjointClassesOf(OWLClass c) throws OWLException {
		// TODO Auto-generated method stub
		return Collections.EMPTY_SET;
	}

	/* (non-Javadoc)
	 * @see org.mindswap.swoop.SwoopReasoner#complementClassesOf(org.semanticweb.owl.model.OWLClass)
	 */
	public Set complementClassesOf(OWLClass c) throws OWLException {
		// TODO Auto-generated method stub
		return Collections.EMPTY_SET;
	}

	/* (non-Javadoc)
	 * @see org.mindswap.swoop.SwoopReasoner#isConsistent(org.semanticweb.owl.model.OWLClass)
	 */
	public synchronized boolean isConsistent(OWLClass c) throws OWLException {
	    // in an inconsistent ontology every class is inconsistent
//		Timer timers = new Timer("Pellet Consistency Check");
//	    timers.start();
		if(!isConsistent())
		    return false;
		
//		System.out.println("DEBUG: Remove me!");
//		return true;
		boolean cons = isConsistent((OWLDescription) c);
//		timers.stop();
//		System.out.println(timers);
		return cons;
	}

	public Set equivalentClassesOf(OWLDescription c) throws OWLException {
	    if(!isConsistent())
	        return new HashSet();
	    
	    return super.equivalentClassesOf(c);
	}
	
	public Set equivalentClassesOf(OWLClass c) throws OWLException {
	    if(!isConsistent())
	        return new HashSet();
	    
	    return super.equivalentClassesOf(c);
	}
	
	public Set subClassesOf(OWLClass c) throws OWLException {
	    if(!isConsistent())
	        return new HashSet();
	    
	    return super.subClassesOf(c);	
	}
	
	public Set superClassesOf(OWLClass c) throws OWLException {
	    if(!isConsistent())
	        return new HashSet();
	    
	    return super.superClassesOf(c);	
	}
	
	public Set allInstancesOf(OWLClass c) throws OWLException {
	    if(!isConsistent())
	        return new HashSet();
	    
		return super.allInstancesOf(c);
	}
	
	/* (non-Javadoc)
	 * @see org.semanticweb.owl.inference.OWLIndividualReasoner#instancesOf(org.semanticweb.owl.model.OWLDescription)
	 */
	public Set instancesOf(OWLClass c) throws OWLException {
	    if(!isConsistent())
	        return new HashSet();
	    
		return super.instancesOf(c);
	}

	public Set typesOf(OWLIndividual ind) throws OWLException {
	    if(!isConsistent())
	        return new HashSet();
	    
		return super.typesOf(ind);	
	}

	public Set allTypesOf(OWLIndividual ind) throws OWLException {
	    if(!isConsistent())
	        return new HashSet();
	    
		return super.allTypesOf(ind);		
	}
	
    /* (non-Javadoc)
     * @see org.mindswap.swoop.SwoopReasoner#supportsExplanation()
     */
    public boolean supportsExplanation() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.mindswap.swoop.SwoopReasoner#setDoExplanation(boolean)
     */
    public void setDoExplanation(boolean explain) {
    	this.doExplanation = explain;
    	if(kb != null)
    		kb.setDoExplanation(explain);        
    }
    
    public void setDoDependencyTracking(boolean depTracking) {
    	if(kb != null)
    		kb.setDoDependencyAxioms( depTracking );
    }
    
    public boolean getDoExplanation() {
//        if(kb != null)
//        return kb.doExplanation();
        
        return this.doExplanation;
    }
    
    public Set getExplanationSet() {
        if(kb != null)
        return kb.getExplanationSet();
        
        return Collections.EMPTY_SET;
    }

    public String parseExplanation(ShortFormProvider shortForms, String explanation) {

    	StringBuffer buffer = new StringBuffer();
    	// TODO fix this horrible hack
		// the string is a multi line string so first split it
		String[] lines = explanation.split(System.getProperty("line.separator")); 
		for(int j = 0; j < lines.length; j++) {
		    // each line has a header and a content
		    int index = lines[j].indexOf(':');
		    String header = "Reason"; //lines[j].substring(0, index);
		    String content = lines[j]; //lines[j].substring(index + 1);
		    buffer.append("<b>" + header + "</b>: ");
		    
		    //BJP: Hideous string replacement hacks...but I got tired of waiting!
		    content = content.replaceAll("\\[", "[ ");
		    content = content.replaceAll("\\]", " ]");
		    content = content.replaceAll("\\(", "( ");
			content = content.replaceAll("\\)", " )");
			content = content.replaceAll(",", " , ");
			content = content.replaceAll("forced to belong to class", "forced to belong to class<blockquote><FONT FACE=\"Verdana\" SIZE=2> ");
			content = content.replaceAll("and its complement", " </FONT></blockquote> <FONT FACE=\"Verdana\" SIZE=2>and its complement</FONT>");
		    String[] parts = content.split("\\s");				    
		    for(int k = 0; k < parts.length; k++) {
		       if(parts[k].startsWith("http:") || parts[k].startsWith("file:"))
		           buffer.append("<a href=\"" + parts[k] + "\">" + shortForms.shortForm(URI.create(parts[k])) + "</a> ");
		       else
		           buffer.append(parts[k] + " ");
		    }
		    buffer.append("<br><FONT FACE=\"Verdana\" SIZE=2>");
		}

		String formattedExplanation = buffer.toString();
		formattedExplanation = formattedExplanation.replaceAll("\\[ ", "[");
		formattedExplanation = formattedExplanation.replaceAll(" \\]", "]");
		formattedExplanation = formattedExplanation.replaceAll("\\( ", "(");
		formattedExplanation = formattedExplanation.replaceAll(" \\)", ")");
		formattedExplanation = formattedExplanation.replaceAll(" , ", ", ");
		formattedExplanation = formattedExplanation.replaceAll("\\),", "),<br>");

        return formattedExplanation;	
    }
    
    /* (non-Javadoc)
     * @see org.mindswap.swoop.SwoopReasoner#getExplanation()
     */
    public String getExplanation(ShortFormProvider shortForms) {
        String explanation = kb.getExplanation();
		// System.out.println(explanation);
		return this.parseExplanation(shortForms, explanation);
    }
    
	public String getExpressivity() throws OWLException {
		//********************************************************
		//Added for Econnections
		//********************************************************
		String expressivity = "";
		if(!(kb instanceof EconnectedKB ))
			expressivity =  kb.getExpressivity().toString();
		else{
			expressivity =  ((EconnectedKB)kb).getEconnExpressivity().toString();
		    expressivity += "<br>" + "C(...) - EConnection";
		    if (((EconnectedKB)kb).getEconnExpressivity().hasInverses())
		    	 expressivity += "<br>" + "C(...)I - Inverses on links";
//		    if (((EconnectedKB)kb).getEconnExpressivity().hasLinkNumberRestrictions())
//		    	 expressivity += "<br>" + "C(...)N - Number restrictions on links";
		    
		    // FIXME Evren: temporarily disabled so old pellet can be used 
//		    if (((EconnectedKB)kb).getEconnExpressivity().hasLinkFunctionalRestrictions())
//		    	 expressivity += "<br>" + "C(...)F - Functional Number restrictions on links";
		    expressivity += "<br>" + "---------------------------------------------------";  
		}
		
		return ExpressivityChecker.getExplanation(expressivity);			
	}
	
	
	/**
	 * Return a set of sameAs individuals given a specific individual
	 * based on axioms in the ontology
	 * @param ind - specific individual to test
	 * @return
	 * @throws OWLException
	 */
//	public Set getSameAsIndividuals(OWLIndividual ind) {	    
//	    try {
//            return super.getSameAsIndividuals(ind);
//        } catch(OWLException e) {
//            return (Set) sameAs.get(ind);
//        }
//	}
	
	// TODO: Need to use Pellet for this
	public Set getDifferentFromIndividuals(OWLIndividual ind) {
		return (HashSet) differentFrom.get(ind);
	}

	/* (non-Javadoc)
	 * @see org.mindswap.swoop.reasoner.SwoopReasoner#setLoadImports(boolean, boolean)
	 */
	public void setLoadImports(boolean useImports, boolean refreshOntology) throws OWLException {
		super.setLoadImports(useImports, refreshOntology);
	}

	/**
	 * Check for assertions/relations involving all individuals in the ontology:
	 *  1. sameAs or differentFrom (axioms)
	 * 
	 */
	private void computeIndividualRelations(OWLOntology ontology) throws OWLException {
		
		Set ontologies = null;
		if(this.loadImports())
			ontologies = OntologyHelper.importClosure(ontology);
		else
			ontologies = Collections.singleton(ontology);
		
		//	iterate through each ontology
		Iterator ont = ontologies.iterator();
		// check axioms for sameAs and differentFrom assertions b/w individuals
		while(ont.hasNext()) {
			OWLOntology o = (OWLOntology) ont.next();
			// get individual axioms for each ontology
			for (Iterator iter = o.getIndividualAxioms().iterator(); iter.hasNext(); ){
				OWLIndividualAxiom indAxiom = (OWLIndividualAxiom) iter.next();
				// get the set of individuals participating in each axiom
				Set inds = indAxiom.getIndividuals();
				Map map = null;				
				if (indAxiom instanceof OWLSameIndividualsAxiom) map = sameAs;
				else map = differentFrom;				
				// add it to the corresponding map
				for (Iterator iter2 = inds.iterator(); iter2.hasNext(); ) {
					Set copyInds = new HashSet(inds); // create copy of set
					OWLIndividual ind = (OWLIndividual) iter2.next();
					copyInds.remove(ind);
					if (map.get(ind)==null) {
						// put new set
						map.put(ind, copyInds);
					}
					else {
						// add to existing set
						Set current = (HashSet) map.get(ind);
						current.addAll(copyInds);
						map.put(ind, current);
					}
				}				
			}
		}
	}
	
	/**
	 * Given a set of unsat. classes in the ontology, separate
	 * the root from the derived classes. Also identify connecting
	 * axioms if any. Generate appropriate output data structures
	 */
	public void autoRootDiscovery() {
		try {
			OWLClass owlNothing = this.getOntology().getOWLDataFactory().getOWLNothing();
			Set unsat = this.equivalentClassesOf(owlNothing);
			if(!unsat.isEmpty()) {
				
				// create instanceof DependencyReasoner
				depFinder = new DependencyReasoner(this.getOntologies(), this.getOntology(), unsat);
//				depFinder.DEBUG = true;
				System.out.println("---------------------------------");
				System.out.println("Finding dependencies using Structural Tracing...");
				Timer timer = new Timer("Structural Tracing");
				timer.start();
				depFinder.findDependencies();
				System.out.println("Move mutual dependencies to potential roots");
				depFinder.mutualToRoot();
				timer.stop();
				System.out.println(timer.toString());
				System.out.println("Potential Roots: "+depFinder.rootClasses.size()+" Derived: "+depFinder.derivedClasses.size());
				Timer timer2 = new Timer("Root Pruning");
				timer2.start();
//				depFinder.findAllRoots();
				depFinder.infDepOntApprox();
				timer2.stop();
				System.out.println(timer2.toString());
				depFinder.countDependencies();
				
				System.out.println("No. of satisfiability tests: "+depFinder.numSatTests);
				System.out.println("---------------------------------");
				System.out.println("No. of roots:"+depFinder.rootClasses.size());
				for (Iterator iter = depFinder.rootClasses.iterator(); iter.hasNext();) {
					OWLClass root = (OWLClass) iter.next();
					System.out.println("Root Unsat.:" + getName(root));
				}
				
				System.out.println("---------------------------------");
				System.out.println("Total No. of derived:"+depFinder.derivedClasses.size());
				for (Iterator iter = depFinder.derivedClasses.iterator(); iter.hasNext();) {
					OWLClass derived = (OWLClass) iter.next();
					System.out.println("Derived Unsat.:" + getName(derived));
				}
				System.out.println("---------------------------------");				
			}
		}
		catch (OWLException ex) {
			ex.printStackTrace();
		}
	}
	
	private String getName(OWLClass cla) {
		try {
			String uri = cla.getURI().toString();
			if (uri.indexOf("#")>=0) uri = uri.substring(uri.indexOf("#")+1, uri.length());
			else uri = uri.substring(uri.lastIndexOf("/")+1, uri.length());
			return uri;
		}
		catch (OWLException ex) {
			ex.printStackTrace();
		}
		return "";
	}
	
	public String getTimeStamp() {
		Calendar cal = Calendar.getInstance(TimeZone.getDefault());
	    String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(DATE_FORMAT);
	    sdf.setTimeZone(TimeZone.getDefault());                    
	    String ts = sdf.format(cal.getTime()).toString();
	    return ts;
	}
	
	public Map getDataPropertyValues(OWLIndividual ind) throws OWLException {
	    return super.getDataPropertyValues(ind);
	}
	
	public Map getObjectPropertyValues(OWLIndividual ind) throws OWLException {
	    return super.getObjectPropertyValues(ind);
	}

    public Set getSameAsIndividuals(OWLIndividual ind) throws OWLException {
        return super.getSameAsIndividuals( ind );
    }
}
