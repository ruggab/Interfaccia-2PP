package it.duepassicalzature.interfaccia.DTO;

import lombok.Data;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
@Data
public class OrdersDTO {

    @Id
    private Integer id;
    private String number;
    private String status;
    private String currency;
    private String dataCreated;
    private String total;
    private String billing_first_name;
    private String billing_last_name;
    private String billing_company;
    private String billing_address_1;
    private String billing_address_2;
    private String billing_city;
    private String billing_state;
    private String billing_postcode;
    private String billing_country;
    private String billing_email;
    private String billing_phone;
    private String shipping_first_name;
    private String shipping_last_name;
    private String shipping_company;
    private String shipping_address_1;
    private String shipping_address_2;
    private String shipping_city;
    private String shipping_state;
    private String shipping_postcode;
    private String shipping_country;
    private String payment_method;
    private String payment_method_title;
    private String datePaid;
    private String shipping_method_title;
    private String shipping_method_id;
    private String shipping_total;
    @OneToMany(cascade = CascadeType.ALL)
    private List<OrdersItem> productsOrder;

}
