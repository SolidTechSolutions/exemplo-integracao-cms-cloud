package com.solidsign.examples.control;

import com.solidsign.examples.service.CmsCloudService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

/**
 * [EN]    REST controller that triggers CAdES (CMS) signing using cloud HSM credentials.
 *         Scans an input folder and signs all files found.
 *
 * [PT-BR] Controller REST que dispara a assinatura CAdES (CMS) usando credenciais de HSM em nuvem.
 *         Varre uma pasta de entrada e assina todos os arquivos encontrados.
 *
 * [ES]    Controller REST que activa la firma CAdES (CMS) usando credenciales de HSM en la nube.
 *         Escanea una carpeta de entrada y firma todos los archivos encontrados.
 */
@RestController
@RequestMapping("/api/cms")
public class CmsCloudController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CmsCloudController.class);

    @Autowired
    private CmsCloudService service;

    // [EN]    Path to the folder containing files to sign
    // [PT-BR] Caminho para a pasta contendo os arquivos a assinar
    // [ES]    Ruta a la carpeta que contiene los archivos a firmar
    @Value("${solidsign.batch.input-path}")
    private String inputPath;

    // [EN]    Path to the folder where the signed ZIP will be written
    // [PT-BR] Caminho para a pasta onde o ZIP assinado será gravado
    // [ES]    Ruta a la carpeta donde se escribirá el ZIP firmado
    @Value("${solidsign.batch.output-path}")
    private String outputPath;

    /**
     * [EN]    Signs all files inside the configured input folder using CAdES Cloud HSM.
     *         Returns the path to the output ZIP on success.
     *
     * [PT-BR] Assina todos os arquivos da pasta de entrada configurada usando CAdES Cloud HSM.
     *         Retorna o caminho do ZIP de saída em caso de sucesso.
     *
     * [ES]    Firma todos los archivos de la carpeta de entrada configurada con CAdES Cloud HSM.
     *         Devuelve la ruta del ZIP de salida en caso de éxito.
     */
    @PostMapping("/sign-cloud")
    public ResponseEntity<String> signFolder() throws IOException {
        File folder = new File(inputPath);
        if (!folder.exists() || !folder.isDirectory()) {
            // [EN]    Configured input path is invalid or not a directory
            // [PT-BR] O caminho de entrada configurado é inválido ou não é um diretório
            // [ES]    La ruta de entrada configurada es inválida o no es un directorio
            return ResponseEntity.badRequest().body("Invalid path: " + inputPath);
        }
        File[] files = folder.listFiles(File::isFile);
        if (files == null || files.length == 0) {
            // [EN]    No files found in the input folder
            // [PT-BR] Nenhum arquivo encontrado na pasta de entrada
            // [ES]    No se encontraron archivos en la carpeta de entrada
            return ResponseEntity.ok("No files found.");
        }
        LOGGER.info("Found {} file(s) for CAdES Cloud signing.", files.length);
        String result = service.signWithCloud(Arrays.asList(files), outputPath);
        return result != null
                ? ResponseEntity.ok("ZIP at: " + result)
                : ResponseEntity.internalServerError().body("Failed. Check logs.");
    }

    /**
     * [EN]    Form signing endpoint for React front-end integration — CAdES cloud HSM.
     *         All parameters from multipart/form-data; application.properties ignored.
     * [PT-BR] Endpoint de formulário para integração React — CAdES HSM em nuvem.
     *         Todos os parâmetros via multipart/form-data; application.properties ignorado.
     * [ES]    Endpoint de formulario para integración React — CAdES HSM en nube.
     *         Todos los parámetros vía multipart/form-data; application.properties ignorado.
     */
    @CrossOrigin
    @PostMapping("/sign/form")
    public ResponseEntity<byte[]> signForm(
            @RequestPart("document")                                      MultipartFile[] documents,
            @RequestPart("authorization")                                 String authorization,
            @RequestPart("baseUrl")                                       String baseUrl,
            @RequestPart("cloudCredentials")                              String cloudCredentials,
            @RequestPart(value = "profile",           required = false)   String profile,
            @RequestPart(value = "hashAlgorithm",     required = false)   String hashAlgorithm,
            @RequestPart(value = "signaturePackaging", required = false)  String signaturePackaging,
            @RequestPart(value = "policyVersion",     required = false)   String policyVersion
    ) throws IOException {
        List<File> tmpFiles = new ArrayList<>();
        java.nio.file.Path tmpDir = java.nio.file.Files.createTempDirectory("solidsign-form-");
        try {
            for (MultipartFile mf : documents) {
                java.nio.file.Path p = tmpDir.resolve(
                        mf.getOriginalFilename() != null ? mf.getOriginalFilename() : "doc");
                mf.transferTo(p);
                tmpFiles.add(p.toFile());
            }
            byte[] zip = service.signWithCloudForm(authorization, baseUrl, cloudCredentials,
                    profile, hashAlgorithm, signaturePackaging, policyVersion, tmpFiles);
            if (zip != null)
                return ResponseEntity.ok()
                        .contentType(org.springframework.http.MediaType.parseMediaType("application/zip"))
                        .header("Content-Disposition", "attachment; filename=\"signed.zip\"")
                        .body(zip);
            return ResponseEntity.internalServerError().build();
        } finally {
            tmpFiles.forEach(java.io.File::delete);
            tmpDir.toFile().delete();
        }
    }
}
