package com.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Cart item stored in the HTTP session.
 *
 * NOTE: Session-based cart is suitable for this project scope.
 * Future improvement: move to Redis or DB-backed cart for distributed/production environments.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDto implements Serializable {

    private Long productId;
    private String productName;
    private String sellerName;
    private String category;
    private int price;
    private int quantity;       // quantity the buyer wants
    private int stockAvailable; // max available from inventory
    private String imagePath;
    private String imageName;

    public int getSubtotal() {
        return price * quantity;
    }
}
