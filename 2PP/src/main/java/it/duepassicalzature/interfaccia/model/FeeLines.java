package it.duepassicalzature.interfaccia.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeeLines {

    private Integer id;
    private String name;
    private String tax_class;
    private String tax_status;
    private String total;
    private String total_tax;
    private String amount;
    private List<Taxes> taxes;
    private List<MetaData> meta_data;

}
