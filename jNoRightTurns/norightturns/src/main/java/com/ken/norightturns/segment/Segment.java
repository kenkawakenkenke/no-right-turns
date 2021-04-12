package com.ken.norightturns.segment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.ken.norightturns.export.DetailedSegment;
import com.ken.norightturns.export.MinimizedSegment;
import com.ken.norightturns.export.GridMap.CellID;

import model.Coordinate;

public class Segment implements Serializable {
	private static final long serialVersionUID = 8238834004703266387L;
	
	public final long parentWayID;
	public final int id;
	public final long[] nodes;
	public final double[] distancesToEnd;
	public Segment(long parentWayID, int id, List<Long> nodes, Map<Long, Coordinate> coordForID) {
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
	
	private Collection<SegmentConnection> connections = new ArrayList<>();
	public void setConnectingSegments(Collection<SegmentConnection> connections) {
		this.connections = connections;
	}
	public Collection<SegmentConnection> connections() {
		return connections;
	}
	
	public DetailedSegment toDetailedSegment() {
		return new DetailedSegment(id, nodes, distancesToEnd);
	}
}