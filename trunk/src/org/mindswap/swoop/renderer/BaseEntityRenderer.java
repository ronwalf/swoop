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

package org.mindswap.swoop.renderer;

import java.io.PrintWriter;
import java.io.Writer;
import java.net.URI;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JEditorPane;

import org.apache.commons.lang.StringEscapeUtils;
import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.TermsDisplay;
import org.mindswap.swoop.reasoner.SwoopReasoner;
import org.semanticweb.owl.io.RendererException;
import org.semanticweb.owl.io.ShortFormProvider;
import org.semanticweb.owl.model.OWLAnnotationProperty;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataType;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOr;
import org.semanticweb.owl.model.OWLProperty;

/**
 * @author Evren Sirin
 */
public abstract class BaseEntityRenderer implements ShortFormProvider {
	protected SwoopModel swoopModel;

	protected SwoopReasoner reasoner;
	protected OWLEntity entity;
	protected Writer writer;
	
	protected PrintWriter pw;
	
	public SwoopRenderingVisitor visitor;
	protected String fontSize;
	
	protected boolean editorEnabled, showInherited, showDivisions, showImports;
	
	/*
	 * Constructor
	 */
	public BaseEntityRenderer() {
//		this.swoopModel = new SwoopModel();
	}
	
	public abstract SwoopRenderingVisitor createVisitor();
	
	/*
	 * Utility method to set model so one can use visitor without using the render method
	 *   Without setting swoopModel, every call to shortForm will fail (NullPointerException) 
	 */
	public void setSwoopModel( SwoopModel model)
	{
		this.swoopModel = model;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mindswap.swoop.SwoopEntityRenderer#render(org.semanticweb.owl.model.OWLEntity,
	 *      org.mindswap.swoop.SwoopReasoner, java.io.Writer)
	 */
	public void render(OWLEntity entity, SwoopModel swoopModel, Writer writer) throws RendererException {		
		try {
			this.swoopModel = swoopModel;
			this.reasoner = swoopModel.getReasoner();
			this.entity = entity;
			this.writer = writer;
			this.pw = new PrintWriter(writer);
			
			this.editorEnabled = swoopModel.getEditorEnabled();
			this.showInherited = swoopModel.getShowInherited();
			this.showDivisions = swoopModel.getShowDivisions();
			this.showImports = swoopModel.getShowImports();
			
			visitor = createVisitor();
			fontSize = swoopModel.getFontSize();
			
			renderEntity();
		} catch (OWLException e) {
			throw new RendererException(e.getMessage());
		}		
	}

	/**
	 * renderEntity
	 * 
	 * 
	 */
	protected void renderEntity() throws OWLException {
		//*********************************************
		//Changed For Econnections
		//********************************************
		
		if(entity instanceof OWLClass) 
			if(!swoopModel.getSelectedOntology().getForeignEntities().containsKey(entity))
				renderClass((OWLClass) entity);
			else
				renderForeignEntity(entity);
		else if(entity instanceof OWLObjectProperty) 
			if(!swoopModel.getSelectedOntology().getForeignEntities().containsKey(entity))
				renderObjectProperty((OWLObjectProperty) entity);
			else
				renderForeignEntity(entity);
		else if(entity instanceof OWLDataProperty) 
			if(!swoopModel.getSelectedOntology().getForeignEntities().containsKey(entity))
					renderDataProperty((OWLDataProperty) entity);
			else
				renderForeignEntity(entity);
		else if(entity instanceof OWLIndividual)
			if(!swoopModel.getSelectedOntology().getForeignEntities().containsKey(entity))
				 renderIndividual((OWLIndividual) entity);
			else
				renderForeignEntity(entity);
		else if(entity instanceof OWLAnnotationProperty)
			renderAnnotationProperty((OWLAnnotationProperty) entity);
		else
		{
			System.out.println( "BaseEntityRenderer: entity is of type = " + entity.getClass().getName());
			throw new RuntimeException("This is not possible!");
		}
	}
	
	public Set getPropertiesWithDomain(OWLClass c, boolean inherited) throws OWLException {
		Set set = new HashSet();
		Iterator i = reasoner.getProperties().iterator();
		while(i.hasNext()) {
			OWLProperty p =  (OWLProperty) i.next();
			
			// skip owl:AnnotationProperties
			if (p instanceof OWLAnnotationProperty) continue;
			
			Set domSet = reasoner.domainsOf(p);
			 
			// consider all properties with no domain (of OWL:Thing)
//			if (inherited && domSet.size()==0) set.add(p);
			
			if (domSet.size()>0) {
				Object obj = null;
				// check if domSet contains union
				if ((obj = domSet.iterator().next()) instanceof OWLOr) {
					OWLOr unionSet = (OWLOr) obj;
					if (unionSet.getOperands().contains(c)) set.add(p);
				}
				else {
					if (domSet.contains(c))
					set.add(p);
				}
			}
		}
		
		if (inherited) {
			
			// also consider properties of subclasses of c
			Set allSubCSet = reasoner.descendantClassesOf(c);
			Iterator supCIter = allSubCSet.iterator();
			while (supCIter.hasNext()) {
				Set supCSet = (HashSet) supCIter.next();
				OWLDescription supC = (OWLDescription) supCSet.iterator().next();
				if (supC instanceof OWLClass) {
					set.addAll(getPropertiesWithDomain((OWLClass) supC, false));
				}
			}
		}
		return set;
	}

