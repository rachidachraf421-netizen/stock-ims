package ma.esith.stockims.service;

import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ma.esith.stockims.exception.ArticleIntrouvableException;
import ma.esith.stockims.exception.ReferenceDejaUtiliseeException;
import ma.esith.stockims.model.Article;
import ma.esith.stockims.repository.ArticleRepository;

@Service @RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository repository;
    private final StockService stockService;

    public List<Article> lister() {
        return repository.findAll();
    }

    /** Liste des articles (triés par référence) avec leur stock calculé. */
    public List<ArticleStock> listerAvecStock() {
        Map<Long, Long> stocks = stockService.stocksParArticle();
        return repository.findAll(Sort.by("reference")).stream()
                .map(a -> new ArticleStock(a, stocks.getOrDefault(a.getId(), 0L)))
                .toList();
    }

    public Article trouver(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ArticleIntrouvableException(id));
    }

    public long stockDe(Article article) {
        return stockService.stockDe(article);
    }

    public Article creer(String reference, String designation) {
        Article article = new Article(reference, designation);
        if (repository.existsByReferenceIgnoreCase(article.getReference())) {
            throw new ReferenceDejaUtiliseeException(article.getReference());
        }
        try {
            return repository.save(article);
        } catch (DataIntegrityViolationException e) {
            // Filet de sécurité : la contrainte unique de MySQL a refusé le doublon.
            throw new ReferenceDejaUtiliseeException(article.getReference());
        }
    }
}
