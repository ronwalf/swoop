package org.mindswap.swoop.renderer;

import java.awt.Component;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.renderer.entity.ConciseFormatEntityRenderer;
import org.mindswap.swoop.utils.ui.SwoopIcons;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataType;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLSubClassAxiom;

/**
 * @author Evren Sirin
 */
public class SwoopCellRenderer extends JLabel implements ListCellRenderer {
	private SwoopModel swoopModel;
	private Set rootURIs;
	
	public SwoopCellRenderer(SwoopModel swoopModel) {
		this.swoopModel = swoopModel;
		this.rootURIs = new HashSet();
	}

	public SwoopCellRenderer(SwoopModel swoopModel, Set roots) {
		this.swoopModel = swoopModel;
		this.rootURIs = new HashSet();
		for (Iterator iter = roots.iterator(); iter.hasNext();) {
			OWLClass root = (OWLClass) iter.next();
			try {
				this.rootURIs.add(root.getURI());
			} catch (OWLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public Component getListCellRendererComponent(
		JList list,
		Object value,
		int index,
		boolean isSelected,
		boolean cellHasFocus) {

		if (value==null) {
			return this;
		}
		
		if (!(value instanceof OWLEntity) && !(value instanceof OWLDataType) && !(value instanceof OWLSubClassAxiom)) {
			setText(value.toString());
			//return this;
		}
		else if (value instanceof OWLSubClassAxiom) {
			// create concise format to render class expr
			ConciseFormatEntityRenderer cfRend = new ConciseFormatEntityRenderer();
	    	cfRend.setSwoopModel(swoopModel);
	    	cfRend.visitor = cfRend.createVisitor();
	    	StringWriter st = new StringWriter();
			PrintWriter buffer = new PrintWriter(st);
	    	cfRend.setWriter(buffer);	
	    	
			try {
				cfRend.printObject((OWLSubClassAxiom) value);
			} catch (OWLException e) {
				e.printStackTrace();
			}
			setText("<html>"+st.toString()+"</html>");
			setIcon(null);
		}
		else if (value instanceof OWLDataType) {
			OWLDataType dt = (OWLDataType) value;
			try {
				setText(swoopModel.shortForm(dt.getURI()));
			} catch (OWLException e) {
				e.printStackTrace();
			}
		}
		else {
			OWLEntity entity = (OWLEntity) value;
	
			SwoopIcons swoopIcons = new SwoopIcons();
			if (swoopIcons.getIcon(entity, swoopModel)!=null) setIcon(swoopIcons.getIcon(entity, swoopModel));
	
			try {
				String text = "";
				// handle anon individuals by displaying gen-ids
				if (entity instanceof OWLIndividual) {
					OWLIndividual ind = (OWLIndividual) entity;
					if (ind.isAnonymous()) text = swoopModel.shortForm(ind.getAnonId()) +" (Anonymous)"; 
				}
				if (text.equals("")) text = swoopModel.shortForm(entity.getURI());
				if (swoopModel.getChangesCache().getChangeList(entity.getURI()).size()>0) text += "*";
				
				try {
					// mark entities that have annotations with superscript A
					if (swoopModel.getAnnotatedObjectURIs().contains(entity.getURI())) {
						text = "<html>"+text+"<sup>A</sup></html>";
					}
					// mark root classes (for RepairFrame)
					if (rootURIs.contains(entity.getURI())) text = "<html>"+text+"<sup>Root</sup></html>";
				} 
				catch (Exception e1) {						
					e1.printStackTrace();
				}
				
				setText(text);
			} catch (OWLException e) {
				setText("ERROR");
			}
		}
		
		if (isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		} else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}
		setEnabled(list.isEnabled());
		setFont(list.getFont());
		setOpaque(true);
		return this;
	}
}
