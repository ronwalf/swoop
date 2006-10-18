package org.mindswap.swoop.reasoner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mindswap.swoop.utils.ExpressivityChecker;
import org.mindswap.swoop.utils.SetUtils;
import org.mindswap.swoop.utils.owlapi.OntologyIndices;
import org.mindswap.swoop.utils.owlapi.QNameShortFormProvider;
import org.semanticweb.owl.io.ShortFormProvider;
import org.semanticweb.owl.model.OWLAnd;
import org.semanticweb.owl.model.OWLAnnotationProperty;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLClassAxiom;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDisjointClassesAxiom;
import org.semanticweb.owl.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owl.model.OWLEquivalentPropertiesAxiom;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLIndividualAxiom;
import org.semanticweb.owl.model.OWLNot;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOr;
import org.semanticweb.owl.model.OWLProperty;
import org.semanticweb.owl.model.OWLPropertyAxiom;
import org.semanticweb.owl.model.OWLSameIndividualsAxiom;
import org.semanticweb.owl.model.OWLSubClassAxiom;
import org.semanticweb.owl.model.OWLSubPropertyAxiom;
import org.semanticweb.owl.model.helper.OntologyHelper;

/**
 * @author Aditya Kalyanpur
 */
public class SwoopRDFSReasoner implements SwoopReasoner {
	
	public static boolean DEBUG = false; //	 for debugging purposes only
	boolean loadImports = true;
	private Set ontologies;
	private OWLOntology ontology;
	private List classes, properties, individuals;
	private List classAxioms, propAxioms, indAxioms;
	private ShortFormProvider shortForms = new QNameShortFormProvider();
	private Map incLinks, outLinks, equivalents, complements, disjoints;
	private Map classInds, sameAs, differentFrom;
	private Map inverses;
	private OntologyIndices indices;
	
	public void setOntology(OWLOntology ont) throws OWLException {
		ontology = ont;		
		if(loadImports)
			ontologies = OntologyHelper.importClosure(ontology);
		else
			ontologies = Collections.singleton(ontology);
		
		classify();
		buildIndices();
	}
	
	/**
	 * Check if a subclass relationship between two OWL descriptions subClass(subC, supC)
	 * needs to be added to the incoming/outgoing links hashmaps. Check
	 * 1. If both subC, supC are OWL Classes
	 * 2. If subC is a OWL Class and supC is an OWLAnd
	 * 3. If subC is an OWLOr and supC is a OWL Class
	 * @param subC
	 * @param supC
	 * @throws OWLException
	 */
	public void subClassCheck(OWLDescription subC, OWLDescription supC) throws OWLException {
	
		if (subC instanceof OWLClass) {
			if (supC instanceof OWLClass) this.addSubSuperClass((OWLClass) subC, (OWLClass) supC);
			else
				// if A subCof (B AND C) -> (A subCOf B) and (A subCOf C) 
				if (supC instanceof OWLAnd) {
				for (Iterator iter = ((OWLAnd) supC).getOperands().iterator(); iter.hasNext(); ) {
					OWLDescription desc = (OWLDescription) iter.next();
					if (desc instanceof OWLClass) addSubSuperClass((OWLClass) subC, (OWLClass) desc);
				}
			}
		}
		else 
		if (subC instanceof OWLOr && supC instanceof OWLClass) {
			// if (A OR B) subCOf C -> (A subCOf C) and (B subCOf C)
			for (Iterator iter = ((OWLOr) subC).getOperands().iterator(); iter.hasNext(); ) {
				OWLDescription desc = (OWLDescription) iter.next();
				if (desc instanceof OWLClass) addSubSuperClass((OWLClass) desc, (OWLClass) supC);
			}
		}
	}
	
