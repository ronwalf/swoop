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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.lang.StringEscapeUtils;
import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.renderer.BaseEntityRenderer;
import org.mindswap.swoop.renderer.SwoopRenderingVisitor;
import org.semanticweb.owl.io.ShortFormProvider;
import org.semanticweb.owl.model.OWLAnd;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataAllRestriction;
import org.semanticweb.owl.model.OWLDataCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataEnumeration;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataPropertyInstance;
import org.semanticweb.owl.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owl.model.OWLDataSomeRestriction;
import org.semanticweb.owl.model.OWLDataType;
import org.semanticweb.owl.model.OWLDataValue;
import org.semanticweb.owl.model.OWLDataValueRestriction;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owl.model.OWLDisjointClassesAxiom;
import org.semanticweb.owl.model.OWLEnumeration;
import org.semanticweb.owl.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owl.model.OWLEquivalentPropertiesAxiom;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLFunctionalPropertyAxiom;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLIndividualTypeAssertion;
import org.semanticweb.owl.model.OWLInverseFunctionalPropertyAxiom;
import org.semanticweb.owl.model.OWLInversePropertyAxiom;
import org.semanticweb.owl.model.OWLNot;
import org.semanticweb.owl.model.OWLObjectAllRestriction;
import org.semanticweb.owl.model.OWLObjectCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectPropertyInstance;
import org.semanticweb.owl.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owl.model.OWLObjectSomeRestriction;
import org.semanticweb.owl.model.OWLObjectValueRestriction;
import org.semanticweb.owl.model.OWLOr;
import org.semanticweb.owl.model.OWLProperty;
import org.semanticweb.owl.model.OWLPropertyDomainAxiom;
import org.semanticweb.owl.model.OWLSameIndividualsAxiom;
import org.semanticweb.owl.model.OWLSubClassAxiom;
import org.semanticweb.owl.model.OWLSubPropertyAxiom;
import org.semanticweb.owl.model.OWLSymmetricPropertyAxiom;
import org.semanticweb.owl.model.OWLTransitivePropertyAxiom;
import org.semanticweb.owl.model.helper.OWLObjectVisitorAdapter;

import qtag.Tagger;

public class NLVisitor extends OWLObjectVisitorAdapter implements SwoopRenderingVisitor {

	ShortFormProvider shortForms; 
	StringWriter sw;
	PrintWriter pw;
	SwoopModel swoopModel;
	
	static Tagger tagger;
	
	//**** Define elements to create Natural Language Tree
	NLTree tree;
	NLNode parent;
	NLLink linkContext;
	HashMap nodeStore;
	Hashtable hyperlinkMap;
	OWLProperty linkProp;
	static String LINK_EQUIVALENT = "is"; // NEW CHANGE
	static String LINK_SUBCLASS = "is";
	static String LINK_COMPLEMENT = "is not a";
	static String LINK_INTERSECTION = "is";
	static String LINK_UNION = "or a";
	static String LINK_ALLVALUES = "that always";
	static String LINK_SOMEVALUES = "that";
	static String LINK_HASVALUE = "that";
	static String LINK_MAXCARD = "with at most";
	static String LINK_MINCARD = "with at least";
	static String LINK_CARD = "with exactly";
	static String LINK_ONEOF = "either";
	static String LINK_BETWEEN = "with between";
	//*****
	
	public NLVisitor( ShortFormProvider shortForms, SwoopModel swoopModel )
	{
		try	{
			tagger = new Tagger("lib/qtag-eng");
		}
		catch (IOException e)	{
			System.out.println("Error: POS library not found!");
		}
		this.shortForms = shortForms;
		this.swoopModel = swoopModel;
		hyperlinkMap = new Hashtable();
		reset();
	}
	
	/**
	 * HTML-escape an object
	 * @param o
	 * @return
	 */
	private static String escape(Object o) {
		return StringEscapeUtils.escapeHtml(o.toString());
	}
	
	public String result() {
		return sw.toString();
	}

	public void reset() {
		sw = new StringWriter();
		pw = new PrintWriter( sw );		
	}
	
	public void resetNLTree(String entityName, String link, int type) {
		// set entity name as the root node of the tree
		NLNode root = new NLNode(entityName, type);
		// create a new NL tree with this root
		this.tree = new NLTree(root);
		// set current parent in NL-Visitor as root
		this.parent = root;
		// set current link (use argument passed as linktype)
		this.linkContext = new NLLink("", link);
		// used to reset parents during each iteration of a and/or/oneof
		nodeStore = new HashMap();
	}
	
	public void resetParent() {
		this.parent = tree.getRoot();
	}
	
	public void setLinkContext(String linkType) {
		linkContext.setLinkType(linkType);
		linkContext.setKeyword("");
	}
	
	public void visit( OWLClass clazz ) throws OWLException {
		// build NL tree 
		String className = getShortForm(clazz.getURI());
		String tokens = getEntityTokens(className);
        // add hyperlink
		hyperlinkMap.put(tokens, "<a href=\"" + clazz.getURI() + "\">" + tokens + "</a>");
		NLNode target = new NLNode(tokens, 0);
		
		parent.addLink(linkContext, target);					    
	}
	
	public void visit( OWLIndividual ind ) throws OWLException {
		// build NL tree 
		String indName = getShortForm(ind.getURI());
		String tokens = getEntityTokens(indName);
        // add hyperlink
		hyperlinkMap.put(tokens, "<a href=\"" + ind.getURI() + "\">" + tokens + "</a>");
		NLNode target = new NLNode(tokens, 3);
		
		parent.addLink(linkContext, target);		
	}
	
