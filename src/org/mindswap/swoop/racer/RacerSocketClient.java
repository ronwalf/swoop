package org.mindswap.swoop.racer;

import java.io.*;
import java.net.*;


/** This class implements a racer client with a plain interface. It allows to establish a connection
	with the RACER server, and send to it plain string messages.
*/

public class RacerSocketClient {
        private boolean logClient=false;

	/** The socket that enables communication with the racer server. */

	private Socket racerSocket;


	/** The input stream from the RACER server socket. */

	protected InputStream racerInputStream;


	/** The output stream to the RACER server socket. */

	private PrintStream racerOutputStream;


	/** The IP location where the racer server is located. */

	private String racerServerIP;


	/** The port used by the racer server. */

	private int racerServerPort;


	/** This is the string for letting know the racer server the process has ended. */

	private static final String SERVER_END_STRING = ":eof";


	/** This int states the synchronous mode in which RACER server will be accessed. It may have three
	different values: SYNCHRONOUS, ASYNCHRONOUS, and MIXED. In within the first mode, the RACER server
	is accessed synchronously for every access, in the second mode it is accessed asynchronously for
	every access; and in the thrid mode, the RACER server is accessed synchronously for those accessed
	that wait for an answer (ask something to the RACER system); and asynchronously for those accessed
	that do not return any answer (typically, TBox and ABox modification RACER primitives).
	When the RACER server is accessed asynchronously, the method waitAsynchronousMessages can be executed
	to wait for all the messages to be processed. */

//	private int synchronousMode=RacerConstants.MIXED_SYNCHRONOUS_MODE;
	private int synchronousMode=RacerConstants.SYNCHRONOUS_MODE;


	/** The number of asynchronous messages that have been sent to the RACER server and for which
	we have not yet read the answer. */

