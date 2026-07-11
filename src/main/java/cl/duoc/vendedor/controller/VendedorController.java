package cl.duoc.vendedor.controller;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import cl.duoc.vendedor.mapper.VendedorMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import jakarta.validation.Valid;
import cl.duoc.vendedor.dto.CreateRequestVendedor;
import cl.duoc.vendedor.model.Vendedor;
import cl.duoc.vendedor.service.VendedorService;
import cl.duoc.vendedor.dto.UpdateRequestVendedor;
import cl.duoc.vendedor.dto.VendedorResponse;
import cl.duoc.vendedor.exception.ResourceNotFoundException;


// IMPLEMENTACIÓN DE OPENAPI / SWAGGER
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
// Usamos un alias para evitar colisión de nombres con el @RequestBody de Spring
 
@RestController
@RequestMapping("/api/v1/vendedores")
@Tag(name = "Vendedor Controller", description = "Endpoints para el registro de postulaciones, gestión de perfiles y publicación de catálogos")
public class VendedorController {

private final VendedorService vendedorService;
private final WebClient  webClient;

public VendedorController(VendedorService vendedorService, WebClient webClient) {
        this.vendedorService = vendedorService;
        this.webClient = webClient;
    }

//endpoint basicos///

//LIstar vendedores

@GetMapping
    @Operation(summary = "Listar vendedores", description = "Recupera una lista con todos los vendedores registrados en el sistema.")
    @ApiResponse(responseCode = "200", description = "Lista de vendedores obtenida con éxito")
public ResponseEntity<List<Vendedor>> listarVendedores() {
    List<Vendedor> vendedores = vendedorService.getVendedores();
    return ResponseEntity.ok(vendedores);
}

//registra : crear postulacion 

@PostMapping
@Operation(summary = "Registrar postulación de vendedor", description = "Crea un registro de postulación inicial para un vendedor en estado 'PENDIENTE'.")
    @ApiResponse(responseCode = "201", description = "Vendedor postulado correctamente")
    @ApiResponse(responseCode = "400", description = "Datos de postulación inválidos")
public ResponseEntity<Map<String, Object>> crearVendedor(

@io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Datos requeridos para la postulación del vendedor",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CreateRequestVendedor.class),
                examples = @ExampleObject(
                    name = "Ejemplo de Postulación",
                    value = "{\n  \"nombre\": \"Juan Pérez\",\n  \"correo\": \"juan.perez@tienda.cl\",\n  \"rutEmpresa\": \"76.123.456-K\",\n  \"telefono\": \"+56912345678\"\n}"
                )
            )
        )




        @Valid @RequestBody CreateRequestVendedor request
) {

    Vendedor nuevoVendedor =
            vendedorService.saveVendedor(
                    VendedorMapper.toVendedor(request)
            );

    Map<String, Object> response = new HashMap<>();
    response.put("mensaje", "Vendedor postulado correctamente");
    response.put("id", nuevoVendedor.getId());

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}
//buscar por id

@GetMapping("/{id}")
    @Operation(summary = "Buscar vendedor por ID", description = "Obtiene los detalles del perfil de un vendedor usando su ID único.")
    @ApiResponse(responseCode = "200", description = "Vendedor encontrado")
    @ApiResponse(responseCode = "404", description = "Vendedor no encontrado")

public ResponseEntity<Vendedor> buscarVendedor(@PathVariable int id) {

    Vendedor vendedor = vendedorService.getVendedorById(id);

    if (vendedor == null) {
        throw new ResourceNotFoundException(
                "Vendedor con id=" + id + " no encontrado"
        );
    }

    return ResponseEntity.ok(vendedor);
}
//actualizar perfil
@PutMapping("/{id}")
@Operation(summary = "Actualizar perfil del vendedor", description = "Permite modificar la información comercial o de contacto del vendedor.")
    @ApiResponse(responseCode = "200", description = "Perfil actualizado correctamente")
    @ApiResponse(responseCode = "404", description = "Vendedor no existente")
public ResponseEntity<Map<String, Object>> actualizarVendedor(
        @PathVariable int id,
@io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Campos a actualizar del vendedor",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UpdateRequestVendedor.class),
                examples = @ExampleObject(
                    name = "Ejemplo de Actualización",
                    value = "{\n  \"nombre\": \"Juan Pérez Editado\",\n  \"telefono\": \"+56987654321\"\n}"
                )
            )
        )


        @Valid @RequestBody UpdateRequestVendedor request
) {

    Vendedor actualizado =
            vendedorService.updateVendedor(id, request);

    Map<String, Object> response = new HashMap<>();
    response.put("mensaje", "Vendedor actualizado correctamente");
    response.put("id", actualizado.getId());

    return ResponseEntity.ok(response);
}
//eliminar pal vendeor

