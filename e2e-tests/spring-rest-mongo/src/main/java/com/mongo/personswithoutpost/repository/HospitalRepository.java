package com.mongo.personswithoutpost.repository;

import com.mongo.personswithoutpost.domain.Hospital;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HospitalRepository extends MongoRepository<Hospital, String>{
	List<Hospital> findByNameLikeIgnoreCase(String name);

//	List<Hospital> findByPositionNearAndAvailableBeds(Point p, int beds);
}