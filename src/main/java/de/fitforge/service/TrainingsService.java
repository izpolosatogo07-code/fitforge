package de.fitforge.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.fitforge.model.Trainingsplan;
import de.fitforge.model.Uebung;
import de.fitforge.model.User;
import de.fitforge.repository.TrainingsplanRepository;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Service
public class TrainingsService {

    private static final Logger log = LoggerFactory.getLogger(TrainingsService.class);
    private final TrainingsplanRepository trainingsplanRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${anthropic.api.key:}")
    private String anthropicApiKey;

    public TrainingsService(TrainingsplanRepository trainingsplanRepository) {
        this.trainingsplanRepository = trainingsplanRepository;
    }

    private static final List<String[]> KOERPERGEWICHT = Arrays.asList(
        new String[]{"Liegestütze", "3×12", "Körper gerade, Ellbogen nah am Körper"},
        new String[]{"Kniebeugen", "4×15", "Knie über Zehen, Rücken gerade"},
        new String[]{"Ausfallschritte", "3×10", "90° Kniewinkel, aufrecht bleiben"},
        new String[]{"Plank", "3×45 Sek", "Bauch anspannen, Hüfte nicht heben"},
        new String[]{"Burpees", "3×10", "Explosiv hochspringen, weich landen"},
        new String[]{"Mountainclimber", "3×30 Sek", "Gleichmäßiges Tempo, Kern angespannt"},
        new String[]{"Trizeps-Dips am Stuhl", "3×12", "Körper nah am Stuhl, Ellbogen hinten"},
        new String[]{"Crunches", "3×20", "Lendenwirbel am Boden lassen"},
        new String[]{"Beinheben", "3×15", "Langsam senken, Kontrolle halten"},
        new String[]{"Jumping Jacks", "4×30", "Arme gestreckt, gleichmäßiger Rhythmus"},
        new String[]{"High Knees", "3×40 Sek", "Knie auf Hüfthöhe, Arme schwingen"},
        new String[]{"Glutebrücke", "3×15", "Hüfte oben halten, Gesäß anspannen"},
        new String[]{"Superman", "3×12", "Arme und Beine gleichzeitig heben"},
        new String[]{"Seitenplank", "3×30 Sek", "Hüfte gerade, nicht durchhängen"},
        new String[]{"Pike Liegestütze", "3×10", "V-Form, Schultern belasten"},
        new String[]{"Box Jumps", "3×10", "Weich landen, Knie leicht gebeugt"},
        new String[]{"Squat Jumps", "3×12", "Explosiv aus der Kniebeuge hochspringen"},
        new String[]{"Reverse Lunges", "3×10", "Kontrolliert nach hinten treten"},
        new String[]{"Donkey Kicks", "3×15", "Hüfte gerade, Bein hinten anheben"},
        new String[]{"Bear Crawl", "3×20 Sek", "Knie knapp über Boden, gleichmäßig"},
        new String[]{"V-Sit", "3×20 Sek", "Beine gestreckt, Rücken gerade"},
        new String[]{"Russian Twist", "3×20", "Füße anheben für mehr Schwierigkeit"},
        new String[]{"Inchworm", "3×10", "Langsam mit Händen nach vorne laufen"},
        new String[]{"Wandsitzen", "3×45 Sek", "90° Kniewinkel, Rücken an Wand"},
        new String[]{"Einbeinige Kniebeuge", "3×8", "An Wand abstützen wenn nötig"}
    );

    private static final List<String[]> KLIMMZUG = Arrays.asList(
        new String[]{"Klimmzüge (Untergriff)", "4×6", "Vollständige Streckung unten"},
        new String[]{"Klimmzüge (Obergriff)", "4×5", "Schulterblätter zusammenziehen"},
        new String[]{"Negativklimmzüge", "3×5", "Langsam 5 Sek herunterlassen"},
        new String[]{"Hängen (aktiv)", "3×30 Sek", "Schultern aktiv, nicht passiv hängen"},
        new String[]{"Knieheben an der Stange", "3×12", "Kontrolliert, kein Schwung"},
        new String[]{"L-Sit an der Stange", "3×20 Sek", "Beine parallel zum Boden"},
        new String[]{"Klimmzüge eng (Hammer)", "3×6", "Ellbogen nah am Körper"},
        new String[]{"Archer Klimmzüge", "3×4", "Einen Arm strecken, anderen beugen"},
        new String[]{"Klimmzug-Holds", "3×10 Sek", "Oben halten, Kinn über Stange"},
        new String[]{"Muscle Up", "3×3", "Explosiv ziehen, über Stange drücken"},
        new String[]{"Around the World", "3×5", "Kreisbewegung um die Stange"},
        new String[]{"Typewriter Klimmzüge", "3×4", "Seitwärts an der Stange entlanggleiten"},
        new String[]{"Windshield Wipers", "3×8", "Beine gestreckt links-rechts schwingen"},
        new String[]{"Frontlever-Progression", "3×10 Sek", "Körper horizontal halten"},
        new String[]{"Klimmzüge mit Pause", "3×5", "3 Sek Pause oben halten"}
    );

