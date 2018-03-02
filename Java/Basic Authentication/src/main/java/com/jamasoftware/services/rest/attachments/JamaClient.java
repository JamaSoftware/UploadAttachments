package com.jamasoftware.services.rest.attachments;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;
import java.io.*;
import java.security.SecureRandom;
import java.util.Collection;

import eu.medsea.mimeutil.MimeType;
import eu.medsea.mimeutil.MimeUtil;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class JamaClient {
    private WebTarget jamaTarget;


    private WebTarget getTarget() {
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
                    .credentials(Config.getUsername(), Config.getPassword())
                    .build();
            config.register(feature);
            Client client = ClientBuilder.newBuilder().sslContext(sc).withConfig(config).property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true).build();
            jamaTarget = client.target(Config.getRestUrl());
        }
        return jamaTarget;
    }

    public Response post(String resource, JSONObject payload) {
        WebTarget target = getTarget().path(resource);
//        System.out.println("POST: " + target.toString());
        return post(target, payload.toJSONString());
    }


    public Long postAttachment(String resource, JSONObject payload) {
        Response response = post(resource, payload);
        String entitiy = response.readEntity(String.class);
        if (entitiy == null) {
            System.out.println("Error occurred when uploading attachment [" + payload + "]...");
            return null;
        }
//        System.out.println(entitiy);
        if (response.getStatus() == 400) {
            System.out.println("Error occurred when uploading attachment [" + payload + "] caused by [" + entitiy);
            return null;
        }
        if (response.getStatus() >= 400) {
            System.out.println("Error occurred when uploading attachment [" + payload + "] caused by [" + entitiy);
            return null;
        }
        Long jamaId = extractJamaID(entitiy);
        if (jamaId == null) {
            System.out.println("Posted attachment [" + payload + "] to Jama, BUT was not able to retrieve ID. Attachment will be dropped.");
            return null;
        }
        return jamaId;
    }


    public boolean uploadAttachment(File file, String resource) throws IOException {
        FormDataMultiPart multiPart = new FormDataMultiPart();
        FormDataContentDisposition.FormDataContentDispositionBuilder dispositionBuilder = FormDataContentDisposition
                .name("file");

        dispositionBuilder.fileName(file.getName());
        dispositionBuilder.size(file.getTotalSpace());
        FormDataContentDisposition formDataContentDisposition = dispositionBuilder.build();

        Collection<MimeType> mimeTypes = MimeUtil.getMimeTypes(file);
        MimeType mimeType = mimeTypes.iterator().next();

        multiPart.bodyPart(new FormDataBodyPart("file", file, MediaType.valueOf(mimeType.toString()))
                .contentDisposition(formDataContentDisposition));
//        multiPart.bodyPart(new FormDataBodyPart("file", file, MediaType.APPLICATION_OCTET_STREAM_TYPE)
//                .contentDisposition(formDataContentDisposition));

        Entity<FormDataMultiPart> entity = Entity.entity(multiPart, MediaType.MULTIPART_FORM_DATA_TYPE);
        WebTarget target = getTarget().path(resource).register(MultiPartFeature.class);
        Response response = target.request().put(entity);
        if (response.getStatus() != 200) {
            System.out.println("Unable to upload attachment to Jama Project.");
            return false;
        }
        return true;
    }

    private File downloadAttachmentFile(Long attachmentId, String filename) throws ParseException, IOException {
        // local variables
        String attachmentURL = "attachments/" + String.valueOf(attachmentId) + "/file";
        // local variables
        ClientConfig clientConfig = null;
        Client client = null;
        WebTarget webTarget = null;
        Invocation.Builder invocationBuilder = null;
        Response response = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;
        int responseCode;
        String responseMessageFromServer = null;
        String responseString = null;
        String qualifiedDownloadFilePath = null;

        try {
            // invoke service after setting necessary parameters
            clientConfig = new ClientConfig();
            client = ClientBuilder.newClient(clientConfig).property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true);
            webTarget = getTarget().path(attachmentURL);
            webTarget.property("accept", "application/png");
//            webTarget.property("accept", "application/png");
            webTarget.property("accept", "application/json,application/pdf,application/png, text/plain,image/jpeg,application/xml,application/vnd.ms-excel, application/octet-stream");
            webTarget.register(MultiPartFeature.class);

            // invoke service
            invocationBuilder = webTarget.request();
            //          invocationBuilder.header("Authorization", "Basic " + authorization);
            response = invocationBuilder.get();

            // get response code
            responseCode = response.getStatus();

            if (response.getStatus() != 200) {
                throw new RuntimeException("Failed with HTTP configErrorLog code : " + responseCode);
            }


            // read response string
            inputStream = response.readEntity(InputStream.class);
            qualifiedDownloadFilePath = filename;
            outputStream = new FileOutputStream(qualifiedDownloadFilePath);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            // release resources, if any
            outputStream.close();
            response.close();
            client.close();
        }
        return new File(qualifiedDownloadFilePath);
    }

    private Response handleErrors(Response response, WebTarget target, String payload) {
        if (response.getStatus() >= 400) {
            String reason = response.readEntity(String.class);
            if (response.getStatus() == 404) {
                System.out.println("Error processing request: " + reason + " for URL: " + target.getUri().toString());
                return null;
            } else if (response.getStatus() == 400) {
                if (reason.contains("already exists"))
                    return response;
                else {
                    System.out.println("Error processing request: " + reason + " for URL: " + target.getUri().toString());
                    return null;
                }
            } else if (response.getStatus() == 401) {
                if (reason.contains("Unauthorized")) {
                    System.out.println("You are not authorized to perform this action. Please verify your login credentials are valid, and you have the correct administrative rights, and try again.");
                    return null;
                } else {
                    System.out.println("Error processing request: " + reason + " for URL: " + target.getUri().toString());
                    return null;
                }
            }
            System.out.println(response.getStatus() + " Error from API: " + reason + " for payload {" + payload + "}");
            return null;
        }
        return response;
    }

    private Response post(WebTarget target, String data) {

        if (Config.getTenant().length() > 0) {
            return handleErrors(target
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .header("x-jama-tenant", Config.getTenant())
                    .post(Entity.entity(data, MediaType.APPLICATION_JSON_TYPE)), target, data);
        }
//        System.out.println("POST: " + data);
        return handleErrors(target
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(data, MediaType.APPLICATION_JSON_TYPE)), target, data);
    }

    public File getFile(Long jamaId, String filename) throws ParseException, IOException {

        File response = downloadAttachmentFile(jamaId, filename);
        if (response == null)
            return null;
        return response;
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


    public Long createAttachmentAndUploadFileToProject(Attachment attachment) throws IOException {
        Long id = createAttachmentObject(attachment.getName(), attachment.getDescription());
        if (id != null) {
            uploadAttachment(id, attachment.getFile());
            return id;
        }
        return null;
    }

    private Long createAttachmentObject(String attachmentName, String description) {
        String resource = "projects/" + String.valueOf(Config.getProjectId()) + "/attachments";
        JSONObject paylaod = new JSONObject();
        JSONObject fields = new JSONObject();
        fields.put("name", attachmentName);
        fields.put("desc", description);
        paylaod.put("fields", fields);
        return postAttachment(resource, paylaod);
    }

    private boolean uploadAttachment(Long attachmentID, File file) throws IOException {
        String resource = "attachments/" + String.valueOf(attachmentID) + "/file";
        return uploadAttachment(file, resource);
    }

    public boolean associateAttachmentToItem(Long itemId, Long attachmentID) {
        String resource = "items/" + String.valueOf(itemId) + "/attachments";
        JSONObject paylaod = new JSONObject();
        paylaod.put("attachment", attachmentID);
        Response response = post(resource, paylaod);
        if (response.getStatus() == 201)
            return true;
        return false;
    }
}

