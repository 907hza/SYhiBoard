package com.sist.web.model;
import java.io.Serializable;

public class HiBoardFile implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	private long hiBbsSeq;      // 게시물 번호(시퀀스:SEQ_HIBOARD_SEQ)
	private short fileSeq;      // 파일번호(HIBBS_SEQ MAX+1)
	private String fileOrgName; // 원본파일명
	private String fileName;    // 파일명
	private String fileExt;     // 파일 확장자
	private long fileSize;    // 파일 크기
	private String regDate;     // 등록일

	public HiBoardFile()
	{
		hiBbsSeq = 0;
		fileSeq = 0;
		fileOrgName = "";
		fileName = "";
		fileExt = "";
		fileSize = 0;
		regDate = "";
	}

	public long getHiBbsSeq() {
		return hiBbsSeq;
	}

	public void setHiBbsSeq(long hiBbsSeq) {
		this.hiBbsSeq = hiBbsSeq;
	}

	public short getFileSeq() {
		return fileSeq;
	}

	public void setFileSeq(short fileSeq) {
		this.fileSeq = fileSeq;
	}

	public String getFileOrgName() {
		return fileOrgName;
	}

	public void setFileOrgName(String fileOrgName) {
		this.fileOrgName = fileOrgName;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileExt() {
		return fileExt;
	}

	public void setFileExt(String fileExt) {
		this.fileExt = fileExt;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	public String getRegDate() {
		return regDate;
	}

	public void setRegDate(String regDate) {
		this.regDate = regDate;
	}
}
