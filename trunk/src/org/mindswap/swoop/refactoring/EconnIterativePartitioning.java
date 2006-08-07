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

package org.mindswap.swoop.refactoring;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mindswap.pellet.utils.Timer;
import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.reasoner.SwoopReasoner;
import org.mindswap.swoop.utils.SetUtils;
import org.mindswap.swoop.utils.change.EnumElementChange;
import org.mindswap.swoop.utils.owlapi.IndicesLibrary;
import org.mindswap.swoop.utils.owlapi.OntologyIndices;
import org.semanticweb.owl.io.vocabulary.OWLVocabularyAdapter;
import org.semanticweb.owl.model.OWLAnd;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataType;
import org.semanticweb.owl.model.OWLDataValue;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDisjointClassesAxiom;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLEnumeration;
import org.semanticweb.owl.model.OWLEquivalentPropertiesAxiom;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLNamedObject;
import org.semanticweb.owl.model.OWLNaryBooleanDescription;
import org.semanticweb.owl.model.OWLNot;
import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectQuantifiedRestriction;
import org.semanticweb.owl.model.OWLObjectRestriction;
import org.semanticweb.owl.model.OWLObjectValueRestriction;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOr;
import org.semanticweb.owl.model.OWLProperty;
import org.semanticweb.owl.model.OWLPropertyAxiom;
import org.semanticweb.owl.model.OWLRestriction;
import org.semanticweb.owl.model.OWLSubClassAxiom;
import org.semanticweb.owl.model.OWLSubPropertyAxiom;
import org.semanticweb.owl.model.change.AddClassAxiom;
import org.semanticweb.owl.model.change.AddDataPropertyInstance;
import org.semanticweb.owl.model.change.AddDataPropertyRange;
import org.semanticweb.owl.model.change.AddDomain;
import org.semanticweb.owl.model.change.AddEntity;
import org.semanticweb.owl.model.change.AddEquivalentClass;
import org.semanticweb.owl.model.change.AddForeignEntity;
import org.semanticweb.owl.model.change.AddIndividualClass;
import org.semanticweb.owl.model.change.AddInverse;
import org.semanticweb.owl.model.change.AddObjectPropertyInstance;
import org.semanticweb.owl.model.change.AddObjectPropertyRange;
import org.semanticweb.owl.model.change.AddPropertyAxiom;
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
import org.semanticweb.owl.model.change.RemoveForeignEntity;
import org.semanticweb.owl.model.change.RemoveIndividualClass;
import org.semanticweb.owl.model.change.RemoveInverse;
import org.semanticweb.owl.model.change.RemoveObjectPropertyInstance;
import org.semanticweb.owl.model.change.RemoveObjectPropertyRange;
import org.semanticweb.owl.model.change.RemovePropertyAxiom;
import org.semanticweb.owl.model.change.RemoveSuperClass;
import org.semanticweb.owl.model.change.RemoveSuperProperty;
import org.semanticweb.owl.model.change.SetFunctional;
import org.semanticweb.owl.model.change.SetInverseFunctional;
import org.semanticweb.owl.model.change.SetLinkTarget;
import org.semanticweb.owl.model.change.SetSymmetric;
import org.semanticweb.owl.model.change.SetTransitive;

/**
 * @author bernardo, aditya
 *
 */
public class EconnIterativePartitioning {
	
	protected SwoopModel swoopModel;
	protected SwoopReasoner reasoner;
	
	private boolean DEBUG = false;
	private boolean APPLY = false;

	ArrayList changes ;
	
	Timer timers = new Timer("Iteration");
	Timer timers2 = new Timer("Copying a large description set");
	Timer timers4 = new Timer("SubClasses/SuperClasses/EquClasses");
	Timer timers8 = new Timer("Actual Computation of Indices");
	Timer timers9 = new Timer("Handling Descendants");
	
	protected StringWriter out;
	protected OWLOntology source;
	protected OWLOntology target;
//	**********************************************************
	//Sets storing the foreign entities created in the partitioning
	//Step
	//***********************************************************
	protected Set addedForeignEntitiesInSource;
	protected Set addedForeignEntitiesInTarget;
	
	//************************************************************
	//Indexes storing general information about the SOURCE ontology
	//**********************************************************
	protected Set Restrictions; // All Restrictions

	protected Set classesInRestriction; //Classes and Class descriptions appearing in a restrictions
	protected Map classesAndPropertiesRestriction;//Key: elements of classes in Restrictions
	                                              //Content: properties in those restrictions
	
	protected Map propToIndividualAssertions; // key - prop R; value - individual a, such that R(a,b)
	protected Map propToDescriptionRestrictions; // key: property R, value: Description C which is a restriction on property R
	
	protected Set individualsInRestriction; //Individuals appearing in a restriction
	protected Map individualsAndPropertiesRestriction;//Key: Elements of individualsInRestriction 
	                                                  //Content: properties in those restrictions
	protected Map individualsAndPropertiesAssertions; // Key: individuals, Value: properties related to it by abox assertions
	public Map individualsInEnumerations; // key - individual a, value - enumeration which contains a
	
	protected Map classesWithDomain;  //Key: Classes and class descriptions that are domain of some property
	                                  //Content: The properties they are domain of
	protected Map classesWithRange;
	protected Set Unions;
	protected Set Intersections;
	protected Set disjointAxioms;
	protected Map ClassesInUnions; // Class descriptions appearing in Unions
	protected Map ClassesInIntersections; // Class Descriptions appearing in intersections
	protected Map linksToSource; //Link properties pointing at the source ontology
	protected HashSet updatedLP; // link properties that need to be updated because link target has been moved to a new ontology
	protected Map nestedRestrictions; // Key: An object property R
	                                  // Value: A Set S1,...,Sn of object properties
	                                 // Means that S1,...,Sn appear in a restriction nested inside a restriction on R
	protected Set LinkProperties; //The set of link properties in SOURCE
	protected Map equivalents;
	protected Map subClasses, superClasses, subProperties, superProperties, instancesOf;
	protected Map boundedInverses;
	protected Map updatedInverses;
	
	//***set of descriptions left in source - first time computed using OntologyIndices
	// then its modified at the end of each partition
	protected Set descriptionsLeft; 
	
	protected IndicesLibrary indexedLibrary;
	
	//State map
	// key: OWL Object, value - State integer
	Map State;
	OWLEntity owlThing;
	
	// 4 possible states
	static String STATE_O1 = "1"; // in O1
	static String STATE_O2 = "2"; // in O2
	static String STATE_L12 = "3"; // link from 1 to 2
	static String STATE_L21 = "4"; // link from 2 to 1
	
	//Sets for making changes..
	// Notation:
	// O1 = OWL Object in Ontology 1 (source)
	// O2 = OWL Object in Ontology 2 (target)
	// S1 = Object Property in Ontology 1
	// S2 = Link Property from O1 to O2
	// S3 = Link Property from O2 to O1
	// S4 = Object Property in Ontology 2	
	Set DescriptionsO1,DescriptionsO2,IndividualsO1,IndividualsO2;
	Set DataPropO1, DataPropO2, LinkPropO1, LinkPropO2;
	Set ObjPropS1, ObjPropS2,ObjPropS3,ObjPropS4;
	Set removedForeignEntitiesInSource;
	
    String Expressivity = " ";
    
	public EconnIterativePartitioning(SwoopModel swoopModel) {
		this.swoopModel = swoopModel;
		this.reasoner = swoopModel.getReasoner();
		this.indexedLibrary = new IndicesLibrary(swoopModel);
	}
	
	public void reset(OWLOntology source, OWLOntology target, Map linksToSource, Map boundedInverses) throws OWLException {
		
		// reset all parameters for any new partitioning step
		this.source = source;
		this.target = target;
		
		this.State = new HashMap();
		
		this.DescriptionsO1 = new HashSet();
		this.DescriptionsO2 = new HashSet();
		this.IndividualsO1 = new HashSet();
		this.IndividualsO2 = new HashSet();
		this.DataPropO1 = new HashSet();
		this.DataPropO2 = new HashSet();
		this.LinkPropO1 = new HashSet();
		this.LinkPropO2 = new HashSet();
		this.ObjPropS1 = new HashSet();
		this.ObjPropS2 = new HashSet();
		this.ObjPropS3 = new HashSet();
		this.ObjPropS4 = new HashSet();
		
		this.updatedLP = new HashSet();
		this.linksToSource = linksToSource;
		this.boundedInverses = boundedInverses;
		this.updatedInverses = new HashMap();
		
		this.addedForeignEntitiesInSource = new HashSet();
		this.addedForeignEntitiesInTarget = new HashSet();
		this.removedForeignEntitiesInSource = new HashSet();
		
		this.changes = new ArrayList();
		
		// populate OWL object sets
		Set all = new HashSet();
		this.DescriptionsO1 = this.descriptionsLeft;
		all.addAll(this.DescriptionsO1);
		this.IndividualsO1.addAll(source.getIndividuals());
		all.addAll(this.IndividualsO1);
		this.DataPropO1.addAll(source.getDataProperties());
		all.addAll(this.DataPropO1);
		this.ObjPropS1.addAll(source.getObjectProperties());		
		this.LinkPropO1.addAll(source.getLinkProperties());
		all.addAll(this.LinkPropO1);
		this.ObjPropS1.removeAll(this.LinkPropO1);
		all.addAll(this.ObjPropS1);
		
		// populate state map using the set "all" created above
		// i.e. for each entity in "all", set its initial state to O1
		for (Iterator iter = all.iterator(); iter.hasNext();) {
			OWLObject obj = (OWLObject) iter.next();
			this.State.put(obj, this.STATE_O1);
		}
	}
	
	/**
	 * Call init only once!!
	 * 
	 */
	public void init(OWLOntology source, OWLOntology target, Map linksToSource, Map boundedInverses) throws OWLException {

		if (DEBUG) System.out.println("Starting initialization.."+swoopModel.getTimeStamp());
		
		// init owl:Thing
		owlThing = source.getOWLDataFactory().getOWLClass( URI.create( OWLVocabularyAdapter.INSTANCE.getThing()));
		
		// build indices using SwoopUtils.OntologyIndices class
		timers8.start();
		indexedLibrary.computeIndices(source);
		timers8.stop();
		OntologyIndices indices = indexedLibrary.getIndices(source);
		Expressivity = indices.getExpressivity(source);
		
		// map external indices to local file data structures
		this.descriptionsLeft = new HashSet();
		this.descriptionsLeft.addAll(source.getClasses());
		for (Iterator iter = indices.descriptions.iterator(); iter.hasNext();) {
			this.descriptionsLeft.add((OWLDescription) iter.next());
		}
		this.Restrictions = indices.restrictionToEntity.keySet();
		this.classesAndPropertiesRestriction = indices.classesToPropertiesRestriction;
		this.classesInRestriction = indices.classesToPropertiesRestriction.keySet();
		this.individualsAndPropertiesRestriction = indices.individualsToPropertiesRestriction;
		this.individualsAndPropertiesAssertions = indices.individualsToPropertiesAssertions;
		this.individualsInRestriction = indices.individualsToPropertiesRestriction.keySet();
		this.individualsInEnumerations = indices.individualsInEnumerations;
		this.propToIndividualAssertions = indices.propToIndividualAssertions;
		this.propToDescriptionRestrictions = indices.propToDescriptionRestrictions;
		this.nestedRestrictions = indices.nestedRestrictions;
		this.classesWithDomain = indices.classesToDomain;
		this.classesWithRange = indices.classesToRange;
		this.equivalents = indices.equivalents;
		this.subClasses = indices.subClasses;
		this.superClasses = indices.superClasses;
		this.subProperties = indices.subProperties;
		this.superProperties = indices.superProperties;
		this.instancesOf = indices.instancesOf;
		this.ClassesInIntersections = indices.classesInIntersections;
		this.ClassesInUnions = indices.classesInUnions;
		this.Unions = indices.unions;
		this.Intersections = indices.intersections;
		this.disjointAxioms = indices.disjointAxioms;
		
		if(DEBUG) {
			System.out.println("Initialization Complete. "+swoopModel.getTimeStamp());
			System.out.println("Stored " + Restrictions.size() + " Restrictions");
			System.out.println("Stored " + Unions.size() + " Unions");
			System.out.println("Stored " + Intersections.size() + " Intersections");
			System.out.println("Stored " + indices.descriptions.size() + " Descriptions");
			System.out.println("There are "+  classesWithDomain.keySet().size()+ " classes which are domain of some property" );
			System.out.println("There are "+  classesWithRange.keySet().size()+ " classes which are range of some property" );
			System.out.println("There are " + linksToSource.keySet().size() + " link properties pointing at the source ontology");
		}
		System.out.println("Stored " + indices.descriptions.size() + " Descriptions");
		
	}
 
