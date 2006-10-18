package org.mindswap.swoop.utils.owlapi;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.StreamTokenizer;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.xerces.impl.dv.XSSimpleType;
import org.mindswap.pellet.datatypes.Datatype;
import org.mindswap.pellet.datatypes.XSDDecimal;
import org.mindswap.pellet.exceptions.UnsupportedFeatureException;
import org.mindswap.pellet.utils.ATermUtils;
import org.mindswap.pellet.utils.AlphaNumericComparator;
import org.mindswap.swoop.utils.owlapi.CorrectedRDFRenderer;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDisjointClassesAxiom;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLProperty;
import org.semanticweb.owl.model.OWLSubClassAxiom;
import org.semanticweb.owl.model.change.AddClassAxiom;
import org.semanticweb.owl.model.change.AddDomain;
import org.semanticweb.owl.model.change.AddEntity;
import org.semanticweb.owl.model.change.AddEquivalentClass;
import org.semanticweb.owl.model.change.AddInverse;
import org.semanticweb.owl.model.change.AddObjectPropertyRange;
import org.semanticweb.owl.model.change.AddSuperClass;
import org.semanticweb.owl.model.change.AddSuperProperty;
import org.semanticweb.owl.model.change.ChangeVisitor;
import org.semanticweb.owl.model.change.OntologyChange;
import org.semanticweb.owl.model.change.SetFunctional;
import org.semanticweb.owl.model.change.SetInverseFunctional;
import org.semanticweb.owl.model.change.SetSymmetric;
import org.semanticweb.owl.model.change.SetTransitive;
import org.semanticweb.owl.model.helper.OWLBuilder;

import aterm.ATermAppl;

/**
 * Parse and test the cases from DL benchmark suite. This class provides parsing
 * for KRSS files.
 *
 * @author Evren Sirin
 */
public class KRSSConverter {
       public static boolean DEBUG = false;

   public static String base = "http://www.example.org/test#";

       public static boolean FORCE_UPPERCASE = false;

       public final int CLASS= 1;
       public final int DATA_PROP = 2;
       public final int OBJECT_PROP = 3;
       public final int INDIVIDUAL = 4;
       public final int FUNC= 5;
       public final int INV_FUNC = 6;
       public final int SYMM = 7;
       public final int TRANS = 8;
       public final int SUB_CLASS = 9;
       public final int SUB_DPROP = 10;
       public final int SUB_OPROP = 11;
       public final int DATA_DOMAIN= 12;
       public final int OBJ_DOMAIN= 13;
       public final int DATA_RANGE= 14;
       public final int OBJ_RANGE= 15;
       public final int INV_PROP= 16;
       public final int SAME_CLASS = 17;
       public final int DISJ_CLASS = 18;

       private ArrayList terms;

       public void assertTrue(boolean b) {
               if(!b) throw new RuntimeException("assert error");
       }

       public void parseToken(StreamTokenizer in, int token) throws Exception {
               assertTrue(token == in.nextToken());
       }

       public void parseToken(StreamTokenizer in, String token) throws Exception {
           in.nextToken();
               assertTrue(token.equals(in.sval));
       }

       public void skipToken(StreamTokenizer in) throws Exception {
               in.nextToken();
       }

       public boolean peekToken(StreamTokenizer in, int token) throws Exception {
               int next = in.nextToken();
               in.pushBack();
               return (token == next);
       }

       public String getToken(StreamTokenizer in) throws Exception {
               in.nextToken();

               return in.sval;
       }

       public int getInt(StreamTokenizer in) throws Exception {
               in.nextToken();

               return (int) in.nval;
       }

       public double getNumber(StreamTokenizer in) throws Exception {
               in.nextToken();

               return in.nval;
       }

       public ATermAppl getTerm(StreamTokenizer in) throws Exception {
           String token = getToken(in);
           if(FORCE_UPPERCASE)
               token = token.toUpperCase();
               return ATermUtils.makeTermAppl( token );
       }

       public ATermAppl term(String str) throws Exception {
               return ATermUtils.makeTermAppl(str);
       }

       public StreamTokenizer initTokenizer(String file) throws Exception {
               StreamTokenizer in = new StreamTokenizer(new FileReader(file));
               in.lowerCaseMode(false);
               in.commentChar(';');
               in.wordChars('/', '/');
               in.wordChars('_', '_');
               in.wordChars('*', '*');
               in.wordChars('?', '?');
               in.wordChars('%', '%');
               in.wordChars('>', '>');
               in.wordChars('<', '<');
               in.wordChars('=', '=');
               in.quoteChar('|');

               return in;
       }

//      public KnowledgeBase initKB(long timeout) {
//          KnowledgeBase kb = new KnowledgeBase();
//
//              kb = new KnowledgeBase();
//              kb.setTimeout(timeout * 1000);
//
//              return kb;
//      }

