package com.pokemonai.pokemon.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
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

    public void update(String name, String description, String imageUrl) {
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.syncedAt = OffsetDateTime.now();
    }
}
