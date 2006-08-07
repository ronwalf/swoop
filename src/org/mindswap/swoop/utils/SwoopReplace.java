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

package org.mindswap.swoop.utils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.utils.owlapi.OWLDescriptionFinder;
import org.semanticweb.owl.model.OWLAnd;
import org.semanticweb.owl.model.OWLAnnotationInstance;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDisjointClassesAxiom;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLEnumeration;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLNot;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOr;
import org.semanticweb.owl.model.OWLProperty;
import org.semanticweb.owl.model.OWLSubClassAxiom;
import org.semanticweb.owl.model.change.AddAnnotationInstance;
import org.semanticweb.owl.model.change.AddClassAxiom;
import org.semanticweb.owl.model.change.AddEquivalentClass;
import org.semanticweb.owl.model.change.AddSuperClass;
import org.semanticweb.owl.model.change.ChangeVisitor;
import org.semanticweb.owl.model.change.OntologyChange;
import org.semanticweb.owl.model.change.RemoveAnnotationInstance;
import org.semanticweb.owl.model.change.RemoveClassAxiom;
import org.semanticweb.owl.model.change.RemoveEquivalentClass;
import org.semanticweb.owl.model.change.RemoveSuperClass;

/**
 * @author Aditya
 *
 */
public class SwoopReplace {	
	public static boolean replaceAnnotations(OWLOntology ontology, OWLEntity entity,OWLOntology newOntology, OWLEntity newEntity) throws OWLException {
		for (Iterator it = entity.getAnnotations(ontology).iterator(); it.hasNext();) {
			OWLAnnotationInstance oai = (OWLAnnotationInstance) it.next();
			RemoveAnnotationInstance change = new RemoveAnnotationInstance(ontology, entity, oai.getProperty(), oai.getContent(), null);
			change.accept((ChangeVisitor) ontology);				
		}
		for (Iterator it = newEntity.getAnnotations(newOntology).iterator(); it.hasNext();) {
			OWLAnnotationInstance oai = (OWLAnnotationInstance) it.next();
			AddAnnotationInstance change = new AddAnnotationInstance(ontology, entity, oai.getProperty(), oai.getContent(), null);
			change.accept((ChangeVisitor) ontology);				
		}
		return true;
	}
	
	public static boolean replaceIntersections(OWLOntology ontology, OWLClass clazz, OWLOntology newOntology,  OWLClass newClass) throws OWLException {
		Set ontologies = new HashSet();
		ontologies.add(ontology);
		for (Iterator it = OWLDescriptionFinder.getIntersections(clazz, ontologies).iterator(); it.hasNext();) {
			OWLAnd intersection = (OWLAnd) it.next();
			RemoveEquivalentClass change = new RemoveEquivalentClass(ontology, clazz, intersection, null);
			change.accept((ChangeVisitor) ontology);						
		}	
		Set newOntologies = new HashSet();
		newOntologies.add(newOntology);		
		for (Iterator it = OWLDescriptionFinder.getIntersections(newClass, newOntologies).iterator(); it.hasNext();) {
			OWLAnd intersection = (OWLAnd) it.next();
			AddEquivalentClass change = new AddEquivalentClass(ontology, clazz, intersection, null);
			change.accept((ChangeVisitor) ontology);						
		}		
		return true;
	}
	
