import json
from AttachmentUpload import Client
from AttachmentUpload import JamaConfig


client_object = Client()
config_object = JamaConfig()



# 1. Create attachment object in Jama
attachmentId = None
attachmentObject = {"fields": {"name": "Attachment Object", "description": "Attachment Description"}}
response = client_object.postAttachmentObject(attachmentObject, config_object.projectId)
if response != None:
    array = json.loads(response.content)["meta"]["location"].split("/")
    attachmentId = array[len(array) -1]
    print("Successfully created attachment [" + str(attachmentId) + "]")

else:
    print("Unable to create attachment object")
    exit()



# 2. Upload attachment file to Jama
fileResponse = client_object.uploadAttachmentFile(config_object.filename, attachmentId)
if fileResponse == True:
    print("Successfully uploaded file to attachment [" + str(attachmentId) + "]")
else:
    print("Unable to upload attachment file")
    exit()



# 3. Associate attachment with Jama item
attachmentIdObject = {}
attachmentObject = {"attachment": attachmentId}
associationResponse = client_object.associateAttachmentWithItem(config_object.itemId, attachmentObject)
if associationResponse == True:
    print("Successfully associated attachment [" + str(attachmentId) + "] with Jama item [" + str(config_object.itemId) + "]")
else:
    print("Unable to associate attachment [" + str(attachmentId) + "] with Jama item [" + str(config_object.itemId) + "]")
    exit()



# 4. Download attachment file from Jama
file = client_object.downloadAttachmentFile(attachmentId, config_object.filename)
if file != None:
    print("Successfully downloaded file for attachment [" + attachmentId + "] from Jama")
else:
    print("Unable to downloaded file for attachment [" + str(attachmentId) + "] from Jama")




print("done")
