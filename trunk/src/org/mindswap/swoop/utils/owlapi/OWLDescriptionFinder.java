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

package org.mindswap.swoop.utils.owlapi;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.semanticweb.owl.model.OWLAnd;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLClassAxiom;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDisjointClassesAxiom;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLEnumeration;
import org.semanticweb.owl.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLNaryBooleanDescription;
import org.semanticweb.owl.model.OWLNot;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectQuantifiedRestriction;
import org.semanticweb.owl.model.OWLObjectRestriction;
import org.semanticweb.owl.model.OWLObjectValueRestriction;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOr;
import org.semanticweb.owl.model.OWLProperty;
import org.semanticweb.owl.model.OWLRestriction;
import org.semanticweb.owl.model.OWLSubClassAxiom;
import org.semanticweb.owl.model.helper.OntologyHelper;

/**
 * Find certain kind of class descriptions in ontologies without doing any kind of reasoning.
 * Reasoners return information about named classes whereas OWLDescriptionFinder returns
 * information as found in the ontology which may contain unnamed class descriptions.
 * 
 * @author Evren Sirin
 */
public class OWLDescriptionFinder {
	public static Set getEnumerations(OWLClass c, Set ontologies) throws OWLException{
		return c.getEnumerations(ontologies);
	}
	
	public static Set getIntersections(OWLClass c, Set ontologies) throws OWLException {
		Set set = new HashSet();
		Iterator i = c.getEquivalentClasses(ontologies).iterator();
		while(i.hasNext()) {
			OWLDescription desc = (OWLDescription) i.next();
			if(desc instanceof OWLAnd)
				set.add(desc);
		}
		return set;
	}

	public static Set getUnions(OWLClass c, Set ontologies) throws OWLException {
		Set set = new HashSet();
		Iterator i = c.getEquivalentClasses(ontologies).iterator();
		while(i.hasNext()) {
			OWLDescription desc = (OWLDescription) i.next();
			if(desc instanceof OWLOr)
				set.add(desc);
		}
		return set;
	}
	
	//Find all the enumerations a certain individual is participating on
	
