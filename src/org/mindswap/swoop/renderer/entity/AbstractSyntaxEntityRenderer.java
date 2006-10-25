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

package org.mindswap.swoop.renderer.entity;

import java.awt.Component;
import java.awt.Font;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JEditorPane;

import org.mindswap.swoop.SwoopDisplayPanel;
import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.TermsDisplay;
import org.mindswap.swoop.renderer.BaseEntityRenderer;
import org.mindswap.swoop.renderer.SwoopEditableRenderer;
import org.mindswap.swoop.renderer.SwoopEntityRenderer;
import org.mindswap.swoop.renderer.SwoopRenderingVisitor;
import org.mindswap.swoop.utils.RuleValue;
import org.mindswap.swoop.utils.owlapi.QNameShortFormProvider;
import org.semanticweb.owl.io.RendererException;
import org.semanticweb.owl.io.ShortFormProvider;
import org.semanticweb.owl.io.abstract_syntax.Renderer;
import org.semanticweb.owl.model.OWLAnd;
import org.semanticweb.owl.model.OWLAnnotationInstance;
import org.semanticweb.owl.model.OWLAnnotationProperty;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLClassAxiom;
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
import org.semanticweb.owl.model.OWLObjectAllRestriction;
import org.semanticweb.owl.model.OWLObjectCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectPropertyInstance;
import org.semanticweb.owl.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owl.model.OWLObjectSomeRestriction;
import org.semanticweb.owl.model.OWLObjectValueRestriction;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOr;
import org.semanticweb.owl.model.OWLProperty;
import org.semanticweb.owl.model.OWLPropertyAxiom;
import org.semanticweb.owl.model.OWLPropertyDomainAxiom;
import org.semanticweb.owl.model.OWLSameIndividualsAxiom;
import org.semanticweb.owl.model.OWLSubClassAxiom;
import org.semanticweb.owl.model.OWLSubPropertyAxiom;
import org.semanticweb.owl.model.OWLSymmetricPropertyAxiom;
import org.semanticweb.owl.model.OWLTransitivePropertyAxiom;
import org.semanticweb.owl.rules.OWLRule;
import org.semanticweb.owl.rules.OWLRuleAtom;
import org.semanticweb.owl.rules.OWLRuleClassAtom;
import org.semanticweb.owl.rules.OWLRuleDObject;
import org.semanticweb.owl.rules.OWLRuleDVariable;
import org.semanticweb.owl.rules.OWLRuleDataPropertyAtom;
import org.semanticweb.owl.rules.OWLRuleDataValue;
import org.semanticweb.owl.rules.OWLRuleEqualityAtom;
import org.semanticweb.owl.rules.OWLRuleIObject;
import org.semanticweb.owl.rules.OWLRuleIVariable;
import org.semanticweb.owl.rules.OWLRuleIndividual;
import org.semanticweb.owl.rules.OWLRuleInequalityAtom;
import org.semanticweb.owl.rules.OWLRuleObjectPropertyAtom;
import org.semanticweb.owl.model.helper.OntologyHelper;

/**
 * @author Evren Sirin
 */ 
//public class AbstractSyntaxEntityRenderer extends BaseEntityRenderer implements SwoopEditableRenderer, SwoopEntityRenderer {
public class AbstractSyntaxEntityRenderer extends BaseEntityRenderer implements SwoopEntityRenderer {

	private String contentType = "text/html";
	String INDENT="";
	String TAB = "   ";
	public QNameShortFormProvider qnameProvider;
	protected String baseURI;
	protected Set allURIs;
	private List shortNames;
	private Map known;
	private int reservedNames;
	
	
	class AbstractSyntaxVisitor implements org.mindswap.swoop.renderer.SwoopRenderingVisitor {		
		ShortFormProvider shortForms; 
		boolean indenting = true;
		StringWriter sw;
		PrintWriter pw;
		boolean noLinks;
		
		
		/**
		 * @param shortForms
		 * @param ontology
		 */
		public AbstractSyntaxVisitor(ShortFormProvider shortForms, boolean noLinks) {
			this.shortForms = shortForms;
			this.noLinks = noLinks;
			reset();
		}
		
		public String escape(Object o) {
			if (noLinks) {
				return o.toString();
			}
			return AbstractSyntaxEntityRenderer.escape(o);
		}
		
		public String result() {
			return sw.toString();
		}

		public void reset() {
			sw = new StringWriter();
			pw = new PrintWriter( sw );
		}
		
		public void visit( OWLDataValue cd ) throws OWLException {		
			
			pw.print(" \"" + escape( cd.getValue() ) + "\"");

			/* Only show it if it's not string */

			URI dvdt = cd.getURI();
			String dvlang = cd.getLang();
			if ( dvdt!=null) {
				if (noLinks)
					// hm... have to render < as &lt;, looks ugly otherwise
					// pw.print( "^^" + "<" + dvdt.toString() + ">");
					pw.print( "^^" + "<" + dvdt.toString() + ">");
				else
					pw.print( "^^" + "&lt;" + escape(dvdt) + "&gt;");
			} else {
				if (dvlang!=null) {
					pw.print( "@" + escape(dvlang) );
				}
			}
		}

		public void visit( OWLAnd and ) throws OWLException {		
			pw.print(INDENT + "intersectionOf(\n");
			increaseINDENT();
			for ( Iterator it = and.getOperands().iterator();it.hasNext(); ) {
				OWLDescription desc = (OWLDescription) it.next();
				// pw.print(INDENT);				
				desc.accept( this );
				if (it.hasNext()) {
					pw.print("\n");
				}
			}
			decreaseINDENT();
			pw.print("\n" + INDENT + ")");
		}

		public void visit( OWLOr or ) throws OWLException {			
			pw.print(INDENT + "unionOf(\n");
			increaseINDENT();
			for ( Iterator it = or.getOperands().iterator();
			it.hasNext(); ) {
				OWLDescription desc = (OWLDescription) it.next();
				// pw.print (INDENT);
				desc.accept( this );
				if (it.hasNext()) {
					pw.print("\n");
				}
			}
			decreaseINDENT();
			pw.print("\n" + INDENT + ")");
		}

		public void visit( OWLNot not ) throws OWLException {
			pw.print(INDENT + "complementOf(\n");
			increaseINDENT();
			OWLDescription desc = not.getOperand();
			// pw.print(INDENT);
			desc.accept( this );
			decreaseINDENT();
			pw.print("\n" + INDENT + ")");
		}

		public void visit( OWLEnumeration enumeration ) throws OWLException {
			pw.print(INDENT + "oneOf(");
			for ( Iterator it = enumeration.getIndividuals().iterator();
			it.hasNext(); ) {
				OWLIndividual desc = (OWLIndividual) it.next();
				desc.accept( this );
				if (it.hasNext()) {
					pw.print(" ");
				}
			}
			pw.print(")");
		}

		public void visit( OWLObjectSomeRestriction restriction ) throws OWLException {
			pw.print(INDENT + "restriction(");
			restriction.getObjectProperty().accept( this );
			pw.print(" someValuesFrom(");
			//******************************************************
			//Changed for Econnections
			//******************************************************
			if(restriction.getObjectProperty().isLink()){
				pw.print(" ForeignClass(");
				pw.print(" foreignOntology(" + shortForm(restriction.getObjectProperty().getLinkTarget()) + ")");
				
				restriction.getDescription().accept( this );
				pw.print(")");
			}
			else{
				restriction.getDescription().accept( this );}
			
			
			//**************************************
			pw.print("))");
		}

		public void visit( OWLObjectAllRestriction restriction ) throws OWLException {
			pw.print(INDENT + "restriction(");
			restriction.getObjectProperty().accept( this );
			pw.print(" allValuesFrom(");
            
			//******************************************************
			//Changed for Econnections
			//******************************************************
			
			if(restriction.getObjectProperty().isLink()){
				pw.print(" ForeignClass(");
				pw.print(" foreignOntology(" + shortForm(restriction.getObjectProperty().getLinkTarget()) + ")");
				restriction.getDescription().accept( this );
				pw.print(")");
			}
			else{				   		
			restriction.getDescription().accept( this );}
			pw.print("))");
		}

