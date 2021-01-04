package com.github.rodrigo_sp17.mscheduler.controller;

import com.github.rodrigo_sp17.mscheduler.shift.ShiftController;
import com.github.rodrigo_sp17.mscheduler.shift.ShiftService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = ShiftController.class)
@AutoConfigureMockMvc
public class ShiftControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ShiftService shiftService;

    @Test
    public void testHappyPath() {

    }
}
