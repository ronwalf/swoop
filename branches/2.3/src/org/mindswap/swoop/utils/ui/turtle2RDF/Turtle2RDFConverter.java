/*
 * Created on Dec 22, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.utils.ui.turtle2RDF;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.StringReader;

import javax.swing.JOptionPane;
import javax.swing.text.JTextComponent;

import org.openrdf.rio.turtle.TurtleParser;

/**
 * @author Dave Wang
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Turtle2RDFConverter 
{
		
	public static String turtle2RDF( String turtleStr, String baseURI, Component c, JTextComponent t )
	{
		TurtleParser parser = new TurtleParser();
		Turtle2RDFStatementHandler handler = new Turtle2RDFStatementHandler(); 
		parser.setStatementHandler( handler );
		parser.setParseErrorListener( new Turtle2RDFParseErrorListener( c, turtleStr, t ) );
		//parser.setParseLocationListener(new Turtle2RDFParseLocationListener() ); 
		try
		{
			handler.start();
			parser.parse( new StringReader( turtleStr ), baseURI );
			handler.end();
			return handler.getRDF();
		}
		catch ( Exception e)
		{ 
			//showMessageDialog( c, e.getMessage(), "Turtle Parse Error", JOptionPane.ERROR_MESSAGE );
			System.out.println("Error parsing Turtle code");
			//e.printStackTrace(); 
			return null;
		}
	}
	
	
	public static void showMessageDialog( Component owner, String msg, String title, int type )
	{ JOptionPane.showMessageDialog( owner, msg, title, type ); }
	
	/* newEntityCode is the text from JEditorPane.getDocument().getText()
	 * htmlText is the text from JEditorPane.getText();
	 * 
	 */
	public static String addLineInfo( String newEntityCode, String htmlText)
	{
		try
		{
			htmlText      = htmlText.replaceAll( "&#160;", " " );		
			htmlText      = htmlText.replaceAll( "<head>", "" );
			htmlText      = htmlText.replaceAll( "</head>", "" );
			htmlText      = htmlText.replaceAll( "<html>", "" );
			htmlText      = htmlText.replaceAll( "</html>", "" );
			htmlText      = htmlText.replaceAll( "<body>", "" );
			htmlText      = htmlText.replaceAll( "</body>", "" );
			htmlText      = htmlText.replaceAll( "<font face=\"Verdana\" size=\"3\">", "" );
			htmlText      = htmlText.replaceAll( "</font>", "" );
			htmlText      = htmlText.replaceAll( "&lt;", "<" );
			htmlText      = htmlText.replaceAll( "&gt;", ">" );
			htmlText      = htmlText.replaceAll( "&quot;", "\"" );
			htmlText      = htmlText.replaceAll( "<br>", "\n" ).trim();
	
			StringBuffer buffer = new StringBuffer( newEntityCode );
			BufferedReader reader = new BufferedReader ( new StringReader( htmlText ) );
			String line = "";
			String text = "";
			int ind =  0;
			while ( (line = reader.readLine() ) != null )
			{
				if ( (line.indexOf("@prefix") != -1) || (line.indexOf("<") != -1 ) )
					continue;
				ind = buffer.indexOf( line, ind );
				if (ind == -1)
					break;
				
				ind = ind + line.length();
				buffer.insert( ind, "\n");
				ind++;
			}
			
			newEntityCode = (buffer.toString()).replaceAll( " \\.", " .\n" );
			return newEntityCode;
		}
		catch ( Exception e )
		{ e.printStackTrace(); }
		
		return null;
	}
	
	public static void main( String [] args )
	{
		String text3 = 
			"@prefix xsd: <http://www.w3.org/2001/XMLSchema#> ." + 
			"@prefix owl: <http://www.w3.org/2002/07/owl#> ." + 
			"@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> ." + 
			"@prefix : <http://protege.stanford.edu/plugins/owl/owl-library/koala.owl#> ." +
			":Koala     a owl:Class;    rdfs:subClassOf     :Marsupials . ";
			
			char c = 160;
			text3 = text3.replaceAll( ""+c, " " );
			
			String rdf = Turtle2RDFConverter.turtle2RDF( text3, "http://protege.stanford.edu/plugins/owl/owl-library/koala.owl#", null, null );
			System.out.println( rdf );
	}
}
