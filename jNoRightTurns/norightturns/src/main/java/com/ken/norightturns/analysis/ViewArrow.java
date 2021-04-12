package com.ken.norightturns.analysis;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.ken.norightturns.export.GridMap;
import com.ken.norightturns.export.MinimizedSegment;
import com.ken.norightturns.segment.Segment;

import common.ds.Tuple;
import common.gui.Draw;
import common.io.ObjectEncoder;
import model.Coordinate;
import viewer.GISViewer;
import viewer.panel.AbstractGISPanel;

public class ViewArrow {

	public static void main(String[] args) throws FileNotFoundException {
		File nodeCache = new File("../../data/nodes.bin");
		Map<Long, Coordinate> coordForID = (Map) ObjectEncoder.readObject(nodeCache);
//		Map<Long, Coordinate> coordForID = coords.stream()
//				.collect(ImmutableMap.<Tuple<Long, Coordinate>, Long, Coordinate>toImmutableMap(e -> e.fst, e->e.snd));
//		System.out.println("Loaded "+coordForID.size()+" coords");
//		
		File segmentCache = new File("../../data/segments.bin");
		List<Segment> segments = (List<Segment>)ObjectEncoder.readObject(segmentCache);

		Coordinate tl = new Coordinate(139.41293293617915,35.7621781638664);
		Coordinate br = new Coordinate(139.84571170956804,35.642820718714624);
		
		GridMap gridMap = new GridMap(15);
		List<MinimizedSegment> minimizedSegments = MinimizedSegment.toMinimizedSegments(gridMap, coordForID, segments);
		segments.stream()
			.map(Segment::toDetailedSegment)
			.forEach(segment -> gridMap.add(coordForID, segment));
		
		GISViewer viewer = new GISViewer();
		viewer.gisView().setLevel(10);
		viewer.gisView().centerOnCoord(new Coordinate(139.699636, 35.7035072));

		viewer.gisView().panelManager().openPanel(new AbstractGISPanel(AbstractGISPanel.MASK_HANDLE_MOUSEPRESSED
				|AbstractGISPanel.MASK_HANDLE_KEYPRESSED
				|AbstractGISPanel.MASK_HANDLE_MOUSEMOVED) {
			
			@Override
			public void paint(Graphics2D g, int width, int height) {
				gridMap.cells().keySet().stream().forEach(cellID -> {
					g.setColor(Color.green);
					Coordinate tl = gridMap.tl(cellID);
					Coordinate br = gridMap.br(cellID);
					Point pointFrom = parentView().screenPointForCoord(tl);
					Point pointTo = parentView().screenPointForCoord(br);
					Draw.drawRect(g, pointFrom, pointTo);
					Point mid = new Point((pointFrom.x+pointTo.x)/2, (pointFrom.y+pointTo.y)/2);
					int num = gridMap.cellFor(cellID).coordForID().size();
					g.setColor(Color.black);
					Draw.drawStringCenteredAt(g, ""+num, mid);
				});
				
			}
		});
	}
}