	private int numberAsynchronousMessages=0;

/** This method builds a new racer client. */

public RacerSocketClient(String ip,int port) {
	super();
	racerServerIP=ip;
	racerServerPort=port;
}
/** This method sends a command to the RACER server and returns immediatly. It does not wait for a
	response. Anyway, it can be useful when sending a lot of racer messages in order to create a TBox
	for example. The combination of this method and the synchronized send method may interfere; the user
	is responsible to use the methods in a proper way. See also the waitRacer method. */

public void setLogClient(boolean b) {
        logClient = b;
        System.out.println("Logging started? " + b);
}

public void asynchronousSend(String command) throws IOException {
	printMessageIntoSocket(command);
	numberAsynchronousMessages++;
}
/** This method tries to establish a connection with the racer server. If there is any problem, an
	IOException is thrown. */

public void closeConnection() throws IOException {
	if (racerOutputStream!=null) {
		racerOutputStream.print(SERVER_END_STRING);
		racerOutputStream.flush();
		racerInputStream.close();
		racerInputStream=null;
		racerOutputStream.close();
		racerOutputStream=null;
	}
	if (racerSocket!=null) {
		racerSocket.close();
		racerSocket=null;
	}
}
// ADDED RC20020322
public boolean isConnected() {
  if (racerOutputStream == null) return false;
  if (racerInputStream == null) return false;
  if (racerSocket == null) return false;
  return true;
}

/** This method returns the current synchronous mode.
 */

public int getSynchronousMode() {
	return synchronousMode;
}
/** This method tries to establish a connection with the racer server. If there is any problem, an
	IOException is thrown. */

public void openConnection() throws IOException {
	racerSocket=new Socket(racerServerIP,racerServerPort);
	racerInputStream=racerSocket.getInputStream();
	OutputStream out=racerSocket.getOutputStream();
	racerOutputStream=new PrintStream(out,true);
}
/** This method parses the result obtained from RACER. In this class the result is returned as it is
	obtained from RACER. This behaviour may be changed for derived classes (and it is redefined in
	RacerClient class). The method may throw a RacerException. */

protected String parseResult(String message,String result) throws RacerException {
	return result;
}
/** This method prints the message into the socket corresponding to the RACER server. It can be redefined
	in derived classes in order to add some behaviour (for example, writing a log file).
 * @param command java.lang.String
 */

protected void printMessageIntoSocket(String command) {
  if (logClient) System.out.println(command);
	racerOutputStream.println(command);
        racerOutputStream.flush();
}
/** This method prints a warning message. The default behaviour is to print it in the console. But
	the method can be redefined in order to print it to other streams.
 * @param warning java.lang.String
 */
protected void printWarning(String warning) {
//	for(int i=0;i<warning.length();i++) {
//		char c=warning.charAt(i);
//		if (c==(char)9) System.out.println();
//		else System.out.print(warning.charAt(i));
//	}
//	System.out.println();
}
/** This method reads a string from the racer socket connection. */

String readFromSocket(InputStream in) throws IOException {
        StringBuffer str = new StringBuffer(131072);
	int c=in.read();
        str.append((char) c);
	while (c!=10) {
		c=in.read();
		if (c!=10) {
          str.append((char) c);
        }
	}
        String result=str.toString();
        if (logClient) System.out.println(result);
	return result;
}
/** This method replaces the ':' characters that are inside a name by '%' if it appears in the string.
	If the character ':' does not appear, the original string is returned.
 * @return java.lang.String
 * @param str java.lang.String
 */
protected String replaceSemicolon(String str) {
  if (str.indexOf(':') == -1) return str;
  boolean inBars = false;
  for (int i = 0 ; i < str.length(); i++) {
    if (str.charAt(i) == '|') inBars = ! inBars;
    if (!inBars) {
      if ((str.charAt(i) == ':') && (str.charAt(i-1) >  ' ')) {
        str = str.substring(0,i).concat("%").concat(str.substring(i+1));
      }
    }
  }
  return str;
}
//protected String replaceSemicolon(String str) {
//	String res=null;
//	int ini1=0,ini2=0,fi=0;
//	while (fi!=-1) {
//		fi=str.indexOf(':',ini2);
//		if (fi!=-1 && fi>0 && str.charAt(fi-1)>' ') {
//			if (res==null) res=str.substring(ini1,fi)+"%";
//			else res=res+str.substring(ini1,fi)+"%";
//			ini1=fi+1;
//		}
//		ini2=fi+1;
//	}
//	if (res!=null) res=res+str.substring(ini1);
//	return res==null ? str : res;
//}

/** This method sends a command to the RACER server and returns a string with the answer. In what refers
	to synchronicity with the RACER server, this method acts as if the command is a query (that does only
	affects the mixed synchronicity mode).<p>
	If theserver returns an ":ok" message, the answer is the null String; if the racer server returns an
	":answer" message, the returned value is the String corresponding to the answer; and finally, if the
	racer server returns an ":error" message, a RacerException is thrown. */

public synchronized String send(String command) throws RacerException, IOException {
	return send(command,true);
}
/** This method sends a command to the RACER server and returns a string with the answer. If the racer
	server returns an ":ok" message, the answer is the null String; if the racer server returns an
	":answer" message, the returned value is the String corresponding to the answer; and finally, if the
	racer server returns an ":error" message, a RacerException is thrown.<p>
	The second parameter states whether the command is a RACER query (an answer is expected) or not. This
	is only taken into account for the synchronicity mode when the mode corresponds to the mixed one. */

protected synchronized String send(String command,boolean isQuery) throws RacerException, IOException {
	String result=null;
	// all characters ':' inside a name are replaced by characters '&', as the presence of ':' causes
	// the interface to hang because of lisp packages interference
//	command=replaceSemicolon(command);
	if (synchronousMode==RacerConstants.SYNCHRONOUS_MODE) result=synchronousSend(command);
	else {
		asynchronousSend(command);
		if (synchronousMode==RacerConstants.MIXED_SYNCHRONOUS_MODE && isQuery)
			result=waitAsynchronousMessages();
	}
	return result;
}

public synchronized String flush() throws RacerException, IOException {
        racerSocket.setSoTimeout(1000);
        String result;
        try {
	  result=readFromSocket(racerInputStream);
        } catch (IOException ex) {
          racerSocket.setSoTimeout(0);
          throw ex;
        }
	return parseResult("(flush)",result);
}

/** This method sets the synchronous mode in which the RACER server will be accessed.
 * @param sm boolean If true, the RACER server will be accessed synchronously; otherwise, it will
   be accessed asynchronously. If the server was accessed asynchronously (or mixed) and the mode
   is set to synchronous, it is necessary to call the waitAsynchronousMessages before this is
   done.
 */

public void setSynchronousMode(int sm) {
	synchronousMode=sm;
}
/** This method sends a command to the RACER server and returns a string with the answer. If the racer
	server returns an ":ok" message, the answer is the null String; if the racer server returns an
	":answer" message, the returned value is the String corresponding to the answer; and finally, if the
	racer server returns an ":error" message, a RacerException is thrown. */

public synchronized String synchronousSend(String command) throws RacerException, IOException {
	printMessageIntoSocket(command);
	String result=readFromSocket(racerInputStream);
	return parseResult(command,result);
}
/** This method waits until all the messages that have been sent to RACER in an asynchronous way are
	processed. If any of them is an error message, the corresponding exception is thrown. The last
	message is also returned (but only the last message!). */

public String waitAsynchronousMessages() throws RacerException, IOException {
	String result=null;
	while (numberAsynchronousMessages>0) {
		result=readFromSocket(racerInputStream);
		numberAsynchronousMessages--;
		result=parseResult(null,result);
	}
	return result;
}
}
