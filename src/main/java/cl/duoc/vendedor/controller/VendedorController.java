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

@RestController
@RequestMapping("/api/v1/vendedores")

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
public ResponseEntity<List<Vendedor>> listarVendedores() {
    List<Vendedor> vendedores = vendedorService.getVendedores();
    return ResponseEntity.ok(vendedores);
}

//registra : crear postulacion 

@PostMapping
public ResponseEntity<Map<String, Object>> crearVendedor(
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
public ResponseEntity<Map<String, Object>> actualizarVendedor(
        @PathVariable int id,
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
public ResponseEntity<Map<String, Object>> verEstado(@PathVariable int id) {

    Vendedor vendedor = vendedorService.getVendedorById(id);

    Map<String, Object> response = new HashMap<>();
    response.put("id", vendedor.getId());
    response.put("estado", vendedor.getEstado());

    return ResponseEntity.ok(response);
}

@PutMapping("/{id}/estado")
public ResponseEntity<Map<String, String>> cambiarEstado(
        @PathVariable int id,
        @RequestBody Map<String, String> request
) {

   vendedorService.cambiarEstado(id, request.get("estado"), "Cambio de estado manual");

    Map<String, String> response = new HashMap<>();
    response.put("mensaje", "Estado actualizado correctamente");

    return ResponseEntity.ok(response);
}

//oomo vendedor quiero poder publicar mi catalogo de productos para que los clientes pueda
@PostMapping("/{id}/catalogo")
public ResponseEntity<Map<String, Object>> publicarProducto(
        @PathVariable int id,
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
//comunicacion

@PutMapping("/rechazar/{id}")
public ResponseEntity<Void> rechazar( @PathVariable Integer id, @RequestBody(required = false) Map<String, String> request // ◄--- Agregado para capturar la observación
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
public ResponseEntity<Void> aprobar(  @PathVariable Integer id, @RequestBody(required = false) Map<String, String> request // ◄--- Agregado para capturar la observación
) {
    System.out.println("VENDEDOR: recibió solicitud del ADMIN ID " + id);
    
    // Extraemos la observación de aprobación de forma segura
    String obs = request != null ? request.getOrDefault("observaciones", "Aprobado sin observaciones") : "Aprobado";
    
    // Le pasamos el ID, el estado y la observación al service
    vendedorService.cambiarEstado(id, "APROBADO", obs);
    
    System.out.println("Vendedor aprobado ID: " + id + " con obs: " + obs);
    return ResponseEntity.ok().build();
}
@GetMapping("/verificar/{id}")
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



