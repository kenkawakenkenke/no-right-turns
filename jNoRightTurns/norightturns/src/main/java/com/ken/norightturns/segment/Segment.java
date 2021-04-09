package com.ken.norightturns.segment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.ken.norightturns.ConnectionType;

import model.Coordinate;

public class Segment implements Serializable {
	private static final long serialVersionUID = 8238834004703266387L;
	
	final long parentWayID;
	public final long id;
	public final long[] nodes;
	public final double[] distancesToEnd;
	public Segment(long parentWayID, long id, List<Long> nodes, Map<Long, Coordinate> coordForID) {
		this.parentWayID = parentWayID;
		this.id = id;
		this.nodes = new long[nodes.size()];
		for(int i=0;i<nodes.size();i++) {
			this.nodes[i] = nodes.get(i);
		}
		distancesToEnd = generateDistMap(nodes, coordForID);
	}
	private static double[] generateDistMap(List<Long> nodes,Map<Long, Coordinate> coordForID ) {
		double[] distancesToEnd = new double[nodes.size()];
		for(int index = nodes.size()-2;index>=0;index--) {
			distancesToEnd[index] = distancesToEnd[index+1] + coordForID.get(nodes.get(index)).distanceWith(coordForID.get(nodes.get(index+1)));
		}
		return distancesToEnd;
	}
	
	public int indexOfNode(long nodeID) {
		for(int index = 0;index<nodes.length;index++) {
			if (nodeID == nodes[index]) {
				return index;
			}
		}
		throw new IllegalArgumentException("missing node: "+nodeID);
	}
	
	public long start() {return nodes[0];}
	public long end() {return nodes[nodes.length-1];}
//	public Node start() {return nodes.get(0);}
//	public Node end() {return nodes.get(nodes.size()-1);}
	
//	public Segment reverse(long id) {
//		return new Segment(parentWayID, id, ListUtil.reverse(nodes));
//	}
	
	private Collection<SegmentConnection> connections = new ArrayList<>();
	public void setConnectingSegments(Collection<SegmentConnection> connections) {
		this.connections = connections;
	}
	public Collection<SegmentConnection> connections() {
		return connections;
	}

	public static class SegmentConnection implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2435748261497597504L;
	public final ConnectionType type;
	public long nextSegmentID;
	public SegmentConnection(ConnectionType type, long nextSegmentID) {
		this.type = type;
		this.nextSegmentID = nextSegmentID;
	}
}

}