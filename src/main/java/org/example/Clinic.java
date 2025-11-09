package org.example;

import org.hibernate.search.engine.backend.types.Sortable;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.*;

import java.util.List;
import java.util.UUID;

@SearchEntity
@Indexed
public class Clinic {

    @DocumentId
    public UUID id;

    @FullTextField(analyzer = "name")
    @KeywordField(name = "name_sort", sortable = Sortable.YES, normalizer = "sort")
    public String name;
    @FullTextField(analyzer = "name")
    @KeywordField(name = "description_sort", sortable = Sortable.YES, normalizer = "sort")
    public String description;
    @FullTextField(analyzer = "name")
    @KeywordField(name = "open_sort", sortable = Sortable.YES, normalizer = "sort")
    public String open;
    @FullTextField(analyzer = "name")
    @KeywordField(name = "close_sort", sortable = Sortable.YES, normalizer = "sort")
    public String close;
    @FullTextField(analyzer = "english")
    public List<String> paymentMethods;


    @ProjectionConstructor
    public Clinic(@IdProjection UUID id, String name, String description, String open, String close, List<String> paymentMethods) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.open = open;
        this.close = close;
        this.paymentMethods = paymentMethods;
    }
}

