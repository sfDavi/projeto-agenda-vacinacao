<%@ include file="/WEB-INF/jsp/layout/header.jspf" %>

<nav aria-label="breadcrumb">
    <ol class="breadcrumb">
        <li class="breadcrumb-item"><a href="${ctx}/vacinas">Vacinas</a></li>
        <li class="breadcrumb-item active" aria-current="page"><c:out value="${vacina.titulo}" /></li>
    </ol>
</nav>

<div class="card">
    <div class="card-header d-flex flex-wrap justify-content-between align-items-center gap-2">
        <h1 class="h4 mb-0"><i class="bi bi-capsule"></i> <c:out value="${vacina.titulo}" /></h1>
        <span class="badge text-bg-info"><c:out value="${vacina.esquema}" /></span>
    </div>
    <div class="card-body">
        <dl class="row mb-0">
            <dt class="col-sm-4 col-lg-3">Código</dt>
            <dd class="col-sm-8 col-lg-9">${vacina.id}</dd>

            <dt class="col-sm-4 col-lg-3">Descrição</dt>
            <dd class="col-sm-8 col-lg-9">
                <c:choose>
                    <c:when test="${empty vacina.descricao}"><span class="text-body-secondary">Sem descrição</span></c:when>
                    <c:otherwise><c:out value="${vacina.descricao}" /></c:otherwise>
                </c:choose>
            </dd>

            <dt class="col-sm-4 col-lg-3">Doses</dt>
            <dd class="col-sm-8 col-lg-9">${vacina.doses}</dd>

            <dt class="col-sm-4 col-lg-3">Periodicidade</dt>
            <dd class="col-sm-8 col-lg-9">
                <c:choose>
                    <c:when test="${vacina.doseUnica}"><span class="text-body-secondary">Não se aplica (dose única)</span></c:when>
                    <c:otherwise>${vacina.periodicidade.rotulo}</c:otherwise>
                </c:choose>
            </dd>

            <dt class="col-sm-4 col-lg-3">Intervalo</dt>
            <dd class="col-sm-8 col-lg-9">
                <c:choose>
                    <c:when test="${vacina.doseUnica}"><span class="text-body-secondary">Não se aplica (dose única)</span></c:when>
                    <c:otherwise>${vacina.periodicidade.descrever(vacina.intervalo)} entre as doses</c:otherwise>
                </c:choose>
            </dd>

            <dt class="col-sm-4 col-lg-3">Contraindicações</dt>
            <dd class="col-sm-8 col-lg-9">
                <c:choose>
                    <c:when test="${empty vacina.contraindicacoes}"><span class="text-body-secondary">Nenhuma alergia registrada como contraindicação</span></c:when>
                    <c:otherwise>
                        <div class="d-flex flex-wrap gap-2">
                            <c:forEach items="${vacina.contraindicacoes}" var="a">
                                <span class="badge rounded-pill text-bg-danger fw-normal">
                                    <i class="bi bi-exclamation-octagon"></i> <c:out value="${a.nome}" /></span>
                            </c:forEach>
                        </div>
                    </c:otherwise>
                </c:choose>
            </dd>

            <dt class="col-sm-4 col-lg-3">Agendas</dt>
            <dd class="col-sm-8 col-lg-9">
                <c:choose>
                    <c:when test="${totalAgendas == 0}">Nenhuma agenda usa esta vacina</c:when>
                    <c:otherwise>${totalAgendas} agenda(s) usam esta vacina</c:otherwise>
                </c:choose>
            </dd>
        </dl>
    </div>
    <div class="card-footer d-flex justify-content-between flex-wrap gap-2">
        <a href="${ctx}/vacinas" class="btn btn-outline-secondary"><i class="bi bi-arrow-left"></i> Voltar</a>
        <a href="${ctx}/vacinas/editar?id=${vacina.id}" class="btn btn-outline-secondary"><i class="bi bi-pencil"></i> Editar</a>
        <a href="${ctx}/agendas/novo?vacina=${vacina.id}" class="btn btn-primary ms-auto">
            <i class="bi bi-calendar-plus"></i> Agendar esta vacina</a>
        <form method="post" action="${ctx}/vacinas/excluir" class="d-inline"
              data-confirmar="Excluir a vacina &quot;${fn:escapeXml(vacina.titulo)}&quot;?">
            <input type="hidden" name="id" value="${vacina.id}">
            <button type="submit" class="btn btn-outline-danger" ${totalAgendas > 0 ? 'disabled title="Vacina em uso por agendas"' : ''}>
                <i class="bi bi-trash"></i> Excluir
            </button>
        </form>
    </div>
</div>

<%@ include file="/WEB-INF/jsp/layout/footer.jspf" %>
