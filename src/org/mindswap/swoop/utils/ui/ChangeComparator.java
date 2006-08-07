/*
 * Created on Mar 11, 2005
 *
 */
package org.mindswap.swoop.utils.ui;

import java.util.Comparator;

import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.change.SwoopChange;
import org.mindswap.swoop.change.TreeTableNode;

/**
 * @author Aditya
 *
 */
public class ChangeComparator implements Comparator {

	SwoopModel swoopModel;
	
	public static ChangeComparator INSTANCE = new ChangeComparator();
	
	public int compare(Object o1, Object o2) {
		
		if (o1 instanceof SwoopChange && o2 instanceof SwoopChange) {
			SwoopChange change1 = (SwoopChange) o1;
			SwoopChange change2 = (SwoopChange) o2;
			String time1 = change1.getTimeStamp();
			String time2 = change2.getTimeStamp();
			if (time1.equals(time2)) {
				if (change1.isCheckpointRelated()) return 1;
				if (change2.isCheckpointRelated()) return -1;
				return -1;
			}
			else return time1.compareTo(time2);
		}
		else if (o1 instanceof TreeTableNode && o2 instanceof TreeTableNode) {
			TreeTableNode node1 = (TreeTableNode) o1;
			TreeTableNode node2 = (TreeTableNode) o2;
			SwoopChange change1 = node1.swoopChange;
			SwoopChange change2 = node2.swoopChange;
			String time1 = change1.getTimeStamp();
			String time2 = change2.getTimeStamp();
			if (time1.equals(time2)) {
				if (change1.isCheckpointRelated()) return 1;
				if (change2.isCheckpointRelated()) return -1;
				return -1;
			}
			else return time1.compareTo(time2);
		}
		return -1;
	}
	
}
