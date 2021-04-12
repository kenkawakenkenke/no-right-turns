package com.ken.norightturns.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.ken.norightturns.ConnectionType;
import com.ken.norightturns.export.DetailedSegment;
import com.ken.norightturns.export.GridMap;
import com.ken.norightturns.export.GridMap.CellID;
import com.ken.norightturns.export.GridMap.GridCell;
import com.ken.norightturns.export.MinimizedSegment;
import com.ken.norightturns.segment.SegmentConnection;

import common.ds.Tuple;
import model.Coordinate;

public class ShortestPathSearcher {

	private static double connectionWeight(ConnectionType connection, SearchStrategy strategy) {
		switch(connection) {
		case UTURN:
			return Double.MAX_VALUE;
		case FOLLOW_ON:
			return 0;
		case LEFT_TURN:
			if(strategy == SearchStrategy.NO_TURNS) {
				// Prefer not to (but don't completely disallow) left turns.
				return 1000;
			}
			return 0.5;
		case RIGHT_TURN:
			if(strategy == SearchStrategy.NO_RIGHT_TURNS || strategy == SearchStrategy.NO_TURNS) {
				// Prefer not to (but don't completely disallow) right turns.
				return 1000;
			}
			return 0.5;
		}
		return Double.MAX_VALUE;
	}
	
	private static List<Tuple<Integer,ConnectionType>> compute(
			List<MinimizedSegment> minimizedSegments,
			Collection<DetailedSegment> segmentsContainingFrom, 
			Collection<DetailedSegment> segmentsContainingTo, 
			SearchStrategy strategy) {
		Set<Integer> targetSegments =
				segmentsContainingTo.stream()
				.map(seg -> seg.id)
				.collect(ImmutableSet.toImmutableSet());

		Map<Integer, Double> shortestDistToSegment = new HashMap<>();
		PriorityQueue<Integer> queue = new PriorityQueue<>(
				Comparator.comparingDouble(shortestDistToSegment::get));
		Set<Integer> processedSegments = new HashSet<>();
		Map<Integer, Tuple<Integer, ConnectionType>> prevSegmentForSegment = new HashMap<>();
		segmentsContainingFrom.stream().forEach(segmentFrom -> {
			// Approximation: we assume the [from] is at the start of the segment (which it typically obvious is not).
			double distToSegment = 0;
			shortestDistToSegment.put(segmentFrom.id, distToSegment);
			queue.add(segmentFrom.id);
		});

		Integer pickedLastSegment = null;
		while(!queue.isEmpty()) {
			Integer segmentID = queue.poll();
			if (processedSegments.contains(segmentID)) {
				continue;
			}
			processedSegments.add(segmentID);
			
			if (targetSegments.contains(segmentID)) {
				// Found.
				// TODO: this is actually an approximation; we don't think about the distance *inside* the segment.
				pickedLastSegment = segmentID;
				break;
			}
			double distanceUpToSegment = shortestDistToSegment.get(segmentID);
			MinimizedSegment segment = minimizedSegments.get(segmentID);
//			Segment segmentObj = segments.get(segment);
			double distanceToEndOfSegment = distanceUpToSegment + segment.distance;
			
			for(SegmentConnection connection : segment.connections) {
				double connectionWeight = connectionWeight(connection.type, strategy);
				if (connectionWeight == Double.MAX_VALUE) {
					continue;
				}
				double distanceToNextSegment = distanceToEndOfSegment + connectionWeight;
				Integer nextSegment = connection.nextSegmentID;
				Double shortestDist = shortestDistToSegment.get(nextSegment);
				if (shortestDist == null || shortestDist > distanceToNextSegment) {
					shortestDistToSegment.put(nextSegment, distanceToNextSegment);
					queue.add(nextSegment);
					prevSegmentForSegment.put(nextSegment, new Tuple<>(segmentID, connection.type));
				}
			}
			
		}
		if (pickedLastSegment==null) {
			System.err.println("couldn't find path");
			return ImmutableList.of();
		}
		
		List<Tuple<Integer,ConnectionType>> reversedPickedSegments = new ArrayList<>();
		reversedPickedSegments.add(new Tuple<>(pickedLastSegment, null));
		while(true) {
			Tuple<Integer,ConnectionType> prev = prevSegmentForSegment.get(reversedPickedSegments.get(reversedPickedSegments.size()-1).fst);
			if (prev==null) {
				break;
			}
			reversedPickedSegments.add(prev);
		}
		return reversedPickedSegments;
	}
	
