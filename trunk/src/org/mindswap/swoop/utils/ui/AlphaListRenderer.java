/*
 * Created on Oct 18, 2004
 */
package org.mindswap.swoop.utils.ui;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.TermsDisplay;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDescription;

/**
 * @author Evren Sirin
 */
public class AlphaListRenderer extends JLabel implements ListCellRenderer {
    
	
	public AlphaListRenderer() {
	    setOpaque(true);
        setVerticalAlignment(CENTER);
    }

    /*
     * This method finds the image and text corresponding
     * to the selected value and returns the label, set up
     * to display the text and image.
     */
    public Component getListCellRendererComponent(
                                       JList list,
                                       Object value,
                                       int index,
                                       boolean isSelected,
                                       boolean cellHasFocus) {
        //Get the selected index. (The index param isn't
        //always valid, so just use the value.)
	    String selectedValue = value.toString();

        //Set the icon and text.  If icon was null, say so.
        Icon icon = null;
        if(selectedValue.equals(TermsDisplay.filterNames[SwoopModel.CLASSES]))
            icon = SwoopIcons.classIcon;
        else if(selectedValue.equals(TermsDisplay.filterNames[SwoopModel.PROPERTIES]))
            icon = SwoopIcons.propIcon;
        else if(selectedValue.equals(TermsDisplay.filterNames[SwoopModel.INDIVIDUALS]))
            icon = SwoopIcons.individualIcon;
        
        setText(selectedValue);
        setFont(list.getFont());
        setIcon(icon);

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } 
        else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        return this;
    }	
}
