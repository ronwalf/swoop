package org.mindswap.swoop.utils.graph.hierarchy;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.util.Set;

import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.visualization.Layout;
import edu.uci.ics.jung.visualization.PluggableRenderer;
import edu.uci.ics.jung.visualization.Renderer;
import edu.uci.ics.jung.visualization.ShapePickSupport;
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


/*
 * This class serves no more purpose than edu.uci.ics.jung.visualization.ClassicPickSupport 
 *   except the other one is not visible from outside of the package.  In order to extend the
 *   VisualizationViewer, this step is necessary :/
 * 
 * 
 */

public class SwoopPickSupport extends ShapePickSupport {
	
    
    protected Layout layout;
    
    /*
    public SwoopClassicPickSupport(Layout l)
    {
        this.layout = l;
    }
    */
    
    public SwoopPickSupport(VisualizationViewer vv) 
    {	    	
        super(vv, 2);
    }

    /** 
     * Iterates over Vertices, checking to see if x,y is contained in the
     * Vertex's Shape. If (x,y) is contained in more than one vertex, use
     * the vertex whose center is closest to the pick point.
     * @see edu.uci.ics.jung.visualization.PickSupport#getVertex(double, double)
     */
    public Vertex getVertex(double x, double y) {
        Layout layout = vv.getGraphLayout();
        PluggableRenderer renderer = null;
        Renderer r = vv.getRenderer();
        if(r instanceof PluggableRenderer) {
            renderer = (PluggableRenderer)r;
        }
		Vertex closest = null;
		double minDistance = Double.MAX_VALUE;
		Set vertexSet = layout.getVisibleVertices();		
		Object [] vertexArray = vertexSet.toArray();

		// reverse the order of checking because we want the one that is drawn last to
		// be checked first.
		// this guarantees that the topmost vertex is always picked
		for ( int i = vertexArray.length-1; i >=0 ; i-- ) 
		{
		    if(renderer != null) {
		        Vertex v = (Vertex)vertexArray[i];
		        if ( !((SwoopOntologyVertex)v).isVisible() )
		        	continue; // if not visible, then don't try to grab support
		        
		        Shape shape = renderer.getVertexShapeFunction().getShape(v);
		        AffineTransform xform = 
		            AffineTransform.getTranslateInstance(layout.getX(v), layout.getY(v));
		        shape = xform.createTransformedShape(shape);

		        if(shape.contains(x, y)) 
		        {
		        	closest = v;
		        	break;
		        }
		    } 
		}
		return closest;
    }

    
    /**
     * Gets the vertex nearest to the location of the (x,y) location selected,
     * within a distance of <tt>maxDistance</tt>. Iterates through all
     * visible vertices and checks their distance from the click. Override this
     * method to provde a more efficient implementation.
     * @param x
     * @param y
     * @param maxDistance temporarily overrides member maxDistance
     */
    /*
    protected Vertex getVertex(double x, double y, double maxDistance) {
        double minDistance = maxDistance * maxDistance;
        Vertex closest = null;
        System.out.println("picking...");
        for (Iterator iter = layout.getVisibleVertices().iterator(); iter.hasNext();) 
        {
            SwoopOntologyVertex v = (SwoopOntologyVertex) iter.next();
            if ( !v.isVisible() )
            {
            	try
				{
            		System.out.println( v.getOntology().getURI() );
				}
            	catch ( Exception e )
				{ e.printStackTrace(); }
            	
            	continue;
            }
            double dx = layout.getX(v) - x;
            double dy = layout.getY(v) - y;
            double dist = dx * dx + dy * dy;
            if (dist < minDistance) {
                minDistance = dist;
                closest = v;
            }
        }
        return closest;
    }
    */
    
    public void setLayout(Layout l)
    {
        this.layout = l;
    }
    
    /** 
     * @return null ClassicPickSupport does not do edges
     */
    public Edge getEdge(double x, double y) {
        return null;
    }
}
