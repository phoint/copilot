# Postman Collection Testing Guide

## Overview

This guide explains how to test the SpringBoot_Copilot Postman collection with the provided test dataset.

## Test Data Setup

### 1. Load Test Data

The test dataset is automatically loaded via Flyway migration `V8__insert_test_data.sql` on application startup.

**Test Data Includes:**
- **4 Test Users**: alice_customer, bob_customer, charlie_customer, diana_customer (IDs: 1-4)
- **12 Test Orders**: Spanning all 7 order states (PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED, REFUNDED)
- **28 Order Items**: Products with various quantities and prices

### 2. Start the Application

```bash
cd c:/Users/pc/learning-project/copilot
mvn spring-boot:run -pl api
```

Server runs on: `http://localhost:8080`

### 3. Import Postman Collection

1. Open Postman
2. Click **Import** → Select `SpringBoot_Copilot.postman_collection.json`
3. Collection automatically uses variables:
   - `{{base_url}}` = `http://localhost:8080/api`
   - `{{version}}` = `/v1`
   - `{{order-service}}` = `/orders`

---

## Test Scenarios

### Scenario 1: Create a New Order (Happy Path)

**Endpoint:** `POST /api/v1/orders`

**Request Body:**
```json
{
  "userId": 1,
  "items": [
    {
      "productId": 1,
      "quantity": 2
    }
  ],
  "shippingAddress": "123 New Street, New City, State",
  "promoCode": null
}
```

**Expected Response:** 
- **Status Code**: `201 Created`
- **Response includes**:
  - `id`: Generated order ID
  - `status`: `PENDING`
  - `items`: Array with order items
  - `totalAmount`: Calculated total

**Validation Points:**
✅ Order created with PENDING status
✅ Order items are linked correctly
✅ Total amount is calculated
✅ Timestamps (createdAt, updatedAt) are set

---

### Scenario 2: Get Order by ID

**Endpoint:** `GET /api/v1/orders/:id`

**Path Variable**: `id` = `1` (from existing test data)

**Expected Response:**
- **Status Code**: `200 OK`
- Response contains complete order details with all items

**Test Cases:**
- ✅ Get valid order → 200
- ❌ Get non-existent order (id=99999) → 404 ORDER_NOT_FOUND

---

### Scenario 3: List All Orders (Pagination)

**Endpoint:** `GET /api/v1/orders?page=0&size=10`

**Query Parameters**:
- `page`: `0`
- `size`: `10`

**Expected Response:**
- **Status Code**: `200 OK`
- **Response includes**:
  - `content`: Array of orders
  - `totalElements`: Total count (should be ≥ 12)
  - `totalPages`: Calculated pages
  - `size`: 10
  - `number`: 0 (page number)

**Validation Points:**
✅ Returns paginated results
✅ Total elements matches test data (12 orders)
✅ Pagination metadata is correct

---

### Scenario 4: Get Orders by User

**Endpoint:** `GET /api/v1/orders/user/:userId?page=0&size=10`

**Path Variable**: `userId` = `1` (alice_customer)

**Expected Response:**
- **Status Code**: `200 OK`
- Returns only orders for user ID 1
- Alice has 4 orders in test data (order IDs: 1, 3, 7, 11)

**Validation Points:**
✅ Returns only user's orders
✅ Filters correctly by userId
✅ Pagination works for filtered results

---

### Scenario 5: State Machine - Valid Transitions

#### 5a. PENDING → CONFIRMED

**Endpoint:** `PATCH /api/v1/orders/:id/status`

**Path Variable**: `id` = `1` (PENDING order)

**Request Body:**
```json
{
  "status": "CONFIRMED"
}
```

**Expected Response:**
- **Status Code**: `200 OK`
- **Response**: Order with `status: CONFIRMED`

**Validation Points:**
✅ Transition allowed
✅ Status updated in response
✅ No errors thrown

---

#### 5b. CONFIRMED → PROCESSING

**Endpoint:** `PATCH /api/v1/orders/:id/status`

**Path Variable**: `id` = `3` (CONFIRMED order)

**Request Body:**
```json
{
  "status": "PROCESSING"
}
```

**Expected Response:**
- **Status Code**: `200 OK`
- Order transitioned to PROCESSING

---

#### 5c. PROCESSING → SHIPPED

**Endpoint:** `PATCH /api/v1/orders/:id/status`

**Path Variable**: `id` = `5` (PROCESSING order)

**Request Body:**
```json
{
  "status": "SHIPPED"
}
```

**Expected Response:**
- **Status Code**: `200 OK`
- Order transitioned to SHIPPED

