<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>

<%
	if(com.sist.web.util.CookieUtil.getCookie(request, (String)request.getAttribute("AUTH_COOKIE_NAME")) != null )
	{ // 로그인이 되어있으면 (쿠키에 저장되어있는) 보여줄 네비 
	  // 반환 값이 Object 일 때는 해당하는 값에 맞춰서 형변환해줘야한다
%>
<nav class="navbar navbar-expand-sm bg-secondary navbar-dark mb-3"> 
	<ul class="navbar-nav"> 
	    <li class="nav-item"> 
	      <a class="nav-link" href="/user/loginOut"> 로그아웃</a> 
	    </li> 
	    <li class="nav-item"> 
	      <a class="nav-link" href="/user/updateForm">회원 정보 수정</a> 
	    </li> 
	    <li class="nav-item"> 
	      <a class="nav-link" href="/board/list"> 게시판</a> 
	    </li> 
	</ul>
</nav>
<%
	}
	else // 로그인 안했을 때 보이는 상단 네비
	{
%>
<nav class="navbar navbar-expand-sm bg-secondary navbar-dark mb-3"> 
	<ul class="navbar-nav"> 
	    <li class="nav-item"> 
	      <a class="nav-link" href="/"> 로그인</a> 
	    </li> 
	    <li class="nav-item"> 
	      <a class="nav-link" href="/user/regForm">회원가입</a> 
	    </li> 
  </ul> 
</nav>
<%
	}
%>