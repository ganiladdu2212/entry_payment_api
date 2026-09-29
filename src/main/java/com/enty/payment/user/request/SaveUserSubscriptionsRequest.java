package com.enty.payment.user.request;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter @Setter
public class SaveUserSubscriptionsRequest {
    private String typeOfMode;
    private Long userId;
    private Long custId;
    private String name;
    private String mobileNumber;
    private String countryCode;
    private String discountType;
    private BigDecimal discountValue;
    private String paymentMode;
    private String paymentStatus;
    private Long membershipSubscriptionId;
    private Long personalTrainingSubscriptionId;
    @Setter(lombok.AccessLevel.NONE)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private boolean membershipProvided;
    @Setter(lombok.AccessLevel.NONE)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private boolean trainingProvided;

    public void setMembershipSubscriptionId(Long value) {
        membershipSubscriptionId=value; membershipProvided=true;
    }
    public void setPersonalTrainingSubscriptionId(Long value) {
        personalTrainingSubscriptionId=value; trainingProvided=true;
    }
}
