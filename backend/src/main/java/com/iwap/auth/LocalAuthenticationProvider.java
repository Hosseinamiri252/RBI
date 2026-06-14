package com.iwap.auth;

import com.iwap.user.User;
import com.iwap.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LocalAuthenticationProvider implements AuthenticationProvider {

    @Autowired private UserService userService;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        User user = userService.findByUsername(username).orElse(null);
        if (user == null || user.getAuthSource() != com.iwap.user.AuthSource.LOCAL) {
            throw new BadCredentialsException("Invalid credentials");
        }
        if (!user.isEnabled()) {
            throw new DisabledException("Account is disabled");
        }
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        userService.updateLastLogin(user.getId());

        return new UsernamePasswordAuthenticationToken(
            user, password,
            List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
