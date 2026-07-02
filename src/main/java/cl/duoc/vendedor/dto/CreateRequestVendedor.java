package cl.duoc.vendedor.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data; 
import java.time.LocalDate;
public record  CreateRequestVendedor(

@NotBlank(message = "El nombre es obligatorio")
    String nombre,

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    String password,

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    String correo,

   @NotBlank(message = "El RUT de la empresa es obligatorio")
    String rutEmpresa, 

    @NotBlank(message = "Debe adjuntar la documentación requerida")
    String urlDocumentos, 

    @NotNull(message = "La fecha de postulación es obligatoria")
    LocalDate fechaPostulacion){


    }


