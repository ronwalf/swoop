/*
 * Created on Oct 30, 2004
 *
 */
package org.mindswap.swoop.utils.ui;

import java.util.Comparator;

import org.mindswap.swoop.annotea.Description;

/**
 * @author Aditya
 *
 */
public class DescriptionComparator implements Comparator {
	
	public static DescriptionComparator INSTANCE = new DescriptionComparator();
	
	public int compare(Object o1, Object o2) {
		
		try {
			String date1 = ((Description) o1).getCreated();
			String date2 = ((Description) o2).getCreated();
			int cmp  = date2.compareTo(date1);
			if (cmp==0) return 1;
			else return cmp;
		}
		catch (Exception ex) {
			// if something fails compare thme as string
			return o1.toString().compareTo(o2.toString());
		}
	}

}
