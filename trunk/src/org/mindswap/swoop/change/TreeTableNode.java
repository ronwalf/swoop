package org.mindswap.swoop.change;
import java.net.URL;
import java.util.Iterator;
import java.util.Vector;


public class TreeTableNode { 
    public SwoopChange swoopChange;
    public Vector 	 children; 
    public URL location;
    
    public TreeTableNode(SwoopChange swc) { 
		this.swoopChange = swc; 
		children = new Vector();
    }

    public String toString() { 
		return swoopChange.toString();
    }

    public Object getValueAt(int i) {
    	switch (i) {
    	
    		case 0: // return author
    			if (swoopChange.getAuthor()!=null) return swoopChange.getAuthor();
    			else return "";
    			
    		case 1: // return description
    			if (swoopChange.getDescription()!=null) {
    				String desc = swoopChange.getDescription();
    				if (desc.indexOf("<u>")>=0) {
    					desc = desc.substring(desc.indexOf("<u>")+3, desc.indexOf("</u>"));
    				}
    				if (!swoopChange.isCommitted) desc = "*" + desc;
    				return desc;
    			}
    			else return "";
    			
			case 2: // return timestamp
				String ts = "";
				if (swoopChange.getTimeStamp()!=null) {
					ts = swoopChange.getTimeStamp();
					if (ts.indexOf(" ")>=0) ts = ts.substring(ts.indexOf(" "), ts.length()) + " " + ts.substring(0, ts.indexOf(" "));
				}
				return ts;
				
			case 3: // return uris
				String uris = "";
				if (swoopChange.getExtraSubjects()!=null && swoopChange.getExtraSubjects().size()>0) {
					for (Iterator iter = swoopChange.getExtraSubjects().iterator(); iter.hasNext();) {
						String uri = iter.next().toString();
						uris += getName(uri) + ",";
					}
					uris = uris.substring(0, uris.length()-1);
				}
				else if (swoopChange.getOwlObjectURI()!=null) {
					uris = getName(swoopChange.getOwlObjectURI().toString());
				}
				return uris;
    	}
		return null; 
    }

    public void setValueAt(Object aValue, int i) {		 
    }
    
	public int getColumnCount() {
		return 4;
	}
	
	public int getChildCount() {
		return children.size();
	}
	
	public TreeTableNode getChildAt(int i) {
		return (TreeTableNode) children.get(i);
    }    
    
    public void addChild(TreeTableNode child) {
    	if(child.getColumnCount() != getColumnCount())
    		throw new RuntimeException("Column count of a child should be same as parent");
    		
		children.add(child);    		
    }
    
    private String getName(String uri) {
    	if (uri.indexOf("#")>=0) return uri.substring(uri.indexOf("#")+1, uri.length());
    	else return uri.substring(uri.lastIndexOf("/")+1, uri.length());
    }
}

