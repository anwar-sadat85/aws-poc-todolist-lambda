package com.anwar.aws.todolist;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.anwar.aws.todolist.util.Util;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemResponse;

public class DeleteTodoItem implements RequestStreamHandler {
	private final DynamoDbClient dynamoDbClient;
	
	public DeleteTodoItem() {
		Region region = Region.US_EAST_1;
		dynamoDbClient = DynamoDbClient.builder()
                .region(region)
                .build();
	}
	public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
		LambdaLogger logger = context.getLogger();
		JSONParser parser = new JSONParser();
	    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
	    JSONObject responseBody = new JSONObject();
	    JSONObject responseHeaders = new JSONObject();
	    JSONObject responseJson = new JSONObject();
	    String tableName = System.getenv("tableName");
	    try {
	    	JSONObject event = (JSONObject)parser.parse(reader);
	    	String taskId = null;
	    	String userId = null;
	    	logger.log("Input -- "+event.toJSONString());
	    	if (event.get("pathParameters") != null) {
	            JSONObject pps = (JSONObject)event.get("pathParameters");
	            if ( pps.get("userId") != null) {
	            	userId = (String) pps.get("userId");
	            }
	            if ( pps.get("taskId") != null) {
	            	taskId = (String) pps.get("taskId");
	            }
	            
	    	}
	    	if(taskId != null && userId != null) {
	    		DeleteItemResponse response = Util.deleteTask(dynamoDbClient, tableName, userId, taskId, logger);
	    		responseBody.put("status", "success");
        		logger.log(responseBody.toJSONString());
        		responseHeaders.put("Access-Control-Allow-Headers", "Content-Type");
        		responseHeaders.put("Access-Control-Allow-Origin", "*");
        		responseHeaders.put("Access-Control-Allow-Methods", "OPTIONS,GET,PUT,POST,DELETE");
        		responseJson.put("isBase64Encoded", false);
    			responseJson.put("statusCode", "200");
    			responseJson.put("body", responseBody.toString());
    			responseJson.put("headers", responseHeaders);
	    	}else {
	    		logger.log("Bad Request Response");
	    		responseBody.put("status", "failure");
		    	responseJson.put("isBase64Encoded", false);
		    	responseHeaders.put("Access-Control-Allow-Headers", "Content-Type");
	    		responseHeaders.put("Access-Control-Allow-Origin", "*");
	    		responseHeaders.put("Access-Control-Allow-Methods", "OPTIONS,GET,PUT,POST,DELETE");
				responseJson.put("statusCode", "400");
				responseJson.put("headers", responseHeaders);
				responseJson.put("body", responseBody.toString());
	    	}
	    }catch(Exception e) {
	    	logger.log("Exception in PutTodoItem"+e);
	    	responseBody.put("status", "failure");
	    	responseJson.put("isBase64Encoded", false);
	    	responseHeaders.put("Access-Control-Allow-Headers", "Content-Type");
    		responseHeaders.put("Access-Control-Allow-Origin", "*");
    		responseHeaders.put("Access-Control-Allow-Methods", "OPTIONS,GET,PUT,POST,DELETE");
			responseJson.put("statusCode", "500");
			responseJson.put("headers", responseHeaders);
			responseJson.put("body", responseBody.toString());
	    }
	    
	    OutputStreamWriter writer = new OutputStreamWriter(output, "UTF-8");
	    writer.write(responseJson.toJSONString());
	    writer.close();
	}
}