		public void visit( OWLObjectValueRestriction restriction ) throws OWLException {
			pw.print(INDENT + "restriction(");
			restriction.getObjectProperty().accept( this );
			/* Changed from hasValue */
			pw.print(" value (");

			//******************************************************
			//Changed for Econnections
			//******************************************************
			
			if(restriction.getObjectProperty().isLink()){
				pw.print(" ForeignIndividual(");
				pw.print(" foreignOntology(" + shortForm(restriction.getObjectProperty().getLinkTarget()) + ")");
				restriction.getIndividual().accept( this );
				pw.print(")");
			}
			else{				   		
				restriction.getIndividual().accept( this );}
			
			   pw.print("))");
		}

		public void visit( OWLDataSomeRestriction restriction ) throws OWLException {
			pw.print(INDENT + "restriction(");
			restriction.getDataProperty().accept( this );
			pw.print(" someValuesFrom (");
			restriction.getDataType().accept( this );
			pw.print("))");
		}

		public void visit( OWLDataAllRestriction restriction ) throws OWLException {
			pw.print(INDENT + "restriction(");
			restriction.getDataProperty().accept( this );
			pw.print(" allValuesFrom(");
			restriction.getDataType().accept( this );
			pw.print("))");
		}

		public void visit( OWLObjectCardinalityRestriction restriction ) throws OWLException {
			pw.print(INDENT +"restriction(");
			restriction.getObjectProperty().accept( this );
			if ( restriction.isExactly() ) {
				pw.print(" cardinality(" + restriction.getAtLeast() + "))");
			} else if ( restriction.isAtMost() ) {
				pw.print(" maxCardinality(" + restriction.getAtMost() + "))");
			} else 	if ( restriction.isAtLeast() ) {
				pw.print(" minCardinality(" + restriction.getAtLeast() + "))");
			} 
		}

		public void visit( OWLDataCardinalityRestriction restriction ) throws OWLException {
			pw.print(INDENT +"restriction(");
			restriction.getDataProperty().accept( this );
			if ( restriction.isExactly() ) {
				pw.print(" cardinality(" + restriction.getAtLeast() + "))");
			} else if ( restriction.isAtMost() ) {
				pw.print(" maxCardinality(" + restriction.getAtMost() + "))");
			} else 	if ( restriction.isAtLeast() ) {
				pw.print(" minCardinality(" + restriction.getAtLeast() + "))");
			} 
		}

		public void visit( OWLDataValueRestriction restriction ) throws OWLException {
			pw.print(INDENT + "restriction(");
			restriction.getDataProperty().accept( this );
			/* Changed from hasValue */
			pw.print(" value (");
			restriction.getValue().accept( this );
			pw.print("))");
		}

		public void visit( OWLEquivalentClassesAxiom axiom ) throws OWLException {
			pw.print("EquivalentClasses(");
			for ( Iterator it = axiom.getEquivalentClasses().iterator();
			it.hasNext(); ) {
				OWLDescription desc = (OWLDescription) it.next();
				desc.accept( this );
				if (it.hasNext()) {
					pw.print(" ");
				}
			}
			pw.print(")");
		}

		public void visit( OWLDisjointClassesAxiom axiom ) throws OWLException {
			pw.print("DisjointClasses(");
			for ( Iterator it = axiom.getDisjointClasses().iterator();
			it.hasNext(); ) {
				OWLDescription desc = (OWLDescription) it.next();
				desc.accept( this );
				if (it.hasNext()) {
					pw.print(" ");
				}
			}
			pw.print(")");
		}

		public void visit( OWLSubClassAxiom axiom ) throws OWLException {
			pw.print("SubClassOf(");
			axiom.getSubClass().accept( this );
			pw.print(" ");
			axiom.getSuperClass().accept( this );
			pw.print(")");
		}

		public void visit( OWLEquivalentPropertiesAxiom axiom ) throws OWLException {
			pw.print("EquivalentProperties(");
			for ( Iterator it = axiom.getProperties().iterator();
			it.hasNext(); ) {
				OWLProperty prop = (OWLProperty) it.next();
				prop.accept( this );
				if (it.hasNext()) {
					pw.print(" ");
				}
			}
			pw.print(")");
		}

		public void visit( OWLSubPropertyAxiom axiom ) throws OWLException {
			pw.print("SubPropertyOf(");
			axiom.getSubProperty().accept( this );
			pw.print(" ");
			axiom.getSuperProperty().accept( this );
			pw.print(")");
		}

		public void visit( OWLDifferentIndividualsAxiom ax) throws OWLException {
			pw.print("DifferentIndividuals(");
			for ( Iterator it = ax.getIndividuals().iterator();
			it.hasNext(); ) {
				OWLIndividual desc = (OWLIndividual) it.next();
				desc.accept( this );
				if (it.hasNext()) {
					pw.print(" ");
				}
			}
			pw.print(")");
		}

		public void visit( OWLSameIndividualsAxiom ax) throws OWLException {
			pw.print("SameIndividual(");
			for ( Iterator it = ax.getIndividuals().iterator();
			it.hasNext(); ) {
				OWLIndividual desc = (OWLIndividual) it.next();
				desc.accept( this );
				if (it.hasNext()) {
					pw.print(" ");
				}
			}
			pw.print(")");
		}

		public void visit( OWLDataType ocdt ) throws OWLException {
			pw.print( escape(shortForms.shortForm( ocdt.getURI() )) );
		}

		public void visit( OWLDataEnumeration enumeration ) throws OWLException {
			pw.print("oneOf(");
			for ( Iterator it = enumeration.getValues().iterator();
			it.hasNext(); ) {
				OWLDataValue desc = (OWLDataValue) it.next();
				desc.accept( this );
				if (it.hasNext()) {
					pw.print(" ");
				}
			}
			pw.print(")");
		}
		
		public void visit( OWLClass clazz ) throws OWLException {
			if (noLinks) {
				pw.print(INDENT + shortForm( clazz.getURI() ));				
			}
			else {
				pw.print(INDENT + "<a href=\"" + escape(clazz.getURI()) + "\">" + escape(shortForm( clazz.getURI() )) + "</a>" );
			}
		}
		
		public void visit( OWLIndividual ind ) throws OWLException {
			if ( ind.isAnonymous() ) {
				pw.print( "Anonymous individual" );
			} else {
				if (noLinks) {
					pw.print( shortForm( ind.getURI() )  );
				}
				else {
					pw.print( "<a href=\"" + escape(ind.getURI()) + "\">" + escape(shortForm( ind.getURI() )) + "</a>" );
				}
			}
		}

		public void visit( OWLObjectProperty prop ) throws OWLException {
			if (noLinks) {
				pw.print(shortForm( prop.getURI() )  );
			}
			else {
				pw.print("<a href=\"" + escape(prop.getURI()) + "\">" + escape(shortForm( prop.getURI() )) + "</a>" );
			}			
		}
		
		
		public void visit( OWLDataProperty prop ) throws OWLException {
			if (noLinks) {
				pw.print(shortForm( prop.getURI() )  );
			}
			else {
				pw.print("<a href=\"" + prop.getURI() + "\">" + shortForm( prop.getURI() ) + "</a>" );
			}		
		}

		/* (non-Javadoc)
		 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLAnnotationProperty)
		 */
		public void visit(OWLAnnotationProperty arg0) throws OWLException {
			// TODO Auto-generated method stub
			
		}

		/* (non-Javadoc)
		 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLAnnotationInstance)
		 */
		public void visit(OWLAnnotationInstance arg0) throws OWLException {
			// TODO Auto-generated method stub
			
		}

		/* (non-Javadoc)
		 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLFrame)
		 */
		public void visit(OWLFrame arg0) throws OWLException {
			// TODO Auto-generated method stub
			
		}

		/* (non-Javadoc)
		 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLOntology)
		 */
		public void visit(OWLOntology arg0) throws OWLException {
			// TODO Auto-generated method stub
			
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
	}
	
	
	
	public String getContentType() {
		return contentType;
	}
	
	public String getName() {
		return "Abstract Syntax";
	}
	
	/* (non-Javadoc)
	 * @see org.mindswap.swoop.BaseEntityRenderer#initVisitor()
	 */
	public SwoopRenderingVisitor createVisitor() {
		//if editorEnabled then nolinks=true
		return new AbstractSyntaxVisitor(this, editorEnabled);
	}


