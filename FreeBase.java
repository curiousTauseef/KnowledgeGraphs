package edu.columbia.adb.freebase.utils;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.ExecutionException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * Created by diwakarmahajan on 3/18/15.
 */

public class FreeBase {
    private static Properties properties = new Properties();

    private static ArrayList<String> entitiesOfInterest = new ArrayList<String>(Arrays.asList("/people/person","/book/author","/film/actor","/tv/tv_actor","/organization/organization_founder","/business/board_member",
            "/sports/sports_league","/sports/sports_team","/sports/professional_sports_team","/people/deceased_person"));

    private static Map<String, Map<String,String>> mapEntities  = new HashMap<String, Map<String,String>>();


    public static void init(){
        //Person
        Map<String,String> map = new HashMap<String,String>();
        map.put("title","PERSON");
        map.put("date_of_birth","Birthday");
        map.put("place_of_birth","Place of Birth");
        map.put("sibling_s","Siblings");
        map.put("spouse_s","Spouses");
        // Siblings,
        // Spouses
        mapEntities.put("/people/person",map);

        ///people/deceased_person
        map=new HashMap<String,String>();
        map.put("title","PERSON");
        map.put("cause_of_death","Cause:");
        map.put("place_of_death","Died At:");
        map.put("date_of_death","Date of Death:");
        mapEntities.put("/people/deceased_person",map);


        ///organization/organization_founder
        map=new HashMap<String,String>();
        map.put("title","BUSINESS_PERSON");
        map.put("organizations_founded","Founded");
        mapEntities.put("/organization/organization_founder",map);

        ///business/board_member
        map=new HashMap<String,String>();
        map.put("title","BUSINESS_PERSON");
        map.put("","");
        mapEntities.put("/business/board_member",map);

        ///book/author
        map=new HashMap<String,String>();
        map.put("title","Author");
        map.put("works_written","Books");
        mapEntities.put("/book/author",map);




    }

    public static JSONObject doTopic(String topicId){
        try {
            properties.load(new FileInputStream("freebase.properties"));
            HttpTransport httpTransport = new NetHttpTransport();
            HttpRequestFactory requestFactory = httpTransport.createRequestFactory();
            JSONParser parser = new JSONParser();
            //topicId = "/en/bob_dylan";
            GenericUrl url = new GenericUrl("https://www.googleapis.com/freebase/v1/topic" + topicId);
            url.put("key", properties.get("API_KEY"));
            HttpRequest request = requestFactory.buildGetRequest(url);
            HttpResponse httpResponse = request.execute();
            JSONObject topic =  (JSONObject)parser.parse(httpResponse.parseAsString());
           // System.out.println(JsonPath.read(topic,"$.property['/type/object/name'].values[0].value").toString());
            return topic;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static String urlEncode(String s){
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static JSONArray doSearch(String query, int limit) {
        try {
            properties.load(new FileInputStream("freebase.properties"));
            HttpTransport httpTransport = new NetHttpTransport();
            HttpRequestFactory requestFactory = httpTransport.createRequestFactory();
            JSONParser parser = new JSONParser();
            GenericUrl url = new GenericUrl("https://www.googleapis.com/freebase/v1/search");
            url.put("query", urlEncode(query));
            //url.put("filter", "(all type:/music/artist created:\"The Lady Killer\")");
            url.put("limit", "10");
            url.put("indent", "true");
            url.put("key", properties.get("API_KEY"));
            HttpRequest request = requestFactory.buildGetRequest(url);
            HttpResponse httpResponse = request.execute();
            JSONObject response = (JSONObject)parser.parse(httpResponse.parseAsString());
            return (JSONArray)response.get("result");
            /*for (Object result : results) {
                System.out.println(JsonPath.read(result,"$.name").toString());
            }*/
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        JSONArray results = doSearch("Bill gates",20);
        Object entity= null;
        JSONObject entityTopic =null;
        ArrayList<String> typesFound = new ArrayList<String>();
        boolean doBreak =false;
        System.out.println("Entities found:"+results.size());
        for (Object result : results) {
             JSONObject topic = doTopic(JsonPath.read(result, "$.mid").toString());
             JSONArray values = (JSONArray)JsonPath.read(topic, "$.property['/type/object/type'].values");
             for (Object value : values) {
                 String id = JsonPath.read(value, "$.id").toString().trim();
                 if(entitiesOfInterest.contains(id)) {
                     if(!doBreak) {
                         doBreak = true;
                         entity=result;
                         entityTopic=topic;
                         System.out.println("Working for First Entity: "+JsonPath.read(entity,"$.name").toString());
                         System.out.println("Ids found:");

                     }
                     typesFound.add(id);
                     System.out.println(id);
                 }
             }
             if (doBreak)
                 break;
        }

        init();
        Map<String,String> infoItems = new HashMap<String, String>();
        infoItems.put("Name",JsonPath.read(entity,"$.name").toString());
        infoItems.put("Description",JsonPath.read(entityTopic, "$.property['/common/topic/description'].values[0].value").toString());
        infoItems.put("Influenced",getDataFromJSONArray(entityTopic, "/influence/influence_node/influenced", "text"));
        infoItems.put("Books About",getDataFromJSONArray(entityTopic, "/book/book_subject/works", "text"));
        infoItems.put("Influenced By",getDataFromJSONArray (entityTopic, "influence/influence_node/influenced_by","text"));
        ///common/topic/description
        //name
        StringBuffer titleBuffer = new StringBuffer();
        for (String type : typesFound){
            Map<String, String> mapForEntity = mapEntities.get(type);
            if (mapForEntity==null)
                continue;
            for (String s : mapForEntity.keySet()) {
                if(s.equals("title")){
                    String title = mapForEntity.get("title");
                    if(!title.equals("PERSON"))
                        titleBuffer.append(","+title);
                    continue;
                }
                String path = "$.property['" + type + "/" + s + "']";
                infoItems.put(mapForEntity.get(s),getDataFromJSONArray(entityTopic, path,"text"));
            }

        }
        if(titleBuffer.length()==0)
            titleBuffer.append("PERSON");
        else
            titleBuffer.deleteCharAt(titleBuffer.length() - 1);
        infoItems.put("title",titleBuffer.toString());




    }

    public static String getDataFromJSONArray(JSONObject entity, String path, String item) {
        try{
            JSONArray array = (JSONArray) JsonPath.read(entity, "$.property['"+path+"'].values");
            StringBuffer buff = new StringBuffer();
            for (Object value : array) {
                String itemVal = JsonPath.read(value, "$." + item).toString().trim();
                buff.append(itemVal+",");
            }
            return buff.substring(0,buff.length()-1);
        }catch (Exception e ){
            return null;
        }

    }
}
