//The MIT License
//
// Copyright (c) 2004 Mindswap Research Group, University of Maryland, College Park
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to
// deal in the Software without restriction, including without limitation the
// rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
// sell copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
// IN THE SOFTWARE.

package org.mindswap.swoop;

/**
 * @author Evren Sirin
 */
public class ModelChangeEvent {
	private SwoopModel model;
	
	private int type;
	private Object source;
	
	// different constants to represent what kind of change happened 
	// in the model
	public final static int IMPORTS_VIEW_CHANGED = 1;
	public final static int QNAME_VIEW_CHANGED   = 2;
	public final static int ONTOLOGY_LOADED 	   = 3;
	public final static int ONTOLOGY_REMOVED 	   = 4;
	public final static int ONTOLOGY_CHANGED 	   = 5;
	public final static int ONTOLOGY_SEL_CHANGED = 6;
	public final static int ENTITY_SEL_CHANGED   = 7;
	public final static int REASONER_SEL_CHANGED = 8;
	public final static int ADDED_ENTITY = 9;
	public final static int REMOVED_ENTITY = 10;
	public final static int EDITABLE_CHANGED = 11;
	public final static int DEBUGGING_CHANGED = 12;
	public final static int SHOW_INHERITED = 13;
	public final static int ADDED_CHECKPOINT = 14;
	public final static int REMOVED_CHECKPOINT = 15;
	public final static int ADDED_CHANGE= 16;
	public final static int RESET_CHANGE= 17;
	public final static int ENABLED_CHANGELOG= 18;
	public final static int ENABLED_CHECKPOINT= 19;
	public final static int CLEAR_SELECTIONS = 20;
	public final static int AUTORETRIEVE_CHANGED = 21;
	public final static int FILTER_SEL_CHANGED = 22;
	public final static int ANNOTATION_CACHE_CHANGED = 23;
	public final static int ONTOLOGY_RELOADED = 24;
	public final static int FONT_CHANGED = 25;
	public final static int ONT_STATS_CHANGED = 26;
	public final static int REASONER_FAIL = 27;
	public final static int MOTHERSHIP_DISPLAY = 28;
	public final static int RULES_CHANGED = 29;

	/**
	 * 
	 */
	public ModelChangeEvent(SwoopModel model, int type) {
		this(model, type, null);
	}
	
	/**
	 * 
	 */
	public ModelChangeEvent(SwoopModel model, int type, Object source) {
		this.model = model;
		this.type = type;
		this.source = source;
	}
	
	/**
	 * @return Returns the model.
	 */
	public SwoopModel getModel() {
		return model;
	}

	/**
	 * @return Returns the source.
	 */
	public Object getSource() {
		return source;
	}

	/**
	 * @return Returns the type.
	 */
	public int getType() {
		return type;
	}

}
