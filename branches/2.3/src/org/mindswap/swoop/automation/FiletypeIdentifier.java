package org.mindswap.swoop.automation;
/*
 * Created on Feb 22, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author Dave Wang
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class FiletypeIdentifier 
{
	public static final String OWL  = "OWL";
	public static final String RDFS = "RDFS";
	public static final String RDF  = "RDF";
	public static final String UNKNOWN = "UNKNOWN";
	public static final String BROKEN  = "BROKEN";
	
	public static void writeFileType( String uriFile, String outputfile, int num )
	{
		BufferedWriter writer = null;
		String uri = null;
		try
		{
			int counter = 1;
			writer = new BufferedWriter( new FileWriter(outputfile) ); 
			BufferedReader reader = new BufferedReader( new FileReader(uriFile) );
			uri = null;
			while ( counter < num )
			{
				uri = reader.readLine();
				counter++;
			}
			while ( ( uri = reader.readLine() ) != null)
			{	
				try
				{
					
					System.err.println("working on the ["+ counter +"]th document.");
					counter++;
					URL url = new URL( uri );
					URLConnection myConnection = url.openConnection();
					BufferedReader myReader = new BufferedReader( new InputStreamReader(myConnection.getInputStream()) );
					String line = null;
					boolean hasOWL  = false;
					boolean hasRDFS = false;
					boolean hasRDF  = false;
					int linecount = 0;
					
					while ( ( line = myReader.readLine()) != null )
					{
						if ( line.indexOf("http://www.w3.org/2002/07/owl") != -1 )
							hasOWL = true;
						else if ( line.indexOf("http://www.w3.org/2000/01/rdf-schema") != -1 )
							hasRDFS = true;
						else if ( line.indexOf("http://www.w3.org/1999/02/22-rdf-syntax-ns") != -1 )
							hasRDF = true;					
						linecount++;
						if ( linecount > 100 )
							break;
					}
					if ( hasOWL )
						writer.write( uri + "\t" + OWL);
					else if ( hasRDFS )
						writer.write( uri + "\t" + RDFS);
					else if ( hasRDF )
						writer.write( uri + "\t" + RDF);
					else
						writer.write( uri + "\t" + UNKNOWN);
					writer.newLine();
					writer.flush();
				}
				catch ( Exception e )
				{
					e.printStackTrace();
					try
					{
						writer.write( uri + "\t" + BROKEN );
						writer.newLine();
						writer.flush();
					}
					catch ( Exception ex )
					{ ex.printStackTrace(); }
				}
				
			}
		}
		catch ( Exception e )
		{ e.printStackTrace(); }
	}
	
	public static void main( String [] args )
	{
		FiletypeIdentifier.writeFileType( "URIS_noDaml.txt", "Filetypes.txt", 1);
	}
}

