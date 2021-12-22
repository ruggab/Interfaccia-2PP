package it.duepassicalzature.interfaccia.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class Attributes {

	private Integer id;
	private String name;
	private Integer position;
	private Boolean visible;
	private Boolean variation;
	private List<String> options;

	

}
