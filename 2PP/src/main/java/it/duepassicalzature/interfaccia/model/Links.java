
package it.duepassicalzature.interfaccia.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data @AllArgsConstructor @NoArgsConstructor
public class Links {


	private Integer id;
    private List<Self> self;
    private List<Collection> collection;
    private List<Customer> customer;

}
