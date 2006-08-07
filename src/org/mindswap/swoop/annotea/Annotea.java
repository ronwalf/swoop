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

package org.mindswap.swoop.annotea;

import java.net.URI;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import org.mindswap.swoop.SwoopModel;
import org.semanticweb.owl.impl.model.OWLConnectionImpl;
import org.semanticweb.owl.io.Parser;
import org.semanticweb.owl.io.owl_rdf.OWLRDFErrorHandler;
import org.semanticweb.owl.io.owl_rdf.OWLRDFParser;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLOntology;
import org.xml.sax.SAXException;

/**
 * @author Aditya
 *
 * Constants and such for the Annotea services to use.
 */
public class Annotea {
	
	static String RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	public static String StandardNS = "http://www.w3.org/2000/10/annotation-ns#";
	static String Annotation = StandardNS + "Annotation";
	static String DublinCore = "http://purl.org/dc/elements/1.1/";
	static String AnnoteaOntology = "http://www.mindswap.org/2004/owl/annotations/";
	static String AnnoteaTypeOntology = "http://www.w3.org/2000/10/annotationType";
	public static Set annotationTypes;
	
	final public static int ALL            = 0;
	final public static int INDIVIDUALS    = 1;
	final public static int CLASSES        = 2;
	final public static int PROPERTIES     = 3;
	final public static int DATAPROPERTIES = 4;
	final public static int OBJPROPERTIES  = 5;
	
	protected static OWLOntology annoteaOntology = null;
	protected static OWLOntology annoteaTypeOntology = null;
	protected static Map annoteaMap = null;
	
	public static Annotea INSTANCE;
	
