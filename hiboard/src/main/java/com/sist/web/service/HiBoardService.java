package com.sist.web.service;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.sist.common.util.FileUtil;
import com.sist.common.util.StringUtil;
import com.sist.web.dao.HiBoardDao;
import com.sist.web.model.HiBoard;
import com.sist.web.model.HiBoardFile;
import com.sist.web.util.HttpUtil;

@Service("hiBoardService")
public class HiBoardService 
{
	private static Logger logger = LoggerFactory.getLogger(HiBoardService.class);
	
	// 파일 저장 경로
	@Value("#{env['upload.save.dir']}")
	private String UPLOAD_SAVE_DIR;
	
	@Autowired
	private HiBoardDao hiBoardDao;
	
	// 게시물 리스트
	public List<HiBoard> boardList(HiBoard hiBoard)
	{
		List<HiBoard> list = null;
		
		try
		{
			list = hiBoardDao.boardList(hiBoard);
		}
		catch(Exception e)
		{
			logger.error("[HiBoardService] boardList SQLException", e);
		}
		
		return list;
	}
	
	// 게시물 등록
	// 트랜잭션은 요청한 응답을 하나로 묶어서 하나라도 오류가 나면 전체 롤백을 하고 아니면 커밋을 해야하는데 그게 트랜잭션 처리이다
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Exception.class) // 트랜잭션 처히랗 때 사용하는 어노테이션
	public int boardInsert(HiBoard hiBoard) throws Exception // 나를 호출하는 컨트롤러에서 터리
	{ // Propagation.REQUIRED 가 호출되기 전에 트랜잭션이 있으면 그 트랜잭션에서 실행이 되고
	  // 없으면 현재 상태에서 새로운 트랜잭션을 실행 (기본 설정)
	  // rollbackFor 은 트랜잭션 처리 중 오류가 났을 때 롤백하고 싶으면 작성하는 것
		int count = 0;

			count = hiBoardDao.boardInsert(hiBoard);

			// 하이보드파일 테이블에 입력할 게 있는지 확인
			if(count > 0 && hiBoard.getHiBoardFile() != null)
			{
				HiBoardFile hiBoardFile = hiBoard.getHiBoardFile();
				
				hiBoardFile.setHiBbsSeq(hiBoard.getHiBbsSeq());
				hiBoardFile.setFileSeq(hiBoard.getHiBoardFile().getFileSeq());
				
				hiBoardDao.boardFileInsert(hiBoardFile);
				
				// 위 로직과 동일한 결과 다른 방법
//				hiBoard.getHiBoardFile().setHiBbsSeq(hiBoard.getHiBbsSeq());
//				hiBoard.getHiBoardFile().setFileSeq((short)1);
//				hiBoardDao.boardFileInsert(hiBoard.getHiBoardFile());
			}

		
		return count;
	}
	
	// 총 게시물 수 
	public long boardListCount(HiBoard hiBoard)
	{
		long totalCount = 0;
		
		try
		{
			totalCount = hiBoardDao.boardListCount(hiBoard);
		}
		catch(Exception e)
		{
			logger.error("[HiBoardService] boardListCount SQLException", e);
		}
		
		return totalCount;
	}
	
	// 게시물 상세보기 // 게시물 존재 여부 확인 용도 
	public HiBoard boardSelect(long hiBbsSeq)
	{
		HiBoard hiBoard = null;
		
		try
		{
			hiBoard = hiBoardDao.boardSelect(hiBbsSeq);
		}
		catch(Exception e)
		{
			logger.error("[HiBoardService] boardSelect Exception", e);
		}
		return hiBoard;
	}
	
	// 찐 게시물 보기 (조회 수 증가 , 첨부파일 포함)
	public HiBoard boardView(long hiBbsSeq, String gubun)
	{
		HiBoard hiBoard = null;
		
		try
		{
			// select 먼저 해주고 update 해줘야한다
			// 게시물이 있는지 먼저 확인해준 뒤 게시물이 존재한다면 조회 수를 증가해준다
			hiBoard = hiBoardDao.boardSelect(hiBbsSeq);
			
			if(hiBoard != null)
			{
				// 조회 수 증가 > 처리 건 수 받아서 뭐 해줄 게 아니니까 Dao 만 호출해서 사용함
				if(StringUtil.equals(gubun, "N"))
				{
					hiBoardDao.boardReadCntPlus(hiBbsSeq);
				}
				
				// 첨부파일 추가
				HiBoardFile hiBoardFile = hiBoardDao.boardFileSelect(hiBbsSeq);
				
				if(hiBoardFile != null)
				{
					hiBoard.setHiBoardFile(hiBoardFile);
				}
			}
		}
		catch(Exception e)
		{
			logger.error("[HiBoardService] boardView Exception", e);
		}
		return hiBoard;
	}
	
	// 첨부파일 조회
	public HiBoardFile boardFileSelect(long hiBbsSeq)
	{
		HiBoardFile hiBoardFile = null;
		
		try
		{
			hiBoardFile = hiBoardDao.boardFileSelect(hiBbsSeq);
		}
		catch(Exception e)
		{
			logger.error("[HiBoardService] boardFileSelect Exception", e);
		}
		return hiBoardFile;
	}
	
	// 게시물 수정 폼 조회 (첨부파일 포함)
	public HiBoard boardViewUpdate(long hiBbsSeq)
	{
		HiBoard hiBoard = null;
		
		try
		{
			hiBoard = hiBoardDao.boardSelect(hiBbsSeq);
			
			if(hiBoard != null)
			{
				HiBoardFile hiBoardFile = hiBoardDao.boardFileSelect(hiBbsSeq);
				
				if(hiBoardFile != null)
					hiBoard.setHiBoardFile(hiBoardFile);
			}
		}
		catch(Exception e)
		{
			logger.error("[HiBoardService] boardViewUpdate Exception", e);
		}
		
		return hiBoard;
	}
	
	// 게시물 수정이 잘 되었는지 확인
	// 하이보드파일 테이블에는 UUID 값이 들어가있음, 파일은 글을 올리면 업로드 파일에 올라가기 때문에 기존의 파일을 지워주고 다시 업데이트 해줄 것이다
	// 업데이트를 먼저하고 첨부파일을 삭제해줄 것이기 때문에 기능이 두개니까 트랜잭션 처리해준다
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Exception.class)
	public int boardUpdate(HiBoard hiBoard) throws Exception
	{
		int count = 0;
		
		count = hiBoardDao.boardUpdate(hiBoard);
		
		// 여기서 하이보드파일은 기존에 존재했던 게 아니라 수정하면서 새로 들어온 친구
		if(count > 0 && hiBoard.getHiBoardFile() != null)
		{
			// 지존에 첨부파일이 존재하는지 확인하고 첨부파일이 존재하면 삭제할 수 있도록 먼저 조회를 해준다잉 (여기있는 파일은 기존 파일)
			// 신규 파일을 등록해주기 위해서 기존에 있던 파일을 지워주는 것이다
			HiBoardFile hiBoardFile = hiBoardDao.boardFileSelect(hiBoard.getHiBbsSeq());

			if(hiBoardFile != null)
			{
				// 풀 경로와 해당 파일을 보내줘야 삭제 가능
				FileUtil.deleteFile(UPLOAD_SAVE_DIR 
						+ FileUtil.getFileSeparator() + hiBoardFile.getFileName());
				
				hiBoardDao.boardFileDelete(hiBoard.getHiBbsSeq());
			}
			
			HiBoardFile hiBoardFile2 = hiBoard.getHiBoardFile();
			hiBoardFile2.setHiBbsSeq(hiBoard.getHiBbsSeq());
			hiBoardFile2.setFileSeq(hiBoard.getHiBoardFile().getFileSeq());
			
			// 위 세줄을 두 줄로 만들어보시오 !
			// hiBoard.getHiBoardFile().setHiBbsSeq(hiBoard.getHiBbsSeq());
			// hiBoard.getHiBoardFile().setFileSeq(hiBoard.getHiBoardFile().getFileSeq());
			
			//컴트롤러에서 요청받으면서 파일이 업데이트된다
			hiBoardDao.boardFileInsert(hiBoard.getHiBoardFile());
		}
		
		return count;
	}
	
	// 게시물 삭제 , 첨부파일이 존재하면 함께 삭제
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Exception.class)
	public int boardDelete(long hiBbsSeq) throws Exception
	{
		int count = 0;
		
		// 위위 에 첨부파일을 포함한 게시글을 조회하는 메소드를 이용한다
		HiBoard hiBoard = boardViewUpdate(hiBbsSeq);
		
		if(hiBoard != null)
		{
			count = hiBoardDao.boardDelete(hiBbsSeq);
			
			if(count > 0)
			{
				HiBoardFile hiBoardFile = hiBoard.getHiBoardFile();
				
				if(hiBoardFile != null)
				{
					if(hiBoardDao.boardFileDelete(hiBbsSeq) > 0)
					{
						// 파일 삭제를 도와주는 공통모듈
						FileUtil.deleteFile(UPLOAD_SAVE_DIR 
								+ FileUtil.getFileSeparator() + hiBoardFile.getFileName());
					
					}
				}
			}
		}
		return count;
	}
	
	// 첨부파일만 삭제할 수 있도록 
	public int boardFileDelete(long hiBbsSeq)
	{
		int count = 0;
		
		try
		{
			count = hiBoardDao.boardFileDelete(hiBbsSeq);
		}
		catch(Exception e)
		{
			logger.error("[HiBoardService] boardFileDelete Exception", e);
		}
		
		return count;
	}
	
	// 게시물 삭제를 위한 답변 글 존재 조회
	public int boardAnswersCount(long hiBbsSeq)
	{
		int count = 0;
		
		try
		{
			count = hiBoardDao.boardAnswersCount(hiBbsSeq);
		}
		catch(Exception e)
		{
			logger.error("[HiBoardService] boardAnswersCount Exception", e);
		}
		return count;
	}
	
	// 게시물 답변 등록
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Exception.class)
	public int boardReplyInsert(HiBoard hiBoard) throws Exception
	{
		int count = 0;
		
		// 그룹 내에 오더 순서를 변경하지 않아도 되는 경우가 생길 수도 있음 >> 카운트에 저장안함요
		hiBoardDao.boardGroupOrderUpdate(hiBoard); // 첫번째 댓글일 경우
		
		count = hiBoardDao.boardReplyInsert(hiBoard);
		
		// 게시물 답글 정상 등록되고 나면 첨부파일 존재시 첨부파일도 등록하도록 합니당
		if(count > 0 && hiBoard.getHiBoardFile() != null)
		{
			// 컨트롤러에서 값 세팅한 hiBoardFile 과 같은 주소를 바라봐용
			HiBoardFile hiBoardFile = hiBoard.getHiBoardFile(); // 같은 집을 바라봐
			
			// 컨트롤러에서 pk 에 해당하는 fileSeq 와 hiBbsSeq 를 세팅하지 않았기 때문에 여기서 세팅해준다이
			hiBoardFile.setHiBbsSeq(hiBoard.getHiBbsSeq());
			hiBoardFile.setFileSeq(hiBoard.getHiBoardFile().getFileSeq());
			
			hiBoardDao.boardFileInsert(hiBoardFile); // == hiBoard.getHiBoardFile()
		}
		
		return count;
	}
}
