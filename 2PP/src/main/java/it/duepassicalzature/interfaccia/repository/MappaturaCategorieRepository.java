package it.duepassicalzature.interfaccia.repository;

import it.duepassicalzature.interfaccia.DTO.MappaturaCatIdDbId;
import it.duepassicalzature.interfaccia.model.ICategoriesWeb;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MappaturaCategorieRepository extends JpaRepository<MappaturaCatIdDbId, String> {

    @Query(value = "SELECT id_database, id_woo_commerce\n" +
            "\tFROM public.mappatura_cat_id_db_id where id_database = ?1 ;", nativeQuery = true)
    MappaturaCatIdDbId getParentWC(String parentId);

    @Query(value = "SELECT id, name, slug, parentid \n" +
            "\tFROM public.vcategories_web where id NOT IN (SELECT id_database FROM public.mappatura_cat_id_db_id) ; ", nativeQuery = true)
    List<ICategoriesWeb> categorieDaAggiungere();

}
