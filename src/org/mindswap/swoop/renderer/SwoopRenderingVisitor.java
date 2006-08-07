package org.mindswap.swoop.renderer;

import org.semanticweb.owl.model.OWLObjectVisitor;

/**
 * @author Evren Sirin
 */
public interface SwoopRenderingVisitor extends OWLObjectVisitor {
	
	public void reset();
	
	public String result();
}
