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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.reasoner.SwoopReasoner;
import org.semanticweb.owl.io.vocabulary.OWLVocabularyAdapter;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLClassAxiom;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDisjointClassesAxiom;
import org.semanticweb.owl.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLProperty;
import org.semanticweb.owl.model.OWLSubClassAxiom;

/**
 * @author Aditya
 */
public class SwoopStatistics {

	SwoopModel swoopModel;
	
	public static String ONTOLOGY = "O";
	public static String UNSATISFIABLE_CLASSES = "Unsatisfiable";
	public static String NO_GCI = "GCIs";
	public static String NO_SUBSUMPTIONS = "Subsumptions";
	public static String NO_DISJOINT = "Disjoints";
	public static String NO_TRANSITIVE = "Transitive";
	public static String NO_SYMMETRIC = "Symmetric";
	public static String NO_FUNCTIONAL = "Functional";
	public static String NO_INVFUNCTIONAL = "Inverse-Functional";
	public static String NO_INVERSE = "Inverses";
	
	public static String MAX_DEPTH_CLASS_TREE = "Deepest SubTree [C]";
	public static String MIN_DEPTH_CLASS_TREE = "Shallowest SubTree [C]";
	public static String AVG_DEPTH_CLASS_TREE = "8";
	public static String MAX_DEPTH_PROP_TREE = "Deepest SubTree [P]";
	public static String MIN_DEPTH_PROP_TREE = "Shallowest SubTree [P]";
	public static String AVG_DEPTH_PROP_TREE = "11";
	public static String MAX_BRANCHING_FACTOR = "Most Children [C]";
	public static String MIN_BRANCHING_FACTOR = "Fewest Children [C]";
	public static String AVG_BRANCHING_FACTOR = "";
	
	public static String MULTIPLE_INHERITANCE_CLASS = "Mult. Inheritance [C]";
	public static String MULTIPLE_INHERITANCE_PROP = "Mult. Inheritance [P]";
	
	public SwoopStatistics(SwoopModel model) {
		this.swoopModel = model;
	}
	
