package org.mindswap.swoop.popup;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import org.mindswap.swoop.SwoopFrame;
import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.utils.owlapi.CorrectedRDFRenderer;
import org.mindswap.swoop.utils.owlapi.diff.OWLDiff;
import org.mindswap.swoop.utils.ui.ExceptionDialog;
import org.mindswap.swoop.utils.ui.SwoopFileFilter;
import org.semanticweb.owl.io.RendererException;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLOntology;
import org.xngr.browser.editor.XmlEditorPane;
import org.xngr.browser.properties.EditorProperties;



/**
 * @author Aditya Kalyanpur
 *
 */
public class PopupOntologySource extends JFrame implements KeyListener, ActionListener, WindowListener, DocumentListener, MouseListener {

	EditorProperties props;
	SwoopFrame swoopHandler;
	SwoopModel swoopModel;
	
	public JEditorPane codePane;
	JMenuItem saveOnt, saveAsOnt, refreshOnt, findMenu, findNextMenu, findPreviousMenu;
//	JCheckBoxMenuItem autoRefreshClose;
	File ontFile = null;
	JFileChooser wrapChooser = new JFileChooser();
	public String originalSrc;
	private OWLOntology originalOntology;
	
	boolean srcChanged = false;
	public String findMatch = "";
	int currentMatch = 0, lastMatch = 0;
	protected UndoAction undoAction;
    protected RedoAction redoAction;
	UndoManager undo = new UndoManager();
	public boolean matchCase = false;
	public boolean matchWord = true;
	public DefaultHighlighter dh;
	public DefaultHighlighter.DefaultHighlightPainter dhp = new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);
	public int format; // use the same values as in the file filters: 0- RDF/XML, 1- AS , 2- Turtle
	int RDF_FORMAT = 0;
	int AS_FORMAT = 1;
	int TURTLE_FORMAT = 2;
	public  String[] fileExt = { ".owl", ".txt",""};
	JCheckBox caseChk;
	JCheckBox wordChk;	
	
	public PopupOntologySource(SwoopFrame handler, SwoopModel model, int format) {
		this.swoopHandler = handler;
		this.swoopModel = model;
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.format = format;
		
		caseChk = new JCheckBox("Match Case");
		wordChk = new JCheckBox("Match Whole Word");
		caseChk.setSelected(false);
		wordChk.setSelected(false);
		setupUI();
		setupMenu();	
	}
	
	
	public void displaySrcCode(OWLOntology ontology) {
		originalOntology = ontology;
		
		try {
			setTitle("View Source - "+ontology.getURI().toString());
		} catch (OWLException e) {			
			e.printStackTrace();
		}
		
		StringWriter st = new StringWriter();
		CorrectedRDFRenderer rdfRenderer = new CorrectedRDFRenderer();
		
		org.semanticweb.owl.io.abstract_syntax.Renderer ASRenderer = 
			new org.semanticweb.owl.io.abstract_syntax.Renderer();
		
		String code = "";
		try {
			switch (this.format) {
				case 0 :
						// source code in RDF
						rdfRenderer.renderOntology(ontology, st);
						code = st.toString();
						break; 
				case 1 : 
						ASRenderer.renderOntology(ontology, st); 
						code = st.toString();
						break;
				case 2 : //TODO Turtle renderer
				break;
			}			
		} catch (RendererException e) {
			e.printStackTrace();
		}		
		// codePane.setSelectionColor(Color.YELLOW);
		codePane.setText(code);
		codePane.setCaretPosition(0);
		originalSrc = codePane.getText();
		codePane.getDocument().addUndoableEditListener(new MyUndoableEditListener());
		codePane.getDocument().addDocumentListener(this);
	}
	
	
	private void setupUI() {
		
		codePane = new XmlEditorPane();
		
		if (format!=RDF_FORMAT) {			
			
			codePane = new JEditorPane();			
			codePane.setFont(new Font("Courier", Font.PLAIN, 13));
		}
		
		codePane.setAutoscrolls(true);
		java.awt.Container content = getContentPane();
		
		// create defaulthighlighter and associate with webPane
    	dh = new DefaultHighlighter(); 
		codePane.setHighlighter(dh); 
		codePane.addMouseListener(this);
		
		content.setLayout(new BorderLayout());
		content.add(new JScrollPane(codePane), "Center");
		setLocation(10,10);
		setSize(800,600);
		
		this.addWindowListener(this);
	}
	
	private void setupMenu() {
		JMenuBar menuBar = new JMenuBar();
		
		JMenu fileMenu = new JMenu("File");
		menuBar.add(fileMenu);
		saveOnt = new JMenuItem("Save");
		saveOnt.addActionListener(this);
		saveAsOnt = new JMenuItem("Save As..");
		saveAsOnt.addActionListener(this);
		fileMenu.add(saveOnt);
		fileMenu.add(saveAsOnt);
		JMenuItem menuExit = new JMenuItem("Exit");
		fileMenu.add(menuExit);
		menuExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				dispose();
			}
		});
		
		JMenu editMenu = new JMenu("Edit");
		menuBar.add(editMenu);
		findMenu = new JMenuItem("Find ");
		findMenu.addActionListener(this);
		findMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK));

		editMenu.add(findMenu);
		findPreviousMenu = new JMenuItem("Find Previous..");
		findPreviousMenu.addActionListener(this);
		findPreviousMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, ActionEvent.CTRL_MASK));
		editMenu.add(findPreviousMenu);
		
		findNextMenu = new JMenuItem("Find Next..");		
		findNextMenu.addActionListener(this);
		findNextMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, ActionEvent.CTRL_MASK));
		editMenu.add(findNextMenu);
		
		editMenu.addSeparator();
		
		undoAction = new UndoAction();
        editMenu.add(undoAction);
        redoAction = new RedoAction();
        editMenu.add(redoAction);
        
		JMenu refreshMenu = new JMenu("Update Model");
		menuBar.add(refreshMenu);
		refreshOnt = new JMenuItem("Save State & Update Ontology in SWOOP");
		refreshOnt.addActionListener(this);
		refreshOnt.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