	/**
	 * Compute subclass, equivalence, complement and disjoint relations 
	 * for all classes in ontology and add it to corresponding HashMaps
	 * 1. check direct assertions (sub/equ)
	 * 2. check class axioms (GCIS) (sub/equ/dis)
	 * @throws OWLException
	 */
	private void computeClassRelations() throws OWLException {
		
		// handle class assertions
		// superClass and equivalentClass
		for(int i = 0; i < classes.size(); i++) {
			OWLClass c = (OWLClass) classes.get(i);
			Iterator supers = c.getSuperClasses(ontologies).iterator();
			while(supers.hasNext()) {
				OWLDescription sup = (OWLDescription) supers.next();
				this.subClassCheck(c, sup);								
			}
			
			Iterator equs = c.getEquivalentClasses(ontologies).iterator();
			while(equs.hasNext()) {
				OWLDescription equ = (OWLDescription) equs.next();
				this.addEquivalentClass(c, equ);			
			}			
		}
		
		// handle class axioms
		// OWLSubClass, OWLEquivalentClasses and OWLDisjointClasses
		for (int i=0; i<classAxioms.size(); i++) {
			OWLClassAxiom axiom = (OWLClassAxiom) classAxioms.get(i);
			if (axiom instanceof OWLSubClassAxiom) {
				OWLSubClassAxiom subAxiom = (OWLSubClassAxiom) axiom;
				this.subClassCheck(subAxiom.getSubClass(), subAxiom.getSuperClass());
								
			}
			else if (axiom instanceof OWLEquivalentClassesAxiom) {
				OWLEquivalentClassesAxiom equAxiom = (OWLEquivalentClassesAxiom) axiom;
				Set equs = equAxiom.getEquivalentClasses();
				for (Iterator iter = equs.iterator(); iter.hasNext(); ) {
					OWLDescription desc = (OWLDescription) iter.next();
					if (desc instanceof OWLClass) {
						Set copyEqus = new HashSet(equs);
						copyEqus.remove(desc);
						for (Iterator iter2 = copyEqus.iterator(); iter2.hasNext(); ) {
							OWLDescription desc2 = (OWLDescription) iter2.next();
							this.addEquivalentClass((OWLClass) desc, desc2);
						}
					}
				}
			}
			else if (axiom instanceof OWLDisjointClassesAxiom) {
				OWLDisjointClassesAxiom disAxiom = (OWLDisjointClassesAxiom) axiom;
				Set dis = disAxiom.getDisjointClasses();
				for (Iterator iter = dis.iterator(); iter.hasNext(); ) {
					OWLDescription desc = (OWLDescription) iter.next();
					if (desc instanceof OWLClass) {
						Set copyDis = new HashSet(dis);
						copyDis.remove(desc);
						for (Iterator iter2 = copyDis.iterator(); iter2.hasNext(); ) {
							OWLDescription desc2 = (OWLDescription) iter2.next();
							if (desc2 instanceof OWLClass) {
								// add disjoint between desc and desc2 when both are classes
								this.addDisjointClass((OWLClass) desc, (OWLClass) desc2);
							}
						}
					}
				}
			}
		}
		
	}
	
	/*
	 * Add complement assertion between two classes to the corresponding hashmap
	 */
	private void addComplementClass(OWLClass c1, OWLClass c2) {
		Set compSet1 = new HashSet(); // complements of c1
		Set compSet2 = new HashSet(); // complements of c2
		if (complements.containsKey(c1)) compSet1.addAll((HashSet) complements.get(c1));
		if (complements.containsKey(c2)) compSet2.addAll((HashSet) complements.get(c2));
		complements.put(c1, compSet1);
		complements.put(c2, compSet2);
	}
	
	/*
	 * Add disjoint assertion between two classes to the corresponding hashmap
	 */
	private void addDisjointClass(OWLClass c1, OWLClass c2) {
		Set disSet1 = new HashSet(); // disjoints of c1
		Set disSet2 = new HashSet(); // disjoints of c2
		if (disjoints.containsKey(c1)) disSet1.addAll((HashSet) disjoints.get(c1));
		if (disjoints.containsKey(c2)) disSet2.addAll((HashSet) disjoints.get(c2));
		disjoints.put(c1, disSet1);
		disjoints.put(c2, disSet2);
	}
	
	/**
	 * Add equivalent class relation to the hashmap - equivalents.
	 * Also perform special check for: A = (B and C) -> A subCOf B..
	 * @param cla - class
	 * @param desc - equivalent class description
	 * @throws OWLException
	 */
	private void addEquivalentClass(OWLClass cla, OWLDescription desc) throws OWLException {
		
		Set equMap = new HashSet();	
		equMap.add(cla); // add class itself to its equivalents map
		
		// get existing map, if any
		if (equivalents.containsKey(cla)) equMap.addAll((HashSet) equivalents.get(cla));
		
		// add only class to map (no descriptions)
		if (desc instanceof OWLClass) {
			equMap.add(desc);
			// get equivalents of desc as well
			if (equivalents.containsKey(desc)) equMap.addAll((HashSet) equivalents.get(desc));
		}
		else if (desc instanceof OWLAnd) {
			// if A = (B AND C); A subCOf B, A subCOf C
			for (Iterator iter = ((OWLAnd) desc).getOperands().iterator(); iter.hasNext(); ) {
				OWLDescription equ = (OWLDescription) iter.next();
				if (equ instanceof OWLClass) {
					this.addSubSuperClass(cla, (OWLClass) equ);
				}
				else if (equ instanceof OWLAnd) {
					// check for nested intersections!
					this.addEquivalentClass(cla, equ);
				}
			}			
		}
		else if (desc instanceof OWLNot) {
			// if complement, put in separate hashmap
			OWLNot not = (OWLNot) desc;
			if (not.getOperand() instanceof OWLClass)
				this.addComplementClass(cla, (OWLClass) not.getOperand());
		}
		
		// put the equivalents for each class in equMap
		for (Iterator iter = equMap.iterator(); iter.hasNext();) {
			OWLClass equCla = (OWLClass) iter.next();
			equivalents.put(equCla, equMap);
		}
	}
	
	private void addSubSuperProperty(OWLProperty subProp, OWLProperty superProp) {
		
		// get existing superset if any
		Set superSet = new HashSet();
		if (outLinks.containsKey(subProp)) superSet = (HashSet) outLinks.get(subProp);
		superSet.add(superProp);
		outLinks.put(subProp, superSet);
		
		// get existing subset if any
		Set subSet = new HashSet();
		if (incLinks.containsKey(superProp)) subSet = (HashSet) incLinks.get(superProp);
		subSet.add(subProp);
		incLinks.put(superProp, subSet);
		
	}
	
