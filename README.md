# 🇧🇷 SolidSign API - Exemplo de Assinatura CMS com Certificado em Nuvem (Batch Mode)

Este projeto demonstra a integração com a **SolidSign API** para realizar a assinatura digital CAdES (CMS) de múltiplos arquivos em lote, utilizando um certificado armazenado em HSM/Nuvem (PSC). A chave privada permanece no HSM do provedor e nunca é transmitida.

## Estrutura do Projeto

* **Controller:** Atua como gatilho para escanear a pasta de entrada local e gerenciar o processo de assinatura CAdES com certificado em nuvem.
* **Service:** Orquestra a chamada para a API SolidSign, trata erros 400/500 e salva o arquivo ZIP com os documentos assinados no armazenamento local.

## Configuração (application.properties)

| Atributo | Descrição | Exemplo / Valor |
| :--- | :--- | :--- |
| `solidsign.api.base-url` | URL base da SolidSign API (sem o caminho). | `https://solidsign.com.br` |
| `solidsign.api.authorization` | Token JWT de autorização (Bearer). | `Bearer eyJhbGciOiJIUzI1...` |
| `solidsign.batch.input-path` | Pasta local com os arquivos a serem assinados (modo lote). | `C:/Users/User/Desktop/input_files` |
| `solidsign.batch.output-path` | Pasta onde o ZIP com os arquivos assinados será salvo. | `C:/Users/User/Desktop/signed_results` |
| `solidsign.cloud.credentials` | JSON com as credenciais do HSM em nuvem: `uuidCert`, `hsmToken` e `hsmServiceUrl`. | `{"uuidCert":"...","hsmToken":"...","hsmServiceUrl":"https://hsm.provedor.com"}` |
| `solidsign.sig.hashAlgorithm` | Algoritmo de hash criptográfico (SHA256, SHA384, SHA512). | `SHA256` |
| `solidsign.sig.profile` | Perfil da assinatura CAdES (ICP-Brasil/ETSI). | `ADRB`, `ADRT`, `CADES_B`, `CADES_T`, `CADES_LT`, `CADES_LTA` |
| `solidsign.sig.signaturePackaging` | Modo de empacotamento da assinatura. | `ENVELOPING`, `ENVELOPED` ou `DETACHED` |
| `solidsign.sig.policyVersion` *(opcional)* | OID da política de assinatura ICP-Brasil. | `2.16.76.1.7.1.1.2.4` |

## Stack
1. Java 17
2. SpringBoot 3.4.x+
3. Maven 3.x.x+
4. Logback (para logging dos erros)

## Como Executar

1. **Obter credenciais:** Solicite ao seu provedor HSM as credenciais `uuidCert`, `hsmToken` e `hsmServiceUrl`.
2. **Configurar:** Preencha `solidsign.cloud.credentials` com o JSON das credenciais e os demais parâmetros em `src/main/resources/application.properties`.
3. **Compilar:** `mvn clean install`
4. **Iniciar:** `mvn spring-boot:run`
5. **Testar:** Envie um POST para `http://localhost:8080/api/cms/sign-cloud`. O sistema processará automaticamente todos os arquivos encontrados na pasta de entrada.

## Tratamento de Erros
O sistema intercepta erros **400 Bad Request** e loga o JSON detalhado da SolidSign para facilitar o debug de credenciais ou parâmetros inválidos.

---

# 🇬🇧 SolidSign API - CMS Cloud Certificate Signature Example (Batch Mode)

This project demonstrates the integration with the **SolidSign API** to perform CAdES (CMS) digital signatures on multiple files in batch mode, using a certificate stored in an HSM/Cloud (PSC). The private key stays in the HSM provider and is never transmitted.

## Project Structure

* **Controller:** Acts as a trigger to scan the local input folder and manage the CAdES cloud signing process.
* **Service:** Orchestrates the SolidSign API calls, handles 400/500 errors, and saves the ZIP file with signed documents to local storage.

## Configuration (application.properties)

| Attribute | Description | Example / Value |
| :--- | :--- | :--- |
| `solidsign.api.base-url` | Base URL of the SolidSign API (without path). | `https://solidsign.com.br` |
| `solidsign.api.authorization` | Authorization JWT Token (Bearer). | `Bearer eyJhbGciOiJIUzI1...` |
| `solidsign.batch.input-path` | Local folder containing the files to be signed (batch mode). | `C:/Users/User/Desktop/input_files` |
| `solidsign.batch.output-path` | Local folder where the signed ZIP file will be saved. | `C:/Users/User/Desktop/signed_results` |
| `solidsign.cloud.credentials` | JSON with HSM cloud credentials: `uuidCert`, `hsmToken` and `hsmServiceUrl`. | `{"uuidCert":"...","hsmToken":"...","hsmServiceUrl":"https://hsm.provider.com"}` |
| `solidsign.sig.hashAlgorithm` | Cryptographic hash algorithm (SHA256, SHA384, SHA512). | `SHA256` |
| `solidsign.sig.profile` | CAdES signature profile (ICP-Brasil/ETSI). | `ADRB`, `ADRT`, `CADES_B`, `CADES_T`, `CADES_LT`, `CADES_LTA` |
| `solidsign.sig.signaturePackaging` | Signature packaging mode. | `ENVELOPING`, `ENVELOPED` or `DETACHED` |
| `solidsign.sig.policyVersion` *(optional)* | ICP-Brasil signature policy OID. | `2.16.76.1.7.1.1.2.4` |

