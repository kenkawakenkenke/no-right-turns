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

public class Explore_ShortestPath {

	public static void main(String[] args) throws FileNotFoundException {
		File nodeCache = new File("../../data/nodes.bin");
		Map<Long, Coordinate> coordForID = (Map) ObjectEncoder.readObject(nodeCache);
		System.out.println("Loaded "+coordForID.size()+" coords");
		
		// TODO: filter for coords used in segments.
		
		File segmentCache = new File("../../data/segments.bin");
		List<Segment> segments = (List<Segment>)ObjectEncoder.readObject(segmentCache);
		System.out.println("Loaded "+segments.size()+" segments");

		GridMap gridMap = new GridMap(15);
		List<MinimizedSegment> minimizedSegments = MinimizedSegment.toMinimizedSegments(gridMap, coordForID, segments);
		segments.stream()
			.map(Segment::toDetailedSegment)
			.forEach(segment -> gridMap.add(coordForID, segment));

		GISViewer viewer = new GISViewer();
		viewer.gisView().setLevel(16);
		viewer.gisView().centerOnCoord(new Coordinate(139.4731736718688, 35.69820027307606));

		viewer.gisView().panelManager().openPanel(new AbstractGISPanel(
				AbstractGISPanel.MASK_HANDLE_MOUSEPRESSED | AbstractGISPanel.MASK_HANDLE_KEYPRESSED) {

			Coordinate firstCoord = null;
			Coordinate lastCoord = null;
			List<NodeAndConnection> shortestPathNodesNoRightTurns = new ArrayList<>();
			List<NodeAndConnection> shortestPathNodesShortest = new ArrayList<>();

			@Override
			public boolean mousePressed(int x, int y) {
				Coordinate mouseCoord = parentView().coordAtScreenPoint(x, y);
				
				if (firstCoord == null) {
					firstCoord = mouseCoord;
					System.out.println("first node: "+firstCoord);
				} else if (lastCoord == null) {
					lastCoord = mouseCoord;
					System.out.println("last node: "+lastCoord);
				} else {
					firstCoord = lastCoord = null;
					shortestPathNodesNoRightTurns = new ArrayList<>();
					shortestPathNodesShortest = new ArrayList<>();
				}

				if (firstCoord != null && lastCoord != null) {
					System.out.println("=======");
					System.out.println("No right turns:");
					shortestPathNodesNoRightTurns = ShortestPathSearcher.computeShortestPath(minimizedSegments,gridMap, firstCoord, lastCoord, SearchStrategy.NO_RIGHT_TURNS);
					System.out.println("right: " + shortestPathNodesNoRightTurns.stream().filter(n -> n.connection == ConnectionType.RIGHT_TURN).count());

//					System.out.println("shortest:");
//					shortestPathNodesShortest = ShortestPathSearcher.computeShortestPath(segments, firstNode, lastNode, SearchStrategy.SHORTEST);
//					System.out.println("right: " + shortestPathNodesShortest.stream().filter(n -> n.connection == ConnectionType.RIGHT_TURN).count());
				}

				repaint();

				return true;
			}
			
			private void drawSegments(Graphics2D g) {
				for (Segment segment : segments) {
					Path2D path = new Path2D.Double();
					boolean first = true;
					for (long nodeID : segment.nodes) {
						Coordinate coord= coordForID.get(nodeID);
						Point p = parentView().screenPointForCoord(coord);
						if (first) {
							path.moveTo(p.x, p.y);
							first = false;
						} else {
							path.lineTo(p.x, p.y);
						}
					}
					g.setColor(Color.gray);
					g.draw(path);
				}
			}
			
			private void drawPath(Graphics2D g, List<NodeAndConnection> path, Color pathColor) {
				if (path.isEmpty()) {
					return;
				}
				
				g.setColor(Color.green);
				Draw.fillCircle(g, parentView().screenPointForCoord(coordForID.get(path.get(0).node)), 20);
				g.setColor(Color.red);
				Draw.fillCircle(g, parentView()
						.screenPointForCoord(coordForID.get(path.get(path.size() - 1).node)), 20);

				Point prev = null;
				g.setStroke(new BasicStroke(3));
				for (NodeAndConnection node : path) {
					g.setColor(pathColor);
					Point p = parentView().screenPointForCoord(coordForID.get(node.node));
					if (prev != null) {
						Point mid = new Point((prev.x + p.x) / 2, (prev.y + p.y) / 2);
						Draw.drawLine(g, prev, p);
						Draw.drawArrow(g, prev, mid, 8);
					}
					if (node.connection != null && node.connection != ConnectionType.FOLLOW_ON) {
						g.setColor(Color.red);
						Draw.drawString(g, node.connection.name(), p);
					}
					prev = p;
				}
				g.setStroke(new BasicStroke(1));
			}

			@Override
			public void paint(Graphics2D g, int width, int height) {
//				drawSegments(g);

				drawPath(g, shortestPathNodesNoRightTurns, Color.black);
				drawPath(g, shortestPathNodesShortest, Color.gray);
			}
		});
	}
}
