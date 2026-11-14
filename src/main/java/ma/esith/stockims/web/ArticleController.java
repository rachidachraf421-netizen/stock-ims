package ma.esith.stockims.web;

import ma.esith.stockims.exception.ReferenceDejaUtiliseeException;
import ma.esith.stockims.model.Article;
import ma.esith.stockims.service.ArticleService;
import ma.esith.stockims.service.MouvementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller @RequestMapping("/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService service;
    private final MouvementService mouvementService;

    @GetMapping
    public String liste(Model model) {
        model.addAttribute("articles", service.listerAvecStock());
        return "articles/liste";
    }

    @GetMapping("/nouveau")
    public String formulaire(Model model) {
        model.addAttribute("article", new ArticleForm());
        return "articles/formulaire";
    }

    @GetMapping("/{id:\\d+}")
    public String detail(@PathVariable Long id, Model model) {
        Article article = service.trouver(id);
        model.addAttribute("article", article);
        model.addAttribute("stock", service.stockDe(article));
        model.addAttribute("mouvements", mouvementService.historique(article));
        return "articles/detail";
    }

    @PostMapping
    public String ajouter(@Valid @ModelAttribute("article") ArticleForm form,
                          BindingResult erreurs,
                          RedirectAttributes redirect) {
        if (erreurs.hasErrors()) return "articles/formulaire";
        try {
            Article cree = service.creer(form.getReference(), form.getDesignation());
            redirect.addFlashAttribute("succes",
                    "Article « " + cree.getReference() + " » enregistré. Son stock est de 0.");
        } catch (ReferenceDejaUtiliseeException e) {
            erreurs.rejectValue("reference", "doublon",
                    "Cette référence existe déjà.");
            return "articles/formulaire";
        }
        return "redirect:/articles";
    }
}
