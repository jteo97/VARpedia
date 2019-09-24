package application;

import javafx.concurrent.Task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class DownloadImagesTask extends Task<Void> {

    private String _searchTerm, _creationName;
    private int _numImages;

    private List<String> _imageList;

    public DownloadImagesTask(String wikiSearchTerm, String nameOfCreation, int numImages) {
        _searchTerm = wikiSearchTerm;
        _numImages = numImages;
        _creationName = nameOfCreation;
    }

    @Override
    protected Void call() {
        List<String> _imageList = getImages(_numImages);
        downloadImages(_imageList);
        return null;
    }

    private void downloadImages(List<String> urls) {
        int count = 1;
        for (String s: urls) {
            try(InputStream in = new URL(s).openStream()){
                Files.copy(in, Paths.get(Main.getCreationDirectory() + "/" + _creationName + "/image" + count));
                count++;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private List<String> getImages(int numImages) {
        String url = "https://www.flickr.com/search/?text=" + _searchTerm;
        String html = "";
        try {
            URL urlObj = new URL(url);
            BufferedReader input = new BufferedReader((new InputStreamReader(urlObj.openStream())));

            String line;
            while ((line = input.readLine()) != null) {
                html += line;
            }
            input.close();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<String> finalImageList = new ArrayList<String>();

        for (String word: html.split(" ")) {
            if (word.matches(".*live.staticflickr.com.*") && word.matches("(?i)url.*")) {

                word = word.replace("url(//","http://");
                word = word.replace(")\"","");

                word = finalURL(word);
                finalImageList.add(word);
            }
        }

        finalImageList = finalImageList.subList(0, numImages);
        return finalImageList;
    }

    private String finalURL(String url) {
        HttpURLConnection con;
        try {
            con = (HttpURLConnection) new URL(url).openConnection();
            con.setInstanceFollowRedirects(false);
            con.connect();
            return con.getHeaderField("Location").toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}