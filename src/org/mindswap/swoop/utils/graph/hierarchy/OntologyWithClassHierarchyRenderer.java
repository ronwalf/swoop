package org.mindswap.swoop.utils.graph.hierarchy;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import org.apache.commons.collections.Predicate;

import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.visualization.PluggableRenderer;
/*
 * Created on Jul 17, 2005
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
public class OntologyWithClassHierarchyRenderer extends PluggableRenderer 
{
	// determines whether a node or an edge is to be drawn or not
	class VisibilityPredicate implements Predicate
	{		
		public boolean evaluate(Object obj) 
		{
	        // if vertex is not visible, then don't draw it
	        if (obj instanceof SwoopOntologyVertex)
	        {
	        	SwoopOntologyVertex vertex = (SwoopOntologyVertex)obj;
	        	return vertex.isVisible();
	        }
			return false;
		}
	}
	
	protected boolean isDrawContent = true;   
	
	public OntologyWithClassHierarchyRenderer()
	{
		super();
		vertexIncludePredicate = new VisibilityPredicate();
	}
	public void paintVertex( Graphics g, Vertex v, int x, int y)
	{
        if (!vertexIncludePredicate.evaluate(v))
            return;
        
        Graphics2D g2d = (Graphics2D)g;
        AffineTransform form = g2d.getTransform();

        Stroke old_stroke = g2d.getStroke();
        Stroke new_stroke = vertexStrokeFunction.getStroke(v);
        if (new_stroke != null)
            g2d.setStroke(new_stroke);
        Paint old_paint = g2d.getPaint();
        
        // get the shape to be rendered
        Shape s = vertexShapeFunction.getShape(v);
        // create a transform that translates to the location of
        // the vertex to be rendered
        AffineTransform xform = AffineTransform.getTranslateInstance(x,y);
        // transform the vertex shape with xtransform
        s = xform.createTransformedShape(s);
        
        // get Paints for filling and drawing
        // (filling is done first so that drawing and label use same Paint)
        Paint fill_paint = vertexPaintFunction.getFillPaint(v); 
        if (fill_paint != null)
        {
            g2d.setPaint(fill_paint);
            g2d.fill(s);
        }

        Paint draw_paint = vertexPaintFunction.getDrawPaint(v);
        if (draw_paint != null)
        {
            g2d.setPaint(draw_paint);
            g2d.draw(s);
        }

        if (new_stroke != null)
            g2d.setStroke(old_stroke);

        // use existing paint for text if no draw paint specified
        if (draw_paint == null)
            g2d.setPaint(old_paint);
        
        // draw content
        if (isDrawContent)
        {
        	HierarchicalVertexDataRenderer rend = HierarchicalVertexDataRenderer.getInstance();
        	OntologyGraphNode node = (OntologyGraphNode)v.getUserDatum( OntologyWithClassHierarchyGraph.DATA );
        	int size = node.getDiameter();
        	rend.render(g2d, x, y, node.getTreeNode() );
        }
        
        String label = vertexStringer.getLabel(v);
        if ( (label != null) && ( label.length() > 1 )) {
            labelVertex(g, v, label, x, y);
        }
        
        g2d.setPaint(old_paint);
  
		double[] matrix = new double[6];
		form.getMatrix(matrix);
		double zoomFactor = matrix[0];
		Point2D.Double origin = new Point2D.Double(0, 0);
		origin = (Point2D.Double) form.transform(origin, origin);
		// create identity matrix
		matrix[0] = 1;
		matrix[1] = 0;
		matrix[2] = 0;
		matrix[3] = 1;
		matrix[4] = 0;
		matrix[5] = 0;

		AffineTransform textForm = new AffineTransform( matrix );
		g2d.setTransform(textForm);
		g2d.setColor(Color.BLACK);
		g2d.drawString("" + zoomFactor, 20, 20);
		g2d.setTransform( form );
	}
	
	public void setIsDrawContent( boolean flag )
	{ isDrawContent = flag; }
	public boolean getIsDrawContent()
	{ return isDrawContent; }
}
