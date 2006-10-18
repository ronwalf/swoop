/*
 * Created on Feb 19, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.automation;

import java.util.Hashtable;

/**
 * @author Dave Wang
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ExpressivityRanker 
{
	
	private static ExpressivityRanker myInstance;
	
	public Hashtable myComplexityRankTable;
	public Hashtable myRoughRankTable;
	
	public static ExpressivityRanker getInstance()
	{
		if ( myInstance == null )
			myInstance = new ExpressivityRanker();
		return myInstance;
	}
	
	private ExpressivityRanker()
	{
		myComplexityRankTable = new Hashtable();
		buildComplexityTable();
		buildRoughRankTable();
	}
	
	/* 1 - Polynomial
	 * 2 - PSpace-Complete
	 * 3 - ExpTime-Complete
 	 * 4 - NExpTime-Complete
	 */
	private void buildComplexityTable()
	{
		myComplexityRankTable.put( "EL",    "1");
		
		myComplexityRankTable.put( "ELI",   "2");
		myComplexityRankTable.put( "ELIH",  "2");		
		myComplexityRankTable.put( "ELIHF", "2"); // PSpace-hard
		
		myComplexityRankTable.put( "ELIF",  "3"); // ExpTime-C
		myComplexityRankTable.put( "ELU",   "3");
		myComplexityRankTable.put( "ELUI",  "3");
		myComplexityRankTable.put( "ELUIF", "3");
		myComplexityRankTable.put( "ELUIH", "3");
		
		myComplexityRankTable.put( "AL", "3");
		myComplexityRankTable.put( "ALC", "3");
		myComplexityRankTable.put( "ALCF", "3");
		myComplexityRankTable.put( "ALCI", "3");
		myComplexityRankTable.put( "ALCH", "3");
		myComplexityRankTable.put( "ALCN", "3");
		
		myComplexityRankTable.put( "ALCHON", "3");
		myComplexityRankTable.put( "ALCHOF", "3");
		myComplexityRankTable.put( "ALCIF", "3");
		myComplexityRankTable.put( "ALCOF", "3");
		myComplexityRankTable.put( "ALCIN", "3");
		myComplexityRankTable.put( "ALCHI", "3");
		myComplexityRankTable.put( "ALCON", "3");
		myComplexityRankTable.put( "ALCHO", "3");
		
		myComplexityRankTable.put( "ALCO", "3");
		myComplexityRankTable.put( "ALCHF", "3");
		myComplexityRankTable.put( "ALCHON", "3");
		myComplexityRankTable.put( "SHO", "3");
		myComplexityRankTable.put( "SHON", "3");
		
		myComplexityRankTable.put( "ALCHIF", "3");
		myComplexityRankTable.put( "ALCHIN", "3");
		
		myComplexityRankTable.put( "ALF", "3"); // bounded by ALCF
		myComplexityRankTable.put( "ALI", "3"); // bounded by ALCI
		myComplexityRankTable.put( "ALHI", "3"); // bounded by ALCHI
		myComplexityRankTable.put( "ALOF", "3"); // bounded by ALCOF
		myComplexityRankTable.put( "ALIF", "3"); // bounded by ALCIF
		myComplexityRankTable.put( "ALHF", "3"); // bounded by ALCHF
		myComplexityRankTable.put( "ALH", "3"); // bounded by ALCH
		myComplexityRankTable.put( "ALO", "3"); // bounded by ALCO
		myComplexityRankTable.put( "ALIN", "3"); // bounded by ALCIN
		
		myComplexityRankTable.put( "ALOIF", "4"); // bounded by ALCOIF
		myComplexityRankTable.put( "ALOIN", "4"); // bounded by ALCOIN
		myComplexityRankTable.put( "ALHON", "3"); // bounded by ALCHON
		
		myComplexityRankTable.put( "ALR+IF", "3"); // bounded by SIF
		myComplexityRankTable.put( "ALR+",   "3"); // bounded by S
		myComplexityRankTable.put( "ALR+H", "3"); // bounded by SH
		myComplexityRankTable.put( "ALR+HI", "3"); // bounded by SHI
		myComplexityRankTable.put( "ALR+HIF", "3"); // bounded by SHIF
		myComplexityRankTable.put( "ALR+HN", "3"); // bounded by SHN
		
		myComplexityRankTable.put( "S", "3");
		myComplexityRankTable.put( "SIF", "3");
		myComplexityRankTable.put( "SH", "3");
		myComplexityRankTable.put( "SHI", "3");
		myComplexityRankTable.put( "SHIF", "3");
		myComplexityRankTable.put( "SHIN", "3");
		myComplexityRankTable.put( "SHF", "3");
		myComplexityRankTable.put( "SF", "3");
		myComplexityRankTable.put( "SI", "3");

		myComplexityRankTable.put( "ALCOIF", "4");
		myComplexityRankTable.put( "ALCOIN", "4");
		myComplexityRankTable.put( "ALCHOIF", "4");
		myComplexityRankTable.put( "ALCHOIN", "4");
		myComplexityRankTable.put( "ALCOIF", "4");
		myComplexityRankTable.put( "ALCOIN", "4");
		myComplexityRankTable.put( "SHOIF", "4");
		myComplexityRankTable.put( "SHOIN", "4");
		myComplexityRankTable.put( "SOIF", "4");
		myComplexityRankTable.put( "SOIN", "4");
		
		myComplexityRankTable.put( "DL-Lite", "1");
		myComplexityRankTable.put( "RDFS(DL)", "1"); // polynomial by Bernardo		
	}
	
	/* Roughly group 
	 *   1. sub-boolean without union
	 *   2. sub-boolean with union to ACL
	 *   3. 
	 */
	private void buildRoughRankTable()
	{
		/*
		myRoughRankTable.put( "DL-Lite", "1");
		myRoughRankTable.put( "RDFS(DL)", "1");		

		myRoughRankTable.put( "EL",    "1");		
		myRoughRankTable.put( "ELI",   "1");
		myRoughRankTable.put( "ELIH",  "1");		
		myRoughRankTable.put( "ELIHF", "1"); 
		
		myRoughRankTable.put( "ELIF",  "2");
		myRoughRankTable.put( "ELU",   "2");
		myRoughRankTable.put( "ELUI",  "2");
		myRoughRankTable.put( "ELUIF", "2");
		myRoughRankTable.put( "ELUIH", "2");
		
		myRoughRankTable.put( "ALF", "2"); // bounded by ALCF
		myRoughRankTable.put( "ALI", "2"); // bounded by ALCI
		myRoughRankTable.put( "ALHI", "2"); // bounded by ALCHI
		myRoughRankTable.put( "ALIF", "2"); // bounded by ALCIF
		myRoughRankTable.put( "ALHF", "2"); // bounded by ALCHF
		myRoughRankTable.put( "ALH", "2"); // bounded by ALCH
		myRoughRankTable.put( "ALIN", "2"); // bounded by ALCIN
		myRoughRankTable.put( "ALOF", "2"); // bounded by ALCOF
		myRoughRankTable.put( "ALO", "2"); // bounded by ALCO

		myRoughRankTable.put( "ALR+IF", "2"); // bounded by SIF
		myRoughRankTable.put( "ALR+",   "2"); // bounded by S
		myRoughRankTable.put( "ALR+H",  "2"); // bounded by SH
		myRoughRankTable.put( "ALR+HI", "2"); // bounded by SHI
		myRoughRankTable.put( "ALR+HIF","2"); // bounded by SHIF
		myRoughRankTable.put( "ALR+HN", "2"); // bounded by SHN

		myRoughRankTable.put( "AL", "2");
		myRoughRankTable.put( "ALC", "2");
		
		myRoughRankTable.put( "ALCF", "3");
		myRoughRankTable.put( "ALCI", "3");
		myRoughRankTable.put( "ALCH", "3");
		myRoughRankTable.put( "ALCN", "3");		
		myRoughRankTable.put( "ALCIF", "3");
		myRoughRankTable.put( "ALCIN", "3");
		myRoughRankTable.put( "ALCHI", "3");
		myRoughRankTable.put( "ALCHF", "3");
		myRoughRankTable.put( "ALCHIF", "3");
		myRoughRankTable.put( "ALCHIN", "3");
		
		myRoughRankTable.put( "ALCHON", "3");
		myRoughRankTable.put( "ALCHOF", "3");
		myRoughRankTable.put( "ALCOF", "3");
		myRoughRankTable.put( "ALCO", "3");
		myRoughRankTable.put( "ALCON", "3");
		myRoughRankTable.put( "ALCHO", "3");
		myRoughRankTable.put( "ALCHON", "3");
	
		myRoughRankTable.put( "S", "4");
		myRoughRankTable.put( "SH", "4");
		myRoughRankTable.put( "SF", "4");
		myRoughRankTable.put( "SHF", "4");
		
		myRoughRankTable.put( "SI", "5");
		myRoughRankTable.put( "SHIF", "5");
		myRoughRankTable.put( "SHI",  "5");
		myRoughRankTable.put( "SIF",  "5");
		myRoughRankTable.put( "SHIN", "5");

		myRoughRankTable.put( "SHOIF", "6");
		myRoughRankTable.put( "SHOIN", "6");
		myRoughRankTable.put( "SOIF", "6");
		myRoughRankTable.put( "SOIN", "6");
		myRoughRankTable.put( "SHO", "6");
		myRoughRankTable.put( "SHON", "6");

		myRoughRankTable.put( "ALOIF", "4"); // bounded by ALCOIF
		myRoughRankTable.put( "ALOIN", "4"); // bounded by ALCOIN
		myRoughRankTable.put( "ALHON", "3"); // bounded by ALCHON
		myRoughRankTable.put( "ALCOIF", "4");
		myRoughRankTable.put( "ALCOIN", "4");
		myRoughRankTable.put( "ALCHOIF", "4");
		myRoughRankTable.put( "ALCHOIN", "4");
		myRoughRankTable.put( "ALCOIF", "4");
		myRoughRankTable.put( "ALCOIN", "4");
		*/

	}

	
	public String rankByRoughBin( String cExp )
	{
		// SHOIN
		if ( ( cExp.indexOf( "N" ) != -1 ) || ( cExp.indexOf( "O" ) != -1 ) )
			return "4";
		// SHIF
		else if ( ( cExp.indexOf( "I" ) != -1 ) || ( cExp.indexOf( "C" ) != -1 || (cExp.indexOf( "S" ) != -1) ))
			return "3";
		// ALHF
		else if ( ( cExp.indexOf( "H" ) != -1 ) || ( cExp.indexOf( "F" ) != -1 ) )
			return "2";
		// AL
		return "1";
	}
	
	public String rankByComplexity( String cExp )	
	{
		return (String)myComplexityRankTable.get( cExp );
	}
}

