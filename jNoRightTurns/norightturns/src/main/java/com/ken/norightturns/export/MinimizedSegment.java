package com.ken.norightturns.export;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.ken.norightturns.export.GridMap.CellID;
import com.ken.norightturns.segment.Segment;
import com.ken.norightturns.segment.SegmentConnection;

import model.Coordinate;

public class MinimizedSegment implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8467996270884743307L;
	
	// A cell where you can get corresponding details from.
	public final CellID cellID;
	public final double distance;
	public final Collection<SegmentConnection> connections;
	
	public MinimizedSegment(CellID cellID, double distance, Collection<SegmentConnection> connections) {
		this.cellID = cellID;
		this.distance = distance;
		this.connections = connections;
	}

	public Map toJSONMap() {
		Map<String, Object> map =new HashMap<>();
		map.put("c", cellID.toString());
		map.put("d", distance);
		map.put("cs", connections.stream().map(con -> con.toJSONMap()).collect(ImmutableList.toImmutableList()));
		return map;
	}
	
	public static List<MinimizedSegment> toMinimizedSegments(GridMap gridMap, Map<Long, Coordinate> coordForID, List<Segment> segments) {
		return segments.stream()
				.map(segment -> {
					Coordinate headCoord = coordForID.get(segment.nodes[0]);
					CellID cellID = gridMap.cellIDFor(headCoord);
					return new MinimizedSegment(cellID, segment.distancesToEnd[0], segment.connections());
				})
				.collect(ImmutableList.toImmutableList());
	}
}
