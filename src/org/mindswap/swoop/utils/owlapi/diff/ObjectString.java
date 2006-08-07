package org.mindswap.swoop.utils.owlapi.diff;

import java.io.PrintStream;
import java.util.Iterator;

import org.semanticweb.owl.model.OWLAnd;
import org.semanticweb.owl.model.OWLAnnotationInstance;
import org.semanticweb.owl.model.OWLAnnotationProperty;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataAllRestriction;
import org.semanticweb.owl.model.OWLDataCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataEnumeration;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataPropertyInstance;
import org.semanticweb.owl.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owl.model.OWLDataSomeRestriction;
import org.semanticweb.owl.model.OWLDataType;
import org.semanticweb.owl.model.OWLDataValue;
import org.semanticweb.owl.model.OWLDataValueRestriction;
import org.semanticweb.owl.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owl.model.OWLDisjointClassesAxiom;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLEnumeration;
import org.semanticweb.owl.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owl.model.OWLEquivalentPropertiesAxiom;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLFrame;
import org.semanticweb.owl.model.OWLFunctionalPropertyAxiom;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLIndividualTypeAssertion;
import org.semanticweb.owl.model.OWLInverseFunctionalPropertyAxiom;
import org.semanticweb.owl.model.OWLInversePropertyAxiom;
import org.semanticweb.owl.model.OWLNamedObject;
import org.semanticweb.owl.model.OWLNot;
import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLObjectAllRestriction;
import org.semanticweb.owl.model.OWLObjectCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectPropertyInstance;
import org.semanticweb.owl.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owl.model.OWLObjectSomeRestriction;
import org.semanticweb.owl.model.OWLObjectValueRestriction;
import org.semanticweb.owl.model.OWLObjectVisitor;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOr;
import org.semanticweb.owl.model.OWLPropertyDomainAxiom;
import org.semanticweb.owl.model.OWLSameIndividualsAxiom;
import org.semanticweb.owl.model.OWLSubClassAxiom;
import org.semanticweb.owl.model.OWLSubPropertyAxiom;
import org.semanticweb.owl.model.OWLSymmetricPropertyAxiom;
import org.semanticweb.owl.model.OWLTransitivePropertyAxiom;
import org.semanticweb.owl.model.change.AddAnnotationInstance;
import org.semanticweb.owl.model.change.AddClassAxiom;
import org.semanticweb.owl.model.change.AddDataPropertyInstance;
import org.semanticweb.owl.model.change.AddDataPropertyRange;
import org.semanticweb.owl.model.change.AddDataType;
import org.semanticweb.owl.model.change.AddDomain;
import org.semanticweb.owl.model.change.AddEntity;
import org.semanticweb.owl.model.change.AddEnumeration;
import org.semanticweb.owl.model.change.AddEquivalentClass;
import org.semanticweb.owl.model.change.AddForeignEntity;
import org.semanticweb.owl.model.change.AddImport;
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
import org.semanticweb.owl.model.change.RemoveAnnotationInstance;
import org.semanticweb.owl.model.change.RemoveClassAxiom;
import org.semanticweb.owl.model.change.RemoveDataPropertyInstance;
import org.semanticweb.owl.model.change.RemoveDataPropertyRange;
import org.semanticweb.owl.model.change.RemoveDataType;
import org.semanticweb.owl.model.change.RemoveDomain;
import org.semanticweb.owl.model.change.RemoveEntity;
import org.semanticweb.owl.model.change.RemoveEnumeration;
import org.semanticweb.owl.model.change.RemoveEquivalentClass;
import org.semanticweb.owl.model.change.RemoveForeignEntity;
import org.semanticweb.owl.model.change.RemoveImport;
import org.semanticweb.owl.model.change.RemoveIndividualAxiom;
import org.semanticweb.owl.model.change.RemoveIndividualClass;
import org.semanticweb.owl.model.change.RemoveInverse;
import org.semanticweb.owl.model.change.RemoveObjectPropertyInstance;
import org.semanticweb.owl.model.change.RemoveObjectPropertyRange;
import org.semanticweb.owl.model.change.RemovePropertyAxiom;
import org.semanticweb.owl.model.change.RemoveSuperClass;
import org.semanticweb.owl.model.change.RemoveSuperProperty;
import org.semanticweb.owl.model.change.SetDeprecated;
import org.semanticweb.owl.model.change.SetFunctional;
import org.semanticweb.owl.model.change.SetInverseFunctional;
import org.semanticweb.owl.model.change.SetLinkTarget;
import org.semanticweb.owl.model.change.SetLogicalURI;
import org.semanticweb.owl.model.change.SetOneToOne;
import org.semanticweb.owl.model.change.SetSymmetric;
import org.semanticweb.owl.model.change.SetTransitive;


