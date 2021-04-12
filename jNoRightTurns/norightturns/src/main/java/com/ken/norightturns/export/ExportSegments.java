package com.ken.norightturns.export;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.ken.norightturns.export.GridMap.GridCell;
import com.ken.norightturns.osm.Way;
import com.ken.norightturns.segment.Segment;
import com.ken.norightturns.segment.Segmenter;

import common.file.FileUtil;
import common.io.ObjectEncoder;
import model.Coordinate;

public class ExportSegments {

	public static void exportMinimizedSegments(List<MinimizedSegment> minimizedSegments) throws FileNotFoundException {
		List<Object> flatMinimizedSegments =
				minimizedSegments
				.stream()
				.map(MinimizedSegment::toJSONMap)
				.collect(ImmutableList.toImmutableList());
		
		PrintWriter out =new PrintWriter("../../data/minimizedSegments.json");
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(Double.class, (JsonSerializer<Double>) (src, typeOfSrc, context) -> {
		    DecimalFormat df = new DecimalFormat("#.##");
		    df.setRoundingMode(RoundingMode.CEILING);
		    return new JsonPrimitive(Double.parseDouble(df.format(src)));
		});
		out.println(gsonBuilder.create().toJson(flatMinimizedSegments));
		out.close();
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		
		File wayCache = new File("../../data/ways.bin");
//		Coordinate tl = new Coordinate(139.32868640724385,35.80218333916872);
//		Coordinate br = new Coordinate(139.9413018663132,35.531241351238144);
//		Map<Long, Coordinate> allCoordForID = ImportRawOSMData.loadCoordForID(tl, br);
//		List<Way> ways = ImportRawOSMData.loadRawWays(allCoordForID);
//		ObjectEncoder.writeObject(wayCache, ways);
		List<Way> ways = (List<Way>) ObjectEncoder.readObject(wayCache);
		
		// Filter used coords
		File nodeCache = new File("../../data/nodes.bin");
//		Set<Long> usedCoords =
//				ways.stream().flatMap(way -> way.nodes.stream()).collect(ImmutableSet.toImmutableSet());
//		Map<Long, Coordinate> coordForID =
//				allCoordForID.entrySet().stream()
//				.filter(e -> usedCoords.contains(e.getKey()))
//				.collect(ImmutableMap.toImmutableMap(e -> e.getKey(), e -> e.getValue()));
//		ObjectEncoder.writeObject(nodeCache, coordForID);
		Map<Long, Coordinate> coordForID = (Map) ObjectEncoder.readObject(nodeCache);
		
		File segmentCache = new File("../../data/segments.bin");
//		List<Segment> segments = Segmenter.generateSegments(ways, coordForID);
//		ObjectEncoder.writeObject(segmentCache, segments);
		List<Segment> segments = (List<Segment>)ObjectEncoder.readObject(segmentCache);

		GridMap gridMap = new GridMap(15);
		List<MinimizedSegment> minimizedSegments = MinimizedSegment.toMinimizedSegments(gridMap, coordForID, segments);
		segments.stream()
			.map(Segment::toDetailedSegment)
			.forEach(segment -> gridMap.add(coordForID, segment));
		
		exportMinimizedSegments(minimizedSegments);
		
		// Export each grid cell
		File gridDir = new File("../../data/grid");
		FileUtil.forceDelete(gridDir);
		gridDir.mkdirs();
		
		gridMap.cells().values()
		.stream()
		.map(GridCell::toJSONMap)
		.forEach(map -> {
			File f  = new File(gridDir, map.get("c")+".json");
			try {
				PrintWriter out =new PrintWriter(f);
				GsonBuilder gsonBuilder = new GsonBuilder();
				out.println(gsonBuilder.create().toJson(map));
				out.close();
			}catch(IOException e) {
				e.printStackTrace();
			}
		});
		
	}
}
