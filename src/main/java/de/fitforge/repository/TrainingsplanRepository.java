package de.fitforge.repository;

import de.fitforge.model.Trainingsplan;
import de.fitforge.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingsplanRepository extends JpaRepository<Trainingsplan, Long> {
    List<Trainingsplan> findByUserOrderByErstelltAmDesc(User user);
    List<Trainingsplan> findTop5ByUserOrderByErstelltAmDesc(User user);
}
