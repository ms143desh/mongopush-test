package com.mongodb.mongopush.events;

public class OplogAppliedLagEvent {

	private boolean oplogAppliedLag;

	public OplogAppliedLagEvent(boolean oplogAppliedLag) {
		this.oplogAppliedLag = oplogAppliedLag;
	}
	
	public boolean isOplogAppliedLag() {
		return oplogAppliedLag;
	}
	
}
