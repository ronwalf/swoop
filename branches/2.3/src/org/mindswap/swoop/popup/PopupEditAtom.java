/*
 * Created on Jun 8, 2005
 */

package org.mindswap.swoop.popup;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.URI;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.renderer.SwoopCellRenderer;
import org.mindswap.swoop.utils.ui.SpringUtilities;
import org.semanticweb.owl.impl.model.OWLConnectionImpl;
import org.semanticweb.owl.impl.model.OWLDataFactoryImpl;
import org.semanticweb.owl.impl.rules.OWLRuleDataFactoryImpl;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.rules.OWLRule;
import org.semanticweb.owl.rules.OWLRuleAtom;
import org.semanticweb.owl.rules.OWLRuleIVariable;
import org.semanticweb.owl.rules.OWLRuleIndividual;


/**
 * @author Daniel Hewlett
 *
 */
public class PopupEditAtom extends JFrame implements ActionListener, ItemListener, ChangeListener 
{
	
	class OWLEntityAlphaComparator implements Comparator
	{
		public int compare(Object arg0, Object arg1) 
		{
			OWLEntity ent1 = (OWLEntity)arg0;
			OWLEntity ent2 = (OWLEntity)arg1;
			try
			{
				String ent1str = ent1.getURI().toString();
				String ent2str = ent2.getURI().toString();
				return ent1str.compareTo( ent2str );
			}
			catch( Exception e )
			{ e.printStackTrace(); }
			return -1;
		}
		
		public boolean equals( Object obj )
		{ return (this.compare( this, obj ) == 0 ); }
	}
	
    String[] types = {"Class Atom", "Object Property Atom", "Datatype Property Atom", "Individual Identity Atom"}; // built-ins not supported yet
    String swrlURI = "http://www.w3.org/2003/11/swrl#";
    
    //boolean fresh;
    boolean containsVars = false; // true if the currOnt contains SWRL variables
    SwoopModel model;
    
    int itemType = 0; // 0 = classes, 1 = objprops, 2 = dprops, 3 = sameAs/differentFrom, 4 = builtIn
    final int CLASS_ATOM_TYPE = 0; 
    final int OBJECT_PROPERTY_ATOM_TYPE = 1;
    final int DATATYPE_PROPERTY_ATOM_TYPE = 2;
    final int SAME_AS_DIFFERENT_FROM_ATOM_TYPE = 3;
    final int BUILT_IN_ATOM_TYPE = 4;
    
    JPanel formPanel = new JPanel();
    
    JComboBox atomTypeBox;
    JComboBox ontBox;
    JComboBox itemBox;
    // JComboBox var1Box;
    JComboBox ind1Box;
    // JComboBox var2Box;
    JComboBox ind2Box;
    
    JRadioButton varButton1;
    JRadioButton indButton1;
    JRadioButton varButton2;
    JRadioButton indButton2;
    
    ButtonGroup buttons1;
    ButtonGroup buttons2;
    
    JButton addButton;
    JButton addCloseButton;
    JButton cancelButton;
    
    JTextField var1TextField;
    JTextField var2TextField;
    
    JLabel itemLabel;
    JLabel atomLabel;
    
    JComboBox freeFormCombo = new JComboBox();
    JCheckBox freeFormBox   = new JCheckBox();
    
    OWLOntology currOnt;
    Set variableSet = null;
    
    DefaultComboBoxModel classMod;
    DefaultComboBoxModel propMod;
    
    PopupAddRule parent;
    OWLRule rule = null;
    OWLRuleAtom targetAtom;
    
    public PopupEditAtom( SwoopModel mod, OWLRule rule, PopupAddRule parent ) {
        model = mod;
        this.parent = parent;
        this.rule = rule;
        targetAtom = null;
        
        currOnt = model.getSelectedOntology();
        
        setupUI();
        
        fillValues();
        
        this.setSize( 400, 300 );
    }
    
    public PopupEditAtom( SwoopModel mod, OWLRule rule, PopupAddRule parent, OWLRuleAtom targetAtom ) {
        this.targetAtom = targetAtom; // TODO make this do something
        model = mod;
        this.parent = parent;
        this.rule = rule;
        
        currOnt = model.getSelectedOntology();
        
        setupUI();
        
        fillValues();
        
        this.setSize( 400, 300 );
    }
    
