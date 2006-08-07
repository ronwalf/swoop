package org.mindswap.swoop.renderer.entity.graph;

import  com.touchgraph.graphlayout.*;
import com.touchgraph.graphlayout.interaction.DragNodeUI;
import com.touchgraph.graphlayout.interaction.LocalityScroll;
import com.touchgraph.graphlayout.interaction.TGAbstractClickUI;
import com.touchgraph.graphlayout.interaction.TGAbstractDragUI;
import com.touchgraph.graphlayout.interaction.TGUserInterface;
 
import  java.awt.event.*;
import  javax.swing.*;
import  javax.swing.event.*;

import org.mindswap.swoop.TermsDisplay;

/** GLNavigateUI. User interface for moving around the graph, as opposed
  * to editing.
  *   
  * @author   Alexander Shapiro                                        
  * @author   Murray Altheim (abstracted GLPanel to TGScrollPane interface)
  * @version  1.21  $Id: GLNavigateUI.java,v 1.16 2002/04/04 06:21:06 x_ander Exp $
  */
public class GraphNavigateUI extends TGUserInterface {
    
	GraphPanel glPanel;
    TGPanel tgPanel;    
    
    GLNavigateMouseListener ml;
        
    TGAbstractDragUI hvDragUI;
    TGAbstractDragUI rotateDragUI;
    //TGAbstractDragUI hvRotateDragUI;
    
    TGAbstractClickUI hvScrollToCenterUI;
    DragNodeUI dragNodeUI;
    LocalityScroll localityScroll;
    JPopupMenu nodePopup;    
    JPopupMenu edgePopup;    
    Node popupNode;
    Edge popupEdge;
    
    private TermsDisplay myDisplay;
    
    public GraphNavigateUI( GraphPanel glp ) {
        glPanel = glp;
        tgPanel = glPanel.getTGPanel();        
        
        localityScroll = glPanel.getLocalityScroll();
        hvDragUI = glPanel.getHVScroll().getHVDragUI();
        rotateDragUI = glPanel.getRotateScroll().getRotateDragUI();
        hvScrollToCenterUI = glPanel.getHVScroll().getHVScrollToCenterUI();
        dragNodeUI = new DragNodeUI(tgPanel);                    

        ml = new GLNavigateMouseListener();
        setUpNodePopup();
        setUpEdgePopup();
        //myDisplay = termsDisplay;
    }
    
    public void activate() {        
        tgPanel.addMouseListener(ml);
    }
    
    public void deactivate() {
        tgPanel.removeMouseListener(ml);
    }
    
    class GLNavigateMouseListener extends MouseAdapter {
    
        public void mousePressed(MouseEvent e) {
            Node mouseOverN = tgPanel.getMouseOverN();
            
            if (e.getModifiers() == MouseEvent.BUTTON1_MASK) { 
                if (mouseOverN == null) 
                    hvDragUI.activate(e);
                else 
                {
                    dragNodeUI.activate(e);
                }
            }
        }

        public void mouseClicked(MouseEvent e) {
            Node mouseOverN = tgPanel.getMouseOverN();
            if (mouseOverN == null)
            	return;
            int modifiers = e.getModifiers();
            if ((modifiers & MouseEvent.BUTTON1_MASK) == MouseEvent.BUTTON1_MASK) 
            {               	
            	// if shift key is pressed, adjust the locale
            	if (( modifiers & InputEvent.SHIFT_MASK) == InputEvent.SHIFT_MASK)
            	{
                    try {
                        tgPanel.setLocale(mouseOverN, localityScroll.getLocalityRadius());                        
                    }
                    catch (TGException ex) {
                       System.out.println("Error changing locale");
                       ex.printStackTrace();
                    } 
            	}
            	else
            	{		
            		SwoopNode n = (SwoopNode)mouseOverN;
            		n.fireHyperLinkEvent();
            	}        	
            }
        }    
        
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) {
                popupNode = tgPanel.getMouseOverN();
                popupEdge = tgPanel.getMouseOverE();
                if (popupNode!=null) {
                    tgPanel.setMaintainMouseOver(true);
                    nodePopup.show(e.getComponent(), e.getX(), e.getY());
                }
                else if (popupEdge!=null) {
                    tgPanel.setMaintainMouseOver(true);
                    edgePopup.show(e.getComponent(), e.getX(), e.getY());
                }
                else {                    
                    glPanel.glPopup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        }    

    }

