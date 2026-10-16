package com.university.booking.controller;

import com.university.booking.dto.OrganizationRequest;
import com.university.booking.model.Organization;
import com.university.booking.service.OrganizationService;
import com.university.booking.web.AdminOnly;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/organizations")
@AllArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @GetMapping
    public ResponseEntity<List<Organization>> getAll() {
        return ResponseEntity.ok(organizationService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Organization> getById(@PathVariable("id") String id) {
        return ResponseEntity.ok(organizationService.get(id));
    }

    @AdminOnly
    @PostMapping
    public ResponseEntity<Organization> create(@Valid @RequestBody OrganizationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(organizationService.create(request));
    }

    @AdminOnly
    @PutMapping("/{id}")
    public ResponseEntity<Organization> update(@PathVariable("id") String id,
                                               @Valid @RequestBody OrganizationRequest request) {
        return ResponseEntity.ok(organizationService.update(id, request));
    }

    @AdminOnly
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") String id) {
        organizationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}