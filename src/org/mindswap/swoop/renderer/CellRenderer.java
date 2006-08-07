package org.mindswap.swoop.renderer;

import java.awt.Component;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.ListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import org.mindswap.swoop.utils.owlapi.DefaultShortFormProvider;
import org.semanticweb.owl.io.ShortFormProvider;
/**
 * @author Aditya
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class CellRenderer extends JLabel implements ListCellRenderer {
	     
		ShortFormProvider shortFormProvider = new DefaultShortFormProvider();	
	
	     public Component getListCellRendererComponent(
	       JList list,
	       Object value,            // value to display
	       int index,               // cell index
	       boolean isSelected,      // is the cell selected
	       boolean cellHasFocus)    // the list and the cell have the focus
	     {
	     	if (value==null) return this;
	     	
	     	String s = value.toString();
	     	try {
	     		if (s.indexOf("New term")>=0) {
	     			setText(getDisplayLabel(s));
	     		}
	     		else {
	     			URI uri = new URI(s);
	     			setText(shortFormProvider.shortForm(uri));
	     		}
	     	}
	     	catch (URISyntaxException ex) {
	     		ex.printStackTrace();
	     	}
	     	
	        if (isSelected) {
	           setBackground(list.getSelectionBackground());
		       setForeground(list.getSelectionForeground());
	   	   	}
	   	   	else {
	   	   		setBackground(list.getBackground());
	   	   		setForeground(list.getForeground());
	   	   	}
	   	   	setEnabled(list.isEnabled());
	   	   	setFont(list.getFont());
	   	   	setOpaque(true);
	   	   	return this;
	    }
	 
	    public String getDisplayLabel(String uri) {
			String label = "";
			if (uri.indexOf("#")>=0) {
				label = uri.substring(uri.indexOf("#")+1, uri.length());
			}
			else {
				label = uri.substring(uri.lastIndexOf("/")+1, uri.length());
			}
			return label;
		}
}
