package edu.ucsb.cs156.example.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import edu.ucsb.cs156.example.entities.Articles;

/**
 * The articlesRepository is a repository for article entities
 */
@Repository
public interface articlesRepository extends CrudRepository<Articles, Long> {
}