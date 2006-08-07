package org.mindswap.swoop.reasoner;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owl.inference.OWLClassReasoner;
import org.semanticweb.owl.inference.OWLIndividualReasoner;
import org.semanticweb.owl.inference.OWLReasoner;
import org.semanticweb.owl.inference.OWLTaxonomyReasoner;
import org.semanticweb.owl.io.ShortFormProvider;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLIndividual;

/**
 * The main reasoner interface that defines the basic functionalities. The functions provided by
 * reasoners always returns named classes.
 * 
 * @author Evren Sirin
 */
public interface SwoopReasoner 
extends OWLReasoner, OWLClassReasoner, OWLIndividualReasoner, OWLPropertyReasoner, OWLTaxonomyReasoner {

	public String getName();
	
	/**
	 * 
	 * Set the LoadImports flag. If an ontology is already loaded in the reasoner it will
	 * automatically reloaded.
	 * 
	 * @param useImports
	 * @throws OWLException
	 */
	public void setLoadImports(boolean useImports, boolean refreshOntology) throws OWLException;
	
	/**
	 * 
	 * Returns the flag for LoadImports.
	 * 
	 * @return
	 */
	public boolean loadImports();

	/**
	 * Returns true if the loaded ontology is consistent. 
	 * 
	 * @param c
	 * @return
	 * @throws OWLException
	 */
	public boolean isConsistent() throws OWLException;
	
	/**
	 * Returns true if the given named class is consistent. If the reasoner does not
	 * support consistency check it should simply return true
	 * 
	 * @param c
	 * @return
	 * @throws OWLException
	 */
	public boolean isConsistent(OWLClass c) throws OWLException;	

	/**
	 * 
	 * Returns all the named classes that this individual is a direct type of. This returns a set of
	 * sets where each set is an equivalent class.
	 * 
	 * @param ind
	 * @return Set of OWLDescription objects
	 * @throws OWLException
	 */
	public Set typesOf(OWLIndividual ind) throws OWLException;
	
	/**
	 * Returns the DL expressivity (ALC..) of the current ontology
	 * @param ind
	 * @return
	 * @throws OWLException
	 */
	public String getExpressivity() throws OWLException;
	
	/**
	 * 
	 * Returns all the named classes that this individual belongs. This returns a set of
	 * sets where each set is an equivalent class
	 * 
	 * @param ind
	 * @return Set of OWLDescription objects
	 * @throws OWLException
	 */
	public Set allTypesOf(OWLIndividual ind) throws OWLException;
	
	/**
	 * 
	 * Returns a set of named classes that are disjoint with the given class.
	 * 
	 * @param c
	 * @return
	 * @throws OWLException
	 */
	public Set disjointClassesOf(OWLClass c) throws OWLException;
	
	/**
	 * 
	 * Returns a set of named classes that are disjoint with the given class.
	 * 
	 * @param c
	 * @return
	 * @throws OWLException
	 */
	public Set complementClassesOf(OWLClass c) throws OWLException;
		
	/**
	 * 
	 * returns the set of ontologies loaded in the reasoner. Generally, this set is equal
	 * to the imports closure of the ontology loaded with setOntology() functions. But it may 
	 * 
	 * @return
	 */
	public Set getOntologies();

	/**
	 * 
	 * Return the set of all named classes defined in any of the ontologies loaded in the reasoner.
	 * 
	 * @return set of OWLClass objects
	 */
	public Set getClasses();

	/**
	 * 
	 * Return the set of all object and data properties defined in any of the ontologies loaded in the reasoner.
	 * 
	 * @return set of OWLClass objects
	 */
	public Set getProperties();	
	
	/**
	 * 
	 * Return the set of all object properties defined in any of the ontologies loaded in the reasoner.
	 * 
	 * @return set of OWLClass objects
	 */
	public Set getObjectProperties();
	
	/**
	 * 
	 * Return the set of all data properties defined in any of the ontologies loaded in the reasoner.
	 * 
	 * @return set of OWLClass objects
	 */
	public Set getDataProperties();	
			
	/**
	 * 
	 * Return the set of all annotation properties defined in any of the ontologies loaded in the reasoner.
	 * 
	 * @return set of OWLClass objects
	 */
	public Set getAnnotationProperties();	
	
	/**
	 * 
	 * Return the set of all individuals defined in any of the ontologies loaded in the reasoner.
	 * 
	 * @return set of OWLClass objects
	 */
	public Set getIndividuals();
	
	/**
	 * Return a set of sameAs individuals given a specific individual
	 * based on axioms in the ontology
	 * @param ind - specific individual to test
	 * @return
	 */
	public Set getSameAsIndividuals(OWLIndividual ind) throws OWLException;
	
	/**
	 * Return a set of differentFrom individuals given a specific individual
	 * based on axioms in the ontology
	 * @param ind - specific individual to test
	 * @return
	 */
	public Set getDifferentFromIndividuals(OWLIndividual ind) throws OWLException;
	
	
	/**
	 * Returns true if this reasoner can generate explantions.
	 * 
	 * @param explain
	 */
	public boolean supportsExplanation();
	
	
	/**
	 * 
	 * Enable the explanation fature in the reasoner. If a specific reasoner implementation does not
	 * support this feature it should simply ignore this command.
	 * 
	 * @param explain
	 */
	public void setDoExplanation(boolean explain);
	
	/**
	 * Check if explanation fature in the reasoner is enabled.
	 */
	public boolean getDoExplanation();

	/**
	/**
	 * Get the explanation for the last issued command. For example, when 
	 * {@link #isConsistent(OWLClass) isConsistent(OWLClass)}
	 * function is called for an inconsistent class this function should return the explanation
	 * for this inconsistency. The string returned should be HTML formatted such that it can be
	 * directly printed in Ontology or Term pane. In theory, this feature cand be used to explain 
	 * subsumption but this is not tested yet.<br><br>
	 * 
	 * <b>NOTE:</b>  Use {@link #setDoExplanation(boolean) setDoExplanation} to enable explanations
	 * before this function is called. Otherwise, value returned is undefined.
	 * 
	 * @param shortForms ShortFormProvider that needs to be used for formatting URI's
	 * @return
	 */
	public String getExplanation(ShortFormProvider shortForms);
	
	/**
	 * Return a set of axioms that are the supporting assertions for the last expanation
	 * generated
	 * 
	 * @return
	 */
	public Set getExplanationSet();

	/**
	 * 
	 * Return direct instances of this class
	 * 
	 * @param c
	 * @return
	 */
	public Set instancesOf(OWLClass c) throws OWLException;
	
	/**
	 * 
	 * Return all the instances of this class.
	 * 
	 * @param c
	 * @return
	 */
	public Set allInstancesOf(OWLClass c) throws OWLException;	
	
	public Map getDataPropertyValues(OWLIndividual ind) throws OWLException;
	
	public Map getObjectPropertyValues(OWLIndividual ind) throws OWLException;
}
