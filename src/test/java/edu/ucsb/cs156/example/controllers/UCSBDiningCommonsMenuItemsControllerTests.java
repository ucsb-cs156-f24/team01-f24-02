package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenuItems;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuItemsRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.time.LocalDateTime;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = UCSBDiningCommonsMenuItemsController.class)
@Import(TestConfig.class)
public class UCSBDiningCommonsMenuItemsControllerTests extends ControllerTestCase {

        @MockBean
        UCSBDiningCommonsMenuItemsRepository ucsbDiningCommonsMenuItemsRepository;

        @MockBean
        UserRepository userRepository;

        // Authorization tests for /api/ucsbDiningCommonsMenuItems/admin/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/ucsbDiningCommonsMenuItems/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/ucsbDiningCommonsMenuItems/all"))
                                .andExpect(status().is(200)); // logged
        }

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/ucsbDiningCommonsMenuItems?id=7"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        // Authorization tests for /api/ucsbDiningCommonsMenuItems/post
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/ucsbDiningCommonsMenuItems/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/ucsbDiningCommonsMenuItems/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {
                UCSBDiningCommonsMenuItems ucsbDiningCommonsMenuItems = UCSBDiningCommonsMenuItems.builder()
                                .name("food1")
                                .station("station1")
                                .diningCommonsCode("dc1")
                                .build();

                when(ucsbDiningCommonsMenuItemsRepository.findById(eq(7L))).thenReturn(Optional.of(ucsbDiningCommonsMenuItems));

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsbDiningCommonsMenuItems?id=7"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(ucsbDiningCommonsMenuItemsRepository, times(1)).findById(eq(7L));
                String expectedJson = mapper.writeValueAsString(ucsbDiningCommonsMenuItems);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(ucsbDiningCommonsMenuItemsRepository.findById(eq(7L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsbDiningCommonsMenuItems?id=7"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(ucsbDiningCommonsMenuItemsRepository, times(1)).findById(eq(7L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("UCSBDiningCommonsMenuItems with id 7 not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_ucsbDiningCommonsMenuItems() throws Exception {

                UCSBDiningCommonsMenuItems ucsbDiningCommonsMenuItems1 = UCSBDiningCommonsMenuItems.builder()
                                .name("food1")
                                .station("station1")
                                .diningCommonsCode("dc1")
                                .build();

                UCSBDiningCommonsMenuItems ucsbDiningCommonsMenuItems2 = UCSBDiningCommonsMenuItems.builder()
                                .name("food2")
                                .station("station2")
                                .diningCommonsCode("dc2")
                                .build();

                ArrayList<UCSBDiningCommonsMenuItems> expectedDiningCommonsMenuItems = new ArrayList<>();
                expectedDiningCommonsMenuItems.addAll(Arrays.asList(ucsbDiningCommonsMenuItems1, ucsbDiningCommonsMenuItems2));

                when(ucsbDiningCommonsMenuItemsRepository.findAll()).thenReturn(expectedDiningCommonsMenuItems);

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsbDiningCommonsMenuItems/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(ucsbDiningCommonsMenuItemsRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedDiningCommonsMenuItems);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_ucsbdiningcommonsmenuitems() throws Exception {
                // arrange

                UCSBDiningCommonsMenuItems ucsbDiningCommonsMenuItems1 = UCSBDiningCommonsMenuItems.builder()
                                .name("food1")
                                .station("station1")
                                .diningCommonsCode("dc1")
                                .build();

                when(ucsbDiningCommonsMenuItemsRepository.save(eq(ucsbDiningCommonsMenuItems1))).thenReturn(ucsbDiningCommonsMenuItems1);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/ucsbDiningCommonsMenuItems/post?name=food1&station=station1&diningCommonsCode=dc1")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbDiningCommonsMenuItemsRepository, times(1)).save(ucsbDiningCommonsMenuItems1);
                String expectedJson = mapper.writeValueAsString(ucsbDiningCommonsMenuItems1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_DiningCommonsMenuItems() throws Exception {
                UCSBDiningCommonsMenuItems ucsbDiningCommonsMenuItems1 = UCSBDiningCommonsMenuItems.builder()
                                .name("food1")
                                .station("station1")
                                .diningCommonsCode("dc1")
                                .build();

                when(ucsbDiningCommonsMenuItemsRepository.findById(eq(15L))).thenReturn(Optional.of(ucsbDiningCommonsMenuItems1));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/ucsbDiningCommonsMenuItems?id=15")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbDiningCommonsMenuItemsRepository, times(1)).findById(15L);
                verify(ucsbDiningCommonsMenuItemsRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBDiningCommonsMenuItems with id 15 deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_ucsbDiningCommonsMenuItems_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(ucsbDiningCommonsMenuItemsRepository.findById(eq(15L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/ucsbDiningCommonsMenuItems?id=15")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(ucsbDiningCommonsMenuItemsRepository, times(1)).findById(15L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBDiningCommonsMenuItems with id 15 not found", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_ucsbDiningCommonsMenuItems() throws Exception {
                UCSBDiningCommonsMenuItems ucsbDiningCommonsMenuItemsOrig = UCSBDiningCommonsMenuItems.builder()
                                .name("food1")
                                .station("station1")
                                .diningCommonsCode("dc1")
                                .build();

                UCSBDiningCommonsMenuItems ucsbDiningCommonsMenuItemsEdited = UCSBDiningCommonsMenuItems.builder()
                                .name("food2")
                                .station("station2")
                                .diningCommonsCode("dc2")
                                .build();

                String requestBody = mapper.writeValueAsString(ucsbDiningCommonsMenuItemsEdited);

                when(ucsbDiningCommonsMenuItemsRepository.findById(eq(67L))).thenReturn(Optional.of(ucsbDiningCommonsMenuItemsOrig));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/ucsbDiningCommonsMenuItems?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbDiningCommonsMenuItemsRepository, times(1)).findById(67L);
                verify(ucsbDiningCommonsMenuItemsRepository, times(1)).save(ucsbDiningCommonsMenuItemsEdited); // should be saved with correct user
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_ucsbDiningCommonsMenuItems_that_does_not_exist() throws Exception {
                UCSBDiningCommonsMenuItems ucsbEditedDiningCommonsMenuItems = UCSBDiningCommonsMenuItems.builder()
                                .name("food1")
                                .station("station1")
                                .diningCommonsCode("dc1")
                                .build();

                String requestBody = mapper.writeValueAsString(ucsbEditedDiningCommonsMenuItems);

                when(ucsbDiningCommonsMenuItemsRepository.findById(eq(67L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/ucsbDiningCommonsMenuItems?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(ucsbDiningCommonsMenuItemsRepository, times(1)).findById(67L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBDiningCommonsMenuItems with id 67 not found", json.get("message"));

        }
}