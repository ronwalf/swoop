/*
 * Created on Aug 24, 2005
 */
package org.mindswap.swoop.racer;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.mindswap.pellet.output.TableData;
import org.mindswap.pellet.utils.AlphaNumericComparator;
import org.mindswap.pellet.utils.FileUtils;
import org.mindswap.pellet.utils.Timer;
import org.mindswap.pellet.utils.Timers;

/**
 * @author Evren Sirin
 *
 */
public class JRacer {
   public RacerClient racer = new RacerClient("localhost", 8088);
   Timers timers = new Timers();
   Timer t = null;

   Set conceptSet;
   String[] concepts;

   public JRacer() throws Exception {
       racer.openConnection();
//       racer.send("(set-nrql-mode 1)");
//       racer.send("(enable-optimized-query-processing t)");
//       racer.send("(enable-nrql-warnings)");

       reset();
   }

   void runDLBenchmark() throws IOException, RacerException {
       String dirName = "/mindswap/pellet/test_data/dl-benchmark/tbox/";
               File dir = new File(dirName);
               String[] files = dir.list( new FilenameFilter() {
                       public boolean accept(File dir, String name) {
                               return dir != null && name.endsWith(".tkb")
                       && name.indexOf("-roles")==-1
                       && name.indexOf("-cd")==-1
                       && name.indexOf("pdwq")==-1
                       && name.indexOf("people")==-1
                       && name.indexOf("veda-all")==-1;
                       }
               });
               Arrays.sort(files, AlphaNumericComparator.CASE_INSENSITIVE);

               TableData table = new TableData(Arrays.asList(new String[] { "Name", " Time"} ));
               for(int i = 0; i < files.length; i++) {
                       System.out.print((i+1) + ") ");
                       List data = new ArrayList();
                       data.add( files[i] );
               read( dirName + files[i] );

               classify();

               data.add(timers.getTimer("classify").getTotal()+"");
               table.addRow(data);
               reset();
               }
               System.out.println( table );
   }

   void runLUBM(int limit) throws Exception {
       String[] data;

       racer.send("(full-reset)");
       racer.send("(set-nrql-mode 1)");
//        racer.send("(set-unique-name-assumption t)");
       racer.send("(enable-optimized-query-processing)");

       String dirName = "/mindswap/pellet/files/LUBM/";
               File dir = new File(dirName);
               String[] files = dir.list(new FilenameFilter() {
                       public boolean accept(File dir, String name) {
                               return (dir != null) && name.endsWith(".owl");
                       }
               });
               Arrays.sort(files, AlphaNumericComparator.CASE_INSENSITIVE);

       t = timers.startTimer( "reading" );
               for(int i = 0; i < limit; i++) {
                       System.out.print((i+1) + ") ");
                       String file = files[i];
                       int index = files[i].indexOf('.');
                       String name = files[i].substring(0, index);

               System.out.println( "Reading " + name );
//              racer.RACERreadFile( dirName + file );
               racer.readOWL( "file:" + dirName + file );
               }
       t.stop();
       System.out.println( "Reading: " + t.getTotal() + "");
//        printTree( "TOP", "" );

       TableData table = new TableData( Arrays.asList( new String[] { "No", "Answers", "Time" } ) );

               consistency();
               table.addRow( Arrays.asList( new String[] {"Consistency", "1", ""+t.getTotal()} ) );

               classify();

       files = dir.list(new FilenameFilter() {
                       public boolean accept(File dir, String name) {
                               return (dir != null) && name.startsWith("Query") && name.endsWith(".txt");
                       }
               });
               Arrays.sort(files, AlphaNumericComparator.CASE_INSENSITIVE);

               for(int i = 0; i < files.length; i++) {
//                  if( i != 0 ) continue;
                       System.out.print((i+1) + ") ");
                       String file = files[i];
                       int index = files[i].indexOf('.');
                       String name = files[i].substring(0, index);

               System.out.println( name );
               String query = FileUtils.readFile( dirName + file );
//              System.out.println( query );

               List row = new ArrayList();
               row.add("Q" + (i+1));

//              t = timers.startTimer( "Q" + (i+1) );
//              int count = racer.nRQL( query, false );
//              t.stop();

               query = query.substring( query.indexOf( "(", 1 ), query.length()-1 );
               int b = 0;
               int e = findMatchingP( query ) + 1;
               String vars = query.substring( b, e );
               String body = query.substring( e + 1 );
               t = timers.startTimer( "Q" + (i+1) );
               racer.send("(racer-prepare-query " + vars + "  " + body + " :id q" + i + ")");
               String c = racer.send("(get-answer-size q" + i + " t)");
               t.stop();
               int count = c.equalsIgnoreCase( "NIL" ) ? 0 : Integer.parseInt(c);

               System.out.println( "Answers "  + count);
               System.out.println( "Time "  + t.getTotal());
//              System.out.println( result );
               row.add( "" + count );
               row.add( "" +  t.getTotal() );
               table.addRow( row );
               }

               System.out.println( table );

//        String[] data = null;
//
//        String ns = null;
////        ns = "http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#";
//        ns = "http://www.lehigh.edu/~zhp2/univ-bench.daml#";
//
//        t = timers.startTimer( "q1" );
//        data = racer.conceptInstances( "|" + ns + "Student|" );
//        t.stop();
//
//        System.out.println( "Students " + data.length + " (" + t.getTotal() + ")"  );
//
//        t = timers.startTimer( "q0" );
//        data = racer.conceptInstances( "|" + ns + "Department|" );
//        t.stop();
//
//        System.out.println( "Departments " + data.length + " (" + t.getTotal() + ")"  );
//
//        t = timers.startTimer( "q2" );
//        data = racer.conceptInstances( "|" + ns + "Chair|" );
//        t.stop();
//
//        System.out.println( "Chair " + data.length + " (" + t.getTotal() + ")"  );
//
//        t = timers.startTimer( "q3" );
//        data = racer.conceptInstances( "|" + ns + "AssociateProfessor|" );
//        t.stop();
//
//        System.out.println( "AssociateProfessor " + data.length + " (" + t.getTotal() + ")"  );
   }

