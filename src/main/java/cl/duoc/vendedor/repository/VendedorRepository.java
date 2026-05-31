package cl.duoc.vendedor.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import cl.duoc.vendedor.model.Vendedor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface VendedorRepository extends JpaRepository<Vendedor  ,Integer> {


  // buscar por correo (igual que admin)
    Vendedor findByCorreo(String correo);

    boolean existsByCorreo(String correo);

    // QUERY METHOD (por estado de postulación)
    List<Vendedor> findByEstado(String estado);

    // CUSTOM QUERY
    @Query(
        value = "SELECT * FROM vendedores WHERE estado = :estado",
        nativeQuery = true
    )
    List<Vendedor> buscarPorEstado(
            @Param("estado") String estado
    );
}





