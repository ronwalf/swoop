package org.mindswap.swoop.utils.graph.hierarchy;

import java.util.Vector;

import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.utils.graph.OntologyGraphProperties;

/**
 * @author Dave Wang
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class OntologyWithClassHierarchyGraphProperties extends OntologyGraphProperties
{
	private Vector myData = null;
	
    public OntologyWithClassHierarchyGraphProperties(SwoopModel model, Vector graphNodes) 
    {
    	super(model);
        myData = graphNodes;
    }

    public int size() 
    {
        return myData.size();
    }

    public OntologyGraphNode getNode( int index )
    {
    	return (OntologyGraphNode)myData.elementAt( index );
    }
    
    public String getPreferredLayout() {
        return "Circle Layout";
    }
}