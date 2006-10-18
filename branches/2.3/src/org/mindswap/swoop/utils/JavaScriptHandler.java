/*
 * Created on 2005-6-10
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.utils;

/**
 * @author Zhao Bin
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
import javax.swing.JLabel;
import javax.swing.JComponent;
import com.holub.ui.HTML.TagHandler;
import com.holub.ui.HTML.HTMLPane;
import java.util.Properties;
import java.util.Enumeration;
import java.util.List;
import java.util.ArrayList;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Function;

public class JavaScriptHandler implements TagHandler {
    public static final String VERIFY_FUN_NAME = "verify";
    public static final String GEN_N3_FUN_NAME = "generateN3";
    public static final String N3_PREFIX = "n3_";
    public static final String ERR_MSG = "errorMessage";
    public static final String NUM_OF_N3 = "numberOfN3";
    public static final int MAX_N3 = 30;
    Context cx = null;
    Scriptable scope = null;
    
	public JComponent handleTag( HTMLPane source, final Properties attributes ){
	    //System.out.println("Javascript code is [" + attributes.get("code") + "]");
	
	    cx = Context.enter();
	    // Initialize the standard objects (Object, Function, etc.)
	    // This must be done before scripts can be executed. Returns
	    // a scope object that we use in later calls.
	    scope = cx.initStandardObjects();
	    
	    // Now evaluate the string we've colected.
	     
	    cx.evaluateString(scope, (String)attributes.get("code"), "<cmd>", 1, null);

	    return new JLabel("<javascript>");
	}
	
	public void clear(){
	    cx = null;
	    scope = null;
	}
	
	public boolean verify(String formName, Properties formData){
	    
	    if (scope == null){
	        return true;
	    }
	    
	    Enumeration e = formData.propertyNames();
	    String result = null;
	    while(e.hasMoreElements()) {
	        String dataName = (String)e.nextElement();
	        System.out.println(formName + "_" + dataName);
	        if (formName != null){
	            // don't put the formName in fron of the data name
	          //scope.put(formName + "_" + dataName, scope, formData.getProperty(dataName));
	            scope.put(dataName, scope, formData.getProperty(dataName));
	        } else {
              scope.put(dataName, scope, formData.getProperty(dataName));
	        }
	     }
	
	    Object verifyObj = scope.get(VERIFY_FUN_NAME, scope);
	
	    if (! (verifyObj instanceof Function)){
	      System.out.println("verify function is not found");
	    } else {
	      Function verifyFun = (Function)verifyObj;
	      result = Context.toString(verifyFun.call(cx, scope, scope, null));
	    }
	    
         //  result can be null,
	    if (result == null){
	        return true;
	    }
	    
	    // result can be "undefined", "true", "false"
	    if (result.equalsIgnoreCase("true") || result.equalsIgnoreCase("false")){
	        return Boolean.valueOf(result).booleanValue();
	    } else {
	        return true;
	    }
	}
	
	public String[] generateN3(){
	    if (scope == null){
	        return new String[0];
	    }
	    List n3List = new ArrayList();
	    Object genObj = scope.get(GEN_N3_FUN_NAME, scope);
	    
	    if (! (genObj instanceof Function)){
	      System.out.println("generateN3 function is not found");
	    } else {
	      Function genFun = (Function)genObj;
	      Object result = genFun.call(cx, scope, scope, null);
	      System.out.println("genN3 result: " + Context.toString(result));
	      for (int i=0;i<getNumberOfN3();i++){
	        String genN3 = N3_PREFIX + i;
	        Object n3 = scope.get(genN3, scope);
	        if (n3 == Scriptable.NOT_FOUND){
	          System.out.println(genN3 + " is not defined");
	        } else {
	          System.out.println(genN3 + " is " + Context.toString(n3));
	          n3List.add(Context.toString(n3));
	        }
	      }
	    }
	    return (String[]) n3List.toArray(new String[0]);
	}
	
	public String getErrorMsg(){
	  if (scope == null){
	      return "Not error message is defined";
	  } else {
	      Object msg = scope.get(ERR_MSG, scope);
	      if (msg == Scriptable.NOT_FOUND){
	          return "Not error message is defined";
	      } else {
	          return Context.toString(msg);
	      }
	  }
	}
	
	private int getNumberOfN3(){
	    if (scope == null){
		      return MAX_N3;
		  } else {
		      Object msg = scope.get(NUM_OF_N3, scope);
		      if (msg == Scriptable.NOT_FOUND){
		          return MAX_N3;
		      } else {
		          String s = Context.toString(msg);
		          try{
		              return Integer.parseInt(s);
		          } catch (Exception e){
		              return MAX_N3;
		          }
		      }
		  }
	}
}
