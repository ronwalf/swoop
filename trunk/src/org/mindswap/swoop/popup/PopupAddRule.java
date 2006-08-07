/*
 * Created on Jun 7, 2005
 */


package org.mindswap.swoop.popup;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.utils.RuleValue;
import org.mindswap.swoop.utils.RulesExpressivity;
import org.semanticweb.owl.impl.rules.OWLRuleDataFactoryImpl;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.rules.OWLRule;
import org.semanticweb.owl.rules.OWLRuleAtom;
import org.semanticweb.owl.rules.OWLRuleClassAtom;
import org.semanticweb.owl.rules.OWLRuleDVariable;
import org.semanticweb.owl.rules.OWLRuleDataPropertyAtom;
import org.semanticweb.owl.rules.OWLRuleIVariable;
import org.semanticweb.owl.rules.OWLRuleObjectPropertyAtom;

/**
 * @author Daniel Hewlett
 *
 */
public class PopupAddRule extends JFrame implements ActionListener {
    SwoopModel model;    
    OWLRule rule;
    OWLRuleAtom headAtom = null;
    OWLRuleIVariable headVar = null;
    OWLRuleIVariable headVar2 = null;
    OWLRuleDVariable headDataVar = null;
    
    
    HashMap tempRuleMap = new HashMap();
    
    boolean fresh = true;
    OWLRuleDataFactoryImpl factory = new OWLRuleDataFactoryImpl();
    RulesExpressivity expr;
    int currExpr = 1;
    
    JList atomList;
    DefaultListModel lm = new DefaultListModel();
    
    JButton addRule;
    JButton cancel;
    JButton addAtom;
    JButton deleteAtom;
    JButton editAtom;
    
    JLabel consLabel;
    JLabel expLabel;
    JLabel ruleLabel;
    
    // creates a blank rule window for a new rule
    public PopupAddRule( SwoopModel mod, OWLObject head ) {
        model = mod;
        
        fresh = true;
       
        try {
	        headVar = factory.getOWLRuleIVariable( URI.create( "x" ) );
	        headVar2 = factory.getOWLRuleIVariable( URI.create( "y" ) );
	        headDataVar = factory.getOWLRuleDVariable( URI.create( "z" ) );
	        
	        if ( head instanceof OWLClass ) {
	            headAtom = factory.getOWLRuleClassAtom( (OWLClass) head, headVar );
	        } else if ( head instanceof OWLObjectProperty ) {
	            headAtom = factory.getOWLRuleObjectPropertyAtom( headVar, (OWLObjectProperty) head, headVar2 );
	        } else if ( head instanceof OWLDataProperty ) {
	            headAtom = factory.getOWLRuleDataPropertyAtom( headVar, (OWLDataProperty) head, headDataVar );
	        } else {
	            System.err.println( "wrong class in parameter" );
	        }
	        
	        Set consequents = new HashSet();
	        consequents.add( headAtom );
	        
		    rule = factory.getOWLRule( new HashSet(), consequents );
        } catch ( OWLException e ) {}
        
        expr = (RulesExpressivity) mod.getRuleExpr().clone();
        
        
        setupUI();
    }
    
    // creates a rule window populated by the current rule, for editing
    public PopupAddRule( SwoopModel mod, OWLRule rule ) {
        model = mod;
        this.rule = rule;
        fresh = false;
        
        
        
        try {
            headAtom = (OWLRuleAtom) rule.getConsequents().iterator().next();
        } 
        catch (Exception e) { 
        	System.out.println(e.getStackTrace());
        }
        
        
        expr = (RulesExpressivity) mod.getRuleExpr().clone();
        
        
        currExpr = expr.calcOneRuleExpress( rule, mod.getSelectedOntology(), false );
        
        setupUI();
    }
    
