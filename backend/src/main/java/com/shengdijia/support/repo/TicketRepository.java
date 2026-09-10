package com.shengdijia.support.repo;

import com.shengdijia.support.domain.Ticket;
import com.shengdijia.support.domain.TicketStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {

    Optional<Ticket> findByTicketNo(String ticketNo);

    @EntityGraph(attributePaths = {"initiator", "handler"})
    @Query("SELECT t FROM Ticket t WHERE t.id = :id")
    Optional<Ticket> findDetailedById(@Param("id") Long id);

    @Query("SELECT t.status, COUNT(t) FROM Ticket t GROUP BY t.status")
    List<Object[]> countGroupByStatus();

    @Query("SELECT t.status, COUNT(t) FROM Ticket t WHERE t.initiator.company = :company GROUP BY t.status")
    List<Object[]> countGroupByStatusForCompany(@Param("company") String company);

    @EntityGraph(attributePaths = {"initiator", "handler"})
    List<Ticket> findTop8ByOrderByUpdatedAtDesc();

    @EntityGraph(attributePaths = {"initiator", "handler"})
    List<Ticket> findTop8ByInitiatorCompanyOrderByUpdatedAtDesc(String company);

    @Query("SELECT MAX(t.ticketNo) FROM Ticket t WHERE t.ticketNo LIKE CONCAT(:prefix, '%')")
    Optional<String> findMaxTicketNoByPrefix(@Param("prefix") String prefix);

    long countByCreatedAtBetween(LocalDateTime from, LocalDateTime to);

    long countByInitiatorCompanyAndCreatedAtBetween(String company, LocalDateTime from, LocalDateTime to);
}
