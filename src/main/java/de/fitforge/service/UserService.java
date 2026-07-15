package de.fitforge.service;

import de.fitforge.model.User;
import de.fitforge.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Benutzer nicht gefunden: " + email));
        return new org.springframework.security.core.userdetails.User(
            user.getEmail(),
            user.getPassword(),
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Transactional
    public User registrieren(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("E-Mail bereits registriert: " + user.getEmail());
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Transactional
    public User profilAktualisieren(User aktualisiertUser) {
        User vorhandener = userRepository.findById(aktualisiertUser.getId())
            .orElseThrow(() -> new IllegalArgumentException("Benutzer nicht gefunden"));
        vorhandener.setName(aktualisiertUser.getName());
        vorhandener.setAlterJahre(aktualisiertUser.getAlterJahre());
        vorhandener.setGeschlecht(aktualisiertUser.getGeschlecht());
        vorhandener.setGroesse(aktualisiertUser.getGroesse());
        vorhandener.setGewicht(aktualisiertUser.getGewicht());
        vorhandener.setFitnessLevel(aktualisiertUser.getFitnessLevel());
        vorhandener.setAusruestung(aktualisiertUser.getAusruestung());
        vorhandener.setZiele(aktualisiertUser.getZiele());
        return userRepository.save(vorhandener);
    }

    public User findeNachEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Benutzer nicht gefunden"));
    }

    public User findeNachId(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Benutzer nicht gefunden"));
    }
}