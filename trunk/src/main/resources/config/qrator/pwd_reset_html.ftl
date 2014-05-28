<html>
<head>
<title>Password reset</title>
</head>
<body>
<p>Hello ${name},</p>
<p>Your Qrator password has been reset as of ${date?datetime}.</p>
<p>Your username and password are:</p>
<p>
username - ${username}<br/>
password - ${password}
</p>
<p>Please contact your administrator at <a href="mailto:${admin}">${admin}</a> if you have questions.</p>
<p>
--<br/>
Qrator Admin
</p>
</body>
</html>  