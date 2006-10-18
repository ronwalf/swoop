/*
 * Created on Dec 22, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.utils.ui.turtle2RDF;

import java.awt.Color;
import java.awt.Component;
import java.io.BufferedReader;
import java.io.StringReader;

import javax.swing.JOptionPane;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;

import org.openrdf.rio.ParseErrorListener;

/**
 * @author Dave Wang
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Turtle2RDFParseErrorListener implements ParseErrorListener
{
	public static final DefaultHighlightPainter DEFAULT_HLPAINTER =
	 	new DefaultHighlighter.DefaultHighlightPainter( new Color(255, 255, 120) ) ;
	
	private Component myMessageDialogOwner;
	private JTextComponent myTextComponent;
	private String myTCode;
	
	public Turtle2RDFParseErrorListener( Component c, String turtleCode, JTextComponent t)
	{
		super();
		myMessageDialogOwner = c;
		myTextComponent      = t;
		myTCode              = turtleCode;
	}
	
	public void warning(String arg0, int arg1, int arg2) 
	{ }

	public void error(String arg0, int arg1, int arg2) 
	{ 
		String message = "Turtle Parsing Error: " + arg0 + " at line " + arg1 ; 
		System.err.println( message );
		Turtle2RDFConverter.showMessageDialog( myMessageDialogOwner, message, "Turtle Parse Error", JOptionPane.ERROR_MESSAGE );
	}

	public void fatalError(String arg0, int arg1, int arg2) 
	{ 
		BufferedReader reader = new BufferedReader( new StringReader( myTCode ) );
		String line = "";
		int l = 0;
		int count = 0;
		try
		{
			if ( myTextComponent != null )
			{
				while ( (line = reader.readLine() ) != null )
				{
					count++;
					if ( count == arg1 )
					{
						myTextComponent.getHighlighter().addHighlight( l + 1, l + line.length() + 1, DEFAULT_HLPAINTER);
						break;
					}
					else
						l = l + line.length();
				}
			}
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
		
		
		String message = "Turtle Parsing Error (Fatal): " + arg0 + " at line " + arg1 ;
		System.err.println( message );
		Turtle2RDFConverter.showMessageDialog( myMessageDialogOwner, message, "Turtle Parse Error", JOptionPane.ERROR_MESSAGE );
	}
}