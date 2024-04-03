package com.sist.web.controller;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sist.common.util.StringUtil;
import com.sist.web.model.Response;
import com.sist.web.model.User;
import com.sist.web.service.UserService;
import com.sist.web.util.CookieUtil;
import com.sist.web.util.HttpUtil;
import com.sist.web.util.JsonUtil;


@Controller("userController") // 내부적으로 매핑 정보를 다 가지고 있기 때문에 이름이 중복되면 안된다
public class UserController 
{
	private static Logger logger = LoggerFactory.getLogger(UserController.class); 
	
	@Autowired 
	private UserService userService;
	
	@Value("#{env['auth.cookie.name']}") // 프로퍼티 값을 해당 변수에 저장해두는 것
	private String AUTH_COOKIE_NAME;
	
	// 로그인 메소드
	// response 객체를 보내면 JSON 통신으로 보내는 걸 인지하고 @ResponseBody 를 선언해줘야한다
	@RequestMapping(value="/user/login",method=RequestMethod.POST) // /user/login 이름으로 들어오면 이 메소드 실행해라(무조건 포스트방식으로 들어오는 애만
	@ResponseBody // 매핑 정보 먼저 받아야한다 순서 주의
	public Response<Object> login(HttpServletRequest request, HttpServletResponse response)
	{
		Response<Object> ajaxResponse = new Response<Object>();
		String userId = HttpUtil.get(request, "userId");
		String userPwd = HttpUtil.get(request, "userPwd");
		
		if(!StringUtil.isEmpty(userId) && !StringUtil.isEmpty(userPwd))
		{
			User user = userService.userSelect(userId);
			
			if(user != null) // DB 에 해당 아이디 존재
			{
				if(StringUtil.equals(user.getUserPwd(), userPwd))
				{
					if(StringUtil.equals(user.getStatus(), "Y"))
					{
						// env.xml  안에 쿠키 설정하는게 적혀있음
						CookieUtil.addCookie(response, "/", -1, AUTH_COOKIE_NAME, CookieUtil.stringToHex(userId));
						// CookieUtil.stringToHex(userId) == userId 를 16 진수로 변경
						ajaxResponse.setResponse(0, "success");
					}
					else
					{
						ajaxResponse.setResponse(505, "status N");
					}
				}
				else // 비밀번호 불일치
				{
					ajaxResponse.setResponse(-1, "Password mismatch"); // 불일치하면 -1 보내기로함
				}
			}
			else // 사용자 정보 없을 때-
			{
				ajaxResponse.setResponse(404, "Not found");
			}
		}
		else
		{
			// 파라미터 값 잘못됐을때 >> 400
			ajaxResponse.setResponse(400, "Bad Request"); // 로그 찍기 위함
		}
		
		if(logger.isDebugEnabled()) // 디버그가 실행 중이라면 실행해줘잉
		{
			logger.debug("[UserController] /user/login_response\n" 
					+ JsonUtil.toJsonPretty(ajaxResponse));
		}
		
		return ajaxResponse;
	}
	
	// 회원가입 화면 요청
	@RequestMapping(value="/user/regForm",method=RequestMethod.GET)
	public String regForm(HttpServletRequest request, HttpServletResponse response)
	{
		String cookieUserId = CookieUtil.getHexValue(request, AUTH_COOKIE_NAME);
		
		if(!StringUtil.isEmpty(cookieUserId))
		{
			CookieUtil.deleteCookie(request, response, "/",AUTH_COOKIE_NAME); // 쿠키 삭제
			return "redirect:/"; // 재접속
		}
		else
		{
			return "/user/regForm";
		}
	}
	
	
	// 아이디 중복 확인
	@RequestMapping(value="/user/idCheck", method=RequestMethod.POST)
	@ResponseBody
	public Response<Object>idCheck(HttpServletRequest request, HttpServletResponse response)
	{
		Response<Object>ajaxResponse = new Response<Object>();
		
		String userId = HttpUtil.get(request, "userId");
		
		if(!StringUtil.isEmpty(userId))
		{
			if(userService.userSelect(userId) == null)
			{
				// 사용 가능한 아이디
				ajaxResponse.setResponse(0, "success");
			}
			else
			{
				ajaxResponse.setResponse(100, "Deplicate id");
			}
		}
		else
		{
			ajaxResponse.setResponse(400, "Bad Request");
		}
		
		if(logger.isDebugEnabled())
		{
			logger.debug("[UserController] /user/idCheck response\n"
					+ JsonUtil.toJsonPretty(ajaxResponse));
		}
		
		return ajaxResponse;
	} 
	
