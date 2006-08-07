/*
 * Created on Apr 19, 2005
 */
package org.mindswap.swoop.utils.graph;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.mindswap.swoop.SwoopModel;
import org.semanticweb.owl.model.OWLOntology;

import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.decorators.ConstantVertexAspectRatioFunction;
import edu.uci.ics.jung.graph.decorators.ConstantVertexStringer;
import edu.uci.ics.jung.graph.decorators.EdgeShape;
import edu.uci.ics.jung.graph.decorators.EdgeShapeFunction;
import edu.uci.ics.jung.graph.decorators.EllipseVertexShapeFunction;
import edu.uci.ics.jung.graph.decorators.VertexAspectRatioFunction;
import edu.uci.ics.jung.graph.decorators.VertexFontFunction;
import edu.uci.ics.jung.graph.decorators.VertexPaintFunction;
import edu.uci.ics.jung.graph.decorators.VertexShapeFunction;
import edu.uci.ics.jung.graph.decorators.VertexSizeFunction;
import edu.uci.ics.jung.graph.decorators.VertexStringer;
import edu.uci.ics.jung.graph.impl.DirectedSparseEdge;
import edu.uci.ics.jung.graph.impl.DirectedSparseGraph;
import edu.uci.ics.jung.graph.impl.DirectedSparseVertex;
import edu.uci.ics.jung.utils.Pair;
import edu.uci.ics.jung.utils.UserDataContainer;
import edu.uci.ics.jung.visualization.FRLayout;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.ISOMLayout;
import edu.uci.ics.jung.visualization.Layout;
import edu.uci.ics.jung.visualization.PickSupport;
import edu.uci.ics.jung.visualization.PickedInfo;
import edu.uci.ics.jung.visualization.PluggableRenderer;
import edu.uci.ics.jung.visualization.ShapePickSupport;
import edu.uci.ics.jung.visualization.SpringLayout;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.ZoomPanGraphMouse;
import edu.uci.ics.jung.visualization.contrib.CircleLayout;
import edu.uci.ics.jung.visualization.contrib.DAGLayout;
import edu.uci.ics.jung.visualization.contrib.KKLayout;

/**
 * @author Evren Sirin
 *
 */
public class GenericGraph extends JPanel implements ActionListener {
    // just a dummy variable used a key
    private static String DATA = "DATA";

    private static int MIN = 20;
    private static int MAX = 100;

    private static final EdgeShapeFunction LINE = new EdgeShape.Line();
    private static final EdgeShapeFunction CURVE = new EdgeShape.QuadCurve();
    private final EdgeShapeFunction CURVED_LINE = new EdgeShapeFn();

    private final UserDataContainer.CopyAction SHARE = new UserDataContainer.CopyAction.Shared();
    private final VertexStringer SHORT_LABEL = new VertexLabel( true );
    private final VertexStringer LONG_LABEL = new VertexLabel( false );
    private final VertexStringer NO_LABEL = new ConstantVertexStringer(""); 
    
    protected SwoopModel model;
    
    protected DirectedSparseGraph graph ;
    protected VisualizationViewer vv;
    private PluggableRenderer pr;
    
    private VertexSize vSize = new VertexSize();
    private VertexFont vFont = new VertexFont();
    
    private VertexAspectRatioFunction vAspect = new ConstantVertexAspectRatioFunction( 1.0f );
    private VertexShapeFunction vShape = new EllipseVertexShapeFunction( vSize, vAspect );
    
    private EdgeShapeFunction eShape = CURVED_LINE;
    
    private GraphProperties props;
    
    public GenericGraph(SwoopModel model, Object obj, GraphProperties props) {
        this( model, Collections.singleton( obj ), props );
    }

    public GenericGraph(SwoopModel model, Collection partitions, GraphProperties props) {
        this.model = model;
        this.props = props;
        
        graph = new DirectedSparseGraph();
        for(Iterator i = partitions.iterator(); i.hasNext();) 
        {
            Object obj = i.next();
            addToGraph(graph, obj);            
        }
        setupUI();
    }
        
    protected void setupUI() {    
        pr = new PluggableRenderer();
        
        Layout layout = new FRLayout( graph );
        
        vv = new VisualizationViewer(layout, pr);
        vv.setPickSupport(new ShapePickSupport(vv));

        pr.setVertexPaintFunction( new VertexColor( vv ) );
        pr.setVertexShapeFunction( vShape );
        pr.setVertexStringer( NO_LABEL );
        pr.setVertexLabelCentering( true );
        pr.setVertexFontFunction( vFont );
        pr.setEdgeShapeFunction( eShape );

        vSize.setGraph( graph );
        
        GraphZoomScrollPane scrollPane = new GraphZoomScrollPane(vv);
        ZoomPanGraphMouse gm = new ZoomPanGraphMouse(vv);
        vv.setGraphMouse( gm );
        vv.setToolTipListener(new VertexTips());
        vv.setBackground(Color.white);
        
        setLayout( new BorderLayout() );
        add( scrollPane );        
        add( getControlPanel(), BorderLayout.SOUTH);
    }
    
