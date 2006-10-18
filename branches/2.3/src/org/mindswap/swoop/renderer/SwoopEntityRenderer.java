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

package org.mindswap.swoop.renderer;

import java.io.Writer;

import org.mindswap.swoop.SwoopModel;
import org.semanticweb.owl.io.RendererException;
import org.semanticweb.owl.model.OWLEntity;

public interface SwoopEntityRenderer extends SwoopRenderer {
	
	public static final String OWL_CLASS            = "http://www.w3.org/2002/07/owl#Class";
	public static final String OWL_INTERSECTIONOF   = "http://www.w3.org/2002/07/owl#intersectionOf";
	public static final String OWL_UNIONOF          = "http://www.w3.org/2002/07/owl#unionOf";
	public static final String OWL_COMPLEMENTOF     = "http://www.w3.org/2002/07/owl#complementOf";
	public static final String OWL_EQUIVALENTCLASS  = "http://www.w3.org/2002/07/owl#equivalentClass";	    
	public static final String OWL_ONPROPERTY       = "http://www.w3.org/2002/07/owl#onProperty";
    public static final String OWL_HASVALUE         = "http://www.w3.org/2002/07/owl#hasValue";
	public static final String OWL_RESTRICTION      = "http://www.w3.org/2002/07/owl#Restriction";
	public static final String OWL_SOMEVALUESFROM   = "http://www.w3.org/2002/07/owl#someValuesFrom";
	public static final String OWL_ALLVALUESFROM    = "http://www.w3.org/2002/07/owl#allValuesFrom";
	public static final String OWL_MINCARDINALITY   = "http://www.w3.org/2002/07/owl#minCardinality";
	public static final String OWL_MAXCARDINALITY   = "http://www.w3.org/2002/07/owl#maxCardinality";
	public static final String OWL_CARDINALITY      = "http://www.w3.org/2002/07/owl#cardinality";	
	public static final String OWL_ONEOF            = "http://www.w3.org/2002/07/owl#oneOf";	
	public static final String OWL_DISJOINTWITH     = "http://www.w3.org/2002/07/owl#disjointWith";
	public static final String OWL_SAMEAS           = "http://www.w3.org/2002/07/owl#sameAs";
	public static final String OWL_DIFFERENTFROM    = "http://www.w3.org/2002/07/owl#differentFrom";
	public static final String OWL_ALLDIFFERENT     = "http://www.w3.org/2002/07/owl#AllDifferent";
	public static final String OWL_DISTINCTMEMBERS  = "http://www.w3.org/2002/07/owl#distinctMembers";	
	
	public static final String OWL_OBJECTPROPERTY        ="http://www.w3.org/2002/07/owl#ObjectProperty";
	public static final String OWL_DATAPROPERTY          ="http://www.w3.org/2002/07/owl#DatatypeProperty";
	public static final String OWL_FUNCTIONALPROP        = "http://www.w3.org/2002/07/owl#FunctionalProperty";
	public static final String OWL_INVERSEFUNCTIONALPROP = "http://www.w3.org/2002/07/owl#InverseFunctionalProperty";
	public static final String OWL_TRANSITIVEPROP        = "http://www.w3.org/2002/07/owl#TransitiveProperty";
	public static final String OWL_SYMMETRICPROP         = "http://www.w3.org/2002/07/owl#SymmetricProperty";
	public static final String OWL_INVERSEOF             = "http://www.w3.org/2002/07/owl#InverseOf";
	public static final String OWL_EQUIVALENTPROP        = "http://www.w3.org/2002/07/owl#equivalentProperty";
	
	public static final String OWL_THING                 = "http://www.w3.org/2002/07/owl#Thing";
	
	public static final String RDFS_SUBCLASSOF      = "http://www.w3.org/2000/01/rdf-schema#subClassOf";
	public static final String RDFS_DOMAIN          = "http://www.w3.org/2000/01/rdf-schema#domain";
	public static final String RDFS_RANGE           = "http://www.w3.org/2000/01/rdf-schema#range";
	public static final String RDFS_SUBPROPERTYOF   = "http://www.w3.org/2000/01/rdf-schema#subPropertyOf";	
	
	public static final String RDF_TYPE_URI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
	
    public void render(OWLEntity entity, SwoopModel swoopModel, Writer writer) throws RendererException;
}