	public static boolean replaceUnions(OWLOntology ontology, OWLClass clazz, OWLOntology newOntology,  OWLClass newClass) throws OWLException {
		Set ontologies = new HashSet();
		ontologies.add(ontology);
		for (Iterator it = OWLDescriptionFinder.getUnions(clazz, ontologies).iterator(); it.hasNext();) {
			OWLOr union = (OWLOr) it.next();
			RemoveEquivalentClass change = new RemoveEquivalentClass(ontology, clazz, union, null);
			change.accept((ChangeVisitor) ontology);						
		}
		Set newOntologies = new HashSet();
		newOntologies.add(newOntology);
		for (Iterator it = OWLDescriptionFinder.getUnions(newClass, newOntologies).iterator(); it.hasNext();) {
			OWLOr union = (OWLOr) it.next();
			AddEquivalentClass change = new AddEquivalentClass(ontology, clazz, union, null);
			change.accept((ChangeVisitor) ontology);						
		}		
		return true;
	}
	
	
	//contingent upon fixing the enum inference problem
	public static boolean replaceEnums(OWLOntology ontology, OWLClass clazz, OWLOntology newOntology,  OWLClass newClass) throws OWLException {
		Set ontologies = new HashSet();
		ontologies.add(ontology);
		for (Iterator it = OWLDescriptionFinder.getEnumerations(clazz, ontologies).iterator(); it.hasNext();) {
			OWLEnumeration enums = (OWLEnumeration) it.next();
			RemoveEquivalentClass change = new RemoveEquivalentClass(ontology, clazz, enums, null);
			change.accept((ChangeVisitor) ontology);						
		}
		Set newOntologies = new HashSet();
		newOntologies.add(newOntology);
		for (Iterator it = OWLDescriptionFinder.getEnumerations(newClass, newOntologies).iterator(); it.hasNext();) {
			OWLEnumeration enums = (OWLEnumeration) it.next();
			AddEquivalentClass change = new AddEquivalentClass(ontology, clazz, enums, null);
			change.accept((ChangeVisitor) ontology);						
		}		
		return true;
	}
	
	public static boolean replaceEquivalents(OWLOntology ontology, OWLClass clazz, OWLOntology newOntology,  OWLClass newClass) throws OWLException {
		Set ontologies = new HashSet();
		ontologies.add(ontology);
		for (Iterator it = OWLDescriptionFinder.getEquivalentClasses(clazz, ontologies).iterator(); it.hasNext();) {
			RemoveEquivalentClass change = new RemoveEquivalentClass(ontology, clazz, (OWLDescription) it.next(), null);
			change.accept((ChangeVisitor) ontology);						
		}
		Set newOntologies = new HashSet();
		newOntologies.add(newOntology);
		for (Iterator it = OWLDescriptionFinder.getEquivalentClasses(newClass, newOntologies).iterator(); it.hasNext();) {
			AddEquivalentClass change = new AddEquivalentClass(ontology, clazz, (OWLDescription) it.next(), null);
			change.accept((ChangeVisitor) ontology);						
		}	
		return true;
	}
	
	// disjoint axioms not visible in rdf/xml and AS view
	public static boolean replaceDisjoints(OWLOntology ontology, OWLClass clazz, OWLOntology newOntology,  OWLClass newClass) throws OWLException {
		Set ontologies = new HashSet();
		ontologies.add(ontology);
		Set disjSet = OWLDescriptionFinder.getDisjoints(clazz, ontologies);
		disjSet.add(clazz);
		OWLDisjointClassesAxiom disAxiom = ontology.getOWLDataFactory().getOWLDisjointClassesAxiom(disjSet);
		RemoveClassAxiom change = new RemoveClassAxiom(ontology, disAxiom, null);				
		change.accept((ChangeVisitor) ontology);
		
		Set newOntologies = new HashSet();
		newOntologies.add(ontology);
		Set newDisjSet = OWLDescriptionFinder.getDisjoints(newClass, newOntologies);
		disjSet.add(newClass);
		OWLDisjointClassesAxiom newDisAxiom = newOntology.getOWLDataFactory().getOWLDisjointClassesAxiom(newDisjSet);
		AddClassAxiom newChange = new AddClassAxiom(ontology, newDisAxiom, null);
		newChange.accept((ChangeVisitor) ontology);		
		return true;
	}
	
	
	public static boolean replaceSuperClasses(OWLOntology ontology, OWLClass clazz, OWLOntology newOntology,  OWLClass newClass) throws OWLException {
		Set ontologies = new HashSet();
		ontologies.add(ontology);
		for (Iterator it = OWLDescriptionFinder.getSuperClasses(clazz, ontologies).iterator(); it.hasNext();) {
		    OWLDescription sup = (OWLDescription) it.next();
			RemoveSuperClass change = new RemoveSuperClass(ontology, clazz, sup, null);
			change.accept((ChangeVisitor) ontology);						
		}
		Set newOntologies = new HashSet();
		newOntologies.add(newOntology);
		for (Iterator it = OWLDescriptionFinder.getSuperClasses(newClass, newOntologies).iterator(); it.hasNext();) {
		    OWLDescription sup = (OWLDescription) it.next();
			AddSuperClass change = new AddSuperClass(ontology, clazz, sup, null);
			change.accept((ChangeVisitor) ontology);						
		}		
		return true;
	}
	
