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

package org.mindswap.swoop.utils;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import org.mindswap.pellet.KnowledgeBase;
import org.mindswap.pellet.exceptions.UnsupportedFeatureException;
import org.mindswap.pellet.query.Query;
import org.mindswap.pellet.query.QueryEngine;
import org.mindswap.pellet.query.QueryResults;
import org.mindswap.pellet.query.impl.SimpleRDQLParser;
import org.mindswap.swoop.SwoopFrame;
import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.reasoner.PelletReasoner;
import org.mindswap.swoop.renderer.ontology.OntologyListRenderer;
import org.mindswap.swoop.utils.owlapi.QNameShortFormProvider;
import org.semanticweb.owl.io.vocabulary.OWLVocabularyAdapter;
import org.semanticweb.owl.io.vocabulary.RDFSVocabularyAdapter;
import org.semanticweb.owl.io.vocabulary.RDFVocabularyAdapter;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.helper.OntologyHelper;
/*
 * Created on Mar 9, 2005
 *
 */

/**
 * @author Daniel Hewlett
 *
 */
public class QueryInterface extends JSplitPane implements ActionListener {
	static public JFrame frame;	
    
	SwoopFrame swoopHandler;
	SwoopModel swoopModel;
	
	//Font tahoma = new Font("Tahoma", Font.PLAIN, 11);
        Font tahoma = new Font("SansSerif", Font.PLAIN, 11);
	
	// main panels of splitPane
	JPanel topPanel;
	JPanel bottomPanel;
	
	// top panel
	JComboBox ontologies;
	// inside tabbedPane
	public JTextArea rdqlText;
	JButton rdqlRun;
	
	//bottom panel
	JEditorPane resultsPane;
	
	public QueryInterface(SwoopFrame swoopHandler, SwoopModel swoopModel) {
		this.swoopHandler = swoopHandler;
		this.swoopModel = swoopModel;
		this.setupUI();
	}
	
	public void setupUI() {
		this.setOrientation( JSplitPane.VERTICAL_SPLIT );
		
		topPanel = new JPanel();
		topPanel.setLayout( new BorderLayout() );
		
		bottomPanel = new JPanel();
		bottomPanel.setLayout( new BorderLayout() );
		
		JLabel ontLabel = new JLabel( "Target Ontology:" );
		ontLabel.setFont(tahoma);
		ontologies = new JComboBox();
		ontologies.setRenderer(new OntologyListRenderer(swoopModel));
		ontologies.setFont(tahoma);
		for (Iterator iter = swoopModel.getOntologies().iterator(); iter.hasNext();) {
			OWLOntology ont = (OWLOntology) iter.next();
			ontologies.addItem(ont);
		}
		ontologies.setSelectedItem(swoopModel.getSelectedOntology());
		
		rdqlText = new JTextArea();
		rdqlText.setText( createInitialRDQL() );
		rdqlText.setCaretPosition( 0 );
		
		JPanel rdqlPanel = new JPanel( new BorderLayout() );
		JLabel rdqlLabel = new JLabel( "Enter RDQL Query Below:" );
		rdqlLabel.setFont(tahoma);
		
		rdqlPanel.setLayout( new BorderLayout() );
		JScrollPane rdqlScroll = new JScrollPane( rdqlText );
		
		JPanel botRDQL = new JPanel();
		botRDQL.setLayout( new BoxLayout( botRDQL, BoxLayout.LINE_AXIS ) );
		botRDQL.setComponentOrientation( ComponentOrientation.RIGHT_TO_LEFT );
		botRDQL.setBorder( BorderFactory.createEmptyBorder( 5, 0, 0, 0 ) );
		
		rdqlRun = new JButton( "Run Query" );
		rdqlRun.setFont(tahoma);
		rdqlRun.addActionListener( this );
		botRDQL.add( rdqlRun );
		
		rdqlPanel.add( rdqlLabel, BorderLayout.NORTH );
		rdqlPanel.add( rdqlScroll, BorderLayout.CENTER );
		rdqlPanel.add( botRDQL, BorderLayout.SOUTH );
		rdqlPanel.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
		
		JTabbedPane queryTabs = new JTabbedPane();
		queryTabs.setFont(tahoma);
		queryTabs.add( rdqlPanel, "RDQL Query" );
		
		JPanel resultsPanel = new JPanel( new BorderLayout() );
		JLabel resultsLabel = new JLabel( "Query Results:" );
		resultsLabel.setFont(tahoma);
		resultsPane = new JEditorPane();
		resultsPane.setEditable( false );
		resultsPane.setContentType( "text/html" );
		resultsPane.addHyperlinkListener(swoopHandler.termDisplay);
		JScrollPane resultScroll = new JScrollPane( resultsPane );
		
		bottomPanel.add( resultsLabel, BorderLayout.NORTH );
		bottomPanel.add( resultScroll, BorderLayout.CENTER );
		bottomPanel.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
		
		JPanel top = new JPanel();
		top.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
		top.setLayout( new BoxLayout( top, BoxLayout.X_AXIS ) );
		
		top.add( ontLabel );
		top.add( ontologies );
		
		topPanel.add( top, BorderLayout.NORTH );
		topPanel.add( queryTabs, BorderLayout.CENTER );
		
		this.setTopComponent( topPanel );
		this.setBottomComponent( bottomPanel );
		this.setDividerLocation( 250 );
	}
	
/**
     * @return
 * @throws OWLException
     */
    private String createInitialRDQL() {
        String rdql = 
            	"SELECT *\n" +
        		"WHERE (?x, rdf:type, owl:Thing)\n";
        
        QNameShortFormProvider qnames = new QNameShortFormProvider();
        
        if(swoopModel.getShortForms() instanceof QNameShortFormProvider)
            qnames = (QNameShortFormProvider) swoopModel.getShortForms();

        Map map = new TreeMap();
        addPrefix(RDFVocabularyAdapter.RDF, qnames, map);
        addPrefix(RDFSVocabularyAdapter.RDFS, qnames, map);
        addPrefix(OWLVocabularyAdapter.OWL, qnames, map);
        try {
            OWLOntology sel = (OWLOntology) ontologies.getSelectedItem();
            Set onts = OntologyHelper.importClosure(sel);
            for(Iterator i = onts.iterator(); i.hasNext();) {
                OWLOntology ont = (OWLOntology) i.next();
                URI uri = ont.getURI();
                addPrefix(uri.toString(), qnames, map);
            }
        } catch(Exception e) {
            e.printStackTrace();
        } 
        
        rdql += "USING\n";
        for(Iterator i = map.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry entry = (Map.Entry) i.next();
            String prefix = (String) entry.getKey();
            String uri = (String) entry.getValue();
            
            rdql += "   " + prefix + " FOR <" + uri + ">";
            if(i.hasNext())
                 rdql += ",";
            rdql += "\n";
        }    
        
        return rdql;
    }
    
