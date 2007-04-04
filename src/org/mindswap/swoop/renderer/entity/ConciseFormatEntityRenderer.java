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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import org.apache.commons.lang.StringEscapeUtils;
import org.mindswap.pellet.debug.owlapi.Reasoner;
import org.mindswap.pellet.debug.utils.Timer;
import org.mindswap.swoop.Swoop;
import org.mindswap.swoop.SwoopDisplayPanel;
import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.TermsDisplay;
import org.mindswap.swoop.debugging.RepairFrame;
import org.mindswap.swoop.popup.PopupAddAnnotation;
import org.mindswap.swoop.popup.PopupAddClass;
import org.mindswap.swoop.popup.PopupAddIndividual;
import org.mindswap.swoop.popup.PopupAddProperty;
import org.mindswap.swoop.popup.PopupAddRule;
import org.mindswap.swoop.popup.PopupAddValue;
import org.mindswap.swoop.reasoner.PelletReasoner;
import org.mindswap.swoop.reasoner.SwoopReasoner;
import org.mindswap.swoop.renderer.BaseEntityRenderer;
import org.mindswap.swoop.renderer.SwoopEditableRenderer;
import org.mindswap.swoop.renderer.SwoopEntityRenderer;
import org.mindswap.swoop.renderer.SwoopRenderingVisitor;
import org.mindswap.swoop.utils.RuleValue;
import org.mindswap.swoop.utils.RulesExpressivity;
import org.mindswap.swoop.utils.SetUtils;
import org.mindswap.swoop.utils.XPointers;
import org.mindswap.swoop.utils.change.BooleanElementChange;
import org.mindswap.swoop.utils.change.EnumElementChange;
import org.mindswap.swoop.utils.external.ExternalRuleSubmitter;
import org.mindswap.swoop.utils.owlapi.CorrectedRDFRenderer;
import org.mindswap.swoop.utils.owlapi.OWLDescriptionFinder;
import org.mindswap.swoop.utils.owlapi.OWLOntBuilder;
import org.mindswap.swoop.utils.ui.SwoopIcons;
import org.semanticweb.owl.io.RendererException;
import org.semanticweb.owl.io.vocabulary.OWLVocabularyAdapter;
import org.semanticweb.owl.model.OWLAnd;
import org.semanticweb.owl.model.OWLAnnotationInstance;
import org.semanticweb.owl.model.OWLAnnotationProperty;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLClassAxiom;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataRange;
import org.semanticweb.owl.model.OWLDataType;
import org.semanticweb.owl.model.OWLDataValue;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owl.model.OWLDisjointClassesAxiom;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLEnumeration;
import org.semanticweb.owl.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owl.model.OWLEquivalentPropertiesAxiom;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLIndividualAxiom;
import org.semanticweb.owl.model.OWLIndividualTypeAssertion;
import org.semanticweb.owl.model.OWLNamedObject;
import org.semanticweb.owl.model.OWLNot;
import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectPropertyInstance;
import org.semanticweb.owl.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOr;
import org.semanticweb.owl.model.OWLProperty;
import org.semanticweb.owl.model.OWLPropertyDomainAxiom;
import org.semanticweb.owl.model.OWLRestriction;
import org.semanticweb.owl.model.OWLSameIndividualsAxiom;
import org.semanticweb.owl.model.OWLSubClassAxiom;
import org.semanticweb.owl.model.OWLSubPropertyAxiom;
import org.semanticweb.owl.model.change.AddAnnotationInstance;
import org.semanticweb.owl.model.change.AddClassAxiom;
import org.semanticweb.owl.model.change.AddDataPropertyInstance;
import org.semanticweb.owl.model.change.AddDataPropertyRange;
import org.semanticweb.owl.model.change.AddDomain;
import org.semanticweb.owl.model.change.AddEntity;
import org.semanticweb.owl.model.change.AddEnumeration;
import org.semanticweb.owl.model.change.AddEquivalentClass;
import org.semanticweb.owl.model.change.AddIndividualAxiom;
import org.semanticweb.owl.model.change.AddIndividualClass;
import org.semanticweb.owl.model.change.AddInverse;
import org.semanticweb.owl.model.change.AddObjectPropertyInstance;
import org.semanticweb.owl.model.change.AddObjectPropertyRange;
import org.semanticweb.owl.model.change.AddPropertyAxiom;
import org.semanticweb.owl.model.change.AddSuperClass;
import org.semanticweb.owl.model.change.AddSuperProperty;
import org.semanticweb.owl.model.change.ChangeVisitor;
import org.semanticweb.owl.model.change.OntologyChange;
import org.semanticweb.owl.model.change.RemoveAnnotationInstance;
import org.semanticweb.owl.model.change.RemoveClassAxiom;
import org.semanticweb.owl.model.change.RemoveDataPropertyInstance;
import org.semanticweb.owl.model.change.RemoveDataPropertyRange;
import org.semanticweb.owl.model.change.RemoveDomain;
import org.semanticweb.owl.model.change.RemoveEntity;
import org.semanticweb.owl.model.change.RemoveEnumeration;
import org.semanticweb.owl.model.change.RemoveEquivalentClass;
import org.semanticweb.owl.model.change.RemoveIndividualAxiom;
import org.semanticweb.owl.model.change.RemoveIndividualClass;
import org.semanticweb.owl.model.change.RemoveInverse;
import org.semanticweb.owl.model.change.RemoveObjectPropertyInstance;
import org.semanticweb.owl.model.change.RemoveObjectPropertyRange;
import org.semanticweb.owl.model.change.RemovePropertyAxiom;
import org.semanticweb.owl.model.change.RemoveSuperClass;
import org.semanticweb.owl.model.change.RemoveSuperProperty;
import org.semanticweb.owl.model.change.SetFunctional;
import org.semanticweb.owl.model.change.SetInverseFunctional;
import org.semanticweb.owl.model.change.SetSymmetric;
import org.semanticweb.owl.model.change.SetTransitive;
import org.semanticweb.owl.model.helper.OntologyHelper;
import org.semanticweb.owl.rules.OWLRule;
import org.semanticweb.owl.rules.OWLRuleAtom;
import org.semanticweb.owl.rules.OWLRuleClassAtom;
import org.semanticweb.owl.rules.OWLRuleDataPropertyAtom;
import org.semanticweb.owl.rules.OWLRuleObjectPropertyAtom;


/**
 * @author Aditya Kalyanpur, Evren Sirin
 */
public class ConciseFormatEntityRenderer extends BaseEntityRenderer implements SwoopEditableRenderer, SwoopEntityRenderer {
	
	List changes = null;
	public Map OWLDescHash;
	OWLEntity displayedEntity;
	String[] depictions = {"http://www.mindswap.org/~glapizco/technical.owl#depiction", "http://xmlns.com/foaf/0.1/depiction"}; 
	String HR = "</tr><tr></tr><tr bgcolor=\"#FFF68F\">";
	boolean imported = false;
	Map sortLevelMap;
	// params used for black box sos derivation
	boolean sosBlackBox = false;
	int axiomLimit = 40;
	// two maps for entities
	HashMap usageMap = new HashMap();
	HashMap axiomMap = new HashMap();
	// one map for axioms
	HashMap signatureMap = new HashMap();
	
	private long parseTime = 0;
	private long netTime = 0;
	private int mupsCount = 0;
	
	public ConciseFormatEntityRenderer() {
	}
	
	public String getContentType() {
		return "text/html";
	}
	
	public String getName() {
		return "Concise Format";
	}
	
	public SwoopRenderingVisitor createVisitor() {
		return new ConciseFormatVisitor(this, swoopModel);
	}

	protected void renderEntity() throws OWLException {
		print("<html><body style='color: black; background-color: white;'>");
		print("<FONT FACE=\""+swoopModel.getFontFace()+"\" SIZE="+fontSize+">");
		super.renderEntity();
		print("</FONT>");
		print("</body></html>");
	}
	
	// silly hack to remove artifact in display when show_divisions is ON
	private String postProcess(String rendStr) {
		if (swoopModel.getShowDivisions()) {
			String remove = "<tr bgcolor=\"#FFF68F\">";
			if (rendStr.lastIndexOf(remove)>rendStr.lastIndexOf("<b>")) {
				rendStr = rendStr.substring(0, rendStr.lastIndexOf(remove));
			}									
		}
		return rendStr;
	}

	/* Well dodgy coding */
	protected void renderAnnotationContent(Object o) throws OWLException {
		if (o instanceof URI) {
			print(escape(o));
		} else if (o instanceof OWLIndividual) {
			print(escape(((OWLIndividual) o).getURI()));
		} else {
			String content = o.toString();
			if (o instanceof OWLDataValue) {
			
				OWLDataValue dv = (OWLDataValue) o;
				/* Only show it if it's not string */
				URI dvdt = dv.getURI();
				String dvlang = dv.getLang();
				if (dvdt != null) {
					print("(Datatype " + escape(dvdt) + ") ");
				} else {
					if (dvlang != null && !dvlang.equals("")) {
						print("(" + escape(dvlang) + ") ");
					}
				}
				content = dv.getValue().toString();
			}
//			print(escape(dv.getValue()));
//		} else {
			// This is such a hack - Ron
			
//			if (content.indexOf("html")>=0 && content.indexOf("body")>=0) {
//				content = content.replaceAll("&lt;", "<");
//				content = content.replaceAll("&gt;", ">");
//				content = content.replaceAll("&amp;", "&");
//			
//				// remove <html><body> outer tags because it's being rendered inside a new html doc anyway
//			
//				int spos = content.indexOf("<body>")+6;
//				int epos = content.indexOf("</body>");
//				content = content.substring(spos, epos);
//				if (content.trim().indexOf("<p")>=0) {
//					content = content.substring(content.indexOf(">")+1, content.length());
//				}
//				content += " (**Stripped HTML tags**)";
//			}
//			// System.out.println(content);
			print(" : "+escape(content));
		}
	}
	
	/**
	 * Return an ontology change which deletes OWL Annotation Instances
	 * @param subj - subject of the annotation
	 * @param prop - OWL Annotation property used
	 * @param content - content of the annotation
	 * @return
	 */
	private OntologyChange isDeleted(OWLObject subj, OWLAnnotationProperty prop, Object content) {
		OntologyChange deleted = null;
		
		Iterator i = changes.iterator();
		while(i.hasNext()) {
			OntologyChange oc = (OntologyChange) i.next();
			
			if(oc instanceof RemoveAnnotationInstance) {
				RemoveAnnotationInstance change = (RemoveAnnotationInstance) oc;
				if (change.getSubject().equals(subj) && change.getProperty().equals(prop) && change.getContent().equals(content))
					deleted = oc;
			}
		}
		
		return deleted;
	}
	
	protected boolean renderAnnotations(OWLObject obj) throws OWLException {
		
		boolean printed = false;
		if (!obj.getAnnotations(reasoner.getOntology()).isEmpty()) {
			printed = true;
			println();
			String annTitle = " <b>Annotations</b>:";
			if (editorEnabled) annTitle += addTitle("A-ANN");
			println(annTitle);
			for (Iterator it = obj.getAnnotations(reasoner.getOntology()).iterator(); it.hasNext();) {
				OWLAnnotationInstance oai = (OWLAnnotationInstance) it.next();
				
				if (editorEnabled) {
					OntologyChange deleted = isDeleted(obj, oai.getProperty(), oai.getContent());				
					if (deleted!=null) {
						print("<font color=\"red\"><strike>");
						print("  " + escape(shortForm(oai.getProperty().getURI())) + " ");
						renderAnnotationContent(oai.getContent());
						print("</strike></font>");
						addUndo(deleted, "Delete", annTitle);
					}
					else {
						print("  " + escape(shortForm(oai.getProperty().getURI())) + " ");
						renderAnnotationContent(oai.getContent());
						addDelete(oai, "A-ANN");
					}
				}
				else {
					print("  " + escape(shortForm(oai.getProperty().getURI())) + " ");
					renderAnnotationContent(oai.getContent());
				}
				println();
				/* Do we need to do this??? */
				visitor.reset();
				oai.accept(visitor);
				// 		if (it.hasNext()) {
				// 		    println();
				// 		}
			}
		}
		
		// print temporarily added changes
		if (editorEnabled) {
			Set added = this.getAddedAnnotations(obj); 
			if (added.size()>0) {
				// print title if not printed already
				String annTitle = " <b>Annotations</b>:";
				annTitle += addTitle("A-ANN");
				if (!printed) {
					println(annTitle);
					printed = true;
				}
				// print added changes in green (and add undo links)
				for (Iterator iter = added.iterator(); iter.hasNext(); ) {
					AddAnnotationInstance change = (AddAnnotationInstance) iter.next();
					print("<font color=\"green\">");
					print("  " + escape(shortForm(change.getProperty().getURI())) + " ");
					renderAnnotationContent(change.getContent());
					print("</font>");
					this.addUndo(change, "Add", annTitle);
					println();
				}
			}
		}
		return printed;
	}

	private boolean isExplicit(OWLObject subj, String code, OWLObject obj) throws OWLException {
		return this.isExplicit(subj, code, obj, null);
	}
	
	/**
	 * Given a subject (OWL entity) and an object (OWL entity) and a type of relation
	 * checks if the relation is explicitly defined in the ontology or not
	 * If not, it is either inferred by the reasoner or imported and hence is
	 * displayed in italics
	 * @param subj - subject of the relationship
	 * @param code - type of relationship
	 * @param obj - object of the relationship
	 * @param value - value only specified for ind-prop-value assertions
	 * @return
	 * @throws OWLException
	 */
	private boolean isExplicit(OWLObject subj, String code, OWLObject obj, Object value) throws OWLException {
		
		OWLOntology ontology = reasoner.getOntology();
		OWLDataFactory factory = ontology.getOWLDataFactory();
		
		if (code.equals("C-INT")) {
			if (subj instanceof OWLClass) {
				OWLClass cla = (OWLClass) subj;
				Iterator iter = cla.getEquivalentClasses(ontology).iterator();
				while (iter.hasNext()) {
					Object equ = iter.next();
					if (equ instanceof OWLAnd) {
						OWLAnd inter = (OWLAnd) equ;
						if (inter.getOperands().contains(obj)) return true;
					}
				}
				return false;
			}
		}
		else if (code.equals("C-UNI")) {
			if (subj instanceof OWLClass) {
				OWLClass cla = (OWLClass) subj;
				Iterator iter = cla.getEquivalentClasses(ontology).iterator();
				while (iter.hasNext()) {
					Object equ = iter.next();
					if (equ instanceof OWLOr) {
						OWLOr union = (OWLOr) equ;
						if (union.getOperands().contains(obj)) return true;
					}
				}
				return false;
			}
		}
		else if (code.equals("I-ONE")) {
			if (subj instanceof OWLClass && obj instanceof OWLIndividual) {
				OWLClass cla = (OWLClass) subj;
				Iterator iter = cla.getEnumerations(ontology).iterator();
				while (iter.hasNext()) {
					OWLEnumeration enumer = (OWLEnumeration) iter.next();
					for (Iterator iter2 = enumer.getIndividuals().iterator(); iter2.hasNext(); ) {
						OWLIndividual ind = (OWLIndividual) iter2.next();
						if (ind.equals(obj)) return true;	
					}				
				}
			}
			return false;
		}
		else if (code.equals("C-DIS")) {
			Iterator iter = ontology.getClassAxioms().iterator();
			while (iter.hasNext()) {
				OWLClassAxiom axiom = (OWLClassAxiom) iter.next(); 
				if (axiom instanceof OWLDisjointClassesAxiom) {
					OWLDisjointClassesAxiom disjAxiom = (OWLDisjointClassesAxiom) axiom;
					if (disjAxiom.getDisjointClasses().contains(subj) && disjAxiom.getDisjointClasses().contains(obj)) return true;
				}
			}
			return false;
		}
		else if(code.equals("C-SUB")) {
			boolean asserted = false;
			if(subj instanceof OWLClass)
				asserted = ((OWLClass) subj).getSuperClasses(ontology).contains(obj);
			OWLSubClassAxiom subClassAxiom = factory.getOWLSubClassAxiom((OWLDescription)subj, (OWLDescription)obj);
			if (!asserted) asserted = (ontology.getClassAxioms().contains(subClassAxiom));
			return asserted;
			
		}
		else if(code.equals("C-SUP")) {
			boolean asserted = false;
			if(obj instanceof OWLClass)
				asserted = ((OWLClass) obj).getSuperClasses(ontology).contains(subj);
			OWLSubClassAxiom subClassAxiom = factory.getOWLSubClassAxiom((OWLDescription)obj, (OWLDescription)subj);
			if (!asserted) asserted = (ontology.getClassAxioms().contains(subClassAxiom));				
			return asserted;
		}
		else if(code.equals("C-EQU")) {
			if(subj instanceof OWLClass)
				return ((OWLClass) subj).getEquivalentClasses(reasoner.getOntologies()).contains(obj);
			if(obj instanceof OWLClass)
				return ((OWLClass) obj).getEquivalentClasses(reasoner.getOntologies()).contains(subj);
			else {
				Set set = new HashSet();
				set.add(subj);
				set.add(obj);
				OWLEquivalentClassesAxiom eqClassAxiom = factory.getOWLEquivalentClassesAxiom(set);
				return (ontology.getClassAxioms().contains(eqClassAxiom));				
			}
		}
		else if(code.equals("C-NOT")) {
			if(subj instanceof OWLClass)
				return OWLDescriptionFinder.getComplements((OWLClass) subj, reasoner.getOntologies()).contains(obj);
			if(obj instanceof OWLClass)
				return OWLDescriptionFinder.getComplements((OWLClass) obj, reasoner.getOntologies()).contains(subj);
		}
		else if(code.equals("P-DOM")) {
			if((subj instanceof OWLClass) && (obj instanceof OWLProperty)) {
				OWLClass cla = (OWLClass) subj;
				OWLProperty prop = (OWLProperty) obj;
				return (prop.getDomains(ontology).contains(cla));
				//return (getPropertiesWithDomain(cla, false).contains(prop));
			}
		}
		else if(code.equals("P-INV")) {
			if((subj instanceof OWLProperty) && (obj instanceof OWLProperty)) {
				OWLProperty p1 = (OWLProperty) subj;
				OWLProperty p2 = (OWLProperty) obj;
				return (((OWLObjectProperty) p1).getInverses(ontology).contains(p2));
				//return (getPropertiesWithDomain(cla, false).contains(prop));
			}
		}
		else if(code.equals("C-HASDOM")) {
			if((obj instanceof OWLClass) && (subj instanceof OWLProperty)) {
				OWLClass cla = (OWLClass) obj;
				OWLProperty prop = (OWLProperty) subj;
				return (prop.getDomains(ontology).contains(cla));
			}
		}
		else if(code.equals("P-RAN")) {
			if((subj instanceof OWLClass) && (obj instanceof OWLProperty)) {
				OWLClass cla = (OWLClass) subj;
				OWLProperty prop = (OWLProperty) obj;
				return (prop.getRanges(ontology).contains(cla));
				//return (getPropertiesWithRange(cla, false).contains(prop));
			}
		}
		else if(code.equals("C-HASRAN")) {
			if((obj instanceof OWLClass) && (subj instanceof OWLProperty)) {
				OWLClass cla = (OWLClass) obj;
				OWLProperty prop = (OWLProperty) subj;
				return (prop.getRanges(ontology).contains(cla));
			}
		}
		else if(code.equals("C-TYP")) {
		    OWLIndividual ind = (OWLIndividual) subj;
		    OWLDescription desc = (OWLDescription) obj;
		    if(desc instanceof OWLClass)		        
		    	return ind.getTypes(ontology).contains(desc);
		    else
		        return true;
		}
		else if(code.equals("I-INS")) {
		    OWLIndividual ind = (OWLIndividual) obj;
		    OWLClass c = (OWLClass) subj;
		    return ind.getTypes(ontology).contains(c);
		}
		else if(code.equals("I-SAM")) {
			// get individual axioms for the ontology
			for (Iterator iter = ontology.getIndividualAxioms().iterator(); iter.hasNext(); ){
				OWLIndividualAxiom indAxiom = (OWLIndividualAxiom) iter.next();
				// get the set of individuals participating in each axiom
				Set inds = indAxiom.getIndividuals();
				if(!(indAxiom instanceof OWLSameIndividualsAxiom))
				    continue;
				if(inds.contains(subj) && inds.contains(obj))
				    return true;			
			}
		    return false;
		}		
		else if(code.equals("I-DIF")) {
			// get individual axioms for the ontology
			for (Iterator iter = ontology.getIndividualAxioms().iterator(); iter.hasNext(); ){
				OWLIndividualAxiom indAxiom = (OWLIndividualAxiom) iter.next();
				// get the set of individuals participating in each axiom
				Set inds = indAxiom.getIndividuals();
				if(!(indAxiom instanceof OWLDifferentIndividualsAxiom))
				    continue;
				if(inds.contains(subj) && inds.contains(obj))
				    return true;			
			}
		    return false;
		}			
		else if(code.equals("P-INSD")) {
			OWLIndividual ind = (OWLIndividual) subj;
			OWLDataProperty prop = (OWLDataProperty) obj;
			Set allValues = (Set) ind.getDataPropertyValues(ontology).get( prop );
			OWLDataValue val = (OWLDataValue) value;
			// Evren: if the values are defined in another ontology then allValues will be null
			if( allValues != null ) {
				for (Iterator iter = allValues.iterator(); iter.hasNext();) {
					OWLDataValue v = (OWLDataValue) iter.next();
					if(v.equals(val))
						return true;
				}
			}
			return false;
		}
		else if(code.equals("P-INSO")) {
			OWLIndividual ind = (OWLIndividual) subj;
			OWLObjectProperty prop = (OWLObjectProperty) obj;
			OWLIndividual val = (OWLIndividual) value;
			Set allValues = (Set) ind.getObjectPropertyValues(ontology).get( prop );
			return (allValues != null) && allValues.contains( val );
		}
		return true;
	}
	
	/**
	 * Check if a property attribute removal change is currently in the
	 * swoopModel.unCommittedChanges and return the change if found
	 * @param prop - property on which attribute is to be changed
	 * @param code - code for the attribute (functional, inversefunctional..)
	 * @return
	 */
	private OntologyChange isDeleted(OWLProperty prop, String code) {
//		 isDeleted function for property-attribues
		Iterator i = changes.iterator();
		while(i.hasNext()) {
			OntologyChange oc = (OntologyChange) i.next();
			if (oc instanceof SetFunctional && code.equals("P-FUN")) {
				SetFunctional change = (SetFunctional) oc;
				if (change.getProperty().equals(prop) && !change.isFunctional()) return oc;
			}
			if (oc instanceof SetInverseFunctional && code.equals("P-IFUN")) {
				SetInverseFunctional change = (SetInverseFunctional) oc;
				if (change.getProperty().equals(prop) && !change.isInverseFunctional()) return oc;
			}
			if (oc instanceof SetTransitive && code.equals("P-TRA")) {
				SetTransitive change = (SetTransitive) oc;
				if (change.getProperty().equals(prop) && !change.isTransitive()) return oc;
			}
			if (oc instanceof SetSymmetric && code.equals("P-SYM")) {
				SetSymmetric change = (SetSymmetric) oc;
				if (change.getProperty().equals(prop) && !change.isSymmetric()) return oc;
			}
		}
		return null;
	}
	
	/**
	 * Function to check instance' property-value axioms that are deleted
	 * If an ontology change is returned, corresponding axiom is striked out in the HTML renderer
	 * @param prop
	 * @param value
	 * @return
	 */
	private OntologyChange isDeleted(OWLProperty prop, OWLObject value) {
		
		// isDeleted function for instance' property-value pairs only
		Iterator i = changes.iterator();
		while(i.hasNext()) {
			OntologyChange oc = (OntologyChange) i.next();
			
			if (oc instanceof RemoveDataPropertyInstance && prop instanceof OWLDataProperty) {
				RemoveDataPropertyInstance rem = (RemoveDataPropertyInstance) oc;
				if (rem.getSubject().equals((OWLIndividual) displayedEntity) 
				&& rem.getProperty().equals((OWLDataProperty) prop)
				&& rem.getObject().equals((OWLDataValue) value))
					return oc;
			}
			
			if (oc instanceof RemoveObjectPropertyInstance && prop instanceof OWLObjectProperty) {
				RemoveObjectPropertyInstance rem = (RemoveObjectPropertyInstance) oc;
				if (rem.getSubject().equals((OWLIndividual) displayedEntity) 
				&& rem.getProperty().equals((OWLObjectProperty) prop)
				&& rem.getObject().equals((OWLIndividual) value))
					return oc;
			}
		}
		return null;
	}
	
