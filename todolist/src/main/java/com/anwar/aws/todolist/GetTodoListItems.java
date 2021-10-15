package com.anwar.aws.todolist;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.anwar.aws.todolist.model.Task;
import com.anwar.aws.todolist.util.Util;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

public class GetTodoListItems implements RequestStreamHandler {
	private final DynamoDbClient dynamoDbClient;
	
	public GetTodoListItems() {
		Region region = Region.US_EAST_1;
		dynamoDbClient = DynamoDbClient.builder()
                .region(region)
                .build();
	}
	@Override
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
	    	logger.log("Input -- "+event.toJSONString());
	    	if (event.get("pathParameters") != null) {
	            JSONObject pps = (JSONObject)event.get("pathParameters");
	            if ( pps.get("userId") != null && pps.get("taskDate") != null) {
	                String userId = (String)pps.get("userId");
	                String taskDate = (String)pps.get("taskDate");
	                QueryResponse result = Util.queryTableWithLSI(dynamoDbClient, tableName, "userId", userId, "#a","taskDate-index","taskDate",taskDate,"#b");
	        		
	        		System.out.println("Result count -- "+ result.count());
	        		JSONArray taskArray = new JSONArray();
	        		for(Map<String, AttributeValue> item : result.items()) {
	        			Task task = new Task();
	        			for(String key : item.keySet()) {
	        				switch(key) {
	        					case "userId":{
	        						task.setUserId(item.get(key).s());
	        						break;
	        					}
	        					case "taskName":{
	        						task.setTaskTitle(item.get(key).s());
	        						break;
	        					}
	        					case "taskDate":{
	        						task.setTaskDate(item.get(key).s());
	        						break;
	        					}
	        					case "taskId":{
	        						task.setTaskId(item.get(key).s());
	        						break;
	        					}
	        					
	        				}
	        			}
	        			taskArray.add(task.toJSON());
	        		}
	        		responseBody.put("status", "success");
	        		responseBody.put("results", taskArray);
	        		logger.log(responseBody.toJSONString());
	        		responseHeaders.put("Access-Control-Allow-Headers", "Content-Type");
	        		responseHeaders.put("Access-Control-Allow-Origin", "*");
	        		responseHeaders.put("Access-Control-Allow-Methods", "OPTIONS,GET");
	        		responseJson.put("isBase64Encoded", false);
	    			responseJson.put("statusCode", "200");
	    			responseJson.put("body", responseBody.toString());
	    			responseJson.put("headers", responseHeaders);
	            }
	        }

	    }catch(Exception e) {
	    	
	    	logger.log("Exception"+e);
	    	responseBody.put("status", "failure");
	    	responseJson.put("isBase64Encoded", false);
	    	responseHeaders.put("Access-Control-Allow-Headers", "Content-Type");
    		responseHeaders.put("Access-Control-Allow-Origin", "*");
    		responseHeaders.put("Access-Control-Allow-Methods", "OPTIONS,GET");
			responseJson.put("statusCode", "500");
			responseJson.put("headers", responseHeaders);
			responseJson.put("body", responseBody.toString());
	    }
	    
	    OutputStreamWriter writer = new OutputStreamWriter(output, "UTF-8");
	    writer.write(responseJson.toJSONString());
	    writer.close();
	}

}
