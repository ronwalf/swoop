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

package org.mindswap.swoop;

import java.awt.Component;
import java.awt.Font;
import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import org.mindswap.swoop.reasoner.SwoopReasoner;
import org.mindswap.swoop.utils.SwoopCache;
import org.mindswap.swoop.utils.owlapi.DefaultShortFormProvider;
import org.mindswap.swoop.utils.treeexport.TreeSerializer;
import org.mindswap.swoop.utils.ui.EntityComparator;
import org.mindswap.swoop.utils.ui.SwoopIcons;
import org.semanticweb.owl.io.ShortFormProvider;
import org.semanticweb.owl.io.vocabulary.OWLVocabularyAdapter;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLProperty;
import org.semanticweb.owl.model.helper.OntologyHelper;

/**
 * @author Evren Sirin
 */
public class TreeRenderer {
	private static OWLVocabularyAdapter OWL = OWLVocabularyAdapter.INSTANCE;

	private SwoopModel swoopModel;

	private SwoopFrame swoopHandler;

	private ShortFormProvider shortFormProvider = new DefaultShortFormProvider();

	private OWLClass owlThing;

	private OWLClass owlNothing;

	public boolean useOldClassTreeReferenceforExpansion = false;

	public boolean useOldPropertyTreeReferenceforExpansion = false;

	TreeCellRenderer treeCellRenderer = new DefaultTreeCellRenderer() {
		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean sel, boolean expanded, boolean leaf, int row,
				boolean hasFocus) {

			super.getTreeCellRendererComponent(tree, value, sel, expanded,
					leaf, row, hasFocus);

			DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
			try {

				SwoopReasoner reasoner = swoopModel.getReasoner();
				Set set = (Set) node.getUserObject();
				String label = "";
				boolean changed = false;
				boolean annoteated = false;
				boolean isCycle    = false;
				
				//*********************************************
				//Added for Econnections
				//**********************************************
				boolean econnected = false;
				//**********************************************

				// check to see if this node is at the end of a cycle 
				// and initialize possible cycle head
				SwoopTreeNode stn = (SwoopTreeNode)node;				
				OWLEntity cycleHead = null;
				if ( ((Boolean)stn.getUserObject( SwoopTreeNode.IS_IN_CYCLE )).booleanValue() )
				{
					isCycle = true;
					cycleHead = (OWLEntity)stn.getUserObject( SwoopTreeNode.CYCLE_HEAD );
				}
				
				if (set.size() > 1)
					label += "[";
				Iterator i = set.iterator();
				while (i.hasNext()) {
					OWLEntity entity = (OWLEntity) i.next();

					// check if entity is dirty (has changes associated with it)
//					if (swoopModel.getDirtyEntities().contains(entity))
					if (swoopModel.getChangesCache().getChangeList(entity.getURI()).size()>0)	
						changed = true;

					// mark entities that have annotations with superscript A
					try {
						if (swoopModel.getAnnotatedObjectURIs().contains(
								entity.getURI()))
							annoteated = true;
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					//*************************************************
					// Changed for Econnections
					//*************************************************
					//Iterator iter =
					// swoopModel.getSelectedOntology().getObjectProperties().iterator();
					//while(iter.hasNext()){
					//OWLObjectProperty prop = (OWLObjectProperty)iter.next();
					//	if(prop.isLink()){
					//		if(entity instanceof OWLClass &&
					// OntologyHelper.entityUsage(swoopModel.getSelectedOntology(),
					// prop).contains(entity)){
					//econnected = true;
					//		econnected=false;
					//break;
					//}
					//}
					//}

					//*************************************************

					SwoopIcons swoopIcons = new SwoopIcons();
					if (swoopIcons.getIcon(entity, swoopModel) != null)
						setIcon(swoopIcons.getIcon(entity, swoopModel));

					label += getShortForm(entity.getURI());
					if (i.hasNext())
						label += ", ";
				}
				if (set.size() > 1)
					label += "]";

				if (changed)
					label = label + "*";
				if (swoopModel.getUseLabels() || annoteated || econnected || isCycle) {
					String htmlLabel = "<html>";
					//if (econnected) htmlLabel += "<sup> <font
					// size=\"2\">ECONN </font></sup>" ;
					if (econnected)
						htmlLabel += "<b> <font color=\"#CC6600\">";
					htmlLabel += label;
					if (annoteated)
						htmlLabel += "<sup>A</sup>";
					if (econnected)
						htmlLabel += "</font> </b>";					
					if (isCycle)
					{
						String cycleHeadName = swoopModel.shortForm( cycleHead.getURI());
						htmlLabel += "<sup>" + cycleHeadName + "</sup>";
					}
					htmlLabel += "</html>";
					label = htmlLabel;

				}

				setText(label);
				//setPreferredSize(new Dimension(label.length()+200, 32));

			} catch (OWLException e) {
				e.printStackTrace();
			}

			return this;
		}

	};

