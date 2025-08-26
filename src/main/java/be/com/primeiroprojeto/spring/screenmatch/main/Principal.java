package be.com.primeiroprojeto.spring.screenmatch.main;

import be.com.primeiroprojeto.spring.screenmatch.model.DadosEpisodio;
import be.com.primeiroprojeto.spring.screenmatch.model.DadosSerie;
import be.com.primeiroprojeto.spring.screenmatch.model.DadosTemporada;
import be.com.primeiroprojeto.spring.screenmatch.model.Episodio;
import be.com.primeiroprojeto.spring.screenmatch.service.ConsumoAPI;
import be.com.primeiroprojeto.spring.screenmatch.service.ConverterDados;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Principal {
private Scanner leitura = new Scanner(System.in);
private ConsumoAPI consumo = new ConsumoAPI();
private ConverterDados conversor = new ConverterDados();
private final String ENDERECO = "https://www.omdbapi.com/?t=";
private final String API_KEY = "&apikey=a822a7e2";
public void exibeMenu(){
    System.out.println("Digite o nome da série para busca");
    var nomeSerie = leitura.nextLine();
    var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
    DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
    System.out.println(dados);


    List<DadosTemporada> temporadas = new ArrayList<>();

    for (int i = 1; i <= dados.totalTemporadas(); i++) {
        json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + "&season=" + i + API_KEY);
        DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
        temporadas.add(dadosTemporada);
    }
    temporadas.forEach(System.out::println);
        for (int i = 0; i < dados.totalTemporadas(); i++) {
            List<DadosEpisodio> episodiosTemporada = temporadas.get(i).episodios();
            for (int j = 0; j < episodiosTemporada.size(); j++) {
                System.out.println(episodiosTemporada.get(j).titulo());
            }
        }


    temporadas.forEach(t -> t.episodios().forEach(e -> e.titulo()));
    List<DadosEpisodio> dadosEpisodios = temporadas.stream()
            .flatMap(t -> t.episodios().stream())
            .collect(Collectors.toList());


    dadosEpisodios.stream()
            .filter(e -> !e.avaliacao().equalsIgnoreCase("N/A"))
            .peek(e -> System.out.println("Primeiro filtro(N/A) " + e))
            .sorted(Comparator.comparing(DadosEpisodio::avaliacao).reversed())
            .peek(e -> System.out.println("Ordenação " + e))
            .limit(10)
            .peek(e -> System.out.println("Limite " + e))
            .map(e -> e.titulo().toUpperCase())
            .peek(e -> System.out.println("Mapeamento" + e))
            .forEach(System.out::println);
    
    
    List<Episodio> episodios = temporadas.stream()
            .flatMap(t -> t.episodios().stream()
                    .map(d -> new Episodio(t.numero(), d))
            ).collect(Collectors.toList());
    episodios.forEach(System.out::println);

    
    System.out.println("Digite o nome do episódio para busca");
    var trechoTitulo = leitura.nextLine();
    Optional<Episodio> episodioBuscado = episodios.stream()
            .filter(n -> n.getTitulo().toUpperCase().contains(trechoTitulo.toUpperCase()))
            .findFirst();
    if (episodioBuscado.isPresent()){
        System.out.println("Episódio encontrado!");
        System.out.println("Temporada: " + episodioBuscado.get().getTemporada());
    } else {
        System.out.println("Episódio não encontrado!");
    }

    System.out.println("A partir de que ano você deseja ver os episodios? ");
    var ano = leitura.nextInt();
    leitura.nextLine();

    
    LocalDate dataBusca = LocalDate.of(ano, 1, 1);

    DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM//yyyy");
    episodios.stream().filter(e -> e.getDataLancamento() != null && e.getDataLancamento().isAfter(dataBusca))
            .forEach(e -> System.out.println(
                    "Temporada: " + e.getTemporada() + " Episódio: " + e.getTitulo() + " Data Lançamento: "
                            + e.getDataLancamento().format(formatador)
            ));
    
    
    Map<Integer, Double> avaliacoesPorTemporada = episodios.stream()
            .filter(e -> e.getAvaliacao() > 0.0)
            .collect(Collectors.groupingBy(Episodio::getTemporada,
                    Collectors.averagingDouble(Episodio::getAvaliacao)));

    
    DoubleSummaryStatistics est = episodios.stream()
            .filter(e -> e.getAvaliacao() > 0.0)
            .collect(Collectors.summarizingDouble(Episodio::getAvaliacao));
    System.out.println("Média: " + est.getAverage());
    System.out.println("Melhor episódio: " + est.getMax());
    System.out.println("Pior episódio: " + est.getMin());
    System.out.println("Quantidade: " + est.getCount());

}
}
