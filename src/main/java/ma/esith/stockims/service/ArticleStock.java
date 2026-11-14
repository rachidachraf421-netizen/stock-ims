package ma.esith.stockims.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ma.esith.stockims.model.Article;

/** Un article accompagné de son stock calculé (objet de lecture, pas une entité). */
@Getter @RequiredArgsConstructor
public class ArticleStock {
    private final Article article;
    private final long stock;
}
