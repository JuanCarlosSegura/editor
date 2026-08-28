const NIVELES = ["PRINCIPAL", "SECUNDARIO", "TERCIARIO", "DESCRIPCION"];
const FORMATOS = new Set(["txt", "docx", "pdf"]);
let listadoDocumento = [];

const elementos = {
    fileInput: document.getElementById("fileInput"),
    tipo: document.getElementById("tipo"),
    texto: document.getElementById("texto"),
    preview: document.getElementById("preview"),
    importar: document.getElementById("importarArchivo"),
    agregar: document.getElementById("agregarAlListado"),
    limpiar: document.getElementById("limpiarTodo"),
    exportar: document.querySelectorAll("[data-formato]")
};

function agregarAlListado() {
    const texto = elementos.texto.value.trim();
    if (!texto) {
        alert("Por favor, escribe un texto.");
        return;
    }

    listadoDocumento.push({ tipo: elementos.tipo.value, texto });
    elementos.texto.value = "";
    actualizarVistaPrevia();
}

function actualizarVistaPrevia() {
    elementos.preview.replaceChildren();
    elementos.exportar.forEach(boton => {
        boton.disabled = listadoDocumento.length === 0;
    });

    listadoDocumento.forEach((item, index) => {
        const li = document.createElement("li");
        li.className = `item-doc item-${item.tipo}`;

        const contentDiv = document.createElement("div");
        contentDiv.className = "item-content";
        const input = document.createElement("input");
        input.value = item.texto;
        input.addEventListener("input", event => {
            listadoDocumento[index].texto = event.target.value;
        });
        contentDiv.appendChild(input);

        const actionsDiv = document.createElement("div");
        actionsDiv.className = "actions-cell";
        [
            ["◀", "Subir nivel jerárquico", () => cambiarNivel(index, -1)],
            ["▶", "Bajar nivel jerárquico", () => cambiarNivel(index, 1)],
            ["▲", "Mover arriba", () => mover(index, -1)],
            ["▼", "Mover abajo", () => mover(index, 1)],
            ["✕", "Eliminar", () => eliminar(index)]
        ].forEach(([label, title, action]) => {
            const button = document.createElement("button");
            button.type = "button";
            button.textContent = label;
            button.title = title;
            button.addEventListener("click", action);
            if (title === "Eliminar") button.className = "delete";
            actionsDiv.appendChild(button);
        });

        li.append(contentDiv, actionsDiv);
        elementos.preview.appendChild(li);
    });
}

function cambiarNivel(index, direccion) {
    const itemActual = listadoDocumento[index];
    const nivelActual = NIVELES.indexOf(itemActual.tipo);
    const nuevoNivel = nivelActual + direccion;

    if (nuevoNivel < 0 || nuevoNivel >= NIVELES.length) return;

    if (index > 0 && direccion === 1) {
        const nivelAnterior = NIVELES.indexOf(listadoDocumento[index - 1].tipo);
        if (nuevoNivel > nivelAnterior + 1) {
            alert("No puedes demover este punto más allá del nivel de su elemento superior inmediato.");
            return;
        }
    }

    itemActual.tipo = NIVELES[nuevoNivel];
    actualizarVistaPrevia();
}

function eliminar(index) {
    listadoDocumento.splice(index, 1);
    actualizarVistaPrevia();
}

function mover(index, direccion) {
    const nuevoIndex = index + direccion;
    if (nuevoIndex < 0 || nuevoIndex >= listadoDocumento.length) return;

    const [elemento] = listadoDocumento.splice(index, 1);
    listadoDocumento.splice(nuevoIndex, 0, elemento);
    actualizarVistaPrevia();
}

function limpiarTodo() {
    if (confirm("¿Vaciar la estructura actual?")) {
        listadoDocumento = [];
        actualizarVistaPrevia();
    }
}

async function importarArchivo() {
    const file = elementos.fileInput.files[0];
    if (!file) {
        alert("Selecciona un archivo.");
        return;
    }

    const extension = file.name.split(".").pop().toLowerCase();
    if (!["txt", "docx"].includes(extension)) {
        alert("Formato no soportado (.txt o .docx)");
        return;
    }

    const formData = new FormData();
    formData.append("file", file);
    elementos.importar.disabled = true;

    try {
        const response = await fetch(`/api/documentos/importar/${extension}`, {
            method: "POST",
            body: formData
        });
        if (!response.ok) throw new Error("Error al procesar el archivo.");

        listadoDocumento = await response.json();
        elementos.fileInput.value = "";
        actualizarVistaPrevia();
    } catch (error) {
        alert(`${error.message} Revisa su contenido.`);
    } finally {
        elementos.importar.disabled = false;
    }
}

async function exportar(formato) {
    if (!FORMATOS.has(formato) || listadoDocumento.length === 0) {
        alert("Agrega elementos antes de exportar.");
        return;
    }

    try {
        const response = await fetch(`/api/documentos/exportar/${formato}`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(listadoDocumento)
        });
        if (!response.ok) throw new Error("No se pudo exportar el documento.");

        const blob = await response.blob();
        const url = URL.createObjectURL(blob);
        const link = document.createElement("a");
        link.href = url;
        link.download = `documento_generado.${formato}`;
        link.click();
        URL.revokeObjectURL(url);
    } catch (error) {
        alert(error.message);
    }
}

elementos.importar.addEventListener("click", importarArchivo);
elementos.agregar.addEventListener("click", agregarAlListado);
elementos.limpiar.addEventListener("click", limpiarTodo);
elementos.exportar.forEach(boton => {
    boton.addEventListener("click", () => exportar(boton.dataset.formato));
});
actualizarVistaPrevia();
