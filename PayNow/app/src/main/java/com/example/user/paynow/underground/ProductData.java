package com.example.user.paynow.underground;

//상품 code/name/price의 getter와 setter

public class ProductData {
    private String product_code;
    private String product_name;
    private String product_price;

    public String getProduct_code() {
        return product_code;
    }

    public String getProduct_name() {
        return product_name;
    }

    public String getProduct_price() {
        return product_price;
    }

    public void setMember_code(String member_id) {
        this.product_code = member_id;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }

    public void setMember_price(String member_address) {
        this.product_price = member_address;
    }
}