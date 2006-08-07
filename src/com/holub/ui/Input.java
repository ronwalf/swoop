// (c) 2003 Allen I Holub. All rights reserved.
package com.holub.ui;

import com.holub.ui.NumericInput;// for testing

import java.util.logging.*;
import javax.swing.*;
import javax.swing.Timer;	// disambiguate from java.util.Timer
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;

import com.holub.tools.Log;

/* Demonstrate a JComponent-style proxy.
 *  <p>
 * This class is
 * a validating Text field. The first time you type something
 * invalid, an tool tip pops up describing what correct
 * input is. Subsequent errors just beep at you, but the
 * tooltip will continue to pop up if the mouse hovers.
 *
 * <!-- ====================== distribution terms ===================== -->
 * <p><blockquote
 * 	style="border-style: solid; border-width:thin; padding: 1em 1em 1em 1em;">
 * <center>
 * 			Copyright &copy; 2003, Allen I. Holub. All rights reserved.
 * </center>
 * <br>
 * <br>
 * 		This code is distributed under the terms of the
 * 		<a href="http://www.gnu.org/licenses/gpl.html"
 * 		>GNU Public License</a> (GPL)
 * 		with the following ammendment to section 2.c:
 * 		<p>
 * 		As a requirement for distributing this code, your splash screen,
 * 		about box, or equivalent must include an my name, copyright,
 * 		<em>and URL</em>. An acceptable message would be:
 * <center>
 * 		This program contains Allen Holub's <em>XXX</em> utility.<br>
 * 				(c) 2003 Allen I. Holub. All Rights Reserved.<br>
 * 						http://www.holub.com<br>
 * </center>
 * 		If your progam does not run interactively, then the foregoing
 * 		notice must appear in your documentation.
 * </blockquote>
 * <!-- =============================================================== -->
 * @see DateInput
 * @see NumericInput
 * @author Allen I. Holub
 */

public class Input extends JTextField implements Styles
{

	private final Customizer customizer;

	private String lastValid;	 // Useful only in on-exit-style validation.
								 // holds the last-known valid contents
								 // of the control.

	private boolean valid = true; // Used for communication between
	private int		offset;		  // the Document event handler and the
	private int		length;		  // UI Delegate update code.

	private Popup popup = null; // Popup window used for "help,"
								// is null unless window is visible.

	private static int POPUP_LIFETIME = 15;	// Maximum lifetime of popup
										    // window in seconds.

	/** Provides information about, defines nonstandard initial
	 *  state for, and validates user input.
	 *  The predefined {@link NumericInput} class implements
	 *  this interface for generic numbers, and the predefined
	 *  {@link Input.Default} class implements it for
	 *  a default, non-validating text control.
	 */

	public interface Customizer
	{
		/** Called by the Input object to determine whether validation
		 *  is performed (by calling {@link #isValid}) after every
		 *  character is entered or when the user hits enter (or the
		 *  input object looses focus)
		 *  <p>
		 *  Note that on-exit validation causes validation to occur
		 *  in two situations: the user hits Enter or the control
		 *  looses focus. The Control won't let the focus change
		 *  occur if the contents don't validate. However the Esc
		 *  character is recognized as a reset-to-original-value
		 *  request, so you can exit the field if you want to.
		 *  The string ("Type Esc to exit") is automatically appended
		 *  to the error message in this mode.
		 *
		 *  @return false for character-by-character validation, true for
		 *  		one-time validation on loss of focus or Enter.
		 */
		boolean validatesOnExit();

		/** Return true if the string is valid input. This method
		 *  is called either on exiting the control or after every
		 *  character is typed, depending on the return value of
		 *  {@link #validatesOnExit}. Reguardless of the
		 *  "validate-on-exit" mode, an hitting Esc alwasy resets
		 *  the control to the last-known valid value.
		 *  <p>
		 *  This method is also called when the control is initialized,
		 *  and the constructor will throw an exception if the initial
		 *  value is not valid.
		 *
		 *  @param s The entire contents of the control, including any
		 *  		 characters the user just entered.
		 */
		boolean isValid( String s );

		/** Return a description of what valid input looks likes.
		 *  The string should be HTML, however the main context
		 *  &lt;html&gt;, &lt;head&gt;, and &lt;body&gt; elements are already
		 *  established, so these tags should not appear in
		 *  your own text.
		 *  @param badInput This is the string that the user tried to type.
		 *  				The control rejects the bad input, so this
		 *  				string is not displayed. You can put it into
		 *  				your error message if you like, however.
		 *  @return a help string or null if no help is avilable.
		 */
		String help( String badInput );

