/*
 * Created on Mar 11, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.automation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URI;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import org.mindswap.swoop.SwoopFrame;
import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.reasoner.SwoopToldReasoner;
import org.mindswap.swoop.utils.SwoopCache;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLOntology;


/**
 * @author Dave Wang
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class OntologyObfuscator 
{
		
	public static SwoopFrame theFrame;
	
	
	
	public void obfuscateConcepts( SwoopModel model, String uri, int startingNumber )
	{
		obfuscateConcepts( model, uri, startingNumber, false );
	}
	
	/*
	 * Loads an ontology and completely changes its concept names 
	 */
	public void obfuscateConcepts( SwoopModel model, String uri, int startingNumber, boolean randomOrder )
	{
		try
		{
			Random random = new Random();
			int counter = startingNumber;
			Hashtable renamedConceptSets = new Hashtable();
			
			OWLOntology ont = model.loadOntology( new URI(uri) );
			model.setShowQNames( false );
			model.setShowImportsWithThreadBlock( false );
			model.setReasonerWithThreadBlock( new SwoopToldReasoner() );
			model.setSelectedOntology( ont );

	 		try
			{ Thread.sleep( 7000 ); }
	 		catch ( Exception e )
			{ e.printStackTrace(); }
	 		
	 		SwoopCache treeCache = model.getClassTreeCache();
	 		
	 		JTree tree = model.myFrame.termDisplay.getCurrentTree();
	 		//treeCache.getTree( ont, new SwoopToldReasoner() );
	 		
	 		DefaultMutableTreeNode root = (DefaultMutableTreeNode)tree.getModel().getRoot();
	 		Enumeration enum_ = root.preorderEnumeration();
	 		
			while ( enum_.hasMoreElements() )
			{
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)enum_.nextElement();
				
				Set set = (Set)node.getUserObject();

				if ( renamedConceptSets.containsKey( set ) )
					continue;
				else
				{
					renamedConceptSets.put(set, set );
					int eleCount = 1;
					for ( Iterator it = set.iterator(); it.hasNext(); )
					{
						OWLEntity ent = (OWLEntity)it.next();
						//System.out.println( ent.getURI() );
						if ( ent.getURI().toString().equals( "http://www.w3.org/2002/07/owl#Thing") )
							continue;
						
						String name = "C" + counter;
						if ( randomOrder )
						{
							String d = random.nextDouble() + "";
							d = d.substring(0, 5);
							name = "C" + d + counter;
						}
							
						//model.setSelectedEntity( ent );
						//System.out.println( ((OWLEntity)set.iterator().next()).toString() + " to C" + counter );
						if ( eleCount == 1)
						{
							model.renameOWLEntity( ont, ent, name );
							counter++;
						}
						else // equivalent classes
						{
							model.renameOWLEntity( ont, ent, name + "-" + eleCount);							
						}
						//counter++;
						eleCount++;
					}
				}
				
			}
			
		}
		catch ( Exception e )
		{ e.printStackTrace(); }
	
	}
	
	public static void main( String [] args )
	{
    	// Create model
    	AutomatedSwoopModel model = new AutomatedSwoopModel();
		// Create application frame.
		theFrame = new SwoopFrame(model);
		
		// Do Not show frame
		theFrame.setVisible( false );
		
		OntologyObfuscator obs = new OntologyObfuscator();
		
		//String [] params = { "file:/C:/Documents%20and%20Settings/Dave%20Wang/Desktop/ontologies/koala.owl", "http://cvs.mygrid.org.uk/cgi-bin/viewcvs.cgi/mygrid/feta/etc/sampleData/service.rdfs?rev=1.2" };
		//String [] params = { "http://protege.stanford.edu/plugins/owl/owl-library/koala.owl", "http://cvs.mygrid.org.uk/cgi-bin/viewcvs.cgi/mygrid/feta/etc/sampleData/service.rdfs?rev=1.2" };
		//String ont = "http://protege.stanford.edu/plugins/owl/owl-library/koala.owl";
		String ont = "http://www.mindswap.org/ontologies/debug-sweet-jpl.owl";
		//String ont = "http://www.mindswap.org/~tw7/temp/testOnt.owl";
		//String ont = "http://www.mindswap.org/ontologies/galen.owl";
		//String ont = "http://protege.stanford.edu/plugins/owl/owl-library/not-galen.owl";
		//String ont = "http://www.fruitfly.org/~cjm/obo-download/obo-all/event/event.owl";
		try
		{
			obs.obfuscateConcepts( model, ont, 1, true );
			Runtime.getRuntime().freeMemory();
			Runtime.getRuntime().gc();
		}
		catch ( Exception e )
		{ e.printStackTrace(); }
		
		theFrame.setVisible( true );
		//System.exit(0);
	}
}
