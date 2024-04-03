package com.sist.web.controller;
import java.io.File;
import java.util.List;

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
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.sist.common.model.FileData;
import com.sist.common.util.FileUtil;
import com.sist.common.util.StringUtil;
import com.sist.web.model.HiBoard;
import com.sist.web.model.HiBoardFile;
import com.sist.web.model.Paging;
import com.sist.web.model.Response;
import com.sist.web.model.User;
import com.sist.web.service.HiBoardService;
import com.sist.web.service.UserService;
import com.sist.web.util.CookieUtil;
import com.sist.web.util.HttpUtil;


@Controller("hiBoardController")
public class HiBoardController 
{
	private static Logger logger = LoggerFactory.getLogger(HiBoardController.class);

	@Autowired
	private HiBoardService hiBoardService;
	
	@Autowired
	private UserService userService;
	
	@Value("#{env['auth.cookie.name']}")
	private String AUTH_COOKIE_NAME;
	
	@Value("#{env['upload.save.dir']}")
	private String UPLOAD_SAVE_DIR;
	
	private static final int LIST_COUNT = 5; // 한 페이지에서의 게시물 수
	private static final int PAGE_COUNT = 5; // 페이징 갯수
	
	// 게시판 리스트 화면
	@RequestMapping(value="/board/list")
	public String list(ModelMap modelMap, HttpServletRequest request, HttpServletResponse response)
	{
		// 조회항목 1:작성자, 2:제못, 3:내용
		String searchType = HttpUtil.get(request, "searchType","");
		// 조회값 input text
		String searchValue = HttpUtil.get(request, "searchValue","");
		// 현재 페이지
		long curPage = HttpUtil.get(request, "curPage", (long)1);
		
		// 게시물 리스트에 사용할 리스트 객체
		List<HiBoard> list = null;
		
		// 조회 객체 선언
		HiBoard hiBoard = new HiBoard();
		
		// 페이징 처리하기 위해 페이징 객체 선언
		Paging paging = null;
		// 총 게시물 수를 나타낼 변수
		long totalCount = 0;
		
		if(!StringUtil.isEmpty(searchType) && !StringUtil.isEmpty(searchValue))
		{
			hiBoard.setSearchType(searchType);
			hiBoard.setSearchValue(searchValue);
		}
		
		totalCount = hiBoardService.boardListCount(hiBoard);

		if(totalCount > 0)
		{                       // 요청 url== value 랑 동일한것인가
			paging = new Paging("/board/list", totalCount, LIST_COUNT, PAGE_COUNT, curPage, "curPage");
			
			// 페이징 객체에 세팅된 스타트 엔드 로우를 보드에 세팅 
			hiBoard.setStartRow(paging.getStartRow());
			hiBoard.setEndRow(paging.getEndRow());
			
			list = hiBoardService.boardList(hiBoard);
		}
		
		modelMap.addAttribute("list", list);
		
		// 폼에 저장하기 위해 조회항목과 조회값, 현재 페이지를 보내줘야한다잉
		modelMap.addAttribute("searchType", searchType);
		modelMap.addAttribute("searchValue", searchValue);
		modelMap.addAttribute("curPage", curPage);
		modelMap.addAttribute("paging", paging);
		
		return "/board/list"; // 해당 값이 null 이 아니라면 servlet-context 뷰리졸버로 가서 접두사, 접미사가 붙어서 해당 화면이 보여지게된다
	}
	
	// 게시판 등록 화면과 폼 전달 용도(리스트 버튼을 클릭할 시) : bbsForm 전달을 위한 컨트롤러 
	@RequestMapping(value="/board/writeForm") // 여기서 모델맵은 데이터 저장 용도
	public String writeForm(ModelMap modelMap, HttpServletRequest request, HttpServletResponse response)
	{
		// 쿠키 값
		String cookieUserId = CookieUtil.getHexValue(request, AUTH_COOKIE_NAME);
		
		// 조회항목
		String searchType = HttpUtil.get(request, "searchType", "");
		// 조회값
		String searchValue = HttpUtil.get(request, "searchValue" ,"");
		// 현재 페이지
		long curPage = HttpUtil.get(request, "curPage", (long)1);
		
		// 사용자 정보조회 : 글쓰기 화면에서 이메일이랑 이름등 사용자 정보를 뿌려주기 때문에 필요
		User user = userService.userSelect(cookieUserId);
		
		modelMap.addAttribute("user", user);
		modelMap.addAttribute("searchType", searchType);
		modelMap.addAttribute("searchValue", searchValue);
		modelMap.addAttribute("curPage", curPage);
		
		return "/board/writeForm";
	}
	
