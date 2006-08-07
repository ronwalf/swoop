// (c) 2003 Allen I Holub. All rights reserved.
// Terms of distribution are described below.

package com.holub.ui;

import com.holub.ui.DateSelectorDialog;
import com.holub.ui.DateInput;
import com.holub.tools.DateUtil;

import javax.swing.*;
import java.util.Date;
import java.util.Calendar;
import java.text.*;
import java.awt.*;
import java.awt.event.*;

// TODO: Button background turns grey when it's pressed! has something
// to do with a focus-change notification?

/************************************************************************
 This class overrides all non-deprecated methods
 of {@link java.util.Date}, and adds a method that produces a
 user-interface (a JPanel that combines the {@link DateInput} and
 {@link DateSelectorDialog} classes into a single control).
 The control displays itself as a text box (which can be editable or not)
 and a button. Clicking the button pops up a date-selector dialog.
 This way you can type in a date or pick one with the mouse.
 When the user specifies a date through the interface, the value
 of the current object changes accordingly.
 If the requested UI isn't editable, the date is dislplayed
 as a label with a transparent background.
 <p>
 The point is that you don't need to worry about updating the date
 from user input:
 <pre>
 Class  myClass
 {  InteractiveDate    date = new InteractiveDate();
    void businessLogic()
    {   // Just use the Interactive Date as if it were a
        // Date object.
    }

    void createUi(Frame parent)
    {   JDialog d = new JDialog( parent, true );  // modal
        //...
        d.getContentPane().add( date.getUi() );
        //...
        d.show()    // popup the dialog.

        // At this point, d has been updated automatically
        // from user input. you don't need to extact a value
        // from a text field and use it to update d.
    }
 }
 </pre>

 <p><b>NOTE:</b>The deprecated methods of Date are not overridden, and
 though you can still call them, you shouldn't. This class will
 not function properly if you call a deprecated base-class method
 that modifies the date's value. The only safe way to change the
 value is with a call to {@link #setTime setTime(...)}. You should
 use a {@link java.util.Calendar} to get finer control over the
 date-setting process, then inport the new value like this:
 <pre>
 Calendar c = new Calendar();
 Date d = new InteractiveDate();   // <-- Base-class reference
 //...
 d.setTime( c.getTime().getTime() );    // local convenience overload
 <pre>
 or you can use the {@link #setTime(java.util.Calendar)} convenience overload
 if you're not using a base-class reference:
 <pre>
 Calendar c = new Calendar();
 InteractiveDate d = new InteractiveDate();
 //...
 d.setTime( c );    // Use local convenience overload.
 <pre>

<!-- ====================== distribution terms ===================== -->
<p><blockquote
	style="border-style: solid; border-width:thin; padding: 1em 1em 1em 1em;">
<center>
			Copyright &copy; 2003, Allen I. Holub. All rights reserved.
</center>
<br>
<br>
		This code is distributed under the terms of the
		<a href="http://www.gnu.org/licenses/gpl.html"
		>GNU Public License</a> (GPL)

		with the following ammendment to section 2.c:
		As a requirement for distributing this code, your splash screen,
		about box, or equivalent must include an my name, copyright,
		<em>and URL</em>. An acceptable message would be:
<center>
		This program contains Allen Holub's <em>XXX</em> utility.<br>
				(c) 2003 Allen I. Holub. All Rights Reserved.<br>
						http://www.holub.com<br>
</center>
		If your progam does not run interactively, then the foregoing
		notice must appear in your documentation.
</blockquote>
<!-- =============================================================== -->

@author Allen I. Holub

*************************************************************************/

public class InteractiveDate extends java.util.Date
{
	private JComponent ui = null;
	private DateFormat formatter = DateFormat.getDateInstance( DateFormat.MEDIUM );