	/**
	 * Constructor
	 */
	public TreeRenderer(SwoopModel swoopModel, SwoopFrame swoopHandler) {
		this.swoopModel = swoopModel;
		this.swoopHandler = swoopHandler;
	}

	public JTree getClassTree(JTree currentTree) throws OWLException {
		System.out.println("Create class tree");

		SwoopReasoner reasoner = swoopModel.getReasoner();
		OWLOntology ontology = reasoner.getOntology();

		Font newFont = swoopModel.getFont();

		if (ontology == null)
			return null;

		if (!ontology.equals(swoopModel.getSelectedOntology())) {
			System.out
					.println("ontology mismatch in swoopReasoner and swoopModel!");
		}

		JTree classTree = null;

		if (ontology == null)
			return classTree;

		classTree = swoopModel.getClassTreeCache().getTree(ontology, reasoner);

		// check if ontology is in cache -> get property tree from cache
		if (classTree != null) {
			classTree.setFont(newFont);
			System.out.println("loading class tree from cache");
			return classTree;
		}

		SwoopTreeNode thing = null;
		SwoopTreeNode nothing = null;

		owlThing = ontology.getOWLDataFactory().getOWLThing();
		owlNothing = ontology.getOWLDataFactory().getOWLNothing();

		Set set = reasoner.equivalentClassesOf(owlThing);
		set.add(owlThing);
		thing = createClassTree(set, new HashSet() );

		Set eqs = reasoner.equivalentClassesOf(owlNothing);
		Iterator i = eqs.iterator();
		if (i.hasNext()) {
			nothing = createNode(owlNothing);
			while (i.hasNext()) {
				OWLClass sub = (OWLClass) i.next();

				SwoopTreeNode node = createNode(sub);

				if (node != null)
					nothing.add(node);
			}
			thing.add(nothing);
		}

		classTree = new JTree(new DefaultTreeModel(thing));
		classTree.setFont(newFont);
		classTree.setCellRenderer(treeCellRenderer);
		classTree.setLargeModel(true); // resize qname toggle node labels

		if (this.getEntityCount(ontology, 0) < Integer.parseInt(swoopModel
				.getTreeThreshold())) {
			// expand each node, if class count < threshold
			for (int row = 1;; row++) {
				if (classTree.getPathForRow(row) != null)
					classTree.expandRow(row);
				else
					break;
			}
		} else {
			// dont expand each node in tree
			// check for current open nodes and expand only those
			if (currentTree != null
					&& this.useOldClassTreeReferenceforExpansion) {
				this.useOldClassTreeReferenceforExpansion = false;
				Enumeration enu = currentTree
						.getExpandedDescendants(currentTree.getPathForRow(0));
				if (enu != null) {
					while (enu.hasMoreElements()) {
						TreePath path = (TreePath) enu.nextElement();
						int row = currentTree.getRowForPath(path);
						classTree.expandRow(row);
					}
				}
			}
		}

		// put class tree in cache
		swoopModel.getClassTreeCache().putTree(ontology, reasoner, classTree);
		System.out.println(ontology.getURI() + " class tree put in cache");
		return classTree;
	}

