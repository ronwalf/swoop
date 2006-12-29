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
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JEditorPane;

import org.apache.commons.lang.StringEscapeUtils;
import org.mindswap.swoop.SwoopDisplayPanel;
import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.TermsDisplay;
import org.mindswap.swoop.renderer.BaseEntityRenderer;
import org.mindswap.swoop.renderer.SwoopEditableRenderer;
import org.mindswap.swoop.renderer.SwoopEntityRenderer;
import org.mindswap.swoop.renderer.SwoopRenderingVisitor;
import org.mindswap.swoop.utils.owlapi.DefaultShortFormProvider;
import org.mindswap.swoop.utils.owlapi.OWLDescriptionFinder;
import org.mindswap.swoop.utils.owlapi.QNameShortFormProvider;
import org.semanticweb.owl.io.RendererException;
import org.semanticweb.owl.io.ShortFormProvider;
import org.semanticweb.owl.model.OWLAnd;
import org.semanticweb.owl.model.OWLAnnotationInstance;
import org.semanticweb.owl.model.OWLAnnotationProperty;
import org.semanticweb.owl.model.OWLBooleanDescription;
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
import org.semanticweb.owl.model.OWLPropertyDomainAxiom;
import org.semanticweb.owl.model.OWLRestriction;
import org.semanticweb.owl.model.OWLSameIndividualsAxiom;
import org.semanticweb.owl.model.OWLSubClassAxiom;
import org.semanticweb.owl.model.OWLSubPropertyAxiom;
import org.semanticweb.owl.model.OWLSymmetricPropertyAxiom;
import org.semanticweb.owl.model.OWLTransitivePropertyAxiom;
import org.semanticweb.owl.model.helper.OntologyHelper;