	public Annotea() {
		try {
			System.out.println("Connecting to the MINDSWAP Annotea Server for Initialization");
			getAnnoteaOntologies();
			buildAnnoteaMap();
			getAnnotationTypes();
			
		} catch (AnnoteaException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "Error Initializing Annotea Library", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
	
	public static void initializeAnnotea() {
		INSTANCE = new Annotea();
	}
	
	public static OWLOntology getAnnoteaOntologies() throws AnnoteaException {
		
		if (annoteaOntology == null) {
			try {				
				OWLRDFParser parser = new OWLRDFParser();
				parser.setOWLRDFErrorHandler(new OWLRDFErrorHandler() {
					public void owlFullConstruct(int code, String message)
							throws SAXException {
					}

					public void error(String message) throws SAXException {
						throw new SAXException(message.toString());
					}

					public void warning(String message) throws SAXException {
						//System.out.println("RDFParser: " + message.toString());
					}

					public void owlFullConstruct(int code, String message, Object obj) throws SAXException {
						// TODO Auto-generated method stub
						
					}
				});
				// load and parse Annotea Ontology (from MINDSWAP)
				URI location = new URI(AnnoteaOntology);
				parser.setConnection(new OWLConnectionImpl());
				annoteaOntology = parser.parseOntology(location);
				
				// load and parse AnnoteaType Ontology (from W3C)
				URI location2 = new URI(AnnoteaTypeOntology);
				parser.setConnection(new OWLConnectionImpl());
				annoteaTypeOntology = parser.parseOntology(location2);
				
			} catch (Exception e) {
				throw new AnnoteaException(e);
			}
		}
		return annoteaOntology;
	}
	
	/**
	 * Build a hashtable containing all the Standard Annotea Classes
	 * and Properties
	 */
	public static void buildAnnoteaMap() throws AnnoteaException {
		
		if (annoteaOntology!=null) {
			annoteaMap = new Hashtable();
			// create a set of all entites present in our OWL Annotea ontology
			Set entitySet = new HashSet();
			try {
				// add all entities from Annotea Ontology
				entitySet.addAll(annoteaOntology.getClasses());
				entitySet.addAll(annoteaOntology.getDataProperties());
				entitySet.addAll(annoteaOntology.getObjectProperties());
				entitySet.addAll(getImportedSet(annoteaOntology, CLASSES));
				entitySet.addAll(getImportedSet(annoteaOntology, DATAPROPERTIES));
				entitySet.addAll(getImportedSet(annoteaOntology, OBJPROPERTIES));
				
				// add all entities from Annotea Type Ontology
				entitySet.addAll(annoteaTypeOntology.getClasses());
				entitySet.addAll(annoteaTypeOntology.getDataProperties());
				entitySet.addAll(annoteaTypeOntology.getObjectProperties());

				// *** For Entity Definition String - adding a datatype property to store value (in HTML)
				URI defnDPURI = new URI(StandardNS + "entityDefinition");
				OWLDataProperty defnDProp = annoteaOntology.getOWLDataFactory().getOWLDataProperty(defnDPURI);
				entitySet.add(defnDProp);
				
				// *** For Ontology Change Sets
				// adding a dataproperty to store value = change set
				URI changeDPURI = new URI(StandardNS + "hasChangeSetRDF");
				OWLDataProperty changeDProp = annoteaOntology.getOWLDataFactory().getOWLDataProperty(changeDPURI);
				entitySet.add(changeDProp);
				changeDPURI = new URI(StandardNS + "hasChangeSetJAVA");
				changeDProp = annoteaOntology.getOWLDataFactory().getOWLDataProperty(changeDPURI);
				entitySet.add(changeDProp);
				// ***				
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
			
			// iterate over each entity in set and add it to hashtable
			Iterator iter = entitySet.iterator();
			while (iter.hasNext()) {
				Object object = iter.next();
				if (object instanceof OWLEntity) {
					OWLEntity entity = (OWLEntity) object;
					try {
						if (entity.getURI()!=null) {
							String entityName = getEntityName(entity.getURI());
							annoteaMap.put(entityName, entity);
						}
					} catch (OWLException e1) {
						e1.printStackTrace();
					}
				}
			}
		}
		
	}
	
	public static String getEntityName(URI uri) {
		String entityURI = uri.toString();
		String entityName = "";
		if (entityURI.indexOf("#")>=0) entityName = entityURI.substring(entityURI.indexOf("#")+1, entityURI.length());
		else entityName = entityURI.substring(entityURI.lastIndexOf("/")+1, entityURI.length());
		return entityName;
	}
	
	/**
	 * Get the set of all annotation types defined in the AnnoteaType ontology
	 *
	 */
	public static void getAnnotationTypes() {
		try {
			annotationTypes = annoteaTypeOntology.getClasses();
		} catch (OWLException e) {
			e.printStackTrace();
		}
	}
	
	public static Set getTransitiveClosureImports(OWLOntology ont) {
		// obtain all imported ontologies under transitive closure
		Set tcl = new HashSet();
		
		try {
			Iterator iter = ont.getIncludedOntologies().iterator();
			while (iter.hasNext()) {
				OWLOntology inclOnt = (OWLOntology) iter.next();
				tcl.add(inclOnt);
				tcl.addAll(getTransitiveClosureImports(inclOnt));
			}
		} catch (OWLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return tcl;
	}
	
	public static Set getImportedSet(OWLOntology ont, int type) {
		
		Set entitySet = new HashSet();
		try {
			if(ont.getIncludedOntologies() != null) {
	        
				Set inclOntSet = getTransitiveClosureImports(ont);
	            Iterator iter = inclOntSet.iterator();
	            while(iter.hasNext()) {
	            	OWLOntology inclOnt = (OWLOntology) iter.next();
	            	switch (type) {
	            		case CLASSES:
	            			Iterator claIter = inclOnt.getClasses().iterator();
	        				while (claIter.hasNext()) {
	        					OWLDescription desc = (OWLDescription) claIter.next();
	        					if (desc instanceof OWLClass) entitySet.add(desc);
	        				}	            			
	            			break;
	            			
	            		case DATAPROPERTIES:
	            			entitySet.addAll(inclOnt.getDataProperties());
	            			break;
	            			
	            		case OBJPROPERTIES:
	            			entitySet.addAll(inclOnt.getObjectProperties());
	            			break;
	            			
	            		case INDIVIDUALS:
	            			Iterator indIter = inclOnt.getIndividuals().iterator();
	        				while (indIter.hasNext()) {
	        					OWLIndividual ind = (OWLIndividual) indIter.next();
	        					if (ind.getURI()!=null) entitySet.add(ind);
	        				}
	            			break;	
	            	}
	            }
			}
		}
		catch (OWLException e) {
			e.printStackTrace();
		}
		return entitySet;
	}
}
