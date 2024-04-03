package com.sist.web.service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.sist.web.dao.UserDao;
import com.sist.web.model.User;

@Service("userService")
public class UserService 
{
	private static Logger logger = LoggerFactory.getLogger(UserService.class);
	
	@Autowired
	private UserDao userDao; // == new UserDao() 와 같기에 이 참조형 변수를 사용하면 된다
	
	// 사용자 조회
	public User userSelect(String userId)
	{
		User user = null;
		
		try
		{
			user = userDao.userSelect(userId);
		}
		catch(Exception e)
		{
			logger.error("[UserService] userSelect Exception", e );
		}
		
		return user;
		
	}
	
	// 사용자 등록
	public int userInsert(User user)
	{
		int count = 0 ;
		
		try
		{
			count = userDao.userInsert(user);
		}
		catch(Exception e)
		{
			logger.error("[UserService] userInsert SQLExcetion",e);
		}
		
		return count;
	}
	
	// 사용자 정보 수정
	public int userUpdate(User user)
	{
		int count = 0;
		
		try
		{
			count = userDao.userUpdate(user);
		}
		catch(Exception e)
		{
			logger.error("[UserService] userUpdate SQLException",e);
		}
		
		return count;
	}
	

}
