package org.mindswap.swoop.utils.owlapi.diff;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.semanticweb.owl.model.OWLAnd;
import org.semanticweb.owl.model.OWLAnnotationProperty;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataAllRestriction;
import org.semanticweb.owl.model.OWLDataCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataEnumeration;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataRange;
import org.semanticweb.owl.model.OWLDataSomeRestriction;
import org.semanticweb.owl.model.OWLDataType;
import org.semanticweb.owl.model.OWLDataValue;
import org.semanticweb.owl.model.OWLDataValueRestriction;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDescriptionVisitor;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLEntityVisitor;
import org.semanticweb.owl.model.OWLEnumeration;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLFrame;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLNot;
import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLObjectAllRestriction;
import org.semanticweb.owl.model.OWLObjectCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectSomeRestriction;
import org.semanticweb.owl.model.OWLObjectValueRestriction;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOr;
import org.semanticweb.owl.model.OWLRestriction;



/**
 * Copies an OWLObject from one ontology with respect to another.
 * @author ronwalf
 *
 */
public class OWLCopy implements OWLDescriptionVisitor, OWLEntityVisitor {

	private OWLDataFactory factory;
	private OWLOntology source;
	private OWLOntology destination;
	private OWLObject value;
	
	public OWLCopy(OWLOntology source, OWLOntology destination) throws OWLException {
		this.source = source;
		this.destination = destination;
		factory = destination.getOWLDataFactory();
		
	}
	
	/**
	 * Return a copy of the given OWLDescription from the source ontology with respect to
	 * the destination ontology.
	 * 
	 * The copy will not include axiom information.
	 * 
	 * @param source - The ontology the desription is from.
	 * @param destination - The ontology where the copy will be made against.
	 * @param description 
	 * @return A copy of the description.
	 * @throws OWLException
	 */
	public static OWLDescription copy(OWLOntology source, OWLOntology destination, OWLDescription description) throws OWLException {
		OWLCopy copier = new OWLCopy(source, destination);
		description.accept(copier);
		return (OWLDescription) copier.value;
	}
	
	/**
	 * Return a copy of the given OWLEntity from the source ontology with respect to
	 * the destination ontology.
	 * 
	 * The copy will not include axiom or property information.
	 * 
	 * @param source - The ontology the desription is from.
	 * @param destination - The ontology where the copy will be made against.
	 * @param description 
	 * @return A copy of the entity.
	 * @throws OWLException
	 */
	public static OWLEntity copy(OWLOntology source, OWLOntology destination, OWLEntity description) throws OWLException {
		OWLCopy copier = new OWLCopy(source, destination);
		description.accept(copier);
		return (OWLEntity) copier.value;
	}
	
	public OWLDescription copy(OWLDescription description) throws OWLException {
		OWLCopy copier = new OWLCopy(source, destination);
		description.accept(copier);
		return (OWLDescription) copier.value;
	}
	
	public OWLEntity copy(OWLEntity entity) throws OWLException {
		OWLCopy copier = new OWLCopy(source, destination);
		entity.accept(copier);
		return (OWLEntity) copier.value;
	}
	
	
	public OWLDataRange copyDataRange(OWLDataRange dataRange) throws OWLException {
		OWLDataRange result;
		if (dataRange instanceof OWLDataType) {
			OWLDataType dataType = (OWLDataType) dataRange;
			result = factory.getOWLConcreteDataType(dataType.getURI());
		} else if (dataRange instanceof OWLDataEnumeration) {
			OWLDataEnumeration dataEnum = (OWLDataEnumeration) dataRange;
			Set values = new HashSet();
			for (Iterator iter = dataEnum.getValues().iterator(); iter.hasNext();) {
				OWLDataValue dv = (OWLDataValue) iter.next();
				values.add(factory.getOWLConcreteData(dv.getURI(), dv.getLang(), dv.getValue()));
			}
			result = factory.getOWLDataEnumeration(values);
		} else {
			throw new OWLException("Can't hand OWLDataRange of type "+dataRange.getClass());		
		}
		return result;
	}

	public void visit(OWLAnd node) throws OWLException {
		Set parts = new HashSet();
		for (Iterator iter = node.getOperands().iterator(); iter.hasNext(); ) {
			parts.add(copy((OWLDescription) iter.next()));
		}
		value =  factory.getOWLAnd(parts);
	}

	public void visit(OWLDataAllRestriction node) throws OWLException {
		OWLDataProperty dstProp = factory.getOWLDataProperty(node.getProperty().getURI());
		OWLDataRange srcDT = copyDataRange(node.getDataType());
		
		value = factory.getOWLDataAllRestriction(dstProp, copyDataRange(srcDT));
	}

	public void visit(OWLDataCardinalityRestriction node) throws OWLException {
		OWLDataProperty dstProp = factory.getOWLDataProperty(node.getProperty().getURI());
		int min = node.getAtLeast();
		int max = node.getAtMost();
		
		value = factory.getOWLDataCardinalityRestriction(dstProp, min, max);
	}

