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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;
import org.mindswap.swoop.Swoop;
import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.renderer.SwoopRenderingVisitor;
import org.mindswap.swoop.utils.ui.SwoopIcons;
import org.semanticweb.owl.impl.model.OWLInversePropertyAxiomImpl;
import org.semanticweb.owl.io.ShortFormProvider;
import org.semanticweb.owl.model.OWLAnd;
import org.semanticweb.owl.model.OWLAnnotationProperty;
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

/**
 * @author Evren Sirin
 */
public class ConciseFormatVisitor extends OWLObjectVisitorAdapter implements SwoopRenderingVisitor
//	Uncomment for explanation
// ,OWLExtendedObjectVisitor
{
    
	ShortFormProvider shortForms; 
	StringWriter sw;
	PrintWriter pw;
	SwoopModel swoopModel;
	public Map highlightMap;
	
	public ConciseFormatVisitor( ShortFormProvider shortForms, SwoopModel swoopModel )
	{
		this.shortForms = shortForms;
		this.swoopModel = swoopModel;
		this.highlightMap = new HashMap();
		reset();
	}
	
	/**
	 * HTML-escape any object
	 * @param object
	 * @return
	 */
	private static String escape(Object object) {
		String escaped = "null";
		if (object != null) {
			escaped = StringEscapeUtils.escapeHtml(object.toString());
		}
		return escaped;
	}
	
	public String result() {
		return sw.toString();
	}

	public void reset() {
		sw = new StringWriter();
		pw = new PrintWriter( sw );
	}
	
	
	public void visit( OWLClass clazz ) throws OWLException {
		String icon = "";
		
		if (swoopModel.getSelectedOntology()!=null && swoopModel.getSelectedOntology().isForeign(clazz)) 
			icon = "<img src=\""+SwoopIcons.getImageURL("ForeignClass.gif")+"\">";
		else
			icon = "<img src=\""+SwoopIcons.getImageURL("Class.gif")+"\">";
		
		if (swoopModel.getEnableDebugging()) {
			boolean isConsistent = swoopModel.getReasoner().isConsistent(clazz);
			System.out.println("isConsistent: "+isConsistent);
			if (!isConsistent) icon = "<img src=\""+SwoopIcons.getImageURL("InconsistentClass.gif")+"\">";							
		}
		
		if (swoopModel.getShowIcons() || (swoopModel.getEnableDebugging() && icon.indexOf("Inconsistent")>=0)) pw.print(icon);
		String highlight = "";
		if (this.highlightMap.containsKey(clazz.getURI())) highlight = this.highlightMap.get(clazz.getURI()).toString();
		pw.print(highlight);
		if (swoopModel.repairColor && swoopModel.repairSet.contains(clazz)) pw.print("<font color=\"red\">");
		pw.print( "<a href=\"" + escape(clazz.getURI()) + "\">" + escape(shortForms.shortForm( clazz.getURI() )) + "</a>" );		
		if (highlight.indexOf("font")>=0 || (swoopModel.repairColor && swoopModel.repairSet.contains(clazz))) pw.print("</font>");
		if (highlight.indexOf("strike")>=0) pw.print("</strike>");
	}
	
	public void visit( OWLIndividual ind ) throws OWLException {
	//********************************************************* 
	//Changed for Econnections
	//*********************************************************	
	 if(swoopModel.getSelectedOntology()!=null && swoopModel.getSelectedOntology().isForeign(ind)){	
	 	if (swoopModel.getShowIcons()) pw.print("<img src=\""+SwoopIcons.getImageURL("ForeignIndividual.gif")+"\">");		 	
	 else{
	 	if (swoopModel.getShowIcons()) pw.print("<img src=\""+SwoopIcons.getImageURL("Instance.gif")+"\">");}
	 }
		if ( ind.isAnonymous() ) {
			pw.print( "<a href=\"" + StringEscapeUtils.escapeHtml(ind.getAnonId().toString()) + "\">" + ind.getAnonId().getFragment() + "</a>" );
		} else {
			String highlight = "";
			if (this.highlightMap.containsKey(ind.getURI())) highlight = this.highlightMap.get(ind.getURI()).toString(); 
			pw.print(highlight); 
			pw.print( "<a href=\"" + escape(ind.getURI()) + "\">" + escape(shortForms.shortForm( ind.getURI() )) + "</a>" );
			if (highlight.indexOf("font")>=0) pw.print("</font>");
			if (highlight.indexOf("strike")>=0) pw.print("</strike>");			
		}
	}
	
	
	public void visit( OWLObjectProperty prop ) throws OWLException {
//		************************ 
		//Changed for Econnections
		//************************	
	if(!swoopModel.getSelectedOntology().isForeign(prop)){
		if(!prop.isLink()){	
			if (swoopModel.getShowIcons()) pw.print("<img src=\""+SwoopIcons.getImageURL("Property.gif")+"\">");}
			else{
				if (swoopModel.getShowIcons()) pw.print("<img src=\""+SwoopIcons.getImageURL("LinkProperty.gif")+"\">");
				 
			}
	}
	else{
		if (swoopModel.getShowIcons()) pw.print("<img src=\""+SwoopIcons.getImageURL("ForeignProperty.gif")+"\">");
	   
	}
		String highlight = "";
		if (this.highlightMap.containsKey(prop.getURI())) highlight = this.highlightMap.get(prop.getURI()).toString(); 
		pw.print(highlight);
		pw.print( "<a href=\"" + escape(prop.getURI()) + "\">" + escape(shortForms.shortForm( prop.getURI() )) + "</a>" );
		if (highlight.indexOf("font")>=0) pw.print("</font>");
		if (highlight.indexOf("strike")>=0) pw.print("</strike>");
	}
	
	public void visit( OWLAnnotationProperty prop ) throws OWLException {
		if(!swoopModel.getSelectedOntology().isForeign(prop)){
			
			if (swoopModel.getShowIcons()) pw.print("<img src=\""+SwoopIcons.getImageURL("DataProperty.gif")+"\">");}
		else{
			if (swoopModel.getShowIcons()) pw.print("<img src=\""+SwoopIcons.getImageURL("ForeignProperty.gif")+"\">");
		}
		
			pw.print( "<a href=\"" + escape(prop.getURI()) + "\">" + escape(shortForms.shortForm( prop.getURI() )) + "</a>" );
		}
	
	public void visit( OWLDataProperty prop ) throws OWLException {
		if(!swoopModel.getSelectedOntology().isForeign(prop)){
			if (swoopModel.getShowIcons()) pw.print("<img src=\""+SwoopIcons.getImageURL("DataProperty.gif")+"\">");
		}
		else{
			if (swoopModel.getShowIcons()) pw.print("<img src=\""+SwoopIcons.getImageURL("ForeignProperty.gif")+"\">");
		}
		String highlight = "";
		if (this.highlightMap.containsKey(prop.getURI())) highlight = this.highlightMap.get(prop.getURI()).toString(); 
		pw.print(highlight);
		pw.print( "<a href=\"" + escape(prop.getURI()) + "\">" + escape(shortForms.shortForm( prop.getURI() )) + "</a>" );
		if (highlight.indexOf("font")>=0) pw.print("</font>");
		if (highlight.indexOf("strike")>=0) pw.print("</strike>");
	}
	
	public void visit( OWLDataValue cd ) throws OWLException {
		
		pw.print( "\"" + escape( cd.getValue() ) + "\"");

		/* Only show it if it's not string */

		URI dvdt = cd.getURI();
		String dvlang = cd.getLang();
		if ( dvdt!=null) {
			pw.print( "^^" + "&lt;" + escape(shortForms.shortForm(dvdt)) + "&gt;");
		} else {
			if (dvlang!=null) {
				pw.print( "@" + escape(dvlang) );
			}
		}
	}

	public void visit( OWLAnd and ) throws OWLException {
		pw.print("(");
		for ( Iterator it = and.getOperands().iterator();
		it.hasNext(); ) {
			OWLDescription desc = (OWLDescription) it.next();
			desc.accept( this );
			if (it.hasNext()) {
				pw.print(" " + ConciseFormat.INTERSECTION + " ");
			}
		}
		pw.print(")");
	}

	public void visit( OWLOr or ) throws OWLException {
		pw.print("(");
		for ( Iterator it = or.getOperands().iterator();
		it.hasNext(); ) {
			OWLDescription desc = (OWLDescription) it.next();
			desc.accept( this );
			if (it.hasNext()) {
				pw.print(" " + ConciseFormat.UNION + " ");
			}
		}
		pw.print(")");
	}

	public void visit( OWLNot not ) throws OWLException {
		pw.print("(" + ConciseFormat.COMPLEMENT);
		OWLDescription desc = not.getOperand();
		desc.accept( this );
		pw.print(")");
	}

	public void visit( OWLEnumeration enumeration ) throws OWLException {
		pw.print("{");
		for ( Iterator it = enumeration.getIndividuals().iterator();
		it.hasNext(); ) {
			OWLIndividual desc = (OWLIndividual) it.next();
			desc.accept( this );
			if (it.hasNext()) {
				pw.print(", ");
			}
		}
		pw.print("}");
	}

	public void visit( OWLObjectSomeRestriction restriction ) throws OWLException {
		pw.print("(" + ConciseFormat.EXISTS);
		
		OWLObjectProperty prop = restriction.getObjectProperty();
		
		// if value of someValues restriction is not struck out
		// dont strike out property either
		OWLDescription desc = restriction.getDescription(); 
		if (desc instanceof OWLClass) {
			OWLClass cla = (OWLClass) desc;
			if (this.highlightMap.containsKey(cla.getURI()) && this.highlightMap.get(cla.getURI()).toString().indexOf("<strike>")>=0) {
				// do nothing
			}
			else {
				this.highlightMap.remove(prop.getURI());
			}
		}
		
		prop.accept( this );
		pw.print(" "+ ConciseFormat.MEMBEROF + " ");

		// if property is struck out, strike out value as well
		boolean strike = false;
		if (this.highlightMap.containsKey(prop.getURI())) {
			String highlight = this.highlightMap.get(prop.getURI()).toString(); 
			if (highlight.indexOf("<strike>")>=0) {
				strike = true;
				pw.print(highlight);				
			}
		}
		
		desc.accept( this );
		
		if (strike) {
			pw.print("</font></strike>");
		}
		
		pw.print(")");
	}

	public void visit( OWLObjectAllRestriction restriction ) throws OWLException {
		pw.print("(" + ConciseFormat.FORALL);
		
		// hack for highlighting
		// if value of object prop restriction is struck, strike out property as well
		OWLDescription desc = restriction.getDescription();
		boolean strike = false;
		if (desc instanceof OWLClass) {
			OWLClass cla = (OWLClass) desc;
			if (this.highlightMap.containsKey(cla.getURI())) {
				String highlight = this.highlightMap.get(cla.getURI()).toString(); 
				if (highlight.indexOf("<strike>")>=0) {
					strike = true;
					pw.print(highlight);
				}
			}
		}
		restriction.getObjectProperty().accept( this );
		
		if (strike) {
			pw.print("</font></strike>");
		}
		
		pw.print(" "+ ConciseFormat.MEMBEROF + " ");
		
		// if property is struck out, strike out value as well
		strike = false;
		OWLObjectProperty prop = restriction.getObjectProperty();
		if (this.highlightMap.containsKey(prop.getURI())) {
			String highlight = this.highlightMap.get(prop.getURI()).toString(); 
			if (highlight.indexOf("<strike>")>=0) {
				strike = true;
				pw.print(highlight);				
			}
		}
		restriction.getDescription().accept( this );
		
		if (strike) {
			pw.print("</font></strike>");
		}
		
		pw.print(")");
	}

	public void visit( OWLObjectValueRestriction restriction ) throws OWLException {
		pw.print("(" + ConciseFormat.EXISTS);
		restriction.getObjectProperty().accept( this );
		/* Changed from hasValue */
		pw.print(" "+ ConciseFormat.MEMBEROF + " {");
		
		// if property is struck out, strike out value as well
		boolean strike = false;
		OWLObjectProperty prop = restriction.getObjectProperty();
		if (this.highlightMap.containsKey(prop.getURI())) {
			String highlight = this.highlightMap.get(prop.getURI()).toString(); 
			if (highlight.indexOf("<strike>")>=0) {
				strike = true;
				pw.print(highlight);				
			}
		}
		
		restriction.getIndividual().accept( this );
		
		if (strike) {
			pw.print("</font></strike>");	
		}
		
		pw.print("})");
	}

	public void visit( OWLDataSomeRestriction restriction ) throws OWLException {
		pw.print("(" + ConciseFormat.EXISTS);
		restriction.getDataProperty().accept( this );
		pw.print(" "+ ConciseFormat.MEMBEROF + " ");
		
		// if property is struck out, strike out value as well
		boolean strike = false;
		OWLDataProperty prop = restriction.getDataProperty();
		if (this.highlightMap.containsKey(prop.getURI())) {
			String highlight = this.highlightMap.get(prop.getURI()).toString(); 
			if (highlight.indexOf("<strike>")>=0) {
				strike = true;
				pw.print(highlight);				
			}
		}
		
		restriction.getDataType().accept( this );
		
		if (strike) {
			pw.print("</font></strike>");
		}
		
		pw.print(")");
	}

	public void visit( OWLDataAllRestriction restriction ) throws OWLException {
		pw.print("(" + ConciseFormat.FORALL);
		restriction.getDataProperty().accept( this );
		pw.print(" "+ ConciseFormat.MEMBEROF + " ");
		
		// if property is struck out, strike out value as well
		boolean strike = false;
		OWLDataProperty prop = restriction.getDataProperty();
		if (this.highlightMap.containsKey(prop.getURI())) {
			String highlight = this.highlightMap.get(prop.getURI()).toString(); 
			if (highlight.indexOf("<strike>")>=0) {
				strike = true;
				pw.print(highlight);				
			}
		}
		
		restriction.getDataType().accept( this );
		
		if (strike) {
			pw.print("</font></strike>");
		}
		
		pw.print(")");
	}

	public void visit( OWLObjectCardinalityRestriction restriction ) throws OWLException {
		pw.print("(");
		
		boolean strike = false;
		OWLObjectProperty prop = restriction.getObjectProperty();
		if (this.highlightMap.containsKey(prop.getURI())) {
			String highlight = this.highlightMap.get(prop.getURI()).toString(); 
			if (highlight.indexOf("<strike>")>=0) {
				strike = true;
				pw.print(highlight);				
			}
		}
		
		if ( restriction.isExactly() ) {
			pw.print(ConciseFormat.EQU + " " + restriction.getAtLeast() + " ");
		} else if ( restriction.isAtMost() ) {
			pw.print(ConciseFormat.LESSEQU + " " + restriction.getAtMost() + " ");
		} else 	if ( restriction.isAtLeast() ) {
			pw.print(ConciseFormat.GREATEQU + " " + restriction.getAtLeast() + " ");
		} 
		prop.accept( this );
		
		if (strike) {
			pw.print("</font></strike>");
		}
		
		pw.print(")");
	}

	public void visit( OWLDataCardinalityRestriction restriction ) throws OWLException {
		
		pw.print("(");
		
		boolean strike = false;
		OWLDataProperty prop = restriction.getDataProperty();
		if (this.highlightMap.containsKey(prop.getURI())) {
			String highlight = this.highlightMap.get(prop.getURI()).toString(); 
			if (highlight.indexOf("<strike>")>=0) {
				strike = true;
				pw.print(highlight);				
			}
		}
		
		if ( restriction.isExactly() ) {
			pw.print(ConciseFormat.EQU + " " + restriction.getAtLeast() + " ");
		} else if ( restriction.isAtMost() ) {
			pw.print(ConciseFormat.LESSEQU + " " + restriction.getAtMost() + " ");
		} else 	if ( restriction.isAtLeast() ) {
			pw.print(ConciseFormat.GREATEQU + " " + restriction.getAtLeast() + " ");
		} 
		prop.accept( this );
		
		if (strike) {
			pw.print("</font></strike>");
		}
		pw.print(")");		
	}

	public void visit( OWLDataValueRestriction restriction ) throws OWLException {
		pw.print("(" + ConciseFormat.EXISTS);
		restriction.getDataProperty().accept( this );
		/* Changed from hasValue */
		pw.print(" "+ ConciseFormat.MEMBEROF + " {");
		
		// if property is struck out, strike out value as well
		boolean strike = false;
		OWLDataProperty prop = restriction.getDataProperty();
		if (this.highlightMap.containsKey(prop.getURI())) {
			String highlight = this.highlightMap.get(prop.getURI()).toString(); 
			if (highlight.indexOf("<strike>")>=0) {
				strike = true;
				pw.print(highlight);				
			}
		}
		
		restriction.getValue().accept( this );
		
		if (strike) {
			pw.print("</font></strike>");
		}
		
		pw.print("})");
	}

	public void visit( OWLEquivalentClassesAxiom axiom ) throws OWLException {

		// sort axiom classes so that atomic appears on the LHS
		Set equClas = axiom.getEquivalentClasses();
		Set atomic = new HashSet();
		Set complex = new HashSet();
		for ( Iterator it = equClas.iterator();
		it.hasNext(); ) {
			OWLDescription desc = (OWLDescription) it.next();
			if (desc instanceof OWLClass) atomic.add(desc);			
			else complex.add(desc);
		}
		
		pw.print("(");
		for ( Iterator it = atomic.iterator();
		it.hasNext(); ) {
			OWLDescription desc = (OWLDescription) it.next();
			desc.accept( this );
			if (it.hasNext() || complex.size()>0) {
				pw.print("&nbsp;" + ConciseFormat.EQUIVALENT + "&nbsp;");
			}
		}
		for ( Iterator it = complex.iterator();
		it.hasNext(); ) {
			OWLDescription desc = (OWLDescription) it.next();
			desc.accept( this );
			if (it.hasNext()) {
				pw.print("&nbsp;" + ConciseFormat.EQUIVALENT + "&nbsp;");
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
				pw.print(" " + ConciseFormat.SUBSET + " " + ConciseFormat.COMPLEMENT);
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
		pw.print( "<a href=\"" + escape(ocdt.getURI()) + "\">" + escape(shortForms.shortForm( ocdt.getURI() )) + "</a>" );		
		//pw.print( shortForms.shortForm( ocdt.getURI() ) );
	}

	public void visit( OWLDataEnumeration enumeration ) throws OWLException {
		pw.print("{");
		for ( Iterator it = enumeration.getValues().iterator();
		it.hasNext(); ) {
			OWLDataValue desc = (OWLDataValue) it.next();
			desc.accept( this );
			if (it.hasNext()) {
				pw.print(", ");
			}
		}
		pw.print("}");
	}
	

//	Uncomment for explanation
	
	public void visit( OWLFunctionalPropertyAxiom axiom ) throws OWLException {
		pw.print("Functional Property (");
		axiom.getProperty().accept( this );
		pw.print(")");
	}
	
	public void visit( OWLPropertyDomainAxiom axiom ) throws OWLException {
		pw.print("(");
		axiom.getProperty().accept( this );
		pw.print(" domain ");
		axiom.getDomain().accept( this );
		pw.print(")");
	}
	
	public void visit( OWLObjectPropertyRangeAxiom axiom ) throws OWLException {
		pw.print("(");
		axiom.getProperty().accept( this );
		pw.print(" range ");
		axiom.getRange().accept( this );
		pw.print(")");
	}
	
	public void visit( OWLDataPropertyRangeAxiom axiom ) throws OWLException {
		pw.print("(");
		axiom.getProperty().accept( this );
		pw.print(" range ");
		axiom.getRange().accept( this );
		pw.print(")");
	}
	
    /* (non-Javadoc)
     * @see org.mindswap.pellet.owlapi.OWLExtendedObjectVisitor#visit(org.mindswap.pellet.owlapi.OWLInverseFunctionalPropertyAxiom)
     *FIXME: Fix this!
     */
    public void visit(OWLInverseFunctionalPropertyAxiom axiom) throws OWLException {
    	if (axiom.getProperty().isInverseFunctional(axiom.getProperty().getOntologies())) {
	    	pw.print("InverseFunctional(");
			axiom.getProperty().accept( this );
			pw.print(")");
    	}
    	if (axiom instanceof OWLInversePropertyAxiomImpl) {
    		OWLInversePropertyAxiomImpl invAxiom = (OWLInversePropertyAxiomImpl) axiom;
    		pw.print("(");
    		invAxiom.getProperty().accept( this );
    		pw.print(" inverse ");
    		invAxiom.getInverseProperty().accept( this );
    		pw.print(")");	
    	}
    }

    /* (non-Javadoc)
     * @see org.mindswap.pellet.owlapi.OWLExtendedObjectVisitor#visit(org.mindswap.pellet.owlapi.OWLTransitivePropertyAxiom)
     */
    public void visit(OWLTransitivePropertyAxiom axiom) throws OWLException {
		pw.print("Transitive(");
		axiom.getProperty().accept( this );
		pw.print(")");    }

    /* (non-Javadoc)
     * @see org.mindswap.pellet.owlapi.OWLExtendedObjectVisitor#visit(org.mindswap.pellet.owlapi.OWLSymmetricPropertyAxiom)
     */
    public void visit(OWLSymmetricPropertyAxiom axiom) throws OWLException {
		pw.print("Symmetric(");
		axiom.getProperty().accept( this );
		pw.print(")");    }

    /* (non-Javadoc)
     * @see org.mindswap.pellet.owlapi.OWLExtendedObjectVisitor#visit(org.mindswap.pellet.owlapi.OWLInversePropertyAxiom)
     */
    public void visit(OWLInversePropertyAxiom axiom) throws OWLException {
		pw.print("(");
		axiom.getProperty().accept( this );
		pw.print(" inverse ");
		axiom.getInverseProperty().accept( this );
		pw.print(")");
    }

    /* (non-Javadoc)
     * @see org.mindswap.pellet.owlapi.OWLExtendedObjectVisitor#visit(org.mindswap.pellet.owlapi.OWLObjectPropertyInstance)
     */
    public void visit(OWLObjectPropertyInstance axiom) throws OWLException {
		pw.print("(");
		axiom.getSubject().accept( this );
		pw.print(" ");
		axiom.getProperty().accept( this );
		pw.print(" ");
		axiom.getObject().accept( this );
		pw.print(")");
    }

    /* (non-Javadoc)
     * @see org.mindswap.pellet.owlapi.OWLExtendedObjectVisitor#visit(org.mindswap.pellet.owlapi.OWLDataPropertyInstance)
     */
    public void visit(OWLDataPropertyInstance axiom) throws OWLException {
		pw.print("(");
		axiom.getSubject().accept( this );
		pw.print(" ");
		axiom.getProperty().accept( this );
		pw.print(" ");
		axiom.getObject().accept( this );
		pw.print(")");
    }

    /* (non-Javadoc)
     * @see org.mindswap.pellet.owlapi.OWLExtendedObjectVisitor#visit(org.mindswap.pellet.owlapi.OWLIndividualClassAxiom)
     */
    public void visit(OWLIndividualTypeAssertion axiom) throws OWLException {
		pw.print("(");
		axiom.getIndividual().accept( this );
		pw.print(" rdf:type ");
		axiom.getType().accept( this );
		pw.print(")");
    }
    
}
