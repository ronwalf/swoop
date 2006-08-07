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

package org.mindswap.swoop.utils;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.JTree;

import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.change.ChangeLog;
import org.mindswap.swoop.change.SwoopChange;
import org.mindswap.swoop.reasoner.SwoopReasoner;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.change.OntologyChange;

/**
 * Cache objects (trees/list) for ontology, reasoner pairs. Each ontology is
 * mapped to another map which maps reasoners to objects.
 * Need a different cache with corresponding put/get function for each object type
 * 
 * @author Evren Sirin, Aditya Kalyanpur
 */

public class SwoopCache implements Serializable {
    Map treeCache, listCache, annotationCache, changeCache, reasonerCache;
    
    public SwoopCache() {
        treeCache = new HashMap();
        listCache = new HashMap();
        annotationCache = new HashMap();
        changeCache = new HashMap();
        reasonerCache = new HashMap();
    }
    
    public void putReasoner(OWLOntology ontology, String reasName, SwoopReasoner reasoner) {
    	try {
	    	String key = ontology.getURI()+">"+reasName;
	    	reasonerCache.put(key, reasoner);
    	}
    	catch (OWLException ex) {
    		ex.printStackTrace();
    	}
    }
    
    public SwoopReasoner getReasoner(OWLOntology ontology, String reasName) {
    	try {
	    	String key = ontology.getURI()+">"+reasName;
	    	if (reasonerCache.containsKey(key)) return (SwoopReasoner) reasonerCache.get(key);
    	}
    	catch (OWLException ex) {
    		ex.printStackTrace();
    	}
	    return null;
    }
    
    public void putTree(OWLOntology ontology, SwoopReasoner reasoner, JTree tree) {
        Map map = (Map) treeCache.get(ontology);
        if(map == null) {
            map = new HashMap();
            treeCache.put(ontology, map);
        }
        map.put(reasoner, tree);
    }
    
    public void putList(OWLOntology ontology, int ENTITY_TYPE, SwoopReasoner reasoner, Vector list) {
        Map[] map = (Map[]) listCache.get(ontology);
        //****************************************************************
        //Changed for Econn
        //****************************************************************
       
        if(ENTITY_TYPE==SwoopModel.GCI){
        	ENTITY_TYPE=4;
        }
        else
        if(ENTITY_TYPE==SwoopModel.FOREIGN_ENT){
        	ENTITY_TYPE=5;
        }
       
        if (map==null) map = new Map[6]; // five types of entities for now
        //***************************************************************
        if(map[ENTITY_TYPE] == null) {
        		map[ENTITY_TYPE] = new HashMap();
        		listCache.put(ontology, map);
        	 	
        	}
        
        
           	map[ENTITY_TYPE].put(reasoner, list);
           
    }
    
    public void putAnnotationSet(URI OWLObjectURI, Set annotationSet) {
    	annotationCache.put(OWLObjectURI, annotationSet);
    }
    
    public Set getAnnotationSet(URI OWLObjectURI) {
    	return (Set) annotationCache.get(OWLObjectURI);
    }
    
    /* Add a pair of (uri, swoop-change) into the hashmap. Appends to the change-list already present for that uri */
    public void addChange(URI owlObjectURI, SwoopChange change) {
    	List changeList = new ArrayList();
    	if (changeCache.containsKey(owlObjectURI)) changeList = (ArrayList) changeCache.get(owlObjectURI);
    	changeList.add(change);
    	changeCache.put(owlObjectURI, changeList);
    }
    
    /* Put a pair of (uri, swoop-change-list) into the hashmap */
    public void putChangeList(URI owlObjectURI, List changeList) {
    	changeCache.put(owlObjectURI, changeList);
    }
    
    /* Return a list of SwoopChange objects associated with a specific OWLObject URI */
    public List getChangeList(URI owlObjectURI) {
    	if (changeCache.containsKey(owlObjectURI)) {
    		List cached = (ArrayList) changeCache.get(owlObjectURI); 
    		return new ArrayList(cached); // return copy, in case its modified by calling fn
    	}
    	else return new ArrayList();
    }
    
    public List getAllChanges() {
    	List allChanges = new ArrayList();
    	for (Iterator iter = changeCache.keySet().iterator(); iter.hasNext();) {
    		URI uri = (URI) iter.next();
    		allChanges.addAll((ArrayList) changeCache.get(uri));
    	}
    	return allChanges;
    }
    
    public Map getChangeMap() {
    	return this.changeCache;
    }
    
    public void putChangeMap(Map changeMap) {
    	this.changeCache = changeMap;
    }
    
    public void putAllChanges(List changeList) {
    	for (Iterator iter = changeList.iterator(); iter.hasNext();) {
    		SwoopChange swc = (SwoopChange) iter.next();
    		this.addChange(swc.getOwlObjectURI(), swc);
    	}
    }
    