		/** Set up the look of the component for stuff not covered
		 *  by the Style class. (For example, alignment and tool-tip
		 *  text). This method after all initializations (including
		 *  the text entry) have been made to the component.
		 *  customizations have been made.
		 */
		void prepare( JTextField current );

	}

	/** An implemenation of Customizer that defines default behavior:
	 *  All input is valid; there is no help; The prepare
	 *  method makes the control 30 columns wide.
	 *  You can extend this class if you just want
	 *  to override one of the methods, much like an
	 *  AWT <em>Xxx</em><code>Adapter</code>.
	 */

	static public class Default implements Customizer
	{	public boolean isValid(String s){return true;}
		public String help(String s) { return null; }
		public void prepare( JTextField current )
		{	current.setColumns(30);
		}
		public boolean validatesOnExit(){ return true; }
	};

	/** A constrained type to describe the border style you want.
	 *  One of the predefined instances Input.BOXED, Input.UNDERLINED,
	 *  or Input.BORDERLESS, must be passed to the
	 *  {@linkplain Input#Input <code>Input</code> constructor}.
	 */

	public static final class BorderStyle{ private BorderStyle(){} }
	public static final BorderStyle BORDERLESS = null;
	public static final BorderStyle BOXED 		= new BorderStyle();
	public static final BorderStyle UNDERLINED = new BorderStyle();

	/** Construct a validating input field. Works like a JTextField,
	 *  but checks the input as it's typed and complains
	 *  if the input is invalid. Also implements the TagBehavior
	 *  interface, so can be used in a PAC system as a stand-in
	 *  for an &lt;input&gt; tag.
	 *  <p>
	 *  The default width of the control is determined by the width of the
	 *  <code>value</code> string. You can change the width from the
	 *  default by calling {@link javax.swing.JTextField#setColumns} after
	 *  you create the object. Unlike the standard JTextField, the maximum
	 *  and minimum widths are constrained to the initial size. (This
	 *  constraint is required for the control to work correctly inside
	 *  a {@link com.holub.ui.HTML.HtmlPane}, which is it's raison d'etre.)
	 *
	 *  @param value initial value.
	 *  @param customizer checks to see if the input is valid. A null argument
	 *  			is treated as if you had passed an {@link Default} object.
	 *  @param border one of BORDERLESS, BOXED, or UNDERLINED.
	 *  @param isHighlighted if true, highlight the text in the Style.HIGHLIGHT_COLOR
	 *
	 *  @throws IllegalArgumentException if the customizer indicates that the
	 *  		initial <code>value</code> is invalid.
	 */
	public Input(String value, Customizer customizer, final BorderStyle border, final boolean isHighlighted)
	{
		this.customizer	= (customizer != null)? customizer: new Default();

		if( !customizer.isValid(value) )
			throw new IllegalArgumentException("Customizer rejected initial value ["+ value +"]");

		lastValid = value;

		setFont( FONT );
		if( isHighlighted )
			setForeground( Color.RED ); // Affects border color too.

		if( border==BOXED )
		{	Border outer = BorderFactory.createLineBorder( Color.BLACK, 1 );
			Border inner = BorderFactory.createEmptyBorder( 0, 4, 0, 4 );

			setBorder( BorderFactory.createCompoundBorder(outer, inner) );
		}
		else if( border==UNDERLINED )
		{	setBorder
			(	new AbstractBorder()
				{	public void paintBorder(Component c, Graphics g,
										int x, int y, int width, int height)
					{	g.drawLine( x, y+height-1, x+width-1, y+height-1 );
					}
				}
			);
		}
		else	// BORDERLESS
		{   setBorder( null );	// no border
		}

		setColumns( value.length() );
		setText( value );
		customizer.prepare( this );

		// We do our own action-listener handling here, because
		// notifications are sent on loss of focus as well as
		// Enter. checkOnExit() validates the data, and
		// notifies listeners if its valid.

		super.addActionListener			// handles Enter
		(	new ActionListener()
			{	public void actionPerformed(ActionEvent e)
				{	if( !validateInputAndNotifyListenersIfValid() )
						retainFocus();
				}
			}
		);

		// Send action events on loss of focus as well as Enter, but
		// only if the control holds valid input.

		super.addFocusListener
		(	new FocusAdapter()
			{	public void focusLost( FocusEvent e )
		        {	if( !validateInputAndNotifyListenersIfValid() )
			            retainFocus();
		        }
			}
		);

		// Process an Esc to reset the field to its last known good
		// value.

		super.addKeyListener
		(	new KeyAdapter()
			{	public void keyPressed( KeyEvent e )
		        {	if( e.getKeyCode() == KeyEvent.VK_ESCAPE )
					{	Input.this.setText( lastValid );
					}
				}
			}
		);

		// Set up a document listener. The documenation for JTextField
		// uses an insertString override to map characters to upper case,
		// but here, I want to disallow characters that will make the
		// entire string invalid. Consequently, a different approach is
		// required. The characterValidator is always installed because
		// we need it to check for the Esc key. The action and focus
		// listeners are added only if we need them because of
		// exit validation.

		if( !customizer.validatesOnExit() )
			getDocument().addDocumentListener( characterValidator );
	}

