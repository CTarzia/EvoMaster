package com.mongo.personswithoutpost.repository;

import com.mongo.personswithoutpost.domain.Product;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;


public interface ProductRepository extends MongoRepository<Product, String>{
	Product findBy_id(ObjectId _id);
	
	List<Product> findByNameLikeIgnoreCase(String name);
}