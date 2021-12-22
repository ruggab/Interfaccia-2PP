package it.duepassicalzature.interfaccia.repository;

import it.duepassicalzature.interfaccia.model.DataUltimoAggiornamento;
import it.duepassicalzature.interfaccia.model.ICategoriesWeb;
import it.duepassicalzature.interfaccia.model.IProductsWeb;
import it.duepassicalzature.interfaccia.model.ProductsWeb;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

public interface DataAggiornamentoRepository extends JpaRepository<DataUltimoAggiornamento, Integer> {

    @Query(value = "SELECT MIN(id) as id, MIN(code) as code, MIN(name) as name, MAX(pricesell) as pricesell, MAX(category) as category, MAX(category_name) as category_name, \n" +
            "TRIM(codart) as codart,  MAX(codcol) as codcol, MAX(desccol) as desccol, MIN(taglia) as taglia, MAX(target) as target, MAX(brand) as brand,\n" +
            "MAX(descbreve) as descbreve, MAX(titolo) as titolo, MAX(percorsodesc) as percorsodesc, MAX(listinoweb1) as listinoweb1,\n" +
            "MAX(lastupdate) as lastupdate, MAX(lastaction) as lastaction, SUM(giacenza) as giacenza, MAX(cat_json) as cat_json, MAX(percentuale) as percentuale, MAX(stagione) as stagione, MAX(eanprod) as eanprod \n" +
            "FROM public.vproducts_web\n" +
            "where codart is not null AND codart NOT LIKE '% %' and code NOT IN (select sku from mappatura_prod_id_sku where parent_idwc is not null)\n" +
            " AND lastupdate > (SELECT MAX(data_ultimo_aggiornamento)\n " +
            "FROM public.data_ultimo_aggiornamento) " +
            "group by TRIM(codart) \n" +
            " ; ", nativeQuery = true)
    List<IProductsWeb> getAllProductsByCodart(); //Togliere and category LIKE ----- AND CODART 036643_1.jpg  037385_3.jpg037915_2.jpgAND codart LIKE '0379%'

    @Query(value = "SELECT MIN(id) as id, MIN(code) as code, MIN(name) as name, MAX(pricesell) as pricesell, MAX(category) as category, MAX(category_name) as category_name, \n" +
            "TRIM(codart) as codart,  MAX(codcol) as codcol, MAX(desccol) as desccol, MIN(taglia) as taglia, MAX(target) as target, MAX(brand) as brand,\n" +
            "MAX(descbreve) as descbreve, MAX(titolo) as titolo, MAX(percorsodesc) as percorsodesc, MAX(listinoweb1) as listinoweb1,\n" +
            "MAX(lastupdate) as lastupdate, MAX(lastaction) as lastaction, SUM(giacenza) as giacenza, MAX(cat_json) as cat_json\n" +
            "FROM public.vproducts_web\n" +
            "where codart is not null AND codart NOT LIKE '% %' \n" +
            "group by TRIM(codart) ; ", nativeQuery = true)
    List<IProductsWeb> getAllProductsGrouped();

    @Query(value = "SELECT id_woo_commerce FROM public.mappatura_prod_id_sku where sku = ?1 ; ", nativeQuery = true)
    Integer getWCidByCodart(String codart);

    @Query(value = "SELECT id, name, slug, parentid, image\n" +
            "\tFROM public.vcategories_web \n" +
            "\twhere parentid is null\n" +
            "\torder by id;", nativeQuery = true)
    List<ICategoriesWeb> getAllParentCategories();

    @Query(value = "SELECT id, name, slug, parentid, image\n" +
            "\tFROM public.vcategories_web \n" +
            "\twhere parentid = ?1 \n" +
            "\torder by id;", nativeQuery = true)
    List<ICategoriesWeb> getAllChildCategoriesByParent(String parentId);

    @Query(value = "select * from public.vcategories_web ; ", nativeQuery = true)
    List<ICategoriesWeb> getAllCategories();

    @Query(value = "select id_woo_commerce from public.mappatura_prod_id_sku where sku in (\n" +
            "select distinct codart from vproducts_web where category like '9999-1-%')", nativeQuery = true)
    List<Integer> getScarpeDonna();



}
