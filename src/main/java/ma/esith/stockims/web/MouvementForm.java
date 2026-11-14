package ma.esith.stockims.web;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/** Saisie d'une entrée ou d'une sortie (le même formulaire sert aux deux). */
@Getter @Setter
public class MouvementForm {

    @NotNull(message = "Choisissez un article.")
    private Long articleId;

    @NotNull(message = "La quantité est obligatoire.")
    @Min(value = 1, message = "La quantité doit être un nombre entier strictement positif.")
    @Max(value = 1_000_000, message = "La quantité ne peut pas dépasser 1 000 000.")
    private Integer quantite;

    @NotNull(message = "La date est obligatoire.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateMouvement = LocalDate.now();
}
