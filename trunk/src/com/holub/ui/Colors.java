// (c) 2003 Allen I Holub. All rights reserved.
package com.holub.ui;
import java.awt.*;

/*	The Colors interface contains nothing but symbolic constants for various
 *	color values that I use regularly. The names are self explanatory.
 */

/*** Provides symbolic definitions for various colors not defined in the {@link Colors} class.
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
 *<p>
 *<table>
 *	<tr><td>Color 			</td><td> Sample 								   </td><td> R  </td><td> G  </td><td> B  </td></tr>
 *	<tr><td>TRANSPARENT		</td><td> &nbsp;								   </td colspan=3> no value, alias for null. </td></tr>
 *
 *	<tr><td>DARK_RED		</td><td bgcolor=#990000> &nbsp;&nbsp;&nbsp;&nbsp; </td><td> 99 </td><td> 00 </td><td> 00 </td></tr>
 *	<tr><td>MEDIUM_RED		</td><td bgcolor=#cc0000> &nbsp;&nbsp;&nbsp;&nbsp; </td><td> cc </td><td> 00 </td><td> 00 </td></tr>
 *	<tr><td>LIGHT_RED		</td><td bgcolor=#ff0000> &nbsp;&nbsp;&nbsp;&nbsp; </td><td> ff </td><td> 00 </td><td> 00 </td></tr>
 *	
 *	<tr><td>DARK_ORANGE		</td><td bgcolor=#ff6600> &nbsp;&nbsp;&nbsp;&nbsp; </td><td> ff </td><td> 66 </td><td> 00 </td></tr>
 *	<tr><td>MEDIUM_ORANGE	</td><td bgcolor=#ff9900> &nbsp;&nbsp;&nbsp;&nbsp; </td><td> ff </td><td> 99 </td><td> 00 </td></tr>
 *	<tr><td>LIGHT_ORANGE	</td><td bgcolor=#ffcc00> &nbsp;&nbsp;&nbsp;&nbsp; </td><td> ff </td><td> cc </td><td> 00 </td></tr>
 *	<tr><td>ORANGE			</td><td bgcolor=#ff9900> &nbsp;&nbsp;&nbsp;&nbsp; </td><td> ff </td><td> 99 </td><td> 00 </td></tr>
 *	
 *	<tr><td>OCHRE			</td><td bgcolor=#cc9900> &nbsp;&nbsp;&nbsp;&nbsp; </td><td> cc </td><td> 99 </td><td> 00 </td></tr>
 *	<tr><td>DARK_YELLOW		</td><td bgcolor=#ffff00> &nbsp;&nbsp;&nbsp;&nbsp; </td><td> ff </td><td> ff </td><td> 00 </td></tr>
 *	<tr><td>MEDIUM_YELLOW	</td><td bgcolor=#ffff99> &nbsp;&nbsp;&nbsp;&nbsp; </td><td> ff </td><td> ff </td><td> 99 </td></tr>
 *	<tr><td>LIGHT_YELLOW	</td><td bgcolor=#ffffdd> &nbsp;&nbsp;&nbsp;&nbsp; </td><td> ff </td><td> ff </td><td> dd </td></tr>
 *	
 *	<tr><td>DARK_GREEN		</td><td bgcolor=#006600> &nbsp;&nbsp;&nbsp;&nbsp; </td><td> 00 </td><td> 66 </td><td> 00 </td></tr>
 *	<tr><td>MEDIUM_GREEN	</td><td bgcolor=#009900> &nbsp;&nbsp;&nbsp;&nbsp; </td><td> 00 </td><td> 99 </td><td> 00 </td></tr>
 *	<tr><td>LIGHT_GREEN		</td><td bgcolor=#00ff00> &nbsp;&nbsp;&nbsp;&nbsp; </td><td> 00 </td><td> ff </td><td> 00 </td></tr>
 *	<tr><td>GREEN			</td><td bgcolor=#009900> &nbsp;&nbsp;&nbsp;&nbsp; </td><td> 00 </td><td> 99 </td><td> 00 </td></tr>
 *	
 *	<tr><td>DARK_BLUE		</td><td bgcolor=#000099> &nbsp;&nbsp;&nbsp;&nbsp; </td><td> 00 </td><td> 00 </td><td> 99 </td></tr>
 *	<tr><td>MEDIUM_BLUE		</td><td bgcolor=#0000cc> &nbsp;&nbsp;&nbsp;&nbsp; </td><td> 00 </td><td> 00 </td><td> cc </td></tr>
 *	<tr><td>LIGHT_BLUE		</td><td bgcolor=#0000ff> &nbsp;&nbsp;&nbsp;&nbsp; </td><td> 00 </td><td> 00 </td><td> ff </td></tr>
 *	
 *	<tr><td>DARK_PURPLE		</td><td bgcolor=#990099> &nbsp;&nbsp;&nbsp;&nbsp; </td><td> 99 </td><td> 00 </td><td> 99 </td></tr>
 *	<tr><td>MEDIUM_PURPLE	</td><td bgcolor=#cc00ff> &nbsp;&nbsp;&nbsp;&nbsp; </td><td> cc </td><td> 00 </td><td> ff </td></tr>
 *	<tr><td>LIGHT_PURPLE	</td><td bgcolor=#cc99ff> &nbsp;&nbsp;&nbsp;&nbsp; </td><td> cc </td><td> 99 </td><td> ff </td></tr>
 *	<tr><td>PURPLE			</td><td bgcolor=#cc00ff> &nbsp;&nbsp;&nbsp;&nbsp; </td><td> cc </td><td> 00 </td><td> ff </td></tr>
 *</table>
 *
 * @see java.awt.Color
 */

