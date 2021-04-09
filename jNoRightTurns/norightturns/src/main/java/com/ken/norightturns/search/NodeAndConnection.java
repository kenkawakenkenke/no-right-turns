package com.ken.norightturns.search;

import com.ken.norightturns.ConnectionType;

public class NodeAndConnection {
	public final long node;
	public final ConnectionType connection;
	public NodeAndConnection(long node, ConnectionType connection) {
		this.node = node;
		this.connection = connection;
	}
	public NodeAndConnection(long node) {
		this(node, null);
	}
}