	// 게시물 등록 ajax
	@RequestMapping(value="/board/writeProc", method=RequestMethod.POST)
	@ResponseBody
	public Response<Object> writeProc
	(MultipartHttpServletRequest request, HttpServletResponse response)
	{ 
		// 파일 업로드를 위해서 MultipartHttpServletRequest 를 사용
		Response<Object> ajaxResponse = new Response<Object>();
		
		String cookieUserId = CookieUtil.getHexValue(request, AUTH_COOKIE_NAME);
		
		String hiBbsTitle = HttpUtil.get(request, "hiBbsTitle","");
		String hiBbsContent = HttpUtil.get(request, "hiBbsContent","");
		
		FileData fileData = HttpUtil.getFile(request, "hiBbsFile", UPLOAD_SAVE_DIR);
		// getFile 메소드에서 파일이름, 사이즈 등등  SEQ (DB 에서 자동적으로 지정해주는 컬럼 제외) 유틸을 통해서
		// 첨부파일이 디렉토리에는 저장되어있지만 테이블에는 저장되어 있지 않은 상태이다
		
		if(!StringUtil.isEmpty(hiBbsTitle) && !StringUtil.isEmpty(hiBbsContent))
		{
			HiBoard hiBoard = new HiBoard();
			
			hiBoard.setUserId(cookieUserId);
			hiBoard.setHiBbsTitle(hiBbsTitle);
			hiBoard.setHiBbsContent(hiBbsContent);
			// 메인글은 나머지는 생성자에서 설정한 초기값으로 기본 세팅한다
			
			// 그래서 첨부파일이 존재할 때만 파일 테이블 객체를 생성해서 해당 첨부파일의 기본 정보를 들어가준다
			if(fileData != null && fileData.getFileSize() > 0) // 첨부파일 존재하는지 확인 
			{
				HiBoardFile hiBoardFile = new HiBoardFile();
				
				hiBoardFile.setFileName(fileData.getFileName());
				hiBoardFile.setFileOrgName(fileData.getFileOrgName());
				hiBoardFile.setFileExt(fileData.getFileExt());
				hiBoardFile.setFileSize(fileData.getFileSize());
				
				hiBoard.setHiBoardFile(hiBoardFile);
				// 163 라인에 생성한 하이보드파일 변수가 가지고 있던 주소 값을 hiBoard.java 에 세팅해주고
				// 해당 객체 선언한 new 의 메모리는 사라지지 않지만, 해당 객체 선언했던 변수는 if 문 안에서 선언한 지역변수이기 때문에
				// 필요없는 메모리는 사라지게 된다 > 트랜잭션 처리 (필요없는 메모리는 사라질 수 있도록 만드는 것)
			}
			
			try // 서비스에서 에러던져서 호출한 컨트롤러에서 예외 처리해줘야한다
			{
				if(hiBoardService.boardInsert(hiBoard) > 0)
				{
					ajaxResponse.setResponse(0, "success");
				}
				else
				{
					ajaxResponse.setResponse(500, "Internal server error");
				}
			}
			catch(Exception e)
			{
				logger.error("[HiBoardController] writeProc Exception", e);
				ajaxResponse.setResponse(500, "Internal server error2");
			}
		}
		else
		{
			// 파라미터 값 오류
			ajaxResponse.setResponse(400, "Bad Request");
		}
		
		return ajaxResponse;
	}
	
