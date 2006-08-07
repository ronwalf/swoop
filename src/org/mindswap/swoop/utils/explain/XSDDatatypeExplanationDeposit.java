/*
 * Created on Feb 14, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.mindswap.swoop.utils.explain;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Hashtable;

import org.semanticweb.owl.io.vocabulary.XMLSchemaSimpleDatatypeVocabulary;
/**
 * @author Dave
 *
 * Deposit of XSD datatype explanations
 * 
 */
public class XSDDatatypeExplanationDeposit extends Hashtable implements DatatypeExplanationDeposit  
{
	private static URI XSDURI = null;
	private static XSDDatatypeExplanationDeposit myDeposit = null;
	
	private int myNumExplanations = 0;
	
	public static XSDDatatypeExplanationDeposit getInstance()
	{
		if (myDeposit == null)
		{
			try
			{
				XSDURI = new URI("http://www.w3.org/TR/xmlschema-2/");
				myDeposit = new XSDDatatypeExplanationDeposit();
			}
			catch (URISyntaxException e)
			{
				e.printStackTrace();
			}
		}
		return myDeposit;
	}
	
	private XSDDatatypeExplanationDeposit() throws URISyntaxException
	{
		URI [] uris = { XSDURI };
		
		
		String name = XMLSchemaSimpleDatatypeVocabulary.XS + "string";
		String [] examples = {"Ontology enginnering is fun!","hoot... hoot..."};
		add( new DatatypeExplanation( new URI( name ),  name, 
				"The string datatype represents character strings in XML. The value" +
				" space of string is the set of finite-length sequences of characters " +
				"(as defined in [XML 1.0 (Second Edition)]) that match the Char production" +
				" from [XML 1.0 (Second Edition)]. A character is an atomic unit of " +
				"communication; it is not further specified except to note that every " +
				"character has a corresponding Universal Character Set code point, which is" +
				" an integer.", uris, examples ));
		
		name = XMLSchemaSimpleDatatypeVocabulary.XS + "boolean";
		String [] ex_boolean = {"true","false", "1", "0"};
		add( new DatatypeExplanation( new URI( name ),  name, 
				"boolean has the value space required to support the mathematical concept " +
				"of binary-valued logic: {true, false}.", uris, ex_boolean ));
		
		name = XMLSchemaSimpleDatatypeVocabulary.XS + "decimal";
		String [] ex_decimals = {"-1.23", "12678967.543233", "+100000.00", "210"};
		add( new DatatypeExplanation( new URI( name ),  name, 
				"decimal represents a subset of the real numbers, which can be represented" +
				" by decimal numerals. The value space of decimal is the set of numbers that" +
				" can be obtained by multiplying an integer by a non-positive power of ten," +
				" i.e., expressible as i × 10^-n where i and n are integers and n >= 0." +
				" Precision is not reflected in this value space; the number 2.0 is not" +
				" distinct from the number 2.00. The order-relation on decimal is the" +
				" order relation on real numbers, restricted to this subset.", uris, ex_decimals));
		
		name = XMLSchemaSimpleDatatypeVocabulary.XS + "float";
		String [] ex_float = {"-1E4", "1267.43233E12", "12.78e-2", "12" , "-0", "0", "INF"};
		add( new DatatypeExplanation( new URI( name ),  name, 
				"float is patterned after the IEEE single-precision 32-bit floating point type [IEEE 754-1985]. The basic value space of float consists of the values m × 2^e, where m is an integer whose absolute value is less than 2^24, and e is an integer between -149 and 104, inclusive. In addition to the basic value space described above, the value space of float also contains the following three special values: positive and negative infinity and not-a-number (NaN). The order-relation on float is: x < y iff y - x is positive for x and y in the value space. Positive infinity is greater than all other non-NaN values. NaN equals itself but is incomparable with (neither greater than nor less than) any other value in the value space.", uris, ex_float));
		
		name = XMLSchemaSimpleDatatypeVocabulary.XS + "double";
		String [] ex_double = {"-1E4", "1267.43233E12", "12.78e-2", "12" , "-0", "0", "INF"};
		add( new DatatypeExplanation( new URI( name ),  name, 
				"The double datatype is patterned after the IEEE double-precision 64-bit floating point type [IEEE 754-1985]. The basic value space of double consists of the values m × 2^e, where m is an integer whose absolute value is less than 2^53, and e is an integer between -1075 and 970, inclusive. In addition to the basic value space described above, the value space of double also contains the following three special values: positive and negative infinity and not-a-number (NaN). The order-relation on double is: x < y iff y - x is positive for x and y in the value space. Positive infinity is greater than all other non-NaN values. NaN equals itself but is incomparable with (neither greater than nor less than) any other value in the value space.", uris, ex_double));
		
		name = XMLSchemaSimpleDatatypeVocabulary.XS + "dateTime";
		String []  ex_dateTime = {"2002-10-10T17:00:00Z", "2002-10-10T12:00:00-05:00"};
		add( new DatatypeExplanation( new URI( name ),  name, 
				"dateTime values may be viewed as objects with integer-valued year, month, day, hour and minute properties, a decimal-valued second property, and a boolean timezoned property. Each such object also has one decimal-valued method or computed property, timeOnTimeline, whose value is always a decimal number; the values are dimensioned in seconds, the integer 0 is 0001-01-01T00:00:00 and the value of timeOnTimeline for other dateTime values is computed using the Gregorian algorithm as modified for leap-seconds. The timeOnTimeline values form two related \"timelines\", one for timezoned values and one for non-timezoned values. Each timeline is a copy of the value space of decimal, with integers given units of seconds.", uris, ex_dateTime));
		
		name = XMLSchemaSimpleDatatypeVocabulary.XS + "time";
		String []  ex_time = {"3:20:00-05:00"};
		add( new DatatypeExplanation( new URI( name ),  name, 
				"time represnts an instant of time that recurs every day. The value space of time is the space of time of day values as defined in § 5.3 of [ISO 8601]. Specifically, it is a set of zero-duration daily time instances.", uris, ex_time));
		
		name = XMLSchemaSimpleDatatypeVocabulary.XS + "date";
		String []  ex_date = {"2002-10-10+13:00"};
		add( new DatatypeExplanation( new URI( name ),  name, 
				"The value space of date consists of top-open intervals of exactly one day in length on the timelines of dateTime, beginning on the beginning moment of each day (in each timezone), i.e. '00:00:00', up to but not including '24:00:00' (which is identical with '00:00:00' of the next day). For nontimezoned values, the top-open intervals disjointly cover the nontimezoned timeline, one per day. For timezoned values, the intervals begin at every minute and therefore overlap.", uris, ex_date));
		
		name = XMLSchemaSimpleDatatypeVocabulary.XS + "gYearMonth";
		String []  ex_gYM = {"1999-05"};
		add( new DatatypeExplanation( new URI( name ),  name, 
				"gYearMonth represents a specific gregorian month in a specific gregorian year. The value space of gYearMonth is the set of Gregorian calendar months as defined in § 5.2.1 of [ISO 8601]. Specifically, it is a set of one-month long, non-periodic instances e.g. 1999-10 to represent the whole month of 1999-10, independent of how many days this month has.", uris, ex_gYM));
		
		name = XMLSchemaSimpleDatatypeVocabulary.XS + "gYear";
		String []  ex_gY = {"1999", "2005", "1978", "-320"};
		add( new DatatypeExplanation( new URI( name ),  name, 
				"gYear represents a gregorian calendar year. The value space of gYear is the set of Gregorian calendar years as defined in § 5.2.1 of [ISO 8601]. Specifically, it is a set of one-year long, non-periodic instances e.g. lexical 1999 to represent the whole year 1999, independent of how many months and days this year has.", uris, ex_gY));
		
		name = XMLSchemaSimpleDatatypeVocabulary.XS + "gMonthDay";
		String []  ex_gMD = {"12-27+13:00", "12-27"};
		add( new DatatypeExplanation( new URI( name ),  name, 
				"gMonthDay is a gregorian date that recurs, specifically a day of the year such as the third of May. Arbitrary recurring dates are not supported by this datatype. The value space of gMonthDay is the set of calendar dates, as defined in § 3 of [ISO 8601]. Specifically, it is a set of one-day long, annually periodic instances.", uris, ex_gMD));
		
		name = XMLSchemaSimpleDatatypeVocabulary.XS + "gDay";
		String []  ex_gD = {"30", "05"};
		add( new DatatypeExplanation( new URI( name ),  name, 
				"gDay is a gregorian day that recurs, specifically a day of the month such as the 5th of the month. Arbitrary recurring days are not supported by this datatype. The value space of gDay is the space of a set of calendar dates as defined in § 3 of [ISO 8601]. Specifically, it is a set of one-day long, monthly periodic instances.", uris, ex_gD));
		
		name = XMLSchemaSimpleDatatypeVocabulary.XS + "gMonth";
		String []  ex_gM = {"12", "01"};
		add( new DatatypeExplanation( new URI( name ),  name, 
				"gMonth is a gregorian month that recurs every year. The value space of gMonth is the space of a set of calendar months as defined in § 3 of [ISO 8601]. Specifically, it is a set of one-month long, yearly periodic instances.", uris, ex_gM));
		
		name = XMLSchemaSimpleDatatypeVocabulary.XS + "hexBinary";
		String []  ex_hexBin = {"0FB7", "BC32"};
		add( new DatatypeExplanation( new URI( name ),  name, 
				"hexBinary represents arbitrary hex-encoded binary data. The value space of hexBinary is the set of finite-length sequences of binary octets. (Note that lower case [a-f] are not allowed)", uris, ex_hexBin));
		
		name = XMLSchemaSimpleDatatypeVocabulary.XS + "base64Binary";
		add( new DatatypeExplanation( new URI( name ),  name, 
				"base64Binary represents Base64-encoded arbitrary binary data. The value space of base64Binary is the set of finite-length sequences of binary octets. For base64Binary data the entire binary stream is encoded using the Base64 Alphabet in [RFC 2045].", uris, null));
		
		name = XMLSchemaSimpleDatatypeVocabulary.XS + "anyURI";
		String [] ex_anyURI = {"http://www.mindswap.org/2004/SWOOP/",  "http://www.w3.org/TR/xmlschema-2/#rf-length"};
		add( new DatatypeExplanation( new URI( name ),  name, 
				"anyURI represents a Uniform Resource Identifier Reference (URI). An anyURI value can be absolute or relative, and may have an optional fragment identifier (i.e., it may be a URI Reference). This type should be used to specify the intention that the value fulfills the role of a URI as defined by [RFC 2396], as amended by [RFC 2732].", uris, ex_anyURI));

		name = XMLSchemaSimpleDatatypeVocabulary.XS + "normalizedString";
		add( new DatatypeExplanation( new URI( name ),  name, 
				"normalizedString represents white space normalized strings. The value space of normalizedString is the set of strings that do not contain the carriage return (#xD), line feed (#xA) nor tab (#x9) characters. The lexical space of normalizedString is the set of strings that do not contain the carriage return (#xD), line feed (#xA) nor tab (#x9) characters. The base type of normalizedString is string.", uris, null));
		
		name = XMLSchemaSimpleDatatypeVocabulary.XS + "token";
		add( new DatatypeExplanation( new URI( name ),  name, 
				"token represents tokenized strings. The value space of token is the set of strings that do not contain the carriage return (#xD), line feed (#xA) nor tab (#x9) characters, that have no leading or trailing spaces (#x20) and that have no internal sequences of two or more spaces. The lexical space of token is the set of strings that do not contain the carriage return (#xD), line feed (#xA) nor tab (#x9) characters, that have no leading or trailing spaces (#x20) and that have no internal sequences of two or more spaces. The base type of token is normalizedString.", uris, null));
		
		name = XMLSchemaSimpleDatatypeVocabulary.XS + "language";
		add( new DatatypeExplanation( new URI( name ),  name, 
				"language represents natural language identifiers as defined by by [RFC 3066] . The value space of language is the set of all strings that are valid language identifiers as defined [RFC 3066] . The lexical space of language is the set of all strings that conform to the pattern [a-zA-Z]{1,8}(-[a-zA-Z0-9]{1,8})* . The base type of language is token.", uris, null));
		
		name = XMLSchemaSimpleDatatypeVocabulary.XS + "NMTOKEN";
		add( new DatatypeExplanation( new URI( name ),  name, 
				"TName represents XML Names. The value space of Name is the set of all strings which match the Name production of [XML 1.0 (Second Edition)]. The lexical space of Name is the set of all strings which match the Name production of [XML 1.0 (Second Edition)]. The base type of Name is token.", uris, null));
		
		name = XMLSchemaSimpleDatatypeVocabulary.XS + "Name";
		add(  new DatatypeExplanation( new URI( name ),  name, 
				"Name represents XML Names. The value space of Name is the set of all strings which match the Name production of [XML 1.0 (Second Edition)]. The lexical space of Name is the set of all strings which match the Name production of [XML 1.0 (Second Edition)]. The base type of Name is token.", uris, null));
		
		name = XMLSchemaSimpleDatatypeVocabulary.XS + "NCName";
		add(  new DatatypeExplanation( new URI( name ),  name, 
				"NCName represents XML \"non-colonized\" Names. The value space of NCName is the set of all strings which match the NCName production of [Namespaces in XML]. The lexical space of NCName is the set of all strings which match the NCName production of [Namespaces in XML]. The base type of NCName is Name.", uris, null));
		
		name = XMLSchemaSimpleDatatypeVocabulary.XS + "integer";
		String [] ex_integer= {"-1", "0", "12678967543233", "+100000"};
		add(  new DatatypeExplanation( new URI( name ),  name, 
				"integer is derived from decimal by fixing the value of fractionDigits to be 0and disallowing the trailing decimal point. This results in the standard mathematical concept of the integer numbers. The value space of integer is the infinite set {...,-2,-1,0,1,2,...}. The base type of integer is decimal.", uris, ex_integer));
		
		name = XMLSchemaSimpleDatatypeVocabulary.XS + "nonPositiveInteger";
		String [] ex_NPI = {"-1", "-5", "-10000", "-12678967543233"};
		add(  new DatatypeExplanation( new URI( name ),  name, 
				"nonPositiveInteger is derived from integer by setting the value of maxInclusive to be 0. This results in the standard mathematical concept of the non-positive integers. The value space of nonPositiveInteger is the infinite set {...,-2,-1,0}. The base type of nonPositiveInteger is integer.", uris, ex_NPI));
		
		name = XMLSchemaSimpleDatatypeVocabulary.XS + "negativeInteger";
		String [] ex_NI = {"1", "-12678967543233", "-100000"};
		add(  new DatatypeExplanation( new URI( name ),  name, 
				"negativeInteger is derived from nonPositiveInteger by setting the value of maxInclusive to be -1. This results in the standard mathematical concept of the negative integers. The value space of negativeInteger is the infinite set {...,-2,-1}. The base type of negativeInteger is nonPositiveInteger.", uris, ex_NI));
		
		name = XMLSchemaSimpleDatatypeVocabulary.XS + "long";
		String [] ex_long  = {"-1", "0", "12678967543233", "+100000"};
		add(  new DatatypeExplanation( new URI( name ),  name, 
				"long is derived from integer by setting the value of maxInclusive to be 9223372036854775807 and minInclusive to be -9223372036854775808. The base type of long is integer.  3.3.16.1 Lexical representation", uris, ex_long));
		
		name = XMLSchemaSimpleDatatypeVocabulary.XS + "int";
		String [] ex_int = {"-1", "0", "126789675", "+100000"};
		add(  new DatatypeExplanation( new URI( name ),  name, 
				"int is derived from long by setting the value of maxInclusive to be 2147483647 and minInclusive to be -2147483648. The base type of int is long.", uris, ex_int));
		
		name = XMLSchemaSimpleDatatypeVocabulary.XS + "short";
		String [] ex_short = {"-1", "0", "12678", "+10000"};
		add(  new DatatypeExplanation( new URI( name ),  name, 
				"short is derived from int by setting the value of maxInclusive to be 32767 and minInclusive to be -32768. The base type of short is int.", uris, ex_short));
		
		name = XMLSchemaSimpleDatatypeVocabulary.XS + "byte";
		String [] ex_byte = {"-1", "0", "126", "+100"};
		add(  new DatatypeExplanation( new URI( name ),  name, 
				"byte is derived from short by setting the value of maxInclusive to be 127 and minInclusive to be -128. The base type of byte is short.", uris, ex_byte));
		
		name = XMLSchemaSimpleDatatypeVocabulary.XS + "nonNegativeInteger";
		String [] ex_NNI = {"0", "1", "12678967543233", "+100000"};
		add(  new DatatypeExplanation( new URI( name ),  name, 
				"nonNegativeInteger is derived from integer by setting the value of minInclusive to be 0. This results in the standard mathematical concept of the non-negative integers. The value space of nonNegativeInteger is the infinite set {0,1,2,...}. The base type of nonNegativeInteger is integer.", uris, ex_NNI));
		
		name = XMLSchemaSimpleDatatypeVocabulary.XS + "unsignedLong";
		String [] ex_UL = {"0", "12678967543233", "100000"};
		add(  new DatatypeExplanation( new URI( name ),  name, 
				"unsignedLong is derived from nonNegativeInteger by setting the value of maxInclusive to be 18446744073709551615. The base type of unsignedLong is nonNegativeInteger", uris, ex_UL));
		
		name = XMLSchemaSimpleDatatypeVocabulary.XS + "unsignedInt";
		String [] ex_UI = {"0", "1267896754", "100000"};
		add(  new DatatypeExplanation( new URI( name ),  name, 
				"unsignedInt is derived from unsignedLong by setting the value of maxInclusive to be 4294967295. The base type of unsignedInt is unsignedLong.", uris, ex_UI));
		
		name = XMLSchemaSimpleDatatypeVocabulary.XS + "unsignedShort";
		String [] ex_US = {"0", "12678", "10000"};
		add(  new DatatypeExplanation( new URI( name ),  name, 
				"unsignedShort is derived from unsignedInt by setting the value of maxInclusive to be 65535. The base type of unsignedShort is unsignedInt.", uris, ex_US));
		
		name = XMLSchemaSimpleDatatypeVocabulary.XS + "unsignedByte";
		String [] ex_UB = {"0", "126", "100"};
		add(  new DatatypeExplanation( new URI( name ),  name, 
				"unsignedByte is derived from unsignedShort by setting the value of maxInclusive to be 255. The base type of unsignedByte is unsignedShort.", uris, ex_UB));
		
		name = XMLSchemaSimpleDatatypeVocabulary.XS + "positiveInteger";
		String [] ex_PI = {"1", "12678967543233", "+100000"};
		add( new DatatypeExplanation( new URI( name ),  name, 
				"positiveInteger is derived from nonNegativeInteger by setting the value of minInclusive to be 1. This results in the standard mathematical concept of the positive integer numbers. The value space of positiveInteger is the infinite set {1,2,...}. The base type of positiveInteger is nonNegativeInteger.", uris, ex_PI));
	}


	public void add(DatatypeExplanation exp)
	{
		super.put( exp.getURI(), exp );
		myNumExplanations++;
	}


	public DatatypeExplanation explain(URI uri) {

		return (DatatypeExplanation)super.get(uri);
	}


	public int numExplanations() {
		
		return myNumExplanations;
	}
}
