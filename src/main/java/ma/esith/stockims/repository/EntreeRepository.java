package ma.esith.stockims.repository;

import ma.esith.stockims.model.Article;
import ma.esith.stockims.model.Entree;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EntreeRepository extends JpaRepository<Entree, Long> {

    /** Somme des quantités entrées pour un article (null si aucune entrée). */
    @Query("select sum(e.quantite) from Entree e where e.article = :article")
    Long totalQuantite(@Param("article") Article article);

    /** Lignes {idArticle, somme} pour tous les articles ayant des entrées. */
    @Query("select e.article.id, sum(e.quantite) from Entree e group by e.article.id")
    List<Object[]> totauxParArticle();

    List<Entree> findByArticle(Article article);
}
