package de.fitforge.model;

import jakarta.persistence.*;

@Entity
@Table(name = "uebungen")
public class Uebung {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainingsplan_id", nullable = false)
    private Trainingsplan trainingsplan;

    @Column(nullable = false)
    private String name;

    private String saetze;
    private String technikTipp;
    private int position;

    public Uebung() {}

    public Uebung(String name, String saetze, String technikTipp, int position) {
        this.name = name;
        this.saetze = saetze;
        this.technikTipp = technikTipp;
        this.position = position;
    }

    // --- Getters & Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Trainingsplan getTrainingsplan() { return trainingsplan; }
    public void setTrainingsplan(Trainingsplan trainingsplan) { this.trainingsplan = trainingsplan; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSaetze() { return saetze; }
    public void setSaetze(String saetze) { this.saetze = saetze; }

    public String getTechnikTipp() { return technikTipp; }
    public void setTechnikTipp(String technikTipp) { this.technikTipp = technikTipp; }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
}