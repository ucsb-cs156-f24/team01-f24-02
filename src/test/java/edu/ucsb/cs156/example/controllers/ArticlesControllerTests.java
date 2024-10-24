package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import lombok.With;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Articles;
import edu.ucsb.cs156.example.entities.Restaurant;
import edu.ucsb.cs156.example.entities.UCSBDate;
import edu.ucsb.cs156.example.repositories.articlesRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
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

@WebMvcTest(controllers = ArticlesController.class)
@Import(TestConfig.class)
public class ArticlesControllerTests  extends ControllerTestCase {
    @MockBean
    articlesRepository articlesRepository;

    @MockBean
    UserRepository userRepository;

    //authorization tests for /api/articles/admin/all
    @Test
    public void logged_out_users_cannot_get_all() throws Exception {
        mockMvc.perform(get("/api/articles/all"))
            .andExpect(status().is(403)); //logged out users can't get all
    }
    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_users_can_get_all() throws Exception {
        mockMvc.perform(get("/api/articles/all"))
            .andExpect(status().is(200)); // logged
    }
    @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/articles?id=7"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }
    // Authorization tests for /api/ucsbdates/post
        // (Perhaps should also have these for put and delete)
    @Test
    public void logged_out_users_cannot_post() throws Exception {
            mockMvc.perform(post("/api/articles/post"))
                    .andExpect(status().is(403));
    }
    
    @WithMockUser(roles = { "USER" })
    @Test
    public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

        // arrange
        LocalDateTime ldt = LocalDateTime.parse("2022-01-03T00:00:00");

        Articles article = Articles.builder()
                        .title("hey there")
                        .url("woah.com")
                        .explanation("nice seeing ya")
                        .email("billybobjoe123@gmail.com")
                        .dateAdded(ldt)
                        .build();

        when(articlesRepository.findById(eq(7L))).thenReturn(Optional.of(article));

        // act
        MvcResult response = mockMvc.perform(get("/api/articles?id=7"))
                        .andExpect(status().isOk()).andReturn();

        // assert

        verify(articlesRepository, times(1)).findById(eq(7L));
        String expectedJson = mapper.writeValueAsString(article);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

            // arrange

            when(articlesRepository.findById(eq(7L))).thenReturn(Optional.empty());

            // act
            MvcResult response = mockMvc.perform(get("/api/articles?id=7"))
                            .andExpect(status().isNotFound()).andReturn();

            // assert

            verify(articlesRepository, times(1)).findById(eq(7L));
            Map<String, Object> json = responseToJson(response);
            assertEquals("EntityNotFoundException", json.get("type"));
            assertEquals("Articles with id 7 not found", json.get("message"));
    }
    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void an_admin_user_can_post_a_new_article() throws Exception {
            // arrange

            Restaurant restaurant1 = Restaurant.builder()
                            .name("Chipotle")
                            .description("Mexican")
                            .build();

            when(restaurantRepository.save(eq(restaurant1))).thenReturn(restaurant1);

            // act
            MvcResult response = mockMvc.perform(
                            post("/api/restaurants/post?name=Chipotle&description=Mexican")
                                            .with(csrf()))
                            .andExpect(status().isOk()).andReturn();

            // assert
            verify(restaurantRepository, times(1)).save(restaurant1);
            String expectedJson = mapper.writeValueAsString(restaurant1);
            String responseString = response.getResponse().getContentAsString();
            assertEquals(expectedJson, responseString);
    }

    
}
