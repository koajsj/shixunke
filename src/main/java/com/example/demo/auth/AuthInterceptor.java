package com.example.demo.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RequireRole requireRole = findRequiredRole(handlerMethod);
        if (requireRole == null) {
            return true;
        }

        HttpSession session = request.getSession(false);
        AuthSession authSession = session == null ? null : (AuthSession) session.getAttribute(AuthService.SESSION_KEY);
        if (authSession == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "Please log in first.");
        }

        if (!hasAnyRole(authSession, requireRole.value())) {
            throw new ResponseStatusException(FORBIDDEN, "You do not have permission to perform this action.");
        }
        return true;
    }

    private RequireRole findRequiredRole(HandlerMethod handlerMethod) {
        RequireRole methodAnnotation = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (methodAnnotation != null) {
            return methodAnnotation;
        }
        return handlerMethod.getBeanType().getAnnotation(RequireRole.class);
    }

    private boolean hasAnyRole(AuthSession authSession, AppRole[] allowedRoles) {
        for (AppRole allowedRole : allowedRoles) {
            if (authSession.role() == allowedRole) {
                return true;
            }
        }
        return false;
    }
}
