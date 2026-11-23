<%@ include file="/WEB-INF/jsp/layout/header.jspf" %>

<div class="d-flex flex-wrap justify-content-between align-items-center mb-3 gap-2">
    <h1 class="h3 mb-0"><i class="bi bi-capsule"></i> Vacinas</h1>
    <a class="btn btn-primary" href="${ctx}/vacinas/novo"><i class="bi bi-plus-lg"></i> Nova vacina</a>
</div>

<c:choose>
    <c:when test="${empty vacinas}">
        <div class="card"><div class="card-body text-center text-body-secondary py-5">
            Nenhuma vacina cadastrada.
            <a href="${ctx}/vacinas/novo">Cadastre a primeira</a>.
        </div></div>
    </c:when>
    <c:otherwise>
        <div class="card">
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead class="table-light">
                    <tr>
                        <th scope="col">Título</th>
                        <th scope="col" class="text-center">Doses</th>
                        <th scope="col">Periodicidade</th>
                        <th scope="col" class="text-center">Intervalo</th>
                        <th scope="col" class="text-center">Contraindicações</th>
                        <th scope="col" class="text-end">Ações</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${vacinas}" var="v">
                        <tr>
                            <td>
                                <a href="${ctx}/vacinas/detalhe?id=${v.id}" class="fw-semibold text-decoration-none">
                                    <c:out value="${v.titulo}" /></a>
                                <c:if test="${not empty v.descricao}">
                                    <div class="small text-body-secondary text-truncate" style="max-width: 28rem">
                                        <c:out value="${v.descricao}" /></div>
                                </c:if>
                            </td>
                            <td class="text-center">${v.doses}</td>
                            <td>
                                <c:choose>
                                    <c:when test="${v.doseUnica}"><span class="text-body-secondary">Dose única</span></c:when>
                                    <c:otherwise>${v.periodicidade.rotulo}</c:otherwise>
                                </c:choose>
                            </td>
                            <td class="text-center">
                                <c:choose>
                                    <c:when test="${v.doseUnica}"><span class="text-body-secondary">&mdash;</span></c:when>
                                    <c:otherwise>${v.intervalo}</c:otherwise>
                                </c:choose>
                            </td>
                            <td class="text-center">
                                <c:choose>
                                    <c:when test="${empty v.contraindicacoes}"><span class="text-body-secondary">0</span></c:when>
                                    <c:otherwise>
                                        <span class="badge text-bg-danger" title="<c:forEach items='${v.contraindicacoes}' var='a' varStatus='s'>${fn:escapeXml(a.nome)}${s.last ? '' : ', '}</c:forEach>">
                                            ${fn:length(v.contraindicacoes)}</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td class="acoes text-end">
                                <a href="${ctx}/vacinas/detalhe?id=${v.id}" class="btn btn-sm btn-outline-primary" title="Detalhes">
                                    <i class="bi bi-eye"></i> Detalhes</a>
                                <a href="${ctx}/vacinas/editar?id=${v.id}" class="btn btn-sm btn-outline-secondary" title="Editar">
                                    <i class="bi bi-pencil"></i></a>
                                <form method="post" action="${ctx}/vacinas/excluir" class="d-inline"
                                      data-confirmar="Excluir a vacina &quot;${fn:escapeXml(v.titulo)}&quot;?">
                                    <input type="hidden" name="id" value="${v.id}">
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
            <div class="card-footer small text-body-secondary">${fn:length(vacinas)} vacina(s)</div>
        </div>
    </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/jsp/layout/footer.jspf" %>
