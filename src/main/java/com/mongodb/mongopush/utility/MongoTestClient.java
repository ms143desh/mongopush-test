package com.mongodb.mongopush.utility;

import static com.mongodb.client.model.Filters.regex;
import static com.mongodb.mongopush.constants.MongoPushConstants.*;
import static com.mongodb.mongopush.constants.MongoPushConstants.CONFIG;
import static com.mongodb.mongopush.constants.MongoPushConstants.LOCAL;
import static com.mongodb.mongopush.constants.MongoPushConstants.UNIQUE_FIELD;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.bson.BsonUndefined;
import org.bson.Document;
import org.bson.UuidRepresentation;
import org.bson.conversions.Bson;
import org.bson.types.Binary;
import org.bson.types.Decimal128;
import org.bson.types.MaxKey;
import org.bson.types.MinKey;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.mongodb.BasicDBObject;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCommandException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

public class MongoTestClient {
	
	@Autowired
	private FakerService fakerService;
	
	private static Logger logger = LoggerFactory.getLogger(MongoTestClient.class);
	
	private String name;
	private ConnectionString connectionString;

	private MongoClientSettings mongoClientSettings;
	private List<String> databaseNameList;
	private List<String> databasesNotToDelete = Arrays.asList(ADMIN, CONFIG, LOCAL);;

	private MongoClient mongoClient;
	
	public MongoTestClient(String name, String clusterUri) {
		this.name = name;
		this.connectionString = new ConnectionString(clusterUri);
	}

	public ConnectionString getConnectionString() {
		return connectionString;
	}

	@PostConstruct
	public void init() {
		mongoClientSettings = MongoClientSettings.builder()
				.applyConnectionString(connectionString)
				.uuidRepresentation(UuidRepresentation.STANDARD)
				.build();
		mongoClient = MongoClients.create(mongoClientSettings);
	}
	
	public void populateData(int numDbs, int collectionsPerDb, int docsPerCollection, boolean uniqueIndex, boolean staticContent) {
		List<Document> docsBuffer = new ArrayList<>(docsPerCollection);
		for (int dbNum = 0; dbNum < numDbs; dbNum++) {
			String dbName = DB + dbNum;
			if(uniqueIndex)
			{
				dbName += "unique";
			}
			MongoDatabase db = mongoClient.getDatabase(dbName);
			for (int collNum = 0; collNum < collectionsPerDb; collNum++) {
				String collName = COL + collNum;
				MongoCollection<Document> coll = db.getCollection(collName);
				
				if(uniqueIndex)
				{
					IndexOptions indexOptions = new IndexOptions().unique(true);
				    String resultCreateIndex = coll.createIndex(Indexes.ascending(UNIQUE_FIELD), indexOptions);
				    logger.info(String.format("Unique index created: {}", resultCreateIndex));
				}
				
				for (int docNum = 0; docNum < docsPerCollection; docNum++) {
					if(staticContent)
					{
						docsBuffer.add(createStaticDocument(docNum));
					}
					else
					{
						docsBuffer.add(createDocument(docNum, false));
					}
				}
				coll.insertMany(docsBuffer);
				docsBuffer.clear();
			}
		}
	}
	
	public void deleteDocuments(int numDbs, int collectionsPerDb, int docsPerCollection)
	{
		for (int dbNum = 0; dbNum < numDbs; dbNum++) {
			String dbName = DB + dbNum;
			MongoDatabase db = mongoClient.getDatabase(dbName);
			for (int collNum = 0; collNum < collectionsPerDb; collNum++) {
				String collName = COL + collNum;
				MongoCollection<Document> coll = db.getCollection(collName);
				for(int i=0;i<docsPerCollection;i++)
				{
					Bson condition = Filters.eq(UNDERSCORE_ID, i);
					coll.findOneAndDelete(condition);
				}
			}
		}
	}
	
	public void updateDocuments(int numDbs, int collectionsPerDb, int docsPerCollection)
	{
		for (int dbNum = 0; dbNum < numDbs; dbNum++) {
			String dbName = DB + dbNum;
			MongoDatabase db = mongoClient.getDatabase(dbName);
			for (int collNum = 0; collNum < collectionsPerDb; collNum++) {
				String collName = COL + collNum;
				MongoCollection<Document> coll = db.getCollection(collName);
				for(int i=0;i<docsPerCollection;i++)
				{
					Bson condition = Filters.eq(UNDERSCORE_ID, i);
					
					BasicDBObject basicDBObject = new BasicDBObject();
					basicDBObject.append("$set", new BasicDBObject("added_field", "Text for new added field"));
					coll.findOneAndUpdate(condition, basicDBObject);
				}
			}
		}
	}
	