---

#### 5d. SHIPPED → DELIVERED

**Endpoint:** `PATCH /api/v1/orders/:id/status`

**Path Variable**: `id` = `7` (SHIPPED order)

**Request Body:**
```json
{
  "status": "DELIVERED"
}
```

**Expected Response:**
- **Status Code**: `200 OK`
- Order transitioned to DELIVERED

---

### Scenario 6: State Machine - Invalid Transitions (Guards)

#### 6a. Invalid Transition: PENDING → SHIPPED (skipping states)

**Endpoint:** `PATCH /api/v1/orders/:id/status`

**Path Variable**: `id` = `2` (PENDING order)

**Request Body:**
```json
{
  "status": "SHIPPED"
}
```

**Expected Response:**
- **Status Code**: `400 Bad Request`
- **Response Body**:
  ```json
  {
    "code": "INVALID_ORDER_TRANSITION",
    "message": "Cannot transition from PENDING to SHIPPED",
    "timestamp": "2026-05-02T17:30:00"
  }
  ```

**Validation Points:**
✅ State machine blocks invalid transition
✅ Returns 400 error code
✅ Error code is INVALID_ORDER_TRANSITION
✅ Clear error message

---

#### 6b. Invalid Transition: From Terminal State (CANCELLED)

**Endpoint:** `PATCH /api/v1/orders/:id/status`

**Path Variable**: `id` = `11` (CANCELLED order)

**Request Body:**
```json
{
  "status": "REFUNDED"
}
```

**Expected Response:**
- **Status Code**: `400 Bad Request`
- **Error**: INVALID_ORDER_TRANSITION

**Validation Points:**
✅ Terminal states prevent transitions
✅ No state changes allowed from CANCELLED

---

### Scenario 7: Order Update (Partial Update)

**Endpoint:** `PATCH /api/v1/orders/:id`

**Path Variable**: `id` = `2`

**Request Body:**
```json
{
  "shippingAddress": "999 Updated Lane, Updated City, State",
  "promoCode": "SUMMER20"
}
```

**Expected Response:**
- **Status Code**: `200 OK`
- Order with updated `shippingAddress` and `promoCode`
- Other fields unchanged

**Validation Points:**
✅ Partial update works
✅ Only specified fields updated
✅ Status not changed (optional in update)
✅ Timestamps updated (updatedAt)

---

### Scenario 8: Cancel Order

**Endpoint:** `POST /api/v1/orders/:id/cancel`

**Path Variable**: `id` = `1`

**Expected Response:**
- **Status Code**: `200 OK`
- Order with `status: CANCELLED`

**Validation Points:**
✅ Convenience endpoint works
✅ Transition to CANCELLED allowed
✅ Order is now in terminal state

---

## Complete Test Flow (Happy Path)

Follow this sequence to test the full order lifecycle:

### Step 1: Create an Order
```
POST /api/v1/orders
Body: Create new order with userId=1, product IDs from seed data
Expected: 201 CREATED with order ID (e.g., 100)
```

### Step 2: Get the Created Order
```
GET /api/v1/orders/100
Expected: 200 OK, order in PENDING status
```

### Step 3: Confirm Order
```
PATCH /api/v1/orders/100/status
Body: {"status": "CONFIRMED"}
Expected: 200 OK, status changed to CONFIRMED
```

### Step 4: Update Shipping Address
```
PATCH /api/v1/orders/100
Body: {"shippingAddress": "New address"}
Expected: 200 OK, address updated
```

### Step 5: Process Order
```
PATCH /api/v1/orders/100/status
Body: {"status": "PROCESSING"}
Expected: 200 OK, status changed to PROCESSING
```

### Step 6: Ship Order
```
PATCH /api/v1/orders/100/status
Body: {"status": "SHIPPED"}
Expected: 200 OK, status changed to SHIPPED
```

### Step 7: Deliver Order
```
PATCH /api/v1/orders/100/status
Body: {"status": "DELIVERED"}
Expected: 200 OK, status changed to DELIVERED
```

### Step 8: Refund Order (Optional)
```
PATCH /api/v1/orders/100/status
Body: {"status": "REFUNDED"}
Expected: 200 OK, status changed to REFUNDED
```

---

## Test Data Orders Reference

### By State:

| Order ID | User | Status | Address | Items | Total |
|----------|------|--------|---------|-------|-------|
| 1-2 | 1-2 | PENDING | Various | 1-2 items | $1999.98 |
| 3-4 | 1,3 | CONFIRMED | Various | 1-2 items | $2014.98 |
| 5-6 | 2-4 | PROCESSING | Various | 2-4 items | $2519.96 |
| 7-8 | 1,3 | SHIPPED | Various | 2-4 items | $4574.95 |
| 9-10 | 2,4 | DELIVERED | Various | 2-3 items | $2999.97 |
| 11-12 | 1,3 | CANCELLED | Various | 1-2 items | $0.00 |
| 13 | 2 | REFUNDED | Chicago, IL | 1 item | $0.00 |

### Test Users:

| ID | Username | Email | Role |
|----|----------|-------|------|
| 1 | alice_customer | alice@example.com | USER |
| 2 | bob_customer | bob@example.com | USER |
| 3 | charlie_customer | charlie@example.com | USER |
| 4 | diana_customer | diana@example.com | USER |

---

## Error Testing Scenarios

### Scenario A: Invalid User ID
```
POST /api/v1/orders
Body: {"userId": 99999, "items": [...]}
Expected: 404 USER_NOT_FOUND
```

### Scenario B: Invalid Product ID
```
POST /api/v1/orders
Body: {"userId": 1, "items": [{"productId": 99999, "quantity": 1}]}
Expected: 404 PRODUCT_NOT_FOUND
```

### Scenario C: Empty Items
```
POST /api/v1/orders
Body: {"userId": 1, "items": []}
Expected: 400 VALIDATION_ERROR
```

### Scenario D: Missing Required Fields
```
POST /api/v1/orders
Body: {"items": [...]}  // Missing userId
Expected: 400 VALIDATION_ERROR
```

### Scenario E: Invalid Quantity
```
POST /api/v1/orders
Body: {"userId": 1, "items": [{"productId": 1, "quantity": -1}]}
Expected: 400 VALIDATION_ERROR (Min value = 1)
```

---

## Guard Conditions Testing

### Guard 1: hasItems()
- ✅ Orders with items → transition allowed
- ❌ Empty items → CONFIRM transition blocked (checked during creation)

### Guard 2: hasValidTotal()
- ✅ Total ≥ 0 → transition to PROCESSING allowed
- ❌ Negative total → transition blocked (guard check)

### Guard 3: hasShippingAddress()
- ✅ Address provided → transition to SHIPPED allowed
- ❌ No address → transition blocked (guard check)

### Guard 4: canBeCancelled()
- ✅ Non-terminal status → CANCEL allowed
- ❌ Terminal status (CANCELLED/REFUNDED) → CANCEL blocked

### Guard 5: withinMaxOrderValue()
- ✅ Total ≤ $100,000 → CONFIRM allowed
- ❌ Total > $100,000 → CONFIRM blocked (fraud prevention)

---

## Performance Testing

### Load Test Scenario
```
Create 100 orders in sequence
GET all orders with page=0&size=50
Expected: Response time < 500ms for each request
```

### Pagination Test
```
GET /api/v1/orders?page=0&size=5
GET /api/v1/orders?page=1&size=5
GET /api/v1/orders?page=2&size=5
Expected: Correct pagination across all pages
```

---

## Troubleshooting

### Issue: 404 Order Not Found
- **Cause**: Order ID doesn't exist or was already deleted
- **Solution**: Use order IDs from test data (1-13) or create a new order first

### Issue: 400 Invalid Transition
- **Cause**: Attempting invalid state transition
- **Solution**: Check state machine diagram; follow valid transition paths
- **Valid paths**: PENDING → CONFIRMED → PROCESSING → SHIPPED → DELIVERED → REFUNDED
- **OR**: Any state → CANCELLED (except terminal states)

### Issue: 400 Validation Error
- **Cause**: Missing required fields or invalid field values
- **Solution**: 
  - Check all required fields are provided
  - Validate field constraints (e.g., quantity ≥ 1)
  - Ensure arrays are not empty

### Issue: 404 User/Product Not Found
- **Cause**: Referenced IDs don't exist
- **Solution**: Use existing IDs from seed data or create entities first

---

## Next Steps

After successful testing:

1. ✅ Verify all 7 order states work correctly
2. ✅ Test all state machine transitions
3. ✅ Validate guard conditions prevent invalid transitions
4. ✅ Confirm pagination and filtering work
5. ✅ Test error scenarios and edge cases

---

## Questions?

For issues or questions about the test data or Postman collection:
- Review the Order Service implementation in `/service/src/main/java/edu/ecommerce/service/`
- Check the Spring State Machine configuration in `/service/src/main/java/edu/ecommerce/service/statemachine/`
- Review the test cases in `/api/src/test/java/edu/ecommerce/api/controller/OrderControllerTest.java`