    public static Set getEnumerations(OWLIndividual ind, OWLOntology ont)
    {
    	Set result = new HashSet();
    	Set aux = new HashSet();
    	aux.add(ont);
    	
    	try {
			Set references = OntologyHelper.entityUsage(ont, ind);
			Set claSet = new HashSet();
			for (Iterator iter = references.iterator(); iter.hasNext(); ) {
				Object obj = iter.next();
				if (obj instanceof OWLClass) claSet.add(obj);
			}
			 for(Iterator it = claSet.iterator(); it.hasNext(); ){
		    	OWLClass clazz = (OWLClass)it.next();
		    	result.addAll(getEnumerations(clazz,aux));
		    }
		} catch (OWLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
    	return result;
    	
    	
    }
    
    //Retrieves the classes that are defined as oneOF the given enumeration
    
    public static Set getClassWithEnumeration(OWLEnumeration enum_, OWLOntology onto){
    	Set result = new HashSet();
    	Set aux = new HashSet();
    	aux.add(onto);
    	try {
			Object[] v = enum_.getIndividuals().toArray();
			OWLIndividual ind = (OWLIndividual)v[0];
			Set references = OntologyHelper.entityUsage(onto, ind);
			Set claSet = new HashSet();
			for (Iterator iter = references.iterator(); iter.hasNext(); ) {
				Object obj = iter.next();
				if (obj instanceof OWLClass) claSet.add(obj);
			}
			 for(Iterator it = claSet.iterator(); it.hasNext(); ){
		    	OWLClass clazz = (OWLClass)it.next();
		     	if(getEnumerations(clazz,aux).contains(enum_))
		     		result.add(clazz);
			 }
		} catch (OWLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return result;
    }
		
	public static Set getComplements(OWLClass c, Set ontologies) throws OWLException {
		Set set = new HashSet();
		// if Class(c complete complementOf(d)) 
		// then c is complement of d
		Iterator i = c.getEquivalentClasses(ontologies).iterator();
		while(i.hasNext()) {
			OWLDescription desc = (OWLDescription) i.next();
			if(desc instanceof OWLNot) {
				OWLDescription negation = ((OWLNot)desc).getOperand();	
//				if(negation instanceof OWLClass) 
					set.add(negation);
			}
		}	
		return set;
	}

	public static Set getDisjoints(OWLClass c, Set ontologies) throws OWLException {
		Set set = new HashSet();
		Iterator i = c.getSuperClasses(ontologies).iterator();
		while(i.hasNext()) {
			OWLDescription desc = (OWLDescription) i.next();
			if(desc instanceof OWLNot) {
				OWLDescription negation = ((OWLNot)desc).getOperand();	
				if(negation instanceof OWLClass) 
					set.add(negation);
			}
		}
		Iterator ont = ontologies.iterator();
		while(ont.hasNext()) {
			OWLOntology o = (OWLOntology) ont.next();
			Iterator a = o.getClassAxioms().iterator();
			while(a.hasNext()) {
				OWLClassAxiom axiom = (OWLClassAxiom) a.next();
				if(axiom instanceof OWLDisjointClassesAxiom) {
					Set disjoints = ((OWLDisjointClassesAxiom) axiom).getDisjointClasses();
					if(disjoints.contains(c)) {
						set.addAll(disjoints);
						set.remove(c);
					}
				}
			}
		}		
		return set;
	}

	public static Set getEquivalentClasses(OWLClass c, Set ontologies) throws OWLException {
		Set set = c.getEquivalentClasses(ontologies);
		set.addAll(c.getEnumerations(ontologies)); //?????
		Iterator ont = ontologies.iterator();
		while(ont.hasNext()) {
			OWLOntology o = (OWLOntology) ont.next();
			Iterator a = o.getClassAxioms().iterator();
			while(a.hasNext()) {
				OWLClassAxiom axiom = (OWLClassAxiom) a.next();
				if(axiom instanceof OWLEquivalentClassesAxiom) {
					Set eqs = ((OWLEquivalentClassesAxiom) axiom).getEquivalentClasses();
					if(eqs.contains(c)) {
						set.addAll(eqs);
						set.remove(c);
					}
				}
			}
			for (Iterator iter = o.getClasses().iterator(); iter.hasNext();) {
				OWLClass cla = (OWLClass) iter.next();
				Set equ = cla.getEquivalentClasses(ontologies);
				if (equ.contains(c)) set.add(cla);				
			}
		}		
		return set;
	}	
	
	public static Set getSuperClasses(OWLClass c, Set ontologies) throws OWLException {
		Set set = c.getSuperClasses(ontologies);
		Iterator ont = ontologies.iterator();
		while(ont.hasNext()) {
			OWLOntology o = (OWLOntology) ont.next();
			Iterator a = o.getClassAxioms().iterator();
			while(a.hasNext()) {
				OWLClassAxiom axiom = (OWLClassAxiom) a.next();
				if(axiom instanceof OWLSubClassAxiom) {
					OWLSubClassAxiom subAxiom = (OWLSubClassAxiom) axiom;
					if(subAxiom.getSubClass().equals(c)) {
						set.add(subAxiom.getSuperClass());
					}
				}
			}
		}		
		return set;
	}	
	
	public static Set getSubClasses(OWLClass c, Set ontologies) throws OWLException {
		Set set = new HashSet();
		Iterator ont = ontologies.iterator();
		while(ont.hasNext()) {
			OWLOntology o = (OWLOntology) ont.next();
			Iterator i = o.getClasses().iterator();
			while(i.hasNext()) {
				OWLClass clazz = (OWLClass) i.next();
				if(clazz.getSuperClasses(ontologies).contains(c))
					set.add(clazz);				
			}
			Iterator a = o.getClassAxioms().iterator();
			while(a.hasNext()) {
				OWLClassAxiom axiom = (OWLClassAxiom) a.next();
				if(axiom instanceof OWLSubClassAxiom) {
					OWLSubClassAxiom subAxiom = (OWLSubClassAxiom) axiom;
					if(subAxiom.getSuperClass().equals(c)) {
						set.add(subAxiom.getSubClass());
					}
				}
			}
		}		
		return set;
	}
	
	
	public static Set getSubClasses(OWLDescription c, Set ontologies) throws OWLException {
		Set set = new HashSet();
		Iterator ont = ontologies.iterator();
		while(ont.hasNext()) {
			OWLOntology o = (OWLOntology) ont.next();
			Iterator i = o.getClasses().iterator();
			while(i.hasNext()) {
				OWLClass clazz = (OWLClass) i.next();
				if(clazz.getSuperClasses(ontologies).contains(c))
					set.add(clazz);				
			}
			Iterator a = o.getClassAxioms().iterator();
			while(a.hasNext()) {
				OWLClassAxiom axiom = (OWLClassAxiom) a.next();
				if(axiom instanceof OWLSubClassAxiom) {
					OWLSubClassAxiom subAxiom = (OWLSubClassAxiom) axiom;
					if(subAxiom.getSuperClass().equals(c)) {
						set.add(subAxiom.getSubClass());
					}
				}
			}
		}		
		return set;
	}
	
	
	public static Set getEquivalentClasses(OWLDescription c, Set ontologies) throws OWLException {
		Set set = new HashSet();
		Iterator ont = ontologies.iterator();
		while(ont.hasNext()) {
			OWLOntology o = (OWLOntology) ont.next();
			Iterator i = o.getClasses().iterator();
			while(i.hasNext()) {
				OWLClass clazz = (OWLClass) i.next();
				if(clazz.getEquivalentClasses(ontologies).contains(c))
					set.add(clazz);				
			}
			Iterator a = o.getClassAxioms().iterator();
			while(a.hasNext()) {
				OWLClassAxiom axiom = (OWLClassAxiom) a.next();
				//if(axiom instanceof OWLSubClassAxiom) {
				   if(axiom instanceof OWLEquivalentClassesAxiom) {
					OWLEquivalentClassesAxiom equivAxiom = (OWLEquivalentClassesAxiom) axiom;
					if(equivAxiom.getEquivalentClasses().contains(c)) {
						set.addAll(equivAxiom.getEquivalentClasses());
					}
				}
			}
		}		
		set.remove(c);
		return set;
	}
	
	public static Set getTypes(OWLIndividual ind, Set ontologies) throws OWLException {
	    return ind.getTypes(ontologies);
	}	
}
