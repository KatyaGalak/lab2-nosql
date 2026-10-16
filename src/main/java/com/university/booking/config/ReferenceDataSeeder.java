package com.university.booking.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.university.booking.enums.OrganizationType;
import com.university.booking.model.Category;
import com.university.booking.model.Organization;
import com.university.booking.model.OrganizationStats;
import com.university.booking.model.Person;
import com.university.booking.model.Room;
import com.university.booking.repository.CategoryRepository;
import com.university.booking.repository.OrganizationRepository;
import com.university.booking.repository.PersonRepository;
import com.university.booking.repository.RoomRepository;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class ReferenceDataSeeder implements ApplicationRunner {

    private static final String RESOURCE = "seed/reference-data.json";

    private final ObjectMapper objectMapper;
    private final RoomRepository roomRepository;
    private final CategoryRepository categoryRepository;
    private final OrganizationRepository organizationRepository;
    private final PersonRepository personRepository;

    @Override
    public void run(ApplicationArguments args) throws IOException {
        SeedData seed;
        try (InputStream in = new ClassPathResource(RESOURCE).getInputStream()) {
            seed = objectMapper.readValue(in, SeedData.class);
        }

        int inserted = 0;
        for (Room room : seed.rooms()) {
            if (!roomRepository.existsById(room.getId())) {
                roomRepository.insert(room);
                inserted++;
            }
        }
        for (Category category : seed.categories()) {
            if (!categoryRepository.existsById(category.getId())) {
                categoryRepository.insert(category);
                inserted++;
            }
        }
        for (SeedOrganization org : seed.organizations()) {
            if (!organizationRepository.existsById(org.id())) {
                organizationRepository.insert(Organization.builder()
                        .id(org.id())
                        .name(org.name())
                        .type(org.type())
                        .contact(org.contact())
                        .active(true)
                        .stats(new OrganizationStats())
                        .createdAt(LocalDateTime.now())
                        .build());
                inserted++;
            }
        }
        if (!personRepository.existsById(seed.admin().getIsuId())) {
            personRepository.insert(seed.admin());
            inserted++;
        }

        log.atInfo().addKeyValue("inserted", inserted).log("reference data seeded");
    }

    record SeedData(List<Room> rooms, List<Category> categories, List<SeedOrganization> organizations,
                    Person admin) {
    }

    record SeedOrganization(String id, String name, OrganizationType type, String contact) {
    }
}