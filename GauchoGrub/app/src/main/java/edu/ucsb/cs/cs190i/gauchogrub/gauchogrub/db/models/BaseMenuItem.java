package edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models;

import io.requery.Column;
import io.requery.Entity;
import io.requery.ForeignKey;
import io.requery.Generated;
import io.requery.Key;
import io.requery.ManyToMany;
import io.requery.ManyToOne;
import io.requery.PreInsert;
import io.requery.query.MutableResult;

/* Food item (in a many-to-many relationship with Menus) */
@Entity
public class BaseMenuItem {

    @Key
    @Generated
    public int id;

    @ForeignKey(references = BaseMenuCategory.class)
    @Column(nullable = false, index = true, unique = false)
    public int menuCategoryId;

    //@ManyToOne
    //public BaseMenuCategory menuCategory;

    @Column(nullable = false, index = true, unique = false)
    public String title;

    @ManyToMany(mappedBy = "menuItems")
    public MutableResult<BaseMenu> menus;

    @Column(nullable = false, index = true, unique = false, value = "false")
    public boolean isVegetarian;

    @Column(nullable = false, index = true, unique = false, value = "false")
    public boolean isVegan;

    @Column(nullable = false, index = true, unique = false, value = "false")
    public boolean hasNuts;

    @PreInsert
    void onPreInsert() {
        // Vegan implies vegetarian
        this.isVegetarian = (this.isVegetarian || this.isVegan);
    }

}
