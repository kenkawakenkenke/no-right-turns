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

public class ExportSegments {

	public static void main(String[] args) throws FileNotFoundException {
		
		File nodeCache = new File("../../data/nodes.bin");
		
		Coordinate tl = new Coordinate(139.2224173720703,35.908991825853526);
		Coordinate br = new Coordinate(140.24139930566406,35.41918980534676);
		Map<Long, Coordinate> coordForID = ImportRawOSMData.loadCoordForID(tl, br);
		System.out.println("saving "+coordForID.size()+" items");
		ObjectEncoder.writeObject(nodeCache, coordForID);
		System.out.println("exported "+coordForID.size()+" coords");
//		Map<Long, Coordinate> coordForID = (Map) ObjectEncoder.readObject(nodeCache);

		File wayCache = new File("../../data/ways.bin");
		List<Way> ways = ImportRawOSMData.loadRawWays(coordForID);
		ObjectEncoder.writeObject(wayCache, ways);
//		List<Way> ways = (List<Way>) ObjectEncoder.readObject(wayCache);
		
		File segmentCache = new File("../../data/segments.bin");
		List<Segment> segments = Segmenter.generateSegments(ways, coordForID);
		ObjectEncoder.writeObject(segmentCache, segments);
//		List<Segment> segments = (List<Segment>)ObjectEncoder.readObject(segmentCache);
	}
}
