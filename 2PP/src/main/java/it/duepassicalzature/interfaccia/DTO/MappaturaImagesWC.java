package it.duepassicalzature.interfaccia.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MappaturaImagesWC {

    @Id
    private String id;
    private Integer idWooCommerce;
    private String sku;
    private Date lastUpdate;

}
