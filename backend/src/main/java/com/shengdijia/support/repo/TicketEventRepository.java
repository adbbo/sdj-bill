package com.shengdijia.support.repo;

import com.shengdijia.support.domain.TicketEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TicketEventRepository extends JpaRepository<TicketEvent, Long> {

    @Query("SELECT e FROM TicketEvent e LEFT JOIN FETCH e.author WHERE e.ticket.id = :ticketId ORDER BY e.createdAt ASC")
    List<TicketEvent> findByTicketIdOrderByCreatedAtAsc(@Param("ticketId") Long ticketId);
}
