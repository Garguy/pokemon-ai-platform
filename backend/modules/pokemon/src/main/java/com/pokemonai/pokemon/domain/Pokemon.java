package com.pokemonai.pokemon.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "pokemon")
public class Pokemon {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "external_id", nullable = false, unique = true)
    private int externalId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    // Comma-separated PokeAPI type slugs, e.g. "grass,poison". Populated by sync.
    @Column(name = "types")
    private String typesCsv;

    @Column(name = "height")
    private Integer height; // decimetres (PokeAPI unit)

    @Column(name = "weight")
    private Integer weight; // hectograms (PokeAPI unit)

    @Column(name = "category")
    private String category;

    @Column(name = "hp")
    private Integer hp;

    @Column(name = "attack")
    private Integer attack;

    @Column(name = "defense")
    private Integer defense;

    @Column(name = "special_attack")
    private Integer specialAttack;

    @Column(name = "special_defense")
    private Integer specialDefense;

    @Column(name = "speed")
    private Integer speed;

    @Column(name = "synced_at", nullable = false)
    private OffsetDateTime syncedAt = OffsetDateTime.now();

    protected Pokemon() {}

    public Pokemon(int externalId, String name, String description, String imageUrl) {
        this.externalId = externalId;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public UUID getId() { return id; }
    public int getExternalId() { return externalId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public OffsetDateTime getSyncedAt() { return syncedAt; }

    public List<String> getTypes() {
        return (typesCsv == null || typesCsv.isBlank()) ? List.of() : List.of(typesCsv.split(","));
    }

    public Integer getHeight() { return height; }
    public Integer getWeight() { return weight; }
    public String getCategory() { return category; }
    public Integer getHp() { return hp; }
    public Integer getAttack() { return attack; }
    public Integer getDefense() { return defense; }
    public Integer getSpecialAttack() { return specialAttack; }
    public Integer getSpecialDefense() { return specialDefense; }
    public Integer getSpeed() { return speed; }

    public void update(String name, String description, String imageUrl) {
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.syncedAt = OffsetDateTime.now();
    }

    public void applyDetails(List<String> types, Integer height, Integer weight, String category,
                             Integer hp, Integer attack, Integer defense,
                             Integer specialAttack, Integer specialDefense, Integer speed) {
        this.typesCsv = (types == null || types.isEmpty()) ? null : String.join(",", types);
        this.height = height;
        this.weight = weight;
        this.category = category;
        this.hp = hp;
        this.attack = attack;
        this.defense = defense;
        this.specialAttack = specialAttack;
        this.specialDefense = specialDefense;
        this.speed = speed;
    }
}
