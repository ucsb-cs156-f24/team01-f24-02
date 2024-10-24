package edu.ucsb.cs156.example.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * This is a JPA entity that represents a help request.
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "helprequests")
public class HelpRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String requesterEmail;
    private String teamId;
    private String tableOrBreakoutRoom;
    private LocalDateTime requestTime;
    private String explanation;
    private boolean solved;
}
