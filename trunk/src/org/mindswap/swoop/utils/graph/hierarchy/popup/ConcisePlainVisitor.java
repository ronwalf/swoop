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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.renderer.BaseEntityRenderer;
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


public class ConcisePlainVisitor extends OWLObjectVisitorAdapter implements SwoopRenderingVisitor
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
	StringWriter sw;
	PrintWriter pw;
	String imageURI = "";
	SwoopModel swoopModel;
	
	public ConcisePlainVisitor( ShortFormProvider shortForms, SwoopModel swoopModel )
	{
		this.shortForms = shortForms;
		this.swoopModel = swoopModel;
		reset();
	}
	
	public String result() {
		return sw.toString();
	}
	
	/* Replace " with \" and \ with \\ */
	private static String escape(Object o) {
		/* Should probably use regular expressions */
		StringBuffer sw = new StringBuffer();
		String str = o.toString();
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (c != '"' && c != '\\') {
				sw.append(c);
			} else {
				sw.append('\\');
				sw.append(c);
			}
		}
		return sw.toString();
	}



	public void reset() {
		sw = new StringWriter();
		pw = new PrintWriter( sw );
	}
		
	public void visit( OWLClass clazz ) throws OWLException {
		pw.print( shortForms.shortForm( clazz.getURI() ) );
	}
	
	public void visit( OWLIndividual ind ) throws OWLException 
	{
		if ( ind.isAnonymous() ) 
			pw.print(   ind.getAnonId().getFragment()  );
		else 
			pw.print(  shortForms.shortForm( ind.getURI() )  );
	}
	
	
	public void visit( OWLObjectProperty prop ) throws OWLException 
	{
		pw.print( shortForms.shortForm( prop.getURI() )  );
	}
	
	public void visit( OWLAnnotationProperty prop ) throws OWLException 
	{
		pw.print( shortForms.shortForm( prop.getURI() ) );
	}
	
	public void visit( OWLDataProperty prop ) throws OWLException 
	{
		pw.print(  shortForms.shortForm( prop.getURI() )  );
	}
	
	public void visit( OWLDataValue cd ) throws OWLException 
	{	
		pw.print( "\"" + escape( cd.getValue() ) + "\"");
		/* Only show it if it's not string */
		URI dvdt = cd.getURI();
		String dvlang = cd.getLang();
		if ( dvdt!=null) {
			pw.print( "^^" + "<" + shortForms.shortForm(dvdt) + ">");
		} 
		else 
		{
			if (dvlang!=null) {
				pw.print( "@" + dvlang );
			}
		}
	}

	public void visit( OWLAnd and ) throws OWLException {
		pw.print("(");
		for ( Iterator it = and.getOperands().iterator(); it.hasNext(); ) 
		{
			OWLDescription desc = (OWLDescription) it.next();
			desc.accept( this );
			if (it.hasNext()) {
				pw.print(" " + ConcisePlainVisitor.INTERSECTION + " ");
			}
		}
		pw.print(")");
	}

	public void visit( OWLOr or ) throws OWLException {
		pw.print("(");
		for ( Iterator it = or.getOperands().iterator(); it.hasNext(); ) 
		{
			OWLDescription desc = (OWLDescription) it.next();
			desc.accept( this );
			if (it.hasNext()) 
			{
				pw.print(" " + ConcisePlainVisitor.UNION + " ");
			}
		}
		pw.print(")");
	}

	public void visit( OWLNot not ) throws OWLException {
		pw.print("(" + ConcisePlainVisitor.NOT);
		OWLDescription desc = not.getOperand();
		desc.accept( this );
		pw.print(")");
	}

	public void visit( OWLEnumeration enumeration ) throws OWLException {
		pw.print("{");
		for ( Iterator it = enumeration.getIndividuals().iterator(); it.hasNext(); ) 
		{
			OWLIndividual desc = (OWLIndividual) it.next();
			desc.accept( this );
			if (it.hasNext()) 
				pw.print(", ");
		}
		pw.print("}");
	}

	public void visit( OWLObjectSomeRestriction restriction ) throws OWLException {
		pw.print("(" + ConcisePlainVisitor.EXISTS);
		restriction.getObjectProperty().accept( this );
		pw.print(" "+ ConcisePlainVisitor.MEMBEROF + " ");
		restriction.getDescription().accept( this );
		pw.print(")");
	}

	public void visit( OWLObjectAllRestriction restriction ) throws OWLException {
		pw.print("(" + ConcisePlainVisitor.FORALL);
		restriction.getObjectProperty().accept( this );
		pw.print(" "+ ConcisePlainVisitor.MEMBEROF + " ");
		restriction.getDescription().accept( this );
		pw.print(")");
	}

	public void visit( OWLObjectValueRestriction restriction ) throws OWLException {
		pw.print("(" + ConcisePlainVisitor.EXISTS);
		restriction.getObjectProperty().accept( this );
		/* Changed from hasValue */
		pw.print(" "+ ConcisePlainVisitor.MEMBEROF + " {");
		restriction.getIndividual().accept( this );
		pw.print("})");
	}

	public void visit( OWLDataSomeRestriction restriction ) throws OWLException {
		pw.print("(" + ConcisePlainVisitor.EXISTS);
		restriction.getDataProperty().accept( this );
		pw.print(" "+ ConcisePlainVisitor.MEMBEROF + " ");
		restriction.getDataType().accept( this );
		pw.print(")");
	}

	public void visit( OWLDataAllRestriction restriction ) throws OWLException {
		pw.print("(" + ConcisePlainVisitor.FORALL);
		restriction.getDataProperty().accept( this );
		pw.print(" "+ ConcisePlainVisitor.MEMBEROF + " ");
		restriction.getDataType().accept( this );
		pw.print(")");
	}

	public void visit( OWLObjectCardinalityRestriction restriction ) throws OWLException 
	{
		pw.print("(");
		if ( restriction.isExactly() ) {
			pw.print(ConcisePlainVisitor.EQU + " " + restriction.getAtLeast() + " ");
		} else if ( restriction.isAtMost() ) {
			pw.print(ConcisePlainVisitor.LESSEQU + " " + restriction.getAtMost() + " ");
		} else 	if ( restriction.isAtLeast() ) {
			pw.print(ConcisePlainVisitor.GREATEQU + " " + restriction.getAtLeast() + " ");
		} 
		restriction.getObjectProperty().accept( this );
		pw.print(")");
	}

	public void visit( OWLDataCardinalityRestriction restriction ) throws OWLException 
	{
		pw.print("(");
		if ( restriction.isExactly() ) {
			pw.print(ConcisePlainVisitor.EQU + " " + restriction.getAtLeast() + " ");
		} else if ( restriction.isAtMost() ) {
			pw.print(ConcisePlainVisitor.LESSEQU + " " + restriction.getAtMost() + " ");
		} else 	if ( restriction.isAtLeast() ) {
			pw.print(ConcisePlainVisitor.GREATEQU + " " + restriction.getAtLeast() + " ");
		} 
		restriction.getDataProperty().accept( this );
		pw.print(")");
	}

	public void visit( OWLDataValueRestriction restriction ) throws OWLException {
		pw.print("(" + ConcisePlainVisitor.EXISTS);
		restriction.getDataProperty().accept( this );
		/* Changed from hasValue */
		pw.print(" "+ ConcisePlainVisitor.MEMBEROF + " {");
		restriction.getValue().accept( this );
		pw.print("})");
	}

	public void visit( OWLEquivalentClassesAxiom axiom ) throws OWLException {

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
		
		pw.print("(");
		for ( Iterator it = atomic.iterator(); it.hasNext(); ) 
		{
			OWLDescription desc = (OWLDescription) it.next();
			desc.accept( this );
			if (it.hasNext() || complex.size()>0)
				pw.print( " " + EQUIVALENTTO + " " );
		}
		
		for ( Iterator it = complex.iterator(); it.hasNext(); ) 
		{
			OWLDescription desc = (OWLDescription) it.next();
			desc.accept( this );
			if (it.hasNext())
				pw.print( " " + EQUIVALENTTO + " " );
		}
		pw.print(")");
	}

	public void visit( OWLDisjointClassesAxiom axiom ) throws OWLException 
	{
		pw.print("(");
		for ( Iterator it = axiom.getDisjointClasses().iterator(); it.hasNext(); ) 
		{
			OWLDescription desc = (OWLDescription) it.next();
			desc.accept( this );
			if (it.hasNext()) 
				pw.print(" " + ConcisePlainVisitor.SUBCLASSOF + " " + ConcisePlainVisitor.NOT);
		}
		pw.print(")");
	}

	public void visit( OWLSubClassAxiom axiom ) throws OWLException 
	{
		pw.print("(");
		axiom.getSubClass().accept( this );
		pw.print(" " + ConcisePlainVisitor.SUBCLASSOF + " ");
		axiom.getSuperClass().accept( this );
		pw.print(")");
	}

	public void visit( OWLEquivalentPropertiesAxiom axiom ) throws OWLException 
	{
		pw.print("(");
		for ( Iterator it = axiom.getProperties().iterator(); it.hasNext(); ) 
		{
			OWLProperty prop = (OWLProperty) it.next();
			prop.accept( this );
			if (it.hasNext())
				pw.print(" = ");
		}
		pw.print(")");
	}

	public void visit( OWLSubPropertyAxiom axiom ) throws OWLException {
		pw.print("(");
		axiom.getSubProperty().accept( this );
		pw.print(" " + ConcisePlainVisitor.SUBCLASSOF + " ");
		axiom.getSuperProperty().accept( this );
		pw.print(")");
	}

	public void visit( OWLDifferentIndividualsAxiom ax) throws OWLException 
	{
		pw.print("(");
		for ( Iterator it = ax.getIndividuals().iterator(); it.hasNext(); ) 
		{
			OWLIndividual desc = (OWLIndividual) it.next();
			desc.accept( this );
			if (it.hasNext())
				pw.print(" " + ConcisePlainVisitor.DISJOINT + " ");
		}
		pw.print(")");
	}

	public void visit( OWLSameIndividualsAxiom ax) throws OWLException {
		pw.print("SameIndividual(");
		for ( Iterator it = ax.getIndividuals().iterator(); it.hasNext(); ) 
		{	
			OWLIndividual desc = (OWLIndividual) it.next();
			desc.accept( this );
			if (it.hasNext())
				pw.print(" = ");
		}
		pw.print(")");
	}

	public void visit( OWLDataType ocdt ) throws OWLException {
		pw.print( shortForms.shortForm( ocdt.getURI() ) );		
	}

	public void visit( OWLDataEnumeration enumeration ) throws OWLException {
		pw.print("{");
		for ( Iterator it = enumeration.getValues().iterator(); it.hasNext(); ) 
		{
			OWLDataValue desc = (OWLDataValue) it.next();
			desc.accept( this );
			if (it.hasNext())
				pw.print(", ");
		}
		pw.print("}");
	}
	

