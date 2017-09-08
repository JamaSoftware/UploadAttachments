
from jama_config import JamaConfig
import requests
from requests import HTTPError



class Client():
    def __init__(self):
        self.jama_config = JamaConfig()
        self.auth = self.jama_config.auth
        self.verify = self.jama_config.verify_ssl
        self.seconds = 2


    def postAttachmentObject(self, attachmentObject, projectId):
        url = self.jama_config.rest_url + "projects/{}/attachments/".format(projectId)
        try:
            response = requests.post(url, auth=self.auth, verify=self.verify, json=attachmentObject)
            return response
        except HTTPError as e:
            print("error occurred")
            return None


    def associateAttachmentWithItem(self, itemId, attachmentJson):
        url = self.jama_config.rest_url + "items/{}/attachments".format(itemId)
        try:
            response = requests.post(url, auth=self.auth, verify=self.verify, json=attachmentJson)
            if response.status_code == 200 or response.status_code == 201:
                return True
            else:
                return False
        except HTTPError as e:
            print("error occurred")
            return None


    def uploadAttachmentFile(self, fileName, attachmentId):
        url = self.jama_config.rest_url + "attachments/{}/file".format(attachmentId)
        files = {'file': open(fileName, 'rb')}
        try:
            response = requests.put(url, files=files, auth=self.auth, verify=self.verify)
            if response.status_code == 200 or response.status_code == 201:
                return True
            else:
                return False
        except HTTPError as e:
            print("error occurred")
            return None


    def downloadAttachmentFile(self, attachmentId, filename):
        url = self.jama_config.rest_url + "attachments/{}/file".format(attachmentId)
        try:
            response = requests.get(url, auth=self.auth, verify=self.verify)
            if response.status_code == 200 or response.status_code == 201:
                file = open(filename, "w")
                file.write(response.content)
                file.close()
                return True
            else:
                return False
        except HTTPError as e:
            print("error occurred")
            return None
