package org.is.bandmanager.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.is.bandmanager.dto.PersonDto;
import org.is.bandmanager.dto.request.PersonRequest;
import org.is.bandmanager.service.PersonService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/persons")
@RequiredArgsConstructor
public class PersonController {

	private final PersonService personService;

	@GetMapping
	public ResponseEntity<List<PersonDto>> getAllPersons() {
		return ResponseEntity.ok(personService.getAll());
	}

	@GetMapping("/{id}")
	public ResponseEntity<PersonDto> getPerson(@PathVariable Long id) {
		PersonDto person = personService.get(id);
		return ResponseEntity.ok(person);
	}

	@PostMapping
	public ResponseEntity<PersonDto> createPerson(@Valid @RequestBody PersonRequest request) {
		PersonDto createdPerson = personService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(createdPerson);
	}

	@PutMapping("/{id}")
	public ResponseEntity<PersonDto> updatePerson(@PathVariable Long id, @Valid @RequestBody PersonRequest request) {
		PersonDto updatedPerson = personService.update(id, request);
		return ResponseEntity.ok(updatedPerson);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<PersonDto> deletePerson(@PathVariable Long id) {
		PersonDto deletedPerson = personService.delete(id);
		return ResponseEntity.ok(deletedPerson);
	}

}
