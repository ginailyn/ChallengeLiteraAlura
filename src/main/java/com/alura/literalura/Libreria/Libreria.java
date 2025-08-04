package com.alura.literalura.Libreria;

import com.alura.literalura.config.ConsumirApiGutendex;
import com.alura.literalura.config.ConvertirDatos;
import com.alura.literalura.models.Autor;
import com.alura.literalura.models.Libro;
import com.alura.literalura.models.LibrosRespuestaApi;
import com.alura.literalura.models.records.DatosLibro;
import com.alura.literalura.repository.iAutorRepository;
import com.alura.literalura.repository.iLibroRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

public class Libreria {

    private final Scanner sc = new Scanner(System.in);
    private final ConsumirApiGutendex consumoApi = new ConsumirApiGutendex();
    private final ConvertirDatos convertir = new ConvertirDatos();

    private static final String API_BASE = "https://gutendex.com/books/?search=";

    private final iLibroRepository libroRepository;
    private final iAutorRepository autorRepository;

    public Libreria(iLibroRepository libroRepository, iAutorRepository autorRepository) {
        this.libroRepository = libroRepository;
        this.autorRepository = autorRepository;
    }

    public void consumo() {
        int opcion = -1;
        while (opcion != 0) {
            String menu = """
                    ╔════════════════════════════════════════════════════════╗
                    ║             BIENVENIDO(A)  A LITERALURA                ║
                    ╠════════════════════════════════════════════════════════╣
                    ║      1 - Buscar libro por Título                       ║
                    ║      2 - Listar libros registrados                     ║
                    ║      3 - Listar Autores registrados                    ║
                    ║      4 - Listar Autores vivos en un determinado año    ║
                    ║      5 - Listar Libros por Idioma                      ║
                    ║      6 - Top 10 Libros mas Descargados                 ║
                    ║      7 - Buscar Autor por Nombre                       ║
                    ║                                                        ║
                    ║      0 - Salir                                         ║
                    ╚════════════════════════════════════════════════════════╝
                    Por favor, ingrese una opción válida del menú
                    """;

            System.out.println(menu);
            try {
                opcion = sc.nextInt();
                sc.nextLine();
            } catch (InputMismatchException e) {
                System.out.println("Por favor, ingrese una opción válida que se encuentre en el menú");
                sc.nextLine();
                continue;
            }

            switch (opcion) {
                case 1 -> buscarEnLaApi();
                case 2 -> listarLibrosRegistrados();
                case 3 -> listarAutoresRegistrados();
                case 4 -> listarAutoresVivosPorAnio();
                case 5 -> listarLibrosPorIdioma();
                case 6 -> top10LibrosMasDescargados();
                case 7 -> buscarAutorPorNombre();
                case 0 -> System.out.println("Gracias por usar nuestra Aplicación, ¡Hasta Luego!\n");
                default -> System.out.println("La opción ingresada fue incorrecta. Intente nuevamente.");
            }
        }
    }



    private DatosLibro getDatosLibro() {
        System.out.print("Por favor, Ingrese el Título(Nombre) del libro: ");
        String nombreLibro = sc.nextLine().toLowerCase();

        String urlConsulta = API_BASE + nombreLibro.replace(" ", "%20");
        var json = consumoApi.obtenerDatos(urlConsulta);

        LibrosRespuestaApi datos = convertir.convertirDatosJsonAJava(json, LibrosRespuestaApi.class);

        if (datos != null && datos.getResultadoLibros() != null && !datos.getResultadoLibros().isEmpty()) {
            return datos.getResultadoLibros().get(0);
        } else {
            return null;
        }
    }

    private void buscarEnLaApi() {
        DatosLibro datosLibro = getDatosLibro();

        if (datosLibro == null) {
            System.out.println("Libro no encontrado en la API Gutendex.");
            return;
        }

        String tituloLibro = datosLibro.titulo();
        if (libroRepository.existsByTitulo(tituloLibro)) {
            System.out.println("El libro ya se encuentra registrado en la base de datos.");
            return;
        }

        // Obtener o crear autor
        var autorRecord = datosLibro.autor() != null && !datosLibro.autor().isEmpty()
                ? datosLibro.autor().get(0)
                : null;

        Autor autorEntidad = null;
        if (autorRecord != null) {
            autorEntidad = autorRepository.findFirstByNombreIgnoreCase(autorRecord.nombre())
                    .orElseGet(() -> {
                        Autor nuevo = new Autor(autorRecord);
                        return autorRepository.save(nuevo);  //  persiste si es nuevo
                    });
        }

        // Crear libro y asignar autor
        Libro nuevoLibro = new Libro(datosLibro);
        nuevoLibro.setAutor(autorEntidad);      //  establece relación bidireccional internamente

        libroRepository.save(nuevoLibro);       //  persistir libro
        System.out.println("Libro guardado con éxito:\n" + nuevoLibro);
    }

