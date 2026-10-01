package com.enty.payment.user.service;

import com.enty.payment.customer.entity.Customer;
import com.enty.payment.customer.repository.CustomerRepository;
import com.enty.payment.customer.service.CustomerApiException;
import com.enty.payment.customer.subscription.repository.CustomerSubscriptionRepository;
import com.enty.payment.user.repository.MemberUserRepository;
import com.enty.payment.user.request.SaveUserSubscriptionsRequest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DuplicateMemberRegistrationTest {
    @Test void rejectsAnExistingMobileEvenForANewRegistration() {
        MemberUserRepository users=mock(MemberUserRepository.class);
        CustomerRepository customers=mock(CustomerRepository.class);
        CustomerSubscriptionRepository plans=mock(CustomerSubscriptionRepository.class);
        EntityManager entityManager=mock(EntityManager.class);
        Customer customer=new Customer(); customer.setCustId(2L);
        when(customers.findByIdForUpdate(2L)).thenReturn(Optional.of(customer));
        when(users.existsByCustomerCustIdAndMobileNumber(2L,"9849546768")).thenReturn(true);
        SaveUserSubscriptionsRequest request=new SaveUserSubscriptionsRequest();
        request.setTypeOfMode("CREATE"); request.setCustId(2L); request.setName("Existing member");
        request.setMobileNumber("9849546768");

        CustomerApiException error=assertThrows(CustomerApiException.class,
            () -> new UserSubscriptionService(users,customers,plans,entityManager).save(request,"2"));

        assertEquals(HttpStatus.CONFLICT,error.getStatus());
        assertEquals("This mobile number is already registered for a member in your gym",error.getMessage());
        verify(users,never()).saveAndFlush(any());
    }
}