	private void addEquivalentProperty(OWLProperty prop1, OWLProperty prop2) throws OWLException {
		if (prop1.equals(prop2)) return;
		
		Set equSet = new HashSet();
		equSet.add(prop1); // add property itself to equivalent set
		
		if (equivalents.containsKey(prop1)) equSet.addAll((HashSet) equivalents.get(prop1));
		equSet.add(prop2);
		
		// put same equivalents set for each property in hashmap
		for (Iterator iter = equSet.iterator(); iter.hasNext();) {
			OWLProperty prop = (OWLProperty) iter.next();
			equivalents.put(prop, equSet);
		}
	}
	
	/**
	 * Add sub-super class relationship links to the two hashmaps :- 
	 * incoming links (incLinks) and outgoing links (outLinks). 
	 * 
	 * @param subClass - subclass description
	 * @param superClass - super class description
	 * @throws OWLException
	 */
	private void addSubSuperClass(OWLClass subClass, OWLClass superClass) throws OWLException {
		
		// get existing superset if any
		Set superSet = new HashSet();
		if (outLinks.containsKey(subClass)) superSet = (HashSet) outLinks.get(subClass);
		
		// get existing subset if any
		Set subSet = new HashSet();
		if (incLinks.containsKey(superClass)) subSet = (HashSet) incLinks.get(superClass);
		
		superSet.add(superClass);
		subSet.add(subClass);
		
		// add to outgoing and incoming links maps respectively
		outLinks.put(subClass, superSet);
		incLinks.put(superClass, subSet);
	}
	
	private void init() throws OWLException {		
		Set classSet = new HashSet();
		Set propSet = new HashSet();
		individuals = new ArrayList();
		
		classAxioms = new ArrayList();
		propAxioms = new ArrayList();
		indAxioms = new ArrayList();
		
		incLinks = new HashMap();
		outLinks = new HashMap();
		equivalents = new HashMap();
		complements = new HashMap();
		disjoints = new HashMap();
		
		classInds = new HashMap();
		sameAs = new HashMap();
		differentFrom = new HashMap();
		
		inverses = new HashMap();
		
		Iterator ont = ontologies.iterator();
		while(ont.hasNext()) {
			OWLOntology o = (OWLOntology) ont.next();
			classSet.addAll(o.getClasses());
			propSet.addAll(o.getObjectProperties());
			propSet.addAll(o.getDataProperties());
			propSet.addAll(o.getAnnotationProperties());
			individuals.addAll(o.getIndividuals());
			classAxioms.addAll(o.getClassAxioms());
			propAxioms.addAll(o.getPropertyAxioms());
			indAxioms.addAll(o.getIndividualAxioms());
		}
		classes = new ArrayList(classSet.size() + 1);
		properties = new ArrayList(propSet);
		
		OWLClass thing = ontology.getOWLDataFactory().getOWLThing();
		OWLClass nothing = ontology.getOWLDataFactory().getOWLNothing();
		// we want owl:Thing and owl:Nothing to be always at the beginning
		classes.add(thing);
		classes.add(nothing);
		// remove multiple copies if exists
		classSet.remove(thing);
		classSet.remove(nothing);
		
		classes.addAll(classSet);
		
	}
	
	/**
	 * Compute subproperty and equivalence relations for all properties 
	 * in ontology and add it to corresponding HashMaps
	 * 1. check direct assertions (sub/equ)
	 * 2. check property axioms (GCIS) (sub/equ)
	 * @throws OWLException
	 */
	private void computePropertyRelations() throws OWLException {
	
		for(int i = 0; i < properties.size(); i++) {
			OWLProperty p = (OWLProperty) properties.get(i);
			
			// handle super properties
			Iterator supers = p.getSuperProperties(ontologies).iterator();
			while(supers.hasNext()) {
				OWLProperty sup = (OWLProperty) supers.next();
				this.addSubSuperProperty(p, sup);												
			}
			
			// handle inverse properties
			if (p instanceof OWLObjectProperty) {
				for (Iterator invIter = ((OWLObjectProperty) p).getInverses(ontologies).iterator(); invIter.hasNext();) {
					OWLObjectProperty invP = (OWLObjectProperty) invIter.next();
					// add inverses to set of each property
					this.addInverse((OWLObjectProperty) p, invP);
					this.addInverse(invP, (OWLObjectProperty) p);
				}
			}
		}
		
		// handle explicit axioms
		for (int i=0; i<propAxioms.size(); i++) {
			OWLPropertyAxiom axiom = (OWLPropertyAxiom) propAxioms.get(i);
			if (axiom instanceof OWLSubPropertyAxiom) {
				OWLSubPropertyAxiom subAxiom = (OWLSubPropertyAxiom) axiom;
				this.addSubSuperProperty(subAxiom.getSubProperty(), subAxiom.getSuperProperty());
			}
			else if (axiom instanceof OWLEquivalentPropertiesAxiom) {
				OWLEquivalentPropertiesAxiom equAxiom = (OWLEquivalentPropertiesAxiom) axiom;
				Set equs = equAxiom.getProperties();
				for (Iterator iter = equs.iterator(); iter.hasNext(); ) {
					OWLProperty prop = (OWLProperty) iter.next();
					Set copyEqus = new HashSet(equs);
					copyEqus.remove(prop);
					for (Iterator iter2 = copyEqus.iterator(); iter2.hasNext(); ) {
						OWLProperty prop2 = (OWLProperty) iter2.next();
						this.addEquivalentProperty(prop, prop2);
					}
				}
			}
		}
	
	}
	