@DeleteMapping("/{id}")
@Operation(summary = "Eliminar un vendedor", description = "Remueve permanentemente el registro de un vendedor del sistema.")
    @ApiResponse(responseCode = "200", description = "Vendedor eliminado correctamente")
    @ApiResponse(responseCode = "404", description = "El ID del vendedor no existe")
public ResponseEntity<Map<String, String>> eliminarVendedor(@PathVariable int id) {

    boolean eliminado = vendedorService.deleteVendedor(id);

    if (!eliminado) {
        throw new ResourceNotFoundException(
                "Vendedor con id=" + id + " no encontrado"
        );
    }

    Map<String, String> response = new HashMap<>();
    response.put("mensaje", "Vendedor eliminado correctamente");

    return ResponseEntity.ok(response);
}



//-----------Endpoint segun historias de usuario------

//ver el estado de psotulacion 
@GetMapping("/{id}/estado")
@Operation(summary = "Ver estado del vendedor", description = "Endpoint rápido para consultar la fase actual del vendedor (PENDIENTE, APROBADO, RECHAZADO).")
    @ApiResponse(responseCode = "200", description = "Estado obtenido")
public ResponseEntity<Map<String, Object>> verEstado(@PathVariable int id) {

    Vendedor vendedor = vendedorService.getVendedorById(id);

    Map<String, Object> response = new HashMap<>();
    response.put("id", vendedor.getId());
    response.put("estado", vendedor.getEstado());

    return ResponseEntity.ok(response);
}
//cambiar estado de postulacion 
@PutMapping("/{id}/estado")
@Operation(summary = "Cambiar estado de forma manual", description = "Permite modificar manualmente el estado operativo de un vendedor.")
 @ApiResponse(responseCode = "200", description = "Estado modificado exitosamente")
public ResponseEntity<Map<String, String>> cambiarEstado(
        @PathVariable int id,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "JSON con el nuevo estado a asignar",
            required = true,
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Ejemplo Cambio Estado",
                    value = "{\n  \"estado\": \"APROBADO\"\n}"
                )
            )
        )
        
        @RequestBody Map<String, String> request
) {

   vendedorService.cambiarEstado(id, request.get("estado"), "Cambio de estado manual");

    Map<String, String> response = new HashMap<>();
    response.put("mensaje", "Estado actualizado correctamente");

    return ResponseEntity.ok(response);
}

//oomo vendedor quiero poder publicar mi catalogo de productos para que los clientes pueda
@PostMapping("/{id}/catalogo")
@Operation(summary = "Publicar producto en catálogo", description = "Valida que el vendedor esté 'APROBADO' y delega la creación del producto al microservicio de productos via WebClient.")
@ApiResponse(responseCode = "201", description = "Producto publicado correctamente")
 @ApiResponse(responseCode = "403", description = "Operación denegada. El vendedor no está APROBADO")
public ResponseEntity<Map<String, Object>> publicarProducto(
        @PathVariable int id,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Datos del producto a publicar",
            required = true,
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Ejemplo Publicación Producto",
                    value = "{\n  \"nombre\": \"Taladro Inalámbrico\",\n  \"descripcion\": \"Taladro de 18V con batería incluida\",\n  \"precio\": 120000,\n  \"stock\": 15\n}"
                )
            )
        )   



        @RequestBody Map<String, Object> request
) {

Vendedor vendedor = vendedorService.getVendedorById(id);
    
    // Si no existe o no está APROBADO, se blowuea
    if (vendedor == null || !"APROBADO".equalsIgnoreCase(vendedor.getEstado())) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Operación denegada. Tu cuenta debe estar APROBADA para publicar productos.");
        errorResponse.put("estadoActual", vendedor == null ? "NO_EXISTE" : vendedor.getEstado());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse); 
    }


    Object producto = webClient.post()
            .uri("http://producto-service/api/v1/productos")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(Object.class)
            .block();

    Map<String, Object> response = new HashMap<>();
    response.put("mensaje", "Producto publicado correctamente");
    response.put("producto", producto);

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}

//coomo vendedor quiero poder ver catalogo de mis productos publicados 

@GetMapping("/{id}/productos")
@Operation(summary = "Ver productos del vendedor", description = "Consulta al servicio de productos los ítems pertenecientes a este vendedor.")
    @ApiResponse(responseCode = "200", description = "Catálogo del vendedor obtenido")
