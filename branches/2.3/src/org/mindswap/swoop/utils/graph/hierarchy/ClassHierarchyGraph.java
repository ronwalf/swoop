/*
 * Created on Apr 19, 2005
 */
package org.mindswap.swoop.utils.graph.hierarchy;

import java.util.Collections;
import java.util.Set;

import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.reasoner.SwoopReasoner;
import org.mindswap.swoop.utils.graph.ClassGraphProperties;
import org.mindswap.swoop.utils.graph.GenericGraph;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLException;

/**
 * @author Evren Sirin
 *
 */
public class ClassHierarchyGraph extends GenericGraph {
    public ClassHierarchyGraph(SwoopModel model, OWLClass cls) throws OWLException {
        super(model, equivalents( model, cls ), new ClassGraphProperties( model ));
    }
    
    private static Set equivalents(SwoopModel model, OWLClass cls) throws OWLException {
        SwoopReasoner reasoner = model.getReasoner();
        Set eqs = reasoner.equivalentClassesOf( cls );
        eqs.add( cls );
        
        return Collections.singleton( eqs );
    }
}