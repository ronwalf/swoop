package org.mindswap.swoop.popup;

import java.net.URI;

import javax.swing.JList;
import javax.swing.ListModel;

import org.mindswap.swoop.SwoopModel;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLException;

/**
 * @author Aditya Kalyanpur
 *
 */
public class PopupCommon {

	/**
	 * Select entity in JList depending on alphabet pressed
	 * 
	 * @param swoopModel - used to get shortForm of entity to match against alpha
	 * @param list - JList containing set of entities
	 * @param alpha - alphabet pressed
	 */
	public static void listSelector(SwoopModel swoopModel, JList list, String alpha) {
		
		// start search from current selected entity, if any
		int startPos = list.getSelectedIndex()+1;
		int endPos = list.getModel().getSize();
		boolean match = findMatch(swoopModel, list, alpha, startPos, endPos);
		
		// if match still not found, and some entity has been selected,
		// roll over from end, and conduct search from start to selected entity
		if (!match && list.getSelectedIndex()!=-1) {
			startPos = 0;
			endPos = list.getSelectedIndex();
			findMatch(swoopModel, list, alpha, startPos, endPos);
		}
	}
	
	/*
	 * Conduct a search in entity list (JList) from startPos to endPos
	 * for any entity whose shortForm (name) matches the alphabet pressed 
	 * and select it (return true if match found)
	 */
	private static boolean findMatch(
			SwoopModel swoopModel, 
			JList list, 
			String alpha,
			int startPos,
			int endPos) 
	{
		
		ListModel model = list.getModel();
		boolean match = false;		
		for (int i=startPos; i<endPos; i++) {			
			OWLEntity entity = (OWLEntity) model.getElementAt(i);
			URI entityURI = null;
			try {
				entityURI = entity.getURI();
			} catch (OWLException e) {
				e.printStackTrace();
			}
			String sf = swoopModel.shortForm(entityURI);
			if (sf.indexOf(":")>=0) {
				sf = sf.substring(sf.indexOf(":")+1, sf.length());					
			}
			if (sf.toLowerCase().startsWith(alpha)) {
				match = true;
				list.setSelectedValue(entity, true);
				break;
			}
		}
		return match;
	}

}
