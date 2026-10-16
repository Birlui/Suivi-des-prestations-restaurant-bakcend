package com.marches.restauration.repository;

import com.marches.restauration.domain.OrdreCommande;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrdreCommandeRepository extends JpaRepository<OrdreCommande, Long> {

    List<OrdreCommande> findByMarcheId(Long marcheId);

    Optional<OrdreCommande> findByNumero(String numero);

    Optional<OrdreCommande> findByNumeroIgnoreCaseAndMarcheId(String numero, Long marcheId);

    /**
     * Renvoie le plus grand suffixe numérique parmi tous les numéros d'ordre
     * existants (ex. "OC-2026-046" -> 46), utilisé pour générer le numéro
     * suivant de façon strictement croissante (règle de gestion §5).
     */
    @org.springframework.data.jpa.repository.Query(
        "select o.numero from OrdreCommande o"
    )
    List<String> findAllNumeros();
}