	/**
	 * Given a subject (OWL entity) and an object (OWL entity) and a relationship code,
	 * check if the relationship axiom is deleted by the user or not
	 * If an ontology change is returned, corresponding axiom is striked out in the HTML renderer
	 * @param subj - subject of the relationship axiom
	 * @param code - relation type code 
	 * @param obj - object of the relationship axiom
	 * @return
	 * @throws OWLException
	 */
	private OntologyChange isDeleted(OWLObject subj, String code, OWLObject obj) throws OWLException {
		OntologyChange deleted = null;
		
		Iterator i = changes.iterator();
		while(i.hasNext()) {
			OntologyChange oc = (OntologyChange) i.next();
			
			if(oc instanceof RemoveDomain) {
				RemoveDomain rem = (RemoveDomain) oc;
				if ((code.equals("C-HASDOM")) && (rem.getProperty().equals((OWLProperty) subj)) && (rem.getDomain().equals((OWLDescription) obj))) {
					deleted = oc;
				}
				if ((code.equals("P-DOM")) && (rem.getProperty().equals((OWLProperty) obj)) && (rem.getDomain().equals((OWLDescription) subj))) {
					deleted = oc;
				}
			}
			else if(oc instanceof RemoveInverse) {
				RemoveInverse rem = (RemoveInverse) oc;
				if ((code.equals("P-INV")) && (rem.getProperty().equals((OWLObjectProperty) subj)) && (rem.getInverse().equals((OWLObjectProperty) obj))) {
					deleted = oc;
				}
				// also inverse is deleted
				if ((code.equals("P-INV")) && (rem.getProperty().equals((OWLObjectProperty) obj)) && (rem.getInverse().equals((OWLObjectProperty) subj))) {
					deleted = oc;
				}
			}
			else if(oc instanceof RemoveObjectPropertyRange) {
				RemoveObjectPropertyRange rem = (RemoveObjectPropertyRange) oc;
				if (subj instanceof OWLObjectProperty) {
					if ((code.equals("C-HASRAN")) && (rem.getProperty().equals((OWLObjectProperty) subj)) && (rem.getRange().equals((OWLDescription) obj))) {
						deleted = oc;
					}
				}
				if (obj instanceof OWLObjectProperty) {
					if ((code.equals("P-RAN")) && (rem.getProperty().equals((OWLObjectProperty) obj)) && (rem.getRange().equals((OWLDescription) subj))) {
						deleted = oc;
					}
				}
			}
			else if(oc instanceof RemoveDataPropertyRange) {
				
				if (subj instanceof OWLDataProperty && obj instanceof OWLDataRange) {
					RemoveDataPropertyRange rem = (RemoveDataPropertyRange) oc;
					if ((code.equals("C-HASRAN")) && (rem.getProperty().equals((OWLDataProperty) subj)) && (rem.getRange().equals((OWLDataRange) obj))) {
						deleted = oc;
					}
				}
			}
			else if(oc instanceof RemovePropertyAxiom) {
				RemovePropertyAxiom rem = (RemovePropertyAxiom) oc;
				if (rem.getAxiom() instanceof OWLEquivalentPropertiesAxiom) {
					OWLEquivalentPropertiesAxiom epAxiom = (OWLEquivalentPropertiesAxiom) rem.getAxiom();
					if (code.equals("P-EQU")
					&& epAxiom.getProperties().contains((OWLProperty) obj)
					&& epAxiom.getProperties().contains((OWLProperty) subj)) {
						deleted = oc;
					}
				}
				else if (rem.getAxiom() instanceof OWLSubPropertyAxiom) {
					OWLSubPropertyAxiom subPAxiom = (OWLSubPropertyAxiom) rem.getAxiom();
					if (code.equals("P-SUB") 
						&& subPAxiom.getSubProperty().equals((OWLProperty) subj) 
						&& subPAxiom.getSuperProperty().equals((OWLProperty) obj)) {
						deleted = oc;
					}
					else if (code.equals("P-SUP") 
						&& subPAxiom.getSubProperty().equals((OWLProperty) obj) 
						&& subPAxiom.getSuperProperty().equals((OWLProperty) subj)) {
						deleted = oc;
					}
				}
			}
			else if(oc instanceof RemoveSuperProperty) {
				RemoveSuperProperty rem = (RemoveSuperProperty) oc;
				if (code.equals("P-SUB") 
					&& rem.getProperty().equals((OWLProperty) subj) 
					&& rem.getSuperProperty().equals((OWLProperty) obj)) {
					deleted = oc;
				}
				else if (code.equals("P-SUP") 
					&& rem.getProperty().equals((OWLProperty) obj) 
					&& rem.getSuperProperty().equals((OWLProperty) subj)) {
					deleted = oc;
				}
			}
			else if(oc instanceof RemoveClassAxiom) {
				RemoveClassAxiom rem = (RemoveClassAxiom) oc;
				if (rem.getAxiom() instanceof OWLDisjointClassesAxiom) {
					OWLDisjointClassesAxiom change = (OWLDisjointClassesAxiom) rem.getAxiom();
					if ((code.equals("C-DIS")) && (change.getDisjointClasses().contains(subj)) && (change.getDisjointClasses().contains(obj))) {
						return oc;
					}
				}
				else if (rem.getAxiom() instanceof OWLSubClassAxiom) {
					OWLSubClassAxiom axiom = (OWLSubClassAxiom) rem.getAxiom();
					if(code.equals("C-SUB")
					&& axiom.getSubClass().equals(subj)
					&& axiom.getSuperClass().equals(obj))
						return oc;
					else 
					if(code.equals("C-SUP")
					&& axiom.getSubClass().equals(obj)
					&& axiom.getSuperClass().equals(subj))
						return oc;
				}
			}
			else if(oc instanceof RemoveSuperClass) {
				RemoveSuperClass change = (RemoveSuperClass) oc;
				if(code.equals("C-SUB")
				&& change.getOWLClass().equals(subj)
				&& change.getDescription().equals(obj))
					return oc;
				else 
				if(code.equals("C-SUP")
				&& change.getOWLClass().equals(obj)
				&& change.getDescription().equals(subj))
					return oc;
			}
			else
			if(oc instanceof RemoveEquivalentClass) {
				RemoveEquivalentClass change = (RemoveEquivalentClass) oc;
				if(code.equals("C-EQU")
				&& change.getOWLClass().equals(subj)
				&& change.getDescription().equals(obj))
					return oc;
			}
			else
			if(oc instanceof BooleanElementChange) {
				
				BooleanElementChange change = (BooleanElementChange) oc;
				if(code.equals("C-INT")
					&& change.getChangeType().equals("Remove")	
					&& change.getOWLClass().equals(subj)
					&& change.getType().equals(OWLAnd.class)
					&& change.getOWLDescription().hashCode()==obj.hashCode())
						deleted = oc;		
				else
				if(code.equals("C-UNI")
					&& change.getChangeType().equals("Remove")
					&& change.getOWLClass().equals(subj)
					&& change.getType().equals(OWLOr.class)
					&& change.getOWLDescription().hashCode()==obj.hashCode())
						deleted = oc;
				else
				if(code.equals("C-NOT")
						&& change.getChangeType().equals("Remove")
						&& change.getOWLClass().equals(subj)
						&& change.getType().equals(OWLNot.class)
						&& change.getOWLDescription().hashCode()==obj.hashCode())
							deleted = oc;
			}	
			else
			if(oc instanceof AddEquivalentClass) {
				AddEquivalentClass change = (AddEquivalentClass) oc;
				if(code.equals("C-INT")
				&& change.getOWLClass().equals(subj)
				&& change.getDescription() instanceof OWLAnd
				&& ((OWLAnd)change.getDescription()).getOperands().contains(obj))
					deleted = null;	
				else
				if(code.equals("C-UNI")
				&& change.getOWLClass().equals(subj)
				&& change.getDescription() instanceof OWLOr
				&& ((OWLOr)change.getDescription()).getOperands().contains(obj))
					deleted = null;					
			}			
			else
			if (oc instanceof EnumElementChange) {
				EnumElementChange change = (EnumElementChange) oc;
				if (code.equals("I-ONE")
				&& change.getChangeType().equals("Remove")
				&& change.getOWLClass().equals(subj)
				&& change.getOWLIndividual().hashCode()==obj.hashCode())
					deleted = oc;
			}
			else
			if (oc instanceof RemoveIndividualClass) {
				RemoveIndividualClass change = (RemoveIndividualClass) oc;
				if (code.equals("I-INS")
				&& change.getDescription().equals(subj)
				&& change.getIndividual().equals(obj))
					deleted = oc;
				else
				if (code.equals("C-TYP")
				&& change.getDescription().equals(obj)
				&& change.getIndividual().equals(subj))
					deleted = oc;				    
			}
			else
			if (oc instanceof AddEnumeration) {
				AddEnumeration change = (AddEnumeration) oc;
				if (code.equals("I-ONE")
				&& change.getOWLClass().equals(subj)
				&& change.getEnumeration().getIndividuals().contains(obj))
					deleted = null;
			}
			else if(oc instanceof RemoveIndividualAxiom) {
				RemoveIndividualAxiom rem = (RemoveIndividualAxiom) oc;
				if (rem.getAxiom() instanceof OWLSameIndividualsAxiom) {
					OWLSameIndividualsAxiom sameAxiom = (OWLSameIndividualsAxiom) rem.getAxiom();
					if (code.equals("I-SAM")
					&& sameAxiom.getIndividuals().contains((OWLIndividual) obj)
					&& sameAxiom.getIndividuals().contains((OWLIndividual) subj)) {
						deleted = oc;
					}
				}
				if (rem.getAxiom() instanceof OWLDifferentIndividualsAxiom) {
					OWLDifferentIndividualsAxiom diffAxiom = (OWLDifferentIndividualsAxiom) rem.getAxiom();
					if (code.equals("I-DIF")
					&& diffAxiom.getIndividuals().contains((OWLIndividual) obj)
					&& diffAxiom.getIndividuals().contains((OWLIndividual) subj)) {
						deleted = oc;
					}
				}
			}			
		}
		
		return deleted;
	}
	
	/**
	 * Same as getAdded(subj, code) except this one only checks
	 * AddAnnotationInstance changes
	 * @param subj
	 * @return
	 * @throws OWLException
	 */
	private Set getAddedAnnotations(OWLObject subj) throws OWLException {
		Set added = new HashSet();
		
		Iterator i = changes.iterator();
		while(i.hasNext()) {
			OntologyChange oc = (OntologyChange) i.next();
			if (oc instanceof AddAnnotationInstance) {
				AddAnnotationInstance change = (AddAnnotationInstance) oc;
				if (change.getSubject().equals(subj)) added.add(oc); 
			}
		}
		
		return added;
	}
	
	/**
	 * Given a subject (OWL entity) and a code for the axiom type,
	 * match any current uncommitted ontology change involving subject and of type code
	 * and return a map between object of the change and the change itself
	 * Stuff returned is rendered in 'green' in the HTML renderer
	 * @param subj - subject of the axiom change
	 * @param code - type of axiom change
	 * @return
	 * @throws OWLException
	 */
	private Map getAdded(OWLObject subj, String code) throws OWLException {
		Map added = new Hashtable();
		
		Iterator i = changes.iterator();
		while(i.hasNext()) {
			OntologyChange oc = (OntologyChange) i.next();
		
			if (oc instanceof SetFunctional) {
				SetFunctional change = (SetFunctional) oc;
				if (code.equals("ATTRIB")) {
					if (change.isFunctional() && change.getProperty().equals(subj)) {
						// only for property attributes, add a set of ontology changes as the value in the hashmap
						Set ocSet = new HashSet();
						if (added.containsKey(subj)) ocSet = (HashSet) added.get(subj);
						ocSet.add(oc);
						added.put(subj, ocSet);	
					}										
				}
			}
			else
			if (oc instanceof SetInverseFunctional) {
				SetInverseFunctional change = (SetInverseFunctional) oc;
				if (code.equals("ATTRIB")) {
					if (change.isInverseFunctional() && change.getProperty().equals(subj)) {
//						 only for property attributes, add a set of ontology changes as the value in the hashmap
						Set ocSet = new HashSet();
						if (added.containsKey(subj)) ocSet = (HashSet) added.get(subj);
						ocSet.add(oc);
						added.put(subj, ocSet);
					}
											
				}
			}
			else
			if (oc instanceof SetTransitive) {
				SetTransitive change = (SetTransitive) oc;
				if (code.equals("ATTRIB")) {
					if (change.isTransitive() && change.getProperty().equals(subj)) {
//						 only for property attributes, add a set of ontology changes as the value in the hashmap
						Set ocSet = new HashSet();
						if (added.containsKey(subj)) ocSet = (HashSet) added.get(subj);
						ocSet.add(oc);
						added.put(subj, ocSet);
					}
											
				}
			}
			else
			if (oc instanceof SetSymmetric) {
				SetSymmetric change = (SetSymmetric) oc;
				if (code.equals("ATTRIB")) {
					if (change.isSymmetric() && change.getProperty().equals(subj)) {
//						 only for property attributes, add a set of ontology changes as the value in the hashmap
						Set ocSet = new HashSet();
						if (added.containsKey(subj)) ocSet = (HashSet) added.get(subj);
						ocSet.add(oc);
						added.put(subj, ocSet);
					}
											
				}
			}	
			else
			if(oc instanceof AddSuperClass) {
				AddSuperClass change = (AddSuperClass) oc;
				if(code.equals("C-SUB")
				&& change.getOWLClass().equals(subj))
					added.put(change.getDescription(), oc);
				else 
				if(code.equals("C-SUP")
				&& change.getDescription().equals(subj))
					added.put(change.getOWLClass(), oc);				
			}
			else if(oc instanceof RemoveSuperClass) {
				RemoveSuperClass change = (RemoveSuperClass) oc;
				if(code.equals("C-SUB")
				&& change.getOWLClass().equals(subj))
					added.remove(change.getDescription());
				else 
				if(code.equals("C-SUP")
				&& change.getDescription().equals(subj))
					added.remove(change.getOWLClass());				
			}
			else
			if(oc instanceof AddEquivalentClass) {
				AddEquivalentClass change = (AddEquivalentClass) oc;
				if(code.equals("C-EQU")
				&& change.getOWLClass().equals(subj))
					added.put(change.getDescription(), oc);
			}
			else
			if(oc instanceof BooleanElementChange) {
				BooleanElementChange change = (BooleanElementChange) oc;
				if(code.equals("C-INT")
				&& change.getChangeType().equals("Add")
				&& change.getType().equals(OWLAnd.class)
				&& change.getOWLClass().equals(subj)) {
					added.put(change.getOWLDescription(), oc);
				}
				else
				if(code.equals("C-UNI")
				&& change.getChangeType().equals("Add")
				&& change.getType().equals(OWLOr.class)
				&& change.getOWLClass().equals(subj)) {
					added.put(change.getOWLDescription(), oc);
				}
				else
				if(code.equals("C-NOT")
					&& change.getChangeType().equals("Add")
					&& change.getType().equals(OWLNot.class)
					&& change.getOWLClass().equals(subj)) {
						added.put(change.getOWLDescription(), oc);
					}
			}	
			else
			if(oc instanceof RemoveEquivalentClass) {
				RemoveEquivalentClass change = (RemoveEquivalentClass) oc;
				if(code.equals("C-EQU")
				&& change.getOWLClass().equals(subj)
				&& !(change.getDescription() instanceof OWLAnd)
				&& !(change.getDescription() instanceof OWLOr)
				&& !(change.getDescription() instanceof OWLEnumeration))
					added.remove(change.getDescription());				
				else if(code.equals("C-INT")
				&& change.getOWLClass().equals(subj)
				&& change.getDescription() instanceof OWLAnd) 
					added.keySet().removeAll(((OWLAnd)change.getDescription()).getOperands());				
				else
				if(code.equals("C-UNI")
				&& change.getOWLClass().equals(subj)
				&& change.getDescription() instanceof OWLOr)
					added.keySet().removeAll(((OWLOr)change.getDescription()).getOperands());
			}				
			else
			if (oc instanceof AddClassAxiom) {
				if (code.equals("C-DIS")) {
					AddClassAxiom change = (AddClassAxiom) oc;
					if (change.getAxiom() instanceof OWLDisjointClassesAxiom) {
						OWLDisjointClassesAxiom disAxiom = (OWLDisjointClassesAxiom) change.getAxiom();
						if (disAxiom.getDisjointClasses().contains(subj)) {
							Iterator j = disAxiom.getDisjointClasses().iterator();					
							while(j.hasNext())
								added.put(j.next(), oc);
							added.remove(subj);
						}
					}
				}
				else if (code.equals("C-SUP")) {
					AddClassAxiom change = (AddClassAxiom) oc;
					if (change.getAxiom() instanceof OWLSubClassAxiom) {
						OWLSubClassAxiom subAxiom = (OWLSubClassAxiom) change.getAxiom();
						if (subAxiom.getSuperClass().equals(subj)) {
							added.put(subAxiom.getSubClass(), oc);							
						}
					}
				}
			}
			else
			if(oc instanceof AddDomain) {
				AddDomain change = (AddDomain) oc;
				if(code.equals("P-DOM")
				&& change.getDomain().equals(subj)
				) {
					added.put(change.getProperty(), oc);
				}
				if(code.equals("C-HASDOM")
				&& change.getProperty().equals(subj)
				) {
					added.put(change.getDomain(), oc);
				}
			}				
			else
			if (oc instanceof AddObjectPropertyRange) {
				AddObjectPropertyRange change = (AddObjectPropertyRange) oc;
				if(code.equals("P-RAN")
				&& change.getRange().equals(subj)
				) {
					added.put(change.getProperty(), oc);
				}
				if(code.equals("C-HASRAN")
				&& change.getProperty().equals(subj)
				) {
					added.put(change.getRange(), oc);
				}
			}
			else
			if (oc instanceof AddInverse){
				AddInverse change = (AddInverse) oc;
				if(code.equals("P-INV")
				&& change.getProperty().equals(subj)) {
					added.put(change.getInverse(), oc);
				}
				// also inverse gets added
				if(code.equals("P-INV")
				&& change.getInverse().equals(subj)) {
					added.put(change.getProperty(), oc);
				}
			}
			else 
			if (oc instanceof AddDataPropertyRange) {
					AddDataPropertyRange change = (AddDataPropertyRange) oc;
					if(code.equals("P-RAN")
					&& change.getRange().equals(subj)
					) {
						added.put(change.getProperty(), oc);
					}
					if(code.equals("C-HASRAN")
					&& change.getProperty().equals(subj)
					) {
						added.put(change.getRange(), oc);
					}
				}
			else
			if (oc instanceof AddPropertyAxiom) {
				AddPropertyAxiom change = (AddPropertyAxiom) oc;
				if (change.getAxiom() instanceof OWLEquivalentPropertiesAxiom && code.equals("P-EQU")) {
					OWLEquivalentPropertiesAxiom propAxiom = (OWLEquivalentPropertiesAxiom) change.getAxiom();
					if (code.equals("P-EQU") && propAxiom.getProperties().contains(subj)) {
						Set equSet = new HashSet(propAxiom.getProperties());
						equSet.remove(subj);
						Iterator iter = equSet.iterator();
						while (iter.hasNext()) {
							added.put(iter.next(), oc);
						}
					}
				}
			}
			if (oc instanceof AddSuperProperty) {
				AddSuperProperty change = (AddSuperProperty) oc;
				if ((code.equals("P-SUB")) && (change.getProperty().equals(subj))) {
					added.put(change.getSuperProperty(), oc);
				}
				if ((code.equals("P-SUP")) && (change.getSuperProperty().equals(subj))) {
					added.put(change.getProperty(), oc);
				}
			}
			else 
			if (oc instanceof AddIndividualClass) {
				AddIndividualClass change = (AddIndividualClass) oc;
				OWLIndividual ind = change.getIndividual();
				OWLDescription desc = change.getDescription();
				if (code.equals("I-INS") && desc.equals(subj)) {
					added.put(change.getIndividual(), oc);
				}
				else
				if (code.equals("C-TYP") && ind.equals(subj)) {
					added.put(change.getDescription(), oc);
				}
			}
			else
			if (oc instanceof EnumElementChange) {
				EnumElementChange change = (EnumElementChange) oc;
				if ((code.equals("I-ONE")) && (change.getChangeType().equals("Add")) && (change.getOWLClass().equals(subj))) {
					added.put(change.getOWLIndividual(), oc);	
				}
			}
			else
			if (oc instanceof RemoveEnumeration) {
				RemoveEnumeration change = (RemoveEnumeration) oc;
				if ((code.equals("I-ONE")) && (change.getOWLClass().equals(subj))) {
					OWLEnumeration enumElem = change.getEnumeration();
					added.keySet().removeAll(enumElem.getIndividuals());					
				}
			}
			else
			if (oc instanceof AddIndividualAxiom) {
				AddIndividualAxiom change = (AddIndividualAxiom) oc;
				if (code.equals("I-SAM")) {					
					if (change.getAxiom() instanceof OWLSameIndividualsAxiom) {
						OWLSameIndividualsAxiom sameAxiom = (OWLSameIndividualsAxiom) change.getAxiom();
						if (sameAxiom.getIndividuals().contains(subj)) {
							Iterator j = sameAxiom.getIndividuals().iterator();					
							while(j.hasNext())
								added.put(j.next(), oc);
							added.remove(subj);
						}
					}
				}
				if (code.equals("I-DIF")) {					
					if (change.getAxiom() instanceof OWLDifferentIndividualsAxiom) {
						OWLDifferentIndividualsAxiom diffAxiom = (OWLDifferentIndividualsAxiom) change.getAxiom();
						if (diffAxiom.getIndividuals().contains(subj)) {
							Iterator j = diffAxiom.getIndividuals().iterator();					
							while(j.hasNext())
								added.put(j.next(), oc);
							added.remove(subj);
						}
					}
				}
			}
		}
			
		return added;
	}
	
