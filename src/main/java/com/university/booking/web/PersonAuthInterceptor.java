package com.university.booking.web;

import com.university.booking.service.PersonLookupService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class PersonAuthInterceptor implements HandlerInterceptor {

    public static final String PERSON_ID_HEADER = "Person-Id";

    public static final String PERSON_ID_ATTRIBUTE = "currentPersonId";

    private final PersonLookupService personLookupService;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) {
        String personId = request.getHeader(PERSON_ID_HEADER);
        if (personId != null && !personId.isBlank() && personLookupService.findById(personId).isPresent()) {
            request.setAttribute(PERSON_ID_ATTRIBUTE, personId);
            return true;
        }

        if (isSelfRegistration(request)) {
            return true;
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Registered person required");
    }

    private boolean isSelfRegistration(HttpServletRequest request) {
        String path = request.getRequestURI();
        return HttpMethod.POST.matches(request.getMethod())
                && (path.equals("/api/persons") || path.equals("/api/persons/"));
    }
}
