/*
 * Created on Sep 27, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.utils.treeexport;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import org.mindswap.swoop.SwoopModel;
import org.semanticweb.owl.model.OWLClass;

/**
 * @author Dave Wang
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

// Takes in a JTree representing class hierarchy, and serializes it out
public class TreeSerializer 
{
	
	private static Hashtable myQNameTable = null;
	private static Hashtable myURITable   = null;
	
	public static void export( JTree tree, String type, File file, SwoopModel swModel )
	{
		myQNameTable = new Hashtable();
		myURITable   = new Hashtable();
		
		if ( type.equals( TM3FileFilter.TM3 ) )
			exportToTM3( tree, file, swModel );
		else if ( type.equals( STFileFilter.ST ) )
			exportToST( tree, file, swModel );
	}	
	
	/* serializes to treemap format (.tm3) 
	 */
	public static void exportToTM3( JTree tree, File file, SwoopModel swModel )
	{
		if ( !file.getName().endsWith( TM3FileFilter.TM3 ))
		{
			System.out.println( "Filename is attached with " + TM3FileFilter.TM3 );
			file = new File(file.getPath() + TM3FileFilter.TM3);
		}
		try
		{
			System.out.println("Exporting to file " + file.getAbsolutePath() + "...");
			BufferedWriter writer = new BufferedWriter( new FileWriter( file ) );
			TreeModel model = tree.getModel();
			DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();
			Enumeration enum_ = root.breadthFirstEnumeration();
			int counter = 0;
			//System.out.println("ID");
			writer.write("ID");
			writer.newLine();
			//System.out.println("INTEGER");
			writer.write("INTEGER");
			writer.newLine();
			while ( enum_.hasMoreElements() )
			{
				//System.out.print( counter + "\t\t" );
				writer.write( counter + "\t\t" );
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)enum_.nextElement();
				TreeNode [] path = node.getPath();
				for ( int i = 0; i < path.length; i ++)
				{
					DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode)path[i];
					//System.out.print( getNodeLabel( currentNode, writer, swModel ) + "\t");
					writer.write( getNodeLabel( currentNode, writer, swModel ) + "\t" );					
				}
				//System.out.println();
				writer.newLine();
				counter++;
			}

			writer.flush();
			writer.close();
			System.out.println("exporting to " + file.getName() + "... done");
		}
		catch ( Exception e )
		{ e.printStackTrace(); }
	}
	
	public static void exportToST( JTree tree, File file, SwoopModel swModel )
	{
		if ( !file.getName().endsWith( STFileFilter.ST ))
		{
			System.out.println( "Filename is attached with " + STFileFilter.ST);
			file = new File(file.getPath() + STFileFilter.ST );
		}
		try
		{
			System.out.println("Exporting to file " + file.getAbsolutePath() + "...");
			BufferedWriter writer = new BufferedWriter( new FileWriter( file ) );
			TreeModel model = tree.getModel();
			DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();
			
			writer.write("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>");
			writer.newLine();
			printNodesInSP( root, writer, 0, swModel );

			writer.flush();
			writer.close();
			System.out.println("exporting to " + file.getName() + "... done");
		}
		catch ( Exception e )
		{ e.printStackTrace(); }
	}
	
	private static void printNodesInSP( DefaultMutableTreeNode node, BufferedWriter writer, 
										int indentLevel, SwoopModel swModel  )
	{
		int counter = indentLevel;
		String indentation = "";
		while (counter != 0)
		{
			indentation = indentation + "  ";
			counter--;
		}
		try
		{
			writer.write( indentation + "<node>" + getNodeLabel( node, writer, swModel) );
			Enumeration children = node.children();
			if ( children.hasMoreElements() )
			{
				writer.newLine();
				while ( children.hasMoreElements() )
				{
					DefaultMutableTreeNode child =(DefaultMutableTreeNode)children.nextElement(); 
					printNodesInSP( child, writer, indentLevel + 1, swModel );
				}
				writer.write( indentation + "</node>");
			}
			else
				writer.write("</node>");
			writer.newLine();
		}
		catch ( Exception e )
		{ e.printStackTrace(); }
		
	}
	
	private static String getNodeLabel( DefaultMutableTreeNode node, BufferedWriter writer, SwoopModel swModel)
	{
		Set set = (Set)node.getUserObject();
		Iterator it = set.iterator();
		String label = "";
		if ( set.size() > 1 )
			label = "[";
		
		int count = 0;
		try
		{
			while (it.hasNext())
			{
				OWLClass concept = (OWLClass)it.next();
				URI conceptURI = concept.getURI();
				String name = swModel.shortForm( conceptURI );
								
				// have seen this name before, check to see if the URI is used
				if ( myQNameTable.containsKey( name ))
				{
					// have seen this before, use its assigned name
					if ( myURITable.containsKey( conceptURI ) )
						name = (String)myURITable.get( conceptURI );
					else
					{
						name = createNewName( name );
						myURITable.put( conceptURI, name );
					}
				}
				else // first time seeing this name, remember it
				{ 
					myQNameTable.put( name, name ); 
					myURITable.put( conceptURI, name );
				}
				
				label = label + name;
				if (it.hasNext())
					label = label + ", ";
			}
		}
		catch ( Exception e )
		{ e.printStackTrace(); }
		
		if ( set.size() > 1 )
			label = label + "]";
		
		return label;
	}
	
	private static String createNewName( String name )
	{
		String newName = "";
		int i = 1;
		while ( true )
		{
		    newName = name + "[" + i + "]";
			if ( !myQNameTable.containsKey( newName ) )
			{
				System.out.println("Created new name for " + newName);
				break;
			}
		}
		return newName;
	}
}
