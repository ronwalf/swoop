//The MIT License
//
// Copyright (c) 2004 Mindswap Research Group, University of Maryland, College Park
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to
// deal in the Software without restriction, including without limitation the
// rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
// sell copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
// IN THE SOFTWARE.

package org.mindswap.swoop.renderer.entity;

import java.awt.Component;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.mindswap.swoop.SwoopDisplayPanel;
import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.TermsDisplay;
import org.mindswap.swoop.renderer.BaseEntityRenderer;
import org.mindswap.swoop.renderer.SwoopEditableRenderer;
import org.mindswap.swoop.renderer.SwoopEntityRenderer;
import org.mindswap.swoop.renderer.SwoopRenderingVisitor;
import org.mindswap.swoop.utils.HyperDAML;
import org.semanticweb.owl.impl.model.OWLDataFactoryImpl;
import org.semanticweb.owl.impl.model.OWLIndividualImpl;
import org.semanticweb.owl.impl.model.OWLObjectPropertyInstanceImpl;
import org.semanticweb.owl.impl.model.OWLObjectSomeRestrictionImpl;
import org.semanticweb.owl.impl.model.OWLOntologyImpl;
import org.semanticweb.owl.io.RendererException;
import org.semanticweb.owl.io.vocabulary.OWLVocabularyAdapter;
import org.semanticweb.owl.io.vocabulary.RDFSVocabularyAdapter;
import org.semanticweb.owl.io.vocabulary.RDFVocabularyAdapter;
import org.semanticweb.owl.model.OWLAnnotationInstance;
import org.semanticweb.owl.model.OWLAnnotationProperty;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataRange;
import org.semanticweb.owl.model.OWLDataType;
import org.semanticweb.owl.model.OWLDataValue;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLEnumeration;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLIndividualAxiom;
import org.semanticweb.owl.model.OWLNamedObject;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectPropertyInstance;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLProperty;
import org.semanticweb.owl.model.OWLPropertyAxiom;

public class NLEntityRenderer extends BaseEntityRenderer implements SwoopEntityRenderer {
	public static final String INDENT = "  "; 
	
	protected Set allURIs;
	private List shortNames;
	private Map known;
	private int reservedNames;
	private String contentType = "text/html";
	
	public String getContentType() {
		return contentType;
	}
	
	public String getName() {
		return "Natural Language";
	}
	
	public SwoopRenderingVisitor createVisitor() {
		return new NLVisitor(this, swoopModel);
	}
	
	public void render(OWLEntity entity, SwoopModel swoopModel, Writer writer) throws RendererException {	
		super.render(entity, swoopModel, writer);			
	}
	
	
	/*
	 *  SwoopRenderer method
	 * 
	 */
	public Component getDisplayComponent( SwoopDisplayPanel panel )
	{		
		if (!(panel instanceof TermsDisplay ))
			throw new IllegalArgumentException();
		
		return super.getEditorPane( this.getContentType(), (TermsDisplay)panel );
	}
	
	
	
	protected void renderAnnotationContent(Object o) throws OWLException {
		if (o instanceof URI) {
			print(o.toString());
		} else if (o instanceof OWLIndividual) {
			print(((OWLIndividual) o).getURI().toString());
		} else if (o instanceof OWLDataValue) {
			OWLDataValue dv = (OWLDataValue) o;
			print("\"" + escape(dv.getValue()) + "\"");
			/* Only show it if it's not string */
			URI dvdt = dv.getURI();
			String dvlang = dv.getLang();
			if (dvdt != null) {
				print("^^" + dvdt);
				// 		if (!dv.getURI().toString().equals(
				// XMLSchemaSimpleDatatypeVocabulary.INSTANCE.getString())) {
				// 		    print( "^^" + dv.getURI() );
				// 		}
			} else {
				if (dvlang != null) {
					print("@" + dvlang);
				}
			}
		} else {
			print("\""+o.toString()+"\"");
		}
	}

