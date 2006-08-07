/*
 * Created on May 15, 2004
 */
package org.mindswap.swoop.utils.ui;

import java.net.URI;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.mindswap.swoop.Swoop;
import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.annotea.Annotea;
import org.mindswap.swoop.annotea.Description;
import org.mindswap.swoop.reasoner.SwoopReasoner;
import org.semanticweb.owl.io.vocabulary.OWLVocabularyAdapter;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLObjectProperty;

/**
 * @author Evren Sirin
 */
public class SwoopIcons {
	private static OWLVocabularyAdapter OWL = OWLVocabularyAdapter.INSTANCE;
	public static Icon thingIcon, nothingIcon, classIcon, propIcon, dataPropIcon, individualIcon, inconsistentClassIcon;
	public static Icon importedClassIcon, importedPropIcon, importedDataPropIcon, importedIndividualIcon;
	public static Icon prevIcon, nextIcon;
	public static Icon rootIcon, commentIcon, seeAlsoIcon, explanationIcon, questionIcon;
	public static Icon upIcon, downIcon;
	public static Icon smallClassIcon, smallPropIcon, smallIndIcon;
	public static Icon linkPropIcon;
	/* *********************************************************
	 * Added for Econnections
	 * author: Meem
	 * *********************************************************
	 */
	public static Icon foreignClassIcon, foreignDataPropIcon, foreignIndividualIcon, foreignLinkPropIcon, foreignPropIcon;
	/* ********************************************************* */
	
	public static Icon getIcon(OWLEntity entity) {
		return getIcon(entity, null);
	}
	