    private void setupUI() {
        atomList = new JList();
        atomList.setModel( lm );
        
        consLabel = new JLabel( headAtom.toString() );
        if ( !fresh ) {
            decodeRule();
        }
        
        JScrollPane listSP = new JScrollPane( atomList );
        listSP.setBorder( new EmptyBorder( 0, 5, 0, 5 ) );
        
        // top panel
        JPanel top = new JPanel();
        top.setLayout( new BoxLayout ( top, BoxLayout.X_AXIS ) );
        
//        try { 
//            top.add( new JLabel( "Creating Rule for " + model.shortForm( model.getSelectedEntity().getURI() ) ) );
//        } catch ( OWLException e ) {}
        top.add( consLabel );
        
        // right panel - buttons for editing atoms
        JPanel right = new JPanel();
        right.setLayout( new BoxLayout( right, BoxLayout.Y_AXIS ) );
        addAtom = new JButton( "Add Atom" );
        addAtom.addActionListener( this );
        addAtom.setMaximumSize( new Dimension( 110, 30 ) );
        deleteAtom = new JButton( "Delete Atom" );
        deleteAtom.addActionListener( this );
        deleteAtom.setMaximumSize( new Dimension( 110, 30 ) );
        editAtom = new JButton( "Edit Atom" );
        editAtom.addActionListener( this );
        editAtom.setMaximumSize( new Dimension( 110, 30 ) );
        right.add( addAtom );
        right.add( Box.createRigidArea( new Dimension( 5, 5 ) ) );
        right.add( deleteAtom );
        right.add( Box.createRigidArea( new Dimension( 5, 5 ) ) );
        right.add( editAtom );
        
        // bottom panel: contains expressivity and concise display of rule
        JPanel bottom = new JPanel();
        bottom.setLayout( new BoxLayout( bottom, BoxLayout.Y_AXIS ) );
        bottom.setBorder( new EmptyBorder( 5, 5, 5, 5 ) );
        ruleLabel = new JLabel();
        ruleLabel.setText( regenerateRuleText() );
        ruleLabel.setAlignmentX( 0 );
        expLabel = new JLabel();
        expLabel.setAlignmentX( 0 );
        if ( fresh ) {
            expLabel.setText( "Expressivity: " + "Full SWRL" );
        } else {
        	// when editing, the function below was adding extra rules
        	// decided to simply comment it out
            // updateExpressivity(); 
        	justUpdateExpressivity();
        }
        JPanel bottomButtons = new JPanel();
        
        
        if ( fresh )
            addRule = new JButton( "Add & Close" );
        else 
            addRule = new JButton( "Done" );
        addRule.addActionListener( this );
        cancel = new JButton( "Cancel" ); 
        cancel.addActionListener(this);
        bottomButtons.add( addRule );
        bottomButtons.add( cancel );
        bottomButtons.setAlignmentX( 0 );
        bottom.add( ruleLabel );
        bottom.add( expLabel );
        bottom.add( bottomButtons );
        
        Container contentPane = this.getContentPane();
        contentPane.setLayout( new BorderLayout() );
        contentPane.add( top, BorderLayout.NORTH );
        contentPane.add( listSP, BorderLayout.CENTER );
        contentPane.add( right, BorderLayout.EAST );
        contentPane.add( bottom, BorderLayout.SOUTH );
        
        this.pack();
        this.setTitle( "Editing Rule" );
        this.setSize( 400, 300 );
        
    }
    
    public void decodeRule() {
        try {
            lm.removeAllElements();
            for ( Iterator a = rule.getAntecedents().iterator(); a.hasNext(); ) {
	            OWLRuleAtom curr = (OWLRuleAtom) a.next();
	            
	            lm.addElement( curr );
	        }
	        
        } catch ( OWLException e ) {}
    }

    public void setRule( OWLRule newRule ) {
    	OWLRule oldRule = rule;
    	rule = newRule;        
        decodeRule();
        updateExpressivity(oldRule);
        this.ruleLabel.setText( regenerateRuleText() );
    }
    
    public String regenerateRuleText() {
        String text = new String();
        
        for ( int i = 0; i < lm.getSize(); i++ ) {
            if ( i > 0 )
                text = text.concat( ", " );
            text = text.concat( lm.get( i ).toString() );
        }
        
        return this.consLabel.getText() + " :- " + text;
    }
    
