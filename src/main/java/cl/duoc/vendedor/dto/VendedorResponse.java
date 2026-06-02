package cl.duoc.vendedor.dto;
import lombok.Data;

@Data

public class VendedorResponse {

    private Integer id;
    private String nombre;
    private String estado;

    // getters y setters


public void setId(Integer id) {
    this.id = id;
}}