    private static final List<String[]> HANTELN = Arrays.asList(
        new String[]{"Bizepscurls", "3×12", "Ellbogen fest am Körper"},
        new String[]{"Schulterdrücken", "3×10", "Kern stabilisieren, nicht hohlkreuzen"},
        new String[]{"Kurzhantel-Rudern", "4×12", "Schulterblätter zusammenziehen"},
        new String[]{"Trizepsdrücken", "3×12", "Ellbogen zeigen nach vorne"},
        new String[]{"Seitliches Heben", "3×15", "Arme leicht gebeugt, bis Schulterhöhe"},
        new String[]{"Frontheben", "3×12", "Langsam hoch und runter"},
        new String[]{"Goblet Squat", "4×12", "Hantel vor Brust, tief squatten"},
        new String[]{"Kurzhantel-Ausfallschritte", "3×10", "Hantel an Seiten, aufrecht bleiben"},
        new String[]{"Romanian Deadlift", "4×10", "Rücken gerade, Hüfte führt"},
        new String[]{"Hammer Curls", "3×12", "Daumen oben, kontrolliert"},
        new String[]{"Chest Flyes", "3×12", "Leichte Beugung der Ellbogen"},
        new String[]{"Kurzhantel-Bankdrücken", "4×10", "Schulterblätter zurückziehen"},
        new String[]{"Farmers Walk", "3×30 Sek", "Aufrecht gehen, Kern angespannt"},
        new String[]{"Zottman Curl", "3×10", "Rauf Untergriff, runter Obergriff"},
        new String[]{"Arnold Press", "3×12", "Rotation beim Drücken"},
        new String[]{"Kurzhantel-Kreuzheben", "3×10", "Kontrolliert senken"},
        new String[]{"Einarmiges Rudern", "3×12", "Bank als Stütze nutzen"},
        new String[]{"Kurzhantel-Shrugs", "3×15", "Schultern hochziehen, kurz halten"},
        new String[]{"Pullovers", "3×12", "Auf Bank liegen, Hantel über Kopf"},
        new String[]{"Turkish Get-Up", "3×5", "Langsam und kontrolliert aufstehen"}
    );

    private static final List<String[]> ZGUTE = Arrays.asList(
        new String[]{"Band-Kniebeugen", "3×15", "Band über Schultern, tief squatten"},
        new String[]{"Band-Bizepscurls", "3×12", "Band unter Füßen, gleichmäßig ziehen"},
        new String[]{"Band-Rudern", "3×15", "Band an Wand befestigen, ziehen"},
        new String[]{"Band-Schulterdrücken", "3×12", "Band unter Füßen, über Kopf drücken"},
        new String[]{"Band-Seitwärtsschritte", "3×20", "Band um Knöchel, seitlich gehen"},
        new String[]{"Band-Pull-Apart", "3×20", "Band auf Schulterhöhe auseinanderziehen"},
        new String[]{"Band-Trizepsdrücken", "3×15", "Band über Kopf, Ellbogen fixiert"},
        new String[]{"Band-Glutekicks", "3×15", "Band um Knöchel, Bein nach hinten"},
        new String[]{"Band-Facepulls", "3×15", "Band auf Augenhöhe, zu Gesicht ziehen"},
        new String[]{"Band-Pallof Press", "3×12", "Band seitlich, gerade nach vorne drücken"},
        new String[]{"Band-Good Morning", "3×15", "Band um Schultern, vorneigen"},
        new String[]{"Band-Chest Press", "3×12", "Band hinter Rücken, nach vorne drücken"},
        new String[]{"Band-Reverse Fly", "3×15", "Arme seitlich öffnen"},
        new String[]{"Band-Crunch", "3×20", "Band über Kopf fixiert, Rumpf beugen"},
        new String[]{"Band-Clamshell", "3×15", "Auf Seite liegen, Knie öffnen"}
    );

