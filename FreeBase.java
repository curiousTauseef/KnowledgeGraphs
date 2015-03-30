import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.jayway.jsonpath.JsonPath;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * Created by diwakarmahajan on 3/18/15.
 */

public class FreeBase {
    private static String API_KEY="";
    private static final int SPACE_LENGTH =20;
    private static final int EXPANSION_FACTOR =6;
    private static ArrayList<String> entitiesOfInterest = new ArrayList<String>(Arrays.asList("/people/person","/book/author","/film/actor","/tv/tv_actor","/organization/organization_founder","/business/board_member",
            "/sports/sports_league","/sports/sports_team","/sports/professional_sports_team"));
    private static Map<String, Map<String,String>> mapEntities  = new HashMap<String, Map<String,String>>();
    private static Map<String, String> formatResults  = new HashMap<String,String>();


    public static void init(){
        //Person
        Map<String,String> map = new LinkedHashMap<String,String>();
        map.put("title","Person");
        map.put("/common/topic/description","Description");
        map.put("date_of_birth","Birthday");
        map.put("place_of_birth","Place of Birth");
        map.put("/people/person/sibling_s,/people/sibling_relationship/sibling","Siblings");
        map.put("/people/person/spouse_s,/people/marriage/spouse@/people/marriage/from@/people/marriage/to@/people/marriage/location_of_ceremony","Spouses");
        map.put("/people/deceased_person/cause_of_death","Cause of Death");
        map.put("/people/deceased_person/place_of_death","Died At");
        map.put("/people/deceased_person/date_of_death","Date of Death");
        mapEntities.put("/people/person",map);

        ///organization/organization_founder
        map=new LinkedHashMap<String,String>();
        map.put("title","Business Person");
        map.put("organizations_founded","Founded");
        mapEntities.put("/organization/organization_founder",map);

        ///business/board_member
        map=new LinkedHashMap<String,String>();
        map.put("title","Business Person");
        map.put("/business/board_member/leader_of,/organization/leadership/organization@/organization/leadership/role@/organization/leadership/title@/organization/leadership/from@/organization/leadership/to","Leadership");
        map.put("/business/board_member/organization_board_memberships,/organization/organization_board_membership/organization@/organization/organization_board_membership/role@/organization/organization_board_membership/title@/organization/organization_board_membership/from@/organization/organization_board_membership/to","Board Member");
        mapEntities.put("/business/board_member",map);

        ///book/author
        map=new LinkedHashMap<String,String>();
        map.put("title","Author");
        map.put("works_written","Books");
        map.put("/book/book_subject/works","Books About");
        map.put("/influence/influence_node/influenced","Influenced");
        map.put("influence/influence_node/influenced_by","Influenced By");
        mapEntities.put("/book/author",map);


        ///film/actor
        map = new LinkedHashMap<String, String>();
        map.put("title","Actor");
        map.put("/film/actor/film,/film/performance/film@/film/performance/character","Films");
        mapEntities.put("/film/actor",map);
        mapEntities.put("/tv/tv_actor",map);

        ///tv/tv_actor

        ///sports/sports_league
        map = new LinkedHashMap<String, String>();
        map.put("title","League");
        map.put("/common/topic/description","Description");
        map.put("championship","Championship");
        map.put("sport","Sport");
        map.put("/organization/organization/slogan","Slogan");
        map.put("/common/topic/official_website","Official Website");
        map.put("/sports/sports_league/teams,/sports/sports_league_participation/team","Teams");
        mapEntities.put("/sports/sports_league",map);

        ///sports/sports_team
        ///sports/professional_sports_team
        map = new LinkedHashMap<String, String>();
        map.put("title","Sports Team");
        map.put("/common/topic/description","Description");
        map.put("/sports/sports_team/championship","Championship");
        map.put("/organization/organization/slogan","Slogan");
        map.put("/common/topic/official_website","Official Website");
        map.put("/sports/sports_league/teams,/sports/sports_league_participation/team","Teams");
        map.put("/sports/sports_team/championships","Championship");
        map.put("/sports/sports_team/sport","Sport");
        map.put("/sports/sports_team/founded","Founded");
        map.put("/sports/sports_team/arena_stadium","Arena");
        map.put("/sports/sports_team/league,/sports/sports_league_participation/league","League");
        map.put("/sports/sports_team/location","Location");
        map.put("/sports/sports_team/coaches,/sports/sports_team_coach_tenure/coach@/sports/sports_team_coach_tenure/position@/sports/sports_team_coach_tenure/from@/sports/sports_team_coach_tenure/to","Coaches");
        map.put("/sports/sports_team/roster,/sports/sports_team_roster/player@/sports/sports_team_roster/position@/sports/sports_team_roster/number@/sports/sports_team_roster/from@/sports/sports_team_roster/to","PlayersRoster");
        mapEntities.put("/sports/professional_sports_team",map);
        mapEntities.put("/sports/sports_team",map);

        formatResults.put("Coaches","Name:,Position:,From:,To:now");
        formatResults.put("PlayersRoster","Name:,Position:,Number:,From:,To:now");
        formatResults.put("Leadership"," Organization:, Role :, Title:, From:, To:now");
        formatResults.put("Board Member", " Organization:, Role :, Title:, From:, To:now");
        formatResults.put("Films", " Film Name:, Character:");
        //"Spouses"," :, ( :, - : now , ) @ :"
        // map.put("Films","");
        formatResults.put("Spouses", "Name:,From:,To:now,Place:");
    }