	public static Icon getIcon(Description annot) {
		try {
			if (annot.getAnnotationType()!=null) {
				if (Annotea.getEntityName(annot.getAnnotationType().getURI()).equals("Comment")) return commentIcon;
				else if (Annotea.getEntityName(annot.getAnnotationType().getURI()).equals("SeeAlso")) return seeAlsoIcon;
				else if (Annotea.getEntityName(annot.getAnnotationType().getURI()).equals("Explanation")) return explanationIcon;
				else if (Annotea.getEntityName(annot.getAnnotationType().getURI()).equals("Question")) return questionIcon;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return rootIcon;
	}
	
	public static URL getImageURL(String imgName) {
		return SwoopIcons.class.getResource("/org/mindswap/swoop/images/"+imgName);
	}
	
	public static ImageIcon getImageIcon(String imgName) {
		//if (!Swoop.isWebStart()) return new ImageIcon("images/"+imgName);
		//else return new ImageIcon(Swoop.class.getResource("/images/"+imgName));
		return new ImageIcon(getImageURL(imgName));
	}
	
	static {
		
		thingIcon = getImageIcon("Thing.gif");
		nothingIcon = getImageIcon("Nothing.gif");
		classIcon = getImageIcon("Class.gif");
		importedClassIcon = getImageIcon("ImportedClass.gif");
		smallClassIcon = getImageIcon("SmallClass.GIF");
		propIcon = getImageIcon("Property.gif");
		linkPropIcon = getImageIcon("LinkProperty.GIF");
		importedPropIcon = getImageIcon("ImportedProperty.gif");
		smallPropIcon = getImageIcon("BothProperty.GIF");
		dataPropIcon = getImageIcon("DataProperty.gif");
		importedDataPropIcon = getImageIcon("ImportedDataProperty.gif");
		individualIcon = getImageIcon("Instance.gif");
		importedIndividualIcon = getImageIcon("ImportedInstance.gif");
		smallIndIcon = getImageIcon("SmallInstance.GIF");
		inconsistentClassIcon = getImageIcon("InconsistentClass.gif");
		prevIcon = getImageIcon("prev.gif");
		nextIcon = getImageIcon("next.gif");
		upIcon = getImageIcon("collapse.gif");
		downIcon = getImageIcon("expand.gif");
		// annotea-renderer icons
		rootIcon = getImageIcon("Root.jpg");
		commentIcon = getImageIcon("Comment.jpg");
		seeAlsoIcon = getImageIcon("SeeAlso.jpg");
		explanationIcon = getImageIcon("Explanation.jpg");
		questionIcon = getImageIcon("Question.jpg");

		/***********************************************************************
		 * Added for Econnections author: Meem
		 * *********************************************************
		 */
		// Foreign icons
		foreignClassIcon = getImageIcon("ForeignClass.gif");
		foreignDataPropIcon = getImageIcon("ForeignDataProperty.gif");
		foreignIndividualIcon = getImageIcon("ForeignIndividual.gif");
		foreignLinkPropIcon = getImageIcon("ForeignLinkProperty.gif");
		foreignPropIcon = getImageIcon("ForeignProperty.gif");
		/* ********************************************************* */
		
	}
	
	public static Icon getIcon(OWLEntity entity, SwoopModel swoopModel) {
		try {
			URI uri = entity.getURI();
			
			if (uri==null) return questionIcon;
			
			SwoopReasoner reasoner = swoopModel.getReasoner();
			
			if (uri.toString().equals(OWL.getThing()))
				return thingIcon;
			else if (uri.toString().equals(OWL.getNothing()))
				return nothingIcon;

			/* *********************************************************
			 * Added for Econnections
			 * author: Meem
			 * *********************************************************
			 */
			// entity is foreign to the reasoner's ontology
			else if ( reasoner.getOntology()!=null && reasoner.getOntology().isForeign( entity ) )
			{
				if( entity instanceof OWLObjectProperty)	// Foreign object property
				{
					OWLObjectProperty prop = (OWLObjectProperty) entity;
					if (prop.isLink()) return foreignLinkPropIcon;	// Foreign links are meanigless/invalid
					return foreignPropIcon;
				}
				else if(entity instanceof OWLDataProperty)	// foreign data type property
					return foreignDataPropIcon;
				else if(entity instanceof OWLIndividual)	// foreign individual  
					return foreignIndividualIcon;
				else if(entity instanceof OWLClass)			// foreign class
				{
					try
					{
						if(reasoner != null && !reasoner.isConsistent((OWLClass) entity))
							return inconsistentClassIcon;
					} catch (Exception e) {}
					
					return foreignClassIcon;
				}
			} // end if entity foreign
			/* ********************************************************* */

			else if(entity instanceof OWLObjectProperty) {
				OWLObjectProperty prop = (OWLObjectProperty) entity;
				if (prop.isLink()) return linkPropIcon;
				if (!entity.getOntologies().contains(reasoner.getOntology())) return importedPropIcon;
				else return propIcon;
			}									
			else if(entity instanceof OWLDataProperty) { 
				if (!entity.getOntologies().contains(reasoner.getOntology())) return importedDataPropIcon;
				else return dataPropIcon;
			}
			else if(entity instanceof OWLIndividual) {  
				if (!entity.getOntologies().contains(reasoner.getOntology())) return importedIndividualIcon;
				else return individualIcon;
			}
			else if(entity instanceof OWLClass) {
				try {
					
					boolean isConsistent = reasoner.isConsistent((OWLClass) entity);
//					if (swoopModel.getEnableDebugging()) {
//						// if reasoner is not an instanceof Pellet, create a new instance
//						if (!(swoopModel.getReasoner() instanceof PelletReasoner)) {
//							isConsistent = swoopModel.getDebugger().isConsistent((OWLClass) entity);
//						}
//					}
						
					if (!isConsistent) return inconsistentClassIcon;					
				} 
				catch (Exception e) {
				}
				
				if (!entity.getOntologies().contains(reasoner.getOntology())) return importedClassIcon;
				
				return classIcon;
			}
		} catch (OWLException e) {
			e.printStackTrace();
		}
			
		return null;
	}
}
