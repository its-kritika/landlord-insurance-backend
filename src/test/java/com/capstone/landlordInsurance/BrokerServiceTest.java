package com.capstone.landlordInsurance;

import com.capstone.landlordInsurance.entity.Broker;
import com.capstone.landlordInsurance.repository.BrokerRepository;
import com.capstone.landlordInsurance.repository.ClientRepository;
import com.capstone.landlordInsurance.repository.QuoteRepository;
import com.capstone.landlordInsurance.service.BrokerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BrokerServiceTest {

	@Mock
	private BrokerRepository brokerRepository;

	@Mock
	private ClientRepository clientRepository;

	@Mock
	private QuoteRepository quoteRepository;

	@InjectMocks
	private BrokerService brokerService;

	private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	@Test
	void saveBroker_ShouldEncodePasswordAndSave() {
		// Arrange
		Broker broker = new Broker();
		broker.setEmail("test@example.com");
		broker.setPassword("plainPassword");

		when(brokerRepository.save(any(Broker.class))).thenAnswer(invocation -> invocation.getArgument(0));

		// Act
		Broker savedBroker = brokerService.saveBroker(broker);

		// Assert
		assertNotNull(savedBroker);
		assertNotEquals("plainPassword", savedBroker.getPassword());
		assertTrue(passwordEncoder.matches("plainPassword", savedBroker.getPassword()));
		verify(brokerRepository, times(1)).save(broker);
	}

	@Test
	void findByEmail_ShouldReturnBroker() {
		// Arrange
		String email = "test@example.com";
		Broker expectedBroker = new Broker();
		expectedBroker.setEmail(email);

		when(brokerRepository.findByEmail(email)).thenReturn(expectedBroker);

		// Act
		Broker result = brokerService.findByEmail(email);

		// Assert
		assertNotNull(result);
		assertEquals(email, result.getEmail());
		verify(brokerRepository, times(1)).findByEmail(email);
	}

	@Test
	void updateBroker_ShouldUpdateNameAndPassword() {
		// Arrange
		Long id = 1L;
		Broker existingBroker = new Broker();
		existingBroker.setBrokerId(id);
		existingBroker.setName("Old Name");
		existingBroker.setEmail("old@example.com");
		existingBroker.setPassword(passwordEncoder.encode("oldPassword"));

		Broker updatedBroker = new Broker();
		updatedBroker.setName("New Name");
		updatedBroker.setPassword("newPassword");

		when(brokerRepository.findById(id)).thenReturn(Optional.of(existingBroker));
		when(brokerRepository.save(any(Broker.class))).thenAnswer(invocation -> invocation.getArgument(0));

		// Act
		Broker result = brokerService.updateBroker(id, updatedBroker);

		// Assert
		assertNotNull(result);
		assertEquals("New Name", result.getName());
		assertTrue(passwordEncoder.matches("newPassword", result.getPassword()));
		assertEquals("old@example.com", result.getEmail()); // Email shouldn't change
		verify(brokerRepository, times(1)).findById(id);
		verify(brokerRepository, times(1)).save(existingBroker);
	}

	@Test
	void updateBroker_WhenEmailChanged_ShouldThrowException() {
		// Arrange
		Long id = 1L;
		Broker existingBroker = new Broker();
		existingBroker.setBrokerId(id);
		existingBroker.setEmail("old@example.com");

		Broker updatedBroker = new Broker();
		updatedBroker.setEmail("new@example.com");

		when(brokerRepository.findById(id)).thenReturn(Optional.of(existingBroker));

		// Act & Assert
		assertThrows(IllegalArgumentException.class, () -> {
			brokerService.updateBroker(id, updatedBroker);
		});
		verify(brokerRepository, never()).save(any());
	}

	@Test
	void updateBroker_WhenBrokerNotFound_ShouldThrowException() {
		// Arrange
		Long id = 1L;
		Broker updatedBroker = new Broker();

		when(brokerRepository.findById(id)).thenReturn(Optional.empty());

		// Act & Assert
		assertThrows(NoSuchElementException.class, () -> {
			brokerService.updateBroker(id, updatedBroker);
		});
		verify(brokerRepository, never()).save(any());
	}

	@Test
	void updateBroker_WhenOnlyNameChanged_ShouldUpdateOnlyName() {
		// Arrange
		Long id = 1L;
		Broker existingBroker = new Broker();
		existingBroker.setBrokerId(id);
		existingBroker.setName("Old Name");
		existingBroker.setEmail("test@example.com");
		existingBroker.setPassword(passwordEncoder.encode("oldPassword"));

		Broker updatedBroker = new Broker();
		updatedBroker.setName("New Name");

		when(brokerRepository.findById(id)).thenReturn(Optional.of(existingBroker));
		when(brokerRepository.save(any(Broker.class))).thenAnswer(invocation -> invocation.getArgument(0));

		// Act
		Broker result = brokerService.updateBroker(id, updatedBroker);

		// Assert
		assertNotNull(result);
		assertEquals("New Name", result.getName());
		assertEquals("test@example.com", result.getEmail());
		assertTrue(passwordEncoder.matches("oldPassword", result.getPassword())); // Password unchanged
	}

	@Test
	void updateBroker_WhenOnlyPasswordChanged_ShouldUpdateOnlyPassword() {
		// Arrange
		Long id = 1L;
		Broker existingBroker = new Broker();
		existingBroker.setBrokerId(id);
		existingBroker.setName("Existing Name");
		existingBroker.setEmail("test@example.com");
		existingBroker.setPassword(passwordEncoder.encode("oldPassword"));

		Broker updatedBroker = new Broker();
		updatedBroker.setPassword("newPassword");

		when(brokerRepository.findById(id)).thenReturn(Optional.of(existingBroker));
		when(brokerRepository.save(any(Broker.class))).thenAnswer(invocation -> invocation.getArgument(0));

		// Act
		Broker result = brokerService.updateBroker(id, updatedBroker);

		// Assert
		assertNotNull(result);
		assertEquals("Existing Name", result.getName()); // Name unchanged
		assertEquals("test@example.com", result.getEmail()); // Email unchanged
		assertTrue(passwordEncoder.matches("newPassword", result.getPassword()));
	}
}
