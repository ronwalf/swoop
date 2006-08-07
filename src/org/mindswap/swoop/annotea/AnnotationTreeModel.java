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

package org.mindswap.swoop.annotea;

import java.net.URI;

import javax.swing.tree.DefaultMutableTreeNode;

import org.mindswap.swoop.treetable.*;
import org.semanticweb.owl.model.OWLException;

/**
 * @author Aditya
 *
 */
public class AnnotationTreeModel extends AbstractTreeTableModel {

	public AnnotationTreeModel(Object root) {
		super(root);
	}

	// Names of the columns.
    static protected String[]  cNames = {"Author", "Subject", "Date", "Entity"};

    // Types of the columns.
    static protected Class[]  cTypes = { String.class,
					 String.class, String.class,
					 String.class};
	
	
	public int getColumnCount() {
		return 4;
	}

	public String getColumnName(int column) {
		return cNames[column];
	}

	public Object getValueAt(Object node, int column) {
		
		DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
		Description annot = (Description) treeNode.getUserObject();
		
		try {
		    switch(column) {
		    
		    case 0:		    	
		    	if (annot.getAuthor()!=null) return annot.getAuthor();
		    	else return "Anonymous";
		    
		    case 1: // type + subject
		    	
		    	String type = "";
				if (annot.getAnnotationType()!=null) {
					try {
						type = Annotea.getEntityName(annot.getAnnotationType().getURI());
						type = type.substring(0, 2);
						type = "[" + type.toUpperCase() + "] ";
					} 
					catch (OWLException e) {
						e.printStackTrace();
					}
				}
		    	// get subject - inside <head> of html block
		    	if (annot.getBody()!=null) {
		    		String subject = annot.getBody().trim();
			    	if (subject.indexOf("<head>")>=0) {
			    		subject = subject.substring(subject.indexOf("<head>")+6, subject.indexOf("</head>")).trim();
			    	}
			    	subject = type + subject;
			    	return subject;
		    	}
		    	else return type;
		    	
		    case 2:
		    	if (annot.getCreated()!=null) {
		    		String date = annot.getCreated();
		    		if (date.indexOf("-")>=0) {
		    			int spos = date.indexOf("-")+1;
		    			int epos = date.indexOf(" ", spos+1);
		    			if (spos!=-1 && epos!=-1) date = date.substring(spos, epos); 
		    		}
		    		return date; 
		    	}
		    	else return "";
			
		    	
		    case 3: // annotated entity
		    	if (annot.annotates!=null) {
			    	String annotURI = annot.annotates[0].toString();
			    	if (annotURI.indexOf("#")>=0) annotURI = annotURI.substring(annotURI.indexOf("#")+1, annotURI.length());
			    	else annotURI = annotURI.substring(annotURI.lastIndexOf("/")+1, annotURI.length());
			    	if (annotURI!=null) return annotURI;
		    	}
		    	return "";
		    }
		}
		catch  (SecurityException se) { }
	   
		return null;
	}

	public int getChildCount(Object node) {
		DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
		return treeNode.getChildCount();
	}

	public Object getChild(Object node, int index) {
		DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
		return treeNode.getChildAt(index);
	}
	
	public Class getColumnClass(int column) {
		return cTypes[column];
	}	
}
