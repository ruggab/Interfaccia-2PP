package it.duepassicalzature.interfaccia.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data @AllArgsConstructor @NoArgsConstructor
public class TaxLines {

    private Integer id;
    private String rate_code;
    private String rate_id;
    private String label;
    private String compound;
    private String tax_total;
    private String shipping_tax_total;
    private List<MetaData> meta_data;

}
