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
        		AbstractGISPanel.MASK_HANDLE_MOUSEPRESSED
        		|AbstractGISPanel.MASK_HANDLE_KEYPRESSED
        		|AbstractGISPanel.MASK_HANDLE_MOUSEMOVED) {
        	
        	@Override
			public boolean keyPressed(char key) {
        		if (key=='1') {
        			editingCorner=1;
        		} else if (key=='2') {
        			editingCorner=2;
        		}
        		return true;
			}
        	@Override
			public boolean mouseMoved(int x, int y) {
        		if (editingCorner==1) {
        			tl = parentView().coordAtScreenPoint(x, y);
        		} else if (editingCorner == 2) {
        			br = parentView().coordAtScreenPoint(x, y);
        		}
        		System.out.println("Coordinate tl = new Coordinate("+tl+");");
        		System.out.println("Coordinate br = new Coordinate("+br+");");
        		repaint();
        		return true;
			}

        	int editingCorner = 0;
        	Coordinate tl = new Coordinate(139.32868640724385,35.80218333916872);
        	Coordinate br = new Coordinate(139.9413018663132,35.531241351238144);
        	@Override
			public boolean mousePressed(int x, int y) {
        		editingCorner = 0;
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