	public StringWriter getTrace(){
		return out;
	}
	
	public void RunStateMachine(OWLClass initial) throws OWLException {
		int threshold = 3;
		
		if (DEBUG) System.out.println("Starting state machine with Class "+getName(initial)+".."+swoopModel.getTimeStamp());
		out.write("Moving INITIALLY class "+getName(initial));
		// move the state of the initial class to O2 and then run state machine
		this.makeStateChange(initial, STATE_O2, this.DescriptionsO1, this.DescriptionsO2);
		
		//Optimization: We know that the ancestors and descendants of the
		//initially moved class must move as well
		timers9.start();
		Set related2 = new HashSet();
		if(DEBUG)System.out.println("Start computing descendants");
		related2.addAll(SetUtils.union(reasoner.descendantClassesOf(initial)));
		related2.addAll(SetUtils.union(reasoner.ancestorClassesOf(initial)));
		related2.remove(owlThing);
		if(DEBUG)System.out.println("Stop computing descendants");
		for (Iterator iter2 = related2.iterator(); iter2.hasNext();) {
			OWLDescription relDesc = (OWLDescription) iter2.next();
			if (checkState(relDesc, STATE_O1)) {
				if (DEBUG) System.out.println("Moving class "+getName(relDesc) + " because related (ANCESTOR/DESCENDANT) class "+getName(initial)+" is in O2");
				makeStateChange(relDesc, STATE_O2, DescriptionsO1, DescriptionsO2);
			}					
		}
		timers9.stop();
		
		// iteratively apply state changes for all OWL Objects
		// till no more changes can be applied
		boolean changed;
		do {
			timers.start();
			changed = false;
			boolean cont = false;
			boolean copyChanged = false;
			
			// handle state changes for classes/descriptions in O1
			
			timers2.start();
		    Set copy = new HashSet(DescriptionsO1);
			timers2.stop();
			// create a copy and iterate over copy because original set may change inside loop
			for (Iterator iter = copy.iterator(); iter.hasNext(); ) {
				
					
				OWLDescription desc = (OWLDescription) iter.next();
				
				
				// skip owl:Thing
				if (desc.equals(owlThing)) continue;
				
	
//				*** if any related (sub/super/equivalent) description is in O2
				timers4.start();
				Set related = new HashSet();
				if (this.subClasses.get(desc)!=null) related.addAll((HashSet) this.subClasses.get(desc));
				if (this.superClasses.get(desc)!=null) related.addAll((HashSet) this.superClasses.get(desc));
				if (this.equivalents.get(desc)!=null) related.addAll((HashSet) this.equivalents.get(desc));
				Set copyRelated = new HashSet(related);
				for (Iterator iter2 = related.iterator(); iter2.hasNext();) {
					OWLDescription relDesc = (OWLDescription) iter2.next();
					if (checkState(relDesc, STATE_O2)) {
						if (DEBUG) System.out.println("Moving class "+getName(desc) + " because related (EQU/SUB/SUP) class "+getName(relDesc)+" is in O2");
						//out.println("Moving class "+getName(desc) + " because related (EQU/SUB/SUP) class "+getName(relDesc)+" is in O2");
						changed = makeStateChange(desc, STATE_O2, DescriptionsO1, DescriptionsO2);
						break;						
					}					
				}
			    timers4.stop();
				   

				//*** if description instanceof OWLAnd
				if (!changed && desc instanceof OWLAnd) {
					OWLAnd and = (OWLAnd) desc;
					Set operands = and.getOperands();
					for (Iterator iter2 = operands.iterator(); iter2.hasNext();) {
						OWLDescription andDesc = (OWLDescription) iter2.next();
						if (checkState(andDesc, STATE_O2)) {
							if (DEBUG) System.out.println("Moving class "+getName(desc) + " because its an INTERSECTION and component "+getName(andDesc)+" is in O2");
							out.write("Moving class "+getName(desc) + " because its an INTERSECTION and component "+getName(andDesc)+" is in O2 <br>");
							changed = makeStateChange(desc, STATE_O2, DescriptionsO1, DescriptionsO2);
							break;						
						}
					}
				}
				
				//*** if description instanceof OWLOr
				if (!changed && desc instanceof OWLOr) {
					OWLOr or = (OWLOr) desc;
					for (Iterator iter2 = or.getOperands().iterator(); iter2.hasNext();) {
						OWLDescription orDesc = (OWLDescription) iter2.next();
						if (checkState(orDesc, STATE_O2)) {
							if (DEBUG) System.out.println("Moving class "+getName(desc) + " because its a UNION and component "+getName(orDesc)+" is in O2");
							out.write("Moving class "+getName(desc) + " because its a UNION and component "+getName(orDesc)+" is in O2 <br>");
							changed = makeStateChange(desc, STATE_O2, DescriptionsO1, DescriptionsO2);
							break;						
						}
					}
				}
				
				//*** if description instanceof OWLRestriction
				//*** and restriction involves P, then..
				if (!changed && desc instanceof OWLRestriction) {
					OWLProperty prop = ((OWLRestriction) desc).getProperty();
					if (this.checkPropertyCausingTransitionA(prop)) {
						if (DEBUG) System.out.println("Moving class "+getName(desc) + " because its a restriction on property "+getName(prop)+" and P has moved ");
						out.write("Moving class "+getName(desc) + " because its a restriction on property "+getName(prop)+" and P has moved <br>");
						changed = makeStateChange(desc, STATE_O2, DescriptionsO1, DescriptionsO2);
						//Optimization: If the number of restrictions on prop is greater than the
						//threshold, then we move those as well and exit the big loop
						if(propToDescriptionRestrictions.containsKey(prop)){
						if(((Set)propToDescriptionRestrictions.get(prop)).size()>threshold){
							for(Iterator i = ((Set)propToDescriptionRestrictions.get(prop)).iterator(); i.hasNext(); ){
							  OWLRestriction res = (OWLRestriction)i.next();
							   if(checkState(res,STATE_O1)){
							  	  changed = makeStateChange(res, STATE_O2, DescriptionsO1, DescriptionsO2);
							  	  cont = true;
							 	 }
							}
						}
						}
						break;
					}
				}
				
				if(cont) continue;
				
				//*** if C is Domain of P, then..
				if (!changed && this.classesWithDomain.get(desc)!=null) {
					for (Iterator iter2 = ((HashSet) classesWithDomain.get(desc)).iterator(); iter2.hasNext(); ) {
						OWLProperty prop = (OWLProperty) iter2.next();
						if (this.checkPropertyCausingTransitionA(prop)) {
							if (DEBUG) System.out.println("Moving class "+getName(desc) + " because its DOMAIN of property "+getName(prop)+" and P has moved ");
							out.write("Moving class "+getName(desc) + " because its DOMAIN of property "+getName(prop)+" and P has moved <br>");
							changed = makeStateChange(desc, STATE_O2, DescriptionsO1, DescriptionsO2);
							//Optimization: If prop has many other domains, move all of them, since all the domains
							//of a property must end in the same partition
							if(reasoner.domainsOf(prop).size() > threshold){
								for(Iterator i = reasoner.domainsOf(prop).iterator(); i.hasNext(); ){
									OWLDescription d = (OWLDescription)i.next();
									 if(checkState(d,STATE_O1)){
									  	  changed = makeStateChange(d, STATE_O2, DescriptionsO1, DescriptionsO2);
									  	  if(DEBUG) System.out.println("OPTIMIZATION: Moved Class" + getName(d) + " because it is a domain of" + getName(prop) + "and other of its domains has moved");
									  	  cont = true;
								 	 }
								}
							}
							break;
						}
					}
				}
				
				if(cont) continue;
				
				//*** if C is range of P, then..
				if (!changed && this.classesWithRange.get(desc)!=null) {
					for (Iterator iter2 = ((HashSet) classesWithRange.get(desc)).iterator(); iter2.hasNext(); ) {
						OWLProperty prop = (OWLProperty) iter2.next();
						if (this.checkPropertyCausingTransitionB(prop)) {
							if (DEBUG) System.out.println("Moving class "+getName(desc) + " because its RANGE of property "+getName(prop)+" and P has moved ");
							out.write("Moving class "+getName(desc) + " because its RANGE of property "+getName(prop)+" and P has moved <br>");
							changed = makeStateChange(desc, STATE_O2, DescriptionsO1, DescriptionsO2);
//							Optimization: If prop has many other ranges, move all of them, since all the domains
							//of a property must end in the same partition
							if(reasoner.rangesOf(prop).size() > threshold){
								for(Iterator i = reasoner.domainsOf(prop).iterator(); i.hasNext(); ){
									OWLDescription d = (OWLDescription)i.next();
									 if(checkState(d,STATE_O1)){
									  	  changed = makeStateChange(d, STATE_O2, DescriptionsO1, DescriptionsO2);
									  	  if(DEBUG) System.out.println("OPTIMIZATION: Moved Class" + getName(d) + " because it is a range of" + getName(prop) + "and other of its domains has moved");
									  	  cont = true;
								 	 }
								}
							}
							break;
						}
					}
				}
				if(cont) continue;
						
				// *** check if C(a) and a in O2
				if (!changed && instancesOf.get(desc)!=null) {
					for (Iterator iter2 = ((HashSet) instancesOf.get(desc)).iterator(); iter2.hasNext();) {
						OWLIndividual ind = (OWLIndividual) iter2.next();
						if (checkState(ind, STATE_O2)) {
							if (DEBUG) System.out.println("Moving class "+getName(desc) + " because its TYPE of instance "+getName(ind)+" and I has moved ");
							out.write("Moving class "+getName(desc) + " because its TYPE of instance "+getName(ind)+" and I has moved <br>");
							changed = makeStateChange(desc, STATE_O2, DescriptionsO1, DescriptionsO2);
							break;
						}
					}
				}
				
				//*** if C is in a restriction involving prop P, then..
				if (!changed && this.classesAndPropertiesRestriction.get(desc)!=null) {
					for (Iterator iter2 = ((HashSet) classesAndPropertiesRestriction.get(desc)).iterator(); iter2.hasNext(); ) {
						OWLProperty prop = (OWLProperty) iter2.next();
						if (!isLinkProperty(prop) && prop instanceof OWLObjectProperty && (checkState(prop, STATE_L12) || checkState(prop, STATE_O2))) {
							if (DEBUG) System.out.println("Moving class "+getName(desc) + " because its IN A RESTRICTION involving property "+getName(prop)+" and P is in S2/S4 ");
							out.write("Moving class "+getName(desc) + " because its IN A RESTRICTION involving property "+getName(prop)+" and P is in S2/S4 <br>");
							changed = makeStateChange(desc, STATE_O2, DescriptionsO1, DescriptionsO2);
							//Optimization: if appears in restrictions involving many other classes, move those as well
							if(((Set)propToDescriptionRestrictions.get(prop)).size()>threshold){
								for(Iterator i = ((Set)propToDescriptionRestrictions.get(prop)).iterator(); i.hasNext(); ){
								  OWLRestriction res = (OWLRestriction)i.next();
								  if(res instanceof OWLObjectQuantifiedRestriction){
								  	 OWLDescription d = ((OWLObjectQuantifiedRestriction)res).getDescription();
								 	 if(checkState(d,STATE_O2)){
								  	  changed = makeStateChange(d, STATE_O2, DescriptionsO1, DescriptionsO2);
								  	  if(DEBUG) System.out.println("I have changed here 2!");
									  	
								 	  cont = true;
								 	 }
									
								  }
									
								}
							}
							
							break;
						}
						
					}
				}
				
				if(cont) continue;
				
				//*** if C is IN AN Intersection/union and any of its components are in O2
				if (!changed && this.ClassesInIntersections.get(desc)!=null) {
					Set aux = (HashSet) this.ClassesInIntersections.get(desc);
					for (Iterator iter2 = aux.iterator(); iter2.hasNext(); ) {
						OWLDescription intDesc = (OWLDescription) iter2.next();
						if (checkState(intDesc, STATE_O2)) {
							if (DEBUG) System.out.println("Moving class "+getName(desc) + " because its IN AN INTERSECTION and one of its components has moved");
							out.write("Moving class "+getName(desc) + " because its IN AN INTERSECTION and one of its components has moved <br>");
							changed = makeStateChange(desc, STATE_O2, DescriptionsO1, DescriptionsO2);
							//Optimization:
							if(aux.size()>threshold){
								Set aux2 = new HashSet(aux);
								for (Iterator it = aux2.iterator(); it.hasNext(); ) {
								  OWLDescription d =(OWLDescription)it.next();
								  if(checkState(d, STATE_O1))
									changed = makeStateChange(d, STATE_O2, DescriptionsO1, DescriptionsO2);
									
								}
								cont=true;
							}
							
							break;
						}
					}
				}
				
				if(cont) continue;
				
				if (!changed && this.ClassesInUnions.get(desc)!=null) {
					for (Iterator iter2 = ((HashSet) this.ClassesInUnions.get(desc)).iterator(); iter2.hasNext(); ) {
						OWLDescription intDesc = (OWLDescription) iter2.next();
						if (checkState(intDesc, STATE_O2)) {
							if (DEBUG) System.out.println("Moving class "+getName(desc) + " because its IN A UNION and one of its components has moved");
							out.write("Moving class "+getName(desc) + " because its IN A UNION and one of its components has moved <br>");
							changed = makeStateChange(desc, STATE_O2, DescriptionsO1, DescriptionsO2);
							break;
						}
					}
				}
				
			} //****** end classes check
			
			copyChanged = changed | copyChanged;
			changed = false;
			
			
			//********** handle state changes for individuals in O1
			copy = new HashSet(IndividualsO1);
			for (Iterator iter = copy.iterator(); iter.hasNext();) {
				OWLIndividual ind = (OWLIndividual) iter.next();
				
				//*** check if type(a) = C and C in O2
				for (Iterator iter2 = ind.getTypes(source).iterator(); iter2.hasNext();) {
					OWLDescription desc = (OWLDescription) iter2.next();
					if (checkState(desc, STATE_O2)) {
						if (DEBUG) System.out.println("Moving individual " +getName(ind) + " because its an INSTANCE-OF class "+ getName(desc) + " and C has moved");
						out.write("Moving individual " +getName(ind) + " because its an INSTANCE-OF class "+ getName(desc) + " and C has moved <br>");
						changed = this.makeStateChange(ind, STATE_O2, this.IndividualsO1, this.IndividualsO2);
						break;
					}
				}
				
				//*** if R(a,b), then based on R..
				Set relProps = new HashSet();
				relProps.addAll(ind.getDataPropertyValues(source).keySet());
				relProps.addAll(ind.getObjectPropertyValues(source).keySet());
				if (!changed) {
					for (Iterator iter2 = relProps.iterator(); iter2.hasNext();) {
						OWLProperty prop = (OWLProperty) iter2.next();
						if (this.checkPropertyCausingTransitionA(prop)) {
							if (DEBUG) System.out.println("Moving individual 'a' " +getName(ind) + " because R(a,b), where R is "+ getName(prop) + " and R has moved");
							out.write("Moving individual 'a' " +getName(ind) + " because R(a,b), where R is "+ getName(prop) + " and R has moved <br>");
							changed = makeStateChange(ind, STATE_O2, IndividualsO1, IndividualsO2);
							break;
						}
					}
				}
				
				//*** if R(b,a), then based on R..
				if (!changed && this.individualsAndPropertiesAssertions.get(ind)!=null) {
					for (Iterator iter2 = ((HashSet) this.individualsAndPropertiesAssertions.get(ind)).iterator(); iter2.hasNext();) {
						OWLProperty prop = (OWLProperty) iter2.next();
						if (this.checkPropertyCausingTransitionB(prop)) {
							if (DEBUG) System.out.println("Moving individual 'a' " +getName(ind) + " because R(b,a), where R is "+ getName(prop) + " and R has moved");
							out.write("Moving individual 'a' " +getName(ind) + " because R(b,a), where R is "+ getName(prop) + " and R has moved <br>");
							changed = makeStateChange(ind, STATE_O2, IndividualsO1, IndividualsO2);
							break;
						}
					}
				}
				
				//*** if hasValue(R,a), then based on R..
				if (!changed && this.individualsAndPropertiesRestriction.get(ind)!=null) {
					for (Iterator iter2 = ((HashSet) this.individualsAndPropertiesRestriction.get(ind)).iterator(); iter2.hasNext();) {
						OWLProperty prop = (OWLProperty) iter2.next();
						if (this.checkPropertyCausingTransitionB(prop)) {
							if (DEBUG) System.out.println("Moving individual " +getName(ind) + " because hasValue(R, a), where R is "+ getName(prop) + " and R has moved");
							out.write("Moving individual " +getName(ind) + " because hasValue(R, a), where R is "+ getName(prop) + " and R has moved <br>");
							changed = makeStateChange(ind, STATE_O2, IndividualsO1, IndividualsO2);
							break;
						}
					}
				}
				
				//*** if a is in enumeration and enum itself is in O2,..
				if (!changed && this.individualsInEnumerations.get(ind)!=null) {
					for (Iterator iter2 = ((HashSet) this.individualsInEnumerations.get(ind)).iterator(); iter2.hasNext();) {
						OWLEnumeration enu = (OWLEnumeration) iter2.next();
						if (checkState(enu, STATE_O2)) {
							if (DEBUG) System.out.println("Moving individual " +getName(ind) + " because its IN AN ENUM which itself has moved");
							out.write("Moving individual " +getName(ind) + " because its IN AN ENUM which itself has moved <br>");
							changed = makeStateChange(ind, STATE_O2, IndividualsO1, IndividualsO2);
							break;
						}
					}
				}
				
			} //**** end individuals check
			
			copyChanged = changed | copyChanged;
			changed = false;
			
			//****** check for state changes for datatype  
			//****** and link properties together
			copy = new HashSet(this.DataPropO1);
			copy.addAll(this.LinkPropO1);
			for (Iterator iter = copy.iterator(); iter.hasNext();) {
				OWLProperty prop = (OWLProperty) iter.next();
				
				Set addToSet = new HashSet();
				Set removeFromSet = new HashSet();
				// determine which sets to alter based on property
				// being considerer
				if (prop instanceof OWLDataProperty) {
					removeFromSet = this.DataPropO1;
					addToSet = this.DataPropO2;				
				}
				else {	
					// else its a link property
					removeFromSet = this.LinkPropO1;
					addToSet = this.LinkPropO2;
					
					// also check for moving linkproperty if inverse is in updatedLP
					for (Iterator iter2 = SetUtils.union(reasoner.inversePropertiesOf((OWLObjectProperty)prop)).iterator(); iter2.hasNext();) {
						OWLObjectProperty invProp = (OWLObjectProperty) iter2.next();
						if (updatedLP.contains(invProp)) {
							if (DEBUG) System.out.println("Moving LinkProperty " +getName(prop) + " because its inverse "+ getName(invProp) + " has been updated");
							out.write("Moving LinkProperty " +getName(prop) + " because its inverse "+ getName(invProp) + " has been updated <br>");
							changed = this.makeStateChange(prop, STATE_O2, this.LinkPropO1, this.LinkPropO2);
							break;
						}
					}
				}
				
				//*** check if domain(P,C) and C in O2
				if(!changed){
				for (Iterator iter2=prop.getDomains(source).iterator(); iter2.hasNext();) {
					OWLDescription desc = (OWLDescription) iter2.next();
					if (this.checkState(desc, STATE_O2)) {
						if (DEBUG) System.out.println("Moving Link/Data Property " +getName(prop) + " because its DOMAIN "+ getName(desc) + " has moved");
						out.write("Moving Link/Data Property " +getName(prop) + " because its DOMAIN "+ getName(desc) + " has moved <br>");
						changed = this.makeStateChange(prop, STATE_O2, removeFromSet, addToSet);
						if(prop instanceof OWLObjectProperty){
							for (Iterator it = SetUtils.union(reasoner.inversePropertiesOf((OWLObjectProperty)prop)).iterator(); it.hasNext();) {
								OWLObjectProperty invProp = (OWLObjectProperty) it.next();
								updatedInverses.put(invProp,target.getURI());
							}
						}
						break;
					}
				}
				}
				//*** check if P(a,d) and a in O2
				if (!changed && this.propToIndividualAssertions.get(prop)!=null) {
					for (Iterator iter2=((HashSet) this.propToIndividualAssertions.get(prop)).iterator(); iter2.hasNext();) {
						OWLIndividual ind = (OWLIndividual) iter2.next();
						if (checkState(ind, STATE_O2)) {
							if (DEBUG) System.out.println("Moving Link/Data Property " +getName(prop) + " because R(a,d), where a is "+ getName(ind) + " and has moved");
							out.write("Moving Link/Data Property " +getName(prop) + " because R(a,d), where a is "+ getName(ind) + " and has moved <br>");
							changed = this.makeStateChange(prop, STATE_O2, removeFromSet, addToSet);
							if(prop instanceof OWLObjectProperty){
								for (Iterator it = SetUtils.union(reasoner.inversePropertiesOf((OWLObjectProperty)prop)).iterator(); it.hasNext();) {
									OWLObjectProperty invProp = (OWLObjectProperty) it.next();
									updatedInverses.put(invProp,target.getURI());
								}
							}
							break;
						}
					}
				}
				
				//*** check if related (sup/super/equ) properties of P are in O2
				if (!changed) {
					Set related = new HashSet();
					if (equivalents.get(prop)!=null) related.addAll((HashSet) this.equivalents.get(prop));
					if (subProperties.get(prop)!=null) related.addAll((HashSet) this.subProperties.get(prop));
					if (superProperties.get(prop)!=null) related.addAll((HashSet) this.superProperties.get(prop));
					for (Iterator iter2 = related.iterator(); iter2.hasNext();) {
						OWLProperty relProp = (OWLProperty) iter2.next();
						if (checkState(relProp, STATE_O2)) {
							if (DEBUG) System.out.println("Moving Link/Data Property " +getName(prop) + " because related (EQU/SUB/SUPER) property "+ getName(relProp) + " has moved");
							out.write("Moving Link/Data Property " +getName(prop) + " because related (EQU/SUB/SUPER) property "+ getName(relProp) + " has moved <br>");
							changed = this.makeStateChange(prop, STATE_O2, removeFromSet, addToSet);
							if(prop instanceof OWLObjectProperty){
								for (Iterator it = SetUtils.union(reasoner.inversePropertiesOf((OWLObjectProperty)prop)).iterator(); iter2.hasNext();) {
									OWLObjectProperty invProp = (OWLObjectProperty) it.next();
									updatedInverses.put(invProp,target.getURI());
								}
							}
							break;		
						}
					}
				}
				
				//*** check if restriction involves P and restriction itself is in O2..
				if (!changed && this.propToDescriptionRestrictions.get(prop)!=null) {
					for (Iterator iter2=((HashSet) this.propToDescriptionRestrictions.get(prop)).iterator(); iter2.hasNext();) {
						OWLDescription desc = (OWLDescription) iter2.next();
						if (checkState(desc, STATE_O2)) {
							if (DEBUG) System.out.println("Moving Link/Data Property " +getName(prop) + " because its IN A RESTRICTION which itself has moved");
							out.write("Moving Link/Data Property " +getName(prop) + " because its IN A RESTRICTION which itself has moved <br>");
							changed = this.makeStateChange(prop, STATE_O2, removeFromSet, addToSet);
							if(prop instanceof OWLObjectProperty){
								for (Iterator it = SetUtils.union(reasoner.inversePropertiesOf((OWLObjectProperty)prop)).iterator(); it.hasNext();) {
									OWLObjectProperty invProp = (OWLObjectProperty) it.next();
									updatedInverses.put(invProp,target.getURI());
								}
							}
							break;
						}
					}
				}
			}// end dataproperty/linkproperty check
			
			copyChanged = changed | copyChanged;
			changed = false;
			
			//*** check for state transition for object properties
			copy = new HashSet(this.ObjPropS1);
			copy.addAll(this.ObjPropS2);
			copy.addAll(this.ObjPropS3);
			for (Iterator iter = copy.iterator(); iter.hasNext();) {
				OWLObjectProperty prop = (OWLObjectProperty) iter.next();
				
				Set removeFrom = new HashSet();
				String currentState = "";
				if (checkState(prop, STATE_O1)) { removeFrom = this.ObjPropS1; currentState = "S1"; } 
				if (checkState(prop, STATE_L12)) { removeFrom = this.ObjPropS2; currentState = "S2"; }
				if (checkState(prop, STATE_L21)) { removeFrom = this.ObjPropS3; currentState = "S3"; }
				
				//*** check if related (sup/super/equ) properties of P are in S2,S3,S4
				Set related = new HashSet();
				if (equivalents.get(prop)!=null) related.addAll((HashSet) this.equivalents.get(prop));
				if (subProperties.get(prop)!=null) related.addAll((HashSet) this.subProperties.get(prop));
				if (superProperties.get(prop)!=null) related.addAll((HashSet) this.superProperties.get(prop));
				for (Iterator iter2 = related.iterator(); iter2.hasNext();) {
					OWLProperty relProp = (OWLProperty) iter2.next();
					// ensure same state (S2/S3/S4) of relProp and prop
					if (!currentState.equals("S2") && checkState(relProp, STATE_L12))  {
						if (DEBUG) System.out.println("Moving ObjectProperty " +getName(prop) + " "+currentState+"->S2 because related property "+ getName(relProp) + " is in S2");
						out.write("Moving ObjectProperty " +getName(prop) + " "+currentState+"->S2 because related property "+ getName(relProp) + " is in S2 <br>");
						changed = this.makeStateChange(prop, STATE_L12, removeFrom, this.ObjPropS2);								
					}
					else if (!currentState.equals("S3") && checkState(relProp, STATE_L21))  {
						if (DEBUG) System.out.println("Moving ObjectProperty " +getName(prop) + " "+currentState+"->S3 because related property "+ getName(relProp) + " is in S3");
						out.write("Moving ObjectProperty " +getName(prop) + " "+currentState+"->S3 because related property "+ getName(relProp) + " is in S3 <br>");
						changed = this.makeStateChange(prop, STATE_L21, removeFrom, this.ObjPropS3);								
					}
					else if (!currentState.equals("S4") && checkState(relProp, STATE_O2))  {
						if (DEBUG) System.out.println("Moving ObjectProperty " +getName(prop) + " "+currentState+"->S4 because related property "+ getName(relProp) + " is in S4");
						out.write("Moving ObjectProperty " +getName(prop) + " "+currentState+"->S4 because related property "+ getName(relProp) + " is in S4 <br>");
						changed = this.makeStateChange(prop, STATE_O2, removeFrom, this.ObjPropS4);								
					}
					if (changed) break;
				}
				
				//*** check if P is in S1, then..
				if (!changed && checkState(prop, STATE_O1)) {
					
					//**check if dom(P,C) and C is in O2, move P to S3
					for (Iterator iter2 = prop.getDomains(source).iterator(); iter2.hasNext();) {
						OWLDescription desc = (OWLDescription) iter2.next();
						if (checkState(desc, STATE_O2)) {
							if (DEBUG) System.out.println("Moving ObjectProperty " +getName(prop) + " S1->S3 because DOMAIN(P,C), where C is "+ getName(desc) + " and C has moved");
							out.write("Moving ObjectProperty " +getName(prop) + " S1->S3 because DOMAIN(P,C), where C is "+ getName(desc) + " and C has moved <br>");
							changed = this.makeStateChange(prop, STATE_L21, this.ObjPropS1, this.ObjPropS3);
							break;
						}
					}
					
					//**check if range(P,C) and C is in O2, move P to S2
					if (!changed) {						
						for (Iterator iter2 = prop.getRanges(source).iterator(); iter2.hasNext();) {
							OWLDescription desc = (OWLDescription) iter2.next();
							if (checkState(desc, STATE_O2)) {
								if (DEBUG) System.out.println("Moving ObjectProperty " +getName(prop) + " S1->S2 because RANGE(P,C), where C is "+ getName(desc) + " and C has moved");
								out.write("Moving ObjectProperty " +getName(prop) + " S1->S2 because RANGE(P,C), where C is "+ getName(desc) + " and C has moved <br>");
								changed = this.makeStateChange(prop, STATE_L12, this.ObjPropS1, this.ObjPropS2);
								break;
							}
						}
					}
					
					//** check if P(a,b) and a in O2, move to S3
					if (!changed && this.propToIndividualAssertions.get(prop)!=null) {
						for (Iterator iter2 = ((HashSet) this.propToIndividualAssertions.get(prop)).iterator(); iter2.hasNext();) {
							OWLIndividual ind = (OWLIndividual) iter2.next();
							if (checkState(ind, STATE_O2)) {
								if (DEBUG) System.out.println("Moving ObjectProperty " +getName(prop) + " S1->S3 because P(a,b), where a is "+ getName(ind) + " and a has moved");
								out.write("Moving ObjectProperty " +getName(prop) + " S1->S3 because P(a,b), where a is "+ getName(ind) + " and a has moved <br>");
								changed = this.makeStateChange(prop, STATE_L21, this.ObjPropS1, this.ObjPropS3);
								break;
							}
							//** also check if P(a,b) and b in O2, move to S2
							Map oValues = ind.getObjectPropertyValues(source);
							for (Iterator iter3=((HashSet) oValues.get(prop)).iterator(); iter3.hasNext();) {
								OWLIndividual valueInd = (OWLIndividual) iter3.next();
								if (checkState(valueInd, STATE_O2)) {
									if (DEBUG) System.out.println("Moving ObjectProperty " +getName(prop) + " S1->S2 because P(a,b), where b is "+ getName(valueInd) + " and b has moved");
									out.write("Moving ObjectProperty " +getName(prop) + " S1->S2 because P(a,b), where b is "+ getName(valueInd) + " and b has moved <br>");
									changed = this.makeStateChange(prop, STATE_L12, this.ObjPropS1, this.ObjPropS2);
									break;
								}
							}
						}
					}					
				}
				
				//*** check if P is in state S2, then..
				if (!changed && checkState(prop, STATE_L12)) {
					
					//** check symmetric/transitive
					if (prop.isSymmetric(source) || prop.isTransitive(source)) {
						if (DEBUG) System.out.println("Moving ObjectProperty " +getName(prop) + " S2->S4 because its SYMMETRIC/TRANSITIVE");
						out.write("Moving ObjectProperty " +getName(prop) + " S2->S4 because its SYMMETRIC/TRANSITIVE <br>");
						changed = this.makeStateChange(prop, STATE_O2, this.ObjPropS2, this.ObjPropS4);
						break;
					}
					
					//** check if dom(P,C) and C is in O2, move P to S4
					if (!changed) {						
						for (Iterator iter2 = prop.getDomains(source).iterator(); iter2.hasNext();) {
							OWLDescription desc = (OWLDescription) iter2.next();
							if (checkState(desc, STATE_O2)) {
								if (DEBUG) System.out.println("Moving ObjectProperty " +getName(prop) + " S2->S4 because DOMAIN(P,C), where C is "+getName(desc) + " and C has moved");
								out.write("Moving ObjectProperty " +getName(prop) + " S2->S4 because DOMAIN(P,C), where C is "+getName(desc) + " and C has moved <br>");
								changed = this.makeStateChange(prop, STATE_O2, this.ObjPropS2, this.ObjPropS4);
								break;
							}
						}	
					}
					
					//** check if P(a,b) and a in O2, move to S4
					if (!changed && this.propToIndividualAssertions.get(prop)!=null) {
						for (Iterator iter2 = ((HashSet) this.propToIndividualAssertions.get(prop)).iterator(); iter2.hasNext();) {
							OWLIndividual ind = (OWLIndividual) iter2.next();
							if (checkState(ind, STATE_O2)) {
								if (DEBUG) System.out.println("Moving ObjectProperty " +getName(prop) + " S2->S4 because P(a,b), where a is "+getName(ind) + " and a has moved");
								out.write("Moving ObjectProperty " +getName(prop) + " S2->S4 because P(a,b), where a is "+getName(ind) + " and a has moved <br>");
								changed = this.makeStateChange(prop, STATE_O2, this.ObjPropS2, this.ObjPropS4);
								break;
							}
						}
					}					
				}
				
				// if P is in state S3, then..
				if (!changed && checkState(prop, STATE_L21)) {
					
					//** check symmetric/transitive
					if (prop.isSymmetric(source) || prop.isTransitive(source)) {
						if (DEBUG) System.out.println("Moving ObjectProperty " +getName(prop) + " S3->S4 because its SYMMETRIC/TRANSITIVE");
						out.write("Moving ObjectProperty " +getName(prop) + " S3->S4 because its SYMMETRIC/TRANSITIVE <br>");
						changed = this.makeStateChange(prop, STATE_O2, this.ObjPropS3, this.ObjPropS4);
						break;
					}
					
					//** check if range(P,C) and C is in O2, move P to S4
					for (Iterator iter2 = prop.getRanges(source).iterator(); iter2.hasNext();) {
							OWLDescription desc = (OWLDescription) iter2.next();
							if (checkState(desc, STATE_O2)) {
								if (DEBUG) System.out.println("Moving ObjectProperty " +getName(prop) + " S3->S4 because RANGE(P,C), where C is "+getName(desc) + " and C has moved");
								out.write("Moving ObjectProperty " +getName(prop) + " S3->S4 because RANGE(P,C), where C is "+getName(desc) + " and C has moved <br>");
								changed = this.makeStateChange(prop, STATE_O2, this.ObjPropS3, this.ObjPropS4);
								break;
							}
						}
					
					//** check if P(a,b) and b in O2, move to S4
					if (this.propToIndividualAssertions.get(prop)!=null) {
						for (Iterator iter2 = ((HashSet) this.propToIndividualAssertions.get(prop)).iterator(); iter2.hasNext();) {
							OWLIndividual ind = (OWLIndividual) iter2.next();							
							Map oValues = ind.getObjectPropertyValues(source);
							for (Iterator iter3=((HashSet) oValues.get(prop)).iterator(); iter3.hasNext();) {
								OWLIndividual valueInd = (OWLIndividual) iter3.next();
								if (checkState(valueInd, STATE_O2)) {
									if (DEBUG) System.out.println("Moving ObjectProperty " +getName(prop) + " S3->S4 because P(a,b), where b is "+getName(valueInd) + " and b has moved");
									out.write("Moving ObjectProperty " +getName(prop) + " S3->S4 because P(a,b), where b is "+getName(valueInd) + " and b has moved <br>");
									changed = this.makeStateChange(prop, STATE_O2, this.ObjPropS3, this.ObjPropS4);
									break;
								}
							}
						}
					}
				}
				
				//*** check for all restrictions involving P
				if (!changed && this.propToDescriptionRestrictions.get(prop)!=null) {
					for (Iterator iter2 = ((HashSet) this.propToDescriptionRestrictions.get(prop)).iterator(); iter2.hasNext();) {
						OWLObjectRestriction rest = (OWLObjectRestriction) iter2.next();
						
						// if restriction itself is in O2
						if (checkState(rest, STATE_O2)) {
							// if P is in S1 move P to S3
							if (checkState(prop, STATE_O1)) {
								if (DEBUG) System.out.println("Moving ObjectProperty " +getName(prop) + " S1->S3 because its IN A RESTRICTION which itself has moved");
								out.write("Moving ObjectProperty " +getName(prop) + " S1->S3 because its IN A RESTRICTION which itself has moved <br>");
								changed = this.makeStateChange(prop, STATE_L21, this.ObjPropS1, this.ObjPropS3);
								break;
							}
							// if P is in S2 move P to S4
							if (checkState(prop, STATE_L12)) {
								if (DEBUG) System.out.println("Moving ObjectProperty " +getName(prop) + " S2->S4 because its IN A RESTRICTION which itself has moved");
								out.write("Moving ObjectProperty " +getName(prop) + " S2->S4 because its IN A RESTRICTION which itself has moved <br>");
								changed = this.makeStateChange(prop, STATE_O2, this.ObjPropS2, this.ObjPropS4);
								break;
							}
						}
						
						// if C in restriction is in O2
						if (rest instanceof OWLObjectQuantifiedRestriction) {
							OWLDescription desc = ((OWLObjectQuantifiedRestriction) rest).getDescription();
							if (checkState(desc, STATE_O2)) {							
								// if P is in S1 move P to S2
								if (checkState(prop, STATE_O1)) {
									if (DEBUG) System.out.println("Moving ObjectProperty " +getName(prop) + " S1->S2 because its IN A (SOME/ALL) RESTRICTION, where C is " +getName(desc)+" and C has moved");
									out.write("Moving ObjectProperty " +getName(prop) + " S1->S2 because its IN A (SOME/ALL) RESTRICTION, where C is " +getName(desc)+" and C has moved <br>");
									changed = this.makeStateChange(prop, STATE_L12, this.ObjPropS1, this.ObjPropS2);
									break;
								}
								// if P is in S3 move P to S4
								if (checkState(prop, STATE_L21)) {
									if (DEBUG) System.out.println("Moving ObjectProperty " +getName(prop) + " S3->S4 because its IN A RESTRICTION, where C is " +getName(desc)+" and C has moved");
									out.write("Moving ObjectProperty " +getName(prop) + " S3->S4 because its IN A RESTRICTION, where C is " +getName(desc)+" and C has moved <br>");
									changed = this.makeStateChange(prop, STATE_O2, this.ObjPropS3, this.ObjPropS4);
									break;
								}
							}
						}
					}
				}
				
				//*** check for inverses 
				if(!changed ){
				for (Iterator iter2 = SetUtils.union(reasoner.inversePropertiesOf(prop)).iterator(); iter2.hasNext();) {
						OWLObjectProperty invProp = (OWLObjectProperty) iter2.next();
						
						// if P in S1,..
						if (checkState(prop, STATE_O1)) {
							// if Q in S2, move P to S3
							if (checkState(invProp, STATE_L12)) {
								if (DEBUG) System.out.println("Moving ObjectProperty " +getName(prop) + " S1->S3 because its INVERSE " +getName(invProp)+" is in S2");
								out.write("Moving ObjectProperty " +getName(prop) + " S1->S3 because its INVERSE " +getName(invProp)+" is in S2 <br>");
								changed = this.makeStateChange(prop, STATE_L21, this.ObjPropS1, this.ObjPropS3);
								break;
							}
							else
						    // if Q in S3, move P to S2
						    if (checkState(invProp, STATE_L21)) {
						    	if (DEBUG) System.out.println("Moving ObjectProperty " +getName(prop) + " S1->S2 because its INVERSE " +getName(invProp)+" is in S3");
						    	out.write("Moving ObjectProperty " +getName(prop) + " S1->S2 because its INVERSE " +getName(invProp)+" is in S3 <br>");
						    	changed = this.makeStateChange(prop, STATE_L12, this.ObjPropS1, this.ObjPropS2);
						    	break;
						    }
						    else {// inverse is in S4, so move P to S4
						    	if (checkState(invProp, STATE_O2)) {
							    	if (DEBUG) System.out.println("Moving ObjectProperty " +getName(prop) + " "+currentState+"->S4 because its INVERSE " +getName(invProp)+" is in S4");
							    	out.write("Moving ObjectProperty " +getName(prop) + " "+currentState+"->S4 because its INVERSE " +getName(invProp)+" is in S4 <br>");
							    	changed = this.makeStateChange(prop, STATE_O2, removeFrom, this.ObjPropS4);						    	
							    	break;
							    }
						    }
						}
						if(checkState(prop,STATE_L12)){
							//if prop is in L12 and its inverse also, move prop to 4
							if(checkState(invProp,STATE_L12)){
								if (DEBUG) System.out.println("Moving ObjectProperty " +getName(prop) + " "+currentState+"->S4 because it is in L_12 and its INVERSE " +getName(invProp)+" also");
						    	out.write("Moving ObjectProperty " +getName(prop) + " "+currentState+"->S4 because it is in L_12 and its  INVERSE " +getName(invProp)+" also <br>");
						    	changed = this.makeStateChange(prop, STATE_O2, removeFrom, this.ObjPropS4);						    	
						    	break;
						  }
							
							if (checkState(invProp, STATE_O2)) {
						    	if (DEBUG) System.out.println("Moving ObjectProperty " +getName(prop) + " "+currentState+"->S4 because its INVERSE " +getName(invProp)+" is in S4");
						    	out.write("Moving ObjectProperty " +getName(prop) + " "+currentState+"->S4 because its INVERSE " +getName(invProp)+" is in S4 <br>");
						    	changed = this.makeStateChange(prop, STATE_O2, removeFrom, this.ObjPropS4);						    	
						    	break;
						    }
						    
						}
						if(checkState(prop,STATE_L21)){
							//if prop is in L21 and its inverse also, move prop to 4
							if(checkState(invProp,STATE_L21)){
								if (DEBUG) System.out.println("Moving ObjectProperty " +getName(prop) + " "+currentState+"->S4 because it is in L_21 and its INVERSE " +getName(invProp)+" also");
						    	out.write("Moving ObjectProperty " +getName(prop) + " "+currentState+"->S4 because it is in L_21 and its  INVERSE " +getName(invProp)+" also <br>");
						    	changed = this.makeStateChange(prop, STATE_O2, removeFrom, this.ObjPropS4);						    	
						    	break;
						  }
							
							if (checkState(invProp, STATE_O2)) {
						    	if (DEBUG) System.out.println("Moving ObjectProperty " +getName(prop) + " "+currentState+"->S4 because its INVERSE " +getName(invProp)+" is in S4");
						    	out.write("Moving ObjectProperty " +getName(prop) + " "+currentState+"->S4 because its INVERSE " +getName(invProp)+" is in S4 <br>");
						    	changed = this.makeStateChange(prop, STATE_O2, removeFrom, this.ObjPropS4);						    	
						    	break;
						    }
						    
						}
												
					}
				}
			} // end object properties check
			changed = changed | copyChanged;
			timers.stop();
		}
		while (changed);
		
		if (DEBUG) System.out.println("Completed state machine.."+swoopModel.getTimeStamp());
		System.out.println("----Timers for state machine iterations----");
		System.out.println(timers.toString());
		System.out.println(timers2.toString());
		System.out.println(timers4.toString());
		System.out.println(timers8.toString());
		System.out.println(timers9.toString());
			
	}
	

	
	public void makeChanges() throws OWLException{
		
		if (DEBUG) System.out.println("Starting makeChanges().."+swoopModel.getTimeStamp());
		
		//***********************************************************
		//Classes and Descriptions in O2
		//***********************************************************
		for(Iterator iter = DescriptionsO2.iterator(); iter.hasNext(); ){
		
			OWLDescription d = (OWLDescription)iter.next();
			//Add entity to Target
			if(d instanceof OWLClass){
				OntologyChange oc = new AddEntity(target,(OWLClass)d,null);
				changes.add(oc);
				if(APPLY) oc.accept((ChangeVisitor)target);
			}
			//handle class equivalence axioms
			if(equivalents.containsKey(d)){
				for(Iterator it = ((Set)equivalents.get(d)).iterator(); it.hasNext();){
					if(d instanceof OWLClass){
						OWLDescription equiv = (OWLDescription)it.next();
						OntologyChange oc5 = new AddEquivalentClass(target,(OWLClass)d,equiv,null);
						changes.add(oc5);
					    if(APPLY) oc5.accept((ChangeVisitor)target);
						OntologyChange oc16 = new RemoveEquivalentClass(source,(OWLClass)d,equiv,null);
					    changes.add(oc16);
					    if(APPLY) oc16.accept((ChangeVisitor)source);
							 
			
					}
					else{
						OWLClass equiv = (OWLClass)it.next();
						OntologyChange oc5 = new AddEquivalentClass(target,(OWLClass)equiv,d,null);
						changes.add(oc5);
					    if(APPLY) oc5.accept((ChangeVisitor)target);
					    OntologyChange oc16 = new RemoveEquivalentClass(source,(OWLClass)equiv,d,null);
					    changes.add(oc16);
					    if(APPLY) oc16.accept((ChangeVisitor)source);
							
						}
				}
			}
			//handle subClassAxioms
			if(subClasses.containsKey(d) && d instanceof OWLClass){
				for(Iterator it = ((Set)superClasses.get(d)).iterator(); it.hasNext(); ){
					OWLDescription sup = (OWLDescription)it.next();
					OntologyChange oc5 = new AddSuperClass(target,(OWLClass)d,sup,null);
					changes.add(oc5);
					if(APPLY) oc5.accept((ChangeVisitor)target);
					OntologyChange oc16 = new RemoveSuperClass(source,(OWLClass)d,sup,null);
				    changes.add(oc16);
				    if(APPLY) oc16.accept((ChangeVisitor)source);								
				}
			}
			if(d instanceof OWLClass){
				for(Iterator j = ((OWLClass)d).getEnumerations(source).iterator(); j.hasNext(); ){
					OWLEnumeration enum_ = (OWLEnumeration)(j.next());
					for(Iterator u = enum_.getIndividuals().iterator(); u.hasNext(); ){
				  		 OWLIndividual indiv = (OWLIndividual)u.next();
				  		 EnumElementChange change = new EnumElementChange("Add", target, (OWLClass) d, indiv, null);
						 changes.add(change);
						 if(APPLY) change.accept((ChangeVisitor)target);						 
							
							
				  	}
				}
			}
			
		}//end loop for classes
		
		//****************************************
		//Individuals in O2
		//****************************************
		for(Iterator iter2 = IndividualsO2.iterator(); iter2.hasNext(); )
		{
			OWLIndividual ind = (OWLIndividual)iter2.next();
			OntologyChange oc = new AddEntity(target,ind,null);
			changes.add(oc);
			if(APPLY) oc.accept((ChangeVisitor)target);
			//Assertions R(a,b), where ``a'' has been moved
			for(Iterator it = ind.getObjectPropertyValues(source).keySet().iterator(); it.hasNext(); ){
				OWLObjectProperty prop = (OWLObjectProperty)it.next();
				Set copytypes = new HashSet((Set)ind.getObjectPropertyValues(source).get(prop));
				
				for(Iterator i = copytypes.iterator(); i.hasNext(); ){
			    	 OWLIndividual b = (OWLIndividual)i.next();
			    	 OntologyChange oc3 = new AddObjectPropertyInstance(target,ind,prop,b,null);
			       	 changes.add(oc3);
			       	 if(APPLY) oc3.accept((ChangeVisitor)target);
			  	     OntologyChange oc2 = new RemoveObjectPropertyInstance(source,ind,prop,b,null);
			       	 changes.add(oc2);
			       	 if(APPLY) oc2.accept((ChangeVisitor)source);			  	    
			    }
			}
			//Assertions of the form U(a,d)
			for(Iterator it3 = ind.getDataPropertyValues(source).keySet().iterator(); it3.hasNext(); ){
				OWLDataProperty prop = (OWLDataProperty)it3.next();
			    for(Iterator i = ((Set)ind.getDataPropertyValues(source).get(prop)).iterator(); i.hasNext();){
			    	 OWLDataValue b = (OWLDataValue)i.next();
			    	 OntologyChange oc3 = new AddDataPropertyInstance(target,ind,prop,b,null);
			       	 changes.add(oc3);
			       	 if(APPLY) oc3.accept((ChangeVisitor)target);
			  	     OntologyChange oc2 = new RemoveDataPropertyInstance(source,ind,prop,b,null);
			       	 changes.add(oc2);
			       	 if(APPLY) oc2.accept((ChangeVisitor)source);			  	    
			    }
			}
			//Assertions of the form C(a)
			// AK: need to create a copy of types to avoid concurrent modification exception
			// because you are iterating over types in source, while deleting them inside the loop
			Set copyTypes = new HashSet(ind.getTypes(source));
			for(Iterator it2= copyTypes.iterator(); it2.hasNext(); ){
				OWLDescription desc = (OWLDescription)it2.next();
				OntologyChange oc3 = new AddIndividualClass(target,ind,desc,null);
				changes.add(oc3);
				if(APPLY) oc3.accept((ChangeVisitor)target);
				OntologyChange oc2 = new RemoveIndividualClass(source,ind,desc,null);
				changes.add(oc2);
				if(APPLY) oc2.accept((ChangeVisitor)source);
					
				  
			}
		}
		//************************************************************
		//Datatype properties in O2
		//************************************************************
		for (Iterator iter3 = DataPropO2.iterator(); iter3.hasNext(); ){
			OWLDataProperty prop = (OWLDataProperty)iter3.next();
			//Add entity to Target=O2
			OntologyChange oc = new AddEntity(target,prop,null);
			changes.add(oc);
			if(APPLY) oc.accept((ChangeVisitor)target);
			//Copy property attributes
			if(prop.isFunctional(source)){
			    SetFunctional oc25 = new SetFunctional(source, prop, false, null);
		        changes.add(oc25);
		        if(APPLY) oc25.accept((ChangeVisitor)source);
				  
		        SetFunctional oc26 = new SetFunctional(target, prop, true, null);
		        changes.add(oc26);
		        if(APPLY) oc26.accept((ChangeVisitor)target);
		    }
			//domains
			Set copyTypes = new HashSet(prop.getDomains(source));
			for(Iterator it = copyTypes.iterator(); it.hasNext(); ){
				OWLDescription desc = (OWLDescription)it.next();
				OntologyChange oc3 = new AddDomain(target,prop,desc,null);
				changes.add(oc3);
				if(APPLY) oc3.accept((ChangeVisitor)target);
				OntologyChange oc2 = new RemoveDomain(source,prop,desc,null);
				changes.add(oc2);
				if(APPLY) oc2.accept((ChangeVisitor)source);
					
			}
			Set copytypes = new HashSet(prop.getRanges(source));
			for(Iterator it = copytypes.iterator(); it.hasNext(); ){
				OWLDataType dat = (OWLDataType)it.next();
				//Add range axiom to Target
				OntologyChange oc3 = new AddDataPropertyRange(target,prop,dat,null);
				changes.add(oc3);
				//Remove range axiom from Source
				if(APPLY) oc3.accept((ChangeVisitor)target);
				OntologyChange oc2 = new RemoveDataPropertyRange(source,prop,dat,null);
				changes.add(oc2);
				if(APPLY) oc2.accept((ChangeVisitor)source);
					
			}
			
	
		}
		//****************************************************************************
		//Object Properties in State2: Links from source-->Target
		//*****************************************************************************
		for(Iterator iter9 = ObjPropS2.iterator(); iter9.hasNext(); ){
			 OWLObjectProperty prop = (OWLObjectProperty)iter9.next();
			 OntologyChange oc = new SetLinkTarget(source,prop,target.getURI(),null);
		     changes.add(oc);
			 if(APPLY) oc.accept((ChangeVisitor)source);
			 
		}
		
		//******************************************************************************
		//Link properties in O2
		//*****************************************************************************
		for(Iterator iter4 = LinkPropO2.iterator(); iter4.hasNext(); ){
		    OWLObjectProperty prop = (OWLObjectProperty)iter4.next();
			OntologyChange oc = new AddEntity(target,prop,null);
			changes.add(oc);
			//Try
		//	OntologyChange oc3 = new SetLinkTarget(source,prop,null,null);
		//    changes.add(oc3);
		//	if(APPLY) oc3.accept((ChangeVisitor)source);
			//
			if(APPLY) oc.accept((ChangeVisitor)target);
			if (prop.isInverseFunctional(source)){
				SetInverseFunctional oc27 = new SetInverseFunctional(source, (OWLObjectProperty)prop, false, null);
			    changes.add(oc27);
			    if(APPLY) oc27.accept((ChangeVisitor)source);
				SetInverseFunctional oc28 = new SetInverseFunctional(target, (OWLObjectProperty)prop, true, null);
			    changes.add(oc28);
			    if(APPLY) oc28.accept((ChangeVisitor)target);
				  
			}
		    if(prop.isFunctional(source)){
		    	SetFunctional oc25 = new SetFunctional(source, prop, false, null);
		        changes.add(oc25);
		        if(APPLY) oc25.accept((ChangeVisitor)source);
		    	SetFunctional oc26 = new SetFunctional(target, prop, true, null);
		        changes.add(oc26);
		        if(APPLY) oc26.accept((ChangeVisitor)target);
		        
		    }
			for(Iterator it = prop.getDomains(source).iterator(); it.hasNext(); ){
				OWLDescription desc = (OWLDescription)it.next();
				OntologyChange oc76 = new AddDomain(target,prop,desc,null);
				changes.add(oc76);
				if(APPLY) oc76.accept((ChangeVisitor)target);
				OntologyChange oc2 = new RemoveDomain(source,prop,desc,null);
				changes.add(oc2);
				if(APPLY) oc2.accept((ChangeVisitor)source);
					
			}
			Set copytypes = new HashSet(prop.getRanges(source));
			for(Iterator it = copytypes.iterator(); it.hasNext(); ){
				OWLDescription desc = (OWLDescription)it.next();
				//Add range axiom to Target
				OntologyChange oc31 = new AddObjectPropertyRange(target,prop,desc,null);
				changes.add(oc31);
				//Remove range axiom from Source
				if(APPLY) oc31.accept((ChangeVisitor)target);
				OntologyChange oc2 = new RemoveObjectPropertyRange(source,prop,desc,null);
				changes.add(oc2);
				if(APPLY) oc2.accept((ChangeVisitor)source);
					
			}
			//Test
			copytypes = new HashSet(prop.getInverses(source));
			for(Iterator it = copytypes.iterator(); it.hasNext(); ){
			       OWLObjectProperty pinv = (OWLObjectProperty)it.next();
			       OntologyChange oc2 = new AddInverse(target,prop,pinv,null);
				   changes.add(oc2);
				   if(APPLY) oc2.accept((ChangeVisitor)target);
				   RemoveInverse change = new RemoveInverse(source, prop, pinv, null);
				   changes.add(change);
				   if(APPLY) change.accept((ChangeVisitor)source);
				   	  
				}
				
				
		}
        //*********************************************************************************** 
		//ObjectProperties in State 3: Link Properties Target-->Source
		//***********************************************************************************
		
		for(Iterator iter5 = ObjPropS3.iterator(); iter5.hasNext(); ){
			OWLObjectProperty prop = (OWLObjectProperty)iter5.next();
			OntologyChange oc = new AddEntity(target,prop,null);
			changes.add(oc);
			if(APPLY) oc.accept((ChangeVisitor)target);
		    OntologyChange oc6 = new SetLinkTarget(target,prop,source.getURI(),null);
			changes.add(oc6);
			if(APPLY) oc6.accept((ChangeVisitor)target);
			if (prop.isInverseFunctional(source)){
				SetInverseFunctional oc27 = new SetInverseFunctional(source, (OWLObjectProperty)prop, false, null);
			    changes.add(oc27);
			    if(APPLY) oc27.accept((ChangeVisitor)source);
			    SetInverseFunctional oc28 = new SetInverseFunctional(target, (OWLObjectProperty)prop, true, null);
			    changes.add(oc28);
			    if(APPLY) oc28.accept((ChangeVisitor)target);
			    
				  
			}
		    if(prop.isFunctional(source)){
		    	SetFunctional oc25 = new SetFunctional(source, prop, false, null);
		        changes.add(oc25);
		        if(APPLY) oc25.accept((ChangeVisitor)source);
		        SetFunctional oc26 = new SetFunctional(target, prop, true, null);
		        changes.add(oc26);
		        if(APPLY) oc26.accept((ChangeVisitor)target);
		        
		    }
		    Set copytypes = new HashSet(prop.getDomains(source));
			for(Iterator it = copytypes.iterator(); it.hasNext(); ){
				OWLDescription desc = (OWLDescription)it.next();
				OntologyChange oc3 = new AddDomain(target,prop,desc,null);
				changes.add(oc3);
				if(APPLY) oc3.accept((ChangeVisitor)target);
				OntologyChange oc2 = new RemoveDomain(source,prop,desc,null);
				changes.add(oc2);
				if(APPLY) oc2.accept((ChangeVisitor)source);				
			}
			copytypes = new HashSet(prop.getRanges(source));
			for(Iterator it = copytypes.iterator(); it.hasNext(); ){
				OWLDescription desc = (OWLDescription)it.next();
				//Add range axiom to Target
				OntologyChange oc3 = new AddObjectPropertyRange(target,prop,desc,null);
				changes.add(oc3);
				//Remove range axiom from Source
				if(APPLY) oc3.accept((ChangeVisitor)target);
				OntologyChange oc2 = new RemoveObjectPropertyRange(source,prop,desc,null);
				changes.add(oc2);
				if(APPLY) oc2.accept((ChangeVisitor)source);			    
			}
		
			copytypes = new HashSet(prop.getInverses(source));
			for(Iterator it = copytypes.iterator(); it.hasNext(); ){
		       OWLObjectProperty pinv = (OWLObjectProperty)it.next();
		       OntologyChange oc2 = new AddInverse(target,prop,pinv,null);
			   changes.add(oc2);
			   if(APPLY) oc2.accept((ChangeVisitor)target);
			   RemoveInverse change = new RemoveInverse(source, prop, pinv, null);
			   changes.add(change);
			   if(APPLY) change.accept((ChangeVisitor)source);
			   	  
			}
			
		}
		
		//**********************************************************************
		//ObjectProperties in State 4
		//**********************************************************************
		for(Iterator iter6 = ObjPropS4.iterator(); iter6.hasNext(); ){
			OWLObjectProperty prop = (OWLObjectProperty)iter6.next();
			OntologyChange oc = new AddEntity(target,prop,null);
			changes.add(oc);
			if(APPLY) oc.accept((ChangeVisitor)target);
			if (prop.isTransitive(source)){
				SetTransitive oc21 = new SetTransitive(source, (OWLObjectProperty)prop, false, null);
			    changes.add(oc21);
			    if(APPLY) oc21.accept((ChangeVisitor)source);
			    SetTransitive oc23 = new SetTransitive(target, (OWLObjectProperty)prop, true, null);
			    changes.add(oc23);
			    if(APPLY) oc23.accept((ChangeVisitor)target);
				  
			}
			if (prop.isSymmetric(source)){
				SetSymmetric oc22 = new SetSymmetric(source, (OWLObjectProperty)prop, false, null);
			    changes.add(oc22);
			    if(APPLY) oc22.accept((ChangeVisitor)source);
				  
			    SetSymmetric oc24 = new SetSymmetric(target, (OWLObjectProperty)prop, true, null);
			    changes.add(oc24);
			    if(APPLY) oc24.accept((ChangeVisitor)target);
				  
			}
			if (prop.isInverseFunctional(source)){
				SetInverseFunctional oc27 = new SetInverseFunctional(source, (OWLObjectProperty)prop, false, null);
			    changes.add(oc27);
			    if(APPLY) oc27.accept((ChangeVisitor)source);
				  
			    SetInverseFunctional oc28 = new SetInverseFunctional(target, (OWLObjectProperty)prop, true, null);
			    changes.add(oc28);
			    if(APPLY) oc28.accept((ChangeVisitor)target);
				  
			}
		    if(prop.isFunctional(source)){
			    SetFunctional oc25 = new SetFunctional(source, prop, false, null);
		        changes.add(oc25);
		        if(APPLY) oc25.accept((ChangeVisitor)source);
				  
		        SetFunctional oc26 = new SetFunctional(target, prop, true, null);
		        changes.add(oc26);
		        if(APPLY) oc26.accept((ChangeVisitor)target);
		    }
		    Set copytypes = new HashSet(prop.getDomains(source));
			for(Iterator it = copytypes.iterator(); it.hasNext(); ){
				OWLDescription desc = (OWLDescription)it.next();
				OntologyChange oc3 = new AddDomain(target,prop,desc,null);
				changes.add(oc3);
				if(APPLY) oc3.accept((ChangeVisitor)target);
				OntologyChange oc2 = new RemoveDomain(source,prop,desc,null);
				changes.add(oc2);
				if(APPLY) oc2.accept((ChangeVisitor)source);
			}
			copytypes = new HashSet(prop.getRanges(source));
			for(Iterator it = copytypes.iterator(); it.hasNext(); ){
				OWLDescription desc = (OWLDescription)it.next();
				//Add range axiom to Target
				OntologyChange oc3 = new AddObjectPropertyRange(target,prop,desc,null);
				changes.add(oc3);
				//Remove range axiom from Source
				if(APPLY) oc3.accept((ChangeVisitor)target);
				OntologyChange oc2 = new RemoveObjectPropertyRange(source,prop,desc,null);
				changes.add(oc2);
				if(APPLY) oc2.accept((ChangeVisitor)source);
			}
			
			for(Iterator it = SetUtils.union(reasoner.inversePropertiesOf(prop)).iterator(); it.hasNext(); ){
			       OWLObjectProperty pinv = (OWLObjectProperty)it.next();
			       OntologyChange oc2 = new AddInverse(target,prop,pinv,null);
				   changes.add(oc2);
				   if(APPLY) oc2.accept((ChangeVisitor)target);
				   RemoveInverse change = new RemoveInverse(source, prop, pinv, null);
				   changes.add(change);
				   if(APPLY) change.accept((ChangeVisitor)source);
				   	  
				}
			
		}
		
		//Loop over all told subProperties, superProperties, equivalentProperties
		//(This is a weird thing in the OWL-API)
		for(Iterator iter7 = subProperties.keySet().iterator(); iter7.hasNext(); ){
			OWLProperty prop = (OWLProperty)iter7.next();
			if(LinkPropO2.contains(prop)||ObjPropS3.contains(prop)||ObjPropS4.contains(prop)||DataPropO2.contains(prop)){
				for(Iterator it = ((Set)subProperties.get(prop)).iterator() ; it.hasNext() ;){
					OWLProperty sub = (OWLProperty)it.next();
					OntologyChange oc = new AddSuperProperty(target,sub,prop,null);
					changes.add(oc);
					if(APPLY) oc.accept((ChangeVisitor)target);
					OntologyChange oc2 = new RemoveSuperProperty(source,sub,prop,null);
					changes.add(oc2);
					if(APPLY) oc2.accept((ChangeVisitor)source);
				}
			}
		}
		
		for(Iterator iter8 = source.getPropertyAxioms().iterator(); iter8.hasNext(); ){
			OWLPropertyAxiom axiom = (OWLPropertyAxiom)iter8.next();
			if(axiom instanceof OWLSubPropertyAxiom){
				OWLProperty prop = (OWLProperty)((OWLSubPropertyAxiom)axiom).getSubProperty();
				if(LinkPropO2.contains(prop)||ObjPropS3.contains(prop)||ObjPropS4.contains(prop)||DataPropO2.contains(prop)){
					OntologyChange oc = new RemovePropertyAxiom(source,axiom,null);
					changes.add(oc);
					if(APPLY) oc.accept((ChangeVisitor)source);
				
				}		
			}
			if(axiom instanceof OWLEquivalentPropertiesAxiom){
				Set aux = ((OWLEquivalentPropertiesAxiom)axiom).getProperties();
				Iterator j = aux.iterator();
				OWLProperty prop = (OWLProperty)j.next();
				if(LinkPropO2.contains(prop)||ObjPropS3.contains(prop)||ObjPropS4.contains(prop)||DataPropO2.contains(prop)){
					OntologyChange oc2 = new AddPropertyAxiom(target,axiom,null);
					changes.add(oc2);
					if(APPLY) oc2.accept((ChangeVisitor)target);
					OntologyChange oc = new RemovePropertyAxiom(source,axiom,null);
					changes.add(oc);
					if(APPLY) oc.accept((ChangeVisitor)source);
				}
			}
		}//end looping for Property axioms
	
	//Disjointness axioms.
	for(Iterator i = disjointAxioms.iterator(); i.hasNext();){
		OWLDisjointClassesAxiom axiom = (OWLDisjointClassesAxiom)i.next();
		int count = 0;
		for(Iterator j = axiom.getDisjointClasses().iterator(); j.hasNext(); ){
			OWLDescription desc = (OWLDescription)j.next();
			if(DescriptionsO2.contains(desc)){
				count++;
			}
		}
		if(count == axiom.getDisjointClasses().size()){
			OntologyChange oc = new RemoveClassAxiom(source, axiom, null);
			changes.add(oc);
			if(APPLY) oc.accept((ChangeVisitor)source);
			OntologyChange oc2 = new AddClassAxiom(target, axiom, null);
			changes.add(oc2);
			if(APPLY) oc2.accept((ChangeVisitor)target);
		}
		else{
			if(count!=0){
				OntologyChange oc = new RemoveClassAxiom(source, axiom, null);
				changes.add(oc);
				if(APPLY) oc.accept((ChangeVisitor)source);
			}
		}
		
	}
	
//	Add the corresponding foreign entities to source and target
	for(Iterator i = addedForeignEntitiesInSource.iterator(); i.hasNext(); ){
   	   OWLEntity ent = (OWLEntity)i.next();
   	   OntologyChange oc = new AddForeignEntity(source,ent,target.getURI(),null);
   	   changes.add(oc);
   	   if(APPLY) oc.accept((ChangeVisitor)source);
   	}
   	
	for(Iterator i = addedForeignEntitiesInTarget.iterator(); i.hasNext(); ){
	   	   OWLEntity ent = (OWLEntity)i.next();
	   	   OntologyChange oc = new AddForeignEntity(target,ent,source.getURI(),null);
	   	   changes.add(oc);
	   	   if(APPLY) oc.accept((ChangeVisitor)target);
    }
	
	//Remove the corresponding foreign entities   	
	
	for(Iterator i = removedForeignEntitiesInSource.iterator(); i.hasNext(); ){
	   	   OWLEntity ent = (OWLEntity)i.next();
	   	   OntologyChange oc = new RemoveForeignEntity(source,ent,null);
	   	   changes.add(oc);
	   	   if(APPLY) oc.accept((ChangeVisitor)source);
    }
	
		
	 //Remove entities from SOURCE
		
		//Named Classes
		for(Iterator j = DescriptionsO2.iterator(); j.hasNext(); ){
			OWLDescription desc = (OWLDescription)j.next();
			if(desc instanceof OWLClass){
				OntologyChange oc = new RemoveEntity(source,(OWLClass)desc,null);
				changes.add(oc);
			    if(APPLY) oc.accept((ChangeVisitor)source);
			    if(DEBUG) System.out.println("Removed Class " + swoopModel.shortForm(((OWLClass)desc).getURI()) + " from SOURCE");
		
			}
		}
		//Individuals in O2
		for(Iterator u = IndividualsO2.iterator(); u.hasNext(); ){
			OWLIndividual ind = (OWLIndividual)u.next();
			OntologyChange oc = new RemoveEntity(source,ind,null);
			changes.add(oc);
		    if(APPLY) oc.accept((ChangeVisitor)source);
		  
		}
		
		//Datatype properties in O2
		for(Iterator j2 = DataPropO2.iterator(); j2.hasNext(); ){
			OWLDataProperty prop = (OWLDataProperty)j2.next();
			OntologyChange oc = new RemoveEntity(source,prop,null);
			changes.add(oc);
		    if(APPLY) oc.accept((ChangeVisitor)source);
	
		}
		//Link Properties in O2
		for(Iterator j3 = LinkPropO2.iterator(); j3.hasNext();){
			OWLObjectProperty prop = (OWLObjectProperty)j3.next();
			OntologyChange oc = new RemoveEntity(source,prop,null);
			changes.add(oc);
		    if(APPLY) oc.accept((ChangeVisitor)source);
		}
		//Object Properties in S3
		for(Iterator j4 = ObjPropS3.iterator(); j4.hasNext();){
			OWLObjectProperty prop = (OWLObjectProperty)j4.next();
			OntologyChange oc = new RemoveEntity(source,prop,null);
			changes.add(oc);
		    if(APPLY) oc.accept((ChangeVisitor)source);
		
		}
		//Object Properties in S4
		for(Iterator j5 = ObjPropS4.iterator(); j5.hasNext();){
			OWLObjectProperty prop = (OWLObjectProperty)j5.next();
			OntologyChange oc = new RemoveEntity(source,prop,null);
			changes.add(oc);
		    if(APPLY) oc.accept((ChangeVisitor)source);
		}
		
		// We also need to update the set of all descriptions left
		// For this, we check all descriptions in State O2 and remove them
		this.descriptionsLeft = new HashSet(this.DescriptionsO1);
		
		if (DEBUG) System.out.println("Completed moveChanges().."+swoopModel.getTimeStamp());
	}

	
	/**
	
	*/
	public void verifyLinks(List partitions) throws OWLException{
		for(Iterator j = partitions.iterator();j.hasNext(); ){
		//for(Iterator j = swoopModel.getOntologies().iterator();j.hasNext(); ){
			OWLOntology current = (OWLOntology)j.next();
			for(Iterator i = updatedLP.iterator(); i.hasNext();){
  			  		OWLObjectProperty prop = (OWLObjectProperty)i.next();
  			  		if(current.getLinkProperties().contains(prop)){
  			  			OntologyChange oc = new SetLinkTarget(current,prop,target.getURI(),null);
  			  			changes.add(oc);
  				   	    if(APPLY) oc.accept((ChangeVisitor)current);
  			  		}
		
  			  	}
		}
	}
	
