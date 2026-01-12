package com.example.iam.bootstrap;

import com.example.iam.entity.*;
import com.example.iam.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final SiteRepository siteRepository;
    private final RoleDefinitionRepository roleDefinitionRepository;
    private final UserOrganizationAccessRepository userOrgAccessRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Initializing IAM Multi-Tenant Structure...");

        // 1. Create Default Organization
        Organization defaultOrg = organizationRepository.findByLegalName("Danxils Default Org")
                .orElseGet(() -> {
                    Organization org = new Organization();
                    org.setLegalName("Danxils Default Org");
                    org.setStatus(Organization.OrganizationStatus.ACTIVE);
                    return organizationRepository.save(org);
                });

        // 2. Create Default Site
        if (siteRepository.findByOrgId(defaultOrg.getOrgId()).isEmpty()) {
            Site site = new Site();
            site.setOrgId(defaultOrg.getOrgId());
            site.setSiteType(Site.SiteType.HEADQUARTERS);
            siteRepository.save(site);
        }

        // 3. Define Roles
        createRoleDefinition("admin", Set.of("ALL_ACCESS"));
        createRoleDefinition("super_user", Set.of("VIEW_DASHBOARD", "MANAGE_USERS"));
        createRoleDefinition("driver", Set.of("VIEW_TASKS", "UPDATE_STATUS"));
        createRoleDefinition("customer", Set.of("VIEW_ORDERS", "CREATE_ORDER"));

        // 4. Create/Migrate Users
        createAccountIfNotFound("demo", "demo@customer.com", "demo123", "customer", defaultOrg);
        createAccountIfNotFound("dispatcher", "dispatcher@danxils.com", "admin123", "super_user", defaultOrg);
        createAccountIfNotFound("driver", "driver@danxils.com", "driver123", "driver", defaultOrg);
        createAccountIfNotFound("cruz", "cruz@voidtracker.com", "meduza91", "admin", defaultOrg);
        createAccountIfNotFound("test_admin", "abcd@1234.123", "abcd4321", "admin", defaultOrg);

        log.info("IAM Initialization completed.");
    }

    private void createRoleDefinition(String roleId, Set<String> permissions) {
        if (!roleDefinitionRepository.existsById(roleId)) {
            RoleDefinition role = new RoleDefinition();
            role.setId(roleId);
            role.setPermissions(permissions);
            roleDefinitionRepository.save(role);
        }
    }

    private void createAccountIfNotFound(String username, String email, String password, String roleId,
            Organization org) {
        UserEntity user = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(email))
                .orElse(null);

        if (user == null) {
            log.info("Creating default user: {}", username);
            user = new UserEntity();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setEnabled(true);
            user.setGlobalStatus(UserEntity.GlobalStatus.ACTIVE);
            user = userRepository.save(user);
            log.info("User {} created successfully.", username);
        }

        // Assign Access to Organization
        UserOrgId accessId = new UserOrgId(user.getUserId(), org.getOrgId());
        if (!userOrgAccessRepository.existsById(accessId)) {
            UserOrganizationAccess access = new UserOrganizationAccess();
            access.setId(accessId);
            access.setRoleDefinitionId(roleId);
            userOrgAccessRepository.save(access);
            log.info("Assigned role {} to user {} for org {}", roleId, username, org.getLegalName());
        }
    }
}
