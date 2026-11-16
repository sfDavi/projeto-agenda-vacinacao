<%@ include file="/WEB-INF/jsp/layout/header.jspf" %>

<div class="d-flex flex-wrap justify-content-between align-items-end mb-4 gap-2">
    <div>
        <h1 class="h3 mb-1">Painel</h1>
        <p class="text-body-secondary mb-0">Hoje é <strong><c:out value="${hoje}" /></strong></p>
    </div>
    <a class="btn btn-primary" href="${ctx}/agendas/novo"><i class="bi bi-plus-lg"></i> Novo agendamento</a>
</div>

<c:if test="${not empty erroJpa}">
    <div class="alert alert-danger" role="alert">
        <h2 class="h6 alert-heading"><i class="bi bi-database-x"></i> Não foi possível conectar ao banco de dados</h2>
        <p class="mb-1">Confira se o MySQL está rodando, se o banco <code>vacinacao</code> foi criado com
            <code>sql/schema.sql</code> e se as variáveis <code>VACINACAO_DB_USER</code> /
            <code>VACINACAO_DB_PASSWORD</code> estão corretas.</p>
        <small class="font-monospace"><c:out value="${erroJpa}" /></small>
    </div>
</c:if>

<div class="row g-3">
    <div class="col-12 col-lg-7">
        <div class="card h-100">
            <div class="card-header d-flex justify-content-between align-items-center">
                <span><i class="bi bi-calendar-day"></i> Agendas de hoje</span>
                <a class="small" href="${ctx}/agendas/hoje">Ver todas</a>
            </div>
            <div class="card-body text-body-secondary">
                As agendas do dia aparecerão aqui.
            </div>
        </div>
    </div>
    <div class="col-12 col-lg-5">
        <div class="card h-100">
            <div class="card-header d-flex justify-content-between align-items-center">
                <span><i class="bi bi-clock-history"></i> Próximas aplicações</span>
                <a class="small" href="${ctx}/doses/proximas">Ver todas</a>
            </div>
            <div class="card-body text-body-secondary">
                As próximas doses e as atrasadas aparecerão aqui.
            </div>
        </div>
    </div>
</div>

<h2 class="h5 mt-4 mb-3">Atalhos</h2>
<div class="row g-3 atalhos">
    <div class="col-6 col-md-3">
        <a class="card text-decoration-none h-100" href="${ctx}/usuarios">
            <div class="card-body text-center"><i class="bi bi-people fs-2"></i><div>Usuários</div></div>
        </a>
    </div>
    <div class="col-6 col-md-3">
        <a class="card text-decoration-none h-100" href="${ctx}/vacinas">
            <div class="card-body text-center"><i class="bi bi-capsule fs-2"></i><div>Vacinas</div></div>
        </a>
    </div>
    <div class="col-6 col-md-3">
        <a class="card text-decoration-none h-100" href="${ctx}/alergias">
            <div class="card-body text-center"><i class="bi bi-exclamation-triangle fs-2"></i><div>Alergias</div></div>
        </a>
    </div>
    <div class="col-6 col-md-3">
        <a class="card text-decoration-none h-100" href="${ctx}/agendas">
            <div class="card-body text-center"><i class="bi bi-calendar-check fs-2"></i><div>Agendas</div></div>
        </a>
    </div>
</div>

<%@ include file="/WEB-INF/jsp/layout/footer.jspf" %>
