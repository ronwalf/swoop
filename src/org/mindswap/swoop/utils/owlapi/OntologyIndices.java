/*
 * Created on Feb 14, 2005
 *
 */
package org.mindswap.swoop.utils.owlapi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.reasoner.SwoopRDFSReasoner;
import org.mindswap.swoop.reasoner.SwoopReasoner;
import org.mindswap.swoop.utils.SetUtils;
import org.semanticweb.owl.model.OWLAnd;
import org.semanticweb.owl.model.OWLCardinalityRestriction;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLClassAxiom;
import org.semanticweb.owl.model.OWLDataAllRestriction;
import org.semanticweb.owl.model.OWLDataCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataEnumeration;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataQuantifiedRestriction;
import org.semanticweb.owl.model.OWLDataRange;
import org.semanticweb.owl.model.OWLDataType;
import org.semanticweb.owl.model.OWLDataValueRestriction;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDisjointClassesAxiom;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLEnumeration;
import org.semanticweb.owl.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owl.model.OWLEquivalentPropertiesAxiom;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLNaryBooleanDescription;
import org.semanticweb.owl.model.OWLNot;
import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLObjectAllRestriction;
import org.semanticweb.owl.model.OWLObjectCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectQuantifiedRestriction;
import org.semanticweb.owl.model.OWLObjectRestriction;
import org.semanticweb.owl.model.OWLObjectValueRestriction;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOr;
import org.semanticweb.owl.model.OWLProperty;
import org.semanticweb.owl.model.OWLPropertyAxiom;
import org.semanticweb.owl.model.OWLRestriction;
import org.semanticweb.owl.model.OWLSubClassAxiom;
import org.semanticweb.owl.model.OWLSubPropertyAxiom;
import org.semanticweb.owl.model.helper.OntologyHelper;

/**
 * @author Aditya
 *
 * Defines what types of cross-references need to be indexed
 * for a particular ontology. Used in BuildIndices.java where ontology
 * and the computed indices are finally stored
 */
public class OntologyIndices implements Serializable {

	private Set ontologies; // current ontology set being indexed
	private List classes, properties, individuals;
	private List classAxioms, propAxioms, indAxioms;
	
	public Set unions, intersections, disjointAxioms;
	public Set descriptions;
	public Map descriptionToEntity; //Key: description, Value: any named entity - Class, Property, Individual where the description appears 
	public Map restrictionToEntity; //Key: restriction, Value: any named entity - Class, Property, Individual where the restriction appears
	
	public Map equivalents; //Key: OWL Object, Value: OWL Object
	public Map classesToPropertiesRestriction;//Key: elements of classes in Restrictions
	                                              //Content: properties in those restrictions
	public Map individualsToPropertiesRestriction;//Key: Elements of individualsInRestriction 
	                                                  //Content: properties in those restrictions
	
	public Map individualsToPropertiesAssertions; // key: Individuals b, 
												  // Value: properties related to it by abox assertions
												  // *** but R(a,b) ***  
	public Map individualsInEnumerations; // key - a, value - enumeration which contains a
	
	public Map propToIndividualAssertions; //key: property R, value: individual a, such that R(a,b) 
	public Map propToDescriptionRestrictions; // key: property R, value: Description C which is a restriction on property R
	
	public Map classesToDomain;  //Key: Classes and class descriptions that are domain of some property
	                                  //Value: The properties they are domain of
	public Map classesToRange; // analogous to domain
	public Map classesInUnions; // Class descriptions appearing in Unions
	public Map classesInIntersections; // Class Descriptions appearing in intersections
	
	public Map nestedRestrictions; // Key: An object property R
	                                  // Value: A Set S1,...,Sn of object properties
	                                 // Means that S1,...,Sn appear in a restriction nested inside a restriction on R	
	public SwoopReasoner reasoner;
	
