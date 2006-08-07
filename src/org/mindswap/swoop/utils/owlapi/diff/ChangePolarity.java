package org.mindswap.swoop.utils.owlapi.diff;

import java.net.URI;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.semanticweb.owl.model.OWLException;
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

public class ChangePolarity implements ChangeVisitor {

	List changes;
	
	private ChangePolarity() {
		changes = new Vector();
	}
	
	public static List invert(Collection changes) throws OWLException {
		ChangePolarity changer = new ChangePolarity();
		
		for (Iterator iter = changes.iterator(); iter.hasNext(); ) {
			OntologyChange change = (OntologyChange) iter.next();
			change.accept(changer);
		}
		
		return changer.changes;
	}
	
	public void visit(AddAnnotationInstance event) throws OWLException {
		changes.add(new RemoveAnnotationInstance(event.getOntology(), event.getSubject(), event.getProperty(), event.getContent(), event.getCause()));
	}

	public void visit(AddEntity event) throws OWLException {
		changes.add(new RemoveEntity(event.getOntology(), event.getEntity(), event.getCause()));
	}

	public void visit(AddDataType event) throws OWLException {
		changes.add(new RemoveDataType(event.getOntology(), event.getDatatype(), event.getCause()));
	}

	public void visit(RemoveEntity event) throws OWLException {
		changes.add(new AddEntity(event.getOntology(), event.getEntity(), event.getCause()));
	}

	public void visit(AddImport event) throws OWLException {
		changes.add(new RemoveImport(event.getOntology(), event.getImportOntology(), event.getCause()));
	}

	public void visit(AddIndividualAxiom event) throws OWLException {
		changes.add(new RemoveIndividualAxiom(event.getOntology(), event.getAxiom(), event.getCause()));
	}

	public void visit(AddClassAxiom event) throws OWLException {
		changes.add(new RemoveClassAxiom(event.getOntology(), event.getAxiom(), event.getCause()));
	}

	public void visit(AddPropertyAxiom event) throws OWLException {
		changes.add(new RemovePropertyAxiom(event.getOntology(), event.getAxiom(), event.getCause()));
	}

	public void visit(AddSuperClass event) throws OWLException {
		changes.add(new RemoveSuperClass(event.getOntology(), event.getOWLClass(), event.getDescription(), event.getCause()));
	}

	public void visit(AddSuperProperty event) throws OWLException {
		changes.add(new RemoveSuperProperty(event.getOntology(), event.getProperty(), event.getSuperProperty(), event.getCause()));
	}

	public void visit(AddIndividualClass event) throws OWLException {
		changes.add(new RemoveIndividualClass(event.getOntology(), event.getIndividual(), event.getDescription(), event.getCause()));
	}

	public void visit(AddEquivalentClass event) throws OWLException {
		changes.add(new RemoveEquivalentClass(event.getOntology(), event.getOWLClass(), event.getDescription(), event.getCause()));
	}

	public void visit(AddEnumeration event) throws OWLException {
		changes.add(new RemoveEnumeration(event.getOntology(), event.getOWLClass(), event.getEnumeration(), event.getCause()));
	}

	public void visit(AddDomain event) throws OWLException {
		changes.add(new RemoveDomain(event.getOntology(), event.getProperty(), event.getDomain(), event.getCause()));
	}

	public void visit(AddDataPropertyRange event) throws OWLException {
		changes.add(new RemoveDataPropertyRange(event.getOntology(), event.getProperty(), event.getRange(), event.getCause()));
	}

	public void visit(AddObjectPropertyRange event) throws OWLException {
		changes.add(new RemoveObjectPropertyRange(event.getOntology(), event.getProperty(), event.getRange(), event.getCause()));
	}

	public void visit(AddInverse event) throws OWLException {
		changes.add(new RemoveInverse(event.getOntology(), event.getProperty(), event.getInverse(), event.getCause()));
	}

	public void visit(SetFunctional event) throws OWLException {
		changes.add(new SetFunctional(event.getOntology(), event.getProperty(), !event.isFunctional(), event.getCause()));
	}

	public void visit(SetTransitive event) throws OWLException {
		changes.add(new SetTransitive(event.getOntology(), event.getProperty(), !event.isTransitive(), event.getCause()));
	}

	public void visit(SetSymmetric event) throws OWLException {
		changes.add(new SetSymmetric(event.getOntology(), event.getProperty(), !event.isSymmetric(), event.getCause()));
	}

	public void visit(SetInverseFunctional event) throws OWLException {
		changes.add(new SetInverseFunctional(event.getOntology(), event.getProperty(), !event.isInverseFunctional(), event.getCause()));
	}

	public void visit(SetOneToOne event) throws OWLException {
		changes.add(new SetOneToOne(event.getOntology(), event.getProperty(), !event.isOneToOne(), event.getCause()));
	}

