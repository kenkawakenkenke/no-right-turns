package com.ken.norightturns.segment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.ken.norightturns.ConnectionType;
import com.ken.norightturns.osm.Way;
import com.ken.norightturns.segment.Segment.SegmentConnection;
import com.ken.norightturns.util.IDGenerator;

import common.ds.ListUtil;
import model.Coordinate;

public class Segmenter {

	private static final double THRESH_MAX_ANGLE_FOR_NATURAL_FOLLOWING_SEGMENT_DEGREES = 20;
	static final double WEIGHT_RIGHT_TURN = 10000;

	private static List<Segment> toSegments(Way way, Map<Long, Integer> numForNode,Map<Long, Coordinate> coordForID, IDGenerator segmentIDGenerator) {

		List<Segment> segments = new ArrayList<>();
		List<Long> currentSegment = new ArrayList<>();
		for (Long node : way.nodes) {
			int num = numForNode.getOrDefault(node, 0);

			if (num > 2 && currentSegment.size() > 0) {
				// Split the segment here.
				currentSegment.add(node);
				segments.add(new Segment(way.id, segmentIDGenerator.get(), currentSegment, coordForID));
				if (!way.isOneWay) {
					segments.add(new Segment(way.id, segmentIDGenerator.get(), ListUtil.reverse(currentSegment), coordForID));
				}

				currentSegment = new ArrayList<>();
				// Start with where we left off.
				currentSegment.add(node);
				continue;
			}
			currentSegment.add(node);
		}
		if (currentSegment.size() >= 2) {
			segments.add(new Segment(way.id, segmentIDGenerator.get(), currentSegment, coordForID));
			if (!way.isOneWay) {
				segments.add(new Segment(way.id, segmentIDGenerator.get(), ListUtil.reverse(currentSegment), coordForID));
			}
		}
		return segments;
	}

	public static List<Segment> toSegments(List<Way> ways, Map<Long, Coordinate> coordForID) {
		IDGenerator segmentIDGenerator = new IDGenerator();

		Map<Long, Integer> numForNode = new HashMap<>();
		for (Way way : ways) {
			for (int i = 0; i < way.nodes.size() - 1; i++) {
				Long node1 = way.nodes.get(i);
				Long node2 = way.nodes.get(i + 1);
				numForNode.put(node1, numForNode.getOrDefault(node1, 0) + 1);
				numForNode.put(node2, numForNode.getOrDefault(node2, 0) + 1);
			}
		}

		List<Segment> segments = new ArrayList<>();
		for (Way way : ways) {
			segments.addAll(toSegments(way, numForNode, coordForID, segmentIDGenerator));
		}
		return segments;
	}

	private static ConnectionType scoreNextSegment(Segment fromSegment, Segment toSegment, Segment naturalNextSegment,
			Map<Long, Coordinate> coordForID) {
		// Segments which are just a reverse of us should never be chosen.
		// (Although in some cases it may make sense to allow u-turns. dunno.)
		if (fromSegment.start() == toSegment.end() && fromSegment.end() == toSegment.start()
				&& fromSegment.parentWayID == toSegment.parentWayID) {
			return ConnectionType.UTURN;
		}
		// Segments that follow on from the current segment isn't a turn, so has no
		// weight.
		if (toSegment == naturalNextSegment) {
			return ConnectionType.FOLLOW_ON;
		}
		double heading = Coordinate.headingBetween(
				coordForID.get(fromSegment.nodes[fromSegment.nodes.length - 2]),
				coordForID.get(fromSegment.nodes[fromSegment.nodes.length - 1]),
				coordForID.get(toSegment.nodes[1]));
		if (heading > 0) {
			return ConnectionType.RIGHT_TURN;
		}
		return ConnectionType.LEFT_TURN;
	}

	private static Segment naturalNextSegment(Segment segment, Collection<Segment> connectedSegments,
			Map<Long, Coordinate> coordForID) {
		// If there's a segment that's on the same way (and it's not a reverse of us),
		// then it's a naturally following segment.
		for (Segment next : connectedSegments) {
			if (next.parentWayID == segment.parentWayID && next.end() != segment.start()) {
				System.out.println("found " + next.parentWayID + " " + segment.parentWayID);
				return next;
			}
		}
		// Otherwise, try to find one that has the closest heading.
		Segment nearestNext = null;
		double nearestAbsHeading = 0;
		for (Segment next : connectedSegments) {
			double heading = Coordinate.headingBetween(
					coordForID.get(segment.nodes[segment.nodes.length - 2]),
					coordForID.get(segment.nodes[segment.nodes.length - 1]), 
					coordForID.get(next.nodes[1]));
			double absHeading = Math.abs(heading);

			// Let's say a "naturally following" road needs to be a turn of less than X
			// degrees:
			if (absHeading * 180 / Math.PI > THRESH_MAX_ANGLE_FOR_NATURAL_FOLLOWING_SEGMENT_DEGREES) {
				continue;
			}

			if (nearestNext == null || absHeading < nearestAbsHeading) {
				nearestNext = next;
				nearestAbsHeading = absHeading;
			}
		}
		return nearestNext;
	}

	private static void connectSegments(List<Segment> segments, Map<Long, Coordinate> coordForID) {
		ImmutableMultimap.Builder<Long, Segment> segmentsStartingAtBuilder = ImmutableMultimap.builder();
		segments.stream().forEach(s -> segmentsStartingAtBuilder.put(s.start(), s));
		ImmutableMultimap<Long, Segment> segmentsStartingAt = segmentsStartingAtBuilder.build();

		for (Segment segment : segments) {
			ImmutableCollection<Segment> connectingSegments = segmentsStartingAt.get(segment.end());

			// Find segments "naturally connecting" to this segment; i.e segments which
			// probably aren't "turns".
			// This isn't trivial, because not all "naturally connecting" segments are on
			// the same "Way"; a way
			// could arbitrarily be split in the middle of a single road.
			Segment naturalNextSegment = naturalNextSegment(segment, connectingSegments, coordForID);

			Collection<SegmentConnection> connections = connectingSegments.stream()
//					.filter(seg -> seg.parentWay != segment.parentWay)
					.map(next -> {
						ConnectionType type = scoreNextSegment(segment, next, naturalNextSegment, coordForID);
						return new SegmentConnection(type, next.id);
					}).filter(con -> con.type != ConnectionType.UTURN).collect(ImmutableList.toImmutableList());
			segment.setConnectingSegments(connections);
		}
	}

	public static List<Segment> generateSegments(List<Way> ways, Map<Long, Coordinate> coordForID) {
		// Segment ways: Split ways into "segments" that:
		// - don't have connections to other segments in internal nodes (i.e nodes that
		// aren't start/end nodes)
		// - are one way (i.e we split both-direction ways into two)
		// - are at least 2 nodes long.
		List<Segment> segments = toSegments(ways, coordForID);

		// Connect segments
		connectSegments(segments, coordForID);
		return segments;
	}

}
