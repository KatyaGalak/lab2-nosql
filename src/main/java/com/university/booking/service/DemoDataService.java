package com.university.booking.service;

import com.university.booking.enums.BookingStatus;
import com.university.booking.enums.PersonRole;
import com.university.booking.model.Booking;
import com.university.booking.model.Category;
import com.university.booking.model.Comment;
import com.university.booking.model.HistoryEntry;
import com.university.booking.model.Organization;
import com.university.booking.model.Person;
import com.university.booking.model.Room;
import com.university.booking.repository.CategoryRepository;
import com.university.booking.repository.OrganizationRepository;
import com.university.booking.repository.PersonRepository;
import com.university.booking.repository.RoomRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DemoDataService {

    private static final int DEMO_PERSONS = 500;
    private static final int FIRST_DEMO_ID = 200000;
    private static final int BATCH = 5000;
    private static final String ADMIN_ID = "100000";
    private static final List<String> COMMENTS = List.of(
            "Нужен проектор", "Можно ли перенести на час позже?", "Будут гости из другого вуза",
            "Нужна стойка для регистрации", "Подтверждаю участие", "Просьба открыть аудиторию заранее");

    private final MongoTemplate mongoTemplate;
    private final PersonRepository personRepository;
    private final RoomRepository roomRepository;
    private final CategoryRepository categoryRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationService organizationService;

    public Map<String, Object> generate(int count) {
        Random random = new Random();
        List<String> personIds = ensureDemoPersons();
        List<Room> rooms = roomRepository.findAll();
        List<String> categories = categoryRepository.findAll().stream().map(Category::getId).toList();
        List<String> organizations = organizationRepository.findAll().stream().map(Organization::getId).toList();

        Set<String> approvedSlots = new HashSet<>();
        List<Booking> batch = new ArrayList<>(BATCH);
        for (int i = 0; i < count; i++) {
            batch.add(randomBooking(random, personIds, rooms, categories, organizations, approvedSlots));
            if (batch.size() == BATCH) {
                mongoTemplate.insert(batch, Booking.class);
                batch.clear();
            }
        }
        if (!batch.isEmpty()) {
            mongoTemplate.insert(batch, Booking.class);
        }

        organizationService.recalculateStats();
        log.atInfo().addKeyValue("count", count).log("demo bookings generated");
        return Map.of("inserted", count, "demoPersons", personIds.size(),
                "examplePersonId", personIds.get(0));
    }

    private List<String> ensureDemoPersons() {
        List<String> ids = new ArrayList<>();
        List<Person> missing = new ArrayList<>();
        for (int i = 0; i < DEMO_PERSONS; i++) {
            String id = String.valueOf(FIRST_DEMO_ID + i);
            ids.add(id);
            if (!personRepository.existsById(id)) {
                missing.add(Person.builder()
                        .isuId(id)
                        .lastName("Демо")
                        .firstName("Пользователь")
                        .middleName(String.valueOf(i))
                        .role(i % 10 == 0 ? PersonRole.TEACHER : PersonRole.STUDENT)
                        .build());
            }
        }
        if (!missing.isEmpty()) {
            personRepository.insert(missing);
        }
        return ids;
    }

    private Booking randomBooking(Random random, List<String> personIds, List<Room> rooms, List<String> categories,
                                  List<String> organizations, Set<String> approvedSlots) {
        String personId = personIds.get(random.nextInt(personIds.size()));
        Room room = rooms.get(random.nextInt(rooms.size()));

        LocalDate date = LocalDate.now().minusDays(1 + random.nextInt(365));
        int hour = 8 + random.nextInt(12);
        LocalDateTime createdAt = date.atStartOfDay().minusDays(1 + random.nextInt(30)).plusMinutes(random.nextInt(1440));
        String organizationId = random.nextInt(10) < 3 ? null : organizations.get(random.nextInt(organizations.size()));

        BookingStatus status = BookingStatus.values()[random.nextInt(BookingStatus.values().length)];
        if (status == BookingStatus.APPROVED && !approvedSlots.add(room.getId() + "|" + date + "|" + hour)) {
            status = BookingStatus.REJECTED;
        }

        List<HistoryEntry> history = new ArrayList<>();
        history.add(new HistoryEntry(BookingStatus.DRAFT, createdAt, personId, "Черновик создан"));
        if (status != BookingStatus.DRAFT) {
            history.add(new HistoryEntry(BookingStatus.UNDER_REVIEW, createdAt.plusMinutes(5), personId,
                    "Отправлена на рассмотрение"));
        }
        LocalDateTime processedAt = null;
        if (status == BookingStatus.APPROVED || status == BookingStatus.REJECTED) {
            processedAt = createdAt.plusHours(1 + random.nextInt(48));
            history.add(new HistoryEntry(status, processedAt, ADMIN_ID,
                    status == BookingStatus.APPROVED ? "Одобрено администратором" : "Помещение занято"));
        }

        List<Comment> comments = new ArrayList<>();
        for (int c = random.nextInt(3); c > 0; c--) {
            String author = random.nextBoolean() ? personId : ADMIN_ID;
            comments.add(new Comment(UUID.randomUUID().toString(), author,
                    COMMENTS.get(random.nextInt(COMMENTS.size())), createdAt.plusMinutes(10 + c)));
        }

        return Booking.builder()
                .id(UUID.randomUUID().toString())
                .personId(personId)
                .organizationId(organizationId)
                .roomId(room.getId())
                .categoryId(categories.get(random.nextInt(categories.size())))
                .eventName("Демо мероприятие " + random.nextInt(10000))
                .eventDate(date)
                .startTime(LocalTime.of(hour, 0))
                .endTime(LocalTime.of(hour + 1, 0))
                .participantCount(1 + random.nextInt(Math.max(1, room.getCapacity())))
                .contactPhone("+7900" + (1000000 + random.nextInt(9000000)))
                .status(status)
                .processedBy(processedAt != null ? ADMIN_ID : null)
                .createdAt(createdAt)
                .processedAt(processedAt)
                .comments(comments)
                .history(history)
                .build();
    }
}