public interface Colors
{
																										static final Color TRANSPARENT   = null;
/*** RGB=(0x99, 0x00, 0x00); <span style="background-color:#990000;">&nbsp;&nbsp;&nbsp;</span> */		static final Color DARK_RED	     = new Color(0x99, 0x00, 0x00);
/*** RGB=(0xcc, 0x00, 0x00); <span style="background-color:#cc0000;">&nbsp;&nbsp;&nbsp;</span> */		static final Color MEDIUM_RED    = new Color(0xcc, 0x00, 0x00);
/*** RGB=(0xff, 0x00, 0x00); <span style="background-color:#ff0000;">&nbsp;&nbsp;&nbsp;</span> */		static final Color LIGHT_RED	 = new Color(0xff, 0x00, 0x00);

/*** RGB=(0xff, 0x66, 0x00); <span style="background-color:#ff6600;">&nbsp;&nbsp;&nbsp;</span> */		static final Color DARK_ORANGE   = new Color(0xff, 0x66, 0x00);
/*** RGB=(0xff, 0x99, 0x00); <span style="background-color:#ff9900;">&nbsp;&nbsp;&nbsp;</span> */		static final Color MEDIUM_ORANGE = new Color(0xff, 0x99, 0x00);
/*** RGB=(0xff, 0xcc, 0x00); <span style="background-color:#ffcc00;">&nbsp;&nbsp;&nbsp;</span> */		static final Color LIGHT_ORANGE  = new Color(0xff, 0xcc, 0x00);
/*** RGB=(0xff, 0x99, 0x00); <span style="background-color:#ff9900;">&nbsp;&nbsp;&nbsp;</span> */		static final Color ORANGE 		 = new Color(0xff, 0x99, 0x00);

/*** RGB=(0xcc, 0x99, 0x00); <span style="background-color:#cc9900;">&nbsp;&nbsp;&nbsp;</span> */		static final Color OCHRE		 = new Color(0xcc, 0x99, 0x00); 
/*** RGB=(0xff, 0xff, 0x00); <span style="background-color:#ffff00;">&nbsp;&nbsp;&nbsp;</span> */		static final Color DARK_YELLOW	 = new Color(0xff, 0xff, 0x00);
/*** RGB=(0xff, 0xff, 0x99); <span style="background-color:#ffff99;">&nbsp;&nbsp;&nbsp;</span> */		static final Color MEDIUM_YELLOW = new Color(0xff, 0xff, 0x99);
/*** RGB=(0xff, 0xff, 0xdd); <span style="background-color:#ffffdd;">&nbsp;&nbsp;&nbsp;</span> */		static final Color LIGHT_YELLOW  = new Color(0xff, 0xff, 0xdd);

/*** RGB=(0x00, 0x66, 0x00); <span style="background-color:#006600;">&nbsp;&nbsp;&nbsp;</span> */		static final Color DARK_GREEN    = new Color(0x00, 0x66, 0x00);
/*** RGB=(0x00, 0x99, 0x00); <span style="background-color:#009900;">&nbsp;&nbsp;&nbsp;</span> */		static final Color MEDIUM_GREEN  = new Color(0x00, 0x99, 0x00);
/*** RGB=(0x00, 0xff, 0x00); <span style="background-color:#00ff00;">&nbsp;&nbsp;&nbsp;</span> */		static final Color LIGHT_GREEN   = new Color(0x00, 0xff, 0x00);
/*** RGB=(0x00, 0x99, 0x00); <span style="background-color:#009900;">&nbsp;&nbsp;&nbsp;</span> */		static final Color GREEN		 = MEDIUM_GREEN;

/*** RGB=(0x00, 0x00, 0x99); <span style="background-color:#000099;">&nbsp;&nbsp;&nbsp;</span> */		static final Color DARK_BLUE     = new Color(0x00, 0x00, 0x99);
/*** RGB=(0x00, 0x00, 0xcc); <span style="background-color:#0000cc;">&nbsp;&nbsp;&nbsp;</span> */		static final Color MEDIUM_BLUE   = new Color(0x00, 0x00, 0xcc);
/*** RGB=(0x00, 0x00, 0xff); <span style="background-color:#0000ff;">&nbsp;&nbsp;&nbsp;</span> */		static final Color LIGHT_BLUE    = new Color(0x00, 0x00, 0xff);

/*** RGB=(0x99, 0x00, 0x99); <span style="background-color:#990099;">&nbsp;&nbsp;&nbsp;</span> */		static final Color DARK_PURPLE   = new Color(0x99, 0x00, 0x99);
/*** RGB=(0xcc, 0x00, 0xff); <span style="background-color:#cc00ff;">&nbsp;&nbsp;&nbsp;</span> */		static final Color MEDIUM_PURPLE = new Color(0xcc, 0x00, 0xff);
/*** RGB=(0xcc, 0x99, 0xff); <span style="background-color:#cc99ff;">&nbsp;&nbsp;&nbsp;</span> */		static final Color LIGHT_PURPLE  = new Color(0xcc, 0x99, 0xff);
/*** RGB=(0xcc, 0x00, 0xff); <span style="background-color:#cc00ff;">&nbsp;&nbsp;&nbsp;</span> */		static final Color PURPLE 		 = MEDIUM_PURPLE;
}
