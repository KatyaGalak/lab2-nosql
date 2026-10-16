package com.university.booking.controller;

import com.university.booking.dto.PersonRequest;
import com.university.booking.model.Person;
import com.university.booking.service.PersonService;
import com.university.booking.web.AdminOnly;
import com.university.booking.web.PersonAuthInterceptor;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/persons")
@AllArgsConstructor
public class PersonController {

    private final PersonService personService;

    @PostMapping
    public ResponseEntity<Person> create(
            @RequestAttribute(name = PersonAuthInterceptor.PERSON_ID_ATTRIBUTE, required = false) String currentPersonId,
            @Valid @RequestBody PersonRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(personService.createPerson(request, currentPersonId));
    }

    @AdminOnly
    @GetMapping
    public ResponseEntity<List<Person>> getAll() {
        return ResponseEntity.ok(personService.getAllPersons());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Person> getById(@RequestAttribute(PersonAuthInterceptor.PERSON_ID_ATTRIBUTE) String currentPersonId,
                                          @PathVariable("id") String id) {
        return ResponseEntity.ok(personService.getPerson(id, currentPersonId));
    }

    @AdminOnly
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") String id) {
        personService.deletePerson(id);
        return ResponseEntity.noContent().build();
    }
}
