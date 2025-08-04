package com.alura.literalura.models;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "autores")
public class Autor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    private Integer cumpleanios;

    private Integer fechaFallecimiento;

    @OneToMany(mappedBy = "autor", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Libro> libros = new ArrayList<>();

    public Autor() {
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public Integer getCumpleanios() {
        return cumpleanios;
    }

    public Integer getFechaFallecimiento() {
        return fechaFallecimiento;
    }

    public List<Libro> getLibros() {
        return libros;
    }

    public void setLibros(List<Libro> libros) {
        this.libros = libros;
    }

    public Autor(com.alura.literalura.models.records.Autor autor) {
        this.nombre = autor.nombre();
        this.cumpleanios = autor.cumpleanios();
        this.fechaFallecimiento = autor.fechaFallecimiento();
    }

    // Agregar método para mantener consistencia bidireccional
    public void addLibro(Libro libro) {
        if (!libros.contains(libro)) {
            libros.add(libro);
            libro.setAutor(this);
        }
    }

    @Override
    public String toString() {
        String lineaSuperior = "╔═════════════════════════════════════════════════════════╗";
        String lineaMedia = "╠═════════════════════════════════════════════════════════╣";
        String lineaInferior = "╚═════════════════════════════════════════════════════════╝";

        String nombreCompleto = nombre != null ? nombre : "N/A";
        String nacimiento = cumpleanios != null ? cumpleanios.toString() : "Desconocido";
        String fallecimiento = fechaFallecimiento != null ? fechaFallecimiento.toString() : "Desconocido";

        String librosString = (libros != null && !libros.isEmpty())
                ? libros.stream().map(Libro::getTitulo).toList().toString()
                : "[Sin libros registrados]";

        return "\n" + lineaSuperior + "\n" +
                "║                          AUTOR                          ║\n" +
                lineaMedia + "\n" +
                String.format("║   AUTOR: %-47s║\n", nombreCompleto) +
                String.format("║   FECHA DE NACIMIENTO: %-33s║\n", nacimiento) +
                String.format("║   FECHA DE FALLECIMIENTO: %-30s║\n", fallecimiento) +
                String.format("║   LIBROS: %-46s║\n", librosString) +
                lineaInferior;
    }
}
