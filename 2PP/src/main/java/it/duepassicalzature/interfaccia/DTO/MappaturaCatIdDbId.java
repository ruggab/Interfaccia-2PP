package it.duepassicalzature.interfaccia.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data @AllArgsConstructor @NoArgsConstructor
public class MappaturaCatIdDbId {

    @Id
    private String idDatabase;
    private Integer idWooCommerce;

}
