SWOOP v2.3 beta 3 
Release Date: January 4, 2006
----------------------------------------------------

Organization: MINDSWAP (http://www.mindswap.org), University of Maryland, College Park
Supervisor: Professor James Hendler (hendler@cs.umd.edu)
Project site: http://www.mindswap.org/2004/SWOOP
Mailing Lists: http://lists.mindswap.org/mailman/listinfo/swoop (users), http://lists.mindswap.org/mailman/listinfo/swoop-devel (developers)

Primary Developers (in alphabetical order): 
Aditya Kalyanpur (aditya@cs.umd.edu)
Bijan Parsia (bparsia@isr.umd.edu)
Evren Sirin (evren@cs.umd.edu)

Other Contributors (in alphabetical order):
Bernardo Cuenca Grau (bernardo@mindlab.umd.edu) 
Daniel Hewlett (dhewlett@umd.edu) 
Michael Grove (mhgrove@hotmail.com) 
Ron Alford (ronwalf@wam.umd.edu) 
Taowei David Wang (tw7@cs.umd.edu) 
Vladimir Kolovski (kolovski@cs.umd.edu)
---------------------------------------------------

Installation Instructions: After unzipping the downloaded file (SWOOP-xxxx.zip), execute runme.bat (/ runme.sh) present inside the "SWOOP-xxxx" directory to start the application on a Windows (/ Mac or Unix) machine. For loading large ontologies such as NCI, you need to allocate more memory for Swoop - use the runme-HIGH file in this case.

***** Important Notes: *****

* The application requires Java 1.4 installed on your machine. You can download the latest version of Java from http://java.sun.com/j2se/1.4/download.html

* The SWOOP application includes/uses the following API's:
** WonderWeb OWL API (http://sourceforge.net/projects/owlapi) MODIFIED SOURCE (see changelog at the end of source file)
** XNGR API (http://xngr.org/)
** Jakarta Slide WebDAV API (http://jakarta.apache.org/slide/) 
** QTag API(http://www.english.bham.ac.uk/staff/omason/software/qtag.html) developed by Oliver Mason, whom we thank for allowing us to bundle qtag.jar with this Swoop release (please see licensing.txt for more information about its use)
** Hexidec Ekit API (http://www.hexidec.com/ekit.php) MODIFIED SOURCE (see changelog at the end of source file)
which are located in the /lib sub-directory under SWOOP. Additional jars in the lib directory (if present) are plugin dependencies.

* SWOOP employs a plugin based system for easy extension. Sample plugins can be downloaded from http://www.mindswap.org/2004/SWOOP/plugins

--------------------------------------------------------------

Software Description: 

SWOOP is an OWL Ontology browser/editor that takes the standard Web browser as the basic UI paradigm. Swoop includes many of the familiar features of a Web browser such as an address bar and history buttons,  bookmarks, hypertextual navigation etc. and applies them to the problem of browsing and editing Web based ontologies. Features include:

* It is simple to load ontologies from the web and to navigate within and between them.
* Multiple ontologies may be loaded at the same time.
* Ontologies, classes, properties, and individuals are rendered in a high level, accessible manner.
* One can "view the source" of ontologies and their entities in a number of common syntaxes (e.g. RDF/XML, the OWL Abstract Syntax, Turtle).
* OWL reasoners can be integrated for subsumption, consistency checking etc. -- default reasoners include a RDFS-like simple reasoner and Pellet, a Description Logic Tableaux Reasoner.
* Ontology change management with extensive rollback and undo mechanisms
* Share Annotations on Ontologies using the Annotea Protocol. Also attach and distribute Ontology Change sets with Annotations
* Search across multiple ontologies and 'find all references' of an OWL named entity
* Compare entities using a Resource Holder
* Export Ontologies directly to a remote WebDav store

This version contains some advanced, experimental features such as:

* Debug Ontologies using Pellet (explanations for unsatisfiable classes & inconsistent ontologies)
* Run "sound and complete" conjunctive ABox queries (written in RDQL) on an ontology using Pellet
* Partition Ontologies automatically by transforming them into an E-connection
* Crop Circles visualization of class hierarchy

-------------------------------------------------------------
New Features in v2.3 beta 3:
* Ability to edit entities in Turtle Syntax, Abstract Syntax and RDF/XML with ontology changes logged as well
* Support explanation of inferences for complex ontologies such as Wine, Pizza, Galen etc. 
* Ability to generate and export HTML for an Ontology, Class, Property or Individual based on current selection in display
* Plugin for "Natural Language" Rendering of OWL Entities included
* Ability to Auto-Save Workspace at regular intervals
* Numerous improvements to the CropCircles UI

-------------------------------------------------------------
Bugs fixed since previous version:
* ABox Query (missing jar) fixed
* Fixed problem due to removal of instances
* Fixed hasValue restriction on datatype properties
* Made component-names consistent across UI
* Fixed problem with cancellation of progress bar when reasoning with Pellet

-------------------------------------------------------------
New Features in v2.3 beta 2:

* Entities can be displayed using either their URIs or their rdfs:label or in a particular language when xml:lang is used in the rdfs:label (see option in File->Preferences)
* Ability to sort and remove Bookmarks
* Ability to set an HTTP proxy server host/port when using Swoop behind a firewall
* Progress bar displayed when shifting to Pellet reasoner
* Explanation of inference improved: strikes out irrevelant parts of axioms
* "Save Ontology" menu option label changes when associated with an ontology

-------------------------------------------------------------
Bugs fixed since previous version:

* Renaming OWL Entities works properly now
* Axiom tracing catches more axioms (new version of pellet-debug.jar included)
* Version Control update/commit to repository works properly now
* Overwrite prompt appears only when file exists and trying to save file
* Fixed some RDF/XML serialization bugs (e.g. xml:lang instead of rdf:language)
* Fixed errors in tree statistics in Swoop OntologyInfo pane

-------------------------------------------------------------
New Features in v2.3 beta 1:

* Latest version of Pellet 1.3 for reasoning..handles large, complex ontologies such as Wine, Galen, Pizza etc.
* Explanation support for inferences when Pellet is turned on (see "Why?" link next to italicized assertion)
* Version control using Annotea (see menu option: Advanced->Version Control)
* Support for adding arbitrary class expressions
* Support for viewing, creating and deleting GCIs in the ontology (see "Show GCIs" option for alphabetical list)
* Advanced Ontology Statistics pane

-------------------------------------------------------------
Bugs fixed since previous versions:

* Bookmarks now work properly for local files as well
* Anonymous individuals now appear in the alphabetical list ("Show Individuals")
* Warning message displayed when overwriting a saved ontology file

-------------------------------------------------------------
Key known issues in last stable release v2.2.1: 

* The ontology (class/property) JTrees are cached after classification of the ontology using the reasoner. But the reasoner results themselves are not cached. This makes switching between ontologies when Pellet is ON considerably slow.
* No form-based UI to create/edit nested Class Expressions. The workaround for now is to edit these in the RDF/XML inline tab.
* If the size of the logged changes in SWOOP becomes large, rendering them takes time because of a bug in the implementation. A workaround for now is either: Save the ontology/workspace as a SWOOP file (*.swo, *.swp) which saves all the changes. Then remove portions of the change log (see "Remove" link in logpane) and resume editing; OR Disable change logging (File->Preferences) and/or turn off change rendering altogether ("Show Changes" checkbox).
* No RDF/XML rendering of datatype enumerations supported (e.g. NATO ontology in the default bookmarks cannot be serialized)
* "Show References" method does not consider imported ontology information. 

For other, less critical issues see: http://www.mindswap.org/issues/

-------------------------------------------------------------