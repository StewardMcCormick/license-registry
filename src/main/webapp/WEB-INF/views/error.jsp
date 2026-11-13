<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head><title>Ошибка</title></head>
<body>
<h1>Ошибка</h1>
<p>Статус: ${status}</p>
<p>${message}</p>
<a href="${pageContext.request.contextPath}/">На главную</a>
</body>
</html>
