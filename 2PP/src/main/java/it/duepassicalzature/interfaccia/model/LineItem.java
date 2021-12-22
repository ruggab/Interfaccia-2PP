
package it.duepassicalzature.interfaccia.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LineItem {

	private Integer id;
	private String name;
	private Integer product_id;
	private Integer variation_id;
	private Integer quantity;
	private String tax_class;
	private String subtotal;
	private String subtotal_tax;
	private String total;
	private String total_tax;
	private List<Taxes> taxes;
	private List<MetaData> meta_data;
	private String sku;
	private Float price;
	private String parent_name;

	

}
