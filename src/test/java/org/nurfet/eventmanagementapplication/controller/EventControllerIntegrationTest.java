package org.nurfet.eventmanagementapplication.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.nurfet.eventmanagementapplication.dto.EventDTO;
import org.nurfet.eventmanagementapplication.dto.EventRegistrationDTO;
import org.nurfet.eventmanagementapplication.dto.RoomDTO;
import org.nurfet.eventmanagementapplication.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import io.restassured.RestAssured;


import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;



@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class EventControllerIntegrationTest {

    @LocalServerPort
    private Integer port;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:latest"
    );

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private EventRepository eventRepository;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + port;
        eventRepository.deleteAll();
    }

    private RoomDTO createRoom(String name, int capacity) throws Exception {

        RoomDTO roomDTO = new RoomDTO();
        roomDTO.setName(name);
        roomDTO.setCapacity(capacity);

        String roomResponse = mockMvc.perform(post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roomDTO)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(roomResponse, RoomDTO.class);

    }

    private EventDTO createEvent(String name, LocalDateTime startTime, LocalDateTime endTime, Long roomId) throws Exception {
        EventDTO eventDTO = new EventDTO();
        eventDTO.setName("Конференция по Spring");
        eventDTO.setStartTime(startTime);
        eventDTO.setEndTime(endTime);
        eventDTO.setRoomId(roomId);

        String eventResponse = mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventDTO)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(eventResponse, EventDTO.class);
    }

    @Test
    void endToEndTest_ShouldRegisterParticipantSuccessfully() throws Exception {
        // 1. Создание помещения

        RoomDTO createdRoom = createRoom("Конференц-зал А", 50);

        // 2. Создание мероприятия
        EventDTO createdEvent = createEvent(
                "Конференция по Spring",
                LocalDateTime.now().plusDays(1).withHour(10).withMinute(0),
                LocalDateTime.now().plusDays(1).withHour(12).withMinute(0),
                createdRoom.getId()
        );


        EventRegistrationDTO registrationDTO = new EventRegistrationDTO();
        registrationDTO.setFirstName("Иван");
        registrationDTO.setLastName("Петров");
        registrationDTO.setEmail("ivan@example.com");
        registrationDTO.setPhone("+7(999)999-99-99");

        String responseContent = mockMvc.perform(post("/api/events/1/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDTO)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String message = objectMapper.readTree(responseContent).get("message").asText();
        assertEquals("Участник Иван Петров успешно зарегистрирован на мероприятии с идентификатором 1", message);
    }

    @Test
    void getEventsBetweenDates_WithValidDates_ShouldReturnEvents() throws Exception {

        // 1. Создание помещения
        RoomDTO createdRoom = createRoom("Конференц-зал А", 50);

        // 2. Создание мероприятия
        LocalDateTime eventStartTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withNano(0);
        LocalDateTime eventEndTime = LocalDateTime.now().plusDays(1).withHour(12).withMinute(0).withNano(0);

        EventDTO createdEvent = createEvent(
                "Конференция по Spring",
                eventStartTime,
                eventEndTime,
                createdRoom.getId()
        );


        String start = LocalDateTime.now().plusDays(1).withHour(11).withMinute(0).withNano(0).toString();
        String end = LocalDateTime.now().plusDays(1).withHour(12).withMinute(0).withNano(0).toString();

        mockMvc.perform(get("/api/events/between")
                        .param("start", start)
                        .param("end", end)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(createdEvent.getId()))
                .andExpect(jsonPath("$[0].name").value(createdEvent.getName()))
                .andExpect(jsonPath("$[0].startTime").value(createdEvent.getStartTime().toString()))
                .andExpect(jsonPath("$[0].endTime").value(createdEvent.getEndTime().toString()))
                .andExpect(jsonPath("$[0].roomId").value(createdRoom.getId()))
                .andExpect(jsonPath("$[0].roomName").value(createdRoom.getName()));
    }
}
