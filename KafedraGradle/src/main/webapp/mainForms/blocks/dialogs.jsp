<%@ page language="java" contentType="text/html" pageEncoding="cp1251"%>
<%@page import="ru.guap.config.WebConfig" %>

<div id="load" title="�������� LONG ����� �������� �����">
	<form action="<%=WebConfig.JSP_PREFIX %>/dbfupload" method="post" enctype="multipart/form-data">
        <input type="file" name="dbf"><br>
        <input type="submit" name="sumbit">
    </form>
</div>

<div id="dialog_report" title="�������� ������">
    <form action="<%=WebConfig.JSP_PREFIX %>/GetReport" method="get" enctype="multipart/form-data">
        <select name="type">
            <option value="0">������� �������</option>
            <option value="1">�������� �������</option>
            <option value="2">������� + �������� ��������</option>
        </select>
        <input type="submit" name="submit" value="������� �����">
    </form>
</div>

<div id="norm" title="������ ����� �����">
	<table>
		<tr>
			<td>
				������� �����: 
			</td>
			<td>
				800
			</td>
		</tr>
		<tr>
			<td>
				����� �����:
			</td>
			<td>
				<input type="text" name="newNorm"><br>
			</td>
		</tr>
		<tr>
			<td>
				<input type="submit" value="��������">
			<td>
		</td>
	</table>
</div>
