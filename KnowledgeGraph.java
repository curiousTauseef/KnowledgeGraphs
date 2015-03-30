import java.io.*;
import java.net.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;  
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.*;
public class KnowledgeGraph {
         public static void main(String[] args) throws IOException{
        	 String username = System.getProperty("user.name");	 
        	 
        	 String apikey;
        	 
             if (args[0].equals( "-key")){
        		 apikey = args[1];
            	 Question part2 = new Question();
            	 
            	 int length = args.length;
            	 
            	 if (length > 2 ) {
                	if (args[2].equals("-q")){
                		String query = args[3];
                		if (args [5].equals("infobox")){
                			//Diwakar: call your code here
                		}
                		if (args [5].equals( "question")){
                			part2.main(query,apikey);
                		}
                	}
                	 //option2
                	if (args[2].equals( "-f")){
                    	BufferedReader reader = null;
                    	String fileName = args[3];
                    	try {
                    		 //read queries from the file
                    		 File file = new File(fileName);
                             reader = new BufferedReader(new FileReader( file ) );
                             
                    		 String tempString = null;
                             
                             //for infobox
                             if (args[5] .equals( "infobox")){
                            	 while ((tempString = reader.readLine()) != null) {
                                	//Diwakar: call your code here
                            	 }
                             }
                             if (args[5] .equals( "question")){
                                 while ((tempString = reader.readLine()) != null) {
                                	System.out.println ("Question: "+ tempString);
                                	System.out.println ();
                         			part2.main(tempString,apikey);
                                 }
                             }
                             reader.close();
                        } catch (IOException e) {
                             e.printStackTrace();
                        } finally {
                             if (reader != null) {
                                 try {
                                     reader.close();
                                 } catch (IOException e1) {
                                 }
                             }
                        }
                	}
                } else {
            		System.out.println("Welcome to infobox creator using Freebase knowledge graph.");
            		System.out.println("Feel curious? Start exploring...");
            		part2.interactive = true;
            		while (true){
            			SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
                   	    System.out.print("["+df.format(new Date())+"] " + username + "@fb_ibox " );
            			BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));  
            			String text = (buffer.readLine()).toLowerCase();
            			
                        if (text.length() > 0)
            			     System.out.println("let me see...");
            			if (text.startsWith("who")){
                			part2.main(text,apikey);
            			} else{
            				//Diwakar: call your code here
            			}
            		}
            	}
            	
        	 } else {
        		 System.out.println("Wrong commmand please follow the format:");
        		 System.out.println("Option1:");
        		 System.out.println("./run.sh -key <Freebase API key> -q <query> -t <infobox|question>");
        		 System.out.println("Option2:");
        		 System.out.println("./run.sh -key <Freebase API key> -f <file of queries> -t <infobox|question>");
        		 System.out.println("Option3:");
        		 System.out.println("./run.sh -key <Freebase API key>");
        	 }
         }
}