       OWLProperty getProperty(StreamTokenizer in, OWLOntology ontology, int type) {

               OWLProperty prop = null;
               try {
                       if (peekToken(in, '(')) {
                               skipToken(in);
                               parseToken(in, "INV");
                               ATermAppl r = getTerm(in);
                               prop = ontology.getObjectProperty(new URI(r.getName()));
                               if (((OWLObjectProperty) prop).getInverses(ontology).size()>0) {
                                       return (OWLObjectProperty) ((OWLObjectProperty) prop).getInverses(ontology).iterator().next();
                               }
                               else {
                                       // create new property q to be inverse of prop and return q
                                       OWLObjectProperty q = (OWLObjectProperty) this.addEntity(ontology, new URI(r.getName()+"_INVERSE"), OBJECT_PROP);
                                       List args = new ArrayList();
                                       args.add(prop);
                                       args.add(q);
                                       this.addAxiom(ontology, INV_PROP, args);
                                       return q;
                               }
                       }
                       else {
                               // simply return property
                               ATermAppl r = getTerm(in);
                               if (type == OBJECT_PROP)
                                       prop = ontology.getObjectProperty(new URI(r.getName()));
                               else
                                       prop = ontology.getDataProperty(new URI(r.getName()));

                               if( prop == null ) {
                                       if (type == OBJECT_PROP)
                                               prop = (OWLObjectProperty) this.addEntity(ontology, new URI(r.getName()), OBJECT_PROP);
                                       else
                                               prop = (OWLObjectProperty) this.addEntity(ontology, new URI(r.getName()), DATA_PROP);
                               }
                       }
               }
               catch (Exception ex) {
                       ex.printStackTrace();
               }
               return prop;
       }

       List parseExprList(StreamTokenizer in, OWLOntology ontology) throws Exception {
           int count = 0;
           while(peekToken(in, '(')) {
               skipToken(in);
               count++;
           }

           List terms = new ArrayList();
           while(true) {
               if(peekToken(in, ')')) {
                   if(count == 0)
                       break;
                   skipToken(in);
                   count--;
                   if(count == 0)
                       break;
               }
               else if(peekToken(in, '(')) {
               skipToken(in);
               count++;
               }
               else
                   terms.add(parseExpr(in, ontology));
           }

//          for(int i = 0; i < count; i++)
//              parseToken(in, ')');

           return (terms);
       }

