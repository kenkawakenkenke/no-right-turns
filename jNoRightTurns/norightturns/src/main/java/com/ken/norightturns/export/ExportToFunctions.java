package com.ken.norightturns.export;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.SetOptions;
import com.google.cloud.firestore.WriteBatch;
import com.google.common.collect.ImmutableList;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.ken.norightturns.export.GridMap.GridCell;
import com.ken.norightturns.segment.Segment;

import common.io.ObjectEncoder;
import model.Coordinate;

public class ExportToFunctions {

	private static void setupFirebase() throws IOException {
		FileInputStream serviceAccount = new FileInputStream("no-right-turns-firebase-adminsdk-m4t6b-fcd783b1d0.json");

		FirebaseOptions options = new FirebaseOptions.Builder()
				.setCredentials(GoogleCredentials.fromStream(serviceAccount))
				.build();
		FirebaseApp.initializeApp(options);
	}

	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
		setupFirebase();
		
		Firestore db = FirestoreClient.getFirestore();
		
		File nodeCache = new File("../../data/nodes.bin");
		Map<Long, Coordinate> coordForID = (Map) ObjectEncoder.readObject(nodeCache);
		
		File segmentCache = new File("../../data/segments.bin");
		List<Segment> segments = (List<Segment>)ObjectEncoder.readObject(segmentCache);
		
		GridMap gridMap = new GridMap(15);
		segments.stream()
			.map(Segment::toDetailedSegment)
			.forEach(segment -> gridMap.add(coordForID, segment));

		List<Map<String, Object>> rows = 
				gridMap.cells().values()
					.stream()
					.map(GridCell::toJSONMap)
					.collect(ImmutableList.toImmutableList());
		
		final int batchSize = 10;
		for(int i=0;i<rows.size();i+=batchSize) {
			int toIndex = Math.min(i+batchSize, rows.size());
			System.out.println("Writing from "+i+"~"+toIndex+ "(of "+rows.size()+")"
					+ "");
			
			WriteBatch batch = db.batch();
			
			rows.subList(i, toIndex)
			.forEach(row -> {
				DocumentReference doc = 
						db.collection("cell").document(row.get("cell").toString());
				batch.set(doc, row, SetOptions.merge());
			});
			System.out.println("write");
			batch.commit().get();
		}
		
		System.out.println("done");
	}
}