/**
 * @author Dave
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class TurtleEntityRenderer extends BaseEntityRenderer implements SwoopEditableRenderer,  SwoopEntityRenderer {
	
	protected class TurtleVisitor implements org.mindswap.swoop.renderer.SwoopRenderingVisitor {		
		ShortFormProvider shortForms; 
		boolean indenting = true;
		StringWriter sw;
		PrintWriter pw;
		
		int indentLevel = 0;
		
		public TurtleVisitor(ShortFormProvider shortForms) {
			this.shortForms = shortForms;
			reset();
		}
		
		public String result() {
			return sw.toString();
		}

		public void reset() {
			sw = new StringWriter();
			pw = new PrintWriter( sw );
		}
		
		// done
		public void visit( OWLDataValue cd ) throws OWLException {
			pw.print( "\"" + escape(cd.getValue())  + "\"");
			/* Only show it if it's not string */
			URI dvdt = cd.getURI();
			// enter the data value URI into the qname provider so we can print its prefix out properl
			myQNameProvider.shortForm(dvdt);
			
			String dvlang = cd.getLang();
			if ( dvdt!=null) {
				pw.print( "^^" + encodeHLink(dvdt, shortForms.shortForm(dvdt)));
			} else {
				if (dvlang!=null) {
					pw.print( "@" + escape(dvlang) );
				}
			}
		}

		// done
		public void visit( OWLAnd and ) throws OWLException {
			try
			{		
				URI uri = new URI(OWL_INTERSECTIONOF);
				pw.print( encodeHLink(uri, shortForms.shortForm(uri)) + TAB + "("+ SPACE);
				pw.print(BREAK);
				indentLevel ++;
				for ( Iterator it = and.getOperands().iterator();it.hasNext(); ) {
					OWLDescription desc = (OWLDescription) it.next();
					System.out.println( desc );
					pw.println(getIndentString());
					desc.accept( this );
					if (it.hasNext()) {
						pw.println(BREAK);
					}
				}
				indentLevel --;
				pw.print(SPACE + ")" + SPACE);
			}
			catch (Exception ex)
			{ ex.printStackTrace(); }
		}

		// done 
		public void visit( OWLOr or ) throws OWLException {	
			try
			{	
				URI uri = new URI(OWL_UNIONOF);
				pw.print( encodeHLink(uri, shortForms.shortForm(uri)) + TAB + "("+ SPACE);
				pw.print(BREAK);
				indentLevel ++;
				for ( Iterator it = or.getOperands().iterator();it.hasNext(); ) {
					OWLDescription desc = (OWLDescription) it.next();
					pw.print(getIndentString());
					desc.accept( this );
					if (it.hasNext()) {
						pw.println(BREAK);
					}
				}
				indentLevel --;
				pw.print(SPACE + ")" + SPACE);
			}
			catch (Exception ex)
			{ ex.printStackTrace(); }
		}

		// done
		public void visit( OWLNot not ) throws OWLException {			
			try
			{					
				URI uri = new URI(OWL_COMPLEMENTOF);
				pw.print( encodeHLink(uri, shortForms.shortForm(uri)) + TAB + SPACE);
				pw.print(BREAK);
				indentLevel ++;
				OWLDescription desc = not.getOperand();
				pw.print(getIndentString());
				desc.accept(this);
				indentLevel --;
				pw.print(SPACE + SPACE);
			}
			catch (Exception ex)			
			{ ex.printStackTrace(); }
		}

		// done
		public void visit( OWLEnumeration enumeration ) throws OWLException {
			try
			{					
				URI uri = new URI(OWL_ONEOF);
				pw.print( encodeHLink(uri, shortForms.shortForm(uri)) + TAB + "("+ SPACE);
				pw.print(BREAK);
				indentLevel ++;
				for ( Iterator it = enumeration.getIndividuals().iterator();it.hasNext(); ) {
					OWLIndividual ind = (OWLIndividual) it.next();
					pw.print(getIndentString());
					ind.accept( this );
					if (it.hasNext()) {
						pw.print(BREAK);
					}
				}
				indentLevel --;
				pw.print(SPACE + ")" + SPACE);
			}
			catch (Exception ex)			
			{ ex.printStackTrace(); }
		}

		// DONE
		public void visit( OWLObjectSomeRestriction restriction ) throws OWLException {
			try
			{
				pw.print("[ "+SPACE);
				URI uri = new URI(OWL_RESTRICTION);
				pw.print( encodeHLink(RDF_TYPE_URI, "a") + TAB +
					   encodeHLink(uri, shortForms.shortForm(uri)) + ";" + BREAK);				
				uri = new URI(OWL_ONPROPERTY);
				print( SPACE + SPACE + encodeHLink(uri, shortForms.shortForm(uri)) + TAB);
				restriction.getObjectProperty().accept( this );
				pw.print(";"+BREAK);  // avoid indentation
				uri = new URI(OWL_SOMEVALUESFROM);
				print( SPACE + SPACE + encodeHLink(uri, shortForms.shortForm(uri)) + TAB);			
				OWLDescription desc = restriction.getDescription();
				//System.out.println( "Obj Restriction: " +desc.getClass().toString() );
				if (!(desc instanceof OWLClass) && !(desc instanceof OWLRestriction))
					pw.print("[ ");
				if (desc instanceof OWLRestriction){
					indentLevel ++;
					pw.print(BREAK + getIndentString());
				}
				desc.accept( this );
				if (!(desc instanceof OWLClass) && !(desc instanceof OWLRestriction))
					pw.print(" ]");
				if (desc instanceof OWLRestriction){
					indentLevel --;
				}
				println();
				print(" ]");
			}
			catch (Exception ex)
			{ ex.printStackTrace(); }
		}

		// done
		public void visit( OWLObjectAllRestriction restriction ) throws OWLException {
			try
			{
				pw.print("[ "+SPACE);
				URI uri = new URI(OWL_RESTRICTION);
				pw.print( encodeHLink(RDF_TYPE_URI, "a") + TAB +
					   encodeHLink(uri, shortForms.shortForm(uri)) + ";" + BREAK);				
				uri = new URI(OWL_ONPROPERTY);
				print( SPACE + SPACE + encodeHLink(uri, shortForms.shortForm(uri)) + TAB);
				restriction.getObjectProperty().accept( this );
				pw.print(";"+BREAK);  // avoid indentation
				uri = new URI(OWL_ALLVALUESFROM);
				print( SPACE + SPACE + encodeHLink(uri, shortForms.shortForm(uri)) + TAB);				
				OWLDescription desc = restriction.getDescription();
				if (!(desc instanceof OWLClass) && !(desc instanceof OWLRestriction))
					pw.print("[ ");
				if (desc instanceof OWLRestriction){
					indentLevel ++;
					pw.print(BREAK + getIndentString());
				}
				
				desc.accept( this );
				if (!(desc instanceof OWLClass) && !(desc instanceof OWLRestriction))
					pw.print(" ]");
				if (desc instanceof OWLRestriction){
					indentLevel --;
				}

				println();
				print(" ]");
			}
			catch (Exception ex)
			{ ex.printStackTrace(); }
		}

		// done	
		public void visit( OWLObjectValueRestriction restriction ) throws OWLException {
			try
			{
				pw.print("[ "+SPACE);
				URI uri = new URI(OWL_RESTRICTION);
				pw.print( encodeHLink(RDF_TYPE_URI, "a") + TAB +
					   encodeHLink(uri, shortForms.shortForm(uri)) + ";" + BREAK);				
				uri = new URI(OWL_ONPROPERTY);
				print( SPACE + SPACE + encodeHLink(uri, shortForms.shortForm(uri)) + TAB);
				restriction.getObjectProperty().accept( this );
				pw.print(";"+BREAK);  // avoid indentation
				uri = new URI(OWL_HASVALUE);
				print( SPACE + SPACE + encodeHLink(uri, shortForms.shortForm(uri)) + TAB);
				restriction.getIndividual().accept( this );
				println();
				print(" ]");
			}
			catch (Exception ex)
			{ ex.printStackTrace(); }
		}

		// done
		public void visit( OWLDataSomeRestriction restriction ) throws OWLException {
			try
			{
				pw.print("[ "+SPACE);
				URI uri = new URI(OWL_RESTRICTION);
				pw.print( encodeHLink(RDF_TYPE_URI, "a") + TAB +
					   encodeHLink(uri, shortForms.shortForm(uri)) + ";" + BREAK);				
				uri = new URI(OWL_ONPROPERTY);
				print( SPACE + SPACE + encodeHLink(uri, shortForms.shortForm(uri)) + TAB);
				restriction.getDataProperty().accept( this );
				pw.print(";"+BREAK);  // avoid indentation
				uri = new URI(OWL_SOMEVALUESFROM);
				print( SPACE + SPACE + encodeHLink(uri, shortForms.shortForm(uri)) + TAB);			
				OWLDataRange ran = restriction.getDataType();
				if (!(ran instanceof OWLClass))
					pw.print("[ ");
				ran.accept( this );
				if (!(ran instanceof OWLClass))
					pw.print(" ]");				
				println();
				print(" ]");
			}
			catch (Exception ex)
			{ ex.printStackTrace(); }
		}

		// done
		public void visit( OWLDataAllRestriction restriction ) throws OWLException {
			try
			{
				pw.print("[ "+SPACE);
				URI uri = new URI(OWL_RESTRICTION);
				pw.print( encodeHLink(RDF_TYPE_URI, "a") + TAB +
					   encodeHLink(uri, shortForms.shortForm(uri)) + ";" + BREAK);				
				uri = new URI(OWL_ONPROPERTY);
				print( SPACE + SPACE + encodeHLink(uri, shortForms.shortForm(uri)) + TAB);
				restriction.getDataProperty().accept( this );
				pw.print(";"+BREAK);  // avoid indentation
				uri = new URI(OWL_ALLVALUESFROM);
				print( SPACE + SPACE + encodeHLink(uri, shortForms.shortForm(uri)) + TAB);			
				OWLDataRange ran = restriction.getDataType();
				if (!(ran instanceof OWLClass))
					pw.print("[ ");
				ran.accept( this );
				if (!(ran instanceof OWLClass))
					pw.print(" ]");
				println();
				print(" ]");
			}
			catch (Exception ex)
			{ ex.printStackTrace(); }
		}

		// done
		public void visit( OWLObjectCardinalityRestriction restriction ) throws OWLException {
			try
			{			
				pw.print("[ " + SPACE);
				URI uri = new URI(OWL_RESTRICTION);
				pw.print( encodeHLink(RDF_TYPE_URI, "a") + TAB +
						encodeHLink(uri, shortForms.shortForm(uri)) + ";" + BREAK);			
				uri = new URI(OWL_ONPROPERTY);
				print( SPACE + SPACE + encodeHLink(uri, shortForms.shortForm(uri)) + TAB);				
				restriction.getObjectProperty().accept( this );				
				pw.print(";"+BREAK);  // avoid syntax indentation
				
				if ( restriction.isExactly() ) {
					uri = new URI(OWL_CARDINALITY);
					print( SPACE + SPACE + encodeHLink(uri, shortForms.shortForm(uri)) + TAB + restriction.getAtLeast());
				} else if ( restriction.isAtMost() ) {
					uri = new URI(OWL_MAXCARDINALITY);
					print( SPACE + SPACE + encodeHLink(uri, shortForms.shortForm(uri)) + TAB + restriction.getAtMost());
				} else 	if ( restriction.isAtLeast() ) {
					uri = new URI(OWL_MINCARDINALITY);
					print( SPACE + SPACE + encodeHLink(uri, shortForms.shortForm(uri)) + TAB + restriction.getAtLeast());
				}
				pw.print(BREAK);
				print(" ]");
			}
			catch(Exception ex)
			{ ex.printStackTrace(); }		
		}

		// done
		public void visit( OWLDataCardinalityRestriction restriction ) throws OWLException {
			try
			{		
				pw.print("[ " + SPACE);
				URI uri = new URI(OWL_RESTRICTION);
				pw.print( encodeHLink(RDF_TYPE_URI, "a") + TAB +
						encodeHLink(uri, shortForms.shortForm(uri)) + ";" + BREAK);			
				uri = new URI(OWL_ONPROPERTY);
				print( SPACE + SPACE + encodeHLink(uri, shortForms.shortForm(uri)) + TAB);				
				restriction.getDataProperty().accept( this );				
				pw.print(";"+BREAK);  // avoid syntax indentation
				
				if ( restriction.isExactly() ) {
					uri = new URI(OWL_CARDINALITY);
					print( SPACE + SPACE + encodeHLink(uri, shortForms.shortForm(uri)) + TAB + restriction.getAtLeast());
				} else if ( restriction.isAtMost() ) {
					uri = new URI(OWL_MAXCARDINALITY);
					print( SPACE + SPACE + encodeHLink(uri, shortForms.shortForm(uri)) + TAB + restriction.getAtMost());
				} else 	if ( restriction.isAtLeast() ) {
					uri = new URI(OWL_MINCARDINALITY);
					print( SPACE + SPACE + encodeHLink(uri, shortForms.shortForm(uri)) + TAB + restriction.getAtLeast());
				}
				pw.print(BREAK);
				print(" ]");
			}
			catch(Exception ex)
			{ ex.printStackTrace(); }
		}

		// done	
		public void visit( OWLDataValueRestriction restriction ) throws OWLException {
			try
			{		
				pw.print("[ " + SPACE);
				URI uri = new URI(OWL_RESTRICTION);
				pw.print( encodeHLink(RDF_TYPE_URI, "a") + TAB +
						encodeHLink(uri, shortForms.shortForm(uri)) + ";" + BREAK);			
				uri = new URI(OWL_ONPROPERTY);
				print( SPACE + SPACE + encodeHLink(uri, shortForms.shortForm(uri)) + TAB);
				restriction.getDataProperty().accept( this );
				
				pw.print(";"+BREAK);  // avoid syntax indentation
				uri = new URI(OWL_HASVALUE);
				print( SPACE + SPACE + encodeHLink(uri, shortForms.shortForm(uri)) + TAB);
				restriction.getValue().accept( this );
				println();
				print(" ]");
			}
			catch(Exception ex)
			{ ex.printStackTrace(); }
		}

		public void visit( OWLEquivalentClassesAxiom axiom ) throws OWLException {
			pw.print("!!OWLEquivalentClassesAxiom!!");
			/*
			try
			{
				URI uri = new URI(OWL_EQUIVALENTCLASS);
				for ( Iterator it = axiom.getEquivalentClasses().iterator(); it.hasNext(); ) {
					OWLDescription eq = (OWLDescription)it.next();
					print( encodeHLink(uri, myQNameProvider.shortForm(uri)) + TAB);
					reset();
					if (!(eq instanceof OWLClass))  // only indent if eq is an anon class
					{
						indentLevel ++;
						pw.print(BREAK + getIndentString());
					}
					eq.accept( this );		
					if (!(eq instanceof OWLClass)) // un-indent
						indentLevel --;
					pw.print(result() + ";" + BREAK);
				}
			}			
			catch (Exception ex)
			{ ex.printStackTrace(); }
			*/
		}

		public void visit( OWLDisjointClassesAxiom axiom ) throws OWLException {
			pw.print("!!OWLDisjointClassesAxiom!!");
			/*
			try
			{
				URI uri = new URI(OWL_DISJOINTWITH);
				for ( Iterator it = axiom.getDisjointClasses().iterator(); it.hasNext(); ) 
				{				
					OWLDescription desc = (OWLDescription)it.next();
					reset();
					desc.accept( this );
					print(encodeHLink(uri, myQNameProvider.shortForm(uri)) + TAB + result() + ";" + BREAK);
					
				}
			}
			catch (Exception ex)
			{ ex.printStackTrace(); }
			*/
		}

		// done ... untested
		public void visit( OWLSubClassAxiom axiom ) throws OWLException {
			pw.print("!!OWLSubClassAxiom!!");
/*
			pw.print(" ????? SubClassOf(");
			axiom.getSubClass().accept( this );
			pw.print(" ");
			axiom.getSuperClass().accept( this );
			pw.print(") ??????");
			*/
		}

		public void visit( OWLEquivalentPropertiesAxiom axiom ) throws OWLException {
			pw.print("!!OWLEquivalentPropertiesAxiom!!");
			/*
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
			*/
		}

		public void visit( OWLSubPropertyAxiom axiom ) throws OWLException {
			pw.print("!!OWLSubPropertyAxiom!!");
			/*
			pw.print("SubPropertyOf(");
			axiom.getSubProperty().accept( this );
			pw.print(" ");
			axiom.getSuperProperty().accept( this );
			pw.print(")");
			*/
		}

		public void visit( OWLDifferentIndividualsAxiom ax) throws OWLException {
			pw.print("!!OWLDifferentIndividualsAxiom!!");
			/*
			pw.print("DifferentIndividuals(");
			for ( Iterator it = ax.getIndividuals().iterator(); it.hasNext(); ) {
				OWLIndividual desc = (OWLIndividual) it.next();
				desc.accept( this );
				if (it.hasNext()) {
					pw.print(" ");
				}
			}
			pw.print(")");
			*/
		}

		public void visit( OWLSameIndividualsAxiom ax) throws OWLException {
			pw.print("!!OWLSameIndividualsAxiom!!");
			/*
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
			*/
		}

		// done
		public void visit( OWLDataType ocdt ) throws OWLException {
			pw.print( encodeHLink(ocdt.getURI(), shortForms.shortForm(ocdt.getURI())) );
		}

		// done
		public void visit( OWLDataEnumeration enumeration ) throws OWLException {
			try
			{					
				URI uri = new URI(OWL_ONEOF);
				print( encodeHLink(uri, shortForms.shortForm(uri)) + TAB + "("+ SPACE);
				indentLevel ++;
				for ( Iterator it = enumeration.getValues().iterator();it.hasNext(); ) {
					OWLDescription desc = (OWLDescription) it.next();
					pw.print(getIndentString());
					desc.accept( this );
					if (it.hasNext()) {
						println(BREAK);
					}
				}
				indentLevel --;
				pw.print(SPACE + ")" + SPACE);
			}
			catch (Exception ex)
			{ throw new OWLException(ex.toString()); }
		}
		
		// done
		public void visit( OWLClass clazz ) throws OWLException {
			pw.print( encodeHLink(clazz.getURI(), shortForms.shortForm( clazz.getURI() )));
		}
		
		// done
		public void visit( OWLIndividual ind ) throws OWLException {
			if ( ind.isAnonymous() ) {
				pw.print( "[ ]" );  // anonymous individual
			} else {
				pw.print( encodeHLink(ind.getURI(), shortForms.shortForm(ind.getURI())) );
			}
		}

		// done
		public void visit( OWLObjectProperty prop ) throws OWLException {
			pw.print( encodeHLink(prop.getURI(), shortForms.shortForm( prop.getURI())) );
		}
		
		// done
		public void visit( OWLDataProperty prop ) throws OWLException {
			pw.print( encodeHLink(prop.getURI(), shortForms.shortForm( prop.getURI())) );
		}

		public void visit(OWLAnnotationProperty arg0) throws OWLException {
			// TODO Auto-generated method stub
			
		}

		public void visit(OWLAnnotationInstance arg0) throws OWLException {
			// TODO Auto-generated method stub
			
		}

		public void visit(OWLFrame arg0) throws OWLException {
			// TODO Auto-generated method stub
			
		}

		public void visit(OWLOntology arg0) throws OWLException {
			// TODO Auto-generated method stub
			
		}
		
		private String getIndentString()
		{
			String indentation = "";
			for (int i = 0; i < indentLevel; i ++)
				indentation = indentation + TAB;			
			return indentation;
		}
				
		// private convenient printing functions to use correct indentation
		private void print(String toBeWritten)
		{ pw.print(getIndentString() + toBeWritten); }
		
		private void println(String toBeWritten)
		{  pw.println(getIndentString()+ toBeWritten + BREAK); }
		
		private void println()
		{  pw.print(BREAK); }

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
	
	
	protected class TurtleShortFormProvider extends DefaultShortFormProvider
	{
		 public String shortForm(URI uri) 
		 {
		 	String name = super.shortForm( uri );
		 	if ( name.indexOf(":") == -1 )
		 		name = ":" + name;
		 	return name;
		 }
	}
	
	private static final String TURTLE_NAME = "Turtle";
	private static final String TURTLE_CONTENT_TYPE = "text/html";
	private static final String TAB = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
	private static final String SPACE = "&nbsp;";
	private static final String BREAK = "<br>";
	private static final String LT = "&lt;";
	private static final String GT = "&gt;";	
	
	private List myUserNS;
	private Set  myAllURIs;
	private QNameShortFormProvider myQNameProvider;
	private SwoopModel myModel;
	private static final String PERIOD = "  .";
	
	public void render(OWLEntity entity, SwoopModel swoopModel, Writer writer) throws RendererException {
	    try{
	    	fontSize = swoopModel.getFontSize();
	    	OWLOntology ontology = swoopModel.getSelectedOntology();
	    	URI logical = ontology.getLogicalURI();
	    	String defaultNS = logical + "#";
	    	//System.out.println( defaultNS );
	    	
	    	this.myAllURIs = OntologyHelper.allURIs(ontology);
	    	this.myQNameProvider = new QNameShortFormProvider();
	    	this.myModel = swoopModel;
			StringWriter sw = new StringWriter();
			PrintWriter buffer = new PrintWriter(sw);
			StringWriter sw2 = new StringWriter();
			PrintWriter buffer2 = new PrintWriter(sw2);
	    	
			buffer2.println("<FONT FACE=\""+swoopModel.getFontFace()+"\" SIZE="+fontSize+">" );
			
	        super.render(entity, swoopModel, buffer);	        
			// Actually used Namespace declarations
			for (Iterator it = myQNameProvider.touchedPrefixes.keySet().iterator(); it.hasNext();){
				String prefix = (String) it.next();
				String ns = (String) myQNameProvider.touchedPrefixes.get(prefix);
				if (!editorEnabled) buffer2.println("@prefix " + escape(prefix) + ": " + LT + "<a href=" + escape(ns) + ">"+escape(ns)+"</a>"+GT + PERIOD + BREAK);
				else buffer2.println("@prefix " + escape(prefix) + ": " + LT + escape(ns)+GT + PERIOD + BREAK);
			}
			// if showing fulll name, add prefix
			if (!myModel.getShowQNames())
				if (!editorEnabled) buffer2.println("@prefix : " + LT + "<a href=" + escape(defaultNS) + ">" + escape(defaultNS) + "</a>"+GT + PERIOD + BREAK );
				else buffer2.println("@prefix : " + LT + escape(defaultNS) +GT + PERIOD + BREAK );
			
	        String turtleText = repairForEndOfStatement(sw.getBuffer().toString()) + BREAK +
								getOtherStatements(entity) + "</FONT>";
	        
			buffer2.println(BREAK);
			String headerText = sw2.getBuffer().toString();
	        writer.write(headerText + turtleText);
	    }
	    catch (OWLException ex){
	    	ex.printStackTrace();
	    }
	    catch (IOException ex){
	    	ex.printStackTrace();
	    }
	  }	
	
	
	public SwoopRenderingVisitor createVisitor() {
		if (myModel.getShowQNames()) return new TurtleVisitor(this.myQNameProvider);
		return  new TurtleVisitor( new TurtleShortFormProvider() );
	}
	
	protected void renderAnnotationProperty(OWLAnnotationProperty prop) throws OWLException {
		// TODO Auto-generated method stub
	}

	/* Well dodgy coding */
	protected void renderAnnotationContent(Object o) throws OWLException {
		if (o instanceof URI) {
			print( encodeHLink((URI)o, myQNameProvider.shortForm((URI)o)) );
		} else if (o instanceof OWLIndividual)
		{
			TurtleVisitor TV = (TurtleVisitor)visitor;
			OWLIndividual ind = (OWLIndividual)o;
			ind.accept(TV);
			print( TV.result() );
		}
		else if (o instanceof OWLDataValue) {
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
			print("\"" + escape(o) + "\"");
		}
	}	
	
	/** Render the annotations for an object */
	protected void renderAnnotations(OWLNamedObject object ) throws OWLException {
		/* Bit nasty this -- annotations result in a new axiom */
		TurtleVisitor TV = (TurtleVisitor)visitor;		
		String indentation = TV.getIndentString();
		
		if (!object.getAnnotations(reasoner.getOntology()).isEmpty()) {
			for (Iterator it = object.getAnnotations(reasoner.getOntology()).iterator(); it.hasNext();) {
				OWLAnnotationInstance oai = (OWLAnnotationInstance) it.next();
				print(indentation + encodeHLink(oai.getProperty().getURI(), myQNameProvider.shortForm(oai.getProperty().getURI())) + TAB);
				/* Just whack out the content. This isn't quite right... */
				renderAnnotationContent(oai.getContent());
				if (it.hasNext())
					print(";"+BREAK);
				/* Do we need to do this??? */
				visitor.reset();
				oai.accept(visitor);
			}
		}		
	}	
	
	protected void renderClass(OWLClass clazz) throws OWLException {
		try{
			OWLClass owlThing = reasoner.getOntology().getOWLDataFactory().getOWLThing();			
			TurtleVisitor TV =  (TurtleVisitor)visitor;
			
			
			// rendering type
			ShortFormProvider shortForms = null;
			if (myModel.getShowQNames()) shortForms = this.myQNameProvider;
			else shortForms = new TurtleShortFormProvider();
			String classQName = shortForms.shortForm(clazz.getURI());

			URI classURI = new URI(OWL_CLASS);
			
			// print the clacss and its type
			println(encodeHLink(clazz.getURI(), classQName) + TAB + 
					encodeHLink(new URI(RDF_TYPE_URI), "a") + SPACE +
					encodeHLink(classURI, myQNameProvider.shortForm(classURI)) + ";");
				
			TV.indentLevel++;	
			
			// print ANNOTATIONS
			if(!clazz.getAnnotations(reasoner.getOntology()).isEmpty()) {
				renderAnnotations(clazz);
				println(";");
			}
			
			// print INTERSECTIONs equivalent to this class
			Iterator it = OWLDescriptionFinder.getIntersections(clazz, reasoner.getOntologies()).iterator();		
			while(it.hasNext()) {
				OWLAnd intersection = (OWLAnd) it.next();
				intersection.accept(TV);
				print(TV.getIndentString() + TV.result() +";" + BREAK);
			}			
			
			// print UNIONs equivalent to this class

			it = OWLDescriptionFinder.getUnions(clazz, reasoner.getOntologies()).iterator();
			while(it.hasNext()) {
				OWLOr union = (OWLOr) it.next();
				union.accept(TV);
				print(TV.getIndentString() + TV.result() + ";" + BREAK);
			}

			// print ENUMERATIONs of classes that are equivalent to this class		

			it = OWLDescriptionFinder.getEnumerations(clazz, reasoner.getOntologies()).iterator();

			while(it.hasNext()) {
				OWLEnumeration oneOf = (OWLEnumeration) it.next();
				oneOf.accept(TV);
				print(TV.getIndentString() + TV.result() + ";" + BREAK);		
			}

			// print EQUIVALENT classes

			Set eqs = OWLDescriptionFinder.getEquivalentClasses(clazz, reasoner.getOntologies());
			it = eqs.iterator();			
			while(it.hasNext()) {
				OWLDescription desc = (OWLDescription) it.next();
				if(!(desc instanceof OWLRestriction || desc instanceof OWLClass))
					it.remove();
			}
			if(reasoner.isConsistent(clazz))
				eqs.addAll(reasoner.equivalentClassesOf(clazz));  // add all named equivalent classes
			it = eqs.iterator();
			URI uri = new URI(OWL_EQUIVALENTCLASS);
			while (it.hasNext())
			{
				OWLDescription eq = (OWLDescription)it.next();
				print( TV.getIndentString() + encodeHLink(uri, myQNameProvider.shortForm(uri)) + TAB);
				TV.reset();
				if (!(eq instanceof OWLClass))  // only indent if eq is an anon class
				{
					TV.indentLevel ++;
					print(BREAK + TV.getIndentString());
				}
				eq.accept(TV);		
				if (!(eq instanceof OWLClass)) // un-indent
					TV.indentLevel --;
				print(TV.result() + ";" + BREAK);	
			}
			
			// DISJOINT CLASSES

			Set disjoints = OWLDescriptionFinder.getDisjoints(clazz, reasoner.getOntologies());
			uri = new URI(OWL_DISJOINTWITH);
			for (it = disjoints.iterator(); it.hasNext(); )
			{
				OWLDescription desc = (OWLDescription)it.next();
				TV.reset();
				desc.accept(TV);
				print(TV.getIndentString() + encodeHLink(uri, myQNameProvider.shortForm(uri)) + TAB + TV.result() + ";" + BREAK);
			}
			
			// SUBCLASS OF
			
			// acquiring all non-inferred super classes (named or anon)
			Set supers = OWLDescriptionFinder.getSuperClasses(clazz, reasoner.getOntologies());
			if(reasoner.isConsistent(clazz)) {
				// remove all the named classes because reasoner will eventually add them
				it = supers.iterator();
				while(it.hasNext())
					if(it.next() instanceof OWLClass)
						it.remove();		
				// add all the named superclasses (including inferred)
				supers.addAll(reasoner.superClassesOf(clazz));
				// remove owl:Thing from the superclass set
				it = supers.iterator();
				while(it.hasNext()) {
					Object o = it.next();
					if(o instanceof Set && ((Set)o).contains(owlThing))
						it.remove();
				}
			}
			uri = new URI(RDFS_SUBCLASSOF);
			for (it = supers.iterator(); it.hasNext(); )
			{
				Object obj = (Object)it.next();				
				// if there is more than one element for this line
				// we only print the first one. rest are either
				// equivalent classes (or properties) 
				if(obj instanceof Collection)
					obj = ((Collection)obj).iterator().next();				
				TV.reset();
				OWLDescription desc = (OWLDescription)obj;
				print(TV.getIndentString() + encodeHLink(uri, myQNameProvider.shortForm(uri)) + TAB);
				if (desc instanceof OWLRestriction)
				{
					TV.indentLevel ++;
					print(BREAK);
				}
				else if (desc instanceof OWLBooleanDescription )
				{
					TV.indentLevel ++;
					print(BREAK);					
				}
				desc.accept(TV);
				if (desc instanceof OWLRestriction)
				{
					print( TV.getIndentString() + TV.result());
					TV.indentLevel --;
				}
				else if (desc instanceof OWLBooleanDescription )
				{
					print( TV.getIndentString() + "[ " + TV.result()  + " ]");
					TV.indentLevel --;
				}
				else
					print(TV.result());				
				pw.print(";" + BREAK);				
			}
						
			TV.indentLevel --;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	protected void renderDataProperty(OWLDataProperty prop) throws OWLException {
		try{
			TurtleVisitor TV =  (TurtleVisitor)visitor;			
			// rendering type
			ShortFormProvider shortForms = null;
			if (myModel.getShowQNames()) shortForms = this.myQNameProvider;
			else shortForms = new TurtleShortFormProvider();
			String classQName = shortForms.shortForm(prop.getURI());
			URI typeURI = new URI(OWL_DATAPROPERTY);			
			// print the clacss and its type
			println(encodeHLink(prop.getURI(), classQName) + TAB + 
					encodeHLink(new URI(RDF_TYPE_URI), "a") + SPACE +
					encodeHLink(typeURI, myQNameProvider.shortForm(typeURI)) + ";");
			TV.indentLevel ++;

			// annotations
			if(!prop.getAnnotations(reasoner.getOntology()).isEmpty()) {
				renderAnnotations(prop);
				println(";");
			}
			
			// is functional
			if (prop.isFunctional(reasoner.getOntologies())) {
				typeURI = new URI(OWL_FUNCTIONALPROP);
				println(TV.getIndentString() + encodeHLink(new URI(RDF_TYPE_URI), "a") + TAB +
						encodeHLink(typeURI, myQNameProvider.shortForm(typeURI)) + ";");
			}
						
			// domain
			for (Iterator it = reasoner.domainsOf(prop).iterator(); it.hasNext();) {
				URI uri = new URI(RDFS_DOMAIN);
				print(TV.getIndentString() + encodeHLink(uri, myQNameProvider.shortForm(uri)) + TAB);
				OWLDescription dom = (OWLDescription) it.next();
				TV.reset();
				dom.accept(TV);
				println( TV.result() + ";");
			}
			
			// range
			for (Iterator it = reasoner.rangesOf(prop).iterator(); it.hasNext();) {
				URI uri = new URI(RDFS_RANGE);
				print(TV.getIndentString() + encodeHLink(uri, myQNameProvider.shortForm(uri)) + TAB);
				OWLDataRange ran = (OWLDataRange) it.next();
				TV.reset();
				ran.accept(TV);
				println( TV.result() + ";");
			}
			
			// print out its superclasses
			for (Iterator it = reasoner.superPropertiesOf(prop).iterator(); it.hasNext();) 
			{
				URI uri = new URI(RDFS_SUBPROPERTYOF);
				print(TV.getIndentString() + encodeHLink(uri, myQNameProvider.shortForm(uri)) + TAB);
				Object obj = it.next();
				
				if (obj instanceof OWLDescription)
				{
					OWLDescription dom = (OWLDescription) it.next();
					TV.reset();
					dom.accept(TV);
					println( TV.result() + ";");
				}
				else // could be a set of OWLDataproperty
				{

					Set set = (Set)obj;
					for (Iterator i = set.iterator(); i.hasNext();)
					{
						OWLDataProperty dom = (OWLDataProperty)i.next();
						TV.reset();
						dom.accept(TV);
						println( TV.result() + ";");
					}
				}

			}
		}
		catch (Exception ex)
		{ ex.printStackTrace(); }
	}

	protected void renderDataType(OWLDataType datatype) throws OWLException {
		// TODO Auto-generated method stub
	}

	protected void renderIndividual(OWLIndividual ind) throws OWLException {
		try{
			
			ShortFormProvider shortForms = null;
			if (myModel.getShowQNames()) shortForms = this.myQNameProvider;
			else shortForms = new TurtleShortFormProvider();
			
			OWLClass owlThing = reasoner.getOntology().getOWLDataFactory().getOWLThing();
			
			if ( ind.isAnonymous() ) 
				print("[ ]"); // rendering anon individuals (not sure if this is correct). 	
			else 
				println(encodeHLink(ind.getURI(), shortForms.shortForm(ind.getURI())) );
			
			TurtleVisitor TV = (TurtleVisitor)visitor;
			TV.indentLevel++;
			
			// defined types
			
			Set types = OWLDescriptionFinder.getTypes(ind, reasoner.getOntologies());
/*			if(reasoner.isConsistent()) {
				// remove all the named classes because reasoner will eventually add them
				Iterator i = types.iterator();
				while(i.hasNext())
					if(i.next() instanceof OWLClass)
						i.remove();		
				// add all the named superclasses
				types.addAll(reasoner.typesOf(ind));
			}
			// remove owl:Thing from the superclass set
			Iterator i = types.iterator();
			while(i.hasNext()) {
				Object o = i.next();
				if(o instanceof Set && ((Set)o).contains(owlThing))
					i.remove();
			}*/
			//System.out.println(types.size());
			for (Iterator it = types.iterator(); it.hasNext();) {
				Object obj = (Object)it.next();				
				// if there is more than one element for this line
				// we only print the first one. rest are either
				// equivalent classes (or properties) 
				if(obj instanceof Collection)
					obj = ((Collection)obj).iterator().next();				
				OWLDescription desc = (OWLDescription)obj;

				TV.reset();
				desc.accept(TV);
				print(TV.getIndentString() + TAB + encodeHLink(new URI(RDF_TYPE_URI), "a") + TAB + TV.result() + ";");
				if (it.hasNext())
					println();
			}
			
			if (types.size() != 0)
				println();
			
			// annotations	
			
			if(!ind.getAnnotations(reasoner.getOntology()).isEmpty()) {
				renderAnnotations(ind);
				println(";");
			}	
			
			// object property values
			
			Map propertyValues = ind.getObjectPropertyValues(reasoner.getOntologies());
			for (Iterator it = propertyValues.keySet().iterator(); it.hasNext();) {
				OWLObjectProperty prop = (OWLObjectProperty) it.next();
				Set vals = (Set) propertyValues.get(prop);
				for (Iterator valIt = vals.iterator(); valIt.hasNext();) {
					OWLIndividual oi = (OWLIndividual) valIt.next();
					TV.reset();
					oi.accept(TV);
					print(TV.getIndentString() + encodeHLink(prop.getURI(), shortForms.shortForm(prop.getURI())) + TAB + TV.result() + ";");
					if (valIt.hasNext()) {
						println();
					}
				}
				if (it.hasNext())
					println();
			}
			
			if (propertyValues.size() != 0)
				println();
			
			// data property values
			
			Map dataValues = ind.getDataPropertyValues(reasoner.getOntologies());
			for (Iterator it = dataValues.keySet().iterator(); it.hasNext();) {
				OWLDataProperty prop = (OWLDataProperty) it.next();
				Set vals = (Set) dataValues.get(prop);
				for (Iterator valIt = vals.iterator(); valIt.hasNext();) {
					OWLDataValue dtv = (OWLDataValue) valIt.next();
					TV.reset();
					dtv.accept(TV);
					print(TV.getIndentString() + encodeHLink(prop.getURI(), shortForms.shortForm(prop.getURI())) + TAB + TV.result() + ";");
					//System.out.println("Datatype Prop:" + prop.getURI().toString());
					if (valIt.hasNext()) {
						println();
					}
				}
				if (it.hasNext())
					println();
			}			
			TV.indentLevel++;
		}
		catch (Exception ex)
		{ ex.printStackTrace(); }
	} 

	protected void renderObjectProperty(OWLObjectProperty prop) throws OWLException {
		try{
			TurtleVisitor TV =  (TurtleVisitor)visitor;			
			// rendering type
			ShortFormProvider shortForms = null;
			if (myModel.getShowQNames()) shortForms = this.myQNameProvider;
			else shortForms = new TurtleShortFormProvider();
			String classQName = shortForms.shortForm(prop.getURI());
			URI typeURI = new URI(OWL_OBJECTPROPERTY);			
			// print the class and its type
			println(encodeHLink(prop.getURI(), classQName) + TAB + 
					encodeHLink(new URI(RDF_TYPE_URI), "a") + SPACE +
					encodeHLink(typeURI, myQNameProvider.shortForm(typeURI)) + ";");
			TV.indentLevel ++;
			
		// print out ANNOTATIONS
			
		if(!prop.getAnnotations(reasoner.getOntology()).isEmpty()) {
			renderAnnotations(prop);
			println(";");
		}
		
		// is TRANSITIVE?
		
		if (prop.isTransitive(reasoner.getOntologies())) {
			typeURI = new URI(OWL_TRANSITIVEPROP);
			print(TV.getIndentString() + encodeHLink(new URI(RDF_TYPE_URI), "a") + TAB +
					encodeHLink(typeURI, myQNameProvider.shortForm(typeURI)) + ";");
		}
		
		// is FUNCTIONAL?
		
		if (prop.isFunctional(reasoner.getOntologies())) {
			typeURI = new URI(OWL_FUNCTIONALPROP);
			println(TV.getIndentString() + encodeHLink(new URI(RDF_TYPE_URI), "a") + TAB +
					encodeHLink(typeURI, myQNameProvider.shortForm(typeURI)) + ";");
		}
		
		// is INVERSE FUNCTIONAL?
		
		if (prop.isInverseFunctional(reasoner.getOntologies())) {
			typeURI = new URI(OWL_INVERSEFUNCTIONALPROP);
			println(TV.getIndentString() + encodeHLink(new URI(RDF_TYPE_URI), "a") + TAB +
					encodeHLink(typeURI, myQNameProvider.shortForm(typeURI)) + ";");
		}
		
		// is SYMMETRIC?
		
		if (prop.isSymmetric(reasoner.getOntologies())) {
			typeURI = new URI(OWL_SYMMETRICPROP);
			println(TV.getIndentString() + encodeHLink(new URI(RDF_TYPE_URI), "a") + TAB +
					encodeHLink(typeURI, myQNameProvider.shortForm(typeURI)) + ";");
		}
		
		// EQUIVALENT properties
		
		for (Iterator it = reasoner.equivalentPropertiesOf(prop).iterator(); it.hasNext();) {
			URI uri = new URI(OWL_EQUIVALENTPROP);
			print(TV.getIndentString() + encodeHLink(uri, myQNameProvider.shortForm(uri)) + TAB);
			Object equi = (Object) it.next();
			OWLObjectProperty op = (OWLObjectProperty)equi;
			TV.reset();
			op.accept(TV);
			println( TV.result() + ";");
		}
		
		// INVERSE
		
		for (Iterator it = prop.getInverses(reasoner.getOntologies()).iterator(); it.hasNext();) 
		{
			URI uri = new URI(OWL_INVERSEOF);
			print(TV.getIndentString() + encodeHLink(uri, myQNameProvider.shortForm(uri)) + TAB);
			OWLObjectProperty inv = (OWLObjectProperty) it.next();
			TV.reset();
			inv.accept(TV);
			println( TV.result() + ";");
		}

		// DOMAIN
		
		for (Iterator it = reasoner.domainsOf(prop).iterator(); it.hasNext();) {
			URI uri = new URI(RDFS_DOMAIN);
			print(TV.getIndentString() + encodeHLink(uri, myQNameProvider.shortForm(uri)) + TAB);
			OWLDescription dom = (OWLDescription) it.next();
			TV.reset();
			dom.accept(TV);
			println( TV.result() + ";");
		}
		
		// RANGE
		
		for (Iterator it = reasoner.rangesOf(prop).iterator(); it.hasNext();) {
			URI uri = new URI(RDFS_RANGE);
			print(TV.getIndentString() + encodeHLink(uri, myQNameProvider.shortForm(uri)) + TAB);
			OWLDescription ran = (OWLDescription) it.next();
			TV.reset();
			ran.accept(TV);
			println( TV.result() + ";");
		}
		
		// print out superclasses (SUBCLASSOF)
		
		for (Iterator it = reasoner.superPropertiesOf(prop).iterator(); it.hasNext();) {
			URI uri = new URI(RDFS_SUBPROPERTYOF);
			print(TV.getIndentString() + encodeHLink(uri, myQNameProvider.shortForm(uri)) + TAB);
			Object Obj = it.next();
			if (Obj instanceof OWLObjectProperty)
			{
				//System.out.println(Obj.getClass().toString() );
				OWLObjectProperty sup = (OWLObjectProperty)Obj;
				TV.reset();
				sup.accept(TV);
				println( TV.result() + ";");
			}
			else // could be a set of ObjProperties ... very strange
			{
				Set set = (Set)Obj;
				for (Iterator i = set.iterator(); i.hasNext();)
				{
					OWLObjectProperty sup = (OWLObjectProperty)i.next();
					TV.reset();
					sup.accept(TV);
					println( TV.result() + ";");
				}
			}
		}
		
		TV.indentLevel ++;
		}
		catch (Exception ex)
		{ ex.printStackTrace(); }
	}

	protected void println() {
		pw.println(BREAK);		
	}
	
	protected void println(String str){
		pw.print(str + BREAK);
	}
			
	public String getContentType() {
		return TURTLE_CONTENT_TYPE;
	}

	public String getName() {
		return TURTLE_NAME;
	}
	
	private String getOtherStatements(OWLEntity ent)
	{
		String result = BREAK;
		ShortFormProvider shortForms = null;
		if (myModel.getShowQNames()) shortForms = this.myQNameProvider;
		else shortForms = new TurtleShortFormProvider();
		
		//System.out.println(ent.getClass().toString());
		try
		{
			OWLClass owlNothing = reasoner.getOntology().getOWLDataFactory().getOWLNothing();
			TurtleVisitor TV = (TurtleVisitor)visitor;	
			if (ent instanceof OWLClass) // here we render all subclasses of this OWLClass
			{
				OWLClass clazz = (OWLClass)ent;
				Set subs = reasoner.subClassesOf(clazz);
				
				// SUPERCLASSOF (separate triple statements)

				// here we only print if the set of subclass is not { owlNothing }
				if ( (subs.size() >= 1 && !((Set)subs.iterator().next()).contains(owlNothing)) )
				{ 							
					URI uri = new URI(RDFS_SUBCLASSOF);
					for (Iterator it = subs.iterator(); it.hasNext(); )
					{
						Object obj = (Object)it.next();				
						// if there is more than one element for this line
						// we only print the first one. rest are either
						// equivalent classes (or properties) 
						if(obj instanceof Collection)
							obj = ((Collection)obj).iterator().next();
						OWLDescription desc = (OWLDescription)obj;
						TV.reset();
						desc.accept(TV);
						result = result + TV.getIndentString() + TV.result() + TAB + encodeHLink(uri, myQNameProvider.shortForm(uri) ) + TAB +
						encodeHLink(clazz.getURI(), shortForms.shortForm(clazz.getURI())) + PERIOD + BREAK;
					}
					result = result + BREAK;
				
				}
				// as DOMAIN of properties
				
				Set propDomains = getPropertiesWithDomain(clazz, showInherited); // whether to show inherited or not
				URI uri = new URI(RDFS_DOMAIN);
				for (Iterator it = propDomains.iterator(); it.hasNext(); )
				{
					Object obj = it.next();
					OWLProperty desc = (OWLProperty)obj;
					TV.reset();
					desc.accept(TV);
					result = result + TV.getIndentString() + TV.result() + TAB + encodeHLink(uri, myQNameProvider.shortForm(uri) ) + TAB + 
								encodeHLink(clazz.getURI(), shortForms.shortForm(clazz.getURI()) )+ PERIOD + BREAK;
				}
				
				if (propDomains.size() > 0)
					result = result + BREAK;
				
				// as RANGE of properties
				
				Set propRanges = getPropertiesWithRange(clazz, showInherited); // whether to show inherited or not
				uri = new URI(RDFS_RANGE);
				for (Iterator it = propRanges.iterator(); it.hasNext(); )
				{
					Object obj = it.next();
					OWLProperty desc = (OWLProperty)obj;
					TV.reset();
					desc.accept(TV);
					result = result + TV.getIndentString() + TV.result() + TAB + encodeHLink(uri, myQNameProvider.shortForm(uri) ) + TAB + 
								encodeHLink(clazz.getURI(), shortForms.shortForm(clazz.getURI()) )+ PERIOD + BREAK;
				}				
				if (propRanges.size() > 0)
					result = result + BREAK;
				
				// INSTANCEs of this class
				
				Set instances = new HashSet();
				if (showInherited)
					instances = reasoner.allInstancesOf(clazz);
				else 
					instances = reasoner.instancesOf(clazz);
				for (Iterator it = instances.iterator(); it.hasNext(); )
				{
					Object obj = it.next();
					OWLIndividual ind = (OWLIndividual)obj;

					result = result +TV.getIndentString() + encodeHLink(ind.getURI(), shortForms.shortForm(ind.getURI()) ) +
								TAB + encodeHLink(new URI(RDF_TYPE_URI), "a") + TAB + 
								encodeHLink(clazz.getURI(), shortForms.shortForm(clazz.getURI()) )+ PERIOD + BREAK;
				}
				
			}
			else if (ent instanceof OWLProperty) // OWLDataProperty + OWLObjectProperty
			{
				OWLProperty prop = (OWLProperty) ent;
				for (Iterator it = reasoner.subPropertiesOf(prop).iterator(); it.hasNext();) 
				{
					URI uri = new URI(RDFS_SUBPROPERTYOF);
					Object obj = it.next();
					// if there is more than one element for this line
					// we only print the first one. rest are either
					// equivalent classes (or properties) 
					if(obj instanceof Collection)
						obj = ((Collection)obj).iterator().next();
					OWLProperty dom = (OWLProperty)obj;
					TV.reset();
					dom.accept(TV);
					result = result + TV.result() + TAB + encodeHLink(uri, myQNameProvider.shortForm(uri)) + 
								TAB + encodeHLink(prop.getURI(), shortForms.shortForm(prop.getURI())) + PERIOD  + BREAK; 
				}
			}			
		}
		catch (Exception ex)
		{ ex.printStackTrace(); }
		
		return result;
	}
	
	
	/*
	 *  SwoopRenderer method
	 * 
	 */
	public Component getDisplayComponent( SwoopDisplayPanel panel )
	{		
		if (!(panel instanceof TermsDisplay ))
			throw new IllegalArgumentException();
		
		JEditorPane editorPane = BaseEntityRenderer.getEditorPane( this.getContentType(), (TermsDisplay)panel );
		
		if (!editorEnabled) 
		{ return editorPane; }
		else 
		{
			editorPane.setEditable(true);			
			// adding to UI listeners of TermsDisplay
			//editorPane.getDocument().addDocumentListener((TermsDisplay)panel);
			editorPane.addMouseListener((TermsDisplay)panel);			
			editorPane.addKeyListener((TermsDisplay)panel);
		}
		return editorPane;
	}
	
		
  private String repairForEndOfStatement(String toBeRendered){
  	toBeRendered = toBeRendered.trim();
  	while (toBeRendered.endsWith(BREAK))
  		toBeRendered = toBeRendered.substring(0, toBeRendered.length() - BREAK.length());
  	while (toBeRendered.endsWith(SPACE))
 		toBeRendered = toBeRendered.substring(0, toBeRendered.length() - SPACE.length());
  	while (toBeRendered.endsWith(TAB))
 		toBeRendered = toBeRendered.substring(0, toBeRendered.length() - TAB.length());
  	
    if (toBeRendered.endsWith(";"))
    	toBeRendered = toBeRendered.substring(0, toBeRendered.length() - 1) + PERIOD;
    else
    {
    	//System.out.println(toBeRendered.substring(toBeRendered.length() - 5));
    	toBeRendered = toBeRendered + PERIOD;
    }
  	return toBeRendered;
  }
  
  private String encodeHLink(URI uri, String name)
  {
  	if (!editorEnabled) return "<a href="+escape(uri)+">"+escape(name)+"</a>";
  	else return name;
  }

  private String encodeHLink(String uri_string, String name) throws URISyntaxException
  {
  	URI uri = new URI(uri_string);
  	if (!editorEnabled) return "<a href="+escape(uri)+">"+escape(name)+"</a>";
  	else return escape(name);
  }

  /* Sanity check.  This way we know if message below is printed, 
   * we are using the wrong ShortFormProvider
   */
  public String shortForm(URI uri)
  {  return "WRONG SHORTFORM PROVIDER!!!";  }

/* (non-Javadoc)
 * @see org.mindswap.swoop.renderer.BaseEntityRenderer#renderForeignEntity(org.semanticweb.owl.model.OWLEntity)
 */
protected void renderForeignEntity(OWLEntity ent) throws OWLException {
	// TODO Auto-generated method stub
	
}

/* (non-Javadoc)
 * @see org.mindswap.swoop.renderer.SwoopEditableRenderer#setEditorEnabled(boolean)
 */
public void setEditorEnabled(boolean mode) 
{ editorEnabled = mode; }

/* (non-Javadoc)
 * @see org.mindswap.swoop.renderer.SwoopEditableRenderer#isEditableText()
 */
public boolean isEditableText() 
{ return editorEnabled; }

}
