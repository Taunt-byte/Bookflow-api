// Funções usadas pela loja (index.html) e pelo painel (painel.html)

const $ = id => document.getElementById(id);

const brl = v => Number(v).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });

const dataBR = iso => new Date(iso).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' });

const nomeStatus = { REALIZADO: 'Realizado', ENVIADO: 'Enviado', ENTREGUE: 'Entregue', CANCELADO: 'Cancelado' };

// Evita que um texto digitado vire HTML na página
function esc(s) {
  return String(s ?? '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
}

// Chama a API e, se der erro, lança uma exceção com a mensagem que o Java mandou
async function api(url, opcoes = {}) {
  const resp = await fetch(url, opcoes);
  const texto = await resp.text();
  let dados = null;
  try { dados = texto ? JSON.parse(texto) : null; } catch { dados = null; }
  if (!resp.ok) {
    throw new Error((dados && dados.erro) || 'Erro ' + resp.status);
  }
  return dados;
}

// Envia um objeto como JSON (POST/PUT)
function enviarJson(url, metodo, corpo) {
  return api(url, { method: metodo, headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(corpo) });
}

function entrar(email, senha) {
  return api('/api/auth/login', { method: 'POST', body: new URLSearchParams({ email, senha }) });
}

function sairDaConta() {
  return api('/api/auth/logout', { method: 'POST' });
}

function usuarioAtual() {
  return api('/api/auth/me');
}

function mostrarMensagem(texto) {
  let t = $('toast');
  if (!t) { t = document.createElement('div'); t.id = 'toast'; document.body.appendChild(t); }
  t.textContent = texto;
  t.classList.remove('oculto');
  clearTimeout(mostrarMensagem.timer);
  mostrarMensagem.timer = setTimeout(() => t.classList.add('oculto'), 3500);
}
