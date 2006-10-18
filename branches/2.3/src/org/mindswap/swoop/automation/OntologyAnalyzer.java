/*
 * Created on Dec 23, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.automation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.mindswap.swoop.SwoopFrame;
import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.TermsDisplay;
import org.mindswap.swoop.reasoner.PelletReasoner;
import org.mindswap.swoop.reasoner.SwoopReasoner;
import org.mindswap.swoop.reasoner.SwoopToldReasoner;
import org.mindswap.swoop.renderer.ontology.SwoopOntologyInfo;
import org.mindswap.swoop.utils.SwoopStatistics;
import org.mindswap.swoop.utils.owlapi.CorrectedRDFRenderer;
import org.semanticweb.owl.io.RendererException;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLProperty;


/**
 * @author Dave Wang
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class OntologyAnalyzer 
{
	
	static class AncestorResult
	{
		public HashSet myResult   = null;
		public boolean myHasCycle = false;
		
		AncestorResult( HashSet set, boolean hasCycle )
		{
			myResult = set;
			myHasCycle = hasCycle;
		}
	}
	
	public static final String LOGICAL_URI = "LOGICALURI";
	public static final String PHYSICAL_URI = "PHYSICALURI";
	public static final String NUM_IMPORTED_CLASSES = "NUMIMPORTEDCLASSES";
	public static final String NUM_DEFINED_CLASSES  = "NUMDEFINEDCLASSES";
	public static final String NUM_IMPORTED_DATA_PROP = "NUMIMPORTEDDATAPROPS";
	public static final String NUM_DEFINED_DATA_PROP  = "NUMDEFINEDDATAPROPS";
	public static final String NUM_IMPORTED_OBJ_PROP  = "NUMIMPORTEDOBJPROPS";
	public static final String NUM_DEFINED_OBJ_PROP   = "NUMDEFINEDOBJPROPS";
	public static final String NUM_IMPORTED_ANNO_PROP = "NUMIMPORTEDANNOPROPS";
	public static final String NUM_DEFINED_ANNO_PROP  = "NUMDEFINEDANNOPROPS";
	public static final String NUM_LINKED_PROP        = "NUMLINKEDPROPS";
	public static final String NUM_IMPORTED_IND       = "NUMIMPORTEDIND";
	public static final String NUM_DEFINED_IND       = "NUMDEFINEDIND";	
	public static final String CONSISTENCY           = "CONSISTENCY";
	public static final String EXPRESSIVITY          = "EXPRESSIVITY";
	public static final String GRAPH_MORPHOLOGY      = "GRAPHMORPHOLOGY";

	// graph morphology categories
	public static final String LIST      = "List";
	public static final String LISTS     = "Lists";
	public static final String TREE      = "Tree";
	public static final String TREES     = "Trees";
	public static final String MULTITREE = "Multitree";
	public static final String DAG       = "DAG";
	public static final String GRAPH     = "Graph";
	public static final String NONE      = "None";
	public static final String INCONSISTENT = "INCONSISTENT";
	
	// individual stats constants
	public static final String NUM_TYPE_ASSERTIONS = "NUMTYPEASSERTIONS";
	public static final String NUM_DPROP_ASSERTIONS = "NUMDPROPASSERTIONS";
	public static final String NUM_OPROP_ASSERTIONS = "NUMOPROPASSERTIONS";
	
	public static final String OWLTHING  = "http://www.w3.org/2002/07/owl#Thing";
	
    public static SwoopFrame theFrame;
        
    public static boolean IS_DEBUG = false;
    public static boolean INFERRED_STATS = false; // whether to get reasoned stats
    
	public Vector analyzeInput(AutomatedSwoopModel model, String[] args) 
	{
		Vector analyses = new Vector();
		for (int i = 0; i < args.length; i++) {
			try 
			{
				OntologyAnalysis oa = analyze( model, new URI(args[i]) );
				SwoopFrame frame = model.getFrame();
				OWLOntology ontology = model.getSelectedOntology();
				model.clearCaches( ontology );             // force clearing!
				model.removeOntStats( ontology );          // force clearing!
				model.removeOntology( ontology.getURI() ); // force removing!
        		frame.termDisplay.removeFromCache(ontology);
				frame.clearWorkspace( false );             // just in case

				//model.getFrame().clearWorkspace( false ); // false for "No popup confirmation"
			} 
			catch (Exception exception) 
			{
				exception.printStackTrace();
				if ( IS_DEBUG )
					System.out.println(" * OntologyAnalyzer: Could not load ontology " + args[i]);
			}
		}
		return analyses;
	}
	
	public Vector analyzeInput(AutomatedSwoopModel model, BufferedReader reader, String outputfile, String logfilename ) 
	{ return analyzeInput(model, reader, outputfile, 1, logfilename); }
	
	/*
	 * pass in null for logfilename if no logging is desired
	 */
	public Vector analyzeInput(AutomatedSwoopModel model, BufferedReader reader, String outputfile, int startline, String logfilename ) 
	{
		if ( startline < 1 )
			startline = 1;
		
		int count = startline; // keep track of how many ontologies we looked at
		
		Vector analyses = new Vector();
		String line = null;
		
		// setting up log file option
		boolean isToLog = true;
		if ( logfilename == null )
			isToLog = false;
		BufferedWriter logWriter = null;
		
		try 
		{
			// setting up log file
			if ( isToLog )
				logWriter = new BufferedWriter( new FileWriter( logfilename ) );
			
			// setting up output file
			BufferedWriter writer = new BufferedWriter( new FileWriter( outputfile ) );
			StatsPrinter.writeFileHeader( writer );
			
			// skip number of lines given
			for ( int i = 1; i < startline; i++ )
				reader.readLine();
						
			int analyzedOntologies   = 0;
			int exceptionedOntologies = 0;
			int skippedOntologies     = 0;
			
			while ( ( line = reader.readLine() ) != null )
			{
				try
				{
					// if line starts with "*", we skip it
					if ( line.startsWith("*"))
					{
						if ( isToLog )
						{
							logWriter.write( "[" + count + "] Skipping <" + line  + ">... ");
							logWriter.newLine();
							skippedOntologies++;
						}
						continue;
					}

					if ( isToLog )
					{
						logWriter.write( "[" + count + "] Analyzing <" + line  + ">... ");
						logWriter.newLine();
						logWriter.flush();
					}
					
					long startTime = System.currentTimeMillis();
					OntologyAnalysis oa = analyze( model, new URI(line) );
					
					// pelletStats can be null if INFERRED_STATS is set to false;
					if (( oa.getPelletStats() != null ) && ( isRDFS( oa ) ))
					{
						System.out.println(" >>>> IS rdfs, fixing... ");
						if ( isToLog )
						{
							logWriter.write( "  - RDFS vocabulary detected instead of owl.  Attempting to fix and reanalyze...");
							logWriter.newLine();
							logWriter.flush();
						}
						oa = analyze( model, new URI(line), true );
					}
					StatsPrinter.printToFileTabDelim( writer, oa.getBasicStats(), oa.getToldStats(), oa.getPelletStats() );
					long stopTime = System.currentTimeMillis();
					double runTime = (stopTime - startTime)/1000;
					analyzedOntologies++;
					if ( isToLog )
					{
						logWriter.write( "  Successful.  Took " + runTime + " seconds.");
						logWriter.newLine();
					}

				}
				catch ( URISyntaxException e )
				{
					e.printStackTrace();
					String trace = Utils.getExceptionTrace( e );
					exceptionedOntologies++;
					writer.newLine(); // write new line so output lines up with input
					if ( isToLog )
					{
						logWriter.write( trace );
						logWriter.newLine();
					}
				}
				catch ( AnalysisException e )
				{
					e.printStackTrace();
					String trace = Utils.getExceptionTrace( e );
					exceptionedOntologies++;
					writer.newLine(); // write new line so output lines up with input
					if ( isToLog )
					{
						logWriter.write( trace );
						logWriter.newLine();
						logWriter.write( " - Caused by " + e.toString() );
						logWriter.newLine();
					}
				}
				finally
				{
					// clean up workspace
					// increment count
					if (isToLog)
					{
						logWriter.newLine();
						logWriter.flush();
					}
					model.getFrame().clearWorkspace( false ); // false for "No popup confirmation"
					count++;
				}
			}
			
			if ( isToLog )
			{
				logWriter.write("Analyzed Ontologies: " + analyzedOntologies );
				logWriter.newLine();
				logWriter.write("Exceptioned Ontologies: " + exceptionedOntologies );
				logWriter.newLine();
				logWriter.write("Skipped Ontologies: " + skippedOntologies );
				logWriter.newLine();
				logWriter.flush();
			}
		} 
		catch ( IOException e ) 
		{
			e.printStackTrace();
			System.err.println("Unrecoverable IOException... halting. ");
			if ( IS_DEBUG )
				System.out.println(" * OntologyAnalyzer: Could not load ontology " + line );
		}
		return analyses;
	}	
	
	
	public OntologyAnalysis analyze( AutomatedSwoopModel model, URI ontURI ) throws AnalysisException
	{ return analyze( model, ontURI, false); }
	
	public OntologyAnalysis analyze( AutomatedSwoopModel model, URI ontURI, boolean isRDFS ) throws AnalysisException
	{
		TermsDisplay display = model.getFrame().termDisplay;
        OWLOntology ont = null;
		try
		{		
			ont = model.loadOntology( ontURI );
			model.setReasonerWithThreadBlock( new SwoopToldReasoner() );
			model.setSelectedOntology( ont );
			model.setShowImportsWithThreadBlock( true );
			
			if ( isRDFS ) // RDFS, we fix the ont by serializing it to OWL, and reload it to the model
			{
				StringWriter st = new StringWriter();
				CorrectedRDFRenderer rdfRenderer = new CorrectedRDFRenderer();
				
				// source code in RDF
				String code = "";
				try
				{
					rdfRenderer.renderOntology(ont, st);
					code = st.toString();
				}
				catch ( RendererException ex )
				{
					ex.printStackTrace();
					throw new AnalysisException("Failed in trying to analyze (Fixing RDFS vocaublary) <OWLException>: " + ex.toString() + "\n" + Utils.getExceptionTrace( ex ) );
				}
				
				StringReader reader = new StringReader( code );
	    		OWLOntology ontology = model.getSelectedOntology();
	    		
	    		// add updated ontology 
				OWLOntology updatedOntology = null;
				updatedOntology = model.loadOntologyInRDF(reader, ontology.getURI());
	    		// restore old ontology, if exception loading new one
	    		if (updatedOntology==null) 
	    		{
					throw new AnalysisException("Failed in trying to analyze (Updating RDFS vocaublary) <OWLException>" );
	    		}
	    		else {
	    			// remove current ontology and clear its cache
	    			
					SwoopFrame frame = model.getFrame();
					model.clearCaches( ontology );             // force clearing!
					model.removeOntStats( ontology );          // force clearing!
					model.removeOntology( ontology.getURI() ); // force removing!
	        		frame.termDisplay.removeFromCache(ontology);
					frame.clearWorkspace( false );        // just in case
					
	        		model.addOntology(updatedOntology);
	        		model.setSelectedOntology(updatedOntology);
	    		}
				
				/*
				BufferedWriter writer = new BufferedWriter( new FileWriter( "tempOntology.owl"));
				writer.write( code );
				writer.flush();
				writer.close();
				SwoopFrame frame = model.getFrame();
				
				model.clearCaches( ont );             // force clearing!
				model.removeOntStats( ont );          // force clearing!
				model.removeOntology( ont.getURI() ); // force removing!
				frame.clearWorkspace( false );        // just in case
				File file = new File("tempOntology.owl");
				
				ont = model.loadOntology( file.toURI() );
				model.setSelectedOntology( ont );
				model.setReasonerWithThreadBlock( new SwoopToldReasoner() );
				model.setShowImportsWithThreadBlock( true );
				*/
	  			}
		}
		catch ( OWLException e )
		{
			e.printStackTrace();
			throw new AnalysisException("Failed in trying to analyze (Setting ontology and imports) <OWLException>: " + e.toString() + "\n" + Utils.getExceptionTrace( e ) );
		}
 		catch ( Exception e )
		{
			e.printStackTrace();
			throw new AnalysisException("Failed in trying to analyze (Loading Ontology) <Exception>: " + e.toString() + "\n" + Utils.getExceptionTrace( e ) );
		}
		
 		try
		{
 			Thread.sleep( 5000 );
		}
 		catch ( Exception e )
		{ e.printStackTrace(); }
 		
		SwoopReasoner reasoner = model.getReasoner();

		// construct class tree
		display.termTabPane.setSelectedIndex( 0 );
		display.updateTreeDisplay();
		// construct prop tree
		display.termTabPane.setSelectedIndex( 1 );
		display.updateTreeDisplay();
		
		Hashtable stats = this.getBasicStats( model, ont );
		if ( IS_DEBUG )
			System.out.println("Doing Individual Stats");
		getIndividualStats( reasoner, ont, stats);

		if ( IS_DEBUG )
			System.out.println("Doing Advanced Stats");
		
		HashMap advStats = model.getOntStats( ont );
		String expressivity = getExpressivity();
		advStats.put( EXPRESSIVITY, expressivity);
		HashMap advReasonedStats = null;
		
		// check to see if it's consistent
		saveConsistency( advStats, model );
		// check graph structure
		doGraphAnalysis( advStats, model, ont);
		
		if ( IS_DEBUG )
		{
			StatsPrinter.printBasicStats( stats );
			StatsPrinter.printAdvancedStats( advStats );
			System.out.println("----");
		}

		// now we clear the stats cache so the reasoned stats won't mess with the told stats
		model.removeOntStatsWithoutNotification( ont );
		
		if ( !INFERRED_STATS )
		{
			System.err.println(" *** Inferred Stats have been disabled [check OntologyAnalyzer.INFERRED_STATS] ***");
			return new OntologyAnalysis(  stats, advStats, null );
		}
		
		try
		{
			model.setReasonerWithThreadBlock( new PelletReasoner() );
		}
		catch ( Exception e )
		{
			e.printStackTrace();
			throw new AnalysisException("Failed in trying to analyze( creating Pellet Reasoner) <Exception>: " + e.toString() + "\n" + Utils.getExceptionTrace( e ) );
		}
		// construct class tree
		display.termTabPane.setSelectedIndex( 0 );
		display.updateTreeDisplay();
		// construct prop tree
		display.termTabPane.setSelectedIndex( 1 );
		display.updateTreeDisplay();
		advReasonedStats = model.getOntStats( ont );
		
		expressivity = getExpressivity();
		advReasonedStats.put( EXPRESSIVITY, expressivity);
		
		// check to see if it's consistent
		saveConsistency( advReasonedStats, model );
		// check graph structure
		doGraphAnalysis( advReasonedStats, model, ont);
		
		if ( IS_DEBUG )
			StatsPrinter.printAdvancedStats( advReasonedStats );
		
		return new OntologyAnalysis(  stats, advStats, advReasonedStats);
	}

	/* Computes statistics for individuals of an ontology
	 *  - # type assertions
	 *  - # data prop assertions
	 *  - # obj prop assertions 
	 */
	public static Hashtable getIndividualStats( SwoopReasoner reasoner, OWLOntology ont, Hashtable stats ) throws AnalysisException
	{
		try
		{
			int numTypeAssertions = 0;
			int numDataPropAssertions = 0;
			int numObjPropAssertions  = 0;
			
			if ( reasoner.getOntology() == null )
			{
				System.out.println(" -.getIndividualStats: Ontology = null");
				System.exit(1);
			}
			if ( reasoner.getOntology().getOWLDataFactory() == null)
			{
				System.out.println(" -.getIndividualStats: DATAFACTORY= null");
				System.exit(1);
			}
			OWLClass owlThing = reasoner.getOntology().getOWLDataFactory().getOWLThing();
			Set inds = ont.getIndividuals();
			for ( Iterator it = inds.iterator(); it.hasNext(); )
			{
				OWLIndividual ind = (OWLIndividual)it.next();
				
				Set types = ind.getTypes( ont );
				types.remove( owlThing );
				numTypeAssertions = numTypeAssertions + types.size();
				
				Map objVals = ind.getObjectPropertyValues( ont );
				Set Objkeys = objVals.keySet();
				for (Iterator iter = Objkeys.iterator(); iter.hasNext(); )
				{
					HashSet obj = (HashSet)objVals.get( iter.next() ) ;
					numObjPropAssertions = numObjPropAssertions + obj.size(); 
				}
	
				Map dataVals = ind.getDataPropertyValues( ont );
				Set DataKeys = dataVals.keySet();
				for (Iterator iter = DataKeys.iterator(); iter.hasNext(); )
				{
					HashSet obj = (HashSet)dataVals.get( iter.next() ) ;
					numDataPropAssertions = numDataPropAssertions + obj.size();
				}
			}
			stats.put( NUM_TYPE_ASSERTIONS, numTypeAssertions + "");
			stats.put( NUM_DPROP_ASSERTIONS, numDataPropAssertions + "");
			stats.put( NUM_OPROP_ASSERTIONS, numObjPropAssertions + "");			
			return stats;
		}
		catch ( Exception e )
		{
			e.printStackTrace();
			throw new AnalysisException("Failed in Individual Stats Checking: " + e.toString() + "\n" + Utils.getExceptionTrace( e ) );
		}
	}
	
	/* see if ontology is consistent.  
	 */
	private static HashMap saveConsistency( HashMap advStats, SwoopModel model ) throws AnalysisException
	{
		try
		{
			SwoopReasoner reasoner = model.getReasoner();
			if ( reasoner.isConsistent() )
				advStats.put( OntologyAnalyzer.CONSISTENCY, "true");
			else
				advStats.put( OntologyAnalyzer.CONSISTENCY, "false");			
			return advStats;
		}
		catch ( Exception e )
		{
			e.printStackTrace();
			throw new AnalysisException("Failed in extracting Consistency Stats: " + e.toString() + "\n" + Utils.getExceptionTrace( e ) );			
		}
	}
	
	/* Decides what kind of graph morphology this onotlogy is.
	 *   can be one of { list, tree, multitree, DAG}
	 */
	private static HashMap doGraphAnalysis( HashMap advStats, SwoopModel model, OWLOntology ont ) throws AnalysisException
	{
		try
		{
			if (IS_DEBUG)
				System.out.println("Doing Graph Analysis");

			// get reasoner
			SwoopReasoner reasoner = model.getReasoner();
			//System.out.println("<<<<<< Reasoner type: " + reasoner.getClass().getName() );
			
			// get multiple inheritance stats
			Set multC = (HashSet) advStats.get(SwoopStatistics.MULTIPLE_INHERITANCE_CLASS);
			
			// if is inconsistent, there is no real semantic structure :/
			String consistency = (String)advStats.get( CONSISTENCY );
			if ( consistency.equals("false") )
			{
				advStats.put( GRAPH_MORPHOLOGY, INCONSISTENT );
				return advStats;
			}
			
			// if it has NO class structure, then return NONE
			OWLClass owlThing   = reasoner.getOntology().getOWLDataFactory().getOWLThing();
			OWLClass owlNothing = reasoner.getOntology().getOWLDataFactory().getOWLNothing();
			
			Set topLevels = reasoner.subClassesOf( owlThing );
			
			// if owl:Thing has no children (toldreasoner will say this) or owl:Thing has owl:Nothing as only child (pellet says this)
			if ( topLevels.size() == 0 )
			{
				//System.out.println(" *** Told says NONE");
				advStats.put( GRAPH_MORPHOLOGY, NONE);
				return advStats;					
			}
			if ( topLevels.size() == 1 )
			{
				HashSet hs = (HashSet)topLevels.iterator().next();
				OWLClass cls = (OWLClass)hs.iterator().next();
				if ( cls.getURI().toString().equals( owlNothing.getURI().toString()) )
				{
					//System.out.println(" *** Reasoned says None");
					advStats.put( GRAPH_MORPHOLOGY, NONE);
					return advStats;				
				}
			}
			
			boolean hasMultipleTop = ( (reasoner.subClassesOf( owlThing )).size() > 1);
			// Either a list or a tree
			if ( (multC == null) || (multC.size() == 0))
			{
				Set classes = ont.getClasses();
				for ( Iterator it = classes.iterator(); it.hasNext(); )
				{
					OWLClass cls = (OWLClass)it.next();
					//System.out.println( "++++" + cls.getURI().toString() );
					Set children = reasoner.subClassesOf( cls );
					if ( children == null )
						continue;
					else if ( children.size() > 1 )
					{
						if ( hasMultipleTop )
							advStats.put( GRAPH_MORPHOLOGY, TREES);	
						else
							advStats.put( GRAPH_MORPHOLOGY, TREE);
						return advStats;
					}
					//if ( children != null )
					//	System.out.println( "++++" + cls.getURI().toString() + " has " + children.size() + " children");
				}
				if ( hasMultipleTop )
					advStats.put( GRAPH_MORPHOLOGY, LISTS );
				else
					advStats.put( GRAPH_MORPHOLOGY, LIST );
				return advStats;
			}
			else // either a multitree or a (nore general) directed acyclic graph
			{				
				HashSet MIClasses = (HashSet)advStats.get( SwoopStatistics.MULTIPLE_INHERITANCE_CLASS );
				for ( Iterator it = MIClasses.iterator(); it.hasNext(); )
				{
					OWLClass cls = (OWLClass)it.next();
					
					HashSet checkedSupers = new HashSet();
					
					Set supers = reasoner.superClassesOf( cls );
					for ( Iterator iterator = supers.iterator(); iterator.hasNext(); )
					{
						Set set = (Set)iterator.next();
						for ( Iterator its = set.iterator(); its.hasNext(); )
						{
							OWLClass sup = (OWLClass)its.next();
							// get all ancestors (aside from owl:Thing) of a parent
							AncestorResult ar = getAncestorClasses( sup, reasoner, new HashSet() );
							HashSet ancs = ar.myResult;
							
							boolean hasCycle = ar.myHasCycle;
							// if has cycle and is using toldReasoner, then we label it as a GRAPH
							if ( (hasCycle) && ( reasoner.getClass().getName().equals("org.mindswap.swoop.reasoner.SwoopToldReasoner")) )
							{
								advStats.put( GRAPH_MORPHOLOGY, GRAPH );
								return advStats;
							}
							ancs.remove( owlThing );
							
							// copy the ancestor set
							HashSet copy = new HashSet();
							copy.addAll( ancs );
							
							// find intersection of ancs and checkedSupers;
							ancs.retainAll( checkedSupers );
							if ( ancs.size() > 0 ) // intersection found, diamonstructure is found
							{
								advStats.put( GRAPH_MORPHOLOGY, DAG );
								return advStats;
							}
							else
								checkedSupers.addAll( copy );
						}
					}
				}
				advStats.put( GRAPH_MORPHOLOGY, MULTITREE );
				return advStats;
			}
		}
		catch ( Exception e )
		{ 
			e.printStackTrace();
			throw new AnalysisException("Failed in Graph Morphology Stats Checking: " + e.toString() + "\n" + Utils.getExceptionTrace( e ) );
		}
	}
	
	private static AncestorResult getAncestorClasses( OWLClass cls, SwoopReasoner reasoner, HashSet ancestors ) throws OWLException
	{
		AncestorResult result = new AncestorResult( ancestors, false );
		result = getAncestorClasses( cls, cls, reasoner, result );
		return result;
	}
	
	/* Recursively compute ancestor (parents, grand parents, long-dead mummified
	 *  great great great great great grand parents classes of a given class
	 */
	private static AncestorResult getAncestorClasses( OWLClass orig_src, OWLClass cls, SwoopReasoner reasoner, AncestorResult result ) throws OWLException
	{
		HashSet ancestors = result.myResult;
		// basic case in recursion (owl:Thing)
		if ( cls.getURI().toString().equals( OWLTHING ) )
			return result;
		// recursively find all ancestors
		Set sup = reasoner.superClassesOf( cls );
		boolean hasCycle = false;
		for ( Iterator iter = sup.iterator(); iter.hasNext(); )
		{
			HashSet set = (HashSet)iter.next();
			for ( Iterator iterator = set.iterator(); iterator.hasNext(); )
			{
				OWLClass os = (OWLClass)iterator.next();
				//System.err.println("os: " + os.getURI().toString() );
				ancestors.add( os );
				
				if ( ancestors.contains( orig_src )) // we have a cycle, prevent inf. recurs. by breaking
				{
					System.out.println( " Cycle: in this class:" + cls.getURI() + "  has parent: " + orig_src.getURI() );
					hasCycle = true;
					break;
				}

				AncestorResult r = new AncestorResult( ancestors, hasCycle );
				// now apply depth first search on the new ancestor os
				r = getAncestorClasses( orig_src, os, reasoner, r );
				ancestors = r.myResult;
								
				if ( r.myHasCycle )
					hasCycle = r.myHasCycle;
			}
		}
		AncestorResult newResult = new AncestorResult( ancestors, hasCycle );
		return newResult;
	}
	
	private static String getExpressivity() throws AnalysisException
	{
		String exprShort = null;
		try
		{
        	String express = theFrame.swoopModel.getReasoner().getExpressivity();
        	exprShort= express;
        	if (express.indexOf("<br>")>=0) exprShort = express.substring(0, express.indexOf("<br>"));        
        	//System.out.println("DL Expressivity: " + exprShort );
		}
		catch ( Exception e )
		{ 
			e.printStackTrace(); 
			e.printStackTrace();
			throw new AnalysisException("Failed in extracting Expressivity Stats: " + e.toString() + "\n" + Utils.getExceptionTrace( e ) );
		}
		return exprShort;
	}

	private Hashtable getBasicStats( AutomatedSwoopModel swoopModel, OWLOntology ont ) throws AnalysisException
	{
		try
		{
			Hashtable stats = new Hashtable();
	    	int numberOfClasses, numberOfObjectProperties=0, numberOfDatatypeProperties, numberOfAnnotationProperties, numberOfInstances;
	        int totalClasses=0, totalObjProps=0, totalDataProps=0, totalAnnotatedProps=0, totalIndividuals=0;
	        //*******************************************
	        //Added for Econnections
	        //*******************************************
	        int numberOfLinkProperties=0, numberOfForeignEntities=0;
	        int numberOfForeignClasses=0, numberOfForeignProperties=0, numberOfForeignIndividuals=0;
	
	        numberOfClasses = ont.getClasses().size();
	        numberOfDatatypeProperties = ont.getDataProperties().size();
	      	
	        //***********************************************************
	        //Added for Econnections
	        //***********************************************************
	        Iterator it= ont.getObjectProperties().iterator();
	        Set foreignOntologies = new HashSet();
	        while(it.hasNext())
	        {        	
	        	OWLObjectProperty property=(OWLObjectProperty)it.next();
	        	if(property.isLink() && !(property.getLinkTarget()).equals(ont.getURI()))
	        	{
	        		numberOfLinkProperties++;
	        		foreignOntologies.add(property.getLinkTarget()); 
	        	}
	        	else
	        	{
	        		numberOfObjectProperties++;
	        	}
	        }
	        //numberOfObjectProperties = ont.getObjectProperties().size();
	        numberOfForeignEntities = ont.getForeignEntities().size();
	        Iterator j= ont.getForeignEntities().keySet().iterator();
	        while(j.hasNext()){
	        	OWLEntity e = (OWLEntity)j.next();
	        	if(e instanceof OWLClass)
	        		numberOfForeignClasses++;
	        	if(e instanceof OWLProperty)
	        		numberOfForeignProperties++;
	        	if(e instanceof OWLIndividual)
	        		numberOfForeignIndividuals++;
	        }
	        //************************************************************
	        numberOfAnnotationProperties = ont.getAnnotationProperties().size();
	        numberOfInstances = ont.getIndividuals().size();
	        
	        // get imported ontology statistics            
	        totalClasses = swoopModel.getEntitySet(ont, SwoopModel.TRANSCLOSE_ONT, SwoopOntologyInfo.SHOW_CLASSES).size();
	        //*****************************************************
	        //Changed for Econnections
	        //*****************************************************
	        totalObjProps = swoopModel.getEntitySet(ont, SwoopModel.TRANSCLOSE_ONT, SwoopOntologyInfo.SHOW_OBJPROPERTIES).size()-numberOfLinkProperties;
	        //***************************************************
	        totalDataProps = swoopModel.getEntitySet(ont, SwoopModel.TRANSCLOSE_ONT, SwoopOntologyInfo.SHOW_DATAPROPERTIES).size();
	        totalIndividuals = swoopModel.getEntitySet(ont, SwoopModel.TRANSCLOSE_ONT, SwoopOntologyInfo.SHOW_INDIVIDUALS).size();
	        totalAnnotatedProps = swoopModel.getEntitySet(ont, SwoopModel.TRANSCLOSE_ONT, SwoopOntologyInfo.SHOW_PROPERTIES).size() - totalDataProps - totalObjProps;
	        
	        String logicalURI = ont.getLogicalURI().toString() ;
	        String physicalURI = ont.getPhysicalURI().toString();
	        // num imported classes
	        int numImportedClasses = totalClasses - numberOfClasses;
	        // num imported datatype props
	        int numImportedDatatypeProps = totalDataProps - numberOfDatatypeProperties;
	        // num imported object props
	        int numImportedObjProps = totalObjProps - numberOfObjectProperties ;
	        // num imported annotation props
	        int numImportedAnnoProps = totalAnnotatedProps - numberOfAnnotationProperties;
	        // num imported individuals
	        int numImportedInd = totalIndividuals - numberOfInstances;
	        
	        stats.put( LOGICAL_URI, logicalURI );
	        stats.put( PHYSICAL_URI, physicalURI );
	        stats.put( NUM_IMPORTED_CLASSES, numImportedClasses + "" );
	        stats.put( NUM_DEFINED_CLASSES, numberOfClasses + "");
	        stats.put( NUM_IMPORTED_DATA_PROP, numImportedDatatypeProps + "" );
	        stats.put( NUM_DEFINED_DATA_PROP, numberOfDatatypeProperties + "" );
	        stats.put( NUM_IMPORTED_OBJ_PROP, numImportedObjProps + "");
	        stats.put( NUM_DEFINED_OBJ_PROP, numberOfObjectProperties + "");
	        stats.put( NUM_IMPORTED_ANNO_PROP, numImportedAnnoProps + "" );
	        stats.put( NUM_DEFINED_ANNO_PROP, numberOfAnnotationProperties + "" );
	        stats.put( NUM_LINKED_PROP, numberOfLinkProperties + "" );
	        stats.put( NUM_IMPORTED_IND, numImportedInd + "" );
	        stats.put( NUM_DEFINED_IND, numberOfInstances + "" );
	        
	        return stats;
		}
		catch ( Exception e )
		{ 
			e.printStackTrace(); 
			throw new AnalysisException( "Failed in computing Basic Stats: " + e.toString() + "\n" + Utils.getExceptionTrace( e ));
		}
	}
		
	/* If an ontology has classes, but the graph morph is NONE in told, and other than INCONSISTENT or NONE in Reasoned, then
	 *  it is using funny RDFS vocab 
	 */
	public static boolean isRDFS( OntologyAnalysis oa ) throws AnalysisException
	{
		Hashtable stats       = oa.getBasicStats();
		HashMap toldStats     = oa.getToldStats();
		HashMap reasonedStats = oa.getPelletStats();
		try
		{
			int numClasses = Integer.parseInt( (String)stats.get( OntologyAnalyzer.NUM_DEFINED_CLASSES )) + 
				             Integer.parseInt( (String)stats.get( OntologyAnalyzer.NUM_IMPORTED_CLASSES ) );
			
			String toldGM = (String)toldStats.get( OntologyAnalyzer.GRAPH_MORPHOLOGY );
			String reasonedGM = (String)reasonedStats.get( OntologyAnalyzer.GRAPH_MORPHOLOGY );
			
			//System.out.println( numClasses > 0 ); 
			//System.out.println( toldGM.equals( OntologyAnalyzer.NONE) );
			//System.out.println( !reasonedGM.equals( OntologyAnalyzer.NONE) );
			//System.out.println( !reasonedGM.equals( OntologyAnalyzer.INCONSISTENT) );
			//System.out.println( reasonedGM );
			
			if ( (numClasses > 0)  && (toldGM.equals( OntologyAnalyzer.NONE)) && 
					( !reasonedGM.equals( OntologyAnalyzer.NONE)) && 
					( !reasonedGM.equals( OntologyAnalyzer.INCONSISTENT)) )
				return true;
			
			return false;
		}
		catch ( Exception e )
		{
			e.printStackTrace();
			throw new AnalysisException("Failed in testing for RDFS vocab" + e.toString() + "\n" + Utils.getExceptionTrace( e ) );						
		}
	}

	
    public static void main(String[] args) 
    {
    	// Create model
    	AutomatedSwoopModel model = new AutomatedSwoopModel();
		// Create application frame.
		theFrame = new SwoopFrame(model);
		
		// Do Not show frame
		theFrame.setVisible( false );
		
		OntologyAnalyzer ana = new OntologyAnalyzer();
		
		//String [] params = { "file:/C:/Documents%20and%20Settings/Dave%20Wang/Desktop/ontologies/koala.owl", "http://cvs.mygrid.org.uk/cgi-bin/viewcvs.cgi/mygrid/feta/etc/sampleData/service.rdfs?rev=1.2" };
		//String [] params = { "http://protege.stanford.edu/plugins/owl/owl-library/koala.owl", "http://cvs.mygrid.org.uk/cgi-bin/viewcvs.cgi/mygrid/feta/etc/sampleData/service.rdfs?rev=1.2" };
		//String [] params = { "http://www.mindswap.org/2004/owl/mindswappers" };
		
		try
		{
			if (IS_DEBUG)
				System.err.println(" >>> In Debugging Mode");
			BufferedReader reader = new BufferedReader( new FileReader( "test.txt" ) );
			ana.analyzeInput( model, reader, "output.txt", 1, "OntologyAnalyzerLog.txt" );
			if (IS_DEBUG)
				System.err.println(" >>> In Debugging Mode");
		}
		catch ( Exception e )
		{ e.printStackTrace(); }
		
		System.exit(0);
	}
}
