package com.oup.apiproxy.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Role {
	
	@JsonProperty("userName")
	private String userName;
	@JsonProperty("role")
	private String role;
	
	@JsonProperty("userName")
	public String getUserName() {
		return userName;
	}
	@JsonProperty("userName")
	public void setUserName(String userName) {
		this.userName = userName;
	}
	@JsonProperty("role")
	public String getRole() {
		return role;
	}
	@JsonProperty("role")
	public void setRole(String role) {
		this.role = role;
	}
	public Role(String userName, String role) {
		super();
		this.userName = userName;
		this.role = role;
	}
	public Role() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	

}
