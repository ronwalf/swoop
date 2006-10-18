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

import java.awt.Component;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.renderer.entity.ConciseFormatEntityRenderer;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLException;

/**
 * @author Aditya
 */
public class ClassExprRenderer extends JLabel implements ListCellRenderer {

	private SwoopModel swoopModel;
	
	public ClassExprRenderer(SwoopModel swoopModel) {
		this.swoopModel = swoopModel;
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
	
		// create concise format to render class expr
		ConciseFormatEntityRenderer cfRend = new ConciseFormatEntityRenderer();
    	cfRend.setSwoopModel(swoopModel);
    	cfRend.visitor = cfRend.createVisitor();
    	StringWriter st = new StringWriter();
		PrintWriter buffer = new PrintWriter(st);
    	cfRend.setWriter(buffer);	
    	
		if (value instanceof OWLDescription) {
			try {
				cfRend.printObject((OWLDescription) value);
			} catch (OWLException e) {
				e.printStackTrace();
			}
			setText("<html>"+st.toString()+"</html>");
		}
		else {
			setText(value.toString());
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
