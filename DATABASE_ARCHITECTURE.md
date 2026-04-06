# DATABASE ARCHITECTURE AND DATA FLOW DOCUMENTATION

## 1. Entity-Relationship Diagrams (ERD)

![ERD](link_to_erd_image)

### Entities
- **User**: Represents the users in the system with attributes like `user_id`, `name`, `email`, `password`, etc.
- **Product**: Represents products with attributes like `product_id`, `name`, `description`, `price`, etc.
- **Order**: Represents orders with attributes like `order_id`, `user_id`, `order_date`, etc.
- **OrderItem**: Represents items in an order with attributes `order_item_id`, `order_id`, `product_id`, `quantity`, etc.

## 2. Data Flow Diagrams (DFD)

![DFD](link_to_dfd_image)

### Processes
1. **User Registration**: Flow of data for a new user registration.
2. **Order Processing**: Flow of data from adding products to the cart to completing the order.

## 3. CRUD Analysis

| Entity     | Create         | Read               | Update         | Delete         |
|------------|----------------|--------------------|----------------|----------------|
| User       | Register user   | Fetch user details  | Update user info| Delete user    |
| Product    | Add new product | Fetch product list  | Update product  | Delete product  |
| Order      | Place order     | Retrieve orders     | Update order    | Cancel order    |
| OrderItem  | Add to cart     | View order items    | Update item     | Remove item     |

## 4. Detailed Schema Documentation

### User Table
- **user_id** (INT, Primary Key)
- **name** (VARCHAR)
- **email** (VARCHAR, UNIQUE)
- **password** (VARCHAR)

### Product Table
- **product_id** (INT, Primary Key)
- **name** (VARCHAR)
- **description** (TEXT)
- **price** (DECIMAL)

### Order Table
- **order_id** (INT, Primary Key)
- **user_id** (INT, Foreign Key)
- **order_date** (DATETIME)

### OrderItem Table
- **order_item_id** (INT, Primary Key)
- **order_id** (INT, Foreign Key)
- **product_id** (INT, Foreign Key)
- **quantity** (INT)

## 5. Conclusion

This document outlines the database architecture and data flow within the e-commerce application, providing clear guidance on entity relationships, data flow, and CRUD operations.