package com.jamasoftware.services.test.client;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import com.jamasoftware.services.test.Config;
import com.jamasoftware.services.test.APIException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;
import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.security.SecureRandom;
import java.time.Instant;

public abstract class JamaClient {
    String tenant = "";
    Long projectId;
    WebTarget fileTarget;
    String clientID = Config.getClientID();
    String clientSecret = Config.getClientSecret();
    String base_url = Config.getRestUrl();
    private WebTarget jamaTarget;
    private WebTarget tokenTarget;
    Long tokenExpiration = 0L;
    String accessToken;
    JSONParser parser = new JSONParser();
    Instant accessTime = null;

    public abstract Response post(WebTarget target, String data) throws APIException;

    public abstract Boolean associateAttachmentWithItem(Long attachmentID, Object itemID) throws APIException;

    public abstract JSONArray getAll(String targetPath) throws ParseException, APIException;

    public abstract Response get(WebTarget target, String data) throws APIException;

    public abstract void updateAccessToken() throws APIException;

    public boolean uploadAttachment(File file, String resource) throws APIException {
        FormDataMultiPart multiPart = new FormDataMultiPart();
        FormDataContentDisposition.FormDataContentDispositionBuilder dispositionBuilder = FormDataContentDisposition
                .name("file");

        dispositionBuilder.fileName(file.getName());
        dispositionBuilder.size(file.getTotalSpace());
        FormDataContentDisposition formDataContentDisposition = dispositionBuilder.build();

        multiPart.bodyPart(new FormDataBodyPart("file", file, MediaType.APPLICATION_OCTET_STREAM_TYPE)
                .contentDisposition(formDataContentDisposition));

        Entity<FormDataMultiPart> entity = Entity.entity(multiPart, MediaType.MULTIPART_FORM_DATA_TYPE);
        WebTarget target;
//        getTokenTarget().path(resource).register(MultiPartFeature.class);
        Response response;
        updateAccessToken();
        target = getFileTarget().path(resource).register(MultiPartFeature.class);
        response = target
                .request()
                .header("authorization", "Bearer " + accessToken)
                .header("Content-type", "multipart/form-data")
                .put(entity);
        if (response.getStatus() != 200) {
            return false;
        }
        return true;
    }

    public Response post(String resource, JSONObject payload) throws APIException {
        WebTarget target = getTarget().path(resource);
        return post(target, payload.toJSONString());
    }

    public Long createAttachmentAndUploadFileToProject(String name, File attachmentFile) throws Exception {
        Long id = createAttachmentObject(name, "");  //"DO-NOT-TOUCH-ReqIF-TRANSFER-CONTENT", "This content is for ReqIF transfers. DO NOT TOUCH");
        if (id != null) {
            System.out.println("Attachment [ " + String.valueOf(id) + "] was successfully created in Jama");
            Boolean uploadResponse = uploadAttachment(id, attachmentFile);
            if (uploadResponse) {
                System.out.println("Successfully uploaded file [" + attachmentFile.getName() + "] to Jama attachment [" + String.valueOf(id) + "]");
                return id;
            }
            System.out.println("Unable to uploaded file [" + attachmentFile.getName() + "] to Jama attachment [" + String.valueOf(id) + "]");
        } else {
            System.out.println("Unable to create attachment [ " + name + "] in Jama");
        }
        return null;
    }

    private boolean uploadAttachment(Long attachmentID, File file) throws Exception {
        String resource = "attachments/" + String.valueOf(attachmentID) + "/file";
        return uploadAttachment(file, resource);
    }

    private Long createAttachmentObject(String attachmentName, String description) throws APIException {
        String resource = "projects/" + String.valueOf(Config.getProjectId()) + "/attachments";
        JSONObject paylaod = new JSONObject();
        JSONObject fields = new JSONObject();
        fields.put("name", attachmentName);
        fields.put("desc", description);
        paylaod.put("fields", fields);
        return postAttachment(resource, paylaod);
    }