	// TODO this is just object properties
	public void visit( OWLObjectProperty prop ) throws OWLException {
		// build NL tree 
		String propName = getShortForm(prop.getURI());
		String tokens = getEntityTokens(propName).toLowerCase();
		String[] tok = getEntityTokens(propName).toLowerCase().split("( )+");
		if ( !tokens.startsWith( "has"  ) && !tokens.startsWith( "is" )) {
		    
			// tagger.setInput(tokens);
            String[] tags = tagger.tag(tok);
            System.out.print(tokens + " TAG:"  );	
            
            for ( int i = 0; i < tags.length; i++ ) {
                System.out.print( tags[i].toString() + " " );
            }
            System.out.println();
            
            if ( tok.length == 1 ) {
//              TODO: there is a problem if the word is ambiguous between a noun and a verb
                // for example: drives. a possible solution is to get the complete tagging and see if
                // such ambiguity exists, and always prefer the verb form, since plural nouns in properties 
                // are apparently rare
                
                // To solve this problems, strips 's' from word and checks if the resulting form can be a verb
                if ( tags[0].equals( "NNS" ) ) {
                    String possibleVerb = tok[0].substring( 0, tok[0].length() - 2 );
                    String[] testTags = tagger.tag( tok );
                    if ( testTags[0].startsWith( "V" ) ) {
                        tok[0] = possibleVerb;
                        tags = tagger.tag(tok);
                        //System.out.print(tokens + " NEW TAG:"  );
                    }
                }
                
    			if( tags[0].startsWith( "VB" ) ){
    			    if ( tags[0].equals( "VBN" ) ) {
    			        tokens = "is " + tokens; // FOR TESTING NOW
    			    } else if ( tags[0].startsWith( "VBD" ) ) {
    			        tokens = "is " + tokens;
    			    }
                } else if ( tags[0].startsWith( "N" ) && !tags[0].endsWith( "S" ) ) {  // only singular nouns
                    tokens = "has " + tokens;
                } else { 
                    //tokens = "has " + tokens;
                    // unidentified prop type?
                }
            } else { 
                if ( tags[0].startsWith( "NN" ) ) {
                    String possibleVerb = tok[0].substring( 0, tok[0].length() - 2 );
                    String[] testTags = tagger.tag( tok );
                    if ( testTags[0].startsWith( "V" ) ) {
                        tok[0] = possibleVerb;
                        tags = tagger.tag(tok);
                    }
                }
                
                int propClass = classifyComplexProp( tags, tok );
                
                System.err.println( propClass );
                
//              0 = complex np (just multiple nouns)			phone number
            	// 1 = np and p 								child of
            	// 2 = vp and np								produces wine
            	// 3 = vp and p 								located in
            	// 4 = vp and pp (p and np)						made from grape
            	// -1 = other (unrecognized)					prop12
                switch ( propClass ) {
                	case 0: 
                	    tokens = "has " + tokens;
                	    break;
            	    case 1:
            	        tokens = "is a " + tokens;
            	        break;
            	    case 2: 
        	        case 3: 
        	        case 4:
            	        if ( tags[0].equals( "VBN" ) ) {
        			        tokens = "is " + tokens; // FOR TESTING NOW
        			    } else if ( tags[0].startsWith( "VBD" ) ) {
        			        tokens = "is " + tokens;
        			    }
            	        break;
            	    case 5:
            	        // unfortunately can't get here
            	        String newTokens = new String( tok[0] + "a " );
            	        for ( int i = 1; i < tok.length; i++ ) {
            	            newTokens = newTokens.concat( tok[i] + " " );
            	        }
            	        
            	        tokens = newTokens;
            	        
            	        break;
                }               
            }
            
		} else if ( tokens.startsWith( "is" ) && tok.length > 1 ) {
		    String[] tags = tagger.tag(tok);
            System.out.print(tokens + " TAG:"  );	
            
            for ( int i = 0; i < tags.length; i++ ) {
                System.out.print( tags[i].toString() + " " );
            }
            System.out.println();
		    
		    int propClass = classifyComplexProp( tags, tok );
            
            if ( propClass == 5 ) {
    	        String newTokens = new String( tok[0] + " a " );
    	        for ( int i = 1; i < tok.length; i++ ) {
    	            newTokens = newTokens.concat( tok[i] + " " );
    	        }
    	        
    	        tokens = newTokens;
            }               
		}
		
		// add hyperlink
		hyperlinkMap.put(tokens, "<a href=\"" + prop.getURI() + "\">" + tokens + "</a>");
		
		linkContext.setKeyword(tokens);
		linkProp = prop;
	}
	
	
	
	// TODO dataproperty is not the same as 
	public void visit( OWLDataProperty prop ) throws OWLException {
		// build NL tree 
		String propName = getShortForm(prop.getURI());
		//
        String tokens = getEntityTokens(propName).toLowerCase();
        String[] tok = getEntityTokens(propName).toLowerCase().split("( )+");
		if (!tokens.startsWith("has") && !tokens.startsWith("is")) {
			
            String[] tags = tagger.tag(tok);
            // System.out.println(tokens+" TAG:"+tags);	
			if(tags[0].startsWith("VB")){
                tokens = "is "+tokens; // heuristic for verbs
            }
            else{
               tokens = "has " + tokens; // heuristic for nouns
            }
		}
		
		// add hyperlink
		hyperlinkMap.put(tokens, "<a href=\"" + prop.getURI() + "\">" + tokens + "</a>");
		
		linkContext.setKeyword(tokens);
		linkProp = prop;
	}
	
	public void visit( OWLDataValue cd ) throws OWLException {
		
		String dVal = " \"" + escape( cd.getValue() ) + "\"";
		NLNode target = new NLNode(getEntityTokens(dVal), 3);
		parent.addLink(linkContext, target);
	}

	public void visit( OWLAnd and ) throws OWLException {
		
		if (linkProp!=null) 
		    prefixDomain(linkProp); // suppose object of prop restr is a intersection
		
		String saveCode = String.valueOf(this.parent.hashCode());
		nodeStore.put(saveCode, parent);
		
		boolean restoreLC = linkContext.isComplement(); // restore link complement for each intersection element
		
		for ( Iterator it = and.getOperands().iterator();
		it.hasNext(); ) {
			linkContext.setLinkType(LINK_INTERSECTION);
			linkContext.setKeyword("");
			linkContext.setIsComplement(restoreLC);
			OWLDescription desc = (OWLDescription) it.next();
			desc.accept( this );
			this.parent = (NLNode) nodeStore.get(saveCode); // reset parent at each iteration
			
			// TESTING:
//			this.printTree();
//			System.out.println();
		}
	}

	public void visit( OWLOr or ) throws OWLException {
		
		if (linkProp!=null) prefixDomain(linkProp); // suppose object of prop restr is a union
		String saveCode = String.valueOf(this.parent.hashCode());
		nodeStore.put(saveCode, parent);
		
		boolean restoreLC = linkContext.isComplement(); // restore link complement for each union element
		
		for ( Iterator it = or.getOperands().iterator();
		it.hasNext(); ) {
			linkContext.setLinkType(LINK_UNION);
			linkContext.setKeyword("");
			linkContext.setIsComplement(restoreLC);
			OWLDescription desc = (OWLDescription) it.next();
			desc.accept( this );
			this.parent = (NLNode) nodeStore.get(saveCode); // reset parent at each iteration
		}
	}

	public void visit( OWLNot not ) throws OWLException {
		
		if (linkProp!=null) prefixDomain(linkProp); // suppose object of prop restr is a complement
		linkContext.setIsComplement(true); // used to print "not" when object is a description
		linkContext.setLinkType(NLVisitor.LINK_COMPLEMENT); // used to print "not" when object is a class
		linkContext.setKeyword("");
		OWLDescription desc = not.getOperand();
		desc.accept( this );		
	}

	public void visit( OWLEnumeration enumeration ) throws OWLException {
		
	    System.out.println( "Visiting Enumeration" );
	    
		String saveCode = String.valueOf(this.parent.hashCode());
		nodeStore.put(saveCode, parent);
		
		boolean restoreLC = linkContext.isComplement(); // restore link complement for each oneof element
		
		//NLLink originalLinkContext = linkContext;
		
		
		String enumString = new String();
		
		for ( Iterator it = enumeration.getIndividuals().iterator(); it.hasNext(); ) {
			//linkContext.setLinkType(LINK_ONEOF);
			//linkContext.setIsComplement(restoreLC);
			OWLIndividual desc = (OWLIndividual) it.next();
			//desc.accept( this );
			//this.parent = (NLNode) nodeStore.get(saveCode); // reset parent at each iteration
			
			String indName = getShortForm(desc.getURI());
			String tokens = getEntityTokens(indName);
			enumString = enumString.concat( indName + ";;;" );
		}		
		
		NLNode enum_ = new NLNode( enumString, 4 ); // create dummy node ?
		parent.addLink( linkContext, enum_ );
	}

	public void visit( OWLObjectSomeRestriction restriction ) throws OWLException {
		prefixDomain(restriction.getObjectProperty());
		linkContext.setLinkType(LINK_SOMEVALUES);
		restriction.getObjectProperty().accept( this );
		restriction.getDescription().accept( this );		
	}

	public void visit( OWLObjectAllRestriction restriction ) throws OWLException {
		prefixDomain(restriction.getObjectProperty());
		linkContext.setLinkType(LINK_ALLVALUES);
		restriction.getObjectProperty().accept( this );	
		restriction.getDescription().accept( this );
	}

	public void visit( OWLObjectValueRestriction restriction ) throws OWLException {
		prefixDomain(restriction.getObjectProperty());				
		linkContext.setLinkType(LINK_HASVALUE);
		restriction.getObjectProperty().accept( this );
		restriction.getIndividual().accept( this );		
	}

	public void visit( OWLDataSomeRestriction restriction ) throws OWLException {
		prefixDomain(restriction.getDataProperty());
		linkContext.setLinkType(LINK_SOMEVALUES);
		restriction.getDataProperty().accept( this );
		restriction.getDataType().accept( this );		
	}

