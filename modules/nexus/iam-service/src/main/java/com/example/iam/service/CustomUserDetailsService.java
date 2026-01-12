package com.example.iam.service;

import com.example.iam.entity.UserOrganizationAccess;
import com.example.iam.repository.UserOrganizationAccessRepository;
import com.example.iam.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
        private final UserRepository userRepository;
        private final UserOrganizationAccessRepository userOrgAccessRepository;

        @Override
        @Transactional(readOnly = true)
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                return userRepository.findByUsername(username)
                                .map(user -> {
                                        // Fetch all organization roles for the user
                                        List<UserOrganizationAccess> accessList = userOrgAccessRepository
                                                        .findByIdUserId(user.getUserId());

                                        // Map roles to authorities (e.g., "ROLE_admin")
                                        // Note: This flattens roles across all organizations, which is a simplification
                                        // for the UserDetails object.
                                        // The actual context-aware check happens in JwtAuthFilter or method security.
                                        List<SimpleGrantedAuthority> authorities = accessList.stream()
                                                        .map(access -> new SimpleGrantedAuthority(
                                                                        "ROLE_" + access.getRoleDefinitionId()
                                                                                        .toUpperCase()))
                                                        .collect(Collectors.toList());

                                        // Ensure the roles from UserEntity are also present (e.g., ROLE_ADMIN)
                                        if (user.getRoles() != null) {
                                                for (com.example.iam.entity.UserRole r : user.getRoles()) {
                                                        SimpleGrantedAuthority roleAuthority = new SimpleGrantedAuthority(
                                                                        r.name());
                                                        if (!authorities.contains(roleAuthority)) {
                                                                authorities.add(roleAuthority);
                                                        }
                                                }
                                        }

                                        return new User(
                                                        user.getUsername(),
                                                        user.getPassword(),
                                                        user.isEnabled(),
                                                        true, true, true,
                                                        authorities);
                                })
                                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        }
}