	// 게시물 상세 페이지 뷰 (제목 클릭했을 때 화면 보여주는 용도)
	@RequestMapping(value="/board/view") // 로그인과 회원가입은 DB 에서 정보를 가져와서 보여주는 개념이 아니지만 글 상세 페이지는 가져와서 보여줘야하기 때문에 모델맵 필요
	public String view(ModelMap modelMap, HttpServletRequest request, HttpServletResponse response)
	{
		// 본인이 쓴 글이라면 수정 삭제 버튼을 보여주려고 쿠키 아이디와 글 작성한 아이디를 대조해보는 것
		String cookieUserId = CookieUtil.getHexValue(request, AUTH_COOKIE_NAME);
		long hiBbsSeq = HttpUtil.get(request, "hiBbsSeq", (long)0);
		
		// 조회항목과 조회값 > 리스트 버튼 눌렀을 시 
		String searchType = HttpUtil.get(request, "searchType","");
		String searchValue = HttpUtil.get(request, "searchValue","");
		long curPage = HttpUtil.get(request, "curPage", (long)1);
		String gubun = HttpUtil.get(request, "gubun","N");
		
		// 본인 글 여부에 대한 변수를 가지고 갈 것
		String boardMe = "N"; // 내 글일 때는 Y 아닐 때는 N
		
		HiBoard hiBoard = null;
		
		if(hiBbsSeq > 0) // 글 번호가 있을 때만 검색
		{
			hiBoard = hiBoardService.boardView(hiBbsSeq, gubun);
			
			 // 본인 글인지 확인
			if(hiBoard != null && StringUtil.equals(hiBoard.getUserId(), cookieUserId))
			{
				boardMe = "Y"; // 글이 존재하고 로그인한 아이디와 글 쓴 아이디가 동일할 때만 변수를 Y 로 세팅
			}
		}
		
		modelMap.addAttribute("boardMe", boardMe);
		modelMap.addAttribute("hiBbsSeq", hiBbsSeq); // hiBoard.hiBbsSeq 를 써도 상관없당
		modelMap.addAttribute("hiBoard", hiBoard);
		modelMap.addAttribute("searchType", searchType);
		modelMap.addAttribute("searchValue", searchValue);
		modelMap.addAttribute("curPage", curPage);
		
		return "/board/view";
	}
	
	// 첨부파일 다운로드
	@RequestMapping(value="/board/download")// modelAndView는 데이터와 실제 보여줘야하는 페이지를 같이 가지고 있다
	public ModelAndView download(HttpServletRequest request, HttpServletResponse response)
	{
		ModelAndView mAv = null;
		
		// view.jsp 에서 hiBbsSeq 값만 보내기 때문에 받아준다이
		long hiBbsSeq = HttpUtil.get(request, "hiBbsSeq", (long)0);
		
		if(hiBbsSeq > 0) // 값있
		{
			HiBoardFile hiBoardFile = hiBoardService.boardFileSelect(hiBbsSeq);
			
			if(hiBoardFile != null)
			{ // FileUtil.getFileSeparator() OS 에 따라서 디렉토리를 지정해준다
			  // hiBoardFile.getFileName() 은 UUID (유효한 값)이 아닌 '오늘의 날씨.txt' 이름으로 다운받을 수 있도록하는 것
			  // 운영체제에 따라서 \/ 를 지정해서 파일 경로를 지정하여서 사용자가 지정한 이름으로 저장될 수 있도록하는 자바에서 지원하는 파일 객체이다
				File file = new File(UPLOAD_SAVE_DIR 
						+ FileUtil.getFileSeparator() + hiBoardFile.getFileName()); 

				if(FileUtil.isFile(file)) // 파일 존재하냐
				{
					mAv = new ModelAndView();
					mAv.setViewName("fileDownloadView");
					// 응답할 view 를 설정하는 것 (servlet-context.xml 에 정의되어있다)
					mAv.addObject("file", file); // 데이터 저장
					mAv.addObject("fileName", hiBoardFile.getFileOrgName());
					
					return mAv;
				}
			}
		}
		
		return mAv;
	}
	
