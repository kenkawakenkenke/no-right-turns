package com.ken.norightturns.segment;

import java.io.Serializable;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.ken.norightturns.ConnectionType;

public class SegmentConnection implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2435748261497597504L;
	public final ConnectionType type;
	public int nextSegmentID;
	public SegmentConnection(ConnectionType type, int nextSegmentID) {
		this.type = type;
		this.nextSegmentID = nextSegmentID;
	}
	public Map toJSONMap() {
		return ImmutableMap.of("t", type.ordinal(), "i", nextSegmentID);
	}
}