/**
 * This is a simple test class to return a pretty string representing the object
 * @author ronwalf
 *
 */
public class ObjectString implements ChangeVisitor, OWLObjectVisitor {

	private String value;
	
	
	private ObjectString() {
	}
	
	public static String getString(OWLObject obj) throws OWLException {
		ObjectString objString = new ObjectString();
		obj.accept(objString);
		if (objString.value == null) {
			objString.value = obj.toString();
		}
		return objString.value;
	}
	
	public static String getString(OntologyChange obj) throws OWLException {
		ObjectString objString = new ObjectString();
		obj.accept(objString);
		if (objString.value == null) {
			objString.value = obj.toString();
		}
		return objString.value;
	}
	
	private String namedString(OWLNamedObject entity) throws OWLException {
		if (entity.getURI() != null) {
			return "<"+entity.getURI()+">";
		} else {
			return "(Anonymous "+entity.getClass()+")";
		}
	}
	
	public void visit(AddAnnotationInstance event) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	public void visit(AddEntity event) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(AddDataType event) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(RemoveEntity event) throws OWLException {
		value = "RemoveEntity "+namedString(event.getEntity());
	}

	public void visit(AddImport event) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(AddIndividualAxiom event) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(AddClassAxiom event) throws OWLException {
		value = "AddClassAxiom("+getString(event.getAxiom())+")";
	}

	public void visit(AddPropertyAxiom event) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(AddSuperClass event) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(AddSuperProperty event) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(AddIndividualClass event) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(AddEquivalentClass event) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(AddEnumeration event) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(AddDomain event) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(AddDataPropertyRange event) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(AddObjectPropertyRange event) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(AddInverse event) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(SetFunctional event) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(SetTransitive event) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(SetSymmetric event) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(SetInverseFunctional event) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(SetOneToOne event) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(SetDeprecated event) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(AddObjectPropertyInstance event) throws OWLException {
		value = ("AddObjectPropertyInstance("
				+getString(event.getSubject())+", "
				+getString(event.getProperty())+", "
				+getString(event.getObject())+")");
	}

	public void visit(AddDataPropertyInstance event) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(RemoveClassAxiom event) throws OWLException {
		value = "RemoveClassAxiom("+getString(event.getAxiom())+")";
	}

	public void visit(RemoveSuperClass event) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(RemoveEquivalentClass event) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(RemoveEnumeration event) throws OWLException {
		value = "RemoveEnumeration("+getString(event.getOWLClass())+", "+getString(event.getEnumeration())+")";
	}

	public void visit(SetLogicalURI event) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(RemoveDomain event) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(RemoveDataPropertyRange event) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(RemoveObjectPropertyRange event) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(RemovePropertyAxiom event) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(RemoveIndividualAxiom event) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(RemoveDataPropertyInstance event) throws OWLException {
		value = ("RemoveDataPropertyInstance("+
				getString(event.getSubject())+", "+
				getString(event.getProperty())+", "+
				getString(event.getObject())+")");
	}