	SwoopTreeNode createClassTree( Set concepts, Set ancestors ) throws OWLException {
		if (concepts.contains(owlNothing))
			return null;

		SwoopTreeNode root = createNode(concepts);
		
		if (concepts.isEmpty())
			return root;

		SwoopReasoner reasoner = swoopModel.getReasoner();
		Object obj = concepts.iterator().next();
		
		if (obj instanceof OWLClass) 
		{
			OWLClass c = (OWLClass) obj;
//			 filter rules classes Impl, ClassAtom, IndividualPropertyAtom,
			// etc.
			if (c.getURI().getPath().equals("/2003/11/swrl"))
				return null;
			Set subs = reasoner.subClassesOf(c);
			Iterator i = subs.iterator();
			ancestors.add( concepts );
			
			while (i.hasNext()) 
			{
				Set set = (Set) i.next();
				if (set.contains(c))
					continue;

				SortedSet sortedSet = orderedEntities(set);
				
				// if ancestors set contains the subclass set then we have a cycle
				if ( ancestors.contains( set ) )
				{
					root.addUserObject( SwoopTreeNode.IS_IN_CYCLE, SwoopTreeNode.TRUE );
					OWLClass cycleHead = (OWLClass)set.iterator().next();
					root.addUserObject( SwoopTreeNode.CYCLE_HEAD, cycleHead );
					continue;
				}
				SwoopTreeNode node = createClassTree(sortedSet, ancestors);
				
				// do not add owl:Nothing to the tree
				if (node != null) {
					int index = 0;
					for (; index < root.getChildCount(); index++) {
						DefaultMutableTreeNode child = (DefaultMutableTreeNode) root
								.getChildAt(index);
						SortedSet otherSet = (SortedSet) child.getUserObject();

						OWLEntity c1 = (OWLEntity) sortedSet.first();
						OWLEntity c2 = (OWLEntity) otherSet.first();

						if (EntityComparator.INSTANCE.compare(
								sortedSet.first(), otherSet.first()) < 0)
							break;
					}

					root.insert(node, index);
				}
			}
			ancestors.remove( concepts );
		}

		return root;
	}

	/**
	 * Count the number of entities of a given type.
	 * @param ontology The ontology to be inspected.  If swoopModel.getShowImports(),
	 *        use the import closure.
	 * @param entityType '0' for classes, '1' for properties (excluding Annotation 
	 *        properties).
	 * @return The number of entities.
	 */
	private int getEntityCount(OWLOntology ontology, int entityType) {
		int count = 0;
		try {
			Set ontologies = Collections.singleton(ontology);
			if (swoopModel.getShowImports()) {
				ontologies = OntologyHelper.importClosure(ontology);
			}
			for (Iterator iter = ontologies.iterator(); iter.hasNext();) {
				OWLOntology ont = (OWLOntology) iter.next();
				switch (entityType) {
				case 0:// class count
					count += ont.getClasses().size();
					break;
				case 1:// prop count
					count += ont.getDataProperties().size();
					count += ont.getObjectProperties().size();
					break;
				}
			}
		} catch (OWLException ex) {
			ex.printStackTrace();
		}
		return count;
	}

	SwoopTreeNode createNode(OWLEntity entity) {
		return new SwoopTreeNode(Collections.singleton(entity));
	}

	SwoopTreeNode createNode(Set set) {
		return new SwoopTreeNode(set);
	}

	private SortedSet orderedEntities(Set entities) {
		SortedSet ss = new TreeSet(EntityComparator.INSTANCE);
		ss.addAll(entities);
		return ss;
	}

