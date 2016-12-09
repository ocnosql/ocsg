<%@ page contentType="text/html; charset=UTF-8"%>
<html>
<head>
<script>
    ///获得文件的大小(单位字节)
    function getFileSize(fileId) {
        var dom = document.getElementById(fileId);
        try {
            var size = dom.files[0].size;
            document.getElementById('myfile_length').value = size;
        } catch (e) {
            alert(e);
        }
    }

    function setFileSize(){
        document.getElementsByTagName("input");
    }
</script>
</head>
<body>
<h2>Hello World!</h2>
<form action="<%=request.getContextPath()%>/upload" method="post" enctype="multipart/form-data" onsubmit="getFileSize('myfile')">
    <input type="text" name="m" value="aaa"/><br />
    <input type="text" name="txt1" value="bbb"/><br />
    <input id="myfile_length" type="hidden" name="myfile_length" value=""/>
    <input id="myfile" type="file" name="myfile" /><br />
    <%--<input type="file" name="myImg" /><br />--%>
    <input type="submit" value="提交" />
</form>
</body>
</html>