	private static final String calendarGraphic = "images/10px.calendar.icon.gif";

/** Works like {@link java.util.Date#Date()}.			*/	public InteractiveDate(		 ) { super();		}
/** Works like {@link java.util.Date#Date(long)}.		*/	public InteractiveDate(long date) { super(date);	}

/** Works like {@link java.util.Date#clone}.			*/	public Object	clone	 ()		   {return super.clone();	  }
/** Works like {@link java.util.Date#getTime}.			*/	public long 	getTime	 ()		   {return super.getTime();   }
/** Works like {@link java.util.Date#before}.			*/	public boolean	before	 (Date w)  {return super.before(w);	  }
/** Works like {@link java.util.Date#after}.			*/	public boolean	after	 (Date w)  {return super.after(w);	  }
/** Works like {@link java.util.Date#equals}.			*/	public boolean	equals	 (Object o){return super.equals(o);	  }
/** Works like {@link java.util.Date#compareTo(Date)}.	*/	public int		compareTo(Date d)  {return super.compareTo(d);}
/** Works like {@link java.util.Date#compareTo(Object)}.	public int		compareTo(Object o){return super.compareTo(o);} */
/** Works like {@link java.util.Date#hashCode}.			*/	public int		hashCode ()		   {return super.hashCode();  }
/** Works like {@link java.util.Date#toString}.			*/	public String	toString ()		   {return super.toString();  }

	/** Works like {@link java.util.Date#setTime}.	*/
	public void	setTime( long t )
	{	super.setTime(t);
		if( ui != null )    // There's a UI displayed right now.
		{   if( ui instanceof JLabel )
				((JLabel)ui).setText( formatter.format(this) );
			else
				((Chooser)ui).dateHasChanged();
		}
	}

	/** A convenience overload extacts the time from a {@link java.util.Calendar}
	 * @param c The calender from which the new date value is extracted
	 */
	public void	setTime( Calendar c )
	{   setTime( c.getTime().getTime() );
	}

	/** A convenience overload extacts the time from another {@link java.util.Date}.
	 * @param c The Date from which the new value is extracted
	 */
	public void	setTime( Date c )
	{   setTime( c.getTime() );
	}

	/** Gat a user interface for the current date. This UI is "hot:" changes
	 *  that the user makes are reflected in the underlying
	 *  date object, and changes made to the Date object (via a call
	 *  to {@link #setTime}) will change the date displayed in the UI.
	 *  Note that user-initiatiated changes are not guaranteed to be
	 *  visible until after the user interface shuts down.
	 *	<p>
	 *	The returned UI is transparent, so the underlying background
	 *	color will show through.
	 *	<p>
	 *  The user interface is disconnected from the Date object that
	 *  manufactures it when the Window that holds it shuts down.
	 *  Attempts to reuse the UI will fail. It's best to insert
	 *  the UI like this:
	 *  <pre>
	 *	Container c;
	 *	//...
	 *	c.add( myDate.getUi( isReadOnly ) );
	 *  </pre>
	 *  without keeping the returned pointer around anywhere.
	 *  <p>
	 *  In the current implementation, only one user interface can
	 *  be outstanding. Attempts to call getUi() a second time
	 *  will fail (with an IllegalStateException) unless the
	 *  window that contains the UI returned from the previous
	 *  call has shut down.
	 *
	 * @param isReadOnly if true, then a simple label that displays the date
	 *          is returned, otherwise, a control initialized with the date,
	 *          into which the user can type (or otherwise select) a new date,
	 *          is returned.
	 * @return A {@link JComponent} that represents this date.
	 * @throws IllegalStateException if you try to get a UI when a previously
	 *          issued UI is still active (the containing window hasn't shut
	 *          down).
	 * @see #getUi(boolean,DateFormat)
	 */
	public JComponent getUi( boolean isReadOnly )
	{	if( ui != null )
			throw new IllegalStateException();
		ui = ( isReadOnly )
				? (JComponent) new JLabel (formatter.format(this))
				: (JComponent) new Chooser(formatter.format(this))
				;
		ui.setOpaque( false );
		return ui;
	}

