/** Panel compartido por los tres roles. Los datos se conservan en MySQL, no en localStorage. */
const $ = selector => document.querySelector(selector);
// Escapar todo texto del servidor antes de ponerlo en HTML evita ejecutar etiquetas guardadas por otro usuario.
const escapar = valor => String(valor ?? '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
const dinero = valor => new Intl.NumberFormat('es-PE', { style:'currency', currency:'PEN' }).format(valor);
const nombresEstado = { PENDIENTE:'Pendiente', CONFIRMADA:'Confirmada', ATENDIDA:'Atendida', CANCELADA:'Cancelada' };
let sesion, seccion = 'reservas', reservas = [], servicios = [], usuarios = [], veterinarios = [];
let accionGuardar = null, accionConfirmar = null;

/** LocalDateTime no contiene zona: mostramos literalmente la hora de Lima devuelta por la API. */
function fecha(texto) {
    const [dia, hora] = texto.split('T');
    const [ano, mes, numero] = dia.split('-');
    return `${numero}/${mes}/${ano} · ${hora.slice(0,5)}`;
}
/** Comparar textos ISO de la misma zona permite saber si una cita sigue siendo futura. */
function ahoraLima() {
    const partes = new Intl.DateTimeFormat('sv-SE', { timeZone:'America/Lima', year:'numeric', month:'2-digit', day:'2-digit', hour:'2-digit', minute:'2-digit', second:'2-digit', hourCycle:'h23' }).formatToParts(new Date());
    const p = Object.fromEntries(partes.map(x => [x.type, x.value]));
    return `${p.year}-${p.month}-${p.day}T${p.hour}:${p.minute}:${p.second}`;
}
function aviso(texto, error = false) { $('#aviso').hidden = false; $('#aviso').textContent = texto; $('#aviso').className = error ? 'error' : ''; }
function fallo(error) { if (error.status === 401) window.location.assign('/login.html'); else aviso(error.message, true); }
function badge(estado) { return `<span class="badge ${estado.toLowerCase()}">${escapar(nombresEstado[estado] || estado)}</span>`; }
function boton(accion, id, texto, peligro = false) { return `<button class="btn tiny ${peligro ? 'danger' : 'ghost'}" data-accion="${accion}" data-id="${id}">${texto}</button>`; }
function vacio(titulo, detalle) { return `<div class="empty"><strong>${titulo}</strong>${detalle}</div>`; }
function tabla(cabeceras, filas) { return `<div class="table-wrap"><table><thead><tr>${cabeceras.map(c=>`<th scope="col">${c}</th>`).join('')}</tr></thead><tbody>${filas.join('')}</tbody></table></div>`; }

/** Se espera la sesión antes de mostrar controles. El servidor ya protege la página y cada endpoint. */
async function iniciar() {
    try {
        sesion = await api('/api/auth/sesion');
        $('#nombreSesion').textContent = sesion.nombre;
        $('#fechaActual').textContent = new Intl.DateTimeFormat('es-PE', {dateStyle:'long',timeZone:'America/Lima'}).format(new Date());
        if (sesion.rol === 'VETERINARIO') { $('#titulo').textContent = 'Cada cita, una oportunidad de cuidar.'; $('#subtitulo').textContent = 'Revisa tu agenda y actualiza el estado de las visitas asignadas.'; }
        const items = [['reservas','◷','Citas'],['servicios','✚','Servicios'],...(sesion.rol === 'ADMIN' ? [['usuarios','♙','Usuarios']] : []),['perfil','○','Mi perfil']];
        $('#navegacion').innerHTML = items.map(([id, icono, titulo])=>`<button class="nav-btn" data-seccion="${id}"><span aria-hidden="true">${icono}</span>${titulo}</button>`).join('');
        await cargar();
    } catch (error) { fallo(error); }
}

/** Las lecturas independientes se piden juntas; no se consulta la lista privada de usuarios salvo como ADMIN. */
async function cargar() {
    const resultados = await Promise.all([api('/api/reservas'), api('/api/servicios'), api('/api/veterinarios'), sesion.rol === 'ADMIN' ? api('/api/usuarios') : Promise.resolve([])]);
    [reservas, servicios, veterinarios, usuarios] = resultados;
    const proximas = reservas.filter(r => ['PENDIENTE','CONFIRMADA'].includes(r.estado) && r.fechaHora > ahoraLima());
    $('#resumen').innerHTML = [[proximas.length,'Próximas citas'],[reservas.filter(r=>r.estado==='PENDIENTE').length,'Por confirmar'],[reservas.filter(r=>r.estado==='ATENDIDA').length,'Visitas atendidas']].map(([n,t])=>`<article class="stat"><small>${t}</small><strong>${n.toString().padStart(2,'0')}</strong></article>`).join('');
    renderizar();
}

/** Cambia la sección visible y sus filtros sin recargar el documento completo. */
function renderizar() {
    document.querySelectorAll('[data-seccion]').forEach(b => { b.classList.toggle('active', b.dataset.seccion === seccion); b.setAttribute('aria-current', b.dataset.seccion === seccion ? 'page' : 'false'); });
    const titulos = { reservas:'Agenda de citas', servicios:'Servicios veterinarios', usuarios:'Usuarios de la clínica', perfil:'Mi perfil' };
    const ayudas = { reservas:'Consulta, organiza y da seguimiento a cada reserva.', servicios:'Cuidados disponibles para cada etapa de su vida.', usuarios:'Gestiona cuentas, roles y acceso a la clínica.', perfil:'Mantén tus datos de contacto actualizados.' };
    $('#seccionTitulo').textContent = titulos[seccion]; $('#seccionAyuda').textContent = ayudas[seccion];
    const puedeCrear = (seccion === 'reservas' && sesion.rol !== 'VETERINARIO') || (sesion.rol === 'ADMIN' && ['servicios','usuarios'].includes(seccion));
    $('#nuevo').hidden = !puedeCrear;
    $('#nuevo').textContent = ({ reservas:'+ Nueva cita', servicios:'+ Nuevo servicio', usuarios:'+ Nuevo usuario' })[seccion] || '';
    if (seccion === 'reservas') {
        $('#filtros').innerHTML = `<label>Estado <select id="filtroEstado"><option value="">Todos los estados</option>${Object.entries(nombresEstado).map(([v,t])=>`<option value="${v}">${t}</option>`).join('')}</select></label><span class="muted">Hora de Lima</span>`;
        $('#filtroEstado').addEventListener('change', mostrarReservas); mostrarReservas();
    } else if (seccion === 'servicios') {
        $('#filtros').innerHTML = '<label for="buscarServicio">Buscar</label><input id="buscarServicio" placeholder="Nombre del servicio" maxlength="100"><button id="buscar" class="btn ghost">Buscar</button>';
        $('#buscar').addEventListener('click', buscarServicios); $('#buscarServicio').addEventListener('keydown', e=>{if(e.key==='Enter') buscarServicios();});
        mostrarServicios(servicios);
    } else if (seccion === 'usuarios') {
        $('#filtros').innerHTML = '';
        $('#contenido').innerHTML = tabla(['Usuario','Contacto','Rol','Estado','Acciones'], usuarios.map(u=>`<tr><td><strong>${escapar(u.nombre)}</strong><small>#${u.id}</small></td><td>${escapar(u.correo)}<small>${escapar(u.telefono)}</small></td><td>${escapar(u.rol)}</td><td>${badge(u.activo?'ACTIVO':'INACTIVO')}</td><td><div class="actions">${boton('editar-usuario',u.id,'Editar')}${u.id!==sesion.id?boton('eliminar-usuario',u.id,'Eliminar',true):''}</div></td></tr>`));
    } else {
        $('#filtros').innerHTML = '';
        $('#contenido').innerHTML = `<div class="perfil"><p><strong>${escapar(sesion.nombre)}</strong><br>${escapar(sesion.correo)}<br>${escapar(sesion.telefono)}</p><p class="muted">Rol: ${escapar(sesion.rol)}</p><br><button data-accion="editar-perfil" class="btn primary">Editar mis datos</button></div>`;
    }
}

/** La lista contiene solo registros autorizados por el backend; este filtro es una ayuda visual. */
function mostrarReservas() {
    const estado = $('#filtroEstado').value;
    const lista = reservas.filter(r=>!estado || r.estado===estado);
    if (!lista.length) { $('#contenido').innerHTML = vacio('La agenda está despejada', 'Las citas registradas aparecerán aquí.'); return; }
    $('#contenido').innerHTML = tabla(['Mascota y servicio','Fecha y profesional',sesion.rol==='CLIENTE'?'Importe':'Cliente','Estado','Acciones'], lista.map(r=>{
        const abierta = ['PENDIENTE','CONFIRMADA'].includes(r.estado);
        const futura = r.fechaHora > ahoraLima();
        const staff = sesion.rol !== 'CLIENTE';
        let acciones = '';
        if (abierta && futura && sesion.rol !== 'VETERINARIO') acciones += boton('editar-reserva',r.id,'Editar');
        if (staff && r.estado==='PENDIENTE') acciones += boton('confirmar',r.id,'Confirmar');
        if (staff && r.estado==='CONFIRMADA' && !futura) acciones += boton('atender',r.id,'Atendida');
        if (abierta && (staff || futura)) acciones += boton('cancelar',r.id,'Cancelar');
        if (sesion.rol==='ADMIN' || (sesion.rol==='CLIENTE' && r.estado==='CANCELADA')) acciones += boton('eliminar-reserva',r.id,'Eliminar',true);
        return `<tr><td><strong>${escapar(r.mascota)}</strong><small>${escapar(r.servicio)} · ${escapar(r.motivo)}</small></td><td>${fecha(r.fechaHora)}<small>${escapar(r.veterinario)}</small></td><td>${sesion.rol==='CLIENTE'?dinero(r.precio):escapar(r.cliente)}</td><td>${badge(r.estado)}</td><td><div class="actions">${acciones || '<span class="muted">Sin acciones</span>'}</div></td></tr>`;
    }));
}
function mostrarServicios(lista) {
    if (!lista.length) { $('#contenido').innerHTML = vacio('Sin servicios para mostrar','Prueba otra búsqueda o registra un servicio.'); return; }
    $('#contenido').innerHTML = `<div class="catalogo">${lista.map(s=>`<article class="service-card"><span class="service-icon" aria-hidden="true">✚</span><h3>${escapar(s.nombre)}</h3><p>${escapar(s.descripcion)}</p><div class="service-meta"><strong>${dinero(s.precio)}</strong><span>${s.duracionMinutos} min</span></div><br>${sesion.rol==='ADMIN'?`${badge(s.activo?'ACTIVO':'INACTIVO')}<br><br><div class="actions">${boton('editar-servicio',s.id,'Editar')}${boton('eliminar-servicio',s.id,'Eliminar',true)}</div>`:sesion.rol==='CLIENTE'?boton('reservar-servicio',s.id,'Agendar una cita'):''}</article>`).join('')}</div>`;
}
/** Esta búsqueda llama al endpoint JPQL; no simula una búsqueda de base de datos en JavaScript. */
async function buscarServicios() { try { mostrarServicios(await api('/api/servicios?nombre='+encodeURIComponent($('#buscarServicio').value.trim()))); } catch (error) { fallo(error); } }

/** Helpers de formularios: todos los campos tienen una etiqueta y límites equivalentes al DTO Java. */
function input(nombre, titulo, valor='', tipo='text', extras='') { return `<label>${titulo}<input name="${nombre}" type="${tipo}" value="${escapar(valor)}" ${extras}></label>`; }
function select(nombre, titulo, lista, seleccionado='') { return `<label>${titulo}<select name="${nombre}" required>${lista.map(([v,t])=>`<option value="${escapar(v)}" ${String(v)===String(seleccionado)?'selected':''}>${escapar(t)}</option>`).join('')}</select></label>`; }
function abrirEditor(titulo, campos, guardar) {
    $('#editorTitulo').textContent = titulo; $('#campos').innerHTML = campos; $('#errorEditor').textContent = '';
    accionGuardar = guardar; $('#editor').showModal();
}
function editarServicio(s) {
    abrirEditor(s?'Editar servicio':'Nuevo servicio',
        input('nombre','Nombre',s?.nombre,'text','required maxlength="100"') + input('precio','Precio (S/)',s?.precio??0,'number','required min="0" max="99999999.99" step="0.01"') +
        `<label class="wide">Descripción<textarea name="descripcion" required maxlength="500" rows="3">${escapar(s?.descripcion)}</textarea></label>` +
        input('duracionMinutos','Duración (minutos)',s?.duracionMinutos??30,'number','required min="15" max="240"') +
        select('activo','Disponibilidad',[['true','Activo'],['false','Inactivo']],s?.activo??true),
        datos=>api('/api/servicios'+(s?'/'+s.id:''), {method:s?'PUT':'POST',body:JSON.stringify({...datos,precio:Number(datos.precio),duracionMinutos:Number(datos.duracionMinutos),activo:datos.activo==='true'})}));
}
function editarUsuario(u) {
    abrirEditor(u?'Editar usuario':'Nuevo usuario',
        input('nombre','Nombre completo',u?.nombre,'text','required maxlength="120"') + input('correo','Correo',u?.correo,'email','required maxlength="150"') +
        input('telefono','Teléfono',u?.telefono,'tel','required maxlength="20"') + input('password',u?'Nueva contraseña (opcional)':'Contraseña','','password',`${u?'':'required'} minlength="8" maxlength="72" autocomplete="new-password"`) +
        select('rol','Rol',[['CLIENTE','Cliente'],['VETERINARIO','Veterinario'],['ADMIN','Administrador']],u?.rol||'CLIENTE') +
        select('activo','Estado',[['true','Activo'],['false','Inactivo']],u?.activo??true),
        datos=>api('/api/usuarios'+(u?'/'+u.id:''),{method:u?'PUT':'POST',body:JSON.stringify({...datos,activo:datos.activo==='true'})}));
}
function editarPerfil() {
    abrirEditor('Mis datos de contacto', input('nombre','Nombre completo',sesion.nombre,'text','required maxlength="120"')+input('correo','Correo',sesion.correo,'email','required maxlength="150"')+input('telefono','Teléfono',sesion.telefono,'tel','required maxlength="20"'),
        async datos=>{sesion=await api('/api/usuarios/me',{method:'PUT',body:JSON.stringify(datos)});$('#nombreSesion').textContent=sesion.nombre;});
}
function editarReserva(r, servicioId) {
    const activos = servicios.filter(s=>s.activo);
    const clientes = usuarios.filter(u=>u.activo&&u.rol==='CLIENTE');
    if (!activos.length || !veterinarios.length || (sesion.rol==='ADMIN'&&!clientes.length)) {
        aviso('Para agendar se necesita al menos un servicio, un veterinario y un cliente activos.',true); return;
    }
    abrirEditor(r?'Editar cita':'Agendar una cita',
        (sesion.rol==='ADMIN'?select('usuarioId','Cliente',clientes.map(u=>[u.id,u.nombre]),r?.usuarioId):'') +
        input('mascota','Nombre de la mascota',r?.mascota,'text','required maxlength="100"') +
        select('servicioId','Servicio',activos.map(s=>[s.id,`${s.nombre} · ${dinero(s.precio)}`]),r?.servicioId||servicioId) +
        select('veterinarioId','Veterinario',veterinarios.map(v=>[v.id,v.nombre]),r?.veterinarioId) +
        input('fechaHora','Fecha y hora (Lima)',r?.fechaHora?.slice(0,16),'datetime-local',`required min="${ahoraLima().slice(0,16)}"`) +
        `<label class="wide">Motivo de la visita<textarea name="motivo" required maxlength="500" rows="3">${escapar(r?.motivo)}</textarea><small>Se comprobará que el profesional esté libre durante toda la cita.</small></label>`,
        datos=>api('/api/reservas'+(r?'/'+r.id:''),{method:r?'PUT':'POST',body:JSON.stringify({...datos,usuarioId:sesion.rol==='ADMIN'?Number(datos.usuarioId):sesion.id,servicioId:Number(datos.servicioId),veterinarioId:Number(datos.veterinarioId)})}));
}

/** La confirmación se muestra antes de borrar o cancelar para evitar clics accidentales. */
function confirmar(texto, accion) { $('#confirmarTexto').textContent=texto; accionConfirmar=accion; $('#confirmacion').showModal(); }
$('#siConfirmar').addEventListener('click',async()=>{
    $('#siConfirmar').disabled=true;
    try { await accionConfirmar(); $('#confirmacion').close(); await cargar(); aviso('Operación completada.'); }
    catch(error) { $('#confirmacion').close(); fallo(error); }
    finally { $('#siConfirmar').disabled=false; }
});
$('#noConfirmar').addEventListener('click',()=>$('#confirmacion').close());
$('#cerrarEditor').addEventListener('click',()=>$('#editor').close());
$('#cancelarEditor').addEventListener('click',()=>$('#editor').close());

/** Se desactiva Guardar mientras viaja la petición para evitar envíos duplicados. */
$('#formEditor').addEventListener('submit',async evento=>{
    evento.preventDefault(); $('#guardar').disabled=true; $('#errorEditor').textContent='';
    try { await accionGuardar(Object.fromEntries(new FormData(evento.target))); $('#editor').close(); await cargar(); aviso('Datos guardados correctamente.'); }
    catch(error) { if(error.status===401) fallo(error); else $('#errorEditor').textContent=error.message; }
    finally { $('#guardar').disabled=false; }
});
$('#navegacion').addEventListener('click',evento=>{const b=evento.target.closest('[data-seccion]');if(b){seccion=b.dataset.seccion;$('#aviso').hidden=true;renderizar();}});
$('#nuevo').addEventListener('click',()=>{if(seccion==='reservas')editarReserva();else if(seccion==='servicios')editarServicio();else editarUsuario();});
$('#salir').addEventListener('click',async()=>{try{await api('/api/auth/logout',{method:'POST'});window.location.assign('/login.html');}catch(error){fallo(error);}});

/** Delegación de eventos: un único listener atiende botones creados después de recibir datos. */
$('#contenido').addEventListener('click',async evento=>{
    const b=evento.target.closest('[data-accion]'); if(!b)return;
    const id=Number(b.dataset.id), accion=b.dataset.accion;
    try {
        if(accion==='editar-perfil')return editarPerfil();
        if(accion==='editar-usuario')return editarUsuario(usuarios.find(u=>u.id===id));
        if(accion==='editar-servicio')return editarServicio(servicios.find(s=>s.id===id));
        if(accion==='editar-reserva')return editarReserva(reservas.find(r=>r.id===id));
        if(accion==='reservar-servicio')return editarReserva(null,id);
        if(accion.startsWith('eliminar-')) {
            const recurso=({'eliminar-usuario':'usuarios','eliminar-servicio':'servicios','eliminar-reserva':'reservas'})[accion];
            return confirmar('Se eliminará este registro. Esta acción no se puede deshacer.',()=>api(`/api/${recurso}/${id}`,{method:'DELETE'}));
        }
        const estado=({confirmar:'CONFIRMADA',atender:'ATENDIDA',cancelar:'CANCELADA'})[accion];
        const cambiar=()=>api(`/api/reservas/${id}/estado`,{method:'PATCH',body:JSON.stringify({estado})});
        if(accion==='cancelar')return confirmar('¿Cancelar esta cita y liberar el horario?',cambiar);
        b.disabled=true; await cambiar(); await cargar(); aviso('Estado de la cita actualizado.');
    } catch(error) { fallo(error); } finally { b.disabled=false; }
});
iniciar();
