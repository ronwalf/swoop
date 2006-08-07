package org.mindswap.swoop.utils;

import java.awt.GridLayout;
import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.httpclient.HttpsURL;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.webdav.lib.WebdavFile;
import org.apache.webdav.lib.WebdavResource;

public class DavUtil {
	private static class StaticAuthenticator extends Authenticator {
		private String user;

		private String pass;

		public StaticAuthenticator(String username, String password) {
			user = username;
			pass = password;
		}

		protected PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(user, pass.toCharArray());
		}
	}

	private static class VisualAuthenticator extends Authenticator {
		protected PasswordAuthentication getPasswordAuthentication() {
			JTextField url = new JTextField();
			JTextField user = new JTextField();
			JTextField password = new JPasswordField();
			JPanel panel = new JPanel(new GridLayout(2, 2));
			panel.add(new JLabel("URL"));
			panel.add(url);
			panel.add(new JLabel("User"));
			panel.add(user);
			panel.add(new JLabel("Password"));
			panel.add(password);
			int option = JOptionPane.showConfirmDialog(null, new Object[] {
					"Host: " + getRequestingHost(),
					"Realm: " + getRequestingPrompt(), panel },
					"Authorization Required", JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.PLAIN_MESSAGE);
			if (option == JOptionPane.OK_OPTION) {
				return new PasswordAuthentication(user.getText(), 
						password.getText().toCharArray());
			} else {
				return null;
			}
		}
	}

	private static WebdavResource resourceIfExists(HttpURL httpURL) {
		try {
			return new WebdavResource(httpURL);
		} catch (Exception e) {
		}
		return null;
	}
	
	private static boolean putString(String data, String url, String username,
			String password) throws HttpException, IOException {
		HttpClient client = new HttpClient();
		Credentials credentials = new UsernamePasswordCredentials(username, password);
		client.getState().setCredentials(null, null, credentials);
		
		PutMethod method = new PutMethod(url);
		method.setRequestBody(data);
		int response = client.executeMethod(method);
		
		if (response >= 200 && response < 300) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean saveString(String data, String url)
			throws HttpException, URIException, IOException {
		return saveString(data, url, null, null);
	}

	public static boolean saveString(String data, String url, String username,
			String password) throws HttpException, URIException, IOException {
		HttpURL httpURL;
		if (url.startsWith("https:")) httpURL = new HttpsURL(url);
		else if (url.startsWith("http:")) httpURL = new HttpURL(url);
		else throw new URIException("Can only save to HTTP resources");
		
		String parentPath = null;
		HttpURL parentURL = null;
		WebdavResource parentResource = null;
		WebdavFile parentFile = new WebdavFile(httpURL);
		Vector pathStack = new Vector();

		if ((username == null) || (password == null) || (username.length() == 0) || (password.length() == 0)) {
			username = null;
			password = null;
		}
		
		// Try a plain PUT first
		if (putString(data, url, username, password)) {
			System.out.println("PUT succeeded");
			return true;
		}

		// Find existing parent resource
		while (parentResource == null) {
			parentPath = parentFile.getParent();
			System.out.println("Testing Parent: " + parentPath);
			if (parentPath == null)
				return false;
			parentURL = new HttpURL(httpURL, parentPath);
			if ((password != null) && (username != null)) {
				parentURL.setUserinfo(username, password);
			}
			parentResource = resourceIfExists(parentURL);

			parentFile = new WebdavFile(parentURL);
			pathStack.add(0, parentPath);
		}

		// Pop parent resource off the stack
		pathStack.remove(0);

		// Make parent directories
		for (Iterator iterator = pathStack.iterator(); iterator.hasNext();) {
			String path = (String) iterator.next();
			System.out.println("Making directory: " + path);
			if (!parentResource.mkcolMethod(path)) {
				System.out.println("Couldn't make directory.");
				parentResource.close();
				return false;
			}
		}

		// Put file in place
		if (!parentResource.putMethod(httpURL.getPath(), data)) {
			System.out.println("Couldn't save file.");
			parentResource.close();
			return false;
		}

		parentResource.close();
		return true;
	}
}
