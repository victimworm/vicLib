package giaohangtietkiem.vn.printer.Objects;

import java.util.ArrayList;

public class Package {

    public String id = "";
    public String order = "290019543";
    public String alias = "S23304.HN11.Q12.290019543";
    public String deliver_cart_alias = "HN11.Q12";
    public String client_id = "";
    public String shop_name = "";
    public String created_username = "S23304";
    public String shop_code = "S23304";
    public String customer_fullname = "BB";
    public String customer_tel = "0987654321";
    public String customer_first_address = "B";
    public String customer_last_address = "Xã Đồng Tháp, Đan Phượng, Hà Nội";
    public String product_name = "";
    public ArrayList<String> productList = new ArrayList<>();

    public Package() {
        productList.add("Bb");
    }
}
