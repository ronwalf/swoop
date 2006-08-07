/*
 * Created on Apr 19, 2005
 */
package org.mindswap.swoop.utils.graph;

import java.util.Collection;
import java.util.Vector;

import org.mindswap.swoop.SwoopModel;
import org.semanticweb.owl.model.OWLOntology;

/**
 * @author Evren Sirin
 *
 */
public class OntologyGraph extends GenericGraph {
    public OntologyGraph(SwoopModel model, OWLOntology ont) 
    {
        super(model, ont, new OntologyGraphProperties(model));
    }

    public OntologyGraph(SwoopModel model, Collection partitions) 
    {
        super(model, partitions, new OntologyGraphProperties(model));
    }
}