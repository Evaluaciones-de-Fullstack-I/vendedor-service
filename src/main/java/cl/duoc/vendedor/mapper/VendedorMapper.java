package cl.duoc.vendedor.mapper;
import cl.duoc.vendedor.model.Vendedor;
import cl.duoc.vendedor.dto.CreateRequestVendedor;
import cl.duoc.vendedor.dto.UpdateRequestVendedor;

import java.time.LocalDate;

public class VendedorMapper {

  // CREATE
    public static Vendedor toVendedor(CreateRequestVendedor request) {

        Vendedor vendedor = new Vendedor();

        vendedor.setNombre(request.nombre());
        vendedor.setCorreo(request.correo());
        vendedor.setPassword(request.password());

        // se asigna automáticamente
        vendedor.setFechaPostulacion(LocalDate.now());

        return vendedor;
    }

    // UPDATE
    public static void updateVendedor(
            Vendedor vendedor,
            UpdateRequestVendedor request
    ) {

        vendedor.setNombre(request.nombre());
        vendedor.setCorreo(request.correo());
        vendedor.setPassword(request.password());
    }
}



