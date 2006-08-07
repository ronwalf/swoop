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

package org.mindswap.swoop.renderer.ontology;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.utils.owlapi.DefaultShortFormProvider;
import org.semanticweb.owl.io.ShortFormProvider;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLOntology;
/**
 * @author Evren Sirin
 *  
 */
public class OntologyListRenderer extends JLabel implements ListCellRenderer {
	ShortFormProvider shortFormProvider = new DefaultShortFormProvider();
	
	private SwoopModel swoopModel;

	public OntologyListRenderer(SwoopModel swoopModel) {
		this.swoopModel = swoopModel;
	}

	public Component getListCellRendererComponent(
			JList list, 
			Object value, // value to display
			int index, // cell index
			boolean isSelected, // is the cell selected
			boolean cellHasFocus) // the list and the cell have the focus
	{
		
		if (value instanceof OWLOntology) { 
			OWLOntology ont = (OWLOntology) value;
			try {
				String label = shortFormProvider.shortForm(ont.getURI());
				if(swoopModel.getChangesCache().getChangeList(ont.getURI()).size()>0)
					label += "*";
				
				if (swoopModel.getAnnotatedObjectURIs().contains(ont.getURI())) 
					label = "<html>"+label+"<sup>A</sup></html>";
				
				setText(label);			
			} catch (OWLException e) {
				e.printStackTrace();
			}
	
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
		}
		else {
			if (value!=null) setText(value.toString()); 
		}
			
		setEnabled(list.isEnabled());
		setFont(list.getFont());
		setOpaque(true);
		return this;
	}

}