    private void setupUI() {
        this.setSize( 400, 300 );
        
        atomTypeBox = new JComboBox( types );
        atomTypeBox.addItemListener( this );
        ontBox = new JComboBox();
        //ontBox.setRenderer( new CellRenderer() );
        ontBox.addItemListener( this );
        itemBox = new JComboBox();
        itemBox.setRenderer( new SwoopCellRenderer( model ) );
        
        JLabel typeLabel = new JLabel( "Atom Type:" );
        JLabel ontLabel = new JLabel( "Ontology:" );
        itemLabel = new JLabel( "Class:" );
        
        freeFormCombo.setEditable( true );
        freeFormCombo.setSelectedItem( "" );
        
        formPanel.setLayout( new SpringLayout() );
        formPanel.add( typeLabel );
        formPanel.add( atomTypeBox );

        formPanel.add( ontLabel );
        formPanel.add( ontBox );

        freeFormBox.addActionListener( this );
        formPanel.add( new JLabel("Freeform:") );
        formPanel.add( freeFormBox );
        formPanel.add( itemLabel );
        formPanel.add( itemBox );
        
        SpringUtilities.makeCompactGrid(formPanel, //parent
                4, 2,
                5, 5,  //initX, initY
                5, 5); //xPad, yPad
        
        JPanel vPan1 = new JPanel();
        vPan1.setLayout( new SpringLayout() );
        vPan1.setBorder( new LineBorder( Color.BLACK, 1 ) );
        // var1Box = new JComboBox();
        //var1Box.setEditable( true );
        // var1Box.setRenderer( new VariableRenderer( model ) );
        ind1Box = new JComboBox();
        ind1Box.setRenderer( new SwoopCellRenderer( model ) );
        varButton1 = new JRadioButton( "Variable" );
        indButton1 = new JRadioButton( "Individual" );
        
        vPan1.add( varButton1 );
        // vPan1.add( var1Box );
        var1TextField = new JTextField();
        vPan1.add(var1TextField);
        
        vPan1.add( indButton1 );
        vPan1.add( ind1Box );

        buttons1 = new ButtonGroup();
        buttons1.add( varButton1 );
        buttons1.add( indButton1 );
        buttons1.setSelected( varButton1.getModel(), true );
        
        
        
        JPanel vPan2 = new JPanel();
        vPan2.setLayout( new SpringLayout() );
        vPan2.setBorder( new LineBorder( Color.BLACK, 1 ) );
        
        // var2Box = new JComboBox();
        //var2Box.setRenderer( new VariableRenderer( model ) );
        ind2Box = new JComboBox();
        ind2Box.setRenderer( new SwoopCellRenderer( model ) );
        varButton2 = new JRadioButton( "Variable" );
        indButton2 = new JRadioButton( "Individual" );
 
        vPan2.add( varButton2 );
        //vPan2.add( var2Box );
        var2TextField = new JTextField();
        vPan2.add(var2TextField);
        
        vPan2.add( indButton2 );
        vPan2.add( ind2Box );
        
        buttons2 = new ButtonGroup();
        buttons2.add( varButton2 );
        buttons2.add( indButton2 );
        buttons2.setSelected( varButton2.getModel(), true );


        
        addButton = new JButton( "Add" );
        addButton.addActionListener( this );
        addCloseButton = new JButton( "Add & Close" );
        addCloseButton.addActionListener( this );
        cancelButton = new JButton( "Cancel" );
        cancelButton.addActionListener( this );
        
        JPanel bottom = new JPanel();
        //bottom.setLayout( new BoxLayout( bottom, BoxLayout.X_AXIS ) );
        //bottom.setComponentOrientation( ComponentOrientation.RIGHT_TO_LEFT );
        bottom.setBorder( new EmptyBorder( 5, 5, 5, 5 ) );
        
        bottom.add( addCloseButton );
        bottom.add( addButton );
        bottom.add( cancelButton );
        
        JPanel main = new JPanel();
        
        main.setLayout( new BoxLayout( main, BoxLayout.Y_AXIS ) );
        
        main.add( formPanel );
        main.add( vPan1 );
        main.add( vPan2 );
        
        SpringUtilities.makeCompactGrid(vPan1, //parent
                2, 2,
                5, 5,  //initX, initY
                5, 5); //xPad, yPad
        
        SpringUtilities.makeCompactGrid(vPan2, //parent
                2, 2,
                5, 5,  //initX, initY
                5, 5); //xPad, yPad
        
        JPanel labelPane = new JPanel();
        JLabel atomLabel = new JLabel( "Atom: " );
        labelPane.add( atomLabel );
        JLabel atomText = new JLabel();
        labelPane.add( atomText );
        
        Container pane = this.getContentPane();
        pane.setLayout( new BorderLayout() );
        pane.add( main, BorderLayout.NORTH );
        pane.add( labelPane, BorderLayout.CENTER );
        pane.add( bottom, BorderLayout.SOUTH );
        
        this.setSecondArgumentEnabled( false );
        
        this.setTitle( "Editing Rule Atom" );
        
        this.pack();
        

    }
    
