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

package org.mindswap.swoop.change;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.utils.change.BooleanElementChange;
import org.mindswap.swoop.utils.change.EnumElementChange;
import org.semanticweb.owl.impl.model.OWLConnectionImpl;
import org.semanticweb.owl.io.owl_rdf.OWLRDFErrorHandler;
import org.semanticweb.owl.io.owl_rdf.OWLRDFParser;
import org.semanticweb.owl.io.vocabulary.XMLSchemaSimpleDatatypeVocabulary;
import org.semanticweb.owl.model.OWLAnd;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataType;
import org.semanticweb.owl.model.OWLDataValue;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owl.model.OWLDisjointClassesAxiom;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLNamedObject;
import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOr;
import org.semanticweb.owl.model.OWLProperty;
import org.semanticweb.owl.model.OWLSameIndividualsAxiom;
import org.semanticweb.owl.model.OWLSubClassAxiom;
import org.semanticweb.owl.model.change.AddClassAxiom;
import org.semanticweb.owl.model.change.AddDataPropertyInstance;
import org.semanticweb.owl.model.change.AddDataPropertyRange;
import org.semanticweb.owl.model.change.AddDomain;
import org.semanticweb.owl.model.change.AddEntity;
import org.semanticweb.owl.model.change.AddEquivalentClass;
import org.semanticweb.owl.model.change.AddImport;
import org.semanticweb.owl.model.change.AddIndividualAxiom;
import org.semanticweb.owl.model.change.AddIndividualClass;
import org.semanticweb.owl.model.change.AddObjectPropertyInstance;
import org.semanticweb.owl.model.change.AddObjectPropertyRange;
import org.semanticweb.owl.model.change.AddSuperClass;
import org.semanticweb.owl.model.change.AddSuperProperty;
import org.semanticweb.owl.model.change.ChangeVisitor;
import org.semanticweb.owl.model.change.OntologyChange;
import org.semanticweb.owl.model.change.RemoveClassAxiom;
import org.semanticweb.owl.model.change.RemoveDataPropertyInstance;
import org.semanticweb.owl.model.change.RemoveDataPropertyRange;
import org.semanticweb.owl.model.change.RemoveDomain;
import org.semanticweb.owl.model.change.RemoveEntity;
import org.semanticweb.owl.model.change.RemoveEquivalentClass;
import org.semanticweb.owl.model.change.RemoveImport;
import org.semanticweb.owl.model.change.RemoveIndividualAxiom;
import org.semanticweb.owl.model.change.RemoveIndividualClass;
import org.semanticweb.owl.model.change.RemoveObjectPropertyInstance;
import org.semanticweb.owl.model.change.RemoveObjectPropertyRange;
import org.semanticweb.owl.model.change.RemoveSuperClass;
import org.semanticweb.owl.model.change.RemoveSuperProperty;
import org.semanticweb.owl.model.change.SetFunctional;
import org.semanticweb.owl.model.change.SetInverseFunctional;
import org.semanticweb.owl.model.change.SetSymmetric;
import org.semanticweb.owl.model.change.SetTransitive;
import org.semanticweb.owl.model.helper.OWLBuilder;
import org.semanticweb.owl.model.helper.OntologyHelper;
import org.xml.sax.SAXException;

/**
 * @author Aditya Kalyanpur
 * This class is used to serialize and deserialize OntologyChange objects 
 * into RDF/XML. It uses a special-purpose OWL-API ontology created for 
 * representing changes in RDF. 
 * 
 * The architecture has to be improved since it requires an instanceof 
 * ChangeLog to be passed to it during initialization and then cyclically calls itself 
 * during serialization. i.e. it calls ChangeLog.getComponentsString() which in turn calls 
 * addToOWLAPIOntology of this Class. 
 *
 */
public class OntologyChangeRenderer {
	
	ChangeLog changeLog;
	OWLOntology owlapiOntology, owlapiOnly;
	static String owlapiLocation = "http://www.mindswap.org/dav/ontologies/owlapi.owl";
	static String xsdString = XMLSchemaSimpleDatatypeVocabulary.XS + "string";
	int argLimit = 10; // limit on no of arguments (parameters) of an Ontology Change
	
	public OntologyChangeRenderer(ChangeLog changeLog) throws OWLException {
		this.changeLog = changeLog;
		try {
			this.reloadOWLAPIOntology();
		}
		catch (Exception ex) {
			System.out.println("Error loading OWL-API ontology from "+owlapiLocation);
			throw new OWLException(ex);
		}
	}
	
	public OWLOntology serializeOntologyChanges(List changeSet) {
		
		for (Iterator iter = changeSet.iterator(); iter.hasNext(); ) {
			OntologyChange change = (OntologyChange) iter.next();
			changeLog.getChangeInformation(change, ChangeLog.CHANGE_RDFXML, this, new ArrayList(), null);					
		}
		
		/*** finally, remove owl-api classes, properties from ontology ***/
		this.removeRedundantOWLAPI();
		
		return this.owlapiOntology;
	}
	
