<%@ include file="/WEB-INF/jsp/layout/header.jspf" %>

<div class="d-flex flex-wrap justify-content-between align-items-center mb-3 gap-2">
    <h1 class="h3 mb-0"><i class="bi bi-exclamation-triangle"></i> Alergias</h1>
    <a class="btn btn-primary" href="${ctx}/alergias/novo"><i class="bi bi-plus-lg"></i> Nova alergia</a>
</div>

<c:choose>
    <c:when test="${empty alergias}">
        <div class="card"><div class="card-body text-center text-body-secondary py-5">
            Nenhuma alergia cadastrada.
            <a href="${ctx}/alergias/novo">Cadastre a primeira</a>.
        </div></div>
    </c:when>
    <c:otherwise>
        <div class="card">
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead class="table-light">
                    <tr>
                        <th scope="col" style="width: 6rem">Código</th>
                        <th scope="col">Nome</th>
                        <th scope="col" class="text-end">Ações</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${alergias}" var="a">
                        <tr>
                            <td class="text-body-secondary">${a.id}</td>
                            <td><c:out value="${a.nome}" /></td>
                            <td class="acoes text-end">
                                <form method="post" action="${ctx}/alergias/excluir" class="d-inline"
                                      data-confirmar="Excluir a alergia &quot;${fn:escapeXml(a.nome)}&quot;?">
                                    <input type="hidden" name="id" value="${a.id}">
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
            <div class="card-footer small text-body-secondary">${fn:length(alergias)} alergia(s)</div>
        </div>
    </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/jsp/layout/footer.jspf" %>
