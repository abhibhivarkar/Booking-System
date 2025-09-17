package com.example.booking.repository;

import com.example.booking.model.Reservation;
import com.example.booking.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long>,
        JpaSpecificationExecutor<Reservation> {

    @Query("select r from Reservation r where r.resource.id = :resourceId and r.status = :status and r.startTime < :end and r.endTime > :start")
    List<Reservation> findOverlappingReservations(@Param("resourceId") Long resourceId,
                                                  @Param("status") ReservationStatus status,
                                                  @Param("start") Instant start,
                                                  @Param("end") Instant end);
}
