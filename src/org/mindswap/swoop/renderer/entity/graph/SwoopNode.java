/*
 * Created on Apr 5, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.mindswap.swoop.renderer.entity.graph;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import javax.swing.event.HyperlinkEvent;

import org.mindswap.swoop.SwoopModel;
import org.semanticweb.owl.io.ShortFormProvider;

import com.touchgraph.graphlayout.Node;

/**
 * @author Dave
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class SwoopNode extends Node 
{
	
	private URI myURI = null;
	private SwoopModel myModel; 
	
	// used to make a node such that its ID is NOT the same as its URI.  
	public SwoopNode( URI uri, String ID, String label, SwoopModel model)
	{
		myURI = uri;
		myModel = model;
		super.lbl = label;
		super.id  = ID;
		super.initialize( super.id );  // ID needs to be unique
		
		//super.font = new Font(model.getFontFace(), Font.PLAIN, Integer.parseInt(model.getFontSize()));
	}
	
	// used to make a node such that has no ID
	public SwoopNode( URI uri, String label, SwoopModel model)
	{
		myURI = uri;
		myModel = model;
		super.lbl = label;
		super.initialize( null );  // no ID
		
		//super.font = new Font(model.getFontFace(), Font.PLAIN, Integer.parseInt(model.getFontSize()));
	}

	public void fireHyperLinkEvent()
	{
		try
		{
			if ( myURI == null )
				return;
			HyperlinkEvent event = new HyperlinkEvent( this, HyperlinkEvent.EventType.ACTIVATED, new URL( myURI.toString()), myURI.toString() );
			myModel.getFrame().termDisplay.hyperlinkUpdate( event );
			
		}
		catch ( MalformedURLException e)
		{
			e.printStackTrace();
		}
		
	}
	
	public URI getURI()
	{ return myURI; }
}
