package com.enty.payment.attendance.service;

import com.enty.payment.attendance.entity.*;
import com.enty.payment.attendance.repository.*;
import com.enty.payment.attendance.request.CheckInCheckOutRequest;
import com.enty.payment.attendance.response.AttendanceResponse;
import com.enty.payment.attendance.response.AttendanceEventResponse;
import com.enty.payment.customer.repository.CustomerRepository;
import com.enty.payment.customer.service.CustomerApiException;
import com.enty.payment.user.entity.MemberUser;
import com.enty.payment.user.repository.MemberUserRepository;
import java.time.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AttendanceService {
    private static final ZoneId INDIA_ZONE=ZoneId.of("Asia/Kolkata");
    private static final int COOLDOWN_SECONDS=10;
    private final UserAttendanceCredentialRepository credentials;
    private final UserAttendanceEventRepository events;
    private final MemberUserRepository users;
    private final CustomerRepository customers;
    private final PasswordEncoder encoder;
    public AttendanceService(UserAttendanceCredentialRepository credentials, UserAttendanceEventRepository events,
            MemberUserRepository users, CustomerRepository customers, PasswordEncoder encoder) {
        this.credentials=credentials; this.events=events; this.users=users; this.customers=customers; this.encoder=encoder;
    }

    @Transactional(isolation=Isolation.SERIALIZABLE)
    public AttendanceResponse scan(CheckInCheckOutRequest request) {
        Long custId=request.custId();
        if(custId==null || !customers.existsById(custId)) throw error(HttpStatus.NOT_FOUND,"customer not found");
        String device=required(request.deviceUniqueId(),"deviceUniqueId",100);
        var known=credentials.findByCustomerCustIdAndDeviceUniqueId(custId,device);
        MemberUser user;
        boolean registered=false;
        if(known.isPresent()) {
            user=known.get().getUser();
        } else {
            if(blank(request.mobileNumber()) || blank(request.pin()))
                throw error(HttpStatus.PRECONDITION_REQUIRED,"Mobile number and PIN required for this device");
            String mobile=required(request.mobileNumber(),"mobileNumber",20);
            if(!mobile.matches("[0-9]{6,15}")) throw error(HttpStatus.BAD_REQUEST,"mobileNumber must contain 6 to 15 digits");
            String pin=request.pin().trim();
            if(!pin.matches("[0-9]{6}")) throw error(HttpStatus.BAD_REQUEST,"PIN must contain exactly 6 digits");
            var matches=users.findAllByCustomerCustIdAndMobileNumber(custId,mobile);
            if(matches.isEmpty()) throw error(HttpStatus.UNAUTHORIZED,"invalid mobile number or PIN");
            if(matches.size()>1) throw error(HttpStatus.CONFLICT,"multiple members use this mobile number; contact the vendor");
            user=matches.getFirst();
            var credential=credentials.findByUserUserId(user.getUserId()).orElse(null);
            if(credential==null) {
                credential=new UserAttendanceCredential(); credential.setCustomer(user.getCustomer()); credential.setUser(user);
                credential.setMobileNumber(user.getMobileNumber()); credential.setPinHash(encoder.encode(pin));
            } else if(!encoder.matches(pin,credential.getPinHash())) {
                throw error(HttpStatus.UNAUTHORIZED,"invalid mobile number or PIN");
            }
            credential.setDeviceUniqueId(device);
            credentials.saveAndFlush(credential);
            registered=true;
        }
        LocalDateTime now=LocalDateTime.now(INDIA_ZONE);
        LocalDateTime from=now.toLocalDate().atStartOfDay();
        LocalDateTime until=from.plusDays(1);
        var latest=events.findFirstByUserUserIdAndCreatedDateGreaterThanEqualAndCreatedDateLessThanOrderByCreatedDateDescAttendanceEventIdDesc(user.getUserId(),from,until);
        if(latest.isPresent() && latest.get().getCreatedDate().isAfter(now.minusSeconds(COOLDOWN_SECONDS)))
            throw error(HttpStatus.CONFLICT,"Please wait before scanning again");
        String action=latest.isPresent() && "CHECK_IN".equals(latest.get().getActionType()) ? "CHECK_OUT" : "CHECK_IN";
        var event=new UserAttendanceEvent(); event.setCustomer(user.getCustomer()); event.setUser(user);
        event.setUserName(user.getName()); event.setMobileNumber(user.getMobileNumber()); event.setDeviceUniqueId(device); event.setActionType(action);
        events.saveAndFlush(event);
        return new AttendanceResponse(event.getAttendanceEventId(),user.getUserId(),user.getName(),user.getMobileNumber(),
            action,event.getCreatedDate(),"CHECK_IN".equals(action)?"CHECK_OUT":"CHECK_IN",registered);
    }
    @Transactional(readOnly=true)
    public java.util.List<AttendanceEventResponse> getEvents(Long userId,String principal) {
        Long custId=customerId(principal);
        if(!customers.existsById(custId)) throw error(HttpStatus.NOT_FOUND,"customer not found");
        if(userId!=null) {
            MemberUser user=users.findById(userId).orElseThrow(() -> error(HttpStatus.NOT_FOUND,"user not found"));
            if(!user.getCustomer().getCustId().equals(custId)) throw error(HttpStatus.FORBIDDEN,"cannot access another customer's attendance");
            return events.findByCustomerCustIdAndUserUserIdOrderByCreatedDateDescAttendanceEventIdDesc(custId,userId)
                .stream().map(AttendanceEventResponse::from).toList();
        }
        return events.findByCustomerCustIdOrderByCreatedDateDescAttendanceEventIdDesc(custId)
            .stream().map(AttendanceEventResponse::from).toList();
    }
    private boolean blank(String value) { return value==null || value.isBlank(); }
    private Long customerId(String principal) {
        try { return Long.valueOf(principal); }
        catch(NumberFormatException ex) { throw error(HttpStatus.FORBIDDEN,"invalid customer identity"); }
    }
    private String required(String value,String field,int max) {
        if(blank(value) || value.trim().length()>max) throw error(HttpStatus.BAD_REQUEST,field+" is required and must be at most "+max+" characters");
        return value.trim();
    }
    private CustomerApiException error(HttpStatus status,String message) { return new CustomerApiException(status,message); }
}
