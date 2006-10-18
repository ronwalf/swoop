/*
 * Created on Apr 19, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.mindswap.swoop.renderer.entity.graph;

import com.touchgraph.graphlayout.Edge;
import com.touchgraph.graphlayout.Node;
import com.touchgraph.graphlayout.TGException;
import com.touchgraph.graphlayout.TGLensSet;
import com.touchgraph.graphlayout.TGPanel;
import com.touchgraph.graphlayout.graphelements.GraphEltSet;
import  com.touchgraph.graphlayout.interaction.*;

import  java.awt.*;
import  java.awt.event.*;
import  javax.swing.*;

import org.mindswap.swoop.TermsDisplay;

import  java.util.Hashtable;
import java.util.Iterator;


/*
 * 
 * @author Dave
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */

public class GraphPanel extends JEditorPane {

	public static int GRAPH_MODE = 0;
	public static int TEXT_MODE = 1;
	
    public String zoomLabel = "Zoom"; // label for zoom menu item
    public String rotateLabel = "Rotate"; // label for rotate menu item
    public String localityLabel = "Locality"; // label for locality menu item

    public HVScroll hvScroll;
    public ZoomScroll zoomScroll;
    //public HyperScroll hyperScroll; // unused
    public RotateScroll rotateScroll;
    public LocalityScroll localityScroll;
    public JPopupMenu glPopup;
    public Hashtable scrollBarHash; //= new Hashtable();

    protected int myMode = GRAPH_MODE;
    
    protected JPanel scrollPanel = new JPanel();
    
    protected JEditorPane myEditorPane; // used to display text components
    protected TGPanel tgPanel;          // graph element
    protected TGLensSet tgLensSet;
    protected TGUIManager tgUIManager;

    private GraphNavigateUI myNavigateUI;
    private Color defaultColor = Color.lightGray;

  // ............


   /** Default constructor.
     */
    public GraphPanel() {
        
        initialize();
    }

   /** Initialize panel, lens, and establish a random graph as a demonstration.
     */
    public void initialize() 
	{
    	tgPanel = new TGPanel();
        scrollBarHash = new Hashtable();
        tgLensSet = new TGLensSet();        
        hvScroll = new HVScroll(tgPanel, tgLensSet);
        zoomScroll = new ZoomScroll(tgPanel);
        rotateScroll = new RotateScroll(tgPanel);
        localityScroll = new LocalityScroll(tgPanel);
        
        buildPanel();
        buildLens();
        tgPanel.setLensSet(tgLensSet);
        addUIs();
        
        setVisible(true);
    }
    
    
    public void clear()
	{
    	tgPanel.clearSelect();
    	tgPanel.clearAll();
    	//initialize();
	}

    /** Return the TGPanel used with this GLPanel. */
    public TGPanel getTGPanel() {
        return tgPanel;
    }

  // navigation .................

    /** Return the HVScroll used with this GLPanel. */
    public HVScroll getHVScroll()
    {
        return hvScroll;
    }

    ///** Return the HyperScroll used with this GLPanel. */
    //public HyperScroll getHyperScroll()
    //{
    //    return hyperScroll;
    //}

    /** Sets the horizontal offset to p.x, and the vertical offset to p.y
      * given a Point <tt>p<tt>. 
      */
    public void setOffset( Point p ) {
        hvScroll.setOffset(p);
    };

    /** Return the horizontal and vertical offset position as a Point. */
    public Point getOffset() {
        return hvScroll.getOffset();
    };

  // rotation ...................

    /** Return the RotateScroll used with this GLPanel. */
    public RotateScroll getRotateScroll()
    {
        return rotateScroll;
    }

    /** Set the rotation angle of this GLPanel (allowable values between 0 to 359). */
     public void setRotationAngle( int angle ) {
        rotateScroll.setRotationAngle(angle);
    }

    /** Return the rotation angle of this GLPanel. */
    public int getRotationAngle() {
        return rotateScroll.getRotationAngle();
    }

  // locality ...................

    /** Return the LocalityScroll used with this GLPanel. */
    public LocalityScroll getLocalityScroll()
    {
        return localityScroll;
    }

    /** Set the locality radius of this TGScrollPane  
      * (allowable values between 0 to 4, or LocalityUtils.INFINITE_LOCALITY_RADIUS). 
      */
    public void setLocalityRadius( int radius ) {
        localityScroll.setLocalityRadius(radius);
    }

    /** Return the locality radius of this GLPanel. */
    public int getLocalityRadius() {
        return localityScroll.getLocalityRadius();
    }

