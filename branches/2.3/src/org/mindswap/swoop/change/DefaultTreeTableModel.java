package org.mindswap.swoop.change;
import javax.swing.tree.TreeNode;
import javax.swing.table.TableModel;
import javax.swing.table.DefaultTableModel;

import org.mindswap.swoop.treetable.AbstractTreeTableModel;
import org.mindswap.swoop.treetable.TreeTableModel;

public class DefaultTreeTableModel extends AbstractTreeTableModel 
                             implements TreeTableModel {

    protected String[]  cNames;
	
    public DefaultTreeTableModel(TreeTableNode root, String[]  cNames) { 
		super(root);
		this.cNames = cNames;
    }

    public int getChildCount(Object node) { 
		return ((TreeTableNode) node).getChildCount();
    }

    public Object getChild(Object node, int i) { 
		return ((TreeTableNode) node).getChildAt(i);
    }

    //
    //  The TreeTableNode interface. 
    //
    public int getColumnCount() {
		return ((TreeTableNode) getRoot()).getColumnCount();
    }

    public String getColumnName(int column) {
		return cNames[column];
    }


    public Class getColumnClass(int column) {
		return column == 0 ? TreeTableModel.class : Object.class;
    }
 
    public Object getValueAt(Object node, int column) {
		return ((TreeTableNode) node).getValueAt(column);
    }

    public boolean isCellEditable(Object node, int column) { 
         return true; 
    }

    public void setValueAt(Object aValue, Object node, int column) {
    	((TreeTableNode) node).setValueAt(aValue, column);
    }
    
}
