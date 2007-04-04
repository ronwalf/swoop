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

package org.mindswap.swoop.explore;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.renderer.BaseEntityRenderer;
import org.mindswap.swoop.renderer.SwoopRenderingVisitor;
import org.mindswap.swoop.utils.graph.hierarchy.popup.ConcisePlainVisitor;
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
import org.semanticweb.owl.model.OWLDataRange;
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


public class AxiomContentExtractor extends OWLObjectVisitorAdapter implements SwoopRenderingVisitor
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
		
	private ShortFormProvider shortForms; 	
	
	HashedCounts classExpCounts;  // expression to number of times used
	int maxDepth;
	int currDepth;
	
	// map of class expression to a Vector of direct conjuncts/disjuncts it involves in
	HashedVectors conjunctionMap;
	HashedVectors disjunctionMap;
	HashedVectors negationMap;
	
	// constructor maps
	HashedVectors existentialMap;  // property to vector of class exp
	HashedVectors universalMap;    // property to vector of class exp
	HashedVectors minCardMap;      // property to vector of numbers
	HashedVectors maxCardMap;      // property to vector of numbers
	HashedVectors cardMap;         // property to vector of numbers
	
	// nominals
	HashedVectors enumerationMap;  // each individual to OWLEnumeration
	HashedVectors hasValueMap;     // property to vector of individuals (that the property has values with)
	
	// datavalues
	HashedVectors dhasValueMap;    // property to vector of datavalues
	
	// depth
	HashedCounts classExpDepths;
	HashSet depthCountingClassExp;
	
	ConcisePlainVisitor myVisitor;
	
	public AxiomContentExtractor( ConcisePlainVisitor visitor )
	{
		myVisitor = visitor;
		reset();
	}
	
	public String result() 
	{ return ""; }
	
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


	public void reset() 
	{
		this.classExpCounts = new HashedCounts();
		this.conjunctionMap = new HashedVectors();
		this.disjunctionMap = new HashedVectors();
		this.negationMap = new HashedVectors();
		
		this.existentialMap = new HashedVectors();
		this.universalMap = new HashedVectors();
		this.minCardMap = new HashedVectors();
		this.maxCardMap = new HashedVectors();
		this.cardMap = new HashedVectors();
		this.enumerationMap = new HashedVectors();
		this.hasValueMap = new HashedVectors();
		
		this.classExpDepths = new HashedCounts();
		this.depthCountingClassExp = new HashSet();
				
		this.dhasValueMap = new HashedVectors(); 
		
		this.maxDepth = 0;
		this.currDepth = 0;
		
	}
		
	public void visit( OWLClass clazz ) throws OWLException 
	{  
		classExpDepths.put( clazz, new Integer(0) );
		//classExpCounts.add( clazz );
	}
	
	public void visit( OWLIndividual ind ) throws OWLException 
	{
		if ( ind.isAnonymous() ) 
		{
			//pw.print(   ind.getAnonId().getFragment()  );
		}
		else
		{
			//pw.print(  shortForms.shortForm( ind.getURI() )  );
		}
	}
	
	
	public void visit( OWLObjectProperty prop ) throws OWLException 
	{ }
	
	public void visit( OWLAnnotationProperty prop ) throws OWLException 
	{ }
	
	public void visit( OWLDataProperty prop ) throws OWLException 
	{ }
	
	public void visit( OWLDataValue cd ) throws OWLException 
	{ }

	public void visit( OWLAnd and ) throws OWLException 
	{
		int maxD = 0;
		
		for ( Iterator it = and.getOperands().iterator(); it.hasNext(); ) 
		{
			OWLDescription desc = (OWLDescription) it.next();
			classExpCounts.add( desc );
			conjunctionMap.add( desc, and );
			
			desc.accept( this );
			if ( (classExpDepths.get( desc ) != null) && (classExpDepths.getCount( desc ) > maxD ))
				maxD = (classExpDepths.getCount( desc ));
		}
		classExpDepths.put( and, new Integer(maxD) );
	}

	public void visit( OWLOr or ) throws OWLException 
	{
		int maxD = 0;
		for ( Iterator it = or.getOperands().iterator(); it.hasNext(); ) 
		{
			OWLDescription desc = (OWLDescription) it.next();
			classExpCounts.add( desc );
			disjunctionMap.add( desc, or );
			
			desc.accept( this );
			if ( (classExpDepths.get( desc ) != null) && (classExpDepths.getCount( desc ) > maxD ))
				maxD = (classExpDepths.getCount( desc ));
		}
		classExpDepths.put( or, new Integer(maxD) );
	}

	public void visit( OWLNot not ) throws OWLException 
	{
		int maxD = 0;
		OWLDescription desc = not.getOperand();
		classExpCounts.add( desc );
		negationMap.add( desc, not );
		
		desc.accept( this );
		
		if ( (classExpDepths.get( desc ) != null) && (classExpDepths.getCount( desc ) > maxD ))
			maxD = (classExpDepths.getCount( desc ));
		classExpDepths.put( not, new Integer(maxD) );
	}

	public void visit( OWLEnumeration enumeration ) throws OWLException 
	{
		classExpDepths.put( enumeration, new Integer(0) );
		
		for ( Iterator it = enumeration.getIndividuals().iterator(); it.hasNext(); ) 
		{
			OWLIndividual ind = (OWLIndividual) it.next();
			enumerationMap.add( ind, enumeration );
			
			ind.accept( this );
		}
	}

	public void visit( OWLObjectSomeRestriction restriction ) throws OWLException 
	{
		incrementCurrDepth();
		incrementAllDepthCounters();
		OWLObjectProperty prop = restriction.getObjectProperty();
		OWLDescription desc = restriction.getDescription();
		
		existentialMap.add( prop, desc );
		classExpCounts.add( desc );
		
		// if not already counted
		if ( !classExpDepths.keySet().contains( restriction ))
		{
			classExpDepths.add( restriction );
			depthCountingClassExp.add( restriction );
			desc.accept( this );
			depthCountingClassExp.remove( restriction );
		}
		else
			desc.accept( this ); 
		
		decrementCurrDepth();
	}

	public void visit( OWLObjectAllRestriction restriction ) throws OWLException 
	{
		incrementCurrDepth();
		incrementAllDepthCounters();
		
		OWLObjectProperty prop = restriction.getObjectProperty();
		OWLDescription desc = restriction.getDescription();
		
		universalMap.add( prop, desc );
		classExpCounts.add( desc );
		 
		if ( !classExpDepths.keySet().contains( restriction ))
		{
			classExpDepths.add( restriction );
			depthCountingClassExp.add( restriction );
			desc.accept( this );
			depthCountingClassExp.remove( restriction );
		}
		else
			desc.accept( this );
		
		decrementCurrDepth();
	}

	public void visit( OWLObjectValueRestriction restriction ) throws OWLException 
	{
		incrementCurrDepth();
		incrementAllDepthCounters();
		classExpDepths.put( restriction, new Integer(1) );

		OWLObjectProperty prop = restriction.getObjectProperty();
		OWLIndividual ind = restriction.getIndividual();
		
		hasValueMap.add( prop, ind );
		decrementCurrDepth();
	}
	
	public void visit( OWLDataSomeRestriction restriction ) throws OWLException 
	{
		incrementCurrDepth();
		incrementAllDepthCounters();
		classExpDepths.put( restriction, new Integer(1) );
		
		OWLDataProperty prop = restriction.getDataProperty();
		OWLDataRange datatype =restriction.getDataType();
		
		existentialMap.add( prop, datatype );
		decrementCurrDepth();
	}

	public void visit( OWLDataAllRestriction restriction ) throws OWLException 
	{
		incrementCurrDepth();
		incrementAllDepthCounters();
		classExpDepths.put( restriction, new Integer(1) );
		
		OWLDataProperty prop  = restriction.getDataProperty();
		OWLDataRange datatype = restriction.getDataType();
		
		universalMap.add( prop, datatype );
		decrementCurrDepth();
	}

	public void visit( OWLObjectCardinalityRestriction restriction ) throws OWLException 
	{
		incrementCurrDepth();
		incrementAllDepthCounters();
		classExpDepths.put( restriction, new Integer(1) );
		
		if ( restriction.isExactly() ) 
		{
			cardMap.add( restriction.getObjectProperty(), new Integer( restriction.getAtLeast() ) );
		} 
		else if ( restriction.isAtMost() ) 
		{
			maxCardMap.add( restriction.getObjectProperty(), new Integer( restriction.getAtMost() ) );
		} 
		else if ( restriction.isAtLeast() ) 
		{
			minCardMap.add( restriction.getObjectProperty(), new Integer( restriction.getAtLeast() ) );
		}
		decrementCurrDepth();
	}


	/*
	 * Data Property Restrictions
	 */
	public void visit( OWLDataCardinalityRestriction restriction ) throws OWLException 
	{
		incrementCurrDepth();
		classExpDepths.add( restriction );
		classExpDepths.put( restriction, new Integer(1) );
		
		if ( restriction.isExactly() ) 
		{
			cardMap.add( restriction.getDataProperty(), new Integer( restriction.getAtLeast() ) );
		} 
		else if ( restriction.isAtMost() ) 
		{
			maxCardMap.add( restriction.getDataProperty(), new Integer( restriction.getAtMost() ) );
		} 
		else if ( restriction.isAtLeast() ) 
		{
			minCardMap.add( restriction.getDataProperty(), new Integer( restriction.getAtLeast() ) );
		}
		decrementCurrDepth();
	}

	public void visit( OWLDataValueRestriction restriction ) throws OWLException 
	{
		incrementCurrDepth();
		OWLDataProperty prop = restriction.getDataProperty();
		OWLDataValue val = restriction.getValue();
		classExpDepths.put( restriction, new Integer(1) );
		
		dhasValueMap.add( prop, val );
		decrementCurrDepth();
	}

	
	/* ------------
	 * Class Axioms
	 * ------------
	 */	
	public void visit( OWLEquivalentClassesAxiom axiom ) throws OWLException 
	{
		int maxDepth = 0;
		Set equClas = axiom.getEquivalentClasses();
		for ( Iterator it = equClas.iterator(); it.hasNext(); ) 
		{
			OWLDescription desc = (OWLDescription) it.next();
			desc.accept( this );
			classExpCounts.add( desc );
		}
	}

	
	public void visit( OWLDisjointClassesAxiom axiom ) throws OWLException 
	{
		for ( Iterator it = axiom.getDisjointClasses().iterator(); it.hasNext(); ) 
		{
			OWLDescription desc = (OWLDescription) it.next();
			desc.accept( this );
			classExpCounts.add( desc );
		}
	}

	public void visit( OWLSubClassAxiom axiom ) throws OWLException 
	{
		OWLDescription subclass = axiom.getSubClass();
		OWLDescription supclass = axiom.getSuperClass();
		classExpCounts.add( subclass );
		classExpCounts.add( supclass );
		
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
	{
		//pw.print( shortForms.shortForm( ocdt.getURI() ) );		
	}

	public void visit( OWLDataEnumeration enumeration ) throws OWLException 
	{
		for ( Iterator it = enumeration.getValues().iterator(); it.hasNext(); ) 
		{
			OWLDataValue desc = (OWLDataValue) it.next();
			desc.accept( this );
		}
	}
	
	
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
	

    public void visit(OWLInverseFunctionalPropertyAxiom axiom) throws OWLException 
    {
    	if (axiom.getProperty().isInverseFunctional(axiom.getProperty().getOntologies())) 
    	{
			axiom.getProperty().accept( this );
    	}
    	if (axiom instanceof OWLInversePropertyAxiomImpl) 
    	{
    		OWLInversePropertyAxiomImpl invAxiom = (OWLInversePropertyAxiomImpl) axiom;
    		invAxiom.getProperty().accept( this );
    		invAxiom.getInverseProperty().accept( this );	
    	}
    }


    public void visit(OWLTransitivePropertyAxiom axiom) throws OWLException 
    {
		axiom.getProperty().accept( this );    
	}


    public void visit(OWLSymmetricPropertyAxiom axiom) throws OWLException 
    {
		axiom.getProperty().accept( this );    
	}


    public void visit(OWLInversePropertyAxiom axiom) throws OWLException 
    {
		axiom.getProperty().accept( this );
		axiom.getInverseProperty().accept( this );
    }

    // object assertion
    public void visit(OWLObjectPropertyInstance axiom) throws OWLException 
    {
		axiom.getSubject().accept( this );
		axiom.getProperty().accept( this );
		axiom.getObject().accept( this );
    }

    // data assertion
    public void visit(OWLDataPropertyInstance axiom) throws OWLException 
    {    	
		axiom.getSubject().accept( this );
		axiom.getProperty().accept( this );
		axiom.getObject().accept( this );
    }

    // type assertion
    public void visit(OWLIndividualTypeAssertion axiom) throws OWLException 
    {
		axiom.getIndividual().accept( this );
		axiom.getType().accept( this );
    }
    
    
    /*
     * manages currDepth and maxDepth
     */
    private void incrementCurrDepth()
    {
    	currDepth++;
    	if ( currDepth > maxDepth )
    		maxDepth = currDepth;
    }
    
    private void decrementCurrDepth()
    { currDepth--; }
    
    
    private void incrementAllDepthCounters() 
    {
    	try
    	{
	    	for (Iterator it = depthCountingClassExp.iterator(); it.hasNext(); )
	    	{
	    		OWLDescription obj = (OWLDescription)it.next();
	    		classExpDepths.add( obj );
	    	}
    	}
    	catch ( Exception e )
    	{ e.printStackTrace(); }
    }
    
}