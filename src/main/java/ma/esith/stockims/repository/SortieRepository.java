package ma.esith.stockims.repository;

import ma.esith.stockims.model.Article;
import ma.esith.stockims.model.Sortie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SortieRepository extends JpaRepository<Sortie, Long> {

    /** Somme des quantités sorties pour un article (null si aucune sortie). */
    @Query("select sum(s.quantite) from Sortie s where s.article = :article")
    Long totalQuantite(@Param("article") Article article);

    /** Lignes {idArticle, somme} pour tous les articles ayant des sorties. */
    @Query("select s.article.id, sum(s.quantite) from Sortie s group by s.article.id")
    List<Object[]> totauxParArticle();

    List<Sortie> findByArticle(Article article);
}
