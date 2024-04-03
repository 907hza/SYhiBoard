<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<%
   // 개행문자 값을 저장한다.
   pageContext.setAttribute("newLine", "\n");
%>
<!DOCTYPE html>
<html>
<head>
<%@ include file="/WEB-INF/views/include/head.jsp" %>
<script type="text/javascript">
$(document).ready(function() {

   $("#btnList").on("click", function() {
		document.bbsForm.hiBbsSeq.value = "";
		document.bbsForm.action = "/board/list";
		document.bbsForm.submit();
   });
   
   $("#btnReply").on("click", function() {
		document.bbsForm.action = "/board/replyForm";
		document.bbsForm.submit();
   });
   
   <c:if test="${boardMe eq 'Y'}">   
	// 수정 삭제 버튼에 해당하는 이벤트는 Y 일 때만 처리할 것이기 때문에 이벤트에도 if 처리를 해준다
	// 보안과 사용자 경험 측면에서 jQuery 이벤트에도 if 처리를 해줘야한다
	// 클라이언트 측에만 if 처리를 하면 개발자 도구로 볼 수도 있기 때문에 서버측에서도 안보일 수 있도록 처리해줘야한다
	   $("#btnUpdate").on("click", function() {
			document.bbsForm.action = "/board/updateForm";
			document.bbsForm.submit();
	   });
	   
	   $("#btnDelete").on("click", function(){
			if(confirm("해당 게시물을 삭제하시겠습니까?") == true)
			{
				$.ajax({
					type:"POST",
					url:"/board/delete",
					data:{
						// c : out 을 사용하는 이유 : 단순히 를 사용해도 값이 나오지만
						// 웹 사이트에 스크립트 코드를 주입시켜서 웹 사이트를 공격할 수 있기 때문에
						// html 코드를 해석하지 못하도록  c : out 으로 막을 수 있어서 중요한 정보들을 전달하고 교환하는 경우에는 c : out 을 사용하는 것이 적절하다
						hiBbsSeq: <c:out value="${hiBbsSeq}" />
					},
					dataType:"JSON",
					beforeSend:function(xhr)
					{
						xhr.setRequestHeader("AJAX","true");
					},
					success:function(response)
					{
						if(response.code == 0)
						{
							alert("게시물이 삭제되었습니다.");
							location.href = "/board/list";
						}
						else if(response.code == 400)
						{
							alert("파라미터 값에 오류가 있습니다.");
						}
						else if(response.code == 403)
						{
							alert("게시물 삭제 권한이 없습니다.");
						}
						else if(response.code == 404)
						{
							alert("해당 게시물이 존재하지 않습니다.");
							// 게시물이 없는건 페이지도 보여줄 수 없으니까 리스트 페이지로 넘겨버리장
							location.href = "/board/list";
						}
						else if(response.code == 500)
						{
							alert("게시물 삭제 중 오류가 발생했습니다.");
						}
						else if(response.code == -999)
						{
							alert("답변이 존재하여 삭제할 수 없습니다.");
						}
						else
						{
							alert("게시물 삭제 중 알 수 없는 오류가 발생했습니다.");
						}
					},
					error:function(xhr, status, error)
					{
						icia.common.error(error);
					}
				});
			}
	   });
   </c:if>

});
</script>

</head>
<body>

<%@ include file="/WEB-INF/views/include/navigation.jsp" %>
<div class="container">
   <h2>게시물 보기</h2>
   <div class="row" style="margin-right:0; margin-left:0;">
      <table class="table">
         <thead>
            <tr class="table-active">
               <th scope="col" style="width:60%">
                  <c:out value="${hiBoard.hiBbsTitle}" /><br/>
                  <c:out value="${hiBoard.userName}" /> &nbsp;&nbsp;&nbsp;
                  <a href="mailto:${hiBoard.userEmail}" style="color:#828282;">
                  <c:out value="${hiBoard.userEmail}" /></a>

				  <c:if test="${!empty hiBoard.hiBoardFile}" ><!-- get 방식으로 보내고있음 -->
                  	&nbsp;&nbsp;&nbsp;<a href="/board/download?hiBbsSeq=${hiBoard.hiBoardFile.hiBbsSeq}" style="color:#000;">[첨부파일]</a>
                  </c:if>    
                  
               </th>
               <th scope="col" style="width:40%" class="text-right">
                  조회 : 
                  <fmt:formatNumber type="number" maxFractionDigits="3" value="${hiBoard.hiBbsReadCnt}" /><br/>
                  ${hiBoard.regDate}
               </th>
            </tr>
         </thead>
         <tbody>
           <tr>
               <td colspan="2"><pre><c:out value="${hiBoard.hiBbsContent}" /></pre></td>
           </tr>
         </tbody>
         <tfoot>
           <tr>
               <td colspan="2"></td>
           </tr>
         </tfoot>
      </table>
   </div>
   
   <button type="button" id="btnList" class="btn btn-secondary">리스트</button>
   <button type="button" id="btnReply" class="btn btn-secondary">답글</button>
   
<c:if test="${boardMe eq 'Y'}">   
   <button type="button" id="btnUpdate" class="btn btn-secondary">수정</button>
   <button type="button" id="btnDelete" class="btn btn-secondary">삭제</button>
</c:if>   
   <br/>
   <br/>
</div>

<form id="bbsForm" name="bbsForm" method="post" >
	<input type="hidden" name="hiBbsSeq" value="${hiBbsSeq}" />
	<input type="hidden" name="searchType" value="${searchType}" />
	<input type="hidden" name="searchValue" value="${searchValue}" />
	<input type="hidden" name="curPage" value="${curPage}" />
</form>
</body>
</html>