    protected JPanel getControlPanel() {
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        
		String[] layouts = { 
		    "KK Layout", "FR Layout", "Circle Layout", 
		    "Spring Layout", "ISOM Layout", "DAG Layout"};
		JComboBox jcb = new JComboBox( layouts );
		jcb.setActionCommand("layout");
		jcb.addActionListener(this);
		jcb.setSelectedItem( props.getPreferredLayout() );
		Box layoutPanel = Box.createHorizontalBox();
		layoutPanel.add(new JLabel("Graph Layout"));
		layoutPanel.add(jcb);
        
		Box labelPanel = Box.createHorizontalBox();
        JCheckBox v_labels = new JCheckBox("Show partition labels");
        v_labels.setAlignmentX(Component.LEFT_ALIGNMENT);
        v_labels.setActionCommand("label");
        v_labels.addActionListener(this);
        v_labels.setSelected( false );
        JCheckBox v_font = new JCheckBox("Use bold font");
        v_font.setActionCommand("font");
        v_font.addActionListener(this);
        v_font.setSelected( false );        
        labelPanel.add(v_labels);
        labelPanel.add(v_font);
        
        JCheckBox v_size = new JCheckBox("Scale nodes with respect to number of entities in the partition");
        v_size.setAlignmentX(Component.LEFT_ALIGNMENT);
        v_size.addActionListener(this);
        v_size.setActionCommand("scale");
        v_size.setSelected( true );       
        
        JCheckBox eShape = new JCheckBox("Do not overlap inverse edges");
        eShape.setAlignmentX(Component.LEFT_ALIGNMENT);
        eShape.addActionListener(this);
        eShape.setActionCommand("inverseEdge");
        eShape.setSelected( true );            

        JButton plus = new JButton("+");
        plus.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                vv.scale(1.1, 1.1);
             }
        });
        JButton minus = new JButton("-");
        minus.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                vv.scale(0.9,0.9);
            }
        });
        JLabel zoomLabel = new JLabel("Zoom");
        zoomLabel.setAlignmentX(Component.LEFT_ALIGNMENT);        
        Box zoomPanel = Box.createHorizontalBox();
        zoomPanel.add(zoomLabel);
        zoomPanel.add(plus);
        zoomPanel.add(minus);

        
        controlPanel.add(labelPanel);
        controlPanel.add(v_size);  
        controlPanel.add(eShape);
        controlPanel.add(zoomPanel);
        controlPanel.add(layoutPanel);
        
        return controlPanel;
    }

    public void actionPerformed(ActionEvent e)
    {
        String cmd = e.getActionCommand();
        if( cmd.equals("label") ) {
            VertexStringer vs = ((JCheckBox) e.getSource()).isSelected()
            	? SHORT_LABEL
            	: NO_LABEL; 
            pr.setVertexStringer( vs );
        }
        else if( cmd.equals("inverseEdge") ) {
            EdgeShapeFunction es = ((JCheckBox) e.getSource()).isSelected()
            	? CURVED_LINE
            	: LINE; 
            pr.setEdgeShapeFunction( es );
        }        
        else if( cmd.equals("scale") ) {
            vSize.setScaling( ((JCheckBox) e.getSource()).isSelected() );
        }
        else if( cmd.equals("font") ) {
            vFont.setBold( ((JCheckBox) e.getSource()).isSelected() );
        }
        else if( cmd.equals("layout") ) {
            String layoutName = ((JComboBox) e.getSource()).getSelectedItem().toString();
            if( layoutName.startsWith("KK") )
                vv.setGraphLayout( new KKLayout( graph ) );
            else if( layoutName.startsWith("Spring") )
                vv.setGraphLayout( new SpringLayout( graph ) );
            else if( layoutName.startsWith("Circle") )
                vv.setGraphLayout( new CircleLayout( graph ) );
            else if( layoutName.startsWith("FR") )
                vv.setGraphLayout( new FRLayout( graph ) );
            else if( layoutName.startsWith("ISOM") )
                vv.setGraphLayout( new ISOMLayout( graph ) );
            else if( layoutName.startsWith("DAG") )
                vv.setGraphLayout( new DAGLayout( graph ) );
            else
                throw new RuntimeException("Unknown layout");
        }

        vv.repaint();
    }
    
    protected DirectedSparseVertex addToGraph(DirectedSparseGraph graph, Object obj) 
    {
        DirectedSparseVertex node = (DirectedSparseVertex) graph.getUserDatum( obj );
        if(node == null) 
        {
            node = new DirectedSparseVertex();
            node.setUserDatum( DATA, obj, SHARE );
            graph.setUserDatum( obj, node, SHARE );
            graph.addVertex( node );
        
	        Collection linkedOnts = props.getLinkedElements( obj );
	        for(Iterator i = linkedOnts.iterator(); i.hasNext();) {
	            Object linkedObj = i.next();
	            DirectedSparseVertex linkedNode = addToGraph(graph, linkedObj);
	            DirectedSparseEdge edge = new DirectedSparseEdge(node, linkedNode);
	            graph.addEdge(edge);
	        }
        }
        return node;
    }

    private final class VertexSize implements VertexSizeFunction {        
        boolean scale = true;
        int maxSize = Integer.MIN_VALUE;
        int minSize = Integer.MAX_VALUE;
        
        double factor = 1.0;
            
        public VertexSize() {
        }
        
        public void setGraph( Graph g ) {
            for(Iterator i = g.getVertices().iterator(); i.hasNext();) {
                Vertex vertex = (Vertex) i.next();
                int size = props.getSize( vertex.getUserDatum( DATA ) );
                maxSize = Math.max( maxSize, size );
                minSize = Math.min( minSize, size );
            }
            
            if( maxSize == minSize )
                factor = 0.0;
            else 
                factor = (double) (MAX - MIN) / (maxSize - minSize);
        }
        
        public void setScaling(boolean scale) {
            this.scale = scale;
        }
        
        public boolean getScaling() {
            return scale;
        }
        
        public int getSize( Vertex vertex ) {
            if( scale ) {
                int size = props.getSize( vertex.getUserDatum( DATA ) );
                
                return ((int) ((size - minSize) * factor)) + MIN;
            }
            else
                return MIN;
        }
	}
    
    private class VertexLabel implements VertexStringer {
        private boolean shortLabel;
        
        public VertexLabel(boolean qname) {
            this.shortLabel = qname;
        }
        
        public String getLabel(Vertex vertex) {
            Object obj = vertex.getUserDatum( DATA );
            
            if( shortLabel )                    
                return props.getShortName( obj );
            else
                return props.getLongName( obj );
        }
    }   
    
    public class VertexTips implements VisualizationViewer.ToolTipListener {
        public VertexTips() {
        }
    
        public String getToolTipText(MouseEvent e) {
	        PickSupport pickSupport = vv.getPickSupport();
	        Point2D p = vv.transform(e.getPoint());

            Vertex v = pickSupport.getVertex(p.getX(), p.getY());
            if (v != null) {
                return LONG_LABEL.getLabel( v );                
            } else {
                Edge edge = pickSupport.getEdge(p.getX(), p.getY());
                if(edge != null) {
                    return edge.toString();
                }
                return "<html><center>Use the mouse wheel to zoom<p>Click and Drag the mouse to pan</center></html>";
            }
        }
    }
    
    private final class VertexColor implements VertexPaintFunction {
        protected PickedInfo pi;
        
        public VertexColor(VisualizationViewer vv) {
            this.pi = vv.getPickedState();
        }
        
        public Paint getDrawPaint(Vertex v) {
            return pi.isPicked(v)
            	? Color.YELLOW
            	: Color.BLACK;
        }
        
        public Paint getFillPaint(Vertex v) {
            if( v.getOutEdges().isEmpty() ) {                
                if( v.getInEdges().isEmpty() )
                    return Color.GREEN;
                else
                    return Color.BLUE;
            }
            else
                return Color.RED;
        }
    }

    private final static class VertexFont implements VertexFontFunction {
        protected boolean bold = false;
        Font f = new Font("Helvetica", Font.PLAIN, 12);
        Font b = new Font("Helvetica", Font.BOLD, 12);
        
        public void setBold(boolean bold) {
            this.bold = bold;
        }
        
        public Font getFont(Vertex v) {
            return bold ? b : f;
        }        
    }    
    
    private class EdgeShapeFn implements EdgeShapeFunction {
        public Shape getShape(Edge edge) {
            Pair pair = edge.getEndpoints();
            Vertex from = (Vertex) pair.getFirst();
            Vertex to = (Vertex) pair.getSecond();
            if( to.findEdge( from ) == null )
                return LINE.getShape( edge );
            else
                return CURVE.getShape( edge );
        }

        public void setControlOffsetIncrement(float inc) {
            LINE.setControlOffsetIncrement( inc );
            CURVE.setControlOffsetIncrement( inc );
        }    
    
    }
}
