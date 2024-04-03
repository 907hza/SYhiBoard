<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<!DOCTYPE html>
<html>
<head>
<%@ include file="/WEB-INF/views/include/head.jsp" %>
<script type="text/javascript">
$(document).ready(function() {
    
   $("#userId").focus();
   
   $("#btnReg").on("click", function() {

	   // 공백 체크
	   var emptCheck = /\s/g;
	   // 영문 대소문자, 숫자로만 이루어진 4-12 자리 정규식
	   var idPwdCheck = /^[a-zA-Z0-9]{4,12}$/;
	   
	   if($.trim($("#userId").val()).length <= 0)
		{
			alert("사용자 아이디를 입력하세요");
			$("#userId").val("");
			$("#userId").focus();
			return;
		}
	   
	   if(emptCheck.test($("#userId").val()))
		{
		   alert("사용자 아이디는 공백을 포함할 수 없습니다");
		   $("#userId").focus();
		   return;
		}
	   
	   if(!idPwdCheck.test($("#userId").val()))
		{
		   alert("사용자 아이디는 4-12자 영문 대소문자와 숫자로만 입력하세요");
		   $("#userId").focus();
		   return;
		}
	   
	   if($.trim($("#userPwd1").val()).length <= 0)
		{
		   alert("사용자 비밀번호를 입력하세요");
		   $("#userPwd1").val("");
		   $("#userPwd1").focus();
		   return;
		}
	   
	   if(emptCheck.test($("#userPwd1").val()))
		{
		   alert("비밀번호에는 공백을 포함할 수 없습니다");
		   $("#userPwd1").val("");
		   $("#userPwd1").focus();
		   return;
		}
	   
	   if(!idPwdCheck.test($("#userPwd1").val()))
		{
		   alert("사용자 비밀번호는 4-12자 영문 대소문자와 숫자로만 입력하세요");
		   $("#userPwd1").val("");
		   $("#userPwd1").focus();
		   return;
		}
	   
	   if($.trim($("#userPwd2").val()).length <= 0)
		{
		   alert("비밀번호 확인란을 입력하세요");
		   $("#userPwd2").val("");
		   $("#userPwd2").focus();
		   return;
		}
	   
	   if($("#userPwd1").val() != $("#userPwd2").val())
		{
		   alert("비밀번호가 일치하지 않습니다");
		   $("#userPwd2").focus();
		   return;
		}
	   
	   $("#userPwd").val($("#userPwd1").val());
	   
	   if($.trim($("#userName").val()).length <= 0)
		{
		   alert("사용자 이름을 입력하세요");
		   $("#userName").val("");
		   $("#userName").focus();
		   return;
		}
	   
	   if($.trim($("#userEmail").val()).length <= 0)
		{
		   alert("사용자 이메일을 입력하세요");
		   $("#userEmail").val("");
		   $("#userEmail").focus();
		   return;
		}
	   
	   if(!fn_validateEmail($("#userEmail").val()))
		{
		   alert("사용자 이메일 형식이 올바르지 않습니다");
		   $("#userEmail").focus();
		   return;
		}
	   
	   // 아이디 중복 체크 에이젝스
	   $.ajax({
		  type:"POST",
		  url:"/user/idCheck",
		  data:{
			  userId:$("#userId").val()
		  },
		  datatype:"JSON",
		  beforeSend:function(xhr)
		  {
			xhr.setRequestHeader("AJAX","true");  
		  },
		  success:function(response) // JSTL, EL 이 없기에 프론트에서 실행하는 것
		  {
			if(response.code == 0)
			{
				alert("중복 아이디가 아니므로, 사용 가능한 아이디입니다");
				fn_userReg();
			}
			else if(response.code == 100)
			{
				alert("중복 아이디입니다");
				$("#userId").focus();
			}
			else if(response.code == 400)
			{
				alert("파라미터 값이 올바르지 않습니다");
				$("#userId").focus();
			}
			else
			{
				alert("오류가 발생했습니다");
				$("#userId").focus();
			}
		  },
		  error:function(xhr, status, error)
		  {
			  icia.common.error(error); // 오류났을 때만 나타내는 로그
		  }
	   });
   });
});

function fn_userReg()
{
   $.ajax({
		type:"POST",
		url:"/user/regProc",
		data:{
			userId:$("#userId").val(),
			userPwd:$("#userPwd").val(),
			userName:$("#userName").val(),
			userEmail:$("#userEmail").val()
		},
		datatype:"JSON",
		  beforeSend:function(xhr)
		  {
			xhr.setRequestHeader("AJAX","true");  
		  },
		  success:function(response)
		  {
			  if(response.code == 0)
			  {
				 alert("회원가입이 되었습니다");
				 location.href = "/";
			  }
			  else if(response.code == 100)
			  {
				 alert("회원 아이디가 중복되었습니다");
				 $("#userId").focus();
			  }
			  else if(response.code == 400)
			  {
				 alert("파라미터 값이 올바르지 않습니다");
				 $("#userId").focus();
			  }
			  else if(response.code == 500)
			  {
				 alert("회원가입 중 오류가 발생하였습니다");
				 $("#userId").focus();
			  }
			  else // 원인 불명 오류와 구분짓기 위해서
			  {
				 alert("회원가입 중 알 수 없는 오류가 발생하였습니다");
				 $("#userId").focus();
			  }
		  },
		  error:function(xhr, status, error)
		  {
			  icia.common.error(error);
		  }
   });
}

function fn_validateEmail(value)
{
   var emailReg = /^([\w-\.]+@([\w-]+\.)+[\w-]{2,4})?$/;
   
   return emailReg.test(value);
}
</script>
</head>
<body>
<%@ include file="/WEB-INF/views/include/navigation.jsp" %>
<div class="container">
    <div class="row mt-5">
       <h1>회원가입</h1>
    </div>
    <div class="row mt-2">
        <div class="col-12">
            <form id="regForm" method="post">
                <div class="form-group">
                    <label for="username">사용자 아이디</label>
                    <input type="text" class="form-control" id="userId" name="userId" placeholder="사용자 아이디" maxlength="12" />
                </div>
                <div class="form-group">
                    <label for="userPwd1">비밀번호</label>
                    <input type="password" class="form-control" id="userPwd1" name="userPwd1" placeholder="비밀번호" maxlength="12" />
                </div>
                <div class="form-group">
                    <label for="userPwd2">비밀번호 확인</label>
                    <input type="password" class="form-control" id="userPwd2" name="userPwd2" placeholder="비밀번호 확인" maxlength="12" />
                </div>
                <div class="form-group">
                    <label for="userName">사용자 이름</label>
                    <input type="text" class="form-control" id="userName" name="userName" placeholder="사용자 이름" maxlength="15" />
                </div>
                <div class="form-group">
                    <label for="userEmail">사용자 이메일</label>
                    <input type="text" class="form-control" id="userEmail" name="userEmail" placeholder="사용자 이메일" maxlength="30" />
                </div>
                <input type="hidden" id="userPwd" name="userPwd" value="" />
                <button type="button" id="btnReg" class="btn btn-primary">등록</button>
            </form>
        </div>
    </div>
</div>
</body>
</html>