    private static final List<String[]> SPRINGSEIL = Arrays.asList(
        new String[]{"Seilspringen Basic", "5×60 Sek", "Auf Fußballen bleiben, Rhythmus halten"},
        new String[]{"Doppelsprünge", "3×30 Sek", "Seil zweimal pro Sprung drehen"},
        new String[]{"Alternating Feet", "4×45 Sek", "Abwechselnd links-rechts"},
        new String[]{"High Knees Seil", "3×30 Sek", "Knie auf Hüfthöhe beim Springen"},
        new String[]{"Boxer Step", "4×60 Sek", "Leichtes Wippen, minimaler Aufprall"},
        new String[]{"Seilspringen Rückwärts", "3×30 Sek", "Seil rückwärts drehen"},
        new String[]{"Kreuzen", "3×20", "Arme vor Körper kreuzen"},
        new String[]{"Einbeinig springen", "3×20 Sek", "Abwechselnd pro Bein"},
        new String[]{"Intervall-Seil", "5×20 Sek", "Maximaltempo, dann kurze Pause"},
        new String[]{"Seil + Squat", "3×10", "Nach jedem Sprung eine Kniebeuge"}
    );

    private static final List<String[]> BOXEN = Arrays.asList(
        new String[]{"Jab-Kreuz Kombination", "4×2 Min", "Hüftrotation bei jedem Schlag"},
        new String[]{"Jab-Kreuz-Haken", "3×2 Min", "Gewicht verlagern beim Haken"},
        new String[]{"Shadowboxing", "5×3 Min", "Füße immer in Bewegung"},
        new String[]{"Slip und Konter", "3×2 Min", "Kopf aus Linie, sofort kontern"},
        new String[]{"Body Jabs", "3×2 Min", "Knie beugen für Körpertreffer"},
        new String[]{"Uppercut Kombination", "3×2 Min", "Kurze explosive Bewegung"},
        new String[]{"Doppel-Jab + Kreuz", "4×90 Sek", "Zweiter Jab täuscht, Kreuz trifft"},
        new String[]{"Footwork-Drill", "4×2 Min", "Vorwärts-rückwärts-seitwärts"},
        new String[]{"Seilspringen Boxer", "5×3 Min", "Boxer Step, gleichmäßiges Tempo"},
        new String[]{"Kombinationen am Sack", "5×3 Min", "Eigene Kombinationen fließen lassen"}
    );

    private static final List<String[]> THAIBOXEN = Arrays.asList(
        new String[]{"Teep (Frontkick)", "3×15", "Hüfte nach vorne schieben beim Kick"},
        new String[]{"Low Kick", "3×10", "Aus Hüfte drehen, Schienbein trifft"},
        new String[]{"Mittelkick (Roundhouse)", "3×10", "Hüftrotation, ganzer Körper dreht"},
        new String[]{"Highkick", "3×8", "Dehnung der Hüfte nötig, Standbein stabil"},
        new String[]{"Knie-Stoß", "3×15", "Hüfte nach vorne, Knie trifft"},
        new String[]{"Ellbogen-Techniken", "3×10", "Kurze Distanz, horizontaler Ellbogen"},
        new String[]{"Jab-Kreuz-Low Kick", "3×2 Min", "Kombination aus Händen und Bein"},
        new String[]{"Teep + Roundhouse", "3×2 Min", "Erst stoppen, dann schwerer Kick"},
        new String[]{"Klinchen + Knie", "3×2 Min", "Im Klinchen Knie zum Körper"},
        new String[]{"Thai-Shadowboxing", "5×3 Min", "Alle Techniken kombinieren"}
    );

    @Transactional
    public Trainingsplan planGenerieren(User user, boolean kiModus) {
        Trainingsplan plan;
        if (kiModus && anthropicApiKey != null && !anthropicApiKey.isBlank()
                && !anthropicApiKey.equals("sk-ant-DEIN_API_KEY_HIER")) {
            plan = mitKiGenerieren(user);
        } else {
            plan = regelbasiertGenerieren(user);
        }
        plan.setUser(user);
        if (plan.getUebungen() != null) {
            plan.getUebungen().forEach(u -> u.setTrainingsplan(plan));
        }
        return trainingsplanRepository.save(plan);
    }

    public List<Trainingsplan> plaeneDesUsers(User user) {
        return trainingsplanRepository.findByUserOrderByErstelltAmDesc(user);
    }

    public List<Trainingsplan> letzteFuenfPlaene(User user) {
        return trainingsplanRepository.findTop5ByUserOrderByErstelltAmDesc(user);
    }

    @Transactional
    public void planLoeschen(Long planId, User user) {
        Trainingsplan plan = trainingsplanRepository.findById(planId)
            .orElseThrow(() -> new IllegalArgumentException("Plan nicht gefunden"));
        if (!plan.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Keine Berechtigung");
        }
        trainingsplanRepository.delete(plan);
    }

