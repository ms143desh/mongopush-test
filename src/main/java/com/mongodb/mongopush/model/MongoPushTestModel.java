package com.mongodb.mongopush.model;

import java.util.List;

import com.mongodb.mongopush.MongopushOptions.IncludeOption;

public class MongoPushTestModel {

	private List<MongoPushTestEvent> mongoPushTestEvents;
	private IncludeOption[] includeOptions;
	private String pocdriveArguments;
	private String populateDataArguments;
	private ReplaceDataArgumentsModel replaceDataArguments;
	private String idAsDocumentArguments;
	private String uniqueIndexArguments;
	private String deleteDocumentArguments;
	private String updateDocumentArguments;
	private long testInitialDocumentCount;
	
	public List<MongoPushTestEvent> getMongoPushTestEvents() {
		return mongoPushTestEvents;
	}
	public void setMongoPushTestEvents(List<MongoPushTestEvent> mongoPushTestEvents) {
		this.mongoPushTestEvents = mongoPushTestEvents;
	}
	public IncludeOption[] getIncludeOptions() {
		return includeOptions;
	}
	public void setIncludeOptions(IncludeOption[] includeOptions) {
		this.includeOptions = includeOptions;
	}
	public String getPocdriveArguments() {
		return pocdriveArguments;
	}
	public void setPocdriveArguments(String pocdriveArguments) {
		this.pocdriveArguments = pocdriveArguments;
	}
	public String getPopulateDataArguments() {
		return populateDataArguments;
	}
	public void setPopulateDataArguments(String populateDataArguments) {
		this.populateDataArguments = populateDataArguments;
	}
	public ReplaceDataArgumentsModel getReplaceDataArguments() {
		return replaceDataArguments;
	}
	public void setReplaceDataArguments(ReplaceDataArgumentsModel replaceDataArguments) {
		this.replaceDataArguments = replaceDataArguments;
	}
	public String getIdAsDocumentArguments() {
		return idAsDocumentArguments;
	}
	public void setIdAsDocumentArguments(String idAsDocumentArguments) {
		this.idAsDocumentArguments = idAsDocumentArguments;
	}
	public String getUniqueIndexArguments() {
		return uniqueIndexArguments;
	}
	public void setUniqueIndexArguments(String uniqueIndexArguments) {
		this.uniqueIndexArguments = uniqueIndexArguments;
	}
	public String getDeleteDocumentArguments() {
		return deleteDocumentArguments;
	}
	public void setDeleteDocumentArguments(String deleteDocumentArguments) {
		this.deleteDocumentArguments = deleteDocumentArguments;
	}
	public String getUpdateDocumentArguments() {
		return updateDocumentArguments;
	}
	public void setUpdateDocumentArguments(String updateDocumentArguments) {
		this.updateDocumentArguments = updateDocumentArguments;
	}
	public long getTestInitialDocumentCount() {
		return testInitialDocumentCount;
	}
	public void setTestInitialDocumentCount(long testInitialDocumentCount) {
		this.testInitialDocumentCount = testInitialDocumentCount;
	}
	
}
