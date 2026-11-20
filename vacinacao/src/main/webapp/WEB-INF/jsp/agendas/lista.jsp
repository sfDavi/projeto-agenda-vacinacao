<%@ include file="/WEB-INF/jsp/layout/header.jspf" %>

<div class="d-flex flex-wrap justify-content-between align-items-center mb-3 gap-2">
    <h1 class="h3 mb-0"><i class="bi bi-calendar-check"></i> Agendas</h1>
    <div class="d-flex gap-2">
        <a class="btn btn-outline-primary" href="${ctx}/agendas/hoje"><i class="bi bi-calendar-day"></i> Hoje</a>
        <a class="btn btn-primary" href="${ctx}/agendas/novo"><i class="bi bi-plus-lg"></i> Novo agendamento</a>
    </div>
</div>

<%-- Atalhos por situação (Item 3) --%>
<div class="d-flex flex-wrap gap-2 mb-3" role="group" aria-label="Filtrar por situação">
    <a class="btn btn-sm ${empty filtroSituacao ? 'btn-dark' : 'btn-outline-dark'}" href="${ctx}/agendas">Todas</a>
    <c:forEach items="${situacoes}" var="s">
        <a class="btn btn-sm ${filtroSituacao == s.nome ? 'btn-dark' : 'btn-outline-dark'}"
           href="${ctx}/agendas?situacao=${s.nome}">
            <span class="badge ${s.classeCss} me-1">&nbsp;</span>${s.rotulo}s</a>
    </c:forEach>
</div>

<form method="get" action="${ctx}/agendas" class="card card-body mb-3">
    <div class="row g-2 align-items-end">
        <div class="col-6 col-md-2">
            <label for="fSituacao" class="form-label small mb-1">Situação</label>
            <select class="form-select form-select-sm" id="fSituacao" name="situacao">
                <option value="">Todas</option>
                <c:forEach items="${situacoes}" var="s">
                    <option value="${s.nome}" ${filtroSituacao == s.nome ? 'selected' : ''}>${s.rotulo}</option>
                </c:forEach>
            </select>
        </div>
        <div class="col-6 col-md-2">
            <label for="fDe" class="form-label small mb-1">De</label>
            <input type="date" class="form-control form-control-sm" id="fDe" name="de" value="${filtroDe}">
        </div>
        <div class="col-6 col-md-2">
            <label for="fAte" class="form-label small mb-1">Até</label>
            <input type="date" class="form-control form-control-sm" id="fAte" name="ate" value="${filtroAte}">
        </div>
        <div class="col-6 col-md-2">
            <label for="fUsuario" class="form-label small mb-1">Usuário</label>
            <select class="form-select form-select-sm" id="fUsuario" name="usuario">
                <option value="">Todos</option>
                <c:forEach items="${usuarios}" var="u">
                    <option value="${u.id}" ${u.id == filtroUsuario ? 'selected' : ''}><c:out value="${u.nome}" /></option>
                </c:forEach>
            </select>
        </div>
        <div class="col-6 col-md-2">
            <label for="fVacina" class="form-label small mb-1">Vacina</label>
            <select class="form-select form-select-sm" id="fVacina" name="vacina">
                <option value="">Todas</option>
                <c:forEach items="${vacinas}" var="v">
                    <option value="${v.id}" ${v.id == filtroVacina ? 'selected' : ''}><c:out value="${v.titulo}" /></option>
                </c:forEach>
            </select>
        </div>
        <div class="col-6 col-md-2 d-flex gap-2">
            <button type="submit" class="btn btn-sm btn-primary flex-grow-1"><i class="bi bi-funnel"></i> Filtrar</button>
            <c:if test="${filtrando}">
                <a href="${ctx}/agendas" class="btn btn-sm btn-outline-secondary" title="Limpar filtros"><i class="bi bi-x-lg"></i></a>
            </c:if>
        </div>
    </div>
</form>

<c:choose>
    <c:when test="${empty agendas and filtrando}">
        <div class="card"><div class="card-body text-center text-body-secondary py-5">
            Nenhuma agenda encontrada com esses filtros. <a href="${ctx}/agendas">Limpar filtros</a>.
        </div></div>
    </c:when>
    <c:when test="${empty agendas}">
        <div class="card"><div class="card-body text-center text-body-secondary py-5">
            Nenhuma agenda cadastrada.
            <a href="${ctx}/agendas/novo">Faça o primeiro agendamento</a>.
        </div></div>
    </c:when>
    <c:otherwise>
        <div class="card">
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead class="table-light">
                    <tr>
                        <th scope="col">Data</th>
                        <th scope="col">Hora</th>
                        <th scope="col">Usuário</th>
                        <th scope="col">Vacina</th>
                        <th scope="col" class="text-center">Dose</th>
                        <th scope="col">Situação</th>
                        <th scope="col">Baixa em</th>
                        <th scope="col" class="text-end">Ações</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${agendas}" var="a">
                        <tr>
                            <td class="text-nowrap">${a.dataFormatada}</td>
                            <td>${a.hora}</td>
                            <td><a href="${ctx}/usuarios/detalhe?id=${a.usuario.id}" class="text-decoration-none">
                                <c:out value="${a.usuario.nome}" /></a></td>
                            <td><a href="${ctx}/vacinas/detalhe?id=${a.vacina.id}" class="text-decoration-none">
                                <c:out value="${a.vacina.titulo}" /></a></td>
                            <td class="text-center text-nowrap">${a.dose}/${a.vacina.doses}</td>
                            <td><span class="badge ${a.situacao.classeCss}">${a.situacao.rotulo}</span></td>
                            <td class="text-nowrap">
                                <c:choose>
                                    <c:when test="${empty a.dataSituacao}"><span class="text-body-secondary">&mdash;</span></c:when>
                                    <c:otherwise>${a.dataSituacaoFormatada}</c:otherwise>
                                </c:choose>
                            </td>
                            <td class="acoes text-end">
                                <%@ include file="/WEB-INF/jsp/agendas/_botoesBaixa.jspf" %>
                                <a href="${ctx}/agendas/detalhe?id=${a.id}" class="btn btn-sm btn-outline-primary" title="Detalhes">
                                    <i class="bi bi-eye"></i></a>
                                <form method="post" action="${ctx}/agendas/excluir" class="d-inline"
                                      data-confirmar="Excluir a agenda de ${fn:escapeXml(a.vacina.titulo)} (dose ${a.dose}) de ${fn:escapeXml(a.usuario.nome)} em ${a.dataFormatada}?">
                                    <input type="hidden" name="id" value="${a.id}">
                                    <input type="hidden" name="escopo" value="agenda">
                                    <button type="submit" class="btn btn-sm btn-outline-danger" title="Excluir">
                                        <i class="bi bi-trash"></i>
                                    </button>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
            <div class="card-footer small text-body-secondary">${fn:length(agendas)} agenda(s)</div>
        </div>
    </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/jsp/agendas/_modalBaixa.jspf" %>
<%@ include file="/WEB-INF/jsp/layout/footer.jspf" %>
