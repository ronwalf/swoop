package org.mindswap.swoop.racer;

import java.io.*;
import java.net.*;

/** This class tests the RACER socket client. It provides simple interaction through the console. The main
	method reads lisp-like expressions that are sent to RACER. It finishes when the string "(exit)"
	is introduced. */

public class TestRacerClient {
        static InputStream in = System.in;
        static PrintStream out = System.out;

/**
 * TestRacerClient constructor comment.
 */
public TestRacerClient() {
	super();
}
/** The main method reads parameters from the command line and performs a loop in which racer messages
	are read from the input stream and the output from racer is wrote in the output stream. */

public static void main(String[] argv) {
	String ip="127.0.0.1";
	int port=8088;
        int inPort=-1;
	if (argv.length==1 && (argv[0]=="-h" || argv[0]=="-help" || argv[0]=="-?")) {
		System.out.println("usage: java jracer.testRacerClient [ip [port]]");
		System.out.println("   where ip is the ip address of the RACER server (defaults to 127.0.0.1);");
		System.out.println("   and port is the ip port to which the RACER server listens (defaults to 8088)");
	}
	else {
		if (argv.length>0) ip=argv[0];
		if (argv.length>1) port=Integer.parseInt(argv[1]);
                if (argv.length>2) {
                  try {
                  inPort=Integer.parseInt(argv[2]);
                  Socket sock = new ServerSocket(inPort).accept();
                  in = sock.getInputStream();
                  out = new PrintStream(sock.getOutputStream());
                  System.out.println("Listening on port " + inPort);
                  } catch (IOException ex) {
                    System.out.println("Error, Quiting!!!");
                    System.exit(-1);
                  }
                }
		RacerSocketClient client=new RacerClient(ip,port);
		try {
			client.openConnection();
			System.out.print("> ");
			String racerCommand=readParenthesizedString(in),racerResult=null;
			while (!racerCommand.equalsIgnoreCase("(exit)")) {
				if (!racerCommand.equalsIgnoreCase("")) {
				try {
					racerResult=client.synchronousSend(racerCommand);
					out.println(racerResult);
				}
				catch (RacerException e) {
					out.println("Error: "+e.getMessage());
				}
				}
				System.out.print("> ");
				racerCommand=readParenthesizedString(in);
			}
			client.closeConnection();
		} catch (IOException e) {
			System.err.println("Input/output error: "+e.getMessage());
			try { client.closeConnection(); }
			catch (IOException ee) { System.err.println("unable to close the connection"); }
		}
	}
}
/** This method reads a string from the standard input. The string corresponds to a basurde frame. It starts with a set
	  of spaces or equivalent followed by a parenthesis and also ended by a parenthesis. Between both parenthesis
	  there can be slot definitions having also a begin and end parenthesis. */

private static String readParenthesizedString(InputStream in) throws IOException {
	boolean first=true;
	int np=0, b=0;
	String str=new String();
	char c;
	for(c=' ';c<=' ';c=(char)in.read());
	if (c!='(') {
	    out.println("Error: ignoring invalid input. Command must be enclosed in parentheses.");
//	    System.out.println("Type (exit) for quitting.");
	    for(b=0;b!=10;b=in.read());
	    return new String();
	}
	while (first || np>0) {
		first=false;
		if (c=='(') np++;
		if (c==')') np--;
		if (c>=' ') str=str+c;
		else str=str+' ';
		if (np!=0) c=(char)in.read();
	}
	return str;
}
}
