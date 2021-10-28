package droppingprivileges;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.lang.reflect.*;

import com.amazonaws.services.securitytoken.model.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;

/**
 * Handler for requests to Lambda function.
 */
public class App implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    public static final String AWS_ACCESS_KEY_ID = "AWS_ACCESS_KEY_ID";
    public static final String AWS_SECRET_ACCESS_KEY = "AWS_SECRET_ACCESS_KEY";
    public static final String AWS_SECRET_KEY = "AWS_SECRET_KEY";
    public static final String AWS_SESSION_TOKEN = "AWS_SESSION_TOKEN";

    private Utils utils = new Utils();

    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        Map<String, String> headers = new HashMap<>();

        headers.put("Content-Type", "application/json");
        headers.put("X-Custom-Header", "application/json");

        JSONObject requestBody = utils.getEventData(input, context);
        String userId1 = getPrincipalId();

//        Map<String, String> env = System.getenv();
//        System.out.println(env);

        String access_key_id = null;
        String secret_access_key = null;
        String secret_key = null;
        String session_token = null;

        if(requestBody.get("IAM_ROLE") != null){
            Credentials credential = assumeRole(requestBody.get("IAM_ROLE").toString(), requestBody.get("SESSION_NAME").toString());
            access_key_id = credential.getAccessKeyId();
            secret_access_key = credential.getSecretAccessKey();
            secret_key = credential.getSecretAccessKey();
            session_token = credential.getSessionToken();
        }else{
            access_key_id = requestBody.get("AWS_ACCESS_KEY_ID").toString();
            secret_access_key = requestBody.get("AWS_SECRET_ACCESS_KEY").toString();
            secret_key = requestBody.get("AWS_SECRET_ACCESS_KEY").toString();
            session_token = requestBody.get("AWS_SESSION_TOKEN").toString();
        }

        setEnv(AWS_ACCESS_KEY_ID, access_key_id);
        setEnv(AWS_SECRET_ACCESS_KEY, secret_access_key);
        setEnv(AWS_SECRET_KEY, secret_key);
        setEnv(AWS_SESSION_TOKEN, session_token);

        String userId2 = getPrincipalId();

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent()
                .withHeaders(headers);

        String output = String.format("{ \"userId 1 \": \"%s\", \"userId 2\":%s\"\"}", userId1, userId2);

        return response
                .withBody(output)
                .withStatusCode(500);
    }

    public static void setEnv(String key, String value){
        try{
            Map<String, String> env = System.getenv();
            Class<?> cl = env.getClass();
            Field field = cl.getDeclaredField("m");
            field.setAccessible(true);
            Map<String, String> writableEnv = (Map<String, String>) field.get(env);
            writableEnv.put(key, value);
        }catch(Exception e){
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    public static String getPrincipalId(){
        AWSSecurityTokenService sts_client = AWSSecurityTokenServiceClientBuilder
                .standard()
                //.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("sts-endpoint.amazonaws.com", "signing-region"))
                .build();

        GetCallerIdentityResult callerIdentity = sts_client.getCallerIdentity(new GetCallerIdentityRequest());
        return callerIdentity.getUserId();
    }

    public static Credentials assumeRole(String roleArn, String sessionName){
        AWSSecurityTokenService sts_client = AWSSecurityTokenServiceClientBuilder
                .standard()
                .build();
        Credentials credentials = null;
        try {

            AssumeRoleRequest roleRequest = new AssumeRoleRequest()
                    .withRoleArn(roleArn)
                    .withRoleSessionName(sessionName);

            AssumeRoleResult roleResponse = sts_client.assumeRole(roleRequest);
            credentials = roleResponse.getCredentials();
        }catch(Exception e){
            System.err.println(e.getMessage());
            System.exit(1);
        }
        return credentials;
    }
}