	public boolean matchRefetchCollection(int numDbs, int collectionsPerDb, int docsPerCollection)
	{
		MongoDatabase db = mongoClient.getDatabase(DB_UNDERSCORE_MONGO_PUSH);
		MongoCollection<Document> coll = db.getCollection(COLL_REFETCH);
		boolean refetchMatched = true;
		dbLabel: for (int dbNum = 0; dbNum < numDbs; dbNum++) {
			String dbName = DB + dbNum;
			for (int collNum = 0; collNum < collectionsPerDb; collNum++) {
				String collName = COL + collNum;
				String srcNamespace = dbName.concat(DOT).concat(collName);
				for(int i=0;i<docsPerCollection;i++)
				{
					Bson idFilter = Filters.eq(ID, i);
				    Bson srcNamespaceFilter = Filters.eq(SRC_NAMESPACE, srcNamespace);
				    FindIterable<Document> findIterable = coll.find(Filters.and(srcNamespaceFilter, idFilter));
				    MongoCursor<Document> mongoCursor = findIterable.iterator();
				    if(!mongoCursor.hasNext())
				    {
				    	refetchMatched = false;
				    	break dbLabel;
				    }
				    while(mongoCursor.hasNext())
				    {
				    	Document document = mongoCursor.next();
				    	String destNamespace = (String) document.get(DEST_NAMESPACE);
				    	if(!srcNamespace.equals(destNamespace))
				    	{
				    		refetchMatched = false;
				    		break dbLabel;
				    	}
				    }
				}
			}
		}
		return refetchMatched;
	}
	
	public void populateDataForDatabase(String dbName, String collName, int docsPerCollection, boolean idAsDocument) {
		List<Document> docsBuffer = new ArrayList<>(docsPerCollection);
			MongoDatabase db = mongoClient.getDatabase(dbName);
				MongoCollection<Document> coll = db.getCollection(collName);
				for (int docNum = 0; docNum < docsPerCollection; docNum++) {
					docsBuffer.add(createDocument(docNum, idAsDocument));
				}
				coll.insertMany(docsBuffer);
				docsBuffer.clear();
	}
	
	public long replaceDocuments(String dbName, String collName, String filter)
	{
		MongoDatabase mongoDatabase = mongoClient.getDatabase(dbName);
		MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(collName);
		Bson query = Document.parse(filter);
        Bson updates = Updates.combine( Updates.addToSet("replaced_document", "Recently replaced"),Updates.currentTimestamp("lastUpdated"));
        UpdateResult result = mongoCollection.updateMany(query, updates);
        return result.getModifiedCount();
	}
	
	private Document createStaticDocument(int docNum)
	{
		Document document = new Document();
		document.append(UNDERSCORE_ID, docNum);
		addStaticDocumentFields(document);
		return document;
	}
	
	private Document createDocument(int docNum, boolean idAsDocument)
	{
		Document document = new Document();
		UUID uuid = UUID.randomUUID();
		if(idAsDocument)
		{
			Document idDocument = new Document();
			idDocument.append("uuid", uuid);
			idDocument.append("created", fakerService.getRandomDate());
			document.append(UNDERSCORE_ID, idDocument);
		}
		else
		{
			document.append(UNDERSCORE_ID, uuid.toString());
		}
		document.append(UNIQUE_FIELD, docNum);
		addDocumentFields(document);
		return document;
	}
	
