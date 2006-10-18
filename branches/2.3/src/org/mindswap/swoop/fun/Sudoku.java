package org.mindswap.swoop.fun;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.mindswap.swoop.SwoopFrame;
import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.reasoner.PelletReasoner;
import org.mindswap.swoop.reasoner.SwoopToldReasoner;
import org.mindswap.swoop.renderer.entity.ConciseFormatEntityRenderer;
import org.mindswap.swoop.utils.SetUtils;
import org.mindswap.swoop.utils.owlapi.CorrectedRDFRenderer;
import org.mindswap.swoop.utils.ui.SwingWorker;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLClassAxiom;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owl.model.OWLDisjointClassesAxiom;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLSameIndividualsAxiom;
import org.semanticweb.owl.model.change.AddClassAxiom;
import org.semanticweb.owl.model.change.AddEntity;
import org.semanticweb.owl.model.change.AddIndividualAxiom;
import org.semanticweb.owl.model.change.AddIndividualClass;
import org.semanticweb.owl.model.change.ChangeVisitor;
import org.semanticweb.owl.model.helper.OWLBuilder;
import org.semanticweb.owl.model.helper.OntologyHelper;
import org.xngr.browser.editor.XmlEditorPane;

/**
 * Created: Jan 09, 2006
 * @author Aditya Kalyanpur
 *
 */
public class Sudoku extends JFrame implements ActionListener, MouseListener {

	// Core Puzzle Parameters:
	static String SUDOKU_NS = "http://sudoku.owl";
	int dimen = 9; // default
	int subdim = (int) Math.sqrt(dimen);
	boolean useIndividuals = true; // default - use individuals for grids in OWL ontology
	boolean changed = false; //TEMP FIXME
	
	// UI:
	Font tahoma = new Font("Tahoma", Font.PLAIN, 11);
	JButton solBtn, clearBtn;
	JTable table;
	OWLOntology gridOnt;
	JMenuItem loadMItem, saveMItem, viewMItem, swoopMItem, changeMItem, defaultMItem1, defaultMItem2;
	JCheckBoxMenuItem useIndMItem, useClaMItem;
	
	// Swoop-related stuff:
	SwoopModel swoopModel;
	SwoopFrame swoopHandler;
	
	// Constructor:
	public Sudoku(int gridDimensions) {
		init(gridDimensions);		
	}
	
	public Sudoku(SwoopModel model, SwoopFrame handler, int gridDimensions) {
		this.swoopModel = model;
		this.swoopHandler = handler;
		init(gridDimensions);
	}
	
	private void init(int gridDimensions) {
		this.dimen = gridDimensions;
		this.subdim = (int) Math.sqrt(dimen);
		this.setupUI();		
	}
	
