package edu.ucsb.cs156.example.controllers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.RecommendationRequest;
import edu.ucsb.cs156.example.repositories.RecommendationRequestRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;

@WebMvcTest(controllers = RecommendationRequestController.class)
@Import(TestConfig.class)
public class RecommendationRequestControllerTests extends ControllerTestCase {

        @MockBean
        RecommendationRequestRepository recommendationRequestRepository;

        @MockBean
        UserRepository userRepository;

        // Authorization tests for /api/recommendationrequests/admin/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/recommendationrequests/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/recommendationrequests/all"))
                                .andExpect(status().is(200)); // logged
        }

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/recommendationrequests?id=7"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        // Authorization tests for /api/recommendationrequests/post
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/recommendationrequests/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/recommendationrequests/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        // // Tests with mocks for database actions

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange
                LocalDateTime requested = LocalDateTime.parse("2022-01-03T00:00:00");
                LocalDateTime needed = LocalDateTime.parse("2022-01-13T00:00:00");

                RecommendationRequest recommendationRequest = RecommendationRequest.builder()
                                .professorEmail("prof@ucsb.edu")
                                .requesterEmail("requester@ucsb.edu")
                                .dateRequested(requested)
                                .dateNeeded(needed)
                                .explanation("Trying to apply for grad school")
                                .done(false)
                                .build();

                when(recommendationRequestRepository.findById(eq(7L))).thenReturn(Optional.of(recommendationRequest));

                // act
                MvcResult response = mockMvc.perform(get("/api/recommendationrequests?id=7"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(recommendationRequestRepository, times(1)).findById(eq(7L));
                String expectedJson = mapper.writeValueAsString(recommendationRequest);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(recommendationRequestRepository.findById(eq(7L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/recommendationrequests?id=7"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(recommendationRequestRepository, times(1)).findById(eq(7L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("RecommendationRequest with id 7 not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_recommendationrequests() throws Exception {

                // arrange

                                // arrange
                LocalDateTime requested1 = LocalDateTime.parse("2022-01-03T00:00:00");
                LocalDateTime needed1 = LocalDateTime.parse("2022-01-13T00:00:00");
                LocalDateTime requested2 = LocalDateTime.parse("2023-11-21T00:00:00");
                LocalDateTime needed2 = LocalDateTime.parse("2023-05-30T00:00:00");
                RecommendationRequest recommendationRequest1 = RecommendationRequest.builder()
                                .professorEmail("prof@ucsb.edu")
                                .requesterEmail("requester@ucsb.edu")
                                .dateRequested(requested1)
                                .dateNeeded(needed1)
                                .explanation("Trying to apply for grad school")
                                .done(false)
                                .build();

                RecommendationRequest recommendationRequest2 = RecommendationRequest.builder()
                                .professorEmail("otherprof@ucsb.edu")
                                .requesterEmail("otherrequester@ucsb.edu")
                                .dateRequested(requested2)
                                .dateNeeded(needed2)
                                .explanation("Trying to apply for internship")
                                .done(true)
                                .build();

                ArrayList<RecommendationRequest> expectedDates = new ArrayList<>();
                expectedDates.addAll(Arrays.asList(recommendationRequest1, recommendationRequest2));

                when(recommendationRequestRepository.findAll()).thenReturn(expectedDates);

                // act
                MvcResult response = mockMvc.perform(get("/api/recommendationrequests/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(recommendationRequestRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedDates);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_recommendationrequest() throws Exception {
                // arrange
                LocalDateTime requested = LocalDateTime.parse("2022-01-03T00:00:00");
                LocalDateTime needed = LocalDateTime.parse("2022-01-13T00:00:00");

                RecommendationRequest recommendationRequest = RecommendationRequest.builder()
                                .professorEmail("prof@ucsb.edu")
                                .requesterEmail("requester@ucsb.edu")
                                .dateRequested(requested)
                                .dateNeeded(needed)
                                .explanation("Trying to apply for grad school")
                                .done(false)
                                .build();


                when(recommendationRequestRepository.save(eq(recommendationRequest))).thenReturn(recommendationRequest);

                // act
                MvcResult response = mockMvc.perform(
                                post(
                                    "/api/recommendationrequests/" +
                                    "post?professorEmail=prof@ucsb.edu&" +
                                    "requesterEmail=requester@ucsb.edu&" +
                                    "explanation=Trying to apply for grad school&" +
                                    "dateRequested=2022-01-03T00:00:00&" +
                                    "dateNeeded=2022-01-13T00:00:00&" +
                                    "done=false"
                                    )
                                    .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(recommendationRequestRepository, times(1)).save(recommendationRequest);
                String expectedJson = mapper.writeValueAsString(recommendationRequest);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_request() throws Exception {
                // arrange

                LocalDateTime requested = LocalDateTime.parse("2022-01-03T00:00:00");
                LocalDateTime needed = LocalDateTime.parse("2022-01-13T00:00:00");

                RecommendationRequest recommendationRequest = RecommendationRequest.builder()
                                .professorEmail("prof@ucsb.edu")
                                .requesterEmail("requester@ucsb.edu")
                                .dateRequested(requested)
                                .dateNeeded(needed)
                                .explanation("Trying to apply for grad school")
                                .done(false)
                                .build();

                when(recommendationRequestRepository.findById(eq(15L))).thenReturn(Optional.of(recommendationRequest));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/recommendationrequests?id=15")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(recommendationRequestRepository, times(1)).findById(15L);
                verify(recommendationRequestRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("RecommendationRequest with id 15 deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_recommendationrequest_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(recommendationRequestRepository.findById(eq(15L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/recommendationrequests?id=15")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(recommendationRequestRepository, times(1)).findById(15L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("RecommendationRequest with id 15 not found", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_recommendationrequest() throws Exception {
                // arrange
                LocalDateTime requested1 = LocalDateTime.parse("2022-01-03T00:00:00");
                LocalDateTime needed1 = LocalDateTime.parse("2022-01-13T00:00:00");
                LocalDateTime requested2 = LocalDateTime.parse("2023-11-21T00:00:00");
                LocalDateTime needed2 = LocalDateTime.parse("2023-05-30T00:00:00");
                RecommendationRequest recommendationRequestOrig = RecommendationRequest.builder()
                                .professorEmail("prof@ucsb.edu")
                                .requesterEmail("requester@ucsb.edu")
                                .dateRequested(requested1)
                                .dateNeeded(needed1)
                                .explanation("Trying to apply for grad school")
                                .done(false)
                                .build();

                RecommendationRequest recommendationRequestEdited = RecommendationRequest.builder()
                                .professorEmail("otherprof@ucsb.edu")
                                .requesterEmail("otherrequester@ucsb.edu")
                                .dateRequested(requested2)
                                .dateNeeded(needed2)
                                .explanation("Trying to apply for internship")
                                .done(true)
                                .build();

                String requestBody = mapper.writeValueAsString(recommendationRequestEdited);

                when(recommendationRequestRepository.findById(eq(67L))).thenReturn(Optional.of(recommendationRequestOrig));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/recommendationrequests?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(recommendationRequestRepository, times(1)).findById(67L);
                verify(recommendationRequestRepository, times(1)).save(recommendationRequestEdited); // should be saved with correct user
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_recommendationrequest_that_does_not_exist() throws Exception {
                // arrange
                LocalDateTime requested = LocalDateTime.parse("2022-01-03T00:00:00");
                LocalDateTime needed = LocalDateTime.parse("2022-01-13T00:00:00");

                RecommendationRequest recommendationRequestEdited = RecommendationRequest.builder()
                                .professorEmail("prof@ucsb.edu")
                                .requesterEmail("requester@ucsb.edu")
                                .dateRequested(requested)
                                .dateNeeded(needed)
                                .explanation("Trying to apply for grad school")
                                .done(false)
                                .build();

                String requestBody = mapper.writeValueAsString(recommendationRequestEdited);

                when(recommendationRequestRepository.findById(eq(67L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/recommendationrequests?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(recommendationRequestRepository, times(1)).findById(67L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("RecommendationRequest with id 67 not found", json.get("message"));

        }
}
