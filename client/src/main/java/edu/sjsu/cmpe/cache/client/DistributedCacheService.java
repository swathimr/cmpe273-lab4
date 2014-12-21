package edu.sjsu.cmpe.cache.client;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;


/**
 * Distributed cache service
 * 
 */
public class DistributedCacheService implements CacheServiceInterface {
	
	
    private final String cacheServerUrl;
    private int responseStatusValue=0;
    private Future<HttpResponse<JsonNode>> future=null;

    public DistributedCacheService(String serverUrl) {
        this.cacheServerUrl = serverUrl;
    }

    /**
     * @see edu.sjsu.cmpe.cache.client.CacheServiceInterface#get(long)
     */
    @Override
    public String get(long key) {
        HttpResponse<JsonNode> response = null;
        try {
            response = Unirest.get(this.cacheServerUrl + "/cache/{key}")
                    .header("accept", "application/json")
                    .routeParam("key", Long.toString(key)).asJson();
        } catch (UnirestException e) {
            System.err.println(e);
        }
        String value = response.getBody().getObject().getString("value");

        return value;
    }

    /**
     * @see edu.sjsu.cmpe.cache.client.CacheServiceInterface#put(long,
     *      java.lang.String)
     */
    @Override
    public void put(long key, String value) {
        HttpResponse<JsonNode> response = null;
        try {
            response = Unirest
                    .put(this.cacheServerUrl + "/cache/{key}/{value}")
                    .header("accept", "application/json")
                    .routeParam("key", Long.toString(key))
                    .routeParam("value", value).asJson();
        } catch (UnirestException e) {
            System.err.println(e);
        }

        if (response.getCode() != 200) {
            System.out.println("Failed to add to the cache.");
        }
    }
    
    //added
        
    @Override
    public void delete(long key) {
        HttpResponse<JsonNode> response = null;
        try {
            response = Unirest
                    .delete(this.cacheServerUrl + "/cache/{key}")
                    .header("accept", "application/json")
                    .routeParam("key", Long.toString(key)).asJson();
        } catch (UnirestException e) {
            System.err.println(e);
        }
        if (response.getCode() != 200) {
            System.out.println("Delete operation");
        }
    }
    
    @Override
    public void getAsynchronous(long key,final ConcurrentHashMap<String,String> map,final CountDownLatch latch)
    {	
    	future = Unirest.get(this.cacheServerUrl + "/cache/{key}")
	    	.header("accept", "application/json")
	        .routeParam("key", Long.toString(key))
	        .asJsonAsync(new Callback<JsonNode>() 
	        {
    			public void failed(UnirestException e) {
    			System.out.println("The request has failed");   
    			}
    			
    			public void completed(HttpResponse<JsonNode> valueResp)
    			{
    			  String value="0";
    			  responseStatusValue= valueResp.getCode();
    			  System.out.println("status value"+responseStatusValue);
    			  if (responseStatusValue==200){
    			  value = valueResp.getBody().getObject().getString("value");
    			  System.out.println("value from aync get"+value);
    			}
    			  map.put(cacheServerUrl, value);
    			  latch.countDown();    			        
    		 }
    		public void cancelled()
    		 {
    		   System.out.println("Request cancelled");
    		  }
    		});   
    }
    
    
    @Override
    public void asynchronousPut(long key, String value) {
    	
    	 future = Unirest.put(this.cacheServerUrl + "/cache/{key}/{value}")
	    			.header("accept", "application/json")
	                .routeParam("key", Long.toString(key))
	                .routeParam("value", value)
    			    .asJsonAsync(new Callback<JsonNode>() {

    			    public void failed(UnirestException e) {
    			    	System.out.println("Request failure");
    			    }
    			    public void completed(HttpResponse<JsonNode> response) {
    			    	responseStatusValue = response.getCode() ;
    			    	System.out.println("status value"+responseStatusValue);
    			         Map<String, List<String>> header = response.getHeaders();
    			         JsonNode body = response.getBody();
    			         InputStream rawBody = response.getRawBody();
    			    }
    			    public void cancelled() {
    			        System.out.println("request cancelled");
    			    }
    			});   
    	}

    
    @Override
    public int getStatusCode()
    {
    	int code=0;
    	try
    	{
    	 HttpResponse<JsonNode> response=future.get(200,TimeUnit.MILLISECONDS);
    	 code=response.getCode();
    	 System.out.println("Code obtained is"+code);
       	}
     catch (Exception exception) 
    	{
    	 System.err.println("in getstatuscode"+exception);
    	 future.cancel(true); 
    	 }
    	 return code;
    }   
}
