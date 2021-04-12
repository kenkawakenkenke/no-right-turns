package com.ken.norightturns.analysis;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Path2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.ken.norightturns.ConnectionType;
import com.ken.norightturns.export.GridMap;
import com.ken.norightturns.export.MinimizedSegment;
import com.ken.norightturns.search.NodeAndConnection;
import com.ken.norightturns.search.SearchStrategy;
import com.ken.norightturns.search.ShortestPathSearcher;
import com.ken.norightturns.segment.Segment;

import common.ds.Tuple;
import common.gui.Draw;
import common.io.ObjectEncoder;
import model.Coordinate;
import viewer.GISViewer;
import viewer.panel.AbstractGISPanel;

public class Play_ShortestPath {

	public static void main(String[] args) throws FileNotFoundException {
		File nodeCache = new File("../../data/nodes.bin");
		Map<Long, Coordinate> coordForID = (Map) ObjectEncoder.readObject(nodeCache);
		System.out.println("Loaded "+coordForID.size()+" coords");

		File segmentCache = new File("../../data/segments.bin");
		List<Segment> segments = (List<Segment>)ObjectEncoder.readObject(segmentCache);
		System.out.println("Loaded "+segments.size()+" segments");

		GridMap gridMap = new GridMap(15);
		List<MinimizedSegment> minimizedSegments = MinimizedSegment.toMinimizedSegments(gridMap, coordForID, segments);
		segments.stream()
			.map(Segment::toDetailedSegment)
			.forEach(segment -> gridMap.add(coordForID, segment));

		Coordinate from = new Coordinate(139.6945485919435,35.693167347138306);
		Coordinate to = new Coordinate(139.7113723072946,35.68939265653715);
		List<NodeAndConnection> path =
				ShortestPathSearcher.computeShortestPath(minimizedSegments,gridMap, from, to, SearchStrategy.NO_RIGHT_TURNS);

	}
}