	/** Render the annotations for an object */
	protected void renderAnnotations(OWLNamedObject object ) throws OWLException {
		/* Bit nasty this -- annotations result in a new axiom */
		if (!object.getAnnotations(reasoner.getOntology()).isEmpty()) {
			for (Iterator it = object.getAnnotations(reasoner.getOntology()).iterator(); it.hasNext();) {
				OWLAnnotationInstance oai = (OWLAnnotationInstance) it.next();
				//print("(" + getShortForm(oai.getProperty().getURI()) + " ");
				/* Just whack out the content. This isn't quite right... */
				renderAnnotationContent(oai.getContent());
				//		print( "\"" + oai.getContent() + "\"" );
				print(" ");
				/* Do we need to do this??? */
				visitor.reset();
				oai.accept(visitor);
				// 		if (it.hasNext()) {
				// 		    println();
				// 		}
			}
		}		
	}
	
	protected void renderClass(OWLClass clazz) throws OWLException {
		
		boolean done = false;
		String className = this.getShortForm(clazz.getURI());
		
		// ** global reset tree **
		((NLVisitor) visitor).resetNLTree(className, NLVisitor.LINK_EQUIVALENT, 0);
		
		if(!clazz.getAnnotations(reasoner.getOntology()).isEmpty()) {
			println("Annotations:");
			renderAnnotations(clazz);
			println("\n");
			done = true;
		}
		
		if (clazz.getEquivalentClasses(reasoner.getOntologies()).size() > 0) {
			println("Definition: (Necessary and Sufficient Conditions)");
			
			for (Iterator it = clazz.getEquivalentClasses(reasoner.getOntologies()).iterator(); it.hasNext();) {
				
				OWLDescription eq = (OWLDescription) it.next();				
				eq.accept(visitor);
				
				// ** local reset tree **
				((NLVisitor) visitor).setLinkContext(NLVisitor.LINK_EQUIVALENT);
				((NLVisitor) visitor).resetParent();
			}
			visitor.reset();
			((NLVisitor) visitor).printTree();
			
			// do post processing here, before printing out the final string			
			print(postProcess(visitor.result()));
			done = true;
			println();
			println();
		}
		
		if (!clazz.getSuperClasses(reasoner.getOntologies()).isEmpty()) {
			
			// ** global reset tree **
			((NLVisitor) visitor).resetNLTree(className, NLVisitor.LINK_SUBCLASS, 0);
			
			println("Details: (Necessary Conditions)");
			
			for (Iterator it = clazz.getSuperClasses(reasoner.getOntologies()).iterator(); it.hasNext();) {
				
				OWLDescription eq = (OWLDescription) it.next();
				eq.accept(visitor);			
				
				// ** local reset tree **
				((NLVisitor) visitor).setLinkContext(NLVisitor.LINK_SUBCLASS);
				((NLVisitor) visitor).resetParent();
			}
			
			visitor.reset();
			((NLVisitor) visitor).printTree();
			
			print(postProcess(visitor.result()));
			done = true;
		}
		/*
		 * This has changed -- used to be simply a oneof in the class definition. We now get a
		 * special keyword in the vocabulary
		 */
		for (Iterator it = clazz.getEnumerations(reasoner.getOntologies()).iterator(); it.hasNext();) {
			OWLDescription eq = (OWLDescription) it.next();
			println(
					"EnumeratedClass"
					+ className
					);		
			/* We know that the description has to be a oneof */
			try {
				OWLEnumeration enumeration = (OWLEnumeration) eq;

				for (Iterator iit = enumeration.getIndividuals().iterator(); iit.hasNext();) {
					OWLIndividual desc = (OWLIndividual) iit.next();
					visitor.reset();
					desc.accept(visitor);
					print(" " + postProcess(visitor.result()));
					// 		    if (iit.hasNext()) {
					// 			print(" ");
					// 		    }
				}
				println("");
				done = true;
			} catch (ClassCastException ex) {
				throw new RendererException(ex.getMessage());
			}
		}

		if (!done) {
			/* We need to give at least an empty definition */
			println(
					className
					+ " is an undefined class."
					);
		}
		
	}