	private void addDocumentFields(Document document)
	{
		Random random = new Random();
		document.append("fld0", random.nextLong());
		document.append("fld1", fakerService.getRandomDate());
		document.append("fld2", "sit amet. Lorem ipsum dolor");
		document.append("fld3", fakerService.getRandomText());
		document.append("fld4", fakerService.getRandomText());
		document.append("fld5", fakerService.getRandomDate());
		document.append("long_field", random.nextLong());
		document.append("boolean_field", random.nextBoolean());
		document.append("double_field", random.nextDouble());
		document.append("document_field", new Document("uuid", UUID.randomUUID()));
		String[] cars_array = {"Volvo", "BMW", "Honda"};
		document.append("array_field", Arrays.asList(cars_array));
		
		Map<String, String> documentRandomMap = new HashMap<String, String>();
		documentRandomMap.put("text_1", fakerService.getRandomText());
		documentRandomMap.put("text_2", fakerService.getRandomText());
		document.append("map_field", documentRandomMap);
		document.append("binary_field", new Binary(fakerService.getRandomText().getBytes()));
		document.append("objectId_field", new ObjectId());
		document.append("undefined_field", new BsonUndefined());
		document.append("null_field", null);
		document.append("regex_field", Pattern.compile("^.* random regex (.*)"));
		document.append("integer_field", random.nextInt());
		document.append("timestamp_field", new Timestamp(new Date().getTime()));
		document.append("decimal128_field", new Decimal128(random.nextLong()));
		document.append("minkey_field", new MinKey());
		document.append("maxkey_field", new MaxKey());
	}
	
	private void addStaticDocumentFields(Document document)
	{
		document.append("fld0", 28928392839L);
		document.append("fld1", "sit amet. Lorem ipsum dolor");
		document.append("long_field", 28928392839L);
		document.append("boolean_field", false);
		document.append("double_field", 28392839.23902390);
		String[] cars_array = {"Volvo", "BMW", "Honda"};
		document.append("array_field", Arrays.asList(cars_array));
		
		Map<String, String> documentRandomMap = new HashMap<String, String>();
		documentRandomMap.put("text_1", "This is document map text");
		documentRandomMap.put("text_2", "This text is for static document");
		document.append("map_field", documentRandomMap);
		document.append("null_field", null);
		document.append("regex_field", Pattern.compile("^.* random regex (.*)"));
		document.append("integer_field", 98982992);
		document.append("decimal128_field", 121212.121212);
	}
	
	public List<String> getAllDatabases() {
		MongoCollection<Document> databasesColl = mongoClient.getDatabase("config").getCollection("databases");
		FindIterable<Document> databases = databasesColl.find();
		List<String> databasesList = new ArrayList<String>();

		for (Document database : databases) {
			String databaseName = database.getString("_id");
			databasesList.add(databaseName);
		}
		return databasesList;
	}
	
	public void dropAllDatabases() {
		for (String dbName : getAllDatabases()) {
			if (!dbName.equals("admin")) {
				logger.debug(name + " dropping " + dbName);
				try {
					mongoClient.getDatabase(dbName).drop();
				} catch (MongoCommandException mce) {
					logger.warn("Drop failed, brute forcing.", mce);
					dropForce(dbName);
				}

			}
		}
	}
	
	private void dropForce(String dbName) {
		DeleteResult r = mongoClient.getDatabase("config").getCollection("collections")
				.deleteMany(regex("_id", "^" + dbName + "\\."));
		logger.debug(String.format("Force deleted %s config.collections documents", r.getDeletedCount()));
		r = mongoClient.getDatabase("config").getCollection("chunks").deleteMany(regex("ns", "^" + dbName + "\\."));
		logger.debug(String.format("Force deleted %s config.chunks documents", r.getDeletedCount()));
	}
	
	public List<String> getAllDatabaseNames(){
		databaseNameList = new ArrayList<String>();
	    MongoCursor<String> dbsCursor = mongoClient.listDatabaseNames().iterator();
	    while(dbsCursor.hasNext()) {
	    	databaseNameList.add(dbsCursor.next());
	    }
	    return databaseNameList;
	}
	
	public void dropAllDatabasesByName() {
		for (String databaseName : getAllDatabaseNames()) {
			if (!databasesNotToDelete.contains(databaseName)) {
				logger.debug(name + " dropping " + databaseName);
				dropDatabase(databaseName);
			}
		}
	}
	
	public void dropDatabase(String databaseName) {
		
		MongoDatabase mongoDatabase = mongoClient.getDatabase(databaseName);
		if(mongoDatabase != null && mongoDatabase.getName() != null)
		{
			logger.info("MongoDB Database found - {}", databaseName);
			mongoDatabase.drop();
		}
	}
	
}
