package com.ken.norightturns.analysis;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.FileNotFoundException;

import common.gui.Draw;
import model.Coordinate;
import viewer.GISViewer;
import viewer.panel.AbstractGISPanel;

public class PickBounds {
    
    public static void main(String[] args) throws FileNotFoundException {
    	
		GISViewer viewer = new GISViewer();
		viewer.gisView().setLevel(10);
		viewer.gisView().centerOnCoord(new Coordinate(139.699636,35.7035072));

        viewer.gisView().panelManager().openPanel(new AbstractGISPanel(
        		AbstractGISPanel.MASK_HANDLE_MOUSEPRESSED) {
        	
        	Coordinate tl = null;
        	Coordinate br = null;
        	@Override
			public boolean mousePressed(int x, int y) {
        		if (tl == null) {
        			tl = parentView().coordAtScreenPoint(x, y);
        		} else if (br == null) {
        			br = parentView().coordAtScreenPoint(x, y);
        		} else {
        			tl = br = null;
        		}
        		System.out.println(tl);
        		System.out.println(br);
        		repaint();
        		return true;
			}
        	
			@Override
			public void paint(Graphics2D g, int width, int height) {
				if (tl ==null || br == null) {
					return;
				}
				g.setColor(Color.green);
				Draw.drawRect(g, parentView().screenPointForCoord(tl), parentView().screenPointForCoord(br));
			}
		});
	}
}