    @Transactional(readOnly = true)
    private void listarLibrosRegistrados() {
        List<Libro> libros = libroRepository.findAll();
        if (libros.isEmpty()) {
            System.out.println("No existen libros en la Base de Datos de Literalura.");
        } else {
            System.out.println("Libros encontrados en la Base de Datos de Literalura:");
            libros.forEach(libro -> System.out.println(libro.toString()));
        }
    }

    private void listarAutoresRegistrados() {
        List<Autor> autores = autorRepository.findAll();

        if (autores.isEmpty()) {
            System.out.println("No se encontraron autores en la base de datos.");
        } else {
            System.out.println("Autores registrados en la base de datos:\n");

            Set<String> autoresUnicos = new HashSet<>();
            for (Autor autor : autores) {
                if (autor.getNombre() != null && autoresUnicos.add(autor.getNombre())) {
                    System.out.println(autor.toString()); // Aquí usamos el nuevo toString()
                }
            }
        }
    }



    private void listarLibrosPorIdioma() {
        System.out.println("""
            ╔═════════════════════════════════════════════╗
            ║         BUSCAR LIBROS POR IDIOMA            ║
            ╠═════════════════════════════════════════════╣
            ║           es : Libros en español.           ║
            ║           en : Libros en inglés.            ║
            ║           fr : Libros en francés.           ║
            ║           pt : Libros en portugués.         ║
            ╚═════════════════════════════════════════════╝
            """);

        String idioma;
        while (true) {
            System.out.print("Por favor, ingrese las dos letras del idioma (es, en, fr, pt): ");
            idioma = sc.nextLine().trim().toLowerCase();

            if (idioma.equals("es") || idioma.equals("en") || idioma.equals("fr") || idioma.equals("pt")) {
                break;
            } else {
                System.out.println("Código de idioma inválido. Intente nuevamente con: es, en, fr, pt.");
            }
        }

        List<Libro> librosPorIdioma = libroRepository.findByIdioma(idioma);

        if (librosPorIdioma.isEmpty()) {
            System.out.println("No se encontraron libros del idioma seleccionado en la base de datos.");
        } else {
            System.out.println("Libros encontrados según el idioma seleccionado:");
            librosPorIdioma.forEach(libro -> System.out.println(libro.toString()));
        }
    }

    private void listarAutoresVivosPorAnio() {
        System.out.print("Por favor, indique el año para consultar autores vivos: ");
        int anioBuscado;
        try {
            anioBuscado = sc.nextInt();
            sc.nextLine();
        } catch (InputMismatchException e) {
            System.out.println("Por favor, ingrese un año válido.");
            sc.nextLine();
            return;
        }

        List<Autor> autoresVivos = autorRepository.findByCumpleaniosLessThanOrFechaFallecimientoGreaterThanEqual(anioBuscado, anioBuscado);

        if (autoresVivos.isEmpty()) {
            System.out.println("No se encontraron autores que estuvieran vivos en el año " + anioBuscado + ".");
        } else {

            System.out.println("Autores que estaban vivos en el año " + anioBuscado + ":\n");
            Set<String> autoresUnicos = new HashSet<>();
            for (Autor autor : autoresVivos) {
                if (autor.getCumpleanios() != null && autor.getFechaFallecimiento() != null) {
                    if (autor.getCumpleanios() <= anioBuscado && autor.getFechaFallecimiento() >= anioBuscado) {
                        if (autoresUnicos.add(autor.getNombre())) {
                            System.out.println(autor);  // Usa el toString con formato visual
                        }
                    }
                }
            }
        }
    }

    private void top10LibrosMasDescargados() {
        List<Libro> top10Libros = libroRepository.findTop10ByTituloByCantidadDescargas();

        if (top10Libros.isEmpty()) {
            System.out.println("No hay libros registrados para mostrar el top 10.");
            return;
        }

        int index = 1;
        System.out.println("\n\nLista de los libros más descargados registrados en la LiterAlura\n");
        for (Libro libro : top10Libros) {


                    System.out.printf("Libro %d: %s | Autor: %s | Descargas: %d%n",
                    index, libro.getTitulo(),
                    libro.getAutor() != null ? libro.getAutor().getNombre() : "N/A",
                    libro.getCantidadDescargas());
            index++;
        }
    }

    private void buscarAutorPorNombre() {
        System.out.print("Ingrese nombre del escritor que quiere buscar: ");
        String escritor = sc.nextLine().trim();

        // Validar que solo contenga letras y espacios
        if (!escritor.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$")) {
            System.out.println("Nombre inválido. Solo se permiten letras y espacios.");
            return;
        }

        Optional<Autor> escritorBuscado = autorRepository.findFirstByNombreContainsIgnoreCase(escritor);
        if (escritorBuscado.isPresent()) {
            System.out.println("\nEl escritor buscado fue: " + escritorBuscado.get().getNombre());
        } else {
            System.out.println("\nEl escritor con el nombre '" + escritor + "' no se encontró.");
        }
    }
}

