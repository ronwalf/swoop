package org.mindswap.swoop.renderer;

import java.awt.Component;

import org.mindswap.swoop.SwoopDisplayPanel;


/**
 * @author Evren Sirin
 */
public interface SwoopRenderer {	
	public String getName();
	
	// panel is the SwoopDisplayPanel that contains the Component this method returns
	public Component getDisplayComponent( SwoopDisplayPanel panel );
}
