import express, { json } from 'express';
import pkg from 'jsonwebtoken';
import crypto from 'crypto'; // Importa crypto usando la sintaxis ES6
const { sign, verify } = pkg; // Importar también 'verify' para validar tokens
const app = express();
const port = 3000;

// Middleware para parsear JSON
app.use(json());

// Datos de usuario (para pruebas)
const USER_CREDENTIALS = {
    user: "jesus",
    pwd: "123456" // Cambia esto a la contraseña deseada
};

// Clave secreta para firmar y validar el token
const SECRET_KEY = "mi_clave_secreta"; // Cambia esto a una clave segura

// Endpoint de autenticación
app.post('/auth', (req, res) => {
    // Imprimir headers y body
    console.log('Headers:', req.headers);
    console.log('Body:', req.body);

    // Obtener headers
    const contentType = req.headers['content-type'];

    // Validar el Content-Type
    if (contentType !== 'application/json') {
        return res.status(400).json({
            status: "ERROR",
            error: "Bad Request",
            message: "Invalid Content-Type"
        });
    }

    // Obtener datos del body
    const { user, pwd } = req.body;

    // Verificar las credenciales
    if (user === USER_CREDENTIALS.user && pwd === USER_CREDENTIALS.pwd) {
        // Generar token con 'sign'
        const token = sign({ sub: user }, SECRET_KEY, { expiresIn: '1h' });

        // Estructura de respuesta exitosa
        const responseBody = {
            status: "OK",
            body: {
                user: USER_CREDENTIALS.user,
                token: token,
                rol: {
                    nombre: "Usuario",
                    codigo: "ROLE_USER",
                    descripcion: null,
                    rolSuperior: null,
                    nivel: null,
                    activo: null,
                    permisos: null
                },
                roles: [
                    "ROLE_USER"
                ],
                tokenType: "Bearer"
            }
        };
        return res.json(responseBody);
    } else {
        // Respuesta de error
        return res.status(401).json({
            status: "ERROR",
            error: "Unauthorized",
            message: "Usuario no valido"
        });
    }
});

// Función para validar el token
function validarToken(token) {
    try {
        // Verificar el token usando la misma SECRET_KEY
        return verify(token, SECRET_KEY);
    } catch (error) {
        return null; // Token inválido o expirado
    }
}

// Función para generar el sello recibido
function generarSello(documentoJson) {
    // Generar un hash SHA-256 basado en el código de generación del documento
    return crypto.createHash('sha256').update(documentoJson.identificacion.codigoGeneracion).digest('hex');
}

// Endpoint para procesar el documento
app.post('/recepciondte', (req, res) => {
    const { ambiente, idenvio, version, tipoDTE, documento, codigoGeneracion } = req.body;
    const headers = req.headers;
    console.log('Headers:', req.headers);
    console.log('Body:', req.body);

    // Obtener el token del header Authorization
    const authHeader = headers.authorization;
    if (!authHeader || !authHeader.startsWith("Bearer ")) {
        return res.status(401).json({
            version: 2,
            ambiente: ambiente || "00",  // Valor por defecto si no se recibe
            versionApp: 2,
            estado: 'RECHAZADO',
            codigoGeneracion: codigoGeneracion || null,
            selloRecibido: null,
            fhProcesamiento: null,
            clasificaMsg: null,
            codigoMsg: 'ERROR_CODIGO',
            descripcionMsg: 'Token no presente en el header Authorization',
            observaciones: null
        });
    }

    // Extraer el token
    const token = authHeader.split(' ')[1];

    // Validar el token
    const decoded = validarToken(token);
    if (!decoded) {
        return res.status(401).json({
            version: 2,
            ambiente: ambiente || "00",  // Valor por defecto si no se recibe
            versionApp: 2,
            estado: 'RECHAZADO',
            codigoGeneracion: codigoGeneracion || null,
            selloRecibido: null,
            fhProcesamiento: null,
            clasificaMsg: null,
            codigoMsg: 'ERROR_CODIGO',
            descripcionMsg: 'Token inválido o expirado',
            observaciones: null
        });
    }

    // Parsear el documento recibido
    let documentoJson;
    try {
        documentoJson = JSON.parse(documento);
    } catch (e) {
        return res.status(400).json({
            version: 2,
            ambiente: ambiente || "00",
            versionApp: 2,
            estado: 'RECHAZADO',
            codigoGeneracion: null,
            selloRecibido: null,
            fhProcesamiento: null,
            clasificaMsg: null,
            codigoMsg: 'ERROR_CODIGO',
            descripcionMsg: 'Documento malformado',
            observaciones: null
        });
    }

    // Validar que los campos coincidan entre la solicitud y el documento
    if (documentoJson.identificacion.version !== version || 
        documentoJson.identificacion.codigoGeneracion !== codigoGeneracion || 
        documentoJson.identificacion.ambiente !== ambiente ||
        documentoJson.identificacion.tipoDte !== tipoDTE) {
        return res.status(400).json({
            version: 2,
            ambiente: ambiente || "00",
            versionApp: 2,
            estado: 'RECHAZADO',
            codigoGeneracion: codigoGeneracion || documentoJson.identificacion.codigoGeneracion || null,
            selloRecibido: null,
            fhProcesamiento: null,
            clasificaMsg: null,
            codigoMsg: 'ERROR_CODIGO',
            descripcionMsg: 'Parámetros no coinciden con el documento',
            observaciones: null
        });
    }

    // Generar el sello recibido
    const selloRecibido = generarSello(documentoJson);

    // Preparar respuesta exitosa
    const response = {
        version: 2,
        ambiente: ambiente,
        versionApp: 2,
        estado: 'PROCESADO',
        codigoGeneracion: codigoGeneracion,
        selloRecibido: selloRecibido,
        fhProcesamiento: new Date().toISOString(),
        clasificaMsg: '10',
        codigoMsg: '001',
        descripcionMsg: 'RECIBIDO',
        observaciones: []
    };

    res.status(200).json(response);
});

// Iniciar el servidor
app.listen(port, () => {
    console.log(`Server running at http://localhost:${port}`);
});