	/** Like {@link #getUi(boolean)}, but permanently replaces
	 *  the formatter used to create a string representation of the
	 *  date with the indicated formatter, then return the UI.
	 * @param isReadOnly true for a read-only representation
	 * @param formatter {@link DateFormat} object to replace the default formatter.
	 * @return A {@link JComponent} that represents this date.
	 * @see #getUi(boolean)
	 */
	public JComponent getUi( boolean isReadOnly, DateFormat formatter )
	{   this.formatter = formatter;
		return getUi(isReadOnly);
	}

	/** Convenience method. Returns a read/write user interface that uses
	 *  the current formatter.
	 * @return An editible {@link JComponent} linked to the current object.
	 */
	public JComponent getUi()
	{   return getUi( false );
	}

	private class Chooser extends JPanel
	{
		private JTextField				control;
		private JButton					button;
		private DateSelectorDialog	selector = null;

		public Chooser( String label )
		{
			setBorder( BorderFactory.createLineBorder(Color.BLACK,1) );

			control  = (JTextField) new DateInput(label);
			control.setBorder( BorderFactory.createEmptyBorder(0,2,0,2) );
			control.setOpaque( false );

			// Catch changes that happen when the user hits Enter or the control
			// looses focus. Note that listeners are notified only when the contents
			// are valid.

			control.addActionListener
			(	new ActionListener()
				{	public void actionPerformed(ActionEvent event)
			        {   setTime( DateUtil.parseDate( control.getText() ).getTime() );
			        }
				}
			);

			button = new JButton(
						new ImageIcon(
							getClass().getClassLoader().getResource(calendarGraphic)));
			button.setOpaque        ( false );
			button.setBorder        ( BorderFactory.createEmptyBorder(1,1,1,1) );
			button.setFocusPainted  ( false );
			button.addActionListener
			(	new ActionListener()
				{	public void actionPerformed(ActionEvent e)
					{
						Component current = button;
						Component parent  = null;
						do
						{   parent = current.getParent();
							if( parent == null )
								break;
							if( parent instanceof Dialog )
								selector = new DateSelectorDialog((Dialog)parent);
							else if( parent instanceof Frame)
								selector = new DateSelectorDialog((Frame)parent);
							current = parent;
						}
						while(selector == null);

						// assert selector != null;
						selector.setLocationRelativeTo(button);
						Date selected = selector.select();
						selector = null;
						if( selected != null )              // not canceled by user
							setTime( selected.getTime());   // causes dateHasChanged, below, to be called
					}
				}
			);

			setLayout( new BorderLayout() );
			add( control, BorderLayout.CENTER );
			add( button, BorderLayout.EAST );
			setOpaque(false);
		}

		/** Called by the outer-class setTime() method to indicate that the control
		 *  needs to display its value.
		 */
		public void dateHasChanged()
		{   if( selector != null )  // kill it
			{   selector.setVisible(false);
				selector.dispose();
				selector=null;
			}
			control.setText( formatter.format(InteractiveDate.this) );
		}
	}
	//----------------------------------------------------------------------
	private static class Test
	{   public static void main(String[] args)
		{
			JFrame f = new JFrame();
			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			f.getContentPane().add( new JLabel("Parent"));
			f.pack();
			f.show();

			JDialog dialog = new JDialog(f,true);
			Container c = dialog.getContentPane();
			c.setLayout( new GridLayout(/*rows*/ 2, /*cols*/ 1) );
			c.setBackground( Color.WHITE );

			final InteractiveDate d1 = new InteractiveDate();
			final InteractiveDate d2 = new InteractiveDate();
			JComponent u1 = d1.getUi(true);
			JComponent u2 = d2.getUi(false);

			c.add( u1 );
			c.add( u2 );
			dialog.pack();
			dialog.show();

			System.out.println("Dialog shut down");
			System.out.println("d1: " + d1 );
			System.out.println("d2: " + d2 );
		}
	}
}

// TODO: check that DateInput verifies the date on setText.
