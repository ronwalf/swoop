package org.mindswap.swoop.utils.graph.hierarchy;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.visualization.GraphMouseListener;
import edu.uci.ics.jung.visualization.MouseListenerTranslator;
import edu.uci.ics.jung.visualization.PickSupport;
import edu.uci.ics.jung.visualization.VisualizationViewer;

/*
 * Created on Jul 27, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * @author Dave Wang
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

// don't think this class is being used at all -- tw7

public class SwoopMouseListenerTranslator extends MouseListenerTranslator
{

	private VisualizationViewer vv;
	private GraphMouseListener gel;

	/**
	 * @param gel
	 * @param vv
	 */
	public SwoopMouseListenerTranslator(GraphMouseListener gel, VisualizationViewer vv) 
	{
		// calls super's constructor, but it doesn't mean anything (since all super's fields are private, not inherited)
		super( gel, vv);
		this.gel = gel;
		this.vv = vv;
	}
	
	/**
	 * Transform the point to the coordinate system in the
	 * VisualizationViewer, then use either PickSuuport
	 * (if available) or Layout to find a Vertex
	 * @param point
	 * @return
	 */
	private Vertex getVertex(Point2D point) {
	    // adjust for scale and offset in the VisualizationViewer
	    Point2D p = vv.transform(point);
	    PickSupport pickSupport = vv.getPickSupport();
	    Vertex v = null;
	    if(pickSupport != null) {
	        v = pickSupport.getVertex(p.getX(), p.getY());
	    } 
	    return v;
	}
	/**
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent e) 
	{
	    Vertex v = getVertex(e.getPoint());
		if ( v != null ) {
			gel.graphClicked(v, e );
		}
	}

	/**
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent e) {
		Vertex v = getVertex(e.getPoint());
		if ( v != null ) {
			gel.graphPressed(v, e );
		}
	}

	/**
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent e) {
		Vertex v = getVertex(e.getPoint());
		if ( v != null ) {
			gel.graphReleased(v, e );
		}
	}
}
