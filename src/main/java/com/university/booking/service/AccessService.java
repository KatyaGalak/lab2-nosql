package com.university.booking.service;

import com.university.booking.enums.PersonRole;
import com.university.booking.exception.AccessDeniedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccessService {

    private final PersonLookupService personLookupService;

    public boolean isAdmin(String personId) {
        if (personId == null) {
            return false;
        }
        return personLookupService.findById(personId)
                .map(person -> person.getRole() == PersonRole.ADMIN)
                .orElse(false);
    }

    public void requireAdmin(String personId) {
        if (!isAdmin(personId)) {
            throw new AccessDeniedException("Действие доступно только администратору");
        }
    }
}
