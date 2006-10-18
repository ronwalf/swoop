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

package org.mindswap.swoop.reasoner;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.mindswap.swoop.utils.owlapi.CorrectedRDFRenderer;
import org.mindswap.swoop.utils.owlapi.OWLEntityRemover;
import org.semanticweb.owl.impl.model.OWLConnectionImpl;
import org.semanticweb.owl.io.owl_rdf.OWLRDFErrorHandler;
import org.semanticweb.owl.io.owl_rdf.OWLRDFParser;
import org.semanticweb.owl.model.OWLAnd;
import org.semanticweb.owl.model.OWLCardinalityRestriction;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLClassAxiom;
import org.semanticweb.owl.model.OWLDataValueRestriction;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDisjointClassesAxiom;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLObjectAllRestriction;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectQuantifiedRestriction;
import org.semanticweb.owl.model.OWLObjectSomeRestriction;
import org.semanticweb.owl.model.OWLObjectValueRestriction;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOr;
import org.semanticweb.owl.model.OWLProperty;
import org.semanticweb.owl.model.OWLRestriction;
import org.semanticweb.owl.model.change.ChangeVisitor;
import org.semanticweb.owl.model.change.RemoveClassAxiom;
import org.semanticweb.owl.model.change.RemoveEntity;
import org.semanticweb.owl.util.OWLConnection;
import org.xml.sax.SAXException;

/**
 * @author Aditya
 *
 */
public class DependencyReasoner {

	Set ontologies;
	OWLOntology ontology;
	Set unsat;
	
	boolean preprocessing = false;
	Set allPC; // set of all prop-value chains ending in universal on unsat class
	List propChain; // property-chain
	boolean lastPropAll = false; // if last prop in propChain is due to a allValuesRestriction
	boolean inUnion = false; // used to tag propChains as optional
	HashMap allPCMap;
	public HashMap dependencyMap;
	public List rootClasses, derivedClasses;
	int numSatTests;
	boolean DEBUG = false;
	SwoopRDFSReasoner rdfsReasoner;
	public HashMap childMap = new HashMap();
	
	public DependencyReasoner(Set ontologies, OWLOntology ontology, Set unsat) {
		this.ontologies = ontologies;
		this.ontology = ontology;
		this.unsat = unsat;
		this.rdfsReasoner = new SwoopRDFSReasoner();
		try {
			rdfsReasoner.setOntology(ontology);
		}
		catch (OWLException ex) {
			ex.printStackTrace();
		}
	}
	
	public void findDependencies() {
		
		/* initialize */
		allPCMap = new HashMap();
		dependencyMap = new HashMap();
		rootClasses = new ArrayList();
		derivedClasses = new ArrayList();
		
		/* for each unsat class in the ontology */
		for (Iterator iter = unsat.iterator(); iter.hasNext();) {
			
			OWLClass cla = (OWLClass) iter.next();
			
			/* Pre-processing - Stage 1 */
			preprocessing = true; /*** setting this global var is key ***/
			getAllPropValueChains((OWLClass) cla); // updates global var allPC
			allPCMap.put(cla, allPC);
			
			/* Extract dependencies - Stage 2 */
			preprocessing = false;
			Set dep = getDependency((OWLClass) cla);
			
			/* Post-processing - Stage 3 */
			
			// add final dependency set to hashmap
			dependencyMap.put(cla, dep);
			
			if (dep.size()==1) rootClasses.add(cla);
			else derivedClasses.add(cla);
		}
	}
	