	 public void setEditorEnabled(boolean mode) {
		
		editorEnabled = mode;
		if (mode) contentType = "text/plain";
		else contentType = "text/html";
	}
	
	 
	 /**
		 * Render the ontology in RDF/XML but skip rendering of a particular entity
		 * This is used (during update) to support editing inside the RDF/XML renderer
		 * @param ontology
		 * @param skip - element skipped during printing
		 * @param only - only elements in this set are printed 
		 * @param writer
		 * @throws RendererException
		 */
	    public void renderOntology(SwoopModel swoopModel, OWLOntology ontology, OWLEntity skip, String newEntityCode, Writer writer)
	        throws RendererException {
	        // this.ontology = ontology;
	        try {
	        	
	        	this.swoopModel = swoopModel; 
	            
	            this.allURIs = OntologyHelper.allURIs(ontology);
	            	            
				this.reasoner = swoopModel.getReasoner();				
				this.writer = writer;
				this.pw = new PrintWriter(writer);
				
				this.editorEnabled = swoopModel.getEditorEnabled();
				this.showInherited = swoopModel.getShowInherited();
				this.showDivisions = swoopModel.getShowDivisions();
				this.showImports = swoopModel.getShowImports();
				
	           //this.visitor = new CorrectedRDFXMLVisitor(this);
	            //****************************************************
	            //changed for Econnections
	            //****************************************************
	            this.visitor = new AbstractSyntaxVisitor(this,true);
	            visitor.reset();
	        //	visitor.setProperty(null);
	        //	visitor.setPropertyObject(null);

	            //***************************************************
		    //
	            this.baseURI = swoopModel.getSelectedOntology().getURI().toString();
	            this.qnameProvider = new QNameShortFormProvider();

	            generateShortNames();
	            writeTextHeader();

	            for (Iterator it =
	                orderedEntities(ontology.getObjectProperties()).iterator();
	                it.hasNext();
	                ) {
	            	OWLObjectProperty prop = (OWLObjectProperty) it.next();
	                if (!prop.equals(skip)) renderTextObjectProperty( prop);
	                else pw.println(newEntityCode); 
	            }
	            pw.println("\n");
	            
	            for (Iterator it =
	                orderedEntities(ontology.getDataProperties()).iterator();
	                it.hasNext();
	                ) {
	            	OWLDataProperty prop = (OWLDataProperty) it.next();
	            	if (!prop.equals(skip)) renderTextDataProperty( prop);
	            	else pw.println(newEntityCode);
	            }
	            pw.println();
	            for (Iterator it =
	                orderedEntities(ontology.getClasses()).iterator();
	                it.hasNext();
	                ) {
	            	OWLClass cla = (OWLClass) it.next();
	                if (skip==null || !cla.equals(skip)) 
	                	renderTextClass(cla);
	                else pw.println(newEntityCode); 
	            }

	         
	           
	            pw.println();
	            for (Iterator it =
	                orderedEntities(ontology.getIndividuals()).iterator();
	                it.hasNext();
	                ) {
	            	OWLIndividual ind = (OWLIndividual) it.next();
	                if (!ind.equals(skip)) renderTextIndividual( ind);
	                else pw.println(newEntityCode);
	            }
	            pw.println();
	            for (Iterator it =
	                orderedEntities(ontology.getClassAxioms()).iterator();
	                it.hasNext();
	                ) {
	                renderTextClassAxiom((OWLClassAxiom) it.next());
	            }
	            
	            for (Iterator it =
	                orderedEntities(ontology.getPropertyAxioms()).iterator();
	                it.hasNext();
	                ) {
	                renderTextPropertyAxiom((OWLPropertyAxiom) it.next());
	            }
		    
		    writeFooter();
		    pw.flush();
	        } catch (OWLException ex) {
	            throw new RendererException(ex.getMessage());
	        }
		
	    }

	    
	/* Well dodgy coding */
	protected void renderAnnotationContent(Object o) throws OWLException {
		if (o instanceof URI) {
			escape(o);
		} else if (o instanceof OWLIndividual) {
			OWLIndividual ind = (OWLIndividual) o;
			if (ind.isAnonymous()) {
				print(escape(ind.getAnonId()));
			} else {
				print(escape(ind.getURI()));
			}
		} else if (o instanceof OWLDataValue) {
			OWLDataValue dv = (OWLDataValue) o;
			print("\"" + escape(dv.getValue()) + "\"");
			/* Only show it if it's not string */
			URI dvdt = dv.getURI();
			String dvlang = dv.getLang();
			if (dvdt != null) {
				print("^^" + escape(dvdt));
				// 		if (!dv.getURI().toString().equals(
				// XMLSchemaSimpleDatatypeVocabulary.INSTANCE.getString())) {
				// 		    print( "^^" + dv.getURI() );
				// 		}
			} else {
				if (dvlang != null) {
					print("@" + escape(dvlang));
				}
			}
		} else {
			print("\""+escape(o)+"\"");
		}
	}

	/** Render the annotations for an object */
	protected void renderAnnotations(OWLNamedObject object ) throws OWLException {
		/* Bit nasty this -- annotations result in a new axiom */
		if (!object.getAnnotations(reasoner.getOntology()).isEmpty()) {
			for (Iterator it = object.getAnnotations(reasoner.getOntology()).iterator(); it.hasNext();) {
				OWLAnnotationInstance oai = (OWLAnnotationInstance) it.next();
				print( TAB + "annotation(" + shortForm(oai.getProperty().getURI()) + " ");
				/* Just whack out the content. This isn't quite right... */
				renderAnnotationContent(oai.getContent());
				//		print( "\"" + oai.getContent() + "\"" );
				print(")");
				/* Do we need to do this??? */
				visitor.reset();
				oai.accept(visitor);
				// 		if (it.hasNext()) {
				// 		    println();
				// 		}
			}
		}		
	}
	
	protected void renderClass(OWLClass clazz) throws OWLException {
		boolean done = false;
		
		String deprecation = (clazz.isDeprecated(reasoner.getOntology()) ? " Deprecated" : "");
		
		if(!clazz.getAnnotations(reasoner.getOntology()).isEmpty()) {
			if (editorEnabled) {
				println(
						"Class ("
						+ shortForm(clazz.getURI())
						+ deprecation
						+ " partial");
			}
			else {
				println(
					"<b>Class </b>("
					+ escape(shortForm(clazz.getURI()))
					+ deprecation
					+ " <i>partial</i>");
			}
			renderAnnotations(clazz);
			println("\n)");
		}
		
		for (Iterator it = clazz.getEquivalentClasses(reasoner.getOntologies()).iterator(); it.hasNext();) {
			OWLDescription eq = (OWLDescription) it.next();
			if (editorEnabled) {
				println(
						"Class("
						+ shortForm(clazz.getURI())
						+ deprecation
						+ " complete ");
			}
			else {
				println(
					"<b>Class&nbsp</b>("
					+ escape(shortForm(clazz.getURI()))
					+ deprecation
					+ " <i>complete</i> ");
			}
			visitor.reset();
			increaseINDENT();
			eq.accept(visitor);
			println(visitor.result() + "\n)");
			decreaseINDENT(); 
			done = true;
		}

		if (!clazz.getSuperClasses(reasoner.getOntologies()).isEmpty()) {
			if (editorEnabled) {
				println(
						"Class ("
						+ shortForm(clazz.getURI())
						+ deprecation
						+ " partial ");
			}
			else {
				println(
					"<b>Class </b>("
					+ escape(shortForm(clazz.getURI()))
					+ deprecation
					+ " <i>partial</i> ");
			}
			for (Iterator it = clazz.getSuperClasses(reasoner.getOntologies()).iterator(); it.hasNext();) {
				OWLDescription eq = (OWLDescription) it.next();
				visitor.reset();
				increaseINDENT();
				eq.accept(visitor);				
				print(visitor.result());
				decreaseINDENT();
				if (it.hasNext()) {
					println();
				}
				done = true;
			}
			println("\n)");
		}
		/*
		 * This has changed -- used to be simply a oneof in the class definition. We now get a
		 * special keyword in the vocabulary
		 */
		for (Iterator it = clazz.getEnumerations(reasoner.getOntologies()).iterator(); it.hasNext();) {
			OWLDescription eq = (OWLDescription) it.next();
			if (editorEnabled) {
				print("EnumeratedClass("
						+ shortForm(clazz.getURI())
						+ deprecation);
			}
			else {
				print(
					"<b>EnumeratedClass</b>("
					+ escape(shortForm(clazz.getURI()))
					+ deprecation);	
			}
			/* We know that the description has to be a oneof */
			try {
				OWLEnumeration enumeration = (OWLEnumeration) eq;

				for (Iterator iit = enumeration.getIndividuals().iterator(); iit.hasNext();) {
					OWLIndividual desc = (OWLIndividual) iit.next();
					visitor.reset();
					desc.accept(visitor);
					print(" " + visitor.result());
					// 		    if (iit.hasNext()) {
					// 			print(" ");
					// 		    }
				}
				println(")");
				done = true;
			} catch (ClassCastException ex) {
				throw new RendererException(ex.getMessage());
			}
		}
// Added to render rules
		if (swoopModel.getEnableRules()) {
			Set rules = swoopModel.getRuleExpr().getRules(clazz);
			for (Iterator it = rules.iterator(); it.hasNext();) {
				OWLRule rule = ((RuleValue) it.next()).getRule();
				println("\n \t" + "Implies(" + "Antecedent(");
				Set antecedents = rule.getAntecedents();
				for (Iterator it1 = antecedents.iterator(); it1.hasNext();) {
					OWLRuleAtom antAtom = (OWLRuleAtom) it1.next();
					renderAtom(antAtom);
					//				visitor.reset();
					//				antAtom.accept((OWLRuleAtomVisitor) visitor);
					//				print(" "+visitor.result());
				}
				println(")");
				println("\t" + "Consequent(");
				OWLRuleAtom consAtom = (OWLRuleAtom) rule.getConsequents()
						.iterator().next();
				//visitor.reset();
				//consAtom.accept((OWLRuleAtomVisitor) visitor);
				//print(" "+visitor.result());
				renderAtom(consAtom);
				println(")");
				println(")");
				done = true;
			}
		}
		if (!done) {
			/* We need to give at least an empty definition */
			if (editorEnabled) {
				println(
						"Class ("
						+ shortForm(clazz.getURI())
						+ deprecation
						+ " partial"
						+ ")");	
			}
			else {
				println(
					"<b>Class </b>("
					+ escape(shortForm(clazz.getURI()))
					+ deprecation
					+ " <i>partial</i>"
					+ ")");
			}
		}
	}
	