    private void addPrefix(String uri, QNameShortFormProvider qnames, Map map) {
        String[] str = {uri, uri + "#"};
         
        for(int i = 0; i < str.length; i++) {
	        String prefix = qnames.getPrefix(str[i]);
	        if(prefix != null) {
	            map.put(prefix, str[i]);
	            return;
	        }
        } 
    }

//    public static void main(String[] args) {
//        javax.swing.SwingUtilities.invokeLater(new Runnable() {
//            public void run() {
//                createAndShowGUI();
//            }
//        });
//    }
    
//    private static void createAndShowGUI() {
//    	// Use the system look and feel.
//		try {
//			UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
//		} catch ( Exception e ) {
//		}
//    	
//        //Make sure we have nice window decorations.
//        JFrame.setDefaultLookAndFeelDecorated(true);
//        JDialog.setDefaultLookAndFeelDecorated(true);
//
//        //Create and set up the window.
//        frame = new JFrame( "Pellet Query" );
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        QueryInterface qi = new QueryInterface();
//		//frame.setJMenuBar( menuBar );
//        frame.getContentPane().add( qi );
//
//        //Display the window.
//        frame.setSize( 500, 500 );
//        //frame.pack();
//        frame.setVisible(true);
//        
//        qi.rdqlText.requestFocus();
//    }

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {
		System.out.println( "Running Query..." );
		
		OWLOntology ont = (OWLOntology) ontologies.getSelectedItem();
		
		PelletReasoner reasoner;
		
		if( swoopModel.getSelectedOntology().equals( ont ) &&
		    swoopModel.getReasoner() instanceof PelletReasoner) {
		    reasoner = (PelletReasoner) swoopModel.getReasoner();
		}
		else {
		    reasoner = new PelletReasoner();
		    try {
                reasoner.setOntology( ont, false );                
            } catch(OWLException e) {
        		resultsPane.setText( "Failed to load the ontology to Pellet!");
        		
                e.printStackTrace();
                return;
            }
		}
		
        if( !reasoner.isConsistent() ) {
            resultsPane.setText( "Cannot run query on inconsistent ontologies!");
            return;
        }

		
		KnowledgeBase kb = reasoner.getKB();
        SimpleRDQLParser parser = new SimpleRDQLParser();
		
		Query query = null;
        try {
            query = parser.parse(rdqlText.getText(), kb);
        } catch(UnsupportedFeatureException e) {
    		resultsPane.setText( "Failed to parse the query: \n" + e.getMessage());

    		e.printStackTrace();
    		
    		return;
        } catch(Exception e) {
    		resultsPane.setText( "Failed to parse the query!");

    		e.printStackTrace();
    		return;
        }
        
        try {
            QueryResults results = QueryEngine.exec(query);
            
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            pw.print("<html><body><FONT FACE=\""+swoopModel.getFontFace()+"\" SIZE="+swoopModel.getFontSize()+">");
            results.toTable(true).print( pw, true );
    		pw.print("</body></html>");
    		
    		resultsPane.setText( sw.toString() );
    		resultsPane.setCaretPosition( 0 );
        } catch(Exception e) {
    		resultsPane.setText( "Failed to run the query!" );

    		e.printStackTrace();
        }		
        
	}
}

