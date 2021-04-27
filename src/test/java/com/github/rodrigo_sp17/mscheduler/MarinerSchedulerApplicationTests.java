package com.github.rodrigo_sp17.mscheduler;

import com.github.rodrigo_sp17.mscheduler.shift.data.ShiftRequest;
import com.github.rodrigo_sp17.mscheduler.user.data.CreateUserRequest;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MarinerSchedulerApplicationTests {

	// TODO - decouple tests

	@Autowired
	private JacksonTester<ShiftRequest> shiftRequestJson;

	@Autowired
	private MockMvc mvc;

	@MockBean
	private JavaMailSender mailSender;

	@MockBean
	private ClientRegistrationRepository clientRegistrationRepository;

	@Test
	void contextLoads() {
	}

	@Test
	@Order(1)
	// Logs in John Doe
	public void testAuthorization() throws Exception {
		mvc.perform(get(new URI("/api/user")))
				.andExpect(status().isForbidden());

		mvc.perform(get(new URI("/api/shifts")))
				.andExpect(status().isForbidden());

		var json = getUserRequest();

		mvc.perform(post(new URI("/api/user/signup"))
				.contentType(MediaType.APPLICATION_JSON)
				.content(json.toString()))
				.andExpect(status().isCreated())
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

	}

	@Test
	@Order(2)
	@WithMockUser("john@doe123")
	public void testAddShifts() throws Exception {
		ShiftRequest request = TestData.getShiftRequest();

		mvc.perform(get("/api/shift"))
				.andExpect(status().isOk())
				.andExpect(content().string(not(containsString("2020"))));


		mvc.perform(get("/api/shift/1"))
				.andExpect(status().isNotFound());

		mvc.perform(post(new URI("/api/shift/add"))
				.contentType(MediaType.APPLICATION_JSON)
				.content(shiftRequestJson.write(request).getJson()))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("2020-11-01")))
				.andExpect(content().string(containsString("2021-01-19")));

		mvc.perform(get("/api/shift"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("2020-11-01")))
				.andExpect(content().string(containsString("2021-01-19")));

		request.setShiftId(2L);
		request.setLeavingDate(LocalDate.of(2020, 11, 29));
		mvc.perform(put(new URI("/api/shift/edit"))
				.content(shiftRequestJson.write(request).getJson())
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("2020-11-29")))
				.andExpect(content().string(not(containsString("2020-11-28"))));

		mvc.perform(delete(new URI("/api/shift/remove"))
				.param("id", "4"))
				.andExpect(status().isNotFound());

		mvc.perform(delete(new URI("/api/shift/remove"))
				.param("id", "3"))
				.andExpect(status().isNoContent());

		mvc.perform(get("/api/shift"))
				.andExpect(status().isOk())
				.andExpect(content().string(not(containsString("2021-01-19"))));
	}

	@Test
	@Order(3)
	@WithMockUser("jane_girl18")
	public void testFriendshipPath() throws Exception {

		// Checks for empty friends and friend requests
		mvc.perform(get(new URI("/api/friend")))
				.andExpect(status().isOk())
				.andExpect(content().string(not(containsString("John Doe"))));

		mvc.perform(get(new URI("/api/friend/request")))
				.andExpect(status().isOk())
				.andExpect(content().string(not(containsString("John Doe"))));

		// Sends a friend request
		mvc.perform(post(new URI("/api/friend/request"))
				.param("username", "john@doe123"))
				.andExpect(status().isCreated())
				.andExpect(content().string(containsString("John Doe")))
				.andExpect(content().string(containsString("Jane Doe")))
				.andExpect(content().string(containsString("2021")));

		mvc.perform(post(new URI("/api/friend/request"))
				.param("username", "joaozinn"))
				.andExpect(status().isCreated())
				.andExpect(content().string(containsString("Joao Silva")))
				.andExpect(content().string(containsString("joaozinn")))
				.andExpect(content().string(containsString("2021")));

		// Checks if a new attempt returns the same request
		mvc.perform(get(new URI("/api/friend/request")))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("John Doe")))
				.andExpect(content().string(containsString("Jane Doe")))
				.andExpect(content().string(containsString("2021")));

		// Checks request for another user and accepts
		mvc.perform(get(new URI("/api/friend/request"))
				.with(user("john@doe123").password("").roles("")))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Jane Doe")));

		mvc.perform(post(new URI("/api/friend/accept"))
				.param("username", "jane_girl18")
				.with(user("john@doe123").password("").roles("")))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Jane Doe")))
				.andExpect(content().string(containsString("jane_girl18")))
				.andExpect(content().string(containsString("jane_doe@gmail.com")));

		mvc.perform(post(new URI("/api/friend/accept"))
				.param("username", "jane_girl18")
				.with(user("joaozinn").password("").roles("")))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Jane Doe")))
				.andExpect(content().string(containsString("jane_girl18")))
				.andExpect(content().string(containsString("jane_doe@gmail.com")));

		mvc.perform(get(new URI("/api/friend"))
				.with(user("john@doe123").password("").roles("")))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Jane Doe")));

		mvc.perform(get(new URI("/api/friend/request"))
				.with(user("john@doe123").password("").roles("")))
				.andExpect(status().isOk())
				.andExpect(content().string(not(containsString("Jane Doe"))));

		// Checks if its added for the other user too
		mvc.perform(get(new URI("/api/friend"))
				).andExpect(status().isOk())
				.andExpect(content().string(containsString("John Doe")));

		mvc.perform(get(new URI("/api/friend/request"))
				).andExpect(status().isOk())
				.andExpect(content().string(not(containsString("John Doe"))));

		// Test remove friend
		mvc.perform(delete(new URI("/api/friend/remove"))
				.param("username", "joaozinn"))
				.andExpect(status().isNoContent());

		mvc.perform(get(new URI("/api/friend")))
				.andExpect(status().isOk())
				.andExpect(content().string(not(containsString("Joao Silva"))));

		mvc.perform(get(new URI("/api/friend"))
				.with(user("joaozinn").password("").roles("")))
				.andExpect(status().isOk())
				.andExpect(content().string(not(containsString("Jane Doe"))));

	}

	@Test
	@Order(4)
	@WithMockUser("jane_girl18")
	public void testCalendarFeature() throws Exception {
		mvc.perform(get(new URI("/api/calendar/available"))
				.param("date", "2020-11-15"))
				.andExpect(status().isOk())
				.andExpect(content().string(not(containsString("John Doe"))));
		mvc.perform(get(new URI("/api/calendar/available"))
				.param("date", "2020-05-01"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("John Doe")));
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
