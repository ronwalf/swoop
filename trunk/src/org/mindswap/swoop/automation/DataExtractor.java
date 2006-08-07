/*
 * Created on Feb 1, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.automation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * @author Dave Wang
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class DataExtractor 
{
	public static final String TAB = "\t";
	
	public static final String EQUALS = "=";
	public static final String GREATERTHAN = ">";
	public static final String LESSTHAN = ">";
	public static final String NOTEQUALS = "!=";
	
	private BufferedReader myReader = null;
	private Vector myTable = null;
	
	
	public DataExtractor( String ontoStats )
	{
		try
		{
			myReader = new BufferedReader( new FileReader( ontoStats ) );
			myTable = new Vector();
			String line = myReader.readLine();
			while ( ( line = myReader.readLine() ) != null )
			{
				StringTokenizer tokens = new StringTokenizer( line, TAB, true );
				Vector segmentedLine = new Vector();
				boolean isLastTokTab = false;
				
				/* parse each line and put each cell between tabs.
				 * each empty cell is represented as ""
				 */
				while ( tokens.hasMoreTokens() )
				{
					String tok = tokens.nextToken();
					if (( isLastTokTab == true ) && ( tok.equals(TAB)))
						segmentedLine.add("");
					if ( tok.equals( TAB ))
					{
						isLastTokTab = true;
						continue;
					}
					else
						isLastTokTab = false;
					segmentedLine.add( tok );
				}
				myTable.add( segmentedLine );
			}
			myReader.close();
		}
		catch ( Exception e )
		{ e.printStackTrace(); }
	}
	
	
	/*
	 * colNum starts with 0
	 */
	public void writeHistogramTo( int colNum, String outputfile )
	{
		try
		{
			Hashtable thisTable = new Hashtable();
			for ( int i = 0; i < myTable.size(); i++ )
			{
				Vector lineData = (Vector)myTable.elementAt( i );
				String data = "";
				if ( lineData.size() >= colNum + 1 )
					data = (String)lineData.elementAt( colNum );
				if ( thisTable.keySet().contains( data ) )
				{
					Integer integer = (Integer)thisTable.get( data );
					integer = new Integer( integer.intValue() + 1 );
					thisTable.put( data, integer );
				}
				else
					thisTable.put( data, new Integer(1) );
			}
			BufferedWriter writer = new BufferedWriter( new FileWriter( outputfile) );
			
			Set keys = thisTable.keySet();
			for ( Iterator it = keys.iterator(); it.hasNext(); )
			{
				String key = (String)it.next();
				writer.write( key + "\t" + thisTable.get( key ) );
				writer.newLine();
			}
			writer.flush();
			writer.close();
		}
		catch ( Exception e )
		{ e.printStackTrace(); }
	}
	
	/*
	 * colNum starts with 0
	 */
	public void writeHistogramWithConstraintsTo( int [] cols, String [] values, int colNum, String outputfile )
	{
		try
		{
			Hashtable thisTable = new Hashtable();
			for ( int i = 0; i < myTable.size(); i++ )
			{
				Vector lineData = (Vector)myTable.elementAt( i );
				String data = "";
				boolean satisfiesConstraints = true;
				
				// checking if lineData.elementAt(j) equals the values in cols[j]
				for ( int j = 0; j < cols.length; j++ )
				{					
					int index = cols[j];
					String val = values[j];
					if ( lineData.size() < index + 1 )
					{
						satisfiesConstraints = false;
						break;
					}
					
					data = (String)lineData.elementAt( index );					
					if ( !data.equals( val ) )
					{
						satisfiesConstraints = false;
						break;
					}
				}				
				if ( satisfiesConstraints )
				{ 
					data = "";
					if ( lineData.size() >= colNum + 1 )
						data = (String)lineData.elementAt( colNum );
					if ( thisTable.keySet().contains( data ) )
					{
						Integer integer = (Integer)thisTable.get( data );
						integer = new Integer( integer.intValue() + 1 );
						thisTable.put( data, integer );
					}
					else
						thisTable.put( data, new Integer(1) );
				}
			}
			BufferedWriter writer = new BufferedWriter( new FileWriter( outputfile) );
			
			Set keys = thisTable.keySet();
			for ( Iterator it = keys.iterator(); it.hasNext(); )
			{
				String key = (String)it.next();
				writer.write( key + "\t" + thisTable.get( key ) );
				writer.newLine();
			}
			writer.flush();
			writer.close();
		}
		catch ( Exception e )
		{ e.printStackTrace(); }
	}	
	
	
	
	public void writeListWithCriteria( int cols[], String values[], String rel[], String outputfile )
	{
		try
		{	
			Vector myMatchingOntologyURIs = new Vector();
			for ( int i = 0; i < myTable.size(); i++ )
			{
				Vector lineData = (Vector)myTable.elementAt( i );
				String data = "";
				boolean satisfiesConstraints = true;
				
				for ( int j = 0; j < cols.length; j++ )
				{					
					int index = cols[j];    
					String val = values[j]; // target value
					if ( lineData.size() < index + 1 )
					{
						satisfiesConstraints = false;
						break;
					}
					try
					{
						data = (String)lineData.elementAt( index );
						if ( rel[j].equals( EQUALS ) )
						{
							if ( !data.equals( val ) )
							{
								satisfiesConstraints = false;
								break;
							}
						}
						else if ( rel[j].equals( NOTEQUALS ) )
						{
							if ( data.equals( val ) )
							{
								satisfiesConstraints = false;
								break;
							}
						}
						else if ( rel[j].equals( GREATERTHAN ) )
						{
							if (data.trim().equals(""))
							{								
								satisfiesConstraints = false;
								break;
							}
							double data_val = Double.parseDouble( data );
							double target_value = Double.parseDouble( val );
							if ( !( data_val > target_value ) )
							{
								satisfiesConstraints = false;
								break;
							}
						}
						else
						{
							double data_val = Double.parseDouble( data );
							double target_value = Double.parseDouble( val );
							if ( !( data_val < target_value ) )
							{
								satisfiesConstraints = false;
								break;
							}
						}
					}
					catch ( NumberFormatException e )
					{
						e.printStackTrace();
						satisfiesConstraints = false;
						break;
					}
				}				
				if ( satisfiesConstraints )
				{ 
					data = (String)lineData.elementAt( 1 ); // reading physical uri of ontology
					myMatchingOntologyURIs.add( data );
				}
			}
			BufferedWriter writer = new BufferedWriter( new FileWriter( outputfile) );
			
			for ( Iterator it = myMatchingOntologyURIs.iterator(); it.hasNext(); )
			{
				String uri = (String)it.next();
				writer.write( uri );
				writer.newLine();
			}
			writer.flush();
			writer.close();
		}
		catch ( Exception e )
		{ e.printStackTrace(); }	
	}
	
	
	public void writeAllColumnsWithCriteria( int cols[], String values[], String rel[], String outputfile )
	{
		try
		{	
			Vector myMatchingData = new Vector();
			for ( int i = 0; i < myTable.size(); i++ )
			{
				Vector lineData = (Vector)myTable.elementAt( i );
				String data = "";
				boolean satisfiesConstraints = true;
				
				for ( int j = 0; j < cols.length; j++ )
				{					
					int index = cols[j];    
					String val = values[j]; // target value
					if ( lineData.size() < index + 1 )
					{
						satisfiesConstraints = false;
						break;
					}
					try
					{
						data = (String)lineData.elementAt( index );
						if ( rel[j].equals( EQUALS ) )
						{
							if ( !data.equals( val ) )
							{
								satisfiesConstraints = false;
								break;
							}
						}
						else if ( rel[j].equals( NOTEQUALS ) )
						{
							if ( data.equals( val ) )
							{
								satisfiesConstraints = false;
								break;
							}
						}
						else if ( rel[j].equals( GREATERTHAN ) )
						{
							double data_val = Double.parseDouble( data );
							double target_value = Double.parseDouble( val );
							if ( !( data_val > target_value ) )
							{
								satisfiesConstraints = false;
								break;
							}
						}
						else
						{
							double data_val = Double.parseDouble( data );
							double target_value = Double.parseDouble( val );
							if ( !( data_val < target_value ) )
							{
								satisfiesConstraints = false;
								break;
							}
						}
					}
					catch ( NumberFormatException e )
					{
						e.printStackTrace();
						satisfiesConstraints = false;
						break;
					}
				}				
				if ( satisfiesConstraints )
				{ 
					myMatchingData.add( lineData );
				}
			}
			BufferedWriter writer = new BufferedWriter( new FileWriter( outputfile) );
			
			for ( Iterator it = myMatchingData.iterator(); it.hasNext(); )
			{
				Vector data = (Vector)it.next();
				for ( Iterator iter = data.iterator(); iter.hasNext(); )
				{
					writer.write( (String)iter.next() );
					if ( iter.hasNext() )
						writer.write( "\t");
				}
				writer.newLine();
			}
			writer.flush();
			writer.close();
		}
		catch ( Exception e )
		{ e.printStackTrace(); }
	
	}

	
	public void writeColumnsTo( int cols[], String filename )
	{
		try
		{
			BufferedWriter writer = new BufferedWriter( new FileWriter( filename) );
			for ( int i = 0; i < myTable.size(); i++ )
			{
				Vector lineData = (Vector)myTable.elementAt(i);
				for ( int j = 0; j < cols.length; j++ )
				{
					if ( lineData.size() <= cols[j] )
					{
						writer.write( " " + "\t" );
						continue;
					}
					writer.write( lineData.elementAt( cols[j] ) + "\t" );
				}
				writer.newLine();
			}
			writer.flush();
			writer.close();
		}
		catch ( Exception e )
		{ e.printStackTrace(); }
	}
	
	public static void main( String [] args )
	{
		
		System.out.print("Starting...  ");
		
		//DataExtractor de = new DataExtractor("test.txt");
		DataExtractor de = new DataExtractor("OWL-only-rightCount.txt");
		/*
		de.writeHistogramTo( 22, "toldExpressHist.txt" );
		de.writeHistogramTo( 39, "reasonedExpressHist.txt" );
		de.writeHistogramTo( 38, "toldGMHist.txt");
		de.writeHistogramTo( 55, "reasonedGMHist.txt");

		int [] indices1 = { 40 };
		String [] values1  = { "0"};
		String [] rel1     = { DataExtractor.GREATERTHAN };
		de.writeListWithCriteria( indices1, values1, rel1, "OntsWithUC.txt");
		
		int [] indices2 = { 32 };
		String [] values2  = { "0" };
		String [] rel2     = { DataExtractor.GREATERTHAN };
		de.writeListWithCriteria( indices2, values2, rel2, "OntsWithPropDepth.txt");
		de.writeHistogramTo( 32, "PropertyDepthHist.txt");
		*/
		
		//int [] cols = { 1, 22 };
		//de.writeColumnsTo( cols, "SwoopExpressivity.txt" );
		
		int [] cols = { 40 };
		String [] vals = { "0.0" };
		String [] rels = { DataExtractor.GREATERTHAN};
		//de.writeColumnsTo( cols, "expressivity.txt");
		de.writeListWithCriteria( cols, vals, rels, "Unsat.txt");
		
		
		//de.writeHistogramTo( 39, "pelletExpressHist.txt" );
		System.out.println("done");
		
	}
}
