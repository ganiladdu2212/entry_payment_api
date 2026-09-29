# Registered users by customer

`GET /entry-payment/api/v1/users/getUser/{custId}`

Requires `Authorization: Bearer <access-token>`. The JWT customer ID must match
`custId`; another customer's ID returns 403. No database schema changes required.

Returns the standard `{status, statusCode, message, data}` envelope. `data` is a
list using the same full user/subscription response as `getUserSubscription`,
including copied membership and training plans. Sorted by `createdDate` descending,
then `userId` descending. An existing customer without users receives `data: []`.
The endpoint currently returns all users without pagination, as requested.

`subscription.paymentStatus` returns `RECEIVED`, `PENDING`, `NOT_ONBOARDED`, or null for legacy records.
Create requests to `saveUserSubscriptions` must include `paymentStatus` (RECEIVED,
PENDING, or NOT_ONBOARDED). On UPDATE, omitting it preserves the existing status. Invalid values
return 400. Enquiry status is not stored or inferred by this endpoint.
For dev/prod, apply `docs/sql/add-payment-status.sql` once, then
`docs/sql/expand-payment-status.sql` before deployment to allow NOT_ONBOARDED.
Local Hibernate schema update adds the nullable column on restart.
The UI date filters use registration time; they do not imply a subscription start date.