	/** 
	 * Function for Computing the foreign entities to be added to the ontologies
	 * Also it updates the linkToSource and boundedInverses maps for subsequent partitioning steps
	 * @param prop
	 * @return
	 */

	
	 public void computeForeignEntities() throws OWLException{
		for(Iterator iter = ObjPropS2.iterator(); iter.hasNext(); ){
			OWLObjectProperty prop = (OWLObjectProperty)iter.next();
			//Restrictions
			if(propToDescriptionRestrictions.containsKey(prop)){
				for(Iterator j = ((Set)propToDescriptionRestrictions.get(prop)).iterator(); j.hasNext(); ){
					OWLRestriction res = (OWLRestriction)j.next();
					if(res instanceof OWLObjectQuantifiedRestriction){
		 				OWLDescription desc = ((OWLObjectQuantifiedRestriction)res).getDescription();
		 				addForeignEntities(source,desc,true,false);
		 			}
		 			if(res instanceof OWLObjectValueRestriction){
		 				OWLIndividual ind = ((OWLObjectValueRestriction)res).getIndividual();
		 				addedForeignEntitiesInSource.add(ind);
		 			}
		 		
				}
			}
			//Ranges
			for(Iterator iter2 = prop.getRanges(source).iterator(); iter2.hasNext(); ){
				OWLDescription desc = (OWLDescription)iter2.next();
				addForeignEntities(source,desc,true,false);
			}
			
			//ABox assertions
			if(propToIndividualAssertions.containsKey(prop)){
				for(Iterator ite = ((Set)propToIndividualAssertions.get(prop)).iterator(); ite.hasNext(); ){
					OWLIndividual a = (OWLIndividual)ite.next();
					for(Iterator k = ((Set)a.getObjectPropertyValues(source).get(prop)).iterator(); k.hasNext(); ){
						OWLIndividual b = (OWLIndividual)k.next();
						addedForeignEntitiesInSource.add(b);
					}
				}
			}
			
			//Compute bounded inverses and add foreign entities for inverses
			for(Iterator g = SetUtils.union(reasoner.inversePropertiesOf(prop)).iterator(); g.hasNext(); ){
 				OWLObjectProperty pinv = (OWLObjectProperty)g.next();
 				addedForeignEntitiesInSource.add(pinv);
 				Set aux = new HashSet();
 				if(!boundedInverses.containsKey(pinv)){
 					aux.add(prop);
 					boundedInverses.put(pinv,aux);
 				}
 				else{
 					aux = (Set)boundedInverses.get(pinv);
 					aux.add(prop);
 					boundedInverses.put(pinv,aux);
 	 			}
 			}
 	
			
		}
		
		for(Iterator itera = ObjPropS3.iterator(); itera.hasNext(); ){
			OWLObjectProperty prop = (OWLObjectProperty)itera.next();
			if(propToDescriptionRestrictions.containsKey(prop)){
				for(Iterator j = ((Set)propToDescriptionRestrictions.get(prop)).iterator(); j.hasNext(); ){
					OWLRestriction res = (OWLRestriction)j.next();
					if(res instanceof OWLObjectQuantifiedRestriction){
		 				OWLDescription desc = ((OWLObjectQuantifiedRestriction)res).getDescription();
		 				addForeignEntities(target,desc,true,false);
		 				addLinksToSource(desc,prop);
		 			
		 			}
		 			if(res instanceof OWLObjectValueRestriction){
		 				OWLIndividual ind = ((OWLObjectValueRestriction)res).getIndividual();
		 				addedForeignEntitiesInTarget.add(ind);
		 				//Set aux = (Set)linksToSource.get(prop);
		 				//aux.add(ind);
		 				//linksToSource.put(prop,aux);
		 			}
		 		}
			}
			for(Iterator iter2 = prop.getRanges(source).iterator(); iter2.hasNext(); ){
				OWLDescription desc = (OWLDescription)iter2.next();
				addForeignEntities(target,desc,true,false);
	 			addLinksToSource(desc,prop);
	 		
			}
			//Abox assertions
			if(propToIndividualAssertions.containsKey(prop)){
				for(Iterator ite = ((Set)propToIndividualAssertions.get(prop)).iterator(); ite.hasNext(); ){
					OWLIndividual a = (OWLIndividual)ite.next();
					for(Iterator k = ((Set)a.getObjectPropertyValues(source).get(prop)).iterator(); k.hasNext(); ){
						OWLIndividual b = (OWLIndividual)k.next();
						addedForeignEntitiesInTarget.add(b);
						//Test
						addLinksToSource(b,prop);
					}
				}
			}
			
			//Update bounded inverses and add foreign entities for inverses
			for(Iterator g = SetUtils.union(reasoner.inversePropertiesOf(prop)).iterator(); g.hasNext(); ){
 				OWLObjectProperty pinv = (OWLObjectProperty)g.next();
 				addedForeignEntitiesInTarget.add(pinv);
 				Set aux = new HashSet();
 	 			if(!boundedInverses.containsKey(prop)){
 					aux.add(pinv);
 					boundedInverses.put(prop,aux);
 				}
 				else{
 					aux = (Set)boundedInverses.get(prop);
 					aux.add(pinv);
 					boundedInverses.put(prop,aux);
 	 			}
 			}
			
		}
		
		for(Iterator iterat = LinkPropO2.iterator(); iterat.hasNext(); ){
			OWLObjectProperty prop = (OWLObjectProperty)iterat.next();
			if(propToDescriptionRestrictions.containsKey(prop)){
				for(Iterator j = ((Set)propToDescriptionRestrictions.get(prop)).iterator(); j.hasNext(); ){
					OWLRestriction res = (OWLRestriction)j.next();
					if(res instanceof OWLObjectQuantifiedRestriction){
		 				OWLDescription desc = ((OWLObjectQuantifiedRestriction)res).getDescription();
		 				addForeignEntities(target,desc,true,false);
		 				addForeignEntities(source,desc,true,true);
		 			}
		 			if(res instanceof OWLObjectValueRestriction){
		 				OWLIndividual ind = ((OWLObjectValueRestriction)res).getIndividual();
		 				removedForeignEntitiesInSource.add(ind);
		 				addedForeignEntitiesInTarget.add(ind);
		 			}
		 		}
			}
			for(Iterator iter2 = prop.getRanges(source).iterator(); iter2.hasNext(); ){
				OWLDescription desc = (OWLDescription)iter2.next();
				Set aux2 = new HashSet();
	 			addForeignEntities(target,desc,true,false);
	 			addForeignEntities(source,desc,true,true);
	 		}
			//Abox assertions
			if(propToIndividualAssertions.containsKey(prop)){
				for(Iterator ite = ((Set)propToIndividualAssertions.get(prop)).iterator(); ite.hasNext(); ){
					OWLIndividual a = (OWLIndividual)ite.next();
					for(Iterator k = ((Set)a.getObjectPropertyValues(source).get(prop)).iterator(); k.hasNext(); ){
						OWLIndividual b = (OWLIndividual)k.next();
						addedForeignEntitiesInTarget.add(b);
						removedForeignEntitiesInSource.add(b);
					}
				}
			}
		
		}
		
	 }
	 
	 
	/** 
	 * Simple function to add foreign entities to the appropriate sets. Called
	 * by ComputeForeignEntities
	 * @param prop
	 * @return
	 */

