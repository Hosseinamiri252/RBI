package com.iwap.session;

import com.iwap.auth.JwtTokenProvider;
import com.iwap.user.User;
import com.iwap.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.GuacamoleSecurityException;
import org.apache.guacamole.net.GuacamoleTunnel;
import org.apache.guacamole.servlet.GuacamoleHTTPTunnelServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/tunnel")
public class GuacamoleTunnelController extends GuacamoleHTTPTunnelServlet {

    @Autowired private SessionService sessionService;
    @Autowired private JwtTokenProvider jwtTokenProvider;
    @Autowired private UserService userService;

    @Override
    protected GuacamoleTunnel doConnect(HttpServletRequest request) throws GuacamoleException {
        String token = request.getParameter("token");
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            throw new GuacamoleSecurityException("Invalid or missing token");
        }

        UUID userId = jwtTokenProvider.getUserIdFromToken(token);
        User user = userService.findById(userId)
            .orElseThrow(() -> new GuacamoleSecurityException("User not found"));

        UUID sessionId = UUID.fromString(request.getParameter("sessionId"));
        BrowserSession session = sessionService.getSessionForUser(sessionId, user);

        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new GuacamoleSecurityException("Session is not active");
        }

        return sessionService.connectTunnel(session);
    }

    @Override
    protected void handleTunnelRequest(HttpServletRequest request, HttpServletResponse response)
            throws GuacamoleException {
        super.handleTunnelRequest(request, response);
    }
}
