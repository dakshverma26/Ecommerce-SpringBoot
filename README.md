# Ecommerce Spring Boot Application

A fully migrated Spring Boot eCommerce application converted from a legacy Java Servlet/JSP project.

## Tech Stack
- **Spring Boot 3.2** — Spring MVC, Spring Data JPA, Spring Security
- **MySQL 8+** — Production database
- **H2** — In-memory DB for unit tests
- **Thymeleaf** — Server-side HTML templates
- **Hibernate** — JPA ORM (auto DDL)
- **Lombok** — Boilerplate reduction
- **BCrypt** — Password hashing

## Features

| Role | Capabilities |
|------|-------------|
| 🛍️ Buyer | Register, login, browse products, add to cart, place order, track orders |
| 📦 Seller | Register, login, add/edit/delete products with image upload |
| 🛡️ Admin | View platform stats, confirm orders, see all sellers/buyers |

## Prerequisites
- Java 17+
- Maven 3.8+
- MySQL 8+ running on `localhost:3306`

## Setup & Run

### 1. Database
MySQL database `ecommerce_db` is auto-created on first run via:
```properties
spring.datasource.url=...?createDatabaseIfNotExist=true
spring.jpa.hibernate.ddl-auto=update
```

Default credentials (configured in `application.properties`):
```
username: root
password: root
```

### 2. Admin Account
Admin user is seeded automatically via `src/main/resources/data.sql`:
```
username: admin
password: admin123
```

### 3. Build & Run

```bash
cd ecommerce-springboot

# Build (skip tests for quick start)
mvn clean install -DskipTests

# Run the application
mvn spring-boot:run
```

Application starts at: **http://localhost:8080**

### 4. Run Tests

Tests use H2 in-memory database — no MySQL required:
```bash
mvn test
```

## Project Structure

```
src/main/java/com/ecommerce/
├── config/          SecurityConfig, WebMvcConfig, SessionGuardInterceptor
├── controller/      AuthController, SellerController, BuyerController, AdminController
├── service/         SellerService, BuyerService, AdminService, OrderService, FileService
├── repository/      SellerRepository, BuyerRepository, ProductRepository, ...
├── entity/          Seller, Buyer, Product, Order, OrderItem, AdminConfirmation, Admin
├── dto/             LoginDto, SellerRegisterDto, BuyerRegisterDto, ProductDto, CartItemDto
└── exception/       GlobalExceptionHandler, ResourceNotFoundException

src/main/resources/
├── templates/
│   ├── seller/      login, register, dashboard, products, add-product, edit-product
│   ├── buyer/       login, register, dashboard, cart, payment, order-success, orders
│   └── admin/       login, dashboard, confirmations, sellers, buyers
├── static/css/      style.css
├── application.properties
└── data.sql         (admin user seed)
```

## Architecture

```
[Browser] → [Thymeleaf Template]
              ↕ HTTP
[Controller] → [Service] → [Repository] → [MySQL DB]
                  ↕
            [SessionGuardInterceptor]  ← role-based URL protection
            [GlobalExceptionHandler]  ← centralized error handling
```

## Security Design

URL access is enforced via `SessionGuardInterceptor`:
- `/seller/**` → requires `SELLER_EMAIL` session attribute
- `/buyer/**`  → requires `BUYER_EMAIL` session attribute  
- `/admin/**`  → requires `ADMIN_USER` session attribute
- Login/register pages → public

> **Note**: Cart is session-based. Future improvement: migrate to Redis for distributed production environments.

## Key Migration Changes from Legacy

| Legacy | Spring Boot |
|--------|-------------|
| Servlet `@WebServlet` | `@Controller` with `@PostMapping` |
| `request.getParameter()` | `@ModelAttribute` / `@RequestParam` |
| Raw JDBC / `ResultSet` | Spring Data JPA Repositories |
| Dynamic `seller_EMAIL` tables | Normalized `products` table with `seller_id` FK |
| JSP scriptlets | Thymeleaf templates |
| Plain-text passwords | BCrypt hashing |
| No exception handling | `@ControllerAdvice` GlobalExceptionHandler |
| Hardcoded file paths | Configurable `app.upload.dir` |

## API Endpoints

| Method | URL | Description |
|--------|-----|-------------|
| GET | `/` | Home / role selector |
| GET/POST | `/seller/login` | Seller login |
| GET/POST | `/seller/register` | Seller register |
| GET | `/seller/dashboard` | Seller dashboard |
| GET/POST | `/seller/product/add` | Add product |
| GET/POST | `/seller/product/edit/{id}` | Edit product |
| POST | `/seller/product/delete/{id}` | Delete product |
| GET/POST | `/buyer/login` | Buyer login |
| GET/POST | `/buyer/register` | Buyer register |
| GET | `/buyer/dashboard` | Browse products |
| POST | `/buyer/cart/add` | Add to cart |
| GET | `/buyer/cart` | View cart |
| POST | `/buyer/cart/remove` | Remove item |
| GET | `/buyer/payment` | Payment page |
| POST | `/buyer/order/place` | Place order |
| GET | `/buyer/orders` | My orders |
| GET/POST | `/admin/login` | Admin login |
| GET | `/admin/dashboard` | Admin dashboard |
| GET | `/admin/confirmations` | View all orders |
| POST | `/admin/confirm/{id}` | Confirm an order |
| GET | `/admin/sellers` | View all sellers |
| GET | `/admin/buyers` | View all buyers |
| GET | `/logout` | Logout (all roles) |

## Target Repository
https://github.com/dakshverma26/Ecommerce-SpringBoot
