# Check-in / check-out API

`POST /entry-payment/api/v1/public/attendance/checkInCheckOut` is public and does not require JWT.
For the current test version, `custId` identifies the QR owner. Replace it with a secure,
revocable branch QR token before production.

## Known device

```json
{"custId":2,"deviceUniqueId":"browser-generated-uuid"}
```

## Unknown device / first setup

The first request above returns HTTP 428 when the device is unknown. Retry with:

```json
{
  "custId":2,
  "deviceUniqueId":"browser-generated-uuid",
  "mobileNumber":"9849546768",
  "pin":"482615"
}
```

The mobile number must already exist in the vendor's `users` table. A new member creates a
BCrypt PIN hash and links the device. An existing member must provide the correct PIN; after
verification the stored old device ID is replaced. The request then records attendance.

The server determines action from the member's latest event for the current Asia/Kolkata day:
no event -> CHECK_IN, CHECK_IN -> CHECK_OUT, CHECK_OUT -> CHECK_IN. A 10-second cooldown
rejects accidental duplicate scans with HTTP 409. PINs must contain exactly six digits.

Successful response data includes `attendanceEventId`, `userId`, name, mobile number,
`actionType`, `eventTime`, `nextAction`, and `deviceRegistered`. All tables use `created_date`.

Apply `docs/sql/create-attendance-tables.sql` in dev/prod. Local `ddl-auto=update` creates them
when the API restarts.

## Get attendance history

Requires the customer access JWT:

`GET /entry-payment/api/v1/attendance/getCheckInCheckOut`

Returns all attendance events belonging to the logged-in customer, newest first.

`GET /entry-payment/api/v1/attendance/getCheckInCheckOut?userId=601`

Returns only that user's events, newest first. A user belonging to another customer is rejected
with 403. An empty result returns `data: []`.
