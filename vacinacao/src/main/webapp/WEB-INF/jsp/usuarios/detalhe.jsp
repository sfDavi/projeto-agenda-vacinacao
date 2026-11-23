<%@ include file="/WEB-INF/jsp/layout/header.jspf" %>

<nav aria-label="breadcrumb">
    <ol class="breadcrumb">
        <li class="breadcrumb-item"><a href="${ctx}/usuarios">Usuários</a></li>
        <li class="breadcrumb-item active" aria-current="page"><c:out value="${usuario.nome}" /></li>
    </ol>
</nav>

<div class="card">
    <div class="card-header d-flex flex-wrap justify-content-between align-items-center gap-2">
        <h1 class="h4 mb-0"><i class="bi bi-person"></i> <c:out value="${usuario.nome}" /></h1>
        <span class="text-body-secondary">${usuario.idade} anos · ${usuario.sexo.rotulo}</span>
    </div>
    <div class="card-body">
        <div class="row g-4">
            <div class="col-12 col-lg-6">
                <h2 class="h6 text-body-secondary text-uppercase">Dados pessoais</h2>
                <dl class="row mb-0">
                    <dt class="col-sm-5">Código</dt>
                    <dd class="col-sm-7">${usuario.id}</dd>
                    <dt class="col-sm-5">Data de nascimento</dt>
                    <dd class="col-sm-7">${usuario.dataNascimentoFormatada}</dd>
                    <dt class="col-sm-5">Idade</dt>
                    <dd class="col-sm-7">${usuario.idade} anos</dd>
                    <dt class="col-sm-5">Sexo</dt>
                    <dd class="col-sm-7">${usuario.sexo.rotulo}</dd>
                </dl>
            </div>
            <div class="col-12 col-lg-6">
                <h2 class="h6 text-body-secondary text-uppercase">Endereço</h2>
                <dl class="row mb-0">
                    <dt class="col-sm-5">Logradouro</dt>
                    <dd class="col-sm-7">
                        <c:choose>
                            <c:when test="${empty usuario.logradouro}"><span class="text-body-secondary">&mdash;</span></c:when>
                            <c:otherwise><c:out value="${usuario.logradouro}" /></c:otherwise>
                        </c:choose>
                    </dd>
                    <dt class="col-sm-5">Número</dt>
                    <dd class="col-sm-7">${empty usuario.numero ? '&mdash;' : usuario.numero}</dd>
                    <dt class="col-sm-5">Setor/Bairro</dt>
                    <dd class="col-sm-7">
                        <c:choose>
                            <c:when test="${empty usuario.setor}"><span class="text-body-secondary">&mdash;</span></c:when>
                            <c:otherwise><c:out value="${usuario.setor}" /></c:otherwise>
                        </c:choose>
                    </dd>
                    <dt class="col-sm-5">Cidade</dt>
                    <dd class="col-sm-7">
                        <c:choose>
                            <c:when test="${empty usuario.cidade}"><span class="text-body-secondary">&mdash;</span></c:when>
                            <c:otherwise><c:out value="${usuario.cidade}" /></c:otherwise>
                        </c:choose>
                    </dd>
                    <dt class="col-sm-5">UF</dt>
                    <dd class="col-sm-7">${usuario.uf.sigla} &ndash; ${usuario.uf.nome}</dd>
                </dl>
            </div>
            <div class="col-12">
                <h2 class="h6 text-body-secondary text-uppercase">Alergias</h2>
                <c:choose>
                    <c:when test="${empty usuario.alergias}">
                        <p class="text-body-secondary mb-0">Nenhuma alergia registrada.</p>
                    </c:when>
                    <c:otherwise>
                        <div class="d-flex flex-wrap gap-2">
                            <c:forEach items="${usuario.alergias}" var="a">
                                <span class="badge rounded-pill text-bg-warning fs-6 fw-normal">
                                    <i class="bi bi-exclamation-triangle"></i> <c:out value="${a.nome}" /></span>
                            </c:forEach>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
            <div class="col-12">
                <h2 class="h6 text-body-secondary text-uppercase">Agendas</h2>
                <p class="mb-0">
                    <c:choose>
                        <c:when test="${totalAgendas == 0}">Nenhuma agenda registrada.</c:when>
                        <c:otherwise>${totalAgendas} agenda(s) registrada(s).</c:otherwise>
                    </c:choose>
                </p>
            </div>
        </div>
    </div>
    <div class="card-footer d-flex justify-content-between flex-wrap gap-2">
        <a href="${ctx}/usuarios" class="btn btn-outline-secondary"><i class="bi bi-arrow-left"></i> Voltar</a>
        <a href="${ctx}/usuarios/editar?id=${usuario.id}" class="btn btn-outline-secondary"><i class="bi bi-pencil"></i> Editar</a>
        <a href="${ctx}/usuarios/agendas?id=${usuario.id}" class="btn btn-outline-primary ms-auto">
            <i class="bi bi-journal-medical"></i> Cartão de vacinação</a>
        <a href="${ctx}/agendas/novo?usuario=${usuario.id}" class="btn btn-primary">
            <i class="bi bi-calendar-plus"></i> Agendar vacina</a>
        <form method="post" action="${ctx}/usuarios/excluir" class="d-inline"
              data-confirmar="Excluir o usuário &quot;${fn:escapeXml(usuario.nome)}&quot;?${totalAgendas > 0 ? ' Suas ' += totalAgendas += ' agenda(s) também serão removidas.' : ''}">
            <input type="hidden" name="id" value="${usuario.id}">
            <button type="submit" class="btn btn-outline-danger"><i class="bi bi-trash"></i> Excluir</button>
        </form>
    </div>
</div>

<%@ include file="/WEB-INF/jsp/layout/footer.jspf" %>
