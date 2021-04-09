package com.ken.norightturns.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.ken.norightturns.ConnectionType;
import com.ken.norightturns.segment.Segment;
import com.ken.norightturns.segment.Segment.SegmentConnection;

import common.ds.Tuple;

public class ShortestPathSearcher {

	public static ImmutableMultimap<Long, Segment> segmentsContainingNode(List<Segment> segments) {
		ImmutableMultimap.Builder<Long, Segment> segmentsContainingNode = 
				ImmutableMultimap.builder();
		for(Segment segment : segments) {
			for(Long node : segment.nodes) {
				segmentsContainingNode.put(node, segment);
			}
		}
		return segmentsContainingNode.build();
	}
	
	public static double connectionWeight(ConnectionType connection, SearchStrategy strategy) {
		switch(connection) {
		case UTURN:
			return Double.MAX_VALUE;
		case FOLLOW_ON:
			return 0;
		case LEFT_TURN:
			return 0.5;
		case RIGHT_TURN:
			if(strategy == SearchStrategy.NO_RIGHT_TURNS) {
				// Prefer not to (but don't completely disallow) right turns.
				return 1000;
			}
			return 0.5;
		}
		return Double.MAX_VALUE;
	}
	
	public static List<NodeAndConnection> computeShortestPath(List<Segment> segments, long from, long to, SearchStrategy strategy) {
		ImmutableMultimap<Long, Segment> segmentsContainingNode = segmentsContainingNode(segments);
		ImmutableMap<Long, Segment> segmentForID
			= segments.stream().collect(ImmutableMap.toImmutableMap(s->s.id, s->s));
		
		Collection<Segment> segmentsContainingFrom = segmentsContainingNode.get(from);
		Collection<Segment> segmentsContainingTo = segmentsContainingNode.get(to);
		
		// Check if we have any segments in common.
		for(Segment segFrom : segmentsContainingFrom) {
			for(Segment segTo : segmentsContainingTo) {
				if (segFrom != segTo) {
					continue;
				}
				int indexFrom = segFrom.indexOfNode(from);
				int indexTo = segFrom.indexOfNode(to);
				if (indexTo < indexFrom) {
					continue;
				}
				List<NodeAndConnection> nodes = new ArrayList<>();
				for(int index = indexFrom;index<=indexTo;index++) {
					nodes.add(new NodeAndConnection(segFrom.nodes[index]));
				}
				return nodes;
			}
		}
		
		HashSet<Segment> targetSegments = new HashSet<>();
		segmentsContainingTo.stream().forEach(seg -> targetSegments.add(seg));
		
		PriorityQueue<Tuple<Segment, Double>> queue = new PriorityQueue(Comparator.<Tuple<Segment, Double>>comparingDouble(i->i.snd));
		Map<Segment, Double> shortestDistToSegment = new HashMap<>();
		Set<Segment> processedSegments = new HashSet<>();
		Map<Segment, Tuple<Segment, ConnectionType>> prevSegmentForSegment = new HashMap<>();
		segmentsContainingFrom.stream().forEach(segmentFrom -> {
			int indexFrom = segmentFrom.indexOfNode(from);
			// Minus the distance from start ~ [from], so that when we add the distance of the segment,
			// we get the "true" weight of this segment (i.e distance from [from] to end).
			double distToSegment =  -segmentFrom.distancesToEnd[indexFrom];
			queue.add(new Tuple<>(segmentFrom,distToSegment));
			shortestDistToSegment.put(segmentFrom, distToSegment);
		});
		
		Segment pickedLastSegment = null;
		while(!queue.isEmpty()) {
			Tuple<Segment, Double> head = queue.poll();
			Segment segment = head.fst;
			if (processedSegments.contains(segment)) {
				continue;
			}
			processedSegments.add(segment);
			
			double distanceUpToSegment = head.snd;
			if (targetSegments.contains(segment)) {
				// Found.
				// TODO: this is actually an approximation; we don't think about the distance *inside* the segment.
				pickedLastSegment = segment;
				break;
			}
			
			for(SegmentConnection connection : segment.connections()) {
				double connectionWeight = connectionWeight(connection.type, strategy);
				if (connectionWeight == Double.MAX_VALUE) {
					continue;
				}
				double distanceToNextSegment = distanceUpToSegment + connectionWeight;
				Segment nextSegment = segmentForID.get(connection.nextSegmentID);
				Double shortestDist = shortestDistToSegment.get(nextSegment);
				if (shortestDist == null || shortestDist > distanceToNextSegment) {
					shortestDistToSegment.put(nextSegment, distanceToNextSegment);
					queue.add(new Tuple<Segment, Double>(nextSegment, distanceToNextSegment));
					prevSegmentForSegment.put(nextSegment, new Tuple<>(segment, connection.type));
				}
			}
			
		}
		if (pickedLastSegment==null) {
			System.err.println("couldn't find path");
			return ImmutableList.of();
		}
		List<Tuple<Segment,ConnectionType>> reversedPickedSegments = new ArrayList<>();
		reversedPickedSegments.add(new Tuple<>(pickedLastSegment, null));
		while(true) {
			Tuple<Segment,ConnectionType> prev = prevSegmentForSegment.get(reversedPickedSegments.get(reversedPickedSegments.size()-1).fst);
			if (prev==null) {
				break;
			}
			reversedPickedSegments.add(prev);
		}
		List<NodeAndConnection> nodes = new ArrayList<>();
		ConnectionType connectionFromPrevSegment = null;
		for(int i=reversedPickedSegments.size()-1;i>=0;i--) {
			Tuple<Segment,ConnectionType> segmentAndConnection = reversedPickedSegments.get(i);
			Segment segment = segmentAndConnection.fst;
			if (i == reversedPickedSegments.size()-1) {
				// First segment
				int indexFrom = segment.indexOfNode(from);
				for(int index = indexFrom;index < segment.nodes.length-1;index++) {
					nodes.add(new NodeAndConnection(segment.nodes[index], connectionFromPrevSegment));
					connectionFromPrevSegment=null;
				}
			} else if (i==0) {
				// Last segment
				int indexTo = segment.indexOfNode(to);
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
	
}
