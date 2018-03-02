package com.jamasoftware.services.test;

import com.jamasoftware.services.test.client.*;
import java.io.File;

public class Main {

    public static void main(String[] args) throws Exception {

        OAuthJamaClient jamaClient = new OAuthJamaClient();
            //0. Validate user client_id and client_secret
        if (jamaClient.validate(Config.getBaseUrl(), Config.getClientID(), Config.getClientSecret(), Config.getProjectId())) {
            System.out.println("Successfully validated user client id and client secret");
            //1. Create attachment object in Jama
            //2. Upload file to newly attachment object
            String attachmentname = Config.getAttachmentName();
            File fileToUpload = new File(Config.getAbsolutePathToFile());

            Long attachmentID = jamaClient.createAttachmentAndUploadFileToProject(attachmentname, fileToUpload);

            //3. Associate newly created attachment object to Jama item
            Boolean value = jamaClient.associateAttachmentWithItem(attachmentID, Config.getItemID());
            if (value)
                System.out.println("Successfully associated attachment [" + String.valueOf(attachmentID) + "] with item [" + String.valueOf(Config.getItemID()) + "]");
            else
                System.out.println("Unable to associate attachment [" + String.valueOf(attachmentID) + "] with item [" + String.valueOf(Config.getItemID()) + "]");

        } else {
            System.out.println("Sorry, could not authenticate your client id and client secret");
        }
    }
}
