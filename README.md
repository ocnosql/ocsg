there is two ways to upload/download files from ocsg.

1. java API usage:

    Configuration conf = TableConfigruation.getConf();<br>
    OutputStream out = ...
    Download download = DownloadFactory.getDownload(conf, path);<br>
    download.write(out);<br>
    download.close();<br>
    out.close();<br>

    InputStream in = ...
    Upload upload = UploadFactory.getInstance(fileSize, THRESTHOLD);<br>
    String returnPath = upload.upload(conf, in, fileName, fileSize);<br>
    in.close();<br>


2. java HTTP Client usage:

    Client client = new Client();<br>
    client.upload(uploadUrl, in, fileLength, fileName);<br>
    in.close();<br>

    DownloadResponse response = client.download(downloadUrl, filePath);<br>
    InputStream in = response.getInputStream();<br>