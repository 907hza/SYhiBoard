<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="/WEB-INF/views/include/taglib.jsp" %>

<!DOCTYPE html>
<html>
<head>

<%@ include file="/WEB-INF/views/include/head.jsp" %>
<%--/**
servlet-context 에서 /index 를 읽어준다
요청보낼때는 항상 / 붙여줘야한다
view 가 주축이기 때문에 우리가 실행할 파일은 무조건 view 아래 

리소스 폴더/ js 로 가서 icia.--.js 파일에서 ajax 에서 사용하는 함수를 볼 수 있다 **/ --%>
<style>
body {
  /* padding-top: 40px; */
  padding-bottom: 40px;
  /* background-color: #eee; */
}

.form-signin {
  max-width: 330px;
  padding: 15px;
  margin: 0 auto;
}
.form-signin .form-signin-heading,
.form-signin .checkbox {
  margin-bottom: 10px;
}
.form-signin .checkbox {
  font-weight: 400;
}
.form-signin .form-control {
  position: relative;
  -webkit-box-sizing: border-box;
  -moz-box-sizing: border-box;
  box-sizing: border-box;
  height: auto;
  padding: 10px;
  font-size: 16px;
}
.form-signin .form-control:focus {
  z-index: 2;
}
.form-signin input[type="text"] {
  margin-bottom: 5px;
  border-bottom-right-radius: 0;
  border-bottom-left-radius: 0;
}
.form-signin input[type="password"] {
  margin-bottom: 10px;
  border-top-left-radius: 0;
  border-top-right-radius: 0;
}
</style>
<script type="text/javascript">
$(document).ready(function() {
	$("#userId").focus();
	$("#userId").on("keypress",function(e){
			if(e.which == 13)
			{
				fn_loginCheck();
			}
		});

	$("#userPwd").on("keypress",function(e){
			if(e.which == 13)
			{
				fn_loginCheck();
			}	
		});
	
	$("#btnLogin").on("click",function(){
		fn_loginCheck();
	});
	$("#btnReg").on("click",function(){
		location.href="/user/regForm"; // 반드시 요청할 때 슬래시 포함해야 해당 파일을 읽어옴
	});
});

function fn_loginCheck()
{
   if($.trim($("#userId").val()).length <= 0){
      alert("아이디를 입력하세요.");
      $("#userId").val("");
      $("#userId").focus();
      return;
   }
   if($.trim($("#userPwd").val()).length <= 0){
      alert("비밀번호를 입력하세요.");
      $("#userPwd").val("");
      $("#userPwd").focus();
      return;
   }
   $.ajax({
      type:"POST",
      url:"/user/login", //맨 앞에 슬래시 반드시 붙여야만,
      data:{
         userId:$("#userId").val(),
         userPwd: $("#userPwd").val()
      },
      datatype:"JSON",
      beforeSend:function(xhr){
         xhr.setRequestHeader("AJAX","true");
      },
      success:function(response){
         if(!icia.common.isEmpty(response)){ // 
         
            icia.common.log(response);
            var code = icia.common.objectValue(response, "code", -500); //response로 받은 것 중에서 코드 값을 가져오는데, 받은 게 없으면 defalt 로 -500을 넘기라는 뜻
            if(code == 0){
            	alert("로그인 성공");
               location.href = "/board/list";
            }
            else{
               if(code == -1){
                  alert("비밀번호가 올바르지 않습니다.");
                  $("#userPwd").focus();
               }
               else if(code == 404){
                  alert("일치하는 아이디가 존재하지 않습니다.");
                  $("#userId").focus();
               }
               else if(code == 400){
                  //파라미터값의 오류는 모두 400번대 처리
                  alert("파라미터 값이 올바르지 않습니다.");
                  $("#userID").focus();
               }
               else if(code == 505)
               {
            	   alert("정지된 사용자입니다.");
               }
               else{
                  alert("오류가 발생하였습니다."); 
               }
            }
         }
         else{
            alert("오류가 발생하였습니다.");
            $("#userId").focus();
         }
      },
      complete:function(data){
         //응답이 종료되면 실행, 잘 사용하지 않는다. 
         icia.common.log(data);
      },
      error:function(xhr,status,error){
         icia.common.error(error);
      }
   });
}
</script>
</head>
<body>
<%@ include file="/WEB-INF/views/include/navigation.jsp" %>

<div class="container">

	<form class="form-signin">
	    <h2 class="form-signin-heading m-b3">로그인</h2>
		<label for="userId" class="sr-only">아이디</label>
		<input type="text" id="userId" name="userId" class="form-control" maxlength="20" placeholder="아이디">
		<label for="userPwd" class="sr-only">비밀번호</label>
		<input type="password" id="userPwd" name="userPwd" class="form-control" maxlength="20" placeholder="비밀번호">
		  
		<button type="button" id="btnLogin" class="btn btn-lg btn-primary btn-block">로그인</button>
    	<button type="button" id="btnReg" class="btn btn-lg btn-primary btn-block">회원가입</button>
	</form>
</div>
</body>
</html>