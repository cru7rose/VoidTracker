package com.example.iam.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Entity
@Table(name = "iam_role_definitions")
@Data
@NoArgsConstructor
public class RoleDefinition {

    @Id
    private String id; // e.g., "logistics_manager"

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "iam_role_permissions", joinColumns = @JoinColumn(name = "role_id"))
    @Column(name = "permission")
    private Set<String> permissions;
}
