package com.solidsign.examples.service;

import com.solidsign.examples.response.SignResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.util.List;
import java.util.zip.*;

/**
 * [EN]    Service that signs CMS (CAdES) documents using a cloud HSM certificate.
 *         Calls the SolidSign API endpoint: POST /solidsign/dsig/cms/sign-hsm-cloud
 *
 * [PT-BR] Serviço que assina documentos CMS (CAdES) usando um certificado HSM em nuvem.
 *         Chama o endpoint da API SolidSign: POST /solidsign/dsig/cms/sign-hsm-cloud
 *
 * [ES]    Servicio que firma documentos CMS (CAdES) usando un certificado HSM en la nube.
 *         Llama al endpoint de la API SolidSign: POST /solidsign/dsig/cms/sign-hsm-cloud
 */
@Service
public class CmsCloudService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CmsCloudService.class);
    private final RestTemplate restTemplate = new RestTemplate();

    // [EN]    Base URL of the SolidSign API (e.g. https://solidsign.com.br)
    // [PT-BR] URL base da API SolidSign (ex: https://solidsign.com.br)
    // [ES]    URL base de la API SolidSign (p.ej. https://solidsign.com.br)
    @Value("${solidsign.api.base-url}")
    private String baseUrl;

    // [EN]    Authorization header value (Bearer token)
    // [PT-BR] Valor do header Authorization (token Bearer)
    // [ES]    Valor del header Authorization (token Bearer)
    @Value("${solidsign.api.authorization}")
    private String authorization;

    // [EN]    Signature profile (e.g. ADRB, ADRT, ADRC, ADRA)
    // [PT-BR] Perfil de assinatura (ex: ADRB, ADRT, ADRC, ADRA)
    // [ES]    Perfil de firma (p.ej. ADRB, ADRT, ADRC, ADRA)
    @Value("${solidsign.sig.profile}")
    private String profile;

    // [EN]    Hash algorithm (SHA256, SHA384, SHA512)
    // [PT-BR] Algoritmo de hash (SHA256, SHA384, SHA512)
    // [ES]    Algoritmo de hash (SHA256, SHA384, SHA512)
    @Value("${solidsign.sig.hashAlgorithm}")
    private String hashAlgorithm;

    // [EN]    Signature packaging (ENVELOPING, ENVELOPED, DETACHED)
    // [PT-BR] Empacotamento da assinatura (ENVELOPING, ENVELOPED, DETACHED)
    // [ES]    Empaquetado de la firma (ENVELOPING, ENVELOPED, DETACHED)
    @Value("${solidsign.sig.signaturePackaging}")
    private String signaturePackaging;

    // [EN]    Policy version (e.g. 1_3)
    // [PT-BR] Versão da política (ex: 1_3)
    // [ES]    Versión de la política (p.ej. 1_3)
    @Value("${solidsign.sig.policyVersion:}")
    private String policyVersion;

    // [EN]    Cloud HSM credentials as JSON (uuidCert, hsmToken, hsmServiceUrl)
    // [PT-BR] Credenciais do HSM em nuvem como JSON (uuidCert, hsmToken, hsmServiceUrl)
    // [ES]    Credenciales del HSM en la nube como JSON (uuidCert, hsmToken, hsmServiceUrl)
    @Value("${solidsign.cloud.credentials}")
    private String cloudCredentials;

    /**
     * [EN]    Signs the given files using a cloud HSM and returns the path of the output ZIP.
     * [PT-BR] Assina os arquivos informados usando um HSM em nuvem e retorna o caminho do ZIP de saída.
     * [ES]    Firma los archivos dados con un HSM en la nube y devuelve la ruta del ZIP de salida.
     *
     * @param files
     *   [EN]    list of files to sign
     *   [PT-BR] lista de arquivos a assinar
     *   [ES]    lista de archivos a firmar
     * @param outputDir
     *   [EN]    destination folder for the output ZIP
     *   [PT-BR] pasta de destino para o ZIP de saída
     *   [ES]    carpeta de destino para el ZIP de salida
     * @return
     *   [EN]    path of the generated ZIP, or null on error
     *   [PT-BR] caminho do ZIP gerado, ou null em caso de erro
     *   [ES]    ruta del ZIP generado, o null en caso de error
     */
    public String signWithCloud(List<File> files, String outputDir) throws IOException {
        LOGGER.info("Starting CAdES Cloud signing for {} file(s).", files.size());

        // [EN]    Build the full endpoint URL from the base URL
        // [PT-BR] Constrói a URL completa do endpoint a partir da URL base
        // [ES]    Construye la URL completa del endpoint a partir de la URL base
        String url = baseUrl + "/solidsign/dsig/cms/sign-hsm-cloud";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("Authorization", authorization);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        // [EN]    Attach each document indexed as document[0], document[1], ...
        // [PT-BR] Anexa cada documento indexado como document[0], document[1], ...
        // [ES]    Adjunta cada documento indexado como document[0], document[1], ...
        for (int i = 0; i < files.size(); i++) {
            body.add("document[" + i + "]", new FileSystemResource(files.get(i)));
        }

        body.add("cloudCredentials",   cloudCredentials);
        body.add("profile",            profile);
        body.add("hashAlgorithm",      hashAlgorithm);
        body.add("signaturePackaging", signaturePackaging);
        if (policyVersion != null && !policyVersion.isBlank()) body.add("policyVersion", policyVersion);

        try {
            ResponseEntity<SignResponse> resp = restTemplate.postForEntity(
                    url, new HttpEntity<>(body, headers), SignResponse.class);
            if (resp.getStatusCode() == HttpStatus.OK && resp.getBody() != null) {
                byte[] zip = downloadAndZip(resp.getBody(), files);
                new File(outputDir).mkdirs();
                String out = outputDir + "/signed_cms_cloud_" + System.currentTimeMillis() + ".zip";
                try (FileOutputStream fos = new FileOutputStream(out)) {
                    fos.write(zip);
                }
                LOGGER.info("CAdES Cloud signing complete. Output: {}", out);
                return out;
            }
        } catch (HttpStatusCodeException e) {
            LOGGER.error("SolidSign API error {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            LOGGER.error("Unexpected error during CAdES Cloud signing: {}", e.getMessage(), e);
        }
        return null;
    }

    /**
     * [EN]    Downloads each signed document from the SolidSign response links and packages them into a ZIP.
     * [PT-BR] Baixa cada documento assinado dos links da resposta SolidSign e os empacota em um ZIP.
     * [ES]    Descarga cada documento firmado de los enlaces de respuesta SolidSign y los empaqueta en un ZIP.
     */
    private byte[] downloadAndZip(SignResponse resp, List<File> originals) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authorization);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            for (int i = 0; i < resp.documents.size(); i++) {
                String downloadUrl = resp.documents.get(i).links.stream()
                        .filter(l -> "self".equals(l.rel))
                        .findFirst()
                        .map(l -> l.href)
                        .orElse(null);
                if (downloadUrl == null) continue;
                ResponseEntity<byte[]> r = restTemplate.exchange(
                        downloadUrl, HttpMethod.GET, entity, byte[].class);
                if (r.getStatusCode() == HttpStatus.OK) {
                    zos.putNextEntry(new ZipEntry("signed_" + originals.get(i).getName()));
                    zos.write(r.getBody());
                    zos.closeEntry();
                }
            }
        }
        return baos.toByteArray();
    }

    // ─── Form endpoint (all params from request, properties ignored) ──────────

    /**
     * [EN]    Signs documents via CAdES cloud HSM with all parameters supplied by the caller.
     * [PT-BR] Assina documentos via CAdES HSM em nuvem com todos os parâmetros fornecidos pelo chamador.
     * [ES]    Firma documentos vía CAdES HSM en la nube con todos los parámetros suministrados por el llamador.
     *
     * @return ZIP bytes with signed documents, or null on error
     */
    public byte[] signWithCloudForm(String auth, String apiBaseUrl, String cloudCredentials,
                                     String profile, String hashAlgorithm,
                                     String signaturePackaging, String policyVersion,
                                     List<File> files) throws IOException {
        LOGGER.info("CAdES Cloud form signing for {} file(s).", files.size());
        String signUrl = apiBaseUrl + "/solidsign/dsig/cms/sign-hsm-cloud";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("Authorization", auth);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        for (int i = 0; i < files.size(); i++) body.add("document[" + i + "]", new FileSystemResource(files.get(i)));
        body.add("cloudCredentials", cloudCredentials);
        if (profile != null && !profile.isBlank())                       body.add("profile",            profile);
        if (hashAlgorithm != null && !hashAlgorithm.isBlank())           body.add("hashAlgorithm",      hashAlgorithm);
        if (signaturePackaging != null && !signaturePackaging.isBlank()) body.add("signaturePackaging", signaturePackaging);
        if (policyVersion != null && !policyVersion.isBlank())           body.add("policyVersion",      policyVersion);
        try {
            ResponseEntity<SignResponse> resp = restTemplate.postForEntity(
                    signUrl, new HttpEntity<>(body, headers), SignResponse.class);
            if (resp.getStatusCode() == HttpStatus.OK && resp.getBody() != null) {
                SignResponse signResp = resp.getBody();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try (ZipOutputStream zos = new ZipOutputStream(baos)) {
                    HttpHeaders dh = new HttpHeaders();
                    dh.set("Authorization", auth);
                    HttpEntity<Void> de = new HttpEntity<>(dh);
                    for (int i = 0; i < signResp.documents.size(); i++) {
                        String dlUrl = signResp.documents.get(i).links.stream()
                                .filter(l -> "self".equals(l.rel)).findFirst()
                                .map(l -> l.href).orElse(null);
                        if (dlUrl == null) continue;
                        ResponseEntity<byte[]> r = restTemplate.exchange(
                                dlUrl, HttpMethod.GET, de, byte[].class);
                        if (r.getStatusCode() == HttpStatus.OK) {
                            zos.putNextEntry(new ZipEntry("signed_" + files.get(i).getName()));
                            zos.write(r.getBody());
                            zos.closeEntry();
                        }
                    }
                }
                return baos.toByteArray();
            }
        } catch (HttpStatusCodeException e) {
            LOGGER.error("SolidSign API error {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            LOGGER.error("Unexpected error in CAdES Cloud form signing: {}", e.getMessage(), e);
        }
        return null;
    }
}
