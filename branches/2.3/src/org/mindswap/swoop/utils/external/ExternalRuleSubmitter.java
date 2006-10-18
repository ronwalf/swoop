/*
 * Created on Jul 5, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.utils.external;

import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.mindswap.swoop.SwoopModel;
import org.semanticweb.owl.rules.OWLRule;


/**
 * @author Dave Wang
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ExternalRuleSubmitter extends JFrame implements ActionListener, ItemListener
{

	class SubmissionProfile
	{
		public String name;
		public String endpoint;
		public String authenticationPoint;
		public String [] options = {};
		public String [] predObjAsserts = {};
		public HashMap optionToAsserts = new HashMap();
		
		public SubmissionProfile( String aName, String aURI, String aPoint, String [] opts, String [] predObjAss )
		{
			this.name = aName;
			this.endpoint  = aURI;
			this.authenticationPoint = aPoint;
			if ( opts != null)
				this.options = opts;
			if ( predObjAss!= null)
				this.predObjAsserts = predObjAss;
			for ( int i = 0; i < options.length; i++ )
			{ optionToAsserts.put( options[i], predObjAsserts[i] ); }			
		}
		
		public String toString()
		{ return name; }
		
		
	}
	
	private SubmissionProfile PIT = new SubmissionProfile("Profiles in Terror", "http://profilesinterror.mindswap.org/document/accept/", null, null, null );
	private SubmissionProfile PP = new SubmissionProfile("PaperPuppy", "http://paperpuppy.mindswap.org/document/accept/", "http://paperpuppy.mindswap.org/document/accept/check_user", new String[]{"is a Display Rule?"}, new String[]{"<http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.mindswap.org/dav/ontologies/2006/paperpuppy.owl#GraphRule>"} );
	
	private static final boolean DEBUG = false;
	
	private JTextField userIDField = new JTextField();
	private JPasswordField passwordField = new JPasswordField();
	private JComboBox  destinationCombo = new JComboBox();
	
	private JLabel submitTo  = new JLabel("Submit to");
	private JLabel userID    = new JLabel("User ID");
	private JLabel password  = new JLabel("Password");
	
	private JButton submitButton = new JButton("Submit");
	private JButton cancelButton = new JButton("Cancel");
	private JPanel  optionalPanel = new JPanel();
	private Vector checkBoxes;
	
	private SwoopModel myModel;
	private OWLRule myRule;

	public ExternalRuleSubmitter( SwoopModel model, OWLRule rule ) 
	{
		super();
		myModel = model;
		myRule = rule;
		setupUI();
		setVisible( true );
	}
	
	private void setupUI()
	{
		this.setLocation( 200, 300 );
		this.setTitle("Submitting Rule to External Portals");
		this.setSize( 300, 100 );
		//this.setResizable( false );
		destinationCombo.addItem( PIT );
		destinationCombo.addItem( PP );
		JPanel top = new JPanel( new GridLayout(3, 1) );
		JPanel p1 = new JPanel( new GridLayout(1, 2));
		p1.add( submitTo );
		p1.add( destinationCombo );
		
		JPanel p2 = new JPanel( new GridLayout(1, 2));
		p2.add( userID );
		p2.add( userIDField );
		
		JPanel p3 = new JPanel( new GridLayout(1, 2));
		p3.add( password );
		p3.add( passwordField );
		
		top.add( p1 );
		top.add( p2 );
		top.add( p3 );
		
		JPanel bot = new JPanel( new GridLayout(1, 2) );
		bot.add( submitButton );
		bot.add( cancelButton );
		
		Container contentPane = this.getContentPane();
		contentPane.setLayout( new BoxLayout( contentPane, BoxLayout.Y_AXIS ) );
		contentPane.add( top );
		contentPane.add( optionalPanel );
		contentPane.add( bot );
		
		submitButton.addActionListener( this );
		cancelButton.addActionListener( this );
		destinationCombo.addItemListener( this );
	}
	
	public void submit( String uri, String user ) throws java.io.IOException, Exception 
	{	
		String n3 = myModel.getRuleExpr().toN3( myRule );
		String label = myModel.getRuleExpr().getLabel( myRule );
		String predicate = myModel.getRuleExpr().getPredicate( myRule );
		
		SubmissionProfile profile = (SubmissionProfile)destinationCombo.getSelectedItem();
		for ( Iterator iter = checkBoxes.iterator(); iter.hasNext(); )
		{			
			JCheckBox box = (JCheckBox)iter.next();
			if ( box.getModel().isSelected())
			{
				//System.out.println( box.getText() + " is enabled.");
				String predObjAssertion = (String)profile.optionToAsserts.get( box.getText() );
				n3 = n3 + "\n<"+predicate + "> " +predObjAssertion + " .";
			}
		}
		
		//System.out.println("n3 = " + n3);
		String encodedrdf = URLEncoder.encode(n3, "UTF-8");
		
		try {
			URL url = new URL(uri);
			URLConnection connection = url.openConnection();
			
			connection.setDoOutput(true);
			PrintWriter out = new PrintWriter(connection.getOutputStream());
			out.println("n3blob=" + encodedrdf);	
			out.println("&inputtype=1");
			out.println("&op=Submit");
			out.println("&label=" + label);
			out.println("&creator=" + user);
			out.flush();
			out.close();
			connection.connect();
			
			// mConn.connect();
            InputStream is = connection.getInputStream();

            BufferedReader in = new BufferedReader(new InputStreamReader(is));

            String output = "";
            String line;

            while ( (line = in.readLine()) != null) {
                output += line;
                if (DEBUG)
                    System.err.println(line);
            }
		}
		catch (Exception e) {
			System.out.println("ERROR!");
			System.out.println(e.getMessage());
		}					
	}


	public void actionPerformed(ActionEvent e) 
	{	
		Object src = e.getSource();
		if ( src == submitButton )
		{
			SubmissionProfile profile = (SubmissionProfile)destinationCombo.getSelectedItem();
			String uri = profile.endpoint;
			String authPoint = profile.authenticationPoint;
			String user = userIDField.getText();
			String passwd = new String( passwordField.getPassword());
			try
			{
				URL url = new URL( authPoint + "?username=" + user + "&password=" + passwd );
				BufferedReader reader = new BufferedReader( new InputStreamReader(url.openStream()) );
				String line = "";
				String text = "";
				while ( ( line = reader.readLine() ) != null )
				{ text = text + line; }
				reader.close();				
				
				submit( uri, user  ); 
				JOptionPane.showMessageDialog(null, "Submission accepted", "Success", JOptionPane.INFORMATION_MESSAGE);
				this.setVisible( false );
				this.dispose();
			}
			catch ( Exception ex )
			{
				ex.printStackTrace();
				if ( ex instanceof java.io.IOException )
				{
					String msg = ex.getMessage();
					if ( msg.indexOf( "HTTP response code: 401") != -1 )
						JOptionPane.showMessageDialog(null, "Invalid Username/Password", "Invalid Username/Password", JOptionPane.ERROR_MESSAGE);
					// if site does not support authentication, allow submission
					else if ( msg.indexOf( "HTTP response code: 404") !=-1 )
					{ 
						try
						{
							submit( uri, user  ); 
							JOptionPane.showMessageDialog(null, "Submission accepted", "Success", JOptionPane.INFORMATION_MESSAGE);
						}
						catch ( Exception exc )
						{ exc.printStackTrace(); }						
					}
				}
			}			
		}
		else if (src == cancelButton )
		{			
			this.setVisible( false );
			this.dispose();
		}
	}

	public void itemStateChanged(ItemEvent e) 
	{
		if ( e.getStateChange() == ItemEvent.SELECTED )
		{
			SubmissionProfile profile = (SubmissionProfile)e.getItem();
			optionalPanel.removeAll();
			String [] opts = profile.options ;
			JPanel panel = new JPanel();
			checkBoxes = new Vector();
			panel.setLayout( new GridLayout(opts.length, 2) );
			for (int i = 0; i < opts.length; i++ )
			{					
				String name = opts[i];
				JCheckBox check = new JCheckBox( name ); 
				panel.add( check );
				checkBoxes.add( check );
			}
			optionalPanel.add( panel );
			optionalPanel.revalidate();
			this.pack();
		}
	}	
}
