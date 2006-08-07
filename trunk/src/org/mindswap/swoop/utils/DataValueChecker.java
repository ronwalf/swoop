package org.mindswap.swoop.utils;

import java.awt.Frame;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

import org.semanticweb.owl.impl.model.OWLConcreteDataTypeImpl;
import org.semanticweb.owl.io.vocabulary.RDFVocabularyAdapter;
import org.semanticweb.owl.io.vocabulary.XMLSchemaSimpleDatatypeVocabulary;

/**
 * @author Dave
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */

/*
 * 
 * 
 * @author Dave
 *
 * This class is used to check for type checkig.  That is, making sure users are entering
 * correct values for a particular data type.  This class contains static methods
 * for easy invocation.
 * 
 * 
 */
public class DataValueChecker {
	

	/**
	 * Pops an error message box in the owner Frame
	 */
	private static void popupError(Frame owner, String msg) 
	{
		JOptionPane.showMessageDialog(owner, msg, "Error", JOptionPane.ERROR_MESSAGE);
	}
	
	/**
	 *  Test to see if given String consists only alphabets
	 */
	private static boolean areAllAlphabets( String str )
	{
		for (int i = 0; i < str.length(); i++)
		{
			char c = str.charAt( i );
			if (!Character.isLetter(c))
				return false;
		}
		return true;
	}
	
	/**
	 *  Test to see if given String consists only alphanumerics
	 */
	private static boolean areAllAlphaNumeric( String str)
	{
		for (int i = 0; i < str.length(); i++)
		{
			char c = str.charAt( i );
			if (!Character.isLetterOrDigit(c))
				return false;
		}
		return true;
	} 
	
	/**
	 *  Test to see if given String consists only hex digits
	 */
	private static boolean isValidHexBinary(String value)
	{
		int permittedLength = 32;
		boolean flag = false;
		String sub = "";
		if (!(value.length() < permittedLength))
		{
			sub = value.substring(0, 32);
			value = value.substring(32);
		}
		else
			sub = value;
		flag = isValidBaseXBinary(sub, 16);
		if (!flag)
			return false;
		return true;
	}
	
	private static boolean isValidBaseXBinary(String value, int base)
	{
		try
			{
				long k = Long.parseLong(value, base);
			}
			catch (NumberFormatException e)
			{
				return false;
			}
		return true;
	}
	
	/**
	 *  Test to see if given String is a valid String tag defined in <http://www.ietf.org/rfc/rfc3066.txt>
	 */
	private static boolean isValidLanguage(String value)
	{
		StringTokenizer tokens = new StringTokenizer(value, "-/n");
		if (tokens.hasMoreTokens())
		{
	  		String first = tokens.nextToken();
			if ( first.length() > 8)
				return false;
			if (!areAllAlphabets(first) )
				return false;
			while ( tokens.hasMoreTokens() )
			{
				String token = tokens.nextToken();
				if (!areAllAlphaNumeric( token ) )
					return false;
			}	  		
	  	}
		else
			return false; // empty or "-" value		
		return true;
	}
	
	/**
	 * Check for the validity of the property-value pair based on the range of the 
	 * property and the data value specified by the user
	 * @param dt - XSD DataType (or rdfs:Literal)
	 * @param value - string value specified by user
	 * @return true (data value falls into the defined datatype range), false (otherwise)
	 */
	public static boolean isValidValue(Frame owner, OWLConcreteDataTypeImpl dt, String value) {
		return isValidValue( owner, dt.getURI(),  value);
	}
	
