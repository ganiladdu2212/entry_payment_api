package com.enty.payment.customer.service;

import com.enty.payment.customer.entity.Customer;
import com.enty.payment.customer.repository.CustomerRepository;
import com.enty.payment.customer.request.CustomerLoginRequest;
import com.enty.payment.customer.request.CustomerRegistrationRequest;
import com.enty.payment.customer.response.CustomerLoginResponse;
import com.enty.payment.customer.response.CustomerResponse;
import com.enty.payment.customer.storage.CustomerLogoStorage;
import com.enty.payment.security.JwtTokenProvider;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {
    private final CustomerRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final CustomerLogoStorage logoStorage;

    public CustomerService(CustomerRepository repository, PasswordEncoder passwordEncoder,
            JwtTokenProvider tokenProvider, CustomerLogoStorage logoStorage) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.logoStorage = logoStorage;
    }

    @Transactional
    public CustomerResponse register(CustomerRegistrationRequest request) {
        String action = normalize(request.getTypeOfAction());
        if (action == null) throw badRequest("typeOfAction is mandatory");
        return switch (action.toUpperCase(Locale.ROOT)) {
            case "CREATE" -> create(request);
            case "UPDATE" -> update(request);
            default -> throw badRequest("typeOfAction must be CREATE or UPDATE");
        };
    }

    @Transactional(readOnly = true)
    public CustomerLoginResponse login(CustomerLoginRequest request) {
        String email = normalizeEmail(request.email());
        String mobile = normalize(request.mobileNumber());
        if (email == null && mobile == null) throw badRequest("email or mobileNumber is mandatory");
        if (normalize(request.pwd()) == null) throw badRequest("pwd is mandatory");

        Customer customer = email != null
                ? repository.findByEmailIgnoreCase(email).orElseThrow(this::invalidCredentials)
                : repository.findByMobileNumber(mobile).orElseThrow(this::invalidCredentials);
        if (email != null && mobile != null && !mobile.equals(customer.getMobileNumber())) throw invalidCredentials();
        if (!passwordEncoder.matches(request.pwd(), customer.getPassword())) throw invalidCredentials();

        String subject = customer.getCustId().toString();
        return new CustomerLoginResponse(CustomerResponse.from(customer), tokenProvider.createAccessToken(subject),
                tokenProvider.createRefreshToken(subject), "Bearer", tokenProvider.getAccessExpirationSeconds());
    }

    private CustomerResponse create(CustomerRegistrationRequest request) {
        String email = normalizeEmail(request.getEmail());
        String mobile = normalize(request.getMobileNumber());
        validateContact(email, mobile);
        if (normalize(request.getPwd()) == null) throw badRequest("pwd is mandatory for CREATE");
        ensureUnique(email, mobile, null);

        Customer customer = new Customer();
        customer.setName(normalize(request.getName()));
        customer.setOrgName(normalize(request.getOrgName()));
        customer.setMobileNumber(mobile);
        customer.setEmail(email);
        customer.setPassword(passwordEncoder.encode(request.getPwd()));
        String newLogo = logoStorage.store(request.getLogo());
        customer.setLogo(newLogo);
        try {
            return CustomerResponse.from(repository.save(customer));
        } catch (RuntimeException ex) {
            logoStorage.delete(newLogo);
            throw ex;
        }
    }

    private CustomerResponse update(CustomerRegistrationRequest request) {
        if (request.getCustId() == null) throw badRequest("custId is mandatory for UPDATE");
        Customer customer = repository.findById(request.getCustId())
                .orElseThrow(() -> new CustomerApiException(HttpStatus.NOT_FOUND, "customer not found"));

        String email = request.getEmail() == null ? customer.getEmail() : normalizeEmail(request.getEmail());
        String mobile = request.getMobileNumber() == null ? customer.getMobileNumber() : normalize(request.getMobileNumber());
        validateContact(email, mobile);
        ensureUnique(email, mobile, customer.getCustId());

        if (request.getName() != null) customer.setName(normalize(request.getName()));
        if (request.getOrgName() != null) customer.setOrgName(normalize(request.getOrgName()));
        if (request.getMobileNumber() != null) customer.setMobileNumber(mobile);
        if (request.getEmail() != null) customer.setEmail(email);
        if (request.getPwd() != null) {
            if (normalize(request.getPwd()) == null) throw badRequest("pwd cannot be blank");
            customer.setPassword(passwordEncoder.encode(request.getPwd()));
        }
        String oldLogo = customer.getLogo();
        String newLogo = logoStorage.store(request.getLogo());
        if (newLogo != null) customer.setLogo(newLogo);
        try {
            CustomerResponse response = CustomerResponse.from(repository.save(customer));
            if (newLogo != null) logoStorage.delete(oldLogo);
            return response;
        } catch (RuntimeException ex) {
            logoStorage.delete(newLogo);
            throw ex;
        }
    }

    private void validateContact(String email, String mobile) {
        if (email == null && mobile == null) throw badRequest("email or mobileNumber is mandatory");
        if (email != null && !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) throw badRequest("email is invalid");
        if (mobile != null && !mobile.matches("^[0-9+][0-9 -]{6,19}$")) throw badRequest("mobileNumber is invalid");
    }

    private void ensureUnique(String email, String mobile, Long excludedId) {
        boolean emailExists = email != null && (excludedId == null
                ? repository.existsByEmailIgnoreCase(email) : repository.existsByEmailIgnoreCaseAndCustIdNot(email, excludedId));
        boolean mobileExists = mobile != null && (excludedId == null
                ? repository.existsByMobileNumber(mobile) : repository.existsByMobileNumberAndCustIdNot(mobile, excludedId));
        if (emailExists) throw new CustomerApiException(HttpStatus.CONFLICT, "email already registered");
        if (mobileExists) throw new CustomerApiException(HttpStatus.CONFLICT, "mobileNumber already registered");
    }

    private CustomerApiException invalidCredentials() {
        return new CustomerApiException(HttpStatus.UNAUTHORIZED, "invalid login credentials");
    }

    private CustomerApiException badRequest(String message) {
        return new CustomerApiException(HttpStatus.BAD_REQUEST, message);
    }

    private String normalizeEmail(String value) {
        String normalized = normalize(value);
        return normalized == null ? null : normalized.toLowerCase(Locale.ROOT);
    }

    private String normalize(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
