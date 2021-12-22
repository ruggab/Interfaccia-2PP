package it.duepassicalzature.interfaccia.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductsWeb {

	private String id;
	private String code;
	private String name;
	private Double pricesell;
	private String category;
	private String category_name;
	private String codart;
	private String codcol;
	private String desccol;
	private String taglia;
	private String target;
	private String brand;
	private String descbreve;
	private String titolo;
	private String percorsodesc;
	private Double listinoweb1;
	private Date lastupdate;
	private String lastaction;
	private Integer giacenza;
	private String catJson;
	private String stagione;
	private String percentuale;
	private String eanprod;
	
	
	

	
}
