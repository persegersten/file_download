# file_download
Utility program for downloading files from a Google Drive

# configuration
Configuration files can be found in 'src/main/resources'.
Copy the file 'download.config.template' to 'donwload.config' and update the configuration.

Modify following parameters:

*drive.files* - File name of google drive folder structure.

*local.root* - Path to local storage folder.

*extensions* - Comma separated list of file extensions of files to download from remote drive.

You must enable your Drive API at https://console.developers.google.com
You must also register and authorize access to your drive. Follow the istructions on https://developers.google.com/drive/api/v3/quickstart/java
 and download the credentials.json into the 'src/main/resources' folder.

# run application
Start application from Gradle:

$ gradle run --scan

