/*
 * Created on Feb 2, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.automation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * @author Dave Wang
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class OntologyGrabber 
{
	public static void saveLocal( String uri, String localfile, BufferedWriter logWriter, int counter )
	{
		try
		{
			URL url = new URL( uri );
			BufferedReader reader = new BufferedReader( new InputStreamReader( url.openConnection().getInputStream()) );
			BufferedWriter writer = new BufferedWriter( new FileWriter( "onts/" + localfile) );
			String line = null;
			while ( ( line = reader.readLine()) != null )
			{
				writer.write( line );
				writer.newLine();
			}
			logWriter.write( "[" + counter + "]" +  uri + " saved. ");
			logWriter.newLine();				
			logWriter.flush();
			
			writer.flush();
			writer.close();
			reader.close();
			
		}
		catch ( Exception e )
		{ 
			e.printStackTrace();
			try
			{
				logWriter.write( "[" + counter + "]" + uri + " failed at saving: " + Utils.getExceptionTrace( e ) );
				logWriter.newLine();
				logWriter.flush();
			}
			catch ( IOException ex )
			{
				System.err.println( "Writing log failed. ");
				ex.printStackTrace();
			} 
		}
	}
	
	public static String convertToFilename( String line )
	{
		line = line.replaceAll("\\\\", "" );
		line = line.replaceAll("/", "" ); 
		line = line.replaceAll("\\?", "" ); 
		line = line.replaceAll("\\*", "" );
		line = line.replaceAll(":", "" );
		line = line.replaceAll("<", "" );
		line = line.replaceAll(">", "" );
		line = line.replaceAll("|", "" );
		line = line.replaceAll("\"", "" );
		return line;
	}
	
	public static String formatNumInDigits( int num, int numDig )
	{
		String n = "" + num ;
		int length = n.length();
				
		while ( length < numDig )
		{
			n = "0" + n;
			length = n.length();
		}
		return n;
	}
	
	public static void main( String [] args )
	{
		try
		{
			BufferedReader reader = new BufferedReader( new FileReader( "physicalURIs.txt" ) );
			String line = null;
			int count = 1;
			BufferedWriter logwriter = new BufferedWriter( new FileWriter( "OntoGrabberLog.txt") );
			while ( (line = reader.readLine()) != null )
			{
				if ( line.startsWith("*") )
					continue;
				line = line.trim();
				OntologyGrabber.saveLocal( line, OntologyGrabber.formatNumInDigits(count,4) + "-" + OntologyGrabber.convertToFilename( line ),  logwriter, count );
				count++;
				System.out.println("Written " + line );
			}
			logwriter.close();
		}
		catch ( Exception e )
		{ 
			e.printStackTrace(); 
		}
	}
}
