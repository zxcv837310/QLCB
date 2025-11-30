package com.java.m3.demo.model;

import java.util.ArrayList;
import java.util.List;

public class Cart {
    private List<CartItem> items = new ArrayList<>();

    public void addItem(Menu menu) {
        // Kiểm tra xem món này đã có trong giỏ chưa
        for (CartItem item : items) {
            if (item.getMenu().getId().equals(menu.getId())) {
                item.setQuantity(item.getQuantity() + 1); // Tăng số lượng
                return;
            }
        }
        // Chưa có thì thêm mới
        items.add(new CartItem(menu, 1));
    }

    public void updateItem(String menuId, int quantity) {
        for (CartItem item : items) {
            if (item.getMenu().getId().equals(menuId)) {
                if (quantity <= 0) {
                    items.remove(item);
                } else {
                    item.setQuantity(quantity);
                }
                return;
            }
        }
    }
    
    public void removeItem(String menuId) {
        items.removeIf(item -> item.getMenu().getId().equals(menuId));
    }

    public List<CartItem> getItems() {
        return items;
    }

    // Tổng số lượng sản phẩm (hiển thị trên icon giỏ hàng)
    public int getTotalQuantity() {
        return items.stream().mapToInt(CartItem::getQuantity).sum();
    }

    // Tổng tiền của cả giỏ hàng
    public double getTotalAmount() {
        return items.stream().mapToDouble(CartItem::getTotalPrice).sum();
    }
}