	private void addInverse(OWLObjectProperty p1, OWLObjectProperty p2) {
		Set invSet = new HashSet();
		if (this.inverses.get(p1)!=null) invSet.addAll((HashSet) inverses.get(p1));
		invSet.add(p2);
		this.inverses.put(p1, invSet);
	}
	
	/**
	 * Compute top-level classes in the ontology and owl:Thing as their superclass
	 * @throws OWLException
	 */
	private void computeTopClasses() throws OWLException {
		OWLClass thing = ontology.getOWLDataFactory().getOWLThing();
		OWLClass nothing = ontology.getOWLDataFactory().getOWLNothing();
		// get set of classes that have atleast one outgoing (subClassOf) link
		Set subclasses = outLinks.keySet();		
		Set topClasses = new HashSet(classes);
		// remove subclasses from total set of classes to get top level classes
		topClasses.removeAll(subclasses);
		topClasses.remove(thing);
		topClasses.remove(nothing);
		
		// add owl:Thing as an outgoing link for topClasses alone
		for (Iterator iter = topClasses.iterator(); iter.hasNext();) {
			OWLClass cla = (OWLClass) iter.next();			
			// also check if equivalents of topClasses have outgoing links
			boolean isTop = true;
			Set equ = this.equivalentClassesOf(cla);
			equ.add(cla);
			for (Iterator iter2 = equ.iterator(); iter2.hasNext();) {
				OWLClass equCla = (OWLClass) iter2.next();
				// check if outgoing links don't come back to equivalent class (cycle)
				if (outLinks.containsKey(equCla)) {
					Set out = new HashSet((HashSet) outLinks.get(equCla));
					out.add(equCla);
					isTop = SetUtils.subset(equ, out);					
				}
			}
			if (isTop) this.addSubSuperClass(cla, thing);
		}
		
	}
	
	private void removeSubSuperClass(OWLClass cl, OWLClass supCl) throws OWLException 
	{
		Set equ = this.equivalentClassesOf(cl);
		equ.add(cl);
		Set equ2 = this.equivalentClassesOf(supCl);
		equ2.add(supCl);
		// remove supCl (and all its equivalents) from outLinks
		// for key cl (and all its equivalents)
		for (Iterator iter = equ.iterator(); iter.hasNext();) {
			OWLClass cla = (OWLClass) iter.next();
			if (outLinks.containsKey(cla)) {
				((HashSet) outLinks.get(cla)).removeAll(equ2);
				if (((HashSet) outLinks.get(cla)).size()==0) outLinks.remove(cla);
			}
		}
		// remove cl (and all its equivalents) from incLinks
		// for key supCl (and all its equivalents)
		for (Iterator iter = equ2.iterator(); iter.hasNext();) {
			OWLClass cla = (OWLClass) iter.next();
			if (incLinks.containsKey(cla)) {
				((HashSet) incLinks.get(cla)).removeAll(equ);
				if (((HashSet) incLinks.get(cla)).size()==0) incLinks.remove(cla);
			}
		}
		
	}
	
	private void removeSubSuperProperty(OWLProperty p, OWLProperty supP) throws OWLException {
		Set equ = this.equivalentPropertiesOf(p);
		equ.add(p);
		Set equ2 = this.equivalentPropertiesOf(supP);
		equ2.add(supP);
		// remove supP (and all its equivalents) from outLinks
		// for key p (and all its equivalents)
		for (Iterator iter = equ.iterator(); iter.hasNext();) {
			OWLProperty prop = (OWLProperty) iter.next();
			if (outLinks.containsKey(prop)) {
				((HashSet) outLinks.get(prop)).removeAll(equ2);
			}
		}
		// remove p (and all its equivalents) from incLinks
		// for key supP (and all its equivalents)
		for (Iterator iter = equ2.iterator(); iter.hasNext();) {
			OWLProperty prop = (OWLProperty) iter.next();
			if (incLinks.containsKey(prop)) {
				((HashSet) incLinks.get(prop)).removeAll(equ);
			}
		}
		
	}
	
