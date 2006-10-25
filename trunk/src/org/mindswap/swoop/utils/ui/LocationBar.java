package org.mindswap.swoop.utils.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.net.URI;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.apache.log4j.Logger;
import org.mindswap.swoop.ModelChangeEvent;
import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.SwoopModelListener;
import org.mindswap.swoop.utils.SwoopLoader;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLNamedObject;
import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLOntology;

/**
 * Provides the address and navigation bar for SWOOP.
 * LocationBar listens to the SwoopModel for updates to keep its
 * history up to date.
 * @author ronwalf
 *
 */
public class LocationBar extends JPanel implements SwoopModelListener {
	SwoopModel model;
	Logger log;
	SwoopLoader loader;
	
	private final static int SELECTED = 0, ONTOLOGY = 1;
	
	int historyCtr = -1; // counter for history array
	OWLNamedObject[][] historyEntity= new OWLNamedObject[9999][2]; // history of OWL objects rendered
	public String addrComboString;
	
	JButton prevBtn, nextBtn;
	public JComboBox addrCombo;
	GUIListener listener;
	
	
	protected class GUIListener implements ActionListener, MouseMotionListener {
//		LocationBar bar;
//		
//		public GUIListener(LocationBar bar) {
//			this.bar = bar;
//		}
		public void actionPerformed(ActionEvent e) {
//			 when user presses the Previous Button (History)
			if (e.getSource()==prevBtn) {
					previousHistory();
			}
			
			// when user presses the Next Button (History)
			if (e.getSource()==nextBtn) {					
				nextHistory();							
			}
			
			// Location bar changed
			if (e.getSource() == addrCombo) {
				addressChanged();
			}
		}

