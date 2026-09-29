package com.enty.payment.customer.service;

import com.enty.payment.customer.entity.Customer;
import com.enty.payment.customer.repository.CustomerRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomerUserDetailsService implements UserDetailsService {
    private final CustomerRepository repository;

    public CustomerUserDetailsService(CustomerRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String customerId) throws UsernameNotFoundException {
        Customer customer;
        try {
            customer = repository.findById(Long.valueOf(customerId))
                    .orElseThrow(() -> new UsernameNotFoundException("customer not found"));
        } catch (NumberFormatException ex) {
            throw new UsernameNotFoundException("invalid customer token", ex);
        }
        return User.withUsername(customer.getCustId().toString()).password(customer.getPassword())
                .roles("CUSTOMER").build();
    }
}
