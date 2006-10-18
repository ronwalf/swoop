/*
 * Created on Jun 25, 2004
 *
 */
package org.mindswap.swoop.utils.ui;

import java.io.IOException;

public class BrowserControl {
    /**
     * Display a file in the system browser.  If you want to display a
     * file, you must include the absolute path name.
     *
     * @param url the file's url (the url must start with either "http://"
or
     * "file://").
     */
	
	 // Used to identify the windows platform.
    private static final String WIN_ID = "Windows";
    // The default system browser under windows.
    private static final String WIN_PATH = "rundll32";
    // The flag to display a url.
    private static final String WIN_FLAG = "url.dll,FileProtocolHandler";
    // The default browser under unix.
    private static final String UNIX_PATH = "netscape";
    // The flag to display a url.
    private static final String UNIX_FLAG = "-remote openURL";
    
    public static void displayURL(String url)
    {
        boolean windows = isWindowsPlatform();
        String cmd = null;
        try
        {
            if (windows)
            {
                // cmd = 'rundll32 url.dll,FileProtocolHandler http://...'
                cmd = WIN_PATH + " " + WIN_FLAG + " " + url;
                Process p = Runtime.getRuntime().exec(cmd);
            }
            else 
            {
            
                // Under Unix, Netscape has to be running for the "-remote"
                // command to work.  So, we try sending the command and
                // check for an exit value.  If the exit command is 0,
                // it worked, otherwise we need to start the browser.
                // cmd = 'netscape -remote openURL(http://www.javaworld.com)'
//                cmd = UNIX_PATH + " " + UNIX_FLAG + "(" + url + ")";
//                Process p = Runtime.getRuntime().exec(cmd);
//                try
//                {
                    // wait for exit code -- if it's 0, command worked,
                    // otherwise we need to start the browser up.
//                    int exitCode = p.waitFor();
//                    if (exitCode != 0)
//                    {
                        // Command failed, start up the browser
                        // cmd = 'netscape http://www.javaworld.com'
                        cmd = UNIX_PATH + " "  + url;
                        try {
                        	Process p = Runtime.getRuntime().exec(cmd);
                        }
                        catch (Exception e) {
                        	System.out.println("Error running command: "+cmd);
                        }
                        try {
                        	Process p = Runtime.getRuntime().exec("open "+url);
                        }
                        catch (Exception e) {
                        	System.out.println("Error running command: open "+url);
                        }
//                    }
//                }
//                catch(InterruptedException x)
//                {
//                    System.err.println("Error bringing up browser, cmd='" +
//                                       cmd + "'");
//                    System.err.println("Caught: " + x);
//                }
//            }
            }
        }
        catch(IOException x)
        {
            // couldn't exec browser
            System.err.println("Could not invoke browser, command=" + cmd);
            System.err.println("Caught: " + x);
        }
    }
    /**
     * Try to determine whether this application is running under Windows
     * or some other platform by examing the "os.name" property.
     *
     * @return true if this application is running under a Windows OS
     */
    public static boolean isWindowsPlatform()
    {
        String os = System.getProperty("os.name");
        if ( os != null && os.startsWith(WIN_ID))
            return true;
        else
            return false;

    }
}
