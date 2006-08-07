/*
 * Created on Jul 27, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * @author Dave Wang
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.utils.graph.hierarchy;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.mindswap.swoop.SwoopModel;

import edu.uci.ics.jung.exceptions.FatalException;
import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.impl.DirectedSparseGraph;
import edu.uci.ics.jung.graph.impl.DirectedSparseVertex;
import edu.uci.ics.jung.utils.PickEventListener;
import edu.uci.ics.jung.visualization.GraphMouseListener;
import edu.uci.ics.jung.visualization.Layout;
import edu.uci.ics.jung.visualization.MultiPickedState;
import edu.uci.ics.jung.visualization.PickSupport;
import edu.uci.ics.jung.visualization.PickedState;
import edu.uci.ics.jung.visualization.Renderer;
import edu.uci.ics.jung.visualization.StatusCallback;
import edu.uci.ics.jung.visualization.VisualizationViewer;

/**
 * A class that maintains many of the details necessary for creating
 * visualizations of graphs.
 * 
 * @author Joshua O'Madadhain
 * @author Tom Nelson
 * @author Danyel Fisher
 */
public class OntologyVisualizationViewer extends VisualizationViewer {
	//protected StatusCallback statusCallback;
	Thread relaxer;

	boolean suspended;

	boolean manualSuspend;

	private SwoopModel myModel = null;

	private OntologyWithClassHierarchyGraph myGraph = null;

	protected Set previousSelectedNodes = null; // selected nodes (by
												// mouse/selection list)

	protected Set previousHighlightedNodes = null; // highlighted nodes (by
												   // search box)

	protected SwoopOntologyVertex previousSelectedVertex = null; // selected
																 // vertex by
																 // mouse

	protected ClassTreeNode listBrowsedNode = null; // viewed/browsed in the class list

	protected ClassTreeNode rightSelectedNode = null; // right clicked node
	
	protected ClassTreeNode currentSelectedNode = null;  // the node that's selected by user

	//protected Renderer renderer;
	//protected Layout layout;

	//protected ToolTipListener toolTipListener;

	/**
	 * holds the values for zoom/pan of the display
	 */
	//protected AffineTransform transform;
	/**
	 * the inverse of transform. Used to map window points to graph points.
	 * Lazily created in transform method and reset to null any time transform
	 * is changed.
	 */
	//protected AffineTransform inverse;
	//protected Map renderingHints = new HashMap();
	/**
	 * pluggable support for picking graph elements by finding them based on
	 * their coordinates. Typically used in mouse events.
	 */
	//protected PickSupport pickSupport;
	/**
	 * pluggable support for handling the picked/not-picked state of graph
	 * elements.
	 */
	//protected PickedState pickedState;
	/**
	 * an offscreen image to render the graph
	 */
	//protected BufferedImage offscreen;
	/**
	 * graphics context for the offscreen image
	 */
	//protected Graphics2D offscreenG2d;
	/**
	 * a collection of user-implementable functions to render under the topology
	 * (before the graph is rendered)
	 */
	//protected List preRenderers = new ArrayList();
	/**
	 * a collection of user-implementable functions to render over the topology
	 * (after the graph is rendered)
	 */
	//protected List postRenderers = new ArrayList();
	//protected long relaxerThreadSleepTime = 20L;
	/**
	 * The <code>changeListener</code>.
	 */
	//protected ChangeListener changeListener;
	/**
	 * Only one <code>ChangeEvent</code> is needed instance since the event's
	 * only state is the source property. The source of events generated is
	 * always "this".
	 */
	//protected transient ChangeEvent changeEvent;
	//public Object pauseObject = new String("PAUSE OBJECT");
	
	// keeps track of where the current view port is centered at
	protected double currentX = 0;
	protected double currentY = 0;

	// debugging values
	private boolean DEBUG = false;
	
	private double DEBUGX = 0;
	private double DEBUGY = 0;
	private double DEBUGW = 0;
	private double DEBUGH = 0;
	
	private double DX = 0;
	private double DY = 0;
	
