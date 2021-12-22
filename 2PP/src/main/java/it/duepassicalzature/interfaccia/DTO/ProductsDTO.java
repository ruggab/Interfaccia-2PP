package it.duepassicalzature.interfaccia.DTO;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Data
@Table(name = "products")
public class ProductsDTO {

    @Id
    private String id;
    private String code;
    private String name;
    private Double pricesell;
    private String category;
    private String target;
    private String codcol;
    private String codart;
    private String taglia;
    private Date lastupdate;
    private String lastaction;

}
