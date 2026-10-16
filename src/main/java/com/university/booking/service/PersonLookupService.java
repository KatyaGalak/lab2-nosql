package com.university.booking.service;

import com.university.booking.model.Person;
import com.university.booking.repository.PersonRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PersonLookupService {

    public static final String CACHE_NAME = "persons";

    private final PersonRepository personRepository;

    @Cacheable(cacheNames = CACHE_NAME, key = "#p0", unless = "#result == null")
    public Optional<Person> findById(String id) {
        return personRepository.findById(id);
    }
}
