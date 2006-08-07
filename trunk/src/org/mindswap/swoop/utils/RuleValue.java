/*
 * Created on Jul 2, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.utils;

import org.semanticweb.owl.rules.OWLRule;


/**
 * @author ednaruckhaus
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class RuleValue {
		OWLRule rule;
		int express;
		public RuleValue(OWLRule r, int exp) {
			rule = r;
			express = exp;
		}
		
		/**
		 * @return Returns the express.
		 */
		public int getExpress() {
			return express;
		}
		/**
		 * @return Returns the rule.
		 */
		public OWLRule getRule() {
			return rule;
		}
	}

