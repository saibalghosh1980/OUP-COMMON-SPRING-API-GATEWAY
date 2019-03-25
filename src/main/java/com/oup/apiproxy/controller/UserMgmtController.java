package com.oup.apiproxy.controller;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.oup.apiproxy.dao.OpMessageDAO;
import com.oup.apiproxy.dao.UserDAO;
import com.oup.apiproxy.dto.Role;
import com.oup.apiproxy.dto.User;

@RestController
@RequestMapping("/uam")
public class UserMgmtController {

	@Autowired
	@Qualifier("springManagedUserDAO")
	private UserDAO userDAO;

	@PostMapping(path = "/user")
	public ResponseEntity<?> add(@RequestBody User user) {

		String pattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";
		if (user.getPassword().matches(pattern)) {

			try {
				return new ResponseEntity<OpMessageDAO>(new OpMessageDAO(userDAO.add(user)), HttpStatus.CREATED);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				return new ResponseEntity<OpMessageDAO>(
						new OpMessageDAO("Problem processing request: " + ExceptionUtils.getFullStackTrace(e)),
						HttpStatus.INTERNAL_SERVER_ERROR);

			}
		} else {
			return new ResponseEntity<OpMessageDAO>(new OpMessageDAO(
					"The password is not strong enough. Ensure that it has a digit,one upper and one lowercase,one special charecter and at least 8 charecters long"),
					HttpStatus.UNPROCESSABLE_ENTITY);
		}

	}

	@PostMapping(path = "/role")
	public ResponseEntity<?> addRole(@RequestBody Role role) {

		try {
			return new ResponseEntity<OpMessageDAO>(new OpMessageDAO(userDAO.addRole(role)), HttpStatus.CREATED);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			return new ResponseEntity<OpMessageDAO>(
					new OpMessageDAO("Problem processing request: " + ExceptionUtils.getFullStackTrace(e)),
					HttpStatus.INTERNAL_SERVER_ERROR);

		}

	}
	
	@DeleteMapping(path = "/role")
	public ResponseEntity<?> deleteRole(@RequestBody Role role) {

		try {
			return new ResponseEntity<OpMessageDAO>(new OpMessageDAO(userDAO.deleteRole(role)), HttpStatus.CREATED);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			return new ResponseEntity<OpMessageDAO>(
					new OpMessageDAO("Problem processing request: " + ExceptionUtils.getFullStackTrace(e)),
					HttpStatus.INTERNAL_SERVER_ERROR);

		}

	}

	@PutMapping(path = "/user/{userId}")
	public ResponseEntity<?> update(@PathVariable String userId, @RequestBody User user) {
		user.setUserName(userId);
		String pattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";
		if (user.getPassword().matches(pattern)) {

			try {
				return new ResponseEntity<OpMessageDAO>(new OpMessageDAO(userDAO.update(user)), HttpStatus.OK);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				return new ResponseEntity<OpMessageDAO>(
						new OpMessageDAO("Problem processing request: " + ExceptionUtils.getFullStackTrace(e)),
						HttpStatus.INTERNAL_SERVER_ERROR);

			}
		} else {
			return new ResponseEntity<OpMessageDAO>(new OpMessageDAO(
					"The password is not strong enough. Ensure that it has a digit,one upper and one lowercase,one special charecter and at least 8 charecters long"),
					HttpStatus.UNPROCESSABLE_ENTITY);
		}

	}

}
