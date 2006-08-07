package org.mindswap.swoop.reasoner;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owl.io.ShortFormProvider;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLProperty;

import uk.ac.man.cs.img.dig.reasoner.Reasoner;
import uk.ac.man.cs.img.dig.reasoner.ReasonerException;
import uk.ac.man.cs.img.dig.reasoner.impl.FaCTReasoner;
import uk.ac.man.cs.img.owl.inference.dl.SimpleOWLReasoner;

/**
 * @author ronwalf
 *
 * Dig Reasoner interface for SWOOP
 */
public class SwoopDIGReasoner implements SwoopReasoner {

	private SimpleOWLReasoner reasoner;
	/**
	 * 
	 */
	public SwoopDIGReasoner() throws OWLException {
		Reasoner digReasoner = null;
		try {
			digReasoner = new uk.ac.man.cs.img.dig.reasoner.impl.BufferingHTTPReasoner("http://localhost:8080/");
		} catch (ReasonerException e) {
			System.out.println("Unable to initialize DIG reasoner");
			//throw new OWLException(e);
		}
		reasoner = new SimpleOWLReasoner(digReasoner);
	}
	/* (non-Javadoc)
	 * @see org.mindswap.swoop.reasoner.SwoopReasoner#allInstancesOf(org.semanticweb.owl.model.OWLClass)
	 */
	public Set allInstancesOf(OWLClass c) throws OWLException {
		// TODO Auto-generated method stub
		return Collections.EMPTY_SET;
	}
	
	public Set allTypesOf(OWLIndividual ind) throws OWLException {
		// TODO Auto-generated method stub
		return Collections.EMPTY_SET;
	}
	/* (non-Javadoc)
	 * @see org.semanticweb.owl.inference.OWLTaxonomyReasoner#ancestorClassesOf(org.semanticweb.owl.model.OWLClass)
	 */
	public Set ancestorClassesOf(OWLClass desc) throws OWLException {
		return reasoner.ancestorClassesOf(desc);
	}
	/* (non-Javadoc)
	 * @see org.semanticweb.owl.inference.OWLClassReasoner#ancestorClassesOf(org.semanticweb.owl.model.OWLDescription)
	 */
	public Set ancestorClassesOf(OWLDescription desc) throws OWLException {
		return reasoner.ancestorClassesOf(desc);
	}
	/* (non-Javadoc)
	 * @see org.mindswap.swoop.reasoner.OWLPropertyReasoner#ancestorPropertiesOf(org.semanticweb.owl.model.OWLProperty)
	 */
	public Set ancestorPropertiesOf(OWLProperty prop) throws OWLException {
		// TODO Auto-generated method stub
		return Collections.EMPTY_SET;
	}
	
	
	public Set complementClassesOf(OWLClass c) throws OWLException {
		// TODO Auto-generated method stub
		return Collections.EMPTY_SET;
	}
	/* (non-Javadoc)
	 * @see org.semanticweb.owl.inference.OWLTaxonomyReasoner#descendantClassesOf(org.semanticweb.owl.model.OWLClass)
	 */
	public Set descendantClassesOf(OWLClass c) throws OWLException {
		return reasoner.descendantClassesOf(c);
	}
	/* (non-Javadoc)
	 * @see org.semanticweb.owl.inference.OWLClassReasoner#descendantClassesOf(org.semanticweb.owl.model.OWLDescription)
	 */
	public Set descendantClassesOf(OWLDescription desc) throws OWLException {
		return reasoner.descendantClassesOf(desc);
	}
	/* (non-Javadoc)
	 * @see org.mindswap.swoop.reasoner.OWLPropertyReasoner#descendantPropertiesOf(org.semanticweb.owl.model.OWLProperty)
	 */
	public Set descendantPropertiesOf(OWLProperty prop) throws OWLException {
		// TODO Auto-generated method stub
		return Collections.EMPTY_SET;
	}
	