	public void visit(OWLDataSomeRestriction node) throws OWLException {
		OWLDataProperty dstProp = factory.getOWLDataProperty(node.getProperty().getURI());
		OWLDataRange dstRange = copyDataRange(node.getDataType());
		
		value = factory.getOWLDataSomeRestriction(dstProp, dstRange);
	}

	public void visit(OWLDataValueRestriction node) throws OWLException {
		OWLDataProperty dstProp = factory.getOWLDataProperty(node.getProperty().getURI());
		OWLDataValue srcValue = node.getValue();
		OWLDataValue dstValue = factory.getOWLConcreteData(srcValue.getURI(), srcValue.getLang(), srcValue.getValue());
		
		value = factory.getOWLDataValueRestriction(dstProp, dstValue);
	}

	public void visit(OWLFrame node) throws OWLException {
		Set superClasses = new HashSet();
		Set restrictions = new HashSet();
		
		for (Iterator iter = node.getSuperclasses().iterator(); iter.hasNext(); ) {
			superClasses.add(copy(source, destination, (OWLDescription) iter.next()));
		}
		for (Iterator iter = node.getRestrictions().iterator(); iter.hasNext(); ) {
			restrictions.add(copy(source, destination, (OWLRestriction) iter.next()));
		}
		value = factory.getOWLFrame(superClasses, restrictions);
	}

	public void visit(OWLObjectAllRestriction node) throws OWLException {
		OWLObjectProperty dstProp = factory.getOWLObjectProperty(node.getProperty().getURI());
		OWLDescription description = copy(node.getDescription());
		
		value = factory.getOWLObjectAllRestriction(dstProp, description);
	}

	public void visit(OWLObjectCardinalityRestriction node) throws OWLException {
		OWLObjectProperty dstProp = factory.getOWLObjectProperty(node.getProperty().getURI());
		int min = node.getAtLeast();
		int max = node.getAtMost();
		
		value = factory.getOWLObjectCardinalityRestriction(dstProp, min, max);
	}

	public void visit(OWLObjectSomeRestriction node) throws OWLException {
		OWLObjectProperty dstProp = factory.getOWLObjectProperty(node.getProperty().getURI());
		OWLDescription description = copy(node.getDescription());
		
		value = factory.getOWLObjectSomeRestriction(dstProp, description);
	}

	public void visit(OWLObjectValueRestriction node) throws OWLException {
		OWLObjectProperty dstProp = factory.getOWLObjectProperty(node.getProperty().getURI());
		OWLIndividual srcInd = node.getIndividual();
		OWLIndividual dstInd;
		if (srcInd.isAnonymous()) {
			dstInd = factory.getAnonOWLIndividual(srcInd.getAnonId());
		} else {
			dstInd = factory.getOWLIndividual(srcInd.getURI());
		}
		
		value = factory.getOWLObjectValueRestriction(dstProp, dstInd);
	}

	public void visit(OWLNot node) throws OWLException {
		value = factory.getOWLNot(copy(node.getOperand()));
	}

	public void visit(OWLOr node) throws OWLException {
		Set parts = new HashSet();
		for (Iterator iter = node.getOperands().iterator(); iter.hasNext(); ) {
			parts.add(copy(source, destination, (OWLDescription) iter.next()));
		}
		value =  factory.getOWLOr(parts);
	}

	public void visit(OWLClass node) throws OWLException {
		value = factory.getOWLClass(node.getURI());
	}

	public void visit(OWLEnumeration node) throws OWLException {
		Set parts = new HashSet();
		for (Iterator iter = node.getIndividuals().iterator(); iter.hasNext(); ) {
			OWLIndividual srcInd = (OWLIndividual) iter.next();
			OWLIndividual dstInd;
			if (srcInd.isAnonymous()) {
				dstInd = factory.getAnonOWLIndividual(srcInd.getAnonId());
			} else {
				dstInd = factory.getOWLIndividual(srcInd.getURI());
			}
			parts.add(dstInd);
		}
		value =  factory.getOWLEnumeration(parts);
	}

	public void visit(OWLDataProperty entity) throws OWLException {
		value = factory.getOWLDataProperty(entity.getURI());
	}

	public void visit(OWLObjectProperty entity) throws OWLException {
		value = factory.getOWLObjectProperty(entity.getURI());
	}

	public void visit(OWLAnnotationProperty entity) throws OWLException {
		value = factory.getOWLAnnotationProperty(entity.getURI());
	}

	public void visit(OWLIndividual entity) throws OWLException {
		if (entity.isAnonymous()) {
			value = factory.getAnonOWLIndividual(entity.getAnonId());
		} else {
			value = factory.getOWLIndividual(entity.getURI());
		}
	}


	

}