	private void setupUI() {
		
		Container content = this.getContentPane();
		// grid table
		TableModel dataModel = new AbstractTableModel() {
			  Object[][] tableContents = new Object[dimen+1][dimen+1];
	          public int getColumnCount() { return dimen; }
	          public int getRowCount() { return dimen;}
	          public Object getValueAt(int row, int col) { return tableContents[row][col]; }
	          public boolean isCellEditable(int row, int col) { return true; }
	          public void setValueAt(Object val, int row, int col) {
	          	tableContents[row][col] = val;
	          }	          
	     };
	     
	     table = new JTable(dataModel);
	     table.setFont(tahoma);
	     table.addMouseListener(this);
	     JScrollPane scrollpane = new JScrollPane(table);
	     // top panel UI
	     JLabel lbl = new JLabel("Sudoku Puzzle Grid");
	     lbl.setFont(tahoma);
	     clearBtn = new JButton("Clear Grid");
	     clearBtn.setFont(tahoma);
	     clearBtn.addActionListener(this);
	     solBtn = new JButton("Solve Puzzle using Pellet");
	     solBtn.setFont(tahoma);
	     solBtn.addActionListener(this);
	     JToolBar btnPanel = new JToolBar();
	     btnPanel.add(clearBtn);
	     btnPanel.add(solBtn);
	     JLabel explLbl = new JLabel("  (Right-click on grid cell to see explanation)");
	     explLbl.setFont(tahoma);
	     btnPanel.add(explLbl);
	     JPanel topPane = new JPanel();
	     topPane.setLayout(new BorderLayout());
	     topPane.add(lbl, "North");
	     topPane.add(scrollpane, "Center");
	     topPane.add(btnPanel, "South");
	     
	     content.setLayout(new GridLayout(1,1));
	     content.add(topPane);
	     
	     // setup menu bar
	     JMenuBar mbar = new JMenuBar();
	     JMenu fileMenu = new JMenu("File");
	     mbar.add(fileMenu);
	     loadMItem = new JMenuItem("Load Puzzle (.owl)");
	     saveMItem = new JMenuItem("Save Puzzle (.owl)");
	     fileMenu.add(loadMItem);
	     fileMenu.add(saveMItem);
	     JMenu gridMenu = new JMenu("Grid");
	     changeMItem = new JMenuItem("Change Dimensions...");
	     changeMItem.addActionListener(this);
	     gridMenu.add(changeMItem);
	     mbar.add(gridMenu);
	     JMenu defaultMenu = new JMenu("Samples");
	     defaultMItem1 = new JMenuItem("Sample 4X4 Puzzle");
	     defaultMItem1.addActionListener(this);
	     defaultMItem2 = new JMenuItem("Sample 9X9 Puzzle");
	     defaultMItem2.addActionListener(this);
	     defaultMenu.add(defaultMItem1);
	     defaultMenu.add(defaultMItem2);
	     mbar.add(defaultMenu);
	     JMenu owlMenu = new JMenu("OWL");
	     mbar.add(owlMenu);
	     viewMItem = new JMenuItem("View Source");
	     JMenu repMenu = new JMenu("Represent Grid Cells Using...");
	     useIndMItem = new JCheckBoxMenuItem("Individuals");
	     useIndMItem.setSelected(true); // default
	     useIndMItem.addActionListener(this);
	     useClaMItem = new JCheckBoxMenuItem("Classes");
	     useClaMItem.setSelected(false); // default
	     useClaMItem.addActionListener(this);
	     repMenu.add(useIndMItem);
	     repMenu.add(useClaMItem);
	     swoopMItem = new JMenuItem("Transfer OWL Ontology to Swoop");
	     owlMenu.add(repMenu);
	     owlMenu.addSeparator();
	     owlMenu.add(viewMItem);
	     owlMenu.add(swoopMItem);
	     loadMItem.addActionListener(this);
	     saveMItem.addActionListener(this);
	     viewMItem.addActionListener(this);
	     swoopMItem.addActionListener(this);
	     this.setJMenuBar(mbar);
	     
	     // setup main frame UI
	     this.setSize(450, 300);
	     this.setLocation(300, 100);
	     this.show();
	     this.setTitle("Sudoku using OWL");	     
	}
	
	public String getRDFXMLGridOnt() {
		String rdf = "";
		try {
			CorrectedRDFRenderer rdfRend = new CorrectedRDFRenderer();
			StringWriter st = new StringWriter();
			rdfRend.renderOntology(gridOnt, st);
			rdf = st.toString();
		}
		catch (OWLException ex) {
			ex.printStackTrace();
		}
		return rdf;
	}
	
