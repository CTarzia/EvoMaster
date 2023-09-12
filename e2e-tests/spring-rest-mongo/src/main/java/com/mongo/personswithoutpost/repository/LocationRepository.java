package com.mongo.personswithoutpost.repository;

import com.mongo.personswithoutpost.domain.Location;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationRepository extends MongoRepository<Location, String> {
	
	List<Location> findByNameAndLocationNear(String sid, Point p, Distance d);
	
	List<Location> findByNameLikeIgnoreCase(String subject);
	
	List<Location> findByPositionNear(Point p, Distance d);


}