	// need the below to obtain sub/super for descriptions as well
	// cannot use reasoner because it returns only named classes
	// and no need to use OWLDescriptionFinder because it will loop
	// over all entities/axioms again
	public Map subClasses, superClasses, subProperties, superProperties, instancesOf;
	
//	This is for computing expressivity
	boolean hasNegation = false;
    boolean hasTransitivity = false;
    boolean hasRoleHierarchy = false;
    boolean hasInverse = false;
    boolean hasNominals = false;
    boolean hasCardinality = false;
    boolean hasFunctionality = false;
    boolean hasDatatype = false;
    boolean hasUnion = false;
    boolean hasIntersection = false;
    boolean hasRange = false;
    boolean hasComplexRestriction = false;
    boolean isDLLite = true;
    boolean isRDFS = true;
    boolean isEL = true;
    boolean isELpp = true;  //EL++
	
    // DEBUGGING TAGS (construct tags)
    boolean DEBUG_NOMINALS  = false;
    boolean DEBUG_NEGATIONS = false;
    boolean DEBUG_UNIONS    = false;
    boolean DEBUG_HASCOMPLEXRESTRICTION = false;
    
    // DEBUGGING TAGS (expressivity tags)
    boolean DEBUG_IS_EL     = false;

    
	public OntologyIndices(SwoopReasoner reasoner) {
		if (reasoner==null) this.reasoner = new SwoopRDFSReasoner();
		else this.reasoner = reasoner;
	}
	
