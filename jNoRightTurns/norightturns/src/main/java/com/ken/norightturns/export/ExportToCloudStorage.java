package com.ken.norightturns.export;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Bucket.BlobTargetOption;
import com.google.cloud.storage.Storage.PredefinedAcl;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.StorageClient;
import com.google.gson.Gson;
import com.ken.norightturns.export.GridMap.GridCell;
import com.ken.norightturns.segment.Segment;

import common.ds.Tuple;
import common.io.ObjectEncoder;
import model.Coordinate;

public class ExportToCloudStorage {

	private static void setupFirebase() throws IOException {
		FileInputStream serviceAccount = new FileInputStream("no-right-turns-firebase-adminsdk-m4t6b-fcd783b1d0.json");

		FirebaseOptions options = new FirebaseOptions.Builder()
				.setCredentials(GoogleCredentials.fromStream(serviceAccount))
				.build();
		FirebaseApp.initializeApp(options);
	}

	
	public static GridMap loadGridMap() {
		File nodeCache = new File("../../data/nodes.bin");
		Map<Long, Coordinate> coordForID = (Map) ObjectEncoder.readObject(nodeCache);
		
		File segmentCache = new File("../../data/segments.bin");
		List<Segment> segments = (List<Segment>)ObjectEncoder.readObject(segmentCache);
		
		GridMap gridMap = new GridMap(15);
		segments.stream()
			.map(Segment::toDetailedSegment)
			.forEach(segment -> gridMap.add(coordForID, segment));
		return gridMap;

	}
	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
		setupFirebase();
		
		StorageClient client = com.google.firebase.cloud.StorageClient.getInstance();

		Bucket bucket = client.bucket("no-right-turns-grid");
		{
			System.out.println(bucket.get("29066_12889").getMediaLink());
			if(true)return;
		}

		GridMap gridMap = loadGridMap();
		
		gridMap.cells().values()
		.stream()
		.map(GridCell::toJSONMap)
		.map(row -> new Tuple<>(row.get("cell").toString(), new Gson().toJson(row)))
		.forEach(cellAndJson -> {
			String cellID = cellAndJson.fst;
			String json = cellAndJson.snd;
			
			try {
				Blob exportedBlob = bucket.create(cellID, json.getBytes("UTF-8")
						,BlobTargetOption.predefinedAcl(PredefinedAcl.PUBLIC_READ)
						);
				System.out.println(cellID+" "+exportedBlob.getMediaLink());
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		});
		
	}
}
