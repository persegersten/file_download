# file_download
Utility program for downloading files from a Google Drive

# configuration
Configuration files can be found in 'src/main/resources'.
Copy the file 'download.config.template' to 'donwload.config' and update the configuration.
Copy the file 'credentials.json.template' to 'credentials.json' and update the configuration. You must
enable youre Drive API at https://console.developers.google.com

drive.files - File name of google drive folder structure.
local.root - Path to local storage folder.
extensions= - Comma separated list of file extensions of files to download from remote drive.

Copy the file 'resourse/credentials.json.template' to 'resourse/credentials.json'
You must register and autohrize access to your drive. See see https://developers.google.com/adwords/api/docs/guides/authentication

# run application
Start application from Gradle:

$ gradle run --scan

