package org.mindswap.swoop.explore;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.reasoner.SwoopReasoner;
import org.mindswap.swoop.renderer.entity.ConciseFormatEntityRenderer;
import org.mindswap.swoop.utils.graph.hierarchy.popup.ClassAxiomContainer;
import org.mindswap.swoop.utils.graph.hierarchy.popup.ConcisePlainVisitor;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLClassAxiom;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDisjointClassesAxiom;
import org.semanticweb.owl.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLSubClassAxiom;

public class AxiomIndexer 
{

	
	private AxiomExtractor myExtractor = null;
	
	HashedVectors classExpToAxioms; // class expression to axioms
	HashedCounts classExpCounts;    // expression to number of times used
	
	Hashtable classExpDepths; 
	
	int maxDepth;	
	
	HashedVectors exiPropToClassExp;
	
	SwoopModel myModel = null;
	ConciseFormatEntityRenderer myShortForms;
	ConcisePlainVisitor myVisitor;
	
	public AxiomIndexer( OWLOntology ont, SwoopModel model, SwoopReasoner reasoner)
	{
		myModel = model;
		myExtractor = new AxiomExtractor( ont, model, reasoner );
		init();
	}
	
	private void init()
	{
		this.classExpToAxioms = new HashedVectors();		
		this.classExpCounts = new HashedCounts();
		
		this.classExpDepths = new Hashtable();
		
		this.maxDepth = 0;
		
		this.myShortForms = new ConciseFormatEntityRenderer();
		myShortForms.setSwoopModel( myModel );
		myVisitor = new ConcisePlainVisitor( myShortForms, myModel);
	}
	
	public void index()
	{
		try
		{
			Vector classAxiomContainers = myExtractor.extractClassAxioms();
			for ( Iterator it = classAxiomContainers.iterator(); it.hasNext(); )
			{
				OWLClassAxiom axi = (OWLClassAxiom)((ClassAxiomContainer)it.next()).getAxiom();
				AxiomContentExtractor content = new AxiomContentExtractor( myVisitor );
				axi.accept( content );
				addToClassIndex( axi, content );
			}
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}		
	}
	
	private void addToClassIndex( OWLClassAxiom axi, AxiomContentExtractor content )
	{
		try
		{
			if ( content.maxDepth > this.maxDepth )
				this.maxDepth = content.maxDepth;
			
			for ( Iterator it = content.classExpCounts.keySet().iterator(); it.hasNext(); )
			{
				OWLDescription desc = (OWLDescription)it.next();
				this.classExpCounts.add( desc, content.classExpCounts.getCount( desc ) );			
				this.classExpToAxioms.add( desc, axi );
			}
			
			for ( Iterator it = content.classExpDepths.keySet().iterator(); it.hasNext(); )
			{
				OWLDescription desc = (OWLDescription)it.next();
				this.classExpDepths.put( desc, new Integer( content.classExpDepths.getCount( desc )) );
			}
		}
		catch ( Exception e )
		{ e.printStackTrace(); }
	}
	
	public void print()
	{
		try
		{
			for ( Iterator it = this.classExpCounts.keySet().iterator(); it.hasNext(); )
			{
				OWLDescription desc = (OWLDescription)it.next();
				desc.accept( myVisitor );
				String str = myVisitor.result();
				myVisitor.reset();
				//System.out.println( str + " || frequency = " + classExpCounts.getCount( desc ) + "  depth = " + classExpDepths.get( desc ) );
			}
		}
		catch ( Exception e )
		{ e.printStackTrace(); }
	}
	
	public void view()
	{
		new ClassExpTable( this, myVisitor, this.classExpCounts, this.classExpDepths );
	}
}
