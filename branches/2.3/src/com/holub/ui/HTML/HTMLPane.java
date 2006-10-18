// (c) 2003 Allen I Holub. All rights reserved.
package com.holub.ui.HTML;

import com.hexidec.ekit.component.ExtendedHTMLDocument;
import com.hexidec.ekit.component.RelativeImageView;
import com.holub.net.UrlUtil;
import com.holub.tools.Log;
import com.holub.ui.AncestorAdapter;

import com.holub.ui.HTML.TagBehavior;
import com.holub.ui.HTML.FilterFactory;

import java.io.*;
import java.util.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import java.util.logging.*;
import java.util.regex.*;
import javax.swing.*;
import javax.swing.text.*;	// View Element ViewFactory
import javax.swing.text.html.*;
import javax.swing.event.*;
import javax.swing.border.*;

// See full documentation for this class in last-month's article
/*** ****************************************************************
 *  This class let's you build an HTML-based interface with JSP-like
 *	functionality that operates entirely within the confines of a
 *	client-side program---no web server is necessary.
 *	<code>HTMLPane</code> also fixes several problems in {@link JEditorPane}'s
 *	HTML support, but it is based on {@link JEditorPane}, so has many of its
 *	limitations.
 *	<p>
 *	<code>HTMLPane</code> augments {@link JEditorPane} in several ways:
 *	<ul>
 *	<li>An <code>HTMLPane</code> logs errors to the com.holub.ui logger
 *	rather than throwing exceptions in many situations. You can hook
 *	into these messages by creating a logger (described below).
 *	<li>I've improved the appearance of form elements a bit:
 *			<ul>
 *			<li>Radio buttons and text-input fields are now aligned properly
 *			with the surrounding text
 *			<li>The list created by a <code>&lt;select&gt;</code>
 *			element is now only a little wider than the contained text
 *			(instead of taking up the full screen).
 *			<li>Radio buttons and check boxes have transparent backgrounds so that
 *			they don't look weird against background textures and
 *			colors that aren't dark grey (<code>bgcolor="d0d0d0"</code>).
 *	</ul>
 *	<li>You can add {@linkplain #addTag custom tags} and handlers for them,
 *	so you add JSP-style behavior to your forms.
 *	<li>You can specify a local form-data handler that's invoked in
 *	response to both submit-style and cancel-style operations. That is,
 *	clicking a "Submit" button causes a chuck of code (that you provide)
 *	to execute rather than sending the form data to a remote server.
 *	<li>
 *	HTTP hyperlinks are supported, but note that the JDK 1.4 runtime
 *	does not support the format: "file://c:/file."
 *	You have to use "file:/c:/file" (one slash).
 *	Strictly speaking, the 1.4 behavior is actually "correct," but some
 *	the rules for forming a "correct" URL are not well known, and the
 *	lack of a support for a double slash might be frustrating.
 *	<li>
 *	Mailto: hyperlinks are supported, but only on the Windows platform.
 *	Subject lines have to be properly URL encoded, (with %20 used for spaces,
 *	etc.). For example:
 *	<PRE>
 *	&lt;a href="mailto:allen@holub.com?subject=foo%20bar"&gt;
 *	</PRE>
 *	Attempts to use mailto on other platforms result in a
 *	logged warning, but are otherwise silently absorbed.
 *	<li>
 *	The <code>&lt;a target=_blank ...&gt;</code> attribute now correctly
 *	causes the page to pop up in its own window. The window is a
 *	<code>pack()</code>ed frame that holds an HTMLPane, so your custom-tag
 *	handlers will work in the popup.
 *	<li>
 *	Standard http: and file: hyperlinks and relative URLs are handled
 *	internally, but only those hyperlinks that reference files that end
 *	in certain {@linkplain #redirect(URL) file
 *	extensions} are processed.
 *	Other protocols (e.g. ftp:) are not supported.
 *	<li>
 *	<code>HTMLPane</code> supports a
 *	{@linkplain #addHostMapping(String,String) host-mapping feature}
 *	that let's you specify maps between the "host" part of a URL and an
 *	arbitrary map-to URL. All hyperlinks aimed at the host will end up
 *	at the map-to location. This way you can map network URLs to
 *	file:// URLs for testing or non-network use.
 *	</ul>
 *	<p>
 *	<font size=+1><b>Logging</b></font>
 *	</p>
 *	The <code>HTMLPanel</code> logs errors and warnings to the
 *	com.holub.ui logger.
 *	To view the messages from all programs that use <code>HTMLPanel</code>,
 *	modify the .../jre/lib/logging.properties file,
 *	setting the <i>.level</i> and
 *	<i>java.util.logging.ConsoleHandler.level</i>
 *	properties to <i>ALL</i>. You can also
 *	put the following code into your program to turn on logging for
 *	that program only:
 *	<PRE>
 *  static
 *  {   Logger  log = Logger.getLogger("com.holub.ui");
 *      Handler h = new ConsoleHandler();
 *      h.  setLevel(Level.ALL);
 *      log.setLevel(Level.ALL);
 *      log.addHandler(h);
 *  }
 *  </PRE>
 *  You can turn logging off by specifying Level.OFF instead of ALL.
 *  The {@link com.holub.tools.Log} class provides convenience
 *  methods for controlling logging.
 *	<p>
 *  <a name="customTags">
 *	    <font size=+1><b>Custom Tags</b></font>
 *  </a>
 *	<p>
 *	You can define custom tags that can be used in your .html input,
 *	in a manner similar to a custom JSP tag. Set up a new tab by calling
 *	{@link #addTag addTag(...)}, passing it
 *	an object that identifies a {@linkplain TagHandler tag handler}
 *	that's activated when the tag is encountered.
 *	 (See {@link #addTag addTag(...)} for more information.)
 *	 <p>
 *	 Because java's {@link
 *	 javax.swing.JEditorPane}
 *	 underlies the current class, you can't define a namespace-style
 *	 custom tag: a name like
 *	 <code>&lt;holub:myTag ...&gt;</code> (that contains a colon)
 *	 isn't recognized. You can use underscores, but that's a kludge.
 *	 <p>
 *	 Also, the current implementation of custom tags does not permit the
 *	 element to have content. The problem is that the default parser
 *	 does not pair non-HTML tags. That is, <code>&lt;foo&gt;</code>
 *	 and <code>&lt;/foo&gt;</code> are treated as completely
 *	 independent tags with no connection between them. The only
 *	 solution is to effectively rewrite the parser, but that's
 *	 a lot of work for not much gain. Future versions of this
 *	 class might permit content, however.
 *	<p> Several pre-built custom tags are provided. Add support for
 *	these by issuing one of the following calls to you <code>HTMLPane</code>:
 *	<PRE>
 *  pane.{@link #addTag addTag}( "size"        , new {@link SizeHandler SizeHandler}()            );
 *  pane.{@link #addTag addTag}( "inputAction", new {@link InputActionHandler InputActionHandler}(this));
 *  pane.{@link #addTag addTag}( "inputNumber", new {@link InputNumberHandler InputNumberHandler}()    );
 *  pane.{@link #addTag addTag}( "inputDate"  , new {@link InputDateHandler InputDateHandler}()      );
 *  </PRE>
 * (These tags are all installed for you if you use the
 * {@link #HTMLPane(boolean)} constructor).
 *
 *	<PRE>
&lt;size  height=400 width=400&gt;
 *	</PRE>
 *	<p style="padding-left:2em">
 *		Specifies the size of the pane in pixels.
 *	</p>
 *	<PRE>
 *  <a name="inputAction">
&lt;inputAction name="myName" value="text"&gt
 *	</PRE>
 *	<p style="padding-left:2em">
 *	This tag inserts a submit-style button that causes actionPerformed
 *	messages to be sent to all registered action listeners (as if the
 *	submit button had been pressed). The method and action attributes
 *	of the passed FormActionEvent will be empty (not null) Strings, and the
 *	data Properties object holds the pair <code>name=<em>XXX</em></code>,
 *	where <em>name</em> was specified using the <em>name=xxx</em> attribute in
 *	the original tag, and <em>XXX</em> holds the value <em>true</em> if the button
 *	was pressed, <em>false</em> if it wasn't.
 *	Key=value pairs supplied by custom
 *	tag handlers [added using {@link #addTag addTag(...)}] are also available, but
 *	the data supplied from standard HTML tags
 *	(<em>&lt;input&gt;</em>, <em>&lt;textarea&gt;</em>, and <em>&lt;select&gt;</em>)
 *	<span style="text-decoration:underline;">is not available</span>.
 *	The value attribute also defines the text
 *	on the button face. This tag is here to make
 *	it easy to implement a "cancel"  or "quit" operation,
 *	but you can actually use it as a generic button if don't need the
 *	form data from the
 *	<em>&lt;input&gt;</em>, <em>&lt;textarea&gt;</em>, or <em>&lt;select&gt;</em>
 *	tags.
 *	</p>
 *	</a>
 *	<PRE>
&lt;inputNumber name="fred" value="0.0" min="0" max="100" precision="2" size=<em>n</em>&gt;
 *	</PRE>
 *	<p style="padding-left:2em">
 *	Like an &lt;input type=text&gt; tag, but inputs a numeric value
 *	in the range <em>min</em> &le; <em>N</em> &le; <em>max</em>, with up to <em>precision</em> digits
 *	permitted to the right of the decimal point (<em>precision can be zero</em>).
 *	The optional <em>size</em> attribute is the width of the field in columns.
 *	</p>
 *	 <PRE>
&lt;inputDate name="fred" value="10/15/03" size=<em>n</em>&gt;
 *	 </PRE>
 *	<p style="padding-left:2em">
 *	A localized date-input field that does data validation when it looses focus or you hit Enter.
 *	Most common date formats are recognized, and a popup dialog lets you choose dates from
 *	a calendar.
 *	The initial value, if present, must specify a date. (An empty string isn't permitted.)
 *	If no value= attribute is specified, today's date is used as the initial value.
 *	The optional <em>size</em> attribute is the width of the field in columns.
 *	(An approximation is used to size the control.)
 *	</p>
 *
 *	<p>
 *	<font size=+1><b>Form Processing</b></font>
 *	 <p>
 *	 Normally, when an HTML form is submitted, the JEditorPane tries to
 *	 actually execute and HTTP POST or GET operation on a remote server,
 *	 passing it the data associated with the form elements as name=value
 *	 pairs.
 *	 <p>
 *	 You can modify form-processing behavior, so that a form is submitted
 *	 to the current program rather than to some server out on the net somewhere.
 *	 Just add an {@link java.awt.event.ActionListener} to the HTMLPane by
 *	 calling {@link #addActionListener addActionListener(...)}. The
 *	 {@link java.awt.event.ActionListener#actionPerformed} method of the listener
 *	 is called when the user hits the submit button. The associated
 *	 <code>ActionEvent</code> object is actually an instance of the
 *	 {@link FormActionEvent} class, and you can get the submit
 *	 data from it.
 *	 You may add as many action listeners as you like (they are all passed
 *	 same event object). This way, the handlers can
 *	 examine the <code>action=</code> attribute of the <code>&lt;form&gt;</code>
 *	 tag and process data only if the associated URL is of interest.
 *	 Once you've added any form handlers,
 *	 <u>all</u> form submissions will go to them rather than being posted to the
 *	 target URL. Your handler can relay the data to the web if it likes, but
 *	 it must do so if you want that behavior.
 *	 <p>
 *	 Here's an example of a simple form-submission handler that just prints
 *	 all the form-related information on standard output.
 *	 <PRE>
 *  pane.addActionListener
 *  (   new ActionListener()
 *      {   public void actionPerformed( ActionEvent event )
 *          {
 *              FormActionEvent act = (FormActionEvent)event;
 *
 *              System.out.println("\n"+ act.getActionCommand() );
 *              System.out.println("\t"+"method=" + act.method() );
 *              System.out.println("\t"+"action=" + act.action() );
 *              act.data().list( System.out );
 *              System.out.println("");
 *              try
 *              {   act.source().setPage    // display a "success" page
 *                  ( new URL(
 *                      "file://c:/src/com/holub/ui/HTML/test/submit.html")
 *                  );
 *              }
 *              catch( Exception e )
 *              {   e.printStackTrace();
 *              }
 *              System.out.println("");
 *          }
 *      }
 *  );
 *  </PRE>
 *	<p>
 *	<font size=+1><b>Known Problems</b></font>
 *	<p>
 *	This class is based on {@link JEditorPane}, which does not
 *	use the world's best HTML parser. The following problems that are caused
 *	by Sun's parser aren't fixed in the current implementation:
 *	<ol>
 *	<li>The parser is dog slow.
 *	<li>CSS support is seriously broken. Styles are useless.
 *	<li>None of the new HTML 4	or XHTML tags are handled.
 *	<li>The parser doesn't handle tables very well.
 *		Very simple table nesting is okay, but complicated stuff fails
 *		miserably.
 *	<li><code>&lt;applet&gt;</code> tags are not supported---they are
 *		actually obsolescent as of HTML 4---but <code>&lt;object&gt;</code>
 *		tags are supported.
 *		Use the latter to embed your applets in a <code>&lt;form&gt;</code>.
 *		See <a href="http://java.sun.com/j2se/1.4/docs/guide/plugin/developerGuide/usingTags.html">
 *		the Java-plugin docs</a> for a discussion of the correct way to do this.
 *	<li>The JDK 1.4 parser parser does not handle
 *		<code>&lt;input type=submit name=x value=y&gt;</code>
 *		correctly (The problem is fixed in JDK ver. 1.5).
 *		In particular, the name=value string is not placed in the
 *		form data as it should be, so you cannot use multiple
 *		type=submit input fields in a single form.
 *		The new &lt;button&gt; and &lt;input type=button&gt; elements aren't
 *		supported either, so you can't use that tag.
 *		<p>
 *		I've added a &lt;inputAction value="Text"&rt; tag for simple
 *		situations, but the implementation of the cancel tag does not
 *		give you access to the data that would com in with a normal
 *		submit operation.
 *	<li>The <code>JEditorPane</code> base class doesn't understand
 *		<code>&lt;script&gt;</code>
 *		tags (or JavaScript)---it just
 *		displays as normal text the contents of all
 *		<code>&lt;script&gt;</code> elements that are not nested inside
 *		comments.
 *		Unfortunately,
 *		javadoc-generated HTML doesn't follow the nest-script-in-comments
 *		convention, so you can't easily use <code>JEditorPane</code>
 *		as a Java-documentation browser.
 *	<li>The {@link JEditorPane} doesn't do frames correctly when you customize
 *		it (as I've done here).
 *		Frames appear to work, but pages displayed within
 *		frames are processed as if you were using a standard {@link
 *		javax.swing.JEditorPane}, with
 *		none of the features described here available to you. Consequently,
 *		you can't really use HTML frames. You can, however, create several
 *		HTMLPane instances and arrange them inside a JPanel (or other
 *		container) using a GridbagLayout or GridLayout.
 *	</ol>
 *	<p>
 *	I'm hoping that at least some {@link EditorKit} behavior
 *	will eventually get fixed. My guess is that the more fundamental
 *	structural problems (like the broken frame stuff) will probably
 *	stay broken, so the only long term solution is to toss the
 *	Sun implementation and replace it with something that works.
 *	<p>
 *	<DL>
 *	  <DT><B>Requires:</B></DT>
 *	  <DD>JDK1.4 (Regular expressions and
 *	  	<code>assert</code> statements are used.)
 *	  </DD>
 *	</DL>
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

//public class HTMLPane extends JEditorPane
// EkitCore needs to extend from JTextPane
// HTMLPane handles well customized tags
public class HTMLPane extends JTextPane
{
	private FilterFactory filterProvider = FilterFactory.NULL_FACTORY;

	private static	final Logger log = Logger.getLogger("com.holub.ui");

	/** A map of actual host names to replacement names, set by
	 *	 {@link #addHostMapping}
	 */
	private static  Map	  hostMap	 = null;

	/** Maps tags to Publishers of {@linkplain TagHandler handlers} for
	 *	 that tag
	 */
	private Map tagHandlers =
						Collections.synchronizedMap(new HashMap());

	/** A list of all components provided by a TagHandler that
	 *	 support the {@link TagBehavior} interface.
	 */

	private ActionListener actionListeners = null;


	/** A list of all JComponents that act as stand in for custom
	 *	tags that also implement TagBehavior.
	 */
	private Collection contributors = new LinkedList();

	/**
	 *	The name used as a key to get the tag-name attribute out of
	 *	the attributes passed to a TagHandler object.
	 */
	public static final String TAG_NAME = "<tagName>";

	/**
	 * All controls created from HTML tags (except for multi-line text input)
	 * are positioned so that they're aligned
	 * properly with respect to the text baseline. This way a radio button,
	 * for example, will line up with the text next to it. If you create
	 * a control of your own to be displayed in place of to a custom tag,
	 * you may want to issue a:
	 * <PRE>
	 * widget.setAlignmentY( BASELINE_ALIGNMENT );
	 * </PRE>
	 * request to get it aligned the same way as the standard controls
	 */

	public static final float BASELINE_ALIGNMENT = 0.70F;

	//@constructor-start
	/**
	 *	Create an empty pane. Populate it by calling
	 *	{@link JEditorPane#setPage(URL)} or {@link JEditorPane#setText(String)}
	 *	<PRE>
 	 *	HTMLPane form  = new HTMLPane();
	 *	form.setPage( new URL("file://test.html") );
	 *	</PRE>
	 *	(For reasons that are not clear to me, the URL and String versions
	 *	of the base-class constructors don't work when called from
	 *	a derived-class constructor, so these base-class constructors
	 *	are not exposed here.)
	 */

	public HTMLPane()
	{
	    // modified
		//registerEditorKitForContentType( "text/html",
		// 						"com.holub.ui.HTMLPane$HTMLPaneEditorKit" );

		//setEditorKitForContentType( "text/html", new HTMLPaneEditorKit() );

		setEditorKit( new HTMLPaneEditorKit() );

		setContentType			  ( "text/html" );
		
		// modified
		//addHyperlinkListener	  ( new HyperlinkHandler() );
		//setEditable				  ( false );

		// Set up to shut down pages gracefully when the parent window
		// shuts down. Note that this doesn't work when somebody closes
		// the outermost frame by clicking the "X" box, because the
		// handler for that event (which typically calls System.exit()) is
		// processed before the ancestor event is generated. This omission
		// is not a big deal, since the program is shutting down anyway,
		// but don't put a println in the following code and be surprised
		// when it doesn't get executed.

		addAncestorListener
		(	new AncestorAdapter()
			{	public void ancestorAdded(AncestorEvent e)
				{	for( Component c=HTMLPane.this; c!=null; c=c.getParent())
					{	if( c instanceof Window )
						{	((Window)c).addWindowListener
							(	new WindowAdapter()
								{	public void windowClosing(WindowEvent e)
									{	handlePageShutdown();
									}
								}
							);
							break;
						}
					}
				}
			}
		);
	}

	/**
	 *	Since destructors aren't possible, provide a method to handle page
	 *	shut down. Notify the JComponents associated with the custom tags that
	 *	the page is shutting down and do some local housekeeping.
	 */
	protected void handlePageShutdown()
	{
		for( Iterator i = contributors.iterator(); i.hasNext(); )
			((TagBehavior) i.next() ).destroy();

		contributors.clear();
	}

	/************************************************************************
	 *  If the argument is true, pre-install all custom-tag handlers
	 *  <a href="#customTags">described earlier</a>,
	 *  otherwise install none of the custom-tag handlers.
	 *  The no-arg constructor doesn't install any handlers, so
	 *  <code>new HTMLPane()</code> and <code>new HTMLPane(false)</code>
	 *  are equivalent.
	 * @param installDefaultTags
	 */
	public HTMLPane( boolean installDefaultTags )
	{   this();
		if( installDefaultTags )
		{   addTag( "size"        , new SizeHandler()            );
			addTag( "inputAction",  new InputActionHandler(this));
			addTag( "inputNumber",  new InputNumberHandler()    );
			addTag( "inputDate"  ,  new InputDateHandler()      );
		}
	}

 	//@constructor-end
	/**
	 *	The {@link JEditorPane} uses an editor kits to get a factory of
	 *	{@link View} objects, each of which is responsible for rendering
	 *	an HTML element on the screen. This kit returns a factory that
	 *	creates custom views, and it also modifies the behavior of the
	 *	underlying {@link Document} slightly.
	 */
	public class HTMLPaneEditorKit extends HTMLEditorKit
	{
		public ViewFactory getViewFactory()
		{	return new CustomViewFactory();
		}

		public Document createDefaultDocument()
		{	//HTMLDocument doc = (HTMLDocument)( super.createDefaultDocument() );

			// <0 for synchronous load. Delays the pop up, but allows
			// tags that set window size, etc. Default value is 4.
			//doc.setAsynchronousLoadPriority(-1);

			// The number of tokens to buffer before displaying them.
			// A smaller number makes the screen to appear a bit more
			// responsive because the system doesn't pause for a long
			// time before displaying anything.
			//
			// doc.setTokenThreshold(10);
			//
			// This is the default value. If it's false, then
			// custom tags won't be recognized.
			//
			// doc.setPreservesUnknownTags( true );
		    
		    // the createDefaultDocument in the EkitCore
			StyleSheet styles = getStyleSheet();
			StyleSheet ss = new StyleSheet();
			ss.addStyleSheet(styles);
			ExtendedHTMLDocument doc = new ExtendedHTMLDocument(ss);
			doc.setParser(getParser());
			doc.setAsynchronousLoadPriority(4);
			doc.setTokenThreshold(100);
			return doc;
			//return doc;
		}

		/**  Return a parser that wraps the real one. This is the
		 *	 only convenient way to get a handle to the input
		 *	 stream that the parser uses: Supply an input-stream
		 *	 decorator that preprocesses the input before
		 *	 the parser reads it.
		 *	 <p>Using a preprocessor at all
		 *	 is a kludge, but creating a DOM from user
		 *	 supplied HTML and inserting it into the
		 *	 HTMLDocument is nasty, and probably slower.
		 *	 <p>
		 *	 <code>Parser</code> and <code>ParserCallback</code>
		 *	 are both inner classes of the {@link HTMLEditorKit}
		 *	 base class.
		 */

		protected Parser getParser()
		{	final Parser p = super.getParser();
			return new Parser()
			{	public void parse(	Reader r,
									ParserCallback callBack,
									boolean ignoreCharSet
								 )	throws IOException
				{	p.parse( filterProvider.inputFilter(r),
										callBack, ignoreCharSet );
				}
			};
		}
	}
	//@editor-kit-end
	/*******************************************************************
	 *	Create Views for the various HTML elements. This factory differs from
	 *	the standard one in that it can create views that handle the
	 *	modifications that I've made to EditorKit. For the most part, it
	 *	just delegates to its base class.
	 */

	private final class CustomViewFactory extends HTMLEditorKit.HTMLFactory
	{
		// Create views for elements.
		// Note that the views are not created as the elements
		// are encountered; rather, they're created more or less
		// at random as the elements are displayed. Don't do anything here
		// that depends on the order in which elements appear in the input.
		//
		// Also note that undefined start-element tags are not in any way
		// linked to the matching end-element tag. They two might move
		// around arbitrarily.

		public View create(Element element)
		{	// dumpElement( element );
			HTML.Tag kind = (HTML.Tag)(
						element.getAttributes().getAttribute(
							javax.swing.text.StyleConstants.NameAttribute) );
			if( (kind==HTML.Tag.INPUT)	|| (kind==HTML.Tag.SELECT)
										|| (kind==HTML.Tag.TEXTAREA) )
			{
				// Create special views that understand Forms and
				// route submit operations to form observers only
				// if observers are registered.
				//
				FormView view = (actionListeners != null)
									? new LocalFormView( element )
									: (FormView)( super.create(element) )
									;

				String type = (String)( element.getAttributes().
										getAttribute(HTML.Attribute.TYPE));
				return view;
			}
			else if( kind instanceof HTML.UnknownTag )
			{	// Handling a custom element. End tags are silently ignored.
				if( element.getAttributes().
								getAttribute(HTML.Attribute.ENDTAG) == null)
				{
					final Component view = doTag( element );
					if( view != null )
					{	return	new ComponentView(element)
								{	protected Component createComponent()
									{	return view;
									}
								};
					}
					// else fall through and return default (invisible) View
				}
			} else if(kind instanceof HTML.Tag)
			{   // EkitCore handles IMG tag
				HTML.Tag tagType = (HTML.Tag)kind;
				if(tagType == HTML.Tag.IMG)
				{
					return new RelativeImageView(element);
				}
			}
			return super.create(element);
		}
	}
	//@view-factory-end
	/******************************************************************* 
	 *  Handle a request to process a custom tag. If no handler for
	 *	 a given tag is found, the fact is logged to the com.holub.ui
	 *	 logger, and the tag is ignored.
	 *
	 *	 @param element	The element that we're handling
	 *
	 *
	 *	 @return a JComponent to use as the view or <code>null</code>
	 *	 if there is no view.
	 */
	private final Component doTag( Element element )
	{
		if( element == null )			// it does happen!
			return null;

		String name	= element.getName();
		if( name == null )
			name = "Unknown" ;

		// Extract the attributes and tag name from the Element:

		Properties   attributes	= new Properties();
		AttributeSet set 		= element.getAttributes();

		for( Enumeration i = set.getAttributeNames(); i.hasMoreElements(); )
		{	Object current 			 = i.nextElement	();
			String attributeName    = current.toString	();
			Object attributeValue   = set.getAttribute	(current);

			attributes.put
			(	(current instanceof StyleConstants)? TAG_NAME: attributeName,
				attributeValue.toString()
			);

		}

		// Now look for a handler for the tag. If there isn't one, just
		// return null, which effectively causes the tag to be ignored.

		TagHandler handler = (TagHandler)( tagHandlers.get(name) );
		if( handler == null )
		{	log.warning( "Couldn't find handler for <" +name+ ">" );
			return null;
		}

		// There is a handler, call it to do the work. Return whatever
		// component the handler returns.

		
		JComponent component = handler.handleTag(this,attributes);
		
		if( component instanceof TagBehavior )
			contributors.add( component );
		return ( component );
	}
	//@do-tag-end

	/******************************************************************* 
	 * Special handling for elements that can occur inside forms.
	 */
	public final class LocalFormView extends javax.swing.text.html.FormView
	{
		public LocalFormView( Element element )
		{	super(element);
		}

		/** Chase up through the form hierarchy to find the
		 *	 <code>&lt;form&gt;</code> tag that encloses the current
		 *	 <code>&lt;input&gt;</code> tag. There's a similar
		 *	 method in the base class, but it's private so I can't use it.
		 */
		private Element findFormTag()
		{	for(Element e=getElement(); e != null; e=e.getParentElement() )
				if(e.getAttributes().getAttribute(StyleConstants.NameAttribute)
															==HTML.Tag.FORM )
					return e;

			throw new Error("HTMLPane.LocalFormView Can't find <form>");
		}

		/** Override the base-class method that actually submits the form
		 *	 data to process it locally instead if the URL in the action
		 *	 field matches the "local" URL.
		 */
		protected void submitData(String data)
		{	//System.out.println("Data [" + data + "]");
		    AttributeSet attributes = findFormTag().getAttributes();
			String action =
						(String)attributes.getAttribute(HTML.Attribute.ACTION);
			String method =
						(String)attributes.getAttribute(HTML.Attribute.METHOD);
			
			String name =
			    		(String)attributes.getAttribute(HTML.Attribute.NAME);

			if( action == null ) action = "";
			if( method == null ) method = "";
			if (name == null) name = "";

			// modified
			//handleSubmit( method.toLowerCase(), action, data );
			handleSubmit( method.toLowerCase(), action, name, data );
		}

		/** Override the base-class image-submit-button class. Given the tag:
		 *  <PRE>
         *  &lt;input type="image" src="grouchoGlasses.gif"
         *                 name=groucho value="groucho.pressed"&gt;
         * </PRE>
		 * The data will hold only two properties:
		 *	 <PRE>
		 *	groucho.y=23
		 *	groucho.x=58
		 *	</PRE>
		 *	Where 23 and 58 are the image-relative positions of the mouse when
		 *	the user clicked. (Note that the value= field is ignored.)
		 *	Image tags are useful primarily for implementing a cancel button.
		 *	<p>
		 *	This method does nothing but chain to the standard submit-
		 *	processing code, which can figure out what's going on by
		 *	looking at the attribute names.
		 */
		protected void imageSubmit(String data)
		{	submitData(data);
		}

		/** Special processing for the reset button. I really want to
		 *	 override the base-class resetForm() method, but it's package-
		 *	 access restricted to javax.swing.text.html.
		 *	 I can, however, override the actionPerformed method
		 *	 that calls resetForm() (Ugh).
		 */
		public void actionPerformed( ActionEvent e )
		{	String type = (String)( getElement().getAttributes().
										getAttribute(HTML.Attribute.TYPE));
			if( type.equals("reset")  )
				doReset( );
			super.actionPerformed(e);
		}

		/** Make transparent any standard components used for input.
		 *	 The default components are all opaque with a gray (0xd0d0d0)
		 *	 background, which looks awful when you've  set the background
		 *	 color to something else (or set it to an image) in the
		 *	 <code>&lt;body&gt;</code> tag. Setting opaque-mode
		 *	 off lets the specified background color show through the
		 *	 <code>&lt;input&gt;</code> fields.
		 */
		
		// modified
		/*protected Component createComponent()
		{	JComponent widget = (JComponent)( super.createComponent() );

			// The widget can be null for things like type=hidden fields

			if( widget != null )
			{
				if( !(widget instanceof JButton) )
					widget.setOpaque(false);

				// Adjust the alignment of everything except multiline text
				// fields so that the control straddles the text baseline
				// instead of sitting on it. This adjustment will make
				// buttons and the text within a single-line text-input
				// field align vertically with any adjacent text.

				if( !(widget instanceof JScrollPane) ) // <input>
				{	widget.setAlignmentY( BASELINE_ALIGNMENT );
				}
				else
				{	// a JList is a <select>, a JTextArea is a <textarea>
					Component contained =
								((JScrollPane)widget).getViewport().getView();

					// If it's a select, change the width from the default
					// (full screen) to a bit wider than the actual contained
					// text.
					if( contained instanceof JList )
					{	widget.setSize( contained.getPreferredSize() );

						Dimension idealSize = contained.getPreferredSize();
						idealSize.width  += 20;
						idealSize.height += 5;

						widget.setMinimumSize  ( idealSize );
						widget.setMaximumSize  ( idealSize );
						widget.setPreferredSize( idealSize );
						widget.setSize		   ( idealSize );
					}
				}
			}
			return widget;
		}*/
	}
	//@local-form-view-end
	/*******************************************************************
	 *	Used by {@link HTMLPane} to pass form-submission information to
	 *	any ActionListener objects.
	 *	When a form is submitted by the user, an actionPerformed() message
	 *	that carries a FormActionEvent is sent to all registered
	 *	action listeners. They can use the event object to get the
	 *	method and action attributes of the form tag as well as the
	 *	set of data provided by the form elements.
	 */

	public class FormActionEvent extends ActionEvent
	{	private final String 	 method;
		private final String 	 action;
		private final String	 name;
		private final Properties data = new Properties();

		// modified
		private FormActionEvent( String method, String action, String name, String data){
		    super( HTMLPane.this, 0, "submit" );
			this.method	= method;
			this.action	= action;
			this.name = name;
			try
			{
				data = UrlUtil.decodeUrlEncoding(data) + "\n" + dataFromContributors();
				this.data.load( new ByteArrayInputStream( data.getBytes()) );
			}
			catch( IOException e )
			{ //assert false : "\"Impossible\" IOException";
			}
		}
		/**
		 * @param method	method= attribute to Form tag.
		 * @param action	action= attribute to Form tag.
		 * @param data		Data provided by standard HTML element. Data
		 * 					provided by custom tags is appended to this set.
		 */
		private FormActionEvent( String method, String action, String data )
		{	super( HTMLPane.this, 0, "submit" );
			this.method	= method;
			this.action	= action;
			this.name = null;
			try
			{
				data = UrlUtil.decodeUrlEncoding(data) + "\n" + dataFromContributors();
				this.data.load( new ByteArrayInputStream( data.getBytes()) );
			}
			catch( IOException e )
			{ //assert false : "\"Impossible\" IOException";
			}
		}

		/** Return the method= attribute of the &lt;form&gt; tag */
		public String	  method()	{ return method; }

		/** Return the action= attribute of the &lt;form&gt; tag */
		public String	  action()	{ return action; }

		public String	  name()	{ return name; }
		/** Return the a set of properties representing the name=value
		 *  pairs that would be sent to the server on form submission.
		 */
		public Properties data()	{ return data; }

		/** Convenience method, works the same as
		 *  <code>(HTMLPane)( event.getSource() )</code>
		 */
		public HTMLPane source(){ return (HTMLPane)getSource(); }
	}
	//@form-action-event-end

	//-----------------------------------------------------------------
	public final void addActionListener( ActionListener listener )
	{	actionListeners = AWTEventMulticaster.add(actionListeners, listener);
	}

	public final void removeActionListener( ActionListener listener )
	{	actionListeners = AWTEventMulticaster.remove(actionListeners, listener);
	}

	private final void handleSubmit(final String method, final String action,
														  final String data )
	{	actionListeners.actionPerformed(
							new FormActionEvent
							(	method,
								action,
								UrlUtil.decodeUrlEncoding(data)
									+ "\n"
									+ dataFromContributors()
							)
						);
	}

	// modified
	private final void handleSubmit(final String method, final String action, 
	        						final String name, final String data )
	{	actionListeners.actionPerformed(
	        new FormActionEvent(method, action, name,
	                			UrlUtil.decodeUrlEncoding(data)
	                			+ "\n"
	                			+ dataFromContributors())
		);
	}
	
	/*package*/ final void handleInputActionTag( final String name )
	{	actionListeners.actionPerformed(new FormActionEvent( "", "", "" ));
	}

	/*** ****************************************************************
	 *   Add support for a custom tag.
	 *	 Custom tags may contain arbitrary attributes (which are passed to your
	 *	 handler in a {@link Properties} object). Elements specified
	 *	 by a custom tag may contain plain-text contents;
	 *	 any nested elements are discarded. The end-tag element is
	 *	 <u>required</u> in the input, since your handler isn't called
	 *	 until the end-tag element is found.
	 *	 <p>
	 *	 Only one handler can be registered for a given tag.
	 *	 If you register more than one, then the most recent registration wins.
	 *	 All tag handlers must be installed before the page is loaded
	 *	 by {@link #setText}, {@link #setPage}, or equivalent.
	 *	 <p>
	 *	 The handler can choose to return a JComponent, which will appear on
	 *	 the rendered page in place of the tag. By default, the component
	 *	 sits on the text baseline. Use {@link JComponent#setAlignmentY}
	 *	 to control the placement relative to the text baseline. A value of
	 *	 0.75 will place 75% of the Component above the baseline, for example.
	 *	 (a value of .85 is about right for a JLabel in the default font.)
	 *	 Here's a custom tag
	 *	 <code>&lt;holub:label text=&quot;<i>TEXT</i>&quot;&gt;</code>
	 *	 that is replaced by a JLabel that holds the <i>TEXT</i> string:
	 *	 <PRE>
     *  pane.addTag
     *  (   "myTag",
     *      new TagHandler()
     *      {   public JComponent
     *          handleTag(HTMLPane source, Properties attributes)
     *          {  JComponent view = new JLabel(attributes.getProperty("text"));
     *             view.setAlignmentY(0.85F);
     *             return view;
     *          }
     *      }
     *  );
     *  </PRE>
	 *	Tag handlers are shared by <u>all</u> instances of HtmlFrame.
	 *	<p>
	 *	Note that <b>tags are not case sensitive</b>, so regardless
	 *	of how the tag appears in the input (or in the tag argument),
	 *	it's treated as if it's all lower-case characters.
	 *
	 *	 @param tag the tag name (without any "angle" brackets).
	 *				Must be all-upper-case or all-lower-case
	 *	 		letters or underscores (no colons, no mixed case).
	 *			Normal HTML tags cannot be redefined.
	 *	 		Because of the way that the {@link JEditorPane} base
	 *	 		class works,
	 *	 		you can't use use "p-implied" or "content" as a custom-tag
	 *	 		name and you can't use "endtag=" as an attribute.
	 *			If assertions are enabled, attempts to register a
	 *			standard tag generate <code>AssertionError</code>s, but if
	 *			assertions are not  enabled, the request is logged,
	 *			but is otherwise ignored (and will form a minor memory leak.)
	 *
	 *	 @param handler The handler to call when the tag is encountered.
	 *	 @see TagHandler
	 */

	public final void addTag(String tag, TagHandler handler)
	{	tag = tag.toLowerCase();
		//assert !isStandardHTMLtag(tag) :
		//		"Illegal attempt to redefine standard HTML tag <"+tag+">";
		//log.info( "Adding custom tag <" + tag + ">" );
		tagHandlers.put( tag, handler );
	}

	/** Remove the handler for a given tag. Once this call is made,
	 *	 the tag will be ignored if it's encountered in a newly-loaded
	 *	 page. (A warning is logged if an unexpected tag is
	 *	 encountered, but this situation is not considered to be a
	 *	 run-time error.)
	 */

	public final void removeTag( String tag )
	{	tagHandlers.remove( tag );
	}


	/** Notify the JComponents associated with the custom tags that
	 *	 the user has hit the reset button.
	 */

	protected void doReset()
	{	for( Iterator i = contributors.iterator(); i.hasNext(); )
			((TagBehavior) i.next() ).reset();
	}

	/** Collect form data from the JComponents associated with the
	 *	 custom tags and append it to the data set passed to the
	 *	 form handlers. This method has been made protected so that
	 *	 overrides can do some sort of processing or filtering
	 *	 on the data if they need to.
	 *	 @return a set of newline-delimited key=value pairs contained
	 *	 in a single String.
	 */

	protected String dataFromContributors()
	{
		StringBuffer formData = new StringBuffer();
		for(Iterator i = contributors.iterator(); i.hasNext(); )
		{	formData.append( ((TagBehavior) i.next() ).getFormData() );
			formData.append("\n");
		}
		return formData.toString();
	}

	/** Workhorse function used by the assertion at the top of
	 *	 {@link #addTag}. You can use this method to see
	 *	 if a tag is available before attempting to register it.
	 *	 Note that the tag must be specified using all-lower-case
	 *	 characters.
	 *
	 *	 @return <code>true</code> if the tag argument specifies a standard
	 *	 HTML tag or one of the internal tags "p-implied" or "content."
	 *	 Otherwise, return <code>false</code>.
	 */

	public static final boolean isStandardHTMLtag(String tag)
	{	HTML.Tag[] allTags = HTML.getAllTags();

		if( tag.equals("p-implied") || tag.equals("content") )
			return true;

		for( int i = 0; i < allTags.length; ++i )
			if( allTags[i].toString().toLowerCase().equals(tag) )
				return true;

		return false;
	}

	/******************************************************************* 
	 * Provide input preprocessing.
	 * Use this method to replace the default input filter
	 * {@link FilterFactory#NULL_FACTORY}.
	 *
	 * @see FilterFactory
	 * @see NamespaceFilterFactory
	 */

	public void filterInput( FilterFactory provider )
	{	filterProvider = provider;
	}

	/**
	 *	A convenience method for local testing of a UI that will eventually
	 *	be web based. If you map your home URL to a local directory,
	 *	all links to the home URL are automatically replaced by
	 *	links to the directory. For example, given
	 *	<PRE>
	 *	myPane.addHostMapping( "www.holub.com", "file://c:/src/test" );
	 *	</PRE>
	 *	an HTML "anchor" that looks like this:
	 *	<PRE>
	 *	&lt;a href="http://www.holub.com/dir/foo.html"&gt;
	 *	</PRE>
	 *	is treated as if you had specified:
	 *	<PRE>
	 *	&lt;a href="file://c:/src/test/dir/foo.html"&gt;
	 *	</PRE>
	 *	Multiple mappings are supported. That is, if you call this method
	 *	more than once, then all the mappings you specify apply.
	 *	<p>
	 *	The host mappings are shared by <u>all</u> instances of
	 *	HtmlFrame, including popups created by target=_blank in a
	 *	hyperlink.
	 *
	 *	 @see #removeHostMapping
	 */
	public static final void addHostMapping( String thisHost,
										String mapsToThisLocation )
	{	if( hostMap == null )
			hostMap = new HashMap();

		hostMap.put( thisHost,
						new HostMapping(thisHost, mapsToThisLocation));
	}

	/** Remove a host mapping added by a previous call to
	 *	 {@link #addHostMapping}.
	 *	 @see #addHostMapping
	 */
	public static final void removeHostMapping( String thisHost )
	{	hostMap.remove(thisHost);
	}

	/** Map a url if there's an entry for the host portion in the host map.
	 *	 @param url a URL specified in terms of the "host" location.
	 *	 @return a URL for the mapped location.
	 *	 @see #addHostMapping
	 *	 @see #removeHostMapping
	 */
	public URL map(URL url)
	{	if( hostMap != null )
		{	HostMapping mapping = (HostMapping) hostMap.get(url.getHost());
			if( mapping != null )
				url = mapping.map( url );
		}
		return url;
	}

	/*******************************************************************
	 * This class handles the host-to-local-file mapping mechanics
	 *	 for {@link #addHostMapping}. The {@link #hostMap}
	 *	 table is a java.util.Map of objects of this class.
	 */
	private static final class HostMapping
	{	private final String remote;
		private final String local;
		public HostMapping( String remote, String local )
		{	this.remote = ".*://"   + remote.replaceAll("\\.", "\\.");
			this.local  = local ;
		}
		public URL map( URL page )
		{	try
			{	String s = page.toExternalForm();
				return new URL( s.replaceFirst(remote, local) );
			}
			catch( MalformedURLException e )
			{	log.warning( "Couldn't map " + remote + " to "
								+ local + ": " + e.getMessage() );
			}
			return page;
		}
	}
	//@hyperlink-handler-start
	/************************************************************************
	 *	This Hyperlink handler replaces the contents of the current pane
	 *	with whatever's at the indicated link. This code is copied more or
	 *	less verbatim from the Sun documentation. I've also added simple
	 *	support for the mailto: protocol.
	 */

	private final class HyperlinkHandler implements HyperlinkListener
	{	public void hyperlinkUpdate(HyperlinkEvent event)
		{	try
			{	if( event.getEventType() == HyperlinkEvent.EventType.ACTIVATED )
				{
					HTMLPane source 	 = (HTMLPane)event.getSource();
					String   description = event.getDescription();
					Element  e      	 = event.getSourceElement();

					// Get the attributes of the <a ...> tag that got
					// us here, then extract the target= attribute. If
					// we find target=_blank, then display the page
					// in a popup window. I'm assuming that the
					// href references an html file, because it wouldn't
					// make much sense to use target=_blank if it didn't.

 					AttributeSet tagAttributes = (AttributeSet)
							(e.getAttributes().getAttribute(HTML.Tag.A));

					String target = null;
					if( tagAttributes != null )
						target = (String) tagAttributes.getAttribute(
													HTML.Attribute.TARGET);

					if( target != null &&  target.equals("_blank")  )
					{	popupBrowser( event.getURL() );
						return;
					}

					// Handle http: and file: links. If the description
					// doesn't contain a protocol (there's no ':'),
					// then assume a "relative" link:

					if(		description.startsWith("http:")
						||	description.startsWith("file:")
						||	description.indexOf(":") == -1
					  )
					{
						JEditorPane pane = (JEditorPane) event.getSource();

						if(event instanceof HTMLFrameHyperlinkEvent)
						{  ((HTMLDocument)(source.getDocument())).
								 processHTMLFrameHyperlinkEvent(
										 	   (HTMLFrameHyperlinkEvent)event);
						}
						else if( !redirect(event.getURL()) )
						{  unknownRedirect(event.getDescription());
						}
					}
					else if( description.startsWith("mailto") )
					{	if( isWindows() )
						   Runtime.getRuntime().exec(
											"cmd.exe /c start " + description);
						else
						   unknownRedirect(event.getDescription());
					}
					else
					{	unknownRedirect( event.getDescription() );
					}
				}
			}
			catch( Exception e )
			{	log.warning
				( "Unexpected exception caught while processing hyperlink: "
					+ e.toString() + "\n"
					+ Log.stackTraceAsString( e )
				);
			}
		}

		/** Used for anchor tags that include target=_blank attributes. Pop up
		 *	 a new instance of an HTMLPanel in a subwindow, displaying the page
		 *	 whose URL is specified in the "description" argument.
		 */
		private final void popupBrowser( URL target )
												throws MalformedURLException
		{ 	JFrame	 popupFrame	= new JFrame();
			HTMLPane popup			= new HTMLPane();
			popupFrame.getContentPane().  add( new JScrollPane(popup) );

			popup.redirect(target);
			Dimension size = popup.getPreferredSize();
			Point location = getLocationOnScreen();
			popupFrame.setBounds(  location.x + 10,
									location.y + 10,
									size.width,
									size.height );
			popupFrame.show();
		}

		/** Mailto support is OS specific, so we need to know the OS.
		 */
		private final boolean isWindows()
		{	return( System.getProperty("os.name").toLowerCase().
											indexOf("windows") != -1);
		}
	}


	/*******************************************************************
	 * Handles hyperlinks for the http:// or file:// protocols.
	 *	Not all files are loaded, and a name-based algorithm
	 *	is used to determine what gets loaded and what doesn't
	 *	get loaded.
	 *	All requests that are not handled here (i.e. for a protocol
	 *	other than http: or file:, or  for a file with an extension
	 *	not on the list, below) are routed to
	 *	{@link #unknownRedirect(String)}
	 *	for processing.
	 *	Overload the current method to change the way that the
	 *	http: and file: protocols are processed.
	 *	Overload {@link #unknownRedirect(String)} to add support
	 *	for other protocols.
	 *	<p>
	 *	Any host mappings specified to
	 *	{@link #addHostMapping} are processed first.
	 *	Then, those URLs that end in
	 *	one of the following extensions are handled.
	 *	<table border=0 cellspacing=0 cellpadding=0>
	 *		<tr><td><code>.shtml</code></td><td> HTML file </td></tr>
	 *		<tr><td><code>.html	</code></td><td> HTML file </td></tr>
	 *		<tr><td><code>.htm	</code></td><td> HTML file </td></tr>
	 *		<tr><td><code>.pl	</code></td><td> Perl script </td></tr>
	 *		<tr><td><code>.jsp	</code></td><td> Java Server Page 	</td></tr>
	 *		<tr><td><code>.asp	</code></td><td> Active Server Page </td></tr>
	 *		<tr><td><code>.php	</code></td><td> PHP script </td></tr>
	 *		<tr><td><code>.py	</code></td><td> Python Script </td></tr>
	 *	</table>
	 *	<p>
	 *	All URL's whose paths do not have a '.' in the string that follows the
	 *	rightmost slash (including directory specifications, which should be
	 *	terminated by a slash) are assumed to reference an implicit index.html
	 *	file. Similarly, a URL that	specifies a host or directory, but no file
	 *	(e.g. "http://www.holub.com" or "http://www.holub.com/directory/),
	 *	is loaded.
	 *	<p>
	 *	N.B.:	A relative file that doesn't have an extension (such as
	 *	<code>&lt;a href=filename&gt;</code> is not recognized as
	 *	an HTML file, so is not processed.
	 *	<p>
	 *	You can override this method if you need to change this default
	 *	behavior, This method is called from the Swing Event Thread, so
	 *	it's safe for your override to use
	 *	{@link JEditorPane#setPage(URL)}
	 *
	 *	 @param page the target page. Must be a file: or http: URL.
	 *	 @return true if the page was handled. If you return false,
	 *			{@link #unknownRedirect(String)} is given a chance
	 *			to process it.
	 */

	public boolean redirect( URL page )
	{	//assert( page.getProtocol().equals("http")
		//		|| page.getProtocol().equals("file") );
		try
		{ 	String file = page.getFile();
			if( !(file.length()==0 || htmlExtensions.matcher(file).matches()) )
				return false;
			setPage( page );	// local version of setPage handles host mapping
		}
		catch (Throwable t)
		{	String message = "HTMLPane couldn't open hyperlink: "
													+ t.getMessage();
			log.warning( message );
			JOptionPane.showMessageDialog(
								HTMLPane.this,
								message,
								"401 Error",
								JOptionPane.WARNING_MESSAGE );
		}
		return true;
	}

	/** A regular expression that identifies all file extensions
	 *	 recognized by {@link #redirect(URL)}
	 *	 as an HTML file. The same expression also recognizes
	 *	 directories (that end in a slash) and all file names
	 *	 that don't have an extension.
	 */
	private static final Pattern htmlExtensions =
		Pattern.compile(  "(.*\\.(html|htm|pl|jsp|asp|php|py|shtml)([?#].*)?$)"
						+ "|(.*/[^\\./]+([?#].*)?$)"
						+ "|(.*/([?#].*)?$)",
						Pattern.CASE_INSENSITIVE );

	/** Handles all hyperlink protocols that aren't recognized by
	 *  {@link #redirect redirect(...)}.
	 *	Is also called if a mailto: protocol is specified and
	 *	we're not running under Windows. This implementation just logs
	 *	a warning to "com.holub.ui" and pops up a warning-style dialog box
	 *	indicating that the protocol isn't supported. You can override
	 *	this method to support protocols other than file:// and http://
	 */
	public void unknownRedirect( String request )
	{	log.warning("HTMLPane: Protocol or file type not supported ("
													+ request + ")" );
		JOptionPane.showMessageDialog
		(	HTMLPane.this,
			"Protocol or file type not supported (" + request + ")",
			"Link Error",
			JOptionPane.WARNING_MESSAGE
		);
	}

	//@hyperlink-handler-end
	/*******************************************************************
	 *	 Overrides {@link JEditorPane#setText(String)}
	 *	 to do general housekeeping.
	 *	 @see #setPage(URL)
	 *	 @see #setPage(String)
	 */
	public final void setText(String text)
	{	handlePageShutdown();
		super.setText(text);
	}

	/** Overrides {@link JEditorPane#setPage(URL)} to
	 *	 map hosts, and do general housekeeping.
	 *	 @see #setText(String)
	 *	 @see #setPage(String)
	 */
	public final void setPage(URL url) throws IOException
	{	handlePageShutdown();
		super.setPage( map(url) );
	}

	/** This version of setPage is disabled in an HTMLPane. You can
	 *	 use setPage(new URL(...)); if all you have is a string.
	 *	 @see #setText(String)
	 *	 @see #setPage(URL)
	 *	 @throws UnsupportedOperationException always
	 */
	public final void setPage(String location)
	{	throw new UnsupportedOperationException(
				"setPage(String) not supported by HTMLPane" );
	}

	/** Read is disabled in an HTMLPane.
	 *	 @throws UnsupportedOperationException always
	 */
	public void read(InputStream in, Object desc) throws IOException
	{	throw new UnsupportedOperationException(
				"read() not supported by HTMLPane" );
	}

	/** A version of {@link JEditorPane#setPage(URL)}
	 *	 that can be called safely from somewhere other than a Swing
	 *	 event handler. Note  that your form handlers <i>are</i>
	 *	 being called from an event handler.
	 */
	public void setPageAsynchronously(final URL page) throws IOException
	{	SwingUtilities.invokeLater
		(	new Runnable()
			{	public void run()
				{	try
					{	setPage(page);
					}
					catch(IOException e)
					{ log.warning("HTMLPane: setPage() failed on Event Thread");
					}
				}
			}
		);
	}