	protected void renderTextClass(OWLClass clazz) throws OWLException {
		boolean done = false;
		
		String deprecation = (clazz.isDeprecated(reasoner.getOntology()) ? " Deprecated" : "");
		
		if(!clazz.getAnnotations(reasoner.getOntology()).isEmpty()) {
			println(
					"Class ("
					+ shortForm(clazz.getURI())
					+ deprecation
					+ " partial");
			renderAnnotations(clazz);
			println("\n)");
		}
		
		for (Iterator it = clazz.getEquivalentClasses(reasoner.getOntologies()).iterator(); it.hasNext();) {
			OWLDescription eq = (OWLDescription) it.next();
			println(
					"Class("
					+ shortForm(clazz.getURI())
					+ deprecation
					+ " complete ");
			visitor.reset();
			increaseINDENT();
			eq.accept(visitor);
			println(visitor.result() + "\n)");
			decreaseINDENT(); 
			done = true;
		}

		if (!clazz.getSuperClasses(reasoner.getOntologies()).isEmpty()) {
			println(
					"Class ("
					+ shortForm(clazz.getURI())
					+ deprecation
					+ " partial ");
			for (Iterator it = clazz.getSuperClasses(reasoner.getOntologies()).iterator(); it.hasNext();) {
				OWLDescription eq = (OWLDescription) it.next();
				visitor.reset();
				increaseINDENT();
				eq.accept(visitor);				
				print(visitor.result());
				decreaseINDENT();
				if (it.hasNext()) {
					println();
				}
				done = true;
			}
			println("\n)");
		}
		/*
		 * This has changed -- used to be simply a oneof in the class definition. We now get a
		 * special keyword in the vocabulary
		 */
		for (Iterator it = clazz.getEnumerations(reasoner.getOntologies()).iterator(); it.hasNext();) {
			OWLDescription eq = (OWLDescription) it.next();
			print(
					"EnumeratedClass("
					+ shortForm(clazz.getURI())
					+ deprecation);		
			/* We know that the description has to be a oneof */
			try {
				OWLEnumeration enumeration = (OWLEnumeration) eq;

				for (Iterator iit = enumeration.getIndividuals().iterator(); iit.hasNext();) {
					OWLIndividual desc = (OWLIndividual) iit.next();
					visitor.reset();
					desc.accept(visitor);
					print(" " + visitor.result());
					// 		    if (iit.hasNext()) {
					// 			print(" ");
					// 		    }
				}
				println(")");
				done = true;
			} catch (ClassCastException ex) {
				throw new RendererException(ex.getMessage());
			}
		}
// Added for rendering rules
		if (swoopModel.getEnableRules()) {
			Set rules = swoopModel.getRuleExpr().getRules(clazz);
			for (Iterator it = rules.iterator(); it.hasNext();) {
				OWLRule rule = ((RuleValue) it.next()).getRule();
				println("\n \t" + "Implies(" + "Antecedent(");
				Set antecedents = rule.getAntecedents();
				for (Iterator it1 = antecedents.iterator(); it1.hasNext();) {
					OWLRuleAtom antAtom = (OWLRuleAtom) it1.next();
					renderAtom(antAtom);
					//				visitor.reset();
					//				antAtom.accept((OWLRuleAtomVisitor) visitor);
					//				print(" "+visitor.result());
				}
				println(")");
				println("\t" + "Consequent(");
				OWLRuleAtom consAtom = (OWLRuleAtom) rule.getConsequents()
						.iterator().next();
				//visitor.reset();
				//consAtom.accept((OWLRuleAtomVisitor) visitor);
				//print(" "+visitor.result());
				renderAtom(consAtom);
				println(")");
				println(")");
				done = true;
			}
		}
		if (!done) {
			/* We need to give at least an empty definition */
			println(
					"Class ("
					+ shortForm(clazz.getURI())
					+ deprecation
					+ " partial"
					+ ")");
		}
	}

