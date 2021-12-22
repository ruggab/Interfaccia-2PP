
package it.duepassicalzature.interfaccia.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data @AllArgsConstructor @NoArgsConstructor
public class ShippingLine {

    private Integer id;
    private String method_title;
    private String method_id;
    private String instance_id;
    private String total;
    private String total_tax;
    private List<Taxes> taxes;
    private List<MetaData> meta_data;

}