	public void visit( OWLDataAllRestriction restriction ) throws OWLException {
		prefixDomain(restriction.getDataProperty());
		linkContext.setLinkType(LINK_ALLVALUES);
		restriction.getDataProperty().accept( this );		
		restriction.getDataType().accept( this );		
	}

	public void visit( OWLObjectCardinalityRestriction restriction ) throws OWLException {
		
		prefixDomain(restriction.getObjectProperty());
		int ncard = -1;
		if ( restriction.isExactly() ) {
			linkContext.setLinkType(LINK_CARD);
			ncard = restriction.getAtLeast();			
		} 
		else if ( restriction.isAtMost() ) {
			linkContext.setLinkType(LINK_MAXCARD);
			ncard = restriction.getAtMost();			
		} 
		else if ( restriction.isAtLeast() ) {
			linkContext.setLinkType(LINK_MINCARD);
			ncard = restriction.getAtLeast();			
		}  
		restriction.getObjectProperty().accept( this );
		
		NLNode cardNode = new NLNode(String.valueOf(ncard), 2);
		parent.addLink(linkContext, cardNode); 		
	}

	public void visit( OWLDataCardinalityRestriction restriction ) throws OWLException {
		
		prefixDomain(restriction.getProperty());
		int ncard = -1;
		if ( restriction.isExactly() ) {
			linkContext.setLinkType(LINK_CARD);
			ncard = restriction.getAtLeast();			
		} 
		else if ( restriction.isAtMost() ) {
			linkContext.setLinkType(LINK_MAXCARD);
			ncard = restriction.getAtMost();			
		} 
		else if ( restriction.isAtLeast() ) {
			linkContext.setLinkType(LINK_MINCARD);
			ncard = restriction.getAtLeast();			
		}  
		restriction.getDataProperty().accept( this );
		
		NLNode cardNode = new NLNode(String.valueOf(ncard), 2);
		parent.addLink(linkContext, cardNode);
	}

	public void visit( OWLDataValueRestriction restriction ) throws OWLException {
		prefixDomain(restriction.getDataProperty());
		linkContext.setLinkType(LINK_HASVALUE);
		restriction.getDataProperty().accept( this );
		restriction.getValue().accept( this );		
	}

	public void visit( OWLEquivalentClassesAxiom axiom ) throws OWLException {
		pw.print("EquivalentClasses(");
		for ( Iterator it = axiom.getEquivalentClasses().iterator();
		it.hasNext(); ) {
			OWLDescription desc = (OWLDescription) it.next();
			desc.accept( this );
			if (it.hasNext()) {
				pw.print(" ");
			}
		}
		pw.print(")");
	}

	public void visit( OWLDisjointClassesAxiom axiom ) throws OWLException {
		pw.print("(");
		for ( Iterator it = axiom.getDisjointClasses().iterator();
		it.hasNext(); ) {
			OWLDescription desc = (OWLDescription) it.next();
			desc.accept( this );
			if (it.hasNext()) {
				pw.print(" " + ConciseFormat.DISJOINT + " ");
			}
		}
		pw.print(")");
	}

	public void visit( OWLSubClassAxiom axiom ) throws OWLException {
		pw.print("(");
		axiom.getSubClass().accept( this );
		pw.print(" " + ConciseFormat.SUBSET + " ");
		axiom.getSuperClass().accept( this );
		pw.print(")");
	}

	public void visit( OWLEquivalentPropertiesAxiom axiom ) throws OWLException {
		pw.print("(");
		for ( Iterator it = axiom.getProperties().iterator();
		it.hasNext(); ) {
			OWLProperty prop = (OWLProperty) it.next();
			prop.accept( this );
			if (it.hasNext()) {
				pw.print(" = ");
			}
		}
		pw.print(")");
	}

	public void visit( OWLSubPropertyAxiom axiom ) throws OWLException {
		pw.print("(");
		axiom.getSubProperty().accept( this );
		pw.print(" " + ConciseFormat.SUBSET + " ");
		axiom.getSuperProperty().accept( this );
		pw.print(")");
	}

	public void visit( OWLDifferentIndividualsAxiom ax) throws OWLException {
		pw.print("(");
		for ( Iterator it = ax.getIndividuals().iterator();
		it.hasNext(); ) {
			OWLIndividual desc = (OWLIndividual) it.next();
			desc.accept( this );
			if (it.hasNext()) {
				pw.print(" " + ConciseFormat.DISJOINT + " ");
			}
		}
		pw.print(")");
	}

	public void visit( OWLSameIndividualsAxiom ax) throws OWLException {
		pw.print("SameIndividual(");
		for ( Iterator it = ax.getIndividuals().iterator();
		it.hasNext(); ) {
			OWLIndividual desc = (OWLIndividual) it.next();
			desc.accept( this );
			if (it.hasNext()) {
				pw.print(" = ");
			}
		}
		pw.print(")");
	}

	public void visit( OWLDataType ocdt ) throws OWLException {
		//pw.print( "<a href=\"" + ocdt.getURI() + "\">" + getShortForm( ocdt.getURI() ) + "</a>" );		
		//pw.print( getShortForm( ocdt.getURI() ) );
	}

	public void visit( OWLDataEnumeration enumeration ) throws OWLException {
		pw.print("{");
		for ( Iterator it = enumeration.getValues().iterator();
		it.hasNext(); ) {
			linkContext.setLinkType(LINK_ONEOF);
			OWLDataValue desc = (OWLDataValue) it.next();
			desc.accept( this );
			if (it.hasNext()) {
				pw.print(", ");
			}
		}
		pw.print("}");
	}
	
