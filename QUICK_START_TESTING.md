# Quick Start: Testing with Postman

## 🚀 5-Minute Setup

### Step 1: Start the Application
```bash
cd c:/Users/pc/learning-project/copilot
mvn spring-boot:run -pl api
```

Server runs on: `http://localhost:8080`

### Step 2: Import Postman Files

**In Postman:**

1. Click **Import** → Select `SpringBoot_Copilot.postman_collection.json`
2. Click **Import** again → Select `SpringBoot_Copilot_Testing.postman_environment.json`
3. In the top-right, select environment: **SpringBoot_Copilot_Testing**

### Step 3: Test Data

Test data is **automatically loaded** via Flyway migration `V8__insert_test_data.sql`:
- ✅ 4 test users
- ✅ 12 test orders (all 7 states)
- ✅ 28 order items

No additional setup needed!

---

## 📋 Test Quick Reference

### Create Order
```
POST {{base_url}}{{version}}{{order-service}}

Body:
{
  "userId": {{test_user_alice_id}},
  "items": [{"productId": 1, "quantity": 2}],
  "shippingAddress": "123 Main St",
  "promoCode": null
}

Expected: 201 CREATED
```

### Get All Orders
```
GET {{base_url}}{{version}}{{order-service}}?page=0&size=10

Expected: 200 OK
Response includes 12+ orders from test data
```

### State Transition: PENDING → CONFIRMED
```
PATCH {{base_url}}{{version}}{{order-service}}/{{test_order_pending_id}}/status

Body: {"status": "CONFIRMED"}

Expected: 200 OK
```

### Try Invalid Transition (Test Guards)
```
PATCH {{base_url}}{{version}}{{order-service}}/{{test_order_pending_id}}/status

Body: {"status": "SHIPPED"}  ← Invalid (skip states)

Expected: 400 INVALID_ORDER_TRANSITION
```

---

## 🧪 Test Scenarios (Ready to Run)

### Scenario 1: Full Order Lifecycle ✅
- Create order → Confirm → Process → Ship → Deliver
- **Endpoint folder**: Order → (Create, Update Status, etc.)

### Scenario 2: State Machine Guards ✅
- Try invalid transitions (PENDING → SHIPPED)
- Expected: 400 error
- **Test endpoint**: Update Order Status (invalid)

### Scenario 3: Pagination ✅
- List orders with page=0&size=5
- List orders with page=1&size=5
- **Endpoint**: Get All Orders

### Scenario 4: Error Handling ✅
- Non-existent order (id=99999) → 404
- Invalid user ID → 404
- **Test endpoints**: Get Order by ID (vary ID values)

---

## 📊 Test Data Summary

### Users (IDs 1-4)
```
alice_customer   → ID 1 → 4 orders
bob_customer     → ID 2 → 2 orders
charlie_customer → ID 3 → 3 orders
diana_customer   → ID 4 → 2 orders
```

### Orders by State
```
PENDING    (IDs 1-2)   → Test confirmation
CONFIRMED  (IDs 3-4)   → Test processing
PROCESSING (IDs 5-6)   → Test shipping
SHIPPED    (IDs 7-8)   → Test delivery
DELIVERED  (IDs 9-10)  → Test refund
CANCELLED  (IDs 11-12) → Test terminal state
REFUNDED   (ID 13)     → Test terminal state
```

### Environment Variables
```
{{base_url}}              = http://localhost:8080/api
{{version}}               = /v1
{{order-service}}         = /orders
{{test_user_alice_id}}    = 1
{{test_order_pending_id}} = 1
{{test_product_id_1}}     = 1
```

---

## ✅ Verification Checklist

Run these tests in sequence to verify everything works:

### Basic Operations
- [ ] `POST /orders` - Create new order (expect 201)
- [ ] `GET /orders/1` - Get order (expect 200)
- [ ] `GET /orders` - List orders (expect 200, 12+ results)
- [ ] `GET /orders/user/1` - Filter by user (expect 200, 4 orders)

### State Transitions (Valid)
- [ ] `PATCH /orders/1/status` → CONFIRMED (expect 200)
- [ ] `PATCH /orders/3/status` → PROCESSING (expect 200)
- [ ] `PATCH /orders/5/status` → SHIPPED (expect 200)
- [ ] `PATCH /orders/7/status` → DELIVERED (expect 200)
- [ ] `PATCH /orders/9/status` → REFUNDED (expect 200)

### Guard Conditions (Invalid Transitions)
- [ ] `PATCH /orders/1/status` → SHIPPED (expect 400)
- [ ] `PATCH /orders/11/status` → CONFIRMED (expect 400 - terminal state)

### Order Updates
- [ ] `PATCH /orders/1` → Update address (expect 200)
- [ ] `POST /orders/1/cancel` → Cancel order (expect 200)

### Error Cases
- [ ] `GET /orders/99999` (expect 404 ORDER_NOT_FOUND)
- [ ] `POST /orders` with invalid userId (expect 404)
- [ ] `POST /orders` with empty items (expect 400)

---

## 🔍 Debugging Tips

### Check if server is running
```bash
curl http://localhost:8080/api/v1/orders
# Should return JSON list of orders
```

### View test data in database
```bash
# While server is running, check H2 console (if enabled)
# Or use: SELECT * FROM orders;
```

### Increase logging
Add to `application.properties`:
```properties
logging.level.edu.ecommerce=DEBUG
```

---

## 📚 Detailed Documentation

For complete testing guide with all scenarios:
- See: `POSTMAN_TESTING_GUIDE.md`

For implementation details:
- Order Service: `/service/src/main/java/edu/ecommerce/service/`
- State Machine: `/service/src/main/java/edu/ecommerce/service/statemachine/`
- Tests: `/api/src/test/java/edu/ecommerce/api/controller/OrderControllerTest.java`

---

## 🎯 Next Steps

1. ✅ Start application (`mvn spring-boot:run -pl api`)
2. ✅ Import Postman collection and environment
3. ✅ Run tests from the **Verification Checklist** above
4. ✅ Review responses to understand order lifecycle
5. ✅ Test edge cases and error scenarios

Happy testing! 🚀