	private static List<NodeAndConnection> toNodes(
			List<MinimizedSegment> minimizedSegments,
			GridMap gridMap,
			long fromNode,
			long toNode,
			List<Tuple<Integer,ConnectionType>> reversedPickedSegments) {
		List<NodeAndConnection> nodes = new ArrayList<>();
		ConnectionType connectionFromPrevSegment = null;
		for(int i=reversedPickedSegments.size()-1;i>=0;i--) {
			Tuple<Integer,ConnectionType> segmentAndConnection = reversedPickedSegments.get(i);
			MinimizedSegment minimizedSegment = minimizedSegments.get(segmentAndConnection.fst);
			CellID cellID = minimizedSegment.cellID;
			DetailedSegment segment = gridMap.cellFor(cellID).segments()
					.stream()
					.filter(seg -> seg.id == segmentAndConnection.fst)
					.findFirst()
					.get();
			System.out.println(i+": "+segmentAndConnection.fst);
//			Segment segment = segments.get(segmentAndConnection.fst);
			if (i == reversedPickedSegments.size()-1) {
				// First segment
				int indexFrom = segment.indexOfNode(fromNode).get();
				for(int index = indexFrom;index < segment.nodes.length-1;index++) {
					nodes.add(new NodeAndConnection(segment.nodes[index], connectionFromPrevSegment));
					connectionFromPrevSegment=null;
				}
			} else if (i==0) {
				// Last segment
				int indexTo = segment.indexOfNode(toNode).get();
				for(int index = 0;index<=indexTo;index++) {
					nodes.add(new NodeAndConnection(segment.nodes[index],connectionFromPrevSegment));
					connectionFromPrevSegment=null;
				}
			} else {
				for(int index = 0;index<segment.nodes.length-1;index++) {
					nodes.add(new NodeAndConnection(segment.nodes[index], connectionFromPrevSegment));
					connectionFromPrevSegment=null;
				}
			}
			connectionFromPrevSegment = segmentAndConnection.snd;
		}
		return nodes;
	}
	
	private static List<NodeAndConnection> checkFromToIsSameSegment(
			Collection<DetailedSegment> segmentsContainingFrom,
			Collection<DetailedSegment> segmentsContainingTo,
			long fromNode, long toNode) {
		// Check if we have any segments in common.
		for(DetailedSegment segFrom : segmentsContainingFrom) {
			for(DetailedSegment segTo : segmentsContainingTo) {
				if (segFrom.id != segTo.id) {
					continue;
				}
				Optional<Integer> indexFrom = segFrom.indexOfNode(fromNode);
				Optional<Integer> indexTo = segFrom.indexOfNode(toNode);
				if (indexFrom.isEmpty() || indexTo.isEmpty()) {
					// shouldn't happen
					continue;
				}
				if (indexFrom.get() > indexTo.get()) {
					continue;
				}
				List<NodeAndConnection> nodes = new ArrayList<>();
				for(int index = indexFrom.get();index<=indexTo.get();index++) {
					nodes.add(new NodeAndConnection(segFrom.nodes[index]));
				}
				return nodes;
			}
		}
		return ImmutableList.of();
	}

	private static Tuple<Long, GridCell> getNearestNodeAndCell( GridMap gridMap, Coordinate coord) {
		CellID containingCell = gridMap.cellIDFor(coord);
		
		GridCell cell = gridMap.cellFor(containingCell);
		
		double nearestDist = Double.MAX_VALUE;
		Long nearest = null;
		for(Entry<Long, Coordinate> entry : cell.coordForID().entrySet()) {
			double dist = entry.getValue().distanceWith(coord);
			if (nearest == null || dist < nearestDist) {
				nearest = entry.getKey();
				nearestDist = dist;
			}
		}
		// TODO: we should actually be looking in neighbouring cells too, if the
		// edge border is closer than the nearest found node.
		return new Tuple<>(nearest, cell);
	}
	
	private static Set<DetailedSegment> getSegmentsContainingNode(GridCell cell, long node) {
		return cell.segments()
		.stream()
		.filter(seg -> {
			for(long segmentNode : seg.nodes) {
				if (segmentNode == node) {
					return true;
				}
			}
			return false;
		})
		.collect(ImmutableSet.toImmutableSet());
	}
	
	public static List<NodeAndConnection> computeShortestPath(
			List<MinimizedSegment> minimizedSegments,
			GridMap gridMap,
			Coordinate fromCoord, Coordinate toCoord, SearchStrategy strategy) {
		
		Tuple<Long, GridCell> fromNodeAndCell = getNearestNodeAndCell(gridMap, fromCoord);
		long fromNode = fromNodeAndCell.fst;
		GridCell fromCell = fromNodeAndCell.snd;
		Tuple<Long, GridCell> toNodeAndCell = getNearestNodeAndCell(gridMap, toCoord);
		long toNode = toNodeAndCell.fst;
		GridCell toCell = toNodeAndCell.snd;
		
		Set<DetailedSegment> segmentsContainingFrom = getSegmentsContainingNode(fromCell, fromNode);
		Set<DetailedSegment> segmentsContainingTo = getSegmentsContainingNode(toCell, toNode);

		//		// Check if we have any segments in common.
		List<NodeAndConnection> nodes
			= checkFromToIsSameSegment(segmentsContainingFrom, segmentsContainingTo, fromNode, toNode);
		if (!nodes.isEmpty()) {
			return nodes;
		}
		
		List<Tuple<Integer,ConnectionType>> reversedPickedSegments = 
				compute(minimizedSegments, segmentsContainingFrom, segmentsContainingTo, strategy);
		nodes=toNodes(minimizedSegments, gridMap, fromNode, toNode, reversedPickedSegments);
		return nodes;
	}
	
}
