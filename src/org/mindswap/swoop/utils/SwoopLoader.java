package org.mindswap.swoop.utils;

import java.awt.Component;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.mindswap.swoop.SwoopFrame;
import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.utils.ui.ExceptionDialog;
import org.mindswap.swoop.utils.ui.SwingWorker;
import org.mindswap.swoop.utils.ui.SwoopProgressDialog;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLNamedObject;
import org.semanticweb.owl.model.OWLOntology;

public class SwoopLoader {
	SwoopModel model;
	JFrame parent;
	
	public SwoopLoader(JFrame parent, SwoopModel model) {
		this.model = model;
		this.parent = parent;
	}
	
	/**
	 * Simply check if URI is present in *any* ontology in SwoopModel
	 * Currently checks Classes, Data/Object Properties and Individuals
	 * @param uri - entity URI to be checked
	 * @return The ontology it was found in.
	 */
	protected OWLNamedObject checkSwoopModel(URI uri) {
		try {
			Collection ontologySet = model.getOntologies();
			Iterator iter = ontologySet.iterator();
			while (iter.hasNext()) {
				OWLOntology ont = (OWLOntology) iter.next();
				if ((ont.getClass(uri)!=null) || (ont.getDataProperty(uri)!=null) || (ont.getObjectProperty(uri)!=null) || (ont.getIndividual(uri)!=null)) 
					return ont;				
			}			
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Important method to select OWL Entity in SWOOP by passing its URI as a string
	 * This method checks the following: 
	 * - If URI is not an XPointer, it checks if the entity URI is in the currently selected Swoop ontology and if found, selects it
	 * -- If URI is an XPointer, it obtains the pointed ontology 
	 * -- If not Xpointer and not found in current ontology, it checks in remaining ontologies in SwoopModel for a match
	 * -- If either Xpointer or match in external ontology -> same result - set selected ontology/entity and call updateOntologyViews
	 * --- As a last step, it attempts to load the external URI reference
	 * Rendering is handled separately, by listening to
	 * change in selected entity in SwoopModel
	 * @param entityURI - URI of the entity to be selected
	 */
	public void selectEntity(String entityURI) {
		
		try {
			OWLNamedObject found = null;
			OWLOntology foundOnt = null;
			
			int find = entityURI.indexOf(XPointers.asDefinedIn);
			if (find==-1) {
				// uri is NOT an XPointer
				// so check in selected ontology first
				OWLOntology selOnt = model.getSelectedOntology();
				
				// if show imports is on for that specific ontology, 
				// only then search through all entities in imports closure
				OWLEntity foundEntity = null;
				if (selOnt != null) {
					foundEntity = model.getEntity(selOnt, new URI(entityURI), model.getImportsSetting(selOnt));
				}
				if (foundEntity!=null) {
					// great! display entity in currently selected ontology and your done
					model.setSelectedEntity(foundEntity);
					return;
				}
			}
			else {
				// URI contains asDefinedIn Xpointer			
				// get ontology uri
				String ontURI = entityURI.substring(find + XPointers.asDefinedIn.length()+1, entityURI.length()-1);
				
				// if ontology not in Swoop, need to add it to Swoop
				if (!model.getOntologyURIs().contains(new URI(ontURI))) {
					model.addOntology(new URI(ontURI));	
				}
				
				// now get entityURI and set ontology in which it occurs 'foundOnt'
				entityURI = entityURI.substring(0, find);
				foundOnt = model.getOntology(new URI(ontURI));					
			}
		
			if (found==null) {
				
				// if foundOnt!=null, it implies it is an XPointer
				if (foundOnt==null) {
					// uri is not in current displayed ontology
					// and NOT an XPointer
					// so check to see if its in another ontology in SwoopModel
					foundOnt = (OWLOntology) checkSwoopModel(new URI(entityURI));
				}
				
				// System.out.println(foundOnt.getURI().toString());
				if (foundOnt!=null) {
					// Note:
					// Since selecting an entity in another ontology
					// requires creating a tree for that ontology first, 
					// we just set swoopModel.selectedEntity to this external entity 
					// Entity is finally selected in updateOntologyViews thread
					
					found = model.getEntity(foundOnt, new URI(entityURI), true); 
					
					if (found!=null) {
						model.setSelectedOntology(foundOnt);
						model.setSelectedEntity((OWLEntity) found);
						
						// whenever ontology selection changes, need to revert
						// to user-specific ontology settings
						model.loadOntSettings(foundOnt);
						return;
					}
				}
			}
			
			// if still not found, load external ontology reference into swoopModel?
			if (found==null) {
				
				String ontURI = entityURI;
                if (entityURI.indexOf("#")>=0) ontURI = entityURI.substring(0, entityURI.indexOf("#"));
                if (!model.getOntologyURIs().contains(new URI(ontURI))) {
					System.out.println("Loading external ontology reference.."+ontURI);					
					if (entityURI.endsWith("#") || entityURI.endsWith("/")) entityURI = ontURI;
					loadURIInModel(ontURI, entityURI);				
				}
				else {
					JOptionPane.showMessageDialog(parent, "Entity not found in ontology", "Error", JOptionPane.ERROR_MESSAGE);
					System.out.println("Not found: "+entityURI);
				}
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/***
	 * Important public method (used by all other components) to load
	 * a new ontology in SWOOP given its URI.
	 * @param urlStr - URL of the ontology passed as a String
	 * @param entityURL - URL of the entity passed as a String
	 * If no entity is to be selected after loading the ontology,
	 * pass entityURL = urlStr
	 * 
	 * Method:
	 * 1. Disable all ontology UI listeners in OntDisplay (ontList, ontHideBox)
	 * 2. Add ontology to swoopModel
	 * 3. If ontology is loaded properly, select ontList value
	 * 4. Call swoopModel.setSelectedOntology(ont) -> selectedOntology = ont, selectedEntity = null, calls updateOntologyDisplay
	 * 5. Finally, also display entityStr if different from urlStr 
	 */
	public void loadURIInModel(String urlStr, String entityURL) {
		try {
			//System.out.println("Load uri " + urlStr);
			final URI uri = new URI(urlStr);
			final String ontURI = urlStr;
			final String entityURI = entityURL;
			model.setCurrentlyLoadingURI(uri); // used in View Source when
			// ontology parsing fails

			//new SwoopProgressFrame(this, "Loading new ontology...", "Loading "
			//		+ uri, "Cannot load ontology from URI:\n" + uri,
			//final JFrame parent = this;
			
			final SwoopProgressDialog progress = new SwoopProgressDialog(parent, "Loading "+uri);
			
			progress.show();
			SwingWorker worker = new SwingWorker() {
				
				private OWLOntology ont = null;
				private Exception error = null;
				
				public Object construct() {
					
					long startTime = Calendar.getInstance().getTimeInMillis();
					try {
						ont = model.loadOntology(uri);
					} catch (OWLException e) {
						error = e;
					}
					
					long endTime = Calendar.getInstance().getTimeInMillis();
					System.out.println("Ontology loaded in "+
							(endTime - startTime) + " milliseconds");
					return ont;
				}
				
				public void finished() {

					progress.dispose();
					if (error != null) {
						error.printStackTrace();
						JDialog dialog = ExceptionDialog.createDialog(parent, "Cannot load ontology", error);	
						dialog.setVisible(true);
						
						return;
					}
					
					try {
						model.addOntology(ont);
						model.setSelectedOntology(ont);
					} catch (Exception e) {
						e.printStackTrace();
						throw new RuntimeException(e.getMessage());
					} finally {

						if (ont != null) {
							
							if (!entityURI.equals(ontURI)) {

								// what happens if ontURI is different
								// from ont.getURI()
								// i.e. logical URI is different from
								// physical URI
								try {
									if (!ontURI.equals(ont.getURI().toString())) {
										
										// get actual URI of ontology
										String logOntURI = ont.getURI().toString();
										if (logOntURI.endsWith("/"))
											logOntURI = logOntURI.substring(0,
													logOntURI.length() - 1);
										
										// obtain entity name
										if (!entityURI.equals(logOntURI)) {
											String entityName = "";
											if (entityURI.indexOf("#") >= 0)
												entityName = entityURI.substring(
														entityURI.indexOf("#"),
														entityURI.length());
											else 
												entityName = entityURI.substring(
														entityURI.indexOf("/"),
														entityURI.length());
											selectEntity(logOntURI
															+ entityName);
										}
									} 
									else
										selectEntity(entityURI);

								} 
								catch (OWLException e) {
									e.printStackTrace();
								}
							}
						}
					}
				}
			};
			worker.start();
		} 
		catch (URISyntaxException ex) {
			JOptionPane.showMessageDialog(null, "This is not a valid URI:\n"
					+ urlStr, "Error!", JOptionPane.ERROR_MESSAGE);
		}
	}


}
