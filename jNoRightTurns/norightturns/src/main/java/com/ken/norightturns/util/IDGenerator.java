package com.ken.norightturns.util;

public class IDGenerator {
	private long idAccum = 0;
	public long get() {
		long id = idAccum;
		idAccum++;
		return id;
	}
}