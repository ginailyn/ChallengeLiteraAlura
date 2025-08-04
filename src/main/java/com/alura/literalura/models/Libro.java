package com.alura.literalura.models;

import com.alura.literalura.dtos.Genero;
import com.alura.literalura.models.records.DatosLibro;
import com.alura.literalura.models.records.Media;
import com.alura.literalura.repository.iAutorRepository;
import jakarta.persistence.*;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ArrayList;

@Entity
@Table(name = "libros")
public class Libro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long libroId;

    @Column(unique = true)
    private String titulo;


    @ManyToOne(fetch = FetchType.EAGER)  // quitamos cascade para evitar borrados accidentales
    @JoinColumn(name = "autor_id")
    private com.alura.literalura.models.Autor autor;

    @Enumerated(EnumType.STRING)
    private Genero genero;

    private String idioma;

    private String imagen;

    private Long cantidadDescargas;



    //  Constructor por defecto ( JPA lo requiere)
    public Libro() {
    }


    public Libro(DatosLibro datosLibro) {
        this.libroId = datosLibro.libroId();
        this.titulo = datosLibro.titulo();
        this.genero = generoModificado(datosLibro.genero());
        this.idioma = idiomaModificado(datosLibro.idioma());
        this.imagen = imagenModificada(datosLibro.imagen());
        this.cantidadDescargas = datosLibro.cantidadDescargas();
    }


    private com.alura.literalura.models.Autor obtenerAutorPersistido(List<com.alura.literalura.models.records.Autor> autoresAPI, iAutorRepository autorRepository) {
        if (autoresAPI == null || autoresAPI.isEmpty()) return null;

        com.alura.literalura.models.records.Autor autorAPI = autoresAPI.get(0);
        String nombreAutor = autorAPI.nombre();

        // Buscar autor en base de datos por nombre
        Optional<com.alura.literalura.models.Autor> autorEnBD = autorRepository.findFirstByNombreIgnoreCase(nombreAutor);

        if (autorEnBD.isPresent()) {
            return autorEnBD.get();
        } else {
            // Si no existe, crear nuevo autor con los datos
            com.alura.literalura.models.Autor nuevoAutor = new com.alura.literalura.models.Autor(autorAPI);
            return nuevoAutor;
        }
    }


    public Libro(Libro libro) {
        this.id = libro.id;
        this.libroId = libro.libroId;
        this.titulo = libro.titulo;
        this.autor = libro.autor;
        this.genero = libro.genero;
        this.idioma = libro.idioma;
        this.imagen = libro.imagen;
        this.cantidadDescargas = libro.cantidadDescargas;
    }

    private Genero generoModificado(List<String> generos) {
        if (generos == null || generos.isEmpty()) {
            return Genero.DESCONOCIDO;
        }
        Optional<String> firstGenero = generos.stream()
                .map(g -> {
                    int index = g.indexOf("--");
                    return index != -1 ? g.substring(index + 2).trim() : null;
                })
                .filter(Objects::nonNull)
                .findFirst();
        return firstGenero.map(Genero::fromString).orElse(Genero.DESCONOCIDO);
    }

    private String idiomaModificado(List<String> idiomas) {
        if (idiomas == null || idiomas.isEmpty()) {
            return "Desconocido";
        }
        return idiomas.get(0);
    }

    private String imagenModificada(Media media) {
        if (media == null || media.imagen().isEmpty()) {
            return "Sin imagen";
        }
        return media.imagen();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLibroId() {
        return libroId;
    }

    public void setLibroId(Long libroId) {
        this.libroId = libroId;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }


    // Setter actualizado para mantener bidireccionalidad
    public void setAutor(com.alura.literalura.models.Autor autor) {
        this.autor = autor;
        if (autor != null) {
            if (autor.getLibros() == null) {
                autor.setLibros(new ArrayList<>());
            }
            if (!autor.getLibros().contains(this)) {
                autor.getLibros().add(this);
            }
        }
    }

    public com.alura.literalura.models.Autor getAutor() {
        return autor;
    }




    public Genero getGenero() {
        return genero;
    }

    public void setGenero(Genero genero) {
        this.genero = genero;
    }

    public String getIdioma() {
        return idioma;
    }

    public void setIdioma(String idioma) {
        this.idioma = idioma;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public Long getCantidadDescargas() {
        return cantidadDescargas;
    }

    public void setCantidadDescargas(Long cantidadDescargas) {
        this.cantidadDescargas = cantidadDescargas;
    }

    @Override
    public String toString() {
        String lineaSuperior = "╔═════════════════════════════════════════════════╗";
        String lineaMedia = "╠═════════════════════════════════════════════════╣";
        String lineaInferior = "╚═════════════════════════════════════════════════╝";

        String autorNombre = (autor != null ? autor.getNombre() : "N/A");

        String tituloFormateado = String.format("║   TÍTULO: %-38s║", titulo);
        String autorFormateado = String.format("║   AUTOR: %-39s║", autorNombre);
        String idiomaFormateado = String.format("║   IDIOMA: %-38s║", idioma);
        String descargasFormateado = String.format("║   DESCARGAS: %-35d║", cantidadDescargas);

        return "\n" + lineaSuperior + "\n" +
                "║              INFORMACIÓN DEL LIBRO              ║\n" +
                lineaMedia + "\n" +
                tituloFormateado + "\n" +
                autorFormateado + "\n" +
                idiomaFormateado + "\n" +
                descargasFormateado + "\n" +
                lineaInferior;
    }
}