    /* Remove a swoop-change associated with a uri in hashmap */
    public void removeChange(URI uri, SwoopChange remSwc) {
    	if (changeCache.containsKey(uri)) {
    		List changeList = (ArrayList) changeCache.get(uri);
    		// iterate through changeList and remove any swoop-change
    		// whose all components match except extraSubjects
    		SwoopChange match = null;
    		for (int i=0; i<changeList.size(); i++) {
    			SwoopChange swc = (SwoopChange) changeList.get(i);
    			if (swc.getAuthor().equals(remSwc.getAuthor()) &&
					swc.getChange().equals(remSwc.getChange()) &&
					swc.getTimeStamp().equals(remSwc.getTimeStamp())) {
						match = swc;
						break;
					}
    		}
    		if (match!=null) changeList.remove(match);
    		changeCache.put(uri, changeList);
    	}
    }
    
    /* Remove an OntologyChange associated with a uri in hashmap 
     * Used for removing uncommitted changes, once they become committed */
    public void removeOntologyChange(URI uri, OntologyChange change, boolean isCommitted) {
    	if (changeCache.containsKey(uri)) {
    		List changeList = (ArrayList) changeCache.get(uri);
    		SwoopChange match = null;
    		// iterate thorough change list and remove any swoop-change 
    		// whose OntologyChange object and committed value matches arguments passed to method
    		for (int i=0; i<changeList.size(); i++) {
    			SwoopChange swc = (SwoopChange) changeList.get(i);
    			if (swc.getChange().equals(change) && swc.isCommitted() == isCommitted) 
    				match = swc;
    		}
    		if (match!=null) changeList.remove(match);
    		changeCache.put(uri, changeList);
    	}
    }
    
    public void removeAllChanges(URI uri) {
    	if (changeCache.containsKey(uri)) {
    		changeCache.remove(uri);
    	}
    }
    
    public void removeAllChanges() {
    	changeCache.clear();
    }
    
    /* A more elaborate remove change method which finds all subjects of change (using ChangeLog.java)
     * then removes change from each subject map
     */
    public void removeOntologyChange(OntologyChange change, SwoopModel swoopModel) {
    	ChangeLog clog = new ChangeLog(null, swoopModel);
    	List uris = new ArrayList();
    	clog.getChangeInformation(change, clog.CHANGE_DESCRIPTION, null, uris, null);
    	for (Iterator iter = uris.iterator(); iter.hasNext();) {
    		URI uri = (URI) iter.next();
    		this.removeOntologyChange(uri, change, false); 
    		this.removeOntologyChange(uri, change, true); 
    	}
    }
    
    public JTree getTree(OWLOntology ont, SwoopReasoner reasoner) {
        Map map = (Map) treeCache.get(ont);
        return (map == null) ? null : (JTree) map.get(reasoner);
    }
    
    public Vector getList(OWLOntology ont, int ENTITY_TYPE, SwoopReasoner reasoner) {
        Map[] map = (Map[]) listCache.get(ont);
        if (map==null) return null;
        //*******************************************************
        //Changed for Econnections
        //*******************************************************
        else{ 
        	if (ENTITY_TYPE==SwoopModel.GCI)
        		return (map[4] == null) ? null : (Vector) map[4].get(reasoner);
        	else
        	if (ENTITY_TYPE==SwoopModel.FOREIGN_ENT)
        		return (map[5] == null) ? null : (Vector) map[5].get(reasoner);
        	else 
        		return (map[ENTITY_TYPE] == null) ? null : (Vector) map[ENTITY_TYPE].get(reasoner);
        }
    }
    
    public void removeOntology(OWLOntology ont) {
    	
        treeCache.remove(ont);
        listCache.remove(ont);
        
        // clear changeCache w.r.t ont
        try {
        	// remove all changes associated with ontology
            this.removeAllChanges(ont.getURI());
          
            // also remove changes where they appear in multiple places: 
            // i.e., any entityURI which is referred to by the change
        	Set entities = ont.getClasses();
        	entities.addAll(ont.getDataProperties());
        	entities.addAll(ont.getObjectProperties());
        	entities.addAll(ont.getIndividuals());
        	for (Iterator iter = entities.iterator(); iter.hasNext();) {
        		OWLEntity entity = (OWLEntity) iter.next();
        		this.removeAllChanges(entity.getURI());
        	}
        }
        catch (OWLException ex) {
        	ex.printStackTrace();
        }
        
        //TODO: remove from annotation cache?
    }
    
    /*
     * Remove reasoner instance associated with the specified ontology and of the given name (class)
     */
    public void removeReasonerOntology(OWLOntology ont, String reasName) {
    	try {
			String key = ont.getURI()+">"+reasName;
			reasonerCache.remove(key);			
		} catch (OWLException e) {
			e.printStackTrace();
		}
    }
    
    /*
     * Remove all reasoner instances associated with the specified ontology
     */
    public void removeReasoners(OWLOntology ont) {
    	try {
    		String keyPrefix = ont.getURI()+">";
    		Set keyset = reasonerCache.keySet();
    		for (Iterator iter = new HashSet(keyset).iterator(); iter.hasNext();) {
    			String key = iter.next().toString();
    			if (key.startsWith(keyPrefix)) reasonerCache.remove(key);
    		}
    	}
    	catch (OWLException ex) {
    		ex.printStackTrace();
    	}
    }
}