public ResponseEntity<Object> verProductos(@PathVariable int id) {

    Object productos = webClient.get()
            .uri("http://producto-service/api/v1/productos/vendedor/" + id)
            .retrieve()
            .bodyToMono(Object.class)
            .block();

    return ResponseEntity.ok(productos);
}


//como vendedor quiero poder actualizar precios y stock de mis productos publicados

@PutMapping("/{id}/catalogo/{productoId}")
@Operation(summary = "Actualizar precio y stock de un producto", description = "Envía una actualización al servicio de catálogo para modificar valores de un producto del vendedor.")
    @ApiResponse(responseCode = "200", description = "Producto actualizado correctamente")
public ResponseEntity<Map<String, String>> actualizarProducto(
        @PathVariable int id,
        @PathVariable int productoId,
        @RequestBody Map<String, Object> request
) {

    webClient.put()
            .uri("http://producto-service/api/v1/productos/" + productoId)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(Void.class)
            .block();

    Map<String, String> response = new HashMap<>();
    response.put("mensaje", "Producto actualizado correctamente");

    return ResponseEntity.ok(response);
}
//comunicacion  asincrona desd eel admin 


@PutMapping("/rechazar/{id}")
@Operation(summary = "Rechazar vendedor (Interno)", description = "Endpoint consumido por el microservicio de Administración para rechazar una postulación guardando observaciones.")
@ApiResponse(responseCode = "200", description = "Vendedor rechazado exitosamente")
public ResponseEntity<Void> rechazar( 
    @PathVariable Integer id, 
    // Agregamos la documentación de Swagger para el cuerpo aquí:
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Observación detallando el motivo del rechazo",
        required = false,
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "Ejemplo Observación Rechazo",
                value = "{\n  \"observaciones\": \"RUT de empresa inválido o no vigente\"\n}"
            )
        )
    )
    @RequestBody(required = false) Map<String, String> request // ◄--- Agregado para capturar la observación
) {
    System.out.println("VENDEDOR: recibió solicitud de rechazo del ADMIN ID " + id);
    
    // Extraemos la observación de rechazo de forma segura
    String obs = request != null ? request.getOrDefault("observaciones", "Rechazado sin observaciones") : "Rechazado";
    
    // Le pasamos el ID, el estado y la observación al service
    vendedorService.cambiarEstado(id, "RECHAZADO", obs);
    
    System.out.println("Vendedor rechazado ID: " + id + " con motivo: " + obs);
    return ResponseEntity.ok().build();
}

@PutMapping("/aprobar/{id}")
@Operation(summary = "Aprobar vendedor (Interno)", description = "Endpoint consumido por el microservicio de Administración para aprobar una postulación.")
@ApiResponse(responseCode = "200", description = "Vendedor aprobado exitosamente")
public ResponseEntity<?> aprobar(
    @PathVariable Integer id, 
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Observación de aprobación opcional",
        required = false,
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "Ejemplo Observación Aprobación", // 👈 Título adaptado
                value = "{\n  \"observaciones\": \"Documentación verificada de forma correcta\"\n}" // 👈 JSON adaptado para aprobación
            )
        )
    )
    
    @RequestBody(required = false) Map<String, String> request // ◄--- Agregado para capturar la observación
) {
    System.out.println("VENDEDOR: recibió solicitud del ADMIN ID " + id);
    
    // Extraemos la observación de aprobación de forma segura
    String obs = request != null ? request.getOrDefault("observaciones", "Aprobado sin observaciones") : "Aprobado";
    
    // Le pasamos el ID, el estado y la observación al service
    vendedorService.cambiarEstado(id, "APROBADO", obs);
    
    System.out.println("Vendedor aprobado ID: " + id + " con obs: " + obs);
    
    Map<String, String> respuesta = new HashMap<>();
    respuesta.put("mensaje", "Vendedor aprobado exitosamente");
    respuesta.put("estado", "APROBADO");


    return ResponseEntity.ok(respuesta);
}
@GetMapping("/verificar/{id}")
@Operation(summary = "Verificar existencia y estado", description = "Retorna el estado directo del vendedor o 'NO_EXISTE' si el ID no se encuentra.")
    @ApiResponse(responseCode = "200", description = "Estado retornado")
public ResponseEntity<String> verificarVendedor(
        @PathVariable Integer id
) {
    Vendedor vendedor = vendedorService.buscarPorId(id);

    if (vendedor == null) {
        return ResponseEntity.ok("NO_EXISTE");
    }

    return ResponseEntity.ok(vendedor.getEstado());
}

}



