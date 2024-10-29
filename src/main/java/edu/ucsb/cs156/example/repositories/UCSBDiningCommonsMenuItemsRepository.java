package edu.ucsb.cs156.example.repositories;

import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenuItems;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * The UCSBDiningCommonsMenuItemsRepository is a repository for UCSBDiningCommonsMenuItems entities
 */
@Repository
public interface UCSBDiningCommonsMenuItemsRepository extends CrudRepository<UCSBDiningCommonsMenuItems, Long> {
 
}