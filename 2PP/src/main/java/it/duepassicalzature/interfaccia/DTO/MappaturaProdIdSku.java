package it.duepassicalzature.interfaccia.DTO;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

@Entity
@Data
public class MappaturaProdIdSku {

    @Id
    private Integer idWooCommerce;
    private String sku;
    private Integer parentIdWC;
    private Date lastUpdate;

}
