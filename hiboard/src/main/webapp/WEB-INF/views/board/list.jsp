<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<!DOCTYPE html>
<html>
<head>
<%@ include file="/WEB-INF/views/include/head.jsp" %>
<script type="text/javascript">
$(document).ready(function() {
    
   $("#btnWrite").on("click", function() {
      document.bbsForm.hiBbsSeq.value= "";
      document.bbsForm.action = "/board/writeForm";
      document.bbsForm.submit();
   });
   
   $("#btnSearch").on("click", function() {
	  document.bbsForm.hiBbsSeq.value = "";
	  document.bbsForm.searchType.value = $("#_searchType").val();
	  document.bbsForm.searchValue.value = $("#_searchValue").val();
	  document.bbsForm.curPage.value = "1"; // 현재 페이지가 어떤거였는지 상관없이 조회할거니까  curPage 에 기본 1 값을 준다
	  document.bbsForm.action = "/board/list";
	  document.bbsForm.submit();
   });
});

function fn_view(bbsSeq) // 제목을 클릭했을 때
{
	document.bbsForm.hiBbsSeq.value = bbsSeq;
	document.bbsForm.action = "/board/view";
	document.bbsForm.submit();
}

function fn_list(curPage) // 페이지 번호 눌렀을 떄
{
	document.bbsForm.hiBbsSeq = "";
	document.bbsForm.curPage.value = curPage;
	document.bbsForm.action = "/board/list";
	document.bbsForm.submit();
}
</script>
</head>
<body>
<%@ include file="/WEB-INF/views/include/navigation.jsp" %>
<div class="container">
   
   <div class="d-flex">
      <div style="width:50%;">
         <h2>게시판</h2>
      </div>
      <div class="ml-auto input-group" style="width:50%;">
         <select name="_searchType" id="_searchType" class="custom-select" style="width:auto;">
            <option value="">조회 항목</option>
            <!-- 조회했을 때 조회항목과 조회값에 내가 조회한 항목과 값이 들어있도록 설정한 것 eq : ==  -->
            <option value="1" <c:if test='${searchType eq "1"}'>selected</c:if> >작성자</option>
            <option value="2" <c:if test='${searchType eq "2"}'>selected</c:if> >제목</option>
            <option value="3" <c:if test='${searchType eq "3"}'>selected</c:if> >내용</option>
         </select>
         <input type="text" name="_searchValue" id="_searchValue" value="${searchValue}" class="form-control mx-1" maxlength="20" style="width:auto;ime-mode:active;" placeholder="조회값을 입력하세요." />
         <button type="button" id="btnSearch" class="btn btn-secondary mb-3 mx-1">조회</button>
      </div>
    </div>
    
   <table class="table table-hover">
      <thead>
      <tr style="background-color: #dee2e6;">
         <th scope="col" class="text-center" style="width:10%">번호</th>
         <th scope="col" class="text-center" style="width:55%">제목</th>
         <th scope="col" class="text-center" style="width:10%">작성자</th>
         <th scope="col" class="text-center" style="width:15%">날짜</th>
         <th scope="col" class="text-center" style="width:10%">조회수</th>
      </tr>
      </thead>
      <tbody>
   
    <!-- JSTL/EL 문법 적용 -->
    <c:if test="${!empty list}" >
    	<c:forEach var="hiBoard" items="${list}" varStatus="status" >   
	      <tr>
	      <c:choose>
	      
	      	 <c:when test="${hiBoard.hiBbsIndent eq 0}" >
	         	<td class="text-center">${hiBoard.hiBbsSeq}</td>
		  	 </c:when>
		  	 
		  	 <c:otherwise>
		  	 	<td class="text-center">&nbsp;</td>
		  	 </c:otherwise>
		  	 
		  </c:choose>
	         <td>
	         	<!-- 글 번호로 해당 글을 찾을 수 있도록 함수에 보낼 것 -->
	            <a href="javascript:void(0)" onclick="fn_view(${hiBoard.hiBbsSeq})">
	      <c:if test="${hiBoard.hiBbsIndent > 0 }" >
	      	<img src="/resources/images/icon_reply.gif" style="margin-left:${hiBoard.hiBbsIndent}em" />
	      </c:if>
	               <c:out value="${hiBoard.hiBbsTitle}" />
	            </a>
	         </td>
	         <td class="text-center">${hiBoard.userName}</td>
	         <td class="text-center">${hiBoard.regDate}</td>
	         <!-- type 은 데이터 타입, maxFractionDigits 는 숫자 세자리 씩 점 찍기 위함 , value 는 출력할 값-->
	         <td class="text-center"><fmt:formatNumber type="number" maxFractionDigits="3" value="${hiBoard.hiBbsReadCnt}" /> </td>
	      </tr>
      </c:forEach>
	</c:if>
	      
      </tbody>
      <tfoot>
      <tr>
            <td colspan="5"></td>
        </tr>
      </tfoot>
   </table>
   
   <!-- 페이징 처리를 하는 부분 -->
   <nav>
      <ul class="pagination justify-content-center">

		<c:if test = "${!empty paging}" >
         <c:if test = "${paging.prevBlockPage gt 0}" ><!-- gt == > -->
         	<li class="page-item"><a class="page-link" href="javascript:void(0)" onclick="fn_list(${paging.prevBlockPage})">이전블럭</a></li></c:if>
			 
			 <!-- 알아서 1씩 증가하며 begin 은 시작 값, end 는 끝 값 
			 JSTL 문법으로 for 문 안에 switch 문을 사용한 것 , otherwise == default 값으로 생각하면 됨
			 JSTL 문법은 xml 이나 쿼리문에서는 c: 를 붙이지 않고 사용하지만, jsp 와 같이 프론트에서 보여지는 부분은 헤더를 작성한 후에 문법을 사용해야한다-->
			 
			 <c:forEach var="i" begin="${paging.startPage}" end="${paging.endPage}">
			 	<c:choose>
			 	<c:when test="${i ne curPage}" > <!-- ne == ! 현재 페이지가 아닐 경우 온 클릭하여 이동할 수 있도록 -->
		         	<li class="page-item"><a class="page-link" href="javascript:void(0)" onclick="fn_list(${i})">${i}</a></li>
				</c:when>
				<c:otherwise >
		         	<li class="page-item active"><a class="page-link" href="javascript:void(0)" style="cursor:default;">${i}</a></li><!--curPage-->
	         	</c:otherwise>
	         	</c:choose>
	         </c:forEach>
	         
   		 <c:if test = "${paging.nextBlockPage gt 0}">
         	<li class="page-item"><a class="page-link" href="javascript:void(0)" onclick="fn_list(${paging.nextBlockPage})">다음블럭</a></li>
		 </c:if>
		</c:if>
      </ul>
   </nav>
   
   <button type="button" id="btnWrite" class="btn btn-secondary mb-3">글쓰기</button>

</div>

<!-- 게시판 페이지에서 현재 페이지 위치를 저장해놓기 위한 폼 -->
<form name="bbsForm" id="bbsForm" method="post" >
	<input type="hidden" id="hiBbsSeq" name="hiBbsSeq" value="" /><!-- 한 화면에 여러개 보이기 때문에 세팅안함 -->
	<input type="hidden" id="searchType" name="searchType" value="${searchType}" />
	<input type="hidden" id="searchValue" name="searchValue" value="${searchValue}" />
	<input type="hidden" id="curPage" name="curPage" value="${curPage}" />
</form>

</body>
</html>