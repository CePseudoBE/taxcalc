package be.hoffmann.backtaxes.entity.enums;

/**
 * Normes europeennes d'emissions (Euro 1 a Euro 7).
 *
 * Ces normes definissent les limites d'emissions polluantes des vehicules.
 * Plus le numero est eleve, plus la norme est stricte.
 *
 * Note: euro_6d_temp et euro_6d sont des sous-categories de Euro 6
 * avec des tests d'emissions plus stricts (WLTP au lieu de NEDC).
 */
public enum EuroNorm {
    euro_1,      // 1992 - Premiere norme
    euro_2,      // 1996
    euro_3,      // 2000
    euro_4,      // 2005
    euro_5,      // 2009
    euro_6,      // 2014
    euro_6d_temp,// 2017 - Euro 6d transitoire (WLTP)
    euro_6d,     // 2019 - Euro 6d complet (WLTP + RDE)
    euro_7       // 2025 - Norme la plus recente
}
