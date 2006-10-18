/*
 * Created on Apr 21, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.renderer.entity;

import java.awt.Component;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;

import javax.swing.JEditorPane;

import org.mindswap.swoop.SwoopDisplayPanel;
import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.TermsDisplay;
import org.mindswap.swoop.renderer.BaseEntityRenderer;
import org.mindswap.swoop.renderer.SwoopEditableRenderer;
import org.mindswap.swoop.renderer.SwoopEntityRenderer;
import org.mindswap.swoop.utils.owlapi.QNameShortFormProvider;
import org.mindswap.swoop.utils.rdfapi.EConnTripleVisitor;
import org.mindswap.swoop.utils.rdfapi.HyperXMLWriter;
import org.mindswap.swoop.utils.rdfapi.NodeProvider;
import org.mindswap.swoop.utils.rdfapi.PrettyXMLWriter;
import org.mindswap.swoop.utils.rdfapi.RDFWriter;
import org.mindswap.swoop.utils.rdfapi.SkolemizingNodeProvider;
import org.mindswap.swoop.utils.rdfapi.SwoopResource;
import org.mindswap.swoop.utils.rdfapi.TripleVisitor;
import org.mindswap.swoop.utils.rdfapi.XMLWriter;
import org.semanticweb.owl.io.RendererException;
import org.semanticweb.owl.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLIndividualAxiom;
import org.semanticweb.owl.model.OWLOntology;
import org.xngr.browser.editor.XmlEditorPane;

import edu.unika.aifb.rdf.api.model.ModelException;

/**
 * @author ronwalf
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class RDFXMLEntityRenderer implements SwoopEntityRenderer, SwoopEditableRenderer {

	protected boolean editorEnabled;
	protected JEditorPane pane;

	
	protected String getContentType() {
		if (isEditableText()) {
			return "text/plain";
		} else {
			return "text/html";
		}
	}
	
	/* (non-Javadoc)
	 * @see org.mindswap.swoop.renderer.SwoopRenderer#getDisplayComponent(org.mindswap.swoop.SwoopDisplayPanel)
	 */
	public Component getDisplayComponent(SwoopDisplayPanel panel) {
		if (!(panel instanceof TermsDisplay ))
			throw new IllegalArgumentException();
		
		if (!editorEnabled) {
			// return standard JEditorPane
			pane = BaseEntityRenderer.getEditorPane( this.getContentType(), (TermsDisplay)panel );
		}
		else 
		{
			// XMLPane construction
			pane = new XmlEditorPane();
			pane.addKeyListener((TermsDisplay) panel);
			pane.getDocument().addDocumentListener((TermsDisplay) panel);
		}
		return pane;
	}

	/* (non-Javadoc)
	 * @see org.mindswap.swoop.renderer.SwoopRenderer#getName()
	 */
	public String getName() {
		return "RDF/XML";
	}

	/* (non-Javadoc)
	 * @see org.mindswap.swoop.renderer.SwoopEditableRenderer#isEditableText()
	 */
	public boolean isEditableText() {
		return editorEnabled;
	}
	
	/* (non-Javadoc)
	 * @see org.mindswap.swoop.renderer.SwoopEntityRenderer#render(org.semanticweb.owl.model.OWLEntity, org.mindswap.swoop.SwoopModel, java.io.Writer)
	 */
	public void render(OWLEntity entity, SwoopModel swoopModel, Writer writer) throws RendererException {
		try {
			int fontSize = 3+3*Integer.parseInt(swoopModel.getFontSize());
			
			//XMLEditorPane does not properly work with setFont...
			//Font font =  new Font(swoopModel.getFontFace(), Font.PLAIN, fontSize);
			//pane.setFont(font);
			
			if (swoopModel.getEditorEnabled() && !entity.getOntologies().contains(swoopModel.getSelectedOntology())) {
				// entity is imported..cannot edit here
				writer.write("Cannot edit Imported Entity directly here.");
				return;
			}
			if (!isEditableText()) {
				writer.write("<html><body style=\"font-face:"+swoopModel.getFontFace()+"; background-color: white; color: black; font-size:"+fontSize+"\">");
			}
			
			OWLOntology ontology = swoopModel.getSelectedOntology();
			NodeProvider nodeProvider = new SkolemizingNodeProvider();
			TripleVisitor visitor = new EConnTripleVisitor(ontology, nodeProvider);
			entity.accept(visitor);
			
			XMLWriter xml;
			if (isEditableText()) {
				xml = new PrettyXMLWriter(writer);
			} else {
				xml = new HyperXMLWriter(writer);
			}
			// Add entity for ontology
			
			QNameShortFormProvider qnames = new QNameShortFormProvider();
			String prefix = qnames.findPrefix(ontology.getURI().resolve("#none"));
			if (prefix != null) 
				xml.addEntity(prefix, ontology.getURI().toString());
			
			
			//visitor.serialize(xml, entity);
			RDFWriter rdf = new RDFWriter(xml, visitor.getModel(), qnames);
			rdf.setMaxLevel(10);
			rdf.setBase(ontology.getURI());
			rdf.startDocument();
			
			if (nodeProvider.getNode(entity) instanceof SwoopResource) {
				SwoopResource resource = (SwoopResource) nodeProvider.getNode(entity);
				rdf.serializeSubject(resource);
				
				for (Iterator iter = ontology.getIndividualAxioms().iterator(); iter.hasNext(); ) {
					OWLIndividualAxiom axiom = (OWLIndividualAxiom) iter.next();
					if (axiom instanceof OWLDifferentIndividualsAxiom) {
						if (axiom.getIndividuals().contains(entity)) {
							SwoopResource axiomResource = (SwoopResource) nodeProvider.getNode(axiom);
							rdf.serializeSubject(axiomResource);
						}
					}
				}
			}
			rdf.serializeAll();
			rdf.endDocument();
			
			if (!isEditableText()) {
				writer.write("</FONT></body></html>");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (OWLException e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see org.mindswap.swoop.renderer.SwoopEditableRenderer#setEditorEnabled(boolean)
	 */
	public void setEditorEnabled(boolean mode) {
		editorEnabled = mode;
	}

}