	protected void renderIndividual(OWLIndividual ind) throws OWLException {
       if ( ind.isAnonymous() ) 
	 	    if (editorEnabled) print("Individual(" );
       		else print("<b>Individual</b>(" );	 	
	 	else 
	 		if (editorEnabled) print("Individual(" + shortForm(ind.getURI())); 
	 		else print("<b>Individual</b>(" + escape(shortForm(ind.getURI())));		
	 	
		if (ind.getAnnotations(reasoner.getOntology()).isEmpty()
				&& ind.getTypes(reasoner.getOntologies()).isEmpty()
				&& ind.getObjectPropertyValues(reasoner.getOntologies()).keySet().isEmpty()
				&& ind.getDataPropertyValues(reasoner.getOntologies()).keySet().isEmpty()) {
			println(")");
		} else {
			for (Iterator it = ind.getAnnotations(reasoner.getOntology()).iterator(); it.hasNext();) {
				println();
				OWLAnnotationInstance oai = (OWLAnnotationInstance) it.next();
				if (editorEnabled) print(TAB + "annotation(" + shortForm(oai.getProperty().getURI()) + " ");
				else print(TAB + "<i>annotation</i>(" + escape(shortForm(oai.getProperty().getURI())) + " ");
				/* Just whack out the content */
				renderAnnotationContent(oai.getContent());
				//		print( oai.getContent() );
				print(")");
				visitor.reset();
				oai.accept(visitor);
				// 		if (it.hasNext()) {
				// 		    println();
				// 		}
			}

			//	    println();
			for (Iterator it = ind.getTypes(reasoner.getOntologies()).iterator(); it.hasNext();) {
				println();
				OWLDescription eq = (OWLDescription) it.next();
				visitor.reset();
				eq.accept(visitor);
				if (editorEnabled) print(TAB + "type(" + visitor.result() + ")"); 
				else print(TAB + "<i>type</i>(" + visitor.result() + ")");
				// 		if (it.hasNext()) {
				// 		    println();
				// 		}
			}
			Map propertyValues = ind.getObjectPropertyValues(reasoner.getOntologies());
			//	    System.out.println("ZZ: " + ind.getURI());
			for (Iterator it = propertyValues.keySet().iterator(); it.hasNext();) {
				println();
				OWLObjectProperty prop = (OWLObjectProperty) it.next();
				Set vals = (Set) propertyValues.get(prop);
				for (Iterator valIt = vals.iterator(); valIt.hasNext();) {
					//		    System.out.println("QQ: " + ((OWLIndividual) valIt.next()).getURI());
					OWLIndividual oi = (OWLIndividual) valIt.next();
					visitor.reset();
					oi.accept(visitor);
					//*******************************************************
					//changed for Econnections
					//*******************************************************
					if(!prop.isLink()){
						if (editorEnabled) {
							print(TAB + "value(" + shortForm(prop.getURI()) + " " + visitor.result() + ")");
						} else {
							print(TAB + "<i>value</i>(" + escape(shortForm(prop.getURI())) + " " + visitor.result() + ")");
						}
						
						if (valIt.hasNext()) {
							println();
						}
					}
					else{
						if (editorEnabled) {
							print(TAB + "value(ForeignIndividual(foreignOntology((" + shortForm(prop.getLinkTarget()) + ")" + shortForm(prop.getURI()) + " " + visitor.result() + ")");
						} else {
							print(TAB + "<i>value(ForeignIndividual(foreignOntology(</i>(" + escape(shortForm(prop.getLinkTarget())) + ")" + escape(shortForm(prop.getURI())) + " " + visitor.result() + ")");
						}
						if (valIt.hasNext()) {
							println();
						}
					}
				}				
			}
			Map dataValues = ind.getDataPropertyValues(reasoner.getOntologies());
			//	    System.out.println("ZZ: " + ind.getURI());
			for (Iterator it = dataValues.keySet().iterator(); it.hasNext();) {
				println();
				OWLDataProperty prop = (OWLDataProperty) it.next();
				Set vals = (Set) dataValues.get(prop);
				for (Iterator valIt = vals.iterator(); valIt.hasNext();) {
					//		    System.out.println("QQ: " + ((OWLIndividual) valIt.next()).getURI());
					OWLDataValue dtv = (OWLDataValue) valIt.next();
					visitor.reset();
					dtv.accept(visitor);
					if (editorEnabled) {
						print(TAB + "value(" + shortForm(prop.getURI()) + " " + visitor.result() + ")");
					} else {
						print(TAB + "<i>value</i>(" + escape(shortForm(prop.getURI())) + " " + visitor.result() + ")");
					}
					if (valIt.hasNext()) {
						println();
					}
				}
				// 		if (it.hasNext()) {
				// 		    println();
				// 		}
			}
			println("\n)");
		}

	}
	
	
	protected void renderTextIndividual(OWLIndividual ind) throws OWLException {
	       if ( ind.isAnonymous() ) 
		 	    print("Individual(" );	 	
		 	else 
		 		print("Individual(" + shortForm(ind.getURI()));		
		 	
			if (ind.getAnnotations(reasoner.getOntology()).isEmpty()
					&& ind.getTypes(reasoner.getOntologies()).isEmpty()
					&& ind.getObjectPropertyValues(reasoner.getOntologies()).keySet().isEmpty()
					&& ind.getDataPropertyValues(reasoner.getOntologies()).keySet().isEmpty()) {
				println(")");
			} else {
				for (Iterator it = ind.getAnnotations(reasoner.getOntology()).iterator(); it.hasNext();) {
					println();
					OWLAnnotationInstance oai = (OWLAnnotationInstance) it.next();
					print(TAB + "annotation(" + shortForm(oai.getProperty().getURI()) + " ");
					/* Just whack out the content */
					renderAnnotationContent(oai.getContent());
					//		print( oai.getContent() );
					print(")");
					visitor.reset();
					oai.accept(visitor);
					// 		if (it.hasNext()) {
					// 		    println();
					// 		}
				}

				//	    println();
				for (Iterator it = ind.getTypes(reasoner.getOntologies()).iterator(); it.hasNext();) {
					println();
					OWLDescription eq = (OWLDescription) it.next();
					visitor.reset();
					eq.accept(visitor);
					print(TAB + "type(" + visitor.result() + ")");
					// 		if (it.hasNext()) {
					// 		    println();
					// 		}
				}
				Map propertyValues = ind.getObjectPropertyValues(reasoner.getOntologies());
				//	    System.out.println("ZZ: " + ind.getURI());
				for (Iterator it = propertyValues.keySet().iterator(); it.hasNext();) {
					println();
					OWLObjectProperty prop = (OWLObjectProperty) it.next();
					Set vals = (Set) propertyValues.get(prop);
					for (Iterator valIt = vals.iterator(); valIt.hasNext();) {
						//		    System.out.println("QQ: " + ((OWLIndividual) valIt.next()).getURI());
						OWLIndividual oi = (OWLIndividual) valIt.next();
						visitor.reset();
						oi.accept(visitor);
						//*******************************************************
						//changed for Econnections
						//*******************************************************
						if(!prop.isLink()){
							print(TAB + "value(" + shortForm(prop.getURI()) + " " + visitor.result() + ")");
							if (valIt.hasNext()) {
								println();
							}
						}
						else{
							print(TAB + "value(ForeignIndividual(foreignOntology((" + shortForm(prop.getLinkTarget()) + ")" + shortForm(prop.getURI()) + " " + visitor.result() + ")");
							if (valIt.hasNext()) {
								println();
							}
						}
					}				
				}
				Map dataValues = ind.getDataPropertyValues(reasoner.getOntologies());
				//	    System.out.println("ZZ: " + ind.getURI());
				for (Iterator it = dataValues.keySet().iterator(); it.hasNext();) {
					println();
					OWLDataProperty prop = (OWLDataProperty) it.next();
					Set vals = (Set) dataValues.get(prop);
					for (Iterator valIt = vals.iterator(); valIt.hasNext();) {
						//		    System.out.println("QQ: " + ((OWLIndividual) valIt.next()).getURI());
						OWLDataValue dtv = (OWLDataValue) valIt.next();
						visitor.reset();
						dtv.accept(visitor);
						print(TAB + "value(" + shortForm(prop.getURI()) + " " + visitor.result() + ")");
						if (valIt.hasNext()) {
							println();
						}
					}
					// 		if (it.hasNext()) {
					// 		    println();
					// 		}
				}
				println("\n)");
			}

		}

	protected void renderAnnotationProperty(OWLAnnotationProperty prop)	throws OWLException {
		println(" <b>AnnotationProperty</b>(" + escape(shortForm(prop.getURI())) + ")");
	}