    private Trainingsplan regelbasiertGenerieren(User user) {
        Trainingsplan plan = new Trainingsplan();
        List<Uebung> uebungen = new ArrayList<>();
        String ziele = user.getZiele() != null ? user.getZiele().toLowerCase() : "";
        String ausruestung = user.getAusruestung() != null ? user.getAusruestung().name() : "";

        if (ziele.contains("thai")) {
            plan.setTitel("Thaiboxen Training");
            plan.setDauer("60 Minuten");
            plan.setKalorien("500-600 kcal");
            uebungen.addAll(zufaellig(SPRINGSEIL, 2));
            uebungen.addAll(zufaellig(THAIBOXEN, 6));
            uebungen.addAll(zufaellig(KOERPERGEWICHT, 2));
        } else if (ziele.contains("boxen") || ziele.contains("kampf")) {
            plan.setTitel("Boxtraining");
            plan.setDauer("60 Minuten");
            plan.setKalorien("450-550 kcal");
            uebungen.addAll(zufaellig(SPRINGSEIL, 3));
            uebungen.addAll(zufaellig(BOXEN, 6));
            uebungen.addAll(zufaellig(KOERPERGEWICHT, 2));
        } else if (ziele.contains("fett") || ziele.contains("cardio") || ziele.contains("ausdauer")) {
            plan.setTitel("Fettverbrennung & Ausdauer");
            plan.setDauer("40-50 Minuten");
            plan.setKalorien("350-450 kcal");
            uebungen.addAll(zufaellig(SPRINGSEIL, 3));
            uebungen.addAll(zufaellig(KOERPERGEWICHT, 5));
            if (!ausruestung.equals("KEIN_GERAET")) {
                uebungen.addAll(zufaellig(ZGUTE, 2));
            }
        } else if (ziele.contains("muskel") || ziele.contains("aufbau")) {
            plan.setTitel("Muskelaufbau Training");
            plan.setDauer("55-65 Minuten");
            plan.setKalorien("300-400 kcal");
            if (ausruestung.equals("VOLLES_STUDIO") || ausruestung.equals("KURZHANTELN")) {
                uebungen.addAll(zufaellig(HANTELN, 5));
                uebungen.addAll(zufaellig(KLIMMZUG, 3));
                uebungen.addAll(zufaellig(KOERPERGEWICHT, 2));
            } else {
                uebungen.addAll(zufaellig(KLIMMZUG, 4));
                uebungen.addAll(zufaellig(KOERPERGEWICHT, 5));
            }
        } else if (ziele.contains("kraft")) {
            plan.setTitel("Krafttraining");
            plan.setDauer("55-70 Minuten");
            plan.setKalorien("250-350 kcal");
            uebungen.addAll(zufaellig(KLIMMZUG, 4));
            uebungen.addAll(zufaellig(HANTELN, 4));
            uebungen.addAll(zufaellig(KOERPERGEWICHT, 2));
        } else if (ziele.contains("aufwärm") || ziele.contains("aufwaerm")) {
            plan.setTitel("Aufwärmen & Mobilität");
            plan.setDauer("15-20 Minuten");
            plan.setKalorien("80-120 kcal");
            uebungen.addAll(zufaellig(SPRINGSEIL, 2));
            uebungen.addAll(zufaellig(KOERPERGEWICHT, 4));
        } else if (ziele.contains("dehn")) {
            plan.setTitel("Dehnen & Entspannung");
            plan.setDauer("20-30 Minuten");
            plan.setKalorien("50-80 kcal");
            uebungen.addAll(dehnuebungen());
        } else {
            plan.setTitel("Ganzkörper Training");
            plan.setDauer("45-55 Minuten");
            plan.setKalorien("300-400 kcal");
            uebungen.addAll(zufaellig(KOERPERGEWICHT, 4));
            uebungen.addAll(zufaellig(KLIMMZUG, 2));
            if (!ausruestung.equals("KEIN_GERAET")) {
                uebungen.addAll(zufaellig(HANTELN, 2));
                uebungen.addAll(zufaellig(ZGUTE, 2));
            } else {
                uebungen.addAll(zufaellig(SPRINGSEIL, 2));
                uebungen.addAll(zufaellig(KOERPERGEWICHT, 2));
            }
        }

        for (int i = 0; i < uebungen.size(); i++) {
            uebungen.get(i).setPosition(i + 1);
        }
        plan.setZiele(user.getZiele());
        plan.setQuelle(Trainingsplan.QuelleTyp.MANUELL);
        plan.setUebungen(uebungen);
        return plan;
    }

