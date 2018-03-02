package com.jamasoftware.services.test.client;

import com.jamasoftware.services.test.Config;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import com.jamasoftware.services.test.APIException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class OAuthJamaClient extends JamaClient {

    public OAuthJamaClient() {
        super();
    }

    public Response get(WebTarget target, String data) throws APIException {
        updateAccessToken();
        Invocation.Builder builder = target
                .request(MediaType.APPLICATION_JSON_TYPE);
        if (!tenant.equals("")) {
            builder.header("x-jama-tenant", tenant);
        }
        builder.header("Authorization", "Bearer " + accessToken);
//        System.out.println("GET: " + target.toString());
        builder = builder.accept("application/json,application/pdf,text/plain,image/jpeg,application/xml,application/vnd.ms-excel");
        return handleErrors(builder.get(), target, data);
    }

    public Response post(String resource, JSONObject payload) throws APIException {
        WebTarget target = getTarget().path(resource);
//        System.out.println("POST: " + target.toString());
        return post(target, payload.toJSONString());
    }

    public Response post(WebTarget target, String data) throws APIException {
        updateAccessToken();
        if (tenant.length() > 0) {
            return handleErrors(target
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .header("x-jama-tenant", tenant)
                    .header("Authorization", "Bearer " + accessToken)
                    .post(Entity.entity(data, MediaType.APPLICATION_JSON_TYPE)), target, data);
        }
//        System.out.println("POST: " + data);
        return handleErrors(target
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", "Bearer " + accessToken)
                .post(Entity.entity(data, MediaType.APPLICATION_JSON_TYPE)), target, data);
    }

    @Override
    public Boolean associateAttachmentWithItem(Long attachmentID, Object itemID) throws APIException {
        String resource = "items/" + String.valueOf(Config.getItemID()) + "/attachments";
        JSONObject payload = new JSONObject();
        payload.put("attachment", attachmentID);
        Response response = post(resource, payload);
        if(response.getStatus() == 201)
            return true;
        return false;
    }

    public JSONArray getAll(String targetPath) throws ParseException, APIException {
        return getAll(targetPath, null, null);
    }

    public boolean validate(String jamaUrl, String clientID, String clientSecret, Long projectId) throws APIException, ParseException {
        this.clientID = clientID;
        this.clientSecret = clientSecret;
        this.base_url = jamaUrl;
        String resource = "projects/" + String.valueOf(projectId);
        try {
            JSONObject jsonObject = get(resource);
            if (jsonObject == null)
                return false;
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public JSONArray validateCredentials(String jamaUrl, String clientID, String clientSecret) {
        this.clientID = clientID;
        this.clientSecret = clientSecret;
        this.base_url = jamaUrl;
        JSONArray projectList = null;
        String resource = "projects";
        try {
            projectList = getAll(resource);
            if (projectList == null)
                return null;
        } catch (Exception e) {
            System.out.println("Error processing all available projects in your instance. Please try again. ");
            return null;
        }
        return projectList;
    }

    @SuppressWarnings("unchecked")
    public void updateAccessToken() throws APIException {
        Long secondsFromStart = null;
        if(accessTime != null)
            secondsFromStart = accessTime.until(Instant.now(), ChronoUnit.SECONDS);
        if (tokenExpiration <= 60L || (secondsFromStart != null && (tokenExpiration - secondsFromStart) <= 60L)) {
            //update token
//        if (tokenExpiration <= 100L) {
            String url = "/rest/oauth/token";
            try {
                JSONObject jsonObject;
                Response response = postAccessToken(url);
                try {
                    if (response == null) {
                        System.out.println("Unable to authenticate with provided credentials. Please verify your credentials are valid, and try again.");
                        throw new APIException(1, "Unable to authenticate with provided credentials. Please verify your credentials are valid, and try again.");
                    }
                    jsonObject = (JSONObject) parser.parse(response.readEntity(String.class));
                } catch (ParseException e) {
                    System.out.println(e.getMessage());
                    throw new APIException(1, e.getMessage());
                }
                accessToken = (String) jsonObject.get("access_token");
                tokenExpiration = (Long) jsonObject.get("expires_in");
                if (accessToken == null || accessToken.equals("") || tokenExpiration == 0) {
                    System.out.println("Unable to retrieve valid access token with provided credentials. Please verify your credentials are valid, and try again.");
                    throw new APIException(1, "Unable to retrieve valid access token with provided credentials. Please verify your credentials are valid, and try again.");
                } else
                    setupTokenData();
            } catch (APIException e) {
                System.out.println(e.getMessage());
                throw new APIException(1, e.getMessage());
            }
        }
    }

    private void setupTokenData() {
        accessTime = Instant.now();
    }

    private Response postAccessToken(String resource) throws APIException {
        WebTarget target = getFreshTokenTarget().path(resource);
        return postForAccessToken(target);
    }

    private Response postForAccessToken(WebTarget target) throws APIException {
        String data = "grant_type=client_credentials";
        if (tenant.length() > 0) {
            return handleErrors(target
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .header("x-jama-tenant", tenant)
                    .post(Entity.entity(data, MediaType.APPLICATION_FORM_URLENCODED)), target, data);
        }
//        System.out.println("POST: " + data);
        return handleErrors(target
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(data, MediaType.APPLICATION_FORM_URLENCODED)), target, data);

    }
}
