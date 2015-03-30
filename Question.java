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
			
			Map<String, String> entityList = new TreeMap<String, String>();
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

			//print results
			if (!interactive){
				if (entityList.isEmpty()){
					System.out.println("it seems no one created [" + userQuery + "]!!!");
				} else {
					int i = 0;
					for(Entry<String, String> elem : entityList.entrySet()){
						i++;
						System.out.println(i+". "+elem.getValue());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void parseResults (JSONArray queryResults, Map<String, String> entityList) {
		for (int j = 0; j < queryResults.size(); j++/*JSONObject result : authors*/) {
				JSONObject result = (JSONObject)queryResults.get(j);
				String name = JsonPath.read(result,"$.name").toString();
				ArrayList<String> creations = new ArrayList<String>();
				String type = null;
				
				if(JsonPath.read(result,"$.type").toString().contains("/book/author")){
					type = "Author";
					int count = StringUtils.countMatches(JsonPath.read(result,"$./book/author/works_written").toString(), "a:name");
					for(int i=0; i<count; i++){
						creations.add(JsonPath.read(result,"$./book/author/works_written.a:name["+i+"]").toString());
					}
				}

				if(JsonPath.read(result,"$.type").toString().contains("/organization/organization_founder")){
					type = "Businessperson";
					int count = StringUtils.countMatches(JsonPath.read(result,"$./organization/organization_founder/organizations_founded").toString(), "a:name");
					for(int i=0; i<count; i++){
						creations.add(JsonPath.read(result,"$./organization/organization_founder/organizations_founded.a:name["+i+"]").toString());
					}
				}

				//compressing their creations
				String creationStr = "";
				for(int i=0; i<creations.size(); i++){
					if(i==0){
						creationStr = creationStr +"<"+ creations.get(i) + ">";
					}else if(creations.size()>2 && i==creations.size()-1){
						creationStr = creationStr + ", and <" + creations.get(i) +">";
					}else if(creations.size()==2 && i==creations.size()-1){
						creationStr = creationStr + " and <" + creations.get(i) +">";
					}else{
						creationStr = creationStr + ", <" + creations.get(i) + ">";
					}
				}
				//for interactive opt
				String[] creationbook = new String[creations.size()];
				for(int l=0; l<creationbook.length; l++){
					creationbook[l] = creations.get(l);
				}
				entityList.put(name, name + " (as "+type+") created "+creationStr);
				if (interactive){
					printInteractive(name,type,creationbook);
				}
			}
	}
	private static void printInteractive(String name,String type,String[] creation ){
		 String Rowname0 = AlignFit(30,name+ ":");
	     System.out.printf("|" + Rowname0);
	     String Rowname01 = AlignFit(30,"As");
	     System.out.printf("|" + Rowname01);   
	     String Rowname02 = AlignFit(37,"Creation");
	     System.out.printf("|"+Rowname02 + "|" );  
	     System.out.println();
	     String Rowname03 = AlignFit(30,"");
	     System.out.printf("|"+ Rowname03);
	     System.out.printf(" "+"-------------------------------------------------------------------");
	     System.out.println();
	     for (int leng = 0;leng< creation .length;leng++){
	        	String Rowname1 = AlignFit(30,"");
	            System.out.printf("|" + Rowname1);
	            String type1;
	            if (leng == 0) {
	            type1 = AlignFit(30,type);}
	            else{
	            	type1 = AlignFit(30,"");
	            }
	            System.out.printf("|"+ type1);
	            String creation1 = AlignFit(37,creation[leng]);
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