	public void buildIndex(
			OWLOntology ontology, 
			boolean indexImports, // consider imported ontologies?
			boolean skipReasoning // can be called by reasoner directly after it does its classification
			) {
		
		if (!skipReasoning) {
			try {
				// set reasoner to classify ontology
				reasoner.setOntology(ontology);
			} catch (OWLException e2) {
				e2.printStackTrace();
			}
		}
		
		// first determine set of ontologies
		if (indexImports)
			try {
				ontologies = OntologyHelper.importClosure(ontology);
			} catch (OWLException e) {
				e.printStackTrace();
			}
		else
			ontologies = Collections.singleton(ontology);
		
		try {
			// initialize to get list of entities and axioms in ontologies
			// also reset all maps
			this.init();
			
			// parse ontology and build corresponding indices 
			this.parseOntology();
			
						
		} catch (OWLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	
	/**
	 * Compute expressivity for a single OWL ontology according to the information 
	 * in the Maps
	 * @param ontology
	 */
	
	public String getExpressivity(OWLOntology ont) {
        String dl = "";
        
        if(isRDFS == true){
        	dl = "RDFS(DL)";
        	return dl;
        }
        if(isDLLite == true){
        	dl= "DL-Lite";
        	return dl;
        }
        
        if ( (isELpp) && (isEL == false))
        	return "EL++";

        if((isEL == true)){
        	dl = "EL";
        	if(hasUnion == true)
        		dl += "U";
        	if(hasInverse == true)
        		dl += "I";
        	if(hasRoleHierarchy == true)
        		dl += "H";
        	if(hasCardinality == true)
                dl += "N";
            else if(hasFunctionality == true)
                dl += "F";
        	if(hasDatatype == true)
                dl += "(D)";
            return dl;
        }

        if(hasNegation==true || hasUnion ==true || hasComplexRestriction == true)
            dl = "ALC";
        else
            dl = "AL";
        
        if(hasTransitivity == true)
            dl += "R+";
        
        if(dl.equals("ALCR+"))
            dl = "S";
        
        if(hasRoleHierarchy == true)
            dl += "H";

        if(hasInverse == true)
            dl += "I";

        if(hasNominals == true)
            dl += "O";

        if(hasCardinality == true)
            dl += "N";
        else if(hasFunctionality == true)
            dl += "F";
        
        if(hasDatatype == true)
            dl += "(D)";
        
        return dl;
    }

	/**
	 * Obtain classes, properties, individuals and axioms in the set 
	 * of ontologies to be indexed. Also, reset all index maps
	 * @throws OWLException
	 */
	private void init() throws OWLException {
		
		classes = new ArrayList();
		properties = new ArrayList();
		individuals = new ArrayList();
		
		classAxioms = new ArrayList();
		propAxioms = new ArrayList();
		indAxioms = new ArrayList();
		
		Iterator ont = ontologies.iterator();
		while(ont.hasNext()) {
			OWLOntology o = (OWLOntology) ont.next();
			classes.addAll(o.getClasses());
			properties.addAll(o.getObjectProperties());
			properties.addAll(o.getDataProperties());
			individuals.addAll(o.getIndividuals());
			classAxioms.addAll(o.getClassAxioms());
			propAxioms.addAll(o.getPropertyAxioms());
			indAxioms.addAll(o.getIndividualAxioms());
			//For expressivity
			if(!(o.getDataProperties().isEmpty()))
				hasDatatype = true;
		}
		
		// reset all index maps
		this.descriptions = new HashSet();
		this.unions = new HashSet();
		this.intersections = new HashSet();
		this.disjointAxioms = new HashSet();
		this.descriptionToEntity = new HashMap();
		this.restrictionToEntity = new HashMap();
		this.equivalents = new HashMap();
		this.classesToPropertiesRestriction = new HashMap();
		this.individualsToPropertiesRestriction = new HashMap();
		this.individualsToPropertiesAssertions = new HashMap();
		this.individualsInEnumerations = new HashMap();
		this.propToIndividualAssertions = new HashMap();
		this.propToDescriptionRestrictions = new HashMap();
		this.classesInIntersections = new HashMap();
		this.classesInUnions = new HashMap();
		this.classesToDomain = new HashMap();
		this.classesToRange = new HashMap();
		this.nestedRestrictions = new HashMap();
		this.subClasses = new HashMap();
		this.superClasses = new HashMap();
		this.subProperties = new HashMap();
		this.superProperties = new HashMap();
		this.instancesOf = new HashMap();		
	}
	
	private void parseOntology() throws OWLException{
		
		
	  	Iterator it = classes.iterator();
		while(it.hasNext()){
		 	OWLClass cla = (OWLClass)it.next();
		 	
		 	// handle class equivalents
		 	Set eqs = cla.getEquivalentClasses(ontologies);
		 	// add to equivalentClasses map		 	
		 	this.addEquivalentSet(eqs, cla);
			Iterator i = eqs.iterator();
			while(i.hasNext()) {
				OWLDescription desc = (OWLDescription) i.next();				
				lookInsideDescription(desc, cla);
			}
			
			// handle subclasses	http://www.gnowsis.org/ont/vcard
			Set subs = cla.getSubClasses(ontologies);
			// add to subClasses map
			this.addToMap(this.subClasses, cla, subs);			
			Iterator j = subs.iterator();
			while(j.hasNext()) {
				OWLDescription desc = (OWLDescription) j.next();
				lookInsideDescription(desc, cla);
			}
			
			// handle superclasses	
			Set sups = cla.getSuperClasses(ontologies);
			// add to superClasses map
			this.addToMap(this.superClasses, cla, sups);
			Iterator k = sups.iterator();
			while(k.hasNext()) {
				OWLDescription desc = (OWLDescription) k.next();
				lookInsideDescription(desc, cla);
			}
			
			// adding enumerations checking (tw7)
			Set enumerations = cla.getEnumerations(ontologies);
			Iterator l = enumerations.iterator();
			while(l.hasNext()) {
				OWLDescription desc = (OWLDescription) l.next();
				lookInsideDescription(desc, cla);		
			}

		}
		
		// handle class axioms
		for (Iterator iter = classAxioms.iterator(); iter.hasNext();) {
			OWLClassAxiom axiom = (OWLClassAxiom) iter.next();
			if (axiom instanceof OWLSubClassAxiom) {
				OWLSubClassAxiom subAxiom = (OWLSubClassAxiom) axiom;
				OWLDescription sub = subAxiom.getSubClass();
				OWLDescription sup = subAxiom.getSuperClass();
				// add to sub/super classes map
				this.addToMap(this.subClasses, sup, sub);
				this.addToMap(this.superClasses, sub, sup);
				
				descriptions.add(sub);
				descriptions.add(sup);
				
				// and also look inside description
				lookInsideDescription(sub, null);
				lookInsideDescription(sup, null);
			}
			else if (axiom instanceof OWLEquivalentClassesAxiom) {
				OWLEquivalentClassesAxiom equAxiom = (OWLEquivalentClassesAxiom) axiom;				
				// add to equivalentClasses map
				this.addEquivalentSet(equAxiom.getEquivalentClasses(), null);
				this.lookInsideSet(equAxiom.getEquivalentClasses());
			}
			else if (axiom instanceof OWLDisjointClassesAxiom) 
			{
				//OWLDIsjointClassAxiom implies existence of OWL:ComplementOf
				// Note that we can have disjoints without having disjoint axioms
				// if we have GCIs.  e.g. (intersection(C,D) subclassOf OWL:Nothing)
				// this says that C and D are disjoint.  No negation used.
				//For expressivity
				this.hasNegation = true;
				this.isRDFS   = false; // no disjoint construct
				if (DEBUG_IS_EL)
					System.out.println("Not EL! Disjoint Axiom Found: in " + axiom);
				this.isEL     = false; //EL has no bottom concept
				// add to disjointAxioms set
				this.disjointAxioms.add(axiom);
				OWLDisjointClassesAxiom disAxiom = (OWLDisjointClassesAxiom) axiom;				
				this.lookInsideSet(disAxiom.getDisjointClasses());
			}
		}
			
		// handle property domains/ranges
		Iterator t = properties.iterator();
		while(t.hasNext())
		{
			OWLProperty prop = (OWLProperty)t.next();
			//For expressivity
			if (prop instanceof OWLObjectProperty){
				if(((OWLObjectProperty)prop).isTransitive(ontologies))
				{
				    isDLLite = false;
				    isRDFS = false;
				    if (DEBUG_IS_EL)
				    	System.out.println("Not EL! Transitivity Found: in " + prop );
				    isEL = false;
					hasTransitivity = true;
				}
				if(((OWLObjectProperty)prop).isSymmetric(ontologies)){
					  hasInverse = true;
					  isRDFS = false;
					  if (DEBUG_IS_EL)
						  System.out.println("Not EL! Symmetry Found: in " + prop );
					  isEL = false;
					  isELpp = false;
				}
				if(!(((OWLObjectProperty)prop).getInverses(ontologies).isEmpty())){
					  hasInverse = true;
				      isRDFS = false;
				      if (DEBUG_IS_EL)
				    	  System.out.println("Not EL! Inverse Found: in " + prop );
				      isEL   = false;
				      isELpp = false;
				}
				if(((OWLObjectProperty)prop).isFunctional(ontologies)){
				    isRDFS = false;
				    if (DEBUG_IS_EL)
				    	System.out.println("Not EL! Functionality Found: in " + prop );
				    isEL   = false;
				    isELpp = false;
				    hasFunctionality = true;
				}
				/* tw7
				 * Inverse functional can be simulated using a property that has 
				 *  an inverse, and that inverse is functional.
				 *  
				 * */
				if (((OWLObjectProperty)prop).isInverseFunctional(ontologies))
				{
					hasFunctionality = true;
					hasInverse = true;
					isRDFS = false;
					if (DEBUG_IS_EL)
						System.out.println("Not EL! InverseFunctionality Found: in " + prop );
					isEL = false;
				    isELpp = false;
				}
			}
			/*
			else
			{
				// also check for datatype properties 
				// though for OWL Lite and DL, datatype properties are not supposed to
				// be transitive, symmetric, inverse, or inverse functional 
				//  OWLAPI does not provide methods for these attributes, only functional
				// (tw7)
				
				if(((OWLDataProperty)prop).isFunctional(ontologies)){
				    hasFunctionality = true;
				    if (DEBUG_IS_EL)
				    	System.out.println("Not EL! Functionality Found: in " + prop );
				    isEL   = false;
				    isELpp = false;
				    isRDFS = false;
				}
			}
			*/
			
			// handle prop domains and ranges
			Iterator g = prop.getDomains(ontologies).iterator();
			while(g.hasNext()){
				OWLDescription desc = (OWLDescription)g.next();
				lookInsideDescription(desc, prop, true, false, false);
			}
			Iterator b = prop.getRanges(ontologies).iterator();
			if(prop instanceof OWLObjectProperty)
			{
				while(b.hasNext())
				{
                    //expressivity
					hasRange = true;
					OWLDescription desc = (OWLDescription)b.next();
					lookInsideDescription(desc, prop, false, true, false);
				}
			}
			
			// get superprops and add it to corresponding maps
			Set supProp = prop.getSuperProperties(ontologies);
			this.addToMap(this.superProperties, prop, supProp);
			for (Iterator iter = supProp.iterator(); iter.hasNext();) {
				OWLProperty sup = (OWLProperty) iter.next();
				this.addToMap(this.subProperties, sup, prop);
			}			
		}
		
		// handle property axioms
		for (Iterator iter = propAxioms.iterator(); iter.hasNext();) {
			OWLPropertyAxiom axiom = (OWLPropertyAxiom) iter.next();
			//For expressivity
			this.isDLLite = false;
			this.hasRoleHierarchy = true;
			// add subproperty axiom info to hashmaps
			if (axiom instanceof OWLSubPropertyAxiom) {
				OWLSubPropertyAxiom subAxiom = (OWLSubPropertyAxiom) axiom;
				this.addToMap(this.superProperties, subAxiom.getSubProperty(), subAxiom.getSuperProperty());
				this.addToMap(this.subProperties, subAxiom.getSuperProperty(), subAxiom.getSubProperty());
			}
			// add equivalent props axiom info to hashmap
			if (axiom instanceof OWLEquivalentPropertiesAxiom) {
				OWLEquivalentPropertiesAxiom equAxiom = (OWLEquivalentPropertiesAxiom) axiom;
				this.addEquivalentPropSet(equAxiom.getProperties());
			}
		}
		
		// handle individuals
		for (Iterator iter = individuals.iterator(); iter.hasNext(); ) {
			OWLIndividual ind = (OWLIndividual) iter.next();
			// get types
			Set types = ind.getTypes(ontologies);
			for (Iterator iter2= types.iterator(); iter2.hasNext();) {
				OWLDescription desc = (OWLDescription) iter2.next();
				//Test
				descriptions.add(desc);
				this.addToMap(this.instancesOf, desc, ind);
			}
			
			// get object property-value assertions
			Map oValues = ind.getObjectPropertyValues(ontologies);
			for (Iterator iter2 = oValues.keySet().iterator(); iter2.hasNext();) {
				OWLObjectProperty prop = (OWLObjectProperty) iter2.next();
				// add to prop->ind map
				this.addToMap(this.propToIndividualAssertions, prop, ind);
				Set vals = (HashSet) oValues.get(prop);
				for (Iterator iter3 = vals.iterator(); iter3.hasNext();) {
					OWLIndividual valueInd = (OWLIndividual) iter3.next();
					// add to ind->prop map such that R(b,a): b->R
					this.addToMap(this.individualsToPropertiesAssertions, valueInd, prop);
				}
			}
			// get data property-value assertions
			Map dValues = ind.getDataPropertyValues(ontologies);
			for (Iterator iter2 = dValues.keySet().iterator(); iter2.hasNext();) {
				OWLDataProperty prop = (OWLDataProperty) iter2.next();
				// add to prop->ind map
				this.addToMap(this.propToIndividualAssertions, prop, ind);
			}
		}
		//expressivity;
		if(hasInverse == false && hasRange == true)
		{
			if (DEBUG_IS_EL)
				System.out.println("Not EL! Has no Inverse, but has Range.");
			isEL = false;
		}
	}

	private void lookInsideDescription(OWLDescription desc, OWLEntity entity) throws OWLException {
		this.lookInsideDescription(desc, entity, false, false, false);
	}
	
	private void lookInsideDescription(
			OWLDescription desc, 
			OWLEntity entity, 
			
			boolean checkingDomain, 
			boolean checkingRange,
			boolean checkingNestedRestriction
			) throws OWLException {
		
		OWLClass owlThing = reasoner.getOntology().getOWLDataFactory().getOWLThing();
		OWLClass owlNothing = reasoner.getOntology().getOWLDataFactory().getOWLNothing();
		
		
		try {
			
			// add to description set anyway
			this.descriptions.add(desc);
			
			// add to Description->Entity map
			if (entity!=null) this.addToMap(this.descriptionToEntity, desc, entity);
			
			// add to Classes/Descriptions -> property map for domain/range
			if (entity!=null && entity instanceof OWLProperty) {
				if (checkingDomain) {
					this.addToMap(this.classesToDomain, desc, entity);
					// also if P has domain C, then any subproperty of P has domain C
					Set descendants = SetUtils.union(reasoner.descendantPropertiesOf((OWLProperty) entity));
					for (Iterator iter = descendants.iterator(); iter.hasNext();) {
						this.addToMap(this.classesToDomain, desc, (OWLProperty) iter.next());
					}
				}
				if (checkingRange) {
					this.addToMap(this.classesToRange, desc, entity);
					// also if P has range C, then any subproperty of P has range C
					Set descendants = SetUtils.union(reasoner.descendantPropertiesOf((OWLProperty) entity));
					for (Iterator iter = descendants.iterator(); iter.hasNext();) {
						this.addToMap(this.classesToRange, desc, (OWLProperty) iter.next());
					}
				}
				
				// also check and add to nestedRestrictions map
				if (checkingNestedRestriction && desc instanceof OWLObjectRestriction) {
					this.addToMap(this.nestedRestrictions, entity, ((OWLObjectRestriction) desc).getObjectProperty());
					//For expressivity
					this.isDLLite = false;
					this.isRDFS = false;
				}
			}
			
			if(desc instanceof OWLAnd){
				this.intersections.add(desc);
				Iterator it = ((OWLNaryBooleanDescription)desc).getOperands().iterator();
				while(it.hasNext()) {
					// add to classes in intersections map
					OWLDescription andOperand = (OWLDescription) it.next();
					this.addToMap(this.classesInIntersections, andOperand, desc);
					
					lookInsideDescription(andOperand, entity, checkingDomain, checkingRange, checkingNestedRestriction);
				}
			}
			
			if(desc instanceof OWLOr){
				this.unions.add(desc);
				//For expressivity
				this.isDLLite = false;
				this.isEL     = false;
				this.isELpp   = false;
				this.isRDFS   = false;
				if (DEBUG_UNIONS)
					System.out.println("<Has Union>: in " + entity + " using " + desc);
				this.hasUnion = true;
				
                //			
				Iterator it = ((OWLNaryBooleanDescription)desc).getOperands().iterator();
				while(it.hasNext()) {
					// add to classes in unions map
					OWLDescription orOperand = (OWLDescription) it.next();
					this.addToMap(this.classesInUnions, orOperand, desc);
					
					lookInsideDescription(orOperand, entity, checkingDomain, checkingRange, checkingNestedRestriction);
				}
			}
			
			if(desc instanceof OWLNot){
			   //For expressivity
			   OWLDescription de = ((OWLNot)desc).getOperand();
			   if(!(de instanceof OWLClass)){
			   	  this.isDLLite = false;
			   }
			   // OWLNothing complementOf OWLThing does not count for 'hasNegation' tw7
			   if ( ( de instanceof OWLClass ) && ( entity instanceof OWLClass ))
			   {
				   OWLClass a = (OWLClass)entity;
				   OWLClass b = (OWLClass)de;
				   if (! ((a.equals(owlThing) && b.equals(owlNothing)) ||
						(a.equals(owlNothing) && b.equals(owlThing))) )
				   {
					   if (DEBUG_NEGATIONS)
						   System.out.println("<Has Negation>: in " + a + " using " + b);
					   this.hasNegation = true;
					   this.isRDFS = false;
					   if (DEBUG_IS_EL)
						   System.out.println("Not EL! Complement Found: in " + entity + " using " +de );
					   this.isEL   = false;
					   this.isELpp = false;
				   }
			   }
			   else
			   {
				   if (DEBUG_NEGATIONS)
					   System.out.println("<Has Negation>: in " + entity + " using " + desc );
				   this.hasNegation = true;
				   this.isRDFS = false;
				   if (DEBUG_IS_EL)
					   System.out.println("Not EL! Complement Found: in " + entity + " using " +de );
				   this.isEL   = false;
				   this.isELpp = false;
			   }
			   lookInsideDescription(((OWLNot)desc).getOperand(), entity, checkingDomain, checkingRange, checkingNestedRestriction);
			}
			
			if (desc instanceof OWLEnumeration) 
			{
				// add individual->enumeration to corresponding hashmap
				OWLEnumeration enu = (OWLEnumeration) desc;
				if ( DEBUG_NOMINALS )
					System.out.println( "<Nominals>: " + entity + " Has Enumeration " + desc);
				//For expressivity
				 this.isRDFS = false;
				 this.isDLLite = false;
				 this.hasNominals = true;
				 if (DEBUG_IS_EL)
					 System.out.println( entity + "Not EL! has Enumeration: " + desc );
				 this.isEL = false;
				 if ( enu.getIndividuals().size() > 1)
				 {
					if (DEBUG_UNIONS)
						System.out.println("<Has Union>: Enumeration in " + entity + " using " + desc);

					this.hasUnion = true; //(tw7) OneOf that has more than 1 element is disjunction
					this.isELpp   = false;     //(tw7) EL++ only allows for enum of single element
				 }
				//
				for (Iterator iter = enu.getIndividuals().iterator(); iter.hasNext();) {
					OWLIndividual ind = (OWLIndividual) iter.next();
					this.addToMap(this.individualsInEnumerations, ind, desc);
				}						
			}

			if(desc instanceof OWLRestriction){
				// add to prop->Description map
				OWLProperty prop = ((OWLRestriction) desc).getProperty();
				//For expressivity
				this.isRDFS = false; //RDFS has no restrictions
				
				// only check for object restrictions because datatype restrictins are handled
				// by datatype reasoners and don't necessaritly cause compexity jump (?) (from Bijan)
				if ( desc instanceof OWLObjectQuantifiedRestriction) 					
				{
					if ( desc instanceof OWLObjectQuantifiedRestriction )
					{
						if(!(((OWLObjectQuantifiedRestriction)desc).getDescription().equals(owlThing)))
							this.isDLLite = false;
					}
					if ((desc instanceof OWLObjectAllRestriction) 
						|| (desc instanceof OWLDataQuantifiedRestriction))
					{
						this.isDLLite = false; // causes intractability (tw7)
						if (DEBUG_IS_EL)
							System.out.println("Not EL! AllValue Restriction Found: in " + entity );
						this.isEL     = false; // causes intractability
						this.isELpp   = false; // causes intractability (tw7)
					}
				}				
				else if ( desc instanceof OWLObjectValueRestriction )
				{
					if ( DEBUG_NOMINALS )
						System.out.println( "<Nominals>: " + entity + " Has ValueRestriction" + desc);
					this.isDLLite = false;
					this.hasNominals = true;
					if (DEBUG_IS_EL)
						System.out.println("Not EL! Nominals Found: in " + entity );
					this.isEL = false;
				}
				else if(desc instanceof OWLCardinalityRestriction )
				{
					if (DEBUG_IS_EL)
						System.out.println("Not EL! Card Restriction Found: in " + entity );
					isEL     = false; //EL has no functionality construct
						              //  ELF is not tractable (tw7)
					isELpp   = false; // causes intracability (tw7)
					isRDFS   = false; //RDFS has no functional or cardinality restrictions (tw7)
					
					if(((OWLCardinalityRestriction)desc).isAtMost()){
	               	 	int m = ((OWLCardinalityRestriction)desc).getAtMost();
	               	 	if(m <= 1)
	               	 	{
	               	 		if (! (desc instanceof OWLDataCardinalityRestriction) )
	               	 				hasFunctionality = true; 
	               	 	}
	               	 	else
	               	 	{
	               	 		hasCardinality = true;
	               	 		isDLLite = false; //DLLite has no cardinality construct
	               	 						  // (DLLite is subset of OWL Lite) (tw7)
	               	 	}
	               	 }
	               	 if(((OWLCardinalityRestriction)desc).isAtLeast()){
	               	 	int m = ((OWLCardinalityRestriction)desc).getAtLeast();
	               	 	// [BUGBUG] need to check if user is using Not(atLeast(2)) to represent less than 2. 
	               	 	if(m <= 1 )
	               	 	{
	               	 		if (! (desc instanceof OWLDataCardinalityRestriction) )
	               	 			hasFunctionality = true; 
	               	 	}
	               	 	else
	               	 	{
	               	 		hasCardinality = true ;
	               	 		isDLLite = false; //DLLite has no cardinality construct
 	 						                  // (DLLite is subset of OWL Lite) (tw7)
	               	 	}
	               	 }
				}
				//end expressivity
				this.addToMap(this.propToDescriptionRestrictions, prop, desc);
				this.lookInsideRestriction((OWLRestriction) desc, entity);
			}
		}
		catch (OWLException ex) {
			ex.printStackTrace();
		}
	}
	
	private void lookInsideRestriction(OWLRestriction rest, OWLEntity entity) 
	{
		// add to Restriction->Entity map
		this.addToMap(this.restrictionToEntity, rest, entity);
		
		if (rest instanceof OWLObjectQuantifiedRestriction) {
			try {
				// add to Class->Properties in Restriction map
				OWLDescription d = ((OWLObjectQuantifiedRestriction) rest).getDescription();				
				
				// also add it to the main descriptions set
				this.descriptions.add(d);
				
				//for expressivity 
				if(!(d instanceof OWLClass))
				{
					if (DEBUG_HASCOMPLEXRESTRICTION)
						System.out.println("<Has Complex Restriction>: in " + entity + " using " + d);
					hasComplexRestriction = true;
					isDLLite = false;
				}
				//
				this.addToMap(this.classesToPropertiesRestriction, d, rest.getProperty());
				
				// also look inside descriptions which appear in some/all restrictions on object properties
				this.lookInsideDescription(d, entity, false, false, false);
				
				// finally, also compute nestedRestrictions map here
				// ** set checkingNestedRestriction = true here ** 
				this.lookInsideDescription(d, rest.getProperty(), false, false, true);
			} 
			catch (OWLException e) {
				e.printStackTrace();
			}
		}
		else if (rest instanceof OWLObjectValueRestriction) {
			try {
				// add to Individual->Properties in Restriction map
				OWLIndividual ind = ((OWLObjectValueRestriction) rest).getIndividual();				
				this.addToMap(this.individualsToPropertiesRestriction, ind, rest.getProperty());
			} 
			catch (OWLException e) {
				e.printStackTrace();
			}
		}		
	}
	
	
	/**
	 * Given a map and a key-value pair, add the value to the hashset 
	 * indexed by the key in the map.
	 * @param map
	 * @param key
	 * @param value
	 */
	private void addToMap(Map map, Object key, Object value) {
		Set index = new HashSet();
		if (map.containsKey(key)) index.addAll((HashSet) map.get(key));
		if (value instanceof Set)
			index.addAll((HashSet) value);
		else 
			index.add(value);
		map.put(key, index);
	}
	
	/**
	 * Look inside all the descriptions inside a set and obtain
	 * all pair-wise cross-references. Used for equivalent and disjoint sets
	 * specified as OWLClassAxioms
	 * @param set
	 */
	private void lookInsideSet(Set set) throws OWLException {
		for (Iterator iter = set.iterator(); iter.hasNext();) {
			OWLDescription desc = (OWLDescription) iter.next();
			this.lookInsideDescription(desc, null);
//			descriptions.add(desc);
//			Set copy = new HashSet(set);
//			copy.remove(desc);
//			for (Iterator iter2=copy.iterator(); iter2.hasNext();) {
//				OWLDescription desc2 = (OWLDescription) iter2.next();
//				if (desc instanceof OWLClass) this.lookInsideDescription(desc2, (OWLClass) desc);
//				if (desc2 instanceof OWLClass) this.lookInsideDescription(desc, (OWLClass) desc2);
//			}
		}
	}
	
	/**
	 * For a given set of equivalent descriptions, add each equivalent 
	 * pairwise to the hashmap - equivalentClasses.
	 * @param set
	 */
	private void addEquivalentSet(Set origSet, OWLDescription cla) {
		Set set = new HashSet(origSet);
		if (cla!=null) set.add(cla);
		for (Iterator iter = set.iterator(); iter.hasNext();) {
			OWLDescription desc = (OWLDescription) iter.next();
			Set copy = new HashSet(set);
			copy.remove(desc);
			for (Iterator iter2=copy.iterator(); iter2.hasNext();) {
				OWLDescription desc2 = (OWLDescription) iter2.next();
				this.addToMap(this.equivalents, desc, desc2);
			}
		}
	}
	
	private void addEquivalentPropSet(Set origSet) {
		Set set = new HashSet(origSet);
		for (Iterator iter = set.iterator(); iter.hasNext();) {
			OWLProperty prop = (OWLProperty) iter.next();
			Set copy = new HashSet(set);
			copy.remove(prop);
			for (Iterator iter2=copy.iterator(); iter2.hasNext();) {
				OWLProperty prop2 = (OWLProperty) iter2.next();
				this.addToMap(this.equivalents, prop, prop2);
			}
		}
	}
	
}