//		autoRefreshClose = new JCheckBoxMenuItem("Auto-Update Ontology upon Closing this Window");
//		autoRefreshClose.setState(true);
		refreshMenu.add(refreshOnt);
//		refreshMenu.add(autoRefreshClose);
		setJMenuBar(menuBar);
	}

	
	private boolean updateOntology() {
		
		if (!srcChanged) {
			return true;
		} else {
			srcChanged = false;
		}
		
		try {
		
		URI baseURI = originalOntology.getURI();
		StringReader originalReader = new StringReader(originalSrc);
    		OWLOntology ontology = null; // = swoopModel.getSelectedOntology();
    		
    		// add updated ontology 
    		StringReader updateReader = new StringReader(codePane.getText());
		OWLOntology updatedOntology = null;
		System.out.println("originalReader: "+originalReader);
		System.out.println("baseURI: "+baseURI);
    		switch (format) {
				case 0: 
					ontology = swoopModel.loadOntologyInRDF(originalReader, baseURI);
					updatedOntology = swoopModel.loadOntologyInRDF(updateReader, baseURI);
					break;
				case 1: 
					ontology = swoopModel.loadOntologyInAbstractSyntax(originalReader, baseURI);
					updatedOntology = swoopModel.loadOntologyInAbstractSyntax(updateReader, ontology.getURI());
					break;
			}
    		// restore old ontology, if exception loading new one
    		if (updatedOntology==null) {
    			JOptionPane.showMessageDialog(this, "Error parsing source code of Ontology.", "Parse Error", JOptionPane.ERROR_MESSAGE);
    			// srcChanged = false;
    			return false;
    		}
    		else {
    			List changes = OWLDiff.getChanges(ontology, updatedOntology, originalOntology);
    			swoopModel.addUncommittedChanges(changes);
    			
    			int result = JOptionPane.showConfirmDialog(this, "Ontology model updated successfully.\nApply changes now?", "Ontology Updated", JOptionPane.YES_NO_OPTION);
    			if (result == JOptionPane.YES_OPTION) {
    				swoopModel.applyOntologyChanges();
    			}
    		}
    		
    		return true;    		
		}
		catch (Exception ex) {
			JDialog dialog = ExceptionDialog.createDialog(this, "Cannot parse ontology", ex);
			dialog.show();
			ex.printStackTrace();
			return false;
		}
	}
	
	public int findClosingParent(int start) {
		// TODO
		int balance = 0, i = 0;
		boolean seenFirst = false;
		String  tmp="";
		
		Document doc = codePane.getDocument();
		
		try {
			tmp = doc.getText(start, doc.getLength()-start);
		}
		catch (BadLocationException e)  {
			
		}
		
		while (true) {
			if (tmp.charAt(i)=='(') { balance++;seenFirst=true;}
			else if (tmp.charAt(i)==')') { balance--;seenFirst = true; }
			
			i++;			
			if (balance==0 && seenFirst) break;
			if (i+start > doc.getLength()) {
				i = -1;
				break;
			}
		}
		
		 if (i != -1) {
			codePane.select(start, start+i+1);
		 }
		return start+i+1;
	}
	
	
	public String getEntityText(int startMatch, int endMatch) {
		
		Document doc = (Document) codePane.getDocument();
		try {
			return doc.getText(startMatch, endMatch-startMatch);
		}
		catch (BadLocationException e)  {	
			return null;
		}
	}
	
	public int matchString(boolean forward, boolean showError) {
		// match the next occurence of originalSrc in codePane
		
		Document doc = (Document) codePane.getDocument();
		if (forward) currentMatch++; else currentMatch--;
		try {
			boolean matchFound = false;
			while (currentMatch>=0 && currentMatch<(doc.getLength()-findMatch.length())) {
				String check = doc.getText(currentMatch, findMatch.length());
				String prevChar = "_";
				if (currentMatch>=1) prevChar = doc.getText(currentMatch-1, 1);
				String nextChar = "_";
				if (currentMatch+findMatch.length()<doc.getLength()) nextChar = doc.getText(currentMatch+findMatch.length(), 1);
				int pcharValue = (int) prevChar.charAt(0);
				int ncharValue = (int) nextChar.charAt(0);				
				boolean charsNotAlphabet = true;
				if (((ncharValue>=65 && ncharValue<=90) || (ncharValue>=97 && ncharValue<=122)) || 
					((pcharValue>=65 && pcharValue<=90) || (pcharValue>=97 && pcharValue<=122))) 
						charsNotAlphabet = false;
				
				if (!matchCase) {
					check = check.toLowerCase();
					findMatch = findMatch.toLowerCase();
				}
				
				if ((matchWord && check.equals(findMatch) && charsNotAlphabet) 
						|| (!matchWord && check.indexOf(findMatch)>=0)) {					
					matchFound = true;
					lastMatch = currentMatch;
					break;
				}
				if (forward) currentMatch++; else currentMatch--;
			}
			if (matchFound) {
				codePane.select(currentMatch, currentMatch+findMatch.length());
				return currentMatch;
			}
			else {
				if (showError) JOptionPane.showMessageDialog(this, "No more matches found!", "Find Error", JOptionPane.ERROR_MESSAGE);
				currentMatch = lastMatch;
			}
		}
		catch (BadLocationException e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	public void actionPerformed(ActionEvent e) {
		
		if (e.getSource()==findMenu) {
			
			String findNewMatch = JOptionPane.showInputDialog(
                    null,
                    new Object[] {
                    	"Specify Search String:",
                    	caseChk,
						wordChk
                    },
                    "Find",
                    JOptionPane.PLAIN_MESSAGE
            );
			if (findNewMatch!=null) {
				findMatch = findNewMatch;
				matchCase = caseChk.isSelected();
				matchWord = wordChk.isSelected();
				currentMatch = -1;
				matchString(true, true);
			}
		}
		
		if (e.getSource()==findPreviousMenu) {
			matchString(false, true);
		}
		
		if (e.getSource()==findNextMenu) {
			matchString(true, true);
		}
		
		if (e.getSource()==refreshOnt) {
			updateOntology();
		}
		
		if (e.getSource()==saveOnt) {			
			saveLocalOntology();
		}
		
		if (e.getSource()==saveAsOnt) {
			ontFile = null;
			saveLocalOntology();
		}
	}
	
	public void saveLocalOntology() {
		try {
			int returnVal;
			FileFilter[] filters = SwoopFileFilter.getOntologyFilters();
			/* (*.swp, *.swo, *.owl, *.rdf, *.xml, ".txt) */
			int mapFormatToFilterIndex[] = {1,4};
			wrapChooser.setFileFilter(filters[mapFormatToFilterIndex[format]]);

		
			if(ontFile == null) {
				returnVal = wrapChooser.showSaveDialog(this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					ontFile = wrapChooser.getSelectedFile();
				}
				else {        // load cancelled
					return;
				}
			}
			
			if (ontFile != null) {				
				wrapChooser.setSelectedFile(ontFile);
				FileWriter writer = new FileWriter(ontFile);
				writer.write(codePane.getText());
				writer.close();
			}
			
			System.out.println("Ontology saved successfully at "+ontFile.getPath().toString());
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void windowClosing(WindowEvent e) {
		boolean closeWindow = true;
		if (srcChanged) {
			int result = JOptionPane.showConfirmDialog(this, "Do you want to update the ontology in SWOOP?", "Update Model", JOptionPane.YES_NO_CANCEL_OPTION);
			if(result==JOptionPane.YES_OPTION) {
				closeWindow = updateOntology();
			} else if (result==JOptionPane.CANCEL_OPTION) {
				closeWindow = false;
			} else {
				closeWindow = true;
			}
					
		}
		if (closeWindow) {
			dispose();
		}
	}
	
	public void windowOpened(WindowEvent arg0) {
	}
	public void windowClosed(WindowEvent arg0) {
	}
	public void windowIconified(WindowEvent arg0) {
	}
	public void windowDeiconified(WindowEvent arg0) {	
	}	
	public void windowActivated(WindowEvent arg0) {
	}
	public void windowDeactivated(WindowEvent arg0) {
	}

	public void  keyPressed(KeyEvent arg0) {
		
	}
	
	public void  keyTyped(KeyEvent arg0) {
		
	}

	public void  keyReleased(KeyEvent arg0) {
		
	}
	
//	This one listens for edits that can be undone.
    protected class MyUndoableEditListener
                    implements UndoableEditListener {
        public void undoableEditHappened(UndoableEditEvent e) {
            //Remember the edit and update the menus.
            undo.addEdit(e.getEdit());
            undoAction.updateUndoState();
            redoAction.updateRedoState();
        }
    }
	
	class UndoAction extends AbstractAction {
        public UndoAction() {
            super("Undo");
            setEnabled(false);
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
        }

        public void actionPerformed(ActionEvent e) {
            try {
                undo.undo();
            } catch (CannotUndoException ex) {
                System.out.println("Unable to undo: " + ex);
                ex.printStackTrace();
            }
            updateUndoState();
            redoAction.updateRedoState();
        }

        protected void updateUndoState() {
            if (undo.canUndo()) {
                setEnabled(true);
                putValue(Action.NAME, undo.getUndoPresentationName());
                putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
            } else {
                setEnabled(false);
                putValue(Action.NAME, "Undo");
                putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
            }
        }
    }

    class RedoAction extends AbstractAction {
        public RedoAction() {
            super("Redo");
            setEnabled(false);
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK));
        }

        public void actionPerformed(ActionEvent e) {
            try {
                undo.redo();
            } catch (CannotRedoException ex) {
                System.out.println("Unable to redo: " + ex);
                ex.printStackTrace();
            }
            updateRedoState();
            undoAction.updateUndoState();
        }

        protected void updateRedoState() {
            if (undo.canRedo()) {
                setEnabled(true);
                putValue(Action.NAME, undo.getRedoPresentationName());
                putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK));
            } else {
                setEnabled(false);
                putValue(Action.NAME, "Redo");
                putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK));
            }
        }
    }

    private void srcModified() {
    	srcChanged = true;
    	dh.removeAllHighlights();
    }
    
	public void insertUpdate(DocumentEvent arg0) {
		srcModified();
	}
	public void removeUpdate(DocumentEvent arg0) {
		srcModified();		
	}
	public void changedUpdate(DocumentEvent arg0) {
		srcModified();		
	}

	public void mouseClicked(MouseEvent e) {		
	}
	

	public void mousePressed(MouseEvent e) {
		if (e.getSource()==codePane) dh.removeAllHighlights();
	}
	public void mouseReleased(MouseEvent arg0) {
	}
	public void mouseEntered(MouseEvent arg0) {
	}
	public void mouseExited(MouseEvent arg0) {
	}

}