	// 게시글 수정화면
	@RequestMapping(value="/board/updateForm")
	public String updateForm(ModelMap modelMap, HttpServletRequest request, HttpServletResponse response)
	{
		// 쿠키값
		String cookieUserId = CookieUtil.getHexValue(request, AUTH_COOKIE_NAME);
		// 게시물 번호
		long hiBbsSeq = HttpUtil.get(request, "hiBbsSeq", (long)0);
		
		String searchType = HttpUtil.get(request, "searchType","");
		String searchValue = HttpUtil.get(request, "searchValue","");
		long curPage = HttpUtil.get(request, "curPage", (long)1);
		
		HiBoard hiBoard = null;
		
		if(hiBbsSeq > 0)
		{
			hiBoard = hiBoardService.boardViewUpdate(hiBbsSeq);
			
			if(hiBoard != null)
			{
				// 내 게시물이 맞는지 확인해줘야한다
				if(!StringUtil.equals(hiBoard.getUserId(), cookieUserId))
				{
					hiBoard = null; // 본인 글이 아닐 경우 수정 불가능하도록 처리
				}
			}
		}
		
		modelMap.addAttribute("searchType", searchType);
		modelMap.addAttribute("searchValue",searchValue);
		modelMap.addAttribute("curPage",curPage);
		modelMap.addAttribute("hiBoard", hiBoard);
			
		return "/board/updateForm";
	}
	
	// 게시물 수정하기
	@RequestMapping(value="/board/updateProc", method=RequestMethod.POST)
	@ResponseBody
	public Response<Object> updateProc(MultipartHttpServletRequest request, HttpServletResponse response)
	{
		Response<Object> ajaxResponse = new Response<Object>();
		
		String cookieUserId = CookieUtil.getHexValue(request, AUTH_COOKIE_NAME);
		
		// 글을 수정하기 위해서 보낸 값들을 보내준당
		long hiBbsSeq = HttpUtil.get(request, "hiBbsSeq", (long)0);
		String hiBbsTitle = HttpUtil.get(request, "hiBbsTitle","");
		String hiBbsContent = HttpUtil.get(request, "hiBbsContent","");
		// 게시글을 업로드하면서 저장된 첨부파일의 정보가 담겨져있는 클래스
		FileData fileData = HttpUtil.getFile(request, "hiBbsFile", UPLOAD_SAVE_DIR);
		
	    if(hiBbsSeq > 0 && !StringUtil.isEmpty(hiBbsTitle) && !StringUtil.isEmpty(hiBbsContent))
	    {
	    	HiBoard hiBoard = hiBoardService.boardSelect(hiBbsSeq);
	    	
	    	if(hiBoard != null)
	    	{
	    		if(StringUtil.equals(cookieUserId, hiBoard.getUserId()))
	    		{
	    			hiBoard.setHiBbsTitle(hiBbsTitle);
	    			hiBoard.setHiBbsContent(hiBbsContent);
	    			
	    			if(fileData != null && fileData.getFileSize() > 0)
	    			{
	    				HiBoardFile hiBoardFile = new HiBoardFile();
	    				
	    				hiBoardFile.setFileName(fileData.getFileName());
	    				hiBoardFile.setFileOrgName(fileData.getFileOrgName());
	    				hiBoardFile.setFileExt(fileData.getFileExt());
	    				hiBoardFile.setFileSize(fileData.getFileSize());
	    				
	    				hiBoard.setHiBoardFile(hiBoardFile);
	    			}
	    			
	    			try
	    			{
	    				if(hiBoardService.boardUpdate(hiBoard) > 0)
	    				{
	    					ajaxResponse.setResponse(0, "Success");
	    				}
	    				else
	    				{
	    					ajaxResponse.setResponse(500, "Internal server error try");
	    				}
	    			}
	    			catch(Exception e)
	    			{
	    				logger.error("[HiBoardController] updateProc Exception", e);
	    				ajaxResponse.setResponse(500, "Internal server error");
	    			}
	    			
	    		}
	    		else
	    		{
	    			ajaxResponse.setResponse(403, "Server error");
	    		}
	    	}
	    	else
	    	{
	    		ajaxResponse.setResponse(404, "Not Found");
	    	}
	    }
	    else
	    {
	    	ajaxResponse.setResponse(400, "parameter Exception");
	    }
		
		return ajaxResponse;
	}
	
