/*
 * Created on Feb 24, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.mindswap.swoop.utils.explain;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Hashtable;

import org.semanticweb.owl.io.vocabulary.OWLVocabularyAdapter;

/**
 * @author Dave
 *
 * Deposit of OWL vocabulary explanations
 * 
 */
public class OWLVocabularyExplanationDeposit extends Hashtable implements VocabularyExplanationDeposit 
{
	private static URI                             OWL_REF   = null;
	private static OWLVocabularyExplanationDeposit myDeposit = null;
	private int myNumExplanations = 0;
	
	public static OWLVocabularyExplanationDeposit getInstance()
	{
		if (myDeposit == null)
		{
			try
			{
				OWL_REF = new URI("http://www.w3.org/TR/owl-ref/");
				myDeposit = new OWLVocabularyExplanationDeposit();
			}
			catch (URISyntaxException e)
			{
				e.printStackTrace();
			}
		}
		return myDeposit;
	}
	
	private OWLVocabularyExplanationDeposit() throws URISyntaxException
	{
		URI [] sources = { OWL_REF };
		
		// OWL classes
		String name = OWLVocabularyAdapter.OWL + "AllDifferent";		
		add( new OWLVocabularyExplanation( new URI(name), name, 
				 "<p>For ontologies in which the unique-names assumption holds, the use of <code>owl:differentFrom</code> is likely to lead to a large number of statements, as all individuals have to be declared pairwise disjoint. For such situations OWL provides a special idiom in the form of the construct owl:AllDifferent. <code>owl:AllDifferent</code> is a special built-in OWL class, for which the property owl:distinctMembers <a name=\"distinctMembers-def\" id=\"distinctMembers-def\"></a> is defined, which links an instance of <code>owl:AllDifferent</code> to a list of individuals. The intended meaning of such a statement is that all individuals in the list are all different from each other.</p>"
			  +  "<p>An example:</p>"
			  +  "<p>&lt;owl:AllDifferent&gt;<br>"
			  +  TAB + "  &lt;owl:distinctMembers rdf:parseType=\"Collection\"&gt;<br>"
			  +  TAB+TAB+"   &lt;Opera rdf:about=\"#Don_Giovanni\"/&gt;<br>"
			  +  TAB+TAB+"    &lt;Opera rdf:about=\"#Nozze_di_Figaro\"/&gt;<br>"
              +  TAB+TAB+"    &lt;Opera rdf:about=\"#Cosi_fan_tutte\"/&gt;<br>"
              +  TAB+TAB+"     &lt;Opera rdf:about=\"#Tosca\"/&gt;<br>"
              +  TAB+TAB+"     &lt;Opera rdf:about=\"#Turandot\"/&gt;<br>"
              +  TAB+TAB+"   &lt;Opera rdf:about=\"#Salome\"/&gt;<br>"
              +  TAB+TAB+"  &lt;/owl:distinctMembers&gt;<br>"
              +  TAB+" &lt;/owl:AllDifferent&gt;<br> </p>"			  
			  +  "<p>This states that these six URI references all point to different operas. </p>",
			  sources, OWL_REF, null)
		);
		
		name = OWLVocabularyAdapter.OWL + "AnnotationProperty";
		URI [] sources_AP = { new URI("http://www.w3.org/TR/owl-ref/#AnnotationProperty-def"), new URI("http://www.w3.org/TR/2004/REC-owl-guide-20040210/#owl_AnnotationProperty") };
		add( new OWLVocabularyExplanation( new URI(name), name, 
				"<p>Properties that are used as annotations should be declared using owl:AnnotationProperty. E.g.</p>"
				+ "<p> &lt;owl:AnnotationProperty rdf:about=\"&dc&#059;creator\" /&gt; </p>"
				+ "<p>In addition, the following restrictions apply</p>"
				+ "<ul>"
				+ "<li>Annotation properties must have an explicit <a name=\"AnnotationProperty-def\">typing triple</a> of the form:  <p>AnnotationPropertyID rdf:type owl:AnnotationProperty . </p> </li>"
        		+ "<li>Annotation properties must not be used in property axioms. Thus, in OWL DL one cannot define subproperties or domain/range constraints for annotation properties.</li>"
        		+ "<li>The object of an annotation property must be either a data literal, a URI reference, or an individual.</li>"
        		+ "</ul>",
			  sources_AP, OWL_REF, null)
		);
		
		name = OWLVocabularyAdapter.OWL + "Class";		
		add( new OWLVocabularyExplanation( new URI(name), name,  
				 "<p>Class descriptions form the building blocks for defining classes through class axioms. The simplest form of a class axiom is a class description of type 1, It just states the existence of a class, using <a name=\"Class-def\" id=\"Class-def\"><code>owl:Class</code></a> with a class identifier.</p> <p>For example, the following class axiom declares the URI reference <code>#Human</code> to be the name of an OWL class:</p><br> &lt;owl:Class rdf:ID=\"Human\"/&gt  "
			  +  "<p>This is correct OWL, but does not tell us very much about the class <code>Human</code>. Class axioms typically contain additional components that state necessary and/or sufficient characteristics of a class. OWL contains three language constructs for combining class descriptions into class axioms:</p>" 
			  +  "<ul>"
			  +  "<li><a href=\"http://www.w3.org/2000/01/rdf-schema#subClassOf\"><code>rdfs:subClassOf</code></a> allows one to say that the class extension of a class description is a subset of the class extension of another class description.</li>"
			  +  "<li><a href=\"http://www.w3.org/2002/07/owl#equivalentClass\"><code>owl:equivalentClass</code></a> allows one to say that a class description has exactly the same class extension as another class  description.</li>"
			  +  "<li><a href=\"http://www.w3.org/2002/07/owl#disjointWith\"><code>owl:disjointWith</code></a> allows one to say that the class extension of a class description has no members incommon with the class extension of another class description.</li>"
			  +  " </ul>"
			  +  "<p>Syntactically, these three language constructs are properties that have a class description as both domain and range. We discuss these properties in more detail in the following subsections.</p>"
			  +  "<p>In addition, OWL allows class axioms in which a class description of the enumeration or the set-operator type is given a name. These class axioms are semantically equivalent to class axioms with a <code>owl:equivalentClass</code> statement, so these will be discussed right after that subsection.</p>",
		         sources, OWL_REF, null) );
		
		name = OWLVocabularyAdapter.OWL + "DataRange";
		//name = OWLVocabularyAdapter.OWL + "Class";
		add( new OWLVocabularyExplanation( new URI(name), name,  
				"<p>In addition to the RDF datatypes, OWL provides one additional construct for defining a range of data values, namely an enumerated datatype. This datatype format makes use of the owl:oneOf construct, that is also used for describing an enumerated class. In the case of an enumerated datatype, the subject of owl:oneOf is a blank node of class owl:DataRange and the object is a list of literals. Unfortunately, we cannot use the rdf:parseType=\"Collection\" idiom for specifying the literal list, because RDF requires the collection to be a list of RDF node elements. Therefore we have to specify the list of data values with the basic list constructs rdf:first, rdf:rest and rdf:nil.</p>"
			+ 	"<p>NOTE: Enumerated datatypes are not part of OWL Lite.</p>"
			+   "<p>The example below specifies the range of the property tennisGameScore to be the list of integer values {0, 15, 30, 40}:.</p>"
			+	"<p>&lt;owl:DatatypeProperty rdf:ID=\"tennisGameScore\"&gt;</p>"
			+	TAB+"&lt;rdfs:range&gt;<br>"
			+	TAB+TAB+"&lt;owl:DataRange&gt;<br>"
			+	TAB+TAB+TAB+"&lt;owl:oneOf&gt;<br>"
			+	TAB+TAB+TAB+TAB+"&lt;rdf:List&gt;<br>"
			+	TAB+TAB+TAB+TAB+TAB+TAB+"&lt;rdf:first rdf:datatype=\"&amp;xsd;integer\"&gt;0&lt;/rdf:first&gt;<br>"
			+	TAB+TAB+TAB+TAB+TAB+TAB+"&lt;rdf:rest&gt;<br>"
			+	TAB+TAB+TAB+TAB+TAB+TAB+"&lt;rdf:List&gt;<br>"
			+	TAB+TAB+TAB+TAB+TAB+TAB+TAB+"&lt;rdf:first rdf:datatype=\"&amp;xsd;integer\"&gt;15&lt;/rdf:first&gt;<br>"
			+	TAB+TAB+TAB+TAB+TAB+TAB+TAB+"&lt;rdf:rest&gt;<br>"
			+	TAB+TAB+TAB+TAB+TAB+TAB+TAB+TAB+"&lt;rdf:List&gt;<br>"
			+	TAB+TAB+TAB+TAB+TAB+TAB+TAB+TAB+TAB+"&lt;rdf:first rdf:datatype=\"&amp;xsd;integer\"&gt;30&lt;/rdf:first&gt;<br>"
			+	TAB+TAB+TAB+TAB+TAB+TAB+TAB+TAB+TAB+"&lt;rdf:rest&gt;<br>"
			+	TAB+TAB+TAB+TAB+TAB+TAB+TAB+TAB+TAB+TAB+"&lt;rdf:List&gt;<br>"
			+	TAB+TAB+TAB+TAB+TAB+TAB+TAB+TAB+TAB+TAB+TAB+"&lt;rdf:first rdf:datatype=\"&amp;xsd;integer\"&gt;40&lt;/rdf:first&gt;<br>"
			+	TAB+TAB+TAB+TAB+TAB+TAB+TAB+TAB+TAB+TAB+"&lt;rdf:rest rdf:resource=\"&amp;rdf;nil\" /&gt;<br>"
			+	TAB+TAB+TAB+TAB+TAB+TAB+TAB+TAB+TAB+TAB+"&lt;/rdf:List&gt;<br>"
			+	TAB+TAB+TAB+TAB+TAB+TAB+TAB+TAB+TAB+"&lt;/rdf:rest&gt;<br>"
			+	TAB+TAB+TAB+TAB+TAB+TAB+TAB+TAB+"&lt;/rdf:List&gt;<br>"
			+	TAB+TAB+TAB+TAB+TAB+TAB+TAB+"&lt;/rdf:rest&gt;<br>"
			+	TAB+TAB+TAB+TAB+TAB+TAB+"&lt;/rdf:List&gt;<br>"
			+	TAB+TAB+TAB+TAB+TAB+"&lt;/rdf:rest&gt;<br>"
			+	TAB+TAB+TAB+TAB+"&lt;/rdf:List&gt;<br>"
			+	TAB+TAB+TAB+"&lt;/owl:oneOf&gt;<br>"
			+	TAB+TAB+"&lt;/owl:DataRange&gt;<br>"
			+	TAB+"&lt;/rdfs:range&gt;<br>"
			+	"&lt;/owl:DatatypeProperty&gt;<br>",
	         sources, OWL_REF, null) );

		name = OWLVocabularyAdapter.OWL + "DatatypeProperty";
		URI [] sources_DT = { new URI("http://www.w3.org/TR/2004/REC-owl-guide-20040210/#owl_DatatypeProperty")  };
		add( new OWLVocabularyExplanation( new URI(name), name,		
				"We distinguish properties according to whether they relate individuals to individuals (object properties) or individuals to datatypes (datatype properties). Datatype properties may range over RDF literals or simple types defined in accordance with http://www.w3.org/TR/xmlschema-2/ -- XML Schema datatypes. </p>"
			+   "<p> <a id=\"owl_DatatypeProperty\" name=\"owl_DatatypeProperty\"></a> OWL uses most of the built-in XML Schema datatypes.  References to these datatypes are by means of the URI reference for the datatype, <tt>http://www.w3.org/2001/XMLSchema</tt>. The following datatypes are <em>recommended</em> for use with OWL:  </p><br>"
			+	"xsd:string, xsd:normalizedString, xsd:boolean, xsd:decimal, xsd:float, xsd:double, xsd:integer, xsd:nonNegativeInteger"
			+   "xsd:positiveInteger, xsd:nonPositiveInteger, xsd:negativeInteger, xsd:long, xsd:int, xsd:short, xsd:byte, xsd:unsignedLong, xsd:unsignedInt, xsd:unsignedShort, xsd:unsignedByte"
			+	"xsd:hexBinary, xsd:base64Binary, xsd:dateTime, xsd:time, xsd:date, xsd:gYearMonth, xsd:gYear, xsd:gMonthDay, xsd:gDay, xsd:gMonth, "
			+ 	"xsd:anyURI, xsd:token, xsd:language, xsd:NMTOKEN, xsd:Name, xsd:NCName"
			+	"<p>"
			+	"The above datatypes, plus <tt>rdfs:Literal</tt>, form the built-in OWL datatypes." 
			+	"All OWL reasoners are required to support the <tt>xsd:integer</tt>"
			+	"and <tt>xsd:string</tt> datatypes."
			+	"</p>"
			+	"<p>Other built-in XML Schema datatypes may be used in OWL Full, but with caveats described in the http://www.w3.org/TR/2004/REC-owl-semantics-20040210/syntax.html -- OWL Semantics and Abstract Syntax</a> documentation </p><br>"
			+	"&lt;owl:Class rdf:ID=\"VintageYear\" /&gt;<br>"
			+	"&lt;owl:DatatypeProperty rdf:ID=\"yearValue\"&gt;<br>"
			+	TAB+"&lt;rdfs:domain rdf:resource=\"#VintageYear\" /&gt;<br>"
			+	TAB+"&lt;rdfs:range  rdf:resource=\"&amp;xsd;positiveInteger\"/&gt;<br>"
			+	"&lt;/owl:DatatypeProperty&gt;<br>",
				sources_DT, OWL_REF, null) 
			);		
		
			name = OWLVocabularyAdapter.OWL + "DeprecatedClass";
			URI [] sources_DeprecatedClass = { new URI("http://www.w3.org/TR/owl-ref/#DeprecatedClass-def")  };
			add( new OWLVocabularyExplanation( new URI(name), name,
				"<p >Deprecation is a feature commonly used in versioning software (for example, see the Java programming language) to indicate that a particular feature is preserved for backward-compatibility purposes, but may be phased out in the future. Here, a specific identifier is said to be of type http://www.w3.org/TR/2004/REC-owl-semantics-20040210/#owl_DeprecatedClass -- owl:DeprecatedClass"
			+	"or http://www.w3.org/TR/2004/REC-owl-semantics-20040210/#owl_DeprecatedProperty -- owl:DeprecatedProperty where <code>owl:DeprecatedClass</code> is a subclass  <code>rdfs:Class</code> and <code>owl:DeprecatedProperty</code> is a subclass of <code>rdf:Property</code>.  By deprecating a term, it means that the term should not be used in new documents that commit to the ontology.  This allows an ontology to maintain"
			+ 	"backward-compatibility while phasing out an old vocabulary (thus, it only makes sense to use deprecation in combination with backward compatibility).  As a result, it it easier for old data and applications to migrate to a new version, and thus can increase the level of adoption of the new version.  This has no meaning in the model theoretic semantics other than that given by the RDF(S) model theory.  However, authoring tools may use it to warn users when checking OWL markup.</p>"
			+ 	"<p>An example of deprecation is:</p>" 
			+ 	"<p>&lt;owl:Ontology rdf:about=\"\"&gt;" + NL
			+	TAB+"&lt;rdfs:comment&gt;Vehicle Ontology, v. 1.1&lt;/rdfs:comment&gt;" + NL
			+	TAB+"&lt;owl:backwardCompatibleWith" + NL
			+	TAB+"TAB+rdf:resource=\"http://www.example.org/vehicle-1.0\"/&gt;" + NL
			+	TAB+"&lt;owl:priorVersion rdf:resource=\"http://www.example.org/vehicle-1.0\"/&gt;" + NL
			+	"&lt;/owl:Ontology&gt;" + NL
			+	"&lt;owl:DeprecatedClass rdf:ID=\"Car\"&gt;" + NL
			+	TAB+"&lt;rdfs:comment&gt;Automobile is now preferred&lt;/rdfs:comment&gt;" + NL
			+	TAB+"&lt;owl:equivalentClass rdf:resource=\"#Automobile\"/&gt;" + NL
			+	TAB+"&lt;!-- note that equivalentClass only means that the classes have the same extension, so this DOES NOT lead to the entailment that  Automobile is of type DeprecatedClass too --&gt;" + NL
			+	"&lt;/owl:DeprecatedClass&gt;" + NL
			+	"&lt;owl:Class rdf:ID=\"Automobile\" /&gt;" + NL
			+	"&lt;owl:DeprecatedProperty rdf:ID=\"hasDriver\"&gt;" + NL
			+	TAB+"&lt;rdfs:comment&gt;inverse property drives is now preferred&lt;/rdfs:comment&gt;" + NL
			+	TAB+"&lt;owl:inverseOf rdf:resource=\"#drives\" /&gt;" + NL
			+	"&lt;/owl:DeprecatedProperty&gt;" + NL
			+	"&lt;owl:ObjectProperty rdf:ID=\"drives\" /&gt;</p>",
				sources_DeprecatedClass, OWL_REF, null) 
			);		
			
			name = OWLVocabularyAdapter.OWL + "DeprecatedProperty";
			URI [] sources_DeprecatedProperty = { new URI("http://www.w3.org/TR/owl-ref/#DeprecatedProperty-def")  };
			add( new OWLVocabularyExplanation( new URI(name), name,
				"<p >Deprecation is a feature commonly used in versioning software (for example, see the Java programming language) to indicate that a particular feature is preserved for backward-compatibility purposes, but may be phased out in the future. Here, a specific identifier is said to be of type http://www.w3.org/TR/2004/REC-owl-semantics-20040210/#owl_DeprecatedClass -- owl:DeprecatedClass"
			+	"or http://www.w3.org/TR/2004/REC-owl-semantics-20040210/#owl_DeprecatedProperty -- owl:DeprecatedProperty where <code>owl:DeprecatedClass</code> is a subclass  <code>rdfs:Class</code> and <code>owl:DeprecatedProperty</code> is a subclass of <code>rdf:Property</code>.  By deprecating a term, it means that the term should not be used in new documents that commit to the ontology.  This allows an ontology to maintain"
			+ 	"backward-compatibility while phasing out an old vocabulary (thus, it only makes sense to use deprecation in combination with backward compatibility).  As a result, it it easier for old data and applications to migrate to a new version, and thus can increase the level of adoption of the new version.  This has no meaning in the model theoretic semantics other than that given by the RDF(S) model theory.  However, authoring tools may use it to warn users when checking OWL markup.</p>"
			+ 	"<p>An example of deprecation is:</p>" 
			+ 	"<p>&lt;owl:Ontology rdf:about=\"\"&gt;" + NL
			+	TAB+"&lt;rdfs:comment&gt;Vehicle Ontology, v. 1.1&lt;/rdfs:comment&gt;" + NL
			+	TAB+"&lt;owl:backwardCompatibleWith" + NL
			+	TAB+"TAB+rdf:resource=\"http://www.example.org/vehicle-1.0\"/&gt;" + NL
			+	TAB+"&lt;owl:priorVersion rdf:resource=\"http://www.example.org/vehicle-1.0\"/&gt;" + NL
			+	"&lt;/owl:Ontology&gt;" + NL
			+	"<br>" + NL
			+	"&lt;owl:DeprecatedClass rdf:ID=\"Car\"&gt;" + NL
			+	TAB+"&lt;rdfs:comment&gt;Automobile is now preferred&lt;/rdfs:comment&gt;" + NL
			+	TAB+"&lt;owl:equivalentClass rdf:resource=\"#Automobile\"/&gt;" + NL
			+	TAB+"&lt;!-- note that equivalentClass only means that the classes have the same extension, so this DOES NOT lead to the entailment that  Automobile is of type DeprecatedClass too --&gt;" + NL
			+	"&lt;/owl:DeprecatedClass&gt;" + NL
			+	"<br>" + NL
			+	"&lt;owl:Class rdf:ID=\"Automobile\" /&gt;" + NL
			+	"&lt;owl:DeprecatedProperty rdf:ID=\"hasDriver\"&gt;" + NL
			+	TAB+"&lt;rdfs:comment&gt;inverse property drives is now preferred&lt;/rdfs:comment&gt;" + NL
			+	TAB+"&lt;owl:inverseOf rdf:resource=\"#drives\" /&gt;" + NL
			+	"&lt;/owl:DeprecatedProperty&gt;" + NL
			+	"<br>" + NL
			+	"&lt;owl:ObjectProperty rdf:ID=\"drives\" /&gt; </p>",
				sources_DeprecatedProperty, OWL_REF, null) 
			);
			
			name = OWLVocabularyAdapter.OWL + "FunctionalProperty";
			URI [] sources_FP = { new URI("http://www.w3.org/TR/owl-ref/#FunctionalProperty-def")  };
			add( new OWLVocabularyExplanation( new URI(name), name,
				"<p>A functional property is a property that can have only one (unique) value y for each instance x, i.e. there cannot be two distinct values y1 and y2 such that the pairs (x,y1) and (x,y2) are both instances of this property. Both object properties and datatype properties can be declared as \"functional\". For this purpose, OWL defines the built-in class owl:FunctionalProperty as a special subclass of the RDF class rdf:Property."
			+	"The following axiom states that the husband property is functional, i.e., a woman can have at most one husband (a good example of culture dependence of ontologies): </p> <br>"
			+	"&lt;owl:ObjectProperty rdf:ID=\"husband\"&gt;"+ NL
			+	TAB+"&lt;rdf:type    rdf:resource=\"&amp;owl;FunctionalProperty\" /&gt;"+ NL
			+	TAB+"&lt;rdfs:domain rdf:resource=\"#Woman\" /&gt;"+ NL
			+	"&lt;rdfs:range  rdf:resource=\"#Man\" /&gt;"+ NL
			+	"&lt;/owl:ObjectProperty&gt;" + NL
			+	"<p>As always, there are syntactic variations. The example above is semantically equivalent to the one below:</p>" + NL 
			+ "&lt;owl:ObjectProperty rdf:ID=\"husband\"&gt;" + NL
			+ TAB + " &lt;rdfs:domain rdf:resource=\"#Woman\" /&gt;" + NL 
			+ TAB + "  &lt;rdfs:range  rdf:resource=\"#Man\" /&gt;" + NL 
			+ "&lt;/owl:ObjectProperty&gt;" + NL
			+ "&lt;owl:FunctionalProperty rdf:about=\"#husband\" /&gt;",
				sources_FP, OWL_REF, null) 
			);
			
			name = OWLVocabularyAdapter.OWL + "InverseFunctionalProperty";
			URI [] sources_IFP = { new URI("http://www.w3.org/TR/owl-ref/#InverseFunctionalProperty-def")  };
			add( new OWLVocabularyExplanation( new URI(name), name,
				"<p>If a property is declared to be inverse-functional, then the object of a property statement uniquely determines the subject (some individual). More formally, if we state that P is an owl:InverseFunctionalProperty, then this asserts that a value y can only be the value of P for a single instance x, i.e. there cannot be two distinct instances x1 and x2 such that both pairs (x1,y) and (x2,y) are instances of P.</p>"
			+	"<p>Syntactically, an inverse-functional property axiom is specified by declaring the property to be an instance of the built-in OWL class owl:InverseFunctionalProperty, which is a subclass of the OWL class owl:ObjectProperty.</p>"
			+	"<p>NOTE: Because in OWL Full datatype properties are a subclass of object properties, an inverse-functional property can be defined for datatype properties. In OWL DL object properties and datatype properties are disjoint, so an inverse-functional property cannot be defined for datatype properties.</p>"
			+   "<p>A typical example of an inverse-functional property:</p><br>"
			+	"&lt;owl:InverseFunctionalProperty rdf:ID=\"biologicalMotherOf\"&gt;" + NL
			+	TAB+"&lt;rdfs:domain rdf:resource=\"#Woman\"/&gt;" + NL
			+	TAB+"&lt;rdfs:range rdf:resource=\"#Human\"/&gt;"  + NL
			+	"&lt;/owl:InverseFunctionalProperty&gt;" +NL			
			+	"<p>This example states that for each object of biologicalMotherOf statements (some human) one should be able to uniquely identify a subject (some woman). Inverse-functional properties resemble the notion of a key in databases.</P>" 
			+	"<p>One difference with functional properties is that for inverse-functional properties no additional object-property or datatype-property axiom is required: inverse-functional properties are by definition object properties.</p>"    
			+	"<p>Notice that owl:FunctionalProperty and owl:InverseFunctionalProperty specify global cardinality constraints. That is, no matter which class the property is applied to, the cardinality constraints must hold. This is different from the cardinality constraints contained in property restrictions. The latter are class descriptions and are only enforced on the property when applied to that class.</p>",
				sources_IFP, OWL_REF, null) 
			);
			
			name = OWLVocabularyAdapter.OWL + "Nothing";
			URI [] sources_nothing = { new URI("http://www.w3.org/TR/owl-ref/#Nothing-def")  };
			add( new OWLVocabularyExplanation( new URI(name), name,
					"<p>Two OWL class identifiers are predefined, namely the classes  owl:Thing and  owl:Nothing. The class extension of owl:Thing is the set of all individuals. The class extension of owl:Nothing is the empty set. Consequently, every OWL class is a subclass of owl:Thing and owl:Nothing is a subclass of every class (for the meaning of the subclass relation, see rdfs:subClassOf).</p>",
					sources_nothing, OWL_REF, null) 
			);	
			
			name = OWLVocabularyAdapter.OWL + "ObjectProperty";
			URI [] sources_OP = { new URI("http://www.w3.org/TR/2004/REC-owl-guide-20040210/#owl_ObjectProperty")  };
			add( new OWLVocabularyExplanation( new URI(name), name,
					"<p>Object properties, relations between instances of two classes. Note that the name object property is not intended to reflect a connection with the RDF term rdf:object</p>",
					sources_OP, OWL_REF, null) 
			);		
			
			
			name = OWLVocabularyAdapter.OWL + "Ontology";
			URI [] sources_Ont = { new URI("http://www.w3.org/TR/owl-ref/#Ontology-def")  };
			add( new OWLVocabularyExplanation( new URI(name), name,
					"<p>A document describing an ontology typically contains information about the ontology itself. An ontology is a resource, so it may be described using properties from the OWL and other namespaces, e.g.:</p><br>"
				+	"&lt;owl:Ontology rdf:about=\"\"&gt;" +NL
				+	TAB+"&lt;owl:versionInfo&gt; ... &lt;/owl:versionInfo&gt;" + NL
				+	TAB+"&lt;rdfs:comment&gt;...&lt;/rdfs:comment&gt;"+ NL
				+	TAB+"&lt;owl:imports rdf:resource=\"...\"/&gt;" + NL
				+	"&lt;/owl:Ontology&gt;"
				+	"<p>This is commonly called the ontology header and is typically found near the beginning of the RDF/XML document. The line</p><br>"
				+ 	"&lt;owl:Ontology rdf:about=\"\"&gt;"
				+	"<P>states that this block describes the current ontology. More precisely, it states the current base URI identifies an instance of the class owl:Ontology. It is recommended that the base URI be defined using an xml:base attribute in the &lt;rdf:RDF&gt; element at the beginning of the document.</P>"
				+	"&lt;owl:Ontology rdf:about=\"\"&gt;" + NL
				+	TAB+"&lt;owl:versionInfo&gt;v 1.17 2003/02/26 12:56:51 mdean&lt;/owl:versionInfo&gt;" + NL
				+	TAB+"&lt;rdfs:comment&gt;An example ontology&lt;/rdfs:comment&gt;" + NL
				+	TAB+"&lt;owl:imports rdf:resource=\"http://www.example.org/foo\"/&gt;" + NL
				+	"&lt;/owl:Ontology&gt;" + NL,
					sources_Ont, OWL_REF, null)
			);		

			name = OWLVocabularyAdapter.OWL + "OntologyProperty";
			URI [] sources_OntP = { new URI("http://www.w3.org/TR/owl-ref/#OntologyProperty-def")  };
			add( new OWLVocabularyExplanation( new URI(name), name,
			  "The ontology-import construct owl:imports and the ontology-versioning constructs owl:priorVersion, owl:backwardCompatibleWith and owl:incompatibleWith are defined in the OWL vocabulary as instances of the OWL built-in class owl:OntologyProperty. Instances of owl:OntologyProperty must have the class owl:Ontology as their domain and range. It is permitted to define other instances of owl:OntologyProperty. In OWL DL for ontology properties the same constraints hold as those specified for annotation properties in Sec. 7.1.",
			  sources_OntP, OWL_REF, null)
			);	
			
			name = OWLVocabularyAdapter.OWL + "Restriction";
			URI [] sources_Restriction = { new URI("http://www.w3.org/TR/owl-ref/#Restriction-def"),  new URI("http://www.w3.org/TR/2004/REC-owl-guide-20040210/#owl_Restriction") };
			add( new OWLVocabularyExplanation( new URI(name), name,
			"<p>In addition to designating property characteristics, it is possible to further constrain the range of a property in specific contexts in a variety of ways. We do this with property restrictions. The various forms described below can only be used within the context of an owl:Restriction. The owl:onProperty element indicates the restricted property.</p>"
			+ "<p>The class owl:Restriction is defined as a subclass of owl:Class. A restriction class should have exactly one triple linking the restriction to a particular property, using the  owl:onProperty property. The restriction class should also have exactly one triple that represents the value constraint c.q. cardinality constraint on the property under consideration, e.g., that the cardinality of the property is exactly 1.</p>",
					sources_Restriction, OWL_REF, null)
			);
			
			name = OWLVocabularyAdapter.OWL + "SymmetricProperty";
			URI [] sources_SymmProp = { new URI("http://www.w3.org/TR/owl-ref/#SymmetricProperty-def")};
			add( new OWLVocabularyExplanation( new URI(name), name,			
				"<p>A symmetric property is a property for which holds that if the pair (x,y) is an instance of P, then the pair (y,x) is also an instance of P. Syntactically, a property is defined as symmetric by making it an instance of the built-in OWL class owl:SymmetricProperty, a subclass of owl:ObjectProperty.</p>"
			+	"<p>A popular example of a symmetric property is the friendOf relation:</p><br>"
			+	"&lt;owl:SymmetricProperty rdf:ID=\"friendOf\"&gt;" + NL
			+ 	TAB +  "&lt;rdfs:domain rdf:resource=\"#Human\"/&gt;" + NL
			+ 	TAB+  "&lt;rdfs:range  rdf:resource=\"#Human\"/&gt;" + NL
			+ 	"&lt;/owl:SymmetricProperty&gt;" + NL
			+	"<p>The domain and range of a symmetric property are the same.</p>",
				sources_SymmProp, OWL_REF, null)
			);
			
			
			name = OWLVocabularyAdapter.OWL + "Thing";
			URI [] sources_thing = { new URI("http://www.w3.org/TR/owl-ref/#Thing-def")  };
			add( new OWLVocabularyExplanation( new URI(name), name,
					"<p>Two OWL class identifiers are predefined, namely the classes  owl:Thing and  owl:Nothing. The class extension of owl:Thing is the set of all individuals. The class extension of owl:Nothing is the empty set. Consequently, every OWL class is a subclass of owl:Thing and owl:Nothing is a subclass of every class (for the meaning of the subclass relation, see rdfs:subClassOf).</p>",
					sources_thing, OWL_REF, null) 
			);	

			name = OWLVocabularyAdapter.OWL + "TransitiveProperty";
			URI [] sources_tranProp = { new URI("http://www.w3.org/TR/owl-ref/#TransitiveProperty-def")  };
			add( new OWLVocabularyExplanation( new URI(name), name,			
					"<p>When one defines a property P to be a transitive property, this means that if a pair (x,y) is an instance of P, and the pair (y,z) is also instance of P, then we can infer the the pair (x,z) is also an instance of P.</p>"
				+	"<p>Syntactically, a property is defined as being transitive by making it an instance of the the built-in OWL class owl:TransitiveProperty, which is defined as a subclass of owl:ObjectProperty.</p>"
				+	"<p>Typical examples of transitive properties are properties representing certain part-whole relations. For example, we might want to say that the subRegionOf property between regions is transitive:</p><br>"			
				+	"&lt;owl:TransitiveProperty rdf:ID=\"subRegionOf\"&gt;"+ NL
				+ 	TAB+ "&lt;rdfs:domain rdf:resource=\"#Region\"/&gt;"+ NL
				+ 	TAB +"&lt;rdfs:range  rdf:resource=\"#Region\"/&gt;"+ NL
				+	"&lt;/owl:TransitiveProperty&gt;" + NL
				+	"<p>From this an OWL reasoner should be able to derive that if ChiantiClassico, Tuscany and Italy are regions, and ChiantiClassico is a subregion of Tuscany, and Tuscany is a subregion of Italy, then ChiantiClassico is also a subregion of Italy.</p>"
				+	"<p>Note that because owl:TransitiveProperty is a subclass of owl:ObjectProperty, the following syntactic variant is equivalent to the example above:</p><br>"		
				+	"&lt;owl:ObjectProperty rdf:ID=\"subRegionOf\"&gt;"+ NL
				+ TAB +  "&lt;rdf:type rdf:resource=\"&amp;owl;TransitiveProperty\"/&gt;"+ NL
				+ TAB +  "&lt;rdfs:domain rdf:resource=\"#Region\"/&gt;"+ NL
				+ TAB +	 "&lt;rdfs:range  rdf:resource=\"#Region\"/&gt;"+ NL
				+	"&lt;/owl:ObjectProperty&gt;"+ NL
				+ "<p>NOTE: OWL DL requires that for a transitive property no local or global cardinality constraints should be declared on the property itself or its superproperties, nor on the inverse of the property or its superproperties.</p>",
					sources_tranProp, OWL_REF, null) 
			);				
			
			
			// OWL properties
			
			
			name = OWLVocabularyAdapter.OWL + "allValuesFrom";
			URI [] sources_AVF = { new URI("http://www.w3.org/TR/owl-ref/#allValuesFrom-def")  };
			add( new OWLVocabularyExplanation( new URI(name), name,
					"<p>The value constraint owl:allValuesFrom is a built-in OWL property that links a restriction class to either a class description or a data range. A restriction containing an owl:allValuesFrom constraint is used to describe a class of all individuals for which all values of the property under consideration are either members of the class extension of the class description or are data values within the specified data range. In other words, it defines a class of individuals x for which holds that if the pair (x,y) is an instance of P (the property concerned), then y should be an instance of the class description or a value in the data range, respectively.</p>"
				+	"<p>A simple example:</p><br>"
				+ 	"&lt;owl:Restriction&gt;" + NL
				+	TAB+ "&lt;owl:onProperty rdf:resource=\"#hasParent\" /&gt;" + NL
				+	TAB+ "&lt;owl:allValuesFrom rdf:resource=\"#Human\"  /&gt;" + NL
				+	"&lt;/owl:Restriction&gt;"				
				+	"<p>This example describes an anonymous OWL class of all individuals for which the hasParent property only has values of class Human. Note that this class description does not state that the property always has values of this class; just that this is true for individuals that belong to the class extension of the anonymous restriction class.</p>"
				+	"<p>NOTE: In OWL Lite the only type of class description allowed as object of owl:allValuesFrom is a class name.</p>"
				+	"<p>An owl:allValuesFrom constraint is analogous to the universal (for-all) quantifier of Predicate logic - for each instance of the class that is being described, every value for P must fulfill the constraint. Also notice that the correspondence of owl:allValuesFrom with the universal quantifier means that an owl:allValuesFrom constraint for a property P is trivially satisfied for an individual that has no value for property P at all. To see why this is so, observe that the owl:allValuesFrom constraint demands that all values of P should be of type T, and if no such values exist, the constraint is trivially true.</p>",
				sources_AVF, OWL_REF, null) 
			);	
			
			name = OWLVocabularyAdapter.OWL + "backwardCompatibleWith";
			URI [] sources_BCW = { new URI("http://www.w3.org/TR/owl-ref/#backwardCompatibleWith-def")  };
			add( new OWLVocabularyExplanation( new URI(name), name,
					"<p>An owl:backwardCompatibleWith statement contains a reference to another ontology. This identifies the specified ontology as a prior version of the containing ontology, and further indicates that it is backward compatible with it. In particular, this indicates that all identifiers from the previous version have the same intended interpretations in the new version. Thus, it is a hint to document authors that they can safely change their documents to commit to the new version (by simply updating namespace declarations and owl:imports statements to refer to the URL of the new version). If owl:backwardCompatibleWith is not declared for two versions, then compatibility should not be assumed.</p>"
				+	"<p>owl:backwardCompatibleWith has no meaning in the model theoretic semantics other than that given by the RDF(S) model theory.</p>"
				+	"<p>owl:backwardCompatibleWith is a built-in OWL property with the class owl:Ontology as its domain and range.</p>"
				+	"<p>NOTE: owl:backwardCompatibleWith is an instance of owl:OntologyProperty.</p>",
				sources_BCW, OWL_REF, null) 
			);
			
			name = OWLVocabularyAdapter.OWL + "cardinality";
			URI [] sources_cardinality = { new URI("http://www.w3.org/TR/owl-ref/#cardinality-def")  };
			add( new OWLVocabularyExplanation( new URI(name), name,
					"<p>The cardinality constraint owl:cardinality is a built-in OWL property that links a restriction class to a data value belonging to the range of the XML Schema datatype nonNegativeInteger. A restriction containing an owl:cardinality constraint describes a class of all individuals that have  exactly N semantically distinct values (individuals or data values) for the property concerned, where N is the value of the cardinality constraint. Syntactically, the cardinality constraint is represented as an RDF property element with the corresponding rdf:datatype attribute</p>"
				+	"<p>This construct is in fact redundant as it can always be replaced by a pair of matching owl:minCardinality and owl:maxCardinality constraints with the same value. It is included as a convenient shorthand for the user.</p>"
				+	"<p>The following example describes a class of individuals that have exactly two parents:</p><br>"
				+	"&lt;owl:Restriction&gt;" + NL
				+ 	TAB +  "&lt;owl:onProperty rdf:resource=\"#hasParent\" /&gt;" + NL
				+ 	TAB +  "&lt;owl:cardinality rdf:datatype=\"&amp;xsd;nonNegativeInteger\"&gt;2&lt;/owl:cardinality&gt;" + NL
				+  	"&lt;/owl:Restriction&gt;" +NL,
				sources_cardinality, OWL_REF, null) 
			);
			
			name = OWLVocabularyAdapter.OWL + "complementOf";
			URI [] sources_compOf = { new URI("http://www.w3.org/TR/owl-ref/#complementOf-def")  };
			add( new OWLVocabularyExplanation( new URI(name), name,			
					"<p>An owl:complementOf property links a class to precisely one class description. An owl:complementOf statement describes a class for which the class extension contains exactly those individuals that do not belong to the class extension of the class description that is the object of the statement. owl:complementOf is analogous to logical negation: the class extension consists of those individuals that are NOT members of the class extension of the complement class.</p>"
				+	"<p>As an example of the use of complement, the expression \"not meat\" could be written as:</p><br>"
				+	"&lt;owl:Class&gt;"
				+	TAB+"&lt;owl:complementOf&gt;" + NL
				+	TAB+TAB+"&lt;owl:Class rdf:about=\"#Meat\"/&gt;" + NL
				+	TAB+"&lt;/owl:complementOf&gt;" + NL
				+	"&lt;/owl:Class&gt;"
				+	"<p>The extension of this class description contains all individuals that do not belong to the class Meat.</p>"
				+	"<p>NOTE: owl:complementOf is not part of OWL Lite.</p>",
					sources_compOf, OWL_REF, null) 
			);
			
			name = OWLVocabularyAdapter.OWL + "differentFrom";
			URI [] sources_DiffFrom = { new URI("http://www.w3.org/TR/owl-ref/#differentFrom-def")  };
			add( new OWLVocabularyExplanation( new URI(name), name,	
					"<p>The built-in OWL owl:differentFrom property links an individual to an individual. An owl:differentFrom statement indicates that two URI references refer to different individuals.</p>"
				+	"<p>An example:</p><br>"
				+	"&lt;Opera rdf:ID=\"Don_Giovanni\"/&gt;" + NL
				+	"&lt;Opera rdf:ID=\"Nozze_di_Figaro\"&gt;" + NL
				+	TAB + "&lt;owl:differentFrom rdf:resource=\"#Don_Giovanni\"/&gt;" + NL
				+	"&lt;/Opera&gt;"
				+	"&lt;Opera rdf:ID=\"Cosi_fan_tutte\"&gt;" + NL
				+	TAB + "&lt;owl:differentFrom rdf:resource=\"#Don_Giovanni\"/&gt;"+ NL
				+	TAB + "&lt;owl:differentFrom rdf:resource=\"#Nozze_di_Figaro\"/&gt;" + NL
				+	"&lt;/Opera&gt;"
				+	"<p>This states that there are three different operas.</p>", 
				sources_DiffFrom, OWL_REF, null) 
			);
			
			name = OWLVocabularyAdapter.OWL + "disjointWith";
			URI [] sources_DisjointWith = { new URI("http://www.w3.org/TR/owl-ref/#disjointWith-def")  };
			add( new OWLVocabularyExplanation( new URI(name), name,
						"<p>A class axiom may also contain (multiple) owl:disjointWith statements. owl:disjointWith is a built-in OWL property with a class description as domain and range. Each owl:disjointWith statement asserts that the class extensions of the two class descriptions involved have no individuals in common. Like axioms with rdfs:subClassOf, declaring two classes to be disjoint is a partial definition: it imposes a necessary but not sufficient condition on the class.</p>"
					+	"<p>This is a popular example of class disjointness:</p><br>"
					+	"&lt;owl:Class rdf:about=\"#Man\"&gt;" + NL 
					+	TAB+"&lt;owl:disjointWith rdf:resource=\"#Woman\"/&gt;" + NL 
					+	"&lt;/owl:Class&gt;"
					+	"<p>Whether this is actually true is a matter for biologists to decide. The following example shows a common use of class disjointness in subclass hierarchies:</p><br>"
					+	"&lt;owl:Class rdf:about=\"#MusicDrama\"&gt;" + NL 
					+ 	TAB + "&lt;owl:equivalentClass&gt;" + NL 
					+ 	TAB + TAB + "&lt;owl:Class&gt;" + NL 
					+ 	TAB + TAB + TAB + "&lt;owl:unionOf rdf:parseType=\"Collection\"&gt;" + NL 
					+ 	TAB + TAB + TAB + TAB + "&lt;owl:Class rdf:about=\"#Opera\"/&gt;" + NL 
					+ 	TAB + TAB +TAB +TAB + "&lt;owl:Class rdf:about=\"#Operetta\"/&gt;" + NL 
					+ 	TAB + TAB + TAB +TAB + "&lt;owl:Class rdf:about=\"#Musical\"/&gt;" + NL 
					+	TAB + TAB + TAB +     "&lt;/owl:unionOf&gt;" + NL 
					+ 	TAB + TAB +   "&lt;/owl:Class&gt;" + NL 
					+ 	TAB +  "&lt;/owl:equivalentClass&gt;" + NL 
					+	"&lt;/owl:Class&gt;"+ NL
					+ 	"<br>"
					+	"&lt;owl:Class rdf:about=\"#Opera\"&gt;" + NL 
					+ 	TAB + "&lt;rdfs:subClassOf rdf:resource=\"#MusicDrama\"/&gt;" + NL 
					+ 	"&lt;/owl:Class&gt;" + NL 
					+ 	"<br>"
					+ 	"&lt;owl:Class rdf:about=\"#Operetta\"&gt;"
					+ 	TAB + "&lt;rdfs:subClassOf rdf:resource=\"#MusicDrama\"/&gt;" + NL 
					+ 	TAB +	"&lt;owl:disjointWith rdf:resource=\"#Opera\"/&gt;" + NL 
					+ 	"&lt;/owl:Class&gt;" + NL
					+ 	"<br>"
					+	"&lt;owl:Class rdf:about=\"#Musical\"&gt;" + NL 
					+ 	TAB + "&lt;rdfs:subClassOf rdf:resource=\"#MusicDrama\"/&gt;" + NL 
					+	TAB + "&lt;owl:disjointWith rdf:resource=\"#Opera\"/&gt;" + NL 
					+	TAB + "&lt;owl:disjointWith rdf:resource=\"#Operetta\"/&gt;" + NL 
					+	TAB + "&lt;/owl:Class&gt;"
					+	"<p>Here, owl:disjointWith statements are used together with owl:unionOf in order to define a set of mutually disjoint and complete subclasses of a superclass. In natural language: every MusicDrama is either an opera, an Operetta, or a Musical (the subclass partitioning is complete) and individuals belonging to one subclass, e.g., Opera, cannot belong to another subclass, e.g., Musical (disjoint or non-overlapping subclasses). This is a common modelling notion used in many data-modelling notations.</p>"
					+	"<p>NOTE: OWL Lite does not allow the use of owl:disjointWith.</p>",
					sources_DisjointWith, OWL_REF, null) 
			);
					
			name = OWLVocabularyAdapter.OWL + "distinctMembers";
			URI [] sources_distinctMembers = { new URI("http://www.w3.org/TR/owl-ref/#distinctMembers-def")  };
			add( new OWLVocabularyExplanation( new URI(name), name,			
					"<p>owl:AllDifferent. owl:AllDifferent is a special built-in OWL class, for which the property owl:distinctMembers  is defined, which links an instance of owl:AllDifferent to a list of individuals. The intended meaning of such a statement is that all individuals in the list are all different from each other.</p>"
				+	"<p>An example:</p> <br>"			
				+	"&lt;owl:AllDifferent&gt;" + NL
				+ 	TAB + "&lt;owl:distinctMembers rdf:parseType=\"Collection\"&gt;" + NL
				+ 	TAB + TAB +"&lt;Opera rdf:about=\"#Don_Giovanni\"/&gt;" + NL
				+ 	TAB + TAB +"&lt;Opera rdf:about=\"#Nozze_di_Figaro\"/&gt;" + NL
				+ 	TAB + TAB +"&lt;Opera rdf:about=\"#Cosi_fan_tutte\"/&gt;" + NL
				+ 	TAB + TAB +"&lt;Opera rdf:about=\"#Tosca\"/&gt;" + NL
				+ 	TAB + TAB +"&lt;Opera rdf:about=\"#Turandot\"/&gt;" + NL
				+ 	TAB + TAB +"&lt;Opera rdf:about=\"#Salome\"/&gt;" + NL
				+ 	TAB +"&lt;/owl:distinctMembers&gt;" + NL
				+ 	"&lt;/owl:AllDifferent&gt;" + NL
				+	"<p>This states that these six URI references all point to different operas.</p>"+ NL
				+	"<p>NOTE: owl:distinctMembers is a special syntactical construct added for convenience and should always be used with an owl:AllDifferent individual as its subject.</p>"+ NL, 
				sources_distinctMembers, OWL_REF, null) 
			);
			
			
			name = OWLVocabularyAdapter.OWL + "equivalentClass";
			URI [] sources_equiClass = { new URI("http://www.w3.org/TR/owl-ref/#equivalentClass-def")  };
			add( new OWLVocabularyExplanation( new URI(name), name,		
					"<p>A class axiom may contain (multiple) owl:equivalentClass statements. owl:equivalentClass is a built-in property that links a class description to another class description. The meaning of such a class axiom is that the two class descriptions involved have the same class extension (i.e., both class extensions contain exactly the same set of individuals).</p>"
				+	"<p>In its simplest form, an equivalentClass axiom states the equivalence (in terms of their class extension) of two named classes. An example:</p><br>"
				+	"&lt;owl:Class rdf:about=\"#US_President\"&gt;" + NL
				+	TAB +"&lt;equivalentClass rdf:resource=\"#PrincipalResidentOfWhiteHouse\"/&gt;"+ NL
				+	"&lt;/owl:Class&gt;"
				+	"<p>NOTE: The use of owl:equivalentClass does not imply class equality. Class equality means that the classes have the same intensional meaning (denote the same concept). In the example above, the concept of \"President of the US\" is related to, but not equal to the concept of the principal resident of a certain estate. Real class equality can only be expressed with the owl:sameAs construct. As this requires treating classes as individuals, class equality can only be expressed in OWL Full.</p>",
					sources_equiClass, OWL_REF, null) 
			);
			

			name = OWLVocabularyAdapter.OWL + "equivalentProperty";
			URI [] sources_equiProp = { new URI("http://www.w3.org/TR/owl-ref/#equivalentProperty-def")  };
			add( new OWLVocabularyExplanation( new URI(name), name,				
					"<p>The owl:equivalentProperty construct can be used to state that two properties have the same property extension. Syntactically, owl:equivalentProperty is a built-in OWL property with rdf:Property as both its domain and range.</p>"
				+	"<p>NOTE: Property equivalence is not the same as property equality. Equivalent properties have the same \"values\" (i.e., the same property extension), but may have different intensional meaning (i.e., denote different concepts). Property equality should be expressed with the owl:sameAs construct. As this requires that properties are treated as individuals, such axioms are only allowed in OWL Full.</p>",
					sources_equiProp, OWL_REF, null) 
			);
				
			
			name = OWLVocabularyAdapter.OWL + "hasValue";
			URI [] sources_hasVal = { new URI("http://www.w3.org/TR/owl-ref/#hasValue-def")  };
			add( new OWLVocabularyExplanation( new URI(name), name,				
					"<p>The value constraint owl:hasValue is a built-in OWL property that links a restriction class to a value V, which can be either an individual or a data value. A restriction containing a owl:hasValue constraint describes a class of all individuals for which the property concerned has at least one value semantically equal to V (it may have other values as well).</p>"
				+	"<p>NOTE: for datatypes \"semantically equal\" means that the lexical representation of the literals maps to the same value. For individuals it means that they either have the same URI reference or are defined as being the same individual (see owl:sameAs).</p>"
				+	"<p>NOTE: the value constraint owl:hasValue is not included in OWL Lite.</p>"
				+	"<p>The following example describes the class of individuals who have the individual referred to as Clinton as their parent:</p><br>"
				+	"&lt;owl:Restriction&gt;" + NL
				+	TAB + "&lt;owl:onProperty rdf:resource=\"#hasParent\" /&gt;" + NL
				+	TAB + "&lt;owl:hasValue rdf:resource=\"#Clinton\" /&gt;" + NL
				+	"&lt;/owl:Restriction&gt;" + NL,
					sources_hasVal, OWL_REF, null) 
			);
			
			name = OWLVocabularyAdapter.OWL + "imports";
			URI [] sources_imports = { new URI("http://www.w3.org/TR/owl-ref/#imports-def")  };
			add( new OWLVocabularyExplanation( new URI(name), name,
					"<p>An owl:imports statement references another OWL ontology containing definitions, whose meaning is considered to be part of the meaning of the importing ontology. Each reference consists of a URI specifying from where the ontology is to be imported. Syntactically, owl:imports is a property with the class owl:Ontology as its domain and range.</p>"
				+	"<p>The owl:imports statements are transitive, that is, if ontology A imports B, and B imports C, then A imports both B and C.</p>"
				+	"<p>Importing an ontology into itself is considered a null action, so if ontology A imports B and B imports A, then they are considered to be equivalent.</p>"
				+	"<p>Note that whether or not an OWL tool must load an imported ontology depends on the purpose of the tool. If the tool is a complete reasoner (including complete consistency checkers) then it must load all of the imported ontologies. Other tools, such as simple editors and incomplete reasoners, may choose to load only some or even none of the imported ontologies.</p>"
				+	"<p>Although owl:imports and namespace declarations may appear redundant, they actually serve different purposes. Namespace declarations simply set up a shorthand for referring to identifiers. They do not implicitly include the meaning of documents located at the URI. On the other hand, owl:imports does not provide any shorthand notation for referring to the identifiers from the imported document. Therefore, it is common to have a corresponding namespace declaration for any ontology that is imported.</p>"
				+	"<p>NOTE: owl:imports is an instance of owl:OntologyProperty.</p>",
					sources_imports, OWL_REF, null) 
			);
					
			name = OWLVocabularyAdapter.OWL + "incompatibleWith";
			URI [] sources_imcompatibleWith = { new URI("http://www.w3.org/TR/owl-ref/#incompatibleWith-def")  };
			add( new OWLVocabularyExplanation( new URI(name), name,			
					"<p>An owl:incompatibleWith statement contains a reference to another ontology. This indicates that the containing ontology is a later version of the referenced ontology, but is not backward compatible with it. Essentially, this is for use by ontology authors who want to be explicit that documents cannot upgrade to use the new version without checking whether changes are required.</p>"
					+	"<p>owl:incompatibleWith has no meaning in the model theoretic semantics other than that given by the RDF(S) model theory.</p>"
					+	"<p>owl:incompatibleWith is a built-in OWL property with the class owl:Ontology as its domain and range.</p>"
					+	"<p>NOTE: owl:backwardCompatibleWith is an instance of owl:OntologyProperty.</p>",
					sources_imcompatibleWith, OWL_REF, null) 
			);
			
			
			name = OWLVocabularyAdapter.OWL + "intersectionOf";
			URI [] sources_intersectionOf = { new URI("http://www.w3.org/TR/owl-ref/#intersectionOf-def")  };
			add( new OWLVocabularyExplanation( new URI(name), name,			
					"<p>The owl:intersectionOf property links a class to a list of class descriptions. An owl:intersectionOf statement describes a class for which the class extension contains precisely those individuals that are members of the class extension of all class descriptions in the list.</p>"
				+	"<p>An example:</p><br>"			
				+	"&lt;owl:Class&gt;" + NL
				+ 	TAB + "&lt;owl:intersectionOf rdf:parseType=\"Collection\"&gt;" + NL
				+ 	TAB + TAB + "&lt;owl:Class&gt;" + NL
				+ 	TAB + TAB + TAB +"&lt;owl:oneOf rdf:parseType=\"Collection\"&gt;" + NL
				+ 	TAB + TAB + TAB + TAB + "&lt;owl:Thing rdf:about=\"#Tosca\" /&gt;" + NL
				+ 	TAB + TAB + TAB + TAB + "&lt;owl:Thing rdf:about=\"#Salome\" /&gt;" + NL
				+ 	TAB + TAB + TAB +"&lt;/owl:oneOf&gt;" + NL
				+ 	TAB + TAB + "&lt;/owl:Class&gt;" + NL
				+ 	TAB + TAB + "&lt;owl:Class&gt;" + NL
				+ 	TAB + TAB + TAB +"&lt;owl:oneOf rdf:parseType=\"Collection\"&gt;" + NL
				+ 	TAB + TAB + TAB + TAB + "&lt;owl:Thing rdf:about=\"#Turandot\" /&gt;" + NL
				+	TAB + TAB + TAB + TAB + "&lt;owl:Thing rdf:about=\"#Tosca\" /&gt;" + NL
				+ 	TAB + TAB + TAB + "&lt;/owl:oneOf&gt;" + NL
				+ 	TAB + TAB +"&lt;/owl:Class&gt;" + NL
				+ 	TAB +"&lt;/owl:intersectionOf&gt;" + NL
				+	"&lt;/owl:Class&gt;" + NL
				+	"<p>In this example the value of owl:intersectionOf is a list of two class descriptions, namely two enumerations, both describing a class with two individuals. The resulting intersection is a class with one individual, namely Tosca. as this is the only individual that is common to both enumerations.</p>"
				+	"<p>NOTE: This assumes that the three individuals are all different. In fact, this is not by definition true in OWL. Different URI references may refer to the same individuals, because OWL does not have a \"unique names\" assumption. In Sec. 5 one can find OWL language constructs for making constraints about equality and difference of individuals.</p>"
				+	"<p>NOTE: In this example we use enumerations to make clear what the meaning is of this language construct. See the OWL Guide [OWL Guide] for more typical examples.</p>"
				+	"<p>NOTE: OWL Lite is restricted in its use of owl:intersectionOf. This is discussed later in this document, see Sec. 3.2.3</p>"
				+	"<p>owl:intersectionOf can be viewed as being analogous to logical conjunction.</p>",
				sources_intersectionOf, OWL_REF, null) 
			);
			
			name = OWLVocabularyAdapter.OWL + "inverseOf";
			URI [] sources_InverseOf = { new URI("http://www.w3.org/TR/owl-ref/#inverseOf-def")  };
			add( new OWLVocabularyExplanation( new URI(name), name,	
				"<p>Properties have a direction, from domain to range. In practice, people often find it useful to define relations in both directions: persons own cars, cars are owned by persons. The owl:inverseOf construct can be used to define such an inverse relation between properties.</p>"
			+	"<p>Syntactically, owl:inverseOf is a built-in OWL property with owl:ObjectProperty as its domain and range. An axiom of the form P1 owl:inverseOf P2 asserts that for every pair (x,y) in the property extension of P1, there is a pair (y,x) in the property extension of P2, and vice versa. Thus, owl:inverseOf is a symmetric property.</p>"
			+	"<p>An example:</p>"
			+	"&lt;owl:ObjectProperty rdf:ID=\"hasChild\"&gt;" + NL
			+ 	TAB + "&lt;owl:inverseOf rdf:resource=\"#hasParent\"/&gt;" + NL
			+ 	"&lt;/owl:ObjectProperty&gt;",
				sources_InverseOf, OWL_REF, null) 
			);

			name = OWLVocabularyAdapter.OWL + "maxCardinality";
			URI [] sources_maxCardinality = { new URI("http://www.w3.org/TR/owl-ref/#maxCardinality-def")  };
			add( new OWLVocabularyExplanation( new URI(name), name,
				"<p>The cardinality constraint owl:maxCardinality is a built-in OWL property that links a restriction class to a data value belonging to the value space of the XML Schema datatype nonNegativeInteger. A restriction containing an owl:maxCardinality constraint describes a class of all individuals that have at most N semantically distinct values (individuals or data values) for the property concerned, where N is the value of the cardinality constraint. Syntactically, the cardinality constraint is represented as an RDF property element with the corresponding rdf:datatype attribute.</p>"
				+	"<p>The following example describes a class of individuals that have at most two parents:</p> <br>"
				+	"&lt;owl:Restriction&gt;" + NL
				+	TAB+"&lt;owl:onProperty rdf:resource=\"#hasParent\" /&gt;" + NL
				+ 	TAB+  "&lt;owl:maxCardinality rdf:datatype=\"&amp;xsd;nonNegativeInteger\"&gt;2&lt;/owl:maxCardinality&gt;" + NL
				+	"&lt;/owl:Restriction&gt;",
				sources_maxCardinality, OWL_REF, null) 
			);

			name = OWLVocabularyAdapter.OWL + "minCardinality";
			URI [] sources_minCardinality = { new URI("http://www.w3.org/TR/owl-ref/#minCardinality-def")  };
			add( new OWLVocabularyExplanation( new URI(name), name,
					"<p>The cardinality constraint owl:minCardinality is a built-in OWL property that links a restriction class to a data value belonging to the value space of the XML Schema datatype nonNegativeInteger. A restriction containing an owl:minCardinality constraint describes a class of all individuals that have at least N semantically distinct values (individuals or data values) for the property concerned, where N is the value of the cardinality constraint. Syntactically, the cardinality constraint is represented as an RDF property element with the corresponding rdf:datatype attribute.</p>"
				+	"<p>The following example describes a class of individuals that have at least two parents:</p> <br>"
				+	"&lt;owl:Restriction&gt;" + NL
	 			+ 	TAB+"&lt;owl:onProperty rdf:resource=\"#hasParent\" /&gt;"+ NL
				+ 	TAB+  "&lt;owl:minCardinality rdf:datatype=\"&amp;xsd;nonNegativeInteger\"&gt;2&lt;/owl:minCardinality&gt;" + NL
				+	"&lt;/owl:Restriction&gt;" + NL	
				+	"<p>Note that an owl:minCardinality of one or more means that all instances of the class must have a value for the property.</p>", 
				sources_minCardinality, OWL_REF, null) 
			);

			name = OWLVocabularyAdapter.OWL + "oneOf";
			URI [] sources_oneOf = { new URI("http://www.w3.org/TR/owl-ref/#oneOf-def")  };
			add( new OWLVocabularyExplanation( new URI(name), name,
					"<p>A  class description of the \"enumeration\" kind is defined with the owl:oneOf property. The value of this built-in OWL property must be a list of individuals that are the instances of the class. This enables a class to be described by exhaustively enumerating its instances. The class extension of a class described with owl:oneOf contains exactly the enumerated individuals, no more, no less. The list of individuals is typically represented with the help of the RDF construct rdf:parseType=\"Collection\", which provides a convenient shorthand for writing down a set of list elements. For example, the following RDF/XML syntax defines a class of all continents:</p><br>"
				+ 	"&lt;owl:Class&gt;" + NL
				+	TAB + "&lt;owl:oneOf rdf:parseType=\"Collection\"&gt;" + NL
			    +	TAB + TAB +"&lt;owl:Thing rdf:about=\"#Eurasia\"/&gt;" + NL
			    +	TAB + TAB +"&lt;owl:Thing rdf:about=\"#Africa\"/&gt;" + NL
			    +	TAB + TAB +"&lt;owl:Thing rdf:about=\"#NorthAmerica\"/&gt;" + NL
			    +	TAB + TAB +"&lt;owl:Thing rdf:about=\"#SouthAmerica\"/&gt;" + NL
			    +	TAB + TAB +"&lt;owl:Thing rdf:about=\"#Australia\"/&gt;" + NL
			    +	TAB + TAB +"&lt;owl:Thing rdf:about=\"#Antarctica\"/&gt;" + NL
				+	TAB +  "&lt;/owl:oneOf&gt;" + NL
				+	"&lt;/owl:Class&gt;"
				+	"<p>The RDF/XML syntax &lt;owl:Thing rdf:about=\"...\"/&gt; refers to some individual (remember: all individuals are by definition instances of owl:Thing).</p>"
				+	"<p>In the section on datatypes we will see another use of the owl:oneOf construct, namely to define an enumeration of data values.</p>"
				+	"<p>NOTE: Enumeration is not part of OWL Lite</p>",
				sources_oneOf, OWL_REF, null) 
			);
			
			name = OWLVocabularyAdapter.OWL + "onProperty";
			URI [] sources_onProp = {  new URI("http://www.w3.org/TR/2004/REC-owl-guide-20040210/#owl_onProperty"), new URI("http://www.w3.org/TR/owl-ref/#onProperty-def")  };
			add( new OWLVocabularyExplanation( new URI(name), name,
					"<p>In addition to designating property characteristics, it is possible to further constrain the range of a property in specific contexts in a variety of ways. We do this with property restrictions. The various forms described below can only be used within the context of an owl:Restriction. The owl:onProperty element indicates the restricted property.</p>"
				+	"<p>A restriction class should have exactly one triple linking the restriction to a particular property, using the  owl:onProperty property.</p>",
				sources_onProp, OWL_REF, null) 
			);

			name = OWLVocabularyAdapter.OWL + "priorVersion";
			URI [] sources_priorVer = {  new URI("http://www.w3.org/TR/owl-ref/#priorVersion-def")  };
			add( new OWLVocabularyExplanation( new URI(name), name,
					"<p>An owl:priorVersion statement contains a reference to another ontology. This identifies the specified ontology as a prior version of the containing ontology. This has no meaning in the model-theoretic semantics other than that given by the RDF(S) model theory. However, it may be used by software to organize ontologies by versions.</p>"
				+	"<p>owl:priorVersion is a built-in OWL property with the class owl:Ontology as its domain and range.</p>"
				+	"<p>NOTE: owl:priorVersion is an instance of owl:OntologyProperty.</p>",
					sources_priorVer, OWL_REF, null) 
			);			

			name = OWLVocabularyAdapter.OWL + "sameAs";
			URI [] sources_sameAs = {  new URI("http://www.w3.org/TR/owl-ref/#sameAs-def")  };
			add( new OWLVocabularyExplanation( new URI(name), name,
					"<p>The built-in OWL property owl:sameAs links an individual to an individual. Such an owl:sameAs statement indicates that two URI references actually refer to the same thing: the individuals have the same \"identity\".</p>"
				+	"<p>For individuals such as \"people\" this notion is relatively easy to understand. For example, we could state that the following two URI references actually refer to the same person:</p><br>"
				+	"&lt;rdf:Description rdf:about=\"#William_Jefferson_Clinton\"&gt;" + NL
				+	TAB + "&lt;owl:sameAs rdf:resource=\"#BillClinton\"/&gt;" + NL
				+	"&lt;/rdf:Description&gt;"
				+	"<p>The owl:sameAs statements are often used in defining mappings between ontologies. It is unrealistic to assume everybody will use the same name to refer to individuals. That would require some grand design, which is contrary to the spirit of the web.</p>"
				+	"<p>In OWL Full, where a class can be treated as instances of (meta)classes, we can use the owl:sameAs construct to define class equality, thus indicating that two concepts have the same intensional meaning. An example:</p> <br>"
				+	"&lt;owl:Class rdf:ID=\"FootballTeam\"&gt;" + NL
				+	TAB+"&lt;owl:sameAs rdf:resource=\"http://sports.org/US#SoccerTeam\"/&gt;" + NL
				+	"&lt;/owl:Class&gt;"
				+	"<p>One could imagine this axiom to be part of a European sports ontology. The two classes are treated here as individuals, in this case as instances of the class owl:Class. This allows us to state that the class FootballTeam in some European sports ontology denotes the same concept as the class SoccerTeam in some American sports ontology. Note the difference with the statement:</p><br>"
				+ 	"&lt;footballTeam owl:equivalentClass us:soccerTeam /&gt;"
				+	"<p>which states that the two classes have the same class extension, but are not (necessarily) the same concepts.</p>"
				+	"<p>NOTE: For details of comparison of URI references, see the section on RDF URI references in the RDF Concepts document [RDF Concepts].</p>",
					sources_sameAs, OWL_REF, null) 
			);			

			name = OWLVocabularyAdapter.OWL + "someValuesFrom";
			URI [] sources_SVF = {  new URI("http://www.w3.org/TR/owl-ref/#someValuesFrom-def")  };
			add( new OWLVocabularyExplanation( new URI(name), name,
					"<p>The value constraint owl:someValuesFrom is a built-in OWL property that links a restriction class to a class description or a data range. A restriction containing an owl:someValuesFrom constraint describes a class of all individuals for which at least one value of the property concerned is an instance of the class description or a data value in the data range. In other words, it defines a class of individuals x for which there is at least one y (either an instance of the class description or value of the data range) such that the pair (x,y) is an instance of P. This does not exclude that there are other instances (x,y') of P for which y' does not belong to the class description or data range.</p>"
				+	"<p>The following example defines a class of individuals which have at least one parent who is a physician:</p><br>"
				+	"&lt;owl:Restriction&gt;"  + NL
  				+	TAB + "&lt;owl:onProperty rdf:resource=\"#hasParent\" /&gt;" + NL
				+	TAB + "&lt;owl:someValuesFrom rdf:resource=\"#Physician\" /&gt;" + NL
				+	"&lt;/owl:Restriction&gt;"
				+	"<p>The owl:someValuesFrom constraint is analogous to the existential quantifier of Predicate logic - for each instance of the class that is being defined, there exists at least one value for P that fulfills the constraint.</p>"
				+	"<p>NOTE: In OWL Lite the only type of class description allowed as object of owl:someValuesFrom is a class name.</p>",
					sources_SVF, OWL_REF, null) 
			);

			name = OWLVocabularyAdapter.OWL + "unionOf";
			URI [] sources_unionOf = {  new URI("http://www.w3.org/TR/owl-ref/#unionOf-def")  };
			add( new OWLVocabularyExplanation( new URI(name), name,
					"<p>The owl:unionOf property links a class to a list of class descriptions. An owl:unionOf statement describes an anonymous class for which the class extension contains those individuals that occur in at least one of the class extensions of the class descriptions in the list.</p>"
				+	"<p>An example:</p><br>"
				+	"&lt;owl:Class&gt;" + NL
				+	TAB +  "&lt;owl:unionOf rdf:parseType=\"Collection\"&gt;" + NL
				+	TAB +TAB +  "&lt;owl:Class&gt;" + NL
				+	TAB +TAB +TAB +    "&lt;owl:oneOf rdf:parseType=\"Collection\"&gt;" + NL
				+	TAB +TAB +TAB +TAB +     "&lt;owl:Thing rdf:about=\"#Tosca\" /&gt;" + NL
				+	TAB +TAB +TAB +TAB +     "&lt;owl:Thing rdf:about=\"#Salome\" /&gt;" + NL
				+	TAB +TAB +TAB +   "&lt;/owl:oneOf&gt;" + NL
				+	TAB +TAB +  "&lt;/owl:Class&gt;" + NL
				+	TAB +TAB +  "&lt;owl:Class&gt;" + NL
				+	TAB +TAB +TAB +    "&lt;owl:oneOf rdf:parseType=\"Collection\"&gt;" + NL
				+	TAB +TAB +TAB +TAB +     "&lt;owl:Thing rdf:about=\"#Turandot\" /&gt;" + NL
				+	TAB +TAB +TAB +TAB +     "&lt;owl:Thing rdf:about=\"#Tosca\" /&gt;" + NL
				+	TAB +TAB +TAB +    "&lt;/owl:oneOf&gt;" + NL
				+	TAB +TAB +  "&lt;/owl:Class&gt;" + NL
				+ 	TAB + "&lt;/owl:unionOf&gt;" + NL
				+	"&lt;/owl:Class&gt;"
				+	"<p>This class description describes a class for which the class extension contains three individuals, namely Tosca, Salome, and Turandot (assuming they are all different).</p>"
				+	"<p>NOTE: owl:unionOf is not part of OWL Lite.</p>"
				+	"<p>owl:unionOf is analogous to logical disjunction.</p>",
					sources_unionOf, OWL_REF, null) 
			);
			
			name = OWLVocabularyAdapter.OWL + "versionInfo";
			URI [] sources_vInfo = {  new URI("http://www.w3.org/TR/owl-ref/#versionInfo-def")  };
			add( new OWLVocabularyExplanation( new URI(name), name,
					"<p>An owl:versionInfo statement generally has as its object a string giving information about this version, for example RCS/CVS keywords. This statement does not contribute to the logical meaning of the ontology other than that given by the RDF(S) model theory.</p>"
				+	"<p>Although this property is typically used to make statements about ontologies, it may be applied to any OWL construct. For example, one could attach a owl:versionInfo statement to an OWL class.</p>"
				+	"<p>NOTE: owl:versionInfo is an instance of owl:AnnotationProperty.</p>",
				sources_vInfo, OWL_REF, null) 
			);

	}
	
	public void add(OWLVocabularyExplanation exp)
	{
		super.put( exp.getURI(), exp );
		myNumExplanations++;
	}

	public VocabularyExplanation explain(URI uri) {

		return (VocabularyExplanation)super.get(uri);
	}

	public int numExplanations() {
		
		return myNumExplanations;
	}
}
