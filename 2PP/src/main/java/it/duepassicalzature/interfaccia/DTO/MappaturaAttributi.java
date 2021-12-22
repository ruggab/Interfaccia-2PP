package it.duepassicalzature.interfaccia.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class MappaturaAttributi {

    @Id
    private Integer idTerms;
    private String name;
    private Integer parent_idWC;

}
