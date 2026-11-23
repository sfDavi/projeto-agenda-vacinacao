package br.ufg.vacinacao.service;

import br.ufg.vacinacao.dao.AlergiaDAO;
import br.ufg.vacinacao.model.Alergia;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Item 7A - Alerta de contraindicação por alergia.
 * <p>
 * Cruza as alergias do usuário com as alergias registradas como contraindicação da vacina
 * ({@code usuario.alergias ∩ vacina.contraindicacoes}).
 */
public class ContraindicacaoService {

    private final AlergiaDAO alergiaDAO = new AlergiaDAO();

    /** Alergias do usuário que contraindicam a vacina (lista vazia se não houver conflito). */
    public List<Alergia> conflitos(Integer usuarioId, Integer vacinaId) {
        if (usuarioId == null || vacinaId == null) {
            return List.of();
        }
        return alergiaDAO.listarConflitos(usuarioId, vacinaId);
    }

    /** id da vacina -> nomes das alergias em conflito, para todas as vacinas contraindicadas ao usuário. */
    public Map<Integer, List<String>> vacinasContraindicadas(Integer usuarioId) {
        if (usuarioId == null) {
            return Map.of();
        }
        return alergiaDAO.listarConflitosPorVacina(usuarioId);
    }

    /** Texto gravado nas observações da agenda quando o operador prossegue mesmo com o alerta. */
    public static String textoAlerta(List<String> nomesAlergias) {
        return "ALERTA: contraindicação por alergia (" + String.join(", ", nomesAlergias)
                + "); agendado ciente do risco.";
    }

    public static List<String> nomes(List<Alergia> alergias) {
        return alergias.stream().map(Alergia::getNome).collect(Collectors.toList());
    }
}