       OWLDescription parseExpr(StreamTokenizer in, OWLOntology ontology) throws Exception {
               OWLDescription a = null;

               int token = in.nextToken();
               String s = in.sval;
               if(token == '(') {
                       token = in.nextToken();
                       assertTrue(token == StreamTokenizer.TT_WORD);

                       s = in.sval;
                       if(s.equalsIgnoreCase("NOT")) {
                               OWLDescription c = parseExpr(in, ontology);
                               a = ontology.getOWLDataFactory().getOWLNot(c);
                               if(c instanceof OWLClass) this.addEntity(ontology, ((OWLClass) c).getURI(), CLASS);
                       }
                       else if(s.equalsIgnoreCase("AND")) {
                               Set list = new HashSet();

                               while(!peekToken(in, ')')) {
                                       OWLDescription c = parseExpr(in, ontology);

                                       if(c instanceof OWLClass) this.addEntity(ontology, ((OWLClass) c).getURI(), CLASS);
                                       list.add(c);
                               }
                               a = ontology.getOWLDataFactory().getOWLAnd(list);
                       }
                       else if(s.equalsIgnoreCase("OR")) {
                               Set list = new HashSet();

                               while(!peekToken(in, ')')) {
                                       OWLDescription c = parseExpr(in, ontology);

                                       if(c instanceof OWLClass) this.addEntity(ontology, ((OWLClass) c).getURI(), CLASS);
                                       list.add(c);
                               }
                               a = ontology.getOWLDataFactory().getOWLOr(list);
                       }
                       else if(s.equalsIgnoreCase("ALL")) {
                               OWLObjectProperty prop = (OWLObjectProperty) getProperty(in, ontology, OBJECT_PROP);
//                              kb.addObjectProperty(r);

                               ATermUtils.assertTrue( prop != null );

                               OWLDescription c = parseExpr(in, ontology);
                               if(c instanceof OWLClass) this.addEntity(ontology, ((OWLClass) c).getURI(), CLASS);

                               a = ontology.getOWLDataFactory().getOWLObjectAllRestriction(prop, c);
                       }
                       else if(s.equalsIgnoreCase("SOME")) {
                               OWLObjectProperty prop = (OWLObjectProperty) getProperty(in, ontology, OBJECT_PROP);

                               ATermUtils.assertTrue( prop != null );

                               OWLDescription c = parseExpr(in, ontology);
                               if(c instanceof OWLClass) this.addEntity(ontology, ((OWLClass) c).getURI(), CLASS);

                               a = ontology.getOWLDataFactory().getOWLObjectSomeRestriction(prop, c);
                       }
                       else if(s.equalsIgnoreCase("AT-LEAST")) {
                               int n = getInt(in);
                               OWLObjectProperty prop = (OWLObjectProperty) getProperty(in, ontology, OBJECT_PROP);
                           a = ontology.getOWLDataFactory().getOWLObjectCardinalityAtLeastRestriction(prop, n);
                       }
                       else if(s.equalsIgnoreCase("AT-MOST")) {
                               int n = getInt(in);
                               OWLObjectProperty prop = (OWLObjectProperty) getProperty(in, ontology, OBJECT_PROP);
                               a = ontology.getOWLDataFactory().getOWLObjectCardinalityAtMostRestriction(prop, n);
                       }
                       else if(s.equalsIgnoreCase("A")) {
                               OWLObjectProperty prop = (OWLObjectProperty) getProperty(in, ontology, OBJECT_PROP);
                           // TODO what does term 'A' stand for
//                              kb.addProperty(r);
//                              kb.addFunctionalProperty(r);
                               this.setPropAttribute(ontology, prop.getURI(), OBJECT_PROP, FUNC, true);
//                              a = ATermUtils.makeMin(r, 1);
                               a = ontology.getOWLDataFactory().getOWLObjectCardinalityAtLeastRestriction(prop, 1);
                       }
                       else if(s.equalsIgnoreCase("MIN") || s.equals(">=")) {
                               ATermAppl r = getTerm(in);
//                              kb.addDatatypeProperty(r);
                               this.addEntity(ontology, new URI(r.getName()), DATA_PROP);

                               String val = getNumber(in)+"";
//                              DatatypeReasoner dtReasoner = kb.getDatatypeReasoner();
                               Datatype dt = XSDDecimal.instance.deriveByRestriction( XSSimpleType.FACET_MININCLUSIVE, val );
//                              String dtName = dtReasoner.defineDatatype(dt);
//                              ATermAppl datatype = ATermUtils.makeTermAppl(dtName);
                               //TODO
//                              a = ATermUtils.makeAllValues(r, datatype);
                       }
                       else if(s.equalsIgnoreCase("MAX")
                            || s.equals("<=")) {
                               ATermAppl r = getTerm(in);
//                              kb.addDatatypeProperty(r);
               this.addEntity(ontology, new URI(r.getName()), DATA_PROP);

                               String val = getNumber(in)+"";
//                              DatatypeReasoner dtReasoner = kb.getDatatypeReasoner();
                               Datatype dt = XSDDecimal.instance.deriveByRestriction( XSSimpleType.FACET_MAXEXCLUSIVE, val );
//                              String dtName = dtReasoner.defineDatatype(dt);
//                              ATermAppl datatype = ATermUtils.makeTermAppl(dtName);
                               //TODO
//                              a = ATermUtils.makeAllValues(r, datatype);
                       }
                       else if(s.equals("=")) {
                               ATermAppl r = getTerm(in);
//                kb.addDatatypeProperty(r);
               this.addEntity(ontology, new URI(r.getName()), DATA_PROP);

                               String val = getNumber(in)+"";
//                              DatatypeReasoner dtReasoner = kb.getDatatypeReasoner();
                               XSDDecimal dt = (XSDDecimal) XSDDecimal.instance.deriveByRestriction( XSSimpleType.FACET_MININCLUSIVE, val );
                               dt = (XSDDecimal) dt.deriveByRestriction( XSSimpleType.FACET_MAXEXCLUSIVE, val );
//                              String dtName = dtReasoner.defineDatatype(dt);
//                              ATermAppl datatype = ATermUtils.makeTermAppl(dtName);
                               //TODO
//                              a = ATermUtils.makeAllValues(r, datatype);
                       }
                       else if(s.equalsIgnoreCase("EXACTLY")) {
                               int n = getInt(in);
                               OWLObjectProperty prop = (OWLObjectProperty) getProperty(in, ontology, OBJECT_PROP);
                               a = ontology.getOWLDataFactory().getOWLObjectCardinalityRestriction(prop, n, n);
                       }
                       else if(s.equalsIgnoreCase("INV")) {
                               // not needed as it is handled in getProperty
                       }
                       else {
                               throw new RuntimeException("Unknown expression " + s);
                       }

                       if(in.nextToken() != ')') {
                           if(s.equalsIgnoreCase("AT-LEAST") || s.equalsIgnoreCase("AT-MOST"))
                               throw new UnsupportedFeatureException("Qualified cardinality restrictions");
                           else
                               throw new RuntimeException("Parse exception at term " + s);
                       }
               }
               else if(token == '#') {
                   int n = getInt(in);
                   if( peekToken(in, '#') ) {
                       skipToken(in);
                       //TODO
//                      a = (ATermAppl) terms.get( n );
                       if(a == null)
                           throw new RuntimeException("Parse exception: #" + n + "# is not defined");
                   }
                   else {
                       parseToken(in, "=");
                       a = parseExpr(in, ontology);

                       while(terms.size() <= n)
                           terms.add(null);

//                      ATermAppl previous = (ATermAppl)
                       terms.set(n, a);
//                      if( previous != null)
//                          System.err.println(
//                              "WARNING: Redfining #" + n + "# as " + a + ", previous was:" + previous);
                   }
               }
               else if(token == StreamTokenizer.TT_EOF)
                       a = null;
               else if(s.equalsIgnoreCase("TOP") || s.equalsIgnoreCase("*TOP*"))
                       a = ontology.getOWLDataFactory().getOWLThing();
               else if(s.equalsIgnoreCase("BOTTOM") || s.equalsIgnoreCase("*BOTTOM*"))
                       a = ontology.getOWLDataFactory().getOWLNothing();
               else {
                   if(FORCE_UPPERCASE)
                       s = s.toUpperCase();
                       // make a class?
//                  a = ATermUtils.makeTermAppl(s);
                   a = (OWLClass) this.addEntity(ontology, new URI(s), CLASS);
               }

               return a;
       }

