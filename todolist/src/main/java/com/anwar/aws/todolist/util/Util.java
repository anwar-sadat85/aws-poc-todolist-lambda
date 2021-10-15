package com.anwar.aws.todolist.util;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.LambdaLogger;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeAction;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.AttributeValueUpdate;
import software.amazon.awssdk.services.dynamodb.model.ComparisonOperator;
import software.amazon.awssdk.services.dynamodb.model.Condition;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemResponse;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse;

public class Util {
	
	public static QueryResponse queryTableWithLSI(DynamoDbClient ddb, String tableName, String partitionKeyName, String partitionKeyVal,
			String partitionAlias, String indexName,String sortKeyName,String sortKeyValue, String sortKeyAlias) {
		
		Condition equalsUserIdCondition = Condition.builder()
                .comparisonOperator(ComparisonOperator.EQ)
                .attributeValueList(
                    AttributeValue.builder()
                            .s(partitionKeyVal)
                            .build()
                ).build();
		Condition equalsDateCondition = Condition.builder()
                .comparisonOperator(ComparisonOperator.EQ)
                .attributeValueList(
                    AttributeValue.builder()
                        .s(sortKeyValue)
                        .build()
                ).build();
		Map<String, Condition> keyConditions = new HashMap<>();
		keyConditions.put(partitionKeyName, equalsUserIdCondition);
		keyConditions.put(sortKeyName, equalsDateCondition);
		QueryRequest queryReq = QueryRequest.builder()
                .tableName(tableName)
                .keyConditions(keyConditions)
                .indexName(indexName)
                .build();
		try {
			QueryResponse response = ddb.query(queryReq);
			return response;
		} catch (DynamoDbException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
		return null;
	}
	
	public static PutItemResponse addTask(DynamoDbClient ddb, String tableName, String userId, String taskId,String taskName, String taskDate,LambdaLogger logger) {
		try {
		HashMap<String,AttributeValue> itemValues = new HashMap<String,AttributeValue>();

        // Add all content to the table
        itemValues.put("userId", AttributeValue.builder().s(userId).build());
        itemValues.put("taskId", AttributeValue.builder().s(taskId).build());
        itemValues.put("taskName", AttributeValue.builder().s(taskName).build());
        itemValues.put("taskDate", AttributeValue.builder().s(taskDate).build());
        
		PutItemRequest request = PutItemRequest.builder()
                .tableName(tableName)
                .item(itemValues)
                .build();
		
		PutItemResponse response = ddb.putItem(request);
		
		return response;
		}catch(Exception ex) {
			logger.log("Exception in put item -- "+ex);
		}
		return null;
	}
	
	public static UpdateItemResponse updateTask(DynamoDbClient ddb, String tableName, String userId, String taskId,String taskName, LambdaLogger logger) {
		try {
			HashMap<String,AttributeValue> itemKey = new HashMap<String,AttributeValue>();
			itemKey.put("userId",AttributeValue.builder().s(userId).build());
			itemKey.put("taskId",AttributeValue.builder().s(taskId).build());
			HashMap<String,AttributeValueUpdate> updatedValues = new HashMap<String,AttributeValueUpdate>();
			updatedValues.put("taskName", AttributeValueUpdate.builder()
	                .value(AttributeValue.builder().s(taskName).build())
	                .action(AttributeAction.PUT)
	                .build());
			UpdateItemRequest request = UpdateItemRequest.builder()
	                .tableName(tableName)
	                .key(itemKey)
	                .attributeUpdates(updatedValues)
	                .build();
			return ddb.updateItem(request);
		}catch(Exception ex) {
			logger.log("Exception in update item -- "+ex);
		}
		return null;
	}
	
	public static DeleteItemResponse deleteTask(DynamoDbClient ddb, String tableName, String userId, String taskId,LambdaLogger logger) {
		try {
			HashMap<String,AttributeValue> keyToGet = new HashMap<String,AttributeValue>();
			keyToGet.put("userId",AttributeValue.builder().s(userId).build());
			keyToGet.put("taskId",AttributeValue.builder().s(taskId).build());
			
			DeleteItemRequest deleteReq = DeleteItemRequest.builder()
	                .tableName(tableName)
	                .key(keyToGet)
	                .build();
			return ddb.deleteItem(deleteReq);
		}catch(Exception ex) {
			logger.log("Exception in delete item -- "+ex);
		}
		return null;
	}
}