    public void justUpdateExpressivity() {
    	try {
    		OWLOntology ontology;
    		ontology = model.getSelectedOntology();
    		//    	 For imported ontologies
    		HashSet importedOnts = (HashSet) ontology.getIncludedOntologies();
    		// If there is no imported ontology in the rules file it assumes
    		// that rules and ontology are in the same file
    		if (!(importedOnts.isEmpty())) {
    			Iterator it = importedOnts.iterator();
    			ontology = (OWLOntology) it.next();
    		}
    		currExpr = expr.calcOneRuleExpress( rule, ontology, false );
    		
    		expLabel.setText( "Expressivity: " + expr.getTypeRulesExpress()[currExpr] );
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    public void updateExpressivity(OWLRule oldRule) {
    	try {
    		OWLOntology ontology;
    		ontology = model.getSelectedOntology();
    		//    	 For imported ontologies
    		HashSet importedOnts = (HashSet) ontology.getIncludedOntologies();
    		// If there is no imported ontology in the rules file it assumes
    		// that rules and ontology are in the same file
    		if (!(importedOnts.isEmpty())) {
    			Iterator it = importedOnts.iterator();
    			ontology = (OWLOntology) it.next();
    		}
    		currExpr = expr.calcOneRuleExpress( rule, ontology, false );
    		RuleValue ruleValue = new RuleValue(rule,
    				currExpr);
    		
    		//before adding to map, need to delete old rule		    		    
		    OWLRuleAtom consAtom = (OWLRuleAtom)oldRule.getConsequents().iterator().next();
		    OWLObject key = null;
		    
		    if (consAtom instanceof OWLRuleClassAtom) {
				key = ((OWLRuleClassAtom) consAtom).getDescription();
			} else {
				if (consAtom instanceof OWLRuleDataPropertyAtom) {
					key = ((OWLRuleDataPropertyAtom) consAtom).getProperty();
				} else {
					
					if (consAtom instanceof OWLRuleObjectPropertyAtom) {
						key = ((OWLRuleObjectPropertyAtom) consAtom)
								.getProperty();
					}
				}
			}
					    
		    HashSet rulesSet = (HashSet) expr.getRuleMap().get(key);
		    
		    //find the rule we want to delete
		    RuleValue rvDelete = null;
		    if (rulesSet != null) {
		    	Iterator it = rulesSet.iterator();		    
		    	while (it.hasNext()) {
		    		RuleValue rv = (RuleValue) it.next();
		    		if (rv.getRule().equals(oldRule)) {
		    			rvDelete = rv;
		    		}		    	
		    	}
		    	rulesSet.remove(rvDelete);
		    	
		    	expr.getRuleMap().put(key, rulesSet);
		    }
		    
    		expr.addMap(ruleValue, (OWLRuleAtom) rule.getConsequents().iterator().next());    		
    		
    		expLabel.setText( "Expressivity: " + expr.getTypeRulesExpress()[currExpr] );
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    public void addAtom( OWLRuleAtom atom ) {
        try {
            Set newAntecedents = new HashSet();
            
            if ( rule.getAntecedents() != null )
                newAntecedents.addAll( rule.getAntecedents() );
            newAntecedents.add( atom );
            
            OWLRule newRule = factory.getOWLRule( newAntecedents, rule.getConsequents() );
            System.out.println( "NEW_RULE: " + newRule );
 
            setRule( newRule );
        } catch ( OWLException e ) { e.printStackTrace(); }
        
        this.deleteAtom.setEnabled( true );
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent arg0) {
        JButton button = (JButton) arg0.getSource();
        
        if ( button == addAtom ) {
            PopupEditAtom test = new PopupEditAtom( model, rule, this );
            test.setVisible(true);
        } else if ( button == editAtom ) {
            // TODO implement this later
        } else if ( button == deleteAtom ) {
        	OWLRule oldRule = rule;
            Set newAntecedents = new HashSet();
            try {
                for ( Iterator a = rule.getAntecedents().iterator(); a.hasNext(); ) {
                   Object curr = a.next();
                   if ( !curr.equals( atomList.getSelectedValue() ) ) {
                       newAntecedents.add( curr );
                   }
                }
                
                OWLRule newRule = factory.getOWLRule( newAntecedents, rule.getConsequents() );
                System.out.println( "NEW_RULE: " + newRule );
                rule = newRule;
                                
            } catch ( OWLException e ) {}
            
            lm.remove( atomList.getSelectedIndex() );
            ruleLabel.setText( regenerateRuleText() );
            updateExpressivity(oldRule);
            
            if ( lm.getSize() == 0 ) {
                deleteAtom.setEnabled( false );
            } else {
                this.atomList.setSelectedIndex( lm.getSize() - 1 );
            }
        }
        else if (button == addRule) {
        	this.setVisible(false);
        	//commit changes to ruleExpr in swoopModel
        	model.setRuleExpr(expr);   
        	model.addUncommittedChanges(new HashSet());
        }
        else if (button == cancel) {        	
        	this.setVisible(false);        	
        }
    }
}