       public OWLOntology readTBox( String file ) throws Exception {
       StreamTokenizer in = initTokenizer( file );
               URI uri = new URI( base );
               // create new OWL ontology object
               OWLBuilder builder = new OWLBuilder();
               builder.createOntology(uri, uri);
               OWLOntology ontology = builder.getOntology();

           Map disjoints = new HashMap();

               int token = in.nextToken();
               while(token != StreamTokenizer.TT_EOF && token != ')') {
                   if(token == '#') {
                       in.ordinaryChar('|');
                       token = in.nextToken();
                       while(token != '#')
                           token = in.nextToken();
                       in.quoteChar('|');
                       token = in.nextToken();
                       if(token == StreamTokenizer.TT_EOF)
                           break;
                   }
                       assertTrue(token == '(');

                       String str = getToken(in);
                       //functional propertirs
                       
                       
                               
                       
                       if(str.equalsIgnoreCase("DEFINE-PRIMITIVE-ROLE")
                       || str.equalsIgnoreCase("DEFINE-PRIMITIVE-ATTRIBUTE")
                       || str.equalsIgnoreCase("DEFPRIMROLE")
                       || str.equalsIgnoreCase("DEFPRIMATTRIBUTE")) {
                               ATermAppl r = getTerm(in);
//                              kb.addObjectProperty(r);
                               this.addEntity(ontology, new URI(r.getName()), OBJECT_PROP);

                               if(str.equalsIgnoreCase("DEFINE-PRIMITIVE-ATTRIBUTE")
                               || str.equalsIgnoreCase("DEFPRIMATTRIBUTE")) {
//                                      kb.addFunctionalProperty(r);
                                       this.setPropAttribute(ontology, new URI(r.getName()), OBJECT_PROP, FUNC, true);
                                       if(DEBUG) System.out.println("DEFINE-PRIMITIVE-ATTRIBUTE " + r);
                               }
                               else if(DEBUG)
                                       System.out.println("DEFINE-PRIMITIVE-ROLE " + r);

                               while(!peekToken(in, ')')) {
                                       if(peekToken(in, ':')) {
                                               parseToken(in, ':');
                                               String cmd = getToken(in);
                                               if(cmd.equalsIgnoreCase("parents")) {
                                                       boolean paren = peekToken(in, '(');
                                                       if(paren) {
                                                               parseToken(in, '(');
                                                               while(!peekToken(in, ')')) {
                                                                       ATermAppl s = getTerm(in);
                                                                       if(!s.getName().equals("NIL")) {
//                                                                              kb.addObjectProperty(s);
                                                                               this.addEntity(ontology, new URI(s.getName()), OBJECT_PROP);
//                                                                              kb.addSubProperty(r, s);
                                                                               List args = new ArrayList();
                                                                               args.add(r.getName());
                                                                               args.add(s.getName());
                                                                               this.addAxiom(ontology, SUB_OPROP, args);
                                                                               if(DEBUG) System.out.println("PARENT-ROLE " + r + " " + s);
                                                                       }
                                                               }
                                                               parseToken(in, ')');
                                                       }
                                                       else {
                                                               ATermAppl s = getTerm(in);
                                                               if(!s.toString().equalsIgnoreCase("NIL")) {
//                                                                      kb.addObjectProperty(s);
                                                                       this.addEntity(ontology, new URI(s.getName()), OBJECT_PROP);
//                                                                      kb.addSubProperty(r, s);
                                                                       List args = new ArrayList();
                                                                       args.add(r.getName());
                                                                       args.add(s.getName());
                                                                       this.addAxiom(ontology, SUB_OPROP, args);
                                                                       if(DEBUG) System.out.println("PARENT-ROLE " + r + " " + s);
                                                               }
                                                       }
                                               }
                                               else if(cmd.equalsIgnoreCase("transitive")) {
                                                       assertTrue(getToken(in).equalsIgnoreCase("T"));
//                                                      kb.addTransitiveProperty(r);
                                                       this.setPropAttribute(ontology, new URI(r.getName()), OBJECT_PROP, TRANS, true);
                                                       if(DEBUG) System.out.println("TRANSITIVE-ROLE " + r);
                                               }
                                               else if(cmd.equalsIgnoreCase("range")) {
                                                   OWLDescription range = parseExpr(in, ontology);
//                                                  kb.addClass(range);
//                                                  kb.addRange(r, range);
                                                   List args = new ArrayList();
                                                   args.add(r.getName());
                                                   args.add(range);
                                                   this.addAxiom(ontology, OBJ_RANGE, args);
                                                       if(DEBUG) System.out.println("RANGE " + r + " " + range );
                                               }
                                               else if(cmd.equalsIgnoreCase("domain")) {
                                                   OWLDescription domain = parseExpr(in, ontology);
//                                                  kb.addClass(domain);
//                                                      kb.addDomain(r, domain);
                                                   List args = new ArrayList();
                                                   args.add(r.getName());
                                                   args.add(domain);
                                                   this.addAxiom(ontology, OBJ_DOMAIN, args);
                                                       if(DEBUG) System.out.println("DOMAIN " + r + " " + domain );
                                               }
                                               else if(cmd.equalsIgnoreCase("inverse")) {
                                                   ATermAppl inv = getTerm(in);
//                                                      kb.addInverseProperty(r, inv);
                                                   this.addEntity(ontology, new URI(inv.getName()), OBJECT_PROP);
                                                       List args = new ArrayList();
                                                       args.add(r.getName());
                                                       args.add(inv.getName());
                                                       this.addAxiom(ontology, INV_PROP, args);
                                                       if(DEBUG) System.out.println("INVERSE " + r + " " + inv );
                                               }
                                               else
                                                       throw new RuntimeException("Invalid role spec "  + cmd);
                                       }
                                       else if(peekToken(in, '(')) {
                                               parseToken(in, '(');
                                               String cmd = getToken(in);
                                               if(cmd.equalsIgnoreCase("domain-range")) {
                                                       ATermAppl domain = getTerm(in);
                                                       ATermAppl range = getTerm(in);

                                                       // add atomic domain and range
                                                       List args = new ArrayList();
                                                       args.add(r);
                                                       args.add(ontology.getClass(new URI(domain.getName())));
//                                                      kb.addDomain(r, domain);
                                                       this.addAxiom(ontology, OBJ_DOMAIN, args);
                                                       args.remove(1);
                                                       args.add(ontology.getClass(new URI(range.getName())));
//                                                      kb.addRange(r, range);
                                                       this.addAxiom(ontology, OBJ_RANGE, args);

                                                       if(DEBUG) System.out.println("DOMAIN-RANGE " + r + " " + domain + " " + range);
                                               }
                                               else
                                                       throw new RuntimeException("Invalid role spec");
                                               parseToken(in, ')');
                                       }
                               }
                       }
                       else if(str.equalsIgnoreCase("DEFINE-PRIMITIVE-CONCEPT")
                            || str.equalsIgnoreCase("DEFPRIMCONCEPT")) {
                               ATermAppl c = getTerm(in);
//                              kb.addClass(c);
                               OWLClass cls = (OWLClass) this.addEntity(ontology, new URI(c.getName()), CLASS);

                               if(!peekToken(in, ')')) {
                                       OWLDescription desc = parseExpr(in, ontology);

                                       if(desc!=null) {
//                                              kb.addClass(expr);
//                                              kb.addSubClass(c, expr);
                                               List args = new ArrayList();
                                               args.add(cls);
                                               args.add(desc);
                                               this.addAxiom(ontology, SUB_CLASS, args);
                                       }
                                       if(DEBUG) System.out.println("DEFINE-PRIMITIVE-CONCEPT " + c + " " + desc);
                               }
                       }
                       else if(str.equalsIgnoreCase("DEFINE-DISJOINT-PRIMITIVE-CONCEPT")) {
                               ATermAppl c = getTerm(in);
//                              kb.addClass(c);
                               OWLClass cls = (OWLClass) this.addEntity(ontology, new URI(c.getName()), CLASS);

                               parseToken(in, '(');
                               while(!peekToken(in, ')')) {
                   OWLDescription desc = parseExpr(in, ontology);

                                       List prevDefinitions = (List) disjoints.get( desc );
                                       if( prevDefinitions == null )
                                           prevDefinitions = new ArrayList();
                                       for(Iterator i = prevDefinitions.iterator(); i.hasNext();) {
                                           OWLClass other = (OWLClass) i.next();
                                               addAxiom( ontology, DISJ_CLASS, Arrays.asList(new OWLClass[] {cls,other}) );
                                               if(DEBUG) System.out.println("DEFINE-PRIMITIVE-DISJOINT " + c + " " + other);
                                       }
                                       prevDefinitions.add( cls );
                               }
                               parseToken(in, ')');

                               OWLDescription desc = parseExpr(in, ontology);
//                              kb.addSubClass(c, expr);
                               List args = new ArrayList();
                               args.add(cls);
                               args.add(desc);
                               this.addAxiom(ontology, SUB_CLASS, args);

//                              if(DEBUG) System.out.println("DEFINE-DISJOINT-PRIMITIVE-CONCEPT " + c + " " + expr);
                       }
                       else if(str.equalsIgnoreCase("DEFINE-CONCEPT")
                            || str.equalsIgnoreCase("DEFCONCEPT")) {
                               ATermAppl c = getTerm(in);
//                              kb.addClass(c);
                               this.addEntity(ontology, new URI(c.getName()), CLASS);

                               OWLDescription desc = parseExpr(in, ontology);
//                              kb.addSameClass(c, expr);
                               List args = new ArrayList();
                               args.add(ontology.getClass(new URI(c.getName())));
                               args.add(desc);
                               this.addAxiom(ontology, SAME_CLASS, args);

//                              if(DEBUG) System.out.println("DEFINE-CONCEPT " + c + " " + expr);
                       }
                       else if(str.equalsIgnoreCase("IMPLIES") || str.equalsIgnoreCase("implies_c")) {
                               OWLDescription c1 = parseExpr(in, ontology);
                               OWLDescription c2 = parseExpr(in, ontology);
//                              kb.addClass(c1);
//                              kb.addClass(c2);
//                              kb.addSubClass(c1, c2);
                               List args = new ArrayList();
                               args.add(c1);
                               args.add(c2);
                               this.addAxiom(ontology, SUB_CLASS, args);


                               if(DEBUG) System.out.println("IMPLIES " + c1 + " " + c2);
                       }
                       else if(str.equalsIgnoreCase("equal_c")) {
                           OWLDescription c1 = parseExpr(in, ontology);
                           OWLDescription c2 = parseExpr(in, ontology);
//                          kb.addClass(c1);
//                          kb.addClass(c2);
//                          kb.addSubClass(c1, c2);
                           List args = new ArrayList();
                           args.add(c1);
                           args.add(c2);
                           this.addAxiom(ontology, SAME_CLASS, args);


                           if(DEBUG) System.out.println("SAME Class " + c1 + " " + c2);
                   }
                       else if(str.equalsIgnoreCase("implies_r")) {
                    	   List args = new ArrayList();
                           ATermAppl s1 = getTerm(in);
                           this.addEntity(ontology, new URI(s1.getName()), OBJECT_PROP);
                           args.add(s1.getName());
                           ATermAppl s2 = getTerm(in);
                           this.addEntity(ontology, new URI(s2.getName()), OBJECT_PROP);
                           args.add(s2.getName());
                           this.addAxiom(ontology, SUB_OPROP, args);

                          }
                       else if(str.equalsIgnoreCase("functional")){
                    	   ATermAppl r = getTerm(in);
                    	   this.addEntity(ontology, new URI(r.getName()), OBJECT_PROP);
                    	   this.setPropAttribute(ontology, new URI(r.getName()), OBJECT_PROP, FUNC, true);
                       }
                       
                       else if(str.equalsIgnoreCase("transitive")){
                    	   ATermAppl r = getTerm(in);
                    	   this.addEntity(ontology, new URI(r.getName()), OBJECT_PROP);
                    	   this.setPropAttribute(ontology, new URI(r.getName()), OBJECT_PROP, TRANS, true);
                       }
                       
                       else if(str.equalsIgnoreCase("DISJOINT")) {
                           List args = parseExprList(in, ontology);
                           this.addAxiom(ontology, DISJ_CLASS, args);

//                          for(int i = 0; i < list.size() - 1; i++) {
//                              OWLDescription c1 = (OWLDescription) list.get(i);
//                              for(int j = i + 1; j < list.size(); j++) {
//                                      OWLDescription c2 = (OWLDescription) list.get(j);
////                                kb.addClass(c2);
////                                kb.addDisjointClass(c1, c2);
//                                      this.addAxiom(ontology, DISJ_CLASS, args);
//                                  if(DEBUG) System.out.println("DISJOINT " + c1 + " " + c2);
//                              }
//                          }
                       }
                       else
                               throw new RuntimeException("Unknown command " + str);
                       parseToken(in, ')');

                       token = in.nextToken();
               }
               return ontology;
       }

