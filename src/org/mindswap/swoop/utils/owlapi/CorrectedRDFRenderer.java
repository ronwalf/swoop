/*
 * Created on Apr 4, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.utils.owlapi;

import java.io.Writer;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.mindswap.swoop.utils.rdfapi.EConnTripleVisitor;
import org.mindswap.swoop.utils.rdfapi.NodeFactoryImpl;
import org.mindswap.swoop.utils.rdfapi.TripleVisitor;
import org.semanticweb.owl.io.Renderer;
import org.semanticweb.owl.io.RendererException;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLObjectVisitor;
import org.semanticweb.owl.model.OWLOntology;

import edu.unika.aifb.rdf.api.syntax.RDFSerializer;
import edu.unika.aifb.rdf.api.util.RDFManager;

/**
 * @author ronwalf
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class CorrectedRDFRenderer implements Renderer {

	
	/* (non-Javadoc)
	 * @see org.semanticweb.owl.io.Renderer#renderOntology(org.semanticweb.owl.model.OWLOntology, java.io.Writer)
	 */
	public void renderOntology(OWLOntology ontology, Writer writer) throws RendererException {
		try {
			TripleVisitor visitor = new EConnTripleVisitor(ontology);
			
			ontology.accept(visitor);
			Vector objects = new Vector();
			
			objects.addAll(ontology.getClassAxioms());
			objects.addAll(ontology.getPropertyAxioms());
			objects.addAll(ontology.getIndividualAxioms());
			
			objects.addAll(ontology.getClasses());
			objects.addAll(ontology.getAnnotationProperties());
			objects.addAll(ontology.getDataProperties());
			objects.addAll(ontology.getObjectProperties());
			objects.addAll(ontology.getIndividuals());
			
			
			for (Iterator objectIter = objects.iterator(); objectIter.hasNext();) {
				OWLObject object = (OWLObject) objectIter.next();
				object.accept(visitor);
			}
			
			visitor.serialize(writer);
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new RendererException(e.toString());
		}
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.io.Options#setOptions(java.util.Map)
	 */
	public void setOptions(Map options) throws OWLException {
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.io.Options#getOptions()
	 */
	public Map getOptions() {
		return Collections.EMPTY_MAP;
	}

}
