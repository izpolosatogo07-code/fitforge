package de.fitforge.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "trainingsplaene")
public class Trainingsplan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String titel;

    private String dauer;
    private String kalorien;

    @Column(length = 100)
    private String ziele;

    @Enumerated(EnumType.STRING)
    private QuelleTyp quelle = QuelleTyp.MANUELL;

    @OneToMany(mappedBy = "trainingsplan", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OrderBy("position ASC")
    private List<Uebung> uebungen;

    @Column(updatable = false)
    private LocalDateTime erstelltAm = LocalDateTime.now();

    public Trainingsplan() {}

    // --- Getters & Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getTitel() { return titel; }
    public void setTitel(String titel) { this.titel = titel; }

    public String getDauer() { return dauer; }
    public void setDauer(String dauer) { this.dauer = dauer; }

    public String getKalorien() { return kalorien; }
    public void setKalorien(String kalorien) { this.kalorien = kalorien; }

    public String getZiele() { return ziele; }
    public void setZiele(String ziele) { this.ziele = ziele; }

    public QuelleTyp getQuelle() { return quelle; }
    public void setQuelle(QuelleTyp quelle) { this.quelle = quelle; }

    public List<Uebung> getUebungen() { return uebungen; }
    public void setUebungen(List<Uebung> uebungen) { this.uebungen = uebungen; }

    public LocalDateTime getErstelltAm() { return erstelltAm; }
    public void setErstelltAm(LocalDateTime erstelltAm) { this.erstelltAm = erstelltAm; }

    public enum QuelleTyp {
        KI, MANUELL
    }
}