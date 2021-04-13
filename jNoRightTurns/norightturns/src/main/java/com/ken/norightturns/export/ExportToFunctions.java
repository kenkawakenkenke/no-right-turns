package com.ken.norightturns.export;

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
import com.google.gson.Gson;
import com.ken.norightturns.export.GridMap.GridCell;

public class ExportToFunctions {

	private static void setupFirebase() throws IOException {
		FileInputStream serviceAccount = new FileInputStream("no-right-turns-firebase-adminsdk-m4t6b-fcd783b1d0.json");

		FirebaseOptions options = new FirebaseOptions.Builder()
				.setCredentials(GoogleCredentials.fromStream(serviceAccount))
				.build();
		FirebaseApp.initializeApp(options);
	}
	
	public static int estimateSize(Object obj) {
		return new Gson().toJson(obj).length();
	}

	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
		setupFirebase();
		
		Firestore db = FirestoreClient.getFirestore();

		GridMap gridMap = ExportToCloudStorage.loadGridMap();

		List<Map<String, Object>> rows = 
				gridMap.cells().values()
					.stream()
					.map(GridCell::toJSONMap)
					.collect(ImmutableList.toImmutableList());
		
		final int batchSize = 5;
		for(int i=920;i<rows.size();i+=batchSize) {
			int toIndex = Math.min(i+batchSize, rows.size());
			List<Map<String, Object>> exportObj = rows.subList(i,  toIndex);
			System.out.println("Writing from "+i+"~"+toIndex+ "(of "+rows.size()+")"
					+ " length:"+estimateSize(exportObj));
			
			WriteBatch batch = db.batch();
			
			exportObj
			.forEach(row -> {
				DocumentReference doc = 
						db.collection("cell").document(row.get("cell").toString());
				batch.set(doc, row, SetOptions.merge());
			});
			batch.commit().get();
			System.out.println(" -> exported");
		}
		
		System.out.println("done");
	}
}
