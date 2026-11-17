package br.ufg.vacinacao.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Converte {@link Periodicidade} para o inteiro da coluna {@code vacinas.periodicidade} (1 a 4). */
@Converter(autoApply = true)
public class PeriodicidadeConverter implements AttributeConverter<Periodicidade, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Periodicidade periodicidade) {
        return periodicidade == null ? null : periodicidade.getCodigo();
    }

    @Override
    public Periodicidade convertToEntityAttribute(Integer codigo) {
        return Periodicidade.deCodigo(codigo);
    }
}
