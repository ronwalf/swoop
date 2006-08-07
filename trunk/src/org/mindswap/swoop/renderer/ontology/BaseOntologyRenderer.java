/*
 * Created on Mar 17, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.mindswap.swoop.renderer.ontology;

import java.awt.Component;
import java.io.Writer;

import javax.swing.JEditorPane;

import org.mindswap.swoop.OntologyDisplay;
import org.mindswap.swoop.SwoopDisplayPanel;
import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.renderer.SwoopOntologyRenderer;
import org.semanticweb.owl.io.RendererException;
import org.semanticweb.owl.model.OWLOntology;

/**
 * @author Dave
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public abstract class BaseOntologyRenderer {

	/**
	 * Create a JEditorPane given the contentType (text/plain or text/html)
	 * and make other default settings (add hyperlink listener, editable false)
	 * @param contentType
	 * @return
	 */
	protected JEditorPane getEditorPane(String contentType, OntologyDisplay OD) {
		JEditorPane editorPane = null;
		if(contentType.equals("text/plain"))
			editorPane = new JEditorPane();
		else if(contentType.equals("text/html")) {
			editorPane = new JEditorPane();
			editorPane.addHyperlinkListener( OD );	
		}
		else if(contentType.equals("text/xml"))
			editorPane = new JEditorPane();
		else
			throw new RuntimeException("Cannot create an editor pane for content type " + contentType);
		
		editorPane.setEditable(false);
		editorPane.setContentType(contentType);
		return editorPane;
	}
}
