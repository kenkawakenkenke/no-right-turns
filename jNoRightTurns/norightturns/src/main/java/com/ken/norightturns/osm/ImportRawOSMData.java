package com.ken.norightturns.osm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import common.ds.ListUtil;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmTag;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.xml.dynsax.OsmXmlIterator;
import model.Coordinate;

public class ImportRawOSMData {

	// Highway types
	// https://wiki.openstreetmap.org/wiki/Key:highway
	private static Set<String> ROAD_HIGHWAY_TYPES = ImmutableSet.<String>builder().add("motorway").add("trunk")
			.add("primary").add("secondary").add("motorway").add("tertiary").add("unclassified").add("residential")
			.add("living_street").build();

	public static Map<Long, Coordinate> loadCoordForID(Coordinate tl, Coordinate br) throws FileNotFoundException {
		File dataFile = new File("../../data/kanto-latest.osm");

		InputStream input = new FileInputStream(dataFile);

		// Create an iterator for XML data
		OsmIterator iterator = new OsmXmlIterator(input, false);

		Map<Long, Coordinate> coordForID = new HashMap<>();
		for (EntityContainer container : iterator) {
			// Check if the element is a node
			if (container.getType() == EntityType.Node) {
				// Cast the entity to OsmNode
				OsmNode node = (OsmNode) container.getEntity();

				double lng = node.getLongitude();
				double lat = node.getLatitude();
				boolean inRange = tl.longitude <= lng && lng <= br.longitude && tl.latitude >= lat
						&& lat >= br.latitude;
						
				inRange = true;
				
				if (inRange) {
					coordForID.computeIfAbsent(node.getId(),
							id -> new Coordinate(node.getLongitude(), node.getLatitude()));
				}
			}
		}
		return coordForID;
	}

	public static List<Way> loadRawWays(Map<Long, Coordinate> coordForID) throws FileNotFoundException {
		File dataFile = new File("../../data/kanto-latest.osm");

		InputStream input = new FileInputStream(dataFile);

		// Create an iterator for XML data
		OsmIterator iterator = new OsmXmlIterator(input, false);

		List<Way> ways = new ArrayList<>();
		for (EntityContainer container : iterator) {
			if (container.getType() == EntityType.Way) {
				OsmWay osmWay = (OsmWay) container.getEntity();

				Map<String, String> tags = new HashMap<>();
				for (int i = 0; i < osmWay.getNumberOfTags(); i++) {
					OsmTag tag = osmWay.getTag(i);
					tags.put(tag.getKey(), tag.getValue());
				}
				String highwayType = tags.get("highway");
				if (highwayType == null) {
					continue;
				}

				if (!ROAD_HIGHWAY_TYPES.contains(highwayType)) {
					continue;
				}

				List<Long> nodes = new ArrayList<>();
				for (int i = 0; i < osmWay.getNumberOfNodes(); i++) {
					long node = osmWay.getNodeId(i);
					if (coordForID.containsKey(node)) {
						nodes.add(node);
					}
				}
				if (nodes.size() <= 1) {
					continue;
				}

				boolean isOneWay = false;
				if ("yes".equals(tags.get("oneway"))) {
					isOneWay = true;
				} else if ("-1".equals(tags.get("oneway"))) {
					isOneWay = true;
					// Reverse the nodes
					nodes = ListUtil.reverse(nodes);
				}

//				System.out.println("osmway: "+osmWay.getId()+" "+osmWay);
				Way way = new Way(osmWay.getId(), nodes, tags, highwayType, isOneWay);
				ways.add(way);
				if (ways.size() % 1000 == 0) {
					System.out.println(ways.size());
				}
//
//				ImmutableMap<String, String> filteredTags = tags.entrySet().stream()
//						.filter(e -> e.getKey().equals("oneway"))
////						.filter(e -> (!e.getKey().equals("highway") && !e.getKey().equals("oneway")
////								&& !e.getKey().equals("source")))
//						.collect(ImmutableMap.toImmutableMap(e -> e.getKey(), e -> e.getValue()));
//
//				if (!filteredTags.isEmpty()) {
//					System.out.println(filteredTags);
//				}
			}
		}
		return ways;
	}
}
