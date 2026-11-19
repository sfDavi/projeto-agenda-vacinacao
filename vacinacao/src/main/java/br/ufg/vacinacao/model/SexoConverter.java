package br.ufg.vacinacao.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Converte {@link Sexo} para a coluna {@code usuarios.sexo} ({@code 'M'}/{@code 'F'}). */
@Converter(autoApply = true)
public class SexoConverter implements AttributeConverter<Sexo, String> {

    @Override
    public String convertToDatabaseColumn(Sexo sexo) {
        return sexo == null ? null : sexo.getCodigo();
    }

    @Override
    public Sexo convertToEntityAttribute(String codigo) {
        return Sexo.deCodigo(codigo);
    }
}
