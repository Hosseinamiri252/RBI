package com.iwap.auth;

import com.iwap.user.AuthSource;
import com.iwap.user.User;
import com.iwap.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;
import java.util.Hashtable;
import java.util.List;

@Component
@ConditionalOnProperty(name = "auth.ldap.enabled", havingValue = "true")
public class LdapAuthenticationProvider implements AuthenticationProvider {

    @Value("${auth.ldap.url}") private String ldapUrl;
    @Value("${auth.ldap.base-dn}") private String baseDn;
    @Value("${auth.ldap.user-search-filter}") private String searchFilter;

    @Autowired private UserService userService;

    @Override
    public Authentication authenticate(Authentication authentication) {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        try {
            Hashtable<String, String> env = new Hashtable<>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(Context.PROVIDER_URL, ldapUrl);
            env.put(Context.SECURITY_AUTHENTICATION, "simple");
            env.put(Context.SECURITY_PRINCIPAL, "CN=" + username + "," + baseDn);
            env.put(Context.SECURITY_CREDENTIALS, password);
            new InitialDirContext(env).close();

            User user = userService.syncLdapUser(username);
            userService.updateLastLogin(user.getId());

            return new UsernamePasswordAuthenticationToken(
                user, password,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
            );
        } catch (NamingException e) {
            return null;
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
