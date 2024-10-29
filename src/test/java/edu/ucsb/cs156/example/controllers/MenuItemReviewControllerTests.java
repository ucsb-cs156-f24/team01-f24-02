package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.MenuItemReview;
import edu.ucsb.cs156.example.repositories.MenuItemReviewRepository;

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

@WebMvcTest(controllers = MenuItemReviewController.class)
@Import(TestConfig.class)
public class MenuItemReviewControllerTests extends ControllerTestCase {

        @MockBean
        //UCSBDateRepository ucsbDateRepository;
        MenuItemReviewRepository menuItemReviewRepository;


        @MockBean
        UserRepository userRepository;

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/menuitemreviews/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/menuitemreviews/all"))
                                .andExpect(status().is(200)); // logged
        }

        // @Test
        // public void logged_out_users_cannot_get_by_id() throws Exception {
        //         mockMvc.perform(get("/api/menuitemreviews?id=7"))
        //                         .andExpect(status().is(403)); // logged out users can't get by id
        // }

        // Authorization tests for /api/ucsbdates/post
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/menuitemreviews/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/menuitemreviews/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

@WithMockUser(roles = { "ADMIN", "USER" })
@Test
public void an_admin_user_can_post_a_new_menuitemreview() throws Exception {

    LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

    MenuItemReview menuItemReview = MenuItemReview.builder()
                    .itemId(123L)
                    .reviewerEmail("test@example.com")
                    .comments("Great item!")
                    .stars(5)
                    .dateReviewed(ldt1)
                    .build();

    when(menuItemReviewRepository.save(eq(menuItemReview))).thenReturn(menuItemReview);

    MvcResult response = mockMvc.perform(
                    post("/api/menuitemreviews/post?itemId=123&reviewerEmail=test@example.com&comments=Great item!&stars=5&localDateTime=2022-01-03T00:00:00")
                                    .with(csrf()))
                    .andExpect(status().isOk()).andReturn();

    verify(menuItemReviewRepository, times(1)).save(menuItemReview);
    String expectedJson = mapper.writeValueAsString(menuItemReview);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
}