	/*
	 * Check for multiple paths in the class/property hierarchy and
	 * eliminate redundant path(s). This prevents the class/property node
	 * from occuring twice in the class/property tree along the same hierarchical chain  
	 */
	private void computeMultiplePaths() throws OWLException {
		// check multiple subClass hierarchies
		// and keep only the lowest hanging class node
		// eg. A->B->C and A->C => remove A->C
		for (Iterator iter = classes.iterator(); iter.hasNext();) {
			// for each class in the ontology
			OWLClass cla = (OWLClass) iter.next();
			// get set of equivalent superclass sets
			Set setOfSets = this.superClassesOf(cla);			
			for (Iterator iter2=setOfSets.iterator(); iter2.hasNext();) {
				// for each superClass set (1)
				Set supSet = (HashSet) iter2.next();
				
				// take all *other* superclasses (2)
				Set others = new HashSet(setOfSets);
				others.remove(supSet);
				others = SetUtils.union(others);
				
				// and check for subClass relationship for every class pair (1, 2)
				for (Iterator iter3 = supSet.iterator(); iter3.hasNext();) {
					
					Object owlObj = iter3.next();
					if (!(owlObj instanceof OWLClass)) continue;
					OWLClass supCla = (OWLClass) owlObj;
					
					// check for loops A subCOf B, B subCOf A
					if (outLinks.get(supCla)!=null) {
						if (((HashSet) outLinks.get(supCla)).contains(cla)) 
						{
							this.removeSubSuperClass(cla, supCla);
							this.removeSubSuperClass(supCla, cla);
							this.addEquivalentClass(cla, supCla);
							continue;
						}
					}
					
					// check for A subCOf B and A=B
					if (equivalents.get(cla)!=null) {
						if (((HashSet) equivalents.get(cla)).contains(supCla)) 
						{
							this.removeSubSuperClass(cla, supCla);
							continue;
						}
					}
					
					// finally check for alternate paths
					for (Iterator iter4 = others.iterator(); iter4.hasNext();) 
					{
						OWLClass remCla = (OWLClass) iter4.next();
						if (this.isSubClassOf(supCla, remCla)) 
						{
							// check to see if cla and remCla are on a circle (if true, don't remove it)							
							Set sub1 = this.descendantClassesOf( cla );
							Set sub2 = this.descendantClassesOf( remCla );
							sub1.retainAll( sub2 );
							if ( sub1.size() > 0 )
								continue;
							this.removeSubSuperClass(cla, remCla); 
						}					
					}
				}
			}
		}
		
		// do the same for the property hierarchy
		for (Iterator iter = properties.iterator(); iter.hasNext();) {
			// for each property in the ontology
			OWLProperty prop = (OWLProperty) iter.next();
			// get set of equivalent superproperty sets
			Set setOfSets = this.superPropertiesOf(prop);			
			for (Iterator iter2=setOfSets.iterator(); iter2.hasNext();) {
				// for each superClass set (1)
				Set supSet = (HashSet) iter2.next();
				
				// take all *other* superproperty sets (2)
				Set others = new HashSet(setOfSets);
				others.remove(supSet);
				others = SetUtils.union(others);
				
				// and check for subProperty relationship for every property pair (1, 2)
				for (Iterator iter3 = supSet.iterator(); iter3.hasNext();) {
					
					Object owlObj = iter3.next();
					if (!(owlObj instanceof OWLProperty)) continue;
					OWLProperty supProp = (OWLProperty) owlObj;
					
					// check for loops A subPOf B, B subPOf A
					if (outLinks.get(supProp)!=null) {
						if (((HashSet) outLinks.get(supProp)).contains(prop)) {
							this.removeSubSuperProperty(prop, supProp);
							this.removeSubSuperProperty(supProp, prop);
							this.addEquivalentProperty(prop, supProp);
						}
					}
					
					for (Iterator iter4 = others.iterator(); iter4.hasNext();) {
						OWLProperty remProp = (OWLProperty) iter4.next();
						if (this.isSubPropertyOf(supProp, remProp)) {
							this.removeSubSuperProperty(prop, remProp); 
						}					
					}
				}
			}
		}
	}
	
	private void classify() {
		try {
			init();
			this.computeClassRelations();
			this.computeMultiplePaths();
			this.computeTopClasses();
			this.computePropertyRelations();
			this.computeIndividualRelations();			
		}
		catch (OWLException e) {
			e.printStackTrace();
		}
	}
	
	public Set getSameAsIndividuals(OWLIndividual ind) {
		if (sameAs.containsKey(ind)) return (HashSet) sameAs.get(ind);
		else return new HashSet();
	}

	public Set getDifferentFromIndividuals(OWLIndividual ind) {		
		if (differentFrom.containsKey(ind)) return (HashSet) differentFrom.get(ind);
		else return new HashSet();
	}
	
