<%@ include file="/WEB-INF/jsp/layout/header.jspf" %>

<nav aria-label="breadcrumb">
    <ol class="breadcrumb">
        <li class="breadcrumb-item"><a href="${ctx}/usuarios">Usuários</a></li>
        <li class="breadcrumb-item active" aria-current="page">Novo</li>
    </ol>
</nav>

<div class="row justify-content-center">
    <div class="col-12 col-xl-10">
        <div class="card">
            <div class="card-header"><h1 class="h5 mb-0">Novo usuário</h1></div>
            <form method="post" action="${ctx}/usuarios/novo" class="card-body">

                <h2 class="h6 text-body-secondary text-uppercase mb-3">Dados pessoais</h2>
                <div class="row g-3 mb-4">
                    <div class="col-12 col-md-6">
                        <label for="nome" class="form-label">Nome <span class="text-danger">*</span></label>
                        <input type="text" class="form-control" id="nome" name="nome" maxlength="60" required autofocus
                               value="${fn:escapeXml(usuario.nome)}">
                    </div>
                    <div class="col-6 col-md-3">
                        <label for="dataNascimento" class="form-label">Nascimento <span class="text-danger">*</span></label>
                        <input type="date" class="form-control" id="dataNascimento" name="dataNascimento" required
                               min="1900-01-01" max="${hojeIso}" value="${usuario.dataNascimento}">
                    </div>
                    <div class="col-6 col-md-3">
                        <span class="form-label d-block mb-2">Sexo <span class="text-danger">*</span></span>
                        <c:forEach items="${sexos}" var="s">
                            <div class="form-check form-check-inline mt-1">
                                <input class="form-check-input" type="radio" name="sexo" id="sexo${s.codigo}"
                                       value="${s.codigo}" required ${usuario.sexo == s ? 'checked' : ''}>
                                <label class="form-check-label" for="sexo${s.codigo}">${s.rotulo}</label>
                            </div>
                        </c:forEach>
                    </div>
                </div>

                <h2 class="h6 text-body-secondary text-uppercase mb-3">Endereço</h2>
                <div class="row g-3 mb-4">
                    <div class="col-12 col-md-8">
                        <label for="logradouro" class="form-label">Logradouro</label>
                        <input type="text" class="form-control" id="logradouro" name="logradouro" maxlength="60"
                               value="${fn:escapeXml(usuario.logradouro)}" placeholder="Rua, avenida, quadra…">
                    </div>
                    <div class="col-12 col-md-4">
                        <label for="numero" class="form-label">Número</label>
                        <input type="text" class="form-control" id="numero" name="numero" inputmode="numeric"
                               pattern="[0-9]{1,10}" maxlength="10" title="Apenas dígitos (até 10)"
                               value="${usuario.numero}">
                    </div>
                    <div class="col-12 col-md-5">
                        <label for="setor" class="form-label">Setor/Bairro</label>
                        <input type="text" class="form-control" id="setor" name="setor" maxlength="40"
                               value="${fn:escapeXml(usuario.setor)}">
                    </div>
                    <div class="col-8 col-md-5">
                        <label for="cidade" class="form-label">Cidade</label>
                        <input type="text" class="form-control" id="cidade" name="cidade" maxlength="40"
                               value="${fn:escapeXml(usuario.cidade)}">
                    </div>
                    <div class="col-4 col-md-2">
                        <label for="uf" class="form-label">UF <span class="text-danger">*</span></label>
                        <select class="form-select" id="uf" name="uf" required>
                            <option value="">--</option>
                            <c:forEach items="${ufs}" var="estado">
                                <option value="${estado.sigla}" title="${estado.nome}"
                                    ${usuario.uf == estado ? 'selected' : ''}>${estado.sigla}</option>
                            </c:forEach>
                        </select>
                    </div>
                </div>

                <h2 class="h6 text-body-secondary text-uppercase mb-3">Alergias</h2>
                <c:choose>
                    <c:when test="${empty alergias}">
                        <p class="text-body-secondary">
                            Nenhuma alergia cadastrada. <a href="${ctx}/alergias/novo">Cadastrar alergia</a>.
                        </p>
                    </c:when>
                    <c:otherwise>
                        <div class="row row-cols-2 row-cols-md-3 row-cols-lg-4 g-2 mb-1">
                            <c:forEach items="${alergias}" var="a">
                                <div class="col">
                                    <div class="form-check">
                                        <input class="form-check-input" type="checkbox" name="alergias"
                                               id="alergia${a.id}" value="${a.id}"
                                            ${alergiasSelecionadas.contains(a.id) ? 'checked' : ''}>
                                        <label class="form-check-label" for="alergia${a.id}"><c:out value="${a.nome}" /></label>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                        <div class="form-text mb-3">Marque todas as alergias do usuário (ou nenhuma).</div>
                    </c:otherwise>
                </c:choose>

                <div class="d-flex justify-content-end gap-2 mt-3">
                    <a href="${ctx}/usuarios" class="btn btn-outline-secondary">Cancelar</a>
                    <button type="submit" class="btn btn-primary"><i class="bi bi-check-lg"></i> Salvar</button>
                </div>
            </form>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/jsp/layout/footer.jspf" %>
