package org.mindswap.swoop.renderer;


/**
 * @author Evren Sirin
 */
public interface SwoopEditableRenderer extends SwoopRenderer {	
	public void setEditorEnabled(boolean mode);	
	public boolean isEditableText();
}
