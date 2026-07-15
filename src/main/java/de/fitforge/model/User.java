package de.fitforge.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name darf nicht leer sein")
    @Size(min = 2, max = 50)
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "E-Mail darf nicht leer sein")
    @Email(message = "Ungültige E-Mail-Adresse")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank
    @Column(nullable = false)
    private String password;

    @Min(10) @Max(100)
    @Column(name = "alter_jahre")
    private Integer alterJahre;

    @Enumerated(EnumType.STRING)
    private Geschlecht geschlecht;

    @Min(100) @Max(250)
    private Integer groesse;

    @Min(30) @Max(300)
    private Integer gewicht;

    @Enumerated(EnumType.STRING)
    private FitnessLevel fitnessLevel = FitnessLevel.ANFAENGER;

    @Enumerated(EnumType.STRING)
    private Ausruestung ausruestung = Ausruestung.KEIN_GERAET;

    @Column(length = 500)
    private String ziele;

    @Column(nullable = false)
    private boolean aktiv = true;

    @Column(updatable = false)
    private LocalDateTime erstelltAm = LocalDateTime.now();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Trainingsplan> trainingsPlaene;

    public User() {}

    // --- Getters & Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Integer getAlterJahre() { return alterJahre; }
    public void setAlterJahre(Integer alterJahre) { this.alterJahre = alterJahre; }

    public Geschlecht getGeschlecht() { return geschlecht; }
    public void setGeschlecht(Geschlecht geschlecht) { this.geschlecht = geschlecht; }

    public Integer getGroesse() { return groesse; }
    public void setGroesse(Integer groesse) { this.groesse = groesse; }

    public Integer getGewicht() { return gewicht; }
    public void setGewicht(Integer gewicht) { this.gewicht = gewicht; }

    public FitnessLevel getFitnessLevel() { return fitnessLevel; }
    public void setFitnessLevel(FitnessLevel fitnessLevel) { this.fitnessLevel = fitnessLevel; }

    public Ausruestung getAusruestung() { return ausruestung; }
    public void setAusruestung(Ausruestung ausruestung) { this.ausruestung = ausruestung; }

    public String getZiele() { return ziele; }
    public void setZiele(String ziele) { this.ziele = ziele; }

    public boolean isAktiv() { return aktiv; }
    public void setAktiv(boolean aktiv) { this.aktiv = aktiv; }

    public LocalDateTime getErstelltAm() { return erstelltAm; }
    public void setErstelltAm(LocalDateTime erstelltAm) { this.erstelltAm = erstelltAm; }

    public List<Trainingsplan> getTrainingsPlaene() { return trainingsPlaene; }
    public void setTrainingsPlaene(List<Trainingsplan> trainingsPlaene) { this.trainingsPlaene = trainingsPlaene; }

    // --- Enums ---
    public enum Geschlecht {
        MAENNLICH, WEIBLICH, DIVERS
    }

    public enum FitnessLevel {
        ANFAENGER, FORTGESCHRITTEN, PROFI;
        public String getAnzeigeName() {
            return switch (this) {
                case ANFAENGER -> "Anfänger";
                case FORTGESCHRITTEN -> "Fortgeschritten";
                case PROFI -> "Profi";
            };
        }
    }

    public enum Ausruestung {
        KEIN_GERAET, KURZHANTELN, VOLLES_STUDIO;
        public String getAnzeigeName() {
            return switch (this) {
                case KEIN_GERAET -> "Kein Gerät";
                case KURZHANTELN -> "Kurzhanteln";
                case VOLLES_STUDIO -> "Vollständiges Studio";
            };
        }
    }
}