	public Set disjointClassesOf(OWLClass c) throws OWLException {
		// TODO Auto-generated method stub
		return Collections.EMPTY_SET;
	}
	/* (non-Javadoc)
	 * @see org.mindswap.swoop.reasoner.OWLPropertyReasoner#domainsOf(org.semanticweb.owl.model.OWLProperty)
	 */
	public Set domainsOf(OWLProperty prop) throws OWLException {
		// TODO Auto-generated method stub
		return Collections.EMPTY_SET;
	}
	/* (non-Javadoc)
	 * @see org.semanticweb.owl.inference.OWLTaxonomyReasoner#equivalentClassesOf(org.semanticweb.owl.model.OWLClass)
	 */
	public Set equivalentClassesOf(OWLClass c) throws OWLException {
		return reasoner.equivalentClassesOf(c);
	}
	/* (non-Javadoc)
	 * @see org.semanticweb.owl.inference.OWLClassReasoner#equivalentClassesOf(org.semanticweb.owl.model.OWLDescription)
	 */
	public Set equivalentClassesOf(OWLDescription desc) throws OWLException {
		return reasoner.equivalentClassesOf(desc);
	}
	/* (non-Javadoc)
	 * @see org.mindswap.swoop.reasoner.OWLPropertyReasoner#equivalentPropertiesOf(org.semanticweb.owl.model.OWLProperty)
	 */
	public Set equivalentPropertiesOf(OWLProperty prop) throws OWLException {
		// TODO Auto-generated method stub
		return Collections.EMPTY_SET;
	}
	/* (non-Javadoc)
	 * @see org.mindswap.swoop.reasoner.SwoopReasoner#getAnnotationProperties()
	 */
	public Set getAnnotationProperties() {
		// TODO Auto-generated method stub
		return Collections.EMPTY_SET;
	}
	/* (non-Javadoc)
	 * @see org.mindswap.swoop.reasoner.SwoopReasoner#getClasses()
	 */
	public Set getClasses() {
		// TODO Auto-generated method stub
		return Collections.EMPTY_SET;
	}
	/* (non-Javadoc)
	 * @see org.mindswap.swoop.reasoner.SwoopReasoner#getDataProperties()
	 */
	public Set getDataProperties() {
		// TODO Auto-generated method stub
		return Collections.EMPTY_SET;
	}
	/* (non-Javadoc)
	 * @see org.mindswap.swoop.reasoner.SwoopReasoner#getDifferentFromIndividuals(org.semanticweb.owl.model.OWLIndividual)
	 */
	public Set getDifferentFromIndividuals(OWLIndividual ind) {
		// TODO Auto-generated method stub
		return Collections.EMPTY_SET;
	}
	/* (non-Javadoc)
	 * @see org.mindswap.swoop.reasoner.SwoopReasoner#getDoExplanation()
	 */
	public boolean getDoExplanation() {
		return false;
	}
	/* (non-Javadoc)
	 * @see org.mindswap.swoop.reasoner.SwoopReasoner#getExplanation(org.mindswap.swoop.utils.ShortFormProvider)
	 */
	public String getExplanation(ShortFormProvider shortForms) {
		return null;
	}
	/* (non-Javadoc)
	 * @see org.mindswap.swoop.reasoner.SwoopReasoner#getExplanationSet()
	 */
	public Set getExplanationSet() {
		return Collections.EMPTY_SET;
	}
	
	public String getExpressivity() throws OWLException {
		return "Unknown";
	}
	/* (non-Javadoc)
	 * @see org.mindswap.swoop.reasoner.SwoopReasoner#getIndividuals()
	 */
	public Set getIndividuals() {
		// TODO Auto-generated method stub
		return Collections.EMPTY_SET;
	}
	/* (non-Javadoc)
	 * @see org.mindswap.swoop.reasoner.SwoopReasoner#getName()
	 */
	public String getName() {
		// TODO Auto-generated method stub
		return "Dig (Test)";
	}
	/* (non-Javadoc)
	 * @see org.mindswap.swoop.reasoner.SwoopReasoner#getObjectProperties()
	 */
	public Set getObjectProperties() {
		// TODO Auto-generated method stub
		return Collections.EMPTY_SET;
	}
	/* (non-Javadoc)
	 * @see org.mindswap.swoop.reasoner.SwoopReasoner#getOntologies()
	 */
	public Set getOntologies() {
		// TODO Auto-generated method stub
		return Collections.EMPTY_SET;
	}
	
	public OWLOntology getOntology() throws OWLException {
		return reasoner.getOntology();
	}
	
	/* (non-Javadoc)
	 * @see org.mindswap.swoop.reasoner.SwoopReasoner#getProperties()
	 */
	public Set getProperties() {
		// TODO Auto-generated method stub
		return Collections.EMPTY_SET;
	}
	/* (non-Javadoc)
	 * @see org.mindswap.swoop.reasoner.SwoopReasoner#getSameAsIndividuals(org.semanticweb.owl.model.OWLIndividual)
	 */
	public Set getSameAsIndividuals(OWLIndividual ind) {
		// TODO Auto-generated method stub
		return Collections.EMPTY_SET;
	}
	/* (non-Javadoc)
	 * @see org.mindswap.swoop.reasoner.SwoopReasoner#instancesOf(org.semanticweb.owl.model.OWLClass)
	 */
	public Set instancesOf(OWLClass c) throws OWLException {
		// TODO Auto-generated method stub
		return Collections.EMPTY_SET;
	}
	/* (non-Javadoc)
	 * @see org.semanticweb.owl.inference.OWLIndividualReasoner#instancesOf(org.semanticweb.owl.model.OWLDescription)
	 */
	public Set instancesOf(OWLDescription desc) throws OWLException {
		// TODO Auto-generated method stub
		return Collections.EMPTY_SET;
	}
	/* (non-Javadoc)
	 * @see org.mindswap.swoop.reasoner.OWLPropertyReasoner#inversePropertiesOf(org.semanticweb.owl.model.OWLObjectProperty)
	 */
	public Set inversePropertiesOf(OWLObjectProperty prop) throws OWLException {
		// TODO Auto-generated method stub
		return Collections.EMPTY_SET;
	}
	/* (non-Javadoc)
	 * @see org.mindswap.swoop.reasoner.SwoopReasoner#isConsistent()
	 */
	public boolean isConsistent() throws OWLException {
		return reasoner.isConsistent();
	}
	
