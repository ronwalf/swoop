package org.mindswap.swoop.renderer.ontology;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.apache.log4j.Logger;

import org.semanticweb.owl.model.OWLAnd;
import org.semanticweb.owl.model.OWLDataCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataEnumeration;
import org.semanticweb.owl.model.OWLDataPropertyInstance;
import org.semanticweb.owl.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owl.model.OWLDataValueRestriction;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLEnumeration;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLFunctionalPropertyAxiom;
import org.semanticweb.owl.model.OWLIndividualTypeAssertion;
import org.semanticweb.owl.model.OWLInverseFunctionalPropertyAxiom;
import org.semanticweb.owl.model.OWLInversePropertyAxiom;
import org.semanticweb.owl.model.OWLNot;
import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLObjectAllRestriction;
import org.semanticweb.owl.model.OWLObjectCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectPropertyInstance;
import org.semanticweb.owl.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owl.model.OWLObjectSomeRestriction;
import org.semanticweb.owl.model.OWLObjectValueRestriction;
import org.semanticweb.owl.model.OWLOr;
import org.semanticweb.owl.model.OWLPropertyDomainAxiom;
import org.semanticweb.owl.model.OWLSymmetricPropertyAxiom;
import org.semanticweb.owl.model.OWLTransitivePropertyAxiom;
import org.semanticweb.owl.model.helper.OWLObjectVisitorAdapter;
import org.semanticweb.owl.validation.OWLValidationConstants;
import org.semanticweb.owl.io.RendererException;
import org.semanticweb.owl.io.abstract_syntax.ObjectRenderer;

import uk.ac.man.cs.img.owl.validation.ClassOrRestrictionIdentifier;

/**
 * @author Dave
 *
 * Basically a copy of  uk.ac.man.cs.img.owl.validation.ExpressionVisitor.
 * This is needed because 1) uk.ac.man.cs.img.owl.validation.ExpressionVisitor is not 
 *    extendable outside of its package
 *                        2) SwoopSpeciesValidatorVisitor requires a ExpressionVisitor
 * This class is fitted for SwoopSpeciesValidatorVisitor.
 * 
 */

public class SwoopExpressionValidatorVisitor extends OWLObjectVisitorAdapter implements OWLValidationConstants
{

    private int level;
    /* A flag that indicates whether we're at the top level of an
    equivalence description. In this particular case, it *is* ok to
    have an intersection */ 
    private boolean topLevelDescription = false;

    private Set complexProperties;
    
    static Logger logger = Logger.getLogger(SwoopExpressionValidatorVisitor.class);
    
    private SwoopSpeciesValidator parent;

    private ObjectRenderer objectRenderer;

    public int getLevel() {
	return level;
    }

    public Set getComplexProperties() {
	return complexProperties;
    }

    public void setTopLevelDescription( boolean b ) {
	topLevelDescription = b;
    }
    
    public SwoopExpressionValidatorVisitor( SwoopSpeciesValidator parent, 
				       ObjectRenderer renderer) {
	this.level = LITE;
	this.parent = parent;
	this.objectRenderer = renderer;
	this.complexProperties = new HashSet();
    }
    
    public void reset() {
	this.level = LITE;
	this.topLevelDescription = false;
    }
    
    /* If it's one of: 
     * or
     * not
     * oneof
     * individualValueRestriction
     * cardinality with anything other than 0, 1 
     * then we're in DL. */
    
