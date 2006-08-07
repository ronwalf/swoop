package org.mindswap.swoop.utils.owlapi.diff;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.semanticweb.owl.model.OWLAnnotationInstance;
import org.semanticweb.owl.model.OWLAnnotationProperty;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLClassAxiom;
import org.semanticweb.owl.model.OWLClassAxiomVisitor;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owl.model.OWLDataRange;
import org.semanticweb.owl.model.OWLDataType;
import org.semanticweb.owl.model.OWLDataValue;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owl.model.OWLDisjointClassesAxiom;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLEntityVisitor;
import org.semanticweb.owl.model.OWLEnumeration;
import org.semanticweb.owl.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owl.model.OWLEquivalentPropertiesAxiom;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLFunctionalPropertyAxiom;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLIndividualAxiom;
import org.semanticweb.owl.model.OWLIndividualAxiomVisitor;
import org.semanticweb.owl.model.OWLInverseFunctionalPropertyAxiom;
import org.semanticweb.owl.model.OWLInversePropertyAxiom;
import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLProperty;
import org.semanticweb.owl.model.OWLPropertyAxiom;
import org.semanticweb.owl.model.OWLPropertyAxiomVisitor;
import org.semanticweb.owl.model.OWLPropertyDomainAxiom;
import org.semanticweb.owl.model.OWLSameIndividualsAxiom;
import org.semanticweb.owl.model.OWLSubClassAxiom;
import org.semanticweb.owl.model.OWLSubPropertyAxiom;
import org.semanticweb.owl.model.OWLSymmetricPropertyAxiom;
import org.semanticweb.owl.model.OWLTransitivePropertyAxiom;
import org.semanticweb.owl.model.change.RemoveAnnotationInstance;
import org.semanticweb.owl.model.change.RemoveClassAxiom;
import org.semanticweb.owl.model.change.RemoveDataPropertyInstance;
import org.semanticweb.owl.model.change.RemoveDataPropertyRange;
import org.semanticweb.owl.model.change.RemoveDataType;
import org.semanticweb.owl.model.change.RemoveDomain;
import org.semanticweb.owl.model.change.RemoveEntity;
import org.semanticweb.owl.model.change.RemoveEnumeration;
import org.semanticweb.owl.model.change.RemoveIndividualAxiom;
import org.semanticweb.owl.model.change.RemoveIndividualClass;
import org.semanticweb.owl.model.change.RemoveObjectPropertyInstance;
import org.semanticweb.owl.model.change.RemoveObjectPropertyRange;
import org.semanticweb.owl.model.change.RemovePropertyAxiom;
import org.semanticweb.owl.model.change.RemoveSuperClass;
import org.semanticweb.owl.model.change.RemoveSuperProperty;
import org.semanticweb.owl.model.change.SetDeprecated;
import org.semanticweb.owl.model.change.SetFunctional;
import org.semanticweb.owl.model.change.SetInverseFunctional;
import org.semanticweb.owl.model.change.SetOneToOne;
import org.semanticweb.owl.model.change.SetSymmetric;
import org.semanticweb.owl.model.change.SetTransitive;