   void run() throws Exception {
       Timer t;

       String dir = "/mindswap/pellet/test_data/dl-benchmark/";
       String tbox = dir + "tbox/";
       String abox = dir + "abox/";
       String file;

       file = tbox + "galen.tkb";
       file = abox + "bike81.akb";

//        file = "http://www.cs.man.ac.uk/~horrocks/OWL/Ontologies/galen.owl";
//        file = "file://C/mindswap/pellet/files/galen.owl";
//        file = "http://protege.stanford.edu/plugins/owl/owl-library/not-galen.owl";
//        file = "file://C/mindswap/pellet/files/nasa-easier.owl";
//        file = "http://www.w3.org/2001/sw/WebOnt/guide-src/wine.owl";
//        file = "http://www.mindswap.org/ontologies/dice.owl";
//        file = "http://www.aktors.org/ontology/portal";
       file = "file:/tools/kaon2/ontologies/dolce/dolce.owl";
//        file = "file:/FEARMO.owl";
       file = "file:/mindswap/ontologies/dice.owl";

       System.out.print( "Reading " + file + "..." );
       t = timers.startTimer( "reading" );

       if( file.startsWith( "http:" ) || file.startsWith( "file:" ) )
           racer.readOWL( file );
       else
           racer.RACERreadFile( file );
       t.stop();
       System.out.println( "done. (" + t.getTotal() + ")");

       info();

//        consistency();

       classify();

       realize();

//        System.out.println(racer.individualInstanceP("I3", "C51"));

//       createTree();

       printTree( "TOP", "" );

//        printTBox();
//
//        printABox();

//        RacerVerify.verify( file, racer );

//        RacerVerify.verify( "C:\\mindswap\\pellet\\files\\galen-local.owl", racer );

       racer.closeConnection();
   }

   void read( String file ) throws IOException, RacerException {
       String name = file;
               int index = name.indexOf('.');
               if(index != -1)
                   name = name.substring(0, index);
               index = name.lastIndexOf( '/' );
               if(index != -1)
                   name = name.substring(index + 1);

       System.out.print( "Reading " + name + "..." );
       Timer t = timers.startTimer( "reading" );
       racer.RACERreadFile( file );
       t.stop();
       System.out.print( "done. (" + t.getTotal() + ") ");
   }

   void reset() throws RacerException, IOException {
       racer.deleteAllTBoxes();
       racer.deleteAllABoxes();
       timers.resetAll();
   }

   void info() throws IOException, RacerException {
       System.out.print("Classes: " + racer.allAtomicConcepts().length + " " );
       System.out.print("Properties: " + racer.allRoles().length + " " );
       System.out.print("Individuals: " + racer.allIndividuals().length );
       System.out.println();
   }

   void consistency() throws IOException, RacerException {
       System.out.print( "ABox Consistency..." );
       t = timers.startTimer( "consistency" );
       racer.aboxConsistentP();
       t.stop();
       System.out.println( "done. (" + t.getTotal() + ")");
   }

   void classify() throws IOException, RacerException {
       System.out.print( "Classifying..." );
       t = timers.startTimer( "classify" );
       racer.classifyTBox();
       t.stop();
       System.out.println( "done. (" + t.getTotal() + ") ");
   }

   void realize() throws IOException, RacerException {
       System.out.print( "Realizing..." );
       t = timers.startTimer( "realize" );
       racer.realizeABox();
       t.stop();
       System.out.println( "done. (" + t.getTotal() + ") " );
   }

   void query(String name, String qry) throws IOException, RacerException {
       System.out.print( "Realizing..." );
       t = timers.startTimer( name );
       racer.send( qry );
       t.stop();
       System.out.println( "done. (" + t.getTotal() + ") " );
   }

   void createTree() throws Exception {
       JTree tree = new JTree(new DefaultTreeModel(createNode("TOP")));
       JFrame frame = new JFrame();
       frame.getContentPane().add(new JScrollPane(tree));

       frame.setVisible(true);
   }

