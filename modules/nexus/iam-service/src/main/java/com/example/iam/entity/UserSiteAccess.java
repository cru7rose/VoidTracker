package com.example.iam.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Entity
@Table(name = "iam_user_site_access")
@Data
@NoArgsConstructor
public class UserSiteAccess {

    @EmbeddedId
    private UserSiteId id;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "iam_user_site_permissions", joinColumns = {
            @JoinColumn(name = "user_id", referencedColumnName = "userId"),
            @JoinColumn(name = "site_id", referencedColumnName = "siteId")
    })
    @Column(name = "permission")
    private Set<String> fineGrainedPermissions;

    @Enumerated(EnumType.STRING)
    private SiteRole role;

    public enum SiteRole {
        SITE_MANAGER, SITE_STAFF
    }
}
