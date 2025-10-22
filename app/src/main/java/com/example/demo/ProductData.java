package com.example.demo;

import android.graphics.Color;
import java.util.ArrayList;
import java.util.List;

public class ProductData {
    private static final List<Product> allProducts = new ArrayList<>();
    public static final List<Product> cartList = new ArrayList<>();
    public static List<Product> getCartList() {
        // Sửa lỗi: Thêm dấu chấm phẩy ở cuối dòng
        if (cartList == null) {}
        return cartList;
    }

    // Gọi hàm này trong MainActivity hoặc lần đầu khi app khởi chạy
    public static void initializeData() {
        if (!allProducts.isEmpty()) return; // tránh khởi tạo lại nhiều lần

        // Cake
        allProducts.add(new Product("Strawberry Cheese", "85.000 VND / pc", R.drawable.cake_strawberry_cheese, Color.parseColor("#F1BCBC"),
                "This dessert features a buttery biscuit base topped with a silky smooth cream cheese layer. Fresh strawberry sauce adds a fruity sweetness, creating a perfect balance of tangy and creamy in every bite.","cake"));
        allProducts.add(new Product("Yellow Lemon Cheese", "90.000 VND / pc", R.drawable.cake_yellow_lemon_cheese, Color.parseColor("#A55102"),
                "A refreshing twist on classic cheesecake with a vibrant lemon curd topping. The creamy cheese layer blends perfectly with the zesty lemon flavor, giving each bite a light, citrusy finish.","cake"));
        allProducts.add(new Product("Blueberry Cheese", "85.000 VND / pc", R.drawable.cake_blueberry_cheese, Color.parseColor("#1F2831"),
                "Rich cream cheese paired with sweet, juicy blueberry compote. The fruity topping enhances the velvety cheesecake base, creating a burst of flavor that melts in your mouth.","cake"));
        allProducts.add(new Product("Tiramisu Chocolate", "75.000 VND / pc", R.drawable.cake_tiramisu_chocolate, Color.parseColor("#562715"),
                "This dessert still has the traditional lady finger cookies, and definitely still calls for that delicious mascarpone cheese-based layer of deliciousness, but in this chocolate version, the cookies are dunked in strong hot chocolate to soften them, and in between each cookie layer is a thick layer of rich, dark chocolate ganache.","cake"));
        allProducts.add(new Product("Tiramisu Matcha", "85.000 VND / pc", R.drawable.cake_tiramisu_matcha, Color.parseColor("#727C26"),
                "A delicate twist on the Italian classic — lady finger cookies soaked in smooth matcha syrup, layered with creamy mascarpone and a light dusting of earthy matcha powder. Balanced, aromatic, and elegant.","cake"));
        allProducts.add(new Product("Eclair Cake", "90.000 VND / pc", R.drawable.cake_eclair_cake, Color.parseColor("#4F252D"),
                "A soft, creamy delight made with layers of vanilla custard and light pastry. Topped with a glossy chocolate glaze that gives each spoonful a melt-in-your-mouth texture.","cake"));
        allProducts.add(new Product("Truffle Cake", "60.000 VND / pc", R.drawable.cake_truffle_cake, Color.parseColor("#FAE4B0"),
                "Decadent layers of moist chocolate sponge and smooth chocolate truffle cream. Intensely rich and silky, this cake is perfect for true chocolate lovers.","cake"));
        allProducts.add(new Product("Opera Chocolate", "75.000 VND / pc", R.drawable.cake_opera_chocolate, Color.parseColor("#4F252D"),
                "A sophisticated dessert made of thin almond sponge soaked in coffee syrup, layered with chocolate ganache and coffee buttercream, then finished with a shiny chocolate glaze. A harmony of bold and smooth flavors.","cake"));
        allProducts.add(new Product("Strawberry Donut", "50.000 VND / pc", R.drawable.cake_strawberry_donut, Color.parseColor("#E95B7C"),
                "Soft and fluffy donut covered with a sweet strawberry glaze. Finished with a glossy sheen and a hint of fresh berry aroma for a playful, fruity bite.","cake"));
        allProducts.add(new Product("Matcha Donut", "55.000 VND / pc", R.drawable.cake_matcha_donut, Color.parseColor("#8DA813"),
                "A pillowy donut with a silky matcha glaze, giving it a gentle bitterness that beautifully balances the sweetness. A perfect treat for matcha lovers.","cake"));
        allProducts.add(new Product("Egg Tart", "20.000 VND / pc", R.drawable.cake_egg_tart, Color.parseColor("#F4BD35"),
                "A crisp, buttery pastry shell filled with silky smooth egg custard, baked to a golden perfection. Lightly sweet, creamy, and incredibly satisfying.","cake"));
        allProducts.add(new Product("Mixed Macarons", "40.000 VND / pc", R.drawable.cake_mixed_macaron, Color.parseColor("#935427"),
                "Colorful, delicate almond shells with a crisp exterior and chewy center, filled with assorted flavors of creamy ganache and buttercream. A bite-sized symphony of sweetness.","cake"));
        allProducts.add(new Product("Original Croissant", "35.000 VND / pc", R.drawable.cake_croissant, Color.parseColor("#964E12"),
                "A flaky, golden-brown pastry with buttery layers that melt in your mouth. Lightly crisp on the outside, soft and airy on the inside — a true classic.","cake"));
        allProducts.add(new Product("Chocolate Croissant", "45.000 VND / pc", R.drawable.cake_chocolate_croissant, Color.parseColor("#562715"),
                "A buttery, flaky croissant wrapped around a rich chocolate filling. The layers are crisp, the center is sweet and smooth, making it a perfect indulgence with every bite.","cake"));

        // Drink
        allProducts.add(new Product("Strawberry Smooth", "100.000 VND / pc", R.drawable.drink_strawberry_smooth, Color.parseColor("#A71317"),
                "A refreshing blend of ripe strawberries and creamy milk, creating a smooth, sweet, and tangy drink that’s both cooling and delightful.","drink"));
        allProducts.add(new Product("Caramel Smooth", "85.000 VND / pc", R.drawable.drink_caramel_smooth, Color.parseColor("#E68E09"),
                "Rich caramel blended with fresh milk for a silky-smooth texture and a deep, buttery sweetness that melts perfectly on your tongue.","drink"));
        allProducts.add(new Product("Oreo Smooth", "85.000 VND / pc", R.drawable.drink_oreo_smooth, Color.parseColor("#220203"),
                "Crunchy Oreo cookies blended into creamy milk, giving you a thick, chocolatey smoothie topped with bits of cookie goodness in every sip.","drink"));
        allProducts.add(new Product("Blueberry Smooth", "100.000 VND / pc", R.drawable.drink_blueberry_smooth, Color.parseColor("#865163"),
                "Sweet and slightly tart blueberries blended into a smooth, creamy drink that bursts with fruity freshness and a beautiful natural color.","drink"));
        allProducts.add(new Product("Matcha Latte", "100.000 VND / pc", R.drawable.drink_matcha_latte, Color.parseColor("#526218"),
                "Premium matcha whisked to perfection with fresh milk, delivering a balanced taste of earthy bitterness and creamy sweetness in one calming sip.","drink"));
        allProducts.add(new Product("Chocolate Milk Tea", "100.000 VND / pc", R.drawable.drink_chocolate_milk_tea, Color.parseColor("#D7A95E"),
                "A luscious mix of rich cocoa and fragrant black tea, combined with fresh milk for a perfect harmony of smooth chocolate flavor and gentle tea aroma.","drink"));
        allProducts.add(new Product("Black Ice Coffee", "60.000 VND / pc", R.drawable.drink_black_ice_coffee, Color.parseColor("#220203"),
                "Strongly brewed black coffee served over ice — bold, aromatic, and refreshingly bitter, perfect for coffee lovers who like it pure and intense.","drink"));
        allProducts.add(new Product("Milk Coffee", "70.000 VND / pc", R.drawable.drink_milk_coffee, Color.parseColor("#964E12"),
                "Smooth Vietnamese coffee mixed with creamy condensed milk, creating a bold yet sweet flavor that’s both energizing and comforting.","drink"));
        allProducts.add(new Product("Hot Coffee", "50.000 VND / pc", R.drawable.drink_hot_coffee, Color.parseColor("#220203"),
                "A classic cup of freshly brewed hot coffee with a deep aroma and balanced flavor — simple, strong, and satisfying.","drink"));
        allProducts.add(new Product("Green Tea", "50.000 VND / pc", R.drawable.drink_green_tea, Color.parseColor("#4C4116"),
                "Fragrant green tea brewed gently to preserve its fresh, grassy notes and light sweetness. A refreshing drink for a peaceful moment.","drink"));
        allProducts.add(new Product("Guava Tea", "70.000 VND / pc", R.drawable.drink_guava_tea, Color.parseColor("#FBA79E"),
                "A tropical twist on tea — infused with the sweet and slightly tangy flavor of guava, creating a light, fruity refreshment.","drink"));
        allProducts.add(new Product("Longan Tea", "70.000 VND / pc", R.drawable.drink_longan_tea, Color.parseColor("#D7A95E"),
                "A soothing herbal tea steeped with dried longan, giving it a natural sweetness and a delicate, honey-like aroma that’s both calming and comforting.","drink"));

        // Food
        allProducts.add(new Product("Sandwich", "₫50.000", R.drawable.food_sandwich, Color.parseColor("#E1B55C"),
                "Freshly toasted bread filled with layers of ham, cheese, and crisp vegetables. The combination of soft bread, creamy sauce, and fresh ingredients makes every bite light, flavorful, and satisfying.","food"));
        allProducts.add(new Product("Fried Chicken Burger", "₫60.000", R.drawable.food_burger, Color.parseColor("#A55102"),
                "A crispy fried chicken fillet coated in golden breadcrumbs, layered with fresh lettuce, tomato, and creamy sauce inside a soft burger bun. Juicy, crunchy, and packed with irresistible flavor.","food"));
    }

    public static List<Product> getAllProducts() {
        return allProducts; // trả về tất cả sản phẩm
    }

    // Lấy sản phẩm theo danh mục (cake / drink / food)
    public static List<Product> getProductsByCategory(String category) {
        List<Product> list = new ArrayList<>();
        for (Product p : allProducts) {
            if (p.getCategory().equalsIgnoreCase(category)) list.add(p);
        }
        return list;
    }
}
