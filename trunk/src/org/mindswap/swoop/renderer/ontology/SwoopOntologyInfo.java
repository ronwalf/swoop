//The MIT License
//
// Copyright (c) 2004 Mindswap Research Group, University of Maryland, College Park
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to
// deal in the Software without restriction, including without limitation the
// rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
// sell copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
// IN THE SOFTWARE.

package org.mindswap.swoop.renderer.ontology;

import java.awt.Component;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.mindswap.pellet.debug.owlapi.Reasoner;
import org.mindswap.swoop.OntologyDisplay;
import org.mindswap.swoop.SwoopDisplayPanel;
import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.utils.RulesExpressivity;
import org.mindswap.swoop.reasoner.PelletReasoner;
import org.mindswap.swoop.reasoner.SwoopReasoner;
import org.mindswap.swoop.renderer.SwoopEditableRenderer;
import org.mindswap.swoop.renderer.SwoopOntologyRenderer;
import org.mindswap.swoop.renderer.entity.ConciseFormatEntityRenderer;
import org.mindswap.swoop.utils.SwoopStatistics;
import org.mindswap.swoop.utils.ui.EntityComparator;
import org.semanticweb.owl.model.OWLAnnotationInstance;
import org.semanticweb.owl.model.OWLAnnotationProperty;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataValue;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLProperty;
import org.semanticweb.owl.model.change.AddAnnotationInstance;
import org.semanticweb.owl.model.change.AddImport;
import org.semanticweb.owl.model.change.OntologyChange;
import org.semanticweb.owl.model.change.RemoveAnnotationInstance;
import org.semanticweb.owl.model.change.RemoveImport;

/**
 * @author Evren Sirin
 */
public class SwoopOntologyInfo extends BaseOntologyRenderer implements SwoopOntologyRenderer, SwoopEditableRenderer {
	
    private boolean editorEnabled = false;
    final public static int SHOW_INDIVIDUALS    = 1;
	final public static int SHOW_CLASSES        = 2;
	final public static int SHOW_PROPERTIES     = 3;
	final public static int SHOW_DATAPROPERTIES = 4;
	final public static int SHOW_OBJPROPERTIES  = 5;
	final String NEWLINE = System.getProperty("line.separator");
	final String HLINE = "--------------------------------------------------"+NEWLINE;
	public String statsText = "";
	
	public Map OWLObjectHash;
	SwoopModel swoopModel;
	PrintWriter out;
	
