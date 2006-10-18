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

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.xerces.impl.dv.util.Base64;
import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.change.ChangeLog;
import org.mindswap.swoop.change.OntologyChangeRenderer;
import org.mindswap.swoop.utils.owlapi.CorrectedRDFRenderer;
import org.semanticweb.owl.io.RendererException;
import org.semanticweb.owl.io.vocabulary.XMLSchemaSimpleDatatypeVocabulary;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataValue;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLProperty;
import org.semanticweb.owl.model.change.AddDataPropertyInstance;
import org.semanticweb.owl.model.change.AddEntity;
import org.semanticweb.owl.model.change.AddImport;
import org.semanticweb.owl.model.change.AddIndividualClass;
import org.semanticweb.owl.model.change.AddObjectPropertyInstance;
import org.semanticweb.owl.model.change.ChangeVisitor;
import org.semanticweb.owl.model.change.OntologyChange;
import org.semanticweb.owl.model.helper.OWLBuilder;

/**
 * @author ronwalf
 *
 * The base Annotea description.
 */
public class Description implements Serializable {
	
	protected URL location = null;
	protected URI[] annotates = null;
	protected String body = null;
	protected String bodyType = null;
	protected String author = null;
	protected String created = null;
	protected OWLClass annotationType = null;
	protected List ontologyChangeSet = null;
	protected String annotatedEntityDefinition = null;
	final static String separator = "|";
	
	/**
	 * Generate a new Description, one with an unknown location.
	 */
	public Description() {		
	}
	
	public Description(URL location) {
		setLocation(location);
	}
	
	/**
	 * Build an OWL Ontology to wrap annotation instance
	 * It imports the default Annotea Ontology that defines class
	 * - Annotation and properties - body, author, created etc
	 * We then create an instance of Annotation and fill its properties with
	 * values from description
	 * @return
	 * @throws AnnoteaException
	 */
	public OWLOntology buildOntology() throws AnnoteaException {
		OWLOntology ontology = null;
		try {
			// create new ontology
			URI ont_uri = new URI("");
			OWLBuilder builder = new OWLBuilder();
			builder.createOntology(ont_uri, ont_uri);
			ontology = builder.getOntology();
			OntologyChange change = null;
			
			// import standard annotea ontology
			change = new AddImport(ontology, Annotea.annoteaOntology, null);
			change.accept((ChangeVisitor) ontology);
			
			// create a new OWL instance
			OWLDataFactory dataFact = ontology.getOWLDataFactory();
			OWLIndividual annotInstance = dataFact.getOWLIndividual(new URI("#Inst"));
			change = new AddEntity(ontology, annotInstance, null);
			change.accept((ChangeVisitor) ontology);
			
			// set instance type as annotea:Annotation
			OWLClass annotClass = (OWLClass) Annotea.annoteaMap.get("Annotation");
			change = new AddIndividualClass(ontology, annotInstance, annotClass, null);
			change.accept((ChangeVisitor) ontology);
			
			// add properties to annotation instance			
			// add annotates
			if (annotates!=null) {
				for (int i=0; i<annotates.length; i++) {					
					OWLIndividual annotatesInd = dataFact.getOWLIndividual(annotates[i]);
					this.addAnnotationProperty("annotates", 2, annotatesInd, null, ontology, annotInstance);
				}
			}
			// add author
			if (author!=null) {
				this.addAnnotationProperty("author", 1, author, "string", ontology, annotInstance);
			}
			// add created
			if (created!=null) {
				this.addAnnotationProperty("created", 1, created, "string", ontology, annotInstance);
			}
			// add annotation type
			if (annotationType!=null) {
				change = new AddIndividualClass(ontology, annotInstance, annotationType, null);
				change.accept((ChangeVisitor) ontology);
			}
			// add entity definition string
			if (annotatedEntityDefinition!=null) {
				this.addAnnotationProperty("entityDefinition", 1, annotatedEntityDefinition, "string", ontology, annotInstance);
			}
			
			// add ontology change set			
			if (ontologyChangeSet!=null) {
				
				if (SwoopModel.changeSharingMethod==SwoopModel.JAVA_SER) {
					// Serialize java object representing change set 
					// and encode it to base-64 string. finally, put it as value
					// of a dataproperty AnnoteaNS+"hasChangeSet"
					ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
					ObjectOutputStream out = new ObjectOutputStream(byteStream);				
					out.writeObject(ontologyChangeSet);
					out.close();
					String changeSetStr = Base64.encode(byteStream.toByteArray());
					// System.out.println("Change set size:"+changeSetStr.length());
					this.addAnnotationProperty("hasChangeSetJAVA", 1, changeSetStr, "string", ontology, annotInstance);
				}
				else {
					// SERIALIZE CHANGE LOGS IN RDF/XML USING OWLAPI ONTOLOGY
					OntologyChangeRenderer changeRenderer = new OntologyChangeRenderer(new ChangeLog(null, null));
					OWLOntology owlapiOntology = changeRenderer.serializeOntologyChanges(ontologyChangeSet);
					
					CorrectedRDFRenderer rdfRenderer = new CorrectedRDFRenderer();
					StringWriter st = new StringWriter();
					rdfRenderer.renderOntology(owlapiOntology, st);
					String rdfxml = st.toString();				
					// TESTING:
	//				System.out.println("OWL ChangeSet Ontology:---");
	//				System.out.println(rdfxml);
					rdfxml = this.stripHTML(rdfxml);
					this.addAnnotationProperty("hasChangeSetRDF", 1, rdfxml, "string", ontology, annotInstance);
				}
			}
			
			// now create an anonymous instance for the body
			//OWLIndividual bodyInstance = dataFact.getOWLIndividual(null);
			Random r = new Random(System.currentTimeMillis());
			OWLIndividual bodyInstance = dataFact.getAnonOWLIndividual(new URI("http://" + Long.toString(r.nextLong())));
			change = new AddEntity(ontology, bodyInstance, null);
			change.accept((ChangeVisitor) ontology);
			
			// add httpBody value = html text from body
			if (body!=null) {
				this.addAnnotationProperty("Body", 1, body, "string", ontology, bodyInstance);				
			}
			// add httpContentType
			if (bodyType!=null) {
				this.addAnnotationProperty("ContentType", 1, bodyType, "string", ontology, bodyInstance);
			}
			// add httpContentLength
			if (body!=null) {
				this.addAnnotationProperty("ContentLength", 1, String.valueOf(body.length()), "int", ontology, bodyInstance);				
			}
			
			// set annotInstance.body = httpBody
			this.addAnnotationProperty("body", 2, bodyInstance, null, ontology, annotInstance);
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new AnnoteaException(e);
		}
		return ontology;
	}
	
