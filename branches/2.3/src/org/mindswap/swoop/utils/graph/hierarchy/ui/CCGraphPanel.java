package org.mindswap.swoop.utils.graph.hierarchy.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.uci.ics.jung.visualization.GraphDraw;
import edu.uci.ics.jung.visualization.VisualizationViewer;


public class CCGraphPanel extends JPanel 
{
    protected VisualizationViewer vv;
    protected boolean hadjusting;
    protected boolean vadjusting;

    public CCGraphPanel(VisualizationViewer vv) 
    {
        super(new BorderLayout());
        this.vv = vv;
        add(vv);
    	this.setupKeyBindings();
    	this.requestFocusInWindow();
    }
    
	private void setupKeyBindings()
	{
		// setting up key actions
		ActionMap amap = getActionMap();
		InputMap  imap = getInputMap();
	}

}