	/** @return true if the data was valid and the listeners notified
	  */
	private boolean validateInputAndNotifyListenersIfValid()
	{
		String text = Input.this.getText();
		valid = customizer.isValid( text );
		if( valid )
		{	lastValid = text;
			fire_ActionEvent();
		}
		return valid;
	}

	private void retainFocus()
	{	Input.this.requestFocus();
		invalidate();
	}

	private final DocumentListener characterValidator = new CharacterObserver();

	private class CharacterObserver implements DocumentListener
	{	public void removeUpdate(DocumentEvent event){/*uninteresting*/}

		public void changedUpdate(DocumentEvent event)
		{	insertUpdate(event);
		}

		public void insertUpdate(DocumentEvent event)
		{
			offset  		= event.getOffset();		//used by paint
			length  		= event.getLength();		//used by paint

			// Document d		= event.getDocument();
			// String newText = d.getText(offset, length);

			String fullText = Input.this.getText();

			valid = Input.this.customizer.isValid( fullText );
			if( !valid );
			{
				// You can't modify a Document in a
				// DocumentListener, so force a repaint
				// and update the valiator in paint(),
				// which checks the "valid" flag, set
				// set earlier.

				invalidate();
			}
		}
	}

	/** Override of event dispatcher dismisses any popups when any
	 *  event is detected.
	 */
	protected void processEvent( AWTEvent e )
	{	super.processEvent(e);
		if( e.getID()!=KeyEvent.KEY_RELEASED )
			dismissPopups();
	}

	private void dismissPopups() // Not synchroinzed---must be called from
	{
		if( popup != null )		  // AWT event thread.
		{	popup.hide();
			popup=null;
		}
	}

	/** This method must be public because it's public in the base class. Don't
	 * override it.
	 */
	public void paint( Graphics g )
	{	try
		{	// Before you repaint, modify the document
			// to eliminate any invalid characters detected by.
			// the document listener

			if( !valid )
			{
				explainTheProblem( getLocationOnScreen(), getText() );
				getDocument().remove( offset, length );
				Toolkit.getDefaultToolkit().beep();
				valid = true;
			}
			super.paint(g);
		}
		catch( BadLocationException e)
		{	Logger.getLogger("com.holub.PAC").warning
			(	Log.stackTraceAsString(e)
			);
		}
	}

	private void explainTheProblem( Point location, String badInput )
	{
		String help = customizer.help( badInput );
		if( help != null && help.length() > 0 )
		{	JLabel text = new JLabel();
			text.setText
			("<html><head>"
			+"<style type=\"text/css\">"
			+"body{ font: 11pt verdana, arial, helvetica, san-serif; background: #ffffcc}"
			+"</style>"
			+"</head><body>"
			+"<table border=0 cellspacing=0 cellpadding=6><tr><td>"
			+ help
			+ (customizer.validatesOnExit()
						? "<p>Press Esc to reset this field." : "")
			+"</td></tr></table></body></html>"
			);

			// Move the location a bit so that it doesn't completely
			// obscure the control.

			location.translate(getWidth() * 1/2, getHeight() * 1/2);
			popup = PopupFactory.getSharedInstance().getPopup( this, text,
													location.x, location.y );
			popup.show();

			// Create a timer to kill the popup after POPUP_LIFETIME
			// seconds of user inactivity
			// Must be an javax.swing.Timer timer so that dismissPopups() will
			// work correctly.
			Timer t=new Timer(	POPUP_LIFETIME * 1000,
								new ActionListener()
								{	public void actionPerformed(ActionEvent evt)
									{	dismissPopups();
									}
								}
							 );
			t.setRepeats(false);
			t.start();
		}
	}