	/*
	 * Mark mutually-dependent derived classes as potential roots
	 */
	public void mutualToRoot() {
		
		// make copy of dependencyMap
		HashMap prevDepMap = new HashMap(dependencyMap);
		
		for (Iterator iter = derivedClasses.iterator(); iter.hasNext();) {
			// get a derived class
			OWLClass derCla = (OWLClass) iter.next();
			// obtain its dependencies
			Set dep = (HashSet) this.dependencyMap.get(derCla);
			dep = this.unfoldSet(dep);
			// iterate and get parents of each dependency
			for (Iterator iter2 = new HashSet(dep).iterator(); iter2.hasNext();) {
				OWLClass parent = (OWLClass) iter2.next();
				if (!parent.equals(derCla)) {
					Set parDep = (HashSet) this.dependencyMap.get(parent);
					parDep = this.unfoldSet(parDep);
					// if parent has dependency equal to current derived class,
					// then both are mutually dependent, and so mark them as roots
					// need to remove them from dependency map
					if (parDep.contains(derCla)) {
						// mutually dependent roots found
						parDep.remove(derCla);
						dependencyMap.put(parent, parDep);
						dep.remove(parent);
						dependencyMap.put(derCla, dep);
					}
				}
			}			
		}
		// finally check dependency map again and move
		// all derived to roots that have only themselves as dependency
		for (Iterator iter = dependencyMap.keySet().iterator(); iter.hasNext();) {
			OWLClass cla = (OWLClass) iter.next();
			Set dep = (HashSet) dependencyMap.get(cla);
			dep = this.unfoldSet(dep);
			if (((dep.size()==1 && dep.contains(cla)) || dep.size()==0) && derivedClasses.contains(cla) && !rootClasses.contains(cla)) {
				// check previous dependency map
				// for each parent, see if now parent has 0 dependencies
				// then make current derived
				Set prevDep = (HashSet) prevDepMap.get(cla);
				prevDep = this.unfoldSet(prevDep);
				boolean stillHasParent = false;
				for (Iterator iter2=prevDep.iterator(); iter2.hasNext();) {
					// get old parent
					OWLClass par = (OWLClass) iter2.next();
					if (!par.equals(cla)) {
						// see its dependencies in new dependencyMap
						Set parDep = (HashSet) this.dependencyMap.get(par);
						parDep = this.unfoldSet(parDep);
						if (parDep.size()>1 || (parDep.size()==1 && !parDep.contains(par))) {
							stillHasParent = true;
							Set newDep = new HashSet();
							if (dependencyMap.get(cla)!=null) newDep = (HashSet) dependencyMap.get(cla);
							newDep.add(par);
							dependencyMap.put(cla, newDep);
						}
					}
				}
				if (!stillHasParent) {
					derivedClasses.remove(cla);
					rootClasses.add(cla);
				}
			}
		}
	}
	
	public void findAllRoots() {
		
		System.out.println("Finding roots using (optimized) brute-force...");
		
		// store all roots here
		Set allRoots = new HashSet();
		// create a copy of original ontology
		OWLOntology copy = this.cloneOntology(ontology);
		// create bkup which gets modified after each iteration
		OWLOntology bkup = this.cloneOntology(copy);
		// also count no. of sat. tests made during this process
		numSatTests = 0;
		OWLClass root = null;
		do {
			// call pruneRoots to find roots in one iteration
			// note: copy, roots gets modified inside pruneRoots
			System.out.println("Testing potential roots: "+rootClasses.size());
			root = pruneRoot(copy, new ArrayList(rootClasses));
			
			if (root==null) {
				// add remaining potential rootClasses to derived
				// also compute its dependency set
				for (Iterator iter = rootClasses.iterator(); iter.hasNext();) {
					OWLClass left = (OWLClass) iter.next();
					Set dep = new HashSet(allRoots);
					dependencyMap.put(left, dep);
				}
				derivedClasses.addAll(rootClasses);
				break;
			}
			else {
				// add roots to allRoots
				allRoots.add(root);
				
				// remove roots from rootClasses before next iteration
				rootClasses.remove(root);
				
				// remove roots from ontology
				System.out.println("Found root:" +getName(root));
				OWLEntity entity = null;
				try {
					entity = bkup.getClass(root.getURI());
//					OWLEntityRemover rem = new OWLEntityRemover(bkup);
//					rem.removeEntity(entity);
					RemoveEntity change = new RemoveEntity(bkup, entity, null);
	        		change.accept((ChangeVisitor) bkup);
				} 
				catch (OWLException e) {
					e.printStackTrace();
				}									
				copy = this.cloneOntology(bkup);
			}
		}
		while (root!=null);
		
		// finally make rootClasses equal to allRoots
		rootClasses = new ArrayList(allRoots);
	}
	
