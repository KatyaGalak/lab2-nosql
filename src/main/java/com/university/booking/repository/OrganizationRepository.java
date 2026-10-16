package com.university.booking.repository;

import com.university.booking.model.Organization;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrganizationRepository extends MongoRepository<Organization, String> {

    List<Organization> findAllByOrderByNameAsc();
}