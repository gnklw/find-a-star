package softuni.exam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import softuni.exam.models.entity.Star;
import softuni.exam.util.StarType;

import java.util.Optional;
import java.util.Set;

@Repository
public interface StarRepository extends JpaRepository<Star, Long> {

    Optional<Star> findByName(String name);

    Set<Star> findAllByStarTypeAndObserversIsEmptyOrderByLightYearsAsc(StarType starType);
}
