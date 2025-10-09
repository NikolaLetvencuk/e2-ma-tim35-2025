package com.example.dailyboss.domain.repository;

import com.example.dailyboss.domain.model.Category;

import java.util.List;

public interface ICategoryRepository {
    /**
     * Dodaje novu kategoriju nakon validacije.
     * @param name naziv kategorije (ne sme biti prazan)
     * @param color boja kategorije (ne sme biti zauzeta)
     * @return true ako je uspešno dodat, false ako nije
     * @throws IllegalArgumentException ako validacija nije prošla
     */
    boolean addCategory(String name, String color) throws IllegalArgumentException;

    /**
     * Vraća sve kategorije iz baze.
     * @return lista kategorija
     */
    List<Category> getAllCategories();

    /**
     * Menja boju postojeće kategorije ako nova boja nije zauzeta.
     * @param id ID kategorije za izmenu
     * @param newColor nova boja
     * @return true ako je uspešno ažurirano
     * @throws IllegalArgumentException ako je boja zauzeta
     */
    boolean updateCategoryColor(String id, String newColor) throws IllegalArgumentException;

    /**
     * Vraća boju kategorije po njenom ID-ju.
     * @param id ID kategorije
     * @return heksadecimalna vrednost boje
     */
    String getColorById(String id);
}
