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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.xerces.impl.dv.util.Base64;
import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.change.ChangeLog;
import org.mindswap.swoop.change.OntologyChangeRenderer;
import org.mindswap.swoop.utils.owlapi.CorrectedRDFRenderer;
import org.semanticweb.owl.impl.model.OWLConnectionImpl;
import org.semanticweb.owl.io.Parser;
import org.semanticweb.owl.io.Renderer;
import org.semanticweb.owl.io.owl_rdf.OWLRDFErrorHandler;
import org.semanticweb.owl.io.owl_rdf.OWLRDFParser;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;
import org.xml.sax.SAXException;

/**
 * @author ronwalf
 *
 * This is the main interface to an Annotea server.
 */
public class AnnoteaClient {
	protected Parser parser;
	protected URL serverURL;
	protected String username;
	protected String password;
	
	protected SwoopModel swoopModel;
	
	public AnnoteaClient(URL serverURL, SwoopModel swoopModel) {
		init(serverURL, null, null, swoopModel);
	}
	
	public AnnoteaClient(URL serverURL, String username, String password, SwoopModel swoopModel) {
		init(serverURL, username, password, swoopModel);
	}
	
	protected void init(URL serverURL, String username, String password, SwoopModel swoopModel) {
		this.serverURL = serverURL;
		this.username = username;
		this.password = password;
		this.swoopModel = swoopModel;
	}
	
