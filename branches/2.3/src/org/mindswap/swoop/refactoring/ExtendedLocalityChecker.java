
package org.mindswap.swoop.refactoring;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.mindswap.swoop.SwoopModel;
import org.semanticweb.owl.model.OWLAnd;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLClassAxiom;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDisjointClassesAxiom;
import org.semanticweb.owl.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLNot;
import org.semanticweb.owl.model.OWLObjectAllRestriction;
import org.semanticweb.owl.model.OWLObjectCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectSomeRestriction;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOr;
import org.semanticweb.owl.model.OWLSubClassAxiom;

public class ExtendedLocalityChecker {
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
	
	
	public ExtendedLocalityChecker(OWLOntology source) {
		this.source = source;
		this.local = new HashSet();
		this.nlocal = new HashSet();
		this.DEBUG = true;
	}	
	
	public boolean isLocal(OWLDescription desc) throws OWLException{
		if (desc instanceof OWLClass){
			if(((OWLClass)desc).getURI().toString().equals("http://www.w3.org/2002/07/owl#Thing"))
				return false;
			else
				return true;
		}
		else{
			if(desc instanceof OWLObjectSomeRestriction){
				if (DEBUG){
					System.out.println("The SomeValuesRestriction on Role"+ ((OWLObjectSomeRestriction)desc).getProperty().getURI().toString() +
							"is local");
				}
				return true;				
			}
			if(desc instanceof OWLObjectAllRestriction){
				return false;
			}
			if (desc instanceof OWLObjectCardinalityRestriction)
			{
				try {
					if(((OWLObjectCardinalityRestriction)desc).isAtLeast() & ((OWLObjectCardinalityRestriction)desc).isExactly())
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
				nlocalAxioms.add(axiom);
						
			}
		}
		
		Iterator ite = this.source.getClasses().iterator();
		while(ite.hasNext()){
			OWLClass cl = (OWLClass)ite.next();
			Set ec = cl.getEquivalentClasses(source);
			Iterator itera = ec.iterator();
			while(itera.hasNext()){
				OWLDescription d = (OWLDescription)itera.next();
				if (!(this.isLocal(d)))
						nlocalAxioms.add(d);
						
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
	
	
	
}
