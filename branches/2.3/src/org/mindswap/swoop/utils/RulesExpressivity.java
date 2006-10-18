/**
 * 
 * @author Bernardo Cuenca
 * 
 * @author Edna Ruckhaus
 * 
 * 
 * 
 * TODO To change the template for this generated type comment go to Window -
 * 
 * Preferences - Java - Code Generation - Code and Comments
 *  
 */
//
package org.mindswap.swoop.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;



import jpl.Atom;
import jpl.Compound;
import jpl.Query;
import jpl.Term;
import jpl.Variable;

import org.apache.commons.httpclient.HttpURL;
import org.apache.webdav.lib.WebdavResource;
import org.mindswap.pellet.jena.OWLReasoner;
import org.mindswap.swoop.ModelChangeEvent;
import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.SwoopModelListener;
import org.mindswap.swoop.reasoner.SwoopReasoner;
import org.semanticweb.owl.io.owl_rdf.SWRLParser;
import org.semanticweb.owl.model.OWLAnnotationInstance;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataValue;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLNamedObject;
import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.helper.OntologyHelper;
import org.semanticweb.owl.rules.OWLRule;
import org.semanticweb.owl.rules.OWLRuleAtom;
import org.semanticweb.owl.rules.OWLRuleClassAtom;
import org.semanticweb.owl.rules.OWLRuleDObject;
import org.semanticweb.owl.rules.OWLRuleDVariable;
import org.semanticweb.owl.rules.OWLRuleDataPropertyAtom;
import org.semanticweb.owl.rules.OWLRuleDataValue;
import org.semanticweb.owl.rules.OWLRuleEqualityAtom;
import org.semanticweb.owl.rules.OWLRuleIObject;
import org.semanticweb.owl.rules.OWLRuleIVariable;
import org.semanticweb.owl.rules.OWLRuleIndividual;
import org.semanticweb.owl.rules.OWLRuleInequalityAtom;
import org.semanticweb.owl.rules.OWLRuleObjectPropertyAtom;

public class RulesExpressivity implements SwoopModelListener, Cloneable {
	SwoopModel swModel;

	HashMap ruleMap = new HashMap();

	// Expressivity of the set of rules in the ontology
	String rulesExpress;

	// Number of rules in each expressivity "level"
	int[] numRulesExpress = new int[6];

	Set nonDLAtoms = new HashSet();

	String[] typeRulesExpress = new String[6];

	int numRules;

	// Stream for allog database that will be used by allog reasoner for
	// answering queries
	BufferedWriter allogStream;

	public final boolean USE_PROLOG_ENGINE = false;
	
	// Rules expressivity levels
	final int SYNTACTIC_SUGAR = 0;

	final int AL_LOG = 1;

	final int CARIN = 3;

	final int DL_SAFE = 4;

	final int CARIN_AND_DL_SAFE = 2;

	final int SWRL = 5;

	public String getRulesExpress() {
		return rulesExpress;
	}
	
	public HashMap getRuleMap() {
		return ruleMap;
	}
	
	public void setRuleMap(HashMap newRulesMap) {
		ruleMap = newRulesMap;
	}

	
	public int getNumRules() {
		return numRules;
	}

	public int[] getNumRulesExpress() {
		return numRulesExpress;
	}

	public String[] getTypeRulesExpress() {
		return typeRulesExpress;
	}
	


	 public Object clone() {
	 	RulesExpressivity newRE = null; 
        try {
            newRE =  (RulesExpressivity) super.clone();
        }
        catch (CloneNotSupportedException e) {
            // This should never happen
            throw new InternalError(e.toString());
        }
        
        //perform a deep copy on the ruleMap
        newRE.ruleMap = new HashMap();        			
        for (Iterator i = ruleMap.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry e = (Map.Entry) i.next();
            newRE.ruleMap.put(e.getKey(), e.getValue());
        }
        
        return newRE;
    }
	 
	public RulesExpressivity(SwoopModel swoopModel) {

		swModel = swoopModel;
		swModel.addListener(this);

		typeRulesExpress[0] = "OWL Syntactic Sugar";
		typeRulesExpress[1] = "AL_log";
		typeRulesExpress[2] = "CARIN_and_DL_Safe";
		typeRulesExpress[3] = "CARIN";
		typeRulesExpress[4] = "DL_Safe";
		typeRulesExpress[5] = "SWRL";
	}

	private void init() {
		numRules = 0;
		ruleMap = new HashMap();
		rulesExpress = "";
		nonDLAtoms = new HashSet();
		for (int i = 0; i < 6; i++) {
			numRulesExpress[i] = 0;
		}		
	}

	 // returns a set of OWLRuleImpl's
	public Set loadRules() {
		init();
		
		HashSet importedOnts = new HashSet();
		Set rules = new HashSet();
		OWLOntology ontology = swModel.getSelectedOntology();
		try {
		URI uri = ontology.getPhysicalURI();
		//			URI uri = ontology.getURI();
		//			URI uri = swModel.getCurrentlyLoadingURI();
		if (!(ontology.equals(null))) {
			SWRLParser parser = new SWRLParser();
			// For imported ontologies
			importedOnts = (HashSet) ontology.getIncludedOntologies();
			// If there is no imported ontology in the rules file it assumes
			// that rules and ontology are in the same file
			if (importedOnts.isEmpty()) {
				parser.setOntology(ontology);
				rules = parser.parseRules(uri);
				numRules = rules.size();
			} else {
				Iterator it = importedOnts.iterator();
				ontology = (OWLOntology) it.next();
				parser.setOntology(ontology);
				rules = parser.parseRules(uri);
				numRules = rules.size();
			}			
		}
		} catch (Exception e) {
			// e.printStackTrace();
			return rules;
		}
		return rules;
	}
	
	// returns a set of RuleValues
	public Set getRulesFromMap(HashMap ruleMap) {
		
		Set rules = new HashSet();		
		for (Iterator it = ruleMap.values().iterator(); it.hasNext();)
		{
		   rules.addAll((HashSet)it.next());

		} 
		numRules = rules.size();
		return rules;
	}
	
