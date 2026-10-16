package com.university.booking.web;

import com.university.booking.service.AccessService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AdminOnlyInterceptor implements HandlerInterceptor {

    private final AccessService accessService;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) {

        if (handler instanceof HandlerMethod method && method.hasMethodAnnotation(AdminOnly.class)) {
            accessService.requireAdmin((String) request.getAttribute(PersonAuthInterceptor.PERSON_ID_ATTRIBUTE));
        }
        return true;
    }
}
