package com.sist.web.dao;
import java.util.List;
import org.springframework.stereotype.Repository;
import com.sist.web.model.HiBoard;
import com.sist.web.model.HiBoardFile;

@Repository("hiBoardDao")
public interface HiBoardDao 
{
	// 게시물 리스트
	public List<HiBoard> boardList(HiBoard hiBoard);
	
	// 게시물 등록
	public int boardInsert(HiBoard hiBoard);
	
	// 게시물 첨부파일 등록
	public int boardFileInsert(HiBoardFile hiBoardFile);
	
	// 게시물 첨부파일 조회
	public HiBoardFile boardFileSelect(long hiBbsSeq);
	
	// 총 게시물 갯수
	public long boardListCount(HiBoard hiBoard);
	
	// 게시물 상세보기
	public HiBoard boardSelect(long hiBbsSeq);
	
	// 게시물 조회수 증가
	public int boardReadCntPlus(long hiBbsSeq);
	
	// 게시물 수정
	public int boardUpdate(HiBoard hiBoard);
	
	// 첨부파일 삭제
	public int boardFileDelete(long hiBbsSeq);
	
	// 게시물 삭제
	public int boardDelete(long hiBbsSeq);
	
	// 게시물 삭제 시 답변 글 수 조회
	public int boardAnswersCount(long hiBbsSeq);
	
	// 답변 : 게시물 그룹 내 순번 수정
	public int boardGroupOrderUpdate(HiBoard hiBoard);
	
	// 게시물 답글 등록
	public int boardReplyInsert(HiBoard hiBoard);
}