	// 게시물 삭제
	@RequestMapping(value="/board/delete", method=RequestMethod.POST)
	@ResponseBody
	public Response<Object> delete(HttpServletRequest request, HttpServletResponse response)
	{
		Response<Object> ajaxResponse = new Response<Object>();
		
		String cookieUserId = CookieUtil.getHexValue(request, AUTH_COOKIE_NAME);
		long hiBbsSeq = HttpUtil.get(request, "hiBbsSeq", (long)0);
		
		if(hiBbsSeq > 0)
		{
			HiBoard hiBoard = hiBoardService.boardSelect(hiBbsSeq);
			
			if(hiBoard != null)
			{
				if(StringUtil.equals(cookieUserId, hiBoard.getUserId()))
				{
					try
					{
						if(hiBoardService.boardAnswersCount(hiBbsSeq) > 0)
						{
							ajaxResponse.setResponse(-999, "answers exist and cannot be delete");
						}
						else // 지우려는 해당 게시물이 HIBBS_PARENTS 에 속해있다면 지울 수 없도록
						{
							if(hiBoardService.boardDelete(hiBbsSeq) > 0)
							{
								ajaxResponse.setResponse(0, "Success");
							}
							else
							{
								ajaxResponse.setResponse(500, "Server error try");
							}
						}
					}
					catch(Exception e)
					{
						logger.error("[HiBoardController] delete Exception", e);
						ajaxResponse.setResponse(500, "Server error catch");
					}
				}
				else
				{
					ajaxResponse.setResponse(403, "Server error");
				}
			}
			else
			{
				ajaxResponse.setResponse(404, "Not Found");
			}
		}
		else
		{
			ajaxResponse.setResponse(400, "Parameter Exception");
		}
		
		return ajaxResponse;
	}
	
	// 첨부파일만 삭제
	@RequestMapping(value="/board/fileDelete")
	@ResponseBody
	public Response<Object> fileDelete(MultipartHttpServletRequest request, HttpServletResponse response)
	{
		Response<Object> ajaxResponse = new Response<Object>();
		
		long hiBbsSeq = HttpUtil.get(request, "hiBbsSeq", (long)0);
		String cookieUserId = CookieUtil.getHexValue(request, AUTH_COOKIE_NAME);
		FileData fileData = HttpUtil.getFile(request, "hiBbsFile", UPLOAD_SAVE_DIR);

		if(hiBbsSeq > 0)
		{
			HiBoard hiBoard = hiBoardService.boardSelect(hiBbsSeq);
			
			if(hiBoard != null)
			{
				HiBoardFile hiBoardFile = hiBoard.getHiBoardFile();
				
				if(hiBoardFile != null)
				{
					if(hiBoardService.boardFileDelete(hiBbsSeq) > 0)
					{
						
					}
					else
					{
						ajaxResponse.setResponse(500, "server error");
					}
				}
				else
				{
					ajaxResponse.setResponse(404, "not found");
				}
			}
			else
			{
				ajaxResponse.setResponse(404, "not found");
			}
		}
		else
		{
			ajaxResponse.setResponse(403, "parameter Exception");
		}
		
		return ajaxResponse;
	}
	
	// 댓글 페이지 화면
	@RequestMapping(value="/board/replyForm", method=RequestMethod.POST)
	public String replyForm(ModelMap modelMap, HttpServletRequest request, HttpServletResponse response)
	{
		String cookieUserId = CookieUtil.getHexValue(request, AUTH_COOKIE_NAME);
		// 시퀀스 번호로 그룹과 부모번호를 등록해줘야하기 때문에 갖고와야한다잉
		long hiBbsSeq = HttpUtil.get(request, "hiBbsSeq", (long)0);
		
		// 리스트 버튼이 존재하기 때문에 받아준다
		String searchType = HttpUtil.get(request, "searchType");
		String searchValue = HttpUtil.get(request, "searchValue");
		long curPage = HttpUtil.get(request, "curPage", (long)1);
		
		HiBoard hiBoard = null;
		User user = null; // 이름, 이메일 
		
		if(hiBbsSeq > 0)
		{
			// 보드로 가져온 정보는 댓글 작성자가 아닌 메인 글 작성자이기 때문에 유저 정보도 따로 가져와줘야한다
			hiBoard = hiBoardService.boardSelect(hiBbsSeq);
		
			if(hiBoard != null)
			{
				logger.debug("=====================================");
				logger.debug(" /board/replyForm hiBbsSeq : "+ hiBoard.getHiBbsSeq());
				logger.debug("=====================================");
				
				user = userService.userSelect(cookieUserId);
			}
		}

		modelMap.addAttribute("hiBoard",hiBoard);
		modelMap.addAttribute("user",user);
		modelMap.addAttribute("searchType",searchType);
		modelMap.addAttribute("searchValue",searchValue);
		modelMap.addAttribute("curPage",curPage);
		
		return "/board/replyForm";
	}

