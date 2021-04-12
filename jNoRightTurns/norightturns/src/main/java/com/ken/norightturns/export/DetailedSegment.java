package com.ken.norightturns.export;

import java.io.Serializable;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;

public class DetailedSegment implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5805798481438918344L;

	public final int id;
	public final long[] nodes;
	public final double[] distancesToEnd;
	
	public DetailedSegment(int id, long[] nodes, double[] distancesToEnd) {
		this.id = id;
		this.nodes = nodes;
		this.distancesToEnd = distancesToEnd;
	}
	
	public Optional<Integer> indexOfNode(long node) {
		for(int i=0;i<nodes.length;i++) {
			if (nodes[i] == node) {
				return Optional.of(i);
			}
		}
		return Optional.empty();
				
	}

	public Object toJSONMap() {
		return ImmutableMap.of(
				"id",id,
				"ns", nodes,
				"ds", distancesToEnd);
	}
	
}
