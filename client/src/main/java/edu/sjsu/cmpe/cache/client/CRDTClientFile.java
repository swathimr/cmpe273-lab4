package edu.sjsu.cmpe.cache.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

public class CRDTClientFile {
	
	private final ConcurrentHashMap<String, String> mapResult = new ConcurrentHashMap<String, String>();
	private Collection<CacheServiceInterface> nodelist = new ArrayList<CacheServiceInterface>();
	private final CountDownLatch wait = new CountDownLatch(3);

	public CRDTClientFile(Collection<String> serverNodeValues) {
		Collection<String> nodeList = serverNodeValues;
		for (String value : nodeList) {
			DistributedCacheService cache = new DistributedCacheService(value);
			nodelist.add(cache);
		}
	}

	public String get(long keyValue) {
		
		HashMap<String, Integer> mapVal = new HashMap<String, Integer>();
		String outputString = null,result = null;
		
		for (CacheServiceInterface nodeval : nodelist) {
			nodeval.getAsynchronous(keyValue, mapResult, wait);
		}	
		try {
			wait.await();
			for (String node : mapResult.keySet())
			{
				result = mapResult.get(node);
				if (mapVal.containsKey(result)) 
				{
					mapVal.put(result,mapVal.get(result) + 1);
					outputString = result;
					System.out.println("Value returning is"+outputString);
				} 
				else
				{
					mapVal.put(result, 1);
				}
			}
		} catch (Exception e) {
			System.out.println("Error waiting");
		}
		
		//repir
		for (String node : mapResult.keySet()) {
			result = mapResult.get(node);
			if (!result.equals(outputString)) {
				System.out.println("inread repair for loop.Value mismatch");
				System.out.println("Read Repair::: "+node + "value" + mapResult.get(node));
				DistributedCacheService cache = new DistributedCacheService(node);
				System.out.println("Key and returning values are" +keyValue+"::"+outputString);
				cache.put(keyValue, outputString);
			}
		}
		return outputString;
	}
	
	public void put(long keyValue, String value) {
		
		Collection<CacheServiceInterface> delete = new ArrayList<CacheServiceInterface>();
		System.out.println("in PUT call");
		
		//for loop for asynchronous put
		for (CacheServiceInterface node : nodelist) {
			node.asynchronousPut(Long.valueOf(keyValue), value);
		}
		
		int tracker=0,statusResp = 0;
		for (CacheServiceInterface node1 : nodelist) {
			statusResp=node1.getStatusCode();
			System.out.println("obtained response value iss:::"+statusResp);
			if (statusResp == 200) {
				tracker++;
				System.out.println("tracker value is increamented.updated tracker value is::"+tracker);
				delete.add(node1);
				System.out.println("code obtained is 200 and node is deleted");
			}
		}
		if (tracker < 2) {
			System.out.println("my tracking values are lesser than 2 so im inside the loop");
			for (CacheServiceInterface nodeDel : delete) {
				nodeDel.delete(keyValue);
				System.out.println("Yup the cache values are deleted");
			}
		}

	}


}
