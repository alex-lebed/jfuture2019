package dev.jfuture.task.parser.movie.impl;

import dev.jfuture.task.entity.Movie;
import dev.jfuture.task.exception.WebsiteParsingException;
import dev.jfuture.task.parser.movie.MoviesParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public abstract class AbstractMoviesParser implements MoviesParser {

    private static final String TABLE_TAG = "table";
    private static final String ROW_TAG = "tr";
    private static final String CELL_TAG = "td";
    private static final int HEADER_INDEX = 0;

    @Override
    public List<Movie> getMoviesByYear(int year) throws WebsiteParsingException {
        try{
            String url = generateUrlByYear(year);
            Document document = Jsoup.connect(url).get();
            Elements tables = document.select(TABLE_TAG);
            List<Movie> allMovies = new ArrayList<>();
            for(Element table : tables){
                if(isMovieTable(table)){
                    List<Movie> movies = parseMovieTable(table, year);
                    allMovies.addAll(movies);
                }
            }
            return allMovies;
        } catch (IOException ex){
            throw new WebsiteParsingException(ex.getMessage(), ex);
        }
    }

    protected abstract String getMovieTableQualifier();

    protected abstract Movie buildMovie(Elements columns, int year);

    protected abstract Map<Integer, String> getSources();

    protected abstract String getGenresSeparator();

    /*package-private*/ List<String> separateGenres(String genresStr){
        String genresSeparator = getGenresSeparator();
        String[] genresArray = genresStr.split(genresSeparator);
        return Arrays.asList(genresArray);
    }

    /*package-private*/ String getByIndex(Elements columns, int index) {
        Element element = columns.get(index);
        return element.text();
    }

    private List<Movie> parseMovieTable(Element table, int year){
        Elements rows = table.select(ROW_TAG);
        removeHeader(rows);
        List<Movie> movies = new ArrayList<>();
        for(Element row : rows){
            Elements columns = row.select(CELL_TAG);
            Movie movie = buildMovie(columns, year);
            movies.add(movie);
        }
        return movies;
    }

    private void removeHeader(Elements rows){
        rows.remove(HEADER_INDEX);
    }

    private String generateUrlByYear(int year){
        Map<Integer, String> sources = getSources();
        return sources.get(year);
    }

    private boolean isMovieTable(Element table){
        String movieTableQualifier = getMovieTableQualifier();
        String tableText = table.text();
        return tableText.contains(movieTableQualifier);
    }

}