    private List<Uebung> zufaellig(List<String[]> quelle, int anzahl) {
        List<String[]> kopie = new ArrayList<>(quelle);
        Collections.shuffle(kopie);
        List<Uebung> result = new ArrayList<>();
        for (int i = 0; i < Math.min(anzahl, kopie.size()); i++) {
            String[] u = kopie.get(i);
            result.add(new Uebung(u[0], u[1], u[2], i + 1));
        }
        return result;
    }

    private List<Uebung> dehnuebungen() {
        return Arrays.asList(
            new Uebung("Oberschenkel-Dehnung", "2×30 Sek", "Ferse an Po, aufrecht stehen", 1),
            new Uebung("Wade dehnen", "2×30 Sek", "An Wand lehnen, Ferse am Boden", 2),
            new Uebung("Schulter-Dehnung", "2×20 Sek", "Arm über Brust ziehen", 3),
            new Uebung("Hüftbeuger-Dehnung", "2×30 Sek", "Ausfallschritt-Position", 4),
            new Uebung("Rumpfrotation liegend", "2×10", "Knie zur Seite fallen lassen", 5),
            new Uebung("Kindshaltung", "2×45 Sek", "Arme weit nach vorne strecken", 6),
            new Uebung("Pigeon Pose", "2×30 Sek", "Hüfte öffnen, Bein gebeugt vorne", 7)
        );
    }

    private Trainingsplan mitKiGenerieren(User user) {
        OkHttpClient client = new OkHttpClient();
        try {
            String prompt = String.format(
                "Du bist Personal Trainer. Erstelle Trainingsplan auf Deutsch. " +
                "Athlet: Geschlecht=%s, Alter=%s, Größe=%s cm, Gewicht=%s kg, " +
                "Level=%s, Ausrüstung=%s, Ziele=%s. " +
                "Antworte NUR mit JSON (keine Backticks): " +
                "{\"titel\":\"...\",\"dauer\":\"...\",\"kalorien\":\"...\",\"uebungen\":[{\"name\":\"...\",\"saetze\":\"...\",\"technikTipp\":\"...\"}]}. " +
                "Gib 6-8 Übungen aus.",
                user.getGeschlecht(), user.getAlterJahre(), user.getGroesse(),
                user.getGewicht(), user.getFitnessLevel(), user.getAusruestung(), user.getZiele()
            );

            Map<String, Object> body = Map.of(
                "model", "claude-sonnet-4-6",
                "max_tokens", 1500,
                "messages", List.of(Map.of("role", "user", "content", prompt))
            );
            String requestBody = objectMapper.writeValueAsString(body);

            Request request = new Request.Builder()
                .url("https://api.anthropic.com/v1/messages")
                .post(RequestBody.create(requestBody, MediaType.get("application/json")))
                .addHeader("x-api-key", anthropicApiKey)
                .addHeader("anthropic-version", "2023-06-01")
                .addHeader("content-type", "application/json")
                .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    return regelbasiertGenerieren(user);
                }
                return parseKiAntwort(response.body().string(), user);
            }
        } catch (Exception e) {
            log.error("KI-Fehler: {}", e.getMessage());
            return regelbasiertGenerieren(user);
        }
    }

    private Trainingsplan parseKiAntwort(String responseBody, User user) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String text = root.path("content").get(0).path("text").asText()
                .replaceAll("```json|```", "").trim();
            JsonNode planNode = objectMapper.readTree(text);

            Trainingsplan plan = new Trainingsplan();
            plan.setTitel(planNode.path("titel").asText("KI-Trainingsplan"));
            plan.setDauer(planNode.path("dauer").asText("45 Minuten"));
            plan.setKalorien(planNode.path("kalorien").asText("300 kcal"));
            plan.setZiele(user.getZiele());
            plan.setQuelle(Trainingsplan.QuelleTyp.KI);

            List<Uebung> uebungen = new ArrayList<>();
            JsonNode arr = planNode.path("uebungen");
            for (int i = 0; i < arr.size(); i++) {
                JsonNode u = arr.get(i);
                uebungen.add(new Uebung(
                    u.path("name").asText(),
                    u.path("saetze").asText(),
                    u.path("technikTipp").asText(),
                    i + 1
                ));
            }
            plan.setUebungen(uebungen);
            return plan;
        } catch (Exception e) {
            log.error("Parse-Fehler: {}", e.getMessage());
            return regelbasiertGenerieren(user);
        }
    }
}