	// 사용자 등록
	@RequestMapping(value="/user/regProc" , method=RequestMethod.POST)
	@ResponseBody
	public Response<Object> regProc(HttpServletRequest request, HttpServletResponse response)
	{
		Response<Object> ajaxResponse = new Response<Object>();
	
		String userId = HttpUtil.get(request, "userId");
		String userPwd = HttpUtil.get(request, "userPwd");
		String userName = HttpUtil.get(request, "userName");
		String userEmail = HttpUtil.get(request, "userEmail");
		
		if(!StringUtil.isEmpty(userId) && !StringUtil.isEmpty(userPwd) 
				&& !StringUtil.isEmpty(userName) && !StringUtil.isEmpty(userEmail))
		{
			// 중복 아이디 체크
			if(userService.userSelect(userId) == null)
			{
				User user = new User();
				user.setUserId(userId);
				user.setUserPwd(userPwd);
				user.setUserName(userName);
				user.setUserEmail(userEmail);
				user.setStatus("Y");
				
				if(userService.userInsert(user) > 0)
				{
					ajaxResponse.setResponse(0, "success");
				}
				else
				{
					ajaxResponse.setResponse(500,"internal server error");
				}
			}
			else
			{
				ajaxResponse.setResponse(100, "Deplicate id");
			}
		}
		else
		{
			ajaxResponse.setResponse(400, "Bad Request");
		}
		
		if(logger.isDebugEnabled())
		{
			logger.debug("[UserController] /user/regProc response\n"
					+ JsonUtil.toJsonPretty(ajaxResponse));
		}
		
		return ajaxResponse;
	}
	
	// 회원정보 수정 화면 // 클릭으로 실행되는 건 다 GET 방식
	@RequestMapping(value="/user/updateForm", method=RequestMethod.GET)
	public String updateForm(ModelMap model,
			HttpServletRequest request, HttpServletResponse response)
	{
		String cookieUserId = CookieUtil.getHexValue(request, AUTH_COOKIE_NAME);
		
		User user = userService.userSelect(cookieUserId);
		model.addAttribute("user" , user); // 첫번째 인수는 JSP 에서 사용할 이름
		
		return "/user/updateForm";
	}
	
	// 회원 정보 수정 ajax 통신용
	@RequestMapping(value="/user/updateProc", method=RequestMethod.POST)
	@ResponseBody
	public Response<Object>updateProc(HttpServletRequest request, HttpServletResponse response)
	{
		Response<Object> ajaxResponse = new Response<Object>();
		
		String userId = HttpUtil.get(request, "userId");
		String userPwd = HttpUtil.get(request, "userPwd");
		String userName = HttpUtil.get(request, "userName");
		String userEmail = HttpUtil.get(request, "userEmail");
		
		String cookieUserId = CookieUtil.getHexValue(request, AUTH_COOKIE_NAME);
		
		// 로그인이 되어있는지 확인
		if(!StringUtil.isEmpty(cookieUserId))
		{
			if(StringUtil.equals(userId, cookieUserId))
			{
				User user = userService.userSelect(userId);
				
				if(user != null)
				{
					if(!StringUtil.isEmpty(userPwd) && !StringUtil.isEmpty(userName) 
							&& !StringUtil.isEmpty(userEmail))
					{
						// 받아온 값을 아예 덮어씌워버리는 것
						// 새로 유저 객체를 선언할 수도 있지만, 굳이 그럴 필요없이 값을 덮어씌우면 된다 
						user.setUserPwd(userPwd);
						user.setUserName(userName);
						user.setUserEmail(userEmail);
						
						if(userService.userUpdate(user) > 0)
						{
							ajaxResponse.setResponse(0, "success");
						}
						else
						{
							ajaxResponse.setResponse(500, "Internal server error");
						}
					}
					else
					{
						// 파라미터 값이 올바르지 않을 경우 : 값 입력 부족
						ajaxResponse.setResponse(400, "Bad request");
					}
				}
				else
				{
					// 회원 정보 없는 경우 : 쿠키를 삭제해주고 오류 처리를 해준다
					CookieUtil.deleteCookie(request, response, "/", AUTH_COOKIE_NAME);
					ajaxResponse.setResponse(404, "Not found");
				}
			}
			else
			{
				CookieUtil.deleteCookie(request, response, "/", AUTH_COOKIE_NAME);
				ajaxResponse.setResponse(430, "id information is different");
			}
		}
		else
		{
			// 로그인이 안된 상태
			ajaxResponse.setResponse(410, "Login failed");
		}
		
		if(logger.isDebugEnabled())
		{
			logger.debug("[UserController] /user/updateProc response\n"
					+ JsonUtil.toJsonPretty(ajaxResponse));
		}
		
		return ajaxResponse;
	}
	
	// 로그아웃
	@RequestMapping(value="/user/loginOut", method=RequestMethod.GET)
	public String loginOut(HttpServletRequest request, HttpServletResponse response)
	{
		if(CookieUtil.getCookie(request, AUTH_COOKIE_NAME) != null)
		{
			// 쿠키에 저장되어있는지 확인하고 있으면 쿠키만 날려주면 됨
			CookieUtil.deleteCookie(request, response, "/", AUTH_COOKIE_NAME);
		}
		
		return "redirect:/"; // == 재접속
	}
	
}
