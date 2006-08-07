/*
 * Created on Feb 27, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.mindswap.swoop.utils.explain;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Hashtable;

import org.semanticweb.owl.io.vocabulary.RDFSVocabularyAdapter;

/**
 * @author Dave
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class RDFSVocabularyExplanationDeposit extends Hashtable implements	VocabularyExplanationDeposit 
{
	
	private static URI                             RDFS_REF   = null;
	private static RDFSVocabularyExplanationDeposit myDeposit = null;
	private int myNumExplanations = 0;
	
	public static RDFSVocabularyExplanationDeposit getInstance()
	{
		if (myDeposit == null)
		{
			try
			{
				RDFS_REF = new URI("http://www.w3.org/TR/rdf-schema/");
				myDeposit = new RDFSVocabularyExplanationDeposit();
			}
			catch (URISyntaxException e)
			{
				e.printStackTrace();
			}
		}
		return myDeposit;
	}
	
	private RDFSVocabularyExplanationDeposit() throws URISyntaxException
	{
		URI [] sources = { RDFS_REF };
		
		// RDFS classes
		String name = RDFSVocabularyAdapter.RDFS + "Resource";
		URI [] sources_Resource = { new URI("http://www.w3.org/TR/rdf-schema/#ch_resource")  };
		add( new RDFSVocabularyExplanation( new URI(name), name,
				"<p>All things described by RDF are called resources, and are instances of the class rdfs:Resource. This is the class of everything. All other classes are subclasses of this class. rdfs:Resource is an instance of rdfs:Class.</p>",
				sources_Resource, RDFS_REF, null) 
		);	
		
		name = RDFSVocabularyAdapter.RDFS + "Class";
		URI [] sources_Class = { new URI("http://www.w3.org/TR/rdf-schema/#ch_class")  };
		add( new RDFSVocabularyExplanation( new URI(name), name,
				"<p>This is the class of resources that are RDF classes. rdfs:Class is an instance of rdfs:Class.</p>",
				sources_Class, RDFS_REF, null) 
		);
		
		
		name = RDFSVocabularyAdapter.RDFS + "Literal";
		URI [] sources_Literal = { new URI("http://www.w3.org/TR/rdf-schema/#ch_literal")  };
		add( new RDFSVocabularyExplanation( new URI(name), name,
					"<p>The class rdfs:Literal is the class of literal values such as strings and integers. Property values such as textual strings are examples of RDF literals. Literals may be plain or typed. A typed literal is an instance of a datatype class. This specification does not define the class of plain literals.</p>"
				+	"<p>rdfs:Literal is an instance of rdfs:Class. rdfs:Literal is a subclass of rdfs:Resource.</p>",
				sources_Literal, RDFS_REF, null) 
		);
		
		name = RDFSVocabularyAdapter.RDFS + "Datatype";
		URI [] sources_DT = { new URI("http://www.w3.org/TR/rdf-schema/#ch_datatype")  };
		add( new RDFSVocabularyExplanation( new URI(name), name,
					"<p>rdfs:Datatype is the class of datatypes. All instances of rdfs:Datatype correspond to the RDF model of a datatype described in the RDF Concepts specification [RDF-CONCEPTS]. rdfs:Datatype is both an instance of and a subclass of rdfs:Class. Each instance of rdfs:Datatype is a subclass of rdfs:Literal.</p>",
				sources_DT, RDFS_REF, null) 
		);
		
		// RDFS Prperties

		name = RDFSVocabularyAdapter.RDFS + "range";
		URI [] sources_range = { new URI("http://www.w3.org/TR/rdf-schema/#ch_properties")  };
		add( new RDFSVocabularyExplanation( new URI(name), name,		
					"<p>rdfs:range is an instance of rdf:Property that is used to state that the values of a property are instances of one or more classes.</p>"
				+	"<p>The triple</p>"
				+	"<p>P rdfs:range C</p>"
				+	"<p>states that P is an instance of the class rdf:Property, that C is an instance of the class rdfs:Class and that the resources denoted by the objects of triples whose predicate is P are instances of the class C.</p>"
				+	"<p>Where P has more than one rdfs:range property, then the resources denoted by the objects of triples with predicate P are instances of all the classes stated by the rdfs:range properties.</p>"
				+	"<p>The rdfs:range property can be applied to itself. The rdfs:range of rdfs:range is the class rdfs:Class. This states that any resource that is the value of an rdfs:range property is an instance of rdfs:Class.</p>"
				+	"<p>The rdfs:range property is applied to properties. This can be represented in RDF using the rdfs:domain property. The rdfs:domain of rdfs:range is the class rdf:Property. This states that any resource with an rdfs:range property is an instance of rdf:Property.</p>",
					sources_range, RDFS_REF, null) 
		);
		name = RDFSVocabularyAdapter.RDFS + "domain";
		URI [] sources_domain = { new URI("http://www.w3.org/TR/rdf-schema/#ch_domain")  };
		add( new RDFSVocabularyExplanation( new URI(name), name,		
					"<p>rdfs:domain is an instance of rdf:Property that is used to state that any resource that has a given property is an instance of one or more classes.</p>"
				+	"<p>A triple of the form:</p>"
				+	"<p>P rdfs:domain C</p>"
				+	"<p>states that P is an instance of the class rdf:Property, that C is a instance of the class rdfs:Class and that the resources denoted by the subjects of triples whose predicate is P are instances of the class C.</p>"
				+	"<p>Where a property P has more than one rdfs:domain property, then the resources denoted by subjects of triples with predicate P are instances of all the classes stated by the rdfs:domain properties.</p>"
				+	"<p>The rdfs:domain property may be applied to itself. The rdfs:domain of rdfs:domain is the class rdf:Property. This states that any resource with an rdfs:domain property is an instance of rdf:Property.</p>"
				+	"<p>The rdfs:range of rdfs:domain is the class rdfs:Class. This states that any resource that is the value of an rdfs:domain property is an instance of rdfs:Class.</p>",
					sources_domain, RDFS_REF, null) 
		);
				
		name = RDFSVocabularyAdapter.RDFS + "subClassOf";
		URI [] sources_subC = { new URI("http://www.w3.org/TR/rdf-schema/#ch_subclassof")  };
		add( new RDFSVocabularyExplanation( new URI(name), name,
					"<p>The property rdfs:subClassOf is an instance of rdf:Property that is used to state that all the instances of one class are instances of another.</p>"
				+	"<p>A triple of the form:</p>"
				+	"<p>C1 rdfs:subClassOf C2</p>"
				+	"<p>states that C1 is an instance of rdfs:Class, C2 is an instance of rdfs:Class and C1 is a subclass of C2. The rdfs:subClassOf property is transitive.</p>"		
				+	"<p>The rdfs:domain of rdfs:subClassOf is rdfs:Class. The rdfs:range of rdfs:subClassOf is rdfs:Class.</p>",
				sources_subC, RDFS_REF, null) 
		);		
		
		name = RDFSVocabularyAdapter.RDFS + "subPropertyOf";
		URI [] sources_subP = { new URI("http://www.w3.org/TR/rdf-schema/#ch_subpropertyof")  };
		add( new RDFSVocabularyExplanation( new URI(name), name,
					"<p>The property rdfs:subPropertyOf is an instance of rdf:Property that is used to state that all resources related by one property are also related by another.</p>"
				+	"<p>A triple of the form:</p>"
				+	"<p>P1 rdfs:subPropertyOf P2</p>"
				+	"<p>states that P1 is an instance of rdf:Property, P2 is an instance of rdf:Property and P1 is a subproperty of P2. The rdfs:subPropertyOf property is transitive.</p>"		
				+	"<p>The rdfs:domain of rdfs:subPropertyOf is rdf:Property. The rdfs:range of rdfs:subPropertyOf is rdf:Property.</p>",
				sources_subP, RDFS_REF, null) 
		);	
		
			name = RDFSVocabularyAdapter.RDFS + "label";
		URI [] sources_lbl= { new URI("http://www.w3.org/TR/rdf-schema/#ch_label")  };
		add( new RDFSVocabularyExplanation( new URI(name), name,
					"<p>rdfs:label is an instance of rdf:Property that may be used to provide a human-readable version of a resource's name.</p>"
				+	"<p>A triple of the form:</p>"
				+	"<p>R rdfs:label L</p>"
				+	"<p>states that L is a human readable label for R.</p>"		
				+	"<p>The rdfs:domain of rdfs:label is rdfs:Resource. The rdfs:range of rdfs:label is rdfs:Literal.</p>"
				+	"<p>Multilingual labels are supported using the language tagging facility of RDF literals.</p>",
				sources_lbl, RDFS_REF, null) 
		);	
		
				name = RDFSVocabularyAdapter.RDFS + "comment";
		URI [] sources_comment = { new URI("http://www.w3.org/TR/rdf-schema/#ch_comment")  };
		add( new RDFSVocabularyExplanation( new URI(name), name,
					"<p>rdfs:comment is an instance of rdf:Property that may be used to provide a human-readable description of a resource.</p>"
				+	"<p>A triple of the form:</p>"
				+	"<p>R rdfs:comment L</p>"
				+	"<p>states that L is a human readable description of R.</p>"		
				+	"<p>The rdfs:domain of rdfs:comment is rdfs:Resource. The rdfs:range of rdfs:comment is rdfs:Literal.</p>"
				+	"<p>A textual comment helps clarify the meaning of RDF classes and properties. Such in-line documentation complements the use of both formal techniques (Ontology and rule languages) and informal (prose documentation, examples, test cases). A variety of documentation forms can be combined to indicate the intended meaning of the classes and properties described in an RDF vocabulary. Since RDF vocabularies are expressed as RDF graphs, vocabularies defined in other namespaces may be used to provide richer documentation.</p>"
				+	"<p>Multilingual documentation is supported through use of the language tagging facility of RDF literals.</p>",
				sources_comment, RDFS_REF, null) 
		);	
		
		
		// Other RDFS Vocabulary
		name = RDFSVocabularyAdapter.RDFS + "Container";
		URI [] sources_cont = { new URI("http://www.w3.org/TR/rdf-schema/#ch_container")  };
		add( new RDFSVocabularyExplanation( new URI(name), name,	
				"<p>The rdfs:Container class is a super-class of the RDF Container classes, i.e. rdf:Bag, rdf:Seq, rdf:Alt.</p>",
				sources_cont, RDFS_REF, null) 
		);	
		
		name = RDFSVocabularyAdapter.RDFS + "ContainerMembershipProperty";
		URI [] sources_CMP = { new URI("http://www.w3.org/TR/rdf-schema/#ch_containermembershipproperty")  };
		add( new RDFSVocabularyExplanation( new URI(name), name,
					"<p>The rdfs:ContainerMembershipProperty class has as instances the properties rdf:_1, rdf:_2, rdf:_3 ... that are used to state that a resource is a member of a container. rdfs:ContainerMembershipProperty is a subclass of rdf:Property. Each instance of rdfs:ContainerMembershipProperty is an rdfs:subPropertyOf the rdfs:member property.</p>"
				+	"<p>Given a container C, a triple of the form:</p>"
				+	"<p>C rdf:_nnn O</p>"
				+	"<p>where nnn is the decimal representation of an integer greater than 0 with no leading zeros, states that O is a member of the container C.</p>"		
				+	"<p>Container membership properties may be applied to resources other than containers.</p>",
				sources_CMP, RDFS_REF, null) 
		);	
		
		name = RDFSVocabularyAdapter.RDFS + "member";
		URI [] sources_member = { new URI("http://www.w3.org/TR/rdf-schema/#ch_member")  };
		add( new RDFSVocabularyExplanation( new URI(name), name,
					"<p>rdfs:member is an instance of rdf:Property that is a super-property of all the container membership properties i.e. each container membership property has an rdfs:subPropertyOf relationship to the property rdfs:member.</p>"
				+	"<p>The rdfs:domain of rdfs:member is rdfs:Resource. The rdfs:range of rdfs:member is rdfs:Resource.</p>",
				sources_member, RDFS_REF, null) 
		);	
		
		// RDFS Utility Properties
		name = RDFSVocabularyAdapter.RDFS + "seeAlso";
		URI [] sources_seeAlso = { new URI("http://www.w3.org/TR/rdf-schema/#ch_seealso")  };
		add( new RDFSVocabularyExplanation( new URI(name), name,
					"<p>rdfs:seeAlso is an instance of rdf:Property that is used to indicate a resource that might provide additional information about the subject resource.</p>"
				+	"<p>A triple of the form:</p>"
				+	"<p>S rdfs:seeAlso O</p>"
				+	"<p>states that the resource O may provide additional information about S. It may be possible to retrieve representations of O from the Web, but this is not required. When such representations may be retrieved, no constraints are placed on the format of those representations.</p>"		
				+	"<p>The rdfs:domain of rdfs:seeAlso is rdfs:Resource. The rdfs:range of rdfs:seeAlso is rdfs:Resource.</p>",
				sources_seeAlso, RDFS_REF, null) 
		);	
		
		name = RDFSVocabularyAdapter.RDFS + "isDefinedBy";
		URI [] sources_isDefinedBy = { new URI("http://www.w3.org/TR/rdf-schema/#ch_isdefinedby")  };
		add( new RDFSVocabularyExplanation( new URI(name), name,
					"<p>rdfs:isDefinedBy is an instance of rdf:Property that is used to indicate a resource defining the subject resource. This property may be used to indicate an RDF vocabulary in which a resource is described.</p>"
				+	"<p>A triple of the form:</p>"
				+	"<p>S rdfs:isDefinedBy O</p>"
				+	"<p>states that the resource O defines S. It may be possible to retrieve representations of O from the Web, but this is not required. When such representations may be retrieved, no constraints are placed on the format of those representations. rdfs:isDefinedBy is a subproperty of rdfs:seeAlso.</p>"		
				+	"<p>The rdfs:domain of rdfs:isDefinedBy is rdfs:Resource. The rdfs:range of rdfs:isDefinedBy is rdfs:Resource.</p>",
				sources_isDefinedBy, RDFS_REF, null) 
		);	
		

	}

	public void add(RDFSVocabularyExplanation exp)
	{
		super.put( exp.getURI(), exp );
		myNumExplanations++;
	}

	public VocabularyExplanation explain(URI uri) {

		return (RDFSVocabularyExplanation)super.get(uri);
	}

	public int numExplanations() {
		
		return myNumExplanations;
	}
	
	
}