	public boolean isConsistent(OWLClass c) throws OWLException {
		return reasoner.isConsistent(c);
	}
	
	public boolean isConsistent(OWLDescription desc) throws OWLException {
		return reasoner.isConsistent(desc);
	}
	
	public boolean isEquivalentClass(OWLDescription desc1, OWLDescription desc2) throws OWLException {
		return reasoner.isEquivalentClass(desc1, desc2);
	}
	/* (non-Javadoc)
	 * @see org.semanticweb.owl.inference.OWLIndividualReasoner#isInstanceOf(org.semanticweb.owl.model.OWLIndividual, org.semanticweb.owl.model.OWLDescription)
	 */
	public boolean isInstanceOf(OWLIndividual ind, OWLDescription desc) throws OWLException {
		return reasoner.isInstanceOf(ind, desc);
	}
	
	public boolean isSubClassOf(OWLDescription desc1, OWLDescription desc2) throws OWLException {
		return reasoner.isSubClassOf(desc1, desc2);
	}
	/* (non-Javadoc)
	 * @see org.mindswap.swoop.reasoner.SwoopReasoner#loadImports()
	 */
	public boolean loadImports() {
		// TODO Auto-generated method stub
		return false;
	}
	/* (non-Javadoc)
	 * @see org.mindswap.swoop.reasoner.OWLPropertyReasoner#rangesOf(org.semanticweb.owl.model.OWLProperty)
	 */
	public Set rangesOf(OWLProperty prop) throws OWLException {
		// TODO Auto-generated method stub
		return Collections.EMPTY_SET;
	}
	/* (non-Javadoc)
	 * @see org.mindswap.swoop.reasoner.SwoopReasoner#setDoExplanation(boolean)
	 */
	public void setDoExplanation(boolean explain) {
		
	}
	/* (non-Javadoc)
	 * @see org.mindswap.swoop.reasoner.SwoopReasoner#setLoadImports(boolean, boolean)
	 */
	public void setLoadImports(boolean useImports, boolean refreshOntology) throws OWLException {
		// TODO Auto-generated method stub
		
	}
	
	public void setOntology(OWLOntology ont) throws OWLException {
		reasoner.setOntology(ont);
	}
	/* (non-Javadoc)
	 * @see org.semanticweb.owl.inference.OWLTaxonomyReasoner#subClassesOf(org.semanticweb.owl.model.OWLClass)
	 */
	public Set subClassesOf(OWLClass c) throws OWLException {
		return reasoner.subClassesOf(c);
	}
	/* (non-Javadoc)
	 * @see org.semanticweb.owl.inference.OWLClassReasoner#subClassesOf(org.semanticweb.owl.model.OWLDescription)
	 */
	public Set subClassesOf(OWLDescription desc) throws OWLException {
		return reasoner.subClassesOf(desc);
	}
	/* (non-Javadoc)
	 * @see org.mindswap.swoop.reasoner.OWLPropertyReasoner#subPropertiesOf(org.semanticweb.owl.model.OWLProperty)
	 */
	public Set subPropertiesOf(OWLProperty prop) throws OWLException {
		// TODO Auto-generated method stub
		return Collections.EMPTY_SET;
	}
	/* (non-Javadoc)
	 * @see org.semanticweb.owl.inference.OWLTaxonomyReasoner#superClassesOf(org.semanticweb.owl.model.OWLClass)
	 */
	public Set superClassesOf(OWLClass desc) throws OWLException {
		return reasoner.superClassesOf(desc);
	}
	/* (non-Javadoc)
	 * @see org.semanticweb.owl.inference.OWLClassReasoner#superClassesOf(org.semanticweb.owl.model.OWLDescription)
	 */
	public Set superClassesOf(OWLDescription desc) throws OWLException {
		return reasoner.superClassesOf(desc);
	}
	/* (non-Javadoc)
	 * @see org.mindswap.swoop.reasoner.OWLPropertyReasoner#superPropertiesOf(org.semanticweb.owl.model.OWLProperty)
	 */
	public Set superPropertiesOf(OWLProperty prop) throws OWLException {
		// TODO Auto-generated method stub
		return Collections.EMPTY_SET;
	}
	/* (non-Javadoc)
	 * @see org.mindswap.swoop.reasoner.SwoopReasoner#supportsExplanation()
	 */
	public boolean supportsExplanation() {
		return false;
	}
	
	public Set typesOf(OWLIndividual ind) throws OWLException {
		// TODO Auto-generated method stub
		return Collections.EMPTY_SET;
	}	


	public Map getDataPropertyValues(OWLIndividual ind) throws OWLException {
	    return ind.getDataPropertyValues( getOntologies() );
	}
	
	public Map getObjectPropertyValues(OWLIndividual ind) throws OWLException{
	    return ind.getObjectPropertyValues( getOntologies() );
	}
}