	private void addForeignEntities(OWLOntology onto, OWLDescription desc, boolean b, boolean remove) throws OWLException{
	  	if(desc instanceof OWLAnd|| desc instanceof OWLOr){
	  		Iterator it = ((OWLNaryBooleanDescription)desc).getOperands().iterator();
			while(it.hasNext()){
				addForeignEntities(onto,(OWLDescription)it.next(),b, remove);
			   }
	  	}
		if(desc instanceof OWLNot){
			addForeignEntities(onto,((OWLNot)desc).getOperand(),b, remove);
		}
		
		if(desc instanceof OWLEnumeration){
			for(Iterator iter = ((OWLEnumeration)desc).getIndividuals().iterator(); iter.hasNext(); ){
				OWLIndividual ind = (OWLIndividual)iter.next();
				if(onto.equals(source)&& b==true){
					addedForeignEntitiesInSource.add(ind);
					
				}
				if(onto.equals(target)&& b==true){
					addedForeignEntitiesInTarget.add(ind);
				}
				if(onto.equals(source) && remove == true){
					removedForeignEntitiesInSource.add(ind);
				}
			}
		}
		
		if(desc instanceof OWLClass){
			if(onto.equals(source)&& b==true){
				addedForeignEntitiesInSource.add(desc);
				
			}
			if(onto.equals(target)&& b==true){
				addedForeignEntitiesInTarget.add(desc);
			}
			if(onto.equals(source) && remove == true){
				removedForeignEntitiesInSource.add(desc);
			}
		}
	  
	  }
	 
	
	//ToDo: An equivalent to the verifyLinks function
	
	
	/** 
	 * Simple function to check if an OWLProperty is a Link Property or not
	 * Need to check if object property first and then call isLink()
	 * @param prop
	 * @return
	 */
	public boolean isLinkProperty(OWLProperty prop) {
		if (prop instanceof OWLObjectProperty && ((OWLObjectProperty) prop).isLink()) return true;
		else return false;
	}

