<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@page import="ru.guap.config.WebConfig" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Main</title>
</head>
<body>
    <center>
    ������� ���� LONG*.DBF �������� �����:
    </center><br>
    <form action="<%=WebConfig.JSP_PREFIX %>/dbfupload" method="post" enctype="multipart/form-data">
        <input type="file" name="dbf"><br>
        <input type="submit" name="sumbit">
    </form>
</body>
</html>