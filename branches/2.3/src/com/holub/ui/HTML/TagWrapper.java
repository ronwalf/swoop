// (c) 2003 Allen I Holub. All rights reserved.

package com.holub.ui.HTML;

import java.awt.*;
import javax.swing.*;
import com.holub.ui.HTML.TagBehavior;

/** The wrapper class is a JPanel that wraps a JComponent that implement
 *  TagBehavior. The point of wrapping it is that we can change the
 *  component represented on some UI without changing the container. That
 *  is, the container knows about the wrapper, but not about what's in it.
 *  Consequently, we can change the contents of the wrapper without
 *  affecting the container.
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
 * @author Allen I. Holub
 */

public class TagWrapper extends JPanel implements TagBehavior
{	private TagBehavior wrapped;

	public TagWrapper( JComponent wrapped )
	{	setLayout( new GridLayout(1,1) );
		add( wrapped );
		this.wrapped = (TagBehavior)wrapped;
	}

	public void		destroy()		{ wrapped.destroy(); }
	public String	getFormData()	{ return wrapped.getFormData(); }
	public void		reset()			{ wrapped.reset();}

	/** Replace the component that's displayed with a different
	 *  component.
	 */

	public void	replace(JComponent replacement)
	{	removeAll();
		repaint();
		add( replacement );
		wrapped = (TagBehavior)replacement;
		repaint();
	}
}
