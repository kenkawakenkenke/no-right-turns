package com.ken.norightturns.analysis;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import com.ken.norightturns.segment.Segment;

import common.gui.Draw;
import common.io.ObjectEncoder;
import model.Coordinate;
import viewer.GISViewer;
import viewer.panel.AbstractGISPanel;

public class ViewArrow {

	public static void main(String[] args) throws FileNotFoundException {

		File segmentCache = new File("../../data/segments.bin");
		List<Segment> segments = (List<Segment>)ObjectEncoder.readObject(segmentCache);
		
		if(true)return;
		
//		File dataFile = new File("../../data/kanto-latest.osm");
//
//		InputStream input = new FileInputStream(dataFile);
//
//		// Create an iterator for XML data
//		OsmIterator iterator = new OsmXmlIterator(input, false);

		Coordinate tl = new Coordinate(139.43665926505858, 35.720283152657764);
		Coordinate br = new Coordinate(139.50147985989872, 35.677769931399155);

		GISViewer viewer = new GISViewer();
		viewer.gisView().setLevel(10);
		viewer.gisView().centerOnCoord(new Coordinate(139.699636, 35.7035072));

		viewer.gisView().panelManager().openPanel(new AbstractGISPanel(AbstractGISPanel.MASK_HANDLE_MOUSEPRESSED
				|AbstractGISPanel.MASK_HANDLE_KEYPRESSED
				|AbstractGISPanel.MASK_HANDLE_MOUSEMOVED) {
			int pointer = 0;
			Coordinate[] coords = new Coordinate[3];
			
			@Override
			public boolean keyPressed(char key) {
				if (key>='1' && key<='3') {
					System.out.println(key);
					pointer = key-'1';
				}
				return true;
			}
			
			@Override
			public boolean mouseMoved(int x, int y) {
				Coordinate coord = parentView().coordAtScreenPoint(x, y);
				coords[pointer] = coord;
				repaint();

				if (coords[coords.length-1]!=null) {
					double diff = Coordinate.headingBetween(coords[0], coords[1], coords[2]);
					System.out.println(diff*180/Math.PI);
				}
				
				return true;
			}
			
			@Override
			public boolean mousePressed(int x, int y) {
				Coordinate coord = parentView().coordAtScreenPoint(x, y);
				coords[pointer] = coord;
				pointer = (pointer+1)%3;
				
				repaint();
				return true;
			}
			
			@Override
			public void paint(Graphics2D g, int width, int height) {
				for(int i=0;i<coords.length-1;i++) {
					Coordinate prev = coords[i];
					Coordinate coord = coords[i+1];
					if (prev ==null || coord == null) {
						break;
					}
					
					Point prevPoint = parentView().screenPointForCoord(prev);
					Point point = parentView().screenPointForCoord(coord);
					g.setColor(Color.red);
					Draw.drawArrow(g, prevPoint, point, 10);
				}
			}
		});
	}
}
