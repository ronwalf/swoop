/*
 * Created on Feb 8, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.automation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.StringTokenizer;

/**
 * @author Dave Wang
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class PostProcessor 
{
	public static final String FILE_OWL  = "OWL";
	public static final String FILE_DAML = "DAML";
	public static final String FILE_RDF  = "RDF";
	public static final String FILE_RDFS = "RDFS";
	public static final String FILE_NONE = "NONE";
	
	public static void removeWordNetStats( String input, String output, String wordnets )
	{
		try
		{
			BufferedReader reader = new BufferedReader( new FileReader( input ) );
			BufferedWriter writer = new BufferedWriter( new FileWriter( output ) );
			BufferedWriter collector = new BufferedWriter( new FileWriter( wordnets) );
			String line =  null;
			while ( (line = reader.readLine()) != null )
			{
				if ( line.startsWith("http://xmlns.com/wordnet/1.6/") )
				{
					collector.write( line );
					collector.newLine();
					continue;
				}
				writer.write( line );
				writer.newLine();
			}
			writer.flush();
			collector.close();
			reader.close();
			writer.close();
			collector.close();
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
	}

	public static void removeW3CJENATestStats( String input, String output, String discardedfile )
	{
		try
		{
			BufferedReader reader = new BufferedReader( new FileReader( input ) );
			// writes data that we want to keep
			BufferedWriter writer = new BufferedWriter( new FileWriter( output ) );
			// collects stats to 'throw away'
			BufferedWriter collector = new BufferedWriter( new FileWriter( discardedfile ) );
			
			String line =  null;
			while ( (line = reader.readLine()) != null )
			{
				if (( line.indexOf("http://cvs.sourceforge.net/viewcvs.py/jena/jena2/testing/") != -1 ) ||
						( line.indexOf("www.w3.org/2002") != -1 ) ||
						( line.indexOf("http://lists.w3.org/Archives/Public") != -1 ) ||
						( line.indexOf("http://web3.w3.org/2002") != -1 ) )
				{
					collector.write( line );
					collector.newLine();
					continue;
				}
				writer.write( line );
				writer.newLine();
			}
			writer.flush();
			collector.close();
			reader.close();
			writer.close();
			collector.close();
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
	}

	
	/* Input phyURIs is a filename containing only the list of physicalURIs
	 *  output contains a column of the physicalURIs and the best guessed filetype
	 * 
	 */
	public static void extractFileType( String phyURIs, String output )
	{
		try
		{
			BufferedReader reader = new BufferedReader( new FileReader( phyURIs) );
			BufferedWriter writer = new BufferedWriter( new FileWriter( output ) );
			String line = "";
			
			while ( (line = reader.readLine()) != null )
			{
				String type = FILE_NONE;
				line = line.trim();
				line = line.toLowerCase();
				if ( line.endsWith(".owl") )
					type = FILE_OWL;
				else if ( line.endsWith(".rdf") )
					type = FILE_RDF;
				else if ( line.endsWith(".daml") )
					type = FILE_DAML;
				else if ( line.endsWith(".rdfs") )
					type = FILE_RDFS;
				else if ( line.indexOf(".owl") != -1 )
					type = FILE_OWL;
				else if ( line.indexOf(".rdf") != -1 )
					type = FILE_RDF;
				else if ( line.indexOf(".daml") != -1 )
					type = FILE_DAML;
				else if ( line.indexOf(".rdfs") != -1 )
					type = FILE_RDFS;
				else
					type = FILE_NONE;
				writer.write( line + "\t" + type );
				writer.newLine();
				writer.flush();
			}	
			writer.close();
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
	}
	
	/* Input phyuriExpresivity is a filename containing the list of physical URIs and Expressivity
	 *  output contains a column of the physical URIs, fixed Expressivity, and a rank based 
	 *  on complexity results.
	 */
	public static void fixNrankExpressivity( String uriExpressivity, String output )
	{
		try
		{
			BufferedReader reader = new BufferedReader( new FileReader( uriExpressivity) );
			BufferedWriter writer = new BufferedWriter( new FileWriter( output ) );
			String line = "";
			
			while ( (line = reader.readLine()) != null )
			{
				StringTokenizer tokens = new StringTokenizer( line );
				int count = tokens.countTokens();
				if ( count < 2 )
				{
					writer.write( tokens.nextToken() + " \t ");
					writer.newLine();
					writer.flush();
					continue;
				}
				else
				{
					String uri = tokens.nextToken();
					String exp = tokens.nextToken();
					
					String cExp = fixExpressivity( exp );
					String rank = rankExpressivity( cExp );
					
					writer.write( uri + "\t" + cExp + "\t" + rank );
					writer.newLine();
					writer.flush();
				}
			}	
			writer.close();
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
	}

	private static String fixExpressivity( String exp )
	{
		// change ALCR+ to S
		if ( exp.indexOf( "ALCR+") != -1 )
			exp = exp.replaceAll( "ALCR+", "S" );
		// change IO to OI
		if ( exp.indexOf("IO") != -1 )
			exp = exp.replaceAll( "IO", "OI");
		// remove (D)
		if ( exp.indexOf("(D)") != -1 )
			exp = exp.replaceAll("\\(D\\)", "" );
		exp = exp.trim();
		return exp;
	}
	
	private static String rankExpressivity( String exp )
	{
		ExpressivityRanker ranker = ExpressivityRanker.getInstance();
		return ranker.rankByRoughBin( exp );
	}
	
	public static void main( String [] args )
	{
		System.out.print("Starting...");
		//PostProcessor.removeWordNetStats( "copy.txt", "noRDFS.txt", "RDFS.txt");
		PostProcessor.fixNrankExpressivity( "expressivity.txt", "binnedExpressivity.txt" );
		//PostProcessor.removeW3CJENATestStats("NoDamlStats.txt", "NoTestFileStats.txt", "TestFileStats.txt");
		System.out.println("Done.");
	}
}
