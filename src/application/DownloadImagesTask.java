package application;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import javax.imageio.ImageIO;

import com.flickr4java.flickr.*;
import com.flickr4java.flickr.photos.*;
import javafx.concurrent.Task;

/**
 * The task for the worker thread to run in order to download images from Flickr in the background
 * @author Tommy Shi and Justin Teo
 *
 */
public class DownloadImagesTask extends Task<Void> {

    private String _searchImageTerm;
    private int _numImages;
    private String _destinationFolder;

    public DownloadImagesTask(String destinationFolder, String searchImageTerm, int numImages) {
        this._searchImageTerm = searchImageTerm;
        this._numImages = numImages;
        this._destinationFolder = destinationFolder;

    }

    public static String getAPIKey(String key) throws Exception {

        String config = System.getProperty("user.dir")
                + System.getProperty("file.separator")+ "flickr-api-keys.txt";

        File file = new File(config);
        BufferedReader br = new BufferedReader(new FileReader(file));

        String line;
        while ( (line = br.readLine()) != null ) {
            if (line.trim().startsWith(key)) {
                br.close();
                return line.substring(line.indexOf("=")+1).trim();
            }
        }
        br.close();
        throw new RuntimeException("Couldn't find " + key +" in config file "+file.getName());
    }

    @Override
    protected Void call() throws Exception {
        try {
            String apiKey = getAPIKey("apiKey");
            String sharedSecret = getAPIKey("sharedSecret");

            Flickr flickr = new Flickr(apiKey, sharedSecret, new REST());

            int page = 0;

            PhotosInterface photos = flickr.getPhotosInterface();
            SearchParameters params = new SearchParameters();
            params.setSort(SearchParameters.RELEVANCE);
            params.setMedia("photos");
            params.setText(_searchImageTerm);

            PhotoList<Photo> results = photos.search(params, _numImages, page);

            int count = 0;
            for (Photo photo: results) {
                count++;
                try {
                    BufferedImage image = photos.getImage(photo,Size.LARGE);
                    String filename = "image" + count + ".jpg";
                    File outputFile = new File(_destinationFolder,filename);
                    ImageIO.write(image, "jpg", outputFile);
                } catch (FlickrException fe) {
                	
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