       private void setPropAttribute(OWLOntology ontology, URI uri, int type, int attrib, boolean value) {
               try {
                       OWLProperty prop = null;
                       if (type == DATA_PROP) prop = ontology.getDataProperty(uri);
                       else prop = ontology.getObjectProperty(uri);

                       OntologyChange change = null;
                       switch (attrib) {
                               case FUNC:
                                       change = new SetFunctional(ontology, prop, value, null);
                                       break;
                               case INV_FUNC:
                                       change = new SetInverseFunctional(ontology, (OWLObjectProperty) prop, value, null);
                                       break;
                               case SYMM:
                                       change = new SetSymmetric(ontology, (OWLObjectProperty) prop, value, null);
                                       break;
                               case TRANS:
                                       change = new SetTransitive(ontology, (OWLObjectProperty) prop, value, null);
                                       break;
                       }
                       change.accept((ChangeVisitor) ontology);
               }
               catch (Exception ex) {
                       ex.printStackTrace();
               }
       }

       private void addAxiom(OWLOntology ontology, int type, List args) {
           OntologyChange oc = null;
               try {
                       switch (type) {
                               case SAME_CLASS:
                                   OWLDescription desc1 = (OWLDescription) args.get(0);
                                   OWLDescription desc2 = (OWLDescription) args.get(1);
                                   if( desc1 instanceof OWLClass ) {
                                       oc = new AddEquivalentClass( ontology, (OWLClass) desc1, desc2, null );
                                   }
                                   else {
                                               OWLEquivalentClassesAxiom equ = ontology.getOWLDataFactory().getOWLEquivalentClassesAxiom(new HashSet(args));
                                               oc = new AddClassAxiom(ontology, equ, null);
                                   }
                                       oc.accept((ChangeVisitor) ontology);
                                       break;

                               case DISJ_CLASS:
                                       OWLDisjointClassesAxiom disj = ontology.getOWLDataFactory().getOWLDisjointClassesAxiom(new HashSet(args));
                                       oc = new AddClassAxiom(ontology, disj, null);
                                       oc.accept((ChangeVisitor) ontology);
                                       break;

                               case SUB_CLASS:
                                       OWLDescription sub = (OWLDescription) args.get(0);
                                       OWLDescription sup = (OWLDescription) args.get(1);
                                       if( sub instanceof OWLClass ) {
                                           oc = new AddSuperClass( ontology, (OWLClass) sub, sup, null );
                                       }
                                       else {
                                           OWLSubClassAxiom ax = ontology.getOWLDataFactory().getOWLSubClassAxiom(sub, sup);
                                               oc = new AddClassAxiom(ontology, ax, null);
                                       }
                                       oc.accept((ChangeVisitor) ontology);
                                       break;

                               case SUB_OPROP:
                                       OWLObjectProperty p = ontology.getObjectProperty(new URI(args.get(0).toString()));
                                       OWLObjectProperty q = ontology.getObjectProperty(new URI(args.get(1).toString()));
                                       AddSuperProperty as = new AddSuperProperty(ontology, p, q, null);
                                       as.accept((ChangeVisitor) ontology);
                                       break;

                               case OBJ_RANGE:
                                       p = ontology.getObjectProperty(new URI(args.get(0).toString()));
                                       OWLDescription desc = (OWLDescription) args.get(1);
                                       AddObjectPropertyRange aopr = new AddObjectPropertyRange(ontology, p, desc, null);
                                       aopr.accept((ChangeVisitor) ontology);
                                       break;

                               case OBJ_DOMAIN:
                                       p = ontology.getObjectProperty(new URI(args.get(0).toString()));
                                       desc = (OWLDescription) args.get(1);
                                       AddDomain ad = new AddDomain(ontology, p, desc, null);
                                       ad.accept((ChangeVisitor) ontology);
                                       break;

                               case INV_PROP:
                                       p = ontology.getObjectProperty(new URI(args.get(0).toString()));
                                       q = ontology.getObjectProperty(new URI(args.get(1).toString()));
                                       AddInverse ai = new AddInverse(ontology, p, q, null);
                                       ai.accept((ChangeVisitor) ontology);
                                       break;
                       }
               }
               catch (Exception ex) {
                       ex.printStackTrace();
                       try {
               oc.accept((ChangeVisitor) ontology);
           }
           catch( OWLException e ) {
               // TODO Auto-generated catch block
               e.printStackTrace();
           }
               }
       }

