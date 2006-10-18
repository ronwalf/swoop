/*
 * Created on Feb 9, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.automation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Vector;

/**
 * @author Dave Wang
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class PelletWebSpeciator 
{
	public static String PELLET_CHECKER_PREFIX = "http://www.mindswap.org/cgi-bin/2003/pellet/pelletGet.cgi?inputFile=";
	public static String PELLET_CHECKER_OPTIONS = "&inputFormat=RDF%2FXML&inputString=&Species=true&classifyFormat=NONE&queryFile=&queryFormat=SPARQL&queryString=";
	
	public static String ERROR    = "Error";
	
	public static void speciate( String input, String output, int num_skipped )
	{
		try
		{
			// read uris in
			BufferedReader reader = new BufferedReader( new FileReader( input ) );
			Vector uris = new Vector();
			String rline = "";
			int counter = 1;
			while ( ( rline = reader.readLine()) != null )
			{
				if ( counter < num_skipped ) // skipping the first num_skipped lines
				{
					counter++;
					continue;
				}
				rline = rline.trim();
				uris.addElement( rline ); 
			}
						
			// send each uri to Pellet and output/log
			BufferedWriter logger = new BufferedWriter( new FileWriter("PelletSpeciatorLog.txt"));
			BufferedWriter writer = new BufferedWriter( new FileWriter(output) );			
			for (int i = 0; i < uris.size(); i++)
			{
				long startTime = System.currentTimeMillis();
				
				String uri = (String)uris.elementAt( i );
				String originalURI = uri.toString();
				logger.write( "Pellet Checking: ["+ (i+counter) + "] " + originalURI );
				logger.newLine();
				System.out.println("Pellet Checking: ["+ (i+counter) + "] " + originalURI );
				
				uri = uri.replaceAll(":", "%3A");
				uri = uri.replaceAll("/", "%2F");
				uri = uri.replaceAll("\\?", "%3F");
				uri = uri.replaceAll(",", "%2C");
				uri = uri.replaceAll("=", "%3D");
				uri = uri.replaceAll("\\+", "%2B");
				
				String status = ERROR;				
				try
				{
					URL url = new URL(PELLET_CHECKER_PREFIX + uri + PELLET_CHECKER_OPTIONS);
					System.out.println( url );
					URLConnection myConnection = url.openConnection();
					BufferedReader myReader = new BufferedReader( new InputStreamReader(myConnection.getInputStream()) );
					String line = "";
					String species = ERROR;
					while ( (line = myReader.readLine() ) != null )
					{
						if ( line.startsWith("<b>OWL Species:"))
						{
							int sindex = line.indexOf("</b>");
							int eindex = line.indexOf("<br>");
							species = line.substring( sindex + 4, eindex );
							break;
						}	
					}
					System.out.println( "!! " + species + " !!");
					writer.write( originalURI + "\t" + species );
					writer.newLine();
					writer.flush();
					logger.write( " - " + originalURI + " is " + species);
					logger.newLine();
					logger.flush();
				}
				catch ( Exception e)
				{
					logger.write( e.toString() );
					logger.newLine();					
				}
				finally
				{
					long stopTime = System.currentTimeMillis();
					double duration = (stopTime - startTime)/10000d;
					logger.write("took " + duration + " seconds ");
					logger.newLine();
					logger.flush();
				}
			}
		}
		catch ( Exception e )
		{ e.printStackTrace(); }

	}	
	
	public static void main( String [] args )
	{
		System.out.println("Starting...");
		PelletWebSpeciator.speciate( "list.txt", "species.txt", 482);
		System.out.println("done");
	}

}