	/**
	 * Accepts an OWL Ontology (annotOnt) containing ontology change instances
	 * serialized in RDF/XML using the owlapiOntology and parses it to return a
	 * list of OntologyChange objects
	 * @param annotOnt - ontology containing change instances
	 * @return list of OntologyChanges
	 * @throws Exception
	 */
	public List deserializeOntologyChanges(OWLOntology annotOnt) {
		
		SwoopModel swoopModel = changeLog.swoopModel;
		
		List changes = new ArrayList();
		
		Set indSet = new HashSet();
		try {
			indSet = annotOnt.getIndividuals();
		}
		catch (OWLException ex) {
			ex.printStackTrace();
		}
		
		// iterate through change instances in the ontology
		for (Iterator iter = indSet.iterator(); iter.hasNext(); ) {
			OWLIndividual ind = (OWLIndividual) iter.next();
			
			// get all values of property argument(i)
			Map dataValues = null;
			try {
				dataValues = ind.getDataPropertyValues(annotOnt);
			} catch (OWLException e) {
				e.printStackTrace();
				return changes;
			}
			
			String[] origArguments = new String[argLimit];
			String[] arguments = new String[argLimit];
			boolean validIndividual = false;
			for (Iterator it = dataValues.keySet().iterator(); it.hasNext();) {
				OWLDataProperty prop = (OWLDataProperty) it.next();
				Set vals = (Set) dataValues.get(prop);
				for (int i=0; i<argLimit; i++) {
					try {
						if (prop.getURI().toString().equals(owlapiLocation+"#argument"+String.valueOf(i+1))) {
							origArguments[i] = vals.iterator().next().toString();
							arguments[i] = origArguments[i];
							if (arguments[i].indexOf("^^")>0) arguments[i] = arguments[i].substring(0, arguments[i].indexOf("^"));
							if (arguments[i].endsWith("@EN")) arguments[i] = arguments[i].substring(0, arguments[i].length()-3);
							if (i>=1) validIndividual = true;
						}
					} catch (OWLException e1) {
						e1.printStackTrace();
					}
				}
			}
			if (!validIndividual) continue;
			
			// iterate through types and check which change class type belongs to
			// normally it better belong to only one Type!!
			Set types = new HashSet();
			try {
				types = ind.getTypes(annotOnt);
			} catch (OWLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			for (Iterator iter2 = types.iterator(); iter2.hasNext(); ) {
				Object type = iter2.next();
				
				// set ontology for the current change type
				// the first argument for the change instance should be the ontology URI
				OWLOntology changeOnt = null;
				OWLDataFactory df = null;
				try {
					URI ontURI = new URI(arguments[0]);
					changeOnt = swoopModel.getOntology(ontURI);
					//*** if ontology is not present in SwoopModel, create a new ontology
					// but set the URI so that it can be used to reference the ontology later ***/
					if (changeOnt==null) {
						 changeOnt = (OWLOntology) this.createNewOWLObject(OWLOntology.class, ontURI, null);
					}
					df = changeOnt.getOWLDataFactory();
				} 
				catch (Exception e2) {
					e2.printStackTrace();
				}
				
				//*** 'type' better be OWLClass since all changes
				// have been represented by OWLClass objects ***/
				if (type instanceof OWLClass) {
					OWLClass changeClass = (OWLClass) type;
					String uri = "";
					try {
						uri = changeClass.getURI().toString();
					} catch (OWLException e3) {
						// TODO Auto-generated catch block
						e3.printStackTrace();
					}
					
					// start processing changes
					if (uri.equals(owlapiLocation+"#AddImport")) {
						try {
							OWLOntology impOnt = swoopModel.getOntology(new URI(arguments[1]));
							if (impOnt==null) {
								// create new ontology with impOnt URI
								impOnt = (OWLOntology) this.createNewOWLObject(OWLOntology.class, new URI(arguments[1]), null);
							}
							AddImport change = new AddImport(changeOnt, impOnt, null);
							changes.add(change);
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					else if (uri.equals(owlapiLocation+"#RemoveImport")) {
						try {
							OWLOntology impOnt = swoopModel.getOntology(new URI(arguments[1]));
							if (impOnt==null) {
								// create new ontology with impOnt URI
								impOnt = (OWLOntology) this.createNewOWLObject(OWLOntology.class, new URI(arguments[1]), null);
							}
							RemoveImport change = new RemoveImport(changeOnt, impOnt, null);
							changes.add(change);
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					else if (uri.equals(owlapiLocation+"#AddCLASS")) {
						try {
							OWLClass newClass = df.getOWLClass(new URI(arguments[1]));
							AddEntity change = new AddEntity(changeOnt, newClass, null);
							changes.add(change);
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					else if (uri.equals(owlapiLocation+"#AddDATATYPE_PROPERTY")) {
						try {
							OWLDataProperty newProp = df.getOWLDataProperty(new URI(arguments[1]));
							AddEntity change = new AddEntity(changeOnt, newProp, null);
							changes.add(change);
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					else if (uri.equals(owlapiLocation+"#AddOBJECT_PROPERTY")) {
						try {
							OWLObjectProperty newProp = df.getOWLObjectProperty(new URI(arguments[1]));
							AddEntity change = new AddEntity(changeOnt, newProp, null);
							changes.add(change);
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					else if (uri.equals(owlapiLocation+"#AddINDIVIDUAL")) {
						try {
							OWLIndividual newProp = df.getOWLIndividual(new URI(arguments[1]));
							AddEntity change = new AddEntity(changeOnt, newProp, null);
							changes.add(change);
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					else if (uri.equals(owlapiLocation+"#RemoveCLASS")) {
						try {
							OWLClass remClass = (OWLClass) swoopModel.getEntity(changeOnt, new URI(arguments[1]), true, swoopModel.CLASSES);							
							RemoveEntity change = new RemoveEntity(changeOnt, remClass, null);
							changes.add(change);
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					else if (uri.equals(owlapiLocation+"#RemoveDATATYPE_PROPERTY")) {
						try {
							OWLDataProperty remProp = (OWLDataProperty) swoopModel.getEntity(changeOnt, new URI(arguments[1]), true, swoopModel.DATAPROPERTIES);							
							RemoveEntity change = new RemoveEntity(changeOnt, remProp, null);
							changes.add(change);
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					else if (uri.equals(owlapiLocation+"#RemoveOBJECT_PROPERTY")) {
						try {
							OWLObjectProperty remProp = (OWLObjectProperty) swoopModel.getEntity(changeOnt, new URI(arguments[1]), true, swoopModel.OBJPROPERTIES);							
							RemoveEntity change = new RemoveEntity(changeOnt, remProp, null);
							changes.add(change);
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					else if (uri.equals(owlapiLocation+"#RemoveINDIVIDUAL")) {
						try {
							OWLIndividual remInd = (OWLIndividual) swoopModel.getEntity(changeOnt, new URI(arguments[1]), true, swoopModel.INDIVIDUALS);
							RemoveEntity change = new RemoveEntity(changeOnt, remInd, null);
							changes.add(change);
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					else if (uri.equals(owlapiLocation+"#AddEquivalentClass")) {
						try {
							OWLClass cla = (OWLClass) swoopModel.getEntity(changeOnt, new URI(arguments[1]), true, swoopModel.CLASSES);
							OWLDescription desc = this.getOWLObjectNode(annotOnt, arguments[2], changeOnt);
							AddEquivalentClass change = new AddEquivalentClass(changeOnt, cla, desc, null);
							changes.add(change);
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					else if (uri.equals(owlapiLocation+"#RemoveEquivalentClass")) {
						try {
							OWLClass cla = (OWLClass) swoopModel.getEntity(changeOnt, new URI(arguments[1]), true, swoopModel.CLASSES);
							OWLDescription desc = this.getOWLObjectNode(annotOnt, arguments[2], changeOnt);
							RemoveEquivalentClass change = new RemoveEquivalentClass(changeOnt, cla, desc, null);
							changes.add(change);
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					else if (uri.equals(owlapiLocation+"#AddSuperClass")) {
						try { 
							OWLClass cla = (OWLClass) swoopModel.getEntity(changeOnt, new URI(arguments[1]), true, swoopModel.CLASSES);
							OWLDescription desc = this.getOWLObjectNode(annotOnt, arguments[2], changeOnt);
							AddSuperClass change = new AddSuperClass(changeOnt, cla, desc, null);
							changes.add(change);
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					else if (uri.equals(owlapiLocation+"#RemoveSuperClass")) {
						try {
							OWLClass cla = (OWLClass) swoopModel.getEntity(changeOnt, new URI(arguments[1]), true, swoopModel.CLASSES);
							OWLDescription desc = this.getOWLObjectNode(annotOnt, arguments[2], changeOnt);
							if (desc!=null) {
								RemoveSuperClass change = new RemoveSuperClass(changeOnt, cla, desc, null);						
								changes.add(change);						
								// also add RemoveClassAxiom change
								OWLSubClassAxiom subClaAxiom = df.getOWLSubClassAxiom(cla, desc);
								RemoveClassAxiom change2 = new RemoveClassAxiom(changeOnt, subClaAxiom, null);
								changes.add(change2);
							}
							else System.out.println("Unable to create change");
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					else if (uri.equals(owlapiLocation+"#AddSubClassAxiom")) {
						try { 
							OWLDescription sub = this.getOWLObjectNode(annotOnt, arguments[1], changeOnt);
							OWLDescription sup = this.getOWLObjectNode(annotOnt, arguments[2], changeOnt);
							OWLSubClassAxiom subAxiom = df.getOWLSubClassAxiom(sub, sup);
							AddClassAxiom change = new AddClassAxiom(changeOnt, subAxiom, null);
							changes.add(change);
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					else if (uri.equals(owlapiLocation+"#RemoveSubClassAxiom")) {
						try {
							OWLDescription sub = this.getOWLObjectNode(annotOnt, arguments[1], changeOnt);
							OWLDescription sup = this.getOWLObjectNode(annotOnt, arguments[2], changeOnt);
							if (sub!=null && sup!=null) {
//								RemoveSuperClass change = new RemoveSuperClass(changeOnt, cla, desc, null);						
//								changes.add(change);						
								// also add RemoveClassAxiom change
								OWLSubClassAxiom subClaAxiom = df.getOWLSubClassAxiom(sub, sup);
								RemoveClassAxiom change2 = new RemoveClassAxiom(changeOnt, subClaAxiom, null);
								changes.add(change2);
							}
							else System.out.println("Unable to create change");
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					else if (uri.equals(owlapiLocation+"#AddDisjointClasses")) {
						try {
							Set disjClasses = new HashSet();
							int i = 1;
							while (arguments[i]!=null) {
								disjClasses.add(this.getOWLObjectNode(annotOnt, arguments[i], changeOnt));
								i++;
							}
							OWLDisjointClassesAxiom disAxiom = df.getOWLDisjointClassesAxiom(disjClasses);
							AddClassAxiom change = new AddClassAxiom(changeOnt, disAxiom, null); 
							changes.add(change);
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					else if (uri.equals(owlapiLocation+"#RemoveDisjointClasses")) {
						try {
							Set disjClasses = new HashSet();
							int i = 1;
							while (arguments[i]!=null) {
								disjClasses.add(this.getOWLObjectNode(annotOnt, arguments[i], changeOnt));
								i++;
							}
							OWLDisjointClassesAxiom disAxiom = df.getOWLDisjointClassesAxiom(disjClasses);
							RemoveClassAxiom change = new RemoveClassAxiom(changeOnt, disAxiom, null); 
							changes.add(change);
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					else if (uri.equals(owlapiLocation+"#AddIntersectionElement")) {
						try {
							OWLClass cla = (OWLClass) swoopModel.getEntity(changeOnt, new URI(arguments[1]), true, swoopModel.CLASSES);
							OWLDescription desc = this.getOWLObjectNode(annotOnt, arguments[2], changeOnt);
							BooleanElementChange change = new BooleanElementChange(OWLAnd.class, "Add", changeOnt, cla, desc, null);
							changes.add(change);
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					else if (uri.equals(owlapiLocation+"#RemoveIntersectionElement")) {
						try {
							OWLClass cla = (OWLClass) swoopModel.getEntity(changeOnt, new URI(arguments[1]), true, swoopModel.CLASSES);
							OWLDescription desc = this.getOWLObjectNode(annotOnt, arguments[2], changeOnt);
							BooleanElementChange change = new BooleanElementChange(OWLAnd.class, "Remove", changeOnt, cla, desc, null);
							changes.add(change);
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					else if (uri.equals(owlapiLocation+"#AddUnionElement")) {
						try {
							OWLClass cla = (OWLClass) swoopModel.getEntity(changeOnt, new URI(arguments[1]), true, swoopModel.CLASSES);
							OWLDescription desc = this.getOWLObjectNode(annotOnt, arguments[2], changeOnt);
							BooleanElementChange change = new BooleanElementChange(OWLOr.class, "Add", changeOnt, cla, desc, null);
							changes.add(change);
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					else if (uri.equals(owlapiLocation+"#RemoveUnionElement")) {
						try {
							OWLClass cla = (OWLClass) swoopModel.getEntity(changeOnt, new URI(arguments[1]), true, swoopModel.CLASSES);
							OWLDescription desc = this.getOWLObjectNode(annotOnt, arguments[2], changeOnt);
							BooleanElementChange change = new BooleanElementChange(OWLOr.class, "Remove", changeOnt, cla, desc, null);
							changes.add(change);
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					else if (uri.equals(owlapiLocation+"#AddEnumerationElement")) {
						try {
							OWLClass cla = (OWLClass) swoopModel.getEntity(changeOnt, new URI(arguments[1]), true, swoopModel.CLASSES);
							OWLIndividual enumElem = (OWLIndividual) swoopModel.getEntity(changeOnt, new URI(arguments[2]), true, swoopModel.INDIVIDUALS);
							EnumElementChange change = new EnumElementChange("Add", changeOnt, cla, enumElem, null);
							changes.add(change);
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					else if (uri.equals(owlapiLocation+"#RemoveEnumerationElement")) {
						try {
							OWLClass cla = (OWLClass) swoopModel.getEntity(changeOnt, new URI(arguments[1]), true, swoopModel.CLASSES);
							OWLIndividual enumElem = (OWLIndividual) swoopModel.getEntity(changeOnt, new URI(arguments[2]), true, swoopModel.INDIVIDUALS);
							EnumElementChange change = new EnumElementChange("Remove", changeOnt, cla, enumElem, null);
							changes.add(change);
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					else if (uri.equals(owlapiLocation+"#SetFunctional")) {
						try {
							OWLProperty prop = (OWLProperty) swoopModel.getEntity(changeOnt, new URI(arguments[1]), true, swoopModel.OBJPROPERTIES);
							boolean setting = true;
							if (arguments[2].equals("false")) setting = false;
							SetFunctional change = new SetFunctional(changeOnt, prop, setting, null);
							changes.add(change);
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					else if (uri.equals(owlapiLocation+"#SetInverseFunctional")) {
						try {
							OWLObjectProperty prop = (OWLObjectProperty) swoopModel.getEntity(changeOnt, new URI(arguments[1]), true, swoopModel.OBJPROPERTIES);
							boolean setting = true;
							if (arguments[2].equals("false")) setting = false;
							SetInverseFunctional change = new SetInverseFunctional(changeOnt, prop, setting, null);
							changes.add(change);
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					else if (uri.equals(owlapiLocation+"#SetTransitive")) {
						try {
							OWLObjectProperty prop = (OWLObjectProperty) swoopModel.getEntity(changeOnt, new URI(arguments[1]), true, swoopModel.OBJPROPERTIES);
							boolean setting = true;
							if (arguments[2].equals("false")) setting = false;
							SetTransitive change = new SetTransitive(changeOnt, prop, setting, null);
							changes.add(change);
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					else if (uri.equals(owlapiLocation+"#SetSymmetric")) {
						try {
							OWLObjectProperty prop = (OWLObjectProperty) swoopModel.getEntity(changeOnt, new URI(arguments[1]), true, swoopModel.OBJPROPERTIES);
							boolean setting = true;
							if (arguments[2].equals("false")) setting = false;
							SetSymmetric change = new SetSymmetric(changeOnt, prop, setting, null);
							changes.add(change);
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					else if (uri.equals(owlapiLocation+"#AddSuperProperty")) {
						try {	
							OWLProperty prop = (OWLProperty) swoopModel.getEntity(changeOnt, new URI(arguments[1]), true, swoopModel.OBJPROPERTIES);
							OWLProperty supProp = (OWLProperty) swoopModel.getEntity(changeOnt, new URI(arguments[1]), true, swoopModel.OBJPROPERTIES);
							AddSuperProperty change = new AddSuperProperty(changeOnt, prop, supProp, null);
							changes.add(change);
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					else if (uri.equals(owlapiLocation+"#RemoveSuperProperty")) {
						try {
							OWLProperty prop = (OWLProperty) swoopModel.getEntity(changeOnt, new URI(arguments[1]), true, swoopModel.OBJPROPERTIES);
							OWLProperty supProp = (OWLProperty) swoopModel.getEntity(changeOnt, new URI(arguments[1]), true, swoopModel.OBJPROPERTIES);
							RemoveSuperProperty change = new RemoveSuperProperty(changeOnt, prop, supProp, null);
							changes.add(change);
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					else if (uri.equals(owlapiLocation+"#AddDomain")) {
						try {
							OWLProperty prop = (OWLProperty) swoopModel.getEntity(changeOnt, new URI(arguments[1]), true, swoopModel.OBJPROPERTIES); //TODO:could be dataProp??
							OWLDescription desc = this.getOWLObjectNode(annotOnt, arguments[2], changeOnt);						
							AddDomain change = new AddDomain(changeOnt, prop, desc, null);
							changes.add(change);
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					else if (uri.equals(owlapiLocation+"#RemoveDomain")) {
						try {
							OWLProperty prop = (OWLProperty) swoopModel.getEntity(changeOnt, new URI(arguments[1]), true, swoopModel.OBJPROPERTIES);
							OWLDescription desc = this.getOWLObjectNode(annotOnt, arguments[2], changeOnt);						
							RemoveDomain change = new RemoveDomain(changeOnt, prop, desc, null);
							changes.add(change);
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					else if (uri.equals(owlapiLocation+"#AddObjectPropertyRange")) {
						try {
							OWLObjectProperty prop = (OWLObjectProperty) swoopModel.getEntity(changeOnt, new URI(arguments[1]), true, swoopModel.OBJPROPERTIES);
							OWLDescription desc = this.getOWLObjectNode(annotOnt, arguments[2], changeOnt);						
							AddObjectPropertyRange change = new AddObjectPropertyRange(changeOnt, prop, desc, null);
							changes.add(change);
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					else if (uri.equals(owlapiLocation+"#RemoveObjectPropertyRange")) {
						try {
							OWLObjectProperty prop = (OWLObjectProperty) swoopModel.getEntity(changeOnt, new URI(arguments[1]), true, swoopModel.OBJPROPERTIES);
							OWLDescription desc = this.getOWLObjectNode(annotOnt, arguments[2], changeOnt);						
							RemoveObjectPropertyRange change = new RemoveObjectPropertyRange(changeOnt, prop, desc, null);
							changes.add(change);
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					else if (uri.equals(owlapiLocation+"#AddDataPropertyRange")) {
						try {
							OWLDataProperty prop = (OWLDataProperty) swoopModel.getEntity(changeOnt, new URI(arguments[1]), true, swoopModel.DATAPROPERTIES);
							OWLDataType dType = df.getOWLConcreteDataType(new URI(arguments[2]));
							AddDataPropertyRange change = new AddDataPropertyRange(changeOnt, prop, dType, null);
							changes.add(change);
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					else if (uri.equals(owlapiLocation+"#RemoveDataPropertyRange")) {
						try {
							OWLDataProperty prop = (OWLDataProperty) swoopModel.getEntity(changeOnt, new URI(arguments[1]), true, swoopModel.DATAPROPERTIES);
							OWLDataType dType = df.getOWLConcreteDataType(new URI(arguments[2]));
							RemoveDataPropertyRange change = new RemoveDataPropertyRange(changeOnt, prop, dType, null);
							changes.add(change);
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					else if (uri.equals(owlapiLocation+"#AddIndividualClass")) {
						try {
							OWLIndividual indi = (OWLIndividual) swoopModel.getEntity(changeOnt, new URI(arguments[1]), true, swoopModel.INDIVIDUALS);
							OWLDescription desc = this.getOWLObjectNode(annotOnt, arguments[2], changeOnt);
							AddIndividualClass change = new AddIndividualClass(changeOnt, indi, desc, null);
							changes.add(change);
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					else if (uri.equals(owlapiLocation+"#RemoveIndividualClass")) {
						try {
							OWLIndividual indi = (OWLIndividual) swoopModel.getEntity(changeOnt, new URI(arguments[1]), true, swoopModel.INDIVIDUALS);
							OWLDescription desc = this.getOWLObjectNode(annotOnt, arguments[2], changeOnt);
							RemoveIndividualClass change = new RemoveIndividualClass(changeOnt, indi, desc, null);
							changes.add(change);
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					else if (uri.equals(owlapiLocation+"#AddSameIndividuals")) {
						try {
							Set sameInds = new HashSet();
							int i = 1;
							while (arguments[i]!=null) {
								sameInds.add((OWLIndividual) swoopModel.getEntity(changeOnt, new URI(arguments[i]), true, swoopModel.INDIVIDUALS));
								i++;
							}
							OWLSameIndividualsAxiom sameAxiom = df.getOWLSameIndividualsAxiom(sameInds);
							AddIndividualAxiom change = new AddIndividualAxiom(changeOnt, sameAxiom, null); 
							changes.add(change);
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					else if (uri.equals(owlapiLocation+"#RemoveSameIndividuals")) {
						try {
							Set sameInds = new HashSet();
							int i = 1;
							while (arguments[i]!=null) {
								sameInds.add((OWLIndividual) swoopModel.getEntity(changeOnt, new URI(arguments[i]), true, swoopModel.INDIVIDUALS));
								i++;
							}
							OWLSameIndividualsAxiom sameAxiom = df.getOWLSameIndividualsAxiom(sameInds);
							RemoveIndividualAxiom change = new RemoveIndividualAxiom(changeOnt, sameAxiom, null); 
							changes.add(change);
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					else if (uri.equals(owlapiLocation+"#AddDifferentIndividuals")) {
						try {
							Set diffInds = new HashSet();
							int i = 1;
							while (arguments[i]!=null) {
								diffInds.add((OWLIndividual) swoopModel.getEntity(changeOnt, new URI(arguments[i]), true, swoopModel.INDIVIDUALS));
								i++;
							}
							OWLDifferentIndividualsAxiom diffAxiom = df.getOWLDifferentIndividualsAxiom(diffInds);
							AddIndividualAxiom change = new AddIndividualAxiom(changeOnt, diffAxiom, null); 
							changes.add(change);
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					else if (uri.equals(owlapiLocation+"#RemoveDifferentIndividuals")) {
						try {
							Set diffInds = new HashSet();
							int i = 1;
							while (arguments[i]!=null) {
								diffInds.add((OWLIndividual) swoopModel.getEntity(changeOnt, new URI(arguments[i]), true, swoopModel.INDIVIDUALS));
								i++;
							}
							OWLDifferentIndividualsAxiom diffAxiom = df.getOWLDifferentIndividualsAxiom(diffInds);
							RemoveIndividualAxiom change = new RemoveIndividualAxiom(changeOnt, diffAxiom, null); 
							changes.add(change);
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					else if (uri.equals(owlapiLocation+"#AddObjectPropertyInstance")) {
						try {
							OWLIndividual subj = (OWLIndividual) swoopModel.getEntity(changeOnt, new URI(arguments[1]), true, swoopModel.INDIVIDUALS);
							OWLObjectProperty prop = (OWLObjectProperty) swoopModel.getEntity(changeOnt, new URI(arguments[2]), true, swoopModel.OBJPROPERTIES);
							OWLIndividual obj = changeOnt.getIndividual(new URI(arguments[3]));
							AddObjectPropertyInstance change = new AddObjectPropertyInstance(changeOnt, subj, prop, obj, null);
							changes.add(change);
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
						 
					}
					else if (uri.equals(owlapiLocation+"#RemoveObjectPropertyInstance")) {
						try {
							OWLIndividual subj = (OWLIndividual) swoopModel.getEntity(changeOnt, new URI(arguments[1]), true, swoopModel.INDIVIDUALS);
							OWLObjectProperty prop = (OWLObjectProperty) swoopModel.getEntity(changeOnt, new URI(arguments[2]), true, swoopModel.OBJPROPERTIES);
							OWLIndividual obj = changeOnt.getIndividual(new URI(arguments[3]));
							RemoveObjectPropertyInstance change = new RemoveObjectPropertyInstance(changeOnt, subj, prop, obj, null);
							changes.add(change);
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
						 
					}
					else if (uri.equals(owlapiLocation+"#AddDataPropertyInstance")) {
						try {
							OWLIndividual subj = (OWLIndividual) swoopModel.getEntity(changeOnt, new URI(arguments[1]), true, swoopModel.INDIVIDUALS);
							OWLDataProperty prop = (OWLDataProperty) swoopModel.getEntity(changeOnt, new URI(arguments[2]), true, swoopModel.DATAPROPERTIES);
							String dValStr = origArguments[3];
							String dTypeURI = dValStr.substring(dValStr.lastIndexOf("[")+1, dValStr.length());
							if (dTypeURI.endsWith("@EN")) dTypeURI = dTypeURI.substring(0, dTypeURI.length()-3);
							if (dTypeURI.indexOf("^")>=0) dTypeURI = dTypeURI.substring(0, dTypeURI.indexOf("^"));
							arguments[3] = arguments[3].substring(0, arguments[3].indexOf("["));
							OWLDataValue dVal = df.getOWLConcreteData(new URI(dTypeURI), "EN", arguments[3]);
							AddDataPropertyInstance change = new AddDataPropertyInstance(changeOnt, subj, prop, dVal, null);
							changes.add(change);
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					else if (uri.equals(owlapiLocation+"#RemoveDataPropertyInstance")) {
						try {
							OWLIndividual subj = (OWLIndividual) swoopModel.getEntity(changeOnt, new URI(arguments[1]), true, swoopModel.INDIVIDUALS);
							OWLDataProperty prop = (OWLDataProperty) swoopModel.getEntity(changeOnt, new URI(arguments[2]), true, swoopModel.DATAPROPERTIES);
							String dValStr = origArguments[3];
							String dTypeURI = dValStr.substring(dValStr.lastIndexOf("[")+1, dValStr.length());
							if (dTypeURI.endsWith("@EN")) dTypeURI = dTypeURI.substring(0, dTypeURI.length()-3);
							if (dTypeURI.indexOf("^")>=0) dTypeURI = dTypeURI.substring(0, dTypeURI.indexOf("^"));
							arguments[3] = arguments[3].substring(0, arguments[3].indexOf("["));
							OWLDataValue dVal = df.getOWLConcreteData(new URI(dTypeURI), "EN", arguments[3]);
							RemoveDataPropertyInstance change = new RemoveDataPropertyInstance(changeOnt, subj, prop, dVal, null);
							changes.add(change);
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}
			}
		}
	
		return changes;		
	}

	/**
	 * Check if argument string which is a URI pointer in annotation ontology
	 * points to an existing entity in current ontology, or a bnode (OWL Description)
	 * which is captured as the type of an individual with URI = "#RDFbnodeHASHCODE" 
	 * @param annotOnt - annotation ontology
	 * @param arg - string argument pointing to a URI 
	 * @param currOnt - current selected ontology in Swoop
	 * @return
	 */
	protected OWLDescription getOWLObjectNode(OWLOntology annotOnt, String arg, OWLOntology currOnt) {
		
		OWLDescription owlObj = null;
		try {
			URI argURI = new URI(arg);
			
			if (arg.indexOf("#RDFbnode")>=0) {
				//*** argument points to an OWL Description
				// hence get individual with URI = bnode (from ont)
				// and gets it type as the required OWL Description ***/
				/*** Does not matter if currOntology exists or not since it gets
				 * bnode description from annotOnt!
				 */
				OWLIndividual ind = annotOnt.getIndividual(argURI);
				owlObj = (OWLDescription) ind.getTypes(annotOnt).iterator().next();				
			}
			else {
				// argument points to existing owl class in currOnt
				owlObj = (OWLClass) changeLog.swoopModel.getEntity(currOnt, argURI, true, changeLog.swoopModel.CLASSES);				
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return owlObj;
	}

	/**
	 * Add an instance to the OWL-API (change characterization) ontology
	 * @param changeType - string representing ontology change type
	 * @param changeArguments - set of arguments of the ontology change 
	 * @return current owlapiOntology
	 */
	public OWLOntology addtoOWLAPIOntology(String changeType, List changeArguments) {
		
		try {
			// initialize owlapi ontology
			OWLDataFactory df = owlapiOntology.getOWLDataFactory();
			OWLDataProperty[] arguments = new OWLDataProperty[argLimit];
			for (int i=0; i<argLimit; i++) {
				arguments[i] = owlapiOntology.getDataProperty(new URI(owlapiLocation+"#argument"+String.valueOf(i+1)));
			}
			
			OWLClass changeClass = owlapiOntology.getClass(new URI(owlapiLocation+"#"+changeType));
			
			if (changeClass==null) {
				System.out.println("Unable to find class "+changeType+" in OWL-API Ontology..");
				return owlapiOntology;
			}
			
			OWLIndividual changeInst = df.getOWLIndividual(new URI("#"+changeType+changeArguments.hashCode()));
			AddEntity ae = new AddEntity(owlapiOntology, changeInst, null);
			ae.accept((ChangeVisitor) owlapiOntology);
			
			AddIndividualClass aic = new AddIndividualClass(owlapiOntology, changeInst, changeClass, null);
			aic.accept((ChangeVisitor) owlapiOntology);						
			
			Iterator iter = changeArguments.iterator();
			int argCtr = 0;
			while (iter.hasNext()) {
				
				Object arg = iter.next();
				// if argument is an entity, it has a URI
				// so add URI directly as value of dataProperty - "argument"
				if (arg instanceof OWLNamedObject) {
					OWLNamedObject namedObj = (OWLNamedObject) arg;
					String uri = namedObj.getURI().toString();
					OWLDataValue dVal = df.getOWLConcreteData(new URI(xsdString), "EN", uri);
					AddDataPropertyInstance dop = new AddDataPropertyInstance(owlapiOntology, changeInst, arguments[argCtr], dVal, null);
					dop.accept((ChangeVisitor) owlapiOntology);
				}
				// else if argument is an OWL Description (Anonymous)
				// create new individual (URI-hashcode of desc) which is of
				// type description :)
				else if (arg instanceof OWLDescription) {
					OWLDescription desc = (OWLDescription) arg;
					String descHash = String.valueOf(desc.hashCode());
					OWLIndividual descInst = df.getOWLIndividual(new URI(owlapiLocation+"#"+"RDFbnode"+descHash));
					ae = new AddEntity(owlapiOntology, descInst, null);
					ae.accept((ChangeVisitor) owlapiOntology);
					aic = new AddIndividualClass(owlapiOntology, descInst, desc, null);
					aic.accept((ChangeVisitor) owlapiOntology);
					String uri = descInst.getURI().toString();
					OWLDataValue dVal = df.getOWLConcreteData(new URI(xsdString), "EN", uri);
					AddDataPropertyInstance dop = new AddDataPropertyInstance(owlapiOntology, changeInst, arguments[argCtr], dVal, null);
					dop.accept((ChangeVisitor) owlapiOntology);
				}
				// else if argument is datatype,
				// add URI directly as value of property
				else if (arg instanceof OWLDataType) {
					OWLDataType dt = (OWLDataType) arg;
					String uri = dt.getURI().toString();
					OWLDataValue dVal = df.getOWLConcreteData(new URI(xsdString), "EN", uri);
					AddDataPropertyInstance dop = new AddDataPropertyInstance(owlapiOntology, changeInst, arguments[argCtr], dVal, null);
					dop.accept((ChangeVisitor) owlapiOntology);
				}
				// dataValue is more direct!
				else if (arg instanceof OWLDataValue) {
					String dValStr = arg.toString();
					dValStr = dValStr.substring(0, dValStr.indexOf("^")) + "[" + dValStr.substring(dValStr.lastIndexOf("^")+1, dValStr.length());
					OWLDataValue dVal = df.getOWLConcreteData(new URI(xsdString), "EN", dValStr);
					AddDataPropertyInstance dop = new AddDataPropertyInstance(owlapiOntology, changeInst, arguments[argCtr], dVal, null);
					dop.accept((ChangeVisitor) owlapiOntology);
				}
				// otherwise treat it as a string and directly add it as the value (w/o parsing)
				else if (arg instanceof String) {
					OWLDataValue dVal = df.getOWLConcreteData(new URI(xsdString), "EN", arg.toString());
					AddDataPropertyInstance dop = new AddDataPropertyInstance(owlapiOntology, changeInst, arguments[argCtr], dVal, null);
					dop.accept((ChangeVisitor) owlapiOntology);
				}
				
				argCtr++;
			}
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return owlapiOntology;
	}
	
	private void reloadOWLAPIOntology() throws OWLException, FileNotFoundException, URISyntaxException {
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
		// load and parse OWL-API Ontology (from local copy)
		InputStreamReader in = new InputStreamReader(OntologyChangeRenderer.class.getResourceAsStream("owlapi.owl")); 
		parser.setConnection(new OWLConnectionImpl());
		owlapiOntology = parser.parseOntology(in, new URI(owlapiLocation));
		
		in = new InputStreamReader(OntologyChangeRenderer.class.getResourceAsStream("owlapi.owl"));
		parser.setConnection(new OWLConnectionImpl());
		owlapiOnly = parser.parseOntology(in, new URI(owlapiLocation));
	}
	
	public OWLOntology getOWLAPIOntology() {
		return this.owlapiOntology;
	}
	
	/**
	 * Add or Remove the elements from the OWLAPI-only ontology
	 * from the serialized version of the changeset ontology 
	 * @param add
	 */
	protected void removeRedundantOWLAPI() {
		try {
			// add/remove classes
			for (Iterator iter = owlapiOnly.getClasses().iterator(); iter.hasNext();) {
				OWLClass cla = (OWLClass) iter.next();
				cla = owlapiOntology.getClass(cla.getURI());
				Set usage = OntologyHelper.entityUsage(owlapiOntology, cla);
				if (usage.size()==0) {
					OntologyChange oc = new RemoveEntity(owlapiOntology, cla, null);
					oc.accept((ChangeVisitor) owlapiOntology);
				}				
			}
			// add/remove data props
			for (Iterator iter = owlapiOnly.getDataProperties().iterator(); iter.hasNext();) {
				OWLDataProperty prop = (OWLDataProperty) iter.next();
				OntologyChange oc = new RemoveEntity(owlapiOntology, owlapiOntology.getDataProperty(prop.getURI()), null);
				oc.accept((ChangeVisitor) owlapiOntology);
			}
			// add/remove obj props
			for (Iterator iter = owlapiOnly.getObjectProperties().iterator(); iter.hasNext();) {
				OWLObjectProperty prop = (OWLObjectProperty) iter.next();
				OntologyChange oc = new RemoveEntity(owlapiOntology, owlapiOntology.getObjectProperty(prop.getURI()), null);
				oc.accept((ChangeVisitor) owlapiOntology);
			}
			
			// finally add/remove imports of OWLAPI-only
			// DONT ADD THIS BECAUSE IT BREAKS PARSER
//			if (add) {
//				// remove imports				
//				RemoveImport ri = new RemoveImport(owlapiOntology, owlapiOnly, null);
//				ri.accept((ChangeVisitor) owlapiOntology);
//			}
//			else {
//				// add imports
//				AddImport ai = new AddImport(owlapiOntology, owlapiOnly, null);
//				ai.accept((ChangeVisitor) owlapiOntology);
//			}
		}
		catch (OWLException ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Create a New OWL Object (Ontology/Entity)
	 * @param cla - Java Class of OWL Object
	 * @param refURI - OWL Object URI
	 * @param refOnt - reference OWL Ontology used to create new entity 
	 * @return
	 */
	private OWLObject createNewOWLObject(Class cla, URI refURI, OWLOntology refOnt) {
 		OWLObject obj = null;
 		try {
			if (cla == OWLOntology.class) {
	 			// create new ontology
				OWLBuilder builder = new OWLBuilder();
				builder.createOntology(refURI, refURI);
				obj = builder.getOntology();
	 		}
			else if (cla == OWLClass.class) {
				// create new class
				obj = refOnt.getOWLDataFactory().getOWLClass(refURI);
			}
			else if (cla == OWLDataProperty.class) {
				// create new data property
				obj = refOnt.getOWLDataFactory().getOWLDataProperty(refURI);
			}
			else if (cla == OWLObjectProperty.class) {
				// create new object property
				obj = refOnt.getOWLDataFactory().getOWLObjectProperty(refURI);
			}
			else if (cla == OWLIndividual.class) {
				// create new individual
				obj = refOnt.getOWLDataFactory().getOWLIndividual(refURI);
			}
 		}
 		catch (OWLException ex) {
 			ex.printStackTrace();
 		}
		return obj;
	}
}