<%@ include file="/WEB-INF/jsp/layout/header.jspf" %>

<nav aria-label="breadcrumb">
    <ol class="breadcrumb">
        <li class="breadcrumb-item"><a href="${ctx}/vacinas">Vacinas</a></li>
        <li class="breadcrumb-item active" aria-current="page">Nova</li>
    </ol>
</nav>

<div class="row justify-content-center">
    <div class="col-12 col-lg-8">
        <div class="card">
            <div class="card-header"><h1 class="h5 mb-0">Nova vacina</h1></div>
            <form method="post" action="${ctx}/vacinas/novo" class="card-body" id="formVacina">
                <div class="mb-3">
                    <label for="titulo" class="form-label">Título <span class="text-danger">*</span></label>
                    <input type="text" class="form-control" id="titulo" name="titulo" maxlength="60" required autofocus
                           value="${fn:escapeXml(vacina.titulo)}" placeholder="Ex.: Hepatite B">
                </div>

                <div class="mb-3">
                    <label for="descricao" class="form-label">Descrição</label>
                    <textarea class="form-control" id="descricao" name="descricao" maxlength="200" rows="2"
                              placeholder="Opcional, até 200 caracteres">${fn:escapeXml(vacina.descricao)}</textarea>
                </div>

                <div class="row g-3 mb-3">
                    <div class="col-12 col-sm-4">
                        <label for="doses" class="form-label">Doses <span class="text-danger">*</span></label>
                        <input type="number" class="form-control" id="doses" name="doses" min="1" step="1" required
                               value="${vacina.doses}">
                    </div>
                    <div class="col-12 col-sm-4">
                        <label for="periodicidade" class="form-label">Periodicidade</label>
                        <select class="form-select" id="periodicidade" name="periodicidade" data-multidose>
                            <option value="">Selecione…</option>
                            <c:forEach items="${periodicidades}" var="p">
                                <option value="${p.codigo}" ${vacina.periodicidade == p ? 'selected' : ''}>${p.rotulo}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="col-12 col-sm-4">
                        <label for="intervalo" class="form-label">Intervalo</label>
                        <input type="number" class="form-control" id="intervalo" name="intervalo" min="1" step="1"
                               value="${vacina.intervalo}" data-multidose>
                    </div>
                </div>
                <p class="form-text mt-n2" id="ajudaDoses">
                    Para vacinas com mais de uma dose, as próximas serão agendadas a cada
                    <em>intervalo</em> unidades da <em>periodicidade</em>
                    (ex.: 5 doses, periodicidade anos, intervalo 3 = uma dose a cada 3 anos).
                    Com dose única, esses campos não se aplicam.
                </p>

                <fieldset class="mb-3">
                    <legend class="form-label fs-6 mb-1">
                        <i class="bi bi-exclamation-octagon text-danger"></i> Contraindicações (alergias)
                    </legend>
                    <c:choose>
                        <c:when test="${empty alergias}">
                            <p class="text-body-secondary small mb-0">
                                Nenhuma alergia cadastrada. <a href="${ctx}/alergias/novo">Cadastrar alergia</a>.
                            </p>
                        </c:when>
                        <c:otherwise>
                            <div class="row row-cols-2 row-cols-md-3 g-2">
                                <c:forEach items="${alergias}" var="a">
                                    <div class="col">
                                        <div class="form-check">
                                            <input class="form-check-input" type="checkbox" name="contraindicacoes"
                                                   id="contra${a.id}" value="${a.id}"
                                                ${contraindicacoesSelecionadas.contains(a.id) ? 'checked' : ''}>
                                            <label class="form-check-label" for="contra${a.id}"><c:out value="${a.nome}" /></label>
                                        </div>
                                    </div>
                                </c:forEach>
                            </div>
                            <div class="form-text">
                                Ao agendar esta vacina para um usuário com alguma dessas alergias, o sistema exibirá um alerta.
                            </div>
                        </c:otherwise>
                    </c:choose>
                </fieldset>

                <div class="d-flex justify-content-end gap-2">
                    <a href="${ctx}/vacinas" class="btn btn-outline-secondary">Cancelar</a>
                    <button type="submit" class="btn btn-primary"><i class="bi bi-check-lg"></i> Salvar</button>
                </div>
            </form>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/jsp/layout/footer.jspf" %>
