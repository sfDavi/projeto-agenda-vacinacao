<%@ include file="/WEB-INF/jsp/layout/header.jspf" %>

<nav aria-label="breadcrumb">
    <ol class="breadcrumb">
        <li class="breadcrumb-item"><a href="${ctx}/agendas">Agendas</a></li>
        <li class="breadcrumb-item active" aria-current="page">Novo agendamento</li>
    </ol>
</nav>

<div class="row g-4">
    <div class="col-12 col-lg-7">
        <div class="card">
            <div class="card-header"><h1 class="h5 mb-0">Novo agendamento</h1></div>
            <form method="post" action="${ctx}/agendas/novo" class="card-body" id="formAgenda"
                  data-previa-url="${ctx}/agendas/previa">
                <c:if test="${empty usuarios or empty vacinas}">
                    <div class="alert alert-warning">
                        Para agendar é preciso ter ao menos um
                        <a href="${ctx}/usuarios/novo">usuário</a> e uma <a href="${ctx}/vacinas/novo">vacina</a> cadastrados.
                    </div>
                </c:if>

                <div class="mb-3">
                    <label for="usuario" class="form-label">Usuário <span class="text-danger">*</span></label>
                    <select class="form-select" id="usuario" name="usuario" required>
                        <option value="">Selecione…</option>
                        <c:forEach items="${usuarios}" var="u">
                            <option value="${u.id}" ${u.id == usuarioId ? 'selected' : ''}><c:out value="${u.nome}" /></option>
                        </c:forEach>
                    </select>
                </div>

                <div class="mb-3">
                    <label for="vacina" class="form-label">Vacina <span class="text-danger">*</span></label>
                    <select class="form-select" id="vacina" name="vacina" required>
                        <option value="">Selecione…</option>
                        <c:forEach items="${vacinas}" var="v">
                            <option value="${v.id}" ${v.id == vacinaId ? 'selected' : ''}>
                                <c:out value="${v.titulo}" /> &mdash; <c:out value="${v.esquema}" /></option>
                        </c:forEach>
                    </select>
                </div>

                <div class="row g-3 mb-3">
                    <div class="col-7">
                        <label for="data" class="form-label">Data da 1ª dose <span class="text-danger">*</span></label>
                        <input type="date" class="form-control" id="data" name="data" required min="${hojeIso}"
                               value="${fn:escapeXml(data)}">
                    </div>
                    <div class="col-5">
                        <label for="hora" class="form-label">Hora <span class="text-danger">*</span></label>
                        <input type="time" class="form-control" id="hora" name="hora" required
                               value="${fn:escapeXml(hora)}">
                    </div>
                </div>

                <div class="mb-3">
                    <label for="observacoes" class="form-label">Observações</label>
                    <textarea class="form-control" id="observacoes" name="observacoes" maxlength="200" rows="2"
                              placeholder="Opcional, até 200 caracteres">${fn:escapeXml(observacoes)}</textarea>
                </div>

                <%-- Alerta de contraindicação por alergia (mostrado pelo servidor após tentativa de salvar
                     e, ao vivo, por app.js consultando /agendas/contraindicacoes) --%>
                <div class="alert alert-danger" id="alertaContraindicacao" role="alert"
                     data-contraindicacoes-url="${ctx}/agendas/contraindicacoes" ${empty conflitos ? 'hidden' : ''}>
                    <h2 class="h6 alert-heading mb-1"><i class="bi bi-exclamation-octagon-fill"></i> Contraindicação por alergia</h2>
                    <p class="mb-2">O usuário tem alergia a
                        <strong data-alergias-conflito><c:forEach items="${conflitos}" var="nomeAlergia" varStatus="s"><c:out value="${nomeAlergia}" />${s.last ? '' : ', '}</c:forEach></strong>,
                        registrada como contraindicação desta vacina.</p>
                    <div class="form-check">
                        <input class="form-check-input" type="checkbox" name="ciente" value="1" id="ciente" ${ciente ? 'checked' : ''}>
                        <label class="form-check-label fw-semibold" for="ciente">Estou ciente &mdash; prosseguir mesmo assim</label>
                    </div>
                    <div class="small mt-1">O alerta ficará registrado nas observações de cada dose.</div>
                </div>

                <div class="d-flex justify-content-end gap-2">
                    <a href="${ctx}/agendas" class="btn btn-outline-secondary">Cancelar</a>
                    <button type="submit" class="btn btn-primary"><i class="bi bi-calendar-plus"></i> Agendar</button>
                </div>
            </form>
        </div>
    </div>

    <div class="col-12 col-lg-5">
        <div class="card h-100">
            <div class="card-header"><i class="bi bi-list-ol"></i> Prévia das doses</div>
            <div class="card-body" id="previa" aria-live="polite">
                <p class="text-body-secondary mb-0">Escolha a vacina e a data para ver as doses que serão agendadas.</p>
            </div>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/jsp/layout/footer.jspf" %>
