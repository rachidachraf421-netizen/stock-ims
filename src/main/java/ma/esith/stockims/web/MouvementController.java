package ma.esith.stockims.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.esith.stockims.exception.ArticleIntrouvableException;
import ma.esith.stockims.exception.StockInsuffisantException;
import ma.esith.stockims.model.Entree;
import ma.esith.stockims.model.Sortie;
import ma.esith.stockims.service.ArticleService;
import ma.esith.stockims.service.MouvementService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller @RequestMapping("/mouvements")
@RequiredArgsConstructor
public class MouvementController {

    private final ArticleService articleService;
    private final MouvementService mouvementService;

    @GetMapping
    public String historique(Model model) {
        model.addAttribute("mouvements", mouvementService.tousLesMouvements());
        return "mouvements/liste";
    }

    @GetMapping("/entree")
    public String formulaireEntree(@RequestParam(required = false) Long articleId,
                                   Model model) {
        MouvementForm form = new MouvementForm();
        form.setArticleId(articleId);
        model.addAttribute("mouvement", form);
        return preparer(model, false);
    }

    @GetMapping("/sortie")
    public String formulaireSortie(@RequestParam(required = false) Long articleId,
                                   Model model) {
        MouvementForm form = new MouvementForm();
        form.setArticleId(articleId);
        model.addAttribute("mouvement", form);
        return preparer(model, true);
    }

    @PostMapping("/entree")
    public String ajouterEntree(@Valid @ModelAttribute("mouvement") MouvementForm form,
                                BindingResult erreurs,
                                Model model,
                                RedirectAttributes redirect) {
        if (erreurs.hasErrors()) return preparer(model, false);
        try {
            Entree entree = mouvementService.enregistrerEntree(
                    form.getArticleId(), form.getQuantite(), form.getDateMouvement());
            long stock = articleService.stockDe(entree.getArticle());
            redirect.addFlashAttribute("succes", "Entrée de " + entree.getQuantite()
                    + " enregistrée pour " + entree.getArticle().getReference()
                    + ". Stock disponible : " + stock + ".");
        } catch (ArticleIntrouvableException e) {
            erreurs.rejectValue("articleId", "introuvable", "Cet article n'existe plus.");
            return preparer(model, false);
        }
        return "redirect:/articles/" + form.getArticleId();
    }

    @PostMapping("/sortie")
    public String ajouterSortie(@Valid @ModelAttribute("mouvement") MouvementForm form,
                                BindingResult erreurs,
                                Model model,
                                RedirectAttributes redirect) {
        if (erreurs.hasErrors()) return preparer(model, true);
        try {
            Sortie sortie = mouvementService.enregistrerSortie(
                    form.getArticleId(), form.getQuantite(), form.getDateMouvement());
            long stock = articleService.stockDe(sortie.getArticle());
            redirect.addFlashAttribute("succes", "Sortie de " + sortie.getQuantite()
                    + " enregistrée pour " + sortie.getArticle().getReference()
                    + ". Stock disponible : " + stock + ".");
        } catch (StockInsuffisantException e) {
            erreurs.rejectValue("quantite", "stock.insuffisant", e.getMessage());
            return preparer(model, true);
        } catch (ArticleIntrouvableException e) {
            erreurs.rejectValue("articleId", "introuvable", "Cet article n'existe plus.");
            return preparer(model, true);
        }
        return "redirect:/articles/" + form.getArticleId();
    }

    /** Données communes aux deux formulaires. */
    private String preparer(Model model, boolean sortie) {
        model.addAttribute("articles", articleService.listerAvecStock());
        model.addAttribute("sortie", sortie);
        model.addAttribute("actif", sortie ? "sortie" : "entree");
        return "mouvements/formulaire";
    }
}