	public static boolean replaceSubClasses(OWLOntology ontology, OWLClass clazz, OWLOntology newOntology,  OWLClass newClass) throws OWLException {
		Set ontologies = new HashSet();
		ontologies.add(ontology);
		for (Iterator it = OWLDescriptionFinder.getSubClasses(clazz, ontologies).iterator(); it.hasNext();) {
		    OWLDescription sub = (OWLDescription) it.next();
		    OntologyChange change = null;
		    if(sub instanceof OWLClass)
		        change = new RemoveSuperClass(ontology, (OWLClass) sub, clazz, null);
		    else {
		        OWLSubClassAxiom axiom = ontology.getOWLDataFactory().getOWLSubClassAxiom(sub, clazz);
		        change = new RemoveClassAxiom(ontology, axiom, null);
		    }
		    
			change.accept((ChangeVisitor) ontology);						
		}


		
		Set newOntologies = new HashSet();
		newOntologies.add(newOntology);
		for (Iterator it = OWLDescriptionFinder.getSubClasses(newClass, newOntologies).iterator(); it.hasNext();) {
		    OWLDescription sub = (OWLDescription) it.next();
		    OntologyChange change = null;
		    if(sub instanceof OWLClass)
		        change = new AddSuperClass(ontology, (OWLClass) sub, clazz, null);
		    else {
		        OWLSubClassAxiom axiom = ontology.getOWLDataFactory().getOWLSubClassAxiom(sub, clazz);
		        change = new AddClassAxiom(ontology, axiom, null);
		    }
		    
			change.accept((ChangeVisitor) ontology);						
		}		
		return true;
	}
	
	public static boolean replaceEquivalentProperties(OWLOntology ontology, OWLProperty property, OWLOntology newOntology,  OWLProperty newProperty) throws OWLException {
		/*
		Set ontologies = new HashSet();
		ontologies.add(ontology);
		for (Iterator it = OWLDescriptionFinder.getEquivalentClasses(clazz, ontologies).iterator(); it.hasNext();) {
			RemoveEquivalentClass change = new RemoveEquivalentClass(ontology, clazz, (OWLDescription) it.next(), null);
			change.accept((ChangeVisitor) ontology);						
		}
		Set newOntologies = new HashSet();
		newOntologies.add(newOntology);
		for (Iterator it = OWLDescriptionFinder.getEquivalentClasses(newClass, newOntologies).iterator(); it.hasNext();) {
			AddEquivalentClass change = new AddEquivalentClass(ontology, clazz, (OWLDescription) it.next(), null);
			change.accept((ChangeVisitor) ontology);						
		}
		*/	
		return true;
	}
	
	
	
