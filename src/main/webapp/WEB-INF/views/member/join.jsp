<%@ page language="java" contentType="text/html; charset=EUC-KR"
    pageEncoding="EUC-KR"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="EUC-KR">
<title>Insert title here</title>
</head>
<body>
	<h1>join</h1>
	
	<div>
		<h2>ȸ������</h2>
	</div>
	<form action="join" method="POST">
	<div>
		<label for="ID">���̵� �Է��ϼ���</label>
		<input type="text" id="ID" name="member_ID">
	</div>
	<div>
		<label for="PW">��й�ȣ�� �Է��ϼ���</label>
		<input type="text" id="PW" name="member_Password">
	</div>
	<div>
		<label for="mail">E-mail�� �Է��ϼ���</label>
		<input type="text" id="mail" name="member_Email">
	</div>
	<div>
		<label for="nick">�г����� �Է��ϼ���</label>
		<input type="text" id="nick" name="member_Nick">
	</div>
	<div>
		<label for="phone">��ȭ��ȣ�� �Է��ϼ���</label>
		<input type="text" id="phone" name="member_PhoneNumber" placeholder="-�� �������ּ���.">
	</div>
	<div>
		<label for="photo">���� ����</label>
		<input type="file" id="photo" name="member_Profile">
	</div>
	<div>
		<button>�����ϱ�</button>
	</div>
	</form>
</body>
</html>