	public HashMap computeStatistics(OWLOntology ontology) {
		
		SwoopReasoner reasoner = swoopModel.getReasoner();
		
		HashMap stats = new HashMap();
//		stats.put(this.ONTOLOGY, ontology); // don't need this
		
		Set gci = new HashSet();
		Set disj = new HashSet();
		
		try {
			// class axiom types
			for (Iterator iter = ontology.getClassAxioms().iterator(); iter.hasNext();) {
				OWLClassAxiom axiom = (OWLClassAxiom) iter.next();
				if (axiom instanceof OWLSubClassAxiom) {
					OWLSubClassAxiom subAxiom = (OWLSubClassAxiom) axiom;
					if (!(subAxiom.getSubClass() instanceof OWLClass)) gci.add(axiom);
				}
				else if (axiom instanceof OWLEquivalentClassesAxiom) {
					OWLEquivalentClassesAxiom equAxiom = (OWLEquivalentClassesAxiom) axiom;
					for (Iterator iter2=equAxiom.getEquivalentClasses().iterator(); iter2.hasNext();) {
						OWLDescription desc = (OWLDescription) iter2.next();
						if (!(desc instanceof OWLClass)) gci.add(axiom);
					}
				}
				else if (axiom instanceof OWLDisjointClassesAxiom) 
					disj.add(axiom);
			}
			
			stats.put(this.NO_GCI, gci);
			stats.put(this.NO_DISJOINT, disj);
			
			// prop attribs
			Set props = ontology.getObjectProperties();
			props.addAll(ontology.getDataProperties());
			Set tran = new HashSet();
			Set symm = new HashSet();
			Set invf = new HashSet();
			Set func = new HashSet();
			Set inv = new HashSet();
			Set multP = new HashSet();
			for (Iterator iter = props.iterator(); iter.hasNext();) {
				OWLProperty prop = (OWLProperty) iter.next();
				
				// check multiple inheritance for props
				if (prop!=null && reasoner.getOntology()!=null && reasoner.getOntology().equals(ontology) && reasoner.superPropertiesOf(prop).size()>1) multP.add(prop);
				
				if (prop instanceof OWLObjectProperty) {
					if (((OWLObjectProperty) prop).isFunctional(ontology)) func.add(prop);
					if (((OWLObjectProperty) prop).isInverseFunctional(ontology)) invf.add(prop);
					if (((OWLObjectProperty) prop).isTransitive(ontology)) tran.add(prop);
					if (((OWLObjectProperty) prop).isSymmetric(ontology)) symm.add(prop);
					if (((OWLObjectProperty) prop).getInverses(ontology).size()>0) inv.add(prop);
				}
				else {
					if (((OWLDataProperty) prop).isFunctional(ontology)) func.add(prop);
				}
			}
			stats.put(this.MULTIPLE_INHERITANCE_PROP, multP);
			stats.put(this.NO_FUNCTIONAL, func);
			stats.put(this.NO_INVFUNCTIONAL, invf);
			stats.put(this.NO_TRANSITIVE, tran);
			stats.put(this.NO_SYMMETRIC, symm);
			stats.put(this.NO_INVERSE, inv);
			
			// tree specifics (using current SwoopReasoner and current Class/Prop trees)
			String minCl="?", maxCl="?", avgCl="?"; // class depth
			String minBf="?", maxBf="?", avgBf="?"; // braching factor 
			Set maxCSet = new HashSet();
			Set minCSet = new HashSet();
			Set maxBfSet = new HashSet();
			Set minBfSet = new HashSet();
			OWLOntology cTreeOfOnt = swoopModel.getFrame().termDisplay.getClassTreeOfOntology();
			if (cTreeOfOnt!=null && cTreeOfOnt.equals(ontology)) {
				int minC = Integer.MAX_VALUE, maxC = 0;
				int minB = Integer.MAX_VALUE, maxB = 0; // B for branching factor
				float avgC = 0;
				float avgB = 0;
				HashMap cache = new HashMap();
				HashMap bcache = new HashMap();
				if (swoopModel.getFrame().termDisplay.getTrees()[0]!=null) {
					TreeModel cTreeModel = swoopModel.getFrame().termDisplay.getTrees()[0].getModel();
					DefaultMutableTreeNode cRoot = (DefaultMutableTreeNode) cTreeModel.getRoot();
					
					// compute total subsumptions
					int subsumptions = 0;
//					for (int i = 0; i<cRoot.getChildCount(); i++) {
						subsumptions = getDescendentCount(cRoot) - cRoot.getChildCount();
//					}
					stats.put(this.NO_SUBSUMPTIONS, String.valueOf(subsumptions));
					
					Enumeration depthFirstEnum = cRoot.depthFirstEnumeration();
					int numLeaves = 0;
					int numNonLeaves = 0;
					while ( depthFirstEnum.hasMoreElements() )
					{
						DefaultMutableTreeNode node = (DefaultMutableTreeNode)depthFirstEnum.nextElement();
						
						// do not count the depth/branching factor of unsatisfiable classes
						TreeNode [] path = node.getPath();
						
						if ( path.length > 1 )
						{
							DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode)path[1];
							Set concepts = (Set)dmtn.getUserObject();
							String n = OWLVocabularyAdapter.OWL;
							OWLClass concept = (OWLClass)concepts.iterator().next(); // get first element
							if ( concept.getURI().toString().equals( OWLVocabularyAdapter.OWL + "Nothing"))
								continue;
						}
						
						if ( node.isLeaf() ) // if leaf, count depth
						{
							numLeaves++;
							int depth = path.length - 1; // path.length - 1 = number of links from root to leaf
							if ( depth > 0)
								this.cacheDepth(cache, String.valueOf(depth), (Set)((DefaultMutableTreeNode)path[1]).getUserObject());
							if ( depth == 0 )
								this.cacheDepth(cache, String.valueOf(depth), (Set)(cRoot).getUserObject());
							
							if ( depth > maxC)
								maxC = depth;
							if ( depth < minC )
								minC = depth;
							avgC = avgC + depth;
						}
						else // not a leaf node, count branching factor
						{
							numNonLeaves++;
							int numChildren = node.getChildCount();							
							this.cacheDepth(bcache, String.valueOf(numChildren), 
									(Set)node.getUserObject());
							if ( numChildren > maxB)
								maxB = numChildren;
							if ( numChildren < minB )
								minB = numChildren;
							avgB = avgB + numChildren;
						}
					}
					avgC = avgC / numLeaves;
					avgB = avgB / numNonLeaves;
					maxCl = String.valueOf(maxC);
					if (minC == Integer.MAX_VALUE) minC = 0;
					minCl = String.valueOf(minC);
					avgCl = String.valueOf(avgC);
					
					maxBf = String.valueOf(maxB);
					if (minB == Integer.MAX_VALUE) minB = 0;
					minBf = String.valueOf(minB);
					avgBf = String.valueOf(avgB);
					
					// get element set corresponding to min/max
					maxCSet = (HashSet) cache.get(maxCl);
					minCSet = (HashSet) cache.get(minCl);
					
					maxBfSet = (HashSet) bcache.get( maxBf );
					minBfSet = (HashSet) bcache.get( minBf );
				}
			}
			
			// class tree depth
			if ((avgCl.length()>4) && ( avgCl.indexOf(".") != -1)) avgCl = avgCl.substring(0, avgCl.indexOf(".") + 2);
			stats.put(this.AVG_DEPTH_CLASS_TREE, avgCl);
			List minCList = new ArrayList();
			minCList.add(minCl);
			minCList.add(minCSet);
			stats.put(this.MIN_DEPTH_CLASS_TREE, minCList);
			List maxCList = new ArrayList();
			maxCList.add(maxCl);
			maxCList.add(maxCSet);
			stats.put(this.MAX_DEPTH_CLASS_TREE, maxCList);
			
			// branching factor
			if ((avgBf.length()>4) && ( avgBf.indexOf(".") != -1)) avgBf = avgBf.substring(0, avgBf.indexOf(".") + 2);
			stats.put(this.AVG_BRANCHING_FACTOR, avgBf);
			List minBList = new ArrayList();
			minBList.add(minBf);
			minBList.add(minBfSet);
			stats.put(this.MIN_BRANCHING_FACTOR, minBList);
			List maxBList = new ArrayList();
			maxBList.add(maxBf);
			maxBList.add(maxBfSet);
			stats.put(this.MAX_BRANCHING_FACTOR, maxBList);			
			
			String minPr="?", maxPr="?", avgPr="?";
			Set maxPSet = new HashSet();
			Set minPSet = new HashSet();
			OWLOntology pTreeOfOnt = swoopModel.getFrame().termDisplay.getPropTreeOfOntology();
			if (pTreeOfOnt!=null && pTreeOfOnt.equals(ontology)) {
				int minP = Integer.MAX_VALUE, maxP = 0;
				float avgP = 0;
				HashMap cache = new HashMap();
				if (swoopModel.getFrame().termDisplay.getTrees()[1]!=null) {
					TreeModel pTreeModel = swoopModel.getFrame().termDisplay.getTrees()[1].getModel();			
					DefaultMutableTreeNode pRoot = (DefaultMutableTreeNode) pTreeModel.getRoot();
					
					Enumeration depthFirstEnum = pRoot.depthFirstEnumeration();
					int numLeaves = 0;
					while ( depthFirstEnum.hasMoreElements() )
					{
						DefaultMutableTreeNode node = (DefaultMutableTreeNode)depthFirstEnum.nextElement();
						if ( node.isLeaf() )
						{
							numLeaves++;
							TreeNode [] path = node.getPath(); 
							int depth = path.length - 2; // path.length - 2 = number of links from top node
							if ( depth > 0)
								this.cacheDepth(cache, String.valueOf(depth), (Set)((DefaultMutableTreeNode)path[1]).getUserObject());
							if ( depth == 0 )
								this.cacheDepth(cache, String.valueOf(depth), (Set)(pRoot).getUserObject());
							
							if ( depth > maxP )
								maxP = depth;
							if ( depth < minP )
								minP = depth;
							avgP = avgP + depth;
						}
					}
					
					avgP = avgP / numLeaves;
					maxPr = String.valueOf(maxP);
					minPr = String.valueOf(minP);
					avgPr = String.valueOf(avgP);
					
					// get element set corresponding to min/max
					maxPSet = (HashSet) cache.get(maxPr);
					minPSet = (HashSet) cache.get(minPr);
				}
			}
			
			if (avgPr.length()>4) avgPr = avgPr.substring(0, 4);
			stats.put(this.AVG_DEPTH_PROP_TREE, avgPr);
			List minPList = new ArrayList();
			minPList.add(minPr);
			minPList.add(minPSet);
			stats.put(this.MIN_DEPTH_PROP_TREE, minPList);
			List maxPList = new ArrayList();
			maxPList.add(maxPr);
			maxPList.add(maxPSet);
			stats.put(this.MAX_DEPTH_PROP_TREE, maxPList);
			
			// multiple inheritance for classes
			Set mult = new HashSet();
			if (reasoner.getOntology()!=null && reasoner.getOntology().equals(ontology)) {
				for (Iterator iter = ontology.getClasses().iterator(); iter.hasNext();) {
					OWLClass cla = (OWLClass) iter.next();
					if (reasoner.isConsistent(cla) && reasoner.superClassesOf(cla).size()>1) mult.add(cla);
				}				
			}
			stats.put(this.MULTIPLE_INHERITANCE_CLASS, mult);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return stats;
	}

	/*
	 * cache stores map: depth no. -> set of classes/props with depth 
	 */
	private void cacheDepth(HashMap cache, String key, Set val) {
		Set curr = new HashSet();
		if (cache.containsKey(key)) curr = (HashSet) cache.get(key);
		curr.addAll(val);
		cache.put(key, curr);
	}
	
	private int getDescendentCount(TreeNode node) {
		int count = 0;
		while (((DefaultMutableTreeNode) node).getNextNode()!=null) {
			count ++;
			node = ((DefaultMutableTreeNode) node).getNextNode();
		}
		return count;
	}
}