    public Long postAttachment(String resource, JSONObject payload) throws APIException {
        Response response = post(resource, payload);
        if (response == null) {
            return null;
        }
        String entitiy = response.readEntity(String.class);
        if (response.getStatus() == 400) {
            return null;
        }
        if (response.getStatus() >= 400) {
            return null;
        }
        Long jamaId = extractJamaID(entitiy);
        if (jamaId == null) {
            return null;
        }
        return jamaId;
    }

    private Long extractJamaID(String responseMessage) {
        JSONParser jsonParser = new JSONParser();
        try {
            JSONObject jsonObject = (JSONObject) jsonParser.parse(responseMessage);
            JSONObject meta = (JSONObject) jsonObject.get("meta");
            String location = (String) meta.get("location");
            String jamaIdString = location.substring(location.lastIndexOf('/') + 1, location.length());
            return Long.valueOf(jamaIdString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public File getFile(String fileLocation, String filename) throws ParseException, APIException, IOException {

        File response = downloadAttachmentFile(fileLocation, filename);
        if (response == null)
            return null;
        return response;
    }

    private File downloadAttachmentFile(String fileLocation, String filename) throws APIException, ParseException, IOException {
        WebTarget webTarget = null;
        Invocation.Builder invocationBuilder = null;
        Response response = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;
        int responseCode;
        String responseMessageFromServer = null;
//        String responseString = null;
        String qualifiedDownloadFilePath = null;

        try {
            // invoke service after setting necessary parameters
//            clientConfig = new ClientConfig();
//            client =  ClientBuilder.newClient(clientConfig).property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true);
//            webTarget = getFileTarget(fileLocation);
            webTarget = getFileTarget().path("/files");
            webTarget = webTarget.queryParam("url", fileLocation);
            webTarget.property("accept", "application/png");
//            webTarget.property("accept", "application/png");
            webTarget.property("accept", "application/json,application/pdf,application/png, text/plain,image/jpeg,application/xml,application/vnd.ms-excel");
            webTarget.register(MultiPartFeature.class);

            // invoke service
            updateAccessToken();
            invocationBuilder = webTarget.request().header("Authorization", "Bearer " + accessToken);
            //          invocationBuilder.header("Authorization", "Basic " + authorization);
            response = invocationBuilder.get();

            // get response code
            responseCode = response.getStatus();

            if (response.getStatus() != 200) {
                throw new RuntimeException("Failed with HTTP configErrorLog code : " + responseCode);
            }

            // get response message
            responseMessageFromServer = response.getStatusInfo().getReasonPhrase();

            // read response string
            inputStream = response.readEntity(InputStream.class);
            qualifiedDownloadFilePath = Config.getProjectDirectory() + Config.fileSeparator + filename;
            outputStream = new FileOutputStream(qualifiedDownloadFilePath);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (Exception ex) {
            throw new APIException(1, responseMessageFromServer);
        } finally {
            // release resources, if any
            if (outputStream != null) {
                outputStream.close();
                response.close();
            }
//            client.close();
        }
        return new File(qualifiedDownloadFilePath);
    }


    Response handleErrors(Response response, WebTarget target, String payload) throws APIException {
        if (response.getStatus() >= 400) {
            String reason = response.readEntity(String.class);
            if (response.getStatus() == 404) {
                System.out.println(reason + " for URL: " + target.getUri().toString());
                return null;
            } else if (response.getStatus() == 400) {
                if (reason.contains("already exists")) {
                    return null;
                } else {
                    System.out.println(reason + " for URL: " + target.getUri().toString() + " for payload [" + payload + "]");
                    return null;
                }
            } else if (response.getStatus() == 401) {
                if (reason.contains("Unauthorized")) {
                    System.out.println("Unauthorized action [" + target.getUri() + "] for [" + payload + "] . Action Failed");
                    return null;
                } else {
                    System.out.println(reason + " for URL: " + target.getUri().toString());
                    return null;
                }
            }
            System.out.println(" Error from API: " + reason + " for payload {" + payload + "} at URL [" + target.getUri() + "]");
            return null;
        }
        return response;
    }

    protected WebTarget getTarget() {
        if (jamaTarget == null) {
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {

                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkServerTrusted(X509Certificate[] arg0, String arg1)
                        throws CertificateException {
                }

                public void checkClientTrusted(X509Certificate[] arg0, String arg1)
                        throws CertificateException {
                }

                public void checkClientTrusted(
                        java.security.cert.X509Certificate[] arg0, String arg1)
                        throws java.security.cert.CertificateException {
                }

                public void checkServerTrusted(
                        java.security.cert.X509Certificate[] arg0, String arg1)
                        throws java.security.cert.CertificateException {
                }
            }};

            // Install the all-trusting trust manager
            SSLContext sc = null;
            try {
                sc = SSLContext.getInstance("TLS");
                sc.init(null, trustAllCerts, new SecureRandom());
                HttpsURLConnection
                        .setDefaultSSLSocketFactory(sc.getSocketFactory());
            } catch (Exception e) {
                e.printStackTrace();
            }

            ClientConfig config = new ClientConfig();
            HttpAuthenticationFeature feature = HttpAuthenticationFeature.basicBuilder()
                    .credentials("", "")
                    .build();
            config.register(feature);
            Client client = ClientBuilder.newBuilder().sslContext(sc).withConfig(config).property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true).build();
            jamaTarget = client.target(base_url);
        }
        return jamaTarget;
    }


    protected WebTarget getFreshTokenTarget() {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {

            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkServerTrusted(X509Certificate[] arg0, String arg1)
                    throws CertificateException {
            }

            public void checkClientTrusted(X509Certificate[] arg0, String arg1)
                    throws CertificateException {
            }

            public void checkClientTrusted(
                    java.security.cert.X509Certificate[] arg0, String arg1)
                    throws java.security.cert.CertificateException {
            }

            public void checkServerTrusted(
                    java.security.cert.X509Certificate[] arg0, String arg1)
                    throws java.security.cert.CertificateException {
            }
        }};

        // Install the all-trusting trust manager
        SSLContext sc = null;
        try {
            sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection
                    .setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }

        ClientConfig config = new ClientConfig();
        HttpAuthenticationFeature feature = HttpAuthenticationFeature.basicBuilder()
                .credentials(Config.getClientID(), Config.getClientSecret())
                .build();
        config.register(feature);

        Client client = ClientBuilder.newBuilder().withConfig(config).property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true).build();
//            Client client = ClientBuilder.newBuilder().sslContext(sc).withConfig(config).property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true).build();
        tokenTarget = client.target(Config.getBaseUrl());
        return tokenTarget;
    }


