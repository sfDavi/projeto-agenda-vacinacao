<%@ include file="/WEB-INF/jsp/layout/header.jspf" %>

<nav aria-label="breadcrumb">
    <ol class="breadcrumb">
        <li class="breadcrumb-item"><a href="${ctx}/alergias">Alergias</a></li>
        <li class="breadcrumb-item active" aria-current="page">Nova</li>
    </ol>
</nav>

<div class="row justify-content-center">
    <div class="col-12 col-md-8 col-lg-6">
        <div class="card">
            <div class="card-header"><h1 class="h5 mb-0">Nova alergia</h1></div>
            <form method="post" action="${ctx}/alergias/novo" class="card-body">
                <div class="mb-3">
                    <label for="nome" class="form-label">Nome <span class="text-danger">*</span></label>
                    <input type="text" class="form-control" id="nome" name="nome" maxlength="40" required autofocus
                           value="${fn:escapeXml(alergia.nome)}" placeholder="Ex.: Ovo, Látex, Gelatina">
                    <div class="form-text">Até 40 caracteres. Não pode repetir uma alergia já cadastrada.</div>
                </div>
                <div class="d-flex justify-content-end gap-2">
                    <a href="${ctx}/alergias" class="btn btn-outline-secondary">Cancelar</a>
                    <button type="submit" class="btn btn-primary"><i class="bi bi-check-lg"></i> Salvar</button>
                </div>
            </form>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/jsp/layout/footer.jspf" %>
