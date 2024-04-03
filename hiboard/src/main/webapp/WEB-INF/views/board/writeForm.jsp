<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<!DOCTYPE html>
<html>
<head>
<%@ include file="/WEB-INF/views/include/head.jsp" %>
<script type="text/javascript">
$(document).ready(function() {
    
   $("#hiBbsTitle").focus();
   
   $("#btnWrite").on("click", function() {

	   // 버튼이 눌러지면 비활성화시키고 ajax 통신이 돌아올 때까지 비활성화
	   // 다중 클릭하여 오류가 나는 것을 방지하기 위해서
		$("#btnWrite").prop("disabled", true);
	   
	   if($.trim($("#hiBbsTitle").val()).length <= 0)
		{
			alert("제목을 입력하세요");
			$("#hiBbsTitle").val("");
			$("#hiBbsTitle").focus();
			
			// 버튼 다시 활성화
			$("#btnWrite").prop("disabled", false);
			return;
		}
		
		if($.trim($("#hiBbsContent").val()).length <= 0)
		{
			alert("내용을 입력하세요");
			$("#hiBbsContent").val("");
			$("#hiBbsContent").focus();
			
			// 버튼 다시 활성화
			$("#btnWrite").prop("disabled", false);
			return;
		}
		
		var form = $("#writeForm")[0] // 폼을 보내기 위해서 폼을 담을 변수 생성 , [0] 는 폼의 첫번째 양식에 해당하는 값을 가져오는 것>> 인덱스 번호같은
		var formData = new FormData(form); // 자바스크립트에서 폼 데이터를 다루는 객체(폼 데이터를 객체 생성하여 폼을 전달)
		
		$.ajax({
			type:"POST",
			enctype:"multipart/form-data",
			url:"/board/writeProc",
			data:formData,
			processData:false, // formData를 스트링으로 변환하지 않는 것을 의미
			contentType:false, // content-type 헤더가 multipart/form-data 로 전송
			cache:false, // 캐시 저장안함
			timeout:600000, // 연결 대기시간을 설정하여 연결이 유지되도록
			
			beforeSend:function(xhr)
			{
				xhr.setRequestHeader("AJAX", "true");
			},
			success:function(response)
			{
				if(response.code == 0)
				{
					alert("게시물이 등록되었습니다");	
					location.href = "/board/list";
					
					// 내가 왔던 페이지로 돌아가는 방법 > 보통 이렇게 안함
					// document.bbsForm.action = "/board/list";
					// document.bbsForm.submit();
				}
				else if(response.code == 400)
				{
					alert("파라미터 값이 올바르지 않습니다");
					$("#btnWrite").prop("disabled", false); // 버튼 활성화
					$("#hiBbsTitle").focus();
				}
				else if(response.code == 500)
				{
					alert("게시물 등록 중 오류가 발생했습니다");
					$("#btnWrite").prop("disabled", false); // 버튼 활성화
					$("#hiBbsTitle").focus();
				}
				else 
				{
					alert("게시물 등록 중 알 수 없는 오류가 발생했습니다");
					$("#btnWrite").prop("disabled", false); // 버튼 활성화
					$("#hiBbsTitle").focus();
				}
			},
			error:function(error)
			{
				icia.common.error(error);
				alert("게시물 등록 중 오류가 발생했습니다");
				$("#btnWrite").prop("disabled", false); // 버튼 활성화
			}
		});

   });
   
   $("#btnList").on("click", function() {
		document.bbsForm.action = "/board/list";
		document.bbsForm.submit();
   });
});
</script>
</head>
<body>
<%@ include file="/WEB-INF/views/include/navigation.jsp" %>
<div class="container">
   <h2>게시물 쓰기</h2> <!-- 첨부파일은 무조건 이렇게 작성해줘야한다 enctype="multipart/form-data" -->
   <form name="writeForm" id="writeForm" method="post" enctype="multipart/form-data">
      <input type="text" name="userName" id="userName" maxlength="20" value="${user.userName}" style="ime-mode:active;" class="form-control mt-4 mb-2" placeholder="이름을 입력해주세요." readonly />
      <input type="text" name="userEmail" id="userEmail" maxlength="30" value="${user.userEmail}" style="ime-mode:inactive;" class="form-control mb-2" placeholder="이메일을 입력해주세요." readonly />
      <input type="text" name="hiBbsTitle" id="hiBbsTitle" maxlength="100" style="ime-mode:active;" class="form-control mb-2" placeholder="제목을 입력해주세요." required />
      <div class="form-group">
               <input type="file" onchange="readURL(this);"> 
		 <img id="preview" />
         <textarea class="form-control" rows="10" name="hiBbsContent" id="hiBbsContent" style="ime-mode:active;" placeholder="내용을 입력해주세요" required>
         </textarea>
      </div>
      <input type="file" id="hiBbsFile" name="hiBbsFile" class="form-control mb-2" enctype="multipart/form-data" placeholder="파일을 선택하세요." required />
      <div class="form-group row">
         <div class="col-sm-12">
            <button type="button" id="btnWrite" class="btn btn-primary" title="저장">저장</button>
            <button type="button" id="btnList" class="btn btn-secondary" title="리스트">리스트</button>
         </div>
      </div>
   </form>

</div>

<form id="bbsForm" name="bbsForm" method="post" >
	<input type="hidden" name="searchType" value="${searchType}" />
	<input type="hidden" name="searchValue" value="${searchValue}" />
	<input type="hidden" name="curPage" value="${curPage}" />
</form>

</body>
</html>