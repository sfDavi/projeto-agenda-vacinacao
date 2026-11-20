<%@ include file="/WEB-INF/jsp/layout/header.jspf" %>

<div class="d-flex flex-wrap justify-content-between align-items-end mb-3 gap-2">
    <div>
        <h1 class="h3 mb-1"><i class="bi bi-calendar-day"></i> Agendas de hoje</h1>
        <p class="text-body-secondary mb-0">${hoje} (${diaSemana}) &middot; ordenadas por situação e hora</p>
    </div>
    <a class="btn btn-primary" href="${ctx}/agendas/novo"><i class="bi bi-plus-lg"></i> Novo agendamento</a>
</div>

<div class="row g-2 mb-3">
    <c:forEach items="${totais}" var="t">
        <div class="col-4">
            <div class="card text-center">
                <div class="card-body py-2">
                    <div class="fs-4 fw-semibold">${t.value}</div>
                    <span class="badge ${t.key.classeCss}">${t.key.rotulo}</span>
                </div>
            </div>
        </div>
    </c:forEach>
</div>

<c:choose>
    <c:when test="${empty agendas}">
        <div class="card"><div class="card-body text-center text-body-secondary py-5">
            Nenhuma agenda para hoje.
        </div></div>
    </c:when>
    <c:otherwise>
        <div class="card">
            <div class="table-responsive">
                <table class="table align-middle mb-0">
                    <thead class="table-light">
                    <tr>
                        <th scope="col">Hora</th>
                        <th scope="col">Usuário</th>
                        <th scope="col">Vacina</th>
                        <th scope="col" class="text-center">Dose</th>
                        <th scope="col">Situação</th>
                        <th scope="col" class="text-end">Ações</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:set var="grupoAnterior" value="" />
                    <c:forEach items="${agendas}" var="a">
                        <c:if test="${a.situacao.nome != grupoAnterior}">
                            <tr class="table-group-divider">
                                <th colspan="6" class="bg-body-tertiary small text-uppercase">
                                    <span class="badge ${a.situacao.classeCss}">${a.situacao.rotulo}</span>
                                    <span class="text-body-secondary ms-1">${totais[a.situacao]}</span>
                                </th>
                            </tr>
                            <c:set var="grupoAnterior" value="${a.situacao.nome}" />
                        </c:if>
                        <tr>
                            <td class="fw-semibold">${a.hora}</td>
                            <td><a href="${ctx}/usuarios/detalhe?id=${a.usuario.id}" class="text-decoration-none">
                                <c:out value="${a.usuario.nome}" /></a></td>
                            <td><c:out value="${a.vacina.titulo}" /></td>
                            <td class="text-center text-nowrap">${a.dose}/${a.vacina.doses}</td>
                            <td><span class="badge ${a.situacao.classeCss}">${a.situacao.rotulo}</span></td>
                            <td class="acoes text-end">
                                <%@ include file="/WEB-INF/jsp/agendas/_botoesBaixa.jspf" %>
                                <a href="${ctx}/agendas/detalhe?id=${a.id}" class="btn btn-sm btn-outline-primary" title="Detalhes">
                                    <i class="bi bi-eye"></i></a>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/jsp/agendas/_modalBaixa.jspf" %>
<%@ include file="/WEB-INF/jsp/layout/footer.jspf" %>
