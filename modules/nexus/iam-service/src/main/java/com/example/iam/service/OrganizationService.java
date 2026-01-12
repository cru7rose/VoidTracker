package com.example.iam.service;

import com.example.iam.entity.Organization;
import com.example.iam.entity.Site;
import com.example.iam.repository.OrganizationRepository;
import com.example.iam.repository.SiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final SiteRepository siteRepository;

    public List<Organization> getAllOrganizations() {
        return organizationRepository.findAll();
    }

    public Organization getOrganization(UUID orgId) {
        return organizationRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));
    }

    @Transactional
    public Organization createOrganization(Organization organization) {
        return organizationRepository.save(organization);
    }

    @Transactional
    public Organization updateOrganization(UUID orgId, Organization updates) {
        Organization org = getOrganization(orgId);
        org.setLegalName(updates.getLegalName());
        org.setVatId(updates.getVatId());
        org.setStatus(updates.getStatus());
        org.setConfiguration(updates.getConfiguration());
        org.setBillingConfig(updates.getBillingConfig());
        return organizationRepository.save(org);
    }

    public List<Site> getSitesForOrganization(UUID orgId) {
        return siteRepository.findByOrgId(orgId);
    }

    @Transactional
    public Site createSite(UUID orgId, Site site) {
        site.setOrgId(orgId);
        return siteRepository.save(site);
    }
}
