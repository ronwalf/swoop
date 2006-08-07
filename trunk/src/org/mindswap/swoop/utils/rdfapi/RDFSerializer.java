/*
 * Created on Apr 22, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.utils.rdfapi;

import java.io.IOException;
import java.net.URI;

import org.semanticweb.owl.model.OWLException;

import edu.unika.aifb.rdf.api.model.Model;

/**
 * @author ronwalf
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface RDFSerializer {
	public void serialize(XMLWriter xml, Model model) throws IOException, OWLException;
	public void setBase(URI logicalURI);
}