	// 게시물 답변 쓰고 답변 버튼 눌렀을 때 
	@RequestMapping(value="/board/replyProc", method=RequestMethod.POST)
	@ResponseBody
	public Response<Object> replyProc(MultipartHttpServletRequest request, HttpServletResponse response)
	{
		Response<Object> ajaxResponse = new Response<Object>();
		
		String cookieUserId = CookieUtil.getHexValue(request, AUTH_COOKIE_NAME);
		long hiBbsSeq = HttpUtil.get(request, "hiBbsSeq", (long)0); // 부모글 시퀀스 번호 
		
		String hiBbsTitle = HttpUtil.get(request, "hiBbsTitle","");
		String hiBbsContent = HttpUtil.get(request, "hiBbsContent","");
		FileData fileData = HttpUtil.getFile(request, "hiBbsFile", UPLOAD_SAVE_DIR);
		
		if(hiBbsSeq > 0 && !StringUtil.isEmpty(hiBbsTitle) && !StringUtil.isEmpty(hiBbsContent))
		{
			HiBoard pHiBoard = hiBoardService.boardSelect(hiBbsSeq); // 부모글
			
			if(pHiBoard != null)
			{
				HiBoard cHiBoard = new HiBoard(); // 답변
				
				// 테이블에 해당하는 값을 세팅해서 넣어준당
				cHiBoard.setUserId(cookieUserId);
				cHiBoard.setHiBbsTitle(hiBbsTitle);
				cHiBoard.setHiBbsContent(hiBbsContent);
				
				// 메인글과 메인글의 답변들은 한 그룹으로 간주한다
				cHiBoard.setHiBbsGroup(pHiBoard.getHiBbsGroup());
				// 그룹 내에 순서이기 때문에 부모 순번 다음 번호호 지정하기 위해 +1
				cHiBoard.setHiBbsOrder(pHiBoard.getHiBbsOrder() + 1);
				// CSS 에서 들여쓰기를 할 용도이기 때문에 
				// 댓글은 메인글보다 한번 뒤로 들어가져있어서 부모 순번 다음지정하기 위해 +1
				cHiBoard.setHiBbsIndent(pHiBoard.getHiBbsIndent() + 1);
				cHiBoard.setHiBbsParent(hiBbsSeq);
				
				if(fileData != null && fileData.getFileSize() > 0)
				{
					HiBoardFile hiBoardFile = new HiBoardFile();
					
					hiBoardFile.setFileName(fileData.getFileName());
					hiBoardFile.setFileOrgName(fileData.getFileOrgName());
					hiBoardFile.setFileExt(fileData.getFileExt());
					hiBoardFile.setFileSize(fileData.getFileSize());
					
					cHiBoard.setHiBoardFile(hiBoardFile);
				}
				
				// 트랜잭션 처리
				try
				{
					if(hiBoardService.boardReplyInsert(cHiBoard) > 0)
					{
						ajaxResponse.setResponse(0, "success");
					}
					else
					{
						ajaxResponse.setResponse(500, "Internal try server error");
					}
				}
				catch(Exception e)
				{
					logger.error("[HiBoardController] replyProc catch Exception", e);
					ajaxResponse.setResponse(500, "Internal catch server error");
				}
			}
			else
			{
				// 메인글 (부모글) 이 없는 경우
				ajaxResponse.setResponse(404, "Not found");
			}
		}
		else
		{
			ajaxResponse.setResponse(400, "parameter Exception");
		}
		
		return ajaxResponse;
	}
}

