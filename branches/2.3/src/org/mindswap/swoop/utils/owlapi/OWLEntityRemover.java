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

/*
 * Author: Aditya Kalyanpur
 */

package org.mindswap.swoop.utils.owlapi;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owl.model.OWLAnd;
import org.semanticweb.owl.model.OWLAnnotationInstance;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLClassAxiom;
import org.semanticweb.owl.model.OWLDataAllRestriction;
import org.semanticweb.owl.model.OWLDataCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataSomeRestriction;
import org.semanticweb.owl.model.OWLDataType;
import org.semanticweb.owl.model.OWLDataValue;
import org.semanticweb.owl.model.OWLDataValueRestriction;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owl.model.OWLDisjointClassesAxiom;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLEnumeration;
import org.semanticweb.owl.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owl.model.OWLEquivalentPropertiesAxiom;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLIndividualAxiom;
import org.semanticweb.owl.model.OWLNot;
import org.semanticweb.owl.model.OWLObjectAllRestriction;
import org.semanticweb.owl.model.OWLObjectCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectQuantifiedRestriction;
import org.semanticweb.owl.model.OWLObjectSomeRestriction;
import org.semanticweb.owl.model.OWLObjectValueRestriction;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOr;
import org.semanticweb.owl.model.OWLProperty;
import org.semanticweb.owl.model.OWLPropertyAxiom;
import org.semanticweb.owl.model.OWLRestriction;
import org.semanticweb.owl.model.OWLSameIndividualsAxiom;
import org.semanticweb.owl.model.OWLSubClassAxiom;
import org.semanticweb.owl.model.OWLSubPropertyAxiom;
import org.semanticweb.owl.model.change.AddAnnotationInstance;
import org.semanticweb.owl.model.change.AddClassAxiom;
import org.semanticweb.owl.model.change.AddDataPropertyInstance;
import org.semanticweb.owl.model.change.AddDataPropertyRange;
import org.semanticweb.owl.model.change.AddDomain;
import org.semanticweb.owl.model.change.AddEnumeration;
import org.semanticweb.owl.model.change.AddEquivalentClass;
import org.semanticweb.owl.model.change.AddIndividualAxiom;
import org.semanticweb.owl.model.change.AddIndividualClass;
import org.semanticweb.owl.model.change.AddInverse;
import org.semanticweb.owl.model.change.AddObjectPropertyInstance;
import org.semanticweb.owl.model.change.AddObjectPropertyRange;
import org.semanticweb.owl.model.change.AddPropertyAxiom;
import org.semanticweb.owl.model.change.AddSuperClass;
import org.semanticweb.owl.model.change.AddSuperProperty;
import org.semanticweb.owl.model.change.ChangeVisitor;
import org.semanticweb.owl.model.change.OntologyChange;
import org.semanticweb.owl.model.change.RemoveClassAxiom;
import org.semanticweb.owl.model.change.RemoveDataPropertyInstance;
import org.semanticweb.owl.model.change.RemoveDomain;
import org.semanticweb.owl.model.change.RemoveEntity;
import org.semanticweb.owl.model.change.RemoveEnumeration;
import org.semanticweb.owl.model.change.RemoveEquivalentClass;
import org.semanticweb.owl.model.change.RemoveIndividualAxiom;
import org.semanticweb.owl.model.change.RemoveIndividualClass;
import org.semanticweb.owl.model.change.RemoveInverse;
import org.semanticweb.owl.model.change.RemoveObjectPropertyInstance;
import org.semanticweb.owl.model.change.RemoveObjectPropertyRange;
import org.semanticweb.owl.model.change.RemovePropertyAxiom;
import org.semanticweb.owl.model.change.RemoveSuperClass;
import org.semanticweb.owl.model.change.RemoveSuperProperty;
import org.semanticweb.owl.model.change.SetFunctional;
import org.semanticweb.owl.model.change.SetInverseFunctional;
import org.semanticweb.owl.model.change.SetSymmetric;
import org.semanticweb.owl.model.change.SetTransitive;

/**
 * @author Aditya Kalyanpur
 *
 */
public class OWLEntityRemover {

	private OWLOntology ontology;
	private OWLEntity remEntity;
	private OWLEntity replEntity;
	
	final static int ANNOTATION = -1;
	final static int SUPERCLASS = 0;
	final static int EQUIVALENTCLASS = 1;
	final static int ENUMERATION = 2;
	final static int SUBCLASSAXIOM = 3;
	final static int EQUCLASSAXIOM = 4;
	final static int DISJOINTAXIOM = 5;
	final static int SUPERPROPERTY = 6;
	final static int EQUIVALENTPROPERTY = 7;
	final static int SUBPROPAXIOM = 8;
	final static int EQUPROPAXIOM = 9;
	final static int DOMAIN = 10;
	final static int RANGE = 11; // object property range
	final static int INVERSE = 12;
	final static int INDIVIDUALCLASS = 13;
	final static int INDDATAPROP = 14;
	final static int INDOBJPROP = 15;
	final static int SAMEAS = 16;
	final static int DIFFFROM = 17;
	final static String DISCARD = "DISCARD";
	