//	Uncomment for explanation
	
	public void visit( OWLFunctionalPropertyAxiom axiom ) throws OWLException {
		pw.print("Functional Property (");
		axiom.getProperty().accept( this );
		pw.print(")");
	}
	
	public void visit( OWLPropertyDomainAxiom axiom ) throws OWLException {
		pw.print("(");
		axiom.getProperty().accept( this );
		pw.print(" domain ");
		axiom.getDomain().accept( this );
		pw.print(")");
	}
	
	public void visit( OWLObjectPropertyRangeAxiom axiom ) throws OWLException {
		pw.print("(");
		axiom.getProperty().accept( this );
		pw.print(" range ");
		axiom.getRange().accept( this );
		pw.print(")");
	}
	
	public void visit( OWLDataPropertyRangeAxiom axiom ) throws OWLException {
		pw.print("(");
		axiom.getProperty().accept( this );
		pw.print(" range ");
		axiom.getRange().accept( this );
		pw.print(")");
	}
	
    /* (non-Javadoc)
     * @see org.mindswap.pellet.owlapi.OWLExtendedObjectVisitor#visit(org.mindswap.pellet.owlapi.OWLInverseFunctionalPropertyAxiom)
     *FIXME: Fix this!
     */
    public void visit(OWLInverseFunctionalPropertyAxiom axiom) throws OWLException {
    	if (axiom.getProperty().isInverseFunctional(axiom.getProperty().getOntologies())) {
	    	pw.print("InverseFunctional(");
			axiom.getProperty().accept( this );
			pw.print(")");
    	}
    	if (axiom instanceof OWLInversePropertyAxiomImpl) {
    		OWLInversePropertyAxiomImpl invAxiom = (OWLInversePropertyAxiomImpl) axiom;
    		pw.print("(");
    		invAxiom.getProperty().accept( this );
    		pw.print(" inverse ");
    		invAxiom.getInverseProperty().accept( this );
    		pw.print(")");	
    	}
    }

    /* (non-Javadoc)
     * @see org.mindswap.pellet.owlapi.OWLExtendedObjectVisitor#visit(org.mindswap.pellet.owlapi.OWLTransitivePropertyAxiom)
     */
    public void visit(OWLTransitivePropertyAxiom axiom) throws OWLException {
		pw.print("Transitive(");
		axiom.getProperty().accept( this );
		pw.print(")");    }

    /* (non-Javadoc)
     * @see org.mindswap.pellet.owlapi.OWLExtendedObjectVisitor#visit(org.mindswap.pellet.owlapi.OWLSymmetricPropertyAxiom)
     */
    public void visit(OWLSymmetricPropertyAxiom axiom) throws OWLException {
		pw.print("Symmetric(");
		axiom.getProperty().accept( this );
		pw.print(")");    }

    /* (non-Javadoc)
     * @see org.mindswap.pellet.owlapi.OWLExtendedObjectVisitor#visit(org.mindswap.pellet.owlapi.OWLInversePropertyAxiom)
     */
    public void visit(OWLInversePropertyAxiom axiom) throws OWLException {
		pw.print("(");
		axiom.getProperty().accept( this );
		pw.print(" inverse ");
		axiom.getInverseProperty().accept( this );
		pw.print(")");
    }

    /* (non-Javadoc)
     * @see org.mindswap.pellet.owlapi.OWLExtendedObjectVisitor#visit(org.mindswap.pellet.owlapi.OWLObjectPropertyInstance)
     */
    public void visit(OWLObjectPropertyInstance axiom) throws OWLException {
		pw.print("(");
		axiom.getSubject().accept( this );
		pw.print(" ");
		axiom.getProperty().accept( this );
		pw.print(" ");
		axiom.getObject().accept( this );
		pw.print(")");
    }

    /* (non-Javadoc)
     * @see org.mindswap.pellet.owlapi.OWLExtendedObjectVisitor#visit(org.mindswap.pellet.owlapi.OWLDataPropertyInstance)
     */
    public void visit(OWLDataPropertyInstance axiom) throws OWLException {
		pw.print("(");
		axiom.getSubject().accept( this );
		pw.print(" ");
		axiom.getProperty().accept( this );
		pw.print(" ");
		axiom.getObject().accept( this );
		pw.print(")");
    }

    /* (non-Javadoc)
     * @see org.mindswap.pellet.owlapi.OWLExtendedObjectVisitor#visit(org.mindswap.pellet.owlapi.OWLIndividualClassAxiom)
     */
    public void visit(OWLIndividualTypeAssertion axiom) throws OWLException {
		pw.print("(");
		axiom.getIndividual().accept( this );
		pw.print(" rdf:type ");
		axiom.getType().accept( this );
		pw.print(")");
    }   
}