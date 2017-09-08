package com.jamasoftware.services.rest.attachments;


public class Config {
    private static Long projectId = 20181L;

    private static String jamaUrl = "https://{base_url}.jamacloud.com";
    private static String restUrl = jamaUrl + "/rest/v1/";
    private static String username = "username";
    private static String password = "password";
    private static String tenant = "";
    private static Long itemId = 2116486L;

    public static String getFileName() {
        return fileName;
    }

    public static void setFileName(String fileName) {
        Config.fileName = fileName;
    }

    private static String fileName = "input.txt";


    public static Long getItemId() {
        return itemId;
    }

    public static void setItemId(Long itemId) {
        Config.itemId = itemId;
    }


    public static Long getProjectId() {
        return Config.projectId;
    }

    public static void setProjectId(Long projectId) {
        Config.projectId = projectId;
    }

    public static String getJamaUrl() {
        return Config.jamaUrl;
    }

    public static void setJamaUrl(String jamaUrl) {
        Config.jamaUrl = jamaUrl;
    }

    public static String getUsername() {
        return Config.username;
    }

    public static void setUsername(String username) {
        Config.username = username;
    }

    public static String getPassword() {
        return Config.password;
    }

    public static void setPassword(String password) {
        Config.password = password;
    }

    public static String getRestUrl() {
        return Config.restUrl;
    }

    public static void setRestUrl(String restUrl) {
        Config.restUrl = restUrl;
    }

    public static String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        Config.tenant = tenant;
    }
}
