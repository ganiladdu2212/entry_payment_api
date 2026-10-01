# User subscriptions

Both endpoints require the customer's access JWT.

- POST /entry-payment/api/v1/users/saveUserSubscriptions
- GET /entry-payment/api/v1/users/getUserSubscription/{userId}

CREATE:

```json
{
  "typeOfMode": "CREATE",
  "custId": 2,
  "name": "Ganesh",
  "mobileNumber": "9876543210",
  "countryCode": "+91",
  "membershipSubscriptionId": 3,
  "personalTrainingSubscriptionId": null,
  "discountType": "PERCENTAGE",
  "discountValue": 10,
  "paymentMode": "UPI"
}
```

UPDATE requires userId; omit unchanged fields. Explicit null for a plan ID
removes that selection. At least one plan must remain selected.

```json
{
  "typeOfMode": "UPDATE",
  "userId": 601,
  "discountType": "FIXED_AMOUNT",
  "discountValue": 500,
  "personalTrainingSubscriptionId": null
}
```

Supplied plan IDs refer to cust_subscriptions. Each supplied plan is copied to
usersubscriptionlist. The usersubcription record references the copies, and
users references usersubcription. Previous snapshots are retained when replaced.
All writes are transactional. GET and save return the full user and subscription,
including membershipPlan and trainingPlan objects with their copied subscriptionId.
created_date is inherited from BaseEntity and uses Asia/Kolkata.

FIXED_AMOUNT discountValue is the entered currency amount (e.g. 500 rupees);
plan basePriceMinor and savingsMinor are copied verbatim. No totals or payment
processing are performed by these endpoints. Existing price-unit inconsistencies
in the UI/catalog should be resolved before introducing payment calculations.

Local profile creates the new tables via Hibernate schema update. Dev/prod
use schema validation and require applying the corresponding additive schema
changes before deployment.
