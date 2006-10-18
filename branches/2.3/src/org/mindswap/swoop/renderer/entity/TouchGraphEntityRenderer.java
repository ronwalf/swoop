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


import java.awt.Color;
import java.awt.Component;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.mindswap.swoop.SwoopDisplayPanel;
import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.TermsDisplay;
import org.mindswap.swoop.renderer.BaseEntityRenderer;
import org.mindswap.swoop.renderer.SwoopEntityRenderer;
import org.mindswap.swoop.renderer.SwoopRenderingVisitor;
import org.mindswap.swoop.renderer.entity.graph.GraphPanel;
import org.mindswap.swoop.renderer.entity.graph.SwoopNode;
import org.mindswap.swoop.utils.owlapi.OWLDescriptionFinder;
import org.semanticweb.owl.io.RendererException;
import org.semanticweb.owl.io.ShortFormProvider;
import org.semanticweb.owl.model.OWLAnd;
import org.semanticweb.owl.model.OWLAnnotationInstance;
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
import org.semanticweb.owl.model.OWLPropertyDomainAxiom;
import org.semanticweb.owl.model.OWLRestriction;
import org.semanticweb.owl.model.OWLSameIndividualsAxiom;
import org.semanticweb.owl.model.OWLSubClassAxiom;
import org.semanticweb.owl.model.OWLSubPropertyAxiom;
import org.semanticweb.owl.model.OWLSymmetricPropertyAxiom;
import org.semanticweb.owl.model.OWLTransitivePropertyAxiom;
import org.semanticweb.owl.model.helper.OntologyHelper;

import com.touchgraph.graphlayout.Edge;
import com.touchgraph.graphlayout.Node;
import com.touchgraph.graphlayout.TGException;
import com.touchgraph.graphlayout.TGPanel;
import com.touchgraph.graphlayout.interaction.HVScroll;
import com.touchgraph.graphlayout.interaction.LocalityScroll;
import com.touchgraph.graphlayout.interaction.RotateScroll;
import com.touchgraph.graphlayout.interaction.ZoomScroll;