	protected void renderObjectProperty(OWLObjectProperty prop) throws OWLException {
	    //*****************************************
		//Changed for Econnections	
		//****************************************
		if(!prop.isLink()){
			if (editorEnabled) print("ObjectProperty(" + shortForm(prop.getURI()));
			else print("<b>ObjectProperty</b>(" + escape(shortForm(prop.getURI())));
		}
		else{
			if (editorEnabled) {
				print("LinkProperty(" + shortForm(prop.getURI()));
				print("foreignOntology(" + shortForm(prop.getLinkTarget()) + ")");
			}
			else {
				print("<b>LinkProperty</b>(" + escape(shortForm(prop.getURI())));
				print("<b> foreignOntology</b>(" + escape(shortForm(prop.getLinkTarget())) + ")");
			}
			
		}
		if (prop.isTransitive(reasoner.getOntologies())) {
			if (editorEnabled) print(" Transitive");
			else print(" <i>Transitive</i>");
		}
		if (prop.isFunctional(reasoner.getOntologies())) {
			if (editorEnabled) print(" Functional");
			else print(" <i>Functional</i>");
		}
		if (prop.isInverseFunctional(reasoner.getOntologies())) {
			if (editorEnabled) print(" InverseFunctional");
			else print(" <i>InverseFunctional</i>");
		}
		if (prop.isSymmetric(reasoner.getOntologies())) {
			if (editorEnabled) print(" Symmetric");
			else print(" <i>Symmetric</i>");
		}
		for (Iterator it = prop.getInverses(reasoner.getOntologies()).iterator(); it.hasNext();) {
			println();
			OWLObjectProperty inv = (OWLObjectProperty) it.next();
			visitor.reset();
			inv.accept(visitor);
			if (editorEnabled) print(TAB + "inverseOf(" + visitor.result() + ")");
			else print(TAB + "<i>inverseOf</i>(" + visitor.result() + ")");
		}
		for (Iterator it = reasoner.superPropertiesOf(prop).iterator(); it.hasNext();) {
			println();
			try {
				
				// superPropertiesOf returns a set of sets,that's why another iterator is needed
				for (Iterator it1 = ((Set) it.next()).iterator(); it1.hasNext();) {
					Object Obj = it1.next();
					if (Obj instanceof OWLObjectProperty) {									
						OWLObjectProperty sup = (OWLObjectProperty)Obj;
						visitor.reset();
						sup.accept(visitor);
						if (editorEnabled) print(TAB + "super(" + visitor.result() + ")");
						else print(TAB + "<i>super</i>(" + visitor.result() + ")");
					}
				
				}	
			}
			catch (Exception e) {
				System.out.println(e.getMessage().toString());
			}
		}
		
		for (Iterator it = reasoner.domainsOf(prop).iterator(); it.hasNext();) {
			println();
			OWLDescription dom = (OWLDescription) it.next();
			visitor.reset();
			dom.accept(visitor);
			if (editorEnabled) print(TAB + "domain(" + visitor.result() + ")"); 
			else print(TAB + "<i>domain</i>(" + visitor.result() + ")");
			// 	    if (it.hasNext()) {
			// 		println();
			// 	    }
		}
		for (Iterator it = reasoner.rangesOf(prop).iterator(); it.hasNext();) {
			println();
			OWLDescription ran = (OWLDescription) it.next();
			visitor.reset();
			ran.accept(visitor);
			if (editorEnabled) print(TAB + "range(" + visitor.result() + ")");
			else print(TAB + "<i>range</i>(" + visitor.result() + ")");
		}
// Added for rendering rules
		if (swoopModel.getEnableRules()) {
			Set rules = swoopModel.getRuleExpr().getRules(prop);
			for (Iterator it = rules.iterator(); it.hasNext();) {
				OWLRule rule = ((RuleValue) it.next()).getRule();
				println("\n \t" + "Implies(" + "Antecedent(");
				Set antecedents = rule.getAntecedents();
				for (Iterator it1 = antecedents.iterator(); it1.hasNext();) {
					OWLRuleAtom antAtom = (OWLRuleAtom) it1.next();
					renderAtom(antAtom);
					//				visitor.reset();
					//				antAtom.accept((OWLRuleAtomVisitor) visitor);
					//				print(" "+visitor.result());
				}
				println(")");
				println("\t" + "Consequent(");
				OWLRuleAtom consAtom = (OWLRuleAtom) rule.getConsequents()
						.iterator().next();
				//visitor.reset();
				//consAtom.accept((OWLRuleAtomVisitor) visitor);
				//print(" "+visitor.result());
				renderAtom(consAtom);
				println(")");
				println(")");
			}
		}
		println("\n)");
	}
	
	protected void renderTextObjectProperty(OWLObjectProperty prop) throws OWLException {
	    //*****************************************
		//Changed for Econnections	
		//****************************************
		if(!prop.isLink()){
			print("ObjectProperty(" + shortForm(prop.getURI()));}
		else{
			print("LinkProperty(" + shortForm(prop.getURI()));
			print(" foreignOntology(" + shortForm(prop.getLinkTarget()) + ")");
			
		}
		if (prop.isTransitive(reasoner.getOntologies())) {
			print(" Transitive");
		}
		if (prop.isFunctional(reasoner.getOntologies())) {
			print(" Functional");
		}
		if (prop.isInverseFunctional(reasoner.getOntologies())) {
			print(" InverseFunctional");
		}
		if (prop.isSymmetric(reasoner.getOntologies())) {
			print(" Symmetric");
		}
		for (Iterator it = prop.getInverses(reasoner.getOntologies()).iterator(); it.hasNext();) {
			println();
			OWLObjectProperty inv = (OWLObjectProperty) it.next();
			visitor.reset();
			inv.accept(visitor);
			print(TAB + "inverseOf(" + visitor.result() + ")");
		}
		for (Iterator it = reasoner.superPropertiesOf(prop).iterator(); it.hasNext();) {
			println();
			try {
				
				// superPropertiesOf returns a set of sets,that's why another iterator is needed
				for (Iterator it1 = ((Set) it.next()).iterator(); it1.hasNext();) {
					Object Obj = it1.next();
					if (Obj instanceof OWLObjectProperty) {									
						OWLObjectProperty sup = (OWLObjectProperty)Obj;
						visitor.reset();
						sup.accept(visitor);
						print(TAB + "super(" + visitor.result() + ")");
					}
				
				}	
			}
			catch (Exception e) {
				System.out.println(e.getMessage().toString());
			}
		}
		
		for (Iterator it = reasoner.domainsOf(prop).iterator(); it.hasNext();) {
			println();
			OWLDescription dom = (OWLDescription) it.next();
			visitor.reset();
			dom.accept(visitor);
			print(TAB + "domain(" + visitor.result() + ")");
			// 	    if (it.hasNext()) {
			// 		println();
			// 	    }
		}
		for (Iterator it = reasoner.rangesOf(prop).iterator(); it.hasNext();) {
			println();
			OWLDescription ran = (OWLDescription) it.next();
			visitor.reset();
			ran.accept(visitor);
			print(TAB + "range(" + visitor.result() + ")");
		}
// Added for rendering rules
		if (swoopModel.getEnableRules()) {
			Set rules = swoopModel.getRuleExpr().getRules(prop);
			for (Iterator it = rules.iterator(); it.hasNext();) {
				OWLRule rule = (OWLRule) it.next();
				println("\n \t" + "Implies(" + "Antecedent(");
				Set antecedents = rule.getAntecedents();
				for (Iterator it1 = antecedents.iterator(); it1.hasNext();) {
					OWLRuleAtom antAtom = (OWLRuleAtom) it1.next();
					renderAtom(antAtom);
					//				visitor.reset();
					//				antAtom.accept((OWLRuleAtomVisitor) visitor);
					//				print(" "+visitor.result());
				}
				println(")");
				println("\t" + "Consequent(");
				OWLRuleAtom consAtom = (OWLRuleAtom) rule.getConsequents()
						.iterator().next();
				//visitor.reset();
				//consAtom.accept((OWLRuleAtomVisitor) visitor);
				//print(" "+visitor.result());
				renderAtom(consAtom);
				println(")");
				println(")");
			}
		}
		println("\n)");
	}
	
	protected void renderPropertyAxiom(OWLPropertyAxiom axiom) throws OWLException{
        visitor.reset();        
        axiom.accept(visitor);
        pw.println(visitor.result());
    }

	protected void renderClassAxiom(OWLClassAxiom axiom) throws OWLException{
        visitor.reset();        
        axiom.accept(visitor);
        pw.println(visitor.result());
    }

	protected void renderTextPropertyAxiom(OWLPropertyAxiom axiom) throws OWLException{
        visitor.reset();        
        axiom.accept(visitor);
        pw.println(visitor.result());
    }

	protected void renderTextClassAxiom(OWLClassAxiom axiom) throws OWLException{
        visitor.reset();        
        axiom.accept(visitor);
        pw.println(visitor.result());
    }


	protected void renderDataProperty(OWLDataProperty prop) throws OWLException {
		print("DatatypeProperty(" + shortForm(prop.getURI()));
		if (prop.isFunctional(reasoner.getOntologies())) {
			if (editorEnabled) print(" Functional");
			else print(" <i>Functional</i>");
		}

		for (Iterator it = reasoner.domainsOf(prop).iterator(); it.hasNext();) {
			println();
			OWLDescription dom = (OWLDescription) it.next();
			visitor.reset();
			dom.accept(visitor);
			if (editorEnabled) print(TAB + "domain(" + visitor.result() + ")"); 
			else print(TAB + "<i>domain</i>(" + visitor.result() + ")");
			// 	    if (it.hasNext()) {
			// 		println();
			// 	    }
		}
		for (Iterator it = reasoner.rangesOf(prop).iterator(); it.hasNext();) {
			println();
			OWLDataRange ran = (OWLDataRange) it.next();
			visitor.reset();
			ran.accept(visitor);
			if (editorEnabled) print(TAB + "range(" + visitor.result() + ")");
			else print(TAB + "<i>range</i>(" + visitor.result() + ")");
		}
		if (swoopModel.getEnableRules()) {
			Set rules = swoopModel.getRuleExpr().getRules(prop);
			for (Iterator it = rules.iterator(); it.hasNext();) {
				OWLRule rule = (OWLRule) it.next();
				println("\n \t" + "Implies(" + "Antecedent(");
				Set antecedents = rule.getAntecedents();
				for (Iterator it1 = antecedents.iterator(); it1.hasNext();) {
					OWLRuleAtom antAtom = (OWLRuleAtom) it1.next();
					renderAtom(antAtom);
					//				visitor.reset();
					//				antAtom.accept((OWLRuleAtomVisitor) visitor);
					//				print(" "+visitor.result());
				}
				println(")");
				println("\t" + "Consequent(");
				OWLRuleAtom consAtom = (OWLRuleAtom) rule.getConsequents()
						.iterator().next();
				//visitor.reset();
				//consAtom.accept((OWLRuleAtomVisitor) visitor);
				//print(" "+visitor.result());
				renderAtom(consAtom);
				println(")");
				println(")");
			}
		}
		println("\n)");

	}
	