    public void refreshClassesAndProps()  {
        itemBox.setEnabled( true );
        
        try {
            ((DefaultComboBoxModel) this.itemBox.getModel()).removeAllElements();
            
	        if ( itemType == 0 ) {
	            classMod = (DefaultComboBoxModel) this.itemBox.getModel();
	            classMod.removeAllElements();
	            Set classSet = currOnt.getClasses();	            
	            // create a vector for sorting
	            Vector vector = new Vector( classSet );
	            Collections.sort( vector, new OWLEntityAlphaComparator() );
	            
		        for ( Iterator c = vector.iterator(); c.hasNext(); ) {
		            OWLClass curr = (OWLClass) c.next();
		            
		            if ( !curr.getURI().toString().startsWith( swrlURI ) ) 
		                classMod.addElement( curr );
		        }
	        } else if ( itemType == 1 ) {
		        propMod = (DefaultComboBoxModel) this.itemBox.getModel();
		        propMod.removeAllElements();
	            Set propSet = currOnt.getObjectProperties();
	            // create a vector for sorting
	            Vector vector = new Vector( propSet );
	            Collections.sort( vector, new OWLEntityAlphaComparator() );
	            
		        for ( Iterator p = vector.iterator(); p.hasNext(); ) {
		            OWLObjectProperty curr = (OWLObjectProperty) p.next();
		            
		            if ( !curr.getURI().toString().startsWith( swrlURI ) ) 
		                propMod.addElement( curr );
		        }
	        } else if ( itemType == 2 ) {
	            propMod = (DefaultComboBoxModel) this.itemBox.getModel();
	            propMod.removeAllElements();
	            Set propSet = currOnt.getDataProperties();
	            // create a vector for sorting
	            Vector vector = new Vector( propSet );
	            Collections.sort( vector, new OWLEntityAlphaComparator() );
	            
		        for ( Iterator p = propSet.iterator(); p.hasNext(); ) {
		            OWLDataProperty curr = (OWLDataProperty) p.next();
		            
		            if ( !curr.getURI().toString().startsWith( swrlURI ) ) 
		                propMod.addElement( curr );
		        } 
	        }
	        
        } catch ( OWLException e ) {}
        
        if ( itemBox.getItemCount() == 0 ) {
            ((DefaultComboBoxModel) itemBox.getModel()).addElement( "No entities found in ontology." );
            itemBox.setEnabled( false );
        }
    }
    
    public void fillValues()  {
        
        DefaultComboBoxModel cbm = (DefaultComboBoxModel) ontBox.getModel();
        for ( Iterator i = model.getOntologies().iterator(); i.hasNext(); ) {
            try {
                cbm.addElement( ((OWLOntology) i.next()).getURI() );
            } catch (OWLException e) {}
        }
        
        DefaultComboBoxModel ind1 = (DefaultComboBoxModel) ind1Box.getModel();
        DefaultComboBoxModel ind2 = (DefaultComboBoxModel) ind2Box.getModel();
        try {
            for ( Iterator i = currOnt.getIndividuals().iterator(); i.hasNext(); ) {
                OWLIndividual curr = (OWLIndividual) i.next();
                
                if ( containsVars ) {
                    if ( !variableSet.contains( curr ) && !curr.isAnonymous() ) {
                        ind1.addElement( curr );
                        ind2.addElement( curr ); 
                    }
                } else if ( !curr.isAnonymous() ) {
                    ind1.addElement( curr );
                    ind2.addElement( curr );
                }
            }
            
        } catch ( OWLException e ) {}
        
        refreshClassesAndProps();
    }
    
