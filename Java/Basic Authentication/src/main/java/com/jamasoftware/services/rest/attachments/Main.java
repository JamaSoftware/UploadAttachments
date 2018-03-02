package com.jamasoftware.services.rest.attachments;

import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        JamaClient jamaClient = new JamaClient();

        //1. Create Attachment item in Jama and upload attachment file
        Attachment attachment = new Attachment();
        attachment.setName("Attachment Name");
        attachment.setDescription("Attachment Description");
        attachment.setFile(new File(Config.getFileName()));
        Long attachmentID = jamaClient.createAttachmentAndUploadFileToProject(attachment);


        //2. Associate newly created attachment with a Jama item
        boolean associationResult = jamaClient.associateAttachmentToItem(Config.getItemId(), attachmentID);
        if(associationResult == true)
            System.out.println("Attachment [" + attachmentID + "] successfully uploaded to item [" + Config.getItemId() + "]");
        else
            System.out.println("Attachment [" + attachmentID + "] could not be uploaded to item [" + Config.getItemId() + "]");


        //3. Download Attachment file from Jama
        if(attachmentID != null) {
            try {
                File downloadedFile = jamaClient.getFile(attachmentID, attachment.getFilename());
                System.out.println("Successfully downloaded file [" + downloadedFile.getAbsolutePath() + "] for attachment [" + attachmentID + "]");

            } catch (ParseException e) {
                System.out.println("Error occurred: " + e.toString());
            } catch (IOException e) {
                System.out.println("Error occurred: " + e.toString());
            }
        }
    }
}