	/*
	 * Detect inferred dependency using ontology approximation technique
	 * Remove causes of contradictions and use reasoner to find hidden
	 * equivalence or subsumption
	 */
	public void infDepOntApprox() {
		
		System.out.println("Finding roots using ontology approximation...");
		try {
//			OWLBuilder builder = new OWLBuilder();
//			builder.createOntology(new URI("test"), new URI("test"));
//			OWLOntology newOnt = builder.getOntology();
//			for (Iterator iter = rootClasses.iterator(); iter.hasNext();) {
//				OWLClass root = (OWLClass) iter.next();
//				AddEntity ae = new AddEntity(newOnt, root, null);
//				ae.accept((ChangeVisitor) newOnt);
//			}
			OWLOntology newOnt = this.cloneOntology(ontology);
			System.out.println("ontology cloned...");
			for (Iterator iter = new HashSet(newOnt.getClasses()).iterator(); iter.hasNext();) {
				OWLClass cla = (OWLClass) iter.next();
				if (!rootClasses.contains(cla)) {
					OWLEntityRemover rem = new OWLEntityRemover(newOnt);
					rem.removeEntity(cla);
				}
			}
			System.out.println("all entities removed...");
		
			// now check disjoint axioms, note classes in disjoints
			// remove axiom and then check equivalence
			Set potRoots = new HashSet();
			for (Iterator iter = new HashSet(newOnt.getClassAxioms()).iterator(); iter.hasNext();) {
				OWLClassAxiom axiom = (OWLClassAxiom) iter.next();
				if (axiom instanceof OWLDisjointClassesAxiom) {
					OWLDisjointClassesAxiom dis = (OWLDisjointClassesAxiom) axiom;
					// check that dis contains atleast one root, otherwise dont remove it
					boolean check = false;
					for (Iterator iter2 = rootClasses.iterator(); iter2.hasNext();) {
						OWLClass root = (OWLClass) iter2.next();
						if (dis.getDisjointClasses().contains(root)) {
							check = true;
							break;
						}
					}
					if (check) {
						potRoots.addAll(dis.getDisjointClasses());
						// remove disjoint axiom
						RemoveClassAxiom rem = new RemoveClassAxiom(newOnt, dis, null);
						rem.accept((ChangeVisitor) newOnt);
					}
				}
			}
			if (!potRoots.isEmpty()) {
				PelletReasoner pellet = new PelletReasoner();
				pellet.setOntology(newOnt, false);
				for (Iterator iter = new HashSet(rootClasses).iterator(); iter.hasNext();) {
					OWLClass root = (OWLClass) iter.next();
					if (!potRoots.contains(root) && pellet.isConsistent(root)) {
						rootClasses.remove(root);
						derivedClasses.add(root);
						Set dep = new HashSet();
						dep.addAll(potRoots);
						dep.add("?");
						dependencyMap.put(root, dep);
					}
				}
			}
//			CorrectedRDFRenderer rdfRend = new CorrectedRDFRenderer();
//			StringWriter st = new StringWriter();
//			rdfRend.renderOntology(newOnt, st);
//			System.out.println(st.toString());
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Prune out all actual *root* classes using a brute force algorithm
	 * Hidden Dependencies between roots may exist because of an inferred
	 * subsumption or equivalence with another root 
	 */
	public OWLClass pruneRoot(OWLOntology ont, List potRoots) {
		
		// order pot-roots, remove pot-root and check sat. of remaining
		// if all remaining become sat., then this pot-root is root
		// all the rest are dependent on it
		try {
			// used to dynamically store dependencies on any potential root
			List dependents = new ArrayList();
			
			List copyPotRoots = new ArrayList(potRoots);
			for (int i=0; i<copyPotRoots.size(); i++) {
				
				// test potential root
				OWLClass potRoot = (OWLClass) copyPotRoots.get(i);
				if (DEBUG) System.out.println("Testing: "+getName(potRoot));
				
				// remove pot root from ontology
				OWLOntology beforeRemovingOnt = this.cloneOntology(ont);
				OWLEntity pr = ont.getClass(potRoot.getURI());				
//				OWLEntityRemover remover = new OWLEntityRemover(ont);        		
//				remover.removeEntity(pr);
        		RemoveEntity change = new RemoveEntity(ont, pr, null);
        		change.accept((ChangeVisitor) ont);
				if (DEBUG) System.out.println("Removing: "+getName(potRoot));
				
        		// check if current potRoot is dependent of any previous potRoot
				if (contains(dependents, potRoot)) {
					// its been removed above, now dont check for satisfiability of anything else
					if (DEBUG) System.out.println("Skipping: "+getName(potRoot)+" because its a dependent");
        			potRoots.remove(potRoot);
        			continue;
        		}
				
        		// set reasoner with new ontology (-potRoot) 
        		PelletReasoner reasoner = new PelletReasoner();
        		reasoner.setOntology(ont, false);
        		
        		// check sat. of *remaining* potRoots
        		potRoots.remove(potRoot);
        		boolean allSat = true;
        		for (Iterator iter2 = potRoots.iterator(); iter2.hasNext();) {
					OWLClass rem = (OWLClass) iter2.next();
					if (DEBUG) System.out.print("Satisfiability of: "+getName(rem)+"..");
					boolean sat = reasoner.isConsistent(rem);
					numSatTests++;
					if (DEBUG) System.out.println(String.valueOf(sat));
					if (!sat) {
						// unsatisfiable class found!
						allSat = false;
						break;
					}
					else {
						// dependency on current potRoot, discard in the future
						dependents.add(rem);						
					}
				}
        		
				if (allSat) {					
					// it must be a root, unless..itself is satisfiable i.e. all classes are satisfiable!
					reasoner.setOntology(beforeRemovingOnt, false);
					numSatTests++;
					if (reasoner.isConsistent(potRoot)) {
						// all potRoots satisfiable..return
						if (DEBUG) System.out.println("All pot-roots are satisfiable");
						return null;
					}
					// found root! current potRoot = root
					OWLClass root = potRoot;
					Set roots = new HashSet();
					roots.add(root);
					roots.add("?");
					// add dependency sets of remaining potRoots
					for (Iterator depIter=potRoots.iterator(); depIter.hasNext();) {
						OWLClass derived = (OWLClass) depIter.next();
						// below is not correct! - parents could be any one/more of the classes removed  
						dependencyMap.put(derived, roots);
					}
					// remove from corresponding global sets for future iterations
					rootClasses.removeAll(potRoots);
					rootClasses.removeAll(dependents);
					derivedClasses.addAll(potRoots);
					derivedClasses.addAll(dependents);
					return root;					
				}				
			}
		}
		catch (OWLException ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	private boolean contains(List list, OWLClass elem) {
		try {
			for (Iterator iter = list.iterator(); iter.hasNext();) {
				OWLClass cla = (OWLClass) iter.next();
				if (cla.getURI().equals(elem.getURI())) return true;
			}
		}
		catch (OWLException ex) {
			ex.printStackTrace();
		}
		return false;
	}
	
	public OWLOntology cloneOntology(OWLOntology source) {
		OWLOntology copy = null;
		try {
			CorrectedRDFRenderer rdfRend = new CorrectedRDFRenderer();
			StringWriter st = new StringWriter();
			rdfRend.renderOntology(source, st);
			copy = this.loadOntologyInRDF(new StringReader(st.toString()), source.getURI(), true);			
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		return copy;
	}
	
	Set getAllPropValueChains(OWLClass cla) {
		
		allPC = new HashSet(); /*** reseting this is key for each class ***/
		try {
			// trace all equivalent axioms for prop-value chains
			for (Iterator iter = cla.getEquivalentClasses(ontologies).iterator(); iter.hasNext();) {
				OWLDescription equCla = (OWLDescription) iter.next();
				propChain = new ArrayList();
				inUnion = false;
				getDependency(equCla);
			}
			// trace all superclass axioms for prop-value chains
			for (Iterator iter = cla.getSuperClasses(ontologies).iterator(); iter.hasNext();) {
				OWLDescription supCla = (OWLDescription) iter.next();
				propChain = new ArrayList();
				inUnion = false;
				getDependency(supCla);
			}		
		}
		catch (OWLException ex) {
			ex.printStackTrace();
		}
		
		return allPC;
	}
	
	Set getDependency(OWLClass cla) {
		Set dep = new HashSet();
		
		try {
//			if (preprocessing) {
//				// do nothing here
//			}
//			else {
				// trace dependencies of all equivalent axioms
				// consider inferred equivalents using RDFS reasoner 
				for (Iterator iter = rdfsReasoner.equivalentClassesOf(cla).iterator(); iter.hasNext();) {
					OWLDescription equCla = (OWLDescription) iter.next();
					propChain = new ArrayList();
					if (unsat.contains(equCla)) {
						if (derivedClasses.contains(equCla)) {
							if (unfoldSet((Set) dependencyMap.get(equCla)).size()>2) {
								dep.add(equCla);
							}
						}
						else dep.add(equCla);
					}
				}
				// consider asserted equivalents
				for (Iterator iter = cla.getEquivalentClasses(ontologies).iterator(); iter.hasNext();) {
					OWLDescription equCla = (OWLDescription) iter.next();				
					propChain = new ArrayList();
					Set d = getDependency(equCla);
					if (!d.isEmpty()) dep.add(d);
				}
				// trace dependencies of all superclass axioms
				for (Iterator iter = cla.getSuperClasses(ontologies).iterator(); iter.hasNext();) {
					OWLDescription supCla = (OWLDescription) iter.next();
					propChain = new ArrayList();
					Set d = getDependency(supCla);
					if (!d.isEmpty()) dep.add(d);					
				}
				// finally if cla itself is unsat add it to dep
				if (unsat.contains(cla)) {
					dep.add(cla);
				}
//			}
		}
		catch (OWLException ex) {
			ex.printStackTrace();
		}
		return dep;
	}
	
	Set getDependency(OWLDescription desc) {
		
		if (desc instanceof OWLClass) {
			return getDependency((OWLClass) desc);
		}
		
		if (desc instanceof OWLRestriction) {
			return getDependency((OWLRestriction) desc);
		}
		
		Set dep = new HashSet();
		try {
			if (desc instanceof OWLAnd) {
				// simply recurse along nested class description operands
				// do not recurse on named classes! instead check for unsat and add them
				for (Iterator iter = ((OWLAnd) desc).getOperands().iterator(); iter.hasNext();) {
					OWLDescription op = (OWLDescription) iter.next();
					// copy propChain before recursing
					List copyPropChain = new ArrayList(propChain);
					if (!(op instanceof OWLClass)) {
						// recurse
						Set d = getDependency(op);
						if (!d.isEmpty()) dep.add(d);						
					}
					else {
						// add named class as dependency if unsatisfiable
						if (unsat.contains(op)) {
							if (preprocessing) {
								if (this.lastPropAll) {
									List endPropChain = new ArrayList(propChain); 
									endPropChain.add(op);
									if (inUnion) endPropChain.add("o");
									else endPropChain.add("d");
									allPC.add(endPropChain);
								}
							}
							else {
								dep.add(op);
							}
						}
					}
					// and restore propChain after recursing
					propChain = new ArrayList(copyPropChain);
				}					
			}
			else if (desc instanceof OWLOr) {
				if (preprocessing) {
					/*** entering union */
					inUnion = true;
					
					// now recurse along nested class description operands
					// do not recurse on named classes! ignore them in preprocessing
					for (Iterator iter = ((OWLOr) desc).getOperands().iterator(); iter.hasNext();) {
						OWLDescription op = (OWLDescription) iter.next();
						// copy propChain before recursing
						List copyPropChain = new ArrayList(propChain);
						if (!(op instanceof OWLClass)) getDependency(op);
						else if (unsat.contains(op)) {
							if (this.lastPropAll) {
								List endPropChain = new ArrayList(propChain); 
								endPropChain.add(op);
								endPropChain.add("o");
								allPC.add(endPropChain);
							}
						}
						// and restore propChain after recursing
						propChain = new ArrayList(copyPropChain);
					}
					
					/*** leaving union */
					inUnion = false;
				}
				else {
					// recurse along nested class description operands
					// do not recurse on named classes, instead check for unsat
					for (Iterator iter = ((OWLOr) desc).getOperands().iterator(); iter.hasNext();) {
						OWLDescription op = (OWLDescription) iter.next();
						Set d = new HashSet();
						// copy propChain before recursing
						List copyPropChain = new ArrayList(propChain);
						if (!(op instanceof OWLClass)) d = getDependency(op);
						else if (unsat.contains(op)) d.add(op);
						// and restore propChain after recursing
						propChain = new ArrayList(copyPropChain);
						if (d.isEmpty()) return new HashSet();
						else dep.add(d);
					}
				}
			}
		}
		catch (OWLException ex) {
			ex.printStackTrace();
		}
		return dep;
	}
	
	Set getDependency(OWLRestriction res) {
		Set dep = new HashSet();
		
		try {
			if (preprocessing) {
				// only check for some/all on object properties
				if (res instanceof OWLObjectQuantifiedRestriction) {
					propChain.add(res.getProperty()); //adds object property
					// get value of restriction
					OWLDescription desc = ((OWLObjectQuantifiedRestriction) res).getDescription();
					if (res instanceof OWLObjectAllRestriction) {
						// if value is a class, append value to propChain
						// and add it to allPC
						if (desc instanceof OWLClass) {
							
							// check if unsatisfiable now:
							if (unsat.contains(desc)) {
								// class now becomes terminal of current propChain
								List endPropChain = new ArrayList(propChain);
								endPropChain.add(desc);
								/* also check inUnion or not and tag as optional */
								if (inUnion) {
									endPropChain.add("o"); // optional
								}
								else endPropChain.add("d"); // definite
								allPC.add(endPropChain);
							}
						}
						this.lastPropAll = true;
					}
					else this.lastPropAll = false;
					// recurse on nested class descriptions   
					if (!(desc instanceof OWLClass)) getDependency(desc);
				}
			}
			else {
				propChain.add(res.getProperty()); //adds property ? (more generic than during preprocessing)
				
				if (res instanceof OWLObjectSomeRestriction) {
					
					// existential restriction - check for dependencies on must exist property
					Set d = getDependency(res.getProperty());
					if (!d.isEmpty()) dep.add(d);
					
					//*** and check for dependencies on value
					OWLDescription value = ((OWLObjectSomeRestriction) res).getDescription();
					if (!(value instanceof OWLClass)) {
						d = getDependency(value);
						if (!d.isEmpty()) dep.add(d);						
					}
					else if (unsat.contains(value)) dep.add(value);					
				}
				else if (res instanceof OWLCardinalityRestriction) {
					
					// minCard >0 restriction - check for dependencies on must exist property
					int value = ((OWLCardinalityRestriction) res).getAtLeast();
					if (value>0) {
						Set d = getDependency(res.getProperty());
						if (!d.isEmpty()) dep.add(d);	
					}
				}
				else if (res instanceof OWLDataValueRestriction || res instanceof OWLObjectValueRestriction) {
					
					// hasValue restriction - check for dependencies on must exist property
					Set d = getDependency(res.getProperty());
					if (!d.isEmpty()) dep.add(d);
				}
			}
		}
		catch (OWLException ex) {
			ex.printStackTrace();
		}
		return dep;
	}
	
	Set getDependency(OWLProperty prop) {
		Set dep = new HashSet();
		
		try {
			if (preprocessing) {
				// do nothing here
			}
			else {
				// check if current propChain is present in allPC
				OWLClass value = null;
				if ((value=checkChain())!=null) {
					dep.add(value);
				}
				
				// check for domain
				for (Iterator iter = prop.getDomains(ontologies).iterator(); iter.hasNext();) {
					OWLDescription dom = (OWLDescription) iter.next();
					if (!(dom instanceof OWLClass)) {
						Set d = getDependency(dom);
						if (!d.isEmpty()) dep.add(d);
					}
					else if (unsat.contains(dom)) dep.add(dom);
				}
				
				// get dependencies of all asserted super props
				for (Iterator iter = prop.getSuperProperties(ontologies).iterator(); iter.hasNext();) {
					OWLProperty supProp = (OWLProperty) iter.next();
					Set d = getDependency(supProp);
					if (!d.isEmpty()) dep.add(d);					
				}
				
				// get dependencies on all inverse properties
				if (prop instanceof OWLObjectProperty) {
					for (Iterator iter = ((OWLObjectProperty) prop).getInverses(ontologies).iterator(); iter.hasNext();) {
						OWLObjectProperty invProp = (OWLObjectProperty) iter.next();
						Set d = getInverseDependency(invProp);
						if (!d.isEmpty()) dep.add(d);					
					}
				}
			}
		}
		catch (OWLException ex) {
			ex.printStackTrace();
		}
		return dep;
	}
	
	private Set getInverseDependency(OWLObjectProperty prop) {
		Set dep = new HashSet();
		
		try {
			if (preprocessing) {
				// do nothing here
			}
			else {
				// check for range
				for (Iterator iter = prop.getRanges(ontologies).iterator(); iter.hasNext();) {
					OWLDescription ran = (OWLDescription) iter.next();
					if (!(ran instanceof OWLClass)) {
						Set d = getDependency(ran);
						if (!d.isEmpty()) dep.add(d);
					}
					else if (unsat.contains(ran)) dep.add(ran);
				}
				
				// get dependencies of all asserted super props
				for (Iterator iter = prop.getSuperProperties(ontologies).iterator(); iter.hasNext();) {
					OWLProperty supProp = (OWLProperty) iter.next();
					Set d = getDependency(supProp);
					if (!d.isEmpty()) dep.add(d);					
				}
			}
		}
		catch (OWLException ex) {
			ex.printStackTrace();
		}
		return dep;
	}
	
	/*
	 * Check if current propChain is a prefix of any allPC chain
	 */
	private OWLClass checkChain() {
		OWLClass value = null;
		for (Iterator iter = allPC.iterator(); iter.hasNext();) {
			List allPCchain = (ArrayList) iter.next();
			/* note last two values of chain are terminal class and optional definite string tag */
			boolean match = true;
			if (propChain.size()==(allPCchain.size()-2)) {
				for (int i=0; i<allPCchain.size()-2; i++) {
					
					if (!allPCchain.get(i).equals(propChain.get(i))) {
						match = false;
						break;
					}
				}
				if (match) {
					value = (OWLClass) allPCchain.get(allPCchain.size()-2);
					break;
				}
			}
		}
		return value;
	}
	
	public OWLOntology loadOntologyInRDF(Reader reader, URI uri, boolean importing) {
		OWLOntology ontology = null;

		try {
			OWLRDFParser parser = new OWLRDFParser();
			parser.setImporting(importing);
			
			parser.setOWLRDFErrorHandler(new OWLRDFErrorHandler() {
				public void owlFullConstruct(int code, String message)
						throws SAXException {
				}
	
				public void error(String message) throws SAXException {
					throw new SAXException(message.toString());
				}
	
				public void warning(String message) throws SAXException {
					System.out.println("RDFParser: " + message.toString());
				}
	
				public void owlFullConstruct(int code, String message, Object obj) throws SAXException {
					// TODO Auto-generated method stub
				}
			});
	    	
			OWLConnection connection = new OWLConnectionImpl();
			parser.setConnection(connection);
			// PARSE THE ONTOLOGY!
			ontology = parser.parseOntology(reader, uri);			
		}
		catch (Exception e) {
			e.printStackTrace();			
		}
		return ontology;
	}
	
	private String getName(OWLEntity entity) {
		try {
			String name = entity.getURI().toString();
			if (name.indexOf("#")>=0) name = name.substring(name.indexOf("#")+1, name.length());
			else name = name.substring(name.lastIndexOf("/")+1, name.length());
			return name;
		}
		catch (OWLException ex) {
			ex.printStackTrace();
		}
		return "";
	}
	
	protected Set unfoldSet(Set set) {
		Set unfold = new HashSet();
		
		for (Iterator iter = set.iterator(); iter.hasNext();) {
			Object obj = iter.next();
			if (obj instanceof Set) unfold.addAll(unfoldSet((Set) obj));
			else if (obj instanceof OWLEntity) unfold.add(obj);
			else if (obj instanceof String) unfold.add(obj);
		}
		return unfold;
	}
	
	/*
	 * count the no. of derived classes under each root
	 */
	public void countDependencies() {
		childMap = new HashMap();
		for (Iterator iter = rootClasses.iterator(); iter.hasNext();) {
			OWLClass root = (OWLClass) iter.next();
			childMap.put(root, getChildren(root));	
		}
	}
	
	/*
	 * create a child-map for each root class 
	 */
	private Set getChildren(OWLClass cla) {
		Set children = new HashSet();
		for (Iterator iter = derivedClasses.iterator(); iter.hasNext();) {
			OWLClass der = (OWLClass) iter.next();
			if (!der.equals(cla)) {
				Set dep = (HashSet) this.dependencyMap.get(der);
				dep = this.unfoldSet(dep);
				if (dep.contains(cla)) {
					children.add(der);
					children.addAll(getChildren(der));
				}
			}
		} 
		return children;
	}
}
