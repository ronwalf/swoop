/*
 * Created on Oct 30, 2005
 *
 */
package org.mindswap.swoop.utils.ui;

import java.util.Comparator;

import org.mindswap.swoop.utils.Bookmark;

/**
 * @author Aditya
 *
 */
public class BookmarkComparator implements Comparator {

	public static BookmarkComparator INSTANCE = new BookmarkComparator();
	
	public int compare(Object b1, Object b2) {
		if (b1 instanceof Bookmark && b2 instanceof Bookmark) {
			return ((Bookmark) b1).getDisplayed_name().compareToIgnoreCase(((Bookmark) b2).getDisplayed_name());
		}
		return -1;
	}

}