	private List changes;
	
	public OWLEntityRemover(OWLOntology ontology) {
		this.ontology = ontology;
		this.changes = new ArrayList();
	}
	
	/**
	 * Replace an OWL Entity with another OWL Entity of the same type
	 * @param removeEntity
	 * @param replaceEntity
	 */
	public void replaceEntity(OWLEntity removeEntity, OWLEntity replaceEntity) {
		this.replEntity = replaceEntity; // set this field
		this.removeEntity(removeEntity); // now call this method which will remove and replace when necessary
	}
	
	/**
	 * Removes all references of an entity from an ontology
	 * @param removeEntity
	 */
	public void removeEntity(OWLEntity removeEntity) {
		
		this.remEntity = removeEntity;
		try {
			// check all classes
			for (Iterator iter = ontology.getClasses().iterator(); iter.hasNext();) {
				
				OWLClass cla = (OWLClass) iter.next();
				
				// check all basic stuff that would get removed automatically
				// by using RemoveEntity on class
				if (cla.equals(remEntity) && replEntity!=null) {
					// move superclasses, equivalentclasses, enumerations, annotations
					for (Iterator iter2 = cla.getSuperClasses(ontology).iterator(); iter2.hasNext();) {
						OWLDescription sup = (OWLDescription) iter2.next();
						this.addChange(replEntity, sup, this.SUPERCLASS);
					}
					for (Iterator iter2 = cla.getEquivalentClasses(ontology).iterator(); iter2.hasNext();) {
						OWLDescription equ = (OWLDescription) iter2.next();
						this.addChange(replEntity, equ, this.EQUIVALENTCLASS);
					}
					for (Iterator iter2 = cla.getEnumerations(ontology).iterator(); iter2.hasNext();) {
						OWLEnumeration enu = (OWLEnumeration) iter2.next();
						this.addChange(replEntity, enu, this.ENUMERATION);
					}
					for (Iterator iter2 = cla.getAnnotations(ontology).iterator(); iter2.hasNext();) {
						OWLAnnotationInstance oai = (OWLAnnotationInstance) iter2.next();
						this.addChange(replEntity, oai, this.ANNOTATION);
					}
				}
				
				// check super classes
				Set sup = cla.getSuperClasses(ontology);
				for (Iterator iter2= sup.iterator(); iter2.hasNext();) {
					OWLDescription desc = (OWLDescription) iter2.next();
					this.checkClassRelation(cla, desc, SUPERCLASS);
				}
				
				// check equivalent classes
				Set equ = cla.getEquivalentClasses(ontology);
				for (Iterator iter2= equ.iterator(); iter2.hasNext();) {
					OWLDescription desc = (OWLDescription) iter2.next();
					this.checkClassRelation(cla, desc, EQUIVALENTCLASS);
				}
				
				// check enumerations
				Set enuSet = cla.getEnumerations(ontology);
				for (Iterator iter2= enuSet.iterator(); iter2.hasNext();) {
					OWLEnumeration enu = (OWLEnumeration) iter2.next();
					Set ops = enu.getIndividuals();
					boolean changed = false;
					for (Iterator iter3=new HashSet(ops).iterator(); iter3.hasNext();) {
						OWLIndividual ind = (OWLIndividual) iter3.next();
						if (ind.equals(remEntity)) {
							changed = true;
							ops.remove(ind);
							if (replEntity!=null) ops.add(replEntity);
						}
					}
					if (changed) {
						// remove current enumeration and add new enum
						this.removeChange(cla, enu, ENUMERATION);
						if (ops.size()>0) {
							OWLEnumeration newEnu = ontology.getOWLDataFactory().getOWLEnumeration(ops);
							this.addChange(cla, newEnu, ENUMERATION);
						}
					}
				}				
			}
			
			// check class axioms
			for (Iterator iter = ontology.getClassAxioms().iterator(); iter.hasNext();) {
				OWLClassAxiom axiom = (OWLClassAxiom) iter.next();
				
				// check sub class axioms
				if (axiom instanceof OWLSubClassAxiom) {
					OWLSubClassAxiom subAxiom = (OWLSubClassAxiom) axiom;
					this.checkClassRelation(subAxiom.getSubClass(), subAxiom.getSuperClass(), SUBCLASSAXIOM);
				}
				
				// check equivalent class axioms
				else if (axiom instanceof OWLEquivalentClassesAxiom) {
					OWLEquivalentClassesAxiom equAxiom = (OWLEquivalentClassesAxiom) axiom;
					Set equOps = equAxiom.getEquivalentClasses();
					boolean changed = false;
					for (Iterator iter2 = new HashSet(equOps).iterator(); iter2.hasNext();) {
						OWLDescription desc = (OWLDescription) iter2.next();
						Object check = this.traceDescription(desc);
						if (check!=null) {
							changed = true;
							equOps.remove(desc);
							if (check instanceof OWLDescription) {
								equOps.add((OWLDescription) check);
							}
						}						
					}
					if (changed) {
						this.removeChange(equAxiom.getEquivalentClasses(), null, EQUCLASSAXIOM);
						this.addChange(equOps, null, EQUCLASSAXIOM);
					}
				}
				
				// check disjoint class axioms
				else if (axiom instanceof OWLDisjointClassesAxiom) {
					OWLDisjointClassesAxiom disAxiom = (OWLDisjointClassesAxiom) axiom;
					Set disOps = disAxiom.getDisjointClasses();
					boolean changed = false;
					for (Iterator iter2 = new HashSet(disOps).iterator(); iter2.hasNext();) {
						OWLDescription desc = (OWLDescription) iter2.next();
						Object check = this.traceDescription(desc);
						if (check!=null) {
							changed = true;
							disOps.remove(desc);
							if (check instanceof OWLDescription) {
								disOps.add((OWLDescription) check);
							}
						}						
					}
					if (changed) {
						this.removeChange(disAxiom.getDisjointClasses(), null, DISJOINTAXIOM);
						this.addChange(disOps, null, DISJOINTAXIOM);
					}
				}				
			}
			
			// check all data/object properties (annotation?)
			Set properties = ontology.getDataProperties();
			properties.addAll(ontology.getObjectProperties());
			for (Iterator iter = properties.iterator(); iter.hasNext();) {
				
				OWLProperty prop = (OWLProperty) iter.next();
				
				// check all basic stuff that would get removed automatically
				// by using RemoveEntity on property
				if (prop.equals(remEntity) && replEntity!=null) {
					// add domain of remEntity to replEntity
					for (Iterator iter2 = prop.getDomains(ontology).iterator(); iter2.hasNext();) {
						OWLDescription dom = (OWLDescription) iter2.next();
						this.addChange(replEntity, dom, this.DOMAIN);
					}					
					// add super-props, functional and annotations
					for (Iterator iter2 = prop.getSuperProperties(ontology).iterator(); iter2.hasNext();) {
						OWLProperty sup = (OWLProperty) iter2.next();
						this.addChange(replEntity, sup, this.SUPERPROPERTY);
					}
					if (prop.isFunctional(ontology)) {
						SetFunctional sf = new SetFunctional(ontology, (OWLProperty) replEntity, true, null);
						changes.add(sf);							
					}
					for (Iterator iter2 = prop.getAnnotations(ontology).iterator(); iter2.hasNext();) {
						OWLAnnotationInstance oai = (OWLAnnotationInstance) iter2.next();
						this.addChange(replEntity, oai, this.ANNOTATION);
					}
					
					if (prop instanceof OWLDataProperty) {
						OWLDataProperty dp = (OWLDataProperty) prop;
						// add data property range
						for (Iterator iter2 = dp.getRanges(ontology).iterator(); iter2.hasNext();) {
							OWLDataType dt = (OWLDataType) iter2.next();
							AddDataPropertyRange adpr = new AddDataPropertyRange(ontology, (OWLDataProperty) replEntity, dt, null);
							changes.add(adpr);
						}
					}
					else if (prop instanceof OWLObjectProperty) {
						// check range, inverse-props, and prop attributes
						for (Iterator iter2 = prop.getRanges(ontology).iterator(); iter2.hasNext();) {
							OWLDescription ran = (OWLDescription) iter2.next();
							this.addChange(replEntity, ran, this.RANGE);
						}
						for (Iterator iter2 = ((OWLObjectProperty) prop).getInverses(ontology).iterator(); iter2.hasNext();) {
							OWLObjectProperty inv = (OWLObjectProperty) iter2.next();
							this.addChange(replEntity, inv, this.INVERSE);
						}
						if (((OWLObjectProperty) prop).isInverseFunctional(ontology)) {
							SetInverseFunctional sif = new SetInverseFunctional(ontology, (OWLObjectProperty) replEntity, true, null);
							changes.add(sif);							
						}
						if (((OWLObjectProperty) prop).isTransitive(ontology)) {
							SetTransitive st = new SetTransitive(ontology, (OWLObjectProperty) replEntity, true, null);
							changes.add(st);							
						}
						if (((OWLObjectProperty) prop).isSymmetric(ontology)) {
							SetSymmetric ss = new SetSymmetric(ontology, (OWLObjectProperty) replEntity, true, null);
							changes.add(ss);							
						}
					}
				}
				
				// check all super properties
				// *** different from adding superprops *of* remProp as done above, 
				// *** below checks if superProp = remProp
				for (Iterator iter2=prop.getSuperProperties(ontology).iterator(); iter2.hasNext();) {
					OWLProperty sup = (OWLProperty) iter2.next();
					if (sup.equals(remEntity)) {
						this.removeChange(prop, sup, SUPERPROPERTY);
						if (replEntity!=null) this.addChange(prop, replEntity, SUPERPROPERTY);
					}
				}
				
				// check all property domains
				for (Iterator iter2=prop.getDomains(ontology).iterator(); iter2.hasNext();) {
					OWLDescription desc = (OWLDescription) iter2.next();
					Object check = this.traceDescription(desc);
					if (check!=null) {
						this.removeChange(prop, desc, DOMAIN);
						if (check instanceof OWLDescription) {
							this.addChange(prop, check, DOMAIN);
						}
					}
				}
				
				// check for all object properties
				if (prop instanceof OWLObjectProperty && !((OWLObjectProperty) prop).isLink()) {
					// check property ranges
					for (Iterator iter2=prop.getRanges(ontology).iterator(); iter2.hasNext();) {
						OWLDescription desc = (OWLDescription) iter2.next();
						Object check = this.traceDescription(desc);
						if (check!=null) {
							this.removeChange(prop, desc, RANGE);
							if (check instanceof OWLDescription) {
								this.addChange(prop, check, RANGE);
							}
						}
					}
					// check inverses
					for (Iterator iter2=((OWLObjectProperty) prop).getInverses(ontology).iterator(); iter2.hasNext();) {
						OWLObjectProperty invProp = (OWLObjectProperty) iter2.next();
						if (invProp.equals(remEntity)) { 
							this.removeChange(prop, invProp, INVERSE);
							if (replEntity!=null) this.addChange(prop, replEntity, INVERSE);
						}
					}
				}				
			}
			
			// check property axioms
			for (Iterator iter = ontology.getPropertyAxioms().iterator(); iter.hasNext();) {
				OWLPropertyAxiom axiom = (OWLPropertyAxiom) iter.next();
				
				// check sub prop axioms
				if (axiom instanceof OWLSubPropertyAxiom) {
					OWLSubPropertyAxiom subAxiom = (OWLSubPropertyAxiom) axiom;
					OWLProperty sub = subAxiom.getSubProperty();
					OWLProperty sup = subAxiom.getSuperProperty();
					if (sub.equals(remEntity) || sup.equals(remEntity)) {
						this.removeChange(sub, sup, SUBPROPAXIOM);
						if (replEntity!=null && !sub.equals(remEntity)) this.addChange(sub, replEntity, SUBPROPAXIOM);
						if (replEntity!=null && !sup.equals(remEntity)) this.addChange(replEntity, sup, SUBPROPAXIOM);
					}
				}
				
				// check equivalent property axioms
				else if (axiom instanceof OWLEquivalentPropertiesAxiom) {
					OWLEquivalentPropertiesAxiom equAxiom = (OWLEquivalentPropertiesAxiom) axiom;
					Set equOps = equAxiom.getProperties();
					boolean changed = false;
					for (Iterator iter2=new HashSet(equOps).iterator(); iter2.hasNext();) {
						OWLProperty prop = (OWLProperty) iter2.next();
						if (prop.equals(remEntity)) {
							changed = true;
							equOps.remove(prop);
							if (replEntity!=null) equOps.add(replEntity); 
						}
					}
					if (changed) {
						this.removeChange(equAxiom.getProperties(), null, EQUPROPAXIOM);
						this.addChange(equOps, null, EQUPROPAXIOM);
					}
				}
			}
			
			// check all individuals
			for (Iterator iter = ontology.getIndividuals().iterator(); iter.hasNext();) {
				
				OWLIndividual ind = (OWLIndividual) iter.next();
				
				// check all basic stuff that would get removed automatically
				// by using RemoveEntity on individual
				if (ind.equals(remEntity) && replEntity!=null) {
					// move types, obj/data prop-values, annotations
					for (Iterator iter2=ind.getTypes(ontology).iterator(); iter2.hasNext();) {
						OWLDescription desc = (OWLDescription) iter2.next();
						this.addChange(replEntity, desc, this.INDIVIDUALCLASS);
					}
					Map dValues = ind.getDataPropertyValues(ontology);
					for (Iterator iter2 = dValues.keySet().iterator(); iter2.hasNext();) {
						OWLDataProperty prop = (OWLDataProperty) iter2.next();
						Set vals = (HashSet) dValues.get(prop);
						for (Iterator valIter = vals.iterator(); valIter.hasNext();) {
							OWLDataValue value = (OWLDataValue) valIter.next();
							OntologyChange change = new AddDataPropertyInstance(ontology, (OWLIndividual) replEntity, prop, value, null);
							changes.add(change);
						}
					}
					Map oValues = ind.getObjectPropertyValues(ontology);
					for (Iterator iter2 = oValues.keySet().iterator(); iter2.hasNext();) {
						OWLObjectProperty prop = (OWLObjectProperty) iter2.next();
						Set vals = (HashSet) oValues.get(prop);
						for (Iterator valIter = vals.iterator(); valIter.hasNext();) {
							OWLIndividual value = (OWLIndividual) valIter.next();
							OntologyChange change = new AddObjectPropertyInstance(ontology, (OWLIndividual) replEntity, prop, value, null);
							changes.add(change);
						}
					}
					for (Iterator iter2=ind.getAnnotations(ontology).iterator(); iter2.hasNext();) {
						OWLAnnotationInstance oai = (OWLAnnotationInstance) iter2.next();
						this.addChange(replEntity, oai, this.ANNOTATION);
					}
				}
				
				// check all individual types
				for (Iterator iter2=ind.getTypes(ontology).iterator(); iter2.hasNext();) {
					OWLDescription desc = (OWLDescription) iter2.next();
					Object check = this.traceDescription(desc);
					if (check!=null) {
						this.removeChange(ind, desc, INDIVIDUALCLASS);
						if (check instanceof OWLDescription) {
							this.addChange(ind, check, INDIVIDUALCLASS);
						}
					}
				}
				
				// check data property value instances
				Map dValues = ind.getDataPropertyValues(ontology);
				for (Iterator iter2 = dValues.keySet().iterator(); iter2.hasNext();) {
					OWLDataProperty prop = (OWLDataProperty) iter2.next();
					Set vals = (HashSet) dValues.get(prop);
					for (Iterator valIter = vals.iterator(); valIter.hasNext();) {
						OWLDataValue value = (OWLDataValue) valIter.next();
						if (prop.equals(remEntity)) {
							OntologyChange change = new RemoveDataPropertyInstance(ontology, ind, prop, value, null);
							changes.add(change);
							if (replEntity!=null) {
								OntologyChange change2 = new AddDataPropertyInstance(ontology, ind, (OWLDataProperty) replEntity, value, null);
								changes.add(change2);
							}							
						}
					}
				}
				
				// check object property value instances
				Map oValues = ind.getObjectPropertyValues(ontology);
				for (Iterator iter2 = oValues.keySet().iterator(); iter2.hasNext();) {
					OWLObjectProperty prop = (OWLObjectProperty) iter2.next();
					Set vals = (HashSet) oValues.get(prop);
					for (Iterator valIter = vals.iterator(); valIter.hasNext();) {
						OWLIndividual value = (OWLIndividual) valIter.next();
						if (prop.equals(remEntity) || value.equals(remEntity)) {
							OntologyChange change = new RemoveObjectPropertyInstance(ontology, ind, prop, value, null);
							changes.add(change);
							if (prop.equals(remEntity) && replEntity!=null) {
								OntologyChange change2 = new AddObjectPropertyInstance(ontology, ind, (OWLObjectProperty) replEntity, value, null);
								changes.add(change2);
							}
							if (value.equals(remEntity) && replEntity!=null) {
								OntologyChange change2 = new AddObjectPropertyInstance(ontology, ind, prop, (OWLIndividual) replEntity, null);
								changes.add(change2);
							}
						}
					}
				}
			}
			
			// check individual axioms
			for (Iterator iter = ontology.getIndividualAxioms().iterator(); iter.hasNext();) {
				OWLIndividualAxiom axiom = (OWLIndividualAxiom) iter.next();
				
				// check sameAs b/w individuals
				if (axiom instanceof OWLSameIndividualsAxiom) {
					OWLSameIndividualsAxiom sameAxiom = (OWLSameIndividualsAxiom) axiom;
					Set sameOps = sameAxiom.getIndividuals();
					boolean changed = false;
					for (Iterator iter2 = new HashSet(sameOps).iterator(); iter2.hasNext();) {
						OWLIndividual ind = (OWLIndividual) iter2.next();
						if (ind.equals(remEntity)) {
							changed = true;
							sameOps.remove(ind);
							if (replEntity!=null) sameOps.add(replEntity);
						}						
					}
					if (changed) {
						this.removeChange(sameAxiom.getIndividuals(), null, SAMEAS);
						this.addChange(sameOps, null, SAMEAS);
					}
				}
				
				// check differentFrom b/w individuals
				if (axiom instanceof OWLDifferentIndividualsAxiom) {
					OWLDifferentIndividualsAxiom diffAxiom = (OWLDifferentIndividualsAxiom) axiom;
					Set diffOps = diffAxiom.getIndividuals();
					boolean changed = false;
					for (Iterator iter2 = new HashSet(diffOps).iterator(); iter2.hasNext();) {
						OWLIndividual ind = (OWLIndividual) iter2.next();
						if (ind.equals(remEntity)) {
							changed = true;
							diffOps.remove(ind);
							if (replEntity!=null) diffOps.add(replEntity);
						}						
					}
					if (changed) {
						this.removeChange(diffAxiom.getIndividuals(), null, DIFFFROM);
						this.addChange(diffOps, null, DIFFFROM);
					}
				}
				
			}
			
			// add removeEntity change right at the end
			OntologyChange change = new RemoveEntity(ontology, remEntity, null);
			changes.add(change);
			
			// finally apply changes
			for (Iterator iter = changes.iterator(); iter.hasNext();) {
				change = (OntologyChange) iter.next();
				change.accept((ChangeVisitor) ontology);
			}
		}
		catch (OWLException ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Updates class relation (type: subclass/equivalent..) as follows:
	 * Traces contents of desc1 to see if entity found inside
	 * and contents of desc2 to see if entity found inside
	 * Based on checks made, adds/removes changes based on relation type specified
	 * @param desc1
	 * @param desc2
	 * @param relType
	 * @throws OWLException
	 */
	public void checkClassRelation(OWLDescription desc1, OWLDescription desc2, int relType) throws OWLException {
		
		boolean changed = false;
		OWLDescription addDesc1 = null;
		OWLDescription addDesc2 = null;
		
		// verify contents of description 1
		Object check = this.traceDescription(desc1);
		if (check!=null) {
			// match found for entity
			changed = true;
			// get updated desc if any
			if (check instanceof OWLDescription) addDesc1 = (OWLDescription) check;
		}
		
		// verify contents of description 2
		check = this.traceDescription(desc2);
		if (check!=null) {
			// match found for entity
			changed = true;
			// get updated desc if any
			if (check instanceof OWLDescription) addDesc2 = (OWLDescription) check;
		}
		
		// make changes based on verification check above
		if (changed || addDesc1!=null || addDesc2!=null) this.removeChange(desc1, desc2, relType);
		if (addDesc1!=null && addDesc2!=null) this.addChange(addDesc1, addDesc2, relType);
		else if (addDesc1!=null) this.addChange(addDesc1, desc2, relType);
		else if (addDesc2!=null) this.addChange(desc1, addDesc2, relType);
	}
	
	/**
	 * Traces the contents of an OWLDescription (desc) as follows:
	 * - if desc matches entity [A, ~A, restr(P,*) or hasValue(*,I)], returns DISCARD
	 * - else if desc contains entity, returns newDesc (minus entity) 
	 * In all other cases (no match for entity at all), returns Null
	 * 
	 * @param desc
	 * @return
	 */
	public Object traceDescription(OWLDescription desc) {
		
		try {
			 
//			// testing
//			if (desc instanceof OWLClass) System.out.println("tracing: "+((OWLClass) desc).getURI());
			
			if (desc instanceof OWLClass && ((OWLClass) desc).equals(remEntity)) { 
				if (replEntity!=null) return replEntity;
				else return this.DISCARD;			
			}
			else
			if (desc instanceof OWLAnd) {
				// if description is intersection, get operands
				OWLAnd and = (OWLAnd) desc;
				Set operands = and.getOperands();
				boolean changed = false;
				for (Iterator iter = new HashSet(operands).iterator(); iter.hasNext();) {
					OWLDescription andOp = (OWLDescription) iter.next();
					Object check = this.traceDescription(andOp);
					if (check!=null) {
						// match found for entity
						changed = true;
						operands.remove(andOp);
						// add new (updated) description if any
						if (check instanceof OWLDescription) operands.add((OWLDescription) check);
					}				
				}
				if (changed) {
					return ontology.getOWLDataFactory().getOWLAnd(operands);
				}			
			}
			else
			if (desc instanceof OWLOr) {
				// if description is union, get operands
				OWLOr or = (OWLOr) desc;
				Set operands = or.getOperands();
				boolean changed = false;
				for (Iterator iter = new HashSet(operands).iterator(); iter.hasNext();) {
					OWLDescription orOp = (OWLDescription) iter.next();
					Object check = this.traceDescription(orOp);
					if (check!=null) {
						// match found for entity
						changed = true;
						operands.remove(orOp);
						// add new (updated) description if any
						if (check instanceof OWLDescription) operands.add((OWLDescription) check);
					}				
				}
				if (changed) {
					return ontology.getOWLDataFactory().getOWLOr(operands);
				}			
			}
			else
			if (desc instanceof OWLNot) {
				OWLNot not = (OWLNot) desc;
				Object check = traceDescription(not.getOperand());
				if (check!=null) {
					// match found for entity
					// either return updated ~Desc or DISCARD
					if (check instanceof OWLDescription) return ontology.getOWLDataFactory().getOWLNot((OWLDescription) check);
					else {
						if (replEntity!=null) return replEntity;
						else return this.DISCARD; // e.g. ~A. where entity = A
					}
				}
				
			}
			else
			if (desc instanceof OWLRestriction) {
				// check property of restriction first to discard entire restriction
				if (((OWLRestriction) desc).getProperty().equals(remEntity)) {
					if (replEntity!=null) {
						// create OWLRestriction replacing prop w/ replEntity
						return replaceRestriction((OWLRestriction) desc, (OWLProperty) replEntity);
					}
					else return this.DISCARD;
				}
				
				// check some/all value restriction
				if (desc instanceof OWLObjectQuantifiedRestriction) {
					OWLObjectQuantifiedRestriction objRes = (OWLObjectQuantifiedRestriction) desc;
					OWLObjectProperty oprop = objRes.getObjectProperty();
					Object check = traceDescription(objRes.getDescription());
					if (check!=null) {
						if (check instanceof OWLDescription) {
							// return new restriction
							OWLObjectQuantifiedRestriction rest = null;
							if (objRes instanceof OWLObjectAllRestriction) rest = ontology.getOWLDataFactory().getOWLObjectAllRestriction(oprop, (OWLDescription) check);
							else rest = ontology.getOWLDataFactory().getOWLObjectSomeRestriction(oprop, (OWLDescription) check);
							return rest;
						}
						else {
							if (replEntity!=null) return replEntity;
							else return this.DISCARD; // e.g exists(P,C), where entity = P;
						}
					}
				}
				
				// finally check hasValue restriction
				if (desc instanceof OWLObjectValueRestriction) {
					OWLObjectValueRestriction objRes = (OWLObjectValueRestriction) desc;
					// discard if match found for individual
					if (objRes.getIndividual().equals(remEntity)) {
						if (replEntity!=null) return replEntity;
						else return this.DISCARD;					
					}
				}
			}
			else if (desc instanceof OWLEnumeration) {
				// check contents of enum to see if entity found
				boolean changed = false;
				Set indOps = ((OWLEnumeration) desc).getIndividuals();
				for (Iterator iter = new HashSet(indOps).iterator(); iter.hasNext();) {
					OWLIndividual ind = (OWLIndividual) iter.next();
					if (ind.equals(remEntity)) {
						// match found
						changed = true;
						indOps.remove(ind);
						if (replEntity!=null) indOps.add(replEntity);
					}
				}
				if (changed) {
					if (indOps.size()==0) {
						if (replEntity!=null) return replEntity;
						else return this.DISCARD;
					}
					else {
						return ontology.getOWLDataFactory().getOWLEnumeration(indOps);						
					}
				}
			}
		}
		catch (OWLException ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	public void removeChange(Object subj, Object obj, int relType) throws OWLException {
		OntologyChange changed = null;
		switch (relType) {
			case SUPERCLASS: 
				changed = new RemoveSuperClass(ontology, (OWLClass) subj, (OWLDescription) obj, null);
				changes.add(changed);
				break;
			case EQUIVALENTCLASS: 
				changed = new RemoveEquivalentClass(ontology, (OWLClass) subj, (OWLDescription) obj, null);
				changes.add(changed);
				break;
			case ENUMERATION: 
				changed = new RemoveEnumeration(ontology, (OWLClass) subj, (OWLEnumeration) obj, null);
				changes.add(changed);
				break;
			case SUBCLASSAXIOM:
				OWLSubClassAxiom axiom = ontology.getOWLDataFactory().getOWLSubClassAxiom((OWLDescription) subj, (OWLDescription) obj);
				changed = new RemoveClassAxiom(ontology, axiom, null);
				changes.add(changed);
				break;
			case EQUCLASSAXIOM:
				OWLEquivalentClassesAxiom equAxiom = ontology.getOWLDataFactory().getOWLEquivalentClassesAxiom((Set) subj);
				changed = new RemoveClassAxiom(ontology, equAxiom, null);
				changes.add(changed);
				break;
			case DISJOINTAXIOM:
				OWLDisjointClassesAxiom disAxiom = ontology.getOWLDataFactory().getOWLDisjointClassesAxiom((Set) subj);
				changed = new RemoveClassAxiom(ontology, disAxiom, null);
				changes.add(changed);
				break;
			
			case SUPERPROPERTY: 
				changed = new RemoveSuperProperty(ontology, (OWLProperty) subj, (OWLProperty) obj, null);
				changes.add(changed);
				break;
			case SUBPROPAXIOM:
				OWLSubPropertyAxiom subPAxiom = ontology.getOWLDataFactory().getOWLSubPropertyAxiom((OWLProperty) subj, (OWLProperty) obj);
				changed = new RemovePropertyAxiom(ontology, subPAxiom, null);
				changes.add(changed);
				break;
			case EQUPROPAXIOM:
				OWLEquivalentPropertiesAxiom equPAxiom = ontology.getOWLDataFactory().getOWLEquivalentPropertiesAxiom((Set) subj);
				changed = new RemovePropertyAxiom(ontology, equPAxiom, null);
				changes.add(changed);
				break;
			case DOMAIN:
				changed = new RemoveDomain(ontology, (OWLProperty) subj, (OWLDescription) obj, null);
				changes.add(changed);
				break;
			case RANGE:
				changed = new RemoveObjectPropertyRange(ontology, (OWLObjectProperty) subj, (OWLDescription) obj, null);
				changes.add(changed);
				break;
			case INVERSE: 
				changed = new RemoveInverse(ontology, (OWLObjectProperty) subj, (OWLObjectProperty) obj, null);
				changes.add(changed);
				break;
				
			case INDIVIDUALCLASS: 
				changed = new RemoveIndividualClass(ontology, (OWLIndividual) subj, (OWLDescription) obj, null);
				changes.add(changed);
				break;
			case SAMEAS:
				OWLSameIndividualsAxiom same = ontology.getOWLDataFactory().getOWLSameIndividualsAxiom((Set) subj);
				changed = new RemoveIndividualAxiom(ontology, same, null);
				changes.add(changed);
				break;
			case DIFFFROM:
				OWLDifferentIndividualsAxiom diff = ontology.getOWLDataFactory().getOWLDifferentIndividualsAxiom((Set) subj);
				changed = new RemoveIndividualAxiom(ontology, diff, null);
				changes.add(changed);
				break;
		}
	}
	
	public void addChange(Object subj, Object obj, int relType) throws OWLException {
		OntologyChange changed = null;
		switch (relType) {
			case ANNOTATION:
				changed = new AddAnnotationInstance(ontology, (OWLEntity) subj, ((OWLAnnotationInstance) obj).getProperty(), ((OWLAnnotationInstance) obj).getContent(), null);
				changes.add(changed);
				break;
			case SUPERCLASS: 
				changed = new AddSuperClass(ontology, (OWLClass) subj, (OWLDescription) obj, null);
				changes.add(changed);
				break;
			case EQUIVALENTCLASS: 
				changed = new AddEquivalentClass(ontology, (OWLClass) subj, (OWLDescription) obj, null);
				changes.add(changed);
				break;
			case ENUMERATION: 
				changed = new AddEnumeration(ontology, (OWLClass) subj, (OWLEnumeration) obj, null);
				changes.add(changed);
				break;
			case SUBCLASSAXIOM:
				OWLSubClassAxiom axiom = ontology.getOWLDataFactory().getOWLSubClassAxiom((OWLDescription) subj, (OWLDescription) obj);
				changed = new AddClassAxiom(ontology, axiom, null);
				changes.add(changed);
				break;
			case EQUCLASSAXIOM:
				OWLEquivalentClassesAxiom equAxiom = ontology.getOWLDataFactory().getOWLEquivalentClassesAxiom((Set) subj);
				changed = new AddClassAxiom(ontology, equAxiom, null);
				changes.add(changed);
				break;
			case DISJOINTAXIOM:
				OWLDisjointClassesAxiom disAxiom = ontology.getOWLDataFactory().getOWLDisjointClassesAxiom((Set) subj);
				changed = new AddClassAxiom(ontology, disAxiom, null);
				changes.add(changed);
				break;
			
			case SUPERPROPERTY: 
				changed = new AddSuperProperty(ontology, (OWLProperty) subj, (OWLProperty) obj, null);
				changes.add(changed);
				break;
			case EQUPROPAXIOM:
				OWLEquivalentPropertiesAxiom equPAxiom = ontology.getOWLDataFactory().getOWLEquivalentPropertiesAxiom((Set) subj);
				changed = new AddPropertyAxiom(ontology, equPAxiom, null);
				changes.add(changed);
				break;
			case DOMAIN:
				changed = new AddDomain(ontology, (OWLProperty) subj, (OWLDescription) obj, null);
				changes.add(changed);
				break;
			case RANGE:
				changed = new AddObjectPropertyRange(ontology, (OWLObjectProperty) subj, (OWLDescription) obj, null);
				changes.add(changed);
				break;
			case INVERSE: 
				changed = new AddInverse(ontology, (OWLObjectProperty) subj, (OWLObjectProperty) obj, null);
				changes.add(changed);
				break;
					
			case INDIVIDUALCLASS: 
				changed = new AddIndividualClass(ontology, (OWLIndividual) subj, (OWLDescription) obj, null);
				changes.add(changed);
				break;
			case SAMEAS:
				OWLSameIndividualsAxiom same = ontology.getOWLDataFactory().getOWLSameIndividualsAxiom((Set) subj);
				changed = new AddIndividualAxiom(ontology, same, null);
				changes.add(changed);
				break;
			case DIFFFROM:
				OWLDifferentIndividualsAxiom diff = ontology.getOWLDataFactory().getOWLDifferentIndividualsAxiom((Set) subj);
				changed = new AddIndividualAxiom(ontology, diff, null);
				changes.add(changed);
				break;
		}
	}

	private OWLRestriction replaceRestriction(OWLRestriction res, OWLProperty prop) {
		try {
			OWLDataFactory df = res.getOWLDataFactory();
			// object prop restrictions
			if (res instanceof OWLObjectAllRestriction) {
				return df.getOWLObjectAllRestriction((OWLObjectProperty) prop, ((OWLObjectAllRestriction) res).getDescription());
			}
			else if (res instanceof OWLObjectSomeRestriction) {
				return df.getOWLObjectSomeRestriction((OWLObjectProperty) prop, ((OWLObjectSomeRestriction) res).getDescription());
			}
			else if (res instanceof OWLObjectValueRestriction) {
				return df.getOWLObjectValueRestriction((OWLObjectProperty) prop, ((OWLObjectValueRestriction) res).getIndividual());
			}
			else if (res instanceof OWLObjectCardinalityRestriction) {
				return df.getOWLObjectCardinalityRestriction((OWLObjectProperty) prop, ((OWLObjectCardinalityRestriction) res).getAtLeast(), ((OWLObjectCardinalityRestriction) res).getAtMost()); 
			}
			// dataprop restrictions
			else if (res instanceof OWLDataAllRestriction) {
				return df.getOWLDataAllRestriction((OWLDataProperty) prop, ((OWLDataAllRestriction) res).getDataType());
			}
			else if (res instanceof OWLDataSomeRestriction) {
				return df.getOWLDataSomeRestriction((OWLDataProperty) prop, ((OWLDataSomeRestriction) res).getDataType());
			}
			else if (res instanceof OWLDataValueRestriction) {
				return df.getOWLDataValueRestriction((OWLDataProperty) prop, ((OWLDataValueRestriction) res).getValue());
			}
			else if (res instanceof OWLDataCardinalityRestriction) {
				return df.getOWLDataCardinalityRestriction((OWLDataProperty) prop, ((OWLDataCardinalityRestriction) res).getAtLeast(), ((OWLDataCardinalityRestriction) res).getAtMost()); 
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
}