	public void delete(URL location) throws AnnoteaException {
		try {
			HttpURLConnection connection = (HttpURLConnection) location.openConnection();
			connection.setRequestMethod("DELETE");
			connection.connect();
			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				throw new AnnoteaException("Could not delete annotation. Response was: "
						+connection.getResponseCode() + " " + connection.getResponseMessage());
			}
		} catch (Exception e) {
			throw new AnnoteaException(e);
		}
	}
	
	protected OWLOntology findXOnt(URI about, String variableName) throws AnnoteaException{
			
		try {
			String query = "?"+variableName+"="
					+ URLEncoder.encode(about.toString(), "US-ASCII");
			URL queryURL = new URL(serverURL.toString() + query);
			HttpURLConnection connection = (HttpURLConnection) queryURL
					.openConnection();
			connection.addRequestProperty("Accept", "application/rdf+xml");
			connection.connect();

			Reader reader = new BufferedReader(new InputStreamReader(connection
					.getInputStream()));
			
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

			parser.setConnection(new OWLConnectionImpl());	
			OWLOntology ontology = parser.parseOntology(reader, new URI(
					queryURL.toString()));
			return ontology;
		} catch (Exception e) {
			e.printStackTrace();
			throw new AnnoteaException(e);
		} 
			
	}
	
	/**
	 * Parses an OWL Individual to get a Description object
	 * @param annotOnt - OWL ontology that contains individual
	 * @param ind - individual that wraps description
	 * @return
	 */
	public Description parseIndividual(OWLOntology annotOnt, OWLIndividual ind) {
		
		Description desc = new Description();
		try {
			
			// get URI of individual and set it to desc - location
			if (ind.getURI()!=null) {
				desc.setLocation(ind.getURI().toURL());				
			}
			// get annotation type of individual
			Set types = ind.getTypes(annotOnt);
			if (types.size()>1) {
				// has a subclass of "Annotation" type as well (eg. Comment,Explanation..)
				// hence put type = subclass instead of "Annotation" 
				Iterator iter = types.iterator();
				while (iter.hasNext()) {
					OWLClass type = (OWLClass) iter.next();
					if (!type.getURI().equals(((OWLClass) Annotea.INSTANCE.annoteaMap.get("Annotation")).getURI())) desc.setAnnotationType(type);
				}
			}
			else desc.setAnnotationType((OWLClass) types.iterator().next());
			
			// get property values
			Map dataValues = ind.getDataPropertyValues(annotOnt);
			if(dataValues.size() > 0) {				
				for (Iterator it = dataValues.keySet().iterator(); it.hasNext();) {
					OWLDataProperty prop = (OWLDataProperty) it.next();
					Set vals = (Set) dataValues.get(prop);					
					
					// check for author property value
					if (prop.getURI().equals(((OWLDataProperty) Annotea.INSTANCE.annoteaMap.get("author")).getURI())) {
						String author = vals.iterator().next().toString();
						author = author.substring(0, author.indexOf("^"));
						desc.setAuthor(author);
					}
					
					// check for created property value
					else if (prop.getURI().equals(((OWLDataProperty) Annotea.INSTANCE.annoteaMap.get("created")).getURI())) {
						String created = vals.iterator().next().toString();
						created = created.substring(0, created.indexOf("^"));
						desc.setCreated(created);
					}
					
					// check for entity definition string value
					else if (prop.getURI().equals(((OWLDataProperty) Annotea.INSTANCE.annoteaMap.get("entityDefinition")).getURI())) {
						String defn = vals.iterator().next().toString();
						defn = defn.substring(0, defn.indexOf("^"));
						defn = this.recoverHTML(defn);
						desc.setAnnotatedEntityDefinition(defn);
					}
					
					// *** BACKWARD COMPATIBILITY: check for Body in node itself
					// This was a problem in the earlier Annotations posted
					// that http:Body was part of the Annotation Instance itself
					// hence accounting for it here to recover old annotations
					else if (prop.getURI().equals(((OWLDataProperty) Annotea.INSTANCE.annoteaMap.get("Body")).getURI())) {
						// get httpBody location
						String body = vals.iterator().next().toString();
						body = body.substring(body.indexOf("<html>")+6, body.indexOf("</html>"));
						desc.setBody(body);
					}
					
					// check for hasChangeSet value - Java serialized
					else if (prop.getURI().equals(((OWLDataProperty) Annotea.INSTANCE.annoteaMap.get("hasChangeSetJAVA")).getURI())) {
						
						if (SwoopModel.changeSharingMethod==SwoopModel.JAVA_SER) {
							// get Ontology Change Set as a Java Serialized Object
							// get value of user-defined dataproperty 'hasChangeSet'
							String changeStr = vals.iterator().next().toString();
							changeStr = changeStr.substring(0, changeStr.indexOf("^"));
							// decode it from its base64 representation
							byte[] changeSetbytes = Base64.decode(changeStr);
							// convert bytes into java object
							ByteArrayInputStream bs = new ByteArrayInputStream(changeSetbytes);
							ObjectInputStream in = new ObjectInputStream(bs);
							List ontologyChangeSet = (ArrayList) in.readObject();
							desc.setOntologyChangeSet(ontologyChangeSet);
						}
					}
					
					// check for hasChangeSet value - RDF/XML serialized
					else if (prop.getURI().equals(((OWLDataProperty) Annotea.INSTANCE.annoteaMap.get("hasChangeSetRDF")).getURI())) {
						
						if (SwoopModel.changeSharingMethod==SwoopModel.RDFXML_SER) {
							// PARSE SERIALIZED RDF/XML OF CHANGES INTO OWL-API CHANGE OBJECTS
							String changeStr = vals.iterator().next().toString();
							changeStr = changeStr.substring(0, changeStr.indexOf("^"));
							changeStr = this.recoverHTML(changeStr);
							File tmpFile = new File("temp");
							FileWriter writer = new FileWriter(tmpFile);
							writer.write(changeStr);
							writer.close();
							// reopen temp file
							String filePath = tmpFile.toURI().toString();
							OWLOntology changeSetOnt = this.loadChangeSetOntology(filePath);						
							OntologyChangeRenderer changeRenderer = new OntologyChangeRenderer(new ChangeLog(null, swoopModel));
							try {
								desc.setOntologyChangeSet(changeRenderer.deserializeOntologyChanges(changeSetOnt));
							}
							catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
			
			// get object property values on annotation instance
			Map objectValues = ind.getObjectPropertyValues(annotOnt);
			if (objectValues.size()>0) {
				for (Iterator it = objectValues.keySet().iterator(); it.hasNext();) {
					OWLObjectProperty prop = (OWLObjectProperty) it.next();
					Set vals = (Set) objectValues.get(prop);
		
					// get values of "annotates"
					if (prop.getURI().equals(((OWLObjectProperty) Annotea.INSTANCE.annoteaMap.get("annotates")).getURI())) {
						// one is actual OWLNamedObject and second is its context (ontology)
						URI[] annotates = new URI[vals.size()];
						int ctr = 0;
						for (Iterator valIter = vals.iterator(); valIter.hasNext(); ) {
							OWLIndividual indVal = (OWLIndividual) valIter.next();
							URI indURI = indVal.getURI();
							annotates[ctr++] = indURI;
						}
						//***! rearrange so that actual OWLNamedObject is first and ontology is second 
						// now done in AnnoteaRenderer!
						desc.setAnnotates(annotates);
					}
					
					// get value of body - anonymous individual
					// For BACKWARD COMPATIBILITY: added desc.getBody()==null
					else if (desc.getBody()==null && prop.getURI().equals(((OWLObjectProperty) Annotea.INSTANCE.annoteaMap.get("body")).getURI())) {
						OWLIndividual bodyVal = (OWLIndividual) vals.iterator().next();
						
						if (bodyVal.getURI()!=null) {
							URL bodyURL = new URL(bodyVal.getURI().toString());
							BufferedReader in = new BufferedReader(new InputStreamReader(bodyURL.openStream()));
							String bodyStr = "";
							String inputLine;
							while ((inputLine = in.readLine()) != null) {
								bodyStr += inputLine;
							}
							desc.setBody(bodyStr);
						}						
					}
				}
			}
			
		} 
		catch (Exception e) {
			System.out.println("Error reading annotation ("+e.getMessage()+")");
			e.printStackTrace();
		}
		return desc;
	}
	
	public OWLOntology loadChangeSetOntology(String url) throws Exception {
		
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
			}
		});
		// load and parse ChangeSet Ontology from local "temp"
		URI location = new URI(url);
		parser.setConnection(new OWLConnectionImpl());
		return (parser.parseOntology(location));

	}
	
	/**
	 * Returns a set of description objects defined as instances in the 
	 * ontology returned by findAnnotationsOnt(URI about)
	 * @param about
	 * @return
	 * @throws AnnoteaException
	 */
	public Set findAnnotations(URI about) throws AnnoteaException {
		
		OWLOntology annotOnt = findAnnotationsOnt(about);
		Set descriptionSet = new HashSet();
		try {
			Iterator iter = annotOnt.getIndividuals().iterator();
			while (iter.hasNext()) {
				OWLIndividual descInd = (OWLIndividual) iter.next();
				if (descInd.getTypes(annotOnt).size()>0) descriptionSet.add(parseIndividual(annotOnt, descInd));
			}
		}
		catch (OWLException e) {
			e.printStackTrace();
		}
		return descriptionSet;
	}
	
	public OWLOntology findAnnotationsOnt(URI about) throws AnnoteaException {
		return findXOnt(about, "w3c_annotates");
	}
	
	public Description findReplies(URI root) throws AnnoteaException{
		return null;
	}
	
	public OWLOntology findRepliesOnt(URI root) throws AnnoteaException {
		return findXOnt(root, "w3c_reply_tree");
	}
	
	public Description get(URL location) {
		return null;
	}
	
	public OWLOntology getOnt(URL location) throws AnnoteaException {
		try {
			URI location_uri = new URI(location.toString());
			Parser parser = new OWLRDFParser();
			OWLOntology ontology = parser.parseOntology(location_uri);
			return ontology;
		} catch (Exception e) {
			throw new AnnoteaException(e);
		}
	}
	
	public URL post(Description description) throws AnnoteaException {
		OWLOntology ontology = description.buildOntology();
		return (post(ontology));
	}
	
	/**
	 * Post an ontology to the annotea server by rendering it first
	 * in RDF/XML using CorrectedRDFRenderer
	 * @param ontology
	 * @return
	 * @throws AnnoteaException
	 */
	public URL post(OWLOntology ontology ) throws AnnoteaException {
		if (ontology == null) { 
			throw new NullPointerException();
		}
		
		try {
			
			HttpURLConnection connection = (HttpURLConnection) serverURL.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/rdf+xml");
			connection.setDoOutput(true);
			Writer writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), Charset.forName("UTF-8")));
			Renderer renderer = new CorrectedRDFRenderer();
			renderer.renderOntology(ontology, writer);
			writer.close();
			//connection.connect();
			
			if (connection.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
				throw new AnnoteaException("Ontology not created! Reason: "
						+connection.getResponseCode() + " "+connection.getResponseMessage());
			}
			
			return new URL(connection.getHeaderField("Location"));
		} catch (Exception e) {
			e.printStackTrace();
			throw new AnnoteaException(e);			
		} 
	}
	
	public void put(URL location, Description description) throws AnnoteaException {
		put(location, description.buildOntology());
	}
	
	public void put(URL location, OWLOntology ontology) throws AnnoteaException {
		try {
			HttpURLConnection connection = (HttpURLConnection) location.openConnection();
			connection.setRequestMethod("PUT");
			connection.setRequestProperty("Content-Type", "application/rdf+xml");
			connection.setDoOutput(true);
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), Charset.forName("UTF-8")));
			Renderer renderer = new CorrectedRDFRenderer();
			renderer.renderOntology(ontology, writer);
					
			connection.connect();
			
			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				throw new AnnoteaException("Ontology not created! Reason: "
						+connection.getResponseCode() + " "+connection.getResponseMessage());
			}
			
		} catch (Exception e) {
			throw new AnnoteaException(e);			
		} 
	}
	
	protected String recoverHTML(String html) {
		html = html.replaceAll("&gt;", ">");
		html = html.replaceAll("&lt;", "<");
		html = html.replaceAll("&amp;", "&");
		return html;
	}
	
}
