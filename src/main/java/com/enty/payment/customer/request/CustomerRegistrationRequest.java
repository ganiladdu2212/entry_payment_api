package com.enty.payment.customer.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class CustomerRegistrationRequest {
    private String typeOfAction;
    private Long custId;
    private String name;
    private String orgName;
    private MultipartFile logo;
    private String mobileNumber;
    private String email;
    private String pwd;
}
