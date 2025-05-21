package com.dapm.security_service.security;

import com.dapm.security_service.models.User;
import com.dapm.security_service.repositories.OrgPermissionRepository;
import com.dapm.security_service.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final OrgPermissionRepository orgPermissionRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        var permissions = orgPermissionRepository.findByOrgRole(user.getOrgRole());

        var authorities = permissions.stream()
                .map(p -> new SimpleGrantedAuthority(p.getAction().name()))
                .collect(Collectors.toSet());

        return new CustomUserDetails(user, authorities);
    }
}

