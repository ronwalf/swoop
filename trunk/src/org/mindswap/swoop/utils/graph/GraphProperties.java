/*
 * Created on Apr 21, 2005
 */
package org.mindswap.swoop.utils.graph;

import java.util.Collection;

/**
 * @author Evren Sirin
 *
 */
public interface GraphProperties {
    public String getShortName(Object obj);
    
    public String getLongName(Object obj);
    
    public Collection getLinkedElements(Object obj);
    
    public int getSize(Object obj);
    
    public String getPreferredLayout();
}
