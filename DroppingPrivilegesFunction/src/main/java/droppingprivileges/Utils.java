package droppingprivileges;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.Context;

public class Utils {
    public String getObjectContent(InputStream input){
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String line = null;
            String lines = "";
            while((line = reader.readLine()) != null){
                lines += line;
            }

            return lines;
        }catch(IOException e){
            return "";
        }
    }

    public JSONObject getEventData(APIGatewayProxyRequestEvent event, Context context) {
        try {

            JSONParser requestParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) requestParser.parse(event.getBody());
            return jsonObject;
        }

        catch (ParseException e) {

            context.getLogger().log("unable to parse incoming event " + e.getMessage());
        }
        return null;
    }
}