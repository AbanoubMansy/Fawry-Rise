import java.util.*;
import java.time.LocalDate;

// Interface for items
interface Shippable {
    String getName();
    double getWeight();
}

class Product {
    String name;
    double price;
    int quantity;
    boolean isExpirable;
    LocalDate expiryDate;
    boolean isShippable;
    double weight;

    public Product(String name, double price, int quantity, boolean isExpirable, LocalDate expiryDate, boolean isShippable, double weight) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.isExpirable = isExpirable;
        this.expiryDate = expiryDate;
        this.isShippable = isShippable;
        this.weight = weight;
    }

    public boolean isExpired() {
        return isExpirable && LocalDate.now().isAfter(expiryDate);
    }
}

// Customer class
class Customer {
    String name;
    double balance;

    public Customer(String name, double balance) {
        this.name = name;
        this.balance = balance;
    }

    public void deductBalance(double amount) {
        balance -= amount;
    }
}

// Cart class
class Cart {
    Map<Product, Integer> items = new HashMap<>();

    public void addProduct(Product p, int qty) {
        if (qty <= 0) {
            throw new IllegalArgumentException("Quantity must be positive.");
        }
        if (p.quantity < qty) {
            throw new IllegalArgumentException("Not enough stock for: " + p.name);
        }
        items.put(p, items.getOrDefault(p, 0) + qty);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public Map<Product, Integer> getItems() {
        return items;
    }
}

// ShippingService class
class ShippingService {
    public static void ship(List<Shippable> items) {
        System.out.println("** Shipment notice **");
        double totalWeight = 0.0;
        for (Shippable item : items) {
            System.out.printf("%sx %-12s %.0fg\n", 1, item.getName(), item.getWeight() * 1000);
            totalWeight += item.getWeight();
        }
        System.out.printf("Total package weight %.1fkg\n", totalWeight);
    }
}

// CheckoutService class
class CheckoutService {
    private static final double SHIPPING_RATE_PER_KG = 30.0; // Flat shipping for simplicity

    public static void checkout(Customer customer, Cart cart) {
        if (cart.isEmpty()) {
            throw new IllegalStateException("Cart is empty.");
        }

        double subtotal = 0.0;
        double totalWeight = 0.0;
        List<Shippable> shippables = new ArrayList<>();

        for (Map.Entry<Product, Integer> entry : cart.getItems().entrySet()) {
            Product p = entry.getKey();
            int qty = entry.getValue();

            if (p.isExpired()) {
                throw new IllegalStateException("Product expired: " + p.name);
            }

            if (p.quantity < qty) {
                throw new IllegalStateException("Product out of stock: " + p.name);
            }

            subtotal += p.price * qty;

            if (p.isShippable) {
                for (int i = 0; i < qty; i++) {
                    double finalWeight = p.weight;
                    String finalName = p.name;
                    shippables.add(new Shippable() {
                        public String getName() { return finalName; }
                        public double getWeight() { return finalWeight; }
                    });
                    totalWeight += finalWeight;
                }
            }
        }

        double shippingFee = totalWeight > 0 ? SHIPPING_RATE_PER_KG : 0;
        double total = subtotal + shippingFee;

        if (customer.balance < total) {
            throw new IllegalStateException("Insufficient balance.");
        }

        // Deduct quantity
        for (Map.Entry<Product, Integer> entry : cart.getItems().entrySet()) {
            Product p = entry.getKey();
            int qty = entry.getValue();
            p.quantity -= qty;
        }

        // Deduct from balance
        customer.deductBalance(total);

        if (!shippables.isEmpty()) {
            ShippingService.ship(shippables);
        }

        // Print receipt
        System.out.println("\n** Checkout receipt **");
        for (Map.Entry<Product, Integer> entry : cart.getItems().entrySet()) {
            Product p = entry.getKey();
            int qty = entry.getValue();
            System.out.printf("%dx %-12s %.0f\n", qty, p.name, p.price * qty);
        }
        System.out.println("----------------------");
        System.out.printf("Subtotal         %.0f\n", subtotal);
        System.out.printf("Shipping         %.0f\n", shippingFee);
        System.out.printf("Amount           %.0f\n", total);
        System.out.printf("Remaining Balance %.0f\n", customer.balance);
    }
}
