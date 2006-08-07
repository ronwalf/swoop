
package org.mindswap.swoop.refactoring;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import org.mindswap.swoop.OntologyDisplay;
import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.renderer.SwoopOntologyRenderer;
import org.mindswap.swoop.renderer.entity.AbstractSyntaxEntityRenderer;
import org.mindswap.swoop.utils.owlapi.CorrectedRDFRenderer;
import org.semanticweb.owl.io.RendererException;
import org.semanticweb.owl.model.OWLAnd;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLClassAxiom;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDisjointClassesAxiom;
import org.semanticweb.owl.model.OWLEnumeration;
import org.semanticweb.owl.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLNot;
import org.semanticweb.owl.model.OWLObjectAllRestriction;
import org.semanticweb.owl.model.OWLObjectCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectSomeRestriction;
import org.semanticweb.owl.model.OWLObjectValueRestriction;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOr;
import org.semanticweb.owl.model.OWLSubClassAxiom;
import org.semanticweb.owl.model.change.AddClassAxiom;
import org.semanticweb.owl.model.change.AddEntity;
import org.semanticweb.owl.model.change.ChangeVisitor;
import org.semanticweb.owl.model.helper.OWLBuilder;

public class LocalityChecker {
	protected SwoopModel swoopModel;
	protected OWLOntology source;
	protected OWLOntology imported;
		
//	************************************************************
	//Indexes storing general information about the SOURCE ontology
	//**********************************************************
	protected Set Restrictions; // All Restrictions
	protected Set Unions;
	protected Set Intersections;
	protected Set disjointAxioms;
	protected Map equivalents;
	protected Map subClasses, superClasses, subProperties, superProperties, instancesOf;
	protected Set local;
	protected Set nlocal;
	protected boolean DEBUG;
	public static final String OWL_THING                 = "http://www.w3.org/2002/07/owl#Thing";
	
	
	public LocalityChecker(OWLOntology source) {
		this.source = source;
		this.local = new HashSet();
		this.nlocal = new HashSet();
		this.DEBUG = true;
	}	
	
	public boolean isLocal(OWLDescription desc) throws OWLException{
		if (desc instanceof OWLClass){
			if(((OWLClass)desc).getURI().toString().equals(OWL_THING)){
					return false;
			}
			else
				return true;
		}
		else{
			if(desc instanceof OWLObjectSomeRestriction){
				return true;				
			}
			if(desc instanceof OWLObjectAllRestriction){
				return false;
			}
			if (desc instanceof OWLObjectCardinalityRestriction)
			{
				try {
					if(((OWLObjectCardinalityRestriction)desc).isAtLeast() || ((OWLObjectCardinalityRestriction)desc).isExactly())
						return true;
					else
						return false;
				} catch (OWLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
			}
			if (desc instanceof OWLNot){
				try {
					if(this.isLocal(((OWLNot)desc).getOperand()))
							return false;
					else
						return true;
				} catch (OWLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			if (desc instanceof OWLEnumeration){
				return true;
			}
			
			if (desc instanceof OWLObjectValueRestriction){
				return true;
			}
			
			if (desc instanceof OWLAnd){
				Iterator iter;
				try {
					iter = ((OWLAnd)desc).getOperands().iterator();
					while(iter.hasNext()){
						OWLDescription conjunct = (OWLDescription)iter.next();
						if (this.isLocal(conjunct))
							return true;
							
					}
				} catch (OWLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				
				}
				
				return false;
			}
			
			if (desc instanceof OWLOr){
				Iterator iter;
				try {
					iter = ((OWLOr)desc).getOperands().iterator();
					while(iter.hasNext()){
						OWLDescription disjunct = (OWLDescription)iter.next();
						if (!(this.isLocal(disjunct)))
							return false;
							
					}
				} catch (OWLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				return true;
			}
				
		}
		return true;	
	}
	
	
	public Set getNonLocalAxioms() throws OWLException {
		Set allGCIs = new HashSet();
		Set nlocalAxioms = new HashSet();
		allGCIs = source.getClassAxioms();
		Iterator iter = allGCIs.iterator();
		while (iter.hasNext()) {
			OWLClassAxiom axiom = (OWLClassAxiom) iter.next();
			if (axiom instanceof OWLSubClassAxiom) {
					if (!(this.isLocal(((OWLSubClassAxiom)axiom).getSubClass())) && 
							this.isLocal(((OWLSubClassAxiom)axiom).getSuperClass())) {
					nlocalAxioms.add(axiom);
				}
			}
			if (axiom instanceof OWLEquivalentClassesAxiom){
				int l = 0,nl = 0;
				Set eq = ((OWLEquivalentClassesAxiom)axiom).getEquivalentClasses();
				Iterator it = eq.iterator();
				while(it.hasNext()){
					OWLDescription d = (OWLDescription)it.next();
					if(this.isLocal(d)){
						l = l+1;
					}
					else{
						nl= nl+1;
					}
						
				}
				if( l > 0 && nl > 0){
					nlocalAxioms.add(axiom);
				}		
			}
		}
		
		Iterator ite = this.source.getClasses().iterator();
		while(ite.hasNext()){
			OWLClass cl = (OWLClass)ite.next();
			Set ec = cl.getEquivalentClasses(source);
			Iterator itera = ec.iterator();
			while(itera.hasNext()){
				OWLDescription d = (OWLDescription)itera.next();
				if (!(this.isLocal(d))){
					 Set aux = new HashSet();
					 aux.add(d);
					 aux.add(cl);
					 OWLEquivalentClassesAxiom axiom = source.getOWLDataFactory().getOWLEquivalentClassesAxiom(aux);
                	 nlocalAxioms.add(axiom);
					
				}
			}
		}
		return nlocalAxioms;
	}
	
	public boolean isLocal(OWLOntology ont) throws OWLException{
		if (this.getNonLocalAxioms().isEmpty())
			return true;
		else
			return false;
		
	}
	
	public OWLOntology createNonLocalPart(Set axioms) throws Exception{
		OWLOntology ontology;
		OWLDataFactory df = null;
		URI uri = new URI("http://www.cs.man.ac.uk/~nonLocal");
		OWLBuilder builder = new OWLBuilder();
		builder.createOntology(uri, uri);
		ontology = builder.getOntology();
		df = ontology.getOWLDataFactory();
		//addAnnotations(df);
		
		/* also add owl:Thing to the ontology */
		// otherwise thing appears as an imported class in the tree?!
		OWLClass thing = df.getOWLThing();
		AddEntity ae = new AddEntity(ontology, thing, null);
		ae.accept((ChangeVisitor) ontology);
		Iterator iter = axioms.iterator();
		while(iter.hasNext()){
			OWLClassAxiom ax = (OWLClassAxiom)iter.next();
			AddClassAxiom aax = new AddClassAxiom(ontology,ax,null);
			aax.accept((ChangeVisitor) ontology);
		}
		
		//swoopModel.addOntology(ontology);
		//((OntologyDisplay) SwoopHandler).swoopHandler.enableMenuOptions();
		//swoopModel.setSelectedOntology(ontology);	
		
		
		
		
		return ontology;
	}
	
	public String renderNonLocal(OWLOntology nonLocalPart) throws OWLException{
		String output;
		StringWriter rdfBuffer = new StringWriter();
		CorrectedRDFRenderer rdfRenderer = new CorrectedRDFRenderer();
		rdfRenderer.renderOntology(nonLocalPart, rdfBuffer);
		output = rdfBuffer.toString();
		return output;
	}
	
	
}
