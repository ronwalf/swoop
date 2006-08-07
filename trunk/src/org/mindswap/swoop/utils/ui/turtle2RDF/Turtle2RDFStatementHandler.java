/*
 * Created on Dec 22, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.utils.ui.turtle2RDF;

import java.io.StringWriter;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.rio.StatementHandler;
import org.openrdf.rio.rdfxml.AbbreviatedRdfXmlWriter;

/**
 * @author Dave Wang
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Turtle2RDFStatementHandler implements StatementHandler
{	
	private AbbreviatedRdfXmlWriter myWriter;;
	private StringWriter myStringWriter;
	
	public Turtle2RDFStatementHandler( )
	{ 
		myStringWriter = new StringWriter();
		myWriter = new AbbreviatedRdfXmlWriter( myStringWriter );
	}
	
	public void start()
	{
		try
		{ myWriter.startDocument(); }
		catch ( Exception e )
		{ e.printStackTrace(); }
	}

	public void end()
	{
		try
		{ myWriter.endDocument(); }
		catch ( Exception e )
		{ e.printStackTrace(); }
	}
	
	public void handleStatement(Resource subject, URI predicate, Value object)
	{
		try
		{ myWriter.writeStatement( subject, predicate, object); }
		catch ( Exception e )
		{ e.printStackTrace(); }
	}
	
	public String getRDF()
	{
		myStringWriter.flush();
		String result = myStringWriter.toString();
		return result;
	}
	
}