	public void prefixDomain(OWLProperty prop) {
		
	    System.out.print( "PROP: " + prop );
	    
		String domainName = "";
		URI uri = null;
		try {
			for (Iterator it = swoopModel.getReasoner().domainsOf(prop).iterator(); it.hasNext();) {
				OWLDescription dom = (OWLDescription) it.next();
				if (dom instanceof OWLClass) {
					uri = ((OWLClass) dom).getURI();
					domainName = getShortForm(((OWLClass) dom).getURI());
					break;
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if (domainName.equals("")) domainName = "Thing"; // THIS WAS CHANGED FROM thing
			
			String tokens = getEntityTokens(domainName);
	        // add hyperlink
			hyperlinkMap.put(tokens, "<a href=\"" + uri + "\">" + tokens + "</a>");
			NLNode target = new NLNode(tokens, 0);
			
			//NLLink isaLink = new NLLink("", this.LINK_SUBCLASS);
			parent = parent.addLink(linkContext, target);
		}		
		
		System.out.println( " DOM: " + domainName );
	}
	
	// this is what does the splitting of prop names into tokens, here is where we add 
	// delimiter support in the future
	public String getEntityTokens(String entityName) {
		
		String[] tokens = new String[10];
		String strTokens = "";
		int tLen = 0;
		int prev = 0;
		
		for (int i=1; i<entityName.length(); i++) {
		    if ( Character.isUpperCase( entityName.charAt( i ) ) ) {
			//if (entityName.charAt(i) >= 'A' && entityName.charAt(i) <= 'Z') {
				if (i-prev>0) {
					strTokens += (entityName.substring(prev,i) + " ");
					tokens[tLen++] = entityName.substring(prev,i);
					prev = i;
				}
			}			
			if (i==(entityName.length()-1)) {
				// System.out.println(i+":"+prev+":"+entityName+":"+entityName.substring(prev));
				tokens[tLen] = entityName.substring(prev);				
				strTokens += entityName.substring(prev);
			}
		}
		
		return strTokens;
		
	}
	
	public String getShortForm(URI uri) throws OWLException {
		String sf = shortForms.shortForm(uri);
		if (sf.indexOf(":")>=0) sf = sf.substring(sf.indexOf(":")+1, sf.length());
		return sf;
	}
	
	public void printTree() {
	    
		NLNode root = tree.getRoot();
		cleanUpThingNodes();
		printNode(root, true, 0);		
	}
	
	private void putLinks(List sorted, HashMap map) {
		for (Iterator i = map.keySet().iterator(); i.hasNext(); ) {
			NLLink link = (NLLink) i.next();
			NLNode node = (NLNode) map.get(link);
			sorted.add(new NLLinkNode(link, node));
		}
	}
	
	// onlyPrefix == false means return the rest, not the whole thing
	private String getPart(String sentence, boolean onlyPrefix) {
		
		// remove hyperlink stuff if any
		if (sentence.indexOf("<a href")>=0) {
			sentence = sentence.substring(sentence.indexOf("\">")+2, sentence.indexOf("</a>"));
		}
		
		if (sentence.indexOf(" ")==-1) return sentence;
		if (onlyPrefix) {
			String prefix = sentence.substring(0, sentence.indexOf(" "));
			return prefix;
		}
		else {
			String rest = sentence.substring(sentence.indexOf(" ")+1, sentence.length());
			return rest;
		}
	}
	
	/* New NLP Functions - Daniel */
	
	// 0 = complex np (just multiple nouns)			phone number
	// 1 = np and p 								child of
	// 2 = vp and np								produces wine
	// 3 = vp and p 								located in
	// 4 = vp and pp (p and np)						made from grape
	// 5 = is NP of									is specific function of
	// -1 = other (unrecognized)					prop12
	
	private int classifyComplexProp( String[] tags, String[] tokens ) {
	    boolean containsVerb = false;
	    boolean containsPrep = false;
	    boolean containsNoun = false;
	    boolean containsOf = false;
	    boolean containsIs = false;
	    
	    for ( int i = 0; i < tags.length; i++ ) {
	        String curr = tags[i];
	    
	        System.err.println( tokens[i] + " " + tags[i] );
	        
	        if ( curr.startsWith( "V" ) ) {
	            containsVerb = true;
	        } else if ( curr.startsWith( "N" ) ) {
	            containsNoun = true;
	            
	            if ( i == tags.length - 1 ) {
	                if ( containsVerb && containsPrep ) {
	                    return 4;
	                } else if ( containsVerb && !containsPrep ) {
	                    return 2;
	                } 
	            }
	        } else if ( curr.startsWith( "IN" ) ) {
	            containsPrep = true;
	            
	            System.out.println( "PREP: " + tokens[i] );
	            
	            if ( tokens[i].equals( "of" ) ) 
	                containsOf = true;
	            
	            if ( i == tags.length - 1 ) {
	                if ( containsOf ) { 		// I assume that anything with of will always have an NP
	                    if ( containsIs ) { 	// is brother of
	                        System.out.println( "RETURNING 5" );
	                        return 5;
	                    } else {				// brother of
	                        return 1;
	                    }
	                }
	                
	                if ( containsVerb ) {
	                    return 3;
	                } else if ( containsNoun ) {
	                    return 1; 
	                }
	                
//	                if ( containsNoun && !containsVerb ) {
//	                    return 1;
//	                } else if ( containsVerb && !containsNoun ) {
//	                    return 3;
//	                } 
	            }
		    } else if ( curr.startsWith( "B" ) ) {
		        containsIs = true;
		    }
	    }
	    
	    if ( containsOf && containsIs ) 
	        return 5;
	    
	    if ( containsNoun && !containsVerb && !containsPrep ) {
	        return 0;
	    }
	    
	    return -1;
	}
	
	private void cleanUpThingNodes() {
	    NLNode root = tree.getRoot();
	    
	    System.out.println( "BEGINNING THING REMOVAL" );
	    
	    printDebugTree();
	    
	    reconcileThingNodes( root );
	    
	    printDebugTree();
	    
	    //reconcileNegativeThingNodes( root );
	    
	    //printDebugTree();
	    
	    reconcileNonIsaThingNodes( root );
	    
	    printDebugTree();
	    
	    System.out.println( "END THING REMOVAL" );
	}
	
	public void printDebugTree() {
	    printDebugNode( tree.getRoot(), 0 );
	}
	
	private void printDebugNode( NLNode node, int indent ) {
	    System.out.println( node.keyword );
	    
	    HashMap links = node.getLinks();
	    Set keys = links.keySet();

		for (Iterator iter = keys.iterator(); iter.hasNext(); ) {
			// cycle through each link
			NLLink link = (NLLink) iter.next();
			NLNode target = (NLNode) links.get(link);
			
			for ( int i = 0; i < indent; i++ ) 
			    System.out.print( "\t" );
			
			System.out.print( "(" + link.linkType + ") " + link.keyword + " " );
			
			printDebugNode( target, indent + 1 );
		}
	}
	
	// TODO make this fix the problem of multiple all values on same property
	private void mergeAllValues( NLNode node ) {
	    
	}
	
	// PROPERTIES
	private void reconcileNonIsaThingNodes( NLNode node ) {
	    HashMap links = node.getLinks();
	    Set keys = links.keySet();
	    NLLink isaLink = new NLLink("", NLVisitor.LINK_SUBCLASS);

	    boolean thingFound = false;
	    Set isaLinks = new HashSet();
	    //Set nonIsaLinks = new HashSet();
	    Set nonIsaThingLinks = new HashSet(); // may be more than one, unlike is-a
		for (Iterator iter = new HashSet(keys).iterator(); iter.hasNext(); ) {
			// cycle through each link
			NLLink link = (NLLink) iter.next();
			NLNode target = (NLNode) links.get(link);
			
			System.out.println( "LINK: " + link.keyword + " (" + link.linkType + "), TARGET: " + target.keyword );
			
			if ( !link.equals( isaLink ) ) {
			    //nonIsaLinks.add( link );
			    
			    if ( target.getKeyword().equals( "Thing" ) || target.getKeyword().equals( "thing" ) ) {
			        System.out.println( "NON-ISA thing found" );
			        nonIsaThingLinks.add( link );
			    } 
			}
		} 
		
		// pulling-up named classes for non-isa thing links
		for ( Iterator nt = nonIsaThingLinks.iterator(); nt.hasNext(); ) {
		    NLLink curr = (NLLink) nt.next();
		    
		    NLNode thingNode = (NLNode) links.get( curr );
		    HashMap tLinks = thingNode.getLinks();
		    
		    Set tNonThingTargets = new HashSet();
		    Set tKeys = tLinks.keySet();
		    for ( Iterator i = tKeys.iterator(); i.hasNext(); ) {
		        NLLink tLink = (NLLink) i.next();
				NLNode tTarget = (NLNode) tLinks.get(tLink);
				
				if ( tLink.equals( isaLink ) ) {
				    if ( !tTarget.getKeyword().equals( "Thing" ) && !tTarget.getKeyword().equals( "thing" ) ) {
				        System.out.println( "pull-up" );
				        
				        System.out.println( links );
				        
				        links.remove( curr );
				        
				        links.put( curr, tTarget );
				        
				        tTarget.links.putAll( thingNode.links );
				        tTarget.links.remove( tLink );
				    } 
				}
		    }    
		}
	    
		// Recurse
		keys = links.keySet();
		for (Iterator iter = keys.iterator(); iter.hasNext(); ) {
			NLLink link = (NLLink) iter.next();
			NLNode target = (NLNode) links.get(link);
			
			System.out.println( "NEW LINK: " + link.keyword + " (" + link.linkType + "), TARGET: " + target.keyword );

			reconcileNonIsaThingNodes( target );
		}
	}
	
	// recursive function to squash Thing nodes - COMPLEMENT
	private void reconcileNegativeThingNodes( NLNode node ) {
	    HashMap links = node.getLinks();
	    NLLink isNotALink = new NLLink("", NLVisitor.LINK_COMPLEMENT);

	    // first: gather all the is-a links
	    NLLink nonThingLink = null;
	    NLLink thingLink = null;
	    boolean thingFound = false;
	    Set isaLinks = new HashSet();
		for (Iterator iter = links.keySet().iterator(); iter.hasNext(); ) {
			NLLink link = (NLLink) iter.next();
			NLNode target = (NLNode) links.get(link);
			
			//System.out.println( "LINK: " + link.keyword + " (" + link.linkType + "), TARGET: " + target.keyword );
			
			if ( link.equals( isNotALink ) ) {
			    isaLinks.add( link );
			    
			    if ( target.getKeyword().equals( "Thing" ) || target.getKeyword().equals( "thing" ) ) {
			        //System.out.println( "thing found" );
			        thingFound = true;
			        thingLink = link;
			    } else {
			        nonThingLink = link;
			    }
			} 
		} 
		
		//System.out.println( "NEG-ISA's: " + isaLinks.size() + " " + thingFound);
		
		if ( thingFound ) {
		    if ( isaLinks.size() == 0 ) {
		        // this should never happen 
		    } else if ( isaLinks.size() == 1 ) {
			    // there is only one node and it is a thing node, so we can safely pull its links up
			    NLNode thingNode = (NLNode) links.get( thingLink );
			    
			    HashMap tLinks = thingNode.getLinks();
			    node.links.putAll( tLinks );
			    links.remove( thingLink );
			} else {
			    NLNode thingNode = (NLNode) links.get( thingLink );
			    NLNode nonThingNode = (NLNode) links.get( nonThingLink );
			    
			    HashMap tLinks = thingNode.getLinks();
			    HashMap ntLinks = nonThingNode.getLinks();
			    ntLinks.putAll( tLinks );
			    links.remove( thingLink );
			}
		}

		// Recurse
		for (Iterator iter = links.keySet().iterator(); iter.hasNext(); ) {
			NLLink link = (NLLink) iter.next();
			NLNode target = (NLNode) links.get(link);
			
			//System.out.println( "NEW LINK: " + link.keyword + " (" + link.linkType + "), TARGET: " + target.keyword );

			reconcileNegativeThingNodes( target );
		}
	}
	
	// recursive function to squash Thing nodes - ISA
	private void reconcileThingNodes( NLNode node ) {
	    HashMap links = node.getLinks();
	    Set keys = links.keySet();
	    NLLink isaLink = new NLLink("", NLVisitor.LINK_SUBCLASS);

	    // first: gather all the is-a links
	    NLLink nonThingLink = null;
	    NLLink thingLink = null;
	    boolean thingFound = false;
	    Set isaLinks = new HashSet();
		for (Iterator iter = new HashSet(keys).iterator(); iter.hasNext(); ) {
			// cycle through each link
			NLLink link = (NLLink) iter.next();
			NLNode target = (NLNode) links.get(link);
			
			System.out.println( "LINK: " + link.keyword + " (" + link.linkType + "), TARGET: " + target.keyword );
			
			if ( link.equals( isaLink ) ) {
			    isaLinks.add( link );
			    
			    if ( target.getKeyword().equals( "Thing" ) || target.getKeyword().equals( "thing" ) ) {
			        System.out.println( "thing found" );
			        thingFound = true;
			        thingLink = link;
			    } else {
			        nonThingLink = link;
			    }
			} 
		} 
		
		System.out.println( "ISA's: " + isaLinks.size() + " " + thingFound);
		
		if ( thingFound ) {
		    if ( isaLinks.size() == 0 ) {
		        // this should never happen 
		    } else if ( isaLinks.size() == 1 ) {
			    // there is only one node and it is a thing node, so we can safely pull its links up
			    NLNode thingNode = (NLNode) links.get( thingLink );
			    
			    HashMap tLinks = thingNode.getLinks();
			    node.links.putAll( tLinks );
			    links.remove( thingLink );
			} else {
			    NLNode thingNode = (NLNode) links.get( thingLink );
			    NLNode nonThingNode = (NLNode) links.get( nonThingLink );
			    
			    HashMap tLinks = thingNode.getLinks();
			    HashMap ntLinks = nonThingNode.getLinks();
			    ntLinks.putAll( tLinks );
			    links.remove( thingLink );
			}
		}

		// Recurse
		keys = links.keySet();
		for (Iterator iter = keys.iterator(); iter.hasNext(); ) {
			NLLink link = (NLLink) iter.next();
			NLNode target = (NLNode) links.get(link);
			
			System.out.println( "NEW LINK: " + link.keyword + " (" + link.linkType + "), TARGET: " + target.keyword );

			reconcileThingNodes( target );
		}
	}
	
	private boolean containsNP( String propName ) {
	    if ( propName.startsWith( "has" ) || propName.startsWith( "is" ) ) 
	        return true;
	    
	    String[] tokens =  propName.split("( )+");
	    String[] tags = tagger.tag( tokens );
	    if ( tokens.length > 1 ) {
	        int result = classifyComplexProp( tags, tokens );
	        
	        if ( result == 2 || result == 4 )
	            return true;
	    }
	    
	    return false;
	}
	
	private void processAllValues( NLNode node, int indent ) {
	    
	    HashMap links = node.getLinks();
	    boolean allPrinted = false;
	    
	    NLLink isaLink = new NLLink( "" , NLVisitor.LINK_SUBCLASS);
		for (Iterator iter = links.keySet().iterator(); iter.hasNext(); ) {
			// cycle through each link
			NLLink link = (NLLink) iter.next();
			NLNode target = (NLNode) links.get(link);
			
			System.out.println( "LINK: " + link.keyword + " (" + link.linkType + "), TARGET: " + target.keyword );
			
			if ( link.equals( isaLink ) ) {
			    for ( Iterator ti = target.getLinks().keySet().iterator(); ti.hasNext(); ) { 
			        NLLink tLink = (NLLink) ti.next();
			        NLNode tTarget = (NLNode) target.getLinks().get( tLink );
			        
			        if ( tLink.getLinkType().equals( NLVisitor.LINK_ALLVALUES ) ) {
			            System.out.println( "All Values present" );
			            
			            boolean simpleAll = !containsNP( tLink.getKeyword() );
			            
			            if ( simpleAll ) {
				            pw.print( "If a " + node.getKeyword() + " " + tLink.getKeyword() + " something" );
				            pw.println( ", then that thing:" );
				            pw.print( "\t- " );
			            } else {
			                pw.print( "If a " + node.getKeyword() + " " + getPart( tLink.getKeyword(), true ) + " a " + getPart( tLink.getKeyword(), false ) );
				            pw.println( ", then that " + getPart( tLink.getKeyword(), false ) + ":" );
				            pw.print( "\t- " );
			            }
					    
			            // old part
			            pw.print( link.getLinkType() + " " );
			            
			            // new part
			            printNode( tTarget, false, indent + 1 );
			            
			            // the all-values is already printed, so remove it
			            ti.remove();
			            allPrinted = true;
				    }
			        
					if ( allPrinted ) 
					    pw.println();
			    }
			} else if ( link.getLinkType().equals( NLVisitor.LINK_ALLVALUES ) ) {
			    System.out.println( "All Values present" );
	            
			    boolean simpleAll = !containsNP( link.getKeyword() );
	            
	            if ( simpleAll ) {
		            pw.print( "If a " + node.getKeyword() + " " + link.getKeyword() + " something" );
		            pw.println( ", then that thing:" );
		            pw.print( "\t- " );
	            } else {
	                pw.print( "If a " + node.getKeyword() + " " + getPart( link.getKeyword(), true ) + " a " + getPart( link.getKeyword(), false ) );
		            pw.println( ", then that " + getPart( link.getKeyword(), false ) + ":" );
		            pw.print( "\t- " );
	            }
			    
	            // old part sans linkType - because we know its all values
	            pw.print( "is " );
	            
	            // new part
	            printNode( target, false, indent + 1 );
	            
	            // the all-values is already printed, so remove it
	            iter.remove();
	            allPrinted = true;
	            
	            pw.println();
			}
		}
	}
	
	
	/**
	 * Recursive algorithm to print a single NL tree node
	 * @param node - print a subtree of this node
	 * @param basic TODO
	 */
	public void printNode(NLNode node, boolean topLevel, int indent ) {
	    System.out.println( "printing node: " + node.keyword + " " + node.links + " type:" + node.nodeType );
	    
	    // processes and REMOVES all all-values nodes
	    processAllValues( node, indent );
	    
	    if ( node.nodeType == 0 ) {
	        pw.print( "a " );
	    } else if ( node.nodeType == 4 ) {
	        String[] elements = node.getKeyword().split( ";;;" );
	        if ( elements.length == 0 ) {
	            System.out.println( "There is a prolem: Empty Enumeration" );
	        } else if ( elements.length == 1 ) {
	            pw.print( elements[0] + " " );
	        } else if ( elements.length == 2 ) {
	            pw.print( elements[0] + " or " + elements[1] + " ");
	        } else {
	            for ( int i = 0; i < elements.length; i++ ) {
		            pw.print( elements[i] );
		            
		             if ( i < elements.length - 2 ) {
		                 pw.print( ", " );
		             } else if ( i == elements.length - 2 ) {
		                 pw.print( " or " );
		             }
		        }
	            
	            pw.print( " " );
	        }
	        
	        return; // enumeration is always terminal
	    }
	    
	    // NODE KEYWORD PRINTED
	    pw.print(node.getKeyword()+" ");
		
		HashMap links = node.getLinks(); // get hashmap of links for current node
		Set keys = links.keySet(); // save the initial set of links
		
		//***********************************************
		// Rule 2: sort links as follows: 1st is-a, 2nd cardinality, 3rd remaining
		HashMap isaLinks = new HashMap();
		HashMap compLinks = new HashMap();
		HashMap cardLinks = new HashMap();
		HashMap allLinks = new HashMap();
		HashMap remLinks = new HashMap();
		
		NLLink isaLink = new NLLink("", NLVisitor.LINK_SUBCLASS);
		
		HashMap newIsaLinks = new HashMap();
		for (Iterator iter = new HashSet(keys).iterator(); iter.hasNext(); ) {
			// cycle through each link
			NLLink link = (NLLink) iter.next();
			NLNode target = (NLNode) links.get(link);
			
			System.out.println( "LINK: " + link.keyword + " (" + link.linkType + "), TARGET: " + target.keyword );
			
			if ( link.equals( isaLink ) ) {
			    newIsaLinks.put( link, target );
			}
			
			if ( link.equals(isaLink) && target.links.keySet().size() == 0 ) {
			    System.out.println( target.keyword + " " + node.keyword );
			    
				// simply is-a link and not a nested restriction
				isaLinks.put(link, target);
			} else if ( link.getLinkType().equals( NLVisitor.LINK_COMPLEMENT ) ) {
			    compLinks.put( link, target );
			}
			else if (link.getLinkType().equals(NLVisitor.LINK_CARD) || link.getLinkType().equals(NLVisitor.LINK_MAXCARD) || link.getLinkType().equals(NLVisitor.LINK_MINCARD)) {
				// cardinality restrictions
				cardLinks.put(link, target);
			}
			else if ( link.getLinkType().equals( NLVisitor.LINK_ALLVALUES ) ) {
			    // all values restrictions - this also never happens
			    allLinks.put( link, target );
			    
			} else {
				// value restrictions
				remLinks.put(link, target);
			}
		}
		
		List sortedLinkNodes = new ArrayList();
		putLinks(sortedLinkNodes, isaLinks);
		putLinks(sortedLinkNodes, compLinks);
		putLinks(sortedLinkNodes, cardLinks);
		putLinks(sortedLinkNodes, remLinks);
		putLinks(sortedLinkNodes, allLinks );
			
		boolean bigMultiple = sortedLinkNodes.size() > 0;
		boolean bigComplex = false;
		
		if ( bigMultiple ) {
			for (int i=0; i<sortedLinkNodes.size(); i++ ) {
			    NLLink link = ((NLLinkNode) sortedLinkNodes.get(i)).link;
			    // Check for just existential restriction or is_a
			    if ( !link.linkType.equals( NLVisitor.LINK_SOMEVALUES ) && 
			            !link.linkType.equals( NLVisitor.LINK_HASVALUE ) && 
			            !link.linkType.equals( NLVisitor.LINK_SUBCLASS ) ) {
			        bigComplex = true;   
			    	break;
			    }
			}
		}
		
		boolean bigFirstTime = true;
		for (int i=0; i<sortedLinkNodes.size(); i++ ) {
			
		    if ( bigComplex && bigFirstTime ) {
		        pw.print( "that: \n" );
		        pw.print( "\t" );
		        
		        for ( int j = 0; j < indent; j ++) {
		            pw.print( "\t" );
		        }
		        
		        pw.print( "-- " );
		    }
		    
			// cycle through each sorted link
			NLLink link = ((NLLinkNode) sortedLinkNodes.get(i)).link;
			String linkProp = link.getKeyword();
			
			List linkTargets = new ArrayList();
			
			//***********************************************
			// Rule 3: find all targets that match the same link property
			// and put them in a shortened list = linkTargets
			for (int j=0; j<sortedLinkNodes.size(); j++) {
				NLLink chkLink = ((NLLinkNode) sortedLinkNodes.get(j)).link;
				if (keys.contains(chkLink) && chkLink.getKeyword().equals(linkProp)) {
					// add link target to common matrix
					NLNode target = ((NLLinkNode) sortedLinkNodes.get(j)).node;
					linkTargets.add(new NLLinkNode(chkLink, target));
					keys.remove(chkLink); // successively remove to prevent duplicate printing of the exact same link
				}
			}
			
			//***********************************************
			// Rule 4: combine restrictions on the same property (e.g. allValues with a cardinality, either with a someValues etc)
			// and change linktype text for restrictions accordingly			
			List minLNs = filterLinks(linkTargets, NLVisitor.LINK_MINCARD, false);
			List maxLNs = filterLinks(linkTargets, NLVisitor.LINK_MAXCARD, false);
			if (minLNs.size()>0 && maxLNs.size()>0) {
				// assuming there is only one of each (can there be more?)
				NLLinkNode minCardLN = (NLLinkNode) minLNs.iterator().next();
				NLLinkNode maxCardLN = (NLLinkNode) maxLNs.iterator().next();
				// then combine both the min and max cardinalities using 'between'
				minCardLN.link.setLinkType(NLVisitor.LINK_BETWEEN);
				minCardLN.node.setKeyword(minCardLN.node.keyword+" - "+maxCardLN.node.keyword);
				linkTargets.remove(maxCardLN);
			}
			List cardLNs = filterLinks(linkTargets, NLVisitor.LINK_CARD, false);
			List someLNs = filterLinks(linkTargets, NLVisitor.LINK_SOMEVALUES, false);
			List allLNs = filterLinks(linkTargets, NLVisitor.LINK_ALLVALUES, false);
			List hasLNs = filterLinks(linkTargets, NLVisitor.LINK_HASVALUE, false);
			if (cardLNs.size()>0 || minLNs.size()>0 || maxLNs.size()>0) {
				// combine cardinality with someValues
				for (Iterator k=someLNs.iterator(); k.hasNext(); ) {
					NLLinkNode someLN = (NLLinkNode) k.next();
					someLN.link.setLinkType("at least one of which is");
				}
				// combine cardinality with allValues - 
				// TODO now this will never happen?
				for (Iterator k=allLNs.iterator(); k.hasNext(); ) {
					NLLinkNode allLN = (NLLinkNode) k.next();
					allLN.link.setLinkType("each of which is");
				}
				// combine cardinality with oneOf
				for (Iterator k=hasLNs.iterator(); k.hasNext(); ) {
				    //System.out.println( "YOYOYO" );
					NLLinkNode hasLN = (NLLinkNode) k.next();
					//hasLN.link.setLinkType("that has");
					hasLN.link.setLinkType("which is");
				}
			}
			// combine someValues with allValues
			// some x, some y and always x or y
			if (someLNs.size()>0 && allLNs.size()>0) {
			 //TODO	
			}
			
			// START PRINTING
			//***********************************************
			// insert either-or for hasValue for oneOfs
			// code below ONLY FOR ONEOFs
			List oneofs = filterLinks(linkTargets, NLVisitor.LINK_ONEOF, true);
			if (oneofs.size()>0) {
				
				String connector1 = "", connector2 = "";
				if (((NLLinkNode) oneofs.get(0)).link.isComplement) {
					connector1 = " neither ";
					connector2 = " nor ";
				}
				else {
					connector1 = " either ";
					connector2 = " or ";
				}
				
				// assume link.keyword starts with is/has
				String prefix = this.getPart(link.getKeyword(), true);
				pw.print( prefix + " " );
				
				if ( !prefix.equals("has") ) {
				    if ( oneofs.size() > 1 ) {
					    //pw.print(" that"+connector1);
					    pw.print( connector1 );
					}
				    pw.print( this.getPart( link.getKeyword(), false ) + " " );
				} else {
				    if ( oneofs.size() > 1 ) {
					    //pw.print(" that"+connector1);
					    pw.print( connector1 );
					}
				}
				
				Vector v = new Vector( oneofs );
				for ( int k = 0; k < v.size(); k++ ) {
				    NLNode target = ((NLLinkNode) v.get( k )).node;
					printNode( target, false, indent );
					if ( v.size() > 2 ) { 
					    if ( k < v.size() - 2 ) {
					        pw.print( ", " ); 
					    } else if ( k < v.size() - 1 ){
					        pw.print( connector2 );
					    }
					} else {
					    if ( k == 0 ) {
					        pw.print( connector2 );
					    }
					}
				}
				
//				for (Iterator j = oneofs.iterator(); j.hasNext(); ) {
//					NLNode target = ((NLLinkNode) j.next()).node;
//					printNode( target, false, indent );
//					if ( j.hasNext() ) 
//					    pw.print(connector2);					
//				}
				
				if ( prefix.equals("has") ) {
				    pw.print( this.getPart( link.getKeyword(), false ) + " " );
				}
				
				//if (linkTargets.size()>1) pw.print(", ");
				//else if (linkTargets.size()==1) pw.print(" and ");
			}			
			
			// ***********************************************
			// insert either-or for UNIONS 
			List unions = filterLinks(linkTargets, NLVisitor.LINK_UNION, true);
			if (unions.size()>0) {
				
				String connector1 = "", connector2 = "";
				if (((NLLinkNode) unions.get(0)).link.isComplement) {
					connector1 = " neither ";
					connector2 = " nor a ";
				}
				else {
					connector1 = " either ";
					connector2 = " or a ";
				}
				
				if (unions.size()>1) pw.print(" is"+connector1+"a ");
				pw.print(link.getKeyword()+" ");
				for (Iterator j=unions.iterator(); j.hasNext(); ) {
					NLNode target = ((NLLinkNode) j.next()).node;
					printNode( target, false, indent );
					if (j.hasNext()) pw.print(connector2);					
				}
				
				if (linkTargets.size()>1) pw.print(", ");
				else if (linkTargets.size()==1) pw.print(" and ");
			}			
			
			//System.out.println( "Here we are!!!" );
			
			//****************************************************************
			// finally handle remaining link-nodes normally	- EVERYTHING ELSE		
			boolean firstTime = true;						
			boolean complex = false;
			boolean multiple = linkTargets.size() > 0;
		 
			for (int j = 0; j < linkTargets.size(); j++ ) {
			    NLLink flink = ((NLLinkNode) linkTargets.get(j)).link;;
				NLNode ftarget = ((NLLinkNode) linkTargets.get(j)).node;
				
				if ( flink.getLinkType().equals( NLVisitor.LINK_SOMEVALUES ) ) {
				    
				} else {
				    complex = true;
				    System.out.println( "complex!" );
				}
			}
			
			//System.out.println( "THE CRITICAL FACTOR: " + (linkTargets.size() > 2) );
			
			boolean cardPrinted = false;
			for (int j = 0; j < linkTargets.size(); j++ ) {
				NLLink flink = ((NLLinkNode) linkTargets.get(j)).link;;
				NLNode ftarget = ((NLLinkNode) linkTargets.get(j)).node;
				
				System.out.println( "LOOP + " + i +  ": " + flink.keyword + " (" + flink.linkType + "), TARGET: " + ftarget.keyword );
				
				if ( cardPrinted ) {
				    System.out.println( "CARD-IN-AL-ITY" );
				    pw.print( ", " );
				    pw.print( flink.getLinkType() + " " );
				    
				    printNode( ftarget, firstTime, indent );
				    
				} else {
					if (flink.getLinkType().equals(NLVisitor.LINK_CARD) 
							|| flink.getLinkType().equals(NLVisitor.LINK_MINCARD) 
							|| flink.getLinkType().equals(NLVisitor.LINK_MAXCARD)
							|| flink.getLinkType().equals(NLVisitor.LINK_BETWEEN)) {
							// change order for property and value for cardinality restriction
							// re-phrase cardinality clauses , 
							// e.g., with at most 1 is made from grape -> which is made from at most 1 grape										
						reOrderCardClause(flink, ftarget, link.getKeyword(), !firstTime, indent);	
						cardPrinted = true;
					} else {
					    if ( bigFirstTime  ) {
					        pw.print(flink.getLinkType()+" ");	
					    }
					    
					    if ( complex && keys.size() > 0 ) {
						    //pw.print( "\n\t- " );
					    }
						
					    String prefix = this.getPart(link.getKeyword(), true);
					    if ( prefix.equals("has") ) { // TODO replace with function isPrefixEligible()
					        pw.print(prefix + " ");
						} else {
						    pw.print( flink.getKeyword() + " " );
						}
					    
					    // make a check here for boolean properties
						// example  is hard working "false" -> is not hard working
					    if (ftarget.keyword.indexOf("false") == -1 && ftarget.keyword.indexOf("true") == -1 ) {						
							// it is NOT a boolean property
					        // THIS IS WHERE MOST PRINTING TAKES PLACE
					        printNode( ftarget, firstTime, indent );	// TODO NEW CHANGE - is this good?
					        
						} else {
						    // it is a boolean property
						    if  (ftarget.keyword.indexOf("false")!= -1) {
						        pw.print("not ");  //TODO make negation better
						    }
						    
						}
					
						if ( prefix.equals("has") ) { // TODO replace with function isPrefixEligible()
						   pw.print(this.getPart(link.getKeyword(), false)+" ");	
						}
					}
				}
				
				// If complex sentence forming, break 
				// Currently complex is determined as those that involve
				// combination of two restrictions (eg. card + all/some/has)
				// and all these combinations use the keyword "which"
				//TODO Extend and improve determination of complex
				//if ( flink.getLinkType().indexOf("that") != -1 && keys.size()>0 ) {
				if ( complex && keys.size() > 0 ) {
				    //pw.print( "\n\t- " );
				    // TODO fix this later
				} else if ( !cardPrinted ) {
					// else add comma or and depending on number of items left
					if ( j < linkTargets.size() - 2 ) 
					    pw.print(", ");
					else if (j == linkTargets.size() - 2 ) 
					    pw.print(" and ");										
				}	
				
				firstTime = false;
			}
			
			// END INTERNAL LOOP
			
			if ( !bigComplex && keys.size() > 0 ) {
				if ( keys.size() == 1 ) 
				    pw.print(" and ");
				else 
				    pw.print(", ");				
			} else {
			    if ( keys.size() == 0 ) {
			        //pw.print( ". " );
			    } else {
			        pw.print( "\n\t-- " );
			    }
			}
			
			bigFirstTime = false;
		}		
	}
	
	//	consider that only the last word of the keyword is a noun phrase
	private void reOrderCardClause(NLLink flink, NLNode ftarget, String strKeyword, boolean onlyonce, int indent) {
		
		//split the strings into tokens
		String[] tokens = strKeyword.split("( )+");
		String[] tags = tagger.tag(tokens);
		int nounPhraseIndex=-1;
		
		//check where the NP starts, assume it starts with the first noun
		for (int i=0; i<tags.length;i++) {
			if (tags[i].equals("NN") || tags[i].equals("NNS") || 
			    tags[i].equals("NP") || tags[i].equals("NPS")) { // noun phrase
				nounPhraseIndex = i;
			}				
		}		
		String strPrefix= "", strNP = "";
		//case when there is no NP - select the last token as target and the rest as prefix		
		if (nounPhraseIndex==-1) nounPhraseIndex = tokens.length-1; 
		
		for (int i=0; i<nounPhraseIndex;i++) {
			strPrefix += (tokens[i] + " ");
		}
		for (int i=nounPhraseIndex; i < tokens.length; i++) {
			strNP += (tokens[i] + " ");
		}
					
		// change - removed all that's from below code
		if (flink.getLinkType().equals(NLVisitor.LINK_CARD)) { //with exactly
			pw.print(" " + strPrefix + " exactly ");						
		}
		else if (flink.getLinkType().equals(NLVisitor.LINK_MINCARD)) { //with at most 
			pw.print(" " + strPrefix + " at least ");			
		}
		else if ( flink.getLinkType().equals(NLVisitor.LINK_MAXCARD)) { //with at least
			pw.print(" " + strPrefix + " at most ");			
		}
		else if (flink.getLinkType().equals(NLVisitor.LINK_BETWEEN)) { //with between
			pw.print(" " + strPrefix + " between ");			
		}
		
		printNode(ftarget, false, indent); // TODO did this matter?
		if (!onlyonce) pw.print(strNP);				
	}
	
	private List filterLinks(List linkTargets, String filterType, boolean doRemoveLinks) {
		
		List filter = new ArrayList();
		Set remove = new HashSet();
		for (int i=0; i<linkTargets.size(); i++ ) {
			NLLink link = ((NLLinkNode) linkTargets.get(i)).link;
			String linkType = link.getLinkType();
			if (linkType.equals(filterType)) {
				remove.add(linkTargets.get(i));
				filter.add(((NLLinkNode) linkTargets.get(i)));
			}
		}
		if (doRemoveLinks) linkTargets.removeAll(remove);
		return filter;
	}
	
	protected class NLTree {
		// natural language tree to represent entity structure
		private NLNode root;
		
		public NLTree() {
		}
		
		public NLTree(NLNode root) {
			this.root = root;
		}
		
		public void setRoot(NLNode root) {
			this.root = root;
		}
		
		public NLNode getRoot() {
			return root;
		}		
	}
	
	protected class NLNode {
		// structure of a single NL node
		private String keyword;
		private HashMap links;
		public int nodeType = 0;
		
		// 0 is for Class
		// 1 is for Cardinality Target
		// 2 is for Data Literal
		// 3 is for Individual
		// 4 is for dummy node for enumeration - always called **enum**
		
		public NLNode(String word, int type) {
			keyword  = word;
			links = new HashMap();
			nodeType = type;
		}
		
		public String getKeyword() {
			return keyword;
		}
		
		public void setKeyword(String word) {
			this.keyword = word;
		}
		
		public NLNode addLink(NLLink link, NLNode target) {
			
			for (Iterator iter = links.keySet().iterator(); iter.hasNext(); ) {
				NLLink key = (NLLink) iter.next();
				if (key.equals(link)) {
					NLNode value = (NLNode) links.get(key);
					if (value.equals(target)) {						
						return value;
					}
				}				
			}
			
			NLNode newTarget = new NLNode(target.getKeyword(), target.nodeType);
			links.put(new NLLink(link.getKeyword(), link.getLinkType(), link.isComplement), newTarget);
			// also remove complement on link if present
			link.setIsComplement(false);
			return newTarget;
		}
		
		public HashMap getLinks() {
			return links;
		}
		
		public boolean equals(NLNode node) {
			if (node.getKeyword().equals(this.keyword)) return true;
			else return false;
		}
	}
	
	protected class NLLink {
		// different types of links between nodes
		private String linkType;
		private String keyword;
		private boolean isComplement;
		
		public NLLink(String keyword, String type) {
			this.keyword = keyword;
			this.linkType = type;
			this.isComplement = false;
		}
		
		public NLLink(String keyword, String type, boolean isComplement) {
			this.keyword = keyword;
			this.linkType = type;
			this.isComplement = isComplement;
		}
		
		public boolean isComplement() {
			return this.isComplement;
		}
		
		public void setIsComplement(boolean mode) {
			this.isComplement = mode;
		}
		
		public String getLinkType() {
			return linkType;
		}
		
		public void setLinkType(String linkType) {
			this.linkType = linkType;
		}
		
		public void setKeyword(String keyword) {
			this.keyword = keyword;
		}
		
		public String getKeyword() {
			return keyword;
		}
		
		public boolean equals(NLLink link) {
						
			if ((link.getKeyword().equals(this.keyword)) && 
				link.getLinkType().equals(this.getLinkType()) && 
				link.isComplement()==this.isComplement()) 
				return true;
			else return false;
		}
		
		public void printLink() {
		    System.out.println( "LINK: (" + this.linkType + ") " + this.keyword );
		}
	}

	protected class NLLinkNode {
		NLLink link;
		NLNode node;
		
		public NLLinkNode(NLLink link, NLNode node) {
			this.link = link;
			this.node = node;
		}
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLFunctionalPropertyAxiom)
	 */
	public void visit(OWLFunctionalPropertyAxiom node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLInverseFunctionalPropertyAxiom)
	 */
	public void visit(OWLInverseFunctionalPropertyAxiom node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLTransitivePropertyAxiom)
	 */
	public void visit(OWLTransitivePropertyAxiom node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLSymmetricPropertyAxiom)
	 */
	public void visit(OWLSymmetricPropertyAxiom node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLInversePropertyAxiom)
	 */
	public void visit(OWLInversePropertyAxiom node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLPropertyDomainAxiom)
	 */
	public void visit(OWLPropertyDomainAxiom node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLObjectPropertyRangeAxiom)
	 */
	public void visit(OWLObjectPropertyRangeAxiom node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLDataPropertyRangeAxiom)
	 */
	public void visit(OWLDataPropertyRangeAxiom node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLObjectPropertyInstance)
	 */
	public void visit(OWLObjectPropertyInstance node) throws OWLException {
	    //prefixDomain(node.getProperty());
		linkContext.setLinkType(LINK_SOMEVALUES);
		node.getProperty().accept( this );
		node.getObject().accept( this );	
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLDataPropertyInstance)
	 */
	public void visit(OWLDataPropertyInstance node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLIndividualTypeAssertion)
	 */
	public void visit(OWLIndividualTypeAssertion node) throws OWLException {
		// TODO Auto-generated method stub
		
	}
}