       private OWLEntity addEntity(OWLOntology ontology, URI uri, int type) {

               OWLEntity newEntity = null;
               try {
                       OWLDataFactory df = ontology.getOWLDataFactory();
                       switch (type) {
                               case CLASS:
                                       newEntity = df.getOWLClass(uri);
                                       break;
                               case DATA_PROP:
                                       newEntity = df.getOWLDataProperty(uri);
                                       break;
                               case OBJECT_PROP:
                                       newEntity = df.getOWLObjectProperty(uri);
                                       break;
                               case INDIVIDUAL:
                                       newEntity = df.getOWLIndividual(uri);
                                       break;
                       }
                       AddEntity ae = new AddEntity(ontology, newEntity, null);
                       ae.accept((ChangeVisitor) ontology);
               }
               catch (Exception ex) {
                       ex.printStackTrace();
               }
               return newEntity;
       }

//      public void readABox(StreamTokenizer in) throws Exception {
//              int token = in.nextToken();
//              while(token != StreamTokenizer.TT_EOF && token != ')') {
//                      assertTrue(token == '(');
//
//                      String str = getToken(in);
//                      if(str.equalsIgnoreCase("INSTANCE")) {
//                              ATermAppl x = getTerm(in);
//                              ATermAppl c = parseExpr(in);
//
//                              kb.addIndividual(x);
//                              kb.addType(x, c);
//                              if(DEBUG) System.out.println("INSTANCE " + x + " " + c);
//                      }
//                      else if(str.equalsIgnoreCase("RELATED")) {
//                              ATermAppl x = getTerm(in);
//                              ATermAppl y = getTerm(in);
//                              ATermAppl r = getTerm(in);
//
//                              kb.addIndividual(x);
//                              kb.addIndividual(y);
//                              kb.addPropertyValue(r, x, y);
//
//                              if(DEBUG) System.out.println("RELATED " + x + " - " + r + " -> " + y);
//                      }
//                      else
//                              throw new RuntimeException("Unknown command " + str);
//
//                      parseToken(in, ')');
//
//                      token = in.nextToken();
//              }
//      }

   public final static void main(String[] args)throws Exception  {
       String loc = "C:/Documents and Settings/UMD/My Documents/Semantic Web/Pellet-Main/test_data/dl-benchmark/tbox/";
       File dir = new File( loc );
       File[] files = dir.listFiles( new FilenameFilter() {
           public boolean accept(File dir, String name) {
               return dir != null && name.endsWith(".tkb")
               && name.indexOf( "-roles" ) == -1 && name.indexOf( "-cd" ) == -1
               && name.indexOf("people") == -1 && name.indexOf("veda-all") == -1
               && name.indexOf("pdwq") == -1;
           }
       });
       Arrays.sort( files, AlphaNumericComparator.CASE_INSENSITIVE );
       for( int i = 0; i < files.length; i++ ) {
           File file = files[i];
           String outFile = file.getAbsolutePath() + ".owl";

           System.out.println( "Converting " + file.getName() );

           KRSSConverter converter = new KRSSConverter();
           OWLOntology ont = converter.readTBox( file.getAbsolutePath() );

           FileWriter writer = new FileWriter( outFile );
           CorrectedRDFRenderer rdfRender = new CorrectedRDFRenderer();
           rdfRender.renderOntology(ont, writer);
           writer.close();
       }

   }
}