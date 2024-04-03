package com.sist.web.dao;
import org.springframework.stereotype.Repository;
import com.sist.web.model.User;

@Repository("userDao") // 원래 try -catch 믄으로 예외처리 해줬던 걸 rEPOSITORY 를 통해 구현해준 것
public interface UserDao 
{
	// 사용자 정보 조회 select 에 있는 아이디가 메소드명
	public User userSelect(String userId);
	
	// 사용자 등록
	public int userInsert(User user);
	
	// 사용자 수정
	public int userUpdate(User user);
}
