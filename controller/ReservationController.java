package com.example.booking.controller;

import com.example.booking.dto.ReservationRequest;
import com.example.booking.dto.ReservationResponse;
import com.example.booking.model.Reservation;
import com.example.booking.model.ReservationStatus;
import com.example.booking.service.ReservationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private static final Logger logger = LoggerFactory.getLogger(ReservationController.class);
    private final ReservationService service;

    public ReservationController(ReservationService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<Page<ReservationResponse>> list(Authentication auth, @RequestParam(required = false) ReservationStatus status, @RequestParam(required = false) BigDecimal minPrice, @RequestParam(required = false) BigDecimal maxPrice, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @RequestParam(required = false) String sort) {
        boolean isAdmin = isAdmin(auth);
        String username = auth.getName();

        logger.info("Listing reservations for user={}, isAdmin={}, status={}, priceRange=[{},{}], page={}, size={}, sort={}", username, isAdmin, status, minPrice, maxPrice, page, size, sort);

        var reservations = service.list(username, isAdmin, status, minPrice, maxPrice, page, size, sort);
        return ResponseEntity.ok(reservations.map(this::toDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse> get(Authentication auth, @PathVariable Long id) {
        boolean isAdmin = isAdmin(auth);

        logger.info("Fetching reservation id={} by user={}, isAdmin={}", id, auth.getName(), isAdmin);

        var reservation = service.getById(id, auth.getName(), isAdmin);
        return ResponseEntity.ok(toDto(reservation));
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> create(Authentication auth, @RequestBody ReservationRequest req) {
        logger.info("Creating reservation for user={}, resourceId={}, startTime={}, endTime={}", auth.getName(), req.getResourceId(), req.getStartTime(), req.getEndTime());

        var reservation = service.create(auth.getName(), req, true);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(reservation));
    }


    @PutMapping("/{id}")
    public ResponseEntity<ReservationResponse> update(Authentication auth, @PathVariable Long id, @RequestBody ReservationRequest req) {
        boolean isAdmin = isAdmin(auth);

        logger.info("Updating reservation id={} by user={}, isAdmin={}", id, auth.getName(), isAdmin);

        var reservation = service.update(id, auth.getName(), isAdmin, req);
        return ResponseEntity.ok(toDto(reservation));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication auth, @PathVariable Long id) {
        boolean isAdmin = isAdmin(auth);

        logger.info("Deleting reservation id={} by user={}, isAdmin={}", id, auth.getName(), isAdmin);

        service.delete(id, auth.getName(), isAdmin);
        return ResponseEntity.noContent().build();
    }

    // ===== Helper Methods =====
    private ReservationResponse toDto(Reservation r) {
        ReservationResponse d = new ReservationResponse();
        d.setId(r.getId());
        d.setResourceId(r.getResource().getId());
        d.setResourceName(r.getResource().getName());
        d.setUserId(r.getUser().getId());
        d.setUsername(r.getUser().getUsername());
        d.setStatus(r.getStatus().name());
        d.setPrice(r.getPrice());
        d.setStartTime(r.getStartTime());
        d.setEndTime(r.getEndTime());
        d.setCreatedAt(r.getCreatedAt());
        d.setUpdatedAt(r.getUpdatedAt());
        return d;
    }

    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(a -> a.equals("ROLE_ADMIN"));
    }
}
