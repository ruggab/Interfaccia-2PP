package it.duepassicalzature.interfaccia.repository;

import it.duepassicalzature.interfaccia.DTO.MappaturaAttributi;
import it.duepassicalzature.interfaccia.model.IVariationWeb;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MappaturaAttributiRepository extends JpaRepository<MappaturaAttributi, Integer> {

    @Query(value = "SELECT DISTINCT(desccol) FROM public.vproducts_web\n" +
            "WHERE desccol NOT IN (select name from mappatura_attributi) ;", nativeQuery = true)
    List<String> getAllNewColors();

    @Query(value = "SELECT DISTINCT(brand) FROM public.vproducts_web \n" +
            "WHERE brand NOT IN (select name from mappatura_attributi) ; ", nativeQuery = true)
    List<String> getAllNewBrands();

    @Query(value = "SELECT DISTINCT(taglia) FROM public.vproducts_web \n" +
            "WHERE taglia NOT IN (select name from mappatura_attributi) ; ", nativeQuery = true)
    List<String> getAllNewSizes();

    @Query(value = "SELECT id, code, codart, desccol, taglia, brand, giacenza, eanprod\n" +
            "\tFROM public.vproducts_web\n" +
            "\twhere codart = ?1 ;", nativeQuery = true)
    List<IVariationWeb> getTaglieColByCodArt(String codart);

    @Query(value = "SELECT id, code, codart, desccol, taglia, brand, giacenza\n" +
            "\tFROM public.vproducts_web\n" +
            "\twhere codart = ?1 AND lastupdate > (SELECT MAX(data_ultimo_aggiornamento)\n" +
            "FROM public.data_ultimo_aggiornamento) and code NOT IN (select sku from mappatura_prod_id_sku where parent_idwc is not null) ;", nativeQuery = true)
    List<IVariationWeb> getTaglieColByCodArtToInsert(String codart);

    @Query(value = "SELECT DISTINCT(stagione) FROM public.vproducts_web \n" +
            "WHERE stagione NOT IN (select name from mappatura_attributi) ; ", nativeQuery = true)
    List<String> getAllNewSeasons();
}