    public void visit( OWLOr node ) {
	explain( DL,
		 UNION,
		 "Or: " + renderNode( node ) );
	level = level | DL;
    }
    public void visit( OWLNot node ) {
	explain( DL,
		 COMPLEMENT,
		 "Not: " + renderNode( node ) );
	level = level | DL;
    }
    public void visit( OWLEnumeration node ) {
	explain( DL,
		 ONEOF,
		 "Enumeration: " + renderNode( node ) );
	level = level | DL;
    }
    public void visit( OWLDataEnumeration node ) {
	explain( DL,
		 ONEOF, /* DATARANGE?? */
		 "Data Enumeration: " + renderNode( node ) );
	level = level | DL;
    }
    public void visit( OWLObjectValueRestriction node ) {
	explain( DL,
		 ONEOF,
		 "Individual Value: " + renderNode( node ) );
	level = level | DL;
    }
    public void visit( OWLObjectCardinalityRestriction node ) throws OWLException {
	/* The property is complex. */
	complexProperties.add( node.getProperty() );

	if ( (node.isAtLeast() && node.getAtLeast() > 1) ||
	     (node.isAtMost() && node.getAtMost() > 1) ) {
	    explain( DL,
		     CARDINALITY,
		     "Cardinality with > 1: " + renderNode( node ) );
	    level = level | DL;
	}
    }
    public void visit( OWLDataCardinalityRestriction node ) throws OWLException {
	if ( (node.isAtLeast() && node.getAtLeast() > 1) ||
	     (node.isAtMost() && node.getAtMost() > 1) ) {
	    explain( DL,
		     CARDINALITY,
		     "Cardinality with > 1: " + renderNode( node ) );
	    level = level | DL;
	}
    }
    public void visit( OWLDataValueRestriction node ) {
	explain( DL,
		 ONEOF,
		 "Data Value: " + renderNode( node ) );
	level = level | DL;
    }

    
    /* It it's an and, and we're at a top level equivalence, we need
     * to check that the operands are all either classes or
     * restrictions.  If they are restrictions, we then need to check
     * that the restrictions are themselves ok. */

    /* This is very unpleasant, and would be the kind of situation
     * where OWLFrame would be useful as it would allow intersection
     * at the top level....*/
    public void visit ( OWLAnd node ) throws OWLException {
	if ( topLevelDescription ) {
	    topLevelDescription = false;
	    ClassOrRestrictionIdentifier cori = 
		new ClassOrRestrictionIdentifier();
	    for ( Iterator it = node.getOperands().iterator();
		  it.hasNext(); ) {
		OWLDescription description = (OWLDescription) it.next();
		cori.reset();
		description.accept( cori );
		if ( !cori.isClassOrRestriction() ) {
		    /* If it's not a class or restriction, we're DL */
		    explain( DL,
			     INTERSECTION,
			     "And with non-class or restriction: " + renderNode( node ) );
		    level = level | DL;
		} else {
		    /* Now need to check the expression itself too. */
		    if ( cori.isRestriction() ) {
			/* Recurse down and check the restriction */
			description.accept( this );
		    }
		}
	    }
	} else {
	    explain( DL,
		     INTERSECTION,
		     "And: " + renderNode( node ) );
	    level = level | DL;
	}
    }

    /* If it's an object restriction, then if the filler is anything
     * other than a class, we're in at least DL. */

    public void visit ( OWLObjectSomeRestriction node ) throws OWLException {
 	topLevelDescription = false;
	SwoopClassIdentifierVisitor civ = new SwoopClassIdentifierVisitor();
	node.getDescription().accept( civ );
	if ( !civ.isClass() ) {
	    explain( DL, EXPRESSIONINRESTRICTION, "Object restriction with non classID filler: " + renderNode( node ) );
	    level = level | DL;
	}
	node.getDescription().accept( this );
    }

    public void visit ( OWLObjectAllRestriction node ) throws OWLException {
	topLevelDescription = false;
	SwoopClassIdentifierVisitor civ = new SwoopClassIdentifierVisitor();
	node.getDescription().accept( civ );
	if ( !civ.isClass() ) {
	    explain( DL, EXPRESSIONINRESTRICTION, "Object restriction with non classID filler: " + renderNode( node ) );
	    level = level | DL;
	}
	node.getDescription().accept( this );
    }

    private String renderNode( OWLObject node ) {
	try {
	    if ( objectRenderer!=null ) {
		return objectRenderer.renderObject( node );
	    }
	} catch (RendererException ex) {
	}
	return node.toString();
    }

    private void explain( int level, 
			  int code, 
			  String str ) {
	if ( parent!=null ) {
	    parent.explain( level, code, str );
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
    
} // ExpressionValidatorVisitor

