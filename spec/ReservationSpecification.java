package com.example.booking.spec;

import com.example.booking.model.Reservation;
import com.example.booking.model.ReservationStatus;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.*;
import java.math.BigDecimal;

public class ReservationSpecification {
    public static Specification<Reservation> build(String requestingUsername, boolean isAdmin,
                                                   ReservationStatus status, BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, cb) -> {
            Predicate p = cb.conjunction();
            if (!isAdmin && requestingUsername != null) {
                // only own reservations
                p = cb.and(p, cb.equal(root.get("user").get("username"), requestingUsername));
            }
            if (status != null) {
                p = cb.and(p, cb.equal(root.get("status"), status));
            }
            if (minPrice != null) {
                p = cb.and(p, cb.ge(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                p = cb.and(p, cb.le(root.get("price"), maxPrice));
            }
            return p;
        };
    }
}
