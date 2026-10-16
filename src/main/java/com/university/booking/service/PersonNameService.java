package com.university.booking.service;

import com.university.booking.model.Booking;
import com.university.booking.model.Comment;
import com.university.booking.model.HistoryEntry;
import com.university.booking.model.Person;
import com.university.booking.repository.PersonRepository;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PersonNameService {

    private final PersonRepository personRepository;

    public Booking withNames(Booking booking) {
        withNames(List.of(booking));
        return booking;
    }

    public <C extends Collection<Booking>> C withNames(C bookings) {
        Set<String> ids = bookings.stream()
                .flatMap(b -> participants(b).stream())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (ids.isEmpty()) {
            return bookings;
        }

        Map<String, String> names = new HashMap<>();
        for (Person person : personRepository.findAllById(ids)) {
            names.put(person.getIsuId(), fullName(person));
        }

        for (Booking booking : bookings) {
            Map<String, String> own = new LinkedHashMap<>();
            for (String id : participants(booking)) {
                String name = names.get(id);
                if (name != null) {
                    own.put(id, name);
                }
            }
            booking.setPersonNames(own);
        }
        return bookings;
    }

    private static Set<String> participants(Booking booking) {
        Set<String> ids = new LinkedHashSet<>();
        ids.add(booking.getPersonId());
        ids.add(booking.getProcessedBy());
        if (booking.getComments() != null) {
            booking.getComments().stream().map(Comment::getAuthorId).forEach(ids::add);
        }
        if (booking.getHistory() != null) {
            booking.getHistory().stream().map(HistoryEntry::getAuthor).forEach(ids::add);
        }
        ids.removeIf(Objects::isNull);
        return ids;
    }

    private static String fullName(Person person) {
        return Stream.of(person.getLastName(), person.getFirstName(), person.getMiddleName())
                .filter(part -> part != null && !part.isBlank())
                .collect(Collectors.joining(" "));
    }
}