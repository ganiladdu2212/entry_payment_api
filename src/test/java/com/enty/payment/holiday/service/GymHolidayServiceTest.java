package com.enty.payment.holiday.service;

import com.enty.payment.customer.entity.Customer;
import com.enty.payment.customer.repository.CustomerRepository;
import com.enty.payment.customer.service.CustomerApiException;
import com.enty.payment.holiday.entity.GymHoliday;
import com.enty.payment.holiday.repository.GymHolidayRepository;
import com.enty.payment.holiday.request.GymHolidayRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import java.time.LocalDate;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GymHolidayServiceTest {
    @Test void createsHolidayForAuthenticatedGym() {
        GymHolidayRepository holidays=mock(GymHolidayRepository.class);
        CustomerRepository customers=mock(CustomerRepository.class);
        Customer customer=new Customer(); customer.setCustId(2L);
        LocalDate date=LocalDate.of(2026,10,20);
        when(customers.findByIdForUpdate(2L)).thenReturn(Optional.of(customer));
        when(holidays.saveAndFlush(any())).thenAnswer(call->{GymHoliday value=call.getArgument(0);value.setHolidayId(7L);return value;});

        var response=new GymHolidayService(holidays,customers).create(new GymHolidayRequest(2L,date,"  Diwali  "),"2");

        assertEquals(7L,response.holidayId()); assertEquals(date,response.holidayDate()); assertEquals("Diwali",response.purpose());
    }
    @Test void rejectsDuplicateDate() {
        GymHolidayRepository holidays=mock(GymHolidayRepository.class);
        CustomerRepository customers=mock(CustomerRepository.class);
        Customer customer=new Customer(); customer.setCustId(2L);
        LocalDate date=LocalDate.of(2026,10,20);
        when(customers.findByIdForUpdate(2L)).thenReturn(Optional.of(customer));
        when(holidays.existsByCustomerCustIdAndHolidayDate(2L,date)).thenReturn(true);

        CustomerApiException error=assertThrows(CustomerApiException.class,()->new GymHolidayService(holidays,customers).create(new GymHolidayRequest(2L,date,"Diwali"),"2"));
        assertEquals(HttpStatus.CONFLICT,error.getStatus());
        verify(holidays,never()).saveAndFlush(any());
    }
}
