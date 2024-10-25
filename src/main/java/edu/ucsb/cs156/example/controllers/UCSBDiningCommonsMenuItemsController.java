package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenuItems;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuItemsRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonProcessingException;

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

import jakarta.validation.Valid;

import java.time.LocalDateTime;

@Tag(name = "UCSBDiningCommonsMenuItems")
@RequestMapping("/api/ucsbDiningCommonsMenuItems")
@RestController
@Slf4j

public class UCSBDiningCommonsMenuItemsController extends ApiController {

    @Autowired
    UCSBDiningCommonsMenuItemsRepository ucsbDiningCommonsMenuItemsRepository;

    /**
     * List all UCSB dining common items
     * 
     * @return an iterable of UCSBDiningCommonsMenuItems
     */
    @Operation(summary= "List all ucsb dining common menu items")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<UCSBDiningCommonsMenuItems> allUCSBDiningCommonsMenuItems() {
        Iterable<UCSBDiningCommonsMenuItems> items = ucsbDiningCommonsMenuItemsRepository.findAll();
        return items;
    }

    /**
     * Get a single DiningCommonsMenuItems by id
     * 
     * @param id the id of the DiningCommonsMenuItems
     * @return a UCSBDiningCommonsMenuItems
     */
    @Operation(summary= "Get a single DiningCommonsMenuItems")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public UCSBDiningCommonsMenuItems getById(
            @Parameter(name="id") @RequestParam Long id) {
        UCSBDiningCommonsMenuItems ucsbDiningCommonsMenuItems = ucsbDiningCommonsMenuItemsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItems.class, id));

        return ucsbDiningCommonsMenuItems;
    }

    /**
     * Create a new DiningCommonsMenuItems
     * 
     * @param diningCommonsCode     the dining common code 
     * @param name                  the name items
     * @param station               the item station
     * @return the saved menu item
     */
    @Operation(summary= "Create a new menu item")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public UCSBDiningCommonsMenuItems postUCSBDiningCommonsMenuItems(
            @Parameter(name="diningCommonsCode") @RequestParam String diningCommonsCode,
            @Parameter(name="name") @RequestParam String name,
            @Parameter(name="station") @RequestParam String station)
            throws JsonProcessingException {

        UCSBDiningCommonsMenuItems ucsbDiningCommonsMenuItems = new UCSBDiningCommonsMenuItems();
        ucsbDiningCommonsMenuItems.setDiningCommonsCode(diningCommonsCode);
        ucsbDiningCommonsMenuItems.setName(name);
        ucsbDiningCommonsMenuItems.setStation(station);

        UCSBDiningCommonsMenuItems savedUcsbDiningCommonsMenuItems = ucsbDiningCommonsMenuItemsRepository.save(ucsbDiningCommonsMenuItems);

        return savedUcsbDiningCommonsMenuItems;
    }

    /**
     * Delete a UCSBDiningCommonsMenuItems
     * 
     * @param id the id of the DiningCommonsMenuItems to delete
     * @return a message indicating the DiningCommonsMenuItems was deleted
     */
    @Operation(summary= "Delete a UCSBDiningCommonsMenuItems")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteUCSBDiningCommonsMenuItems(
            @Parameter(name="id") @RequestParam Long id) {
        UCSBDiningCommonsMenuItems ucsbDiningCommonsMenuItems = ucsbDiningCommonsMenuItemsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItems.class, id));

        ucsbDiningCommonsMenuItemsRepository.delete(ucsbDiningCommonsMenuItems);
        return genericMessage("UCSBDiningCommonsMenuItems with id %s deleted".formatted(id));
    }

    /**
     * Update a single DiningCommonsMenuItems
     * 
     * @param id       id of the DiningCommonsMenuItems to update
     * @param incoming the new DiningCommonsMenuItems
     * @return the updated DiningCommonsMenuItems object
     */
    @Operation(summary= "Update a single DiningCommonsMenuItems")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public UCSBDiningCommonsMenuItems updateUCSBDiningCommonsMenuItems(
            @Parameter(name="id") @RequestParam Long id,
            @RequestBody @Valid UCSBDiningCommonsMenuItems incoming) {

        UCSBDiningCommonsMenuItems ucsbDiningCommonsMenuItems = ucsbDiningCommonsMenuItemsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItems.class, id));

        ucsbDiningCommonsMenuItems.setDiningCommonsCode(incoming.getDiningCommonsCode());
        ucsbDiningCommonsMenuItems.setName(incoming.getName());
        ucsbDiningCommonsMenuItems.setStation(incoming.getStation());

        ucsbDiningCommonsMenuItemsRepository.save(ucsbDiningCommonsMenuItems);

        return ucsbDiningCommonsMenuItems;
    }
}