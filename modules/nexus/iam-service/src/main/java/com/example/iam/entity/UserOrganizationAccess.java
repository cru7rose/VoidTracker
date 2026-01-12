package com.example.iam.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "iam_user_org_access")
@Data
@NoArgsConstructor
public class UserOrganizationAccess {

    @EmbeddedId
    private UserOrgId id;

    @Column(name = "role_definition_id")
    private String roleDefinitionId;

    @Column(name = "user_config_overrides", columnDefinition = "jsonb")
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    private String configOverrides;
}
