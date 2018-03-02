package com.jamasoftware.services.rest.attachments;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;

public class Attachment {
    private String name;
    private String description;
    private String filename;
    private File file;
    private String url;
    private Long jamaId;
    private Long attachmentId;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public File getFile() {
        return file;
    }

    public void setJamaId(Long jamaId) {
        this.jamaId = jamaId;
    }

    public Long getJamaId() {
        return this.jamaId;
    }

    public String getFilename() {
        return filename;
    }

    public void setFile(File file) {
        this.file = file;
        this.filename = file.getName();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAttachmentId(Long attachmentId) {
        this.attachmentId = attachmentId;
    }

    public Long getAttachmentId() {
        return attachmentId;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }
}
