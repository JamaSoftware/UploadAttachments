class JamaConfig:
    def __init__(self):
        # please fill in the CONFIG section below:
        self.username = "username"
        self.password = "password"
        self.base_url = "https://{base_url}.jamacloud.com"
        self.filename = "input.txt"
        self.itemId = 2116486
        self.projectId = 20181


        # NOT part of the CONFIG section
        self.auth = (self.username, self.password)
        self.rest_url = self.base_url + "/rest/v1/"
        self.verify_ssl = True