	public void visit(SetDeprecated event) throws OWLException {
		changes.add(new SetDeprecated(event.getOntology(), event.getObject(), !event.isDeprecated(), event.getCause()));
	}

	public void visit(AddObjectPropertyInstance event) throws OWLException {
		changes.add(new RemoveObjectPropertyInstance(event.getOntology(), event.getSubject(), event.getProperty(), event.getObject(), event.getCause()));
	}

	public void visit(AddDataPropertyInstance event) throws OWLException {
		changes.add(new RemoveDataPropertyInstance(event.getOntology(), event.getSubject(), event.getProperty(), event.getObject(), event.getCause()));

	}

	public void visit(RemoveClassAxiom event) throws OWLException {
		changes.add(new AddClassAxiom(event.getOntology(), event.getAxiom(), event.getCause()));
	}

	public void visit(RemoveSuperClass event) throws OWLException {
		changes.add(new AddSuperClass(event.getOntology(), event.getOWLClass(), event.getDescription(), event.getCause()));
	}

	public void visit(RemoveEquivalentClass event) throws OWLException {
		changes.add(new AddEquivalentClass(event.getOntology(), event.getOWLClass(), event.getDescription(), event.getCause()));
	}

	public void visit(RemoveEnumeration event) throws OWLException {
		changes.add(new AddEnumeration(event.getOntology(), event.getOWLClass(), event.getEnumeration(), event.getCause()));
	}

	public void visit(SetLogicalURI event) throws OWLException {
		// Err, no reverse of this.
		changes.add(event);
	}

	public void visit(RemoveDomain event) throws OWLException {
		changes.add(new AddDomain(event.getOntology(), event.getProperty(), event.getDomain(), event.getCause()));
	}

	public void visit(RemoveDataPropertyRange event) throws OWLException {
		changes.add(new AddDataPropertyRange(event.getOntology(), event.getProperty(), event.getRange(), event.getCause()));
	}

	public void visit(RemoveObjectPropertyRange event) throws OWLException {
		changes.add(new AddObjectPropertyRange(event.getOntology(), event.getProperty(), event.getRange(), event.getCause()));
	}

	public void visit(RemovePropertyAxiom event) throws OWLException {
		changes.add(new AddPropertyAxiom(event.getOntology(), event.getAxiom(), event.getCause()));
	}

	public void visit(RemoveIndividualAxiom event) throws OWLException {
		changes.add(new AddIndividualAxiom(event.getOntology(), event.getAxiom(), event.getCause()));
	}

	public void visit(RemoveDataPropertyInstance event) throws OWLException {
		changes.add(new AddDataPropertyInstance(event.getOntology(), event.getSubject(), event.getProperty(), event.getObject(), event.getCause()));
	}

	public void visit(RemoveObjectPropertyInstance event) throws OWLException {
		changes.add(new AddObjectPropertyInstance(event.getOntology(), event.getSubject(), event.getProperty(), event.getObject(), event.getCause()));
	}

	public void visit(RemoveSuperProperty event) throws OWLException {
		changes.add(new AddSuperProperty(event.getOntology(), event.getProperty(), event.getSuperProperty(), event.getCause()));
	}

	public void visit(RemoveAnnotationInstance event) throws OWLException {
		changes.add(new AddAnnotationInstance(event.getOntology(), event.getSubject(), event.getProperty(), event.getContent(), event.getCause()));
	}

	public void visit(RemoveImport event) throws OWLException {
		changes.add(new AddImport(event.getOntology(), event.getImportOntology(), event.getCause()));
	}

	public void visit(RemoveIndividualClass event) throws OWLException {
		changes.add(new AddIndividualClass(event.getOntology(), event.getIndividual(), event.getDescription(), event.getCause()));
	}

	public void visit(RemoveInverse event) throws OWLException {
		changes.add(new AddInverse(event.getOntology(), event.getProperty(), event.getInverse(), event.getCause()));
	}

	public void visit(SetLinkTarget target) throws OWLException {
		// No inverse
		changes.add(target);
	}

	public void visit(AddForeignEntity entity) throws OWLException {
		changes.add(new RemoveForeignEntity(entity.getOntology(), entity.getForeignEntity(), entity.getCause()));
	}

	public void visit(RemoveForeignEntity entity) throws OWLException {
		for (Iterator iter = entity.getOntology().getForeignEntities().entrySet().iterator();
				iter.hasNext(); ) {
			Map.Entry entry = (Map.Entry) iter.next();
			if (entity.getForeignEntity().equals(entry.getValue())) {
				changes.add(new AddForeignEntity(entity.getOntology(), entity.getForeignEntity(), (URI) entry.getKey(), entity.getCause()));
			}
		}
	}

	public void visit(RemoveDataType event) throws OWLException {
		changes.add(new AddDataType(event.getOntology(), event.getDatatype(), event.getCause()));
	}

}
