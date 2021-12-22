package it.duepassicalzature.interfaccia.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

@Entity
@Data
public class DataUltimaGiacenza {

	@Id
	private Integer id;
	private Date dataAggiornamentoGiacenza;

	

}