	/**
	 * Check if an OWL Object is in the state specified
	 * @param obj
	 * @param state
	 * @return
	 */
	public boolean checkState(OWLObject obj, String state) {
		if (State.get(obj)!=null) {
			if (State.get(obj).toString().equals(state)) return true;
		}
		else {
			System.out.println("*******" + getName(obj)+" is in invalid state *********");
		}
		return false;
	}
	
	/**
	 * Called when desc is being moved to new state (O2)
	 * linksToSource.get(LP) returns a bounded set of classes
	 * check if OWLDescription desc is in that list and if any other class in that list has moved
	 * @param desc 
	 */
	public void moveBoundedEntities(OWLObject desc) {
		// iterate over keySet of linksToSource
		for (Iterator iter2=linksToSource.keySet().iterator(); iter2.hasNext();) {
			OWLProperty prop = (OWLProperty) iter2.next();
			Set boundedEntities = (HashSet) linksToSource.get(prop);
			if (boundedEntities.contains(desc)) {
				for (Iterator iter3=boundedEntities.iterator(); iter3.hasNext();) {
					OWLObject boundedDesc = (OWLObject) iter3.next();
					// move boundedDesc as well
					if (boundedDesc instanceof OWLDescription &&  !checkState(boundedDesc, STATE_O2)) {
						if (DEBUG) System.out.println("Moving "+getName(boundedDesc) + " because its bound to set by Link Property "+getName(prop));
						out.write("Moving "+getName(boundedDesc) + " because its bound to set by Link Property "+getName(prop) + "<br>");
						this.makeStateChangeForBounded(boundedDesc, STATE_O2, this.DescriptionsO1, this.DescriptionsO2);
						
					}
					else if (boundedDesc instanceof OWLIndividual && !checkState(boundedDesc, STATE_O2)){
						if (DEBUG) System.out.println("Moving "+getName(boundedDesc) + " because its bound to set by Link Property "+getName(prop));
						out.write("Moving "+getName(boundedDesc) + " because its bound to set by Link Property "+getName(prop)+ "<br>");
						this.makeStateChangeForBounded(boundedDesc, STATE_O2, this.IndividualsO1, this.IndividualsO2);
						
					}
				}
				// also update bounded link properties
				this.updatedLP.add(prop);												
			}
		}
		//We need to remove the elements of linksToSource that are
		//in updatedLP
		for(Iterator i = updatedLP.iterator(); i.hasNext(); ){
			OWLObjectProperty prop = (OWLObjectProperty)i.next();
			linksToSource.remove(prop);
		}
	}
	
	
	 public ArrayList getChanges(){
		return changes;
	}
	
