package com.editordocument.editor;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import com.editordocument.editor.model.ConfiguracionDocumento;
import com.editordocument.editor.model.ElementoDocumento;
import com.editordocument.editor.service.DocumentoExportService;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@SpringBootTest
class EditorApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void aplicaPuntosSoloALosHijosDelNivelConfigurado() {
		ConfiguracionDocumento configuracion = new ConfiguracionDocumento();
		configuracion.setFormatosPorNivel(Map.of("PRINCIPAL", "PUNTOS"));
		List<ElementoDocumento> elementos = List.of(
				new ElementoDocumento("PRINCIPAL", "Padre"),
				new ElementoDocumento("SECUNDARIO", "Hijo"));

		String txt = new String(new DocumentoExportService().aTxt(elementos, configuracion), StandardCharsets.UTF_8);
		org.junit.jupiter.api.Assertions.assertTrue(txt.contains("1. PADRE"));
		org.junit.jupiter.api.Assertions.assertTrue(txt.contains("    • Hijo"));
	}

}