	private void printConciseObject(OWLEntity obj) {
		try {
			if (obj==null) return;
			String objURI = obj.getURI().toString();
			String objName = shortForm(obj.getURI());
//			String imageURI = baseImageURI; // JAVA WEBSTART STUFF
			
			boolean showIcon = swoopModel.getShowIcons();
			if (obj instanceof OWLClass && swoopModel.getEnableDebugging()) showIcon = true;
			
			if (showIcon || swoopModel.getEnableDebugging()) {				
				String imageURI = "http://www.mindswap.org/2004/SWOOP/icons/"; // JAVA WEBSTART STUFF
				if (obj instanceof OWLClass) {
					
					boolean isConsistent = reasoner.isConsistent((OWLClass) obj);
					
					if (isConsistent) imageURI += "InconsistentClass.gif";
					else imageURI += "Class.gif"; 
				}
			    //*************************************************
			    //changed for Econns
			    //*************************************************
				else if (obj instanceof OWLObjectProperty){
				  if(((OWLObjectProperty)obj).isLink())	{
				  	imageURI += "LinkProperty.GIF";
				  }
				  else{
					imageURI += "Property.gif";
				  }
				}
				else if (obj instanceof OWLDataProperty) imageURI += "DataProperty.gif";
				else if (obj instanceof OWLIndividual) imageURI += "Instance.gif";
				if (imageURI.endsWith(".gif") || imageURI.endsWith(".GIF") ) print("<img src=\""+imageURI+"\">");
			}
			
			print("<a href=\""+StringEscapeUtils.escapeHtml(objURI)+"\">"+StringEscapeUtils.escapeHtml(objName)+"</a>");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private void printLine(OWLObject obj, String title) throws OWLException {
		String code = getCode(title);
		boolean explicit = isExplicit(displayedEntity, code, obj);
		OntologyChange oc = editorEnabled ?	isDeleted(displayedEntity, code, obj) : null;
		boolean deleted = (oc != null);
		
		if(deleted) print("<font color=\"red\"><strike>");
		else if(!explicit) print("<i>");
		
		printObject(obj);
		
		if (editorEnabled && !deleted && explicit) {
			// add delete hyperlink in editable mode
			// store object hash
			addDelete(obj, title);								
		}
		
		if(!explicit) {
			print("</i>");
			if (reasoner.supportsExplanation()) addWhy(obj, title);
		}
		else if(deleted) {
			print("</strike></font>");
			addUndo(oc, "Delete", title);
		}
		
		println();		
	}
	
	private void printCollection(Collection coll, String title) throws OWLException {
		if(coll.isEmpty()) 
			return;
			
		if(title != null) {
			println();
			println(title);
		}
		Iterator i = coll.iterator();
		while(i.hasNext()) {
			Object obj = i.next();	
			// if there is more than one element for this line
			// we only print the first one. rest are either
			// equivalent classes (or properties) 
			if(obj instanceof Collection) {
				if (((Collection) obj).size()>0)
					obj = ((Collection)obj).iterator().next();
				else 
					return; // empty set
			}
			
			// if debugging is enabled, for each class description, check consistency
			if (swoopModel.getEnableDebugging() && obj instanceof OWLDescription && !(obj instanceof OWLClass)) {
				URL imageURI; // = baseImageURI;
				OWLDescription claExpr = (OWLDescription) obj;
				boolean isConsistent = reasoner.isConsistent(claExpr);
				
				if (!isConsistent) {
					imageURI = SwoopIcons.getImageURL("InconsistentClassExpression.gif");
				}
				else {
					imageURI = SwoopIcons.getImageURL("ClassExpression.gif");
				}
				String hashCode = String.valueOf(claExpr.hashCode()); 
				OWLDescHash.put(hashCode, claExpr);
				print("<a href=\"<CE:"+hashCode+"\"><img src=\""+imageURI+"\"></a>");				
			}
			
			printLine((OWLObject) obj, title);
		}			
		
		insertAddedChanges(coll, displayedEntity, title, "");		
	}
	
	/**
	 * Accepts a collection (coll) of objects already displayed,
	 * gets all added objects on (displayedEntity), matching the (title)/code,
	 * inserts added objects not present in collection 
	 * @param coll
	 * @param displayedEntity
	 * @param title
	 * @param titleHead
	 * @throws OWLException
	 */
	protected boolean insertAddedChanges(Collection coll, OWLEntity displayedEntity, String title, String titleHead) throws OWLException {		
		boolean printed = false;
		String code = getCode(title);
		if(editorEnabled) {
			Map added = getAdded(displayedEntity, code);
			if(!added.isEmpty()) {
				
				printed = true;
				if (!titleHead.equals("")) {
					println();
					println(titleHead+addTitle(code));
				}
				
				Iterator j = added.entrySet().iterator();
				while(j.hasNext()) {
					Map.Entry entry = (Map.Entry) j.next();
					OWLObject obj = (OWLObject) entry.getKey();
					if(coll.contains(obj))
						continue;
					Object change = entry.getValue();
					print("<font color=\"green\">");
					printObject(obj);
					print("</font>");
					addUndo(change, "Add", title);
					println();
				}
			}
		}
		
		return printed;
	}

	
	
	protected void renderClass(OWLClass clazz) throws OWLException {
		
		// check if class being rendered is a class expression
		if (clazz.getURI().toString().indexOf("#ClassExpression")>=0) {
			String uri = clazz.getURI().toString();
			String hashCode = uri.substring(uri.indexOf("#ClassExpression")+16, uri.length());					
			// obtain class expression from hash
			OWLDescription claExpr = swoopModel.getCEfromHash(hashCode);
			
			// turn editable off
			boolean saveEditable = this.editorEnabled;
			this.editorEnabled = false;
			
			// print class expression
			print("Class Expression: ");
			printLine((OWLObject) claExpr, "");
			
			// print equivalent classes of CE
			String equTitle = "<b>Equivalent to:</b>";
			Set eqs = reasoner.equivalentClassesOf(claExpr);
			printCollection(eqs, equTitle);
			// print subclasses of CE
			String subTitle = "<b>Sub-Class of:</b>";
			Set sub = reasoner.superClassesOf(claExpr);
			printCollection(sub, subTitle);
			// print superclasses of CE
			String supTitle = "<b>Super-Class of:</b>";
			Set sup = reasoner.subClassesOf(claExpr);
			printCollection(sup, supTitle);			
			
			// restore editable setting
			this.editorEnabled = saveEditable;
			return;
		}
		
		// check if class is in current ontology or imported
		String importedLbl = "";
		imported = false;
		
		if (!clazz.getOntologies().contains(swoopModel.getSelectedOntology())) {
			importedLbl = "Imported ";
			imported = true;
		}
		
		Iterator i = null;
		List notPrinted = new ArrayList();
		
		OWLClass owlThing = reasoner.getOntology().getOWLDataFactory().getOWLThing();
		OWLClass owlNothing = reasoner.getOntology().getOWLDataFactory().getOWLNothing();
		
		//****** TESTING PURPOSES ONLY ***************
//		print("<a href=\"PELLET-SATURATION\">SATURATION TEST</a><br>");
		// *******************************************
		
		print("<b>"+importedLbl+"OWL-Class:</b>&nbsp;");
		printObject(clazz);
		
		if (importedLbl.length()>0) {			
			println();
			// print("<b>Imported from Ontology(s):</b>&nbsp;");
			// also print where the class is imported from
			Iterator ontIter = clazz.getOntologies().iterator();
			while (ontIter.hasNext()) {
				OWLOntology ont = (OWLOntology) ontIter.next();
				String ontURI = ont.getURI().toString();
				String ontName = ontURI.substring(ontURI.lastIndexOf("/")+1, ontURI.length());
				String classXPtrURI = clazz.getURI() + XPointers.asDefinedIn + "(" + ontURI + ")";
				print("See its definition in Imported Ontology: <a href=\""+escape(classXPtrURI)+"\">" + escape(ontName) + "</a>");
				// print("<a href=\""+ontURI+"asDefinedIn("+ontURI+")\">"+className+"</a>&nbsp;&nbsp;");
				println();
			}
			println();
		} 
		println();
		
		//*** explanations for inconsistent class
	    if(!reasoner.isConsistent( clazz )) {
			println("<font color=\"red\"><b>Unsatisfiable concept</b></font>");
			if (reasoner instanceof PelletReasoner && swoopModel.isDebugGlass()) {
				
				if (swoopModel.findAllMUPS)  parseTime = 0;
				Timer mupsTimer = new Timer("MUPS Processing Time");
			    mupsTimer.start();
				
				// create a set of sets for MUPS
				List MUPS = new ArrayList();
				List explStr = new ArrayList(); // also get explanation strings if using tableau
				
				Set explanationSet = new HashSet();
				if (swoopModel.isUseTableau()) {
				    // call debugging version of Pellet instead
					List explSOS = this.getTableauSOS(swoopModel.getSelectedOntology(), clazz);
	//				String explanation = explSOS.get(0).toString();
					// get explanation set from pellet
					explanationSet = (HashSet) explSOS.get(1);
					explStr.add(explSOS.get(0).toString());
				}	
				else {
					// use black box method instead
					explanationSet = this.getBlackBoxSOS(swoopModel.getSelectedOntology(), clazz);					
				}
				MUPS.add(explanationSet);				
				
				// find all MUPS using Hitting Set Trees approach
				if (swoopModel.isFindAllMUPS()) HSTMUPS(explanationSet, swoopModel.getSelectedOntology(), MUPS, explStr, new HashSet(), new HashSet());
				
				mupsTimer.stop();
				System.out.println("------ MUPS PROCEDURE COMPLETE -------");
				if (swoopModel.findAllMUPS) {
					System.out.println(mupsTimer);
					System.out.println("parse time: "+parseTime);
					System.out.println("net time: "+ String.valueOf(mupsTimer.getTotal() - parseTime));
					netTime = mupsTimer.getTotal() - parseTime;
					mupsCount = MUPS.size();
				}
				Object[] explObj = explStr.toArray();
    			int o = 0;
    			HashMap explMap = new HashMap();
    			Set mupsSet = new HashSet();
    			for (Iterator iter = MUPS.iterator(); iter.hasNext();) {
    				Set explSet = (HashSet) iter.next();
    				mupsSet.add(explSet);
    				if (o<explObj.length && explObj[o]!=null) {
    					String expl = explObj[o].toString();
    					explMap.put(explSet, expl);
    				}
    				o++;
    			}
				
				for (Iterator iter = mupsSet.iterator(); iter.hasNext();) {
					Set m = (HashSet) iter.next();
					print("<FONT FACE=\""+swoopModel.getFontFace()+"\" SIZE="+fontSize+">");
					if (explMap.containsKey(m)) println(explMap.get(m).toString());
					this.printExplanationSOS(m, clazz, false);					
				}
			
			}
		}
		//***
//	  rules stuff
		if (swoopModel.getEnableRules()) {
			RulesExpressivity ruleExpr = swoopModel.getRuleExpr();
			Set rules = ruleExpr.getRules(clazz);
			String ruleTitle = "<b>Rules:</b>";
			if (editorEnabled)
				ruleTitle += addTitle("RULE");
			pw.println( "\n" + ruleTitle + " \n<br>" );
			for (Iterator iter = rules.iterator(); iter.hasNext();) {
			    OWLRule rule = ((RuleValue) iter.next()).getRule();
				String ruleDesc = rule.toString();
				int ptr;
				ptr = ruleDesc.indexOf(">")+1;
			    String consequent = ruleDesc.substring(ptr,ruleDesc.indexOf("."));
			    String varConsequent = consequent.substring((consequent.indexOf("(")+1),consequent.indexOf(")"));
			    ruleDesc = ruleDesc.substring(0,ptr-2)+".";
			    ruleDesc = consequent + "  <b>:-</b>  " + ruleDesc;
				ptr = 0;
				ruleDesc = " " + ruleDesc;
				String hyperRule = ruleDesc;
				while ((ptr = ruleDesc.indexOf("(", ptr))>=0) {
					String name = ruleDesc.substring(ruleDesc.lastIndexOf(" ", ptr), ptr);
					String uri = this.getURIForTerm(name.trim());
					hyperRule = hyperRule.replaceAll(name, "<a href=\""+uri+"\">"+name+"</a>");
					ptr++;
				}
				
				if ( editorEnabled ) {
				    pw.print( hyperRule );
				    
					String hash = String.valueOf(rule.hashCode());
					OWLDescHash.put(hash, rule);
					String titleCode = "RULE"; //getCode(title);
					pw.print("&nbsp;&nbsp;(<font color=\"red\"><a href=\"<Delete:" + hash
							+ ":" + titleCode + "\">Delete</a></font>) ");
					pw.print("&nbsp;&nbsp;(<font color=\"red\"><a href=\"<Edit:" + hash
							+ ":" + titleCode + "\">Edit</a></font>)");
					pw.print("&nbsp;&nbsp;(<font color=\"red\"><a href=\"<Publish:" + hash
							+ ":" + titleCode + "\">Publish</a></font>)");
				    
					pw.println( "<br>" );
					
				} else {
				    pw.println( hyperRule + "<br>"); 
				}
			}
		}
		if (showDivisions) {
			print("<table cellpadding=\"5\"><tr bgcolor=\"#FFF68F\"><FONT FACE=\""+swoopModel.getFontFace()+"\" SIZE="+fontSize+">");
		}
		
		boolean printedAnn = renderAnnotations(clazz);
		if (!printedAnn) notPrinted.add("A-ANN");
		else if (showDivisions) print(HR+"<FONT FACE=\""+swoopModel.getFontFace()+"\" SIZE="+fontSize+">");
		
		int notPrintedSize = notPrinted.size();
		
		i = OWLDescriptionFinder.getIntersections(clazz, reasoner.getOntologies()).iterator();
		
		if (!i.hasNext()) {
			// insert added elements
			boolean printed = insertAddedChanges(new HashSet(), clazz, "C-INT", "<b>Intersection of:</b>");
			if (!printed) notPrinted.add("C-INT");
		}
		while(i.hasNext()) {
			OWLAnd intersection = (OWLAnd) i.next();
			String title = "<b>Intersection of:</b>";
			if (editorEnabled) title += addTitle("C-INT");
			
			// check for empty collection
			if (intersection.getOperands().size()==0) {
				// copy from above
				boolean printed = insertAddedChanges(new HashSet(), clazz, "C-INT", "<b>Intersection of:</b>");
				if (!printed) notPrinted.add("C-INT");
			}
			else printCollection(intersection.getOperands(), title);		
		}
		
		i = OWLDescriptionFinder.getUnions(clazz, reasoner.getOntologies()).iterator();
		if (!i.hasNext()) {
			boolean printed = insertAddedChanges(new HashSet(), clazz, "C-UNI", "<b>Union of:</b>");
			if (!printed) notPrinted.add("C-UNI");
		}
		while(i.hasNext()) {
			OWLOr union = (OWLOr) i.next();
			String title = "<b>Union of:</b>";
			if (editorEnabled) title += addTitle("C-UNI");
			
			// check for empty collection
			if (union.getOperands().size()==0) {
				//copy from above
				boolean printed = insertAddedChanges(new HashSet(), clazz, "C-UNI", "<b>Union of:</b>");
				if (!printed) notPrinted.add("C-UNI");
			}
			else printCollection(union.getOperands(), title);		
		}
			
		i = OWLDescriptionFinder.getEnumerations(clazz, reasoner.getOntologies()).iterator();
		if (!i.hasNext()) {
			boolean printed = insertAddedChanges(new HashSet(), clazz, "I-ONE", "<b>One of:</b>");
			if (!printed) notPrinted.add("I-ONE");
		}
		while(i.hasNext()) {
			OWLEnumeration oneOf = (OWLEnumeration) i.next();
			String title = "<b>One of:</b>";
			if (editorEnabled) title += addTitle("I-ONE");
			
			// check for empty collection
			if (oneOf.getIndividuals().size()==0) {
				//copy from above
				boolean printed = insertAddedChanges(new HashSet(), clazz, "I-ONE", "<b>One of:</b>");
				if (!printed) notPrinted.add("I-ONE");
			}
			else printCollection(oneOf.getIndividuals(), title);		
		}
		
		if ((notPrinted.size()-notPrintedSize<3) && (showDivisions)) print(HR+"<FONT FACE=\""+swoopModel.getFontFace()+"\" SIZE="+fontSize+">");
		notPrintedSize = notPrinted.size();
		
		// print equivalent classes
		String equTitle = "<b>Equivalent to:</b>";
		if (editorEnabled) equTitle += addTitle("C-EQU");
		// get defined equivalentclasses
		Set eqs = OWLDescriptionFinder.getEquivalentClasses(clazz, reasoner.getOntologies());
		// remove all complements
		i = eqs.iterator();
		while(i.hasNext()) {
			OWLDescription desc = (OWLDescription) i.next();
			if(!(desc instanceof OWLClass || desc instanceof OWLRestriction))
				i.remove();	
		}
		if(reasoner.isConsistent(clazz)) {
			// add all the named equivalent classes
			eqs.addAll(reasoner.equivalentClassesOf(clazz));
		}
		else
		    eqs.add(owlNothing);
		
		// title is not printed if there was no equivalent class
		if (!eqs.isEmpty()) {
			printCollection(eqs, equTitle);
		}
		else {
			boolean printed = insertAddedChanges(new HashSet(), clazz, "C-EQU", "<b>Equivalent to:</b>");
			if (!printed) notPrinted.add("C-EQU");
		}
		
		// print complements
		String notTitle = "<b>Complement of:</b>";
		if (editorEnabled) notTitle += addTitle("C-NOT");
		// get complements
		Set nots = OWLDescriptionFinder.getComplements(clazz, reasoner.getOntologies());
		if (nots.isEmpty()) {
			boolean printed = insertAddedChanges(new HashSet(), clazz, "C-NOT", "<b>Complement of:</b>");
			if (!printed) notPrinted.add("C-NOT");
		}
		else {
//			if(reasoner.isConsistent(clazz)) {
//				// add all the named equivalent classes
//				nots.addAll(reasoner.equivalentClassesOf(clazz));
//			}
//			else
//			    nots.add(owlNothing);
			printCollection(nots, notTitle);
		}					
		
		// print named disjoints
		String disTitle = "<b>Disjoint with:</b>";
		Set disjoints = OWLDescriptionFinder.getDisjoints(clazz, reasoner.getOntologies());
		if (disjoints.size()==0) {
			boolean printed = insertAddedChanges(new HashSet(), clazz, "C-DIS", disTitle);
			if (!printed) notPrinted.add("C-DIS");
		}
		else {
			if (editorEnabled) disTitle += addTitle("C-DIS");	
			printCollection(disjoints, disTitle);
		}
		
		// get defined superclasses
		Set supers = OWLDescriptionFinder.getSuperClasses(clazz, reasoner.getOntologies());
		
		if(reasoner.isConsistent(clazz)) {
			// remove all the named classes because reasoner will eventually add them
			i = supers.iterator();
			while(i.hasNext())
				if(i.next() instanceof OWLClass)
					i.remove();		
			// add all the named superclasses
			supers.addAll(reasoner.superClassesOf(clazz));
			// remove owl:Thing from the superclass set
			i = supers.iterator();
			while(i.hasNext()) {
				Object o = i.next();
				if(o instanceof Set && ((Set)o).contains(owlThing))
					i.remove();	
			}
		}
		String subTitle = "<b>Subclass of:</b>";
		if(supers.size() == 0) {
			boolean printed = insertAddedChanges(new HashSet(), clazz, "C-SUB", "<b>Subclass of:</b>");
			if (!printed) notPrinted.add("C-SUB");
		}
		else {
			if (editorEnabled) subTitle += addTitle("C-SUB");	
			printCollection(supers, subTitle);			
		}
			
		// print named subclasses
		// and asserted complex subclasses
		String supTitle = "<b>Superclass of:</b>";
		if (editorEnabled) supTitle += addTitle("C-SUP");					
		Set subs = reasoner.subClassesOf(clazz);
		subs = SetUtils.union(subs);
		subs.addAll(OWLDescriptionFinder.getSubClasses(clazz, reasoner.getOntologies()));
		subs.remove(owlNothing);
		subs.removeAll(reasoner.equivalentClassesOf(owlNothing));
		
		if(subs.size() >= 1) 
			printCollection(subs, supTitle);
		else {
			boolean printed = insertAddedChanges(new HashSet(), clazz, "C-SUP", "<b>Superclass of:</b>");
			if (!printed) notPrinted.add("C-SUP");
		}
		
		if ((notPrinted.size()-notPrintedSize<5) && (showDivisions)) print(HR+"<FONT FACE=\""+swoopModel.getFontFace()+"\" SIZE="+fontSize+">");
		notPrintedSize = notPrinted.size();
		
		// start printing properties whose domain/range is this class
		String domTitle = "<b>Domain of:</b>";
		if (editorEnabled) domTitle += addTitle("P-DOM");
		if (getPropertiesWithDomain(clazz, showInherited).size()==0) {
			boolean printed = insertAddedChanges(new HashSet(), clazz, "P-DOM", "<b>Domain of:</b>");
			if (!printed) notPrinted.add("P-DOM");
		}
		else printCollection(getPropertiesWithDomain(clazz, showInherited), domTitle);
		
		String ranTitle = "<b>Range of:</b>";
		if (editorEnabled) ranTitle += addTitle("P-RAN");
		if (getPropertiesWithRange(clazz, showInherited).size()==0) {
			boolean printed = insertAddedChanges(new HashSet(), clazz, "P-RAN", "<b>Range of:</b>");
			if (!printed) notPrinted.add("P-RAN");
		}
		else printCollection(getPropertiesWithRange(clazz, showInherited), ranTitle);
		
		if ((notPrinted.size()-notPrintedSize<2) && (showDivisions)) print(HR+"<FONT FACE=\""+swoopModel.getFontFace()+"\" SIZE="+fontSize+">");
		notPrintedSize = notPrinted.size();
		
		Set instances = new HashSet();
		if (showInherited) instances = reasoner.allInstancesOf(clazz);
		else instances = reasoner.instancesOf(clazz);
		String insTitle = "<b>Instances:</b>";
		if (editorEnabled) insTitle += addTitle("I-INS");
		if (instances.size()==0) {
			boolean printed = insertAddedChanges(new HashSet(), clazz, "I-INS", "<b>Instances:</b>");
			if (!printed) notPrinted.add("I-INS");
		}
		else printCollection(instances, insTitle);
		
		if ((notPrinted.size()-notPrintedSize<1) && (showDivisions)) print(HR+"<FONT FACE=\""+swoopModel.getFontFace()+"\" SIZE="+fontSize+">");
		notPrintedSize = notPrinted.size();
		
		if (showDivisions && !editorEnabled) {
			print("</tr>");
		}
		
		// add unprinted titles at the bottom
		addUnprinted(notPrinted);
		
	}

	/**
	 * Add all the unprinted parameters at the end of the CF pane if editor is enabled
	 * @param notPrinted
	 */
	protected void addUnprinted(List notPrinted) {
		
		if (editorEnabled) {
			Iterator npIter = notPrinted.iterator();
			boolean div1 = false, div2 = false, div3 = false, div4 = false, div5 = false, div6 = false, div7 = false;
			while (npIter.hasNext()) {
				
				String typeCode = npIter.next().toString();
				
				if (typeCode.equals("A-ANN")) {
					println();
					println(" <b>Annotations</b>:"+addTitle(typeCode));
					if (showDivisions) print(HR+"<FONT FACE=\""+swoopModel.getFontFace()+"\" SIZE="+fontSize+">");
				}
				
				if (typeCode.equals("C-INT")) {
					div1 = true;
					println();
					println(" <b>Intersection of:</b>"+addTitle(typeCode));
				}
				else if (typeCode.equals("C-UNI")) {
					div1 = true;
					println();
					println(" <b>Union of:</b>"+addTitle(typeCode));
				}
				else if (typeCode.equals("I-ONE")) {
					div1 = true;
					println();
					println(" <b>One of:</b>"+addTitle(typeCode));
				}
				else if (showDivisions && div1) {
					print(HR+"<FONT FACE=\""+swoopModel.getFontFace()+"\" SIZE="+fontSize+">");
					div1 = false;
				}
				
				if (typeCode.equals("C-EQU")) {
					div2 = true;
					println();
					println(" <b>Equivalent to:</b>"+addTitle(typeCode));
				}
				else if (typeCode.equals("C-NOT")) {
					div2 = true;
					println();
					println(" <b>Complement of:</b>"+addTitle(typeCode));
				}
				else if (typeCode.equals("C-DIS")) {
					div2 = true;
					println();
					println(" <b>Disjoint with:</b>"+addTitle(typeCode));
				}
				else if (typeCode.equals("C-SUB")) {
					div2 = true;
					println();
					println(" <b>Subclass of:</b>"+addTitle(typeCode));
				}
				else if (typeCode.equals("C-SUP")) {
					div2 = true;
					println();
					println(" <b>Superclass of:</b>"+addTitle(typeCode));
				}
				else if (showDivisions && div2) {
					print(HR+"<FONT FACE=\""+swoopModel.getFontFace()+"\" SIZE="+fontSize+">");
					div2 = false;
				}
				
				if (typeCode.equals("P-DOM")) {
					div3 = true;
					println();
					println(" <b>Domain of:</b>"+addTitle(typeCode));
				}
				else if (typeCode.equals("P-RAN")) {
					div3 = true;
					println();
					println(" <b>Range of:</b>"+addTitle(typeCode));
				}
				else if (showDivisions && div3) {
					print(HR+"<FONT FACE=\""+swoopModel.getFontFace()+"\" SIZE="+fontSize+">");
					div3 = false;
				}
				
				if (typeCode.equals("I-INS")) {
					println();
					println(" <b>Instances:</b>"+addTitle(typeCode));
				}
				
				// render unassigned property axiom divisions
				
				if (typeCode.equals("P-EQU")) {
					div4 = true;
					println();
					println(" <b>Equivalent to:</b>"+addTitle(typeCode));
				}
				else if (typeCode.equals("P-INV")) {
					div4 = true;
					println();
					println(" <b>Inverse of:</b>"+addTitle(typeCode));
				}
				else if (typeCode.equals("P-SUB")) {
					div4 = true;
					println();
					println(" <b>Subproperty of:</b>"+addTitle(typeCode));
				}
				else if (typeCode.equals("P-SUP")) {
					div4 = true;
					println();
					println(" <b>Superproperty of:</b>"+addTitle(typeCode));
				}
				else if (showDivisions && div4) {
					print(HR+"<FONT FACE=\""+swoopModel.getFontFace()+"\" SIZE="+fontSize+">");
					div4 = false;
				}
				
				if (typeCode.equals("C-HASDOM")) {
					div5 = true;
					println();
					println(" <b>Has Domain:</b>"+addTitle(typeCode));
				}
				else if (typeCode.equals("C-HASRAN")) {
					div5 = true;
					println();
					println(" <b>Has Range:</b>"+addTitle(typeCode));
				}
				else if (showDivisions && div5) {
					print(HR+"<FONT FACE=\""+swoopModel.getFontFace()+"\" SIZE="+fontSize+">");
					div5 = false;
				}
				
				if (typeCode.equals("ATTRIB")) {
					println();
					println(" <b>Attributes:</b>"+addTitle(typeCode));
				}
				
				// render individual related information 
				
				if (typeCode.equals("C-TYP")) {
					println();
					println("<b>Instance of:</b>"+addTitle(typeCode));
					div6 = true;
				}
				else if (typeCode.equals("I-SAM")) {
					println();
					println("<b>Same As:</b>"+addTitle(typeCode));
					div6 = true;
				}
				else if (typeCode.equals("I-DIF")) {
					println();
					println("<b>Different From:</b>"+addTitle(typeCode));
					div6 = true;
				}
				else if (showDivisions && div6) {
					print(HR+"<FONT FACE=\""+swoopModel.getFontFace()+"\" SIZE="+fontSize+">");
					div6 = false;
				}
				
				if (typeCode.equals("P-INSD")) {
					println();
					println("<b>Datatype Assertions:</b>"+addTitle(typeCode));
					div7 = true;
				}
				if (typeCode.equals("P-INSO")) {
					println();
					println("<b>Object Assertions:</b>"+addTitle(typeCode));
					div7 = true;
				}
			}
		}
	}
	
	protected String addTitle(String param) {
		String title = "&nbsp;&nbsp;(<font color=\"red\"><a href=\"<Add"+param+"\">Add</a></font>)";
		return title;
	}
	
	protected String getCode(String title) {
		String titleCode = title;
		if (editorEnabled) {
			// remove hyperlink stuff and return code
			if(title.indexOf("red")>=0) {		
				int spos = title.indexOf("<a href=\"<")+10;
				int epos = title.indexOf("\"", spos+1);
				titleCode = title.substring(spos, epos);
				titleCode = titleCode.substring(3, titleCode.length()); // remove 'Add..'
			}
		}
		else {
			// return code directly
			if (title.equals("<b>Intersection of:</b>")) titleCode = "C-INT";
			else if (title.equals("<b>Union of:</b>")) titleCode = "C-UNI";
			else if (title.equals("<b>One of:</b>")) titleCode = "I-ONE";
			else if (title.equals("<b>Equivalent to:</b>")) {
				if (displayedEntity instanceof OWLClass) titleCode = "C-EQU";
				else titleCode = "P-EQU";
			}
			else if (title.equals("<b>Disjoint with:</b>")) titleCode = "C-DIS";
			else if (title.equals("<b>Complement of:</b>")) titleCode = "C-NOT";
			else if (title.equals("<b>Subclass of:</b>")) titleCode = "C-SUB";
			else if (title.equals("<b>Superclass of:</b>")) titleCode = "C-SUP";
			else if (title.equals("<b>Domain of:</b>")) titleCode = "P-DOM";
			else if (title.equals("<b>Range of:</b>")) titleCode = "P-RAN";
			else if (title.equals("<b>Instances:</b>")) titleCode = "I-INS";
			else if (title.equals("<b>Instance of:</b>")) titleCode = "C-TYP";
			else if (title.equals("<b>Inverse of:</b>")) titleCode = "P-INV";
			else if (title.equals("<b>Subproperty of:</b>")) titleCode = "P-SUB";
			else if (title.equals("<b>Superproperty of:</b>")) titleCode = "P-SUP";
			else if (title.equals("<b>Has domain:</b>")) titleCode = "C-HASDOM";
			else if (title.equals("<b>Has range:</b>")) titleCode = "C-HASRAN";
			
		}
		return titleCode;
	}
	
	protected void addWhy(Object obj, String title) {		
		String hash = String.valueOf(obj.hashCode());
		OWLDescHash.put(hash, obj);
		String titleCode = getCode(title);
		if (!imported) {
			print("&nbsp;&nbsp;(<font color=\"red\"><a href=\"<Why:"+hash+":"+titleCode+"\">Why?</a></font>)");
		}		
	}
	
	protected void addDelete(Object obj, String title) {		
		String hash = String.valueOf(obj.hashCode());
		OWLDescHash.put(hash, obj);
		String titleCode = getCode(title);
		if (!imported) {
			print("&nbsp;&nbsp;(<font color=\"red\"><a href=\"<Delete:"+hash+":"+titleCode+"\">Delete</a></font>)");
		}		
	}
	
	protected void addValueLink(OWLProperty prop) {
		String hash = String.valueOf(prop.hashCode());
		OWLDescHash.put(hash, prop);
		print("&nbsp;&nbsp;(<font color=\"red\"><a href=\"<AddV:"+hash+"\">Add Value</a></font>)");
	}
	
	protected void deleteValueLink(OWLProperty prop, OWLDataValue dtv) {
		String hash1 = String.valueOf(prop.hashCode());
		String hash2 = String.valueOf(dtv.hashCode());
		OWLDescHash.put(hash1, prop);
		OWLDescHash.put(hash2, dtv);
		print("&nbsp;&nbsp;(<font color=\"red\"><a href=\"<Delete:"+hash1+":P-VAL:"+hash2+"\">Delete</a></font>)");
	}
	
	protected void deleteValueLink(OWLProperty prop, OWLIndividual ind) {
		String hash1 = String.valueOf(prop.hashCode());
		String hash2 = String.valueOf(ind.hashCode());
		OWLDescHash.put(hash1, prop);
		OWLDescHash.put(hash2, ind);
		print("&nbsp;&nbsp;(<font color=\"red\"><a href=\"<Delete:"+hash1+":P-VAL:"+hash2+"\">Delete</a></font>)");
	}
	
	protected void deleteTypeLink(OWLDescription cla) {
		String hash = String.valueOf(cla.hashCode());
		OWLDescHash.put(hash, cla);
		print("&nbsp;&nbsp;(<font color=\"red\"><a href=\"<Delete:"+hash+":I-INS\">Delete</a></font>)");
	}
	
	protected void addUndo(Object obj, String type, String title) {		
		String hash = String.valueOf(obj.hashCode());
		OWLDescHash.put(hash, obj);
		String titleCode = getCode(title);
		print("&nbsp;&nbsp;(<font color=\"red\"><a href=\"<Undo:"+type+":"+hash+":"+titleCode+"\">Undo</a></font>)");
	}
	
	protected String isImage(String url) {
		
		Set imageExtSet = new HashSet();
		imageExtSet.add("jpg");
		imageExtSet.add("gif");
		
		if (url.indexOf("<a href=")>=0) {
			int spos = url.indexOf("<a href=\"<")+10;
			int epos = url.indexOf("\"", spos+1);
			url = url.substring(spos, epos);
			String ext = url.substring(url.lastIndexOf(".")+1, url.length());
			if (imageExtSet.contains(ext)) {
				return url;
			}
		}
		return "";
	}
	
	protected void renderIndividual(OWLIndividual ind) throws OWLException {
		
		// check if individual is in current ontology or imported
		String importedLbl = "";
		imported = false;
		List unprinted = new ArrayList();
		
		if (!ind.getOntologies().contains(swoopModel.getSelectedOntology())) {
			importedLbl = "Imported ";
			imported = true;
		}
		
		OWLClass owlThing = reasoner.getOntology().getOWLDataFactory().getOWLThing();
		
		print("<b>"+importedLbl+"OWL-Individual:</b>&nbsp;");
		if (ind.isAnonymous())
			println("Anonymous ");
		else
			println(shortForm(ind.getURI()) + "  ");
		
		if (importedLbl.length()>0) {			
			println();
			// print("<b>Imported from Ontology(s):</b>&nbsp;");
			// also print where the class is imported from
			Iterator ontIter = ind.getOntologies().iterator();
			while (ontIter.hasNext()) {
				OWLOntology ont = (OWLOntology) ontIter.next();
				String ontURI = ont.getURI().toString();
				String ontName = ontURI.substring(ontURI.lastIndexOf("/")+1, ontURI.length());
				String indXPtrURI = ind.getURI() + XPointers.asDefinedIn + "(" + ontURI + ")";
				print("See its definition in Imported Ontology: <a href=\""+indXPtrURI+"\">" + ontName + "</a>");
				// print("<a href=\""+ontURI+"asDefinedIn("+ontURI+")\">"+className+"</a>&nbsp;&nbsp;");
				println();
			}
			println();
		}
		println();
		
		if (showDivisions) {
			print("<table cellpadding=\"5\"><tr bgcolor=\"#FFF68F\"><FONT FACE=\""+swoopModel.getFontFace()+"\" SIZE="+fontSize+">");
		}
		
		boolean printedAnn = renderAnnotations(ind);
		if (!printedAnn) unprinted.add("A-ANN");
		else // print the first division here (after annotations) 
			if (showDivisions) print(HR+"<FONT FACE=\""+swoopModel.getFontFace()+"\" SIZE="+fontSize+">");
		
		boolean div2 = false; 
		// get defined types
		Set types = OWLDescriptionFinder.getTypes(ind, reasoner.getOntologies());
		if(reasoner.isConsistent()) {
			// remove all the named classes because reasoner will eventually add them
			Iterator i = types.iterator();
			while(i.hasNext())
				if(i.next() instanceof OWLClass)
					i.remove();
//			types.clear(); // *** turns out reasoner returns asserted types as well
			// add all the named superclasses
			types.addAll(reasoner.typesOf(ind));
		}
		// remove owl:Thing from the superclass set
		Iterator i = types.iterator();
		while(i.hasNext()) {
			Object o = i.next();
			if(o instanceof Set && ((Set)o).contains(owlThing))
				i.remove();	
		}
		
		boolean printedType = false;
		String title = "<b>Instance of:</b>";
		if(types.size() == 0) {
			printedType = insertAddedChanges(new HashSet(), ind, "C-TYP", "<b>Instance of:</b>");			
		}
		else {
			if (editorEnabled) title += addTitle("C-TYP");	
			printCollection(types, title);
			printedType = true;
		}
		if (!printedType) unprinted.add("C-TYP");
		else div2 = true;
		
		// print sameAs individuals
		title = "<b>Same As:</b>";
		boolean printedSame = false;
		if (reasoner.isConsistent() && reasoner.getSameAsIndividuals(ind)!=null && reasoner.getSameAsIndividuals(ind).size()>0) {			
			Set sameAs = reasoner.getSameAsIndividuals(ind);
			if (editorEnabled) title += addTitle("I-SAM");
			printCollection(sameAs, title);
			printedSame = true;
		}
		else {
			printedSame = insertAddedChanges(new HashSet(), ind, "I-SAM", "<b>Same As:</b>");							    
		}
		if (!printedSame) unprinted.add("I-SAM");
		else div2 = true;
		
		// print diffFrom individuals
		boolean printedDiff = false;
		title = "<b>Different From:</b>";
		if (reasoner.isConsistent() && reasoner.getDifferentFromIndividuals(ind)!=null && reasoner.getDifferentFromIndividuals(ind).size()>0) {
			Set diffFrom = reasoner.getDifferentFromIndividuals(ind);
			if (editorEnabled) title += addTitle("I-DIF");
			printCollection(diffFrom, title);
			printedDiff = true;
		}
		else {
			printedDiff = insertAddedChanges(new HashSet(), ind, "I-DIF", "<b>Different From:</b>");				
		}
		if (!printedDiff) unprinted.add("I-DIF");
		else div2 = true;
		
		// print second division here (after types, sameAs, diffFrom)
		if (div2) print(HR+"<FONT FACE=\""+swoopModel.getFontFace()+"\" SIZE="+fontSize+">"); 
		
		// get properties whose domain is the type of the individual
		HashMap propMap = new HashMap();
		for (Iterator typeIter = types.iterator(); typeIter.hasNext();) {
			Object typ = typeIter.next();
			if (typ instanceof Set) {
				Set equClas = (HashSet) typ; // set of equivalent class types
				for (Iterator ite=equClas.iterator(); ite.hasNext();) {
					OWLDescription type = (OWLDescription) ite.next();
					if (!(type instanceof OWLClass)) continue;
					propMap.put(type, getPropertiesWithDomain((OWLClass) type, false));
					if (showInherited) {
						Set ancSet = reasoner.ancestorClassesOf(type);
						for (Iterator supCIter = ancSet.iterator(); supCIter.hasNext();) {
							Set supCSet = (HashSet) supCIter.next();
							OWLDescription supC = (OWLDescription) supCSet.iterator().next();
							if (supC instanceof OWLClass) {
								propMap.put(supC, getPropertiesWithDomain((OWLClass) supC, false));
							}
						}
					}
				}
			}
		}
		
		boolean div3 = false;
		// print all datatype properties and value pairs of instance
		Set instanceProps = new HashSet();
		Map dataValues = new HashMap();
		if (reasoner.isConsistent()) dataValues = reasoner.getDataPropertyValues(ind);
		boolean printedDataAssTitle = false;
		if(dataValues.size() > 0) {
			
			for (Iterator it = dataValues.keySet().iterator(); it.hasNext();) {
				OWLDataProperty prop = (OWLDataProperty) it.next();
				
				boolean valLinkPrinted = false;
				Set vals = (Set) dataValues.get(prop);
				
				if (vals.size()>0) {
					if (!printedDataAssTitle) {
						// just print title once
						println();
						print("<b>Datatype Assertions:</b>");
						if (editorEnabled) print(addTitle("P-INSD"));
						println();
						printedDataAssTitle = true;
					}
					instanceProps.add(prop);
				}
				
				for (Iterator valIt = vals.iterator(); valIt.hasNext();) {
					OWLDataValue dtv = (OWLDataValue) valIt.next();
					
					// check if DataPropertyInstance is deleted
					OntologyChange oc = editorEnabled ?	isDeleted(prop, dtv) : null;
					boolean deleted = (oc != null);
					if(deleted) print("<font color=\"red\"><strike>");
					
					visitor.reset();
					dtv.accept(visitor);
					if (!isExplicit(ind, "P-INSD", prop, dtv)) print("<i>");
					printConciseObject(prop);
					print(" : " + visitor.result());
					if (!isExplicit(ind, "P-INSD", prop, dtv)) print("</i>");
					
					if(deleted) {
						print("</strike></font>");
						addUndo(oc, "Delete", "P-VAL");
					}					
					else if (editorEnabled && isExplicit(ind, "P-INSD", prop, dtv)) deleteValueLink(prop, dtv);
					
					if ((!valLinkPrinted) && (editorEnabled)) {
						addValueLink(prop);
						valLinkPrinted = true; // only display 'Add Value' link once for each property
					}
					if (valIt.hasNext()) {
						println();
					}
				}
				if (printedDataAssTitle) println();
			}
		}

		// print all AddDataPropertyInstance changes associated with this instance in green
		Iterator citer = changes.iterator();
		while(citer.hasNext()) {
			OntologyChange oc = (OntologyChange) citer.next();
			if (oc instanceof AddDataPropertyInstance) {
				AddDataPropertyInstance change = (AddDataPropertyInstance) oc;
				if (change.getSubject().equals(ind)) {
					
					if (!printedDataAssTitle) {
						println();
						print("<b>Datatype Assertions:</b>");
						if (editorEnabled) print(addTitle("P-INSD"));
						println();
						printedDataAssTitle = true;
					}
					
					OWLDataProperty prop = change.getProperty();
					instanceProps.add(prop);
					OWLDataValue dtv = change.getObject();
					visitor.reset();
					dtv.accept(visitor);
					print("<font color=\"green\">");
					printConciseObject(prop);
					print(" : " + visitor.result());
					addUndo(change, "Add", "V");
					print("</font>");
					println();
				}
			}
		}
		if (!printedDataAssTitle) unprinted.add("P-INSD");
		else div3 = true;
		
		// print all object properties and value pairs of instance
		Map propertyValues = new HashMap();
		if (reasoner.isConsistent()) propertyValues = reasoner.getObjectPropertyValues(ind);
		boolean printedObjAssTitle = false;
		if(propertyValues.size() > 0) {
			
			for (Iterator it = propertyValues.keySet().iterator(); it.hasNext();) {
				OWLObjectProperty prop = (OWLObjectProperty) it.next();
				
				boolean valLinkPrinted = false;
				Set vals = (Set) propertyValues.get(prop);
				
				if (vals.size()>0) {
					if (!printedObjAssTitle) {
						// only print title once
						println();
						print("<b>Object Assertions:</b>");
						if (editorEnabled) print(addTitle("P-INSO"));
						println();
						printedObjAssTitle = true;
					}
					instanceProps.add(prop);
				}
				
				for (Iterator valIt = vals.iterator(); valIt.hasNext();) {
					OWLIndividual oi = (OWLIndividual) valIt.next();
					
					// check if ObjectPropertyInstance is deleted
					OntologyChange oc = editorEnabled ?	isDeleted(prop, oi) : null;
					boolean deleted = (oc != null);
					if(deleted) print("<font color=\"red\"><strike>");
					
					if (!isExplicit(ind, "P-INSO", prop, oi)) print("<i>");
					printConciseObject(prop);
					
					visitor.reset();
					boolean turnOnIcons = false;
					// turn off icons while rendering individuals 
					// since we are rendering image values inline
					if (swoopModel.getShowIcons()) {
						turnOnIcons = true;
						swoopModel.setShowIcons(false, false);
					}
					oi.accept(visitor);
					String objValue = visitor.result();
					
					if (isImage(objValue).equals("")) print(" : " + visitor.result());
					else {
						// render image inline
						print(" : ");
						println("<P align=\"left\"><b><img border=\"3\" src=\""+ isImage(objValue)+ "\" width=\"100\" height=\"100\"></b></P>");
					}
					// check for turnOnIcons
					if (turnOnIcons) swoopModel.setShowIcons(true, false);
					
					if (!isExplicit(ind, "P-INSO", prop, oi)) {
						print("</i>");
						if (reasoner.supportsExplanation()) {
							// add why link manually
							List propVal = new ArrayList();
							propVal.add(prop);
							propVal.add(oi);
							String hash = String.valueOf(propVal.hashCode());
							OWLDescHash.put(hash, propVal);
							if (!imported) {
								print("&nbsp;&nbsp;(<font color=\"red\"><a href=\"<Why:"+hash+":"+"P-INSO"+"\">Why?</a></font>)");
							}
						}
					}
					
					if (deleted) {
						print("</strike></font>");
						addUndo(oc, "Delete", "P-VAL");
					}
					else if (editorEnabled && isExplicit(ind, "P-INSO", prop, oi)) deleteValueLink(prop, oi);
					
					if ((!valLinkPrinted) && (editorEnabled)) {
						addValueLink(prop);
						valLinkPrinted = true; // only display 'Add Value' link once for each property
					}
					if (valIt.hasNext()) {
						println();
					}
				}
				if (printedObjAssTitle) println();
			}
		}
		
		// print all AddObjectPropertyInstance changes associated with this instance in green
		citer = changes.iterator();
		while(citer.hasNext()) {
			OntologyChange oc = (OntologyChange) citer.next();
			if (oc instanceof AddObjectPropertyInstance) {
				AddObjectPropertyInstance change = (AddObjectPropertyInstance) oc;
				if (change.getSubject().equals(ind)) {
					
					if (!printedObjAssTitle) {
						println();
						print("<b>Object Assertions:</b>");
						if (editorEnabled) print(addTitle("P-INSO"));
						println();
						printedObjAssTitle = true;
					}
					
					OWLObjectProperty prop = change.getProperty();
					instanceProps.add(prop);
					OWLIndividual objInst = change.getObject();
					print("<font color=\"green\">");
					printConciseObject(prop);
					print(" : ");
					printConciseObject(objInst);
					addUndo(change, "Add", "V");
					print("</font>");
					println();
				}
			}
		}
		if (!printedObjAssTitle) unprinted.add("P-INSO");
		else div3 = true;
		
		if (div3) {
			if (showDivisions) print(HR+"<FONT FACE=\""+swoopModel.getFontFace()+"\" SIZE="+swoopModel.getFontSize()+">");
		}
		
		this.addUnprinted(unprinted);
		
		if (editorEnabled) {
			 if (propMap.size()>0) {
				if (showDivisions) print(HR+"<FONT FACE=\""+swoopModel.getFontFace()+"\" SIZE="+fontSize+">");
				println();
				println("<b>Likely Properties to Use:</b>");
				for (Iterator iter = propMap.keySet().iterator(); iter.hasNext();)	{				
					OWLClass cla = (OWLClass) iter.next();
					Set props = (HashSet) propMap.get(cla);
					for (Iterator pit = props.iterator(); pit.hasNext();) {
						OWLProperty prop = (OWLProperty) pit.next();
						if (!instanceProps.contains(prop)) {
							print("(of ");
							printConciseObject(cla);
							print(") ");
							
							printConciseObject(prop);
							print(" : ");
							addValueLink(prop);
							println();
						}
					}
				}
			 }
		}		
	}

	protected void renderAnnotationProperty(OWLAnnotationProperty prop)	throws OWLException {
		println("<b>OWL-AnnotationProperty:</b>&nbsp;" + shortForm(prop.getURI()) + "  ");
	}

	protected void renderObjectProperty(OWLObjectProperty prop)	throws OWLException {
		
		// check if property is in current ontology or imported
		String importedLbl = "";
		imported = false;
		
		if (!prop.getOntologies().contains(swoopModel.getSelectedOntology())) {
			importedLbl = "Imported ";
			imported = true;
		}
		
		List notPrinted = new ArrayList();
		Iterator it = null;
//		print("<b>"+importedLbl+"OWL-ObjectProperty:</b>&nbsp;");
//		printObject(prop);
//		
//		*********************************************
		//Added for Econnections
		//**********************************************
		if(!prop.isLink()){
			print("<b>"+importedLbl+"OWL-ObjectProperty:</b>&nbsp;");
			printObject(prop);}
		else{
			print("<b>"+importedLbl+"OWL-LinkProperty:</b>&nbsp;");
			printObject(prop);
			print("<br><b>"+"ForeignOntology:</b> &nbsp;");
			URI linkURI = prop.getLinkTarget();
		    print( "<a href=\""+ linkURI.toString() + "\">" + swoopModel.shortForm(linkURI) + "</a>");
		 }
		//***********************************************
		
		if (importedLbl.length()>0) {			
			println();
			// print("<b>Imported from Ontology(s):</b>&nbsp;");
			// also print where the class is imported from
			Iterator ontIter = prop.getOntologies().iterator();
			while (ontIter.hasNext()) {
				OWLOntology ont = (OWLOntology) ontIter.next();
				String ontURI = ont.getURI().toString();
				String ontName = ontURI.substring(ontURI.lastIndexOf("/")+1, ontURI.length());
				String propXPtrURI = prop.getURI() + XPointers.asDefinedIn + "(" + ontURI + ")";
				print("See its definition in Imported Ontology: <a href=\""+propXPtrURI+"\">" + ontName + "</a>");
				// print("<a href=\""+ontURI+"asDefinedIn("+ontURI+")\">"+className+"</a>&nbsp;&nbsp;");
				println();
			}
			println();
		}
		else if (editorEnabled) print("&nbsp;&nbsp;&nbsp;<font color=\"red\">(<a href=\"<SwitchProp\">Switch</a>)</font>");
		
		if (showDivisions) print("<table cellpadding=\"5\"><tr bgcolor=\"#FFF68F\"><FONT FACE=\""+swoopModel.getFontFace()+"\" SIZE="+fontSize+">");
//		**** rules stuff for object properties****
		if (swoopModel.getEnableRules()) {
			RulesExpressivity ruleExpr = swoopModel.getRuleExpr();
			System.out.println("number of rules:" + ruleExpr.getNumRules());			
			Set rules = ruleExpr.getRules(prop);
			//pw.println("<b> Rules: </b> <br>");
			
			String ruleTitle = "<b> Rules: </b>";
			if (editorEnabled)
				ruleTitle += addTitle("RULE");
			pw.println( ruleTitle + " \n<br>" );
			
			for (Iterator iter = rules.iterator(); iter.hasNext();) {

			    //OWLRule rule = (OWLRule) iter.next();
			    RuleValue rv = (RuleValue) iter.next();
			    OWLRule rule = rv.getRule();
				String ruleDesc = rule.toString();

				int ptr;
				ptr = ruleDesc.indexOf(">")+1;
			    String consequent = ruleDesc.substring(ptr,ruleDesc.indexOf("."));
			    String varConsequent = consequent.substring((consequent.indexOf("(")+1),consequent.indexOf(")"));
			    ruleDesc = ruleDesc.substring(0,ptr-2)+".";
			    ruleDesc = consequent + "  <b>:-</b>  " + ruleDesc;
				ptr = 0;
				ruleDesc = " " + ruleDesc;
				String hyperRule = ruleDesc;
				while ((ptr = ruleDesc.indexOf("(", ptr))>=0) {
					String name = ruleDesc.substring(ruleDesc.lastIndexOf(" ", ptr), ptr);
					String uri = this.getURIForTerm(name.trim());
					hyperRule = hyperRule.replaceAll(name, "<a href=\""+uri+"\">"+name+"</a>");
					ptr++;
				}
				
				if ( editorEnabled ) {
				    pw.print( hyperRule );
				    
					String hash = String.valueOf(rule.hashCode());
					OWLDescHash.put(hash, rule);
					String titleCode = "RULE"; //getCode(title);
					pw.print("&nbsp;&nbsp;(<font color=\"red\"><a href=\"<Delete:" + hash
							+ ":" + titleCode + "\">Delete</a></font>) ");
					pw.print("&nbsp;&nbsp;(<font color=\"red\"><a href=\"<Edit:" + hash
							+ ":" + titleCode + "\">Edit</a></font>)");
					pw.print("&nbsp;&nbsp;(<font color=\"red\"><a href=\"<Publish:" + hash
							+ ":" + titleCode + "\">Publish</a></font>)");
				    
					pw.println( "<br>" );
					
				} else {
				    pw.println( hyperRule + "<br>"); 
				}
			}
		}
		boolean printedAnn = renderAnnotations(prop);
		if (!printedAnn) notPrinted.add("A-ANN");
		else if (showDivisions) print(HR+"<FONT FACE=\""+swoopModel.getFontFace()+"\" SIZE="+fontSize+">");
		
		int notPrintedSize = notPrinted.size();
		
		String title = "<b>Equivalent to:</b>";
		if (reasoner.equivalentPropertiesOf(prop).size()==0) {
			boolean printed = insertAddedChanges(new HashSet(), prop, "P-EQU", title);
			if (!printed) notPrinted.add("P-EQU");
		}
		else {
			if (editorEnabled) title += addTitle("P-EQU");
			printCollection(reasoner.equivalentPropertiesOf(prop), title);
		}
		
		title = "<b>Inverse of:</b>";
		if (reasoner.inversePropertiesOf(prop).size()==0) {
			boolean printed = insertAddedChanges(new HashSet(), prop, "P-INV", title);
			if (!printed) notPrinted.add("P-INV");
		}
		else {
			if (editorEnabled) title += addTitle("P-INV");
			printCollection(reasoner.inversePropertiesOf(prop), title);
		}
		
		title = "<b>Subproperty of:</b>";
		if (reasoner.superPropertiesOf(prop).size()==0) {
			boolean printed = insertAddedChanges(new HashSet(), prop, "P-SUB", title);
			if (!printed) notPrinted.add("P-SUB");
		}
		else {
			if (editorEnabled) title += addTitle("P-SUB");
			printCollection(reasoner.superPropertiesOf(prop), title);
		}
		
		title = "<b>Superproperty of:</b>";
		if (reasoner.subPropertiesOf(prop).size()==0) {
			boolean printed = insertAddedChanges(new HashSet(), prop, "P-SUP", title);
			if (!printed) notPrinted.add("P-SUP");
		}
		else {
			if (editorEnabled) title += addTitle("P-SUP");
			printCollection(reasoner.subPropertiesOf(prop), title);	
		}
		
		if ((notPrinted.size()-notPrintedSize<4) && showDivisions) print(HR+"<FONT FACE=\""+swoopModel.getFontFace()+"\" SIZE="+fontSize+">");
		notPrintedSize = notPrinted.size();
		
		title = "<b>Has domain:</b>";
		Set doms = reasoner.domainsOf(prop);
		doms.addAll(prop.getDomains(swoopModel.getSelectedOntology()));
		if (doms.size()==0) {
			boolean printed = insertAddedChanges(new HashSet(), prop, "C-HASDOM", title);
			if (!printed) notPrinted.add("C-HASDOM");
		}
		else {
			if (editorEnabled) title += addTitle("C-HASDOM");
			printCollection(doms, title);
		}
		
		title = "<b>Has range:</b>";
		Set rans = reasoner.rangesOf(prop);
		rans.addAll(prop.getRanges(swoopModel.getSelectedOntology()));
		if (rans.size()==0) {
			boolean printed = insertAddedChanges(new HashSet(), prop, "C-HASRAN", title);
			if (!printed) notPrinted.add("C-HASRAN");
		}
		else {
			if (editorEnabled) title += addTitle("C-HASRAN");
			//printCollection(reasoner.rangesOf(prop), title);
			printCollection(rans, title);
		}
		
		if ((notPrinted.size()-notPrintedSize<2) && showDivisions) print(HR+"<FONT FACE=\""+swoopModel.getFontFace()+"\" SIZE="+fontSize+">");
		notPrintedSize = notPrinted.size();
		
		String OWL = OWLVocabularyAdapter.OWL;
		title = "<b>Attributes:</b>";
		boolean printedAttribs = false;
		if(prop.isTransitive(reasoner.getOntologies()) || prop.isFunctional(reasoner.getOntologies()) || 
		   prop.isInverseFunctional(reasoner.getOntologies()) || prop.isSymmetric(reasoner.getOntologies())) {

			printedAttribs = true;
			println();
			if (editorEnabled) title += addTitle("ATTRIB");
			println(title);
			
			if (prop.isFunctional(reasoner.getOntologies())) {
				
				OntologyChange isDeleted = isDeleted(prop, "P-FUN"); 
				if (editorEnabled) {
					if (isDeleted!=null) {
						print("<font color=\"red\"><strike><a href=\""+OWL+"FunctionalProperty\">Functional</a></strike></font>");
						addUndo(isDeleted, "Delete", title);						
					}
					else {
						print("<a href=\""+OWL+"FunctionalProperty\">Functional</a>");
						addDelete(prop, "P-FUN");						
					}
				}
				else print("<a href=\""+OWL+"FunctionalProperty\">Functional</a>");
				println();
			}
			
			if (prop.isInverseFunctional(reasoner.getOntologies())) {
				OntologyChange isDeleted = isDeleted(prop, "P-IFUN"); 
				if (editorEnabled) {
					if (isDeleted!=null) {
						print("<font color=\"red\"><strike><a href=\""+OWL+"InverseFunctionalProperty\">Inverse Functional</a></strike></font>");
						addUndo(isDeleted, "Delete", title);						
					}
					else {
						print("<a href=\""+OWL+"InverseFunctionalProperty\">Inverse Functional</a>");
						addDelete(prop, "P-IFUN");						
					}
				}
				else print("<a href=\""+OWL+"InverseFunctionalProperty\">Inverse Functional</a>");
				println();
			}
			
			if (prop.isTransitive(reasoner.getOntologies())) {
				OntologyChange isDeleted = isDeleted(prop, "P-TRA"); 
				if (editorEnabled) {
					if (isDeleted!=null) {
						print("<font color=\"red\"><strike><a href=\""+OWL+"TransitiveProperty\">Transitive</a></strike></font>");
						addUndo(isDeleted, "Delete", title);						
					}
					else {
						print("<a href=\""+OWL+"TransitiveProperty\">Transitive</a>");
						addDelete(prop, "P-TRA");						
					}
				}
				else print("<a href=\""+OWL+"TransitiveProperty\">Transitive</a>");
				println();
			}
			
			if (prop.isSymmetric(reasoner.getOntologies())) {
				OntologyChange isDeleted = isDeleted(prop, "P-SYM"); 
				if (editorEnabled) {
					if (isDeleted!=null) {
						print("<font color=\"red\"><strike><a href=\""+OWL+"SymmetricProperty\">Symmetric</a></strike></font>");
						addUndo(isDeleted, "Delete", title);						
					}
					else {
						print("<a href=\""+OWL+"SymmetricProperty\">Symmetric</a>");
						addDelete(prop, "P-SYM");						
					}
				}
				else print("<a href=\""+OWL+"SymmetricProperty\">Symmetric</a>");
				println();
			}		
		}
		
		// print temporarily added attribute changes
		Map added = this.getAdded(prop, "ATTRIB");
		if (!added.isEmpty()) {
			if (!printedAttribs) {
				printedAttribs = true;
				if (editorEnabled) title += addTitle("ATTRIB");
				println(title);
			}			
			Iterator j = added.entrySet().iterator();
			while(j.hasNext()) {
				Map.Entry entry = (Map.Entry) j.next();
				OWLObject obj = (OWLObject) entry.getKey();
				Set changes = (HashSet) entry.getValue();
				for (Iterator changeIter = changes.iterator(); changeIter.hasNext(); ) {
					OntologyChange change = (OntologyChange) changeIter.next();
					print("<font color=\"green\">");
					if (change instanceof SetFunctional) {
						print("Functional");						
					}
					else if (change instanceof SetInverseFunctional) {
						print("Inverse Functional");
					}
					else if (change instanceof SetTransitive) {
						print("Transitive");						
					}
					else if (change instanceof SetSymmetric) print("Symmetric");
					print("</font>");
					addUndo(change, "Add", title);
					println();
				}
			}
		}
		
		
		if (swoopModel.getEditorEnabled() && !printedAttribs) notPrinted.add("ATTRIB");		
		if (showDivisions && printedAttribs) print(HR+"<FONT FACE=\""+swoopModel.getFontFace()+"\" SIZE="+fontSize+">");

		// add unprinted titles at the bottom
		addUnprinted(notPrinted);	
	}
	
	// recursively find all MUPS using HST principle
	public void HSTMUPS(Set mups, OWLOntology onto, List MUPS, List explStr, Set satPaths, Set currPath) {
		
		// key step - make a backup of onto
		OWLOntology backup = swoopModel.cloneOntology(onto);
		
		try {
			for (Iterator iter = mups.iterator(); iter.hasNext();) {
				
				// reset ontology 
				OWLOntology copyOnt = swoopModel.cloneOntology(backup);
				
				OWLObject axiom = (OWLObject) iter.next();
				currPath.add(axiom);
//				System.out.println(axiom);
				
				// **** remove axiom from copyOnt *****
				if (axiom instanceof OWLDisjointClassesAxiom) {
					OWLDisjointClassesAxiom dis = (OWLDisjointClassesAxiom) axiom;
					Set disSet = dis.getDisjointClasses();
					Set newDisSet = new HashSet();
					for (Iterator iter2 = disSet.iterator(); iter2.hasNext();) {
						OWLDescription desc = (OWLDescription) iter2.next();
						if (desc instanceof OWLClass) 
							newDisSet.add(copyOnt.getClass(((OWLClass) desc).getURI()));						
						else 
							newDisSet.add(desc);
					}
					OWLDisjointClassesAxiom newDis = copyOnt.getOWLDataFactory().getOWLDisjointClassesAxiom(newDisSet);
					RemoveClassAxiom r = new RemoveClassAxiom(copyOnt, (OWLClassAxiom) newDis, null);
					r.accept((ChangeVisitor) copyOnt);
				}
				else if (axiom instanceof OWLEquivalentClassesAxiom) {
					OWLEquivalentClassesAxiom equ = (OWLEquivalentClassesAxiom) axiom;
					Set equSet = equ.getEquivalentClasses();
					Set newEquSet = new HashSet();
					List equList = new ArrayList();
					for (Iterator iter2 = equSet.iterator(); iter2.hasNext();) {
						OWLDescription desc = (OWLDescription) iter2.next();
						if (desc instanceof OWLClass) { 
							newEquSet.add(copyOnt.getClass(((OWLClass) desc).getURI()));
							equList.add(copyOnt.getClass(((OWLClass) desc).getURI()));
						}
						else {
							newEquSet.add(desc);
							equList.add(desc);
						}
					}
					OWLEquivalentClassesAxiom newEqu = copyOnt.getOWLDataFactory().getOWLEquivalentClassesAxiom(newEquSet);
					RemoveClassAxiom r = new RemoveClassAxiom(copyOnt, (OWLClassAxiom) newEqu, null);
					r.accept((ChangeVisitor) copyOnt);
					if (equList.size()==2) {
						OWLDescription desc1 = (OWLDescription) equList.get(0);
						OWLDescription desc2 = (OWLDescription) equList.get(0);
						if (desc1 instanceof OWLClass) {
							RemoveEquivalentClass re = new RemoveEquivalentClass(copyOnt, (OWLClass) desc1, desc2, null);
							re.accept((ChangeVisitor) copyOnt);
						}
						if (desc2 instanceof OWLClass) {
							RemoveEquivalentClass re = new RemoveEquivalentClass(copyOnt, (OWLClass) desc2, desc1, null);
							re.accept((ChangeVisitor) copyOnt);
						}
					}
				}
				else if (axiom instanceof OWLSubClassAxiom) {
					OWLSubClassAxiom subA = (OWLSubClassAxiom) axiom;
					OWLDescription sub = subA.getSubClass();
					OWLDescription sup = subA.getSuperClass();
					OWLDescription newSub = sub;
					if (sub instanceof OWLClass) newSub = copyOnt.getClass(((OWLClass) sub).getURI());
					OWLDescription newSup = sup;
					if (sup instanceof OWLClass) newSup = copyOnt.getClass(((OWLClass) sup).getURI());
					OWLSubClassAxiom newSubA = copyOnt.getOWLDataFactory().getOWLSubClassAxiom(newSub, newSup);
					OntologyChange r = new RemoveClassAxiom(copyOnt, (OWLClassAxiom) newSubA, null);
					r.accept((ChangeVisitor) copyOnt);
					if (newSub instanceof OWLClass) {
						r = new RemoveSuperClass(copyOnt, (OWLClass) newSub, newSup, null);
						r.accept((ChangeVisitor) copyOnt);
					}					
				}
				else if (axiom instanceof OWLPropertyDomainAxiom) {
					OWLPropertyDomainAxiom opd = (OWLPropertyDomainAxiom) axiom;
					OWLProperty prop = opd.getProperty();
					OWLProperty newProp = null;
					if (prop instanceof OWLDataProperty) newProp = copyOnt.getDataProperty(prop.getURI());
					else newProp = copyOnt.getObjectProperty(prop.getURI());
					OWLDescription desc = opd.getDomain();
					OWLDescription newDesc = desc;
					if (desc instanceof OWLClass) newDesc = copyOnt.getClass(((OWLClass) desc).getURI());
					RemoveDomain rd = new RemoveDomain(copyOnt, newProp, newDesc, null);
					rd.accept((ChangeVisitor) copyOnt);
				}
				else if (axiom instanceof OWLObjectPropertyRangeAxiom) {
					OWLObjectPropertyRangeAxiom opd = (OWLObjectPropertyRangeAxiom) axiom;
					OWLObjectProperty prop = opd.getProperty();
					OWLObjectProperty newProp = copyOnt.getObjectProperty(prop.getURI());
					OWLDescription desc = opd.getRange();
					OWLDescription newDesc = desc;
					if (desc instanceof OWLClass) newDesc = copyOnt.getClass(((OWLClass) desc).getURI());
					RemoveObjectPropertyRange ropr = new RemoveObjectPropertyRange(copyOnt, newProp, newDesc, null);
					ropr.accept((ChangeVisitor) copyOnt);
				}
				else if (axiom instanceof OWLObjectPropertyInstance) {
					OWLObjectPropertyInstance oop = (OWLObjectPropertyInstance) axiom;
					OWLIndividual sub = copyOnt.getIndividual(oop.getSubject().getURI());
					OWLObjectProperty prop = copyOnt.getObjectProperty(oop.getProperty().getURI());
					OWLIndividual obj = copyOnt.getIndividual(oop.getObject().getURI());
					RemoveObjectPropertyInstance ropi = new RemoveObjectPropertyInstance(copyOnt, sub, prop, obj, null);
					ropi.accept((ChangeVisitor) copyOnt);
				}
				else if (axiom instanceof OWLSameIndividualsAxiom) {
					OWLSameIndividualsAxiom osi = (OWLSameIndividualsAxiom) axiom;
					Set newInd = new HashSet();
					for (Iterator it = osi.getIndividuals().iterator(); it.hasNext();) {
						newInd.add(copyOnt.getIndividual(((OWLIndividual) it.next()).getURI()));						
					}
					OWLSameIndividualsAxiom copyInd = copyOnt.getOWLDataFactory().getOWLSameIndividualsAxiom(newInd);
					RemoveIndividualAxiom ria = new RemoveIndividualAxiom(copyOnt, copyInd, null);
					ria.accept((ChangeVisitor) copyOnt);
				}
				else if (axiom instanceof OWLDifferentIndividualsAxiom) {
					OWLDifferentIndividualsAxiom osi = (OWLDifferentIndividualsAxiom) axiom;
					Set newInd = new HashSet();
					for (Iterator it = osi.getIndividuals().iterator(); it.hasNext();) {
						newInd.add(copyOnt.getIndividual(((OWLIndividual) it.next()).getURI()));						
					}
					OWLDifferentIndividualsAxiom copyInd = copyOnt.getOWLDataFactory().getOWLDifferentIndividualsAxiom(newInd);
					RemoveIndividualAxiom ria = new RemoveIndividualAxiom(copyOnt, copyInd, null);
					ria.accept((ChangeVisitor) copyOnt);
				}
				//TODO: more removal!
				
				// test if copyOnt has changed
				//FIXME: not working when individual obj prop assertions are actually removed 
//				if (copyOnt.equals(onto)) {
//					System.out.println("Ontology hasn't changed after removing axiom "+axiom);
//					continue;
//				}
				
				// get class in copyOnt
				OWLClass cla = null;
				if (swoopModel.getSelectedEntity()!=null) cla = copyOnt.getClass(swoopModel.getSelectedEntity().getURI());
				
				// early path termination
				boolean earlyTermination = false;
				for (Iterator i=satPaths.iterator(); i.hasNext();) {
					Set satPath = (HashSet) i.next();
					if (satPath.containsAll(currPath)) {
						System.out.println("EARLY PATH TERMINATION!");
						earlyTermination = true;
						break;
					}
				}
				
				if (!earlyTermination) { 
					// check if there is a new mups of class
					Set newMUPS = new HashSet();
					String expl = "";
					if (swoopModel.isUseTableau()) {
						// use tableau tracing
						List explList = this.getTableauSOS(copyOnt, cla);
						expl = explList.get(0).toString();
						newMUPS = (HashSet) explList.get(1);
					}
					else {
						// use black box
						newMUPS = this.getBlackBoxSOS(copyOnt, cla);
					}
					if (!newMUPS.isEmpty()) {
						if (!MUPS.contains(newMUPS)) { 
							// print explanation for new MUPS
							MUPS.add(newMUPS);
							explStr.add(expl);
							System.out.println("FOUND NEW MUPS - MUPS COUNT: "+MUPS.size());
							// recurse!						
							HSTMUPS(newMUPS, copyOnt, MUPS, explStr, satPaths, currPath);
						}
					}
					else {
						satPaths.add(new HashSet(currPath));
					}
				}
				
				currPath.remove(axiom);
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	//************************************************************
	//Added for Econnections
	//************************************************************
	protected void renderForeignEntity(OWLEntity e) throws OWLException{
		if(e instanceof OWLClass){
		  print("<b> OWL-ForeignClass:</b>&nbsp;");
		  printObject(e);
			
		  print("<br><b> ForeignOntology </b> "); 
	    	print("<a href=\"" + swoopModel.getSelectedOntology().getForeignEntities().get(e).toString() + "\">" + swoopModel.shortForm(((URI)swoopModel.getSelectedOntology().getForeignEntities().get(e))) + "</a>");	
		}
		if(e instanceof OWLObjectProperty){
			if(!((OWLObjectProperty)e).isLink()){
				print("<b> OWL-ForeignObjectProperty:</b>&nbsp;");
				printObject(e);}
			else{
				print("<b> OWL-ForeignLinkProperty:</b>&nbsp;");
				printObject(e);
			}
			
			print("<br><b> ForeignOntology </b> "); 
		    print("<a href=\"" + swoopModel.getSelectedOntology().getForeignEntities().get(e).toString() + "\">" + swoopModel.shortForm(((URI)swoopModel.getSelectedOntology().getForeignEntities().get(e))) + "</a>");	
				
		}
		
		if(e instanceof OWLDataProperty){
			print("<b> OWL-ForeignDatatypeProperty:</b>&nbsp;");
			printObject(e);
			
			print("<br><b> ForeignOntology </b> "); 
		    print("<a href=\"" + swoopModel.getSelectedOntology().getForeignEntities().get(e).toString() + "\">" + swoopModel.shortForm(((URI)swoopModel.getSelectedOntology().getForeignEntities().get(e))) + "</a>");	
				
		}
		
		if(e instanceof OWLIndividual){
			print("<b> OWL-ForeignIndividual:</b>&nbsp;");
			printObject(e);
			
			print("<br><b> ForeignOntology </b> "); 
		    print("<a href=\"" + swoopModel.getSelectedOntology().getForeignEntities().get(e).toString() + "\">" + swoopModel.shortForm(((URI)swoopModel.getSelectedOntology().getForeignEntities().get(e))) + "</a>");	
				
		}
	}
	
	//***********************************************************

	protected void renderDataProperty(OWLDataProperty prop)	throws OWLException {
		
		// check if property is in current ontology or imported
		String importedLbl = "";
		imported = false;
		
		if (!prop.getOntologies().contains(swoopModel.getSelectedOntology())) {
			importedLbl = "Imported ";
			imported = true;
		}
		
		Iterator it = null;
		List notPrinted = new ArrayList();
		
		print("<b>"+importedLbl+"OWL-DatatypeProperty:</b>&nbsp;");
		printObject(prop);
		
		if (importedLbl.length()>0) {			
			println();
			// print("<b>Imported from Ontology(s):</b>&nbsp;");
			// also print where the class is imported from
			Iterator ontIter = prop.getOntologies().iterator();
			while (ontIter.hasNext()) {
				OWLOntology ont = (OWLOntology) ontIter.next();
				String ontURI = ont.getURI().toString();
				String ontName = ontURI.substring(ontURI.lastIndexOf("/")+1, ontURI.length());
				String propXPtrURI = prop.getURI() + XPointers.asDefinedIn + "(" + ontURI + ")";
				print("See its definition in Imported Ontology: <a href=\""+escape(propXPtrURI)+"\">" + escape(ontName) + "</a>");
				// print("<a href=\""+ontURI+"asDefinedIn("+ontURI+")\">"+className+"</a>&nbsp;&nbsp;");
				println();
			}
			println();
		}
		else if (editorEnabled) print("&nbsp;&nbsp;&nbsp;<font color=\"red\">(<a href=\"<SwitchProp\">Switch</a>)</font>");
		
		if (showDivisions) {
			print("<table cellpadding=\"5\"><tr bgcolor=\"#FFF68F\"><FONT FACE=\""+swoopModel.getFontFace()+"\" SIZE="+fontSize+">");
		}

		boolean printedAnn = renderAnnotations(prop);
		if (!printedAnn) notPrinted.add("A-ANN");
		else if (showDivisions) print(HR+"<FONT FACE=\""+swoopModel.getFontFace()+"\" SIZE="+fontSize+">");
		
		int notPrintedSize = notPrinted.size();
		
		String title = "<b>Equivalent to:</b>";
		if (reasoner.equivalentPropertiesOf(prop).size()==0) {
			boolean printed = insertAddedChanges(new HashSet(), prop, "P-EQU", title);
			if (!printed) notPrinted.add("P-EQU");
		}
		else {
			if (editorEnabled) title += addTitle("P-EQU");
			printCollection(reasoner.equivalentPropertiesOf(prop), title);			
		}
		
		title = "<b>Subproperty of:</b>";
		if (reasoner.superPropertiesOf(prop).size()==0) {
			boolean printed = insertAddedChanges(new HashSet(), prop, "P-SUB", title);
			if (!printed) notPrinted.add("P-SUB");
		}
		else {
			if (editorEnabled) title += addTitle("P-SUB");
			printCollection(reasoner.superPropertiesOf(prop), title);
		}
		
		title = "<b>Superproperty of:</b>";
		if (reasoner.subPropertiesOf(prop).size()==0) {
			boolean printed = insertAddedChanges(new HashSet(), prop, "P-SUP", title);
			if (!printed) notPrinted.add("P-SUP");
		}
		else {
			if (editorEnabled) title += addTitle("P-SUP");
			printCollection(reasoner.subPropertiesOf(prop), title);
		}
		
		if ((notPrinted.size()-notPrintedSize<3) && showDivisions) print(HR+"<FONT FACE=\""+swoopModel.getFontFace()+"\" SIZE="+fontSize+">");
		notPrintedSize = notPrinted.size();
		
		title = "<b>Has domain:</b>";
		Set doms = reasoner.domainsOf(prop);
		doms.addAll(prop.getDomains(swoopModel.getSelectedOntology()));
		if (doms.size()==0) {
			boolean printed = insertAddedChanges(new HashSet(), prop, "C-HASDOM", title);
			if (!printed) notPrinted.add("C-HASDOM");
		}
		else {
			if (editorEnabled) title += addTitle("C-HASDOM");
			printCollection(doms, title);
		}
		
		title = "<b>Has range:</b>";
		if (reasoner.rangesOf(prop).size()==0) {
			boolean printed = insertAddedChanges(new HashSet(), prop, "C-HASRAN", title);
			if (!printed) notPrinted.add("C-HASRAN");
		}
		else {
			if (editorEnabled) title += addTitle("C-HASRAN");
			printCollection(reasoner.rangesOf(prop), title);
		}
		
		if ((notPrinted.size()-notPrintedSize<2) && showDivisions) print(HR+"<FONT FACE=\""+swoopModel.getFontFace()+"\" SIZE="+fontSize+">");
		notPrintedSize = notPrinted.size();
		
		title = "<b>Attributes</b>:";
		String OWL = OWLVocabularyAdapter.OWL;
		boolean printedAttribs = false;
		if (prop.isFunctional(reasoner.getOntologies())) {
			printedAttribs = true;
			if (editorEnabled) title += addTitle("ATTRIB");
			println(title);
			OntologyChange isDeleted = isDeleted(prop, "P-FUN"); 
			if (editorEnabled) {
				if (isDeleted!=null) {
					print("<font color=\"red\"><strike><a href=\""+OWL+"FunctionalProperty\">Functional</a></strike></font>");
					addUndo(isDeleted, "Delete", title);						
				}
				else {
					print("<a href=\""+OWL+"FunctionalProperty\">Functional</a>");
					addDelete(prop, "P-FUN");						
				}
			}
			else print("<a href=\""+OWL+"FunctionalProperty\">Functional</a>");
			println();
		}
//		 print temporarily added attribute changes
		Map added = this.getAdded(prop, "ATTRIB");
		if (!added.isEmpty()) {
			if (!printedAttribs) {
				printedAttribs = true;
				if (editorEnabled) title += addTitle("ATTRIB");
				println(title);
			}			
			Iterator j = added.entrySet().iterator();
			while(j.hasNext()) {
				Map.Entry entry = (Map.Entry) j.next();
				OWLObject obj = (OWLObject) entry.getKey();
				Set changes = (HashSet) entry.getValue();
				for (Iterator changeIter = changes.iterator(); changeIter.hasNext(); ) {
					OntologyChange change = (OntologyChange) changeIter.next();
					print("<font color=\"green\">");
					if (change instanceof SetFunctional) print("Functional");
					print("</font>");
					addUndo(change, "Add", title);
					println();
				}
			}
		}
		
		if (swoopModel.getEditorEnabled() && !printedAttribs) notPrinted.add("ATTRIB");
		if (showDivisions && printedAttribs) print(HR+"<FONT FACE=\""+swoopModel.getFontFace()+"\" SIZE="+fontSize+">");
//		 rules stuff
		if (swoopModel.getEnableRules()) {
			RulesExpressivity ruleExpr = swoopModel.getRuleExpr();
			Set rules = ruleExpr.getRules(prop);
			pw.println("<b> Rules: </b> <br>");
			for (Iterator iter = rules.iterator(); iter.hasNext();) {
				String ruleDesc = ((RuleValue) iter.next()).getRule().toString();
				int ptr;
				ptr = ruleDesc.indexOf(">")+1;
			    String consequent = ruleDesc.substring(ptr,ruleDesc.indexOf("."));
			    ruleDesc = ruleDesc.substring(0,ptr-2)+".";
			    ruleDesc = consequent + " <b>:- </b> " + ruleDesc;
				ptr = 0;
				ruleDesc = " " + ruleDesc;
				String hyperRule = ruleDesc;
				while ((ptr = ruleDesc.indexOf("(", ptr))>=0) {
					String name = ruleDesc.substring(ruleDesc.lastIndexOf(" ", ptr), ptr);
					String uri = this.getURIForTerm(name.trim());
					hyperRule = hyperRule.replaceAll(name, "<a href=\""+uri+"\">"+name+"</a>");
					ptr++;
				}
				pw.println( hyperRule + "<br>");
			}
		}
		// add unprinted titles at the bottom
		addUnprinted(notPrinted);
				
	}

	protected void renderDataType(OWLDataType datatype) throws OWLException {
		println("<b>OWL-Datatype:</b>&nbsp;" + shortForm(datatype.getURI()) + "  ");
	}
	
	protected void println() {
		pw.println("<br>");		
	}

	public void setEditorEnabled(boolean mode) {
		this.editorEnabled = mode;
		//if(mode) changes = new ArrayList();		
	}
	
	private void reset() {
		OWLDescHash = new Hashtable();				
	}
	
	public void render(OWLEntity entity, SwoopModel swoopModel, Writer writer) throws RendererException {
		changes = swoopModel.getUncommittedChanges();
		reset();
		displayedEntity = entity;
		StringWriter sw = new StringWriter();
		PrintWriter buffer = new PrintWriter(sw);
		super.render(entity, swoopModel, buffer);
		String output = postProcess(sw.getBuffer().toString());
		try {
			writer.write(output);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Method that handles the Add* operation when the user clicks on the
	 * corresponding hyperlink in the Concise Format Pane (eg. Add SuperClass)
	 * It calls the appropriate PopupAdd* Java Class depending on the type of add operation
	 * @param hLink - code representing type of Add operation
	 * @return
	 * @throws OWLException
	 */
	public List handleAddLink(String hLink) throws OWLException {

		OWLOntology ontology = reasoner.getOntology();
		// remove the prefixed "<"
		if (hLink.startsWith("<")) hLink = hLink.substring(1, hLink.length());
		else System.out.println("hyperlink error: "+hLink);
//		 new rules addition
		if ( hLink.equals( "AddRULE" ) ) {
		    
		    PopupAddRule test = new PopupAddRule( swoopModel, displayedEntity );
		    test.show();
		}
		if (hLink.equals("AddA-ANN")) {
			// add annotation
			PopupAddAnnotation popup = new PopupAddAnnotation(swoopModel, false);
			popup.setLocation(200, 200);
			popup.show();
			return new ArrayList();			
		}
		
		if (hLink.equals("AddATTRIB")) {
			// add property attribute - Functional etc
			JList attribList = new JList();
			//attribList.setFont(new Font("Tahoma", Font.PLAIN, 10));
			List attribs = new ArrayList();
			OWLProperty prop = (OWLProperty) swoopModel.getSelectedEntity();
			if (!prop.isFunctional(reasoner.getOntologies())) attribs.add("Functional");
			if (prop instanceof OWLObjectProperty) {
				if (!((OWLObjectProperty) prop).isInverseFunctional(reasoner.getOntologies())) attribs.add("Inverse Functional");
				if (!((OWLObjectProperty) prop).isTransitive(reasoner.getOntologies())) attribs.add("Transitive");
				if (!((OWLObjectProperty) prop).isSymmetric(reasoner.getOntologies())) attribs.add("Symmetric");
			}
			if (attribs.size()==0) {
				JOptionPane.showMessageDialog(null, "Property already has all possible attributes", "Unable to Add", JOptionPane.INFORMATION_MESSAGE);
				return new ArrayList();
			}
			attribList.setListData(attribs.toArray());
			int attr = JOptionPane.showConfirmDialog(
                    null,
                    new JScrollPane(attribList),
                    "Specify Property Attribute:",
                    JOptionPane.YES_NO_OPTION
            );
			if (attr==0) {
				OntologyChange change = null;
				String sel = attribList.getSelectedValue().toString();
				if (sel.equals("Functional")) change = new SetFunctional(ontology, prop, true, null);
				else if (sel.equals("Inverse Functional")) change = new SetInverseFunctional(ontology, (OWLObjectProperty) prop, true, null);
				else if (sel.equals("Transitive")) change = new SetTransitive(ontology, (OWLObjectProperty) prop, true, null);
				else if (sel.equals("Symmetric")) change = new SetSymmetric(ontology, (OWLObjectProperty) prop, true, null);
				
				swoopModel.addUncommittedChange(change);
			}
			return new ArrayList();
		}
		
		if (hLink.startsWith("AddC")) {
			// popup Add Class Frame
			String type = hLink.substring(hLink.indexOf("-")+1, hLink.length());
			PopupAddClass popup = new PopupAddClass(reasoner, type, swoopModel);
			popup.setLocation(200, 200);
			popup.show();
			return popup.changes;
		}
		
		if (hLink.startsWith("AddP")) {
			// popup Add Property Frame
			boolean DP = true, OP = true;
			if (hLink.startsWith("AddP-INSO")) DP = false;
			if (hLink.startsWith("AddP-INSD")) OP = false;
			String type = hLink.substring(hLink.indexOf("-")+1, hLink.length());
			PopupAddProperty popup = new PopupAddProperty(reasoner, type, swoopModel, DP, OP);
			popup.setLocation(200, 200);
			popup.show();
			return popup.changes;
		}
		
		if (hLink.startsWith("AddI-SAM") || hLink.startsWith("AddI-DIF") || hLink.startsWith("AddI-ONE") || hLink.startsWith("AddI-INS")) {
			// popup Add Individual Frame
			String type = hLink.substring(hLink.indexOf("-")+1, hLink.length());
			PopupAddIndividual popup = new PopupAddIndividual(reasoner, type, swoopModel);
			popup.setLocation(200, 200);
			popup.show();
			return popup.changes;
		}
		
		if (hLink.startsWith("AddV")) {
			// add value for property
			String hash = hLink.substring(hLink.indexOf(":")+1, hLink.length());
			OWLProperty prop = (OWLProperty) OWLDescHash.get(hash);
			//***********************************************************
			//changed for Econnections
			//***********************************************************
            OWLEntity entit = swoopModel.getSelectedEntity();
			if(prop instanceof OWLObjectProperty){
				if(((OWLObjectProperty)prop).isLink()){
					URI u = ((OWLObjectProperty)prop).getLinkTarget();
					OWLOntology foreignOnto = swoopModel.getOntology(u);
					reasoner.setOntology(foreignOnto);
					swoopModel.setSelectedOntology(foreignOnto);
				}
			}
			PopupAddValue popup = new PopupAddValue(reasoner, swoopModel, prop);
			popup.setLocation(200, 200);
			popup.show();
			if(prop instanceof OWLObjectProperty){
				if(((OWLObjectProperty)prop).isLink()){
					reasoner.setOntology(ontology);
					swoopModel.setSelectedOntology(ontology);
					swoopModel.setSelectedEntity(entit);
				}
			}
			
			
			return popup.changes;
		}
		
		return null;
	}
public List handlePublishLink(String hLink) throws OWLException {
		
		OWLOntology ontology = reasoner.getOntology();

		// parse DELETE hyper-link
		int pos1 = hLink.indexOf(":");
		int pos2 = hLink.indexOf(":", pos1 + 1);
		String hashCode = hLink.substring(pos1 + 1, pos2);
		String titleCode = hLink.substring(pos2 + 1, hLink.length());
		Object obj = OWLDescHash.get(hashCode);
		OWLRule rule = (OWLRule) obj;
		
		if ( titleCode.startsWith( "RULE" ) ) {
		    //System.out.println( "Deleting Rule" );
		    //String n3 = swoopModel.getRuleExpr().toN3();
		    ExternalRuleSubmitter submitter = new ExternalRuleSubmitter( swoopModel, rule );
		}
		
		//no changes to return
		return null;
		   
	}
	/**
	 * 
	 * Returns a list of changes that are required to fufill the requirements of clicking a
	 * delete link. A delete operation may involve various steps of changes and may also
	 * cause some other changes to happen. The list will contain all the atomic changes
	 * required for this operation  
	 * 
	 * @param hLink the link clicked on the editor pane
	 * @return A list of OntologyChange objects
	 * @throws OWLException

	 */
	public List handleDeleteLink(String hLink) throws OWLException {
		List changes = new ArrayList();	
		OWLOntology ontology = reasoner.getOntology();
		
		// parse DELETE hyper-link
		int pos1 = hLink.indexOf(":");
		int pos2 = hLink.indexOf(":", pos1+1);
		String hashCode = hLink.substring(pos1+1, pos2);
		String titleCode = hLink.substring(pos2+1, hLink.length());
		Object obj = OWLDescHash.get(hashCode);
		
		if (titleCode.equals("P-FUN")) {
			// Remove Functional Attribute
			// *** Note: Change is effective immediately for property attribute
			SetFunctional change = new SetFunctional(ontology, (OWLProperty) obj, false, null);
			swoopModel.addUncommittedChange(change);
			return new ArrayList();
		}
		
		if (titleCode.equals("P-IFUN")) {
			// Remove InverseFunctional Attribute
			// *** Note: Change is effective immediately for property attribute
			SetInverseFunctional change = new SetInverseFunctional(ontology, (OWLObjectProperty) obj, false, null);
			swoopModel.addUncommittedChange(change);
			return new ArrayList();
		}
		
		if (titleCode.equals("P-TRA")) {
			// Remove Transitive Attribute
			// *** Note: Change is effective immediately for property attribute
			SetTransitive change = new SetTransitive(ontology, (OWLObjectProperty) obj, false, null);
			swoopModel.addUncommittedChange(change);
			return new ArrayList();
		}
		
		if (titleCode.equals("P-SYM")) {
			// Remove Symmetric Attribute
			// *** Note: Change is effective immediately for property attribute
			SetSymmetric change = new SetSymmetric(ontology, (OWLObjectProperty) obj, false, null);
			swoopModel.addUncommittedChange(change);
			return new ArrayList();
		}
		
		if (titleCode.equals("A-ANN")) {
			// Remove Annotation Instance
			OWLEntity currEntity = swoopModel.getSelectedEntity();
			if (obj instanceof OWLAnnotationInstance) {
				OWLAnnotationInstance oai = (OWLAnnotationInstance) obj;
				RemoveAnnotationInstance change = new RemoveAnnotationInstance(ontology, currEntity, oai.getProperty(), oai.getContent(), null);
				swoopModel.addUncommittedChange(change);
			}
			return new ArrayList();
		}
		
		if (titleCode.equals("I-SAM") || titleCode.equals("I-DIF")) {
			// delete sameAs axiom
			Set indSet = new HashSet();
			indSet.add((OWLIndividual) displayedEntity);
			indSet.add((OWLIndividual) obj);
			OWLIndividualAxiom indAxiom = null;
			if (titleCode.equals("I-SAM")) indAxiom = ontology.getOWLDataFactory().getOWLSameIndividualsAxiom(indSet);
			else indAxiom = ontology.getOWLDataFactory().getOWLDifferentIndividualsAxiom(indSet);
			RemoveIndividualAxiom change = new RemoveIndividualAxiom(ontology, indAxiom, null);
			changes.add(change);
		}
		
		if (titleCode.equals("C-EQU")) {			
			// delete equivalent class
			OWLClass displayedClass = (OWLClass) displayedEntity;
			if (obj instanceof OWLDescription) {
				OWLDescription desc = (OWLDescription) obj;
				RemoveEquivalentClass change = new RemoveEquivalentClass(ontology, displayedClass, desc, null);
				changes.add(change);
			}
			else {
				Iterator descIter = ((Collection) obj).iterator();
				while (descIter.hasNext()){
					OWLDescription desc = (OWLDescription) descIter.next();
					RemoveEquivalentClass change = new RemoveEquivalentClass(ontology, displayedClass, desc, null);
					changes.add(change);				
				}				
			}		
		}
		else if (titleCode.equals("C-DIS")) {			
			// delete disjoint class
			OWLClass displayedClass = (OWLClass) displayedEntity;
			Set disSet = new HashSet();
			if (obj instanceof OWLDescription) {
				OWLDescription desc = (OWLDescription) obj;				
				disSet.add(desc);
				disSet.add(displayedClass);
			}
			else {
				disSet.add(displayedClass);
				disSet.addAll((Collection) obj);
			}
			
			OWLDisjointClassesAxiom disAxiom = ontology.getOWLDataFactory().getOWLDisjointClassesAxiom(disSet);
			RemoveClassAxiom change = new RemoveClassAxiom(ontology, disAxiom, null);				
			changes.add(change);								
		}
		else if (titleCode.equals("C-SUB")) {			
			// delete super-class
			OWLClass displayedClass = (OWLClass) displayedEntity;
			if (obj instanceof OWLDescription) {
				OWLDescription desc = (OWLDescription) obj;
				if (displayedClass.getSuperClasses(ontology).contains(obj)) {
					// add RemoveSuperClass change in this case
					RemoveSuperClass change = new RemoveSuperClass(ontology, displayedClass, desc, null);				
					changes.add(change);
				}
				else {
					// remove specific axiom in the other case
					OWLSubClassAxiom axiom = ontology.getOWLDataFactory().getOWLSubClassAxiom(displayedClass, desc);
					RemoveClassAxiom change2 = new RemoveClassAxiom(ontology, axiom, null);
					changes.add(change2);
				}
			}
			else {
				Iterator descIter = ((Collection) obj).iterator();
				while (descIter.hasNext()){
					OWLDescription desc = (OWLDescription) descIter.next();
					if (displayedClass.getSuperClasses(ontology).contains(obj)) {
						RemoveSuperClass change = new RemoveSuperClass(ontology, displayedClass, desc, null);
						changes.add(change);
					}
					else {
						// remove specific axiom if present
						OWLSubClassAxiom axiom = ontology.getOWLDataFactory().getOWLSubClassAxiom(displayedClass, desc);
						RemoveClassAxiom change2 = new RemoveClassAxiom(ontology, axiom, null);
						changes.add(change2);
					}
				}
			}								
		}
		else if (titleCode.equals("C-SUP")) {			
			// delete super-class
			OWLClass displayedClass = (OWLClass) displayedEntity;
			if (obj instanceof OWLClass) {
				// super classes can be defined in two ways
				// check to see what kind of change needs to be put
				OWLClass desc = (OWLClass) obj;
				if (desc.getSuperClasses(ontology).contains(displayedClass)) {
					RemoveSuperClass change = new RemoveSuperClass(ontology, desc, displayedClass, null);
					changes.add(change);
				}
				else {
					// remove specific axiom if present
					OWLSubClassAxiom axiom = ontology.getOWLDataFactory().getOWLSubClassAxiom(desc, displayedClass);
					RemoveClassAxiom change2 = new RemoveClassAxiom(ontology, axiom, null);
					changes.add(change2);
				}
			}
			else {
				OWLDescription desc = (OWLDescription) obj;
				// remove specific axiom if present
				OWLSubClassAxiom axiom = ontology.getOWLDataFactory().getOWLSubClassAxiom(desc, displayedClass);
				RemoveClassAxiom change2 = new RemoveClassAxiom(ontology, axiom, null);
				changes.add(change2);
			}								
		}
		else if (titleCode.equals("C-INT")) {			
			// delete intersection element
			// remove whole intersection and add remaining elements
			OWLClass displayedClass = (OWLClass) displayedEntity;
			if (obj instanceof OWLDescription) {
				OWLDescription desc = (OWLDescription) obj;
				deleteFromBooleanDesc(ontology, displayedClass, desc, OWLAnd.class, changes);
			}
			else {
				Iterator descIter = ((Collection) obj).iterator();
				while (descIter.hasNext()){
					OWLClass desc = (OWLClass) descIter.next();
					deleteFromBooleanDesc(ontology, displayedClass, desc, OWLAnd.class, changes);
				}
			}
		}
		else if (titleCode.equals("C-UNI")) {			
			// delete union element
			// remove whole union and add remaining elements
			OWLClass displayedClass = (OWLClass) displayedEntity;
			if (obj instanceof OWLDescription) {
				OWLDescription desc = (OWLDescription) obj;
				deleteFromBooleanDesc(ontology, displayedClass, desc, OWLOr.class, changes);
			}
			else {
				Iterator descIter = ((Collection) obj).iterator();
				while (descIter.hasNext()){
					OWLClass desc = (OWLClass) descIter.next();
					deleteFromBooleanDesc(ontology, displayedClass, desc, OWLOr.class, changes);
				}
			}
		}
		else if (titleCode.equals("C-NOT")) {			
			// delete complement element
			OWLClass displayedClass = (OWLClass) displayedEntity;			
			if (obj instanceof OWLDescription)  {
				BooleanElementChange change = new BooleanElementChange(OWLNot.class, "Remove", ontology, displayedClass, (OWLDescription) obj, null);
				changes.add(change);
			}			
		}
		else if (titleCode.equals("I-ONE")) {			
			// delete one-of element
			OWLClass displayedClass = (OWLClass) displayedEntity;
			if (obj instanceof OWLIndividual) {
				OWLIndividual desc = (OWLIndividual) obj;
				updateEnumerations(ontology, displayedClass, desc, changes);
			}			
		}
		else if (titleCode.equals("I-INS")) {			
			// delete instance
			RemoveIndividualClass change = new RemoveIndividualClass(ontology, (OWLIndividual) obj, (OWLDescription) displayedEntity, null);
			changes.add(change);
		}
		else if (titleCode.equals("C-TYP")) {			
			// delete type
			RemoveIndividualClass change = new RemoveIndividualClass(ontology, (OWLIndividual) displayedEntity, (OWLDescription) obj, null);
			changes.add(change);
		}
		else if (titleCode.equals("C-HASDOM")) {			
			// delete property domain
			OWLProperty prop = (OWLProperty) displayedEntity;
			if (obj instanceof OWLDescription) {
				OWLDescription desc = (OWLDescription) obj;
				RemoveDomain change = new RemoveDomain(ontology, prop, desc, null);
				changes.add(change);
			}
		}
		else if (titleCode.equals("P-DOM")) {			
			// delete property domain
			OWLClass cla = (OWLClass) displayedEntity;
			if (obj instanceof OWLProperty) {
				RemoveDomain change = new RemoveDomain(ontology, (OWLProperty) obj, cla, null);
				changes.add(change);
			}
		}
		else if (titleCode.equals("C-HASRAN")) {			
			// delete property range
			// check if datatype or object property
			if (displayedEntity instanceof OWLObjectProperty) {
				OWLObjectProperty prop = (OWLObjectProperty) displayedEntity;
				if (obj instanceof OWLDescription) {
					OWLDescription desc = (OWLDescription) obj;
					RemoveObjectPropertyRange change = new RemoveObjectPropertyRange(ontology, prop, desc, null);
					changes.add(change);
				}
			}
			else {
				OWLDataProperty prop = (OWLDataProperty) displayedEntity;
				if (obj instanceof OWLDataRange) {
					OWLDataRange dran = (OWLDataRange) obj;
					RemoveDataPropertyRange change = new RemoveDataPropertyRange(ontology, prop, dran, null);
					changes.add(change);
				}
			}
			
		}
		else if (titleCode.equals("P-RAN")) {			
			// delete property range
			OWLClass cla = (OWLClass) displayedEntity;
			if (obj instanceof OWLObjectProperty) {
				RemoveObjectPropertyRange change = new RemoveObjectPropertyRange(ontology, (OWLObjectProperty) obj, cla, null);
				changes.add(change);
			}
		}
		else if (titleCode.equals("P-SUB")) {			
			// remove super property
			if (obj instanceof OWLProperty) {
				if (((OWLProperty) displayedEntity).getSuperProperties(ontology).contains(obj)) {
					RemoveSuperProperty change = new RemoveSuperProperty(ontology, (OWLProperty) displayedEntity, (OWLProperty) obj, null);
					changes.add(change);
				}
				else {
					// remove specific axiom if present
					OWLSubPropertyAxiom axiom = ontology.getOWLDataFactory().getOWLSubPropertyAxiom((OWLProperty) displayedEntity, (OWLProperty) obj);
					RemovePropertyAxiom change2 = new RemovePropertyAxiom(ontology, axiom, null);
					changes.add(change2);
				}
			}
		}
		else if (titleCode.equals("P-SUP")) {			
			// remove sub property
			if (obj instanceof OWLProperty) {
				if (((OWLProperty) obj).getSuperProperties(ontology).contains(displayedEntity)) {
					RemoveSuperProperty change = new RemoveSuperProperty(ontology, (OWLProperty) obj, (OWLProperty) displayedEntity, null);
					changes.add(change);
				}
				else {
					// remove specific axiom if present
					OWLSubPropertyAxiom axiom = ontology.getOWLDataFactory().getOWLSubPropertyAxiom((OWLProperty) obj, (OWLProperty) displayedEntity);
					RemovePropertyAxiom change2 = new RemovePropertyAxiom(ontology, axiom, null);
					changes.add(change2);
				}
			}								
		}
		else if (titleCode.equals("P-EQU")) {			
			// remove equivalent property
			if (obj instanceof OWLProperty) {
				Set propSet = new HashSet();
				propSet.add((OWLProperty) obj);
				propSet.add((OWLProperty) displayedEntity);
				OWLEquivalentPropertiesAxiom axiom = ontology.getOWLDataFactory().getOWLEquivalentPropertiesAxiom(propSet);
				RemovePropertyAxiom change = new RemovePropertyAxiom(ontology, axiom, null);
				changes.add(change);
			}
		}
		else if (titleCode.equals("P-INV")) {			
			// remove inverse property
			if (obj instanceof OWLObjectProperty) {
				OWLObjectProperty prop = (OWLObjectProperty) displayedEntity;
				OWLObjectProperty inverse = (OWLObjectProperty) obj;
				RemoveInverse change = new RemoveInverse(ontology, prop, inverse, null);
				changes.add(change);
			}
		}
		else if (titleCode.startsWith(("P-VAL"))) {
			// remove property value pair
			
			// also obtain value from hash table
			String valueHashKey = titleCode.substring(titleCode.lastIndexOf(":")+1, titleCode.length());
			Object value = OWLDescHash.get(valueHashKey);
			
			if (obj instanceof OWLObjectProperty) {
				RemoveObjectPropertyInstance change = new RemoveObjectPropertyInstance(ontology, (OWLIndividual) displayedEntity, (OWLObjectProperty) obj, (OWLIndividual) value, null);
				changes.add(change);
			}
			else if (obj instanceof OWLDataProperty) {
				RemoveDataPropertyInstance change = new RemoveDataPropertyInstance(ontology, (OWLIndividual) displayedEntity, (OWLDataProperty) obj, (OWLDataValue) value, null);
				changes.add(change);
			}
		}
		else if ( titleCode.startsWith( "RULE" ) ) {
		    System.out.println( "Deleting Rule" );
		    //String n3 = swoopModel.getRuleExpr().publishRulesToPychinko();		    		   
		    
		    // obj is OWLRule
		    // go through the rulemap and remove this rule
		    OWLRule rule = (OWLRule) obj;
		    //get the  rule and its friggin expressivity (exactly why are they bundled together?)
		    OWLRuleAtom consAtom = (OWLRuleAtom)rule.getConsequents().iterator().next();
		    OWLObject key = null;
		    
		    
		    
		    if (consAtom instanceof OWLRuleClassAtom) {
				key = ((OWLRuleClassAtom) consAtom).getDescription();
			} else {
				if (consAtom instanceof OWLRuleDataPropertyAtom) {
					key = ((OWLRuleDataPropertyAtom) consAtom).getProperty();
				} else {
					
					if (consAtom instanceof OWLRuleObjectPropertyAtom) {
						key = ((OWLRuleObjectPropertyAtom) consAtom)
								.getProperty();
					}
				}
			}
					    
		    HashSet rulesSet = (HashSet) swoopModel.getRuleExpr().getRuleMap().get(key);
		    
		    //find the rule we want to delete
		    RuleValue rvDelete = null;
		    Iterator it = rulesSet.iterator();		    
		    while (it.hasNext()) {
		    	RuleValue rv = (RuleValue) it.next();
		    	if (rv.getRule().equals(obj)) {
		    		rvDelete = rv;
		    	}		    	
		    }
		    rulesSet.remove(rvDelete);
		    
		    swoopModel.getRuleExpr().getRuleMap().put(key, rulesSet);
						
			
		    		   
		}
		
		return changes;
	}
	
	/**
	 * Handle what happens when the reasoner supports explanation, a "Why?" link is displayed
	 * next to an inference and the user clicks on the link.
	 * In this case, we call reasoner's explanation tool to display reason
	 */
	public List handleWhyLink(String hLink) throws Exception {
		
		OWLOntology ontology = reasoner.getOntology();
		OWLDataFactory ontDF = ontology.getOWLDataFactory();
		
		// parse WHY hyper-link
		int pos1 = hLink.indexOf(":");
		int pos2 = hLink.indexOf(":", pos1+1);
		String hashCode = hLink.substring(pos1+1, pos2);
		String titleCode = hLink.substring(pos2+1, hLink.length());
		Object obj = OWLDescHash.get(hashCode);
		OWLEntity entity = swoopModel.getSelectedEntity();
		
		// create description of inference
		String infSubj = "", infObj = "", infRel = ""; 
		if (entity instanceof OWLNamedObject) {
			infSubj = swoopModel.shortForm(((OWLNamedObject) entity).getURI());
			// add entity URI to highlightSet in ConciseFormatVisitor
			((ConciseFormatVisitor) visitor).highlightMap.put(((OWLNamedObject) entity).getURI(), "<font color=\"green\">");
		}
		if (obj instanceof OWLNamedObject) {
			infObj = swoopModel.shortForm(((OWLNamedObject) obj).getURI());
			// add obj URI to highlightSet in ConciseFormatVisitor
			((ConciseFormatVisitor) visitor).highlightMap.put(((OWLNamedObject) obj).getURI(), "<font color=\"green\">");
		}
		
		OWLDescription satClazz = null;
		List restoreChanges = new ArrayList();
		Set sos = new HashSet();
		
		if (titleCode.equals("C-SUB")) {
			// explanation for why entity subClassOf obj
			// check sat. of C ^ ~D
			infRel = infSubj + "&nbsp;" + ConciseFormat.SUBSET + "&nbsp;"+ infObj;
			Set and = new HashSet();
			and.add(entity);
			and.add(ontDF.getOWLNot((OWLDescription) obj));
			satClazz = ontDF.getOWLAnd(and);
			sos = this.getSOS(ontology, satClazz);
		}
		else if (titleCode.equals("C-SUP")) {
			// check sat. of D ^ ~C
			infRel = infObj + "&nbsp;"+ConciseFormat.SUBSET + "&nbsp;"+infSubj;
			Set and = new HashSet();
			and.add(obj);
			and.add(ontDF.getOWLNot((OWLDescription) entity));
			satClazz = ontDF.getOWLAnd(and);
			sos = this.getSOS(ontology, satClazz);
		}
		else if (titleCode.equals("C-EQU")) {
			// check sat. of (C ^ ~D) v (~C ^ D)
			infRel = infSubj + "&nbsp;"+ConciseFormat.EQUIVALENT + "&nbsp;"+infObj;
			Set and1 = new HashSet();
			and1.add(entity);
			and1.add(ontDF.getOWLNot((OWLDescription) obj));
			Set and2 = new HashSet();
			and2.add(obj);
			and2.add(ontDF.getOWLNot((OWLDescription) entity));
			Set or = new HashSet();
			or.add(ontDF.getOWLAnd(and1));
			or.add(ontDF.getOWLAnd(and2));
			satClazz = ontDF.getOWLOr(or);
			sos = this.getSOS(ontology, satClazz);
		}
		else if (titleCode.equals("C-DIS")) {
			// check sat. of C ^ D
			infRel = infSubj + "&nbsp;"+ConciseFormat.SUBSET + ConciseFormat.COMPLEMENT + "&nbsp;"+infObj;
			Set and = new HashSet();
			and.add(obj);
			and.add(entity);
			satClazz = ontDF.getOWLAnd(and);
			sos = this.getSOS(ontology, satClazz);
		}
		else if (titleCode.equals("C-HASDOM")) {
			// check sat. of (\exists P. Top ^ ~C)
			infRel = infSubj + " rdfs:domain " + infObj;
			OWLProperty prop = (OWLProperty) entity;
			OWLDescription dom = (OWLDescription) obj;
			OWLDescription existsPT = null;
			OWLClass thing = ontDF.getOWLThing();
			if (prop instanceof OWLDataProperty) existsPT = ontDF.getOWLDataCardinalityAtLeastRestriction((OWLDataProperty) prop, 1);
			else existsPT = ontDF.getOWLObjectSomeRestriction((OWLObjectProperty) prop, thing);
			Set and = new HashSet();
			and.add(existsPT);
			and.add(ontDF.getOWLNot(dom));
			satClazz = ontDF.getOWLAnd(and);
			sos = this.getSOS(ontology, satClazz);
		}
		else if (titleCode.equals("P-DOM")) {
			// check sat. of (\exists P. Top ^ ~C)
			infRel = infObj + " rdfs:domain " + infSubj;
			OWLProperty prop = (OWLProperty) obj;
			OWLClass dom = (OWLClass) entity;
			OWLDescription existsPT = null;
			OWLClass thing = ontDF.getOWLThing();
			if (prop instanceof OWLDataProperty) existsPT = ontDF.getOWLDataCardinalityAtLeastRestriction((OWLDataProperty) prop, 1);
			else existsPT = ontDF.getOWLObjectSomeRestriction((OWLObjectProperty) prop, thing);
			Set and = new HashSet();
			and.add(existsPT);
			and.add(ontDF.getOWLNot(dom));
			satClazz = ontDF.getOWLAnd(and);
			sos = this.getSOS(ontology, satClazz);
		}
		else if (titleCode.equals("C-HASRAN") && entity instanceof OWLObjectProperty) {
			// check sat. of (Thing ^ ~(\forall P.C))
			infRel = infSubj + " rdfs:range " + infObj;
			OWLObjectProperty prop = (OWLObjectProperty) entity;
			OWLDescription ran = (OWLDescription) obj;
			OWLDescription forallPR = null;
			OWLClass thing = ontDF.getOWLThing();
			forallPR = ontDF.getOWLObjectAllRestriction(prop, ran);
			Set and = new HashSet();
			and.add(thing);
			and.add(ontDF.getOWLNot(forallPR));
			satClazz = ontDF.getOWLAnd(and);
			sos = this.getSOS(ontology, satClazz);
		}
		else if (titleCode.equals("P-RAN") && obj instanceof OWLObjectProperty) {
			// check sat. of (Thing ^ ~(\forall P.C))
			infRel = infObj + " rdfs:range " + infSubj;
			OWLObjectProperty prop = (OWLObjectProperty) obj;
			OWLClass ran = (OWLClass) entity;
			OWLDescription forallPR = null;
			OWLClass thing = ontDF.getOWLThing();
			forallPR = ontDF.getOWLObjectAllRestriction(prop, ran);
			Set and = new HashSet();
			and.add(thing);
			and.add(ontDF.getOWLNot(forallPR));
			satClazz = ontDF.getOWLAnd(and);
			sos = this.getSOS(ontology, satClazz);
		}
		else if (titleCode.equals("I-INS")) {
			infRel = infObj + " rdf:type " + infSubj;
			OWLClass cla = (OWLClass) entity;
			OWLIndividual ind = (OWLIndividual) obj;
			OWLDescription notC = ontDF.getOWLNot(cla);
			// temporarily add assertion to ontology for getting explanation
			AddIndividualClass ai = new AddIndividualClass(ontology, ind, notC, null);
			ai.accept((ChangeVisitor) ontology);
			restoreChanges.add(new RemoveIndividualClass(ontology, ind, notC, null));
			satClazz = notC;
			sos = this.getSOS(ontology, satClazz);
			// remove type assertion from sos
			for (Iterator iter = new HashSet(sos).iterator(); iter.hasNext();) {
				Object axiom = iter.next();
				if (axiom instanceof OWLIndividualTypeAssertion) {
					OWLIndividualTypeAssertion oit = (OWLIndividualTypeAssertion) axiom;
					if (oit.getIndividual().equals(ind) && oit.getType().equals(notC)) {
						sos.remove(axiom);
						break;
					}
				}
			}
		}
		else if (titleCode.equals("C-TYP")) {
			infRel = infSubj + " rdf:type " + infObj;
			OWLDescription cla = (OWLDescription) obj;
			OWLIndividual ind = (OWLIndividual) entity;
			OWLDescription notC = ontDF.getOWLNot(cla);
			// temporarily add assertion to ontology for getting explanation
			AddIndividualClass ai = new AddIndividualClass(ontology, ind, notC, null);
			ai.accept((ChangeVisitor) ontology);
			restoreChanges.add(new RemoveIndividualClass(ontology, ind, notC, null));
			satClazz = notC;
			sos = this.getSOS(ontology, satClazz);
			// remove type assertion from sos
			for (Iterator iter = new HashSet(sos).iterator(); iter.hasNext();) {
				Object axiom = iter.next();
				if (axiom instanceof OWLIndividualTypeAssertion) {
					OWLIndividualTypeAssertion oit = (OWLIndividualTypeAssertion) axiom;
					if (oit.getIndividual().equals(ind) && oit.getType().equals(notC)) {
						sos.remove(axiom);
						break;
					}
				}
			}
		}
		else if (titleCode.startsWith(("P-INSO")) && obj instanceof List) {
			// also obtain value from hash table
			List pvTuple = (List) obj;
			OWLIndividual value = (OWLIndividual) pvTuple.get(1);
			OWLIndividual ind = (OWLIndividual) entity;
			OWLObjectProperty prop = (OWLObjectProperty) pvTuple.get(0);
			// \exists R \neg {o}
			//TODO: infRel = infSubj + " subClassOf " + infObj;
			Set one = new HashSet();
			one.add(value);
			OWLEnumeration oneOf = ontDF.getOWLEnumeration(one);
			OWLDescription negOne = ontDF.getOWLNot(oneOf);
			OWLDescription existsNeg = ontDF.getOWLObjectAllRestriction(prop, negOne);
			AddIndividualClass ai = new AddIndividualClass(ontology, ind, existsNeg, null);
			restoreChanges.add(new RemoveIndividualClass(ontology, ind, existsNeg, null));
			ai.accept((ChangeVisitor) ontology);
			satClazz = existsNeg;
			sos = this.getSOS(ontology, satClazz);
			// remove type assertion from sos
			for (Iterator iter = new HashSet(sos).iterator(); iter.hasNext();) {
				Object axiom = iter.next();
				if (axiom instanceof OWLIndividualTypeAssertion) {
					OWLIndividualTypeAssertion oit = (OWLIndividualTypeAssertion) axiom;
					if (oit.getIndividual().equals(ind) && oit.getType().equals(existsNeg)) {
						sos.remove(axiom);
						break;
					}
				}
			}
		}
		else if (titleCode.startsWith(("I-SAM"))) {
			// add different from assertion
			infRel = infSubj + "&nbsp;"+ConciseFormat.EQU + "&nbsp;"+infObj;
			OWLClass temp1 = ontDF.getOWLClass(new URI("temp1"));
			AddEntity ae = new AddEntity(ontology, temp1, null);
			ae.accept((ChangeVisitor) ontology);
			OWLClass temp2 = ontDF.getOWLClass(new URI("temp2"));
			AddEntity ae2 = new AddEntity(ontology, temp2, null);
			ae2.accept((ChangeVisitor) ontology);
			Set enum1 = new HashSet();
			enum1.add((OWLIndividual) displayedEntity);
			OWLEnumeration en1 = ontDF.getOWLEnumeration(enum1);
			AddEquivalentClass aec = new AddEquivalentClass(ontology, temp1, en1, null);
			aec.accept((ChangeVisitor) ontology);
			restoreChanges.add(new RemoveEquivalentClass(ontology, temp1, en1, null));
			Set enum2 = new HashSet();
			enum2.add((OWLIndividual) obj);
			OWLEnumeration en2 = ontDF.getOWLEnumeration(enum2);
			aec = new AddEquivalentClass(ontology, temp2, en2, null);
			aec.accept((ChangeVisitor) ontology);
			restoreChanges.add(new RemoveEquivalentClass(ontology, temp2, en2, null));
			aec = new AddEquivalentClass(ontology, temp1, ontDF.getOWLNot(temp2), null);
			aec.accept((ChangeVisitor) ontology);
			restoreChanges.add(new RemoveEquivalentClass(ontology, temp1, ontDF.getOWLNot(temp2), null));
			restoreChanges.add(new RemoveEntity(ontology, temp1, null));
			restoreChanges.add(new RemoveEntity(ontology, temp2, null));
			satClazz = temp1;
			sos = this.getSOS(ontology, satClazz);
			// remove axioms contains temp1 or temp2 
			for (Iterator iter = new HashSet(sos).iterator(); iter.hasNext();) {
				OWLObject axiom = (OWLObject) iter.next();
				if (axiom instanceof OWLEquivalentClassesAxiom) {
					OWLEquivalentClassesAxiom equ = (OWLEquivalentClassesAxiom) axiom;
					boolean remove = false;
					for (Iterator iter2 = equ.getEquivalentClasses().iterator(); iter2.hasNext();) {
						OWLDescription desc = (OWLDescription) iter2.next();
						if (desc instanceof OWLClass && ( ((OWLClass) desc).getURI().equals(new URI("temp1")) || ((OWLClass) desc).getURI().equals(new URI("temp2")) )) {
							remove = true;
							break;
						}
					}
					if (remove) sos.remove(axiom);
				}
 			}
		}
		else if (titleCode.startsWith(("I-DIF"))) {
			// add sameAs assertion
			infRel = infSubj + "&nbsp;"+ConciseFormat.DISJOINT + "&nbsp;"+infObj;
			OWLClass temp1 = ontDF.getOWLClass(new URI("temp1"));
			AddEntity ae = new AddEntity(ontology, temp1, null);
			ae.accept((ChangeVisitor) ontology);
			OWLClass temp2 = ontDF.getOWLClass(new URI("temp2"));
			AddEntity ae2 = new AddEntity(ontology, temp2, null);
			ae2.accept((ChangeVisitor) ontology);
			Set enum1 = new HashSet();
			enum1.add((OWLIndividual) displayedEntity);
			OWLEnumeration en1 = ontDF.getOWLEnumeration(enum1);
			AddEquivalentClass aec = new AddEquivalentClass(ontology, temp1, en1, null);
			aec.accept((ChangeVisitor) ontology);
			restoreChanges.add(new RemoveEquivalentClass(ontology, temp1, en1, null));
			Set enum2 = new HashSet();
			enum2.add((OWLIndividual) obj);
			OWLEnumeration en2 = ontDF.getOWLEnumeration(enum2);
			aec = new AddEquivalentClass(ontology, temp2, en2, null);
			aec.accept((ChangeVisitor) ontology);
			restoreChanges.add(new RemoveEquivalentClass(ontology, temp2, en2, null));
			aec = new AddEquivalentClass(ontology, temp1, temp2, null);
			aec.accept((ChangeVisitor) ontology);
			restoreChanges.add(new RemoveEquivalentClass(ontology, temp1, temp2, null));
			restoreChanges.add(new RemoveEntity(ontology, temp1, null));
			restoreChanges.add(new RemoveEntity(ontology, temp2, null));
			satClazz = temp1;
			sos = this.getSOS(ontology, satClazz);
			// remove axioms containing temp1 or temp2
			for (Iterator iter = new HashSet(sos).iterator(); iter.hasNext();) {
				Object axiom = iter.next();
				if (axiom instanceof OWLEquivalentClassesAxiom) {
					OWLEquivalentClassesAxiom equ = (OWLEquivalentClassesAxiom) axiom;
					boolean remove = false;
					for (Iterator iter2 = equ.getEquivalentClasses().iterator(); iter2.hasNext();) {
						OWLDescription desc = (OWLDescription) iter2.next();
						if (desc instanceof OWLClass && ( ((OWLClass) desc).getURI().equals(new URI("temp1")) || ((OWLClass) desc).getURI().equals(new URI("temp2")) )) {
							remove = true;
							break;
						}
					}
					if (remove) sos.remove(axiom);
				}
			}
		}
		else {
			List sosStr = new ArrayList();
			sosStr.add("No explanation support yet for this inference.");
			sosStr.add("No explanation support yet for this inference.");
			return sosStr;
		}
		
		// save current print writer
		PrintWriter copy = new PrintWriter(pw);
		// create new temp writer to render axioms
		StringWriter st = new StringWriter();
		PrintWriter buffer = new PrintWriter(st);
		setWriter(buffer);
		// Note: choice of base below only effects ordering
		OWLNamedObject base = null;
		if (entity instanceof OWLNamedObject) base = (OWLNamedObject) entity;
		else if (obj instanceof OWLNamedObject) base = (OWLNamedObject) obj;
		else base = ontDF.getOWLThing();
		printExplanationSOS(sos, base, false);
		String sosStr1 = st.toString();
		sosStr1 = sosStr1.replaceAll("problem", "inference <br><font color=\"green\">"+infRel+"</font>");
		// call printing again but turn highlighting on
		st = new StringWriter();
		buffer = new PrintWriter(st);
		setWriter(buffer);
		printExplanationSOS(sos, base, true);
		String sosStr2 = st.toString();
		sosStr2 = sosStr2.replaceAll("problem", "inference <br><font color=\"green\">"+infRel+"</font>");
		
		// restore writer
		this.pw = copy;
		// restore highlightedSet in visitor to empty
		((ConciseFormatVisitor) visitor).highlightMap.clear();
		// also restore ontology to initial state if changed
		for (Iterator iter = restoreChanges.iterator(); iter.hasNext();) {
			OntologyChange res = (OntologyChange) iter.next();
			res.accept((ChangeVisitor) ontology);	
		}
		
		List sosStr = new ArrayList();
		sosStr.add(sosStr1);
		sosStr.add(sosStr2);
		return sosStr;
	}
	
	public String getSudokuExplanation(OWLOntology gridOnt, OWLIndividual ind, OWLClass cla) {
		
		try {
			OWLOntology ontology = swoopModel.cloneOntology(gridOnt);
			ind = ontology.getIndividual(ind.getURI());
			cla = ontology.getClass(cla.getURI());
			OWLDescription notC = ontology.getOWLDataFactory().getOWLNot(cla);
			// temporarily add assertion to ontology for getting explanation
			AddIndividualClass ai = new AddIndividualClass(ontology, ind, notC, null);
			ai.accept((ChangeVisitor) ontology);
			OWLDescription satClazz = notC;
			Set sos = this.getSOS(ontology, satClazz);
			// remove type assertion from sos
			for (Iterator iter = new HashSet(sos).iterator(); iter.hasNext();) {
				Object axiom = iter.next();
				if (axiom instanceof OWLIndividualTypeAssertion || axiom instanceof OWLSubClassAxiom) {
					sos.remove(axiom);					
				}
			}
			// create new temp writer to render axioms
			StringWriter st = new StringWriter();
			PrintWriter buffer = new PrintWriter(st);
			setWriter(buffer);
			// Note: choice of base below only effects ordering
			OWLNamedObject base = null;
			if (entity instanceof OWLNamedObject) base = (OWLNamedObject) entity;
			else if (cla instanceof OWLNamedObject) base = (OWLNamedObject) cla;
			else base = ontology.getOWLDataFactory().getOWLThing();
			printExplanationSOS(sos, base, false);
			String sosStr = st.toString();
			sosStr = sosStr.replaceAll("problem", "inference");
			sosStr = "<html><font face=\"Verdana\" SIZE=2>"+sosStr+"</font></html>";
			
			return sosStr;
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		return "";
	}
	public List handleEditLink( String hLink ) throws OWLException {
	    List changes = new ArrayList();
		OWLOntology ontology = reasoner.getOntology();
	    
		// parse EDIT hyper-link
		int pos1 = hLink.indexOf(":");
		int pos2 = hLink.indexOf(":", pos1 + 1);
		String hashCode = hLink.substring(pos1 + 1, pos2);
		String titleCode = hLink.substring(pos2 + 1, hLink.length());
		Object obj = OWLDescHash.get(hashCode);
		
		//System.out.println( "CODE: " + titleCode );
		
		if ( titleCode.startsWith( "RULE" ) ) {
		    System.out.println( "\nEDITING RULE\n" );
		    
		    OWLRule rule = (OWLRule) obj;
		    
		    PopupAddRule test = new PopupAddRule( swoopModel, rule );
		    test.show();
		    
		}
		
		return changes;
	}
	
	/*
	 *  SwoopRenderer method
	 * 
	 */
	public Component getDisplayComponent( SwoopDisplayPanel panel )
	{		
		if (!(panel instanceof TermsDisplay ))
			throw new IllegalArgumentException();
		
		return getEditorPane( this.getContentType(), (TermsDisplay)panel );
	}
	
	
	
	private void deleteFromBooleanDesc(OWLOntology ontology, OWLClass displayedClass, OWLDescription desc, Class type, List changes) throws OWLException {
		
		BooleanElementChange change = new BooleanElementChange(type, "Remove", ontology, displayedClass, desc, null);
		changes.add(change);
	}	
		
	/** Accepts an ontology, class, individual (changes) ; 
	 * Removes the enumeration on class that contains individual
	 * Adds a new enumeration without the individual
	 * Records changes
	*/
	private void updateEnumerations(OWLOntology ontology, OWLClass displayedClass, OWLIndividual desc, List changes) throws OWLException {
		
		EnumElementChange change = new EnumElementChange("Remove", ontology, displayedClass, desc, null);
		changes.add(change);
	}

	public boolean isEditableText() {
		return false;
	}
	
	/*
	 * For debugging purposes: sort axioms in explanation set to explain trace better
	 */
	private List sortAxioms(URI baseURI, Map LHSmap, Map RHSmap, Set removed, int level) {
		List sort = new ArrayList();
		try {
			for (Iterator iter = LHSmap.keySet().iterator(); iter.hasNext();) {
				OWLObject axiom = (OWLObject) iter.next();
				List LHS = (List) LHSmap.get(axiom);
				for (Iterator iter2 = LHS.iterator(); iter2.hasNext();) {
					OWLNamedObject term = (OWLNamedObject) iter2.next();
					if (baseURI==null || term.getURI().equals(baseURI)) {
						
						// match found, add axiom to source, tag removal
						if (!removed.contains(axiom)) {
							sort.add(axiom);
							sortLevelMap.put(axiom, String.valueOf(level));
						}
						
						removed.add(axiom);
						
						// and remove from LHS/RHS maps
						Map newLHSmap = new HashMap(LHSmap);
						newLHSmap.remove(axiom);
						Map newRHSmap = new HashMap(RHSmap);
						newRHSmap.remove(axiom);
						// now check RHS and move baseURI here if possible
						List RHS = (List) RHSmap.get(axiom);
						URI newBaseURI = null;
						if (!RHS.isEmpty()) {
							for (int rctr=0; rctr < RHS.size(); rctr++) {
								newBaseURI = ((OWLNamedObject) RHS.get(rctr)).getURI();
								sort.addAll(sortAxioms(newBaseURI, newLHSmap, newRHSmap, removed, level+1));								
							}								
						}
						else {
							// else move to element in LHS
							List modLHS = new ArrayList(LHS);
							modLHS.remove(term);
							for (int lctr=0; lctr < modLHS.size(); lctr++) {
								newBaseURI = ((OWLNamedObject) modLHS.get(lctr)).getURI();
								sort.addAll(sortAxioms(newBaseURI, newLHSmap, newRHSmap, removed, level+1));								
							}
						}						
					}
				}
			}
//			// check for remaining axioms
//			for (Iterator iter = removed.iterator(); iter.hasNext();) {
//				OWLObject axiom = (OWLObject) iter.next();
//				LHSmap.remove(axiom);
//				RHSmap.remove(axiom);
//			}
//			if (LHSmap.keySet().size()>0) {
//				sort.addAll(sortAxioms(null, LHSmap, RHSmap, new HashSet()));
//			}

		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		return sort;
	}
	
	/*
	 * Used to prune explanation set obtained from Pellet
	 */
	public boolean checkSatisfiability(Set axioms, OWLDescription clazz) {
		// create a new ontology with axioms
		// and check satisfiability of clazz
		boolean sat = false;
		try {
			OWLOntBuilder ontBuilder = new OWLOntBuilder();
			
			// create new ontology using axioms
			// use OWLOntBuilder to build a new ontology given axioms
			for (Iterator iter = axioms.iterator(); iter.hasNext();) {
				OWLObject obj = (OWLObject) iter.next();
				obj.accept(ontBuilder);
			}
			
			// if clazz is not in ontology, return true
			OWLOntology newOnt = ontBuilder.currentOnt;
			if (clazz!=null && clazz instanceof OWLClass && newOnt.getClass(((OWLClass) clazz).getURI())==null) return true;
			else if (clazz!=null) {
				// get clazz in newOnt
				clazz = ontBuilder.visitDescription(clazz);				
			}
			
			// create new instance of pellet and check sat. of clazz
			PelletReasoner newPellet = new PelletReasoner();
			newPellet.setOntology(newOnt, false);
			if (clazz!=null) sat = newPellet.isConsistent(clazz);
			else sat = newPellet.isConsistent();
			// TESTING:
			// also add clazz to some ind type
//			try {
//				OWLIndividual dummy = newOnt.getOWLDataFactory().getOWLIndividual(new URI("dummy"));
//				AddEntity ae = new AddEntity(newOnt, dummy, null);
//				ae.accept((ChangeVisitor) newOnt);
//				AddIndividualClass aic = new AddIndividualClass(newOnt, dummy, clazz, null);
//				aic.accept((ChangeVisitor) newOnt);
//			}
//			catch (Exception ex) {
//				ex.printStackTrace();
//			}
//			System.out.println(clazz + "satisfiability: " + sat);
//			CorrectedRDFRenderer rdf = new CorrectedRDFRenderer();
//			StringWriter w  = new StringWriter();
//			rdf.renderOntology(newOnt, null, w);
//			System.out.println(w.toString());
		}
		catch (Exception ex) {	
			System.out.println(ex.getMessage()); // clazz (description) may not be in ontology!
//			ex.printStackTrace();
			return true;
		}
		
		return sat;
	}
	
	private boolean checkSatisfiability(OWLOntBuilder ob, OWLDescription clazz) {
		// check satisfiability of clazz in ont in ob
		OWLOntology newOnt = ob.currentOnt;
		boolean sat = false;
		try {
			if (clazz!=null && clazz instanceof OWLClass && newOnt.getClass(((OWLClass) clazz).getURI())==null) return true;
			else if (clazz!=null) {
				// get clazz in newOnt
				clazz = ob.visitDescription(clazz);
			}
			
			
			// create new instance of pellet and check sat. of clazz
			PelletReasoner newPellet = new PelletReasoner();
			newPellet.setOntology(newOnt, false);
			Timer parseTimer = new Timer("parse time");
			parseTimer.start();
			if (clazz != null)
				sat = newPellet.isConsistent(clazz);
			else
				sat = newPellet.isConsistent();
			parseTimer.stop();
			parseTime += parseTimer.getTotal();		        
			
		}
		catch (Throwable ex) {	
			System.out.println(ex.getMessage()); // clazz (description) may not be in ontology!
			ex.printStackTrace();
			return true;
		}
		
		return sat;
	}
	
	/*
	 * check if entity is in ont, if not, create new entity and return it
	 */
	private OWLEntity checkEntity(OWLOntology ont, OWLEntity entity) {
		try {
			AddEntity ae = null;
			OWLDataFactory ontDF = ont.getOWLDataFactory();
			if (entity instanceof OWLClass) {
				if (ont.getClass(entity.getURI())==null) {
					entity = ontDF.getOWLClass(entity.getURI());
					ae = new AddEntity(ont, entity, null);
				}
				else entity = ont.getClass(entity.getURI());
			}
			else if (entity instanceof OWLDataProperty) {
				if (ont.getDataProperty(entity.getURI())==null) {
					entity = ontDF.getOWLDataProperty(entity.getURI());
					ae = new AddEntity(ont, entity, null);
				}
				else entity = ont.getDataProperty(entity.getURI());
			}
			else if (entity instanceof OWLObjectProperty) {
				if (ont.getObjectProperty(entity.getURI())==null) {
					entity = ontDF.getOWLObjectProperty(entity.getURI());
					ae = new AddEntity(ont, entity, null);
				}
				else entity = ont.getObjectProperty(entity.getURI());
			}
			else if (entity instanceof OWLIndividual) {
				if (ont.getIndividual(entity.getURI())==null) {
					entity = ontDF.getOWLIndividual(entity.getURI());
					ae = new AddEntity(ont, entity, null);
				}
				else entity = ont.getIndividual(entity.getURI());
			}
			if (ae!=null) ae.accept((ChangeVisitor) ont);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		return entity;
	}
	
	/*
	 * Prune the explanation set and print axioms
	 */
	public void printExplanationSOS(Set explanationSet, OWLNamedObject entity, boolean enableHighlights) {
		
		Set strikeSet = new HashSet();
		try {
			if(explanationSet != null && !explanationSet.isEmpty()) {
				
			    println("<b>Axioms causing the problem:</b>");
			    // need to sort axioms to explain trace better
			    Iterator i = explanationSet.iterator();
			    // create instance of object container to check contents of axiom
			    OWLObjectContainer owlCont = null;
			    owlCont = new OWLObjectContainer(entity);
				
			    Map LHSmap = new HashMap();
			    Map RHSmap = new HashMap();
			    Map objectCount = new HashMap();
			    while(i.hasNext()) {
			        OWLObject axiom = (OWLObject) i.next();
			        owlCont.reset();
			        // visit axiom
			        owlCont.visit(axiom);
			        // get contained objects, LHS and RHS of axiom
			        List objects = owlCont.getContainedObjects();			        
			        List LHS = owlCont.getLHS();
			        List RHS = owlCont.getRHS();
			        if (LHS.size()==0 && RHS.size()==0) LHS = objects;
			        LHSmap.put(axiom, LHS);
			        RHSmap.put(axiom, RHS);
			        List total = new ArrayList(LHS);
			        total.addAll(RHS);
			        updateCount(objectCount, total);
			    }
			    
			    // highlight based on entity count
			    if (enableHighlights) {
				    for (Iterator iter=objectCount.keySet().iterator(); iter.hasNext();) {
				    	URI objURI = (URI) iter.next();
				    	String valStr = objectCount.get(objURI).toString();
				    	int val = Integer.parseInt(valStr);
				    	// System.out.println(objURI + ":"+ val);
				    	if (val==1 && !((ConciseFormatVisitor) visitor).highlightMap.containsKey(objURI)) { 
				    		((ConciseFormatVisitor) visitor).highlightMap.put(objURI, "<strike><font color=\"red\">");
				    		strikeSet.add(objURI);
				    	}
				    }
			    }
			    
			    // now call sorting method after computing lhs/rhs of axioms
			    URI baseURI = entity.getURI();
			    
			    int count = 1;
			    sortLevelMap = new HashMap(); // reset map which stores links between sorted axioms in their resp level (recursive depth)
			    do {
			    	List axioms = sortAxioms(baseURI, LHSmap, RHSmap, new HashSet(), 0);
			    	// now print sorted axiom list
				    for (Iterator iter = axioms.iterator(); iter.hasNext();) {
				    	
				    	OWLObject axiom = (OWLObject) iter.next();
				    	int level = Integer.parseInt(sortLevelMap.get(axiom).toString());
				    	print("<b>" + (count++) + ")</b> ");
				    	for (int ctr=0; ctr<level*2; ctr++) print("&nbsp;");
				    	if (level>0) print("|_");
				        printObject(axiom);
				        println();
				        
				        LHSmap.remove(axiom);
		    			RHSmap.remove(axiom);
				    }
			    	baseURI = null;
			    }
			    while ((count-1)<explanationSet.size());				    					    
			}
			print("<hr>");						
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		finally {
			// clear highlight map of all strikes
			((ConciseFormatVisitor) visitor).highlightMap.keySet().removeAll(strikeSet);
		}
	}
	 
	/*
	 * Prune the explanation set and print axioms
	 */
	public void printRepairSOS(Set explanationSet, OWLNamedObject entity, boolean enableHighlights, RepairFrame repair) {
		
		Set strikeSet = new HashSet();
		String insFont = "<FONT FACE=\""+swoopModel.getFontFace()+"\" SIZE=3>";
		String tab = "_____"; //&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
		try {
			if(explanationSet != null && !explanationSet.isEmpty()) {
				
			    println("<b>Axioms causing the problem:</b>");
			    // need to sort axioms to explain trace better
			    Iterator i = explanationSet.iterator();
			    // create instance of object container to check contents of axiom
			    OWLObjectContainer owlCont = null;
			    owlCont = new OWLObjectContainer(entity);
				
			    Map LHSmap = new HashMap();
			    Map RHSmap = new HashMap();
			    Map objectCount = new HashMap();
			    while(i.hasNext()) {
			        OWLObject axiom = (OWLObject) i.next();
			        owlCont.reset();
			        // visit axiom
			        owlCont.visit(axiom);
			        // get contained objects, LHS and RHS of axiom
			        List objects = owlCont.getContainedObjects();			        
			        List LHS = owlCont.getLHS();
			        List RHS = owlCont.getRHS();
			        if (LHS.size()==0 && RHS.size()==0) LHS = objects;
			        LHSmap.put(axiom, LHS);
			        RHSmap.put(axiom, RHS);
			        List total = new ArrayList(LHS);
			        total.addAll(RHS);
			        updateCount(objectCount, total);
			    }
			    
			    // highlight based on entity count
			    if (enableHighlights) {
				    for (Iterator iter=objectCount.keySet().iterator(); iter.hasNext();) {
				    	URI objURI = (URI) iter.next();
				    	String valStr = objectCount.get(objURI).toString();
				    	int val = Integer.parseInt(valStr);
				    	// System.out.println(objURI + ":"+ val);
				    	if (val==1 && !((ConciseFormatVisitor) visitor).highlightMap.containsKey(objURI)) { 
				    		((ConciseFormatVisitor) visitor).highlightMap.put(objURI, "<strike><font color=\"red\">");
				    		strikeSet.add(objURI);
				    	}
				    }
			    }
			    
			    // now call sorting method after computing lhs/rhs of axioms
			    URI baseURI = entity.getURI();
			    
			    int count = 1;
			    sortLevelMap = new HashMap(); // reset map which stores links between sorted axioms in their resp level (recursive depth)
			    print("</b><hr><table border=\"1\">");
				print("<tr><td>"+insFont+"<b>Erroneous Axioms</b></td><td>"+insFont+"<b><a href=\":ARITY\">Arity</a></b></td><td>"+insFont+"<b><a href=\":IMPACT\">Impact</a></b></td><td>"+insFont+"<b><a href=\":USAGE\">Usage</a></b></td><td>"+insFont+"<b><a href=\":RANK\">Rank</a></b></td>"+insFont+"<b>Status</b></td></tr>");
			    do {
			    	List axioms = sortAxioms(baseURI, LHSmap, RHSmap, new HashSet(), 0);
			    	// now print sorted axiom list
			    	for (Iterator iter = axioms.iterator(); iter.hasNext();) {
				    	
			    		OWLObject axiom = (OWLObject) iter.next();
				    	
			    		if (repair.keptAxiomSet.contains(axiom)) print("<tr bgcolor=\"C3FDB8\"><td>");
			    		else if (repair.removedAxiomSet.contains(axiom)) print("<tr bgcolor=\"FFDDDD\"><td>");
			    		else print("<tr><td>");
			    		print(insFont);
				    	
				    	int level = Integer.parseInt(sortLevelMap.get(axiom).toString());
				    	print("<b>" + (count++) + ")</b> ");
				    	for (int ctr=0; ctr<level*2; ctr++) print("&nbsp;");
				    	if (level>0) print("|_");
				    	
				    	int arity = ((HashSet) repair.axiomUnsatClaMap.get(axiom)).size();
						String color = "FFFFFF";
						if (arity==2) color = "FFFF88";
						else if (arity==3) color = "FFFF44";
						else if (arity>=4) color = "FFFF22";						
						print("<font size = 3 style=\"BACKGROUND-COLOR:"+color+"\">");
						
						printObject(axiom);
						print("</td>");
//				    	
						String hash = String.valueOf(repair.axiomUnsatClaMap.get(axiom).hashCode());
						repair.objectMap.put(hash, repair.axiomUnsatClaMap.get(axiom));
						print("<td>"+insFont+"<a href=\":HASH:Arity:"+hash+"\">"+String.valueOf(arity)+"</a></td>");
						
						int impact = 0;
						Set impactSet = new HashSet();
						if (repair.axiomSOSMap.containsKey(axiom)) impactSet = (HashSet) repair.axiomSOSMap.get(axiom);
						impact = impactSet.size();
						hash = String.valueOf(impactSet.hashCode());
						repair.objectMap.put(hash, impactSet);
						print("<td>"+insFont+"<a href=\":HASH:Impact:"+hash+"\">" + String.valueOf(impact)+"</a></td>");
						
						int usage = ((HashSet) repair.axiomUsageMap.get(axiom)).size();
						hash = String.valueOf(repair.axiomUsageMap.get(axiom).hashCode());
						repair.objectMap.put(hash, repair.axiomUsageMap.get(axiom));
						print("<td>"+insFont+"<a href=\":HASH:Usage:"+hash+"\">" + String.valueOf(usage)+"</a></td>");
						
						String rank = "-";
						if (repair.axiomRanksMap.containsKey(axiom)) rank = repair.axiomRanksMap.get(axiom).toString();
						print("<td>"+insFont+rank+"</td>");
				        
				        String rem = "R";
						if (repair.removedAxiomSet.contains(axiom)) rem="Undo";
						print("<td>"+insFont+"<font color = \"red\">[<a href=\":FORCE:"+axiom.hashCode()+"\">"+rem+"</a>]&nbsp;</font>");
						
						String keep = "K";
						if (repair.keptAxiomSet.contains(axiom)) keep = "Undo";
						print(insFont+"<font color = \"green\">[<a href =\":BLOCK:"+axiom.hashCode()+"\">"+keep+"</a>]&nbsp;</font></td>");
						print("</tr>");
				        
				        LHSmap.remove(axiom);
		    			RHSmap.remove(axiom);
				    }
			    	baseURI = null;
			    }
			    while ((count-1)<explanationSet.size());
			    print("</table>");
			}
			print("<hr>");						
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		finally {
			// clear highlight map of all strikes
			((ConciseFormatVisitor) visitor).highlightMap.keySet().removeAll(strikeSet);
		}
	}
	
	// also used in Why? explanation in Repair Frame :/
	public void printRepairSOS(Set explanationSet, OWLNamedObject entity, RepairFrame repair) {
		
		Set strikeSet = new HashSet();
		String insFont = "<FONT FACE=\""+swoopModel.getFontFace()+"\" SIZE=3>";
		try {
			if(explanationSet != null && !explanationSet.isEmpty()) {
				
			    println("<b>Axioms causing the problem:</b>");
			    // need to sort axioms to explain trace better
			    Iterator i = explanationSet.iterator();
			    // create instance of object container to check contents of axiom
			    OWLObjectContainer owlCont = null;
			    owlCont = new OWLObjectContainer(entity);
				
			    Map LHSmap = new HashMap();
			    Map RHSmap = new HashMap();
			    Map objectCount = new HashMap();
			    while(i.hasNext()) {
			        OWLObject axiom = (OWLObject) i.next();
			        owlCont.reset();
			        // visit axiom
			        owlCont.visit(axiom);
			        // get contained objects, LHS and RHS of axiom
			        List objects = owlCont.getContainedObjects();			        
			        List LHS = owlCont.getLHS();
			        List RHS = owlCont.getRHS();
			        if (LHS.size()==0 && RHS.size()==0) LHS = objects;
			        LHSmap.put(axiom, LHS);
			        RHSmap.put(axiom, RHS);
			        List total = new ArrayList(LHS);
			        total.addAll(RHS);
			        updateCount(objectCount, total);
			    }
			    
			    // now call sorting method after computing lhs/rhs of axioms
			    URI baseURI = entity.getURI();
			    
			    int count = 1;
			    sortLevelMap = new HashMap(); // reset map which stores links between sorted axioms in their resp level (recursive depth)
			    do {
			    	List axioms = sortAxioms(baseURI, LHSmap, RHSmap, new HashSet(), 0);
			    	// now print sorted axiom list
			    	for (Iterator iter = axioms.iterator(); iter.hasNext();) {
				    	
			    		OWLObject axiom = (OWLObject) iter.next();
				    	
			    		print(insFont);
				    	
				    	int level = Integer.parseInt(sortLevelMap.get(axiom).toString());
				    	print("<b>" + (count++) + ")</b> ");
				    	for (int ctr=0; ctr<level*2; ctr++) print("&nbsp;");
				    	if (level>0) print("|_");
				    	
				    	if (repair.planSolnAxioms[0].contains(axiom)) print("<font color=\"red\"><b>[X]</b></font>");
				    	printObject(axiom);
						print("<br>");
						
				        LHSmap.remove(axiom);
		    			RHSmap.remove(axiom);
				    }
			    	baseURI = null;
			    }
			    while ((count-1)<explanationSet.size());			    
			}									
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	// expand axiom set iteratively 
	public Set expandAxiomSet(Set axioms, OWLOntology ont, boolean expandMore) {
		
		try {
			Set newEntities = new HashSet();
			
			for (Iterator iter = new HashSet(axioms).iterator(); iter.hasNext();) {
				OWLObject axiom = (OWLObject) iter.next();
				if (signatureMap.containsKey(axiom)) {
					newEntities.addAll((HashSet) signatureMap.get(axiom));
				}
				else {
					Set ents = swoopModel.getAxiomSignature(axiom, ont);
					signatureMap.put(axiom, ents);
					newEntities.addAll(ents);					
				}
				
				if (expandMore) {
					// expand entity set to include usage entities
					for (Iterator iter2 = new HashSet(newEntities).iterator(); iter2.hasNext();) {
						Set usage = new HashSet();
						OWLEntity ent = (OWLEntity) iter2.next();
						// check in local cache first before using OntologyHelper
						if (usageMap.containsKey(ent.getURI())) {
							usage = (HashSet) usageMap.get(ent.getURI());
						}
						else {	
							usage = OntologyHelper.entityUsage(ont, ent);
							usageMap.put(ent.getURI(), usage);
						}
						// only add entities because axioms are returned
						for (Iterator it = usage.iterator(); it.hasNext();) {
							Object e = it.next();
							if (e instanceof OWLEntity) newEntities.add(e);
							else if (e instanceof OWLObject) {
								if (signatureMap.containsKey(e)) {
									newEntities.addAll((HashSet) signatureMap.get(e));
								}
								else {
									Set ents = swoopModel.getAxiomSignature((OWLObject) e, ont);
									signatureMap.put(e, ents);
									newEntities.addAll(ents);					
								}
							}
						}
					}
				}
			}
			
			// get axioms for all newEntities either from local cache or from swoopModel
			Set newAxioms = new HashSet();
			for (Iterator iter2 = newEntities.iterator(); iter2.hasNext();) {
				OWLEntity ent = (OWLEntity) iter2.next();
				if (axiomMap.containsKey(ent.getURI())) {
					newAxioms.addAll((HashSet) axiomMap.get(ent.getURI()));
				}
				else {
					Set ax = swoopModel.getAxioms(ent, ont);
					axiomMap.put(ent.getURI(), ax);
					newAxioms.addAll(ax);
				}								
			}
			
			if (axioms.containsAll(newAxioms)) {
				return new HashSet();
			}
			else {
				// determine latest axioms
				Set before = new HashSet(axioms);
				Set latest = new HashSet(axioms);
				latest.addAll(newAxioms);
				latest.removeAll(before);
//				// set a limit on axioms to be added
				if (latest.size()>axiomLimit) {
					// only let limited entities remain in latest
					Set copyLatest = new HashSet(latest);
					latest.clear();
					for (int ctr = 0; ctr < axiomLimit; ctr++) {
						Object ax = copyLatest.iterator().next();
						latest.add(ax);
						copyLatest.remove(ax);
					}
					axiomLimit *= 1.25; // slowly increase axiom limit
				}
				newAxioms = latest;
				axioms.addAll(newAxioms);
				return newAxioms;
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		return new HashSet();
	}
	
	/*
	 * find a single MUPS of an unsatisfiable class using the black box method
	 */
	public Set getBlackBoxSOS(OWLOntology ont, OWLClass cla) {
		
		// first check if cla is indeed unsatisfiable in ont
		try {
			PelletReasoner newPellet = new PelletReasoner();
			newPellet.setOntology(ont, false);
			if (cla!=null && newPellet.isConsistent(cla)) {
				System.out.println(swoopModel.shortForm(cla.getURI())+" is satisfiable in "+swoopModel.shortForm(ont.getURI()));
				return new HashSet();
			}
//			else if (cla==null && newPellet.isConsistent()) {
//				System.out.println(swoopModel.shortForm(ont.getURI())+" is consistent");
//				return new HashSet();
//			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		Set sos = new HashSet();
		axiomLimit = 40;
		usageMap.clear();
		axiomMap.clear();
		signatureMap.clear();
		
		Timer sosTimer = new Timer("find SOS black-box");
		if (!swoopModel.findAllMUPS) parseTime = 0;
		sosTimer.start(); 
		try {
			// add axioms related to class 
			Set axioms = swoopModel.getAxioms(cla, ont);
			axiomMap.put(cla.getURI(), axioms);
			OWLOntBuilder ob = new OWLOntBuilder();
			ob.addAxiom = true; // add axiom mode set to true
			
			// add all base axioms to testOnt via ob
			for (Iterator iter = axioms.iterator(); iter.hasNext();) {
				OWLObject axiom = (OWLObject) iter.next();
				axiom.accept(ob);
			}
			
			// toggle a variable that considers entity usage while expanding axiom set
			boolean expandMore = false;
			
			// add linked references iteratively
			while (checkSatisfiability(ob, cla)) {
				
				Set newAxioms = this.expandAxiomSet(axioms, ont, expandMore);
//				System.out.println("Size of axioms: "+axioms.size());
				
				// add axioms from latest to testOnt
				for (Iterator it = newAxioms.iterator(); it.hasNext();) {
					OWLObject axiom = (OWLObject) it.next(); 
					axiom.accept(ob);					
				}
				
				if (newAxioms.isEmpty() && expandMore) {
					System.out.println("ERROR: Could not find axioms responsible for error!");
					CorrectedRDFRenderer rdfRend = new CorrectedRDFRenderer();
					StringWriter st = new StringWriter();
					rdfRend.renderOntology(ob.currentOnt, st);
					FileWriter fw = new FileWriter("testSOS.owl");
					fw.write(st.toString());
					fw.close();
					System.out.println("Saved Test Ontology: testSOS.owl");
					return sos; 
				}
				else if (newAxioms.isEmpty()) {
					expandMore = true;
				}
//				System.out.println(axioms);
			}
			
			// now axioms contains cla unsatisfiable
			// remove one axiom at a time and if it turns satisfiable, add it to sos
//			System.out.println("Found concept unsatisfiable: #axioms = "+axioms.size());
			
			// fast pruning 
			List axiomList = new ArrayList(axioms);
			int pruneWindow = 10;
			if (axiomList.size()>pruneWindow) {
				axioms.clear();
				int parts = axiomList.size() / pruneWindow;
				for (int part=0; part<parts; part++) {
					for (int i=part*pruneWindow; i<part*pruneWindow+pruneWindow; i++) {
						ob.addAxiom = false;
						((OWLObject)axiomList.get(i)).accept(ob);
					}
					if (checkSatisfiability(ob, cla)) {
						for (int i=part*pruneWindow; i<part*pruneWindow+pruneWindow; i++) {
							axioms.add(axiomList.get(i));
							ob.addAxiom = true;
							((OWLObject)axiomList.get(i)).accept(ob);
						}
					}
				}
				if (axiomList.size()>parts*pruneWindow) {
					// add remaining from list to axioms
					for (int i=parts*pruneWindow; i<axiomList.size(); i++) {
						axioms.add(axiomList.get(i));
					}
				}
			}
			
//			System.out.println("After pruning axioms quickly: #axioms = "+axioms.size());
			
			// slow pruning
			for (Iterator iter = new HashSet(axioms).iterator(); iter.hasNext();) {
				OWLObject axiom = (OWLObject) iter.next();
				axioms.remove(axiom);
				ob.addAxiom = false;
				axiom.accept(ob);
				if (checkSatisfiability(ob, cla)) {
					sos.add(axiom);
					axioms.add(axiom);
					ob.addAxiom = true;
					axiom.accept(ob);
				}
//				System.out.println("Size of axioms: "+axioms.size());
			}
			sosTimer.stop();
			if (!swoopModel.findAllMUPS) {
				System.out.println(sosTimer);
				System.out.println("parse time: "+parseTime);
				System.out.println("net time: "+ String.valueOf(sosTimer.getTotal() - parseTime));
				netTime = sosTimer.getTotal() - parseTime;
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		return sos;
	}
	
	/*
	 * This method is mainly used by the explanations feature
	 * to obtain the SOS for any OWLDescription
	 */
	public Set getSOS(OWLOntology ontology, OWLDescription satClazz) {
		
		Set sos = new HashSet();
		if (!(satClazz instanceof OWLClass)) {
			try {
				// if its not an atomic class
				// create an atomic class and make it equivalent to the complex class
				OWLDataFactory ontDF = ontology.getOWLDataFactory();
				OWLClass tempCla = ontDF.getOWLClass(new URI("tempClass"));
				AddEntity ae = new AddEntity(ontology, tempCla, null);
				ae.accept((ChangeVisitor) ontology);
				Set equs = new HashSet();
				equs.add(tempCla);
				equs.add(satClazz);
				OWLEquivalentClassesAxiom equAxiom = ontDF.getOWLEquivalentClassesAxiom(equs);
				AddClassAxiom aca = new AddClassAxiom(ontology, equAxiom, null);
				aca.accept((ChangeVisitor) ontology);
				// get sos for new temp class
				if (swoopModel.isUseTableau()) {
					List sosExpl = getTableauSOS(ontology, tempCla);
					sos = (HashSet) sosExpl.get(1);
				}
				else {
					sos = getBlackBoxSOS(ontology, tempCla);
				}
				// remove equivalence axiom from sos
				sos.remove(equAxiom);
				// undo changes made to the ontology
				RemoveClassAxiom rec = new RemoveClassAxiom(ontology, equAxiom, null);
				rec.accept((ChangeVisitor) ontology);
				RemoveEntity re = new RemoveEntity(ontology, tempCla, null);
				re.accept((ChangeVisitor) ontology);
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		else {
			// directly find sos of atomic class 
			if (swoopModel.isUseTableau()) {
				List sosExpl = getTableauSOS(ontology, (OWLClass) satClazz);
				return (HashSet) sosExpl.get(1);
			}
			else return getBlackBoxSOS(ontology, (OWLClass) satClazz);
		}
		return sos;
	}
	
	/*
	 * For a specific ontology and clazz, use debugging version of
	 * Pellet to get string explanation and SOS
	 */
	public List getTableauSOS(OWLOntology ont, OWLDescription clazz) {
		List explSOS = new ArrayList();
		try {
			Reasoner pelletDebug = new Reasoner();
			pelletDebug.setOntology(ont);
			pelletDebug.getKB().setDoExplanation(true);
			pelletDebug.getKB().doDependencyTracking = true;
			boolean consistent = true;
			Timer timers = new Timer("Pellet Debugging Check");
		    timers.start();
			if (clazz!=null) consistent = pelletDebug.isConsistent(clazz);
			else consistent = pelletDebug.isConsistent();
			timers.stop();
			System.out.println(timers);
			if (consistent) {
				// no SOS cos ABox is consistent!
				System.out.println("No SOS since ABox is consistent");
				explSOS.add("No Explanation");
				explSOS.add(new HashSet()); // empty SOS
				return explSOS;
			}
			
			String explanation = ((PelletReasoner) reasoner).parseExplanation(this, pelletDebug.getKB().getExplanation());
			Set explanationSet = pelletDebug.getKB().getExplanationSet();
			
			// prune the axioms in case there are additional axioms
			Set prunedSet = new HashSet(explanationSet);
			for (Iterator iter = explanationSet.iterator(); iter.hasNext();) {
				OWLObject axiom = (OWLObject) iter.next();
				prunedSet.remove(axiom);
				boolean sat = false;
				sat = this.checkSatisfiability(prunedSet, clazz);
				if (sat) prunedSet.add(axiom);
			}
			explanationSet = prunedSet;
			// end of pruning
			
			explSOS.add(explanation);
			explSOS.add(explanationSet);			
		}
		catch (OWLException ex) {
			ex.printStackTrace();
		}
		return explSOS;
	}
	
	public void setWriter(Writer w) {
		this.pw = (PrintWriter) w;
	}
	
	/*
	 * update count for each entity appearing in an axiom
	 */
	private void updateCount(Map objectCount, List objects) {
		try {
//			Set once = new HashSet();
			for (Iterator iter = objects.iterator(); iter.hasNext();) {
				OWLNamedObject obj = (OWLNamedObject) iter.next();
//				if (once.contains(obj.getURI())) continue; ???
				int val = 1;
				if (objectCount.containsKey(obj.getURI())) {
					String valStr = objectCount.get(obj.getURI()).toString();
					val = Integer.parseInt(valStr)+1;
				}
//				once.add(obj.getURI());
				objectCount.put(obj.getURI(), String.valueOf(val));
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void setReasoner(SwoopReasoner reas) {
		this.reasoner = reas;
	}
	
	/*
	 * run all debugging tests w/ Pellet, RACER and KAON
	 * IMPORTANT NOTE: TO MAKE THIS WORK CORRECTLY ---
	 * Select "Debug->Use Black Box to compute MUPS"
	 * Open required Test Ontology in Swoop
	 * Select some class in ConciseFormat
	 * Then click on "Advanced->Run Debug Tests"
	 */
	public void runDebugTests() throws Exception {
		
		// check anyway
		swoopModel.useTableau = false;
		swoopModel.setDebugGlass(true);
		
		// Single MUPS
		// w/ pellet
		String NEWLINE = System.getProperty("line.separator");
		String log = "";
		
		// read test cases	 
		OWLOntology ont = swoopModel.getSelectedOntology();
		String fname = "testUniversity";
////		String fname = swoopModel.shortForm(ont.getURI());
		List testCla = new ArrayList();
		BufferedReader in = new BufferedReader(new FileReader(new File(fname+".txt")));
		String line = null; //not declared within while loop
	    while (( line = in.readLine()) != null) {
	     	OWLClass cla =  ont.getClass(new URI(line));
	     	if (cla!=null) testCla.add(cla); 
	    }
//		for (Iterator iter = swoopModel.getReasoner().equivalentClassesOf(swoopModel.getSelectedOntology().getOWLDataFactory().getOWLNothing()).iterator(); iter.hasNext();) {
//			testCla.add(iter.next());
//		}
	    
	    // run tests
//		useRACER = false;
//		useKAON = false;		
//	    log += runReasonerTest(ont, testCla, "Pellet")+NEWLINE;
//	    
//	    useKAON = true;		
//	    log += runReasonerTest(ont, testCla, "KAON2")+NEWLINE;
	    
//	    useRACER = true;
//		useKAON = false;		
	    log += runReasonerTest(ont, testCla, "RACER")+NEWLINE;
	    
//	    System.out.println(log);
	    
	    FileWriter fw = new FileWriter(new File(fname+"DebugLog.txt"));
	    fw.write(log);
	    fw.close();

	    System.out.println("DONE: Log File written to "+fname+"DebugLog.txt");
	}
	
	private String runReasonerTest(OWLOntology ont, List testCla, String reasStr) throws Exception {
		String NEWLINE = System.getProperty("line.separator");
		String log = "Reasoner: "+ reasStr + NEWLINE;
		log += "Single MUPS"+NEWLINE;
	    swoopModel.findAllMUPS = false;
		for (Iterator iter = testCla.iterator(); iter.hasNext();) {
	    	OWLClass cla = (OWLClass) iter.next();
	    	swoopModel.setSelectedEntity(cla);
	    	log += "| "+cla.getURI().toString() + " | "+netTime + " |"+NEWLINE; 
	    }
		log += "All MUPS"+NEWLINE;
	    swoopModel.findAllMUPS = true;
	    for (Iterator iter = testCla.iterator(); iter.hasNext();) {
	    	OWLClass cla = (OWLClass) iter.next();
	    	swoopModel.setSelectedEntity(cla);
	    	log += "| "+cla.getURI().toString() + " ("+mupsCount+") | "+netTime + " |"+NEWLINE;	    	
	    }
	    System.out.println("--- Tests done for "+reasStr+"----");
	    return log;	    
	}
	public String getURIForTerm(String name) {
		try {
			for (Iterator iter = swoopModel.getOntologies().iterator(); iter.hasNext();) {
				OWLOntology ont = (OWLOntology) iter.next();
				Set allURIs = OntologyHelper.allURIs(ont);
				for (Iterator iter2 = allURIs.iterator(); iter2.hasNext(); ) {
					String uri = iter2.next().toString();
					if (uri.endsWith("#"+name) || uri.endsWith("/"+name)) {
						return uri;
					}							
				}
			}
		}
		catch (OWLException ex) {
			ex.printStackTrace();
		}
		return "";
	}
}