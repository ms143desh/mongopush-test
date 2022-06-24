package com.mongodb.mongopush.events;

public class OplogTotalDocInsertedEvent {

	private boolean oplogTotalDocInserted;

	public OplogTotalDocInsertedEvent(boolean oplogTotalDocInserted) {
		this.oplogTotalDocInserted = oplogTotalDocInserted;
	}
	
	public boolean isOplogTotalDocInserted() {
		return oplogTotalDocInserted;
	}
	
}
