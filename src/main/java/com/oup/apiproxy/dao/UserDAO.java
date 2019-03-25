package com.oup.apiproxy.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.oup.apiproxy.dto.Role;
import com.oup.apiproxy.dto.User;;

@Component("springManagedUserDAO")
public class UserDAO {

	@Autowired
	private DataSource dataSource;

	private JdbcTemplate jdbcTemplate;
	
	public String add(User user) throws Exception {

		jdbcTemplate = new JdbcTemplate(dataSource);

		int rowsAffected = jdbcTemplate.update("INSERT INTO users(`username`,`password`,`enabled`) VALUES(?,?,1)",
				user.getUserName(), new BCryptPasswordEncoder().encode(user.getPassword()));

		return rowsAffected + " users added successfully";

	}

	public String update(User user) throws Exception {

		jdbcTemplate = new JdbcTemplate(dataSource);

		int rowsAffected = jdbcTemplate.update("UPDATE users SET PASSWORD = ? WHERE USERNAME = ?",
				new BCryptPasswordEncoder().encode(user.getPassword()), user.getUserName());

		if (rowsAffected == 0)
			return "The specified user " + user.getUserName() + " do not exist";
		else
			return rowsAffected + " users updated successfully";

	}

	public String addRole(Role role) throws Exception {
		jdbcTemplate = new JdbcTemplate(dataSource);
		
		this.deleteRole(role);

		int rowsAffected = jdbcTemplate.update("INSERT INTO authorities(`username`,`authority`) VALUES(?,?)",
				role.getUserName(), role.getRole());

		return rowsAffected + " roles added successfully";
	}
	
	public String deleteRole(Role role) throws Exception {
		jdbcTemplate = new JdbcTemplate(dataSource);

		int rowsAffected = jdbcTemplate.update("DELETE FROM authorities WHERE username = ? AND authority = ?",
				role.getUserName(), role.getRole());

		return rowsAffected + " roles deleted successfully";
	}
	
	public HashMap<String,User> getAllUsers() throws Exception{
		HashMap<String,User> allUsers=new HashMap<>();
		jdbcTemplate = new JdbcTemplate(dataSource);
		for(Map row:jdbcTemplate.queryForList("SELECT * FROM users"))
		{
			User item=new User();
			item.setUserName((String)row.get("username"));
			item.setPassword("ENCRYPTED_PWD");
			allUsers.put(item.getUserName(), item);
		}
		return allUsers;
	}
	
	public ArrayList<Role> getRole(String userName) throws Exception{
		ArrayList<Role> allRoles=new ArrayList<>();
		jdbcTemplate = new JdbcTemplate(dataSource);
		for(Map row:jdbcTemplate.queryForList("SELECT * FROM authorities WHERE username = ?",userName))
		{
			Role item=new Role();
			item.setUserName(userName);
			item.setRole((String)row.get("authority"));
			allRoles.add(item);
		}
		return allRoles;
	}

}
