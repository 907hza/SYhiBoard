<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<!DOCTYPE html>
<html>
<head>
<%@ include file="/WEB-INF/views/include/head.jsp" %>
<!-- // 하이보드가 없는 경우 화면을 띄우는 컨트롤러에서 포스트로 보냈지만 보드가 없어도 모델맵에 담겨져서 생성될 수도 있기 때문에 여기서도 따로 처리해줌 -->
<script type="text/javascript">

$(document).ready(function() {

<c:choose> 
	<c:when test="${empty hiBoard}" >
		alert("답변할 게시물이 존재하지 않습니다.");
		location.href = "/board/list";
	</c:when>
	
	<c:otherwise>
	   $("#hiBbsTitle").focus();
	 
	   $("#btnReply").on("click", function() {
	      
	      $("#btnReply").prop("disabled", true);  // 답변 버튼 비활성화
			
	      if($.trim($("#hiBbsTitle").val()).length <= 0)
	      {
	    	  alert("제목을 입력하세요");
	    	  $("#hiBbsTitle").val("");
	    	  $("#hiBbsTitle").focus();
	      
	    	  $("#btnReply").prop("disabled", false);
	    	  return;
	      }
	      
	      if($.trim($("#hiBbsContent").val()).length <= 0)
	      {
	    	  alert("내용을 입력하세요");
	    	  $("#hiBbsContent").val("");
	    	  $("#hiBbsContent").focus();
	    	  
	    	  $("#btnReply").prop("disabled", false);
	    	  return;
	      }
	      
	      var form = $("#replyForm")[0];
	      var formData = new FormData(form);
	      
	      $.ajax({
	    	  type:"POST",
	    	  enctype:"multipart/form-data",
	    	  url:"/board/replyProc",
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
	    			   alert("답변이 등록되었습니다.");
	    			   document.bbsForm.hiBbsSeq.value = "${hiBoard.hiBbsSeq}";
	    			   location.href = "/board/list";
	    		   }
	    		   else if(response.code == 400)
	    		   {
	    			   alert("입력값이 올바르지 않습니다.");
	    			   $("#btnReply").prop("disabled", false);
	    		   }
	    		   else if(response.code == 404)
	    		   {
	    			   alert("해당 게시물이 존재하지 않습니다.");
	    			   location.href = "/board/list";
	    		   }
	    		   else if(response.code == 500)
	    		   {
	    			   alert("답변 증록 중 오류가 발생했습니다.");
	    			   $("#btnReply").prop("disabled", false);
	    		   }
	    		   else
	    		   {
	    			   alert("답변 등록 중 알 수 없는 오류가 발생했습니다.");
	    			   $("#btnReply").prop("disabled", false);
	    		   }
	    	  },
	    	  error:function(error)
	    	  {
	    		  icia.common.error(error);
	    		  alert("게시물 답변 중 오류가 발생했습니다");
	    		  
	    		  $("#btnReply").prop("disabled", false);
	    	  }
	      });
	   });
	
	 
	   $("#btnList").on("click", function() {
			document.bbsForm.action = "/board/list";
			document.bbsForm.submit();
	   });
   </c:otherwise>
</c:choose>
});
</script>
</head>
<body>

<c:if test="${!empty hiBoard}">
	<%@ include file="/WEB-INF/views/include/navigation.jsp" %>
	<div class="container">
	   <h2>게시물 답변</h2>
	   <form name="replyForm" id="replyForm" method="post" enctype="multipart/form-data">
	      <input type="text" name="userName" id="userName" maxlength="20" value="${user.userName}" style="ime-mode:active;" class="form-control mt-4 mb-2" placeholder="이름을 입력해주세요." readonly />
	      <input type="text" name="userEmail" id="userEmail" maxlength="30" value="${user.userEmail}"  style="ime-mode:inactive;" class="form-control mb-2" placeholder="이메일을 입력해주세요." readonly />
	      <input type="text" name="hiBbsTitle" id="hiBbsTitle" maxlength="100" style="ime-mode:active;" value="" class="form-control mb-2" placeholder="제목을 입력해주세요." required />
	      <div class="form-group">
	         <textarea class="form-control" rows="10" name="hiBbsContent" id="hiBbsContent" style="ime-mode:active;" placeholder="내용을 입력해주세요" required></textarea>
	      </div>
	      <input type="file" name="hiBbsFile" id="hiBbsFile" class="form-control mb-2" placeholder="파일을 선택하세요." required />
	      <input type="hidden" name="hiBbsSeq" value="${hiBoard.hiBbsSeq}" />
	      <input type="hidden" name="searchType" value="${searchType}" />
	      <input type="hidden" name="searchValue" value="${searchValue}" />
	      <input type="hidden" name="curPage" value="${curPage}" />
	   </form>
	   
	   <div class="form-group row">
	      <div class="col-sm-12">
	         <button type="button" id="btnReply" class="btn btn-primary" title="답변">답변</button>
	         <button type="button" id="btnList" class="btn btn-secondary" title="리스트">리스트</button>
	      </div>
	   </div>
	</div>
</c:if>
<!-- 불필요한 값을 보내지 않기 위해 따로 리스트 전용 폼 생성 -->
<form name="bbsForm" id="bbsForm" method="post" >
	<input type="hidden" name="hiBbsSeq" value="${hiBoard.hiBbsSeq}" />
	<input type="hidden" name="searchType" value="${searchType}" />
	<input type="hidden" name="searchValue" value="${searchValue}" />
	<input type="hidden" name="curPage" value="${curPage}" />
</form>

</body>
</html>