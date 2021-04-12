package com.ken.norightturns.export;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableMap;

import model.Coordinate;

public class GridMap implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2919969946923048087L;
	
	public static class CellID implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = -6292943491661422258L;
		public final int gridX;
		public final int gridY;
		public CellID(int gridX, int gridY) {
			this.gridX = gridX;
			this.gridY = gridY;
		}
		@Override
		public int hashCode() {
			return Objects.hash(gridX, gridY);
		}
		@Override
		public boolean equals(Object obj) {
			CellID other = (CellID)obj;
			return gridX == other.gridX && gridY == other.gridY;
		}
		public CellID bottomRight() {
			return new CellID(gridX+1, gridY+1);
		}
		@Override
		public String toString() {
			return gridX+"_"+gridY;
		}
		
		private static Pattern STRING_FORMAT = Pattern.compile("(.*)_(.*)");
		public static CellID fromString(String str) {
			Matcher matcher = STRING_FORMAT.matcher(str);
			if (!matcher.matches()) {
				throw new IllegalArgumentException("unknown format: "+str);
			}
			return new CellID(Integer.parseInt(matcher.group(1)),
					Integer.parseInt(matcher.group(2)));
		}
	}
	
	public final int zoomLevel;
	public final int tileSize;
	private Map<CellID, GridCell> cells;
	public GridMap(int zoomLevel) {
		this.zoomLevel = zoomLevel;
		this.tileSize = 1 << zoomLevel;
		this.cells  = new HashMap<>();
	}
	
	public CellID cellIDFor(Coordinate coord) {
		Point2D normalized = coord.normalized();
		int x = (int)(normalized.getX() * tileSize);
		int y = (int)(normalized.getY() * tileSize);
		CellID cellID = new CellID(x,y);
		return cellID;
	}
	public GridCell cellFor(CellID cellID) {
		return cells.computeIfAbsent(cellID, unused -> new GridCell(cellID));
	}
	public GridCell cellAt(Coordinate coord) {
		CellID cellID = cellIDFor(coord);
		return cellFor(cellID);
	}
	public Map<CellID, GridCell> cells() {
		return cells;
	}
	public Coordinate tl(CellID cellID) {
		double x = cellID.gridX / (double)tileSize;
		double y = cellID.gridY / (double)tileSize;
		return Coordinate.coordFromNormalizedPoint(x, y);
	}
	public Coordinate br(CellID cellID) {
		return tl(cellID.bottomRight());
	}
	
	public void add(Map<Long, Coordinate> coordForID, DetailedSegment segment) {
		Set<CellID> cellIDs = new HashSet<>();
		for(Long node : segment.nodes) {
			Coordinate nodeCoord = coordForID.get(node);
			cellIDs.add(this.cellIDFor(nodeCoord));
		}

		for(Long node : segment.nodes) {
			Coordinate nodeCoord = coordForID.get(node);
			cellIDs.stream()
			.map(this::cellFor)
			.forEach(cell -> cell.addCoord(node, nodeCoord));
		}
		cellIDs.stream()
			.map(this::cellFor)
			.forEach(cell -> cell.addSegment(segment));
	}
	
	public static class GridCell implements Serializable {
		/**
	 * 
	 */
	private static final long serialVersionUID = 6138080461660438102L;
		public final CellID cellID;
		private final Map<Long, Coordinate> coordForID = new HashMap<>();
		private final Map<Integer, DetailedSegment> segmentForID =  new HashMap<>();
		
		public GridCell(CellID cellID) {
			this.cellID = cellID;
		}
		
		public void addCoord(Long id, Coordinate coord) {
			coordForID.put(id, coord);
		}
		public void addSegment(DetailedSegment segment) {
			segmentForID.put(segment.id, segment);
		}
		
		public Map<Long, Coordinate> coordForID() {
			return coordForID;
		}
		public Collection<DetailedSegment> segments() {
			return segmentForID.values();
		}
		public Map<String, Object> toJSONMap() {
			ImmutableMap<String, Object> jsonCoordForID =
				coordForID.entrySet()
				.stream()
				.collect(ImmutableMap.toImmutableMap(
						e->e.getKey().toString(), 
						e->{
							Coordinate coord = e.getValue();
							return ImmutableMap.of(
									"lat", coord.latitude,
									"lng", coord.longitude);
						}));
			
			ImmutableMap<String, Object> jsonSegmentForID =
					segmentForID.entrySet()
					.stream()
					.collect(ImmutableMap.toImmutableMap(
							e->e.getKey().toString(),
							e->e.getValue().toJSONMap()));
			return ImmutableMap.of(
					"cell", cellID.toString(),
					"nodes", jsonCoordForID,
					"segments", jsonSegmentForID
					);
		}
	}
}