/*
 * Created on Jan 4, 2006
 *
 */
package org.mindswap.swoop;

import java.io.File;
import java.util.TimerTask;

/**
 * @author Aditya
 *
 */
public class SwoopReminder extends TimerTask {

	SwoopFrame swoopHandler;
	SwoopModel swoopModel;
	
	public SwoopReminder(SwoopModel model, SwoopFrame handler) {
		this.swoopModel = model;
		this.swoopHandler = handler;
	}
	
	public void run() {
		
		// save workspace automatically if option is selected
		if (swoopModel.isAutoSaveWkspc()) {
			try {
				String fname = swoopModel.getSaveWkspcFile();
				File tmp = swoopHandler.wkspcFile;
				swoopHandler.wkspcFile = new File(fname);
				swoopHandler.saveWorkspace(false, false);
				swoopHandler.wkspcFile = tmp;
				System.out.println("Auto-Saved Workspace at "+fname+" (" + swoopModel.getTimeStamp()+")");
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}
