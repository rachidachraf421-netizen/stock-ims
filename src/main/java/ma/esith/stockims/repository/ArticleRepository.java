package ma.esith.stockims.repository;

import jakarta.persistence.LockModeType;
import ma.esith.stockims.model.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ArticleRepository
        extends JpaRepository<Article, Long> {

    boolean existsByReferenceIgnoreCase(String reference);

    // Verrouille la ligne de l'article pendant l'enregistrement d'une sortie :
    // deux sorties simultanées ne peuvent pas dépasser ensemble le stock.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Article a where a.id = :id")
    Optional<Article> findByIdPourMiseAJour(@Param("id") Long id);
}
