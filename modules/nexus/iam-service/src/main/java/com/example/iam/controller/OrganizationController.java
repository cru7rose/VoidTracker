package com.example.iam.controller;

import com.example.iam.entity.Organization;
import com.example.iam.entity.Site;
import com.example.iam.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @GetMapping
    public ResponseEntity<List<Organization>> getAllOrganizations() {
        return ResponseEntity.ok(organizationService.getAllOrganizations());
    }

    @PostMapping
    public ResponseEntity<Organization> createOrganization(@RequestBody Organization organization) {
        return ResponseEntity.ok(organizationService.createOrganization(organization));
    }

    @GetMapping("/{orgId}")
    public ResponseEntity<Organization> getOrganization(@PathVariable UUID orgId) {
        return ResponseEntity.ok(organizationService.getOrganization(orgId));
    }

    @PutMapping("/{orgId}")
    public ResponseEntity<Organization> updateOrganization(@PathVariable UUID orgId,
            @RequestBody Organization organization) {
        return ResponseEntity.ok(organizationService.updateOrganization(orgId, organization));
    }

    @GetMapping("/{orgId}/sites")
    public ResponseEntity<List<Site>> getSites(@PathVariable UUID orgId) {
        return ResponseEntity.ok(organizationService.getSitesForOrganization(orgId));
    }

    @PostMapping("/{orgId}/sites")
    public ResponseEntity<Site> createSite(@PathVariable UUID orgId, @RequestBody Site site) {
        return ResponseEntity.ok(organizationService.createSite(orgId, site));
    }
}
