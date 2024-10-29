package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBDiningCommons;
import edu.ucsb.cs156.example.entities.UCSBOrganizations;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationsRepository;

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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = UCSBOrganizationsController.class)
@Import(TestConfig.class)
public class UCSBOrganizationsControllerTests extends ControllerTestCase {
    
    @MockBean
    UCSBOrganizationsRepository ucsbOrganizationsRepository;

    @MockBean
    UserRepository userRepository;

    // TEST for GET /api/ucsborganizations/all
    @Test
    public void logged_out_users_cannot_get_all() throws Exception {
        mockMvc.perform(get("/api/ucsborganizations/all"))
                        .andExpect(status().is(403)); // logged out users can't get all
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_users_can_get_all() throws Exception {
        mockMvc.perform(get("/api/ucsborganizations/all"))
                        .andExpect(status().is(200)); // logged
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_user_can_get_all_ucsborganizations() throws Exception {
        // arrange
        UCSBOrganizations zetaPhiRho = UCSBOrganizations.builder()
                    .orgCode("ZPR")
                    .orgTranslationShort("ZETA PHI RHO")
                    .orgTranslation("ZETA PHI RHO")
                    .inactive(false)
                    .build();

        UCSBOrganizations skyDivingClub = UCSBOrganizations.builder()
                    .orgCode("SKY")
                    .orgTranslationShort("SKYDIVING CLUB")
                    .orgTranslation("SKYDIVING CLUB AT UCSB")
                    .inactive(false)
                    .build();

        UCSBOrganizations studentLife = UCSBOrganizations.builder()
                    .orgCode("OSLI")
                    .orgTranslationShort("STUDENT LIFE")
                    .orgTranslation("OFFICE OF STUDENT LIFE")
                    .inactive(false)
                    .build(); 

        UCSBOrganizations koreanRadio = UCSBOrganizations.builder()
                    .orgCode("KRC")
                    .orgTranslationShort("KOREAN RADIO CL")
                    .orgTranslation("KOREAN RADIO CLUB")
                    .inactive(false)
                    .build();

        ArrayList<UCSBOrganizations> expectedOrganizations = new ArrayList<>();
        expectedOrganizations.addAll(Arrays.asList(zetaPhiRho, skyDivingClub, studentLife, koreanRadio));

        when(ucsbOrganizationsRepository.findAll()).thenReturn(expectedOrganizations);

        // act
        MvcResult response = mockMvc.perform(get("/api/ucsborganizations/all"))
                        .andExpect(status().isOk()).andReturn();

        // assert
        verify(ucsbOrganizationsRepository, times(1)).findAll();
        String expectedJson = mapper.writeValueAsString(expectedOrganizations);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    // Tests for GET /api/ucsbdiningcommons?...
    @Test
    public void logged_out_users_cannot_get_by_id() throws Exception {
        mockMvc.perform(get("/api/ucsborganizations?orgCode=sbhacks"))
                        .andExpect(status().is(403)); // logged out users can't get by id
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {
        // arrange
        UCSBOrganizations zetaPhiRho = UCSBOrganizations.builder()
                .orgCode("ZPR")
                .orgTranslationShort("ZETA PHI RHO")
                .orgTranslation("ZETA PHI RHO")
                .inactive(false)
                .build();

        when(ucsbOrganizationsRepository.findById(eq("ZPR"))).thenReturn(Optional.of(zetaPhiRho));

        // act
        MvcResult response = mockMvc.perform(get("/api/ucsborganizations?orgCode=ZPR"))
                        .andExpect(status().isOk()).andReturn();

        // assert
        verify(ucsbOrganizationsRepository, times(1)).findById(eq("ZPR"));
        String expectedJson = mapper.writeValueAsString(zetaPhiRho);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {
        // arrange
        when(ucsbOrganizationsRepository.findById(eq("sbhacks"))).thenReturn(Optional.empty());

        // act
        MvcResult response = mockMvc.perform(get("/api/ucsborganizations?orgCode=sbhacks"))
                        .andExpect(status().isNotFound()).andReturn();

        // assert
        verify(ucsbOrganizationsRepository, times(1)).findById(eq("sbhacks"));
        Map<String, Object> json = responseToJson(response);
        assertEquals("EntityNotFoundException", json.get("type"));
        assertEquals("UCSBOrganizations with id sbhacks not found", json.get("message"));
    }

    // Tests for POST /api/ucsbdiningcommons...
    @Test
    public void logged_out_users_cannot_post() throws Exception {
        mockMvc.perform(post("/api/ucsborganizations/post"))
                        .andExpect(status().is(403));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_regular_users_cannot_post() throws Exception {
        mockMvc.perform(post("/api/ucsborganizations/post"))
                        .andExpect(status().is(403)); // only admins can post
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void an_admin_user_can_post_a_new_ucsborganizations() throws Exception {
        // arrange
        UCSBOrganizations zetaPhiRho = UCSBOrganizations.builder()
                .orgCode("ZPR")
                .orgTranslationShort("ZETA PHI RHO")
                .orgTranslation("ZETA PHI RHO")
                .inactive(false)
                .build();

        when(ucsbOrganizationsRepository.save(eq(zetaPhiRho))).thenReturn(zetaPhiRho);

        // act
        MvcResult response = mockMvc.perform(
                        post("/api/ucsborganizations/post?name=zetaPhiRho&orgCode=ZPR&orgTranslationShort=ZETA PHI RHO&orgTranslation=ZETA PHI RHO&inactive=false")
                                        .with(csrf()))
                        .andExpect(status().isOk()).andReturn();

        // assert
        verify(ucsbOrganizationsRepository, times(1)).save(zetaPhiRho);
        String expectedJson = mapper.writeValueAsString(zetaPhiRho);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void an_admin_user_can_post_an_inactive_ucsborganization() throws Exception {
        // arrange
        UCSBOrganizations inactive = UCSBOrganizations.builder()
                .orgCode("INACTIVE")
                .orgTranslationShort("INACTIVE ORG")
                .orgTranslation("INACTIVE ORGANIZATION")
                .inactive(true)
                .build();

        when(ucsbOrganizationsRepository.save(eq(inactive))).thenReturn(inactive);

        // act
        MvcResult response = mockMvc.perform(
                        post("/api/ucsborganizations/post?name=INACTIVE&orgCode=INACTIVE&orgTranslationShort=INACTIVE ORG&orgTranslation=INACTIVE ORGANIZATION&inactive=true")
                                        .with(csrf()))
                        .andExpect(status().isOk()).andReturn();

        // assert
        verify(ucsbOrganizationsRepository, times(1)).save(inactive);
        String expectedJson = mapper.writeValueAsString(inactive);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    // Tests for PUT /api/ucsbdiningcommons?...
    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_can_edit_an_existing_organizations() throws Exception {
        // arrange
        UCSBOrganizations zetaPhiRhoOrig = UCSBOrganizations.builder()
                .orgCode("ZPR")
                .orgTranslationShort("ZETA PHI RHO")
                .orgTranslation("ZETA PHI RHO")
                .inactive(false)
                .build();

        UCSBOrganizations zetaPhiRhoEdited = UCSBOrganizations.builder()
                .orgCode("ZPR")
                .orgTranslationShort("ZETA PHI RHO FRAT")
                .orgTranslation("ZETA PHI RHO FRATERNITY")
                .inactive(true)
                .build();

        String requestBody = mapper.writeValueAsString(zetaPhiRhoEdited);
        when(ucsbOrganizationsRepository.findById(eq("ZPR"))).thenReturn(Optional.of(zetaPhiRhoOrig));

        // act
        MvcResult response = mockMvc.perform(
                        put("/api/ucsborganizations?orgCode=ZPR")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .characterEncoding("utf-8")
                                        .content(requestBody)
                                        .with(csrf()))
                        .andExpect(status().isOk()).andReturn();

        // assert
        verify(ucsbOrganizationsRepository, times(1)).findById("ZPR");
        verify(ucsbOrganizationsRepository, times(1)).save(zetaPhiRhoEdited); // should be saved with updated info
        String responseString = response.getResponse().getContentAsString();
        assertEquals(requestBody, responseString);
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_cannot_edit_organizations_that_does_not_exist() throws Exception {
        // arrange
        UCSBOrganizations editedOrganization = UCSBOrganizations.builder()
                .orgCode("SBHACKS")
                .orgTranslationShort("SBHACKS CLUB")
                .orgTranslation("SANTA BARBARA HACKS CLUB")
                .inactive(false)
                .build();

        String requestBody = mapper.writeValueAsString(editedOrganization);

        when(ucsbOrganizationsRepository.findById(eq("SBHACKS"))).thenReturn(Optional.empty());

        // act
        MvcResult response = mockMvc.perform(
                        put("/api/ucsborganizations?orgCode=SBHACKS")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .characterEncoding("utf-8")
                                        .content(requestBody)
                                        .with(csrf()))
                        .andExpect(status().isNotFound()).andReturn();

        // assert
        verify(ucsbOrganizationsRepository, times(1)).findById("SBHACKS");
        Map<String, Object> json = responseToJson(response);
        assertEquals("UCSBOrganizations with id SBHACKS not found", json.get("message"));
    }

    // Tests for DELETE /api/ucsbdiningcommons?...
    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_can_delete_a_organization() throws Exception {
        // arrange
        UCSBOrganizations zetaPhiRho = UCSBOrganizations.builder()
            .orgCode("ZPR")
            .orgTranslationShort("ZETA PHI RHO")
            .orgTranslation("ZETA PHI RHO")
            .inactive(false)
            .build();

        when(ucsbOrganizationsRepository.findById(eq("ZPR"))).thenReturn(Optional.of(zetaPhiRho));

        // act
        MvcResult response = mockMvc.perform(
                        delete("/api/ucsborganizations?orgCode=ZPR")
                                        .with(csrf()))
                        .andExpect(status().isOk()).andReturn();

        // assert
        verify(ucsbOrganizationsRepository, times(1)).findById("ZPR");
        verify(ucsbOrganizationsRepository, times(1)).delete(any());

        Map<String, Object> json = responseToJson(response);
        assertEquals("UCSBOrganizations with id ZPR deleted", json.get("message"));
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_tries_to_delete_non_existant_organization_and_gets_right_error_message() throws Exception {
        // arrange
        when(ucsbOrganizationsRepository.findById(eq("SBHACKS"))).thenReturn(Optional.empty());

        // act
        MvcResult response = mockMvc.perform(
                        delete("/api/ucsborganizations?orgCode=SBHACKS")
                                        .with(csrf()))
                        .andExpect(status().isNotFound()).andReturn();

        // assert
        verify(ucsbOrganizationsRepository, times(1)).findById("SBHACKS");
        Map<String, Object> json = responseToJson(response);
        assertEquals("UCSBOrganizations with id SBHACKS not found", json.get("message"));
    }
}