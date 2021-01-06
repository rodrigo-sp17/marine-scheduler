package com.github.rodrigo_sp17.mscheduler;

import com.github.rodrigo_sp17.mscheduler.user.data.CreateUserRequest;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.net.URI;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MarinerSchedulerApplicationTests {

	@Autowired
	private MockMvc mvc;

	@Test
	void contextLoads() {
	}

	@Test
	public void testAuthorization() throws Exception {
		mvc.perform(get(new URI("/api/user")))
				.andExpect(status().isForbidden());

		mvc.perform(get(new URI("/api/shifts")))
				.andExpect(status().isForbidden());

		var json = getUserRequest();

		mvc.perform(post(new URI("/api/user/signup"))
				.contentType(MediaType.APPLICATION_JSON)
				.content(json.toString()))
				.andExpect(status().isOk())
				.andReturn();

		var jsonLogin = new JSONObject();
		jsonLogin.put("username", "john@doe123")
				.put("password", "testPassword");


		var result = mvc.perform(post(new URI("/login"))
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonLogin.toString()))
				.andExpect(status().isOk())
				.andReturn();

		String token = result
				.getResponse()
				.getHeader("Authorization");

		mvc.perform(get(new URI("/api/user"))
				.header("Authorization", token))
				.andExpect(status().isOk());

		/*
		mvc.perform(get(new URI("/api/shift"))
				.header("Authorization", token))
				.andExpect(status().isOk());
		*/
	}

	private JSONObject getUserRequest() {
		CreateUserRequest user = new CreateUserRequest();
		user.setName("John Doe");
		user.setEmail("john_doe@gmail.com");
		user.setUsername("john@Doe123");
		user.setPassword("testPassword");
		user.setConfirmPassword("testPassword");

		JSONObject jsonRequest = new JSONObject();
		try {
			jsonRequest.put("username", user.getUsername());
			jsonRequest.put("name", user.getName());
			jsonRequest.put("password", user.getPassword());
			jsonRequest.put("confirmPassword", user.getPassword());
			jsonRequest.put("email", user.getEmail());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonRequest;
	}

}