/**
 * @author Dave
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class TouchGraphEntityRenderer { // extends BaseEntityRenderer  implements SwoopEntityRenderer 
//{
//
//	class TouchGraphVisitor implements org.mindswap.swoop.renderer.SwoopRenderingVisitor 
//	{
//		private ShortFormProvider shortForms;
//		//private Node mySourceNode = null;
//		private Node myResultNode = null;
//		private GraphPanel myPanel   = null;
//		private String myEdgeID     = null;
//		
//		public TouchGraphVisitor(ShortFormProvider shortForms, GraphPanel panel) {
//			this.shortForms = shortForms;
//			this.myPanel    = panel;
//			reset();
//		}
//		
//		public void reset() 
//		{
//			myResultNode = null;
//			myEdgeID      = null;
//		}
//
//		public String result() {
//			return myEdgeID;
//		}
//		
//		public Node getResult()
//		{
//			return myResultNode;
//		}
//
//
//		// and (DONE)
//		public void visit(OWLAnd and) throws OWLException 
//		{			
//			try
//			{	
//				URI uri = new URI(OWL_INTERSECTIONOF);								
//				Node andNode = new SwoopNode( uri, INTERSECTION, myModel );
//				andNode.setBackColor( Color.WHITE );
//				andNode.setTextColor( Color.BLACK );
//				myPanel.addNode( andNode );
//				for ( Iterator it = and.getOperands().iterator();it.hasNext(); ) {
//					OWLDescription desc = (OWLDescription) it.next();
//					desc.accept( this );
//					myPanel.addEdge( andNode, myResultNode, DEFAULT_DISTANCE );					
//				}
//				andNode.setBackColor( Edge.DEFAULT_COLOR );
//				andNode.setType( Node.TYPE_HOLLOW_CIRCLE );
//				myResultNode = andNode;
//			}
//			catch (Exception ex)
//			{ 
//				ex.printStackTrace(); 
//			}
//		}
//		
//		// or (DONE)
//		public void visit(OWLOr or) throws OWLException 
//		{
//			try
//			{	
//				URI uri = new URI(OWL_UNIONOF);				
//				Node orNode = new SwoopNode( uri, UNION, myModel );
//				orNode.setBackColor( Color.WHITE );
//				orNode.setTextColor( Color.BLACK );
//				myPanel.addNode( orNode );
//				
//				for ( Iterator it = or.getOperands().iterator();it.hasNext(); ) {
//					OWLDescription desc = (OWLDescription) it.next();
//					desc.accept( this );
//					myPanel.addEdge( orNode, myResultNode, DEFAULT_DISTANCE );					
//				}
//				orNode.setBackColor( Edge.DEFAULT_COLOR );
//				orNode.setType( Node.TYPE_HOLLOW_CIRCLE );
//				myResultNode = orNode;
//			}
//			catch (Exception ex)
//			{ 
//				ex.printStackTrace(); 
//			}
//		}
//		
//		// not (DONE)
//		public void visit(OWLNot not) throws OWLException 
//		{
//			try
//			{					
//				URI uri = new URI(OWL_COMPLEMENTOF);
//				
//				Node notNode = new SwoopNode( uri, NOT, myModel);
//				notNode.setBackColor( Color.WHITE );
//				notNode.setTextColor( Color.BLACK );
//				myPanel.addNode( notNode );
//				
//				OWLDescription desc = not.getOperand();
//				desc.accept(this);
//				Edge expEdge = new Edge( notNode, myResultNode, DEFAULT_DISTANCE );
//				myPanel.addEdge( expEdge );
//				
//				notNode.setBackColor( Edge.DEFAULT_COLOR );
//				notNode.setType( Node.TYPE_HOLLOW_CIRCLE );
//				myResultNode = notNode;
//			}
//			catch (Exception ex)			
//			{ ex.printStackTrace(); }		
//		}
//		
//		// class
//		public void visit( OWLClass clazz ) throws OWLException 
//		{
//			try
//			{	
//				URI uri = clazz.getURI();
//				Node classNode = new SwoopNode( uri, uri.toString(), shortForms.shortForm( uri ), myModel);
//				myResultNode = myPanel.addNode( classNode );
//				//myResultNode = classNode;
//			}
//			catch (Exception ex)
//			{ 
//				ex.printStackTrace(); 
//			}
//		}
//		
//		// object property
//		public void visit(OWLObjectProperty prop) throws OWLException 
//		{
//			try
//			{	
//				URI uri = prop.getURI();
//				Node propNode = new SwoopNode( uri, uri.toString(), shortForms.shortForm(uri), myModel);
//				myResultNode = myPanel.addNode( propNode );
//				
//				myPanel.addNode( propNode );
//				myResultNode.setBackColor( OBJ_PROP_NODE_COLOR );
//				myResultNode.setType( Node.TYPE_ELLIPSE );
//
//			}
//			catch (Exception ex)
//			{ 
//				ex.printStackTrace(); 
//			}
//		}
//		
//		// data property
//		public void visit(OWLDataProperty prop) throws OWLException 
//		{
//			try
//			{	
//				URI uri = prop.getURI();
//				Node propNode = new SwoopNode( uri, uri.toString(), shortForms.shortForm(uri), myModel);
//				myResultNode = myPanel.addNode( propNode );
//				
//				myResultNode.setBackColor( DATA_PROP_NODE_COLOR );
//			}
//			catch (Exception ex)
//			{ 
//				ex.printStackTrace(); 
//			}
//		}
//		
//		// data enumeration (DONE)
//		public void visit(OWLDataEnumeration enumeration) throws OWLException 
//		{
//			try
//			{
//				URI uri = new URI(OWL_ONEOF);
//				Node oneOfNode = new SwoopNode( uri, UNION, myModel);
//				myPanel.addNode( oneOfNode );
//				oneOfNode.setBackColor( Color.WHITE );
//				oneOfNode.setTextColor( Color.BLACK );
//				
//				for ( Iterator it = enumeration.getValues().iterator();it.hasNext(); ) 
//				{
//					OWLDescription desc = (OWLDescription) it.next();
//					desc.accept( this );
//					myPanel.addEdge( oneOfNode, myResultNode, DEFAULT_DISTANCE );
//				}
//				oneOfNode.setBackColor( Edge.DEFAULT_COLOR );
//				oneOfNode.setType( Node.TYPE_HOLLOW_CIRCLE );
//				myResultNode = oneOfNode;
//				myEdgeID = "";
//			}
//			catch (Exception ex)
//			{ throw new OWLException(ex.toString()); }
//		}
//		
//		// datatype
//		public void visit(OWLDataType ocdt) throws OWLException 
//		{
//			try
//			{
//				URI uri = ocdt.getURI() ;
//				Node dataTypeNode = new SwoopNode( uri, uri.toString(), shortForms.shortForm(ocdt.getURI()), myModel);
//				myResultNode = dataTypeNode;
//				myResultNode.setBackColor(  LIT_NODE_COLOR );
//				myResultNode.setType( Node.TYPE_ELLIPSE );
//			}
//			catch(Exception ex)
//			{ ex.printStackTrace(); }
//		}
//		
//		// data value
//		// data values don't have URI's
//		public void visit(OWLDataValue dv) throws OWLException 
//		{		
//			try
//			{
//				String value = "\"" + escape(dv.getValue()) + "\"";					
//				/* Only show it if it's not string */
//				URI uri = dv.getURI();
//				String dvlang = dv.getLang();
//				String garnish = "";
//				if (uri != null) {
//					garnish = "^^" + shortForms.shortForm(uri);
//				} 
//				else 
//				{
//					if (dvlang != null)
//					garnish =  "@" + dvlang;
//				}
//				
//				// add to graph
//				Node valueNode = new SwoopNode( null, value + garnish, myModel);
//				myPanel.addNode( valueNode );
//				myResultNode = valueNode;
//				myResultNode.setBackColor(  LIT_NODE_COLOR );
//			}
//			catch (Exception ex)
//			{ 
//				ex.printStackTrace(); 
//			}
//		}
//		
//		// individual
//		public void visit(OWLIndividual ind) throws OWLException 
//		{
//			try
//			{
//				if ( ind.isAnonymous() ) 
//				{
//					Node anonNode = new SwoopNode(null, "", myModel); // anonymous individual
//					myResultNode = myPanel.addNode(anonNode);
//				} else 
//				{
//					URI uri = ind.getURI();
//					Node anonNode = new SwoopNode(uri, uri.toString(), shortForms.shortForm( uri ), myModel);
//					myPanel.addNode(anonNode);
//					myResultNode = anonNode;					
//				}
//				myResultNode.setBackColor( IND_NODE_COLOR);
//			}
//			catch (Exception ex)
//			{ 
//				ex.printStackTrace(); 
//			}			
//		}
//		
//		// oneof (DONE)
//		public void visit(OWLEnumeration enumeration) throws OWLException 
//		{
//			try
//			{					
//				URI uri = new URI(OWL_ONEOF);
//				
//				Node oneOfNode = new SwoopNode(uri, shortForms.shortForm( uri ), myModel);
//				myPanel.addNode( oneOfNode );
//				oneOfNode.setBackColor( Color.WHITE );
//				oneOfNode.setTextColor( Color.BLACK );
//
//				for ( Iterator it = enumeration.getIndividuals().iterator();it.hasNext(); )
//				{
//					OWLIndividual ind = (OWLIndividual) it.next();
//					ind.accept( this );
//					myPanel.addEdge( oneOfNode, myResultNode, DEFAULT_DISTANCE );
//				}
//				
//				oneOfNode.setBackColor( Edge.DEFAULT_COLOR );
//				oneOfNode.setType( Node.TYPE_HOLLOW_CIRCLE );
//				myResultNode = oneOfNode;
//				myEdgeID = null;
//			}
//			catch (Exception ex)			
//			{ ex.printStackTrace(); }
//		}
//		
//		// object some restriction (DONE)
//		public void visit( OWLObjectSomeRestriction restriction ) throws OWLException {
//			try
//			{							
//				URI propertyURI = restriction.getObjectProperty().getURI();
//				
//				Node propNode = new SwoopNode(propertyURI, propertyURI.toString(), shortForms.shortForm(propertyURI), myModel);
//				propNode.setType( Node.TYPE_ELLIPSE );
//				propNode.setBackColor( OBJ_PROP_NODE_COLOR);
//				propNode = myPanel.addNode( propNode );
//		
//				OWLDescription desc = restriction.getDescription();
//				desc.accept( this );
//
//				Edge edge = new Edge( propNode, myResultNode );
//				edge.setID( EXISTS );
//				myPanel.addEdge( edge );
//				
//				myResultNode = propNode;
//				myEdgeID     = null;
//			}
//			catch (Exception ex)
//			{ ex.printStackTrace(); }
//		}
//		
//		// object all restriction (DONE)
//		public void visit(OWLObjectAllRestriction restriction) throws OWLException 
//		{
//			try
//			{				
//				URI propertyURI = restriction.getObjectProperty().getURI();	
//				Node propNode = new SwoopNode(propertyURI, propertyURI.toString(), shortForms.shortForm(propertyURI), myModel);
//				propNode.setType( Node.TYPE_ELLIPSE );
//				propNode.setBackColor( OBJ_PROP_NODE_COLOR);
//				propNode = myPanel.addNode( propNode );
//
//				OWLDescription desc = restriction.getDescription();
//				desc.accept( this );
//
//				Edge edge = new Edge( propNode, myResultNode );
//				edge.setID( FORALL );
//				myPanel.addEdge( edge );
//				
//				myResultNode = propNode;
//				myEdgeID     = null;
//
//			}
//			catch (Exception ex)
//			{ ex.printStackTrace(); }
//		}
//		
//		// object value restriction (DONE)
//		public void visit(OWLObjectValueRestriction restriction) throws OWLException
//		{
//			try
//			{	
//				URI propertyURI = restriction.getObjectProperty().getURI();	
//				Node propNode = new SwoopNode(propertyURI, propertyURI.toString(), shortForms.shortForm(propertyURI), myModel);
//				propNode.setType( Node.TYPE_ELLIPSE );
//				propNode.setBackColor( OBJ_PROP_NODE_COLOR);
//				propNode = myPanel.addNode( propNode );
//				
//				restriction.getIndividual().accept( this );
//
//				Edge edge = new Edge( propNode, myResultNode );
//				edge.setID( MUSTBE );
//				myPanel.addEdge( edge );
//				
//				myResultNode = propNode;
//				myEdgeID     = null;
//				
//			}
//			catch (Exception ex)
//			{ ex.printStackTrace(); }
//		}
//		
//		// object cardinality restriction (DONE)
//		public void visit(OWLObjectCardinalityRestriction restriction) throws OWLException 
//		{
//			try
//			{					
//				URI propertyURI = restriction.getObjectProperty().getURI();
//				Node propNode = new SwoopNode(propertyURI, propertyURI.toString(), shortForms.shortForm(propertyURI), myModel);
//				propNode.setType( Node.TYPE_ELLIPSE );
//				propNode.setBackColor( OBJ_PROP_NODE_COLOR);
//				propNode = myPanel.addNode( propNode );
//				
//				String number  = "";
//				String garnish = "";
//				if ( restriction.isExactly() ) {
//					number  = number +  restriction.getAtLeast();
//					garnish = EQUALS;
//				} else if ( restriction.isAtMost() ) {
//					number  = number +  restriction.getAtMost();
//					garnish = LESSTHAN;
//				} else 	if ( restriction.isAtLeast() ) {
//					number = number +  restriction.getAtLeast();
//					garnish = GREATERTHAN;
//				}
//				
//				// add number node
//				Node numNode  = new SwoopNode( null, number, myModel); 
//				myPanel.addNode( numNode );
//				numNode.setBackColor( LIT_NODE_COLOR );
//				
//				// add edge from property to number
//				Edge edge = new Edge( propNode, numNode, DEFAULT_DISTANCE);
//				edge.setID( garnish );
//				myPanel.addEdge( edge );
//				
//				myResultNode = propNode;
//				myEdgeID     = null;
//			}
//			catch(Exception ex)
//			{ ex.printStackTrace(); }
//		}
//		
//		// data all restriction (DONE)
//		public void visit(OWLDataAllRestriction restriction) throws OWLException
//		{
//			
//			try
//			{							
//				URI propertyURI = restriction.getDataProperty().getURI();	
//				Node propNode = new SwoopNode( propertyURI, propertyURI.toString(), shortForms.shortForm( propertyURI ), myModel);
//				propNode.setType( Node.TYPE_ELLIPSE );
//				propNode.setBackColor( DATA_PROP_NODE_COLOR);
//				propNode = myPanel.addNode( propNode );
//				
//				OWLDataRange ran = restriction.getDataType();
//				ran.accept( this );
//
//				Edge edge = new Edge( propNode, myResultNode );
//				edge.setID( FORALL );
//				myPanel.addEdge( edge );
//				
//				myResultNode = propNode;
//				myEdgeID     = null;
//			}
//			catch (Exception ex)
//			{ ex.printStackTrace(); }
//		}
//		
//		// data some restriction (DONE)
//		public void visit(OWLDataSomeRestriction restriction) throws OWLException 
//		{
//			try
//			{							
//				URI propertyURI = restriction.getDataProperty().getURI();	
//				Node propNode = new SwoopNode( propertyURI, propertyURI.toString(), shortForms.shortForm( propertyURI ), myModel);
//				propNode.setType( Node.TYPE_ELLIPSE );
//				propNode.setBackColor( DATA_PROP_NODE_COLOR);
//				propNode = myPanel.addNode( propNode );
//				
//				OWLDataRange ran = restriction.getDataType();
//				ran.accept( this );
//
//				Edge edge = new Edge( propNode, myResultNode );
//				edge.setID( EXISTS );
//				myPanel.addEdge( edge );
//				
//				myResultNode = propNode;
//				myEdgeID     = null;
//			}
//			catch (Exception ex)
//			{ ex.printStackTrace(); }
//		}
//		
//		// data value restriction (DONE )
//		public void visit(OWLDataValueRestriction restriction) throws OWLException 
//		{
//			try
//			{							
//				URI propertyURI = restriction.getDataProperty().getURI();	
//				Node propNode = new SwoopNode( propertyURI, propertyURI.toString(), shortForms.shortForm( propertyURI ), myModel);
//				propNode.setType( Node.TYPE_ELLIPSE );
//				propNode.setBackColor( DATA_PROP_NODE_COLOR);
//				propNode = myPanel.addNode( propNode );
//
//				restriction.getValue().accept( this );
//
//				Edge edge = new Edge( propNode, myResultNode );
//				edge.setID( MUSTBE );
//				myPanel.addEdge( edge );
//				
//				myResultNode = propNode;
//				myEdgeID     = null;
//			}
//			catch (Exception ex)
//			{ ex.printStackTrace(); }
//		}
//		
//		// data cardinality restriction
//		public void visit(OWLDataCardinalityRestriction restriction) throws OWLException 
//		{
//			try
//			{
//				URI propertyURI = restriction.getDataProperty().getURI();
//				Node propNode = new SwoopNode( propertyURI, propertyURI.toString(), shortForms.shortForm( propertyURI ), myModel);
//				propNode.setType( Node.TYPE_ELLIPSE );
//				propNode.setBackColor( DATA_PROP_NODE_COLOR);
//				propNode = myPanel.addNode( propNode );
//				
//				String number  = "";
//				String garnish = "";
//				if ( restriction.isExactly() ) {
//					number  = number +  restriction.getAtLeast();
//					garnish = EQUALS;
//				} else if ( restriction.isAtMost() ) {
//					number  = number +  restriction.getAtMost();
//					garnish = LESSTHAN;
//				} else 	if ( restriction.isAtLeast() ) {
//					number = number +  restriction.getAtLeast();
//					garnish = GREATERTHAN;
//				}
//	
//				Node numNode  = new SwoopNode(null, number, myModel); 
//				myPanel.addNode( numNode );
//				numNode.setBackColor( LIT_NODE_COLOR );
//				Edge edge = new Edge( propNode, numNode, DEFAULT_DISTANCE);
//				edge.setID( garnish );
//				myPanel.addEdge( edge );
//				
//				myResultNode = propNode;
//				myEdgeID     = null;
//			}
//			catch(Exception ex)
//			{ ex.printStackTrace(); }
//		}
//
//		public void visit(OWLAnnotationProperty arg0) throws OWLException {}
//		public void visit(OWLAnnotationInstance arg0) throws OWLException {}
//		
//		public void visit(OWLFrame arg0) throws OWLException {}
//		public void visit(OWLOntology arg0) throws OWLException {}
//
//		public void visit(OWLDifferentIndividualsAxiom arg0) throws OWLException {}
//		public void visit(OWLDisjointClassesAxiom arg0) throws OWLException {}
//		public void visit(OWLEquivalentClassesAxiom arg0) throws OWLException {}
//		public void visit(OWLEquivalentPropertiesAxiom arg0) throws OWLException {}
//		public void visit(OWLSameIndividualsAxiom arg0) throws OWLException {}
//		public void visit(OWLSubClassAxiom arg0) throws OWLException {}
//		public void visit(OWLSubPropertyAxiom arg0) throws OWLException {}
//
//		/* (non-Javadoc)
//		 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLFunctionalPropertyAxiom)
//		 */
//		public void visit(OWLFunctionalPropertyAxiom node) throws OWLException {
//			// TODO Auto-generated method stub
//			
//		}
//
//		/* (non-Javadoc)
//		 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLInverseFunctionalPropertyAxiom)
//		 */
//		public void visit(OWLInverseFunctionalPropertyAxiom node) throws OWLException {
//			// TODO Auto-generated method stub
//			
//		}
//
//		/* (non-Javadoc)
//		 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLTransitivePropertyAxiom)
//		 */
//		public void visit(OWLTransitivePropertyAxiom node) throws OWLException {
//			// TODO Auto-generated method stub
//			
//		}
//
//		/* (non-Javadoc)
//		 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLSymmetricPropertyAxiom)
//		 */
//		public void visit(OWLSymmetricPropertyAxiom node) throws OWLException {
//			// TODO Auto-generated method stub
//			
//		}
//
//		/* (non-Javadoc)
//		 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLInversePropertyAxiom)
//		 */
//		public void visit(OWLInversePropertyAxiom node) throws OWLException {
//			// TODO Auto-generated method stub
//			
//		}
//
//		/* (non-Javadoc)
//		 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLPropertyDomainAxiom)
//		 */
//		public void visit(OWLPropertyDomainAxiom node) throws OWLException {
//			// TODO Auto-generated method stub
//			
//		}
//
//		/* (non-Javadoc)
//		 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLObjectPropertyRangeAxiom)
//		 */
//		public void visit(OWLObjectPropertyRangeAxiom node) throws OWLException {
//			// TODO Auto-generated method stub
//			
//		}
//
//		/* (non-Javadoc)
//		 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLDataPropertyRangeAxiom)
//		 */
//		public void visit(OWLDataPropertyRangeAxiom node) throws OWLException {
//			// TODO Auto-generated method stub
//			
//		}
//
//		/* (non-Javadoc)
//		 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLObjectPropertyInstance)
//		 */
//		public void visit(OWLObjectPropertyInstance node) throws OWLException {
//			// TODO Auto-generated method stub
//			
//		}
//
//		/* (non-Javadoc)
//		 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLDataPropertyInstance)
//		 */
//		public void visit(OWLDataPropertyInstance node) throws OWLException {
//			// TODO Auto-generated method stub
//			
//		}
//
//		/* (non-Javadoc)
//		 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLIndividualTypeAssertion)
//		 */
//		public void visit(OWLIndividualTypeAssertion node) throws OWLException {
//			// TODO Auto-generated method stub
//			
//		}
//	}
//	
//	
//	public static final String NAME = "Graphical View";
//	public static final int    DEFAULT_DEPTH = 2;
//	public static final int    DEFAULT_DISTANCE = 40;
//	
//	public static final String FORALL = "\u2200";  // all restriction
//	public static final String EXISTS = "\u2203";  // some restriction
//	public static final String MUSTBE= " = ";      // value restriction
//	//public static final String 
//	
//	public static final String EQUALS = "(=)";
//	public static final String GREATERTHAN = "(\u2265)";
//	public static final String LESSTHAN    = "(\u2264)";
//	public static final String SUBCLASSOF  = "\u2286";
//	public static final String EQUIVALENTTO = "\u2261";
//	
//	public static final String INTERSECTION = "\u2293";  // AND
//	public static final String UNION        = "\u2294";  // OR
//	public static final String NOT          = "\u00ac";  // NOT
//	
//	public static final String ISA          = "a"; 
//	
//	private Set myAllURIs;
//	private SwoopModel myModel;
//	private GraphPanel myPanel;
//	
//	public static final Color CENTER_NODE_COLOR = new Color(254, 155, 46);
//	public static final Color IND_NODE_COLOR    = new Color(255, 103, 141);
//	public static final Color LIT_NODE_COLOR    = new Color(35, 150, 228);
//	public static final Color OBJ_PROP_NODE_COLOR   = new Color(198, 106, 106);
//	public static final Color DATA_PROP_NODE_COLOR   = new Color(90, 146, 146);
//	//public static final Color CLASS_NODE_COLOR
//	
//    public HVScroll hvScroll;
//    public ZoomScroll zoomScroll;
//    //public HyperScroll hyperScroll; // unused
//    public RotateScroll rotateScroll;
//    public LocalityScroll localityScroll;
//	
//	public TouchGraphEntityRenderer()
//	{
//		init();
//	}
//	
//	private void init()
//	{
//		TGPanel p = new TGPanel();
//		myPanel = new GraphPanel( );
//        myPanel.setVisible(true);
//	}
//
//	
//	public void render(OWLEntity entity, SwoopModel swoopModel, Writer writer) throws RendererException {
//	    try{
//	    	fontSize = swoopModel.getFontSize();
//	    	OWLOntology ontology = swoopModel.getSelectedOntology();
//	    	this.myAllURIs = OntologyHelper.allURIs(ontology);
//	    	this.myModel = swoopModel;
//	    	
//	    	myPanel.clear();
//	    	
//	    	// set TemrsDisplay so it can receive HyperlinkEvents from GraphNagigateUI
//	    	myPanel.setTermsDisplay( swoopModel.getFrame().termDisplay );
//	    	
//	    	super.render(entity, swoopModel, new StringWriter() );
//	    }
//	    catch (OWLException ex){
//	    	ex.printStackTrace();
//	    }
//	  }
//	
//	
//	public String getName() {
//		return TouchGraphEntityRenderer.NAME;
//	}
//
//	public Component getDisplayComponent(SwoopDisplayPanel panel) 
//	{
//		if (!(panel instanceof TermsDisplay ))
//			throw new IllegalArgumentException();
//		
//		//myPanel.addHyperlinkListener( (TermsDisplay)panel );
//		return myPanel;
//	}
//
//
//	public SwoopRenderingVisitor createVisitor() {
//		return new TouchGraphVisitor( this, myPanel );
//	}
//
//	protected void renderAnnotationProperty(OWLAnnotationProperty prop) throws OWLException 
//	{
//		// nothing to do here
//	}
//
//	protected Node renderAnnotationContent(Object o) throws OWLException
//	{
//		
//		try{
//				if (o instanceof URI) 
//				{
//					
//					Node contentNode = new SwoopNode( null, super.shortForm((URI)o), myModel); 
//					myPanel.addNode( contentNode );
//					contentNode.setBackColor( LIT_NODE_COLOR );
//					return contentNode;
//				} 
//				else if (o instanceof OWLIndividual)
//				{
//					TouchGraphVisitor TV = (TouchGraphVisitor)visitor;
//					OWLIndividual ind = (OWLIndividual)o;
//					ind.accept(TV);
//					
//					Node indNode =  TV.getResult();					
//					return indNode;
//				}
//				else if (o instanceof OWLDataValue) {
//					OWLDataValue dv = (OWLDataValue) o;
//					
//					String value = "\"" + escape(dv.getValue()) + "\"";
//					
//					/* Only show it if it's not string */
//					URI uri = dv.getURI();
//					String dvlang = dv.getLang();
//					String garnish = "";
//					if (uri != null) {
//						garnish = "^^" + super.shortForm(uri);
//					} 
//					else 
//					{
//						if (dvlang != null)
//						 garnish =  "@" + dvlang;
//					}
//					
//					// add to graph
//					Node contentNode = new SwoopNode( null, value + garnish, myModel); 
//					myPanel.addNode( contentNode );
//					contentNode.setBackColor( LIT_NODE_COLOR );
//					return contentNode;
//				} 
//				else 
//				{
//					Node contentNode = new SwoopNode( null, o.toString(), myModel); 
//					myPanel.addNode(contentNode );
//					contentNode.setBackColor( LIT_NODE_COLOR );
//					return contentNode;
//				}
//			}
//			catch ( TGException tge )
//			{
//            	tge.printStackTrace();
//        	}
//		return null;
//	}	
//
//
//	/** Render the annotations for an object */
//	protected void renderAnnotations(OWLNamedObject object, Node source ) throws OWLException 
//	{
//		/* Bit nasty this -- annotations result in a new axiom */
//		TouchGraphVisitor TV = (TouchGraphVisitor)visitor;		
//		
//		try
//		{
//			if (!object.getAnnotations(reasoner.getOntology()).isEmpty()) {
//				for (Iterator it = object.getAnnotations(reasoner.getOntology()).iterator(); it.hasNext();) {
//					OWLAnnotationInstance oai = (OWLAnnotationInstance) it.next();
//					
//					String annotationType = super.shortForm(oai.getProperty().getURI());
//						
//					// Just whack out the content. This isn't quite right... 
//					Node annotationContent = renderAnnotationContent(oai.getContent() );					
//					myPanel.addNode( annotationContent );
//					
//					Edge edge = new Edge( source, annotationContent, DEFAULT_DISTANCE);
//					edge.setID( annotationType );
//					myPanel.addEdge( edge );
//				}
//			}		
//		}
//		catch ( TGException tge ) {
//            tge.printStackTrace();
//        }
//	}	
//
//	protected void renderClass(OWLClass clazz) throws OWLException 
//	{
//		//System.out.println( "renderingClass");
//        try {
//        	OWLClass owlThing = reasoner.getOntology().getOWLDataFactory().getOWLThing();			
//			TouchGraphVisitor TV =  (TouchGraphVisitor)visitor;
//			
//			// rendering class node
//			String classQName = super.shortForm( clazz.getURI() );
//			URI classURI = clazz.getURI();
//			Node classNode = new SwoopNode( classURI, classURI.toString(), classQName, myModel ); 
//			myPanel.addNode( classNode );
//
//			classNode.setBackColor( CENTER_NODE_COLOR );
//			
//			// rendering type
//			URI uri = new URI(OWL_CLASS);
//			Node prop = new SwoopNode( uri, super.shortForm(uri), myModel); 
//			myPanel.addNode( prop );
//			Edge edge = new Edge( classNode, prop, DEFAULT_DISTANCE );
//			myPanel.addEdge( edge );
//			edge.setID("a");
//			
//			// rendering ANNOTATIONS
//			if(!clazz.getAnnotations(reasoner.getOntology()).isEmpty()) {
//				renderAnnotations(clazz, classNode);
//			}
//			  
//			// render INTERSECTIONs equivalent to this class
//			Iterator it = OWLDescriptionFinder.getIntersections(clazz, reasoner.getOntologies()).iterator();
//			
//			while(it.hasNext()) {
//				OWLAnd intersection = (OWLAnd) it.next();
//
//				intersection.accept(TV);
//				Node branchNode = TV.getResult();
//				Node andNode = TV.getResult();
//				Edge equiEdge = new Edge( classNode, andNode, DEFAULT_DISTANCE);
//				equiEdge.setID( EQUIVALENTTO );
//				myPanel.addEdge( equiEdge );
//			}
//			
//			// render UNIONs equivalent to this class
//			it = OWLDescriptionFinder.getUnions(clazz, reasoner.getOntologies()).iterator();
//			while(it.hasNext()) {
//				OWLOr union = (OWLOr) it.next();
//				union.accept(TV);
//				Node orNode = TV.getResult();
//				Edge equiEdge = new Edge( classNode, orNode, DEFAULT_DISTANCE);
//				equiEdge.setID( EQUIVALENTTO );
//				myPanel.addEdge( equiEdge );
//			}
//			
//			// render ENUMERATIONs of classes that are equivalent to this class
//			it = OWLDescriptionFinder.getEnumerations(clazz, reasoner.getOntologies()).iterator();
//			while(it.hasNext()) {
//				OWLEnumeration oneOf = (OWLEnumeration) it.next();
//				oneOf.accept(TV);
//				Node enumNode = TV.getResult();
//				Edge enumEdge = new Edge( classNode, enumNode, DEFAULT_DISTANCE);
//				enumEdge.setID( TV.result() );
//				myPanel.addEdge( enumEdge );		
//			}
//
//			// print EQUIVALENT classes
//			Set eqs = OWLDescriptionFinder.getEquivalentClasses(clazz, reasoner.getOntologies());
//			it = eqs.iterator();			
//			while(it.hasNext()) {
//				OWLDescription desc = (OWLDescription) it.next();
//				if(!(desc instanceof OWLRestriction || desc instanceof OWLClass))
//					it.remove();
//			}
//			if(reasoner.isConsistent(clazz))
//				eqs.addAll(reasoner.equivalentClassesOf(clazz));  // add all named equivalent classes
//			it = eqs.iterator();
//			
//			uri = new URI(OWL_EQUIVALENTCLASS);
//			String eqName = super.shortForm( uri );
//			
//			while (it.hasNext())
//			{
//				OWLDescription eq = (OWLDescription)it.next();
//				TV.reset();
//				eq.accept(TV);		
//				Node equivalenceNode = TV.getResult();
//				Edge eqEdge = new Edge( classNode, equivalenceNode, DEFAULT_DISTANCE );
//				eqEdge.setID( eqName );
//				myPanel.addEdge( eqEdge );
//			}
//			
//			// DISJOINT CLASSES
//			Set disjoints = OWLDescriptionFinder.getDisjoints(clazz, reasoner.getOntologies());
//			uri = new URI(OWL_DISJOINTWITH);
//			Node disjointNode = new Node( super.shortForm( uri ));
//			for (it = disjoints.iterator(); it.hasNext(); )
//			{
//				OWLDescription desc = (OWLDescription)it.next();
//				TV.reset();
//				desc.accept(TV);
//				Node distNode = TV.getResult();
//				Edge distEdge = new Edge( classNode, distNode, DEFAULT_DISTANCE);
//				distEdge.setID( super.shortForm(uri) );
//				myPanel.addEdge( distEdge );
//			}
//
//			
//			// SUBCLASS OF
//			
//			// acquiring all non-inferred super classes (named or anon)
//			Set supers = OWLDescriptionFinder.getSuperClasses(clazz, reasoner.getOntologies());
//			if(reasoner.isConsistent(clazz)) {
//				// remove all the named classes because reasoner will eventually add them
//				it = supers.iterator();
//				while(it.hasNext())
//					if(it.next() instanceof OWLClass)
//						it.remove();		
//				// add all the named superclasses (including inferred)
//				supers.addAll(reasoner.superClassesOf(clazz));
//				// remove owl:Thing from the superclass set
//				it = supers.iterator();
//				while(it.hasNext()) {
//					Object o = it.next();
//					if(o instanceof Set && ((Set)o).contains(owlThing))
//						it.remove();
//				}
//			}
//			uri = new URI(RDFS_SUBCLASSOF);
//			Node subClassNode = new Node( );
//			subClassNode.setType( Node.TYPE_NOTHING );
//			
//			for (it = supers.iterator(); it.hasNext(); )
//			{
//				Object obj = (Object)it.next();				
//				// if there is more than one element for this line
//				// we only print the first one. rest are either
//				// equivalent classes (or properties) 
//				if(obj instanceof Collection)
//					obj = ((Collection)obj).iterator().next();				
//				TV.reset();
//				OWLDescription desc = (OWLDescription)obj;
//				desc.accept(TV);
//				Node superNode = TV.getResult();
//				Edge superEdge = new Edge( classNode, superNode, DEFAULT_DISTANCE);
//				superEdge.setID( SUBCLASSOF );
//				myPanel.addEdge( superEdge );
//			}
//			
//        } catch ( TGException tge ) {
//            tge.printStackTrace();
//        }catch ( URISyntaxException ex){
//        	ex.printStackTrace();
//        }
//	}
//
//	
//	protected void renderObjectProperty(OWLObjectProperty prop) throws OWLException 
//	{
//		try{
//			TouchGraphVisitor TV =  (TouchGraphVisitor)visitor;			
//			
//			// rendering prop node
//			String propName = super.shortForm( prop.getURI() );
//			URI uri = prop.getURI();
//			Node propNode = new SwoopNode( uri, uri.toString(), propName, myModel); 
//			myPanel.addNode( propNode );
//			propNode.setType( Node.TYPE_ELLIPSE );
//			propNode.setBackColor( CENTER_NODE_COLOR );
//			
//			// rendering type
//			uri = new URI(OWL_OBJECTPROPERTY);
//			Node propTypeNode = new SwoopNode( uri, super.shortForm( uri ), myModel ); 
//			myPanel.addNode( propTypeNode );
//			Edge propEdge = new Edge( propNode, propTypeNode, DEFAULT_DISTANCE );
//			propEdge.setID( ISA );
//			myPanel.addEdge( propEdge );
//			
//			// annotations
//			if(!prop.getAnnotations(reasoner.getOntology()).isEmpty()) {
//				renderAnnotations(prop, propNode);
//			}
//		
//			// is TRANSITIVE?
//			
//			if (prop.isTransitive(reasoner.getOntologies())) {
//				URI typeURI = new URI(OWL_TRANSITIVEPROP);
//				Node node = new SwoopNode( uri, super.shortForm(uri), myModel); 
//				myPanel.addNode( node ) ;
//				Edge edge =  new Edge( propNode, node, DEFAULT_DISTANCE );
//				edge.setID( ISA );
//				myPanel.addEdge( edge );
//			}
//			
//			// is FUNCTIONAL?
//			
//			if (prop.isFunctional(reasoner.getOntologies())) {
//				URI typeURI = new URI(OWL_FUNCTIONALPROP);
//				Node node = new SwoopNode( uri, super.shortForm(uri), myModel);  
//				myPanel.addNode( node ) ;
//				Edge edge =  new Edge( propNode, node, DEFAULT_DISTANCE );
//				edge.setID( ISA );
//				myPanel.addEdge( edge );
//			}
//			
//			// is INVERSE FUNCTIONAL?
//			
//			if (prop.isInverseFunctional(reasoner.getOntologies())) {
//				URI typeURI = new URI(OWL_INVERSEFUNCTIONALPROP);
//				Node node = new SwoopNode( uri, super.shortForm(uri), myModel);
//				myPanel.addNode( node ) ;
//				Edge edge =  new Edge( propNode, node, DEFAULT_DISTANCE );
//				edge.setID( ISA );
//				myPanel.addEdge( edge );
//			}
//			
//			// is SYMMETRIC?
//			
//			if (prop.isSymmetric(reasoner.getOntologies())) {
//				URI typeURI = new URI(OWL_SYMMETRICPROP);
//				Node node = new SwoopNode( uri, super.shortForm(uri), myModel);
//				myPanel.addNode( node ) ;
//				Edge edge =  new Edge( propNode, node, DEFAULT_DISTANCE );
//				edge.setID( ISA );
//				myPanel.addEdge( edge );
//			}
//			
//			// EQUIVALENT properties
//			uri = new URI(OWL_EQUIVALENTPROP);
//			Set equiSet = reasoner.equivalentPropertiesOf(prop);
//			
//			for (Iterator it = equiSet.iterator(); it.hasNext();) {			
//				Object equi = (Object) it.next();
//				OWLObjectProperty op = (OWLObjectProperty)equi;
//				TV.reset();
//				op.accept(TV);
//				Node result = TV.getResult();
//				Edge edge = new Edge( propNode, result, DEFAULT_DISTANCE );
//				edge.setID( EQUIVALENTTO );
//				myPanel.addEdge( edge );
//				myPanel.addNode( result );
//			}
//			
//			// INVERSE
//			uri = new URI(OWL_INVERSEOF);
//			Set invSet = prop.getInverses(reasoner.getOntologies());
//			for (Iterator it = invSet.iterator(); it.hasNext();) 
//			{
//				OWLObjectProperty inv = (OWLObjectProperty) it.next();
//				TV.reset();
//				inv.accept(TV);
//				Node result = TV.getResult();
//				
//				Edge edge = new Edge( propNode, result, DEFAULT_DISTANCE );
//				edge.setID( super.shortForm( uri ) );
//				myPanel.addEdge( edge );
//				myPanel.addNode( result );
//			}
//	
//			// DOMAIN
//			uri = new URI(RDFS_DOMAIN);
//			Set domainSet = reasoner.domainsOf(prop);
//			for (Iterator it = domainSet.iterator(); it.hasNext();) {
//				OWLDescription dom = (OWLDescription) it.next();
//				TV.reset();
//				dom.accept(TV);
//				
//				Node result = TV.getResult();
//				Edge edge = new Edge( propNode, result, DEFAULT_DISTANCE );
//				edge.setID( super.shortForm( uri ) );
//				myPanel.addEdge( edge );
//				myPanel.addNode( result );
//			}
//				
//			// RANGE
//			uri = new URI(RDFS_RANGE);
//			Set rangeSet = reasoner.rangesOf(prop);
//			for (Iterator it = rangeSet.iterator(); it.hasNext();) 
//			{
//				OWLDescription ran = (OWLDescription) it.next();
//				TV.reset();
//				ran.accept(TV);
//				
//				Node result = TV.getResult();
//				Edge edge = new Edge( propNode, result, DEFAULT_DISTANCE );
//				edge.setID( super.shortForm( uri ) );
//				myPanel.addEdge( edge );
//				myPanel.addNode( result );
//			}
//			
//			// print out superclasses (SUBCLASSOF)
//			uri = new URI(RDFS_SUBPROPERTYOF);
//			Set superSet = reasoner.superPropertiesOf(prop);
//			for (Iterator it = superSet.iterator(); it.hasNext();) 
//			{
//				Object Obj = it.next();
//				if (Obj instanceof OWLObjectProperty)
//				{
//					OWLObjectProperty sup = (OWLObjectProperty)Obj;
//					TV.reset();
//					sup.accept(TV);
//					
//					Node result = TV.getResult();					
//					Edge edge = new Edge( propNode, result, DEFAULT_DISTANCE );
//					edge.setID( ISA );
//					myPanel.addEdge( edge );
//					myPanel.addNode( result );
//				}
//				else // could be a set of ObjProperties ... very strange
//				{
//					Set set = (Set)Obj;
//					for (Iterator i = set.iterator(); i.hasNext();)
//					{
//						OWLObjectProperty sup = (OWLObjectProperty)i.next();
//						TV.reset();
//						sup.accept(TV);
//						
//						Node result = TV.getResult();
//						Edge edge = new Edge( propNode, result, DEFAULT_DISTANCE );
//						edge.setID( ISA );
//						myPanel.addEdge( edge );
//						myPanel.addNode( result );
//					}
//				}
//			}
//		}
//		catch (Exception ex)
//		{ ex.printStackTrace(); }
//	}
//	
//	
//	protected void renderDataProperty(OWLDataProperty prop) throws OWLException 
//	{
//		try{
//			TouchGraphVisitor TV =  (TouchGraphVisitor)visitor;			
//			
//			// rendering prop node
//			String propName = super.shortForm( prop.getURI() );
//			URI uri = prop.getURI();
//			Node propNode = new SwoopNode( uri, uri.toString(), super.shortForm(uri), myModel); 
//			myPanel.addNode( propNode );
//			propNode.setType( Node.TYPE_ELLIPSE );
//			propNode.setBackColor( CENTER_NODE_COLOR );
//			
//			// rendering type
//			uri = new URI(OWL_DATAPROPERTY);
//			Node propTypeNode = new SwoopNode( uri, super.shortForm( uri ), myModel);
//			myPanel.addNode( propTypeNode );
//			Edge propEdge = new Edge( propNode, propTypeNode, DEFAULT_DISTANCE );
//			propEdge.setID(ISA);
//			myPanel.addEdge( propEdge );
//			
//			// annotations
//			if(!prop.getAnnotations(reasoner.getOntology()).isEmpty()) {
//				renderAnnotations(prop, propNode);
//			}
//			
//			// is functional
//			if (prop.isFunctional(reasoner.getOntologies())) {
//				URI typeURI = new URI(OWL_FUNCTIONALPROP);
//
//				Node node = new SwoopNode( typeURI, super.shortForm( typeURI ), myModel ); 
//				myPanel.addNode( node ) ;
//				Edge edge =  new Edge( propNode, node, DEFAULT_DISTANCE );
//				edge.setID( ISA );
//				myPanel.addEdge( edge );
//			}
//						
//			// domain
//			URI domainURI = new URI(RDFS_DOMAIN);
//			Set domainSet = reasoner.domainsOf(prop);
//			for (Iterator it = domainSet.iterator(); it.hasNext();)
//			{				
//				OWLDescription dom = (OWLDescription) it.next();
//				TV.reset();
//				dom.accept(TV);
//				
//				Node result = TV.getResult();
//				Edge edge = new Edge( propNode, result, DEFAULT_DISTANCE );
//				edge.setID( super.shortForm( domainURI ) );
//				myPanel.addEdge( edge );
//				myPanel.addNode( result );
//			}
//			
//			// range
//			URI rangeURI = new URI(RDFS_RANGE);
//			Set rangeSet = reasoner.rangesOf(prop);
//			for (Iterator it = rangeSet.iterator(); it.hasNext();) 
//			{
//				OWLDataRange ran = (OWLDataRange) it.next();
//				TV.reset();
//				ran.accept(TV);
//				
//				Node result = TV.getResult();
//				Edge edge = new Edge( propNode, result, DEFAULT_DISTANCE );
//				edge.setID( super.shortForm( rangeURI ) );
//				myPanel.addEdge( edge );
//				myPanel.addNode( result );
//			}
//			
//			// print out its superclasses
//			URI subPropURI = new URI(RDFS_SUBPROPERTYOF);
//			Set subPropSet = reasoner.superPropertiesOf(prop);
//			for (Iterator it = subPropSet.iterator(); it.hasNext();) 
//			{
//				Object obj = it.next();
//				if (obj instanceof OWLDescription)
//				{
//					OWLDescription dom = (OWLDescription) it.next();
//					TV.reset();
//					dom.accept(TV);
//					
//					Node result = TV.getResult();
//					Edge edge = new Edge( propNode, result, DEFAULT_DISTANCE );
//					edge.setID( super.shortForm( domainURI ) );
//					myPanel.addEdge( edge );
//					myPanel.addNode( result );
//				}
//				else // could be a set of OWLDataproperty
//				{
//					Set set = (Set)obj;
//					for (Iterator i = set.iterator(); i.hasNext();)
//					{
//						OWLDataProperty dom = (OWLDataProperty)i.next();
//						TV.reset();
//						dom.accept(TV);
//
//						Node result = TV.getResult();
//						Edge edge = new Edge( propNode, result, DEFAULT_DISTANCE );
//						edge.setID( super.shortForm( domainURI ) );
//						myPanel.addEdge( edge );
//						myPanel.addNode( result );
//					}
//				}
//			}		
//		}
//		catch (Exception ex)
//		{ ex.printStackTrace(); }
//	}
//
//
//
//	protected void renderIndividual(OWLIndividual ind) throws OWLException 
//	{
//		
//		try{
//			TouchGraphVisitor TV =  (TouchGraphVisitor)visitor;
//			OWLClass owlThing = reasoner.getOntology().getOWLDataFactory().getOWLThing();
//			
//			String indName = "";
//			if ( !ind.isAnonymous() ) 
//				indName = super.shortForm(ind.getURI());
//			
//			URI uri = ind.getURI();
//			Node indNode = new SwoopNode( uri, uri.toString(), indName, myModel); 
//			myPanel.addNode( indNode );
//			indNode.setBackColor( CENTER_NODE_COLOR );
//			
//			// defined types
//			
//			Set types = OWLDescriptionFinder.getTypes(ind, reasoner.getOntologies());
//
//			URI typeURI = new URI(RDF_TYPE_URI);
//			Node typeNode = new SwoopNode( typeURI, super.shortForm(typeURI), myModel ); 
//			
//			for (Iterator it = types.iterator(); it.hasNext();) {
//				Object obj = (Object)it.next();				
//				// if there is more than one element for this line
//				// we only print the first one. rest are either
//				// equivalent classes (or properties) 
//				if(obj instanceof Collection)
//					obj = ((Collection)obj).iterator().next();				
//				OWLDescription desc = (OWLDescription)obj;
//				TV.reset();
//				desc.accept(TV);
//				myPanel.addEdge( typeNode, TV.getResult(), DEFAULT_DISTANCE );
//			}
//			if ( !types.isEmpty() )
//			{
//				myPanel.addNode( typeNode );
//				myPanel.addEdge( indNode, typeNode, DEFAULT_DISTANCE );
//			}
//			
//			// annotations	
//			if(!ind.getAnnotations(reasoner.getOntology()).isEmpty()) {
//				renderAnnotations(ind, indNode);
//			}	
//			
//			// object property values
//			Map propertyValues = ind.getObjectPropertyValues(reasoner.getOntologies());
//			Set keySet = propertyValues.keySet();
//			for (Iterator it = keySet.iterator(); it.hasNext();) {
//				OWLObjectProperty prop = (OWLObjectProperty) it.next();
//				Set vals = (Set) propertyValues.get(prop);
//				for (Iterator valIt = vals.iterator(); valIt.hasNext();) {
//					OWLIndividual oi = (OWLIndividual) valIt.next();
//					TV.reset();
//					oi.accept(TV);
//					URI propURI = prop.getURI();
//					Node propNode = new SwoopNode( propURI, propURI.toString(), super.shortForm( propURI ), myModel); 
//					myPanel.addNode( propNode );
//					myPanel.addEdge( indNode, propNode, DEFAULT_DISTANCE );
//					myPanel.addEdge( propNode, TV.getResult(), DEFAULT_DISTANCE);
//				}
//			}
//					
//			// data property values
//			Map dataValues = ind.getDataPropertyValues(reasoner.getOntologies());
//			for (Iterator it = dataValues.keySet().iterator(); it.hasNext();) {
//				OWLDataProperty prop = (OWLDataProperty) it.next();
//				Set vals = (Set) dataValues.get(prop);
//				for (Iterator valIt = vals.iterator(); valIt.hasNext();) {
//					OWLDataValue dtv = (OWLDataValue) valIt.next();
//					TV.reset();
//					dtv.accept(TV);
//					URI propURI = prop.getURI();
//					Node propNode = new SwoopNode( propURI, propURI.toString(), super.shortForm(propURI), myModel); 
//					myPanel.addNode( propNode );
//					myPanel.addEdge( propNode, TV.getResult(), DEFAULT_DISTANCE );
//					myPanel.addEdge( indNode, propNode, DEFAULT_DISTANCE );
//				}
//			}			
//		}
//		catch (Exception ex)
//		{ ex.printStackTrace(); }
//	}
//
//	
//	private void DEBUG()
//	{
//		Iterator it = myPanel.getAllNodes();
//		for (;it.hasNext();)
//		{
//			Node n = (Node)it.next();
//			System.out.println( n.getID() );
//		}
//	}
//
//	protected void renderDataType(OWLDataType datatype) throws OWLException 
//	{
//		// nothing to do here
//	}
//	
//	protected void renderForeignEntity(OWLEntity ent) throws OWLException 
//	{
//		// nothing to do here
//	}
	
}