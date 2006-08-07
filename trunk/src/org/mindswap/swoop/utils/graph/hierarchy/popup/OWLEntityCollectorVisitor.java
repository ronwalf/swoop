/*
GNU Lesser General Public License

ConciseFormatVisitor.java
Copyright (C) 2005 MINDSWAP Research Group, University of Maryland College Park

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package org.mindswap.swoop.utils.graph.hierarchy.popup;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.renderer.SwoopRenderingVisitor;
import org.semanticweb.owl.impl.model.OWLInversePropertyAxiomImpl;
import org.semanticweb.owl.io.ShortFormProvider;
import org.semanticweb.owl.model.OWLAnd;
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
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owl.model.OWLDisjointClassesAxiom;
import org.semanticweb.owl.model.OWLEnumeration;
import org.semanticweb.owl.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owl.model.OWLEquivalentPropertiesAxiom;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLFunctionalPropertyAxiom;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLIndividualTypeAssertion;
import org.semanticweb.owl.model.OWLInverseFunctionalPropertyAxiom;
import org.semanticweb.owl.model.OWLInversePropertyAxiom;
import org.semanticweb.owl.model.OWLNot;
import org.semanticweb.owl.model.OWLObjectAllRestriction;
import org.semanticweb.owl.model.OWLObjectCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectPropertyInstance;
import org.semanticweb.owl.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owl.model.OWLObjectSomeRestriction;
import org.semanticweb.owl.model.OWLObjectValueRestriction;
import org.semanticweb.owl.model.OWLOr;
import org.semanticweb.owl.model.OWLProperty;
import org.semanticweb.owl.model.OWLPropertyDomainAxiom;
import org.semanticweb.owl.model.OWLSameIndividualsAxiom;
import org.semanticweb.owl.model.OWLSubClassAxiom;
import org.semanticweb.owl.model.OWLSubPropertyAxiom;
import org.semanticweb.owl.model.OWLSymmetricPropertyAxiom;
import org.semanticweb.owl.model.OWLTransitivePropertyAxiom;
import org.semanticweb.owl.model.helper.OWLObjectVisitorAdapter;


public class OWLEntityCollectorVisitor extends OWLObjectVisitorAdapter implements SwoopRenderingVisitor
//	Uncomment for explanation
// ,OWLExtendedObjectVisitor
{
    	
	public static final String FORALL     = "\u2200";  // all restriction
	public static final String EXISTS     = "\u2203";  // some restriction
	public static final String MEMBEROF   = ".";
	public static final String EQU        = "=";
	public static final String GREATEQU   = "\u2265";
	public static final String LESSEQU    = "\u2264";
	public static final String SUBCLASSOF   = "\u2286";   // subset
	public static final String DISJOINT     = "\u2260";
	public static final String EQUIVALENTTO = "\u2261";  // identical
	
	public static final String INTERSECTION = "\u2293";  // AND
	public static final String UNION        = "\u2294";  // OR
	public static final String NOT          = "\u00ac";  // NOT
	
	public static final String ISA          = "a"; 
		
	ShortFormProvider shortForms; 

	String imageURI = "";
	SwoopModel swoopModel;
	
	OWLEntityStorage myStorage = new OWLEntityStorage(500);
	
	public OWLEntityCollectorVisitor( ShortFormProvider shortForms, SwoopModel swoopModel )
	{
		this.shortForms = shortForms;
		this.swoopModel = swoopModel;
		reset();
	}
	
	public String result() { return null; }
	
	public OWLEntityStorage getStoredResult()
	{ return myStorage; }
	
	public void reset() 
	{
		myStorage.clear();
	}
		
	public void visit( OWLClass clazz ) throws OWLException {
		String icon = "";
		myStorage.put( clazz, OWLEntityStorage.OWLCLASS );
	}
	
	public void visit( OWLIndividual ind ) throws OWLException 
	{
		myStorage.put( ind, OWLEntityStorage.OWLINDIVIDUAL );
	}
	
	
	public void visit( OWLObjectProperty prop ) throws OWLException 
	{
		myStorage.put( prop, OWLEntityStorage.OWLOBJECTPROPERTY );
	}
	
	public void visit( OWLAnnotationProperty prop ) throws OWLException 
	{
		myStorage.put( prop, OWLEntityStorage.OWLANNOTATIONPROPERTY );
	}
	
	public void visit( OWLDataProperty prop ) throws OWLException 
	{
		myStorage.put( prop, OWLEntityStorage.OWLDATAPROPERTY );
	}
	
	public void visit( OWLDataValue cd ) throws OWLException 
	{ /* do nothing*/ }

	public void visit( OWLAnd and ) throws OWLException 
	{
		for ( Iterator it = and.getOperands().iterator(); it.hasNext(); ) 
		{
			OWLDescription desc = (OWLDescription) it.next();
			desc.accept( this );
		}
	}

	public void visit( OWLOr or ) throws OWLException 
	{
		for ( Iterator it = or.getOperands().iterator(); it.hasNext(); ) 
		{
			OWLDescription desc = (OWLDescription) it.next();
			desc.accept( this );
		}
	}

	public void visit( OWLNot not ) throws OWLException 
	{
		OWLDescription desc = not.getOperand();
		desc.accept( this );
	}

	public void visit( OWLEnumeration enumeration ) throws OWLException 
	{
		for ( Iterator it = enumeration.getIndividuals().iterator(); it.hasNext(); ) 
		{
			OWLIndividual desc = (OWLIndividual) it.next();
			desc.accept( this );
		}
	}

	public void visit( OWLObjectSomeRestriction restriction ) throws OWLException 
	{
		restriction.getObjectProperty().accept( this );
		restriction.getDescription().accept( this );
	}

	public void visit( OWLObjectAllRestriction restriction ) throws OWLException 
	{
		restriction.getObjectProperty().accept( this );
		restriction.getDescription().accept( this );
	}

	public void visit( OWLObjectValueRestriction restriction ) throws OWLException 
	{
		restriction.getObjectProperty().accept( this );
		restriction.getIndividual().accept( this );
	}

	public void visit( OWLDataSomeRestriction restriction ) throws OWLException 
	{
		restriction.getDataProperty().accept( this );
		restriction.getDataType().accept( this );
	}

	public void visit( OWLDataAllRestriction restriction ) throws OWLException 
	{		
		restriction.getDataProperty().accept( this );
		restriction.getDataType().accept( this );
	}

	public void visit( OWLObjectCardinalityRestriction restriction ) throws OWLException 
	{
		restriction.getObjectProperty().accept( this );
	}

	public void visit( OWLDataCardinalityRestriction restriction ) throws OWLException 
	{
		restriction.getDataProperty().accept( this );
	}

	public void visit( OWLDataValueRestriction restriction ) throws OWLException 
	{
		restriction.getDataProperty().accept( this );
		restriction.getValue().accept( this );
	}

	public void visit( OWLEquivalentClassesAxiom axiom ) throws OWLException 
	{
		// sort axiom classes so that atomic appears on the LHS
		Set equClas = axiom.getEquivalentClasses();
		Set atomic = new HashSet();
		Set complex = new HashSet();
		for ( Iterator it = equClas.iterator(); it.hasNext(); ) 
		{
			OWLDescription desc = (OWLDescription) it.next();
			if (desc instanceof OWLClass) atomic.add(desc);			
			else complex.add(desc);
		}
		
		for ( Iterator it = atomic.iterator(); it.hasNext(); ) 
		{
			OWLDescription desc = (OWLDescription) it.next();
			desc.accept( this );
		}
		
		for ( Iterator it = complex.iterator(); it.hasNext(); ) 
		{
			OWLDescription desc = (OWLDescription) it.next();
			desc.accept( this );
		}

	}

	public void visit( OWLDisjointClassesAxiom axiom ) throws OWLException 
	{
		for ( Iterator it = axiom.getDisjointClasses().iterator(); it.hasNext(); ) 
		{
			OWLDescription desc = (OWLDescription) it.next();
			desc.accept( this );
		}
	}

	public void visit( OWLSubClassAxiom axiom ) throws OWLException 
	{
		axiom.getSubClass().accept( this );
		axiom.getSuperClass().accept( this );
	}

	public void visit( OWLEquivalentPropertiesAxiom axiom ) throws OWLException 
	{
		for ( Iterator it = axiom.getProperties().iterator(); it.hasNext(); ) 
		{
			OWLProperty prop = (OWLProperty) it.next();
			prop.accept( this );
		}
	}

	public void visit( OWLSubPropertyAxiom axiom ) throws OWLException 
	{
		axiom.getSubProperty().accept( this );
		axiom.getSuperProperty().accept( this );
	}

	public void visit( OWLDifferentIndividualsAxiom ax) throws OWLException 
	{
		for ( Iterator it = ax.getIndividuals().iterator(); it.hasNext(); ) 
		{
			OWLIndividual desc = (OWLIndividual) it.next();
			desc.accept( this );
		}
	}

	public void visit( OWLSameIndividualsAxiom ax) throws OWLException 
	{
		for ( Iterator it = ax.getIndividuals().iterator(); it.hasNext(); ) 
		{	
			OWLIndividual desc = (OWLIndividual) it.next();
			desc.accept( this );
		}
	}

	public void visit( OWLDataType ocdt ) throws OWLException 
	{ /* do nothing for OWLDataType -- is this correct?? */}

	public void visit( OWLDataEnumeration enumeration ) throws OWLException 
	{
		for ( Iterator it = enumeration.getValues().iterator(); it.hasNext(); ) 
		{
			OWLDataValue desc = (OWLDataValue) it.next();
			desc.accept( this );
		}
	}
	

