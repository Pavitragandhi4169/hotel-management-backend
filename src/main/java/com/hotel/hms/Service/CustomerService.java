
package com.hotel.hms.Service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotel.hms.Entity.Customer;
import com.hotel.hms.Exception.ResourceNotFoundException;
import com.hotel.hms.Repository.CustomerRepository;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Customer getCustomerById(Integer id) {
        return customerRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found"));
    }

    public Customer saveCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    public void deleteCustomer(Integer id) {
        Customer customer = getCustomerById(id);
        customerRepository.delete(customer);
    }

    public Customer findByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Customer email is required");
        }

        return customerRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer profile not found"));
    }

    @Transactional
    public Customer findOrCreateGuestProfile(
            String email,
            String name) {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "User email is required");
        }

        return customerRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Complete your guest profile before booking."));
    }

    @Transactional
    public Customer createGuestProfile(
            String authenticatedEmail,
            String name,
            String phone,
            String address) {

        if (authenticatedEmail == null
                || authenticatedEmail.isBlank()) {
            throw new IllegalArgumentException(
                    "Authenticated user email is required");
        }

        if (name == null || name.isBlank()
                || phone == null || !phone.matches("\\d{10}")
                || address == null || address.isBlank()) {
            throw new IllegalArgumentException(
                    "Enter a name, valid 10-digit phone number, and address.");
        }

        String email = authenticatedEmail.trim();

        Customer customer = customerRepository
                .findByEmailIgnoreCase(email)
                .orElseGet(Customer::new);

        customer.setName(name.trim());
        customer.setEmail(email);
        customer.setPhone(phone.trim());
        customer.setAddress(address.trim());

        return customerRepository.save(customer);
    }
}
