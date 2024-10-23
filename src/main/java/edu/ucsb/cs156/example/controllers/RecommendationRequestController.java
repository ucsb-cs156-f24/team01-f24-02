package edu.ucsb.cs156.example.controllers;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import edu.ucsb.cs156.example.entities.RecommendationRequest;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.RecommendationRequestRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

/**
 * This is a REST controller for RecommendationRequests
 */

@Tag(name = "RecommendationRequests")
@RequestMapping("/api/recommendationrequests")
@RestController
@Slf4j
public class RecommendationRequestController extends ApiController {

    @Autowired
    RecommendationRequestRepository recommendationRequestRepository;

    /**
     * List all Recommendation requests
     * 
     * @return an iterable of RecommendationRequest
     */
    @Operation(summary= "List all recommendation requests")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<RecommendationRequest> allRecommendationRequests() {
        Iterable<RecommendationRequest> requests = recommendationRequestRepository.findAll();
        return requests;
    }

    /**
     * Get a single request by id
     * 
     * @param id the id of the request
     * @return a RecommendationRequest
     */
    @Operation(summary= "Get a single request")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public RecommendationRequest getById(
            @Parameter(name="id") @RequestParam Long id) {
        RecommendationRequest recommendationRequest = recommendationRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(RecommendationRequest.class, id));

        return recommendationRequest;
    }

    /**
     * Create a new request
     * 
     * @param requesterEmail email of requester
     * @param professorEmail email of professor
     * @param explanation    reason for recommendation
     * @param dateRequested  date of request
     * @param dateNeeded     date request must be processed by
     * @param done           status of request
     * @return the saved recommendationrequest
     */
    @Operation(summary= "Create a new date")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public RecommendationRequest postRecommendationRequest(
            @Parameter(name="requesterEmail") @RequestParam String requesterEmail,
            @Parameter(name="professorEmail") @RequestParam String professorEmail,
            @Parameter(name="explanation") @RequestParam String explanation,
            @Parameter(name="dateRequested", description="date (in iso format, e.g. YYYY-mm-ddTHH:MM:SS; see https://en.wikipedia.org/wiki/ISO_8601)") @RequestParam("dateRequested") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateRequested,
            @Parameter(name="dateNeeded", description="date (in iso format, e.g. YYYY-mm-ddTHH:MM:SS; see https://en.wikipedia.org/wiki/ISO_8601)") @RequestParam("dateNeeded") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateNeeded,
            @Parameter(name="done") @RequestParam boolean done)
            throws JsonProcessingException {

        // For an explanation of @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        // See: https://www.baeldung.com/spring-date-parameters

        RecommendationRequest recommendationRequest = RecommendationRequest.builder()
            .requesterEmail(requesterEmail)
            .professorEmail(professorEmail)
            .explanation(explanation)
            .dateRequested(dateRequested)
            .dateNeeded(dateNeeded)
            .done(done)
            .build();
        
        RecommendationRequest savedRecommendationRequest = recommendationRequestRepository.save(recommendationRequest);

        return savedRecommendationRequest;
    }

    /**
     * Delete a RecommendationRequest
     * 
     * @param id the id of the date to delete
     * @return a message indicating the date was deleted
     */
    @Operation(summary= "Delete a RecommendationRequest")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteRecommendationRequest(
            @Parameter(name="id") @RequestParam Long id) {
        RecommendationRequest recommendationRequest = recommendationRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(RecommendationRequest.class, id));

        recommendationRequestRepository.delete(recommendationRequest);
        return genericMessage("RecommendationRequest with id %s deleted".formatted(id));
    }

    /**
     * Update a single date
     * 
     * @param id       id of the date to update
     * @param incoming the new date
     * @return the updated date object
     */
    @Operation(summary= "Update a single date")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public RecommendationRequest updateRecommendationRequest(
            @Parameter(name="id") @RequestParam Long id,
            @RequestBody @Valid RecommendationRequest incoming) {

        RecommendationRequest recommendationRequest = recommendationRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(RecommendationRequest.class, id));

        recommendationRequest.setRequesterEmail(incoming.getRequesterEmail());
        recommendationRequest.setProfessorEmail(incoming.getProfessorEmail());
        recommendationRequest.setExplanation(incoming.getExplanation());
        recommendationRequest.setDateRequested(incoming.getDateRequested());
        recommendationRequest.setDateNeeded(incoming.getDateNeeded());
        recommendationRequest.setDone(incoming.getDone());
        
        recommendationRequestRepository.save(recommendationRequest);

        return recommendationRequest;
    }
}