//	Uncomment for explanation
	
	public void visit( OWLFunctionalPropertyAxiom axiom ) throws OWLException 
	{
		axiom.getProperty().accept( this );
	}
	
	public void visit( OWLPropertyDomainAxiom axiom ) throws OWLException 
	{
		axiom.getProperty().accept( this );
		axiom.getDomain().accept( this );
	}
	
	public void visit( OWLObjectPropertyRangeAxiom axiom ) throws OWLException 
	{
		axiom.getProperty().accept( this );
		axiom.getRange().accept( this );
	}
	
	public void visit( OWLDataPropertyRangeAxiom axiom ) throws OWLException 
	{
		axiom.getProperty().accept( this );
		axiom.getRange().accept( this );
	}
	
    /* (non-Javadoc)
     * @see org.mindswap.pellet.owlapi.OWLExtendedObjectVisitor#visit(org.mindswap.pellet.owlapi.OWLInverseFunctionalPropertyAxiom)
     *FIXME: Fix this!
     */
    public void visit(OWLInverseFunctionalPropertyAxiom axiom) throws OWLException 
	{
    	if (axiom.getProperty().isInverseFunctional(axiom.getProperty().getOntologies()))     	
			axiom.getProperty().accept( this );
    	
    	if (axiom instanceof OWLInversePropertyAxiomImpl) 
    	{
    		OWLInversePropertyAxiomImpl invAxiom = (OWLInversePropertyAxiomImpl) axiom;
    		invAxiom.getProperty().accept( this );
    		invAxiom.getInverseProperty().accept( this );	
    	}
    }

    /* (non-Javadoc)
     * @see org.mindswap.pellet.owlapi.OWLExtendedObjectVisitor#visit(org.mindswap.pellet.owlapi.OWLTransitivePropertyAxiom)
     */
    public void visit(OWLTransitivePropertyAxiom axiom) throws OWLException 
	{
		axiom.getProperty().accept( this );
	}
    
    /* (non-Javadoc)
     * @see org.mindswap.pellet.owlapi.OWLExtendedObjectVisitor#visit(org.mindswap.pellet.owlapi.OWLSymmetricPropertyAxiom)
     */
    public void visit(OWLSymmetricPropertyAxiom axiom) throws OWLException 
	{
		axiom.getProperty().accept( this );
    }

    /* (non-Javadoc)
     * @see org.mindswap.pellet.owlapi.OWLExtendedObjectVisitor#visit(org.mindswap.pellet.owlapi.OWLInversePropertyAxiom)
     */
    public void visit(OWLInversePropertyAxiom axiom) throws OWLException 
	{
		axiom.getProperty().accept( this );
		axiom.getInverseProperty().accept( this );
    }

    /* (non-Javadoc)
     * @see org.mindswap.pellet.owlapi.OWLExtendedObjectVisitor#visit(org.mindswap.pellet.owlapi.OWLObjectPropertyInstance)
     */
    public void visit(OWLObjectPropertyInstance axiom) throws OWLException 
	{
		axiom.getSubject().accept( this );
		axiom.getProperty().accept( this );
		axiom.getObject().accept( this );
    }

    /* (non-Javadoc)
     * @see org.mindswap.pellet.owlapi.OWLExtendedObjectVisitor#visit(org.mindswap.pellet.owlapi.OWLDataPropertyInstance)
     */
    public void visit(OWLDataPropertyInstance axiom) throws OWLException 
	{
		axiom.getSubject().accept( this );
		axiom.getProperty().accept( this );
		axiom.getObject().accept( this );
    }

    /* (non-Javadoc)
     * @see org.mindswap.pellet.owlapi.OWLExtendedObjectVisitor#visit(org.mindswap.pellet.owlapi.OWLIndividualClassAxiom)
     */
    public void visit(OWLIndividualTypeAssertion axiom) throws OWLException 
	{
		axiom.getIndividual().accept( this );
		axiom.getType().accept( this );
    }
    
}