  // zoom .......................

    /** Return the ZoomScroll used with this GLPanel. */
    public ZoomScroll getZoomScroll() 
    {
        return zoomScroll;
    }

    /** Set the zoom value of this GLPanel (allowable values between -100 to 100). */
    public void setZoomValue( int zoomValue ) {
        zoomScroll.setZoomValue(zoomValue);
    }

    /** Return the zoom value of this GLPanel. */
    public int getZoomValue() {
        return zoomScroll.getZoomValue();
    }

  // ....

    public JPopupMenu getGLPopup() 
    {
        return glPopup;
    }

    public void buildLens() {
        tgLensSet.addLens(hvScroll.getLens());
        tgLensSet.addLens(zoomScroll.getLens());
      //tgLensSet.addLens(hyperScroll.getLens());
        tgLensSet.addLens(rotateScroll.getLens());
        tgLensSet.addLens(tgPanel.getAdjustOriginLens());
    }

    public void buildPanel() {
        final JScrollBar horizontalSB = hvScroll.getHorizontalSB();
        final JScrollBar verticalSB = hvScroll.getVerticalSB();
        final JScrollBar zoomSB = zoomScroll.getZoomSB();
        final JScrollBar rotateSB = rotateScroll.getRotateSB();
        final JScrollBar localitySB = localityScroll.getLocalitySB();
        
        scrollPanel = new JPanel();
        setLayout(new BorderLayout());

        
        scrollPanel.setBackground(defaultColor);
        scrollPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();


        new Color(0, 64, 128);
        JPanel modeSelectPanel = new JPanel();
        modeSelectPanel.setBackground(defaultColor);
        modeSelectPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0,0));

        AbstractAction navigateAction = new AbstractAction("Navigate") {
            public void actionPerformed(ActionEvent e) {
                tgUIManager.activate("Navigate");
            }
        };

        JRadioButton rbNavigate = new JRadioButton(navigateAction);
        rbNavigate.setBackground(defaultColor);
        rbNavigate.setSelected(true);

        ButtonGroup bg = new ButtonGroup();
        bg.add(rbNavigate);

        modeSelectPanel.add(rbNavigate);

        final JPanel topPanel = new JPanel();
        topPanel.setBackground(defaultColor);
        topPanel.setLayout(new GridBagLayout());
        c.gridy=0; c.fill=GridBagConstraints.HORIZONTAL;
        /*
        c.gridx=0;c.weightx=0;
        topPanel.add(new Label("Zoom",Label.RIGHT), c);
        c.gridx=1;c.weightx=0.5;
        topPanel.add(zoomSB,c);
        c.gridx=2;c.weightx=0;
        topPanel.add(new Label("Locality",Label.RIGHT), c);
        c.gridx=3;c.weightx=0.5;
        topPanel.add(localitySB,c);
        */
        c.gridx=0;c.weightx=0;c.insets = new Insets(0,10,0,10);
        topPanel.add(modeSelectPanel,c);
        c.insets=new Insets(0,0,0,0);
        c.gridx=1;c.weightx=1;

        scrollBarHash.put(zoomLabel, zoomSB);
        scrollBarHash.put(rotateLabel, rotateSB);
        scrollBarHash.put(localityLabel, localitySB);

        JPanel scrollselect = scrollSelectPanel(new String[] {zoomLabel, rotateLabel, localityLabel});
        scrollselect.setBackground(defaultColor);
        topPanel.add(scrollselect,c);

        add(topPanel, BorderLayout.NORTH);

        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = 1;
        c.gridx = 0; c.gridy = 1; c.weightx = 1; c.weighty = 1;
        scrollPanel.add(tgPanel,c);

        c.gridx = 1; c.gridy = 1; c.weightx = 0; c.weighty = 0;
        scrollPanel.add(verticalSB,c);

        c.gridx = 0; c.gridy = 2;
        scrollPanel.add(horizontalSB,c);

        add(scrollPanel,BorderLayout.CENTER);

        glPopup = new JPopupMenu();
        glPopup.setBackground(defaultColor);

        JMenuItem menuItem = new JMenuItem("Toggle Controls");
        ActionListener toggleControlsAction = new ActionListener() {
                boolean controlsVisible = true;
                public void actionPerformed(ActionEvent e) {
                    controlsVisible = !controlsVisible;
                    horizontalSB.setVisible(controlsVisible);
                    verticalSB.setVisible(controlsVisible);
                    topPanel.setVisible(controlsVisible);
                }
            };
        menuItem.addActionListener(toggleControlsAction);
        glPopup.add(menuItem);
    }

    protected JPanel scrollSelectPanel(String[] scrollBarNames) {
        final JComboBox scrollCombo = new JComboBox(scrollBarNames);
        scrollCombo.setBackground(defaultColor);
        scrollCombo.setPreferredSize(new Dimension(80,20));
        scrollCombo.setSelectedIndex(0);
        final JScrollBar initialSB = (JScrollBar) scrollBarHash.get(scrollBarNames[0]);
        scrollCombo.addActionListener(new ActionListener() {
            JScrollBar currentSB = initialSB;
            public void actionPerformed(ActionEvent e) {
                JScrollBar selectedSB = (JScrollBar) scrollBarHash.get(
                        (String) scrollCombo.getSelectedItem());
                if (currentSB!=null) currentSB.setVisible(false);
                if (selectedSB!=null) selectedSB.setVisible(true);
                currentSB = selectedSB;
            }
        });

        final JPanel sbp = new JPanel(new GridBagLayout());
        sbp.setBackground(defaultColor);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0; c.weightx= 0;
        sbp.add(scrollCombo,c);
        c.gridx = 1; c.gridy = 0; c.weightx = 1; c.insets=new Insets(0,10,0,17);
        c.fill=GridBagConstraints.HORIZONTAL;
        for (int i = 0;i<scrollBarNames.length;i++) {
            JScrollBar sb = (JScrollBar) scrollBarHash.get(scrollBarNames[i]);
              if(sb==null) continue;
              if(i!=0) sb.setVisible(false);
              //sb.setMinimumSize(new Dimension(200,17));
              sbp.add(sb,c);
        }
        return sbp;
    }

    public void addUIs() {
        tgUIManager = new TGUIManager();
        myNavigateUI = new GraphNavigateUI(this);
        tgUIManager.addUI(myNavigateUI,"Navigate");
        tgUIManager.activate("Navigate");
    }

    public void randomGraph() throws TGException {
        Node n1= tgPanel.addNode();
        n1.setType(0);
        for ( int i=0; i<249; i++ ) {
            Node r = tgPanel.getGES().getRandomNode();
            Node n = tgPanel.addNode();
            n.setType(4);
            if (tgPanel.findEdge(r,n)==null) tgPanel.addEdge(r,n,Edge.DEFAULT_LENGTH);
            if (i%2==0) {
                r = tgPanel.getGES().getRandomNode();
                if (tgPanel.findEdge(r,n)==null) tgPanel.addEdge(r,n,Edge.DEFAULT_LENGTH);
            }
        }
        tgPanel.setLocale(n1,2);
    }    

    
	public Node addNode( Node node) throws TGException
	{
		return tgPanel.addNode( node );
	}
	
	public Node addNode( String name ) throws TGException
	{
		return tgPanel.addNode( name );
	}
	
	public void addEdge(Edge edge)
	{
		tgPanel.addEdge( edge );
	}
    
	public Edge addEdge( Node node1, Node node2, int distance)
	{
		return tgPanel.addEdge( node1, node2, distance );
	}
	
	public Iterator getAllNodes()
	{
		return tgPanel.getAllNodes();
	}
	
	public void setTermsDisplay( TermsDisplay display )
	{
		myNavigateUI.setTermsDisplay( display );
	}
	
	public void DEBUG()
	{
		Iterator it = getAllNodes();
		for (;it.hasNext();)
		{
			Node n = (Node)it.next();
			System.out.println( n.getID() );
		}
	}
	
	public void setMode( int mode )
	{

		if (myMode == mode)
			return;
		if (mode == GRAPH_MODE)
		{
			this.removeAll();
			initialize();
			myMode = mode;
		}
		else if (mode == TEXT_MODE)
		{
			this.removeAll();
			myMode = mode;
		}
		//validate();	
		Container parent = this.getParent();
		parent.validate();
		setVisible(true);
	}
	
	public void setText( String info)
	{
		//if (myMode != TEXT_MODE)
		//	return;
		this.setMode( TEXT_MODE );
		super.setContentType("text/html");
		super.setText( info );
	}
	
    public static void main(String[] args) {

        JFrame frame;
        frame = new JFrame("Graph Layout");
        GraphPanel glPanel = new GraphPanel();
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {System.exit(0);}
        });

        frame.getContentPane().add("Center", glPanel);
        frame.setSize(500,500);
        frame.setVisible(true);
        
    }

}
