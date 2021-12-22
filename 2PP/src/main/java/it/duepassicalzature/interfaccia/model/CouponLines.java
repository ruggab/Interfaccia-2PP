package it.duepassicalzature.interfaccia.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CouponLines {

    private Integer id;
    private String code;
    private String discount;
    private String discount_tax;
    private List<MetaData> meta_data;

}