	protected void renderTextDataProperty(OWLDataProperty prop) throws OWLException {
		print("DatatypeProperty(" + shortForm(prop.getURI()));
		if (prop.isFunctional(reasoner.getOntologies())) {
			print(" Functional");
		}

		for (Iterator it = reasoner.domainsOf(prop).iterator(); it.hasNext();) {
			println();
			OWLDescription dom = (OWLDescription) it.next();
			visitor.reset();
			dom.accept(visitor);
			print(TAB + "domain(" + visitor.result() + ")");
			// 	    if (it.hasNext()) {
			// 		println();
			// 	    }
		}
		for (Iterator it = reasoner.rangesOf(prop).iterator(); it.hasNext();) {
			println();
			OWLDataRange ran = (OWLDataRange) it.next();
			visitor.reset();
			ran.accept(visitor);
			print(TAB + "range(" + visitor.result() + ")");
		}
		if (swoopModel.getEnableRules()) {
			Set rules = swoopModel.getRuleExpr().getRules(prop);
			for (Iterator it = rules.iterator(); it.hasNext();) {
				OWLRule rule = ((RuleValue) it.next()).getRule();
				println("\n \t" + "Implies(" + "Antecedent(");
				Set antecedents = rule.getAntecedents();
				for (Iterator it1 = antecedents.iterator(); it1.hasNext();) {
					OWLRuleAtom antAtom = (OWLRuleAtom) it1.next();
					renderAtom(antAtom);
					//				visitor.reset();
					//				antAtom.accept((OWLRuleAtomVisitor) visitor);
					//				print(" "+visitor.result());
				}
				println(")");
				println("\t" + "Consequent(");
				OWLRuleAtom consAtom = (OWLRuleAtom) rule.getConsequents()
						.iterator().next();
				//visitor.reset();
				//consAtom.accept((OWLRuleAtomVisitor) visitor);
				//print(" "+visitor.result());
				renderAtom(consAtom);
				println(")");
				println(")");

			}
		}
		println("\n)");

	}

	protected void renderDataType(OWLDataType datatype) throws OWLException {
		if (editorEnabled) println("Datatype(" + shortForm(datatype.getURI()) + ")");
		else println("<b>Datatype</b>(" + escape(shortForm(datatype.getURI())) + ")");
	}
	
	//************************************************************
	//Added for Econnections
	//************************************************************
	protected void renderForeignEntity(OWLEntity ent) throws OWLException {
		if(ent instanceof OWLObjectProperty ){
            if(!((OWLObjectProperty)ent).isLink())		
				println("<b>ForeignObjectProperty</b>(" + escape(shortForm(ent.getURI())) + ")");
            else
            	println("<b>ForeignLinkProperty</b>(" + escape(shortForm(ent.getURI())) + ")");
		}
		if(ent instanceof OWLDataProperty )
			println("<b>ForeignDatatypeProperty</b>(" + escape(shortForm(ent.getURI())) + ")");
		if(ent instanceof OWLClass )
			println("<b>ForeignClass</b>(" + escape(shortForm(ent.getURI()))+ ")");
		 
		if(ent instanceof OWLIndividual )
			println("<b>ForeignIndividual</b>(" + escape(shortForm(ent.getURI())) + ")");
	
      pw.print(" foreignOntology(" + escape(shortForm(((URI)swoopModel.getSelectedOntology().getForeignEntities().get(ent)))) + ")");
			
	}
	
	
	//*************************************************************
	
	protected void println() {
		pw.println();		
	}
	