	/**
	 * Whenever a state change needs to be made, call this method with object, new state
	 * and the sets which need to updated because of the state transition
	 * @param obj
	 * @param newState
	 * @param removeFromSet
	 * @param addToSet
	 * @return true 
	 */
	public boolean makeStateChange(OWLObject obj, String newState, Set removeFromSet, Set addToSet) {
		
		// check if owl:Thing is being moved..never move thing!
		if (obj instanceof OWLClass) {
			try {
				if (((OWLClass) obj).getURI().equals(owlThing.getURI())) {
//					System.out.println("owl:Thing Moved!!");
					return false;
				}
			} catch (OWLException e) {
				e.printStackTrace();
			}
			
		}
		
		if (obj instanceof OWLDescription|| obj instanceof OWLIndividual) {
			// whenever a description is being moved
			// we need to check and move all bounded classes
			moveBoundedEntities(obj);
		}
		
		
		State.put(obj, newState);
		removeFromSet.remove(obj);
		addToSet.add(obj);
		return true;
	}
	
	public boolean makeStateChangeForBounded(OWLObject obj, String newState, Set removeFromSet, Set addToSet) {
		
		State.put(obj, newState);
		removeFromSet.remove(obj);
		addToSet.add(obj);
		return true;
	}
	
	
	/**
	 * "A" type of check to see if property causes transition:
	 * Checks if prop is a Datatype/Link Property in O2
	 * or an ObjectProperty in S3/S4
	 * and returns true
	 * Used by:
	 * - domain check in classes
	 * - restriction check in classes
	 * - abox assertion R(a,b) check in individuals
	 * @param prop
	 * @return
	 */
	public boolean checkPropertyCausingTransitionA(OWLProperty prop) {
		if (prop instanceof OWLDataProperty || this.isLinkProperty(prop)) {
			if (checkState(prop, STATE_O2)) {
				return true;
			}
		}
		else {
			// prop is an ObjectProperty
			if (checkState(prop, STATE_L21) || checkState(prop, STATE_O2)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * "B" type of check to see if property causes transition:
	 * Checks if prop is an ObjectProperty in S2/S4
	 * and returns true
	 * Used by:
	 * - range check in classes 
	 * - abox assertion check R(b,a) in individuals 
	 * - hasValue(a,R) in individuals
	 */
	public boolean checkPropertyCausingTransitionB(OWLProperty prop) {
		if (!this.isLinkProperty(prop) && prop instanceof OWLObjectProperty && (checkState(prop, STATE_L12) || checkState(prop, STATE_O2))) 
			return true;
		else return false;
	}
	
	
	/**
	 * This function updates the linksToSource map for further partitioning steps
	 * Used by: computeForeignEntities
	*/
	private void addLinksToSource(OWLObject desc, OWLProperty prop) throws OWLException{
	  	
		if(desc instanceof OWLAnd|| desc instanceof OWLOr){
	  		Iterator it = ((OWLNaryBooleanDescription)desc).getOperands().iterator();
			while(it.hasNext()){
				Set aux = new HashSet();
				if(linksToSource.containsKey(prop)) aux.addAll((Set)linksToSource.get(prop));
				aux.add(desc);
				linksToSource.put(prop,aux);
				addLinksToSource((OWLDescription)it.next(),prop);				
			}
	  	}
		if(desc instanceof OWLNot){
			Set aux = new HashSet();
			if(linksToSource.containsKey(prop)) aux.addAll((Set)linksToSource.get(prop));
			aux.add(desc);
			linksToSource.put(prop,aux);
			addLinksToSource(((OWLNot)desc).getOperand(),prop);						  	
		}
		if(desc instanceof OWLClass|| desc instanceof OWLIndividual ){
			Set aux = new HashSet();
			if(linksToSource.containsKey(prop)) aux.addAll((Set)linksToSource.get(prop));
			aux.add(desc);
			linksToSource.put(prop,aux);			
		}
		
		
		
	}
	
	public void setDebug(boolean b){
		this.DEBUG = b;
	}
	
	public String getExpressivity(){
		return Expressivity;
	}
	
	public boolean getDebug(){
		return DEBUG;
	}
	
	public void setApply(boolean b){
		this.APPLY = b;
	}
	
	public boolean getApply(){
		return(this.APPLY);
	}
	
	public void setOutput(StringWriter out) {
	    this.out = out;
	}
	
	//public void setOutput(OutputStream out) {
	  //  setOutput(new OutputStreamWriter(out));
	//}
	
	public Map getLinksToSource(){
		return linksToSource;
	}
	
	public Map getUpdatedInverses(){
		return updatedInverses;
	}
	
	public Map getBoundedInverses(){
		return boundedInverses;
	}

	public Set getMovedClasses(){
	  	 return this.DescriptionsO2;
	  }
	
	public String getName(OWLObject obj) {
		try {
			if (obj instanceof OWLNamedObject) {
				return swoopModel.shortForm(((OWLNamedObject) obj).getURI());
			}
		}
		catch (OWLException ex) {
			ex.printStackTrace();
		}
		return "b-node";
	}
}
