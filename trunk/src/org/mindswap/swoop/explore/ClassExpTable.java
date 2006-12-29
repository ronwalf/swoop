package org.mindswap.swoop.explore;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Panel;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.utils.graph.hierarchy.popup.ClassAxiomContainer;
import org.mindswap.swoop.utils.graph.hierarchy.popup.ConcisePlainVisitor;
import org.semanticweb.owl.impl.model.OWLClassImpl;
import org.semanticweb.owl.impl.model.OWLObjectImpl;
import org.semanticweb.owl.io.ShortFormProvider;
import org.semanticweb.owl.model.OWLClassAxiom;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLObject;

public class ClassExpTable extends JFrame implements WindowListener, ComponentListener, MouseListener
{
	
	class ClassExpRenderer extends JLabel implements TableCellRenderer 
	{
		private ConcisePlainVisitor myVisitor;
		public ClassExpRenderer( ConcisePlainVisitor visitor ) 
		{ 
			this.setOpaque( true );
			myVisitor = visitor; 
		}

		public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) 
		{
			try
			{
				// managing colors
				if ( isSelected )
				{
					this.setBackground( table.getSelectionBackground() );
					this.setForeground( table.getSelectionForeground() );
				}
				else
				{
					this.setBackground( table.getSelectionForeground() );
					this.setForeground( Color.BLACK );
				}
				
				OWLDescription desc = (OWLDescription)value;
				desc.accept( myVisitor );
				String str = myVisitor.result();
				myVisitor.reset();
				
				this.setText( str  );
				//System.out.println( this.getText() );
			}
			catch ( Exception e )
			{ e.printStackTrace(); }
			
			return this;
		}
	}
	
	class RichHeaderRenderer extends JPanel implements TableCellRenderer 
	{
		private ConcisePlainVisitor myVisitor;
		private JLabel myLabel = null;
		private int myColNum;
		private boolean isInit = false;
		
		public RichHeaderRenderer() 
		{ 
			super();
			setOpaque(true);
		}

		public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) 
		{				
			if ( !isInit )
				init( table, value, column );
			return this;
		}
		
		private void init( JTable table, Object value, int column )
		{
			isInit = true;
			myLabel = new JLabel( value.toString() );
			this.add( myLabel );
			myColNum = column;
			this.setPreferredSize( new Dimension( 200, 50 ));
			//this.setBackground( Color.LIGHT_GRAY );
		}
		
		public void paint( Graphics g)
		{
			super.paint(g);
			Graphics2D g2d = (Graphics2D)g;
			g2d.setColor( Color.BLACK );
			
			g2d.drawLine( 0, 0, this.getWidth(), 0 );  // top
			g2d.drawLine( 0, this.getHeight()-1, this.getWidth(), this.getHeight()-1 ); // bot			
			g2d.drawLine( this.getWidth()-1, 0, this.getWidth()-1, this.getHeight() ); // right
			if ( myColNum == 0 )
				g2d.drawLine( 0, 0, 0, this.getHeight() ); // left
		}
	}
	
	
	class ClassExpTableModel extends AbstractTableModel
	{
	    private String[] myColNames = null;
	    private Object[][] myData = null;

	    public ClassExpTableModel( String [] colNames, Object[][] data )
	    {
	    	myColNames = colNames;
	    	myData = data;
	    }
	    
	    public int getColumnCount() 
	    { return myColNames.length; }

	    public int getRowCount() 
	    { return myData.length; }

	    public String getColumnName(int col) 
	    { return myColNames[col]; }

	    public Object getValueAt(int row, int col) 
	    { return myData[row][col]; }

	    public Class getColumnClass(int c) 
	    {
	    	//System.err.println("column class: " + c);
	    	return getValueAt(0, c).getClass(); 
	    }

	    public boolean isCellEditable(int row, int col) 
	    { return false; }

	    public void setValueAt(Object value, int row, int col) 
	    { }
	}
	
	private JTable myTable;
	private ClassExpTableModel myDataModel;
	private TableSorter myManipModel;

	private AxiomIndexer myIndexer;
	
	public ClassExpTable( AxiomIndexer indexer, ConcisePlainVisitor visitor, HashedCounts classExpCounts, Hashtable classExpDepths )
	{
		this.myIndexer = indexer;
		String [] colNames = {"Class Expression", "#Occurences", "Depth", "Score"};
		
		Object [][] data = new Object[classExpCounts.keySet().size() ][4];
		
		int i = 0;
		for ( Iterator it = classExpCounts.keySet().iterator(); it.hasNext(); )
		{
			OWLDescription desc = (OWLDescription)it.next();
			if ( desc == null )
				System.out.println("description is null at " + i);
			Integer count = new Integer( classExpCounts.getCount( desc ));
			Integer depth = (Integer)classExpDepths.get( desc );
			try
			{
				if ( depth == null )
				{
					System.out.println(" depth is null at " + i);
					desc.accept( visitor );
					String str = visitor.result();
					System.out.println("        [" + str + "]");
					visitor.reset();
				}
			}
			catch ( Exception e )
			{ e.printStackTrace(); }
				
			data[i][0] = desc;
			data[i][1] = count;
			data[i][2] = depth;
			data[i][3] = new Double( Math.pow( count.intValue(), (depth.intValue() + 1) ));
			i++;
		}
		myDataModel = new ClassExpTableModel(colNames , data);
		myManipModel = new TableSorter( myDataModel );
		myTable = new JTable( myManipModel );
		myManipModel.setTableHeader( myTable.getTableHeader() );
			
			
			/*
			for ( int vColIndex = 0; vColIndex < 3; vColIndex++ )
			{
				TableColumn col = myTable.getColumnModel().getColumn(vColIndex);
				col.setHeaderRenderer(new RichHeaderRenderer());
			}
			*/
			
		myTable.setDefaultRenderer( OWLObjectImpl.class, new ClassExpRenderer(visitor));
		
		setupUI();
		
		this.addWindowListener( this );
		this.addComponentListener( this );
		myTable.addMouseListener( this );
		this.setSize( 300, 600 );
		this.setVisible( true );
	}
	
	private void setupUI()
	{
		JScrollPane scrollPane = new JScrollPane( myTable );
		myTable.setPreferredScrollableViewportSize(new Dimension(500, 300));

		this.add( scrollPane, BorderLayout.CENTER );
	}
	

	public void windowOpened(WindowEvent e) {}

	public void windowClosing(WindowEvent e) { this.setVisible( false ); this.dispose(); }

	public void windowClosed(WindowEvent e) {}

	public void windowIconified(WindowEvent e) {}

	public void windowDeiconified(WindowEvent e) {}

	public void windowActivated(WindowEvent e) {}

	public void windowDeactivated(WindowEvent e) {}
	

	
	public void componentResized(ComponentEvent e) {}

	public void componentMoved(ComponentEvent e) {}

	public void componentShown(ComponentEvent e) {}

	public void componentHidden(ComponentEvent e) {}

	
	
	public void mouseClicked(MouseEvent e) 
	{
		
		if (e.getClickCount() == 2) 
		{
			if ( e.getSource() instanceof JTable )
			{
				JTable table = (JTable)e.getSource();
				int row = table.getSelectedRow();
				OWLDescription desc = (OWLDescription)table.getValueAt( row, 0 );			
				Vector axioms = (Vector)myIndexer.classExpToAxioms.get( desc );
				
				Vector axiomContainers = new Vector();
				for ( Iterator it = axioms.iterator(); it.hasNext(); )
				{
					OWLClassAxiom axi = (OWLClassAxiom)it.next();
					ClassAxiomContainer container = new ClassAxiomContainer( axi, myIndexer.myShortForms, myIndexer.myModel );
					axiomContainers.add( container );
				}
				AxiomList view = new AxiomList( "Axioms where selected expression occurs in", axiomContainers );
			}
		}
		
	}

	public void mousePressed(MouseEvent e) {}

	public void mouseReleased(MouseEvent e) {}

	public void mouseEntered(MouseEvent e) {}

	public void mouseExited(MouseEvent e) {}
}