	public void saveGridOntology() {
		try {
			FileWriter fw = new FileWriter(new File("sudoku.owl"));
			fw.write(this.getRDFXMLGridOnt());
			fw.close();
			System.out.println("Saved grid to file: sudoku.owl");
			JOptionPane.showMessageDialog(this, "Saved grid to file: sudoku.owl", "Saving Sudoko OWL Ontology", JOptionPane.INFORMATION_MESSAGE);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void loadGridOntologyToSwoop() {
		try {
			this.generatePopulatedSudokuOWLGrid();
			swoopModel.addOntology(gridOnt);
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setGridValue(String ind1, String ind2) {
		try {
			Set entSet = new HashSet();
			if (this.useIndividuals) {
				entSet.add(gridOnt.getIndividual(new URI(SUDOKU_NS+ind1)));
				entSet.add(gridOnt.getIndividual(new URI(SUDOKU_NS+ind2)));
			}
			else {
				entSet.add(gridOnt.getClass(new URI(SUDOKU_NS+ind1)));
				entSet.add(gridOnt.getClass(new URI(SUDOKU_NS+ind2)));
			}
			this.makeEqual(entSet);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void generatePopulatedSudokuOWLGrid() {
		
		// set useIndividuals based on user selection
		this.useIndividuals = this.useIndMItem.isSelected();
		
		// reset gridOnt
		this.generateEmptySudokuOWLGrid();
		
		// set individual values in gridOnt based on user inputs in table UI
		TableModel tm = table.getModel();
		for (int row = 1; row<=dimen; row++) {
			for (int col = 1; col<=dimen; col++) {
				if (tm.getValueAt(row-1, col-1)!=null) {
					String valStr = tm.getValueAt(row-1, col-1).toString();
					if (!valStr.equals("")) {
						valStr = "#"+valStr;
						// map (row, col) to (g, i, j)
						int g = ((row-1) / subdim) * subdim + 1 + ((int) (col-1) / subdim);
						int i = row % subdim;
						if (i==0) i = subdim;
						int j = col % subdim;
						if (j==0) j = subdim;
						String cellValue = "";
						if (this.useIndividuals) cellValue = "#V"+String.valueOf(g)+"_"+String.valueOf(i)+String.valueOf(j);
						else cellValue = "#C"+String.valueOf(g)+"_"+String.valueOf(i)+String.valueOf(j);
//						System.out.println("("+row+","+col+") translates to "+cellValue);
						this.setGridValue(cellValue, valStr);
					}
				}
			}
		}		
	}
	
	public OWLOntology generateEmptySudokuOWLGrid() {
		
		System.out.println("Generating sudoku grid of dimensions "+dimen+" X "+dimen+", with "+dimen +" sub-grids of dimension "+subdim+" X "+subdim);
		
		gridOnt = null;
		try {
			
			// initialize ontology
			OWLBuilder builder = new OWLBuilder();
			builder.createOntology(new URI(SUDOKU_NS), new URI(SUDOKU_NS));
			gridOnt = builder.getOntology();
			OWLDataFactory gridDF = gridOnt.getOWLDataFactory();
			
			// setup game grid
			// first init grid table array
			OWLObject[][][] grid = new OWLObject[dimen+1][subdim+1][subdim+1];
			Set gridEntities = new HashSet();
			// now create individuals (/classes) for each grid cell
			for (int g=1; g<=dimen; g++) {
				for (int i=1; i<=subdim; i++) {
					for (int j=1; j<=subdim; j++) {
						String gridCell = "";
						OWLEntity ent = null;
						if (this.useIndividuals) {
							gridCell = SUDOKU_NS+"#V"+ String.valueOf(g) + "_" + String.valueOf(i) + String.valueOf(j);
							ent = gridDF.getOWLIndividual(new URI(gridCell));
						}
						else {
							gridCell = SUDOKU_NS+"#C"+ String.valueOf(g) + "_" + String.valueOf(i) + String.valueOf(j);
							ent = gridDF.getOWLClass(new URI(gridCell));
						}
						
						AddEntity ae = new AddEntity(gridOnt, ent, null);
						ae.accept((ChangeVisitor) gridOnt);
						
						//*** add corresponding elements to grid sets ***
						grid[g][i][j] = ent;
						gridEntities.add(ent);
						
						if (this.useIndividuals) {
							// create dummy classes to see results in pellet online demo
							OWLClass cla = gridDF.getOWLClass(new URI(SUDOKU_NS+"#C"+ String.valueOf(g) + "_" + String.valueOf(i) + String.valueOf(j)));
							ae = new AddEntity(gridOnt, cla, null);
							ae.accept((ChangeVisitor) gridOnt);
							AddIndividualClass ai = new AddIndividualClass(gridOnt, (OWLIndividual) ent, cla, null);
							ai.accept((ChangeVisitor) gridOnt);
						}
						else {
							// create dummy individuals for each class
							OWLIndividual ind = gridDF.getOWLIndividual(new URI(SUDOKU_NS+"#V"+ String.valueOf(g) + "_" + String.valueOf(i) + String.valueOf(j)));
							ae = new AddEntity(gridOnt, ind, null);
							ae.accept((ChangeVisitor) gridOnt);
							AddIndividualClass ai = new AddIndividualClass(gridOnt, ind, (OWLClass) ent, null);
							ai.accept((ChangeVisitor) gridOnt);
						}
					}
				}
			}
			
			// create value individuals(/classes)
			Set valSet = new HashSet();
			for (int i=1; i<=dimen; i++) {
				String gridValue = SUDOKU_NS+"#"+String.valueOf(i);
				OWLEntity val;
				if (this.useIndividuals) val = gridDF.getOWLIndividual(new URI(gridValue));
				else val = gridDF.getOWLClass(new URI(gridValue));
				valSet.add(val);
				AddEntity ae = new AddEntity(gridOnt, val, null);
				ae.accept((ChangeVisitor) gridOnt);
			}
			// make all values different(/disjoint)
			this.makeDifferent(valSet);
			
			// make owl:Thing a subclass of oneOf(1,2,..param) (/OR each class equivalent to unionOf(1,2..param)
			OWLDescription topConstraint = null;
			if (this.useIndividuals) {
				topConstraint = gridDF.getOWLEnumeration(valSet);				
			}
			else {
				topConstraint = gridDF.getOWLOr(valSet);
			}
			OWLClassAxiom cax = gridDF.getOWLSubClassAxiom(gridDF.getOWLThing(), topConstraint);
			AddClassAxiom ac = new AddClassAxiom(gridOnt, cax, null);
			ac.accept((ChangeVisitor) gridOnt);
			
			// enforce constraints of sudoku puzzle
			// make all sub-grid values distinct
			for (int g=1; g<=dimen; g++) {
				Set subGrid = new HashSet();
				for (int i=1; i<=subdim; i++) {
					for (int j=1; j<=subdim; j++) {
						subGrid.add(grid[g][i][j]);						
					}
				}
				this.makeDifferent(subGrid);
			}
			// make all grid-row values distinct
			for (int g=1; g<=dimen; g+=subdim) {
				for (int row=1; row<=subdim; row++) {
					Set gridRow = new HashSet();
					for (int sg=g; sg<=(g+subdim-1); sg++) {
						for (int col=1; col<=subdim; col++) {
							gridRow.add(grid[sg][row][col]);							
						}
					}
					this.makeDifferent(gridRow);					
				}				
			}
			// make all grid-column values distinct
			for (int g=1; g<=subdim; g++) {
				for (int col=1; col<=subdim; col++) {
					Set gridCol = new HashSet();
					for (int sg=g; sg<=dimen; sg+=subdim) {
						for (int row=1; row<=subdim; row++) {
							gridCol.add(grid[sg][row][col]);							
						}
					}
					this.makeDifferent(gridCol);					
				}				
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		return gridOnt;
	}
	
	private void makeDifferent(Set diff) {
		try {
			OWLDataFactory gridDF = gridOnt.getOWLDataFactory();
			if (this.useIndividuals) {
				OWLDifferentIndividualsAxiom dif = gridDF.getOWLDifferentIndividualsAxiom(diff);
				AddIndividualAxiom ai = new AddIndividualAxiom(gridOnt, dif, null);
				ai.accept((ChangeVisitor) gridOnt);
			}
			else {
				OWLDisjointClassesAxiom dis = gridDF.getOWLDisjointClassesAxiom(diff);
				AddClassAxiom ai = new AddClassAxiom(gridOnt, dis, null);
				ai.accept((ChangeVisitor) gridOnt);
			}
		}
		catch (OWLException ex) {
			ex.printStackTrace();
		}
	}
	
	private void makeEqual(Set equ) {
		try {
			OWLDataFactory gridDF = gridOnt.getOWLDataFactory();
			if (this.useIndividuals) {
				OWLSameIndividualsAxiom sam = gridDF.getOWLSameIndividualsAxiom(equ);
				AddIndividualAxiom ai = new AddIndividualAxiom(gridOnt, sam, null);
				ai.accept((ChangeVisitor) gridOnt);
			}
			else {
				OWLEquivalentClassesAxiom eq = gridDF.getOWLEquivalentClassesAxiom(equ);
				AddClassAxiom ai = new AddClassAxiom(gridOnt, eq, null);
				ai.accept((ChangeVisitor) gridOnt);
			}
		}
		catch (OWLException ex) {
			ex.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		
		Sudoku sud = new Sudoku(9);
	}

	public void actionPerformed(ActionEvent e) {
		
		try {
			if (e.getSource() == defaultMItem1) {
				// load sample 4X4 puzzle
				try {
					gridOnt = swoopModel.loadOntology(new URI("http://www.mindswap.org/~aditkal/sudoku4X4.owl"));
					this.parseOWLOntToGrid(gridOnt);
					
				}
				catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			else
			if (e.getSource() == defaultMItem2) {
				// load sample 9X9 puzzle
				try {
					gridOnt = swoopModel.loadOntology(new URI("http://www.mindswap.org/~aditkal/sudoku9X9.owl"));
					this.parseOWLOntToGrid(gridOnt);
					
				}
				catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			else
			if (e.getSource() == loadMItem) {
				this.loadOWLGrid();
			}
			else
			if (e.getSource() == saveMItem) {
				this.saveOWLGrid();
			}
			else
			if (e.getSource() == useIndMItem) {
				useClaMItem.setSelected(!useIndMItem.isSelected());
			}
			else
			if (e.getSource() == useClaMItem) {
				useIndMItem.setSelected(!useClaMItem.isSelected());
			}
			else
			if (e.getSource() == viewMItem) {
				viewSource();
			}
			else
		    if (e.getSource() == solBtn) 
				solveSudoku();			
			else 
			if (e.getSource() == clearBtn)
				clearTable();
			else
			if (e.getSource() == swoopMItem)
//				saveGridOntology();
				this.loadGridOntologyToSwoop();
			else if (e.getSource() == changeMItem) 
				changeDimensions();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void clearTable() {
		for (int i = 0; i<table.getModel().getRowCount(); i++)
			for (int j = 0; j<table.getModel().getRowCount(); j++)
				table.setValueAt("", i, j);
		table.updateUI();
	}
	
	private void viewSource() {
		// generate and view source of OWL
		this.generatePopulatedSudokuOWLGrid();
		JFrame popup = new JFrame("View Source");
		Container content  = popup.getContentPane();
		content.setLayout(new GridLayout(1,1));
		XmlEditorPane xmlPane = new XmlEditorPane();
		xmlPane.setEditable(false);
		xmlPane.setContentType("text/xml");
		xmlPane.setText(this.getRDFXMLGridOnt());
		xmlPane.setCaretPosition(0);
		content.add(new JScrollPane(xmlPane));
		popup.setSize(600, 400);
		popup.setLocation(200, 200);
		popup.show();
	}
	
	private void solveSudoku() {
		
		// generate gridOnt OWL ontology based on puzzle UI
		this.generatePopulatedSudokuOWLGrid();
		
		// use Pellet to get solution to puzzle
		SwingWorker worker = new SwingWorker() {
			PelletReasoner pellet = new PelletReasoner();
			boolean fail = false;
			
			public Object construct() {
				try {
					pellet.setOntology(gridOnt);
				} 
				catch (Exception ex) {
					fail = true;
					if( ex != null )
					    throw new RuntimeException(ex.getMessage());
					else
					    throw new RuntimeException( "Unexpected error" );
				}	
				return null;
			}
			public void finished() {
				if (fail) {											
				}
				else {
					// pellet has got answers
					try {
						if (pellet.isConsistent()) {
							boolean incomplete = false;
							for (int g=1; g<=dimen; g++) {
								for (int i=1; i<=subdim; i++) {
									for (int j=1; j<=subdim; j++) {
										OWLEntity ent = null;
										if (useIndividuals) ent = gridOnt.getClass(new URI(SUDOKU_NS+"#C"+ String.valueOf(g) + "_" + String.valueOf(i) + String.valueOf(j)));
										else ent = gridOnt.getIndividual(new URI(SUDOKU_NS+"#V"+ String.valueOf(g) + "_" + String.valueOf(i) + String.valueOf(j)));
										Set equ = new HashSet();
										if (useIndividuals) equ = pellet.allInstancesOf((OWLClass) ent);
										else {
//											if (!pellet.isConsistent(cla)) {
//												JOptionPane.showMessageDialog(null, "Invalid Data: Puzzle Constraints Violated", "Error", JOptionPane.ERROR_MESSAGE);
//												return;
//											}
											equ = SetUtils.union(pellet.allTypesOf((OWLIndividual) ent)); 											
										}
										
										String val = "";
										for (Iterator iter = equ.iterator(); iter.hasNext();) {
											OWLEntity eq = (OWLEntity) iter.next();
											if (eq.getURI().toString().indexOf("#V")==-1 && eq.getURI().toString().indexOf("#C")==-1 && eq.getURI().toString().indexOf("#T")==-1) {
												val = eq.getURI().toString();
												val = val.substring(val.indexOf("#")+1, val.length());
												break;
											}												
										}
										// translate g,i,j to (row, col)
										int row = ((g - 1) / subdim) * subdim + i;
										int c = g % subdim;
										if (c==0) c = subdim;
										int col = (c-1) * subdim + j;
//											System.out.println(g+"_"+String.valueOf(i)+String.valueOf(j)+" translates to "+"("+row+","+col+")");
										if (table.getValueAt(row-1, col-1) == null || table.getValueAt(row-1, col-1).toString().equals("") && !val.equals("")) changed = true;
										table.setValueAt(val, row-1, col-1);
										if (val.equals("")) incomplete = true;
										
										table.updateUI();
									}
								}								
							}
							//FIXME: iteratively solve sudoku if using classes?!
//							if (!useIndividuals && incomplete && changed) {
//								changed = false;
//								generatePopulatedSudokuOWLGrid();
//								solveSudoku();
//							}
						}
						else {
							JOptionPane.showMessageDialog(null, "Invalid Data: Puzzle Constraints Violated", "Error", JOptionPane.ERROR_MESSAGE);
						}
					}
					catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		};				
	    worker.start();
	}
	
	private void loadOWLGrid() {
		try {
			// prompt user for file
			JFileChooser wrapChooser = new JFileChooser();
//			wrapChooser.addChoosableFileFilter();
			File openFile = null;
			int returnVal = wrapChooser.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				openFile = wrapChooser.getSelectedFile();
			} else { // open cancelled
					return;
			}
			// open file
			FileInputStream in = new FileInputStream(openFile);
			InputStreamReader reader = new InputStreamReader(in);
			URI uri = new URI(openFile.getName());
			gridOnt = swoopModel.loadOntologyInRDF(reader, uri);
			this.parseOWLOntToGrid(gridOnt);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void parseOWLOntToGrid(OWLOntology gridOnt) {
		
		try {
			
			// first get used classes or individuals
			Set uris = OntologyHelper.allURIs(gridOnt);
			boolean usedInd = false;
			for (Iterator iter = uris.iterator(); iter.hasNext();) {
				URI u = (URI) iter.next();
				if (u.toString().indexOf("#V")>=0) {
					usedInd = true;
					break;
				}
			}
			this.useIndividuals = usedInd;
			this.useIndMItem.setSelected(usedInd);
			this.useClaMItem.setSelected(!usedInd);
			
			// second get grid dimensions
			int num = 0;
			for (int i=10; i>0; i--) {
				String val = "#"+String.valueOf((int) Math.pow(i,2));
				if (this.useIndividuals && gridOnt.getIndividual(new URI(SUDOKU_NS+val))!=null) {
					num = (int) Math.pow(i,2);
					break;
				}
				if (!this.useIndividuals && gridOnt.getClass(new URI(SUDOKU_NS+val))!=null) {
					num = (int) Math.pow(i,2);
					break;
				}
			}
			if (dimen!=num) {
				this.dimen = num;
				this.subdim = (int) Math.sqrt(dimen);
				this.getContentPane().removeAll();
				this.setupUI();
				this.repaint();
			}
			
			// populate table
			SwoopToldReasoner reas = new SwoopToldReasoner();
			reas.setOntology(gridOnt);
			for (int g=1; g<=dimen; g++) {
				for (int i=1; i<=subdim; i++) {
					for (int j=1; j<=subdim; j++) {
						OWLEntity ent = null;
						String val = "";
						if (this.useIndividuals) {
							String cell = "#V"+String.valueOf(g)+"_"+String.valueOf(i)+String.valueOf(j);
							ent = gridOnt.getIndividual(new URI(SUDOKU_NS+cell));							
						}
						else {
							String cell = "#C"+String.valueOf(g)+"_"+String.valueOf(i)+String.valueOf(j);
							ent = gridOnt.getClass(new URI(SUDOKU_NS+cell));
						}
						if (ent==null) {
							JOptionPane.showMessageDialog(this, "Error: Invalid Grid Dimensions", "Load Error", JOptionPane.ERROR_MESSAGE);
							return;
						}
						else {
							// get value
							Set equ = new HashSet();
							if (this.useIndividuals) equ = reas.getSameAsIndividuals((OWLIndividual) ent);
							else equ = reas.equivalentClassesOf((OWLClass) ent);
							for (Iterator iter = equ.iterator(); iter.hasNext();) {
								OWLEntity eq = (OWLEntity) iter.next();
								if (eq.getURI().toString().indexOf("#V")==-1 && eq.getURI().toString().indexOf("#C")==-1 && eq.getURI().toString().indexOf("#T")==-1) {
									val = eq.getURI().toString();
									val = val.substring(val.indexOf("#")+1, val.length());
									break;
								}												
							}							
						}
						// map g,i,j to row,col
						int row = ((g - 1) / subdim) * subdim + i;
						int c = g % subdim;
						if (c==0) c = subdim;
						int col = (c-1) * subdim + j;
						table.setValueAt(val, row-1, col-1);						
					}
				}
			}
			table.updateUI();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void saveOWLGrid() {
		try {
			JFileChooser wrapChooser = new JFileChooser();
			File saveFile = null;
			int returnVal = wrapChooser.showSaveDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				saveFile = wrapChooser.getSelectedFile();
			} else { // save cancelled
				return;
			}
			
			// prompt user if overwriting
			if (saveFile.exists()) {
				int result = JOptionPane.showConfirmDialog(this, "Saving File at " + saveFile.getAbsolutePath() + ". Overwrite?", "Save Ontology", JOptionPane.YES_NO_OPTION);
				if (result != JOptionPane.YES_OPTION) return;
			}
			
			// write ontology file
			FileWriter fw = new FileWriter(saveFile);
			this.generatePopulatedSudokuOWLGrid();
			fw.write(this.getRDFXMLGridOnt());
			fw.close();
			System.out.println("Saved grid to file: "+saveFile.getName());
			JOptionPane.showMessageDialog(this, "Saved grid to file: "+saveFile.getName(), "Saving Sudoko OWL Ontology", JOptionPane.INFORMATION_MESSAGE);			
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void popupExplanation(int row, int col) {
		try {
			// translate dimensions to to g, i, j
			int g = ((row-1) / subdim) * subdim + 1 + ((int) (col-1) / subdim);
			int i = row % subdim;
			if (i==0) i = subdim;
			int j = col % subdim;
			if (j==0) j = subdim;
			String val = table.getValueAt(row-1, col-1).toString();
			
			if (!val.equals("") && this.useIndividuals) {
				
				// Cg_ij has instance value
				
				// get explanation
				ConciseFormatEntityRenderer cfRend = new ConciseFormatEntityRenderer(); // swoopHandler.termDisplay.renderers.get(0);
				cfRend.setSwoopModel(swoopModel);
				cfRend.visitor = cfRend.createVisitor();
				OWLIndividual ind = gridOnt.getIndividual(new URI(SUDOKU_NS+"#"+val));
				OWLClass cla = gridOnt.getClass(new URI(SUDOKU_NS+"#C"+String.valueOf(g)+"_"+String.valueOf(i)+String.valueOf(j)));
				String expl = cfRend.getSudokuExplanation(gridOnt, ind, cla);
				String nlExpl = "<html><body><font face=\"Verdana\" size=2>";
				nlExpl += "<b>Cell ("+row+","+col+")</b> has value <b>"+val+"</b> because...<br><hr><font face=\"Verdana\" size=2>";
				nlExpl += "Each cell must have <b>one of</b> the values [1..."+String.valueOf(dimen)+"] <i>[constraint]</i><br>";
				List nlList = new ArrayList();
				// parse explanation to add NL
				String[] st = expl.split("<br>");
				for (int s=0; s<st.length; s++) {
					String line = st[s];
					String nl = "";
					
//					System.out.println(line);
					
					if (line.indexOf("SameIndividual")>=0) {
						String token = "\">";
						int pos = line.indexOf(token)+2;
						String val1 = line.substring(pos, line.indexOf("<", pos));
						int pos2 = line.indexOf(token, pos)+2;
						String val2 = line.substring(pos2, line.indexOf("<", pos2));
						if (val2.indexOf("V")>=0) {
							String temp = val1;
							val1 = val2;
							val2 = temp;
						}
						String gs = val1.substring(1,2);
						String is = val1.substring(3,4);
						String js = val1.substring(4,5);
						g = Integer.parseInt(gs);
						i = Integer.parseInt(is);
						j = Integer.parseInt(js);
						row = ((g - 1) / subdim) * subdim + i;
						int c = g % subdim;
						if (c==0) c = subdim;
						col = (c-1) * subdim + j;
						nl = "<b>Cell ("+row+","+col+")</b> has value "+val2 + " <i>[given]</i>";						
					}
					else {
						// check grid
						for (g=1; g<=dimen; g++) {
							boolean match = true;
							for (i=1; i<=subdim; i++) {
								for (j=1; j<=subdim; j++) {
									String cell = "V"+String.valueOf(g)+"_"+String.valueOf(i)+String.valueOf(j);
									if (line.indexOf(cell)==-1) {
										match = false;
										break;
									}
								}
							}
							if (match) {
								nl = "<b>Grid "+String.valueOf(g)+"</b> must have all values different <i>[constraint]</i>";
								break;
							}
						}
						if (nl.equals("")) {
							// check row
							for (g=1; g<=dimen; g+=subdim) {
								for (i=1; i<=subdim; i++) {
									boolean match = true;
									for (int sg=g; sg<=(g+subdim-1); sg++) {
										for (j=1; j<=subdim; j++) {
											String cell = "V"+String.valueOf(g)+"_"+String.valueOf(i)+String.valueOf(j);
											if (line.indexOf(cell)==-1) {
												match = false;
												break;
											}							
										}
									}
									if (match) {
										row = ((g - 1) / subdim) * subdim + i;
										nl = "<b>Row "+row+"</b> must have all values different <i>[constraint]</i>";
										break;
									}
								}				
							}
						}
						if (nl.equals("")) {
							// check columns
							for (g=1; g<=subdim; g++) {
								for (j=1; j<=subdim; j++) {
									boolean match = true;
									for (int sg=g; sg<=dimen; sg+=subdim) {
										for (i=1; i<=subdim; i++) {
											String cell = "V"+String.valueOf(g)+"_"+String.valueOf(i)+String.valueOf(j);
											if (line.indexOf(cell)==-1) {
												match = false;
												break;
											}							
										}
									}
									if (match) {
										int c = g % subdim;
										if (c==0) c = subdim;
										col = (c-1) * subdim + j;
										nl = "<b>Column "+col+"</b> must have all values different <i>[constraint]</i>";
										break;
									}
								}				
							}
						}
					}					
//					System.out.println("--->"+nl);
					if (!nl.equals("")) nlList.add(nl); //nlExpl += nl+"<br>"; 					
				}
				
				// post process to *order* NL
				while (!nlList.isEmpty()) {
					boolean findVal = false;
					String valLine = "";
					for (Iterator iter = nlList.iterator(); iter.hasNext();) {
						String line = (String) iter.next();
						if (line.indexOf("has value")>=0) {
							findVal = true;
							valLine = line;
							break;
						}
					}
					if (findVal) {
						nlList.remove(valLine);
						// add findVal to output
						nlExpl += valLine + "<br>";
						String rs = valLine.substring(valLine.indexOf("(")+1, valLine.indexOf(","));
						String cs = valLine.substring(valLine.indexOf(",")+1, valLine.indexOf(")"));
						row = Integer.parseInt(rs);
						col = Integer.parseInt(cs);
						
						// add all grid/row/col constraints after this line
						for (Iterator iter = new ArrayList(nlList).iterator(); iter.hasNext();) {
							String line = (String) iter.next();
							boolean match = false;
							if (line.indexOf("Grid")>=0) {
								int pos = line.indexOf("Grid")+5;
								String gs = line.substring(pos, line.indexOf("</b>")).trim();
								g = Integer.parseInt(gs);
								// check if row,col appear in g
								if (g == ((row-1) / subdim) * subdim + 1 + ((int) (col-1) / subdim)) match = true;
							}
							else if (line.indexOf("Row")>=0) {
								int pos = line.indexOf("Row")+4;
								String is = line.substring(pos, line.indexOf("</b>")).trim();
								i = Integer.parseInt(is);
								// check if i matches
								if (i==row) match = true;
							}
							else if (line.indexOf("Column")>=0) {
								int pos = line.indexOf("Column")+7;
								String js = line.substring(pos, line.indexOf("</b>")).trim();
								j = Integer.parseInt(js);
								// check if j matches 
								if (col==j) match = true;
							}
							if (match) {
								nlExpl += "|_" + line + "<br>";
								nlList.remove(line);
							}
						}
					}
					else {
						// flush remaining constraints
						for (Iterator iter = nlList.iterator(); iter.hasNext();) {
							String line = (String) iter.next();
							nlExpl += line + "<br>";
						}
						nlList.clear();
					}
				}
				
				nlExpl += "</body></html>";
				
				// display explanation in popup
				JFrame popup = new JFrame("Explanation");
				Container content  = popup.getContentPane();
				content.setLayout(new GridLayout(1,1));
				JEditorPane htmlPane = new JEditorPane();
				htmlPane.setEditable(false);
				htmlPane.setContentType("text/html");
				htmlPane.setText(nlExpl);
				htmlPane.setCaretPosition(0);
				content.add(new JScrollPane(htmlPane));
				popup.setSize(400, 200);
				popup.setLocation(300, 300);
				popup.show();
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void mouseClicked(MouseEvent e) {
		if (e.getSource() == table) {			
			if (e.getButton() == MouseEvent.BUTTON3) {
				// popup menu on right click
				int row = table.rowAtPoint(e.getPoint());
				int col = table.columnAtPoint(e.getPoint());
				this.popupExplanation(row+1, col+1);
			}
		}
	}
	public void mousePressed(MouseEvent arg0) {
	}
	public void mouseReleased(MouseEvent arg0) {
	}
	public void mouseEntered(MouseEvent arg0) {
	}
	public void mouseExited(MouseEvent arg0) {
	}
	
	private void changeDimensions() {
		String result = JOptionPane.showInputDialog(this, "Specify New Dimensions", "Puzzle Grid", JOptionPane.INFORMATION_MESSAGE);
		if (result!=null) {
			int num = Integer.parseInt(result);
			if (Math.sqrt(num) != ((int) Math.sqrt(num))) {
				JOptionPane.showMessageDialog(this, "Error: Invalid Grid Dimensions - Number needs to be a perfect square", "Dimension Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			this.dimen = num;
			this.subdim = (int) Math.sqrt(dimen);
			this.getContentPane().removeAll();
			this.setupUI();
			this.repaint();
		}
	}
}
