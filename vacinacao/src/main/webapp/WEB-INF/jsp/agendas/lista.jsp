<%@ include file="/WEB-INF/jsp/layout/header.jspf" %>

<div class="d-flex flex-wrap justify-content-between align-items-center mb-3 gap-2">
    <h1 class="h3 mb-0"><i class="bi bi-calendar-check"></i> Agendas</h1>
    <a class="btn btn-primary" href="${ctx}/agendas/novo"><i class="bi bi-plus-lg"></i> Novo agendamento</a>
</div>

<c:choose>
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
                            <td class="acoes text-end">
                                <a href="${ctx}/agendas/detalhe?id=${a.id}" class="btn btn-sm btn-outline-primary" title="Detalhes">
                                    <i class="bi bi-eye"></i> Detalhes</a>
                                <form method="post" action="${ctx}/agendas/excluir" class="d-inline"
                                      data-confirmar="Excluir a agenda de ${fn:escapeXml(a.vacina.titulo)} (dose ${a.dose}) de ${fn:escapeXml(a.usuario.nome)} em ${a.dataFormatada}?">
                                    <input type="hidden" name="id" value="${a.id}">
                                    <input type="hidden" name="escopo" value="agenda">
                                    <button type="submit" class="btn btn-sm btn-outline-danger" title="Excluir">
                                        <i class="bi bi-trash"></i> Excluir
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

<%@ include file="/WEB-INF/jsp/layout/footer.jspf" %>
