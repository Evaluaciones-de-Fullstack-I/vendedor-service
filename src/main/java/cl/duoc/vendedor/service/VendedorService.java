package cl.duoc.vendedor.service;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import cl.duoc.vendedor.model.Vendedor;
import cl.duoc.vendedor.repository.VendedorRepository;
import cl.duoc.vendedor.dto.UpdateRequestVendedor;
import cl.duoc.vendedor.mapper.VendedorMapper;
import cl.duoc.vendedor.exception.ResourceNotFoundException;
import cl.duoc.vendedor.dto.CreateRequestVendedor;
import cl.duoc.vendedor.dto.VendedorResponse;
@Service
public class VendedorService {

private VendedorRepository vendedorRepository;
private final WebClient webClient;

public VendedorService(
        VendedorRepository vendedorRepository,
        WebClient webClient
) {
        this.vendedorRepository = vendedorRepository;
        this.webClient = webClient;
}

  // LISTAR VENDEDORES
    public List<Vendedor> getVendedores() {
        return vendedorRepository.findAll();
    }

    // CREAR POSTULACIÓN 
    public Vendedor saveVendedor(Vendedor vendedor) {
        vendedor.setEstado("PENDIENTE"); // clave de negocio marketplace
        return vendedorRepository.save(vendedor);
    }

    // BUSCAR POR ID
    public Vendedor getVendedorById(int id) {
        return vendedorRepository.findById(id)
                .orElse(null);
    }

    // ACTUALIZAR
    public Vendedor updateVendedor(int id, UpdateRequestVendedor request) {

        Vendedor vendedor = vendedorRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Vendedor no encontrado"));

        VendedorMapper.updateVendedor(vendedor, request);

        return vendedorRepository.save(vendedor);
    }

    // ELIMINAR
    public boolean deleteVendedor(int id) {

        Optional<Vendedor> v = vendedorRepository.findById(id);

        if (v.isPresent()) {
            vendedorRepository.delete(v.get());
            return true;
        } else {
            throw new ResourceNotFoundException("Vendedor no encontrado");
        }
    }

    // ESTADO POSTULACIÓN
    public String getEstado(int id) {

        Vendedor v = vendedorRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Vendedor no encontrado"));

        return v.getEstado();
    }

    public void cambiarEstado(int id, String estado) {

        Vendedor v = vendedorRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Vendedor no encontrado"));

        v.setEstado(estado);
        vendedorRepository.save(v);
    }



    // Método para encontrar un vendedor por su ID
    public Vendedor findById(Integer id) {
        return vendedorRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Vendedor no encontrado"));
    }
}


