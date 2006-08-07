/*
 * Created on Apr 7, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.utils.rdfapi;

import org.mindswap.swoop.utils.rdfapi.SwoopResource;

/**
 * @author ronwalf
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class NodeFactoryImpl extends edu.unika.aifb.rdf.mainmemory.NodeFactoryImpl
		implements SwoopNodeFactory {

	public edu.unika.aifb.rdf.api.model.Resource createResource() {
		return new SwoopResourceImpl();
	}

	/* (non-Javadoc)
	 * @see org.mindswap.swoop.utils.rdfapi.NodeFactory#createString(java.lang.String)
	 */
	public edu.unika.aifb.rdf.api.model.Resource createResource(String uri) {
		if (uri==null)
			return createResource();
		
		SwoopResource result=(SwoopResource)m_resources.get(uri);
        	if (result==null) {
        		result=new SwoopResourceImpl(uri);
        		//System.out.println("New Resource: "+result+" from "+uri);
        		m_resources.put(uri,result);
        	}
        	return result;
	}

}
