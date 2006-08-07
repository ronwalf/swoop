package org.mindswap.swoop.treetable;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import org.mindswap.swoop.change.DefaultTreeTableModel;
import org.mindswap.swoop.change.SwoopChange;
import org.mindswap.swoop.change.TreeTableNode;

public class TreeTableTest {

    static public void main(String []args) {
		final String[] cNames  = {"Author", "Description", "Date", "Entity"};   	
    	
    	SwoopChange swc1 = new SwoopChange("Root", null, null, "2:34", "Work?", true, false);
    	SwoopChange swc2 = new SwoopChange("Child", null, null, "2:34", "Work?", true, false);
    	
		TreeTableNode root = new TreeTableNode(swc1);			
		TreeTableNode c1 = new TreeTableNode(swc2);			
		
		DefaultTreeTableModel ttModel = new DefaultTreeTableModel(root, cNames);
    	
    	JTreeTable tt = new JTreeTable(ttModel);
    	//tt.setShowGrid(true);
    	
    	JFrame f = new JFrame();
    	f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
			    System.exit(0);
			}
	    });	    
    	f.getContentPane().add(new JScrollPane(tt));
    	f.pack();
    	f.show();
    	root.addChild(c1);
    }    
}
