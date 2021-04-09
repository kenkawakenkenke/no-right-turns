package com.ken.norightturns.osm;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class Way implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6590649586137947909L;
	public final long id;
	public List<Long> nodes;
	public Map<String, String> tags;
	public final String highwayType;
	public final boolean isOneWay;

	public Way(long id, List<Long> nodes, Map<String, String> tags, String highwayType, boolean isOneWay) {
		this.id = id;
		this.nodes = nodes;
		this.tags = tags;
		this.highwayType = highwayType;
		this.isOneWay = isOneWay;
	}
}