    protected WebTarget getTokenTarget() {
        if (tokenTarget == null) {
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {

                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkServerTrusted(X509Certificate[] arg0, String arg1)
                        throws CertificateException {
                }

                public void checkClientTrusted(X509Certificate[] arg0, String arg1)
                        throws CertificateException {
                }

                public void checkClientTrusted(
                        java.security.cert.X509Certificate[] arg0, String arg1)
                        throws java.security.cert.CertificateException {
                }

                public void checkServerTrusted(
                        java.security.cert.X509Certificate[] arg0, String arg1)
                        throws java.security.cert.CertificateException {
                }
            }};

            // Install the all-trusting trust manager
            SSLContext sc = null;
            try {
                sc = SSLContext.getInstance("TLS");
                sc.init(null, trustAllCerts, new SecureRandom());
                HttpsURLConnection
                        .setDefaultSSLSocketFactory(sc.getSocketFactory());
            } catch (Exception e) {
                e.printStackTrace();
            }

            ClientConfig config = new ClientConfig();
            HttpAuthenticationFeature feature = HttpAuthenticationFeature.basicBuilder()
                    .credentials(Config.getClientID(), Config.getClientSecret())
                    .build();
            config.register(feature);

            Client client = ClientBuilder.newBuilder().withConfig(config).property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true).build();
//            Client client = ClientBuilder.newBuilder().sslContext(sc).withConfig(config).property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true).build();
            tokenTarget = client.target(Config.getBaseUrl());
        }
        return tokenTarget;
    }


    protected WebTarget getFileTarget() {
        if (fileTarget == null) {
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {

                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkServerTrusted(X509Certificate[] arg0, String arg1)
                        throws CertificateException {
                }

                public void checkClientTrusted(X509Certificate[] arg0, String arg1)
                        throws CertificateException {
                }

                public void checkClientTrusted(
                        java.security.cert.X509Certificate[] arg0, String arg1)
                        throws java.security.cert.CertificateException {
                }

                public void checkServerTrusted(
                        java.security.cert.X509Certificate[] arg0, String arg1)
                        throws java.security.cert.CertificateException {
                }
            }};

            // Install the all-trusting trust manager
            SSLContext sc = null;
            try {
                sc = SSLContext.getInstance("TLS");
                sc.init(null, trustAllCerts, new SecureRandom());
                HttpsURLConnection
                        .setDefaultSSLSocketFactory(sc.getSocketFactory());
            } catch (Exception e) {
                e.printStackTrace();
            }

            ClientConfig config = new ClientConfig();
            HttpAuthenticationFeature feature = HttpAuthenticationFeature.basicBuilder()
                    .credentials(Config.getClientID(), Config.getClientSecret())
                    .build();
            config.register(feature);

//            Client client = ClientBuilder.newBuilder().withConfig(config).property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true).build();
            Client client = ClientBuilder.newBuilder().sslContext(sc).withConfig(config).property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true).build();
            fileTarget = client.target(Config.getRestUrl());
        }
        return fileTarget;
    }


    @SuppressWarnings("unchecked")
    public JSONArray getAll(String targetPath, String[] queryParams, String[] values) throws ParseException, APIException {
        WebTarget target = getTarget().path(targetPath);
        if (queryParams != null && queryParams.length != 0) {
            for (int i = 0; i < queryParams.length; ++i) {
                target = target.queryParam(queryParams[i], values[i]);
            }
        }
        JSONArray allResults = new JSONArray();
        long remainingResults = -1;
        long currentStartIndex = 0;
        while (remainingResults != 0) {
            WebTarget currentTarget = target.queryParam("startAt", currentStartIndex);
//            System.out.println(currentTarget.toString());
            Response response = get(currentTarget, currentTarget.getUri().toString());
            if (response == null)
                return null;
            String responseEntity = response.readEntity(String.class);
            JSONObject object = (JSONObject) parser.parse(responseEntity);
            JSONObject meta = (JSONObject) object.get("meta");
            JSONObject pageInfo = (JSONObject) meta.get("pageInfo");
            if (pageInfo == null) {
                // it's the REST beta
                return (JSONArray) object.get("data");
            }
            long totalResults = (long) pageInfo.get("totalResults");
            long startIndex = (long) pageInfo.get("startIndex");
            long resultCount = (long) pageInfo.get("resultCount");
            remainingResults = totalResults - (startIndex + resultCount);
            currentStartIndex = startIndex + 20;

            JSONArray results = (JSONArray) object.get("data");
            allResults.addAll(results);
        }
        return allResults;
    }

    public JSONObject get(String resource) throws ParseException, APIException {
        WebTarget target = getTarget().path(resource);
        Response response = get(target, resource);
        if (response == null)
            return null;
        JSONObject object = null;
        try {
            object = (JSONObject) parser.parse(response.readEntity(String.class));
        } catch (ParseException p) {
        }
        if (object != null)
            return (JSONObject) object.get("data");
        else
            return null;
    }

    public abstract JSONArray validateCredentials(String jamaUrl, String userId, String userSecret);

    public String getActiveUser() {
        JSONObject activeUser = null;
        try {
            activeUser = get("users/current");
        } catch (ParseException e) {
            return "Unknown";
        } catch (APIException e) {
            return "Unknown";
        }
        if (activeUser == null) {
            return "Unknown";
        } else {
            return activeUser.get("firstName") + " " + activeUser.get("lastName");
        }
    }

}

