import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.jayway.jsonpath.JsonPath;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.*;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

public class Question {
	public boolean interactive = false;
	
	public void main(String userQuery,String key) {
		try {

			String accountKey = "AIzaSyD0e2ClA78Lkjr_cVKSqe6bbz133RA_LmM";
			String search = userQuery;
			
			//convert user query to correct format
			userQuery = userQuery.trim();
			userQuery = userQuery.toLowerCase();
			if(!userQuery.startsWith("who created ")) {
				System.out.println("Wrong question!!!");
				return;
			}
			
			if(userQuery.contains("?") && (userQuery.lastIndexOf('?') == userQuery.length()-1)) {
				userQuery = userQuery.substring(12, userQuery.length()-1);
			} else {
				userQuery = userQuery.substring(12);
			}


			HttpTransport httpTransport = new NetHttpTransport();
			HttpRequestFactory requestFactory = httpTransport.createRequestFactory();

			//query for books
			String author = "[{\"/book/author/works_written\":[{\"a:name\":null,\"name~=\":\"" + userQuery 
								+"\"}],\"id\":null,\"name\":null,\"type\":\"/book/author\"}]";

			GenericUrl url = new GenericUrl("https://www.googleapis.com/freebase/v1/mqlread");
			url.put("query", author);
			url.put("key", accountKey);
			HttpRequest request = requestFactory.buildGetRequest(url);
			HttpResponse httpResponse = request.execute();
			JSONParser parser = new JSONParser();
			JSONObject response = (JSONObject)parser.parse(httpResponse.parseAsString());
			JSONArray authors = (JSONArray)response.get("result");
			
			Map<String, ArrayList<String>> entityList = new TreeMap<String, ArrayList<String>>();
			parseResults (authors, entityList); 
			
			//query for organizations
			String business = "[{\"/organization/organization_founder/organizations_founded\":[{\"a:name\":null,\"name~=\":\"" 
									+ userQuery+"\"}],\"id\":null,\"name\":null,\"type\":\"/organization/organization_founder\"}]";

			url = new GenericUrl("https://www.googleapis.com/freebase/v1/mqlread");
			url.put("query", business);
			url.put("key", accountKey);
			request = requestFactory.buildGetRequest(url);
			httpResponse = request.execute();
			response = (JSONObject)parser.parse(httpResponse.parseAsString());
			JSONArray businessPersons = (JSONArray)response.get("result");
			
			parseResults (businessPersons, entityList); 

			//print for interactive opt
			if (interactive){
				System.out.println(" "+"--------------------------------------------------------------------------------------------------");
				
				String namespace = AlignFit(40,"");
				System.out.printf( "|" + namespace );
				String q = "Who created " + userQuery + "?";
				
				q = AlignFit(58,q);
				System.out.printf(q +"|");
				System.out.println();
				System.out.println(" "+"--------------------------------------------------------------------------------------------------");
			}

			//print results
			if (entityList.isEmpty()) {
				System.out.println("it seems no one created [" + userQuery + "]!!!");
			} else {
				if (!interactive){
					int i = 0;
					for(Entry<String, ArrayList<String>> elem : entityList.entrySet()){
						i++;
						ArrayList<String> set = elem.getValue();

						System.out.println(i+". "+ elem.getKey() + " (as "+set.get(set.size()-1)+") created "+ regOutput(set));
					}
				} else {
					for(Entry<String, ArrayList<String>> elem : entityList.entrySet()){
						ArrayList<String> set = elem.getValue();
						int len = set.size();
						String[] book = new String[len-1];
						
						for(int l=0; l<len-1; l++){
							book[l] = set.get(l);
						}
						printInteractive(elem.getKey(),set.get(len-1), book);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("\n");
	}

	private void parseResults (JSONArray queryResults, Map<String, ArrayList<String>> entityList) {
		for (int j = 0; j < queryResults.size(); j++) {
				JSONObject result = (JSONObject)queryResults.get(j);
				String name = JsonPath.read(result,"$.name").toString();
				ArrayList<String> creationList = new ArrayList<String>();
				String type = null;
				
				if(JsonPath.read(result,"$.type").toString().contains("/book/author")){
					type = "Author";
					int count = StringUtils.countMatches(JsonPath.read(result,"$./book/author/works_written").toString(), "a:name");
					for(int i=0; i<count; i++){
						creationList.add(JsonPath.read(result,"$./book/author/works_written.a:name["+i+"]").toString());
					}
				}

				if(JsonPath.read(result,"$.type").toString().contains("/organization/organization_founder")){
					type = "Businessperson";
					int count = StringUtils.countMatches(JsonPath.read(result,"$./organization/organization_founder/organizations_founded").toString(), "a:name");
					for(int i=0; i<count; i++){
						creationList.add(JsonPath.read(result,"$./organization/organization_founder/organizations_founded.a:name["+i+"]").toString());
					}
				}

				creationList.add(type); // append type at the end
				entityList.put(name, creationList);
			}
	}

	private static String regOutput (ArrayList<String> set) {
		String setStr = "";
		for(int i=0; i<set.size()-1; i++){
			if(i==0){
				setStr = setStr +"<"+ set.get(i) + ">";
			}else if(set.size()>=3 && i==set.size()-2){
				setStr = setStr + " and <" + set.get(i) +">";
			}else{
				setStr = setStr + ", <" + set.get(i) + ">";
			}
		}
		return (setStr);
	}

	private static void printInteractive(String name, String type, String[] creation ){
		 String row0 = AlignFit(30,name+ ":");
	     System.out.printf("|" + row0);
	     String row1 = AlignFit(30,"As");
	     System.out.printf("|" + row1);   
	     String row2 = AlignFit(37,"Creation");
	     System.out.printf("|"+row2 + "|" );  
	     System.out.println();
	     String row3 = AlignFit(30,"");
	     System.out.printf("|"+ row3);
	     System.out.printf(" "+"-------------------------------------------------------------------");
	     System.out.println();
	     for (int len = 0;len < creation.length; len++){
	        	String row = AlignFit(30,"");
	            System.out.printf("|" + row);
	            String type1;
	            if (len == 0) {
	            	type1 = AlignFit(30,type);}
	            else{
	            	type1 = AlignFit(30,"");
	            }
	            System.out.printf("|"+ type1);
	            String creation1 = AlignFit(37,creation[len]);
	            System.out.printf("|"+creation1 + "|" );
	            System.out.println();
	        }
	        System.out.println(" "+"--------------------------------------------------------------------------------------------------");
				
	}

	public static String AlignFit(int length, String string)   
    {   
        String str = string;   
        String c = " ";
        if (string.length() > length)   
            str = string.substring(0,length);   
        else  
            for (int i = 0; i < length - string.length(); i++)      
        str = str + c;   
        return str;   
    } 
	
}