@WithMockUser(roles = { "USER" })
@Test
public void logged_in_user_can_get_all_menuitemreviews() throws Exception {

                // arrange
        LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

        MenuItemReview menuItemReview1 = MenuItemReview.builder()
                        .itemId(1L)
                        .reviewerEmail("test1@example.com")
                        .comments("Great item!")
                        .stars(5)
                        .dateReviewed(LocalDateTime.now())
                        .build();

        LocalDateTime ldt2 = LocalDateTime.parse("2022-03-11T00:00:00");

        MenuItemReview menuItemReview2 = MenuItemReview.builder()
                        .itemId(2L)
                        .reviewerEmail("test2@example.com")
                        .comments("Not so great")
                        .stars(3)
                        .dateReviewed(LocalDateTime.now())
                        .build();

        ArrayList<MenuItemReview> expectedReviews = new ArrayList<>();
        expectedReviews.addAll(Arrays.asList(menuItemReview1, menuItemReview2));

        when(menuItemReviewRepository.findAll()).thenReturn(expectedReviews);

                // act
        MvcResult response = mockMvc.perform(get("/api/menuitemreviews/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

        verify(menuItemReviewRepository, times(1)).findAll();
        String expectedJson = mapper.writeValueAsString(expectedReviews);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(menuItemReviewRepository.findById(eq(7L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/menuitemreviews?id=7"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(menuItemReviewRepository, times(1)).findById(eq(7L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("MenuItemReview with id 7 not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange
                LocalDateTime ldt = LocalDateTime.parse("2022-01-03T00:00:00");

                MenuItemReview menuItemReview = MenuItemReview.builder()
                                .itemId(2L)
                                .reviewerEmail("test2@example.com")
                                .comments("Not so great")
                                .stars(3)
                                .dateReviewed(LocalDateTime.now())
                                .build();

                when(menuItemReviewRepository.findById(eq(7L))).thenReturn(Optional.of(menuItemReview));

                // act
                MvcResult response = mockMvc.perform(get("/api/menuitemreviews?id=7"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(menuItemReviewRepository, times(1)).findById(eq(7L));
                String expectedJson = mapper.writeValueAsString(menuItemReview);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }
        
        @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_can_edit_an_existing_menuitemreview() throws Exception {
        
        // arrange

        LocalDateTime ldt1 = LocalDateTime.parse("2024-03-02T00:00:00");
        LocalDateTime ldt2 = LocalDateTime.parse("2022-05-05T11:11:11");

        MenuItemReview menuItemReviewOne = MenuItemReview.builder()
                        .itemId(1L)
                        .reviewerEmail("test@gmail.com")
                        .comments("Good Food!")
                        .stars(3)
                        .dateReviewed(ldt1)
                        .build();

        MenuItemReview menuItemReviewTwo = MenuItemReview.builder()
                        .itemId(2L)
                        .reviewerEmail("testagain@example.com")
                        .comments("Was not good")
                        .stars(1)
                        .dateReviewed(ldt2)
                        .build();

        String requestBody = mapper.writeValueAsString(menuItemReviewTwo);

        when(menuItemReviewRepository.findById(eq(67L))).thenReturn(Optional.of(menuItemReviewOne));

        // act
        MvcResult response = mockMvc.perform(
                        put("/api/menuitemreviews?id=67")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .characterEncoding("utf-8")
                                        .content(requestBody)
                                        .with(csrf()))
                        .andExpect(status().isOk()).andReturn();

        // assert
        verify(menuItemReviewRepository, times(1)).findById(67L);
        verify(menuItemReviewRepository, times(1)).save(menuItemReviewTwo);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(requestBody, responseString);
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_cannot_edit_menuitemreview_that_does_not_exist() throws Exception {
        
        // arrange

        LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

        MenuItemReview menuItemReview = MenuItemReview.builder()
                        .itemId(2L)
                        .reviewerEmail("test@example.com")
                        .comments("Good Food!")
                        .stars(3)
                        .dateReviewed(ldt1)
                        .build();

        String requestBody = mapper.writeValueAsString(menuItemReview);

        when(menuItemReviewRepository.findById(eq(67L))).thenReturn(Optional.empty());

        // act
        MvcResult response = mockMvc.perform(
                        put("/api/menuitemreviews?id=67")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .characterEncoding("utf-8")
                                        .content(requestBody)
                                        .with(csrf()))
                        .andExpect(status().isNotFound()).andReturn();

        // assert
        verify(menuItemReviewRepository, times(1)).findById(67L);
        Map<String, Object> json = responseToJson(response);
        assertEquals("MenuItemReview with id 67 not found", json.get("message"));
    }


    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_can_delete_a_date() throws Exception {
            // arrange

            LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

            MenuItemReview menuItemReview1 = MenuItemReview.builder()
                        .itemId(3L)
                        .reviewerEmail("test@example.com")
                        .comments("Good food")
                        .stars(3)
                        .dateReviewed(ldt1)
                        .build();

            when(menuItemReviewRepository.findById(eq(3L))).thenReturn(Optional.of(menuItemReview1));

            // act
            MvcResult response = mockMvc.perform(
                            delete("/api/menuitemreviews?id=3")
                                            .with(csrf()))
                            .andExpect(status().isOk()).andReturn();

            // assert
            verify(menuItemReviewRepository, times(1)).findById(3L);
            verify(menuItemReviewRepository, times(1)).delete(any());

            Map<String, Object> json = responseToJson(response);
            assertEquals("MenuItemReview with id 3 deleted", json.get("message"));
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_tries_to_delete_non_existant_menuitemreview_and_gets_right_error_message()
                    throws Exception {
            // arrange

            when(menuItemReviewRepository.findById(eq(3L))).thenReturn(Optional.empty());

            // act
            MvcResult response = mockMvc.perform(
                            delete("/api/menuitemreviews?id=3")
                                            .with(csrf()))
                            .andExpect(status().isNotFound()).andReturn();

            // assert
            verify(menuItemReviewRepository, times(1)).findById(3L);
            Map<String, Object> json = responseToJson(response);
            assertEquals("MenuItemReview with id 3 not found", json.get("message"));
    }

}
//
