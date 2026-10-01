package com.enty.payment.user.service;
import com.enty.payment.customer.repository.CustomerRepository;
import com.enty.payment.customer.service.CustomerApiException;
import com.enty.payment.customer.subscription.repository.CustomerSubscriptionRepository;
import com.enty.payment.user.entity.*;
import com.enty.payment.user.repository.MemberUserRepository;
import com.enty.payment.user.request.SaveUserSubscriptionsRequest;
import com.enty.payment.user.response.UserSubscriptionResponse;
import jakarta.persistence.EntityManager;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.Set;

@Service
public class UserSubscriptionService {
    private final MemberUserRepository users;
    private final CustomerRepository customers;
    private final CustomerSubscriptionRepository plans;
    private final EntityManager em;
    public UserSubscriptionService(MemberUserRepository users, CustomerRepository customers,
            CustomerSubscriptionRepository plans, EntityManager em) {
        this.users=users; this.customers=customers; this.plans=plans; this.em=em;
    }
    @Transactional
    public UserSubscriptionResponse save(SaveUserSubscriptionsRequest r, String principal) {
        String mode=required(r.getTypeOfMode(), "typeOfMode", 10).toUpperCase(Locale.ROOT);
        if (!Set.of("CREATE","UPDATE").contains(mode)) throw bad("typeOfMode must be CREATE or UPDATE");
        boolean create=mode.equals("CREATE");
        MemberUser user;
        if (create) {
            if (r.getUserId()!=null) throw bad("userId must be omitted for CREATE");
            access(r.getCustId(), principal);
            user=new MemberUser();
            user.setCustomer(customers.findByIdForUpdate(r.getCustId()).orElseThrow(() -> missing("customer")));
            user.setSubscription(new UserSubscription());
        } else {
            if(r.getUserId()==null) throw bad("userId is mandatory for UPDATE");
            user=users.findById(r.getUserId()).orElseThrow(() -> missing("user"));
            access(user.getCustomer().getCustId(),principal);
            customers.findByIdForUpdate(user.getCustomer().getCustId()).orElseThrow(() -> missing("customer"));
            if(r.getCustId()!=null && !r.getCustId().equals(user.getCustomer().getCustId()))
                throw bad("custId cannot be changed");
        }
        if(create || r.getName()!=null) user.setName(required(r.getName(),"name",150));
        if(create || r.getMobileNumber()!=null) {
            String mobile=required(r.getMobileNumber(),"mobileNumber",20);
            if(!mobile.matches("[0-9]{6,15}")) throw bad("mobileNumber must contain 6 to 15 digits");
            Long customerId=user.getCustomer().getCustId();
            boolean duplicate=create
                ? users.existsByCustomerCustIdAndMobileNumber(customerId,mobile)
                : users.existsByCustomerCustIdAndMobileNumberAndUserIdNot(customerId,mobile,user.getUserId());
            if(duplicate) throw new CustomerApiException(HttpStatus.CONFLICT,
                "This mobile number is already registered for a member in your gym");
            user.setMobileNumber(mobile);
        }
        if(create || r.getCountryCode()!=null) {
            String code=required(r.getCountryCode(),"countryCode",5);
            if(!code.matches("\\+[1-9][0-9]{0,3}")) throw bad("countryCode must be like +91");
            user.setCountryCode(code);
        }
        UserSubscription s=user.getSubscription();
        if(create || r.isMembershipProvided()) s.setMembershipPlan(copy(r.getMembershipSubscriptionId(),user,"MEMBERSHIP"));
        if(create || r.isTrainingProvided()) s.setTrainingPlan(copy(r.getPersonalTrainingSubscriptionId(),user,"PERSONAL_TRAINING"));
        if(s.getMembershipPlan()==null && s.getTrainingPlan()==null) throw bad("select membership or personal training");
        validatePlanDurations(s.getMembershipPlan(), s.getTrainingPlan(),
            java.time.LocalDate.now(java.time.ZoneId.of("Asia/Kolkata")));
        if(s.getMembershipPlan()!=null && s.getTrainingPlan()!=null &&
            !s.getMembershipPlan().getCurrency().equals(s.getTrainingPlan().getCurrency()))
            throw bad("selected plans must use the same currency");
        if(create || r.getDiscountType()!=null)
            s.setDiscountType(required(r.getDiscountType(),"discountType",20).toUpperCase(Locale.ROOT));
        if(!Set.of("PERCENTAGE","FIXED_AMOUNT").contains(s.getDiscountType()))
            throw bad("discountType must be PERCENTAGE or FIXED_AMOUNT");
        if(create || r.getDiscountValue()!=null) s.setDiscountValue(r.getDiscountValue());
        BigDecimal discount=s.getDiscountValue();
        if(discount==null || discount.signum()<0 || discount.scale()>2 || discount.precision()-discount.scale()>10)
            throw bad("discountValue must be non-negative with at most 2 decimal places");
        if(s.getDiscountType().equals("PERCENTAGE") && discount.compareTo(BigDecimal.valueOf(100))>0)
            throw bad("percentage cannot exceed 100");
        if(create || r.getPaymentMode()!=null)
            s.setPaymentMode(required(r.getPaymentMode(),"paymentMode",10).toUpperCase(Locale.ROOT));
        if(!Set.of("UPI","CASH","CARD").contains(s.getPaymentMode())) throw bad("paymentMode must be UPI, CASH, or CARD");
        if (create || r.getPaymentStatus()!=null) {
            String status=required(r.getPaymentStatus(),"paymentStatus",20).toUpperCase(Locale.ROOT);
            if (!Set.of("RECEIVED","PENDING","NOT_ONBOARDED").contains(status)) throw bad("paymentStatus must be RECEIVED, PENDING, or NOT_ONBOARDED");
            s.setPaymentStatus(status);
        }
        users.saveAndFlush(user);
        return UserSubscriptionResponse.from(user);
    }
    @Transactional(readOnly=true)
    public UserSubscriptionResponse get(Long userId,String principal) {
        MemberUser user=users.findById(userId).orElseThrow(() -> missing("user"));
        access(user.getCustomer().getCustId(),principal);
        return UserSubscriptionResponse.from(user);
    }
    @Transactional(readOnly=true)
    public java.util.List<UserSubscriptionResponse> getByCustomer(Long custId, String principal) {
        access(custId, principal);
        if (!customers.existsById(custId)) throw missing("customer");
        return users.findByCustomerCustIdOrderByCreatedDateDescUserIdDesc(custId).stream()
            .map(UserSubscriptionResponse::from).toList();
    }
    private UserPlanSnapshot copy(Long id, MemberUser user, String type) {
        if(id==null) return null;
        var plan=plans.findById(id).orElseThrow(() -> missing("subscription plan"));
        if(!plan.getCustomer().getCustId().equals(user.getCustomer().getCustId()))
            throw new CustomerApiException(HttpStatus.FORBIDDEN,"plan belongs to another customer");
        if(!Boolean.TRUE.equals(plan.getActive()) || !type.equals(plan.getTypeOfPlan()))
            throw bad("selected plan must be active and have type "+type);
        var snapshot=UserPlanSnapshot.copyOf(plan);
        em.persist(snapshot);
        return snapshot;
    }
    static void validatePlanDurations(UserPlanSnapshot membership, UserPlanSnapshot training, java.time.LocalDate start) {
        if (training == null) return;
        if (membership == null || planEnd(training, start).isAfter(planEnd(membership, start)))
            throw new CustomerApiException(HttpStatus.BAD_REQUEST,
                "Personal training cannot last longer than your membership. Select a longer membership or a shorter training plan.");
    }
    private static java.time.LocalDate planEnd(UserPlanSnapshot plan, java.time.LocalDate start) {
        String unit = plan.getDurationUnit().trim().replace("-", "").replace("_", "")
            .replace(" ", "").toUpperCase(Locale.ROOT);
        return switch (unit) {
            case "DAY", "DAILY", "DAYS" -> start.plusDays(plan.getDurationValue());
            case "WEEK", "WEEKLY", "WEEKS" -> start.plusWeeks(plan.getDurationValue());
            case "MONTH", "MONTHLY", "MONTHS", "QUARTER", "QUARTERLY", "HALFYEAR", "HALFYEARLY" ->
                start.plusMonths(plan.getDurationValue());
            case "YEAR", "YEARS" -> start.plusYears(plan.getDurationValue());
            case "YEARLY" -> start.plusMonths(plan.getDurationValue());
            default -> throw new CustomerApiException(HttpStatus.BAD_REQUEST, "Invalid plan duration unit");
        };
    }
    private void access(Long custId,String principal) {
        if(custId==null || !custId.toString().equals(principal))
            throw new CustomerApiException(HttpStatus.FORBIDDEN,"cannot access another customer's users");
    }
    private String required(String value,String field,int max) {
        if(value==null || value.isBlank() || value.trim().length()>max) throw bad(field+" is required and must be at most "+max+" characters");
        return value.trim();
    }
    private CustomerApiException bad(String message) { return new CustomerApiException(HttpStatus.BAD_REQUEST,message); }
    private CustomerApiException missing(String entity) { return new CustomerApiException(HttpStatus.NOT_FOUND,entity+" not found"); }
}
