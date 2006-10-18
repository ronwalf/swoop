/*
 * Created on Jan 29, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.automation;

import java.io.BufferedWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.mindswap.swoop.utils.SwoopStatistics;
import org.semanticweb.owl.model.OWLClass;

/**
 * @author Dave Wang
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class StatsPrinter 
{
	/*
	 * methods to print to screen
	 * 
	 * 
	 */
	public static void printBasicStats( Hashtable stats )
	{
		System.out.println("Logical URI: " + stats.get( OntologyAnalyzer.LOGICAL_URI ) );
		System.out.println("Physical URI: " + stats.get( OntologyAnalyzer.PHYSICAL_URI ) );
		System.out.println("No. Defined Classes: " + stats.get( OntologyAnalyzer.NUM_DEFINED_CLASSES )  );
		System.out.println("No. Imported Classes: " + stats.get( OntologyAnalyzer.NUM_IMPORTED_CLASSES ) );
		System.out.println("No. Defined Datatype Properties: " + stats.get( OntologyAnalyzer.NUM_DEFINED_DATA_PROP ) );
		System.out.println("No. Imported Datatype Properties: " + stats.get( OntologyAnalyzer.NUM_IMPORTED_DATA_PROP ) );
		System.out.println("No. Defined Object Properties: " + stats.get( OntologyAnalyzer.NUM_DEFINED_OBJ_PROP ) );
		System.out.println("No. Imported Object Properties: " + stats.get( OntologyAnalyzer.NUM_IMPORTED_OBJ_PROP ) );
		System.out.println("No. Defined Annotation Properties: " + stats.get( OntologyAnalyzer.NUM_DEFINED_ANNO_PROP ) );
		System.out.println("No. Imported Annotation Properties: " + stats.get( OntologyAnalyzer.NUM_IMPORTED_ANNO_PROP ) );
		System.out.println("No. Link Properties" + stats.get( OntologyAnalyzer.NUM_LINKED_PROP ) );
		System.out.println("No. Defined Individuals: " + stats.get( OntologyAnalyzer.NUM_DEFINED_IND ) );
		System.out.println("No. Imported Individuals: " + stats.get( OntologyAnalyzer.NUM_IMPORTED_IND ) );
		System.out.println("No. Type Assertions: " + stats.get( OntologyAnalyzer.NUM_TYPE_ASSERTIONS ) );
		System.out.println("No. Individual Datatype Assertions: " + stats.get( OntologyAnalyzer.NUM_DPROP_ASSERTIONS ) );
		System.out.println("No. Individual Object Assertions: " + stats.get( OntologyAnalyzer.NUM_OPROP_ASSERTIONS ) );
	}

	
	public static void printAdvancedStats( HashMap advStats )
	{
		Set unsat = (HashSet) advStats.get(SwoopStatistics.UNSATISFIABLE_CLASSES);
    	Set gci = (HashSet) advStats.get(SwoopStatistics.NO_GCI);
        String subsumptions = (String) advStats.get(SwoopStatistics.NO_SUBSUMPTIONS);
    	Set disj = (HashSet) advStats.get(SwoopStatistics.NO_DISJOINT);            
        Set func = (HashSet) advStats.get(SwoopStatistics.NO_FUNCTIONAL);
        Set ifunc = (HashSet) advStats.get(SwoopStatistics.NO_INVFUNCTIONAL);
        Set tran = (HashSet) advStats.get(SwoopStatistics.NO_TRANSITIVE);
        Set symm = (HashSet) advStats.get(SwoopStatistics.NO_SYMMETRIC);
        Set inv = (HashSet) advStats.get(SwoopStatistics.NO_INVERSE);
        
        // class tree depth
        List maxCList = (List) advStats.get(SwoopStatistics.MAX_DEPTH_CLASS_TREE);
        List minCList = (List) advStats.get(SwoopStatistics.MIN_DEPTH_CLASS_TREE);
        String avgC = advStats.get(SwoopStatistics.AVG_DEPTH_CLASS_TREE).toString();
        // class tree branching factor
        List maxBList = (List) advStats.get(SwoopStatistics.MAX_BRANCHING_FACTOR);
        List minBList = (List) advStats.get(SwoopStatistics.MIN_BRANCHING_FACTOR);
        String avgB = advStats.get(SwoopStatistics.AVG_BRANCHING_FACTOR).toString();
        // prop tree depth
        List maxPList = (List) advStats.get(SwoopStatistics.MAX_DEPTH_PROP_TREE);
        List minPList = (List) advStats.get(SwoopStatistics.MIN_DEPTH_PROP_TREE);
        String avgP = advStats.get(SwoopStatistics.AVG_DEPTH_PROP_TREE).toString();
        // multiple inheritance (class and prop)
        Set multC = (HashSet) advStats.get(SwoopStatistics.MULTIPLE_INHERITANCE_CLASS);
        Set multP = (HashSet) advStats.get(SwoopStatistics.MULTIPLE_INHERITANCE_PROP);
                
        // expressivity
        System.out.println("DL Expressivity: " + advStats.get( OntologyAnalyzer.EXPRESSIVITY) );
        
        if ( unsat == null )
        	System.out.println("No. Unsatisfiable Classes: 0");
        else
        	System.out.println("No. Unsatisfiable Classes: " + unsat.size() );
		System.out.println("No. GCI's : " + gci.size() );
		System.out.println("No. Subsumptions: " + subsumptions );
		System.out.println("No. Disjoint Axioms: " + disj.size() );
		System.out.println("No. Functional Props: " + func.size() );
		System.out.println("No. Inverse-Functional Props: " + ifunc.size() );
		System.out.println("No. Transitive Props: " + tran.size() );
		System.out.println("No. Symmetric Props: " + symm.size() );
		System.out.println("No. Inverse Props: " + inv.size() );
		System.out.println("Max Class Tree Depth: " + maxCList.get(0) );
		System.out.println("Min Class Tree Depth: " + minCList.get(0) );
		System.out.println("Avg Class Tree Depth: " + avgC );
		System.out.println("Max Class Tree Branching Factor: " + maxBList.get(0) );
		System.out.println("Min Class Tree Branching Factor: " + minBList.get(0) );
		System.out.println("Avg Class Tree Branching Factor: " + avgB );
		System.out.println("Max Prop Tree Depth: " + maxPList.get(0) );
		System.out.println("Min Prop Tree Depth: " + minPList.get(0) );
		System.out.println("Avg Prop Tree Depth: " + avgP );
		System.out.println("No. Class Multiple Inheritance: " + multC.size() );
		System.out.println("No. Property Multiple Inheritance: " + multP.size() );
		System.out.println("Is Consistent? " + advStats.get( OntologyAnalyzer.CONSISTENCY ) );
		System.out.println("Graph Morphology: " + advStats.get( OntologyAnalyzer.GRAPH_MORPHOLOGY ) );		
	}

	public static void printToFileTabDelim( BufferedWriter writer, Hashtable basicStats, HashMap toldAdvStats, HashMap pelletAdvStats )
	{
		try
		{
			writeStaticStatsToFile( writer, basicStats, toldAdvStats );
			writeToldAndReasonedDynamicStats( writer, toldAdvStats, pelletAdvStats );
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}		
	}	
	
	/* Writing the categories of each stats.
	 */
	public static void writeFileHeader( BufferedWriter writer )
	{
		try
		{
			writer.write("Logical URI\t");
			writer.write("Physical URI\t");
			
			writer.write("No. Defined Classes\t");
			writer.write("No. Imported Classes\t");
			writer.write("No. Defined Datatype Props\t");
			writer.write("No. Imported Datatype Props\t");
			writer.write("No. Defined Object Props\t");
			writer.write("No. Imported Object Props\t");
			writer.write("No. Defined Annotation Props\t");
			writer.write("No. Imported Annotation Props\t");
			writer.write("No. Linked Props\t");
			writer.write("No. Defined Individuals\t");
			writer.write("No. Imported Individuals Individuals\t");
			
			writer.write("No. GCIs\t");
			writer.write("No. Functional Props\t");
			writer.write("No. Inverse Functional Props\t");
			writer.write("No. Transitive Props\t");
			writer.write("No. Symmetric Props\t");
			writer.write("No. Inverse Props\t");
			
			writer.write("No. Type assertions\t");
			writer.write("No. Ind Data Assertions\t");
			writer.write("No. Ind Obj Assertions\t");
			
			// told ontology tree stats
			writer.write("DL Expressivity \t");
			writer.write("No. Unsatisfiable Classes\t");
			writer.write("No. Subsumptions\t");
			writer.write("No. Disjoint Axioms\t");
			writer.write("Max Class Tree Depth\t");
			writer.write("Min Class Tree Depth\t");
			writer.write("Avg Class Tree Depth\t");
			writer.write("Max Class Tree Branching Factor\t");
			writer.write("Min Class Tree Branching Factor\t");
			writer.write("Avg Class Tree Branching Factor\t");
			writer.write("Max Prop Tree Depth\t");
			writer.write("Min Prop Tree Depth\t");
			writer.write("Avg Prop Tree Depth\t");
			writer.write("No. Class Multiple Inheritance\t");
			writer.write("No. Property Multiple Inheritance\t");
			writer.write("Consistent?\t");
			writer.write("Graph Morphology\t");
			
			// pellet ontology tree stats
			writer.write("DL Expressivity \t");
			writer.write("No. Unsatisfiable Classes\t");
			writer.write("No. Subsumptions\t");
			writer.write("No. Disjunctions\t");
			writer.write("Max Class Tree Depth\t");
			writer.write("Min Class Tree Depth\t");
			writer.write("Avg Class Tree Depth\t");
			writer.write("Max Class Tree Branching Factor\t");
			writer.write("Min Class Tree Branching Factor\t");
			writer.write("Avg Class Tree Branching Factor\t");
			writer.write("Max Prop Tree Depth\t");
			writer.write("Min Prop Tree Depth\t");
			writer.write("Avg Prop Tree Depth\t");
			writer.write("No. Class Multiple Inheritance\t");
			writer.write("No. Property Multiple Inheritance\t");
			writer.write("Consistent?\t");
			writer.write("Graph Morphology\t");
			
			// extra stuff
			// writer.write("Pellet Classification Time");
			
			writer.newLine();
			
			writer.flush();
		}
		catch ( Exception e )
		{ e.printStackTrace(); }
	}
	
	/*
	 * Writes out stats that do not change when reasoner is turned on.
	 *  Takes in a basicStats hashmap and an advancedstats hashmap 
	 */
	private static void writeStaticStatsToFile( BufferedWriter writer, Hashtable basicStats, HashMap advStats )
	{
		try
		{
			writer.write( (String)basicStats.get( OntologyAnalyzer.LOGICAL_URI ) + "\t" );
			writer.write( (String)basicStats.get( OntologyAnalyzer.PHYSICAL_URI ) + "\t" );
			
			writer.write( (String)basicStats.get( OntologyAnalyzer.NUM_DEFINED_CLASSES ) + "\t" );
			writer.write( (String)basicStats.get( OntologyAnalyzer.NUM_IMPORTED_CLASSES ) + "\t" );
			writer.write( (String)basicStats.get( OntologyAnalyzer.NUM_DEFINED_DATA_PROP ) + "\t" );
			writer.write( (String)basicStats.get( OntologyAnalyzer.NUM_IMPORTED_DATA_PROP ) + "\t" );
			writer.write( (String)basicStats.get( OntologyAnalyzer.NUM_DEFINED_OBJ_PROP ) + "\t" );
			writer.write( (String)basicStats.get( OntologyAnalyzer.NUM_IMPORTED_OBJ_PROP ) + "\t" );
			writer.write( (String)basicStats.get( OntologyAnalyzer.NUM_DEFINED_ANNO_PROP ) + "\t" );
			writer.write( (String)basicStats.get( OntologyAnalyzer.NUM_IMPORTED_ANNO_PROP ) + "\t" );
			writer.write( (String)basicStats.get( OntologyAnalyzer.NUM_LINKED_PROP ) + "\t" );
			writer.write( (String)basicStats.get( OntologyAnalyzer.NUM_DEFINED_IND ) + "\t" );
			writer.write( (String)basicStats.get( OntologyAnalyzer.NUM_IMPORTED_IND ) + "\t" );
			
			writer.write( ((HashSet) advStats.get(SwoopStatistics.NO_GCI)).size() + "\t" );
			writer.write( ((HashSet) advStats.get(SwoopStatistics.NO_FUNCTIONAL)).size() + "\t" );
			writer.write( ((HashSet) advStats.get(SwoopStatistics.NO_INVFUNCTIONAL)).size() + "\t" );
			writer.write( ((HashSet) advStats.get(SwoopStatistics.NO_TRANSITIVE)).size() + "\t" );
			writer.write( ((HashSet) advStats.get(SwoopStatistics.NO_SYMMETRIC)).size() + "\t" );
			writer.write( ((HashSet) advStats.get(SwoopStatistics.NO_INVERSE)).size() + "\t" );

			writer.write( (String)basicStats.get( OntologyAnalyzer.NUM_TYPE_ASSERTIONS ) + "\t");
			writer.write( (String)basicStats.get( OntologyAnalyzer.NUM_DPROP_ASSERTIONS ) + "\t");
			writer.write( (String)basicStats.get( OntologyAnalyzer.NUM_OPROP_ASSERTIONS ) + "\t");

			writer.flush();
		}		
		catch ( Exception e )
		{ e.printStackTrace(); }		 
	}
	
	private static void writeToldAndReasonedDynamicStats( BufferedWriter writer, HashMap toldAdvStats, HashMap pelletAdvStats )
	{
		try
		{
			writeDynamicStats( writer, toldAdvStats );
			if ( pelletAdvStats != null)
				writeDynamicStats( writer, pelletAdvStats );
			writer.newLine();
			writer.flush();
		}
		catch ( Exception e )
		{ e.printStackTrace(); }
	}

	/*
	 * write out the dynamic portions of just one advanced stats
	 * 
	 */
	private static void writeDynamicStats( BufferedWriter writer, HashMap advStats )
	{
		try
		{        	
			Set unsat = (HashSet) advStats.get(SwoopStatistics.UNSATISFIABLE_CLASSES);
	    	Set gci = (HashSet) advStats.get(SwoopStatistics.NO_GCI);
	        String subsumptions = (String) advStats.get(SwoopStatistics.NO_SUBSUMPTIONS);
	    	Set disj = (HashSet) advStats.get(SwoopStatistics.NO_DISJOINT);            
	        Set func = (HashSet) advStats.get(SwoopStatistics.NO_FUNCTIONAL);
	        Set ifunc = (HashSet) advStats.get(SwoopStatistics.NO_INVFUNCTIONAL);
	        Set tran = (HashSet) advStats.get(SwoopStatistics.NO_TRANSITIVE);
	        Set symm = (HashSet) advStats.get(SwoopStatistics.NO_SYMMETRIC);
	        Set inv = (HashSet) advStats.get(SwoopStatistics.NO_INVERSE);
	        
	        // class tree depth
	        List maxCList = (List) advStats.get(SwoopStatistics.MAX_DEPTH_CLASS_TREE);
	        List minCList = (List) advStats.get(SwoopStatistics.MIN_DEPTH_CLASS_TREE);
	        String avgC = advStats.get(SwoopStatistics.AVG_DEPTH_CLASS_TREE).toString();
	        
	        // class tree branching factor
	        List maxBList = (List) advStats.get(SwoopStatistics.MAX_BRANCHING_FACTOR);
	        List minBList = (List) advStats.get(SwoopStatistics.MIN_BRANCHING_FACTOR);
	        String avgB = advStats.get(SwoopStatistics.AVG_BRANCHING_FACTOR).toString();
	        // prop tree depth
	        List maxPList = (List) advStats.get(SwoopStatistics.MAX_DEPTH_PROP_TREE);
	        List minPList = (List) advStats.get(SwoopStatistics.MIN_DEPTH_PROP_TREE);
	        String avgP = advStats.get(SwoopStatistics.AVG_DEPTH_PROP_TREE).toString();
	        
	        // multiple inheritance (class and prop)
	        Set multC = (HashSet) advStats.get(SwoopStatistics.MULTIPLE_INHERITANCE_CLASS);
	        Set multP = (HashSet) advStats.get(SwoopStatistics.MULTIPLE_INHERITANCE_PROP);	        
	        
	        String exprShort = (String)advStats.get( OntologyAnalyzer.EXPRESSIVITY );
			writer.write( exprShort + "\t");
			
	        if ( unsat == null )
	        	writer.write("0\t");
	        else
	        	writer.write( unsat.size() + "\t" );
	        
			writer.write( subsumptions + "\t" );
			writer.write( disj.size() + "\t" );
			writer.write( maxCList.get(0) + "\t" );
			writer.write( minCList.get(0) + "\t" );
			writer.write( avgC + "\t" );
			writer.write( maxBList.get(0) + "\t" );
			writer.write( minBList.get(0) + "\t" );
			writer.write( avgB + "\t" );
			writer.write( maxPList.get(0) + "\t");
			writer.write( minPList.get(0) + "\t");
			writer.write( avgP + "\t");
			writer.write( multC.size() + "\t");
			writer.write( multP.size() + "\t");
			writer.write( (String)advStats.get( OntologyAnalyzer.CONSISTENCY) + "\t");
			writer.write( (String)advStats.get( OntologyAnalyzer.GRAPH_MORPHOLOGY ) + "\t");
			
			writer.flush();
		}
		catch ( Exception e )
		{ e.printStackTrace(); }
	}	
}
