package com.example.booking.service;

import com.example.booking.exception.ResourceNotFoundException;
import com.example.booking.model.ResourceEntity;
import com.example.booking.repository.ResourceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResourceService {
    private final ResourceRepository repo;

    public ResourceService(ResourceRepository repo) {
        this.repo = repo;
    }

    public ResourceEntity create(ResourceEntity r) {
        return repo.save(r);
    }

    public ResourceEntity update(Long id, ResourceEntity r) {
        var existing = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Resource not found: " + id));
        existing.setName(r.getName());
        existing.setDescription(r.getDescription());
        existing.setType(r.getType());
        existing.setCapacity(r.getCapacity());
        existing.setActive(r.isActive());
        return repo.save(existing);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    public ResourceEntity get(Long id) {
        return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Resource not found: " + id));
    }

    public List<ResourceEntity> list() {
        return repo.findAll();
    }
}