    public static JSONObject doTopic(String topicId){
        try {
            HttpTransport httpTransport = new NetHttpTransport();
            HttpRequestFactory requestFactory = httpTransport.createRequestFactory();
            JSONParser parser = new JSONParser();
            GenericUrl url = new GenericUrl("https://www.googleapis.com/freebase/v1/topic" + topicId);
            url.put("key", API_KEY);
            HttpRequest request = requestFactory.buildGetRequest(url);
            HttpResponse httpResponse = request.execute();
            JSONObject topic =  (JSONObject)parser.parse(httpResponse.parseAsString());
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
            HttpTransport httpTransport = new NetHttpTransport();
            HttpRequestFactory requestFactory = httpTransport.createRequestFactory();
            JSONParser parser = new JSONParser();
            GenericUrl url = new GenericUrl("https://www.googleapis.com/freebase/v1/search");
            url.put("query", urlEncode(query));
            //url.put("filter", "(all type:/music/artist created:\"The Lady Killer\")");
            url.put("limit", "10");
            url.put("indent", "true");
            url.put("key", API_KEY);
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

    public static void query(String query,String apiKey) {
        API_KEY = apiKey;
        JSONArray results = doSearch(query,20);
        if (results==null || results.size()==0) {
            System.out.println("No results found!! Exiting!!");
            System.exit(0);
        }
        Object entity= null;
        JSONObject entityTopic =null;
        ArrayList<String> typesFound = new ArrayList<String>();
        boolean doBreak =false;
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
                    }
                    typesFound.add(id);
                }
            }
            if (doBreak)
                break;
        }
        if (entity==null) {
            System.out.println("No results found!! Exiting!!");
            System.exit(0);
        }
        init();
        Map<String,String> infoItems = new LinkedHashMap<String, String>();
        infoItems.put("Name",JsonPath.read(entity,"$.name").toString());
        //name
        StringBuffer titleBuffer = new StringBuffer();
        for (String type : typesFound){
            Map<String, String> mapForEntity = mapEntities.get(type);
            if (mapForEntity==null)
                continue;
            for (String s : mapForEntity.keySet()) {
                if(s.equals("title")){
                    String title = mapForEntity.get("title");
                    if(!title.equals("Person")){
                        if(!titleBuffer.toString().contains(title))
                            titleBuffer.append(title+",");
                        continue;
                    }
                }
                String path = s;
                if(!s.contains("/"))
                    path =  type + "/" + s ;
                String infoItem= getDataFromJSONArray(entityTopic, path + "", "text");
                String key =mapForEntity.get(s);
                infoItems.put(key,getDataFromJSONArray(entityTopic, path + "","text"));
            }

        }
        if(titleBuffer.length()==0)
            titleBuffer.append("PERSON");
        else
            titleBuffer.deleteCharAt(titleBuffer.length() - 1);
        infoItems.put("title",infoItems.get("Name")+"("+titleBuffer.toString()+")");
        printInfoItems(infoItems);
    }

    public static String getDataFromJSONArray(JSONObject entity, String path, String item) {
        String hierarchy[] = path.split(",");
        try {
            JSONArray array = (JSONArray) JsonPath.read(entity, "$.property['" + hierarchy[0] + "'].values");
            StringBuffer buff = new StringBuffer();
            for (Object value : array) {
                if (hierarchy.length > 1) {
                    String fetchVals[] = hierarchy[1].split("@");
                    for (String queries : fetchVals) {
                        boolean addedData= false;
                        try {
                            JSONArray subArray = (JSONArray) JsonPath.read(value, "$.property['" + queries + "'].values");
                            for (Object subVal : subArray) {
                                String itemVal = JsonPath.read(subVal, "$." + item).toString().trim();
                                buff.append(itemVal + "@");
                                addedData=true;
                            }
                        } catch (Exception e) {

                        }
                        if(addedData)
                            buff.deleteCharAt(buff.length() - 1);
                        else
                            buff.append(" ");
                        buff.append("#~");
                    }

                } else {
                    try {
                        String itemVal = JsonPath.read(value, "$." + item).toString().trim();
                        if(path.contains("description"))
                            itemVal = JsonPath.read(value, "$." + "value").toString().trim().replace("\n","");
                        buff.append(itemVal);
                    } catch (Exception e) {

                    }
                    buff.append("#~");
                }
                buff.deleteCharAt(buff.length() - 1);
                buff.deleteCharAt(buff.length() - 1);
                buff.append("#%");
            }
            return buff.substring(0, buff.length() - 2);
        }catch (Exception e){
            return  null;
        }

    }

    public static void printInMiddle(String s){
        System.out.print("|");
        for (int i =0 ; i < SPACE_LENGTH*6/2 ; i++){
            System.out.print(" ");
            if(i==(SPACE_LENGTH*6/3 - s.length()/2)){
                System.out.print(s);
                i+=s.length()/2;
            }
            System.out.print(" ");
        }
        System.out.println("|");
    }

    public static void printInfoItems(Map<String,String> infoItems) {
        System.out.println(" "+getCharRepeated(SPACE_LENGTH*EXPANSION_FACTOR,'-'));
        printInMiddle(infoItems.get("title"));
        for (String infoKey : infoItems.keySet()) {
            String info = infoItems.get(infoKey);
            if (info == null || infoKey.equals("title"))
                continue;
            System.out.println(" "+getCharRepeated(SPACE_LENGTH*EXPANSION_FACTOR, '-'));
            String val[] = info.split("#%");
            System.out.print("| "+infoKey + ":");
            System.out.print(getCharRepeated(SPACE_LENGTH - 3 - infoKey.length(), ' '));
            String formatString = formatResults.get(infoKey);
            if (formatString != null) {
                StringBuffer formatBuff = new StringBuffer();
                String subItemsFormatter[] = formatString.split(",");
                int newSpaceLength = (int)(SPACE_LENGTH*(EXPANSION_FACTOR-1)/subItemsFormatter.length);
                for (String formatS : subItemsFormatter){
                    formatBuff.append("| ");
                    formatBuff.append(formatS.split(":")[0]);
                    formatBuff.append(getCharRepeated(newSpaceLength-formatS.split(":")[0].length()-2,' '));
                }

                formatBuff.append(" |");
                formatBuff.append("\n");
                formatBuff.append("|"+getCharRepeated(SPACE_LENGTH,' '));
                formatBuff.append(getCharRepeated(SPACE_LENGTH*(EXPANSION_FACTOR-1),'-'));
                formatBuff.append("\n");
                for (String subInfo : val) {
                    formatBuff.append("|"+getCharRepeated(SPACE_LENGTH-1,' '));

                    String subVals[] = subInfo.split("#~");
                    boolean fromSubValAdded= false;
                    for (int i = 0; i < subVals.length; i++) {
                        if (subVals[i].isEmpty())
                            subVals[i]="@";
                        String defaultVals[] =subItemsFormatter[i].split(":");
                        formatBuff.append("| ");
                        String sumOfVals="";
                        String subSubVal[] = subVals[i].split("@");
                        if(subItemsFormatter[i].contains("From"))
                            System.out.print("");
                        boolean toValAdded= false;
                        for (int p =0  ; p < subSubVal.length ;p++ ) {
                            if(!subSubVal[p].trim().isEmpty() && subItemsFormatter[i].contains("From"))
                                fromSubValAdded = true;
                            if(!subSubVal[p].trim().isEmpty())
                                toValAdded=true;
                            sumOfVals += subSubVal[p] + ", ";

                        }
                        if(!toValAdded && fromSubValAdded){
                            if(subItemsFormatter[i].contains("To")) {
                                sumOfVals= sumOfVals.substring(0,sumOfVals.length()-3);
                                sumOfVals += defaultVals[1] + ", ";
                            }
                        }

                        sumOfVals= sumOfVals.substring(0,sumOfVals.length()-2);
                        if(!sumOfVals.isEmpty()){
                            formatBuff.append(adjustString(sumOfVals,newSpaceLength-2));
                            formatBuff.append(getCharRepeated(newSpaceLength-sumOfVals.length()-2,' '));
                        }
                        else
                            formatBuff.append(getCharRepeated(newSpaceLength,' '));
                    }
                    formatBuff.append(" |\n");
                }
                System.out.print(formatBuff.toString());
            }
            else {
                String printVal = "";
                for (int i =0 ; i< val.length;i++) {
                    if (i!=0)
                        printVal+= "|"+getCharRepeated(SPACE_LENGTH-1, ' ');
                    printVal += adjustForSpace(val[i]);
                    printVal += getCharRepeated((int)(0.845*SPACE_LENGTH*EXPANSION_FACTOR - val[i].length()),' ');
                    printVal +="|";
                    printVal +="\n";
                }
                System.out.println(printVal.substring(0,printVal.length()-1));
            }
        }
        System.out.println(" "+getCharRepeated(SPACE_LENGTH*EXPANSION_FACTOR, '-'));
    }

    public static String getCharRepeated(int num, char ch){
        StringBuffer s= new StringBuffer();
        for (int  k =0; k< num; k++)
            s.append(ch);
        return s.toString();
    }

    public static String adjustForSpace(String s){
        StringBuffer buff = new StringBuffer();
        if(s.length() > SPACE_LENGTH*EXPANSION_FACTOR){
            int len = s.length();
            int i=0;
            int factor = SPACE_LENGTH*(EXPANSION_FACTOR-1);
            String lastString ="";
            while (i*factor<len){
                if(i>0){
                    buff.append("|");
                    buff.append(getCharRepeated(SPACE_LENGTH-1,' '));
                }
                lastString=s.substring(Math.min(len - 2, i *factor),Math.min(len - 1, (i + 1) *factor));
                buff.append(lastString);
                buff.append(" |");
                buff.append("\n");
                i++;
            }
            buff.deleteCharAt(buff.length() - 1);
            buff.deleteCharAt(buff.length() - 1);
            buff.deleteCharAt(buff.length() - 1);
            buff.append(getCharRepeated((int) (0.845 * SPACE_LENGTH * EXPANSION_FACTOR - lastString.length()), ' '));
            return buff.toString();
        }
        return  s;
    }


    public  static  String adjustString(String s, int size){
        if (s.length() > size-1){
            StringBuffer buff = new StringBuffer();
            buff.append(s.substring(0,Math.max(2,size-3)));
            buff.append("...");
            return buff.toString();
        }
        return  s;

    }

}