	/**
	 * Check for assertions/relations involving all individuals in the ontology:
	 *  1. instance types (assertions)
	 *  2. sameAs or differentFrom (axioms)
	 * 
	 */
	private void computeIndividualRelations() throws OWLException {
		
		// check types of individuals
		OWLClass thing = ontology.getOWLDataFactory().getOWLThing();
		// add owl:Thing as a default type of every individual
		Set allindSet = new HashSet();
		allindSet.addAll(individuals);
		classInds.put(thing, allindSet);
		
		for (Iterator iter = individuals.iterator(); iter.hasNext();) {
			OWLIndividual ind = (OWLIndividual) iter.next();
			Set types = ind.getTypes(ontologies);
			for (Iterator iter2 = types.iterator(); iter2.hasNext();) {
				OWLDescription desc = (OWLDescription) iter2.next();
				Set indSet = new HashSet();
				if (classInds.containsKey(desc)) indSet = (HashSet) classInds.get(desc);
				indSet.add(ind);
				classInds.put(desc, indSet);
			}
		}
		
		// iterate through each ontology
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

	public String getName() {
		return "RDFS-like";
	}

	/**
	 * refreshOntology
	 * 
	 * 
	 */
	private void refreshOntology() throws OWLException {
		if(ontology != null) setOntology(ontology);
	}
	
	public void setLoadImports(boolean useImports, boolean refresh) throws OWLException {
		this.loadImports = useImports;
		if (refresh) this.refreshOntology();
	}

	public boolean loadImports() {
		return this.loadImports;
	}

	public boolean isConsistent() {
		return true;
	}

	public boolean isConsistent(OWLClass c) throws OWLException {
		return true;
	}

	public Set typesOf(OWLIndividual ind) throws OWLException {
		Set types = ind.getTypes(ontologies);
		// remove unnamed types
		for (Iterator iter = new HashSet(types).iterator(); iter.hasNext();) {
			OWLDescription desc = (OWLDescription) iter.next();
			if (!(desc instanceof OWLClass)) types.remove(desc);
		}
		return this.getSetofEquivalentSets(types);
	}

	public String getExpressivity() throws OWLException {
		if (indices!=null) {
			String expressivity = indices.getExpressivity(ontology);
			return ExpressivityChecker.getExplanation(expressivity);
		}
		return "Unable to determine";
	}

	public Set allTypesOf(OWLIndividual ind) throws OWLException {
		Set types = ind.getTypes(ontologies);
		Set copy = new HashSet(types);
		for (Iterator iter = copy.iterator(); iter.hasNext();) {
			OWLClass cla = (OWLClass) iter.next();
			List classTracker = new ArrayList();
			Set resultSet = this.getClassHierarchy(cla, classTracker, "SUPER");
			types.addAll(resultSet);
		}
		// return set of sets
		return this.getSetofEquivalentSets(types);
	}

	public Set disjointClassesOf(OWLClass c) throws OWLException {
		if (disjoints.containsKey(c)) 
			return this.getSetofEquivalentSets((HashSet) disjoints.get(c));
		else return new HashSet();
	}

	public Set complementClassesOf(OWLClass c) throws OWLException {
		if (complements.containsKey(c)) 
			return this.getSetofEquivalentSets((HashSet) complements.get(c));
		else return new HashSet();
	}

	public Set getOntologies() {
		return this.ontologies;
	}

	public Set getClasses() {
		return new HashSet(classes);
	}

	public Set getProperties() {
		return new HashSet(properties);
	}

	public Set getObjectProperties() {
		Set set = new HashSet();
		for(int i = 0; i < properties.size(); i++) 
			if(properties.get(i) instanceof OWLObjectProperty)
				set.add(properties.get(i));
		return set;
	}

	public Set getDataProperties() {
		Set set = new HashSet();
		for(int i = 0; i < properties.size(); i++) 
			if(properties.get(i) instanceof OWLDataProperty)
				set.add(properties.get(i));
		return set;
	}

	public Set getAnnotationProperties() {
		Set set = new HashSet();
		for(int i = 0; i < properties.size(); i++) 
			if(properties.get(i) instanceof OWLAnnotationProperty)
				set.add(properties.get(i));
		return set;
	}

	public Set getIndividuals() {
		return new HashSet(individuals);
	}

	public boolean supportsExplanation() {
		return false;
	}

	public void setDoExplanation(boolean explain) {
		
	}

	public boolean getDoExplanation() {
		return false;
	}

	public String getExplanation(ShortFormProvider shortForms) {
		return null;
	}

	public Set getExplanationSet() {
		return null;
	}

	public Set instancesOf(OWLClass c) throws OWLException {
		return this.instancesOf((OWLDescription) c);
	}

	public Set allInstancesOf(OWLClass c) throws OWLException {
		// get all descendents of class c
		List classTracker = new ArrayList();
		Set descendants = this.getClassHierarchy(c, classTracker, "SUB");
		descendants.add(c);
		Set resultSet = new HashSet();
		for (Iterator iter = descendants.iterator(); iter.hasNext();) {
			OWLClass cla = (OWLClass) iter.next();
			resultSet.addAll(this.instancesOf(cla));
		}
		return resultSet;
	}

	public OWLOntology getOntology() throws OWLException {
		return ontology;
	}

	public boolean isSubClassOf(OWLDescription d1, OWLDescription d2) throws OWLException {
		if (d1 instanceof OWLClass && d2 instanceof OWLClass) {
			OWLClass cl = (OWLClass) d1;
			List classTracker = new ArrayList();
			Set ancestors = this.getClassHierarchy(cl, classTracker, "SUPER");	
			return (ancestors.contains(d2));
		}
		else return false;
	}

	public boolean isEquivalentClass(OWLDescription d1, OWLDescription d2) throws OWLException {
		if (this.equivalents.containsKey(d1)) {
			return ((HashSet) equivalents.get(d1)).contains(d2);
		}
		else return false;
	}

	public boolean isConsistent(OWLDescription d1) throws OWLException {
		return true;
	}

	public Set superClassesOf(OWLDescription d) throws OWLException {
		Set resultSet = new HashSet();
		
		Set claSet = new HashSet();
		claSet.add(d);
		// also find equivalents
		if (equivalents.containsKey(d)) {			
			claSet.addAll((HashSet) equivalents.get(d));
		}
		
		// get direct superclasses of all classes in claSet 
		for (Iterator iter = claSet.iterator(); iter.hasNext();) {
			OWLDescription desc = (OWLDescription) iter.next();
			if (desc instanceof OWLClass && outLinks.containsKey(desc)) {
				Set superClaSet = (HashSet) this.outLinks.get(desc);
				resultSet.addAll(this.getSetofEquivalentSets(superClaSet));
			}
		}
		return resultSet;
	}

	public Set ancestorClassesOf(OWLDescription d) throws OWLException {
		Set resultSet = new HashSet();
		if (d instanceof OWLClass) {
			OWLClass cl = (OWLClass) d;
			List classTracker = new ArrayList();
			resultSet = this.getClassHierarchy(cl, classTracker, "SUPER");			
		}
		return this.getSetofEquivalentSets(resultSet);
	}

	public Set getClassHierarchy(OWLClass cl, List tracker, String dirn) throws OWLException {
		
		Set setOfSets = new HashSet();
		if (dirn.equals("SUPER")) setOfSets = this.superClassesOf(cl); // return superclass sets
		else setOfSets = this.subClassesOf(cl); // return subclass sets
		
		for (Iterator iter = setOfSets.iterator(); iter.hasNext();) {
			Set set = (HashSet) iter.next(); // get each equivalent superClass set
			for (Iterator iter2=set.iterator(); iter2.hasNext();) {
				OWLClass cla = (OWLClass) iter2.next();
				if (!tracker.contains(cla)) {
					tracker.add(cla);
					// recurse
					getClassHierarchy(cla, tracker, dirn); 					
				}
				else {
					// cycle found!
					// verify cycle: check for a subclass from supCla to cl
					// i.e. check if owl:Thing is one of the links inbetween
					int pos1 = tracker.indexOf(cla);
					int pos2 = tracker.indexOf(cl);
					boolean cycle = true;
					OWLClass thing = ontology.getOWLDataFactory().getOWLThing();
					OWLClass nothing = ontology.getOWLDataFactory().getOWLNothing();
					for (int i=pos1; i<pos2; i++) {
						if (dirn.equals("SUPER")) {
							if (tracker.get(i).equals(thing)) cycle = false;
						}
						else {
							if (tracker.get(i).equals(nothing)) cycle = false;
						}
					}
					if (cycle) 
					{						
						this.addEquivalentClass( cl, cla );
					}
				}
			}
		}
		return new HashSet(tracker);
	}
	
	public Set subClassesOf(OWLDescription d) throws OWLException {
		Set resultSet = new HashSet();
		
		Set claSet = new HashSet();
		claSet.add(d);
		// also find equivalents
		if (equivalents.containsKey(d)) {			
			claSet.addAll((HashSet) equivalents.get(d));
		}
		
		// get direct subclasses of all classes in claSet 
		for (Iterator iter = claSet.iterator(); iter.hasNext();) {
			OWLDescription desc = (OWLDescription) iter.next();
			if (desc instanceof OWLClass && incLinks.containsKey(desc)) 
			{
				Set subClaSet = (HashSet) this.incLinks.get(desc);
				resultSet.addAll(this.getSetofEquivalentSets(subClaSet));
			}
		}
		return resultSet;		
	}

	public Set descendantClassesOf(OWLDescription d) throws OWLException {
		Set resultSet = new HashSet();
		if (d instanceof OWLClass) {
			OWLClass cl = (OWLClass) d;
			List classTracker = new ArrayList();
			resultSet = this.getClassHierarchy(cl, classTracker, "SUB");			
		}
		return this.getSetofEquivalentSets(resultSet);
	}

	public Set equivalentClassesOf(OWLDescription d) throws OWLException {
		if (d instanceof OWLClass && equivalents.containsKey(d)) {
			Set equSet = new HashSet((HashSet) equivalents.get(d));
			equSet.remove(d);
			return equSet;
		}
		return new HashSet();
	}

	public boolean isInstanceOf(OWLIndividual i, OWLDescription d) throws OWLException {
		return false;
	}

	public Set instancesOf(OWLDescription d) throws OWLException {
		
		Set resultSet = new HashSet();
		
		Set claSet = new HashSet();
		claSet.add(d);
		// also find equivalents
		if (equivalents.containsKey(d)) {			
			claSet.addAll((HashSet) equivalents.get(d));
		}
		
		// get individuals of all classes in claSet 
		for (Iterator iter = claSet.iterator(); iter.hasNext();) {
			OWLClass cla = (OWLClass) iter.next();
			if (classInds.containsKey(cla)) {
				Set inds = (HashSet) classInds.get(cla);
				resultSet.addAll(inds);
			}
		}
		return resultSet;		
	}

	public boolean isSubPropertyOf(OWLProperty p1, OWLProperty p2) throws OWLException {
		Set ancestors = this.getPropertyHierarchy(p1, new ArrayList(), "SUPER");
		return ancestors.contains(p2); 
	}
	
	public Set superPropertiesOf(OWLProperty prop) throws OWLException {
		Set resultSet = new HashSet();
		
		Set propSet = new HashSet();
		propSet.add(prop);
		// also find equivalents
		if (equivalents.containsKey(prop)) {			
			propSet.addAll((HashSet) equivalents.get(prop));
		}
		
		// get direct superproperties of all properties in propSet 
		for (Iterator iter = propSet.iterator(); iter.hasNext();) {
			OWLProperty p = (OWLProperty) iter.next();
			if (outLinks.containsKey(p)) {
				Set subPropSet = (HashSet) this.outLinks.get(p);
				resultSet.addAll(this.getSetofEquivalentSets(subPropSet));
			}
		}
		return resultSet;
	}

	public Set ancestorPropertiesOf(OWLProperty prop) throws OWLException {
		List propTracker = new ArrayList();
		Set ancestors = this.getPropertyHierarchy(prop, propTracker, "SUPER");
		return this.getSetofEquivalentSets(ancestors);
	}
	
	public Set getPropertyHierarchy(OWLProperty p, List tracker, String dirn) throws OWLException {
		
		Set setOfSets = new HashSet();
		if (dirn.equals("SUPER")) setOfSets = this.superPropertiesOf(p); // return superclass sets
		else setOfSets = this.subPropertiesOf(p); // return subclass sets
		
		for (Iterator iter = setOfSets.iterator(); iter.hasNext();) {
			Set set = (HashSet) iter.next(); // get each equivalent superClass set
			for (Iterator iter2=set.iterator(); iter2.hasNext();) {
				OWLProperty prop = (OWLProperty) iter2.next();
				if (!tracker.contains(prop)) {
					tracker.add(prop);
					// recurse
					getPropertyHierarchy(prop, tracker, dirn); 					
				}
				else {
					// cycle found!					
				}
			}
		}
		return new HashSet(tracker);
	}

	/**
	 * Return a 'set of equivalent sets' given a set of objects i.e. for each
	 * object 'obj' in the input set, get its equivalent set (using the hashmap
	 * 'equivalents') and add it to result set
	 * @param set - set of objects whose equivalents have to be determined
	 * @return
	 */
	public Set getSetofEquivalentSets(Set set) {
		Set resultSet = new HashSet();
		for (Iterator iter = set.iterator(); iter.hasNext(); ) {
			Object obj = iter.next();
			Set equObjs = new HashSet();
			equObjs.add(obj);
			if (equivalents.containsKey(obj)) equObjs = (HashSet) equivalents.get(obj);			
			resultSet.add(equObjs);
		}
		return resultSet;
	}
	
	public Set subPropertiesOf(OWLProperty prop) throws OWLException {
		Set resultSet = new HashSet();
		
		Set propSet = new HashSet();
		propSet.add(prop);
		// also find equivalents
		if (equivalents.containsKey(prop)) {			
			propSet.addAll((HashSet) equivalents.get(prop));
		}
		
		// get direct subproperties of all properties in propSet 
		for (Iterator iter = propSet.iterator(); iter.hasNext();) {
			OWLProperty p = (OWLProperty) iter.next();
			if (incLinks.containsKey(p)) {
				Set subPropSet = (HashSet) this.incLinks.get(p);
				resultSet.addAll(this.getSetofEquivalentSets(subPropSet));
			}
		}
		return resultSet;
	}

	public Set descendantPropertiesOf(OWLProperty prop) throws OWLException {
		List propTracker = new ArrayList();
		Set descendents = this.getPropertyHierarchy(prop, propTracker, "SUB");
		return this.getSetofEquivalentSets(descendents);
	}

	public Set equivalentPropertiesOf(OWLProperty prop) throws OWLException {
		if (equivalents.containsKey(prop)) {
			Set equSet = new HashSet((HashSet) equivalents.get(prop));
			equSet.remove(prop);
			return equSet;
		}
		return new HashSet();
	}

	public Set inversePropertiesOf(OWLObjectProperty prop) throws OWLException {
		if (this.inverses.containsKey(prop)) {
			Set inverses = (Set) this.inverses.get(prop);
			return this.getSetofEquivalentSets(inverses);
		}
		else return this.getSetofEquivalentSets(new HashSet());
	}

	public Set rangesOf(OWLProperty prop) throws OWLException {
		Set ranges = prop.getRanges(ontologies);
//		return ranges;
		return SetUtils.union(this.getSetofEquivalentSets(ranges));
	}

	public Set domainsOf(OWLProperty prop) throws OWLException {
		Set domains = prop.getDomains(ontologies);
//		return domains;
		return SetUtils.union(this.getSetofEquivalentSets(domains));		
	}

	public Set superClassesOf(OWLClass cl) throws OWLException {
		return this.superClassesOf((OWLDescription) cl);
	}

	public Set ancestorClassesOf(OWLClass cl) throws OWLException {
		return this.ancestorClassesOf((OWLDescription) cl);
	}

	public Set subClassesOf(OWLClass cl) throws OWLException {		
		return this.subClassesOf((OWLDescription) cl);
	}

	public Set descendantClassesOf(OWLClass cl) throws OWLException {
		return this.descendantClassesOf((OWLDescription) cl);
	}

	public Set equivalentClassesOf(OWLClass cl) throws OWLException {		
		return this.equivalentClassesOf((OWLDescription) cl);
	}
	
	public void buildIndices() {
		indices = new OntologyIndices(this);
		indices.buildIndex(ontology, loadImports, true);
//		System.out.println("Expressivity:" +indices.getExpressivity(ontology));
	}


	public Map getDataPropertyValues(OWLIndividual ind) throws OWLException {
	    return ind.getDataPropertyValues( getOntologies() );
	}
	
	public Map getObjectPropertyValues(OWLIndividual ind) throws OWLException{
	    return ind.getObjectPropertyValues( getOntologies() );
	}
}
