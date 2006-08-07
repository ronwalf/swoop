/*
 * Created on Feb 27, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.mindswap.swoop.utils.explain;

import java.util.Hashtable;

import java.net.URI;
import java.net.URISyntaxException;

import org.semanticweb.owl.io.vocabulary.RDFVocabularyAdapter;

/**
 * @author Dave
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class RDFVocabularyExplanationDeposit extends Hashtable implements VocabularyExplanationDeposit 
{
	private static URI                             RDF_REF   = null;
	private static RDFVocabularyExplanationDeposit myDeposit  = null;
	private int myNumExplanations = 0;
	
	public static RDFVocabularyExplanationDeposit getInstance()
	{
		if (myDeposit == null)
		{
			try
			{
				RDF_REF = new URI("http://www.w3.org/TR/rdf-schema/");
				myDeposit = new RDFVocabularyExplanationDeposit();
			}
			catch (URISyntaxException e)
			{
				e.printStackTrace();
			}
		}
		return myDeposit;
	}
	
	private RDFVocabularyExplanationDeposit() throws URISyntaxException
	{
		URI [] sources = { RDF_REF };
		String name;
		
		// RDF Classes
		name = RDFVocabularyAdapter.RDF + "XMLLiteral";
		URI [] sources_XMLLit = { new URI("http://www.w3.org/TR/rdf-schema/#ch_xmlliteral")  };
		add( new RDFVocabularyExplanation( new URI(name), name,		
					"<p>The class rdf:XMLLiteral is the class of XML literal values. rdf:XMLLiteral is an instance of rdfs:Datatype and a subclass of rdfs:Literal.</p>",
				sources_XMLLit, RDF_REF, null) 
		);
		
		name = RDFVocabularyAdapter.RDF + "Property";
		URI [] sources_props = { new URI("http://www.w3.org/TR/rdf-schema/#ch_property")  };
		add( new RDFVocabularyExplanation( new URI(name), name,		
					"<p>rdf:Property is the class of RDF properties. rdf:Property is an instance of rdfs:Class.</p>",
				sources_props, RDF_REF, null) 
		);
		// RDF properties
		
		name = RDFVocabularyAdapter.RDF + "type";
		URI [] sources_type = { new URI("http://www.w3.org/TR/rdf-schema/#ch_type")  };
		add( new RDFVocabularyExplanation( new URI(name), name,
					"<p>rdf:type is an instance of rdf:Property that is used to state that a resource is an instance of a class.</p>"		
				+	"<p>A triple of the form:</p>"
				+	"<p>R rdf:type C</p>"
				+	"<p>states that C is an instance of rdfs:Class and R is an instance of C.</p>"
				+ 	"<p>The rdfs:domain of rdf:type is rdfs:Resource. The rdfs:range of rdf:type is rdfs:Class.</p>",
				sources_type, RDF_REF, null) 
		);
		
		// RDF container classes
		name = RDFVocabularyAdapter.RDF + "Bag";
		URI [] sources_bag = { new URI("http://www.w3.org/TR/rdf-schema/#ch_bag")  };
		add( new RDFVocabularyExplanation( new URI(name), name,
					"<p>The rdf:Bag class is the class of RDF 'Bag' containers. It is a subclass of rdfs:Container. Whilst formally it is no different from an rdf:Seq or an rdf:Alt, the rdf:Bag class is used conventionally to indicate to a human reader that the container is intended to be unordered.</p>",
				sources_bag, RDF_REF, null) 
		);	
		
		name = RDFVocabularyAdapter.RDF + "Seq";
		URI [] sources_seq = { new URI("http://www.w3.org/TR/rdf-schema/#ch_seq")  };
		add( new RDFVocabularyExplanation( new URI(name), name,
				"<p>The rdf:Seq class is the class of RDF 'Sequence' containers. It is a subclass of rdfs:Container. Whilst formally it is no different from an rdf:Bag or an rdf:Alt, the rdf:Seq class is used conventionally to indicate to a human reader that the numerical ordering of the container membership properties of the container is intended to be significant.</p>",
				sources_seq, RDF_REF, null) 
		);	
		name = RDFVocabularyAdapter.RDF + "Alt";
		URI [] sources_alt = { new URI("http://www.w3.org/TR/rdf-schema/#ch_alt")  };
		add( new RDFVocabularyExplanation( new URI(name), name,
				"<p>The rdf:Alt class is the class of RDF 'Alternative' containers. It is a subclass of rdfs:Container. Whilst formally it is no different from an rdf:Seq or an rdf:Bag, the rdf:Alt class is used conventionally to indicate to a human reader that typical processing will be to select one of the members of the container. The first member of the container, i.e. the value of the rdf:_1 property, is the default choice.</p>",
				sources_alt, RDF_REF, null) 
		);	
		
		// RDF Collections
		name = RDFVocabularyAdapter.RDF + "List";
		URI [] sources_list = { new URI("http://www.w3.org/TR/rdf-schema/#ch_list")  };
		add( new RDFVocabularyExplanation( new URI(name), name,
					"<p>rdf:List is an instance of rdfs:Class that can be used to build descriptions of lists and other list-like structures.</p>",
				sources_list, RDF_REF, null) 
		);	
		
		name = RDFVocabularyAdapter.RDF + "first";
		URI [] sources_first = { new URI("http://www.w3.org/TR/rdf-schema/#ch_first")  };
		add( new RDFVocabularyExplanation( new URI(name), name,
					"<p>rdf:first is an instance of rdf:Property that can be used to build descriptions of lists and other list-like structures.</p>"
				+	"<p>A triple of the form:</p>"
				+	"<p>L rdf:first O</p>"
				+	"<p>states that there is a first-element relationship between L and O.</p>"		
				+	"<p>The rdfs:domain of rdf:first is rdf:List. The rdfs:range of rdf:first is rdfs:Resource.</p>",
				sources_first, RDF_REF, null) 
		);	
		
		name = RDFVocabularyAdapter.RDF + "rest";
		URI [] sources_rest = { new URI("http://www.w3.org/TR/rdf-schema/#ch_rest")  };
		add( new RDFVocabularyExplanation( new URI(name), name,
					"<p>rdf:rest is an instance of rdf:Property that can be used to build descriptions of lists and other list-like structures.</p>"
				+	"<p>A triple of the form:</p>"
				+	"<p>L rdf:rest O</p>"
				+	"<p>states that there is a rest-of-list relationship between L and O.</p>"		
				+	"<p>The rdfs:domain of rdf:rest is rdf:List. The rdfs:range of rdf:rest is rdf:List.</p>",
				sources_rest, RDF_REF, null) 
		);	

		name = RDFVocabularyAdapter.RDF + "nil";
		URI [] sources_nil = { new URI("http://www.w3.org/TR/rdf-schema/#ch_nil")  };
		add( new RDFVocabularyExplanation( new URI(name), name,
					"<p>The resource rdf:nil is an instance of rdf:List that can be used to represent an empty list or other list-like structure.</p>"
				+	"<p>A triple of the form:</p>"
				+	"<p>L rdf:rest rdf:nil</p>"
				+	"<p>states that L is an instance of rdf:List that has one item; that item can be indicated using the rdf:first property.</p>",		
				sources_nil, RDF_REF, null) 
		);	
		
		//RDF Reification vocabulary	
		name = RDFVocabularyAdapter.RDF + "Statement";
		URI [] sources_statement = { new URI("http://www.w3.org/TR/rdf-schema/#ch_statement")  };
		add( new RDFVocabularyExplanation( new URI(name), name,
					"<p>rdf:Statement is an instance of rdfs:Class. It is intended to represent the class of RDF statements. An RDF statement is the statement made by a token of an RDF triple. The subject of an RDF statement is the instance of rdfs:Resource identified by the subject of the triple. The predicate of an RDF statement is the instance of rdf:Property identified by the predicate of the triple. The object of an RDF statement is the instance of rdfs:Resource identified by the object of the triple. rdf:Statement is in the domain of the properties rdf:predicate, rdf:subject and rdf:object. Different individual rdf:Statement instances may have the same values for their rdf:predicate, rdf:subject and rdf:object properties.</p>",
				sources_statement, RDF_REF, null) 
		);	
		
		name = RDFVocabularyAdapter.RDF + "subject";
		URI [] resources_subj = { new URI("http://www.w3.org/TR/rdf-schema/#ch_subject")  };
		add( new RDFVocabularyExplanation( new URI(name), name,
					"<p>rdf:subject is an instance of rdf:Property that is used to state the subject of a statement.</p>"
				+	"<p>A triple of the form:</p>"
				+	"<p>S rdf:subject R</p>"
				+	"<p>states that S is an instance of rdf:Statement and that the subject of S is R.</p>"		
				+	"<p>The rdfs:domain of rdf:subject is rdf:Statement. The rdfs:range of rdf:subject is rdfs:Resource.</p>",
				resources_subj, RDF_REF, null) 
		);	
		
		name = RDFVocabularyAdapter.RDF + "predicate";
		URI [] sources_pred = { new URI("http://www.w3.org/TR/rdf-schema/#ch_predicate")  };
		add( new RDFVocabularyExplanation( new URI(name), name,
					"<p>rdf:predicate is an instance of rdf:Property that is used to state the predicate of a statement.</p>"
				+	"<p>A triple of the form:</p>"
				+	"<p>S rdf:predicate P</p>"
				+	"<p>states that S is an instance of rdf:Statement, that P is an instance of rdf:Property and that the predicate of S is P.</p>"		
				+	"<p>The rdfs:domain of rdf:predicate is rdf:Statement and the rdfs:range is rdfs:Resource.</p>",
				sources_pred, RDF_REF, null) 
		);	
		
		name = RDFVocabularyAdapter.RDF + "object";
		URI [] sources_obj = { new URI("http://www.w3.org/TR/rdf-schema/#ch_object")  };
		add( new RDFVocabularyExplanation( new URI(name), name,
					"<p>rdf:object is an instance of rdf:Property that is used to state the object of a statement.</p>"
				+	"<p>A triple of the form:</p>"
				+	"<p>S rdf:object O</p>"
				+	"<p>states that S is an instance of rdf:Statement and that the object of S is O.</p>"		
				+	"<p>The rdfs:domain of rdf:object is rdf:Statement. The rdfs:range of rdf:object is rdfs:Resource.</p>",
				sources_obj, RDF_REF, null) 
		);	
	
		// RDF Utility Vocabulary
		
		name = RDFVocabularyAdapter.RDF + "value";
		URI [] sources_value = { new URI("http://www.w3.org/TR/rdf-schema/#ch_value")  };
		add( new RDFVocabularyExplanation( new URI(name), name,
					"<p>rdf:value is an instance of rdf:Property that may be used in describing structured values.</p>"
				+	"<p>rdf:value has no meaning on its own. It is provided as a piece of vocabulary that may be used in idioms such as illustrated in example 16 of the RDF primer [RDF-PRIMER]. Despite the lack of formal specification of the meaning of this property, there is value in defining it to encourage the use of a common idiom in examples of this kind.</p>"
				+	"<p>The rdfs:domain of rdf:value is rdfs:Resource. The rdfs:range of rdf:value is rdfs:Resource.</p>",
					sources_value, RDF_REF, null) 
		);	
	}
	
	
	public void add(RDFVocabularyExplanation exp)
	{
		super.put( exp.getURI(), exp );
		myNumExplanations++;
	}

	public VocabularyExplanation explain(URI uri) {

		return (RDFVocabularyExplanation)super.get(uri);
	}

	public int numExplanations() {
		
		return myNumExplanations;
	}
	
	
}