	public static boolean replaceClass(SwoopModel swoopModel, OWLClass clazz, OWLOntology newOntology,  OWLClass newClass) {
		
		try {
			OWLOntology ontology = swoopModel.getSelectedOntology();
			
			replaceAnnotations(ontology, clazz, newOntology, newClass);
			
			replaceIntersections(ontology, clazz,  newOntology, newClass);
			
			replaceUnions(ontology, clazz,  newOntology, newClass);
			
			replaceEnums(ontology, clazz,  newOntology, newClass);
			
			replaceDisjoints(ontology, clazz,  newOntology, newClass);
			
			replaceSuperClasses(ontology, clazz, newOntology,  newClass);
			
			// We should replace subclass axioms because renderers do not 
			// actually render these axioms so we will lose the axioms 
			// even though user did not delete them in the text editor
			//replaceSubClasses(ontology, clazz, newOntology,  newClass);
		}
		catch (OWLException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	

	/**
	 * @param model
	 * @param property
	 * @param ontology
	 * @param class1
	 */
	public static boolean replaceDataProperty(SwoopModel swoopModel, OWLDataProperty property, OWLOntology newOntology, OWLDataProperty newProperty) {
		
	
	try {
		OWLOntology ontology = swoopModel.getSelectedOntology();
		
		replaceAnnotations(ontology, property, newOntology, newProperty);
		
		replaceEquivalentProperties(ontology, property, newOntology, newProperty);
/*
		
		String title = "<b>Equivalent to:</b>";
		if (reasoner.equivalentPropertiesOf(prop).size()==0) {
			boolean printed = insertAddedChanges(new HashSet(), prop, "P-EQU", title);
			if (!printed) notPrinted.add("P-EQU");
		}
		else {
			if (editorEnabled) title += addTitle("P-EQU");
			printCollection(reasoner.equivalentPropertiesOf(prop), title);			
		}
		
		title = "<b>Subproperty of:</b>";
		if (reasoner.superPropertiesOf(prop).size()==0) {
			boolean printed = insertAddedChanges(new HashSet(), prop, "P-SUB", title);
			if (!printed) notPrinted.add("P-SUB");
		}
		else {
			if (editorEnabled) title += addTitle("P-SUB");
			printCollection(reasoner.superPropertiesOf(prop), title);
		}
		
		title = "<b>Superproperty of:</b>";
		if (reasoner.subPropertiesOf(prop).size()==0) {
			boolean printed = insertAddedChanges(new HashSet(), prop, "P-SUP", title);
			if (!printed) notPrinted.add("P-SUP");
		}
		else {
			if (editorEnabled) title += addTitle("P-SUP");
			printCollection(reasoner.subPropertiesOf(prop), title);
		}
		
		if ((notPrinted.size()-notPrintedSize<3) && showDivisions) print(HR+"<FONT FACE=\"Verdana\" SIZE=2>");
		notPrintedSize = notPrinted.size();
		
		title = "<b>Has domain:</b>";
		if (reasoner.domainsOf(prop).size()==0) {
			boolean printed = insertAddedChanges(new HashSet(), prop, "C-HASDOM", title);
			if (!printed) notPrinted.add("C-HASDOM");
		}
		else {
			if (editorEnabled) title += addTitle("C-HASDOM");
			printCollection(reasoner.domainsOf(prop), title);
		}
		
		title = "<b>Has range:</b>";
		if (reasoner.rangesOf(prop).size()==0) {
			boolean printed = insertAddedChanges(new HashSet(), prop, "C-HASRAN", title);
			if (!printed) notPrinted.add("C-HASRAN");
		}
		else {
			if (editorEnabled) title += addTitle("C-HASRAN");
			printCollection(reasoner.rangesOf(prop), title);
		}
		
		if ((notPrinted.size()-notPrintedSize<2) && showDivisions) print(HR+"<FONT FACE=\"Verdana\" SIZE=2>");
		notPrintedSize = notPrinted.size();
		
		if (prop.isFunctional(reasoner.getOntologies())) {
			println();
			print("<b>Attribute</b>: Functional");
			if (editorEnabled) addDelete(prop, "P-FUN");
			println();
		}
		else if (editorEnabled) notPrinted.add("P-FUN");
	}
		
		
*/
	}
	catch (Exception e) {
	}
	return true;
}
}