		public void mouseDragged(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		public void mouseMoved(MouseEvent e) {
//			 if mouse is over previous button, set tooltip text to previous history
			if (e.getSource()==prevBtn) {
				try {
					String prevHist = "";
					if (historyCtr > 0) {
						prevHist = model.shortForm(historyEntity[historyCtr-1][SELECTED].getURI());
					}
					prevBtn.setToolTipText(prevHist);	
				}
				catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			
			// if mouse is over next button, set tooltip text to next history
			if (e.getSource()==nextBtn) {
				try {
					String nextHist = "";
					if (historyEntity[historyCtr+1][SELECTED] != null) {
						nextHist = model.shortForm(historyEntity[historyCtr+1][SELECTED].getURI());
					}
					nextBtn.setToolTipText(nextHist);
				}
				catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		
	}
	
	public LocationBar(JFrame parent, SwoopModel model) {
		this.model = model;
		loader = new SwoopLoader(parent, model);
		model.addListener(this);
		setupUI();
	}

	protected void setupUI() {
//		listener = new GUIListener(this);
		listener = new GUIListener();
		
		setLayout(new GridLayout(1,4));
		
		//		 load history button images
		ImageIcon prevIcon = (ImageIcon) SwoopIcons.prevIcon;
		prevBtn = new JButton("Previous", prevIcon);
		prevBtn.setFont(model.getFont());
		prevBtn.addActionListener(listener);
		prevBtn.setEnabled(false);
		prevBtn.addMouseMotionListener(listener);
		ImageIcon nextIcon = (ImageIcon) SwoopIcons.nextIcon;
		nextBtn = new JButton("Next", nextIcon);
		nextBtn.setFont(model.getFont());
		nextBtn.addActionListener(listener);
		nextBtn.setEnabled(false);
		nextBtn.addMouseMotionListener(listener);	
		
		
//		 add row panel for Address URL bar
		JPanel rowPanelAddr = new JPanel();
		JLabel addrLbl = new JLabel("Address: ");
		addrLbl.setFont(model.getFont());
		addrCombo = new JComboBox();
		addrCombo.setFont(model.getFont());
		//addrCombo.addItemListener(this); // don't add this - creates weird listener problems
		addrCombo.addActionListener(listener);
		addrCombo.setEditable(true);
		addrCombo.setSelectedItem("");
		
		rowPanelAddr.setLayout(new BorderLayout());
		rowPanelAddr.add(addrLbl, "West");
		rowPanelAddr.add(addrCombo, "Center");
		JSplitPane topPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		JPanel histBtnPane = new JPanel();
		histBtnPane.setLayout(new GridLayout(1, 6));
		histBtnPane.add(prevBtn);
		histBtnPane.add(nextBtn);
		topPanel.setLeftComponent(histBtnPane);
		topPanel.setRightComponent(rowPanelAddr);
		topPanel.setDividerLocation(200);
		topPanel.setEnabled(false);
		
		this.removeAll();
		this.add(topPanel);
		
	}
	
	public void modelChanged(ModelChangeEvent event) {
		if ((event.getType() == ModelChangeEvent.ENTITY_SEL_CHANGED )||
				(event.getType() == ModelChangeEvent.ONTOLOGY_SEL_CHANGED)) {
			update();
		}	
	}
	
	/**
	 * Update the history pointers and address bar
	 *
	 */
	protected void update() {
		OWLOntology selectedOnt = model.getSelectedOntology();
		OWLNamedObject selectedEntity = model.getSelectedEntity();
		if (selectedEntity == null) {
			selectedEntity = selectedOnt;
		}
		
		if ((historyCtr >= 0) &&
				(selectedEntity == historyEntity[historyCtr][SELECTED]) && 
				(selectedOnt == historyEntity[historyCtr][ONTOLOGY])) {
			// Nothing to do
		} else if ((historyCtr > 0) &&
				(selectedEntity == historyEntity[historyCtr-1][SELECTED]) && 
				(selectedOnt == historyEntity[historyCtr-1][ONTOLOGY])) {
			historyCtr -= 1;
		} else if ((historyCtr < historyEntity.length-1) &&
				(selectedEntity == historyEntity[historyCtr+1][SELECTED]) && 
				(selectedOnt == historyEntity[historyCtr+1][ONTOLOGY])) {
			historyCtr += 1;
		} else {
			historyCtr++;
			historyEntity[historyCtr][SELECTED] = selectedEntity;
			historyEntity[historyCtr][ONTOLOGY] = selectedOnt;
			
			for (int i = historyCtr + 1; i < historyEntity.length; i++) {
				historyEntity[i][SELECTED] = null;
				historyEntity[i][ONTOLOGY] = null;
			}
		}
		updateAddressBar();
		updateButtons();
	}
	

	protected void addressChanged() {

		try {
			String uri = (String) addrCombo.getSelectedItem();
			if ((uri == null) || (uri.equals(addrComboString))) {
				return;
			}
			addrComboString = uri;
			uri = uri.trim();
			if (uri.equals(""))
				return;
			
			OWLEntity selectedEntity = model.getSelectedEntity(); 
			if(selectedEntity != null) {
				URI selectedURI = selectedEntity.getURI();			
				if( selectedURI == null && (selectedEntity instanceof OWLIndividual))
				    selectedURI = ((OWLIndividual) selectedEntity).getAnonId();
				if( uri.equals(selectedURI.toString()))
					return;
			}

			// check if uri is one of the ontologies
			Set ontSet = model.getOntologyURIs();
			Iterator ontIter = ontSet.iterator();
			while (ontIter.hasNext()) {
				URI ontURI = (URI) ontIter.next();
				if (ontURI.toString().equals(uri)) {
					OWLOntology ont = model.getOntology(ontURI);
					//ontDisplay.selectOntology(ont);
					model.setSelectedOntology(ont);
					return;
				} 
			}

			loader.selectEntity(uri);
//			// or a class/property/instance
//			if ((!isOntology) && (entityInSwoopModel)) {
//				
//			} else {
//				// new ontology needs to be loaded in swoopModel
//				String ontURI = uri;
//				if (uri.indexOf("#") >= 0)
//					ontURI = uri.substring(0, uri.indexOf("#"));
//				loader.loadURIInModel(ontURI, uri);
//			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Traverse to the previous element in the History
	 * Calls traverseHistory with the appropriate arguments (entity, ontology, imports)
	 * as obtained from the historyEntity list.
	 *
	 */
	public void previousHistory() {
		try {
			// get previous term from History Array
			if (historyCtr<=0) return; else historyCtr--;
			OWLNamedObject namedObj = historyEntity[historyCtr][0];
			OWLOntology histOnt = (OWLOntology) historyEntity[historyCtr][1];
			this.traverseHistory(namedObj, histOnt);
			
			// if previous was pressed, next button must be enabled
			nextBtn.setEnabled(true);			
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		updateButtons();
	}
	
	
	/**
	 * Traverse to the next element in the History
	 * Calls traverseHistory with the appropriate arguments (entity, ontology, imports)
	 * as obtained from the historyEntity list.
	 *
	 */
	public void nextHistory() {
		try {
			// get next term from History Array			
			historyCtr++;
			OWLNamedObject namedObj = historyEntity[historyCtr][0];
			OWLOntology histOnt = (OWLOntology) historyEntity[historyCtr][1];
			this.traverseHistory(namedObj, histOnt);
			
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		updateButtons();
	}
	
	protected void traverseHistory(OWLNamedObject obj, OWLOntology ont) throws OWLException {
		if (obj == ont) {
			model.setSelectedOntology(ont);
		} else if (obj instanceof OWLEntity) {
			model.setSelectedEntity((OWLEntity) obj);
		} else {
			
		}
	}
	
	protected void updateAddressBar() {
		String uri = "";
		OWLObject selected = model.getSelectedEntity();
		if (selected == null) {
			selected = model.getSelectedOntology();
		}
		if (selected != null) {
			uri = "<no uri>";
			try {
				if (selected instanceof OWLNamedObject) {
					OWLNamedObject named = (OWLNamedObject) selected;
					if (named.getURI() != null) {
						uri = named.getURI().toString();
					} else if (named instanceof OWLIndividual) {
						uri = ((OWLIndividual) named).getAnonId().toString();
					} 
				}
			} catch (OWLException e) {
				// Can't retreive uri.  Oops!
				e.printStackTrace();
			}
		}
		
		Set contents = new TreeSet();
		for (int i = 0; i < addrCombo.getItemCount(); i++) {
			String addrURI = addrCombo.getItemAt(i).toString();
			if (!addrURI.toLowerCase().equals(uri.toLowerCase()))
				contents.add(addrURI);
		}

		// add (or bump) uri to top
		addrCombo.removeAllItems();
		addrCombo.addItem(uri);
		Iterator iter = contents.iterator();
		while (iter.hasNext()) {
			String addrURI = iter.next().toString();
			addrCombo.addItem(addrURI);
		}
		
	}
	
	protected void updateButtons() {
		prevBtn.setEnabled(historyCtr > 0);
		nextBtn.setEnabled((historyEntity[historyCtr + 1][SELECTED] != null) &&
				(historyEntity[historyCtr + 1][ONTOLOGY] != null));
	}
	
	public static void main(String args[]) throws Exception {
		final SwoopModel model = new SwoopModel();
		model.addOntology(URI.create("http://volus.net/~ronwalf/foaf.rdf"));
		
		
//		Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	JFrame frame = new JFrame();
        		LocationBar bar = new LocationBar(null, model);
        		frame.add(bar);
        		frame.setVisible(true);
            }
        });
        
        while(true) {
        	Thread.sleep(1000);
        	model.setSelectedEntity(model.getSelectedOntology().getIndividual(
        			URI.create("http://volus.net/~ronwalf/foaf.rdf#Ron_Alford")));
        }
	}

}
