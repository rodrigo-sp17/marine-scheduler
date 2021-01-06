package com.github.rodrigo_sp17.mscheduler.controller;

import com.github.rodrigo_sp17.mscheduler.security.UserDetailsServiceImpl;
import com.github.rodrigo_sp17.mscheduler.shift.ShiftController;
import com.github.rodrigo_sp17.mscheduler.shift.ShiftService;
import com.github.rodrigo_sp17.mscheduler.shift.data.Shift;
import com.github.rodrigo_sp17.mscheduler.shift.data.ShiftRequest;
import com.github.rodrigo_sp17.mscheduler.user.UserService;
import com.github.rodrigo_sp17.mscheduler.user.data.AppUser;
import com.github.rodrigo_sp17.mscheduler.user.data.UserInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = ShiftController.class)
@ExtendWith(MockitoExtension.class) // This annotation enables mockito on JUnit5
@AutoConfigureJsonTesters
public class ShiftControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ShiftService shiftService;

    @MockBean
    private UserService userService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JacksonTester<ShiftRequest> shiftRequestJson;

    @Captor
    ArgumentCaptor<List<Shift>> acList;

    @Captor
    ArgumentCaptor<Shift> acShift;

    @Captor
    ArgumentCaptor<Long> acLong;


    @Test
    @WithMockUser(username = "john@doe123")
    public void testGetShiftById() throws Exception {
        var shift = getShifts().get(0);
        shift.setOwner(getUsers().get(0));

        when(shiftService.getShiftById(1L, shift.getOwner()
                .getUserInfo().getUsername())).thenReturn(shift);

        mvc.perform(get(new URI("/api/shift/1")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("2020")));

        verify(shiftService, atLeast(1)).getShiftById(any(),any());
    }

    @Test
    public void testGetShifts() {
        // TODO
    }

    @Test
    @WithMockUser(username = "john@doe123")
    public void testAddShifts() throws Exception {
        var req = getShiftRequest();
        when(shiftService.addShifts(any(), eq("john@doe123"))).thenReturn(getShifts());

        mvc.perform(post(new URI("/api/shift/add"))
                .content(shiftRequestJson.write(req).getJson())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(shiftService).addShifts(acList.capture(), any());
        var result = acList.getValue();
        assertEquals(2, result.size());
        assertEquals(LocalDate.of(2021, 01, 19),
                result.get(1).getLeavingDate());
    }

    @Test
    @WithMockUser(username = "john@doe123")
    public void testEditShift() throws Exception {
        var req = getShiftRequest();
        req.setShiftId(null);

        var shift = getShifts().get(0);
        shift.setOwner(getUsers().get(0));

        when(shiftService.getShiftById(eq(1L), eq("john@doe123"))).thenReturn(shift);

        mvc.perform(put(new URI("/api/shift/edit"))
                .content(shiftRequestJson.write(req).getJson())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        req.setShiftId(1L);
        req.setUnavailabilityStartDate(req.getUnavailabilityStartDate().plusDays(3));

        mvc.perform(put(new URI("/api/shift/edit"))
                .content(shiftRequestJson.write(req).getJson())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        req.setUnavailabilityStartDate(req.getUnavailabilityStartDate().minusDays(3));
        LocalDate editedDate = LocalDate.of(2020, 11, 03);
        req.setBoardingDate(editedDate);
        shift.setBoardingDate(editedDate);

        when(shiftService.editShift(any())).thenReturn(shift);

        mvc.perform(put(new URI("/api/shift/edit"))
                .content(shiftRequestJson.write(req).getJson())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("2020-11-03")));

        verify(shiftService).editShift(acShift.capture());
        Shift captured = acShift.getValue();
        assertEquals(editedDate, captured.getBoardingDate());
    }

    @Test
    @WithMockUser(username = "john@doe123")
    public void testRemoveShift() throws Exception {
        var shift = getShifts().get(0);

        when(shiftService.getShiftById(eq(2L), eq("john@doe123"))).thenReturn(null);
        when(shiftService.getShiftById(eq(1L), eq("john@doe123"))).thenReturn(shift);

        mvc.perform(delete(new URI("/api/shift/remove"))
                .param("id", "2"))
                .andExpect(status().isBadRequest());

        mvc.perform(delete(new URI("/api/shift/remove"))
                .param("id", "1"))
                .andExpect(status().isNoContent());

        verify(shiftService).removeShift(acLong.capture());
        Long idToDelete = acLong.getValue();

        assertEquals(1, idToDelete);
    }

    private ShiftRequest getShiftRequest() {
        var shiftRequest = new ShiftRequest();
        shiftRequest.setUnavailabilityStartDate(LocalDate.of(2020, 11, 01));
        shiftRequest.setBoardingDate(LocalDate.of(2020, 11, 02));
        shiftRequest.setLeavingDate(LocalDate.of(2020, 11, 28));
        shiftRequest.setUnavailabilityEndDate(LocalDate.of(2020, 11, 29));
        shiftRequest.setRepeat(1);
        return shiftRequest;
    }

    private List<Shift> getShifts() {
        Shift shift1 = new Shift();
        shift1.setShiftId(1L);
        shift1.setUnavailabilityStartDate(LocalDate.of(2020, 11, 01));
        shift1.setBoardingDate(LocalDate.of(2020, 11, 02));
        shift1.setLeavingDate(LocalDate.of(2020, 11, 28));
        shift1.setUnavailabilityEndDate(LocalDate.of(2020, 11, 29));

        Shift shift2 = new Shift();
        shift2.setShiftId(2L);
        shift2.setUnavailabilityStartDate(LocalDate.of(2018, 12, 01));
        shift2.setBoardingDate(LocalDate.of(2018, 12, 03));
        shift2.setLeavingDate(LocalDate.of(2019, 01, 26));
        shift2.setUnavailabilityEndDate(LocalDate.of(2019, 01, 27));

        return Arrays.asList(shift1, shift2);
    }


    private List<AppUser> getUsers() {
        AppUser user = new AppUser();
        user.setUserInfo(new UserInfo());
        user.setUserId(1L);
        user.getUserInfo().setName("John Doe");
        user.getUserInfo().setEmail("john_doe@gmail.com");
        user.getUserInfo().setUsername("john@doe123");
        user.getUserInfo().setPassword("testPassword");

        AppUser user2 = new AppUser();
        user2.setUserInfo(new UserInfo());
        user2.setUserId(2L);
        user2.getUserInfo().setName("Jane Doe");
        user2.getUserInfo().setEmail("janedoe12@gmail.com");
        user2.getUserInfo().setUsername("jane_girl");
        user2.getUserInfo().setPassword("testpassword2");

        return Arrays.asList(user, user2);
    }


}
