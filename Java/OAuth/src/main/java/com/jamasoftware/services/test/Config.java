package com.jamasoftware.services.test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Config {
    public static String fileSeparator;

    private static String restUrl;
    private static String baseUrl = "https://{base_url}.jamacloud.com";
    private static String clientID = "client_id";
    private static String clientSecret = "client_secret";

    private static String attachmentName = "Newly Created Attachment";
    private static String attachmentFileName = "/Users/ibilal/OneDrive - Jama Software/IdeaProjects/uploadAttachmentsample/file000693070568.jpg";
    private static Long itemID = 129936L;
    private static Long projectId = 77L;
    private static String projectDirectory;

    static {
        Path currentRelativePath = Paths.get("");
        String currentPath = currentRelativePath.toAbsolutePath().toString();
        projectDirectory = currentPath;
        String system = System.getProperty("os.name");
        if (system.contains("Windows")) {
            fileSeparator = "\\";
        } else {
            fileSeparator = File.separator;
        }

        if (!baseUrl.startsWith("https://") && !baseUrl.startsWith("http://"))
            baseUrl = "https://" + baseUrl;

        restUrl = baseUrl.endsWith("/") ? baseUrl + "rest/v1/" : baseUrl + "/rest/v1/";
    }

    public static Long getProjectId() {
        return projectId;
    }

    public static String getBaseUrl() {
        return baseUrl;
    }

    public static String getClientID() {
        return clientID;
    }

    public static String getClientSecret() {
        return clientSecret;
    }

    public static String getRestUrl() {
        return restUrl;
    }

    public static String getProjectDirectory() {
        return projectDirectory;
    }

    public static String getAttachmentName() {
        return attachmentName;
    }

    public static String getAbsolutePathToFile() {
        return attachmentFileName;
    }

    public static Object getItemID() {
        return itemID;
    }
}