## Stack
1. Java 17
2. SpringBoot 3.4.x+
3. Maven 3.x.x+
4. Logback (for error logging)

## How to Run

1. **Get credentials:** Request `uuidCert`, `hsmToken` and `hsmServiceUrl` from your HSM provider.
2. **Configure:** Fill `solidsign.cloud.credentials` with the credentials JSON and set the remaining parameters in `src/main/resources/application.properties`.
3. **Build:** `mvn clean install`
4. **Start:** `mvn spring-boot:run`
5. **Test:** Send a POST request to `http://localhost:8080/api/cms/sign-cloud`. The application will automatically process all files found in the input folder.

## Error Handling
The system intercepts **400 Bad Request** errors and logs the detailed JSON response from SolidSign to assist in debugging invalid credentials or parameters.

---

# 🇪🇸 SolidSign API - Ejemplo de Firma CMS con Certificado en la Nube (Modo Batch)

Este proyecto demuestra la integración con la **SolidSign API** para realizar la firma digital CAdES (CMS) de múltiples archivos en lote, usando un certificado almacenado en HSM/Nube (PSC). La clave privada permanece en el HSM del proveedor y nunca se transmite.

## Estructura del Proyecto

* **Controller:** Actúa como disparador para escanear la carpeta local de entrada y gestionar el proceso de firma CAdES con certificado en la nube.
* **Service:** Orquestra las llamadas a la API SolidSign, gestiona errores 400/500 y guarda el archivo ZIP con los documentos firmados en el almacenamiento local.

## Configuración (application.properties)

| Atributo | Descripción | Ejemplo / Valor |
| :--- | :--- | :--- |
| `solidsign.api.base-url` | URL base de la SolidSign API (sin la ruta). | `https://solidsign.com.br` |
| `solidsign.api.authorization` | Token JWT de autorización (Bearer). | `Bearer eyJhbGciOiJIUzI1...` |
| `solidsign.batch.input-path` | Carpeta local con los archivos a firmar (modo lote). | `C:/Users/User/Desktop/input_files` |
| `solidsign.batch.output-path` | Carpeta donde se guardará el ZIP con los archivos firmados. | `C:/Users/User/Desktop/signed_results` |
| `solidsign.cloud.credentials` | JSON con las credenciales del HSM en la nube: `uuidCert`, `hsmToken` y `hsmServiceUrl`. | `{"uuidCert":"...","hsmToken":"...","hsmServiceUrl":"https://hsm.proveedor.com"}` |
| `solidsign.sig.hashAlgorithm` | Algoritmo de hash criptográfico (SHA256, SHA384, SHA512). | `SHA256` |
| `solidsign.sig.profile` | Perfil de firma CAdES (ICP-Brasil/ETSI). | `ADRB`, `ADRT`, `CADES_B`, `CADES_T`, `CADES_LT`, `CADES_LTA` |
| `solidsign.sig.signaturePackaging` | Modo de empaquetado de la firma. | `ENVELOPING`, `ENVELOPED` o `DETACHED` |
| `solidsign.sig.policyVersion` *(opcional)* | OID de la política de firma ICP-Brasil. | `2.16.76.1.7.1.1.2.4` |

## Stack
1. Java 17
2. SpringBoot 3.4.x+
3. Maven 3.x.x+
4. Logback (para el registro de errores)

## Cómo Ejecutar

1. **Obtener credenciales:** Solicite al proveedor HSM las credenciales `uuidCert`, `hsmToken` y `hsmServiceUrl`.
2. **Configurar:** Complete `solidsign.cloud.credentials` con el JSON de credenciales y configure los demás parámetros en `src/main/resources/application.properties`.
3. **Compilar:** `mvn clean install`
4. **Iniciar:** `mvn spring-boot:run`
5. **Probar:** Envíe una solicitud POST a `http://localhost:8080/api/cms/sign-cloud`. La aplicación procesará automáticamente todos los archivos encontrados en la carpeta de entrada.

## Gestión de Errores
El sistema intercepta errores **400 Bad Request** y registra el JSON detallado de SolidSign para facilitar la depuración de credenciales o parámetros inválidos.
