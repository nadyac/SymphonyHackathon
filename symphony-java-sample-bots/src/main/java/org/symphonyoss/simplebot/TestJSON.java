/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.symphonyoss.simplebot;
import java.io.FileNotFoundException;
import java.io.FileReader;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;

 

/**
 *
 * @author macbookpro
 */
public class TestJSON {
    private static final String path = "/Users/macbookpro/Documents/Symphony/"
            + "symphony-java-sample-bots/src/main/java/org/symphonyoss/simplebot";
    
    public static void main(String [] args) throws FileNotFoundException{
        try {
            FileReader reader = new FileReader (path);
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);
//            JSONArray result = (JSONArray) jsonObject.get("results");
            JSONObject results = (JSONObject) jsonObject.get("results");
            System.out.println(results.get("482|lastUpdateDate"));
            
        }catch(Exception e){
            
        };
    }
            
    
}