    private ActionListener listeners = null;

	/************************************************************************
	 *  Action listeners are notified when the control holds valid
	 *  input and the user is done entering data. This will happen
	 *  in two situations:
	 *  <ol>
	 *  <li> The user hit Enter and the contents
	 *  are properly validated.
	 *  <li>When a control looses focus,
	 *  and the control holds a valid string. (It's actually not possible
	 *  for the control to loose focus when the contents aren't valid,
	 *  and a notification <em>is not sent</em> if the user exits
	 *  the control by hitting Esc.). The {@link ActionEvent ActionEvent}'s
	 *  "command" string holds the user input as would be returned from
	 *  <code>toString()</code>.
	 * </ol>
	 * @param l
	 */
    public synchronized void addActionListener(ActionListener l)
    {   listeners = AWTEventMulticaster.add(listeners, l);
    }
	/** Remove a listener added by a prior
	 *  {@link #addActionListener addActionListener(...)} call.
	 */
    public synchronized void removeActionListener(ActionListener l)
    {   listeners = AWTEventMulticaster.remove(listeners, l);
    }
    public void fire_ActionEvent()
    {   if( listeners != null )
	    {   ActionEvent e = new ActionEvent( this, 0, toString() );
			listeners.actionPerformed(e);
	    }
	}

	//----------------------------------------------------------------------
	// Misc short methods.

	public Dimension getMinimumSize()	{ return getPreferredSize(); 	}
	public Dimension getMaximumSize()	{ return getPreferredSize();	}
	public String    toString()         { return getText();             }

	/************************************************************************
	 * A test class.
	 */
	public static class Test
	{	public static void main( String[] args )
		{
			Customizer weird =
				new Customizer()
				{	public boolean validatesOnExit(){ return true; }
					public boolean isValid( String s )
					{	System.out.println("Weird validating: [" + s + "]" );
						return s.length() == 0;
					}

					public String help( String badInput )
					{	return "Only empty strings are valid";
					}

					public void prepare( JTextField current )
					{	current.setToolTipText("Only empty strings are valid");
					}
				};

			final Input s1 = new Input( "", weird, BOXED, false );
			s1.setColumns(10);

			Customizer integer = new NumericInput.Behavior(-100, 100, 0);
			Customizer money   = new NumericInput.Behavior(0, 10000,2);
			Customizer plain   = new NumericInput.Behavior();

			final Input n1 = new Input( "99",  	 	 integer,  UNDERLINED, true );
			final Input n2 = new Input( "123.00", 	 money ,   BOXED, 	   false);
			final Input n3 = new Input( "1,234.567", plain,    BORDERLESS, false);

			ActionListener reporter =
			    new ActionListener()
				{   public void actionPerformed( ActionEvent e )
					{   System.out.println("--------------------");
						System.out.println( "n1=" + n1.getText() );
						System.out.println( "n2=" + n2.getText() );
						System.out.println( "n3=" + n3.getText() );
					}
				};

			n1.addActionListener( reporter );
			n2.addActionListener( reporter );
			n3.addActionListener( reporter );

			try	// check that validation occurs on initialization
			{	Input x = new Input( "xxx", plain,  BOXED, true );
				System.out.println("Initialization validation Failed");
			}
			catch( IllegalArgumentException e )
			{	System.out.println("Initialization validation OK");
			}

			n1.setColumns(5);

			JPanel panel = new JPanel();
			panel.setLayout( new FlowLayout(FlowLayout.CENTER, 10, 10) );
			panel.setBackground( Color.WHITE );
			panel.add(n1);
			panel.add(n2);
			panel.add(n3);
			panel.add(s1);

			JFrame frame = new JFrame();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.getContentPane().setBackground( Color.WHITE );
			frame.getContentPane().setLayout( new BorderLayout() );
			frame.getContentPane().add(panel, BorderLayout.SOUTH );
			frame.pack();
			frame.show();

			frame.addWindowListener
			(	new WindowAdapter()
				{	public void windowClosing( WindowEvent e )
					{	System.out.println( "n1=" + n1.getText() );
						System.out.println( "n2=" + n2.getText() );
						System.out.println( "n3=" + n3.getText() );
						System.exit(0);
					}
				}
			);
		}
	}
}