	public void preprocess() throws Exception {
		try {
			if (USE_PROLOG_ENGINE)
				allogStream = new BufferedWriter(new FileWriter("allog/allogDB.pl"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		nonDLAtoms = new HashSet();
		
		Set rules = new HashSet();
		boolean addRulesToMap = false;
		if (ruleMap.isEmpty()) {
			rules = loadRules();
			//need to convert the OWLRuleImpl's to RuleValues			
			Set ruleValues = new HashSet();		
			for (Iterator it = rules.iterator(); it.hasNext();)
			{			  			  
			   ruleValues.add(new RuleValue((OWLRule) it.next(), -1));
			} 
			numRules = rules.size();
			rules = ruleValues;
			addRulesToMap = true;
		} else {
			for (int i = 0; i < 6; i++) {
				numRulesExpress[i] = 0;
			}
			rules = getRulesFromMap(ruleMap);
		}
		
		if (rules.size()==0) return;
		
		OWLOntology ontology = swModel.getSelectedOntology();
		Set importedOnts = ontology.getIncludedOntologies(); 
		if (!importedOnts.isEmpty()) {
			Iterator it = importedOnts.iterator();
			ontology = (OWLOntology) it.next();
		}
		// Expressivity of one rule
		int levelExpressRule;

		if (numRules != 0) {

			for (Iterator it = rules.iterator(); it.hasNext();) {
				OWLRule rule = ((RuleValue) it.next()).getRule();
				// Expressivity of each rule
				levelExpressRule = calcOneRuleExpress(rule, ontology, true);				
				if (levelExpressRule == AL_LOG) {
					// add rule and all non-DL atom to Allog stream
					writeAllog(rule, ontology);
				}				
			}
		}
		//find the al-log rules and write them to allogStream		
		if (USE_PROLOG_ENGINE) {
			
			allogStream.flush();
			allogStream.close();	
		
			// Pre-process all the DL atoms and add them to the Datalog
		// 	database
		
//			  Query q1 = new Query("spy(shell)");
//			  System.out.println("consult q1 " + (q1.hasSolution() ? "succeeded" : "failed"));
			
			if ((numRulesExpress[0] + numRulesExpress[1]) > 0) {
				Atom of = new Atom(ontology.getPhysicalURI().toString());
				Atom ol = new Atom(ontology.getLogicalURI().toString());
				System.out.println("logical URI is " + ol.toString());
				Term t = new Compound("preProcess", new Term[] { of, ol });
				Query q1 = new Query(t);
				System.out.println("consult q1 "
						+ (q1.hasSolution() ? "succeeded" : "failed"));
				q1 = new Query("listing(clause1)");
				System.out.println("consult q1 "
						+ (q1.hasSolution() ? "succeeded" : "failed"));
			}
		}
	}
	
	
	// Calculates the expressivity of the set of rules
	// split this in two, one function to load and parse rules, another to calc expressivity
	public void setRulesExpress() throws OWLException {	
		nonDLAtoms = new HashSet();
				
		Set rules = new HashSet();
		boolean addRulesToMap = false;
		if (ruleMap.isEmpty()) {
			rules = loadRules();
			//need to convert the OWLRuleImpl's to RuleValues			
			Set ruleValues = new HashSet();		
			for (Iterator it = rules.iterator(); it.hasNext();)
			{			  			  
			   ruleValues.add(new RuleValue((OWLRule) it.next(), -1));
			} 
			numRules = rules.size();
			rules = ruleValues;
			addRulesToMap = true;
		} else {
			for (int i = 0; i < 6; i++) {
				numRulesExpress[i] = 0;
			}
			rules = getRulesFromMap(ruleMap);
		}
		
		if (rules.size()==0) return;
		
		OWLOntology ontology = swModel.getSelectedOntology();
		Set importedOnts = ontology.getIncludedOntologies(); 
		if (!importedOnts.isEmpty()) {
			Iterator it = importedOnts.iterator();
			ontology = (OWLOntology) it.next();
		}
		// Expressivity of the set of rules. It is the maximum of the
		// expressivity levels in the set
		int levelExpress = 0;
		// Expressivity of one rule
		int levelExpressRule;

		if (numRules != 0) {

			for (Iterator it = rules.iterator(); it.hasNext();) {
				OWLRule rule = ((RuleValue) it.next()).getRule();
				// Expressivity of each rule
				levelExpressRule = calcOneRuleExpress(rule, ontology, true);				
				
				OWLRuleAtom consAtom = (OWLRuleAtom) rule.getConsequents().iterator().next();
				// Create HashMap for rules (class or property)
				// Key is consequent and object is its set of rules
				// values
				// Each rule value is a pair (rule, expressivity)
				RuleValue ruleValue = new RuleValue(rule, levelExpressRule);
				if (addRulesToMap) {
					addMap(ruleValue, consAtom);
				}
				// Maximum of set of rules expressivity
				if (levelExpressRule > levelExpress) {
					levelExpress = levelExpressRule;
				}
			}
			
			// Setting the expressivity level variable
			switch (levelExpress) {
			case SYNTACTIC_SUGAR:
				rulesExpress = "Syntactic Sugar for OWL";
				rulesExpress += "<br>"
					+ "Antecedent and Consequent have exactly one common variable ";
				rulesExpress += "<br>"
					+ "Or no common variables and at least one common individual";
				break;
			case AL_LOG:
				rulesExpress = "AL-log";
				rulesExpress += "<br>"
					+ "Class DL-Atoms in Antecedent of rules";
				break;
			case CARIN_AND_DL_SAFE:
				rulesExpress = "CARIN and DL-Safe";
				rulesExpress += "<br>"
					+ "Class and Role DL-Atoms in Antecedent of rules";
				rulesExpress += "<br>"
					+ "Each variable in a rule appears in a non-DL atom in the body of the rule";
				break;
			case CARIN:
				rulesExpress = "CARIN";
				rulesExpress += "<br>"
					+ "Class and Role DL-Atoms in Antecedent of rules";
				break;
			case DL_SAFE:
				rulesExpress = "DL-Safe";
				rulesExpress += "<br>"
					+ "Class and Role DL-Atoms in Antecedent and Consequent of rules";
				rulesExpress += "<br>"
					+ "Each variable in a rule appears in a non-DL atom in the body of the rule";
				break;
			case SWRL:
				rulesExpress = "SWRL";
				rulesExpress += "<br>"
					+ "Class and Role DL-Atoms in Antecedent and Consequent of rules";
				break;
			}
		}
	}		
	

	public void addMap(RuleValue ruleValue, OWLRuleAtom consAtom) {
		HashSet rulesSet = new HashSet();
		OWLObject key = null;
		Set antecedents;
		Set consequents;
		try {
			antecedents = ruleValue.getRule().getAntecedents();
			consequents = ruleValue.getRule().getConsequents();
			if (consAtom instanceof OWLRuleClassAtom) {
				key = ((OWLRuleClassAtom) consAtom).getDescription();
			} else {
				if (consAtom instanceof OWLRuleDataPropertyAtom) {
					key = ((OWLRuleDataPropertyAtom) consAtom).getProperty();
				} else {
					if (consAtom instanceof OWLRuleObjectPropertyAtom) {
						key = ((OWLRuleObjectPropertyAtom) consAtom)
								.getProperty();
					}
				}
			}
			if (ruleMap.containsKey(key))
				rulesSet = (HashSet) ruleMap.get(key);
			rulesSet.add(ruleValue);
			ruleMap.put(key, rulesSet);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int calcOneRuleExpress(OWLRule rule, OWLOntology onto, boolean update) {
		int answer = 0;
		boolean isDLSafe = false;
		boolean isCARIN = false;
		if (checkSyntSugar(rule)) {
			answer = SYNTACTIC_SUGAR;
		} else {
			if (checkALLog(rule, onto)) {
				answer = AL_LOG;
			} else {
				isDLSafe = checkDLSafe(rule, onto);
				isCARIN = checkCARIN(rule, onto);
				if (isCARIN & isDLSafe) {
					answer = CARIN_AND_DL_SAFE;
				} else {
					if (isCARIN) {
						answer = CARIN;
					} else {
						if (isDLSafe) {
							answer = DL_SAFE;
						} else {
							answer = SWRL;
						}
					}
				}

			}

		}
		if (update) numRulesExpress[answer]++;
		return (answer);
	}

	/**
	 * Checks if a given SWRL rule is a CARIN rule
	 *  
	 */
	public boolean checkCARIN(OWLRule rule, OWLOntology onto) {
		try {
			Set antecedent = rule.getAntecedents();
			Set consequent = rule.getConsequents();
			for (Iterator i = consequent.iterator(); i.hasNext();) {
				OWLRuleAtom consAtom = (OWLRuleAtom) i.next();
				if (isDLAtom(consAtom, onto)) {
					return false;
				}
			}
		} catch (OWLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * Checks if a given SWRL rule is DL safe It checks that each variable in r
	 * occurs in a non-DL atom in the rule body
	 */
	public boolean checkDLSafe(OWLRule rule, OWLOntology onto) {
		try {
			Set antecedent = rule.getAntecedents();
			Set consequent = rule.getConsequents();
			HashSet vars = new HashSet();
			HashSet bodyNonDLVars = new HashSet();
			// Constructs sets of variables and variables in non-DL atoms in the
			// antecedent of the rule
			for (Iterator i = antecedent.iterator(); i.hasNext();) {
				OWLRuleAtom antAtom = (OWLRuleAtom) i.next();
				if (antAtom instanceof OWLRuleClassAtom) {
					OWLRuleIObject arg = ((OWLRuleClassAtom) antAtom)
							.getArgument();
					if (arg instanceof OWLRuleIVariable) {
						URI aux = ((OWLRuleIVariable) arg).getURI();
						vars.add(aux);
						if (!(isDLAtom(antAtom, onto)))
							bodyNonDLVars.add(aux);
					}
				}
				if (antAtom instanceof OWLRuleObjectPropertyAtom) {
					OWLRuleIObject arg1 = ((OWLRuleObjectPropertyAtom) antAtom)
							.getFirstArgument();
					if (arg1 instanceof OWLRuleIVariable) {
						URI aux = ((OWLRuleIVariable) arg1).getURI();
						vars.add(aux);
						if (!(isDLAtom(antAtom, onto)))
							bodyNonDLVars.add(aux);
					}

					OWLRuleIObject arg2 = ((OWLRuleObjectPropertyAtom) antAtom)
							.getSecondArgument();
					if (arg2 instanceof OWLRuleIVariable) {
						URI aux = ((OWLRuleIVariable) arg2).getURI();
						vars.add(aux);
						if (!(isDLAtom(antAtom, onto)))
							bodyNonDLVars.add(aux);
					}

				}
			}
			// Adds variables of consequent to set of variables
			for (Iterator j = consequent.iterator(); j.hasNext();) {
				OWLRuleAtom consAtom = (OWLRuleAtom) j.next();
				if (consAtom instanceof OWLRuleClassAtom) {
					OWLRuleIObject arg = ((OWLRuleClassAtom) consAtom)
							.getArgument();
					if (arg instanceof OWLRuleIVariable) {
						URI aux = ((OWLRuleIVariable) arg).getURI();
						vars.add(aux);
					}

				}
				if (consAtom instanceof OWLRuleObjectPropertyAtom) {
					OWLRuleIObject arg1 = ((OWLRuleObjectPropertyAtom) consAtom)
							.getFirstArgument();
					if (arg1 instanceof OWLRuleIVariable) {
						URI aux = ((OWLRuleIVariable) arg1).getURI();
						vars.add(aux);
					}
					OWLRuleIObject arg2 = ((OWLRuleObjectPropertyAtom) consAtom)
							.getSecondArgument();
					if (arg2 instanceof OWLRuleIVariable) {
						URI aux = ((OWLRuleIVariable) arg2).getURI();
						vars.add(aux);
					}

				}
			}
			// Finds if each variable in a rule appeaars in a non-DL atom in the
			// body of rule
			if (vars.equals(bodyNonDLVars))
				return true;
			else
				return false;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	public boolean checkALLog(OWLRule rule, OWLOntology onto) {
		try {
			Set antecedent = rule.getAntecedents();
			Set consequent = rule.getConsequents();
			for (Iterator i = consequent.iterator(); i.hasNext();) {

				OWLRuleAtom consAtom = (OWLRuleAtom) i.next();
				if (isDLAtom(consAtom, onto)) {
					return false;
				}
			}
			for (Iterator j = antecedent.iterator(); j.hasNext();) {

				OWLRuleAtom antAtom = (OWLRuleAtom) j.next();

				if (antAtom instanceof OWLRuleObjectPropertyAtom
						&& isDLAtom(antAtom, onto)) {
					return false;
				}
			}
		} catch (OWLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * Checks if a given SWRL rule can be expressed in OWL
	 *  
	 */
	public boolean checkSyntSugar(OWLRule rule) {
		String answer = "";
		try {
			Set antecedent = rule.getAntecedents();
			Set consequent = rule.getConsequents();
			if (antecedent.isEmpty() || (consequent.isEmpty())) {
				return true;
			} else {
				HashSet antVars = new HashSet();
				HashSet consVars = new HashSet();
				HashSet antInds = new HashSet();
				HashSet consInds = new HashSet();
				// Constructs sets of variables and individuals in antecedent
				for (Iterator i = antecedent.iterator(); i.hasNext();) {
					OWLRuleAtom antAtom = (OWLRuleAtom) i.next();
					if (antAtom instanceof OWLRuleClassAtom) {
						OWLRuleIObject arg = ((OWLRuleClassAtom) antAtom)
								.getArgument();
						if (arg instanceof OWLRuleIVariable) {
							URI aux = ((OWLRuleIVariable) arg).getURI();
							antVars.add(aux);
						} else {
							OWLIndividual aux = ((OWLRuleIndividual) arg)
									.getIndividual();
							antInds.add(aux);
						}
					}
					if (antAtom instanceof OWLRuleObjectPropertyAtom) {
						OWLRuleIObject arg1 = ((OWLRuleObjectPropertyAtom) antAtom)
								.getFirstArgument();
						if (arg1 instanceof OWLRuleIVariable) {
							URI aux = ((OWLRuleIVariable) arg1).getURI();
							antVars.add(aux);
						} else {
							OWLIndividual aux = ((OWLRuleIndividual) arg1)
									.getIndividual();
							antInds.add(aux);
						}
						OWLRuleIObject arg2 = ((OWLRuleObjectPropertyAtom) antAtom)
								.getSecondArgument();
						if (arg2 instanceof OWLRuleIVariable) {
							URI aux = ((OWLRuleIVariable) arg2).getURI();
							antVars.add(aux);
						} else {
							OWLIndividual aux = ((OWLRuleIndividual) arg2)
									.getIndividual();
							antInds.add(aux);
						}
					}
				}
				// Construct sets of variables and individuals in consequent
				for (Iterator j = consequent.iterator(); j.hasNext();) {
					OWLRuleAtom consAtom = (OWLRuleAtom) j.next();
					if (consAtom instanceof OWLRuleClassAtom) {
						OWLRuleIObject arg = ((OWLRuleClassAtom) consAtom)
								.getArgument();
						if (arg instanceof OWLRuleIVariable) {
							URI aux = ((OWLRuleIVariable) arg).getURI();
							consVars.add(aux);
						} else {
							OWLIndividual aux = ((OWLRuleIndividual) arg)
									.getIndividual();
							consInds.add(aux);
						}
					}
					if (consAtom instanceof OWLRuleObjectPropertyAtom) {
						OWLRuleIObject arg1 = ((OWLRuleObjectPropertyAtom) consAtom)
								.getFirstArgument();
						if (arg1 instanceof OWLRuleIVariable) {
							URI aux = ((OWLRuleIVariable) arg1).getURI();
							consVars.add(aux);
						} else {
							OWLIndividual aux = ((OWLRuleIndividual) arg1)
									.getIndividual();
							consInds.add(aux);
						}
						OWLRuleIObject arg2 = ((OWLRuleObjectPropertyAtom) consAtom)
								.getSecondArgument();
						if (arg2 instanceof OWLRuleIVariable) {
							URI aux = ((OWLRuleIVariable) arg2).getURI();
							consVars.add(aux);
						} else {
							OWLIndividual aux = ((OWLRuleIndividual) arg2)
									.getIndividual();
							consInds.add(aux);
						}
					}
				}
				// Finds number of common variables and common individuals.
				// A rule can be expressed in OWL if it has exactly one common
				// variable, or
				// if it has no common variables and at least one common
				// individual.
				antVars.retainAll(consVars);
				antInds.retainAll(consInds);
				int varsSize = antVars.size();
				int indsSize = antInds.size();
				if (varsSize == 0) {
					if (indsSize > 0)
						return true;
				} else if (varsSize == 1)
					return true;
				else
					return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	public static boolean isDLAtom(OWLRuleAtom atom, OWLOntology ont)
			throws OWLException {
		//If the class is defined in terms of others in the ontology,
		// or used elsewhere in the ontology then it is a DL-atom
		if (atom instanceof OWLRuleClassAtom) {
			OWLDescription desc = ((OWLRuleClassAtom) atom).getDescription();
			//			A complex description will always be a DL atom
			if (!(desc instanceof OWLClass)) {
				return true;
			}
			//Check if it is used anywhere in the ontology
			Set aux = OntologyHelper.entityUsage(ont, (OWLClass) desc);
			// Create a new set of usage that excludes the instances of the
			// class
			Set aux1 = new HashSet();
			for (Iterator it = aux.iterator(); it.hasNext();) {
				OWLObject object = (OWLObject) it.next();
				if (!(object instanceof OWLIndividual)) {
					aux1.add(object);
				}
			}
			// If it os defined in terms of others in the ontology
			Set aux2 = ((OWLClass) desc).getSuperClasses(ont);
			Set aux3 = ((OWLClass) desc).getEquivalentClasses(ont);
			aux1.addAll(aux2);
			aux1.addAll(aux3);
			if (aux1.isEmpty())
				return false;
			else
				return true;

		}

		// An object property that:
		//1) is used in a restriction in the ontology, OR
		// 2)it is the inverse of a property that is used in a restriction OR
		// 3)it has a subProperty or a superProperty that is used in a restr
		// 4) it is stated to be transitive, functional or inverse functional
		//Then it is a DL-atom
		if (atom instanceof OWLRuleObjectPropertyAtom) {
			OWLObjectProperty prop = ((OWLRuleObjectPropertyAtom) atom)
					.getProperty();
			if (prop.isTransitive(ont) || prop.isSymmetric(ont)
					|| prop.isFunctional(ont))
				return true;
			Set aux = OntologyHelper.entityUsage(ont, prop);
			// Create a new set without the assetions on the properties
			Set aux1 = new HashSet();
			for (Iterator it = aux.iterator(); it.hasNext();) {
				OWLObject object = (OWLObject) it.next();
				if (!(object instanceof OWLIndividual)) {
					aux1.add(object);
				}
			}
			Set aux2 = prop.getSuperProperties(ont);
			Set aux3 = prop.getInverses(ont);
			aux1.addAll(aux2);
			aux1.addAll(aux3);
			if (aux1.isEmpty()) {
				return false;
			} else
				return true;

		}
		// If the data property is defined in terms of others in the ontology,
		// or it has super properties
		// then it is a DL-atom
		if (atom instanceof OWLRuleDataPropertyAtom) {
			OWLDataProperty prop = ((OWLRuleDataPropertyAtom) atom)
					.getProperty();
			Set aux = OntologyHelper.entityUsage(ont, prop);
			Set aux1 = new HashSet();
			for (Iterator it = aux.iterator(); it.hasNext();) {
				OWLObject object = (OWLObject) it.next();
				if (!(object instanceof OWLIndividual)) {
					aux1.add(object);
				}
			}
			Set aux2 = prop.getSuperProperties(ont);
			aux1.addAll(aux2);
			if (aux1.isEmpty()) {
				return false;
			} else
				return true;

		}

		return false;
	}

	// Write the allog database with the non-dl atoms rules and facts
	private void writeAllog(OWLRule rule, OWLOntology onto) {
		try {
			String allogCons = "";
			String allogRule = "";
			String allogFact = "";
			allogRule = "rule(";
			OWLRuleAtom consAtom = (OWLRuleAtom) rule.getConsequents()
					.iterator().next();
			allogRule = allogRule.concat(writeAtom(consAtom));
			Object[] antecedents = rule.getAntecedents().toArray();
			allogRule = allogRule.concat(writeAntecedents(antecedents, onto)
					+ ")." + "\n");
			allogStream.write(allogRule);			

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String writeAntecedents(Object[] atoms, OWLOntology onto) {
		String cons = "";
		try {
			int length = atoms.length;
			OWLRuleAtom atom = (OWLRuleAtom) atoms[0];
			if (!(isDLAtom(atom, onto))) {
				//System.out.println("non-dl-atom " + atom.toString());
				if (!(nonDLAtoms.contains(atom.toString()))) {
					System.out.println("non atom " + atom.toString());
					nonDLAtoms.add(atom.toString());
					writeFacts(atom, onto);
				}
				if (length == 1)
					cons = cons.concat("," + writeAtom(atom));
				else {
					Object[] atoms1 = new Object[length - 1];
					for (int i = 1; i < length; i++) {
						atoms1[i - 1] = atoms[i];
					}
					cons = cons.concat(",(" + writeAtom(atom)
							+ writeAntecedents(atoms1, onto) + ")");
				}
			} else {
				if (length == 1)
					cons = cons.concat("," + writeConstraint(atom));
				else {
					Object[] atoms1 = new Object[length - 1];
					for (int i = 1; i < length; i++) {
						atoms1[i - 1] = atoms[i];
					}
					cons = cons.concat(",(" + writeConstraint(atom)
							+ writeAntecedents(atoms1, onto) + ")");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cons;
	}

	private void writeFacts(OWLRuleAtom atom, OWLOntology onto) {
		try {
			if (atom instanceof OWLRuleClassAtom) {
				OWLDescription desc = ((OWLRuleClassAtom) atom)
						.getDescription();

				OWLClass clazz = (OWLClass) desc;
				Set objects1 = clazz.getUsage(onto);
				for (Iterator it = objects1.iterator(); it.hasNext();) {
					OWLEntity entity = (OWLEntity) it.next();
					if (entity instanceof OWLIndividual) {
						allogStream
								.write("fact("
										+ clazz.getURI().getFragment()
												.toLowerCase() + "("
										+ entity.getURI().getFragment()
										+ ")).\n");
					}
				}

			}

			if (atom instanceof OWLRuleObjectPropertyAtom) {
				OWLObjectProperty prop = (OWLObjectProperty) ((OWLRuleObjectPropertyAtom) atom)
						.getProperty();
				Set objects1 = prop.getUsage(onto);
				for (Iterator it = objects1.iterator(); it.hasNext();) {
					OWLEntity entity = (OWLEntity) it.next();
					if (entity instanceof OWLIndividual) {
						Map objects2 = ((OWLIndividual) entity)
								.getObjectPropertyValues(onto);
						for (Iterator it1 = ((Set) objects2.get(prop))
								.iterator(); it1.hasNext();) {
							allogStream.write("fact("
									+ prop.getURI().getFragment().toLowerCase()
									+ "("
									+ entity.getURI().getFragment()
									+ ","
									+ ((OWLEntity) it1.next()).getURI()
											.getFragment() + ")).\n");
						}
					}

				}
			}
			if (atom instanceof OWLRuleDataPropertyAtom) {
				OWLDataProperty prop = (OWLDataProperty) ((OWLRuleDataPropertyAtom) atom)
						.getProperty();
				Set objects1 = prop.getUsage(onto);
				for (Iterator it = objects1.iterator(); it.hasNext();) {
					OWLEntity entity = (OWLEntity) it.next();
					if (entity instanceof OWLIndividual) {
						Map objects2 = ((OWLIndividual) entity)
								.getDataPropertyValues(onto);
						for (Iterator it1 = ((Set) objects2.get(prop))
								.iterator(); it1.hasNext();) {
							allogStream.write("fact("
									+ prop.getURI().getFragment().toLowerCase()
									+ "(" + entity.getURI().getFragment() + ","
									+ ((OWLEntity) it1.next()).toString()
									+ ")).\n");
						}
					}

				}
			}
		}

		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String writeAtom(OWLRuleAtom atom) {
		String result = "";
		try {
			if (atom instanceof OWLRuleClassAtom) {
				OWLDescription desc = ((OWLRuleClassAtom) atom)
						.getDescription();
				URI aux = ((OWLClass) desc).getURI();
				result = result.concat(aux.getFragment().toLowerCase() + "(");
				OWLRuleIObject var = ((OWLRuleClassAtom) atom).getArgument();
				if (var instanceof OWLRuleIVariable) {
					URI aux1 = ((OWLRuleIVariable) var).getURI();
					result = result = result.concat("'$"
							+ aux1.getFragment().toUpperCase() + "')");
				} else {
					URI aux1 = ((OWLNamedObject) ((OWLRuleIndividual) var)
							.getIndividual()).getURI();
					result = result.concat(aux1.getFragment() + ")");
				}
			}
			if (atom instanceof OWLRuleObjectPropertyAtom) {
				OWLObjectProperty prop = ((OWLRuleObjectPropertyAtom) atom)
						.getProperty();
				URI aux = prop.getURI();
				result = result.concat(aux.getFragment().toLowerCase() + "(");
				OWLRuleIObject var = ((OWLRuleObjectPropertyAtom) atom)
						.getFirstArgument();
				if (var instanceof OWLRuleIVariable) {
					URI aux1 = ((OWLRuleIVariable) var).getURI();
					result = result.concat("'$"
							+ aux1.getFragment().toUpperCase() + "',");
				} else {
					URI aux1 = ((OWLNamedObject) ((OWLRuleIndividual) var)
							.getIndividual()).getURI();
					result = result.concat(aux1.getFragment() + ",");
				}
				OWLRuleIObject var2 = ((OWLRuleObjectPropertyAtom) atom)
						.getSecondArgument();
				if (var2 instanceof OWLRuleIVariable) {
					URI aux1 = ((OWLRuleIVariable) var2).getURI();
					result = result.concat("'$"
							+ aux1.getFragment().toUpperCase() + "')");
				} else {
					URI aux1 = ((OWLNamedObject) ((OWLRuleIndividual) var2)
							.getIndividual()).getURI();
					result = result.concat(aux1.getFragment() + ")");
				}
			}
			if (atom instanceof OWLRuleDataPropertyAtom) {
				OWLDataProperty prop = ((OWLRuleDataPropertyAtom) atom)
						.getProperty();
				URI aux = prop.getURI();
				result = result = result.concat(aux.getFragment().toLowerCase()
						+ "(");
				OWLRuleDObject var = (OWLRuleDObject) ((OWLRuleDataPropertyAtom) atom)
						.getFirstArgument();
				if (var instanceof OWLRuleDVariable) {
					URI aux1 = ((OWLRuleIVariable) var).getURI();
					result = result.concat("'$"
							+ aux1.getFragment().toUpperCase() + "',");
				} else {
					URI aux1 = ((OWLNamedObject) ((OWLRuleIndividual) var)
							.getIndividual()).getURI();
					result = result.concat(aux1.getFragment() + ",");
				}
				OWLRuleDObject var2 = ((OWLRuleDataPropertyAtom) atom)
						.getSecondArgument();
				if (var2 instanceof OWLRuleIVariable) {
					URI aux1 = ((OWLRuleIVariable) var2).getURI();
					result = result.concat("'$"
							+ aux1.getFragment().toUpperCase() + "')");
				} else {
					String aux1 = ((OWLRuleDataValue) var).toString();
					result = result.concat(aux1 + ")");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	// Writing Allog constraint atoms
	private String writeConstraint(OWLRuleAtom atom) {
		String result = "";
		try {
			if (atom instanceof OWLRuleClassAtom) {
				OWLDescription desc = ((OWLRuleClassAtom) atom)
						.getDescription();
				URI aux = ((OWLClass) desc).getURI();
				result = result.concat("restriction(");
				OWLRuleIObject var = ((OWLRuleClassAtom) atom).getArgument();
				if (var instanceof OWLRuleIVariable) {
					URI aux1 = ((OWLRuleIVariable) var).getURI();
					result = result = result.concat("'$"
							+ aux1.getFragment().toUpperCase() + "',");
				} else {
					URI aux1 = ((OWLNamedObject) ((OWLRuleIndividual) var)
							.getIndividual()).getURI();
					result = result.concat("'" + aux1.getFragment() + "',");
				}
				result = result.concat("'" + aux.getFragment() + "')");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mindswap.swoop.SwoopModelListener#modelChanged(org.mindswap.swoop.ModelChangeEvent)
	 */
	public void modelChanged(ModelChangeEvent event) {
		// TODO Auto-generated method stub
		if ((swModel.getEnableRules())
				&& (event.getType() == ModelChangeEvent.ONTOLOGY_LOADED
						|| event.getType() == ModelChangeEvent.ONTOLOGY_SEL_CHANGED
						|| event.getType() == ModelChangeEvent.ONTOLOGY_RELOADED || event
						.getType() == ModelChangeEvent.ONTOLOGY_CHANGED)) {
			try {
			this.setRulesExpress();
			} catch (OWLException e) {
				e.printStackTrace();
			}
		}
	}

	public Set getRules(OWLObject obj) {
		if (ruleMap.containsKey(obj)) {
			return (HashSet) ruleMap.get(obj);
		} else
			return new HashSet();
	}

	public String publishRulesToPychinko()  {
		
		// need to print out header code here
		String n3 = "@prefix log: <http://www.w3.org/2000/10/swap/log#>.\n" +				
				"@prefix str: <http://www.w3.org/2000/10/swap/string#>.\n" + 
				"@prefix owl: <http://www.w3.org/2002/07/owl#>.\n" +
				"@prefix xsd: <http://www.w3.org/2001/XMLSchema#>.\n" +
				"@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>.\n" +
				"@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>.\n" +
				"@prefix : <http://counterterror.mindswap.org/2005/terrorism.owl#>.\n";

		HashSet rules =  (HashSet) getRulesFromMap(ruleMap);
		
		Iterator it = rules.iterator();		
		
		try {
			while (it.hasNext()) {
				n3 += "\n{ ";
				RuleValue rv = (RuleValue) it.next();
				OWLRule rule = rv.getRule();
				
				//  print out the whole URI for predicates and classes
				//  fragments for avariables only
				HashSet antecedents  = (HashSet) rule.getAntecedents();
				Iterator antIterator = antecedents.iterator();
				while (antIterator.hasNext()) {
					OWLRuleAtom ant = (OWLRuleAtom) antIterator.next();
					n3 += renderRuleAtom(ant);
				}
												
				// --------consequents
				n3 += "}\n=> \n{";				
				
				OWLRuleAtom consequent = (OWLRuleAtom) rule.getConsequents().iterator().next();
				n3 += renderRuleAtom(consequent);
				n3 += "}.\n";
								
				String consequentPredicate = getURIString( consequent );
				String label = getConsequentLabel( consequent );
				if ( label != null )
					n3 += "<" + consequentPredicate + ">" + " rdfs:label " + "\"" + label + "\" .";
				else
					n3 += "<" + consequentPredicate + ">" + " rdfs:label " + "\"" + guessConsequentLabel( consequent ) + "\" .";
				
			}
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			n3 += "\nERROR!";
		}
		return n3;
	}


	public String toN3( OWLRule rule )
	{		
		// need to print out header code here
		String n3 = "@prefix log: <http://www.w3.org/2000/10/swap/log#>.\n" +				
				"@prefix str: <http://www.w3.org/2000/10/swap/string#>.\n" + 
				"@prefix owl: <http://www.w3.org/2002/07/owl#>.\n" +
				"@prefix xsd: <http://www.w3.org/2001/XMLSchema#>.\n" +
				"@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>.\n" +
				"@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>.\n";
		
		try {

			n3 += "\n{ ";
			//  print out the whole URI for predicates and classes
			//  fragments for avariables only
			HashSet antecedents  = (HashSet) rule.getAntecedents();
			Iterator antIterator = antecedents.iterator();
			while (antIterator.hasNext()) {
				OWLRuleAtom ant = (OWLRuleAtom) antIterator.next();
				n3 += renderRuleAtom(ant);
			}
											
			// --------consequents
			n3 += "}\n=> \n{";				
			
			OWLRuleAtom consequent = (OWLRuleAtom) rule.getConsequents().iterator().next();
			n3 += renderRuleAtom(consequent);
			n3 += "}.\n";
							
			String consequentPredicate = getURIString( consequent );
			String label = getLabel( rule );
			n3 += "<" + consequentPredicate + ">" + " rdfs:label " + "\"" + label + "\" .";
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			n3 += "\nERROR!";
		}
		return n3;		
	}
	
	public String getPredicate( OWLRule rule )
	{
		String consequentPredicate = null;
		try
		{
			OWLRuleAtom consequent = (OWLRuleAtom) rule.getConsequents().iterator().next();
			consequentPredicate = getURIString( consequent );
		}
		catch ( Exception e )
		{ e.printStackTrace(); }
		return consequentPredicate;
	}
	
	public String getLabel( OWLRule rule )
	{			
		String label = null;
		try
		{
			OWLRuleAtom consequent = (OWLRuleAtom) rule.getConsequents().iterator().next();
			label = getConsequentLabel( consequent );
			if ( label == null )
				label = guessConsequentLabel( consequent );
			}
		catch ( Exception e )
		{			
			e.printStackTrace();
		}
		return label;
	}
	
	/*
	 * Returns label for the given consequent (corresponding to SWOOP's rdfs:label for that entity)
	 *   If it doesn't exist, it returns null;
	 */
	private String getConsequentLabel( OWLRuleAtom atom )
	{
		String label = null;
		try
		{
			// adding labels for rules
			if ( atom instanceof OWLRuleClassAtom )
			{
				OWLDescription desc = ((OWLRuleClassAtom) atom ).getDescription();					
				URI classURI = ((OWLClass) desc).getURI();
				Set set = desc.getAnnotations();
				for (Iterator iter = set.iterator(); iter.hasNext();)
				{
					OWLAnnotationInstance oai = (OWLAnnotationInstance) iter.next();
					URI uri = oai.getProperty().getURI();
					if ( uri.toString().equals("http://www.w3.org/2000/01/rdf-schema#label") )
						label = oai.getContent().toString();
				} 
			}
			else if ( atom instanceof OWLRuleObjectPropertyAtom )
			{
				OWLObjectProperty prop = ((OWLRuleObjectPropertyAtom) atom).getProperty();					
				URI propURI = prop.getURI();					
				Set set = prop.getAnnotations();
				for (Iterator iter = set.iterator(); iter.hasNext();)
				{
					OWLAnnotationInstance oai = (OWLAnnotationInstance) iter.next();
					URI uri = oai.getProperty().getURI();
					if ( uri.toString().equals("http://www.w3.org/2000/01/rdf-schema#label") )
					{
						label = oai.getContent().toString();
					}
				}
			}
			else // OWLRuleDataPropertyAtom
			{ 
				OWLDataProperty prop = ((OWLRuleDataPropertyAtom) atom).getProperty();					
				URI propURI = prop.getURI();					
				Set set = prop.getAnnotations();
				for (Iterator iter = set.iterator(); iter.hasNext();)
				{
					OWLAnnotationInstance oai = (OWLAnnotationInstance) iter.next();
					URI uri = oai.getProperty().getURI();
					if ( uri.toString().equals("http://www.w3.org/2000/01/rdf-schema#label") )
						label = oai.getContent().toString();
				}
			}		
		}
		catch ( Exception e )
		{ e.printStackTrace();}
		
		return label;
	}
	
	private String guessConsequentLabel( OWLRuleAtom consequent )
	{
		String uriStr = getURIString( consequent );		
		int ind = uriStr.indexOf( "#" );
		String guess = "";
		if ( !( ind < 0 ) )
			guess = uriStr.substring( ind + 1 );
		else
		{
			ind = uriStr.lastIndexOf( "/" );
			guess = uriStr.substring( ind + 1 );
		}		
		return guess;
	}
	
	private String getURIString( OWLRuleAtom atom )
	{
		try
		{
			if ( atom instanceof OWLRuleClassAtom )
			{
				OWLDescription desc = ((OWLRuleClassAtom) atom ).getDescription();					
				URI classURI = ((OWLClass) desc).getURI();
				return classURI.toString();
			}
			else if ( atom instanceof OWLRuleObjectPropertyAtom )
			{
				OWLObjectProperty prop = ((OWLRuleObjectPropertyAtom) atom).getProperty();					
				URI propURI = prop.getURI();
				return propURI.toString();
			}
			else // OWLRuleDataPropertyAtom
			{ 
				OWLDataProperty prop = ((OWLRuleDataPropertyAtom) atom).getProperty();					
				URI propURI = prop.getURI();					
				return propURI.toString();
			}	
		}
		catch ( Exception e)
		{ e.printStackTrace(); }
		return null;
	}
	
	/**
	 * @param consequent
	 * @return n3 representatio of the ruleatom
	 */
	private String renderRuleAtom(OWLRuleAtom atom) {		
		String result = "";
		try {
			if (atom instanceof OWLRuleClassAtom) {
				OWLDescription desc = ((OWLRuleClassAtom) atom).getDescription();
				
				URI classURI = ((OWLClass) desc).getURI();
				OWLRuleIObject var = ((OWLRuleClassAtom) atom).getArgument();
				if (var instanceof OWLRuleIVariable) {
					
					//  "?x rdf:type b"					
					String varName = ((OWLRuleIVariable) var).getURI().toString();					
					result = result = result.concat("?"
							+ varName + "  rdf:type ");
					result = result.concat(" <" + classURI + "> ");
					
				} else {
					//not a variable: x rdf:type w					
					URI indURI = ((OWLNamedObject) ((OWLRuleIndividual) var).getIndividual()).getURI();
					result = result.concat("< " + indURI + ">  rdf:Type ");
					result = result.concat(" <" + classURI + "> ");						
				}
			}
			if (atom instanceof OWLRuleObjectPropertyAtom) {
				OWLObjectProperty prop = ((OWLRuleObjectPropertyAtom) atom).getProperty();
				
				URI propURI = prop.getURI();
				
				OWLRuleIObject var = ((OWLRuleObjectPropertyAtom) atom).getFirstArgument();				
				if (var instanceof OWLRuleIVariable) {
				
					String varName = ((OWLRuleIVariable) var).getURI().toString();					
					result = result.concat("?" + varName + " ");
										
				} else {
					URI indURI = ((OWLNamedObject) ((OWLRuleIndividual) var).getIndividual()).getURI();
					result = result.concat("< " + indURI + "> ");
				}
				
				result = result.concat("<" + propURI + "> ");
				
				var = ((OWLRuleObjectPropertyAtom) atom).getSecondArgument();
				if (var instanceof OWLRuleIVariable) {
					
						String varName = ((OWLRuleIVariable) var).getURI().toString();					
						result = result.concat("?" + varName + " ");
											
					} else {
						URI indURI = ((OWLNamedObject) ((OWLRuleIndividual) var).getIndividual()).getURI();
						result = result.concat("< " + indURI + "> ");
				}
					
			}
			if (atom instanceof OWLRuleDataPropertyAtom) {
				/*
				OWLDataProperty prop = ((OWLRuleDataPropertyAtom) atom)
				.getProperty();
				URI aux = prop.getURI();
				result = result = result.concat(aux.getFragment().toLowerCase()
						+ "(");
				OWLRuleDObject var = (OWLRuleDObject) ((OWLRuleDataPropertyAtom) atom)
				.getFirstArgument();
				if (var instanceof OWLRuleDVariable) {
					URI aux1 = ((OWLRuleIVariable) var).getURI();
					result = result.concat("'$"
							+ aux1.getFragment().toUpperCase() + "',");
				} else {
					URI aux1 = ((OWLNamedObject) ((OWLRuleIndividual) var)
							.getIndividual()).getURI();
					result = result.concat(aux1.getFragment() + ",");
				}
				OWLRuleDObject var2 = ((OWLRuleDataPropertyAtom) atom)
				.getSecondArgument();
				if (var2 instanceof OWLRuleIVariable) {
					URI aux1 = ((OWLRuleIVariable) var2).getURI();
					result = result.concat("'$"
							+ aux1.getFragment().toUpperCase() + "')");
				} else {
					String aux1 = ((OWLRuleDataValue) var).toString();
					result = result.concat(aux1 + ")");
				}
				*/
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		result = result.concat(".\n");
		return result;
	}
	
	/**
	 * @param rule
	 */
	public void removeRule(RuleValue ruleValue, OWLRuleAtom consAtom ) {	
		
		HashSet rulesSet = new HashSet();
		OWLObject key = null;
		Set antecedents;
		Set consequents;
		try {
			antecedents = ruleValue.getRule().getAntecedents();
			consequents = ruleValue.getRule().getConsequents();
			if (consAtom instanceof OWLRuleClassAtom) {
				key = ((OWLRuleClassAtom) consAtom).getDescription();
			} else {
				if (consAtom instanceof OWLRuleDataPropertyAtom) {
					key = ((OWLRuleDataPropertyAtom) consAtom).getProperty();
				} else {
					if (consAtom instanceof OWLRuleObjectPropertyAtom) {
						key = ((OWLRuleObjectPropertyAtom) consAtom)
								.getProperty();
					}
				}
			}
			if (ruleMap.containsKey(key))
				rulesSet = (HashSet) ruleMap.get(key);			
			rulesSet.remove(ruleValue);
			ruleMap.put(key, rulesSet);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}