    public void setSecondArgumentEnabled( boolean enabled ) {
        indButton2.setEnabled( enabled );
        varButton2.setEnabled( enabled );
        ind2Box.setEnabled( enabled );
        //var2Box.setEnabled( enabled );
        var2TextField.setEnabled(enabled);
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent arg0) {

    	try {
    	
    	if ( arg0.getSource() == freeFormBox )
    	{
    		boolean isFree = freeFormBox.getModel().isSelected();
    		if ( isFree )
    		{
    			System.out.println( "adding freeFormCombo");
    			formPanel.remove( itemBox );
    			formPanel.add( freeFormCombo );
    	        SpringUtilities.makeCompactGrid(formPanel, //parent
    	                4, 2,
    	                5, 5,  //initX, initY
    	                5, 5); //xPad, yPad
    	        formPanel.repaint();
    			formPanel.revalidate();
    		
    		}
    		else
    		{
    			System.out.println( "adding itemBox");
    			formPanel.remove( freeFormCombo );
    			formPanel.add( itemBox );
    	        SpringUtilities.makeCompactGrid(formPanel, //parent
    	                4, 2,
    	                5, 5,  //initX, initY
    	                5, 5); //xPad, yPad
    	        formPanel.repaint();
    			formPanel.revalidate();
    		}
    		return;
    	}
        if ( arg0.getSource() == addButton || arg0.getSource() == addCloseButton ) {
            int index = atomTypeBox.getSelectedIndex();
            OWLDataFactoryImpl fac = OWLDataFactoryImpl.getInstance( new OWLConnectionImpl() );

            boolean isFreeform = freeFormBox.getModel().isSelected();
            
            OWLRuleDataFactoryImpl test = new OWLRuleDataFactoryImpl();
            OWLRuleAtom atom = null;
            OWLOntology ontology = model.getOntology( (URI) ontBox.getModel().getSelectedItem());
            
            // make new class atom
            if ( index == CLASS_ATOM_TYPE ) {
                try 
				{
                	OWLRuleIVariable var1 = test.getOWLRuleIVariable( URI.create(var1TextField.getText()));
                    if ( buttons1.getSelection().equals( varButton1.getModel() ) ) // variable 
                    {
                    	if ( isFreeform )
                    	{
                    		URI uri =  new URI((String)freeFormCombo.getSelectedItem());
                    		atom = test.getOWLRuleClassAtom( fac.getOWLClass( uri ), var1 );
                    	}		
                    	else
                    		atom = test.getOWLRuleClassAtom( (OWLClass) itemBox.getSelectedItem(), var1 );
                    } 
                    else  // individual
                    {
                		OWLRuleIndividual ind1 = test.getOWLRuleIndividual( (OWLIndividual) this.ind1Box.getSelectedItem() );
                    	if ( isFreeform )
                    	{
                    		URI uri =  new URI((String)freeFormCombo.getSelectedItem());
                    		atom = test.getOWLRuleClassAtom( fac.getOWLClass( uri ), ind1 );
                    	}		
                    	else
                    		atom = test.getOWLRuleClassAtom( (OWLClass) itemBox.getSelectedItem(), ind1 );
                    }
                    System.out.println( atom );
            	
                } catch ( OWLException e ) {}
                
            } 
            else if ( index == OBJECT_PROPERTY_ATOM_TYPE )
            {                
                if ( buttons1.getSelection().equals( varButton1.getModel() ) ) // first arg is var 
                {
                	OWLRuleIVariable var1 = test.getOWLRuleIVariable( URI.create(var1TextField.getText()));               
                    if ( buttons2.getSelection().equals( varButton2.getModel() ) ) // second arg is var
                    {
                		OWLRuleIVariable var2 = test.getOWLRuleIVariable(URI.create(var2TextField.getText()) );
                    	if ( isFreeform )
                    	{
                    		URI uri =  new URI((String)freeFormCombo.getSelectedItem());
                    		atom = test.getOWLRuleObjectPropertyAtom( var1, fac.getOWLObjectProperty( uri ), var2  );	
                    	}
                    	else
                    		atom = test.getOWLRuleObjectPropertyAtom( var1, (OWLObjectProperty) itemBox.getSelectedItem(), var2  );
                    } 
                    else // second arg is ind
                    {
                		OWLRuleIndividual ind2 = test.getOWLRuleIndividual( (OWLIndividual) this.ind2Box.getSelectedItem() );
                    	if ( isFreeform )
                    	{
                    		URI uri =  new URI((String)freeFormCombo.getSelectedItem());
                    		atom = test.getOWLRuleObjectPropertyAtom( var1, fac.getOWLObjectProperty( uri ), ind2  );
                    	}
                    	else
                    		atom = test.getOWLRuleObjectPropertyAtom( var1, (OWLObjectProperty) itemBox.getSelectedItem(), ind2  );
                    }
                }
                else // first arg is ind
                {
                    OWLRuleIndividual ind1 = test.getOWLRuleIndividual( (OWLIndividual) this.ind1Box.getSelectedItem() );                    
                    if ( buttons2.getSelection().equals( varButton2.getModel() ) ) // second arg is var
                    {
                		OWLRuleIVariable var2 = test.getOWLRuleIVariable( URI.create(var2TextField.getText()) );
                    	if ( isFreeform )
                    	{
                    		URI uri =  new URI((String)freeFormCombo.getSelectedItem());
                    		atom = test.getOWLRuleObjectPropertyAtom( ind1, fac.getOWLObjectProperty( uri ), var2  );
                    	}
                    	else
                    		atom = test.getOWLRuleObjectPropertyAtom( ind1, (OWLObjectProperty) itemBox.getSelectedItem(), var2  );
                    }
                    else // second arg is ind
                    {
                		OWLRuleIndividual ind2 = test.getOWLRuleIndividual( (OWLIndividual) this.ind2Box.getSelectedItem() );
                    	if ( isFreeform )
                    	{
                    		URI uri =  new URI((String)freeFormCombo.getSelectedItem());
                    		atom = test.getOWLRuleObjectPropertyAtom( ind1, fac.getOWLObjectProperty( uri ), ind2  );                    		
                    	}
                    	else
                    		atom = test.getOWLRuleObjectPropertyAtom( ind1, (OWLObjectProperty) itemBox.getSelectedItem(), ind2  );
                    }
                }
            }
            
            System.out.println( atom );
            
//          adding the atom to the rule
            
            parent.addAtom( atom );
            
            if ( arg0.getSource() == addCloseButton ) {
                this.setVisible(false);
            } else {
                
            }
        } else if ( arg0.getSource() == cancelButton ) {
            this.setVisible(false);
        } 
    	 } catch ( Exception e ) {}
    }

