<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<!DOCTYPE html>
<html>
<head>
<%@ include file="/WEB-INF/views/include/head.jsp" %>
<script type="text/javascript">
$(document).ready(function() {

   $("#hiBbsTitle").focus();
   
   $("#btnUpdate").on("click", function() {
        // 버튼 비활성화
	    $("#btnUpdate").prop("disabled", true);
        
        if($.trim($("#hiBbsTitle").val()).length <= 0)
        {
        	alert("제목을 입력하세요");
        	$("#hiBbsTitle").val("");
        	$("#hiBbsTitle").focus();
        	// 버튼 활성화
        	$("#btnUpdate").prop("disabled", false);
        	return;
        }
        
        if($.trim($("#hiBbsContent").val()).length <= 0)
        {
        	alert("내용을 입력하세요");
        	$("#hiBbsContent").val("");
        	$("#hiBbsContent").focus();
        	// 버튼 활성화
        	$("#btnUpdate").prop("disabled", false);
        	return;
        }
        
        var form = $("#updateForm")[0];
        var formData = new FormData(form);
        
        $.ajax({
        	type:"POST",
        	enctype:"multipart/form-data",
        	url:"/board/updateProc",
        	data:formData,
        	processData:false,
        	contentType:false,
        	cache:false,
        	beforeSend:function(xhr)
        	{
        		xhr.setRequestHeader("AJAX","true");
        	},
        	success:function(response)
        	{
        		if(response.code == 0)
        		{
        			alert("게시물 수정이 완료되었습니다.");
        			document.bbsForm.gubun.value="Y";
        			document.bbsForm.action = "/board/view";
        			document.bbsForm.submit();
        		}
        		else if(response.code == 400)
        		{
        			alert("파라미터 값이 올바르지 않습니다.");
        			$("#btnUpdate").prop("disabled",false);
        		}
        		else if(response.code == 403)
        		{
        			alert("수정하실 권한이 없습니다.");
        			$("#btnUpdate").prop("disabled",false);
        		}
        		else if(response.code == 404)
        		{
        			alert("조회하신 게시물이 존재하지 않습니다.");
        			$("#btnUpdate").prop("disabled",false);
        		}
        		else if(response.code == 500)
        		{
        			alert("게시물 수정 중 오류가 발생했습니다.");
        			$("#btnUpdate").prop("disabled",false);
        		}
        		else
        		{
        			alert("게시물 수정 중 알 수 없는 오류가 발생했습니다.");
        		}
        	},
        	error:function(xhr, status, error)
        	{
        		icia.common.error(error);
        		alert("게시물 수정 중 오류가 발생했습니다.");
        		$("#btnUpdate").prop("disabled",false);
        	}
        });
   });
   
   $("#fileDelete").on("click",function(){
		$.ajax({
			
		});   
   });
   
   $("#btnList").on("click", function() {
	    document.bbsForm.hiBbsSeq.value = "";
	    document.bbsForm.action = "/board/list";
		document.bbsForm.submit();
   });

});
</script>
</head>
<body>

<%@ include file="/WEB-INF/views/include/navigation.jsp" %>
<div class="container">
   <h2>게시물 수정</h2>
   <form name="updateForm" id="updateForm" method="post" enctype="multipart/form-data">
      <input type="text" name="userName" id="userName" maxlength="20" value="${hiBoard.userName}" style="ime-mode:active;" class="form-control mt-4 mb-2" placeholder="이름을 입력해주세요." readonly />
      <input type="text" name="userEmail" id="userEmail" maxlength="30" value="${hiBoard.userEmail}"  style="ime-mode:inactive;" class="form-control mb-2" placeholder="이메일을 입력해주세요." readonly />
      <input type="text" name="hiBbsTitle" id="hiBbsTitle" maxlength="100" style="ime-mode:active;" value="${hiBoard.hiBbsTitle}" class="form-control mb-2" placeholder="제목을 입력해주세요." required />
      <div class="form-group">
         <textarea class="form-control" rows="10" name="hiBbsContent" id="hiBbsContent" style="ime-mode:active;" placeholder="내용을 입력해주세요" required>${hiBoard.hiBbsContent}</textarea>
      </div>
      <input type="file" name="hiBbsFile" id="hiBbsFile" class="form-control mb-2" placeholder="파일을 선택하세요." required />

	<c:if test="${!empty hiBoard.hiBoardFile}" >
      <div style="margin-bottom:0.3em;">[첨부파일 : ${hiBoard.hiBoardFile.fileOrgName}] &nbsp; <button type="button" id="fileDelete" name="fileDelete">X</button> </div>
	</c:if>
	
	<!-- 수정버튼을 눌러서 수정을 완료했을 떄 다시 view 페이지가 나오고 거기있는 리스트 버튼을 눌렀을 때를 감안해서 넣은 것 -->
	<input type="hidden" name="hiBbsSeq" value="${hiBoard.hiBbsSeq}" />
	<input type="hidden" name="searchType" value="${searchType}" />
	<input type="hidden" name="searchValue" value="${searchValue}" />
	<input type="hidden" name="curPage" value="${curPage}" />
   </form>
   
   <div class="form-group row">
      <div class="col-sm-12">
         <button type="button" id="btnUpdate" class="btn btn-primary" title="수정">수정</button>
         <button type="button" id="btnList" class="btn btn-secondary" title="리스트">리스트</button>
      </div>
   </div>
</div>

<!-- 수정 버튼 눌렀을 때의 제출할 수 있는 폼과 리스트 버튼을 눌렀을 때의 제출할 수 있는 폼 두개가 있어야한다잉~~ -->
<form name="bbsForm" id="bbsForm" method="post" >
	<input type="hidden" name="hiBbsSeq" value="${hiBoard.hiBbsSeq}" />
	<input type="hidden" name="searchType" value="${searchType}" />
	<!-- 수정을 했을 때는 조회수가 오르지 않도록 하기 위해서 수정했다는 구분자를 넣어서 구분하도록(Y:수정함, N:수정안함) -->
	<input type="hidden" name="gubun" value="Y" />
	<input type="hidden" name="searchValue" value="${searchValue}" />
	<input type="hidden" name="curPage" value="${curPage}" />
</form>
</body>
</html>