public class NegativeDiff implements OWLEntityVisitor, OWLClassAxiomVisitor,
		OWLPropertyAxiomVisitor, OWLIndividualAxiomVisitor {

	/**
	 * Get the differences as a set of OWLChanges, ready to apply to the
	 * target.
	 * @param source The source ontology
	 * @param destination The destination ontology
	 * @param target The OWLOntology the changes should be applied to.
	 * @return
	 * @throws OWLException 
	 */
	public static List getChanges(OWLOntology source, OWLOntology destination, OWLOntology target) throws OWLException {
		NegativeDiff ndiff = new NegativeDiff(source, destination);
		return ndiff.getChanges(target);
	}
	private List changes;
	private OWLCopy copier;
	private OWLOntology destination;
	private OWLDataFactory factory;
	
	
	private OWLOntology source;
	
	private OWLOntology target;
	
	
	public NegativeDiff(OWLOntology source, OWLOntology destination) {
		this.source = source;
		this.destination = destination;
		
		changes = new Vector();
	}
	
	/**
	 * Remove any excess annotations an object might have.
	 * @param src
	 * @param dst
	 * @param tgt
	 * @throws OWLException
	 */
	protected void annotationRemover(OWLObject src, OWLObject dst, OWLObject tgt) throws OWLException {
		Set srcAnnotations = src.getAnnotations(source);
		Set dstAnnotations = dst.getAnnotations(destination);
		
		for (Iterator iter = srcAnnotations.iterator(); iter.hasNext();) {
			OWLAnnotationInstance annotation = (OWLAnnotationInstance) iter.next();
			if (!dstAnnotations.contains(annotation)) {
				OWLAnnotationProperty tgtProp = (OWLAnnotationProperty) copier.copy(annotation.getProperty());
				changes.add(new RemoveAnnotationInstance(target, tgt, tgtProp, annotation.getContent(), null));
			}
		}
	}
	
	/**
	 * Adds changes between OWLProperties (not Data or Object property specific changes)
	 * @param src
	 * @param dst
	 * @param tgt
	 * @throws OWLException
	 */
	private void genericPropertyChanges(OWLProperty entity, OWLProperty dstEntity, OWLProperty tgtEntity) throws OWLException {
		annotationRemover(entity, dstEntity, tgtEntity);
		if (entity.isDeprecated(source) && !dstEntity.isDeprecated(destination)) {
			changes.add(new SetDeprecated(target, tgtEntity, false, null));
		}
		if (entity.isFunctional(source) && !dstEntity.isFunctional(destination)) {
			changes.add(new SetFunctional(target, tgtEntity, false, null));
		}
		
		for (Iterator iter = entity.getDomains(source).iterator(); iter.hasNext(); ) {
			OWLDescription desc = (OWLDescription) iter.next();
			if (!dstEntity.getDomains(destination).contains(desc)) {
				OWLDescription tgtDesc = copier.copy(desc);
				changes.add(new RemoveDomain(target, tgtEntity, tgtDesc, null));
			}
		}
		
		for (Iterator iter = entity.getSuperProperties(source).iterator(); iter.hasNext();) {
			OWLProperty prop = (OWLProperty) iter.next();
			if (!dstEntity.getSuperProperties(destination).contains(prop)) {
				OWLProperty tgtProp = (OWLProperty) copier.copy(prop);
				changes.add(new RemoveSuperProperty(target, tgtEntity, tgtProp, null));
			}
		}
	}
	
	/**
	 * Get the differences as a set of OWLChanges, ready to apply to the
	 * target.
	 * @param target The OWLOntology the changes should be applied to.
	 * @return
	 * @throws OWLException 
	 */
	public List getChanges(OWLOntology target) throws OWLException {
		changes = new Vector();
		this.target = target;
		factory = target.getOWLDataFactory();
		copier = new OWLCopy(source, target);
		
		Set entities = new HashSet();
		entities.addAll(source.getClasses());
		entities.addAll(source.getIndividuals());
		entities.addAll(source.getAnnotationProperties());
		entities.addAll(source.getDataProperties());
		entities.addAll(source.getObjectProperties());
		
		
		for (Iterator iter = entities.iterator(); iter.hasNext();) {
			((OWLEntity) iter.next()).accept(this);
		}
		
		for (Iterator iter = source.getClassAxioms().iterator(); iter.hasNext();) {
			((OWLClassAxiom) iter.next()).accept(this);
		}
		
		for (Iterator iter = source.getPropertyAxioms().iterator(); iter.hasNext();) {
			((OWLPropertyAxiom) iter.next()).accept(this);
		}
		
		for (Iterator iter = source.getIndividualAxioms().iterator(); iter.hasNext();) {
			((OWLIndividualAxiom) iter.next()).accept(this);
		}
		
		// Data types don't have a specific visitor, don't want all of OWLObjectVisitor
		for (Iterator iter = source.getDatatypes().iterator(); iter.hasNext();) {
			OWLDataType srcDT = (OWLDataType) iter.next();
			if (!destination.getDatatypes().contains(srcDT)) {
				OWLDataType tgtDT = factory.getOWLConcreteDataType(srcDT.getURI());
				changes.add(new RemoveDataType(target, tgtDT, null));
			}
		}
		
		return changes; 
	}
	
	public void visit(OWLAnnotationProperty entity) throws OWLException {
		OWLAnnotationProperty tgtEntity = factory.getOWLAnnotationProperty(entity.getURI());
		OWLAnnotationProperty dstEntity = destination.getAnnotationProperty(entity.getURI());
		
		if (dstEntity == null) {
			changes.add(new RemoveEntity(target, tgtEntity, null));
			dstEntity = destination.getOWLDataFactory().getOWLAnnotationProperty(entity.getURI());
		}
		annotationRemover(entity, dstEntity, tgtEntity);
	}

	public void visit(OWLClass entity) throws OWLException {
		OWLClass tgtEntity = factory.getOWLClass(entity.getURI());
		OWLClass dstEntity = destination.getClass(entity.getURI());
		
		if (dstEntity == null) {
			changes.add(new RemoveEntity(target, tgtEntity, null));
			dstEntity = destination.getOWLDataFactory().getOWLClass(entity.getURI());
		} 
		
		annotationRemover(entity, dstEntity, tgtEntity);
		
		for (Iterator iter = entity.getEnumerations(source).iterator(); iter.hasNext();) {
			OWLEnumeration srcEnum = (OWLEnumeration) iter.next();
			if (!dstEntity.getEnumerations(destination).contains(srcEnum)) {
				changes.add(new RemoveEnumeration(target, tgtEntity,
						(OWLEnumeration) copier.copy(srcEnum), null));
			}
		}
		
		for (Iterator iter = entity.getSuperClasses(source).iterator(); iter.hasNext();) {
			OWLDescription desc = (OWLDescription) iter.next();
			if (!dstEntity.getSuperClasses(destination).contains(desc)) {
				changes.add(new RemoveSuperClass(target, tgtEntity,
						copier.copy(desc), null));
			}
		}
	}

	public void visit(OWLDataProperty entity) throws OWLException {
		OWLDataProperty tgtEntity = factory.getOWLDataProperty(entity.getURI());
		OWLDataProperty dstEntity = destination.getDataProperty(entity.getURI());
		
		if (dstEntity == null) {
			changes.add(new RemoveEntity(target, tgtEntity, null));
			dstEntity = destination.getOWLDataFactory().getOWLDataProperty(entity.getURI());
		} 
		genericPropertyChanges(entity, dstEntity, tgtEntity);
		
		for (Iterator iter = entity.getRanges(source).iterator(); iter.hasNext(); ) {
			OWLDataRange range = (OWLDataRange) iter.next();
			if (!dstEntity.getRanges(destination).contains(range)) {
				OWLDataRange tgtRange = copier.copyDataRange(range);
				changes.add(new RemoveDataPropertyRange(target, tgtEntity, tgtRange, null));
			}
		}
	}

	public void visit(OWLDataPropertyRangeAxiom axiom) throws OWLException {
		if (!destination.getPropertyAxioms().contains(axiom)) {
			OWLDataProperty tgtProperty = (OWLDataProperty) copier.copy(axiom.getProperty());
			OWLDataRange tgtRange = copier.copyDataRange(axiom.getRange());
			OWLPropertyAxiom tgtAxiom = factory.getOWLDataPropertyRangeAxiom(tgtProperty, tgtRange);
			changes.add(new RemovePropertyAxiom(target, tgtAxiom, null));
		}
	}

	public void visit(OWLDifferentIndividualsAxiom axiom) throws OWLException {
		if (!destination.getIndividualAxioms().contains(axiom)) {
			Set different = new HashSet();
		
			for (Iterator iter = axiom.getIndividuals().iterator(); iter.hasNext(); ) {
				different.add(copier.copy((OWLIndividual)iter.next()));
			}
			OWLIndividualAxiom tgtAxiom = factory.getOWLDifferentIndividualsAxiom(different);
			changes.add(new RemoveIndividualAxiom(target, tgtAxiom, null));
		}
	}

	public void visit(OWLDisjointClassesAxiom axiom) throws OWLException {
		if (!destination.getClassAxioms().contains(axiom)) {
			Set disjoints = new HashSet();
			for (Iterator iter = axiom.getDisjointClasses().iterator(); iter.hasNext(); ) {
				disjoints.add(copier.copy((OWLDescription) iter.next()));
			}
			OWLClassAxiom tgtAxiom = factory.getOWLDisjointClassesAxiom(disjoints);
			changes.add(new RemoveClassAxiom(target, tgtAxiom, null));
		}
	}

	public void visit(OWLEquivalentClassesAxiom axiom) throws OWLException {
		if (!destination.getClassAxioms().contains(axiom)) {
			Set equivalents = new HashSet();
			for (Iterator iter = axiom.getEquivalentClasses().iterator(); iter.hasNext(); ) {
				equivalents.add(copier.copy((OWLDescription) iter.next()));
			}
			OWLClassAxiom tgtAxiom = factory.getOWLEquivalentClassesAxiom(equivalents);
			changes.add(new RemoveClassAxiom(target, tgtAxiom, null));
		}
	}

	public void visit(OWLEquivalentPropertiesAxiom axiom) throws OWLException {
		if (!destination.getPropertyAxioms().contains(axiom)) {
			Set equivalents = new HashSet();
			for (Iterator iter = axiom.getProperties().iterator(); iter.hasNext();) {
				equivalents.add(copier.copy((OWLProperty) iter.next()));
			}
			OWLPropertyAxiom tgtAxiom = factory.getOWLEquivalentPropertiesAxiom(equivalents);
			changes.add(new RemovePropertyAxiom(target, tgtAxiom, null));
		}
	}

	public void visit(OWLFunctionalPropertyAxiom axiom) throws OWLException {
		if (!destination.getPropertyAxioms().contains(axiom)) {
			OWLProperty tgtProp = (OWLProperty) copier.copy(axiom.getProperty());
			OWLPropertyAxiom tgtAxiom = factory.getOWLFunctionalPropertyAxiom(tgtProp);
			changes.add(new RemovePropertyAxiom(target, tgtAxiom, null));
		}
	}

	public void visit(OWLIndividual entity) throws OWLException {
		OWLIndividual tgtEntity;
		OWLIndividual dstEntity;
		
		if (entity.isAnonymous()) {
			tgtEntity = factory.getAnonOWLIndividual(entity.getAnonId());
			dstEntity = destination.getIndividual(entity.getAnonId());
		} else {
			tgtEntity = factory.getOWLIndividual(entity.getURI());
			dstEntity = destination.getIndividual(entity.getURI());
		}
		
		if (dstEntity == null) {
			changes.add(new RemoveEntity(target, tgtEntity, null));
			if (entity.isAnonymous()) {
				dstEntity = destination.getOWLDataFactory().getAnonOWLIndividual(entity.getAnonId());
			} else {
				dstEntity = destination.getOWLDataFactory().getOWLIndividual(entity.getURI());
			}
		}
		annotationRemover(entity, dstEntity, tgtEntity);

		Set srcTypes = entity.getTypes(source);
		Set dstTypes = dstEntity.getTypes(destination);
		for (Iterator typeIter = srcTypes.iterator(); typeIter.hasNext();) {
			OWLDescription srcType = (OWLDescription) typeIter.next();
			if (!dstTypes.contains(srcType)) {
				OWLDescription tgtType = copier.copy(srcType);
				changes.add(new RemoveIndividualClass(target, tgtEntity,
						tgtType, null));
			}
		}

		Map srcProps = entity.getDataPropertyValues(source);
		Map dstProps = dstEntity.getDataPropertyValues(destination);
		for (Iterator propIter = srcProps.keySet().iterator(); propIter
				.hasNext();) {
			OWLDataProperty prop = (OWLDataProperty) propIter.next();
			OWLDataProperty dstProp = destination.getOWLDataFactory().getOWLDataProperty(prop.getURI());
			OWLDataProperty tgtProp = factory.getOWLDataProperty(prop.getURI());

			Set srcValues = (Set) srcProps.get(prop);

			Set dstValues = (Set) dstProps.get(dstProp);
			if (dstValues == null) {
				dstValues = Collections.EMPTY_SET;
			}

			for (Iterator valueIter = srcValues.iterator(); valueIter.hasNext();) {
				OWLDataValue dv = (OWLDataValue) valueIter.next();
				if (!dstValues.contains(dv)) {
					OWLDataValue tgtDV = factory.getOWLConcreteData(
							dv.getURI(), dv.getLang(), dv.getValue());
					changes.add(new RemoveDataPropertyInstance(target,
							tgtEntity, tgtProp, tgtDV, null));
				}
			}
		}

		srcProps = entity.getObjectPropertyValues(source);
		dstProps = dstEntity.getObjectPropertyValues(destination);
		for (Iterator propIter = srcProps.keySet().iterator(); propIter
				.hasNext();) {
			OWLObjectProperty prop = (OWLObjectProperty) propIter.next();
			OWLObjectProperty dstProp = destination.getOWLDataFactory().getOWLObjectProperty(prop.getURI());
			OWLObjectProperty tgtProp = factory.getOWLObjectProperty(prop.getURI());

			Set srcValues = (Set) srcProps.get(prop);
			Set dstValues = (Set) dstProps.get(dstProp);
			if (dstValues == null) {
				dstValues = Collections.EMPTY_SET;
			}

			for (Iterator valueIter = srcValues.iterator(); valueIter.hasNext();) {
				OWLIndividual object = (OWLIndividual) valueIter.next();
				OWLIndividual dstObject;
				OWLIndividual tgtInd;
				if (object.isAnonymous()) {
					dstObject = destination.getOWLDataFactory().getAnonOWLIndividual(object.getAnonId());
					tgtInd = factory.getAnonOWLIndividual(object.getAnonId());
				} else {
					dstObject = destination.getOWLDataFactory().getOWLIndividual(object.getURI());
					tgtInd = factory.getOWLIndividual(object.getURI());
				}
				if (!dstValues.contains(dstObject)) {
					changes.add(new RemoveObjectPropertyInstance(target,
							tgtEntity, tgtProp, tgtInd, null));
				}
			}
		}
			
			
		
	}

	public void visit(OWLInverseFunctionalPropertyAxiom axiom) throws OWLException {
		if (!destination.getPropertyAxioms().contains(axiom)) {
			OWLObjectProperty tgtProp = (OWLObjectProperty) copier.copy(axiom.getProperty());
			OWLPropertyAxiom tgtAxiom = factory.getOWLInverseFunctionalPropertyAxiom(tgtProp);
			changes.add(new RemovePropertyAxiom(target, tgtAxiom, null));
		}
	}

	public void visit(OWLInversePropertyAxiom axiom) throws OWLException {
		if (!destination.getPropertyAxioms().contains(axiom)) {
			OWLObjectProperty tgtProp = (OWLObjectProperty) copier.copy(axiom.getProperty());
			OWLObjectProperty tgtInverse = (OWLObjectProperty) copier.copy(axiom.getInverseProperty());
			OWLPropertyAxiom tgtAxiom = factory.getOWLInversePropertyAxiom(tgtProp, tgtInverse);
			changes.add(new RemovePropertyAxiom(target, tgtAxiom, null));
		}
	}


	public void visit(OWLObjectProperty entity) throws OWLException {
		OWLObjectProperty tgtEntity = factory.getOWLObjectProperty(entity.getURI());
		OWLObjectProperty dstEntity = destination.getObjectProperty(entity.getURI());
		
		if (dstEntity == null) {
			changes.add(new RemoveEntity(target, tgtEntity, null));
			dstEntity = destination.getOWLDataFactory().getOWLObjectProperty(entity.getURI());
		}
		genericPropertyChanges(entity, dstEntity, tgtEntity);
		
		if (entity.isInverseFunctional(source) && !dstEntity.isInverseFunctional(destination)) {
			changes.add(new SetInverseFunctional(target, tgtEntity, false, null));
		}
		if (entity.isSymmetric(source) && !dstEntity.isSymmetric(destination)) {
			changes.add(new SetSymmetric(target, tgtEntity, false, null));
		}
		if (entity.isTransitive(source) && !dstEntity.isTransitive(destination)) {
			changes.add(new SetTransitive(target, tgtEntity, false, null));
		}
		if (entity.isOneToOne(source) && !dstEntity.isOneToOne(destination)) {
			changes.add(new SetOneToOne(target, tgtEntity, false, null));
		}
		
		for (Iterator iter = entity.getRanges(source).iterator(); iter.hasNext(); ) {
			OWLDescription desc = (OWLDescription) iter.next();
			if (!dstEntity.getRanges(destination).contains(desc)) {
				OWLDescription tgtDesc = copier.copy(desc);
				changes.add(new RemoveObjectPropertyRange(target, tgtEntity, tgtDesc, null));
			}
		}
		
	}


	public void visit(OWLObjectPropertyRangeAxiom axiom) throws OWLException {
		if (!destination.getPropertyAxioms().contains(axiom)) {
			OWLObjectProperty tgtProperty = (OWLObjectProperty) copier.copy(axiom.getProperty());
			OWLDescription tgtRange = copier.copy(axiom.getRange());
			OWLPropertyAxiom tgtAxiom = factory.getOWLObjectPropertyRangeAxiom(tgtProperty, tgtRange);
			changes.add(new RemovePropertyAxiom(target, tgtAxiom, null));
		}
	}


	public void visit(OWLPropertyDomainAxiom axiom) throws OWLException {
		if (!destination.getPropertyAxioms().contains(axiom)) {
			OWLProperty tgtProperty = (OWLProperty) copier.copy(axiom.getProperty());
			OWLDescription tgtDomain = copier.copy(axiom.getDomain());
			OWLPropertyAxiom tgtAxiom = factory.getOWLPropertyDomainAxiom(tgtProperty, tgtDomain);
			changes.add(new RemovePropertyAxiom(target, tgtAxiom, null));
		}
	}


	public void visit(OWLSameIndividualsAxiom axiom) throws OWLException {
		if (!destination.getIndividualAxioms().contains(axiom)) {
			Set equivalents = new HashSet();
			for (Iterator iter = axiom.getIndividuals().iterator(); iter.hasNext(); ) {
				equivalents.add(copier.copy((OWLIndividual)iter.next()));
			}
			OWLIndividualAxiom tgtAxiom = factory.getOWLSameIndividualsAxiom(equivalents);
			changes.add(new RemoveIndividualAxiom(target, tgtAxiom, null));
		}
	}


	public void visit(OWLSubClassAxiom axiom) throws OWLException {
		if (!destination.getClassAxioms().contains(axiom)) {
			OWLDescription subClass = copier.copy(axiom.getSubClass());
			OWLDescription superClass = copier.copy(axiom.getSuperClass());
			OWLClassAxiom tgtAxiom = factory.getOWLSubClassAxiom(subClass, superClass);

			changes.add(new RemoveClassAxiom(target, tgtAxiom, null));
		}
	}


	public void visit(OWLSubPropertyAxiom axiom) throws OWLException {
		if (!destination.getPropertyAxioms().contains(axiom)) {
			OWLProperty subProp = (OWLProperty) copier.copy(axiom.getSubProperty());
			OWLProperty superProp = (OWLProperty) copier.copy(axiom.getSuperProperty());
			OWLPropertyAxiom tgtAxiom = factory.getOWLSubPropertyAxiom(subProp, superProp);
		
			changes.add(new RemovePropertyAxiom(target, tgtAxiom, null));
		}
	}


	public void visit(OWLSymmetricPropertyAxiom axiom) throws OWLException {
		if (!destination.getPropertyAxioms().contains(axiom)) {
			OWLObjectProperty tgtProp = (OWLObjectProperty) copier.copy(axiom.getProperty());
			OWLPropertyAxiom tgtAxiom = factory.getOWLSymmetricPropertyAxiom(tgtProp);
			changes.add(new RemovePropertyAxiom(target, tgtAxiom, null));
		}
	}


	public void visit(OWLTransitivePropertyAxiom axiom) throws OWLException {
		if (!destination.getPropertyAxioms().contains(axiom)) {
			OWLObjectProperty tgtProp = (OWLObjectProperty) copier.copy(axiom.getProperty());
			OWLPropertyAxiom tgtAxiom = factory.getOWLTransitivePropertyAxiom(tgtProp);
			changes.add(new RemovePropertyAxiom(target, tgtAxiom, null));
		}
	}

}
