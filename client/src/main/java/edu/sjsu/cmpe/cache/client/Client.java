package edu.sjsu.cmpe.cache.client;

import java.util.ArrayList;
import java.util.Collection;

public class Client {

    public static void main(String[] args) throws Exception {
        System.out.println("Starting Cache Client...");
        
        
        Collection<String> listUrl= new ArrayList<String>();
        listUrl.add("http://localhost:3000");
        listUrl.add("http://localhost:3001");
        listUrl.add("http://localhost:3002");
    	String value=null;
    	
    	/*Adding value 1->a using put*/
    	CRDTClientFile crdtClient= new CRDTClientFile(listUrl);
    	crdtClient.put(1, "a");
    	System.out.println("Added 1st value in the put ::::::: 1->a");
    	    	
       	try 
       	{    
       		Thread.sleep(30000);                
		} 
       	catch(InterruptedException ex)
       	{
		    Thread.currentThread().interrupt();
		}
       	
       	/*Adding value 1->b using put*/
       	CRDTClientFile crdtClient1= new CRDTClientFile(listUrl);
    	crdtClient1.put(1, "b");
    	System.out.println("Added 2nd value in the put ::::::: 1->b");
    	try 
    	{
		    Thread.sleep(30000);                
		} 
    	catch(InterruptedException ex) 
    	{
		    Thread.currentThread().interrupt();
		}
    	 	
    	
    	/*Fetching value*/   	
    	CRDTClientFile crdtClient2= new CRDTClientFile(listUrl);
    	value=crdtClient2.get(1);
    	System.out.println("value obtained from getting key1 is:::::::::: "+value);
        System.out.println("Existing Cache Client...");
    }

}