	protected void renderIndividual(OWLIndividual ind) throws OWLException {
	    OWLIndividualImpl test = (OWLIndividualImpl) ind;
	    OWLDataFactoryImpl factory = (OWLDataFactoryImpl) test.getOWLDataFactory();
	    
		boolean done = false;
		String indName = this.getShortForm(ind.getURI());
		
		// ** global reset tree **
		//((NLVisitor) visitor).resetNLTree(indName, NLVisitor.LINK_EQUIVALENT);
		
		if(!ind.getAnnotations(reasoner.getOntology()).isEmpty()) {
			println("Annotations:");
			renderAnnotations(ind);
			println("\n");
			done = true;
		}
		
		// ** global reset tree **
		((NLVisitor) visitor).resetNLTree(indName, NLVisitor.LINK_SUBCLASS, 3);
		
		println("Details:");
		for ( Iterator it = ind.getTypes( reasoner.getOntologies() ).iterator(); it.hasNext(); ) {
		
			OWLDescription eq = (OWLDescription) it.next();
			eq.accept(visitor);	
			
			// ** local reset tree **
			((NLVisitor) visitor).setLinkContext(NLVisitor.LINK_SUBCLASS);
			((NLVisitor) visitor).resetParent();
		}
	
//		 ** global reset tree **
		//((NLVisitor) visitor).resetNLTree(indName, NLVisitor.LINK_SOMEVALUES, 3);
		((NLVisitor) visitor).setLinkContext(NLVisitor.LINK_SUBCLASS);
		((NLVisitor) visitor).resetParent();
		
		Map oPropVals = ind.getObjectPropertyValues( reasoner.getOntologies() );
		for ( Iterator it = oPropVals.keySet().iterator(); it.hasNext(); ) {
		    OWLObjectProperty currKey = (OWLObjectProperty) it.next();
		    
		    Set targetSet = (Set) oPropVals.get( currKey );
		    System.out.println( targetSet.getClass() );
		    
		    for ( Iterator t = targetSet.iterator(); t.hasNext(); ) {
		        OWLIndividual target = (OWLIndividual) t.next();
		        
		        System.out.println( target + " ++ " + target.getClass() );
		        
			    OWLObjectPropertyInstance propInst = new OWLObjectPropertyInstanceImpl( factory, ind, currKey, target ); 
			    
			    propInst.accept( visitor );
		    }
		    
//		  ** local reset tree **
			((NLVisitor) visitor).setLinkContext(NLVisitor.LINK_SUBCLASS);
			((NLVisitor) visitor).resetParent();
		}
		
		visitor.reset();
		((NLVisitor) visitor).printTree();
		
		print(postProcess(visitor.result()));
		done = true;
			
		if (!done) {
			/* We need to give at least an empty definition */
			println(
					indName
					+ " is an undefined individual."
					);
		}
		
		return;
	}

	protected void renderAnnotationProperty(OWLAnnotationProperty prop)	throws OWLException {
		println(" <b>AnnotationProperty</b>(" + getShortForm(prop.getURI()) + ")");
	}

	protected void renderObjectProperty(OWLObjectProperty prop) throws OWLException {
	    
		// TODO: Not implemented yet
		pw.print("Natural Language rendering of properties not implemented yet");
		return;
		
	}

	protected void renderDataProperty(OWLDataProperty prop) throws OWLException {

		// TODO: Not implemented yet
		pw.print("Natural Language rendering of properties not implemented yet");
		return;		
	}

	protected void renderDataType(OWLDataType datatype) throws OWLException {
		println("<b>Datatype</b>(" + getShortForm(datatype.getURI()) + ")");
	}
	
	protected void println() {
		pw.println();		
	}
	
	protected void renderEntity() throws OWLException {
		// turn qnames off in Natural Language mode
		//if (swoopModel.showQNames()) swoopModel.setShowQNames(false);
		
		print("<html><body><code><pre><FONT FACE=\""+swoopModel.getFontFace()+"\" SIZE="+fontSize+">");
		super.renderEntity();
		print("</FONT></pre></code></body></html>");
	}

	
	protected void renderPropertyAxiom(OWLPropertyAxiom axiom) throws OWLException {
	}
	protected String sanitize(String str) {
    	
    		str = str.replaceAll("&", "&amp;");
    		str = str.replaceAll("<", "&lt;");
    		str = str.replaceAll(">", "&gt;");
    		return str;
	}
	
