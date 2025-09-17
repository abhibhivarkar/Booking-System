package com.example.booking.service;

import com.example.booking.dto.ReservationRequest;
import com.example.booking.exception.ResourceNotFoundException;
import com.example.booking.model.*;
import com.example.booking.repository.ReservationRepository;
import com.example.booking.repository.ResourceRepository;
import com.example.booking.repository.UserRepository;
import com.example.booking.spec.ReservationSpecification;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;

    public ReservationService(ReservationRepository r, ResourceRepository resRepo, UserRepository uRepo) {
        this.reservationRepository = r;
        this.resourceRepository = resRepo;
        this.userRepository = uRepo;
    }

    @Transactional
    public Reservation create(String username, ReservationRequest req, boolean allowOverlapPrevention) {
        var resource = resourceRepository.findById(req.getResourceId()).orElseThrow(() -> new ResourceNotFoundException("Resource not found: " + req.getResourceId()));
        var user = userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        Instant start = Instant.parse(req.getStartTime());
        Instant end = Instant.parse(req.getEndTime());

        // optional overlap prevention
        if (allowOverlapPrevention) {
            List<Reservation> conflicts = reservationRepository.findOverlappingReservations(resource.getId(), ReservationStatus.CONFIRMED, start, end);
            if (!conflicts.isEmpty()) {
                throw new IllegalArgumentException("Time range overlaps with an existing CONFIRMED reservation.");
            }
        }

        Reservation r = new Reservation();
        r.setResource(resource);
        r.setUser(user);
        r.setPrice(req.getPrice());
        r.setStartTime(start);
        r.setEndTime(end);
        // status: if provided (admins), else default PENDING
        if (req.getStatus() != null) {
            r.setStatus(ReservationStatus.valueOf(req.getStatus()));
        } else {
            r.setStatus(ReservationStatus.PENDING);
        }
        return reservationRepository.save(r);
    }

    public Page<Reservation> list(String requestingUsername, boolean isAdmin,
                                  ReservationStatus status, BigDecimal minPrice, BigDecimal maxPrice,
                                  int page, int size, String sort) {

        Pageable pageable = createPageable(page, size, sort);
        Specification<Reservation> spec = ReservationSpecification.build(requestingUsername, isAdmin, status, minPrice, maxPrice);
        return reservationRepository.findAll(spec, pageable);
    }

    public Reservation getById(Long id, String requesterUsername, boolean isAdmin) {
        var r = reservationRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Reservation not found: " + id));
        if (!isAdmin && !r.getUser().getUsername().equals(requesterUsername)) {
            throw new SecurityException("Not authorized to access this reservation");
        }
        return r;
    }

    @Transactional
    public Reservation update(Long id, String requesterUsername, boolean isAdmin, ReservationRequest req) {
        var r = getById(id, requesterUsername, isAdmin);
        if (req.getPrice() != null) r.setPrice(req.getPrice());
        if (req.getStartTime() != null) r.setStartTime(Instant.parse(req.getStartTime()));
        if (req.getEndTime() != null) r.setEndTime(Instant.parse(req.getEndTime()));
        if (req.getStatus() != null) r.setStatus(ReservationStatus.valueOf(req.getStatus()));
        return reservationRepository.save(r);
    }

    public void delete(Long id, String requesterUsername, boolean isAdmin) {
        var r = getById(id, requesterUsername, isAdmin);
        reservationRepository.delete(r);
    }

    private Pageable createPageable(int page, int size, String sort) {
        if (sort == null || sort.isBlank()) {
            return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        }
        String[] parts = sort.split(",");
        Sort.Direction dir = parts.length > 1 && parts[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(page, size, Sort.by(dir, parts[0]));
    }
}