	public void visit(RemoveObjectPropertyInstance event) throws OWLException {
		value = ("RemoveObjectPropertyInstance("
				+getString(event.getSubject())+", "
				+getString(event.getProperty())+", "
				+getString(event.getObject())+")");
	}

	public void visit(RemoveSuperProperty event) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(RemoveAnnotationInstance event) throws OWLException {
		value = ("RemoveAnnotationInstance ("+
				getString(event.getSubject())+", "+
				namedString(event.getProperty())+", "+
				event.getContent()+")");
	}

	public void visit(RemoveImport event) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(RemoveIndividualClass event) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(RemoveInverse event) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(SetLinkTarget target) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(AddForeignEntity entity) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(RemoveForeignEntity entity) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(OWLAnd node) throws OWLException {
		StringBuffer buffer = new StringBuffer();
		buffer.append("And(");
		for (Iterator iter = node.getOperands().iterator(); iter.hasNext();) {
			OWLObject obj = (OWLObject) iter.next();
			buffer.append(getString(obj));
			if (iter.hasNext()) {
				buffer.append(", ");
			}
		}
		buffer.append(")");
		
	}

	public void visit(OWLAnnotationProperty node) throws OWLException {
		value = "Annotation Prop "+namedString(node);	
	}

	public void visit(OWLAnnotationInstance node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	public void visit(OWLDataValue node) throws OWLException {
		value = "\""+node.getValue()+"\"";
		if (node.getURI() != null) {
			value += "^^<"+node.getURI()+">";
		}
		if (node.getLang() != null) {
			value += "@"+node.getLang();
		}
	}

	public void visit(OWLDataType node) throws OWLException {
		value = namedString(node);
	}

	public void visit(OWLDataEnumeration node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	public void visit(OWLDataAllRestriction node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	public void visit(OWLDataCardinalityRestriction node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	public void visit(OWLDataProperty node) throws OWLException {
		value = namedString(node);
	}

	public void visit(OWLDataSomeRestriction node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	public void visit(OWLDataValueRestriction node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	public void visit(OWLDifferentIndividualsAxiom node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	public void visit(OWLDisjointClassesAxiom node) throws OWLException {
		value = "DisjointClasses("+node.getDisjointClasses()+")";
	}

	public void visit(OWLEquivalentClassesAxiom node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	public void visit(OWLEquivalentPropertiesAxiom node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	public void visit(OWLFrame node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	public void visit(OWLIndividual node) throws OWLException {
		value = namedString(node);
	}

	public void visit(OWLObjectAllRestriction node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	public void visit(OWLObjectCardinalityRestriction node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	public void visit(OWLObjectProperty node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	public void visit(OWLObjectSomeRestriction node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	public void visit(OWLObjectValueRestriction node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	public void visit(OWLNot node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	public void visit(OWLOntology node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	public void visit(OWLOr node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	public void visit(OWLClass node) throws OWLException {
		// Default
	}

	public void visit(OWLEnumeration node) throws OWLException {
		value = node.getIndividuals().toString();
	}

	public void visit(OWLSameIndividualsAxiom node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	public void visit(OWLSubClassAxiom node) throws OWLException {
		value = "SubClass("+getString(node.getSubClass())+", "+getString(node.getSuperClass())+")";
	}

	public void visit(OWLSubPropertyAxiom node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	public void visit(OWLFunctionalPropertyAxiom node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	public void visit(OWLInverseFunctionalPropertyAxiom node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	public void visit(OWLTransitivePropertyAxiom node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	public void visit(OWLSymmetricPropertyAxiom node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	public void visit(OWLInversePropertyAxiom node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	public void visit(OWLPropertyDomainAxiom node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	public void visit(OWLObjectPropertyRangeAxiom node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	public void visit(OWLDataPropertyRangeAxiom node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	public void visit(OWLObjectPropertyInstance node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	public void visit(OWLDataPropertyInstance node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	public void visit(OWLIndividualTypeAssertion node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	public void visit(RemoveDataType type) throws OWLException {
		// TODO Auto-generated method stub
		
	}

}
