package com.ken.norightturns.util;

public class IDGenerator {
	private int idAccum = 0;
	public int get() {
		int id = idAccum;
		idAccum++;
		return id;
	}
}