	protected Set getPropertiesWithRange(OWLClass c, boolean inherited) throws OWLException {
		Set set = new HashSet();
		Iterator i = reasoner.getProperties().iterator();
		while(i.hasNext()) {
			OWLProperty p =  (OWLProperty) i.next();
			
			// skip owl:AnnotationProperties
			if (p instanceof OWLAnnotationProperty) continue;
			
			if(reasoner.rangesOf(p).contains(c))
				set.add(p);			
		}
		
		if (inherited) {
			// also consider properties of subclasses of c
			Set allSubCSet = reasoner.descendantClassesOf(c);
			Iterator supCIter = allSubCSet.iterator();
			while (supCIter.hasNext()) {
				Set supCSet = (HashSet) supCIter.next();
				OWLDescription supC = (OWLDescription) supCSet.iterator().next();
				if (supC instanceof OWLClass) {
					set.addAll(getPropertiesWithRange((OWLClass) supC, false));
				}
			}
		}
		
		return set;		
	}
	
	/**
	 * Create a JEditorPane given the contentType (text/plain or text/html)
	 * and make other default settings (add hyperlink listener, editable false)
	 * @param contentType
	 * @return
	 */
	public static JEditorPane getEditorPane(String contentType, TermsDisplay TD) {
		JEditorPane editorPane = null;
		if(contentType.equals("text/plain"))
			editorPane = new JEditorPane();
		else if(contentType.equals("text/html")) {
			editorPane = new JEditorPane();
			editorPane.addHyperlinkListener( TD );	
		}
		else if(contentType.equals("text/xml"))
			editorPane = new JEditorPane();
		else
			throw new RuntimeException("Cannot create an editor pane for content type " + contentType);
		
		editorPane.setEditable(false);
		editorPane.setContentType(contentType);
		
		// adding to UI listeners of TermsDisplay
		editorPane.getDocument().addDocumentListener(TD);
		editorPane.addMouseListener(TD);			
		editorPane.addKeyListener(TD);
		
		return editorPane;
	}
	
	
	
	abstract protected void renderAnnotationProperty(OWLAnnotationProperty prop) throws OWLException ;
	abstract protected void renderClass(OWLClass clazz) throws OWLException ;
	abstract protected void renderDataProperty(OWLDataProperty prop) throws OWLException ;
	abstract protected void renderDataType(OWLDataType datatype) throws OWLException ;
	abstract protected void renderIndividual(OWLIndividual ind) throws OWLException ;
	abstract protected void renderObjectProperty(OWLObjectProperty prop) throws OWLException ;
	//************************************************
	//Added for Econnections
	//**********************************************
	abstract protected void renderForeignEntity(OWLEntity ent) throws OWLException ;
	
	//**********************************************************
	public String shortForm(URI uri) {
		return swoopModel.shortForm(uri);
	}

	/* Return a collection, ordered by the URIs. */
	protected SortedSet orderedEntities(Set entities) {
		SortedSet ss = new TreeSet(new Comparator() {
			public int compare(Object o1, Object o2) {
				try {
					return ((OWLEntity) o1).getURI().toString().compareTo(
						((OWLEntity) o2).getURI().toString());
				} catch (Exception ex) {
					return o1.toString().compareTo(o2.toString());
				}
			}
		});
		ss.addAll(entities);
		return ss;
	}


	/**
	 * HTML-escape an object
	 * @param o
	 * @return
	 */
	protected static String escape(Object o) {
		return StringEscapeUtils.escapeHtml(o.toString());
	}

	public void printObject(OWLObject obj) throws OWLException {
		visitor.reset();
		obj.accept(visitor);
		print(visitor.result());		
	}
	
	protected void print(String str) {
		pw.print(str);	
	}
	
	protected void println(String str) {
		print(str);
		println();
	}
	
	protected void println() {
		println();		
	}	
	
	/**
	 * Used to render a single OWLObject alone in its Concise Format form
	 * @param obj - OWLObject to be rendered
	 * @param swoopModel
	 * @param writer - results get written to this stream
	 * @throws RendererException
	 */
	public void renderObject(OWLObject obj, SwoopModel swoopModel, Writer writer) throws RendererException {		
		try {
			this.swoopModel = swoopModel;
			this.reasoner = swoopModel.getReasoner();
			this.writer = writer;
			this.pw = new PrintWriter(writer);
			
			visitor = createVisitor();
			fontSize = swoopModel.getFontSize();
			
			printObject(obj);
		} catch (OWLException e) {
			throw new RendererException(e.getMessage());
		}		
	}
	
} // Renderer