	protected void renderIndividualAxiom(OWLIndividualAxiom axiom) throws OWLException {
		OWLIndividual ind = (OWLIndividual) entity;
		pw.println("<rdf:Description rdf:about=\"" + ind.getURI() + "\">");
		Set individuals = axiom.getIndividuals();
		for (Iterator it = individuals.iterator(); it.hasNext(); ) {
				OWLIndividual oi = (OWLIndividual) it.next();
				if(oi.equals(ind)) continue;
				pw.println(INDENT + "<"
						+ (axiom instanceof OWLDifferentIndividualsAxiom ? "owl:differentFrom" : "owl:sameAs")
						+ " rdf:resource=\""
						//			       + getShortForm(oi.getURI())
						+ oi.getURI()
						+ "\"/>");
		}		
		
	}
	
	
	public String getShortForm(URI uri) throws OWLException {
		String sf = shortForm(uri);
		if (sf.indexOf(":")>=0) sf = sf.substring(sf.indexOf(":")+1, sf.length());
		return sf;
	}
	
	protected void renderForeignEntity(OWLEntity ent) throws OWLException {
	}
	
	// eliminate the duplicate words, example has has
	// split the string into words, eliminate the ones that are duplicate and rebuild it
	// right now it only works for 1 word , but it can be extended if needed to handle n-grams
	// it only works when the repetition occurs immediately after, as its purpose is to clean up
	// the has has stuff	
	protected String eliminateDuplicateWords(String str) {
		String[] tokens = str.split("( )+");
		String strTokens = "";
		
		for (int i=0; i<tokens.length-1;i++) {
			if (!tokens[i].equalsIgnoreCase(tokens[i+1]))
				strTokens += (tokens[i] + " ");
		}
		strTokens += tokens[tokens.length-1];
		return strTokens;
	}
	
	protected boolean startsWithVowel(String str) {
		if (str.toLowerCase().charAt(0) == 'a' || str.charAt(0) == 'e' ||
				str.charAt(0) == 'i' || str.charAt(0) == 'o' ||
				str.charAt(0) == 'u' )
			return true;
		else return false;
	}

	protected String fixAorAN(String str) {
		String[] tokens = str.split("( )+");
		String strTokens = "";
		
		for (int i=0; i<tokens.length-1;i++) {
			if (tokens[i].equals("a") && startsWithVowel(tokens[i+1])) 
				strTokens += ("an ");
			else
				strTokens += (tokens[i] + " ");
		}
		strTokens += tokens[tokens.length-1];
		return strTokens;			
	}

	protected String removeThingClause(String str) {
		str = str.replaceAll("and is a thing", "");
		str = str.replaceAll("and is not a thing", "");
		return str;
	}
	
	protected String insertHyperlinks(String tmp) {
		//TODO
//		tmp = " " + tmp +" ";
//		Hashtable hLinks = ((NLVisitor) visitor).hyperlinkMap;
//		for (Iterator iter = hLinks.keySet().iterator(); iter.hasNext(); ) {
//			String key = iter.next().toString();
//			String value = hLinks.get(key).toString();
//			tmp = tmp.replaceAll(" "+key+" ", value);
//		}
		return tmp;
	}
	
	protected String removeHasIsClause(String str) {
		str = str.replaceAll("has is", "is");		
		return str;
	}
	
	protected String insertBreaks(String str) {
		int pos = 0;
		String newStr = "";
		int breakPos = 80;
		while (str.length()>pos+breakPos) {
			newStr += str.substring(pos, pos+breakPos);
			if (!newStr.endsWith(" ")) newStr += "-";
			newStr += "<br>";
			str = str.substring(pos+breakPos, str.length());
			pos += breakPos;
		}
		newStr += str;
		
		return newStr;
	}
	
	protected String postProcess(String str) {
		String tmp = eliminateDuplicateWords(str);
		
		tmp = fixAorAN(tmp);
		
		tmp = removeThingClause(tmp);
		
//		tmp = removeHasIsClause(tmp);
		
//		tmp = insertBreaks(tmp);
		
		//TODO: Need to improve the hyperlinking code
		tmp = insertHyperlinks(tmp); // do this right at the end of post processing
		return tmp;
	}
} 


