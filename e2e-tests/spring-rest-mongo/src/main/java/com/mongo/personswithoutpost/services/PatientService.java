package com.mongo.personswithoutpost.services;

import com.mongo.personswithoutpost.domain.Location;
import com.mongo.personswithoutpost.domain.Patient;
import com.mongo.personswithoutpost.repository.PatientRepository;
import com.mongo.personswithoutpost.services.exception.ObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class PatientService {

	@Autowired
	private PatientRepository repo;
	
	public Patient findById(String id) {
		//repo.save(new Patient("ecb296acabdb3d3b3987c18b", "", "", new Date(3), "", new Date(3), new Location("", new GeoJsonPoint(1.0, 2.0))));
		Optional<Patient> obj = repo.findById(id);
		return obj.orElseThrow(() -> new ObjectNotFoundException("Paciente não encontrado!"));
	}
	public Patient update(Patient obj) {
		return repo.save(obj);
	}
}