	public static boolean isValidValue(Frame owner, URI uri, String value) {
		boolean valid = false;
		String xsd = XMLSchemaSimpleDatatypeVocabulary.XS;
		String errorMsg = "Invalid Value for Specified Datatype - require ";
		
		//checking for xsd:anyURI
		if (uri.toString().equals(xsd+"anyURI")) {
			try
			{
				URI dummyURI = new URI( value );
				valid = true;
			}
			catch (URISyntaxException e)
			{
				valid = false;
				popupError(owner, errorMsg+"a valid absolute or relative URI");
			}
		}
		//checking for xsd:boolean
		else if (uri.toString().equals(xsd+"boolean")) {
			if (value.equals("true") || value.equals("false") ||
					value.equals("1") || value.equals("0") ) valid = true;
			else {
				valid = false;
				popupError(owner, errorMsg+"'true' or 'false'");
			}
		}
		//checking for xsd:base64Binary
		else if (uri.toString().equals(xsd+"base64Binary")) {
			valid = true; // todo: need to fix this!
		}
		//checking for xsd:byte
		else if (uri.toString().equals(xsd+"byte")) {
			try
			{
				byte b = Byte.parseByte( value );
				valid = true;
			}
			catch (NumberFormatException e)
			{
				popupError(owner, errorMsg+"byte value: [-127, 128]");
			}
		}
		//checking for xsd:date
		else if (uri.toString().equals(xsd+"date")) {
			// format: CCYY-MM-DD
			SimpleDateFormat df = new SimpleDateFormat();
			df.applyPattern("yyyy-MM-dd");
			df.setLenient(false);
			try {
				df.parse(value);
				valid = true;
			}
			catch (Exception e) {
				valid = false;
				popupError(owner, errorMsg+"format: YYYY-MM-DD");
			}
			return valid;			
		}
		//checking for xsd:dateTime
		else if (uri.toString().equals(xsd+"dateTime")) {
			// format: ['-'? yyyy '-' mm '-' dd 'T' hh ':' mm ':' ss ('.' s+)? (zzzzzz)?] 
			// ex: 1999-05-31T13:20:00.000
			// here we do a fixed (no-optional adoption)
			SimpleDateFormat df = new SimpleDateFormat();
			df.applyPattern("yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'SSS");
			df.setLenient(false);
			try {
				df.parse(value);
				valid = true;
			}
			catch (Exception e) {
				valid = false;
				popupError(owner, errorMsg+"format: YYYY-MM-DD'T'hh:mm:ss.sss  ex: 1999-05-31T13:20:00.000");
			}
			return valid;		
		}
		//checking for xsd:decimal or xsd:double
		else if (uri.toString().equals(xsd+"decimal") || uri.toString().equals(xsd+"double")) {
			try {
				double checkDouble = Double.parseDouble(value);
				valid = true;
			}
			catch (Exception e) {
				valid = false;
				popupError(owner, errorMsg+"double precision value");
			}
		}
		//checking for xsd:float
		else if (uri.toString().equals(xsd+"float")) {
			try {
				float checkFloat = Float.parseFloat(value);
				valid = true;
			}
			catch (Exception e) {
				valid = false;
				popupError(owner, errorMsg+"floating point value");
			}
		}
		//checking for xsd:gDay
		else if (uri.toString().equals(xsd+"gDay")) {
			// format: DD
			// ex: 31
			SimpleDateFormat df = new SimpleDateFormat();
			df.applyPattern("dd");
			df.setLenient(false);
			try {
				df.parse(value);
				valid = true;
			}
			catch (Exception e) {
				valid = false;
				popupError(owner, errorMsg+"format: DD");
			}
			return valid;	
		}
		//checking for xsd:gMonth
		else if (uri.toString().equals(xsd+"gMonth")) {
			// format: MM
			// ex: 05
			SimpleDateFormat df = new SimpleDateFormat();
			df.applyPattern("MM");
			df.setLenient(false);
			try {
				df.parse(value);
				valid = true;
			}
			catch (Exception e) {
				valid = false;
				popupError(owner, errorMsg+"format: MM    ex: 05");
			}
			return valid;	
		}
		//checking for xsd:gMonthDay
		else if (uri.toString().equals(xsd+"gMonthDay")) {
			// format: MM-DD
			// ex: 05-31
			SimpleDateFormat df = new SimpleDateFormat();
			df.applyPattern("MM'-'dd");
			df.setLenient(false);
			try {
				df.parse(value);
				valid = true;
			}
			catch (Exception e) {
				valid = false;
				popupError(owner, errorMsg+"format: MM-DD");
			}
			return valid;	
		}
		//checking for xsd:gYear
		else if (uri.toString().equals(xsd+"gYear")) {
			// format: yyyy
			// ex: 1999
			SimpleDateFormat df = new SimpleDateFormat();
			df.applyPattern("yyyy");
			df.setLenient(false);
			try {
				df.parse(value);
				valid = true;
			}
			catch (Exception e) {
				valid = false;
				popupError(owner, errorMsg+"format: YYYY");
			}
			return valid;	
		}
		//checking for xsd:gYearMonth
		else if (uri.toString().equals(xsd+"gYearMonth")) {
			// format: CCYY-MM
			// ex: 1999-05
			SimpleDateFormat df = new SimpleDateFormat();
			df.applyPattern("yyyy'-'MM");
			df.setLenient(false);
			try {
				df.parse(value);
				valid = true;
			}
			catch (Exception e) {
				valid = false;
				popupError(owner, errorMsg+"format: YYYY-MM");
			}
			return valid;	
		}
		// checking for xsd:hexBinary 
		else if (uri.toString().equals(xsd+"hexBinary")) {
			System.out.println("checking binary...? "+valid);
			valid = isValidHexBinary(value);
			System.out.println("hexBin: vaild? "+valid);
			if (!valid)
				popupError(owner, errorMsg+"binary in HEX (0-9,A,B,C,D,E,F)");
			return valid;
		}
		// checking for xsd:int
		else if (uri.toString().equals(xsd+"int") || uri.toString().equals(xsd+"integer")) {
			try {
				BigInteger Int = new BigInteger( value );
				valid = true;
			}
			catch (Exception e) {
				valid = false;
				popupError(owner, errorMsg+"an integer of decimal digits");
			}
		}
		// checking for xsd:language
		else if (uri.toString().equals(xsd+"int")) {
			valid = isValidLanguage( value );
			if (!valid)
				popupError(owner, errorMsg+"language tag.  See http://www.ietf.org/rfc/rfc3066.txt for details");

		}
		//checking for xsd:long
		else if (uri.toString().equals(xsd+"long")) {
			try {
				long checkInt = Long.parseLong(value);
				valid = true;
			}
			catch (Exception e) {
				valid = false;
				popupError(owner, errorMsg+"long number");
			}
		}
		//checking for xsd:Name
		else if (uri.toString().equals(xsd+"Name")) {
				valid = true;  // todo: allowing all strings, but need more restrictions. consult: http://www.w3.org/TR/2000/WD-xml-2e-20000814#NT-Name
		}
		//checking for xsd:normalizedString
		else if (uri.toString().equals(xsd+"normlizedString")) {
		// cannot contain line feed (10), carriage return (13), tab (9)
				if (( value.indexOf( 13 ) != -1 ) || ( value.indexOf( 10 ) != -1 ) || 
						 ( value.indexOf( 9 ) != -1 ) )
					valid = false;
				else 
					valid = true;
							if (!valid)
				popupError(owner, errorMsg+"valid normalizedString: see http://www.w3.org/TR/xmlschema-2/#normalizedString");
		}
		//checking for xsd:Name
		else if (uri.toString().equals(xsd+"NCName")) {
				valid = true;  // todo: allowing all strings, but need more restrictions. consult: http://www.w3.org/TR/1999/REC-xml-names-19990114/#NT-NCName
		}
		//checking for xsd:NMTOKEN
		else if (uri.toString().equals(xsd+"NMTOKEN")) {
				valid = true;  // todo: allowing all strings, but need more restrictions. consult: http://www.w3.org/TR/2000/WD-xml-2e-20000814#NT-Nmtoken
		}
		//checking for xsd:token
		else if (uri.toString().equals(xsd+"token")) 
		{
			// cannot contain line feed (10), carriage return (13), tab (9)
			if (( value.indexOf( 13 ) != -1 ) || ( value.indexOf( 10 ) != -1 ) || 
					 ( value.indexOf( 9 ) != -1 ) )
				valid = false;
			// cannot contain internal white space of length >1
			else if ( value.indexOf("  ") != -1)
				valid = false;
			// cannot contain leading/trailing white spaces
			else if ( !value.trim().equals(value ))
				valid = false;
			else
				valid = true;
			if (!valid)
				popupError(owner, errorMsg+"valid token: see http://www.w3.org/TR/xmlschema-2/#token");
		}
		//checking for xsd:negativeInteger
		else if (uri.toString().equals(xsd+"negativeInteger")) {
			try {
				int checkInt = Integer.parseInt(value);
				if (checkInt < 0) valid = true;
				else valid = false;
			}
			catch (Exception e) {
				valid = false;				
			}
			if (!valid) popupError(owner, errorMsg+"negative integer");
		}
		//checking for xsd:nonNegativeInteger
		else if (uri.toString().equals(xsd+"nonNegativeInteger")) {
			try {
				int checkInt = Integer.parseInt(value);
				if (checkInt>=0) valid = true;
				else valid = false;
			}
			catch (Exception e) {
				valid = false;				
			}
			if (!valid) popupError(owner, errorMsg+"non-negative integer");
		}
		//checking for xsd:nonPositiveInteger
		else if (uri.toString().equals(xsd+"nonPositiveInteger")) {
			try {
				int checkInt = Integer.parseInt(value);
				if (checkInt<=0) valid = true;
				else valid = false;
			}
			catch (Exception e) {
				valid = false;				
			}
			if (!valid) popupError(owner, errorMsg+"non-positive integer");
		}
		//checking for xsd:positiveInteger
		else if (uri.toString().equals(xsd+"positiveInteger")) {
			try {
				int checkInt = Integer.parseInt(value);
				if (checkInt>0) valid = true;
				else valid = false;
			}
			catch (Exception e) {
				valid = false;				
			}
			if (!valid) popupError(owner, errorMsg+"positive integer");
		}
		//checking for xsd:string (always valid)
		else if (uri.toString().equals(xsd+"string")) {
			valid = true;			
		}
		//checking for xsd:time
		else if (uri.toString().equals(xsd+"time")) {
			// format: hh:mm:ss.sss 
			SimpleDateFormat df = new SimpleDateFormat();
			df.applyPattern("hh:mm:ss.SSS");
			df.setLenient(false);
			try {
				df.parse(value);
				valid = true;
			}
			catch (Exception e) {
				valid = false;
				popupError(owner, errorMsg+"format: hh:mm:ss.sss");
			}
			return valid;
		}
		// checking xsd:ungisnedInt
		else if (uri.toString().equals(xsd+"unsignedInt")) {
			try 
			{
				BigInteger checkie = new BigInteger( value );
				if (checkie.compareTo( new BigInteger("4294967295")) == 1) // bigger than max value allowed
					valid = false;
				else if (checkie.compareTo( BigInteger.ZERO) == -1) // smaller than min value allowed
					valid = false;
				else
					valid = true;	
			}
			catch (Exception e) { // possibly NumberFormatException
				valid = false;			
			}
			if (!valid) 
				popupError(owner, errorMsg+"unsigned integer: [0, 4294967295]");
		}
		// checking xsd:ungisnedLong
		else if (uri.toString().equals(xsd+"ungisnedLong")) 
		{
			try 
			{
				BigInteger checkie = new BigInteger( value );
				if (checkie.compareTo( new BigInteger("18446744073709551615")) == 1) // bigger than max value allowed
					valid = false;
				else if (checkie.compareTo( BigInteger.ZERO) == -1) // smaller than min value allowed
					valid = false;
				else
					valid = true;	
			}
			catch (Exception e) { // possibly NumberFormatException
				valid = false;			
			}
			if (!valid) 
				popupError(owner, errorMsg+"unsigned integer: [0, 18446744073709551615]");
		}
		// checking xsd:ungisnedShort
		else if (uri.toString().equals(xsd+"ungisnedShort")) 
		{
			try 
			{
				Integer checkie = new Integer( value );
				if ((checkie.intValue() > 65535 ) || ( checkie.intValue() < 0 )) 
					valid = false;
				else
					valid = true;	
			}
			catch (Exception e) { // possibly NumberFormatException
				valid = false;			
			}
			if (!valid) 
				popupError(owner, errorMsg+"unsigned short: [0, 65535]");
		}
		// checking xsd:ungisnedShort
		else if (uri.toString().equals(xsd+"ungisnedByte")) 
		{
			try 
			{
				Integer checkie = new Integer( value );
				if ((checkie.intValue() > 255 ) || ( checkie.intValue() < 0 )) 
					valid = false;
				else
					valid = true;	
			}
			catch (Exception e) { // possibly NumberFormatException
				valid = false;			
			}
			if (!valid) 
				popupError(owner, errorMsg+"unsigned byte: [0, 255]");
		}
		
		//checking for RDFLiteral (always true)
		else if (uri.toString().equals(RDFVocabularyAdapter.RDF+"XMLLiteral")) {
			valid = true;
		}
		
		else if (!valid) popupError(owner, "We do not recognize this xsd:datatype");
		return valid;
	}
	
	public static boolean isGood(String dummy)
	{
		return isValidLanguage(dummy);
	}
	
	public static void main(String [] args) throws Exception
	{

		System.out.println( isGood("EN") );
		System.out.println( isGood("EN-US"));
		System.out.println( isGood("BIG-5"));
		System.out.println( isGood("EN1"));
		System.out.println( isGood("ENLISHIDK-US"));
		System.out.println( isGood("EN-us"));
		System.out.println( isGood("jp-75m,d"));
	}
}
