package com.example.booking.controller;

import com.example.booking.dto.ResourceDto;
import com.example.booking.model.ResourceEntity;
import com.example.booking.response.ResourceResponse;
import com.example.booking.service.ResourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/resources")
public class ResourceController {


    private static final Logger log = LoggerFactory.getLogger(ResourceController.class);
    @Autowired
    private ResourceService service;


    @GetMapping
    public List<ResourceDto> list() {
        return service.list().stream().map(this::toDto).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResourceDto get(@PathVariable Long id) {
        return toDto(service.get(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<ResourceResponse> create(@RequestBody ResourceEntity r) {
        ResourceResponse response = new ResourceResponse();
        try {
            ResourceDto dto = toDto(service.create(r));
            log.info("Resource created successfully: {}", dto.getName());
            response.setMessage("Resource created successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error while creating resource", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResourceDto update(@PathVariable Long id, @RequestBody ResourceEntity r) {
        return toDto(service.update(id, r));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    private ResourceDto toDto(ResourceEntity r) {
        ResourceDto d = new ResourceDto();
        d.setId(r.getId());
        d.setName(r.getName());
        d.setType(r.getType());
        d.setDescription(r.getDescription());
        d.setCapacity(r.getCapacity());
        d.setActive(r.isActive());
        return d;
    }
}
