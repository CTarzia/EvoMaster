package com.mongo.personswithoutpost.resource;


import com.mongo.personswithoutpost.constant.Constant;
import com.mongo.personswithoutpost.domain.Hospital;
import com.mongo.personswithoutpost.domain.Patient;
import com.mongo.personswithoutpost.resource.exception.ResourceNotFoundException;
import com.mongo.personswithoutpost.services.HospitalService;
import com.mongo.personswithoutpost.services.PatientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.regex.Pattern;

@CrossOrigin("http://localhost:4200") // permissão para o Angular
@RestController
@RequestMapping(path = Constant.V1Path)
public class PatientResource {
	private static final Logger LOGGER = LoggerFactory.getLogger(PatientResource.class);

	@Autowired
	private PatientService service;

	@Autowired
	private HospitalService hospitalService;

/*
	@GetMapping(path = "pacientes")
	public ResponseEntity<List<Patient>> findPatients(@PathVariable String hospital_id) {
		try {
			Hospital obj = hospitalService.findById(hospital_id);
			List<Patient> patientList = obj.getPatients();
			if (patientList != null) {
				return ResponseEntity.ok(patientList);
			}
			throw new ResourceNotFoundException("Hospital sem pacientes!");
		} catch (Exception e) {
			//LOGGER.error("findPatients - Error with message: {}", e.getMessage());
			return ResponseEntity.notFound().build();
		}
	}

 */

	@GetMapping(path = "pacientes/{patientId}")
	public ResponseEntity<Patient> findPatientById(@PathVariable String hospital_id, @PathVariable String patientId) {
		try {
			System.out.println("About to execute findById in findPatientById with id value: " + patientId);
			Patient patient = service.findById(patientId);
			return ResponseEntity.ok().body(patient);
		} catch (Exception e) {
			LOGGER.error("findPatientById - Error with message: {}", e.getMessage());
			return ResponseEntity.notFound().build();
		}
	}

/*

	@PostMapping(path = "pacientes/checkin", produces = "application/json")
	public ResponseEntity<Patient> checkinPacient(@PathVariable("hospital_id") String idHospital, @RequestBody Patient patient) {
		try {
			Hospital hospital = hospitalService.findById(idHospital);
			return ResponseEntity.ok(hospitalService.checkIn(hospital, patient));
		} catch (Exception e) {
			//LOGGER.error("checkinPacient - Error with message: {}", e.getMessage());
			return ResponseEntity.notFound().build();
		}		
	}

	@PostMapping(path = "pacientes/checkout", produces = "application/json")
	public ResponseEntity<Patient> checkoutPacient(@PathVariable("hospital_id") String idHospital, @RequestBody String idPatient) {
		try {
			Hospital hospital = hospitalService.findById(idHospital);
			return ResponseEntity.ok(hospitalService.checkOut(hospital, idPatient));
		} catch (Exception e) {
			//LOGGER.error("checkoutPacient - Error with message: {}", e.getMessage());
			return ResponseEntity.notFound().build();
		}		
	}

	@PutMapping(path = "pacientes/{patientId}")
	public ResponseEntity<Patient> updatePatient(@PathVariable("hospital_id") String idHospital, @PathVariable String patientId,
			@RequestBody Patient patient) {
		try {
			Patient p = service.findById(patientId);
			p.setName(patient.getName());
			p.setCpf(patient.getCpf());
			p.setBirthDate(patient.getBirthDate());
			p.setGender(patient.getGender());
			return ResponseEntity.ok(service.update(p));
		} catch (Exception e) {
			//LOGGER.error("updatePatient - Error with message: {}", e.getMessage());
			return ResponseEntity.notFound().build();
		}
	}

 */
}