    private void setUpNodePopup() {        
        nodePopup = new JPopupMenu();
        JMenuItem menuItem;
        
        menuItem = new JMenuItem("Expand Node");
        ActionListener expandAction = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if(popupNode!=null) {
                        tgPanel.expandNode(popupNode);
                    }
                }
            };
            
        menuItem.addActionListener(expandAction);
        nodePopup.add(menuItem);
         
        menuItem = new JMenuItem("Collapse Node");
        ActionListener collapseAction = new ActionListener() {
                public void actionPerformed(ActionEvent e) {                    
                    if(popupNode!=null) {
                        tgPanel.collapseNode(popupNode );
                    }
                }
            };
            
        menuItem.addActionListener(collapseAction);
        nodePopup.add(menuItem);

        menuItem = new JMenuItem("Hide Node");
        ActionListener hideAction = new ActionListener() {
                public void actionPerformed(ActionEvent e) {                    
                    if(popupNode!=null) {
                        tgPanel.hideNode(popupNode );
                    }
                }
            };
            
        menuItem.addActionListener(hideAction);
        nodePopup.add(menuItem);

        nodePopup.addPopupMenuListener(new PopupMenuListener() {
            public void popupMenuCanceled(PopupMenuEvent e) {}
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                tgPanel.setMaintainMouseOver(false);
                tgPanel.setMouseOverN(null);
                tgPanel.repaint();        
            }
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}
        });
        
    }

    private void setUpEdgePopup() {        
        edgePopup = new JPopupMenu();
                 
        JMenuItem hideMenuItem = new JMenuItem("Hide Edge");
        ActionListener hideAction = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if(popupEdge!=null) {
                        tgPanel.hideEdge(popupEdge);
                    }
                }
            };
            
        JMenuItem loosenMenuItem = new JMenuItem("Loosen Edge");
        ActionListener loosenAction = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(popupEdge!=null) {
                	popupEdge.setLength( popupEdge.getLength() + 20 );
                	tgPanel.resetDamper();
                }
            }
        };
        
        JMenuItem tightenMenuItem = new JMenuItem("Tighten Edge");
        ActionListener tightenAction = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(popupEdge!=null) 
                {
                	if (popupEdge.getLength() <= 20)
                		return;
                	popupEdge.setLength( popupEdge.getLength() - 20 );
                	tgPanel.resetDamper();
                }
            }
        };
        
        hideMenuItem.addActionListener( hideAction );
        loosenMenuItem.addActionListener( loosenAction );
        tightenMenuItem.addActionListener( tightenAction );
        
        edgePopup.add(hideMenuItem);        
        edgePopup.add(loosenMenuItem);
        edgePopup.add(tightenMenuItem);
        
        edgePopup.addPopupMenuListener(new PopupMenuListener() {
            public void popupMenuCanceled(PopupMenuEvent e) {}
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                tgPanel.setMaintainMouseOver(false);
                tgPanel.setMouseOverE(null);
                tgPanel.repaint();
            }
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}
        });
    }

    // We need to have TermsDisplay so we can send it
    //  HyperlinkEvents when a node is clicked.
    //  In TouchGraphEntityRenderer, we always call this
    //  this method when a render is invoked.
    public void setTermsDisplay( TermsDisplay display )
	{ myDisplay = display; }
    
} // end com.touchgraph.graphlayout.interaction.GLNavigateUI