	/**
	 * Add a single annotation property instance  
	 * @param propName - name of the annotea property (body, author, created..)
	 * @param propType - type of annotea property (1-Data, 2-Object)
	 * @param propValue - value of the annotea property
	 * @param valueDatatype - xsd of value, if its a data-value
	 * @param ourOntology - our new ontology containing single annotation instance (to be posted to server)
	 * @param annotInstance - annotation instance in ourOntology
	 */
	protected void addAnnotationProperty(String propName, int propType, Object propValue, String valueDatatype, OWLOntology ourOntology, OWLIndividual instance) {
	
		// get annotea property 
		OWLProperty annoteaProp = null;
		OntologyChange change = null;
		
		try {
			switch (propType) {			
				case 1: // data property
					annoteaProp = (OWLDataProperty) Annotea.INSTANCE.annoteaMap.get(propName);
					// create data property value
					String xsd = XMLSchemaSimpleDatatypeVocabulary.XS;
					URI xsdType = new URI(xsd+valueDatatype);
					OWLDataValue propDataVal = ourOntology.getOWLDataFactory().getOWLConcreteData(xsdType, "EN", propValue);
					change = new AddDataPropertyInstance(ourOntology, instance, (OWLDataProperty) annoteaProp, propDataVal, null);					
					break;
					
				case 2: // object property
					annoteaProp = (OWLObjectProperty) Annotea.INSTANCE.annoteaMap.get(propName);
					change = new AddObjectPropertyInstance(ourOntology, instance, (OWLObjectProperty) annoteaProp, (OWLIndividual) propValue, null);
					break;					
			}
			change.accept((ChangeVisitor) ourOntology);
		}
		catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	public URI[] getAnnotates() {
		return annotates;
	}
	
	public String getBody() {
		return body;
	}
	
	public String getBodyType() {
		return bodyType;
	}
	
	public URL getLocation() {
		return location;
	}
	
	public void setAnnotates(URI[] annotated) {
		this.annotates = annotated;
	}
	
	public void setBody(String body) {
		this.body = body;
	}
	
	public void setBody(String body, String type) {
		setBody(body);
		setBodyType(type);
	}
	
	public void setBodyType(String type) {
		this.bodyType = type;
	}
	
	public void setLocation(URL location) {
		this.location = location;
	}
	
	public String getAuthor() {
		return this.author;
	}

	public String getCreated() {
		return this.created;
	}
	
	public void setAuthor(String author) {
		this.author = author;
	}

	public void setCreated(String created) {
		this.created = created;
	}
	
	public OWLClass getAnnotationType() {
		return this.annotationType;
	}
	
	public void setAnnotationType(OWLClass type) {
		this.annotationType = type;
	}
	
	public List getOntologyChangeSet() {
		return this.ontologyChangeSet;
	}
	
	public void setOntologyChangeSet(List changeSet) {
		this.ontologyChangeSet = new ArrayList(changeSet);
	}
	
	protected String stripHTML(String html) {
		html = html.replaceAll("&", "&amp;");
		html = html.replaceAll("<", "&lt;");
		html = html.replaceAll(">", "&gt;");
		return html;
	}
	
	public String getAnnotatedEntityDefinition() {
		return annotatedEntityDefinition;
	}
	
	public void setAnnotatedEntityDefinition(String annotatedEntityDefinition) {
		this.annotatedEntityDefinition = annotatedEntityDefinition;
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof Description) {
			Description desc = (Description) obj;
			boolean match = true;
			
			if (desc.annotatedEntityDefinition!=null && this.annotatedEntityDefinition!=null) {
				if (!desc.annotatedEntityDefinition.equals(this.annotatedEntityDefinition)) match = false;
			}
			if (desc.annotates!=null && this.annotates!=null) {
				if (!desc.annotates.equals(this.annotates)) match = false;
			}
			if (desc.annotationType!=null && this.annotationType!=null) {
				try {
					if (!desc.annotationType.equals(this.annotationType)) match = false;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (desc.author!=null && this.author!=null) {
				if (!desc.author.equals(this.author)) match = false;
			}
			if (desc.body!=null && this.body!=null) {
				if (!desc.body.equals(this.body)) match = false;
			}
			if (desc.bodyType!=null && this.bodyType!=null) {
				if (!desc.bodyType.equals(this.bodyType)) match = false;
			}
			if (desc.created!=null && this.created!=null) {
				if (!desc.created.equals(this.created)) match = false;
			}
			if (desc.location!=null && this.location!=null) {
				if (!desc.location.equals(this.location)) match = false;
			}
			if (desc.ontologyChangeSet!=null && this.ontologyChangeSet!=null) {
				if (!desc.ontologyChangeSet.equals(this.ontologyChangeSet)) match = false;
			}	
			
			return match;
		}
		return false;
	}
	
	/*
	 * Serialize the Description object into a String
	 * Dont write all its components, only those needed to represent
	 * SwoopChange objects i.e. author of change, date, comment on change,
	 * uris the change refers to and serialized version of the change object
	 */
	public String serializeIntoString(SwoopModel swoopModel) throws OWLException {
		String ser = "";
		
		ser = "AUTHOR={"+ this.author + "}"+separator;
		ser += "DATE={"+ this.created + "}"+separator;
		ser += "COMMENT={" + this.annotatedEntityDefinition + "}"+separator;
		ser += "BODY={" + this.body + "}"+separator;
		
		ser += "URIS = {";
		for (int i=0; i<this.annotates.length; i++) {
			ser += this.annotates[i].toString() + ",";
		}
		ser += "}"+separator;
		
		if (this.ontologyChangeSet.size()>0) {
			// serialize changes in RDF/XML and add them in the BODY
			ChangeLog clog = new ChangeLog(null, swoopModel);
			OntologyChangeRenderer oc = new OntologyChangeRenderer(clog);
			OWLOntology changeOnt = oc.serializeOntologyChanges(this.ontologyChangeSet);
			CorrectedRDFRenderer rdfRend = new CorrectedRDFRenderer();
			StringWriter st = new StringWriter();
			try {
				rdfRend.renderOntology(changeOnt, st);
			} catch (RendererException e) {
				e.printStackTrace();
			}
			ser += "CHANGE={" + st.toString() + "}"+separator;
		}
		return ser;
	}
	
	/*
	 * Deserialize a description string into Description object
	 */
	public void deserializeFromString(String ser, SwoopModel swoopModel) throws OWLException {
		String[] parts = splitString(ser);
		this.author = this.getBracketed(parts[0]);
		this.created = this.getBracketed(parts[1]);
		this.annotatedEntityDefinition = this.getBracketed(parts[2]);
		this.body = this.getBracketed(parts[3]);
		
		String uris = this.getBracketed(parts[4]);
		String[] uriparts = uris.split(",");
		this.annotates = new URI[uriparts.length];
		for (int i=0; i<uriparts.length; i++) {
			try {
				this.annotates[i] = new URI(uriparts[i]);
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
		
		String body = this.getBracketed(parts[5]);
		// deserialize body to get change set
		ChangeLog clog = new ChangeLog(null, swoopModel);
		OntologyChangeRenderer oc = new OntologyChangeRenderer(clog);
		
		OWLOntology changeOnt = swoopModel.loadOntologyInRDF(new StringReader(body), URI.create(""));
		this.ontologyChangeSet = oc.deserializeOntologyChanges(changeOnt);		
	}
	
	private String getBracketed(String part) {
		return part.substring(part.indexOf("{")+1, part.lastIndexOf("}"));
	}
	
	private String[] splitString(String str) {
		
		List parts = new ArrayList();
		int pointer = 0;
		while (str.indexOf(separator, pointer)>=0) {
			String part = str.substring(pointer, str.indexOf(separator, pointer));
			pointer = str.indexOf(separator, pointer)+1;
			parts.add(part);
		}
		Object[] obj = parts.toArray();
		String[] spl = new String[obj.length];
		for (int i=0; i<obj.length; i++) {
			spl[i] = obj[i].toString();
		}
		return spl;
	}
}
