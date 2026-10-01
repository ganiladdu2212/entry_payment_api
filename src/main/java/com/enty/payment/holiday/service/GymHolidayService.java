package com.enty.payment.holiday.service;

import com.enty.payment.customer.repository.CustomerRepository;
import com.enty.payment.customer.service.CustomerApiException;
import com.enty.payment.holiday.entity.GymHoliday;
import com.enty.payment.holiday.repository.GymHolidayRepository;
import com.enty.payment.holiday.request.GymHolidayRequest;
import com.enty.payment.holiday.response.GymHolidayResponse;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GymHolidayService {
    private final GymHolidayRepository holidays;
    private final CustomerRepository customers;
    public GymHolidayService(GymHolidayRepository holidays, CustomerRepository customers) {
        this.holidays=holidays; this.customers=customers;
    }
    @Transactional(readOnly=true)
    public List<GymHolidayResponse> get(Long custId, String principal) {
        access(custId,principal);
        if(!customers.existsById(custId)) throw error(HttpStatus.NOT_FOUND,"customer not found");
        return holidays.findByCustomerCustIdOrderByHolidayDateAsc(custId).stream().map(GymHolidayResponse::from).toList();
    }
    @Transactional
    public GymHolidayResponse create(GymHolidayRequest request, String principal) {
        access(request.custId(),principal);
        var customer=customers.findByIdForUpdate(request.custId()).orElseThrow(() -> error(HttpStatus.NOT_FOUND,"customer not found"));
        if(holidays.existsByCustomerCustIdAndHolidayDate(request.custId(),request.holidayDate()))
            throw error(HttpStatus.CONFLICT,"A holiday already exists for this date");
        var holiday=new GymHoliday(); holiday.setCustomer(customer); holiday.setHolidayDate(request.holidayDate());
        holiday.setPurpose(request.purpose().trim());
        return GymHolidayResponse.from(holidays.saveAndFlush(holiday));
    }
    @Transactional
    public void delete(Long holidayId, String principal) {
        var holiday=holidays.findById(holidayId).orElseThrow(() -> error(HttpStatus.NOT_FOUND,"holiday not found"));
        access(holiday.getCustomer().getCustId(),principal);
        holidays.delete(holiday);
    }
    private void access(Long custId,String principal) {
        if(custId==null || principal==null || !custId.toString().equals(principal))
            throw error(HttpStatus.FORBIDDEN,"cannot access another customer's holidays");
    }
    private CustomerApiException error(HttpStatus status,String message) { return new CustomerApiException(status,message); }
}