	/**
	 * The VisualizationViewer constructor creates a JPanel based a given Layout
	 * and Renderer. While GraphDraw places reasonable defaults on these, this
	 * gives more precise control.
	 * 
	 * @param layout
	 *            The Layout to apply, with its associated Graph
	 * @param renderer
	 *            The Renderer to draw it with
	 */
	public OntologyVisualizationViewer(SwoopModel model,
			OntologyWithClassHierarchyGraph graph, Layout layout, Renderer r) {
		super(layout, r);
		myModel = model;
		myGraph = graph;

		// adding postrenderer (overlaygraph)
		//postRenderers.add(myGraph.getOverlayGraph());

		// code copied from the constructor.
		setDoubleBuffered( true );
		this.transform = new AffineTransform();
		this.addComponentListener(new VisualizationListener(this));
		this.renderer = r;
		pickedState = new MultiPickedState();

		if (layout instanceof PickEventListener)
			pickedState.addListener((PickEventListener) layout);
		r.setPickedKey(pickedState);
		this.layout = layout;
		//setPreferredSize(new Dimension(640, 600));
		//setSize(640, 600);
		Dimension d = getPreferredSize();
		Dimension ld = layout.getCurrentSize();
		
		// if the layout has NOT been intialized yet, initialize it
		// now to be the same size as the VisualizationViewer window
		if (ld == null) {
			System.out.println("Ont VV constructor: layout not init" );
			layout.initialize(d);
		}
		ld = layout.getCurrentSize();
		
		if (DEBUG)
		{
			System.out.println( " vv preferred size: w=" + d.width + " h=" + d.height);
			System.out.println( " layout current:    w=" + ld.width + " lh=" + d.height);		
			System.out.println( " after init" );
			System.out.println( " layout current:    w=" + ld.width + " lh=" + d.height);
		}
		
		// set my scale to show the entire layout
		setScale((float) d.width / ld.width, (float) d.height / ld.height,
				new Point2D.Float());
		this.suspended = true;
		this.manualSuspend = false;
		renderingHints.put(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		init();
		initMouseClicker();
	}

	private Rectangle getViewRectangle()
	{
		DirectedSparseGraph graph = myGraph.getVisualGraph();
		Set vertices = graph.getVertices();
		double xmin = Integer.MAX_VALUE;
		double xmax = Integer.MIN_VALUE;
		double ymin = Integer.MAX_VALUE;
		double ymax = Integer.MIN_VALUE;

		for (Iterator it = vertices.iterator(); it.hasNext();) {
			Vertex v = (Vertex) it.next();
			//System.out.println("v x,y= " + layout.getX(v) + " " + layout.getY(v));
			OntologyGraphNode ontNode = ((OntologyGraphNode) v.getUserDatum(OntologyWithClassHierarchyGraph.DATA));
			int r = ontNode.getRadius();
			//System.out.println( "radius = " + r);
			double x = layout.getX(v);
			double y = layout.getY(v);
			if ((x - r) < xmin)
				xmin = x - r;
			if ((x + r) > xmax)
				xmax = x + r;
			if ((y - r) < ymin)
				ymin = y - r;
			if ((y + r) > ymax)
				ymax = y + r;
		}

		Rectangle rect = new Rectangle( (int)xmin, (int)ymin, (int)(xmax - xmin), (int)(ymax - ymin));
		return rect;
	}
	
	/*
	 *  Called by OntologyWithClassHierarchyGraph at init time to zoom in / center at the  
	 *   graph so every vertex on the graph is in the view port.
	 * 
	 */
	public void autoPanZoom() 
	{		
		Rectangle rect = getViewRectangle();
		int xfocus = (int) ( (rect.width/2 + rect.x) );
		int yfocus = (int) ( (rect.height/2 + rect.y) );
		
		// getting the size of the screen space
		int screenWidth = this.getWidth();
		int screenHeight = this.getHeight();
		double hw = screenWidth / 2;
		double hh = screenHeight / 2;

		Point2D point = transform( new Point2D.Double( screenWidth/2, screenHeight/2) );
		Point2D focus = this.transformGraph2Screen( new Point2D.Double( xfocus, yfocus));

		if (DEBUG)
		{
			System.out.println( " screenWidth = " + screenWidth + " screenHeight = "+ screenHeight );
			System.out.println( " xmin = " + rect.x + " ymin = " + rect.y + " xmax " + ( rect.width + rect.x ) + " ymax " + (rect.height + rect.y ));
			System.out.println( " xfocus = " + xfocus + " yfocus = " + yfocus );
			System.out.println( " center of screen: x=" + (screenWidth/2) + " y=" + (screenHeight/2) );
			System.out.println( " graphspace of centerscreen: " + point.getX() + " " + point.getY() );
			System.out.println( " screensapce: xfocus = " + focus.getX() + " yfocus " + focus.getY() );
			System.out.println( " scaling = " + this.getScaleX()); 
		}
		
		DX = focus.getX();
		DY = focus.getY();
		DEBUGX = rect.x;
		DEBUGY = rect.y;
		DEBUGW = rect.width ;
		DEBUGH = rect.height;
		
		// compute for the necessary translation offsets to center
		double translateOffsetX = (point.getX() - xfocus);
		double translateOffsetY = (point.getY() - yfocus);
		//double translateOffsetX = 0;
		//double translateOffsetY = 0;
		
		// compute for the appropriate scaling factor for zoom in
		double min = Math.min(screenHeight, screenWidth);
		double graphMaxLength = Math.max(( rect.width), (rect.height));
		double scaleX = min / (graphMaxLength) / this.getScaleX();
		if (scaleX > 1)
			scaleX = 1;
		double scaleY = scaleX;
		
		// apply translation and zooming
		this.translate(translateOffsetX, translateOffsetY);
		this.scale(scaleX, scaleY);

		// set initial position of where the viewer is viewing
		this.currentX = xfocus;
		this.currentY = yfocus;
	}

	public void autoPanZoomTest() 
	{
		
		Rectangle rect = getViewRectangle();
		int xfocus = (int) ( (rect.width/2 + rect.x) );
		int yfocus = (int) ( (rect.height/2 + rect.y) );

		int screenWidth = this.getWidth();
		int screenHeight = this.getHeight();
		double hw = screenWidth / 2;
		double hh = screenHeight / 2;

		double translateOffsetX = (currentX - xfocus);
		double translateOffsetY = (currentY - yfocus);

		double min = Math.min(screenHeight, screenWidth);
		double graphMaxLength = Math.max(( rect.width ), ( rect.height ));
		double scaleX = min / (graphMaxLength) / this.getScaleX();
		if (scaleX > 1)
			scaleX = 1;
		double scaleY = scaleX;

		this.translate(translateOffsetX, translateOffsetY);
		this.scale(scaleX, scaleY);
	}

	/**
	 * Returns the time between iterations of the Relaxer thread. The Relaxer
	 * thread sleeps for a moment before calling the Layout to update again.
	 * This tells how long the current delay is. The default, 20 milliseconds,
	 * essentially causes the system to run the next iteration with virtually no
	 * pause.
	 * 
	 * @return Returns the relaxerThreadSleepTime.
	 */
	public long getRelaxerThreadSleepTime() {
		return relaxerThreadSleepTime;
	}

	/**
	 * Sets the relaxerThreadSleepTime.
	 * 
	 * @see #getRelaxerThreadSleepTime()
	 * @param relaxerThreadSleepTime
	 *            The relaxerThreadSleepTime to set.
	 */
	public void setRelaxerThreadSleepTime(long relaxerThreadSleepTime) {
		this.relaxerThreadSleepTime = relaxerThreadSleepTime;
	}

	/**
	 * Creates a default mouseClicker behavior: a default
	 * 
	 * @link{GraphMouseImpl}
	 * @deprecated replaced by setGraphMouse()
	 */
	protected void initMouseClicker() {
		// GraphMouseImpl will give original behavior
		setGraphMouse(new GraphMouseImpl(this));
	}

	/**
	 * a setter for the GraphMouse. This will remove any previous GraphMouse
	 * (including the one that is added in the initMouseClicker method.
	 * 
	 * @param graphMouse
	 *            new value
	 */
	public void setGraphMouse(GraphMouse graphMouse) {
		MouseListener[] ml = getMouseListeners();
		for (int i = 0; i < ml.length; i++) {
			if (ml[i] instanceof GraphMouse) {
				removeMouseListener(ml[i]);
			}
		}
		MouseMotionListener[] mml = getMouseMotionListeners();
		for (int i = 0; i < mml.length; i++) {
			if (mml[i] instanceof GraphMouse) {
				removeMouseMotionListener(mml[i]);
			}
		}
		MouseWheelListener[] mwl = getMouseWheelListeners();
		for (int i = 0; i < mwl.length; i++) {
			if (mwl[i] instanceof GraphMouse) {
				removeMouseWheelListener(mwl[i]);
			}
		}
		addMouseListener(graphMouse);
		addMouseMotionListener(graphMouse);
		addMouseWheelListener(graphMouse);
	}

	/**
	 * Sets the showing Renderer to be the input Renderer. Also tells the
	 * Renderer to refer to this visualizationviewer as a PickedKey. (Because
	 * Renderers maintain a small amount of state, such as the PickedKey, it is
	 * important to create a separate instance for each VV instance.)
	 * 
	 * @param v
	 */
	public void setRenderer(Renderer r) {
		this.renderer = r;
		r.setPickedKey(pickedState);
		repaint();
	}

	/**
	 * Returns the renderer used by this instance.
	 * 
	 * @return
	 */
	public Renderer getRenderer() {
		return renderer;
	}

	/**
	 * Removes the current graph layout, and adds a new one.
	 */
	public void setGraphLayout(Layout layout) {
		if (this.layout instanceof PickEventListener)
			pickedState.removeListener((PickEventListener) this.layout);
		suspend();
		Dimension d = getPreferredSize();
		Dimension ld = layout.getCurrentSize();
		// if the layout has NOT been initialized yet, initialize it
		// now to the size of the VisualizationViewer window
		if (ld == null) {
			layout.initialize(d);
		}
		ld = layout.getCurrentSize();
		// set scale to show the entire graph layout
		setScale((float) d.width / ld.width, (float) d.height / ld.height,
				new Point2D.Float());

		this.layout = layout;
		layout.restart();
		prerelax();
		unsuspend();
		if (layout instanceof PickEventListener)
			pickedState.addListener((PickEventListener) layout);
		this.pickSupport.setLayout(layout);
	}

	public void setGraph(OntologyWithClassHierarchyGraph graph) {
		myGraph = graph;
	}

	public OntologyWithClassHierarchyGraph getGraph() {
		return myGraph;
	}

	/**
	 * Returns the current graph layout.
	 */
	public Layout getGraphLayout() {
		return layout;
	}

	/**
	 * This is the interface for adding a mouse listener. The GEL will be called
	 * back with mouse clicks on vertices.
	 * 
	 * @param gel
	 */
	public void addGraphMouseListener(GraphMouseListener gel) {
		addMouseListener(new SwoopMouseListenerTranslator(gel, this));
	}

	/**
	 * starts a visRunner thread without prerelaxing
	 */
	public synchronized void restartThreadOnly() {
		if (visRunnerIsRunning) {
			throw new FatalException("Can't init while a visrunner is running");
		}
		relaxer = new VisRunner();
		relaxer.setPriority(Thread.MIN_PRIORITY);
		relaxer.start();
	}

	/**
	 * Pre-relaxes and starts a visRunner thread
	 */
	public synchronized void init() {
		if (visRunnerIsRunning) {
			throw new FatalException("Can't init while a visrunner is running");
		}
		prerelax();
		relaxer = new VisRunner();
		relaxer.start();
	}

	/**
	 * Restarts layout, then calls init();
	 */
	public synchronized void restart() {
		if (visRunnerIsRunning) {
			throw new FatalException(
					"Can't restart while a visrunner is running");
		}
		layout.restart();
		init();
		repaint();
	}

	/**
	 * 
	 * @see javax.swing.JComponent#setVisible(boolean)
	 */
	public void setVisible(boolean aFlag) {
		super.setVisible(aFlag);
		layout.resize(this.getSize());
	}

	/**
	 * Runs the visualization forward a few hundred iterations (for half a
	 * second)
	 */
	public void prerelax() {
		suspend();

		int i = 0;
		if (layout.isIncremental()) {
			// then increment layout for half a second
			long timeNow = System.currentTimeMillis();
			while (System.currentTimeMillis() - timeNow < 500
					&& !layout.incrementsAreDone()) {
				i++;
				layout.advancePositions();
			}
		}
		unsuspend();
	}

	/**
	 * If the visualization runner is not yet running, kick it off.
	 */
	protected synchronized void start() {
		suspended = false;
		synchronized (pauseObject) {
			pauseObject.notifyAll();
		}
	}

	public synchronized void suspend() {
		manualSuspend = true;
	}

	public synchronized void unsuspend() {
		manualSuspend = false;
		synchronized (pauseObject) {
			pauseObject.notifyAll();
		}
	}

	/**
	 * @deprecated Use <code>getPickedState.isPicked(e)</code>.
	 */
	public boolean isPicked(Vertex v) {
		return pickedState.isPicked(v);
	}

	/**
	 * @deprecated Use <code>getPickedState.isPicked(e)</code>.
	 */
	public boolean isPicked(Edge e) {
		return pickedState.isPicked(e);
	}

	/**
	 * @deprecated Use <code>getPickedState.pick(picked, b)</code>.
	 */
	protected void pick(Vertex picked, boolean b) {
		pickedState.pick(picked, b);
	}

	long[] relaxTimes = new long[5];

	long[] paintTimes = new long[5];

	int relaxIndex = 0;

	int paintIndex = 0;

	double paintfps, relaxfps;

	boolean stop = false;

	boolean visRunnerIsRunning = false;

	protected class VisRunner extends Thread {
		public VisRunner() {
			super("Relaxer Thread");
		}

		public void run() {
			visRunnerIsRunning = true;
			try {
				while (!layout.incrementsAreDone() && !stop) {
					synchronized (pauseObject) {
						while ((suspended || manualSuspend) && !stop) {
							try {
								pauseObject.wait();
							} catch (InterruptedException e) {
							}
						}
					}
					long start = System.currentTimeMillis();
					layout.advancePositions();
					long delta = System.currentTimeMillis() - start;

					if (stop)
						return;

					String status = layout.getStatus();
					if (statusCallback != null && status != null) {
						statusCallback.callBack(status);
					}

					if (stop)
						return;

					relaxTimes[relaxIndex++] = delta;
					relaxIndex = relaxIndex % relaxTimes.length;
					relaxfps = average(relaxTimes);

					if (stop)
						return;

					repaint();

					if (stop)
						return;

					try {
						sleep(relaxerThreadSleepTime);
					} catch (InterruptedException ie) {
					}
				}
			} finally {
				visRunnerIsRunning = false;
			}
		}
	}

	/**
	 * Returns a flag that says whether the visRunner thread is running. If it
	 * is not, then you may need to restart the thread (with
	 * 
	 * @return
	 */
	public boolean isVisRunnerRunning() {
		return visRunnerIsRunning;
	}

	/**
	 * setter for the scale fires a PropertyChangeEvent with the
	 * AffineTransforms representing the previous and new values for scale and
	 * offset
	 * 
	 * @param scalex
	 * @param scaley
	 */
	public void scale(double scalex, double scaley) {
		scale(scalex, scaley, null);
	}

	public void scale(double scalex, double scaley, Point2D from) {
		if (from == null) {
			Dimension d = getSize();
			from = new Point2D.Float(d.width / 2.f, d.height / 2.f);
		}

		AffineTransform xf = AffineTransform.getTranslateInstance(from.getX(),
				from.getY());
		xf.scale(scalex, scaley);
		xf.translate(-from.getX(), -from.getY());
		inverse = null;
		transform.preConcatenate(xf);

		fireStateChanged();
		repaint();
	}

	public void setScale(double scalex, double scaley) {
		setScale(scalex, scaley, null);
	}

	/**
	 * setter for the scale fires a PropertyChangeEvent with the
	 * AffineTransforms representing the previous and new values for scale and
	 * offset
	 * 
	 * @param scalex
	 * @param scaley
	 */
	public void setScale(double scalex, double scaley, Point2D from) {
		if (from == null) {
			Dimension d = getSize();
			from = new Point2D.Float(d.width / 2.f, d.height / 2.f);
		}

		inverse = null;
		transform.setToIdentity();
		transform.translate(from.getX(), from.getY());
		transform.scale(scalex, scaley);
		transform.translate(-from.getX(), -from.getY());

		fireStateChanged();
		repaint();
	}

	/**
	 * getter for scalex
	 * 
	 * @return scalex
	 */
	public double getScaleX() {
		return transform.getScaleX();
	}

	/**
	 * getter for scaley
	 * 
	 * @return
	 */
	public double getScaleY() {
		return transform.getScaleY();
	}

	/**
	 * getter for offsetx
	 * 
	 * @return
	 */
	public double getOffsetX() {
		return getTranslateX();
	}

	public double getTranslateX() {
		return transform.getTranslateX();
	}

	/**
	 * getter for offsety
	 * 
	 * @return
	 */
	public double getOffsetY() {
		return getTranslateY();
	}

	public double getTranslateY() {
		return transform.getTranslateY();
	}

	/**
	 * set the offset values that will be used in the translation component of
	 * the graph rendering transform. Changes the transform to the identity
	 * transform, then sets the translation conponents to the passed values
	 * Fires a PropertyChangeEvent with the AffineTransforms representing the
	 * previous and new values for the transform
	 * 
	 * @param offsetx
	 * @param offsety
	 */
	public void setOffset(double offsetx, double offsety) {
		setTranslate(offsetx, offsety);
	}

	public void setTranslate(double tx, double ty) {
		float scalex = (float) transform.getScaleX();
		float scaley = (float) transform.getScaleY();
		inverse = null;
		transform.setTransform(scalex, 0, 0, scaley, tx, ty);
		fireStateChanged();
		repaint();
	}

	public void translate(double offsetx, double offsety) {
		inverse = null;
		transform.translate(offsetx, offsety);
		fireStateChanged();
		repaint();
		currentX = currentX - offsetx;
		currentY = currentY - offsety;
	}

	/**
	 * Transform the mouse point with the inverse transform of the
	 * VisualizationViewer. This maps from screen coordinates to graph
	 * coordinates.
	 * 
	 * @param p
	 *            the point to transform (typically, a mouse point)
	 * @return a transformed Point2D
	 */
	public Point2D transform(Point2D p) {
		if (inverse == null) {
			try {
				inverse = transform.createInverse();
			} catch (NoninvertibleTransformException e) {
				throw new IllegalArgumentException(e.toString());
			}
		}
		return inverse.transform(p, null);
	}

	public Point2D transformGraph2Screen(Point2D p) {
		return transform.transform(p, null);
	}

	/**
	 * @return Returns the renderingHints.
	 */
	public Map getRenderingHints() {
		return renderingHints;
	}

	/**
	 * @param renderingHints
	 *            The renderingHints to set.
	 */
	public void setRenderingHints(Map renderingHints) {
		this.renderingHints = renderingHints;
	}

	protected synchronized void paintComponent(Graphics g) {
		start();
		super.paintComponent(g);

		Graphics2D g2d = (Graphics2D) g;
		renderGraph();
		g2d.drawImage(offscreen, null, 0, 0);

	}

	protected void renderGraph() {
		if (offscreenG2d == null)
			return;
		offscreenG2d.setRenderingHints(renderingHints);

		long start = System.currentTimeMillis();

		// the size of the VisualizationViewer
		Dimension d = getSize();
		// composites to control layering of the rendering
		AlphaComposite srcOver = AlphaComposite
				.getInstance(AlphaComposite.SRC_OVER);
		AlphaComposite clear = AlphaComposite.getInstance(AlphaComposite.CLEAR);

		// clear the offscreen image
		offscreenG2d.setComposite(clear);
		offscreenG2d.fillRect(0, 0, d.width, d.height);

		AffineTransform oldXform = offscreenG2d.getTransform();

		offscreenG2d.setTransform(transform);

		offscreenG2d.setComposite(srcOver);

		// if there are preRenderers set, paint them
		for (Iterator iterator = preRenderers.iterator(); iterator.hasNext();) {
			Paintable paintable = (Paintable) iterator.next();
			if (paintable.useTransform()) {
				paintable.paint(offscreenG2d);
			} else {
				offscreenG2d.setTransform(oldXform);
				paintable.paint(offscreenG2d);
				offscreenG2d.setTransform(transform);
			}
		}

		// paint all the edges
		for (Iterator iter = layout.getVisibleEdges().iterator(); iter
				.hasNext();) {
			Edge e = (Edge) iter.next();
			Vertex v1 = (Vertex) e.getEndpoints().getFirst();
			Vertex v2 = (Vertex) e.getEndpoints().getSecond();
			renderer.paintEdge(offscreenG2d, e, (int) layout.getX(v1),
					(int) layout.getY(v1), (int) layout.getX(v2), (int) layout
							.getY(v2));
		}

		// paint all the vertices
		for (Iterator iter = layout.getVisibleVertices().iterator(); iter
				.hasNext();) {
			Vertex v = (Vertex) iter.next();
			renderer.paintVertex(offscreenG2d, v, (int) layout.getX(v),
					(int) layout.getY(v));
		}

		// paint overlay edges and such
		//OverlayGraph overlayGraph = myGraph.getOverlayGraph();
		//overlayGraph.paint( offscreenG2d );

		long delta = System.currentTimeMillis() - start;
		paintTimes[paintIndex++] = delta;
		paintIndex = paintIndex % paintTimes.length;
		paintfps = average(paintTimes);

		// if there are postRenderers set, do it
		for (Iterator iterator = postRenderers.iterator(); iterator.hasNext();) {
			Paintable paintable = (Paintable) iterator.next();
			if (paintable.useTransform()) {
				paintable.paint(offscreenG2d);
			} else {
				offscreenG2d.setTransform(oldXform);
				paintable.paint(offscreenG2d);
				offscreenG2d.setTransform(transform);
			}
		}
		offscreenG2d.setTransform(oldXform);
		// visual debugging
		if (DEBUG)
		{
			offscreenG2d.setColor( Color.BLACK );
			Point2D boxTL = transformGraph2Screen( new Point2D.Double(DEBUGX, DEBUGY) );
			Point2D boxBR = transformGraph2Screen( new Point2D.Double(DEBUGX + DEBUGW, DEBUGY + DEBUGH) );
	 		offscreenG2d.drawRect( (int)boxTL.getX(), (int)boxTL.getY(), (int)(boxBR.getX() - boxTL.getX()), (int)(boxBR.getY() - boxTL.getY())  );
	 		offscreenG2d.drawLine( (int)((boxTL.getX()+boxBR.getX())/2), (int)boxTL.getY(), (int)((boxTL.getX()+boxBR.getX())/2), (int)boxBR.getY() );
	 		offscreenG2d.drawLine( (int)boxTL.getX(), (int)((boxTL.getY()+boxBR.getY())/2), (int)boxBR.getX(), (int)((boxTL.getY()+boxBR.getY())/2) );
			offscreenG2d.fillOval( (this.getWidth()/2)-5, (this.getHeight()/2)-5, 10, 10 );
			offscreenG2d.setColor( Color.RED );
			Point2D focus = transformGraph2Screen( new Point2D.Double(DX, DY) );
			offscreenG2d.drawRect( (int)(focus.getX()-5), (int)(focus.getY()-5), 10, 10 );
		}
	}

	/**
	 * Returns the double average of a number of long values.
	 * 
	 * @param paintTimes
	 *            an array of longs
	 * @return the average of the doubles
	 */
	protected double average(long[] paintTimes) {
		double l = 0;
		for (int i = 0; i < paintTimes.length; i++) {
			l += paintTimes[i];
		}
		return l / paintTimes.length;
	}

	protected class VisualizationListener extends ComponentAdapter {
		protected OntologyVisualizationViewer vv;

		public VisualizationListener(OntologyVisualizationViewer vv) {
			this.vv = vv;
		}

		/**
		 * create a new offscreen image for the graph whenever the window is
		 * resied
		 */
		public void componentResized(ComponentEvent e) {
			Dimension d = vv.getSize();
			offscreen = new BufferedImage(d.width, d.height,
					BufferedImage.TYPE_INT_ARGB);
			offscreenG2d = offscreen.createGraphics();
		}
	}

	/**
	 * @param scb
	 */
	public void setTextCallback(StatusCallback scb) {
		this.statusCallback = scb;
	}

	/**
	 *  
	 */
	public synchronized void stop() {
		manualSuspend = false;
		suspended = false;
		stop = true;
		synchronized (pauseObject) {
			pauseObject.notifyAll();
		}
	}

	public void setToolTipListener(ToolTipListener listener) {
		this.toolTipListener = listener;
		setToolTipText("VisViewer"); // something to make tool tips happen at
									 // all
	}

	public String getToolTipText(MouseEvent event) {
		if (toolTipListener != null) {
			return toolTipListener.getToolTipText(event);
		} else {
			return getToolTipText();
		}
	}

	public void resetSelectedNode() 
	{
		if (previousSelectedNodes != null) 
		{
			for (Iterator it = previousSelectedNodes.iterator(); it.hasNext();) 
			{
				ClassTreeNode previousSelectedNode = (ClassTreeNode) it.next();
				previousSelectedNode.setIsSelected(false);
			}
		}
	}

	// used when user traverses through the Class list history
	public void setSelectedNodeWithoutAddingHistory(ClassTreeNode node) {
		resetSelectedNode(); // reset selectred nodes
		resetHighlightedNode(); // reset highlighted nodes
		resetListBrowsedNode(); // reset listbrowsed node
		currentSelectedNode = node; // set selected node
		previousSelectedNodes = new HashSet(); // clean up old vector
		previousSelectedNodes = node.getOntologyNode().findNodesBy( node.getURI() ); // set new node to be previous
		// set new nodes to be selected
		for (Iterator it = previousSelectedNodes.iterator(); it.hasNext();) {
			ClassTreeNode previousSelectedNode = (ClassTreeNode) it.next();
			previousSelectedNode.setIsSelected(true);
		}
	}

	public void setSelectedNode(ClassTreeNode node) 
	{
		if ( node == currentSelectedNode )
			return; // don't the new node is the same as the current one
			
		resetSelectedNode(); // reset selectred nodes
		resetHighlightedNode(); // reset highlighted nodes
		resetListBrowsedNode(); // reset listbrowsed node
		previousSelectedNodes = new HashSet(); // clean up old vector

		currentSelectedNode = node; // set selected node
		
		// set new node to be previous
		DirectedSparseGraph graph = myGraph.getVisualGraph();
		Set vertexSet = graph.getVertices();
		if (node.getParent() == null) // selected node is a OWL:Thing
		{
			OntologyGraphNode graphNode = node.getOntologyNode();
			previousSelectedNodes.addAll(graphNode.findNodesBy(node.getURI()));
		} else {
			for (Iterator it = vertexSet.iterator(); it.hasNext();) {
				DirectedSparseVertex vertex = (DirectedSparseVertex) it.next();
				OntologyGraphNode graphNode = (OntologyGraphNode) vertex.getUserDatum(OntologyWithClassHierarchyGraph.DATA);
				previousSelectedNodes.addAll(graphNode.findNodesBy(node.getURI()));
			}
		}

		// set new nodes to be selected
		for (Iterator it = previousSelectedNodes.iterator(); it.hasNext();) {
			ClassTreeNode previousSelectedNode = (ClassTreeNode) it.next();
			previousSelectedNode.setIsSelected(true);
		}
		myGraph.populateClassList(node); // reload classList tree
	}

	public void resetHighlightedNode() {
		if (previousHighlightedNodes != null) {
			for (Iterator it = previousHighlightedNodes.iterator(); it
					.hasNext();) {
				ClassTreeNode previousHighlighteddNode = (ClassTreeNode) it
						.next();
				previousHighlighteddNode.setIsHighlighted(false);
			}
		}
	}

	public void resetListBrowsedNode() {
		if (listBrowsedNode != null)
			listBrowsedNode.setIsListBrowsed(false);
	}

	public void setHighlightedNodeWithoutAddingHistory(Set nodes) {
		resetSelectedNode(); // reset selectred nodes
		resetHighlightedNode(); // reset highlighted nodes
		resetListBrowsedNode(); // reset listbrowsed node
		previousHighlightedNodes = nodes; // clean up old vector
		for (Iterator it = previousHighlightedNodes.iterator(); it.hasNext();) {
			ClassTreeNode previousHighlightedNode = (ClassTreeNode) it.next();
			previousHighlightedNode.setIsHighlighted(true);
		}
	}

	public void setHighlightedNode(Set nodes, String subText) {
		resetSelectedNode(); // reset selectred nodes
		resetHighlightedNode(); // reset highlighted nodes
		resetListBrowsedNode(); // reset listbrowsed node
		previousHighlightedNodes = nodes;
		for (Iterator it = previousHighlightedNodes.iterator(); it.hasNext();) {
			ClassTreeNode previousHighlightedNode = (ClassTreeNode) it.next();
			previousHighlightedNode.setIsHighlighted(true);
		}
		myGraph.populateClassListWithSearchTerm(nodes, subText); // reload
																 // classList
																 // tree
	}

	public SwoopOntologyVertex getSelectedVertex() {
		return previousSelectedVertex;
	}

	public ClassTreeNode getListBrowsedNode() {
		return listBrowsedNode;
	}

	public void setListBrowsedNode(ClassTreeNode node) {
		if (listBrowsedNode != null)
			listBrowsedNode.setIsListBrowsed(false);
		listBrowsedNode = node;
		node.setIsListBrowsed(true);
	}

	public ClassTreeNode getRightSelectedNode() {
		return rightSelectedNode;
	}
	
	public ClassTreeNode getCurrentSelectedNode()
	{ return currentSelectedNode; }
	
	
	public boolean isNodeOnScreen( ClassTreeNode targetNode )
	{
		int screenWidth = this.getWidth();
		int screenHeight = this.getHeight();
		Point2D.Double point = targetNode.getGlobalCenter();
		
		
		int ulx = (int)(point.x - (targetNode.getRadius() * this.getScaleX()) );
		int uly = (int)(point.y - (targetNode.getRadius()* this.getScaleX()) );
		int brx = (int)(point.x + (targetNode.getRadius()* this.getScaleX()) );
		int bry = (int)(point.y + (targetNode.getRadius()* this.getScaleX()) );
		
		//System.out.println("Parent Center: " + point.x + " " + point.y );
		//System.out.println( "(" + ulx + "," + uly + ") , (" + brx + "," + bry + ")" );
		if ( (ulx > 0) && (uly > 0) && (brx < screenWidth) && (bry < screenHeight) )
			return true;
		return false;
	}
	
	public void smartPanZoomToNode( ClassTreeNode targetNode, ClassTreeNode topNode )
	{
		int screenWidth = this.getWidth();
		int screenHeight = this.getHeight();
		Point2D.Double point = targetNode.getGlobalCenter();
		if (targetNode == topNode)
			point = (Point2D.Double) this.transformGraph2Screen(targetNode.getLocalCenter());

		if (  ( targetNode.getSubtreeDepth() > 6 ) || (targetNode.getNumChildren() > 75)  )
		{
			panZoomToFitNode( targetNode, topNode );
			return;
		}
		
		double r = targetNode.getRadius();
		double screenR = r * this.getScaleX();
		double screenD = 2 * screenR;
		if ( ((screenD) > screenWidth) && ((screenD) > screenHeight) )
		{
			panZoomToFitNode( targetNode, topNode ); 
		}
		else if ( screenD < 75 )
		{
			panZoomToFitNode( targetNode, topNode );
		}
	}

	public void panZoomToFitNode( ClassTreeNode targetNode, ClassTreeNode topNode )
	{
		//System.out.println("targetNode: " + targetNode.getURI() );
		//System.out.println("topNode:    " + topNode.getURI() );
		int screenWidth = this.getWidth();
		int screenHeight = this.getHeight();
		Point2D.Double point = targetNode.getGlobalCenter();
		if (targetNode == topNode)
			point = (Point2D.Double) this.transformGraph2Screen(targetNode.getLocalCenter());

		double r = targetNode.getRadius();
		if (targetNode == topNode)
			r = topNode.getOntologyNode().getRadius();
		
		double min = Math.min(screenHeight, screenWidth);
		double scaleX = min / (2 * r) / this.getScaleX();
		double scaleY = scaleX;

		double hw = screenWidth / 2;
		double hh = screenHeight / 2;
		double translateOffsetX = (hw - point.x)/ this.getScaleX();
		double translateOffsetY = (hh - point.y)/ this.getScaleX();
		this.translate(translateOffsetX, translateOffsetY);
		this.scale(scaleX, scaleY);
		//System.out.println(" translate x, y = " + translateOffsetX + " " + translateOffsetY);
		//System.out.println(" scalex = " + scaleX );
	}

	
	/**
	 * an interface for the preRender and postRender
	 */
	public interface Paintable {
		public void paint(Graphics g);

		public boolean useTransform();
	}

	/**
	 * a convenience type to represent a class that processes all types of mouse
	 * events for the graph
	 */
	public interface GraphMouse extends MouseListener, MouseMotionListener,
			MouseWheelListener {
	}

	/**
	 * this is the original GraphMouse class, renamed to use GraphMouse as the
	 * interface name, and updated to correctly apply the vv transform to the
	 * point point
	 *  
	 */
	protected final class GraphMouseImpl extends MouseAdapter implements GraphMouse 
	{
		protected Vertex picked;

		protected OntologyVisualizationViewer myViewer = null;

		public GraphMouseImpl(OntologyVisualizationViewer v) {
			myViewer = v;
		}

		public void mouseClicked(MouseEvent e)
		{}
		
		public void mousePressed(MouseEvent e) 
		{
			if (DEBUG)
			{
				System.out.println("mousePressed (screenspace): " + e.getX() + " " + e.getY() );
				Point2D gp = transform( new Point2D.Double( e.getX(), e.getY() ));
				System.out.println("mousePressed (graph space): " + gp.getX() + " " + gp.getY() );
			}
			// find the circle that's picked, if any
			Point2D p = transform(e.getPoint());
			Vertex v = pickSupport.getVertex(p.getX(), p.getY());
			picked = v;
			previousSelectedVertex = (SwoopOntologyVertex) v;
			OntologyWithClassHierarchyRenderer rend = (OntologyWithClassHierarchyRenderer) renderer;

			// re-center at the point of double left click
			if (SwingUtilities.isLeftMouseButton(e) && (e.getClickCount() == 2)) {

				if (!rend.getIsDrawContent() || picked == null) {
					autoPanZoomTest();
					return;
				}
			}

			if (rend.getIsDrawContent()) {
				// if no vertex selected, we exit
				if (v == null) 
				{
					previousSelectedVertex = null;
					return;
				}

				// left click...
				if (SwingUtilities.isLeftMouseButton(e)) 
				{
					int shiftmask = MouseEvent.SHIFT_DOWN_MASK;
					if ((e.getModifiersEx() & shiftmask) == shiftmask) 
						handleRightClick(e, rend, p, v);
					else 
					{
						ClassTreeNode topNode = ((OntologyGraphNode) v.getUserDatum(OntologyWithClassHierarchyGraph.DATA)).getTreeNode();
						ClassTreeNode selectedNode = topNode.getSelectedChild(p);

						if (selectedNode == null)  // null selectedNode means user clicked on owl:Thing
							selectedNode = topNode;

						if (e.getClickCount() == 2) // zoom into the node
							panZoomToFitNode( selectedNode, topNode );
						
						myViewer.setSelectedNode( selectedNode );
					}
					pick(picked, true);
				}
				// right click...
				else if (SwingUtilities.isRightMouseButton(e)) {
					handleRightClick(e, rend, p, v);
				}
			}
			repaint();

		}

		private void handleRightClick(MouseEvent e,
				OntologyWithClassHierarchyRenderer rend, Point2D p, Vertex v) {
			ClassTreeNode selectedNode = null;
			if (rend.getIsDrawContent()) {
				ClassTreeNode topNode = ((OntologyGraphNode) v
						.getUserDatum(OntologyWithClassHierarchyGraph.DATA))
						.getTreeNode();
				selectedNode = topNode.getSelectedChild(p);
			}
			if (selectedNode == null)
				myGraph.showOntologyPopupMenu(e, v);
			else
				myGraph.showClassPopupMenu(e, selectedNode);
			rightSelectedNode = selectedNode;
		}

		public void mouseReleased(MouseEvent e) {
			if (picked == null)
				return;
			pick(picked, false);
			picked = null;
			repaint();
		}

		public void mouseDragged(MouseEvent e) {
			if (picked == null)
				return;
			Point2D p = transform(e.getPoint());

			layout.forceMove(picked, (int) p.getX(), (int) p.getY());
			repaint();
			//			drawSpot( e.getX(), e.getY() );
		}

		public void mouseMoved(MouseEvent e) {
			return;
		}

		public void mouseWheelMoved(MouseWheelEvent e) {
			return;
		}
	}

	/**
	 * @param paintable
	 *            The paintable to add.
	 */
	public void addPreRenderPaintable(Paintable paintable) {
		if (preRenderers == null) {
			preRenderers = new ArrayList();
		}
		preRenderers.add(paintable);
	}

	/**
	 * @param paintable
	 *            The paintable to remove.
	 */
	public void removePreRenderPaintable(Paintable paintable) {
		if (preRenderers != null) {
			preRenderers.remove(paintable);
		}
	}

	/**
	 * @param paintable
	 *            The paintable to add.
	 */
	public void addPostRenderPaintable(Paintable paintable) {
		if (postRenderers == null) {
			postRenderers = new ArrayList();
		}
		postRenderers.add(paintable);
	}

	/**
	 * @param paintable
	 *            The paintable to remove.
	 */
	public void removePostRenderPaintable(Paintable paintable) {
		if (postRenderers != null) {
			postRenderers.remove(paintable);
		}
	}

	/**
	 * Adds a <code>ChangeListener</code>.
	 * 
	 * @param l
	 *            the listener to be added
	 */
	public void addChangeListener(ChangeListener l) {
		listenerList.add(ChangeListener.class, l);
	}

	/**
	 * Removes a ChangeListener.
	 * 
	 * @param l
	 *            the listener to be removed
	 */
	public void removeChangeListener(ChangeListener l) {
		listenerList.remove(ChangeListener.class, l);
	}

	/**
	 * Returns an array of all the <code>ChangeListener</code> s added with
	 * addChangeListener().
	 * 
	 * @return all of the <code>ChangeListener</code> s added or an empty
	 *         array if no listeners have been added
	 */
	public ChangeListener[] getChangeListeners() {
		return (ChangeListener[]) (listenerList
				.getListeners(ChangeListener.class));
	}

	/**
	 * Notifies all listeners that have registered interest for notification on
	 * this event type. The event instance is lazily created.
	 * 
	 * @see EventListenerList
	 */
	protected void fireStateChanged() {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ChangeListener.class) {
				// Lazily create the event:
				if (changeEvent == null)
					changeEvent = new ChangeEvent(this);
				((ChangeListener) listeners[i + 1]).stateChanged(changeEvent);
			}
		}
	}

	/**
	 * @return Returns the pickedState.
	 */
	public PickedState getPickedState() {
		return pickedState;
	}

	/**
	 * @param pickedState
	 *            The pickedState to set.
	 */
	public void setPickedState(PickedState pickedState) {
		this.pickedState = pickedState;
		if (layout instanceof PickEventListener)
			pickedState.addListener((PickEventListener) layout);
	}

	/**
	 * @return Returns the pickSupport.
	 */
	public PickSupport getPickSupport() {
		return pickSupport;
	}

	/**
	 * @param pickSupport
	 *            The pickSupport to set.
	 */
	public void setPickSupport(PickSupport pickSupport) {
		this.pickSupport = pickSupport;
	}

	/**
	 * @return Returns the transform.
	 */
	public AffineTransform getTransform() {
		return transform;
	}
}