   DefaultMutableTreeNode createNode(String concept) throws Exception {
       DefaultMutableTreeNode node = new DefaultMutableTreeNode();
       String label = concept;
       String[] eqs = racer.conceptSynonyms( concept );
       for(int i = 0; i < eqs.length; i++) {
           if( !eqs[i].equals( concept ) )
               label += " = " + eqs[i];
       }
       node.setUserObject( label );

       String[] subs = racer.conceptChildren( concept );
       for(int i = 0; i < subs.length; i++) {
           if( subs[i].equals( "BOTTOM" ) ) continue;

           node.add( createNode( subs[i] ) );
       }

       return node;
   }

   void printTree(String concept, String indent) throws Exception {
       if( concept.equals( "BOTTOM" ) ) return;

       System.out.print( indent + concept );
       String[] eqs = racer.conceptSynonyms( concept );
       for(int i = 0; i < eqs.length; i++) {
           if( !eqs[i].equals( concept ) )
               System.out.print( " = " + eqs[i] );
       }
       if( racer.aboxRealizedP() ) {
           String[] instances = racer.conceptInstances( concept );
           if( instances.length > 0 ) System.out.print( " (" );
           for(int i = 0; i < instances.length; i++) {
               if( i > 0 ) System.out.print( ", " );
               System.out.print( instances[i] );
           }
           if( instances.length > 0 ) System.out.print( ")" );
       }
       System.out.println();

       indent += "   ";
       String[] subs = racer.conceptChildren( concept );
       for(int i = 0; i < subs.length; i++) {
           printTree( subs[i], indent );
       }
   }

   void print(String[] data, String header) throws Exception {
       System.out.println( header + " (" + data.length + ")");
       for(int i = 0; i < data.length; i++) {
           System.out.println( data[i] );
       }
   }
   void printTBox() throws Exception {
       concepts = racer.allAtomicConcepts();
       conceptSet = new HashSet( Arrays.asList(concepts));
       for(int i = 0; i < concepts.length; i++) {
           String concept = concepts[i];
           printTBox(concept);
       }
   }
   void printTBox(String concept) throws Exception {
       if( concept.equals( "BOTTOM" ) || conceptSet.contains( concept ) ) return;

       System.out.print("(");

       printConcept(concept);

       System.out.print(" ");

       String[] supers = racer.conceptParents( concept );
       if(supers.length == 0) {
           System.out.print("NIL");
       }
       else {
           System.out.print("(");
           for(int i = 0; i < supers.length; i++) {
               if( i > 0 ) System.out.print( " " );
               printConcept( supers[i] );
           }
           System.out.print(")");
       }

       System.out.print(" ");

       String[] subs = racer.conceptChildren( concept );
       if(subs.length == 0) {
           System.out.print("NIL");
       }
       else {
           System.out.print("(");
               for(int i = 0; i < subs.length; i++) {
                   if( i > 0 ) System.out.print( " " );
                   printConcept( subs[i] );
               }
               System.out.print(")");
       }

       System.out.println(")");
   }

   void printABox() throws Exception {
       for(int i = 0; i < concepts.length; i++) {
           String concept = concepts[i];
           printTBox(concept);
       }
   }
   void printABox(String concept) throws Exception {
       if( concept.equals( "BOTTOM" ) ) return;

       System.out.print("(");

       printConcept(concept);

       System.out.print(" ");

       String[] instances = racer.conceptInstances( concept );
       if(instances.length == 0) {
           System.out.print("NIL");
       }
       else {
           System.out.print("(");
           for(int i = 0; i < instances.length; i++) {
               if( i > 0 ) System.out.print( " " );
               System.out.print( instances[i] );
           }
           System.out.print(")");
       }

       System.out.println(")");
   }

   void printConcept(String concept) throws Exception {
       String[] eqs = filter( racer.conceptSynonyms( concept ) );
       if(eqs.length>1) System.out.print("(");
       for(int i = 0; i < eqs.length; i++) {
           if( i > 0 ) System.out.print( " " );
           System.out.print( eqs[i] );
       }
       if(eqs.length>1) System.out.print(")");
   }

   String[] filter(String[] array) {
       List list = new ArrayList(Arrays.asList(array));
       list.remove("*TOP*");
       list.remove("*BOTTOM*");
       return (String[]) list.toArray(new String[list.size()]);
   }

   public int findMatchingP( String qry ) throws Exception {
       int openBracket = 0;
       for(int i = 0; i < qry.length(); i++) {
               char c = qry.charAt(i);
               if( c == '(' )
                   openBracket++;
               else if( c == ')' ) {
                   openBracket--;
                   if( openBracket == 0 )
                       return i;
               }

       }

       return -1;
   }

   void close() throws IOException {
       racer.closeConnection();
   }

   public static void main(String[] args) throws Exception{
       JRacer test = new JRacer();
       try {
       test.run();
//        test.runDLBenchmark();
//        test.runLUBM(Integer.parseInt(args[0]));
       }
       catch( Exception e ) {
           // TODO Auto-generated catch block
           e.printStackTrace();
       }

       test.close();
   }

}