    /* (non-Javadoc)
     * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
     */
    public void itemStateChanged(ItemEvent arg0) {
        if ( arg0.getSource() == ontBox ) {
            currOnt = model.getOntology( (URI) ontBox.getModel().getSelectedItem() );
            
            // find variable class
            containsVars = false;
            variableSet = null;
            try {
	            for ( Iterator c = currOnt.getClasses().iterator(); c.hasNext(); ) {
	                OWLClass curr = (OWLClass) c.next();
	                if ( curr.getURI().equals( URI.create( swrlURI + "Variable" ) ) ) {
	                    System.out.println( "Variable found" );
	                    
	                    containsVars = true;
	                    
	                    variableSet = model.getReasoner().allInstancesOf( curr );
	                    for ( Iterator v = variableSet.iterator(); v.hasNext(); ) {
	                    	
	                        OWLIndividual currVar = (OWLIndividual) v.next();
	                        
	                        /*
	                        ((DefaultComboBoxModel) var1Box.getModel()).addElement( currVar );
	                        ((DefaultComboBoxModel) var2Box.getModel()).addElement( currVar );
	                        (*/
	                    }
	                    
	                    break;
	                }
	            }
	            
            } catch ( Exception e ) {}
            
            refreshClassesAndProps();
            
        } else if ( arg0.getSource() == this.atomTypeBox ) {
            int index = atomTypeBox.getSelectedIndex();
            
            this.itemType = index;
            
            refreshClassesAndProps();
            
            if ( index == 0 ) {
                this.itemLabel.setText( "Class:" );
                this.setSecondArgumentEnabled( false );
            } else {
                this.itemLabel.setText( "Property:" );
                this.setSecondArgumentEnabled( true );
            }

        } 
    }

    /* (non-Javadoc)
     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
     */
    public void stateChanged(ChangeEvent arg0) {
       
    }

}
