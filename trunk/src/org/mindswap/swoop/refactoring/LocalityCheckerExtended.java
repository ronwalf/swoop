
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
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLEnumeration;
import org.semanticweb.owl.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLIndividual;
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

public class LocalityCheckerExtended {
	protected SwoopModel swoopModel;
	protected OWLOntology source;
	protected OWLOntology imported;
		
//	************************************************************
	//Indexes storing general information about the SOURCE ontology
	//**********************************************************
	protected Set local;
	protected Set nlocal;
	protected boolean DEBUG;
	public static final String OWL_THING                 = "http://www.w3.org/2002/07/owl#Thing";
	
	
	
	public LocalityCheckerExtended(OWLOntology source) {
		this.source = source;
		this.local = new HashSet();
		this.nlocal = new HashSet();
		this.DEBUG = true;
	}	
	
	/******
	 * 
	 * @param desc
	 * @param foreign
	 * @return TRUE if the description is non-local with respect to foreign
	 *         and FALSE otherwise
	 * @throws OWLException
	 */
	
	public boolean isNonLocal(OWLDescription desc, Set foreign) throws OWLException {
		if(desc instanceof OWLClass){
			return false;
		}
		
		if(desc instanceof OWLObjectSomeRestriction){
			return false;
		}
			
		if(desc instanceof OWLObjectValueRestriction){
			return false;
		}
			
		if(desc instanceof OWLEnumeration){
			return false;
		}
			
		if(desc instanceof OWLObjectAllRestriction){
			if(!foreign.contains(((OWLObjectAllRestriction)desc).getProperty())){
				return true;
			}
			else{
				if(isNonLocal(((OWLObjectAllRestriction)desc).getDescription(),foreign))
					return true;		
				else
					return false;
			}
		}
			
		if (desc instanceof OWLObjectCardinalityRestriction)
		{
			try {
				if(((OWLObjectCardinalityRestriction)desc).isAtLeast())
					return false;
				if(((OWLObjectCardinalityRestriction)desc).isAtMost())
					if(!foreign.contains(((OWLObjectCardinalityRestriction)desc).getProperty()))
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
					if(this.isNonLocal(((OWLNot)desc).getOperand(), foreign))
							return false;
					if(this.isLocal(((OWLNot)desc).getOperand(), foreign))
							return true;
					return false;
				} catch (OWLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			//All conjuncts must be non-local
			if (desc instanceof OWLAnd){
				Iterator iter;
				try {
					iter = ((OWLAnd)desc).getOperands().iterator();
					while(iter.hasNext()){
						OWLDescription conjunct = (OWLDescription)iter.next();
						if (!this.isNonLocal(conjunct, foreign))
							return false;
							
					}
					return true;
				} catch (OWLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				
				}
				
				return false;
			}
			
			//At least one of the disjuncts must be non-local
			if (desc instanceof OWLOr){
				Iterator iter;
				try {
					iter = ((OWLOr)desc).getOperands().iterator();
					while(iter.hasNext()){
						OWLDescription disjunct = (OWLDescription)iter.next();
						if (this.isNonLocal(disjunct, foreign))
							return true;
			
					}
					return false;
				} catch (OWLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				return true;
			}
			
		return true;	
	}
	
	
	
	
	
	
	
	
	
	public boolean isLocal(OWLDescription desc, Set foreign) throws OWLException{
		if (desc instanceof OWLClass){
			if (foreign.contains(desc))
				return false;
			else
			{
				if(((OWLClass)desc).getURI().toString().equals(OWL_THING)){
						return false;
				}
				else
					return true;
			}
		}
		else{
			if(desc instanceof OWLObjectSomeRestriction){
				if(!foreign.contains(((OWLObjectSomeRestriction)desc).getProperty())){
					return true;
				}
				else{
					if(isLocal(((OWLObjectSomeRestriction)desc).getDescription(),foreign))
						return true;		
					else
						return false;
				}
				
			}
			if(desc instanceof OWLEnumeration){
				Set indiv = new HashSet();
				indiv = ((OWLEnumeration)desc).getIndividuals();
	 			boolean b = true;
				Iterator it = indiv.iterator();
				while(it.hasNext()){
					OWLIndividual in = (OWLIndividual)it.next();
					if(foreign.contains(in)){
						return false;
					}
				}
				return b;
			}
			
			if(desc instanceof OWLObjectValueRestriction){
				if(!foreign.contains(((OWLObjectValueRestriction)desc).getProperty())){
					return true;
				}
				else{
					if(!foreign.contains(((OWLObjectValueRestriction)desc).getIndividual()))
						return true;		
					else
						return false;
				}
				
			}
			
									
			if(desc instanceof OWLObjectAllRestriction){
				return false;
			}
			
			if (desc instanceof OWLObjectCardinalityRestriction)
			{
				try {
					if(((OWLObjectCardinalityRestriction)desc).isAtMost())
						return false;
					if(((OWLObjectCardinalityRestriction)desc).isAtLeast() || ((OWLObjectCardinalityRestriction)desc).isExactly())
						if(!foreign.contains(((OWLObjectCardinalityRestriction)desc).getProperty()))
							return true;
						else
							return false;
					else
						return false;
				} catch (OWLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
			}
			if (desc instanceof OWLNot){
				try {
					if(this.isLocal(((OWLNot)desc).getOperand(), foreign))
							return false;
					if(this.isNonLocal(((OWLNot)desc).getOperand(), foreign))
							return true;
					return false;
				} catch (OWLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			//At least one conjunct must be local
			if (desc instanceof OWLAnd){
				Iterator iter;
				try {
					iter = ((OWLAnd)desc).getOperands().iterator();
					while(iter.hasNext()){
						OWLDescription conjunct = (OWLDescription)iter.next();
						if (this.isLocal(conjunct, foreign))
							return true;
							
					}
					return false;
				} catch (OWLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				
				}
				
				return false;
			}
			
			//All disjuncts must be local
			if (desc instanceof OWLOr){
				Iterator iter;
				try {
					iter = ((OWLOr)desc).getOperands().iterator();
					while(iter.hasNext()){
						OWLDescription disjunct = (OWLDescription)iter.next();
						if (!(this.isLocal(disjunct, foreign)))
							return false;
							
					}
					return true;
				} catch (OWLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				return true;
			}
				
		}
		return true;	
	}
	
	
	public Set getNonLocalAxioms(Set foreign) throws OWLException {
		Set allGCIs = new HashSet();
		Set nlocalAxioms = new HashSet();
		allGCIs = source.getClassAxioms();
		Iterator iter = allGCIs.iterator();
		while (iter.hasNext()) {
			OWLClassAxiom axiom = (OWLClassAxiom) iter.next();
			if (axiom instanceof OWLSubClassAxiom) {
					if (!(this.isLocal(((OWLSubClassAxiom)axiom).getSubClass(), foreign)) && 
							!this.isNonLocal(((OWLSubClassAxiom)axiom).getSuperClass(), foreign)) {
					nlocalAxioms.add(axiom);
				}
			}
			if (axiom instanceof OWLEquivalentClassesAxiom){
				int l = 0,nl = 0, neither = 0;
				Set eq = ((OWLEquivalentClassesAxiom)axiom).getEquivalentClasses();
				Iterator it = eq.iterator();
				while(it.hasNext()){
					OWLDescription d = (OWLDescription)it.next();
					if(this.isLocal(d,foreign)){
						l = l+1;
					}
					else{
						if(this.isNonLocal(d,foreign)){
							nl = nl+1;
						}
						else
							neither= neither+1;	
					}
						
					
						
				}
				if( nl > 0 || neither > 0){
					nlocalAxioms.add(axiom);
				}		
			}
		}
		
		Iterator ite = this.source.getClasses().iterator();
		
		while(ite.hasNext()){
			OWLClass cl = (OWLClass)ite.next();
			Set ec = new HashSet();
			ec = cl.getEquivalentClasses(source);
			Iterator iit = ec.iterator();
			while(iit.hasNext()){
				OWLDescription d = (OWLDescription)iit.next();
				if(!this.isLocal(d,foreign)){
					Set aux = new HashSet();
						aux.add(d);
						aux.add(cl);
						OWLEquivalentClassesAxiom axiom = source.getOWLDataFactory().getOWLEquivalentClassesAxiom(aux);
	                	 nlocalAxioms.add(axiom);
					}
					
				
			}
			
			Set sc = new HashSet();
			sc = cl.getSuperClasses(source);
			Iterator j = sc.iterator();
			while(j.hasNext()){
				OWLDescription d = (OWLDescription)j.next();
				if(foreign.contains(cl)){
				  if(!this.isNonLocal(d,foreign)){
						OWLSubClassAxiom axiom = source.getOWLDataFactory().getOWLSubClassAxiom(cl,d);
						nlocalAxioms.add(axiom);
					}
				}
			}
		}
		return nlocalAxioms;
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

	/*
	public Set getForeignEntities(OWLOntology ontology) throws OWLException {
		Set foreignOntologies = new HashSet();
		foreignOntologies = ontology.getIncludedOntologies();
		Set foreignEntities = new HashSet();
		Iterator iter = foreignOntologies.iterator();
		while (iter.hasNext()){
			OWLOntology auxOnt = (OWLOntology)iter.next();
			Set entities = new HashSet();
			entities = auxOnt.getClasses();
			entities.addAll(auxOnt.getObjectProperties());
			entities.addAll(auxOnt.getIndividuals());
			foreignEntities.addAll(entities);
		}
		return foreignEntities;
	}
*/
	public boolean isLocal(OWLOntology ont, Set foreign) throws OWLException{
		if (this.getNonLocalAxioms(foreign).isEmpty())
			return true;
		else
			return false;
		
	}

	public String renderForeignInNonLocal(OWLOntology ont, Set foreign) throws OWLException {
		String result= new String();
		Set entities = new HashSet();
		entities = ont.getClasses();
		entities.addAll(ont.getObjectProperties());
		entities.addAll(ont.getIndividuals());
		Iterator iter = entities.iterator();
		while(iter.hasNext()){
			OWLEntity ent = (OWLEntity)iter.next();
			if(foreign.contains(ent)){
				result = result.concat(ent.getURI().toString() + "\n");
			}
			
		}
		
		return result;
	}

	public boolean isHierarchical(OWLOntology ontology) throws OWLException {
		Set includedOnts = new HashSet();
		includedOnts = ontology.getIncludedOntologies();
		Iterator iter = includedOnts.iterator();
		while(iter.hasNext()){
			OWLOntology aux = (OWLOntology)iter.next();
			Set auxIncluded = new HashSet();
			auxIncluded = aux.getIncludedOntologies();
			if (auxIncluded.contains(ontology)){
				return false;
			}
		}
		return true;
	}
	
	
	
}
