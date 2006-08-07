/*
 * Created on Jul 8, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.utils;

import java.net.URI;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.List;
import java.util.ArrayList;
import java.util.TreeSet;
import java.awt.Frame;

import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.utils.ui.EntityComparator;

import org.semanticweb.owl.impl.model.OWLConcreteDataTypeImpl;
import org.semanticweb.owl.impl.model.OWLDataEnumerationImpl;
import org.semanticweb.owl.io.vocabulary.OWLVocabularyAdapter;
import org.semanticweb.owl.io.vocabulary.RDFVocabularyAdapter;
import org.semanticweb.owl.io.vocabulary.XMLSchemaSimpleDatatypeVocabulary;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataRange;
import org.semanticweb.owl.model.OWLDataType;
import org.semanticweb.owl.model.OWLDataValue;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLEnumeration;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLNamedObject;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.change.AddDataPropertyInstance;
import org.semanticweb.owl.model.change.AddObjectPropertyInstance;
import org.semanticweb.owl.model.change.RemoveEntity;
import org.semanticweb.owl.model.helper.OntologyHelper;
import org.semanticweb.owl.model.OWLDataFactory;
/**
 * @author Zhao Bin
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class InstanceCreator {

    private Frame frame = null;
    
    private class N3Statement{
        String subject = null;
        String predicate = null;
        String object = null;
        
        private N3Statement(String subject, String predicate, String object){
            this.subject = subject;
            this.predicate = predicate;
            this.object = object;
        }
        
        public String getSubject() { return subject;}
        
        public String getPredicate() { return predicate;}
        
        public String getObject() { return object;}
        
        public String toString() { 
            String s = "subject [" + subject + "]\n " +
            			"predicate [" + predicate + "]\n " +
            			"object [" + object + "]";
            return s;
        }
        
    }
    
    private N3Statement parse(String n3){
        
        // simply break the subject and predicate based on whitespace
        StringTokenizer strTk = new StringTokenizer(n3);
        String subject = strTk.nextToken();
        String predicate = strTk.nextToken();
        String object = null;
        
        // take whatever the remaing part
        String s = strTk.nextToken("").trim();
        
        if (s.charAt(0) == '\''){
            object = new StringTokenizer(s, "'").nextToken();
        } else {
            object = new StringTokenizer(s).nextToken();
        }
        
        return new N3Statement(subject, predicate, object);
    }
    
    private void createEntity(SwoopModel swoopModel, OWLOntology ontology, 
            				  OWLEntity newEntity, OWLClass parentClass) {
		if (newEntity!=null){
			swoopModel.addEntity(ontology, newEntity, parentClass);
		}
	}
    
    /*
     * Create class instance (individual)
     */
    public void createClassInstance(SwoopModel swoopModel, String instanceURIStr,
            						String parentClassURIStr) throws InstanceCreationException{
        
        OWLOntology ontology = swoopModel.getSelectedOntology();
        OWLDataFactory df = null;
        
        try {
            df = ontology.getOWLDataFactory();
        } catch (Exception e){
            throw new InstanceCreationException(e.getMessage());
        }
        
        URI instanceURI = null;
        URI parentClassURI = null;
        
        try{
            instanceURI = new URI(instanceURIStr);
        } catch (Exception e){
            throw new InstanceCreationException ("Invalid instance URI " + e.getMessage());
        }
        
        try{
            parentClassURI = new URI(parentClassURIStr);
        } catch (Exception e){
            throw new InstanceCreationException ("Invalid parent class URI " + e.getMessage());
        }
        
        try{
            OWLEntity newEntity = df.getOWLIndividual(instanceURI);	
            OWLClass parentClass = ontology.getClass(parentClassURI);
        
            createEntity(swoopModel, ontology, newEntity, parentClass);
        } catch (Exception e){
            e.printStackTrace();
            throw new InstanceCreationException(e.getMessage());
        }
    }
    
    private boolean checkValidValue(OWLConcreteDataTypeImpl dt, String value) {
		boolean valid = false;
		String xsd = XMLSchemaSimpleDatatypeVocabulary.XS;
		String errorMsg = "Invalid Value for Specified Datatype - require ";
		
		/*if (dt.getURI().toString().equals(RDFVocabularyAdapter.RDF+"XMLLiteral"))
			this.isRDFXMLLiteral = true;
		*/
		
		return DataValueChecker.isValidValue(frame, dt, value);

	}
    
    public void createDataPropertyInstance(SwoopModel swoopModel, String subjectURIStr,
			   							   OWLDataProperty prop, String val) throws InstanceCreationException{
        
        Set rangeSet = null;
        OWLConcreteDataTypeImpl dt = null;
        OWLDataEnumerationImpl de = null;
        String dType = null;
        List changes = new ArrayList();
        
        try{
            rangeSet = prop.getRanges(swoopModel.getSelectedOntology());
        
			if (rangeSet.size()>0) {
			    Iterator iter = rangeSet.iterator();
				// range of DatatypeProperty is a 
				OWLDataRange desc = (OWLDataRange) iter.next();
				if (desc instanceof OWLConcreteDataTypeImpl) {
					dt = (OWLConcreteDataTypeImpl) desc;
				} else if (desc instanceof OWLDataEnumerationImpl) {
					de = (OWLDataEnumerationImpl) desc;
					//rangeList.add("OWL Data Range");
					Iterator deIter = de.getValues().iterator();
					while (deIter.hasNext()) {
						//String val = deIter.next().toString();
						 dType = val.substring(val.lastIndexOf("^")+1, val.length());
						// dType = "("+dType.substring(dType.indexOf("#")+1, dType.length())+")";//      ["+dType+"]";
						//val = val.substring(0, val.indexOf("^"));
						//valueList.add(val+" "+"("+dType+")");
					}
				}
			} else {
				// ** no range specified **
				// default - String? for Datatype Properties
				// default - OWLThing for Object Properties
				if (prop instanceof OWLDataProperty) {
					URI xsdString = null;
					try{
					    xsdString = new URI("http://www.w3.org/2001/XMLSchema#string");
					    dt = (OWLConcreteDataTypeImpl) swoopModel.getSelectedOntology().getOWLDataFactory().getOWLConcreteDataType(xsdString);
					} catch (Exception ex){
					     ex.printStackTrace();
					    throw new InstanceCreationException(ex.getMessage());
					}
				}
			}
			
			// create datevalue from user-typed text
			OWLDataValue dVal = null;
			
			if (dt != null) {
				URI datatypeURI = dt.getURI(); 
				boolean valid = checkValidValue(dt, val);
		
				if (valid) {
					val = val.replaceAll("&", "&amp;");
					val = val.replaceAll("<", "&lt;");
					val = val.replaceAll(">", "&gt;");				
					dVal = swoopModel.getSelectedOntology().getOWLDataFactory().getOWLConcreteData(datatypeURI, "EN", val);
				}
			} else {
				//val = valueBox.getSelectedValue().toString();
				//String dType = val.substring(val.lastIndexOf("(")+1, val.length()-1);
				//val = val.substring(0, val.lastIndexOf("(")).trim();
				URI datatypeURI = new URI(dType); 
				dVal = swoopModel.getSelectedOntology().getOWLDataFactory().getOWLConcreteData(datatypeURI, "EN", val);
			}
			
			// create data property change
			//OWLIndividual dispInst = (OWLIndividual) swoopModel.getSelectedEntity();
			
			URI subjectURI = null;
			try{
			    subjectURI = new URI(subjectURIStr);
	        } catch (Exception e){
	            throw new InstanceCreationException ("Invalid instance URI " + subjectURIStr + " " + e.getMessage());
	        }
	        
	        OWLIndividual subjectInstance = swoopModel.getSelectedOntology().getIndividual(subjectURI);
	        
	        if (subjectInstance == null){
	            throw new InstanceCreationException ("Subject instance " + subjectURIStr + " does not exist!"); 
	        }
	        
			AddDataPropertyInstance change = new AddDataPropertyInstance(swoopModel.getSelectedOntology(), subjectInstance, (OWLDataProperty) prop, dVal, null);
			changes.add(change);
			swoopModel.addUncommittedChanges(changes);
        } catch (Exception ex){
            ex.printStackTrace();
            throw new InstanceCreationException(ex.getMessage());
        }
    }
    
    public void createObjectPropertyInstance(SwoopModel swoopModel, String subjectURIStr,
			   								 OWLObjectProperty prop, String objectURIStr) throws InstanceCreationException{
        // prop is an object property
		
		try{
		    List changes = new ArrayList();
		    
		    URI subjectURI = null;
			try{
			    subjectURI = new URI(subjectURIStr);
	        } catch (Exception e){
	            throw new InstanceCreationException ("Invalid instance URI " + subjectURIStr + " " + e.getMessage());
	        }
	        
	        OWLIndividual subjectInstance = swoopModel.getSelectedOntology().getIndividual(subjectURI);
			
	        if (subjectInstance == null){
	            throw new InstanceCreationException ("Subject instance " + subjectURIStr + " does not exist!"); 
	        }
	        
	        URI objectURI = null;
			try{
			    objectURI = new URI(objectURIStr);
	        } catch (Exception e){
	            throw new InstanceCreationException ("Invalid instance URI " + objectURIStr + " " + e.getMessage());
	        }
	        
	        OWLIndividual objectInstance = swoopModel.getSelectedOntology().getIndividual(objectURI);
			
	        if (objectInstance == null){
	            throw new InstanceCreationException ("Object instance " + objectURIStr + " does not exist!"); 
	        }
	        
			AddObjectPropertyInstance change = new AddObjectPropertyInstance(swoopModel.getSelectedOntology(), subjectInstance, prop, objectInstance, null);
			changes.add(change);
			//**************************************************
			//Added for Econnections
			//***************************************************
			if(prop instanceof OWLObjectProperty){
				if(((OWLObjectProperty)prop).isLink()){
				   RemoveEntity oc2 = new RemoveEntity(swoopModel.getSelectedOntology(),objectInstance, null);
				   changes.add(oc2);
		      }
			}
			//*********************************
		
			swoopModel.addUncommittedChanges(changes);
		} catch (Exception e){
		    e.printStackTrace();
		    throw new InstanceCreationException(e.getMessage());
		}

    }

    public void createPropertyInstance(SwoopModel swoopModel, String subjectURIStr,
            						   String propURIStr, String objectURIStr) throws InstanceCreationException{
        
        OWLOntology ontology = swoopModel.getSelectedOntology();
        URI propURI = null;
        
        try{
            propURI = new URI(propURIStr);
        } catch (Exception e){
            throw new InstanceCreationException ("Invalid property URI " + e.getMessage());
        }
        
        OWLDataProperty dataProp = null;
        OWLObjectProperty objProp = null;
        
        try{
            dataProp = ontology.getDataProperty(propURI);
            if (dataProp != null){
                // it is a data property
                createDataPropertyInstance(swoopModel, subjectURIStr, dataProp, objectURIStr);
                return;
            }
        } catch (Exception ex){
            ex.printStackTrace();
            throw new InstanceCreationException(ex.getMessage());
        }
        
        try{
            objProp = ontology.getObjectProperty(propURI);
            if (objProp != null){
                // it is a object property
                createObjectPropertyInstance(swoopModel, subjectURIStr, objProp, objectURIStr);
                return;
            }
        } catch (Exception ex){
            ex.printStackTrace();
            throw new InstanceCreationException(ex.getMessage());
        }
        
        throw new InstanceCreationException("Property " + propURIStr + " does not exist");
    }
    
    public void createInstances(Frame frame, String[] n3, SwoopModel swoopModel) throws InstanceCreationException{
        
        this.frame = frame;
        for (int i=0;i<n3.length; i++){
            N3Statement stmt = parse(n3[i]);
            System.out.println(stmt.toString());
            
            if (stmt.getPredicate().equalsIgnoreCase("a")){
                // this is to create a class instance
                try{
                  createClassInstance(swoopModel, stmt.getSubject(), stmt.getObject());
                } catch (Exception e){
                    //e.printStackTrace();
                    throw new InstanceCreationException(e.getMessage());
                }
            } else {
                // this is to create a property instance
                try{
                    createPropertyInstance(swoopModel, stmt.getSubject(), stmt.getPredicate(),
                        			       stmt.getObject());
                } catch (Exception e){
                    //e.printStackTrace();
                    throw new InstanceCreationException(e.getMessage());
                }
            }
        }
    }
    
    /*
    public static void main(String[] args){
        
        InstanceCreator c = new InstanceCreator();
        
        
        String n1 = "http://counterterror.mindswap.org/2005/terrorism.owl#TestOrganization00 a http://counterterror.mindswap.org/2005/terrorism.owl#Organization";
        String n2 = "http://counterterror.mindswap.org/2005/terrorism.owl#TestOrganization00 http://counterterror.mindswap.org/2005/terrorism.owl#hasDescription 'This is a terorist Organization'";
        
        List l = new ArrayList();
        l.add(n1);
        l.add(n2);
        
        try{
        	c.createInstances(null, (String[])l.toArray(new String[0]), null);
        } catch (Exception e){
        	e.printStackTrace();
        }
    }*/
}
