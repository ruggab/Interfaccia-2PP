package it.duepassicalzature.interfaccia.DTO;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class OrdersItem {

    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id_gen;
    private Integer id;
    private String name;
    private Integer product_id;
    private Integer variation_id;
    private Integer quantity;
    private String total;
    private String sku;
    private Float price;

}