//@end

	/*******************************************************************
	 *	A test class. Creates pages using test/main.html and
	 *	test/submit.html. This test is, unfortunately, highly interactive.
	 */

	private static class Test
	{
		static
		{
			try
			{	UIManager.setLookAndFeel(
					UIManager.getSystemLookAndFeelClassName() );
			}
			catch( Exception e )
			{	e.printStackTrace();
				System.exit(-1);
			}
		}

		public static void main( String[] args )
		{
		try
		{	// Use the Windows look and feel.

			URL mainPage;

			mainPage =new URL( args.length >= 1 
									? args[0]
									: "file:/c:/src/com/holub/ui/HTML/test/main.html" );

			Log.toScreen("com.holub.ui");
			final HTMLPane pane = new HTMLPane();

			pane.addActionListener
			(	new ActionListener()
				{	public void actionPerformed( ActionEvent event )
					{
						FormActionEvent act = (FormActionEvent)event;
						act.data().list(System.out);
						String value = act.data().getProperty( "value" );
						if( value != null && value.equals("Cancel") )
						{	System.out.println("CANCEL");
						}
						else
						{	System.out.println("\n"+ act.getActionCommand() );
							System.out.println("\t"+"method=" + act.method() );
							System.out.println("\t"+"action=" + act.action() );
							act.data().list( System.out );
							System.out.println("");
							try
							{	act.source().setPage
								( new URL(
									"file:/c:/src/com/holub/ui/HTML/test/submit.html")
								);
							}
							catch( Exception e )
							{	e.printStackTrace();
							}
							System.out.println("");
						}
					}
				}
			);

			pane.addHostMapping( "www.test.com",
									"file:/c:/src/com/holub/ui/test" );

			// should fail with an assertion because of null handler
			try { pane.addTag( "B", null ); } catch(AssertionError e){}

			pane.addTag
			(	"size",
				new TagHandler()
				{ public JComponent
				  handleTag(HTMLPane source, Properties attributes)
				  { source.setPreferredSize
					(	new Dimension
						( Integer.parseInt(attributes.getProperty("width")),
						  Integer.parseInt(attributes.getProperty("height"))
						)
					);
					return null;
				  }
				}
			);

			// Converts tags of the form <abcd:efg> into <abcd_efg>
			// The algorithm looks for a <, followed by non-white
			// characters other than >, followed by a :, and replaces
			// the : with an _.

			pane.filterInput( new NamespaceFilterFactory() );

			pane.addTag			// handle <holub:JLabel> tags
			(	"holub_JLabel",
				new TagHandler()
				{	public JComponent
					handleTag(HTMLPane source,Properties a)
					{	JComponent view = new JLabel(a.getProperty("text"));
						view.setAlignmentY(0.85F);
						return view;
					}
				}
			);

			class ContributingText extends JTextField implements TagBehavior
			{	private final String value;
				private final String name;
				public ContributingText(String name, String value)
				{	super(value);
					this.name	=name;
					this.value	=value;
				}

				public void reset()
				{
					setText( value );
					invalidate();
				}

				public void destroy()
				{	System.out.println("Destroying <textInput> control");
				}

				public String getFormData ()
				{	return name + "=" + getText();
				};

				public Dimension getPreferredSize()
				{	return new Dimension(150,20);
				}
				public Dimension getMinimumSize()
				{	return getPreferredSize();
				}
				public Dimension getMaximumSize()
				{	return getPreferredSize();
				}
			}


			pane.addTag
			(	"textInput",
				new TagHandler()
				{	public JComponent
					handleTag(HTMLPane source,Properties attributes)
					{	return new ContributingText
									( attributes.getProperty("name"),
									  attributes.getProperty("value") );
					}
				}
			);


			pane.addTag
			(	"test",
				new TagHandler()
				{	public JComponent
					handleTag(HTMLPane source,Properties attr)
					{	System.out.println
						(	"\n<"
							+ attr.getProperty(HTMLPane.TAG_NAME)
							+">"
						);
						attr.list( System.out );
						return null;
					}
				}
			);



			JFrame 	frame = new JFrame();
			frame.addWindowListener
			(	new WindowAdapter()
				{	public void windowClosing( WindowEvent e )
					{	System.exit(255);
					}
				}
			);

			frame.getContentPane().add( new JScrollPane(pane) );

			try
			{	pane.setPage( mainPage );
			}
			catch(Exception e)
			{	System.err.println( "Can't open " + mainPage + "\n" );
				e.printStackTrace();
			}

			frame.setSize( pane.getPreferredSize() );
			frame.pack();
			frame.show();

			/*
			createSmallFrame
			(	new Runnable()
				{	public void run()
					{	createSmallFrame(null);
					}
				}
			);
			*/

		}
		catch( Throwable e )
		{	e.printStackTrace();
		}
		}
	}

	// Create a small frame. When it shuts down (is submitted)
	// it executes r.run() [before disposing the current window]
	//
	private static void createSmallFrame( final Runnable r )
	{
		try
		{
			final JFrame   frame = new JFrame();
			final HTMLPane pane  = new HTMLPane();

			pane.addActionListener
			(	new ActionListener()
				{	public void actionPerformed(ActionEvent e)
					{
						if( r != null )
							r.run();

						frame.setVisible(false);
						frame.dispose();
					}
				}
			);
			pane.setPage(
				new URL("file:/c:/src/com/holub/ui/HTML/test/second.html"));

			frame.getContentPane().add( pane );
			frame.pack();
			frame.show();
		}
		catch(Exception e)
		{	e.printStackTrace();
		}
	}
}
//@end
