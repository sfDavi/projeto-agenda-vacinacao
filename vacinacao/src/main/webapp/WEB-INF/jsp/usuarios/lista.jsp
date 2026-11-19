<%@ include file="/WEB-INF/jsp/layout/header.jspf" %>

<div class="d-flex flex-wrap justify-content-between align-items-center mb-3 gap-2">
    <h1 class="h3 mb-0"><i class="bi bi-people"></i> Usuários</h1>
    <a class="btn btn-primary" href="${ctx}/usuarios/novo"><i class="bi bi-person-plus"></i> Novo usuário</a>
</div>

<form method="get" action="${ctx}/usuarios" class="mb-3" role="search">
    <div class="input-group">
        <span class="input-group-text"><i class="bi bi-search"></i></span>
        <input type="search" class="form-control" name="busca" value="${fn:escapeXml(busca)}"
               placeholder="Buscar por nome" aria-label="Buscar por nome">
        <button type="submit" class="btn btn-outline-primary">Buscar</button>
        <c:if test="${not empty busca}">
            <a href="${ctx}/usuarios" class="btn btn-outline-secondary">Limpar</a>
        </c:if>
    </div>
</form>

<c:choose>
    <c:when test="${empty usuarios and not empty busca}">
        <div class="card"><div class="card-body text-center text-body-secondary py-5">
            Nenhum usuário encontrado para "<c:out value="${busca}" />".
        </div></div>
    </c:when>
    <c:when test="${empty usuarios}">
        <div class="card"><div class="card-body text-center text-body-secondary py-5">
            Nenhum usuário cadastrado.
            <a href="${ctx}/usuarios/novo">Cadastre o primeiro</a>.
        </div></div>
    </c:when>
    <c:otherwise>
        <div class="card">
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead class="table-light">
                    <tr>
                        <th scope="col">Nome</th>
                        <th scope="col" class="text-center">Idade</th>
                        <th scope="col">Sexo</th>
                        <th scope="col">Cidade/UF</th>
                        <th scope="col" class="text-center">Alergias</th>
                        <th scope="col" class="text-end">Ações</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${usuarios}" var="u">
                        <c:set var="qtdAgendas" value="${empty agendasPorUsuario[u.id] ? 0 : agendasPorUsuario[u.id]}" />
                        <tr>
                            <td>
                                <a href="${ctx}/usuarios/detalhe?id=${u.id}" class="fw-semibold text-decoration-none">
                                    <c:out value="${u.nome}" /></a>
                            </td>
                            <td class="text-center">${u.idade}</td>
                            <td>${u.sexo.rotulo}</td>
                            <td><c:out value="${u.cidadeUf}" /></td>
                            <td class="text-center">
                                <c:choose>
                                    <c:when test="${empty u.alergias}"><span class="text-body-secondary">0</span></c:when>
                                    <c:otherwise>
                                        <span class="badge text-bg-warning" title="<c:forEach items='${u.alergias}' var='a' varStatus='s'>${fn:escapeXml(a.nome)}${s.last ? '' : ', '}</c:forEach>">
                                            ${fn:length(u.alergias)}</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td class="acoes text-end">
                                <a href="${ctx}/usuarios/detalhe?id=${u.id}" class="btn btn-sm btn-outline-primary" title="Detalhes">
                                    <i class="bi bi-eye"></i> Detalhes</a>
                                <form method="post" action="${ctx}/usuarios/excluir" class="d-inline"
                                      data-confirmar="Excluir o usuário &quot;${fn:escapeXml(u.nome)}&quot;?${qtdAgendas > 0 ? ' Suas ' += qtdAgendas += ' agenda(s) também serão removidas.' : ''}">
                                    <input type="hidden" name="id" value="${u.id}">
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
            <div class="card-footer small text-body-secondary">${fn:length(usuarios)} usuário(s)</div>
        </div>
    </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/jsp/layout/footer.jspf" %>
