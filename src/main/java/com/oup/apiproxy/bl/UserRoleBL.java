package com.oup.apiproxy.bl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.oup.apiproxy.dao.UserDAO;
import com.oup.apiproxy.dto.Role;
import com.oup.apiproxy.dto.User;

@Service("springManagedUserBL")
public class UserRoleBL {

	@Autowired
	@Qualifier("springManagedUserDAO")
	private UserDAO userDAO;

	public String addUserIfDoesNotExists(User user) throws Exception {
		if (!userDAO.getAllUsers().containsKey(user.getUserName())) {
			return userDAO.add(user);
		} else
			return "User already exists in the DB";

	}

	public String addRoleForUserIfDoesNotExists(Role role) throws Exception {
		if (userDAO.getRole(role.getUserName()).size() == 0)
			return userDAO.addRole(role);
		else
			return "There is an role already associated with this user";
	}

}
