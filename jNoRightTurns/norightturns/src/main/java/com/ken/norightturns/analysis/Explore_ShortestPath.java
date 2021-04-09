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

import com.ken.norightturns.ConnectionType;
import com.ken.norightturns.osm.ImportRawOSMData;
import com.ken.norightturns.osm.Way;
import com.ken.norightturns.search.NodeAndConnection;
import com.ken.norightturns.search.SearchStrategy;
import com.ken.norightturns.search.ShortestPathSearcher;
import com.ken.norightturns.segment.Segment;
import com.ken.norightturns.segment.Segmenter;

import common.gui.Draw;
import common.io.ObjectEncoder;
import model.Coordinate;
import viewer.GISViewer;
import viewer.panel.AbstractGISPanel;

public class Explore_ShortestPath {

	public static Long getNearestNode(List<Segment> segments, Coordinate coord, Map<Long, Coordinate> coordForID) {
		double nearestDist = Double.MAX_VALUE;
		Long nearest = null;
		for (Segment segment : segments) {
			for (long nodeID : segment.nodes) {
				Coordinate otherCoord = coordForID.get(nodeID);
				double dist = coord.distanceWith(otherCoord);
				if (nearest == null || dist < nearestDist) {
					nearest = nodeID;
					nearestDist = dist;
				}
			}
		}
		return nearest;
	}

	public static void main(String[] args) throws FileNotFoundException {
		
		File nodeCache = new File("../../data/nodes.bin");
		Map<Long, Coordinate> coordForID = (Map) ObjectEncoder.readObject(nodeCache);
		
		File segmentCache = new File("../../data/segments.bin");
		List<Segment> segments = (List<Segment>)ObjectEncoder.readObject(segmentCache);

		GISViewer viewer = new GISViewer();
		viewer.gisView().setLevel(16);
		viewer.gisView().centerOnCoord(new Coordinate(139.4731736718688, 35.69820027307606));

		viewer.gisView().panelManager().openPanel(new AbstractGISPanel(
				AbstractGISPanel.MASK_HANDLE_MOUSEPRESSED | AbstractGISPanel.MASK_HANDLE_KEYPRESSED) {

			Long firstNode = null;
			Long lastNode = null;
			List<NodeAndConnection> shortestPathNodesNoRightTurns = new ArrayList<>();
			List<NodeAndConnection> shortestPathNodesShortest = new ArrayList<>();

			@Override
			public boolean mousePressed(int x, int y) {
				Coordinate mouseCoord = parentView().coordAtScreenPoint(x, y);
				Long node = getNearestNode(segments, mouseCoord, coordForID);

				if (firstNode == null) {
					firstNode = node;
				} else if (lastNode == null) {
					lastNode = node;
				} else {
					firstNode = lastNode = null;
					shortestPathNodesNoRightTurns = new ArrayList<>();
					shortestPathNodesShortest = new ArrayList<>();
				}

				if (firstNode != null && lastNode != null) {
					shortestPathNodesNoRightTurns = ShortestPathSearcher.computeShortestPath(segments, firstNode, lastNode, SearchStrategy.NO_RIGHT_TURNS);
					shortestPathNodesShortest = ShortestPathSearcher.computeShortestPath(segments, firstNode, lastNode, SearchStrategy.SHORTEST);
					
					System.out.println("no right turns: " + shortestPathNodesNoRightTurns.stream().filter(n -> n.connection == ConnectionType.RIGHT_TURN).count());
					System.out.println("shortest: " + shortestPathNodesShortest.stream().filter(n -> n.connection == ConnectionType.RIGHT_TURN).count());
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
				drawSegments(g);

				drawPath(g, shortestPathNodesNoRightTurns, Color.black);
				drawPath(g, shortestPathNodesShortest, Color.gray);
			}
		});
	}
}