	protected void renderEntity() throws OWLException {
		if (!editorEnabled) {
			print("<html><body style='color: black; background-color: white;'>");
			print("<code><pre><FONT FACE=\""+swoopModel.getFontFace()+"\" SIZE="+fontSize+">");
		}
		// generateShortNames();
		
		this.baseURI = swoopModel.getSelectedOntology().getURI().toString();
		
		this.allURIs = getAllURIs(reasoner.getOntology());
		StringWriter sw = new StringWriter();
		PrintWriter buffer = new PrintWriter(sw);
		
		this.qnameProvider = new QNameShortFormProvider();
		generateShortNames();
		writeHeader();
		

		super.renderEntity();
		writeFooter(); 
		
		
		if (!editorEnabled) print("</FONT></pre></code></body></html>");
	}
	protected void renderAtom(OWLRuleAtom atom) throws OWLException {
		String result = "";
		try {
			if (atom instanceof OWLRuleClassAtom) {
				OWLDescription desc = ((OWLRuleClassAtom) atom)
						.getDescription();
				URI aux = ((OWLClass) desc).getURI();
				result = result = result.concat("\t" + "<a href=\"" + aux + "\">"
						+ shortForm(aux) + "</a>" + "(");
				OWLRuleIObject var = ((OWLRuleClassAtom) atom).getArgument();
				if (var instanceof OWLRuleIVariable) {
					URI aux1 = ((OWLRuleIVariable) var).getURI();
					result = result.concat(" I-variable(" +  shortForm(aux1) +  ")" + ")");
				} else {
					URI aux1 = ((OWLNamedObject) ((OWLRuleIndividual) var)
							.getIndividual()).getURI();
					result = result.concat(" <a href=\"" + aux1 + "\">"
							+ shortForm(aux1) + "</a>" + ")");
				}
			}
			if (atom instanceof OWLRuleObjectPropertyAtom) {
				OWLObjectProperty prop = ((OWLRuleObjectPropertyAtom) atom)
						.getProperty();
				URI aux = prop.getURI();
				result = result.concat("\t" + "<a href=\"" + aux + "\">"
						+ shortForm(aux) + "</a>" + "(");
				OWLRuleIObject var = ((OWLRuleObjectPropertyAtom) atom)
						.getFirstArgument();
				if (var instanceof OWLRuleIVariable) {
					URI aux1 = ((OWLRuleIVariable) var).getURI();
					result = result.concat(" I-variable(" + shortForm(aux1) +  ")" + ")");
				} else {
					URI aux1 = ((OWLNamedObject) ((OWLRuleIndividual) var)
							.getIndividual()).getURI();
					result = result.concat(" <a href=\"" + aux1 + "\">"
							+ shortForm(aux1) + "</a>" + ")");
				}
				OWLRuleIObject var2 = ((OWLRuleObjectPropertyAtom) atom)
						.getSecondArgument();
				if (var2 instanceof OWLRuleIVariable) {
					URI aux1 = ((OWLRuleIVariable) var2).getURI();
					result = result.concat(" I-variable(" + shortForm(aux1)  + ")" + ")");
				} else {
					URI aux1 = ((OWLNamedObject) ((OWLRuleIndividual) var2)
							.getIndividual()).getURI();
					result = result.concat(" <a href=\"" + aux1 + "\">"
							+ shortForm(aux1) + "</a>" + ")");
				}
			}
			if (atom instanceof OWLRuleDataPropertyAtom) {
				OWLDataProperty prop = ((OWLRuleDataPropertyAtom) atom)
						.getProperty();
				URI aux = prop.getURI();
				result = result.concat("\t" + "<a href=\"" + aux + "\">"
						+ shortForm(aux) + "</a>" + "(");
				OWLRuleDObject var = (OWLRuleDObject) ((OWLRuleDataPropertyAtom) atom)
						.getFirstArgument();
				if (var instanceof OWLRuleDVariable) {
					URI aux1 = ((OWLRuleIVariable) var).getURI();
					result = result.concat("I-variable(" + shortForm(aux1) +  ")" + ")");
				} else {
					URI aux1 = ((OWLNamedObject) ((OWLRuleIndividual) var)
							.getIndividual()).getURI();
					result = result.concat(" <a href=\"" + aux1 + "\">"
							+ shortForm(aux1) + "</a>" + ")");
				}
				OWLRuleDObject var2 = ((OWLRuleDataPropertyAtom) atom)
						.getSecondArgument();
				if (var2 instanceof OWLRuleIVariable) {
					URI aux1 = ((OWLRuleIVariable) var2).getURI();
					result = result.concat(" D-variable(" + shortForm(aux1) + ")" + ")");
				} else {
					String aux1 = ((OWLRuleDataValue) var).toString();
					result = result.concat(" " + aux1 + ")");
				}
			}
			if (atom instanceof OWLRuleEqualityAtom) {
				result = result.concat("\t" + "sameAs" + "(");
				OWLRuleIObject var = ((OWLRuleObjectPropertyAtom) atom)
						.getFirstArgument();
				if (var instanceof OWLRuleIVariable) {
					URI aux1 = ((OWLRuleIVariable) var).getURI();
					result = result.concat(" I-variable(" +  shortForm(aux1) +  ")" + ")");
				} else {
					URI aux1 = ((OWLNamedObject) ((OWLRuleIndividual) var)
							.getIndividual()).getURI();
					result = result.concat(" <a href=\"" + aux1 + "\">"
							+ shortForm(aux1) + "</a>" + ")");
				}
				OWLRuleIObject var2 = ((OWLRuleObjectPropertyAtom) atom)
						.getSecondArgument();
				if (var2 instanceof OWLRuleIVariable) {
					URI aux1 = ((OWLRuleIVariable) var2).getURI();
					result = result.concat(" I-variable(" + shortForm(aux1) +  ")" + ")");
				} else {
					URI aux1 = ((OWLNamedObject) ((OWLRuleIndividual) var2)
							.getIndividual()).getURI();
					result = result.concat(" <a href=\"" + aux1 + "\">"
							+ shortForm(aux1) + "</a>" + ")");
				}

			}
			if (atom instanceof OWLRuleInequalityAtom) {
				result = result.concat("\t" + "differentAs" + "(");
				OWLRuleIObject var = ((OWLRuleObjectPropertyAtom) atom)
						.getFirstArgument();
				if (var instanceof OWLRuleIVariable) {
					URI aux1 = ((OWLRuleIVariable) var).getURI();
					result = result.concat(" I-variable(" +  shortForm(aux1) +  ")" + ")");
				} else {
					URI aux1 = ((OWLNamedObject) ((OWLRuleIndividual) var)
							.getIndividual()).getURI();
					result = result.concat(" <a href=\"" + aux1 + "\">"
							+ shortForm(aux1) + "</a>" + ")");
				}
				OWLRuleIObject var2 = ((OWLRuleObjectPropertyAtom) atom)
						.getSecondArgument();
				if (var2 instanceof OWLRuleIVariable) {
					URI aux1 = ((OWLRuleIVariable) var2).getURI();
					result = result.concat(" I-variable(" + shortForm(aux1) +  ")" + ")");
				} else {
					URI aux1 = ((OWLNamedObject) ((OWLRuleIndividual) var2)
							.getIndividual()).getURI();
					result = result.concat(" <a href=\"" + aux1 + "\">"
							+ shortForm(aux1) + "</a>" + ")");
				}

			}
			print(result + "\n");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	protected void generateShortNames() {
        /* Generates a list of namespaces. */
        
        for (Iterator it = allURIs.iterator(); it.hasNext();) {
 	    URI uri = (URI) it.next();
	    String qname = qnameProvider.shortForm(uri, false);
        }
    }
	protected void writeHeader() {
						
		// 	System names
       for (Iterator it = qnameProvider.getPrefixSet().iterator(); it.hasNext();) {
	    String prefix = (String) it.next();
            String ns = qnameProvider.getURI(prefix);
            if (editorEnabled)
            	pw.println("Namespace(" + prefix + " = <" + ns + ">)");
            else 
            	pw.println("Namespace(" + escape(prefix) + " = &lt;" + escape(ns) + "&gt;)");
        }
       pw.println();
        if (editorEnabled)
        	pw.println("Ontology( <" + baseURI + ">");
        else
        	pw.println("Ontology( &lt;" + escape(baseURI) + "&gt;");
	
	}
	
	protected void writeTextHeader() {
				
		// 	System names
		for (Iterator it = qnameProvider.getPrefixSet().iterator(); it.hasNext();) {
		String prefix = (String) it.next();
		String ns = qnameProvider.getURI(prefix);
		pw.println("Namespace(" + prefix + " = <" + ns + ">)");
		}
		pw.println();
		pw.println("Ontology( <" + baseURI + ">");
		
	}
			
	
	
	/** Returns all the uris of all the entities in the ontology. */
	public static Set getAllURIs( OWLOntology ontology ) throws OWLException {
		Set allURIs = new HashSet();
		for ( Iterator cit = ontology.getClasses().iterator();
		cit.hasNext(); ) {
			OWLNamedObject entity = (OWLNamedObject) cit.next();
			allURIs.add(entity.getURI());
		}
		for ( Iterator cit = ontology.getIndividuals().iterator();
		cit.hasNext(); ) {
			OWLIndividual entity = (OWLIndividual) cit.next();
			if ( !entity.isAnonymous() ) {
				allURIs.add(entity.getURI());
			}
		}
		for ( Iterator cit = ontology.getObjectProperties().iterator();
		cit.hasNext(); ) {
			OWLNamedObject entity = (OWLNamedObject) cit.next();
			allURIs.add(entity.getURI());
		}
		for ( Iterator cit = ontology.getDataProperties().iterator();
		cit.hasNext(); ) {
			OWLNamedObject entity = (OWLNamedObject) cit.next();
			allURIs.add(entity.getURI());
		}
		for ( Iterator cit = ontology.getDatatypes().iterator();
		cit.hasNext(); ) {
			OWLDataType entity = (OWLDataType) cit.next();
			allURIs.add(entity.getURI());
		}
		for ( Iterator cit = ontology.getAnnotationProperties().iterator();
		cit.hasNext(); ) {
			OWLAnnotationProperty entity = (OWLAnnotationProperty) cit.next();
			allURIs.add(entity.getURI());
		}		
		return allURIs;
	}
	
	public String shortForm(URI uri) {
		
		String qname = uri.toString();
		try {	
			qname = qnameProvider.shortForm(uri, false);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		return qname;
    }
	
	
	protected void writeFooter() {
		pw.println(")");
		
	}
	
	/*
	 *  SwoopRenderer method
	 * 
	 */
	public Component getDisplayComponent( SwoopDisplayPanel panel )
	{		
		if (!(panel instanceof TermsDisplay ))
			throw new IllegalArgumentException();		
		if (!editorEnabled) {
			// return standard JEditorPane
			return super.getEditorPane( this.getContentType(), (TermsDisplay)panel );
		}
		else 
		{
		 	JEditorPane editorPane = null;
			if(contentType.equals("text/plain")) {
				editorPane = new JEditorPane();
				editorPane.setFont(new Font("Verdana", Font.PLAIN, 12));
			}
			else if(contentType.equals("text/html")) {
				editorPane = new JEditorPane();
				editorPane.addHyperlinkListener( (TermsDisplay)panel );	
			}
			else if(contentType.equals("text/xml"))
				editorPane = new JEditorPane();
			else
				throw new RuntimeException("Cannot create an editor pane for content type " + contentType);
			
			editorPane.setEditable(true);
			editorPane.setContentType(contentType);
			
			// adding to UI listeners of TermsDisplay
			//editorPane.getDocument().addDocumentListener((TermsDisplay)panel);
			editorPane.addMouseListener((TermsDisplay)panel);			
			editorPane.addKeyListener((TermsDisplay)panel);
			
			return editorPane;
		}
		
		
	}
	
	
	
	public boolean isEditableText() {

		if (contentType.equals("text/html")) return true;
		else return false;
	}
	
	
	public void decreaseINDENT() {
		INDENT = INDENT.substring(0, INDENT.length() - TAB.length());
	}
	
	public void increaseINDENT() {
		INDENT = INDENT + TAB;
	}
	
	public void setNoLinks(boolean noLinks) {
		((AbstractSyntaxVisitor) visitor).noLinks = noLinks;
	}
}