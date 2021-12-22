
package it.duepassicalzature.interfaccia.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MetaData {

	private Integer id;
	private String key;
	private Object value;
	private Long version;
	private String display_key;
	private Object display_value;

	

}