	/**
	 * Print the added "import ontology" changes (currently in swoopModel uncommitted changes)
	 * @param ont
	 */
    private void printAddedImports(OWLOntology ont) {

        String addImports = "";
        try {
            List changes = swoopModel.getUncommittedChanges();
            Iterator iter = changes.iterator();
            while(iter.hasNext()) {
                OntologyChange change = (OntologyChange) iter.next();
                if(change instanceof AddImport) {
                    AddImport add = (AddImport) change;
                    if(add.getOntology().getURI().equals(ont.getURI())) {
                        OWLOntology impOnt = add.getImportOntology();
                        out.print("<br><font color=\"green\">");
	                    out.print("<a href=\"" + impOnt.getURI().toString() + "\">"+ impOnt.getURI() + "</a>");
                        out.print("</font>");
                        this.addUndo(add, "Delete");
                    }
                }
            }
        } 
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Check if a RemoveImport change in swoopModel.uncommittedChanges()
     * matches the subj, obj arguments passed to iw
     * @param subj
     * @param obj
     * @return
     */
    private OntologyChange isDeleted(OWLOntology subj, OWLOntology obj) {

        // isDeleted function for Imported Ontologies
        List changes = swoopModel.getUncommittedChanges();
        Iterator i = changes.iterator();
        while(i.hasNext()) {
            OntologyChange oc = (OntologyChange) i.next();

            if(oc instanceof RemoveImport) {
                RemoveImport rem = (RemoveImport) oc;
                if(rem.getOntology().equals(subj)
                        && (rem.getImportOntology().equals(obj))) return oc;
            }
        }
        return null;
    }

    public String getContentType() {
        return "text/html";
    }

    public String getName() {
        return "Ontology Info";
    }

    // display stats in ont pane
    public void showStatistics(OWLOntology ont, PrintWriter statistics) {
        
    	statistics.print("<FONT FACE=\""+swoopModel.getFontFace()+"\" SIZE="+swoopModel.getFontSize()+">");
    	
    	int numberOfClasses, numberOfObjectProperties=0, numberOfDatatypeProperties, numberOfAnnotationProperties, numberOfInstances;
        int totalClasses=0, totalObjProps=0, totalDataProps=0, totalAnnotatedProps=0, totalIndividuals=0;
        //*******************************************
        //Added for Econnections
        //*******************************************
        int numberOfLinkProperties=0, numberOfForeignEntities=0;
        int numberOfForeignClasses=0, numberOfForeignProperties=0, numberOfForeignIndividuals=0;
        //********************************************
        try {
            //statistics.println("<br><b> ONTOLOGY STATISTICS </b><br>");
            numberOfClasses = ont.getClasses().size();
            numberOfDatatypeProperties = ont.getDataProperties().size();
            //***********************************************************
            //Added for Econnections
            //*************************************************************
            Iterator it= ont.getObjectProperties().iterator();
            Set foreignOntologies = new HashSet();
            while(it.hasNext()){
            	OWLObjectProperty property=(OWLObjectProperty)it.next();
            	if(property.isLink() && !(property.getLinkTarget()).equals(ont.getURI())){
            		numberOfLinkProperties++;
            		foreignOntologies.add(property.getLinkTarget()); 
            	}
            	else{
            		numberOfObjectProperties++;
            	}
            }
            //numberOfObjectProperties = ont.getObjectProperties().size();
            numberOfForeignEntities = ont.getForeignEntities().size();
            Iterator j= ont.getForeignEntities().keySet().iterator();
            while(j.hasNext()){
            	OWLEntity e = (OWLEntity)j.next();
            	if(e instanceof OWLClass)
            		numberOfForeignClasses++;
            	if(e instanceof OWLProperty)
            		numberOfForeignProperties++;
            	if(e instanceof OWLIndividual)
            		numberOfForeignIndividuals++;
            	
            }
            //************************************************************
            numberOfAnnotationProperties = ont.getAnnotationProperties().size();
            numberOfInstances = ont.getIndividuals().size();
            
            // get imported ontology statistics            
            totalClasses = swoopModel.getEntitySet(ont, swoopModel.TRANSCLOSE_ONT, this.SHOW_CLASSES).size();
            //*****************************************************
            //Changed for Econnections
            //*****************************************************
            totalObjProps = swoopModel.getEntitySet(ont, swoopModel.TRANSCLOSE_ONT, this.SHOW_OBJPROPERTIES).size()-numberOfLinkProperties;
            //***************************************************
            totalDataProps = swoopModel.getEntitySet(ont, swoopModel.TRANSCLOSE_ONT, this.SHOW_DATAPROPERTIES).size();
            totalIndividuals = swoopModel.getEntitySet(ont, swoopModel.TRANSCLOSE_ONT, this.SHOW_INDIVIDUALS).size();
            totalAnnotatedProps = swoopModel.getEntitySet(ont, swoopModel.TRANSCLOSE_ONT, this.SHOW_PROPERTIES).size() - totalDataProps - totalObjProps;
//          *****************************************************
			//Added for rules
			//*****************************************************

            if (!swoopModel.getRuleExpr().getRuleMap().isEmpty()) { //there are rules
			URI swrlVariableURI;
			try {
				swrlVariableURI = new URI(
						"http://www.w3.org/2003/11/swrl#Variable");
			URI swrlClassPredURI = new URI(
					"http://www.w3.org/2003/11/swrl#classPredicate");
			URI swrlPropertyPredURI = new URI(
					"http://www.w3.org/2003/11/swrl#propertyPredicate");
			
			// number of instances of variables to be removed
			int numberOfVariables = swoopModel.getReasoner().allInstancesOf(
					swoopModel.getSelectedOntology().getClass(swrlVariableURI)).size();
			numberOfInstances =- numberOfVariables;
			totalIndividuals =- numberOfVariables;
			Set setPredicates = new HashSet();
			// number of instances of classPredicates to be removed
			int numberOfClassPredicates = 0;
			OWLObjectProperty prop1 = (OWLObjectProperty) swoopModel
					.getSelectedOntology().getObjectProperty(swrlClassPredURI);
			if (!(prop1 == null)) {
				Set objects1 = prop1.getUsage(swoopModel.getSelectedOntology());
				for (Iterator it1 = objects1.iterator(); it1.hasNext();) {
					OWLEntity entity = (OWLEntity) it1.next();
					if (entity instanceof OWLIndividual) {
						Map objects2 = ((OWLIndividual) entity)
								.getObjectPropertyValues(swoopModel
										.getSelectedOntology());
						numberOfClassPredicates =+ ((Set) objects2.get(prop1)).size();
					}
				}
			}
			numberOfInstances =- numberOfClassPredicates;
			totalIndividuals =- numberOfClassPredicates;
			// number of instances of property predicates to be removed
			int numberOfPropPredicates = 0;
			prop1 = (OWLObjectProperty) swoopModel.getSelectedOntology()
					.getObjectProperty(swrlPropertyPredURI);
			if (!(prop1 == null)) {
				Set objects1 = prop1.getUsage(swoopModel.getSelectedOntology());
				for (Iterator it1 = objects1.iterator(); it1.hasNext();) {
					OWLEntity entity = (OWLEntity) it1.next();
					if (entity instanceof OWLIndividual) {
						Map objects2 = ((OWLIndividual) entity)
								.getObjectPropertyValues(swoopModel
										.getSelectedOntology());
						numberOfPropPredicates =+ ((Set) objects2.get(prop1)).size();
		
					}
				}
			}
			numberOfInstances =- numberOfPropPredicates;
			totalIndividuals =- numberOfPropPredicates;
			// number of classes to be removed
			Set setOfClasses = ont.getClasses();
			for (Iterator it1 = setOfClasses.iterator(); it1.hasNext();) {
				OWLClass c = (OWLClass) it1.next();
				if (c.getURI().getPath().equals("/2003/11/swrl")) {
					numberOfClasses--;
					totalClasses--;
				}
					
			}
//			 number of object properties to be removed
			Set setOfObjectProps = ont.getObjectProperties();
			for (Iterator it1 = setOfObjectProps.iterator(); it1.hasNext();) {
				OWLObjectProperty p = (OWLObjectProperty) it1.next();
				if (p.getURI().getPath().equals("/2003/11/swrl")) {
					numberOfObjectProperties--;
					totalObjProps--;
				}
					
			}
			}
			
			
			
			
			catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
            }
			/*
			 * END Rules code
			 */
			
			
			
            statsText = "OWL Ontology "+swoopModel.shortForm(ont.getLogicalURI()).toString()+NEWLINE;
            statsText += "Logical URI: "+ont.getLogicalURI().toString()+NEWLINE;
            statsText += "Physical URL: "+ont.getPhysicalURI().toString()+NEWLINE;
            statsText += HLINE;
            
            statistics.print("<br><br><b>Total Number of Classes:</b> ");
            statistics.print(String.valueOf(totalClasses));
            statistics.print("&nbsp;(Defined: "+String.valueOf(numberOfClasses) + ",&nbsp;Imported: "+String.valueOf(totalClasses-numberOfClasses)+")");
            
            statsText += "Total Number of Classes: "+String.valueOf(totalClasses)+ " (Defined: "+String.valueOf(numberOfClasses) + ", Imported: "+String.valueOf(totalClasses-numberOfClasses)+")"+NEWLINE;
            	
            statistics.print("<br><b>Total Number of Datatype Properties:</b> ");         
            statistics.print(String.valueOf(totalDataProps));
            statistics.print("&nbsp;(Defined: "+String.valueOf(numberOfDatatypeProperties) + ",&nbsp;Imported: "+String.valueOf(totalDataProps-numberOfDatatypeProperties)+")");
            
            statsText += "Total Number of Datatype Properties: "+String.valueOf(totalDataProps)+ " (Defined: "+String.valueOf(numberOfDatatypeProperties) + ", Imported: "+String.valueOf(totalDataProps-numberOfDatatypeProperties)+")"+NEWLINE;
            
            statistics.print("<br><b>Total Number of Object Properties:</b> ");
            statistics.print(String.valueOf(totalObjProps));
            statistics.print("&nbsp;(Defined: "+String.valueOf(numberOfObjectProperties) + ",&nbsp;Imported: "+String.valueOf(totalObjProps-numberOfObjectProperties)+")");
            
            statsText += "Total Number of Object Properties: "+String.valueOf(totalObjProps)+ " (Defined: "+String.valueOf(numberOfObjectProperties) + ", Imported: "+String.valueOf(totalObjProps-numberOfObjectProperties)+")"+NEWLINE;
            
            statistics.print("<br><b>Total Number of Annotation Properties:</b> ");
            statistics.print(String.valueOf(totalAnnotatedProps));
            statistics.print("&nbsp;(Defined: "+String.valueOf(numberOfAnnotationProperties) + ",&nbsp;Imported: "+String.valueOf(totalAnnotatedProps-numberOfAnnotationProperties)+")");
            
            statsText += "Total Number of Annotation Properties: "+String.valueOf(totalAnnotatedProps)+ " (Defined: "+String.valueOf(numberOfAnnotationProperties) + ", Imported: "+String.valueOf(totalAnnotatedProps-numberOfAnnotationProperties)+")"+NEWLINE;
            
            statistics.print("<br><b>Total Number of Individuals:</b> ");
            statistics.print(String.valueOf(totalIndividuals));
            statistics.print("&nbsp;(Defined: "+String.valueOf(numberOfInstances) + ",&nbsp;Imported: "+String.valueOf(totalIndividuals-numberOfInstances)+")");
            
            statsText += "Total Number of Individuals: "+String.valueOf(totalIndividuals)+ " (Defined: "+String.valueOf(numberOfInstances) + ", Imported: "+String.valueOf(totalIndividuals-numberOfInstances)+")"+NEWLINE;
            
            // Advanced Statistics using SwoopStatistics            
            HashMap advStats = swoopModel.getOntStats(swoopModel.getSelectedOntology());
            if (swoopModel.getSelectedOntology()!=null && advStats.keySet().size()>0) {
	            Set unsat = (HashSet) advStats.get(SwoopStatistics.UNSATISFIABLE_CLASSES);
            	Set gci = (HashSet) advStats.get(SwoopStatistics.NO_GCI);
	            String subsumptions = (String) advStats.get(SwoopStatistics.NO_SUBSUMPTIONS);
            	Set disj = (HashSet) advStats.get(SwoopStatistics.NO_DISJOINT);            
	            Set func = (HashSet) advStats.get(SwoopStatistics.NO_FUNCTIONAL);
	            Set ifunc = (HashSet) advStats.get(SwoopStatistics.NO_INVFUNCTIONAL);
	            Set tran = (HashSet) advStats.get(SwoopStatistics.NO_TRANSITIVE);
	            Set symm = (HashSet) advStats.get(SwoopStatistics.NO_SYMMETRIC);
	            Set inv = (HashSet) advStats.get(SwoopStatistics.NO_INVERSE);
	            List maxCList = (List) advStats.get(SwoopStatistics.MAX_DEPTH_CLASS_TREE);
	            List minCList = (List) advStats.get(SwoopStatistics.MIN_DEPTH_CLASS_TREE);
	            String avgC = "?"; 
	            if (advStats.containsKey(SwoopStatistics.AVG_DEPTH_CLASS_TREE)) avgC = advStats.get(SwoopStatistics.AVG_DEPTH_CLASS_TREE).toString();
	            
	            List maxBList = (List) advStats.get(SwoopStatistics.MAX_BRANCHING_FACTOR);
	            List minBList = (List) advStats.get(SwoopStatistics.MIN_BRANCHING_FACTOR);
	            String avgB = advStats.get(SwoopStatistics.AVG_BRANCHING_FACTOR).toString();
	            
	            List maxPList = (List) advStats.get(SwoopStatistics.MAX_DEPTH_PROP_TREE);
	            List minPList = (List) advStats.get(SwoopStatistics.MIN_DEPTH_PROP_TREE);
	            String avgP = "?"; 
	            if (advStats.containsKey(SwoopStatistics.AVG_DEPTH_PROP_TREE)) avgP = advStats.get(SwoopStatistics.AVG_DEPTH_PROP_TREE).toString();
	            Set multC = (HashSet) advStats.get(SwoopStatistics.MULTIPLE_INHERITANCE_CLASS);
	            Set multP = (HashSet) advStats.get(SwoopStatistics.MULTIPLE_INHERITANCE_PROP);
	            
	            statistics.print("<br><br><p><b>Advanced Ontology Statistics:</b></p>\n\n"); // (<a href=\"<RESTAT\">Recompute</a>)<br>");
	            statsText += HLINE + "Advanced Ontology Statistics:" + NEWLINE + HLINE;
	            statistics.print("<table border=\"1\">");	            
	            statistics.print("<tr>");
	            statistics.print("<td> General Statistics </td>");
	            statistics.print("<td> Property Tree Statistics </td>");
	            statistics.print("<td> Satisfiable Class Tree Statistics </td>");
	            statistics.print("</tr>");
	            
	            statistics.print("<tr>");	            
	            statistics.print("<td>");
	            statsText += "General Statistics" + NEWLINE;
	            if (unsat!=null && unsat.size()>0) {
	            	statistics.print("<font color=\"red\"><b>No. of Unsatisfiable Classes: <a href=\"<CSTAT:"+SwoopStatistics.UNSATISFIABLE_CLASSES+"\">"+unsat.size()+"</a></b></font>");
	            	statsText += "No. of Unsatisfiable Classes: "+unsat.size();
	            }
	            if(ont.getLinkProperties().isEmpty()){
	            	String express = swoopModel.getReasoner().getExpressivity();
	            	String exprShort = express;
	            	if (express.indexOf("<br>")>=0) exprShort = express.substring(0, express.indexOf("<br>"));
	            	statistics.print("<br>DL Expressivity: <a href=\"<DLEXP:"+express+"\">"+exprShort+"</a>");
	            	statsText += "DL Expressivity: "+exprShort+NEWLINE;
	             }
	            statistics.print("<br>No. of <i>GCIs:</i> "+gci.size());
	            statsText += "GCIs: "+gci.size()+NEWLINE;
	            statistics.print("<br>No. of <i>Sub-classes:</i> "+subsumptions);
	            statsText += "No. of Sub-class Axioms: " + subsumptions + NEWLINE;
	            statistics.print("<br>No. of <i>Disjoint Axioms:</i> <a href=\"<CSTAT:"+SwoopStatistics.NO_DISJOINT+"\">"+disj.size()+"</a>");
	            statsText += "Disjoint Axioms: "+disj.size()+NEWLINE;
	            statistics.print("<br>No. of <i>Functional</i> Properties: <a href=\"<PSTAT:"+SwoopStatistics.NO_FUNCTIONAL+"\">"+func.size()+"</a>");
	            statsText += "Functional Properties: "+func.size()+NEWLINE;
	            statistics.print("<br>No. of <i>Inverse Functional</i> Properties: <a href=\"<PSTAT:"+SwoopStatistics.NO_INVFUNCTIONAL+"\">"+ifunc.size()+"</a>");
	            statsText += "Inverse Functional Properties: "+ifunc.size()+NEWLINE;
	            statistics.print("<br>No. of <i>Transitive</i> Properties: <a href=\"<PSTAT:"+SwoopStatistics.NO_TRANSITIVE+"\">"+tran.size()+"</a>");
	            statsText += "Transitive Properties: "+tran.size()+NEWLINE;
	            statistics.print("<br>No. of <i>Symmetric</i> Properties: <a href=\"<PSTAT:"+SwoopStatistics.NO_SYMMETRIC+"\">"+symm.size()+"</a>");
	            statsText += "Symmetric Properties: "+symm.size()+NEWLINE;
	            statistics.print("<br>No. of <i>Inverse</i> Properties: <a href=\"<PSTAT:"+SwoopStatistics.NO_INVERSE+"\">"+inv.size()+"</a>");
	            statsText += "Inverse Properties: "+inv.size()+NEWLINE;
	            statistics.print("</td>");
	            
	            statistics.print("<td>");
	            statsText += HLINE+"Property Tree Statistics"+NEWLINE;
	            statistics.print("<br>Properties with <i>Multiple Inheritance</i>: <a href=\"<PSTAT:"+SwoopStatistics.MULTIPLE_INHERITANCE_PROP+"\">"+multP.size()+"</a>");
	            statsText += "Properties with Multiple Inheritance: "+multP.size()+NEWLINE;
	            statistics.print("<br><i>Max. Depth</i> of Property Tree: <a href=\"<PSTAT:"+SwoopStatistics.MAX_DEPTH_PROP_TREE+"\">"+maxPList.get(0).toString()+"</a>");
	            statsText += "Max. Depth of Property Tree: "+maxPList.get(0).toString()+NEWLINE;
	            statistics.print("<br><i>Min. Depth</i> of Property Tree: <a href=\"<PSTAT:"+SwoopStatistics.MIN_DEPTH_PROP_TREE+"\">"+minPList.get(0).toString()+"</a>");
	            statsText += "Min. Depth of Property Tree: "+minPList.get(0).toString()+NEWLINE;
	            statistics.print("<br><i>Avg. Depth</i> of Property Tree: "+avgP);
	            statsText += "Avg. Depth of Property Tree: "+avgP+NEWLINE;
	            statistics.print("</td>");
	            
	            statistics.print("<td>");
	            statsText += HLINE+"Class Tree Statistics"+NEWLINE;
	            statistics.print("<br>Classes with <i>Multiple Inheritance</i>: <a href=\"<CSTAT:"+SwoopStatistics.MULTIPLE_INHERITANCE_CLASS+"\">"+multC.size()+"</a>");
	            statsText += "Classes with Multiple Inheritance: "+multC.size()+NEWLINE;
	            statistics.print("<br><i>Max. Depth</i> of Class Tree: <a href=\"<CSTAT:"+SwoopStatistics.MAX_DEPTH_CLASS_TREE+"\">"+maxCList.get(0).toString()+"</a>");
	            statsText += "Max. Depth of Class Tree: "+maxCList.get(0).toString()+NEWLINE;
	            statistics.print("<br><i>Min. Depth</i> of Class Tree: <a href=\"<CSTAT:"+SwoopStatistics.MIN_DEPTH_CLASS_TREE+"\">"+minCList.get(0).toString()+"</a>");
	            statsText += "Min. Depth of Class Tree: "+minCList.get(0).toString()+NEWLINE;
	            statistics.print("<br><i>Avg. Depth</i> of Class Tree: "+avgC);
	            statsText += "Avg. Depth of Class Tree: "+avgC+NEWLINE;
	            statistics.print("<br><i>Max. Branching Factor</i> of Class Tree: <a href=\"<CSTAT:"+SwoopStatistics.MAX_BRANCHING_FACTOR+"\">"+maxBList.get(0).toString()+"</a>");
	            statsText += "Max. Branching Factor of Class Tree: "+maxBList.get(0).toString()+NEWLINE;
	            statistics.print("<br><i>Min. Branching Factor</i> of Class Tree: <a href=\"<CSTAT:"+SwoopStatistics.MIN_BRANCHING_FACTOR+"\">"+minBList.get(0).toString()+"</a>");
	            statsText += "Min. Branching Factor of Class Tree: "+minBList.get(0).toString()+NEWLINE;
	            statistics.print("<br><i>Avg. Branching Factor</i> of Class Tree: "+avgB);
	            statsText += "Avg. Branching Factor of Class Tree: "+avgB+NEWLINE;
	            statistics.print("</td>");
	            statistics.print("</tr>");
	            statistics.print("</table>");
	            statsText += HLINE;
            }
//            statistics.print("<FONT FACE=\""+swoopModel.getFontFace()+"\" SIZE="+swoopModel.getFontSize()+">");
            
            //***************************************************
            //Added for Econnections
            //****************************************************
            if(swoopModel.isEconnectedOntology(ont)){
              
              statistics.print("<br><br><i> This Ontology is part of an Econnection </i>");
              statistics.print("<br><b>It has links to the following Ontologies</b> ");
              Iterator iter = foreignOntologies.iterator();
              while(iter.hasNext()){
              	URI u = (URI)iter.next();
          	
              	statistics.print("<br><a href=\""+u.toString()+"\">"+swoopModel.shortForm(u)+"</a>" );
              }
              statistics.print("<br><b>Number of Defined Link Properties:</b> ");
              statistics.print(String.valueOf(numberOfLinkProperties));
              //statistics.print("<br><b>Number of Foreign Entities:</b> ");
              //statistics.print(String.valueOf(numberOfForeignEntities));
              statistics.print("<br><b>Number of Foreign Classes:</b> ");
              statistics.print(String.valueOf(numberOfForeignClasses));
              statistics.print("<br><b>Number of Foreign Properties:</b> ");
              statistics.print(String.valueOf(numberOfForeignProperties));
              statistics.print("<br><b>Number of Foreign Individuals:</b> ");
              statistics.print(String.valueOf(numberOfForeignIndividuals));
              
            }
            //*****************************************************
			//***************************************************
			//Added for Rules
			//****************************************************
			if (swoopModel.getEnableRules()) {
				RulesExpressivity rulesExpress = swoopModel.getRuleExpr();
				
				rulesExpress.setRulesExpress();			
				
				statistics.print("<br><br><b>Rules Expressivity:</b> "
						+ rulesExpress.getRulesExpress());
				statistics.print("<br><br><b>Total Number of Rules:</b> "
						+ rulesExpress.getNumRules());
				if (!(rulesExpress.getNumRules() == 0)) {
					for (int i = 0; i < 6; i++) {
						statistics.print("<br><br><b>Total Number of "
								+ rulesExpress.getTypeRulesExpress()[i]
								+ " rules </b>: "
								+ rulesExpress.getNumRulesExpress()[i]);
				}
			}
			}
        } catch(OWLException ex) {
            System.out.println(ex.getMessage());
        }

    }

    public void render(OWLOntology ont, SwoopModel swoopModel, Writer writer) {
        out = new PrintWriter(writer);
        this.OWLObjectHash = new HashMap();
        this.swoopModel = swoopModel;
        
        out.print("<html><body style=\"background-color: white; color: black;\"><FONT FACE=\""+swoopModel.getFontFace()+"\" SIZE="+swoopModel.getFontSize()+">");
        try {
			out.print("<b>OWL Ontology:</b> <a href=\""+ont.getURI()+"\">"+swoopModel.shortForm(ont.getURI())+"</a>");
			if (this.editorEnabled) out.print("&nbsp;&nbsp;(<font color=\"red\"><a href=\"<EditURL\">Edit URI</a></font>)");
			out.print("<br>");
		} catch (OWLException e) {
			e.printStackTrace();
		}
        
        
        try {
            // print annotations
        	boolean annotPrinted = this.renderAnnotations(ont);
        	if (!annotPrinted) {
        		String annTitle = " <b>Annotations</b>:";
    			if (editorEnabled) annTitle += addTitle("O-ANN");
    			out.print("<br>"+annTitle+"<br>");    			
        	}

            // print imported ontologies
            if(ont.getIncludedOntologies() != null) {
                Set inclOntSet = ont.getIncludedOntologies();
                Iterator iter = inclOntSet.iterator();
                String impStr = "<b>Imports:</b>";
                if(editorEnabled) impStr += addTitle("O-IMP");
                boolean displayImports = false;
                while(iter.hasNext()) {
                	displayImports = true;
                    OWLOntology inclOnt = (OWLOntology) iter.next();
                    URI inclOntURI = inclOnt.getURI();
                    String inclOntLbl = inclOntURI.toString(); //swoopModel.shortForm(inclOnt.getURI());
                    
                    OntologyChange oc = editorEnabled ? isDeleted(ont, inclOnt) : null;
                    boolean deleted = (oc != null);
                    if(deleted) impStr += "<font color=\"red\"><strike>";

                    impStr += "<br>" + "<a href=\"" + inclOntURI + "\">"
                            + inclOntLbl + "</a>";

                    if(deleted) {
                    	// strike out and add undo link
                    	OWLObjectHash.put(String.valueOf(oc.hashCode()), oc);
                        impStr += "</strike></font>";
                        impStr += "&nbsp;&nbsp;(<font color=\"red\"><a href=\"<UndoO-IMP:"
                                + oc.hashCode()
                                + "\">Undo</a></font>)";
                    } 
                    else if(editorEnabled) {
                    	// add delete link
                    	OWLObjectHash.put(String.valueOf(inclOnt.hashCode()), inclOnt);
                    	impStr += "&nbsp;&nbsp;(<font color=\"red\"><a href=\"<DeleteO-IMP:"
                            + inclOnt.hashCode()+ "\">Delete</a></font>)";
                    }
                            
                }
                if (editorEnabled || displayImports) out.print("<br>"+impStr);
            }
            		    
            if(editorEnabled) {
                printAddedImports(ont);
            }
            
            //*** explanations for inconsistent ontology
            SwoopReasoner reasoner = swoopModel.getReasoner();
//	        reasoner.setOntology(ont);
            if (reasoner.getOntology()!=null) {
	            if(reasoner.isConsistent()) {
	                // consistent ontology - find unsatisfiable classes
	        		OWLClass owlNothing = reasoner.getOntology().getOWLDataFactory().getOWLNothing();
	        		Set unsat = reasoner.equivalentClassesOf(owlNothing);
	        		if(!unsat.isEmpty()) {
	        		    
	                    // print dependencies between unsat. classes if debugging is enabled
	                    if (reasoner instanceof PelletReasoner && swoopModel.isDebugBlack()) {
	                    	
	                    	String plural = unsat.size() > 1 ? "es" : ""; 
	  	                    out.print("<br><br>");
	  	                    out.print("<font color=\"red\"><b>Root/Derived Debugging Information:<b><br>");
	  	                    out.print(unsat.size());
	  	                    out.print(" unsatisfiable class" + plural + ":</b></font><br>");
	                    	
	                    	PelletReasoner pellet = (PelletReasoner) reasoner;
	                    	if (pellet.depFinder==null) pellet.autoRootDiscovery();
	                    	List rootList = pellet.depFinder.rootClasses;
	                    	List derivedList = pellet.depFinder.derivedClasses;
	                    	Map dependencyMap = pellet.depFinder.dependencyMap;
	                    	
	                    	Set roots = new TreeSet(EntityComparator.INSTANCE);
	                    	roots.addAll(rootList);
	                    	Set derived = new TreeSet(EntityComparator.INSTANCE);
	                    	derived.addAll(derivedList);
	                    	
	                    	out.print("<table border=1>");
	                    	out.print("<tr>"+insFont()+"<b>root</b> unsat. classes ("+roots.size()+")");
							out.print("</tr>");
	                    	for (Iterator iter = roots.iterator(); iter.hasNext(); ) {
	                    		OWLClass root = (OWLClass) iter.next();
	                    		URI uri = root.getURI();
	                    		out.print("<tr>"+insFont());
	                    		out.print("<a href=\"" + uri + "\">" + swoopModel.shortForm(uri) + "</a> ("+ ((HashSet) pellet.depFinder.childMap.get(root)).size() +")<br>");
	                    		out.print("</tr>");
	                    	}
	                    	out.print("</table>");
	                    	
	                    	if (derived.size()>0) {
		                    	out.print("<br><table border=1>"); // font FACE=\""+swoopModel.getFontFace()+"\" SIZE="+swoopModel.getFontSize()+">");
		                    	out.print("<tr><td>"+insFont()+"<b>derived</b> unsat. classes ("+derived.size()+")"+"</td><td>"+insFont()+"<b>parent dependencies</b></td></tr>");
		                    	for (Iterator iter = derived.iterator(); iter.hasNext(); ) {
		                    		out.print("<tr><td>"+insFont());
		                    		OWLClass der = (OWLClass) iter.next();
		                    		URI uri = der.getURI();
		                    		out.print("<a href=\"" + uri + "\">" + swoopModel.shortForm(uri) + "</a>");
	
		                    		// also print dependency for derived
		                    		out.print("</td><td>"+insFont());
		                    		Set allDep = unfoldSet((HashSet) dependencyMap.get(der));
		                    		for (Iterator iter2 = allDep.iterator(); iter2.hasNext();) {
		                    			Object obj = iter2.next();
		                    			if (obj instanceof OWLClass) {
		                    				OWLClass dep = (OWLClass) obj;	                    			
			                    			URI uri2 = dep.getURI();
			                    			if (uri2.equals(uri)) continue;// dont print class itself
				                    		out.print("<a href=\"" + uri2 + "\">" + swoopModel.shortForm(uri2) + "</a>,&nbsp;");
		                    			}
		                    			else {
		                    				out.print(obj.toString()+",&nbsp;");
		                    			}
		                    		}
		                    		out.print("</td></tr");	                    		
		                    	}
		                    	out.print("</table>");		                    	
	                    	}
	                    	out.print("</FONT></body></html>");
	                    	return;
	                    }
	                    else {
		                    // in all other cases, print all unsatisfiable classes
	                    	swoopModel.getOntStats(reasoner.getOntology()).put(SwoopStatistics.UNSATISFIABLE_CLASSES, unsat);
//		                    Iterator i = unsat.iterator();
//		                    while(i.hasNext()) {
//		                        OWLClass c = (OWLClass) i.next();
//		                        URI uri = c.getURI();
//		                        out.print("<a href=\"" + uri + "\">" + swoopModel.shortForm(uri) + "</a><br>");   
//		                    }
	                    }
	        		}
	            }
	            else {
	                // inconsistent ontology
	                out.print("<br><br>");
	                out.print("<font color=\"red\"><b>Inconsistent ontology</b></font><br>");
	                if(reasoner.supportsExplanation()) {
	                    
	                	// display explanation always
	                	Reasoner pelletDebug = new Reasoner();
        				pelletDebug.setOntology(swoopModel.getSelectedOntology());
        				pelletDebug.getKB().setDoExplanation(true);
        				pelletDebug.getKB().doDependencyTracking = true;
        				pelletDebug.isConsistent();
        				
        				String explanation = ((PelletReasoner) reasoner).parseExplanation(swoopModel.getShortForms(), pelletDebug.getKB().getExplanation());
	                	
	                	// display SOS if debugging option is enabled
//	                    if (swoopModel.isDebugGlass()) {
	                    	ConciseFormatEntityRenderer cfRend = new ConciseFormatEntityRenderer();
	                    	cfRend.setSwoopModel(swoopModel);
	                    	cfRend.setReasoner(swoopModel.getReasoner());
	                    	cfRend.visitor = cfRend.createVisitor();
	                    	List MUPS = new ArrayList();
	            			List explStr = new ArrayList();
	            			List firstExpl = cfRend.getTableauSOS(swoopModel.getSelectedOntology(), null);
	            			String firstExplStr = firstExpl.get(0).toString();
	            			Set firstExplSet = (HashSet) firstExpl.get(1); 
	            			MUPS.add(firstExplSet);
	            			explStr.add(firstExplStr);
	            			if (swoopModel.isFindAllMUPS()) {
	            				// use tableau method to find SOS of inconsistent ont
	            				// since black box soln is not coded yet
	            				boolean temp = swoopModel.isUseTableau();
	            				swoopModel.useTableau = true;
	            				cfRend.HSTMUPS(firstExplSet, swoopModel.getSelectedOntology(), MUPS, explStr, new HashSet(), new HashSet());
	            				swoopModel.useTableau = temp;
	            			}
	            			Object[] explObj = explStr.toArray();
	            			int o = 0;
	            			HashMap explMap = new HashMap();
	            			Set mupsSet = new HashSet();
	            			for (Iterator iter = MUPS.iterator(); iter.hasNext();) {
	            				Set explSet = (HashSet) iter.next();
	            				mupsSet.add(explSet);
	            				if (o<explObj.length && explObj[o]!=null) {
	            					String expl = explObj[o].toString();
	            					explMap.put(explSet, expl);
	            				}
	            				o++;
	            			}
	            				            			
	            			for (Iterator iter = mupsSet.iterator(); iter.hasNext();) {
		            			Set explSet = (HashSet) iter.next();
	                    		StringWriter st = new StringWriter();
		                		PrintWriter buffer = new PrintWriter(st);
		                    	cfRend.setWriter(buffer);
		                    	cfRend.printExplanationSOS(explSet, reasoner.getOntology().getOWLDataFactory().getOWLThing(), false); 
		                    	out.print("<font face=\"Verdana\" size=2>");
		                    	if (explMap.containsKey(explSet)) out.println(explMap.get(explSet).toString());
		                    	out.print(st.toString());		                    	
	                    	}
//	                    }	                       
	                }
	            }
            }
            
            // show advanced stats
            showStatistics(ont, out);
//            System.out.println(statsText);
        } 
        catch(OWLException ex) {
            System.out.println(ex.getMessage());
        }
        out.print("</FONT></body></html>");

    }

    protected String addTitle(String param) {
        String title = "&nbsp;&nbsp;(<font color=\"red\"><a href=\"<Add" + param
                + "\">Add</a></font>)";
        return title;
    }

    public void setOptions(Map options) {
    }

    public Map getOptions() {
        return null;
    }

    public void setEditorEnabled(boolean mode) {
        this.editorEnabled = mode;
    }

	public boolean isEditableText() {
		return false;
	}
    
	
	/*
	 *  SwoopRenderer method
	 * 
	 */
	public Component getDisplayComponent( SwoopDisplayPanel panel )
	{		
		if (! (panel instanceof OntologyDisplay ))
			throw new IllegalArgumentException();
		return super.getEditorPane( this.getContentType(), (OntologyDisplay)panel );
	}
	
	
	/**
	 * Code for rendering annotations same as in ConciseFormatEntityRenderer
	 * @param obj
	 * @return
	 * @throws OWLException
	 */
	protected boolean renderAnnotations(OWLOntology obj) throws OWLException {
		
		boolean printed = false;
		if (!obj.getAnnotations().isEmpty()) {
			printed = true;
			out.print("<br>");
			String annTitle = " <b>Annotations</b>:";
			if (editorEnabled) annTitle += addTitle("O-ANN");
			out.print(annTitle);
			out.print("<br>");
			for (Iterator it = obj.getAnnotations().iterator(); it.hasNext();) {
				OWLAnnotationInstance oai = (OWLAnnotationInstance) it.next();
				
				if (editorEnabled) {
					OntologyChange deleted = isDeleted(obj, oai.getProperty(), oai.getContent());				
					if (deleted!=null) {
						out.print("<font color=\"red\"><strike>");
						out.print("  <b>" + swoopModel.shortForm(oai.getProperty().getURI()) + "</b> ");
						renderAnnotationContent(oai.getContent());
						out.print("</strike></font>");
						addUndo(deleted, "Delete");
					}
					else {
						out.print("  <b>" + swoopModel.shortForm(oai.getProperty().getURI()) + "</b> ");
						renderAnnotationContent(oai.getContent());
						addDelete(oai);
					}
				}
				else {
					out.print("  <b>" + swoopModel.shortForm(oai.getProperty().getURI()) + "</b> ");
					renderAnnotationContent(oai.getContent());
				}
				out.print("<br>");				
			}
		}
		
		// print temporarily added changes
		if (editorEnabled) {
			Set added = this.getAddedAnnotations(obj); 
			if (added.size()>0) {
				// print title if not printed already
				String annTitle = "<br> <b>Annotations</b>:";
				annTitle += addTitle("O-ANN");
				if (!printed) {
					out.print(annTitle);
					out.print("<br>");
					printed = true;
				}
				// print added changes in green (and add undo links)
				for (Iterator iter = added.iterator(); iter.hasNext(); ) {
					AddAnnotationInstance change = (AddAnnotationInstance) iter.next();
					out.print("<font color=\"green\">");
					out.print("  " + swoopModel.shortForm(change.getProperty().getURI()) + " ");
					renderAnnotationContent(change.getContent());
					out.print("</font>");
					this.addUndo(change, "Add");
					out.print("<br>");
				}
			}
		}
		return printed;
	}
	
	private Set getAddedAnnotations(OWLObject subj) throws OWLException {
		Set added = new HashSet();
		
		Iterator i = swoopModel.getUncommittedChanges().iterator();
		while(i.hasNext()) {
			OntologyChange oc = (OntologyChange) i.next();
			if (oc instanceof AddAnnotationInstance) {
				AddAnnotationInstance change = (AddAnnotationInstance) oc;
				if (change.getSubject().equals(subj)) added.add(oc); 
			}
		}
		
		return added;
	}
	
	protected void addUndo(Object obj, String type) {		
		String hash = String.valueOf(obj.hashCode());
		OWLObjectHash.put(hash, obj);
		out.print("&nbsp;&nbsp;(<font color=\"red\"><a href=\"<Undo:"+type+":"+hash+"\">Undo</a></font>)");
	}
	
	protected void renderAnnotationContent(Object o) throws OWLException {
		if (o instanceof URI) {
			out.print(o.toString());
		} else if (o instanceof OWLIndividual) {
			out.print(((OWLIndividual) o).getURI().toString());
		} else {
			String content = o.toString();
			if (o instanceof OWLDataValue) {
			
				OWLDataValue dv = (OWLDataValue) o;
				/* Only show it if it's not string */
				URI dvdt = dv.getURI();
				String dvlang = dv.getLang();
				if (dvdt != null) {
					out.print("(Datatype " + dvdt + ") ");
				} else {
					if (dvlang != null && !dvlang.equals("")) {
						out.print("(" + dvlang + ") ");
					}
				}
				content = dv.getValue().toString();
			}
//			print(escape(dv.getValue()));
//		} else {
			if (content.indexOf("html")>=0 && content.indexOf("body")>=0) {
				content = content.replaceAll("&lt;", "<");
				content = content.replaceAll("&gt;", ">");
				content = content.replaceAll("&amp;", "&");
			
				// remove <html><body> outer tags because it's being rendered inside a new html doc anyway
			
				int spos = content.indexOf("<body>")+6;
				int epos = content.indexOf("</body>");
				content = content.substring(spos, epos);
				if (content.trim().indexOf("<p")>=0) {
					content = content.substring(content.indexOf(">")+1, content.length());
				}
				content += " (**Stripped HTML tags**)";
			}
			// System.out.println(content);
			out.print(" : "+content);
		}
	}
	
	/**
	 * Check if a RemoveAnnotationInstance change is in swoopModel uncommitted changes
	 * that matches the subj, predicate and content arguments passed
	 * @param subj
	 * @param prop
	 * @param content
	 * @return
	 */
	private OntologyChange isDeleted(OWLObject subj, OWLAnnotationProperty prop, Object content) {
		OntologyChange deleted = null;
		
		Iterator i = swoopModel.getUncommittedChanges().iterator();
		while(i.hasNext()) {
			OntologyChange oc = (OntologyChange) i.next();
			
			if(oc instanceof RemoveAnnotationInstance) {
				RemoveAnnotationInstance change = (RemoveAnnotationInstance) oc;
				if (change.getSubject().equals(subj) && change.getProperty().equals(prop) && change.getContent().equals(content))
					deleted = oc;
			}
		}
		
		return deleted;
	}
	
	protected void addDelete(Object obj) {		
		String hash = String.valueOf(obj.hashCode());
		OWLObjectHash.put(hash, obj);
		out.print("&nbsp;&nbsp;(<font color=\"red\"><a href=\"<DeleteO-ANN:"+hash+"\">Delete</a></font>)");				
	}
	
	protected Set unfoldSet(Set set) {
		Set unfold = new HashSet();
		
		for (Iterator iter = set.iterator(); iter.hasNext();) {
			Object obj = iter.next();
			if (obj instanceof Set) unfold.addAll(unfoldSet((Set) obj));
			else if (obj instanceof OWLEntity) unfold.add(obj);
			else if (obj instanceof String) unfold.add(obj);
		}
		return unfold;
	}
	
	private String insFont() {
		return("<FONT FACE=\""+swoopModel.getFontFace()+"\" SIZE="+swoopModel.getFontSize()+">");
	}
}