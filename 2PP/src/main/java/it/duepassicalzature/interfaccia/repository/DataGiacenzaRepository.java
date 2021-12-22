package it.duepassicalzature.interfaccia.repository;

import it.duepassicalzature.interfaccia.model.DataUltimaGiacenza;
import it.duepassicalzature.interfaccia.model.IGiacenzaWeb;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DataGiacenzaRepository extends JpaRepository<DataUltimaGiacenza, Integer> {

    @Query(value = "select * from vgiacenze_web where parent_idwc is null and stockdate > (select data_aggiornamento_giacenza from data_ultima_giacenza) ; ", nativeQuery = true)
    List<IGiacenzaWeb> listaGiacenzePadri();

    @Query(value = "select * from vgiacenze_web where parent_idwc is not null and stockdate > (select data_aggiornamento_giacenza from data_ultima_giacenza) ; ", nativeQuery = true)
    List<IGiacenzaWeb> listaGiacenzeVariazioni();

}
