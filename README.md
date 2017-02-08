there is two ways to upload/download files from ocsg.

1. java API usage:

    Configuration conf = TableConfigruation.getConf();
    OutputStream out = ...
    Download download = DownloadFactory.getDownload(conf, path);
    download.write(out);
    download.close();
    out.close();

    InputStream in = ...
    Upload upload = UploadFactory.getInstance(fileSize, THRESTHOLD);
    String returnPath = upload.upload(conf, in, fileName, fileSize);
    in.close();


2. java HTTP Client usage:

    Client client = new Client();
    client.upload(uploadUrl, in, fileLength, fileName);
    in.close();

    DownloadResponse response = client.download(downloadUrl, filePath);
    InputStream in = response.getInputStream();