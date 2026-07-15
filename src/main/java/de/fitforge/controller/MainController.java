package de.fitforge.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import de.fitforge.model.Trainingsplan;
import de.fitforge.model.User;
import de.fitforge.service.TrainingsService;
import de.fitforge.service.UserService;
import jakarta.validation.Valid;

@Controller
public class MainController {

    private final UserService userService;
    private final TrainingsService trainingsService;

    public MainController(UserService userService, TrainingsService trainingsService) {
        this.userService = userService;
        this.trainingsService = trainingsService;
    }

    // =========================================================
    // STARTSEITE
    // =========================================================

    @GetMapping("/")
    public String startseite() { return "index"; }

    // =========================================================
    // LOGIN
    // =========================================================

    @GetMapping("/login")
    public String loginSeite() { return "login"; }

    // =========================================================
    // REGISTRIERUNG
    // =========================================================

    @GetMapping("/registrieren")
    public String registrierenSeite(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("fitnessLevels", User.FitnessLevel.values());
        model.addAttribute("ausruestungen", User.Ausruestung.values());
        return "registrieren";
    }

    @PostMapping("/registrieren")
    public String registrierenVerarbeiten(
            @Valid @ModelAttribute("user") User user,
            BindingResult result,
            @RequestParam(value = "ziele", required = false) String[] ziele,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("fitnessLevels", User.FitnessLevel.values());
            model.addAttribute("ausruestungen", User.Ausruestung.values());
            return "registrieren";
        }
        try {
            if (ziele != null && ziele.length > 0) {
                user.setZiele(String.join(",", ziele));
            }
            userService.registrieren(user);
            redirectAttributes.addFlashAttribute("erfolg",
                "Registrierung erfolgreich! Bitte einloggen.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("fehler", e.getMessage());
            model.addAttribute("fitnessLevels", User.FitnessLevel.values());
            model.addAttribute("ausruestungen", User.Ausruestung.values());
            return "registrieren";
        }
    }

    // =========================================================
    // DASHBOARD
    // =========================================================

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        User user = userService.findeNachEmail(auth.getName());
        List<Trainingsplan> letztePlaene = trainingsService.letzteFuenfPlaene(user);
        model.addAttribute("user", user);
        model.addAttribute("letztePlaene", letztePlaene);
        return "dashboard";
    }

    // =========================================================
    // PROFIL
    // =========================================================

    @GetMapping("/profil")
    public String profilSeite(Authentication auth, Model model) {
        User user = userService.findeNachEmail(auth.getName());
        model.addAttribute("user", user);
        model.addAttribute("fitnessLevels", User.FitnessLevel.values());
        model.addAttribute("ausruestungen", User.Ausruestung.values());
        List<String> aktuelleZiele = user.getZiele() != null
            ? Arrays.asList(user.getZiele().split(",")) : List.of();
        model.addAttribute("aktuelleZiele", aktuelleZiele);
        return "profil";
    }

    @PostMapping("/profil")
    public String profilSpeichern(
            Authentication auth,
            @ModelAttribute User formUser,
            @RequestParam(value = "ziele", required = false) String[] ziele,
            RedirectAttributes redirectAttributes) {

        User user = userService.findeNachEmail(auth.getName());
        formUser.setId(user.getId());
        formUser.setEmail(user.getEmail());
        formUser.setPassword(user.getPassword());
        if (ziele != null && ziele.length > 0) {
            formUser.setZiele(String.join(",", ziele));
        }
        userService.profilAktualisieren(formUser);
        redirectAttributes.addFlashAttribute("erfolg", "Profil erfolgreich gespeichert!");
        return "redirect:/profil";
    }

    // =========================================================
    // TRAINING
    // =========================================================

    @GetMapping("/training")
    public String trainingSeite(Authentication auth, Model model) {
        User user = userService.findeNachEmail(auth.getName());
        model.addAttribute("user", user);
        model.addAttribute("plaene", trainingsService.plaeneDesUsers(user));
        return "training";
    }

    @PostMapping("/training/generieren")
    public String trainingsplanGenerieren(
            Authentication auth,
            @RequestParam(value = "kiModus", defaultValue = "false") boolean kiModus,
            RedirectAttributes redirectAttributes) {

        User user = userService.findeNachEmail(auth.getName());
        if (user.getZiele() == null || user.getZiele().isBlank()) {
            redirectAttributes.addFlashAttribute("warnung",
                "Bitte erst Trainingsziele im Profil eintragen.");
            return "redirect:/profil";
        }
        Trainingsplan plan = trainingsService.planGenerieren(user, kiModus);
        redirectAttributes.addFlashAttribute("erfolg",
            "Trainingsplan \"" + plan.getTitel() + "\" wurde erstellt!");
        redirectAttributes.addFlashAttribute("neuerPlanId", plan.getId());
        return "redirect:/training";
    }

    @PostMapping("/training/loeschen/{id}")
    public String trainingsplanLoeschen(
            @PathVariable Long id,
            Authentication auth,
            RedirectAttributes redirectAttributes) {

        User user = userService.findeNachEmail(auth.getName());
        trainingsService.planLoeschen(id, user);
        redirectAttributes.addFlashAttribute("erfolg", "Trainingsplan wurde gelöscht.");
        return "redirect:/training";
    }

    // =========================================================
    // TIMER
    // =========================================================

    @GetMapping("/timer")
    public String timerSeite() { return "timer"; }
}