package it.duepassicalzature.interfaccia.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

@Entity
@Data
public class DataUltimoAggiornamento {

	@Id
	private Integer id;
	private Date data_ultimo_aggiornamento;

	

}
