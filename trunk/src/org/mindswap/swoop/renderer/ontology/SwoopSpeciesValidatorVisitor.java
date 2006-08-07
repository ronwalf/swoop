package org.mindswap.swoop.renderer.ontology;

/**
 * @author Dave
 *
 * Basically a copy of  uk.ac.man.cs.img.owl.validation.SpeciesValidatorVisitor.
 * This is needed because 1) uk.ac.man.cs.img.owl.validation.SpeciesValidatorVisitor is not 
 *    extendable outside of its package
 *                        2) SwoopSpeciesValidator requires a validatorVisitor
 * This class is fitted for SwoopSpeciesValidator.
 *
 */

import org.semanticweb.owl.model.helper.OWLObjectVisitorAdapter;
import org.semanticweb.owl.model.OWLDataPropertyInstance;
import org.semanticweb.owl.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owl.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owl.model.OWLDisjointClassesAxiom;
import org.semanticweb.owl.model.OWLFunctionalPropertyAxiom;
import org.semanticweb.owl.model.OWLIndividualTypeAssertion;
import org.semanticweb.owl.model.OWLInverseFunctionalPropertyAxiom;
import org.semanticweb.owl.model.OWLInversePropertyAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyInstance;
import org.semanticweb.owl.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owl.model.OWLPropertyDomainAxiom;
import org.semanticweb.owl.model.OWLSubClassAxiom;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLSymmetricPropertyAxiom;
import org.semanticweb.owl.model.OWLTransitivePropertyAxiom;

import java.util.Iterator;
import org.semanticweb.owl.model.OWLClass;
import org.apache.log4j.Logger;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.io.abstract_syntax.ObjectRenderer;
import org.semanticweb.owl.io.RendererException;
import org.semanticweb.owl.validation.OWLValidationConstants;

import uk.ac.man.cs.img.owl.validation.ClassOrRestrictionIdentifier;

public class SwoopSpeciesValidatorVisitor extends OWLObjectVisitorAdapter implements OWLValidationConstants
{

    private int level;

    static Logger logger = Logger.getLogger(SwoopSpeciesValidatorVisitor.class);
    
    private SwoopSpeciesValidator parent;

    private ObjectRenderer objectRenderer;

    public int getLevel() {
	return level;
    }

    public SwoopSpeciesValidatorVisitor( SwoopSpeciesValidator parent, 
				    ObjectRenderer renderer ) {
	this.level = SwoopSpeciesValidator.LITE;
	this.parent = parent;
	this.objectRenderer = renderer;
    }
    
    public void visit( OWLDisjointClassesAxiom node ) {
	/* If a Disjoint Classes Axiom is used, we must be in at
	 * least DL. */
	logger.debug("Visiting DisjointClassesAxiom");
	parent.explain( SwoopSpeciesValidator.DL,
			DISJOINT, 
			"Disjoint Classes axiom found: " + renderNode( node ) );
	level = level | SwoopSpeciesValidator.DL;
    }
    
    public void visit( OWLEquivalentClassesAxiom node ) {
	/* Depends on the format of the expressions. */
	logger.debug("Visiting EquivalentClassesAxiom");
	try {
	    for ( Iterator it = node.getEquivalentClasses().iterator();
		  it.hasNext(); ) {
		OWLObject oo = (OWLObject) it.next();
		logger.debug( "Object is: " + oo );
		if ( !isClassOrSimpleRestriction( oo ) ) {
		    parent.explain( SwoopSpeciesValidator.DL,
				    EXPRESSIONINAXIOM,
				    "Equivalent Classes axiom using expressions found: " + renderNode( node ));
		    
		    level = level | SwoopSpeciesValidator.DL;
		}
	    }
	} catch ( OWLException ex ) {
	    level = level | SwoopSpeciesValidator.OTHER;
	}
    }
    
    public void visit( OWLSubClassAxiom node ) {
	/* Depends on the format of the expressions. */
	try {
	    OWLDescription subClass = node.getSubClass();
	    
	    if ( !isClass( subClass ) ) {
		parent.explain( SwoopSpeciesValidator.DL,
				EXPRESSIONINAXIOM,
				"SubClass axiom using expressions found: " + renderNode( node ));
		
		level = level | SwoopSpeciesValidator.DL;
	    }
	    OWLDescription superClass = node.getSuperClass();
	    if ( !isClassOrSimpleRestriction( superClass ) ) {
		parent.explain( SwoopSpeciesValidator.DL,
				EXPRESSIONINAXIOM,
				"SubClass axiom using expressions found: " + renderNode( node ));
		
		level = level | SwoopSpeciesValidator.DL;
	    }
	} catch ( OWLException ex ) {
	    level = level | SwoopSpeciesValidator.OTHER;
	}
    }
    
    public void visit( OWLClass node ) {
	/* Checks all the various things that have been said about the
	 * class. */
	
    }
    
    /* Check if the description is a Class */
    private boolean isClass( OWLObject oo ) throws OWLException {
	ClassOrRestrictionIdentifier civ = 
	    new ClassOrRestrictionIdentifier();
	oo.accept( civ );
	return civ.isClass();
    }

    private boolean isClassOrSimpleRestriction( OWLObject oo ) throws OWLException {
	ClassOrRestrictionIdentifier civ = 
	    new ClassOrRestrictionIdentifier();
	oo.accept( civ );
	return civ.isClassOrSimpleRestriction();
    }

    private String renderNode( OWLObject node ) {
	try {
	    return objectRenderer.renderObject( node );
	} catch (RendererException ex) {
	    return node.toString();
	}
    }

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLFunctionalPropertyAxiom)
	 */
	public void visit(OWLFunctionalPropertyAxiom node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLInverseFunctionalPropertyAxiom)
	 */
	public void visit(OWLInverseFunctionalPropertyAxiom node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLTransitivePropertyAxiom)
	 */
	public void visit(OWLTransitivePropertyAxiom node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLSymmetricPropertyAxiom)
	 */
	public void visit(OWLSymmetricPropertyAxiom node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLInversePropertyAxiom)
	 */
	public void visit(OWLInversePropertyAxiom node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLPropertyDomainAxiom)
	 */
	public void visit(OWLPropertyDomainAxiom node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLObjectPropertyRangeAxiom)
	 */
	public void visit(OWLObjectPropertyRangeAxiom node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLDataPropertyRangeAxiom)
	 */
	public void visit(OWLDataPropertyRangeAxiom node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLObjectPropertyInstance)
	 */
	public void visit(OWLObjectPropertyInstance node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLDataPropertyInstance)
	 */
	public void visit(OWLDataPropertyInstance node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLIndividualTypeAssertion)
	 */
	public void visit(OWLIndividualTypeAssertion node) throws OWLException {
		// TODO Auto-generated method stub
		
	}
    
} // SpeciesValidatorVisitor
