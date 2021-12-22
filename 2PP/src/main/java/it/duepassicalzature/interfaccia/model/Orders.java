
package it.duepassicalzature.interfaccia.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data @AllArgsConstructor @NoArgsConstructor
public class Orders {

    private Integer id;
    private Integer parent_id;
    private String number;
    private String order_key;
    private String created_via;
    private String version;
    private String status;
    private String currency;
    private String date_created;
    private String date_created_gmt;
    private String date_modified;
    private String date_modified_gmt;
    private String discount_total;
    private String discount_tax;
    private String shipping_total;
    private String shipping_tax;
    private String cart_tax;
    private String total;
    private String total_tax;
    private Boolean prices_include_tax;
    private Integer customer_id;
    private String customer_ip_address;
    private String customer_user_agent;
    private String customer_note;
    private Billing billing;
    private Shipping shipping;
    private String payment_method;
    private String payment_method_title;
    private String transaction_id;
    private String date_paid;
    private String date_paid_gmt;
    private String date_completed;
    private String date_completed_gmt;
    private String cart_hash;
    private List<MetaData> meta_data;
    private List<LineItem> line_items;
	private List<TaxLines> tax_lines;
	private List<ShippingLine> shipping_lines;
    private List<FeeLines> fee_lines;
    private List<CouponLines> coupon_lines;
    private List<Refunds> refunds;
    private String currency_symbol;
	private Links _links;
	
	
	

}