	public JTree getPropertyTree(JTree currentTree) throws Exception {
		System.out.println("Create prop tree");

		SwoopReasoner reasoner = swoopModel.getReasoner();
		OWLOntology ontology = reasoner.getOntology();
		JTree propTree = null;

		Font newFont = swoopModel.getFont();

		if (ontology == null)
			return propTree;

		propTree = swoopModel.getPropTreeCache().getTree(ontology, reasoner);

		// check if ontology is in cache -> get property tree from cache
		if (propTree != null) {
			propTree.setFont(newFont);
			System.out.println("loading prop tree from cache");
			return propTree;
		}

		OWLClass owlThing = ontology.getOWLDataFactory().getOWLThing();
		SwoopTreeNode root = createNode(owlThing);

		propTree = new JTree(new DefaultTreeModel(root));
		propTree.setFont(newFont);
		propTree.setCellRenderer(treeCellRenderer);
		propTree.setLargeModel(true); // resize qname toggle node labels

		SortedSet set = orderedEntities(reasoner.getProperties());
//		 filter rules properties argument1, argument2, body, classPredicate,
		// etc.
		for (Iterator it = set.iterator(); it.hasNext();) {
			OWLEntity entity = (OWLEntity) it.next();
			if (entity.getURI().getPath().equals("/2003/11/swrl") )
				it.remove();		
		}
		Iterator i = set.iterator();
		while (i.hasNext()) {
			OWLProperty prop = (OWLProperty) i.next();
			Set eqProps = reasoner.equivalentPropertiesOf(prop);
			Set superProps = reasoner.superPropertiesOf(prop);
			if (superProps.size() > 0)
				continue;
			eqProps.add(prop);
			root.add(createPropertyTree(eqProps));
		}

		propTree.setModel(new DefaultTreeModel(root));

		if (this.getEntityCount(ontology, 1) < Integer.parseInt(swoopModel
				.getTreeThreshold())) {
			// expand each node, if property count < threshold
			for (int row = 1;; row++) {
				if (propTree.getPathForRow(row) != null)
					propTree.expandRow(row);
				else
					break;
			}
		} else {
			// dont expand each node in tree
			// check for current open nodes and expand only those
			if (currentTree != null
					&& this.useOldPropertyTreeReferenceforExpansion) {
				this.useOldPropertyTreeReferenceforExpansion = false;
				Enumeration enu = currentTree
						.getExpandedDescendants(currentTree.getPathForRow(0));
				if (enu != null) {
					while (enu.hasMoreElements()) {
						TreePath path = (TreePath) enu.nextElement();
						int row = currentTree.getRowForPath(path);
						propTree.expandRow(row);
					}
				}
			}
		}

		propTree.setRootVisible(false);
		propTree.setShowsRootHandles(true);
		propTree.setFont(newFont);

		// put property tree in cache
		swoopModel.getPropTreeCache().putTree(ontology, reasoner, propTree);
		System.out.println(ontology.getURI() + " prop tree put in cache");

		return propTree;
	}

	SwoopTreeNode createPropertyTree(Set props) throws OWLException {

		SwoopTreeNode root = createNode(props);

		Object obj = props.iterator().next();
		if (obj instanceof OWLProperty) {

			OWLProperty prop = (OWLProperty) obj;
			SwoopReasoner reasoner = swoopModel.getReasoner();
			Set subs = reasoner.subPropertiesOf(prop);

			Iterator i = subs.iterator();
			while (i.hasNext()) {
				SortedSet set = orderedEntities((Set) i.next());
				if (set.contains(prop))
					continue;

				SwoopTreeNode node = createPropertyTree(set);

				int index = 0;
				for (; index < root.getChildCount(); index++) {
					DefaultMutableTreeNode child = (DefaultMutableTreeNode) root
							.getChildAt(index);
					SortedSet otherSet = (SortedSet) child.getUserObject();

					OWLEntity c1 = (OWLEntity) set.first();
					OWLEntity c2 = (OWLEntity) otherSet.first();

					if (EntityComparator.INSTANCE.compare(set.first(), otherSet
							.first()) < 0)
						break;
				}

				root.insert(node, index);
			}
		}

		return root;
	}

	private String getShortForm(URI uri) {
		return shortFormProvider.shortForm(uri);
	}

	/**
	 * @return Returns the shortFormProvider.
	 */
	public ShortFormProvider getShortFormProvider() {
		return shortFormProvider;
	}

	/**
	 * @param shortFormProvider
	 *            The shortFormProvider to set.
	 */
	public void setShortFormProvider(ShortFormProvider shortFormProvider) {
		this.shortFormProvider = shortFormProvider;
	}

	public void setSwoopModel(SwoopModel swoopModel) {
		this.swoopModel = swoopModel;
	}

	public void removeClassTreeCacheEntry(OWLOntology ont) {
		swoopModel.getClassTreeCache().removeOntology(ont);
	}

	public void removeClassTreeCacheEntry(OWLOntology ont,
			SwoopReasoner reasoner) {
		swoopModel.getClassTreeCache().putTree(ont, reasoner, null);
	}

	public void removePropTreeCacheEntry(OWLOntology ont) {
		swoopModel.getPropTreeCache().removeOntology(ont);
	}

	public void removePropTreeCacheEntry(OWLOntology ont, SwoopReasoner reasoner) {
		swoopModel.getPropTreeCache().putTree(ont, reasoner, null);
	}
	
	public void export( JTree tree, String type, File file, SwoopModel model)
	{ TreeSerializer.export( tree, type, file, model); }
	
}
